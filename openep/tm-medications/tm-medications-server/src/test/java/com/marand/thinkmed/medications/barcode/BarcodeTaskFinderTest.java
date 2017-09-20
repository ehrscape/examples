package com.marand.thinkmed.medications.barcode;

import java.util.Collections;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.MedicationsFinder;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto.BarcodeSearchResult;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class BarcodeTaskFinderTest
{
  @InjectMocks
  private final BarcodeTaskFinder barcodeTaskFinder = new BarcodeTaskFinder();

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private MedicationsBo medicationsBo;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsFinder medicationsFinder;

  private final DateTime testTime = new DateTime(2017, 8, 23, 10, 0);

  @Test
  public void testGetAdministrationTaskForBarcodeNoMedication()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("222"))
        .thenReturn(null);

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "222", testTime);
    assertEquals(BarcodeSearchResult.NO_MEDICATION, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeNoDueTasks()
  {
    Mockito.reset(medicationsDao);
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("222"))
        .thenReturn(222L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Collections.emptyList());

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "222", testTime);
    assertEquals(BarcodeSearchResult.NO_TASK, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeNoMatchingTherapy()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("222"))
        .thenReturn(222L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "uid1|Medication instruction")));

    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionPairs(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(buildInstructionPair("uid1::1")));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "222", testTime);
    assertEquals(BarcodeSearchResult.NO_TASK, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeDirectMedicationMatch()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "uid1|Medication instruction")));

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair = buildInstructionPair(
        "uid1::1");
    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionPairs(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(instructionPair));

    Mockito
        .when(medicationsBo.getMedicationIds(instructionPair.getSecond().getOrder().get(0)))
        .thenReturn(Lists.newArrayList(999L));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.TASK_FOUND, dto.getBarcodeSearchResult());
    assertEquals("333", dto.getTaskId());
    assertEquals((Long)999L, dto.getMedicationId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeSimilarMedicationMatch()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "uid1|Medication instruction")));

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair = buildInstructionPair(
        "uid1::1");
    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionPairs(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(instructionPair));

    Mockito
        .when(medicationsBo.getMedicationIds(instructionPair.getSecond().getOrder().get(0)))
        .thenReturn(Lists.newArrayList(888L));

    Mockito
        .when(medicationsFinder.findMedicationProducts(888L, Lists.newArrayList(444L), testTime))
        .thenReturn(Lists.newArrayList(buildMedicationDto(888L), buildMedicationDto(999L)));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.TASK_FOUND, dto.getBarcodeSearchResult());
    assertEquals("333", dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeNoSimilarMedicationMatch()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "uid1|Medication instruction")));

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair = buildInstructionPair(
        "uid1::1");
    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionPairs(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(instructionPair));

    Mockito
        .when(medicationsBo.getMedicationIds(instructionPair.getSecond().getOrder().get(0)))
        .thenReturn(Lists.newArrayList(888L));

    Mockito
        .when(medicationsFinder.findMedicationProducts(888L, Lists.newArrayList(444L), testTime))
        .thenReturn(Lists.newArrayList(buildMedicationDto(888L), buildMedicationDto(777L)));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.NO_TASK, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeMultipleTasksSingleTherapy()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(
            buildTaskDto("333", "uid1|Medication instruction"),
            buildTaskDto("334", "uid1|Medication instruction")));

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair = buildInstructionPair("uid1::1");

    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionPairs(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(instructionPair));

    Mockito
        .when(medicationsBo.getMedicationIds(instructionPair.getSecond().getOrder().get(0)))
        .thenReturn(Lists.newArrayList(888L));

    Mockito
        .when(medicationsFinder.findMedicationProducts(888L, Lists.newArrayList(444L), testTime))
        .thenReturn(Lists.newArrayList(buildMedicationDto(888L), buildMedicationDto(999L)));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.MULTIPLE_TASKS, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeMultipleTasksMultipleTherapies()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(
            buildTaskDto("333", "uid1|Medication instruction"),
            buildTaskDto("334", "uid2|Medication instruction")));

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair1 = buildInstructionPair(
        "uid1::1");
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair2 = buildInstructionPair(
        "uid2::1");

    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionPairs(Sets.newHashSet("uid1", "uid2")))
        .thenReturn(Lists.newArrayList(instructionPair1, instructionPair2));

    Mockito
        .when(medicationsBo.getMedicationIds(instructionPair1.getSecond().getOrder().get(0)))
        .thenReturn(Lists.newArrayList(999L));
    Mockito
        .when(medicationsBo.getMedicationIds(instructionPair2.getSecond().getOrder().get(0)))
        .thenReturn(Lists.newArrayList(888L));

    Mockito
        .when(medicationsFinder.findMedicationProducts(888L, Lists.newArrayList(444L), testTime))
        .thenReturn(Lists.newArrayList(buildMedicationDto(888L), buildMedicationDto(999L)));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.MULTIPLE_TASKS, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeByTherapyId()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("123e4567-e89b-12d3-a456-426655440001|Medication instruction"))
        .thenReturn(null);

    Mockito
        .when(medicationsBo.getOriginalTherapyId(
            "1",
            "123e4567-e89b-12d3-a456-426655440000"))
        .thenReturn("123e4567-e89b-12d3-a456-426655440001|Medication instruction");

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "123e4567-e89b-12d3-a456-426655440000|Medication instruction")));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode(
        "1",
        "123e4567-e89b-12d3-a456-426655440001|Medication instruction",
        testTime);
    assertEquals(BarcodeSearchResult.TASK_FOUND, dto.getBarcodeSearchResult());
    assertEquals("333", dto.getTaskId());
    assertNull(dto.getMedicationId());
  }

  private Pair<MedicationOrderComposition, MedicationInstructionInstruction> buildInstructionPair(
      final String compositionUid)
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition(compositionUid, null, null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("Medication instruction");
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);

    instruction.getOrder().add(
        MedicationsTestUtils.buildTestOrderActivity(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            444L,
            null,
            null,
            null));

    return Pair.of(composition, instruction);
  }

  private MedicationDto buildMedicationDto(final Long medicationId)
  {
    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(medicationId);
    return medicationDto;
  }

  private TaskDto buildTaskDto(final String taskId, final String therapyId)
  {
    final TaskDto taskDto = new TaskDto();
    taskDto.setId(taskId);
    taskDto.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);
    taskDto.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), AdministrationTypeEnum.START.name());
    return taskDto;
  }
}
