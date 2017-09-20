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

Class.define('app.views.medications.ordering.InfusionRatePane', 'tm.jquery.Container', {
  cls: "infusion-rate-pane",
  scrollable: "visible",

  /** configs */
  view: null,
  setInfusionRateTypeFunction: null, //optional
  getInfusionRateTypeBolusFunction: null, //optional
  getInfusionIngredientsFunction: null,
  getContinuousInfusionFunction: null,
  getVolumeSumFunction: null,
  getDurationFunction: null,
  formulaVisibleFunction: null,
  singleIngredientVolumeCalculatedEvent: null, //optional
  durationChangeEvent: null, //optional
  rateFormulaChangeEvent: null, //optional
  changeEvent: null, //optional
  infusionRatePaneForVariable: false,
  getInfusionDataForVariableFunction: null,
  verticalLayout: false,
  formulaFieldFocusLostEvent: null, //optional
  rateFieldFocusLostEvent: null, //optional
  allowZeroRate: false, //optional
  autoRefreshRate: true,
  rateUnit: 'ml/h',

  /** privates */
  firstMedicationData: null,
  clearingInProgress: null,
  /** privates: components */
  durationField: null,
  durationSpacer: null,
  durationUnitLabel: null,
  durationUnitSpacer: null,
  rateField: null,
  rateUnitLabel: null,
  formulaField: null,
  formulaUnitPane: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildComponents();
    if (this.verticalLayout)
    {
      this.setLayout(tm.jquery.VFlexboxLayout.create("center", "stretch", 0));
      this._buildVerticalGui();
    }
    else
    {
      this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));
      this._buildGui();
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.durationField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, "duration-field");
    this.durationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      var durationValue = $(component.getDom()).val();
      if (durationValue)
      {
        self._setDurationInUnits(durationValue);
      }

      if (self.durationChangeEvent)
      {
        self.durationChangeEvent();
      }
      self._calculateInfusionValues('DURATION');
    });

    this.durationSpacer = this._createSpacer();
    this.durationUnitLabel = new app.views.medications.ValueLabel({
      cls: 'TextData pointer-cursor duration-unit-label', width: "25", value: 'h' });
    this.durationUnitLabel.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._toggleDurationUnits();
    });
    this.durationUnitSpacer = this._createSpacer();
    this.rateField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, "rate-field");

    if (this.isAutoRefreshRate())
    {
      this.rateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        self.refreshRate();
      });
    }

    this.rateField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.requestFocusToFormula();
      if (self.rateFieldFocusLostEvent)
      {
        self.rateFieldFocusLostEvent();
      }
    });

    this.rateUnitLabel = new tm.jquery.Container({
      cls: 'TextData rate-unit-label',
      html: tm.views.medications.MedicationUtils.getFormattedUnit(this.rateUnit)
    });

    this.formulaField = tm.views.medications.MedicationUtils.createNumberField('n3', 68, "formula-field");
    this.formulaField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._calculateRate('FORMULA');
      if (self.rateFormulaChangeEvent)
      {
        self.rateFormulaChangeEvent();
      }
      self._calculateInfusionValues('FORMULA');
    });

    if (this.formulaFieldFocusLostEvent)
    {
      this.formulaField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
      {
        if (!self.formulaUnitPane.isHidden())
        {
          self.formulaUnitPane.requestFocus();
        }
        if (self.formulaFieldFocusLostEvent)
        {
          self.formulaFieldFocusLostEvent();
        }
      });
    }

    this.formulaUnitPane = new app.views.medications.ordering.InfusionRateFormulaUnitPane({
      view: this.view,
      formulaUnitChangeEvent: function()
      {
        self._calculateFormulaFromRate();
      }
    });
  },

  _buildGui: function()
  {
    this.add(this.durationField);
    this.add(this.durationSpacer);
    this.add(this.durationUnitLabel);
    this.add(this.durationUnitSpacer);
    this.add(this.rateField);
    this.add(this._createSpacer());
    this.add(this.rateUnitLabel);
    this.add(this._createSpacer());
    this.add(this.formulaField);
    this.add(this._createSpacer());
    this.add(this.formulaUnitPane);
  },

  _buildVerticalGui: function()
  {
    var durationContainer = new tm.jquery.Container({
      cls: "duration-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center")
    });
    this.durationField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    durationContainer.add(this.durationField);
    durationContainer.add(this._createSpacer());
    durationContainer.add(this.durationUnitLabel);
    durationContainer.add(this._createVerticalSpacer());
    this.add(durationContainer);
    var rateFieldContainer = new tm.jquery.Container({
      cls: "rate-field-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center")
    });
    this.rateField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    rateFieldContainer.add(this.rateField);
    rateFieldContainer.add(this._createSpacer());
    rateFieldContainer.add(this.rateUnitLabel);
    rateFieldContainer.add(this._createVerticalSpacer());
    this.add(rateFieldContainer);
    var formulaFieldContainer = new tm.jquery.Container({
      cls: "formula-field-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center"),
      scrollable: 'visible'
    });
    this.formulaField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    formulaFieldContainer.add(this.formulaField);
    formulaFieldContainer.add(this._createSpacer());
    formulaFieldContainer.add(this.formulaUnitPane);
    formulaFieldContainer.add(this._createVerticalSpacer());
    this.add(formulaFieldContainer);
  },

  _createSpacer: function()
  {
    return new tm.jquery.Spacer({type: 'horizontal', size: 5});
  },

  _createVerticalSpacer: function()
  {
    return new tm.jquery.Spacer({type: 'horizontal', size: 2});
  },

  _showDurationFields: function()
  {
    this.durationField.show();
    this.durationSpacer.show();
    this.durationUnitLabel.show();
    this.durationUnitSpacer.show();
  },

  _hideDurationFields: function()
  {
    this.durationField.hide();
    this.durationSpacer.hide();
    this.durationUnitLabel.hide();
    this.durationUnitSpacer.hide();
  },

  _calculateRate: function(changeType, preventEvent) //changeType: 'RATE', 'FORMULA'
  {
    if (changeType === 'RATE')
    {
      var rate = this.rateField.getValue();
      if (rate)
      {
        this._calculateFormulaFromRate(preventEvent);
      }
      else if (this.isAllowZeroRate() && rate === 0)
      {
        this.formulaField.setValue(0, true);
      }
      else
      {
        this._calculateRateFromFormula(preventEvent);
      }
    }
    else if (changeType === 'FORMULA')
    {
      var formula = this.formulaField.getValue();
      if (formula)
      {
        this._calculateRateFromFormula(preventEvent);
      }
      else
      {
        this._calculateFormulaFromRate(preventEvent);
      }
    }
  },

  _calculateInfusionValues: function(changeType) //changeType: 'VOLUME', 'DURATION', 'RATE', 'FORMULA'
  {

    var duration = null;
    if (!this.infusionRatePaneForVariable)
    {
      duration = !this.durationField.isHidden() ? this._getDuration("h") : null;
    }
    else
    {
      duration = this.getDurationFunction();
    }

    var rate = this.rateField.getValue();
    var formula = this.formulaField.getValue();
    var dataForCalculation = this._getDataForCalculation();
    var continuousInfusion = this.getContinuousInfusionFunction();
    if (!continuousInfusion)
    {
      var volume = dataForCalculation.volume;
      var infusionIngredients = this.getInfusionIngredientsFunction();

      if (duration && volume && !rate)
      {
        this._calculateRateFromDurationAndVolume(duration, volume);
      }
      else if (!duration && volume && rate)
      {
        this._calculateDurationFromRateAndVolume(rate, volume);
      }
      else if (duration && !volume && rate && infusionIngredients.length == 1)
      {
        this._calculateVolumeFromDurationAndRate(duration, rate);
        this._calculateRate('RATE');
      }
      else if (duration && !volume && formula && infusionIngredients.length == 1)
      {
        this._calculateVolumeFromDurationAndFormula(duration, formula);
        this._calculateRate('FORMULA');
      }
      else if (duration && volume && rate)
      {
        if (changeType == 'VOLUME')
        {
          this._calculateRateFromDurationAndVolume(duration, volume);
        }
        else if (changeType == 'DURATION')
        {
          this._calculateRateFromDurationAndVolume(duration, volume);
        }
        else if (changeType == 'RATE')
        {
          if (this.infusionRatePaneForVariable)
          {
            this._calculateVolumeFromDurationAndRate(duration, rate);
          }
          else
          {
            this._calculateDurationFromRateAndVolume(rate, volume);
          }

        }
      }
    }
    else if (changeType == 'VOLUME' && rate)
    {
      this._calculateRate('RATE');
    }

  },

  _setDurationInUnits: function(durationValue)
  {
    var lastChar = durationValue.slice(-1);
    if (lastChar == "h" || lastChar == "m")
    {
      var duration = durationValue.substring(0, durationValue.length - 1);
      duration = duration.replace(',', '.');
      if (tm.jquery.Utils.isNumeric(duration))
      {
        this.durationField.setValue(duration);
        this.durationUnitLabel.setValue(lastChar == "m" ? "min" : "h");
      }
    }
  },

  _clearDuration: function(preventEvent)
  {
    this.durationField.setValue(null, preventEvent);
    this.durationUnitLabel.setValue("h", preventEvent);
  },

  _toggleDurationUnits: function()
  {
    var durationValue = this.durationField.getValue();
    var isMinutes = this.durationUnitLabel.getValue() == "min";
    var oldUnits = isMinutes ? "min" : "h";
    var newUnits = isMinutes ? "h" : "min";
    this._setDuration(durationValue, oldUnits, newUnits);
  },

  _setDuration: function(durationValue, oldUnits, newUnits)
  {
    var newDurationValue = tm.views.medications.MedicationUtils.convertToUnit(durationValue, oldUnits, newUnits);
    this.durationUnitLabel.setValue(newUnits);
    this.durationField.setValue(newDurationValue);
  },

  _getDuration: function(newUnits)
  {
    var durationValue = this.durationField.getValue();
    var oldUnits = this.durationUnitLabel.getValue();
    return tm.views.medications.MedicationUtils.convertToUnit(durationValue, oldUnits, newUnits);
  },

  _calculateRateFromDurationAndVolume: function(duration, volume)
  {
    var rate = Number(volume) / Number(duration);
    this.rateField.setValue(rate);
  },

  _calculateDurationFromRateAndVolume: function(rate, volume)
  {
    var duration = Number(volume) / Number(rate);
    this._setDuration(duration, "h", this.durationUnitLabel.getValue());
  },

  _calculateVolumeFromDurationAndRate: function(duration, rate) //for single ingredient only
  {
    if (this.singleIngredientVolumeCalculatedEvent)
    {
      var volume = Number(duration) * Number(rate);
      this.singleIngredientVolumeCalculatedEvent(volume);
    }
  },

  _calculateVolumeFromDurationAndFormula: function(duration, formula)    //for single ingredient only
  {
    if (this.singleIngredientVolumeCalculatedEvent)
    {
      var dataForCalculation = this._getDataForCalculationFromSingleMedicationStrength();

      if (formula &&
          dataForCalculation.patientWeight &&
          dataForCalculation.mass &&
          dataForCalculation.massUnit &&
          dataForCalculation.quantityDenominator)
      {
        var calculatedRate = this._calculateRateFromFormulaWithData(
            dataForCalculation.patientWeight,
            dataForCalculation.mass,
            dataForCalculation.massUnit,
            dataForCalculation.quantityDenominator,
            formula);

        if (calculatedRate)
        {
          var volume = Number(duration) * Number(calculatedRate);
          this.singleIngredientVolumeCalculatedEvent(volume);
        }
      }
    }
  },

  _getDataForCalculation: function()
  {
    var utils = tm.views.medications.MedicationUtils;
    var enums = app.views.medications.TherapyEnums;
    var infusionIngredients = this.getInfusionIngredientsFunction();
    if (this.infusionRatePaneForVariable && infusionIngredients.length == 1)
    {
      return this.getInfusionDataForVariableFunction();
    }
    var referenceWeight = this.view.getReferenceWeight();

    if (!tm.jquery.Utils.isEmpty(infusionIngredients) && infusionIngredients.length == 1)
    {
      var continuousInfusion = this.getContinuousInfusionFunction();
      if (continuousInfusion)
      {
        return this._getDataForCalculationFromSingleMedicationStrength();
      }
      else
      {
        return {
          patientWeight: referenceWeight,
          mass: infusionIngredients[0].quantity,
          massUnit: infusionIngredients[0].quantityUnit,
          volume: infusionIngredients[0].quantityDenominator != null ?
              infusionIngredients[0].quantityDenominator :
              infusionIngredients[0].quantity
        };
      }
    }
    else
    {
      var volumeSum = this.getVolumeSumFunction();
      var orderVolumeSum = 0;
      var ratio = 1;
      for (var i = 0; i < infusionIngredients.length; i++)
      {
        if (!tm.jquery.Utils.isEmpty(infusionIngredients[i].quantityDenominator))
        {
          orderVolumeSum += infusionIngredients[i].quantityDenominator;
        }
        else if (utils.isUnitVolumeUnit(infusionIngredients[i].quantityUnit))
        {
          orderVolumeSum += infusionIngredients[i].quantity;
        }
      }
      var therapyMedsAndSupps = tm.views.medications.MedicationUtils.filterMedicationsByTypes(infusionIngredients, 
          [enums.medicationTypeEnum.MEDICATION, enums.medicationTypeEnum.SUPPLEMENT]);
      if (therapyMedsAndSupps.length == 1 && volumeSum)
      {
        if (orderVolumeSum != 0)
        {
          ratio = volumeSum / orderVolumeSum;
        }
        return {
          patientWeight: referenceWeight,
          mass: therapyMedsAndSupps[0].quantity * ratio,
          massUnit: therapyMedsAndSupps[0].quantityUnit,
          volume: volumeSum
        };
      }
      else
      {
        return {
          patientWeight: referenceWeight,
          mass: null,
          massUnit: null,
          volume: volumeSum
        };
      }
    }
  },

  _getDataForCalculationFromSingleMedicationStrength: function()
  {
    var referenceWeight = this.view.getReferenceWeight();
    if (this.getFirstMedicationData())
    {
      var definingIngredient = this.getFirstMedicationData().getDefiningIngredient();
      if (definingIngredient &&
          (definingIngredient.strengthDenominatorUnit == 'ml' || definingIngredient.strengthDenominatorUnit == 'mL'))
      {
        return {
          patientWeight: referenceWeight,
          mass: definingIngredient.strengthNumerator,
          massUnit: definingIngredient.strengthNumeratorUnit,
          volume: definingIngredient.strengthDenominator
        };
      }
    }
    return {
      patientWeight: referenceWeight,
      mass: null,
      massUnit: null,
      volume: null
    };
  },

  _calculateRateFromFormula: function(preventEvent)
  {
    var dataForCalculation = this._getDataForCalculation();
    var formula = this.formulaField.getValue();
    if (formula && dataForCalculation.patientWeight && dataForCalculation.mass && dataForCalculation.massUnit && dataForCalculation.volume)
    {
      var calculatedRate = this._calculateRateFromFormulaWithData(
          dataForCalculation.patientWeight,
          dataForCalculation.mass,
          dataForCalculation.massUnit,
          dataForCalculation.volume,
          formula);

      if (calculatedRate != null)
      {
        this.rateField.setValue(calculatedRate, preventEvent);
      }
    }
  },

  _calculateRateFromFormulaWithData: function(patientWeight, mass, massUnit, volume, formula)  //mass unit = mg
  {
    var formulaUnit = this.formulaUnitPane.getRateFormulaUnit(); // (ug/kg/min)
    var formulaMassUnit = formulaUnit.massUnit;
    var formulaPatientUnit = formulaUnit.patientUnit;
    var formulaTimeUnit = formulaUnit.timeUnit;

    var formulaWithPatientUnit;
    if (formulaPatientUnit == 'kg')
    {
      formulaWithPatientUnit = formula * patientWeight;  // ug/min
    }
    else if (formulaPatientUnit == 'm2')
    {
      var heightInCm = this.view.getPatientHeightInCm();
      formulaWithPatientUnit = formula * tm.views.medications.MedicationUtils.calculateBodySurfaceArea(heightInCm, patientWeight);
    }
    else
    {
      formulaWithPatientUnit = formula;
    }
    var formulaInMassUnit = tm.views.medications.MedicationUtils.convertToUnit(formulaWithPatientUnit, formulaMassUnit, massUnit); // mg/min
    if (formulaInMassUnit)
    {
      var formulaInMl = formulaInMassUnit * volume / mass; // ml/min
      var timeRatio = tm.views.medications.MedicationUtils.convertToUnit(1, formulaTimeUnit, "h");
      return formulaInMl / timeRatio;  // ml/h
    }
    return null;
  },

  _calculateFormulaFromRate: function(preventEvent)
  {
    var dataForCalculation = this._getDataForCalculation();
    if (dataForCalculation.patientWeight && dataForCalculation.mass && dataForCalculation.massUnit && dataForCalculation.volume)
    {
      var calculatedFormula = this._calculateFormulaFromRateWithData(
          dataForCalculation.patientWeight,
          dataForCalculation.mass,
          dataForCalculation.massUnit,
          dataForCalculation.volume);

      if (calculatedFormula != null)
      {
        this.formulaField.setValue(calculatedFormula, preventEvent);
      }
    }
  },

  _calculateFormulaFromRateWithData: function(patientWeight, mass, massUnit, volume)  //mass unit = mg
  {
    var rate = this.rateField.getValue(); // ml/h
    var formulaUnit = this.formulaUnitPane.getRateFormulaUnit(); // (ug/kg/min)
    if (formulaUnit)
    {
      var formulaMassUnit = formulaUnit.massUnit;
      var formulaPatientUnit = formulaUnit.patientUnit;
      var formulaTimeUnit = formulaUnit.timeUnit;

      var patientUnitWithFormula;
      if (formulaPatientUnit == 'kg')
      {
        patientUnitWithFormula = rate / patientWeight;  // ml/kg/h
      }
      else if (formulaPatientUnit == 'm2')
      {
        var heightInCm = this.view.getPatientHeightInCm();
        patientUnitWithFormula = rate / tm.views.medications.MedicationUtils.calculateBodySurfaceArea(heightInCm, patientWeight);
      }
      else
      {
        patientUnitWithFormula = rate;
      }
      var rateInMassUnit = patientUnitWithFormula * mass / volume; // mg/kg/h
      var rateInFormulaMassUnit = tm.views.medications.MedicationUtils.convertToUnit(rateInMassUnit, massUnit, formulaMassUnit); // ug/kg/h
      if (rateInFormulaMassUnit)
      {
        var timeRatio = tm.views.medications.MedicationUtils.convertToUnit(1, "h", formulaTimeUnit);
        return rateInFormulaMassUnit / timeRatio;  // ug/kg/min
      }
    }
    return null;
  },

  _clearFieldValues: function(preventEvent)
  {
    this._clearDuration(preventEvent);
    this.rateField.setValue(null, preventEvent);
    this.formulaField.setValue(null, preventEvent);
  },

  /** public methods */

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {boolean} [preventEvent=false]
   */
  setFirstMedicationData: function(medicationData, preventEvent)
  {
    this.firstMedicationData = medicationData;
    this.formulaUnitPane.setMedicationData(medicationData, this.getContinuousInfusionFunction(), preventEvent);
  },

  getInfusionRate: function()
  {
    if (this.getInfusionRateTypeBolusFunction && this.getInfusionRateTypeBolusFunction() == true)
    {
      return 'BOLUS';
    }
    return {
      duration: !this.durationField.isHidden() && this.durationField.getValue() ? this._getDuration("min") : null,
      rate: this.rateField.getValue(),
      rateUnit: this.rateUnit,
      rateFormula: this.formulaField.getValue(),
      rateFormulaUnit: this.formulaUnitPane.getRateFormulaUnit() && !tm.jquery.Utils.isEmpty(this.formulaField.getValue()) ?
          this.formulaUnitPane.getRateFormulaUnit().displayUnit : null
    }
  },

  setInfusionRate: function(rate, preventEvent)
  {
    if (rate && rate != 'BOLUS')
    {
      this.rateField.setValue(rate, preventEvent);
    }

    if (!preventEvent && this.setInfusionRateTypeFunction)
    {
      this.setInfusionRateTypeFunction(rate);
    }
  },

  setRate: function(rate, preventEvent)
  {
    this.rateField.setValue(rate, preventEvent);
  },

  setRateUnit: function(rateUnit)
  {
    this.rateUnit = rateUnit;
    this.rateUnitLabel.setHtml(tm.views.medications.MedicationUtils.getFormattedUnit(rateUnit));
  },

  /**
   * @param {Boolean} visible
   */
  setDurationVisible: function(visible)
  {
    if (visible)
    {
      this._showDurationFields();
    }
    else
    {
      this._hideDurationFields();
    }
  },

  getInfusionRateFormulaPerHour: function(quantityUnit)
  {
    var rateFormula = this.formulaField.getValue();
    var rateFormulaUnit = this.formulaUnitPane.getRateFormulaUnit();
    if (rateFormula && quantityUnit)
    {
      var formulaInMassUnit = tm.views.medications.MedicationUtils.convertToUnit(rateFormula, rateFormulaUnit.massUnit, quantityUnit);
      if (formulaInMassUnit)
      {
        var timeRatio = tm.views.medications.MedicationUtils.convertToUnit(1, rateFormulaUnit.timeUnit, "h");
        return formulaInMassUnit / timeRatio;
      }
    }
    return null;
  },

  clear: function(preventEvent)
  {
    this.setDurationVisible(true);
    this._clearFieldValues(preventEvent);
    if (this.infusionRateTypePane)
    {
      this.infusionRateTypePane.clear(preventEvent);
    }
  },

  calculateInfusionValues: function()
  {
    this._calculateInfusionValues('VOLUME');
  },

  clearInfusionValues: function()
  {
    this._clearFieldValues(true);
    var infusionIngredients = this.getInfusionIngredientsFunction();
    if (infusionIngredients.length == 1)
    {
      this.singleIngredientVolumeCalculatedEvent(null);
    }
  },

  clearFieldValues: function()
  {
    this._clearFieldValues(true);
  },

  getInfusionRatePaneValidations: function()
  {
    var self = this;
    var formFields = [];
    formFields.push(new tm.jquery.FormField({
      component: self,
      required: true,
      componentValueImplementationFn: function()
      {
        var infusionRate = self.getInfusionRate();
        if (infusionRate == 'BOLUS')
        {
          return true;
        }
        else if (infusionRate.rate && (self.durationField.isHidden() || infusionRate.duration))
        {
          return true;
        }
        else if (self.isAllowZeroRate() && infusionRate.rate === 0)
        {
          return true;
        }
        return null;
      }
    }));
    return formFields;
  },

  requestFocus: function()
  {
    if (!this.durationField.isHidden())
    {
      this.durationField.focus();
    }
    else
    {
      this.rateField.focus();
    }
  },

  requestFocusToRate: function()
  {
    this.rateField.focus();
  },

  requestFocusToFormula: function()
  {
    this.formulaField.focus();
  },

  setFormula: function(formula, preventEvent)
  {
    this.formulaField.setValue(formula, preventEvent);
  },

  setFormulaUnitToLabel: function(formulaUnitDisplay)
  {
    this.formulaUnitPane.setFormulaUnitToLabel(formulaUnitDisplay);
    this._calculateFormulaFromRate();
  },

  setFormulaUnit: function(formulaUnitDisplay)
  {
    this.formulaUnitPane.setFormulaUnit(formulaUnitDisplay);
    this._calculateFormulaFromRate();
  },

  setFormulaVisible: function()
  {
    if (this.formulaVisibleFunction())
    {
      this.isRendered() ? this.formulaField.show() : this.formulaField.setHidden(false);
      this.isRendered() ? this.formulaUnitPane.show() : this.formulaUnitPane.setHidden(false);
    }
    else
    {
      this.isRendered() ? this.formulaField.hide() : this.formulaField.setHidden(true);
      this.isRendered() ? this.formulaUnitPane.hide() : this.formulaUnitPane.setHidden(true);
    }
  },
  
  refreshRate: function(preventEvent)
  {
    this._calculateRate('RATE', preventEvent);
    this._calculateInfusionValues('RATE', preventEvent);
    if (!preventEvent && this.changeEvent)
    {
      this.changeEvent();
    }
  },

  presentContinuousInfusionFields: function(continuousInfusion)
  {
    this.setDurationVisible(!continuousInfusion);
    this._calculateRate("RATE");
  },

  updateFormulaUnitsForContinuous: function(continuousInfusion)
  {
    if (this.firstMedicationData)
    {
      this.formulaUnitPane.setMedicationData(this.firstMedicationData, continuousInfusion);
    }
  },

  /**
   * @returns {boolean}
   */
  isAutoRefreshRate: function()
  {
    return this.autoRefreshRate === true;
  },

  /**
   * @returns {boolean}
   */
  isAllowZeroRate: function()
  {
    return this.allowZeroRate === true;
  },

  /**
   * @returns {tm.jquery.NumberField}
   */
  getRateField: function()
  {
    return this.rateField;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getFirstMedicationData: function()
  {
    return this.firstMedicationData;
  },

  getRateFormulaUnit: function()
  {
    return this.formulaUnitPane.getRateFormulaUnit();
  }
});
