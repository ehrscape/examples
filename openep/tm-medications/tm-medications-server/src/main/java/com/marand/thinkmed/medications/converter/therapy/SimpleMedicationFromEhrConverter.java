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

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.StructuredDoseCluster;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;

/**
 * @author Bostjan Vester
 */
public abstract class SimpleMedicationFromEhrConverter<M extends SimpleTherapyDto> extends MedicationFromEhrConverter<M>
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

    final OrderActivity representingOrderActivity = MedicationsEhrUtils.getRepresentingOrderActivity(instruction); // All common properties are duplicated through all order activities

    fillTherapyDto(result, representingOrderActivity, createdTimestamp, compositionId, ehrOrderName, medicationDataProvider);
    result.setLinkedToAdmission(
        !MedicationsEhrUtils.getLinksOfType(
            instruction,
            EhrLinkType.MEDICATION_ON_ADMISSION)
            .isEmpty());

    result.setMedication(buildMedicationFromOrderForSimpleTherapy(representingOrderActivity, medicationDataProvider));
    result.setCompositionUid(compositionId);
    result.setEhrOrderName(ehrOrderName);

    if (representingOrderActivity.getIngredientsAndForm() != null)
    {
      final String doseFormCode =
          ((DvCodedText)representingOrderActivity.getIngredientsAndForm().getForm()).getDefiningCode().getCodeString();

      if (result.getMedication().getId() != null)
      {
        result.setDoseForm(medicationDataProvider.getMedicationDoseForm(result.getMedication().getId()));
      }
      else
      {
        result.setDoseForm(medicationDataProvider.getDoseForm(doseFormCode, when));
      }
    }

    fillDoseElements(result, instruction.getOrder());

    final StructuredDoseCluster structuredDose = representingOrderActivity.getStructuredDose();
    if (structuredDose != null)
    {
      if (structuredDose.getQuantity() != null)
      {
        result.setQuantityUnit(structuredDose.getDoseUnit().getDefiningCode().getCodeString());
      }
      else if (structuredDose.getRatioNumerator() != null)
      {
        result.setQuantityUnit(
            structuredDose.getRatioNumerator().getDoseUnit().getDefiningCode().getCodeString());
        result.setQuantityDenominatorUnit(
            structuredDose.getRatioDenominator().getDoseUnit().getDefiningCode().getCodeString());
      }
    }

    result.setApplicationPrecondition(getApplicationPreconditionFromOrderForSimpleTherapy(representingOrderActivity));

    fillSelfAdministerDataFromEhr(instruction, result);

    result.setDoseType(TherapyDoseTypeEnum.QUANTITY);
    return result;
  }

  protected abstract void fillDoseElements(
      final M result,
      final List<OrderActivity> orderActivities);

  protected SimpleDoseElementDto createSimpleDoseElement(final OrderActivity orderActivity)
  {
    final StructuredDoseCluster structuredDose = orderActivity.getStructuredDose();
    final SimpleDoseElementDto doseElement = new SimpleDoseElementDto();
    if (structuredDose != null)
    {
      if (MedicationsEhrUtils.isDoseRangeStructuredDose(structuredDose))
      {
        doseElement.setDoseRange(MedicationsEhrUtils.buildDoseRangeFromStructuredDose(structuredDose));
      }
      else
      {
        if (structuredDose.getQuantity() != null)
        {
          doseElement.setQuantity(((DvQuantity)structuredDose.getQuantity()).getMagnitude());
        }
        else if (structuredDose.getRatioNumerator() != null)
        {
          doseElement.setQuantity(((DvQuantity)structuredDose.getRatioNumerator().getAmount()).getMagnitude());
          doseElement.setQuantityDenominator(((DvQuantity)structuredDose.getRatioDenominator().getAmount()).getMagnitude());
        }
      }
      doseElement.setDoseDescription(DvUtils.getString(structuredDose.getDescription()));
    }
    return doseElement;
  }
}
