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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.process.dto.TaskDto;

/**
 * @author Mitja Lapajne
 */
public class TherapyTaskUtils
{
  private TherapyTaskUtils()
  {
  }

  public static List<String> getPatientIdKeysForTaskTypes(
      @Nonnull final Set<String> patientIdsSet,
      @Nonnull final Set<TaskTypeEnum> taskTypesSet)
  {
    Preconditions.checkNotNull(patientIdsSet, "patientIdsSet is null");
    Preconditions.checkNotNull(taskTypesSet, "taskTypesSet is null");
    final List<String> patientIdKeys = new ArrayList<>();
    for (final String patientId : patientIdsSet)
    {
      Preconditions.checkNotNull(patientId, "patientId is null");
      for (final TaskTypeEnum taskType : taskTypesSet)
      {
        Preconditions.checkNotNull(taskType, "taskType is null");
        patientIdKeys.add(taskType.buildKey(patientId));
      }
    }
    return patientIdKeys;
  }

  public static TherapyDoseDto buildTherapyDoseDtoFromTask(@Nonnull final TaskDto task)
  {
    Preconditions.checkNotNull(task, "task is null");
    final String doseType = (String)task.getVariables().get(AdministrationTaskDef.DOSE_TYPE.getName());

    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    therapyDoseDto.setTherapyDoseTypeEnum(doseType != null ? TherapyDoseTypeEnum.valueOf(doseType) : null);
    therapyDoseDto.setDenominator((Double)task.getVariables().get(AdministrationTaskDef.DOSE_DENOMINATOR.getName()));
    therapyDoseDto.setDenominatorUnit((String)task.getVariables().get(AdministrationTaskDef.DOSE_DENOMINATOR_UNIT.getName()));
    therapyDoseDto.setNumerator((Double)task.getVariables().get(AdministrationTaskDef.DOSE_NUMERATOR.getName()));
    therapyDoseDto.setNumeratorUnit((String)task.getVariables().get(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName()));

    return therapyDoseDto.getNumerator() == null ? null : therapyDoseDto;
  }
}
