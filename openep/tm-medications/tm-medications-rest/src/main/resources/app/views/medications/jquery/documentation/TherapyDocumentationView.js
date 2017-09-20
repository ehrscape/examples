/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */
Class.define('app.views.medications.documentation.TherapyDocumentationView', 'tm.jquery.Container', {
  cls: "documentation-view-container",
  view: null,

  _angularContainer: null,
  _angularApp: null,
  _angularScope: null,
  _activeRefreshTask: null,
  _prescribedViewActions: null,

  angularReady: false,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;

    this._prescribedViewActions = [
      tm.views.medications.TherapyView.VIEW_ACTION_CANCEL_PRESCRIPTION,
      tm.views.medications.TherapyView.VIEW_ACTION_OUTPATIENT_PRESCRIPTION,
      tm.views.medications.TherapyView.VIEW_ACTION_DELETE_OUTPATIENT_PRESCRIPTION,
      tm.views.medications.TherapyView.VIEW_ACTION_GET_EXTERNAL_OUTPATIENT_PRESCRIPTIONS
    ];

    this._buildGui();

    this.getAngularContainer().on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      self._deinitializeAngular(true);
      angular.module('tm.angular.medications.documentation.TherapyDocumentationApp')
          .constant('ViewActionName', {
            cancelPrescription: tm.views.medications.TherapyView.VIEW_ACTION_CANCEL_PRESCRIPTION,
            outpatientPrescription: tm.views.medications.TherapyView.VIEW_ACTION_OUTPATIENT_PRESCRIPTION,
            deleteOutpatientPrescription: tm.views.medications.TherapyView.VIEW_ACTION_DELETE_OUTPATIENT_PRESCRIPTION,
            updateOutpatientPrescription: tm.views.medications.TherapyView.VIEW_ACTION_UPDATE_OUTPATIENT_PRESCRIPTION,
            getExternalOutpatientPrescriptions: tm.views.medications.TherapyView.VIEW_ACTION_GET_EXTERNAL_OUTPATIENT_PRESCRIPTIONS,
            authorizeOutpatientPrescription: tm.views.medications.TherapyView.VIEW_ACTION_AUTHORIZE_OUTPATIENT_PRESCRIPTION
          })
          .value("SignalAngularReady", self._markAngularReadyClosure())
          .value("ActiveInitData", {
            isTablet: tm.jquery.ClientUserAgent.isTablet(),
            editAllowed: self.getView().getTherapyAuthority().isEditAllowed(),
            language: self.getView().getViewLanguage()
          })
          .value("ActiveUpdateData", {
            casTicket: self.getView().getCasTicket(),
            patientId: self.getView().getPatientId()
          })
          .config(['tm.angularjs.common.tmcBridge.ViewProxyProvider', function(viewProxyProvider)
          {
            viewProxyProvider.setAppView(self.getView());
          }])
          .run(['$rootScope', '$http', function($rootScope, $http)
          {
            $http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";
          }]);

      var componentElement = angular.element($("#" + component.getId()));
      self._angularApp = angular.bootstrap(componentElement, ['tm.angular.medications.documentation.TherapyDocumentationApp']);
      self._angularScope = componentElement.scope();
    });
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var angContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%"),
      cls: "tm-flexboxlayout direction-vertical-nowrap justify-content-flex-start align-items-stretch align-content-center", /* layout property doesn't work, probably due to html content */
      html: '<tm-meds-therapy-documentation-view></tm-meds-therapy-documentation-view>'
    });

    this.add(angContainer);

    this._angularContainer = angContainer;
  },

  _markAngularReadyClosure: function()
  {
    var self = this;
    return function()
    {
      self.angularReady = true;
    };
  },

  _deinitializeAngular: function(destroyScope)
  {
    this.angularReady = false;
    if (!tm.jquery.Utils.isEmpty(this._angularApp) && destroyScope)
    {
      var rootScope = this._angularApp.get('$rootScope');
      if (rootScope)
      {
        rootScope.$destroy();
      }
    }
    this._angularScope = null;
  },

  getView: function()
  {
    return this.view;
  },

  getAngularContainer: function()
  {
    return this._angularContainer;
  },

  /**
   * @returns {Object | null}
   */
  getAngularScope: function()
  {
    return this._angularScope;
  },

  refreshData: function()
  {
    var self = this;

    if (tm.jquery.Utils.isEmpty(this._activeRefreshTask) == false)
    {
      this._activeRefreshTask.abort();
    }
    this._activeRefreshTask = this.getView().getAppFactory().createConditionTask(
        function()
        {
          self._angularEmit('refreshData', {patientId: self.getView().getPatientId()});
          self._activeRefreshTask = null;
        },
        function()
        {
          return self.isRendered() && self.isAngularReady();
        },
        70, 150
    );
  },

  clearData: function()
  {
    this._angularEmit('clearData');
  },

  isAngularReady: function()
  {
    return this.angularReady;
  },

  /**
   * Sends a command with the specified parameters to the main Angular controller.
   * @param {string} command
   * @param {object} params
   * @private
   */
  _angularEmit: function(command, params)
  {
    var scope = this.getAngularScope();
    if (scope)
    {
      scope.$emit(command, params);
    }
  },

  onViewActionCallback: function(actionCallback)
  {
    this.getView().getLocalLogger().info("onViewActionCallback in TherapyDocumentationView triggered.");

    if (!tm.jquery.Utils.isEmpty(actionCallback) && actionCallback.successful)
    {
      this._angularEmit(actionCallback.action, actionCallback.actionData);
    }
  },

  /**
   * Returns an array of valid view action names for which callbacks should be passed along when the view is active.
   * @returns {Array}
   */
  getPrescribedViewActions: function()
  {
    return this._prescribedViewActions;
  },

  /**
   * @Override
   */
  destroy: function()
  {
    this.callSuper();
    this._deinitializeAngular(true);
  }
});