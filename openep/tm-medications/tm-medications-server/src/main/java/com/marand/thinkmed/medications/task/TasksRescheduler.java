package com.marand.thinkmed.medications.task;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface TasksRescheduler
{
  void rescheduleTask(@Nonnull String patientId, @Nonnull String taskId, @Nonnull DateTime newTime);

  void rescheduleTasks(@Nonnull String patientId, @Nonnull String taskId, @Nonnull DateTime newTime);

  void rescheduleGroup(@Nonnull String patientId, @Nonnull String therapyId, @Nonnull String groupUUId, long timeDiff);
}
