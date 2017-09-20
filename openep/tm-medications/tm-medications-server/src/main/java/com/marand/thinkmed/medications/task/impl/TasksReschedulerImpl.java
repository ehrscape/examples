package com.marand.thinkmed.medications.task.impl;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.TasksRescheduler;
import com.marand.thinkmed.medications.therapy.TherapyCacheInvalidator;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class TasksReschedulerImpl implements TasksRescheduler
{
  private MedicationsTasksHandler medicationsTasksHandler;
  private TherapyCacheInvalidator therapyCacheInvalidator;
  private MedicationsTasksProvider medicationsTasksProvider;

  @Required
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Required
  public void setTherapyCacheInvalidator(final TherapyCacheInvalidator therapyCacheInvalidator)
  {
    this.therapyCacheInvalidator = therapyCacheInvalidator;
  }

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Override
  public void rescheduleTask(@Nonnull final String patientId, @Nonnull final String taskId, @Nonnull final DateTime newTime)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null!");
    Preconditions.checkNotNull(taskId, "taskId must not be null!");
    Preconditions.checkNotNull(newTime, "newTime must not be null!");

    final AdministrationTaskDto task =  medicationsTasksProvider.getAdministrationTask(taskId);
    if (task.getGroupUUId() != null && task.getAdministrationTypeEnum() == AdministrationTypeEnum.START)
    {
      rescheduleGroup(
          patientId,
          task.getTherapyId(),
          task.getGroupUUId(),
          newTime.getMillis() - task.getPlannedAdministrationTime().getMillis());
    }
    else
    {
      medicationsTasksHandler.rescheduleTask(taskId, newTime);
    }

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  public void rescheduleTasks(@Nonnull final String patientId, @Nonnull final String taskId, @Nonnull final DateTime newTime)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null!");
    Preconditions.checkNotNull(taskId, "taskId must not be null!");
    Preconditions.checkNotNull(newTime, "newTime must not be null!");

    final AdministrationTaskDto task = medicationsTasksProvider.getAdministrationTask(taskId);

    final List<TaskDto> laterTasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(task.getTherapyId()),
        task.getPlannedAdministrationTime(),
        null,
        null,
        false);

    final long timeDiff = newTime.getMillis() - task.getPlannedAdministrationTime().getMillis();

    medicationsTasksHandler.rescheduleTask(taskId, newTime);

    laterTasks.forEach(laterTask -> medicationsTasksHandler.rescheduleTask(
        laterTask.getId(),
        new DateTime(laterTask.getDueTime()).plusMillis(Math.toIntExact(timeDiff))));

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  public void rescheduleGroup(
      @Nonnull final String patientId,
      @Nonnull final String therapyId,
      @Nonnull final String groupUUId,
      final long timeDiff)
  {
    final List<TaskDto> groupTasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(therapyId),
        null,
        null,
        groupUUId,
        false);

    //noinspection NumericCastThatLosesPrecision
    groupTasks.forEach(task -> medicationsTasksHandler.rescheduleTask(
        task.getId(),
        new DateTime(task.getDueTime()).plusMillis((int)timeDiff)));
  }
}
