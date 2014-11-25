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

Class.define('app.views.medications.ordering.UniversalComplexTherapyContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "universal-complex-therapy-container",
  scrollable: 'both',
  margin: '0 0 0 15',

  /** configs */
  view: null,
  therapyToEdit: null,
  /** privates */
  validationForm: null,
  timedDoseElements: null,
  editingStartTimestamp: null,
  /** privates: components */
  medicationField: null,
  doseFormCombo: null,
  universalDosePane: null,
  heparinPane: null,
  routesCombo: null,
  rateLabelContainer: null,
  infusionRateTypePane: null,
  rateLabelVSpacer: null,
  infusionRatePane: null,
  variableRateContainer: null,
  varioButton: null,
  dosingFrequencyTitle: null,
  dosingFrequencyPane: null,
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
    this.setLayout(tm.jquery.VFlexboxLayout.create('start', "stretch", 0));

    if (this.therapyToEdit)
    {
      this.editingStartTimestamp = new Date();
      this.editingStartTimestamp.setSeconds(0);
      this.editingStartTimestamp.setMilliseconds(0);
    }

    this._buildComponents();
    this._buildGui();
    this._handleVarioEnabling();

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
    this.universalDosePane = new app.views.medications.ordering.UniversalDosePane({
      view: this.view,
      numeratorRequired: false,
      denominatorRequired: true,
      denominatorAlwaysVolume: true,
      denominatorChangeEvent: function()
      {
        self._handleVarioEnabling();
        self.infusionRatePane.calculateInfusionValues();
      }
    });
    this.heparinPane = new app.views.medications.ordering.HeparinPane({view: this.view});
    this.routesCombo = new tm.jquery.TypeaheadField({
      displayProvider: function(route)
      {
        return route.name;
      },
      minLength: 1,
      mode: 'advanced',
      width: 150,
      padding: "0 20 0 0",
      items: 10000
    });
    this.routesCombo.setSource(this.view.getRoutes());
    this.routesCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._handleRouteChange(self.routesCombo.getSelection());
    });

    this.infusionRateTypePane = new app.views.medications.ordering.InfusionRateTypePane({
      view: this.view,
      continuousInfusionChangedFunction: function(continuousInfusion, clearValues)
      {
        self._continuousInfusionChanged(continuousInfusion, clearValues)
      },
      adjustToFluidBalanceChangedFunction: function(adjustToFluidBalance)
      {
        self._setRateContainersVisible(!adjustToFluidBalance);
        self.infusionRatePane.clearInfusionValues();
      },
      bolusChangedFunction: function()
      {
        self.universalDosePane.show();
        self._setRateContainersVisible(false);
        self.infusionRatePane.clearFieldValues();
        self.dosingFrequencyLabel.show();
        self.dosingFrequencyPane.show();
      },
      speedChangedFunction: function(isSpeed)
      {
        if (isSpeed)
        {
          self.universalDosePane.show();
          self.infusionRatePane.clearFieldValues();
          self._setRateContainersVisible(true);
          self.infusionRatePane.setDurationVisible(true);
          self.dosingFrequencyLabel.show();
          self.dosingFrequencyPane.show();
        }
        else
        {
          self._setRateContainersVisible(false);
        }
      }
    });
    this.rateLabelVSpacer = this._createVerticalSpacer(2);
    this.rateLabelContainer = new tm.jquery.Container({width: 160, layout: tm.jquery.HFlexboxLayout.create("start", "start")});
    var contextMenu = appFactory.createContextMenu();
    var menuItem = new tm.jquery.MenuItem({
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
              self.infusionRatePane.show();
              self.infusionRatePane.clear();
              self._continuousInfusionChanged(self.infusionRateTypePane.isContinuousInfusion(), true);
              self.dosingFrequencyPane.showAllFields();
              self.dosingFrequencyLabel.show();
              self.variableRateContainer.hide();
              self.timedDoseElements.removeAll();
              self.universalDosePane.setVolumeEditable(true);
              self.infusionRatePane.requestFocus();
            }
          }}
    );
    contextMenu.addMenuItem(menuItem);
    this.rateLabelContainer.setContextMenu(contextMenu);
    this.rateLabelContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      component.getContextMenu().show(elementEvent.pageX, elementEvent.pageY);
    });

    this.infusionRatePane = new app.views.medications.ordering.InfusionRatePane({
      view: this.view,
      cls: "infusion-rate-pane",
      width: 583,
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
        return [self._getInfusionIngredient()];
      },
      getContinuousInfusionFunction: function()
      {
        return self.infusionRateTypePane.isContinuousInfusion()
      },
      getVolumeSumFunction: function()
      {
        return null;
      },
      durationChangeEvent: function()
      {
        self.therapyIntervalPane.calculateEnd();
      },
      singleIngredientVolumeCalculatedEvent: function(volume)
      {
        self.universalDosePane.setDenominator(volume);
      },
      formulaVisibleFunction: function()
      {
        return false;
      }
    });
    this.infusionRatePane.setFormulaVisible();
    this.variableRateContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("start", "stretch"), margin: '7 0 0 0'});

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
    this._setRateContainersVisible(false);
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
    var doseRowContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout(), width: 678, height: 30});
    doseRowContainer.add(new tm.jquery.Container({flex: 1}));
    doseRowContainer.add(this.universalDosePane);
    this.add(doseRowContainer);
    this.add(this._createVerticalSpacer(2));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', 'Heparin'));
    this.add(this.heparinPane);
    this.add(this._createVerticalSpacer(2));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('route')));
    var routesRowPane = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});
    routesRowPane.add(this.routesCombo);
    routesRowPane.add(this.infusionRateTypePane);
    this.add(routesRowPane);
    this.add(this._createVerticalSpacer(2));

    this.rateLabelContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('infusion.rate')));
    this.rateLabelContainer.add(new tm.jquery.Container({cls: 'menu-icon', width: 16, height: 16, margin: '4 0 0 5'}));
    this.add(this.rateLabelContainer);
    this.add(this.rateLabelVSpacer);
    var infusionRateContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start", 5), width: 678});
    infusionRateContainer.add(this.infusionRatePane);
    infusionRateContainer.add(this.varioButton);
    infusionRateContainer.add(this.variableRateContainer);
    this.add(infusionRateContainer);
    this.add(this._createVerticalSpacer(2));

    this.dosingFrequencyLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval'));
    this.add(this.dosingFrequencyLabel);
    this.add(this.dosingFrequencyPane);
    this.add(this._createVerticalSpacer(2));

    var therapyIntervalLabelsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "start")});
    therapyIntervalLabelsContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('start'), '5 210 0 0'));
    therapyIntervalLabelsContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('therapy.duration'), '5 290 0 0'));
    therapyIntervalLabelsContainer.add(this.maxFrequencyTitleLabel);
    this.add(therapyIntervalLabelsContainer);
    this.add(this.therapyIntervalPane);
    this.add(this._createVerticalSpacer(2));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('commentary')));
    this.add(this.commentField);
    this.add(new tm.jquery.Container({flex: 1}));
    this.add(this.performerContainer);
  },

  _getInfusionIngredient: function()
  {
    var dose = this.universalDosePane.getDose();
    return {
      medication: {
        name: this.medicationField.getValue()
      },
      quantity: dose.quantity,
      quantityUnit: this.universalDosePane.getNumeratorUnit(),
      volume: dose.quantityDenominator,
      volumeUnit: 'ml',
      doseForm: this.doseFormCombo.getSelection()
    }
  },

  _continuousInfusionChanged: function(continuousInfusion, clearInfusionValues)
  {
    this.infusionRatePane.presentContinuousInfusionFields(continuousInfusion);
    if (continuousInfusion)
    {
      this._setRateContainersVisible(true);
      this.dosingFrequencyLabel.hide();
      this.dosingFrequencyPane.hide();
      this.universalDosePane.clear();
      this.universalDosePane.hide();
    }
    else
    {
      this._setRateContainersVisible(false);
      this.dosingFrequencyLabel.show();
      this.dosingFrequencyPane.show();
      this.universalDosePane.show();
    }
    if (clearInfusionValues)
    {
      this.infusionRatePane.clearInfusionValues();
    }
    this._handleVarioEnabling();
  },

  _handleRouteChange: function(route)
  {
    this.route = route;
    if (route && route.type != 'IV')
    {
      this._continuousInfusionChanged(false, true);
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
    this.validationForm.addFormField(new tm.jquery.FormField({
      component: self.routesCombo,
      required: true,
      componentValueImplementationFn: function()
      {
        return self.routesCombo.getSelection();
      }
    }));
    if (!this.infusionRatePane.isHidden())
    {
      this._addValidations(this.infusionRatePane.getInfusionRatePaneValidations());
    }
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

  _openVariableRateEditPane: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var variableDosePane = new app.views.medications.ordering.ComplexVariableRatePane({
      view: this.view,
      startProcessOnEnter: true,
      height: 180,
      padding: 10,
      medicationData: this.medicationData,
      timedDoseElements: this.timedDoseElements,
      infusionIngredients: [this._getInfusionIngredient()],
      continuousInfusion: this.infusionRateTypePane.isContinuousInfusion(),
      volumeSum: this.universalDosePane.getDose().quantityDenominator,
      showFormula: false
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
            self.universalDosePane.setVolumeEditable(false);
          }
        },
        500,
        320
    );
    var cancelButton = variableDoseEditDialog.getBody().footer.cancelButton;
    cancelButton.setText(this.view.getDictionary('remove.vario'));
    cancelButton.setType("link");
    var cancelButtonHandler = cancelButton.getHandler();
    cancelButton.setHandler(function()
    {
      self.therapyIntervalPane.setStartHourEnabled(true);
      self.infusionRatePane.show();
      self.infusionRatePane.clear();
      self._continuousInfusionChanged(self.infusionRateTypePane.isContinuousInfusion(), true);
      self.dosingFrequencyPane.showAllFields();
      self.dosingFrequencyLabel.show();
      self.variableRateContainer.hide();
      self.timedDoseElements.removeAll();
      self.universalDosePane.setVolumeEditable(true);
      self.infusionRatePane.requestFocus();
      cancelButtonHandler();
    });

    variableDoseEditDialog.getBody().footer.setRightButtons([cancelButton, variableDoseEditDialog.getBody().footer.confirmButton]);
    variableDoseEditDialog.show();
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

  _showVariableRateDisplayValue: function()
  {
    this.variableRateContainer.removeAll(true);
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

      rowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', doseDisplayValue, 0));
      this.variableRateContainer.add(rowContainer);
    }

    this.variableRateContainer.show();
    this.variableRateContainer.repaint();
  },

  _createVerticalSpacer: function(size)
  {
    return new tm.jquery.Spacer({type: 'vertical', size: size});
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
    var variableRate = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var therapy = {
      medicationOrderFormType: app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX,
      variable: variableRate,
      ingredientsList: [this._getInfusionIngredient()],
      route: this.routesCombo.getSelection(),
      continuousInfusion: this.infusionRateTypePane.isContinuousInfusion(),
      adjustToFluidBalance: this.infusionRateTypePane.isAdjustToFluidBalance(),
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
      comment: this.commentField.getValue() ? this.commentField.getValue() : null
    };

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

  _handleDosingFrequencyChange: function()
  {
    this.therapyIntervalPane.calculateStart(this.dosingFrequencyPane.getDaysOfWeek());
    this.therapyIntervalPane.calculateEnd();
  },

  _handleVarioEnabling: function()
  {
    var quantityDenominator = this.universalDosePane.getDose().quantityDenominator;
    var continuousInfusion = this.infusionRateTypePane.isContinuousInfusion();
    this.varioButton.setEnabled(quantityDenominator != null || continuousInfusion);
  },

  _presentTherapy: function()
  {
    var therapy = this.therapyToEdit;
    var infusionIngredient = therapy.ingredientsList[0];
    this.medicationField.setValue(infusionIngredient.medication.name);
    this.doseFormCombo.setSelection(infusionIngredient.doseForm);
    this.heparinPane.setHeparinValue(therapy.additionalInstruction);
    this.routesCombo.setSelection(therapy.route);

    this.universalDosePane.setValue(
        infusionIngredient.quantity,
        infusionIngredient.quantityUnit,
        infusionIngredient.volume,
        infusionIngredient.volumeUnit);

    this.infusionRateTypePane.setContinuousInfusion(therapy.continuousInfusion, therapy.baselineInfusion, false, false);
    if (therapy.adjustToFluidBalance)
    {
      this.infusionRateTypePane.setAdjustToFluidBalance(true);
    }

    if (therapy.variable)
    {
      if (!therapy.continuousInfusion)
      {
        this.infusionRateTypePane.setSpeed(true);
      }
      this.infusionRatePane.setInfusionRate(null);
      this.infusionRatePane.hide();
      this.dosingFrequencyPane.showDaysOnly();
      this.timedDoseElements.push.apply(this.timedDoseElements, therapy.timedDoseElements);
      this._showVariableRateDisplayValue();
      this._setAndDisableStartHourForVario();
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
      this.infusionRatePane.setInfusionRate(rate);
      this.infusionRatePane.setDurationVisible(!therapy.continuousInfusion);
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

