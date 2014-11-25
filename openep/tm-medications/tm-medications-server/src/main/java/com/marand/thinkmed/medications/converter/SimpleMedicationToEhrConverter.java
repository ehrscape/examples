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

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.TherapyCommentEnum;
import com.marand.thinkmed.medications.business.impl.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCount;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;

/**
 * @author Bostjan Vester
 */
public abstract class SimpleMedicationToEhrConverter<M extends SimpleTherapyDto> extends MedicationToEhrConverter<M>
{
  @Override
  public final void fillInstructionFromTherapy(
      final MedicationInstructionInstruction instruction, final M therapy)
  {
    instruction.setNarrative(DataValueUtils.getText(therapy.getTherapyDescription()));
    instruction.getOrder().clear();
    fillDoseElements(therapy, instruction);
  }

  protected void fillOrderActivityFromTherapyDto(
      final OrderActivity orderActivity,
      final M therapyDto,
      final SimpleDoseElementDto doseElement,
      final HourMinuteDto administrationTime,
      final DateTime administrationDate)
  {
    MedicationsEhrUtils.setMedicineDescription(
        orderActivity,
        therapyDto.getTherapyDescription(),
        therapyDto.getMedication().getName(),
        therapyDto.getMedication().getId());
    MedicationsEhrUtils.setMedicationTiming(
        orderActivity,
        therapyDto.getDosingFrequency(),
        therapyDto.getDosingDaysFrequency(),
        therapyDto.getDaysOfWeek(),
        therapyDto.getStart(),
        therapyDto.getEnd(),
        administrationTime,
        administrationDate,
        therapyDto.getWhenNeeded(),
        therapyDto.getStartCriterions(),
        therapyDto.getMaxDailyFrequency());
    orderActivity.getComment().add(DataValueUtils.getText(therapyDto.getComment()));
    for (final String warning : therapyDto.getCriticalWarnings())
    {
      final String warningCommentPrefix = TherapyCommentEnum.getFullString(TherapyCommentEnum.WARNING) + " ";
      orderActivity.getComment().add(DataValueUtils.getText(warningCommentPrefix + warning));
    }
    orderActivity.getClinicalIndication().add(DataValueUtils.getText(therapyDto.getClinicalIndication()));
    if (therapyDto.getPastDaysOfTherapy() != null)
    {
      orderActivity.setPastDaysOfTherapy(new DvCount());
      orderActivity.getPastDaysOfTherapy().setMagnitude(therapyDto.getPastDaysOfTherapy());
    }
    final AdministrationDetailsCluster administration = MedicationsEhrUtils.createAdministrationDetailsFor(orderActivity);
    MedicationsEhrUtils.setRoute(administration, therapyDto.getRoute());

    orderActivity.setStructuredDose(
        MedicationsEhrUtils.buildStructuredDose(
            doseElement.getQuantity(),
            therapyDto.getQuantityUnit(),
            doseElement.getQuantityDenominator(),
            therapyDto.getQuantityDenominatorUnit(),
            doseElement.getDoseDescription()));

    final IngredientsAndFormCluster ingredientsAndForm = MedicationsEhrUtils.createIngredientsAndFormFor(orderActivity);
    MedicationsEhrUtils.setMedicationForm(ingredientsAndForm, therapyDto.getDoseForm());
  }

  protected abstract void fillDoseElements(final M therapy, final MedicationInstructionInstruction instruction);
}
