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

package com.marand.thinkmed.medications.converter.therapy;

import java.util.List;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;

/**
 * @author Bostjan Vester
 */
public class ConstantComplexMedicationToEhrConverter extends ComplexMedicationToEhrConverter<ConstantComplexTherapyDto>
{
  @Override
  protected void fillOrderActivityFromTherapyDto(
      final MedicationInstructionInstruction.OrderActivity orderActivity,
      final ConstantComplexTherapyDto therapyDto,
      final ComplexDoseElementDto doseElement,
      final List<HourMinuteDto> administrationTimes)
  {
    super.fillOrderActivityFromTherapyDto(orderActivity, therapyDto, doseElement, administrationTimes);
    MedicationsEhrUtils.setTitration(orderActivity, therapyDto);
  }

  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof ConstantComplexTherapyDto;
  }

  @Override
  protected void fillDoseElements(
      final ConstantComplexTherapyDto therapy,
      final MedicationInstructionInstruction instruction)
  {
    final MedicationInstructionInstruction.OrderActivity orderActivity =
        MedicationsEhrUtils.createEmptyOrderActivityFor(instruction);
    fillOrderActivityFromTherapyDto(orderActivity, therapy, therapy.getDoseElement(), therapy.getDoseTimes());
  }

  @Override
  protected boolean isSpecificSpeedHandled(
      final ConstantComplexTherapyDto therapy,
      final AdministrationDetailsCluster administration)
  {
    if (MedicationsEhrUtils.BOLUS.equals(therapy.getRateString()))
    {
      MedicationsEhrUtils.addInfusionAdministrationDetails(
          administration, therapy.isBaselineInfusion(), MedicationsEhrUtils.BOLUS, null, null, null, null);
      return true;
    }
    return false;
  }

  @Override
  protected boolean isRecurringContinuousInfusion(final ConstantComplexTherapyDto therapy)
  {
    return false;
  }
}
