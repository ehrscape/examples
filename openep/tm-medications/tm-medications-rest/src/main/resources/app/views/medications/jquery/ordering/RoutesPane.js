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

Class.define('app.views.medications.ordering.RoutesPane', 'tm.jquery.Container', {
  /** configs */
  view: null,
  changeEvent: null, //optional
  /** privates: components */
  routesButtonGroup: null,

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
    this.routesButtonGroup = new tm.jquery.ButtonGroup({orientation: "horizontal", type: "radio"});
    if (this.changeEvent)
    {
      this.routesButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        var selectedRoute = self.getSelectedRoute();
        self.changeEvent(selectedRoute)
      });
    }
  },

  _buildGui: function()
  {
    this.add(this.routesButtonGroup);
  },

  /** public methods */
  setRoutes: function(routes, defaultRoute)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    appFactory.createConditionTask(
        function()
        {
          self._setRoutes(routes, defaultRoute);
        },
        function()
        {
          return self.routesButtonGroup.isRendered();
        },
        20, 1000
    );
  },

  _setRoutes: function(routes, defaultRoute)
  {
    var previousRoute = this.getSelectedRoute();
    if (routes.length === 1)
    {
      defaultRoute = routes[0];
    }
    var preselectedRouteCode = previousRoute ? previousRoute.code : (defaultRoute ? defaultRoute.code : null);
    routes.sort(function(route1, route2)
        {
          return route1.name.toLowerCase().localeCompare(route2.name.toLowerCase());
        }
    );
    var buttons = [];
    for (var i = 0; i < routes.length; i++)
    {
      var preselect = preselectedRouteCode == routes[i].code;
      buttons.push(new tm.jquery.Button({data: routes[i], text: routes[i].name, pressed: preselect}));
    }
    this.routesButtonGroup.setButtons(buttons);
    if (this.changeEvent)
    {
      var selectedRoute = this.getSelectedRoute();
      if (selectedRoute != null)
      {
        this.changeEvent(selectedRoute)
      }
    }
  },

  getSelectedRoute: function()
  {
    var selectedButtons = this.routesButtonGroup.getSelection();
    if (selectedButtons.length > 0)
    {
      return selectedButtons[0].data;
    }
    return null;
  },

  setSelectedRoute: function(route)
  {
    if (route)
    {
      var buttons = this.routesButtonGroup.getButtons();
      for (var i = 0; i < buttons.length; i++)
      {
        if (buttons[i].data.code == route.code)
        {
          this.routesButtonGroup.setSelection([buttons[i]]);
          break;
        }
      }
    }
    else
    {
      this.routesButtonGroup.setSelection([]);
    }
  },

  getRoutesPaneValidations: function()
  {
    var self = this;
    var formFields = [];
    formFields.push(new tm.jquery.FormField({
      component: self,
      required: true,
      componentValueImplementationFn: function()
      {
        return self.getSelectedRoute();
      }
    }));
    return formFields;
  },

  requestFocus: function()
  {
    var buttons = this.routesButtonGroup.getButtons();
    if (buttons.length > 0)
    {
      buttons[0].focus();
    }
  }
});

