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
  editMode: false,
  allowMoreThan24hFrequency: true,
  /** privates*/
  mode: null,  // 'HOURS', 'COUNT'
  daysOfWeek: null,
  daysInterval: null,
  selectedInterval: null,
  shownInterval: null,
  applicationPrecondition: null,
  /** privates: components */
  cardContainer: null,
  buttonsContainer: null,
  labelsContainer: null,
  button1xExButtonGroup: null,
  button1xEx: null,
  timeOfDayButtonGroup: null,
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
  countModeButton: null,
  hoursModeButton: null,
  daysButton: null,
  daysLabel: null,
  preconditionButton: null,
  preconditionLabel: null,
  dosingPatternPane: null,
  onModeChangeEvent: null, //optional
  _frequencyFieldValidationForm: null,

  visibilityContext: null,

  BUTTON_WIDTH: 40,

  _triggerChangeEventConditionTask: null,

  /** constructor */
  Constructor: function(config)
  {
    this.daysOfWeek = [];
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"));
    this._buildComponents();
    this._buildGui();
    this.mode = 'COUNT';
    if (!this.editMode)
    {
      this._setModeFromContext();
    }
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
    this.dosingPatternPane = new app.views.medications.ordering.DosingPatternPane({
      view: this.view,
      hidden: true,
      scrollable: "visible",
      getFrequencyKeyFunction: function()
      {
        return self.getFrequencyKey();
      },
      getFrequencyTypeFunction: function()
      {
        return self.getFrequencyType();
      },
      patternChangedEvent: function()
      {
        if (self.frequencyChangeEvent)
        {
          self.frequencyChangeEvent();
        }
      }
    });
    this.button1xEx = new tm.jquery.ToggleButton({cls: "button1xEx", text: '1ex'});

    this.button1xExButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      buttons: [this.button1xEx]
    });

    this.button1xExButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.button1xExButtonGroup.getSelection().length > 0)
      {
        self._clearOther(self.button1xExButtonGroup);
        self._triggerChangeEvent(self.button1xExButtonGroup.getSelection()[0]);
        self._frequencyFieldValidationForm.reset();
      }
    });

    this.buttonMorning = new tm.jquery.Button({
      cls: "morning-button", data: 'MORNING', text: this.view.getDictionary("in.morning").substring(0, 3)
    });
    this.buttonNoon = new tm.jquery.Button({
      cls: "noon-button", data: 'NOON', text: this.view.getDictionary("at.noon").substring(0, 2)
    });
    this.buttonEvening = new tm.jquery.Button({
      cls: "evening-button", data: 'EVENING', text: this.view.getDictionary("in.evening").substring(0, 3)
    });

    this.timeOfDayButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      buttons: [this.buttonMorning, this.buttonNoon, this.buttonEvening]
    });
    this.timeOfDayButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.timeOfDayButtonGroup.getSelection().length > 0)
      {
        self._clearOther(self.timeOfDayButtonGroup);
        self._triggerChangeEvent(self.timeOfDayButtonGroup.getSelection()[0]);
        self._frequencyFieldValidationForm.reset();
      }
    });

    this.cardContainer = new tm.jquery.CardContainer({
      cls: 'frequency-card-container',
      width: this.withSingleFrequencies ? 220 : 185,
      height: 30,
      animation: 'slide-vertical'
    });

    this.button6h = new tm.jquery.Button({cls: "button6h", data: 6, text: '6h', width: this.BUTTON_WIDTH});
    this.button8h = new tm.jquery.Button({cls: "button8h", data: 8, text: '8h', width: this.BUTTON_WIDTH});
    this.button12h = new tm.jquery.Button({cls: "button12h", data: 12, text: '12h', width: this.BUTTON_WIDTH});
    this.button24h = new tm.jquery.Button({cls: "button24h", data: 24, text: '24h', width: this.BUTTON_WIDTH});

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
        self._triggerChangeEvent(self.hoursButtonGroup.getSelection()[0]);
        self._frequencyFieldValidationForm.reset();
      }
    });

    this.hoursField = new tm.jquery.TextField({cls: "hours-field", width: 45});
    this.hoursField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._validateFrequencyInputField(self.hoursField);
    });
    this.hoursField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.countModeButton.focus();
    });

    this.button1x = new tm.jquery.Button({cls: "button1x", data: 1, text: '1X', width: this.BUTTON_WIDTH});
    this.button2x = new tm.jquery.Button({cls: "button2x", data: 2, text: '2X', width: this.BUTTON_WIDTH});
    this.button3x = new tm.jquery.Button({cls: "button3x", data: 3, text: '3X', width: this.BUTTON_WIDTH});
    this.button4x = new tm.jquery.Button({cls: "button4x", data: 4, text: '4X', width: this.BUTTON_WIDTH});

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
        self._triggerChangeEvent(self.countButtonGroup.getSelection()[0]);
        self._frequencyFieldValidationForm.reset();
      }
    });

    this.countField = new tm.jquery.TextField({cls: "count-field", width: 45});
    this.countField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._validateFrequencyInputField(self.countField);
    });
    this.countField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.countModeButton.focus();
    });

    this.countModeButton = new tm.jquery.Button({cls: "count-mode-button", text: 'X', pressed: true});
    this.hoursModeButton = new tm.jquery.Button({cls: "hours-mode-button", text: 'h'});

    this.modeButtonGroup = new tm.jquery.ButtonGroup({
      cls: 'mode-button-group',
      type: 'radio',
      buttons: [this.countModeButton, this.hoursModeButton]
    });

    this.modeButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._modeButtonGroupChanged();
      if (self.onModeChangeEvent)
      {
        self.onModeChangeEvent();
      }
      self._triggerChangeEvent(self.modeButtonGroup.getSelection()[0]);
    });

    this.daysButton = new tm.jquery.Button({
      cls: 'show-days-icon days-button',
      handler: function()
      {
        self._openDaysPane();
      }});
    this.daysLabel = new tm.jquery.Container({cls: 'TextData days-label'});
    this.preconditionButton = new tm.jquery.Button({
      cls: 'conditions-icon preconditions-button',
      handler: function()
      {
        self._openApplicationPreconditionsPane();
      }});
    this.preconditionLabel = new tm.jquery.Container({cls: 'TextData'});
    this._frequencyFieldValidationForm = new tm.jquery.Form({
      requiredFieldValidatorErrorMessage: self.allowMoreThan24hFrequency ? 
          self.view.getDictionary('invalid.dosing.frequency.number.format') :
          self.view.getDictionary('invalid.dosing.frequency.number.range') 
    });
  },

  _buildGui: function()
  {
    this.buttonsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)});
    this.add(this.buttonsContainer);
    if (this.withSingleFrequencies)
    {
      this.buttonsContainer.add(this.button1xExButtonGroup);
      this.buttonsContainer.add(this.timeOfDayButtonGroup);
    }

    this.countCard = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)});
    this.countCard.add(this.countButtonGroup);
    this.countCard.add(this.countField);
    this.cardContainer.add(this.countCard);

    this.hoursCard = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)});
    this.hoursCard.add(this.hoursButtonGroup);
    this.hoursCard.add(this.hoursField);
    this.cardContainer.add(this.hoursCard);

    this.buttonsContainer.add(this.cardContainer);
    this.buttonsContainer.add(this.modeButtonGroup);

    if (this.withSingleFrequencies)
    {
      this.buttonsContainer.add(this.daysButton);
      this.buttonsContainer.add(this.preconditionButton);
      if (!(this.visibilityContext &&
            this.visibilityContext.hasOwnProperty("dosingPatternPaneVisible") &&
            this.visibilityContext.dosingPatternPaneVisible === false))
      {
        this.add(this.dosingPatternPane);
      }
      this.labelsContainer = new tm.jquery.Container({
        layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 1)
      });
      this.add(this.labelsContainer);
      this.labelsContainer.add(this.daysLabel);
      this.labelsContainer.add(this.preconditionLabel);
    }
  },

  _triggerChangeEvent: function(sender, preventPatternRefresh, preventFrequencyChangeEvent)
  {
    var appFactory = this.view.getAppFactory();
    var self = this;
    if (!(this.visibilityContext &&
        this.visibilityContext.hasOwnProperty("dosingPatternPaneVisible") &&
        this.visibilityContext.dosingPatternPaneVisible === false) &&
        this.withSingleFrequencies && !this.dosingPatternPane.isHidden())
    {

      if (!tm.jquery.Utils.isEmpty(this._triggerChangeEventConditionTask))
      {
        this._triggerChangeEventConditionTask.abort();
      }

      this._triggerChangeEventConditionTask = appFactory.createConditionTask(
          function()
          {
            self._triggerChangeEventImpl(sender, preventPatternRefresh, preventFrequencyChangeEvent);
            self._triggerChangeEventConditionTask = null;
          },
          function()
          {
            return self.dosingPatternPane.isRendered() && $(self.dosingPatternPane.getDom()).is(':visible');
          },
          50, 100);
    }
    else
    {
      self._triggerChangeEventImpl(sender, preventPatternRefresh, preventFrequencyChangeEvent);
    }
  },

  _triggerChangeEventImpl: function(sender, preventPatternRefresh, preventFrequencyChangeEvent)
  {
    if (this.dosingPatternPane)
    {
      if ([this.countModeButton, this.hoursModeButton].indexOf(sender) < 0)
      {
        var $dosingPattern = $(this.dosingPatternPane.getDom());
        $dosingPattern.removeClass("left-sided");

        if (!preventPatternRefresh)
        {
          $dosingPattern.css("visibility", "hidden");
          this.dosingPatternPane.refreshDosingPattern();
        }
        if (sender)
        {
          var $sender = $(sender.getDom());
          var $dom = $(this.getDom());

          if (this.button1xExButtonGroup.getSelection().length > 0 && sender == this.button1xExButtonGroup.getSelection()[0])
          {
            $dosingPattern.addClass("left-sided");
          }
          $dosingPattern.css("visibility", "hidden");
          $dosingPattern.css("margin-left", ""); // reset to get the proper size

          var paneWidth = $dosingPattern.outerWidth();
          var domLeftOffset = $dom.offset().left;
          var senderLeftOffset = $sender.offset().left;
          var senderOuterWidth = $sender.outerWidth();

          var freeRightSpace = $dom.width() - senderLeftOffset + domLeftOffset - (senderOuterWidth / 2);
          // we need to check and see how much space there's left to the right - since we want to move the
          // pane so that it's centered under the sender
          paneWidth = paneWidth / 2 > freeRightSpace ? 2 * freeRightSpace : paneWidth;
          var marginLeft = senderLeftOffset - domLeftOffset - (paneWidth / 2) + (senderOuterWidth / 2);

          if (marginLeft > 0) $dosingPattern.css("margin-left", marginLeft + "px");
        }

        $dosingPattern.css("visibility", "");
      }
      else
      {
        this.dosingPatternPane.refreshDosingPattern();
      }
    }
    if (this.frequencyChangeEvent && !preventFrequencyChangeEvent)
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
    this._frequencyFieldValidationForm.reset();
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

  _openApplicationPreconditionsPane: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var preconditionsPane = new app.views.medications.ordering.ApplicationPreconditionPane({
      view: this.view,
      applicationPrecondition: this.applicationPrecondition,
      padding: 5
    });

    var conditionsDialog = appFactory.createDataEntryDialog(
        this.view.getDictionary("therapy.application.preconditions"),
        null,
        preconditionsPane,
        function(resultData)
        {
          if (resultData)
          {
            self.applicationPrecondition = resultData.selection;
            self._setApplicationPreconditionLabel();
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
      shownInterval: this.shownInterval,
      selectedInterval: this.selectedInterval,
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
              self.selectedInterval = resultData.value.selectedInterval;
              self.shownInterval = resultData.value.shownInterval;
            }
            self._setDaysOfWeekLabel();
            self._triggerChangeEvent(null, true);
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
      if (!tm.jquery.Utils.isEmpty(this.shownInterval) && !tm.jquery.Utils.isEmpty(this.selectedInterval))
      {
        var selectedInterval = this.selectedInterval.interval + ".lc";
        this.daysLabel.setHtml(this.view.getDictionary("chosen.interval") + ": " +
        this.view.getDictionary("every") + " " + (this.shownInterval == 1 ? "" : (this.shownInterval + ". ")) +
        this.view.getDictionary(selectedInterval));
      }
      else
      {
        this.daysLabel.setHtml(this.view.getDictionary("chosen.days") + ": " +
        this.view.getDictionary("every") + " " + this.daysInterval + ". " +
        this.view.getDictionary("day.lc"));
      }
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

  _setApplicationPreconditionLabel: function()
  {
    var enums = app.views.medications.TherapyEnums;
    if (this.applicationPrecondition)
    {
      if (enums.medicationAdditionalInstructionEnum.BEFORE_MEAL == this.applicationPrecondition)
      {
        this.preconditionLabel.setHtml(
            this.view.getDictionary("medication.start.criterion") + ": " +
            this.view.getDictionary('before.meal'));
      }
      if (enums.medicationAdditionalInstructionEnum.AFTER_MEAL == this.applicationPrecondition)
      {
        this.preconditionLabel.setHtml(
            this.view.getDictionary("medication.start.criterion") + ": " +
            this.view.getDictionary('after.meal'));
      }
    }
    else
    {
      this.preconditionLabel.setHtml("");
    }
  },

  _clearOther: function(component)
  {
    if (this.button1xExButtonGroup != component)
    {
      this.button1xExButtonGroup.clearSelection();
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
      this.hoursField.setValue(null, true);
    }
    if (this.countField != component)
    {
      this.countField.setValue(null, true);
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

  _validateFrequencyInputField: function(component)
  {
    var self = this;
    this._frequencyFieldValidationForm.reset();

    this._frequencyFieldValidationForm.setOnValidationError(function()
    {
      component.setValue(null, true);
    });
    this._frequencyFieldValidationForm.addFormField(new tm.jquery.FormField({
      component: component,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = component.getValue();
        var preventValueGreaterThan24 = !self.allowMoreThan24hFrequency && self.getFrequencyMode() === "HOURS";

        if (value && 
            (!tm.jquery.Utils.isNumeric(value) || 
            value < 0) || 
            value % 1 !== 0 ||
            preventValueGreaterThan24 ? value > 24 : false)
        {
          return null;
        }
        return true;
      }
    }));
    this._frequencyFieldValidationForm.submit();
    this._clearOther(component);
    this._triggerChangeEvent(component);
  },

  /** public methods */
  clear: function()
  {
    this.showAllFields();
    this.button1xExButtonGroup.show();
    this.timeOfDayButtonGroup.show();
    this.cardContainer.show();
    this.modeButtonGroup.show();
    this.daysOfWeek = [];
    this.daysInterval = null;
    this.button1xExButtonGroup.clearSelection(true);
    this.timeOfDayButtonGroup.clearSelection(true);
    if (!this.editMode)
    {
      this._setModeFromContext();
    }
    this.hoursButtonGroup.clearSelection(true);
    this.countButtonGroup.clearSelection(true);
    this.hoursField.setValue(null, true);
    this.countField.setValue(null, true);
    this.daysLabel.setHtml("");
    this.applicationPrecondition = null;
    this.preconditionLabel.setHtml("");
    this.dosingPatternPane.clear();
  },

  getFrequency: function()
  {
    var enums = app.views.medications.TherapyEnums;
    if (this.button1xExButtonGroup.getSelection().length > 0)
    {
      return this._buildDosingFrequency(enums.dosingFrequencyTypeEnum.ONCE_THEN_EX, null);
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
      var countFieldValue = this.countField.getValue();
      if (countFieldValue && tm.jquery.Utils.isNumeric(countFieldValue))
      {
        return this._buildDosingFrequency(enums.dosingFrequencyTypeEnum.DAILY_COUNT, countFieldValue)
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
      var hoursFieldValue = this.hoursField.getValue();
      if (hoursFieldValue && tm.jquery.Utils.isNumeric(hoursFieldValue))
      {
        return this._buildDosingFrequency(enums.dosingFrequencyTypeEnum.BETWEEN_DOSES, hoursFieldValue);
      }
    }
    return null;
  },

  getFrequencyValue: function()
  {
    var frequency = this.getFrequency();
    return !tm.jquery.Utils.isEmpty(frequency) ? frequency.value : null;
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

  getTimesPerWeek: function()
  {
    var timesPerWeek = null;
    if (this.getDaysFrequency() != null)
    {
      //timesPerWeek = Math.ceil(7 / this.getDaysFrequency());
      timesPerWeek = 7 / this.getDaysFrequency();
    }
    else if (this.getDaysOfWeek() != null)
    {
      timesPerWeek = this.getDaysOfWeek().size();
    }

    return timesPerWeek;
  },

  setDaysFrequency: function(daysInterval)
  {
    this.daysInterval = daysInterval;
    this._setDaysOfWeekLabel();
  },

  getApplicationPrecondition: function()
  {
    return this.applicationPrecondition;
  },

  setApplicationPrecondition: function(applicationPrecondition)
  {
    this.applicationPrecondition = applicationPrecondition;
    this._setApplicationPreconditionLabel();
  },

  getDosingPattern: function()
  {
    return this.dosingPatternPane.getDosingPattern();
  },

  setFrequency: function(frequency, preventEvent) //returns selected component
  {
    var oldFrequency = this.getFrequency();

    if (oldFrequency && frequency &&
        (tm.views.medications.MedicationTimingUtils.getFrequencyKey(oldFrequency) ===
        tm.views.medications.MedicationTimingUtils.getFrequencyKey(frequency)))
    {
      // do nothing
      return;
    }

    var enums = app.views.medications.TherapyEnums;
    var selectedComponent = null;
    if (frequency)
    {
      if (frequency.type == enums.dosingFrequencyTypeEnum.ONCE_THEN_EX)
      {
        this.button1xExButtonGroup.setSelection([this.button1xExButtonGroup.getButtons()[0]], true);
        selectedComponent = this.button1xExButtonGroup.getButtons()[0];
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
            this.timeOfDayButtonGroup.setSelection([timeOfDayButtons[k]], true);
            selectedComponent = timeOfDayButtons[k];
          }
        }
      }
      else if (frequency.type == enums.dosingFrequencyTypeEnum.DAILY_COUNT)
      {
        this.modeButtonGroup.setSelection([this.countModeButton], true);
        this._modeButtonGroupChanged();
        var countButtons = this.countButtonGroup.getButtons();
        var countValueInButtons = false;
        for (var i = 0; i < countButtons.length; i++)
        {
          if (countButtons[i].data == frequency.value)
          {
            countValueInButtons = true;
            this.countButtonGroup.setSelection([countButtons[i]], true);
            this.countField.setValue(null, true);
            selectedComponent = countButtons[i];
          }
        }
        if (!countValueInButtons)
        {
          this.countButtonGroup.clearSelection();
          this.countField.setValue(frequency.value, true);
          selectedComponent = this.countField;
        }
      }
      else if (frequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        this.modeButtonGroup.setSelection([this.hoursModeButton], true);
        this._modeButtonGroupChanged();
        var hoursButtons = this.hoursButtonGroup.getButtons();
        var hoursValueInButtons = false;
        for (var j = 0; j < hoursButtons.length; j++)
        {
          if (hoursButtons[j].data == frequency.value)
          {
            hoursValueInButtons = true;
            this.hoursButtonGroup.setSelection([hoursButtons[j]], true);
            this.hoursField.setValue(null, true);
            selectedComponent = hoursButtons[j];
          }
        }
        if (!hoursValueInButtons)
        {
          this.hoursButtonGroup.clearSelection();
          this.hoursField.setValue(frequency.value, true);
          selectedComponent = this.hoursField;
        }
      }
    }
    else
    {
      this.clear();
    }
    if (!preventEvent)
    {
      this._triggerChangeEvent(selectedComponent);
    }
    return selectedComponent;
  },

  setDosingPattern: function(pattern, frequency)
  {
    var enums = app.views.medications.TherapyEnums;
    if (this.dosingPatternPane)
    {
      var frequencyKey = tm.views.medications.MedicationTimingUtils.getFrequencyKey(frequency);
      if (frequency && frequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        var hoursPattern =
            tm.views.medications.MedicationTimingUtils.getPatternForFrequencyBetweenHours(pattern[0], frequencyKey ? frequencyKey : this.getFrequencyKey());
        this.dosingPatternPane.setDosingPattern(hoursPattern);
      }
      else
      {
        this.dosingPatternPane.setDosingPattern(pattern);
      }
    }
  },

  setDosingFrequencyAndPattern: function(frequency, pattern, preventEvent)
  {
    var selectedComponent = this.setFrequency(frequency, true);
    if (pattern && pattern.length > 0)
    {
      this.setDosingPattern(pattern, frequency, preventEvent);
    }
    // trigger change event so the position sets correctly, prevent all events
    this._triggerChangeEvent(selectedComponent, true, true);
  },

  getDosingFrequencyPaneValidations: function()
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;

    var formFields = [];
    if (!self.cardContainer.isHidden())
    {
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
      formFields.push(new tm.jquery.FormField({
        component: self, //validate values of multiple fields on this pane
        required: true,
        componentValueImplementationFn: function()
        {
          return self.getFrequency();
        },
        validation: {
          type: "local",
          validators: [
            new tm.jquery.Validator({
              errorMessage: tm.jquery.Utils.isEmpty(self.daysInterval) || tm.jquery.Utils.isEmpty(self.getFrequencyValue()) ? "" :
                  tm.jquery.Utils.formatMessage(self.view.getDictionary('invalid.dosing.frequency.days.hours'),
                      self.daysInterval, self.getFrequencyValue()),
              isValid: function()
              {
                var frequency = self.getFrequency();
                if (tm.jquery.Utils.isEmpty(frequency) || tm.jquery.Utils.isEmpty(frequency.value) ||
                    frequency.type != enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
                {
                  return true;
                }
                var positiveNumber = tm.jquery.Utils.isNumeric(frequency.value) && frequency.value > 0;
                return positiveNumber && tm.jquery.Utils.isEmpty(self.daysInterval);
              }
            })
          ]
        }
      }));
    }
    if (this.withSingleFrequencies && this.dosingPatternPane && !this.dosingPatternPane.isHidden())
    {
      formFields.push(this.dosingPatternPane.getDosingPatternPaneValidation());
    }
    return formFields;
  },

  requestFocus: function()
  {
    this.button1xExButtonGroup.focus();
  },

  hideDaysButton: function()
  {
    this.daysButton.hide();
  },

  showDaysButton: function()
  {
    this.daysButton.show();
  },

  showDaysOnly: function(preventClear)
  {
    if (!preventClear)
    {
      this.button1xExButtonGroup.clearSelection(true);
      this.timeOfDayButtonGroup.clearSelection(true);
      this.modeButtonGroup.setSelection([this.countModeButton], true);
      this.hoursButtonGroup.clearSelection(true);
      this.countButtonGroup.clearSelection(true);
      this.hoursField.setValue(null, true);
      this.countField.setValue(null, true);
    }
    this.button1xExButtonGroup.hide();
    this.timeOfDayButtonGroup.hide();
    this.cardContainer.hide();
    this.modeButtonGroup.hide();
    this.dosingPatternPane.hide();
  },

  showAllFields: function()
  {
    this.button1xExButtonGroup.show();
    this.timeOfDayButtonGroup.show();
    this.cardContainer.show();
    this.modeButtonGroup.show();
  },
  /**
   * @Override
   */
  destroy: function()
  {
    this.callSuper();
    if (!tm.jquery.Utils.isEmpty(this._triggerChangeEventConditionTask))
    {
      this._triggerChangeEventConditionTask.abort();
      this._triggerChangeEventConditionTask = null;
    }
  }
});

