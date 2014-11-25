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

package com.marand.thinkmed.medications.process.utils;

import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.process.task.MedicationTaskDef;
import com.marand.thinkmed.process.dto.TaskDto;

/**
 * @author Mitja Lapajne
 */
public class TherapyTaskUtils
{
  private TherapyTaskUtils()
  {
  }

  public static TherapyDoseDto buildTherapyDoseDtoFromTask(final TaskDto task)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    therapyDoseDto.setTherapyDoseTypeEnum(
        TherapyDoseTypeEnum.valueOf((String)task.getVariables().get(MedicationTaskDef.DOSE_TYPE.getName())));
    therapyDoseDto.setDenominator(
        (Double)task.getVariables().get(MedicationTaskDef.DOSE_DENOMINATOR.getName()));
    therapyDoseDto.setDenominatorUnit(
        (String)task.getVariables().get(MedicationTaskDef.DOSE_DENOMINATOR_UNIT.getName()));
    therapyDoseDto.setNumerator(
        (Double)task.getVariables().get(MedicationTaskDef.DOSE_NUMERATOR.getName()));
    therapyDoseDto.setNumeratorUnit(
        (String)task.getVariables().get(MedicationTaskDef.DOSE_NUMERATOR_UNIT.getName()));
    if (therapyDoseDto.getNumerator() == null)
    {
      return null;
    }
    return therapyDoseDto;
  }
}
