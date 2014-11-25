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

Class.define('app.views.medications.ordering.HeparinPane', 'tm.jquery.Container', {

  /** configs */
  view: null,
  /** privates */
  /** privates: components */
  heparinButtonGroup: null,
  button0: null,
  button05: null,
  button1: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var appFactory = this.view.getAppFactory();

    this.setLayout(appFactory.createDefaultHFlexboxLayout("start", "center", 5));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    this.button0 = new tm.jquery.RadioButton({labelText: "0", data: null, labelAlign: "right", checked: true});
    this.button05 = new tm.jquery.RadioButton({labelText: "0.5", data: "HEPARIN_05", labelAlign: "right"});
    this.button1 = new tm.jquery.RadioButton({labelText: "1", data: "HEPARIN_1", labelAlign: "right"});
    this.heparinButtonGroup = new tm.jquery.RadioButtonGroup({});
    this.heparinButtonGroup.add(this.button0);
    this.heparinButtonGroup.add(this.button05);
    this.heparinButtonGroup.add(this.button1);
  },

  _buildGui: function()
  {
    var appFactory = this.view.getAppFactory();

    this.add(appFactory.createHRadioButtonGroupContainer(this.heparinButtonGroup));
    this.add(new tm.jquery.Label({text: "IE/mL", nowrap: true}));
  },

  /** public methods */
  getHeparinValue: function()
  {
    return this.heparinButtonGroup.getActiveRadioButton().data;
  },

  setHeparinValue: function(heparinValue)
  {
    var buttons = this.heparinButtonGroup.getRadioButtons();
    for (var i = 0; i < buttons.length; i++)
    {
      if (buttons[i].data == heparinValue)
      {
        this.heparinButtonGroup.setActiveRadioButton(buttons[i]);
        break;
      }
    }
    return this.heparinButtonGroup.getActiveRadioButton().data;
  },

  clear: function()
  {
    this.heparinButtonGroup.setActiveRadioButton(this.button0);
  }
});
