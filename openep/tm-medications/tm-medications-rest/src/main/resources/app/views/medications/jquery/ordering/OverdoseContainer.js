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

Class.define('app.views.medications.ordering.OverdoseContainer', 'tm.jquery.Container', {
  /** configs */
  margin: "0px 5px 0px 0px",
  view: null,

  /** public */
  basicUnit: null,
  medicationStrength: null,
  medicationUnit: null, //for now only tablets/capsules

  /** privates */
  overTenTimesDoseImage: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /** private methods */
  _buildGui: function()
  {
    this.overTenTimesDoseImage = new tm.jquery.Image({
      margin: "0 5 0 5",
      cursor: "pointer",
      width: 16,
      height: 16,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px"),
      cls: "icon_late",
      hidden: true
    });

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));
    this.add(this.overTenTimesDoseImage);
  },

  _createPopup: function(text)
  {
    var defaultPopoverTooltip = this.view.getAppFactory().createDefaultPopoverTooltip(null, null, new tm.jquery.Label({
      text: text,
      alignSelf: "center",
      width: 300,
      padding: '5 10 5 10'
    }));
    defaultPopoverTooltip.setPlacement("bottom");
    return defaultPopoverTooltip;
  },

  /** public methods */
  setMedicationDataValues: function(medicationData)
  {
    this.overTenTimesDoseImage.hide();
    if (!tm.jquery.Utils.isEmpty(medicationData))
    {
      this.basicUnit = medicationData.basicUnit;
      this.medicationUnit = medicationData.getStrengthNumeratorUnit();

      var definingIngredient = medicationData.getDefiningIngredient();
      if (!tm.jquery.Utils.isEmpty(definingIngredient))
      {
        this.medicationStrength = definingIngredient.strengthNumerator;
      }
    }
  },

  isTabletOrCapsule: function()
  {
    var tabletOrCapsuleUnits = ['tablet', 'tableta', 'capsule', 'kapsula'];
    return tabletOrCapsuleUnits.indexOf(this.basicUnit) > -1;
  },

  calculateOverdose: function(value)
  {
    if (tm.jquery.Utils.isNumeric(value))
    {
      var isOverdoes = false;
      if (this.medicationUnit == "mg" || this.medicationUnit == "microgram")
      {
        if (!tm.jquery.Utils.isEmpty(this.medicationStrength) && value >= 5 * this.medicationStrength)
        {
          isOverdoes = true;
        }
      }
      else if (this.medicationUnit == "tablet" || this.medicationUnit == "capsule")
      {
        if (value >= 5)
        {
          isOverdoes = true;
        }
      }
      if (isOverdoes)
      {
        var tooltipText = this.view.getDictionary("five.time.overdose").replace("{0}", this.basicUnit);
        this.overTenTimesDoseImage.setTooltip(this._createPopup(tooltipText));
        this.overTenTimesDoseImage.show();
      }
      else
      {
        this.overTenTimesDoseImage.hide();
      }
    }
  }
});
