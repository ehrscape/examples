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
Class.define('app.views.medications.common.dto.SaveMedicationOrder', 'tm.jquery.Object', {
  therapy: null, // app.views.medications.common.dto.Therapy
  actionEnum: null, /* app.views.medications.TherapyEnums.medicationOrderActionEnum */
  sourceId: null, /* string */
  linkCompositionUid: null, /* string */
  changeReasonDto: null, /* app.views.medications.common.dto.TherapyChangeReason */

  therapyStatus: app.views.medications.TherapyEnums.therapyStatusEnum.NORMAL, /* for displaying style */
  readOnly: false,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._applyTherapyStatus();
  },

  setActionEnum: function(enumValue)
  {
    this.actionEnum = enumValue;
    this._applyTherapyStatus();
  },
  setSourceId: function(value)
  {
    this.sourceId = value;
  },
  setChangeReasonDto: function(value)
  {
    this.changeReasonDto = value;
  },
  setReadOnly: function(value)
  {
    this.readOnly = value;
  },

  getTherapy: function()
  {
    return this.therapy;
  },
  getActionEnum: function()
  {
    return this.actionEnum;
  },
  getSourceId: function()
  {
    return this.sourceId;
  },
  getChangeReasonDto: function()
  {
    return this.changeReasonDto;
  },
  getTherapyStatus: function()
  {
    return this.therapyStatus;
  },
  isReadOnly: function()
  {
    return this.readOnly;
  },

  /**
   * @returns {string|null}
   */
  getLinkCompositionUid: function()
  {
    return this.linkCompositionUid;
  },
  /**
   * @param {string} linkCompositionUid
   */
  setLinkCompostionUid: function(linkCompositionUid)
  {
    this.linkCompositionUid = linkCompositionUid;
  },

  _applyTherapyStatus: function()
  {
    var medicationOrderEnum = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var therapyStatusEnum = app.views.medications.TherapyEnums.therapyStatusEnum;
    var actionEnum = this.getActionEnum();

    switch (actionEnum)
    {
      case medicationOrderEnum.ABORT:
        this.therapyStatus = therapyStatusEnum.ABORTED;
        break;
      case medicationOrderEnum.SUSPEND:
        this.therapyStatus = therapyStatusEnum.SUSPENDED;
        break;
      case medicationOrderEnum.SUSPEND_ADMISSION:
        this.therapyStatus = therapyStatusEnum.SUSPENDED;
        break;
      default:
        this.therapyStatus = therapyStatusEnum.NORMAL;
        break;
    };
  }
});