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
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public class ConstantSimpleMedicationToEhrConverter extends SimpleMedicationToEhrConverter<ConstantSimpleTherapyDto>
{
  @Override
  protected void fillOrderActivityFromTherapyDto(
      final MedicationInstructionInstruction.OrderActivity orderActivity,
      final ConstantSimpleTherapyDto therapyDto,
      final SimpleDoseElementDto doseElement,
      final List<HourMinuteDto> administrationTimes,
      final DateTime administrationDate)
  {
    super.fillOrderActivityFromTherapyDto(orderActivity, therapyDto, doseElement, administrationTimes, administrationDate);
    MedicationsEhrUtils.setTitration(orderActivity, therapyDto);
  }

  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof ConstantSimpleTherapyDto;
  }

  @Override
  protected void fillDoseElements(final ConstantSimpleTherapyDto therapy, final MedicationInstructionInstruction instruction)
  {
    final MedicationInstructionInstruction.OrderActivity orderActivity =
        MedicationsEhrUtils.createEmptyOrderActivityFor(instruction);
    fillOrderActivityFromTherapyDto(orderActivity, therapy, therapy.getDoseElement(), therapy.getDoseTimes(), null);
  }
}
