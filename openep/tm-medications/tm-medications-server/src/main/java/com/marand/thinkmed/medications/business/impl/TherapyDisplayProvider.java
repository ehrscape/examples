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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Preconditions;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.formatter.NumberFormatters;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.b2b.MedicationsConnector;
import com.marand.thinkmed.medications.dto.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Mitja Lapajne
 */
public class TherapyDisplayProvider implements InitializingBean
{
  private MedicationsConnector medicationsConnector;

  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    Assert.notNull(medicationsConnector, "medicationsConnector is required!");
  }

  public void fillDisplayValues(
      final TherapyDto order,
      final boolean showLongFormattedDescription,
      final boolean showTherapyInterval,
      final Locale locale)
  {
    try
    {
      final String frequencyDisplay = getFrequencyDisplay(order.getDosingFrequency(), locale);
      order.setFrequencyDisplay(frequencyDisplay);

      final String whenNeededDisplay = getWhenNeededDisplay(order.getWhenNeeded(), locale);
      order.setWhenNeededDisplay(whenNeededDisplay);

      final String startCriterionDisplay = getStartCriterionDisplay(order.getStartCriterions(), locale);
      order.setStartCriterionDisplay(startCriterionDisplay);

      final String daysOfWeekDisplay = getMedicationDaysOfWeekDisplay(order.getDaysOfWeek(), locale);
      order.setDaysOfWeekDisplay(daysOfWeekDisplay);

      final String daysFrequencyDisplay = getDaysFrequencyDisplay(order.getDosingDaysFrequency(), locale);
      order.setDaysFrequencyDisplay(daysFrequencyDisplay);

      if (order instanceof SimpleTherapyDto)
      {
        fillSimpleOrderDisplayValues((SimpleTherapyDto)order, locale);
      }
      else
      {
        fillComplexOrderDisplayValues((ComplexTherapyDto)order, locale);
      }
      if (showLongFormattedDescription)
      {
        order.setFormattedTherapyDisplay(getTherapyFormattedDescription(order, showTherapyInterval, locale));
      }
      else
      {
        order.setFormattedTherapyDisplay(getTherapyFormattedShortDescription(order, false, locale));
      }
    }
    catch (final ParseException e)
    {
      throw new IllegalArgumentException(e);
    }
  }

  //fillDisplayValues must be called first!!
  private String getTherapyFormattedDescription(
      final TherapyDto therapy,
      final boolean showTherapyInterval, final Locale locale)
  {
    if (therapy instanceof SimpleTherapyDto)
    {
      return getSimpleTherapyFormatted((SimpleTherapyDto)therapy, showTherapyInterval, locale);
    }
    if (therapy instanceof ComplexTherapyDto)
    {
      return getComplexTherapyFormatted((ComplexTherapyDto)therapy, showTherapyInterval, locale);
    }
    return "";
  }

  //fillDisplayValues must be called first!!
  private String getTherapyFormattedShortDescription(
      final TherapyDto therapy,
      final boolean showTherapyInterval,
      final Locale locale) throws ParseException
  {
    if (therapy instanceof SimpleTherapyDto)
    {
      return getShortSimpleTherapyFormatted((SimpleTherapyDto)therapy, showTherapyInterval, locale);
    }
    if (therapy instanceof ComplexTherapyDto)
    {
      return getShortComplexTherapyFormatted((ComplexTherapyDto)therapy, showTherapyInterval, locale);
    }
    return "";
  }

  private String getShortSimpleTherapyFormatted(
      final SimpleTherapyDto therapy,
      final boolean showTherapyInterval,
      final Locale locale)
  {

    final StringBuilder description = new StringBuilder();

    description.append(getMedicationSpannedDisplay(therapy.getMedication(), true, true));
    description.append(addLineBreak());

    final List<String> therapyElementsToDelimit = new ArrayList<>();
    if (StringUtils.isNotEmpty(therapy.getQuantityDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getQuantityDisplay(), "Quantity TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getFrequencyDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getFrequencyDisplay(), "Frequency TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysOfWeekDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getDaysOfWeekDisplay(), "DaysOfWeek TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysFrequencyDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getDaysFrequencyDisplay(), "FrequencyDisplay TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getWhenNeededDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getWhenNeededDisplay(), "WhenNeeded TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getStartCriterionDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getStartCriterionDisplay(), "StartCriterion TextData"));
    }

    if (therapy.getRoute() != null)
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getRoute().getName(), "Route TextData"));
    }

    if (!therapyElementsToDelimit.isEmpty())
    {
      description.append(delimitValues(therapyElementsToDelimit));
    }

    if (showTherapyInterval)
    {
      description.append(getTherapyIntervalDisplay(therapy, locale));
    }
    return description.toString();
  }

  private String getShortComplexTherapyFormatted(
      final ComplexTherapyDto therapy,
      final boolean showTherapyInterval,
      final Locale locale) throws ParseException
  {
    final StringBuilder description = new StringBuilder();

    boolean allIngredientsSolutions = true;
    for (final InfusionIngredientDto ingredient : therapy.getIngredientsList())
    {
      final List<String> ingredientsToDelimit = new ArrayList<>();
      ingredientsToDelimit.add(getMedicationSpannedDisplay(ingredient.getMedication(), false, false));

      description.append(delimitValues(ingredientsToDelimit));
      description.append(addLineBreak());
    }

    final List<String> elementsToDelimit = new ArrayList<>();

    if (therapy.getIngredientsList().size() == 1)
    {
      final InfusionIngredientDto onlyIngredient = therapy.getIngredientsList().get(0);

      if (StringUtils.isNotEmpty(onlyIngredient.getQuantityDisplay()))
      {
        if (onlyIngredient.getMedication().getMedicationType() != null &&
            onlyIngredient.getMedication().getMedicationType() != MedicationTypeEnum.SOLUTION)
        {
          allIngredientsSolutions = false;
          elementsToDelimit.add(
              createSpannedValue(getIngredientQuantityDisplay(onlyIngredient, true, locale), "Quantity TextData"));
        }
        else
        {
          elementsToDelimit.add(createSpannedValue(onlyIngredient.getQuantityDisplay(), "Quantity TextData"));
        }
      }
    }

    if (StringUtils.isNotEmpty(therapy.getVolumeSumDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getVolumeSumDisplay(), "VolumeSum TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getSpeedDisplay()) || therapy.isAdjustToFluidBalance())
    {
      final String speed = therapy.isAdjustToFluidBalance()
                           ? medicationsConnector.getEntry("adjust.to.fluid.balance.short", null, locale)
                           : therapy.getSpeedDisplay();

      elementsToDelimit.add(createSpannedValue(speed, "Speed TextData"));
    }

    if (!allIngredientsSolutions && therapy.isContinuousInfusion() && StringUtils.isNotEmpty(therapy.getSpeedFormulaDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getSpeedFormulaDisplay(), "SpeedFormula TextData"));
    }

    if (therapy instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto constantTherapy = (ConstantComplexTherapyDto)therapy;
      if (StringUtils.isNotEmpty(constantTherapy.getDurationDisplay()))
      {
        elementsToDelimit.add(createSpannedValue(constantTherapy.getDurationDisplay(), "Duration TextData"));
      }
    }

    if (therapy.isContinuousInfusion())
    {
      elementsToDelimit.add(
          createSpannedValue(
              medicationsConnector.getEntry("continuous.infusion", null, locale).toUpperCase(),
              "Infusion TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getFrequencyDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getFrequencyDisplay(), "Frequency TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getWhenNeededDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getWhenNeededDisplay(), "WhenNeeded TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getStartCriterionDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getStartCriterionDisplay(), "StartCriterion TextData"));
    }

    if (therapy.getRoute() != null)
    {
      elementsToDelimit.add(createSpannedValue(therapy.getRoute().getName(), "Route TextData"));
    }

    description.append(delimitValues(elementsToDelimit));

    if (showTherapyInterval)
    {
      description.append(getTherapyIntervalDisplay(therapy, locale));
    }

    return description.toString();
  }

  private String getSimpleTherapyFormatted(
      final SimpleTherapyDto therapy,
      final boolean showTherapyInterval,
      final Locale locale)
  {
    final StringBuilder description = new StringBuilder();

    description.append(getMedicationSpannedDisplay(therapy.getMedication(), true, false));
    description.append(addLineBreak());

    final List<String> therapyElementsToDelimit = new ArrayList<>();
    if (StringUtils.isNotEmpty(therapy.getQuantityDisplay()))
    {
      therapyElementsToDelimit.add(
          createSpannedValue(medicationsConnector.getEntry("dose", null, locale).toUpperCase(), "DoseLabel TextLabel")
              + createSpannedValue(therapy.getQuantityDisplay(), "Quantity TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getFrequencyDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getFrequencyDisplay(), "Frequency TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysOfWeekDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getDaysOfWeekDisplay(), "DaysOfWeek TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysFrequencyDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getDaysFrequencyDisplay(), "FrequencyDisplay TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getWhenNeededDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getWhenNeededDisplay(), "WhenNeeded TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getStartCriterionDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getStartCriterionDisplay(), "StartCriterion TextData"));
    }

    if (therapy.getRoute() != null)
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getRoute().getName(), "Route TextData"));
    }

    if (!therapyElementsToDelimit.isEmpty())
    {
      description.append(delimitValues(therapyElementsToDelimit));
    }

    if (showTherapyInterval)
    {
      description.append(getTherapyIntervalDisplay(therapy, locale));
    }

    if (StringUtils.isNotEmpty(therapy.getComment()))
    {
      description.append(addLineBreak());
      description.append(createSpannedValue(
                             medicationsConnector.getEntry("commentary", null, locale).toUpperCase(),
                             "CommentLabel TextLabel"));
      description.append(createSpannedValue(therapy.getComment(), "Comment TextData"));
      description.append(addLineBreak());
    }

    return description.toString();
  }

  private String getComplexTherapyFormatted(
      final ComplexTherapyDto therapy,
      final boolean showTherapyInterval,
      final Locale locale)
  {
    final StringBuilder description = new StringBuilder();

    if (therapy.isBaselineInfusion())
    {
      description.append(createSpannedValue(therapy.getBaselineInfusionDisplay(), "BaseLineInfusion TextLabel"));
    }

    boolean allIngredientsSolutions = true;
    for (final InfusionIngredientDto ingredient : therapy.getIngredientsList())
    {
      final List<String> ingredientsToDelimit = new ArrayList<>();

      if (ingredient.getMedication().getMedicationType() != MedicationTypeEnum.SOLUTION)
      {
        ingredientsToDelimit.add(getMedicationSpannedDisplay(ingredient.getMedication(), true, false));
        allIngredientsSolutions = false;
      }
      else
      {
        ingredientsToDelimit.add(getMedicationSpannedDisplay(ingredient.getMedication(), false, false));
      }

      if (StringUtils.isNotEmpty(ingredient.getQuantityDisplay()))
      {
        ingredientsToDelimit.add(createSpannedValue(ingredient.getQuantityDisplay(), "Quantity TextData"));
      }

      description.append(delimitValues(ingredientsToDelimit));
      description.append(addLineBreak());
    }

    if (StringUtils.isNotEmpty(therapy.getVolumeSumDisplay()))
    {
      description.append(
          createSpannedValue(medicationsConnector.getEntry("volume.total", null, locale).toUpperCase(), "VolumeSumLabel TextLabel")
              + createSpannedValue(therapy.getVolumeSumDisplay(), "VolumeSum TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getVolumeSumDisplay()) &&
        StringUtils.isNotEmpty(therapy.getAdditionalInstructionDisplay()))
    {
      description.append(" + ");
    }

    if (StringUtils.isNotEmpty(therapy.getAdditionalInstructionDisplay()))
    {
      description.append(createSpannedValue(therapy.getAdditionalInstructionDisplay(), "AdditionalInstruction TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getVolumeSumDisplay()) || StringUtils.isNotEmpty(therapy.getAdditionalInstructionDisplay()))
    {
      description.append(addLineBreak());
    }

    final List<String> elementsToDelimit = new ArrayList<>();

    if (StringUtils.isNotEmpty(therapy.getSpeedDisplay()) || therapy.isAdjustToFluidBalance())
    {
      final String speed = therapy.isAdjustToFluidBalance() ? medicationsConnector.getEntry("adjust.to.fluid.balance.short", null, locale) : therapy.getSpeedDisplay();

      elementsToDelimit.add(
          createSpannedValue(medicationsConnector.getEntry("rate", null, locale).toUpperCase(), "SpeedLabel TextLabel")
              + createSpannedValue(speed, "Speed TextData"));
    }

    if (!allIngredientsSolutions && therapy.isContinuousInfusion() && StringUtils.isNotEmpty(therapy.getSpeedFormulaDisplay()))
    {
      elementsToDelimit.add(
          createSpannedValue(medicationsConnector.getEntry("dose", null, locale).toUpperCase(), "SpeedFormulaLabel TextLabel")
              + createSpannedValue(therapy.getSpeedFormulaDisplay(), "SpeedFormula TextData"));
    }

    if (therapy instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto constantTherapy = (ConstantComplexTherapyDto)therapy;
      if (StringUtils.isNotEmpty(constantTherapy.getDurationDisplay()))
      {
        elementsToDelimit.add(
            createSpannedValue(medicationsConnector.getEntry("duration", null, locale).toUpperCase(), "DurationLabel TextLabel")
                + createSpannedValue(constantTherapy.getDurationDisplay(), "Duration TextData"));
      }
    }

    if (therapy.isContinuousInfusion())
    {
      elementsToDelimit.add(createSpannedValue(
                                medicationsConnector.getEntry("continuous.infusion", null, locale).toUpperCase(),
                                "Infusion TextData"));
    }
    else if (StringUtils.isNotEmpty(therapy.getFrequencyDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(
                                medicationsConnector.getEntry("dosing.interval", null, locale).toUpperCase(),
                                "FrequencyLabel TextLabel"));
      description.append(delimitValues(elementsToDelimit));
      elementsToDelimit.clear();
    }

    if (StringUtils.isNotEmpty(therapy.getFrequencyDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getFrequencyDisplay(), "Frequency TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysOfWeekDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getDaysOfWeekDisplay(), "DaysOfWeek TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysFrequencyDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getDaysFrequencyDisplay().toLowerCase(), "DaysFrequency TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getWhenNeededDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getWhenNeededDisplay(), "WhenNeeded TextData"));
    }

    if (StringUtils.isNotEmpty(therapy.getStartCriterionDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getStartCriterionDisplay(), "StartCriterion TextData"));
    }

    if (therapy.getRoute() != null)
    {
      elementsToDelimit.add(createSpannedValue(therapy.getRoute().getName(), "Route TextData"));
    }

    description.append(delimitValues(elementsToDelimit));

    if (showTherapyInterval)
    {
      description.append(getTherapyIntervalDisplay(therapy, locale));
    }

    if (StringUtils.isNotEmpty(therapy.getComment()))
    {
      description.append(addLineBreak());
      description.append(createSpannedValue(
                             medicationsConnector.getEntry("commentary", null, locale).toUpperCase(),
                             "CommentLabel TextLabel"));
      description.append(createSpannedValue(therapy.getComment(), "Comment TextData"));
      description.append(addLineBreak());
    }

    return description.toString();
  }

  private String getMedicationSpannedDisplay(final MedicationDto medication, final boolean showGeneric, final boolean preferShortName)
  {
    final StringBuilder medicationDisplay = new StringBuilder();

    final String name = preferShortName && medication.getShortName() != null
                        ? medication.getShortName()
                        : medication.getName();

    if (showGeneric && medication.getGenericName() != null)
    {
      medicationDisplay.append(createSpannedValue(medication.getGenericName(), "GenericName TextDataBold"));
      medicationDisplay.append(createSpannedValue("(" + name + ")", "MedicationName TextData"));
    }
    else
    {
      medicationDisplay.append(createSpannedValue(name, "MedicationName TextData"));
    }

    return medicationDisplay.toString();
  }

  private String getTherapyIntervalDisplay(final TherapyDto therapy, final Locale locale)
  {
    final StringBuilder therapyDisplay = new StringBuilder();
    therapyDisplay.append("<span class='TherapyInterval'>");
    therapyDisplay.append(addLineBreak());
    therapyDisplay.append(createSpannedValue(medicationsConnector.getEntry("from", null, locale), "FromLabel TextLabel"));
    therapyDisplay.append(createSpannedValue(DateTimeFormat.forPattern("dd-MMM-yyyy").print(therapy.getStart()), "From TextData"));
    //therapyDisplay.append(createSpannedValue(DateTimeFormatters.shortDateTime().print(therapy.getStart()), "From TextData"));

    if (therapy.getEnd() != null)
    {
      therapyDisplay.append(createSpannedValue(medicationsConnector.getEntry("until.low.case", null, locale), "UntilLabel TextLabel"));
      therapyDisplay.append(createSpannedValue(DateTimeFormat.forPattern("dd-MMM-yyyy").print(therapy.getEnd()), "From TextData"));
      //therapyDisplay.append(createSpannedValue(DateTimeFormatters.shortDateTime().print(therapy.getEnd()), "Until TextData"));
    }
    therapyDisplay.append("</span>");
    return therapyDisplay.toString();
  }

  private String delimitValues(final List<String> valuesToDelimit)
  {
    final StringBuilder delimitedDisplay = new StringBuilder();

    for (final Iterator<String> iterator = valuesToDelimit.iterator(); iterator.hasNext();)
    {
      final String value = iterator.next();
      delimitedDisplay.append(value);

      if (iterator.hasNext())
      {
        delimitedDisplay.append(addDelimiter());
      }
    }

    return delimitedDisplay.toString();
  }

  private String createSpannedValue(final String valueToBeSpanned, final String spanClassValue)
  {
    if (StringUtils.isEmpty(valueToBeSpanned) || StringUtils.isEmpty(spanClassValue))
    {
      return "";
    }
    return "<span class='" + spanClassValue + "'>" + valueToBeSpanned + " " + "</span>";
  }

  private String addLineBreak()
  {
    return "<br>";
  }

  private String addDelimiter()
  {
    return createSpannedValue("<span> &ndash; </span>", "Delimiter TextData");
  }

  private void fillComplexOrderDisplayValues(final ComplexTherapyDto complexOrder, final Locale locale)
      throws ParseException
  {
    final String volumeSumDisplay =
        getVolumeSumDisplay(complexOrder.getVolumeSum(), complexOrder.getVolumeSumUnit(), locale);
    complexOrder.setVolumeSumDisplay(volumeSumDisplay);
    final String speedDisplay = getSpeedDisplay(complexOrder, locale);
    complexOrder.setSpeedDisplay(speedDisplay);

    final String speedFormulaDisplay = getSpeedFormulaDisplay(complexOrder, locale);
    complexOrder.setSpeedFormulaDisplay(speedFormulaDisplay);
    
    for (final InfusionIngredientDto ingredient : complexOrder.getIngredientsList())
    {
      final String ingredientQuantityDisplay = getIngredientQuantityDisplay(ingredient, false, locale);
      ingredient.setQuantityDisplay(ingredientQuantityDisplay);
      final String medicationDisplay = getMedicationDisplay(ingredient.getMedication());
      ingredient.getMedication().setDisplayName(medicationDisplay);
    }

    final String additionInstructionDisplay = getAdditionInstructionDisplay(complexOrder.getAdditionalInstruction(), locale);
    complexOrder.setAdditionalInstructionDisplay(additionInstructionDisplay);

    final String baselineInfusionDisplay = getBaselineInfusionDisplay(complexOrder.isBaselineInfusion(), locale);
    complexOrder.setBaselineInfusionDisplay(baselineInfusionDisplay);

    final String orderDescription = getComplexOrderDescription(complexOrder);
    complexOrder.setTherapyDescription(orderDescription);

    if (complexOrder instanceof VariableComplexTherapyDto)
    {
      final VariableComplexTherapyDto variableComplexOrder = (VariableComplexTherapyDto)complexOrder;
      for (final TimedComplexDoseElementDto timedComplexDoseElement : variableComplexOrder.getTimedDoseElements())
      {
        final String intervalDisplay =
            getCalculatedIntervalDisplay(
                timedComplexDoseElement.getDoseTime(),
                timedComplexDoseElement.getDoseElement().getDuration(),
                complexOrder.getEnd());
        timedComplexDoseElement.setIntervalDisplay(
            intervalDisplay);
        final String elementSpeedDisplay = getSpeedElementDisplay(timedComplexDoseElement.getDoseElement(), locale);
        timedComplexDoseElement.setSpeedDisplay(elementSpeedDisplay);

        final String elementSpeedFormulaDisplay =
            getSpeedFormulaElementDisplay(timedComplexDoseElement.getDoseElement(), locale);
        timedComplexDoseElement.setSpeedFormulaDisplay(elementSpeedFormulaDisplay);
      }
    }
    else
    {
      final ConstantComplexTherapyDto constantComplexTherapyDto = (ConstantComplexTherapyDto)complexOrder;
      final String durationDisplay = getDurationDisplay(constantComplexTherapyDto, locale);
      constantComplexTherapyDto.setDurationDisplay(durationDisplay);
    }
  }

  private String getAdditionInstructionDisplay(final String additionalInstruction, final Locale locale)
      throws ParseException
  {
    if (additionalInstruction == null)
    {
      return null;
    }
    final MedicationAdditionalInstructionEnum additionalInstructionEnum =
        MedicationAdditionalInstructionEnum.valueOf(additionalInstruction);
    if (additionalInstructionEnum == MedicationAdditionalInstructionEnum.HEPARIN_05)
    {
      return "heparin " + NumberFormatters.doubleFormatter2(locale).valueToString(0.5) + " IE/ml";
    }
    else if (additionalInstructionEnum == MedicationAdditionalInstructionEnum.HEPARIN_1)
    {
      return "heparin " + NumberFormatters.doubleFormatter2(locale).valueToString(1) + " IE/ml";
    }
    else
    {
      throw new IllegalArgumentException("Unknown additional instruction:" + additionalInstruction);
    }
  }

  private void fillSimpleOrderDisplayValues(final SimpleTherapyDto simpleOrder, final Locale locale) throws ParseException
  {
    final String quantityDisplay = getSimpleOrderQuantityDisplay(simpleOrder, locale);
    simpleOrder.setQuantityDisplay(quantityDisplay);
    if (simpleOrder.getMedication() != null)
    {
      final String medicationDisplay = getMedicationDisplay(simpleOrder.getMedication());
      simpleOrder.getMedication().setDisplayName(medicationDisplay);
    }
    final String orderDescription = getSimpleOrderDescription(simpleOrder);
    simpleOrder.setTherapyDescription(orderDescription);
    if (simpleOrder instanceof VariableSimpleTherapyDto)
    {
      final VariableSimpleTherapyDto variableSimpleOrder = (VariableSimpleTherapyDto)simpleOrder;
      for (final TimedSimpleDoseElementDto timedSimpleDoseElement : variableSimpleOrder.getTimedDoseElements())
      {
        timedSimpleDoseElement.setTimeDisplay(hourMinuteToString(timedSimpleDoseElement.getDoseTime()));
        final String elementQuantityDisplay =
            getSimpleOrderElementQuantityDisplay(variableSimpleOrder, timedSimpleDoseElement.getDoseElement(), locale);
        timedSimpleDoseElement.setQuantityDisplay(elementQuantityDisplay);
      }
    }
  }

  private String getSimpleOrderDescription(final SimpleTherapyDto simpleOrder)
  {
    final StringBuilder descriptionBuilder = new StringBuilder();
    if (simpleOrder.getMedication() != null)
    {
      descriptionBuilder.append(simpleOrder.getMedication().getDisplayName());
    }
    descriptionBuilder.append(" - ");
    descriptionBuilder.append(simpleOrder.getQuantityDisplay());
    descriptionBuilder.append(" - ");
    descriptionBuilder.append(simpleOrder.getFrequencyDisplay());
    descriptionBuilder.append(" - ");
    if (simpleOrder.getWhenNeededDisplay() != null)
    {
      descriptionBuilder.append(simpleOrder.getWhenNeededDisplay());
      descriptionBuilder.append(" - ");
    }
    if (simpleOrder.getStartCriterionDisplay() != null)
    {
      descriptionBuilder.append(simpleOrder.getStartCriterionDisplay());
      descriptionBuilder.append(" - ");
    }
    if (simpleOrder.getRoute() != null)
    {
      descriptionBuilder.append(simpleOrder.getRoute().getName());
    }
    return descriptionBuilder.toString();
  }

  private String getComplexOrderDescription(final ComplexTherapyDto complexOrder)
  {
    final StringBuilder descriptionBuilder = new StringBuilder();
    for (final InfusionIngredientDto ingredientDto : complexOrder.getIngredientsList())
    {
      descriptionBuilder.append(ingredientDto.getMedication().getDisplayName());
      descriptionBuilder.append(" - ");
      if (ingredientDto.getQuantityDisplay() != null)
      {
        descriptionBuilder.append(ingredientDto.getQuantityDisplay());
        descriptionBuilder.append(", ");
      }
    }
    if (complexOrder.getAdditionalInstructionDisplay() != null)
    {
      descriptionBuilder.append(complexOrder.getAdditionalInstructionDisplay());
      descriptionBuilder.append(", ");
    }
    if (complexOrder.getVolumeSumDisplay() != null)
    {
      descriptionBuilder.append(complexOrder.getVolumeSumDisplay());
      descriptionBuilder.append(" - ");
    }
    descriptionBuilder.append(complexOrder.getSpeedDisplay());
    descriptionBuilder.append(" - ");
    if (complexOrder.getFrequencyDisplay() != null)
    {
      descriptionBuilder.append(complexOrder.getFrequencyDisplay());
    }
    descriptionBuilder.append(" - ");
    if (complexOrder.getWhenNeededDisplay() != null)
    {
      descriptionBuilder.append(complexOrder.getWhenNeededDisplay());
      descriptionBuilder.append(" - ");
    }
    if (complexOrder.getStartCriterionDisplay() != null)
    {
      descriptionBuilder.append(complexOrder.getStartCriterionDisplay());
      descriptionBuilder.append(" - ");
    }
    if (complexOrder.getRoute() != null)
    {
      descriptionBuilder.append(complexOrder.getRoute().getName());
    }
    return descriptionBuilder.toString();
  }

  private String getFrequencyDisplay(final DosingFrequencyDto frequency, final Locale locale)
  {
    if (frequency == null)
    {
      return null;
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.BETWEEN_DOSES)
    {
      Preconditions.checkNotNull(frequency.getValue());
      return medicationsConnector.getEntry("once.every", null, locale) + ' ' + frequency.getValue() + ' ' +
          medicationsConnector.getEntry("hours.accusative", null, locale);
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.DAILY_COUNT)
    {
      Preconditions.checkNotNull(frequency.getValue());
      return frequency.getValue() + "X " + medicationsConnector.getEntry("per.day", null, locale);
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.EVENING)
    {
      return medicationsConnector.getEntry("in.evening", null, locale);
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.NOON)
    {
      return medicationsConnector.getEntry("at.noon", null, locale);
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.MORNING)
    {
      return medicationsConnector.getEntry("in.morning", null, locale);
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.ONCE_THEN_EX)
    {
      return "1x + ex";
    }
    return null;
  }

  private String getWhenNeededDisplay(final Boolean whenNeeded, final Locale locale)
  {
    if (whenNeeded != null && whenNeeded)
    {
      return medicationsConnector.getEntry("when.needed", null, locale);
    }
    return null;
  }

  private String getStartCriterionDisplay(final List<String> startCriterions, final Locale locale)
  {
    String startCriterionDisplay = "";
    for (final String startCriterion : startCriterions)
    {
      if (startCriterion != null && !startCriterion.isEmpty())
      {
        final MedicationStartCriterionEnum startConditionEnum = MedicationStartCriterionEnum.valueOf(startCriterion);
        if (startConditionEnum != null)
        {
          if (startConditionEnum == MedicationStartCriterionEnum.BY_DOCTOR_ORDERS)
          {
            startCriterionDisplay += medicationsConnector.getEntry("by.doctor.orders", null, locale);
          }
          else if (MedicationStartCriterionEnum.APPLICATION_PRECONDITION.contains(startConditionEnum))
          {
            startCriterionDisplay += medicationsConnector.getEntry("MedicationStartCriterionEnum." + startConditionEnum.name(), null, locale);
          }
          else
          {
            throw new IllegalArgumentException("No display defined for start criterion " + startCriterion);
          }
        }
        else
        {
          throw new IllegalArgumentException("Unknown start condition " + startCriterion);
        }
      }
    }
    return startCriterionDisplay;
  }

  private String getSimpleOrderQuantityDisplay(final SimpleTherapyDto order, final Locale locale)
      throws ParseException
  {
    if (order instanceof ConstantSimpleTherapyDto)
    {
      final ConstantSimpleTherapyDto constantOrder = (ConstantSimpleTherapyDto)order;
      return getSimpleOrderElementQuantityDisplay(constantOrder, constantOrder.getDoseElement(), locale);
    }
    else
    {
      final VariableSimpleTherapyDto variableOrder = (VariableSimpleTherapyDto)order;

      final boolean variableDays = variableOrder.getTimedDoseElements().get(0).getDate() != null;
      if (variableDays)
      {
        return "Variable dose"; //TODO Mitja
      }
      else
      {
        final StringBuilder quantityDisplay = new StringBuilder();
        int index = 0;
        for (final TimedSimpleDoseElementDto timedDoseElement : variableOrder.getTimedDoseElements())
        {
          quantityDisplay.append(
              NumberFormatters.doubleFormatter2(locale).valueToString(timedDoseElement.getDoseElement().getQuantity()));
          if (timedDoseElement.getDoseElement().getQuantityDenominator() != null)
          {
            quantityDisplay.append('/');
            quantityDisplay.append(
                NumberFormatters.doubleFormatter2(locale)
                    .valueToString(timedDoseElement.getDoseElement().getQuantityDenominator()));
          }
          if (index < variableOrder.getTimedDoseElements().size() - 1)
          {
            quantityDisplay.append('-');
          }
          index++;
        }
        quantityDisplay.append(' ').append(variableOrder.getQuantityUnit());
        if (variableOrder.getQuantityDenominatorUnit() != null)
        {
          quantityDisplay.append('/').append(variableOrder.getQuantityDenominatorUnit());
        }
        return quantityDisplay.toString();
      }
    }
  }

  private String getSimpleOrderElementQuantityDisplay(
      final SimpleTherapyDto simpleOrder, final SimpleDoseElementDto simpleDoseElement, final Locale locale)
      throws ParseException
  {
    if (simpleDoseElement.getQuantity() != null)
    {
      String quantityDisplay =
          NumberFormatters.doubleFormatter2(locale).valueToString(simpleDoseElement.getQuantity());
      quantityDisplay += ' ' + simpleOrder.getQuantityUnit();
      if (simpleDoseElement.getQuantityDenominator() != null)
      {
        final String quantityDenominator =
            NumberFormatters.doubleFormatter2(locale)
                .valueToString(simpleDoseElement.getQuantityDenominator());
        quantityDisplay += '/' + quantityDenominator + ' ' + simpleOrder.getQuantityDenominatorUnit();
      }
      return quantityDisplay;
    }
    if (simpleDoseElement.getDoseDescription() != null)
    {
      return simpleDoseElement.getDoseDescription();
    }
    return null;
  }

  private String getMedicationDisplay(final MedicationDto medication)
  {
    if (medication == null)
    {
      return null;
    }
    if (medication.getMedicationType() == MedicationTypeEnum.SOLUTION || medication.getGenericName() == null)
    {
      return medication.getName();
    }
    return medication.getGenericName() + " (" + medication.getName() + ')';
  }

  private String getVolumeSumDisplay(final Double volumeSum, final String volumeSumUnit, final Locale locale)
      throws ParseException
  {
    if (volumeSum != null)
    {
      return volumeToString(volumeSum, locale) + ' ' + volumeSumUnit;
    }
    return null;
  }

  private String getIngredientQuantityDisplay(
      final InfusionIngredientDto ingredient,
      final boolean isShortDisplay,
      final Locale locale)
      throws ParseException
  {
    if (ingredient.getQuantity() != null && ingredient.getMedication().getMedicationType() != MedicationTypeEnum.SOLUTION)
    {
      String quantityDisplay = NumberFormatters.doubleFormatter2(locale).valueToString(ingredient.getQuantity());
      quantityDisplay += ' ' + ingredient.getQuantityUnit();
      if (ingredient.getVolume() != null && !isShortDisplay)
      {
        final String volume = volumeToString(ingredient.getVolume(), locale);
        quantityDisplay += '/' + volume + ' ' + ingredient.getVolumeUnit();
      }
      return quantityDisplay;
    }
    else if (ingredient.getVolume() != null)
    {
      final String volume = volumeToString(ingredient.getVolume(), locale);
      return volume + ' ' + ingredient.getVolumeUnit();
    }
    else
    {
      return null;
    }
  }

  private String getSpeedDisplay(final ComplexTherapyDto order, final Locale locale) throws ParseException
  {
    if (order instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto constantOrder = (ConstantComplexTherapyDto)order;

      if (constantOrder.getRateString() != null)
      {
        return constantOrder.getRateString();
      }
      else if (constantOrder.getDoseElement() != null && constantOrder.getDoseElement().getRate() != null)
      {
        return getRateDisplayString(
            constantOrder.getDoseElement().getRate(), constantOrder.getDoseElement().getRateUnit(), locale);
      }
    }
    else
    {
      final VariableComplexTherapyDto variableOrder = (VariableComplexTherapyDto)order;
      final StringBuilder speedDisplay = new StringBuilder();
      int index = 0;
      for (final TimedComplexDoseElementDto timedDoseElement : variableOrder.getTimedDoseElements())
      {
        speedDisplay.append(
            NumberFormatters.doubleFormatter2(locale).valueToString(timedDoseElement.getDoseElement().getRate()));
        if (index < variableOrder.getTimedDoseElements().size() - 1)
        {
          speedDisplay.append('-');
        }
        index++;
      }
      speedDisplay.append(' ');
      speedDisplay.append(variableOrder.getTimedDoseElements().get(0).getDoseElement().getRateUnit());
      return speedDisplay.toString();
    }
    return null;
  }

  public String getRateDisplayString(final Double rate, final String rateUnit, final Locale locale) throws ParseException
  {
    String rateDisplay = NumberFormatters.doubleFormatter2(locale).valueToString(rate);
    rateDisplay += ' ' + rateUnit;
    return rateDisplay;
  }

  private String getSpeedFormulaDisplay(final ComplexTherapyDto order, final Locale locale)
      throws ParseException
  {
    if (order instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto individualOrder = (ConstantComplexTherapyDto)order;

      if (individualOrder.getDoseElement() != null && individualOrder.getDoseElement().getRateFormula() != null)
      {
        final String speedFormulaString =
            NumberFormatters.doubleFormatter3(locale).valueToString(individualOrder.getDoseElement().getRateFormula());
        return speedFormulaString + ' ' + individualOrder.getDoseElement().getRateFormulaUnit();
      }
    }
    return null;
  }

  private String getDurationDisplay(final ConstantComplexTherapyDto order, final Locale locale) throws ParseException
  {
    if (order.getDoseElement() != null &&
        order.getDoseElement().getDuration() != null &&
        order.getDoseElement().getDuration() > 0)
    {
      final double durationInMinutes = order.getDoseElement().getDuration().doubleValue();
      final boolean showDurationInHours = durationInMinutes > 120 || durationInMinutes % 30 == 0;
      final double durationInHours = order.getDoseElement().getDuration().doubleValue() / 60.0;
      final String unit = showDurationInHours ? "h" : "min";
      return NumberFormatters.doubleFormatter2(locale).valueToString(
          showDurationInHours ? durationInHours : durationInMinutes) + unit;
    }
    return null;
  }

  private String getSpeedElementDisplay(final ComplexDoseElementDto complexDoseElement, final Locale locale)
      throws ParseException
  {
    String speedDisplay = NumberFormatters.doubleFormatter2(locale).valueToString(complexDoseElement.getRate());
    speedDisplay += ' ' + complexDoseElement.getRateUnit();
    return speedDisplay;
  }

  private String getSpeedFormulaElementDisplay(final ComplexDoseElementDto complexDoseElement, final Locale locale)
      throws ParseException
  {
    if (complexDoseElement.getRateFormula() != null)
    {
      String speedDisplay = NumberFormatters.doubleFormatter3(locale).valueToString(complexDoseElement.getRateFormula());
      speedDisplay += ' ' + complexDoseElement.getRateFormulaUnit();
      return speedDisplay;
    }
    return null;
  }

  private String getBaselineInfusionDisplay(final boolean baselineInfusion, final Locale locale)
  {
    if (baselineInfusion)
    {
      return medicationsConnector.getEntry("baseline.infusion.short", null, locale);
    }
    return null;
  }

  private String volumeToString(final Double volume, final Locale locale) throws ParseException
  {
    if (volume > 10.0)
    {
      return NumberFormatters.doubleFormatter2(locale).valueToString(volume);
    }
    return NumberFormatters.doubleFormatter3(locale).valueToString(volume);
  }

  private String hourMinuteToString(final HourMinuteDto hourMinuteDto)
  {
    if (hourMinuteDto != null)
    {
      return String.format("%02d", hourMinuteDto.getHour()) + ':' + String.format("%02d", hourMinuteDto.getMinute());
    }
    return null;
  }

  private String getCalculatedIntervalDisplay(
      final HourMinuteDto startHourMinute,
      final Integer durationInMinutes,
      final DateTime therapyEnd)
  {
    if (startHourMinute != null)
    {
      final DateTime start = new DateTime(1, 1, 1, startHourMinute.getHour(), startHourMinute.getMinute());

      String intervalString = hourMinuteToString(startHourMinute) + " - ";
      if (durationInMinutes != null)
      {
        final DateTime end = start.plusMinutes(durationInMinutes);
        final HourMinuteDto endHourMinute = new HourMinuteDto(end.getHourOfDay(), end.getMinuteOfHour());
        intervalString += hourMinuteToString(endHourMinute);
      }
      else if (therapyEnd != null)
      {
        final HourMinuteDto endHourMinute = new HourMinuteDto(therapyEnd.getHourOfDay(), therapyEnd.getMinuteOfHour());
        intervalString += hourMinuteToString(endHourMinute);
      }
      else
      {
        intervalString += "...";
      }
      return intervalString;
    }
    return null;
  }

  private String getMedicationDaysOfWeekDisplay(final List<String> daysOfQWeek, final Locale locale)
  {
    if (daysOfQWeek == null || daysOfQWeek.isEmpty() || daysOfQWeek.size() == 7)
    {
      return null;
    }

    final StringBuilder displayStringBuilder = new StringBuilder();
    for (final String day : daysOfQWeek)
    {
      displayStringBuilder.append(medicationsConnector.getEntry(day.toLowerCase(locale) + ".f2l", null, locale));
      displayStringBuilder.append(' ');
    }
    return displayStringBuilder.toString().trim();
  }

  private String getDaysFrequencyDisplay(final Integer daysFrequency, final Locale locale)
  {
    if (daysFrequency != null)
    {
      return medicationsConnector.getEntry("every", null, locale).toLowerCase(locale) + ' ' + daysFrequency + ". " +
          medicationsConnector.getEntry("day.lc", null, locale);
    }
    return null;
  }
}
