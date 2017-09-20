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
  cls: 'variable-dose-days-pane',
  scrollable: 'both',
  editMode: false,
  /** configs */
  view: null,
  medicationData: null,
  timedDoseElements: null,
  frequency: null,
  addDosageCalculationPane: false,
  dosageCalculationUnits: null,
  untilCanceled: false,
  selectedDate: null,
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
  referenceDosePane: null,
  fillDosesButton: null,
  unitLabel: null,
  rowsContainer: null,
  daysContainer: null,
  datesContainer: null,
  dateField: null,
  addTimeButton: null,
  removeTimeButton: null,
  addDayButton: null,
  removeDayButton: null,
  _therapyNextAdministrationLabelPane: null,
  _repeatInfoLabel: null,
  _repeatOptionsButton: null,
  _lastDoseContinuedContainer: null,
  _threeDotsContainers: null,
  _lateAdministrationInfoLabel: null,

  _prepareFieldsConditionTask: null,
  _testRenderCoordinator: null,

  visibilityContext: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var enums = app.views.medications.TherapyEnums;
    this.initialState = true;
    this.rows = [];
    this.timeFields = [];
    this.dosePanes = [];
    this.dateLabels = [];
    this.dayLabels = [];
    this._threeDotsContainers = [];
    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'simple-variable-dose-days-pane-coordinator',
      view: this.getView(),
      component: this,
      manualMode: true /* due to repaints issued after the render process */
    });

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"));
    this._buildComponents();
    this._buildGui();

    this.dosingFrequencyPane.setFrequency(this.frequency, false);

    var self = this;
    if (this.timedDoseElements == null || this.timedDoseElements.length == 0)
    {
      this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
      {

        if (self.dosingFrequencyPane.getFrequencyKey() == null)
        {
          self.dosingFrequencyPane.setFrequency(self._getDosingFrequencyFromMode(3), false);
          self.dosingFrequencyPane.requestFocus();
        }
        self._addColumn();
        self._addColumn();
        self._addColumn();
        self._addColumn(true);
        setTimeout(function yieldToPaint()
        {
          self._testRenderCoordinator.insertCoordinator();
        }, 0);
      });
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var view = this.getView();
    this.dosingFrequencyPane = new app.views.medications.ordering.DosingFrequencyPane({
      view: view,
      withSingleFrequencies: false,
      allowMoreThan24hFrequency: false,
      frequencyChangeEvent: function()
      {
        self._handleDosingFrequencyChange();
      },
      onModeChangeEvent: function()
      {
        self.dosingFrequencyPane.setFrequency(self._getDosingFrequencyFromMode(self.timeFields.length), true);
        self._setEnabledTimeFields();
      }
    });
    this.referenceDosePane = new app.views.medications.ordering.DosePane({
      view: view,
      medicationData: this.medicationData,
      addDosageCalculationPane: true,
      addDosageCalcBtn: true,
      showDoseUnitCombos: false
    });
    this.fillDosesButton = new tm.jquery.Button({
      cls: "fill-doses-button",
      text: view.getDictionary('fill'),
      type: "link",
      padding: "0 0 0 15",
      handler: function()
      {
        var dose = self.referenceDosePane.getDose();
        if (dose && dose.quantity)
        {
          self._fillEmptyDoses(dose.quantity);
        }
      }
    });
    this._repeatInfoLabel = new tm.jquery.Container({
      cls: "TextData",
      hidden: true
    });

    this.rowsContainer = new tm.jquery.Container({
      cls: "rows-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 5),
      scrollable: 'visible'
    });
    this.daysContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 5),
      padding: "0 0 0 81",
      scrollable: 'visible'
    });
    var firstDayLabel = this._createDayLabel(1);
    this.dayLabels.push(firstDayLabel);
    this.daysContainer.add(firstDayLabel);

    this.datesContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 5),
      padding: "0 0 0 59",
      scrollable: 'visible'
    });
    this.dateField = new tm.jquery.DatePicker({
      cls: "date-field",
      date: CurrentTime.get(),
      showType: "focus",
      width: 95,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this.dateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._fixDates();
    });

    this.unitLabel = tm.views.medications.MedicationUtils.crateLabel(
        'TextDataBold unit-label', 
        this.medicationData.getStrengthNumeratorUnit(), 
        "0 0 0 15");

    this.addTimeButton = new tm.jquery.Container({cls: 'add-icon add-time-button', width: 30, height: 30});
    this.addTimeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._addRow(null, null, true);
    });
    this.removeTimeButton = new tm.jquery.Container({cls: 'remove-icon remove-time-button', width: 30, height: 30});
    this.removeTimeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._removeLastRow(true);
    });

    this.addDayButton = new tm.jquery.Container({cls: 'add-icon add-day-button', width: 30, height: 30});
    this.addDayButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._addColumn(true);
    });
    this._repeatOptionsButton = new tm.jquery.Button({
      cls: "repeat-options-button",
      text: view.getDictionary("options") + "...",
      type: "link",
      handler: function()
      {
        self._showRepeatProtocolContainer();
      }
    });

    this.removeDayButton = new tm.jquery.Container({cls: 'remove-icon remove-day-button', width: 30, height: 30});
    this.removeDayButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._removeLastColumn();
    });

    this._therapyNextAdministrationLabelPane = new app.views.medications.ordering.TherapyNextAdministrationLabelPane({
      view: view
    });

    this._lateAdministrationInfoLabel = new tm.jquery.Container({
      cls: 'TextData late-administration',
      html: view.getDictionary('missed.administration.events.highlighted'),
      hidden: true
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
    var view = this.getView();
    var firstRowTitleContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5}), scrollable: 'visible'});
    firstRowTitleContainer.add(new tm.jquery.Container({cls: 'TextLabel', html: view.getDictionary('dosing.interval'), width: 368, padding: "5 0 0 0"}));
    firstRowTitleContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary('fill.empty.cells'), "5 0 0 0"));
    this.add(firstRowTitleContainer);
    var firstRowContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5}), scrollable: 'visible'});
    firstRowContainer.add(this.dosingFrequencyPane);
    var doseRowContainer = new tm.jquery.Container({
      cls: "reference-dose-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      scrollable: "visible"
    });
    doseRowContainer.add(this.referenceDosePane);
    doseRowContainer.add(this.fillDosesButton);
    firstRowContainer.add(doseRowContainer);
    this.add(firstRowContainer);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary('unit')));
    this.add(this.unitLabel);
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary('dose')));
    this.add(this.daysContainer);

    this.datesContainer.add(this.dateField);
    var datesAndButtonsContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5}), scrollable: 'visible'});
    datesAndButtonsContainer.add(this.datesContainer);
    datesAndButtonsContainer.add(this.addDayButton);
    datesAndButtonsContainer.add(this.removeDayButton);
    datesAndButtonsContainer.add(this._repeatOptionsButton);

    this.add(datesAndButtonsContainer);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 5}));
    var rowsAndContinuedContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      scrollable: 'visible'
    });
    this._lastDoseContinuedContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-end", "flex-end"),
      hidden: !this.untilCanceled && tm.jquery.Utils.isEmpty(this.selectedDate)
    });

    rowsAndContinuedContainer.add(this.rowsContainer);
    rowsAndContinuedContainer.add(this._lastDoseContinuedContainer);
    this.add(rowsAndContinuedContainer);
    var buttonsContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    buttonsContainer.add(this.addTimeButton);
    buttonsContainer.add(this.removeTimeButton);
    buttonsContainer.add(new tm.jquery.Spacer({
      type: 'horizontal',
      size: 5,
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      style: "display: block;"
    }));
    this.add(buttonsContainer);
    this.add(this._repeatInfoLabel);
    this.add(this._therapyNextAdministrationLabelPane);
    this.add(this._lateAdministrationInfoLabel);
  },

  _showProtocolRepeatLabel: function(date)
  {
    var view = this.getView();

    if (!tm.jquery.Utils.isEmpty(date))
    {
      this._repeatInfoLabel.setHtml(view.getDictionary("protocol.repeat.until") + " " + view.getDisplayableValue(date, "date.short"));
    }
    else
    {
      this._repeatInfoLabel.setHtml(view.getDictionary("protocol.repeat.until.canceled"));
    }
    this._repeatInfoLabel.isRendered() ? this._repeatInfoLabel.show() : this._repeatInfoLabel.setHidden(false);
  },

  _hideRepeatProtocolLabel: function()
  {
    this._repeatInfoLabel.isRendered() ? this._repeatInfoLabel.hide() : this._repeatInfoLabel.setHidden(true);
  },

  _addThreeDotsContainer: function()
  {
    var view = this.getView();

    var threeDots = new tm.jquery.Container({
      cls: "three-dots-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "flex-end"),
      html: "..."
    });
    threeDots.setTooltip(tm.views.medications.MedicationUtils.createTooltip(view.getDictionary("last.dose.repeating"), "bottom", view));
    this._threeDotsContainers.push(threeDots);
    this._lastDoseContinuedContainer.add(threeDots);
  },

  _removeThreeDotsContainer: function()
  {
    var threeDots = this._threeDotsContainers[this._threeDotsContainers.length - 1];
    this._lastDoseContinuedContainer.remove(threeDots);
    this._threeDotsContainers.pop();
  },

  _showRepeatProtocolContainer: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var protocolOptionsContainer = new app.views.medications.ordering.ProtocolOptionsContainer({
      view: view,
      untilCanceled: this.untilCanceled,
      selectedDate: this.selectedDate,
      startDate: this.dateField.getDate(),
      protocolDays: this.dayLabels.length
    });
    var protocolOptionsDialog = appFactory.createDataEntryDialog(
        view.getDictionary('repeat.protocol'),
        null,
        protocolOptionsContainer,
        function(resultData)
        {
          if (resultData)
          {
            if (resultData.value.untilCanceled)
            {
              self.selectedDate = null;
              self.untilCanceled = resultData.value.untilCanceled;
              self._showProtocolRepeatLabel();
              self._lastDoseContinuedContainer.show();
              self._setDosePaneBorders(true);
            }
            else if (resultData.value.selectedDate)
            {
              self.untilCanceled = false;
              self.selectedDate = resultData.value.selectedDate;
              self._showProtocolRepeatLabel(resultData.value.selectedDate);
              self._lastDoseContinuedContainer.show();
              self._setDosePaneBorders(true);
            }
            else
            {
              self.selectedDate = null;
              self.untilCanceled = false;
              self._lastDoseContinuedContainer.hide();
              self._setDosePaneBorders(false);
              self._hideRepeatProtocolLabel();
            }
            if (resultData.value.repeatWholeProtocolTimes > 1)
            {
              self._calculateAndRepeatProtocol(resultData.value.repeatWholeProtocolTimes);
            }
          }
        },
        340,
        230
    );
    protocolOptionsDialog.show();
  },

  _setDosePaneBorders: function(sharpen)
  {
    this.dosePanes.forEach(function(dosePanesRow)
    {
      var lastDosePane = dosePanesRow[dosePanesRow.length - 1];
      lastDosePane.setSharpBorders(sharpen)
    })
  },

  _calculateAndRepeatProtocol: function(times)
  {
    var columnsToAdd = this.dayLabels.length * times - this.dayLabels.length;
    var initialProtocolLength = this.dayLabels.length;
    for (var i = 0; i < columnsToAdd; i++)
    {
      this._addColumn(i === columnsToAdd - 1, initialProtocolLength, this.dayLabels.length);
    }
  },

  _addRow: function(doseTime, numerator, repaint)
  {
    var self = this;
    var rowIndex = this.dosePanes.length;
    this.dosePanes[rowIndex] = [];

    if (doseTime)
    {
      var time = CurrentTime.get();
      time.setHours(doseTime.hour);
      time.setMinutes(doseTime.minute);
    }

    var timePicker = new tm.jquery.TimePicker({
      time: time ? time : null,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    timePicker.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      var frequencyType = self.dosingFrequencyPane.getFrequencyType();
      if (!tm.jquery.Utils.isEmpty(frequencyType))
      {
        if (component.isEnabled() &&
            frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
        {
          self._recalculateTimesForBetweenDoses();
        }
      }
      self._prepareFieldsAndFixDates();
    });

    this.timeFields.push(timePicker);
    var row = new tm.jquery.Container({
      cls: "dose-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5),
      scrollable: 'visible'
    });
    row.add(timePicker);

    var numberOfColumns = this.dayLabels.length;
    for (var i = 0; i < (numberOfColumns > 0 ? numberOfColumns : 1); i++)
    {
      this._addDosePane(row, rowIndex, i, numerator);
    }
    this.rows.push(row);
    this.rowsContainer.add(row);

    this._addThreeDotsContainer();
    if (this.selectedDate || this.untilCanceled)
    {
      this._setDosePaneBorders(true);
    }
    this.dosingFrequencyPane.setFrequency(this._getDosingFrequencyFromMode(rowIndex + 1), true);
    if (repaint)
    {
      this.rowsContainer.repaint();
      this._lastDoseContinuedContainer.repaint();
    }
  },

  _removeLastRow: function(repaint)
  {
    if (this.rows.length > 1)
    {
      var lastRow = this.rows[this.rows.length - 1];
      this.rowsContainer.remove(lastRow);
      this.rows.pop();
      this.dosePanes.pop();
      this.timeFields.pop();
      this._removeThreeDotsContainer();

      this.dosingFrequencyPane.setFrequency(this._getDosingFrequencyFromMode(this.rows.length), true);
      if (repaint)
      {
        this.rowsContainer.repaint();
      }
    }
  },

  _addColumn: function(repaint, initialProtocolLength, currentColumnIndex)
  {
    this._setDosePaneBorders(false);
    var dayIndex = this.dayLabels.length;
    var dayLabel = this._createDayLabel(dayIndex + 1);
    this.dayLabels.push(dayLabel);
    this.daysContainer.add(dayLabel);

    var date = new Date(this.dateField.getDate());
    date.setDate(date.getDate() + dayIndex);
    var dateLabel = this._createDateLabel(date);
    this.dateLabels.push(dateLabel);
    this.datesContainer.add(dateLabel);

    var numberOfRows = this.rows.length;
    for (var i = 0; i < (numberOfRows > 0 ? numberOfRows : 1); i++)
    {
      var numerator = null;
      if (!tm.jquery.Utils.isEmpty(initialProtocolLength) && !tm.jquery.Utils.isEmpty(currentColumnIndex))
      {
        numerator = this.dosePanes[i][currentColumnIndex % initialProtocolLength].getDose().quantity;
      }
      this._addDosePane(this.rows[i], i, dayIndex, numerator);
    }
    if (this.selectedDate || this.untilCanceled)
    {
      this._setDosePaneBorders(true);
    }
    if (repaint)
    {
      this.daysContainer.repaint();
      this.datesContainer.repaint();
      this.rowsContainer.repaint();
      this._lastDoseContinuedContainer.repaint();
      var self = this;
      self.scrollTo(10000000, 0, 1000);
    }
  },

  _removeLastColumn: function()
  {
    var lastColumnIndex = (this.dayLabels.length - 1);
    if (lastColumnIndex > 0)
    {
      this._setDosePaneBorders(false);
      for (var i = 0; i < this.rows.length; i++)
      {
        var lastDosePane = this.dosePanes[i][lastColumnIndex];
        this.rows[i].remove(lastDosePane);
        this.dosePanes[i].pop();
      }

      var lastDayLabel = this.dayLabels[lastColumnIndex];
      this.daysContainer.remove(lastDayLabel);
      this.dayLabels.pop();

      var lastDateLabel = this.dateLabels[this.dateLabels.length - 1];
      this.datesContainer.remove(lastDateLabel);
      this.dateLabels.pop();
      if (this.selectedDate || this.untilCanceled)
      {
        this._setDosePaneBorders(true);
      }
      this.daysContainer.repaint();
      this.datesContainer.repaint();
      this.rowsContainer.repaint();
    }
  },

  _addDosePane: function(rowContainer, rowIndex, columnIndex, numerator)
  {
    var dosePane = this._buildDosePane(numerator, rowIndex, columnIndex);
    this.dosePanes[rowIndex].push(dosePane);
    rowContainer.add(dosePane);
  },

  _fixDates: function()
  {
    var firstDate = this.dateField.getDate();
    for (var i = 0; i < this.dateLabels.length; i++)
    {
      var applicationDate = new Date(firstDate);
      applicationDate.setDate(applicationDate.getDate() + i + 1);
      var dateDisplay = tm.views.medications.MedicationTimingUtils.getDateWithoutYearDisplay(this.getView(), applicationDate);
      this.dateLabels[i].setText(dateDisplay);
      this.dateLabels[i].date = applicationDate;
    }

    var now = CurrentTime.get();
    var lateAdministrationExist = false;
    for (var row = 0; row < this.dosePanes.length; row++)
    {
      for (var column = 0; column < this.dosePanes[row].length; column++)
      {
        var time = this.timeFields[row].getTime();
        var date = column == 0 ? this.dateField.getDate() : this.dateLabels[column - 1].date;

        if (!tm.jquery.Utils.isEmpty(time) && !tm.jquery.Utils.isEmpty(date))
        {
          var applicationTimestamp =
              new Date(
                  date.getFullYear(),
                  date.getMonth(),
                  date.getDate(),
                  time.getHours(),
                  time.getMinutes());
          var lateDose = false;

          if (applicationTimestamp < now)
          {
            lateDose = true;
            if (this.editMode)
            {
              this.dosePanes[row][column].setPaneEditable(false);
            }
          }
          if (lateAdministrationExist === false && lateDose === true)
          {
            lateAdministrationExist = true;
          }
          this.dosePanes[row][column].markAsLateDose(lateDose);
        }
      }
    }
    lateAdministrationExist ? this.showLateDoseInfoLabel() : this.hideLateDoseInfoLabel();
    if (this.timeFields.length > 0 && !tm.jquery.Utils.isEmpty(this.timeFields[0]))
    {
      time = this.timeFields[0].getTime();
      if (time)
      {
        var firstAdministrationTime = new Date(
            firstDate.getFullYear(),
            firstDate.getMonth(),
            firstDate.getDate(),
            time.getHours(),
            time.getMinutes());
        this._therapyNextAdministrationLabelPane.setNextAdministration(firstAdministrationTime);
        this._therapyNextAdministrationLabelPane.show();
      }
    }
  },

  showLateDoseInfoLabel: function()
  {
    this._lateAdministrationInfoLabel.isRendered() ? this._lateAdministrationInfoLabel.show() :
        this._lateAdministrationInfoLabel.setHidden(false);
  },

  hideLateDoseInfoLabel: function()
  {
    this._lateAdministrationInfoLabel.isRendered() ? this._lateAdministrationInfoLabel.hide() :
        this._lateAdministrationInfoLabel.setHidden(true);
  },

  _createDayLabel: function(day)
  {
    return new tm.jquery.Container({
      cls: 'TextLabel center',
      html: this.getView().getDictionary('day') + " " + day,
      width: 68,
      padding: "5 0 0 0"
    });
  },

  _createDateLabel: function(date)
  {
    var dateDisplay = tm.views.medications.MedicationTimingUtils.getDateWithoutYearDisplay(this.getView(), date);
    return new tm.jquery.Label({cls: 'TextData', text: dateDisplay, width: 68, padding: "6 0 0 0", style: 'display:block; text-align:center;'});
  },

  _buildDosePane: function(numerator, rowIndex, columnIndex)
  {
    var self = this;

    return new app.views.medications.ordering.DosePane({
      testAttribute: tm.jquery.Utils.formatMessage('dose-input-{0}-{1}', rowIndex, columnIndex),
      view: this.getView(),
      medicationData: this.medicationData,
      doseNumerator: numerator,
      hideDenominator: true,
      hideUnit: true,
      numeratorFocusLostEvent: function()
      {
        if (columnIndex < self.dosePanes[rowIndex].length - 1)
        {
          self.dosePanes[rowIndex][columnIndex + 1].requestFocusToNumerator();
        }
        else if (rowIndex < self.dosePanes.length - 1)
        {
          self.dosePanes[rowIndex + 1][0].requestFocusToNumerator();
        }
        else
        {
          self.dosePanes[0][0].requestFocusToNumerator();
        }
      }
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
        if (tm.views.medications.MedicationUtils.getIndexOfHourMinute(time, times) == -1)
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

        var row = tm.views.medications.MedicationUtils.getIndexOfHourMinute(timedDoseElement.doseTime, times);
        var column = dates.indexOf((new Date(timedDoseElement.date)).getTime());
        this.dosePanes[row][column].setDoseNumerator(
            timedDoseElement.doseElement ? timedDoseElement.doseElement.quantity : null, true);
        if (timedDoseElement.doseElement && timedDoseElement.doseElement.quantityDenominator)
        {
          this.dosePanes[row][column].setDoseDenominator(timedDoseElement.doseElement.quantityDenominator, true)
        }
      }

      this.dateField.setDate(firstDate);
      this._prepareFieldsAndFixDates();

      this.rowsContainer.repaint();
      if (this.untilCanceled)
      {
        this._showProtocolRepeatLabel();
      }
      else if (this.selectedDate)
      {
        this._showProtocolRepeatLabel(this.selectedDate);
      }
      var firstAdministrationTime = new Date(
          firstDate.getFullYear(),
          firstDate.getMonth(),
          firstDate.getDate(),
          firstElement.doseTime.hour,
          firstElement.doseTime.minute);
      this._therapyNextAdministrationLabelPane.setNextAdministration(firstAdministrationTime);
      this._therapyNextAdministrationLabelPane.show();
    }
  },

  _prepareFieldsAndFixDates: function()
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();
    this._abortPrepareFieldsConditionTask();
    this._prepareFieldsConditionTask = appFactory.createConditionTask(
        function()
        {
          self._fixDates();
        },
        function()
        {
          if (self.dateField.getPlugin() == null)
          {
            return false;
          }
          var allTimeFieldsReady = true;
          self.timeFields.some(function(timeField)
          {
            if (timeField.getPlugin() == null)
            {
              allTimeFieldsReady = false;
              return true;
            }
          });
          var allDosePanesReady = true;
          self.dosePanes.some(function(dosePane)
          {
            dosePane.some(function(pane)
            {
              if (!pane.isRendered(true))
              {
                allDosePanesReady = false;
                return true;
              }
            });
          });
          return allTimeFieldsReady && allDosePanesReady;
        },
        100, 500
    );
  },
  _abortPrepareFieldsConditionTask: function()
  {
    if (this._prepareFieldsConditionTask)
    {
      this._prepareFieldsConditionTask.abort();
      this._prepareFieldsConditionTask = null;
    }
  },

  _fillEmptyDoses: function(dose)
  {
    for (var i = 0; i < this.dosePanes.length; i++)
    {
      for (var j = 0; j < this.dosePanes[i].length; j++)
      {
        if (!this.dosePanes[i][j].getDose().quantity)
        {
          this.dosePanes[i][j].setDoseNumerator(dose);
        }
      }
    }
  },

  _adjustRowsToDosingFrequency: function()
  {
    var numberOfRows = this.dosePanes.length;
    var newNumberOfRows = this._getNumberOfRowsFromDosingFrequency();

    while (numberOfRows > newNumberOfRows)
    {
      this._removeLastRow(false);
      numberOfRows = this.dosePanes.length;
    }
    while (numberOfRows < newNumberOfRows)
    {
      this._addRow();
      numberOfRows = this.dosePanes.length;
    }

    this._adjustTimesToDosingFrequency();
    this.rowsContainer.repaint();
  },

  _getNumberOfRowsFromDosingFrequency: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var frequencyKey = this.dosingFrequencyPane.getFrequencyKey();
    var frequencyType = this.dosingFrequencyPane.getFrequencyType();
    var administrationTimes =
        tm.views.medications.MedicationTimingUtils.getFrequencyTimingPattern(
            this.getView().getAdministrationTiming(), frequencyKey, frequencyType);
    if (administrationTimes.length > 0)
    {
      return administrationTimes.length;
    }
    else
    {
      var frequency = this.dosingFrequencyPane.getFrequency();
      if (frequency)
      {
        if (frequency.type == enums.dosingFrequencyTypeEnum.DAILY_COUNT)
        {
          return frequency.value;
        }
        else if (frequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
        {
          return frequency.value ? 24 / frequency.value : 1;
        }
        else
        {
          return 1;
        }
      }
    }
  },

  _setEnabledTimeFields: function()
  {
    var frequencyType = this.dosingFrequencyPane.getFrequencyType();
    var enabled = frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.DAILY_COUNT;

    for (var i = 1; i < this.timeFields.length; i++)
    {
      this.timeFields[i].setEnabled(enabled);
    }
  },

  _adjustTimesToDosingFrequency: function()
  {
    var frequencyKey = this.dosingFrequencyPane.getFrequencyKey();
    var frequencyType = this.dosingFrequencyPane.getFrequencyType();
    var administrationTimes =
        tm.views.medications.MedicationTimingUtils.getFrequencyTimingPattern(
            this.getView().getAdministrationTiming(), frequencyKey, frequencyType);
    for (var i = 0; i < administrationTimes.length; i++)
    {
      var administrationHourMinute = administrationTimes[i];
      if (administrationHourMinute)
      {
        var time = CurrentTime.get();
        time.setHours(administrationHourMinute.hour);
        time.setMinutes(administrationHourMinute.minute);

        var enabled = i === 0 || frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.DAILY_COUNT;
        this.timeFields[i].setTime(time);
        this.timeFields[i].setEnabled(enabled);
      }
    }
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
      this._adjustRowsToDosingFrequency();
      this._prepareFieldsAndFixDates();
    }

    if (this.dosePanes.length > 0 && this.dosePanes[0].length > 0)
    {
      this.dosePanes[0][0].requestFocusToNumerator();
    }
  },

  _setupValidation: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    this.validationForm.reset();

    /* Since validation is called multiple times, existsDosePaneWithValue check was moved to separate method so it only happens once */
    if (!this._existsDosePaneWithValue())
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: self.rowsContainer,
        required: true,
        componentValueImplementationFn: function()
        {
          return null;
        }
      }));
      appFactory.createWarningSystemDialog(view.getDictionary("one.dose.required"), 320, 122).show();
    }

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

  /**
   * Determines if at least one dose of protocol is filled
   * @returns {boolean}
   * @private
   */
  _existsDosePaneWithValue: function()
  {
    for (var j = 0; j < this.dosePanes.length; j++)
    {
      for (var k = 0; k < this.dosePanes[j].length; k++)
      {
        var dosePane = this.dosePanes[j][k];
        if (dosePane.getDose().quantity !== null || dosePane.getDose().quantityDenominator !== null)
        {
          return true;
        }
      }
    }
    return false;
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
      frequency: this.dosingFrequencyPane.getFrequency(),
      untilCanceled: this.untilCanceled,
      endDate: this.selectedDate
    };
    this.resultCallback(new app.views.common.AppResultData({success: true, value: resultValue}));
  },

  /**
   * @param {number} baseValue
   * @returns {Object}
   * @private
   */
  _getDosingFrequencyFromMode: function(baseValue)
  {
    var enums = app.views.medications.TherapyEnums;
    var isCountFrequency = this.dosingFrequencyPane.getFrequencyMode() === "COUNT";
    return {
      type: isCountFrequency ? enums.dosingFrequencyTypeEnum.DAILY_COUNT : enums.dosingFrequencyTypeEnum.BETWEEN_DOSES,
      value: isCountFrequency ? baseValue : Math.round(24 / baseValue)
    };
  },

  _recalculateTimesForBetweenDoses: function()
  {
    var frequency = this.dosingFrequencyPane.getFrequencyValue();

    this._abortPrepareFieldsConditionTask();
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
   * Getters & Setters
   */

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
  },

  /**
   * @Override
   */
  destroy: function()
  {
    this.rows = [];
    this.timeFields = [];
    this.dosePanes = [];
    this.dateLabels = [];
    this.dayLabels = [];
    this._threeDotsContainers = [];
    this._abortPrepareFieldsConditionTask();
    this._testRenderCoordinator.removeCoordinator();
    this.callSuper();
  }
});