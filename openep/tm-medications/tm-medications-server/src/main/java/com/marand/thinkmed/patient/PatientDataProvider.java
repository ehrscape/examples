package com.marand.thinkmed.patient;

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface PatientDataProvider
{
  PatientDataForMedicationsDto getPatientData(@Nonnull String patientId, @Nonnull DateTime when);

  Map<String, PatientDisplayWithLocationDto> getPatientDisplayWithLocationMap(
      Collection<String> careProviderIds,
      Collection<String> patientIds);
}
