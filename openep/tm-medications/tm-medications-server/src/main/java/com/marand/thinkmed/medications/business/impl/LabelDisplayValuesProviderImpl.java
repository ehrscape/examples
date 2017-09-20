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

package com.marand.thinkmed.medications.business.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.business.LabelDisplayValuesProvider;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Klavdij Lapajne
 */
public class LabelDisplayValuesProviderImpl implements LabelDisplayValuesProvider
{
  private MedicationsConnector medicationsConnector;

  @Required
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Override
  public String getPrescribedByString(final String composerName, final Locale locale)
  {
    return getActionWithUserString("prescribed.by", composerName, locale);
  }

  @Override
  public String getPreparedByString(final String userName, final Locale locale)
  {
    return getActionWithUserString("prepared.by", userName, locale);
  }

  @Override
  public String getTherapyDisplayValueForPerfusionSyringeLabel(final TherapyDto therapy, final Locale locale)
  {
    final List<Pair<String, String>>  medicationNamesWithDoses = getTherapyMedicationNamesWithDoses(therapy);
    final String volumeSumDisplay =
        therapy instanceof ComplexTherapyDto
        ? ((ComplexTherapyDto)therapy).getVolumeSumDisplay()
        : null;
    final String additionalInstructionDisplay =
        therapy instanceof ComplexTherapyDto
        ? ((ComplexTherapyDto)therapy).getAdditionalInstructionDisplay()
        : null;

    return getTherapyDisplayValueForPerfusionSyringeLabel(
        medicationNamesWithDoses,
        volumeSumDisplay,
        additionalInstructionDisplay,
        locale);
  }

  private String getActionWithUserString(final String actionDictionaryKey, final String user, final Locale locale)
  {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(Dictionary.getEntry(actionDictionaryKey, locale).toUpperCase());
    stringBuilder.append(": ");
    stringBuilder.append(user);
    return stringBuilder.toString();
  }

  private List<Pair<String, String>> getTherapyMedicationNamesWithDoses(final TherapyDto therapy)
  {
    final List<Pair<String, String>> medicationNamesWithDoses = new ArrayList<>();
    if (therapy instanceof SimpleTherapyDto)
    {
      final MedicationDto medication = ((SimpleTherapyDto)therapy).getMedication();
      medicationNamesWithDoses.add(Pair.of(medication.getName(), ((SimpleTherapyDto)therapy).getQuantityDisplay()));
    }
    if (therapy instanceof ComplexTherapyDto)
    {
      final List<InfusionIngredientDto> ingredientsList = ((ComplexTherapyDto)therapy).getIngredientsList();
      for (final InfusionIngredientDto infusionIngredientDto : ingredientsList)
      {
        medicationNamesWithDoses.add(
            Pair.of(infusionIngredientDto.getMedication().getName(), infusionIngredientDto.getQuantityDisplay()));
      }
    }
    return medicationNamesWithDoses;
  }

  private String getTherapyDisplayValueForPerfusionSyringeLabel(
      final List<Pair<String, String>> medicationNamesWithDoses,
      final String volumeSumDisplay,
      final String additionalInstructionDisplay,
      final Locale locale)
  {
    final StringBuilder therapyDisplayValue = new StringBuilder();

    for (final Pair<String, String> medicationNameAndDose : medicationNamesWithDoses)
    {
      final StringBuilder medicationNameWithDoseDisplay = new StringBuilder();
      medicationNameWithDoseDisplay.append(medicationNameAndDose.getFirst());
      if (medicationNameAndDose.getSecond() != null)
      {
        medicationNameWithDoseDisplay.append(" — <b>");
        medicationNameWithDoseDisplay.append(medicationNameAndDose.getSecond());
        medicationNameWithDoseDisplay.append("</b>");
      }
      if (!therapyDisplayValue.toString().isEmpty())
      {
        therapyDisplayValue.append(" ● ");
      }
      therapyDisplayValue.append(medicationNameWithDoseDisplay);
    }
    if (volumeSumDisplay != null)
    {
      therapyDisplayValue.append(" ● ");
      therapyDisplayValue.append(Dictionary.getEntry("volume.total", locale).toUpperCase());
      therapyDisplayValue.append(": <b>");
      therapyDisplayValue.append(volumeSumDisplay);
      therapyDisplayValue.append("</b>");
    }
    if (additionalInstructionDisplay != null)
    {
      therapyDisplayValue.append("<b>");
      therapyDisplayValue.append(" + ");
      therapyDisplayValue.append(additionalInstructionDisplay);
      therapyDisplayValue.append("</b>");
    }
    return therapyDisplayValue.toString();
  }
}
