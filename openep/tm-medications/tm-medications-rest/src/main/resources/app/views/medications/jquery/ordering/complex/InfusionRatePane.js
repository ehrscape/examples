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
  scrollable: "visible",

  /** configs */
  view: null,
  setInfusionRateTypeFunction: null, //optional
  getInfusionRateTypeBolusFunction: null, //optional
  getInfusionIngredientsFunction: null,
  getContinuousInfusionFunction: null,
  getVolumeSumFunction: null,
  formulaVisibleFunction: null,
  singleIngredientVolumeCalculatedEvent: null, //optional
  durationChangeEvent: null, //optional
  rateFormulaChangeEvent: null, //optional
  changeEvent: null, //optional
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
    this.setLayout(tm.jquery.HFlexboxLayout.create("start", "start"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.clearingInProgress = false;

    this.durationField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, '0');
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
    this.durationUnitLabel = new app.views.medications.ValueLabel({cls: 'TextData pointer-cursor', width: "25", value: 'h', padding: '6 0 0 0'});
    this.durationUnitLabel.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._toggleDurationUnits();
    });
    this.durationUnitSpacer = this._createSpacer();
    this.rateField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, '0');
    this.rateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._calculateRate('RATE');
      self._calculateInfusionValues('RATE');
      if (self.changeEvent)
      {
        self.changeEvent();
      }
    });
    this.rateUnitLabel = tm.views.medications.MedicationUtils.crateLabel('TextData', 'ml/h');

    this.formulaField = tm.views.medications.MedicationUtils.createNumberField('n3', 68, '0');
    this.formulaField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._calculateRate('FORMULA');
      if (self.rateFormulaChangeEvent)
      {
        self.rateFormulaChangeEvent();
      }
      self._calculateInfusionValues('FORMULA');
    });

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

  _createSpacer: function()
  {
    return new tm.jquery.Spacer({type: 'horizontal', size: 5});
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

  _calculateRate: function(changeType) //changeType: 'RATE', 'FORMULA'
  {
    if (!this.clearingInProgress)
    {
      if (changeType == 'RATE')
      {
        var rate = this.rateField.getValue();
        if (rate)
        {
          this._calculateFormulaFromRate();
        }
        else
        {
          this._calculateRateFromFormula();
        }
      }
      else if (changeType == 'FORMULA')
      {
        var formula = this.formulaField.getValue();
        if (formula)
        {
          this._calculateRateFromFormula();
        }
        else
        {
          this._calculateFormulaFromRate();
        }
      }
    }
  },

  _calculateInfusionValues: function(changeType) //changeType: 'VOLUME', 'DURATION', 'RATE', 'FORMULA'
  {
    if (!this.clearingInProgress)
    {
      var duration = !this.durationField.isHidden() ? this._getDuration("h") : null;
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
            this._calculateDurationFromRateAndVolume(rate, volume);
          }
        }
      }
      else if (changeType == 'VOLUME' && rate)
      {
        this._calculateRate('RATE');
      }
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

  _clearDuration: function()
  {
    this.durationField.setValue(null);
    this.durationUnitLabel.setValue("h");
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

      if (formula && dataForCalculation.patientWeight && dataForCalculation.mass && dataForCalculation.massUnit && dataForCalculation.volume)
      {
        var calculatedRate = this._calculateRateFromFormulaWithData(
            dataForCalculation.patientWeight,
            dataForCalculation.mass,
            dataForCalculation.massUnit,
            dataForCalculation.volume,
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
    var referenceWeight = this.view.getReferenceWeight();
    var infusionIngredients = this.getInfusionIngredientsFunction();
    if (infusionIngredients.length == 1)
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
          volume: infusionIngredients[0].volume
        };
      }
    }
    else
    {
      var volumeSum = this.getVolumeSumFunction();
      var therapyMedsAndSupps = tm.views.medications.MedicationUtils.filterMedicationsByTypes(infusionIngredients, ['MEDICATION', 'SUPPLEMENT']);
      if (therapyMedsAndSupps.length == 1 && volumeSum)
      {
        return {
          patientWeight: referenceWeight,
          mass: therapyMedsAndSupps[0].quantity,
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
    var definingIngredient = tm.views.medications.MedicationUtils.getDefiningIngredient(this.firstMedicationData);
    if (definingIngredient)
    {
      if (definingIngredient.strengthDenominatorUnit == 'ml')
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


  _calculateRateFromFormula: function()
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
        this.rateField.setValue(calculatedRate);
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
      var heightInCm = this.view.getPatientData().heightInCm;
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

  _calculateFormulaFromRate: function()
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
        this.formulaField.setValue(calculatedFormula);
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
        var heightInCm = this.view.getPatientData().heightInCm;
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

  _setDurationVisible: function(visible)
  {
    if (visible)
    {
      this._showDurationFields();
    }
    else
    {
      this._hideDurationFields();
    }
    this._calculateRate("RATE");
  },

  _clearFieldValues: function()
  {
    this.clearingInProgress = true;
    this._clearDuration();
    this.rateField.setValue(null);
    this.formulaField.setValue(null);
    this.clearingInProgress = false;
  },

  /** public methods */

  setFirstMedicationData: function(medicationData)
  {
    this.firstMedicationData = medicationData;
    this.formulaUnitPane.setMedicationData(medicationData);
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
      rateUnit: 'ml/h',
      rateFormula: this.formulaField.getValue(),
      rateFormulaUnit: this.formulaUnitPane.getRateFormulaUnit() ? this.formulaUnitPane.getRateFormulaUnit().displayUnit : null
    }
  },

  setInfusionRate: function(rate)
  {
    if (rate && rate != 'BOLUS')
    {
      this.rateField.setValue(rate);
    }

    if (this.setInfusionRateTypeFunction)
    {
      this.setInfusionRateTypeFunction(rate);
    }
  },

  setRate: function(rate)
  {
    this.rateField.setValue(rate);
  },

  setDurationVisible: function(visible)
  {
    this._setDurationVisible(visible);
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

  clear: function()
  {
    this._setDurationVisible(true);
    this._clearFieldValues();
    if (this.infusionRateTypePane)
    {
      this.infusionRateTypePane.clear();
    }
  },

  calculateInfusionValues: function()
  {
    this._calculateInfusionValues('VOLUME');
  },

  clearInfusionValues: function()
  {
    this.clearingInProgress = true;
    this._clearFieldValues();
    var infusionIngredients = this.getInfusionIngredientsFunction();
    if (infusionIngredients.length == 1)
    {
      this.singleIngredientVolumeCalculatedEvent(null);
    }
    this.clearingInProgress = false;
  },

  clearFieldValues: function()
  {
    this._clearFieldValues();
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

  setFormula: function(formula)
  {
    this.formulaField.setValue(formula);
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
      this.formulaField.show();
      this.formulaUnitPane.show();
    }
    else
    {
      this.formulaField.hide();
      this.formulaUnitPane.hide();
    }
  },

  presentContinuousInfusionFields: function(continuousInfusion)
  {
    this._setDurationVisible(!continuousInfusion);
  }
});
