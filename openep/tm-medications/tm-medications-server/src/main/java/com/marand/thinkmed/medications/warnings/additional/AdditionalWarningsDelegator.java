package com.marand.thinkmed.medications.warnings.additional;

import java.util.Collection;
import java.util.Locale;
import javax.annotation.Nonnull;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface AdditionalWarningsDelegator
{
  Opt<AdditionalWarningsDto> getAdditionalWarnings(
      @Nonnull Collection<AdditionalWarningsType> types,
      @Nonnull String patientId,
      @Nonnull PatientDataForMedicationsDto patientData,
      @Nonnull DateTime when,
      @Nonnull Locale locale);
}
