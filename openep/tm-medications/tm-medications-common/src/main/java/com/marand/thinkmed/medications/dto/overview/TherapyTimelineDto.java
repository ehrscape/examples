package com.marand.thinkmed.medications.dto.overview;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class TherapyTimelineDto extends DataTransferObject
{
  private List<TherapyRowDto> therapyRows = new ArrayList<>();

  private final AdditionalWarningsDto additionalWarnings;
  private final EnumSet<AdditionalWarningsType> additionalWarningsTypes;

  public TherapyTimelineDto(
      final List<TherapyRowDto> therapyRows,
      final AdditionalWarningsDto additionalWarnings,
      final EnumSet<AdditionalWarningsType> additionalWarningsTypes)
  {
    this.therapyRows = therapyRows;
    this.additionalWarnings = additionalWarnings;
    this.additionalWarningsTypes = additionalWarningsTypes;
  }

  public List<TherapyRowDto> getTherapyRows()
  {
    return therapyRows;
  }

  public AdditionalWarningsDto getAdditionalWarnings()
  {
    return additionalWarnings;
  }

  public EnumSet<AdditionalWarningsType> getAdditionalWarningsTypes()
  {
    return additionalWarningsTypes;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapyRows", therapyRows)
        .append("additionalWarnings", additionalWarnings)
        .append("additionalWarningsTypes", additionalWarningsTypes)
    ;
  }
}
