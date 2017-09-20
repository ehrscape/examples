package com.marand.thinkmed.medications.dto.warning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class AdditionalWarningsDto extends DataTransferObject implements JsonSerializable
{
  private List<TherapyAdditionalWarningDto> warnings = new ArrayList<>();
  private Set<String> taskIds = new HashSet<>();

  public List<TherapyAdditionalWarningDto> getWarnings()
  {
    return warnings;
  }

  public void setWarnings(final List<TherapyAdditionalWarningDto> warnings)
  {
    this.warnings = warnings;
  }

  public Set<String> getTaskIds()
  {
    return taskIds;
  }

  public void setTaskIds(final Set<String> taskIds)
  {
    this.taskIds = taskIds;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("warnings", warnings)
        .append("taskIds", taskIds)
    ;
  }
}
