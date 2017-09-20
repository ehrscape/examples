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

import com.marand.maf.core.CollectionUtils;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.TherapyCommentEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import org.openehr.jaxb.rm.DvCount;

/**
 * @author Bostjan Vester
 */
public abstract class ComplexMedicationToEhrConverter<M extends ComplexTherapyDto> extends MedicationToEhrConverter<M>
{
  @Override
  public final void fillInstructionFromTherapy(final MedicationInstructionInstruction instruction, final M therapy)
  {
    instruction.setNarrative(DataValueUtils.getText(therapy.getTherapyDescription()));
    instruction.getOrder().clear();

    fillDoseElements(therapy, instruction);

    fillBnfToEhr(instruction, therapy);
    fillSelfAdministrationDataToEhr(instruction, therapy);
  }

  protected void fillOrderActivityFromTherapyDto(
      final OrderActivity orderActivity,
      final M therapyDto,
      final ComplexDoseElementDto doseElement,
      final List<HourMinuteDto> administrationTimes)
  {
    MedicationsEhrUtils.setMedicineDescription(orderActivity, therapyDto.getTherapyDescription(), null, null);
    MedicationsEhrUtils.setMedicationTiming(
        orderActivity,
        therapyDto.getDosingFrequency(),
        therapyDto.getDosingDaysFrequency(),
        therapyDto.getDaysOfWeek(),
        therapyDto.getStart(),
        therapyDto.getEnd(),
        administrationTimes,
        null,
        therapyDto.getWhenNeeded(),
        therapyDto.getStartCriterion(),
        therapyDto.getMaxDailyFrequency());

    orderActivity.getComment().add(DataValueUtils.getText(therapyDto.getComment()));
    for (final String warning : therapyDto.getCriticalWarnings())
    {
      final String warningCommentPrefix = TherapyCommentEnum.getFullString(TherapyCommentEnum.WARNING) + " ";
      orderActivity.getComment().add(DataValueUtils.getText(warningCommentPrefix + warning));
    }
    MedicationsEhrUtils.setClinicalIndication(orderActivity, therapyDto.getClinicalIndication());

    if (therapyDto.getPastDaysOfTherapy() != null)
    {
      orderActivity.setPastDaysOfTherapy(new DvCount());
      orderActivity.getPastDaysOfTherapy().setMagnitude(therapyDto.getPastDaysOfTherapy());
    }
    MedicationsEhrUtils.setApplicationPrecondition(orderActivity, therapyDto.getApplicationPrecondition());

    final AdministrationDetailsCluster administration = MedicationsEhrUtils.createAdministrationDetailsFor(orderActivity);
    if (!CollectionUtils.isEmpty(therapyDto.getRoutes()))
    {
      MedicationsEhrUtils.setRoutes(administration, therapyDto.getRoutes());
    }

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
      final boolean recurringContinuousInfusion = isRecurringContinuousInfusion(therapyDto);
      final MedicationDeliveryMethodEnum deliveryMethodEnum =
          recurringContinuousInfusion ?
          MedicationDeliveryMethodEnum.RECURRING_CONTINUOUS_INFUSION :
          MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION;
      administration.setDeliveryMethod(
          DataValueUtils.getText(
              MedicationDeliveryMethodEnum.getFullString(deliveryMethodEnum)));
    }

    if (therapyDto.getVolumeSum() != null)
    {
      orderActivity.setStructuredDose(MedicationsEhrUtils.buildStructuredDose(
          therapyDto.getVolumeSum(),
          therapyDto.getVolumeSumUnit()));
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

      ingredient.setRoleEnum(getMedicationRole(ingredientDto.getMedication()));

      MedicationsEhrUtils.setIngredientForm(ingredient, ingredientDto.getDoseForm());

      MedicationsEhrUtils.setQuantity(
          ingredient,
          ingredientDto.getQuantity(),
          ingredientDto.getQuantityUnit(),
          ingredientDto.getQuantityDenominator(),
          ingredientDto.getQuantityDenominatorUnit());
    }
  }

  private PharmacyReviewReportComposition.MiscellaneousSection.Role getMedicationRole(final MedicationDto medication)
  {
    final MedicationTypeEnum medicationType = medication.getMedicationType();
    PharmacyReviewReportComposition.MiscellaneousSection.Role role = null;

    if (medicationType == MedicationTypeEnum.MEDICATION)
    {
      role = PharmacyReviewReportComposition.MiscellaneousSection.Role.THERAPEUTIC;
    }
    else if (medicationType == MedicationTypeEnum.SOLUTION)
    {
      role = PharmacyReviewReportComposition.MiscellaneousSection.Role.DILUTANT;
    }
    return role;
  }

  protected abstract boolean isSpecificSpeedHandled(final M therapyDto, final AdministrationDetailsCluster administration);

  protected abstract boolean isRecurringContinuousInfusion(final M therapy);

  protected abstract void fillDoseElements(final M therapy, final MedicationInstructionInstruction instruction);
}
