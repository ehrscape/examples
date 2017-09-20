package com.marand.thinkmed.medications.rule.parameters;

import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public class ParacetamolRuleForAdministrationParameters extends ParacetamolRuleParameters
{
  private TherapyDoseDto therapyDoseDto;
  private String administrationId;
  private String taskId;
  private TherapyDto therapyDto;
  private String patientId;
  private Interval searchInterval;

  public TherapyDoseDto getTherapyDoseDto()
  {
    return therapyDoseDto;
  }

  public void setTherapyDoseDto(final TherapyDoseDto therapyDoseDto)
  {
    this.therapyDoseDto = therapyDoseDto;
  }

  public String getAdministrationId()
  {
    return administrationId;
  }

  public void setAdministrationId(final String administrationId)
  {
    this.administrationId = administrationId;
  }

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(final String taskId)
  {
    this.taskId = taskId;
  }

  public TherapyDto getTherapyDto()
  {
    return therapyDto;
  }

  public void setTherapyDto(final TherapyDto therapyDto)
  {
    this.therapyDto = therapyDto;
  }

  public Interval getSearchInterval()
  {
    return searchInterval;
  }

  public void setSearchInterval(final Interval searchInterval)
  {
    this.searchInterval = searchInterval;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public void setPatientId(final String patientId)
  {
    this.patientId = patientId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("therapyDoseDto", therapyDoseDto)
        .append("administrationId", administrationId)
        .append("taskId", taskId)
        .append("therapyDto", therapyDto)
        .append("patientId", patientId)
        .append("searchInterval", searchInterval)
    ;
  }
}
