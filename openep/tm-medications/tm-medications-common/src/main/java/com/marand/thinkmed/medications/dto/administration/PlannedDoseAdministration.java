package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.dto.TherapyDoseDto;

/**
 * @author Nejc Korasa
 */
public interface PlannedDoseAdministration
{
  TherapyDoseDto getPlannedDose();

  void setPlannedDose(final TherapyDoseDto plannedDose);
}
