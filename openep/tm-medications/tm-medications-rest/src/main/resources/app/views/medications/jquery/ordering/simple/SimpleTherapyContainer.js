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

Class.define('app.views.medications.ordering.SimpleTherapyContainer', 'tm.jquery.Container', {
  cls: "simple-therapy-container",
  scrollable: 'both',

  /** configs */
  view: null,
  editMode: false,
  copyMode: false,
  getTherapyStartNotBeforeDateFunction: null, //optional
  confirmTherapyEvent: null,
  saveDateTimePaneEvent: null,
  medicationsProviderFunction: null, //optional
  changeCardEvent: null,  //optional
  saveToTemplateFunction: null, //optional
  linkTherapyFunction: null,
  isPastMode: false,
  presetDate: null, //optional
  /** privates */
  validationForm: null,
  medicationData: null,
  timedDoseElements: null,
  therapyLink: null,
  /** privates: components */
  medicationInfo: null,
  removeButton: null,
  medicationField: null,
  routesPane: null,
  dosePane: null,
  variableDoseContainer: null,
  varioButton: null,
  dosingFrequencyTitle: null,
  dosingFrequencyPane: null,
  therapyDurationTitle: null,
  therapyStartTitle: null,
  therapyIntervalPane: null,
  commentField: null,
  indicationField: null,
  calculatedDosagePane: null,
  addToBasketButton: null,
  pastDaysOfTherapySpacer: null,
  pastDaysOfTherapyLabel: null,
  pastDaysOfTherapyField: null,
  maxFrequencyTitleLabel: null,

  templatesButton: null,
  complexTherapyButton: null,
  saveToTemplateButton: null,
  linkTherapyButton: null,
  COMPONENT_PADDING: 5,
  LABEL_PADDING: 2,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.timedDoseElements = [];
    this.therapyLink = null;
    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var medicationDetailsPane = new app.views.medications.therapy.MedicationDetailsCardPane({view: self.view});
    var detailsCardTooltip = appFactory.createDefaultPopoverTooltip(
        self.view.getDictionary("medication"),
        null,
        medicationDetailsPane
    );
    detailsCardTooltip.onShow = function()
    {
      medicationDetailsPane.setMedicationData(self.medicationData);
    };
    this.medicationInfo = new tm.jquery.Container({
      cls: 'info-icon pointer-cursor',
      width: 25,
      height: 30,
      margin: '0 0 0 5',
      tooltip: detailsCardTooltip
    });

    this.removeButton = new tm.jquery.Container({cls: 'remove-icon', width: 30, height: 30});
    this.removeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (self.changeCardEvent)
      {
        self.changeCardEvent('TEMPLATES');
      }
    });

    this.medicationField = tm.views.medications.MedicationUtils.createMedicationsSearchField(
        this.view,
        636,
        this.editMode ? 'advanced' : 'basic',
        null,
        this.editMode
    );

    if (this.medicationsProviderFunction)
    {
      this.medicationField.setSource(this.medicationsProviderFunction());
      this.medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
      {
        var selection = component.getSelection();
        if (selection)
        {
          self._readMedicationData(selection.id, function(medicationData)
          {
            if (!self.medicationData || medicationData.medication.id != self.medicationData.medication.id)
            {
              self.setMedicationData(medicationData, false);
            }
          })
        }
      });
    }

    this.routesPane = new app.views.medications.ordering.RoutesPane({view: this.view, height: 30, width: 678});
    this.dosePane = new app.views.medications.ordering.DosePane({
      margin: '0 0 0 5',
      view: this.view,
      numeratorChangeEvent: function()
      {
        self._calculateDosing();
      },
      focusLostEvent: function()
      {
        if (!self.routesPane.getSelectedRoute())
        {
          self.routesPane.requestFocus();
        }
        else
        {
          self.dosingFrequencyPane.requestFocus();
        }
      }
    });
    this.variableDoseContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "stretch"), margin: '0 0 0 10'});

    this.descriptiveDoseField = new tm.jquery.TextField({placeholder: this.view.getDictionary('single.dose'), width: 620});
    this.descriptiveDoseField.hide();

    this.varioButton = new tm.jquery.Button({
      text: 'Variable',
      handler: function()
      {
        var variableDates = self.timedDoseElements.length > 0 && self.timedDoseElements[0].date;
        self._openVariableDoseEditPane(variableDates);
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
      isPastMode: this.isPastMode,
      presetDate: this.presetDate,
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
    this.pastDaysOfTherapySpacer = this._createVerticalSpacer(2);
    this.pastDaysOfTherapyLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('consecutive.days.antibiotic'));
    this.pastDaysOfTherapyField = new tm.jquery.TextField({width: 68});
    this.commentField = new tm.jquery.TextField({width: 326});
    this.commentField.onKey(
        new tm.jquery.event.KeyStroke({key: "t", altKey: true, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.saveDateTimePaneEvent();
        });
    this.indicationField = new tm.jquery.TextField({width: 345});
    this.calculatedDosagePane = new app.views.medications.ordering.CalculatedDosagePane({view: this.view, height: 20});

    if (!this.editMode && !this.copyMode)
    {
      this.addToBasketButton = new tm.jquery.Button({
        text: this.view.getDictionary("add"),
        handler: function()
        {
          self.addToBasketButton.setEnabled(false);
          self.validateAndConfirmOrder();
        }
      });

      this.templatesButton = new tm.jquery.Button({
        text: this.view.getDictionary('empty.form'),
        type: "link",
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent('TEMPLATES');
          }
        }});
      this.complexTherapyButton = new tm.jquery.Button({
        text: this.view.getDictionary('expanded1'),
        type: "link",
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent(self.medicationData);
          }
        }});
      this.saveToTemplateButton = new tm.jquery.Button({
        text: this.view.getDictionary('add.to.template'),
        type: "link",
        handler: function()
        {
          if (self.saveToTemplateFunction)
          {
            var therapy = self._buildTherapy();
            self._setupValidation();
            var invalidTherapy = self.validationForm.hasFormErrors();
            self.saveToTemplateFunction(therapy, invalidTherapy);
          }
        }});
      this.linkTherapyButton = new tm.jquery.Button({
        text: "Link Medication",
        style: "width: 150px, text-align:left;",
        type: "link",
        handler: function()
        {
          self.linkTherapyFunction(function(startAfterTherapy, therapyLink)
          {
            if (startAfterTherapy.end)
            {
              self.therapyIntervalPane.setStart(new Date(startAfterTherapy.end));
              self.therapyLink = therapyLink;

              if (!tm.jquery.Utils.isEmpty(self.therapyLink))
              {
                self.linkTherapyButton.setCls("link_therapy_button_with_link");
              }
            }
          });
        }});
    }

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._confirm();
      },
      onValidationError: function()
      {
        if (self.confirmTherapyEvent)
        {
          self.confirmTherapyEvent("VALIDATION_FAILED");
        }
        if (self.addToBasketButton)
        {
          self.addToBasketButton.setEnabled(true);
        }
      },
      requiredFieldValidatorErrorMessage: self.view.getDictionary("field.value.is.required")
    });
  },

  _buildGui: function()
  {
    var mainContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "stretch"), margin: '10 0 0 20'});
    this.add(mainContainer);
    var medicationContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start")});
    medicationContainer.add(this.medicationField);
    medicationContainer.add(this.medicationInfo);
    mainContainer.add(medicationContainer);
    mainContainer.add(this._createVerticalSpacer(7));

    var doseRowContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({align: 'right'}), width: 678});
    if (!this.editMode && !this.copyMode)
    {
      doseRowContainer.add(this.removeButton);
    }
    doseRowContainer.add(new tm.jquery.Container({flex: 1}));
    doseRowContainer.add(this.varioButton);
    doseRowContainer.add(this.dosePane);
    doseRowContainer.add(this.variableDoseContainer);
    doseRowContainer.add(this.descriptiveDoseField);

    mainContainer.add(doseRowContainer);
    mainContainer.add(this._createVerticalSpacer(2));
    mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('route')));
    mainContainer.add(this.routesPane);
    mainContainer.add(this._createVerticalSpacer(2));
    this.dosingFrequencyTitle = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval'));
    mainContainer.add(this.dosingFrequencyTitle);
    mainContainer.add(this.dosingFrequencyPane);
    mainContainer.add(this._createVerticalSpacer(2));
    var therapyIntervalLabelsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start")});
    therapyIntervalLabelsContainer.add(this.therapyStartTitle);
    therapyIntervalLabelsContainer.add(this.therapyDurationTitle);
    therapyIntervalLabelsContainer.add(this.maxFrequencyTitleLabel);
    mainContainer.add(therapyIntervalLabelsContainer);
    mainContainer.add(this.therapyIntervalPane);
    mainContainer.add(this.pastDaysOfTherapySpacer);
    mainContainer.add(this.pastDaysOfTherapyLabel);
    mainContainer.add(this.pastDaysOfTherapyField);
    mainContainer.add(this._createVerticalSpacer(2));

    var commentLabelsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5)});
    commentLabelsContainer.add(new tm.jquery.Container({cls: 'TextLabel', html: this.view.getDictionary('commentary'), width: 328, padding : "5 0 0 0"}));
    commentLabelsContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('indication')));
    mainContainer.add(commentLabelsContainer);
    var commentsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5)});
    commentsContainer.add(this.commentField);
    commentsContainer.add(this.indicationField);
    mainContainer.add(commentsContainer);
    mainContainer.add(this._createVerticalSpacer(2));
    mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('calculated.dosing')));
    mainContainer.add(this.calculatedDosagePane);

    if (!this.editMode && !this.copyMode)
    {
      mainContainer.add(this._createVerticalSpacer(7));
      this.add(new tm.jquery.Container({style: 'border-top: 1px solid #d6d6d6'}));
      this.add(this._createVerticalSpacer(7));

      var navigationContainer = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("end", "center", 20),
        width: 678,
        margin: '0 0 0 20'
      });
      navigationContainer.add(this.templatesButton);
      navigationContainer.add(this.complexTherapyButton);
      navigationContainer.add(this.saveToTemplateButton);
      navigationContainer.add(this.linkTherapyButton);
      var addToBasketContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("end", "center"), width: 200});
      addToBasketContainer.add(this.addToBasketButton);
      navigationContainer.add(addToBasketContainer);
      this.add(navigationContainer);
    }

    //this.commentField.focus();
    this.commentField.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      component.getInputElement().focus();
    });
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();
    this._addValidations(this.routesPane.getRoutesPaneValidations());
    var therapyStartNotBefore = this.getTherapyStartNotBeforeDateFunction ? this.getTherapyStartNotBeforeDateFunction() : null;
    this._addValidations(this.therapyIntervalPane.getTherapyIntervalPaneValidations(therapyStartNotBefore));
    if (!this.dosePane.isHidden())
    {
      this._addValidations(this.dosePane.getDosePaneValidations());
    }
    else if (!this.descriptiveDoseField.isHidden())
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: self.descriptiveDoseField,
        required: true
      }));
    }
    if (!this.dosingFrequencyPane.isHidden())
    {
      this._addValidations(this.dosingFrequencyPane.getDosingFrequencyPaneValidations());
    }

    if (!this.pastDaysOfTherapyField.isHidden())
    {
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
    }
  },

  _addValidations: function(validation)
  {
    for (var i = 0; i < validation.length; i++)
    {
      this.validationForm.addFormField(validation[i]);
    }
  },

  _openVariableDoseEditPane: function (variableDays)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var variableDoseEditDialog;
    var variableDosePane;
    if (variableDays)
    {
      variableDosePane = new app.views.medications.ordering.SimpleVariableDoseDaysPane({
        view: self.view,
        startProcessOnEnter: true,
        padding: 10,
        height: 180,
        medicationData: this.medicationData,
        timedDoseElements: this.timedDoseElements,
        frequency: this.dosingFrequencyPane.getFrequency()
      });
    }
    else
    {
      variableDosePane = new app.views.medications.ordering.SimpleVariableDosePane({
        view: self.view,
        startProcessOnEnter: true,
        padding: 10,
        height: 180,
        medicationData: this.medicationData,
        timedDoseElements: this.timedDoseElements,
        frequency: this.dosingFrequencyPane.getFrequency()
      });
    }

    variableDoseEditDialog = appFactory.createDataEntryDialog(
        this.view.getDictionary('variable.dose'),
        null,
        variableDosePane,
        function (resultData)
        {
          if (resultData)
          {
            self.dosePane.hide();
            self.dosePane.clear();
            self.timedDoseElements = resultData.value.timedDoseElements;
            self._showVariableDoseDisplayValue();
            self.dosingFrequencyPane.setFrequency(resultData.value.frequency);
            self._showHideDosingFrequencyPane();
          }
        },
        800,
        500
    );
    var removeVarioButton = variableDoseEditDialog.getBody().footer.cancelButton;
    removeVarioButton.setText(this.view.getDictionary('remove.vario'));
    removeVarioButton.setType("link");
    var cancelButtonHandler = removeVarioButton.getHandler();
    removeVarioButton.setHandler(function ()
    {
      self.dosePane.show();
      self.variableDoseContainer.hide();
      self.timedDoseElements.removeAll();
      self._showHideDosingFrequencyPane();
      self.dosePane.requestFocusToNumerator();
      cancelButtonHandler();
    });

    if (variableDays)
    {
      variableDoseEditDialog.getBody().footer.setLeftButtons([removeVarioButton]);
    }
    else
    {
      var switchToVariableDaysButton = new tm.jquery.Button({
        text: "Protocol", //TODO Mitja
        type: "link",
        handler: function ()
        {
          variableDoseEditDialog.hide();
          self._openVariableDoseEditPane(true);
        }});
      variableDoseEditDialog.getBody().footer.setLeftButtons([removeVarioButton, switchToVariableDaysButton]);
    }
    variableDoseEditDialog.getBody().footer.setRightButtons([variableDoseEditDialog.getBody().footer.confirmButton]);
    variableDoseEditDialog.show();
  },

  _createVerticalSpacer: function(size)
  {
    return new tm.jquery.Spacer({type: 'vertical', size: size});
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
    if (this.timedDoseElements.length > 0)
    {
      if (this.timedDoseElements[0].date)
      {
        this._showDaysVariableDoseDisplayValue();
      }
      else
      {
        this._showHoursVariableDoseDisplayValue();
      }
    }

    this.variableDoseContainer.show();
    this.variableDoseContainer.repaint();
  },

  _showDaysVariableDoseDisplayValue: function()
  {
    var self = this;
    this.variableDoseContainer.add(this.linkTherapyButton = new tm.jquery.Button({
      text: "VARIABLE DOSE",
      type: "link",
      handler: function()
      {
        self._openVariableDoseEditPane(true);
      }}));
  },

  _showHoursVariableDoseDisplayValue: function()
  {
    var self = this;
    for (var n = 0; n < this.timedDoseElements.length; n++)
    {
      var rowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 10)});
      var timedDoseElement = this.timedDoseElements[n];
      var doseTime = timedDoseElement.doseTime;
      var timeDisplayValue = tm.views.medications.MedicationTimingUtils.hourMinuteToString(doseTime.hour, doseTime.minute) + '  ';
      rowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', timeDisplayValue, '1 0 0 0'));
      var definingIngredient = tm.views.medications.MedicationUtils.getDefiningIngredient(self.medicationData);
      var numeratorUnit = definingIngredient ? definingIngredient.strengthNumeratorUnit : this.medicationData.basicUnit;
      var doseDisplayValue = tm.views.medications.MedicationUtils.doubleToString(timedDoseElement.doseElement.quantity, 'n2') + ' ' + numeratorUnit;
      if (definingIngredient && timedDoseElement.doseElement.quantityDenominator)
      {
        doseDisplayValue += ' / ' +
            tm.views.medications.MedicationUtils.doubleToString(timedDoseElement.doseElement.quantityDenominator, 'n2') + ' ' +
            definingIngredient.strengthDenominatorUnit;
      }

      rowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', doseDisplayValue, 0));
      this.variableDoseContainer.add(rowContainer);
    }
  },

  _calculateDosing: function()
  {
    if (this.medicationData)
    {
      var quantity = this.dosePane.getDose().quantity;
      var quantityUnit = tm.views.medications.MedicationUtils.getStrengthNumeratorUnit(this.medicationData);
      var timesPerDay = this.dosingFrequencyPane.getFrequencyTimesPerDay();
      var weightInKg = this.view.getReferenceWeight() || 0;
      var heightInCm = this.view.getPatientData() ? this.view.getPatientData().heightInCm : 0;
      this.calculatedDosagePane.calculate(quantity, quantityUnit, timesPerDay, weightInKg, heightInCm, false);
    }
  },

  _confirm: function()
  {
    var therapy = this._buildTherapy();
    var confirmSuccess = this.confirmTherapyEvent(therapy);
    if (confirmSuccess == false)
    {
      this.addToBasketButton.setEnabled(true);
    }
  },

  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var variableDose = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var therapy = {
      medicationOrderFormType: enums.medicationOrderFormType.SIMPLE,
      variable: variableDose,
      medication: this.medicationData.medication,
      doseForm: this.medicationData.doseForm,
      route: this.routesPane.getSelectedRoute(),
      quantityUnit: tm.views.medications.MedicationUtils.getStrengthNumeratorUnit(this.medicationData),
      quantityDenominatorUnit: tm.views.medications.MedicationUtils.getStrengthDenominatorUnit(this.medicationData),
      dosingFrequency: this.dosingFrequencyPane.getFrequency(),
      dosingDaysFrequency: this.dosingFrequencyPane.getDaysFrequency(),
      daysOfWeek: this.dosingFrequencyPane.getDaysOfWeek(),
      maxDailyFrequency: !tm.jquery.Utils.isEmpty(maxDailyFrequency) ? maxDailyFrequency : null,
      start: this.therapyIntervalPane.getStart(),
      end: this.therapyIntervalPane.getEnd(),
      whenNeeded: this.therapyIntervalPane.getWhenNeeded(),
      startCriterions: this._getStartCriterions(),
      comment: this.commentField.getValue() ? this.commentField.getValue() : null,
      clinicalIndication: this.indicationField.getValue() ? this.indicationField.getValue() : null,
      linkFromTherapy: this.therapyLink
    };

    if (!this.pastDaysOfTherapyField.isHidden())
    {
      therapy.pastDaysOfTherapy = this.pastDaysOfTherapyField.getValue() == 0 ? null : this.pastDaysOfTherapyField.getValue();
    }

    if (variableDose)
    {
      therapy.timedDoseElements = this.timedDoseElements;
    }
    else
    {
      if (!this.dosePane.isHidden())
      {
        therapy.doseElement = this.dosePane.getDose();
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
    if (this.therapyIntervalPane.getStartCriterion())
    {
      startCriterions.push(this.therapyIntervalPane.getStartCriterion());
    }
    if (this.dosingFrequencyPane.getTherapyApplicationCondition())
    {
      startCriterions.push(this.dosingFrequencyPane.getTherapyApplicationCondition());
    }
    return startCriterions;
  },

  _handleDosingFrequencyChange: function()
  {
    this.therapyIntervalPane.calculateStart(this.dosingFrequencyPane.getDaysOfWeek());
    this.therapyIntervalPane.calculateEnd();
    this._calculateDosing();
  },

  _showDoseComponent: function(medicationData)
  {
    if (medicationData.doseForm && medicationData.doseForm.medicationOrderFormType == 'DESCRIPTIVE')
    {
      this.dosePane.hide();
      this.varioButton.hide();
      this.descriptiveDoseField.show();
    }
    else
    {
      this.dosePane.show();
      this.varioButton.show();
      this.descriptiveDoseField.hide();
    }
  },

  _clear: function()
  {
    this.timedDoseElements = [];
    this.therapyLink = null;
    if (this.linkTherapyButton)
    {
      this.linkTherapyButton.setCls("");
    }
    this.variableDoseContainer.hide();
    this.dosingFrequencyPane.clear();
    this.therapyIntervalPane.clear();
    this.pastDaysOfTherapyField.setValue(null);
    this.commentField.setValue(null);
    this.indicationField.setValue(null);
    this.therapyDurationTitle.show();
    this._showHideDosingFrequencyPane();
  },

  _readMedicationData: function(medicationId, medicationDataEvent)
  {
    if (!tm.jquery.Utils.isEmpty(medicationId))
    {
      var medicationDataUrl =
          this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MEDICATION_DATA;
      var params = {medicationId: medicationId};
      this.view.loadViewData(medicationDataUrl, params, null, function(data)
      {
        medicationDataEvent(data);
      });
    }
  },

  /** public methods */
  setMedicationData: function(medicationData, clear)
  {
    var self = this;
    this.medicationData = medicationData;
    this.medicationField.setSelection(medicationData.medication);
    this.routesPane.setRoutes(medicationData.routes, medicationData.defaultRoute);
    this.dosePane.setMedicationData(medicationData);
    this.validationForm.reset();
    this.descriptiveDoseField.setValue(null);
    if (this.addToBasketButton)
    {
      this.addToBasketButton.setEnabled(true);
    }
    this._showDoseComponent(medicationData);
    if (clear)
    {
      this._clear();
    }

    setTimeout(function()
    {
      if (!self.dosePane.isHidden())
      {
        self.dosePane.requestFocusToNumerator();
      }
      else if (!self.descriptiveDoseField.isHidden())
      {
        self.descriptiveDoseField.focus();
      }
    }, 0);

    if (!medicationData.antibiotic)
    {
      this.pastDaysOfTherapySpacer.hide();
      this.pastDaysOfTherapyLabel.hide();
      this.pastDaysOfTherapyField.hide();
    }
    else
    {
      this.pastDaysOfTherapySpacer.show();
      this.pastDaysOfTherapyLabel.show();
      this.pastDaysOfTherapyField.show();
    }

    this.pastDaysOfTherapyField.setEnabled(!this.editMode);
  },

  validateAndConfirmOrder: function()
  {
    this._setupValidation();
    this.validationForm.submit();
  },

  setSimpleTherapy: function(therapy, setTherapyInterval)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    this._readMedicationData(therapy.medication.id, function(medicationData)
    {
      if (medicationData)
      {
        self.setMedicationData(medicationData, true);
        if (therapy.variable)
        {
          self.timedDoseElements = therapy.timedDoseElements;
          self.dosePane.hide();
          self._showVariableDoseDisplayValue();
          self._showHideDosingFrequencyPane();
        }
        else if (medicationData.doseForm && medicationData.doseForm.medicationOrderFormType == 'DESCRIPTIVE')
        {
          self.descriptiveDoseField.setValue(therapy.doseElement.doseDescription);
        }
        else
        {
          self.dosePane.setDoseNumerator(therapy.doseElement.quantity);
        }
        self.routesPane.setSelectedRoute(therapy.route);
      }
      else
      {
        var message = self.view.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            self.view.getDictionary('abort.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 320, 150).show();
      }
      self.dosingFrequencyPane.setFrequency(therapy.dosingFrequency);
      self.dosingFrequencyPane.setDaysOfWeek(therapy.daysOfWeek);
      self.dosingFrequencyPane.setDaysFrequency(therapy.dosingDaysFrequency);
      self.therapyIntervalPane.setWhenNeeded(therapy.whenNeeded);
      self.therapyIntervalPane.setMaxDailyFrequency(therapy.maxDailyFrequency);
      self.commentField.setValue(therapy.comment);
      self.indicationField.setValue(therapy.clinicalIndication);
      self.pastDaysOfTherapyField.setValue(therapy.pastDaysOfTherapy);

      var enums = app.views.medications.TherapyEnums;
      for (var i = 0; i < therapy.startCriterions.length; i++)
      {
        var criterion = therapy.startCriterions[i];
        if (criterion == enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS)
        {
          self.therapyIntervalPane.setStartCriterion(criterion);
        }
        else if (criterion == enums.medicationStartCriterionEnum.BEFORE_MEAL || criterion == enums.medicationStartCriterionEnum.AFTER_MEAL)
        {
          self.dosingFrequencyPane.setTherapyApplicationCondition(criterion);
        }
      }

      if (setTherapyInterval)
      {
        self.therapyIntervalPane.setStart(therapy.start && therapy.start.data ? therapy.start.data : therapy.start);
        self.therapyIntervalPane.setEnd(therapy.end && therapy.end.data ? therapy.end.data : therapy.end);
      }
    })
  }
});

