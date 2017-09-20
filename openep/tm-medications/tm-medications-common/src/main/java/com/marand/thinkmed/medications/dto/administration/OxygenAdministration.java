package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.dto.OxygenStartingDevice;

/**
 * @author Nejc Korasa
 */
public interface OxygenAdministration extends DoseAdministration, PlannedDoseAdministration
{
  OxygenStartingDevice getPlannedStartingDevice();

  void setPlannedStartingDevice(OxygenStartingDevice plannedStartingDevice);

  OxygenStartingDevice getStartingDevice();

  void setStartingDevice(OxygenStartingDevice startingDevice);
}
