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

Class.define('app.views.medications.ordering.UniversalSimpleTherapyContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "universal-simple-therapy-container",
  scrollable: 'both',
  margin: '0 0 0 15',

  /** configs */
  view: null,
  therapyToEdit: null,
  /** privates */
  validationForm: null,
  timedDoseElements: null,
  /** privates: components */
  medicationField: null,
  doseFormCombo: null,
  routesCombo: null,
  universalDosePane: null,
  variableDoseContainer: null,
  varioButton: null,
  dosingFrequencyTitle: null,
  dosingFrequencyPane: null,
  therapyStartTitle: null,
  therapyDurationTitle: null,
  therapyIntervalPane: null,
  commentField: null,
  performerContainer: null,
  saveDateTimePane: null,
  maxFrequencyTitleLabel: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.timedDoseElements = [];

    if (this.therapyToEdit)
    {
      this.editingStartTimestamp = new Date();
      this.editingStartTimestamp.setSeconds(0);
      this.editingStartTimestamp.setMilliseconds(0);
    }

    this.setLayout(tm.jquery.VFlexboxLayout.create('start', "stretch", 0));
    this._buildComponents();
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self.medicationField.focus();
      }, 0);
    });

    this.therapyIntervalPane.setMaxDailyFrequencyFieldVisible(false);
    if (this.therapyToEdit)
    {
      var appFactory = this.view.getAppFactory();
      appFactory.createConditionTask(
          function()
          {
            self._presentTherapy();
          },
          function()
          {
            return self.isRendered(true);
          },
          50, 1000
      );
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    this.medicationField = new tm.jquery.TextField({width: 644});

    this.doseFormCombo = new tm.jquery.TypeaheadField({
      displayProvider: function(doseForm)
      {
        return doseForm.name;
      },
      minLength: 1,
      width: 656,
      items: 10000
    });
    this.doseFormCombo.setSource(this.view.getDoseForms());
    this.doseFormCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._showDoseComponent(self.doseFormCombo.getSelection());
    });

    this.routesCombo = new tm.jquery.TypeaheadField({
      displayProvider: function(route)
      {
        return route.name;
      },
      minLength: 1,
      mode: 'advanced',
      width: 150,
      items: 10000
    });
    this.routesCombo.setSource(this.view.getRoutes());

    this.universalDosePane = new app.views.medications.ordering.UniversalDosePane({
      view: this.view,
      numeratorUnitChangeEvent: function()
      {
        self._setVarioEnabled();
      }
    });
    this.variableDoseContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "stretch"), margin: '0 0 0 10'});
    this.variableDoseContainer.hide();

    this.descriptiveDoseField = new tm.jquery.TextField({placeholder: this.view.getDictionary('single.dose'), width: 678});
    this.descriptiveDoseField.hide();

    this.varioButton = new tm.jquery.Button({
      text: 'Variable',
      handler: function()
      {
        self.universalDosePane.clear();
        self._openVariableDoseEditPane();
      }
    });

    this.dosingFrequencyPane = new app.views.medications.ordering.DosingFrequencyPane({
      view: this.view,
      width: 678,
      frequencyChangeEvent: function()
      {
        self._handleDosingFrequencyChange();
      },
      setMaxDailyFrequencyFieldsVisibleFunction: function(setVisible)
      {
        if (self.therapyIntervalPane)
        {
          self.therapyIntervalPane.setMaxDailyFrequencyFieldVisible(setVisible);
        }
      }
    });
    this.therapyDurationTitle = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('therapy.duration'), '5 290 0 0');
    this.therapyStartTitle = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('start'), '5 210 0 0');
    this.maxFrequencyTitleLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', 'Max');
    this.therapyIntervalPane = new app.views.medications.ordering.TherapyIntervalPane({
      view: this.view,
      width: 678,
      getFrequencyKeyFunction: function()
      {
        return self.dosingFrequencyPane.getFrequencyKey();
      },
      getFrequencyModeFunction: function()
      {
        return self.dosingFrequencyPane.getFrequencyType();
      },
      getDosingFrequencyModeFunction: function()
      {
        return self.dosingFrequencyPane.getFrequencyMode();
      },
      untilHideEvent: function(hide)
      {
        if (hide)
        {
          self.therapyDurationTitle.hide();
          self.therapyStartTitle.setPadding('5 603 0 0')
        }
        else
        {
          self.therapyDurationTitle.show();
          self.therapyStartTitle.setPadding('5 210 0 0')
        }
      },
      setMaxFrequencyTitleVisible: function(show)
      {
        if (show)
        {
          self.maxFrequencyTitleLabel.show();
        }
        else
        {
          self.maxFrequencyTitleLabel.hide();
        }
      }
    });
    this.commentField = new tm.jquery.TextField({width: 644});
    this.commentField.onKey(
        new tm.jquery.event.KeyStroke({key: "t", altKey: true, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.saveDateTimePaneEvent();
        });

    var careProfessionals = this.view.getCareProfessionals();
    var currentUserAsCareProfessionalName = this.view.getCurrentUserAsCareProfessional() ? this.view.getCurrentUserAsCareProfessional().name : null;
    this.performerContainer =
        tm.views.medications.MedicationUtils.createPerformerContainer(this.view, careProfessionals, currentUserAsCareProfessionalName);

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane();
    this.saveDateTimePane.hide();

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        if (!self.performerContainer.getPerformer())
        {
          appFactory.createWarningSystemDialog(self.view.getDictionary("prescriber.not.defined.warning"), 320, 122).show();
          self.resultCallback(new app.views.common.AppResultData({success: false}));
        }
        else
        {
          self._saveTherapy();
        }
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
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('medication')));
    this.add(this.medicationField);
    this.add(this._createVerticalSpacer(2));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dose.form')));
    this.add(this.doseFormCombo);
    this.add(this._createVerticalSpacer(2));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dose')));
    var doseRowContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout(), width: 678});
    doseRowContainer.add(new tm.jquery.Container({flex: 1}));
    doseRowContainer.add(this.varioButton);
    doseRowContainer.add(this.universalDosePane);
    doseRowContainer.add(this.variableDoseContainer);
    doseRowContainer.add(this.descriptiveDoseField);
    this.add(doseRowContainer);
    this.add(this._createVerticalSpacer(2));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('route')));
    this.add(this.routesCombo);
    this.add(this._createVerticalSpacer(2));

    this.dosingFrequencyTitle = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval'));
    this.add(this.dosingFrequencyTitle);
    this.add(this.dosingFrequencyPane);
    this.add(this._createVerticalSpacer(2));

    var therapyIntervalLabelsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start")});
    therapyIntervalLabelsContainer.add(this.therapyStartTitle);
    therapyIntervalLabelsContainer.add(this.therapyDurationTitle);
    therapyIntervalLabelsContainer.add(this.maxFrequencyTitleLabel);
    this.add(therapyIntervalLabelsContainer);
    this.add(this.therapyIntervalPane);
    this.add(this._createVerticalSpacer(2));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('commentary')));
    this.add(this.commentField);
    this.add(new tm.jquery.Container({flex: 1}));
    this.add(this.performerContainer);
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();
    this.validationForm.addFormField(new tm.jquery.FormField({
      component: self.medicationField,
      required: true
    }));
    this.validationForm.addFormField(new tm.jquery.FormField({
      component: self.doseFormCombo,
      required: true,
      componentValueImplementationFn: function()
      {
        return self.doseFormCombo.getSelection();
      }
    }));
    if (!this.universalDosePane.isHidden())
    {
      this._addValidations(this.universalDosePane.getDosePaneValidations());
    }
    else if (!this.descriptiveDoseField.isHidden())
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: self.descriptiveDoseField,
        required: true
      }));
    }
    this.validationForm.addFormField(new tm.jquery.FormField({
      component: self.routesCombo,
      required: true,
      componentValueImplementationFn: function()
      {
        return self.routesCombo.getSelection();
      }
    }));

    if (!this.dosingFrequencyPane.isHidden())
    {
      this._addValidations(this.dosingFrequencyPane.getDosingFrequencyPaneValidations());
    }
    this._addValidations(this.therapyIntervalPane.getTherapyIntervalPaneValidations(this.editingStartTimestamp));
  },

  _addValidations: function(validation)
  {
    for (var i = 0; i < validation.length; i++)
    {
      this.validationForm.addFormField(validation[i]);
    }
  },

  _openVariableDoseEditPane: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var variableDosePane = new app.views.medications.ordering.SimpleVariableDosePane({
      view: self.view,
      startProcessOnEnter: true,
      height: 180,
      padding: 10,
      medicationData: {
        medicationIngredients: [
          {
            strengthNumeratorUnit: self.universalDosePane.getNumeratorUnit(),
            strengthDenominatorUnit: self.universalDosePane.getDenominatorUnit()
          }
        ]
      },
      timedDoseElements: this.timedDoseElements,
      frequency: this.dosingFrequencyPane.getFrequency()
    });

    var variableDoseEditDialog = appFactory.createDataEntryDialog(
        this.view.getDictionary('variable.dose'),
        null,
        variableDosePane,
        function(resultData)
        {
          if (resultData)
          {
            self.universalDosePane.hide();
            self.universalDosePane.clear();
            self.timedDoseElements = resultData.value.timedDoseElements;
            self._showVariableDoseDisplayValue();
            self.dosingFrequencyPane.setFrequency(resultData.value.frequency);
            self._showHideDosingFrequencyPane();
          }
        },
        400,
        350
    );
    var cancelButton = variableDoseEditDialog.getBody().footer.cancelButton;
    cancelButton.setText(this.view.getDictionary('remove.vario'));
    cancelButton.setType("link");
    var cancelButtonHandler = cancelButton.getHandler();
    cancelButton.setHandler(function()
    {
      self.universalDosePane.show();
      self.variableDoseContainer.hide();
      self.timedDoseElements.removeAll();
      self._showHideDosingFrequencyPane();
      cancelButtonHandler();
    });

    variableDoseEditDialog.getBody().footer.setRightButtons([cancelButton, variableDoseEditDialog.getBody().footer.confirmButton]);
    variableDoseEditDialog.show();
  },

  _createVerticalSpacer: function(size)
  {
    return new tm.jquery.Spacer({type: 'vertical', size: size});
  },

  _handleDosingFrequencyChange: function()
  {
    this.therapyIntervalPane.calculateStart(this.dosingFrequencyPane.getDaysOfWeek());
    this.therapyIntervalPane.calculateEnd();
  },

  _setVarioEnabled: function()
  {
    this.varioButton.setEnabled(this.universalDosePane.getNumeratorUnit() != null);
  },

  _showDoseComponent: function(doseForm)
  {
    if (doseForm && doseForm.medicationOrderFormType == 'DESCRIPTIVE')
    {
      this.universalDosePane.clear();
      this.universalDosePane.hide();
      this.varioButton.hide();
      this.descriptiveDoseField.show();
    }
    else
    {
      this.universalDosePane.show();
      this.varioButton.show();
      this.descriptiveDoseField.hide();
    }
  },

  _showHideDosingFrequencyPane: function()
  {
    if (this.timedDoseElements.length > 0)
    {
      this.dosingFrequencyTitle.hide();
      this.dosingFrequencyPane.hide();
    }
    else
    {
      this.dosingFrequencyTitle.show();
      this.dosingFrequencyPane.show();
    }
  },

  _showVariableDoseDisplayValue: function()
  {
    this.variableDoseContainer.removeAll(true);
    for (var n = 0; n < this.timedDoseElements.length; n++)
    {
      var rowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 10)});
      var timedDoseElement = this.timedDoseElements[n];
      var doseTime = timedDoseElement.doseTime;
      var timeDisplayValue = tm.views.medications.MedicationTimingUtils.hourMinuteToString(doseTime.hour, doseTime.minute) + '  ';
      rowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', timeDisplayValue, '1 0 0 0'));
      var doseDisplayValue =
          tm.views.medications.MedicationUtils.doubleToString(timedDoseElement.doseElement.quantity, 'n2') + ' ' +
              this.universalDosePane.getNumeratorUnit();
      var denominatorUnit = this.universalDosePane.getDenominatorUnit();
      if (denominatorUnit)
      {
        doseDisplayValue += ' / ' +
            tm.views.medications.MedicationUtils.doubleToString(timedDoseElement.doseElement.quantityDenominator, 'n2') + ' ' +
            denominatorUnit;
      }

      rowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', doseDisplayValue, 0));
      this.variableDoseContainer.add(rowContainer);
    }

    this.variableDoseContainer.show();
    this.variableDoseContainer.repaint();
  },

  _saveTherapy: function()
  {
    var self = this;
    var therapy = this._buildTherapy();
    var saveDateTime = this.saveDateTimePane.isHidden() ? null : this.saveDateTimePane.getSaveDateTime();

    var centralCaseData = this.view.getCentralCaseData();
    var saveUrl;
    var params;

    if (this.therapyToEdit)
    {
      therapy.compositionUid = this.therapyToEdit.compositionUid;
      therapy.ehrOrderName = this.therapyToEdit.ehrOrderName;
      saveUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MODIFY_THERAPY;
      params = {
        patientId: this.view.getPatientId(),
        therapy: JSON.stringify(therapy),
        centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
        careProviderId: centralCaseData ? centralCaseData.careProviderId : null,
        sessionId: centralCaseData && centralCaseData.sessionId ? centralCaseData.sessionId : null,
        knownOrganizationalEntity: this.view.getKnownOrganizationalEntity(),
        prescriber: JSON.stringify(this.performerContainer.getPerformer()),
        saveDateTime: JSON.stringify(saveDateTime)
      };
    }
    else
    {
      saveUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_MEDICATIONS_ORDER;
      params = {
        patientId: this.view.getPatientId(),
        therapies: JSON.stringify([therapy]),
        centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
        careProviderId: centralCaseData ? centralCaseData.careProviderId : null,
        sessionId: centralCaseData && centralCaseData.sessionId ? centralCaseData.sessionId : null,
        knownOrganizationalEntity: this.view.getKnownOrganizationalEntity(),
        prescriber: JSON.stringify(this.performerContainer.getPerformer()),
        roundsInterval: JSON.stringify(this.view.getRoundsInterval()),
        saveDateTime: JSON.stringify(saveDateTime)
      };
    }

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

  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var variableDose = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var therapy = {
      medicationOrderFormType: enums.medicationOrderFormType.SIMPLE,
      variable: variableDose,
      medication: {
        name: this.medicationField.getValue()
      },
      doseForm: this.doseFormCombo.getSelection(),
      route: this.routesCombo.getSelection(),
      quantityUnit: this.universalDosePane.getNumeratorUnit(),
      quantityDenominatorUnit: this.universalDosePane.getDenominatorUnit(),
      dosingFrequency: this.dosingFrequencyPane.getFrequency(),
      dosingDaysFrequency: this.dosingFrequencyPane.getDaysFrequency(),
      daysOfWeek: this.dosingFrequencyPane.getDaysOfWeek(),
      maxDailyFrequency: !tm.jquery.Utils.isEmpty(maxDailyFrequency) ? maxDailyFrequency : null,
      start: this.therapyIntervalPane.getStart(),
      end: this.therapyIntervalPane.getEnd(),
      whenNeeded: this.therapyIntervalPane.getWhenNeeded(),
      startCriterions: this._getStartCriterions(),
      comment: this.commentField.getValue() ? this.commentField.getValue() : null
    };

    if (variableDose)
    {
      therapy.timedDoseElements = this.timedDoseElements;
    }
    else
    {
      if (!this.universalDosePane.isHidden())
      {
        therapy.doseElement = this.universalDosePane.getDose();
      }
      else
      {
        therapy.doseElement = {doseDescription: this.descriptiveDoseField.getValue()};
      }
    }
    return therapy;
  },

  _getStartCriterions: function()
  {
    var startCriterions = [];
    if(this.therapyIntervalPane.getStartCriterion())
    {
      startCriterions.push(this.therapyIntervalPane.getStartCriterion());
    }
    if(this.dosingFrequencyPane.getTherapyApplicationCondition())
    {
      startCriterions.push(this.dosingFrequencyPane.getTherapyApplicationCondition());
    }
    return startCriterions;
  },

  _presentTherapy: function()
  {
    var therapy = this.therapyToEdit;
    this.medicationField.setValue(therapy.medication.name);
    this.doseFormCombo.setSelection(therapy.doseForm);
    this.routesCombo.setSelection(therapy.route);

    if (therapy.variable)
    {
      this.timedDoseElements = therapy.timedDoseElements;
      this.universalDosePane.hide();
      this._showVariableDoseDisplayValue();
      this._showHideDosingFrequencyPane();
    }
    else if (therapy.doseForm.medicationOrderFormType == 'DESCRIPTIVE')
    {
      this.descriptiveDoseField.setValue(therapy.doseElement.doseDescription);
    }
    else
    {
      this.universalDosePane.setValue(
          therapy.doseElement.quantity,
          therapy.quantityUnit,
          therapy.doseElement.quantityDenominator,
          therapy.quantityDenominatorUnit);
    }

    this.dosingFrequencyPane.setFrequency(therapy.dosingFrequency);
    this.dosingFrequencyPane.setDaysOfWeek(therapy.daysOfWeek);
    this.dosingFrequencyPane.setDaysFrequency(therapy.dosingDaysFrequency);
    this.therapyIntervalPane.setWhenNeeded(therapy.whenNeeded);
    this.therapyIntervalPane.setMaxDailyFrequency(therapy.maxDailyFrequency);
    this.commentField.setValue(therapy.comment);

    var enums = app.views.medications.TherapyEnums;
    for (var i = 0; i < therapy.startCriterions.length; i++)
    {
      var criterion = therapy.startCriterions[i];
      if (criterion == enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS)
      {
        this.therapyIntervalPane.setStartCriterion(criterion);
      }
      else if (criterion == enums.medicationStartCriterionEnum.BEFORE_MEAL || criterion == enums.medicationStartCriterionEnum.AFTER_MEAL)
      {
        this.dosingFrequencyPane.setTherapyApplicationCondition(criterion);
      }
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit();
  }
});

