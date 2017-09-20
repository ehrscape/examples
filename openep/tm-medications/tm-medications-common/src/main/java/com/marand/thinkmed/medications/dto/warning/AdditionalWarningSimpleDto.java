package com.marand.thinkmed.medications.dto.warning;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class AdditionalWarningSimpleDto extends DataTransferObject implements JsonSerializable
{
  private final AdditionalWarningsType additionalWarningsType;
  private final String warning;

  public AdditionalWarningSimpleDto(final String warning, final AdditionalWarningsType additionalWarningsType)
  {
    this.warning = warning;
    this.additionalWarningsType = additionalWarningsType;
  }

  public String getWarning()
  {
    return warning;
  }

  public AdditionalWarningsType getAdditionalWarningsType()
  {
    return additionalWarningsType;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("warning", warning)
        .append("additionalWarningsType", additionalWarningsType)
    ;
  }
}
