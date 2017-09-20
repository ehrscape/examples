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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.PartialList;
import com.marand.maf.core.StringUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.allergies.CheckNewAllergiesTaskDto;
import com.marand.thinkmed.medications.dto.mentalHealth.CheckMentalHealthMedsTaskDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.CheckMentalHealthMedsTaskDef;
import com.marand.thinkmed.medications.task.CheckNewAllergiesTaskDef;
import com.marand.thinkmed.medications.task.DoctorReviewTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.PerfusionSyringeCompletePreparationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeDispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeStartPreparationTaskDef;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.SupplyReviewTaskDef;
import com.marand.thinkmed.medications.task.SwitchToOralTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.process.dto.AbstractTaskDto;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class MedicationsTasksProviderImpl implements MedicationsTasksProvider
{
  private ProcessService processService;
  private AdministrationTaskConverter administrationTaskConverter;

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Required
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Override
  public List<AdministrationTaskDto> findAdministrationTasks(
      final String patientId,
      final Collection<String> therapyIds,
      final Interval searchInterval,
      final boolean findHistoric)
  {
    if (therapyIds.isEmpty())
    {
      return new ArrayList<>();
    }
    final List<TaskDto> tasks =
        findAdministrationTasks(
            Collections.singletonList(AdministrationTaskDef.getTaskTypeEnum().buildKey(patientId)),
            therapyIds,
            searchInterval != null ? searchInterval.getStart() : null,
            searchInterval != null ? searchInterval.getEnd() : null,
            null,
            findHistoric);

    return tasks.stream()
        .map(task -> administrationTaskConverter.convertTaskToAdministrationTask(task))
        .collect(Collectors.toList());
  }

  @Override
  public List<TaskDto> findAdministrationTasks(
      final String patientId,
      final Collection<String> therapyIds,
      final DateTime taskDueAfter,
      final DateTime taskDueBefore,
      final String groupUUId,
      final boolean findHistoric)
  {
    final List<String> taskKeys = Collections.singletonList(AdministrationTaskDef.getTaskTypeEnum().buildKey(patientId));
    return findAdministrationTasks(taskKeys, therapyIds, taskDueAfter, taskDueBefore, groupUUId, findHistoric);
  }

  @Override
  public AdministrationTaskDto getAdministrationTask(@Nonnull final String taskId)
  {
    return Opt.of(processService.loadTask(taskId, false))
        .map(administrationTaskConverter::convertTaskToAdministrationTask)
        .orElseThrow(() -> new IllegalStateException("task " + taskId + " not found!"));
  }

  @Override
  public List<TaskDto> findAdministrationTasks(
      final Set<String> patientIds,
      final DateTime taskDueAfter,
      final DateTime taskDueBefore)
  {
    final List<String> taskKeys = patientIds
        .stream()
        .map(patientId -> AdministrationTaskDef.getTaskTypeEnum().buildKey(patientId))
        .collect(Collectors.toList());

    return findAdministrationTasks(taskKeys, null, taskDueAfter, taskDueBefore, null, false);
  }

  private List<TaskDto> findAdministrationTasks(
      final List<String> taskKeys,
      final Collection<String> therapyIds,
      final DateTime taskDueAfter,
      final DateTime taskDueBefore,
      final String groupUUId,
      final boolean findHistorical)
  {
    if (taskKeys.isEmpty())
    {
      return Collections.emptyList();
    }

    final List<TaskDto> allTasks = processService.findTasks(
        null,
        null,
        null,
        findHistorical,
        taskDueAfter,
        taskDueBefore,
        taskKeys,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    return allTasks.stream()
        .filter(task -> !findHistorical || !task.isDeleted())
        .filter(task -> groupUUId == null || groupUUId.equals(task.getVariables().get(AdministrationTaskDef.GROUP_UUID.getName())))
        .filter(task -> therapyIds == null
            || therapyIds.contains((String)task.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName())))
        .collect(Collectors.toList());
  }

  @Override
  public TaskDto getNextAdministrationTask(final String patientId, final DateTime fromWhen)
  {
    final List<TaskDto> tasks = findAdministrationTasks(patientId, null, fromWhen, null, null, false);

    TaskDto nextTask = null;
    for (final TaskDto task : tasks)
    {
      if (task.getDueTime().isAfter(fromWhen))
      {
        if (nextTask == null || task.getDueTime().isBefore(nextTask.getDueTime()))
        {
          nextTask = task;
        }
      }
    }
    return nextTask;
  }

  @Override
  public Opt<AdministrationTaskDto> findLastAdministrationTaskForTherapy(
      @Nonnull final String patientId,
      @Nonnull final String therapyId,
      final Interval searchInterval,
      final boolean findHistoric)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null!");
    Preconditions.checkNotNull(therapyId, "therapyId must not be null!");

    return findLastTaskForTherapy(patientId, therapyId, searchInterval, findHistoric)
        .map(task -> administrationTaskConverter.convertTaskToAdministrationTask(task));
  }

  private Opt<TaskDto> findLastTaskForTherapy(
      final String patientId,
      final String therapyId,
      final Interval searchInterval,
      final boolean findHistoric)
  {
    final List<TaskDto> tasks = findAdministrationTasks(
        Collections.singletonList(AdministrationTaskDef.getTaskTypeEnum().buildKey(patientId)),
        Collections.singletonList(therapyId),
        searchInterval != null ? searchInterval.getStart() : null,
        searchInterval != null ? searchInterval.getEnd() : null,
        null,
        findHistoric);

    return Opt.from(
        tasks.stream()
            .max(Comparator.comparing(TaskDto::getDueTime)));
  }

  @Override
  public Opt<DateTime> findLastAdministrationTaskTimeForTherapy(
      @Nonnull final String patientId,
      @Nonnull final String therapyId,
      final Interval searchInterval,
      final boolean findHistoric)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null!");
    Preconditions.checkNotNull(therapyId, "therapyId must not be null!");

    return findLastTaskForTherapy(patientId, therapyId, searchInterval, findHistoric).map(AbstractTaskDto::getDueTime);
  }

  @Override
  public Map<String, DateTime> findLastAdministrationTaskTimesForTherapies(
      final Collection<String> patientIds,
      final DateTime fromTime,
      final boolean findHistoric)
  {
    final List<String> taskKeys = patientIds
        .stream()
        .map(patientId -> AdministrationTaskDef.getTaskTypeEnum().buildKey(patientId))
        .collect(Collectors.toList());

    final List<TaskDto> administrationTasks = findAdministrationTasks(taskKeys, null, fromTime, null, null, findHistoric);

    final Map<String, DateTime> therapyLastAdministrationTaskTimestampMap = new HashMap<>();
    //noinspection Convert2streamapi
    for (final TaskDto taskDto : administrationTasks)
    {
      final String taskTherapyId = (String)taskDto.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName());
      final DateTime taskDueTime = taskDto.getDueTime();
      if (therapyLastAdministrationTaskTimestampMap.containsKey(taskTherapyId))
      {
        if (taskDueTime.isAfter(therapyLastAdministrationTaskTimestampMap.get(taskTherapyId)))
        {
          therapyLastAdministrationTaskTimestampMap.put(taskTherapyId, taskDueTime);
        }
      }
      else
      {
        therapyLastAdministrationTaskTimestampMap.put(taskTherapyId, taskDueTime);
      }
    }

    return therapyLastAdministrationTaskTimestampMap;
  }

  @Override
  public Map<String, List<TherapyTaskSimpleDto>> findSimpleTasksForTherapies(
      final String patientId,
      final Collection<String> therapyIds,
      final DateTime when)
  {
    final List<String> taskKeys = new ArrayList<>();
    taskKeys.add(DoctorReviewTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)));
    taskKeys.add(SwitchToOralTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)));
    taskKeys.add(SupplyReviewTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)));
    taskKeys.add(SupplyReminderTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)));
    taskKeys.add(PerfusionSyringeStartPreparationTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)));
    taskKeys.add(PerfusionSyringeCompletePreparationTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)));
    taskKeys.add(PerfusionSyringeDispenseMedicationTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)));

    final Map<String, TaskDto> doctorReviewTasks = new HashMap<>();
    final Map<String, TaskDto> switchToOralTasks = new HashMap<>();
    final Map<String, TaskDto> supplyReviewTasks = new HashMap<>();
    final Map<String, TaskDto> supplyReminderTasks = new HashMap<>();
    final Map<String, TaskDto> perfusionSyringeTasks = new HashMap<>();

    final PartialList<TaskDto> tasks = processService.findTasks(
        null,
        null,
        null,
        false,
        null,
        null,
        taskKeys,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    if (tasks != null)
    {
      for (final TaskDto task : tasks)
      {
        final String therapyId = (String)task.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
        if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.DOCTOR_REVIEW.getName()))
        {
          //don't show future tasks
          if (!task.getDueTime().withTimeAtStartOfDay().isAfter(when.withTimeAtStartOfDay()))
          {
            doctorReviewTasks.put(therapyId, task);
          }
        }
        else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SWITCH_TO_ORAL.getName()))
        {
          //don't show future tasks
          if (!task.getDueTime().withTimeAtStartOfDay().isAfter(when.withTimeAtStartOfDay()))
          {
            switchToOralTasks.put(therapyId, task);
          }
        }
        else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REVIEW.getName()))
        {
          supplyReviewTasks.put(therapyId, task);
        }
        else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REMINDER.getName()))
        {
          final Boolean isDismissed = (Boolean)task.getVariables().get(SupplyReminderTaskDef.IS_DISMISSED.getName());
          if (isDismissed == null || !isDismissed)
          {
            supplyReminderTasks.put(therapyId, task);
          }
        }
        else if (
            task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_START.getName())
                || task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE.getName())
                || task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE.getName()))
        {
          perfusionSyringeTasks.put(therapyId, task);
        }
      }
    }

    final Map<String, List<TherapyTaskSimpleDto>> tasksMap = new HashMap<>();
    for (final String therapyId : therapyIds)
    {
      tasksMap.put(therapyId, new ArrayList<>());

      if (doctorReviewTasks.containsKey(therapyId))
      {
        final TherapyTaskSimpleDto doctorReviewTask =
            buildTherapyTaskSimpleDto(doctorReviewTasks.get(therapyId), TaskTypeEnum.DOCTOR_REVIEW);
        tasksMap.get(therapyId).add(doctorReviewTask);
      }
      if (switchToOralTasks.containsKey(therapyId))
      {
        final TherapyTaskSimpleDto switchToOralTask =
            buildTherapyTaskSimpleDto(switchToOralTasks.get(therapyId), TaskTypeEnum.SWITCH_TO_ORAL);
        tasksMap.get(therapyId).add(switchToOralTask);
      }
      if (supplyReviewTasks.containsKey(therapyId))
      {
        final TherapyTaskSimpleDto supplyReviewTask =
            buildTherapyTaskSimpleDto(supplyReviewTasks.get(therapyId), TaskTypeEnum.SUPPLY_REVIEW);
        tasksMap.get(therapyId).add(supplyReviewTask);
      }
      if (supplyReminderTasks.containsKey(therapyId))
      {
        final TherapyTaskSimpleDto supplyReminderTask =
            buildTherapyTaskSimpleDto(supplyReminderTasks.get(therapyId), TaskTypeEnum.SUPPLY_REMINDER);
        tasksMap.get(therapyId).add(supplyReminderTask);
      }
      if (perfusionSyringeTasks.containsKey(therapyId))
      {
        final TaskDto perfusionSyringeTaskDto = perfusionSyringeTasks.get(therapyId);
        final TherapyTaskSimpleDto perfusionSyringeTaskSimple = buildTherapyTaskSimpleDto(
            perfusionSyringeTasks.get(therapyId),
            TaskTypeEnum.getByName(perfusionSyringeTaskDto.getTaskExecutionStrategyId()));
        tasksMap.get(therapyId).add(perfusionSyringeTaskSimple);
      }
    }
    return tasksMap;
  }

  private TherapyTaskSimpleDto buildTherapyTaskSimpleDto(final TaskDto taskDto, final TaskTypeEnum taskType)
  {
    final TherapyTaskSimpleDto simpleTaskDto = new TherapyTaskSimpleDto();
    simpleTaskDto.setId(taskDto.getId());
    simpleTaskDto.setDueTime(taskDto.getDueTime());
    simpleTaskDto.setTaskType(taskType);
    return simpleTaskDto;
  }

  @Override
  public List<AdministrationPatientTaskDto> findAdministrationTasks(
      final Map<String, PatientDisplayWithLocationDto> patientWithLocationMap,
      final Interval searchInterval,
      final int maxNumberOfTasks,
      final Locale locale,
      final DateTime when)
  {
    final List<TaskDto> tasks =
        findAdministrationTasks(patientWithLocationMap.keySet(), searchInterval.getStart(), searchInterval.getEnd());

    Collections.sort(tasks, (task1, task2) -> task1.getDueTime().compareTo(task2.getDueTime()));

    final List<TaskDto> filteredTasks = tasks.size() > maxNumberOfTasks ? tasks.subList(0, maxNumberOfTasks) : tasks;

    final List<AdministrationPatientTaskDto> list = administrationTaskConverter.convertTasksToAdministrationPatientTasks(
        filteredTasks, patientWithLocationMap, locale, when);

    Collections.sort(list, (task1, task2) -> task1.getPlannedTime().compareTo(task2.getPlannedTime()));

    return list;
  }

  @Override
  public List<CheckNewAllergiesTaskDto> findNewAllergiesTasks(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId must be defined");

    final PartialList<TaskDto> tasks = processService.findTasks(
        null,
        null,
        null,
        false,
        null,
        null,
        Collections.singletonList(CheckNewAllergiesTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId))),
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    return tasks
        .stream()
        .map(t -> new CheckNewAllergiesTaskDto(t.getId(), getAllergiesFromTask(t)))
        .collect(Collectors.toList());
  }

  @Override
  public List<CheckMentalHealthMedsTaskDto> findNewCheckMentalHealthMedsTasks(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId must be defined");

    final PartialList<TaskDto> tasks = processService.findTasks(
        null,
        null,
        null,
        false,
        null,
        null,
        Collections.singletonList(CheckMentalHealthMedsTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId))),
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    return tasks
        .stream()
        .map(t -> new CheckMentalHealthMedsTaskDto(t.getId()))
        .collect(Collectors.toList());
  }

  private Set<NamedExternalDto> getAllergiesFromTask(final TaskDto task)
  {
    return Sets.newHashSet(JsonUtil.fromJson(
        (String)task.getVariables().get(CheckNewAllergiesTaskDef.NEW_ALLERGIES.getName()),
        NamedExternalDto[].class));
  }
}