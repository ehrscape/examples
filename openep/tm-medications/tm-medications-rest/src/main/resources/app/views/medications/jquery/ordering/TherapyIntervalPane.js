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

  /** configs */
  view: null,
  getDurationFunction: null,  //optional
  getFrequencyKeyFunction: null, //optional
  getFrequencyModeFunction: null, //optional
  getDosingFrequencyModeFunction: null,
  untilHideEvent: null, //optional
  isPastMode: false,
  presetDate: null, //optional
  setMaxFrequencyTitleVisible: null,
  /** privates */
  fixedEndDate: null,
  /** privates: components */
  nowButton: null,
  startDateField: null,
  startHourField: null,

  untilLinkButton: null,
  cardContainer: null,
  modeButtonGroup: null,
  daysModeButton: null,
  dateModeButton: null,
  endDaysField: null,
  endDaysCard: null,
  endDateCard: null,
  endDateField: null,
  endHourField: null,
  maxDailyFrequencyField: null,

  whenNeededButton: null,
  byDoctorsOrderButton: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.HFlexboxLayout.create("start", "start", 5));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.nowButton = new tm.jquery.Button({
      text: this.view.getDictionary("asap"),
      handler: function()
      {
        self.startDateField.setDate(new Date());
        if (self.startHourField.isEnabled())
        {
          self.startHourField.setTime(new Date());
        }
      }});
    this.startDateField = new tm.jquery.DatePicker();
    this.startDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.calculateEnd();
    });
    this.startDateField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.startDateField.setDate(new Date());
          self.startHourField.setTime(self.startHourField.formatTime(new Date()));
        });
    this.startHourField = new tm.jquery.TimePicker();
    this.startHourField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.calculateEnd();
    });

    this.startHourField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.startDateField.setDate(new Date());
          self.startHourField.setTime(self.startHourField.formatTime(new Date()));
        });
    this.cardContainer = new tm.jquery.CardContainer({width: 192, height: 30, animation: 'slide-vertical'});
    this.hoursCard = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});

    this.daysModeButton = new tm.jquery.Button({text: 'd', pressed: true});
    this.dateModeButton = new tm.jquery.Button({text: 'D'});
    this.modeButtonGroup = new tm.jquery.ButtonGroup({
      padding: '0 0 0 0',
      type: 'radio',
      buttons: [this.daysModeButton, this.dateModeButton]
    });
    this.modeButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._modeButtonGroupChanged();
    });

    //end days
    this.endDaysCard = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    this.endDaysField = new tm.jquery.TextField({width: 40});
    this.endDaysField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.whenNeededButton.focus();
    });

    //end date
    this.endDateCard = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    this.endDateField = new tm.jquery.DatePicker();
    this.endDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_LOST, function(component)
    {
      var endDate = component.getDate();
      var endTime = self.endHourField.getTime();
      if (endDate)
      {
        if (!endTime)
        {
          self.endHourField.setTime(self.startHourField.getTime());
        }
      }
    });
    this.endDateField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.endDateField.setDate(new Date());
          self.endHourField.setTime(self.endHourField.formatTime(new Date()));
        });
    this.endHourField = new tm.jquery.TimePicker();
    this.endHourField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.endDateField.setDate(new Date());
          self.endHourField.setTime(self.endHourField.formatTime(new Date()));
        });

    if (this.isPastMode)
    {
      //todo remove when flexbox on datetime pickers (fields didn't resize)
      $(this.endDateField.getInputElement()).css("min-width", 72);
      $(this.endHourField.getInputElement()).css("min-width", 38);
    }

    this.untilLinkButton = new tm.jquery.Button({
      cls: "until-link-button",
      text: this.view.getDictionary('until.cancellation.lc'),
      margin: '0 0 0 5',
      type: "link",
      handler: function()
      {
        if (self.fixedEndDate)
        {
          self.modeButtonGroup.setSelection([self.dateModeButton]);
          self.fixedEndDate = null;
        }
        self.untilLinkButton.hide();
        self.modeButtonGroup.show();
        self.cardContainer.show();
      }});

    if (this.isPastMode)
    {
      this.untilLinkButton.hide();
      this.cardContainer.show();
      this.modeButtonGroup.show();
    }
    else
    {
      this.untilLinkButton.show();
      this.cardContainer.hide();
      this.modeButtonGroup.hide();
    }
    this.whenNeededButton = new tm.jquery.ToggleButton({text: this.view.getDictionary("when.needed.short")});
    this.whenNeededButton.setTooltip(tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("when.needed"), "left", this.view));
    this.whenNeededButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (self.whenNeededButton.isPressed())
      {
        self.byDoctorsOrderButton.setPressed(false);
      }
      self.setMaxDailyFrequencyFieldVisible(self.whenNeededButton.isPressed());
    });

    this.byDoctorsOrderButton = new tm.jquery.ToggleButton({
      width: 40,
      text: this.view.getDictionary("by.doctor.orders.short"),
      data: app.views.medications.TherapyEnums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS
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

    this.maxDailyFrequencyField = new tm.jquery.TextField({width: 25});

  },

  _buildGui: function()
  {
    //start
    if (!this.isPastMode)
    {
      this.add(this.nowButton);
    }
    this.add(this.startDateField);
    this.add(this.startHourField);

    var endContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5), width: 295, height: 30});
    this.add(endContainer);

    //until cancellation
    endContainer.add(this.untilLinkButton);

    endContainer.add(this.modeButtonGroup);
    endContainer.add(this.cardContainer);

    //end days card
    this.cardContainer.add(this.endDaysCard);
    this.endDaysCard.add(this.endDaysField);
    this.endDaysCard.add(tm.views.medications.MedicationUtils.crateLabel('TextData', this.view.getDictionary("days")));
    //end date card
    this.cardContainer.add(this.endDateCard);
    this.endDateCard.add(this.endDateField);
    this.endDateCard.add(this.endHourField);

    //when needed and by doctor orders
    this.add(this.whenNeededButton);
    this.add(this.byDoctorsOrderButton);
    this.add(this.maxDailyFrequencyField);
  },

  _modeButtonGroupChanged: function()
  {
    var selectedButton = this.modeButtonGroup.getSelection()[0];
    if (selectedButton == this.daysModeButton)
    {
      this.cardContainer.setActiveItem(this.endDaysCard);
      this.endDaysField.setValue(null);
    }
    else
    {
      this.cardContainer.setActiveItem(this.endDateCard);
      var daysCount = this.endDaysField.getValue();
      if (daysCount)
      {
        var startDate = this.startDateField.getDate();
        if (startDate)
        {
          var endDate = new Date();
          endDate.setDate(Number(startDate.getDate()) + Number(daysCount));
          this.endDateField.setDate(endDate);

          var startTime = this.startHourField.getTime();
          if (startTime)
          {
            this.endHourField.setTime(startTime);
          }
        }
      }
      else if (this.fixedEndDate)
      {
        this.endDateField.setDate(this.fixedEndDate);
        this.endHourField.setTime(this.fixedEndDate);
      }
      else
      {
        this.endDateField.setDate(null);
        this.endHourField.setTime(null);
      }
    }
  },

  /** public methods */
  clear: function()
  {
    this.startDateField.setDate(this.presetDate ? this.presetDate : null);
    this.startHourField.setTime(null);
    this.endDaysField.setValue(null);
    this.whenNeededButton.setPressed(false);
    this.byDoctorsOrderButton.setPressed(false);
    this.setMaxDailyFrequencyFieldVisible(false);
    this.setEnd(null);
  },

  setMaxDailyFrequencyFieldVisible: function(show)
  {
    var setVisible = show && this.whenNeededButton.isPressed() && this.getDosingFrequencyModeFunction() == 'HOURS';
    if (setVisible)
    {
      this.maxDailyFrequencyField.show();
    }
    else
    {
      this.maxDailyFrequencyField.setValue(null);
      this.maxDailyFrequencyField.hide();
    }
    this.setMaxFrequencyTitleVisible(setVisible);
  },

  getStart: function()
  {
    var date = this.startDateField.getDate();
    var time = this.startHourField.getTime();
    if (date && time)
    {
      return new Date(date.getFullYear(), date.getMonth(), date.getDate(), time.getHours(), time.getMinutes(), 0, 0);
    }
    return null;
  },

  setStart: function(start)
  {
    var startDate = new Date(start);
    this.startDateField.setDate(start ? startDate : null);
    this.startHourField.setTime(start ? startDate : null);
  },

  getEnd: function()
  {
    if (this.fixedEndDate)
    {
      return this.fixedEndDate;
    }

    //end defined as duration in days
    if (this.cardContainer.getActiveItem() == this.endDaysCard)
    {
      var daysCount = this.endDaysField.getValue();
      if (daysCount)
      {
        var end = this.getStart();
        if (end)
        {
          end.setDate(Number(end.getDate()) + Number(daysCount));
        }
        return end;
      }
      return null;
    }
    //end defined as date
    var endDate = this.endDateField.getDate();
    var endTime = this.endHourField.getTime();
    if (endDate && endTime)
    {
      return new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate(), endTime.getHours(), endTime.getMinutes(), 0, 0);
    }
    return null;
  },

  setEnd: function(end)
  {
    this.fixedEndDate = end ? new Date(end) : null;
    this.endDateField.setDate(null);
    this.endHourField.setTime(null);
    this.endDaysField.setValue(null);
    if (this.fixedEndDate)
    {
      this.untilLinkButton.setText(this.view.getDictionary("until.low.case") + ' ' + Globalize.format(this.fixedEndDate, 'X'));
      this.untilLinkButton.show();
      this.modeButtonGroup.hide();
      this.cardContainer.hide();
    }
    else if (this.isPastMode)
    {
      this.untilLinkButton.hide();
      this.modeButtonGroup.show();
      this.cardContainer.show();
      this.modeButtonGroup.setSelection([this.dateModeButton]);
    }
    else
    {
      this.untilLinkButton.setText(this.view.getDictionary('until.cancellation.lc'));
      this.untilLinkButton.show();
      this.modeButtonGroup.hide();
      this.cardContainer.hide();
    }
  },

  getMaxDailyFrequency: function()
  {
    return this.maxDailyFrequencyField.getValue();
  },

  setMaxDailyFrequency: function(maxDailyFrequency)
  {
    this.maxDailyFrequencyField.setValue(maxDailyFrequency);
  },

  setStartHourEnabled: function(enabled)
  {
    this.startHourField.setEnabled(enabled);
  },

  calculateStart: function(daysOfWeek)
  {
    if (this.getFrequencyKeyFunction)
    {
      var frequencyKey = this.getFrequencyKeyFunction();
      var frequencyModeKey = this.getFrequencyModeFunction();

      var administrationTimestamp =
          tm.views.medications.MedicationTimingUtils.getNextAdministrationTimestamp(
              frequencyKey,
              frequencyModeKey,
              daysOfWeek,
              this.view.getAdministrationTiming());
      if (administrationTimestamp)
      {
        if (frequencyModeKey == "DAYS_ONLY")
        {
          this.startDateField.setDate(administrationTimestamp);
        }
        else
        {
          this.startDateField.setDate(administrationTimestamp);
          this.startHourField.setTime(administrationTimestamp);
          var end = this.getEnd();
          if (end && administrationTimestamp > end)
          {
            this.setEnd(null);
          }
        }
      }
    }
  },

  calculateEnd: function()
  {
    var start = this.getStart();
    if (start)
    {
      if (this.getFrequencyKeyFunction)
      {
        var frequencyKey = this.getFrequencyKeyFunction();
        if (frequencyKey == 'ONCE_THEN_EX')
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
            this.untilLinkButton.hide();
            if (this.untilHideEvent)
            {
              this.untilHideEvent(true);
            }
          }
        }
        else
        {
          if (this.untilLinkButton.isHidden())
          {
            this.setEnd(null);
          }
          if (this.untilHideEvent)
          {
            this.untilHideEvent(false);
          }
        }
      }
    }
  },

  getWhenNeeded: function()
  {
    return this.whenNeededButton.isPressed();
  },

  setWhenNeeded: function(whenNeeded)
  {
    this.setMaxDailyFrequencyFieldVisible(whenNeeded);
    return this.whenNeededButton.setPressed(whenNeeded);
  },

  getStartCriterion: function()
  {
    if (this.byDoctorsOrderButton.isPressed())
    {
      return this.byDoctorsOrderButton.data;
    }
    return null;
  },

  setStartCriterion: function(startCriterion)
  {
    if (startCriterion == this.byDoctorsOrderButton.data)
    {
      this.byDoctorsOrderButton.setPressed(true);
    }
    return null;
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
        if (self.endDateField.getDate() && !self.endHourField.getTime() ||
            !self.endDateField.getDate() && self.endHourField.getTime())
        {
          return null;
        }
        if (endTime && startTime > endTime)
        {
          return null;
        }

        if (startNotBefore)
        {
          if (startTime < startNotBefore)
          {
            return null;
          }
        }
        return true;
      }
    }));
    formFields.push(new tm.jquery.FormField({
      component: self.endDaysField,
      required: true,
      componentValueImplementationFn: function()
      {
        if (self.endDaysField.getValue() && !tm.jquery.Utils.isNumeric(self.endDaysField.getValue()))
        {
          return null;
        }
        return true;
      }
    }));
    return formFields;
  },

  requestFocus: function()
  {
    this.startDateField.focus();
  }
});

