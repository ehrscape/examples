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
Class.define('app.views.medications.common.dto.Medication', 'tm.jquery.Object', {

  id: null, /* number */
  name: null, /* String */
  shortName: null, /* String */
  genericName: null, /* String */
  medicationType: null, /* MedicationTypeEnum */

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },
  /**
   * @param {number} id
   */
  setId: function(id)
  {
    this.id = id;
  },
  /**
   * @returns {number | null}
   */
  getId: function()
  {
    return this.id;
  },
  /**
   * @param {String} name
   */
  setName: function(name)
  {
    this.name = name;
  },
  /**
   * @returns {String}
   */
  getName: function()
  {
    return this.name;
  },
  /**
   * @param {String} shortName
   */
  setShortName: function(shortName)
  {
    this.shortName = shortName;
  },
  /**
   * @returns {String}
   */
  getShortName: function()
  {
    return this.shortName;
  },
  /**
   * @param {String} genericName
   */
  setGenericName: function(genericName)
  {
    this.genericName = genericName;
  },
  /**
   * @returns {String}
   */
  getGenericName: function()
  {
    return this.genericName;
  },
  /**
   * @param {String} medicationType
   */
  setMedicationType: function(medicationType)
  {
    this.medicationType = medicationType;
  },
  /**
   * @returns {String}
   */
  getMedicationType: function()
  {
    return this.medicationType;
  },
  /**
   * @returns {boolean}
   */
  isMedicationUniversal: function()
  {
    return tm.jquery.Utils.isEmpty(this.id)
  },
  /**
   *
   * @returns {boolean}
   */
  isSolution: function()
  {
    return this.medicationType === app.views.medications.TherapyEnums.medicationTypeEnum.SOLUTION;
  },

  /**
   * @returns {boolean}
   */
  isOxygen: function()
  {
    return this.medicationType === app.views.medications.TherapyEnums.medicationTypeEnum.OXYGEN;
  },

  /**
   * @returns {String}
   */
  getDisplayName: function()
  {
    if (this.getGenericName())
    {
      return this.getGenericName() + ' (' + this.getName() + ')';
    }
    return this.getName();
  },

  /**
   * @returns {String}
   */
  getFormattedDisplayName: function()
  {
    if (tm.jquery.Utils.isEmpty(this.getGenericName()) || this.isSolution())
    {
      return "<span class='TextDataBold'>" + this.getName() + "</span>";
    }
    return "<span class='TextDataBold'>" + this.getGenericName() + "</span>"
        + " " + "<span class='TextData'>" + "(" + this.getName() + ")" + "</span>";
  }
});