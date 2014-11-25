/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
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

Class.define('app.views.medications.ordering.MedicationsOrderingContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "medications-ordering-container",
  padding: 0,

  /** configs */
  view: null,
  patientId: null,
  presetMedicationId: null, //optional
  presetTherapies: null,    //optional
  presetDate: null,    //optional
  warningsEnabled: true,
  isPastMode: false,
  assertBaselineInfusion: true,

  /** privates */
  baselineInfusionIntervals: null,
  oldTherapiesForWarningsSearch: null,
  resultCallback: null,
  linkIndex: null,
  /** privates: components */
  orderingContainer: null,
  basketContainer: null,
  warningsContainer: null,
  performerContainer: null,
  saveDateTimePane: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    var appFactory = this.view.getAppFactory();
    this.setLayout(appFactory.createDefaultVFlexboxLayout("start", "stretch"));
    this.baselineInfusionIntervals = [];
    this.oldTherapiesForWarningsSearch = [];
    this.linkIndex = 0;
    this._loadPatientBaselineInfusionIntervals();
    if (this.warningsEnabled)
    {
      this._loadTherapiesForWarningsSearch();
    }

    this._buildComponents();
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      if (self.presetMedicationId)
      {
        setTimeout(function()
        {
          self.orderingContainer.presetMedication(self.presetMedicationId);
        }, 500);
      }
      if (self.presetTherapies)
      {
        self._fixTherapiesTimingAndAddToBasket(self.presetTherapies);
      }
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.orderingContainer = new app.views.medications.ordering.OrderingContainer({
      view: this.view,
      width: 720,
      mainContainer: this,
      isPastMode: this.isPastMode,
      presetDate: this.presetDate,
      addTherapiesToBasketFunction: function(therapies)
      {
        self._fixTherapiesTimingAndAddToBasket(therapies);
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePane.setHeight(34);
        self.saveDateTimePane.setPadding('4 0 0 0');
        self.saveDateTimePane.show();
        self.saveDateTimePane.repaint();
      },
      confirmTherapyEvent: function(data)
      {
        if (data != 'VALIDATION_FAILED')
        {
          return self._addToBasket(data);
        }
        return false;
      },
      removeFromBasketFunction: function(therapy)
      {
        self.basketContainer.removeTherapy(therapy);
      },
      saveTherapyToTemplateFunction: function(therapy, invalidTherapy)
      {
        self._openSaveTemplateDialog([therapy], true, invalidTherapy);
      },
      linkTherapyFunction: function(callback)
      {
        self._openLinkTherapyDialog(callback);
      }
    });
    this.basketContainer = new app.views.medications.ordering.BasketContainer({
      view: this.view,
      flex: 1,
      therapyAddedEvent: function()
      {
        if (self.warningsEnabled)
        {
          self.warningsContainer.refreshWarnings();
        }
        self.orderingContainer.clear();
      },
      therapiesRemovedEvent: function(therapies)
      {
        if (self.warningsEnabled)
        {
          self.warningsContainer.refreshWarnings();
        }
        for (var i = 0; i < therapies.length; i++)
        {
          var therapy = therapies[i].therapy;
          if (therapy && therapy.baselineInfusion)
          {
            self._removeBaselineInfusion(therapy);
          }
        }
      },
      editTherapyFunction: function(therapy)
      {
        self.orderingContainer.editTherapy(therapy);
        if (therapy && therapy.baselineInfusion)
        {
          self._removeBaselineInfusion(therapy);
        }
      },
      saveTemplateFunction: function(therapies)
      {
        self._openSaveTemplateDialog(therapies, false);
      }
    });
    this.warningsContainer = new app.views.medications.ordering.WarningsContainer({
      view: this.view,
      height: this.warningsEnabled ? 300 : 30,
      getPatientMedsForWarningsFunction: function()
      {
        return self._getTherapiesForWarningsSearch();
      }});

    var careProfessionals = this.view.getCareProfessionals();
    var currentUserAsCareProfessionalName = this.view.getCurrentUserAsCareProfessional() ? this.view.getCurrentUserAsCareProfessional().name : null;
    this.performerContainer =
        tm.views.medications.MedicationUtils.createPerformerContainer(this.view, careProfessionals, currentUserAsCareProfessionalName);

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane();
    this.saveDateTimePane.hide();
  },

  _buildGui: function()
  {
    var appFactory = this.view.getAppFactory();
    var mainContainer = new tm.jquery.Container({layout: appFactory.createDefaultHFlexboxLayout("start", "stretch"), flex: 1});
    mainContainer.add(this.orderingContainer);
    var eastContainer = new tm.jquery.Container({layout: appFactory.createDefaultVFlexboxLayout("start", "stretch"), flex: 1});
    eastContainer.add(this.basketContainer);
    eastContainer.add(this.warningsContainer);
    mainContainer.add(eastContainer);
    this.add(mainContainer);
    this.add(this.performerContainer);
    this.add(this.saveDateTimePane);
  },

  _addToBasket: function(therapy)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;

    var params = {therapy: JSON.stringify(therapy)};
    var fillDisplayValuesUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FILL_DISPLAY_VALUES;

    var baselineInfusionAlreadyExists =
        therapy.baselineInfusion && this._baselineInfusionsIntersects(therapy.start, therapy.end);
    if (baselineInfusionAlreadyExists && this.assertBaselineInfusion)
    {
      appFactory.createWarningSystemDialog(self.view.getDictionary("patient.already.has.baseline.infusion"), 320, 122).show();
      return false;
    }
    else if (therapy.dosingFrequency && therapy.dosingFrequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES && therapy.dosingDaysFrequency)
    {
      this.view.loadViewData(fillDisplayValuesUrl, params, null, function(therapyWithDisplayValues)
      {
        var message = self.view.getDictionary('can.not.prescribe.therapy.with.dosing.frequency') + ": " +
            therapyWithDisplayValues.frequencyDisplay.toLowerCase() + ', ' +
            therapyWithDisplayValues.daysFrequencyDisplay + '".';
        appFactory.createWarningSystemDialog(message, 520, 122).show();
      });
      return false;
    }
    else
    {
      this.view.loadViewData(fillDisplayValuesUrl, params, null, function(therapyWithDisplayValues)
      {
        //fix date after serialization
        therapyWithDisplayValues.start = new Date(therapyWithDisplayValues.start.data);
        therapyWithDisplayValues.end = therapyWithDisplayValues.end ? new Date(therapyWithDisplayValues.end.data) : null;

        self.basketContainer.addTherapy(therapyWithDisplayValues);
      });
      if (therapy.baselineInfusion)
      {
        this.baselineInfusionIntervals.push({
          startMillis: therapy.start.getTime(),
          endMillis: therapy.end ? therapy.end.getTime() : null
        })
      }
      return true;
    }
  },

  _openSaveTemplateDialog: function(therapies, addSingleTherapy, invalidTherapy)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        this.view.getDictionary('therapy.template'),
        null,
        new app.views.medications.ordering.SaveTemplatePane({
          view: self.view,
          startProcessOnEnter: true,
          padding: 8,
          addSingleTherapy: addSingleTherapy,
          invalidTherapy: invalidTherapy,
          therapies: therapies,
          templates: self.orderingContainer.getTemplates()}),
        function(resultData)
        {
          if (resultData)
          {
            self.orderingContainer.reloadTemplates();
          }
        },
        300,
        300
    );

    dialog.show();
  },

  _openLinkTherapyDialog: function(callback)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        this.view.getDictionary('link'),
        null,
        new app.views.medications.ordering.LinkTherapyPane({
          view: self.view,
          linkIndex: self.linkIndex,
          orderedTherapies: self.basketContainer.getTherapies()
        }),
        //new tm.jquery.Container({layout: appFactory.createDefaultHFlexboxLayout("start", "stretch"), flex: 1, html: "demo"}),
        function(resultData)
        {
          if (resultData)
          {
            callback(resultData.selectedTherapy, self.linkIndex);
            self.linkIndex++;
          }
        },
        550,
        480
    );

    dialog.show();
  },

  _fixTherapiesTimingAndAddToBasket: function(therapies)
  {
    for (var i = 0; i < therapies.length; i++)
    {
      var therapy = therapies[i];
      var dosingFrequencyKey = tm.views.medications.MedicationTimingUtils.getFrequencyKey(therapy.dosingFrequency);
      var dosingFrequencyModeKey = therapy.dosingFrequency ? therapy.dosingFrequency.type : "WITHOUT_FREQUENCY";
      var nextAdministrationTimestamp;
      if (therapy.variable)
      {
        nextAdministrationTimestamp =
            tm.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForVario(therapy.timedDoseElements)
      }
      else
      {
        nextAdministrationTimestamp = tm.views.medications.MedicationTimingUtils.getNextAdministrationTimestamp(
            dosingFrequencyKey,
            dosingFrequencyModeKey,
            null,
            this.view.getAdministrationTiming());
      }
      therapy.start = nextAdministrationTimestamp ? nextAdministrationTimestamp : new Date();
      therapy.end = null;
      if (this.presetDate)
      {
        therapy.start = new Date(
            this.presetDate.getFullYear(),
            this.presetDate.getMonth(),
            this.presetDate.getDate(),
            therapy.start.getHours(),
            therapy.start.getMinutes(),
            0,
            0);
      }

      var baselineInfusionAlreadyExists =
          therapy.baselineInfusion && this._baselineInfusionsIntersects(therapy.start, therapy.end);
      if (baselineInfusionAlreadyExists && this.assertBaselineInfusion)
      {
        this.view.getAppFactory().createWarningSystemDialog(
            this.view.getDictionary("patient.already.has.baseline.infusion"), 320, 122).show();
        return false;
      }

      if (therapy.baselineInfusion)
      {
        this.baselineInfusionIntervals.push({
          startMillis: therapy.start.getTime(),
          endMillis: therapy.end ? therapy.end.getTime() : null
        })
      }
      this._fixTherapyTimingAndAddToBasket(therapy, therapy.completed);
    }
  },

  _fixTherapyTimingAndAddToBasket: function(therapy, completed)
  {
    var self = this;
    var params = {therapy: JSON.stringify(therapy)};
    var fillDisplayValuesUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FILL_DISPLAY_VALUES;
    this.view.loadViewData(fillDisplayValuesUrl, params, null, function(therapyWithDisplayValues)
    {
      //fix date after serialization
      therapyWithDisplayValues.start = new Date(therapyWithDisplayValues.start.data);
      therapyWithDisplayValues.end = therapyWithDisplayValues.end ? new Date(therapyWithDisplayValues.end.data) : null;
      therapyWithDisplayValues.completed = completed;
      self.basketContainer.addTherapy(therapyWithDisplayValues);
    });
  },

  _baselineInfusionsIntersects: function(start, end)
  {
    for (var i = 0; i < this.baselineInfusionIntervals.length; i++)
    {
      if (start.getTime() < this.baselineInfusionIntervals[i].startMillis)
      {
        if (!end || end.getTime() > this.baselineInfusionIntervals[i].startMillis)
        {
          return true
        }
      }
      else
      {
        if (!this.baselineInfusionIntervals[i].endMillis || this.baselineInfusionIntervals[i].endMillis > start.getTime())
        {
          return true;
        }
      }
    }
    return false;
  },

  _validateAndSaveTherapies: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var therapies = this.basketContainer.getTherapies();
    var prescriber = this.performerContainer.getPerformer();

    if (therapies.length == 0)
    {
      appFactory.createWarningSystemDialog(self.view.getDictionary("you.have.no.therapies.in.basket"), 360, 122).show();
      this._savingFailed();
    }
    else if (!self.isPastMode && !prescriber)
    {
      appFactory.createWarningSystemDialog(self.view.getDictionary("prescriber.not.defined.warning"), 320, 122).show();
      this._savingFailed();
    }
    else
    {
      var incompleteTherapiesExist = this._doesOrderContainIncompleteTherapies(therapies);
      if (incompleteTherapiesExist)
      {
        appFactory.createWarningSystemDialog(self.view.getDictionary("unfinished.therapies.in.basket.warning"), 360, 122).show();
        this._savingFailed();
      }
      else
      {
        var unfinishedOrderExists = this.orderingContainer.unfinishedOrderExists();

        if (unfinishedOrderExists)
        {
          var confirmDialog = appFactory.createConfirmSystemDialog(this.view.getDictionary('unfinished.therapy.that.will.not.be.saved.warning'),
              function(confirmed)
              {
                if (confirmed == true)
                {
                  self._saveOrder(therapies);
                }
                else
                {
                  self._savingFailed();
                }
              }
          );
          confirmDialog.setWidth(380);
          confirmDialog.setHeight(122);
          confirmDialog.show();
        }
        else
        {
          this._saveOrder(therapies);
        }
      }
    }
  },

  _doesOrderContainIncompleteTherapies: function(therapies)
  {
    for (var i = 0; i < therapies.length; i++)
    {
      if (therapies[i].completed == false)
      {
        return true;
      }
    }
    return false;
  },

  _saveOrder: function(therapies)
  {
    var self = this;

    this._fillTherapyWarningsBeforeSave(therapies);

    var saveDateTime = this.saveDateTimePane.isHidden() ? null : this.saveDateTimePane.getSaveDateTime();
    var saveMedicationsOrderUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_MEDICATIONS_ORDER;
    var centralCaseData = self.view.getCentralCaseData();
    var params = {
      patientId: self.view.getPatientId(),
      therapies: JSON.stringify(therapies),
      roundsInterval: JSON.stringify(self.view.getRoundsInterval()),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: centralCaseData ? centralCaseData.careProviderId : null,
      sessionId: centralCaseData && centralCaseData.sessionId ? centralCaseData.sessionId : null,
      knownOrganizationalEntity: self.view.getKnownOrganizationalEntity(),
      prescriber: JSON.stringify(self.performerContainer.getPerformer()),
      saveDateTime: JSON.stringify(saveDateTime)
    };

    this.view.loadPostViewData(saveMedicationsOrderUrl, params, null,
        function()
        {
          var resultData = new app.views.common.AppResultData({success: true});
          self.resultCallback(resultData);
        },
        function()
        {
          self._savingFailed();
        },
        true);
  },

  _fillTherapyWarningsBeforeSave: function(therapies)
  {
    var enums = app.views.medications.TherapyEnums;
    var warnings = this.warningsContainer.getOverriddenWarnings();

    for (var i = 0; i < warnings.length; i++)
    {
      var overrideReason = warnings[i].overrideReason;
      var warningDto = warnings[i].warning; //MedicationsWarningDto
      var primaryMedicationId = warningDto.primaryMedication ? warningDto.primaryMedication.id : null;
      var secondaryMedicationId = warningDto.secondaryMedication ? warningDto.secondaryMedication.id : null;

      if (primaryMedicationId || secondaryMedicationId)
      {
        for (var j = 0; j < therapies.length; j++)
        {
          var therapy = therapies[j];
          var therapyContainsMedicationWithWarning = false;
          if (therapy.medicationOrderFormType == enums.medicationOrderFormType.SIMPLE)
          {
            if (therapy.medication.id == primaryMedicationId ||
                therapy.medication.id == secondaryMedicationId)
            {
              therapyContainsMedicationWithWarning = true;
            }
          }
          else if (therapy.medicationOrderFormType == enums.medicationOrderFormType.COMPLEX)
          {
            for (var k = 0; k < therapy.ingredientsList.length; k++)
            {
              var infusionIngredient = therapy.ingredientsList[k];
              if (infusionIngredient.medication.id == primaryMedicationId ||
                  infusionIngredient.medication.id == secondaryMedicationId)
              {
                therapyContainsMedicationWithWarning = true;
              }
            }
          }
          if (therapyContainsMedicationWithWarning)
          {
            therapy.criticalWarnings.push("Warning overriden. Reason: " + overrideReason + " Warning: " + warningDto.description);
          }
        }
      }
    }
  },

  _savingFailed: function()
  {
    var resultData = new app.views.common.AppResultData({success: false});
    this.resultCallback(resultData);
  },

  _loadPatientBaselineInfusionIntervals: function()
  {
    var self = this;
    var params = {patientId: this.patientId};
    var baselineTherapiesUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_BASELINE_THERAPIES;
    this.view.loadViewData(baselineTherapiesUrl, params, null, function(data)
    {
      self.baselineInfusionIntervals = data;
    });
  },

  _loadTherapiesForWarningsSearch: function()
  {
    var self = this;
    var params = {patientId: this.patientId};
    var therapiesForWarningsUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPIES_FOR_WARNINGS;
    this.view.loadViewData(therapiesForWarningsUrl, params, null, function(data)
    {
      self.oldTherapiesForWarningsSearch = data;
    });
  },

  _getTherapiesForWarningsSearch: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var therapiesForWarnings = [];

    var therapies = this.basketContainer.getTherapies();
    for (var i = 0; i < therapies.length; i++)
    {
      var therapy = therapies[i];
      if (therapy.medicationOrderFormType == enums.medicationOrderFormType.SIMPLE)
      {
        therapiesForWarnings.push(
            {
              id: therapy.medication.id,
              description: therapy.medication.name,
              prospective: true,
              routeCode: therapy.route ? therapy.route.code : null,
              doseUnit: therapy.quantityUnit,
              doseAmount: therapy.variable ? 0 : therapy.doseElement.quantity
            });
      }
      else if (therapy.medicationOrderFormType == enums.medicationOrderFormType.COMPLEX)
      {
        for (var j = 0; j < therapy.ingredientsList.length; j++)
        {
          var infusionIngredient = therapy.ingredientsList[j];
          therapiesForWarnings.push(
              {
                id: infusionIngredient.medication.id,
                description: infusionIngredient.medication.name,
                prospective: true,
                routeCode: therapy.route ? therapy.route.code : null,
                doseUnit: infusionIngredient.quantityUnit,
                doseAmount: infusionIngredient.quantity
              });
        }
      }
    }
    $.merge(therapiesForWarnings, this.oldTherapiesForWarningsSearch);
    return therapiesForWarnings;
  },

  _removeBaselineInfusion: function(therapy)
  {
    var baselineInfusionIntervalsLength = this.baselineInfusionIntervals.length;
    for (var i = 0; i < baselineInfusionIntervalsLength; i++)
    {
      var baselineInterval = this.baselineInfusionIntervals[i];
      if (baselineInterval.startMillis == therapy.start.getTime() &&
          (therapy.end == null && baselineInterval.endMillis == null) || (baselineInterval.endMillis == therapy.end.getTime()))
      {
        this.baselineInfusionIntervals.splice(i, 1);
        break;
      }
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._validateAndSaveTherapies();
  },

  clear: function()
  {
    this.orderingContainer.clear();
    this.basketContainer.clear();
    this.warningsContainer.clear();
  }
});

