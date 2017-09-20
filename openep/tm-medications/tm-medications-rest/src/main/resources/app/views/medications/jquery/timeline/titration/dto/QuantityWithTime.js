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
Class.define('app.views.medications.timeline.titration.dto.QuantityWithTime', 'tm.jquery.Object', {

  time: null,
  quantity: null,
  bolusQuantity: null,
  bolusUnit: null,
  comment: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    if (this.time && !tm.jquery.Utils.isDate(this.time))
    {
      this.time = new Date(this.time);
    }
  },

  /**
   * @returns {Date|null}
   */
  getTime: function()
  {
    return this.time;
  },

  /**
   * @returns {Number}
   */
  getUtcTime: function()
  {
    var time = new Date(this.time);
    return Date.UTC(time.getFullYear(), time.getMonth(), time.getDate(), time.getHours(), time.getMinutes());
  },

  /**
   * @returns {Number|null}
   */
  getQuantity: function()
  {
    return this.quantity;
  },

  /**
   * @param {Number} quantity
   */
  setQuantity: function(quantity)
  {
    this.quantity = quantity;
  },

  /**
   * @returns {Number|null}
   */
  getBolusQuantity: function()
  {
    return this.bolusQuantity;
  },

  /**
   * @returns {String|null}
   */
  getBolusUnit: function()
  {
    return this.bolusUnit;
  },

  /**
   * @returns {String|null}
   */
  getComment: function()
  {
    return this.comment;
  },

  /**
   * @returns {Boolean}
   */
  isBolusAdministration: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getBolusQuantity());
  },

  /**
   * @returns {Boolean}
   */
  hasComment: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getComment());
  }
});