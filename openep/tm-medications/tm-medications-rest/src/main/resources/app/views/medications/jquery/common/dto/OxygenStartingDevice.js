/*
 * Copyright (c) 2010-2017 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.common.dto.OxygenStartingDevice', 'tm.jquery.Object', {
  route: null,
  routeType: null,
  /**
   * @param {Object} config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {string|null}
   */
  getRoute: function()
  {
    return this.route;
  },
  /**
   * @returns {number|null}
   */
  getRouteType: function()
  {
    return this.routeType;
  },

  /**
   * @param {string} value of type app.views.medications.TherapyEnums.oxygenDeliveryClusterRoute
   */
  setRoute: function(value)
  {
    this.route = value;
  },
  /**
   * @param {number|null} value
   */
  setRouteType: function(value)
  {
    this.routeType = value;
  },

  /**
   * @param {app.views.common.AppView} appView
   * @returns {String}
   */
  getDisplayText: function(appView)
  {
    if (appView && this.getRoute())
    {
      var routeText = appView.getDictionary('OxygenDeliveryCluster.Route.' + this.getRoute());
      if (this.getRouteType())
      {
        routeText += ' ';
        routeText += this.getRouteType();
      }
      return routeText;
    }
    return '';
  },

  /**
   * @returns {boolean}
   */
  isVenturiMask: function()
  {
    return this.getRoute() === app.views.medications.TherapyEnums.oxygenDeliveryClusterRoute.VENTURI_MASK
  }
});
