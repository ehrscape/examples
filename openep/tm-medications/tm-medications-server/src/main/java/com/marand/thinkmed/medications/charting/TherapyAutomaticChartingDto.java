package com.marand.thinkmed.medications.charting;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public abstract class TherapyAutomaticChartingDto extends DataTransferObject
{
  private final AutomaticChartingType type;
  private final String compositionUid;
  private final String instructionName;
  private final String patientId;

  protected TherapyAutomaticChartingDto(
      @Nonnull final AutomaticChartingType type,
      @Nonnull final String compositionUid,
      @Nonnull final String instructionName,
      @Nonnull final String patientId)
  {
    this.type = Preconditions.checkNotNull(type, "type");
    this.compositionUid = Preconditions.checkNotNull(compositionUid, "compositionUid");
    this.instructionName = Preconditions.checkNotNull(instructionName, "instructionName");
    this.patientId = Preconditions.checkNotNull(patientId, "patientId");
  }

  public AutomaticChartingType getType()
  {
    return type;
  }

  public String getCompositionUid()
  {
    return compositionUid;
  }

  public String getInstructionName()
  {
    return instructionName;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public abstract boolean isEnabled(@Nonnull final DateTime when);

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("compositionUid", compositionUid)
        .append("instructionName", instructionName)
        .append("patientId", patientId)
    ;
  }
}
