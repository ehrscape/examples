package com.marand.thinkmed.medications.connector.impl.local.provider.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.impl.provider.PatientDataProvider;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Bostjan Vester
 */
@RunWith(SpringProxiedJUnit4ClassRunner.class)
@ContextConfiguration({"/com/marand/thinkmed/medications/connector/impl/local/provider/impl/LocalPatientDataProviderTest-context.xml"})
@DbUnitConfiguration(databaseConnection = {"dbUnitDatabaseConnection"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@DatabaseSetup("LocalPatientDataProviderTest.xml")
@Transactional
public class LocalPatientDataProviderTest
{
  @Autowired
  private PatientDataProvider patientDataProvider;

  @Test
  public void testGetValidInpatientPatientData()
  {
    final PatientDataForMedicationsDto patientData = patientDataProvider.getPatientData(
        "1",
        new DateTime(2015, 1, 10, 0, 0));
    Assert.assertNotNull(patientData);
    Assert.assertEquals(Gender.MALE, patientData.getGender());
    final List<NamedExternalDto> allergies = patientData.getAllergies();
    Assert.assertNotNull(allergies);
    Assert.assertEquals(2, allergies.size());
  }

  @Test
  public void testGetValidOutpatientPatientData()
  {
    final PatientDataForMedicationsDto patientData = patientDataProvider.getPatientData(
        "1",
        new DateTime(2015, 1, 10, 0, 0));
    Assert.assertNotNull(patientData);
    Assert.assertEquals(Gender.MALE, patientData.getGender());
    final List<NamedExternalDto> allergies = patientData.getAllergies();
    Assert.assertNotNull(allergies);
    Assert.assertEquals(2, allergies.size());
  }

  @Test
  public void getNonExistingPatientData()
  {
    final PatientDataForMedicationsDto patientData = patientDataProvider.getPatientData(
        "999",
        new DateTime(2015, 1, 10, 0, 0));
    Assert.assertNull(patientData);
  }

  @Test
  public void getPatientValidAllergies()
  {
    final List<NamedExternalDto> patientAllergies = patientDataProvider.getPatientAllergies(
        "1",
        new DateTime(2015, 1, 10, 0, 0));
    Assert.assertNotNull(patientAllergies);
    Assert.assertEquals(2, patientAllergies.size());
  }

  @Test
  public void getPatientNonValidAllergies()
  {
    final List<NamedExternalDto> patientAllergies = patientDataProvider.getPatientAllergies(
        "1",
        new DateTime(2010, 1, 10, 0, 0));
    Assert.assertNotNull(patientAllergies);
    Assert.assertTrue(patientAllergies.isEmpty());
  }

  @Test
  public void getAllExistingPatientsDisplayData()
  {
    final Map<String, PatientDisplayDto> result = patientDataProvider.getPatientDisplayData(Sets.newSet("1", "2"));
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.containsKey("1"));
    Assert.assertTrue(result.containsKey("2"));

    final PatientDisplayDto patient1 = result.get("1");
    Assert.assertEquals("1", patient1.getId());

    final PatientDisplayDto patient2 = result.get("2");
    Assert.assertEquals("2", patient2.getId());
  }

  @Test
  public void getSomeExistingPatientsDisplayData()
  {
    final Map<String, PatientDisplayDto> result = patientDataProvider.getPatientDisplayData(Sets.newSet("1", "999"));
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.containsKey("1"));
    Assert.assertFalse(result.containsKey("999"));

    final PatientDisplayDto patient1 = result.get("1");
    Assert.assertEquals("1", patient1.getId());
  }

  @Test
  public void getNonExistingPatientsDisplayData()
  {
    final Map<String, PatientDisplayDto> result = patientDataProvider.getPatientDisplayData(Sets.newSet("999", "998"));
    Assert.assertNotNull(result);
    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void getMultipleExistingPatientsDisplayData()
  {
    final Map<String, PatientDisplayDto> result = patientDataProvider.getPatientDisplayData(Sets.newSet("1", "1", "2"));
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.containsKey("1"));
    Assert.assertTrue(result.containsKey("2"));

    final PatientDisplayDto patient1 = result.get("1");
    Assert.assertEquals("1", patient1.getId());

    final PatientDisplayDto patient2 = result.get("2");
    Assert.assertEquals("2", patient2.getId());
  }

  @Test
  public void getPatientsIdsAndCareProviderNamesForMultipleEpisodes()
  {
    final Set<NamedExternalDto> patientsIdsAndCareProviderNames = patientDataProvider.getPatientsIdsAndCareProviderNames(
        Sets.newSet("1", "2"),
        new DateTime(2015, 1, 10, 0, 0));
    Assert.assertNotNull(patientsIdsAndCareProviderNames);
    Assert.assertEquals(1, patientsIdsAndCareProviderNames.size());
    final NamedExternalDto patient1 = patientsIdsAndCareProviderNames.iterator().next();
    Assert.assertEquals("1", patient1.getId());
    Assert.assertEquals("Organization1 - CareProvider1", patient1.getName());
  }

  @Test
  public void getPatientsIdsAndCareProviderNamesForSingleEpisode()
  {
    final Set<NamedExternalDto> patientsIdsAndCareProviderNames = patientDataProvider.getPatientsIdsAndCareProviderNames(
        Sets.newSet("2", "3"),
        new DateTime(2015, 3, 1, 13, 0));
    Assert.assertNotNull(patientsIdsAndCareProviderNames);
    Assert.assertEquals(1, patientsIdsAndCareProviderNames.size());
    final NamedExternalDto patient1 = patientsIdsAndCareProviderNames.iterator().next();
    Assert.assertEquals("1", patient1.getId());
    Assert.assertEquals("Organization2 - CareProvider3", patient1.getName());
  }

  @Test
  public void getPatientsIdsAndCareProviderNamesForNonExistingCareProvider()
  {
    final Set<NamedExternalDto> patientsIdsAndCareProviderNames = patientDataProvider.getPatientsIdsAndCareProviderNames(
        Sets.newSet("3"),
        new DateTime(2015, 1, 10, 0, 0));
    Assert.assertNotNull(patientsIdsAndCareProviderNames);
    Assert.assertTrue(patientsIdsAndCareProviderNames.isEmpty());
  }
}
