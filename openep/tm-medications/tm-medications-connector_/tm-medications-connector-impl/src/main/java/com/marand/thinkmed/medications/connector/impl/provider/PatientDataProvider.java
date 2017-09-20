package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public interface PatientDataProvider
{
  PatientDataForMedicationsDto getPatientData(String patientId, DateTime when);

  List<NamedExternalDto> getPatientAllergies(String patientId, DateTime when);

  Map<String, PatientDisplayDto> getPatientDisplayData(Set<String> patientIds);

  Set<NamedExternalDto> getPatientsIdsAndCareProviderNames(final Set<String> careProviderIds, final DateTime when);
}
