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
Class.define('app.views.medications.reconciliation.dto.DischargeSourceMedication', 'app.views.medications.reconciliation.dto.SourceMedication', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.reconciliation.dto.DischargeSourceMedication({
        therapy: app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapy),
        sourceId: jsonObject.sourceId,
        status: jsonObject.status,
        reviewed: jsonObject.reviewed,
        changeReasonDto: app.views.medications.common.dto.TherapyChangeReason.fromJson(jsonObject.changeReasonDto)
      });
    }
  },
  reviewed: false, /* boolean */
  status: null, /* app.views.medications.TherapyEnums.therapyStatusEnum  */
  changeReason: null, /* app.views.medications.common.dto.TherapyChangeReason */

  Constructor: function (config)
  {
    this.callSuper(config);
  },

  getStatus: function()
  {
    return this.status;
  },
  getChangeReasonDto: function()
  {
    return this.changeReasonDto;
  },
  isReviewed: function()
  {
    return this.reviewed === true;
  },
  // for display provider to find the status
  getTherapyStatus: function()
  {
    return this.status;
  },

  setStatus: function(status)
  {
    this.status = status;
  },
  setChangeReasonDto: function(therapyChangeReason)
  {
    this.changeReasonDto = therapyChangeReason;
  },
  setReviewed: function(value)
  {
    this.reviewed = value;
  }
});