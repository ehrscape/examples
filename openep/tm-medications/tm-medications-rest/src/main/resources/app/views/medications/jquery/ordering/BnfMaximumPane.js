/*
 * Copyright (c) 2010-2015 Marand d.o.o. (www.marand.com)
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

Class.define('app.views.medications.ordering.BnfMaximumPane', 'tm.jquery.Container', {
  cls: "bnf-maximum-pane",
  /** configs */
  view: null,
  percentage: null,
  maxValue: null,
  unit: null, // BnfMaximumUnitType.java
  numeratorUnit: null, //microgram or miligram

  quantity: null,
  timesPerDay: null,
  timesPerWeek: null,
  variable: null,

  bnfLabel: null,
  bnfImage: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /** private methods */
  _buildGui: function()
  {
    this.bnfLabel = new tm.jquery.Label({
      text: tm.jquery.Utils.isEmpty(this.percentage) ? null : this.percentage + '%',
      cls: "TextData",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      hidden: tm.jquery.Utils.isEmpty(this.percentage)
    });

    this.bnfImage = new tm.jquery.Image({
      margin: "0 5 0 5",
      cursor: "pointer",
      width: 16,
      height: 16,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px"),
      hidden: tm.jquery.Utils.isEmpty(this.percentage)
    });

    var cls = tm.views.medications.warning.WarningsHelpers.getImageClsForBnfMaximumPercentage(this.percentage);
    if (cls != null)
    {
      this.bnfImage.setCls(cls);
    }

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));
    this.add(this.bnfImage);
    this.add(this.bnfLabel);
  },

  _resetDisplayData: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.percentage))
    {
      this.bnfLabel.isRendered() ? this.bnfLabel.show() : this.bnfLabel.setHidden(false);
      this.bnfImage.isRendered() ? this.bnfImage.show() : this.bnfImage.setHidden(false);

      this.bnfLabel.setText(this.percentage + '%');
      this.bnfImage.setCls(tm.views.medications.warning.WarningsHelpers.getImageClsForBnfMaximumPercentage(this.percentage));
      this.bnfImage.setTooltip(this._createPopup(this.percentage + "% of antipsychotic BNF maximum"));
    }
    else
    {
      this.bnfLabel.isRendered() ? this.bnfLabel.hide() : this.bnfLabel.setHidden(true);
      this.bnfImage.isRendered() ? this.bnfImage.hide() : this.bnfImage.setHidden(true);
    }
  },

  _createPopup: function(text)
  {
    var defaultPopoverTooltip = this.view.getAppFactory().createDefaultPopoverTooltip(null, null, new tm.jquery.Label({
      text: text,
      alignSelf: "center",
      padding: '5 10 5 10'
    }));
    defaultPopoverTooltip.setPlacement("bottom");
    return defaultPopoverTooltip;
  },

  /** public methods */

  /**
   * Getters & Setters
   */

  /**
   * @param {Number|null} value
   */
  setTimesPerWeek: function(value)
  {
    this.timesPerWeek = value;
  },

  /**
   * @param {Number|null} value
   */
  setTimesPerDay: function(value)
  {
    this.timesPerDay = value;
  },

  /**
   * @param {Number|null} value
   */
  setQuantity: function(value)
  {
    this.quantity = value;
  },

  /**
   * @param {Number|null} value
   */
  setPercentage: function(value)
  {
    this.percentage = value;
    this._resetDisplayData();
  },

  /**
   * @returns {Number|null}
   */
  getPercentage: function ()
  {
    return this.percentage;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData] medicationData
   * @param {Object} route
   */
  setBnfValuesAndNumeratorUnit: function(medicationData, route)
  {
    var definingIngredient = medicationData.getDefiningIngredient();
    var numeratorUnit = definingIngredient ? definingIngredient.strengthNumeratorUnit : medicationData.getBasicUnit();
    var maxValue = route.bnfMaximumDto.quantity;

    if (!tm.jquery.Utils.isEmpty(numeratorUnit) && numeratorUnit == "microgram")
    {
      maxValue = maxValue * 1000; // convert mg to micrograms
    }

    this.numeratorUnit = numeratorUnit;
    this.maxValue = maxValue;
    this.unit = route.bnfMaximumDto.quantityUnit;

    if (!tm.jquery.Utils.isEmpty(this.quantity))
    {
      this.calculatePercentage(this.quantity, this.timesPerDay, this.timesPerWeek, this.variable);
    }
  },

  clear: function()
  {
    this.setPercentage(null);
    this.setQuantity(null);
    this.setTimesPerDay(null);
    this.setTimesPerWeek(null);
  },

  calculatePercentage: function(quantity, timesPerDay, timesPerWeek, variable)
  {
    var self = this;
    this.quantity = quantity;
    this.timesPerDay = timesPerDay;
    this.timesPerWeek = timesPerWeek;
    this.variable = variable;

    if (!tm.jquery.Utils.isEmpty(this.timesPerDay) && !tm.jquery.Utils.isEmpty(this.quantity))
    {
      if (this.variable)
      {
        this.timesPerDay = 1;
      }

      if (this.unit === app.views.medications.TherapyEnums.bnfMaximumUnitType.DAY)
      {
        this.percentage = Math.ceil(this.timesPerDay * this.quantity / this.maxValue * 100);
      }
      else if (this.unit === app.views.medications.TherapyEnums.bnfMaximumUnitType.WEEK)
      {
        //var maxDayValue = maxValue / 7;

        if (this.timesPerWeek === 0)
        {
          this.setTimesPerWeek(7);
        }
        this.percentage = Math.ceil((this.timesPerWeek * this.quantity * this.timesPerDay) / this.maxValue * 100);
      }
      this._resetDisplayData();
      return this.percentage;
    }
    else
    {
      self.setPercentage(null);
      return null;
    }
  }
});
