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
  cls: "dose-pane",
  /** configs */
  view: null,
  pack: 'start',
  medicationData: null,
  doseNumerator: null,
  doseDenominator: null,
  denominatorAlwaysVolume: false,
  hideDenominator: false,
  hideUnit: false,
  numeratorChangeEvent: null, //optional
  volumeChangedEvent: null, //optional
  focusLostEvent: null, //optional
  numeratorFocusLostEvent: null, //optional
  denominatorFocusLostEvent: null, //optional
  dosageCalculationFocusLostEvent: null, //optional
  verticalLayout: false,
  addDosageCalculationPane: false,
  showDosageCalculation: false,
  addDosageCalcBtn: null,
  showDoseUnitCombos: null,
  showRounding: false,
  doseRangeEnabled: false,
  /** privates */
  strengthNumeratorUnit: null,
  strengthDenominatorUnit: null,
  /** privates: components */
  numeratorField: null,
  numeratorUnitLabel: null,
  numeratorUnitField: null,
  fractionLine: null,
  dosageCalculationFractionLine: null,
  denominatorField: null,
  denominatorUnitLabel: null,
  denominatorUnitField: null,
  dosageCalculationField: null,
  dosageCalculationUnitCombo: null,
  dosageCalculationUnitLabel: null,
  numeratorRoundingTooltipAllowed: false,
  denominatorRoundingTooltipAllowed: false,
  _numeratorRoundingTooltipTimer: null,
  _denominatorRoundingTooltipTimer: null,
  dosageRoundingValidationForm: null,
  dosageCalculationBtn: null,
  /** statics */
  LABEL_PADDING: "5 0 0 5",

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (this.verticalLayout)
    {
      this.LABEL_PADDING = 0;
    }
    this._buildComponents();
    if (this.verticalLayout)
    {
      this.setLayout(tm.jquery.VFlexboxLayout.create("center", "stretch", 0));
      this._buildVerticalGui();
    }
    else
    {
      this.setLayout(tm.jquery.HFlexboxLayout.create(this.pack, "center"));
      this._buildGui();
    }
    if (this.medicationData)
    {
      this.setMedicationData(this.medicationData);
      this.setDoseNumerator(this.doseNumerator, true);
      if (this.doseDenominator)
      {
        this.setDoseDenominator(this.doseDenominator);
      }
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.dosageRoundingValidationForm = new tm.jquery.Form({
      showTooltips: false,
      onValidationSuccess: function()
      {
        if (!self.numeratorField.isHidden())
        {
          self.numeratorField.setTooltip(null);
        }
        if (!self.denominatorField.isHidden())
        {
          self.denominatorField.setTooltip(null);
        }
      },
      onValidationError: function()
      {
        if (!self.numeratorField.isHidden())
        {
          self._attachDoseNotRoundedTooltip(self.numeratorField);
        }
        if (!self.denominatorField.isHidden())
        {
          self._attachDoseNotRoundedTooltip(self.denominatorField);
        }
        return false;
      }
    });

    this.numeratorField = this.isDoseRangeEnabled() ?
        new app.views.medications.common.RangeField({
          cls: 'numerator-field',
          formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2},
          width: 110
        }) :
        tm.views.medications.MedicationUtils.createNumberField('n2', 68, 'numerator-field');
    
    this.numeratorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._numeratorFieldChangedAction();
      self._applyAfterFocusBorders(component);
    });
    this.numeratorField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      if (!self.denominatorField.isHidden())
      {
        self.denominatorField.focus();
      }
      else if (!self.dosageCalculationField.isHidden())
      {
        self.dosageCalculationField.focus();
      }
      else
      {
        self._setupDosageRoundingValidation();
        self.dosageRoundingValidationForm.submit();
      }
      if (self.focusLostEvent && self.denominatorField.isHidden() && self.dosageCalculationField.isHidden())
      {
        self.focusLostEvent();
      }
      if (self.numeratorFocusLostEvent)
      {
        self.numeratorFocusLostEvent(self);
      }
    });

    this.numeratorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function()
    {
      self._allowNumeratorRoundingTooltip();
      if (self.showRounding === true && self._isDoseRoundingPossible() == true)
      {
        var definingIngredient = self.medicationData.getDefiningIngredient();
        if (definingIngredient && definingIngredient.strengthNumerator)
        {
          var roundedDosages = self._calculateDosageRounding(self.numeratorField.getValue(), definingIngredient.strengthNumerator);
        }

        if (!tm.jquery.Utils.isEmpty(roundedDosages) && roundedDosages.isDoseRounded == false)
        {
          if (self.numeratorRoundingTooltipAllowed)
          {
            self._showNumeratorRoundingTooltip(roundedDosages.roundedDoseUp, roundedDosages.roundedDoseDown);
          }
        }
      }
    });

    this.numeratorUnitLabel = new tm.jquery.Container({cls: 'TextData numerator-unit-label', padding: this.LABEL_PADDING});
    this.numeratorUnitField = new tm.jquery.TypeaheadField({
      cls: "numerator-unit-field",
      margin: '0 0 0 8',
      minLength: 1,
      mode: 'advanced',
      width: 140,
      items: 10000,
      hidden: true
    });
    this.fractionLine = this._getFractionLine();
    this.dosageCalculationFractionLine = this._getFractionLine();
    
    this.denominatorField = this.isDoseRangeEnabled() ?
        new app.views.medications.common.RangeField({
          cls: 'denominator-field',
          formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2},
          width: 110
        }) : tm.views.medications.MedicationUtils.createNumberField('n2', 68, 'denominator-field');
    this.denominatorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._denominatorFieldChangedAction();
      self._applyAfterFocusBorders(component);

    });
    this.denominatorField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      if (self.focusLostEvent)
      {
        self.focusLostEvent();
      }
      if (!self.dosageCalculationField.isHidden())
      {
        self.dosageCalculationField.focus();
      }
      else
      {
        self._setupDosageRoundingValidation();
        self.dosageRoundingValidationForm.submit();
      }
      if (self.denominatorFocusLostEvent)
      {
        self.denominatorFocusLostEvent(self);
      }

    });

    this.denominatorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function()
    {
      self._allowDenominatorRoundingTooltip();
      if (self.showRounding === true && self._isDoseRoundingPossible() == true)
      {
        var definingIngredient = self.medicationData.getDefiningIngredient();
        var roundedDosages = null;
        if (definingIngredient && definingIngredient.strengthDenominator)
        {
          roundedDosages = self._calculateDosageRounding(self.denominatorField.getValue(), definingIngredient.strengthDenominator);
        }
        if (!tm.jquery.Utils.isEmpty(roundedDosages) && roundedDosages.isDoseRounded == false)
        {
          if (self.denominatorRoundingTooltipAllowed)
          {
            self._showDenominatorRoundingTooltip(roundedDosages.roundedDoseUp, roundedDosages.roundedDoseDown);
          }
        }
      }
    });

    this.denominatorUnitLabel = new tm.jquery.Container({
      cls: 'TextData denominator-unit-label',
      padding: this.LABEL_PADDING
    });
    this.denominatorUnitField = new tm.jquery.TypeaheadField({
      cls: "denominator-unit-field",
      minLength: 1,
      mode: 'advanced',
      margin: "0 1 0 8",
      width: 140,
      items: 10000,
      hidden: true
    });

    this.dosageCalculationBtn = new tm.jquery.Button({
      cls: "dosage-calculation-icon dosage-calculation-button",
      alignSelf: "center",
      handler: function()
      {
        self.showDosageCalculationFields();
        self._applyAfterFocusBorders(self.dosageCalculationField);
        self._showDosageCalcUnitComboOrLabel();
        self.setDosageCalculationFieldValue();
        self.requestFocusToDosageCalculation();
        self.dosageCalculationBtn.hide();
      },
      hidden: true
    });
    this.dosageCalculationField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, 'dosage-calculation-field');

    this.dosageCalculationField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      if (self.focusLostEvent)
      {
        self.focusLostEvent();
      }
      if (self.dosageCalculationFocusLostEvent)
      {
        self.dosageCalculationFocusLostEvent(self);
      }
      self._setupDosageRoundingValidation();
      self.dosageRoundingValidationForm.submit();
    });

    this.dosageCalculationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (!tm.jquery.Utils.isEmpty(self.dosageCalculationUnitCombo) && !self.dosageCalculationUnitCombo.isHidden())
      {
        if (self.dosageCalculationUnitCombo.getSelections().length > 0)
        {
          self._dosageCalculationFieldChangedAction(
              self.dosageCalculationUnitCombo.getSelections()[0].doseUnit,
              self.dosageCalculationUnitCombo.getSelections()[0].patientUnit
          );
        }
      }
      else if (!tm.jquery.Utils.isEmpty(self.dosageCalculationUnitLabel) && !self.dosageCalculationUnitLabel.isHidden())
      {
        self._dosageCalculationFieldChangedAction(
            self.dosageCalculationUnitLabel.getData().doseUnit,
            self.dosageCalculationUnitLabel.getData().patientUnit
        );

      }
      self._applyAfterFocusBorders(component);
    });

    this.dosageCalculationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function()
    {
      self._allowNumeratorOrDenominatorRoundingTooltip();
    });

    this.dosageCalculationUnitCombo = new tm.jquery.SelectBox({
      cls: "dosage-calculation-unit-combo",
      width: 140,
      allowSingleDeselect: false,
      multiple: false,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
      },
      hidden: true
    });
    this.dosageCalculationUnitCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (component.getSelections().length > 0)
      {
        self._calculateDosageCalculationFromFormula(component.getSelections()[0]);
      }
    });
    this.dosageCalculationUnitLabel = new tm.jquery.Container({
      cls: "TextData dosage-calculation-unit-label",
      padding: '5 0 0 5',
      hidden: true
    });

    this.view.provideUnits(
        function(units)
        {
          self.numeratorUnitField.setSource(units);
          self.numeratorUnitField.setSelection("mg");
          if (!self.denominatorAlwaysVolume)
          {
            self.denominatorUnitField.setSource(units);
          }
        });
  },

  _buildGui: function()
  {
    this.add(this.numeratorField);
    this.add(this.numeratorUnitLabel);
    this.add(this.numeratorUnitField);
    this.add(this.fractionLine);
    this.add(this.denominatorField);
    this.add(this.denominatorUnitLabel);
    this.add(this.denominatorUnitField);
    if (this.addDosageCalcBtn === true)
    {
      this.add(this.dosageCalculationBtn);
    }
    if (this.addDosageCalculationPane)
    {
      this.add(this.dosageCalculationFractionLine);
      this.add(this.dosageCalculationField);
      this.add(this.dosageCalculationUnitCombo);
      this.add(this.dosageCalculationUnitLabel);
    }
  },
  _buildVerticalGui: function()
  {
    var numeratorContainer = new tm.jquery.Container({
      cls: "numerator-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 5)
    });
    this.numeratorField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    numeratorContainer.add(this.numeratorField);
    numeratorContainer.add(this.numeratorUnitLabel);
    numeratorContainer.add(this._createVerticalSpacer());
    this.add(numeratorContainer);
    var denominatorContainer = new tm.jquery.Container({
      cls: "denominator-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 5)
    });
    this.denominatorField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    denominatorContainer.add(this.denominatorField);
    denominatorContainer.add(this.denominatorUnitLabel);
    denominatorContainer.add(this._createVerticalSpacer());
    this.add(denominatorContainer);
  },
  _createVerticalSpacer: function()
  {
    return new tm.jquery.Spacer({type: 'horizontal', size: 2});
  },
  _getFractionLine: function()
  {
    return new tm.jquery.Container({cls: 'TextData dose-pane-fraction-line', html: '/', padding: this.LABEL_PADDING});
  },

  _calculateDosageRounding: function(fieldValue, basicStrength)
  {
    var roundingFactor = this.medicationData.roundingFactor;
    if (!tm.jquery.Utils.isEmpty(fieldValue) && tm.jquery.Utils.isNumeric(fieldValue) && !tm.jquery.Utils.isEmpty(roundingFactor))
    {
      var rounding = basicStrength * roundingFactor;
      var roundedDoseUp = Math.round((Math.ceil(fieldValue / rounding) * rounding) * 10000000000) / 10000000000;
      var roundedDoseDown = Math.round((Math.floor(fieldValue / rounding) * rounding) * 10000000000) / 10000000000;
      var isDoseRounded = false;
      if (fieldValue == roundedDoseUp || fieldValue == roundedDoseDown)
      {
        isDoseRounded = true;
      }
      return {roundedDoseUp: roundedDoseUp, roundedDoseDown: roundedDoseDown, isDoseRounded: isDoseRounded};
    }
    return null;
  },

  /**
   *
   * @param {tm.jquery.Component} selectedField
   * @private
   */
  _attachDoseNotRoundedTooltip: function(selectedField)
  {
    if (!tm.jquery.ClientUserAgent.isTablet())
    {
      selectedField.setTooltip(this._getDoseNotRoundedHintTooltip());
    }
  },

  _getDoseNotRoundedHintTooltip: function()
  {
    return new tm.views.medications.MedicationUtils.createTooltip(
        this.view.getDictionary("strength.dose.not.matched"),
        "bottom",
        this.view);
  },

  _validateNumeratorAndDenominator: function()
  {
    if (this.medicationData)
    {
      this._setupDosageRoundingValidation();
      this.dosageRoundingValidationForm.submit();
    }
  },

  _numeratorFieldChangedAction: function()
  {
    var self = this;
    if (this.medicationData)
    {
      var definingIngredient = this.medicationData.getDefiningIngredient();
      var numerator = this.numeratorField.getValue();

      if (tm.jquery.Utils.isNumeric(numerator) && this.showRounding && this._isDoseRoundingPossible())
      {
        this._validateNumeratorAndDenominator();
        var roundedDosages = null;
        if (definingIngredient && definingIngredient.strengthNumerator)
        {
          roundedDosages = this._calculateDosageRounding(numerator, definingIngredient.strengthNumerator);
        }

        if (!tm.jquery.Utils.isEmpty(roundedDosages) && !roundedDosages.isDoseRounded)
        {
          var roundedDoseUp = roundedDosages.roundedDoseUp;
          var roundedDoseDown = roundedDosages.roundedDoseDown;

          if (self.numeratorRoundingTooltipAllowed === true)
          {
            self._showNumeratorRoundingTooltip(roundedDoseUp, roundedDoseDown);
          }
        }
      }

      self.calculateAndSetDoseDenominator(true);

      if (this.volumeChangedEvent && !this.medicationData.getMedication().isMedicationUniversal())
      {
        this.volumeChangedEvent();
      }

      if (!this.dosageCalculationField.isHidden())
      {
        this.setDosageCalculationFieldValue();
      }
      if (this.numeratorChangeEvent)
      {
        this.numeratorChangeEvent();
      }
    }
  },

  setDosageCalculationFieldValue: function()
  {
    if (!this.dosageCalculationUnitCombo.isHidden())
    {
      if (this.dosageCalculationUnitCombo.getSelections().length > 0)
      {
        this._calculateDosageCalculationFromFormula(this.dosageCalculationUnitCombo.getSelections()[0]);
      }
      else if (this.dosageCalculationUnitCombo.getOptions().length > 0)
      {
        this._calculateDosageCalculationFromFormula(this.dosageCalculationUnitCombo.getOptions()[0].value);
      }
    }
    else if (!this.dosageCalculationUnitLabel.isHidden())
    {
      this._calculateDosageCalculationFromFormula(this.dosageCalculationUnitLabel.getData());
    }
  },

  _denominatorFieldChangedAction: function()
  {
    var self = this;
    if (this.medicationData)
    {
      var definingIngredient = this.medicationData.getDefiningIngredient();
      var denominator = this.denominatorField.getValue();
      var isDenominatorRange = denominator instanceof app.views.medications.common.dto.Range;

      if (tm.jquery.Utils.isNumeric(denominator) && this.showRounding && this._isDoseRoundingPossible() &&
          !tm.jquery.Utils.isEmpty(definingIngredient.strengthDenominator))
      {
        this._setupDosageRoundingValidation();
        this.dosageRoundingValidationForm.submit();
        var roundedDosages = null;
        if (definingIngredient && definingIngredient.strengthDenominator)
        {
          roundedDosages = this._calculateDosageRounding(this.denominatorField.getValue(), definingIngredient.strengthDenominator);
        }

        if (!tm.jquery.Utils.isEmpty(roundedDosages) && !roundedDosages.isDoseRounded)
        {
          var roundedDoseUp = roundedDosages.roundedDoseUp;
          var roundedDoseDown = roundedDosages.roundedDoseDown;
          if (self.denominatorRoundingTooltipAllowed)
          {
            self._showDenominatorRoundingTooltip(roundedDoseUp, roundedDoseDown);
          }
        }
      }

      if (!this.numeratorField.isHidden() && !this.medicationData.getMedication().isMedicationUniversal())
      {
        if (definingIngredient)
        {
          if (definingIngredient.strengthNumerator)
          {
            if (isDenominatorRange)
            {
              var numeratorRange = app.views.medications.common.dto.Range.createStrict(
                Math.round(
                    (definingIngredient.strengthNumerator / definingIngredient.strengthDenominator * denominator.getMin()) *
                    10000000000) / 10000000000,
                Math.round(
                    (definingIngredient.strengthNumerator / definingIngredient.strengthDenominator * denominator.getMax()) *
                    10000000000) / 10000000000
              );
              this.numeratorField.setValue(numeratorRange);
            }
            else if (tm.jquery.Utils.isNumeric(denominator))
            {
              var numerator = Math.round(
                      (definingIngredient.strengthNumerator / definingIngredient.strengthDenominator * denominator) *
                      10000000000) / 10000000000;
              this.numeratorField.setValue(numerator);
            }
            else if (!tm.jquery.Utils.isNumeric(denominator))
            {
              this._clearNumerator();
            }
          }
        }
        else if (this.medicationData.getMedication() &&
            (!this.medicationData.getMedication().isMedicationUniversal() ||
            (!tm.jquery.Utils.isNumeric(denominator) && !isDenominatorRange)))
        {
          this._clearNumerator()
        }
      }
      else if (this.volumeChangedEvent)
      {
        this.volumeChangedEvent();
      }
    }
  },

  _showNumeratorRoundingTooltip: function(roundedDoseUp, roundedDoseDown)
  {
    var self = this;
    var appFactory = self.view.getAppFactory();
    this._attachDoseNotRoundedTooltip(this.denominatorField);

    var tooltipButtonsContainer = new tm.jquery.Container({
      cls: "rounding-tooltip-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 5)
    });
    var roundingLabel = new tm.jquery.Container({
      cls: "PopupHeading1 rounding-tooltip-heading",
      html: this.view.getDictionary("rounding"),
      alignSelf: "center"
    });
    tooltipButtonsContainer.add(roundingLabel);
    var roundUpButton = new tm.jquery.Button({
      cls: "btn-flat button-align-left",
      iconCls: 'icon-round-up-arrow',
      alignSelf: 'stretch',
      text: roundedDoseUp + " " + self.numeratorUnitLabel.getHtml(),
      handler: function()
      {
        self.numeratorField.setValue(roundedDoseUp);
        self.numeratorField.focus();
      }
    });
    tooltipButtonsContainer.add(roundUpButton);

    if (roundedDoseDown > 0)
    {
      var roundDownButton = new tm.jquery.Button({
        cls: "btn-flat button-align-left",
        iconCls: 'icon-round-down-arrow',
        alignSelf: 'stretch',
        text: roundedDoseDown + " " + self.numeratorUnitLabel.getHtml(),
        handler: function()
        {
          self.numeratorField.setValue(roundedDoseDown);
          self.numeratorField.focus();
        }
      });
      tooltipButtonsContainer.add(roundDownButton);
    }

    var numeratorRoundingTooltip = appFactory.createDefaultPopoverTooltip(null, null, tooltipButtonsContainer);
    numeratorRoundingTooltip.setPlacement("bottom");
    numeratorRoundingTooltip.setTrigger("manual");

    numeratorRoundingTooltip.onHide = function(component)
    {
      // can't figure out why onHide executes as soon as we set the tooltip, even before it's shown for the first time
      // so double check if the field has lost focus before replacing the tooltip
      if (!$(self.numeratorField.getDom()).is(":focus") && numeratorRoundingTooltip)
      {
        numeratorRoundingTooltip = null;
        var definingIngredient = self.medicationData.getDefiningIngredient();
        var calculatedDosages = null;
        if (definingIngredient && definingIngredient.strengthNumerator)
        {
          calculatedDosages = self._calculateDosageRounding(
              self.numeratorField.getValue(),
              definingIngredient.strengthNumerator
          );
        }
        self.numeratorField.setTooltip(null);
        if (!tm.jquery.Utils.isEmpty(calculatedDosages) && calculatedDosages.isDoseRounded == false)
        {
          self._attachDoseNotRoundedTooltip(self.numeratorField);
        }
      }
    };
    this.numeratorField.setTooltip(numeratorRoundingTooltip);

    setTimeout(function()
    {
      if (self.numeratorRoundingTooltipAllowed && 
          numeratorRoundingTooltip && self.numeratorField.getTooltip() === numeratorRoundingTooltip)
      {
        numeratorRoundingTooltip.show();
      }
    }, 0);
  },

  _showDenominatorRoundingTooltip: function(roundedDoseUp, roundedDoseDown)
  {
    var self = this;
    var appFactory = self.view.getAppFactory();
    this._attachDoseNotRoundedTooltip(this.numeratorField);

    var tooltipButtonsContainer = new tm.jquery.Container({
      cls: "rounding-tooltip-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 5)
    });
    var roundingLabel = new tm.jquery.Container({
      cls: "PopupHeading1 rounding-tooltip-heading",
      html: this.view.getDictionary("rounding"),
      alignSelf: "center"
    });
    tooltipButtonsContainer.add(roundingLabel);
    var roundUpButton = new tm.jquery.Button({
      cls: "btn-flat button-align-left",
      iconCls: 'icon-round-up-arrow',
      alignSelf: 'stretch',
      text: roundedDoseUp + " " + self.denominatorUnitLabel.getHtml(),
      handler: function()
      {
        self.denominatorField.setValue(roundedDoseUp);
        self.denominatorField.focus();
      }
    });
    tooltipButtonsContainer.add(roundUpButton);

    if (roundedDoseDown > 0)
    {
      var roundDownButton = new tm.jquery.Button({
        cls: "btn-flat button-align-left",
        iconCls: 'icon-round-down-arrow',
        alignSelf: 'stretch',
        text: roundedDoseDown + " " + self.denominatorUnitLabel.getHtml(),
        handler: function()
        {
          self.denominatorField.setValue(roundedDoseDown);
          self.denominatorField.focus();
        }
      });
      tooltipButtonsContainer.add(roundDownButton);
    }

    var denominatorRoundingTooltip = appFactory.createDefaultPopoverTooltip(null, null, tooltipButtonsContainer);
    denominatorRoundingTooltip.setPlacement("bottom");
    denominatorRoundingTooltip.setTrigger("manual");

    denominatorRoundingTooltip.onHide = function()
    {
      // can't figure out why onHide executes as soon as we set the tooltip, even before it's shown for the first time
      // so double check if the field has lost focus before replacing the tooltip
      if (!$(self.denominatorField.getDom()).is(":focus") && denominatorRoundingTooltip)
      {
        denominatorRoundingTooltip = null;
        var definingIngredient = self.medicationData.getDefiningIngredient();
        var calculatedDosages = null;
        if (definingIngredient && definingIngredient.strengthDenominator)
        {
          calculatedDosages = self._calculateDosageRounding(self.denominatorField.getValue(), definingIngredient.strengthDenominator);
        }
        self.denominatorField.setTooltip(null);
        if (!tm.jquery.Utils.isEmpty(calculatedDosages) && calculatedDosages.isDoseRounded === false)
        {
          self._attachDoseNotRoundedTooltip(self.denominatorField);
        }
      }
    };
    this.denominatorField.setTooltip(denominatorRoundingTooltip);

    setTimeout(function()
    {
      if (self.denominatorRoundingTooltipAllowed && 
          denominatorRoundingTooltip && self.denominatorField.getTooltip() === denominatorRoundingTooltip)
      {
        denominatorRoundingTooltip.show();
      }
    }, 0);
  },

  _dosageCalculationFieldChangedAction: function(selectedDoseUnit, selectedPatientUnit)
  {
    if (this.medicationData)
    {
      var dataForCalculation = this._getDataForCalculations();
      var dosageCalculationFieldValue = this.dosageCalculationField.getValue();

      if (tm.jquery.Utils.isNumeric(dosageCalculationFieldValue))
      {
        var dosageCalculationInDefiningUnit = tm.views.medications.MedicationUtils.convertToUnit(dosageCalculationFieldValue, selectedDoseUnit, dataForCalculation.doseUnit);
        var numerator = null;
        if (selectedPatientUnit == 'kg')
        {
          if (dataForCalculation.patientWeight)
          {
            numerator = Math.round((dosageCalculationInDefiningUnit * dataForCalculation.patientWeight) * 10000000000) / 10000000000;
            this.numeratorField.setValue(numerator);
          }
        }
        else if (selectedPatientUnit == 'm2')
        {
          if (dataForCalculation.patientBodySurfaceArea)
          {
            numerator = Math.round((dosageCalculationInDefiningUnit * dataForCalculation.patientBodySurfaceArea) * 10000000000) / 10000000000;
            this.numeratorField.setValue(numerator);
          }
        }
      }
      else
      {
        if (!this.numeratorField.isHidden())
        {
          if (this.numeratorField.getValue() != null)
          {
            this.numeratorField.setValue(null);
          }
        }
      }
    }
  },

  _setDosageCalculationComboUnits: function(doseUnit)
  {
    this.dosageCalculationUnitCombo.removeAllOptions();
    var patientHeight = this.view.getPatientHeightInCm();
    var dosageCalculationUnits = tm.views.medications.MedicationUtils.getDosageCalculationUnitOptions(this.view, doseUnit, patientHeight);
    if (dosageCalculationUnits.length == 1)
    {
      this.setDosageCalculationUnitLabel(dosageCalculationUnits[0]);
    }
    var selectedId = null;
    var selectedOption = null;
    var setOptionSelected = null;
    var selectBoxOptions = dosageCalculationUnits.map(function(option)
    {
      selectedId = option.doseUnit == doseUnit && option.patientUnit == "kg" ? option.id : selectedId;
      setOptionSelected = option.doseUnit == doseUnit && option.patientUnit == "kg";

      var currentOption = tm.jquery.SelectBox.createOption(option, option.displayUnit, null, null, setOptionSelected);
      if (setOptionSelected)
      {
        selectedOption = currentOption.value;
      }
      return currentOption;
    });
    this.dosageCalculationUnitCombo.addOptions(selectBoxOptions);

    if (!tm.jquery.Utils.isEmpty(selectedOption))
    {
      this.dosageCalculationUnitCombo.setSelections([selectedOption]);
    }
  },

  _calculateDosageCalculationFromFormula: function(selectedDoseUnit)
  {
    if (!tm.jquery.Utils.isEmpty(selectedDoseUnit))
    {
      var patientUnit = selectedDoseUnit.patientUnit;
      var doseUnit = selectedDoseUnit.doseUnit;
      var dataForCalculation = this._getDataForCalculations();
      if (dataForCalculation && dataForCalculation.patientWeight && dataForCalculation.doseUnit && dataForCalculation.dose)
      {
        var doseInUnit = tm.views.medications.MedicationUtils.convertToUnit(dataForCalculation.dose, dataForCalculation.doseUnit, doseUnit);
        if (patientUnit == 'kg')
        {
          this.dosageCalculationField.setValue(Math.round((doseInUnit / dataForCalculation.patientWeight) * 10000000000) / 10000000000, true);
        }
        else if (patientUnit == 'm2' && dataForCalculation.patientBodySurfaceArea)
        {
          this.dosageCalculationField.setValue(Math.round((doseInUnit / dataForCalculation.patientBodySurfaceArea) * 10000000000) / 10000000000, true);
        }
      }
    }
  },

  _getDataForCalculations: function()
  {
    var utils = tm.views.medications.MedicationUtils;
    var patientWeight = this.view.getReferenceWeight();
    var dose = this.numeratorField.getValue();
    var doseUnit = !tm.jquery.Utils.isEmpty(this.medicationData) ?
        this.medicationData.getStrengthNumeratorUnit() :
        null;
    var patientBodySurfaceArea = !tm.jquery.Utils.isEmpty(this.view.getPatientHeightInCm()) && !tm.jquery.Utils.isEmpty(patientWeight) ?
        utils.calculateBodySurfaceArea(this.view.getPatientHeightInCm(), patientWeight) :
        null;
    return {
      patientWeight: patientWeight,
      patientBodySurfaceArea: patientBodySurfaceArea,
      dose: dose,
      doseUnit: doseUnit
    };
  },

  _isDoseRoundingPossible: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.medicationData))
    {
      var definingIngredient = this.medicationData.getDefiningIngredient();
      var roundingFactor = this.medicationData.getRoundingFactor();
      if (!tm.jquery.Utils.isEmpty(definingIngredient))
      {
        var strengthNumerator = definingIngredient.strengthNumerator;
        if (!tm.jquery.Utils.isEmpty(strengthNumerator) && !tm.jquery.Utils.isEmpty(roundingFactor))
        {
          return true;
        }
      }
      return false;
    }
    return false;
  },

  _allowNumeratorOrDenominatorRoundingTooltip: function()
  {
    if (this.denominatorField.isHidden())
    {
      this._allowNumeratorRoundingTooltip();
    }
    else
    {
      this._allowDenominatorRoundingTooltip();
    }
  },

  _allowNumeratorRoundingTooltip: function()
  {
    this.denominatorRoundingTooltipAllowed = false;
    this.numeratorRoundingTooltipAllowed = true;
  },

  _allowDenominatorRoundingTooltip: function()
  {
    this.denominatorRoundingTooltipAllowed = true;
    this.numeratorRoundingTooltipAllowed = false;
  },

  _prepareFields: function()
  {
    var utils = tm.views.medications.MedicationUtils;
    this.numeratorField.show();
    this.numeratorUnitLabel.show();
    this.fractionLine.show();
    this.denominatorField.show();
    this.denominatorUnitLabel.show();
    if (this.isDosageCalculationPossible())
    {
      this._setDosageCalculationComboUnits(this.medicationData.getStrengthNumeratorUnit());
      if (this.addDosageCalcBtn === true)
      {
        this.dosageCalculationBtn.show();
      }
      if (this.showDosageCalculation === true)
      {
        this.showDosageCalculationFields();
        this._showDosageCalcUnitComboOrLabel();
      }
      else
      {
        this._hideDosageCalculationFields();
        this._applyAfterFocusBorders(this.numeratorField);
      }
    }
    else
    {
      this.dosageCalculationBtn.hide();
      this._hideDosageCalculationFields();
      this._applyAfterFocusBorders(this.numeratorField);
    }

    this.numeratorUnitLabel.setHtml(utils.getFormattedUnit(this.strengthNumeratorUnit));

    if (this.medicationData.getMedication() &&
        this.medicationData.getMedication().isMedicationUniversal() &&
        !this.verticalLayout &&
        this.showDoseUnitCombos !== false)
    {
      if (this.medicationData && this.medicationData.getMedication() && this.medicationData.getMedication().isSolution())  //no ingredient or ingredient without denominator or therapy is a solution
      {
        this.fractionLine.hide();
        this.denominatorField.hide();
        this.denominatorUnitField.hide();
        this.denominatorUnitLabel.hide();
      }
      else
      {
        this.numeratorUnitLabel.hide();
        this.numeratorUnitField.show();
      }
      var definingIngredient = !tm.jquery.Utils.isEmpty(this.medicationData) ?
          this.medicationData.getDefiningIngredient() : null;

      !tm.jquery.Utils.isEmpty(definingIngredient) && !tm.jquery.Utils.isEmpty(this.medicationData.getStrengthNumeratorUnit()) ?
          this.numeratorUnitField.setSelection(this.medicationData.getStrengthNumeratorUnit()) :
          this.numeratorUnitField.setSelection("mg");
      
      if (this.denominatorAlwaysVolume)
      {
        this.denominatorUnitLabel.setHtml('mL');
      }
      else
      {
        this.denominatorUnitLabel.hide();
        this.denominatorUnitField.show();
        if (!tm.jquery.Utils.isEmpty(definingIngredient))
        {
          this.denominatorUnitField.setSelection(this.medicationData.getStrengthDenominatorUnit());
        }
      }
    }
    else
    {
      this.numeratorUnitField.isRendered() ? this.numeratorUnitField.hide() : this.numeratorUnitField.setHidden(true);
      this.denominatorUnitField.isRendered() ? this.denominatorUnitField.hide() : this.denominatorUnitField.setHidden(true);
      if (this.strengthDenominatorUnit)
      {
        this.denominatorUnitLabel.setHtml(utils.getFormattedUnit(this.strengthDenominatorUnit));
      }
      else
      {
        this.fractionLine.hide();
        this.denominatorField.hide();
        this.denominatorUnitLabel.hide();
      }
    }
    if (this.hideDenominator)
    {
      this.fractionLine.hide();
      this.denominatorField.hide();
      this.denominatorUnitLabel.hide();
      this.denominatorUnitField.hide();
    }
    if (this.hideUnit)
    {
      this.numeratorUnitLabel.hide();
      this.numeratorUnitField.hide();
    }
  },

  _applyAfterFocusBorders: function(lastChangedCell)
  {
    var denominatorFieldCls = tm.jquery.Utils.isEmpty(this.denominatorField.getCls()) ? "" : this.denominatorField.getCls().replace(" softer-border", "");
    var numeratorFieldCls = tm.jquery.Utils.isEmpty(this.numeratorField.getCls()) ? "" : this.numeratorField.getCls().replace(" softer-border", "");
    var dosageCalculationFieldCls = tm.jquery.Utils.isEmpty(this.dosageCalculationField.getCls()) ? "" : this.dosageCalculationField.getCls().replace(" softer-border", "");
    if (!this.dosageCalculationField.isHidden())
    {
      this.denominatorField.setCls(denominatorFieldCls + " softer-border");
      this.numeratorField.setCls(numeratorFieldCls + " softer-border");
      this.dosageCalculationField.setCls(dosageCalculationFieldCls + " softer-border");
      lastChangedCell.setCls(lastChangedCell.getCls().replace(" softer-border", ""));
    }
    else
    {
      this.denominatorField.setCls(denominatorFieldCls);
      this.numeratorField.setCls(numeratorFieldCls);
    }
  },

  _showDosageCalcUnitComboOrLabel: function()
  {
    if (this.dosageCalculationUnitCombo.getOptions().length == 1)
    {
      this.dosageCalculationUnitCombo.hide();
      this.dosageCalculationUnitLabel.show();
    }
    else
    {
      this.dosageCalculationUnitCombo.show();
      this.dosageCalculationUnitLabel.hide();
    }
  },

  _hideDosageCalculationFields: function()
  {
    this.dosageCalculationFractionLine.hide();
    this.dosageCalculationField.hide();
    this.dosageCalculationUnitCombo.hide();
    this.dosageCalculationUnitLabel.hide();
  },

  _setupDosageRoundingValidation: function()
  {
    var self = this;
    this.dosageRoundingValidationForm.reset();
    if (!this.numeratorField.isHidden())
    {
      this.dosageRoundingValidationForm.addFormField(new tm.jquery.FormField({
        component: self.numeratorField,
        required: true,
        componentValueImplementationFn: function()
        {
          var definingIngredient = self.medicationData.getDefiningIngredient();
          var roundedDosages = null;
          if (definingIngredient && definingIngredient.strengthNumerator)
          {
            roundedDosages = self._calculateDosageRounding(self.numeratorField.getValue(), definingIngredient.strengthNumerator);
          }
          if (!tm.jquery.Utils.isEmpty(roundedDosages) && roundedDosages.isDoseRounded === false)
          {
            return null;
          }
          return true;
        }
      }));
    }
    if (!this.denominatorField.isHidden())
    {
      this.dosageRoundingValidationForm.addFormField(new tm.jquery.FormField({
        component: self.denominatorField,
        required: true,
        componentValueImplementationFn: function()
        {
          var definingIngredient = self.medicationData.getDefiningIngredient();
          var roundedDosages = null;
          if (definingIngredient && definingIngredient.strengthDenominator)
          {
            roundedDosages = self._calculateDosageRounding(self.denominatorField.getValue(), definingIngredient.strengthDenominator);
          }
          if (!tm.jquery.Utils.isEmpty(roundedDosages) && roundedDosages.isDoseRounded === false)
          {
            return null;
          }
          return true;
        }
      }));
    }
  },

  _clearNumerator: function(preventEvent)
  {
    if (this.numeratorField.getValue() != null)
    {
      this.numeratorField.setValue(null, preventEvent === true);
    }
  },

  _clearDenominator: function(preventEvent)
  {
    if (this.denominatorField.getValue() != null)
    {
      this.denominatorField.setValue(null, preventEvent === true);
    }
  },

  _clearDosageCalculation: function(preventEvent)
  {
    if (this.dosageCalculationField.getValue() != null)
    {
      this.dosageCalculationField.setValue(null, preventEvent === true);
    }
  },

  /** public methods */
  setMedicationData: function(medicationData)
  {
    this.medicationData = medicationData;
    this.clear();
    if (medicationData)
    {
      if (this.medicationData.getMedication().isMedicationUniversal() &&
          this.medicationData.getMedication() && this.medicationData.getMedication().isSolution())
      {
        this.strengthNumeratorUnit = 'ml';
      }
      else
      {
        this.strengthNumeratorUnit = !tm.jquery.Utils.isEmpty(this.medicationData.getStrengthNumeratorUnit()) ?
            this.medicationData.getStrengthNumeratorUnit() :
            this.numeratorUnitField.getSelection();
        if (this.medicationData.getMedication().isMedicationUniversal())
        {
          this.strengthDenominatorUnit = this.denominatorAlwaysVolume ? 'ml' :
              (this.medicationData.getStrengthDenominatorUnit() ?
                  this.medicationData.getStrengthDenominatorUnit() :
                  this.denominatorUnitField.getSelection());
        }
        else
        {
          this.strengthDenominatorUnit = this.medicationData.getStrengthDenominatorUnit();
        }
      }
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

  clear: function(keepUnits)
  {
    this._clearNumerator(true);
    this._clearDenominator(true);
    this._clearDosageCalculation(true);
    this.denominatorRoundingTooltipAllowed = false;
    this.numeratorRoundingTooltipAllowed = false;
    this.dosageRoundingValidationForm.reset();
    if (keepUnits !== true)
    {
      this.numeratorUnitField.setSelection("mg", true);
      this.denominatorUnitField.setSelection(null, true);
      this.numeratorUnitField.isRendered() ? this.numeratorUnitField.hide() : this.numeratorUnitField.setHidden(true);
      this.denominatorUnitField.isRendered() ? this.denominatorUnitField.hide() : this.denominatorUnitField.setHidden(true);
      this.fractionLine.isRendered() ? this.fractionLine.show() : this.fractionLine.setHidden(false);
      this.numeratorUnitLabel.isRendered() ? this.numeratorUnitLabel.show() : this.numeratorUnitLabel.setHidden(false);
      this.denominatorUnitLabel.isRendered() ? this.denominatorUnitLabel.show() : this.denominatorUnitLabel.setHidden(false);
    }
    this._hideDosageCalculationFields();
  },

  getDose: function()
  {
    var numeratorValue = !this.numeratorField.isHidden() ? this.numeratorField.getValue() : null;
    var denominatorValue = null;
    if (this.hideDenominator && this.medicationData.getStrengthDenominatorUnit() || !this.denominatorField.isHidden())
    {
      denominatorValue = this.denominatorField.getValue();
    }

    if (numeratorValue instanceof app.views.medications.common.dto.Range ||
        denominatorValue instanceof app.views.medications.common.dto.Range)
    {
      return {
        doseRange: {
          minNumerator: numeratorValue ? numeratorValue.getMin() : null,
          maxNumerator: numeratorValue ? numeratorValue.getMax() : null,
          minDenominator: denominatorValue ? denominatorValue.getMin() : null,
          maxDenominator: denominatorValue ? denominatorValue.getMax() : null
        }
      }
    }

    return {
      quantity: numeratorValue,
      quantityDenominator: denominatorValue
    };
  },

  getDoseWithUnits: function()
  {
    var dose = this.getDose();
    var doseUnits = this.getDoseUnits();

    dose.quantityUnit = doseUnits.quantityUnit;
    dose.denominatorUnit = doseUnits.denominatorUnit;

    return dose;
  },

  getDoseUnits: function()
  {
    return {
      quantityUnit: !this.numeratorUnitField.isHidden() ? this.numeratorUnitField.getValue() :
          !this.numeratorUnitLabel.isHidden() && this.strengthNumeratorUnit ? this.strengthNumeratorUnit : null,
      denominatorUnit: !this.denominatorUnitField.isHidden() ? this.denominatorUnitField.getValue() :
          !this.denominatorUnitLabel.isHidden() && this.strengthDenominatorUnit ? this.strengthDenominatorUnit : null
    }
  },
  
  getDosageCalculation: function()
  {
    return !this.dosageCalculationField.isHidden() ? this.dosageCalculationField.getValue() : null;
  },

  setDoseDenominator: function(denominator, preventEvent)
  {
    this.denominatorField.setValue(denominator, preventEvent);

    if (preventEvent)
    {
      this._allowNumeratorOrDenominatorRoundingTooltip();
      if (this.showRounding && this._isDoseRoundingPossible() && tm.jquery.Utils.isNumeric(denominator))
      {
        this._validateNumeratorAndDenominator();
      }
    }
  },

  setVolume: function(volume, preventEvent)
  {
    if (this.denominatorField.isHidden() && tm.views.medications.MedicationUtils.isUnitVolumeUnit(this.strengthNumeratorUnit))
    {
      this.setDoseNumerator(volume, preventEvent);
    }
    else if (!this.denominatorField.isHidden())
    {
      this.setDoseDenominator(volume, preventEvent);
    }
  },

  setDoseNumerator: function(doseNumerator, preventEvent)
  {
    this.numeratorField.setValue(doseNumerator, preventEvent);

    if (preventEvent)
    {
      this._allowNumeratorOrDenominatorRoundingTooltip();

      if (this.showRounding && this._isDoseRoundingPossible() && tm.jquery.Utils.isNumeric(doseNumerator))
      {
        this._validateNumeratorAndDenominator();
      }
    }
  },

  /**
   * Calculates the dose denominator based on the numerator value and defining ingredient, if set.
   * @param {Boolean} preventEvent
   */
  calculateAndSetDoseDenominator: function(preventEvent)
  {
    var numerator = this.numeratorField.getValue();
    var isNumeratorRange = numerator instanceof app.views.medications.common.dto.Range;
    var definingIngredient = this.medicationData.getDefiningIngredient();

    if (definingIngredient)
    {
      if (definingIngredient.strengthDenominator)
      {
        if (isNumeratorRange)
        {
          var denominatorRange = app.views.medications.common.dto.Range.createStrict(
            Math.round((definingIngredient.strengthDenominator / definingIngredient.strengthNumerator * numerator.getMin()) *
                10000000000) / 10000000000,
            Math.round((definingIngredient.strengthDenominator / definingIngredient.strengthNumerator * numerator.getMax()) *
                10000000000) / 10000000000
          );
          this.denominatorField.setValue(denominatorRange, preventEvent);
        }
        else if (tm.jquery.Utils.isNumeric(numerator))
        {
          var denominator = Math.round(
                  (definingIngredient.strengthDenominator / definingIngredient.strengthNumerator * numerator) *
                  10000000000) / 10000000000;
          this.denominatorField.setValue(denominator, preventEvent);
        }
        else if (!tm.jquery.Utils.isNumeric(numerator))
        {
          this._clearDenominator(preventEvent);
          this._clearDosageCalculation(true);
        }
      }
    }
    else if (this.medicationData.getMedication() &&
        (!this.medicationData.getMedication().isMedicationUniversal() ||
        (!tm.jquery.Utils.isNumeric(numerator) && !isNumeratorRange)))
    {
      this._clearDenominator(preventEvent);
      this._clearDosageCalculation(true);
    }

    if (preventEvent && this.showRounding && this._isDoseRoundingPossible() && tm.jquery.Utils.isNumeric(numerator))
    {
      this._validateNumeratorAndDenominator();
    }
  },

  getNumeratorUnit: function()
  {
    return this.numeratorUnitField.getSelection();
  },

  getDenominatorUnit: function()
  {
    if (this.denominatorAlwaysVolume)
    {
      return 'ml';
    }
    return this.denominatorUnitField.getSelection();
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
        validation: {
          type: "local",
          validators: [
            new tm.jquery.Validator({
              errorMessage: this.view.getDictionary('value.must.be.numeric.not.zero'),
              isValid: function(value)
              {
                if ((self.isDoseRangeEnabled() && value instanceof app.views.medications.common.dto.Range) ||
                    (tm.jquery.Utils.isNumeric(value) && value > 0))
                {
                  return true;
                }
                return false;
              }
            }),
            new tm.jquery.Validator({
              errorMessage: this.view.getDictionary('value.must.be.within.the.prescribed.dose.rage'),
              isValid: function(value)
              {
                if (self._inputDoseLimitRange instanceof app.views.medications.common.dto.Range)
                {
                  return self._inputDoseLimitRange.getMin() <= value && self._inputDoseLimitRange.getMax() >= value;
                }

                return true;
              }
            })
          ]
        }
      }));
    }

    if (!this.numeratorUnitField.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.numeratorUnitField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.numeratorUnitField.getSelection();
          if (value == null)
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
          if (self.denominatorUnitField.isHidden() && (value == null || value <= 0))
          {
            return null;
          }
          else if (!self.denominatorUnitField.isHidden() && self.denominatorUnitField.getSelection() != null && (value == null || value <= 0))
          {
            return null;
          }
          return true;
        }
      }));
    }

    if (!this.denominatorUnitField.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.denominatorUnitField,
        required: true,
        componentValueImplementationFn: function()
        {
          var denominator = self.denominatorField.getValue();
          var denominatorUnit = self.denominatorUnitField.getSelection();
          if ((denominator != null || denominator > 0) && (denominatorUnit == null))
          {
            return null;
          }
          return true;
        }
      }));
    }

    if (!this.dosageCalculationField.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.dosageCalculationField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.dosageCalculationField.getValue();
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
      this._allowNumeratorRoundingTooltip();
      this.numeratorField.focus();
    }
    else if (!this.denominatorField.isHidden())
    {
      this._allowDenominatorRoundingTooltip();
      this.denominatorField.focus();
    }
  },

  requestFocusToNumerator: function()
  {
    this._allowNumeratorRoundingTooltip();
    this.numeratorField.focus();
    $(this.numeratorField.getDom()).select();
  },

  requestFocusToDosageCalculation: function()
  {
    this._allowNumeratorOrDenominatorRoundingTooltip();
    this.dosageCalculationField.focus();
    $(this.dosageCalculationField.getDom()).select();
  },

  requestFocusToDenominator: function()
  {
    this._allowDenominatorRoundingTooltip();
    this.denominatorField.focus();
    $(this.denominatorField.getDom()).select();
  },

  setPaneEditable: function(editable)
  {
    this.numeratorField.setEnabled(editable);
    this.denominatorField.setEnabled(editable);
    this.dosageCalculationField.setEnabled(editable);
  },

  markAsLateDose: function(late)
  {
    var numeratorCls = this.numeratorField.getCls().replace(" late-dose", "");
    var denominatorCls = this.denominatorField.getCls().replace(" late-dose", "");
    if (late)
    {
      numeratorCls += " late-dose";
      denominatorCls += " late-dose";
    }
    this.numeratorField.setCls(numeratorCls);
    this.denominatorField.setCls(denominatorCls);
  },
  
  setSharpBorders: function(sharpBorders)
  {
    var numeratorCls = this.numeratorField.getCls().replace(" sharp-borders", "");
    var denominatorCls = this.denominatorField.getCls().replace(" sharp-borders", "");
    if (sharpBorders)
    {
      numeratorCls += " sharp-borders";
      denominatorCls += " sharp-borders";
    }
    this.numeratorField.setCls(numeratorCls);
    this.denominatorField.setCls(denominatorCls);
  },

  setDosageCalculationUnitLabel: function(doseUnit)
  {
    this.dosageCalculationUnitCombo.hide();
    this.dosageCalculationUnitLabel.show();
    if (!tm.jquery.Utils.isEmpty(doseUnit))
    {
      this.dosageCalculationUnitLabel.setData(doseUnit);
      this.dosageCalculationUnitLabel.setHtml(doseUnit.displayUnit);
      this._calculateDosageCalculationFromFormula(doseUnit);
    }
    else
    {
      this.dosageCalculationUnitLabel.setData(null);
      this.dosageCalculationUnitLabel.setHtml("");
    }
  },

  isDosageCalculationHidden: function()
  {
    return this.dosageCalculationField.isHidden();
  },

  isDosageCalculationPossible: function()
  {
    var utils = tm.views.medications.MedicationUtils;
    if (!tm.jquery.Utils.isEmpty(this.medicationData) &&
        this.medicationData.getMedication() &&
        !this.medicationData.getMedication().isMedicationUniversal())
    {
      return utils.unitForDosageCalculation(this.medicationData.getStrengthNumeratorUnit());
    }
    return false;
  },

  showDosageCalculationFields: function()
  {
    this.dosageCalculationFractionLine.show();
    this.dosageCalculationField.show();
  },

  suppressDosageCalculationTooltips: function()
  {
    if (this._isDoseRoundingPossible())
    {
      this.numeratorRoundingTooltipAllowed = false;
      this.denominatorRoundingTooltipAllowed = false;
      this._setupDosageRoundingValidation();
      this.dosageRoundingValidationForm.submit();

    }
  },

  /**
   * @param {Object} doseRange
   */
  limitByDoseRange: function(doseRange)
  {
    if (doseRange)
    {
      var limitRange = new app.views.medications.common.dto.Range.createStrict(
          doseRange.minNumerator,
          doseRange.maxNumerator
      );

      this._inputDoseLimitRange = limitRange;
    }
    else
    {
      this._inputDoseLimitRange = null;
    }
  },

  /**
   * @returns {boolean}
   */
  isDoseRangeEnabled: function()
  {
    return this.doseRangeEnabled === true;
  }
});