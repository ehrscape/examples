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

package com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class PerfusionSyringeLabelDto extends DataTransferObject
{
  private String patientName;
  private String patientBirthDate;
  private String patientCareProvider;
  private String patientRoomAndBed;

  private String therapyDisplayValue;
  private String prescribedBy;
  private String preparedBy;
  private String preparationStartedTime;

  private String barCode;    //temporary not used

  public String getPatientName()
  {
    return patientName;
  }

  public void setPatientName(final String patientName)
  {
    this.patientName = patientName;
  }

  public String getPatientBirthDate()
  {
    return patientBirthDate;
  }

  public void setPatientBirthDate(final String patientBirthDate)
  {
    this.patientBirthDate = patientBirthDate;
  }

  public String getPatientCareProvider()
  {
    return patientCareProvider;
  }

  public void setPatientCareProvider(final String patientCareProvider)
  {
    this.patientCareProvider = patientCareProvider;
  }

  public String getPatientRoomAndBed()
  {
    return patientRoomAndBed;
  }

  public void setPatientRoomAndBed(final String patientRoomAndBed)
  {
    this.patientRoomAndBed = patientRoomAndBed;
  }

  public String getTherapyDisplayValue()
  {
    return therapyDisplayValue;
  }

  public void setTherapyDisplayValue(final String therapyDisplayValue)
  {
    this.therapyDisplayValue = therapyDisplayValue;
  }

  public String getPrescribedBy()
  {
    return prescribedBy;
  }

  public void setPrescribedBy(final String prescribedBy)
  {
    this.prescribedBy = prescribedBy;
  }

  public String getPreparedBy()
  {
    return preparedBy;
  }

  public void setPreparedBy(final String preparedBy)
  {
    this.preparedBy = preparedBy;
  }

  public String getPreparationStartedTime()
  {
    return preparationStartedTime;
  }

  public void setPreparationStartedTime(final String preparationStartedTime)
  {
    this.preparationStartedTime = preparationStartedTime;
  }

  public String getBarCode()
  {
    return barCode;
  }

  public void setBarCode(final String barCode)
  {
    this.barCode = barCode;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientName", patientName)
        .append("patientBirthDate", patientBirthDate)
        .append("patientCareProvider", patientCareProvider)
        .append("patientRoomAndBed", patientRoomAndBed)
        .append("therapyDisplayValue", therapyDisplayValue)
        .append("prescribedBy", prescribedBy)
        .append("preparedBy", preparedBy)
        .append("preparationStartedTime", preparationStartedTime)
        .append("barCode", barCode);
  }
}
