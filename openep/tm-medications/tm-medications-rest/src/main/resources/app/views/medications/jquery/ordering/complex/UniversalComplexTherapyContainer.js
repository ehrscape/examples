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
  therapyAlreadyStarted: false,
  therapyModifiedInThePast: false,
  /** privates */
  validationForm: null,
  timedDoseElements: null,
  editingStartTimestamp: null,
  saveTherapyFunction: null, //optional
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
  warningsContainer: null,
  saveDateTimePane: null,
  therapyNextAdministrationLabelPane: null,
  administrationPreviewTimeline: null,

  _previewRefreshTimer: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.timedDoseElements = [];
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', "stretch", 0));

    if (this.therapyToEdit)
    {
      this.editingStartTimestamp = CurrentTime.get();
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
    this.therapyIntervalPane.setEnd(null);
    if (this.therapyToEdit)
    {
      var therapyHasAlreadyStarted = new Date(this.therapyToEdit.start) < this._getEditTimestamp();
      if (therapyHasAlreadyStarted &&
          this.therapyToEdit.continuousInfusion &&
          this.therapyToEdit.variable && !this.therapyToEdit.recurringContinuousInfusion)
      {
        this.therapyToEdit.variable = false;
        this.therapyToEdit.doseElement = this.therapyToEdit.timedDoseElements[this.therapyToEdit.timedDoseElements.length - 1].doseElement;
        this.therapyToEdit.timedDoseElements = [];
      }

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
      cls: "routes-combo",
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
      disableContinuousInfusionChange: this.therapyToEdit != null && this.therapyAlreadyStarted,
      continuousInfusionChangedFunction: function(continuousInfusion, clearValues)
      {
        tm.jquery.Utils.isEmpty(self.therapyIntervalPane.getStart()) ?
            self.administrationPreviewTimeline.setData(self._buildTherapy(), []) : self.refreshAdministrationPreview();

        self._continuousInfusionChanged(continuousInfusion, clearValues)
      },
      adjustableRateChangeFunction: function(adjustableRate)
      {
        self.administrationPreviewTimeline.setData(self._buildTherapy(), []);
        self._setRateContainersVisible(!adjustableRate);
        self.infusionRatePane.clearInfusionValues();
      },
      bolusChangedFunction: function()
      {
        self.administrationPreviewTimeline.setData(self._buildTherapy(), []);
        self.universalDosePane.show();
        self._setRateContainersVisible(false);
        self.infusionRatePane.clearFieldValues();
        self.dosingFrequencyLabel.show();
        self.dosingFrequencyPane.show();
      },
      speedChangedFunction: function(isSpeed)
      {
        self.administrationPreviewTimeline.setData(self._buildTherapy(), []);
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
    this.rateLabelContainer = new tm.jquery.Container({
      width: 160,
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start")
    });
    var contextMenu = appFactory.createContextMenu();
    var menuItem = new tm.jquery.MenuItem({
          cls: "clear-form-menu-item",
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
          }
        }
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
    this.variableRateContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      margin: '7 0 0 0'
    });

    this.varioButton = new tm.jquery.Button({
      cls: "vario-button",
      text: this.view.getDictionary('variable'),
      type: 'link',
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
      },
      getDurationFunction: function()
      {
        var infusionRate = self.infusionRatePane.getInfusionRate();
        if (infusionRate && infusionRate.duration)
        {
          return infusionRate.duration;
        }
        return null;
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
    doseRowContainer.add(new tm.jquery.Container({flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")}));
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
    var infusionRateContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5),
      width: 678
    });
    infusionRateContainer.add(this.infusionRatePane);
    infusionRateContainer.add(this.varioButton);
    infusionRateContainer.add(this.variableRateContainer);
    this.add(infusionRateContainer);
    this.add(this._createVerticalSpacer(2));

    this.dosingFrequencyLabel = tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval'));
    this.add(this.dosingFrequencyLabel);
    this.add(this.dosingFrequencyPane);
    this.add(this._createVerticalSpacer(2));

    this.add(this.therapyIntervalPane);
    this.add(this.therapyNextAdministrationLabelPane);
    this.add(this.administrationPreviewTimeline);
    this.add(this._createVerticalSpacer(2));

    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('commentary')));
    this.add(this.commentField);
    this.add(this.warningsContainer);
    this.add(new tm.jquery.Container({flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")}));
  },

  _getEditTimestamp: function()
  {
    return this.saveDateTimePane.isHidden() ? this.editingStartTimestamp : this.saveDateTimePane.getSaveDateTime();
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
      quantityDenominator: dose.quantityDenominator,
      quantityDenominatorUnit: 'ml',
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
      this.therapyIntervalPane.setRestrictedStartHourSelection(false);
    }
    else
    {
      this._setRateContainersVisible(false);
      this.dosingFrequencyLabel.show();
      this.dosingFrequencyPane.show();
      this.universalDosePane.show();
      if (!this.editMode)
      {
        this.therapyIntervalPane.setRestrictedStartHourSelection(true);
      }
    }
    if (clearInfusionValues)
    {
      this.infusionRatePane.clearInfusionValues();
      this.therapyIntervalPane.setStartHourEnabled(true);
    }
    var therapy = this._buildTherapy();
    var oldTherapy = this.therapyToEdit ? this.therapyToEdit : null;
    this.therapyIntervalPane.calculateStart(therapy, oldTherapy, null);
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
    var start = CurrentTime.get();
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

    var variableDosePane = new app.views.medications.ordering.ComplexVariableRateDataEntryContainer({
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
    var utils = tm.views.medications.MedicationUtils;
    this.variableRateContainer.removeAll(true);
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
      var doseDisplayValue = utils.getFormattedDecimalNumber(utils.doubleToString(doseElement.rate, 'n2')) + ' ' + doseElement.rateUnit;

      rowContainer.add(utils.crateLabel('TextData', doseDisplayValue, 0));
      this.variableRateContainer.add(rowContainer);
    }

    this.variableRateContainer.show();
    this.variableRateContainer.repaint();
  },

  _createVerticalSpacer: function(size)
  {
    return new tm.jquery.Spacer({type: 'vertical', size: size});
  },

  _saveTherapy: function(therapy)
  {
    var self = this;
    var prescriber = this.view.getCurrentUserAsCareProfessional();
    var saveDateTime = this.saveDateTimePane.isHidden() ? null : this.saveDateTimePane.getSaveDateTime();

    if (this.therapyToEdit)
    {
      therapy.setCompositionUid(this.therapyToEdit.getCompositionUid());
      therapy.setEhrOrderName(this.therapyToEdit.getEhrOrderName());

      this.view.getRestApi().modifyTherapy(
          therapy,
          null,
          prescriber,
          this.therapyAlreadyStarted,
          saveDateTime,
          true)
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
      var order = [
        new app.views.medications.common.dto.SaveMedicationOrder({
          therapy: therapy,
          actionEnum: app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE
        })
      ];

      this.view.getRestApi().saveMedicationsOrder(order, prescriber, saveDateTime, null, true)
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
    var variableRate = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var therapy = new app.views.medications.common.dto.Therapy({
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
      startCriterion: this.therapyIntervalPane.getStartCriterion(),
      reviewReminderDays: this.therapyIntervalPane.getReviewReminderDays(),
      applicationPrecondition: this.dosingFrequencyPane.getApplicationPrecondition(),
      comment: this.commentField.getValue() ? this.commentField.getValue() : null
    });

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
    this.routesCombo.setSelection(therapy.getRoutes()[0]);

    this.universalDosePane.setValue(
        infusionIngredient.quantity,
        infusionIngredient.quantityUnit,
        infusionIngredient.quantityDenominator,
        infusionIngredient.quantityDenominatorUnit);

    this.infusionRateTypePane.setContinuousInfusion(
        therapy.isBaselineInfusion(),
        therapy.isTitrationDoseType(),
        therapy.isAdjustToFluidBalance(),
        false,
        false);

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
      if (this.recurringContinuousInfusion)
      {
        var nextAdministrationTimestamp =
            tm.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForVario(this.timedDoseElements);
        this.therapyIntervalPane.setStart(nextAdministrationTimestamp);
        this.therapyIntervalPane.setStartHourEnabled(true);
      }
      else if (this.infusionRateTypePane.isContinuousInfusion())
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
      this.infusionRatePane.setInfusionRate(rate);
      this.infusionRatePane.setDurationVisible(!therapy.continuousInfusion);
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
      this.therapyNextAdministrationLabelPane.setOldTherapyId(therapy.compositionUid, therapy.ehrOrderName, this.infusionRateTypePane.isContinuousInfusion());
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

    this._previewRefreshTimer = setTimeout(function()
    {
      if (self.isRendered())
      {
        self.administrationPreviewTimeline._refreshAdministrationPreviewImpl(self.therapyIntervalPane.getStart(), self._buildTherapy());
      }
    }, 150);
  }
});

