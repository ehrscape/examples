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

Class.define('app.views.medications.ordering.ProtocolOptionsContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'protocol-options-container',
  scrollable: 'both',
  /** configs */
  view: null,
  selectedDate: null,
  untilCanceled: false,
  startDate: null,
  protocolDays: null,

  /** privates */
  _repeatWholeProtocolTimes: 1,
  /** privates: components */
  _untilCanceledButton: null,
  _selectDateButton: null,
  _repeatWholeProtocolButton: null,
  _endDateField: null,
  _repeatProtocolTimesField: null,
  _protocolRepeatButtonGroup: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;

    var repeatLastDoseLabel = new tm.jquery.Container({
      cls: "TextLabel",
      html: this.view.getDictionary("repeat.last.day") + ":"
    });

    var untilCanceledButton = new tm.jquery.RadioButton({
      cls: "TextData",
      labelText: this.view.getDictionary("until.cancellation"),
      uncheckable: true,
      checked: self.untilCanceled
    });
    untilCanceledButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._untilCanceledButtonChangeEvent(component);
    });
    this._selectDateButton = new tm.jquery.RadioButton({
      cls: "TextData",
      labelText: this.view.getDictionary("select.date") + ":",
      uncheckable: true,
      checked: !tm.jquery.Utils.isEmpty(self.selectedDate)
    });

    this._selectDateButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._selectDateButtonChangeEvent();
    });

    var repeatWholeProtocolLabel = new tm.jquery.Container({
      cls: "TextLabel",
      html: this.view.getDictionary("repeat.whole.protocol") + ":"
    });
    this._repeatWholeProtocolButton = new tm.jquery.RadioButton({
      cls: "TextData",
      labelText: this.view.getDictionary("repeat.whole.protocol"),
      uncheckable: true,
      checked: false
    });

    this._repeatWholeProtocolButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (!self._repeatWholeProtocolButton.isChecked())
      {
        self._repeatWholeProtocolTimes = null;
      }
    });

    this._protocolRepeatButtonGroup = new tm.jquery.RadioButtonGroup({
      groupName: "therapyDurationRadioGroup",
      radioButtons: [untilCanceledButton, this._selectDateButton, this._repeatWholeProtocolButton]
    });

    var lastProtocolDate = new Date(moment(new Date(self.startDate)).add(self.protocolDays - 1, 'day'));

    this._endDateField = new tm.jquery.DatePicker({
      cls: "end-date-field",
      showType: "focus",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "78px"),
      date: self.selectedDate ? (self.selectedDate > lastProtocolDate ? self.selectedDate : lastProtocolDate) : null,
      minDate: lastProtocolDate,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });

    this._endDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (component.getDate())
      {
        self._selectDateButton.setChecked(true);
      }
    });
    var selectDateContainer = new tm.jquery.Container({
      cls: "select-date-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      scrollable: "visible"
    });

    selectDateContainer.add(this._selectDateButton);
    selectDateContainer.add(this._endDateField);

    var repeatWholeProtocolContainer = new tm.jquery.Container({
      cls: "repeat-whole-protocol-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
    });

    this._repeatProtocolTimesField = tm.views.medications.MedicationUtils.createNumberField('n0', 45, "repeat-whole-protocol-field");

    this._repeatProtocolTimesField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._repeatProtocolTimesFieldChangeEvent(component.getValue())
    });
    var timesLabel = new tm.jquery.Container({
      cls: "TextData",
      html: this.view.getDictionary("times")
    });
    repeatWholeProtocolContainer.add(this._repeatWholeProtocolButton);
    repeatWholeProtocolContainer.add(this._repeatProtocolTimesField);
    repeatWholeProtocolContainer.add(timesLabel);

    this.add(repeatLastDoseLabel);
    this.add(untilCanceledButton);
    this.add(selectDateContainer);
    this.add(repeatWholeProtocolLabel);
    this.add(repeatWholeProtocolContainer);

  },

  _untilCanceledButtonChangeEvent: function(untilCanceledButton)
  {
    if (untilCanceledButton.isChecked())
    {
      this.untilCanceled = true;
      this.selectedDate = null;
      this._repeatWholeProtocolTimes = null;
      this._endDateField.setDate(null);
    }
    else
    {
      this.untilCanceled = false;
    }
  },

  _selectDateButtonChangeEvent: function()
  {
    if (this._selectDateButton.isChecked())
    {
      this.selectedDate = this._endDateField.getDate();
      this.untilCanceled = false;
      this._repeatWholeProtocolTimes = null;
    }
    else
    {
      this.selectedDate = null;
      this._endDateField.setDate(null);
    }
  },

  _repeatProtocolTimesFieldChangeEvent: function(componentValue)
  {
    if (componentValue && tm.jquery.Utils.isNumeric(componentValue))
    {
      if (!this._repeatWholeProtocolButton.isChecked())
      {
        this._repeatWholeProtocolButton.setChecked(true);
      }
      this._repeatWholeProtocolTimes = Math.round(componentValue);
    }
    else
    {
      this._repeatWholeProtocolTimes = null;
      this._repeatProtocolTimesField.setValue(null, true);
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    var self = this;
    var validationForm = new tm.jquery.Form({

      onValidationSuccess: function()
      {
        var resultValue = {
          untilCanceled: self.untilCanceled,
          selectedDate: self.selectedDate,
          repeatWholeProtocolTimes: self._repeatWholeProtocolTimes
        };
        resultDataCallback(new app.views.common.AppResultData({success: true, value: resultValue}));
      },
      onValidationError: function()
      {
        resultDataCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: self.view.getDictionary("field.value.is.required")
    });


    if (this._protocolRepeatButtonGroup.getActiveRadioButton() == this._selectDateButton)
    {
      validationForm.addFormField(new tm.jquery.FormField({
        component: self._endDateField,
        required: true,
        componentValueImplementationFn: function()
        {
          return self._endDateField.getDate();
        }
      }));
    }
    if (this._protocolRepeatButtonGroup.getActiveRadioButton() == this._repeatWholeProtocolButton)
    {
      validationForm.addFormField(new tm.jquery.FormField({
        component: self._repeatProtocolTimesField,
        required: true,
        componentValueImplementationFn: function()
        {
          return self._repeatProtocolTimesField.getValue();
        },
        validation: {
          type: "local",
          validators: [
            new tm.jquery.Validator({
              errorMessage: self.view.getDictionary("value.must.be.numeric.not.zero"),
              isValid: function(value)
              {
                return (tm.jquery.Utils.isNumeric(value) && value > 0);
              }
            })
          ]
        }
      }));
    }
    validationForm.submit();
  }
});