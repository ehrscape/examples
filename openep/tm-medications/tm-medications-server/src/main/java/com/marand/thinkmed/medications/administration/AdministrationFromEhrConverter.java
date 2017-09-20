package com.marand.thinkmed.medications.administration;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;

/**
 * @author Nejc Korasa
 */
public interface AdministrationFromEhrConverter
{
  List<AdministrationDto> convertToAdministrationDtos(
      @Nonnull Map<String, List<MedicationAdministrationComposition>> administrations,
      @Nonnull List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs);

  AdministrationDto convertToAdministrationDto(
      @Nonnull MedicationAdministrationComposition administrationComp,
      @Nonnull Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      @Nonnull String therapyId);
}
