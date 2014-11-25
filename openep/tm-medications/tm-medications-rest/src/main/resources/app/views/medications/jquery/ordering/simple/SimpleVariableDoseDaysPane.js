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

Class.define('app.views.medications.ordering.SimpleVariableDoseDaysPane', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'variable-dose-pane',
  scrollable: 'both',
  /** configs */
  view: null,
  medicationData: null,
  timedDoseElements: null,
  frequency: null,
  /** privates*/
  validationForm: null,
  initialState: null,
  rows: null,
  timeFields: null,
  dosePanes: null,
  dateLabels: null,
  dayLabels: null,
  resultDataCallback: null,
  /** privates: components */
  dosingFrequencyPane: null,
  unitLabel: null,
  rowsContainer: null,
  daysContainer: null,
  datesContainer: null,
  dateField: null,
  addTimeButton: null,
  removeTimeButton: null,
  addDayButton: null,
  removeDayButton: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.initialState = true;
    this.rows = [];
    this.timeFields = [];
    this.dosePanes = [];
    this.dateLabels = [];
    this.dayLabels = [];

    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"));
    this._buildComponents();
    this._buildGui();

    this.dosingFrequencyPane.setFrequency(this.frequency);

    var self = this;
    if (this.timedDoseElements == null || this.timedDoseElements.length == 0)
    {
      this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
      {
        setTimeout(function()
        {
          self.dosingFrequencyPane.requestFocus();
          self._addRow(null, null, false);
          self._addRow(null, null, false);
          self._addRow(null, null, false);
          self._addColumn();
          self._addColumn();
          self._addColumn();
          self._addColumn(true);
        }, 500);
      });
    }
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
    this.rowsContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5),
      padding: "0 10 0 0",
      scrollable: 'visible'
    });
    this.daysContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 5),
      padding: "0 0 0 81",
      scrollable: 'visible'
    });
    var firstDayLabel = this._createDayLabel(1);
    this.dayLabels.push(firstDayLabel);
    this.daysContainer.add(firstDayLabel);

    this.datesContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 5),
      padding: "0 0 0 59",
      scrollable: 'visible'
    });
    this.dateField = new tm.jquery.DatePicker({date: new Date(), showType: "focus", width: 90});
    this.dateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._fixDates();
    });

    var numeratorUnit = tm.views.medications.MedicationUtils.getStrengthNumeratorUnit(this.medicationData);

    this.unitLabel = tm.views.medications.MedicationUtils.crateLabel('TextDataBold', numeratorUnit, "0 0 0 15");

    this.addTimeButton = new tm.jquery.Container({cls: 'add-icon', width: 30, height: 30});
    this.addTimeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._addRow(null, null, true);
    });
    this.removeTimeButton = new tm.jquery.Container({cls: 'remove-icon', width: 30, height: 30});
    this.removeTimeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._removeLastRow();
    });

    this.addDayButton = new tm.jquery.Container({cls: 'add-icon', width: 30, height: 30});
    this.addDayButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._addColumn(true);
    });
    this.removeDayButton = new tm.jquery.Container({cls: 'remove-icon', width: 30, height: 30});
    this.removeDayButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._removeLastColumn();
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
    //var container = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "stretch"), scrollable: 'visible'});
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval')));
    this.add(this.dosingFrequencyPane);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('unit')));
    this.add(this.unitLabel);
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dose')));
    this.add(this.daysContainer);

    this.datesContainer.add(this.dateField);
    var datesAndButtonsContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5}), scrollable: 'visible'});
    datesAndButtonsContainer.add(this.datesContainer);
    datesAndButtonsContainer.add(this.addDayButton);
    datesAndButtonsContainer.add(this.removeDayButton);

    this.add(datesAndButtonsContainer);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 5}));
    this.add(this.rowsContainer);

    var buttonsContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    buttonsContainer.add(this.addTimeButton);
    buttonsContainer.add(this.removeTimeButton);
    buttonsContainer.add(new tm.jquery.Spacer({type: 'horizontal', size: 5, flex: 1, style: "display: block;"}));
    this.add(buttonsContainer);
  },

  _addRow: function(doseTime, numerator, repaint)
  {
    var self = this;
    var rowIndex = this.dosePanes.length;
    this.dosePanes[rowIndex] = [];

    if (doseTime)
    {
      var time = new Date();
      time.setHours(doseTime.hour);
      time.setMinutes(doseTime.minute);
    }

    var timePicker = new tm.jquery.TimePicker({time: time ? time : null});
    this.timeFields.push(timePicker);

    var row = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("start", "start", 5),
      scrollable: 'visible'
    });
    row.add(timePicker);

    var numberOfColumns = this.dayLabels.length;
    for (var i = 0; i < (numberOfColumns > 0 ? numberOfColumns : 1); i++)
    {
      this._addDosePane(row, rowIndex, numerator);
    }
    this.rows.push(row);
    this.rowsContainer.add(row);

    if (repaint)
    {
      this.rowsContainer.repaint();
    }
  },

  _removeLastRow: function()
  {
    if (this.rows.length > 1)
    {
      var lastRow = this.rows[this.rows.length - 1];
      this.rowsContainer.remove(lastRow);
      this.rows.pop();
      this.dosePanes.pop();
      this.timeFields.pop();
      this.rowsContainer.repaint();
    }
  },

  _addColumn: function(repaint)
  {
    var dayIndex = this.dayLabels.length;
    var dayLabel = this._createDayLabel(dayIndex);
    this.dayLabels.push(dayLabel);
    this.daysContainer.add(dayLabel);

    var date = new Date(this.dateField.getDate());
    date.setDate(date.getDate() + dayIndex);
    var dateLabel = this._createDateLabel(date);
    this.dateLabels.push(dateLabel);
    this.datesContainer.add(dateLabel);

    var numberOfRows = this.rows.length;
    for (var i = 0; i < (numberOfRows > 0 ? numberOfRows : 1) ; i++)
    {
      this._addDosePane(this.rows[i], i, null);
    }
    if (repaint)
    {
      this.daysContainer.repaint();
      this.datesContainer.repaint();
      this.rowsContainer.repaint();

      var self = this;
      self.scrollTo(10000000, 0, 1000);
    }
  },

  _removeLastColumn: function()
  {
    var lastColumnIndex = (this.dayLabels.length - 1);
    if (lastColumnIndex > 0)
    {
      for (var i = 0; i < this.rows.length; i++)
      {
        var lastDosePane = this.dosePanes[i][lastColumnIndex];
        this.rows[i].remove(lastDosePane);
        this.dosePanes[i].pop();
      }

      var lastDayLabel = this.dayLabels[ lastColumnIndex];
      this.daysContainer.remove(lastDayLabel);
      this.dayLabels.pop();

      var lastDateLabel = this.dateLabels[this.dateLabels.length - 1];
      this.datesContainer.remove(lastDateLabel);
      this.dateLabels.pop();

      this.daysContainer.repaint();
      this.datesContainer.repaint();
      this.rowsContainer.repaint();
    }
  },

  _addDosePane: function(rowContainer, rowIndex, numerator)
  {
    var dosePane = this._buildDosePane(numerator);
    this.dosePanes[rowIndex].push(dosePane);
    rowContainer.add(dosePane);
  },

  _fixDates: function()
  {
    var firstDate = this.dateField.getDate();
    for (var i = 0; i < this.dateLabels.length; i++)
    {
      var date = new Date(firstDate);
      date.setDate(date.getDate() + i + 1);
      var dateDisplay = this._getDateShortDisplay(date);
      this.dateLabels[i].setText(dateDisplay);
      this.dateLabels[i].date = date;
    }
  },

  _createDayLabel: function(day)
  {
    return new tm.jquery.Label({cls: 'TextLabel', text: "Day " + day, width: 68, padding: "5 0 0 0", style: 'display:block; text-align:center;'});
  },

  _createDateLabel: function(date)
  {
    var dateDisplay = this._getDateShortDisplay(date);
    return new tm.jquery.Label({cls: 'TextData', text: dateDisplay, width: 68, padding: "6 0 0 0", style: 'display:block; text-align:center;'});
  },

  _getDateShortDisplay: function(date)
  {
    var dateDisplay = Globalize.format(date, 'L');
    dateDisplay = dateDisplay.substring(0, dateDisplay.length - 5);
    return dateDisplay;
  },

  _buildDosePane: function(numerator)
  {
    var self = this;

    return new app.views.medications.ordering.DosePane({
      view: this.view,
      medicationData: this.medicationData,
      doseNumerator: numerator,
      hideDenominator: true,
      hideUnit: true
    });
  },

  _presentData: function()
  {
    if (this.timedDoseElements.length > 0)
    {
      var firstElement = this.timedDoseElements[0];
      var firstDate = new Date(firstElement.date);
      this._addRow(firstElement.doseTime, firstElement.doseElement ? firstElement.doseElement.quantity : null);

      for (var i = 1; i < this.timedDoseElements.length; i++)
      {
        if ((new Date(this.timedDoseElements[i].date)).getTime() == firstDate.getTime())
        {
          this._addRow(this.timedDoseElements[i].doseTime,
              this.timedDoseElements[i].doseElement ? this.timedDoseElements[i].doseElement.quantity : null);
        }
      }

      var dates = []; //dates in millis
      var times = [];
      for (var j = 0; j < this.timedDoseElements.length; j++)
      {
        var dateInMillis = (new Date(this.timedDoseElements[j].date)).getTime();
        if (!dates.contains(dateInMillis))
        {
          dates.push(dateInMillis);
        }
        var time = this.timedDoseElements[j].doseTime;
        if (this._getIndexOfHourMinute(time, times) == -1)
        {
          times.push(time);
        }
      }

      for (var k = 1; k < dates.length; k++)
      {
        this._addColumn(false);
      }

      for (var m = 0; m < this.timedDoseElements.length; m++)
      {
        var timedDoseElement = this.timedDoseElements[m];

        var row = this._getIndexOfHourMinute(timedDoseElement.doseTime, times);
        var column = dates.indexOf((new Date(timedDoseElement.date)).getTime());
        this.dosePanes[row][column].setDoseNumerator(
            timedDoseElement.doseElement ? timedDoseElement.doseElement.quantity : null);
      }

      this.dateField.setDate(firstDate);
      this._fixDates();
      this.rowsContainer.repaint();
    }
  },

  _getIndexOfHourMinute: function(hourMinute, list)
  {
    for (var i = 0; i < list.length; i++)
    {
      if (list[i].hour == hourMinute.hour && list[i].minute == hourMinute.minute)
      {
        return i;
      }
    }
    return -1
  },

  _rebuildRows: function()
  {
    var enums = app.views.medications.TherapyEnums;
    this.timeFields.removeAll();
    this.dosePanes.removeAll();
    this.rows.removeAll();
    this.rowsContainer.removeAll(true);
    var frequencyKey = this.dosingFrequencyPane.getFrequencyKey();
    var administrationTimes =
        tm.views.medications.MedicationTimingUtils.getFrequencyAdministrationTimesOfDay(
            this.view.getAdministrationTiming(), frequencyKey);
    if (administrationTimes.length > 0)
    {
      for (var i = 0; i < administrationTimes.length; i++)
      {
        this._addRow(administrationTimes[i]);
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
              this._addRow();
            }
          }
        }
        else if (frequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
        {
          if (frequency.value)
          {
            for (var k = 0; k < 24 / frequency.value; k++)
            {
              this._addRow();
            }
          }
        }
        else
        {
          this._addRow();
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

    if (this.dosePanes.length > 0 && this.dosePanes[0].length > 0)
    {
      //this.dosePanes[0][0].requestFocusToNumerator();
    }
  },

  _setupValidation: function()
  {
    this.validationForm.reset();
    this._addValidations(this.dosingFrequencyPane.getDosingFrequencyPaneValidations());

    for (var i = 0; i < this.timeFields.length; i++)
    {
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
    this._fixDates();
    var timedDoseElements = [];
    for (var row = 0; row < this.dosePanes.length; row++)
    {
      for (var column = 0; column < this.dosePanes[row].length; column++)
      {
        var time = this.timeFields[row].getTime();
        var dose = this.dosePanes[row][column].getDose();
        timedDoseElements.push({
          doseElement: {
            quantity: dose.quantity,
            quantityDenominator: dose.quantityDenominator
          },
          doseTime: {
            hour: time.getHours(),
            minute: time.getMinutes()
          },
          date: column == 0 ? this.dateField.getDate() : this.dateLabels[column - 1].date
        });
      }
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

