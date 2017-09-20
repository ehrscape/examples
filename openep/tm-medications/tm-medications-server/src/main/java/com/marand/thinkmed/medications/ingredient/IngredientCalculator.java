package com.marand.thinkmed.medications.ingredient;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import com.google.common.collect.Multimap;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface IngredientCalculator
{
  /**
   * Calculates ingredient quantity in therapies for one day (24 hours) in unit.
   * @return Double
   */
  double calculateIngredientQuantityInTherapies(
      @Nonnull List<TherapyDto> therapies,
      @Nonnull Map<Long, MedicationDataDto> medicationDataDtoMap,
      Long ingredientId,
      MedicationRuleEnum ingredientRuleEnum,
      @Nonnull String unit);

  /**
   * Calculates ingredient quantity in administrations for interval.
   * @return Double
   */
  double calculateIngredientQuantityInAdministrations(
      TherapyDoseDto currentAdministrationTherapyDoseDto,
      TherapyDto currentTherapyDto,
      @Nonnull Multimap<String, AdministrationDto> administrationDtoMap,
      @Nonnull Map<String, TherapyDto> therapyDtoMap,
      @Nonnull Map<Long, MedicationDataDto> medicationDataMap,
      @Nonnull Interval searchInterval,
      Long ingredientId,
      MedicationRuleEnum ingredientRule,
      @Nonnull String unit);
}
