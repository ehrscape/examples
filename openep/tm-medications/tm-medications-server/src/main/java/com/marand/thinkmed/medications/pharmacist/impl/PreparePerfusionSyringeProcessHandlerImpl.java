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

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.ispek.bpm.service.BpmService;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.thinkmed.bpm.medications.process.PreparePerfusionSyringeProcess;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.pharmacist.PreparePerfusionSyringeProcessHandler;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Klavdij Lapajne
 */
public class PreparePerfusionSyringeProcessHandlerImpl implements PreparePerfusionSyringeProcessHandler
{
  private BpmService bpmService;
  private MedicationsBo medicationsBo;

  @Required
  public void setBpmService(final BpmService bpmService)
  {
    this.bpmService = bpmService;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Override
  public void handlePreparationRequest(
      @Nonnull final String patientId,
      @Nonnull final String therapyCompositionUid,
      @Nonnull final String ehrOrderName,
      final int numberOfSyringes,
      final boolean urgent,
      @Nonnull final DateTime dueTime,
      @Nonnull final String userName,
      final boolean printSystemLabel)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    StringUtils.checkNotBlank(therapyCompositionUid, "therapyCompositionUid");
    StringUtils.checkNotBlank(ehrOrderName, "ehrOrderName");
    Preconditions.checkNotNull(dueTime, "dueTime");
    StringUtils.checkNotBlank(userName, "userName");

    final String originalTherapyId = medicationsBo.getOriginalTherapyId(patientId, therapyCompositionUid);

    Preconditions.checkArgument(
        !bpmService.isProcessInExecution(originalTherapyId, PreparePerfusionSyringeProcess.class),
        "Only one active prepare perfusion syringe request allowed for therapy!");

    bpmService.startProcess(
        originalTherapyId,
        PreparePerfusionSyringeProcess.class,
        Pair.of(PreparePerfusionSyringeProcess.patientId, patientId),
        Pair.of(PreparePerfusionSyringeProcess.originalTherapyId, originalTherapyId),
        Pair.of(PreparePerfusionSyringeProcess.numberOfSyringes, numberOfSyringes),
        Pair.of(PreparePerfusionSyringeProcess.isUrgent, urgent),
        Pair.of(PreparePerfusionSyringeProcess.undoState, false),
        Pair.of(PreparePerfusionSyringeProcess.cancelPreparation, false),
        Pair.of(PreparePerfusionSyringeProcess.orderCanceled, false),
        Pair.of(PreparePerfusionSyringeProcess.therapyCanceled, false),
        Pair.of(PreparePerfusionSyringeProcess.dueDateTimeMillis, dueTime.getMillis()),
        Pair.of(PreparePerfusionSyringeProcess.orderer, RequestContextHolder.getContext().getUserId()),
        Pair.of(PreparePerfusionSyringeProcess.ordererFullName, userName),
        Pair.of(PreparePerfusionSyringeProcess.printSystemLabel , printSystemLabel)
    );
  }

  @Override
  public void handleOrderCancellationMessage(
      @Nonnull final String patientId,
      @Nonnull  final String therapyCompositionUid,
      @Nonnull final String ehrOrderName)
  {
    Preconditions.checkNotNull(patientId, "patientId is null");
    Preconditions.checkNotNull(therapyCompositionUid, "therapyCompositionUid is null");

    final String originalTherapyId = medicationsBo.getOriginalTherapyId(patientId, therapyCompositionUid);

    Preconditions.checkArgument(
        bpmService.isProcessInExecution(originalTherapyId, PreparePerfusionSyringeProcess.class),
        "Prepare perfusion syringe process not in active state!"
    );

    bpmService.messageEventReceived(
        PreparePerfusionSyringeProcess.cancelOrderMessage,
        originalTherapyId,
        PreparePerfusionSyringeProcess.class,
        Pair.of(PreparePerfusionSyringeProcess.patientId, patientId),
        Pair.of(PreparePerfusionSyringeProcess.originalTherapyId, originalTherapyId),
        Pair.of(PreparePerfusionSyringeProcess.cancelPreparation, true),
        Pair.of(PreparePerfusionSyringeProcess.therapyCanceled, false),
        Pair.of(PreparePerfusionSyringeProcess.orderCanceled, true));
  }

  @Override
  public void handleTherapyCancellationMessage(
      @Nonnull final String patientId,
      @Nonnull final String therapyCompositionUid,
      @Nonnull final String ehrOrderName)
  {
    Preconditions.checkNotNull(patientId, "patientId is null");
    Preconditions.checkNotNull(therapyCompositionUid, "therapyCompositionUid is null");
    Preconditions.checkNotNull(ehrOrderName, "ehrOrderName is null");

    final String originalTherapyId = medicationsBo.getOriginalTherapyId(patientId, therapyCompositionUid);
    handleTherapyCancellationMessage(patientId, originalTherapyId);
  }

  @Override
  public void handleTherapyCancellationMessage(@Nonnull final String patientId, @Nonnull final String originalTherapyId)
  {
    Preconditions.checkNotNull(patientId, "patientId is null");
    Preconditions.checkNotNull(originalTherapyId, "originalTherapyId is null");

    if (bpmService.isProcessInExecution(originalTherapyId, PreparePerfusionSyringeProcess.class))
    {
      bpmService.messageEventReceived(
          PreparePerfusionSyringeProcess.cancelTherapyMessage,
          originalTherapyId,
          PreparePerfusionSyringeProcess.class,
          Pair.of(PreparePerfusionSyringeProcess.patientId, patientId),
          Pair.of(
              PreparePerfusionSyringeProcess.originalTherapyId,
              originalTherapyId),
          Pair.of(PreparePerfusionSyringeProcess.cancelPreparation, true),
          Pair.of(PreparePerfusionSyringeProcess.therapyCanceled, true),
          Pair.of(PreparePerfusionSyringeProcess.orderCanceled, false));
    }
  }

  @Override
  public void handleMedicationAdministrationMessage(
      @Nonnull final String patientId,
      @Nonnull final String therapyCompositionUid,
      @Nonnull final String ehrOrderName)
  {
    Preconditions.checkNotNull(patientId, "patientId is null");
    Preconditions.checkNotNull(therapyCompositionUid, "therapyCompositionUid is null");
    Preconditions.checkNotNull(ehrOrderName, "ehrOrderName is null");

    final String originalTherapyId = medicationsBo.getOriginalTherapyId(patientId, therapyCompositionUid);

    Preconditions.checkArgument(
        bpmService.isProcessInExecution(originalTherapyId, PreparePerfusionSyringeProcess.class),
        "Prepare perfusion syringe process not in active state!"
    );

    bpmService.messageEventReceived(
        PreparePerfusionSyringeProcess.medicationAdministrationMessage,
        originalTherapyId,
        PreparePerfusionSyringeProcess.class,
        Pair.of(PreparePerfusionSyringeProcess.patientId, patientId),
        Pair.of(
            PreparePerfusionSyringeProcess.originalTherapyId,
            originalTherapyId));
    //Pair.<PreparePerfusionSyringeProcess, Object>of(PreparePerfusionSyringeProcess.cancelPreparation, false),
    //Pair.<PreparePerfusionSyringeProcess, Object>of(PreparePerfusionSyringeProcess.undoState, false));
  }
}
