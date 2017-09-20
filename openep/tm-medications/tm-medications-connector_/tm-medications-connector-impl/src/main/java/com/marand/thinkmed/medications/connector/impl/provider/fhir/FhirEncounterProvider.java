package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;
import com.google.common.base.Preconditions;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.exception.SystemException;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.EncounterDto;
import com.marand.thinkmed.medications.connector.data.object.EncounterStatus;
import com.marand.thinkmed.medications.connector.data.object.EncounterType;
import com.marand.thinkmed.medications.connector.impl.provider.EncounterProvider;

import ca.uhn.fhir.model.base.composite.BaseIdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.BoundCodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.EncounterClassEnum;
import ca.uhn.fhir.model.dstu2.valueset.EncounterStateEnum;
import ca.uhn.fhir.model.dstu2.valueset.ParticipantTypeEnum;
import ca.uhn.fhir.rest.client.IGenericClient;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("OverlyBroadCatchBlock")
public class FhirEncounterProvider implements EncounterProvider
{
  private IGenericClient fhirClient;

  private String fhirPatientIdSystem;
  private String fhirEncounterIdSystem;
  private String fhirWardIdSystem;
  private String fhirWardLocationPhysicalType;
  private String fhirLocationLocationPhysicalType;
  private String fhirDoctorParticipantType;

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

  @Required
  public void setFhirEncounterIdSystem(final String fhirEncounterIdSystem)
  {
    this.fhirEncounterIdSystem = fhirEncounterIdSystem;
  }

  @Required
  public void setFhirWardIdSystem(final String fhirWardIdSystem)
  {
    this.fhirWardIdSystem = fhirWardIdSystem;
  }

  @Required
  public void setFhirWardLocationPhysicalType(final String fhirWardLocationPhysicalType)
  {
    this.fhirWardLocationPhysicalType = fhirWardLocationPhysicalType;
  }

  @Required
  public void setFhirLocationLocationPhysicalType(final String fhirLocationLocationPhysicalType)
  {
    this.fhirLocationLocationPhysicalType = fhirLocationLocationPhysicalType;
  }

  @Required
  public void setFhirDoctorParticipantType(final String fhirDoctorParticipantType)
  {
    this.fhirDoctorParticipantType = fhirDoctorParticipantType;
  }

  @Override
  public EncounterDto getPatientLatestEncounter(@Nonnull final String patientId)
  {
    Preconditions.checkArgument(StringUtils.isNotBlank(patientId), "patientId must not be blank");

    final Bundle result;
    try
    {
      result = fhirClient
          .search()
          .forResource(Encounter.class)
          .include(Encounter.INCLUDE_LOCATION)
          .include(Encounter.INCLUDE_PRACTITIONER)
          .where(Encounter.PATIENT.hasChainedProperty(
              Patient.IDENTIFIER.exactly().identifier(new IdentifierDt(fhirPatientIdSystem, patientId))))
          .and(Encounter.STATUS.exactly().identifiers(
              buildFhirEncounterStatusIdentifiers(
                  EnumSet.of(
                      EncounterStateEnum.ARRIVED,
                      EncounterStateEnum.IN_PROGRESS,
                      EncounterStateEnum.ON_LEAVE,
                      EncounterStateEnum.FINISHED))))
          .sort().descending(Encounter.DATE)
          .count(1)
          .returnBundle(Bundle.class)
          .execute();
    }
    catch (final Exception exception)
    {
      throw new SystemException(
          Dictionary.getEntry(
              "fhir.error.reading.encounter.data",
              DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()),
          exception);
    }

    return result.getEntry().stream()
        .filter(e -> e.getResource() instanceof Encounter)
        .map(e -> getEncounterDto((Encounter)e.getResource(), patientId))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<EncounterDto> getPatientsActiveEncounters(@Nonnull final Collection<String> patientsIds)
  {
    Preconditions.checkNotNull(patientsIds, "patientId must not be blank");

    if (patientsIds.isEmpty())
    {
      return Collections.emptyList();
    }

    final Bundle result;
    try
    {
      result = fhirClient
          .search()
          .forResource(Encounter.class)
          .include(Encounter.INCLUDE_PATIENT)
          .include(Encounter.INCLUDE_LOCATION)
          .include(Encounter.INCLUDE_PRACTITIONER)
          .where(Encounter.PATIENT.hasChainedProperty(
              Patient.IDENTIFIER.exactly().identifiers(buildFhirPatientsIdentifiers(patientsIds))))
          .and(Encounter.STATUS.exactly().identifiers(
              buildFhirEncounterStatusIdentifiers(
                  EnumSet.of(
                      EncounterStateEnum.ARRIVED,
                      EncounterStateEnum.IN_PROGRESS,
                      EncounterStateEnum.ON_LEAVE,
                      EncounterStateEnum.FINISHED))))
          .sort().ascending(Encounter.DATE)
          .returnBundle(Bundle.class)
          .execute();
    }
    catch (final Exception exception)
    {
      throw new SystemException(
          Dictionary.getEntry(
              "fhir.error.reading.encounter.data",
              DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()),
          exception);
    }

    return result.getEntry().stream()
        .filter(e -> e.getResource() instanceof Encounter)
        .map(e -> (Encounter)e.getResource())
        .map(e -> getEncounterDto(e, getPatientIdentifier(e)))
        .collect(Collectors.toList());
  }

  private List<BaseIdentifierDt> buildFhirEncounterStatusIdentifiers(final EnumSet<EncounterStateEnum> statuses)
  {
    return statuses.stream()
        .map(s -> new IdentifierDt("http://hl7.org/fhir/encounter-state", s.getCode()))
        .collect(Collectors.toList());
  }

  private List<BaseIdentifierDt> buildFhirPatientsIdentifiers(final Collection<String> patientIds)
  {
    return patientIds.stream()
        .map(i -> new IdentifierDt(fhirPatientIdSystem, i))
        .collect(Collectors.toList());
  }

  private EncounterDto getEncounterDto(final Encounter encounter, final String patientId)
  {
    final String encounterId = getIdentifier(encounter.getIdentifier(), fhirEncounterIdSystem)
        .orElseThrow(() -> new FhirValidationException("fhir.no.identifier", "Encounter", fhirEncounterIdSystem));

    final EncounterDto encounterDto = new EncounterDto(encounterId);
    encounterDto.setPatientId(patientId);
    encounterDto.setStart(getStart(encounter));
    encounterDto.setEnd(getEnd(encounter));
    encounterDto.setType(mapEncounterType(encounter.getClassElement()));
    encounterDto.setStatus(mapEncounterStatus(encounter.getStatus()));
    encounterDto.setWard(getWard(encounter));
    encounterDto.setLocation(getLocationName(encounter));
    encounterDto.setDoctor(getDoctor(encounter));
    return encounterDto;
  }

  private DateTime getEnd(final Encounter encounter)
  {
    return Opt.resolve(() -> encounter.getPeriod().getEnd()).map(DateTime::new).orElse(null);
  }

  private DateTime getStart(final Encounter encounter)
  {
    return Opt.resolve(() -> encounter.getPeriod().getStart())
        .map(DateTime::new)
        .orElseThrow(() -> new FhirValidationException("fhir.encounter.start.mandatory"));
  }

  private String getPatientIdentifier(final Encounter encounter)
  {
    final Patient patient = (Patient)encounter.getPatient().getResource();
    final Opt<String> identifier = getIdentifier(patient.getIdentifier(), fhirPatientIdSystem);

    return identifier
        .orElseThrow(() -> new FhirValidationException("fhir.no.identifier", "Patient", fhirPatientIdSystem));
  }

  private Opt<String> getIdentifier(final List<IdentifierDt> identifier, final String system)
  {
    return Opt.from(identifier.stream()
        .filter(i -> i.getSystem().equals(system))
        .map(IdentifierDt::getValue)
        .filter(Objects::nonNull)
        .findAny());
  }

  private EncounterType mapEncounterType(final String fhirEncounterClass)
  {
    final EncounterClassEnum encounterClass = EncounterClassEnum.forCode(fhirEncounterClass);
    if (encounterClass == EncounterClassEnum.INPATIENT)
    {
      return EncounterType.INPATIENT;
    }
    if (encounterClass == EncounterClassEnum.EMERGENCY)
    {
      return EncounterType.EMERGENCY;
    }
    if (EnumSet.of(EncounterClassEnum.AMBULATORY, EncounterClassEnum.OUTPATIENT, EncounterClassEnum.DAYTIME)
        .contains(encounterClass))
    {
      return EncounterType.OUTPATIENT;
    }
    throw new FhirValidationException("fhir.invalid.encounter.class", fhirEncounterClass);
  }

  private EncounterStatus mapEncounterStatus(final String fhirStatusCode)
  {
    final EncounterStateEnum fhirStatus = EncounterStateEnum.forCode(fhirStatusCode);
    if (EnumSet.of(EncounterStateEnum.ARRIVED, EncounterStateEnum.IN_PROGRESS).contains(fhirStatus))
    {
      return EncounterStatus.ACTIVE;
    }
    if (fhirStatus == EncounterStateEnum.ON_LEAVE)
    {
      return EncounterStatus.ON_LEAVE;
    }
    if (fhirStatus == EncounterStateEnum.FINISHED)
    {
      return EncounterStatus.FINISHED;
    }
    throw new FhirValidationException("fhir.invalid.encounter.status", fhirStatusCode);
  }

  private NamedExternalDto getWard(final Encounter encounter)
  {
    final Opt<Location> location = getEncounterLocation(encounter, fhirWardLocationPhysicalType);
    return location
        .map(l -> getIdentifier(l.getIdentifier(), fhirWardIdSystem).orElse(null))
        .map(wardId -> new NamedExternalDto(wardId, location.get().getName()))
        .orElse(null);
  }

  private String getLocationName(final Encounter encounter)
  {
    return getEncounterLocation(encounter, fhirLocationLocationPhysicalType)
        .map(Location::getName)
        .orElse(null);
  }

  private String getDoctor(final Encounter encounter)
  {
    for (final Encounter.Participant participant : encounter.getParticipant())
    {
      for (final BoundCodeableConceptDt<ParticipantTypeEnum> participantType : participant.getType())
      {
        for (final CodingDt participantTypeCoding : participantType.getCoding())
        {
          if (participantTypeCoding.getCode().equals(fhirDoctorParticipantType))
          {
            return ((Practitioner)participant.getIndividual().getResource()).getName().getText();
          }
        }
      }
    }
    return null;
  }

  private Opt<Location> getEncounterLocation(final Encounter encounter, final String locationPhysicalType)
  {
    return Opt.from(
        encounter.getLocation().stream()
            .map(l -> (Location)l.getLocation().getResource())
            .filter(l -> l.getPhysicalType().getText().equals(locationPhysicalType))
            .filter(Objects::nonNull)
            .findFirst());
  }
}
