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
  therapyAlreadyStarted: false,
  therapyModifiedInThePast: false,
  saveTherapyFunction: null, //optional
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
  therapyIntervalPane: null,
  commentField: null,
  saveDateTimePane: null,
  therapyNextAdministrationLabelPane: null,
  administrationPreviewTimeline: null,
  warningsContainer: null,

  _previewRefreshTimer: null,


  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.timedDoseElements = [];

    if (this.therapyToEdit)
    {
      this.editingStartTimestamp = CurrentTime.get();
      this.editingStartTimestamp.setSeconds(0);
      this.editingStartTimestamp.setMilliseconds(0);
    }

    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', "stretch", 0));
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
    this.therapyIntervalPane.setEnd(null);

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

    this.medicationField = new tm.jquery.TextField({cls: "medication-field", width: 644});

    this.doseFormCombo = new tm.jquery.TypeaheadField({
      cls: "dose-form-combo",
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
      cls: "routes-combo",
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
    this.variableDoseContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"), margin: '0 0 0 10'});
    this.variableDoseContainer.hide();

    this.descriptiveDoseField = new tm.jquery.TextField({cls: "description-dose-field", placeholder: this.view.getDictionary('single.dose'), width: 678});
    this.descriptiveDoseField.hide();

    this.varioButton = new tm.jquery.Button({
      cls: "vario-button",
      text: this.view.getDictionary('variable'),
      type: 'link',
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

    this.therapyIntervalPane = new app.views.medications.ordering.TherapyIntervalPane({
      view: this.view,
      width: 678,
      getFrequencyDataFunction: function()
      {
        return {
          frequencyKey: self.dosingFrequencyPane.getFrequencyKey(),
          frequencyType: self.dosingFrequencyPane.getFrequencyType(),
          frequencyMode: self.dosingFrequencyPane.getFrequencyMode()
        }
      },
      getDosingPatternFunction: function()
      {
        var variableDose = self.timedDoseElements.length > 0;
        if (variableDose)
        {
          var dosingPattern = [];
          self.timedDoseElements.forEach(function(element)
          {
            dosingPattern.push(element.doseTime);
          });
          return dosingPattern;
        }
        else
        {
          return self.dosingFrequencyPane.getDosingPattern();
        }
      }
    });
    this.therapyIntervalPane.on(app.views.medications.ordering.TherapyIntervalPane.EVENT_TYPE_INTERVAL_CHANGE,
        function(component, componentEvent)
        {
          var eventData = componentEvent.eventData;
          self.therapyNextAdministrationLabelPane.setNextAdministration(eventData.start);
          self.refreshAdministrationPreview();
        });

    this.therapyNextAdministrationLabelPane = new app.views.medications.ordering.TherapyNextAdministrationLabelPane({
      view: this.view
    });

    this.administrationPreviewTimeline = new app.views.medications.ordering.AdministrationPreviewTimeline({
      margin: "10 20 0 0",
      view: this.view
    });

    this.commentField = new tm.jquery.TextField({cls: "comment-field", width: 644});
    this.commentField.onKey(
        new tm.jquery.event.KeyStroke({key: "t", altKey: true, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.saveDateTimePaneEvent();
        });
    this.warningsContainer = new tm.jquery.Container({
      cls: "warnings-container",
      padding: '5 0 5 0',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start")
    });
    this.warningsContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('warnings')));
    var noWarningsLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('clinical.screening.not.possible'), '5 0 0 5');
    noWarningsLabel.style = 'color: #646464; text-transform: none;';
    this.warningsContainer.add(noWarningsLabel);

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane();
    this.saveDateTimePane.hide();

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        var therapy = self._buildTherapy();
        if (self.saveTherapyFunction)
        {
          self.saveTherapyFunction(therapy, self.view.getCurrentUserAsCareProfessional());
        }
        else
        {
          self._saveTherapy(therapy);
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
    doseRowContainer.add(new tm.jquery.Container({flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")}));
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

    this.add(this.therapyIntervalPane);
    this.add(this.therapyNextAdministrationLabelPane);
    this.add(this.administrationPreviewTimeline);
    this.add(this._createVerticalSpacer(2));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('commentary')));
    this.add(this.commentField);
    this.add(this.warningsContainer);
    this.add(new tm.jquery.Container({flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")}));
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
    var universalTherapy = app.views.medications.common.TherapyJsonConverter.convert({
      medication: new app.views.medications.common.dto.Medication(),
      quantityUnit: self.universalDosePane.getNumeratorUnit(),
      quantityDenominatorUnit: self.universalDosePane.getDenominatorUnit()
    });
    var variableDosePane = new app.views.medications.ordering.SimpleVariableDosePane({
      view: self.view,
      startProcessOnEnter: true,
      height: 180,
      padding: 10,
      medicationData: tm.views.medications.MedicationUtils.getMedicationDataFromSimpleTherapy(universalTherapy),
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
            self.dosingFrequencyPane.setFrequency(resultData.value.frequency, false);
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
    var therapy = this._buildTherapy();
    this._calculateStartAndEnd(therapy);
  },

  _calculateStartAndEnd: function(therapy)
  {
    var self = this;
    var oldTherapy = this.therapyToEdit ? this.therapyToEdit : null;
    this.therapyIntervalPane.calculateStart(therapy, tm.jquery.Utils.isEmpty(oldTherapy), oldTherapy, function()
    {
      self.therapyIntervalPane.calculateEnd();
    });
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
    var utils = tm.views.medications.MedicationUtils;
    this.variableDoseContainer.removeAll(true);
    for (var n = 0; n < this.timedDoseElements.length; n++)
    {
      var rowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 10)});
      var timedDoseElement = this.timedDoseElements[n];
      var doseTime = timedDoseElement.doseTime;
      var timeDisplayValue = tm.views.medications.MedicationTimingUtils.hourMinuteToString(doseTime.hour, doseTime.minute) + '  ';
      rowContainer.add(utils.crateLabel('TextLabel', timeDisplayValue, '1 0 0 0'));
      var doseDisplayValue =
          utils.getFormattedDecimalNumber(utils.doubleToString(timedDoseElement.doseElement.quantity, 'n2')) + ' ' +
          this.universalDosePane.getNumeratorUnit();
      var denominatorUnit = this.universalDosePane.getDenominatorUnit();
      if (denominatorUnit)
      {
        doseDisplayValue += ' / ' +
            utils.getFormattedDecimalNumber(utils.doubleToString(timedDoseElement.doseElement.quantityDenominator, 'n2')) + ' ' +
            denominatorUnit;
      }

      rowContainer.add(utils.crateLabel('TextData', doseDisplayValue, 0));
      this.variableDoseContainer.add(rowContainer);
    }

    this.variableDoseContainer.show();
    this.variableDoseContainer.repaint();
  },

  _saveTherapy: function(therapy)
  {
    var self = this;
    var saveDateTime = this.saveDateTimePane.isHidden() ? null : this.saveDateTimePane.getSaveDateTime();
    var prescriber = this.view.getCurrentUserAsCareProfessional();
    
    if (this.therapyToEdit)
    {
      therapy.setCompositionUid(this.therapyToEdit.getCompositionUid());
      therapy.setEhrOrderName(this.therapyToEdit.getEhrOrderName());

      this.view.getRestApi().modifyTherapy(therapy, null, prescriber, this.therapyAlreadyStarted, saveDateTime, true)
          .then(function onSuccess()
              {
                self.resultCallback(new app.views.common.AppResultData({success: true}));
              },
              function onFailure()
              {
                self.resultCallback(new app.views.common.AppResultData({success: false}));
              });
    }
    else
    {
      var medicationOrder = [
        new app.views.medications.common.dto.SaveMedicationOrder({
          therapy: therapy,
          actionEnum: app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE
        })
      ];
      this.view.getRestApi().saveMedicationsOrder(medicationOrder, prescriber, saveDateTime, null, true)
          .then(function onSuccess()
              {
                self.resultCallback(new app.views.common.AppResultData({success: true}));
              },
              function onFailure()
              {
                self.resultCallback(new app.views.common.AppResultData({success: false}));
              });
    }
  },

  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var variableDose = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var therapy = new app.views.medications.common.dto.Therapy({
      medicationOrderFormType: enums.medicationOrderFormType.SIMPLE,
      variable: variableDose,
      medication: {
        name: this.medicationField.getValue()
      },
      doseForm: this.doseFormCombo.getSelection(),
      routes: [this.routesCombo.getSelection()],
      quantityUnit: this.universalDosePane.getNumeratorUnit(),
      quantityDenominatorUnit: this.universalDosePane.getDenominatorUnit(),
      dosingFrequency: this.dosingFrequencyPane.getFrequency(),
      dosingDaysFrequency: this.dosingFrequencyPane.getDaysFrequency(),
      daysOfWeek: this.dosingFrequencyPane.getDaysOfWeek(),
      maxDailyFrequency: !tm.jquery.Utils.isEmpty(maxDailyFrequency) ? maxDailyFrequency : null,
      start: this.therapyIntervalPane.getStart(),
      end: this.therapyIntervalPane.getEnd(),
      whenNeeded: this.therapyIntervalPane.getWhenNeeded(),
      startCriterion: this.therapyIntervalPane.getStartCriterion(),
      reviewReminderDays: this.therapyIntervalPane.getReviewReminderDays(),
      applicationPrecondition: this.dosingFrequencyPane.getApplicationPrecondition(),
      comment: this.commentField.getValue() ? this.commentField.getValue() : null
    });

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
      var dosingPattern = this.dosingFrequencyPane.getDosingPattern();
      var frequencyType = this.dosingFrequencyPane.getFrequencyType();
      if (frequencyType == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        therapy.doseTimes = dosingPattern.length > 0 ? [dosingPattern[0]] : [];
      }
      else
      {
        therapy.doseTimes = dosingPattern;
      }
    }
    return therapy;
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

    this.dosingFrequencyPane.setDosingFrequencyAndPattern(therapy.dosingFrequency, therapy.doseTimes);
    this.dosingFrequencyPane.setDaysOfWeek(therapy.daysOfWeek);
    this.dosingFrequencyPane.setDaysFrequency(therapy.dosingDaysFrequency);
    this.therapyIntervalPane.setWhenNeeded(therapy.whenNeeded);
    this.therapyIntervalPane.setStartCriterion(therapy.startCriterion);
    this.therapyIntervalPane.setReviewReminderDays(therapy.reviewReminderDays);
    this.dosingFrequencyPane.setApplicationPrecondition(therapy.applicationPrecondition);
    this.therapyIntervalPane.setMaxDailyFrequency(therapy.maxDailyFrequency);
    if (!this.therapyToEdit)
    {
      this.therapyIntervalPane.setEnd(therapy.end);
    }
    this.commentField.setValue(therapy.comment);

    var therapyStart = therapy.start;
    var therapyHasAlreadyStarted = new Date(therapyStart) < CurrentTime.get();
    if (!therapyHasAlreadyStarted)
    {
      this.therapyIntervalPane.setStartOptionsFromPattern();
      this.therapyIntervalPane.setStart(therapyStart);
    }
    else
    {
      this._calculateStartAndEnd(therapy);
    }

    if (this.therapyToEdit && (this.therapyAlreadyStarted || this.therapyModifiedInThePast))
    {
      this.therapyNextAdministrationLabelPane.setOldTherapyId(therapy.compositionUid, therapy.ehrOrderName, false);
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit();
  },

  refreshAdministrationPreview: function()
  {
    var self = this;
    /* add a small delay so we don't call it too often*/
    clearTimeout(this._previewRefreshTimer);

    this._previewRefreshTimer = setTimeout(function ()
    {
      if (self.isRendered())
      {
        self.administrationPreviewTimeline._refreshAdministrationPreviewImpl(self.therapyIntervalPane.getStart(), self._buildTherapy());
      }
    }, 150);
  }
});

