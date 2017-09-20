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

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedSimpleDoseElementDto;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public class VariableSimpleMedicationFromEhrConverter extends SimpleMedicationFromEhrConverter<VariableSimpleTherapyDto>
{
  @Override
  public boolean isFor(final MedicationInstructionInstruction instruction)
  {
    return MedicationsEhrUtils.isSimpleInstruction(instruction)
        && MedicationsEhrUtils.isVariableInstruction(instruction)
        && !MedicationsEhrUtils.isOxygenInstruction(instruction);
  }

  @Override
  protected void fillDoseElements(
      final VariableSimpleTherapyDto result,
      final List<OrderActivity> orderActivities)
  {
    if (orderActivities.size() <= 1)
    {
      throw new IllegalArgumentException("Expected more than one order activity, got " + orderActivities.size() + '!');
    }
    final List<TimedSimpleDoseElementDto> timedDoseElements = new ArrayList<>();
    for (final OrderActivity orderActivity : orderActivities)
    {
      final SimpleDoseElementDto doseElement = createSimpleDoseElement(orderActivity);
      final List<HourMinuteDto> administrationTimes = getAdministrationTimes(orderActivity);
      final HourMinuteDto administrationTime = administrationTimes.isEmpty() ? null : administrationTimes.get(0);
      final DateTime administrationDate = getAdministrationDate(orderActivity);
      final TimedSimpleDoseElementDto doseElementDto = new TimedSimpleDoseElementDto();
      doseElementDto.setDoseElement(doseElement);
      doseElementDto.setDoseTime(administrationTime);
      doseElementDto.setDate(administrationDate);
      timedDoseElements.add(doseElementDto);
    }
    result.setTimedDoseElements(timedDoseElements);
  }

  @Override
  protected VariableSimpleTherapyDto createEmptyTherapyDto()
  {
    return new VariableSimpleTherapyDto();
  }
}
