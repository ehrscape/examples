package com.marand.thinkmed.medications.dto.report;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class TherapySurgeryReportDto extends DataTransferObject
{
  private final PatientDataForTherapyReportDto patientData;
  private List<TherapySurgeryReportElementDto> elements = new ArrayList<>();
  private final String currentDate;

  public TherapySurgeryReportDto(
      final PatientDataForTherapyReportDto patientData,
      final List<TherapySurgeryReportElementDto> elements,
      final String currentDate)
  {
    this.patientData = patientData;
    this.elements = elements;
    this.currentDate = currentDate;
  }

  public PatientDataForTherapyReportDto getPatientData()
  {
    return patientData;
  }

  public List<TherapySurgeryReportElementDto> getElements()
  {
    return elements;
  }

  public String getCurrentDate()
  {
    return currentDate;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientData", patientData)
        .append("elements", elements)
        .append("currentDate", currentDate)
    ;
  }
}
