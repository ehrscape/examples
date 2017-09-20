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

Class.define('app.views.medications.ordering.UniversalMedicationDataContainer', 'app.views.common.containers.AppDataEntryContainer', {
  view: null,
  /** privates: components*/
  _nameField: null,
  _doseFormCombo: null,
  _medicationSolutionRadioBtnGroup: null,
  /** privates */
  cls: 'universal-medication-data-container',

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this._buildGui();
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self.view.getAppFactory().createConditionTask(
          function()
          {
            self._nameField.focus();
          },
          function(task)
          {
            if (!self.isRendered())
            {
              task.abort()
            }
            return self.isRendered(true) && $(self._nameField.getDom()).isVisible();
          },
          500, 50
      );
    });
  },

  _buildGui: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"));

    var medicationBtn = new tm.jquery.RadioButton({
      labelText: this.view.getDictionary("MedicationTypeEnum.MEDICATION"),
      labelAlign: "right",
      data: enums.medicationTypeEnum.MEDICATION,
      checked: true
    });

    var solutionBtn = new tm.jquery.RadioButton({
      labelText: this.view.getDictionary("MedicationTypeEnum.SOLUTION"),
      labelAlign: "right",
      data: enums.medicationTypeEnum.SOLUTION
    });

    this._medicationSolutionRadioBtnGroup = new tm.jquery.RadioButtonGroup({
      cls: "medication-solution-radio-btn-group",
      groupName: "horizontal-radiobutton-group",
      radioButtons: [medicationBtn, solutionBtn]
    });

    var nameLabel = new tm.jquery.Container({
      cls: "TextLabel universal-medication-label",
      html: this.view.getDictionary("name")
    });

    this._nameField = new tm.jquery.TextField({
      cls: 'universal-medication-name-field',
      width: 450
    });

    this._nameField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self._doseFormCombo.focus();
    });

    var doseFormLabel = new tm.jquery.Container({
      cls: "TextLabel universal-medication-label",
      html: this.view.getDictionary("dose.form")
    });

    this._doseFormCombo = new tm.jquery.TypeaheadField({
      cls: "dose-form-combo",
      displayProvider: function(doseForm)
      {
        return doseForm.name;
      },
      minLength: 1,
      width: 450,
      items: 10000
    });

    this._doseFormCombo.setSource(this.view.getDoseForms());

    this._medicationSolutionRadioBtnGroupContainer =
        appFactory.createHRadioButtonGroupContainer(this._medicationSolutionRadioBtnGroup);
    this.add(this._medicationSolutionRadioBtnGroupContainer);
    this.add(nameLabel);
    this.add(this._nameField);
    this.add(doseFormLabel);
    this.add(this._doseFormCombo);
  },

  _buildMedicationData: function()
  {
    var medication = new app.views.medications.common.dto.Medication({
      id: null,
      name: this._nameField.getValue(),
      medicationType: this._medicationSolutionRadioBtnGroup.getActiveRadioButton().data
    });

    return app.views.medications.common.dto.MedicationData.fromJson({
      doseForm: this._doseFormCombo.getSelection(),
      medication: medication
    });
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        var medicationData = self._buildMedicationData();
        resultDataCallback(new app.views.common.AppResultData({success: true, value: medicationData}));
      },
      onValidationError: function()
      {
        resultDataCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: self.view.getDictionary("field.value.is.required")
    });
    validationForm.reset();
    validationForm.addFormField(new tm.jquery.FormField({
      component: self._nameField,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self._nameField.getValue();
        if (value == null || value == "")
        {
          return null;
        }
        return true;
      }
    }));
    validationForm.addFormField(new tm.jquery.FormField({
      component: self._doseFormCombo,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self._doseFormCombo.getSelection();
        if (value == null)
        {
          return null;
        }
        return true;
      }
    }));
    validationForm.addFormField(new tm.jquery.FormField({
      component: self._medicationSolutionRadioBtnGroupContainer,
      required: true,
      componentValueImplementationFn: function()
      {
        var solution =
            self._medicationSolutionRadioBtnGroup.getActiveRadioButton().data === enums.medicationTypeEnum.SOLUTION;
        var simpleDoseFormType = self._doseFormCombo.getSelection() &&
            self._doseFormCombo.getSelection().medicationOrderFormType === enums.medicationOrderFormType.SIMPLE;
        if (solution && simpleDoseFormType)
        {
          return null;
        }
        return true;
      }
    }));

    validationForm.submit();
  }
});
