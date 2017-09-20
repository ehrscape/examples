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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.marand.ispek.bpm.service.BpmService;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.bpm.medications.process.PharmacySupplyProcess;
import com.marand.thinkmed.bpm.medications.process.PreparePerfusionSyringeProcess;
import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.task.DispenseMedicationTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskCreator;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskHandler;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.task.DispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeTaskDef;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import com.marand.thinkmed.medications.task.SupplyReviewTaskDef;
import com.marand.thinkmed.medications.task.SupplyTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.process.TaskCompletedType;
import com.marand.thinkmed.process.dto.AbstractTaskDto;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Klavdij Lapajne
 */

public class PharmacistTaskHandlerImpl implements PharmacistTaskHandler
{
  private ProcessService processService;
  private BpmService bpmService;
  private PharmacistTaskProvider pharmacistTaskProvider;
  private PharmacistTaskCreator pharmacistTaskCreator;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsTasksProvider medicationsTasksProvider;

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Required
  public void setPharmacistTaskProvider(final PharmacistTaskProvider pharmacistTaskProvider)
  {
    this.pharmacistTaskProvider = pharmacistTaskProvider;
  }

  @Required
  public void setBpmService(final BpmService bpmService)
  {
    this.bpmService = bpmService;
  }

  @Required
  public void setPharmacistTaskCreator(final PharmacistTaskCreator pharmacistTaskCreator)
  {
    this.pharmacistTaskCreator = pharmacistTaskCreator;
  }

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Override
  public void deleteSupplyTask(final List<String> taskIds, final String userId)
  {
    for (final String taskId : taskIds)
    {
      //variables must not exist on the task, so they get set on the process
      final Map<String, Object> variables = new HashMap<>();
      variables.put(PharmacySupplyProcess.confirmResupply.name(), false);
      variables.put(PharmacySupplyProcess.createResupplyReminder.name(), false);
      processService.setVariables(taskId, variables);
      deleteMedicationTaskIfPartOfProcess(taskId, userId, TaskCompletedType.DELETED.getBpmName());
    }
  }

  @Override
  public void dismissSupplyTask(final List<String> taskIds, final String userId)
  {
    for (final String taskId : taskIds)
    {
      final Map<String, Object> variables = new HashMap<>();
      variables.put(SupplyReminderTaskDef.IS_DISMISSED.getName(), true);
      processService.setVariablesLocal(taskId, variables);
    }
  }

  @Override
  public void deleteNurseSupplyTask(final String patientId, final String taskId, final String userId)
  {
    final TaskDto taskDto = processService.loadTask(taskId, false);
    final String originalTherapyId = (String)taskDto.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
    final List<TaskDto> nurseSupplyTasks = pharmacistTaskProvider.findNurseSupplyTasksForTherapy(
        patientId,
        originalTherapyId);

    boolean isSupplyDispensed = true;
    final List<String> taskIds = new ArrayList<>();
    for (final TaskDto nurseSupplyTask : nurseSupplyTasks)
    {
      taskIds.add(nurseSupplyTask.getId());
      if (nurseSupplyTask.getTaskExecutionStrategyId().equals(TaskTypeEnum.DISPENSE_MEDICATION.getName()))
      {
        isSupplyDispensed = false; //dispense task still not closed
      }
    }

    if (isSupplyDispensed)
    {
      throw new IllegalStateException("Can't delete task. Requested supply already dispensed!");
    }
    else
    {
      deleteSupplyTask(taskIds, userId);
    }
  }

  @Override
  public void confirmSupplyReminderTask(
      final String taskId,
      final String therapyIdAtConfirmation,
      final MedicationSupplyTypeEnum supplyTypeEnum,
      final Integer supplyInDays,
      final String userId,
      final String comment)
  {
    final Map<String, Object> variables = new HashMap<>();
    variables.put(PharmacySupplyProcess.supplyType.name(), supplyTypeEnum.name());
    variables.put(PharmacySupplyProcess.supplyInDays.name(), supplyInDays);
    variables.put(PharmacySupplyProcess.createResupplyReminder.name(), true);
    variables.put(PharmacySupplyProcess.confirmResupply.name(), true);
    variables.put(PharmacySupplyProcess.dispenseMedication.name(), supplyTypeEnum != MedicationSupplyTypeEnum.PATIENTS_OWN);
    final TaskDto taskDto = processService.loadTaskWithCustomDetails(taskId, false, EnumSet.of(TaskDetailsEnum.PROCESS_INFO));
    bpmService.setProcessVariables(taskDto.getProcessBusinessKey(), PharmacySupplyProcess.class, variables);

    final Map<String, Object> localVariables = new HashMap<>();
    localVariables.put(SupplyTaskDef.SUPPLY_REQUEST_COMMENT.getName(), comment);
    localVariables.put(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName(), therapyIdAtConfirmation);
    setPerformerAndCompleteMedicationTask(taskId, userId, localVariables);
  }

  @Override
  public void editSupplyReminderTask(
      final String taskId,
      final MedicationSupplyTypeEnum supplyTypeEnum,
      final Integer supplyInDays,
      final String comment,
      final DateTime when)
  {
    final Map<String, Object> localVariables = new HashMap<>();
    localVariables.put(SupplyReminderTaskDef.SUPPLY_REQUEST_COMMENT.getName(), comment);
    localVariables.put(SupplyReminderTaskDef.DAYS_SUPPLY.getName(), supplyInDays);
    localVariables.put(SupplyReminderTaskDef.SUPPLY_TYPE.getName(), supplyTypeEnum.name());
    localVariables.put(SupplyReminderTaskDef.IS_DISMISSED.getName(), false);
    processService.setVariablesLocal(taskId, localVariables);
    processService.setDueDate(taskId, when.plusDays(supplyInDays));
  }

  @Override
  public void confirmSupplyReviewTask(
      final String patientId,
      final String taskId,
      final String therapyIdAtConfirmation,
      final boolean alreadyDispensed,
      final boolean updateSupplyRequestData,
      @Nullable final MedicationSupplyTypeEnum supplyTypeEnum,
      @Nullable final Integer supplyInDays,
      @Nullable final String comment,
      final String userId,
      final DateTime when)
  {
    if (updateSupplyRequestData)
    {
      Preconditions.checkArgument(!alreadyDispensed, "Trying to update already dispensed tasks!");
      Preconditions.checkNotNull(supplyTypeEnum, "Trying to update supply tasks with supplyTypeEnum == null");
      Preconditions.checkNotNull(supplyInDays, "Trying to update supply tasks with supplyInDays == null");
    }
    final Set<TaskTypeEnum> supplyTaskTypes = EnumSet.of(
        TaskTypeEnum.SUPPLY_REMINDER,
        TaskTypeEnum.SUPPLY_REVIEW,
        TaskTypeEnum.DISPENSE_MEDICATION);

    final TaskDto taskDto = processService.loadTask(taskId, false);

    final String originalTherapyId = (String)taskDto.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
    final List<MedicationSupplyTaskSimpleDto> tasks =
        pharmacistTaskProvider.findSupplySimpleTasksForTherapy(
            null,
            Collections.singleton(patientId),
            supplyTaskTypes,
            originalTherapyId);

    if (!alreadyDispensed)
    {
      // if not already dispensed set data on dispense medication task
      final String dispenseTaskId = getTaskIdForSimpleTaskOfType(
          TaskTypeEnum.DISPENSE_MEDICATION,
          TherapyAssigneeEnum.NURSE,
          tasks);
      if (dispenseTaskId == null)
      {
        throw new IllegalStateException("Related dispense task is not in active state!"); // if !alreadyDispensed, dispense task must be found here
      }
      final Map<String, Object> variables = new HashMap<>();
      variables.put(DispenseMedicationTaskDef.REQUEST_STATUS.getName(), SupplyRequestStatus.VERIFIED.name());
      if (updateSupplyRequestData)
      {
        variables.put(DispenseMedicationTaskDef.SUPPLY_TYPE.getName(), supplyTypeEnum.name());
        variables.put(DispenseMedicationTaskDef.DAYS_SUPPLY.getName(), supplyInDays);
        if (comment != null && !comment.isEmpty())
        {
          variables.put(DispenseMedicationTaskDef.SUPPLY_REQUEST_COMMENT.getName(), comment);
        }
      }
      processService.setVariablesLocal(dispenseTaskId, variables);
    }
    if (updateSupplyRequestData)
    {
      final String reminderTaskId = getTaskIdForSimpleTaskOfType(
          TaskTypeEnum.SUPPLY_REMINDER,
          null,
          tasks);

      // if reminder task exists set data on the task ... don't create a new one
      // if it doesn't, set data on process and tell it to create a new task
      if (reminderTaskId != null)
      {
        final Map<String, Object> processVariables = new HashMap<>();
        processVariables.put(PharmacySupplyProcess.createResupplyReminder.name(), false);
        processVariables.put(PharmacySupplyProcess.requesterRole.name(), TherapyAssigneeEnum.PHARMACIST.name());
        bpmService.setProcessVariables(taskDto.getProcessBusinessKey(), PharmacySupplyProcess.class, processVariables);
        editSupplyReminderTask(reminderTaskId, supplyTypeEnum, supplyInDays, comment, when);
      }
      else
      {
        final Map<String, Object> processVariables = new HashMap<>();
        processVariables.put(PharmacySupplyProcess.createResupplyReminder.name(), true);
        processVariables.put(PharmacySupplyProcess.requesterRole.name(), TherapyAssigneeEnum.PHARMACIST.name());
        processVariables.put(PharmacySupplyProcess.supplyType.name(), supplyTypeEnum.name());
        processVariables.put(PharmacySupplyProcess.supplyInDays.name(), supplyInDays);
        processVariables.put(PharmacySupplyProcess.supplyRequestComment.name(), comment);
        bpmService.setProcessVariables(taskDto.getProcessBusinessKey(), PharmacySupplyProcess.class, processVariables);
      }
    }

    final Map<String, Object> localVariables = new HashMap<>();
    localVariables.put(SupplyTaskDef.SUPPLY_REQUEST_COMMENT.getName(), comment);
    localVariables.put(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName(), therapyIdAtConfirmation);
    setPerformerAndCompleteMedicationTask(taskId, userId, localVariables);
  }

  @Override
  public void handleReviewTaskOnTherapiesChange(
      @Nonnull final String patientId,
      final DateTime hospitalizationStart,
      @Nonnull final DateTime when,
      final String lastEditorName,
      final DateTime lastEditTime,
      final PrescriptionChangeTypeEnum changeType,
      @Nonnull final PharmacistReviewTaskStatusEnum status)
  {
    Preconditions.checkNotNull(patientId, "patientId");
    Preconditions.checkNotNull(when, "when");
    Preconditions.checkNotNull(status, "status");

    final DateTime dueTime = Opt.of(medicationsTasksProvider.getNextAdministrationTask(patientId, when))
        .map(AbstractTaskDto::getDueTime)
        .orElse(null);

    final Opt<String> taskId = pharmacistTaskProvider.findPharmacistReviewTaskId(patientId);
    if (taskId.isPresent())
    {
      pharmacistTaskCreator.updatePharmacistReviewTask(taskId.get(), dueTime, lastEditorName, lastEditTime);
    }
    else
    {
      pharmacistTaskCreator.createPharmacistReviewTask(
          patientId,
          dueTime,
          lastEditorName,
          lastEditTime,
          Opt.of(changeType).orElseGet(() -> getPharmacistTherapyChangeType(patientId, hospitalizationStart, when)),
          status);
    }
  }

  private PrescriptionChangeTypeEnum getPharmacistTherapyChangeType(
      final String patientId,
      final DateTime hospitalizationStart,
      final DateTime when)
  {
    if (hospitalizationStart == null)
    {
      return PrescriptionChangeTypeEnum.NEW_ADMISSION_PRESCRIPTION;
    }

    return medicationsOpenEhrDao.findMedicationInstructions(
        patientId,
        new Interval(hospitalizationStart, Days.THREE),
        null)
        .stream()
        .filter(Objects::nonNull)
        .findFirst()
        .map(Pair::getFirst)
        .map(c -> new Interval(DataValueUtils.getDateTime(c.getCompositionEventContext().getStartTime()), Hours.ONE))
        .map(interval -> interval.contains(when)
                         ? PrescriptionChangeTypeEnum.NEW_ADMISSION_PRESCRIPTION
                         : PrescriptionChangeTypeEnum.ADDITION_TO_EXISTING_PRESCRIPTION)
        .orElse(PrescriptionChangeTypeEnum.NEW_ADMISSION_PRESCRIPTION);
  }

  @Override
  public void confirmPharmacistDispenseTask(
      final String patientId,
      final String taskId,
      final String therapyIdAtConfirmation,
      final TherapyAssigneeEnum requesterRole,
      final SupplyRequestStatus supplyRequestStatus,
      final String userId)
  {
    if (requesterRole == TherapyAssigneeEnum.NURSE && supplyRequestStatus == SupplyRequestStatus.UNVERIFIED)
    {
      final TaskDto taskDto = processService.loadTask(taskId, false);
      final String originalTherapyId = (String)taskDto.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());

      final List<MedicationSupplyTaskSimpleDto> tasks =
          pharmacistTaskProvider.findSupplySimpleTasksForTherapy(
              null,
              Collections.singleton(patientId),
              EnumSet.of(TaskTypeEnum.SUPPLY_REVIEW),
              originalTherapyId);

      //we should get one or zero tasks
      final String supplyReviewTaskId = tasks.isEmpty() ? null : tasks.get(0).getTaskId();
      if (supplyReviewTaskId == null)
      {
        throw new IllegalStateException("Related review task is not in active state!"); // if UNVERIFIED, review task must be found here
      }
      processService.setVariableLocal(supplyReviewTaskId, SupplyReviewTaskDef.ALREADY_DISPENSED.getName(), true);
    }

    final Map<String, Object> localVariables = new HashMap<>();
    localVariables.put(SupplyTaskDef.TASK_CLOSED_WITH_THERAPY_ID.getName(), therapyIdAtConfirmation);
    setPerformerAndCompleteMedicationTask(taskId, userId, localVariables);
  }

  @Override
  public void deletePerfusionSyringeTask(final String taskId, final String userId)
  {
    final Map<String, Object> variables = new HashMap<>();
    variables.put(PreparePerfusionSyringeProcess.cancelPreparation.name(), true);
    processService.setVariables(taskId, variables);
    deleteMedicationTaskIfPartOfProcess(taskId, userId, TaskCompletedType.DELETED.getBpmName());
  }

  @Override
  public void undoPerfusionSyringeTask(final String taskId, final String userId)
  {
    final Map<String, Object> variables = new HashMap<>();
    variables.put(PreparePerfusionSyringeProcess.undoState.name(), true);
    processService.setVariables(taskId, variables);
    deleteMedicationTaskIfPartOfProcess(taskId, userId, TaskCompletedType.DELETED.getBpmName()); //TODO PERFUSION - ADD UNDO AS REASON
  }

  @Override
  public void confirmPerfusionSyringeTasks(final List<String> taskIds, final String userId)
  {
    for (final String taskId : taskIds)
    {
      setPerformerAndCompleteMedicationTask(taskId, userId, null);
    }
  }

  @Override
  public void setDispenseTaskPrintedTimestamp(final String taskId, final DateTime requestTimestamp)
  {
    final Map<String, Object> variables = new HashMap<>();
    variables.put(DispenseMedicationTaskDef.LAST_PRINTED_TIMESTAMP.getName(), requestTimestamp);
    processService.setVariablesLocal(taskId, variables);
  }

  @Override
  public void editPerfusionSyringeTask(
      @Nonnull final String taskId,
      @Nonnull final Integer numberOfSyringes,
      final boolean isUrgent,
      @Nonnull final DateTime dueDate,
      final boolean printSystemLabel)
  {
    StringUtils.checkNotBlank(taskId, "taskId is required");
    Preconditions.checkNotNull(numberOfSyringes, "numberOfSyringes is required");
    Preconditions.checkNotNull(dueDate, "dueDate is required");

    final Map<String, Object> processVariables = new HashMap<>();
    processVariables.put(PreparePerfusionSyringeProcess.numberOfSyringes.name(), numberOfSyringes);
    processVariables.put(PreparePerfusionSyringeProcess.isUrgent.name(), isUrgent);
    processVariables.put(PreparePerfusionSyringeProcess.dueDateTimeMillis.name(), dueDate.getMillis());
    processVariables.put(PreparePerfusionSyringeProcess.printSystemLabel.name(), printSystemLabel);
    final Map<String, Object> localVariables = new HashMap<>();
    localVariables.put(PerfusionSyringeTaskDef.NUMBER_OF_SYRINGES.getName(), numberOfSyringes);
    localVariables.put(PerfusionSyringeTaskDef.IS_URGENT.getName(), isUrgent);
    localVariables.put(PerfusionSyringeTaskDef.PRINT_SYSTEM_LABEL.getName(), printSystemLabel);

    final TaskDto taskDto = processService.loadTaskWithCustomDetails(taskId, false, EnumSet.of(TaskDetailsEnum.PROCESS_INFO));
    bpmService.setProcessVariables(
        taskDto.getProcessBusinessKey(),
        PreparePerfusionSyringeProcess.class,
        processVariables);
    processService.setVariablesLocal(taskId, localVariables);
    processService.setDueDate(taskId, dueDate);
  }

  @Override
  public void confirmPerfusionSyringeTasksForTherapy(
      final String patientId,
      final String userId,
      final TaskTypeEnum taskTypeEnum,
      final String originalTherapyId)
  {
    final List<TaskDto> therapyTasks = pharmacistTaskProvider.getTherapyTasks(taskTypeEnum, patientId, originalTherapyId);

    final List<String> taskIds = new ArrayList<>();
    for (final TaskDto task : therapyTasks)
    {
      taskIds.add(task.getId());
    }

    confirmPerfusionSyringeTasks(taskIds, userId);
  }

  private void deleteMedicationTaskIfPartOfProcess(
      final String taskId,
      final String userId,
      final String deleteReason)
  {
    //we can't delete tasks that are part of a running process ... we can close them and set delete reason by hand
    setPerformerAndCompleteMedicationTask(taskId, userId, null);
    processService.setTaskDeleteReason(taskId, deleteReason);
  }

  private void setPerformerAndCompleteMedicationTask(
      final String taskId,
      final String userId,
      @Nullable final Map<String, Object> localVariables)
  {
    final Map<String, Object> localVariablesWithPerformer =
        localVariables == null ? new HashMap<>() : localVariables;
    localVariablesWithPerformer.put(MedsTaskDef.PERFORMER.getName(), userId);
    processService.completeTaskAndSetVariables(taskId, localVariablesWithPerformer, true);
  }

  private String getTaskIdForSimpleTaskOfType(
      final TaskTypeEnum taskType,
      final TherapyAssigneeEnum requesterRole,
      final List<MedicationSupplyTaskSimpleDto> tasks)
  {
    for (final MedicationSupplyTaskSimpleDto task : tasks)
    {
      if (task.getTaskType() == taskType)
      {
        boolean isRequesterRoleMatched = true;
        if (task instanceof DispenseMedicationTaskSimpleDto)
        {
          final TherapyAssigneeEnum taskRequesterRole = ((DispenseMedicationTaskSimpleDto)task).getRequesterRole();
          isRequesterRoleMatched = taskRequesterRole != null && taskRequesterRole == requesterRole;
        }
        if (isRequesterRoleMatched)
        {
          return task.getTaskId();
        }
      }
    }
    return null;
  }
}
