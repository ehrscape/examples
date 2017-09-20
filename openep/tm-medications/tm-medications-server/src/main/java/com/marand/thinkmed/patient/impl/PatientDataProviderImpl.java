package com.marand.thinkmed.patient.impl;

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.PatientImageUtils;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.patient.PatientDataProvider;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class PatientDataProviderImpl implements PatientDataProvider
{
  private MedicationsConnector medicationsConnector;

  @Required
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Override
  public PatientDataForMedicationsDto getPatientData(@Nonnull final String patientId, @Nonnull final DateTime when)
  {
    return medicationsConnector.getPatientData(
        Preconditions.checkNotNull(patientId, "patientId must not be null!"),
        Preconditions.checkNotNull(when, "when must not be null!"));
  }

  @Override
  public Map<String, PatientDisplayWithLocationDto> getPatientDisplayWithLocationMap(
      final Collection<String> careProviderIds,
      final Collection<String> patientIds)
  {
    final Map<String, PatientDisplayWithLocationDto> patientDisplayWithLocations = medicationsConnector.getPatientDisplayWithLocationMap(
        careProviderIds,
        patientIds);

    patientDisplayWithLocations.values()
        .stream()
        .filter(p -> p.getPatientDisplayDto() != null)
        .map(PatientDisplayWithLocationDto::getPatientDisplayDto)
        .forEach(p -> p.setPatientImagePath(PatientImageUtils.getPatientImagePath(p.getGender(), p.getBirthDate())));

    return patientDisplayWithLocations;
  }
}
