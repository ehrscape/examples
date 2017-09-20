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
import java.util.Objects;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.InfusionAdministrationDetailsPurpose;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.medications.tdo.StructuredDoseCluster;
import com.marand.thinkmed.medications.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationSiteDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster.IngredientQuantityCluster;
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

    fillBnfFromEhr(instruction, result);

    final OrderActivity representingOrderActivity = instruction.getOrder().get(0); // All common properties are duplicated through all order activities
    final List<OrderActivity> allOrderActivities = instruction.getOrder();

    fillTherapyDto(result, representingOrderActivity, createdTimestamp, compositionId, ehrOrderName, medicationDataProvider);
    result.setLinkedToAdmission(
        !MedicationsEhrUtils.getLinksOfType(
            instruction,
            EhrLinkType.MEDICATION_ON_ADMISSION)
            .isEmpty());

    result.setTherapyDescription(DvUtils.getString(instruction.getNarrative()));

    final AdministrationDetailsCluster administration = representingOrderActivity.getAdministrationDetails();

    result.setContinuousInfusion(MedicationDeliveryMethodEnum.isContinuousInfusion(administration.getDeliveryMethod()));
    fillAdministrationDetails(administration, result);

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

    fillDoseElements(result, instruction.getOrder(), medicationDataProvider);

    result.setIngredientsList(createIngredients(medicationDataProvider, allOrderActivities));

    final StructuredDoseCluster structuredDose = representingOrderActivity.getStructuredDose();
    if (structuredDose != null)
    {
      result.setVolumeSum(((DvQuantity)structuredDose.getQuantity()).getMagnitude());
      result.setVolumeSumUnit(DvUtils.getString(structuredDose.getDoseUnit()));
    }
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
          else if (MedicationAdditionalInstructionEnum.HEPARIN.contains(additionalInstructionEnum))
          {
            result.setAdditionalInstruction(additionalInstructionEnum.name());
          }
          else if (MedicationAdditionalInstructionEnum.APPLICATION_PRECONDITION.contains(additionalInstructionEnum))
          {
            result.setApplicationPrecondition(additionalInstructionEnum.name());
          }
        }
      }
      else
      {
        throw new IllegalArgumentException(
            "Additional instruction " + additionalInstruction.getValue() + " is not DvCodedText");
      }
    }

    fillSelfAdministerDataFromEhr(instruction, result);
    result.setDoseType(getDoseType(instruction, administration));

    return result;
  }

  private TherapyDoseTypeEnum getDoseType(
      final MedicationInstructionInstruction instruction,
      final AdministrationDetailsCluster administrationDetails)
  {
    if (MedicationsEhrUtils.isContinuousInfusion(instruction))
    {
      return TherapyDoseTypeEnum.RATE;
    }

    final boolean multipleIngredients = instruction.getOrder().get(0).getIngredientsAndForm().getIngredient().size() > 1;
    final boolean hasRate = administrationDetails.getInfusionAdministrationDetails()
        .stream()
        .filter(Objects::nonNull)
        .findFirst()
        .map(InfusionAdministrationDetailsCluster::getDoseAdministrationRate)
        .filter(r -> !isBolus(r))
        .isPresent();

    if (multipleIngredients)
    {
      return hasRate ? TherapyDoseTypeEnum.RATE_VOLUME_SUM : TherapyDoseTypeEnum.VOLUME_SUM;
    }
    else
    {
      return hasRate ? TherapyDoseTypeEnum.RATE_QUANTITY : TherapyDoseTypeEnum.QUANTITY;
    }
  }

  private boolean isBolus(final DataValue doseAdministrationRate)
  {
    return doseAdministrationRate instanceof DvText &&
        MedicationsEhrUtils.BOLUS.equals(((DvText)doseAdministrationRate).getValue());
  }

  protected abstract void fillDoseElements(
      final M result,
      final List<OrderActivity> orderActivities,
      final MedicationDataProvider medicationDataProvider);

  protected final List<InfusionIngredientDto> createIngredients(
      final MedicationDataProvider medicationDataProvider,
      final List<OrderActivity> allOrderActivities)
  {
    final OrderActivity orderActivity = allOrderActivities.get(0);
    final List<InfusionIngredientDto> ingredients = new ArrayList<>();
    final IngredientsAndFormCluster ingredientsAndForm = orderActivity.getIngredientsAndForm();
    for (final IngredientsAndFormCluster.IngredientCluster ingredientCluster : ingredientsAndForm.getIngredient())
    {
      final InfusionIngredientDto infusionIngredient = new InfusionIngredientDto();

      if (ingredientCluster.getName() instanceof DvCodedText)   //if DvCodedText then medication exists in database
      {
        final String definingCode = ((DvCodedText)ingredientCluster.getName()).getDefiningCode().getCodeString();
        final Long medicationId = Long.parseLong(definingCode);
        infusionIngredient.setMedication(medicationDataProvider.getMedication(medicationId));
      }
      else
      {
        final MedicationDto medication = new MedicationDto();
        medication.setName(ingredientCluster.getName().getValue());
        if (ingredientCluster.getRoleEnum() == PharmacyReviewReportComposition.MiscellaneousSection.Role.DILUTANT)
        {
          medication.setMedicationType(MedicationTypeEnum.SOLUTION);
        }
        else
        {
          medication.setMedicationType(MedicationTypeEnum.MEDICATION);
        }
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
          infusionIngredient.setQuantityDenominator(volumeAmount);
          infusionIngredient.setQuantityDenominatorUnit(volumeUnit);
        }
        else
        {
          final Double amount = ingredientQuantity.getQuantity().getMagnitude();
          final String unit = ingredientQuantity.getDoseUnit().getDefiningCode().getCodeString();
          infusionIngredient.setQuantity(amount);
          infusionIngredient.setQuantityUnit(unit);
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

  protected abstract void fillAdministrationDetails(final AdministrationDetailsCluster administration, final M therapyDto);
}
