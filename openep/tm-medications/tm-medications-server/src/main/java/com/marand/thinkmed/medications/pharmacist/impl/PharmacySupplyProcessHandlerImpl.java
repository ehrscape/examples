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

import java.util.List;

import com.marand.ispek.bpm.service.BpmService;
import com.marand.maf.core.Pair;
import com.marand.thinkmed.bpm.medications.process.PharmacySupplyProcess;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacySupplyProcessHandler;
import com.marand.thinkmed.process.dto.TaskDto;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Klavdij Lapajne
 */
public class PharmacySupplyProcessHandlerImpl implements PharmacySupplyProcessHandler
{
  private BpmService bpmService;
  private MedicationsBo medicationsBo;
  private PharmacistTaskProvider pharmacistTaskProvider;

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

  @Required
  public void setPharmacistTaskProvider(final PharmacistTaskProvider pharmacistTaskProvider)
  {
    this.pharmacistTaskProvider = pharmacistTaskProvider;
  }

  @Override
  public void handleSupplyRequest(
      final String patientId,
      final TherapyAssigneeEnum requesterRole,
      final String therapyCompositionUid,
      final String ehrOrderName,
      final Integer supplyInDays,
      final MedicationSupplyTypeEnum supplyType)
  {
    final String originalTherapyId = medicationsBo.getOriginalTherapyId(patientId, therapyCompositionUid);
    handleSupplyRequest(patientId, requesterRole, originalTherapyId, supplyType, supplyInDays);
  }

  @Override
  public void handleSupplyRequest(
      final String patientId,
      final TherapyAssigneeEnum requesterRole,
      final String originalTherapyId,
      final MedicationSupplyTypeEnum supplyType,
      final Integer supplyInDays)
  {
    final boolean supplyProcessExists = bpmService.isProcessInExecution(originalTherapyId, PharmacySupplyProcess.class);

    if (supplyProcessExists)
    {
      if (requesterRole == TherapyAssigneeEnum.PHARMACIST)
      {
        throw new IllegalStateException("Only one active supply process allowed for therapy!");
      }
      else if (requesterRole == TherapyAssigneeEnum.NURSE)
      {
        final List<TaskDto> nurseSupplyTasks = pharmacistTaskProvider.findNurseSupplyTasksForTherapy(
            patientId,
            originalTherapyId);
        if (nurseSupplyTasks != null && !nurseSupplyTasks.isEmpty())
        {
          throw new IllegalStateException("Only one active nurse resupply request allowed for therapy!");
        }
        createNurseResupplyRequest(originalTherapyId, patientId);
      }
    }
    else
    {
      final String supplyTypeName = supplyType != null ? supplyType.name() : null;
      bpmService.startProcess(
          originalTherapyId,
          PharmacySupplyProcess.class,
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.patientId, patientId),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.originalTherapyId, originalTherapyId),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.requesterRole, requesterRole.name()),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.createResupplyReminder, requesterRole == TherapyAssigneeEnum.PHARMACIST),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.supplyInDays, supplyInDays),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.supplyType, supplyTypeName),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.dispenseMedication,
              requesterRole == TherapyAssigneeEnum.NURSE || supplyType != MedicationSupplyTypeEnum.PATIENTS_OWN));
    }
  }

  private void createNurseResupplyRequest(final String originalTherapyId, final String patientId)
  {
    bpmService.messageEventReceived(
        PharmacySupplyProcess.nurseResupplyMessage,
        originalTherapyId,
        PharmacySupplyProcess.class,
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.patientId, patientId),
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.originalTherapyId, originalTherapyId),
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.requesterRole, TherapyAssigneeEnum.NURSE.name()),
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.confirmResupply, true),
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.dispenseMedication, true));
  }
}
