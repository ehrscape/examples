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
Class.define('app.views.medications.ordering.ComplexVariableRateRowContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_START_TIME_CHANGE: new tm.jquery.event.EventType({
      name: 'complexVariableRateRowContainerStartTimeChange', delegateName: null
    }),
    EVENT_TYPE_END_TIME_CHANGE: new tm.jquery.event.EventType({
      name: 'complexVariableRateRowContainerEndTimeChange', delegateName: null
    }),
    EVENT_TYPE_RATE_CHANGE: new tm.jquery.event.EventType({
      name: 'complexVariableRateRowContainerRateChange', delegateName: null
    }),
    EVENT_TYPE_RATE_FIELD_FOCUS_LOST: new tm.jquery.event.EventType({
      name: 'complexVariableRateRowContainerRateFieldFocusLost', delegateName: null
    }),
    EVENT_TYPE_FORMULA_FIELD_FOCUS_LOST: new tm.jquery.event.EventType({
      name: 'complexVariableRateRowContainerFormulaFieldFocusLost', delegateName: null
    })
  },
  cls: "rate-row",
  formulaUnitDisplay: null,
  timedDoseElement: null,
  endAsLabel: false,
  showFormula: false,
  continuousInfusion: false,
  view: null,
  infusionIngredients: null,
  volumeSum: null,
  medicationData: null,

  _startField: null,
  _endField: null,
  _internalTimeInterval: null,
  _infusionRatePane: null,

  /** constructor */
  Constructor: function()
  {
    this.callSuper();

    this._internalTimeInterval = this._buildInternalTimeInterval();
    this.infusionIngredients = tm.jquery.Utils.isArray(this.infusionIngredients) ? this.infusionIngredients : [];

    this.registerEventTypes('app.views.medications.ordering.ComplexVariableRateRowContainer', [
      {eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_START_TIME_CHANGE},
      {eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_END_TIME_CHANGE},
      {eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_RATE_CHANGE},
      {eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_RATE_FIELD_FOCUS_LOST},
      {eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_FORMULA_FIELD_FOCUS_LOST}
    ]);

    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;
    var rate = this.getTimedDoseElement() && this.getTimedDoseElement().doseElement &&
    this.getTimedDoseElement().doseElement.rate ?
        this.getTimedDoseElement().doseElement.rate :
        null;

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));

    this._startField = new tm.jquery.TimePicker({
      cls: "start-field",
      time: this._getInternalStart(),
      width: 76,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this._startField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._internalTimeInterval.start = component.getTime();
      self._fireStartTimeChangeEvent();
    });

    // the field is swappable because setting values on a hidden TimePicker caused several odd issues when
    // setting the field's value to null.
    this._endField = this.isEndAsLabel() ?
        this._buildEndLabel(this._getInternalEnd()) :
        this._buildEndField(this._getInternalEnd());

    this._infusionRatePane = new app.views.medications.ordering.InfusionRatePane({
      view: this.getView(),
      getInfusionIngredientsFunction: function()
      {
        return self.getInfusionIngredients();
      },
      getContinuousInfusionFunction: function()
      {
        return self.isContinuousInfusion();
      },
      getVolumeSumFunction: function()
      {
        return self.getVolumeSum();
      },
      changeEvent: function()
      {
        self._fireRateChangeEvent()
      },
      formulaVisibleFunction: function()
      {
        return self.isShowFormula();
      },
      rateFieldFocusLostEvent: function()
      {
        self._fireRateFieldFocusLostEvent()
      },
      formulaFieldFocusLostEvent: function()
      {
        self._fireFormulaFieldFocusLostEvent()
      }
    });

    this._infusionRatePane.setFirstMedicationData(this.getMedicationData(), true);
    this._infusionRatePane.setDurationVisible(false);
    if (rate)
    {
      this._infusionRatePane.setInfusionRate(rate);
    }
    this._infusionRatePane.setFormulaUnitToLabel(this.getFormulaUnitDisplay());
    this._infusionRatePane.setFormulaVisible();

    this.add(this._startField);
    this.add(new tm.jquery.Container({
      cls: 'TextData',
      html: this.getView().getDictionary("until.low.case")
    }));
    this.add(this._endField);
    this.add(this._infusionRatePane);
  },

  /**
   * @param {Date} date
   * @param {Boolean} [isRecurringChecked=false]
   * @private
   */
  _buildEndLabel: function(date, isRecurringChecked)
  {
    var endString =
        date ? tm.views.medications.MedicationTimingUtils.hourMinuteToString(date.getHours(), date.getMinutes()) : "";
    var endLabelText = this.isContinuousInfusion() && !isRecurringChecked ?
        this.getView().getDictionary('cancel.accusative').toLowerCase() :
        endString;

    return new tm.jquery.Container({
      cls: 'TextData end-label',
      width: 78,
      html: endLabelText
    });
  },

  _buildEndField: function(end)
  {
    var self = this;
    var endField = new tm.jquery.TimePicker({
      isEndField: true,
      cls: "end-field",
      time: end,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    endField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_LOST, function(component)
    {
      var startTime;
      var textValue = component.getField().getValue();
      if (textValue)
      {
        if ((textValue.indexOf('+') == 0))
        {
          if (textValue.indexOf('h') == textValue.length - 1)
          {
            var plusHours = textValue.substring(1, textValue.length - 1);
            if (tm.jquery.Utils.isNumeric(plusHours))
            {
              startTime = self.getStartTime();
              if (startTime)
              {
                var hours = Number(startTime.getHours()) + Number(plusHours);
                component.setTime(
                    new Date(
                        startTime.getFullYear(),
                        startTime.getMonth(),
                        startTime.getDate(),
                        hours,
                        startTime.getMinutes(),
                        0,
                        0
                    )
                );
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
              startTime = self.getStartTime();
              var minutes = Number(startTime.getMinutes()) + Number(plusMinutes);
              component.setTime(
                  new Date(
                      startTime.getFullYear(),
                      startTime.getMonth(),
                      startTime.getDate(),
                      startTime.getHours(),
                      minutes,
                      0,
                      0
                  )
              );
            }
          }
        }
      }
    });
    endField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._internalTimeInterval.end = component.getTime();
      self._fireEndTimeChangeEvent()
    });
    return endField;
  },

  /**
   * Caching the values for two reasons - first, the TimePicker will not function properly until
   * the internal plugin is initialized (can't getTime() until then) and the second because
   * when the endField is a label, we can't really call getTime() on it.
   * @returns {{start: Date|null, end: Date|null}}
   * @private
   */
  _buildInternalTimeInterval: function()
  {
    var startEnd = {
      start: null,
      end: null
    };

    if (this.getTimedDoseElement())
    {
      if (this.getTimedDoseElement().doseTime)
      {
        startEnd.start = CurrentTime.get();
        startEnd.start.setHours(this.getTimedDoseElement().doseTime.hour);
        startEnd.start.setMinutes(this.getTimedDoseElement().doseTime.minute);

        if (this.getTimedDoseElement().doseElement && this.getTimedDoseElement().doseElement.duration)
        {
          startEnd.end = CurrentTime.get();
          startEnd.end.setHours(startEnd.start.getHours());
          startEnd.end.setMinutes(startEnd.start.getMinutes() + this.getTimedDoseElement().doseElement.duration);
        }
      }
    }

    return startEnd;
  },

  _fireStartTimeChangeEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_START_TIME_CHANGE,
      eventData: {}
    }), null);
  },

  _fireEndTimeChangeEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_END_TIME_CHANGE,
      eventData: null
    }), null);

  },

  _fireRateChangeEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_RATE_CHANGE,
      eventData: null
    }), null);
  },

  _fireRateFieldFocusLostEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_RATE_FIELD_FOCUS_LOST,
      eventData: null
    }), null);
  },

  _fireFormulaFieldFocusLostEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.ComplexVariableRateRowContainer.EVENT_TYPE_FORMULA_FIELD_FOCUS_LOST,
      eventData: null
    }), null);
  },

  /**
   * @returns {Date|null}
   * @private
   */
  _getInternalStart: function()
  {
    return this._internalTimeInterval.start;
  },

  /**
   * @returns {Date|null}
   * @private
   */
  _getInternalEnd: function()
  {
    return this._internalTimeInterval.end;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {Object}
   */
  getTimedDoseElement: function()
  {
    return this.timedDoseElement;
  },

  /**
   * @returns {string|null}
   */
  getFormulaUnitDisplay: function()
  {
    return this.formulaUnitDisplay;
  },

  /**
   * @returns {boolean}
   */
  isEndAsLabel: function()
  {
    return this.endAsLabel === true;
  },

  /**
   * @param {boolean} isLabel
   * @param {Boolean} [isRecurringChecked=false]
   */
  setEndAsLabel: function(isLabel, isRecurringChecked)
  {
    if (isLabel === this.endAsLabel) return;

    if (isLabel)
    {
      var label = this._buildEndLabel(this._getInternalEnd(), isRecurringChecked);
      this.replace(this._endField, label);
      this._endField = label;
      this.repaint();
    }
    else
    {
      var timePicker = this._buildEndField(this._getInternalEnd());
      this.replace(this._endField, timePicker);
      this._endField = timePicker;
    }
    this.endAsLabel = isLabel;
  },

  getStartTime: function()
  {
    return this._startField && this._startField.getPlugin() ? this._startField.getTime() : this._getInternalStart();
  },

  /**
   * @param {Date|null} date
   * @param {Boolean} [preventEvent=false]
   */
  setStartTime: function(date, preventEvent)
  {
    this._internalTimeInterval.start = date;
    this._startField.setTime(date, preventEvent);
  },

  /**
   * @param {Date} date
   * @param {boolean} [preventEvent=false]
   */
  setEndTime: function(date, preventEvent)
  {
    this._internalTimeInterval.end = date;
    if (!this.isEndAsLabel())
    {
      this._endField.setTime(date, preventEvent);
    }
    else
    {
      if (date)
      {
        var calculatedEndString =
            tm.views.medications.MedicationTimingUtils.hourMinuteToString(date.getHours(), date.getMinutes());
        this._endField.setHtml(calculatedEndString);
      }
      else
      {
        this._endField.setHtml(
            this.isContinuousInfusion() ?
                this.view.getDictionary('cancel.accusative').toLowerCase() :
                "");
      }
    }
  },

  /**
   * @returns {Date|null}
   */
  getEndTime: function()
  {
    return this._getInternalEnd();
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
   * @returns {app.views.medications.common.dto.MedicationData}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   *
   * @returns {String|{duration, rate, rateUnit, rateFormula, rateFormulaUnit}}
   */
  getInfusionRate: function()
  {
    return this._infusionRatePane.getInfusionRate();
  },

  /**
   * @returns {boolean}
   */
  isShowFormula: function()
  {
    return this.showFormula === true;
  },

  requestFocusToFormula: function()
  {
    this._infusionRatePane.requestFocusToFormula();
  },

  requestFocusToRate: function()
  {
    this._infusionRatePane.requestFocusToRate();
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getFormValidations: function()
  {
    var infusionRateValidations = this._infusionRatePane.getInfusionRatePaneValidations();
    infusionRateValidations.push(new tm.jquery.FormField({
      component: this._startField,
      required: true,
      componentValueImplementationFn: function(component)
      {
        return component.getTime();
      }
    }));
    if (!this.isEndAsLabel())
    {
      infusionRateValidations.push(new tm.jquery.FormField({
        component: this._endField,
        required: true,
        componentValueImplementationFn: function(component)
        {
          return component.getTime();
        }
      }));
    }
    return infusionRateValidations;
  },

  /**
   * @param {string} formulaUnitDisplay
   */
  setFormulaUnitToLabel: function(formulaUnitDisplay)
  {
    this._infusionRatePane.setFormulaUnitToLabel(formulaUnitDisplay);
  },

  setFocusToStartField: function()
  {
    this._startField.focus();
  },

  /**
   * @returns {boolean}
   */
  isContinuousInfusion: function()
  {
    return this.continuousInfusion === true;
  }
});