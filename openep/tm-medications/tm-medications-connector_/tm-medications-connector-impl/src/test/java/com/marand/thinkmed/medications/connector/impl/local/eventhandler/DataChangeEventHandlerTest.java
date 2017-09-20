package com.marand.thinkmed.medications.connector.impl.local.eventhandler;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.event.AdmitPatientEvent;
import com.marand.thinkmed.medications.event.CentralCaseDetails;
import com.marand.thinkmed.medications.event.PatientDetails;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@ContextConfiguration({"/com/marand/thinkmed/medications/connector/impl/local/eventhandler/DataChangeEventHandlerTest-context.xml"})
@DbUnitConfiguration(databaseConnection = {"dbUnitDatabaseConnection"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@DatabaseSetup("DataChangeEventHandlerTest.xml")
@Transactional
public class DataChangeEventHandlerTest
{
  @Autowired
  private DataChangeEventHandler eventHandler;

  @Test
  public void testAdmitNullPatient()
  {
    eventHandler.handle((AdmitPatientEvent)null);
  }

  @Test(expected = NullPointerException.class)
  public void testAdmitNewPatientWithoutPatientAndCentralCaseDetails()
  {
    eventHandler.handle(new AdmitPatientEvent("1000", new DateTime(), null, "1", null));
  }

  @Test(expected = NullPointerException.class)
  public void testAdmitNewPatientWithoutPatientDetails()
  {
    final CentralCaseDetails centralCaseDetails = new CentralCaseDetails(false, "1", "1", "R1-B1");
    eventHandler.handle(new AdmitPatientEvent("1000", new DateTime(), null, "1", centralCaseDetails));
  }

  @Test(expected = NullPointerException.class)
  public void testAdmitNewPatientWithoutCentralCaseDetails()
  {
    final PatientDetails patientDetails = new PatientDetails("New Patient", new DateTime(2000, 1, 1, 0, 0), Gender.MALE, "New address");
    eventHandler.handle(new AdmitPatientEvent("1000", new DateTime(), patientDetails, "1", null));
  }

  @Test
  public void testAdmitNewPatientToKnownCareProvider()
  {
    final PatientDetails patientDetails = new PatientDetails("New Patient", new DateTime(2000, 1, 1, 0, 0), Gender.MALE, "New address");
    final CentralCaseDetails centralCaseDetails = new CentralCaseDetails(false, "1", "1", "R1-B1");
    final DateTime now = new DateTime();
    eventHandler.handle(new AdmitPatientEvent("1000", now, patientDetails, "1", centralCaseDetails));

    // TODO Check DB here!
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAdmitNewPatientToUnknownCareProvider()
  {
    final PatientDetails patientDetails = new PatientDetails("New Patient", new DateTime(2000, 1, 1, 0, 0), Gender.MALE, "New address");
    final CentralCaseDetails centralCaseDetails = new CentralCaseDetails(false, "1", "1", "R1-B1");
    final DateTime now = new DateTime();
    eventHandler.handle(new AdmitPatientEvent("1000", now, patientDetails, "999", centralCaseDetails));
  }

  @Test(expected = NullPointerException.class)
  public void testAdmitExistingPatientWithoutCentralCaseDetails()
  {
    eventHandler.handle(new AdmitPatientEvent("1", new DateTime(), null, "1", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAdmitExistingPatientToUnknownCareProvider()
  {
    final CentralCaseDetails centralCaseDetails = new CentralCaseDetails(false, "1", "1", "R1-B1");
    final DateTime now = new DateTime();
    eventHandler.handle(new AdmitPatientEvent("1", now, null, "999", centralCaseDetails));
  }

  @Test
  public void testAdmitExistingPatient()
  {
    final CentralCaseDetails centralCaseDetails = new CentralCaseDetails(false, "1", "1", "R1-B1");
    final DateTime now = new DateTime();
    eventHandler.handle(new AdmitPatientEvent("1", now, null, "1", centralCaseDetails));

    // TODO Check DB here!
  }
}
