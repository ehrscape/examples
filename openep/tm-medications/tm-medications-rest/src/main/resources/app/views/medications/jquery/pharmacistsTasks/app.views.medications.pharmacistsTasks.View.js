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
Class.define('app.views.medications.pharmacistsTasks.View', 'app.views.common.AppView', {
  cls: "v-pharmacist-tasks-view",

  activeContextData: null,

  /** rest methods */
  statics: {
    ACTION_SAVE_CONTEXT: "SAVE_CONTEXT",
    ACTION_PRINT_PERFUSION_SYRINGE_TASK: 'perfusionSyringeLabelPrint',
    SERVLET_PATH_LOAD_CARE_PROVIDERS: '/getCurrentUserCareProviders',
    ACTION_OPEN_PATIENT: 'openPatient'
  },

  /** privates: components */
  activeCommandConditionTask: null,

  _pharmacySupplyReviewTasklistEnabled: null,
  _pharmacyDispenseTasklistEnabled: null,
  _pharmacyPharmacistReviewTasklistEnabled: null,
  _medicationOnPreparationTasklistEnabled: null,
  _careProviderFilterEnabled: false,

  Constructor: function ()
  {
    this.callSuper();

    var viewInitData = this.getViewInitData();

    this.activeContextData = viewInitData && viewInitData.contextData ?
        JSON.parse(viewInitData.contextData) : this._createInitContextData();
    this._careProviderFilterEnabled = viewInitData && viewInitData.careProviderFilterEnabled === true;
    // user authorities //
    this._pharmacySupplyReviewTasklistEnabled = this.getProperty("pharmacySupplyReviewTasklistEnabled");
    this._pharmacyDispenseTasklistEnabled = this.getProperty("pharmacyDispenseTasklistEnabled");
    this._pharmacyPharmacistReviewTasklistEnabled = this.getProperty("pharmacyPharmacistReviewTasklistEnabled");
    this._medicationOnPreparationTasklistEnabled = this.getProperty("medicationOnPreparationTasklistEnabled");

    this._buildGui();

    var documentResizeDebouncedTask = this.getAppFactory().createDebouncedTask(
        "onDocumentResizeDebouncedTask",
        function(){
          tm.jquery.ComponentUtils.hideAllTooltips();
          tm.jquery.ComponentUtils.hideAllDropDownMenus();
        },
        0,
        500
    );

    var self = this;
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function ()
    {
      $('.careprovider-filter-btn').css('z-index', 1);
      self._thinkGridModule = angular
          .module("tm.angularjs.gui.view.pharmacistsTask", ['tm.angularjs.gui.modules.pharmacistsTask'])
          .config(['tm.angularjs.common.tmcBridge.ViewProxyProvider', function(viewProxyProvider)
          {
            viewProxyProvider.setAppView(self);
          }])
          .value("SaveGridTypeToContext", self._getSaveGridContextClosure())
          .value("SaveCareProviderIdsToContext", self._getSaveCareProviderIdsContextClosure())
          .value("SavePerfusionSyringeFilterStateToContext", self._getSavePerfusionSyringeFilterContextClosure())
          .value("OpenPatientClickHandler", self._getOpenPatientClickHandler())
          .value("PrintPerfusionSyringeTaskViewAction", self._getPrintPerfusionSyringeTaskClosure())
          .value("ActiveInitData", {
            isTablet: tm.jquery.ClientUserAgent.isTablet(),
            language: self.getViewLanguage(),
            careProviderIds: tm.jquery.Utils.isArray(self.activeContextData.careProviderIds) ?
                self.activeContextData.careProviderIds :
                [],
            selectedGrid : self.activeContextData.selectedGrid,
            openClosedFilter: 'open',
            perfusionSyringeFilter: self.activeContextData.perfusionSyringeFilter,
            careProviderFilterEnabled: self.isCareProviderFilterEnabled(),
            permissions: {
              supplyList: self.isPharmacySupplyReviewTasklistEnabled(),
              dispenseList: self.isPharmacyDispenseTasklistEnabled(),
              reviewList: self.isPharmacyPharmacistReviewTasklistEnabled(),
              preparationList: self.isMedicationOnPreparationTasklistEnabled()
            }
          })
          .value("ActiveUpdateData", {
            patientIds: self.isCareProviderFilterEnabled() ? null : []
          })
          .run(['$rootScope', '$http', function ($rootScope, $http)
          {
            $http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";
            document.addEventListener("click", function(e){
              $rootScope.$broadcast("documentClicked", e);
            });
            document.addEventListener("keyup", function(e){
              if (e.keyCode == 27) {
                $rootScope.$broadcast("documentClicked", e, true);
              }
            });
          }]);

      angular.bootstrap($(".angular-tasks-container"), ['tm.angularjs.gui.view.pharmacistsTask']);

      self._setWindowHandler();
    });

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_WINDOW_RESIZE, function(){
      documentResizeDebouncedTask.run();
    });
    if(this.isDevelopmentMode() === true)
    {
      setTimeout(function () {
        self.onViewCommand({update: {
          // patientIds: [ '399302674', '399302689' ]
        }});
      }, 500);
    }
  },

  /**
   * @Override
   * @param command
   */
  onViewCommand: function (command)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    if(tm.jquery.Utils.isEmpty(this.activeCommandConditionTask) == false)
    {
      this.activeCommandConditionTask.abort();
    }

    this.activeCommandConditionTask = appFactory.createConditionTask(
        function ()
        {
          self._onViewCommandImpl(command);
        },
        function ()
        {
          return self.isRendered(true) && (angular.element(document.getElementById('supplyController')).scope() !== undefined);
        },
        50, 100
    );
  },

  _onViewCommandImpl: function (command)
  {
    tm.jquery.ComponentUtils.hideAllDropDownMenus();
    tm.jquery.ComponentUtils.hideAllTooltips();
    tm.jquery.ComponentUtils.hideAllDialogs();

    this.getLocalLogger().info("Recieved update command: ", command);

    if (command.hasOwnProperty('update'))
    {
      this.updateData(command.update);
    }
    else if (command.hasOwnProperty('refresh'))
    {
      this.refreshData();
    }
    else if (command.hasOwnProperty("clear"))
    {
      this.clearData();
    }
  },

  _buildGui: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var html = '';
    html += '<div data-ng-controller="ThinkGridCtrl" class="supplyController" id="supplyController"> ';
    html += '<div class="headerSection"> ';
    html += '<div class="btn-group group-sub-view">';
    if(this.isPharmacySupplyReviewTasklistEnabled())
    {
      html += '<button type="button" title="' + this.getDictionary('show.supply.tasks') + '" data-ng-click="showSupplyGrid();" ';
      html += 'data-ng-class="{ \'selected\': maskData.selectedGrid === \'SUPPLY\'}" class="btn btn-default icon-supply grid_chooser_btn"></button> ';
    }
    if(this.isPharmacyDispenseTasklistEnabled())
    {
      html += '<button type="button" title="' + this.getDictionary('show.dispense.tasks') + '" data-ng-click="showDispenseGrid();" ';
      html += 'data-ng-class="{ \'selected\': maskData.selectedGrid === \'DISPENSE\'}" class="btn btn-default icon-dispense grid_chooser_btn"></button> ';
    }
    if(this.isPharmacyPharmacistReviewTasklistEnabled())
    {
      html += '<button type="button" title="' + this.getDictionary('show.review.tasks') + '" data-ng-click="showReviewGrid();" ';
      html += 'data-ng-class="{ \'selected\': maskData.selectedGrid === \'REVIEW\'}" class="btn btn-default icon-pharmacist grid_chooser_btn"></button> ';
    }
    if(this.isMedicationOnPreparationTasklistEnabled())
    {
      html += '<button type="button" title="' + this.getDictionary('show.syringe.catheter') + '" data-ng-click="showPerfusionSyringesGrid();" ';
      html += 'data-ng-class="{ \'selected\': maskData.selectedGrid === \'PERFUSIONSYRINGES\'}" class="btn btn-default icon-perfusion-syringes grid_chooser_btn"></button> ';
    }
    html += '</div>';
    html += '<tm-careprovider-selector ng-if="isCareProviderFilterEnabled()" data-confirm-callback="confirm" ' +
              'data-cancel-callback="cancel" data-selected="getContextCareproviders" ' +
              'data-data-provider="careproviderSelectorDataProvider" style="float: right; direction: rtl;">' +
        '</tm-careprovider-selector>';
    html += '<open-close-filter data-ng-show="showForSupplyAndDispence()" data-click-callback="applyOpenCloseFilter" ' +
        'data-selected="getContextFilters" style="float: right; direction: rtl;"></open-close-filter>';
    html += '<syringes-filter-menu data-ng-if="maskData.selectedGrid === \'PERFUSIONSYRINGES\'" style="float: right;"></syringes-filter-menu>';
    html += '</div>';
    html += '<div> ';
    html += '<div style="height:100%" data-ng-show="maskData.selectedGrid === \'SUPPLY\'"> ';
    html += '<dtable options="maskData.supply.options" data-rows="maskData.supply.data" class="material"></dtable> ';
    html += '</div> ';
    html += '<div style="height:100%" data-ng-show="maskData.selectedGrid === \'DISPENSE\'"> ';
    html += '<dtable options="maskData.dispense.options" data-rows="maskData.dispense.data" class="material"></dtable> ';
    html += '</div> ';
    html += '<div style="height:100%" data-ng-show="maskData.selectedGrid === \'REVIEW\'"> ';
    html += '<dtable options="maskData.review.options" data-rows="maskData.review.data" class="material"></dtable> ';
    html += '</div> ';
    html += '<div style="height:100%" data-ng-if="maskData.selectedGrid === \'PERFUSIONSYRINGES\'"> ';
    html += '<syringes-list></syringes-list>';
    html += '</div> ';
    html += '</div> ';
    html += '</div>';

    var angContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%"),
      cls: 'angular-tasks-container',
      html: html
    });

    this.add(angContainer);
  },
  _setWindowHandler : function ()
  {

    var resizeTaskListScroller = function () {
      $('.dt-body .dt-body-scroller').height($('.v-pharmacist-tasks-view').height() - 40 - 50 - 6);
      $('.perfusion-syringes-list').height($('.v-pharmacist-tasks-view').height() - 45);
    };
    this._attachWindowHandler(resizeTaskListScroller);
    setTimeout(function () {
      resizeTaskListScroller();
    }, 500);
  },

  _attachWindowHandler : function (resizeTaskListScroller) {

    window.onresize = function ()
    {
      if (window.resizeEditorHandler)
      {
        clearTimeout(window.resizeEditorHandler);
      }

      window.resizeEditorHandler = setTimeout(function ()
      {
        resizeTaskListScroller();
      }, 500);
    }
  },

  /* PRIVATE CONTEXT METHODS */
  _createInitContextData: function()
  {
    return this._createContextData([], 'SUPPLY', {
      showInProgress: true, showCompleted: false, showDispensed: false, closedTasksDate: new Date() });
  },

  _createContextData: function(careProviderIds, selectedGrid, perfusionSyringeFilterState)
  {
    return {
      careProviderIds: careProviderIds,
      selectedGrid: selectedGrid,
      perfusionSyringeFilter: perfusionSyringeFilterState
    };
  },

  /**
   * @param {Object} commandData
   */
  updateData: function (commandData)
  {
    this.getLocalLogger().info("Updating view data.");
    angular.element(document.getElementById('supplyController')).scope().$emit('updateData', commandData);
  },

  refreshData: function ()
  {
    this.getLocalLogger().info("Refreshing view data.");
    angular.element(document.getElementById('supplyController')).scope().$emit('refreshData');
  },

  clearData: function ()
  {
    this.getLocalLogger().info("Clearing view data.");
    angular.element(document.getElementById('supplyController')).scope().$emit('clearData');

  },

  getTranslationClosure : function ()
  {
    var self = this;
    return function (key)
    {
      return self.getDictionary(key);
    };
  },

  _getSaveGridContextClosure : function ()
  {
    var self = this;
    return function (selectedGrid)
    {
      self.activeContextData.selectedGrid = selectedGrid;
      self.saveContextData();
    };
  },

  _getSaveCareProviderIdsContextClosure : function ()
  {
    var self = this;
    return function (careProviderIds)
    {
      self.activeContextData.careProviderIds = careProviderIds;
      self.saveContextData();
    };
  },

  _getSavePerfusionSyringeFilterContextClosure: function()
  {
    var self = this;
    return function (filterState)
    {
      self.activeContextData.perfusionSyringeFilter = filterState;
      self.saveContextData();
    };
  },

  _getOpenPatientClickHandler : function () {
    var self = this;
    return function (patientId, therapyViewType)
    {
      self.sendAction(app.views.medications.pharmacistsTasks.View.ACTION_OPEN_PATIENT,
          {patientId: patientId, therapyViewType: therapyViewType});
    };
  },

  _getPrintPerfusionSyringeTaskClosure: function()
  {
    var self = this;
    return function (taskLabels)
    {
      self.sendAction(app.views.medications.pharmacistsTasks.View.ACTION_PRINT_PERFUSION_SYRINGE_TASK,
          { perfusionSyringeTherapyLabelsPrintDto: JSON.stringify(taskLabels) });
    };
  },

  saveContextData: function()
  {
    this.sendAction(app.views.medications.pharmacistsTasks.View.ACTION_SAVE_CONTEXT,
    {
      contextData: JSON.stringify(this.activeContextData)
    });
  },

  isPharmacySupplyReviewTasklistEnabled: function()
  {
    return this._pharmacySupplyReviewTasklistEnabled === true;
  },
  isPharmacyDispenseTasklistEnabled: function()
  {
    return this._pharmacyDispenseTasklistEnabled === true;
  },
  isPharmacyPharmacistReviewTasklistEnabled: function()
  {
    return this._pharmacyPharmacistReviewTasklistEnabled === true;
  },
  isMedicationOnPreparationTasklistEnabled: function()
  {
    return this._medicationOnPreparationTasklistEnabled === true;
  },
  isCareProviderFilterEnabled: function()
  {
    return this._careProviderFilterEnabled === true;
  }
});
