package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.dto.TherapyDoseDto;

/**
 * @author Nejc Korasa
 */
public interface DoseAdministration
{
  TherapyDoseDto getAdministeredDose();

  void setAdministeredDose(TherapyDoseDto administeredDose);
}
