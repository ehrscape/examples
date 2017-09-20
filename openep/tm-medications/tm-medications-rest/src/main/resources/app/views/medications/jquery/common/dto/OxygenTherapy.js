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
Class.define('app.views.medications.common.dto.OxygenTherapy', 'app.views.medications.common.dto.Therapy', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.start = tm.jquery.Utils.isEmpty(jsonObject.start) ? null : new Date(jsonObject.start);
      config.end = tm.jquery.Utils.isEmpty(jsonObject.end) ? null : new Date(jsonObject.end);
      config.createdTimestamp = tm.jquery.Utils.isEmpty(jsonObject.createdTimestamp) ? null :
          new Date(jsonObject.createdTimestamp);
      config.medication = !tm.jquery.Utils.isEmpty(jsonObject.medication) ?
          new app.views.medications.common.dto.Medication(jsonObject.medication) :
          null;

      if (config.startingDevice)
      {
        config.startingDevice = new app.views.medications.common.dto.OxygenStartingDevice(config.startingDevice);
      }

      return new app.views.medications.common.dto.OxygenTherapy(config);
    }
  },
  flowRate: null,
  flowRateUnit: null,
  flowRateMode: null,
  startingDevice: null,
  minTargetSaturation: null,
  maxTargetSaturation: null,
  humidification: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!this.flowRateUnit)
    {
      this.flowRateUnit = 'l/min'; /* default, so it's not absent anywhere */
    }
    this.setMedicationOrderFormType(app.views.medications.TherapyEnums.medicationOrderFormType.OXYGEN);
  },

  /**
   * @param {Number|null] value
   */
  setFlowRate: function(value)
  {
    this.flowRate = value;
  },
  setFlowRateUnit: function(value)
  {
    this.flowRateUnit = value;
  },
  setFlowRateMode: function(value)
  {
    this.flowRateMode = value;
  },
  /**
   * @param {app.views.medications.common.dto.OxygenStartingDevice|null} value
   */
  setStartingDevice: function(value)
  {
    this.startingDevice = value;
  },
  setMinTargetSaturation: function(value)
  {
    this.minTargetSaturation = value;
  },
  setMaxTargetSaturation: function(value)
  {
    this.maxTargetSaturation = value;
  },
  setHumidification: function(value)
  {
    this.humidification = value;
  },
  
  getFlowRate: function()
  {
    return this.flowRate;
  },
  getFlowRateUnit: function()
  {
    return this.flowRateUnit;
  },
  getFlowRateMode: function()
  {
    return this.flowRateMode;
  },
  /**
   * @returns {app.views.medications.common.dto.OxygenStartingDevice|null}
   */
  getStartingDevice: function()
  {
    return this.startingDevice;
  },
  getMinTargetSaturation: function()
  {
    return this.minTargetSaturation;
  },
  getMaxTargetSaturation: function()
  {
    return this.maxTargetSaturation;
  },
  /**
   * @returns {boolean}
   */
  isHumidification: function()
  {
    return this.humidification === true;
  },

  /**
   * @returns {boolean}
   */
  isHighFlowOxygen: function()
  {
    return this.flowRateMode === app.views.medications.TherapyEnums.flowRateMode.HIGH_FLOW;
  },

  /**
   * Override, anything other is impossible anyway.
   * @returns {boolean}
   */
  isOrderTypeOxygen: function()
  {
    return true;
  },

  /**
   * Override
   * @returns {boolean}
   */
  isTherapyWithDurationAdministrations: function()
  {
    return true;
  }
});
