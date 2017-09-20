package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import java.util.List;

import ca.uhn.fhir.model.dstu2.composite.BoundCodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.EncounterClassEnum;
import ca.uhn.fhir.model.dstu2.valueset.EncounterStateEnum;
import ca.uhn.fhir.model.dstu2.valueset.ParticipantTypeEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.IClientExecutable;
import ca.uhn.fhir.rest.gclient.IParam;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ISort;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import com.google.common.collect.Lists;
import com.marand.maf.core.exception.SystemException;
import com.marand.thinkmed.medications.connector.data.object.EncounterDto;
import com.marand.thinkmed.medications.connector.data.object.EncounterStatus;
import com.marand.thinkmed.medications.connector.data.object.EncounterType;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

/**
 * @author Mitja Lapajne
 */

@RunWith(SpringProxiedJUnit4ClassRunner.class)
@ContextConfiguration
public class FhirEncounterProviderTest
{
  @SuppressWarnings("PublicField")
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Autowired
  private FhirEncounterProvider fhirEncounterProvider;

  @Autowired
  private IGenericClient fhirClient;

  @Test
  public void testGetPatientLatestEncounterAllValues()
  {
    final Bundle result = new Bundle();
    final Bundle.Entry entry = getEncounterResultBundleEntry(
        "encounter1",
        "patient1",
        EncounterClassEnum.INPATIENT,
        EncounterStateEnum.FINISHED,
        new DateTime(2016, 10, 1, 12, 0),
        new DateTime(2016, 10, 2, 13, 0),
        "ward1",
        "Test Ward",
        "Test Location",
        "Test Doctor");
    result.addEntry(entry);

    mockFhirClientResult(result);

    final EncounterDto encounterDto = fhirEncounterProvider.getPatientLatestEncounter("patient1");
    assertEquals("encounter1", encounterDto.getId());
    assertEquals(EncounterType.INPATIENT, encounterDto.getType());
    assertEquals(EncounterStatus.FINISHED, encounterDto.getStatus());
    assertEquals("Test Doctor", encounterDto.getDoctor());
    assertEquals("Test Location", encounterDto.getLocation());
    assertEquals("ward1", encounterDto.getWard().getId());
    assertEquals("Test Ward", encounterDto.getWard().getName());
    assertEquals(new DateTime(2016, 10, 1, 12, 0), encounterDto.getStart());
    assertEquals(new DateTime(2016, 10, 2, 13, 0), encounterDto.getEnd());
  }

  @Test
  public void testGetPatientLatestEncounterOnlyMandatoryValues()
  {
    final Bundle result = new Bundle();
    final Bundle.Entry entry = getEncounterResultBundleEntry(
        "encounter1",
        "patient1",
        EncounterClassEnum.INPATIENT,
        EncounterStateEnum.IN_PROGRESS,
        new DateTime(2016, 10, 1, 12, 0),
        null,
        null,
        null,
        null,
        null);
    result.addEntry(entry);

    mockFhirClientResult(result);

    final EncounterDto encounterDto = fhirEncounterProvider.getPatientLatestEncounter("patient1");
    assertEquals("encounter1", encounterDto.getId());
    assertEquals(EncounterType.INPATIENT, encounterDto.getType());
    assertEquals(EncounterStatus.ACTIVE, encounterDto.getStatus());
    assertNull(encounterDto.getDoctor());
    assertNull(encounterDto.getLocation());
    assertNull(encounterDto.getWard());
    assertEquals(new DateTime(2016, 10, 1, 12, 0), encounterDto.getStart());
    assertNull(encounterDto.getEnd());
  }

  @Test
  public void testGetPatientLatestEncounterNoEncounterId()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage(
        "FHIR validation failed - No identifier for Encounter with system \"encounterIdSystem1\" defined");

    final Bundle result = new Bundle();
    final Bundle.Entry entry = getEncounterResultBundleEntry(
        null,
        "patient1",
        EncounterClassEnum.INPATIENT,
        EncounterStateEnum.IN_PROGRESS,
        new DateTime(2016, 10, 1, 12, 0),
        null,
        null,
        null,
        null,
        null);
    result.addEntry(entry);

    mockFhirClientResult(result);

    fhirEncounterProvider.getPatientLatestEncounter("patient1");
  }

  @Test
  public void testGetPatientLatestEncounterNoStart()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage(
        "FHIR validation failed - Encounter.Period.Start is mandatory");

    final Bundle result = new Bundle();
    final Bundle.Entry entry = getEncounterResultBundleEntry(
        "encounter1",
        "patient1",
        EncounterClassEnum.INPATIENT,
        EncounterStateEnum.IN_PROGRESS,
        null,
        null,
        null,
        null,
        null,
        null);
    result.addEntry(entry);

    mockFhirClientResult(result);

    fhirEncounterProvider.getPatientLatestEncounter("patient1");
  }

  @Test
  public void testGetPatientLatestEncounterInvalidEncounterType()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage(
        "FHIR validation failed - Invalid Encounter.class: home");

    final Bundle result = new Bundle();
    final Bundle.Entry entry = getEncounterResultBundleEntry(
        "encounter1",
        "patient1",
        EncounterClassEnum.HOME,
        EncounterStateEnum.IN_PROGRESS,
        new DateTime(2016, 10, 1, 12, 0),
        null,
        null,
        null,
        null,
        null);
    result.addEntry(entry);

    mockFhirClientResult(result);

    fhirEncounterProvider.getPatientLatestEncounter("patient1");
  }


  @Test
  public void testGetPatientLatestEncounterInvalidEncounterStatus()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage(
        "FHIR validation failed - Invalid Encounter.status: cancelled");

    final Bundle result = new Bundle();
    final Bundle.Entry entry = getEncounterResultBundleEntry(
        "encounter1",
        "patient1",
        EncounterClassEnum.INPATIENT,
        EncounterStateEnum.CANCELLED,
        new DateTime(2016, 10, 1, 12, 0),
        null,
        null,
        null,
        null,
        null);
    result.addEntry(entry);

    mockFhirClientResult(result);

    fhirEncounterProvider.getPatientLatestEncounter("patient1");
  }

  @Test
  public void testGetPatientsActiveEncounters()
  {
    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getEncounterResultBundleEntry(
        "encounter1",
        "patient1",
        EncounterClassEnum.OUTPATIENT,
        EncounterStateEnum.FINISHED,
        new DateTime(2016, 10, 1, 12, 0),
        new DateTime(2016, 10, 2, 13, 0),
        "ward1",
        "Test Ward",
        "Test Location",
        "Test Doctor");
    result.addEntry(entry1);

    final Bundle.Entry entry2 = getEncounterResultBundleEntry(
        "encounter2",
        "patient2",
        EncounterClassEnum.INPATIENT,
        EncounterStateEnum.ON_LEAVE,
        new DateTime(2016, 10, 1, 12, 0),
        null,
        null,
        null,
        null,
        null);
    result.addEntry(entry2);

    final Bundle.Entry entry3 = getEncounterResultBundleEntry(
        "encounter3",
        "patient2",
        EncounterClassEnum.INPATIENT,
        EncounterStateEnum.FINISHED,
        new DateTime(2016, 9, 9, 9, 0),
        null,
        null,
        null,
        null,
        null);
    result.addEntry(entry3);

    mockFhirClientResult(result);

    final List<EncounterDto> encounterDtos =
        fhirEncounterProvider.getPatientsActiveEncounters(Lists.newArrayList("patient1", "patient2"));

    final EncounterDto encounterDto1 = encounterDtos.get(0);
    assertEquals("encounter1", encounterDto1.getId());
    assertEquals(EncounterType.OUTPATIENT, encounterDto1.getType());
    assertEquals(EncounterStatus.FINISHED, encounterDto1.getStatus());
    assertEquals("Test Doctor", encounterDto1.getDoctor());
    assertEquals("Test Location", encounterDto1.getLocation());
    assertEquals("ward1", encounterDto1.getWard().getId());
    assertEquals("Test Ward", encounterDto1.getWard().getName());
    assertEquals(new DateTime(2016, 10, 1, 12, 0), encounterDto1.getStart());
    assertEquals(new DateTime(2016, 10, 2, 13, 0), encounterDto1.getEnd());

    final EncounterDto encounterDto2 = encounterDtos.get(1);
    assertEquals("encounter2", encounterDto2.getId());
    assertEquals(EncounterType.INPATIENT, encounterDto2.getType());
    assertEquals(EncounterStatus.ON_LEAVE, encounterDto2.getStatus());
    assertNull(encounterDto2.getDoctor());
    assertNull(encounterDto2.getLocation());
    assertNull(encounterDto2.getWard());
    assertEquals(new DateTime(2016, 10, 1, 12, 0), encounterDto2.getStart());
    assertNull(encounterDto2.getEnd());
  }

  @Test
  public void testGetPatientsActiveEncountersNoPatientId()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage(
        "FHIR validation failed - No identifier for Patient with system \"patientIdSystem1\" defined");

    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getEncounterResultBundleEntry(
        "encounter1",
        null,
        EncounterClassEnum.OUTPATIENT,
        EncounterStateEnum.FINISHED,
        new DateTime(2016, 10, 1, 12, 0),
        new DateTime(2016, 10, 2, 13, 0),
        "ward1",
        "Test Ward",
        "Test Location",
        "Test Doctor");
    result.addEntry(entry1);

    mockFhirClientResult(result);

    fhirEncounterProvider.getPatientsActiveEncounters(Lists.newArrayList("patient1"));
  }

  private Bundle.Entry getEncounterResultBundleEntry(
      final String encounterId,
      final String patientId,
      final EncounterClassEnum encounterClass,
      final EncounterStateEnum encounterState,
      final DateTime start,
      final DateTime end,
      final String wardId,
      final String wardName,
      final String locationName,
      final String doctorName)
  {
    final Bundle.Entry entry = new Bundle.Entry();

    final Encounter encounter = new Encounter();
    entry.setResource(encounter);

    if (encounterId != null)
    {
      encounter.getIdentifier().add(new IdentifierDt("encounterIdSystem1", encounterId));
    }

    final ResourceReferenceDt patientResourceRef = new ResourceReferenceDt();
    encounter.setPatient(patientResourceRef);

    final Patient patient = new Patient();
    patientResourceRef.setResource(patient);

    if (patientId != null)
    {
      patient.getIdentifier().add(new IdentifierDt("patientIdSystem1", patientId));
    }

    encounter.setClassElement(encounterClass);
    encounter.setStatus(encounterState);

    if (wardId != null && wardName != null)
    {
      encounter.getLocation().add(getLocation("wardIdSystem1", wardId, wardName, "ward"));
    }
    if (locationName != null)
    {
      encounter.getLocation().add(getLocation("locationIdSystem1", null, locationName, "location"));
    }

    if (doctorName != null)
    {
      encounter.getParticipant().add(getDoctor(doctorName));
    }

    final PeriodDt period = new PeriodDt();
    encounter.setPeriod(period);

    if (start != null)
    {
      period.setStart(new DateTimeDt(start.toDate()));
    }

    if (end != null)
    {
      period.setEnd(new DateTimeDt(end.toDate()));
    }
    return entry;
  }

  @SuppressWarnings({"deprecation", "unchecked"})
  private Encounter.Participant getDoctor(final String doctorName)
  {
    final Encounter.Participant participant = new Encounter.Participant();
    final BoundCodeableConceptDt<ParticipantTypeEnum> participantType = new BoundCodeableConceptDt();
    final CodingDt participantTypeCode = new CodingDt();
    participantTypeCode.setCode("primaryDoctor");
    participantType.getCoding().add(participantTypeCode);
    participant.getType().add(participantType);
    final ResourceReferenceDt individualResourceRef = new ResourceReferenceDt();
    final Practitioner practitioner = new Practitioner();
    final HumanNameDt practitionerName = new HumanNameDt();
    practitionerName.setText(doctorName);
    practitioner.setName(practitionerName);
    individualResourceRef.setResource(practitioner);
    participant.setIndividual(individualResourceRef);
    return participant;
  }

  private Encounter.Location getLocation(
      final String locationIdSystem,
      final String locationId,
      final String locationName,
      final String locationPhysicalType)
  {
    final Encounter.Location location = new Encounter.Location();
    final ResourceReferenceDt locationResourceRef = new ResourceReferenceDt();
    final Location locationResource = new Location();
    locationResource.getIdentifier().add(new IdentifierDt(locationIdSystem, locationId));
    locationResource.setName(locationName);
    final CodeableConceptDt physicalLocation = new CodeableConceptDt();
    physicalLocation.setText(locationPhysicalType);
    locationResource.setPhysicalType(physicalLocation);
    locationResourceRef.setResource(locationResource);
    location.setLocation(locationResourceRef);
    return location;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void mockFhirClientResult(final Bundle result)
  {
    final IUntypedQuery search = Mockito.mock(IUntypedQuery.class);
    final IQuery forResource = Mockito.mock(IQuery.class);
    final IQuery include = Mockito.mock(IQuery.class);
    final IQuery where = Mockito.mock(IQuery.class);
    final IQuery and = Mockito.mock(IQuery.class);
    final ISort sort = Mockito.mock(ISort.class);
    final IQuery sortDesc = Mockito.mock(IQuery.class);
    final IQuery sortAsc = Mockito.mock(IQuery.class);
    final IQuery count = Mockito.mock(IQuery.class);
    final IClientExecutable bundle = Mockito.mock(IClientExecutable.class);

    Mockito.when(fhirClient.search()).thenReturn(search);
    Mockito.when(search.forResource(Encounter.class)).thenReturn(forResource);
    Mockito.when(forResource.include(any())).thenReturn(include);
    Mockito.when(include.include(any())).thenReturn(include);
    Mockito.when(include.where(any())).thenReturn(where);
    Mockito.when(where.and(any())).thenReturn(and);
    Mockito.when(and.sort()).thenReturn(sort);
    Mockito.when(sort.descending(any(IParam.class))).thenReturn(sortDesc);
    Mockito.when(sort.ascending(any(IParam.class))).thenReturn(sortAsc);
    Mockito.when(sortDesc.count(anyInt())).thenReturn(count);
    Mockito.when(sortAsc.returnBundle(any())).thenReturn(bundle);
    Mockito.when(count.returnBundle(any())).thenReturn(bundle);
    Mockito.when(bundle.execute()).thenReturn(result);
  }
}
