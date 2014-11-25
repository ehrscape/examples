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

Class.define('app.views.medications.ordering.WarningOverrideReasonPane', 'app.views.common.containers.AppDataEntryContainer', {
  /** configs */
  view: null,
  overrideReason: null,
  /** privates */
  resultCallback: null,
  validationForm: null,
  overrideReason: null,
  /** privates: components */
  textArea: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.setLayout(new tm.jquery.HFlexboxLayout({gap: 5, flex: 1}));
    this.setPadding('10');
    this._buildComponents();
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self.textArea.focus();
        $(self.textArea.getDom()).select();
        self.textArea.setValue(self.overrideReason);
      }, 0);
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.textArea = new tm.jquery.TextArea({value: null, rows: 4, width: 275, size: "large"});

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self.overrideReason = self.textArea.getValue();
        self.resultCallback(new app.views.common.AppResultData({success: true, overrideReason: self.overrideReason}));
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
    this.add(this.textArea);
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();
    this.validationForm.addFormField(new tm.jquery.FormField({
      component: self.textArea,
      required: true,
      componentValueImplementationFn: function()
      {
        if (tm.jquery.Utils.isEmpty(self.textArea.getValue()))
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
    this.validationForm.submit()
  }
});

