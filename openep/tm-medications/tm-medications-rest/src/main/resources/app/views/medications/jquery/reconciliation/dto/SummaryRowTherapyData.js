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
Class.define('app.views.medications.reconciliation.dto.SummaryRowTherapyData', 'tm.jquery.Object', {
  therapy: null, // app.views.medications.common.dto.Therapy
  changeReasonDto: null, /* app.views.medications.common.dto.TherapyChangeReason */
  therapyStatus: app.views.medications.TherapyEnums.therapyStatusEnum.NORMAL, /* for displaying style */
  changes: null, /* change history */
  modifiedFromLastReview: null, /* boolean flag, marks the therapy as changed */

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this.changes = this.getConfigValue("changes", []);

    if (this.changes.length > 0)
    {
      this.modifiedFromLastReview = true;
    }
  },

  setTherapyStatus: function(value)
  {
    this.therapyStatus = value;
  },
  setModifiedFromLastReview: function(value)
  {
    this.modifiedFromLastReview = value;
  },

  getTherapy: function()
  {
    return this.therapy;
  },
  getChangeReasonDto: function()
  {
    return this.changeReasonDto;
  },
  getTherapyStatus: function()
  {
    return this.therapyStatus;
  },
  getChanges: function()
  {
    return this.changes;
  },
  isModifiedFromLastReview: function()
  {
    return this.modifiedFromLastReview === true;
  }
});