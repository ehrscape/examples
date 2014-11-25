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

Class.define('tm.views.medications.timeline.TherapyAdministrationContainer', 'app.views.common.containers.AppDataEntryContainer', {

  /** configs */
  view: null,
  displayProvider: null,
  therapy: null,
  administrations: null,
  administration: null,
  patientId: null,
  createNewTask: null,
  therapyDoseTypeEnum: null, //optional
  administrationType: null,  //optional
  editMode: null,
  /** privates */
  medicationId: null,
  similarMedications: null,
  /** privates: components */
  therapyDescriptionContainer: null,
  medicationField: null,
  administrationDateTimeCard: null,
  administrationDateField: null,
  administrationTimeField: null,
  doseRateSpacer: null,
  ratePane: null,
  rateLabel: null,
  dosePane: null,
  doseLabel: null,
  volumeLabel: null,
  volumeField: null,
  volumePane: null,
  commentField: null,
  confirmCancelRadioButtonGroup: null,
  confirmRadioButton: null,
  cancelRadioButton: null,
  infusionSetChangeButtonGroup: null,
  infusionSystemChangeButton: null,
  infusionSyringeChangeButton: null,
  validationForm: null,
  maxAdministrationsWarningLabel: null,
  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.medicationId = this._getMedicationId(this.therapy);
    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch", 0));
    this.setMargin(10);
    this._buildComponents();
    this._buildGui();
    this._setAdministrationValues();
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._loadMedicationData(self.medicationId, false);
    });
  },

  _buildComponents: function()
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;

    this.therapyDescriptionContainer = new tm.jquery.Container({
      layout: new tm.jquery.HFlexboxLayout(),
      html: this.therapy.formattedTherapyDisplay,
      cls: 'TherapyDescription'
    });

    this.administrationDateTimeCard = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    this.administrationDateField = new tm.jquery.DatePicker();
    this.administrationTimeField = new tm.jquery.TimePicker();
    this.administrationDateField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.administrationDateField.setDate(new Date());
          self.administrationTimeField.setTime(new Date());
        });
    this.administrationTimeField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.administrationDateField.setDate(new Date());
          self.administrationTimeField.setTime(new Date());
        });

    this.administrationDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._assertNumberOfAllowedAdministrationsReached();
    });

    this.administrationTimeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._assertNumberOfAllowedAdministrationsReached();
    });

    this.maxAdministrationsWarningLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel max-administrations-warning', '');

    this.doseLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dose'));
    this.dosePane = new app.views.medications.ordering.DosePane({
      view: this.view
    });
    this.rateLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('infusion.rate'));
    this.ratePane = new app.views.medications.ordering.InfusionRatePane({
      view: this.view,
      cls: "infusion-rate-pane",
      getInfusionRateTypeBolusFunction: function()
      {
        return self.therapy.rateString && self.therapy.rateString == "BOLUS";
      },
      getInfusionIngredientsFunction: function()
      {
        if (self.therapy.medicationOrderFormType == enums.medicationOrderFormType.COMPLEX)
        {
          return self.therapy.ingredientsList;
        }
        return null;
      },
      getContinuousInfusionFunction: function()
      {
        if (self.therapy.medicationOrderFormType == enums.medicationOrderFormType.COMPLEX)
        {
          return self.therapy.continuousInfusion;
        }
        return false;
      },
      getVolumeSumFunction: function()
      {
        return self.therapy.volumeSum;
      },
      formulaVisibleFunction: function()
      {
        return self.medicationId != null;
      }
    });
    this.volumeLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('volume.total'));
    this.volumeField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, '0');
    this.volumePane = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 5)});
    this.volumePane.add(this.volumeField);
    this.volumePane.add(tm.views.medications.MedicationUtils.crateLabel('TextData', 'ml'));
    this.doseRateSpacer = new tm.jquery.Spacer({type: 'vertical', size: 2});

    this._hideAllDoseRateFields();
    this.commentField = new tm.jquery.TextField({width: 420});
    this.confirmRadioButton = new tm.jquery.RadioButton({labelText: this.view.getDictionary('successful'), labelCls: 'TextLabel', data: true, labelAlign: "right", checked: true});
    this.cancelRadioButton = new tm.jquery.RadioButton({labelText: this.view.getDictionary('unsuccessful'), data: false, labelAlign: "right"});
    if (!this.createNewTask)
    {
      this.confirmCancelRadioButtonGroup = new tm.jquery.RadioButtonGroup();
      this.confirmCancelRadioButtonGroup.add(this.confirmRadioButton);
      this.confirmCancelRadioButtonGroup.add(this.cancelRadioButton);
    }

    if (this.administrationType == enums.administrationTypeEnum.INFUSION_SET_CHANGE)
    {
      this.infusionSystemChangeButton = new tm.jquery.RadioButton({
        labelText: this.view.getDictionary("InfusionSetChangeEnum." + enums.infusionSetChangeEnum.INFUSION_SYSTEM_CHANGE),
        data: enums.infusionSetChangeEnum.INFUSION_SYSTEM_CHANGE,
        labelAlign: "right",
        checked: true,
        padding: "5 0 0 0",
        margin: "0 0 -5 0"
      });
      this.infusionSyringeChangeButton = new tm.jquery.RadioButton({
        labelText: this.view.getDictionary("InfusionSetChangeEnum." + enums.infusionSetChangeEnum.INFUSION_SYRINGE_CHANGE),
        data: enums.infusionSetChangeEnum.INFUSION_SYRINGE_CHANGE,
        labelAlign: "right"
      });
      this.infusionSetChangeButtonGroup = new tm.jquery.RadioButtonGroup();
      this.infusionSetChangeButtonGroup.add(this.infusionSystemChangeButton);
      this.infusionSetChangeButtonGroup.add(this.infusionSyringeChangeButton);
    }

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._confirmTherapyAdministration();
      },
      onValidationError: function()
      {
        self.resultCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: this.view.getDictionary("field.value.is.required")
    });
  },

  _setAdministrationValues: function()
  {
    var administrationTime;
    var self = this;

    var now = new Date();
    if (this.administration)
    {
      if (this.editMode)
      {
        if (this.administration.administrationStatus == "FAILED")
        {
          this.confirmCancelRadioButtonGroup.setActiveRadioButton(this.cancelRadioButton);
        }
        administrationTime = new Date(this.administration.administrationTime);
        this.commentField.setValue(this.administration.comment);
      }
      else
      {
        var plannedTime = new Date(this.administration.plannedTime);
        administrationTime = plannedTime < now ? plannedTime : now;
      }
    }
    else
    {
      administrationTime = now;
    }

    this.administrationDateField.setDate(administrationTime);
    this.administrationTimeField.setTime(administrationTime);

    this.administrationDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      setTimeout(function(){
        component.getInputElement().focus();
        self._assertNumberOfAllowedAdministrationsReached();
      }, 100);
    });
  },

  _buildGui: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;

    this.add(this.therapyDescriptionContainer);

    this.add(new tm.jquery.Spacer({type: 'vertical', size: 4}));
    if (!this.createNewTask && this.administration && this.administration.taskId != null)
    {
      this.add(this.view.getAppFactory().createHRadioButtonGroupContainer(this.confirmCancelRadioButtonGroup));
    }

    if (!this.createNewTask && this.therapy.medicationOrderFormType == enums.medicationOrderFormType.SIMPLE && this.therapy.medication.id)
    {
      this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('medication')));
      this.medicationField =
          tm.views.medications.MedicationUtils.createMedicationsSearchField(this.view, 420, 'advanced', null, true);
      this.add(this.medicationField);
      this.similarMedications = [];
      this.medicationField.setSource(this.similarMedications);
      this._loadSimilarMedications(this.therapy.medication.id, this.therapy.route.code);
      this.medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
      {
        var selection = component.getSelection();
        if (selection && self.medicationId != selection.id)
        {
          self.medicationId = selection.id;
          self._loadMedicationData(selection.id, true);
        }
      });
    }

    if (this.infusionSetChangeButtonGroup)
    {
      this.add(appFactory.createVRadioButtonGroupContainer(this.infusionSetChangeButtonGroup, 0));
    }

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('administration.time')));
    this.add(this.administrationDateTimeCard);
    this.administrationDateTimeCard.add(this.administrationDateField);
    this.administrationDateTimeCard.add(this.administrationTimeField);
    this.add(this.maxAdministrationsWarningLabel);

    this.add(this.doseRateSpacer);
    this.add(this.doseLabel);
    this.add(this.dosePane);
    this.add(this.rateLabel);
    this.add(this.ratePane);
    this.add(this.volumeLabel);
    this.add(this.volumePane);

    this.add(new tm.jquery.Spacer({type: 'vertical', size: 2}));
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('commentary')));
    this.add(this.commentField);
  },

  _loadSimilarMedications: function(medicationId, routeCode) //Similar medications have same generic and route
  {
    var self = this;
    //var medicationsUrl =
    //    this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_SIMILAR_MEDICATIONS;
    var medicationsUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_MEDICATION_PRODUCTS;
    var params = {
      medicationId: medicationId,
      routeCode: routeCode
    };
    this.view.loadViewData(medicationsUrl, params, null, function(data)
    {
      self.similarMedications.length = 0;
      $.merge(self.similarMedications, data);
      if (data.length >= 1)
      {
        self._loadMedicationData(self.similarMedications[0].id, true);
      }
    });
  },

  _getMedicationId: function(therapy)
  {
    var isComplex = therapy.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX;
    if (isComplex)
    {
      return (therapy.ingredientsList && therapy.ingredientsList.length > 0) ? therapy.ingredientsList[0].medication.id : null;
    }
    else
    {
      return therapy.medication.id
    }
  },

  _confirmTherapyAdministration: function()
  {
    var self = this;

    if (this.administration == null)
    {
      this.administration = {};
      this.administration.additionalAdministration = true;
      this.administration.administrationType = this.administrationType;
    }
    this.administration.administeredDose = {};
    this.administration.plannedDose = {};

    var administrationDate = this.administrationDateField.getDate();
    var administrationTime = this.administrationTimeField.getTime();
    var selectedTimestamp = new Date(
        administrationDate.getFullYear(),
        administrationDate.getMonth(),
        administrationDate.getDate(),
        administrationTime.getHours(),
        administrationTime.getMinutes(),
        0, 0);

    this.administration.comment = this.commentField.getValue() ? this.commentField.getValue() : null;

    var therapyDose = {};
    if (!this.dosePane.isHidden()) // TherapyDoseTypeEnum QUANTITY
    {
      var doseValues = this.dosePane.getDoseWithUnits();
      therapyDose.therapyDoseTypeEnum = "QUANTITY";
      therapyDose.numerator = doseValues.quantity;
      therapyDose.numeratorUnit = doseValues.quantityUnit;
      therapyDose.denominator = doseValues.quantityDenominator;
      therapyDose.denominatorUnit = doseValues.denominatorUnit;
    }
    else if (!this.ratePane.isHidden()) //TherapyDoseTypeEnum RATE
    {
      var isCodedMedication = this.medicationId != null; //uncoded medication are from universal form
      var rateValues = this.ratePane.getInfusionRate();
      therapyDose.therapyDoseTypeEnum = "RATE";
      therapyDose.numerator = rateValues.rate;
      therapyDose.numeratorUnit = rateValues.rateUnit;
      therapyDose.denominator = isCodedMedication ? rateValues.rateFormula : null;
      therapyDose.denominatorUnit = isCodedMedication ? rateValues.rateFormulaUnit : null;
    }
    else if (!this.volumePane.isHidden()) //TherapyDoseTypeEnum VOLUME_SUM
    {
      therapyDose.therapyDoseTypeEnum = "VOLUME_SUM";
      therapyDose.numerator = this.volumeField.getValue();
      therapyDose.numeratorUnit = 'ml';
      therapyDose.denominator = null;
      therapyDose.denominatorUnit = null;
    }

    if (this.medicationField)
    {
      var selectedMedication = this.medicationField.getSelection();
      var therapyMedication = this._getMedicationId(this.therapy);
      if (selectedMedication.id != therapyMedication)
      {
        this.administration.substituteMedication = selectedMedication;
      }
    }

    if (this.infusionSetChangeButtonGroup)
    {
      this.administration.infusionSetChangeEnum = this.infusionSetChangeButtonGroup.getActiveRadioButton().data;
    }

    var url;
    var params;
    if (this.createNewTask)
    {
      this.administration.plannedTime = selectedTimestamp;
      this.administration.plannedDose = therapyDose;

      url = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_CREATE_ADMINISTRATION_TASK;
      params = {
        therapyCompositionUid: this.therapy.compositionUid,
        ehrOrderName: this.therapy.ehrOrderName,
        patientId: this.patientId,
        administration: JSON.stringify(this.administration)
      };
    }
    else
    {
      this.administration.administrationTime = selectedTimestamp;
      this.administration.administeredDose = therapyDose;

      url = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_CONFIRM_THERAPY_ADMINISTRATION;
      var administrationSuccessful = this.confirmCancelRadioButtonGroup.getActiveRadioButton().data;
      var centralCaseData = self.view.getCentralCaseData(); //TherapyCentralCaseData

      params = {
        therapyCompositionUid: this.therapy.compositionUid,
        ehrOrderName: this.therapy.ehrOrderName,
        patientId: this.patientId,
        administrationSuccessful: administrationSuccessful,
        editMode: this.editMode,
        administration: JSON.stringify(this.administration),
        centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
        knownOrganizationalEntity: self.view.getKnownOrganizationalEntity(),
        careProviderId: centralCaseData ? centralCaseData.careProviderId : null,
        sessionId: centralCaseData && centralCaseData.sessionId ? centralCaseData.sessionId : null
      };
    }

    this.view.loadPostViewData(url, params, null,
        function()
        {
          var resultData = new app.views.common.AppResultData({success: true});
          self.resultCallback(resultData);
        },
        function()
        {
          var resultData = new app.views.common.AppResultData({success: false});
          self.resultCallback(resultData);
        },
        true);
  },

  _loadMedicationData: function(medicationId, setMedicationField)
  {
    var self = this;
    if (medicationId)
    {
      var medicationDataUrl =
          this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MEDICATION_DATA;
      var params = {medicationId: medicationId};
      this.view.loadViewData(medicationDataUrl, params, null, function(data)
      {
        self._presentMedicationData(data, setMedicationField);
      });
    }
    else //uncoded medications (universal forms)
    {
      var appFactory = this.view.getAppFactory();
      appFactory.createConditionTask(
          function()
          {
            self._presentMedicationData(null, false);
          },
          function()
          {
            return self.isRendered(true);
          },
          50, 1000
      );
    }
  },

  _presentMedicationData: function(medicationData, setMedicationField)
  {
    var therapyDoseTypeEnum = this.therapyDoseTypeEnum;

    if (this.administration)
    {
      if (this.administration.plannedDose && this.administration.plannedDose.therapyDoseTypeEnum)
      {
        therapyDoseTypeEnum = this.administration.plannedDose.therapyDoseTypeEnum;
      }
      else if (this.administration.administeredDose && this.administration.administeredDose.therapyDoseTypeEnum)
      {
        therapyDoseTypeEnum = this.administration.administeredDose.therapyDoseTypeEnum;
      }

      if (this.administration.infusionSetChangeEnum)
      {
        var buttons = this.infusionSetChangeButtonGroup.getRadioButtons();
        for (var i = 0; i < buttons.length; i++)
        {
          if (buttons[i].data == this.administration.infusionSetChangeEnum)
          {
            this.infusionSetChangeButtonGroup.setActiveRadioButton(buttons[i]);
            break;
          }
        }
      }
    }

    if (medicationData && setMedicationField && this.medicationField)
    {
      this.medicationField.setSelection(medicationData.medication);
    }

    var administrationDose;
    if (this.administration)
    {
      administrationDose = this.editMode ? this.administration.administeredDose : this.administration.plannedDose;
    }
    if (!therapyDoseTypeEnum)
    {
      this._hideAllDoseRateFields();
    }
    else if (therapyDoseTypeEnum == 'QUANTITY')
    {
      if (medicationData)
      {
        this.dosePane.setMedicationData(medicationData);
      }
      else //therapies from universal form
      {
        if (this.therapy.medicationOrderFormType == "SIMPLE")
        {
          this.dosePane.setUnits(this.therapy.quantityUnit, this.therapy.quantityDenominatorUnit);
        }
        if (this.therapy.medicationOrderFormType == "COMPLEX" && this.therapy.ingredientsList.length == 1)
        {
          var onlyIngredient = this.therapy.ingredientsList[0];
          if (onlyIngredient.quantityUnit)
          {
            this.dosePane.setUnits(onlyIngredient.quantityUnit, onlyIngredient.volumeUnit);
          }
          else
          {
            this.dosePane.setUnits(onlyIngredient.volumeUnit, null);
          }
        }
      }
      this.doseLabel.show();
      this.dosePane.show();
      if (this.administration && administrationDose.numerator)
      {
        this.dosePane.setDoseNumerator(administrationDose.numerator);
      }
      //only set denominator for uncoded medication (!medicationData), for coded medications it calculates from numerator
      if (!medicationData && this.administration && administrationDose.denominator)
      {
        this.dosePane.setDoseDenominator(administrationDose.denominator);
      }

    }
    else if (therapyDoseTypeEnum == 'VOLUME_SUM')
    {
      this.dosePane.setMedicationData(medicationData);
      this.volumeLabel.show();
      this.volumePane.show();
      if (this.administration && administrationDose.numerator)
      {
        this.volumeField.setValue(administrationDose.numerator);
      }
    }
    else if (therapyDoseTypeEnum == 'RATE')
    {
      this.ratePane.setFirstMedicationData(medicationData);
      this.rateLabel.show();
      this.ratePane.show();
      this.ratePane.setDurationVisible(false);
      if (this.administration && administrationDose)
      {
        this.ratePane.setFormulaUnitToLabel(administrationDose.denominatorUnit);
        this.ratePane.setRate(administrationDose.numerator);
      }
      this.ratePane.setFormulaVisible();
    }
  },

  _hideAllDoseRateFields: function()
  {
    this.doseRateSpacer.hide();
    this.dosePane.hide();
    this.doseLabel.hide();
    this.rateLabel.hide();
    this.ratePane.hide();
    this.volumeLabel.hide();
    this.volumePane.hide();
  },

  _setupValidation: function()
  {
    var self = this;
    if (this.medicationField)
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: self.medicationField,
        required: true,
        componentValueImplementationFn: function()
        {
          return self.medicationField.getSelection();
        }
      }));
    }
    if (!this.dosePane.isHidden())
    {
      this._addValidations(this.dosePane.getDosePaneValidations());
    }
    if (!this.ratePane.isHidden())
    {
      this._addValidations(this.ratePane.getInfusionRatePaneValidations());
    }
    this.validationForm.addFormField(
        new tm.jquery.FormField({
          component: self.administrationTimeField,
          required: true,
          componentValueImplementationFn: function()
          {
            if (self.maxAdministrationsWarningLabel.isHidden())
            {
              return true;
            }
            return null;
          }
        }));
    if (!this.volumePane.isHidden())
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: self.volumeField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.volumeField.getValue();
          if (value == null || value <= 0)
          {
            return null;
          }
          return true;
        }
      }));
    }
  },

  _addValidations: function(validation)
  {
    for (var i = 0; i < validation.length; i++)
    {
      this.validationForm.addFormField(validation[i]);
    }
  },

  _assertNumberOfAllowedAdministrationsReached: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.therapy.maxDailyFrequency) && !tm.jquery.Utils.isEmpty(this.administrations))
    {
      var administrationDate = this.administrationDateField.getDate();
      var administrationTime = this.administrationTimeField.getTime();

      if (!tm.jquery.Utils.isEmpty(administrationDate) && !tm.jquery.Utils.isEmpty(administrationTime))
      {
        var selectedTimestamp = new Date(
            administrationDate.getFullYear(),
            administrationDate.getMonth(),
            administrationDate.getDate(),
            administrationTime.getHours(),
            administrationTime.getMinutes(),
            0, 0);
      }

      var administrationTimestamps = this._getAdministrationsTimestampsFor24HourInterval(this.administrations, selectedTimestamp);
      administrationTimestamps.unshift(selectedTimestamp);

      for (var i = 0; i < administrationTimestamps.length; i++)
      {
        var administrationTimestamp = administrationTimestamps[i];
        var tasksInInterval = this._getTaskFor24HoursFromTime(this.administrations, administrationTimestamp);
        if (tasksInInterval.length >= this.therapy.maxDailyFrequency)
        {
          this._assertNextAllowedAdministrationTime(tasksInInterval, administrationTimestamp);
          this.maxAdministrationsWarningLabel.show();
          break;
        }
        else
        {
          this.maxAdministrationsWarningLabel.hide();
        }
      }
    }
    else
    {
      this.maxAdministrationsWarningLabel.hide();
    }

  },

  _getAdministrationsTimestampsFor24HourInterval: function(administrations, when)
  {
    var timestamps = [];
    for (var i = 0; i < administrations.length; i++)
    {
      var administration = administrations[i];
      var isAdministered = tm.jquery.Utils.isEmpty(administration.administrationTime) == false;
      var administrationTime = isAdministered ? new Date(administration.administrationTime) : new Date(administration.plannedTime);
      var timePlus24Hours = when.getTime() + 86400000;
      if (when.getTime() < administrationTime.getTime() && timePlus24Hours > administrationTime)
      {
        timestamps.push(administrationTime);
      }
    }
    return timestamps;
  },

  _assertNextAllowedAdministrationTime: function(tasksInInterval, selectedTimestamp)
  {
    //gets first task for interval where maxDailyFrequency isn't reached
    tasksInInterval.sort(function(administrationA, administrationB)
    {
      var isAdministeredA = tm.jquery.Utils.isEmpty(administrationA.administrationTime) == false;
      var administrationTimeA = isAdministeredA ? administrationA.administrationTime : administrationA.plannedTime;
      var administrationTimeAMillis = (new Date(administrationTimeA)).getTime();

      var isAdministeredB = tm.jquery.Utils.isEmpty(administrationB.administrationTime) == false;
      var administrationTimeB = isAdministeredB ? administrationB.administrationTime : administrationB.plannedTime;
      var administrationTimeBMillis = (new Date(administrationTimeB)).getTime();

      return administrationTimeAMillis - administrationTimeBMillis;
    });

    while (tasksInInterval.length > this.therapy.maxDailyFrequency)
    {
      tasksInInterval.shift();
      tasksInInterval = this._getTaskFor24HoursFromTime(tasksInInterval, selectedTimestamp);
    }
    var firstIntervalAdministration = tasksInInterval[0];
    var isAdministered = tm.jquery.Utils.isEmpty(firstIntervalAdministration.administrationTime) == false;
    var firstIntervalAdministrationTime = isAdministered ? firstIntervalAdministration.administrationTime : firstIntervalAdministration.plannedTime;

    var nextAdministration = new Date(firstIntervalAdministrationTime);
    nextAdministration.setDate(nextAdministration.getDate() + 1);

    var tasksInNextInterval = this._getTaskFor24HoursFromTime(this.administrations, nextAdministration);
    //assertNumberOfAllowedAdministrations for the calculated nextAdministration time - repeat until you find appropriate value
    if (tasksInNextInterval.length >= this.therapy.maxDailyFrequency)
    {
      this._assertNextAllowedAdministrationTime(tasksInNextInterval, nextAdministration);
    }
    else
    {
      var nextAdministrationDisplayValue = this.view.getDisplayableValue(nextAdministration, "short.date.time");
      this.maxAdministrationsWarningLabel.setText(this.view.getDictionary('dosing.max.24h.warning') + " " + nextAdministrationDisplayValue);
    }
  },

  _getTaskFor24HoursFromTime: function(administrations, when)
  {
    if (administrations == null || administrations.length == 0)
    {
      return 0;
    }

    var tasksInInterval = [];
    for (var i = 0; i < administrations.length; i++)
    {
      var administration = administrations[i];
      if (administration.administrationStatus != 'FAILED')
      {
        var isAdministered = tm.jquery.Utils.isEmpty(administration.administrationTime) == false;
        var administrationTime = isAdministered ? administration.administrationTime : administration.plannedTime;
        var administrationTimeMillis = (new Date(administrationTime)).getTime();
        var whenMillis = when.getTime();
        var twentyFourHoursInMillis = 86400000;
        if ((whenMillis >= administrationTimeMillis) && ((whenMillis - administrationTimeMillis) < twentyFourHoursInMillis))
        {
          tasksInInterval.push(administration);
        }
      }
    }
    //console.log("#####  UNDELETED TASKS IN LAST 24 HOURS FROM ", when,  " are: ", tasksInInterval.length, " ", tasksInInterval);
    return tasksInInterval;
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit();
  }
});