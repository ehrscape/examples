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

Class.define('app.views.medications.ordering.DosingPatternPane', 'tm.jquery.Container', {
  cls: "dosing-pattern-pane",
  /** configs */
  view: null,
  getFrequencyKeyFunction: null,
  getFrequencyTypeFunction: null,
  patternChangedEvent: null,
  /** privates */
  _eventFireTimer: null,
  _hideTooltipTimer: null,

  /** privates: components */
  timeFields: null,
  validationForm: null,
  _dosingPatternValidator: null,
  /** statics */


  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    var layout = tm.jquery.HFlexboxLayout.create('flex-start', "center", 0);
    layout.setFlexFlow(tm.jquery.flexbox.FlexFlow.create("row", "wrap"));
    this.setLayout(layout);

    this._dosingPatternValidator = new app.views.medications.common.DosingPatternValidator({
      view: this.getView()
    });

    this.timeFields = [];

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
      },
      onValidationError: function()
      {
        setTimeout(function ()
        {
          self.getTooltip().show();
        }, 0);
      }
    });
  },

  /** private methods */
  _presentPattern: function(administrationTimes)
  {
    $(this.getDom()).removeClass("form-field-validationError");
    this.setTooltip(null);

    administrationTimes = tm.jquery.Utils.isEmpty(administrationTimes) ? [] : administrationTimes;

    if (administrationTimes.length > 0)
      this.show();
    else
      this.hide();

    var self = this;
    var frequencyType = this.getFrequencyTypeFunction();

    this.removeAll();
    this.timeFields.length = 0;
    var nowContainer = new tm.jquery.Container();
    var nowButton = new tm.jquery.Button({
      cls: 'asap-btn',
      height: 22,
      margin: '0 5 0 5',
      text: this.view.getDictionary("asap"),
      handler: function()
      {
        self.timeFields[0].setTime(CurrentTime.get());
      }
    });
    nowContainer.add(nowButton);
    if (this.getFrequencyTypeFunction() == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES ||
        this.getFrequencyTypeFunction() == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.ONCE_THEN_EX)
    {
      this.add(nowContainer);
    }

    administrationTimes.forEach(function(hourMinute, index)
    {
      var time = tm.views.medications.MedicationTimingUtils.hourMinuteToDate(hourMinute);
      var enabled = index == 0 || frequencyType == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.DAILY_COUNT;

      var timeField = this._buildNewTimePicker(time, enabled, frequencyType);
      this.timeFields.push(timeField);
      var timeFieldContainer = new tm.jquery.Container({
        cls: "time-field-container",
        layout: tm.jquery.VFlexboxLayout.create("center", "center")
      });
      if (enabled)
      {
        var arrowContainer = new tm.jquery.Container({
          cls: "arrow-container",
          width: 0,
          height: 0,
          alignSelf: "flex-start"
        });
        timeFieldContainer.add(arrowContainer);
      }
      timeFieldContainer.add(timeField);
      this.add(timeFieldContainer);
    }, this);

    if (this.isRendered())
    {
      this.repaint();
    }
  },

  _buildNewTimePicker: function(time, enabled, frequencyType)
  {
    var self = this;
    var timeField = new tm.jquery.TimePicker({
      cls: "time-field",
      time: time,
      showType: "focus",
      enabled: enabled,
      width: 47,
      initialValue: time,
      nowButton: {
        text: this.view.getDictionary("asap")
      },
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    timeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (frequencyType == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        if (component.isEnabled() && frequencyType)
        {
          self._recalculateTimesForBetweenDoses();
        }
      }
      self._fireSingleChangeEvent();
    });
    timeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function(component){
      clearTimeout(self._hideTooltipTimer);
      if (self.getTooltip() && !self.getTooltip().isShowed()) {
        self.getTooltip().hideOnDocumentClickHandler = false;
        self.getTooltip().show();
      }
    });
    timeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_LOST, function(component){
      clearTimeout(self._hideTooltipTimer);
      self._hideTooltipTimer = setTimeout(function(){
        if (self.getTooltip()) self.getTooltip().hide();
      }, 100);
    });
    return timeField;
  },

  _recalculateTimesForBetweenDoses: function()
  {
    var frequencyKey = this.getFrequencyKeyFunction();
    var betweenDoses = frequencyKey.substring(0, frequencyKey.length - 1);
    if (this.timeFields.length > 0)
    {
      var firstTime = this.timeFields[0].getTime();
      if (firstTime)
      {
        for (var i = 1; i < this.timeFields.length; i++)
        {
          this.timeFields[i].setTime(new Date(firstTime.getTime() + i * betweenDoses * 60 * 60 * 1000), true);
        }
      }
    }
  },

  _fireSingleChangeEvent: function()
  {
    var self = this;
    clearTimeout(this._eventFireTimer);
    this._eventFireTimer = setTimeout(function(){
      self.patternChangedEvent();
      self._validate();
    }, 100);
  },

  _validate: function()
  {
    this.validationForm.addFormField(this.getDosingPatternPaneValidation());
    this.validationForm.submit();
  },

  /**
   * @returns {Array<String>} times
   */
  getTimes: function()
  {
    var times = [];
    for (var i = 0; i < this.getTimeFields().length; i++)
    {
      times.push(this.timeFields[i].getTime());
    }
    return times;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {Array}
   */
  getTimeFields: function()
  {
    return this.timeFields;
  },

  /** public methods */
  getDosingPattern: function()
  {
    var hourMinutes = [];

    this.timeFields.forEach(function(timeField)
    {
      var value = timeField.getPlugin() != null ? timeField.getTime() : timeField.initialValue;
      if (!tm.jquery.Utils.isEmpty(value))
      {
        hourMinutes.push({
          hour: value.getHours(),
          minute: value.getMinutes()
        });
      }
    });
    return hourMinutes;
  },

  getDosingPatternPaneValidation: function()
  {
    return this._dosingPatternValidator.getDosingPatternValidation(this, this.getTimes());
  },

  setDosingPattern: function(pattern)
  {
    this._presentPattern(pattern);
  },

  refreshDosingPattern: function()
  {
    var administrationTiming = this.view.getAdministrationTiming();
    var frequencyKey = this.getFrequencyKeyFunction();
    var frequencyType = this.getFrequencyTypeFunction();
    var pattern =
        tm.views.medications.MedicationTimingUtils.getFrequencyTimingPattern(administrationTiming, frequencyKey, frequencyType);
    this._presentPattern(pattern);
  },

  clear: function()
  {
    this._presentPattern([]);
    this.hide();
  },

  setTooltip: function(tooltip)
  {
    if (tooltip)
    {
      tooltip.setTrigger('manual');
      tooltip.hideOnDocumentClickHandler = false;
    }
    this.callSuper(tooltip);
  }

});