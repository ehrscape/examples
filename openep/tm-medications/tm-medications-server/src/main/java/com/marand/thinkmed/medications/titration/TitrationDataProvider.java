package com.marand.thinkmed.medications.titration;

import java.util.Locale;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.dto.TitrationDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TitrationDataProvider
{
  TitrationDto getDataForTitration(
      @Nonnull String patientId,
      @Nonnull String therapyId,
      @Nonnull TitrationType titrationType,
      @Nonnull DateTime searchStart,
      @Nonnull DateTime searchEnd,
      @Nonnull DateTime when,
      @Nonnull Locale locale);
}
