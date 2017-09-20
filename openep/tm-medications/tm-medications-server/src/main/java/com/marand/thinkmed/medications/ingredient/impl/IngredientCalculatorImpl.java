package com.marand.thinkmed.medications.ingredient.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.business.util.TherapyUnitsConverter;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.ingredient.IngredientCalculator;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Minutes;

/**
 * @author Nejc Korasa
 */
public class IngredientCalculatorImpl implements IngredientCalculator
{
  @Override
  public double calculateIngredientQuantityInTherapies(
      @Nonnull final List<TherapyDto> therapies,
      @Nonnull final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      @Nonnull final String unit)
  {
    Preconditions.checkNotNull(therapies, "therapies must not be null");
    Preconditions.checkNotNull(medicationDataDtoMap, "medicationDataDtoList must not be null");
    Preconditions.checkArgument(
        ingredientId != null || ingredientRuleEnum != null,
        "ingredientId or ingredientRuleEnum must be defined");
    Preconditions.checkNotNull(unit, "unit must not be null");

    double quantitySum = 0.0;
    for (final TherapyDto therapy : therapies)
    {
      final MedicationOrderFormType medicationOrderFormType = therapy.getMedicationOrderFormType();

      Double quantity = null;
      if (MedicationOrderFormType.SIMPLE_ORDERS.contains(medicationOrderFormType))
      {
        final Long mainMedicationId = therapy.getMainMedicationId();
        if (medicationContainedInMap(medicationDataDtoMap, mainMedicationId))
        {
          quantity = calculateIngredientQuantityForSimpleTherapy(
              therapy,
              medicationDataDtoMap.get(mainMedicationId),
              ingredientId,
              ingredientRuleEnum,
              unit);
        }
      }
      else if (medicationOrderFormType == MedicationOrderFormType.COMPLEX)
      {
        quantity = calculateIngredientQuantityForComplexTherapy(
            therapy,
            medicationDataDtoMap,
            ingredientId,
            ingredientRuleEnum,
            unit);
      }
      else if (medicationOrderFormType != MedicationOrderFormType.OXYGEN)
      {
        throw new UnsupportedOperationException("this method only support SIMPLE or COMPLEX orders ");
      }

      if (quantity != null)
      {
        quantitySum += quantity;
      }
    }

    return quantitySum;
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  private boolean medicationContainedInMap(final Map<Long, MedicationDataDto> medicationDataDtoMap, final Long medicationId)
  {
    return medicationId != null && medicationDataDtoMap.get(medicationId) != null;
  }

  private Double calculateIngredientQuantityForSimpleTherapy(
      final TherapyDto therapyDto,
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    final SimpleTherapyDto simpleTherapyDto = (SimpleTherapyDto)therapyDto;

    final Double quantity;
    final boolean variable = simpleTherapyDto instanceof VariableSimpleTherapyDto;
    final Integer timesPerDay;

    if (variable)
    {
      quantity = getSimpleVariablePerDay(((VariableSimpleTherapyDto)simpleTherapyDto).getTimedDoseElements());
      timesPerDay = 1;
    }
    else
    {
      final SimpleDoseElementDto doseElement = ((ConstantSimpleTherapyDto)simpleTherapyDto).getDoseElement();

      quantity = Opt.resolve(doseElement::getQuantity).orElseGet(() -> getMaxDoseRangeQuantity(doseElement));

      final Integer calculatedTimesPerDay = getTimesPerDay(
          simpleTherapyDto.getDosingFrequency(),
          therapyDto.getMaxDailyFrequency());

      if (calculatedTimesPerDay == null)
      {
        return null;
      }
      else
      {
        timesPerDay = calculatedTimesPerDay;
      }
    }

    if (quantity == null)
    {
      return null;
    }

    final String strengthNumeratorUnit = getStrengthNumeratorUnit(medicationDataDto);
    final Double ingredientPercentage = getIngredientPercentage(medicationDataDto, ingredientId, ingredientRuleEnum, unit);

    return TherapyUnitsConverter.convertToUnit(timesPerDay * quantity * ingredientPercentage, strengthNumeratorUnit, unit);
  }

  private Double getMaxDoseRangeQuantity(final SimpleDoseElementDto doseElement)
  {
    return Opt.resolve(() -> doseElement.getDoseRange().getMaxNumerator()).orElse(null);
  }

  private Double calculateIngredientQuantityForComplexTherapy(
      final TherapyDto therapyDto,
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    final ComplexTherapyDto complexTherapyDto = (ComplexTherapyDto)therapyDto;

    final boolean continuousInfusion = complexTherapyDto.isContinuousInfusion();

    //noinspection IfMayBeConditional
    if (continuousInfusion)
    {
      return calculateIngredientQuantityForContinuousInfusion(
          complexTherapyDto,
          medicationDataDtoMap,
          ingredientId,
          ingredientRuleEnum,
          unit);
    }
    else
    {
      return calculateIngredientQuantityForNormalInfusion(
          complexTherapyDto,
          medicationDataDtoMap,
          ingredientId,
          ingredientRuleEnum,
          unit);
    }
  }

  private Double calculateIngredientQuantityForNormalInfusion(
      final ComplexTherapyDto complexTherapyDto,
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    final double ingredientQuantity = getIngredientQuantityOfInfusionIngredients(
        medicationDataDtoMap,
        complexTherapyDto.getIngredientsList(),
        ingredientId,
        ingredientRuleEnum,
        unit);

    final Integer timesPerDay = getTimesPerDay(
        complexTherapyDto.getDosingFrequency(),
        complexTherapyDto.getMaxDailyFrequency());

    return timesPerDay != null ? ingredientQuantity * timesPerDay : null;
  }

  private Double calculateIngredientQuantityForContinuousInfusion(
      final ComplexTherapyDto complexTherapyDto,
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    final List<InfusionIngredientDto> infusionIngredientDtoList = complexTherapyDto.getIngredientsList();

    // in ml per hour
    final Double rate = getContinuousInfusionRate(complexTherapyDto);

    if (rate != null)
    {
      //noinspection IfMayBeConditional
      if (infusionIngredientDtoList.size() == 1)
      {
        return calculateIngredientQuantityForContinuousInfusionWithOneIngredient(
            medicationDataDtoMap,
            infusionIngredientDtoList,
            rate,
            ingredientId,
            ingredientRuleEnum,
            unit);
      }
      else
      {
        return calculateIngredientQuantityForContinuousInfusionWithMultipleIngredients(
            complexTherapyDto,
            medicationDataDtoMap,
            infusionIngredientDtoList,
            rate,
            ingredientId,
            ingredientRuleEnum,
            unit);
      }
    }
    else
    {
      return null;
    }
  }

  private Double calculateIngredientQuantityForContinuousInfusionWithOneIngredient(
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final List<InfusionIngredientDto> infusionIngredientDtoList,
      final Double rate,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    final Long medicationId = infusionIngredientDtoList.get(0).getMedication().getId();
    if (medicationContainedInMap(medicationDataDtoMap, medicationId))
    {
      final MedicationDataDto medicationDataDto = medicationDataDtoMap.get(medicationId);

      final Double ingredientQuantityPerMl = getIngredientQuantityInOneMl(
          medicationDataDto,
          ingredientId,
          ingredientRuleEnum,
          unit);

      return ingredientQuantityPerMl * rate * 24;
    }

    return null;
  }

  private Double calculateIngredientQuantityForContinuousInfusionWithMultipleIngredients(
      final ComplexTherapyDto complexTherapyDto,
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final List<InfusionIngredientDto> infusionIngredientDtoList,
      final Double rate,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    if (complexTherapyDto.getVolumeSum() != null)
    {
      final double ingredientQuantity = getIngredientQuantityOfInfusionIngredients(
          medicationDataDtoMap,
          infusionIngredientDtoList,
          ingredientId,
          ingredientRuleEnum,
          unit);

      final Double volumeSumInMl = TherapyUnitsConverter.convertToUnit(
          complexTherapyDto.getVolumeSum(),
          complexTherapyDto.getVolumeSumUnit(),
          "ml");

      if (volumeSumInMl != null)
      {
        return ingredientQuantity / volumeSumInMl * rate * 24;
      }
    }

    return null;
  }

  @Override
  public double calculateIngredientQuantityInAdministrations(
      final TherapyDoseDto currentAdministrationTherapyDoseDto,
      final TherapyDto currentTherapyDto,
      @Nonnull final Multimap<String, AdministrationDto> administrationDtoMap,
      @Nonnull final Map<String, TherapyDto> therapyDtoMap,
      @Nonnull final Map<Long, MedicationDataDto> medicationDataMap,
      @Nonnull final Interval searchInterval,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      @Nonnull final String unit)
  {
    Preconditions.checkNotNull(administrationDtoMap, "administrationDtoMap must not be null");
    Preconditions.checkNotNull(medicationDataMap, "medicationDataMap must not be null");
    Preconditions.checkNotNull(therapyDtoMap, "therapyDtoMap must not be null");
    Preconditions.checkNotNull(searchInterval, "searchInterval must not be null");
    Preconditions.checkArgument(
        ingredientId != null || ingredientRule != null,
        "ingredientId or ingredientRuleEnum must be defined");
    Preconditions.checkNotNull(unit, "unit must not be null");

    double quantitySumInUnit = 0.0;

    final Multimap<String, AdministrationDto> sortedAdministrationsMultimap = sortAdministrationsByTime(administrationDtoMap);
    for (final String therapyId : sortedAdministrationsMultimap.keySet())
    {
      final Collection<AdministrationDto> administrations = administrationDtoMap.get(therapyId);
      for (final AdministrationDto administration : administrations)
      {
        final TherapyDto therapyDto = therapyDtoMap.get(therapyId);
        final AdministrationTypeEnum administrationType = administration.getAdministrationType();

        final boolean correctType = administrationType == AdministrationTypeEnum.START
            || administrationType == AdministrationTypeEnum.ADJUST_INFUSION
            || administrationType == AdministrationTypeEnum.BOLUS;

        final boolean correctResult = administration.getAdministrationResult() == AdministrationResultEnum.GIVEN
            || administration.getAdministrationResult() == AdministrationResultEnum.SELF_ADMINISTERED;

        if (correctType && correctResult)
        {
          final double quantity = calculateIngredientQuantityForAdministration(
              sortedAdministrationsMultimap,
              medicationDataMap,
              searchInterval,
              therapyId,
              ingredientId,
              ingredientRule,
              administration,
              therapyDto,
              unit);

          quantitySumInUnit += quantity;
        }
      }
    }

    if (currentTherapyDto != null && currentAdministrationTherapyDoseDto != null)
    {
      quantitySumInUnit += getIngredientQuantityOfCurrentAdministration(
          currentAdministrationTherapyDoseDto,
          currentTherapyDto,
          medicationDataMap,
          ingredientId,
          ingredientRule,
          unit);
    }

    return quantitySumInUnit;
  }

  private double calculateIngredientQuantityForAdministration(
      @Nonnull final Multimap<String, AdministrationDto> administrationDtoMap,
      @Nonnull final Map<Long, MedicationDataDto> medicationDataMap,
      @Nonnull final Interval searchInterval,
      final String therapyId,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final AdministrationDto administration,
      final TherapyDto therapyDto,
      final String unit)
  {
    final TherapyDoseDto administeredDose = getAdministeredDose(administration);
    final TherapyDoseTypeEnum therapyDoseTypeEnum = administeredDose.getTherapyDoseTypeEnum();
    final boolean isInSearchInterval = searchInterval.contains(administration.getAdministrationTime())
        || administration.getAdministrationTime().equals(searchInterval.getEnd());

    if (therapyDoseTypeEnum == TherapyDoseTypeEnum.QUANTITY && isInSearchInterval)
    {
      final Long mainMedicationId = therapyDto.getMainMedicationId();
      if (medicationContainedInMap(medicationDataMap, mainMedicationId))
      {
        final MedicationDataDto medicationDataDto = medicationDataMap.get(mainMedicationId);
        return calculateIngredientQuantityForQuantityDoseType(
            administeredDose,
            medicationDataDto,
            ingredientId,
            ingredientRule,
            unit);
      }
    }
    else if (TherapyDoseTypeEnum.WITH_RATE.contains(therapyDoseTypeEnum))
    {
      return calculateIngredientQuantityForRateAdministration(
          administrationDtoMap,
          medicationDataMap,
          searchInterval,
          therapyId,
          administration,
          (ComplexTherapyDto)therapyDto,
          administeredDose,
          ingredientId,
          ingredientRule,
          unit);
    }
    else if (therapyDoseTypeEnum == TherapyDoseTypeEnum.VOLUME_SUM && isInSearchInterval)
    {
      return calculateIngredientQuantityForVolumeSumDoseType(
          medicationDataMap,
          (ComplexTherapyDto)therapyDto,
          administeredDose,
          ingredientId,
          ingredientRule,
          unit);
    }
    return 0.0;
  }

  private TherapyDoseDto getAdministeredDose(final AdministrationDto administration)
  {
    final AdministrationTypeEnum administrationType = administration.getAdministrationType();

    return administrationType == AdministrationTypeEnum.START ?
           ((StartAdministrationDto)administration).getAdministeredDose() :
           ((AdjustInfusionAdministrationDto)administration).getAdministeredDose();
  }

  private double getIngredientQuantityOfCurrentAdministration(
      final TherapyDoseDto currentAdministrationTherapyDoseDto,
      final TherapyDto currentTherapyDto,
      final Map<Long, MedicationDataDto> medicationDataMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule, final String unit)
  {
    if (currentAdministrationTherapyDoseDto.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.QUANTITY)
    {
      final Long medicationId = currentTherapyDto.getMainMedicationId();
      if (medicationContainedInMap(medicationDataMap, medicationId))
      {
        return calculateIngredientQuantityForQuantityDoseType(
            currentAdministrationTherapyDoseDto,
            medicationDataMap.get(medicationId),
            ingredientId,
            ingredientRule, unit);
      }
    }
    else if (currentAdministrationTherapyDoseDto.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.VOLUME_SUM)
    {
      return calculateIngredientQuantityForVolumeSumDoseType(
          medicationDataMap,
          (ComplexTherapyDto)currentTherapyDto,
          currentAdministrationTherapyDoseDto,
          ingredientId,
          ingredientRule, unit);
    }

    return 0.0;
  }

  private double calculateIngredientQuantityForQuantityDoseType(
      final TherapyDoseDto administeredDose,
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final String unit)
  {
    final Double ingredientPercentage = getIngredientPercentage(medicationDataDto, ingredientId, ingredientRule, unit);
    final Double administeredQuantity = administeredDose.getNumerator();
    final String administeredUnit = administeredDose.getNumeratorUnit();

    final Double convertedToUnit = TherapyUnitsConverter.convertToUnit(administeredQuantity, administeredUnit, unit);

    return convertedToUnit != null ? ingredientPercentage * convertedToUnit : 0.0;
  }

  private Multimap<String, AdministrationDto> sortAdministrationsByTime(final Multimap<String, AdministrationDto> administrationDtoMap)
  {
    final Multimap<String, AdministrationDto> sorted = ArrayListMultimap.create();
    administrationDtoMap.keySet().forEach(
        t ->
            sorted.putAll(t, administrationDtoMap.get(t)
                .stream()
                .sorted(Comparator.comparing(AdministrationDto::getAdministrationTime))
                .collect(Collectors.toList())));

    return sorted;
  }

  private double calculateIngredientQuantityForRateAdministration(
      final Multimap<String, AdministrationDto> administrationDtoMap,
      final Map<Long, MedicationDataDto> medicationDataMap,
      final Interval searchInterval,
      final String therapyId,
      final AdministrationDto administration,
      final ComplexTherapyDto therapyDto,
      final TherapyDoseDto administeredDose,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final String unit)
  {
    final Double quantity;
    final Double rate = administeredDose.getNumerator();

    final int minutesDuration = calculateRateDurationInterval(
        administration,
        searchInterval,
        therapyId,
        administrationDtoMap);

    final List<InfusionIngredientDto> infusionIngredientDtoList = therapyDto.getIngredientsList();

    if (infusionIngredientDtoList.size() == 1)
    {
      final Long ingredientMedicationId = infusionIngredientDtoList.get(0).getMedication().getId();
      if (medicationContainedInMap(medicationDataMap, ingredientMedicationId))
      {
        final double ingredientQuantityInMlPerHour = getIngredientQuantityInOneMl(
            medicationDataMap.get(ingredientMedicationId),
            ingredientId,
            ingredientRule,
            unit);

        quantity = ingredientQuantityInMlPerHour * rate * minutesDuration / 60;
      }
      else
      {
        quantity = 0.0;
      }
    }
    else
    {
      final double ingredientQuantity = getIngredientQuantityOfInfusionIngredients(
          medicationDataMap,
          infusionIngredientDtoList,
          ingredientId,
          ingredientRule,
          unit);

      final Double volumeSumInMl = TherapyUnitsConverter.convertToUnit(
          therapyDto.getVolumeSum(),
          therapyDto.getVolumeSumUnit(),
          "ml");

      quantity = volumeSumInMl != null ? ingredientQuantity / volumeSumInMl * rate * minutesDuration / 60 : 0.0;
    }

    return quantity;
  }

  private int calculateRateDurationInterval(
      final AdministrationDto administrationDto,
      final Interval searchInterval,
      final String therapyId,
      final Multimap<String, AdministrationDto> administrationDtoMap)
  {
    if (administrationDto.getAdministrationTime().isAfter(searchInterval.getEnd()))
    {
      return 0;
    }

    final List<AdministrationDto> laterAdministrationsForTherapy = getLaterAdministrationsForTherapy(
        therapyId,
        administrationDto,
        administrationDtoMap);

    if (laterAdministrationsForTherapy.isEmpty())
    {
      final Interval administrationDurationInterval =
          new Interval(administrationDto.getAdministrationTime(), searchInterval.getEnd());

      final Interval overlapInterval = searchInterval.overlap(administrationDurationInterval);
      return Minutes.minutesIn(overlapInterval).getMinutes();
    }
    else
    {
      DateTime intervalEnd = searchInterval.getEnd();

      for (final AdministrationDto laterAdministrationDto : laterAdministrationsForTherapy)
      {
        if (laterAdministrationDto.getAdministrationType() == AdministrationTypeEnum.STOP
            || laterAdministrationDto.getAdministrationType() == AdministrationTypeEnum.ADJUST_INFUSION)
        {
          final DateTime laterAdministrationTime = laterAdministrationDto.getAdministrationTime();
          if (laterAdministrationTime != null && laterAdministrationTime.isBefore(intervalEnd))
          {
            intervalEnd = laterAdministrationTime;
          }
        }
      }

      final Interval administrationDurationInterval = new Interval(administrationDto.getAdministrationTime(), intervalEnd);
      final Interval overlapInterval = searchInterval.overlap(administrationDurationInterval);
      return Minutes.minutesIn(overlapInterval).getMinutes();
    }
  }

  private List<AdministrationDto> getLaterAdministrationsForTherapy(
      final String therapyId,
      final AdministrationDto searchAdministrationDto,
      final Multimap<String, AdministrationDto> administrationDtoMap)
  {
    return administrationDtoMap.get(therapyId)
        .stream()
        .filter(administration -> administration.getTherapyId().equals(searchAdministrationDto.getTherapyId())
            && !administration.getAdministrationId().equals(searchAdministrationDto.getAdministrationId())
            && administration.getAdministrationTime().isAfter(searchAdministrationDto.getAdministrationTime()))
        .collect(Collectors.toList());
  }

  private double calculateIngredientQuantityForVolumeSumDoseType(
      final Map<Long, MedicationDataDto> medicationDataMap,
      final ComplexTherapyDto therapyDto,
      final TherapyDoseDto administeredDose,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final String unit)
  {
    final List<InfusionIngredientDto> infusionIngredientDtoList = therapyDto.getIngredientsList();

    final double ingredientQuantity = getIngredientQuantityOfInfusionIngredients(
        medicationDataMap,
        infusionIngredientDtoList,
        ingredientId,
        ingredientRule, unit);

    final Double therapyVolumeSum = TherapyUnitsConverter.convertToUnit(
        therapyDto.getVolumeSum(),
        therapyDto.getVolumeSumUnit(),
        "ml");

    final Double administrationVolumeSum = TherapyUnitsConverter.convertToUnit(
        administeredDose.getNumerator(),
        administeredDose.getNumeratorUnit(),
        "ml");

    return therapyVolumeSum != null && administrationVolumeSum != null
           ? administrationVolumeSum / therapyVolumeSum * ingredientQuantity
           : 0.0;
  }

  private double getIngredientQuantityOfInfusionIngredients(
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final List<InfusionIngredientDto> infusionIngredientDtoList,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    double ingredientQuantitySum = 0.0;
    for (final InfusionIngredientDto infusionIngredientDto : infusionIngredientDtoList)
    {
      final Long medicationId = infusionIngredientDto.getMedication().getId();
      if (medicationContainedInMap(medicationDataDtoMap, medicationId))
      {
        final MedicationDataDto medicationDataDto = medicationDataDtoMap.get(medicationId);
        if (medicationDataDto != null)
        {
          final double ingredientQuantity = getIngredientQuantityForInfusionIngredient(
              medicationDataDto,
              infusionIngredientDto,
              ingredientId,
              ingredientRuleEnum,
              unit);

          ingredientQuantitySum += ingredientQuantity;
        }
      }
    }

    return ingredientQuantitySum;
  }

  private double getIngredientQuantityForInfusionIngredient(
      final MedicationDataDto medicationDataDto,
      final InfusionIngredientDto infusionIngredientDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    final Double quantity = infusionIngredientDto.getQuantity();
    double ingredientQuantity = 0.0;

    if (quantity != null)
    {
      final double ingredientPercentage = getIngredientPercentage(medicationDataDto, ingredientId, ingredientRuleEnum, unit);
      final Double quantityInUnit = TherapyUnitsConverter.convertToUnit(
          quantity,
          infusionIngredientDto.getQuantityUnit(),
          unit);

      if (quantityInUnit != null)
      {
        ingredientQuantity = ingredientPercentage * quantityInUnit;
      }
    }

    return ingredientQuantity;
  }

  private double getIngredientPercentage(
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    if (medicationDataDto.getMedicationIngredients().size() == 1)
    {
      return isSearchIngredient(ingredientId, ingredientRuleEnum, medicationDataDto.getMedicationIngredients().get(0))
             ? 1.0
             : 0.0;
    }

    double ingredientQuantitySum = 0.0;
    final MedicationIngredientDto descriptiveIngredient = medicationDataDto.getDescriptiveIngredient();
    if (descriptiveIngredient != null && descriptiveIngredient.getStrengthNumerator() != null)
    {
      final Double quantitySum = TherapyUnitsConverter.convertToUnit(
          descriptiveIngredient.getStrengthNumerator(),
          descriptiveIngredient.getStrengthNumeratorUnit(), unit);

      for (final MedicationIngredientDto medicationIngredientDto : medicationDataDto.getMedicationIngredients())
      {
        if (isSearchIngredient(ingredientId, ingredientRuleEnum, medicationIngredientDto))
        {
          final Double ingredientQuantity = TherapyUnitsConverter.convertToUnit(
              medicationIngredientDto.getStrengthNumerator(),
              medicationIngredientDto.getStrengthNumeratorUnit(),
              unit);

          if (ingredientQuantity != null)
          {
            ingredientQuantitySum += ingredientQuantity;
          }
        }
      }

      return quantitySum != null ? ingredientQuantitySum / quantitySum : 0.0;
    }
    else
    {
      double otherIngredientQuantitySum = 0.0;

      for (final MedicationIngredientDto medicationIngredientDto : medicationDataDto.getMedicationIngredients())
      {
        final Double ingredientQuantity = TherapyUnitsConverter.convertToUnit(
            medicationIngredientDto.getStrengthNumerator(),
            medicationIngredientDto.getStrengthNumeratorUnit(),
            unit);

        if (ingredientQuantity != null)
        {
          if (isSearchIngredient(ingredientId, ingredientRuleEnum, medicationIngredientDto))
          {
            ingredientQuantitySum += ingredientQuantity;
          }
          else
          {
            otherIngredientQuantitySum += ingredientQuantity;
          }
        }
      }

      return ingredientQuantitySum / (otherIngredientQuantitySum + ingredientQuantitySum);
    }
  }

  private boolean isSearchIngredient(
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final MedicationIngredientDto medicationIngredientDto)
  {
    final boolean ruleEnumMatches = ingredientRuleEnum == null
        || medicationIngredientDto.getIngredientRule() == ingredientRuleEnum;

    final boolean ingredientIdMatches = ingredientId == null
        || medicationIngredientDto.getIngredientId() == ingredientId;

    return ruleEnumMatches && ingredientIdMatches;
  }

  private double getIngredientQuantityInOneMl(
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final String unit)
  {
    double ingredientQuantity = 0.0;
    for (final MedicationIngredientDto medicationIngredientDto : medicationDataDto.getMedicationIngredients())
    {
      if (isSearchIngredient(ingredientId, ingredientRuleEnum, medicationIngredientDto))
      {
        final Double ingredientInUnit = TherapyUnitsConverter.convertToUnit(
            medicationIngredientDto.getStrengthNumerator(),
            medicationIngredientDto.getStrengthNumeratorUnit(),
            unit);

        if (ingredientInUnit != null
            && medicationIngredientDto.getStrengthDenominator() != null
            && TherapyUnitsConverter.isLiquidUnit(medicationIngredientDto.getStrengthDenominatorUnit()))
        {
          final Double ingredientMl =
              TherapyUnitsConverter.convertToUnit(
                  medicationIngredientDto.getStrengthDenominator(),
                  medicationIngredientDto.getStrengthDenominatorUnit(),
                  "ml");

          if (ingredientMl != null)
          {
            ingredientQuantity += ingredientInUnit / ingredientMl;
          }
        }
      }
    }

    return ingredientQuantity;
  }

  private double getInfusionRateForVariableComplexTherapy(final VariableComplexTherapyDto complexTherapyDto)
  {
    final List<TimedComplexDoseElementDto> timedDoseElements = complexTherapyDto.getTimedDoseElements();

    double durationInMinutes = 0.0;
    double mlQuantitySum = 0.0;
    for (int i = 0; i < timedDoseElements.size(); i++)
    {
      final boolean isLastElement = i == timedDoseElements.size() - 1;
      final TimedComplexDoseElementDto doseElementDto = timedDoseElements.get(i);

      final double currentDuration;
      if (isLastElement)
      {
        currentDuration = 1440 - durationInMinutes; // 1440 = 60 * 24
      }
      else
      {
        currentDuration = doseElementDto.getDoseElement().getDuration();
        durationInMinutes += currentDuration;
      }

      final double currentQuantity = doseElementDto.getDoseElement().getRate() * currentDuration / 60;
      mlQuantitySum += currentQuantity;
    }

    return mlQuantitySum;
  }

  private Double getSimpleVariablePerDay(final List<TimedSimpleDoseElementDto> timedDoseElements)
  {
    if (timedDoseElements.isEmpty())
    {
      return null;
    }

    return timedDoseElements.stream()
        .filter(t -> t.getDoseElement().getQuantity() != null)
        .mapToDouble(t -> t.getDoseElement().getQuantity())
        .sum();
  }

  private Integer getTimesPerDay(final DosingFrequencyDto dosingFrequency, final Integer maxDailyFrequency)
  {
    if (maxDailyFrequency != null)
    {
      return maxDailyFrequency;
    }
    if (dosingFrequency == null)
    {
      return null;
    }
    if (dosingFrequency.getType() == DosingFrequencyTypeEnum.DAILY_COUNT)
    {
      return dosingFrequency.getValue();
    }
    else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.BETWEEN_DOSES)
    {
      return 24 / dosingFrequency.getValue();
    }
    else
    {
      return 1;
    }
  }

  private String getStrengthNumeratorUnit(final MedicationDataDto medicationDataDto)
  {
    final MedicationIngredientDto definingIngredient = getDefiningIngredient(medicationDataDto);

    return definingIngredient != null ? definingIngredient.getStrengthNumeratorUnit() : medicationDataDto.getBasicUnit();
  }

  private MedicationIngredientDto getDefiningIngredient(final MedicationDataDto medicationDataDto)
  {
    final MedicationIngredientDto definingIngredient;
    if (medicationDataDto.getMedicationIngredients().size() == 1)
    {
      definingIngredient = medicationDataDto.getMedicationIngredients().get(0);
    }
    else if (medicationDataDto.getDescriptiveIngredient() != null)
    {
      definingIngredient = medicationDataDto.getDescriptiveIngredient();
    }
    else
    {
      definingIngredient = null;
    }
    return definingIngredient;
  }

  private Double getContinuousInfusionRate(final ComplexTherapyDto complexTherapyDto)
  {
    if (complexTherapyDto instanceof VariableComplexTherapyDto)
    {
      return getInfusionRateForVariableComplexTherapy((VariableComplexTherapyDto)complexTherapyDto);
    }
    else
    {
      final ComplexDoseElementDto doseElement = ((ConstantComplexTherapyDto)complexTherapyDto).getDoseElement();
      return doseElement != null ? doseElement.getRate() : null;
    }
  }
}

