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

Class.define('app.views.medications.ordering.ComplexTherapyContainer', 'tm.jquery.Container', {
  cls: "complex-therapy-container",
  scrollable: 'vertical',
  margin: '10 0 0 0',

  /** configs */
  view: null,
  editMode: false,
  copyMode: false,
  withStartEndTime: true,
  withSupply: false, /* if supply is true it will override withStartEndTime to false */
  changeCardEvent: null, //optional
  saveToTemplateFunction: null, //optional
  getTherapyStartNotBeforeDateFunction: null, //optional
  confirmTherapyEvent: null,
  saveDateTimePaneEvent: null,
  isPastMode: false,
  therapyAlreadyStarted: false,
  changeReasonRequired: false,
  getBasketTherapiesFunction: null, //optional
  refreshBasketFunction: null, //optional
  screenTherapyForWarningsFunction: null,
  showBnf: null,
  showSource: false,
  additionalMedicationSearchFilter: null,
  preventUnlicensedMedicationSelection: false,
  /** privates */
  therapyToEdit: null,
  validationForm: null,
  timedDoseElements: null,
  recurringContinuousInfusion: null,
  medicationData: null,
  medicationPanes: null,
  valueSettingInProgress: null,
  linkName: null,
  readyConditionTask: null,
  showHeparinPane: null,
  linkedTherapy: null, /* Therapy.js */
  medicationRuleUtils: null,
  /** privates: components */
  medicationPanesContainer: null,
  heparinPane: null,
  volumeSumTopSpacer: null,
  volumeSumPane: null,
  routesPane: null,
  infusionRatePane: null,
  infusionRateTypePane: null,
  rateLabelVSpacer: null,
  variableRateContainer: null,
  varioButton: null,
  dosingFrequencyLabel: null,
  dosingFrequencyPane: null,
  therapyIntervalPane: null,
  therapySupplyContainer: null,
  commentIndicationPane: null,
  changeReasonPane: null,
  calculatedDosagePane: null,
  warningsContainer: null,
  addToBasketButton: null,
  templatesButton: null,
  simpleTherapyButton: null,
  saveToTemplateButton: null,
  pastDaysOfTherapySpacer: null,
  pastDaysOfTherapyLabel: null,
  pastDaysOfTherapyField: null,
  therapyNextAdministrationLabelPane: null,
  linkIcon: null,
  linkTherapyButton: null,
  unlinkTherapyButton: null,
  administrationPreviewTimeline: null,

  contentExtensions: null,

  visibilityContext: null,

  _previewRefreshTimer: null,
  _overridenCriticalWarnings: null,
  _extensionsPlaceholder: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.timedDoseElements = [];
    this.recurringContinuousInfusion = false;
    this.medicationPanes = [];
    this.showHeparinPane = this.view.getMedicationsShowHeparinPane();

    if (this.visibilityContext &&
        this.visibilityContext.hasOwnProperty("medicationsShowHeparinPane") &&
        this.visibilityContext.medicationsShowHeparinPane === false)
    {
      this.showHeparinPane = false;
    }

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

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
    this._ensureChangeReasonOptionsLoaded();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();


    this.medicationPanesContainer = new tm.jquery.Container({
      cls: "medication-panes-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: 'visible'
    });

    if (this.showHeparinPane === true)
    {
      this.heparinPane = new app.views.medications.ordering.HeparinPane({view: view});
      this.heparinPane.hide();
    }
    else
    {
      this.volumeSumTopSpacer = this._createVerticalSpacer(23);
      this.volumeSumTopSpacer.hide();
    }

    this.volumeSumPane = new app.views.medications.ordering.VolumeSumPane({
      view: view,
      width: 504,
      margin: '0 10 0 0',
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      adjustVolumesEvent: function()
      {
        self._adjustVolumes();
      }
    });

    this.routesPane = new app.views.medications.ordering.RoutesPane({
      view: view,
      height: 30,
      discretionaryRoutesDisabled: false,
      changeEvent: function(selectedRoutes)
      {
        if (tm.jquery.Utils.isArray(selectedRoutes) && selectedRoutes.length === 1
            && !tm.jquery.Utils.isEmpty(selectedRoutes[0].bnfMaximumDto))
        {
          for (var j = 0; j < self.medicationPanes.length; j++)
          {
            var medicationData = self.medicationPanes[j].getMedicationData();
            if (self._isBnfMaximumDefined(medicationData) &&
                !tm.jquery.Utils.isEmpty(self._getSelectedRouteFromMedication(medicationData)))
            {
              self._handleBnfMaximumChange(self.medicationPanes[j], medicationData);
            }
            else
            {
              self.medicationPanes[j].clearBnf();
            }
          }
        }
        else
        {
          self.showBnf = false;
          self.removeBnfWarning();
        }
      }
    });

    this.infusionRateTypePane = new app.views.medications.ordering.InfusionRateTypePane({
      view: view,
      visibilityContext: this.visibilityContext,
      preventRateTypeChange: this.isEditMode() && !this.isCopyMode(),
      continuousInfusionChangedFunction: function(continuousInfusion, clearValues)
      {
        self._setMedicationsEditable();
        self.variableRateContainer.hide();
        self.timedDoseElements.removeAll();
        self.recurringContinuousInfusion = false;
        self._continuousInfusionChanged(continuousInfusion, clearValues, true);
        // since we trigger continuousInfusionChangedFunction either when being selected or deselected
        // on top of the related bolusChangedFunction / speedChangedFunction, only recalculate when
        // we actually select/deselect continues infusion and let the other handlers handle other scenarios.
        if (clearValues)
        {
          self._calculateDosing();
        }
        self._handleTherapyLinkButtonDisplay();
      },
      adjustableRateChangeFunction: function(adjustableRate)
      {
        self._setRateContainersVisible(!adjustableRate);
        self.infusionRatePane.clearInfusionValues();
        // reset the possible warnings. If set to false, dose should be entered first before anything needs to be recalc.
        if (adjustableRate)
        {
          self._calculateDosing();
        }
      },
      bolusChangedFunction: function()
      {
        if (self.withStartEndTime === true)
        {
          self.administrationPreviewTimeline.setData(self._buildTherapy(), []);
        }
        self._handleSpeedOrBolusChanged(false);
        self._calculateDosing();
      },
      speedChangedFunction: function(isSpeed)
      {
        if (self.withStartEndTime === true)
        {
          self.administrationPreviewTimeline.setData(self._buildTherapy(), []);
        }
        if (isSpeed)
        {
          self._handleSpeedOrBolusChanged(true);
          self.infusionRatePane.setDurationVisible(true);
        }
        else
        {
          self._handleSpeedOrBolusChanged(false);
        }
        self._calculateDosing();
      }
    });
    this.rateLabelContainer = new tm.jquery.Container({
      width: 160,
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "start")
    });
    var clearRatesContextMenu = appFactory.createContextMenu();
    var clearRatesMenuItem = new tm.jquery.MenuItem({
          cls: "clear-rates-menu-item",
          text: view.getDictionary('empty.form'),
          handler: function()
          {
            if (!self.infusionRatePane.isHidden())
            {
              self.infusionRatePane.clearInfusionValues();
            }
            else
            {
              self.therapyIntervalPane.setStartHourEnabled(true);
              self.dosingFrequencyPane.showAllFields();
              self.infusionRatePane.show();
              self.infusionRatePane.clear();
              self._continuousInfusionChanged(self.infusionRateTypePane.isContinuousInfusion(), true, true);
              self.variableRateContainer.hide();
              self.timedDoseElements.removeAll();
              self.recurringContinuousInfusion = false;
              self._setMedicationsEditable();
              self.infusionRatePane.requestFocus();
              self._calculateDosing();
            }
            self.varioButton.show();
            self.rateLabelContainer.show();
          }
        }
    );
    clearRatesContextMenu.addMenuItem(clearRatesMenuItem);
    this.rateLabelContainer.setContextMenu(clearRatesContextMenu);
    this.rateLabelContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      component.getContextMenu().show(elementEvent.pageX, elementEvent.pageY);
    });
    this.rateLabelVSpacer = this._createVerticalSpacer(2);
    this.infusionRatePane = new app.views.medications.ordering.InfusionRatePane({
      view: view,
      scrollable: "visible",
      cls: "infusion-rate-pane",
      width: 587,
      getInfusionRateTypeBolusFunction: function()
      {
        return self.infusionRateTypePane.isBolus();
      },
      setInfusionRateTypeFunction: function(rate)
      {
        self._infusionRateTypePaneFunction(rate);
      },
      getInfusionIngredientsFunction: function()
      {
        return self._getComplexTherapyIngredients();
      },
      getContinuousInfusionFunction: function()
      {
        return self.infusionRateTypePane.isContinuousInfusion();
      },
      getVolumeSumFunction: function()
      {
        return self.volumeSumPane.getVolumeSum();
      },
      durationChangeEvent: function()
      {
        self.therapyIntervalPane.calculateEnd();
        self.refreshAdministrationPreview();
      },
      rateFormulaChangeEvent: function()
      {
        self._calculateDosing();
      },
      singleIngredientVolumeCalculatedEvent: function(volume)
      {
        self.medicationPanes[0].setVolume(volume);
        self._handleVarioEnabling();
      },
      formulaVisibleFunction: function()
      {
        return !self._areAllIngredientsSolutions();
      }
    });
    this.variableRateContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      margin: '5 0 0 10'
    });

    this.varioButton = new tm.jquery.Button({
      cls: "vario-button",
      text: view.getDictionary('variable'),
      type: 'link',
      handler: function()
      {
        self._openVariableRateEditPane();
      }
    });

    this.dosingFrequencyPane = new app.views.medications.ordering.DosingFrequencyPane({
      view: view,
      visibilityContext: this.visibilityContext,
      editMode: this.isEditMode(),
      width: 678,
      frequencyChangeEvent: function()
      {
        self._handleDosingFrequencyChange();
        self._calculateDosing();
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
    this.therapyStartTitle = new tm.jquery.Container({
      cls: 'TextLabel',
      html: view.getDictionary('start'),
      width: 255
    });
    this.therapyDurationTitle = new tm.jquery.Container({
      cls: 'TextLabel',
      html: view.getDictionary('therapy.duration'),
      width: 300
    });
    this.reviewReminderTitle = new tm.jquery.Container({
      cls: 'TextLabel',
      html: view.getDictionary('reminder')
    });
    this.maxFrequencyTitleLabel = new tm.jquery.Container({
      cls: 'TextLabel',
      html: 'Max',
      tooltip: tm.views.medications.MedicationUtils.createTooltip(view.getDictionary('dosing.max.24h'), null, view)
    });
    this.therapyIntervalPane = new app.views.medications.ordering.TherapyIntervalPane({
      view: view,
      width: 678,
      isPastMode: this.isPastMode,
      withStartEndTime: this.withStartEndTime,
      hidden: this.withSupply === true,
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
        return self.timedDoseElements.length > 0 ?
            [self.timedDoseElements[0].doseTime] :
            self.dosingFrequencyPane.getDosingPattern();
      },
      getDurationFunction: function()
      {
        var infusionRate = self.infusionRatePane.getInfusionRate();
        if (infusionRate && infusionRate.duration)
        {
          return infusionRate.duration;
        }
        return null;
      },
      untilHideEvent: function(hide)
      {
        if (hide)
        {
          self.therapyDurationTitle.setHtml("");
        }
        else
        {
          self.therapyDurationTitle.setHtml(view.getDictionary('therapy.duration'));
        }
      },
      reminderHideEvent: function(hide)
      {
        if (hide)
        {
          self.reviewReminderTitle.setHtml("");
        }
        else
        {
          self.reviewReminderTitle.setHtml(view.getDictionary('reminder'));
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
          var durationGiven = self.infusionRateTypePane.isSpeed() && !self.infusionRatePane.isHidden() ?
              !tm.jquery.Utils.isEmpty(self.infusionRatePane.getInfusionRate().duration) :
              true;
          if (self.withStartEndTime === true && durationGiven)
          {
            self.refreshAdministrationPreview();
          }
        });
    if (this.withStartEndTime !== true && this.withSupply === true)
    {
      this.therapySupplyContainer = new app.views.medications.ordering.TherapySupplyContainer({
        view: view
      });
    }

    this.therapyNextAdministrationLabelPane = new app.views.medications.ordering.TherapyNextAdministrationLabelPane({
      hidden: this.withStartEndTime === false,
      view: view
    });

    if (this.showSource)
    {
      this.sourceLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary("source"));
      this.sourceField = new tm.jquery.SelectBox({
        dropdownHeight: 5,
        dropdownWidth: "stretch",
        options: tm.views.medications.MedicationUtils.createTherapySourceSelectBoxOptions(view),
        selections: [],
        multiple: false,
        allowSingleDeselect: true,
        placeholder: " ",
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        padding: "0 20 0 0",
        appendTo: function()
        {
          return appFactory.getDefaultRenderToElement();
        }
      });
    }

    this.pastDaysOfTherapySpacer = this._createVerticalSpacer(2);
    this.pastDaysOfTherapyLabel = tm.views.medications.MedicationUtils.crateLabel(
        'TextLabel',
        this.getView().getDictionary('consecutive.days.antibiotic'));
    this.pastDaysOfTherapyField = new tm.jquery.TextField({cls: "therapy-past-days-field", width: 68});
    this._hidePastDaysOfTherapy();

    this.commentIndicationPane = new app.views.medications.ordering.CommentIndicationPane({
      view: view,
      visibilityContext: this.visibilityContext,
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePaneEvent();
      }
    });
    this.changeReasonPane = new app.views.medications.ordering.ChangeReasonPane({
      padding: "5 20 0 0",
      view: view,
      hidden: !this.isChangeReasonRequired()
    });
    this.calculatedDosagePane = new app.views.medications.ordering.CalculatedDosagePane({view: view, height: 20});

    this._extensionsPlaceholder = new tm.jquery.Container({
      cls: "extensions-container",
      scrollable: "visible",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this.warningsContainer = new tm.views.medications.warning.SimpleWarningsContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "visible"
    });
    if (!this.isEditMode())
    {
      this.warningsContainer.show();
      this.addToBasketButton = new tm.jquery.Button({
        cls: "add-to-basket-button",
        text: view.getDictionary("add"),
        handler: function()
        {
          self.addToBasketButton.setEnabled(false);
          self.validateAndConfirmOrder();
        }
      });

      this.templatesButton = new tm.jquery.Button({
        cls: "templates-button",
        text: view.getDictionary('empty.form'),
        type: "link",
        //flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent('TEMPLATES');
            self.refreshBasketFunction();
          }
        }
      });
      this.simpleTherapyButton = new tm.jquery.Button({
        cls: "simple-therapy-button",
        text: view.getDictionary('simple'),
        type: "link",
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent(
                self.medicationData,
                self.warningsContainer.list.getListData(),
                self.medicationPanes.length > 1
            );
            self.refreshBasketFunction();
          }
        }
      });
      this.saveToTemplateButton = new tm.jquery.Button({
        cls: "save-to-template-button",
        text: view.getDictionary('add.to.template'),
        type: "link",
        handler: function()
        {
          if (self.saveToTemplateFunction)
          {
            var therapy = self._buildTherapy();
            therapy.linkName = null;
            therapy.linkCompositionUid = null;
            self._setupValidation();
            var invalidTherapy = self.validationForm.hasFormErrors();
            self.saveToTemplateFunction(therapy, invalidTherapy);
          }
        }
      });

      this.linkTherapyButton = new tm.jquery.Button({
        cls: "link-therapy-button",
        text: view.getDictionary("link.medication"),
        style: "width: 150px, text-align:left;",
        type: "link",
        handler: function()
        {
          self._openLinkTherapyDialog();
        }
      });
      this.linkIcon = new tm.jquery.Container({
        margin: '8 -18 0 0',
        cls: 'basket-container-link'
      });
      this.unlinkTherapyButton = new tm.jquery.Button({
        cls: "unlink-therapy-button",
        text: view.getDictionary("unlink.medication"),
        style: "width: 150px, text-align:left;",
        type: "link",
        handler: function()
        {
          self._unlinkTherapy();
          self._handleTherapyLinkButtonDisplay();
          self.refreshBasketFunction();
        }
      });
    }
    else
    {
      this.warningsContainer.hide();
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
      requiredFieldValidatorErrorMessage: view.getDictionary("field.value.is.required")
    });

    this._setRateContainersVisible(false);
  },

  _handleBnfWarningChange: function(bnfPercentage, medicationData)
  {
    !tm.jquery.Utils.isEmpty(bnfPercentage) && bnfPercentage >= 100 ?
        this.warningsContainer.addBnfWarning(bnfPercentage, medicationData.getMedication())
        : this.removeBnfWarning(medicationData);
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
    this.add(this.medicationPanesContainer);

    var mainContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      margin: '0 0 0 20',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: 'visible'
    });
    this.add(mainContainer);
    var heparinRowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")});
    if (this.showHeparinPane === true)
    {
      mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', 'Heparin'));
      heparinRowContainer.add(this.heparinPane);
      this.heparinPane.show();
    }
    else
    {
      mainContainer.add(this.volumeSumTopSpacer);
    }
    heparinRowContainer.add(this.volumeSumPane);
    mainContainer.add(heparinRowContainer);
    mainContainer.add(this._createVerticalSpacer(2));

    mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('route')));
    var routesRowPane = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5),
      scrollable: 'visible'
    });
    routesRowPane.add(this.routesPane);
    routesRowPane.add(this.infusionRateTypePane);
    mainContainer.add(routesRowPane);
    mainContainer.add(this._createVerticalSpacer(2));

    this.rateLabelContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('infusion.rate')));
    this.rateLabelContainer.add(new tm.jquery.Container({cls: 'menu-icon', width: 16, height: 16, margin: '4 0 0 5'}));
    mainContainer.add(this.rateLabelContainer);
    mainContainer.add(this.rateLabelVSpacer);
    var infusionRateContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      width: 678,
      scrollable: "visible"
    });
    infusionRateContainer.add(this.infusionRatePane);
    infusionRateContainer.add(this.varioButton);
    infusionRateContainer.add(this.variableRateContainer);

    mainContainer.add(infusionRateContainer);
    mainContainer.add(this._createVerticalSpacer(2));

    this.dosingFrequencyLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval'));
    mainContainer.add(this.dosingFrequencyLabel);
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
      this.administrationPreviewTimeline = new app.views.medications.ordering.AdministrationPreviewTimeline({
        margin: "10 20 0 0",
        view: this.view
      });
      mainContainer.add(this.administrationPreviewTimeline);
    }

    mainContainer.add(this._createVerticalSpacer(2));

    mainContainer.add(this.pastDaysOfTherapySpacer);
    mainContainer.add(this.pastDaysOfTherapyLabel);
    mainContainer.add(this.pastDaysOfTherapyField);
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

      this.linkTherapyButton.hide();
      this.unlinkTherapyButton.hide();
      this.linkIcon.hide();
      navigationContainer.add(this.templatesButton);
      navigationContainer.add(new tm.jquery.Spacer({type: 'horizontal', size: 50}));
      navigationContainer.add(this.simpleTherapyButton);
      navigationContainer.add(this.saveToTemplateButton);
      navigationContainer.add(this.linkTherapyButton);
      navigationContainer.add(this.linkIcon);
      navigationContainer.add(this.unlinkTherapyButton);
      var addToBasketContainer = new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
        layout: tm.jquery.HFlexboxLayout.create("flex-end", "flex-start")
      });
      addToBasketContainer.add(this.addToBasketButton);
      navigationContainer.add(addToBasketContainer);
      this.add(navigationContainer);
    }

    this.add(this.warningsContainer);

    this._applyChangeReasonPaneVisibility();
    this._rebuildExtensions();
  },

  _ensureChangeReasonOptionsLoaded: function()
  {
    var pane = this.changeReasonPane;
    var editEnum = app.views.medications.TherapyEnums.pharmacistTherapyChangeType.EDIT;
    var appFactory = this.getView().getAppFactory();

    if (tm.jquery.Utils.isEmpty(this.getView().getTherapyChangeReasonTypeMap()))
    {
      this.getView().loadTherapyChangeReasonTypeMap(function onDataLoad(data)
      {
        pane.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component){
          appFactory.createConditionTask(
              function()
              {
                component.setReasonOptions(data[editEnum]);
              },
              function(task)
              {
                return component.isRendered(true);
              },
              50, 100
          );
        });
      });
    }
    else
    {
      pane.setReasonOptions(this.getView().getTherapyChangeReasonTypeMap()[editEnum]);
    }
  },

  _createVerticalSpacer: function(size)
  {
    return new tm.jquery.Spacer({type: 'vertical', size: size});
  },

  _setRateContainersVisible: function(isVisible)
  {
    if (isVisible)
    {
      this.infusionRatePane.show();
      this.rateLabelContainer.show();
      this.rateLabelVSpacer.hide();
      this.varioButton.show();
    }
    else
    {
      this.infusionRatePane.hide();
      this.rateLabelContainer.hide();
      this.rateLabelVSpacer.show();
      this.varioButton.hide();
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData|null} medicationData
   * @param {boolean} addSpacer
   * @param {boolean} medicationEditable
   * @param {boolean} sameGenericOnly
   * @param {boolean} addRemoveEnabled
   * @param {boolean} isMainMedication
   * @returns {app.views.medications.ordering.ComplexTherapyMedicationPane}
   * @private
   */
  _addMedicationPane: function(medicationData, addSpacer, medicationEditable, sameGenericOnly, addRemoveEnabled,
                               isMainMedication)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;

    var medicationPane = new app.views.medications.ordering.ComplexTherapyMedicationPane({
      view: this.view,
      addSpacer: addSpacer,
      medicationEditable: medicationEditable,
      medicationEditableSameGenericOnly: sameGenericOnly,
      addRemoveEnabled: addRemoveEnabled,
      medicationData: medicationData,
      titratedDoseSupported: self.medicationPanes.length === 0 && !(this.visibilityContext &&
          this.visibilityContext.hasOwnProperty("titratedDoseModeVisible") &&
          this.visibilityContext.titratedDoseModeVisible === false),
      additionalMedicationSearchFilter: self.additionalMedicationSearchFilter,
      preventUnlicensedMedicationSelection: self.preventUnlicensedMedicationSelection,
      preventTitrationChange: this.isEditMode() && !this.isCopyMode(),
      addElementEvent: function(medicationPane)
      {
        self._addMedicationPane(null, true, true, false, true, false);
        self._handleFirstMedicationPaneTitratedDoseModeSupport();
        self._handleFirstMedicationPaneDoseVisibility();
        self.medicationPanesContainer.repaint(); // repaint after values for visibility are set, because of the repaint yield
        self._showHideVolumeSum();

        self._executeOnValidContentExtensions(function(extension)
        {
          extension.therapyMedicationsCountChangedFunction(self.medicationPanes.length);
        });

        setTimeout(function()
        {
          self._focusToNextMedicationPane(medicationPane);
        }, 500);
      },
      removeElementEvent: function()
      {
        if (self.medicationPanes.length > 1)
        {
          self._removeMedicationPane();
          self._handleFirstMedicationPaneTitratedDoseModeSupport();
          self._handleFirstMedicationPaneDoseVisibility();
          self.medicationPanesContainer.repaint(); // repaint after values for visibility are set, because of the repaint yield
          self._showHideVolumeSum();
          self.infusionRatePane.setFormulaVisible();
          self._calculateVolumeSum();

          self.removeBnfWarning(medicationPane.getMedicationData());
          self._handleParacetamolRuleChange();

          if (!self.infusionRatePane.isHidden())
          {
            self.infusionRatePane.calculateInfusionValues();
          }
          if (self.screenTherapyForWarningsFunction)
          {
            self.screenTherapyForWarningsFunction();
          }
          if (self.saveToTemplateButton)
          {
            if (self._existsUniversalMedication())
            {
              self.saveToTemplateButton.hide();
            }
            else
            {
              self.saveToTemplateButton.show();
            }
          }
          self._executeOnValidContentExtensions(function(extension)
          {
            extension.therapyMedicationsCountChangedFunction(self.medicationPanes.length);
          });
          self._handleInfusionTypePaneAvailability();
        }
        else
        {
          if (!self.isEditMode() && self.changeCardEvent)
          {
            self.changeCardEvent('TEMPLATES');
            self.refreshBasketFunction();
          }
        }
      },
      volumeChangedEvent: function()
      {
        self._calculateVolumeSum();
        self._handleVarioEnabling();
        if (!self.infusionRatePane.isHidden())
        {
          self.infusionRatePane.calculateInfusionValues();
        }
      },
      numeratorChangeEvent: function()
      {
        self._calculateDosing(self);
        self.infusionRatePane.refreshRate();
      },
      focusLostEvent: function(medicationPane)
      {
        self._focusToNextMedicationPane(medicationPane);
      },
      medicationChangedEvent: function(medicationData)
      {
        self.infusionRatePane.setFormulaVisible();
        self.infusionRatePane.setFormula(null, true);
        if (isMainMedication)
        {
          self._setMedicationData(medicationData);
        }
        if (medicationData.getMedication().isMedicationUniversal())
        {
          medicationPane.hideMedicationInfo(true);
          medicationPane.setMedication(medicationData.getMedication());

          if (self.saveToTemplateButton)
          {
            self.saveToTemplateButton.hide();
          }
        }
        else
        {
          medicationPane.hideMedicationInfo(false);
        }
        self._handleFirstMedicationPaneDoseVisibility();
        if (self.screenTherapyForWarningsFunction)
        {
          self.screenTherapyForWarningsFunction();
        }
        self._handleInfusionTypePaneAvailability();
      }
    });

    if (this.medicationPanes.length === 0)
    {
      medicationPane.on(
          app.views.medications.ordering.ComplexTherapyMedicationPane.EVENT_TYPE_TITRATION_CHANGE,
          function(component, componentEvent)
          {
            var eventData = componentEvent.eventData;
            self._onTitrationDosingSelectionChanged(eventData && eventData.selected);
          });
    }

    if (medicationData && self._isBnfMaximumDefined(medicationPane.getMedicationData()))
    {
      medicationPane.displayBnf(true);
      medicationPane.setDefinedBnfValuesAndSelectedRoute(medicationPane.getMedicationData(),
          this._getSelectedRouteFromMedication(medicationPane.getMedicationData()));
    }

    var lastMedicationPaneIndex = this.medicationPanes.length - 1;
    if (lastMedicationPaneIndex >= 0)
    {
      this.medicationPanes[lastMedicationPaneIndex].setAddRemoveButtonsVisible(false);
    }
    if (medicationData)
    {
      if (medicationData.getMedication().isMedicationUniversal())
      {
        if (self.saveToTemplateButton)
        {
          self.saveToTemplateButton.hide();
        }
        medicationPane.hideMedicationInfo(true);
      }
      else
      {
        medicationPane.hideMedicationInfo(false);
      }
    }
    this.medicationPanes.push(medicationPane);
    this.medicationPanesContainer.add(medicationPane);
    return medicationPane;
  },

  _focusToNextMedicationPane: function(medicationPane)
  {
    var index = this.medicationPanes.indexOf(medicationPane);
    var nextMedicationPane = this.medicationPanes[index + 1];
    if (nextMedicationPane)
    {
      nextMedicationPane.focusToMedicationField();
    }
    else
    {
      this.routesPane.requestFocus();
    }
  },

  _removeMedicationPane: function()
  {
    var lastMedicationPane = this.medicationPanes[this.medicationPanes.length - 1];
    this.medicationPanesContainer.remove(lastMedicationPane);
    this.medicationPanes.pop();   //removes last medicationPane
    var lastMedicationPaneIndex = this.medicationPanes.length - 1;
    this.medicationPanes[lastMedicationPaneIndex].setAddRemoveButtonsVisible(true);
  },

  _openVariableRateEditPane: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var volumeSum = null;
    if (!this.volumeSumPane.isHidden())
    {
      volumeSum = this.volumeSumPane.getVolumeSum();
    }
    else if (this.medicationPanes.length == 1)
    {
      volumeSum = this.medicationPanes[0].getVolume();
    }

    var variableDosePane = new app.views.medications.ordering.ComplexVariableRateDataEntryContainer({
      view: self.view,
      startProcessOnEnter: true,
      medicationData: this.medicationData,
      timedDoseElements: this.timedDoseElements,
      recurringContinuousInfusion: this.recurringContinuousInfusion,
      infusionIngredients: this._getComplexTherapyIngredients(),
      continuousInfusion: this.infusionRateTypePane.isContinuousInfusion(),
      volumeSum: volumeSum,
      showFormula: !this._areAllIngredientsSolutions()
    });

    var variableDoseEditDialog = appFactory.createDataEntryDialog(
        this.view.getDictionary('variable.dose'),
        null,
        variableDosePane,
        function(resultData)
        {
          if (resultData && resultData.isSuccess())
          {
            self.infusionRatePane.hide();
            self.dosingFrequencyPane.showDaysOnly();
            self.timedDoseElements = resultData.timedDoseElements;
            self.recurringContinuousInfusion = resultData.recurring;
            self._showVariableRateDisplayValue();
            if (self.recurringContinuousInfusion)
            {
              var nextAdministrationTimestamp =
                  tm.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForVario(self.timedDoseElements);
              self.therapyIntervalPane.setStart(nextAdministrationTimestamp);
              self.therapyIntervalPane.setStartHourEnabled(true);
            }
            else if (self.infusionRateTypePane.isContinuousInfusion())
            {
              self._setAndDisableStartHourForVario();
            }
            else
            {
              self._handleDosingFrequencyChange();
            }
            self._setMedicationsEditable();
            self._calculateDosing();
          }
        },
        550,
        330
    );
    var removeVarioButton = variableDoseEditDialog.getBody().footer.cancelButton;
    removeVarioButton.setText(this.view.getDictionary('remove.vario'));
    removeVarioButton.setType("link");
    var removeVarioButtonHandler = removeVarioButton.getHandler();
    removeVarioButton.setHandler(function()
    {
      self.therapyIntervalPane.setStartHourEnabled(true);
      self.infusionRatePane.clear();
      self._continuousInfusionChanged(self.infusionRateTypePane.isContinuousInfusion(), true, true);
      self.dosingFrequencyPane.showAllFields();
      self.dosingFrequencyLabel.show();
      self.variableRateContainer.hide();
      self.timedDoseElements.removeAll();
      self.recurringContinuousInfusion = false;
      self._setRateContainersVisible(true);
      self._setMedicationsEditable();
      self._calculateDosing();
      self.infusionRatePane.requestFocus();
      removeVarioButtonHandler();
    });

    variableDoseEditDialog.getBody().footer.setRightButtons([removeVarioButton, variableDoseEditDialog.getBody().footer.confirmButton]);
    variableDoseEditDialog.show();
  },

  _showVariableRateDisplayValue: function()
  {
    var utils = tm.views.medications.MedicationUtils;
    this.variableRateContainer.removeAll();
    var solutionsOnly = this._areAllIngredientsSolutions();
    for (var i = 0; i < this.timedDoseElements.length; i++)
    {
      var rowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 10)});
      var timedDoseElement = this.timedDoseElements[i];
      var doseTime = timedDoseElement.doseTime;
      var doseElement = timedDoseElement.doseElement;

      var startTimeDisplayValue = tm.views.medications.MedicationTimingUtils.hourMinuteToString(doseTime.hour, doseTime.minute) + ' - ';
      rowContainer.add(utils.crateLabel('TextLabel', startTimeDisplayValue, '1 0 0 0'));

      if (doseElement.duration)
      {
        var endTime = CurrentTime.get();
        endTime.setHours(doseTime.hour);
        endTime.setMinutes(doseTime.minute + doseElement.duration);
        var endTimeDisplayValue = tm.views.medications.MedicationTimingUtils.hourMinuteToString(endTime.getHours(), endTime.getMinutes()) + '  ';
        rowContainer.add(utils.crateLabel('TextLabel', endTimeDisplayValue, '1 0 0 0'));
      }
      else
      {
        rowContainer.add(utils.crateLabel('TextLabel', '...', '1 20 0 0'));
      }
      var doseDisplayValue = utils.getFormattedDecimalNumber(utils.doubleToString(doseElement.rate, 'n2')) + ' ' +
          utils.getFormattedUnit(doseElement.rateUnit);
      if (!solutionsOnly && doseElement.rateFormula)
      {
        doseDisplayValue += ' (' +
            utils.getFormattedDecimalNumber(utils.doubleToString(doseElement.rateFormula, 'n2')) + ' ' +
            doseElement.rateFormulaUnit + ')';
      }

      rowContainer.add(utils.crateLabel('TextData', doseDisplayValue, 0));
      this.variableRateContainer.add(rowContainer);
    }

    this.variableRateContainer.show();
    this.variableRateContainer.repaint();
  },

  _setMedicationsEditable: function()
  {
    var vario = this.timedDoseElements.length > 0;
    var continuousInfusion = this.infusionRateTypePane.isContinuousInfusion();
    var enableAddingAndRemoving = !vario && (!this.isEditMode() || this.isCopyMode() || continuousInfusion);
    var doseEditable = !vario;

    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var isLastRow = i == this.medicationPanes.length - 1;
      var showAddRemoveButtons = enableAddingAndRemoving && isLastRow;
      var medicationEditable = !vario && (this.isEditMode() || i > 0);
      var medicationEditableSameGenericOnly = !vario && this.isEditMode() && !this.isCopyMode() &&
          !this.medicationPanes[i].getMedicationData().getMedication().isMedicationUniversal();
      if (continuousInfusion)
      {
        medicationEditableSameGenericOnly = medicationEditableSameGenericOnly && i == 0;
      }
      this.medicationPanes[i].setPaneEditable(
          showAddRemoveButtons, medicationEditable, medicationEditableSameGenericOnly, doseEditable);
    }
  },

  _setAndDisableStartHourForVario: function()
  {
    var viewMode = this.view.getViewMode();
    if (viewMode != 'ORDERING_PAST' && viewMode != 'EDIT_PAST')
    {
      var firstTimedDoseElement = this.timedDoseElements[0];
      var start = CurrentTime.get();
      start.setHours(firstTimedDoseElement.doseTime.hour);
      start.setMinutes(firstTimedDoseElement.doseTime.minute);
      this.therapyIntervalPane.setStart(start);
      this.therapyIntervalPane.setStartHourEnabled(false);
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @returns {boolean}
   */
  _isBnfMaximumDefined: function(medicationData)
  {
    var self = this;
    var selectedRoutes = this.routesPane.getSelectedRoutes();

    if (medicationData && medicationData.getRoutes() && selectedRoutes.length === 1)
    {
      return medicationData.getRoutes().some(function(route)
      {
        if (!tm.jquery.Utils.isEmpty(route.bnfMaximumDto))
        {
          self.showBnf = true;
          return route.id === selectedRoutes[0].id;
        }
      });
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @returns {Object}
   */
  _getSelectedRouteFromMedication: function(medicationData)
  {
    var selectedRoutes = this.routesPane.getSelectedRoutes();

    if (medicationData.getRoutes())
    {
      return medicationData.getRoutes().filter(function(route)
      {
        if (!tm.jquery.Utils.isEmpty(route.bnfMaximumDto))
        {
          return route.id === selectedRoutes[0].id;
        }
      })[0];
    }
  },

  /**
   * @param {boolean} selected
   * @private
   */
  _onTitrationDosingSelectionChanged: function(selected)
  {
    this.therapyIntervalPane.setByDoctorsOrderButtonEnabled(!selected);
    this.infusionRateTypePane.setTitratedDoseMode(selected, true);
    if (!this.infusionRateTypePane.isContinuousInfusion())
    {
      this._continuousInfusionChanged(false, true, false);
    }
    else
    {
      this._setRateContainersVisible(!selected);

      if (selected)
      {
        this.infusionRatePane.clear(true);
      }
      else
      {
        this.infusionRatePane.presentContinuousInfusionFields(true);
      }
    }
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();

    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      this._addValidations(this.medicationPanes[i].getMedicationPaneValidations());
    }

    this._addValidations(this.routesPane.getRoutesPaneValidations());
    this._addValidations(this.commentIndicationPane.getIndicationValidations());
    if (!this.infusionRatePane.isHidden() && this.infusionRateTypePane.isContinuousInfusion())
    {
      this._addValidations(this.infusionRatePane.getInfusionRatePaneValidations());
    }
    if (!this.dosingFrequencyPane.isHidden())
    {
      this._addValidations(this.dosingFrequencyPane.getDosingFrequencyPaneValidations());
    }
    if (this.withStartEndTime === true)
    {
      if (this.isEditMode() && !this.isCopyMode())
      {
        var therapyStartNotBefore = this.getTherapyStartNotBeforeDateFunction ? this.getTherapyStartNotBeforeDateFunction() : null;
        this._addValidations(this.therapyIntervalPane.getTherapyIntervalPaneValidations(therapyStartNotBefore));
      }
      else
      {
        this._addValidations(this.therapyIntervalPane.getTherapyIntervalPaneValidations());
      }
    }
    if (this.therapySupplyContainer != null)
    {
      this._addValidations(this.therapySupplyContainer.getFormValidations());
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
    if (this.isChangeReasonRequired())  this._addValidations(this.changeReasonPane.getChangeReasonValidations());

    if ((this.therapySupplyContainer == null) &&
        (this.therapyIntervalPane.getWhenNeeded()))
    {
      this._addValidations(this.commentIndicationPane.getIndicationFieldValidation());
    }

    this._executeOnValidContentExtensions(function(extension)
    {
      self._addValidations(extension.getFormValidations());
    });
  },

  _addValidations: function(validation)
  {
    for (var i = 0; i < validation.length; i++)
    {
      this.validationForm.addFormField(validation[i]);
    }
  },

  _showHideVolumeSum: function()
  {
    if (this.medicationPanes.length > 1)
    {
      if (!this.showHeparinPane)
      {
        this.isRendered() ? this.volumeSumTopSpacer.show() : this.volumeSumTopSpacer.setHidden(false);
      }
      this.isRendered() ? this.volumeSumPane.show() : this.volumeSumPane.setHidden(false) ;
    }
    else
    {
      this.isRendered() ? this.volumeSumPane.hide() : this.volumeSumPane.setHidden(true);
      if (!this.showHeparinPane)
      {
        this.isRendered() ? this.volumeSumTopSpacer.hide() : this.volumeSumTopSpacer.setHidden(true);
      }
    }
  },

  _handleDosingFrequencyChange: function()
  {
    this._calculateStartAndEnd(this._buildTherapy(), true);
  },

  /*
   * @param therapy
   * @private
   */
  _calculateStartAndEnd: function(therapy)
  {
    var self = this;
    // only copy the end date when editing
    var oldTherapy = this.isEditMode() && !this.isCopyMode() ? this.therapyToEdit : null;
    this.therapyIntervalPane.calculateStart(therapy, tm.jquery.Utils.isEmpty(oldTherapy), oldTherapy, function()
    {
      self.therapyIntervalPane.calculateEnd();
    });
  },

  _adjustVolumes: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var medsAndSuppsVolumeSum = this._getTherapyVolumeSumOfTypes([enums.medicationTypeEnum.MEDICATION, enums.medicationTypeEnum.SUPPLEMENT]);
    var solutionsVolumeSum = this._getTherapyVolumeSumOfTypes([enums.medicationTypeEnum.SOLUTION]);
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var solutionData = this.medicationPanes[i].getMedicationData();
      if (solutionData)
      {
        var medicationType = solutionData.medication.medicationType;
        if (medicationType == enums.medicationTypeEnum.SOLUTION)
        {
          var solutionVolume = this.medicationPanes[i].getVolume();
          var solutionRatio = solutionVolume / solutionsVolumeSum;
          var newVolume = solutionVolume - medsAndSuppsVolumeSum * solutionRatio;
          this.medicationPanes[i].setVolume(newVolume);
        }
      }
    }
    this._calculateVolumeSum();
  },

  _getTherapyVolumeSumOfTypes: function(types)
  {
    var volumeSum = 0;
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var medicationData = this.medicationPanes[i].getMedicationData();
      if (medicationData)
      {
        var medicationType = medicationData.medication.medicationType;
        if ($.inArray(medicationType, types) > -1)
        {
          var volume = this.medicationPanes[i].getVolume();
          if (volume)
          {
            volumeSum += volume;
          }
        }
      }
    }
    return volumeSum;
  },

  _calculateVolumeSum: function()
  {
    var volumeSum = 0;
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var volume = this.medicationPanes[i].getVolume();
      if (volume)
      {
        volumeSum += volume;
      }
    }
    this.volumeSumPane.setVolumeSum(volumeSum);
    this._handleVarioEnabling();
  },

  _handleSpeedOrBolusChanged: function(rateContainersVisible)
  {
    this._handleFirstMedicationPaneDoseVisibility();
    this.infusionRatePane.clearFieldValues();
    this.dosingFrequencyLabel.show();
    this.dosingFrequencyPane.show();
    this._setRateContainersVisible(rateContainersVisible);
    //handle vario
    this.variableRateContainer.hide();
    this.timedDoseElements.removeAll();
    this._setMedicationsEditable();
    this.dosingFrequencyPane.showAllFields();
    this.therapyIntervalPane.setStartHourEnabled(true);
    this._handleVarioEnabling();
  },

  _continuousInfusionChanged: function(continuousInfusion, clearInfusionValues, calculateStart)
  {
    this.infusionRatePane.presentContinuousInfusionFields(continuousInfusion);
    this._handleFirstMedicationPaneTitratedDoseModeSupport();
    if (continuousInfusion)
    {
      this._setRateContainersVisible(true);
      this.dosingFrequencyLabel.hide();
      this.dosingFrequencyPane.clear();
      this.dosingFrequencyPane.hide();
      this.therapyIntervalPane.setRestrictedStartHourSelection(false);
      this.therapyIntervalPane.hideWhenNeededDoctorsOrder();
    }
    else
    {
      this._setRateContainersVisible(false);
      this.dosingFrequencyPane.showAllFields();
      this.dosingFrequencyLabel.show();
      this.dosingFrequencyPane.show();
      if (!this.isEditMode() || this.isCopyMode())
      {
        this.therapyIntervalPane.setRestrictedStartHourSelection(true);
      }
      this.therapyIntervalPane.showWhenNeededDoctorsOrder();
    }
    this.infusionRatePane.updateFormulaUnitsForContinuous(continuousInfusion);
    this.therapyIntervalPane.setMaxDailyFrequencyFieldVisible(!continuousInfusion);

    if (clearInfusionValues)
    {
      this.infusionRatePane.clearInfusionValues();
      this.therapyIntervalPane.setStartHourEnabled(true);
    }
    var therapy = this._buildTherapy();
    var oldTherapy = this.isEditMode() && !this.isCopyMode() ? this.therapyToEdit : null;
    if (calculateStart)
    {
      this.therapyIntervalPane.calculateStart(therapy, true, oldTherapy, null);
    }

    this._handleFirstMedicationPaneDoseVisibility();
    this._handleVarioEnabling();
  },

  _handleFirstMedicationPaneDoseVisibility: function()
  {
    if (this.infusionRateTypePane.isContinuousInfusion() && this.medicationPanes.length == 1)
    {
      this.medicationPanes[0].setDoseVisible(false);
    }
    else if (this.medicationPanes.length > 0)
    {
      this.medicationPanes[0].setDoseVisible(tm.jquery.Utils.isEmpty(this.medicationPanes[0].getTitrationDoseType()));
    }
  },

  _handleFirstMedicationPaneTitratedDoseModeSupport: function()
  {
    if (this.medicationPanes.length > 0)
    {
      this.medicationPanes[0].setTitratedDoseSupported(
          !this.infusionRateTypePane.isContinuousInfusion() && this.medicationPanes.length === 1 &&
          !(this.visibilityContext &&
              this.visibilityContext.hasOwnProperty("titratedDoseModeVisible") &&
              this.visibilityContext.titratedDoseModeVisible === false));
    }
  },

  _handleVarioEnabling: function()
  {
    var volumeSumSet = !this.volumeSumPane.isHidden() && this.volumeSumPane.getVolumeSum() > 0 ||
        this.medicationPanes.length == 1 && this.medicationPanes[0].getVolume() > 0;
    var continuousInfusion = this.infusionRateTypePane.isContinuousInfusion();
    this.varioButton.setEnabled(volumeSumSet || continuousInfusion);
  },

  _areAllIngredientsSolutions: function()
  {
    var solutionsOnly = true;
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var infusionIngredient = this.medicationPanes[i].getInfusionIngredient();
      if (infusionIngredient &&
          infusionIngredient.medication.medicationType != app.views.medications.TherapyEnums.medicationTypeEnum.SOLUTION)
      {
        solutionsOnly = false;
        break;
      }
    }
    return solutionsOnly;
  },

  _calculateDosing: function()
  {
    var ingredients = this._getComplexTherapyIngredients();
    if (ingredients.length == 1)
    {
      var quantity;
      var quantityUnit;
      var weightInKg;
      var heightInCm;

      if (this.infusionRateTypePane.isContinuousInfusion())
      {
        quantityUnit = ingredients[0].quantityUnit;
        weightInKg = this.view.getReferenceWeight();
        heightInCm = this.view.getPatientHeightInCm();

        var rateFormulaUnit = this.infusionRatePane.getRateFormulaUnit();
        var formulaPatientUnit = rateFormulaUnit.patientUnit;
        var infusionRateFormulaInMgPerHour = this.infusionRatePane.getInfusionRateFormulaPerHour(quantityUnit);

        var calculatedInfusionRateWithPatientUnitFormula;

        if (formulaPatientUnit === 'kg')
        {
          calculatedInfusionRateWithPatientUnitFormula = infusionRateFormulaInMgPerHour * weightInKg;
        }
        else if (formulaPatientUnit === 'm2')
        {
          var calculatedBodySurfaceArea = tm.views.medications.MedicationUtils.calculateBodySurfaceArea(heightInCm, weightInKg);
          calculatedInfusionRateWithPatientUnitFormula = infusionRateFormulaInMgPerHour * calculatedBodySurfaceArea;
        }
        else
        {
          calculatedInfusionRateWithPatientUnitFormula = infusionRateFormulaInMgPerHour;
        }

        quantity = infusionRateFormulaInMgPerHour != null ? calculatedInfusionRateWithPatientUnitFormula : null;
        this.calculatedDosagePane.calculate(quantity, quantityUnit, null, weightInKg, heightInCm, true);
      }
      else
      {
        quantity = ingredients[0].quantity;
        quantityUnit = ingredients[0].quantityUnit;
        var timesPerDay = this.dosingFrequencyPane.getFrequencyTimesPerDay();
        weightInKg = this.view.getReferenceWeight();
        heightInCm = this.view.getPatientHeightInCm();
        this.calculatedDosagePane.calculate(quantity, quantityUnit, timesPerDay, weightInKg, heightInCm, false);
        this.medicationPanes[0].overdosePane.calculateOverdose(quantity);
      }
    }
    else
    {
      this.calculatedDosagePane.clear();
    }

    if (this.medicationPanes.length >= 1)
    {
      for (var j = 0; j < this.medicationPanes.length; j++)
      {
        var medicationData = this.medicationPanes[j].getMedicationData();
        if (this._isBnfMaximumDefined(medicationData) &&
            !tm.jquery.Utils.isEmpty(this._getSelectedRouteFromMedication(medicationData)))
        {
          this._handleBnfMaximumChange(this.medicationPanes[j], medicationData);
        }
      }
    }

    if (ingredients.length >= 1)
    {
      this._handleParacetamolRuleChange();
    }
  },

  /**
   * @param medicationPane
   * @param {app.views.medications.common.dto.MedicationData]} medicationData
   * @private
   */
  _handleBnfMaximumChange: function(medicationPane, medicationData)
  {
    var ingredient = medicationPane.getInfusionIngredient();
    var timesPerDay = this.dosingFrequencyPane.getFrequencyTimesPerDay();
    var quantity = !tm.jquery.Utils.isEmpty(ingredient) ? ingredient.quantity : null;
    var variable = false;

    if (this.timedDoseElements.length > 0)
    {
      variable = true;
      quantity = tm.views.medications.MedicationUtils.calculateVariablePerDay(this.timedDoseElements);
    }
    medicationPane.displayBnf(true);
    medicationPane.setDefinedBnfValuesAndSelectedRoute(medicationData,
        this._getSelectedRouteFromMedication(medicationData));
    var bnfPercentage = medicationPane.calculateBnfPercentage(
        quantity, timesPerDay, this.dosingFrequencyPane.getTimesPerWeek(), variable);
    this._handleBnfWarningChange(bnfPercentage, medicationData);
  },

  _handleParacetamolRuleChange: function()
  {
    var view = this.view;
    var self = this;
    var medicationIngredientRuleEnum = app.views.medications.TherapyEnums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE;

    var medicationDataList = [];
    var containsParacetamol = false;
    var paracetamolMedicationPane = null;

    for (var j = 0; j < this.medicationPanes.length; j++)
    {
      var currentPane = this.medicationPanes[j];
      var paracetamolRuleSet = self.medicationRuleUtils.isMedicationRuleSet(currentPane.getMedicationData(), medicationIngredientRuleEnum) === true;
      containsParacetamol = containsParacetamol || paracetamolRuleSet;

      medicationDataList.push(currentPane.getMedicationData());

      if (paracetamolMedicationPane == null && paracetamolRuleSet)
      {
        paracetamolMedicationPane = currentPane;
      }
    }

    if (containsParacetamol)
    {
      self.medicationRuleUtils.getParacetamolRuleForTherapy(
          view,
          this._buildTherapy(),
          medicationDataList,
          view.getPatientData(),
          view.getReferenceWeight()).then(
          function validationSuccessHandler(medicationRuleResult)
          {
            if (self.isRendered())
            {
              paracetamolMedicationPane.setCalculatedParacetamolLimit(medicationRuleResult);
              self._handleParacetamolWarningsChange(medicationRuleResult);
            }
          });
    }
    else
    {
      for (var i = 0; i < this.medicationPanes.length; i++)
      {
        this.medicationPanes[i].paracetamolLimitContainer.hideAll();
      }
    }
  },

  _openLinkTherapyDialog: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_GET_LINK_THERAPY_CANDIDATES_HUB;
    viewHubNotifier.actionStarted(hubAction);
    this.view.showLoaderMask();

    var params = {
      patientId: this.view.getPatientId(),
      referenceWeight: this.view.getReferenceWeight()
    };
    if (this.view.getPatientHeightInCm())
    {
      params.patientHeight = this.view.getPatientHeightInCm();
    }

    var getTherapiesForLinkUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_LINK_THERAPY_CANDIDATES;
    this.view.loadGetViewData(getTherapiesForLinkUrl, params, null,
        function(linkTherapyCandidates)
        {
          self.view.hideLoaderMask();
          var actTherapies = [];
          linkTherapyCandidates.forEach(function(therapy)
          {
            actTherapies.push(new app.views.medications.common.TherapyJsonConverter.convert(therapy));
          });
          actTherapies = actTherapies.concat(self.getBasketTherapiesFunction());
          var dialog = appFactory.createDataEntryDialog(
              self.view.getDictionary('link'),
              null,
              new app.views.medications.ordering.LinkTherapyPane({
                view: self.view,
                orderedTherapies: actTherapies
              }),
              function(resultData)
              {
                if (resultData)
                {
                  var linkTherapy = resultData.selectedTherapy;
                  if (!linkTherapy.linkName)
                  {
                    linkTherapy.linkName = self.view.getPatientNextLinkName();
                  }
                  self.linkName = tm.views.medications.MedicationUtils.getNextLinkName(linkTherapy.linkName);

                  if (linkTherapy.end)
                  {
                    if (self.therapyIntervalPane.getEnd() && self.therapyIntervalPane.getStart())
                    {
                      var diff = linkTherapy.end - self.therapyIntervalPane.getStart();
                      self.therapyIntervalPane.setEnd(
                          new Date(self.therapyIntervalPane.getEnd().getTime() + diff));
                    }
                    self.therapyIntervalPane.setStart(new Date(linkTherapy.end));
                    self._handleTherapyLinkButtonDisplay();
                    self.refreshBasketFunction();
                    self.linkedTherapy = linkTherapy;
                  }
                }
              },
              750,
              480
          );

          dialog.show();
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  _unlinkTherapy: function()
  {
    var isSecondLinkedTherapy = this.linkName && this.linkName.length == 2 && this.linkName.charAt(1) == '2';
    if (isSecondLinkedTherapy) //clear linkName on first therapy
    {
      var previousLinkName = this.linkName.charAt(0) + '1';
      var basketTherapies = this.getBasketTherapiesFunction();
      for (var i = 0; i < basketTherapies.length; i++)
      {
        if (basketTherapies[i].linkName == previousLinkName)
        {
          basketTherapies[i].linkName = null;
          break;
        }
      }
    }
    this.linkName = null;
    this.linkedTherapy = null;
  },

  _infusionRateTypePaneFunction: function(rate, preventEvent)
  {
    if (rate)
    {
      if (rate == 'BOLUS')
      {
        this.infusionRateTypePane.markAsBolus(preventEvent);
      }
      else
      {
        if (!this.infusionRateTypePane.isContinuousInfusion())
        {
          this.infusionRateTypePane.markAsSpeed(preventEvent);
        }
        this.infusionRatePane.setRate(rate, preventEvent);
      }
    }
  },

  _getComplexTherapyIngredients: function()
  {
    var ingredients = [];
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var infusionIngredient = this.medicationPanes[i].getInfusionIngredient();
      if (infusionIngredient)
      {
        ingredients.push(infusionIngredient);
      }
    }
    return ingredients;
  },

  _waitUntilAllMedicationsLoaded: function(loadedMedicationsCount, allMedicationsCount, allMedicationsLoadedEvent)
  {
    var appFactory = this.view.getAppFactory();
    appFactory.createConditionTask(
        function()
        {
          allMedicationsLoadedEvent();
        },
        function()
        {
          return loadedMedicationsCount.count == allMedicationsCount;
        },
        50, 1000
    );
  },

  _confirm: function()
  {
    var therapy = this._buildTherapy();
    var changeReason = this.isChangeReasonRequired() ? new app.views.medications.common.dto.TherapyChangeReason({
      changeReason: this.changeReasonPane.getReasonValue(),
      comment: this.changeReasonPane.getComment()
    }) : null;
    var confirmSuccess = this.confirmTherapyEvent(therapy, changeReason, this.getLinkedTherapy());
    if (confirmSuccess == false)
    {
      this.addToBasketButton.setEnabled(true);
    }
    else if (therapy.linkName)
    {
      this.view.setPatientLastLinkNamePrefix(therapy.linkName.substring(0, 1));
    }

    if (this.view.getViewMode() == 'ORDERING_PAST')
    {
      this.view.setPresetDate(therapy.start);
    }
  },

  /**
   * @returns {string}
   * @private
   */
  _getDoseType: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var multipleIngredients = this.medicationPanes.length > 1;

    if (this.infusionRateTypePane.isContinuousInfusion())
    {
      return enums.therapyDoseTypeEnum.RATE;
    }
    else if (this.infusionRateTypePane.isSpeed())
    {
      return multipleIngredients ? enums.therapyDoseTypeEnum.RATE_VOLUME_SUM : enums.therapyDoseTypeEnum.RATE_QUANTITY;
    }
    else
    {
      return multipleIngredients ? enums.therapyDoseTypeEnum.VOLUME_SUM : enums.therapyDoseTypeEnum.QUANTITY;
    }
  },

  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;

    var variableRate = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var bnfPercentage = null;
    for (var j = 0; j < this.medicationPanes.length; j++)
    {
      if (this.medicationPanes[j].isShowBnf())
      {
        bnfPercentage += this.medicationPanes[j].getBnfPercentage();
      }
    }

    var selectedRoutes = this.routesPane.getSelectedRoutes();

    var therapy = {                                                            // [ComplexTherapyDto.java]
      medicationOrderFormType: enums.medicationOrderFormType.COMPLEX,
      variable: variableRate,
      ingredientsList: this._getComplexTherapyIngredients(),
      routes: selectedRoutes,
      continuousInfusion: this.infusionRateTypePane.isContinuousInfusion(),
      adjustToFluidBalance: this.infusionRateTypePane.isAdjustToFluidBalance(),
      volumeSum: !this.volumeSumPane.isHidden() ? this.volumeSumPane.getVolumeSum() : null,
      volumeSumUnit: !this.volumeSumPane.isHidden() ? "ml" : null,
      additionalInstruction: this.showHeparinPane === true ? this.heparinPane.getHeparinValue() : null,
      baselineInfusion: this.infusionRateTypePane.isBaselineInfusion(),
      dosingFrequency: !this.dosingFrequencyPane.isHidden() ? this.dosingFrequencyPane.getFrequency() : null,
      dosingDaysFrequency: this.dosingFrequencyPane.getDaysFrequency(),
      daysOfWeek: this.dosingFrequencyPane.getDaysOfWeek(),
      doseType: this._getDoseType(),
      maxDailyFrequency: !tm.jquery.Utils.isEmpty(maxDailyFrequency) ? maxDailyFrequency : null,
      start: this.therapyIntervalPane.getStart(),
      end: this.therapyIntervalPane.getEnd(),
      whenNeeded: this.therapySupplyContainer != null ?
          this.therapySupplyContainer.getWhenNeeded() : this.therapyIntervalPane.getWhenNeeded(),
      startCriterion: this.therapyIntervalPane.getStartCriterion(),
      reviewReminderDays: this.therapyIntervalPane.getReviewReminderDays(),
      applicationPrecondition: this.dosingFrequencyPane.getApplicationPrecondition(),
      comment: this.commentIndicationPane.getComment(),
      clinicalIndication: this.commentIndicationPane.getIndication(),
      prescriptionSupply: this.therapySupplyContainer != null ? this.therapySupplyContainer.getSupply() : null,
      criticalWarnings: this._overridenCriticalWarnings ? this._overridenCriticalWarnings : [],
      linkName: this.linkName,
      bnfMaximumPercentage: bnfPercentage,
      linkedToAdmission: !tm.jquery.Utils.isEmpty(this.therapyToEdit) ? this.therapyToEdit.isLinkedToAdmission() : false,
      titration: this._getTitrationType()
    };

    var value = this.pastDaysOfTherapyField.getValue();
    therapy.pastDaysOfTherapy = (value && (!tm.jquery.Utils.isNumeric(value)) || value <= 0) ? null : value;

    if (variableRate)                  // [VariableComplexTherapyDto.java]
    {
      therapy.timedDoseElements = this.timedDoseElements;
      therapy.recurringContinuousInfusion = this.recurringContinuousInfusion;
    }
    else                                // [ConstantComplexTherapyDto.java]
    {
      var infusionRate = this.infusionRatePane.getInfusionRate();
      if (infusionRate.duration)
      {
        infusionRate.duration = Math.round(infusionRate.duration);
      }
      if (infusionRate == 'BOLUS')
      {
        therapy.rateString = infusionRate;
      }
      else
      {
        therapy.doseElement = infusionRate;
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

    function getTitrationValue()
    {
      return !this.infusionRateTypePane.isContinuousInfusion() ?
          (this.medicationPanes[0] ? this.medicationPanes[0].getTitrationDoseType() : null) :
          (this.infusionRateTypePane.isTitratedRate() ? this.medicationData.getTitration() : null)
    }
  },

  /**
   * @returns {string|null}
   * @private
   */
  _getTitrationType: function()
  {
    if (this.infusionRateTypePane.isContinuousInfusion())
    {
      return this.infusionRateTypePane.isTitratedRate() ? this.medicationData.getTitration() : null;
    }

    return this.medicationPanes[0] ? this.medicationPanes[0].getTitrationDoseType() : null;
  },

  clear: function()
  {
    this.linkName = null;
    this.linkedTherapy = null;
    if (this.linkTherapyButton != null)
    {
      this.linkTherapyButton.hide();
      this.unlinkTherapyButton.hide();
      this.linkIcon.hide();
    }
    this.medicationPanesContainer.removeAll(true);
    this.medicationPanes.removeAll();
    if (this.showHeparinPane === true) this.heparinPane.clear();
    this.volumeSumPane.clear();
    this.routesPane.clear();
    this.infusionRateTypePane.clear(true);
    this.infusionRatePane.clear(true);
    this.dosingFrequencyPane.clear();
    this.therapyIntervalPane.clear();
    // reset the restricted start mode as it was when constructed, since the component doesn't hold the state any more
    this.therapyIntervalPane.setRestrictedStartHourSelection(true);
    if (this.therapySupplyContainer != null) this.therapySupplyContainer.clear();
    this.pastDaysOfTherapyField.setValue(null);
    this.commentIndicationPane.clear();
    this._setRateContainersVisible(false);
    this.dosingFrequencyPane.show();
    this.dosingFrequencyLabel.show();
    this.variableRateContainer.hide();
    this.timedDoseElements.removeAll();
    this.recurringContinuousInfusion = false;
    this._setMedicationsEditable();
    this.infusionRatePane.requestFocus();
    if (!tm.jquery.Utils.isEmpty(this.sourceField)) this.sourceField.setSelections([]);
    if (this.refreshBasketFunction)
    {
      this.refreshBasketFunction();
    }
    this._hidePastDaysOfTherapy();
    this.warningsContainer.clear();
    this.changeReasonPane.clear();
    this.calculatedDosagePane.clear();
    if (this.saveToTemplateButton)
    {
      this.saveToTemplateButton.show();
    }
    this._executeOnValidContentExtensions(function(extension)
    {
      extension.clear();
    });
  },

  _setMedicationData: function(medicationData)
  {
    this.medicationData = medicationData;
    var routes = !tm.jquery.Utils.isEmpty(medicationData.routes) ? medicationData.routes : this.view.getRoutes();
    this.routesPane.setRoutes(routes, medicationData.defaultRoute);
    this.infusionRatePane.setFirstMedicationData(medicationData, true);
    this.commentIndicationPane.setMedicationData(medicationData);
    this.infusionRateTypePane.setTitratedRateSupported(!tm.jquery.Utils.isEmpty(medicationData.getTitration()));
    this._executeOnValidContentExtensions(function(extension)
    {
      extension.setMedicationData(medicationData);
    });
  },

  _handleTherapyLinkButtonDisplay: function()
  {
    if (this.linkTherapyButton != null)
    {
      var continuousInfusion = this.infusionRateTypePane.isContinuousInfusion();
      if (continuousInfusion)
      {
        if (!tm.jquery.Utils.isEmpty(this.linkName))
        {
          this.linkTherapyButton.hide();
          this.linkIcon.setHtml(this.linkName);
          this.linkIcon.show();

          var basketTherapies = this.getBasketTherapiesFunction();
          var otherTherapiesLinkedToTherapy =
              tm.views.medications.MedicationUtils.areOtherTherapiesLinkedToTherapy(this.linkName, basketTherapies);
          if (!otherTherapiesLinkedToTherapy)
          {
            this.unlinkTherapyButton.show();
          }
        }
        else
        {
          this.linkTherapyButton.show();
          this.unlinkTherapyButton.hide();
          this.linkIcon.setHtml("");
          this.linkIcon.hide();
        }
      }
      else
      {
        this.linkTherapyButton.hide();
        this.unlinkTherapyButton.hide();
        this.linkIcon.setHtml("");
        this.linkIcon.hide();
        this._unlinkTherapy();
        this.refreshBasketFunction();
      }
    }
  },

  _hidePastDaysOfTherapy: function()
  {
    this.pastDaysOfTherapySpacer.hide();
    this.pastDaysOfTherapyLabel.hide();
    this.pastDaysOfTherapyField.hide();
  },

  _showPastDaysOfTherapy: function()
  {
    this.isRendered() ? this.pastDaysOfTherapySpacer.show() : this.pastDaysOfTherapySpacer.setHidden(false);
    this.isRendered() ? this.pastDaysOfTherapyLabel.show() : this.pastDaysOfTherapyLabel.setHidden(false);
    this.isRendered() ? this.pastDaysOfTherapyField.show() : this.pastDaysOfTherapyField.setHidden(false);
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

  _applyChangeReasonPaneVisibility: function()
  {
    if (this.isChangeReasonRequired())
    {
      this.isRendered() ? this.changeReasonPane.show() : this.changeReasonPane.setHidden(false);
    }
    else
    {
      this.isRendered() ? this.changeReasonPane.hide() : this.changeReasonPane.setHidden(true);
    }
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

  _existsUniversalMedication: function()
  {
    var universalMedicationFound = false;
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      if (this.medicationPanes[i].getMedicationData() &&
          this.medicationPanes[i].getMedicationData().getMedication().isMedicationUniversal())
      {
        universalMedicationFound = true;
        break;
      }
    }
    return universalMedicationFound;
  },

  _handleInfusionTypePaneAvailability: function()
  {
    if (!this.isEditMode() || this.isCopyMode())
    {
      var utils = tm.views.medications.MedicationUtils;
      var medicationWithVolumeUnitAvailable = false;
      for (var i = 0; i < this.medicationPanes.length; i++)
      {
        var medicationData = this.medicationPanes[i].getMedicationData();
        if (utils.isUnitVolumeUnit(medicationData.getStrengthNumeratorUnit()) ||
            utils.isUnitVolumeUnit(medicationData.getStrengthDenominatorUnit()) ||
            medicationData.getMedication().isMedicationUniversal())
        {
          medicationWithVolumeUnitAvailable = true;
          break;
        }
      }

      this.infusionRateTypePane.setPreventRateTypeChange(!medicationWithVolumeUnitAvailable);
      this.infusionRateTypePane.setRateGroupTooltip(
          !medicationWithVolumeUnitAvailable ?
              this.getView().getDictionary("ingredient.with.volume.required") :
              null);
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Boolean} [repaint=false]
   * @param {Array<*>} warnings
   * @param {Boolean} [warningsLoaded=false]
   */
  setMedicationData: function(medicationData, repaint, warnings, warningsLoaded)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    this._setMedicationData(medicationData);
    var medicationEditable = this.isEditMode() && !medicationData.getMedication().isMedicationUniversal();
    var sameGenericOnly = self.isEditMode() && !medicationData.getMedication().isMedicationUniversal();
    this._addMedicationPane(medicationData, false, medicationEditable, sameGenericOnly, true, true);
    this._handleInfusionTypePaneAvailability();
    this._showHideVolumeSum();
    this.infusionRatePane.setFormulaVisible();
    this.validationForm.reset();
    this._handleVarioEnabling();
    if (this.addToBasketButton)
    {
      this.addToBasketButton.setEnabled(true);
    }
    if ((tm.jquery.Utils.isEmpty(warnings) || warnings.length == 0) && warningsLoaded === false)
    {
      self.setWarningsMessage(true, this.view.getDictionary('loading.warnings'))
    }
    else if (!tm.jquery.Utils.isEmpty(warnings))
    {
      self.setWarnings(warnings);
    }

    if (repaint && this.medicationPanesContainer.isRendered())
    {
      this.medicationPanesContainer.repaint();
      setTimeout(function()
      {
        self.medicationPanes[0].requestFocusToDose();
      }, 0);
    }

    if (medicationData.antibiotic && !(this.visibilityContext &&
        this.visibilityContext.hasOwnProperty("pastDaysOfTherapyVisible") &&
        this.visibilityContext.pastDaysOfTherapyVisible === false))
    {
      if (!this.isEditMode() || this.isCopyMode())
      {
        this._showPastDaysOfTherapy();
      }
    }
  },

  validateAndConfirmOrder: function()
  {
    this._setupValidation();
    this.validationForm.submit();
  },

  /**
   * This method should not execute any Ajax requests and change the UI values when the request returns,
   * since it has to be safe to execute the render right after it!
   *
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<app.views.medications.common.dto.MedicationData>} medicationData
   */
  setMedicationDataByTherapy: function(therapy, medicationData)
  {
    this.setMedicationData(medicationData[0], false);

    for (var i = 1; i < therapy.getIngredientsList().length; i++)
    {
      var infusionIngredient = therapy.getIngredientsList()[i];
      var ingredientMedicationData =
          tm.views.medications.MedicationUtils.findInArray(medicationData, function(currentElement)
          {
            return currentElement.getMedication().getId() === infusionIngredient.medication.getId();
          });

      if (!ingredientMedicationData && !infusionIngredient.medication.isMedicationUniversal())
      {
        continue;
      } // skip if not found (anymore?)

      var medicationEditable = !this.isEditMode() && !infusionIngredient.medication.isMedicationUniversal();
      var sameGenericOnly = this.isEditMode() && !therapy.continuousInfusion &&
          !infusionIngredient.medication.isMedicationUniversal();
      var medicationPane = this._addMedicationPane(
          ingredientMedicationData,
          true,
          medicationEditable,
          sameGenericOnly,
          true,
          false);

      if (!infusionIngredient.medication.isMedicationUniversal())
      {
        medicationPane.setDose(infusionIngredient.quantity, infusionIngredient.quantityDenominator, true);
      }
      else
      {
        medicationPane.setUniversalMedicationAndDose(
            tm.views.medications.MedicationUtils.getMedicationDataFromComplexTherapy(therapy, i),
            infusionIngredient.quantity,
            infusionIngredient.quantityDenominator,
            true);
      }
    }

    if (this.medicationPanesContainer.isRendered())
    {
      this.medicationPanesContainer.repaint();
    }
  },

  /**
   * This method can load data via an Ajax call and thus has to be called only when you are certain the
   * whole container was already rendered/repainted. Otherwise changes will not be correctly propagated to the DOM!
   *
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} [setTherapyStart=false]
   * @param {Boolean} [therapyModifiedInThePast=false]
   */
  setComplexTherapy: function(therapy, setTherapyStart, therapyModifiedInThePast)
  {
    var enums = app.views.medications.TherapyEnums;
    var view = this.getView();

    this.therapyToEdit = therapy;

    var firstMedicationPane = this.medicationPanes[0];
    firstMedicationPane.setDose(therapy.ingredientsList[0].quantity, therapy.ingredientsList[0].quantityDenominator, true);

    if (therapy.isTitrationDoseType() && !therapy.isContinuousInfusion())
    {
      firstMedicationPane.setTitrationDoseType(therapy.getTitration(), true);
      this.infusionRateTypePane.setTitratedDoseMode(true, true);
      this.therapyIntervalPane.setByDoctorsOrderButtonEnabled(false);
    }

    this._calculateVolumeSum();
    this._showHideVolumeSum();

    if (this.showHeparinPane === true)
    {
      this.heparinPane.setHeparinValue(therapy.additionalInstruction);
    }

    this._handleFirstMedicationPaneDoseVisibility();
    this.infusionRatePane.setFormulaVisible();

    this.routesPane.setSelectedRoute(therapy.getRoutes());

      if (therapy.isContinuousInfusion())
      {
        this.infusionRateTypePane.markAsContinuousInfusion(
            therapy.isBaselineInfusion(),
            therapy.isTitrationDoseType(),
            therapy.isAdjustToFluidBalance(),
            false,
            true);
      }
      this._continuousInfusionChanged(therapy.isContinuousInfusion(), false, false);

    if (therapy.isAdjustToFluidBalance() || therapy.isTitrationDoseType())
    {
      this._setRateContainersVisible(false);
    }

    this.dosingFrequencyPane.setDosingFrequencyAndPattern(therapy.dosingFrequency, therapy.doseTimes, true);
    this.dosingFrequencyPane.setDaysOfWeek(therapy.daysOfWeek);
    this.dosingFrequencyPane.setDaysFrequency(therapy.dosingDaysFrequency);
    this.dosingFrequencyPane.setApplicationPrecondition(therapy.applicationPrecondition);
    if (this.therapySupplyContainer != null)
    {
      this.therapySupplyContainer.setWhenNeeded(therapy.whenNeeded);
    }
    this.therapyIntervalPane.setWhenNeeded(therapy.whenNeeded);
    this.therapyIntervalPane.setStartCriterion(therapy.startCriterion);
    this.therapyIntervalPane.setReviewReminderDays(therapy.reviewReminderDays);
    this.therapyIntervalPane.setMaxDailyFrequency(therapy.maxDailyFrequency);
    this.commentIndicationPane.setComment(therapy.comment);
    this.commentIndicationPane.setIndication(therapy.clinicalIndication);
    if (this.therapySupplyContainer != null)
    {
      this.therapySupplyContainer.setSupply(therapy.prescriptionSupply);
    }
    if (!this.isCopyMode())
    {
      this.pastDaysOfTherapyField.setValue(therapy.pastDaysOfTherapy);
    }
    this.linkName = therapy.linkName;
    this._handleTherapyLinkButtonDisplay();

      if (therapy.variable)
      {
        if (!therapy.continuousInfusion)
        {
          this.infusionRateTypePane.markAsSpeed();
          this._setRateContainersVisible(true);
        }
        this.infusionRatePane.setInfusionRate(null);
        this.infusionRatePane.hide();
        this.dosingFrequencyPane.showDaysOnly();
        this.timedDoseElements.push.apply(this.timedDoseElements, therapy.timedDoseElements);
        this.recurringContinuousInfusion = therapy.recurringContinuousInfusion;
        this._showVariableRateDisplayValue();
        if (this.recurringContinuousInfusion)
        {
          var nextAdministrationTimestamp =
              tm.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForVario(this.timedDoseElements);
          this.therapyIntervalPane.setStart(nextAdministrationTimestamp);
          this.therapyIntervalPane.setStartHourEnabled(true);
        }
        else if (therapy.isContinuousInfusion())
        {
          this._setAndDisableStartHourForVario();
        }
        else
        {
          this._handleDosingFrequencyChange();
        }
      }
      else
      {
        if (therapy.doseElement && therapy.doseElement.rate)
        {
          this._setRateContainersVisible(true);
        }
        if (therapy.doseElement && therapy.doseElement.rateFormulaUnit)
        {
          this.infusionRatePane.setFormulaUnit(therapy.doseElement.rateFormulaUnit);
        }
        var rate = therapy.doseElement && therapy.doseElement.rate ? therapy.doseElement.rate : therapy.rateString;
        this._infusionRateTypePaneFunction(rate, true);
        this.infusionRatePane.setDurationVisible(!therapy.isContinuousInfusion());
        this.infusionRatePane.refreshRate(true);
      }

      // Set the start if declared (usually meaning the therapy hasn't started yet) and we're not copying,
      // otherwise calculate a new start and possibly end (which should always happen when copying a therapy).
      if (setTherapyStart && !this.isCopyMode())
      {
        this.therapyIntervalPane.setStartOptionsFromPattern();
        this.therapyIntervalPane.setStart(
            tm.jquery.Utils.isDate(therapy.getStart()) ? new Date(therapy.getStart().getTime()) : null);
        this.therapyIntervalPane.setEnd(
            tm.jquery.Utils.isDate(therapy.getEnd()) ? new Date(therapy.getEnd().getTime()) : null);
      }
      else
      {
        // Set the end regardless (unless we're copying), since it might get recalculated (1ex) or cleared
        // (no end date support). If it's not set, the therapyIntervalPane's calculateEnd,
        // called by _calculateStartAndEnd, will clear it, which is not what we want when editing a prescribed therapy.
        if (!this.isCopyMode())
        {
          this.therapyIntervalPane.setEnd(
              tm.jquery.Utils.isDate(therapy.getEnd()) ? new Date(therapy.getEnd().getTime()) : null);
        }
        this._calculateStartAndEnd(this._buildTherapy());
      }

    this._setMedicationsEditable();
    this._calculateDosing();

    if (!tm.jquery.Utils.isEmpty(therapy.bnfMaximumPercentage))
    {
      for (var j = 0; j < this.medicationPanes.length; j++)
      {
        if (this.medicationPanes[j].isShowBnf())
        {
          this.medicationPanes[j].setBnfPercentage(therapy.bnfMaximumPercentage);
          this._handleBnfWarningChange(therapy.bnfMaximumPercentage, this.medicationPanes[j].medicationData);
          break;
        }
      }
    }

    if (this.isEditMode() && !this.isCopyMode() && (this.therapyAlreadyStarted || therapyModifiedInThePast))
    {
      this.therapyNextAdministrationLabelPane.setOldTherapyId(
          therapy.getCompositionUid(),
          therapy.getEhrOrderName(),
          therapy.isContinuousInfusion());
    }
    this._overridenCriticalWarnings = therapy.criticalWarnings;

    this._executeOnValidContentExtensions(function(extension)
    {
      if (extension.setComponentsFromTherapy && tm.jquery.Utils.isFunction(extension.setComponentsFromTherapy))
      {
        extension.setComponentsFromTherapy(therapy);
      }
    });

    setTimeout(function()
    {
      firstMedicationPane.requestFocusToDose();
    }, 0);
  },

  getComplexTherapy: function()
  {
    return this._buildTherapy();
  },

  setWarnings: function(warnings)
  {
    this.warningsContainer.setWarnings(warnings);
  },

  setWarningsMessage: function(loading, text)
  {
    this.warningsContainer.setWarningsMessage(loading, text);
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData]} *medicationData
   * @param {Boolean} *removeAllBnfWarnings
   */
  removeBnfWarning: function(medicationData, removeAllBnfWarnings)
  {
    this.warningsContainer.removeBnfWarning(medicationData ? medicationData.getMedication().getId() : null,
        removeAllBnfWarnings ? removeAllBnfWarnings : !this._isBnfMaximumDefined(medicationData));
  },

  setChangeReasonRequired: function(value)
  {
    this.changeReasonRequired = value;
    this._applyChangeReasonPaneVisibility();
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

  isChangeReasonRequired: function()
  {
    return this.changeReasonRequired === true;
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
        }
        , 150);
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {Therapy|null}
   */
  getLinkedTherapy: function()
  {
    return this.linkedTherapy;
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
  isCopyMode: function()
  {
    return this.copyMode === true;
  }
});
