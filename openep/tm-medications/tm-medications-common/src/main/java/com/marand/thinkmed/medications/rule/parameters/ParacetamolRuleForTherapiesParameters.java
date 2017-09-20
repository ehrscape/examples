package com.marand.thinkmed.medications.rule.parameters;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.medications.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public class ParacetamolRuleForTherapiesParameters extends ParacetamolRuleParameters
{
  private List<TherapyDto> therapies = new ArrayList<>();
  private Double patientHeight;
  private Interval searchInterval;
  private String patientId;

  public void setTherapies(final List<TherapyDto> therapies)
  {
    this.therapies = therapies;
  }

  public List<TherapyDto> getTherapies()
  {
    return therapies;
  }

  public Double getPatientHeight()
  {
    return patientHeight;
  }

  public void setPatientHeight(final Double patientHeight)
  {
    this.patientHeight = patientHeight;
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
        .append("therapies", therapies)
        .append("patientHeight", patientHeight)
        .append("searchInterval", searchInterval)
        .append("patientId", patientId);
  }
}
