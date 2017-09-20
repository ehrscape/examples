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
Class.define('app.views.medications.common.MedicationOnDischargeTherapyDisplayProvider', 'app.views.medications.TherapyDisplayProvider', {
  showChangeHistory: false,
  showChangeReason: true,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getStatusClass: function(dto){
    var therapy = dto.therapy;

    if (dto instanceof app.views.medications.reconciliation.dto.MedicationOnDischarge)
    {
      var enums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;

      switch (dto.getStatus())
      {
        case enums.NOT_PRESCRIBED:
          return "aborted";
        case enums.EDITED_AND_PRESCRIBED:
          return "changed";
        default:
          return "normal";
      }
    }

    return "normal";
  },

  getStatusIcon: function(dto)
  {
    if (dto instanceof app.views.medications.reconciliation.dto.MedicationOnDischarge)
    {
      var enums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;
      switch (dto.getStatus())
      {
        case enums.NOT_PRESCRIBED:
          return "icon_aborted";
        default:
          return null;
      }
    }
    return null;
  },

  getBigIconContainerHtml: function(dto)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;
    var appFactory = view.getAppFactory();

    var options = this.getBigIconContainerOptions(dto);
    if (dto instanceof app.views.medications.reconciliation.dto.MedicationOnDischarge
        && dto.getStatus() === enums.EDITED_AND_PRESCRIBED)
    {
      options.layers.push({hpos: "right", vpos: "bottom", cls: "icon_changed"});
    }
    if (dto.getTherapy().isLinkedToAdmission())
    {
      options.layers.push({hpos: "left", vpos: "top", cls: "icon_linked_to_admission"});
    }
    return appFactory.createLayersContainerHtml(options);
  }
});