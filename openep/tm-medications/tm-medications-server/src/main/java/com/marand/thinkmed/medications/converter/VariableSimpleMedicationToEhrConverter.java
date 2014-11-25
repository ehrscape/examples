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

package com.marand.thinkmed.medications.converter;

import com.marand.thinkmed.medications.business.impl.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Bostjan Vester
 */
public class VariableSimpleMedicationToEhrConverter extends SimpleMedicationToEhrConverter<VariableSimpleTherapyDto>
{
  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof VariableSimpleTherapyDto;
  }

  @Override
  protected void fillDoseElements(final VariableSimpleTherapyDto therapy, final MedicationInstructionInstruction instruction)
  {
    for (final TimedSimpleDoseElementDto timedDoseElement : therapy.getTimedDoseElements())
    {
      final MedicationInstructionInstruction.OrderActivity orderActivity =
          MedicationsEhrUtils.createEmptyOrderActivityFor(instruction);
      fillOrderActivityFromTherapyDto(
          orderActivity,
          therapy,
          timedDoseElement.getDoseElement(),
          timedDoseElement.getDoseTime(),
          timedDoseElement.getDate());
    }
  }
}
