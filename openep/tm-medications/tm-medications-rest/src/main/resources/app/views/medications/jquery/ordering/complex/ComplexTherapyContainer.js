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
  copyTherapy: false,
  changeCardEvent: null, //optional
  saveToTemplateFunction: null, //optional
  linkTherapyFunction: null,
  getTherapyStartNotBeforeDateFunction: null, //optional
  confirmTherapyEvent: null,
  saveDateTimePaneEvent: null,
  closeDialogFunction: null, //optional
  isPastMode: false,
  presetDate: null, //optional
  getTemplatesFunction: null,  //optional
  /** privates */
  validationForm: null,
  timedDoseElements: null,
  medicationData: null,
  medicationPanes: null,
  valueSettingInProgress: null,
  therapyLink: null,
  /** privates: components */
  medicationPanesContainer: null,
  heparinPane: null,
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
  commentField: null,
  indicationField: null,
  calculatedDosagePane: null,
  addToBasketButton: null,
  templatesButton: null,
  simpleTherapyButton: null,
  saveToTemplateButton: null,
  linkTherapyButton: null,
  pastDaysOfTherapySpacer: null,
  pastDaysOfTherapyLabel: null,
  pastDaysOfTherapyField: null,
  maxFrequencyTitleLabel: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.timedDoseElements = [];
    this.medicationPanes = [];
    this.therapyLink = null;
    this.valueSettingInProgress = false;
    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    this.medicationPanesContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "stretch"), scrollable: 'visible'});
    this.heparinPane = new app.views.medications.ordering.HeparinPane({view: this.view});
    this.volumeSumPane = new app.views.medications.ordering.VolumeSumPane({
      view: this.view,
      width: 504,
      adjustVolumesEvent: function()
      {
        self._adjustVolumes();
      }
    });
    this.routesPane = new app.views.medications.ordering.RoutesPane({
      view: this.view,
      height: 30
    });
    this.infusionRateTypePane = new app.views.medications.ordering.InfusionRateTypePane({
      view: this.view,
      continuousInfusionChangedFunction: function(continuousInfusion, clearValues)
      {
        self._setMedicationsEditable();
        self.variableRateContainer.hide();
        self.timedDoseElements.removeAll();
        self._continuousInfusionChanged(continuousInfusion, clearValues)
      },
      adjustToFluidBalanceChangedFunction: function(adjustToFluidBalance)
      {
        self._setRateContainersVisible(!adjustToFluidBalance);
        self.infusionRatePane.clearInfusionValues();
      },
      bolusChangedFunction: function()
      {
        self._handleSpeedOrBolusChanged(false);
      },
      speedChangedFunction: function(isSpeed)
      {
        if (isSpeed)
        {
          self._handleSpeedOrBolusChanged(true);
          self.infusionRatePane.setDurationVisible(true);
        }
        else
        {
          self._handleSpeedOrBolusChanged(false);
        }
      }
    });
    this.rateLabelContainer = new tm.jquery.Container({width: 160, layout: tm.jquery.HFlexboxLayout.create("start", "start")});
    var clearRatesContextMenu = appFactory.createContextMenu();
    var clearRatesMenuItem = new tm.jquery.MenuItem({
          text: this.view.getDictionary('empty.form'),
          handler: function()
          {
            if (!self.infusionRatePane.isHidden())
            {
              self.infusionRatePane.clearInfusionValues();
            }
            else
            {
              self.therapyIntervalPane.setStartHourEnabled(true);
              self._continuousInfusionChanged(self.infusionRateTypePane.isContinuousInfusion(), true);
              self.dosingFrequencyPane.showAllFields();
              self.infusionRatePane.show();
              self.infusionRatePane.clear();
              self.variableRateContainer.hide();
              self.timedDoseElements.removeAll();
              self._setMedicationsEditable();
              self.infusionRatePane.requestFocus();
            }
            self.varioButton.show();
            self.rateLabelContainer.show();
          }}
    );
    clearRatesContextMenu.addMenuItem(clearRatesMenuItem);
    this.rateLabelContainer.setContextMenu(clearRatesContextMenu);
    this.rateLabelContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      component.getContextMenu().show(elementEvent.pageX, elementEvent.pageY);
    });
    this.rateLabelVSpacer = this._createVerticalSpacer(2);
    this.infusionRatePane = new app.views.medications.ordering.InfusionRatePane({
      view: this.view,
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
        return self._getInfusionIngredients();
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
    this.variableRateContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "start"), margin: '5 0 0 10'});

    this.varioButton = new tm.jquery.Button({
      text: 'Variable',
      handler: function()
      {
        self._openVariableRateEditPane();
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
        return self.infusionRateTypePane.isContinuousInfusion() ? "WITHOUT_FREQUENCY" : self.dosingFrequencyPane.getFrequencyType();
      },
      getDosingFrequencyModeFunction: function()
      {
        return self.dosingFrequencyPane.getFrequencyMode();
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

    if (!this.editMode)
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
        flex: 1,
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent('TEMPLATES');
          }
        }});
      this.simpleTherapyButton = new tm.jquery.Button({
        text: this.view.getDictionary('simple1'),
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
        //text: this.view.getDictionary('add.to.template'),
        text: "Link Medication",
        style: "width: 150px, text-align:left;",
        type: "link",
        handler: function()
        {
          self.linkTherapyFunction(function(startAfterTherapy, therapyLink)
          {
            if (!tm.jquery.Utils.isEmpty(startAfterTherapy) && startAfterTherapy.end)
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

    this._setRateContainersVisible(false);
  },

  _buildGui: function()
  {
    this.add(this.medicationPanesContainer);

    var mainContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "stretch"), margin: '0 0 0 20'});
    this.add(mainContainer);

    var heparinRowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "center"), height: 30});
    heparinRowContainer.add(this.heparinPane);
    heparinRowContainer.add(this.volumeSumPane);
    mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', 'Heparin'));
    mainContainer.add(heparinRowContainer);
    mainContainer.add(this._createVerticalSpacer(2));

    mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('route')));
    var routesRowPane = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5), scrollable: 'visible'});
    routesRowPane.add(this.routesPane);
    routesRowPane.add(this.infusionRateTypePane);
    mainContainer.add(routesRowPane);
    mainContainer.add(this._createVerticalSpacer(2));

    this.rateLabelContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('infusion.rate')));
    this.rateLabelContainer.add(new tm.jquery.Container({cls: 'menu-icon', width: 16, height: 16, margin: '4 0 0 5'}));
    mainContainer.add(this.rateLabelContainer);
    mainContainer.add(this.rateLabelVSpacer);
    var infusionRateContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start"), width: 678,  scrollable: "visible"});
    infusionRateContainer.add(this.infusionRatePane);
    infusionRateContainer.add(this.varioButton);
    infusionRateContainer.add(this.variableRateContainer);

    mainContainer.add(infusionRateContainer);
    mainContainer.add(this._createVerticalSpacer(2));

    this.dosingFrequencyLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval'));
    mainContainer.add(this.dosingFrequencyLabel);
    mainContainer.add(this.dosingFrequencyPane);
    mainContainer.add(this._createVerticalSpacer(2));

    var therapyIntervalLabelsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start")});
    therapyIntervalLabelsContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('start'), '5 210 0 0'));
    therapyIntervalLabelsContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('therapy.duration'), '5 290 0 0'));
    therapyIntervalLabelsContainer.add(this.maxFrequencyTitleLabel);
    mainContainer.add(therapyIntervalLabelsContainer);
    mainContainer.add(this.therapyIntervalPane);
    mainContainer.add(this._createVerticalSpacer(2));

    mainContainer.add(this.pastDaysOfTherapySpacer);
    mainContainer.add(this.pastDaysOfTherapyLabel);
    mainContainer.add(this.pastDaysOfTherapyField);

    var commentLabelsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5)});
    commentLabelsContainer.add(new tm.jquery.Container({cls: 'TextLabel', html: this.view.getDictionary('commentary'), width: 328, padding: "5 0 0 0"}));
    commentLabelsContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('indication')));
    mainContainer.add(commentLabelsContainer);
    var commentsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5)});
    commentsContainer.add(this.commentField);
    commentsContainer.add(this.indicationField);
    mainContainer.add(commentsContainer);
    mainContainer.add(this._createVerticalSpacer(2));
    mainContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('calculated.dosing')));
    mainContainer.add(this.calculatedDosagePane);
    mainContainer.add(new tm.jquery.Spacer({type: 'vertical', size: 12}));

    if (!this.editMode)
    {
      this.add(new tm.jquery.Container({style: 'border-top: 1px solid #d6d6d6'}));
      this.add(this._createVerticalSpacer(7));

      var navigationContainer = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("end", "start", 20),
        margin: '0 25 0 20'
      });
      navigationContainer.add(this.templatesButton);
      navigationContainer.add(this.simpleTherapyButton);
      navigationContainer.add(this.saveToTemplateButton);
      navigationContainer.add(this.linkTherapyButton);
      var addToBasketContainer = new tm.jquery.Container({flex: 1, layout: tm.jquery.HFlexboxLayout.create("end", "start")});
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

  _addMedicationPane: function(medicationData, addSpacer, searchEnabled, addRemoveEnabled, mainMedication, typeaheadAdvancedMode)
  {
    var self = this;
    var medicationPane = new app.views.medications.ordering.ComplexTherapyMedicationPane({
      view: this.view,
      typeaheadAdvancedMode: typeaheadAdvancedMode,
      addSpacer: addSpacer,
      searchEnabled: searchEnabled,
      addRemoveEnabled: addRemoveEnabled,
      medicationData: medicationData,
      templates: this.getTemplatesFunction ? this.getTemplatesFunction() : null,
      addElementEvent: function(medicationPane)
      {
        self._addMedicationPane(null, true, true, true, false, false);
        self.medicationPanesContainer.repaint();
        self._showHideVolumeSum();
        self._handleFirstMedicationPaneDoseVisibility();

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
          self.medicationPanesContainer.repaint();
          self._showHideVolumeSum();
          self._handleFirstMedicationPaneDoseVisibility();
          self.infusionRatePane.setFormulaVisible();
          self._calculateVolumeSum();
          if (!self.infusionRatePane.isHidden())
          {
            self.infusionRatePane.calculateInfusionValues();
          }
        }
        else
        {
          if (!self.editMode && self.changeCardEvent)
          {
            self.changeCardEvent('TEMPLATES');
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
        self._calculateDosing();
      },
      focusLostEvent: function(medicationPane)
      {
        self._focusToNextMedicationPane(medicationPane);
      },
      medicationChangedEvent: function(medicationData)
      {
        self.infusionRatePane.setFormulaVisible();
        self.infusionRatePane.setFormula(null);
        if (mainMedication)
        {
          self._setMedicationData(medicationData);
        }
        self._handleFirstMedicationPaneDoseVisibility();
      },
      closeDialogFunction: function()
      {
        if (self.closeDialogFunction)
        {
          self.closeDialogFunction();
        }
      }
    });
    var lastMedicationPaneIndex = this.medicationPanes.length - 1;
    if (lastMedicationPaneIndex >= 0)
    {
      this.medicationPanes[lastMedicationPaneIndex].setAddRemoveButtonsVisible(false);
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

    var variableDosePane = new app.views.medications.ordering.ComplexVariableRatePane({
      view: self.view,
      startProcessOnEnter: true,
      height: 180,
      padding: 10,
      medicationData: this.medicationData,
      timedDoseElements: this.timedDoseElements,
      infusionIngredients: this._getInfusionIngredients(),
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
          if (resultData)
          {
            self.infusionRatePane.hide();
            self.dosingFrequencyPane.showDaysOnly();
            self.timedDoseElements = resultData.value;
            self._showVariableRateDisplayValue();
            self._setAndDisableStartHourForVario();
            self._setMedicationsEditable();
          }
        },
        500,
        320
    );
    var removeVarioButton = variableDoseEditDialog.getBody().footer.cancelButton;
    removeVarioButton.setText(this.view.getDictionary('remove.vario'));
    removeVarioButton.setType("link");
    var removeVarioButtonHandler = removeVarioButton.getHandler();
    removeVarioButton.setHandler(function()
    {
      self.therapyIntervalPane.setStartHourEnabled(true);
      self.infusionRatePane.show();
      self.infusionRatePane.clear();
      self._continuousInfusionChanged(self.infusionRateTypePane.isContinuousInfusion(), true);
      self.dosingFrequencyPane.showAllFields();
      self.dosingFrequencyLabel.show();
      self.variableRateContainer.hide();
      self.timedDoseElements.removeAll();
      self._setMedicationsEditable();
      self.infusionRatePane.requestFocus();
      removeVarioButtonHandler();
    });

    variableDoseEditDialog.getBody().footer.setRightButtons([removeVarioButton, variableDoseEditDialog.getBody().footer.confirmButton]);
    variableDoseEditDialog.show();
  },

  _showVariableRateDisplayValue: function()
  {
    this.variableRateContainer.removeAll();
    var solutionsOnly = this._areAllIngredientsSolutions();
    for (var i = 0; i < this.timedDoseElements.length; i++)
    {
      var rowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 10)});
      var timedDoseElement = this.timedDoseElements[i];
      var doseTime = timedDoseElement.doseTime;
      var doseElement = timedDoseElement.doseElement;

      var startTimeDisplayValue = tm.views.medications.MedicationTimingUtils.hourMinuteToString(doseTime.hour, doseTime.minute) + ' - ';
      rowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', startTimeDisplayValue, '1 0 0 0'));

      if (doseElement.duration)
      {
        var endTime = new Date();
        endTime.setHours(doseTime.hour);
        endTime.setMinutes(doseTime.minute + doseElement.duration);
        var endTimeDisplayValue = tm.views.medications.MedicationTimingUtils.hourMinuteToString(endTime.getHours(), endTime.getMinutes()) + '  ';
        rowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', endTimeDisplayValue, '1 0 0 0'));
      }
      else
      {
        rowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', '...', '1 20 0 0'));
      }
      var doseDisplayValue = tm.views.medications.MedicationUtils.doubleToString(doseElement.rate, 'n2') + ' ' + doseElement.rateUnit;
      if (!solutionsOnly && doseElement.rateFormula)
      {
        doseDisplayValue += ' (' +
            tm.views.medications.MedicationUtils.doubleToString(doseElement.rateFormula, 'n2') + ' ' +
            doseElement.rateFormulaUnit + ')';
      }

      rowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', doseDisplayValue, 0));
      this.variableRateContainer.add(rowContainer);
    }

    this.variableRateContainer.show();
    this.variableRateContainer.repaint();
  },

  _setMedicationsEditable: function()
  {
    var vario = this.timedDoseElements.length > 0;
    var copyMode = this.copyTherapy;
    var continuousInfusion = this.infusionRateTypePane.isContinuousInfusion();
    var enableAddingAndRemoving = !vario && (!this.editMode || copyMode || continuousInfusion);
    var doseEditable = !vario;

    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var isLastRow = i == this.medicationPanes.length - 1;
      var showAddRemoveButtons = enableAddingAndRemoving && isLastRow;
      var medicationEditable = !vario && (this.editMode || i > 0);
      var medicationEditableSameGenericOnly = !vario && this.editMode && !copyMode;
      if (continuousInfusion)
      {
        medicationEditableSameGenericOnly = medicationEditableSameGenericOnly && i == 0;
      }
      this.medicationPanes[i].setPaneEditable(
          showAddRemoveButtons, medicationEditable, medicationEditableSameGenericOnly, doseEditable, !this.valueSettingInProgress);
    }
  },

  _setAndDisableStartHourForVario: function()
  {
    var firstTimedDoseElement = this.timedDoseElements[0];
    var start = new Date();
    start.setHours(firstTimedDoseElement.doseTime.hour);
    start.setMinutes(firstTimedDoseElement.doseTime.minute);
    this.therapyIntervalPane.setStart(start);
    this.therapyIntervalPane.setStartHourEnabled(false);
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
    if (!this.infusionRatePane.isHidden() && this.infusionRateTypePane.isContinuousInfusion())
    {
      this._addValidations(this.infusionRatePane.getInfusionRatePaneValidations());
    }
    if (!this.dosingFrequencyPane.isHidden())
    {
      this._addValidations(this.dosingFrequencyPane.getDosingFrequencyPaneValidations());
    }
    if (this.editMode && !this.copyTherapy)
    {
      var therapyStartNotBefore = this.getTherapyStartNotBeforeDateFunction ? this.getTherapyStartNotBeforeDateFunction() : null;
      this._addValidations(this.therapyIntervalPane.getTherapyIntervalPaneValidations(therapyStartNotBefore));
    }
    else
    {
      this._addValidations(this.therapyIntervalPane.getTherapyIntervalPaneValidations());
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

  _showHideVolumeSum: function()
  {
    if (this.medicationPanes.length > 1)
    {
      this.volumeSumPane.show();
    }
    else
    {
      this.volumeSumPane.hide();
    }
  },

  _handleDosingFrequencyChange: function()
  {
    this.therapyIntervalPane.calculateStart(this.dosingFrequencyPane.getDaysOfWeek());
    this.therapyIntervalPane.calculateEnd();
    this._calculateDosing();
  },

  _adjustVolumes: function()
  {
    var medsAndSuppsVolumeSum = this._getTherapyVolumeSumOfTypes(['MEDICATION', 'SUPPLEMENT']);
    var solutionsVolumeSum = this._getTherapyVolumeSumOfTypes(['SOLUTION']);
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var solutionData = this.medicationPanes[i].getMedicationData();
      if (solutionData)
      {
        var medicationType = solutionData.medication.medicationType;
        if (medicationType == 'SOLUTION')
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

  _continuousInfusionChanged: function(continuousInfusion, clearInfusionValues)
  {
    this.infusionRatePane.presentContinuousInfusionFields(continuousInfusion);
    if (continuousInfusion)
    {
      this._setRateContainersVisible(true);
      this.dosingFrequencyLabel.hide();
      this.dosingFrequencyPane.hide();
    }
    else
    {
      this._setRateContainersVisible(false);
      this.dosingFrequencyPane.showAllFields();
      this.dosingFrequencyLabel.show();
      this.dosingFrequencyPane.show();
    }
    if (clearInfusionValues)
    {
      this.infusionRatePane.clearInfusionValues();
      this.therapyIntervalPane.setStartHourEnabled(true);
    }
    this.therapyIntervalPane.calculateStart(this.dosingFrequencyPane.getDaysOfWeek());
    this._calculateDosing();
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
      this.medicationPanes[0].setDoseVisible(true);
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
      if (infusionIngredient && infusionIngredient.medication.medicationType != 'SOLUTION')
      {
        solutionsOnly = false;
        break;
      }
    }
    return solutionsOnly;
  },

  _calculateDosing: function()
  {
    var ingredients = this._getInfusionIngredients();
    if (ingredients.length == 1)
    {
      var quantity;
      var quantityUnit;
      var weightInKg;
      var heightInCm;
      if (this.infusionRateTypePane.isContinuousInfusion())
      {
        quantityUnit = ingredients[0].quantityUnit;
        var infusionRateFormulaInMgPerHour = this.infusionRatePane.getInfusionRateFormulaPerHour(quantityUnit);
        weightInKg = this.view.getReferenceWeight();
        heightInCm = this.view.getPatientData().heightInCm;
        quantity = infusionRateFormulaInMgPerHour != null ? infusionRateFormulaInMgPerHour * weightInKg : null;
        this.calculatedDosagePane.calculate(quantity, quantityUnit, null, weightInKg, heightInCm, true);
      }
      else
      {
        quantity = ingredients[0].quantity;
        quantityUnit = ingredients[0].quantityUnit;
        var timesPerDay = this.dosingFrequencyPane.getFrequencyTimesPerDay();
        weightInKg = this.view.getReferenceWeight();
        heightInCm = this.view.getPatientData().heightInCm;
        this.calculatedDosagePane.calculate(quantity, quantityUnit, timesPerDay, weightInKg, heightInCm, false);
      }
    }
    else
    {
      this.calculatedDosagePane.clear();
    }
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

  _infusionRateTypePaneFunction: function(rate)
  {
    if (rate)
    {
      if (rate == 'BOLUS')
      {
        this.infusionRateTypePane.setBolus(true);
      }
      else
      {
        if (!this.infusionRateTypePane.isContinuousInfusion())
        {
          this.infusionRateTypePane.setSpeed(true);
        }
        this.infusionRatePane.setRate(rate);
      }
    }
  },

  _getInfusionIngredients: function()
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
    var confirmSuccess = this.confirmTherapyEvent(therapy);
    if (confirmSuccess == false)
    {
      this.addToBasketButton.setEnabled(true);
    }
  },

  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;

    var variableRate = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var therapy = {
      medicationOrderFormType: enums.medicationOrderFormType.COMPLEX,
      variable: variableRate,
      ingredientsList: this._getInfusionIngredients(),
      route: this.routesPane.getSelectedRoute(),
      continuousInfusion: this.infusionRateTypePane.isContinuousInfusion(),
      adjustToFluidBalance: this.infusionRateTypePane.isAdjustToFluidBalance(),
      volumeSum: !this.volumeSumPane.isHidden() ? this.volumeSumPane.getVolumeSum() : null,
      volumeSumUnit: !this.volumeSumPane.isHidden() ? "ml" : null,
      additionalInstruction: this.heparinPane.getHeparinValue(),
      baselineInfusion: this.infusionRateTypePane.isBaselineInfusion(),
      dosingFrequency: !this.dosingFrequencyPane.isHidden() ? this.dosingFrequencyPane.getFrequency() : null,
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

    if (variableRate)
    {
      therapy.timedDoseElements = this.timedDoseElements;
    }
    else
    {
      var infusionRate = this.infusionRatePane.getInfusionRate();
      if (infusionRate == 'BOLUS')
      {
        therapy.rateString = infusionRate;
      }
      else
      {
        therapy.doseElement = infusionRate;
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

  _clear: function()
  {
    this.therapyLink = null;
    if (this.linkTherapyButton)
    {
      this.linkTherapyButton.setCls("");
    }
    this.medicationPanesContainer.removeAll(true);
    this.medicationPanes.removeAll();
    this.heparinPane.clear();
    this.volumeSumPane.clear();
    this.routesPane.setSelectedRoute(null);
    this._continuousInfusionChanged(false, true);
    this.infusionRateTypePane.clear();
    this.infusionRatePane.clear();
    this.dosingFrequencyPane.clear();
    this.therapyIntervalPane.clear();
    this.pastDaysOfTherapyField.setValue(null);
    this.commentField.setValue(null);
    this.indicationField.setValue(null);
    this._setRateContainersVisible(false);
    this.dosingFrequencyPane.show();
    this.dosingFrequencyLabel.show();
    this.variableRateContainer.hide();
    this.timedDoseElements.removeAll();
    this._setMedicationsEditable();
    this.infusionRatePane.requestFocus();
    this.therapyIntervalPane.setStartHourEnabled(true);
  },

  /** public methods */
  setMedicationData: function(medicationData, repaint)
  {
    var self = this;
    this._clear();
    this._setMedicationData(medicationData);
    this._addMedicationPane(medicationData, false, this.editMode, true, true, self.editMode);
    if (repaint)
    {
      this.medicationPanesContainer.repaint();
    }
    this._showHideVolumeSum();
    this.infusionRatePane.setFormulaVisible();
    this.validationForm.reset();
    this._handleVarioEnabling();
    if (this.addToBasketButton)
    {
      this.addToBasketButton.setEnabled(true);
    }

    setTimeout(function()
    {
      self.medicationPanes[0].requestFocusToDose();
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

    this.pastDaysOfTherapyField.setEnabled(self.editMode && self.copyTherapy || !self.editMode);
  },

  _setMedicationData: function(medicationData)
  {
    this.medicationData = medicationData;
    this.routesPane.setRoutes(medicationData.routes, medicationData.defaultRoute);
    this.infusionRatePane.setFirstMedicationData(medicationData);
  },

  validateAndConfirmOrder: function()
  {
    this._setupValidation();
    this.validationForm.submit();
  },

  setComplexTherapy: function(therapy, setTherapyInterval)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    this.valueSettingInProgress = true;
    this._readMedicationData(therapy.ingredientsList[0].medication.id, function(medicationData)
    {
      if (medicationData)
      {
        self.setMedicationData(medicationData, false);
        var firstMedicationPane = self.medicationPanes[0];
        firstMedicationPane.setDose(therapy.ingredientsList[0].quantity, therapy.ingredientsList[0].volume);

        var loadedMedicationsDataCount = {count: 1};
        for (var i = 1; i < therapy.ingredientsList.length; i++)
        {
          var infusionIngredient = therapy.ingredientsList[i];
          var typeaheadAdvancedMode = self.editMode && !therapy.continuousInfusion;
          var medicationPane = self._addMedicationPane(null, true, true, true, false, typeaheadAdvancedMode);

          medicationPane.setMedicationAndDose(infusionIngredient.medication, infusionIngredient.quantity, infusionIngredient.volume, function()
          {
            loadedMedicationsDataCount.count = loadedMedicationsDataCount.count + 1;
          });
        }
        self._waitUntilAllMedicationsLoaded(loadedMedicationsDataCount, therapy.ingredientsList.length, function()
        {
          self._calculateVolumeSum();
          self._showHideVolumeSum();

          self.heparinPane.setHeparinValue(therapy.additionalInstruction);
          self._handleFirstMedicationPaneDoseVisibility();
          self.infusionRatePane.setFormulaVisible();
          self.routesPane.setSelectedRoute(therapy.route);
          self.infusionRateTypePane.setContinuousInfusion(therapy.continuousInfusion, therapy.baselineInfusion, false, false);
          if (therapy.adjustToFluidBalance)
          {
            self.infusionRateTypePane.setAdjustToFluidBalance(true);
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

          if (therapy.variable)
          {
            if (!therapy.continuousInfusion)
            {
              self.infusionRateTypePane.setSpeed(true);
            }
            self.infusionRatePane.setInfusionRate(null);
            self.infusionRatePane.hide();
            self.dosingFrequencyPane.showDaysOnly();
            self.timedDoseElements.push.apply(self.timedDoseElements, therapy.timedDoseElements);
            self._showVariableRateDisplayValue();
            self._setAndDisableStartHourForVario();
          }
          else
          {
            if (therapy.doseElement && therapy.doseElement.rate)
            {
              self._setRateContainersVisible(true);
            }
            if (therapy.doseElement && therapy.doseElement.rateFormulaUnit)
            {
              self.infusionRatePane.setFormulaUnit(therapy.doseElement.rateFormulaUnit);
            }
            var rate = therapy.doseElement && therapy.doseElement.rate ? therapy.doseElement.rate : therapy.rateString;
            self.infusionRatePane.setInfusionRate(rate);
            self.infusionRatePane.setDurationVisible(!therapy.continuousInfusion);
          }
          if (setTherapyInterval)
          {
            self.therapyIntervalPane.setStart(therapy.start && therapy.start.data ? therapy.start.data : therapy.start);
            self.therapyIntervalPane.setEnd(therapy.end && therapy.end.data ? therapy.end.data : therapy.end);
          }

          setTimeout(function()
          {
            self._setMedicationsEditable();
            self.medicationPanesContainer.repaint();
            self.valueSettingInProgress = false;
          }, 100);

        });
      }
      else
      {
        var message = self.view.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            self.view.getDictionary('abort.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 500, 160).show();

        if (self.closeDialogFunction)
        {
          self.closeDialogFunction();
        }
      }
    })
  }
});

