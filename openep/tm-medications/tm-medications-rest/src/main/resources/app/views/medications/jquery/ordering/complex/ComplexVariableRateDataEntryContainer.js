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

Class.define('app.views.medications.ordering.ComplexVariableRateDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'variable-rate-pane',
  scrollable: 'vertical',
  /** configs */
  view: null,
  medicationData: null,
  infusionIngredients: null,
  continuousInfusion: null,
  volumeSum: null,
  timedDoseElements: null,
  recurringContinuousInfusion: null,
  showFormula: true,
  modal: null,
  /** privates: components */
  _rowsContainer: null,
  _warningLabel: null,
  _formulaUnitPane: null,
  _recurringCheckBox: null,
  _rows: null,
  _validationForm: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._rows = [];
    this.timedDoseElements = tm.jquery.Utils.isArray(this.timedDoseElements) ? this.timedDoseElements : [];

    this._buildGui();
    this._presentData();

    var self = this;
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self._rows[0].setFocusToStartField();
      }, 0);
    });
  },

  /** private methods */
  _buildGui: function()
  {
    var self = this;

    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'flex-start', 0));

    this._formulaUnitPane = new app.views.medications.ordering.InfusionRateFormulaUnitPane({
      view: this.view,
      dropdownAppendTo: this.getView().getAppFactory().getDefaultRenderToElement(),
      formulaUnitChangeEvent: function()
      {
        var formulaUnit = self._formulaUnitPane.getRateFormulaUnit();
        self._rows.forEach(function applyDisplayUnit(row)
        {
          row.setFormulaUnitToLabel(formulaUnit.displayUnit);
        });
      }
    });

    if (this.isContinuousInfusion())
    {
      this._recurringCheckBox = new tm.jquery.CheckBox({
        cls: "recurring-checkbox",
        labelText: this.view.getDictionary("repeat.every.24h"),
        checked: this.recurringContinuousInfusion
      });
      this._recurringCheckBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        self._calculateEnd();
      });
    }

    this._rowsContainer = new tm.jquery.Container({
      cls: "rate-rows-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: "visible"
    });

    var addButton = new tm.jquery.Container({cls: 'add-icon add-row-button', width: 30, height: 30});
    addButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      var formulaUnit = self._formulaUnitPane.getRateFormulaUnit();
      var index = self._rows.length - 1;
      self._endLabelToField(index);
      self._addRow(null, formulaUnit ? formulaUnit.displayUnit : null, true);
      self._calculateEnd();
      self._rowsContainer.repaint();
    });
    var removeButton = new tm.jquery.Container({cls: 'remove-icon remove-row-button', width: 30, height: 30});
    removeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._removeLastField();
    });

    this._warningLabel = new tm.jquery.Label({cls: "waring-label", emphasis: "error"});

    this._validationForm = new tm.jquery.Form({
      requiredFieldValidatorErrorMessage: self.view.getDictionary("field.value.is.required")
    });

    if (this.isShowFormula())
    {
      this.add(this._formulaUnitPane);
    }
    if (this.isContinuousInfusion())
    {
      this.add(this._recurringCheckBox);
    }
    this.add(this._rowsContainer);

    var buttonsContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0)
    });
    this.add(buttonsContainer);
    buttonsContainer.add(addButton);
    buttonsContainer.add(removeButton);
    this.add(this._warningLabel);
  },

  _addRow: function(timedDoseElement, formulaUnitDisplay, endAsLabel)
  {
    var self = this;
    var rowContainer = new app.views.medications.ordering.ComplexVariableRateRowContainer({
      formulaUnitDisplay: formulaUnitDisplay,
      timedDoseElement: timedDoseElement,
      endAsLabel: endAsLabel,
      continuousInfusion: this.isContinuousInfusion(),
      view: this.getView(),
      infusionIngredients: this.getInfusionIngredients(),
      volumeSum: this.getVolumeSum(),
      medicationData: this.getMedicationData(),
      showFormula: this.isShowFormula()
    });
    rowContainer.on(
        app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_START_TIME_CHANGE,
        function()
        {
          self._calculateEnd();
        });
    rowContainer.on(
        app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_END_TIME_CHANGE,
        function(component)
        {
          var index = self._rows.indexOf(component);
          var nextRowContainer = self._rows[index + 1];
          if (nextRowContainer)
          {
            nextRowContainer.setStartTime(component.getEndTime(), true);
          }
          self._calculateEnd();
        });
    rowContainer.on(
        app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_RATE_CHANGE,
        function()
        {
          self._calculateEnd();
        });
    rowContainer.on(
        app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_RATE_FIELD_FOCUS_LOST,
        function(component)
        {
          var index = self._rows.indexOf(component);
          if (component.getInfusionRate().rate)
          {
            var nextRowContainer = self._rows[index + 1];
            if (nextRowContainer)
            {
              nextRowContainer.requestFocusToRate();
            }
          }
          else
          {
            component.requestFocusToFormula();
          }
        });
    rowContainer.on(
        app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_FORMULA_FIELD_FOCUS_LOST,
        function(component)
        {
          var index = self._rows.indexOf(component);
          var nextRow = self._rows[index + 1];
          if (nextRow)
          {
            if (nextRow.getInfusionRate().rateFormula)
            {
              nextRow.requestFocusToFormula();
            }
            else
            {
              nextRow.requestFocusToRate();
            }
          }
          else
          {
            component.requestFocusToFormula();
          }
        });
    this._rows.push(rowContainer);
    this._rowsContainer.add(rowContainer);
  },

  /**
   * @param {Number} index
   * @private
   */
  _endLabelToField: function(index)
  {
    var endRow = this._rows[index];
    if (endRow)
    {
      endRow.setEndAsLabel(false);
    }
  },

  /**
   * @param {Date} endDate
   * @param {Boolean} [isRecurringChecked=false]
   * @private
   */
  _setLastEndAsLabel: function(endDate, isRecurringChecked)
  {
    var lastRow = this._rows[this._rows.length - 1];
    lastRow.setEndTime(endDate, true);
    lastRow.setEndAsLabel(true, isRecurringChecked);
  },

  _removeLastField: function()
  {
    var lastRowIndex = this._rows.length - 1;
    if (lastRowIndex > 1)
    {
      var lastRow = this._rows[lastRowIndex];
      this._rowsContainer.remove(lastRow);
      this._rows.pop();
      this._calculateEnd();
    }
  },

  _presentData: function()
  {
    this._formulaUnitPane.setMedicationData(this.getMedicationData(), this.isContinuousInfusion());
    if (this.getTimedDoseElements().length > 0)
    {
      var formulaUnitDisplay = this.getTimedDoseElements()[0].doseElement.rateFormulaUnit;
      if (formulaUnitDisplay)
      {
        this._formulaUnitPane.setFormulaUnit(formulaUnitDisplay);
      }

      for (var i = 0; i < this.getTimedDoseElements().length; i++)
      {
        this._addRow(this.getTimedDoseElements()[i], formulaUnitDisplay, i == this.getTimedDoseElements().length - 1);
      }
      this._calculateEnd();
    }
    else
    {
      var defaultFormulaUnit = this._formulaUnitPane.getDefaultValue();
      this._addRow(null, defaultFormulaUnit.displayUnit, false);
      this._addRow(null, defaultFormulaUnit.displayUnit, true);
    }
  },

  /**
   * Change with caution. The desired behavior od the whole component is described in task TMC-12827.
   * @param {Date} start
   * @param {Date} end
   * @returns {number}
   * @private
   */
  _getDurationInMinutes: function(start, end)
  {
    var firstRowStartFormatted = moment(this._rows[0].getStartTime()).set({'seconds': 0, 'millisecond': 0});
    var startFormatted = moment(start).set({'seconds': 0, 'millisecond': 0});
    var endFormatted = moment(end).set({'seconds': 0, 'millisecond': 0});

    if (startFormatted.isSameOrBefore(firstRowStartFormatted))
    {
      startFormatted.add(1, 'days');
    }
    if (endFormatted.isSameOrBefore(firstRowStartFormatted))
    {
      endFormatted.add(1, 'days');
    }
    if (endFormatted.isAfter(startFormatted))
    {
      return endFormatted.diff((startFormatted), 'minutes');
    }
    else
    {
      return endFormatted.add(1, 'days').diff(startFormatted, 'minutes');
    }
  },

  _calculateEnd: function()
  {
    this._warningLabel.setText('');
    if (this.isContinuousInfusion())
    {
      if (this._recurringCheckBox.isChecked())
      {
        this._setLastEndAsLabel(this._rows[0].getStartTime(), true);
      }
      else
      {
        this._setLastEndAsLabel(null);
      }
      return;
    }
    if (!this.getVolumeSum())
    {
      this._setLastEndAsLabel(null);
      return;
    }

    var usedVolume = 0;
    var usedTimeInMinutes = 0;
    for (var i = 0; i < this._rows.length - 1; i++)
    {
      var infusionRate = this._rows[i].getInfusionRate();
      var start = this._rows[i].getStartTime();
      var end = this._rows[i].getEndTime();
      if (infusionRate.rate && start && end)
      {
        var durationInMinutes = this._getDurationInMinutes(start, end);
        usedVolume += durationInMinutes * infusionRate.rate / 60;
        usedTimeInMinutes += durationInMinutes;
        if (usedVolume > this.getVolumeSum())
        {
          this._warningLabel.setText(this.view.getDictionary('therapy.interval.too.long.for.volume'));
          this._setLastEndAsLabel(null);
          return;
        }
      }
      else
      {
        this._setLastEndAsLabel(null);
        return;
      }
    }
    var firstRowStart = this._rows[0].getStartTime();
    var lastRowInfusionRate = this._rows[this._rows.length - 1].getInfusionRate();
    var lastRowStart = this._rows[this._rows.length - 1].getStartTime();

    if (!lastRowInfusionRate.rate || !lastRowStart)
    {
      this._setLastEndAsLabel(null);
      return;
    }

    var remainingTimeInMinutes = (this.getVolumeSum() - usedVolume) / (lastRowInfusionRate.rate / 60);
    var lastRowEnd = new Date(
        firstRowStart.getFullYear(),
        firstRowStart.getMonth(),
        firstRowStart.getDate(),
        firstRowStart.getHours(),
        firstRowStart.getMinutes() + usedTimeInMinutes + Number(remainingTimeInMinutes),
        0,
        0);

    this._setLastEndAsLabel(lastRowEnd);
  },

  _setupValidation: function()
  {
    this._validationForm.reset();

    this._rows.forEach(function addRowValidators(row)
    {
      this._addValidations(row.getFormValidations());
    }, this);
  },

  /**
   * @param {Array<tm.jquery.FormField>} validations
   * @private
   */
  _addValidations: function(validations)
  {
    for (var i = 0; i < validations.length; i++)
    {
      this._validationForm.addFormField(validations[i]);
    }
  },

  /**
   * @returns {Array<Object>}
   * @private
   */
  _buildTimeDoseElementsFromRows: function()
  {
    var timedDoseElements = [];
    for (var i = 0; i < this._rows.length; i++)
    {
      var start = this._rows[i].getStartTime();
      var end = this._rows[i].getEndTime();
      var rateValues = this._rows[i].getInfusionRate();
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
    return timedDoseElements;
  },

  /**
   * This method is a validator for recurring continuous infusion which validates that duration is equal to 24 hours.
   * @param {Object[]} timedDoseElements
   * @returns {Boolean}
   * @private
   */
  _validateRecurring24: function(timedDoseElements)
  {
    if (this.isContinuousInfusion() && this._recurringCheckBox.isChecked())
    {
      var durationInMinutes = 0;
      for (var i = 0; i < timedDoseElements.length; i++)
      {
        durationInMinutes += timedDoseElements[i].doseElement.duration;
      }
      if (durationInMinutes != 24 * 60) //more than 24 h
      {
        this.getView().getAppFactory().createWarningSystemDialog(
            this.getView().getDictionary('recurring.infusion.duration.not.24h'),
            250,
            140
        ).show();
        return false;
      }
    }
    return true
  },

  /**
   * Recurring continuous infusion has its own validation.
   * This method is a validator for continuous and infusion
   * with duration which validates that the duration is of maximum 23 hours and 59 minutes.
   * @param {Object[]} timedDoseElements
   * @returns {Boolean}
   * @private
   */
  _validateMaxIntervalDuration: function(timedDoseElements)
  {
    if (!(this.isContinuousInfusion() && this._recurringCheckBox.isChecked()))
    {
      var durationInMinutes = 0;
      for (var i = 0; i < timedDoseElements.length; i++)
      {
        durationInMinutes += timedDoseElements[i].doseElement.duration;
      }
      if (durationInMinutes >= 24 * 60)
      {
        this.getView().getAppFactory().createWarningSystemDialog(
            this.getView().getDictionary('infusion.maximum.duration.less.than'),
            250,
            140
        ).show();
        return false;
      }
    }
    return true;
  },

  /**
   * This method validates that the infusion interval is not broken. Start value has to be the same as the previous end.
   * @returns {Boolean}
   * @private
   */
  _validateContinuousInterval: function()
  {
    for (var i = 0; i < this._rows.length - 1; i++)
    {
      var nextStartTime = this._rows[i + 1].getStartTime();
      var nextStartHoursMinutes = nextStartTime.getHours() + nextStartTime.getMinutes();
      var endTime = this._rows[i].getEndTime();
      var endHoursMinutes = endTime.getHours() + endTime.getMinutes();

      if (nextStartHoursMinutes !== endHoursMinutes)
      {
        this.getView().getAppFactory().createWarningSystemDialog(
            this.getView().getDictionary('therapy.has.to.have.continuous.interval'),
            300,
            140
        ).show();
        return false;
      }
    }
    return true;
  },

  /**
   * @returns {boolean}
   */
  isContinuousInfusion: function()
  {
    return this.continuousInfusion === true;
  },

  /**
   * @returns {boolean}
   */
  isShowFormula: function()
  {
    return this.showFormula === true;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @returns {Array<Object>}
   */
  getTimedDoseElements: function()
  {
    return this.timedDoseElements;
  },

  /**
   * @returns {Array<*>}
   */
  getInfusionIngredients: function()
  {
    return this.infusionIngredients;
  },

  /**
   * @returns {Number}
   */
  getVolumeSum: function()
  {
    return this.volumeSum;
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
    var self = this;
    var failureResult = new app.views.common.AppResultData({success: false});
    if (this._warningLabel.getText())
    {
      resultDataCallback(failureResult);
    }
    else
    {
      this._setupValidation();
      this._validationForm.setOnValidationSuccess(
          function onSuccess()
          {
            var timedDoseElements = self._buildTimeDoseElementsFromRows();

            if (self._validateContinuousInterval() && self._validateRecurring24(timedDoseElements) &&
                self._validateMaxIntervalDuration(timedDoseElements))
            {
              resultDataCallback(new app.views.common.AppResultData({
                success: true,
                timedDoseElements: timedDoseElements,
                recurring: self.isContinuousInfusion() && self._recurringCheckBox.isChecked()
              }));
            }
            else
            {
              resultDataCallback(failureResult);
            }
          });
      this._validationForm.setOnValidationError(
          function onFailure()
          {
            resultDataCallback(failureResult);
          });
      this._validationForm.submit();
    }
  }
});