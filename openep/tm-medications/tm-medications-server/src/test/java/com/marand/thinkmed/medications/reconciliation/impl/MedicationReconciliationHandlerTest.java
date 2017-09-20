package com.marand.thinkmed.medications.reconciliation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.admission.impl.MedicationOnAdmissionHandlerImpl;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.discharge.impl.MedicationOnDischargeHandlerImpl;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionReconciliationDto;
import com.marand.thinkmed.medications.dto.change.StringTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeReconciliationDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowGroupEnum;
import org.joda.time.DateTime;
import org.junit.After;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */
@SuppressWarnings("TooBroadScope")
@RunWith(MockitoJUnitRunner.class)
public class MedicationReconciliationHandlerTest
{
  private static final Locale LOCALE = new Locale("en_GB");

  @InjectMocks
  private MedicationReconciliationHandlerImpl medicationReconciliationHandler = new MedicationReconciliationHandlerImpl();

  @Mock
  private MedicationOnAdmissionHandlerImpl medicationOnAdmissionHandler;

  @Mock
  private MedicationOnDischargeHandlerImpl medicationOnDischargeHandler;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private TherapyChangeCalculator therapyChangeCalculator;

  @Before
  public void setUpMocks()
  {
    final List<MedicationOnAdmissionReconciliationDto> onAdmissionReconciliationDtoList = new ArrayList<>();

    final CodedNameDto namedIdDto = new CodedNameDto("10", "reason");

    final MedicationOnAdmissionReconciliationDto reconciliationDto = new MedicationOnAdmissionReconciliationDto();
    final TherapyDto therapyDto = new ConstantComplexTherapyDto();
    therapyDto.setCompositionUid("10L");
    reconciliationDto.setTherapy(therapyDto);
    final TherapyChangeReasonDto changeReasonDto = new TherapyChangeReasonDto();
    changeReasonDto.setChangeReason(namedIdDto);
    changeReasonDto.setComment("on admission 10L");
    reconciliationDto.setChangeReasonDto(changeReasonDto);
    onAdmissionReconciliationDtoList.add(reconciliationDto);

    final MedicationOnAdmissionReconciliationDto reconciliationDto1 = new MedicationOnAdmissionReconciliationDto();
    final TherapyDto therapyDto1 = new ConstantComplexTherapyDto();
    therapyDto1.setCompositionUid("20L");
    reconciliationDto1.setTherapy(therapyDto1);
    onAdmissionReconciliationDtoList.add(reconciliationDto1);
    final MedicationOnAdmissionReconciliationDto reconciliationDto2 = new MedicationOnAdmissionReconciliationDto();
    final TherapyDto therapyDto2 = new ConstantComplexTherapyDto();
    therapyDto2.setCompositionUid("30L");
    reconciliationDto2.setTherapy(therapyDto2);
    onAdmissionReconciliationDtoList.add(reconciliationDto2);

    final MedicationOnAdmissionReconciliationDto reconciliationDto3 = new MedicationOnAdmissionReconciliationDto();
    final TherapyDto therapyDto3 = new ConstantComplexTherapyDto();
    therapyDto3.setCompositionUid("30L");
    reconciliationDto3.setTherapy(therapyDto3);
    onAdmissionReconciliationDtoList.add(reconciliationDto3);

    final MedicationOnAdmissionReconciliationDto reconciliationDto7 = new MedicationOnAdmissionReconciliationDto();
    final TherapyDto therapyDto7 = new ConstantComplexTherapyDto();
    therapyDto7.setCompositionUid("70L");
    final TherapyChangeReasonDto ch7 = new TherapyChangeReasonDto();
    ch7.setComment("on admission 70L");
    ch7.setChangeReason(namedIdDto);
    reconciliationDto7.setChangeReasonDto(ch7);
    reconciliationDto7.setTherapy(therapyDto7);
    onAdmissionReconciliationDtoList.add(reconciliationDto7);

    final MedicationOnAdmissionReconciliationDto reconciliationDto8 = new MedicationOnAdmissionReconciliationDto();
    final TherapyDto therapyDto8 = new ConstantComplexTherapyDto();
    therapyDto8.setCompositionUid("80L");
    reconciliationDto8.setTherapy(therapyDto8);
    onAdmissionReconciliationDtoList.add(reconciliationDto8);

    final List<MedicationOnDischargeReconciliationDto> onDischargeReconciliationDtoList = new ArrayList<>();

    final MedicationOnDischargeReconciliationDto dischargeReconciliationDto = new MedicationOnDischargeReconciliationDto();
    final TherapyDto therapyDto4 = new ConstantComplexTherapyDto();
    therapyDto4.setCompositionUid("40L");
    dischargeReconciliationDto.setTherapy(therapyDto4);
    dischargeReconciliationDto.setLinkedAdmissionCompositionId("10L");
    onDischargeReconciliationDtoList.add(dischargeReconciliationDto);


    final MedicationOnDischargeReconciliationDto dischargeReconciliationDto2 = new MedicationOnDischargeReconciliationDto();
    final TherapyDto therapyDto5 = new ConstantComplexTherapyDto();
    therapyDto5.setCompositionUid("50L");
    dischargeReconciliationDto2.setTherapy(therapyDto5);

    final TherapyChangeReasonDto changeReasonDto2 = new TherapyChangeReasonDto();
    changeReasonDto2.setChangeReason(namedIdDto);
    changeReasonDto2.setComment("on discharge 50L");
    dischargeReconciliationDto2.setChangeReasonDto(changeReasonDto2);
    dischargeReconciliationDto2.setLinkedAdmissionCompositionId("20L");
    onDischargeReconciliationDtoList.add(dischargeReconciliationDto2);

    final MedicationOnDischargeReconciliationDto dischargeReconciliationDto3 = new MedicationOnDischargeReconciliationDto();
    final TherapyDto therapyDto6 = new ConstantComplexTherapyDto();
    therapyDto6.setCompositionUid("60L");
    dischargeReconciliationDto3.setTherapy(therapyDto6);
    onDischargeReconciliationDtoList.add(dischargeReconciliationDto3);

    final MedicationOnDischargeReconciliationDto dischargeReconciliationDto4 = new MedicationOnDischargeReconciliationDto();
    final TherapyDto therapyDto10 = new ConstantComplexTherapyDto();
    therapyDto10.setCompositionUid("90L");
    dischargeReconciliationDto4.setLinkedAdmissionCompositionId("80L");
    dischargeReconciliationDto4.setTherapy(therapyDto10);
    onDischargeReconciliationDtoList.add(dischargeReconciliationDto4);


    Mockito.reset(medicationOnAdmissionHandler);
    Mockito.when(
        medicationOnAdmissionHandler.getMedicationsOnAdmissionForReconciliation(
            "10",
            new DateTime(2015, 2, 3, 0, 0),
            new DateTime(2015, 6, 3, 0, 0),
            LOCALE))
        .thenReturn(onAdmissionReconciliationDtoList);

    Mockito.reset(medicationOnDischargeHandler);
    Mockito.when(
        medicationOnDischargeHandler.getMedicationsOnDischargeForReconciliation(
            "10",
            new DateTime(2015, 2, 3, 0, 0),
            new DateTime(2015, 6, 3, 0, 0),
            LOCALE))
        .thenReturn(onDischargeReconciliationDtoList);

    final Map<String, Pair<DateTime, TherapyChangeReasonDto>> reasonsForEditedAdmissionCompositions = new HashMap<>();
    final TherapyChangeReasonDto reasonDto = new TherapyChangeReasonDto();
    reasonDto.setComment("mapped for admission 20L");
    reasonDto.setChangeReason(namedIdDto);
    reasonsForEditedAdmissionCompositions.put("20L", Pair.of(DateTime.now().minusDays(5), reasonDto));

    final TherapyChangeReasonDto reasonDto3 = new TherapyChangeReasonDto();
    reasonDto3.setComment("mapped for edited 80L");
    reasonDto3.setChangeReason(namedIdDto);
    reasonsForEditedAdmissionCompositions.put("80L", Pair.of(DateTime.now().minusDays(5), reasonDto3));

    Mockito.reset(medicationsOpenEhrDao);
    Mockito.when(medicationsOpenEhrDao.getLastEditChangeReasonsForCompositionsFromAdmission("10"))
        .thenReturn(reasonsForEditedAdmissionCompositions);

    final Map<String, Pair<TherapyStatusEnum, TherapyChangeReasonDto>> reasonsForAbort = new HashMap<>();
    final TherapyChangeReasonDto reasonDto2 = new TherapyChangeReasonDto();
    reasonDto2.setComment("mapped for admission 30L");
    reasonDto2.setChangeReason(namedIdDto);
    reasonsForAbort.put("30L", Pair.of(TherapyStatusEnum.ABORTED, reasonDto2));

    Mockito.when(medicationsOpenEhrDao.getLastChangeReasonsForCompositionsFromAdmission("10", true))
        .thenReturn(reasonsForAbort);

    final List<TherapyChangeDto<?, ?>> therapyChangeDtos = new ArrayList<>();
    therapyChangeDtos.add(new StringTherapyChangeDto(TherapyChangeType.DOSE));

    final List<TherapyChangeDto<?, ?>> therapyChangeDtos2 = new ArrayList<>();
    therapyChangeDtos2.add(new StringTherapyChangeDto(TherapyChangeType.DOSE));

    Mockito.reset(therapyChangeCalculator);
    Mockito.when(therapyChangeCalculator.calculateTherapyChanges(therapyDto, therapyDto4, false, LOCALE))
        .thenReturn(therapyChangeDtos);

    Mockito.when(therapyChangeCalculator.calculateTherapyChanges(therapyDto1, therapyDto5, false, LOCALE))
        .thenReturn(therapyChangeDtos2);

    Mockito.when(therapyChangeCalculator.calculateTherapyChanges(therapyDto8, therapyDto10, false, LOCALE))
        .thenReturn(therapyChangeDtos2);
  }

  @After
  public void tearDown()
  {
    Mockito.validateMockitoUsage();
  }

  @Test
  public void testBuildingReconciliationRows()
  {
    final List<ReconciliationRowDto> reconciliationGroups = medicationReconciliationHandler.getReconciliationGroups(
        "10", new DateTime(2015, 2, 3, 0, 0),
        new DateTime(2015, 6, 3, 0, 0), LOCALE);

    assertEquals(6, reconciliationGroups.size());
    assertEquals(ReconciliationRowGroupEnum.CHANGED, reconciliationGroups.get(0).getGroupEnum());
    assertEquals("10L", reconciliationGroups.get(0).getTherapyOnAdmission().getCompositionUid());
    assertEquals("40L", reconciliationGroups.get(0).getTherapyOnDischarge().getCompositionUid());
    assertEquals("on admission 10L", reconciliationGroups.get(0).getChangeReasonDto().getComment());

    assertEquals(ReconciliationRowGroupEnum.CHANGED, reconciliationGroups.get(1).getGroupEnum());
    assertEquals("20L", reconciliationGroups.get(1).getTherapyOnAdmission().getCompositionUid());
    assertEquals("50L", reconciliationGroups.get(1).getTherapyOnDischarge().getCompositionUid());
    assertEquals("on discharge 50L", reconciliationGroups.get(1).getChangeReasonDto().getComment());

    assertEquals(ReconciliationRowGroupEnum.ONLY_ON_DISCHARGE, reconciliationGroups.get(2).getGroupEnum());
    assertNotNull(reconciliationGroups.get(2).getTherapyOnDischarge());

    assertEquals(ReconciliationRowGroupEnum.CHANGED, reconciliationGroups.get(3).getGroupEnum());
    assertEquals("80L", reconciliationGroups.get(3).getTherapyOnAdmission().getCompositionUid());
    assertEquals("90L", reconciliationGroups.get(3).getTherapyOnDischarge().getCompositionUid());
    assertEquals("mapped for edited 80L", reconciliationGroups.get(3).getChangeReasonDto().getComment());
    assertTrue(!reconciliationGroups.get(3).getChanges().isEmpty());

    assertEquals(ReconciliationRowGroupEnum.ONLY_ON_ADMISSION, reconciliationGroups.get(4).getGroupEnum());
    assertEquals("30L", reconciliationGroups.get(4).getTherapyOnAdmission().getCompositionUid());
    assertNull(reconciliationGroups.get(4).getTherapyOnDischarge());
    assertEquals("mapped for admission 30L", reconciliationGroups.get(4).getChangeReasonDto().getComment());

    assertEquals(ReconciliationRowGroupEnum.ONLY_ON_ADMISSION, reconciliationGroups.get(5).getGroupEnum());
    assertEquals("70L", reconciliationGroups.get(5).getTherapyOnAdmission().getCompositionUid());
    assertNull(reconciliationGroups.get(5).getTherapyOnDischarge());
    assertEquals("on admission 70L", reconciliationGroups.get(5).getChangeReasonDto().getComment());
  }
}
