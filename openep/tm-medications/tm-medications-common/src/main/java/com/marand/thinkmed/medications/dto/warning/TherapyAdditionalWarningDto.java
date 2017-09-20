package com.marand.thinkmed.medications.dto.warning;

import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class TherapyAdditionalWarningDto extends DataTransferObject implements JsonSerializable
{
  private final TherapyDto therapy;
  private final List<AdditionalWarningDto> warnings;

  public TherapyAdditionalWarningDto(final TherapyDto therapy, final List<AdditionalWarningDto> warnings)
  {
    this.therapy = therapy;
    this.warnings = warnings;
  }

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public List<AdditionalWarningDto> getWarnings()
  {
    return warnings;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("warnings", warnings)
    ;
  }
}
