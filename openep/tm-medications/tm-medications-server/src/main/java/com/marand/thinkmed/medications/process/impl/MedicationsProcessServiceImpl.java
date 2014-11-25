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

package com.marand.thinkmed.medications.process.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.bpm.service.BpmService;
import com.marand.ispek.bpm.service.exception.ProcessNotExistsBpmException;
import com.marand.maf.core.Pair;
import com.marand.maf.core.PartialList;
import com.marand.maf.core.openehr.util.InstructionTranslator;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.ServiceMethod;
import com.marand.maf.core.service.auditing.Auditing;
import com.marand.maf.core.service.auditing.Level;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrLinkType;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.api.core.data.Named;
import com.marand.thinkmed.api.core.data.NamedIdentity;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.bpm.medications.TherapyProcess;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationPreferencesUtil;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dao.EhrMedicationsDao;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTaskDto;
import com.marand.thinkmed.medications.process.MedicationsProcessService;
import com.marand.thinkmed.medications.process.TherapyTaskCreator;
import com.marand.thinkmed.medications.process.task.MedicationTaskDef;
import com.marand.thinkmed.medications.process.utils.TherapyTaskUtils;
import com.marand.thinkmed.medications.provider.TherapyTasksProvider;
import com.marand.thinkmed.process.TaskCompletedType;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Mitja Lapajne
 */
public class MedicationsProcessServiceImpl implements MedicationsProcessService, InitializingBean
{
  private BpmService bpmService;
  private ProcessService processService;

  private EhrMedicationsDao ehrMedicationsDao;
  private TherapyTasksProvider therapyTasksProvider;
  private MedicationsBo medicationsBo;
  private TherapyTaskCreator therapyTaskCreator;

  public void setBpmService(final BpmService bpmService)
  {
    this.bpmService = bpmService;
  }

  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  public void setEhrMedicationsDao(final EhrMedicationsDao ehrMedicationsDao)
  {
    this.ehrMedicationsDao = ehrMedicationsDao;
  }

  public void setTherapyTasksProvider(final TherapyTasksProvider therapyTasksProvider)
  {
    this.therapyTasksProvider = therapyTasksProvider;
  }

  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  public void setTherapyTaskCreator(final TherapyTaskCreator therapyTaskCreator)
  {
    this.therapyTaskCreator = therapyTaskCreator;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    Assert.notNull(bpmService, "bpmService is required");
    Assert.notNull(processService, "processService is required");
    Assert.notNull(ehrMedicationsDao, "ehrMedicationsDao is required");
    Assert.notNull(therapyTasksProvider, "therapyTasksProvider is required");
    Assert.notNull(medicationsBo, "medicationsBo is required");
    Assert.notNull(therapyTaskCreator, "therapyTaskCreator is required");
  }

  @Override
  public void startTherapyProcess(
      final long patientId,
      final MedicationOrderComposition composition,
      final Long sessionId,
      final Named knownOrganizationalEntity,
      final DateTime when)
  {
    for (final MedicationInstructionInstruction instruction : composition.getMedicationDetail().getMedicationInstruction())
    {
      final String therapyId = InstructionTranslator.translate(instruction, composition);
      final String knownOrganizationalEntityName =
          knownOrganizationalEntity != null ? knownOrganizationalEntity.name() : null;
      bpmService.startProcess(
          therapyId,
          TherapyProcess.class,
          Pair.<TherapyProcess, Object>of(TherapyProcess.therapyId, therapyId),
          Pair.<TherapyProcess, Object>of(TherapyProcess.patientId, patientId),
          Pair.<TherapyProcess, Object>of(TherapyProcess.originalTherapyId, therapyId),
          Pair.<TherapyProcess, Object>of(TherapyProcess.sessionId, sessionId),
          Pair.<TherapyProcess, Object>of(TherapyProcess.knownOrganizationalEntity, knownOrganizationalEntityName),
          Pair.<TherapyProcess, Object>of(TherapyProcess.when, when.getMillis()));
    }
  }

  @Override
  public void sendAbortTherapyRequest(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final boolean therapySuspended,
      final DateTime abortTimestamp)
  {
    final String therapyId = getTherapyId(ehrOrderName, compositionUid);

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> originalTherapy =
        medicationsBo.getOriginalTherapy(patientId, compositionUid, ehrOrderName);
    final String originalTherapyId =
        InstructionTranslator.translate(originalTherapy.getSecond(), originalTherapy.getFirst());
    bpmService.messageEventReceived(
        therapySuspended ? TherapyProcess.receiveAbortSuspendedTherapyRequest : TherapyProcess.receiveAbortTherapyRequest,
        originalTherapyId,
        TherapyProcess.class,
        Pair.<TherapyProcess, Object>of(TherapyProcess.therapyId, therapyId),
        Pair.<TherapyProcess, Object>of(TherapyProcess.userId, RequestContextHolder.getContext().getUserId()),
        Pair.<TherapyProcess, Object>of(TherapyProcess.when, abortTimestamp.getMillis()));
  }

  @Override
  public void sendReviewTherapyRequest(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final Long sessionId,
      final Named knownOrganizationalEntity)
  {
    final String therapyId = getTherapyId(ehrOrderName, compositionUid);

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> originalTherapy =
        medicationsBo.getOriginalTherapy(patientId, compositionUid, ehrOrderName);
    final String originalTherapyId =
        InstructionTranslator.translate(originalTherapy.getSecond(), originalTherapy.getFirst());
    final String knownOrganizationalEntityName =
        knownOrganizationalEntity != null ? knownOrganizationalEntity.name() : null;
    bpmService.messageEventReceived(
        TherapyProcess.receiveReviewOrderRequest,
        originalTherapyId,
        TherapyProcess.class,
        Pair.<TherapyProcess, Object>of(TherapyProcess.userId, RequestContextHolder.getContext().getUserId()),
        Pair.<TherapyProcess, Object>of(
            TherapyProcess.when,
            RequestContextHolder.getContext().getRequestTimestamp().getMillis()),
        Pair.<TherapyProcess, Object>of(TherapyProcess.therapyId, therapyId),
        Pair.<TherapyProcess, Object>of(TherapyProcess.sessionId, sessionId),
        Pair.<TherapyProcess, Object>of(TherapyProcess.knownOrganizationalEntity, knownOrganizationalEntityName)
    );
  }

  @Override
  public void sendSuspendTherapyRequest(
      final long patientId, final String compositionUid, final String ehrOrderName, final DateTime suspendTimestamp)
  {
    final String therapyId = getTherapyId(ehrOrderName, compositionUid);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> originalTherapy =
        medicationsBo.getOriginalTherapy(patientId, compositionUid, ehrOrderName);
    final String originalTherapyId =
        InstructionTranslator.translate(originalTherapy.getSecond(), originalTherapy.getFirst());
    bpmService.messageEventReceived(
        TherapyProcess.receiveSuspendTherapyRequest,
        originalTherapyId,
        TherapyProcess.class,
        Pair.<TherapyProcess, Object>of(TherapyProcess.therapyId, therapyId),
        Pair.<TherapyProcess, Object>of(TherapyProcess.userId, RequestContextHolder.getContext().getUserId()),
        Pair.<TherapyProcess, Object>of(TherapyProcess.when, suspendTimestamp.getMillis()));
  }

  @Override
  public void sendReissueTherapyRequest(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final Long sessionId,
      final Named knownOrganizationalEntity)
  {
    final String therapyId = getTherapyId(ehrOrderName, compositionUid);

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> originalTherapy =
        medicationsBo.getOriginalTherapy(patientId, compositionUid, ehrOrderName);
    final String originalTherapyId =
        InstructionTranslator.translate(originalTherapy.getSecond(), originalTherapy.getFirst());
    final String knownOrganizationalEntityName =
        knownOrganizationalEntity != null ? knownOrganizationalEntity.name() : null;
    bpmService.messageEventReceived(
        TherapyProcess.receiveReissueSuspendedTherapyRequest,
        originalTherapyId,
        TherapyProcess.class,
        Pair.<TherapyProcess, Object>of(TherapyProcess.therapyId, therapyId),
        Pair.<TherapyProcess, Object>of(TherapyProcess.userId, RequestContextHolder.getContext().getUserId()),
        Pair.<TherapyProcess, Object>of(
            TherapyProcess.when, RequestContextHolder.getContext().getRequestTimestamp().getMillis()),
        Pair.<TherapyProcess, Object>of(TherapyProcess.sessionId, sessionId),
        Pair.<TherapyProcess, Object>of(TherapyProcess.knownOrganizationalEntity, knownOrganizationalEntityName)
    );
  }

  @Override
  public void sendModifyTherapyRequest(
      final long patientId,
      final TherapyDto modifiedTherapy,
      final boolean therapySuspended,
      final Long centralCaseId,
      final Long careProviderId,
      final Long sessionId,
      final Named knownOrganizationalEntity,
      final NamedIdentity prescriber,
      final DateTime when,
      final Locale locale)
  {
    final String therapyId = getTherapyId(modifiedTherapy.getEhrOrderName(), modifiedTherapy.getCompositionUid());
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> originalTherapy =
        medicationsBo.getOriginalTherapy(patientId, modifiedTherapy.getCompositionUid(), modifiedTherapy.getEhrOrderName());
    final String originalTherapyId =
        InstructionTranslator.translate(originalTherapy.getSecond(), originalTherapy.getFirst());
    final String knownOrganizationalEntityName =
        knownOrganizationalEntity != null ? knownOrganizationalEntity.name() : null;

    try
    {
      bpmService.messageEventReceived(
          therapySuspended ? TherapyProcess.receiveModifySuspendedTherapyRequest : TherapyProcess.receiveModifyTherapyRequest,
          originalTherapyId,
          TherapyProcess.class,
          Pair.<TherapyProcess, Object>of(TherapyProcess.therapyId, therapyId),
          Pair.<TherapyProcess, Object>of(TherapyProcess.userId, RequestContextHolder.getContext().getUserId()),
          Pair.<TherapyProcess, Object>of(TherapyProcess.prescriber, prescriber),
          Pair.<TherapyProcess, Object>of(TherapyProcess.when, when.getMillis()),
          Pair.<TherapyProcess, Object>of(TherapyProcess.language, locale),
          Pair.<TherapyProcess, Object>of(TherapyProcess.modifiedTherapy, modifiedTherapy),
          Pair.<TherapyProcess, Object>of(TherapyProcess.sessionId, sessionId),
          Pair.<TherapyProcess, Object>of(TherapyProcess.centralCaseId, centralCaseId),
          Pair.<TherapyProcess, Object>of(TherapyProcess.careProviderId, careProviderId),
          Pair.<TherapyProcess, Object>of(TherapyProcess.knownOrganizationalEntity, knownOrganizationalEntityName)
      );
    }
    catch (ProcessNotExistsBpmException e)
    {
      // no process no problem ;-)
    }
  }

  private String getTherapyId(final String ehrOrderName, final String compositionUid)
  {
    final MedicationInstructionInstruction instruction = new MedicationInstructionInstruction();
    instruction.setName(DataValueUtils.getText(ehrOrderName));
    return InstructionTranslator.translate(instruction, compositionUid);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void generateInitialTasks(
      final Long patientId,
      final String therapyId,
      final Long sessionId,
      final String departmentName,
      final Long whenMillis)
  {
    generateInitialTasks(
        patientId,
        therapyId,
        sessionId,
        departmentName,
        false,
        whenMillis);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void generateInitialTasks(
      final Long patientId,
      final String therapyId,
      final Long sessionId,
      final String departmentName,
      final boolean forceGenerate,
      final Long whenMillis)
  {
    createTherapyTasks(
        patientId,
        therapyId,
        sessionId,
        KnownClinic.Utils.fromName(departmentName),
        true,
        null,
        forceGenerate,
        new DateTime(whenMillis));
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void reviewTherapy(
      final Long patientId,
      final String therapyId,
      final Long sessionId,
      final String departmentName,
      final Long userId,
      final Long whenMillis)
  {
    final Pair<String, String> compositionIdAndInstructionName =
        InstructionTranslator.getCompositionIdAndInstructionName(therapyId);
    final String compositionUid = compositionIdAndInstructionName.getFirst();
    final String instructionName = compositionIdAndInstructionName.getSecond();

    final DateTime when = new DateTime(whenMillis);
    medicationsBo.reviewTherapy(patientId, compositionUid, instructionName, userId, when);
    final KnownClinic department =
        KnownClinic.Utils.fromName(departmentName);

    DateTime lastTaskTimestamp = findLastTaskForTherapy(therapyId);
    if (lastTaskTimestamp == null)
    {
      lastTaskTimestamp = when;
    }

    createTherapyTasks(patientId, therapyId, sessionId, department, false, lastTaskTimestamp, false, when);
  }

  private DateTime findLastTaskForTherapy(final String therapyId)
  {
    DateTime lastTaskTimestamp = null;
    final List<TherapyTaskDto> tasksForTherapies = therapyTasksProvider.findTherapyTasks(null, therapyId, null);
    for (final TherapyTaskDto task : tasksForTherapies)
    {
      if (lastTaskTimestamp == null || task.getPlannedAdministrationTime().isAfter(lastTaskTimestamp))
      {
        lastTaskTimestamp = task.getPlannedAdministrationTime();
      }
    }
    return lastTaskTimestamp;
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void suspendTherapy(
      final Long patientId,
      final String therapyId,
      final Long userId,
      final Long whenMillis)
  {
    final Pair<String, String> compositionIdAndInstructionName =
        InstructionTranslator.getCompositionIdAndInstructionName(therapyId);
    final String compositionUid = compositionIdAndInstructionName.getFirst();
    final String instructionName = compositionIdAndInstructionName.getSecond();

    final DateTime when = new DateTime(whenMillis);
    medicationsBo.suspendTherapy(patientId, compositionUid, instructionName, userId, when);
    handleTasksOnTherapyStop(patientId, therapyId, userId, compositionUid, instructionName, false, when);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void reissueTherapy(
      final Long patientId,
      final String therapyId,
      final Long sessionId,
      final String departmentName,
      final Long userId,
      final Long whenMillis)
  {
    final Pair<String, String> compositionIdAndInstructionName =
        InstructionTranslator.getCompositionIdAndInstructionName(therapyId);
    final String compositionUid = compositionIdAndInstructionName.getFirst();
    final String instructionName = compositionIdAndInstructionName.getSecond();

    final DateTime when = new DateTime(whenMillis);
    final RoundsIntervalDto roundsInterval =
        MedicationPreferencesUtil.getRoundsInterval(
            sessionId, KnownClinic.Utils.fromName(departmentName));
    medicationsBo.reissueTherapy(patientId, compositionUid, instructionName, roundsInterval, userId, when);

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
        ehrMedicationsDao.getTherapyInstructionPair(patientId, compositionUid, instructionName);
    if (MedicationsEhrUtils.isContinuousInfusion(therapyInstructionPair.getSecond())) //reactivate continuous infusion
    {
      final TherapyDoseDto therapyDose =
          therapyTaskCreator.getTherapyDoseDto(therapyInstructionPair.getSecond().getOrder().get(0), false);
      createSingleAdministrationTask(
          therapyInstructionPair.getSecond(), compositionUid, patientId, AdministrationTypeEnum.START, when, therapyDose);
    }
    else
    {
      DateTime lastTaskTimestamp = findLastTaskForTherapy(therapyId);
      if (lastTaskTimestamp == null)
      {
        lastTaskTimestamp = when;
      }
      createTherapyTasks(
          patientId,
          therapyInstructionPair,
          sessionId,
          KnownClinic.Utils.fromName(departmentName),
          false,
          lastTaskTimestamp,
          when);
    }
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void abortTherapy(
      final Long patientId,
      final String therapyId,
      final Long userId,
      final Long whenMillis)
  {
    final Pair<String, String> compositionIdAndInstructionName =
        InstructionTranslator.getCompositionIdAndInstructionName(therapyId);
    final String compositionUid = compositionIdAndInstructionName.getFirst();
    final String instructionName = compositionIdAndInstructionName.getSecond();
    final DateTime when = new DateTime(whenMillis);
    medicationsBo.abortTherapy(patientId, compositionUid, instructionName, userId, when);

    handleTasksOnTherapyStop(patientId, therapyId, userId, compositionUid, instructionName, true, when);
  }

  private void handleTasksOnTherapyStop(
      final Long patientId,
      final String therapyId,
      final Long userId,
      final String compositionUid,
      final String instructionName,
      final boolean completePreviousTasks,
      final DateTime when)
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
        ehrMedicationsDao.getTherapyInstructionPair(patientId, compositionUid, instructionName);

    deleteTherapyTasks(patientId, therapyId, therapyInstructionPair, userId, completePreviousTasks, when);

    if (MedicationsEhrUtils.isContinuousInfusion(therapyInstructionPair.getSecond()))
    {
      createSingleAdministrationTask(
          therapyInstructionPair.getSecond(), compositionUid, patientId, AdministrationTypeEnum.STOP, when, null);
    }
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void modifyTherapy(
      final Long patientId,
      final String therapyId,
      final TherapyDto modifiedTherapy,
      final Long centralCaseId,
      final Long careProviderId,
      final Long sessionId,
      final String departmentName,
      final Long userId,
      final NamedIdentity prescriber,
      final Long whenMillis,
      final Locale locale,
      final boolean alwaysOverrideTherapy,
      final boolean generateTasks)
  {
    final KnownClinic department =
        KnownClinic.Utils.fromName(departmentName);
    final RoundsIntervalDto roundsInterval =
        MedicationPreferencesUtil.getRoundsInterval(sessionId, department);

    final DateTime when = new DateTime(whenMillis);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> newInstructionPair =
        medicationsBo.modifyTherapy(
            patientId,
            modifiedTherapy,
            centralCaseId,
            careProviderId,
            roundsInterval,
            userId,
            prescriber,
            when,
            alwaysOverrideTherapy,
            locale);

    if (generateTasks)
    {
      final Pair<String, String> compositionIdAndInstructionName =
          InstructionTranslator.getCompositionIdAndInstructionName(therapyId);

      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> oldInstructionPair =
          ehrMedicationsDao.getTherapyInstructionPair(
              patientId, compositionIdAndInstructionName.getFirst(), compositionIdAndInstructionName.getSecond());

      deleteTherapyTasks(patientId, therapyId, oldInstructionPair, userId, true, when);

      if (MedicationsEhrUtils.isContinuousInfusion(newInstructionPair.getSecond()))
      {
        final DateTime changeTimestamp =
            DataValueUtils.getDateTime(
                newInstructionPair.getSecond().getOrder().get(0).getMedicationTiming().getStartDate());
        createSingleAdministrationTask(
            newInstructionPair.getSecond(),
            newInstructionPair.getFirst().getUid().getValue(),
            patientId,
            AdministrationTypeEnum.ADJUST_INFUSION,
            changeTimestamp,
            therapyTaskCreator.getTherapyDoseDto(newInstructionPair.getSecond().getOrder().get(0), false));
      }
      else
      {
        createTherapyTasks(patientId, newInstructionPair, sessionId, department, true, null, when);
      }
    }
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void createSingleAdministrationTask(
      final MedicationInstructionInstruction instruction,
      final String compositionUid,
      final long patientId,
      final DateTime timestamp,
      final AdministrationTypeEnum type,
      final TherapyDoseDto dose)
  {
    createSingleAdministrationTask(instruction, compositionUid, patientId, type, timestamp, dose);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void undoCompleteTask(final String taskId)
  {
    //activiti does not support undo, we will delete completed task and create a copy
    final TaskDto task = processService.getHistoricTask(taskId);
    final NewTaskRequestDto taskRequest = therapyTaskCreator.createTaskRequestFromTaskDto(task);
    processService.setTaskDeleteReason(taskId, TaskCompletedType.DELETED.getBpmName());
    processService.createTasks(taskRequest);
  }

  private void createSingleAdministrationTask(
      final MedicationInstructionInstruction instruction,
      final String compositionUid,
      final long patientId,
      final AdministrationTypeEnum administrationTypeEnum,
      final DateTime timestamp,
      final TherapyDoseDto dose)
  {
    if (dose != null)
    {
      final NewTaskRequestDto additionalAdministrationTask =
          therapyTaskCreator.createMedicationTaskRequest(
              instruction, compositionUid, patientId, administrationTypeEnum, timestamp, dose);

      processService.createTasks(additionalAdministrationTask);
    }
  }

  private void createTherapyTasks(
      final Long patientId,
      final String therapyId,
      final Long sessionId,
      final KnownClinic department,
      final boolean therapyStart,
      final DateTime lastTaskTimestamp,
      final boolean forceGenerate,
      final DateTime when)
  {
    final Pair<String, String> compositionIdAndInstructionName =
        InstructionTranslator.getCompositionIdAndInstructionName(therapyId);
    final String compositionUid = compositionIdAndInstructionName.getFirst();
    final String instructionName = compositionIdAndInstructionName.getSecond();

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
        ehrMedicationsDao.getTherapyInstructionPair(patientId, compositionUid, instructionName);

    if (!forceGenerate &&
        !MedicationsEhrUtils.getLinksOfType(therapyInstructionPair.getSecond(), OpenEhrLinkType.ISSUE).isEmpty())
    {
      //therapy task creation will be triggered when the last administration from the linked therapy is confirmed
      return;
    }
    createTherapyTasks(
        patientId, therapyInstructionPair, sessionId, department, therapyStart, lastTaskTimestamp, when);
  }

  private void createTherapyTasks(
      final Long patientId,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair,
      final Long sessionId,
      final Named knownOrganizationalEntity,
      final boolean therapyStart,
      final DateTime lastTaskTimestamp,
      final DateTime when)
  {
    final RoundsIntervalDto roundsInterval =
        MedicationPreferencesUtil.getRoundsInterval(sessionId, knownOrganizationalEntity);
    final AdministrationTimingDto administrationTiming =
        MedicationPreferencesUtil.getAdministrationTiming(sessionId, knownOrganizationalEntity);

    final List<NewTaskRequestDto> taskRequests = therapyTaskCreator.createTasks(
        patientId, therapyInstructionPair, administrationTiming, roundsInterval, when, therapyStart, lastTaskTimestamp);

    processService.createTasks(taskRequests.toArray(new NewTaskRequestDto[taskRequests.size()]));
  }

  private void deleteTherapyTasks(
      final Long patientId,
      final String therapyId,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair,
      final Long userId,
      final boolean completePastTasks,
      final DateTime when)
  {
    final PartialList<TaskDto> tasks = processService.findTasks(
        null,
        MedicationTaskDef.INSTANCE.getGroupName(),
        MedicationTaskDef.INSTANCE.getTaskExecutionId(),
        false,
        null,
        null,
        Pair.of(MedicationTaskDef.THERAPY_ID, therapyId));

    final List<String> futureTasksIds = new ArrayList<>();
    for (final TaskDto task : tasks)
    {
      final DateTime taskTimestamp = task.getDueTime();
      if (taskTimestamp.isAfter(when))
      {
        futureTasksIds.add(task.getId());
      }
      else if (completePastTasks)
      {
        final AdministrationDto administrationDto;
        final AdministrationTypeEnum administrationTypeEnum = AdministrationTypeEnum.valueOf(
            (String)task.getVariables().get(MedicationTaskDef.ADMINISTRATION_TYPE.getName()));
        if (administrationTypeEnum == AdministrationTypeEnum.START)
        {
          administrationDto = new StartAdministrationDto();
          final TherapyDoseDto therapyDoseDto = TherapyTaskUtils.buildTherapyDoseDtoFromTask(task);
          ((StartAdministrationDto)administrationDto).setPlannedDose(therapyDoseDto);
        }
        else if (administrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION)
        {
          administrationDto = new AdjustInfusionAdministrationDto();
          final TherapyDoseDto therapyDoseDto = TherapyTaskUtils.buildTherapyDoseDtoFromTask(task);
          ((AdjustInfusionAdministrationDto)administrationDto).setPlannedDose(therapyDoseDto);
        }
        else if (administrationTypeEnum == AdministrationTypeEnum.STOP)
        {
          administrationDto = new StopAdministrationDto();
        }
        else
        {
          throw new IllegalArgumentException("Inconsistent AdministrationTypeEnum.");
        }
        administrationDto.setTaskId(task.getId());
        administrationDto.setPlannedTime(taskTimestamp);
        administrationDto.setAdministrationTime(taskTimestamp);

        final String administrationUid =
            medicationsBo.confirmMedicationAdministration(
                therapyInstructionPair,
                patientId,
                userId,
                null,
                null,
                administrationDto,
                MedicationActionEnum.WITHHOLD,
                when);
        therapyTasksProvider.associateTaskWithAdministration(administrationDto.getTaskId(), administrationUid);
        therapyTasksProvider.completeTasks(administrationDto.getTaskId());
      }
    }
    if (!futureTasksIds.isEmpty())
    {
      processService.deleteTasks(futureTasksIds);
    }
  }
}
