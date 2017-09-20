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
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import org.openehr.jaxb.rm.DvText;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;

/**
 * @author Bostjan Vester
 */
public class ConstantComplexMedicationFromEhrConverter extends ComplexMedicationFromEhrConverter<ConstantComplexTherapyDto>
{
  @Override
  public boolean isFor(final MedicationInstructionInstruction instruction)
  {
    return !MedicationsEhrUtils.isSimpleInstruction(instruction) && !MedicationsEhrUtils.isVariableInstruction(instruction);
  }

  @Override
  protected void fillDoseElements(
      final ConstantComplexTherapyDto result,
      final List<MedicationInstructionInstruction.OrderActivity> orderActivities,
      final MedicationDataProvider medicationDataProvider)
  {
    if (orderActivities.size() != 1)
    {
      throw new IllegalArgumentException("Expected exactly one order activity, got " + orderActivities.size() + '!');
    }
    final MedicationInstructionInstruction.OrderActivity orderActivity = orderActivities.get(0);

    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();

    boolean specificsPrefilled = false;
    final AdministrationDetailsCluster administration = orderActivity.getAdministrationDetails();
    if (!administration.getInfusionAdministrationDetails().isEmpty())
    {
      final InfusionAdministrationDetailsCluster infusionDetails = administration.getInfusionAdministrationDetails().get(0);
      if (infusionDetails.getDoseAdministrationRate() instanceof DvText)
      {
        final String doseAdministrationRate = ((DvText)infusionDetails.getDoseAdministrationRate()).getValue();
        if (!MedicationsEhrUtils.BOLUS.equals(doseAdministrationRate))
        {
          throw new IllegalArgumentException(
              "Only [" + MedicationsEhrUtils.BOLUS + "] " +
                  "allowed as string dose administration rate! Was [" + doseAdministrationRate + "]...");
        }
        result.setRateString(MedicationsEhrUtils.BOLUS);
        specificsPrefilled = true;
      }
    }
    fillDoseElementFromOrderActivity(doseElement, orderActivity, specificsPrefilled);
    result.setDoseElement(doseElement);

    final List<HourMinuteDto> administrationTimes = getAdministrationTimes(orderActivity);
    result.setDoseTimes(administrationTimes);

    result.setTitration(MedicationsEhrUtils.getTitration(orderActivity));
  }

  @Override
  protected void fillAdministrationDetails(
      final AdministrationDetailsCluster administration, final ConstantComplexTherapyDto therapyDto)
  {
  }

  @Override
  protected ConstantComplexTherapyDto createEmptyTherapyDto()
  {
    return new ConstantComplexTherapyDto();
  }
}
