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

Class.define('app.views.medications.ordering.SimpleVariableDosePane', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'variable-dose-pane',
  scrollable: 'vertical',
  /** configs */
  view: null,
  medicationData: null,
  timedDoseElements: null,
  frequency: null,
  addDosageCalculationPane: false,
  showDosageCalculationPanes: false,
  /** privates*/
  validationForm: null,
  initialState: null,
  timeFields: null,
  dosePanes: null,
  resultDataCallback: null,
  /** privates: components */
  dosingFrequencyPane: null,
  rowsContainer: null,
  dosageCalculationUnitCombo: null,
  dosageCalculationBtn: null,
  dosageCalculationUnitHeader: null,
  _dosingPatternValidator: null,
  _testRenderCoordinator: null,

  visibilityContext: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.initialState = true;
    this.timeFields = [];
    this.dosePanes = [];

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'simple-variable-dose-pane-coordinator',
      view: this.getView(),
      component: this
    });

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
    this._setDosageCalculationUnits();
    this.dosingFrequencyPane.setFrequency(this.frequency, false);

    this._dosingPatternValidator = new app.views.medications.common.DosingPatternValidator({
     view: this.getView()
    });
    var self = this;
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self.dosingFrequencyPane.requestFocus();
      }, 0);
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.dosageCalculationUnitHeader = new tm.jquery.Container({
      cls: "TextLabel",
      width: 250,
      html: this.view.getDictionary('dosage.calculation')
    });
    this.dosageCalculationUnitCombo = new tm.jquery.SelectBox({
      cls: "dosage-calculation-unit-combo",
      width: 125,
      padding: '0 5 0 0',
      allowSingleDeselect: false,
      multiple: false,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
      },
      hidden: true
    });

    this.dosageCalculationUnitCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._changeDosageCalculationUnits(component.getSelections()[0]);
    });

    this.dosageCalculationBtn = new tm.jquery.Button({
      cls: "dosage-calculation-icon dosage-calculation-button",
      handler: function()
      {
        self.showDosageCalculationPanes = true;
        self.dosageCalculationUnitCombo.show();
        self.dosageCalculationBtn.hide();
        self._showDosageCalculationPanes(self.dosageCalculationUnitCombo.getSelections()[0]);
        self.dosageCalculationUnitHeader.setHtml(self.view.getDictionary('dosage.calculation.unit'));
      }
    });

    this.dosingFrequencyPane = new app.views.medications.ordering.DosingFrequencyPane({
      view: this.view,
      withSingleFrequencies: false,
      visibilityContext: this.visibilityContext,
      frequencyChangeEvent: function()
      {
        self._handleDosingFrequencyChange();
      }
    });
    this.rowsContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 5),
      scrollable: 'visible'
    });

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._returnResult();
      },
      onValidationError: function()
      {
        self.resultCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: self.view.getDictionary("field.value.is.required")
    });
  },

  _buildGui: function()
  {
    var container = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: 'visible'
    });
    if (this.addDosageCalculationPane)
    {
      container.add(this.dosageCalculationUnitHeader);
      container.add(this.dosageCalculationUnitCombo);
      container.add(this.dosageCalculationBtn);
    }
    container.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval')));
    container.add(this.dosingFrequencyPane);
    container.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));
    container.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dose')));
    container.add(this.rowsContainer);
    this.add(container);
  },

  _addField: function(doseTime, numerator, denominator, enabled, frequencyType)
  {
    var self = this;

    if (doseTime)
    {
      var time = CurrentTime.get();
      time.setHours(doseTime.hour);
      time.setMinutes(doseTime.minute);
    }
    var timePicker = new tm.jquery.TimePicker({
      cls: "time-field",
      time: time ? time : null,
      enabled: enabled,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this.timeFields.push(timePicker);

    timePicker.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (!tm.jquery.Utils.isEmpty(frequencyType))
      {
        if (component.isEnabled() &&
            frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
        {
          self._recalculateTimesForBetweenDoses();
        }
      }
    });

    var dosePane = new app.views.medications.ordering.DosePane({
      cls: "dose-pane with-large-input",
      view: this.view,
      medicationData: this.medicationData,
      doseNumerator: numerator,
      doseDenominator: denominator,
      addDosageCalculationPane: true,
      showDoseUnitCombos: false,
      showDosageCalculation: this.showDosageCalculationPanes,
      showRounding: true,
      numeratorFocusLostEvent: function(dosePane)
      {
        var index = self.dosePanes.indexOf(dosePane);
        self.timeFields[index].focus();
        if (dosePane.getDose().quantity)
        {
          var nextDosePane = self.dosePanes[index + 1];
          if (nextDosePane)
          {
            nextDosePane.requestFocusToNumerator();
            dosePane.suppressDosageCalculationTooltips();
          }
        }
        else
        {
          if (!dosePane.denominatorField.isHidden())
          {
            dosePane.requestFocusToDenominator();
          }
          else if (!dosePane.isDosageCalculationHidden())
          {
            dosePane.requestFocusToDosageCalculation();
          }
        }
      },
      denominatorFocusLostEvent: function(dosePane)
      {
        var index = self.dosePanes.indexOf(dosePane);
        var nextDosePane = self.dosePanes[index + 1];
        if (nextDosePane)
        {
          if (dosePane.getDose().quantityDenominator)
          {
            nextDosePane.requestFocusToDenominator();
          }
          else if (!dosePane.isDosageCalculationHidden())
          {
            dosePane.requestFocusToDosageCalculation();
          }
          else
          {
            nextDosePane.requestFocusToNumerator();
          }
          dosePane.suppressDosageCalculationTooltips();
        }
        else if (!dosePane.isDosageCalculationHidden())
        {
          dosePane.requestFocusToDosageCalculation();
        }
        else
        {
          dosePane.requestFocusToDenominator();
        }
      },
      dosageCalculationFocusLostEvent: function(dosePane)
      {
        var index = self.dosePanes.indexOf(dosePane);
        var nextDosePane = self.dosePanes[index + 1];
        if (nextDosePane)
        {
          if (!tm.jquery.Utils.isEmpty(dosePane.getDosageCalculation()))
          {
            nextDosePane.requestFocusToDosageCalculation();
          }
          else
          {
            nextDosePane.requestFocusToNumerator();
          }
          dosePane.suppressDosageCalculationTooltips();
        }
        else
        {
          timePicker.focus();
        }
      }
    });
    if (this.showDosageCalculationPanes)
    {
      if (this.dosageCalculationUnitCombo.getSelections().length > 0)
      {
        dosePane.setDosageCalculationUnitLabel(this.dosageCalculationUnitCombo.getSelections()[0]);
      }
      else if (this.dosageCalculationUnitCombo.getOptions().length > 0)
      {
        dosePane.setDosageCalculationUnitLabel(this.dosageCalculationUnitCombo.getOptions()[0].value);
      }
    }
    this.dosePanes.push(dosePane);
    var row = new tm.jquery.Container({
      cls: "dose-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "start", 5),
      scrollable: 'visible'
    });
    row.add(timePicker);
    row.add(dosePane);
    this.rowsContainer.add(row);
  },

  _presentData: function()
  {
    for (var i = 0; i < this.timedDoseElements.length; i++)
    {
      this._addField(
          this.timedDoseElements[i].doseTime,
          this.timedDoseElements[i].doseElement ? this.timedDoseElements[i].doseElement.quantity : null,
          this.timedDoseElements[i].doseElement ? this.timedDoseElements[i].doseElement.quantityDenominator : null);
    }
    this.rowsContainer.repaint();
  },

  _rebuildRows: function()
  {
    var enums = app.views.medications.TherapyEnums;
    this.timeFields.removeAll();
    this.dosePanes.removeAll();
    this.rowsContainer.removeAll(true);
    var frequencyKey = this.dosingFrequencyPane.getFrequencyKey();
    var frequencyType = this.dosingFrequencyPane.getFrequencyType();
    var administrationTimes =
        tm.views.medications.MedicationTimingUtils.getFrequencyTimingPattern(
            this.view.getAdministrationTiming(), frequencyKey, frequencyType);
    if (administrationTimes.length > 0)
    {
      for (var i = 0; i < administrationTimes.length; i++)
      {
        var enabled = i === 0 || frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.DAILY_COUNT;
        this._addField(administrationTimes[i], null, null, enabled, frequencyType);
      }
    }
    else
    {
      var frequency = this.dosingFrequencyPane.getFrequency();
      if (frequency)
      {
        if (frequency.type == enums.dosingFrequencyTypeEnum.DAILY_COUNT)
        {
          if (frequency.value)
          {
            for (var j = 0; j < frequency.value; j++)
            {
              this._addField();
            }
          }
        }
        else if (frequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
        {
          if (frequency.value)
          {
            for (var k = 0; k < 24 / frequency.value; k++)
            {
              this._addField();
            }
          }
        }
        else
        {
          this._addField();
        }
      }
    }
    this.rowsContainer.repaint();
  },

  _handleDosingFrequencyChange: function()
  {
    if (this.timedDoseElements && this.timedDoseElements.length > 0 && this.initialState)
    {
      this._presentData();
      this.initialState = false;
    }
    else
    {
      this._rebuildRows();
    }

    if (this.dosePanes.length > 0)
    {
      this.dosePanes[0].requestFocusToNumerator();
    }
  },

  _setupValidation: function()
  {
    this.validationForm.reset();
    this._addValidations(this.dosingFrequencyPane.getDosingFrequencyPaneValidations());

    var times = [];
    for (var i = 0; i < this.timeFields.length; i++)
    {
      var dosePane = this.dosePanes[i];
      this._addValidations(dosePane.getDosePaneValidations());

      var timeField = this.timeFields[i];
      var timeFieldValidation = new tm.jquery.FormField({
        component: timeField,
        required: true,
        componentValueImplementationFn: function(component)
        {
          return component.getTime();
        }
      });
      this._addValidations([timeFieldValidation]);
      times.push(this.timeFields[i].getTime());
    }
    this.validationForm.addFormField(this._dosingPatternValidator.getDosingPatternValidation(this.rowsContainer, times));
  },

  _addValidations: function(validation)
  {
    for (var i = 0; i < validation.length; i++)
    {
      this.validationForm.addFormField(validation[i]);
    }
  },

  _returnResult: function()
  {
    var timedDoseElements = [];
    for (var i = 0; i < this.timeFields.length; i++)
    {
      var time = this.timeFields[i].getTime();
      var dose = this.dosePanes[i].getDose();
      timedDoseElements.push({
        doseElement: {
          quantity: dose.quantity,
          quantityDenominator: dose.quantityDenominator
        },
        doseTime: {
          hour: time.getHours(),
          minute: time.getMinutes()
        }
      });
    }
    var resultValue = {
      timedDoseElements: timedDoseElements,
      frequency: this.dosingFrequencyPane.getFrequency()
    };
    this.resultCallback(new app.views.common.AppResultData({success: true, value: resultValue}));
  },

  _changeDosageCalculationUnits: function(selectedUnit)
  {
    for (var i = 0; i < this.dosePanes.length; i++)
    {
      var dosePane = this.dosePanes[i];
      dosePane.setDosageCalculationUnitLabel(selectedUnit);
    }
  },

  _showDosageCalculationPanes: function(selectedUnit)
  {
    for (var i = 0; i < this.dosePanes.length; i++)
    {
      var dosePane = this.dosePanes[i];
      dosePane.showDosageCalculationFields();
      dosePane.setDosageCalculationUnitLabel(selectedUnit);
      dosePane.setDosageCalculationFieldValue();
    }
  },

  _setDosageCalculationUnits: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.medicationData))
    {
      var doseUnit = this.medicationData.getStrengthNumeratorUnit();
      this.dosageCalculationUnitCombo.removeAllOptions();
      var patientHeight = this.view.getPatientHeightInCm();
      var dosageCalculationUnits = tm.views.medications.MedicationUtils.getDosageCalculationUnitOptions(this.view, doseUnit, patientHeight);
      var selectedId = null;
      var selectedOption = null;
      var setOptionSelected = null;
      var selectBoxOptions = dosageCalculationUnits.map(function(option)
      {
        selectedId = option.doseUnit == doseUnit && option.patientUnit == "kg" ? option.id : selectedId;
        setOptionSelected = option.doseUnit == doseUnit && option.patientUnit == "kg";
        var currentOption = tm.jquery.SelectBox.createOption(option, option.displayUnit, null, null, setOptionSelected);
        if (setOptionSelected)
        {
          selectedOption = currentOption.value;
        }
        return currentOption;
      });
      this.dosageCalculationUnitCombo.addOptions(selectBoxOptions);

      if (!tm.jquery.Utils.isEmpty(selectedOption))
      {
        this.dosageCalculationUnitCombo.setSelections([selectedOption]);
      }
    }
    else
    {
      this.dosageCalculationUnitCombo.hide()
    }

  },

  _recalculateTimesForBetweenDoses: function()
  {
    var frequency = this.dosingFrequencyPane.getFrequencyValue();

    if (this.timeFields.length > 0)
    {
      var firstTime = this.timeFields[0].getTime();
      if (firstTime)
      {
        for (var i = 1; i < this.timeFields.length; i++)
        {
          this.timeFields[i].setTime(new Date(firstTime.getTime() + i * frequency * 60 * 60 * 1000), true);
        }
      }
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit();
  }
});

