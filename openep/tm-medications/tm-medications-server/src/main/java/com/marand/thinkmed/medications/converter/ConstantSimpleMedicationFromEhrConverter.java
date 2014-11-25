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

import java.util.List;

import com.marand.thinkmed.medications.business.impl.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.SimpleDoseElementDto;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;

/**
 * @author Bostjan Vester
 */
public class ConstantSimpleMedicationFromEhrConverter extends SimpleMedicationFromEhrConverter<ConstantSimpleTherapyDto>
{
  @Override
  public boolean isFor(final MedicationInstructionInstruction instruction)
  {
    return MedicationsEhrUtils.isSimpleInstruction(instruction) && !MedicationsEhrUtils.isVariableInstruction(instruction);
  }

  @Override
  protected void fillDoseElements(
      final ConstantSimpleTherapyDto result,
      final List<OrderActivity> orderActivities)
  {
    if (orderActivities.size() != 1)
    {
      throw new IllegalArgumentException("Expected exactly one order activity, got " + orderActivities.size() + '!');
    }
    final OrderActivity orderActivity = orderActivities.get(0);
    final SimpleDoseElementDto doseElement = createSimpleDoseElement(orderActivity);
    result.setDoseElement(doseElement);
  }

  @Override
  protected ConstantSimpleTherapyDto createEmptyTherapyDto()
  {
    return new ConstantSimpleTherapyDto();
  }
}
