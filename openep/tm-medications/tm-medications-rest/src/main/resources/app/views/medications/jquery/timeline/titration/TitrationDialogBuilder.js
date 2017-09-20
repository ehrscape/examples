/*
 * Copyright (c) 2010-2015 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.timeline.titration.TitrationDialogBuilder', 'tm.jquery.Object', {
  view: null,

  /**
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy Titrated therapy.
   * @param {Object} administration The administration for this dialog.
   * @param {Array<Object>} administrations All administrations for this therapy.
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum} administrationType
   * @param {number | null} lastPositiveInfusionRate
   * @param {Boolean|null} activeContinuousInfusion
   * @param {Boolean} [scheduleAdditional=false]
   * @param {Boolean} [applyUnplanned=false]
   * @param {Boolean} [stopFlow=false]
   * @returns {tm.jquery.Promise} Promise, which will be resolved when the dialog closes.
   */
  showAdministrationDialog: function(therapy, administration, administrations, administrationType, lastPositiveInfusionRate,
                                     activeContinuousInfusion, scheduleAdditional, applyUnplanned, stopFlow)
  {
    return this._showTitrationAdministrationDialog(
        therapy,
        therapy.getTitration(),
        administration,
        true,
        administrations,
        administrationType,
        lastPositiveInfusionRate,
        activeContinuousInfusion,
        scheduleAdditional,
        applyUnplanned,
        stopFlow);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy}  therapy
   * @param {app.views.medications.TherapyEnums.therapyTitrationTypeEnum} titrationType Based on main MedicationData or therapy
   * @param {Object} administration
   * @returns {tm.jquery.Promise} Promise, which will be resolved when the dialog closes.
   */
  showDataViewingDialog: function(therapy, titrationType, administration)
  {
    return this._showTitrationAdministrationDialog(
        therapy,
        titrationType,
        administration,
        false);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy Display therapy.
   * @param {app.views.medications.TherapyEnums.therapyTitrationTypeEnum} titrationType Based on therapy or medication data.
   * @param {Object} administration
   * @param {Boolean} enableDosing
   * @param {Array<Object>} [administrations=null]
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum} [administrationType=null]
   * @param {number | null} [lastPositiveInfusionRate=null]
   * @param {Boolean} [activeContinuousInfusion=null]
   * @param {Boolean} [scheduleAdditional=false]
   * @param {Boolean} [applyUnplanned=false]
   * @param {Boolean} [stopFlow=false]
   * @returns {tm.jquery.Promise} Promise, which will be resolved when the dialog closes.
   * @private
   */
  _showTitrationAdministrationDialog: function(therapy, titrationType, administration, enableDosing, administrations,
                                               administrationType, lastPositiveInfusionRate, activeContinuousInfusion,
                                               scheduleAdditional, applyUnplanned, stopFlow)
  {
    var view = this.getView();
    var dialogResultDeferred = new tm.jquery.Deferred();
    var self = this;

    var titrationDataLoader = new app.views.medications.timeline.titration.TitrationDataLoader({
      view: view,
      therapyId: therapy.getTherapyId(),
      titrationType: titrationType
    });

    var initialEndInterval = CurrentTime.get();
    initialEndInterval.setHours(initialEndInterval.getHours() + 4); // adding 4 hours to make sure the data is clearly visible

    titrationDataLoader.init(initialEndInterval).then(function()
    {
      self._buildTitrationAdministrationDialog(
          therapy,
          administration,
          enableDosing,
          titrationDataLoader,
          dialogResultDeferred,
          administrations,
          administrationType,
          lastPositiveInfusionRate,
          activeContinuousInfusion,
          scheduleAdditional,
          applyUnplanned,
          stopFlow).show();
    });

    return dialogResultDeferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy Display therapy.
   * @param {Object} administration
   * @param {Boolean} enableDosing
   * @param {app.views.medications.timeline.titration.TitrationDataLoader} titrationDataLoader
   * @param {tm.jquery.Deferred} dialogResultDeferred
   * @param {Array<Object>} administrations
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum} administrationType
   * @param {number | null} lastPositiveInfusionRate
   * @param {Boolean} activeContinuousInfusion
   * @param {Boolean} [scheduleAdditional=false]
   * @param {Boolean} [applyUnplanned=false]
   * @param {Boolean} [stopFlow=false]
   * @returns {app.views.common.dialog.AppDialog}
   * @private
   */
  _buildTitrationAdministrationDialog: function(therapy, administration, enableDosing, titrationDataLoader, 
                                                dialogResultDeferred, administrations, administrationType,
                                                lastPositiveInfusionRate, activeContinuousInfusion,
                                                scheduleAdditional, applyUnplanned, stopFlow)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var dialogContentContainer = new app.views.medications.timeline.titration.TitrationBasedAdministrationDataEntryContainer({
      view: view,
      therapy: therapy,
      administration: administration,
      administrationType: administrationType,
      allAdministrations: administrations,
      titrationData: titrationDataLoader.getCurrentData(),
      dataLoader: titrationDataLoader,
      lastPositiveInfusionRate: lastPositiveInfusionRate,
      activeContinuousInfusion: activeContinuousInfusion,
      enableDosing: enableDosing,
      scheduleAdditional: scheduleAdditional,
      applyUnplanned: applyUnplanned,
      stopFlow: stopFlow
    });

    var titrationAdministrationDialog = appFactory.createDataEntryDialog(
        view.getDictionary("dose.titration"),
        null,
        dialogContentContainer,
        function(resultData)
        {
            dialogResultDeferred.resolve(resultData);
        },
        dialogContentContainer.getDefaultWidth(),
        dialogContentContainer.getDefaultHeight()
    );
    titrationAdministrationDialog.setContainmentElement(view.getDom());
    titrationAdministrationDialog.setCls("invisible");

    return titrationAdministrationDialog;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});