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

Class.define('app.views.medications.ordering.EditPastDaysOfTherapyContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "edit-consecutive-days-container",
  scrollable: 'vertical',

  /** configs */
  view: null,
  pastDaysOfTherapy: null,
  ehrCompositionId: null,
  ehrOrderName: null,
  validationForm: null,
  /** privates */

  /** privates: components */
  pastDaysOfTherapyField: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);

    this._buildComponents();
    this._buildGui();
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._presentValue();
    });
    this.pastDaysOfTherapyField.focus();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.pastDaysOfTherapyField = new tm.jquery.TextField({cls: 'day-of-therapy-field', width: 68});

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._savePastDaysOfTherapy();
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
    this.add(this.pastDaysOfTherapyField);
  },

  _savePastDaysOfTherapy: function()
  {
    var self = this;

    var value = this.pastDaysOfTherapyField.getValue();
    var pastDaysOfTherapy = (value && (!tm.jquery.Utils.isNumeric(value)) || value <= 0) ? null : value;

    var saveUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_CONSECUTIVE_DAYS;
    var params = {
      patientId: self.view.getPatientId(),
      compositionUid: self.ehrCompositionId,
      ehrOrderName: self.ehrOrderName,
      pastDaysOfTherapy: pastDaysOfTherapy
    };

    this.view.loadPostViewData(saveUrl, params, null,
        function()
        {
          var resultData = new app.views.common.AppResultData({success: true});
          self.resultCallback(resultData);
        },
        function()
        {
          var resultData = new app.views.common.AppResultData({success: false});
          self.resultCallback(resultData);
        },
        true);
  },

  _presentValue: function()
  {
    var pastDaysOfTherapy = this.pastDaysOfTherapy ? this.pastDaysOfTherapy : 0;
    this.pastDaysOfTherapyField.setValue(pastDaysOfTherapy);
  },

  _setupValidation: function()
  {
    var self = this;

    this.validationForm.addFormField(new tm.jquery.FormField({
      component: this.pastDaysOfTherapyField,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self.pastDaysOfTherapyField.getValue();

        if (value && (!tm.jquery.Utils.isNumeric(value) || value < 0))
        {
          return null;
        }
        return true;
      }
    }));
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit();
  }
});

