package com.marand.thinkmed.medications.dto.warning;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class AdditionalWarningDto extends DataTransferObject implements JsonSerializable
{
  private final AdditionalWarningsType additionalWarningsType;
  private final MedicationsWarningDto warning;

  public AdditionalWarningDto(
      final AdditionalWarningsType additionalWarningsType,
      final MedicationsWarningDto warning)
  {
    this.additionalWarningsType = additionalWarningsType;
    this.warning = warning;
  }

  public AdditionalWarningsType getAdditionalWarningsType()
  {
    return additionalWarningsType;
  }

  public MedicationsWarningDto getWarning()
  {
    return warning;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("additionalWarningsType", additionalWarningsType)
        .append("warning", warning)
    ;
  }
}
