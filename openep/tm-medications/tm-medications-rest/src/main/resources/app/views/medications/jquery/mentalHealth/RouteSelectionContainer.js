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

Class.define('app.views.medications.mentalHealth.RouteSelectionContainer', 'app.views.common.containers.AppBodyContentContainer', {
  cls: "route-selection-container",

  /** configs */
  view: null,
  routes: null,
  linesNumber: null,
  medicationFormattedDisplay: null,

  /** privates */
  resultCallback: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.routes = tm.jquery.Utils.isArray(this.routes) ? this.routes : [];
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;

    self.add(new tm.jquery.Label({
      html: self.medicationFormattedDisplay,
      cls: "TextData",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    }));

    for (var i = 1; i <= self.linesNumber; i++)
    {
      this._buildRowContainer(i);
    }
  },

  _buildRowContainer: function(lineNumber)
  {
    var self = this;
    var container = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch")});
    this.routes.forEach(function(route)
    {
      if (route.lineNumber == lineNumber)
      {
        var button = new tm.jquery.Button({
          cls: "btn-bubble",
          text: route.name,
          data: route,
          handler: function(button)
          {
            self.processResultData(button)
          }
        });
        container.add(button);
      }
    });

    self.add(container);
  },

  processResultData: function(button)
  {
    this.resultCallback(button.data);
  }
});