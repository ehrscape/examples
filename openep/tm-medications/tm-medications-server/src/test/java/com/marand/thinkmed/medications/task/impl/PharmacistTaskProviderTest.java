/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.marand.thinkmed.medications.task.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.marand.ispek.bpm.service.BpmService;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.PartialList;
import com.marand.maf.core.security.remoting.GlobalAuditContext;
import com.marand.maf.core.service.ConstantUserMetadataProvider;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.RequestContextImpl;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.business.LabelDisplayValuesProvider;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringePreparationDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.task.DispenseMedicationTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.SupplyReminderTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.SupplyReviewTaskDto;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.pharmacist.impl.PharmacistTaskProviderImpl;
import com.marand.thinkmed.medications.task.DispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeTaskDef;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import com.marand.thinkmed.medications.task.SupplyReviewTaskDef;
import com.marand.thinkmed.medications.task.SupplyTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.patient.PatientDataProvider;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.or;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings({"TooBroadScope", "unchecked"})
@RunWith(MockitoJUnitRunner.class)
public class PharmacistTaskProviderTest
{
  @InjectMocks
  private PharmacistTaskProvider pharmacistTaskProvider = new PharmacistTaskProviderImpl();

  @Mock
  private PatientDataProvider patientDataProvider;

  @Mock
  private OverviewContentProvider overviewContentProvider;

  @Mock
  private ProcessService processService;

  @Mock
  private LabelDisplayValuesProvider labelDisplayValuesProvider;

  @Test
  public void testBuildPharmacistViewOpenedTasks()
  {
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.SUPPLY_REMINDER, TaskTypeEnum.SUPPLY_REVIEW);
    setUpMocksForSupplyTest(taskTypes, false);

    final List<MedicationSupplyTaskDto> tasks =
        pharmacistTaskProvider.findSupplyTasks(
            null,
            createPatientWithLocationTestMap(),
            taskTypes,
            false,
            true,
            new DateTime(2015, 9, 14, 12, 0),
            new Locale("SI", "SI"));

    assertEquals(2, tasks.size());

    assertTrue(tasks.get(0) instanceof SupplyReviewTaskDto);
    assertNull(tasks.get(0).getSupplyInDays());
    assertEquals(new DateTime(2015, 9, 10, 0, 0), tasks.get(0).getCreatedDateTime());
    assertEquals(new DateTime(2015, 9, 10, 0, 0), ((SupplyReviewTaskDto)tasks.get(0)).getDueDate());
    assertNull(tasks.get(0).getSupplyTypeEnum());
    assertTrue(((SupplyReviewTaskDto)tasks.get(0)).isAlreadyDispensed());
    assertEquals("patient1", tasks.get(0).getPatientDisplayDto().getName());
    assertEquals(
        "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4::1",
        tasks.get(0).getTherapyDayDto().getTherapy().getCompositionUid());

    assertTrue(tasks.get(1) instanceof SupplyReminderTaskDto);
    assertEquals(Integer.valueOf(2), tasks.get(1).getSupplyInDays());
    assertEquals(new DateTime(2015, 9, 10, 0, 0), tasks.get(1).getCreatedDateTime());
    assertEquals(new DateTime(2015, 9, 15, 0, 0), ((SupplyReminderTaskDto)tasks.get(1)).getDueDate());
    assertEquals(MedicationSupplyTypeEnum.ONE_STOP_DISPENSING, tasks.get(1).getSupplyTypeEnum());
    assertEquals("patient1", tasks.get(1).getPatientDisplayDto().getName());
    assertEquals(
        "e9b4cb80-9697-4a83-b7a1-7237d4ecb211::2",
        tasks.get(1).getTherapyDayDto().getTherapy().getCompositionUid());

    resetMocks();
  }

  @Before
  public void setUp()
  {
    RequestContextHolder.clearContext();
    RequestContextHolder.setContext(
        new RequestContextImpl(
            1L,
            GlobalAuditContext.current(),
            new DateTime(),
            Opt.of(ConstantUserMetadataProvider.createMetadata("1", "test"))));
  }

  @Test
  public void testBuildPharmacistViewClosedTasks()
  {
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.SUPPLY_REMINDER, TaskTypeEnum.SUPPLY_REVIEW);
    setUpMocksForSupplyTest(taskTypes, true);
    final List<MedicationSupplyTaskDto> tasks =
        pharmacistTaskProvider.findSupplyTasks(
            null,
            createPatientWithLocationTestMap(),
            taskTypes,
            false,
            true,
            new DateTime(2015, 9, 14, 12, 0),
            new Locale("SI", "SI"));
    assertEquals(1, tasks.size());
    assertTrue(tasks.get(0) instanceof SupplyReminderTaskDto);
    assertEquals(new DateTime(2015, 9, 15, 0, 0), tasks.get(0).getClosedDateTime());
    assertEquals("patient1", tasks.get(0).getPatientDisplayDto().getName());
    assertEquals(
        "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4::1",
        tasks.get(0).getTherapyDayDto().getTherapy().getCompositionUid());

    resetMocks();
  }

  @Test
  public void testBuildDispenseViewOpenedTasks()
  {
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.DISPENSE_MEDICATION);
    setUpMocksForSupplyTest(taskTypes, false);
    final List<MedicationSupplyTaskDto> tasks =
        pharmacistTaskProvider.findSupplyTasks(
            null,
            createPatientWithLocationTestMap(),
            taskTypes,
            false,
            true,
            new DateTime(2015, 9, 14, 12, 0),
            new Locale("SI", "SI"));
    assertEquals(1, tasks.size());
    assertTrue(tasks.get(0) instanceof DispenseMedicationTaskDto);
    assertEquals(Integer.valueOf(2), tasks.get(0).getSupplyInDays());
    assertEquals("patient2", tasks.get(0).getPatientDisplayDto().getName());
    assertNull(((DispenseMedicationTaskDto)tasks.get(0)).getLastPrintedTimestamp());
    assertEquals(TherapyAssigneeEnum.PHARMACIST, ((DispenseMedicationTaskDto)tasks.get(0)).getRequesterRole());
    assertEquals(SupplyRequestStatus.VERIFIED, ((DispenseMedicationTaskDto)tasks.get(0)).getSupplyRequestStatus());
    assertEquals(
        "e9b4cb80-9697-4a83-b7a1-7237d4ecb200::2",
        tasks.get(0).getTherapyDayDto().getTherapy().getCompositionUid());

    resetMocks();
  }

  @Test
  public void testFindSupplySimpleTasksForTherapy()
  {
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.SUPPLY_REVIEW);
    setUpMocksForSupplySimpleTest(taskTypes, false);
    final List<MedicationSupplyTaskSimpleDto> tasks =
        pharmacistTaskProvider.findSupplySimpleTasksForTherapy(
            null,
            Collections.singleton("1"),
            taskTypes,
            "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order");
    assertEquals(1, tasks.size());

    assertEquals(TaskTypeEnum.SUPPLY_REVIEW, tasks.get(0).getTaskType());
    assertEquals(2, tasks.get(0).getSupplyInDays().intValue());
    assertEquals(MedicationSupplyTypeEnum.ONE_STOP_DISPENSING, tasks.get(0).getSupplyTypeEnum());
    assertEquals("task1", tasks.get(0).getTaskId());

    resetMocks();
  }

  @Test
  public void testBuildDispenseViewClosedTasks()
  {
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.DISPENSE_MEDICATION);
    setUpMocksForSupplyTest(taskTypes, true);
    final List<MedicationSupplyTaskDto> tasks =
        pharmacistTaskProvider.findSupplyTasks(
            null,
            createPatientWithLocationTestMap(),
            taskTypes,
            false,
            true,
            new DateTime(2015, 9, 14, 12, 0),
            new Locale("SI", "SI"));
    assertEquals(1, tasks.size());
    assertEquals("patient2", tasks.get(0).getPatientDisplayDto().getName());
    assertEquals(new DateTime(2015, 9, 14, 1, 0), tasks.get(0).getClosedDateTime());
    assertEquals(new DateTime(2015, 9, 14, 0, 55), ((DispenseMedicationTaskDto)tasks.get(0)).getLastPrintedTimestamp());
    assertEquals(
        "e9b4cb80-9697-4a83-b7a1-7237d4ecb200::2",
        tasks.get(0).getTherapyDayDto().getTherapy().getCompositionUid());

    resetMocks();
  }

  @Test
  public void testGetOriginalTherapyIdAndPerfusionSyringeTaskIdMap()
  {
    final Set<TaskTypeEnum> taskTypes = TaskTypeEnum.PERFUSION_SYRINGE_TASKS_SET;

    setUpMocksForPerfusionSyringeTestWithTaskVariables(taskTypes, false);

    final Map<String, String> originalTherapyIdAndPerfusionSyringeTaskIdMap =
        pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringeTaskIdMap("1", true);
    assertEquals(4, originalTherapyIdAndPerfusionSyringeTaskIdMap.size());
    final String t1 = "e9b4cb80-9697-4a83-b7a1-7237d4ecb211|Medication order";
    assertEquals("task1", originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t1));
    final String t2 = "e9b4cb80-9697-4a83-b7a1-7237d4ecb200|Medication order";
    assertEquals("task2", originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t2));
    final String t3 = "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order";
    assertEquals("task3", originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t3));
    final String t4 = "e9b4cb80-9697-4a83-b7a1-7237d4ecb210|Medication order";
    assertEquals("task4", originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t4));
    resetMocks();
  }

  @Test
  public void testPatientHasTasksClosedInInterval()
  {
    final Set<TaskTypeEnum> taskTypes = Collections.singleton(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE);

    setUpMocksForPerfusionSyringeTestForSingleValue();

    final boolean hasTasksClosedInInterval = pharmacistTaskProvider.therapyHasTasksClosedInInterval(
        "1",
        "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order",
        taskTypes,
        new Interval(new DateTime(2015, 9, 14, 14, 0), new DateTime(2015, 9, 14, 22, 0)));
    assertTrue(hasTasksClosedInInterval);
  }

  @Test
  public void testPatientHasNoTasksClosedInInterval()
  {
    final Set<TaskTypeEnum> taskTypes = Collections.singleton(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE);

    setUpMocksForPerfusionSyringeTestForSingleValue();

    final boolean hasTasksClosedInInterval = pharmacistTaskProvider.therapyHasTasksClosedInInterval(
        "1",
        "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order",
        taskTypes,
        new Interval(new DateTime(2015, 9, 14, 20, 0), new DateTime(2015, 9, 14, 22, 0)));
    assertFalse(hasTasksClosedInInterval);
  }

  @Test
  public void testPatientHasTasksClosedInIntervalWithNullPatient()
  {
    testPatientHasTasksClosedInIntervalAssertError(
        null,
        Collections.singleton(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE),
        new Interval(new DateTime(2015, 9, 14, 14, 0), new DateTime(2015, 9, 14, 22, 0)),
        "patientId is null");
  }

  @Test
  public void testPatientHasTasksClosedInIntervalWithNullTaskType()
  {
    testPatientHasTasksClosedInIntervalAssertError(
        "1L",
        null,
        new Interval(new DateTime(2015, 9, 14, 14, 0), new DateTime(2015, 9, 14, 22, 0)),
        "taskTypesSet is null");
  }

  private void testPatientHasTasksClosedInIntervalAssertError(
      final String patientId,
      final Set<TaskTypeEnum> taskTypes,
      final Interval interval,
      final String errorMessage)
  {
    setUpMocksForPerfusionSyringeTestForSingleValue();
    try
    {
      pharmacistTaskProvider.therapyHasTasksClosedInInterval(
          patientId,
          "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order",
          taskTypes,
          interval);
      Assert.fail();
    }
    catch (final RuntimeException e)
    {
      assertEquals(errorMessage, e.getMessage());
    }
  }

  @Test
  public void testPatientHasNoTasksClosedInIntervalWithNullInterval()
  {
    testPatientHasTasksClosedInIntervalAssertError(
        "1L",
        Collections.singleton(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE),
       null,
        "interval is null");
  }

  @Test
  public void testGetOriginalTherapyIdAndPerfusionSyringePreparationDtoMap()
  {
    RequestContextHolder.setContext(
        new RequestContextImpl(
            1L,
            GlobalAuditContext.current(),
            new DateTime(),
            Opt.of(ConstantUserMetadataProvider.createMetadata("11", "test"))));

    setUpMocksForPerfusionSyringeTestWithTaskVariables(TaskTypeEnum.PERFUSION_SYRINGE_TASKS_SET, false);

    final String t1 = "e9b4cb80-9697-4a83-b7a1-7237d4ecb211|Medication order";
    final String t2 = "e9b4cb80-9697-4a83-b7a1-7237d4ecb200|Medication order";
    final String t3 = "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order";
    final Map<String, PerfusionSyringePreparationDto> originalTherapyIdAndPerfusionSyringeTaskIdMap =
        pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringePreparationDtoMap(
            "1",
            true,
            Sets.newHashSet(t1, t2, t3),
            new DateTime(2015, 9, 14, 12, 0),
            new Locale("SI", "SI"));

    assertEquals(3, originalTherapyIdAndPerfusionSyringeTaskIdMap.size());
    assertEquals("task1", originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t1).getCompletePreparationTaskId());
    assertNotNull(originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t1).getPerfusionSyringeLabelDto());
    assertEquals("task2", originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t2).getCompletePreparationTaskId());
    assertNotNull(originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t2).getPerfusionSyringeLabelDto());
    assertEquals("task3", originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t3).getCompletePreparationTaskId());
    assertNotNull(originalTherapyIdAndPerfusionSyringeTaskIdMap.get(t3).getPerfusionSyringeLabelDto());

    resetMocks();
  }

  @Test
  public void testFindPerfusionSyringeTasks()
  {
    final Set<TaskTypeEnum> taskTypes = TaskTypeEnum.PERFUSION_SYRINGE_TASKS_SET;

    setUpMocksForPerfusionSyringeTestWithoutVariablesSearch(taskTypes, false);
    final List<PerfusionSyringePatientTasksDto> perfusionSyringeTasks = pharmacistTaskProvider.findPerfusionSyringeTasks(
        createPatientWithLocationTestMap(),
        null,
        taskTypes,
        false,
        new DateTime(2015, 9, 10, 12, 0),
        new Locale("SI", "SI"));

    assertEquals(3, perfusionSyringeTasks.size());
    assertTrue(perfusionSyringeTasks.get(0).isUrgent());
    assertTrue(perfusionSyringeTasks.get(1).isUrgent());
    assertFalse(perfusionSyringeTasks.get(2).isUrgent());

    assertEquals("patient1", perfusionSyringeTasks.get(0).getPatientDisplayDto().getName());
    assertEquals("patient2", perfusionSyringeTasks.get(1).getPatientDisplayDto().getName());
    assertEquals("patient1", perfusionSyringeTasks.get(2).getPatientDisplayDto().getName());

    final PerfusionSyringePatientTasksDto perfusionSyringePatientTasksDto = perfusionSyringeTasks.get(2);
    assertEquals(1, perfusionSyringePatientTasksDto.getTasksList().size());

    final PerfusionSyringeTaskDto taskDto = perfusionSyringePatientTasksDto.getTasksList().get(0);
    assertEquals("task2", taskDto.getId());
    assertEquals("e9b4cb80-9697-4a83-b7a1-7237d4ecb200|Medication order", taskDto.getOriginalTherapyId());
    assertEquals(4, taskDto.getNumberOfSyringes());
    assertEquals("task2", taskDto.getId());
    assertEquals(TaskTypeEnum.PERFUSION_SYRINGE_START, taskDto.getTaskType());
    assertEquals("11", taskDto.getOrderedBy().getId());
    assertEquals("Full Orderer Name", taskDto.getOrderedBy().getName());

    resetMocks();
  }

  private void resetMocks()
  {
    Mockito.reset(processService);
  }

  private void setUpMocksForSupplyTest(final Set<TaskTypeEnum> taskTypes, final boolean closedTasks)
  {
    resetMocks();

    final List<TaskDto> tasksList = getSupplyTasksList();
    filterTasks(tasksList, taskTypes, closedTasks);

    Mockito
        .when(
            processService.findTasks(
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyBoolean(),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyListOf(String.class),
                ArgumentMatchers.argThat(argument -> ((Set<TaskDetailsEnum>)argument).contains(TaskDetailsEnum.VARIABLES))))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));

    setUpSupplyCommonMocks();
  }

  private void setUpMocksForSupplySimpleTest(final Set<TaskTypeEnum> taskTypes, final boolean closedTasks)
  {
    resetMocks();

    final List<TaskDto> tasksList = getSupplySimpleTasksList();
    filterTasks(tasksList, taskTypes, closedTasks);

    Mockito
        .when(
            processService.findTasks(
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyBoolean(),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyListOf(String.class),
                ArgumentMatchers.argThat(argument -> argument.contains(TaskDetailsEnum.VARIABLES)),
                ArgumentMatchers.any(Pair.class)))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));

    setUpSupplyCommonMocks();
  }

  private void setUpSupplyCommonMocks()
  {
    final Map<String, TherapyDayDto> therapiesMap = createTherapiesMap();

    Mockito
        .when(
            overviewContentProvider.getOriginalCompositionUidAndLatestTherapyDayDtoMap(
                ArgumentMatchers.anyMapOf(String.class, String.class),
                ArgumentMatchers.any(Integer.class),
                ArgumentMatchers.any(DateTime.class),
                ArgumentMatchers.any(Locale.class))
        ).thenReturn(therapiesMap);
  }

  private void setUpMocksForPerfusionSyringeTestForSingleValue()
  {
    resetMocks();

    final List<TaskDto> tasksList = getSingletonPerfusionSyringeTaskList();

    Mockito
        .when(
            processService.findTasks(
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyBoolean(),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyList(),
                ArgumentMatchers.anySet(),
                ArgumentMatchers.any(Pair.class)))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));

    setUpCommonPerfusionSyringeMocks();
  }

  private void setUpMocksForPerfusionSyringeTestWithoutVariablesSearch(
      final Set<TaskTypeEnum> taskTypes,
      final boolean closedTasks)
  {
    resetMocks();

    final List<TaskDto> tasksList = getPerfusionSyringeTaskList();
    filterTasks(tasksList, taskTypes, closedTasks);

    Mockito
        .when(
            processService.findTasks(
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyBoolean(),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyList(),
                ArgumentMatchers.argThat(argument -> ((Set<TaskDetailsEnum>)argument).contains(TaskDetailsEnum.VARIABLES))))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));
    setUpCommonPerfusionSyringeMocks();
  }

  private void setUpMocksForPerfusionSyringeTestWithTaskVariables(
      final Set<TaskTypeEnum> taskTypes,
      final boolean closedTasks)
  {
    resetMocks();

    final List<TaskDto> tasksList = getPerfusionSyringeTaskList();
    filterTasks(tasksList, taskTypes, closedTasks);

    Mockito
        .when(
            processService.findTasks(
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.anyString(), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyBoolean(),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                or(ArgumentMatchers.any(DateTime.class), ArgumentMatchers.isNull()),
                ArgumentMatchers.anyListOf(String.class),
                ArgumentMatchers.argThat(argument -> ((Set<TaskDetailsEnum>)argument).contains(TaskDetailsEnum.VARIABLES)),
                ArgumentMatchers.any(Pair.class)))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));
    setUpCommonPerfusionSyringeMocks();
  }

  private void setUpCommonPerfusionSyringeMocks()
  {
    final Map<String, PatientDisplayWithLocationDto> patientDisplayWithLocationDtoMap = createPatientWithLocationTestMap();
    Mockito
        .when(
            patientDataProvider.getPatientDisplayWithLocationMap(
                or(ArgumentMatchers.anySetOf(String.class), ArgumentMatchers.isNull()),
                ArgumentMatchers.anySetOf(String.class))
        ).thenReturn(patientDisplayWithLocationDtoMap);

    final Map<String, TherapyDayDto> therapiesMap = createTherapiesMap();

    Mockito
        .when(
            overviewContentProvider.getOriginalCompositionUidAndLatestTherapyDayDtoMap(
                ArgumentMatchers.anyMapOf(String.class, String.class),
                ArgumentMatchers.any(Integer.class),
                ArgumentMatchers.any(DateTime.class),
                ArgumentMatchers.any(Locale.class))
        ).thenReturn(therapiesMap);
  }

  private Map<String, TherapyDayDto> createTherapiesMap()
  {
    final Map<String, TherapyDayDto> therapiesMap = new HashMap<>();

    final TherapyDayDto therapyDayDto1 = new TherapyDayDto();
    final TherapyDto therapyDto1 = new ConstantSimpleTherapyDto();
    therapyDto1.setCompositionUid("e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4::1");
    therapyDayDto1.setTherapy(therapyDto1);
    therapiesMap.put("e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4", therapyDayDto1);

    final TherapyDayDto therapyDayDto2 = new TherapyDayDto();
    final TherapyDto therapyDto2 = new ConstantSimpleTherapyDto();
    therapyDto2.setCompositionUid("e9b4cb80-9697-4a83-b7a1-7237d4ecb200::2");
    therapyDayDto2.setTherapy(therapyDto2);
    therapiesMap.put("e9b4cb80-9697-4a83-b7a1-7237d4ecb200", therapyDayDto2);

    final TherapyDayDto therapyDayDto3 = new TherapyDayDto();
    final TherapyDto therapyDto3 = new ConstantSimpleTherapyDto();
    therapyDto3.setCompositionUid("e9b4cb80-9697-4a83-b7a1-7237d4ecb211::2");
    therapyDayDto3.setTherapy(therapyDto3);
    therapiesMap.put("e9b4cb80-9697-4a83-b7a1-7237d4ecb211", therapyDayDto3);

    final TherapyDayDto therapyDayDto4 = new TherapyDayDto();
    final TherapyDto therapyDto4 = new ConstantSimpleTherapyDto();
    therapyDto4.setCompositionUid("e9b4cb80-9697-4a83-b7a1-7237d4ecb210::2");
    therapyDayDto4.setTherapy(therapyDto4);
    therapiesMap.put("e9b4cb80-9697-4a83-b7a1-7237d4ecb210", therapyDayDto4);

    return therapiesMap;
  }

  private Map<String, PatientDisplayDto> createPatientTestMap()
  {
    final Map<String, PatientDisplayDto> patientsMap = new HashMap<>();

    patientsMap.put("1", new PatientDisplayDto("1", "patient1", new DateTime(1990, 5, 14, 0, 0), Gender.MALE, ""));
    patientsMap.put("2", new PatientDisplayDto("2", "patient2", new DateTime(1995, 5, 14, 0, 0), Gender.FEMALE, ""));
    return patientsMap;
  }

  private Map<String, PatientDisplayWithLocationDto> createPatientWithLocationTestMap()
  {
    final Map<String, PatientDisplayWithLocationDto> patientWithLocationTestMap = new HashMap<>();
    final Map<String, PatientDisplayDto> patientTestMap = createPatientTestMap();
    patientWithLocationTestMap.put(
        "1",
        new PatientDisplayWithLocationDto(
            patientTestMap.get("1"),
            "careProviderName1",
            "R10B15"
        )
    );
    patientWithLocationTestMap.put(
        "2",
        new PatientDisplayWithLocationDto(
            patientTestMap.get("2"),
            "careProviderName2",
            "R10B16"
        )
    );
    return patientWithLocationTestMap;
  }

  private void filterTasks(final List<TaskDto> tasksList, final Set<TaskTypeEnum> taskTypes, final boolean closedTasks)
  {
    final List<TaskDto> filteredList = tasksList
        .stream()
        .filter(taskDto -> taskTypes == null ||
            (taskTypes.contains(TaskTypeEnum.getByName(taskDto.getTaskExecutionStrategyId())) && taskDto.isCompleted() == closedTasks))
        .collect(Collectors.toList());
    tasksList.clear();
    tasksList.addAll(filteredList);
  }

  private List<TaskDto> getSupplySimpleTasksList()
  {
    final List<TaskDto> tasksList = new ArrayList<>();

    final TaskDto task1 = new TaskDto();
    task1.setId("task1");
    task1.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task1.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REVIEW.getName());
    task1.setVariables(new HashMap<>());
    task1.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order");
    task1.getVariables().put(TherapyTaskDef.PATIENT_ID.getName(), "1");
    task1.getVariables().put(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName(), null);
    task1.getVariables().put(SupplyTaskDef.DAYS_SUPPLY.getName(), 2);
    task1.getVariables().put(SupplyTaskDef.SUPPLY_TYPE.getName(), "ONE_STOP_DISPENSING");
    task1.getVariables().put(SupplyReviewTaskDef.ALREADY_DISPENSED.getName(), true);

    tasksList.add(task1);
    return tasksList;
  }

  private List<TaskDto> getSupplyTasksList()
  {
    final List<TaskDto> tasksList = new ArrayList<>();

    final TaskDto task1 = new TaskDto();
    task1.setId("task1");
    task1.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task1.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REVIEW.getName());
    task1.setVariables(new HashMap<>());
    task1.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order");
    task1.getVariables().put(TherapyTaskDef.PATIENT_ID.getName(), "1");
    task1.getVariables().put(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName(), null);
    task1.getVariables().put(SupplyTaskDef.DAYS_SUPPLY.getName(), 2);
    task1.getVariables().put(SupplyTaskDef.SUPPLY_TYPE.getName(), "ONE_STOP_DISPENSING");
    task1.getVariables().put(SupplyReviewTaskDef.ALREADY_DISPENSED.getName(), true);

    tasksList.add(task1);

    final TaskDto task2 = new TaskDto();
    task2.setId("task2");
    task2.setDueTime(new DateTime(2015, 9, 15, 0, 0));
    task2.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task2.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REMINDER.getName());
    task2.setVariables(new HashMap<>());
    task2.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb211|Medication order");
    task2.getVariables().put(TherapyTaskDef.PATIENT_ID.getName(), "1");
    task2.getVariables().put(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName(), null);
    task2.getVariables().put(SupplyTaskDef.DAYS_SUPPLY.getName(), 2);
    task2.getVariables().put(SupplyTaskDef.SUPPLY_TYPE.getName(), "ONE_STOP_DISPENSING");
    tasksList.add(task2);

    final TaskDto task3 = new TaskDto();
    task3.setId("task3");
    task3.setDueTime(new DateTime(2015, 9, 13, 0, 0));
    task3.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task3.setTaskExecutionStrategyId(TaskTypeEnum.DISPENSE_MEDICATION.getName());
    task3.setVariables(new HashMap<>());
    task3.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb200|Medication order");
    task3.getVariables().put(TherapyTaskDef.PATIENT_ID.getName(), "2");
    task3.getVariables().put(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName(), null);
    task3.getVariables().put(SupplyTaskDef.DAYS_SUPPLY.getName(), 2);
    task3.getVariables().put(SupplyTaskDef.SUPPLY_TYPE.getName(), "ONE_STOP_DISPENSING");
    task3.getVariables().put(DispenseMedicationTaskDef.REQUESTER_ROLE.getName(), "PHARMACIST");
    task3.getVariables().put(DispenseMedicationTaskDef.REQUEST_STATUS.getName(), "VERIFIED");
    tasksList.add(task3);

    final TaskDto task4 = new TaskDto();
    task4.setId("task4");
    task4.setDueTime(new DateTime(2015, 9, 15, 0, 0));
    task4.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task4.setEndTime(new DateTime(2015, 9, 15, 0, 0));
    task4.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REMINDER.getName());
    task4.setVariables(new HashMap<>());
    task4.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order");
    task4.getVariables().put(TherapyTaskDef.PATIENT_ID.getName(), "1");
    task4.getVariables().put(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName(), null);
    task4.getVariables().put(SupplyTaskDef.DAYS_SUPPLY.getName(), 2);
    task4.getVariables().put(SupplyTaskDef.SUPPLY_TYPE.getName(), "ONE_STOP_DISPENSING");
    task4.setCompleted(true);
    tasksList.add(task4);

    final TaskDto task5 = new TaskDto();
    task5.setId("task5");
    task5.setDueTime(new DateTime(2015, 9, 13, 0, 0));
    task5.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task5.setEndTime(new DateTime(2015, 9, 14, 1, 0));
    task5.setTaskExecutionStrategyId(TaskTypeEnum.DISPENSE_MEDICATION.getName());
    task5.setVariables(new HashMap<>());
    task5.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb200|Medication order");
    task5.getVariables().put(TherapyTaskDef.PATIENT_ID.getName(), "2");
    task5.getVariables().put(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName(), null);
    task5.getVariables().put(SupplyTaskDef.DAYS_SUPPLY.getName(), 2);
    task5.getVariables().put(SupplyTaskDef.SUPPLY_TYPE.getName(), "ONE_STOP_DISPENSING");
    task5.getVariables().put(DispenseMedicationTaskDef.REQUESTER_ROLE.getName(), "PHARMACIST");
    task5.getVariables().put(DispenseMedicationTaskDef.REQUEST_STATUS.getName(), "VERIFIED");
    task5.getVariables().put(DispenseMedicationTaskDef.LAST_PRINTED_TIMESTAMP.getName(), new DateTime(2015, 9, 14, 0, 55));
    task5.setCompleted(true);
    tasksList.add(task5);

    return tasksList;
  }

  public List<TaskDto> getSingletonPerfusionSyringeTaskList()
  {
    final TaskDto task = new TaskDto();
    task.setId("task");
    task.setCreateTime(new DateTime(2015, 9, 14, 0, 0));
    task.setTaskExecutionStrategyId(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE.getName());
    task.setVariables(new HashMap<>());
    task.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order");
    task.getVariables().put(PerfusionSyringeTaskDef.IS_URGENT.getName(), true);
    task.getVariables().put(PerfusionSyringeTaskDef.NUMBER_OF_SYRINGES.getName(), 5);
    task.getVariables().put(PerfusionSyringeTaskDef.PATIENT_ID.getName(), "1");
    task.setCompleted(true);
    task.setEndTime(new DateTime(2015, 9, 14, 15, 0));

    return Collections.singletonList(task);
  }

  private List<TaskDto> getPerfusionSyringeTaskList()
  {
    final List<TaskDto> tasksList = new ArrayList<>();

    final TaskDto task1 = new TaskDto();
    task1.setId("task1");
    task1.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task1.setDueTime(new DateTime(2015, 10, 10, 0, 0));
    task1.setTaskExecutionStrategyId(TaskTypeEnum.PERFUSION_SYRINGE_START.getName());
    task1.setVariables(new HashMap<>());
    task1.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb211|Medication order");
    task1.getVariables().put(PerfusionSyringeTaskDef.IS_URGENT.getName(), true);
    task1.getVariables().put(PerfusionSyringeTaskDef.NUMBER_OF_SYRINGES.getName(), 5);
    task1.getVariables().put(PerfusionSyringeTaskDef.PATIENT_ID.getName(), "1");

    tasksList.add(task1);

    final TaskDto task2 = new TaskDto();
    task2.setId("task2");
    task2.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task2.setDueTime(new DateTime(2015, 10, 11, 0, 0));
    task2.setTaskExecutionStrategyId(TaskTypeEnum.PERFUSION_SYRINGE_START.getName());
    task2.setVariables(new HashMap<>());
    task2.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb200|Medication order");
    task2.getVariables().put(PerfusionSyringeTaskDef.IS_URGENT.getName(), false);
    task2.getVariables().put(PerfusionSyringeTaskDef.NUMBER_OF_SYRINGES.getName(), 4);
    task2.getVariables().put(PerfusionSyringeTaskDef.PATIENT_ID.getName(), "1");
    task2.getVariables().put(PerfusionSyringeTaskDef.ORDERER.getName(), "11");
    task2.getVariables().put(PerfusionSyringeTaskDef.ORDERER_FULL_NAME.getName(), "Full Orderer Name");

    tasksList.add(task2);

    final TaskDto task3 = new TaskDto();
    task3.setId("task3");
    task3.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task3.setDueTime(new DateTime(2015, 10, 12, 0, 0));
    task3.setTaskExecutionStrategyId(TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE.getName());
    task3.setVariables(new HashMap<>());
    task3.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order");
    task3.getVariables().put(PerfusionSyringeTaskDef.IS_URGENT.getName(), true);
    task3.getVariables().put(PerfusionSyringeTaskDef.NUMBER_OF_SYRINGES.getName(), 5);
    task3.getVariables().put(PerfusionSyringeTaskDef.PATIENT_ID.getName(), "1");
    task3.getVariables().put(PerfusionSyringeTaskDef.ORDERER.getName(), "12");
    task2.getVariables().put(PerfusionSyringeTaskDef.ORDERER_FULL_NAME.getName(), "Full Orderer Name");

    tasksList.add(task3);

    final TaskDto task4 = new TaskDto();
    task4.setId("task4");
    task4.setCreateTime(new DateTime(2015, 9, 10, 0, 0));
    task4.setDueTime(new DateTime(2015, 10, 12, 0, 0));
    task4.setTaskExecutionStrategyId(TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE.getName());
    task4.setVariables(new HashMap<>());
    task4.getVariables()
        .put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb210|Medication order");
    task4.getVariables().put(PerfusionSyringeTaskDef.IS_URGENT.getName(), true);
    task4.getVariables().put(PerfusionSyringeTaskDef.NUMBER_OF_SYRINGES.getName(), 5);
    task4.getVariables().put(PerfusionSyringeTaskDef.PATIENT_ID.getName(), "2");
    task4.getVariables().put(PerfusionSyringeTaskDef.ORDERER.getName(), "12");
    task2.getVariables().put(PerfusionSyringeTaskDef.ORDERER_FULL_NAME.getName(), "Full Orderer Name");

    tasksList.add(task4);

    return tasksList;
  }
}
