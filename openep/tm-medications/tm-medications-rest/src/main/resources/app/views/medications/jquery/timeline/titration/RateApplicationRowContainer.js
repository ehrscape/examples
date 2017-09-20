Class.define('app.views.medications.timeline.titration.RateApplicationRowContainer', 'app.views.medications.timeline.titration.BaseApplicationRowContainer', {
  scrollable: 'visible',
  cls: "rate-application-row-container",

  lastPositiveInfusionRate: null,
  activeContinuousInfusion: true,

  _ratePane: null,
  _bagContainerWrapper: null,
  _bagField: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  buildGui: function()
  {
    this.callSuper();
    var self = this;
    this._timePicker.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.assertAdministrationChange();
    });
  },

  /**
   * @returns {tm.views.medications.common.VerticallyTitledComponent}
   */
  buildApplicationOptionsColumn: function()
  {
    var self = this;
    var view = this.getView();

    var applicationOptionsColumn = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("application"),
      scrollable: 'visible',
      contentComponent: new tm.jquery.CheckBox({
        labelText: view.getDictionary("mark.this.application.as.already.given"),
        cls: "mark-given-checkbox",
        labelCls: "TextData",
        checked: this.isAdjustInfusion() || this.isStopFlow(),
        labelAlign: "right",
        enabled: !(this.isAdjustInfusion() || this.isStopFlow()),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        nowrap: true
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    applicationOptionsColumn.getContentComponent().on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (view.isInfusionBagEnabled())
      {
        self.setMarkGiven(component.checked === true);
      }
      self.assertAdministrationChange();
    });
    this._markAsGivenCheckBox = applicationOptionsColumn.getContentComponent();

    return applicationOptionsColumn;
  },

  /**
   * @returns {tm.jquery.Container}
   */
  buildApplicationDosingRow: function()
  {
    var self = this;
    var view = this.getView();
    var plannedDoseTime = this.getAdministration() ? new Date(this.getAdministration().plannedTime) : CurrentTime.get();
    var applicationDosingRow = new tm.jquery.Container({
      scrollable: 'visible',
      hidden: this.isStopFlow(),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var ratePaneColumn = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("infusion.rate"),
      cls: 'rate-pane-container',
      scrollable: 'visible',
      contentComponent: new app.views.medications.ordering.InfusionRatePane({
        view: this.view,
        cls: "infusion-rate-pane",
        medicationData: this.getMedicationData(),
        verticalLayout: false,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        getInfusionIngredientsFunction: function()
        {
          var ingredientList = [];

          self.getTherapyForTitration().getTherapy().getIngredientsList().forEach(function(ingredient)
          {
            ingredientList.push({
              medication: ingredient.medication,
              quantity: ingredient.quantity,
              quantityUnit: ingredient.quantityUnit,
              quantityDenominator: ingredient.quantityDenominator
            });
          });
          return ingredientList;
        },
        getContinuousInfusionFunction: function()
        {
          if (self.getTherapyForTitration().getTherapy().isOrderTypeComplex())
          {
            return self.getTherapyForTitration().getTherapy().isContinuousInfusion();
          }
          return false;
        },
        formulaVisibleFunction: function()
        {
          return true;
        },
        getVolumeSumFunction: function()
        {
          return self.getTherapyForTitration().getTherapy().getVolumeSum();
        }
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    applicationDosingRow.add(ratePaneColumn);

    ratePaneColumn.getContentComponent().setDurationVisible(false);
    ratePaneColumn.getContentComponent().setFirstMedicationData(this.medicationData);
    ratePaneColumn.getContentComponent().setFormulaVisible(true);

    this._ratePane = ratePaneColumn.getContentComponent();

    this.assertAdministrationChange(plannedDoseTime);

    if (this.isAdjustInfusion())
    {
      this._setRateFromLastAdministrationWithRate();
    }
    return applicationDosingRow;
  },

  /**
   * @returns {tm.jquery.Container}
   */
  buildInfusionBagRow: function()
  {
    var view = this.getView();

    var infusionBagRow = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      height: 48,
      hidden: true
    });

    var bagLabel = new tm.jquery.Container({
      cls: 'TextLabel volume-label',
      html: view.getDictionary('bag.syringe.volume'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var bagContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)
    });

    var bagField = tm.views.medications.MedicationUtils.createNumberField('n2', 68);

    var bagMlLabel = new tm.jquery.Container({
      cls: 'TextData ml-label',
      html: 'mL',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "center")
    });

    bagContainer.add(bagField);
    bagContainer.add(bagMlLabel);
    infusionBagRow.add(bagLabel);
    infusionBagRow.add(bagContainer);

    this._bagContainerWrapper = infusionBagRow;
    this._bagField = bagField;

    return infusionBagRow;
  },

  /**
   * @param {Date} [administrationDate=null]
   * @private
   */
  assertAdministrationChange: function(administrationDate)
  {
    administrationDate = administrationDate ? administrationDate : this._timePicker.getDate();

    var checkNextInLine = this._markAsGivenCheckBox.isChecked() && administrationDate > CurrentTime.get();
    var normalizedAdministrationDate = new Date(
        administrationDate.getFullYear(),
        administrationDate.getMonth(),
        administrationDate.getDate(),
        administrationDate.getHours(),
        administrationDate.getMinutes(),
        0, 0);

    var warnings = this._administrationWarningsProvider.getRestrictiveAdministrationWarnings(
        normalizedAdministrationDate,
        true,
        checkNextInLine,
        false,
        false
    );

    this._warningContainer.setRestrictiveWarnings(warnings);
    this.setAdministrationWarnings(warnings);
  },

  _setRateFromLastAdministrationWithRate: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var rateIsSet = false;

    if (this.getAllAdministrations())
    {
      var lastAdministrationWithRate = null;
      this.getAllAdministrations().forEach(function(administration)
      {
        if (((administration.administrationType === enums.administrationTypeEnum.START ||
            administration.administrationType === enums.administrationTypeEnum.ADJUST_INFUSION) &&
            administration.administrationStatus === enums.administrationStatusEnum.COMPLETED ||
            administration.administrationStatus === enums.administrationStatusEnum.COMPLETED_EARLY ||
            administration.administrationStatus === enums.administrationStatusEnum.COMPLETED_LATE) &&
            (!tm.jquery.Utils.isEmpty(administration.administeredDose) && administration.administeredDose.numerator !== 0))
        {
          if (lastAdministrationWithRate === null ||
              administration.administrationTime > lastAdministrationWithRate.administrationTime)
          {
            lastAdministrationWithRate = administration;
          }
        }
      });
      if (!tm.jquery.Utils.isEmpty(lastAdministrationWithRate))
      {
        if (!tm.jquery.Utils.isEmpty(lastAdministrationWithRate.administeredDose.numerator))
        {
          rateIsSet = true;
          this._ratePane.setRate(lastAdministrationWithRate.administeredDose.numerator);
        }
        if (!tm.jquery.Utils.isEmpty(lastAdministrationWithRate.administeredDose.denominatorUnit))
        {
          this._ratePane.setFormulaUnitToLabel(lastAdministrationWithRate.administeredDose.denominatorUnit);
        }
      }
    }
    if (!rateIsSet && this.getLastPositiveInfusionRate())
    {
      this._ratePane.setRate(this.getLastPositiveInfusionRate());
    }
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getFormFields: function()
  {
    var formFields = [];
    var self = this;
    formFields = formFields.concat(this._ratePane.getInfusionRatePaneValidations());

    formFields.push(new tm.jquery.FormField({
      component: this._timePicker,
      required: true,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function()
            {
              return !(self.getAdministrationWarnings() ?
                  self.getAdministrationWarnings().hasRestrictiveWarnings() : false);
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate();
      },
      getComponentValidationMarkElement: function(component)
      {
        return component.getTimePicker().getField().getInputElement();
      }
    }));

    formFields.push(new tm.jquery.FormField({
      component: this._markAsGivenCheckBox,
      required: false,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function(value)
            {
              var hasRestrictiveWarnings = self.getAdministrationWarnings() ?
                  self.getAdministrationWarnings().hasRestrictiveWarnings() : false;
              return value === true && !hasRestrictiveWarnings || value === false;
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.isChecked();
      }
    }));

    formFields.push(new tm.jquery.FormField({
      component: this._timePicker,
      required: true,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function()
            {
              return !(self.getAdministrationWarnings() ?
                  self.getAdministrationWarnings().hasRestrictiveWarnings() : false);
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate();
      },
      getComponentValidationMarkElement: function(component)
      {
        return component.getDatePicker().getField().getInputElement();
      }
    }));

    return formFields;
  },

  /**
   * @param {Boolean} markedAsGiven
   */
  setMarkGiven: function(markedAsGiven)
  {
    if (markedAsGiven)
    {
      this._bagContainerWrapper.show();
      this._bagContainerWrapper.focus();
    }
    else
    {
      this._bagContainerWrapper.hide();
    }
  },

  /**
   * @returns {app.views.medications.common.dto.TherapyDose}
   */
  getTherapyDose: function()
  {
    var doseEnums = app.views.medications.TherapyEnums.therapyDoseTypeEnum;
    var rateValues = this._ratePane.getInfusionRate();
    return new app.views.medications.common.dto.TherapyDose({
      therapyDoseTypeEnum: doseEnums.RATE,
      numerator: this.isStopFlow() ? 0 : rateValues.rate,
      denominator: this.isStopFlow() ? 0 : rateValues.rateFormula,
      numeratorUnit: rateValues.rateUnit,
      denominatorUnit: this.isStopFlow() ? this.getTherapyForTitration().getUnit() : rateValues.rateFormulaUnit
    });
  },

  /**
   * @returns {Float|Number|null}
   */
  getLastPositiveInfusionRate: function()
  {
    return this.lastPositiveInfusionRate
  }
});
