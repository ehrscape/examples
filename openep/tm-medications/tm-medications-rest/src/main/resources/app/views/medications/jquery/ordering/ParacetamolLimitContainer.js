/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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

Class.define('app.views.medications.ordering.ParacetamolLimitContainer', 'tm.jquery.Container', {
  cls: "paracetamol-container",

  /** configs */
  view: null,
  percentage: null,

  /** privates */
  _paracetamolLimitLabel: null,
  _paracetamolLimitImage: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /** private methods */
  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));

    this._paracetamolLimitLabel = new tm.jquery.Label({
      cls: "TextData",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      hidden: true
    });

    this._paracetamolLimitImage = new tm.jquery.Image({
      cls: 'bnf-high-icon',
      width: 16,
      height: 16,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px"),
      hidden: true
    });

    this.add(this._getParacetamolLimitImage());
    this.add(this._getParacetamolLimitLabel());
  },

  _createPopup: function(text)
  {
    var defaultPopoverTooltip = this.view.getAppFactory().createDefaultPopoverTooltip(null, null,
        new tm.jquery.Label({
          cls: 'warning-popover-tooltip',
          text: text
        }));

    defaultPopoverTooltip.setPlacement("bottom");
    return defaultPopoverTooltip;
  },

  /** private methods */
    _getParacetamolLimitLabel: function()
  {
    return this._paracetamolLimitLabel;
  },

  _getParacetamolLimitImage: function()
  {
    return this._paracetamolLimitImage;
  },

  /** public methods */
  setCalculatedParacetamolLimit: function(calculatedParacetamolRule) // MedicationIngredientRuleDto.java
  {
    if (!tm.jquery.Utils.isEmpty(calculatedParacetamolRule))
    {
      var percentage = calculatedParacetamolRule.underageRulePercentage >= calculatedParacetamolRule.adultRulePercentage
          ? calculatedParacetamolRule.underageRulePercentage
          : calculatedParacetamolRule.adultRulePercentage;

      this.percentage = percentage;
      if (this.percentage >= 100 && tm.jquery.Utils.isEmpty(calculatedParacetamolRule.errorMessage))
      {
        this._getParacetamolLimitImage().setTooltip(this._createPopup(tm.jquery.Utils.formatMessage(
            this.view.getDictionary("paracetamol.max.daily.limit.percentage"),
            [percentage])));

        this._getParacetamolLimitLabel().setText(percentage + "%");
        this.showAll();
      }
      else
      {
        this.percentage = null;
        this.hideAll();
      }
    }
    else
    {
      this.percentage = null;
      this.hideAll();
    }
  },

  getPercentage: function ()
  {
    return this.percentage;
  },

  hasContent: function()
  {
    return tm.jquery.Utils.isEmpty(this.percentage);
  },

  hideAll: function()
  {
    if (this.isRendered())
    {
      this._getParacetamolLimitLabel().hide();
      this._getParacetamolLimitImage().hide();
    }
    else
    {
      this._getParacetamolLimitLabel().setHidden(true);
      this._getParacetamolLimitImage().setHidden(true);
    }
  },

  showAll: function()
  {
    if (this.isRendered())
    {
      this._getParacetamolLimitLabel().show();
      this._getParacetamolLimitImage().show();
      this.show();
    }
    else
    {
      this._getParacetamolLimitLabel().setHidden(false);
      this._getParacetamolLimitImage().setHidden(false);
      this.setHidden(false);
    }
  },

  clear: function()
  {
      this.percentage = null;
      this.hideAll();
  }
});
