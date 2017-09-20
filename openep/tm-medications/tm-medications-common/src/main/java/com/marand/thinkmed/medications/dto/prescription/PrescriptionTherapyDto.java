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

package com.marand.thinkmed.medications.dto.prescription;

import com.marand.openehr.medications.tdo.MedicationAuthorisationSloveniaCluster.PrescriptionStatus;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class PrescriptionTherapyDto extends DataTransferObject implements JsonSerializable
{
  private String prescriptionTherapyId;
  private PrescriptionStatus prescriptionStatus;
  private TherapyDto therapy;

  public String getPrescriptionTherapyId()
  {
    return prescriptionTherapyId;
  }

  public void setPrescriptionTherapyId(final String prescriptionTherapyId)
  {
    this.prescriptionTherapyId = prescriptionTherapyId;
  }

  public PrescriptionStatus getPrescriptionStatus()
  {
    return prescriptionStatus;
  }

  public void setPrescriptionStatus(final PrescriptionStatus prescriptionStatus)
  {
    this.prescriptionStatus = prescriptionStatus;
  }

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("prescriptionTherapyId", prescriptionTherapyId)
        .append("prescriptionStatus", prescriptionStatus)
        .append("therapy", therapy);
  }
}
