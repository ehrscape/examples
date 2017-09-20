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

package com.marand.thinkmed.medications.pharmacist.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.marand.ispek.bpm.service.BpmService;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.PartialList;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.exception.UserWarning;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.UserMetadata;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.bpm.medications.process.PharmacySupplyProcess;
import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.business.LabelDisplayValuesProvider;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringeLabelDto;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringePreparationDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.SupplyDataForPharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.DispenseMedicationTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.DispenseMedicationTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PharmacistReminderTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PharmacistReviewTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.SupplyReminderTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.SupplyReminderTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.SupplyReviewTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.SupplyReviewTaskSimpleDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.task.DispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReminderTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReviewTaskDef;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import com.marand.thinkmed.medications.task.SupplyReviewTaskDef;
import com.marand.thinkmed.medications.task.SupplyTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskUtils;
import com.marand.thinkmed.patient.PatientDataProvider;
import com.marand.thinkmed.process.TaskCompletedType;
import com.marand.thinkmed.process.dto.AbstractTaskDto;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Klavdij Lapajne
 */
public class PharmacistTaskProviderImpl implements PharmacistTaskProvider
{
  private ProcessService processService;
  private MedicationsBo medicationsBo;
  private OverviewContentProvider overviewContentProvider;
  private BpmService bpmService;
  private LabelDisplayValuesProvider labelDisplayValuesProvider;
  private PatientDataProvider patientDataProvider;

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setOverviewContentProvider(final OverviewContentProvider overviewContentProvider)
  {
    this.overviewContentProvider = overviewContentProvider;
  }

  @Required
  public void setBpmService(final BpmService bpmService)
  {
    this.bpmService = bpmService;
  }

  @Required
  public void setLabelDisplayValuesProvider(final LabelDisplayValuesProvider labelDisplayValuesProvider)
  {
    this.labelDisplayValuesProvider = labelDisplayValuesProvider;
  }

  @Required
  public void setPatientDataProvider(final PatientDataProvider patientDataProvider)
  {
    this.patientDataProvider = patientDataProvider;
  }

  @Override
  public List<TaskDto> findPharmacistReminderAndSupplyTasks(final String patientId, final Interval searchInterval)
  {
    final DateTime taskDueAfter = searchInterval != null ? searchInterval.getStart() : null;
    final DateTime taskDueBefore = searchInterval != null ? searchInterval.getEnd() : null;

    final Set<TaskTypeEnum> taskTypes = EnumSet.of(
        TaskTypeEnum.PHARMACIST_REMINDER,
        TaskTypeEnum.SUPPLY_REMINDER,
        TaskTypeEnum.SUPPLY_REVIEW,
        TaskTypeEnum.DISPENSE_MEDICATION);

    final List<String> taskKeys = TherapyTaskUtils.getPatientIdKeysForTaskTypes(
        Collections.singleton(patientId),
        taskTypes);

    return processService.findTasks(
        TherapyAssigneeEnum.PHARMACIST.name(),
        null,
        null,
        false,
        taskDueAfter,
        taskDueBefore,
        taskKeys,
        EnumSet.of(TaskDetailsEnum.VARIABLES));
  }

  @Override
  public Opt<String> findPharmacistReviewTaskId(@Nonnull final String patientId)
  {
    Preconditions.checkNotNull(patientId, "patientId");

    final EnumSet<TaskDetailsEnum> details = EnumSet.of(TaskDetailsEnum.VARIABLES);
    final String assignee = TherapyAssigneeEnum.PHARMACIST.name();
    final List<String> taskKeys = TherapyTaskUtils.getPatientIdKeysForTaskTypes(
        Collections.singleton(patientId),
        EnumSet.of(TaskTypeEnum.PHARMACIST_REVIEW));

    //noinspection unchecked
    return Opt.from(
        processService.findTasks(assignee, null, null, false, null, null, taskKeys, details)
            .stream()
            .filter(Objects::nonNull)
            .findAny()
            .map(AbstractTaskDto::getId));
  }

  @Override
  public List<String> findTaskIds(
      final Interval searchInterval,
      final String assignee,
      final Set<String> patientIdsSet,
      final Set<TaskTypeEnum> taskTypes)
  {
    if (patientIdsSet.isEmpty())
    {
      return Collections.emptyList();
    }
    final List<String> patientIdKeysForTaskTypes = TherapyTaskUtils.getPatientIdKeysForTaskTypes(
        patientIdsSet,
        taskTypes);

    final PartialList<TaskDto> tasks = processService.findTasks(
        assignee,
        null,
        null,
        false,
        searchInterval != null ? searchInterval.getStart() : null,
        searchInterval != null ? searchInterval.getEnd() : null,
        patientIdKeysForTaskTypes,
        EnumSet.noneOf(TaskDetailsEnum.class));

    return new ArrayList<>(Lists.transform(tasks, input -> {
      Preconditions.checkNotNull(input);
      return input.getId();
    }));
  }

  @Override
  public List<PatientTaskDto> findPharmacistTasks(
      final Interval searchInterval,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      final Set<TaskTypeEnum> taskTypes)
  {
    final Set<String> patientIdsSet = Sets.newHashSet(patientIdAndPatientWithLocationMap.keySet());
    if (patientIdsSet.isEmpty())
    {
      return Collections.emptyList();
    }
    final List<String> patientIdKeysForTaskTypes = TherapyTaskUtils.getPatientIdKeysForTaskTypes(
        patientIdsSet,
        taskTypes);

    final List<TaskDto> tasks = processService.findTasks(
        TherapyAssigneeEnum.PHARMACIST.name(),
        null,
        null,
        false,
        searchInterval != null ? searchInterval.getStart() : null,
        searchInterval != null ? searchInterval.getEnd() : null,
        patientIdKeysForTaskTypes,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    return buildPharmacistTasks(tasks, patientIdAndPatientWithLocationMap);
  }

  @Override
  public List<MedicationSupplyTaskDto> findSupplyTasks(
      @Nullable final Interval searchInterval,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      final Set<TaskTypeEnum> taskTypes,
      final boolean closedTasksOnly,
      final boolean includeUnverifiedDispenseTasks,
      final DateTime when,
      final Locale locale)
  {
    if (patientIdAndPatientWithLocationMap.isEmpty())
    {
      return Collections.emptyList();
    }

    final Set<String> patientIdsSet = Sets.newHashSet(patientIdAndPatientWithLocationMap.keySet());

    final List<String> patientIdKeysForTaskTypes = TherapyTaskUtils.getPatientIdKeysForTaskTypes(
        patientIdsSet,
        taskTypes);
    final List<TaskDto> tasksList = processService.findTasks(
        TherapyAssigneeEnum.PHARMACIST.name(), //loading optimization
        closedTasksOnly ? MedsTaskDef.GROUP_NAME : null,
        null,
        closedTasksOnly,
        searchInterval != null ? searchInterval.getStart() : null,
        searchInterval != null ? searchInterval.getEnd() : null,
        patientIdKeysForTaskTypes,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    if (closedTasksOnly)   // findTasks loaded closed, opened and deleted tasks ... we want only closed tasks
    {
      removeOpenedAndDeletedTasksFromTaskList(tasksList);
    }
    else
    {
      removeDismissedReminderTasks(tasksList);
    }
    return buildSupplyTasks(
        tasksList,
        patientIdAndPatientWithLocationMap,
        includeUnverifiedDispenseTasks,
        closedTasksOnly,
        when,
        locale);
  }

  @Override
  public List<MedicationSupplyTaskSimpleDto> findSupplySimpleTasksForTherapy(
      @Nullable final Interval searchInterval,
      final Set<String> patientIdsSet,
      final Set<TaskTypeEnum> taskTypes,
      final String originalTherapyId)
  {
    if (patientIdsSet.isEmpty())
    {
      return Collections.emptyList();
    }

    final List<String> patientIdKeysForTaskTypes = TherapyTaskUtils.getPatientIdKeysForTaskTypes(
        patientIdsSet,
        taskTypes);
    final List<TaskDto> tasksList = processService.findTasks(
        TherapyAssigneeEnum.PHARMACIST.name(),
        null,
        null,
        false,
        searchInterval != null ? searchInterval.getStart() : null,
        searchInterval != null ? searchInterval.getEnd() : null,
        patientIdKeysForTaskTypes,
        EnumSet.of(TaskDetailsEnum.VARIABLES),
        Pair.of(TherapyTaskDef.ORIGINAL_THERAPY_ID, originalTherapyId));

    return buildSupplySimpleTasks(tasksList);
  }

  private void removeOpenedAndDeletedTasksFromTaskList(final List<TaskDto> tasks)
  {
    final List<TaskDto> closedTasks = new ArrayList<>();
    for (final TaskDto task : tasks)
    {
      if (task.isCompleted() && task.getCompletedType() != TaskCompletedType.DELETED)
      {
        closedTasks.add(task);
      }
    }
    tasks.clear();
    tasks.addAll(closedTasks);
  }

  private void removeDismissedReminderTasks(final List<TaskDto> tasks)
  {
    final List<TaskDto> activeTasks = new ArrayList<>();
    for (final TaskDto task : tasks)
    {
      final Boolean isDismissed = (Boolean)task.getVariables().get(SupplyReminderTaskDef.IS_DISMISSED.getName());
      if (isDismissed == null || !isDismissed)
      {
        activeTasks.add(task);
      }
    }
    tasks.clear();
    tasks.addAll(activeTasks);
  }

  @Override
  public List<TaskDto> findNurseSupplyTasksForTherapy(final String patientId, final String originalTherapyId)
  {
    // gets related (nurse) dispense and supplyReview tasks, where taskId is id of one of them
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(
        TaskTypeEnum.SUPPLY_REVIEW,
        TaskTypeEnum.DISPENSE_MEDICATION);
    final List<String> patientIdKeysForTaskTypes =
        TherapyTaskUtils.getPatientIdKeysForTaskTypes(Collections.singleton(patientId), taskTypes);

    return processService.findTasks(
        TherapyAssigneeEnum.PHARMACIST.name(),
        null,
        null,
        false,
        null,
        null,
        patientIdKeysForTaskTypes,
        EnumSet.of(TaskDetailsEnum.VARIABLES),
        Pair.of(TherapyTaskDef.ORIGINAL_THERAPY_ID, originalTherapyId),
        Pair.of(DispenseMedicationTaskDef.REQUESTER_ROLE, TherapyAssigneeEnum.NURSE.name()));
  }

  private List<PatientTaskDto> buildPharmacistTasks(
      final List<TaskDto> tasks,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap)
  {
    final List<PatientTaskDto> pharmacistTasks = getPatientTaskDtos(tasks, patientIdAndPatientWithLocationMap);
    sortPatientTaskDtos(pharmacistTasks);
    return pharmacistTasks;
  }

  private void sortPatientTaskDtos(final List<PatientTaskDto> patientTasks)
  {
    Collections.sort(
        patientTasks, (task1, task2) -> {
          if (task1 instanceof PharmacistReviewTaskDto && task2 instanceof PharmacistReviewTaskDto)
          {
            final PharmacistReviewTaskDto o1 = (PharmacistReviewTaskDto)task1;
            final PharmacistReviewTaskDto o2 = (PharmacistReviewTaskDto)task2;
            if (o1.getFirstAdministrationTimestamp() == null && o2.getFirstAdministrationTimestamp() == null)
            {
              return o1.getLastEditTimestamp().compareTo(o2.getLastEditTimestamp());
            }
            if (o1.getFirstAdministrationTimestamp() == null)
            {
              return 1;
            }
            if (o2.getFirstAdministrationTimestamp() == null)
            {
              return -1;
            }
            return o1.getFirstAdministrationTimestamp().compareTo(o2.getFirstAdministrationTimestamp());
          }
          if (task1 instanceof PharmacistReminderTaskDto && task2 instanceof PharmacistReminderTaskDto)
          {
            final PharmacistReminderTaskDto o1 = (PharmacistReminderTaskDto)task1;
            final PharmacistReminderTaskDto o2 = (PharmacistReminderTaskDto)task2;
            return o1.getReminderDate().compareTo(o2.getReminderDate());
          }
          if (task1 instanceof PharmacistReviewTaskDto && task2 instanceof PharmacistReminderTaskDto)
          {
            final PharmacistReviewTaskDto o1 = (PharmacistReviewTaskDto)task1;
            final PharmacistReminderTaskDto o2 = (PharmacistReminderTaskDto)task2;
            final DateTime reviewTaskTime = o1.getFirstAdministrationTimestamp() == null
                                            ? o1.getLastEditTimestamp()
                                            : o1.getFirstAdministrationTimestamp();
            final int compare = reviewTaskTime.compareTo(o2.getReminderDate());
            return compare == 0 ? 1 : compare;
          }
          if (task1 instanceof PharmacistReminderTaskDto && task2 instanceof PharmacistReviewTaskDto)
          {
            final PharmacistReminderTaskDto o1 = (PharmacistReminderTaskDto)task1;
            final PharmacistReviewTaskDto o2 = (PharmacistReviewTaskDto)task2;
            final DateTime reviewTaskTime = o2.getFirstAdministrationTimestamp() == null
                                            ? o2.getLastEditTimestamp()
                                            : o2.getFirstAdministrationTimestamp();
            final int compare = o1.getReminderDate().compareTo(reviewTaskTime);
            return compare == 0 ? -1 : compare;
          }
          return 0;
        });
  }

  private List<PatientTaskDto> getPatientTaskDtos(
      final List<TaskDto> tasks,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap)
  {
    final List<PatientTaskDto> pharmacistTasks = new ArrayList<>();
    for (final TaskDto task : tasks)
    {
      if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PHARMACIST_REVIEW.getName()))
      {
        final PharmacistReviewTaskDto reviewTask = new PharmacistReviewTaskDto();
        final String patientId = (String)task.getVariables().get(MedsTaskDef.PATIENT_ID.getName());
        reviewTask.setId(task.getId());
        fillPatientDataForPatientTaskDto(reviewTask, patientIdAndPatientWithLocationMap, patientId);
        reviewTask.setStatus(
            PharmacistReviewTaskStatusEnum.valueOf(
                (String)task.getVariables().get(PharmacistReviewTaskDef.STATUS.getName())));
        reviewTask.setChangeType(
            PrescriptionChangeTypeEnum.valueOf(
                (String)task.getVariables().get(PharmacistReviewTaskDef.CHANGE_TYPE.getName())));
        reviewTask.setFirstAdministrationTimestamp(
            task.getDueTime() != null ? new DateTime(task.getDueTime()) : null);
        reviewTask.setLastEditTimestamp(
            new DateTime(task.getVariables().get(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS.getName())));
        reviewTask.setLastEditorName(
            (String)task.getVariables().get(PharmacistReviewTaskDef.LAST_EDITOR_NAME.getName()));
        pharmacistTasks.add(reviewTask);
      }
      else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PHARMACIST_REMINDER.getName()))
      {
        final PharmacistReminderTaskDto reminderTask = new PharmacistReminderTaskDto();
        reminderTask.setReminderDate(task.getDueTime());
        reminderTask.setReminderNote((String)task.getVariables().get(PharmacistReminderTaskDef.COMMENT.getName()));
        final String patientId = (String)task.getVariables().get(PharmacistReminderTaskDef.PATIENT_ID.getName());
        reminderTask.setId(task.getId());
        fillPatientDataForPatientTaskDto(reminderTask, patientIdAndPatientWithLocationMap, patientId);
        pharmacistTasks.add(reminderTask);
      }
    }

    return pharmacistTasks;
  }

  private List<MedicationSupplyTaskDto> buildSupplyTasks(
      final List<TaskDto> tasksList,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      final boolean includeUnverifiedDispenseTasks,
      final boolean closedTasks,
      final DateTime when,
      final Locale locale)
  {
    final List<MedicationSupplyTaskDto> pharmacistTasks = getMedicationSupplyTaskDtos(
        tasksList,
        patientIdAndPatientWithLocationMap,
        includeUnverifiedDispenseTasks,
        closedTasks,
        when,
        locale);
    sortSupplyTaskDtos(pharmacistTasks, !closedTasks);
    return pharmacistTasks;
  }

  private List<MedicationSupplyTaskSimpleDto> buildSupplySimpleTasks(final List<TaskDto> tasksList)
  {
    final List<MedicationSupplyTaskSimpleDto> supplySimpleTaskDtos = new ArrayList<>();
    for (final TaskDto task : tasksList)
    {
      final MedicationSupplyTaskSimpleDto supplyTaskSimpleDto;

      if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.DISPENSE_MEDICATION.getName()))
      {
        supplyTaskSimpleDto = new DispenseMedicationTaskSimpleDto();
        ((DispenseMedicationTaskSimpleDto)supplyTaskSimpleDto).setRequesterRole(
            TherapyAssigneeEnum.valueOf(
                (String)task.getVariables().get(DispenseMedicationTaskDef.REQUESTER_ROLE.getName())));
      }
      else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REVIEW.getName()))
      {
        supplyTaskSimpleDto = new SupplyReviewTaskSimpleDto();
        final Boolean alreadyDispensed = (Boolean)task.getVariables().get(SupplyReviewTaskDef.ALREADY_DISPENSED.getName());
        ((SupplyReviewTaskSimpleDto)supplyTaskSimpleDto).setAlreadyDispensed(
            alreadyDispensed == null
            ? false
            : alreadyDispensed);
      }
      else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REMINDER.getName()))
      {
        supplyTaskSimpleDto = new SupplyReminderTaskSimpleDto();
        supplyTaskSimpleDto.setTaskType(TaskTypeEnum.getByName(task.getTaskExecutionStrategyId()));
        final Boolean isDismissed = (Boolean)task.getVariables().get(SupplyReminderTaskDef.IS_DISMISSED.getName());
        ((SupplyReminderTaskSimpleDto)supplyTaskSimpleDto).setDismissed(isDismissed != null && isDismissed);
      }
      else
      {
        throw new IllegalStateException("Only supply tasks expected!");
      }

      supplyTaskSimpleDto.setTaskId(task.getId());
      supplyTaskSimpleDto.setSupplyInDays(
          (Integer)task.getVariables().get(SupplyTaskDef.DAYS_SUPPLY.getName()));
      final String supplyTypeName = (String)task.getVariables().get(SupplyTaskDef.SUPPLY_TYPE.getName());
      supplyTaskSimpleDto.setSupplyTypeEnum(
          supplyTypeName != null ? MedicationSupplyTypeEnum.valueOf(supplyTypeName) : null);
      supplySimpleTaskDtos.add(supplyTaskSimpleDto);
    }
    return supplySimpleTaskDtos;
  }

  private void sortSupplyTaskDtos(final List<MedicationSupplyTaskDto> medicationSupplyTaskDtos, final boolean sortAscending)
  {
    Collections.sort(
        medicationSupplyTaskDtos, (task1, task2) -> {
          final DateTime sortDateTime1 = getSupplyTaskSortTime(task1);
          final DateTime sortDateTime2 = getSupplyTaskSortTime(task2);
          return sortAscending ? sortDateTime1.compareTo(sortDateTime2) : sortDateTime2.compareTo(sortDateTime1);
        });
  }

  private DateTime getSupplyTaskSortTime(final MedicationSupplyTaskDto task)
  {
    if (task.getClosedDateTime() != null)
    {
      return task.getClosedDateTime();
    }
    else if (task instanceof SupplyReminderTaskDto)
    {
      return ((SupplyReminderTaskDto)task).getDueDate();
    }
    else
    {
      return task.getCreatedDateTime();
    }
  }

  private List<MedicationSupplyTaskDto> getMedicationSupplyTaskDtos(
      final List<TaskDto> tasksList,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      final boolean includeUnverifiedDispenseTasks,
      final boolean closedTasks,
      final DateTime when,
      final Locale locale)
  {
    final Map<String, MedicationSupplyTaskDto> taskIdAndMedicationSupplyTaskDtoMap = new HashMap<>();
    final Map<String, String> therapyCompositionUidAndPatientIdMap = new HashMap<>();
    final Map<String, Set<String>> therapyUidAndDispenseSupplyTaskIdsMap = new HashMap<>();
    final Map<String, Set<String>> therapyUidAndReminderSupplyTaskIdMap = new HashMap<>();
    final Map<String, Set<String>> therapyUidAndReviewSupplyTaskIdMap = new HashMap<>();
    for (final TaskDto task : tasksList)
    {
      //common variables
      final MedicationSupplyTaskDto medicationSupplyTaskDto;
      final String patientId = (String)task.getVariables().get(MedsTaskDef.PATIENT_ID.getName());
      final String originalTherapyId =
          (String)task.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
      final String closedWithTherapyId =
          (String)task.getVariables().get(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName());

      final String compositionUid = closedTasks
                                    ? TherapyIdUtils.getCompositionUidWithoutVersion(closedWithTherapyId)
                                    : TherapyIdUtils.parseTherapyId(originalTherapyId).getFirst();
      //task type dependant variables
      if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.DISPENSE_MEDICATION.getName()))
      {
        medicationSupplyTaskDto = new DispenseMedicationTaskDto();
        if (therapyUidAndDispenseSupplyTaskIdsMap.get(compositionUid) != null)
        {
          therapyUidAndDispenseSupplyTaskIdsMap.get(compositionUid).add(task.getId());
        }
        else
        {
          final Set<String> tasksSet = new HashSet<>();
          tasksSet.add(task.getId());
          therapyUidAndDispenseSupplyTaskIdsMap.put(compositionUid, tasksSet);
        }
        medicationSupplyTaskDto.setSupplyInDays(
            (Integer)task.getVariables().get(DispenseMedicationTaskDef.DAYS_SUPPLY.getName()));
        final String supplyTypeName = (String)task.getVariables().get(DispenseMedicationTaskDef.SUPPLY_TYPE.getName());
        medicationSupplyTaskDto.setSupplyTypeEnum(
            supplyTypeName != null ? MedicationSupplyTypeEnum.valueOf(supplyTypeName) : null);
        final SupplyRequestStatus requestStatus = SupplyRequestStatus.valueOf(
            (String)task.getVariables().get(DispenseMedicationTaskDef.REQUEST_STATUS.getName()));
        ((DispenseMedicationTaskDto)medicationSupplyTaskDto).setSupplyRequestStatus(requestStatus);
        ((DispenseMedicationTaskDto)medicationSupplyTaskDto).setRequesterRole(
            TherapyAssigneeEnum.valueOf(
                (String)task.getVariables().get(DispenseMedicationTaskDef.REQUESTER_ROLE.getName())));
        ((DispenseMedicationTaskDto)medicationSupplyTaskDto).setLastPrintedTimestamp(
            (DateTime)task.getVariables().get(DispenseMedicationTaskDef.LAST_PRINTED_TIMESTAMP.getName()));

        if (includeUnverifiedDispenseTasks || requestStatus == SupplyRequestStatus.VERIFIED)
        {
          taskIdAndMedicationSupplyTaskDtoMap.put(task.getId(), medicationSupplyTaskDto);
        }
      }
      else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REMINDER.getName()))
      {
        medicationSupplyTaskDto = new SupplyReminderTaskDto();
        if (therapyUidAndReminderSupplyTaskIdMap.get(compositionUid) != null)
        {
          therapyUidAndReminderSupplyTaskIdMap.get(compositionUid).add(task.getId());
        }
        else
        {
          final Set<String> tasksSet = new HashSet<>();
          tasksSet.add(task.getId());
          therapyUidAndReminderSupplyTaskIdMap.put(compositionUid, tasksSet);
        }
        medicationSupplyTaskDto.setSupplyInDays(
            (Integer)task.getVariables().get(SupplyReminderTaskDef.DAYS_SUPPLY.getName()));
        ((SupplyReminderTaskDto)medicationSupplyTaskDto).setDueDate(task.getDueTime());

        final String supplyTypeName = (String)task.getVariables().get(SupplyReminderTaskDef.SUPPLY_TYPE.getName());
        medicationSupplyTaskDto.setSupplyTypeEnum(
            supplyTypeName != null ? MedicationSupplyTypeEnum.valueOf(supplyTypeName) : null);
        taskIdAndMedicationSupplyTaskDtoMap.put(task.getId(), medicationSupplyTaskDto);
      }
      else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REVIEW.getName()))
      {
        medicationSupplyTaskDto = new SupplyReviewTaskDto();
        if (therapyUidAndReviewSupplyTaskIdMap.get(compositionUid) != null)
        {
          therapyUidAndReviewSupplyTaskIdMap.get(compositionUid).add(task.getId());
        }
        else
        {
          final Set<String> tasksSet = new HashSet<>();
          tasksSet.add(task.getId());
          therapyUidAndReviewSupplyTaskIdMap.put(compositionUid, tasksSet);
        }
        final Boolean alreadyDispensed = (Boolean)task.getVariables().get(SupplyReviewTaskDef.ALREADY_DISPENSED.getName());
        ((SupplyReviewTaskDto)medicationSupplyTaskDto).setDueDate(task.getCreateTime());
        ((SupplyReviewTaskDto)medicationSupplyTaskDto).setAlreadyDispensed(
            alreadyDispensed == null
            ? false
            : alreadyDispensed);
        taskIdAndMedicationSupplyTaskDtoMap.put(task.getId(), medicationSupplyTaskDto);
      }
      else
      {
        throw new IllegalArgumentException("Only supply tasks expected here. " + task.getTaskExecutionStrategyId() + " not allowed!");
      }

      medicationSupplyTaskDto.setCreatedDateTime(task.getCreateTime());
      medicationSupplyTaskDto.setClosedDateTime(task.getEndTime());
      medicationSupplyTaskDto.setId(task.getId());

      therapyCompositionUidAndPatientIdMap.put(compositionUid, patientId);
      fillPatientDataForPatientTaskDto(medicationSupplyTaskDto, patientIdAndPatientWithLocationMap, patientId);
    }

    final Map<String, TherapyDayDto> therapyUidAndTherapyDayDtoMap =
        closedTasks
        ? overviewContentProvider.getCompositionUidAndTherapyDayDtoMap(
            therapyCompositionUidAndPatientIdMap,
            when,
            locale)
        : overviewContentProvider.getOriginalCompositionUidAndLatestTherapyDayDtoMap(
            therapyCompositionUidAndPatientIdMap,
            2,
            when,
            locale);

    fillTherapyDataForSupplyTasks(
        taskIdAndMedicationSupplyTaskDtoMap,
        therapyUidAndDispenseSupplyTaskIdsMap,
        therapyUidAndReminderSupplyTaskIdMap,
        therapyUidAndReviewSupplyTaskIdMap,
        therapyUidAndTherapyDayDtoMap);
    return Lists.newArrayList(taskIdAndMedicationSupplyTaskDtoMap.values());
  }

  private void fillPatientDataForPatientTaskDto(
      final PatientTaskDto patientTaskDto,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      final String patientId)
  {
    if (patientIdAndPatientWithLocationMap.containsKey(patientId))
    {
      final PatientDisplayWithLocationDto patientWithLocationDto = patientIdAndPatientWithLocationMap.get(patientId);
      patientTaskDto.setPatientDisplayDto(patientWithLocationDto.getPatientDisplayDto());
      patientTaskDto.setCareProviderName(patientWithLocationDto.getCareProviderName());
    }
  }

  private <T extends MedicationSupplyTaskDto> void fillTherapyDataForSupplyTasks(
      final Map<String, T> taskIdAndMedicationSupplyTaskDtoMap,
      final Map<String, Set<String>> therapyUidAndDispenseSupplyTaskIdsMap,
      final Map<String, Set<String>> therapyUidAndReminderSupplyTaskIdMap,
      final Map<String, Set<String>> therapyUidAndReviewSupplyTaskIdMap,
      final Map<String, TherapyDayDto> therapyUidAndTherapyDayDtoMap)
  {
    for (final Map.Entry<String, TherapyDayDto> entry : therapyUidAndTherapyDayDtoMap.entrySet())
    {
      final String therapyUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(entry.getKey());
      final TherapyDayDto therapyDayDto = entry.getValue();

      final Set<String> medicationDispenseSupplyTasksIds =
          therapyUidAndDispenseSupplyTaskIdsMap.get(therapyUidWithoutVersion);
      if (medicationDispenseSupplyTasksIds != null)
      {
        for (final String medicationDispenseSupplyTaskId : medicationDispenseSupplyTasksIds)
        {
          taskIdAndMedicationSupplyTaskDtoMap.get(medicationDispenseSupplyTaskId).setTherapyDayDto(therapyDayDto);
        }
      }
      final Set<String> medicationReminderSupplyTaskIds =
          therapyUidAndReminderSupplyTaskIdMap.get(therapyUidWithoutVersion);
      if (medicationReminderSupplyTaskIds != null)
      {
        for (final String medicationReminderSupplyTaskId : medicationReminderSupplyTaskIds)
        {
          taskIdAndMedicationSupplyTaskDtoMap.get(medicationReminderSupplyTaskId).setTherapyDayDto(therapyDayDto);
        }
      }
      final Set<String> medicationReviewSupplyTaskIds =
          therapyUidAndReviewSupplyTaskIdMap.get(therapyUidWithoutVersion);
      if (medicationReviewSupplyTaskIds != null)
      {
        for (final String medicationReviewSupplyTaskId : medicationReviewSupplyTaskIds)
        {
          taskIdAndMedicationSupplyTaskDtoMap.get(medicationReviewSupplyTaskId).setTherapyDayDto(therapyDayDto);
        }
      }
    }
  }

  @Override
  public MedicationSupplyTaskSimpleDto getSupplySimpleTask(
      final String taskId,
      final DateTime when,
      final Locale locale)
  {
    final TaskDto supplyTask = processService.loadTask(taskId, false);
    final List<MedicationSupplyTaskSimpleDto> medicationSupplyTaskDtos =
        buildSupplySimpleTasks(Collections.singletonList(supplyTask));
    return medicationSupplyTaskDtos.isEmpty() ? null : medicationSupplyTaskDtos.get(0);
  }

  @Override
  public SupplyDataForPharmacistReviewDto getSupplyDataForPharmacistReview(
      @Nonnull final String patientId,
      @Nonnull final String therapyCompositionUid)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    StringUtils.checkNotBlank(therapyCompositionUid, "therapyCompositionUid");

    final String originalTherapyId = medicationsBo.getOriginalTherapyId(patientId, therapyCompositionUid);

    final SupplyDataForPharmacistReviewDto supplyDataForPharmacistReviewDto = new SupplyDataForPharmacistReviewDto();

    final List<MedicationSupplyTaskSimpleDto> supplyTasks =
        findSupplySimpleTasksForTherapy(
            null,
            Collections.singleton(patientId),
            EnumSet.of(TaskTypeEnum.SUPPLY_REMINDER),
            originalTherapyId);

    if (!supplyTasks.isEmpty())
    {
      Preconditions.checkArgument(supplyTasks.size() == 1, "We expect one or zero SUPPLY REMINDER tasks!");
      supplyDataForPharmacistReviewDto.setProcessExists(true); // if we have an active reminder task, process exists
      final SupplyReminderTaskSimpleDto supplyTaskSimpleDto = (SupplyReminderTaskSimpleDto)supplyTasks.get(0);
      supplyDataForPharmacistReviewDto.setTaskId(supplyTaskSimpleDto.getTaskId());
      supplyDataForPharmacistReviewDto.setDaysSupply(supplyTaskSimpleDto.getSupplyInDays());
      supplyDataForPharmacistReviewDto.setSupplyTypeEnum(supplyTaskSimpleDto.getSupplyTypeEnum());
      supplyDataForPharmacistReviewDto.setDismissed(supplyTaskSimpleDto.isDismissed());
    }
    else
    {
      supplyDataForPharmacistReviewDto.setProcessExists(
          bpmService.isProcessInExecution(originalTherapyId, PharmacySupplyProcess.class));
    }
    return supplyDataForPharmacistReviewDto;
  }

  @Override
  public MedicationSupplyTaskSimpleDto getSupplySimpleTask(final String taskId)
  {
    final TaskDto supplyTask = processService.loadTask(taskId, false);
    final List<MedicationSupplyTaskSimpleDto> medicationSupplyTaskDtos =
        buildSupplySimpleTasks(Collections.singletonList(supplyTask));
    return medicationSupplyTaskDtos.isEmpty() ? null : medicationSupplyTaskDtos.get(0);
  }

  @Override
  public List<PerfusionSyringePatientTasksDto> findPerfusionSyringeTasks(
      @Nonnull final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      final Interval searchInterval,
      @Nonnull final Set<TaskTypeEnum> taskTypes,
      final boolean closedTasksOnly,
      @Nonnull final DateTime when,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(patientIdAndPatientWithLocationMap, "patientIdAndPatientWithLocationMap is null");
    Preconditions.checkNotNull(taskTypes, "taskTypes is null");
    Preconditions.checkNotNull(when, "when is null");
    Preconditions.checkNotNull(locale, "locale is null");
    Preconditions.checkArgument(!taskTypes.isEmpty(), "taskTypes can not be empty");

    if (patientIdAndPatientWithLocationMap.isEmpty())
    {
      return Collections.emptyList();
    }

    final Set<String> patientIdsSet = Sets.newHashSet(patientIdAndPatientWithLocationMap.keySet());
    final List<String> patientIdKeysForTaskTypes = TherapyTaskUtils.getPatientIdKeysForTaskTypes(patientIdsSet, taskTypes);

    final DateTime taskDueAfter = searchInterval != null ? searchInterval.getStart() : null;
    final DateTime taskDueBefore = searchInterval != null ? searchInterval.getEnd() : null;

    final PartialList<TaskDto> tasksList = processService.findTasks(
        null,
        closedTasksOnly ? MedsTaskDef.GROUP_NAME : null, //loading optimization
        null,
        closedTasksOnly,
        taskDueAfter,
        taskDueBefore,
        patientIdKeysForTaskTypes,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    if (closedTasksOnly)    // findTasks loaded closed, opened and deleted tasks ... we want only closed tasks
    {
      removeOpenedAndDeletedTasksFromTaskList(tasksList);
    }

    final List<PerfusionSyringePatientTasksDto> perfusionSyringePatientTasksDtos = createPerfusionSyringePatientTasksList(
        tasksList,
        patientIdAndPatientWithLocationMap,
        when,
        locale);

    sortPerfusionSyringePatientTasksList(perfusionSyringePatientTasksDtos);
    return perfusionSyringePatientTasksDtos;
  }

  @Override
  public Map<String, PerfusionSyringePreparationDto> getOriginalTherapyIdAndPerfusionSyringePreparationDtoMap(
      final String patientId,
      final boolean isUrgent,
      final Set<String> originalTherapyIds,
      final DateTime when,
      final Locale locale)
  {
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE);
    final List<String> patientIdKeysForTaskTypes =
        TherapyTaskUtils.getPatientIdKeysForTaskTypes(Collections.singleton(patientId), taskTypes);

    final List<TaskDto> tasksList = getPerfusionSyringeTasksForOriginalTherapyIds(
        patientIdKeysForTaskTypes,
        originalTherapyIds,
        isUrgent);

    return buildPerfusionSyringePreparationDtosMap(tasksList, when, locale);
  }

  private List<TaskDto> getPerfusionSyringeTasksForOriginalTherapyIds(
      final List<String> patientIdKeysForTaskTypes,
      final Set<String> originalTherapyIds,
      final boolean isUrgent)
  {
    final PartialList<TaskDto> tasksList = processService.findTasks(
        null,
        null,
        null,
        false,
        null,
        null,
        patientIdKeysForTaskTypes,
        EnumSet.of(TaskDetailsEnum.VARIABLES),
        Pair.of(PerfusionSyringeTaskDef.IS_URGENT, isUrgent));

    final List<TaskDto> filteredTasksList = new ArrayList<>();

    for (final TaskDto task : tasksList)
    {
      final String originalTherapyId = (String)task.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
      if (originalTherapyIds.contains(originalTherapyId))
      {
        filteredTasksList.add(task);
      }
    }
    return filteredTasksList;
  }

  private Map<String, PerfusionSyringePreparationDto> buildPerfusionSyringePreparationDtosMap(
      final List<TaskDto> completePerfusionSyringeTasksList,
      final DateTime when,
      final Locale locale)
  {
    final Map<String, String> originalTherapyIdAndPatientIdMap =
        getOriginalTherapyIdAndPatientIdMap(completePerfusionSyringeTasksList);

    final Map<String, DateTime> originalTherapyIdAndPreparationStartedDateTimeMap =
        getOriginalTherapyIdAndPreparationStartedDateTimeMap(completePerfusionSyringeTasksList);

    final Map<String, TherapyDayDto> originalCompositionUidAndLatestTherapyDayDtoMap =
        overviewContentProvider.getOriginalCompositionUidAndLatestTherapyDayDtoMap(
            originalTherapyIdAndPatientIdMap,
            4,
            when,
            locale);

    final Set<String> patientIds = Sets.newHashSet(originalTherapyIdAndPatientIdMap.values());
    final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientDisplayDataMap =
        patientDataProvider.getPatientDisplayWithLocationMap(null, patientIds);

    final Map<String, PerfusionSyringeLabelDto> originalTherapyIdPerfusionSyringeLabelDtoMap =
        getOriginalTherapyIdPerfusionSyringeLabelDtoMap(
            originalCompositionUidAndLatestTherapyDayDtoMap,
            patientIdAndPatientDisplayDataMap,
            originalTherapyIdAndPreparationStartedDateTimeMap,
            originalTherapyIdAndPatientIdMap,
            locale);

    return getOriginalTherapyIdAndSyringePreparationDtoMap(
        completePerfusionSyringeTasksList,
        originalTherapyIdPerfusionSyringeLabelDtoMap);
  }

  private Map<String, PerfusionSyringeLabelDto> getTaskIdAndPerfusionSyringeLabelDtoMap(
      final Map<String, String> originalTherapyUidAndPerfusionSyringeTaskIdMap,
      final Map<String, TherapyDayDto> originalCompositionUidAndLatestTherapyDayDtoMap,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientDisplayDataMap,
      final Map<String, DateTime> originalTherapyIdAndPreparationStartedDateTimeMap,
      final Map<String, String> originalTherapyIdAndPatientIdMap,
      final Locale locale)
  {
    final Map<String, PerfusionSyringeLabelDto> taskIdAndPerfusionSyringeLabelDtoMap = new HashMap<>();
    final Map<String, PerfusionSyringeLabelDto> originalTherapyIdPerfusionSyringeLabelDtoMap =
        getOriginalTherapyIdPerfusionSyringeLabelDtoMap(
            originalCompositionUidAndLatestTherapyDayDtoMap,
            patientIdAndPatientDisplayDataMap,
            originalTherapyIdAndPreparationStartedDateTimeMap,
            originalTherapyIdAndPatientIdMap,
            locale);

    for (final String originalTherapyUid : originalTherapyIdPerfusionSyringeLabelDtoMap.keySet())
    {
      if (originalTherapyUidAndPerfusionSyringeTaskIdMap.containsKey(originalTherapyUid))
      {
        final String taskId = originalTherapyUidAndPerfusionSyringeTaskIdMap.get(originalTherapyUid);
        final PerfusionSyringeLabelDto taskDto = originalTherapyIdPerfusionSyringeLabelDtoMap.get(originalTherapyUid);
        taskIdAndPerfusionSyringeLabelDtoMap.put(taskId, taskDto);
      }
    }
    return taskIdAndPerfusionSyringeLabelDtoMap;
  }

  private Map<String, PerfusionSyringeLabelDto> getOriginalTherapyIdPerfusionSyringeLabelDtoMap(
      final Map<String, TherapyDayDto> originalCompositionUidAndLatestTherapyDayDtoMap,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientDisplayDataMap,
      final Map<String, DateTime> originalTherapyIdAndPreparationStartedDateTimeMap,
      final Map<String, String> originalTherapyIdAndPatientIdMap,
      final Locale locale)
  {
    final Map<String, PerfusionSyringeLabelDto> originalTherapyIdPerfusionSyringeLabelDtoMap = new HashMap<>();

    final String userName = RequestContextHolder.getContext().getUserMetadata().map(UserMetadata::getFullName).get();

    for (final String originalTherapyId : originalTherapyIdAndPatientIdMap.keySet())
    {
      final String patientId = originalTherapyIdAndPatientIdMap.get(originalTherapyId);
      final PatientDisplayWithLocationDto patientWithLocationDto = patientIdAndPatientDisplayDataMap.get(patientId);

      final PerfusionSyringeLabelDto perfusionSyringeLabelDto = new PerfusionSyringeLabelDto();
      final DateTime startedDateTime = originalTherapyIdAndPreparationStartedDateTimeMap.get(originalTherapyId);
      perfusionSyringeLabelDto.setPreparationStartedTime(
          startedDateTime != null ? startedDateTime.toString(DateTimeFormatters.shortDateTime(locale)) : null);
      perfusionSyringeLabelDto.setBarCode(TherapyIdUtils.createTherapyId(originalTherapyId));

      perfusionSyringeLabelDto.setPreparedBy(labelDisplayValuesProvider.getPreparedByString(userName, locale));
      perfusionSyringeLabelDto.setPatientCareProvider(patientWithLocationDto.getCareProviderName());
      final String roomAndBed = patientWithLocationDto.getRoomAndBed();
      perfusionSyringeLabelDto.setPatientRoomAndBed(roomAndBed != null ? roomAndBed : "/");
      final PatientDisplayDto patientDisplayDto = patientWithLocationDto.getPatientDisplayDto();
      final String patientBirthDate = patientDisplayDto.getBirthDate().toString(DateTimeFormatters.shortDate(locale));
      perfusionSyringeLabelDto.setPatientBirthDate(patientBirthDate);
      perfusionSyringeLabelDto.setPatientName(patientDisplayDto.getName());

      final TherapyDayDto therapyDayDto = originalCompositionUidAndLatestTherapyDayDtoMap.get(originalTherapyId);

      if (therapyDayDto != null)
      {
        perfusionSyringeLabelDto.setPrescribedBy(
            labelDisplayValuesProvider.getPrescribedByString(therapyDayDto.getTherapy().getComposerName(), locale));
        perfusionSyringeLabelDto.setTherapyDisplayValue(
            labelDisplayValuesProvider.getTherapyDisplayValueForPerfusionSyringeLabel(therapyDayDto.getTherapy(), locale));
      }
      originalTherapyIdPerfusionSyringeLabelDtoMap.put(originalTherapyId, perfusionSyringeLabelDto);
    }

    return originalTherapyIdPerfusionSyringeLabelDtoMap;
  }

  private Map<String, PerfusionSyringePreparationDto> getOriginalTherapyIdAndSyringePreparationDtoMap(
      final List<TaskDto> perfusionSyringeTasksList,
      final Map<String, PerfusionSyringeLabelDto> originalTherapyIdPerfusionSyringeLabelDtoMap)
  {
    final Map<String, PerfusionSyringePreparationDto> originalTherapyIdAndPreparationDtoMap = new HashMap<>();
    for (final TaskDto task : perfusionSyringeTasksList)
    {
      final String originalTherapyId = (String)task.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
      final PerfusionSyringePreparationDto perfusionSyringePreparationDto = new PerfusionSyringePreparationDto();
      perfusionSyringePreparationDto.setCompletePreparationTaskId(task.getId());
      perfusionSyringePreparationDto.setPerfusionSyringeLabelDto(
          originalTherapyIdPerfusionSyringeLabelDtoMap.get(TherapyIdUtils.parseTherapyId(originalTherapyId).getFirst()));
      originalTherapyIdAndPreparationDtoMap.put(originalTherapyId, perfusionSyringePreparationDto);
    }
    return originalTherapyIdAndPreparationDtoMap;
  }

  private Map<String, String> getOriginalTherapyIdAndPatientIdMap(final List<TaskDto> therapyTasksList)
  {
    final Map<String, String> originalTherapyIdAndPatientIdMap = new HashMap<>();

    for (final TaskDto taskDto : therapyTasksList)
    {
      final String originalTherapyId = (String)taskDto.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
      final String patientId = (String)taskDto.getVariables().get(TherapyTaskDef.PATIENT_ID.getName());
      originalTherapyIdAndPatientIdMap.put(TherapyIdUtils.parseTherapyId(originalTherapyId).getFirst(), patientId);
    }
    return originalTherapyIdAndPatientIdMap;
  }

  private Map<String, DateTime> getOriginalTherapyIdAndPreparationStartedDateTimeMap(final List<TaskDto> perfusionSyringeTasksList)
  {
    final Map<String, DateTime> originalTherapyIdAndPreparationStartedDateTimeMap = new HashMap<>();

    for (final TaskDto taskDto : perfusionSyringeTasksList)
    {
      final String originalTherapyId = (String)taskDto.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
      final Long preparationStartedMillis =
          (Long)taskDto.getVariables().get(PerfusionSyringeTaskDef.PREPARATION_STARTED_TIME_MILLIS.getName());
      if (preparationStartedMillis != null)
      {
        originalTherapyIdAndPreparationStartedDateTimeMap.put(
            TherapyIdUtils.parseTherapyId(originalTherapyId).getFirst(),
            new DateTime(preparationStartedMillis));
      }
    }
    return originalTherapyIdAndPreparationStartedDateTimeMap;
  }


  @Override
  public Map<String, String> getOriginalTherapyIdAndPerfusionSyringeTaskIdMap(final String patientId, final boolean isUrgent)
  {
    final Set<TaskTypeEnum> taskTypes = TaskTypeEnum.PERFUSION_SYRINGE_TASKS_SET;
    final List<String> patientIdKeysForTaskTypes =
        TherapyTaskUtils.getPatientIdKeysForTaskTypes(Collections.singleton(patientId), taskTypes);

    final PartialList<TaskDto> tasksList = processService.findTasks(
        null,
        null,
        null,
        false,
        null,
        null,
        patientIdKeysForTaskTypes,
        EnumSet.of(TaskDetailsEnum.VARIABLES),
        Pair.of(PerfusionSyringeTaskDef.IS_URGENT, isUrgent));

    final Map<String, String> originalTherapyIdAndPerfusionSyringeTaskIdMap = new HashMap<>();

    for (final TaskDto task : tasksList)
    {
      final String originalTherapyId = (String)task.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
      originalTherapyIdAndPerfusionSyringeTaskIdMap.put(originalTherapyId, task.getId());
    }

    return originalTherapyIdAndPerfusionSyringeTaskIdMap;
  }

  private void sortPerfusionSyringePatientTasksList(
      final List<PerfusionSyringePatientTasksDto> perfusionSyringePatientTasksDtos)
  {
    //then sort PerfusionSyringePatientTasksDtos
    Collections.sort(
        perfusionSyringePatientTasksDtos, (task1, task2) -> {
          if (task1.isUrgent() && !task2.isUrgent())
          {
            return -1;
          }
          if (!task1.isUrgent() && task2.isUrgent())
          {
            return 1;
          }

          final DateTime dueTime1 = task1.getTasksList().get(0).getDueTime();
          final DateTime dueTime2 = task2.getTasksList().get(0).getDueTime();
          final int compareByTime = dueTime1.compareTo(dueTime2);
          if (compareByTime == 0)
          {
            final int compareByName =
                task1.getPatientDisplayDto().getName().compareTo(task2.getPatientDisplayDto().getName());
            if (compareByName == 0)
            {
              return task1.getPatientDisplayDto().getId().compareTo(task2.getPatientDisplayDto().getId());
            }
            return compareByName;
          }
          return compareByTime;
        });

    //first sort tasksLists inside PerfusionSyringePatientTasksDtos
    sortPerfusionSyringeTaskLists(perfusionSyringePatientTasksDtos);
  }

  private void sortPerfusionSyringeTaskLists(final List<PerfusionSyringePatientTasksDto> perfusionSyringePatientTasksDtos)
  {
    for (final PerfusionSyringePatientTasksDto perfusionSyringePatientTasksDto : perfusionSyringePatientTasksDtos)
    {
      Collections.sort(
          perfusionSyringePatientTasksDto.getTasksList(), (task1, task2) -> {
            final int compareByTime = task1.getDueTime().compareTo(task2.getDueTime());
            if (compareByTime == 0)
            {
              return task1.getId().compareTo(task2.getId());
            }
            return compareByTime;
          });
    }
  }

  @Override
  public PerfusionSyringeTaskSimpleDto getPerfusionSyringeTaskSimpleDto(final String taskId, final Locale locale)
  {
    final TaskDto task = processService.loadTask(taskId, false);
    if (task == null)
    {
      throw new UserWarning(Dictionary.getEntry("data.changed.please.reload", locale));
    }
    return buildPerfusionSyringeTaskSimple(task);
  }

  @Override
  public boolean therapyHasTasksClosedInInterval(
      @Nonnull final String patientId,
      @Nonnull final String originalTherapyId,
      @Nonnull final Set<TaskTypeEnum> taskTypesSet,
      @Nonnull final Interval interval)
  {
    Preconditions.checkNotNull(patientId, "patientId is null");
    Preconditions.checkNotNull(originalTherapyId, "originalTherapyId is null");
    Preconditions.checkNotNull(taskTypesSet, "taskTypesSet is null");
    Preconditions.checkNotNull(interval, "interval is null");

    final List<String> patientIdKeysForTaskTypes =
        TherapyTaskUtils.getPatientIdKeysForTaskTypes(Collections.singleton(patientId), taskTypesSet);

    final PartialList<TaskDto> tasks = processService.findTasks(
        null,
        null,
        null,
        true,
        null,
        null,
        patientIdKeysForTaskTypes,
        EnumSet.noneOf(TaskDetailsEnum.class),
        Pair.of(TherapyTaskDef.ORIGINAL_THERAPY_ID, originalTherapyId));

    for (final TaskDto task : tasks)
    {
      if (task.isCompleted() && interval.contains(task.getEndTime()))
      {
        return true;
      }
    }

    return false;
  }

  @Override
  public DateTime getLastEditTimestampForPharmacistReview(final String patientId)
  {
    final List<String> patientIdKeysForTaskTypes = TherapyTaskUtils.getPatientIdKeysForTaskTypes(
        Collections.singleton(patientId),
        EnumSet.of(TaskTypeEnum.PHARMACIST_REVIEW));

    final List<TaskDto> tasks = processService.findTasks(
        TherapyAssigneeEnum.PHARMACIST.name(),
        null,
        null,
        false,
        null,
        null,
        patientIdKeysForTaskTypes,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    DateTime lastEditTimestamp = null;

    for (final TaskDto task : tasks)
    {
      final DateTime editTimestamp =
          new DateTime(task.getVariables().get(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS.getName()));

      if (lastEditTimestamp == null || lastEditTimestamp.isBefore(editTimestamp))
      {
        lastEditTimestamp = editTimestamp;
      }
    }

    return lastEditTimestamp;
  }

  @Override
  public List<TaskDto> getTherapyTasks(
      final TaskTypeEnum taskTypeEnum,
      final String patientId,
      final String originalTherapyId)
  {
    final List<String> patientIdKeysForTaskTypes =
        TherapyTaskUtils.getPatientIdKeysForTaskTypes(Collections.singleton(patientId), Collections.singleton(taskTypeEnum));

    return processService.findTasks(
        null,
        null,
        null,
        false,
        null,
        null,
        patientIdKeysForTaskTypes,
        EnumSet.of(TaskDetailsEnum.VARIABLES),
        Pair.of(TherapyTaskDef.ORIGINAL_THERAPY_ID, originalTherapyId));
  }

  private PerfusionSyringeTaskSimpleDto buildPerfusionSyringeTaskSimple(final TaskDto task)
  {
    if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_START.getName())
        || task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE.getName())
        || task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE.getName()))
    {
      final PerfusionSyringeTaskSimpleDto taskSimpleDto = new PerfusionSyringeTaskSimpleDto();

      taskSimpleDto.setNumberOfSyringes(
          (Integer)task.getVariables().get(PerfusionSyringeTaskDef.NUMBER_OF_SYRINGES.getName()));
      taskSimpleDto.setUrgent
          ((Boolean)task.getVariables().get(PerfusionSyringeTaskDef.IS_URGENT.getName()));
      final Boolean printSystemLabel = (Boolean)task.getVariables()
          .get(PerfusionSyringeTaskDef.PRINT_SYSTEM_LABEL.getName());
      taskSimpleDto.setPrintSystemLabel(Opt.of(printSystemLabel).orElse(false));
      taskSimpleDto.setDueTime(task.getDueTime());
      taskSimpleDto.setId(task.getId());
      return taskSimpleDto;
    }
    else
    {
      throw new IllegalArgumentException("Only perfusion syringe preparation tasks expected here. " + task.getTaskExecutionStrategyId() + " not allowed!");
    }
  }

  private List<PerfusionSyringePatientTasksDto> createPerfusionSyringePatientTasksList(
      final PartialList<TaskDto> tasksList,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      final DateTime when,
      final Locale locale)
  {
    final Map<String, PerfusionSyringeTaskDto> taskIdAndPerfusionSyringeTaskDtoMap = new HashMap<>();
    final Map<String, String> originalTherapyIdAndPatientIdMap = new HashMap<>();
    final Map<String, DateTime> originalTherapyIdAndPreparationStartedDateTimeMap = new HashMap<>();
    final Map<Pair<String, Boolean>, List<String>> patientWithUrgencyAndTaskIdsMap = new HashMap<>();
    final Map<String, String> originalTherapyUidAndPerfusionSyringeTaskIdMap = new HashMap<>();

    for (final TaskDto task : tasksList)
    {
      if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_START.getName())
          || task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE.getName())
          || task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE.getName()))
      {
        final PerfusionSyringeTaskDto perfusionSyringeTaskDto = new PerfusionSyringeTaskDto();

        final String originalTherapyId =
            (String)task.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
        final String patientId = (String)task.getVariables().get(MedsTaskDef.PATIENT_ID.getName());

        perfusionSyringeTaskDto.setId(task.getId());
        perfusionSyringeTaskDto.setDueTime(task.getDueTime());
        perfusionSyringeTaskDto.setTaskType(TaskTypeEnum.getByName(task.getTaskExecutionStrategyId()));
        perfusionSyringeTaskDto.setNumberOfSyringes(
            (Integer)task.getVariables().get(PerfusionSyringeTaskDef.NUMBER_OF_SYRINGES.getName()));
        perfusionSyringeTaskDto.setOriginalTherapyId(originalTherapyId);

        final Boolean printSystemLabel =
            (Boolean)task.getVariables().get(PerfusionSyringeTaskDef.PRINT_SYSTEM_LABEL.getName());
        perfusionSyringeTaskDto.setPrintSystemLabel(Opt.of(printSystemLabel).orElse(false));

        final String ordererId = (String)task.getVariables().get(PerfusionSyringeTaskDef.ORDERER.getName());
        if (ordererId != null)
        {
          final String ordererFullName = (String)task.getVariables().get(PerfusionSyringeTaskDef.ORDERER_FULL_NAME.getName());
          perfusionSyringeTaskDto.setOrderedBy(new NamedExternalDto(ordererId, ordererFullName));
        }

        final String originalTherapyUid = TherapyIdUtils.parseTherapyId(originalTherapyId).getFirst();
        originalTherapyIdAndPatientIdMap.put(originalTherapyUid, patientId);
        originalTherapyUidAndPerfusionSyringeTaskIdMap.put(originalTherapyUid, task.getId());

        taskIdAndPerfusionSyringeTaskDtoMap.put(task.getId(), perfusionSyringeTaskDto);

        final Long preparationStartedMillis =
            (Long)task.getVariables().get(PerfusionSyringeTaskDef.PREPARATION_STARTED_TIME_MILLIS.getName());
        if (preparationStartedMillis != null)
        {
          originalTherapyIdAndPreparationStartedDateTimeMap.put(originalTherapyUid, new DateTime(preparationStartedMillis));
        }

        final Boolean urgencyObject = (Boolean)task.getVariables().get(PerfusionSyringeTaskDef.IS_URGENT.getName());
        final boolean isUrgent = urgencyObject != null && urgencyObject;

        if (patientWithUrgencyAndTaskIdsMap.containsKey(Pair.of(patientId, isUrgent)))
        {
          patientWithUrgencyAndTaskIdsMap.get(Pair.of(patientId, isUrgent)).add(task.getId());
        }
        else
        {
          final List<String> taskList = new ArrayList<>();
          taskList.add(task.getId());
          patientWithUrgencyAndTaskIdsMap.put(Pair.of(patientId, isUrgent), taskList);
        }
      }
      else
      {
        throw new IllegalArgumentException(
            "Only perfusion syringe preparation tasks expected here. " + task.getTaskExecutionStrategyId() + " not allowed!");
      }
    }

    final Map<String, TherapyDayDto> originalCompositionUidAndLatestTherapyDayDtoMap =
        overviewContentProvider.getOriginalCompositionUidAndLatestTherapyDayDtoMap(
            originalTherapyIdAndPatientIdMap,
            16,
            when,
            locale);

    final Map<String, TherapyDayDto> taskIdAndTherapyDayDtoMap = getTherapyDataForPerfusionSyringeTasks(
        originalCompositionUidAndLatestTherapyDayDtoMap,
        originalTherapyUidAndPerfusionSyringeTaskIdMap);

    final Map<String, PerfusionSyringeLabelDto> taskIdPerfusionSyringeLabelDtoMap =
        getTaskIdAndPerfusionSyringeLabelDtoMap(
            originalTherapyUidAndPerfusionSyringeTaskIdMap,
            originalCompositionUidAndLatestTherapyDayDtoMap,
            patientIdAndPatientWithLocationMap,
            originalTherapyIdAndPreparationStartedDateTimeMap,
            originalTherapyIdAndPatientIdMap,
            locale);

    return buildPerfusionSyringePatientTasks(
        taskIdAndPerfusionSyringeTaskDtoMap,
        taskIdAndTherapyDayDtoMap,
        taskIdPerfusionSyringeLabelDtoMap,
        patientWithUrgencyAndTaskIdsMap,
        patientIdAndPatientWithLocationMap,
        Sets.newHashSet(originalTherapyIdAndPatientIdMap.values()));
  }

  private List<PerfusionSyringePatientTasksDto> buildPerfusionSyringePatientTasks(
      final Map<String, PerfusionSyringeTaskDto> taskIdAndPerfusionSyringeTaskDtoMap,
      final Map<String, TherapyDayDto> taskIdAndTherapyDayDtoMap,
      final Map<String, PerfusionSyringeLabelDto> taskIdPerfusionSyringeLabelDtoMap,
      final Map<Pair<String, Boolean>, List<String>> patientWithUrgencyAndTaskIdsMap,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      final Set<String> patientIds)
  {
    final List<PerfusionSyringePatientTasksDto> perfusionSyringePatientTasksList = new ArrayList<>();
    for (final String patientId : patientIds)
    {
      final Pair<String, Boolean> patientUrgentKey = Pair.of(patientId, Boolean.TRUE);
      final Pair<String, Boolean> patientNormalKey = Pair.of(patientId, Boolean.FALSE);

      final PatientDisplayDto patientDisplayDto =
          patientIdAndPatientWithLocationMap.containsKey(patientId)
          ? patientIdAndPatientWithLocationMap.get(patientId).getPatientDisplayDto()
          : null;
      if (patientWithUrgencyAndTaskIdsMap.containsKey(patientUrgentKey))
      {
        final List<PerfusionSyringeTaskDto> perfusionSyringeTaskDtos = fillTasksTherapiesAndCreateDtoList(
            taskIdAndPerfusionSyringeTaskDtoMap,
            taskIdAndTherapyDayDtoMap,
            patientWithUrgencyAndTaskIdsMap.get(patientUrgentKey));

        fillTaskLabels(perfusionSyringeTaskDtos, taskIdPerfusionSyringeLabelDtoMap);
        perfusionSyringePatientTasksList.add(
            createPerfusionSyringePatientTaskDto(
                patientDisplayDto,
                perfusionSyringeTaskDtos,
                true));
      }
      if (patientWithUrgencyAndTaskIdsMap.containsKey(patientNormalKey))
      {
        final List<PerfusionSyringeTaskDto> perfusionSyringeTaskDtos = fillTasksTherapiesAndCreateDtoList(
            taskIdAndPerfusionSyringeTaskDtoMap,
            taskIdAndTherapyDayDtoMap,
            patientWithUrgencyAndTaskIdsMap.get(patientNormalKey));
        fillTaskLabels(perfusionSyringeTaskDtos, taskIdPerfusionSyringeLabelDtoMap);
        perfusionSyringePatientTasksList.add(
            createPerfusionSyringePatientTaskDto(
                patientDisplayDto,
                perfusionSyringeTaskDtos,
                false));
      }
    }

    return perfusionSyringePatientTasksList;
  }

  private void fillTaskLabels(
      final List<PerfusionSyringeTaskDto> perfusionSyringeTaskDtos,
      final Map<String, PerfusionSyringeLabelDto> taskIdPerfusionSyringeLabelDtoMap)
  {
    for (final PerfusionSyringeTaskDto perfusionSyringeTaskDto : perfusionSyringeTaskDtos)
    {
      if (perfusionSyringeTaskDto.getTaskType() == TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE
          || perfusionSyringeTaskDto.getTaskType() == TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE)
      {
        final PerfusionSyringeLabelDto perfusionSyringeLabelDto =
            taskIdPerfusionSyringeLabelDtoMap.get(perfusionSyringeTaskDto.getId());
        perfusionSyringeTaskDto.setPerfusionSyringeLabelDto(perfusionSyringeLabelDto);
      }
    }
  }

  private List<PerfusionSyringeTaskDto> fillTasksTherapiesAndCreateDtoList(
      final Map<String, PerfusionSyringeTaskDto> taskIdAndPerfusionSyringeTaskDtoMap,
      final Map<String, TherapyDayDto> taskIdAndTherapyDayDtoMap,
      final List<String> perfusionSyringeTaskIdsList)
  {
    final List<PerfusionSyringeTaskDto> tasksList = new ArrayList<>();
    for (final String taskId : perfusionSyringeTaskIdsList)
    {
      final PerfusionSyringeTaskDto perfusionSyringeTaskDto = taskIdAndPerfusionSyringeTaskDtoMap.get(taskId);
      perfusionSyringeTaskDto.setTherapyDayDto(taskIdAndTherapyDayDtoMap.get(taskId));
      tasksList.add(perfusionSyringeTaskDto);
    }
    return tasksList;
  }

  private PerfusionSyringePatientTasksDto createPerfusionSyringePatientTaskDto(
      final PatientDisplayDto patientDisplayDto,
      final List<PerfusionSyringeTaskDto> perfusionSyringeTaskDtos,
      final boolean urgent)
  {
    final PerfusionSyringePatientTasksDto urgentTaskDto = new PerfusionSyringePatientTasksDto();
    urgentTaskDto.setUrgent(urgent);
    urgentTaskDto.setPatientDisplayDto(patientDisplayDto);
    urgentTaskDto.setTasksList(perfusionSyringeTaskDtos);
    return urgentTaskDto;
  }

  private Map<String, TherapyDayDto> getTherapyDataForPerfusionSyringeTasks(
      final Map<String, TherapyDayDto> originalCompositionUidAndLatestTherapyDayDtoMap,
      final Map<String, String> originalTherapyUidAndPerfusionSyringeTaskIdMap)
  {
    final Map<String, TherapyDayDto> taskIdAndTherapyDayDtoMap = new HashMap<>();

    for (final Map.Entry<String, TherapyDayDto> entry : originalCompositionUidAndLatestTherapyDayDtoMap.entrySet())
    {
      final String originalTherapyUidWithoutVersion = entry.getKey();
      final TherapyDayDto therapyDayDto = entry.getValue();

      final String perfusionSyringeTaskId =
          originalTherapyUidAndPerfusionSyringeTaskIdMap.get(originalTherapyUidWithoutVersion);
      if (perfusionSyringeTaskId != null)
      {
        taskIdAndTherapyDayDtoMap.put(perfusionSyringeTaskId, therapyDayDto);
      }
    }
    return taskIdAndTherapyDayDtoMap;
  }
}
