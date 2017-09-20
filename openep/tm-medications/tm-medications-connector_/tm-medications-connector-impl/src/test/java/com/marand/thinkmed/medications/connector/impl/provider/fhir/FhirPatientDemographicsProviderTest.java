package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import java.util.List;

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.IClientExecutable;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import com.google.common.collect.Lists;
import com.marand.maf.core.exception.SystemException;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsDto;
import com.marand.thinkmed.medications.connector.impl.provider.PatientDemographicsProvider;
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
import static org.mockito.ArgumentMatchers.any;

/**
 * @author Mitja Lapajne
 */

@RunWith(SpringProxiedJUnit4ClassRunner.class)
@ContextConfiguration
public class FhirPatientDemographicsProviderTest
{
  @SuppressWarnings("PublicField")
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Autowired
  private PatientDemographicsProvider patientDemographicsProvider;

  @Autowired
  private IGenericClient fhirClient;

  @Test
  public void testGetPatientsDemographicsAllValues()
  {
    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        "patient1",
        "Patient Name1",
        new DateTime(2016, 3, 3, 0, 0),
        AdministrativeGenderEnum.FEMALE);
    result.addEntry(entry1);

    final Bundle.Entry entry2 = getPatientResultBundleEntry(
        "patient2",
        "Patient Name2",
        new DateTime(2010, 10, 10, 0, 0),
        AdministrativeGenderEnum.MALE);
    result.addEntry(entry2);

    mockFhirClientResult(result);

    final List<PatientDemographicsDto> patientsDemographics =
        patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList("patient1", "patient2"));

    assertEquals(2, patientsDemographics.size());

    final PatientDemographicsDto patient1 = patientsDemographics.get(0);
    assertEquals("patient1", patient1.getId());
    assertEquals("Patient Name1", patient1.getName());
    assertEquals(new DateTime(2016, 3, 3, 0, 0), patient1.getBirthDate());
    assertEquals(Gender.FEMALE, patient1.getGender());

    final PatientDemographicsDto patient2 = patientsDemographics.get(1);
    assertEquals("patient2", patient2.getId());
    assertEquals("Patient Name2", patient2.getName());
    assertEquals(new DateTime(2010, 10, 10, 0, 0), patient2.getBirthDate());
    assertEquals(Gender.MALE, patient2.getGender());
  }

  @Test
  public void testGetPatientsDemographicsNoName()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage("FHIR validation failed - No Patient.name.text defined");

    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        "patient1",
        null,
        new DateTime(2016, 3, 3, 0, 0),
        AdministrativeGenderEnum.FEMALE);
    result.addEntry(entry1);

    mockFhirClientResult(result);

    patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList("patient1"));
  }

  @Test
  public void testGetPatientsDemographicsNoIdentifier()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage(
        "FHIR validation failed - No identifier for Patient with system \"patientIdSystem1\" defined");

    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        null,
        "Patient Name1",
        new DateTime(2016, 3, 3, 0, 0),
        AdministrativeGenderEnum.FEMALE);
    result.addEntry(entry1);

    mockFhirClientResult(result);

    patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList("patient1"));
  }

  @Test
  public void testGetPatientsDemographicsNoBirthDate()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage("No Patient.birthDate defined");

    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        "patient1",
        "Patient Name1",
        null,
        AdministrativeGenderEnum.FEMALE);
    result.addEntry(entry1);

    mockFhirClientResult(result);

    patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList("patient1"));
  }

  @Test
  public void testGetPatientsDemographicsNoGender()
  {
    expectedException.expect(SystemException.class);
    expectedException.expectMessage("No Patient.gender defined");

    final Bundle result = new Bundle();
    final Bundle.Entry entry1 = getPatientResultBundleEntry(
        "patientId",
        "Patient Name1",
        new DateTime(2016, 3, 3, 0, 0),
        null);
    result.addEntry(entry1);

    mockFhirClientResult(result);

    patientDemographicsProvider.getPatientsDemographics(Lists.newArrayList("patient1"));
  }

  private Bundle.Entry getPatientResultBundleEntry(
      final String patientId,
      final String patientName,
      final DateTime birthDate,
      final AdministrativeGenderEnum gender)
  {
    final Bundle.Entry entry = new Bundle.Entry();

    final Patient patient = new Patient();
    entry.setResource(patient);

    if (patientId != null)
    {
      patient.getIdentifier().add(new IdentifierDt("patientIdSystem1", patientId));
    }

    if (patientName != null)
    {
      final HumanNameDt humanName = new HumanNameDt();
      humanName.setText(patientName);
      patient.getName().add(humanName);
    }

    if (birthDate != null)
    {
      patient.setBirthDateWithDayPrecision(birthDate.toDate());
    }

    if (gender != null)
    {
      patient.setGender(gender);
    }

    return entry;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void mockFhirClientResult(final Bundle result)
  {
    final IUntypedQuery search = Mockito.mock(IUntypedQuery.class);
    final IQuery forResource = Mockito.mock(IQuery.class);
    final IQuery where = Mockito.mock(IQuery.class);
    final IClientExecutable bundle = Mockito.mock(IClientExecutable.class);

    Mockito.when(fhirClient.search()).thenReturn(search);
    Mockito.when(search.forResource(Patient.class)).thenReturn(forResource);
    Mockito.when(forResource.where(any())).thenReturn(where);
    Mockito.when(where.returnBundle(any())).thenReturn(bundle);
    Mockito.when(bundle.execute()).thenReturn(result);
  }
}
