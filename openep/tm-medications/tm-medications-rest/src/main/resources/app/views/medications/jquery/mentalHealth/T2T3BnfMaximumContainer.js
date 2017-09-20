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

Class.define('app.views.medications.mentalHealth.T2T3BnfMaximumContainer', 'tm.jquery.Container', {
  cls: "mental-health-bnf-container",
  layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
  flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),

  /** configs */
  view: null,

  /** privates */
  bnfMaximumLabel: null,
  bnfMaximumTextField: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var view = this.view;
    this.bnfMaximumLabel = new tm.jquery.Label({
      text: view.getDictionary("please.enter.bnf.max.limit") + ": ",
      cls: "TextData",
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      padding: '0px 5px 0px 5px'
    });

    this.bnfMaximumTextField = new tm.jquery.TextField({
      width: 40,
      cls: "field-flat"
    });

    this.add(this.bnfMaximumLabel);
    this.add(this.bnfMaximumTextField);
    this.add(new tm.jquery.Label({
      text: '%',
      cls: "TextData",
      padding: '0px 0px 0px 5px',
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto")
    }));
  },

  getResult: function()
  {
    return this.bnfMaximumTextField.getValue();
  }
});
