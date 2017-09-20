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

package com.marand.thinkmed.medications.dto.report;

import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyDayReportDto extends DataTransferObject
{
  private final boolean forEmptyReport;

  private int patientSortOrder;
  private PatientDataForTherapyReportDto patientData;

  private List<TherapyDayElementReportDto> simpleElements;

  private List<TherapyDayElementReportDto> complexElements;

  public TherapyDayReportDto(final boolean forEmptyReport)
  {
    this.forEmptyReport = forEmptyReport;
  }

  public boolean isForEmptyReport()
  {
    return forEmptyReport;
  }

  public int getPatientSortOrder()
  {
    return patientSortOrder;
  }

  public void setPatientSortOrder(final int patientSortOrder)
  {
    this.patientSortOrder = patientSortOrder;
  }

  public PatientDataForTherapyReportDto getPatientData()
  {
    return patientData;
  }

  public void setPatientData(final PatientDataForTherapyReportDto patientData)
  {
    this.patientData = patientData;
  }

  public List<TherapyDayElementReportDto> getSimpleElements()
  {
    return simpleElements;
  }

  public void setSimpleElements(final List<TherapyDayElementReportDto> simpleElements)
  {
    this.simpleElements = simpleElements;
  }

  public List<TherapyDayElementReportDto> getComplexElements()
  {
    return complexElements;
  }

  public void setComplexElements(final List<TherapyDayElementReportDto> complexElements)
  {
    this.complexElements = complexElements;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientSortOrder", patientSortOrder)
        .append("patientData", patientData)
        .append("simpleElements", simpleElements)
        .append("complexElements", complexElements)
    ;
  }
}
