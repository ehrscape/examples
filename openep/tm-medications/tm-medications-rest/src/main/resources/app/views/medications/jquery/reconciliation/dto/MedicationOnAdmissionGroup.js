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
Class.define('app.views.medications.reconciliation.dto.MedicationOnAdmissionGroup', 'app.views.medications.reconciliation.dto.MedicationGroup', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var group = new app.views.medications.reconciliation.dto.MedicationOnAdmissionGroup({
        groupEnum: jsonObject.groupEnum,
        groupName: jsonObject.groupName
      });

      if (!tm.jquery.Utils.isEmpty(jsonObject.groupElements))
      {
        jsonObject.groupElements.forEach(function (therapyJsonObject)
        {
          group.getGroupElements().push(new app.views.medications.reconciliation.dto.SourceMedication({
            therapy: app.views.medications.common.TherapyJsonConverter.convert(therapyJsonObject.therapy),
            sourceId: therapyJsonObject.sourceId,
            reviewed: therapyJsonObject.reviewed
          }));
        }, this);
      }

      return group;
    }
  }
});