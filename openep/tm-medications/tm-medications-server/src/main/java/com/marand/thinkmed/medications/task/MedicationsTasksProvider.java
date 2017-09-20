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

package com.marand.thinkmed.medications.task;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.allergies.CheckNewAllergiesTaskDto;
import com.marand.thinkmed.medications.dto.mentalHealth.CheckMentalHealthMedsTaskDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */
public interface MedicationsTasksProvider
{
  List<AdministrationTaskDto> findAdministrationTasks(
      String patientId,
      Collection<String> therapyIds,
      Interval searchInterval,
      boolean findHistoric);

  List<TaskDto> findAdministrationTasks(
      String patientId,
      Collection<String> therapyIds,
      DateTime taskDueAfter,
      DateTime taskDueBefore,
      String groupUUId,
      boolean findHistoric);

  AdministrationTaskDto getAdministrationTask(@Nonnull String taskId);

  List<TaskDto> findAdministrationTasks(Set<String> patientIds, DateTime taskDueAfter, DateTime taskDueBefore);

  TaskDto getNextAdministrationTask(String patientId, DateTime fromWhen);

  Opt<AdministrationTaskDto> findLastAdministrationTaskForTherapy(
      @Nonnull String patientId,
      @Nonnull String therapyId,
      Interval searchInterval,
      boolean findHistoric);

  Opt<DateTime> findLastAdministrationTaskTimeForTherapy(
      @Nonnull String patientId,
      @Nonnull String therapyId,
      Interval searchInterval,
      boolean findHistoric);

  Map<String, DateTime> findLastAdministrationTaskTimesForTherapies(
      Collection<String> patientIds,
      DateTime fromTime,
      boolean findHistoric);

  Map<String, List<TherapyTaskSimpleDto>> findSimpleTasksForTherapies(
      String patientId,
      Collection<String> therapyIds,
      DateTime when);

  List<AdministrationPatientTaskDto> findAdministrationTasks(
      Map<String, PatientDisplayWithLocationDto> patientWithLocationMap,
      Interval searchInterval,
      int maxNumberOfTasks,
      Locale locale,
      DateTime when);

  List<CheckNewAllergiesTaskDto> findNewAllergiesTasks(@Nonnull final String patientId);

  List<CheckMentalHealthMedsTaskDto> findNewCheckMentalHealthMedsTasks(@Nonnull final String patientId);
}
