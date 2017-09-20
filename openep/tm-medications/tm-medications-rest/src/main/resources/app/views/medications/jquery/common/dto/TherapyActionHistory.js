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
Class.define('app.views.medications.common.dto.TherapyActionHistory', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.actionPerformedTime = new Date(jsonObject.actionPerformedTime);
      config.actionTakesEffectTime = !tm.jquery.Utils.isEmpty(jsonObject.actionTakesEffectTime) ?
          new Date(jsonObject.actionTakesEffectTime):
          null;
      config.changeReason = !tm.jquery.Utils.isEmpty(jsonObject.changeReason) ?
          app.views.medications.common.dto.TherapyChangeReason.fromJson(jsonObject.changeReason):
          null;
      for (var i = 0; i < jsonObject.changes.length; i++)
      {
        jsonObject.changes[i] = new app.views.medications.common.dto.TherapyChange(jsonObject.changes[i])
      }
      config.changes = jsonObject.changes;
      return new app.views.medications.common.dto.TherapyActionHistory(config);
    }
  },
  actionPerformedTime: null, /* Date */
  actionTakesEffectTime: null, /* Date|null */
  performer: null, /* String */
  therapyActionHistoryType: null, /* String */
  changeReason: null, /* app.views.medications.common.dto.TherapyChangeReason */
  changes: null, /* Array(app.views.medications.common.dto.TherapyChange) */
  
  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },
  
  /**
   * @returns {Date}
   */
  getActionPerformedTime: function()
  {
    return this.actionPerformedTime;
  },
  
  /**
   * @param {Date} actionPerformedTime
   */
  setActionPerformedTime: function(actionPerformedTime)
  {
    this.actionPerformedTime = actionPerformedTime;
  },
  
  /**
   * @returns {Date|null}
   */
  getActionTakesEffectTime: function()
  {
    return this.actionTakesEffectTime;
  },
  
  /**
   * @param {Date|null} actionTakesEffectTime
   */
  setActionTakesEffectTime: function(actionTakesEffectTime)
  {
    this.actionTakesEffectTime = actionTakesEffectTime;
  },
  
  /**
   * @returns {String}
   */
  getPerformer: function()
  {
    return this.performer;
  },
  
  /**
   * @param {String} performer
   */
  setPerformer: function(performer)
  {
    this.performer = performer;
  },
  
  /**
   * @returns {String}
   */
  getTherapyActionHistoryType: function()
  {
    return this.therapyActionHistoryType;
  },
  
  /**
   * @param {String} therapyActionHistoryType
   */
  setTherapyActionHistoryType: function(therapyActionHistoryType)
  {
    this.therapyActionHistoryType = therapyActionHistoryType;
  },
  
  /**
   * @returns {app.views.medications.common.dto.TherapyChangeReason|null}
   */
  getChangeReason: function()
  {
    return this.changeReason;
  },
  
  /**
   * @param {app.views.medications.common.dto.TherapyChangeReason|null} changeReason
   */
  setChangeReason: function(changeReason)
  {
    this.changeReason = changeReason;
  },
  
  /**
   * @returns {Array}
   */
  getChanges: function()
  {
    return this.changes;
  },
  
  /**
   * @param {Array} changes
   */
  setChanges: function(changes)
  {
    this.changes = changes;
  }
});