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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.marand.maf.core.PartialList;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.or;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class MedicationsTasksProviderTest
{
  @InjectMocks
  private MedicationsTasksProvider medicationsTasksProvider = new MedicationsTasksProviderImpl();

  @Mock
  private AdministrationTaskConverter administrationTaskConverter;

  @Mock
  private ProcessService processService;

  @Before
  public void setUpMocks()
  {
    Mockito.reset(processService);

    final List<TaskDto> tasksList = new ArrayList<>();

    final TaskDto task1 = new TaskDto();
    task1.setId("task1");
    task1.setDueTime(new DateTime(2015, 9, 14, 0, 0));
    task1.setTaskExecutionStrategyId(TaskTypeEnum.DOCTOR_REVIEW.getName());
    task1.setVariables(new HashMap<>());
    task1.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    tasksList.add(task1);

    final TaskDto task2 = new TaskDto();
    task2.setId("task2");
    task2.setDueTime(new DateTime(2015, 9, 15, 0, 0));
    task2.setTaskExecutionStrategyId(TaskTypeEnum.DOCTOR_REVIEW.getName());
    task2.setVariables(new HashMap<>());
    task2.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    tasksList.add(task2);

    final TaskDto task3 = new TaskDto();
    task3.setId("task3");
    task3.setDueTime(new DateTime(2015, 9, 13, 0, 0));
    task3.setTaskExecutionStrategyId(TaskTypeEnum.SWITCH_TO_ORAL.getName());
    task3.setVariables(new HashMap<>());
    task3.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy2");
    tasksList.add(task3);

    final TaskDto task4 = new TaskDto();
    task4.setId("task4");
    task4.setDueTime(new DateTime(2015, 9, 15, 0, 0));
    task4.setTaskExecutionStrategyId(TaskTypeEnum.SWITCH_TO_ORAL.getName());
    task4.setVariables(new HashMap<>());
    task4.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy2");
    tasksList.add(task4);

    final TaskDto task5 = new TaskDto();
    task5.setId("task5");
    task5.setDueTime(new DateTime(2015, 9, 14, 0, 0));
    task5.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REMINDER.getName());
    task5.setVariables(new HashMap<>());
    task5.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    tasksList.add(task5);

    final TaskDto task6 = new TaskDto();
    task6.setId("task6");
    task6.setDueTime(new DateTime(2015, 9, 15, 0, 0));
    task6.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REMINDER.getName());
    task6.setVariables(new HashMap<>());
    task6.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy2");
    tasksList.add(task6);

    final TaskDto task7 = new TaskDto();
    task7.setId("task7");
    task7.setDueTime(new DateTime(2015, 9, 14, 0, 0));
    task7.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REVIEW.getName());
    task7.setVariables(new HashMap<>());
    task7.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    tasksList.add(task7);

    final TaskDto task8 = new TaskDto();
    task8.setId("task8");
    task8.setDueTime(new DateTime(2015, 9, 20, 0, 0));
    task8.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REVIEW.getName());
    task8.setVariables(new HashMap<>());
    task8.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy3");
    tasksList.add(task8);



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
                ArgumentMatchers.anySetOf(TaskDetailsEnum.class)))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));

    final List<AdministrationPatientTaskDto> tasks = new ArrayList<>();
    final AdministrationPatientTaskDto patientTask1 = new AdministrationPatientTaskDto();
    patientTask1.setId("t1");
    patientTask1.setPlannedTime(new DateTime(2015, 9, 20, 0, 0));
    tasks.add(patientTask1);
    final AdministrationPatientTaskDto patientTask2 = new AdministrationPatientTaskDto();
    patientTask2.setId("t2");
    patientTask2.setPlannedTime(new DateTime(2015, 9, 22, 0, 0));
    tasks.add(patientTask2);
    final AdministrationPatientTaskDto patientTask3 = new AdministrationPatientTaskDto();
    patientTask3.setId("t2");
    patientTask3.setPlannedTime(new DateTime(2015, 10, 22, 0, 0));
    tasks.add(patientTask3);
    final AdministrationPatientTaskDto patientTask4 = new AdministrationPatientTaskDto();
    patientTask4.setId("t2");
    patientTask4.setPlannedTime(new DateTime(2015, 10, 22, 0, 0));
    tasks.add(patientTask3);
    final AdministrationPatientTaskDto patientTask5 = new AdministrationPatientTaskDto();
    patientTask5.setId("t2");
    patientTask5.setPlannedTime(new DateTime(2015, 10, 22, 0, 0));
    tasks.add(patientTask3);
    final AdministrationPatientTaskDto patientTask6 = new AdministrationPatientTaskDto();
    patientTask6.setId("t2");
    patientTask6.setPlannedTime(new DateTime(2015, 10, 22, 0, 0));
    tasks.add(patientTask3);
    final AdministrationPatientTaskDto patientTask7 = new AdministrationPatientTaskDto();
    patientTask7.setId("t2");
    patientTask7.setPlannedTime(new DateTime(2015, 10, 22, 0, 0));
    tasks.add(patientTask3);
    final AdministrationPatientTaskDto patientTask8 = new AdministrationPatientTaskDto();
    patientTask8.setId("t2");
    patientTask8.setPlannedTime(new DateTime(2015, 10, 22, 0, 0));
    tasks.add(patientTask3);

    Mockito
        .when(
            administrationTaskConverter.convertTasksToAdministrationPatientTasks(
                ArgumentMatchers.anyListOf(TaskDto.class),
                ArgumentMatchers.anyMapOf(String.class, PatientDisplayWithLocationDto.class),
                ArgumentMatchers.any(Locale.class),
                ArgumentMatchers.any(DateTime.class)))
        .thenAnswer(invocation -> {
          final List<TaskDto> administrationPatientTaskDtos = (List<TaskDto>)invocation.getArguments()[0];
          return tasks.subList(0, administrationPatientTaskDtos.size());
        });
  }

  @Test
  public void testBuildAdministrationFromTask()
  {
    final Set<String> therapyIds = new HashSet<>();
    therapyIds.add("therapy1");
    therapyIds.add("therapy2");
    therapyIds.add("therapy3");
    therapyIds.add("therapy4");

    final Map<String, List<TherapyTaskSimpleDto>> tasksMap =
        medicationsTasksProvider.findSimpleTasksForTherapies("1", therapyIds, new DateTime(2015, 9, 14, 12, 0));

    final List<TherapyTaskSimpleDto> tasksList1 = tasksMap.get("therapy1");
    assertEquals(3, tasksList1.size());
    assertEquals("task1", tasksList1.get(0).getId());
    assertEquals(TaskTypeEnum.DOCTOR_REVIEW, tasksList1.get(0).getTaskType());
    assertEquals(new DateTime(2015, 9, 14, 0, 0), tasksList1.get(0).getDueTime());
    assertEquals("task7", tasksList1.get(1).getId());
    assertEquals(TaskTypeEnum.SUPPLY_REVIEW, tasksList1.get(1).getTaskType());
    assertEquals(new DateTime(2015, 9, 14, 0, 0), tasksList1.get(1).getDueTime());
    assertEquals("task5", tasksList1.get(2).getId());
    assertEquals(TaskTypeEnum.SUPPLY_REMINDER, tasksList1.get(2).getTaskType());
    assertEquals(new DateTime(2015, 9, 14, 0, 0), tasksList1.get(2).getDueTime());

    final List<TherapyTaskSimpleDto> tasksList2 = tasksMap.get("therapy2");
    assertEquals(2, tasksList2.size());
    assertEquals("task3", tasksList2.get(0).getId());
    assertEquals(TaskTypeEnum.SWITCH_TO_ORAL, tasksList2.get(0).getTaskType());
    assertEquals(new DateTime(2015, 9, 13, 0, 0), tasksList2.get(0).getDueTime());
    assertEquals("task6", tasksList2.get(1).getId());
    assertEquals(TaskTypeEnum.SUPPLY_REMINDER, tasksList2.get(1).getTaskType());
    assertEquals(new DateTime(2015, 9, 15, 0, 0), tasksList2.get(1).getDueTime());

    final List<TherapyTaskSimpleDto> tasksList3 = tasksMap.get("therapy3");
    assertEquals(1, tasksList3.size());
    assertEquals("task8", tasksList3.get(0).getId());
    assertEquals(TaskTypeEnum.SUPPLY_REVIEW, tasksList3.get(0).getTaskType());
    assertEquals(new DateTime(2015, 9, 20, 0, 0), tasksList3.get(0).getDueTime());
  }

  @Test
  public void testGetAdministrationTasks()
  {
    final List<AdministrationPatientTaskDto> tasks = medicationsTasksProvider.findAdministrationTasks(
        getTestPatientWithLocationMap(),
        new Interval(new DateTime(2016, 1, 6, 12, 0), new DateTime(2016, 1, 7, 12, 0)),
        50,
        new Locale("en"),
        new DateTime(2016, 1, 7, 12, 15));
    assertEquals(8, tasks.size());
  }

  @Test
  public void testGetAdministrationTasksLimitSize()
  {
    final List<AdministrationPatientTaskDto> tasks = medicationsTasksProvider.findAdministrationTasks(
        getTestPatientWithLocationMap(),
        new Interval(new DateTime(2016, 1, 6, 12, 0), new DateTime(2016, 1, 7, 12, 0)),
        2,
        new Locale("en"),
        new DateTime(2016, 1, 7, 12, 15));
    assertEquals(2, tasks.size());
  }

  @Test
  public void testGetAdministrationTasksEmpty()
  {
    final List<AdministrationPatientTaskDto> tasks = medicationsTasksProvider.findAdministrationTasks(
        Collections.emptyMap(),
        new Interval(new DateTime(2016, 1, 6, 12, 0), new DateTime(2016, 1, 7, 12, 0)),
        50,
        new Locale("en"),
        new DateTime(2016, 1, 7, 12, 15));
    assertTrue(tasks.isEmpty());
  }

  private Map<String, PatientDisplayWithLocationDto> getTestPatientWithLocationMap()
  {
    final Map<String, PatientDisplayWithLocationDto> patientWithLocationMap = new HashMap<>();

    patientWithLocationMap.put(
        "patient1",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient1", "patient1", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp1", "R1B1"));
    patientWithLocationMap.put(
        "patient2",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient2", "patient2", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp1", "R1B2"));
    patientWithLocationMap.put(
        "patient3",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient3", "patient3", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp2", "R2B2"));
    return patientWithLocationMap;
  }
}
