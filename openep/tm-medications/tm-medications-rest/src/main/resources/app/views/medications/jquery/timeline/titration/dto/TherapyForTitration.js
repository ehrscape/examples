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
Class.define('app.views.medications.timeline.titration.dto.TherapyForTitration', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);

      if (jsonObject.therapy)
      {
        config.therapy = app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapy);
      }

      if (jsonObject.administrations)
      {
        config.administrations = jsonObject.administrations.map(function(item)
        {
          return new app.views.medications.timeline.titration.dto.QuantityWithTime(item);
        });
      }

      return new app.views.medications.timeline.titration.dto.TherapyForTitration(config);
    }
  },

  therapy: null,
  administrations: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.administrations = this.getConfigValue("administrations", []);
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {Array<app.views.medications.timeline.titration.dto.QuantityWithTime>}
   */
  getAdministrations: function()
  {
    return this.administrations;
  },
  /**
   * @returns {String}
   */
  getUnit: function()
  {
    return this.doseUnit;
  },

  /**
   * @returns {Number}
   */
  getInfusionFormulaAtIntervalStart: function()
  {
    return this.infusionFormulaAtIntervalStart;
  }
});