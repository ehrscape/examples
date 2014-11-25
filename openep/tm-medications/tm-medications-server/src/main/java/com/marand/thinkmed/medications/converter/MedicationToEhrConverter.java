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

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Bostjan Vester
 */
public abstract class MedicationToEhrConverter<M extends TherapyDto>
{
  public abstract boolean isFor(final TherapyDto therapy);

  public final MedicationInstructionInstruction createInstructionFromTherapy(final TherapyDto therapy)
  {
    final MedicationInstructionInstruction instruction = MedicationsEhrUtils.createEmptyMedicationInstruction();
    fillInstructionFromTherapy(instruction, (M)therapy);
    return instruction;
  }

  public abstract void fillInstructionFromTherapy(MedicationInstructionInstruction instruction, M order);
}
