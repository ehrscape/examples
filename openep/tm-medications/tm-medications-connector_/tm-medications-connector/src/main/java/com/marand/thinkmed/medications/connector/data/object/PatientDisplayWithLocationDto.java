package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */
public class PatientDisplayWithLocationDto extends DataTransferObject
{
  private PatientDisplayDto patientDisplayDto;
  private String careProviderName;
  private String roomAndBed;

  public PatientDisplayWithLocationDto(
      final PatientDisplayDto patientDisplayDto,
      final String careProviderName,
      final String roomAndBed)
  {
    this.patientDisplayDto = patientDisplayDto;
    this.careProviderName = careProviderName;
    this.roomAndBed = roomAndBed;
  }

  public PatientDisplayDto getPatientDisplayDto()
  {
    return patientDisplayDto;
  }

  public void setPatientDisplayDto(final PatientDisplayDto patientDisplayDto)
  {
    this.patientDisplayDto = patientDisplayDto;
  }

  public String getCareProviderName()
  {
    return careProviderName;
  }

  public void setCareProviderName(final String careProviderName)
  {
    this.careProviderName = careProviderName;
  }

  public String getRoomAndBed()
  {
    return roomAndBed;
  }

  public void setRoomAndBed(final String roomAndBed)
  {
    this.roomAndBed = roomAndBed;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientDisplayDto", patientDisplayDto)
        .append("careProviderName", careProviderName)
        .append("roomAndBed", roomAndBed);
  }
}
