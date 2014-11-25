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

Class.define('app.views.medications.ordering.DosePane', 'tm.jquery.Container', {

  /** configs */
  view: null,
  pack: 'start',
  medicationData: null,
  doseNumerator: null,
  denominatorAlwaysVolume: false,
  hideDenominator: false,
  hideUnit: false,
  numeratorChangeEvent: null, //optional
  volumeChangedEvent: null, //optional
  focusLostEvent: null, //optional
  numeratorFocusLostEvent: null, //optional
  denominatorFocusLostEvent: null, //optional
  /** privates */
  strengthNumeratorUnit: null,
  strengthDenominatorUnit: null,
  /** privates: components */
  numeratorField: null,
  numeratorUnitLabel: null,
  fractionLine: null,
  denominatorField: null,
  denominatorUnitLabel: null,
  /** statics */
  LABEL_PADDING: "6 0 0 5",

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(new tm.jquery.HFlexboxLayout({
      alignment: new tm.jquery.FlexboxLayoutAlignment({
        pack: this.pack,
        align: 'start'
      })
    }));
    this._buildComponents();
    this._buildGui();
    if (this.medicationData)
    {
      this.setMedicationData(this.medicationData);
      this.setDoseNumerator(this.doseNumerator);
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.numeratorField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, '0 0 0 0');
    this.numeratorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._numeratorFieldChangedAction();
    });
    this.numeratorField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.denominatorField.focus();
      if (self.focusLostEvent && self.denominatorField.isHidden())
      {
        self.focusLostEvent();
      }
      if (self.numeratorFocusLostEvent)
      {
        self.numeratorFocusLostEvent(self);
      }
    });
    this.numeratorUnitLabel = new tm.jquery.Container({cls: 'TextData', padding: this.LABEL_PADDING});
    this.fractionLine = new tm.jquery.Container({cls: 'TextData', html: '/', padding: this.LABEL_PADDING});
    this.denominatorField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, '0 0 0 5');
    this.denominatorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._denominatorFieldChangedAction();
    });
    this.denominatorField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.numeratorField.focus();
      if (self.focusLostEvent)
      {
        self.focusLostEvent();
      }
      if (self.denominatorFocusLostEvent)
      {
        self.denominatorFocusLostEvent(self);
      }
    });
    this.denominatorUnitLabel = new tm.jquery.Container({cls: 'TextData', padding: this.LABEL_PADDING});
  },

  _buildGui: function()
  {
    this.add(this.numeratorField);
    this.add(this.numeratorUnitLabel);
    this.add(this.fractionLine);
    this.add(this.denominatorField);
    this.add(this.denominatorUnitLabel);
  },

  _numeratorFieldChangedAction: function()
  {
    if (this.medicationData)
    {
      var definingIngredient = tm.views.medications.MedicationUtils.getDefiningIngredient(this.medicationData);
      var numerator = this.numeratorField.getValue();
      if (definingIngredient && tm.jquery.Utils.isNumeric(numerator))
      {
        if (definingIngredient.strengthDenominator)
        {
          var denominator = numerator * definingIngredient.strengthDenominator / definingIngredient.strengthNumerator;
          this.denominatorField.setValue(denominator);
        }
      }
      else
      {
        if (this.denominatorField.getValue() != null)
        {
          this.denominatorField.setValue(null);
        }
      }
      if (this.numeratorChangeEvent)
      {
        this.numeratorChangeEvent();
      }
    }
  },

  _denominatorFieldChangedAction: function()
  {
    if (this.medicationData)
    {
      var definingIngredient = tm.views.medications.MedicationUtils.getDefiningIngredient(this.medicationData);
      var denominator = this.denominatorField.getValue();
      if (definingIngredient && tm.jquery.Utils.isNumeric(denominator))
      {
        if (definingIngredient.strengthNumerator)
        {
          var numerator = definingIngredient.strengthNumerator * denominator / definingIngredient.strengthDenominator;
          this.numeratorField.setValue(numerator);
        }
      }
      else
      {
        if (this.numeratorField.getValue() != null)
        {
          this.numeratorField.setValue(null);
        }
      }
      if (this.volumeChangedEvent)
      {
        this.volumeChangedEvent();
      }
    }
  },

  _prepareFields: function()
  {
    this.numeratorField.show();
    this.numeratorUnitLabel.show();
    this.fractionLine.show();
    this.denominatorField.show();
    this.denominatorUnitLabel.show();

    this.numeratorUnitLabel.setHtml(this.strengthNumeratorUnit);

    if (this.hideDenominator)
    {
      this.fractionLine.hide();
      this.denominatorField.hide();
      this.denominatorUnitLabel.hide();
    }
    else if (this.denominatorAlwaysVolume)
    {
      this.denominatorUnitLabel.setHtml('ml');
      var solution = this.medicationData && this.medicationData.medication && this.medicationData.medication.medicationType == 'SOLUTION';
      if (this.strengthDenominatorUnit == null || solution)  //no ingredient or ingredient without denominator or therapy is a solution
      {
        this.fractionLine.hide();
        this.numeratorField.hide();
        this.numeratorUnitLabel.hide();
      }
    }
    else
    {
      if (this.strengthDenominatorUnit)
      {
        this.denominatorUnitLabel.setHtml(this.strengthDenominatorUnit);
      }
      else
      {
        this.fractionLine.hide();
        this.denominatorField.hide();
        this.denominatorUnitLabel.hide();
      }
    }
    if (this.hideUnit)
    {
      this.numeratorUnitLabel.hide();
    }
  },

  /** public methods */
  setMedicationData: function(medicationData)
  {
    this.medicationData = medicationData;
    this.clear();
    if (medicationData)
    {
      this.strengthNumeratorUnit = tm.views.medications.MedicationUtils.getStrengthNumeratorUnit(this.medicationData);
      this.strengthDenominatorUnit = tm.views.medications.MedicationUtils.getStrengthDenominatorUnit(this.medicationData);
      this._prepareFields();
    }
  },

  //use this for uncoded medications only (universal forms)
  setUnits: function(numeratorUnit, denominatorUnit)
  {
    this.strengthNumeratorUnit = numeratorUnit;
    this.strengthDenominatorUnit = denominatorUnit;
    this._prepareFields();
  },

  clear: function()
  {
    this.numeratorField.setValue(null);
    this.denominatorField.setValue(null);
  },

  getDose: function()
  {
    return {
      quantity: this.numeratorField.getValue(),
      quantityDenominator: this.denominatorField.getValue()
    };
  },

  getDoseWithUnits: function()
  {
    return {
      quantity: this.numeratorField.getValue(),
      quantityUnit: this.strengthNumeratorUnit,
      quantityDenominator: this.denominatorField.getValue(),
      denominatorUnit: this.strengthDenominatorUnit
    };
  },

  setDoseDenominator: function(denominator)
  {
    this.denominatorField.setValue(denominator);
  },

  setDoseNumerator: function(doseNumerator)
  {
    this.numeratorField.setValue(doseNumerator);
    this._numeratorFieldChangedAction();
  },

  getDosePaneValidations: function()
  {
    var self = this;
    var formFields = [];
    if (!this.numeratorField.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.numeratorField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.numeratorField.getValue();
          if (value == null || value <= 0)
          {
            return null;
          }
          return true;
        }
      }));
    }
    if (!this.denominatorField.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.denominatorField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.denominatorField.getValue();
          if (value == null || value <= 0)
          {
            return null;
          }
          return true;
        }
      }));
    }
    return formFields;
  },

  requestFocusToDose: function()
  {
    if (!this.numeratorField.isHidden())
    {
      this.numeratorField.focus();
    }
    else if (!this.denominatorField.isHidden())
    {
      this.denominatorField.focus();
    }
  },

  requestFocusToNumerator: function()
  {
    this.numeratorField.focus();
  },

  requestFocusToDenominator: function()
  {
    this.denominatorField.focus();
  },

  setPaneEditable: function(editable)
  {
    this.numeratorField.setEnabled(editable);
    this.denominatorField.setEnabled(editable);
  }
});