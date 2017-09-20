package com.marand.thinkmed.medications.administration.impl;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Opt;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.business.util.TherapyUnitsConverter;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionBagAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

/**
 * @author Nejc Korasa
 */
public class AdministrationUtilsImpl implements AdministrationUtils
{
  @Override
  public DateTime getAdministrationTime(@Nonnull final AdministrationDto administrationDto)
  {
    Preconditions.checkNotNull(administrationDto, "administrationDto must not be null");

    return administrationDto.getAdministrationTime() != null
           ? administrationDto.getAdministrationTime()
           : administrationDto.getPlannedTime();
  }

  @Override
  public Double getInfusionRate(@Nonnull final AdministrationDto administrationDto)
  {
    Preconditions.checkNotNull(administrationDto, "administrationDto must not be null");

    final TherapyDoseDto therapyDoseDto = getTherapyDose(administrationDto);
    return therapyDoseDto.getNumerator();
  }

  @Override
  public boolean isRateAdministration(@Nonnull final AdministrationDto administrationDto)
  {
    Preconditions.checkNotNull(administrationDto, "administrationDto must not be null");
    return getTherapyDose(administrationDto).getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.RATE;
  }

  @Override
  public Double getVolumeForRateQuantityOrRateVolumeSum(
      @Nonnull final AdministrationDto administrationDto,
      @Nonnull final String unit)
  {
    Preconditions.checkNotNull(administrationDto, "administrationDto must not be null");
    Preconditions.checkNotNull(unit, "unit must not be null");

    final TherapyDoseDto therapyDoseDto = getTherapyDose(administrationDto);

    if (therapyDoseDto.getSecondaryNumeratorUnit() != null
        && TherapyUnitsConverter.isLiquidUnit(therapyDoseDto.getSecondaryNumeratorUnit()))
    {
      return TherapyUnitsConverter.convertToUnit(
          therapyDoseDto.getSecondaryNumerator(),
          therapyDoseDto.getSecondaryNumeratorUnit(),
          unit);
    }
    if (therapyDoseDto.getSecondaryDenominatorUnit() != null
        && TherapyUnitsConverter.isLiquidUnit(therapyDoseDto.getSecondaryDenominatorUnit()))
    {
      return TherapyUnitsConverter.convertToUnit(
          therapyDoseDto.getSecondaryDenominator(),
          therapyDoseDto.getSecondaryDenominatorUnit(),
          unit);
    }

    return null;
  }

  @Override
  public InfusionBagDto getInfusionBagDto(@Nonnull final AdministrationDto administrationDto)
  {
    Preconditions.checkNotNull(administrationDto, "administrationDto must not be null");

    return administrationDto instanceof InfusionBagAdministration
           ? ((InfusionBagAdministration)administrationDto).getInfusionBag()
           : null;
  }

  @Override
  public AdministrationResultEnum getAdministrationResult(@Nonnull final MedicationActionAction action)
  {
    Preconditions.checkNotNull(action, "action must not be null");

    final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);

    if (actionEnum == MedicationActionEnum.DEFER)
    {
      return AdministrationResultEnum.DEFER;
    }
    if (actionEnum == MedicationActionEnum.WITHHOLD)
    {
      return AdministrationResultEnum.NOT_GIVEN;
    }

    return action.getSelfAdministrationType() != null
           ? AdministrationResultEnum.SELF_ADMINISTERED
           : AdministrationResultEnum.GIVEN;
  }

  @Override
  public TherapyDoseDto getTherapyDose(@Nonnull final AdministrationDto administration)
  {
    Preconditions.checkNotNull(administration, "administration must not be null");

    if (administration instanceof StartAdministrationDto)
    {
      final StartAdministrationDto startAdministrationDto = (StartAdministrationDto)administration;
      return startAdministrationDto.getAdministeredDose() == null
             ? startAdministrationDto.getPlannedDose()
             : startAdministrationDto.getAdministeredDose();
    }
    else if (administration instanceof AdjustInfusionAdministrationDto)
    {
      final AdjustInfusionAdministrationDto adjustInfusionAdministrationDto = (AdjustInfusionAdministrationDto)administration;
      return adjustInfusionAdministrationDto.getAdministeredDose() == null
             ? adjustInfusionAdministrationDto.getPlannedDose()
             : adjustInfusionAdministrationDto.getAdministeredDose();
    }
    else if (administration instanceof BolusAdministrationDto)
    {
      final BolusAdministrationDto bolusAdministration = (BolusAdministrationDto)administration;
      return bolusAdministration.getAdministeredDose();
    }
    else
    {
      throw new IllegalArgumentException("This administration type is not supported for this method");
    }
  }

  @Override
  public TherapyDoseTypeEnum getTherapyDoseType(@Nonnull final AdministrationDto administrationDto)
  {
    Preconditions.checkNotNull(administrationDto, "administration must not be null");

    return Opt.resolve(() -> getTherapyDose(administrationDto).getTherapyDoseTypeEnum()).orElse(null);
  }

  @Override
  public void fillDurationForInfusionWithRate(@Nonnull final Collection<AdministrationDto> administrations)
  {
    Preconditions.checkNotNull(administrations, "administrations must not be null");

    StartAdministrationDto previousStartAdministration = null;
    DateTime previousAdministrationTime = null;
    Double duration = null;
    Double quantitySum = null;
    Double previousRate = null;

    for (final AdministrationDto administrationDto : administrations)
    {
      if (administrationDto.getAdministrationType() == AdministrationTypeEnum.START)
      {
        final StartAdministrationDto currentStartAdministration = (StartAdministrationDto)administrationDto;
        setStartAdministrationDuration(previousStartAdministration, duration, quantitySum, previousRate);

        if (currentStartAdministration.getAdministrationResult() == AdministrationResultEnum.NOT_GIVEN)
        {
          previousStartAdministration = null;
          previousAdministrationTime = null;
        }
        else
        {
          previousStartAdministration = currentStartAdministration;
          previousRate = getInfusionRate(currentStartAdministration);
          previousAdministrationTime = getAdministrationTime(currentStartAdministration);
        }
        duration = 0.0;
        quantitySum = 0.0;
      }
      else if (administrationDto.getAdministrationType() == AdministrationTypeEnum.ADJUST_INFUSION
          && administrationDto.getAdministrationResult() != AdministrationResultEnum.NOT_GIVEN)
      {
        if (previousAdministrationTime != null)
        {
          final DateTime currentAdministrationTime = getAdministrationTime(administrationDto);
          final double minutes = (double)Minutes
              .minutesBetween(previousAdministrationTime, currentAdministrationTime)
              .getMinutes();

          duration += minutes;
          quantitySum += minutes / 60 * previousRate;

          previousRate = getInfusionRate(administrationDto);
          previousAdministrationTime = currentAdministrationTime;
        }
      }
    }

    setStartAdministrationDuration(previousStartAdministration, duration, quantitySum, previousRate);
  }

  @Override
  public String generateGroupUUId(@Nonnull final DateTime date)
  {
    Preconditions.checkNotNull(date, "date must not be null!");
    final String uuid = UUID.randomUUID().toString();
    return new StringBuilder().append(date.getMillis()).append("_").append(uuid).toString();
  }

  @Override
  public int calculateDurationForRateQuantityDose(@Nonnull final TherapyDoseDto dose)
  {
    Preconditions.checkNotNull(dose, "dose");

    final double rate = dose.getNumerator();
    final double quantity = Opt.resolve(dose::getSecondaryDenominator).orElse(dose.getSecondaryNumerator());
    final String unit = Opt.resolve(dose::getSecondaryDenominatorUnit).orElse(dose.getSecondaryNumeratorUnit());

    final Double mlQuantity = TherapyUnitsConverter.convertToUnit(quantity, unit, "ml");
    Preconditions.checkNotNull(mlQuantity, "mlQuantity");

    //noinspection NumericCastThatLosesPrecision
    return (int)(mlQuantity / rate * 60);
  }

  private void setStartAdministrationDuration(
      final StartAdministrationDto administration,
      final Double duration,
      final Double quantitySum,
      final Double previousRate)
  {
    if (administration != null)
    {
      final Double doseQuantity = getVolumeForRateQuantityOrRateVolumeSum(administration, "ml");

      if (doseQuantity != null && doseQuantity > quantitySum)
      {
        final double remainingQuantity = doseQuantity - quantitySum;
        final double calculatedDuration = duration + remainingQuantity / previousRate * 60;
        administration.setDuration(calculatedDuration);
      }
    }
  }
}
