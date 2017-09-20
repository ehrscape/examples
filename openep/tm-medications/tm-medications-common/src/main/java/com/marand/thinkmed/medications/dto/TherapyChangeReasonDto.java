package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
public class TherapyChangeReasonDto extends DataTransferObject implements JsonSerializable
{
  private CodedNameDto changeReason;
  private String comment;

  public CodedNameDto getChangeReason()
  {
    return changeReason;
  }

  public void setChangeReason(final CodedNameDto changeReason)
  {
    this.changeReason = changeReason;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("changeReason", changeReason)
        .append("comment", comment)
    ;
  }
}
