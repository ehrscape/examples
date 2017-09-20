package com.marand.thinkmed.medications.automatic.impl;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.service.ServiceMethod;
import com.marand.maf.core.service.auditing.Auditing;
import com.marand.maf.core.service.auditing.Level;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.automatic.AdministrationAutoTaskConfirmerHandler;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */
public class AdministrationAutoTaskConfirmerHandlerImpl implements AdministrationAutoTaskConfirmerHandler
{
  private AdministrationTaskConverter administrationTaskConverter;
  private AdministrationHandler administrationHandler;
  private MedicationsTasksHandler medicationsTasksHandler;
  private ProcessService processService;

  @Required
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Required
  public void setAdministrationHandler(final AdministrationHandler administrationHandler)
  {
    this.administrationHandler = administrationHandler;
  }

  @Required
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Override
  @EhrSessioned
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public void autoConfirmAdministrationTask(
      @Nonnull final AutomaticChartingType type,
      @Nonnull final String patientId,
      @Nonnull final MedicationOrderComposition composition,
      @Nonnull final TaskDto administrationTask,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(type, "type");
    StringUtils.checkNotBlank(patientId, "patient");
    Preconditions.checkNotNull(composition, "composition");
    Preconditions.checkNotNull(when, "now");
    Preconditions.checkNotNull(administrationTask, "administrationTask");

    final AdministrationDto administrationDto = administrationTaskConverter.buildAdministrationFromTask(
        administrationTaskConverter.convertTaskToAdministrationTask(administrationTask),
        when);

    final String taskId = administrationDto.getTaskId();
    final String administrationUid = administrationHandler.autoConfirmSelfAdministration(
        type,
        composition,
        patientId,
        administrationDto,
        when);

    medicationsTasksHandler.associateTaskWithAdministration(taskId, administrationUid);
    processService.completeTasks(taskId);
  }
}
