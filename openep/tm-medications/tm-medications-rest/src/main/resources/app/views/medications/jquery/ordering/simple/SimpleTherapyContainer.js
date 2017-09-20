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
  scrollable: 'vertical',

  /** configs */
  view: null,
  editMode: false,
  copyMode: false,
  withStartEndTime: true,
  withSupply: false, /* if set to true it will set withStartEndTime to false */
  getTherapyStartNotBeforeDateFunction: null, //optional
  confirmTherapyEvent: null,
  saveDateTimePaneEvent: null,
  changeCardEvent: null,  //optional
  saveToTemplateFunction: null, //optional
  isPastMode: false,
  therapyAlreadyStarted: false,
  changeReasonRequired: false,
  bnfMaximumPercentage: null,
  showBnf: null,
  showparacetamolContainer: null,
  showSource: false,
  /** privates */
  therapyToEdit: null,
  validationForm: null,
  medicationData: null,
  timedDoseElements: null,
  medicationRuleUtils: null,
  repeatProtocolUntilCanceled: false,
  protocolEndDate: null,
  /** privates: components */
  medicationInfo: null,
  removeButton: null,
  medicationField: null,
  highRiskIconsContainer: null,
  routesPane: null,
  dosePane: null,
  variableDoseContainer: null,
  varioButton: null,
  dosingFrequencyTitle: null,
  dosingFrequencyPane: null,
  therapyIntervalPane: null,
  therapySupplyContainer: null,
  commentIndicationPane: null,
  bnfMaximumPane: null,
  paracetamolLimitContainer: null,
  overdosePane: null,
  changeReasonPane: null,
  calculatedDosagePane: null,
  warningsContainer: null,
  addToBasketButton: null,
  pastDaysOfTherapySpacer: null,
  pastDaysOfTherapyLabel: null,
  pastDaysOfTherapyField: null,
  therapyNextAdministrationLabelPane: null,
  sourceLabel: null,
  sourceField: null,

  templatesButton: null,
  complexTherapyButton: null,
  saveToTemplateButton: null,
  administrationPreviewTimeline: null,

  contentExtensions: null,

  visibilityContext: null,

  _previewRefreshTimer: null,
  _overridenCriticalWarnings: null,
  _extensionsPlaceholder: null,
  _toggleTitrationButton: null,
  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.timedDoseElements = [];
    if (tm.jquery.Utils.isEmpty(this.contentExtensions))
    {
      this.contentExtensions = [];
    }
    if (this.withSupply)
    {
      this.withStartEndTime = false;
    }

    this.medicationRuleUtils = this.getConfigValue("medicationRuleUtils",
        new tm.views.medications.MedicationRuleUtils({view: this.view}));

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'simple-therapy-container-coordinator',
      view: this.getView(),
      component: this
    });

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    this.medicationInfo = new tm.jquery.Container({
      cls: 'info-icon pointer-cursor medication-info',
      width: 25,
      height: 30,
      hidden: true
    });

    this.removeButton = new tm.jquery.Container({cls: 'remove-icon clear-button', width: 30, height: 30});
    this.removeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self.warningsContainer.clear();
      self.warningsContainer.repaint();
      if (self.changeCardEvent)
      {
        self.changeCardEvent('TEMPLATES');
      }
    });

    this.medicationField = new app.views.medications.common.MedicationSearchField({
      view: this.view,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      enabled: false,
      dropdownWidth: "stretch",
      dropdownAppendTo: this.view.getAppFactory().getDefaultRenderToElement() /* due to the dialog use */
    });
    
    this.medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
    {
      var medication = component.getSelectionMedication();
      if (medication && (!self.medicationData || self.medicationData.getMedication().getId() !== medication.getId()))
      {
        self.getView().getRestApi().loadMedicationData(medication.getId()).then(function setData(medicationData)
        {
          self.setMedicationData(medicationData, false);
        });
      }
    });

    this.highRiskIconsContainer = new app.views.medications.ordering.HighRiskMedicationIconsContainer({
      view: this.view
    });

    this.routesPane = new app.views.medications.ordering.RoutesPane({
      view: this.view,
      height: 30,
      width: 678,
      discretionaryRoutesDisabled: false,
      changeEvent: function(selectedRoutes)
      {
        if (self._isBnfMaximumDefined())
        {
          self.bnfMaximumPane.setBnfValuesAndNumeratorUnit(self.medicationData, selectedRoutes[0]);
          self.setBnfPaneShowed(true);
          self._handleBnfWarningChange(self.bnfMaximumPane.getPercentage());
        }
        else
        {
          self.setBnfPaneShowed(false);
          self.removeBnfWarning();
        }
      }
    });

    this.dosePane = new app.views.medications.ordering.DosePane({
      cls: "dose-pane with-large-input",
      margin: '0 0 0 5',
      view: this.view,
      addDosageCalculationPane: true,
      addDosageCalcBtn: true,
      showRounding: true,
      doseRangeEnabled: this.getView().isDoseRangeEnabled(),
      numeratorChangeEvent: function()
      {
        self._calculateDosing();
      },
      focusLostEvent: function()
      {
        if (self.routesPane.getSelectedRoutes().length === 0)
        {
          self.routesPane.requestFocus();
        }
        else
        {
          self.dosingFrequencyPane.requestFocus();
        }
      }
    });
    this.variableDoseContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      margin: '0 0 0 10'
    });

    this.descriptiveDoseField = new tm.jquery.TextField({
      cls: "descriptive-dose-field", placeholder: this.view.getDictionary('single.dose'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%"),
      hidden: true
    });

    this.varioButton = new tm.jquery.Button({
      testAttribute: 'variable-dose-button',
      cls: "vario-button",
      text: this.view.getDictionary('variable'),
      type: 'link',
      margin: '0 40 0 0',
      handler: function()
      {
        var variableDates = tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(self.timedDoseElements);
        self._openVariableDoseEditPane(variableDates);
      }
    });

    this.bnfMaximumPane = new app.views.medications.ordering.BnfMaximumPane({
      padding: "5 20 0 0",
      alignSelf: "center",
      view: this.view,
      percentage: this.getBnfMaximumPercentage()
    });

    this.paracetamolLimitContainer = new app.views.medications.ordering.ParacetamolLimitContainer({view: this.view});

    this.overdosePane = new app.views.medications.ordering.OverdoseContainer({
      view: this.view,
      alignSelf: "center",
      padding: "5 0 0 0",
      hidden: true
    });

    this.dosingFrequencyPane = new app.views.medications.ordering.DosingFrequencyPane({
      view: this.view,
      width: 678,
      visibilityContext: this.visibilityContext,
      editMode: this.isEditMode(),
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
    if (this.visibilityContext &&
        this.visibilityContext.hasOwnProperty("daysButtonVisibile") &&
        this.visibilityContext.daysButtonVisibile === false)
    {
      this.dosingFrequencyPane.hideDaysButton();
    }
    this.therapyIntervalPane = new app.views.medications.ordering.TherapyIntervalPane({
      view: this.view,
      width: 678,
      withStartEndTime: this.withStartEndTime,
      isPastMode: this.isPastMode,
      hidden: this.withSupply === true,
      copyMode: this.isCopyMode(),
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
      },
      onMaxDailyFrequencyFieldFocusLost: function()
      {
        self._handleParacetamolRuleChange();
      }
    });
    this.therapyIntervalPane.on(app.views.medications.ordering.TherapyIntervalPane.EVENT_TYPE_INTERVAL_CHANGE,
        function(component, componentEvent)
        {
          var eventData = componentEvent.eventData;
          self.therapyNextAdministrationLabelPane.setNextAdministration(eventData.start);
          if (self.withStartEndTime === true)
          {
            self.refreshAdministrationPreview();
          }
        });

    if (this.withStartEndTime !== true && this.withSupply === true)
    {
      this.therapySupplyContainer = new app.views.medications.ordering.TherapySupplyContainer({
        view: this.view
      });
    }

    this.therapyNextAdministrationLabelPane = new app.views.medications.ordering.TherapyNextAdministrationLabelPane({
      hidden: this.withStartEndTime === false,
      view: this.view
    });

    if (this.withStartEndTime === true)
    {
      this.administrationPreviewTimeline = new app.views.medications.ordering.AdministrationPreviewTimeline({
        margin: "10 20 0 0",
        view: this.view
      });
    }

    if (this.showSource)
    {
      this.sourceLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary("source"));
      this.sourceField = new tm.jquery.SelectBox({
        dropdownHeight: 5,
        dropdownWidth: "stretch",
        options: tm.views.medications.MedicationUtils.createTherapySourceSelectBoxOptions(this.view),
        selections: [],
        multiple: false,
        allowSingleDeselect: true,
        placeholder: " ",
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        padding: "0 20 0 0",
        appendTo: function()
        {
          return self.view.getAppFactory().getDefaultRenderToElement();
        }
      });
    }

    this.pastDaysOfTherapySpacer = this._createVerticalSpacer(2);
    this.pastDaysOfTherapyLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('consecutive.days.antibiotic'));
    this.pastDaysOfTherapyField = new tm.jquery.TextField({cls: "therapy-pat-days", width: 68});
    this._hidePastDaysOfTherapy();
    this.commentIndicationPane = new app.views.medications.ordering.CommentIndicationPane({
      view: this.view,
      visibilityContext: this.visibilityContext,
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePaneEvent();
      }
    });
    this.changeReasonPane = new app.views.medications.ordering.ChangeReasonPane({
      padding: "5 20 0 0",
      view: this.view,
      hidden: !this.isChangeReasonRequired()
    });
    this.calculatedDosagePane = new app.views.medications.ordering.CalculatedDosagePane({view: this.view, height: 20});
    this.warningsContainer = new tm.views.medications.warning.SimpleWarningsContainer({
      view: this.view,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "visible"
    });

    if (!this.isEditMode())
    {
      this.warningsContainer.show();
      this.addToBasketButton = new tm.jquery.Button({
        cls: 'add-to-basket-button',
        text: this.view.getDictionary("add"),
        handler: function()
        {
          self.addToBasketButton.setEnabled(false);
          self.validateAndConfirmOrder();
        }
      });

      this.templatesButton = new tm.jquery.Button({
        cls: "templates-button",
        text: this.view.getDictionary('empty.form'),
        type: "link",
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent('TEMPLATES');
          }
        }
      });
      this.complexTherapyButton = new tm.jquery.Button({
        cls: "complex-therapy-button",
        text: this.view.getDictionary('expanded1'),
        type: "link",
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent(self.medicationData, self.warningsContainer.list.getListData());
          }
        }
      });
      this.saveToTemplateButton = new tm.jquery.Button({
        cls: "save-to-template-button",
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
        }
      });
    }
    else
    {
      this.warningsContainer.hide();
    }

    this._extensionsPlaceholder = new tm.jquery.Container({
      cls: "extensions-container",
      scrollable: "visible",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

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

    this._toggleTitrationButton = new tm.jquery.ToggleButton({
      cls: 'toggle-titration-button',
      iconCls: 'icon-titration-dosage-24',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      alignSelf: "center",
      tooltip: appFactory.createDefaultHintTooltip(this.view.getDictionary("dose.titration"), "bottom"),
      enabled: !this.isEditMode() || this.isCopyMode(),
      handler: function(component)
      {
        component.isPressed() ? self._markAsTitrationDosing() : self._unmarkAsTitrationDosing();
      }
    });
    this._setTitrationButtonVisibility();
  },

  _handleBnfWarningChange: function(bnfPercentage)
  {
    var medication = this.medicationData.getMedication();
    !tm.jquery.Utils.isEmpty(bnfPercentage) && bnfPercentage >= 100 ?
        this.warningsContainer.addBnfWarning(bnfPercentage, medication) : this.removeBnfWarning();
  },
  _handleParacetamolWarningsChange: function(calculatedParacetamolRule) // MedicationIngredientRuleDto.java
  {
    if (!tm.jquery.Utils.isEmpty(calculatedParacetamolRule))
    {
      var enums = app.views.medications.TherapyEnums;
      if (calculatedParacetamolRule.quantityOk === false && tm.jquery.Utils.isEmpty(calculatedParacetamolRule.errorMessage))
      {
        this.warningsContainer.addParacetamolLimitExceededWarning(calculatedParacetamolRule)
      }
      else
      {
        this.warningsContainer.removeAdditionalWarning(enums.additionalWarningType.PARACETAMOL);
      }
    }
  },

  _buildGui: function()
  {
    var mainContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: 'visible',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      margin: '10 0 0 20'
    });
    this.add(mainContainer);
    var medicationContainer = new tm.jquery.Container({
      scrollable: 'visible',
      cls: 'medication-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start")
    });
    medicationContainer.add(this.medicationField);
    medicationContainer.add(this.highRiskIconsContainer);
    medicationContainer.add(this.medicationInfo);
    mainContainer.add(medicationContainer);
    mainContainer.add(this._createVerticalSpacer(7));

    var doseRowContainer = new tm.jquery.Container({
      cls: 'dose-row-container',
      layout: new tm.jquery.HFlexboxLayout({align: 'right'}),
      width: 678,
      scrollable: "visible"
    });
    if (!this.isEditMode())
    {
      doseRowContainer.add(this.removeButton);
    }
    doseRowContainer.add(new tm.jquery.Container({flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")}));
    if (!(this.visibilityContext &&
        this.visibilityContext.hasOwnProperty("variableButtonVisible") &&
        this.visibilityContext.variableButtonVisible === false))
    {
      doseRowContainer.add(this.varioButton);
    }
    doseRowContainer.add(this.overdosePane);
    doseRowContainer.add(this.paracetamolLimitContainer);
    doseRowContainer.add(this.bnfMaximumPane);
    doseRowContainer.add(this.dosePane);
    doseRowContainer.add(this.variableDoseContainer);
    doseRowContainer.add(this.descriptiveDoseField);
    doseRowContainer.add(this._toggleTitrationButton);

    mainContainer.add(doseRowContainer);
    mainContainer.add(this._createVerticalSpacer(2));
    mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('route')));
    mainContainer.add(this.routesPane);
    mainContainer.add(this._createVerticalSpacer(2));
    this.dosingFrequencyTitle = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval'));
    mainContainer.add(this.dosingFrequencyTitle);
    mainContainer.add(this.dosingFrequencyPane);
    mainContainer.add(this._createVerticalSpacer(2));
    mainContainer.add(this.therapyIntervalPane);
    if (this.therapySupplyContainer != null)
    {
      mainContainer.add(this.therapySupplyContainer);
    }
    mainContainer.add(this.therapyNextAdministrationLabelPane);
    if (this.withStartEndTime === true)
    {
      mainContainer.add(this.administrationPreviewTimeline);
    }
    mainContainer.add(this.pastDaysOfTherapySpacer);
    mainContainer.add(this.pastDaysOfTherapyLabel);
    mainContainer.add(this.pastDaysOfTherapyField);
    mainContainer.add(this._createVerticalSpacer(2));
    mainContainer.add(this.commentIndicationPane);
    mainContainer.add(this.changeReasonPane);

    if (this.showSource)
    {
      mainContainer.add(this.sourceLabel);
      mainContainer.add(this.sourceField);
    }

    this.add(this._extensionsPlaceholder);
    this.add(new tm.views.medications.common.VerticallyTitledComponent({
      margin: "0 20 5 20",
      titleText: this.view.getDictionary('calculated.dosing'),
      contentComponent: this.calculatedDosagePane,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    }));

    if (!this.isEditMode())
    {
      this.add(new tm.jquery.Container({style: 'border-top: 1px solid #d6d6d6'}));
      this.add(this._createVerticalSpacer(7));

      var navigationContainer = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 20),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        margin: '0 25 5 20'
      });

      navigationContainer.add(this.templatesButton);
      navigationContainer.add(new tm.jquery.Spacer({type: 'horizontal', size: 50}));
      navigationContainer.add(this.complexTherapyButton);
      navigationContainer.add(this.saveToTemplateButton);
      var addToBasketContainer = new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
        layout: tm.jquery.HFlexboxLayout.create("flex-end", "flex-start")
      });
      addToBasketContainer.add(this.addToBasketButton);
      navigationContainer.add(addToBasketContainer);
      this.add(navigationContainer);
    }

    this.add(this.warningsContainer);


    this._configureChangeReasonEditPane();
    this._rebuildExtensions();
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();
    this._addValidations(this.routesPane.getRoutesPaneValidations());
    var therapyStartNotBefore = this.getTherapyStartNotBeforeDateFunction ? this.getTherapyStartNotBeforeDateFunction() : null;
    if (this.withStartEndTime === true)
    {
      this._addValidations(this.therapyIntervalPane.getTherapyIntervalPaneValidations(therapyStartNotBefore));
    }
    if (this.therapySupplyContainer != null)
    {
      this._addValidations(this.therapySupplyContainer.getFormValidations());
    }
    this._addValidations(this.commentIndicationPane.getIndicationValidations());
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
    if (this.isChangeReasonRequired()) this._addValidations(this.changeReasonPane.getChangeReasonValidations());

    if ((this.therapySupplyContainer == null) &&
        (this.therapyIntervalPane.getWhenNeeded()))
    {
      this._addValidations(this.commentIndicationPane.getIndicationFieldValidation());
    }

    this._executeOnValidContentExtensions(function(extension)
    {
      self._addValidations(extension.getFormValidations());
    })
  },

  _addValidations: function(validation)
  {
    if (tm.jquery.Utils.isEmpty(validation)) return;

    for (var i = 0; i < validation.length; i++)
    {
      this.validationForm.addFormField(validation[i]);
    }
  },

  _configureChangeReasonEditPane: function()
  {
    var pane = this.changeReasonPane;
    var view = this.view;
    var editEnum = app.views.medications.TherapyEnums.pharmacistTherapyChangeType.EDIT;

    if (pane && this.isChangeReasonRequired())
    {
      if (tm.jquery.Utils.isEmpty(view.getTherapyChangeReasonTypeMap()))
      {
        view.loadTherapyChangeReasonTypeMap(function(data)
        {
          pane.setReasonOptions(data[editEnum]);
        });
      }
      else
      {
        pane.setReasonOptions(view.getTherapyChangeReasonTypeMap()[editEnum]);
      }
      pane.show();
    }
    else
    {
      pane.hide();
    }
  },

  _openVariableDoseEditPane: function(variableDays)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var variableDoseEditDialog;
    var variableDosePane;
    var width;
    var height;

    var medicationData = jQuery.extend({}, this.medicationData);
    if (medicationData.getMedication().isMedicationUniversal())
    {
      medicationData.setMedicationIngredients([
        {
          strengthNumeratorUnit: self.dosePane.getNumeratorUnit(),
          strengthDenominatorUnit: self.dosePane.getDenominatorUnit()
        }
      ]);
    }
    if (variableDays)
    {
      var timedDoseElementsForVariableDays = this.timedDoseElements;
      if (this.timedDoseElements != null && this.timedDoseElements.length > 0 && this.timedDoseElements[0].date != null)
      {
        timedDoseElementsForVariableDays = this.timedDoseElements;
      }
      else
      {
        timedDoseElementsForVariableDays = null;
      }
      var endDate = this.protocolEndDate;
      if (!tm.jquery.Utils.isEmpty(this.protocolEndDate) && this.therapyIntervalPane.getEnd() > this.protocolEndDate)
      {
        endDate = this.therapyIntervalPane.getEnd();
      }
      variableDosePane = new app.views.medications.ordering.SimpleVariableDoseDaysPane({
        view: self.view,
        visibilityContext: this.visibilityContext,
        startProcessOnEnter: true,
        padding: 10,
        height: 180,
        medicationData: medicationData,
        timedDoseElements: timedDoseElementsForVariableDays,
        frequency: this.dosingFrequencyPane.getFrequency(),
        editMode: this.isEditMode(),
        untilCanceled: this.repeatProtocolUntilCanceled,
        selectedDate: endDate
      });
      height = 850;
      width = 950;
    }
    else
    {
      variableDosePane = new app.views.medications.ordering.SimpleVariableDosePane({
        view: self.view,
        visibilityContext: this.visibilityContext,
        startProcessOnEnter: true,
        padding: 10,
        height: 180,
        medicationData: medicationData,
        timedDoseElements: this.timedDoseElements,
        frequency: this.dosingFrequencyPane.getFrequency(),
        addDosageCalculationPane: self.dosePane.isDosageCalculationPossible()
      });
      height = 500;
      width = 650;
    }

    variableDoseEditDialog = appFactory.createDataEntryDialog(
        this.view.getDictionary('variable.dose'),
        null,
        variableDosePane,
        function(resultData)
        {
          if (resultData)
          {
            self.dosePane.hide();
            self.dosePane.clear(true);
            self.timedDoseElements = resultData.value.timedDoseElements;
            self._showVariableDoseDisplayValue();
            self.dosingFrequencyPane.setFrequency(resultData.value.frequency, false);
            self._handleDosingFrequencyChange();
            self._adjustDosingFrequencyPaneFields();
            self._adjustTherapyIntervalToVariableDose(resultData.value.endDate, resultData.value.untilCanceled);
          }
        },
        width,
        height
    );
    variableDoseEditDialog.addTestAttribute(variableDays ? 'variable-days-edit-dialog' : 'variable-dose-edit-dialog');
    var removeVarioButton = variableDoseEditDialog.getBody().footer.cancelButton;
    removeVarioButton.setText(this.view.getDictionary('remove.vario'));
    removeVarioButton.setType("link");
    var cancelButtonHandler = removeVarioButton.getHandler();
    removeVarioButton.setHandler(function()
    {
      self._removeVariableDosage();
      cancelButtonHandler();
    });

    if (variableDays)
    {
      variableDoseEditDialog.getBody().footer.setLeftButtons([removeVarioButton]);
    }
    else
    {
      var switchToVariableDaysButton = new tm.jquery.Button({
        testAttribute: 'switch-to-protocol-button',
        text: self.view.getDictionary('protocol'),
        type: "link",
        handler: function()
        {
          variableDoseEditDialog.hide();
          self._openVariableDoseEditPane(true);
        }
      });
      variableDoseEditDialog.getBody().footer.setLeftButtons([removeVarioButton, switchToVariableDaysButton]);
    }
    variableDoseEditDialog.getBody().footer.setRightButtons([variableDoseEditDialog.getBody().footer.confirmButton]);
    variableDoseEditDialog.show();
  },

  _createVerticalSpacer: function(size)
  {
    return new tm.jquery.Spacer({type: 'vertical', size: size});
  },

  _adjustDosingFrequencyPaneFields: function()
  {
    if (this.timedDoseElements.length > 0)
    {
      this.dosingFrequencyPane.showDaysOnly(true);
    }
    else
    {
      this.dosingFrequencyPane.showAllFields();
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

  _removeVariableDosage: function()
  {
    this.dosePane.show();
    this.variableDoseContainer.hide();
    this.timedDoseElements.removeAll();
    this.dosingFrequencyPane.setFrequency(null, true);
    this._adjustDosingFrequencyPaneFields();
    this.dosePane.requestFocusToNumerator();
    this._handleDosingFrequencyChange();
    this.repeatProtocolUntilCanceled = false;
    this.protocolEndDate = null;
    this.therapyIntervalPane.setMinEnd(null);
    this.varioButton.show();
  },

  _showDaysVariableDoseDisplayValue: function()
  {
    var self = this;
    this.variableDoseContainer.add(new tm.jquery.Button({
      text: "VARIABLE DOSE",
      type: "link",
      handler: function()
      {
        self._openVariableDoseEditPane(true);
      }
    }));
    this.varioButton.hide();
  },

  _showHoursVariableDoseDisplayValue: function()
  {
    var self = this;
    var utils = tm.views.medications.MedicationUtils;
    for (var n = 0; n < this.timedDoseElements.length; n++)
    {
      var rowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 10)});
      var timedDoseElement = this.timedDoseElements[n];
      var doseTime = timedDoseElement.doseTime;
      var timeDisplayValue = tm.views.medications.MedicationTimingUtils.hourMinuteToString(doseTime.hour, doseTime.minute) + '  ';
      rowContainer.add(utils.crateLabel('TextLabel', timeDisplayValue, '1 0 0 0'));
      var definingIngredient = self.medicationData.getDefiningIngredient();
      var numeratorUnit = null;
      if (definingIngredient)
      {
        numeratorUnit = utils.getFormattedUnit(definingIngredient.strengthNumeratorUnit);
      }
      else if (!tm.jquery.Utils.isEmpty(this.medicationData.basicUnit))
      {
        numeratorUnit = utils.getFormattedUnit(this.medicationData.basicUnit);
      }
      else
      {
        numeratorUnit = this.dosePane.getNumeratorUnit();
      }
      var doseDisplayValue = utils.getFormattedDecimalNumber(utils.doubleToString(timedDoseElement.doseElement.quantity, 'n2')) + ' ' + numeratorUnit;
      if (definingIngredient && timedDoseElement.doseElement.quantityDenominator)
      {
        doseDisplayValue += ' / ' +
            utils.getFormattedDecimalNumber(utils.doubleToString(timedDoseElement.doseElement.quantityDenominator, 'n2')) + ' ' +
            utils.getFormattedUnit(definingIngredient.strengthDenominatorUnit);
      }

      rowContainer.add(utils.crateLabel('TextData', doseDisplayValue, 0));
      this.variableDoseContainer.add(rowContainer);
    }
  },

  _adjustTherapyIntervalToVariableDose: function(endDate, untilCanceled)
  {
    if (tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(this.timedDoseElements))
    {
      var therapyInterval = tm.views.medications.MedicationTimingUtils.getVariableDaysTherapyInterval(this.timedDoseElements);
      if (!this.isEditMode())
      {
        this.therapyIntervalPane.setStart(therapyInterval.start);
      }
      var therapyEnd = null;
      if (endDate)
      {
        therapyEnd = endDate;
        therapyEnd.setHours(therapyInterval.end.getHours());
        therapyEnd.setMinutes(therapyInterval.end.getMinutes());
        this.repeatProtocolUntilCanceled = false;
        this.protocolEndDate = endDate;

      }
      else if (untilCanceled)
      {
        this.repeatProtocolUntilCanceled = true;
        this.protocolEndDate = null;
        therapyEnd = null;
      }
      else
      {
        therapyEnd = therapyInterval.end;
        this.repeatProtocolUntilCanceled = false;
      }
      this.therapyIntervalPane.setEnd(therapyEnd);
      this.therapyIntervalPane.setMinEnd(therapyEnd)
    }
    else
    {
      var nextAdministrationTimestamp =
          tm.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForVario(this.timedDoseElements);
      this.therapyIntervalPane.setStart(nextAdministrationTimestamp);
    }
  },

  _handleParacetamolRuleChange: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var view = this.view;
    var self = this;

    var medicationData = this.medicationData;
    if (self.medicationRuleUtils.isMedicationRuleSet(medicationData, enums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE))
    {
      var medicationId = !tm.jquery.Utils.isEmpty(medicationData)
          ? medicationData.medication.id
          : null;

      self.medicationRuleUtils.getParacetamolRuleForTherapy(
          view,
          this._buildTherapy(),
          [medicationData],
          view.getPatientData(),
          view.getReferenceWeight()).then(
          function validationSuccessHandler(medicationRuleResult)
          {
            var medicationConsistent = self.medicationData && self.medicationData.medication && medicationId
                && medicationId == self.medicationData.medication.id;

            if (self.isRendered() && medicationConsistent)
            {
              self.paracetamolLimitContainer.setCalculatedParacetamolLimit(medicationRuleResult);
              if (self.paracetamolLimitContainer.hasContent())
              {
                self.paracetamolLimitContainer.show();
              }
              self._handleParacetamolWarningsChange(medicationRuleResult);
            }
          });
    }
    else
    {
      this.paracetamolLimitContainer.setCalculatedParacetamolLimit(null);
      this.paracetamolLimitContainer.hideAll();
    }
  },

  _calculateDosing: function()
  {
    if (this.medicationData)
    {
      var view = this.view;

      var dose = this.dosePane.getDose();
      var quantity = dose.doseRange ? dose.doseRange : dose.quantity;
      var quantityUnit = this.medicationData.getStrengthNumeratorUnit();
      var timesPerDay = this.dosingFrequencyPane.getFrequencyTimesPerDay();
      var weightInKg = view.getReferenceWeight() || 0;
      var heightInCm = view.getPatientHeightInCm() ? view.getPatientHeightInCm() : 0;

      this.calculatedDosagePane.calculate(quantity, quantityUnit, timesPerDay, weightInKg, heightInCm, false);
      this.overdosePane.calculateOverdose(quantity);
      var variable = false;

      if (quantity == null)
      {
        variable = true;
        quantity = tm.views.medications.MedicationUtils.calculateVariablePerDay(this.timedDoseElements);
      }

      if (this.isShowBnf())
      {
        var bnfPercentage = this.bnfMaximumPane.calculatePercentage(quantity, timesPerDay, this.dosingFrequencyPane.getTimesPerWeek(), variable);
        this._handleBnfWarningChange(bnfPercentage);
      }

      this._handleParacetamolRuleChange();
    }
  },

  _confirm: function()
  {
    var therapy = this._buildTherapy();
    var changeReason = this.isChangeReasonRequired() ? new app.views.medications.common.dto.TherapyChangeReason({
      changeReason: this.changeReasonPane.getReasonValue(),
      comment: this.changeReasonPane.getComment()
    }) : null;

    var confirmSuccess = this.confirmTherapyEvent(therapy, changeReason);
    if (confirmSuccess == false)
    {
      this.addToBasketButton.setEnabled(true);
    }
    if (this.view.getViewMode() == 'ORDERING_PAST')
    {
      this.view.setPresetDate(therapy.start);
    }
  },

  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var variableDose = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var selectedRoutes = this.routesPane.getSelectedRoutes();
    var quantityUnits = this._getStrengthUnits();

    var therapy = {                                                              // [SimpleTherapyDto.java]
      medicationOrderFormType: enums.medicationOrderFormType.SIMPLE,
      variable: variableDose,
      medication: this.medicationData.medication,
      doseForm: this.medicationData.doseForm,
      routes: selectedRoutes,
      quantityUnit: quantityUnits.quantityUnit,
      quantityDenominatorUnit: quantityUnits.quantityDenominatorUnit,
      dosingFrequency: this.dosingFrequencyPane.getFrequency(),
      dosingDaysFrequency: this.dosingFrequencyPane.getDaysFrequency(),
      daysOfWeek: this.dosingFrequencyPane.getDaysOfWeek(),
      maxDailyFrequency: !tm.jquery.Utils.isEmpty(maxDailyFrequency) ? maxDailyFrequency : null,
      start: this.therapyIntervalPane.getStart(),
      end: this.therapyIntervalPane.getEnd(),
      reviewReminderDays: this.therapyIntervalPane.getReviewReminderDays(),
      whenNeeded: this.therapySupplyContainer != null ?
          this.therapySupplyContainer.getWhenNeeded() : this.therapyIntervalPane.getWhenNeeded(),
      startCriterion: this.therapyIntervalPane.getStartCriterion(),
      applicationPrecondition: this.dosingFrequencyPane.getApplicationPrecondition(),
      comment: this.commentIndicationPane.getComment(),
      clinicalIndication: this.commentIndicationPane.getIndication(),
      prescriptionSupply: this.therapySupplyContainer != null ? this.therapySupplyContainer.getSupply() : null,
      criticalWarnings: this._overridenCriticalWarnings ? this._overridenCriticalWarnings : [],
      bnfMaximumPercentage: selectedRoutes.length === 1 && !tm.jquery.Utils.isEmpty(selectedRoutes[0].bnfMaximumDto)
          ? this.bnfMaximumPane.getPercentage()
          : null,
      linkedToAdmission: !tm.jquery.Utils.isEmpty(this.therapyToEdit) ? this.therapyToEdit.linkedToAdmission : false,
      titration: this._toggleTitrationButton.isPressed() ? this.medicationData.getTitration() : null
    };

    var value = this.pastDaysOfTherapyField.getValue();
    therapy.pastDaysOfTherapy = (value && (!tm.jquery.Utils.isNumeric(value)) || value <= 0) ? null : value;

    if (variableDose)          // [VariableSimpleTherapyDto.java]
    {
      therapy.timedDoseElements = this.timedDoseElements;
    }
    else                    // [ConstantSimpleTherapyDto.java]
    {
      if (!this.dosePane.isHidden())
      {
        therapy.doseElement = this.dosePane.getDose();
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

    this._executeOnValidContentExtensions(function(extension)
    {
      extension.buildTherapy(therapy);
    });

    return app.views.medications.common.TherapyJsonConverter.convert(therapy);
  },

  _getStrengthUnits: function()
  {
    if (this.medicationData.getMedication().isMedicationUniversal())
    {
      return {
        quantityUnit: this.dosePane.getNumeratorUnit(),
        quantityDenominatorUnit: this.dosePane.getDenominatorUnit()
      }
    }
    return {
      quantityUnit: this.medicationData.getStrengthNumeratorUnit(),
      quantityDenominatorUnit: this.medicationData.getStrengthDenominatorUnit()
    }
  },

  _handleDosingFrequencyChange: function(preventDoseRecalculation)
  {
    this._calculateStartAndEnd(this._buildTherapy(), preventDoseRecalculation);
  },

  _calculateStartAndEnd: function(therapy, preventDoseRecalculation)
  {
    var self = this;
    // only
    var oldTherapy = this.isEditMode() && !this.isCopyMode() ? this.therapyToEdit : null;
    this.therapyIntervalPane.calculateStart(therapy, tm.jquery.Utils.isEmpty(oldTherapy), oldTherapy, function()
    {
      self.therapyIntervalPane.calculateEnd();
    });
    if (!preventDoseRecalculation)
    {
      this._calculateDosing();
    }
  },

  /**
   * @param {boolean} show Show or hide the dose components.
   * @private
   */
  _showDoseComponents: function(show)
  {
    var isDescriptiveDose = this.medicationData && this.medicationData.isDoseFormDescriptive();
    var isVariableDose = this.timedDoseElements.length > 0;

    if (show)
    {
      if (isDescriptiveDose)
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
    }
    else
    {
      if (isVariableDose)
      {
        this._removeVariableDosage();
      }

      this.dosePane.hide();
      this.varioButton.hide();
      this.descriptiveDoseField.hide();
    }
  },

  _clear: function()
  {
    this.timedDoseElements = [];
    this.routesPane.setSelectedRoute(null);
    this.dosePane.clear();
    this.bnfMaximumPane.clear();
    this.setBnfPaneShowed(false);
    this.variableDoseContainer.hide();
    this.dosingFrequencyPane.clear();
    this.therapyIntervalPane.clear();
    if (this.therapySupplyContainer != null) this.therapySupplyContainer.clear();
    this.pastDaysOfTherapyField.setValue(null);
    this.commentIndicationPane.clear();
    this._adjustDosingFrequencyPaneFields();
    this._hidePastDaysOfTherapy();
    if (!tm.jquery.Utils.isEmpty(this.sourceField)) this.sourceField.setSelections([]);
    this._executeOnValidContentExtensions(function(extension)
    {
      extension.clear();
    });
    this.warningsContainer.clear();
    this.changeReasonPane.clear();
    this._toggleTitrationButton.setPressed(false);
    this.highRiskIconsContainer.clear();
    this.repeatProtocolUntilCanceled = false;
    this.protocolEndDate = null;
    this.therapyIntervalPane.setMinEnd(null);
    this.paracetamolLimitContainer.clear();
    this.routesPane.clear();
    this.calculatedDosagePane.clear();
  },

  _hidePastDaysOfTherapy: function()
  {
    this.pastDaysOfTherapySpacer.hide();
    this.pastDaysOfTherapyLabel.hide();
    this.pastDaysOfTherapyField.hide();
  },

  _showPastDaysOfTherapy: function()
  {
    this.pastDaysOfTherapySpacer.show();
    this.pastDaysOfTherapyLabel.show();
    this.pastDaysOfTherapyField.show();
  },

  _rebuildExtensions: function()
  {
    this._extensionsPlaceholder.removeAll();
    this.contentExtensions.forEach(function(extension)
    {
      this._extensionsPlaceholder.add(extension);
    }, this);

    if (this._extensionsPlaceholder.isRendered()) this._extensionsPlaceholder.repaint();
  },

  _executeOnValidContentExtensions: function(callback)
  {
    if (tm.jquery.Utils.isEmpty(callback)) return;

    var contentExtensions = tm.jquery.Utils.isEmpty(this.contentExtensions) ? [] : this.contentExtensions;

    contentExtensions.forEach(function(extension)
    {
      if (extension instanceof app.views.medications.ordering.PrescriptionContentExtensionContainer)
      {
        callback(extension);
      }
    }, this);
  },

  _showMedicationInfoPopup: function()
  {
    var appFactory = this.getView().getAppFactory();
    var medicationInfoContent = new app.views.medications.common.MedicationDetailsContainer({
      view: this.getView(),
      medicationData: [this.medicationData],
      selectedRoute: this.routesPane.getSelectedRoutes()
    });

    var medicationInfoPopup = appFactory.createDefaultPopoverTooltip(
        this.getView().getDictionary("medication"),
        null,
        medicationInfoContent
    );

    this.medicationInfo.setTooltip(medicationInfoPopup);

    setTimeout(function()
    {
      medicationInfoPopup.show();
    }, 10);
  },

  _setTitrationButtonVisibility: function()
  {
    var isVisible = this.medicationData && !tm.jquery.Utils.isEmpty(this.medicationData.getTitration()) &&
        !(this.visibilityContext &&
            this.visibilityContext.hasOwnProperty("titratedDoseModeVisible") &&
            this.visibilityContext.titratedDoseModeVisible === false);

    if (isVisible)
    {
      this._toggleTitrationButton.setPressed(false, true);
      this.isRendered() ? this._toggleTitrationButton.show() : this._toggleTitrationButton.setHidden(false);
    }
    else
    {
      this.isRendered() ? this._toggleTitrationButton.hide() : this._toggleTitrationButton.setHidden(true);
    }
  },

  _markAsTitrationDosing: function()
  {
    this._showDoseComponents(false);
    this.therapyIntervalPane.setByDoctorsOrderButtonEnabled(false);
  },

  _unmarkAsTitrationDosing: function()
  {
    this._showDoseComponents(true);
    this.therapyIntervalPane.setByDoctorsOrderButtonEnabled(true);
  },


  /** public methods */
  setMedicationData: function(medicationData, clear, warnings, warningsLoaded)
  {
    var self = this;
    this.medicationData = medicationData;

    if (clear)
    {
      this._clear();
    }
    this.medicationField.setSelection(medicationData.getMedication(), true);
    var routes = !tm.jquery.Utils.isEmpty(medicationData.getRoutes()) ? medicationData.getRoutes() : this.view.getRoutes();
    this.routesPane.setRoutes(routes, medicationData.defaultRoute);
    this.dosePane.setMedicationData(medicationData);
    this.commentIndicationPane.setMedicationData(medicationData);
    this.therapyIntervalPane.setMedicationData(medicationData);
    this.validationForm.reset();
    this.descriptiveDoseField.setValue(null);
    this.overdosePane.setMedicationDataValues(medicationData);
    if (this.overdosePane.isTabletOrCapsule())
    {
      this.overdosePane.show();
    }
    else
    {
      this.overdosePane.hide();
    }
    //this.therapySupplyContainer.medicationId = medicationData.medication.id;
    //TODO nejc - remove line above if Control drugs Supply container will not be implemented
    if (this.addToBasketButton)
    {
      this.addToBasketButton.setEnabled(true);
    }
    if (!medicationData.getMedication().isMedicationUniversal())
    {
      if (this.saveToTemplateButton)
      {
        this.saveToTemplateButton.show();
      }
      this.medicationInfo.show();
      this.medicationInfo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
      {
        self._showMedicationInfoPopup();
      });
    }
    else
    {
      if (this.saveToTemplateButton)
      {
        this.saveToTemplateButton.hide();
      }
      this.medicationInfo.hide();
    }

    this._showDoseComponents(true);

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

    if (medicationData.antibiotic &&
        !(this.visibilityContext &&
          this.visibilityContext.hasOwnProperty("pastDaysOfTherapyVisible") &&
          this.visibilityContext.pastDaysOfTherapyVisible === false))
    {
      if (!this.isEditMode() || this.isCopyMode())
      {
        this._showPastDaysOfTherapy();
      }
    }
    if ((tm.jquery.Utils.isEmpty(warnings) || warnings.length == 0) && warningsLoaded === false)
    {
      this.setWarningsMessage(true, this.view.getDictionary('loading.warnings'))
    }
    else if (!tm.jquery.Utils.isEmpty(warnings))
    {
      this.setWarnings(warnings);
    }

    this.bnfMaximumPane.setPercentage(null);
    var selectedRoutes = this.routesPane.getSelectedRoutes();
    if (selectedRoutes.length === 1 && !tm.jquery.Utils.isEmpty(selectedRoutes[0].bnfMaximumDto))
    {
      this.setBnfPaneShowed(true);
      this.bnfMaximumPane.setBnfValuesAndNumeratorUnit(medicationData, selectedRoutes[0]);
    }

    this.showHideParacetamolContainer(
        this.medicationRuleUtils.isMedicationRuleSet(
            medicationData, app.views.medications.TherapyEnums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE));

    this.highRiskIconsContainer.presentHighAlertIcons(medicationData);

    this._setTitrationButtonVisibility();

    this._executeOnValidContentExtensions(function(extension)
    {
      extension.setMedicationData(medicationData);
    });
  },

  validateAndConfirmOrder: function()
  {
    this._setupValidation();
    this.validationForm.submit();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {boolean} setTherapyStart
   * @param {boolean} therapyModifiedInThePast
   */
  setSimpleTherapyData: function(therapy, medicationData, setTherapyStart, therapyModifiedInThePast)
  {
    var self = this;
    this.therapyToEdit = therapy;
    if (medicationData)
    {
      self.setMedicationData(medicationData, true);
      if (therapy.isVariable())
      {
        self.timedDoseElements = therapy.getTimedDoseElements();
        self.dosePane.hide();
        self._showVariableDoseDisplayValue();
        self._adjustDosingFrequencyPaneFields();
      }
      else if (medicationData.doseForm && medicationData.doseForm.medicationOrderFormType == 'DESCRIPTIVE')
      {
        self.descriptiveDoseField.setValue(therapy.getDoseElement().doseDescription);
      }
      else
      {
        var numerator = therapy.getDoseElement().doseRange ?
            app.views.medications.common.dto.Range.createStrict(
                therapy.getDoseElement().doseRange.minNumerator,
                therapy.getDoseElement().doseRange.maxNumerator) :
            therapy.getDoseElement().quantity;
        self.dosePane.setDoseNumerator(numerator, true);
        if (therapy.getMedication().isMedicationUniversal())
        {
          var denominator = therapy.getDoseElement().doseRange ?
              app.views.medications.common.dto.Range.createStrict(
                  therapy.getDoseElement().doseRange.minDenominator,
                  therapy.getDoseElement().doseRange.maxDenominator) :
              therapy.getDoseElement().quantityDenominator;
          self.dosePane.setDoseDenominator(denominator, true);
        }
        else
        {
          self.dosePane.calculateAndSetDoseDenominator(true);
        }
      }

      if (therapy.isTitrationDoseType())
      {
        self._toggleTitrationButton.setPressed(true, true);
        self._markAsTitrationDosing();
      }

      self.routesPane.setSelectedRoute(therapy.getRoutes());
    }
    self.dosingFrequencyPane.setDosingFrequencyAndPattern(therapy.getDosingFrequency(), therapy.getDoseTimes(), true);
    self.dosingFrequencyPane.setDaysOfWeek(therapy.getDaysOfWeek());
    self.dosingFrequencyPane.setDaysFrequency(therapy.getDosingDaysFrequency());
    if (self.therapySupplyContainer != null)
    {
      self.therapySupplyContainer.setWhenNeeded(therapy.getWhenNeeded());
    }
    self.therapyIntervalPane.setWhenNeeded(therapy.getWhenNeeded());
    self.therapyIntervalPane.setStartCriterion(therapy.getStartCriterion());
    self.therapyIntervalPane.setReviewReminderDays(therapy.getReviewReminderDays());
    self.dosingFrequencyPane.setApplicationPrecondition(therapy.getApplicationPrecondition());
    self.therapyIntervalPane.setMaxDailyFrequency(therapy.getMaxDailyFrequency());
    self.commentIndicationPane.setComment(therapy.getComment());
    self.commentIndicationPane.setIndication(therapy.getClinicalIndication());

    if (self.therapySupplyContainer != null)
    {
      self.therapySupplyContainer.setSupply(therapy.prescriptionSupply);
    }
    if (therapy.getBnfMaximumPercentage())
    {
      self.bnfMaximumPane.setPercentage(therapy.getBnfMaximumPercentage());
      if (therapy.getBnfMaximumPercentage() >= 100)
      {
        self.warningsContainer.addBnfWarning(therapy.getBnfMaximumPercentage(), therapy.getMedication());
      }
    }
    if (!self.isCopyMode())
    {
      self.pastDaysOfTherapyField.setValue(therapy.getPastDaysOfTherapy());
    }

    // Set the start if declared (usually meaning the therapy hasn't started yet) and we're not copying,
    // otherwise calculate a new start and possibly end (which should always happen when copying a therapy).
    if (setTherapyStart && !self.isCopyMode())
    {
      self.therapyIntervalPane.setStartOptionsFromPattern();
      self.therapyIntervalPane.setStart(
          tm.jquery.Utils.isDate(therapy.getStart()) ? new Date(therapy.getStart().getTime()) : null);
      self.therapyIntervalPane.setEnd(
          tm.jquery.Utils.isDate(therapy.getEnd()) ? new Date(therapy.getEnd().getTime()) : null);
    }
    else
    {
      // Set the end regardless (unless we're copying), since it might get recalculated (1ex) or cleared
      // (no end date support). If it's not set, the therapyIntervalPane's calculateEnd,
      // called by _calculateStartAndEnd, will clear it, which is not what we want when editing a prescribed therapy.
      if (!self.isCopyMode())
      {
        self.therapyIntervalPane.setEnd(
            tm.jquery.Utils.isDate(therapy.getEnd()) ? new Date(therapy.getEnd().getTime()) : null);
      }
      self._calculateStartAndEnd(self._buildTherapy(), true);
    }

    if (self.isEditMode())
    {
      if (!therapy.getMedication().isMedicationUniversal())
      {
        self.medicationField.setLimitBySimilar(therapy.getMedication(), therapy.getRoutes());
      }

      if (!self.isCopyMode() && (self.therapyAlreadyStarted || therapyModifiedInThePast))
      {
        self.therapyNextAdministrationLabelPane.setOldTherapyId(
            therapy.getCompositionUid(), therapy.getEhrOrderName(), false);
      }
    }
    self._overridenCriticalWarnings = therapy.getCriticalWarnings();
    self._calculateDosing();

    self._executeOnValidContentExtensions(function(extension)
    {
      if (extension.setComponentsFromTherapy && tm.jquery.Utils.isFunction(extension.setComponentsFromTherapy))
      {
        extension.setComponentsFromTherapy(therapy);
      }
    });
  },

  refreshAdministrationPreview: function()
  {
    var self = this;
    /* add a small delay so we don't call it too often*/
    clearTimeout(this._previewRefreshTimer);

    this._previewRefreshTimer = setTimeout(function()
    {
      if (self.isRendered())
      {
        self.administrationPreviewTimeline._refreshAdministrationPreviewImpl(self.therapyIntervalPane.getStart(), self._buildTherapy());
      }
    }, 150);
  },

  getSimpleTherapy: function()
  {
    return this._buildTherapy();
  },

  setChangeReasonRequired: function(value)
  {
    this.changeReasonRequired = value;
    this._configureChangeReasonEditPane();
  },

  setBnfPaneShowed: function(value)
  {
    this.showBnf = value;
    value === true ? this.bnfMaximumPane.show() : this.bnfMaximumPane.hide();
  },

  showHideParacetamolContainer: function(show)
  {
    this.showparacetamolContainer = show;
    if (this.paracetamolLimitContainer.isRendered())
    {
      show === true ? this.paracetamolLimitContainer.show() : this.paracetamolLimitContainer.hide();
    }
    else
    {
      this.paracetamolLimitContainer.setHidden(show === false);
    }
  },

  isChangeReasonRequired: function()
  {
    return this.changeReasonRequired === true;
  },

  setWarnings: function(warnings)
  {
    this.warningsContainer.setWarnings(warnings);
  },

  setWarningsMessage: function(loading, text)
  {
    this.warningsContainer.setWarningsMessage(loading, text);
  },

  setContentExtensions: function(extensions)
  {
    if (!tm.jquery.Utils.isEmpty(extensions))
    {
      extensions = tm.jquery.Utils.isArray(extensions) ? extensions : [extensions];
      this.contentExtensions = extensions;

      this._rebuildExtensions();
    }
  },
  /**
   * @param {Boolean} *removeAllBnfWarnings
   */
  removeBnfWarning: function(removeAllBnfWarnings)
  {
    this.warningsContainer.removeBnfWarning(this.medicationData.getMedication().getId(), removeAllBnfWarnings);
  },

  /**
   * @returns {boolean}
   */
  isEditMode: function ()
  {
    return this.editMode === true;
  },

  /**
   * @returns {boolean}
   */
  _isBnfMaximumDefined: function()
  {
    var selectedRoutes = this.routesPane.getSelectedRoutes();

    return tm.jquery.Utils.isArray(selectedRoutes) && selectedRoutes.length === 1 &&
        !tm.jquery.Utils.isEmpty(selectedRoutes[0].bnfMaximumDto);
  },

  /**
   * @returns {boolean}
   */
  isCopyMode: function ()
  {
    return this.copyMode === true;
  },

  /**
   * @returns {boolean}
   */
  isShowBnf: function()
  {
    return this.showBnf === true;
  },

  /**
   * Getters & setters
   */

  /**
   *
   * @returns {Number|null}
   */
  getBnfMaximumPercentage: function()
  {
    return this.bnfMaximumPercentage;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});

