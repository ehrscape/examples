package com.marand.thinkmed.medications.dto.warning;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class OverrideWarningDto extends DataTransferObject
{
  private String therapyId;
  private List<String> warnings = new ArrayList<>();

  public String getTherapyId()
  {
    return therapyId;
  }

  public void setTherapyId(final String therapyId)
  {
    this.therapyId = therapyId;
  }

  public List<String> getWarnings()
  {
    return warnings;
  }

  public void setWarnings(final List<String> warnings)
  {
    this.warnings = warnings;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("therapyId", therapyId).append("warnings", warnings);
  }
}