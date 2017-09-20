package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import ca.uhn.fhir.model.base.composite.BaseIdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.rest.client.IGenericClient;
import com.google.common.base.Preconditions;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.exception.SystemException;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsDto;
import com.marand.thinkmed.medications.connector.impl.provider.PatientDemographicsProvider;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("OverlyBroadCatchBlock")
public class FhirPatientDemographicsProvider implements PatientDemographicsProvider
{
  private IGenericClient fhirClient;
  private String fhirPatientIdSystem;

  @Required
  public void setFhirClient(final IGenericClient fhirClient)
  {
    this.fhirClient = fhirClient;
  }

  @Required
  public void setFhirPatientIdSystem(final String fhirPatientIdSystem)
  {
    this.fhirPatientIdSystem = fhirPatientIdSystem;
  }

  @Override
  public List<PatientDemographicsDto> getPatientsDemographics(@Nonnull final Collection<String> patientsIds)
  {
    Preconditions.checkNotNull(patientsIds, "patientsIds must not be null");

    if (patientsIds.isEmpty())
    {
      return Collections.emptyList();
    }
    final List<Patient> patients = queryPatients(patientsIds);
    return patients.stream()
        .map(this::getPatientDemographicsDto)
        .collect(Collectors.toList());
  }

  private List<Patient> queryPatients(final Collection<String> patientsIds)
  {
    final List<BaseIdentifierDt> fhirIdentifiers = patientsIds.stream()
        .map(i -> new IdentifierDt(fhirPatientIdSystem, i))
        .collect(Collectors.toList());

    final Bundle results;
    try
    {
      results = fhirClient
          .search()
          .forResource(Patient.class)
          .where(Patient.IDENTIFIER.exactly().identifiers(fhirIdentifiers))
          .returnBundle(Bundle.class)
          .execute();
    }
    catch (final Exception exception)
    {
      throw new SystemException(
          Dictionary.getEntry("fhir.error.reading.patient.data", DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()),
          exception);
    }

    return results.getEntry().stream()
        .map(e -> (Patient)e.getResource())
        .collect(Collectors.toList());
  }

  private PatientDemographicsDto getPatientDemographicsDto(final Patient patient)
  {
    final String patientName = patient.getName().stream()
        .filter(Objects::nonNull)
        .findFirst()
        .map(HumanNameDt::getText)
        .orElseThrow(() -> new FhirValidationException("fhir.no.patient.name"));

    final String identifierDt = patient.getIdentifier()
        .stream()
        .filter(i -> fhirPatientIdSystem.equals(i.getSystem()))
        .map(IdentifierDt::getValue)
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(() -> new FhirValidationException("fhir.no.identifier", "Patient", fhirPatientIdSystem));
    return new PatientDemographicsDto(
        identifierDt,
        patientName,
        getBirthDate(patient),
        getGender(patient));
  }

  private DateTime getBirthDate(final Patient patient)
  {
    return Opt.of(patient.getBirthDate())
        .map(DateTime::new)
        .orElseThrow(() -> new FhirValidationException("fhir.no.patient.birth.date"));
  }

  private Gender getGender(final Patient patient)
  {
    return Opt.of(patient.getGender())
        .map(g -> mapGender(AdministrativeGenderEnum.forCode(g)))
        .orElseThrow(() -> new FhirValidationException("fhir.no.patient.gender"));
  }

  private Gender mapGender(final AdministrativeGenderEnum gender)
  {
    if (gender == AdministrativeGenderEnum.MALE)
    {
      return Gender.MALE;
    }
    if (gender == AdministrativeGenderEnum.FEMALE)
    {
      return Gender.FEMALE;
    }
    if (gender == AdministrativeGenderEnum.OTHER)
    {
      return Gender.INDEFINABLE;
    }
    return Gender.NOT_KNOWN;
  }
}
