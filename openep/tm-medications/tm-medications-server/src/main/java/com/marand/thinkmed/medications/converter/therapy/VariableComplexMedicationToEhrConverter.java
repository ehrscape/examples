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

import java.util.Collections;

import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;

/**
 * @author Bostjan Vester
 */
public class VariableComplexMedicationToEhrConverter extends ComplexMedicationToEhrConverter<VariableComplexTherapyDto>
{
  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof VariableComplexTherapyDto;
  }

  @Override
  protected void fillDoseElements(
      final VariableComplexTherapyDto therapy, final MedicationInstructionInstruction instruction)
  {
    for (final TimedComplexDoseElementDto timedDoseElement : therapy.getTimedDoseElements())
    {
      final OrderActivity orderActivity = MedicationsEhrUtils.createEmptyOrderActivityFor(instruction);
      fillOrderActivityFromTherapyDto(
          orderActivity,
          therapy,
          timedDoseElement.getDoseElement(),
          Collections.singletonList(timedDoseElement.getDoseTime()));
    }
  }

  @Override
  protected boolean isSpecificSpeedHandled(
      final VariableComplexTherapyDto therapy,
      final AdministrationDetailsCluster administration)
  {
    return false;
  }

  @Override
  protected boolean isRecurringContinuousInfusion(final VariableComplexTherapyDto therapy)
  {
    return therapy.isRecurringContinuousInfusion();
  }
}
