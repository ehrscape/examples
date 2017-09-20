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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskCreator;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReminderTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReviewTaskDef;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Klavdij Lapajne
 */
public class PharmacistTaskCreatorImpl implements PharmacistTaskCreator
{
  private ProcessService processService;

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Override
  public void createPharmacistReminderTask(
      final String patientId,
      final String compositionUid,
      final DateTime reminderDate,
      final String reminderNote,
      final Locale locale)
  {
    final NewTaskRequestDto taskRequest = createPharmacistReminderTaskRequest(
        patientId,
        compositionUid,
        reminderDate,
        reminderNote,
        locale);

    processService.createTasks(taskRequest);
  }

  @Override
  public void createPharmacistReviewTask(
      @Nonnull final String patientId,
      final DateTime dueTime,
      final String lastEditorName,
      final DateTime lastEditTime,
      @Nonnull final PrescriptionChangeTypeEnum changeType,
      @Nonnull final PharmacistReviewTaskStatusEnum status)
  {
    Preconditions.checkNotNull(patientId, "patientId");
    Preconditions.checkNotNull(changeType, "changeType");
    Preconditions.checkNotNull(status, "status");

    final NewTaskRequestDto taskRequest = createPharmacistReviewTaskRequest(
        patientId,
        dueTime,
        lastEditorName,
        lastEditTime,
        changeType,
        status);

    processService.createTasks(taskRequest);
  }

  @Override
  public void updatePharmacistReviewTask(
      @Nonnull final String taskId,
      final DateTime dueTime,
      final String lastEditorName,
      final DateTime lastEditTime)
  {
    Preconditions.checkNotNull(taskId, "taskId");

    final Map<String, Object> variables = new HashMap<>();
    variables.put(PharmacistReviewTaskDef.LAST_EDITOR_NAME.getName(), lastEditorName);
    variables.put(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS.getName(), lastEditTime.getMillis());
    processService.setDueDate(taskId, dueTime);
    processService.setVariables(taskId, variables);
  }

  private NewTaskRequestDto createPharmacistReviewTaskRequest(
      final String patientId,
      final DateTime dueTime,
      final String lastEditorName,
      final DateTime lastEditTime,
      final PrescriptionChangeTypeEnum changeType,
      final PharmacistReviewTaskStatusEnum status)
  {
    return new NewTaskRequestDto(
        PharmacistReviewTaskDef.INSTANCE,
        PharmacistReviewTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)),
        "Pharmacist review",
        "Pharmacist review",
        TherapyAssigneeEnum.PHARMACIST.name(),
        dueTime,
        null,
        Pair.of(MedsTaskDef.PATIENT_ID, patientId),
        Pair.of(PharmacistReviewTaskDef.LAST_EDITOR_NAME, lastEditorName),
        Pair.of(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS, lastEditTime.getMillis()),
        Pair.of(PharmacistReviewTaskDef.CHANGE_TYPE, changeType.name()),
        Pair.of(PharmacistReviewTaskDef.STATUS, status.name()));
  }

  private NewTaskRequestDto createPharmacistReminderTaskRequest(
      final String patientId,
      final String pharmacistReviewCompositionUid,
      final DateTime reminderDate,
      final String reminderNote,
      final Locale locale)
  {
    return new NewTaskRequestDto(
        PharmacistReminderTaskDef.INSTANCE,
        PharmacistReminderTaskDef.getTaskTypeEnum().buildKey(String.valueOf(patientId)),
        "Pharmacist reminder " + reminderDate.toString(DateTimeFormatters.shortDate(locale)),
        "Pharmacist reminder " + reminderDate.toString(DateTimeFormatters.shortDate(locale)),
        TherapyAssigneeEnum.PHARMACIST.name(),
        reminderDate,
        null,
        Pair.of(MedsTaskDef.PATIENT_ID, patientId),
        Pair.of(PharmacistReminderTaskDef.PHARMACIST_REVIEW_ID, pharmacistReviewCompositionUid),
        Pair.of(PharmacistReminderTaskDef.COMMENT, reminderNote));
  }
}
