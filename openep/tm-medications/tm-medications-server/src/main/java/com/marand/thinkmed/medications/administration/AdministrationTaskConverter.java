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
import java.util.Locale;
import java.util.Map;

import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface AdministrationTaskConverter
{
  AdministrationDto buildAdministrationFromTask(AdministrationTaskDto task, DateTime when);

  AdministrationDto convertNewTaskRequestDtoToAdministrationDto(NewTaskRequestDto taskRequest, DateTime when);

  AdministrationTaskDto convertTaskToAdministrationTask(TaskDto task);

  List<AdministrationPatientTaskDto> convertTasksToAdministrationPatientTasks(
      List<TaskDto> tasks,
      Map<String, PatientDisplayWithLocationDto> patientWithLocationMap,
      Locale locale,
      DateTime when);
}
