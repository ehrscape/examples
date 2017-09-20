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

Class.define('app.views.medications.ordering.TherapyIntervalPane', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_INTERVAL_CHANGE: new tm.jquery.event.EventType({
      name: 'therapyIntervalPaneIntervalChange', delegateName: null})
  },
  cls: "therapy-interval-pane",
  scrollable: 'visible',

  /** configs */
  view: null,
  getDurationFunction: null,  //optional
  getFrequencyDataFunction: null, //optional
  getDosingPatternFunction: null, //optional
  onMaxDailyFrequencyFieldFocusLost: null,
  reminderHideEvent: null, //optional
  isPastMode: false,
  copyMode: false,
  setMaxFrequencyTitleVisible: null,
  withStartEndTime: true,
  restrictedStartHourSelection: true,
  presetCurrentTime: false,
  byDoctorsOrderAvailable: true,
  maxDailyFrequencyAvailable: true,
  /** privates */
  fixedEndDate: null,
  /** privates: components */
  startDateField: null,
  startHourField: null,
  startHourCombo: null,
  _therapyDurationFieldsContainer: null,
  _buttonsAndReminderContainer: null,
  _maxFrequencyContainer: null,
  durationUnitOfMeasureSelectBox: null,
  endAmountField: null,
  endAmountFieldUnitId: null, /* caches the last selected selectbox value for calculations when switching to date */
  endDateTimeField: null,
  endDateTimeText: null,
  reviewReminderField: null,
  whenNeededButton: null,
  byDoctorsOrderButton: null,
  maxDailyFrequencyField: null,

  _endDateLabelRefreshTimer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.registerEventTypes('app.views.medications.ordering.TherapyIntervalPane', [
      { eventType: app.views.medications.ordering.TherapyIntervalPane.EVENT_TYPE_INTERVAL_CHANGE }
    ]);

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));
    this._buildStartEndTimeComponents();
    this._buildOtherComponents();
    this._buildGui();
  },

  /** private methods */
  _buildStartEndTimeComponents: function()
  {
    var self = this;
    this.startDateField = new tm.jquery.DatePicker({
      cls: "start-date-field",
      showType: "focus",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "150px"),
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      },
      date: this.isPresetCurrentTime() ? CurrentTime.get() : null
    });
    this.startDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.calculateEnd();
      self._onEndAmountValueChanged();
      self._signalChangedEvent();
      self._calculateAndSetMinEndDateTime(true);
    });
    this.startDateField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.startDateField.setDate(CurrentTime.get());
          self.startHourField.setTime(self.startHourField.formatTime(CurrentTime.get()));
        });
    this.startHourField = new tm.jquery.TimePicker({
      cls: "start-hour-field",
      showType: "focus",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "90px"),
      nowButton: {
        text: this.view.getDictionary("asap")
      },
      hidden: this.isRestrictedStartHourSelection(),
      time: this.isPresetCurrentTime() && ! this.isRestrictedStartHourSelection() ? CurrentTime.get() : null,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this.startHourField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.calculateEnd();
      self._onEndAmountValueChanged();
      self._signalChangedEvent();
      self._calculateAndSetMinEndDateTime(true);
    });
    this.startHourField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.startDateField.setDate(CurrentTime.get());
          self.startHourField.setTime(self.startHourField.formatTime(CurrentTime.get()));
        });
    this.startHourCombo = new tm.jquery.SelectBox({
      cls: "start-hour-combo",
      allowSingleDeselect: false,
      multiple: false,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "90px"),
      dropdownWidth: "stretch",
      placeholder: " ",
      hidden: !this.isRestrictedStartHourSelection(),
      defaultValueCompareToFunction: function(value1, value2)
      {
        return value1.hour == value2.hour && value1.minute == value2.minute;
      },
      defaultTextProvider: function(selectBox, index, option)
      {
        var value = option.getValue();
        return tm.views.medications.MedicationTimingUtils.hourMinuteToString(value.hour, value.minute)
      }
    });

    this.startHourCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._onEndAmountValueChanged();
      self._signalChangedEvent();
      self._calculateAndSetMinEndDateTime(true);
    });

    var durationUnitOptions = this.getDurationInputTypes().map(function (item)
    {
      return tm.jquery.SelectBox.createOption(item, null);
    });
    this.durationUnitOfMeasureSelectBox = new tm.jquery.SelectBox({
      liveSearch: false,
      options: durationUnitOptions,
      selections: durationUnitOptions.length > 0 ? [durationUnitOptions[0]] : [],
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      dropdownWidth: "stretch",
      allowSingleDeselect: false,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
      },
      defaultTextProvider: function(selectBox, index, option)
      {
        return option.getValue().title;
      },
      hidden: false
    });
    this.durationUnitOfMeasureSelectBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        function(component, componentEvent){
          var selections = componentEvent.eventData.selections;
          self._onDurationUnitOfMeasureValueChanged(selections.length > 0 ? selections[0] : null);
    });

    this.endAmountField = new tm.jquery.TextField({ // using TextField instead of NumberField due to KEY_UP event support
      cls: "end-days-field",
      width: 40,
      hidden: true
    });

    var endAmountFieldEventDebouncedTask = this.view.getAppFactory().createDebouncedTask(
        "endAmountFieldEventDebouncedTask", function()
        {
          self._onEndAmountValueChanged();
          self._signalChangedEvent();
        }, 200);

    this.endAmountField.on(tm.jquery.ComponentEvent.EVENT_TYPE_KEY_UP, function()
    {
      endAmountFieldEventDebouncedTask.run();
    });

    this.endAmountField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.whenNeededButton.focus();
    });

    this.endDateTimeField = new tm.jquery.DateTimePicker({
      showType: "focus",
      hidden: true
    });

    this.endDateTimeField.getDatePicker().on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._onEndDateTimeDatePickerChange();
    });

    this.endDateTimeField.getTimePicker().on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.getStart() && self.endDateTimeField.getDatePicker().getDate())
      {
        self._signalChangedEvent();
      }
    });

    this.endDateTimeField.getDatePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.endDateTimeField.setDate(CurrentTime.get());
        });
    this.endDateTimeField.getTimePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.endDateTimeField.setDate(CurrentTime.get());
        });
    this.endDateTimeField.getDatePicker().setStyle("min-width: 95px;");

    this.endDateTimeText = new tm.jquery.Label({
      cls: "TextData",
      html: null,
      hidden: true
    });

    if (this.isPastMode)
    {
      this.showDurationFieldsForValueType();
    }

    this.reviewReminderField = new tm.jquery.TextField({
      cls: "review-reminder-field",
      placeholder: this.view.getDictionary("days"),
      width: 45
    });
  },

  _buildOtherComponents: function()
  {
    var self = this;

    this.whenNeededButton = new tm.jquery.ToggleButton({
      cls: "when-needed-button",
      text: this.view.getDictionary("when.needed.short"),
      alignSelf: "center"
    });
    this.whenNeededButton.setTooltip(tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("when.needed"), "left", this.view));
    this.whenNeededButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (self.whenNeededButton.isPressed() && self.isByDoctorsOrderAvailable())
      {
        self.byDoctorsOrderButton.setPressed(false);
      }
      self.setMaxDailyFrequencyFieldVisible(self.whenNeededButton.isPressed());
      self._signalChangedEvent();
    });

    if (this.isByDoctorsOrderAvailable())
    {
      this.byDoctorsOrderButton = new tm.jquery.ToggleButton({
        cls: "by-doctors-orders",
        width: 40,
        text: this.view.getDictionary("by.doctor.orders.short"),
        data: app.views.medications.TherapyEnums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS,
        alignSelf: "center"
      });
      this.byDoctorsOrderButton.setTooltip(tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("by.doctor.orders"), "left", this.view));
      this.byDoctorsOrderButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
      {
        if (self.byDoctorsOrderButton.isPressed())
        {
          self.whenNeededButton.setPressed(false);
          self.setMaxDailyFrequencyFieldVisible(false);
        }
      });
    }

    if (this.isMaxDailyFrequencyAvailable())
    {
      this.maxDailyFrequencyField = new tm.jquery.TextField({
        cls: "max-daily-frequency-field",
        width: 32,
        tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary('dosing.max.24h'), null, this.view)
      });

      this.maxDailyFrequencyField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_LOST, function()
      {
        self.onMaxDailyFrequencyFieldFocusLost();
      });
    }
  },

  _buildGui: function()
  {
    //start
    if (this.withStartEndTime === true)
    {
      var dateTimeContainer = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
        scrollable: "visible"
      });

      dateTimeContainer.add(this.startDateField);
      dateTimeContainer.add(this.startHourField);
      dateTimeContainer.add(this.startHourCombo);

      var startTimeContainer = new tm.views.medications.common.VerticallyTitledComponent({
        cls: 'therapy-start-container',
        titleText: this.view.getDictionary('start'),
        contentComponent: dateTimeContainer,
        scrollable: "visible"
      });

      this.add(startTimeContainer);

      var therapyDurationContentContainer = new tm.jquery.Container({
        cls: "therapy-duration-container",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
        scrollable: "visible",
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
      });

      therapyDurationContentContainer.add(this.endAmountField);
      therapyDurationContentContainer.add(this.endDateTimeField);
      therapyDurationContentContainer.add(this.durationUnitOfMeasureSelectBox); // value type selectbox
      therapyDurationContentContainer.add(this.endDateTimeText);

      this._therapyDurationFieldsContainer = new tm.views.medications.common.VerticallyTitledComponent({
        cls: "therapy-duration-fields-container",
        titleText: this.view.getDictionary('therapy.duration'),
        contentComponent: therapyDurationContentContainer,
        scrollable: "visible",
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });

      this.add(this._therapyDurationFieldsContainer);
    }

    //stretch, so buttons will be on the right
    var rightContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "flex-end"),
      scrollable: "visible",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });
    var buttonsContainer = new tm.jquery.Container({
      cls: "right-btns-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    //when needed and by doctor orders
    buttonsContainer.add(this.reviewReminderField);
    buttonsContainer.add(this.whenNeededButton);
    if (this.isByDoctorsOrderAvailable())
    {
      buttonsContainer.add(this.byDoctorsOrderButton);
    }

    this._buttonsAndReminderContainer = new tm.views.medications.common.VerticallyTitledComponent({
      cls: "btns-reminder-container",
      titleText: this.view.getDictionary('reminder'),
      contentComponent: buttonsContainer,
      scrollable: "visible",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    rightContainer.add(this._buttonsAndReminderContainer);

    if (this.isMaxDailyFrequencyAvailable())
    {
      this._maxFrequencyContainer = new tm.views.medications.common.VerticallyTitledComponent({
        cls: "max-frequency-container",
        titleText: "Max",
        contentComponent: this.maxDailyFrequencyField,
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
      });

      rightContainer.add(this._maxFrequencyContainer);
    }
    this.add(rightContainer);
  },

  _resetTherapyDurationInputFields: function(setSelections)
  {
    this.isRendered() ? this.endDateTimeField.hide() : this.endDateTimeField.setHidden(true);
    this.isRendered() ? this.endAmountField.hide() : this.endAmountField.setHidden(true);
    this.isRendered() ? this.durationUnitOfMeasureSelectBox.show() : this.durationUnitOfMeasureSelectBox.setHidden(false);
    this.isRendered() ? this.endDateTimeText.hide() : this.endDateTimeText.setHidden(true);
    if (this._therapyDurationFieldsContainer)
    {
      this.isRendered() ?
          this._therapyDurationFieldsContainer.show() :
          this._therapyDurationFieldsContainer.setHidden(false);
    }
    if (setSelections)
    {
      var firstOption = this.durationUnitOfMeasureSelectBox.getOptions()[0];
      if (!tm.jquery.Utils.isEmpty(firstOption))
      {
        this.durationUnitOfMeasureSelectBox.setSelections([firstOption.value], true);
      }
    }
  },

  _onDurationUnitOfMeasureValueChanged: function(selection)
  {
    var self = this;

    if (!tm.jquery.Utils.isEmpty(selection)
        && selection instanceof app.views.medications.ordering.TherapyIntervalPaneDurationUnitType)
    {
      if (tm.jquery.Utils.isEmpty(this.endAmountFieldUnitId)) /* if there was no value date was prev. selected */
      {
        this.endAmountField.setValue(null);
        this.endDateTimeText.setHtml(null);
      }
      this.endAmountFieldUnitId = selection.getId();
      this.showDurationFieldsForValueType();
      this._onEndAmountValueChanged();
    }
    else if (selection.id == app.views.medications.TherapyEnums.therapyIntervalPaneSelectionIds.UNTIL_CANCELED)
    {
      self.endAmountField.setValue(null);
      self.endDateTimeText.setHtml(null);
      self.endDateTimeField.getDatePicker().setDate(null, true);
      self.endDateTimeField.getTimePicker().setTime(null, true);
      self._signalChangedEvent();

      self._resetTherapyDurationInputFields(true);
    }
    else // date picker
    {
      var fixedAmountValue = this._getDeLocalizedEndAmountValue();
      if (fixedAmountValue && tm.jquery.Utils.isNumeric(fixedAmountValue) && fixedAmountValue > 0)
      {
        var fixedAmountType = this._findDurationValueTypOptionById(this.endAmountFieldUnitId);
        var startDate = this.startDateField.getDate();

        if (startDate && fixedAmountType != null)
        {
          var endDate = new Date(startDate.valueOf());
          var startTime = this.getStart();
          if (startTime)
          {
            endDate.setTime(startTime);
          }

          this.endDateTimeField.setDate(fixedAmountType.appendToInputValue(endDate, fixedAmountValue));
        }
      }
      else if (this.fixedEndDate)
      {
        this.endDateTimeField.setDate(this.fixedEndDate);
      }
      else
      {
        this.endDateTimeField.setDate(null);
        this.endAmountField.setValue(null);
        this.endDateTimeText.setHtml(null);
      }
      this.endAmountFieldUnitId = null; /* reset whatever was cached as last value */

      this._isOneTimeFrequencySelected() && this._therapyDurationFieldsContainer ?
          this._therapyDurationFieldsContainer.hide() :
          this.showDurationFieldsForDateType();
    }
  },

  /**
   * @returns {boolean}
   * @private
   */
  _isOneTimeFrequencySelected: function()
  {
    if (this.getFrequencyDataFunction)
    {
      var frequencyType = this.getFrequencyDataFunction().frequencyType;
      if (frequencyType == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.ONCE_THEN_EX)
      {
        return true;
      }
    }
    return false;
  },

  _onEndAmountValueChanged: function()
  {
    var self = this;
    var view = this.view;
    clearTimeout(this._endDateLabelRefreshTimer);
    this._endDateLabelRefreshTimer = setTimeout(function()
    {
      if (self.isRendered())
      {
        var displayValue = self.getEnd();
        displayValue = tm.jquery.Utils.isEmpty(displayValue) ?
            null : ("(" + view.getDisplayableValue(displayValue, "short.date.time") + ")");
        self.endDateTimeText.setHtml(displayValue);
      }
    }, 350);
  },

  _findDurationValueTypOptionById: function(id)
  {
    if (tm.jquery.Utils.isEmpty(id)) return null;

    var options = this.durationUnitOfMeasureSelectBox.getOptions();
    for (var idx = 0; idx < options.length; idx++)
    {
      var option = options[idx].value;

      if (option instanceof app.views.medications.ordering.TherapyIntervalPaneDurationUnitType && option.getId() === id)
      {
        return option;
      }
    }
    return null;
  },

  _frequencyOrPatternChanged: function(therapy, oldTherapy)
  {
    var frequencyChanged = tm.jquery.Utils.isEmpty(oldTherapy) || tm.jquery.Utils.isEmpty(oldTherapy.dosingFrequency) ||
        oldTherapy.dosingFrequency.type != therapy.dosingFrequency.type ||
        oldTherapy.dosingFrequency.value != therapy.dosingFrequency.value ||
        oldTherapy.dosingDaysFrequency != therapy.dosingDaysFrequency;

    if (frequencyChanged)
    {
      return true;
    }
    else if (therapy.variable)
    {

    }
    else
    {
      for (var i = 0; i < therapy.doseTimes.length; i++)
      {
        if (therapy.doseTimes[i].hour != oldTherapy.doseTimes[i].hour ||
            therapy.doseTimes[i].minute != oldTherapy.doseTimes[i].minute)
        {
          return true;
        }
      }
    }
    return false;
  },

  /**
   * Triggers the INTERVAL_CHANGE event.
   * @private
   */
  _signalChangedEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.TherapyIntervalPane.EVENT_TYPE_INTERVAL_CHANGE,
      eventData: { start: this.getStart() }
    }), null);
  },

  _reminderHideEvent: function(hide)
  {
    if (hide)
    {
      this._buttonsAndReminderContainer.setTitleText("");
    }
    else
    {
      this._buttonsAndReminderContainer.setTitleText(this.view.getDictionary('reminder'));
    }
  },
  _setMaxFrequencyTitleVisible: function(show)
  {
    if (!this.isMaxDailyFrequencyAvailable()) return;

    if (show)
    {
      this._maxFrequencyContainer.show();
    }
    else
    {
      this._maxFrequencyContainer.hide();
    }
  },

  _handleStartHourInputRestrictionChange: function()
  {
    if (!this.isRestrictedStartHourSelection())
    {
      this.isRendered() ? this.startHourCombo.hide() : this.startHourCombo.setHidden(true);
      this.isRendered() ? this.startHourField.show() : this.startHourField.setHidden(false);

      if (this._therapyDurationFieldsContainer && this._therapyDurationFieldsContainer.isHidden()) //previous frequency was 1ex
      {
        this.setEnd(null);
      }
    }
    else
    {
      this.isRendered() ? this.startHourField.hide() : this.startHourField.setHidden(true);
      this.isRendered() ? this.startHourCombo.show() : this.startHourCombo.setHidden(false);
    }
  },

  /**
   * Returns the endAmountField value with the possible occurrences of decimal symbols replaced to the
   * english format, which works with JS's parseFloat and jQuery's isNumeric method for further use
   * and input checks.
   * @returns {null|String}
   * @private
   */
  _getDeLocalizedEndAmountValue: function()
  {
    var fieldAmount = this.endAmountField.getValue();

    if (!fieldAmount) return null;

    var decimalSymbol = Globalize.cldr.main("numbers/symbols-numberSystem-latn/decimal");

    if (decimalSymbol != ".") // sl
    {
      fieldAmount = fieldAmount.replaceAll(decimalSymbol, '.');
    }
    return fieldAmount;
  },

  /**
   * Sets minimal therapy end date and time
   * @param {Boolean} limitByStart
   * @private
   */
  _calculateAndSetMinEndDateTime: function(limitByStart)
  {
    if (limitByStart)
    {
      if (!this.endDateTimeField.isHidden() && this.getStart())
      {
        if (this.endDateTimeField.getDatePicker().getDate() &&
            this.startDateField.getDate() > this.endDateTimeField.getDatePicker().getDate())
        {
          this.endDateTimeField.getDatePicker().setDate(this.startDateField.getDate(), true);
        }
        this.endDateTimeField.getDatePicker().setMinDate(this.getStart());

        if (this.endDateTimeField.getDatePicker().getDate() &&
            this.endDateTimeField.getDatePicker().getDate().getTime() === this.startDateField.getDate().getTime())
        {
          this.endDateTimeField.getTimePicker().setMinTime(this.getStart());
          if (this.endDateTimeField.getTimePicker().getTime() &&
              this.endDateTimeField.getTimePicker().getTime() < this.getStart())
          {
            this.endDateTimeField.getTimePicker().setTime(this.getStart(), true);
          }
        }
        else
        {
          this.endDateTimeField.getTimePicker().setMinTime(null);
        }
      }
    }
    else
    {
      this.endDateTimeField.getDatePicker().setMinDate(null);
      this.endDateTimeField.getTimePicker().setMinTime(null);
    }
  },

  _onEndDateTimeDatePickerChange: function()
  {
    if (this.getStart())
    {
      this._calculateAndSetMinEndDateTime(true);
      if (this.endDateTimeField.getTimePicker().getTime())
      {
        this._signalChangedEvent();
      }
    }
  },

  /** public methods */

  setMedicationData: function(medicationData)
  {
    if (medicationData.reviewReminder)
    {
      this.reviewReminderField.show();
    }
    else
    {
      this.reviewReminderField.hide();
    }
    this._reminderHideEvent(!medicationData.reviewReminder);
  },

  clear: function()
  {
    var presetDate = this.view.getPresetDate();
    this.startDateField.setDate(presetDate);
    this.startHourField.setTime(presetDate);
    this.endAmountField.setValue(null);
    this.endDateTimeText.setHtml(null);
    this.endAmountFieldUnitId = null;
    this.whenNeededButton.setPressed(false);
    if (this.isByDoctorsOrderAvailable())
    {
      this.byDoctorsOrderButton.setPressed(false);
      this.byDoctorsOrderButton.setEnabled(true);
    }
    this.setMaxDailyFrequencyFieldVisible(false);
    this.setEnd(null);
    this.startHourCombo.removeAllOptions();
    this.showWhenNeededDoctorsOrder();
    this.reviewReminderField.setValue(null);
    this.setStartHourEnabled(true);
  },

  setMaxDailyFrequencyFieldVisible: function(show)
  {
    if (!this.isMaxDailyFrequencyAvailable()) return;

    var setVisible = show && this.whenNeededButton.isPressed() && this.getFrequencyDataFunction().frequencyMode == 'HOURS';
    if (setVisible)
    {
      this._maxFrequencyContainer.show();
    }
    else
    {
      this.maxDailyFrequencyField.setValue(null);
      this._maxFrequencyContainer.hide();
    }
    this._setMaxFrequencyTitleVisible(setVisible);
  },

  getStart: function()
  {
    if (this.withStartEndTime === false) return null;

    var date = this.startDateField.getDate();
    var time = null;
    if (!this.startHourField.isHidden())
    {
      time = this.startHourField.getTime();
    }
    else
    {
      var hourMinute = this.startHourCombo.getSelections()[0];
      time = tm.views.medications.MedicationTimingUtils.hourMinuteToDate(hourMinute);
    }
    if (date && time)
    {
      return new Date(date.getFullYear(), date.getMonth(), date.getDate(), time.getHours(), time.getMinutes(), 0, 0);
    }
    return null;
  },

  setStart: function(start, suppressChangeEvent)
  {
    var startDate = new Date(start);
    this.startDateField.setDate(start ? startDate : null);
    if (!this.startHourField.isHidden())
    {
      this.startHourField.setTime(start ? startDate : null, suppressChangeEvent === true);
    }
    else
    {
      var selections = start ? [{hour: startDate.getHours(), minute: startDate.getMinutes()}] : [];
      this.startHourCombo.setSelections(selections, suppressChangeEvent === true);
    }
  },

  getEnd: function()
  {
    if (this.fixedEndDate)
    {
      return this.fixedEndDate;
    }

    if (this.withStartEndTime === false)
    {
      return null;
    }

    //end defined as duration in days
    if (!this.endAmountField.isHidden())
    {
      var fixedAmountValue = this._getDeLocalizedEndAmountValue();
      if (fixedAmountValue && tm.jquery.Utils.isNumeric(fixedAmountValue) && fixedAmountValue > 0)
      {
        var fixedAmountType = this.durationUnitOfMeasureSelectBox.getSelections()[0];
        var end = this.getStart();

        if (end && !tm.jquery.Utils.isEmpty(fixedAmountType))
        {
          return fixedAmountType.appendToInputValue(end, fixedAmountValue);
        }
      }
      return null;
    }

    //end defined as date
    var endDate = this.endDateTimeField.getDate();
    if (!tm.jquery.Utils.isEmpty(endDate))
    {
      return new Date(endDate.valueOf());
    }
    return null;
  },

  getReviewReminderDays: function()
  {
    if (!this.reviewReminderField.isHidden())
    {
      var value = this.reviewReminderField.getValue();
      return value ? value : null;
    }
    return null;
  },

  setReviewReminderDays: function(reminderDays)
  {
    if (!this.reviewReminderField.isHidden())
    {
      this.reviewReminderField.setValue(reminderDays);
    }
  },

  setEnd: function(end)
  {
    this.fixedEndDate = end ? new Date(end.getTime()) : null;
    this.endDateTimeField.setDate(null);
    this.endAmountField.setValue(null);
    this.endAmountFieldUnitId = null;

    if (this.fixedEndDate)
    {
      this._resetTherapyDurationInputFields(false);

      this.endDateTimeField.setDate(this.fixedEndDate);
      this.durationUnitOfMeasureSelectBox.setSelections([{id: app.views.medications.TherapyEnums.therapyIntervalPaneSelectionIds.DATE}]);
      this.fixedEndDate = null;
    }
    else if (this.isPastMode)
    {
      this.showDurationFieldsForDateType();
    }
    else
    {
      this._resetTherapyDurationInputFields(true);
    }

    if (this._isOneTimeFrequencySelected() && this._therapyDurationFieldsContainer)
    {
      this.isRendered() ? this._therapyDurationFieldsContainer.hide() : this._therapyDurationFieldsContainer.setHidden(true);
    }
  },

  /**
   * @returns {number|null}
   */
  getMaxDailyFrequency: function()
  {
    if (this.isMaxDailyFrequencyAvailable() && tm.jquery.Utils.isNumeric(this.maxDailyFrequencyField.getValue()))
    {
      return this.maxDailyFrequencyField.getValue();
    }
    return null;
  },

  setMaxDailyFrequency: function(maxDailyFrequency)
  {
    if (this.isMaxDailyFrequencyAvailable())
    {
      this.maxDailyFrequencyField.setValue(maxDailyFrequency);
    }
  },

  setStartHourEnabled: function(enabled)
  {
    this.startHourField.setEnabled(enabled);
  },

  setStartOptionsFromPattern: function()
  {
    var self = this;
    if (this.getDosingPatternFunction)
    {
      this.startHourCombo.removeAllOptions();
      var dosingPattern = this.getDosingPatternFunction();

      if (dosingPattern && dosingPattern.length > 0)
      {
        var uniquePatterns = {};
        dosingPattern.forEach(function(item)
        {
          var key = (!tm.jquery.Utils.isEmpty(item.hour) ? (item.hour).toString() : "")
          + (!tm.jquery.Utils.isEmpty(item.minute) ? (item.minute).toString() : "");

          if (!uniquePatterns.hasOwnProperty(key))
          {
            uniquePatterns[key] = true;
            self.startHourCombo.addOption(tm.jquery.SelectBox.createOption(item));
          }
        });
      }
      this.startHourCombo.setSelections([], true);
    }
  },

  /**
   * Calling this method will recalculate the start - which usually means to a new date either now or in the future.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} [limitEndDate=true] Should the new start limit the therapy end date.
   * @param {app.views.medications.common.dto.Therapy} [oldTherapy=null]
   * @param {Function} [callback=null]
   */
  calculateStart: function(therapy, limitEndDate, oldTherapy, callback)
  {
    limitEndDate = tm.jquery.Utils.isBoolean(limitEndDate) ? limitEndDate : true;

    var self = this;
    therapy.setEnd(null);
    if (this.getDosingPatternFunction)
    {
      this.setStartOptionsFromPattern();
      var patternMode = !this.startHourCombo.isHidden();
      if (patternMode)
      {
        if (!therapy.getDosingFrequency() && !therapy.isNormalVariableInfusion())
        {
          this.setStart(null);
        }
        else
        {
          if (therapy.isNormalVariableInfusion())
          {
            therapy.setTimedDoseElements([therapy.getTimedDoseElements()[0]]);
          }
          var newPrescriptionOrFrequencyChanged = this._frequencyOrPatternChanged(therapy, oldTherapy);
          if (!newPrescriptionOrFrequencyChanged)
          {
            newPrescriptionOrFrequencyChanged = false;
            therapy.setStart(oldTherapy.getStart());
          }
          else
          {
            var now = CurrentTime.get();
            therapy.setStart(new Date(
                now.getFullYear(),
                now.getMonth(),
                now.getDate(),
                now.getHours(),
                now.getMinutes(),
                0,
                0));
          }

          var moreThan24h =
              therapy.getDosingFrequency() &&
              therapy.getDosingFrequency().type == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES &&
              therapy.getDosingFrequency().value > 24;
          var daysInvolved = !tm.jquery.Utils.isEmpty(therapy.getDosingDaysFrequency()) || moreThan24h ||
              (therapy.getDaysOfWeek() && therapy.getDaysOfWeek().length > 0);

          if (!tm.jquery.Utils.isEmpty(oldTherapy) && newPrescriptionOrFrequencyChanged && daysInvolved)
          {
            self.setStart(null);
          }
          else
          {
            tm.views.medications.MedicationTimingUtils.getTherapyNextAdministration(
                this.view,
                therapy,
                newPrescriptionOrFrequencyChanged,
                function(nextTime)
                {
                  if (nextTime)
                  {
                    var nextHourMinute = {
                      hour: nextTime.getHours(),
                      minute: nextTime.getMinutes()
                    };
                    // set the date first, but suppress the change event, since it's unreliable as it checks
                    // if the new value is the same as the old before it fires a change event
                    self.startDateField.setDate(nextTime, true);
                    self.startHourCombo.setSelections([nextHourMinute], true);
                    self._onEndAmountValueChanged();
                    self._calculateAndSetMinEndDateTime(limitEndDate);
                    self._signalChangedEvent();
                    if (callback)
                    {
                      callback();
                    }
                  }
                });
          }
        }
      }
      else
      {
        // set the date first, but suppress the change event, since it's unreliable as it checks
        // if the new value is the same as the old before it fires a change event
        var startTime = tm.views.medications.MedicationTimingUtils.getTimestampRoundedUp(CurrentTime.get(), 5);
        this.startDateField.setDate(startTime, true);
        this.startHourField.setTime(startTime, true); // suppress the change event
        this._onEndAmountValueChanged();
        this._calculateAndSetMinEndDateTime(limitEndDate);
        if (callback)
        {
          callback();
        }
        self._signalChangedEvent();
      }
    }
  },

  calculateEnd: function()
  {
    var start = this.getStart();
    if (start)
    {
      if (this._isOneTimeFrequencySelected())
      {
        if (this.getDurationFunction)
        {
          var duration = this.getDurationFunction();
          if (duration)
          {
            start.setMinutes(Number(start.getMinutes()) + Number(duration));
            this.setEnd(start);
          }
          else
          {
            this.setEnd(start);
          }
        }
        else
        {
          this.setEnd(start);
        }
        if (this._therapyDurationFieldsContainer)
        {
          this._therapyDurationFieldsContainer.hide();
        }
      }
      else
      {
        if (this._therapyDurationFieldsContainer && this._therapyDurationFieldsContainer.isHidden()) //previous frequency was 1ex
        {
          this.setEnd(null);
        }
      }
    }
  },

  getWhenNeeded: function()
  {
    return !this.whenNeededButton.isHidden() ? this.whenNeededButton.isPressed() : false;
  },

  setWhenNeeded: function(whenNeeded)
  {
    if (this.isMaxDailyFrequencyAvailable())
    {
      this.setMaxDailyFrequencyFieldVisible(whenNeeded);
    }
    this.whenNeededButton.setPressed(whenNeeded);
  },

  /**
   * @param {boolean} value
   */
  setByDoctorsOrderButtonEnabled: function(value)
  {
    if (this.isByDoctorsOrderAvailable())
    {
      this.byDoctorsOrderButton.setPressed(false);
      this.byDoctorsOrderButton.setEnabled(value);
    }
  },

  getStartCriterion: function()
  {
    if (this.isByDoctorsOrderAvailable() && !this.byDoctorsOrderButton.isHidden() && this.byDoctorsOrderButton.isPressed())
    {
      return this.byDoctorsOrderButton.data;
    }
    return null;
  },

  setStartCriterion: function(startCriterion)
  {
    if (this.isByDoctorsOrderAvailable() && startCriterion == this.byDoctorsOrderButton.data)
    {
      this.byDoctorsOrderButton.setPressed(true);
    }
  },

  getTherapyIntervalPaneValidations: function(startNotBefore)
  {
    var self = this;
    var formFields = [];
    var startTime = self.getStart();
    var endTime = self.getEnd();

    formFields.push(new tm.jquery.FormField({
      component: self,
      required: true,
      componentValueImplementationFn: function()
      {
        if (!startTime)
        {
          return null;
        }
        if (self.isPastMode && !endTime)
        {
          return null;
        }
        if (endTime && startTime > endTime)
        {
          return null;
        }
        if (self.endDateTimeField.getDatePicker().getDate() && !self.endDateTimeField.getTimePicker().getTime() ||
            !self.endDateTimeField.getDatePicker().getDate() && self.endDateTimeField.getTimePicker().getTime())
        {
          return null;
        }
        if (startNotBefore)
        {
          if (startTime < startNotBefore && !this.isCopyMode())
          {
            return null;
          }
        }
        return true;
      }
    }));
    formFields.push(new tm.jquery.FormField({
      component: self.endAmountField,
      required: false,
      componentValueImplementationFn: function()
      {
        return self._getDeLocalizedEndAmountValue();
      },
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: this.view.getDictionary("field.value.is.invalid"),
            isValid: function (value)
            {
              return value ? tm.jquery.Utils.isNumeric(value) : true;
            }
          })
        ]
      }
    }));
    return formFields;
  },

  getDurationInputTypes: function()
  {
    var view = this.view;
    var enums = app.views.medications.TherapyEnums;
    var options = [];

    options.push(
        new tm.jquery.Object({
          id: enums.therapyIntervalPaneSelectionIds.UNTIL_CANCELED,
          title: view.getDictionary("until.cancellation.lc").toLowerCase()
        }),
        new app.views.medications.ordering.TherapyIntervalPaneDurationUnitType({
          id: enums.therapyIntervalPaneSelectionIds.HOURS,
          title: view.getDictionary("hours.accusative").toLowerCase(),
          appendToInputValue: function(toDate, inputAmount)
          {
            var endDate = new Date(toDate.valueOf());
            endDate.setMinutes(endDate.getMinutes() + parseFloat(inputAmount) * 60);
            return endDate;
          }
        }),
        new app.views.medications.ordering.TherapyIntervalPaneDurationUnitType({
          id: enums.therapyIntervalPaneSelectionIds.DAYS,
          title: view.getDictionary("days").toLowerCase(),
          appendToInputValue: function(toDate, inputAmount)
          {
            var endDate = new Date(toDate.valueOf());
            endDate.setDate(endDate.getDate() + parseInt(inputAmount));
            return endDate;
          }
        }),
        new app.views.medications.ordering.TherapyIntervalPaneDurationUnitType({
          id: enums.therapyIntervalPaneSelectionIds.WEEKS,
          title: view.getDictionary("weeks").toLowerCase(),
          appendToInputValue: function(toDate, inputAmount)
          {
            var endDate = new Date(toDate.valueOf());
            endDate.setDate(endDate.getDate() + (parseInt(inputAmount) * 7));
            return endDate;
          }
        }),
        new app.views.medications.ordering.TherapyIntervalPaneDurationUnitType({
          id: enums.therapyIntervalPaneSelectionIds.MONTHS,
          title: view.getDictionary("month.plural.lc").toLowerCase(),
          appendToInputValue: function(toDate, inputAmount)
          {
            var endDate = new Date(toDate.valueOf());
            endDate.setDate(endDate.getDate() + (parseInt(inputAmount) * 30));
            return endDate;
          }
        }),
        new tm.jquery.Object({
          id: enums.therapyIntervalPaneSelectionIds.DATE,
          title: view.getDictionary("select.date").toLowerCase()
        })
    );
    return options;
  },

  /**
   * @param {boolean} value
   */
  setRestrictedStartHourSelection: function(value)
  {
    this.restrictedStartHourSelection = value;
    this._handleStartHourInputRestrictionChange();
  },

  /**
   * @returns {boolean}
   */
  isRestrictedStartHourSelection: function()
  {
    return this.restrictedStartHourSelection === true;
  },

  requestFocus: function()
  {
    this.startDateField.focus();
  },

  isPaneReady: function()
  {
    return this.withStartEndTime === true
        ? this.startHourCombo.isRendered() && this.startDateField.getPlugin() != null
        : true;
  },

  showDurationFieldsForValueType: function()
  {
    this.isRendered() ? this.endDateTimeField.hide() : this.endDateTimeField.setHidden(true);
    this.isRendered() ? this.endAmountField.show() : this.endAmountField.setHidden(false);
    // make sure durationUnitOfMeasureSelectBox it's displayed - costs nothing
    this.isRendered() ? this.durationUnitOfMeasureSelectBox.show() : this.durationUnitOfMeasureSelectBox.setHidden(false);
    this.isRendered() ? this.endDateTimeText.show() : this.endDateTimeText.setHidden(false);
    if (this._therapyDurationFieldsContainer)
    {
      // make sure it's displayed - costs nothing
      this.isRendered() ?
          this._therapyDurationFieldsContainer.show() :
          this._therapyDurationFieldsContainer.setHidden(false);
    }
  },

  setMinEnd: function(therapyEnd)
  {
    this.endDateTimeField.getDatePicker().setMinDate(therapyEnd);
  },

  showDurationFieldsForDateType: function()
  {
    this.isRendered() ? this.endAmountField.hide() : this.endAmountField.setHidden(true);
    this.isRendered() ? this.endDateTimeText.hide() : this.endDateTimeText.setHidden(true);
    this.isRendered() ? this.endDateTimeField.show() : this.endDateTimeField.setHidden(false);
    this._calculateAndSetMinEndDateTime(true);
    // make sure durationUnitOfMeasureSelectBox it's displayed - costs nothing
    this.isRendered() ? this.durationUnitOfMeasureSelectBox.show() : this.durationUnitOfMeasureSelectBox.setHidden(false);
    if (this._therapyDurationFieldsContainer)
    {
      // make sure it's displayed - costs nothing
      this.isRendered() ?
          this._therapyDurationFieldsContainer.show() :
          this._therapyDurationFieldsContainer.setHidden(false);
    }
  },

  hideWhenNeededDoctorsOrder: function()
  {
    this.isRendered() ? this.whenNeededButton.hide() : this.whenNeededButton.setHidden(false);
    if (this.isByDoctorsOrderAvailable())
    {
      this.isRendered() ?  this.byDoctorsOrderButton.hide() : this.byDoctorsOrderButton.setHidden(true);
    }
  },

  showWhenNeededDoctorsOrder: function()
  {
    if (this.whenNeededButton.isRendered())
    {
      this.whenNeededButton.show();
    }
    if (this.isByDoctorsOrderAvailable()&& this.byDoctorsOrderButton.isRendered())
    {
      this.byDoctorsOrderButton.show();
    }
  },

  /**
   * @returns {boolean}
   */
  isPresetCurrentTime: function()
  {
    return this.presetCurrentTime === true;
  },

  /**
   * @returns {boolean}
   */
  isCopyMode: function ()
  {
    return this.copyMode === true;
  },

  /**
   * @returns {boolean}
   */
  isByDoctorsOrderAvailable: function()
  {
    return this.byDoctorsOrderAvailable === true;
  },

  /**
   * @returns {boolean}
   */
  isMaxDailyFrequencyAvailable: function()
  {
    return this.maxDailyFrequencyAvailable === true;
  }
});
Class.define('app.views.medications.ordering.TherapyIntervalPaneDurationUnitType', 'tm.jquery.Object', {
  id: null,
  title: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  appendToInputValue: function(toDate, inputAmount)
  {
    // implement the conversion function, make a copy of toDate first
    return new Date(toDate.valueOf());
  },

  getTitle: function()
  {
    return this.title;
  },
  getId: function()
  {
    return this.id;
  }
});

