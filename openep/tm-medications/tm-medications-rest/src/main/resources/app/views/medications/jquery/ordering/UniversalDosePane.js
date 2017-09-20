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

Class.define('app.views.medications.ordering.UniversalDosePane', 'tm.jquery.Container', {
  cls: "universal-dose-pane",

  /** configs */
  view: null,
  numeratorRequired: true,
  denominatorRequired: false,
  denominatorAlwaysVolume: false,
  numeratorUnitChangeEvent: null, //optional
  denominatorChangeEvent: null, //optional
  /** privates */
  /** privates: components */
  numeratorField: null,
  numeratorUnitCombo: null,
  fractionLine: null,
  denominatorField: null,
  denominatorUnitCombo: null,
  denominatorUnitLabel: null,
  /** statics */
  LABEL_PADDING: "6 0 0 5",

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(new tm.jquery.HFlexboxLayout({gap: 5}));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.numeratorField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, 'numerator-field');
    this.numeratorUnitCombo = new tm.jquery.TypeaheadField({
      cls: "numerator-unit-combo",
      minLength: 1,
      mode: 'advanced',
      width: 140,
      items: 10000
    });
    this.numeratorUnitCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.numeratorUnitChangeEvent && self.numeratorUnitCombo.getSource().length > 0)
      {
        self.numeratorUnitChangeEvent();
      }
    });
    this.fractionLine = new tm.jquery.Container({cls: 'TextData', html: '/', padding: this.LABEL_PADDING});
    this.denominatorField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, 'denominator-field');
    this.denominatorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.denominatorChangeEvent)
      {
        self.denominatorChangeEvent();
      }
    });

    if (this.denominatorAlwaysVolume)
    {
      this.denominatorUnitLabel = new tm.jquery.Container({cls: 'TextData', html: 'mL', padding: this.LABEL_PADDING});
    }
    else
    {
      this.denominatorUnitCombo = new tm.jquery.TypeaheadField({
        cls: "denominator-unit-combo",
        minLength: 1,
        mode: 'advanced',
        margin: "0 1 0 0",
        width: 140,
        items: 10000
      });
    }

    this.view.provideUnits(
        function(units)
        {
          self.numeratorUnitCombo.setSource(units);
          self.numeratorUnitCombo.setSelection("mg");
          if (!self.denominatorAlwaysVolume)
          {
            self.denominatorUnitCombo.setSource(units);
          }
        });
  },

  _buildGui: function()
  {
    this.add(this.numeratorField);
    this.add(this.numeratorUnitCombo);
    this.add(this.fractionLine);
    this.add(this.denominatorField);
    if (this.denominatorAlwaysVolume)
    {
      this.add(this.denominatorUnitLabel);
    }
    else
    {
      this.add(this.denominatorUnitCombo);
    }
  },

  /** public methods */

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

  getNumeratorUnit: function()
  {
    return this.numeratorUnitCombo.getSelection();
  },

  getDenominatorUnit: function()
  {
    if (this.denominatorAlwaysVolume)
    {
      return 'ml';
    }
    return this.denominatorUnitCombo.getSelection();
  },

  setDenominator: function(denominator)
  {
    this.denominatorField.setValue(denominator);
  },

  getDosePaneValidations: function()
  {
    var self = this;
    var formFields = [];
    if (!this.numeratorField.isHidden() && this.numeratorRequired)
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
      formFields.push(new tm.jquery.FormField({
        component: self.numeratorUnitCombo,
        required: true,
        componentValueImplementationFn: function()
        {
          return self.numeratorUnitCombo.getSelection();
        }
      }));
    }
    if (!this.denominatorField.isHidden() && this.denominatorRequired)
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
      formFields.push(new tm.jquery.FormField({
        component: self.numeratorUnitCombo,
        required: true,
        componentValueImplementationFn: function()
        {
          return self.numeratorUnitCombo.getSelection();
        }
      }));
    }
    if (this.denominatorField.getValue() && !this.denominatorAlwaysVolume)
    {
      formFields.push(new tm.jquery.FormField({
        component: self.denominatorUnitCombo,
        required: true,
        componentValueImplementationFn: function()
        {
          return self.denominatorUnitCombo.getSelection();
        }
      }));
    }
    if (this.numeratorField.getValue())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.numeratorUnitCombo,
        required: true,
        componentValueImplementationFn: function()
        {
          return self.numeratorUnitCombo.getSelection();
        }
      }));
    }
    return formFields;
  },

  setVolumeEditable: function(editable)
  {
    this.denominatorField.setEnabled(editable);
  },

  setValue: function(numerator, numeratorUnit, denominator, denominatorUnit)
  {
    this.numeratorField.setValue(numerator);
    this.numeratorUnitCombo.setSelection(numeratorUnit);
    this.denominatorField.setValue(denominator);
    if (!this.denominatorAlwaysVolume)
    {
      this.denominatorUnitCombo.setSelection(denominatorUnit);
    }
  }
});
