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
Class.define('app.views.medications.common.dto.TherapyAuditTrail', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.currentTherapy = app.views.medications.common.TherapyJsonConverter.convert(jsonObject.currentTherapy);
      config.originalTherapy = app.views.medications.common.TherapyJsonConverter.convert(jsonObject.originalTherapy);
      for (var i = 0; i < jsonObject.actionHistoryList.length; i++)
      {
        jsonObject.actionHistoryList[i] = app.views.medications.common.dto.TherapyActionHistory.fromJson(jsonObject.actionHistoryList[i]);
      }
      config.actionHistoryList = jsonObject.actionHistoryList;

      return new app.views.medications.common.dto.TherapyAuditTrail(config);
    }
  },
  currentTherapy: null, /* app.views.medications.common.dto.Therapy */
  originalTherapy: null, /* app.views.medications.common.dto.Therapy */
  actionHistoryList: null, /* Array */
  
  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getCurrentTherapy: function()
  {
    return this.currentTherapy;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} currentTherapy
   */
  setCurrentTherapy: function(currentTherapy)
  {
    this.currentTherapy = currentTherapy;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getOriginalTherapy: function()
  {
    return this.originalTherapy;
  },
  
  /**
   * @param {app.views.medications.common.dto.Therapy} originalTherapy
   */
  setOriginalTherapy: function(originalTherapy)
  {
    this.originalTherapy = originalTherapy;
  },

  /**
   * @returns {Array}
   */
  getActionHistoryList: function()
  {
    return this.actionHistoryList;
  },

  /**
   * @param {Array} actionHistoryList
   */
  setActionHistoryList: function(actionHistoryList)
  {
    this.actionHistoryList = actionHistoryList
  }

});
