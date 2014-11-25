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

import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.StructuredDoseCluster;
import com.marand.thinkmed.medications.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.dto.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationSiteDto;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster.IngredientQuantityCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.InfusionAdministrationDetailsPurpose;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;
/**
 * @author Bostjan Vester
 */
public abstract class ComplexMedicationFromEhrConverter<M extends ComplexTherapyDto> extends MedicationFromEhrConverter<M>
{
  @Override
  public final M createTherapyFromInstruction(
      final MedicationInstructionInstruction instruction,
      final String compositionId,
      final String ehrOrderName,
      final DateTime createdTimestamp,
      final DateTime when,
      final MedicationDataProvider medicationDataProvider)
  {
    final M result = createEmptyTherapyDto();

    final OrderActivity representingOrderActivity = instruction.getOrder().get(0); // All common properties are duplicated through all order activities

    fillTherapyDto(result, representingOrderActivity, createdTimestamp, compositionId, ehrOrderName);

    result.setTherapyDescription(DvUtils.getString(instruction.getNarrative()));

    final AdministrationDetailsCluster administration = representingOrderActivity.getAdministrationDetails();

    result.setContinuousInfusion(
        MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION.isEqualTo(administration.getDeliveryMethod()));

    if (!administration.getInfusionAdministrationDetails().isEmpty())
    {
      final InfusionAdministrationDetailsCluster infusionDetails = administration.getInfusionAdministrationDetails().get(0);
      result.setBaselineInfusion(infusionDetails.getPurposeEnum() == InfusionAdministrationDetailsPurpose.BASELINE_ELECTROLYTE_INFUSION);
    }

    if (administration.getSite() != null)
    {
      final MedicationSiteDto siteDto = new MedicationSiteDto();
      siteDto.setCode(administration.getSite().getDefiningCode().getCodeString());
      siteDto.setName(administration.getSite().getValue());
      result.setSite(siteDto);
    }

    fillDoseElements(result, instruction.getOrder(), when, medicationDataProvider);

    result.setIngredientsList(createIngredients(when, medicationDataProvider, representingOrderActivity));

    final StructuredDoseCluster structuredDose = representingOrderActivity.getStructuredDose();
    if (structuredDose != null)
    {
      result.setVolumeSum(structuredDose.getQuantity().getMagnitude());
      result.setVolumeSumUnit(DvUtils.getString(structuredDose.getDoseUnit()));
    }
    if (!representingOrderActivity.getAdditionalInstruction().isEmpty())
    {
      for (final DvText additionalInstruction : representingOrderActivity.getAdditionalInstruction())
      {
        if (additionalInstruction instanceof DvCodedText)
        {
          final String additionalInstructionEnumString =
              ((DvCodedText)additionalInstruction).getDefiningCode().getCodeString();
          final MedicationAdditionalInstructionEnum additionalInstructionEnum =
              MedicationAdditionalInstructionEnum.getByFullString(additionalInstructionEnumString);
          if (additionalInstructionEnum != null)
          {
            if (additionalInstructionEnum == MedicationAdditionalInstructionEnum.ADJUST_TO_FLUID_BALANCE)
            {
              result.setAdjustToFluidBalance(true);
            }
            else
            {
              result.setAdditionalInstruction(additionalInstructionEnum.name());
            }
          }
          else
          {
            throw new IllegalArgumentException("Unknown additional instruction " + additionalInstructionEnumString);
          }
        }
        else
        {
          throw new IllegalArgumentException(
              "Additional instruction " + additionalInstruction.getValue() + " is not DvCodedText");
        }
      }
    }

    return result;
  }

  protected abstract void fillDoseElements(
      final M result,
      final List<OrderActivity> orderActivities,
      final DateTime when,
      final MedicationDataProvider medicationDataProvider);

  protected final List<InfusionIngredientDto> createIngredients(
      final DateTime when,
      final MedicationDataProvider medicationDataProvider,
      final OrderActivity orderActivity)
  {
    final List<InfusionIngredientDto> ingredients = new ArrayList<>();
    final IngredientsAndFormCluster ingredientsAndForm = orderActivity.getIngredientsAndForm();
    for (final IngredientsAndFormCluster.IngredientCluster ingredientCluster : ingredientsAndForm.getIngredient())
    {
      final InfusionIngredientDto infusionIngredient = new InfusionIngredientDto();

      if (ingredientCluster.getName() instanceof DvCodedText)   //if DvCodedText then medication exists in database
      {
        final String definingCode = ((DvCodedText)ingredientCluster.getName()).getDefiningCode().getCodeString();
        final Long medicationId = Long.parseLong(definingCode);
        infusionIngredient.setMedication(medicationDataProvider.getMedication(medicationId, when));
      }
      else
      {
        final MedicationDto medication = new MedicationDto();
        medication.setName(ingredientCluster.getName().getValue());
        infusionIngredient.setMedication(medication);
      }

      if (ingredientCluster.getForm() != null)
      {
        final DoseFormDto doseFormDto = new DoseFormDto();
        doseFormDto.setCode(((DvCodedText)ingredientCluster.getForm()).getDefiningCode().getCodeString());
        doseFormDto.setName(ingredientCluster.getForm().getValue());
        infusionIngredient.setDoseForm(doseFormDto);
      }

      final IngredientQuantityCluster ingredientQuantity = ingredientCluster.getIngredientQuantity();
      if (ingredientQuantity != null)
      {
        final IngredientQuantityCluster.RatioNumeratorCluster ratioNumerator = ingredientQuantity.getRatioNumerator();
        final IngredientQuantityCluster.RatioDenominatorCluster ratioDenominator = ingredientQuantity.getRatioDenominator();
        if (ratioNumerator != null)
        {
          final Double quantityAmount = ratioNumerator.getAmount().getMagnitude();
          final String quantityUnit = ratioNumerator.getDoseUnit().getDefiningCode().getCodeString();
          final Double volumeAmount = ratioDenominator.getAmount().getMagnitude();
          final String volumeUnit = ratioDenominator.getDoseUnit().getDefiningCode().getCodeString();

          infusionIngredient.setQuantity(quantityAmount);
          infusionIngredient.setQuantityUnit(quantityUnit);
          infusionIngredient.setVolume(volumeAmount);
          infusionIngredient.setVolumeUnit(volumeUnit);
        }
        else
        {
          final Double amount = ingredientQuantity.getQuantity().getMagnitude();
          final String unit = ingredientQuantity.getDoseUnit().getDefiningCode().getCodeString();

          if ("ml".equals(unit)) // TODO: determine if this is a volume amount in a more appropriate way!
          {
            infusionIngredient.setVolume(amount);
            infusionIngredient.setVolumeUnit(unit);
          }
          else
          {
            infusionIngredient.setQuantity(amount);
            infusionIngredient.setQuantityUnit(unit);
          }
        }
      }
      ingredients.add(infusionIngredient);
    }
    return ingredients;
  }

  protected final void fillDoseElementFromOrderActivity(
      final ComplexDoseElementDto doseElement,
      final OrderActivity orderActivity,
      final boolean specificsPrefilled)
  {
    if (!specificsPrefilled)
    {
      final AdministrationDetailsCluster administration = orderActivity.getAdministrationDetails();

      final Period duration = DvUtils.getDuration(administration.getDoseDuration());
      if (duration != null)
      {
        doseElement.setDuration(duration.toStandardMinutes().getMinutes());
      }

      if (!administration.getInfusionAdministrationDetails().isEmpty())
      {
        final InfusionAdministrationDetailsCluster infusionDetails = administration.getInfusionAdministrationDetails().get(0);
        if (infusionDetails.getDoseAdministrationRate() instanceof DvQuantity)
        {
          final DvQuantity administrationRateQuantity = (DvQuantity)infusionDetails.getDoseAdministrationRate();
          doseElement.setRate(administrationRateQuantity.getMagnitude());
          doseElement.setRateUnit(administrationRateQuantity.getUnits());
        }
        if (infusionDetails.getDoseAdministrationFormula() instanceof DvQuantity)
        {
          final DvQuantity administrationRateFormula = (DvQuantity)infusionDetails.getDoseAdministrationFormula();
          doseElement.setRateFormula(administrationRateFormula.getMagnitude());
          doseElement.setRateFormulaUnit(administrationRateFormula.getUnits());
        }
      }
    }
  }
}
