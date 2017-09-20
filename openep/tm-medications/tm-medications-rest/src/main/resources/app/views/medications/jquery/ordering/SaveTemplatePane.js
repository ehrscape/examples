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

Class.define('app.views.medications.ordering.SaveTemplatePane', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "save-template-pane",
  /** configs */
  view: null,
  therapies: null,
  templates: null,
  addSingleTherapy: true,
  templateMode: null,
  /** privates*/
  resultCallback: null,
  validationForm: null,
  /** privates: components */
  typeCombo: null,
  modeButtonGroup: null,
  newTemplateButton: null,
  existingTemplateButton: null,
  newTemplateNameField: null,
  templatesCombo: null,
  incompleteTherapyCheckBox: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);

    this.setLayout(new tm.jquery.VFlexboxLayout());
    this._buildComponents();
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self.newTemplateNameField.focus();
      }, 0);
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;

    this.typeCombo = new tm.jquery.SelectBox({
      cls: "template-type-combo",
      width: 265,
      padding: '0 0 0 25'
    });
    var typeUser = tm.jquery.SelectBox.createOption(enums.templateTypeEnum.USER, this.view.getDictionary('my.template'), null, null, true);
    var typeOrg = tm.jquery.SelectBox.createOption(enums.templateTypeEnum.ORGANIZATIONAL, this.view.getDictionary('organizational.template'), null, null, false);
    var typePat = tm.jquery.SelectBox.createOption(enums.templateTypeEnum.PATIENT, this.view.getDictionary('patient.template'), null, null, false);

    this.typeCombo.addOption(typeUser);
    if (this.view.getTherapyAuthority().isManageOrganizationalTemplatesAllowed())
    {
      this.typeCombo.addOption(typeOrg);
    }
    if (this.view.getTherapyAuthority().isManagePatientTemplatesAllowed())
    {
      this.typeCombo.addOption(typePat);
    }

    this.typeCombo.setSelections([enums.templateTypeEnum.USER]);
    this.typeCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      var type = self._getSelectedTemplateType();
      if (type == enums.templateTypeEnum.USER)
      {
        self.templatesCombo.setSource(self.templates.userTemplates);
      }
      else if (type == enums.templateTypeEnum.PATIENT)
      {
        self.templatesCombo.setSource(self.templates.patientTemplates);
      }
      else
      {
        self.templatesCombo.setSource(self.templates.organizationTemplates);
      }
      self.templatesCombo.setSelection(null);
    });

    this.newTemplateButton = new tm.jquery.RadioButton({cls:"new-template-button", checked: true, margin: "10 0 0 0"});
    this.existingTemplateButton = new tm.jquery.RadioButton({cls:"existing-template-button"});
    this.modeButtonGroup = new tm.jquery.RadioButtonGroup({
      groupName: "modeGroup",
      radioButtons: [this.newTemplateButton, this.existingTemplateButton],
      onChange: function()
      {
        setTimeout(function()
        {
          if (self.modeButtonGroup.getActiveRadioButton() != self.newTemplateButton)
          {
            self.newTemplateNameField.setValue(null);
          }
          if (self.modeButtonGroup.getActiveRadioButton() != self.existingTemplateButton)
          {
            self.templatesCombo.setSelection(null);
          }
        }, 100);
      }
    });

    this.newTemplateNameField = new tm.jquery.TextField({cls: "template-name-field", width: 240});
    this.newTemplateNameField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.newTemplateNameField.getValue())
      {
        self.modeButtonGroup.setActiveRadioButton(self.newTemplateButton);
      }
    });

    this.templatesCombo = new tm.jquery.TypeaheadField({
      cls: "templates-combo",
      displayProvider: function(template)
      {
        return template.name;
      },
      mode: 'advanced',
      margin: '0 1 0 0',
      minLength: 1,
      width: 240,
      items: 10000
    });
    this.templatesCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.templatesCombo.getSelection())
      {
        self.modeButtonGroup.setActiveRadioButton(self.existingTemplateButton);
      }
    });
    this.templatesCombo.setSource(this.templates.userTemplates);

    if (this.addSingleTherapy)
    {
      this.incompleteTherapyCheckBox = new tm.jquery.CheckBox({
        cls: "incomplete-therapy-checkbox",
        labelText: this.view.getDictionary("mark.therapy.incomplete"),
        nowrap: true,
        padding: '0 0 0 47'
      });
      if (this.invalidTherapy)
      {
        this.incompleteTherapyCheckBox.setChecked(true);
        this.incompleteTherapyCheckBox.setEnabled(false);
      }
    }

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._saveTemplate();
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
    this.add(this.typeCombo);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('new.template'), '5 0 0 25'));
    var newTemplateContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 5)});
    newTemplateContainer.add(this.newTemplateButton);
    newTemplateContainer.add(this.newTemplateNameField);
    this.add(newTemplateContainer);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));

    var existingTemplateTitle = this.addSingleTherapy ? this.view.getDictionary('add.to.existing.template') : this.view.getDictionary('override.existing.template');
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', existingTemplateTitle, '5 0 0 25'));
    var existingTemplateContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 5)});
    existingTemplateContainer.add(this.existingTemplateButton);
    existingTemplateContainer.add(this.templatesCombo);
    this.add(existingTemplateContainer);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 20}));
    if (this.addSingleTherapy)
    {
      this.add(this.incompleteTherapyCheckBox);
    }
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();
    if (this.modeButtonGroup.getActiveRadioButton() == self.newTemplateButton)
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: self.newTemplateNameField,
        required: true,
        componentValueImplementationFn: function()
        {
          return self.newTemplateNameField.getValue();
        }
      }));
    }
    else
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: self.templatesCombo,
        required: true,
        componentValueImplementationFn: function()
        {
          return self.templatesCombo.getSelection();
        }
      }));
    }
  },

  _getSelectedTemplateType: function()
  {
    return this.typeCombo.getSelections()[0];
  },

  _saveTemplate: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var template = this._buildTemplate();

    if (template.id == 0 && this._doseTemplateWithNameAlreadyExist(template.name))
    {
      appFactory.createWarningSystemDialog(self.view.getDictionary("template.with.name.already.exists"), 360, 122).show();
      self.resultCallback(new app.views.common.AppResultData({success: false}));
    }
    else
    {
      var saveUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_TEMPLATE;
      var params = {
        template: JSON.stringify(template),
        templateMode: this.templateMode
      };

      this.view.loadPostViewData(saveUrl, params, null,
          function()
          {
            self.resultCallback(new app.views.common.AppResultData({success: true}));
          },
          function()
          {
            self.resultCallback(new app.views.common.AppResultData({success: false}));
          },
          true);
    }
  },

  _doseTemplateWithNameAlreadyExist: function(templateName)
  {
    var templates = this.templatesCombo.getSource();
    for (var i = 0; i < templates.length; i++)
    {
      if (templates[i].name == templateName)
      {
        return true;
      }
    }
    return false;
  },

  _buildTemplate: function()
  {
    var enums = app.views.medications.TherapyEnums;

    var createNewTemplate = this.modeButtonGroup.getActiveRadioButton() == this.newTemplateButton;
    var selectedTemplate = this.templatesCombo.getSelection();

    var templateElements = [];
    if (this.addSingleTherapy && !createNewTemplate && selectedTemplate)
    {
      templateElements = selectedTemplate.templateElements;
    }
    for (var i = 0; i < this.therapies.length; i++)
    {
      templateElements.push({
        therapy: this.therapies[i],
        completed: this.addSingleTherapy ? !this.incompleteTherapyCheckBox.isChecked() : this.therapies[i].completed != false
      });
    }

    var templateType = this._getSelectedTemplateType();
    var isOrganizationalTemplate = templateType == enums.templateTypeEnum.ORGANIZATIONAL;
    var isPatientTemplate = templateType == enums.templateTypeEnum.PATIENT;
    return {
      id: createNewTemplate ? 0 : this.templatesCombo.getSelection().id,
      name: createNewTemplate ? this.newTemplateNameField.getValue() : this.templatesCombo.getSelection().name,
      type: templateType,
      careProviderId: isOrganizationalTemplate ? this.view.getCareProviderId() : null,
      patientId: isPatientTemplate ? this.view.getPatientId() : null,
      templateElements: templateElements
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit()
  }
});
