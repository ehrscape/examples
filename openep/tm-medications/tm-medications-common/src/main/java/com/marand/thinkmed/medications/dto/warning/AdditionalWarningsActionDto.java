package com.marand.thinkmed.medications.dto.warning;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class AdditionalWarningsActionDto extends DataTransferObject implements JsonSerializable
{
  private String patientId;
  private List<String> abortTherapyIds = new ArrayList<>();
  private List<OverrideWarningDto> overrideWarnings = new ArrayList<>();
  private List<String> completeTaskIds = new ArrayList<>();

  public String getPatientId()
  {
    return patientId;
  }

  public void setPatientId(final String patientId)
  {
    this.patientId = patientId;
  }

  public List<String> getAbortTherapyIds()
  {
    return abortTherapyIds;
  }

  public void setAbortTherapyIds(final List<String> abortTherapyIds)
  {
    this.abortTherapyIds = abortTherapyIds;
  }

  public List<OverrideWarningDto> getOverrideWarnings()
  {
    return overrideWarnings;
  }

  public void setOverrideWarnings(final List<OverrideWarningDto> overrideWarnings)
  {
    this.overrideWarnings = overrideWarnings;
  }

  public List<String> getCompleteTaskIds()
  {
    return completeTaskIds;
  }

  public void setCompleteTaskIds(final List<String> completeTaskIds)
  {
    this.completeTaskIds = completeTaskIds;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientId", patientId)
        .append("abortTherapyIds", abortTherapyIds)
        .append("overrideWarnings", overrideWarnings)
        .append("completeTaskIds", completeTaskIds)
    ;
  }
}
