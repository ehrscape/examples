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
  cls: "infusion-rate-formula-unit-pane",
  scrollable: "visible",

  /** configs */
  view: null,
  formulaUnitChangeEvent: null,
  /** privates */
  defaultValue: null,
  heightDefined: null,
  dropdownAppendTo: false,
  /** privates: components */
  formulaUnitCombo: null,
  formulaUnitLabel: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.heightDefined = this.view.getPatientHeightInCm() != null;
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.formulaUnitCombo = new tm.jquery.SelectBox({
      cls: "formula-unit-combo",
      width: 151,
      appendTo: this.getDropdownAppendTo(),
      allowSingleDeselect: false,
      multiple: false,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
      }
    });
    this.formulaUnitCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.formulaUnitChangeEvent();
    });

    this.formulaUnitLabel = new tm.jquery.Container({
      cls: 'TextData formula-unit-label',
      hidden: true
    });
  },

  _buildGui: function()
  {
    this.add(this.formulaUnitCombo);
    this.add(this.formulaUnitLabel);
  },

  _setGramFormulaComboOptions: function(continuousInfusion, preventEvent)
  {
    var previousSelectedOptions = this.formulaUnitCombo.getSelections();
    this.formulaUnitCombo.removeAllOptions();
    this.defaultValue = {id: 0, displayUnit: 'mg/kg/h', massUnit: 'mg', patientUnit: 'kg', timeUnit: 'h'};
    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(this.defaultValue, this.defaultValue.displayUnit));
    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
        {id: 1, displayUnit: 'mg/h', massUnit: 'mg', patientUnit: null, timeUnit: 'h'}, 'mg/h'));
    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
        {id: 2, displayUnit: 'mg/kg/min', massUnit: 'mg', patientUnit: 'kg', timeUnit: 'min'}, 'mg/kg/min'));
    if (continuousInfusion === true)
    {
      this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
          {id: 3, displayUnit: 'mg/kg/d', massUnit: 'mg', patientUnit: 'kg', timeUnit: 'day'}, 'mg/kg/d'));
      this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
          {id: 4, displayUnit: 'mg/m2/d', massUnit: 'mg', patientUnit: 'm2', timeUnit: 'day'}, 'mg/m2/d',
          null, null, false, this.heightDefined));
    }
    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
        {id: 5, displayUnit: 'nanogram/kg/min', massUnit: 'nanogram', patientUnit: 'kg', timeUnit: 'min'}, 'nanogram/kg/min'));
    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
        {id: 6, displayUnit: 'nanogram/kg/h', massUnit: 'nanogram', patientUnit: 'kg', timeUnit: 'h'}, 'nanogram/kg/h'));
    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
        {id: 7, displayUnit: 'microgram/kg/min', massUnit: 'microgram', patientUnit: 'kg', timeUnit: 'min'}, 'microgram/kg/min'));
    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
        {id: 8, displayUnit: 'microgram/kg/h', massUnit: 'microgram', patientUnit: 'kg', timeUnit: 'h'}, 'microgram/kg/h'));
    this._setDefaultSelection(previousSelectedOptions, preventEvent);
  },

  _setFormulaComboOptions: function(unit, continuousInfusion, preventEvent)
  {
    var previousSelectedOptions = this.formulaUnitCombo.getSelections();
    this.formulaUnitCombo.removeAllOptions();
    this.defaultValue = {id: 0, displayUnit: unit + '/kg/h', massUnit: unit, patientUnit: 'kg', timeUnit: 'h'};
    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(this.defaultValue, this.defaultValue.displayUnit));
    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
        {id: 1, displayUnit: unit + '/h', massUnit: unit, patientUnit: null, timeUnit: 'h'}, unit + '/h'));
    if (continuousInfusion === true)
    {
      this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
          {id: 2, displayUnit: unit + '/kg/d', massUnit: unit, patientUnit: 'kg', timeUnit: 'day'}, unit + '/kg/d'));
    }

    this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
        {id: 3, displayUnit: unit + '/kg/min', massUnit: unit, patientUnit: 'kg', timeUnit: 'min'}, unit + '/kg/min'));
    if (continuousInfusion === true)
    {
      this.formulaUnitCombo.addOption(tm.jquery.SelectBox.createOption(
          {
            id: 4,
            displayUnit: unit + '/m2/d',
            massUnit: unit,
            patientUnit: 'm2',
            timeUnit: 'day'
          }, unit + '/m2/d', null, null, false, this.heightDefined));
    }
    this._setDefaultSelection(previousSelectedOptions, preventEvent);
  },

  _setDefaultSelection: function(previousSelectedOptions, preventEvent)
  {
    var self = this;
    var preselect = null;
    if (previousSelectedOptions.length > 0)
    {
      var previousSelection = previousSelectedOptions[0];
      for (var i = 0; i < this.formulaUnitCombo.getOptions().length; i++)
      {
        var option = this.formulaUnitCombo.getOptions()[i];
        if (option.value.id == previousSelection.id)
        {
          preselect = option.value;
          break;
        }
      }
    }
    this.formulaUnitCombo.setSelections(
        !tm.jquery.Utils.isEmpty(preselect) ? [preselect] : [this.defaultValue], 
        preventEvent
    );
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
  setMedicationData: function(medicationData, continuousInfusion, preventEvent)
  {
    var numeratorUnit = null;
    if (!tm.jquery.Utils.isEmpty(medicationData))
    {
      var definingIngredient = !tm.jquery.Utils.isEmpty(medicationData) ?
          medicationData.getDefiningIngredient() :
          null;
      numeratorUnit = definingIngredient ? definingIngredient.strengthNumeratorUnit : medicationData.basicUnit;
    }

    if (numeratorUnit)
    {
      if (numeratorUnit == 'g' ||
          numeratorUnit == 'gram' ||
          numeratorUnit == 'mg' ||
          numeratorUnit == 'microgram' ||
          numeratorUnit == 'nanogram')
      {
        this._setGramFormulaComboOptions(continuousInfusion, preventEvent);
      }
      else
      {
        this._setFormulaComboOptions(numeratorUnit, continuousInfusion, preventEvent);
      }
    }
    else
    {
      this._setGramFormulaComboOptions(continuousInfusion, preventEvent);
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
    var self = this;
    var appFactory = this.view.getAppFactory();
    appFactory.createConditionTask(  //wait combo to be ready
        function()
        {
          var formulaUnit = self._getFormulaByDisplayValue(formulaUnitDisplay);
          self.formulaUnitCombo.setSelections([formulaUnit]);
        },
        function()
        {
          return self.formulaUnitCombo.getSelections().length > 0;
        },
        50, 1000
    );
  },

  setFormulaUnitToLabel: function(formulaUnitDisplay)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    if (formulaUnitDisplay)
    {
      setFormulaAndShowLabel(formulaUnitDisplay);
    }
    else
    {
      appFactory.createConditionTask(
          function()
          {
            setFormulaAndShowLabel(self.formulaUnitCombo.getSelections()[0].displayUnit);
          },
          function()
          {
            return self.formulaUnitCombo.getSelections().length > 0;
          },
          50, 1000
      );
    }

    function setFormulaAndShowLabel(formula)
    {
      self.formulaUnitCombo.hide();
      self.formulaUnitLabel.setHtml(formula);
      self.formulaUnitLabel.show();
    }
  },

  getDefaultValue: function()
  {
    if (!this.defaultValue)
    {
      this._setGramFormulaComboOptions();
    }
    return this.defaultValue;
  },

  /**
   * @see tm.jquery.SelectBox#appendTo
   * @returns {Boolean|Element|String}
   */
  getDropdownAppendTo: function()
  {
    return this.dropdownAppendTo;
  },

  requestFocus: function()
  {
    if(!this.formulaUnitCombo.isHidden())
    {
      this.formulaUnitCombo.focus();
    }
  }
});
