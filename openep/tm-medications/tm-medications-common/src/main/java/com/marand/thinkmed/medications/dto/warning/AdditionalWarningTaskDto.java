package com.marand.thinkmed.medications.dto.warning;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public abstract class AdditionalWarningTaskDto extends DataTransferObject implements JsonSerializable
{
  private final String taskId;

  protected AdditionalWarningTaskDto(@Nonnull final String taskId)
  {
    this.taskId = Preconditions.checkNotNull(taskId, "taskId must not be null!");
  }

  public String getTaskId()
  {
    return taskId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("taskId", taskId);
  }
}
