package com.marand.thinkmed.medications.administration;

import java.util.Collection;
import javax.annotation.Nonnull;

import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface AdministrationUtils
{
  DateTime getAdministrationTime(@Nonnull AdministrationDto administrationDto);

  Double getInfusionRate(@Nonnull AdministrationDto administrationDto);

  boolean isRateAdministration(@Nonnull AdministrationDto administrationDto);

  Double getVolumeForRateQuantityOrRateVolumeSum(@Nonnull AdministrationDto administrationDto, @Nonnull String unit);

  InfusionBagDto getInfusionBagDto(@Nonnull AdministrationDto administrationDto);

  AdministrationResultEnum getAdministrationResult(@Nonnull MedicationActionAction action);

  TherapyDoseDto getTherapyDose(@Nonnull AdministrationDto administrationDto);

  TherapyDoseTypeEnum getTherapyDoseType(@Nonnull AdministrationDto administrationDto);

  void fillDurationForInfusionWithRate(@Nonnull Collection<AdministrationDto> administrations);

  String generateGroupUUId(@Nonnull DateTime date);

  int calculateDurationForRateQuantityDose(@Nonnull TherapyDoseDto dose);
}
