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
Class.define('app.views.medications.common.SaveMedicationOrderTherapyDisplayProvider', 'app.views.medications.TherapyDisplayProvider', {
  showChangeHistory: false,
  showChangeReason: false,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getStatusClass: function(dto){
    var therapy = dto.therapy;
    var therapyEndTime = therapy.end ? new Date(therapy.end) : null;
    var therapyEnded = therapyEndTime != null && therapyEndTime.getTime() <= CurrentTime.get().getTime() ? true : false;

    if (dto instanceof app.views.medications.common.dto.SaveMedicationOrder)
    {

      var enums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
      switch (dto.getActionEnum())
      {
        case enums.ABORT:
          return "aborted";
        case enums.SUSPEND:
          return "suspended";
        case enums.SUSPEND_ADMISSION:
          return "suspended";
        case enums.EDIT:
          return "changed";
        default:
          return therapyEnded ? "ended" : "normal";
      }
    }
    else
      return therapyEnded ? "ended" : "normal";
  },

  getStatusIcon: function(dto)
  {
    if (dto instanceof app.views.medications.common.dto.SaveMedicationOrder)
    {
      var enums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
      switch (dto.getActionEnum())
      {
        case enums.ABORT:
          return "icon_aborted";
        case enums.SUSPEND:
          return "icon_suspended";
        case enums.SUSPEND_ADMISSION:
          return "icon_suspended";
        default:
          return null;
      }
    }
    return null;
  },

  getBigIconContainerHtml: function(dto)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var appFactory = view.getAppFactory();

    var options = this.getBigIconContainerOptions(dto);
    if (dto instanceof app.views.medications.common.dto.SaveMedicationOrder && dto.getActionEnum() === enums.EDIT)
    {
      options.layers.push({hpos: "left", vpos: "top", cls: "icon_changed"});
    }
    return appFactory.createLayersContainerHtml(options);
  }
});