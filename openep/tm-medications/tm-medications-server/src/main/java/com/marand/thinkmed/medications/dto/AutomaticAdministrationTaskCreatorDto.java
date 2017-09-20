package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public class AutomaticAdministrationTaskCreatorDto extends DataTransferObject
{
  private final TherapyDto therapyDto;
  private final String patientId;
  private final DateTime lastAdministrationTime;

  public AutomaticAdministrationTaskCreatorDto(
      final TherapyDto therapyDto,
      final String patientId,
      final DateTime lastAdministrationTime)
  {
    this.therapyDto = therapyDto;
    this.patientId = patientId;
    this.lastAdministrationTime = lastAdministrationTime;
  }

  public TherapyDto getTherapyDto()
  {
    return therapyDto;
  }

  public DateTime getLastAdministrationTime()
  {
    return lastAdministrationTime;
  }


  public String getPatientId()
  {
    return patientId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapyDto", therapyDto)
        .append("patientId", patientId)
        .append("lastAdministrationTime", lastAdministrationTime)
    ;
  }
}
