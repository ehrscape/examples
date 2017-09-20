package com.marand.thinkmed.medications.connector.impl.local.provider.impl;

import java.util.List;
import java.util.Locale;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.google.common.collect.Lists;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.impl.provider.AdtDataProvider;
import com.marand.thinkmed.medications.connector.impl.provider.PatientDataProvider;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
@ContextConfiguration({"/com/marand/thinkmed/medications/connector/impl/local/provider/impl/LocalAdtDataProviderTest-context.xml"})
@DbUnitConfiguration(databaseConnection = {"dbUnitDatabaseConnection"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@DatabaseSetup("LocalAdtDataProviderTest.xml")
@Transactional
public class LocalAdtDataProviderTest
{
  @Autowired
  private AdtDataProvider adtDataProvider;

  @Autowired
  private PatientDataProvider patientDataProvider;

  @Test
  public void testGetLastDischargedCentralCaseEffectiveInterval()
  {
    final Interval lastDischargedCentralCaseInterval = adtDataProvider.getLastDischargedCentralCaseEffectiveInterval("1");
    Assert.assertNotNull(lastDischargedCentralCaseInterval);
    Assert.assertEquals(new DateTime(2015, 2, 1, 12, 0), lastDischargedCentralCaseInterval.getEnd());
  }

  @Test
  public void testGetLastDischargedCentralCaseEffectiveIntervalNoDischarge()
  {
    final Interval lastDischargedCentralCaseInterval = adtDataProvider.getLastDischargedCentralCaseEffectiveInterval("2");
    Assert.assertNull(lastDischargedCentralCaseInterval);
  }

  @Test
  public void getLastDischargeEncounterTimeForNonExistingPatient()
  {
    final Interval lastDischargedCentralCaseInterval = adtDataProvider.getLastDischargedCentralCaseEffectiveInterval("999");
    Assert.assertNull(lastDischargedCentralCaseInterval);
  }

  @Test
  public void getCentralCaseForMedicationsForExistingPatient()
  {
    final MedicationsCentralCaseDto centralCaseForMedicationsDto = adtDataProvider.getCentralCaseForMedicationsDto("1");
    Assert.assertNotNull(centralCaseForMedicationsDto);
    Assert.assertFalse(centralCaseForMedicationsDto.isOutpatient());
    Assert.assertEquals("1", centralCaseForMedicationsDto.getCentralCaseId());
  }

  @Test
  public void getCentralCaseForMedicationsForExistingPatientWithNoDischargeEncounters()
  {
    final MedicationsCentralCaseDto centralCaseForMedicationsDto = adtDataProvider.getCentralCaseForMedicationsDto("2");
    Assert.assertNotNull(centralCaseForMedicationsDto);
    Assert.assertTrue(centralCaseForMedicationsDto.isOutpatient());
    Assert.assertEquals("3", centralCaseForMedicationsDto.getCentralCaseId());
  }

  @Test
  public void getCentralCaseForMedicationsForNonExistingPatient()
  {
    final MedicationsCentralCaseDto centralCaseForMedicationsDto = adtDataProvider.getCentralCaseForMedicationsDto("999");
    Assert.assertNull(centralCaseForMedicationsDto);
  }

  @Test
  public void getPatientDataForTherapyReport()
  {
    Mockito.reset(patientDataProvider);
    Mockito.when(
        patientDataProvider.getPatientAllergies(Mockito.anyString(), Mockito.any(DateTime.class)))
        .thenReturn(Lists.newArrayList(new NamedExternalDto("1", "Allergy1"), new NamedExternalDto("2", "Allergy2")));

    final PatientDataForTherapyReportDto patientDataForTherapyReport = adtDataProvider.getPatientDataForTherapyReport(
        "1",
        true,
        new DateTime(
            2015,
            1,
            10,
            0,
            0),
        Locale.ENGLISH);

    Assert.assertNotNull(patientDataForTherapyReport);
    Assert.assertTrue(patientDataForTherapyReport.isInpatient());
    Assert.assertEquals("Patient1", patientDataForTherapyReport.getPatientName());
  }

  @Test
  public void testGetPatientDiseases()
  {
    final List<ExternalCatalogDto> patientDiseases = adtDataProvider.getPatientDiseases("1");
    Assert.assertNotNull(patientDiseases);
    Assert.assertEquals(2, patientDiseases.size());
  }
}
