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

package com.marand.thinkmed.medications.process;

import java.util.Locale;

import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.api.core.data.Named;
import com.marand.thinkmed.api.core.data.NamedIdentity;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.joda.time.DateTime;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Mitja Lapajne
 */
public interface MedicationsProcessService
{
  void startTherapyProcess(
      long patientId,
      MedicationOrderComposition medicationOrderComposition,
      Long sessionId,
      Named knownOrganizationalEntity,
      final DateTime when);

  void sendAbortTherapyRequest(
      long patientId,
      String compositionUid,
      String ehrOrderName,
      boolean therapySuspended,
      DateTime abortTimestamp);

  void sendReviewTherapyRequest(
      long patientId,
      String compositionUid,
      String ehrOrderName,
      Long sessionId,
      Named knownOrganizationalEntity);

  void sendSuspendTherapyRequest(long patientId, String compositionUid, String ehrOrderName, DateTime suspendTimestamp);

  void sendReissueTherapyRequest(
      long patientId,
      String compositionUid,
      String ehrOrderName,
      Long sessionId,
      Named knownOrganizationalEntity);

  void sendModifyTherapyRequest(
      long patientId,
      TherapyDto modifiedTherapy,
      boolean therapySuspended,
      Long centralCaseId,
      Long careProviderId,
      Long sessionId,
      Named knownOrganizationalEntity,
      NamedIdentity prescriber,
      DateTime when,
      Locale locale);

  void reviewTherapy(
      Long patientId,
      String therapyId,
      Long sessionId,
      String knownOrganizationalEntity,
      Long userId,
      Long whenMillis);

  void suspendTherapy(
      Long patientId,
      String therapyId,
      Long userId,
      Long whenMillis);

  void generateInitialTasks(
      Long patientId,
      String therapyId,
      Long sessionId,
      String knownOrganizationalEntity,
      Long whenMillis);

  void generateInitialTasks(
      Long patientId,
      String therapyId,
      Long sessionId,
      String knownOrganizationalEntity,
      boolean forceGenerate,
      Long whenMillis);

  void reissueTherapy(
      Long patientId,
      String therapyId,
      Long sessionId,
      String knownOrganizationalEntity,
      Long userId,
      Long requestTimestampMills);

  void abortTherapy(Long patientId, String therapyId, Long userId, Long whenMillis);

  void modifyTherapy(
      Long patientId,
      String therapyId,
      TherapyDto modifiedTherapy,
      Long centralCaseId,
      Long careProviderId,
      Long sessionId,
      String knownOrganizationalEntityName,
      Long userId,
      NamedIdentity prescriber,
      Long whenMillis,
      Locale locale,
      boolean alwaysOverrideTherapy,
      boolean generateTasks);

  void createSingleAdministrationTask(
      MedicationInstructionInstruction instruction,
      String compositionUid,
      long patientId,
      DateTime timestamp,
      AdministrationTypeEnum type,
      TherapyDoseDto dose);

  void undoCompleteTask(String taskId);
}
