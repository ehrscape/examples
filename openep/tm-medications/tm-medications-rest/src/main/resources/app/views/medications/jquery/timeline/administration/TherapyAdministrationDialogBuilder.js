/*
 * Copyright (c) 2010-2017 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.timeline.administration.TherapyAdministrationDialogBuilder', 'tm.jquery.Object', {
  view: null,

  _therapyMedicationDataLoader: null,

  /**
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._therapyMedicationDataLoader = new app.views.medications.common.TherapyMedicationDataLoader({
      view: this.getView()
    })
  },

  /**
   * Returns a promise to preload the medication data and show the {@link TherapyAdministrationContainer} dialog.
   * The promise is resolved when the dialog's resultCallback is executed.
   *
   * @param {Object} timelineRowData
   * @param {app.views.medications.common.dto.Therapy|OxygenTherapy} therapy
   * @param {Array<Object>|null} administrations
   * @param {Object|null} administration
   * @param {Boolean} createNewTask
   * @param {String} containerTitle
   * @param {app.views.medications.TherapyEnums.therapyDoseTypeEnum} therapyDoseTypeEnum
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|therapyDoseTypeEnum} administrationType
   * @param {Boolean} editMode
   * @param {Boolean} stopFlow
   * @param {Number} scannedMedicationId
   * @param {String} barcode
   * @returns {tm.jquery.Promise}
   */
  showAdministrationDialog: function(timelineRowData, therapy, administrations, administration, createNewTask,
                                     containerTitle, therapyDoseTypeEnum, administrationType, editMode, stopFlow,
                                     scannedMedicationId, barcode)
  {
    var self = this;
    var dialogResultDeferred = new tm.jquery.Deferred();

    this._therapyMedicationDataLoader.load(therapy).then(
        function onDataLoad(medicationData)
        {
          self._buildAdministrationDialog(timelineRowData, therapy, medicationData, administrations, administration,
              createNewTask, containerTitle, therapyDoseTypeEnum, administrationType, editMode, stopFlow,
              dialogResultDeferred, scannedMedicationId, barcode);
        },
        function onFailure()
        {
          dialogResultDeferred.reject();
        });

    return dialogResultDeferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy|OxygenTherapy} therapy
   * @param {Object|null} administration
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|therapyDoseTypeEnum} administrationType
   * @returns {number}
   * @private
   */
  _calculateDialogHeight: function(therapy, administration, administrationType)
  {
    var enums = app.views.medications.TherapyEnums;
    var height = 555;

    if (therapy.isOrderTypeComplex())
    {
      height += 72;
    }
    if (therapy.isOrderTypeOxygen() && administrationType === enums.administrationTypeEnum.START)
    {
      height += 38;
    }
    if (!tm.jquery.Utils.isEmpty(therapy.getIngredientsList()) && therapy.getIngredientsList().length > 1)
    {
      height += 38;
    }
    if (tm.jquery.Utils.isEmpty(administration) ||
        administrationType === enums.administrationTypeEnum.ADJUST_INFUSION ||
        administrationType === enums.administrationTypeEnum.INFUSION_SET_CHANGE)
    {
      height -= 100;
    }

    if (this.getView().getMedicationsWitnessRequired())
    {
      height += 48;
    }

    if (administration && administration.doctorsComment)
    {
      height += 23;
    }

    return height;
  },

  /**
   * @param {Object} timelineRowData
   * @param {app.views.medications.common.dto.Therapy|OxygenTherapy} therapy
   * @param {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData} medicationData
   * @param {Array<Object>|null} administrations
   * @param {Object|null} administration
   * @param {Boolean} createNewTask
   * @param {String} containerTitle
   * @param {app.views.medications.TherapyEnums.therapyDoseTypeEnum} therapyDoseTypeEnum
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|therapyDoseTypeEnum} administrationType
   * @param {Boolean} editMode
   * @param {Boolean} stopFlow
   * @param {Number} scannedMedicationId
   * @param {String} barcode
   * @param {tm.jquery.Deferred} dialogResultDeferred
   * @private
   */
  _buildAdministrationDialog: function(timelineRowData, therapy, medicationData, administrations, administration,
                                       createNewTask, containerTitle, therapyDoseTypeEnum, administrationType, editMode,
                                       stopFlow, dialogResultDeferred, scannedMedicationId, barcode)

  {
    var self = this;
    var appFactory = this.getView().getAppFactory();
    var latestTherapyVersion = timelineRowData && timelineRowData.therapy !== therapy ? timelineRowData.therapy : null;

    // Properties from timeline row - only for additional actions for continuous infusion.
    var infusionActive = true;
    if ((therapy.isContinuousInfusion() || therapy.isOrderTypeOxygen()) && timelineRowData)
    {
      var lastPositiveInfusionRate = timelineRowData.lastPositiveInfusionRate;
      infusionActive = timelineRowData.infusionActive;
    }

    var therapyReviewedUntil = null;
    if (timelineRowData && timelineRowData.reviewedUntil)
    {
      therapyReviewedUntil = new Date(timelineRowData.reviewedUntil);
    }

    var config = {
      view: this.getView(),
      cls: 'therapy-administration-container',
      startProcessOnEnter: false,
      scrollable: 'vertical',
      therapy: therapy,
      administrations: administrations,
      administration: administration,
      patientId: this.patientId,
      createNewTask: createNewTask,
      therapyDoseTypeEnum: therapyDoseTypeEnum,
      administrationType: administrationType,
      editMode: editMode,
      therapyReviewedUntil: therapyReviewedUntil,
      latestTherapyVersion: latestTherapyVersion,
      stopFlow: stopFlow === true,
      lastPositiveInfusionRate: !tm.jquery.Utils.isEmpty(lastPositiveInfusionRate) ? lastPositiveInfusionRate : null,
      infusionActive: infusionActive,
      medicationData: medicationData,
      barcode: barcode
    };

    this._attachMedicationProductsOnConfig(therapy, administration, createNewTask, config, scannedMedicationId).then(
        function()
        {
          var therapyAdministrationContainer = new tm.views.medications.timeline.TherapyAdministrationContainer(config);
          self.getView().setActionCallbackListener(therapyAdministrationContainer);

          var therapyAdministrationDialog = appFactory.createDataEntryDialog(
              containerTitle,
              null,
              therapyAdministrationContainer,
              function(resultData)
              {
                dialogResultDeferred.resolve(resultData);
              },
              475,
              self._calculateDialogHeight(therapy, administration, administrationType)
          );
          therapyAdministrationDialog.addTestAttribute('therapy-administration-dialog');
          therapyAdministrationDialog.setContainmentElement(self.getView().getDom());
          therapyAdministrationDialog.header.setCls("therapy-admin-header");
          therapyAdministrationDialog.getFooter().setCls("therapy-admin-footer");
          therapyAdministrationDialog.getFooter().rightContainer.layout.gap = 0;

          therapyAdministrationDialog.setLeftButtons([therapyAdministrationContainer.getResetButton()]);
          therapyAdministrationContainer.setDialog(therapyAdministrationDialog);
          therapyAdministrationDialog.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
          {
            self.getView().hideLoaderMask();
          });
          therapyAdministrationDialog.show();
        }
    );
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Object} config
   * @param {Boolean} createNewTask
   * @param {Number} scannedMedicationId
   * @returns {tm.jquery.Promise}
   * @private
   */
  _attachMedicationProductsOnConfig: function(therapy, administration, createNewTask, config, scannedMedicationId)
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    if (!createNewTask && therapy.isOrderTypeSimple() && therapy.getMedication().getId())
    {
      this._loadTherapyMedicationProducts(therapy).then(
          function(medicationProducts)
          {
            config.medicationProducts = medicationProducts;
            self._findPreselectedMedicationProduct(medicationProducts, administration, therapy, scannedMedicationId).then(
                function(productMedicationData)
                {
                  config.preselectedProductMedicationData = productMedicationData;
                  deferred.resolve();
                })
          });
    }
    else
    {
      deferred.resolve();
    }
    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @returns {tm.jquery.Promise}
   * @private
   */
  _loadTherapyMedicationProducts: function(therapy)
  {
    var medicationProductsDeferred = tm.jquery.Deferred.create();
    this.getView().getRestApi().loadMedicationProducts(therapy.getMedication().getId(), therapy.getRoutes(), false).then(
        function(data)
        {
          medicationProductsDeferred.resolve(data);
        },
        function()
        {
          medicationProductsDeferred.reject();
        });
    return medicationProductsDeferred.promise();
  },

  /**
   * @param {Array<app.views.medications.common.dto.Medication>} medicationProducts
   * @param {Object} administration
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Number} scannedMedicationId
   * @returns {tm.jquery.Promise}
   * @private
   */
  _findPreselectedMedicationProduct: function(medicationProducts, administration, therapy, scannedMedicationId)
  {
    var deferredLoad = tm.jquery.Deferred.create();
    var medicationIdToCompare = administration && !tm.jquery.Utils.isEmpty(administration.substituteMedication) ?
        administration.substituteMedication.id :
        therapy.getMedicationId();

    var matchingProductId = null;
    if (scannedMedicationId)
    {
      matchingProductId = scannedMedicationId;
    }
    else
    {
      var matchingProduct = tm.views.medications.MedicationUtils.findInArray(medicationProducts, isMatchById);

      matchingProductId = matchingProduct ? matchingProduct.id :
          (medicationProducts.length >= 1 ? medicationProducts[0].id : null);
    }

    if (matchingProductId)
    {
      this.getView().getRestApi().loadMedicationData(matchingProductId).then(
          function(data)
          {
            deferredLoad.resolve(data)
          },
          function()
          {
            deferredLoad.reject();
          });
    }
    else
    {
      deferredLoad.resolve(null);
    }

    return deferredLoad.promise();

    function isMatchById(product)
    {
      return product.id === medicationIdToCompare;
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});