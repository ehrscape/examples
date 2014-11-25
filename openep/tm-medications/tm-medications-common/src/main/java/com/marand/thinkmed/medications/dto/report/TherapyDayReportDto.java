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

import com.marand.maf.core.data.object.DataObject;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyDayReportDto extends DataObject
{
  private final boolean forEmptyReport;

  private int patientSortOrder;
  private PatientDataForTherapyReportDto patientData;

  private List<TherapyDayElementReportDto<SimpleTherapyDto>> simpleElements;

  private List<TherapyDayElementReportDto<ComplexTherapyDto>> complexElements;

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

  public List<TherapyDayElementReportDto<SimpleTherapyDto>> getSimpleElements()
  {
    return simpleElements;
  }

  public void setSimpleElements(final List<TherapyDayElementReportDto<SimpleTherapyDto>> simpleElements)
  {
    this.simpleElements = simpleElements;
  }

  public List<TherapyDayElementReportDto<ComplexTherapyDto>> getComplexElements()
  {
    return complexElements;
  }

  public void setComplexElements(final List<TherapyDayElementReportDto<ComplexTherapyDto>> complexElements)
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
