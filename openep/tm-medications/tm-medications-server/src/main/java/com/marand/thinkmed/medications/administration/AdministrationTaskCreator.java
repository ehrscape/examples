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

package com.marand.thinkmed.medications.administration;

import java.util.List;
import javax.annotation.Nonnull;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface AdministrationTaskCreator
{
  List<NewTaskRequestDto> createTaskRequests(
      String patientId,
      @Nonnull TherapyDto therapy,
      @Nonnull AdministrationTaskCreateActionEnum action,
      @Nonnull DateTime actionTimestamp,
      DateTime lastTaskTimestamp);

  NewTaskRequestDto createMedicationTaskRequest(
      String patientId,
      @Nonnull TherapyDto therapy,
      @Nonnull AdministrationTypeEnum administrationType,
      @Nonnull DateTime timestamp,
      TherapyDoseDto dose);

  List<NewTaskRequestDto> createTaskRequestsForAdditionalAdministration(
      String patientId,
      @Nonnull TherapyDto therapy,
      @Nonnull AdministrationTypeEnum administrationType,
      @Nonnull DateTime timestamp,
      TherapyDoseDto dose);

  NewTaskRequestDto createMedicationTaskRequestWithGroupUUId(
      String patientId,
      String groupUUId,
      @Nonnull TherapyDto therapy,
      @Nonnull AdministrationTypeEnum administrationType,
      @Nonnull DateTime timestamp,
      TherapyDoseDto dose);

  List<NewTaskRequestDto> createRequestsForAdditionalRateAdministration(
      String patientId,
      @Nonnull TherapyDto therapy,
      @Nonnull DateTime timestamp,
      @Nonnull TherapyDoseDto dose,
      String groupUUId,
      boolean createStart);

  NewTaskRequestDto createTherapyEndTaskRequest(String patientId, @Nonnull TherapyDto therapy, @Nonnull DateTime timestamp);

  List<Pair<TaskVariable, Object>> getDoseTaskVariables(@Nonnull TherapyDoseDto dose);

  NewTaskRequestDto createTaskRequestFromTaskDto(@Nonnull TaskDto taskDto);

  List<AdministrationDto> calculateTherapyAdministrationTimes(@Nonnull TherapyDto therapy, @Nonnull DateTime when);

  DateTime calculateNextTherapyAdministrationTime(
      @Nonnull TherapyDto therapy,
      boolean presetTimeOnNewPrescription,
      @Nonnull DateTime when);
}
