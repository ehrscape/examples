package com.marand.thinkmed.medications.warnings.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.allergies.AllergiesHandler;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.allergies.CheckNewAllergiesTaskDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.warnings.additional.impl.AllergiesAdditionalWarningsProvider;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("TooBroadScope")
@RunWith(MockitoJUnitRunner.class)
public class AllergiesAdditionalWarningsProviderTest
{
  @InjectMocks
  private AllergiesAdditionalWarningsProvider allergiesAdditionalWarnings = new AllergiesAdditionalWarningsProvider();

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private AllergiesHandler allergiesHandler;

  @Mock
  private ProcessService processService;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Before
  public void setUp()
  {
    final DateTime date = new DateTime(2017, 2, 3, 0, 0);
    final String patient1 = "patient1";
    final String patient2 = "patient2";

    final List<CheckNewAllergiesTaskDto> allergyTasks = new ArrayList<>();
    final Set<NamedExternalDto> allergies = new HashSet<>();

    allergies.add(new NamedExternalDto("1", "allergy1"));
    allergies.add(new NamedExternalDto("2", "allergy2"));
    allergies.add(new NamedExternalDto("3", "allergy3"));
    allergies.add(new NamedExternalDto("4", "allergy4"));
    allergies.add(new NamedExternalDto("5", "allergy5"));

    allergyTasks.add(new CheckNewAllergiesTaskDto("task1", allergies));

    Mockito.when(medicationsTasksProvider.findNewAllergiesTasks(patient1)).thenReturn(allergyTasks);
    Mockito.when(medicationsTasksProvider.findNewAllergiesTasks(patient2)).thenReturn(Collections.emptyList());

    final NamedExternalDto medication1 = new NamedExternalDto("1", "medication1");
    final NamedExternalDto medication2 = new NamedExternalDto("2", "medication2");
    final NamedExternalDto medication3 = new NamedExternalDto("3", "medication3");
    final NamedExternalDto medication4 = new NamedExternalDto("4", "medication4");
    final NamedExternalDto medication5 = new NamedExternalDto("5", "medication5");

    final List<MedicationsWarningDto> allergyWarnings = new ArrayList<>();

    final MedicationsWarningDto warning1 = new MedicationsWarningDto();
    warning1.setMedications(Collections.singletonList(medication1));
    allergyWarnings.add(warning1);

    final MedicationsWarningDto warning2 = new MedicationsWarningDto();
    warning2.setMedications(Collections.singletonList(medication2));
    allergyWarnings.add(warning2);

    final List<NamedExternalDto> medicationsWarning3 = new ArrayList<>();
    medicationsWarning3.add(medication3);
    medicationsWarning3.add(medication4);
    medicationsWarning3.add(medication5);

    final MedicationsWarningDto warning3 = new MedicationsWarningDto();
    warning3.setMedications(medicationsWarning3);
    allergyWarnings.add(warning3);
  }

  @Test
  public void testGetAdditionalAllergyWarnings()
  {
    final DateTime date = new DateTime(2017, 2, 3, 0, 0);
    final String patientId = "patient1";
    final Opt<AdditionalWarningsDto> warnings = allergiesAdditionalWarnings.getAdditionalWarnings(
        patientId,
        createPatientData(),
        date,
        new Locale("en_GB"));

    assertNotNull(warnings);

    assertTrue(warnings.get().getTaskIds().contains("task1"));
  }

  @Test
  public void testNoAllergyTasks()
  {
    final DateTime date = new DateTime(2017, 2, 3, 0, 0);
    final String patientId = "patient2";
    final Opt<AdditionalWarningsDto> additionalWarnings = allergiesAdditionalWarnings.getAdditionalWarnings(
        patientId,
        createPatientData(),
        date,
        new Locale("en_GB"));

    assertTrue(additionalWarnings.isAbsent());
  }

  private PatientDataForMedicationsDto createPatientData()
  {
    return new PatientDataForMedicationsDto(
        new DateTime(),
        10.0,
        10.0,
        Gender.FEMALE,
        Collections.emptyList(),
        Collections.emptyList(),
        null);
  }
}
