/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
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

Class.define('app.views.medications.TherapyDisplayProvider', 'tm.jquery.Object', {
  /** members: configs */
  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getStatusIcon: function(status)
  {
    var enums = app.views.medications.TherapyEnums;
    if (status == enums.therapyStatusEnum.REVIEWED)
    {
      return "icon_reviewed";
    }
    if (status == enums.therapyStatusEnum.ABORTED)
    {
      return "icon_aborted";
    }
    if (status == enums.therapyStatusEnum.CANCELLED)
    {
      return "icon_cancelled";
    }
    if (status == enums.therapyStatusEnum.LATE)
    {
      return "icon_late";
    }
    if (status == enums.therapyStatusEnum.VERY_LATE)
    {
      return "icon_very_late";
    }
    if (status == enums.therapyStatusEnum.SUSPENDED)
    {
      return "icon_suspended";
    }
    return null;
  },

  getMedicationNameDisplay: function(medication, showGeneric, preferShortName)
  {
    var nameDisplay = "";
    var name = (preferShortName && medication.shortName) ? medication.shortName : medication.name;
    if (showGeneric && medication.genericName)
    {
      nameDisplay += "<span class='TextDataBold'>" + medication.genericName + "</span>";
      nameDisplay += "<span class='TextData'> (" + name + ")";
    }
    else
    {
      nameDisplay += "<span class='TextData'>" + name;
    }
    nameDisplay += "</span>";
    return nameDisplay;
  }
});