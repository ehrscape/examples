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
Class.define('app.views.medications.common.dto.TherapyDose', 'tm.jquery.Object', {

  therapyDoseTypeEnum: null,
  numerator: null,
  numeratorUnit: null,
  denominator: null,
  denominatorUnit: null,
  secondaryNumerator: null,
  secondaryNumeratorUnit: null,
  secondaryDenominator: null,
  secondaryDenominatorUnit: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {String} value
   */
  setTherapyDoseTypeEnum: function(value)
  {
    this.therapyDoseTypeEnum = value;
  },
  /**
   * @param {Number} value
   */
  setNumerator: function(value)
  {
    this.numerator = value;
  },
  /**
   * @param {String} value
   */
  setNumeratorUnit: function(value)
  {
    this.numeratorUnit = value;
  },
  /**
   * @param {Number} value
   */
  setDenominator: function(value)
  {
    this.denominator = value;
  },
  /**
   * @param {String} value
   */
  setDenominatorUnit: function(value)
  {
    this.denominatorUnit = value;
  },
  /**
   * @param {Number} value
   */
  setSecondaryNumerator: function(value)
  {
    this.secondaryNumerator = value;
  },
  /**
   * @param {String} value
   */
  setSecondaryNumeratorUnit: function(value)
  {
    this.secondaryNumeratorUnit = value;
  },
  /**
   * @param {Number} value
   */
  setSecondaryDenominator: function(value)
  {
    this.secondaryDenominator = value;
  },
  /**
   * @param {String} value
   */
  setSecondaryDenominatorUnit: function(value)
  {
    this.secondaryDenominatorUnit = value;
  },

  /**
   * @returns {String}
   */
  getTherapyDoseTypeEnum: function()
  {
    return this.therapyDoseTypeEnum;
  },
  /**
   * @returns {Number}
   */
  getNumerator: function()
  {
    return this.numerator;
  },
  /**
   * @returns {String}
   */
  getNumeratorUnit: function()
  {
    return this.numeratorUnit;
  },
  /**
   * @returns {Number}
   */
  getDenominator: function()
  {
    return this.denominator;
  },
  /**
   * @returns {String}
   */
  getDenominatorUnit: function()
  {
    return this.denominatorUnit;
  },
  /**
   * @returns {Number}
   */
  getSecondaryNumerator: function()
  {
    return this.secondaryNumerator;
  },
  /**
   * @returns {String}
   */
  getSecondaryNumeratorUnit: function()
  {
    return this.secondaryNumeratorUnit;
  },
  /**
   * @returns {Number}
   */
  getSecondaryDenominator: function()
  {
    return this.secondaryDenominator;
  },
  /**
   * @returns {String}
   */
  getSecondaryDenominatorUnit: function()
  {
    return this.secondaryDenominatorUnit;
  }
});