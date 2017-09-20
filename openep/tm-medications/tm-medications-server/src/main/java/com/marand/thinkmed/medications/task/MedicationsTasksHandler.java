package com.marand.thinkmed.medications.task;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface MedicationsTasksHandler
{
  void deletePatientTasksOfType(String patientId, Set<TaskTypeEnum> taskTypes, String userId);

  void deleteTherapyTasksOfType(String patientId, Set<TaskTypeEnum> taskTypes, String userId, String originalTherapyId);

  void associateTaskWithAdministration(String taskId, String administrationCompositionUid);

  void rescheduleTask(String taskId, DateTime newTime);

  void setDoctorConfirmationResult(String taskId, Boolean result);

  void setAdministrationTitratedDose(
      @Nonnull String patientId,
      @Nonnull String latestTherapyId,
      @Nonnull String taskId,
      @Nonnull TherapyDoseDto therapyDose,
      String doctorsComment,
      @Nonnull DateTime plannedAdministrationTime,
      DateTime until);

  void deleteTask(@Nonnull String taskId, String comment);

  void deleteAdministrationTasks(
      @Nonnull String patientId,
      @Nonnull String therapyId,
      String groupUUId,
      List<AdministrationTypeEnum> types);

  void createCheckNewAllergiesTask(
      @Nonnull String patientId,
      @Nonnull Collection<NamedExternalDto> allergies,
      @Nonnull DateTime when);

  void createCheckMentalHealthMedsTask(@Nonnull String patientId, @Nonnull DateTime when);

  void undoCompleteTask(String taskId);

  void setAdministrationDoctorsComment(@Nonnull String taskId, String doctorsComment);
}
