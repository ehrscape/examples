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

Class.define('app.views.medications.ordering.ComplexVariableRatePane', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'variable-rate-pane',
  scrollable: 'vertical',
  /** configs */
  view: null,
  medicationData: null,
  infusionIngredients: null,
  continuousInfusion: null,
  volumeSum: null,
  timedDoseElements: null,
  showFormula: true,
  /** privates*/
  validationForm: null,
  startFields: null,
  endFields: null,
  ratePanes: null,
  rows: null,
  resultDataCallback: null,
  /** privates: components */
  formulaUnitPane: null,
  rowsContainer: null,
  addButton: null,
  removeButton: null,
  warningLabel: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.startFields = [];
    this.endFields = [];
    this.ratePanes = [];
    this.rows = [];

    this.setLayout(tm.jquery.VFlexboxLayout.create('start', 'start', 5));
    this._buildComponents();
    this._buildGui();
    this._presentData();

    var self = this;
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self.startFields[0].focus();
      }, 0);
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.formulaUnitPane = new app.views.medications.ordering.InfusionRateFormulaUnitPane({
      view: this.view,
      formulaUnitChangeEvent: function()
      {
        setTimeout(function()
        {
          for (var i = 0; i < self.ratePanes.length; i++)
          {
            var formulaUnit = self.formulaUnitPane.getRateFormulaUnit();
            self.ratePanes[i].setFormulaUnitToLabel(formulaUnit.displayUnit);
          }
        }, 200);
      }
    });
    this.formulaUnitPane.setMedicationData(this.medicationData);

    this.rowsContainer = new tm.jquery.Container({layout: new tm.jquery.VFlexboxLayout({gap: 5})});

    this.addButton = new tm.jquery.Container({cls: 'add-icon', width: 30, height: 30});
    this.addButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      var formulaUnit = self.formulaUnitPane.getRateFormulaUnit();
      var index = self.endFields.length - 1;
      self._endLabelToField(index);
      self._addField(null, formulaUnit.displayUnit, true);
      self.rowsContainer.repaint();
    });
    this.removeButton = new tm.jquery.Container({cls: 'remove-icon', width: 30, height: 30});
    this.removeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._removeLastField();
    });

    this.warningLabel = new tm.jquery.Label({emphasis: "error"});

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
    if (this.showFormula)
    {
      this.add(this.formulaUnitPane);
      this.add(new tm.jquery.Spacer({type: 'vertical', size: 10}));
    }
    this.add(this.rowsContainer);

    var buttonsContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    this.add(buttonsContainer);
    buttonsContainer.add(this.addButton);
    buttonsContainer.add(this.removeButton);
    this.add(this.warningLabel);
  },

  _addField: function(timedDoseElement, formulaUnitDisplay, endAsLabel)
  {
    var self = this;

    var start = null;
    var end = null;
    var rate = null;
    if (timedDoseElement)
    {
      if (timedDoseElement.doseTime)
      {
        start = new Date();
        start.setHours(timedDoseElement.doseTime.hour);
        start.setMinutes(timedDoseElement.doseTime.minute);

        if (timedDoseElement.doseElement && timedDoseElement.doseElement.duration)
        {
          end = new Date();
          end.setHours(start.getHours());
          end.setMinutes(start.getMinutes() + timedDoseElement.doseElement.duration);
        }
      }
      if (timedDoseElement.doseElement && timedDoseElement.doseElement.rate)
      {
        rate = timedDoseElement.doseElement.rate;
      }
    }

    var startField = new tm.jquery.TimePicker({time: start ? start : null, width: 76});
    startField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._calculateEnd();
    });
    this.startFields.push(startField);

    var endField;
    if (endAsLabel)
    {
      endField = this._buildEndLabel(end);
    }
    else
    {
      endField = this._buildEndField(end);
    }
    this.endFields.push(endField);

    var ratePane = new app.views.medications.ordering.InfusionRatePane({
      view: this.view,
      cls: "infusion-rate-pane",
      getInfusionIngredientsFunction: function()
      {
        if (self.infusionIngredients)
        {
          return self.infusionIngredients;
        }
        return [];
      },
      getContinuousInfusionFunction: function()
      {
        return self.continuousInfusion;
      },
      getVolumeSumFunction: function()
      {
        return self.volumeSum;
      },
      changeEvent: function()
      {
        self._calculateEnd();
      },
      formulaVisibleFunction: function()
      {
        return self.showFormula;
      }
    });
    ratePane.setFirstMedicationData(this.medicationData);
    ratePane.setDurationVisible(false);
    if (rate)
    {
      ratePane.setInfusionRate(rate);
    }
    ratePane.setFormulaUnitToLabel(formulaUnitDisplay);
    ratePane.setFormulaVisible();
    this.ratePanes.push(ratePane);

    var row = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    row.add(startField);
    row.add(tm.views.medications.MedicationUtils.crateLabel('TextData', this.view.getDictionary("until.low.case")));
    row.add(endField);
    row.add(new tm.jquery.Spacer({type: 'horizontal', size: 20}));
    row.add(ratePane);
    this.rows.push(row);
    this.rowsContainer.add(row);
  },

  _endLabelToField: function(index)
  {
    var endLabel = this.endFields[index];
    var endField = this._buildEndField(null);
    this.rows[index].replace(endLabel, endField);
    this.endFields[index] = endField;
  },

  _endFieldToLabel: function(index)
  {
    var endField = this.endFields[index];
    var endLabel = this._buildEndLabel();
    this.rows[index].replace(endField, endLabel);
    this.endFields[index] = endLabel;
    this._calculateEnd();
  },

  _buildEndLabel: function(end)
  {
    var endString =
        end ? tm.views.medications.MedicationTimingUtils.hourMinuteToString(end.getHours(), end.getMinutes()) : "";
    var endLabelText = this.continuousInfusion ? this.view.getDictionary('cancel.accusative') : endString;
    return new tm.jquery.Container({cls: 'TextData', isLabel: true, labelDate: end, html: endLabelText, width: 76, padding: "5 0 0 5"});
  },

  _setLastEndLabelValue: function(labelDate)
  {
    var lastEndField = this.endFields[this.endFields.length - 1];
    if (labelDate)
    {
      var calculatedEndString =
          tm.views.medications.MedicationTimingUtils.hourMinuteToString(labelDate.getHours(), labelDate.getMinutes());
      lastEndField.setHtml(calculatedEndString);
      lastEndField.labelDate = labelDate;
    }
    else
    {
      lastEndField.setHtml(this.continuousInfusion ? this.view.getDictionary('cancel.accusative') : "");
      lastEndField.labelDate = null;
    }
  },

  _buildEndField: function(end)
  {
    var self = this;
    var endField = new tm.jquery.TimePicker({time: end ? end : null});
    endField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_LOST, function(component)
    {
      var textValue = component.getField().getValue();
      if (textValue)
      {
        if ((textValue.indexOf('+') == 0))
        {
          var index;
          var startTime;
          var endTime;
          if (textValue.indexOf('h') == textValue.length - 1)
          {
            var plusHours = textValue.substring(1, textValue.length - 1);
            if (tm.jquery.Utils.isNumeric(plusHours))
            {
              index = self.endFields.indexOf(component);
              startTime = self.startFields[index].getTime();
              if (startTime)
              {
                var hours = Number(startTime.getHours()) + Number(plusHours);
                endTime = new Date(startTime.getFullYear(), startTime.getMonth(), startTime.getDate(), hours, startTime.getMinutes(), 0, 0);
                component.setTime(endTime);
              }
            }
          }
          else
          {
            var plusMinutes = null;
            if (textValue.indexOf('m') == textValue.length - 1)
            {
              plusMinutes = textValue.substring(1, textValue.length - 1);
            }
            else
            {
              plusMinutes = textValue.substring(1, textValue.length);
            }

            if (tm.jquery.Utils.isNumeric(plusMinutes))
            {
              index = self.endFields.indexOf(component);
              startTime = self.startFields[index].getTime();
              var minutes = Number(startTime.getMinutes()) + Number(plusMinutes);
              endTime = new Date(startTime.getFullYear(), startTime.getMonth(), startTime.getDate(), startTime.getHours(), minutes, 0, 0);
              component.setTime(endTime);
            }
          }
        }
      }
    });
    endField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      var index = self.endFields.indexOf(component);
      var nextStartField = self.startFields[index + 1];
      if (nextStartField)
      {
        nextStartField.setTime(component.getTime());
      }
      self._calculateEnd();
    });
    return endField;
  },

  _removeLastField: function()
  {
    var lastRowIndex = this.rows.length - 1;
    var lastRow = this.rows[lastRowIndex];
    this.rowsContainer.remove(lastRow);
    this.startFields.pop();
    this.endFields.pop();
    this.ratePanes.pop();
    this.rows.pop();
    this._endFieldToLabel(this.rows.length - 1);
  },

  _presentData: function()
  {
    if (this.timedDoseElements.length > 0)
    {
      var formulaUnitDisplay = this.timedDoseElements[0].doseElement.rateFormulaUnit;

      var self = this;
      setTimeout(function()
      {
        self.formulaUnitPane.setFormulaUnit(formulaUnitDisplay);
      }, 100);

      for (var i = 0; i < this.timedDoseElements.length; i++)
      {
        this._addField(this.timedDoseElements[i], formulaUnitDisplay, i == this.timedDoseElements.length - 1);
      }
    }
    else
    {
      var defaultFormulaUnit = this.formulaUnitPane.getDefaultValue();
      this._addField(null, defaultFormulaUnit.displayUnit, false);
      this._addField(null, defaultFormulaUnit.displayUnit, true);
    }
  },

  _getDurationInMinutes: function(start, end)
  {
    var startInMinutes = start.getHours() * 60 + start.getMinutes();
    var endInMinutes = end.getHours() * 60 + end.getMinutes();
    if (startInMinutes <= endInMinutes)
    {
      return endInMinutes - startInMinutes;
    }
    //next day
    return 24 * 60 - (startInMinutes - endInMinutes);
  },

  _calculateEnd: function()
  {
    this.warningLabel.setText('');
    if (!this.volumeSum || this.continuousInfusion)
    {
      this._setLastEndLabelValue(null);
      return;
    }

    var usedVolume = 0;
    var usedTimeInMinutes = 0;
    for (var i = 0; i < this.rows.length - 1; i++)
    {
      var infusionRate = this.ratePanes[i].getInfusionRate();
      var start = this.startFields[i].getTime();
      var end = this.endFields[i].getTime();
      if (infusionRate.rate && start && end)
      {
        var durationInMinutes = this._getDurationInMinutes(start, end);
        usedVolume += durationInMinutes * infusionRate.rate / 60;
        usedTimeInMinutes += durationInMinutes;
        if (usedVolume >= this.volumeSum)
        {
          this.warningLabel.setText(this.view.getDictionary('therapy.interval.too.long.for.volume'));
          this._setLastEndLabelValue(null);
          return;
        }
      }
      else
      {
        this._setLastEndLabelValue(null);
        return;
      }
    }
    var firstRowStart = this.startFields[0].getTime();
    var lastRowInfusionRate = this.ratePanes[this.ratePanes.length - 1].getInfusionRate();
    var lastRowStart = this.startFields[this.ratePanes.length - 1].getTime();

    if (!lastRowInfusionRate.rate || !lastRowStart)
    {
      this._setLastEndLabelValue(null);
      return;
    }

    var remainingTimeInMinutes = (this.volumeSum - usedVolume) / (lastRowInfusionRate.rate / 60);
    var lastRowEnd = new Date(
        firstRowStart.getFullYear(),
        firstRowStart.getMonth(),
        firstRowStart.getDate(),
        firstRowStart.getHours(),
        firstRowStart.getMinutes() + usedTimeInMinutes + Number(remainingTimeInMinutes),
        0,
        0);

    this._setLastEndLabelValue(lastRowEnd);
  },

  _setupValidation: function()
  {
    this.validationForm.reset();

    for (var i = 0; i < this.rows.length; i++)
    {
      var ratePane = this.ratePanes[i];
      this._addValidations(ratePane.getInfusionRatePaneValidations());

      var startField = this.startFields[i];
      var startFieldValidation = new tm.jquery.FormField({
        component: startField,
        required: true,
        componentValueImplementationFn: function(component)
        {
          return component.getTime();
        }
      });
      this._addValidations([startFieldValidation]);

      var endField = this.endFields[i];
      if (!endField.isLabel)
      {
        var endFieldValidation = new tm.jquery.FormField({
          component: endField,
          required: true,
          componentValueImplementationFn: function(component)
          {
            return component.getTime();
          }
        });
      }
      this._addValidations([endFieldValidation]);
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
    for (var i = 0; i < this.rows.length; i++)
    {
      var start = this.startFields[i].getTime();
      var end = this.endFields[i].isLabel ? this.endFields[i].labelDate : this.endFields[i].getTime();
      var rateValues = this.ratePanes[i].getInfusionRate();
      var duration = end ? this._getDurationInMinutes(start, end) : null;

      timedDoseElements.push({
        doseElement: {
          duration: duration,
          rate: rateValues.rate,
          rateUnit: rateValues.rateUnit,
          rateFormula: rateValues.rateFormula,
          rateFormulaUnit: rateValues.rateFormulaUnit
        },
        doseTime: {
          hour: start.getHours(),
          minute: start.getMinutes()
        }
      });
    }
    this.resultCallback(new app.views.common.AppResultData({success: true, value: timedDoseElements}));
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    if (this.warningLabel.getText())
    {
      resultDataCallback(new app.views.common.AppResultData({success: false}));
    }
    else
    {
      this.resultCallback = resultDataCallback;
      this._setupValidation();
      this.validationForm.submit();
    }
  }
});

