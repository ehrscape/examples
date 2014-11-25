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

Class.define('app.views.medications.ordering.InfusionRateFormulaUnitPane', 'tm.jquery.Container', {
  scrollable: "visible",

  /** configs */
  view: null,
  formulaUnitChangeEvent: null,
  /** privates */
  defaultValue: null,
  heightDefined: null,
  /** privates: components */
  formulaUnitCombo: null,
  formulaUnitLabel: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.heightDefined = this.view.getPatientData().heightInCm != null;
    this.setLayout(tm.jquery.HFlexboxLayout.create("start", "start"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.formulaUnitCombo = new tm.jquery.ComboBox({
      width: 120,
      displayPropertyName: "displayUnit",
      allowSingleDeselect: false,
      multiple: false
    });
    this.formulaUnitCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.formulaUnitChangeEvent();
    });

    this.formulaUnitLabel = new tm.jquery.Container({cls: 'TextData', padding: "6 0 0 0"});
    this.formulaUnitLabel.hide();
  },

  _buildGui: function()
  {
    this.add(this.formulaUnitCombo);
    this.add(this.formulaUnitLabel);
  },

  _setDefaultFormulaComboOptions: function()
  {
    this.formulaUnitCombo.removeAllOptions();
    this.defaultValue = {id: 0, displayUnit: 'mg/kg/h', massUnit: 'mg', patientUnit: 'kg', timeUnit: 'h'};
    this.formulaUnitCombo.addOption(this.defaultValue);
    this.formulaUnitCombo.addOption({id: 1, displayUnit: 'mg/h', massUnit: 'mg', patientUnit: null, timeUnit: 'h'});
    this.formulaUnitCombo.addOption({id: 2, displayUnit: 'mg/kg/min', massUnit: 'mg', patientUnit: 'kg', timeUnit: 'min'});
    this.formulaUnitCombo.addOption({id: 3, displayUnit: 'mg/kg/d', massUnit: 'mg', patientUnit: 'kg', timeUnit: 'day'});
    this.formulaUnitCombo.addOption({id: 4, displayUnit: 'mg/m2/d', massUnit: 'mg', patientUnit: 'm2', timeUnit: 'day', enabled: this.heightDefined});
    this.formulaUnitCombo.addOption({id: 5, displayUnit: 'ng/kg/min', massUnit: 'ng', patientUnit: 'kg', timeUnit: 'min'});
    this.formulaUnitCombo.addOption({id: 6, displayUnit: 'ng/kg/h', massUnit: 'ng', patientUnit: 'kg', timeUnit: 'h'});
    this.formulaUnitCombo.addOption({id: 7, displayUnit: 'µg/kg/min', massUnit: 'µg', patientUnit: 'kg', timeUnit: 'min'});
    this.formulaUnitCombo.addOption({id: 8, displayUnit: 'µg/kg/h', massUnit: 'µg', patientUnit: 'kg', timeUnit: 'h'});
    this.formulaUnitCombo.setSelections([this.defaultValue]);
  },

  _setIeFormulaComboOptions: function()
  {
    this.formulaUnitCombo.removeAllOptions();
    this.defaultValue = {id: 0, displayUnit: 'i.e./kg/h', massUnit: 'i.e.', patientUnit: 'kg', timeUnit: 'h'};
    this.formulaUnitCombo.addOption(this.defaultValue);
    this.formulaUnitCombo.addOption({id: 1, displayUnit: 'i.e./h', massUnit: 'i.e.', patientUnit: null, timeUnit: 'h'});
    this.formulaUnitCombo.addOption({id: 2, displayUnit: 'i.e./kg/d', massUnit: 'i.e.', patientUnit: 'kg', timeUnit: 'day'});
    this.formulaUnitCombo.addOption({id: 3, displayUnit: 'i.e./kg/min', massUnit: 'i.e.', patientUnit: 'kg', timeUnit: 'min'});
    this.formulaUnitCombo.addOption({id: 4, displayUnit: 'i.e./m2/d', massUnit: 'i.e.', patientUnit: 'm2', timeUnit: 'day', enabled: this.heightDefined});
    this.formulaUnitCombo.setSelections([this.defaultValue]);
  },

  _setMmolFormulaComboOptions: function()
  {
    this.formulaUnitCombo.removeAllOptions();
    this.defaultValue = {id: 0, displayUnit: 'mmol/kg/h', massUnit: 'mmol',patientUnit: 'kg', timeUnit: 'h'};
    this.formulaUnitCombo.addOption(this.defaultValue);
    this.formulaUnitCombo.addOption({id: 1, displayUnit: 'mmol/h', massUnit: 'mmol', patientUnit: null, timeUnit: 'h'});
    this.formulaUnitCombo.addOption({id: 2, displayUnit: 'mmol/kg/min', massUnit: 'mmol', patientUnit: 'kg', timeUnit: 'min'});
    this.formulaUnitCombo.addOption({id: 3, displayUnit: 'mmol/kg/d', massUnit: 'mmol', patientUnit: 'kg', timeUnit: 'day'});
    this.formulaUnitCombo.addOption({id: 4, displayUnit: 'mmol/m2/d', massUnit: 'mmol', patientUnit: 'm2', timeUnit: 'day', enabled: this.heightDefined});
    this.formulaUnitCombo.setSelections([this.defaultValue]);
  },

  _getFormulaByDisplayValue: function(formulaUnitDisplay)
  {
    var comboOptions = this.formulaUnitCombo.getOptions();
    for (var i = 0; i < comboOptions.length; i++)
    {
      if (comboOptions[i].value != null && comboOptions[i].value.displayUnit == formulaUnitDisplay)
      {
        return comboOptions[i].value;
      }
    }
    return null;
  },

  /** public methods */
  setMedicationData: function(medicationData)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    appFactory.createConditionTask(
        function()
        {
          self._setMedicationData(medicationData);
        },
        function()
        {
          return self.formulaUnitCombo.isRendered();
        },
        20, 1000
    );
  },

  _setMedicationData: function(medicationData)
  {
    var definingIngredient = tm.views.medications.MedicationUtils.getDefiningIngredient(medicationData);
    if (definingIngredient)
    {
      if (definingIngredient.strengthNumeratorUnit == 'i.e.')
      {
        this._setIeFormulaComboOptions();
      }
      else if (definingIngredient.strengthNumeratorUnit == 'mmol')
      {
        this._setMmolFormulaComboOptions();
      }
      else
      {
        this._setDefaultFormulaComboOptions();
      }
    }
    else
    {
      this._setDefaultFormulaComboOptions();
    }
  },

  getRateFormulaUnit: function()
  {
    if (!this.formulaUnitLabel.isHidden())
    {
      return this._getFormulaByDisplayValue(this.formulaUnitLabel.getHtml());
    }
    else
    {
      return this.formulaUnitCombo.getSelections()[0];
    }
  },

  setFormulaUnit: function(formulaUnitDisplay)
  {
    var formulaUnit = this._getFormulaByDisplayValue(formulaUnitDisplay);
    this.formulaUnitCombo.setSelections([formulaUnit]);
  },

  setFormulaUnitToLabel: function(formulaUnitDisplay)
  {
    this.formulaUnitCombo.hide();
    this.formulaUnitLabel.setHtml(formulaUnitDisplay ? formulaUnitDisplay : this.formulaUnitCombo.getSelections()[0].displayUnit);
    this.formulaUnitLabel.show();
  },

  getDefaultValue: function()
  {
    if (!this.defaultValue)
    {
      this._setDefaultFormulaComboOptions();
    }
    return this.defaultValue;
  },

  requestFocus: function()
  {
    this.formulaUnitCombo._getChosenSearchInputDomElement().focus();
  }
});
