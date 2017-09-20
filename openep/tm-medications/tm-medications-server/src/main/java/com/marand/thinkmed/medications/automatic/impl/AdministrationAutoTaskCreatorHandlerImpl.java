package com.marand.thinkmed.medications.automatic.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Opt;
import com.marand.maf.core.service.ServiceMethod;
import com.marand.maf.core.service.auditing.Auditing;
import com.marand.maf.core.service.auditing.Level;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.medications.administration.AdministrationTaskCreator;
import com.marand.thinkmed.medications.automatic.AdministrationAutoTaskCreatorHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.dto.AutomaticAdministrationTaskCreatorDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */
public class AdministrationAutoTaskCreatorHandlerImpl implements AdministrationAutoTaskCreatorHandler
{
  private static final Logger LOG = LoggerFactory.getLogger(AdministrationAutoTaskCreatorHandlerImpl.class);

  private ProcessService processService;
  private AdministrationTaskCreator administrationTaskCreator;
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationsBo medicationsBo;

  @Required
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

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

  @Override
  @EhrSessioned
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public void createAdministrationTasksOnAutoCreate(
      @Nonnull final AutomaticAdministrationTaskCreatorDto automaticAdministrationTaskCreatorDto,
      @Nonnull final DateTime actionTimestamp)
  {
    Preconditions.checkNotNull(automaticAdministrationTaskCreatorDto.getTherapyDto(), "automaticAdministrationTaskCreatorDto.getTherapyDto()");
    Preconditions.checkNotNull(automaticAdministrationTaskCreatorDto.getPatientId(), "automaticAdministrationTaskCreatorDto.getPatientId()");

    final List<NewTaskRequestDto> taskRequestDtos = administrationTaskCreator.createTaskRequests(
        automaticAdministrationTaskCreatorDto.getPatientId(),
        automaticAdministrationTaskCreatorDto.getTherapyDto(),
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        actionTimestamp,
        automaticAdministrationTaskCreatorDto.getLastAdministrationTime());

    if (!taskRequestDtos.isEmpty())
    {
      processService.createTasks(taskRequestDtos.toArray(new NewTaskRequestDto[taskRequestDtos.size()]));
    }
  }

  @Override
  public List<AutomaticAdministrationTaskCreatorDto> getAutoAdministrationTaskCreatorDtos(
      @Nonnull final DateTime when,
      @Nonnull final Map<MedicationOrderComposition, String> activeInstructionsWithPatientIds)
  {
    Preconditions.checkNotNull(when, "when");
    Preconditions.checkNotNull(activeInstructionsWithPatientIds, "activeInstructionsWithPatientIds");

    final Set<String> patientIds = new HashSet<>(activeInstructionsWithPatientIds.values());

    final Map<String, DateTime> lastAdministrationTaskTimesForTherapies = new HashMap<>();
    for (final String patientId : patientIds)
    {
      lastAdministrationTaskTimesForTherapies.putAll(
          medicationsTasksProvider.findLastAdministrationTaskTimesForTherapies(
              Collections.singletonList(patientId),
              when.minusDays(7),
              true));
    }

    final List<AutomaticAdministrationTaskCreatorDto> automaticAdministrationTaskCreatorDtos = new ArrayList<>();
    for (final Map.Entry<MedicationOrderComposition, String> entry : activeInstructionsWithPatientIds.entrySet())
    {
      try
      {
        final MedicationOrderComposition composition = entry.getKey();
        final MedicationInstructionInstruction instruction = entry.getKey().getMedicationDetail().getMedicationInstruction().get(0);

        final TherapyDto therapyDto = medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
            composition,
            instruction,
            null,
            null,
            when,
            false,
            null);

        final String therapyId = TherapyIdUtils.createTherapyId(
            composition.getUid().getValue(),
            instruction.getName().getValue());

        final DateTime lastAdministrationTime =
            lastAdministrationTaskTimesForTherapies.containsKey(therapyId)
            ? lastAdministrationTaskTimesForTherapies.get(therapyId) : null;

        final String patientId = entry.getValue();
        final AutomaticAdministrationTaskCreatorDto administrationTaskCreatorDto = new AutomaticAdministrationTaskCreatorDto(
            therapyDto,
            patientId,
            lastAdministrationTime);

        automaticAdministrationTaskCreatorDtos.add(administrationTaskCreatorDto);
      }
      catch (final Throwable t)
      {
        LOG.error("Error converting from composition : " + Opt.resolve(() -> entry.getKey().getUid().getValue()).get());
      }
    }

    return automaticAdministrationTaskCreatorDtos;
  }
}
