package com.marand.thinkmed.medications.charting;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public class NormalInfusionAutomaticChartingDto extends TherapyAutomaticChartingDto
{
  public NormalInfusionAutomaticChartingDto(
      @Nonnull final String compositionUid,
      @Nonnull final String instructionName,
      @Nonnull final String patientId)
  {
    super(AutomaticChartingType.NORMAL_INFUSION, compositionUid, instructionName, patientId);
  }

  @Override
  public boolean isEnabled(@Nonnull final DateTime when)
  {
    return true;
  }
}
