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

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.business.impl.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import org.joda.time.DateTime;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;

/**
 * @author Bostjan Vester
 */
public class VariableComplexMedicationFromEhrConverter extends ComplexMedicationFromEhrConverter<VariableComplexTherapyDto>
{
  @Override
  public boolean isFor(final MedicationInstructionInstruction instruction)
  {
    return !MedicationsEhrUtils.isSimpleInstruction(instruction) && MedicationsEhrUtils.isVariableInstruction(instruction);
  }

  @Override
  protected void fillDoseElements(
      final VariableComplexTherapyDto result,
      final List<OrderActivity> orderActivities,
      final DateTime when,
      final MedicationDataProvider medicationDataProvider)
  {
    if (orderActivities.size() <= 1)
    {
      throw new IllegalArgumentException("Expected more than one order activity, got " + orderActivities.size() + '!');
    }
    final List<TimedComplexDoseElementDto> timedDoseElements = new ArrayList<>();
    for (final OrderActivity orderActivity : orderActivities)
    {
      final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
      fillDoseElementFromOrderActivity(doseElement, orderActivity, false);
      final HourMinuteDto administrationTime = getAdministrationTime(orderActivity);
      final TimedComplexDoseElementDto timedComplexDoseElementDto = new TimedComplexDoseElementDto();
      timedComplexDoseElementDto.setDoseElement(doseElement);
      timedComplexDoseElementDto.setDoseTime(administrationTime);
      timedDoseElements.add(timedComplexDoseElementDto);
    }
    result.setTimedDoseElements(timedDoseElements);
  }

  @Override
  protected VariableComplexTherapyDto createEmptyTherapyDto()
  {
    return new VariableComplexTherapyDto();
  }
}
