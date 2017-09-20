package com.marand.thinkmed.medications.mentalhealth;

import javax.annotation.Nonnull;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface MentalHealthFormHandler
{
  void saveNewMentalHealthForm(
      @Nonnull MentalHealthDocumentDto mentalHealthDocumentDto,
      NamedExternalDto careProvider,
      @Nonnull DateTime when);
}
