package com.marand.thinkmed.patient.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.patient.PatientDataProvider;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

/**
 * @author Nejc Korasa
 */
@RunWith(MockitoJUnitRunner.class)
public class PatientDataProviderTest
{
  @InjectMocks
  private PatientDataProvider patientDataProvider = new PatientDataProviderImpl();

  @Mock
  private MedicationsConnector medicationsConnector;

  @Before
  public void mock()
  {
    final Map<String, PatientDisplayWithLocationDto> returnMap = new HashMap<>();
    returnMap.put(
        "patient1",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto("id1", "patient1", DateTime.now().minusYears(10), Gender.FEMALE, null),
            "careProvider",
            "roomAndBed"));

    returnMap.put(
        "patient2",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto("id2", "patient2", DateTime.now().minusYears(70), Gender.MALE, null),
            "careProvider",
            "roomAndBed"));

    //noinspection unchecked
    Mockito
        .when(medicationsConnector.getPatientDisplayWithLocationMap(ArgumentMatchers.anyCollection(), ArgumentMatchers.anyCollection()))
        .thenReturn(returnMap);
  }

  @Test
  public void testGetPatientDemographicDataWithImagePath()
  {
    final Map<String, PatientDisplayWithLocationDto> map = patientDataProvider.getPatientDisplayWithLocationMap(
        Collections.emptyList(),
        Collections.emptyList());

    Assert.assertTrue(map.values().stream().allMatch(v -> v.getPatientDisplayDto().getPatientImagePath() != null));
    Assert.assertTrue(map.get("patient1").getPatientDisplayDto().getPatientImagePath().contains("patient-06y-f.png"));
    Assert.assertTrue(map.get("patient2").getPatientDisplayDto().getPatientImagePath().contains("patient-70y-m.png"));
  }
}