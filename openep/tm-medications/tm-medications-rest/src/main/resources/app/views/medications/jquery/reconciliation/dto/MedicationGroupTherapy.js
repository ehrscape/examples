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
Class.define('app.views.medications.reconciliation.dto.MedicationGroupTherapy', 'tm.jquery.Object', {
  therapy: null, // app.views.medications.common.dto.Therapy
  sourceGroupEnum: null, /* String */
  sourceId: null, /* String */
  status: null, /* String */
  changeReasonDto: null, /* app.views.medications.common.dto.TherapyChangeReason */

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  getTherapy: function()
  {
    return this.therapy;
  },
  getSourceGroupEnum: function()
  {
    return this.sourceGroupEnum;
  },
  getSourceId: function()
  {
    return this.sourceId;
  },
  getStatus: function()
  {
    return this.status;
  },
  getChangeReasonDto: function()
  {
    return this.changeReasonDto;
  },

  setTherapy: function(therapy)
  {
    this.therapy = therapy;
  },
  setSourceGroupEnum: function(sourceGroup)
  {
    this.sourceGroupEnum = sourceGroup;
  },
  setSourceId: function(sourceId)
  {
    this.sourceId = sourceId;
  },
  setStatus: function(status)
  {
    this.status = status;
  },
  setChangeReasonDto: function(therapyChangeReason)
  {
    this.changeReasonDto = therapyChangeReason;
  }
});