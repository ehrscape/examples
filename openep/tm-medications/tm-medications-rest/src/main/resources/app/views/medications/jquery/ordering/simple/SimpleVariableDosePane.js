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
  /** privates*/
  validationForm: null,
  initialState: null,
  timeFields: null,
  dosePanes: null,
  resultDataCallback: null,
  /** privates: components */
  dosingFrequencyPane: null,
  rowsContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.initialState = true;
    this.timeFields = [];
    this.dosePanes = [];

    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"));
    this._buildComponents();
    this._buildGui();

    this.dosingFrequencyPane.setFrequency(this.frequency);

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
    this.dosingFrequencyPane = new app.views.medications.ordering.DosingFrequencyPane({
      view: this.view,
      withSingleFrequencies: false,
      frequencyChangeEvent: function()
      {
        self._handleDosingFrequencyChange();
      }
    });
    this.rowsContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5)});

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
    var container = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "stretch")});
    container.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval')));
    container.add(this.dosingFrequencyPane);
    container.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));
    container.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dose')));
    container.add(this.rowsContainer);
    this.add(container);
  },

  _addField: function(doseTime, numerator)
  {
    var self = this;

    if (doseTime)
    {
      var time = new Date();
      time.setHours(doseTime.hour);
      time.setMinutes(doseTime.minute);
    }
    var timePicker = new tm.jquery.TimePicker({time: time ? time : null});
    this.timeFields.push(timePicker);

    var dosePane = new app.views.medications.ordering.DosePane({
      view: this.view,
      medicationData: this.medicationData,
      doseNumerator: numerator,
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
          }
        }
        else
        {
          dosePane.requestFocusToDenominator();
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
          else
          {
            nextDosePane.requestFocusToNumerator();
          }
        }
        else
        {
          dosePane.requestFocusToDenominator();
        }
      }
    });
    this.dosePanes.push(dosePane);

    var row = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5)});
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
          this.timedDoseElements[i].doseElement ? this.timedDoseElements[i].doseElement.quantity : null);
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
    var administrationTimes =
        tm.views.medications.MedicationTimingUtils.getFrequencyAdministrationTimesOfDay(
            this.view.getAdministrationTiming(), frequencyKey);
    if (administrationTimes.length > 0)
    {
      for (var i = 0; i < administrationTimes.length; i++)
      {
        this._addField(administrationTimes[i]);
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
    }
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

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit();
  }
});

