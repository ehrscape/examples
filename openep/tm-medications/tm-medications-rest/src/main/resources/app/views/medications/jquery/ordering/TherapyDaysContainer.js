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

Class.define('app.views.medications.ordering.TherapyDaysContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "therapy-days-container",

  /** configs */
  view: null,
  daysInterval: null,
  daysOfWeek: null,
  selectedInterval: null,
  shownInterval: null,

  /** privates */
  resultCallback: null,
  validationForm: null,
  scrollable: 'visible',
  /** privates: components */
  allDaysButton: null,
  daysOfWeekButton: null,
  daysIntervalButton: null,
  daysButtonGroup: null,
  mondayBtn: null,
  tuesdayBtn: null,
  wednesdayBtn: null,
  thursdayBtn: null,
  fridayBtn: null,
  saturdayBtn: null,
  sundayBtn: null,
  daysIntervalField: null,
  intervalSelectBox: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._buildComponents();
    this._buildGui();
    this._presentValue();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.allDaysButton = new tm.jquery.RadioButton({cls: "all-days-button", checked: true});
    this.daysOfWeekButton = new tm.jquery.RadioButton({cls: "days-of-week-button"});
    this.daysIntervalButton = new tm.jquery.RadioButton({cls: "days-interval-button"});
    this.buttonGroup = new tm.jquery.RadioButtonGroup({
      groupName: "modeGroup",
      radioButtons: [this.allDaysButton, this.daysOfWeekButton, this.daysIntervalButton],
      onChange: function()
      {
        setTimeout(function()
        {
          if (self.buttonGroup.getActiveRadioButton() != self.daysIntervalButton)
          {
            self.daysIntervalField.setValue(null);
          }
          if (self.buttonGroup.getActiveRadioButton() != self.daysOfWeekButton)
          {
            self.daysButtonGroup.clearSelection();
          }
        }, 100);
      }
    });

    this.mondayBtn = this._createDayButton("MONDAY");
    this.tuesdayBtn = this._createDayButton("TUESDAY");
    this.wednesdayBtn = this._createDayButton("WEDNESDAY");
    this.thursdayBtn = this._createDayButton("THURSDAY");
    this.fridayBtn = this._createDayButton("FRIDAY");
    this.saturdayBtn = this._createDayButton("SATURDAY");
    this.sundayBtn = this._createDayButton("SUNDAY");

    this.daysButtonGroup = new tm.jquery.ButtonGroup({
      padding: 3,
      buttons: [this.mondayBtn, this.tuesdayBtn, this.wednesdayBtn, this.thursdayBtn, this.fridayBtn, this.saturdayBtn, this.sundayBtn]});
    this.daysButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      var selectedDays = self.daysButtonGroup.getSelection();
      if (selectedDays.length != 0 && selectedDays.length != 7)
      {
        self.buttonGroup.setActiveRadioButton(self.daysOfWeekButton);
      }
    });

    this.daysIntervalField = tm.views.medications.MedicationUtils.createNumberField('n0', 30, 'days-interval-filed');
    this.daysIntervalField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.daysIntervalField.getValue())
      {
        self.buttonGroup.setActiveRadioButton(self.daysIntervalButton);
      }
    });

    this.intervalSelectBox = new tm.jquery.SelectBox({
      cls: "interval-combo",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "90px"),
      allowSingleDeselect: false,
      multiple: false,
      options: this._createIntervalSelectBoxOptions(),
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
      }
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

  _createIntervalSelectBoxOptions: function()
  {
    var dayOption = tm.jquery.SelectBox.createOption(
        {id: 0, interval: "day", multiplier: 1},
        this.view.getDictionary("day.lc"),
        null,
        null,
        true);

    var weekOption = tm.jquery.SelectBox.createOption(
        {id: 1, interval: "week", multiplier: 7},
        this.view.getDictionary("week.lc"),
        null,
        null,
        false);

    var monthOption = tm.jquery.SelectBox.createOption(
        {id: 2, interval: "month", multiplier: 30},
        this.view.getDictionary("month.lc"),
        null,
        null,
        false);
    return [dayOption, weekOption, monthOption];
  },

  _createDayButton: function(day)
  {
    var dayOfWeekString = tm.views.medications.MedicationTimingUtils.getDayOfWeekDisplay(this.view, day, true);
    return new tm.jquery.Button({cls: day.toLowerCase() + "-day-button", data: day, text: dayOfWeekString});
  },

  _buildGui: function()
  {
    var allDaysContainer = new tm.jquery.Container({height: 30, layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 5)});
    allDaysContainer.add(this.allDaysButton);
    allDaysContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('all.days.selected'), '4 0 0 3'));
    this.add(allDaysContainer);
    this.add(this._createSeparator());

    var daysOfWeekContainer = new tm.jquery.Container({height: 35, layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 5)});
    daysOfWeekContainer.add(this.daysOfWeekButton);
    daysOfWeekContainer.add(this.daysButtonGroup);
    this.add(daysOfWeekContainer);
    this.add(this._createSeparator());

    var daysIntervalContainer = new tm.jquery.Container({
      height: 30,
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 5),
      scrollable: 'visible'
    });
    daysIntervalContainer.add(this.daysIntervalButton);
    daysIntervalContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('on'), '4 0 0 3'));
    daysIntervalContainer.add(this.daysIntervalField);
    daysIntervalContainer.add(this.intervalSelectBox);
    this.add(daysIntervalContainer);
    this.add(this._createSeparator());
  },

  _createSeparator: function()
  {
    return new tm.jquery.Container({cls: 'separator-container', margin: "5 0 5 0"});
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();

    if (this.buttonGroup.getActiveRadioButton() == this.daysIntervalButton)
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: this.daysIntervalField,
        required: true,
        componentValueImplementationFn: function()
        {
          var daysInterval = self.daysIntervalField.getValue();
          if (daysInterval)
          {
            return true;
          }
          return null;
        }
      }));
    }
  },

  _presentValue: function()
  {
    if (this.daysInterval)
    {
      this.buttonGroup.setActiveRadioButton(this.daysIntervalButton);
      if (!tm.jquery.Utils.isEmpty(this.shownInterval) && !tm.jquery.Utils.isEmpty(this.selectedInterval))
      {
        this.daysIntervalField.setValue(this.shownInterval);
        this.intervalSelectBox.setSelections([this.selectedInterval]);
      }
      else
      {
        this.daysIntervalField.setValue(this.daysInterval);
        this.intervalSelectBox.setSelections([this.intervalSelectBox.getOptions[0]]);
      }
    }
    else if (this.daysOfWeek && this.daysOfWeek.length > 0)
    {
      this.buttonGroup.setActiveRadioButton(this.daysOfWeekButton);
      var buttons = this.daysButtonGroup.getButtons();
      var selectedButtons = [];
      for (var i = 0; i < this.daysOfWeek.length; i++)
      {
        for (var j = 0; j < buttons.length; j++)
        {
          if (buttons[j].data == this.daysOfWeek[i])
          {
            selectedButtons.push(buttons[j]);
            break;
          }
        }
      }
      this.daysButtonGroup.setSelection(selectedButtons);
    }
  },

  _getSelectedDays: function()
  {
    var selectedDays = [];
    var selectedButtons = this.daysButtonGroup.getSelection();
    for (var i = 0; i < selectedButtons.length; i++)
    {
      selectedDays.push(selectedButtons[i].data);
    }
    return selectedDays;
  },

  _getValue: function()
  {
    var activeButton = this.buttonGroup.getActiveRadioButton();
    if (activeButton == this.allDaysButton)
    {
      return {type: "ALL_DAYS"};
    }
    else if (activeButton == this.daysOfWeekButton)
    {
      var selectedDays = this._getSelectedDays();
      if (selectedDays.length == 0 || selectedDays.length == 7)
      {
        return {type: "ALL_DAYS"};
      }
      return {
        type: "DAYS_OF_WEEK",
        daysOfWeek: selectedDays};
    }
    else
    {
      var daysInterval = this.daysIntervalField.getValue();
      var selectedInterval = this.intervalSelectBox.getSelections().length > 0 ?
          this.intervalSelectBox.getSelections()[0] :
          this.intervalSelectBox.getOptions()[0];
      var multiplier = selectedInterval.multiplier;
      if (daysInterval == 1 && selectedInterval.id == 0)
      {
        return {type: "ALL_DAYS"};
      }
      return {
        type: "DAYS_INTERVAL",
        daysInterval: daysInterval * multiplier,
        shownInterval: daysInterval,
        selectedInterval: selectedInterval
      };
    }
  },

  _returnResult: function()
  {
    var value = this._getValue();
    this.resultCallback(new app.views.common.AppResultData({success: true, value: value}));
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit();
  }
});

