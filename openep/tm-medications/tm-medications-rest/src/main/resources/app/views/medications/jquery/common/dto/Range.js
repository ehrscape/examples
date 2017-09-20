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
Class.define('app.views.medications.common.dto.Range', 'tm.jquery.Object', {
  statics: {
    /**
     * @param {Number|null} min
     * @param {Number|null} max
     * @returns {app.views.medications.common.dto.Range|null}
     */
    createStrict: function(min, max){

      if (!min || !max) return null;

      return new app.views.medications.common.dto.Range({
        min: min,
        max: max
      });
    }
  },
  
  min: null,
  max: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {null|Number}
   */
  getMin: function()
  {
    return this.min;
  },

  /**
   * @returns {null|Number}
   */
  getMax: function()
  {
    return this.max;
  },

  /**
   * Override, as per http://jsclass.jcoglan.com/equality.html recommendation, based on jsClass's Range.
   * @returns {string}
   */
  hash: function()
  {
    var hash = (this.getMin() ? this.getMin() : '') + '..';
    hash += this.getMax() ? this.getMax() : '.';
    return hash;
  },

  /**
   * Override
   * @param {*} other
   * @returns {boolean}
   */
  equals: function(other)
  {
    return JS.isType(other, app.views.medications.common.dto.Range) &&
        other.getMin() === this.getMin() && other.getMax() === this.getMax();
  },

  /**
   * @returns {string}
   */
  toString: function()
  {
    return this.getMin() + ' - ' + this.getMax();
  }
});