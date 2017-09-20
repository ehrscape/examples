package com.marand.thinkmed.medications.infusion.impl;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.InfusionBagTaskDto;
import com.marand.thinkmed.medications.infusion.InfusionBagTaskHandler;
import com.marand.thinkmed.medications.infusion.InfusionBagTaskProvider;
import com.marand.thinkmed.medications.task.InfusionBagChangeTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.therapy.TherapyCacheInvalidator;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class InfusionBagTaskHandlerImpl implements InfusionBagTaskHandler
{
  private ProcessService processService;
  private InfusionBagTaskProvider infusionBagTaskProvider;
  private MedicationsTasksHandler medicationsTasksHandler;
  private TherapyCacheInvalidator therapyCacheInvalidator;

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Required
  public void setInfusionBagTaskProvider(final InfusionBagTaskProvider infusionBagTaskProvider)
  {
    this.infusionBagTaskProvider = infusionBagTaskProvider;
  }

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

  @Override
  public InfusionBagTaskDto convertTaskToInfusionBagTask(@Nonnull final TaskDto task)
  {
    Preconditions.checkNotNull(task, "task must not be null");

    final InfusionBagTaskDto infusionBagTask = new InfusionBagTaskDto();
    final String therapyId = (String)task.getVariables().get(InfusionBagChangeTaskDef.THERAPY_ID.getName());
    infusionBagTask.setTherapyId(therapyId);

    infusionBagTask.setTaskId(task.getId());
    infusionBagTask.setPlannedAdministrationTime(task.getDueTime());

    final String administrationId = (String)task.getVariables().get(InfusionBagChangeTaskDef.THERAPY_ADMINISTRATION_ID.getName());
    infusionBagTask.setAdministrationId(administrationId);

    final Double quantity = (Double)task.getVariables().get(InfusionBagChangeTaskDef.INFUSION_BAG_QUANTITY.getName());
    final String quantityUnit = (String)task.getVariables().get(InfusionBagChangeTaskDef.INFUSION_BAG_UNIT.getName());

    final InfusionBagDto infusionBag = new InfusionBagDto(quantity, quantityUnit);
    infusionBagTask.setInfusionBag(infusionBag);
    infusionBagTask.setAdministrationTypeEnum(AdministrationTypeEnum.INFUSION_SET_CHANGE);

    return infusionBagTask;
  }

  @Override
  public void deleteInfusionBagTasks(
      @Nonnull final String patientId,
      @Nonnull final List<String> therapyIds,
      final String comment)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(therapyIds, "therapyId must not be null");

    final List<TaskDto> tasks =
        infusionBagTaskProvider.findTasks(
            Collections.singletonList(InfusionBagChangeTaskDef.getTaskTypeEnum().buildKey(patientId)),
            therapyIds,
            false,
            null,
            null);

    tasks.forEach(taskDto -> medicationsTasksHandler.deleteTask(taskDto.getId(), comment));
    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  public void createInfusionBagTask(
      @Nonnull final String patientId,
      @Nonnull final String therapyId,
      @Nonnull final DateTime plannedTime)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(therapyId, "therapyId must not be null");
    Preconditions.checkNotNull(plannedTime, "plannedTime must not be null");

    processService.createTasks(createInfusionBagTaskRequest(patientId, therapyId, plannedTime));
  }

  private NewTaskRequestDto createInfusionBagTaskRequest(
      final String patientId,
      final String therapyId,
      final DateTime plannedTime)
  {
    //noinspection unchecked
    return new NewTaskRequestDto(
        InfusionBagChangeTaskDef.INSTANCE,
        InfusionBagChangeTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)),
        "Infusion bag change",
        "Infusion bag change",
        TherapyAssigneeEnum.NURSE.name(),
        plannedTime,
        null,
        Pair.of(MedsTaskDef.PATIENT_ID, patientId),
        Pair.of(InfusionBagChangeTaskDef.THERAPY_ID, therapyId),
        Pair.of(InfusionBagChangeTaskDef.ADMINISTRATION_TYPE, AdministrationTypeEnum.INFUSION_SET_CHANGE));
  }
}
