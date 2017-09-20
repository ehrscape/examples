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
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;

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
      final List<HourMinuteDto> administrationTimes = getAdministrationTimes(orderActivity);
      final HourMinuteDto administrationTime = administrationTimes.isEmpty() ? null : administrationTimes.get(0);
      final TimedComplexDoseElementDto timedComplexDoseElementDto = new TimedComplexDoseElementDto();
      timedComplexDoseElementDto.setDoseElement(doseElement);
      timedComplexDoseElementDto.setDoseTime(administrationTime);
      timedDoseElements.add(timedComplexDoseElementDto);
    }
    result.setTimedDoseElements(timedDoseElements);
  }

  @Override
  protected void fillAdministrationDetails(
      final AdministrationDetailsCluster administration,
      final VariableComplexTherapyDto therapyDto)
  {
    therapyDto.setRecurringContinuousInfusion(
        MedicationDeliveryMethodEnum.RECURRING_CONTINUOUS_INFUSION.isEqualTo(administration.getDeliveryMethod()));
  }

  @Override
  protected VariableComplexTherapyDto createEmptyTherapyDto()
  {
    return new VariableComplexTherapyDto();
  }
}
