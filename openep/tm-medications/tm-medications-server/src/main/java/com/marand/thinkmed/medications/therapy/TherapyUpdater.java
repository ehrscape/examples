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

package com.marand.thinkmed.medications.therapy;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TherapyUpdater
{
  List<MedicationOrderComposition> saveTherapies(
      String patientId,
      List<SaveMedicationOrderDto> medicationOrders,
      String centralCaseId,
      @Nullable String careProviderId,
      NamedExternalDto prescriber,
      DateTime when,
      Locale locale);

  void modifyTherapy(
      String patientId,
      TherapyDto modifiedTherapy,
      TherapyChangeReasonDto changeReason,
      String centralCaseId,
      @Nullable String careProviderId,
      NamedExternalDto prescriber,
      boolean therapyAlreadyStarted,
      String basedOnPharmacyReviewId,
      DateTime when,
      Locale locale);

  void abortTherapy(
      String patientId,
      String compositionUid,
      String ehrOrderName,
      TherapyChangeReasonDto changeReason,
      DateTime when);

  String suspendTherapy(
      String patientId,
      String compositionUid,
      String ehrOrderName,
      TherapyChangeReasonDto changeReason,
      DateTime when);

  void reissueTherapy(
      String patientId,
      String compositionUid,
      String ehrOrderName,
      DateTime when);

  String reviewTherapy(String patientId, String compositionUid);

  void saveConsecutiveDays(
      String patientId,
      String compositionUid,
      String ehrOrderName,
      String userId,
      Integer consecutiveDays);

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  boolean startLinkedTherapy(
      String patientId,
      Pair<MedicationOrderComposition, MedicationInstructionInstruction> linkedTherapy,
      DateTime startTimestamp);

  void addMedicationOnAdmissionLinkToInstruction(
      String patientId,
      String onAdmissionCompositionId,
      MedicationInstructionInstruction instruction);

  void updateTherapySelfAdministeringStatus(
      String patientId,
      MedicationOrderComposition orderComposition,
      SelfAdministeringActionEnum selfAdministeringActionEnum,
      String userId,
      DateTime when);

  void createTherapyTasks(
      String patientId,
      MedicationOrderComposition composition,
      AdministrationTaskCreateActionEnum action,
      DateTime lastTaskTimestamp,
      DateTime when);

  void createAdditionalAdministrationTask(
      MedicationOrderComposition composition,
      String patientId,
      DateTime timestamp,
      AdministrationTypeEnum type,
      TherapyDoseDto dose);
}
