package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.connector.data.object.EncounterDto;

/**
 * @author Mitja Lapajne
 */
public interface EncounterProvider
{
  EncounterDto getPatientLatestEncounter(@Nonnull String patientId);

  List<EncounterDto> getPatientsActiveEncounters(@Nonnull Collection<String> patientsIds);
}
