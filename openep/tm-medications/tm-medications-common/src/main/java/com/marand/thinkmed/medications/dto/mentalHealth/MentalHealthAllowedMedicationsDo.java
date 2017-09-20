package com.marand.thinkmed.medications.dto.mentalHealth;

import java.util.Set;

import com.google.common.collect.SetMultimap;

/**
 * @author Nejc Korasa
 */
public class MentalHealthAllowedMedicationsDo
{
  private final SetMultimap<Long, Long> medicationIdsWithRouteIds;
  private final Set<Long> allRoutesMedicationIds;

  public MentalHealthAllowedMedicationsDo(
      final SetMultimap<Long, Long> medicationIdsWithRouteIds,
      final Set<Long> allRoutesMedicationIds)
  {
    this.medicationIdsWithRouteIds = medicationIdsWithRouteIds;
    this.allRoutesMedicationIds = allRoutesMedicationIds;
  }

  public SetMultimap<Long, Long> getMedicationIdsWithRouteIds()
  {
    return medicationIdsWithRouteIds;
  }

  public Set<Long> getAllRoutesMedicationIds()
  {
    return allRoutesMedicationIds;
  }
}
