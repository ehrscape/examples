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

import java.util.Collection;

import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.TherapyCommentEnum;
import com.marand.thinkmed.medications.business.impl.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import org.openehr.jaxb.rm.DvCount;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;

/**
 * @author Bostjan Vester
 */
public abstract class ComplexMedicationToEhrConverter<M extends ComplexTherapyDto> extends MedicationToEhrConverter<M>
{
  @Override
  public final void fillInstructionFromTherapy(
      final MedicationInstructionInstruction instruction, final M therapy)
  {
    instruction.setNarrative(DataValueUtils.getText(therapy.getTherapyDescription()));
    instruction.getOrder().clear();
    for (final Pair<ComplexDoseElementDto, HourMinuteDto> timedDoseElement : getDoseElements(therapy))
    {
      final OrderActivity orderActivity = MedicationsEhrUtils.createEmptyOrderActivityFor(instruction);
      fillOrderActivityFromTherapyDto(orderActivity, therapy, timedDoseElement.getFirst(), timedDoseElement.getSecond());
    }
  }

  private void fillOrderActivityFromTherapyDto(
      final OrderActivity orderActivity,
      final M therapyDto,
      final ComplexDoseElementDto doseElement,
      final HourMinuteDto administrationTime)
  {
    MedicationsEhrUtils.setMedicineDescription(orderActivity, therapyDto.getTherapyDescription(), null, null);
    MedicationsEhrUtils.setMedicationTiming(
        orderActivity,
        therapyDto.getDosingFrequency(),
        therapyDto.getDosingDaysFrequency(),
        therapyDto.getDaysOfWeek(),
        therapyDto.getStart(),
        therapyDto.getEnd(),
        administrationTime,
        null,
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
    MedicationsEhrUtils.setSite(administration, therapyDto.getSite());

    if (!isSpecificSpeedHandled(therapyDto, administration))
    {
      if (doseElement.getRate() != null || doseElement.getRateFormula() != null || therapyDto.isBaselineInfusion())
      {
        MedicationsEhrUtils.addInfusionAdministrationDetails(
            administration,
            therapyDto.isBaselineInfusion(),
            null,
            doseElement.getRate(),
            doseElement.getRateUnit(),
            doseElement.getRateFormula(),
            doseElement.getRateFormulaUnit());
      }
      if (doseElement.getDuration() != null)
      {
        MedicationsEhrUtils.setDuration(administration, doseElement.getDuration());
      }
    }
    if (therapyDto.isContinuousInfusion())
    {
      administration.setDeliveryMethod(
          DataValueUtils.getText(
              MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION)));
    }

    if (therapyDto.getVolumeSum() != null)
    {
      orderActivity.setStructuredDose(
          MedicationsEhrUtils.buildStructuredDose(
              therapyDto.getVolumeSum(), therapyDto.getVolumeSumUnit(), null, null, null));
    }

    if (therapyDto.getAdditionalInstruction() != null)
    {
      final MedicationAdditionalInstructionEnum additionalInstructionEnum =
          MedicationAdditionalInstructionEnum.valueOf(therapyDto.getAdditionalInstruction());
      if (additionalInstructionEnum != null)
      {
        final String enumString = MedicationAdditionalInstructionEnum.getFullString(additionalInstructionEnum);
        orderActivity.getAdditionalInstruction().add(DataValueUtils.getLocalCodedText(enumString, enumString));
      }
      else
      {
        throw new IllegalArgumentException("Unknown additional instruction:" + therapyDto.getAdditionalInstruction());
      }
    }
    if (therapyDto.isAdjustToFluidBalance())
    {
      final String enumString =
          MedicationAdditionalInstructionEnum.getFullString(MedicationAdditionalInstructionEnum.ADJUST_TO_FLUID_BALANCE);
      orderActivity.getAdditionalInstruction().add(DataValueUtils.getLocalCodedText(enumString, enumString));
    }

    final IngredientsAndFormCluster ingredientsAndForm = MedicationsEhrUtils.createIngredientsAndFormFor(orderActivity);
    for (final InfusionIngredientDto ingredientDto : therapyDto.getIngredientsList())
    {
      final IngredientsAndFormCluster.IngredientCluster ingredient = MedicationsEhrUtils.addIngredientTo(ingredientsAndForm);
      MedicationsEhrUtils.setName(
          ingredient, ingredientDto.getMedication().getName(), ingredientDto.getMedication().getId());
      MedicationsEhrUtils.setIngredientForm(ingredient, ingredientDto.getDoseForm());
      MedicationsEhrUtils.setQuantityWithVolume(
          ingredient,
          ingredientDto.getQuantity(),
          ingredientDto.getQuantityUnit(),
          ingredientDto.getVolume(),
          ingredientDto.getVolumeUnit());
    }
  }

  protected abstract boolean isSpecificSpeedHandled(
      final M therapyDto,
      final AdministrationDetailsCluster administration);

  protected abstract Collection<Pair<ComplexDoseElementDto, HourMinuteDto>> getDoseElements(final M therapy);
}
