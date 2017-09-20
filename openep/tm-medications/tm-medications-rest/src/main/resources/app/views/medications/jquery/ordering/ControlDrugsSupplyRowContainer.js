/**
 * Created by Nejc Korasa on 17.10.2015.
 */
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

Class.define('app.views.medications.ordering.ControlDrugsSupplyRowContainer', 'tm.jquery.Container', {
  cls: 'drug-container',
  layout: tm.jquery.HFlexboxLayout.create("start", "stretch"),

  /** configs **/
  view: null,

  /** data - SupplyMedicationDto.java **/
  id: null,
  name: null,
  basicUnit: null,
  strengthNumerator: null,
  supplyQuantity: null,

  /** data **/
  remainingDoseQuantity: null,

  /** callbacks **/
  onItemSelectedCallback: null,
  onItemDeselectedCallback: null,

  /** privates **/
  checkbox: null,
  nameLabel: null,
  unitLabel: null,
  label: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;

    var checkbox = new tm.jquery.CheckBox({
      labelText: self.getName(),
      labelCls: "TextData",
      checked: false,
      labelAlign: "right",
      enabled: true,
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      nowrap: true
    });

    checkbox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent, elementEvent)
    {
      component.isChecked() ? self.onItemSelectedCallback(self) : self.onItemDeselectedCallback(self);
    });

    this.checkbox = checkbox;

    this.unitLabel = new tm.jquery.Label({
      text: self.getBasicUnit(),
      width: 50,
      cls: "TextData",
      margin: "0px, 5px, 0px, 5px"
    });

    this.label = new tm.jquery.Label({
      text: null,
      width: 50,
      style: "text-align: right;",
      cls: "TextDataBold",
      margin: "0px, 5px, 0px, 5px"
    });

    this.add(this.checkbox);
    this.add(this.label);
    this.add(this.unitLabel);
  },

  setSupplyQuantity: function(quantity)
  {
    this.supplyQuantity = quantity;
  },

  isSelected: function()
  {
    return this.checkbox.isSelected();
  },

  setEnabled: function(value)
  {
    this.checkbox.setEnabled(value);

    if (value == false)
    {
      this.checkbox.setLabelCls("TextDataLight");
      this.unitLabel.setCls("TextDataLight");
      this.repaint();
    }
    else
    {
      this.checkbox.setLabelCls("TextData");
      this.unitLabel.setCls("TextData");
      this.repaint();
    }
  },

  isEnabled: function()
  {
    return this.checkbox.isEnabled();
  },

  isChecked: function()
  {
    return this.checkbox.isChecked();
  },

  resetData: function()
  {
    this.setSupplyQuantity(null);
    this.checkbox.setChecked(false, true);
    this.label.setText(null);
  },

  getId: function()
  {
    return this.id;
  },

  getName: function()
  {
    return this.name;
  },

  getStrengthNumerator: function()
  {
    return this.strengthNumerator;
  },

  getBasicUnit: function()
  {
    return this.basicUnit;
  },

  getSupplyQuantity: function()
  {
    return this.supplyQuantity;
  },

  displayNumberOfUnits: function(value)
  {
    this.label.setText(value);
  }
});