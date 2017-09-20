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

package com.marand.thinkmed.medications.pharmacist;

import java.util.List;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */
public interface PharmacistTaskHandler
{
  void deleteSupplyTask(final List<String> taskIds, final String userId);

  void dismissSupplyTask(List<String> taskIds, String userId);

  void deleteNurseSupplyTask(String patientId, String taskId, String userId);

  void confirmSupplyReminderTask(
      String taskId,
      String therapyIdAtConfirmation,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String userId,
      String comment);

  void editSupplyReminderTask(
      String taskId,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment,
      DateTime when);

  void confirmSupplyReviewTask(
      String patientId,
      String taskId,
      String therapyIdAtConfirmation,
      boolean alreadyDispensed,
      boolean createSupplyReminder,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment,
      String userId,
      DateTime when);

  void handleReviewTaskOnTherapiesChange(
      @Nonnull String patientId,
      DateTime hospitalizationStart,
      @Nonnull DateTime when,
      String lastEditorName,
      DateTime lastEditTime,
      PrescriptionChangeTypeEnum changeType,
      @Nonnull PharmacistReviewTaskStatusEnum status);

  void confirmPharmacistDispenseTask(
      String patientId,
      String taskId,
      String therapyIdAtConfirmation,
      TherapyAssigneeEnum requesterRole,
      SupplyRequestStatus supplyRequestStatus,
      String userId);

  void deletePerfusionSyringeTask(String taskId, String userId);

  void undoPerfusionSyringeTask(String taskId, String userId);

  void confirmPerfusionSyringeTasks(List<String> taskIds, String userId);

  void setDispenseTaskPrintedTimestamp(String taskId, DateTime requestTimestamp);

  void editPerfusionSyringeTask(
      @Nonnull String taskId,
      @Nonnull Integer numberOfSyringes,
      boolean isUrgent,
      @Nonnull DateTime dueDate,
      boolean printSystemLabel);

  void confirmPerfusionSyringeTasksForTherapy(String patientId, String userId, TaskTypeEnum taskTypeEnum, String originalTherapyId);
}
