package com.marand.thinkmed.medications.administration;

import javax.annotation.Nonnull;

import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;

/**
 * @author Nejc Korasa
 */
public interface AdministrationSaver
{
  String save(
      @Nonnull String patientId,
      @Nonnull MedicationAdministrationComposition composition,
      @Nonnull AdministrationDto dto);
}
