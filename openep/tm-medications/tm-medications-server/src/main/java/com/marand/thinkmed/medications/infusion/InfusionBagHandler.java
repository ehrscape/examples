package com.marand.thinkmed.medications.infusion;

import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface InfusionBagHandler
{
  /**
   * Recalculates planned infusion bag change time for therapy
   */
  void recalculateInfusionBagChange(
      @Nonnull String patientId,
      @Nonnull String therapyId,
      final AdministrationDto administrationDto,
      final String administrationId,
      @Nonnull DateTime actionTimestamp);

  /**
   * Calculates remaining infusion bag quantity for therapy at given time
   * @return Double
   */
  Double getRemainingInfusionBagQuantity(@Nonnull String patientId, @Nonnull String therapyId, @Nonnull DateTime when);
}
