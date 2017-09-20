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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringePreparationDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.SupplyDataForPharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskSimpleDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Klavdij Lapajne
 */
public interface PharmacistTaskProvider
{
  List<TaskDto> findPharmacistReminderAndSupplyTasks(String patientId, Interval searchInterval);

  Opt<String> findPharmacistReviewTaskId(@Nonnull String patientId);

  List<String> findTaskIds(
      final Interval searchInterval,
      final String assignee,
      final Set<String> patientIdsSet,
      final Set<TaskTypeEnum> taskTypes);

  List<PatientTaskDto> findPharmacistTasks(
      Interval searchInterval,
      final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      Set<TaskTypeEnum> taskTypes);

  List<MedicationSupplyTaskDto> findSupplyTasks(
      @Nullable Interval searchInterval,
      Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      Set<TaskTypeEnum> taskTypes,
      boolean closedTasksOnly,
      boolean includeUnverifiedDispenseTasks,
      DateTime when,
      Locale locale);

  List<MedicationSupplyTaskSimpleDto> findSupplySimpleTasksForTherapy(
      @Nullable final Interval searchInterval,
      final Set<String> patientIdsSet,
      final Set<TaskTypeEnum> taskTypes,
      final String originalTherapyId);

  List<TaskDto> findNurseSupplyTasksForTherapy(String patientId, String originalTherapyId);

  MedicationSupplyTaskSimpleDto getSupplySimpleTask(
      String taskId,
      DateTime when,
      Locale locale);

  SupplyDataForPharmacistReviewDto getSupplyDataForPharmacistReview(
      @Nonnull String patientId,
      @Nonnull String therapyCompositionUid);

  MedicationSupplyTaskSimpleDto getSupplySimpleTask(String taskId);

  List<PerfusionSyringePatientTasksDto> findPerfusionSyringeTasks(
      @Nonnull Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap,
      Interval searchInterval,
      @Nonnull Set<TaskTypeEnum> taskTypes,
      boolean closedTasksOnly,
      @Nonnull DateTime when,
      @Nonnull Locale locale);

  Map<String, PerfusionSyringePreparationDto> getOriginalTherapyIdAndPerfusionSyringePreparationDtoMap(
      String patientId,
      boolean isUrgent,
      Set<String> originalTherapyIds,
      DateTime when,
      Locale locale);

  Map<String, String> getOriginalTherapyIdAndPerfusionSyringeTaskIdMap(String patientId, boolean isUrgent);

  PerfusionSyringeTaskSimpleDto getPerfusionSyringeTaskSimpleDto(String taskId, Locale locale);

  boolean therapyHasTasksClosedInInterval(
      @Nonnull String patientId,
      @Nonnull String originalTherapyId,
      @Nonnull Set<TaskTypeEnum> taskTypeEnum,
      @Nonnull Interval interval);

  DateTime getLastEditTimestampForPharmacistReview(String patientId);

  List<TaskDto> getTherapyTasks(TaskTypeEnum taskTypeEnum, String patientId, String originalTherapyId);
}
