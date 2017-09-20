package com.marand.thinkmed.medications.administration;

import java.util.List;
import javax.annotation.Nonnull;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface AdministrationProvider
{
  /**
   * Finds administration compositions for instruction pairs in search interval and converts them to AdministrationDto.
   *
   * @param patientId Patients id.
   * @param instructionPairs Instructions for which administrations are loaded.
   * @param searchInterval Search interval.
   *
   * @return List of AdministrationDto.
   */
  List<AdministrationDto> getTherapiesAdministrations(
      @Nonnull String patientId,
      @Nonnull List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs,
      Interval searchInterval);
}
