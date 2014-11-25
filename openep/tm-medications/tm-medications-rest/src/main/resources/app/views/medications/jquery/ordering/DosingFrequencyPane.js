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

Class.define('app.views.medications.ordering.DosingFrequencyPane', 'tm.jquery.Container', {
  cls: "dosing-frequency-pane",

  /** configs */
  view: null,
  frequencyChangeEvent: null, //optional
  setMaxDailyFrequencyFieldsVisibleFunction: null, //optional
  withSingleFrequencies: true,
  /** privates*/
  mode: null,  // 'HOURS', 'COUNT'
  daysOfWeek: null,
  daysInterval: null,
  therapyApplicationCondition: null,
  /** privates: components */
  cardContainer: null,
  buttonsContainer: null,
  labelsContainer: null,
  button1xEx: null,
  button1xExSpacer: null,
  timeOfDayButtonGroup: null,
  timeOfDayButtonGroupSpacer: null,
  buttonMorning: null,
  buttonNoon: null,
  buttonEvening: null,
  hoursCard: null,
  hoursButtonGroup: null,
  button6h: null,
  button8h: null,
  button12h: null,
  button24h: null,
  hoursField: null,
  countCard: null,
  countButtonGroup: null,
  button1x: null,
  button2x: null,
  button3x: null,
  button4x: null,
  countField: null,
  modeButtonGroup: null,
  modeButtonGroupSpacer: null,
  countModeButton: null,
  hoursModeButton: null,
  daysSpacer: null,
  daysButton: null,
  daysLabel: null,
  conditionsSpacer: null,
  conditionsButton: null,
  conditionsLabel: null,

  BUTTON_WIDTH: 40,

  /** constructor */
  Constructor: function(config)
  {
    this.daysOfWeek = [];
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "start"));
    this._buildComponents();
    this._buildGui();
    this.mode = 'COUNT';
    this._setModeFromContext();
  },

  /** private methods */
  _buildDosingFrequency: function(type, value)
  {
    return {
      type: type,
      value: value
    }
  },

  _buildComponents: function()
  {
    var self = this;
    this.button1xEx = new tm.jquery.ToggleButton({text: '1ex'});
    this.button1xEx.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (self.button1xEx.isPressed())
      {
        self._clearOther(self.button1xEx);
        self._triggerChangeEvent();
      }
    });

    this.buttonMorning = new tm.jquery.Button({data: 'MORNING', text: this.view.getDictionary("in.morning").substring(0, 3)});
    this.buttonNoon = new tm.jquery.Button({data: 'NOON', text: this.view.getDictionary("at.noon").substring(0, 2)});
    this.buttonEvening = new tm.jquery.Button({data: 'EVENING', text: this.view.getDictionary("in.evening").substring(0, 3)});

    this.timeOfDayButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      buttons: [this.buttonMorning, this.buttonNoon, this.buttonEvening]
    });
    this.timeOfDayButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.timeOfDayButtonGroup.getSelection().length > 0)
      {
        self._clearOther(self.timeOfDayButtonGroup);
        self._triggerChangeEvent();
      }
    });

    this.cardContainer = new tm.jquery.CardContainer({
      width: this.withSingleFrequencies ? 220 : 185,
      height: 30,
      animation: 'slide-vertical'
    });

    this.button6h = new tm.jquery.Button({data: 6, text: '6h', width: this.BUTTON_WIDTH});
    this.button8h = new tm.jquery.Button({data: 8, text: '8h', width: this.BUTTON_WIDTH});
    this.button12h = new tm.jquery.Button({data: 12, text: '12h', width: this.BUTTON_WIDTH});
    this.button24h = new tm.jquery.Button({data: 24, text: '24h', width: this.BUTTON_WIDTH});

    this.hoursButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      buttons: this.withSingleFrequencies ?
          [this.button6h, this.button8h, this.button12h, this.button24h] :
          [this.button6h, this.button8h, this.button12h]
    });
    this.hoursButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.hoursButtonGroup.getSelection().length > 0)
      {
        self._clearOther(self.hoursButtonGroup);
        self._triggerChangeEvent();
      }
    });

    this.hoursField = new tm.jquery.TextField({width: 45});
    this.hoursField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.hoursField.getValue())
      {
        self._clearOther(self.hoursField);
        self._triggerChangeEvent();
      }
    });
    this.hoursField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.countModeButton.focus();
    });

    this.button1x = new tm.jquery.Button({data: 1, text: '1X', width: this.BUTTON_WIDTH});
    this.button2x = new tm.jquery.Button({data: 2, text: '2X', width: this.BUTTON_WIDTH});
    this.button3x = new tm.jquery.Button({data: 3, text: '3X', width: this.BUTTON_WIDTH});
    this.button4x = new tm.jquery.Button({data: 4, text: '4X', width: this.BUTTON_WIDTH});

    this.countButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      height: 30,
      buttons: this.withSingleFrequencies ?
          [this.button1x, this.button2x, this.button3x, this.button4x] :
          [this.button2x, this.button3x, this.button4x]
    });
    this.countButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.countButtonGroup.getSelection().length > 0)
      {
        self._clearOther(self.countButtonGroup);
        self._triggerChangeEvent();
      }
    });

    this.countField = new tm.jquery.TextField({width: 45});
    this.countField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.countField.getValue())
      {
        self._clearOther(self.countField);
      }
      self._triggerChangeEvent();
    });
    this.countField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.countModeButton.focus();
    });

    this.countModeButton = new tm.jquery.Button({text: 'X', pressed: true});
    this.hoursModeButton = new tm.jquery.Button({text: 'h'});

    this.modeButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      buttons: [this.countModeButton, this.hoursModeButton]
    });

    this.modeButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._modeButtonGroupChanged();
      self._triggerChangeEvent();
    });

    this.daysButton = new tm.jquery.Button({
      cls: 'show-days-icon',
      handler: function()
      {
        self._openDaysPane();
      }});
    this.daysLabel = new tm.jquery.Container({cls: 'TextData'});
    if (this.withSingleFrequencies)
    {
      this.button1xExSpacer = this._createHorizontalSpacer();
      this.timeOfDayButtonGroupSpacer = this._createHorizontalSpacer();
    }
    this.modeButtonGroupSpacer = this._createHorizontalSpacer();
    this.daysSpacer = this._createHorizontalSpacer();

    this.conditionsSpacer = this._createHorizontalSpacer();
    this.conditionsButton = new tm.jquery.Button({
      cls: 'conditions-icon',
      handler: function()
      {
        self._openConditionsPane();
      }});
    this.conditionsLabel = new tm.jquery.Container({cls: 'TextData'});
  },

  _buildGui: function()
  {
    this.buttonsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5)});
    this.add(this.buttonsContainer);
    if (this.withSingleFrequencies)
    {
      this.buttonsContainer.add(this.button1xEx);
      this.buttonsContainer.add(this.button1xExSpacer);
      this.buttonsContainer.add(this.timeOfDayButtonGroup);
      this.buttonsContainer.add(this.timeOfDayButtonGroupSpacer);
    }

    this.countCard = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5)});
    this.countCard.add(this.countButtonGroup);
    this.countCard.add(this.countField);
    this.cardContainer.add(this.countCard);

    this.hoursCard = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5)});
    this.hoursCard.add(this.hoursButtonGroup);
    this.hoursCard.add(this.hoursField);
    this.cardContainer.add(this.hoursCard);

    this.buttonsContainer.add(this.cardContainer);
    this.buttonsContainer.add(this.modeButtonGroupSpacer);
    this.buttonsContainer.add(this.modeButtonGroup);
    this.buttonsContainer.add(this.daysSpacer);

    if (this.withSingleFrequencies)
    {
      this.buttonsContainer.add(this.daysButton);
      this.buttonsContainer.add(this.conditionsSpacer);
      this.buttonsContainer.add(this.conditionsButton);
      //this.buttonsContainer.add(this._createHorizontalSpacer());
      this.labelsContainer = new tm.jquery.Container({
        layout: tm.jquery.VFlexboxLayout.create("start", "start", 1),
        padding: "7 0 0 0"
      });
      this.add(this.labelsContainer);
      this.labelsContainer.add(this.daysLabel);
      this.labelsContainer.add(this.conditionsLabel);
    }
  },

  _createHorizontalSpacer: function()
  {
    return new tm.jquery.Spacer({type: 'horizontal', size: 5});
  },

  _triggerChangeEvent: function()
  {
    if (this.frequencyChangeEvent)
    {
      this.frequencyChangeEvent();
    }
  },

  _modeButtonGroupChanged: function()
  {
    var selectedButton = this.modeButtonGroup.getSelection()[0];
    if (selectedButton == this.countModeButton)
    {
      this.mode = 'COUNT';
      this.cardContainer.setActiveItem(this.countCard);
      if (this.hoursButtonGroup.getSelection().length > 0)
      {
        this.hoursButtonGroup.clearSelection();
      }
      if (this.hoursField.getValue())
      {
        this.hoursField.setValue(null)
      }
      if (this.setMaxDailyFrequencyFieldsVisibleFunction)
      {
        this.setMaxDailyFrequencyFieldsVisibleFunction(false);
      }
    }
    else
    {
      this.mode = 'HOURS';
      this.cardContainer.setActiveItem(this.hoursCard);
      if (this.countButtonGroup.getSelection().length > 0)
      {
        this.countButtonGroup.clearSelection();
      }
      if (this.countField.getValue())
      {
        this.countField.setValue(null)
      }
      if (this.setMaxDailyFrequencyFieldsVisibleFunction)
      {
        this.setMaxDailyFrequencyFieldsVisibleFunction(true);
      }
    }
    this._setContext();
  },

  _setContext: function()
  {
    var context = this.view.getContext();
    if (!context)
    {
      this.view.setContext({});
    }
    this.view.getContext().dosingFrequencyMode = this.mode;
  },

  _openConditionsPane: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var conditionsPane = new app.views.medications.ordering.TherapyApplicationConditionPane({
      view: this.view,
      therapyApplicationCondition: this.therapyApplicationCondition,
      padding: 5
    });

    var conditionsDialog = appFactory.createDataEntryDialog(
        this.view.getDictionary("therapy.application.conditions"),
        null,
        conditionsPane,
        function(resultData)
        {
          if (resultData)
          {
            self.therapyApplicationCondition = resultData.selection;
            self._setConditionLabel();
          }
        },
        340,
        200
    );
    conditionsDialog.show();
  },

  _openDaysPane: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var daysPane = new app.views.medications.ordering.TherapyDaysContainer({
      view: self.view,
      startProcessOnEnter: true,
      padding: 5,
      daysInterval: this.daysInterval,
      daysOfWeek: this.daysOfWeek
    });

    var daysDialog = appFactory.createDataEntryDialog(
        this.view.getDictionary("chosen.days"),
        null,
        daysPane,
        function(resultData)
        {
          if (resultData)
          {
            if (resultData.value.type == 'ALL_DAYS')
            {
              self.daysOfWeek.removeAll();
              self.daysInterval = null;
            }
            else if (resultData.value.type == 'DAYS_OF_WEEK')
            {
              self.daysOfWeek = resultData.value.daysOfWeek;
              self.daysInterval = null;
            }
            else if (resultData.value.type == 'DAYS_INTERVAL')
            {
              self.daysOfWeek.removeAll();
              self.daysInterval = resultData.value.daysInterval;
            }
            self._setDaysOfWeekLabel();
            self._triggerChangeEvent();
          }
        },
        400,
        250
    );
    daysDialog.show();
  },

  _setDaysOfWeekLabel: function()
  {
    if (this.daysInterval)
    {
      this.daysLabel.setHtml(this.view.getDictionary("chosen.days") + ": " +
          this.view.getDictionary("every") + " " + this.daysInterval + ". " +
          this.view.getDictionary("day.lc"));
    }
    else if (this.daysOfWeek.length > 0)
    {
      var daysDisplay = this.view.getDictionary("chosen.days") + ": ";
      for (var i = 0; i < this.daysOfWeek.length; i++)
      {
        daysDisplay += tm.views.medications.MedicationTimingUtils.getDayOfWeekDisplay(this.view, this.daysOfWeek[i], true);
        if (i < this.daysOfWeek.length - 1)
        {
          daysDisplay += ', ';
        }
      }
      this.daysLabel.setHtml(daysDisplay);
      this.daysLabel.setTooltip(tm.views.medications.MedicationUtils.createTooltip(daysDisplay, null, this.view));
    }
    else
    {
      this.daysLabel.setHtml("");
    }
  },

  _setConditionLabel: function()
  {
    if (this.therapyApplicationCondition)
    {
      this.conditionsLabel.setHtml(this.view.getDictionary("medication.start.criterion") + ": " +
          this.view.getDictionary('MedicationStartCriterionEnum.' + this.therapyApplicationCondition));
    }
    else
    {
      this.conditionsLabel.setHtml("");
    }
  },

  _clearOther: function(component)
  {
    if (this.button1xEx != component)
    {
      this.button1xEx.setPressed(false);
    }
    if (this.timeOfDayButtonGroup != component)
    {
      this.timeOfDayButtonGroup.clearSelection();
    }
    if (this.countButtonGroup != component)
    {
      this.countButtonGroup.clearSelection();
    }
    if (this.hoursButtonGroup != component)
    {
      this.hoursButtonGroup.clearSelection();
    }
    if (this.hoursField != component)
    {
      this.hoursField.setValue(null);
    }
    if (this.countField != component)
    {
      this.countField.setValue(null);
    }
  },

  _setModeFromContext: function()
  {
    var context = this.view.getContext();
    if (context && this.mode != context.dosingFrequencyMode)
    {
      if (context.dosingFrequencyMode == "HOURS")
      {
        this.modeButtonGroup.setSelection([this.hoursModeButton]);
      }
      else
      {
        this.modeButtonGroup.setSelection([this.countModeButton]);
      }
      this._modeButtonGroupChanged();
    }
  },

  /** public methods */
  clear: function()
  {
    this.showAllFields();
    this.button1xEx.show();
    this.timeOfDayButtonGroup.show();
    this.cardContainer.show();
    this.modeButtonGroup.show();
    this.daysOfWeek = [];
    this.daysInterval = null;
    this.button1xEx.setPressed(false);
    this.timeOfDayButtonGroup.clearSelection();
    this._setModeFromContext();
    this.hoursButtonGroup.clearSelection();
    this.countButtonGroup.clearSelection();
    this.hoursField.setValue(null);
    this.countField.setValue(null);
    this.daysLabel.setHtml("");
    this.therapyApplicationCondition = null;
    this.conditionsLabel.setHtml("");
  },

  getFrequency: function()
  {
    var enums = app.views.medications.TherapyEnums;
    if (this.button1xEx.isPressed())
    {
      return this._buildDosingFrequency('ONCE_THEN_EX', null);
    }
    if (this.timeOfDayButtonGroup.getSelection().length > 0)
    {
      var timeOfDayButton = this.timeOfDayButtonGroup.getSelection()[0];
      return this._buildDosingFrequency(timeOfDayButton.data)
    }
    if (this.mode == 'COUNT')
    {
      if (this.countButtonGroup.getSelection().length > 0)
      {
        var countButton = this.countButtonGroup.getSelection()[0];
        return this._buildDosingFrequency(enums.dosingFrequencyTypeEnum.DAILY_COUNT, countButton.data)
      }
      if (this.countField.getValue())
      {
        return this._buildDosingFrequency(enums.dosingFrequencyTypeEnum.DAILY_COUNT, this.countField.getValue())
      }
      return null;
    }
    if (this.mode == 'HOURS')
    {
      if (this.hoursButtonGroup.getSelection().length > 0)
      {
        var hoursButton = this.hoursButtonGroup.getSelection()[0];
        return this._buildDosingFrequency(enums.dosingFrequencyTypeEnum.BETWEEN_DOSES, hoursButton.data);
      }
      if (this.hoursField.getValue())
      {
        return this._buildDosingFrequency(enums.dosingFrequencyTypeEnum.BETWEEN_DOSES, this.hoursField.getValue());
      }
    }
    return null;
  },

  getFrequencyKey: function()
  {
    var dosingFrequency = this.getFrequency();
    return tm.views.medications.MedicationTimingUtils.getFrequencyKey(dosingFrequency);
  },

  getFrequencyTimesPerDay: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var dosingFrequency = this.getFrequency();
    if (dosingFrequency)
    {
      if (dosingFrequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        return 24 / dosingFrequency.value;
      }
      if (dosingFrequency.type == enums.dosingFrequencyTypeEnum.DAILY_COUNT)
      {
        return dosingFrequency.value;
      }
      return 1;
    }
    return null;
  },

  getDaysOfWeek: function()
  {
    return this.daysOfWeek;
  },

  getFrequencyType: function()
  {
    var frequency = this.getFrequency();
    if (frequency)
    {
      return frequency.type;
    }
    else if (this.daysOfWeek.length > 0)
    {
      return "DAYS_ONLY";
    }
    return null;
  },

  getFrequencyMode: function()
  {
     return this.mode;
  },

  setDaysOfWeek: function(daysOfWeek)
  {
    this.daysOfWeek = daysOfWeek ? daysOfWeek : [];
    this._setDaysOfWeekLabel();
  },

  getDaysFrequency: function()
  {
    return this.daysInterval;
  },

  setDaysFrequency: function(daysInterval)
  {
    this.daysInterval = daysInterval;
    this._setDaysOfWeekLabel();
  },

  getTherapyApplicationCondition: function()
  {
    return this.therapyApplicationCondition;
  },

  setTherapyApplicationCondition: function(therapyApplicationCondition)
  {
    this.therapyApplicationCondition = therapyApplicationCondition;
    this._setConditionLabel(therapyApplicationCondition);
  },

  setFrequency: function(frequency)
  {
    var enums = app.views.medications.TherapyEnums;
    if (frequency)
    {
      if (frequency.type == enums.dosingFrequencyTypeEnum.ONCE_THEN_EX)
      {
        this.button1xEx.setPressed(true);
      }
      else if (frequency.type == enums.dosingFrequencyTypeEnum.MORNING ||
          frequency.type == enums.dosingFrequencyTypeEnum.NOON ||
          frequency.type == enums.dosingFrequencyTypeEnum.EVENING)
      {
        var timeOfDayButtons = this.timeOfDayButtonGroup.getButtons();
        for (var k = 0; k < timeOfDayButtons.length; k++)
        {
          if (timeOfDayButtons[k].data == frequency.type)
          {
            this.timeOfDayButtonGroup.setSelection([timeOfDayButtons[k]]);
          }
        }
      }
      else if (frequency.type == enums.dosingFrequencyTypeEnum.DAILY_COUNT)
      {
        this.modeButtonGroup.setSelection([this.countModeButton]);
        var countButtons = this.countButtonGroup.getButtons();
        var countValueInButtons = false;
        for (var i = 0; i < countButtons.length; i++)
        {
          if (countButtons[i].data == frequency.value)
          {
            countValueInButtons = true;
            this.countButtonGroup.setSelection([countButtons[i]]);
          }
        }
        if (!countValueInButtons)
        {
          this.countButtonGroup.clearSelection();
          this.countField.setValue(frequency.value);
        }
        this._triggerChangeEvent();
      }
      else if (frequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        this.modeButtonGroup.setSelection([this.hoursModeButton]);
        this._modeButtonGroupChanged();
        var hoursButtons = this.hoursButtonGroup.getButtons();
        var hoursValueInButtons = false;
        for (var j = 0; j < hoursButtons.length; j++)
        {
          if (hoursButtons[j].data == frequency.value)
          {
            hoursValueInButtons = true;
            this.hoursButtonGroup.setSelection([hoursButtons[j]]);
          }
        }
        if (!hoursValueInButtons)
        {
          this.hoursButtonGroup.clearSelection();
          this.hoursField.setValue(frequency.value);
        }
        this._triggerChangeEvent();
      }
    }
    else
    {
      this.clear();
      this._triggerChangeEvent();
    }
  },

  getDosingFrequencyPaneValidations: function()
  {
    var self = this;
    var formFields = [];
    if (!self.cardContainer.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self,
        required: true,
        componentValueImplementationFn: function()
        {
          return self.getFrequency();
        }
      }));
      formFields.push(new tm.jquery.FormField({
        component: self.countField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.countField.getValue();

          if (value && (!tm.jquery.Utils.isNumeric(value) || value <= 0))
          {
            return null;
          }
          return true;
        }
      }));
      formFields.push(new tm.jquery.FormField({
        component: self.hoursField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.hoursField.getValue();

          if (value && (!tm.jquery.Utils.isNumeric(value) || value <= 0))
          {
            return null;
          }
          return true;
        }
      }));
    }
    return formFields;
  },

  requestFocus: function()
  {
    this.button1xEx.focus();
  },

  showDaysOnly: function()
  {
    this.button1xEx.setPressed(false);
    this.timeOfDayButtonGroup.clearSelection();
    this.modeButtonGroup.setSelection([this.countModeButton]);
    this.hoursButtonGroup.clearSelection();
    this.countButtonGroup.clearSelection();
    this.hoursField.setValue(null);
    this.countField.setValue(null);
    this.button1xEx.hide();
    this.timeOfDayButtonGroup.hide();
    this.cardContainer.hide();
    this.modeButtonGroup.hide();
    this.daysSpacer.hide();
    this.modeButtonGroupSpacer.hide();
    if (this.timeOfDayButtonGroupSpacer)
    {
      this.timeOfDayButtonGroupSpacer.hide();
    }
    if (this.button1xExSpacer)
    {
      this.button1xExSpacer.hide();
    }
  },

  showAllFields: function()
  {
    this.button1xEx.show();
    this.timeOfDayButtonGroup.show();
    this.cardContainer.show();
    this.modeButtonGroup.show();
    this.daysSpacer.show();
    this.modeButtonGroupSpacer.show();
    if (this.timeOfDayButtonGroupSpacer)
    {
      this.timeOfDayButtonGroupSpacer.show();
    }
    if (this.button1xExSpacer)
    {
      this.button1xExSpacer.show();
    }
  }
});

