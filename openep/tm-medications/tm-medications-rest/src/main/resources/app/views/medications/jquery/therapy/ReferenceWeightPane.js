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

Class.define('app.views.medications.therapy.ReferenceWeightPane', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "reference-weight-pane",
  /** configs */
  view: null,
  weight: null,
  /** privates */
  resultCallback: null,
  validationForm: null,
  /** privates: components */
  weightField: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.setLayout(new tm.jquery.HFlexboxLayout({gap: 5}));
    this._buildComponents();
    this._buildGui();
    this.weightField.setValue(this.weight);

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self.weightField.focus();
        $(self.weightField.getDom()).select();
      }, 0);
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.weightField = tm.views.medications.MedicationUtils.createNumberField('n3', 90);

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._saveReferenceWeight();
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
    this.add(this.weightField);
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextData', "kg"));
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();
    this.validationForm.addFormField(new tm.jquery.FormField({
      component: self.weightField,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self.weightField.getValue();
        if (value == null || value <= 0)
        {
          return null;
        }
        return true;
      }
    }));
  },

  _saveReferenceWeight: function()
  {
    var self = this;

    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SAVE_REFERENCE_WEIGHT_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var referenceWeight = this.weightField.getValue();
    var saveUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_REFERENCE_WEIGHT;
    var params = {
      patientId: self.view.getPatientId(),
      weight: referenceWeight
    };

    this.view.loadPostViewData(saveUrl, params, null,
        function()
        {
          var resultData = new app.views.common.AppResultData({success: true, value: referenceWeight});
          self.resultCallback(resultData);
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          var resultData = new app.views.common.AppResultData({success: false});
          self.resultCallback(resultData);
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit()
  }
});

