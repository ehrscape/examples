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

Class.define('app.views.medications.ordering.InfusionRateTypePane', 'tm.jquery.Container', {
  cls: "infusion-rate-type-pane",

  /** configs */
  view: null,
  continuousInfusionChangedFunction: null,
  adjustableRateChangeFunction: null,
  bolusChangedFunction: null,
  speedChangedFunction: null,
  titratedRateSupported: false,
  preventRateTypeChange: false,
  visibilityContext: null,
  /** privates*/
  _continuousInfusion: null,

  /** privates: components */
  baselineInfusionButton: null,
  continuousInfusionButton: null,
  bolusButton: null,
  speedButton: null,
  rateButtonGroup: null,
  adjustToFluidBalanceButton: null,
  _titratedRateButton: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.baselineInfusionButton = new tm.jquery.ToggleButton({
      cls: "baseline-infusion-button",
      text: this.getView().getDictionary('baseline.infusion.short'),
      enabled: !this.isPreventRateTypeChange(),
      hidden: this._isRateTypeSelectionHiddenByContext()
    });

    this.baselineInfusionButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
    {
      if (component.isPressed())
      {
        if (!self.continuousInfusionButton.isPressed())
        {
          self.rateButtonGroup.setSelection([self.continuousInfusionButton]);
          self.continuousInfusionChangedFunction(true, true);
        }
        self.adjustToFluidBalanceButton.show();
        self.adjustToFluidBalanceButton.setPressed(true);
        self._titratedRateButton.setPressed(false, true);
      }
      else
      {
        self.adjustToFluidBalanceButton.hide();
        self.adjustToFluidBalanceButton.setPressed(false);
      }
    });

    this.continuousInfusionButton = new tm.jquery.Button({
      cls: "continuous-infusion-button", text: this.getView().getDictionary('continuous.infusion')});
    this.bolusButton = new tm.jquery.Button({cls: "bolus-button", text: 'Bolus'});
    this.speedButton = new tm.jquery.Button({cls: "speed-button", text: this.getView().getDictionary('rate')});

    this.rateButtonGroup = new tm.jquery.ButtonGroup({
      orientation: 'horizontal',
      type: 'checkbox',
      enabled: !this.isPreventRateTypeChange(),
      buttons: [this.continuousInfusionButton, this.bolusButton, this.speedButton],
      hidden: this._isRateTypeSelectionHiddenByContext()
    });
    this.rateButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      var pressedButton = componentEvent.getEventData().pressedButton;
      if (pressedButton)
      {
        if (self.rateButtonGroup.getSelection().length > 1)
        {
          self.rateButtonGroup.setSelection([pressedButton]);
        }
        if (pressedButton == self.continuousInfusionButton)
        {
          self.continuousInfusionChangedFunction(self.isContinuousInfusion(), true);
        }
        else if (pressedButton == self.bolusButton)
        {
          self.bolusChangedFunction();
        }
        else if (pressedButton == self.speedButton)
        {
          self.speedChangedFunction(self.speedButton.isPressed());
        }
      }
      if (!self.isContinuousInfusion())
      {
        self.baselineInfusionButton.setPressed(false);
      }
      var continuousInfusionSelected = self.isContinuousInfusion();
      if (self._continuousInfusion && !continuousInfusionSelected || !self._continuousInfusion && continuousInfusionSelected)
      {
        self.continuousInfusionChangedFunction(continuousInfusionSelected, false);
      }
      self._continuousInfusion = continuousInfusionSelected;
      self._applyTitratedRateButtonVisibility();
    });

    this.adjustToFluidBalanceButton = new tm.jquery.ToggleButton({
      cls: 'fluid-balance-icon adjust-fluid-balance-button',
      width: 40,
      tooltip: tm.views.medications.MedicationUtils.createTooltip(
          this.getView().getDictionary('adjust.to.fluid.balance.long'),
          null,
          this.getView()),
      hidden: true,
      enabled: !this.isPreventRateTypeChange()
    });
    this.adjustToFluidBalanceButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
    {
      self._onAdjustToFluidBalanceButtonClick();
    });

    this._titratedRateButton = new tm.jquery.ToggleButton({
      iconCls: 'icon-titration-dosage-24',
      tooltip: tm.views.medications.MedicationUtils.createTooltip(
          this.getView().getDictionary('rate.titration'),
          null,
          this.getView()),
      hidden: true,
      enabled: !this.isPreventRateTypeChange()
    });
    this._titratedRateButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
       self._onTitratedRateButtonClick();
    });
  },

  _buildGui: function()
  {
    this.add(this.rateButtonGroup);
    this.add(this.baselineInfusionButton);
    this.add(this.adjustToFluidBalanceButton);
    this.add(this._titratedRateButton);
  },

  /**
   * @returns {boolean}
   * @private
   */
  _isRateTypeSelectionHiddenByContext: function()
  {
    return this.getVisibilityContext() && this.getVisibilityContext().hasOwnProperty("infusionRateTypeSelectionVisible") &&
        this.getVisibilityContext().infusionRateTypeSelectionVisible === false;
  },

  _applyTitratedRateButtonVisibility: function()
  {
    var showButton = this.isTitratedRateSupported() && this.isContinuousInfusion() &&
        !this._isRateTypeSelectionHiddenByContext();

    if (showButton)
    {
      this.isRendered() ? this._titratedRateButton.show() : this._titratedRateButton.setHidden(false);
    }
    else
    {
      if (this._titratedRateButton.isPressed())
      {
        this._titratedRateButton.setPressed(false, true);
      }
      this.isRendered() ? this._titratedRateButton.hide() : this._titratedRateButton.setHidden(true);
    }
  },

  _onTitratedRateButtonClick: function()
  {
    var isTitratedRate = this._titratedRateButton.isPressed();
    var wasAdjustToFluid = this.adjustToFluidBalanceButton.isPressed();

    if (isTitratedRate)
    {
      this.baselineInfusionButton.setPressed(false, true);
      this.adjustToFluidBalanceButton.setPressed(false, true);
      this.adjustToFluidBalanceButton.hide();
    }
    // detect change to adjust to rate state and trigger the change event, otherwise skip
    if (!wasAdjustToFluid && this.adjustableRateChangeFunction)
    {
      this.adjustableRateChangeFunction(isTitratedRate);
    }
  },

  _onAdjustToFluidBalanceButtonClick: function()
  {
    var isAdjustToFluidBalance = this.adjustToFluidBalanceButton.isPressed();
    var wasTitratedRate = this._titratedRateButton.isPressed();

    // detect change to adjust to rate state and trigger the change event, otherwise skip
    if (!wasTitratedRate && this.adjustableRateChangeFunction)
    {
      this.adjustableRateChangeFunction(isAdjustToFluidBalance);
    }
  },

  /** public methods */
  clear: function(preventEvent)
  {
    this.setTitratedDoseMode(false, preventEvent);
    this.rateButtonGroup.clearSelection(preventEvent);
    this._continuousInfusion = false;
    if (this.isRendered())
    {
      this.adjustToFluidBalanceButton.hide();
    }
    else
    {
      this.adjustToFluidBalanceButton.setHidden(true);
    }
    this._titratedRateButton.setPressed(false, preventEvent);
    this._applyTitratedRateButtonVisibility();
  },

  /**
   * @param {boolean} [baselineInfusion=false]
   * @param {boolean} [titratedRate=false]
   * @param {boolean} [adjustToFluidBalance=false]
   * @param {boolean} [clearValues=false]
   * @param {boolean} [preventEvent=false]
   */
  markAsContinuousInfusion: function(baselineInfusion, titratedRate, adjustToFluidBalance, clearValues,
                                  preventEvent)
  {
    var wasAdjustableRate = this._titratedRateButton.isPressed() || this.adjustToFluidBalanceButton.isPressed();
    var isAdjustableRate =  titratedRate === true || adjustToFluidBalance === true;

    this._continuousInfusion = true;
    this.rateButtonGroup.setSelection([this.continuousInfusionButton], preventEvent);
    this.baselineInfusionButton.setPressed(baselineInfusion && !titratedRate, preventEvent);
    this._titratedRateButton.setPressed(titratedRate, preventEvent);
    this._applyTitratedRateButtonVisibility();
    if (baselineInfusion && !titratedRate)
    {
      if (!this._isRateTypeSelectionHiddenByContext())
      {
        this.adjustToFluidBalanceButton.show();
      }
      this.adjustToFluidBalanceButton.setPressed(adjustToFluidBalance, preventEvent);
    }
    else
    {
      if (!this._isRateTypeSelectionHiddenByContext())
      {
        this.adjustToFluidBalanceButton.hide();
      }
      this.adjustToFluidBalanceButton.setPressed(false, preventEvent);
    }

    if (!preventEvent && this.adjustableRateChangeFunction && (wasAdjustableRate !== isAdjustableRate))
    {
      this.adjustableRateChangeFunction(adjustToFluidBalance);
    }
    if (!preventEvent)
    {
      this.continuousInfusionChangedFunction(true, clearValues);
    }
  },

  /**
   * @param {Boolean} value
   * @param {Boolean} preventChangeEvent
   */
  setTitratedDoseMode: function(value, preventChangeEvent)
  {
    if (!this._isRateTypeSelectionHiddenByContext()) // prevent visibility change when the default state is hidden
    {
      if (!this.rateButtonGroup.isRendered())
      {
        this.baselineInfusionButton.setHidden(value);
        this.bolusButton.setHidden(value);
        this.speedButton.setHidden(value);
      }
      else
      {
        value ? this.baselineInfusionButton.hide() : this.baselineInfusionButton.show();
        value ? this.bolusButton.hide() : this.bolusButton.show();
        value ? this.speedButton.hide() : this.speedButton.show();
      }
    }

    var cleanRateButtonCls = this.rateButtonGroup.getCls() ?
        this.rateButtonGroup.getCls().replace(' single-visible', '') :
        '';

    this.rateButtonGroup.setCls(cleanRateButtonCls + (value ? ' single-visible' : ''));

    if (value)
    {
      if (this.rateButtonGroup.getSelection().length > 0 && !this.isContinuousInfusion())
      {
        this.rateButtonGroup.clearSelection(preventChangeEvent);
      }
    }
  },

  /**
   * @returns {Boolean}
   */
  isContinuousInfusion: function()
  {
    return this.continuousInfusionButton.isPressed();
  },

  /**
   * @returns {Boolean}
   */
  isBaselineInfusion: function()
  {
    return this.baselineInfusionButton.isPressed();
  },

  /**
   * @returns {Boolean}
   */
  isAdjustToFluidBalance: function()
  {
    return this.adjustToFluidBalanceButton.isPressed();
  },

  /**
   * @returns {Boolean}
   */
  isTitratedRate: function()
  {
    return this.isTitratedRateSupported() && this._titratedRateButton.isPressed();
  },

  /**
   * @returns {Boolean}
   */
  isBolus: function()
  {
    return this.bolusButton.isPressed();
  },

  /**
   * @returns {Boolean}
   */
  isSpeed: function()
  {
    return this.speedButton.isPressed();
  },

  /**
   * @param {Boolean} preventEvent
   */
  markAsBolus: function(preventEvent)
  {
    this.rateButtonGroup.setSelection([this.bolusButton], preventEvent);

    if (!preventEvent)
    {
      this.bolusChangedFunction(true);
    }
  },

  /**
   * @param {Boolean} preventEvent
   */
  markAsSpeed: function(preventEvent)
  {
    this.rateButtonGroup.setSelection([this.speedButton], preventEvent);

    if (!preventEvent)
    {
      this.speedChangedFunction(true);
    }
  },

  /**
   * @param {Boolean} prevent
   */
  setPreventRateTypeChange: function(prevent)
  {
    if (this.isPreventRateTypeChange() === prevent) return;

    if (prevent)
    {
      if (this.isSpeed())
      {
        this.speedChangedFunction(false);
      }
      else if (this.isContinuousInfusion())
      {
        this._continuousInfusion = false;
        this.continuousInfusionChangedFunction(false, true);
      }
      else if (this.isBolus())
      {
        this.bolusChangedFunction();
      }
      this.rateButtonGroup.clearSelection(true);
    }
    else
    {
      this.setRateGroupTooltip(null);
    }
    this.rateButtonGroup.setEnabled(!prevent);
    this._titratedRateButton.setEnabled(!prevent);
    this.baselineInfusionButton.setEnabled(!prevent);
    this.adjustToFluidBalanceButton.setEnabled(!prevent);
    this.preventRateTypeChange = prevent;
  },

  /**
   * @param {string|null} tooltipText
   */
  setRateGroupTooltip: function(tooltipText)
  {
    this.rateButtonGroup.setTooltip(
        tooltipText ?
            this.getView().getAppFactory().createDefaultHintTooltip(tooltipText, "bottom", "click") :
            null
    );
  },

  /**
   * @param {boolean} value
   */
  setTitratedRateSupported: function(value)
  {
    this.titratedRateSupported = value;
  },

  /**
   * @returns {boolean}
   */
  isTitratedRateSupported: function()
  {
    return this.titratedRateSupported === true;
  },

  /**
   * @returns {boolean}
   */
  isPreventRateTypeChange: function()
  {
    return this.preventRateTypeChange === true;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {Object|null}
   */
  getVisibilityContext: function()
  {
    return this.visibilityContext;
  }
});