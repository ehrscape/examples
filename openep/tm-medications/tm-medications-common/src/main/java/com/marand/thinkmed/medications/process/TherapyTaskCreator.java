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

import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Mitja Lapajne
 */
public interface TherapyTaskCreator
{
  List<NewTaskRequestDto> createTasks(
      Long patientId,
      Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair,
      AdministrationTimingDto administrationTiming,
      RoundsIntervalDto roundsInterval,
      DateTime actionTimestamp,
      boolean therapyStart,
      DateTime lastTaskTimestamp);

  NewTaskRequestDto createMedicationTaskRequest(
      final MedicationInstructionInstruction instruction,
      final String compositionUid,
      final long patientId,
      final AdministrationTypeEnum administrationTypeEnum,
      final DateTime timestamp,
      final TherapyDoseDto dose);

  NewTaskRequestDto createTaskRequestFromTaskDto(TaskDto taskDto);

  TherapyDoseDto getTherapyDoseDto(final MedicationInstructionInstruction.OrderActivity orderActivity, final boolean simple);
}
