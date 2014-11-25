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

  /** configs */
  view: null,
  continuousInfusionChangedFunction: null,
  adjustToFluidBalanceChangedFunction: null,
  bolusChangedFunction: null,
  speedChangedFunction: null,

  /** privates: components */
  baselineInfusionButton: null,
  continuousInfusionButton: null,
  bolusButton: null,
  speedButton: null,
  rateButtonGroup: null,
  adjustToFluidBalanceButton: null,

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
    this.baselineInfusionButton = new tm.jquery.ToggleButton({text: this.view.getDictionary('baseline.infusion.short')});
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
      }
      else
      {
        self.adjustToFluidBalanceButton.hide();
        self.adjustToFluidBalanceButton.setPressed(false);
      }
    });

    this.continuousInfusionButton = new tm.jquery.Button({text: this.view.getDictionary('continuous.infusion')});
    this.bolusButton = new tm.jquery.Button({text: 'Bolus'});
    this.speedButton = new tm.jquery.Button({text: this.view.getDictionary('rate')});

    this.rateButtonGroup = new tm.jquery.ButtonGroup({
      orientation: 'horizontal',
      type: 'checkbox',
      buttons: [this.continuousInfusionButton, this.bolusButton, this.speedButton]
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
    });

    this.adjustToFluidBalanceButton = new tm.jquery.ToggleButton({cls: 'fluid-balance-icon', width: 40});
    this.adjustToFluidBalanceButton.setTooltip(
        tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary('adjust.to.fluid.balance.long'), null, this.view));
    this.adjustToFluidBalanceButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
    {
      self.adjustToFluidBalanceChangedFunction(component.isPressed());
    });
    this.adjustToFluidBalanceButton.hide();
  },

  _buildGui: function()
  {
    this.add(this.rateButtonGroup);
    this.add(this.baselineInfusionButton);
    this.add(this.adjustToFluidBalanceButton);
  },

  /** public methods */
  clear: function()
  {
    this.rateButtonGroup.clearSelection();
    this.baselineInfusionButton.setPressed(false);
  },

  setContinuousInfusion: function(continuousInfusion, baselineInfusion, clearValues, fireChangedEvent)
  {
    if (continuousInfusion)
    {
      this.rateButtonGroup.setSelection([this.continuousInfusionButton]);
    }
    this.baselineInfusionButton.setPressed(baselineInfusion, !fireChangedEvent);
    this.continuousInfusionChangedFunction(continuousInfusion, clearValues);
    if (baselineInfusion)
    {
      this.adjustToFluidBalanceButton.show();
    }
    else
    {
      this.adjustToFluidBalanceButton.hide();
    }
  },

  setAdjustToFluidBalance: function(adjustToFluidBalance)
  {
    this.adjustToFluidBalanceButton.setPressed(adjustToFluidBalance);
    this.adjustToFluidBalanceChangedFunction(adjustToFluidBalance);
  },

  isContinuousInfusion: function()
  {
    return this.continuousInfusionButton.isPressed();
  },

  isBaselineInfusion: function()
  {
    return this.baselineInfusionButton.isPressed();
  },

  isAdjustToFluidBalance: function()
  {
    return this.adjustToFluidBalanceButton.isPressed();
  },

  isBolus: function()
  {
    return this.bolusButton.isPressed();
  },

  setBolus: function(isBolus)
  {
    if (isBolus)
    {
      this.rateButtonGroup.setSelection([this.bolusButton]);
    }
    this.bolusChangedFunction(isBolus);
  },

  setSpeed: function(isSpeed)
  {
    if (isSpeed)
    {
      this.rateButtonGroup.setSelection([this.speedButton]);
    }
    this.speedChangedFunction(isSpeed);
  }
});