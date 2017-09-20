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
Class.define('app.views.medications.common.dto.TherapyChange', 'tm.jquery.Object', {
  type: null, /* String */
  newValue: null, /* String|Object|null */
  oldValue: null, /* String|Object|null */
  
  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * 
   * @returns {String}
   */
  getType: function()
  {
    return this.type;
  },

  /**
   * @param {String} type
   */
  setType: function(type)
  {
    this.type = type;
  },
  
  /**
   * @returns {String|Object|null}
   */
  getNewValue: function()
  {
    return this.newValue;
  },
  
  /**
   * @param {String|Object|null} newValue
   */
  setNewValue: function(newValue)
  {
    this.newValue = newValue;
  },
  
  /**
   * @returns {String|Object|null}
   */
  getOldValue: function()
  {
    return this.oldValue;
  },
  
  /**
   * @param {String|Object|null} oldValue
   */
  setOldValue: function(oldValue)
  {
    this.oldValue = oldValue;
  },

  /**
   * @returns {boolean}
   */
  isChangeTypeDose: function()
  {
    var enums = app.views.medications.TherapyEnums;
    return this.getType() === enums.therapyChangeTypeEnum.VARIABLE_DOSE ||
        this.getType() === enums.therapyChangeTypeEnum.VARIABLE_DOSE_TO_DOSE ||
        this.getType() === enums.therapyChangeTypeEnum.DOSE_TO_VARIABLE_DOSE

  },

  /**
   * @returns {boolean}
   */
  isChangeTypeRate: function()
  {
    var enums = app.views.medications.TherapyEnums;
    return this.getType() === enums.therapyChangeTypeEnum.VARIABLE_RATE ||
    this.getType() === enums.therapyChangeTypeEnum.VARIABLE_RATE_TO_RATE ||
    this.getType() === enums.therapyChangeTypeEnum.RATE_TO_VARIABLE_RATE
  }
});