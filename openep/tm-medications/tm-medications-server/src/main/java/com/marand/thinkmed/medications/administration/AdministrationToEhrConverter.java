package com.marand.thinkmed.medications.administration;

import java.util.Locale;
import javax.annotation.Nonnull;

import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface AdministrationToEhrConverter
{
  MedicationAdministrationComposition buildMedicationAdministrationComposition(
      @Nonnull MedicationOrderComposition composition,
      @Nonnull AdministrationDto administration,
      @Nonnull MedicationActionEnum actionEnum,
      @Nonnull String composerName,
      @Nonnull String composerId,
      String centralCaseId,
      String careProviderId,
      @Nonnull DateTime when);

  MedicationAdministrationComposition buildSetChangeAdministrationComposition(
      @Nonnull MedicationOrderComposition composition,
      @Nonnull InfusionSetChangeDto administration,
      @Nonnull String composerName,
      @Nonnull String composerId,
      String centralCaseId,
      String careProviderId,
      @Nonnull Locale locale,
      @Nonnull DateTime when);
}
