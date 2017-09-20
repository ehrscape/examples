package com.marand.thinkmed.medications.mentalhealth;

import java.util.Collection;
import javax.annotation.Nonnull;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface MentalHealthFormProvider
{
  Opt<MentalHealthDocumentDto> getLatestMentalHealthDocument(@Nonnull String patientId);

  MentalHealthDocumentDto getMentalHealthDocument(@Nonnull String patientId, @Nonnull String compositionUId);

  Collection<MentalHealthDocumentDto> getMentalHealthDocuments(@Nonnull String patientId, Interval interval, Integer fetchCount);
}
