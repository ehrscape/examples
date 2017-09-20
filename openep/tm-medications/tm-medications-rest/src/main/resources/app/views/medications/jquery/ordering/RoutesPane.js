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
  cls: "routes-container",

  /** configs */
  view: null,
  changeEvent: null, //optional
  multipleRoutes: null,
  discretionaryRoutesDisabled: true,
  maxRouteButtons: 5,
  maxRouteCharLength: 37,
  overflow: 'visible',

  /** privates: components */
  routesButtonGroup: null,
  routesSelectBox: null,

  _oldSelectedRoutes: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._oldSelectedRoutes = [];
    this.setLayout(new tm.jquery.HFlexboxLayout({gap: 5}));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.routesButtonGroup = new tm.jquery.ButtonGroup({
      orientation: "horizontal",
      type: "checkbox"
    });

    this.routesSelectBox = new tm.jquery.SelectBox({
      width: 220,
      liveSearch: true,
      placeholder: self.view.getDictionary('select.route'),
      dropdownWidth: "stretch",
      dropdownHeight: 5,
      dropdownAlignment: "left",

      options: [],
      selections: [],

      allowSingleDeselect: true,
      multiple: true
    });

    if (this.changeEvent)
    {
      this.routesButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        var selectedRoutes = self.getSelectedRoutes();
        self.changeEvent(selectedRoutes)
      });
    }

    this.routesSelectBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (self.discretionaryRoutesDisabled != true)
      {
        var selections = component.getSelections();
        var selectionLength = selections.length;

        if (selectionLength === 1)
        {
          self._oldSelectedRoutes = [selections[0]];
          if (selections[0].discretionary != true) component.hidePluginDropdown();
        }
        else if (selectionLength >= 1)
        {
          var oldSelectedRoutes = self._oldSelectedRoutes;
          var oldSelectionLength = oldSelectedRoutes.length;

          if (oldSelectionLength > 0 && oldSelectionLength < selectionLength)
          {
            var newSelectedRoute = null;
            selections.forEach(function(item)
            {
              if (!self._oldSelectedRoutes.contains(item))
              {
                newSelectedRoute = item;
              }
            });

            var oldContainsDiscretionary = false;
            oldSelectedRoutes.forEach(function(item)
            {
              if (item.discretionary === true)
              {
                oldContainsDiscretionary = true;
              }
            });

            if (newSelectedRoute.discretionary != true || (newSelectedRoute.discretionary == true && oldContainsDiscretionary == false))
            {
              component.deselectAll(true);
              component.setSelections([newSelectedRoute], false);
              self._oldSelectedRoutes = [newSelectedRoute];
            }
            else
            {
              self._oldSelectedRoutes = selections;
            }
            if (newSelectedRoute.discretionary != true) component.hidePluginDropdown();
          }
          else
          {
            self._oldSelectedRoutes = selections;
          }
        }
        var selectedRoutes = self.getSelectedRoutes();
      }
      if (self.changeEvent)
      {
        self.changeEvent(selectedRoutes);
      }
    });
  },

  _buildGui: function()
  {
    this.add(this.routesButtonGroup);
    this.add(this.routesSelectBox);
  },

  /** public methods */
  setRoutes: function(routes, defaultRoute, preventEvent) //routes -> medicationRouteDto.java
  {
    var maxRoutes = this.maxRouteButtons;
    var maxRouteChars = this.maxRouteCharLength;

    var allRoutesStr = "";
    for (var i = 0; i < routes.length; i++)
    {
      allRoutesStr += routes[i].name;
    }

    if(routes.length >= maxRoutes || allRoutesStr.length > maxRouteChars)
    {
      this.routesButtonGroup.hide();
      this.routesSelectBox.show();
      this.multipleRoutes = true;
    }
    else
    {
      this.routesSelectBox.hide();
      this.routesButtonGroup.show();
      this.multipleRoutes = false;
    }
    var self = this;
    self._setRoutes(routes, defaultRoute);
  },

  _setRoutes: function(routes, defaultRoute, preventEvent)
  {
    var self = this;
    var previousRoutes = this.getSelectedRoutes();
    if (routes.length === 1)
    {
      defaultRoute = routes[0];
    }

    var preselectedRouteIds = [];
    if (previousRoutes.length > 0)
    {
      preselectedRouteIds = previousRoutes.map(function(route)
      {
        return route.id;
      });
    }
    else if (defaultRoute)
    {
      preselectedRouteIds = [defaultRoute.id];
    }

    routes.sort(function(route1, route2)
        {
          return route1.name.toLowerCase().localeCompare(route2.name.toLowerCase());
        }
    );

    if (this.multipleRoutes)
    {
      this.routesSelectBox.removeAllOptions();

      var options = [];

      var preselected = [];
      for (var i = 0; i < routes.length; i++)
      {
        var preselect = preselectedRouteIds.contains(routes[i].id);
        var unlicensedRoute = routes[i].unlicensedRoute;
        var discretionary = routes[i].discretionary && self.discretionaryRoutesDisabled != true;

        var cls = "route-option";
        if (unlicensedRoute)
        {
          cls += " route-option-unlicensed";
        }
        if (discretionary)
        {
          cls += " route-option-discretionary";
        }

        var option = tm.jquery.SelectBox.createOption(routes[i], routes[i].name, cls, null, preselect);

        if (preselect)
        {
          preselected.push(option.value);
        }

        options.push(option);
      }

      this.routesSelectBox.addOptions(options);
      this.routesSelectBox.setSelections(preselected, true);

      if (!preventEvent && this.changeEvent)
      {
        var selectedRoutes = this.getSelectedRoutes();
        if (selectedRoutes.length > 0)
        {
          this.changeEvent(selectedRoutes)
        }
      }
    }
    else
    {
      var buttons = [];
      for (var i = 0; i < routes.length; i++)
      {
        var preselect = preselectedRouteIds.contains(routes[i].id);
        var unlicensedRoute = routes[i].unlicensedRoute;
        var discretionary = routes[i].discretionary && self.discretionaryRoutesDisabled != true;

        var cls = "route-button";
        if (unlicensedRoute)
        {
          cls = "route-button route-button-unlicensed";
        }
        if (discretionary)
        {
          cls = "route-button route-button-discretionary";
        }

        var routeButton = new tm.jquery.Button({
          cls: cls,
          data: routes[i],
          text: routes[i].name,
          pressed: preselect,
          handler: function(comp)
          {
            var discretionary = comp.data.discretionary === true && self.discretionaryRoutesDisabled != true;
            if (comp.pressed === true && !discretionary)
            {
              self.routesButtonGroup.setSelection([comp], true);
            }
            else if (comp.pressed === true && discretionary)
            {
              var pressedButtons = self.routesButtonGroup.getSelection();
              pressedButtons.forEach(function(item)
              {
                if (item.data.discretionary != true)
                {
                  self.routesButtonGroup.setSelection([comp], true);
                }
              });
            }
          }
        });

        if (unlicensedRoute)
        {
          routeButton.setTooltip(tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary('unlicensed.route'), null, this.view));
        }
        if (discretionary)
        {
          routeButton.setTooltip(tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary('discretionary.route'), null, this.view));
        }

        buttons.push(routeButton);
      }

      this.routesButtonGroup.setButtons(buttons);
      if (!preventEvent && this.changeEvent)
      {
        var selectedRoutes = this.getSelectedRoutes();
        if (selectedRoutes.length > 0)
        {
          this.changeEvent(selectedRoutes)
        }
      }
    }
  },

  getSelectedRoutes: function()
  {
    if(this.multipleRoutes)
    {
      var selectedOptions = this.routesSelectBox.getSelections();
      if (selectedOptions.length > 0)
      {
        return selectedOptions;
      }
      return [];
    }
    else
    {
      // for some reason sometimes the button is pressed, but the validation fails.. returning the first 1 if there's only 1
      var buttons = this.routesButtonGroup.getButtons();
      if (buttons.length == 1) return [buttons[0].data];

      var selectedButtons = this.routesButtonGroup.getSelection();
      if (selectedButtons.length > 0)
      {
        return selectedButtons.map(function(button)
        {
          return button.data;
        });
      }
      return [];
    }
  },

  setSelectedRoute: function(routes)
  {
    if (tm.jquery.Utils.isArray(routes) && routes.length > 0)
    {
      if (this.multipleRoutes)
      {
        var options = this.routesSelectBox.getOptions();
        for (var i = 0; i < options.length; i++)
        {
          if (options[i].value.id === routes[0].id)
          {
            this.routesSelectBox.setSelections([options[i].value]);
            break;
          }
        }
      }
      else
      {
        var buttons = this.routesButtonGroup.getButtons();
        for (var i = 0; i < buttons.length; i++)
        {
          if (buttons[i].data.id == routes[0].id)
          {
            this.routesButtonGroup.setSelection([buttons[i]]);
            break;
          }
        }
      }
    }
    else
    {
      this.clear();
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
        var selectedRoutes = self.getSelectedRoutes();
        return selectedRoutes.length > 0 ? selectedRoutes : null;
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
  },

  clear: function()
  {
    this.routesButtonGroup.clearSelection(true);
    this.routesSelectBox.deselectAll(true);
  }
});

