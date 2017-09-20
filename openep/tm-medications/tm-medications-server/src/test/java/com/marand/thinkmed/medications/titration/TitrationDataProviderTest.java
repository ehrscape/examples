package com.marand.thinkmed.medications.titration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyForTitrationDto;
import com.marand.thinkmed.medications.dto.TitrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.titration.impl.TitrationDataProviderImpl;
import org.joda.time.DateTime;
import org.joda.time.Interval;
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

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class TitrationDataProviderTest
{
  @InjectMocks
  private TitrationDataProvider titrationDataProvider = new TitrationDataProviderImpl();

  @Mock
  private MedicationsConnector medicationsConnector;

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsBo medicationsBo;

  @Mock
  private AdministrationProvider administrationProvider;

  @Mock
  private OverviewContentProvider overviewContentProvider;

  @Before
  public void resetMocks()
  {
    Mockito.reset(
        medicationsConnector,
        medicationsDao,
        medicationsOpenEhrDao,
        medicationsBo,
        overviewContentProvider);
  }

  @Test
  public void testGetDataForTitration()
  {
    mockMedicationsDao();
    mockMedicationsConnector();
    mockOverviewContentProvider();

    final TitrationDto dataForTitration = titrationDataProvider.getDataForTitration(
        "1",
        "uid1|1",
        TitrationType.BLOOD_SUGAR,
        new DateTime(2016, 2, 4, 12, 0),
        new DateTime(2016, 2, 8, 12, 0),
        new DateTime(2016, 2, 6, 12, 0),
        new Locale("en"));
    assertNotNull(dataForTitration);
    assertEquals(TitrationType.BLOOD_SUGAR, dataForTitration.getTitrationType());

    assertNotNull(dataForTitration.getMedicationData());
    assertEquals(Long.valueOf(333L), dataForTitration.getMedicationData().getMedication().getId());

    assertEquals(2, dataForTitration.getTherapies().size());
    final TherapyForTitrationDto therapyForTitrationDto1 = dataForTitration.getTherapies().get(0);
    assertEquals("uid1", therapyForTitrationDto1.getTherapy().getCompositionUid());
    assertEquals(Double.valueOf(500.0), therapyForTitrationDto1.getAdministrations().get(0).getQuantity());
    assertEquals(new DateTime(2016, 6, 2, 10, 0), therapyForTitrationDto1.getAdministrations().get(0).getTime());

    final TherapyForTitrationDto therapyForTitrationDto2 = dataForTitration.getTherapies().get(1);
    assertEquals("uid2", therapyForTitrationDto2.getTherapy().getCompositionUid());
    assertEquals("2", therapyForTitrationDto2.getTherapy().getEhrOrderName());

    assertEquals(1, dataForTitration.getResults().size());
    final QuantityWithTimeDto quantityWithTimeDto = dataForTitration.getResults().get(0);
    assertEquals(new DateTime(2016, 6, 6, 13, 0), quantityWithTimeDto.getTime());
    assertEquals(Double.valueOf(200.0), quantityWithTimeDto.getQuantity());
    assertEquals("comment", quantityWithTimeDto.getComment());
  }

  @Test
  public void testGetDataForTitrationNoData()
  {
    final TitrationDto dataForTitration = titrationDataProvider.getDataForTitration(
        "1",
        "uid1|1",
        TitrationType.BLOOD_SUGAR,
        new DateTime(2016, 2, 4, 12, 0),
        new DateTime(2016, 2, 8, 12, 0),
        new DateTime(2016, 2, 6, 12, 0),
        new Locale("en"));

    assertNotNull(dataForTitration);
    assertNull(dataForTitration.getMedicationData());
    assertTrue(dataForTitration.getTherapies().isEmpty());
    assertTrue(dataForTitration.getResults().isEmpty());
  }

  private void mockMedicationsDao()
  {
    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto();
    medication.setId(333L);
    medicationDataDto.setMedication(medication);
    medicationDataDto.setTitration(TitrationType.BLOOD_SUGAR);
    Mockito
        .when(medicationsDao.getMedicationData(anyLong(), isNull(), any(DateTime.class)))
        .thenReturn(medicationDataDto);
  }

  private void mockMedicationsConnector()
  {
    final List<QuantityWithTimeDto> observations = new ArrayList<>();
    final QuantityWithTimeDto observation = new QuantityWithTimeDto(new DateTime(2016, 6, 6, 13, 0), 200.0, "comment");
    observations.add(observation);
    Mockito
        .when(medicationsConnector.getBloodSugarObservations(anyString(), any()))
        .thenReturn(observations);
  }

  private void mockOverviewContentProvider()
  {
    final List<TherapyRowDto> therapyRows = new ArrayList<>();

    final MedicationDto medication = new MedicationDto();
    medication.setId(333L);

    final TherapyRowDto therapyRow1 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy1 = new ConstantSimpleTherapyDto();
    therapy1.setCompositionUid("uid1");
    therapy1.setEhrOrderName("1");
    therapy1.setTitration(TitrationType.BLOOD_SUGAR);
    therapy1.setMedication(medication);
    therapyRow1.setTherapy(therapy1);
    therapyRow1.setTherapyId("uid1|1");
    therapyRows.add(therapyRow1);

    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administration.setAdministrationTime(new DateTime(2016, 6, 2, 10, 0));
    final TherapyDoseDto administeredDose = new TherapyDoseDto();
    administeredDose.setNumerator(500.0);
    administeredDose.setNumeratorUnit("mg");
    administration.setAdministeredDose(administeredDose);
    therapyRow1.getAdministrations().add(administration);

    final TherapyRowDto therapyRow2 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy2 = new ConstantSimpleTherapyDto();
    therapy2.setCompositionUid("uid2");
    therapy2.setEhrOrderName("2");
    therapy2.setTitration(TitrationType.BLOOD_SUGAR);
    therapy2.setMedication(medication);
    therapyRow2.setTherapy(therapy2);
    therapyRow2.setTherapyId("uid2|2");
    therapyRows.add(therapyRow2);

    //noinspection unchecked
    Mockito
        .when(overviewContentProvider.buildTherapyRows(
            anyString(),
            anyList(),
            anyList(),
            anyList(),
            any(TherapySortTypeEnum.class),
            anyBoolean(),
            anyList(),
            isNull(),
            any(Interval.class),
            isNull(),
            any(Locale.class), any(DateTime.class)))
        .thenReturn(therapyRows);
  }
}
