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

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.CollectionUtils;
import com.marand.maf.core.Opt;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.maf.core.formatter.NumberFormatters;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.thinkmed.api.core.GrammaticalGender;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantTherapy;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.supply.PrescriptionSupplyDto;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * @author Mitja Lapajne
 */
public class TherapyDisplayProvider
{
  public void fillDisplayValues(
      final TherapyDto therapy,
      final boolean showLongFormattedDescription,
      final boolean showTherapyInterval,
      final Locale locale)
  {
    fillDisplayValues(therapy, showLongFormattedDescription, showTherapyInterval, false, locale, false);
  }

  public void fillDisplayValues(
      final TherapyDto therapy,
      final boolean showLongFormattedDescription,
      final boolean showTherapyInterval,
      final boolean showTherapyDuration,
      final Locale locale,
      final boolean showCommentOnShortFormat)
  {
    try
    {
      final String frequencyDisplay = getFrequencyDisplay(therapy.getDosingFrequency(), locale);
      therapy.setFrequencyDisplay(frequencyDisplay);

      final String whenNeededDisplay = getWhenNeededDisplay(therapy.getWhenNeeded(), locale);
      therapy.setWhenNeededDisplay(whenNeededDisplay);

      final String startCriterionDisplay = getStartCriterionDisplay(therapy.getStartCriterion(), locale);
      therapy.setStartCriterionDisplay(startCriterionDisplay);

      final String applicationPreconditionDisplay =
          getApplicationPreconditionDisplay(therapy.getApplicationPrecondition(), locale);
      therapy.setApplicationPreconditionDisplay(applicationPreconditionDisplay);

      final String daysOfWeekDisplay = getMedicationDaysOfWeekDisplay(therapy.getDaysOfWeek(), locale);
      therapy.setDaysOfWeekDisplay(daysOfWeekDisplay);

      final String daysFrequencyDisplay = getDaysFrequencyDisplay(therapy.getDosingDaysFrequency(), locale);
      therapy.setDaysFrequencyDisplay(daysFrequencyDisplay);

      if (therapy instanceof SimpleTherapyDto)
      {
        fillSimpleOrderDisplayValues((SimpleTherapyDto)therapy, locale);
      }
      else if (therapy instanceof ComplexTherapyDto)
      {
        fillComplexOrderDisplayValues((ComplexTherapyDto)therapy, locale);
      }
      else if (therapy instanceof OxygenTherapyDto)
      {
        fillOxygenOrderDisplayValues((OxygenTherapyDto)therapy, locale);
      }

      final String therapyFormattedDescription;
      if (showLongFormattedDescription)
      {
        therapyFormattedDescription =
            getTherapyFormattedDescription(therapy, showTherapyInterval, showTherapyDuration, locale);
        therapy.setFormattedTherapyDisplay(therapyFormattedDescription);
      }
      else
      {
        therapyFormattedDescription =
            getTherapyFormattedShortDescription(
                therapy,
                showTherapyInterval,
                showTherapyDuration,
                locale,
                showCommentOnShortFormat);
      }
      therapy.setFormattedTherapyDisplay(therapyFormattedDescription);
    }
    catch (final ParseException e)
    {
      throw new IllegalArgumentException(e);
    }
  }

  //fillDisplayValues must be called first!!
  private String getTherapyFormattedDescription(
      final TherapyDto therapy,
      final boolean showTherapyInterval,
      final boolean showTherapyDuration,
      final Locale locale)
  {
    if (therapy instanceof SimpleTherapyDto)
    {
      return getSimpleTherapyFormatted((SimpleTherapyDto)therapy, showTherapyInterval, showTherapyDuration, locale);
    }
    if (therapy instanceof ComplexTherapyDto)
    {
      return getComplexTherapyFormatted((ComplexTherapyDto)therapy, showTherapyInterval, showTherapyDuration, locale);
    }
    if (therapy instanceof OxygenTherapyDto)
    {
      return getOxygenTherapyFormatted((OxygenTherapyDto)therapy, showTherapyInterval, showTherapyDuration, true, locale);
    }
    return "";
  }

  //fillDisplayValues must be called first!!
  private String getTherapyFormattedShortDescription(
      final TherapyDto therapy,
      final boolean showTherapyInterval,
      final boolean showTherapyDuration,
      final Locale locale,
      final boolean showComment)
  {
    if (therapy instanceof SimpleTherapyDto)
    {
      return getShortSimpleTherapyFormatted(
          (SimpleTherapyDto)therapy,
          showTherapyInterval,
          showTherapyDuration,
          locale,
          showComment);
    }
    if (therapy instanceof ComplexTherapyDto)
    {
      return getShortComplexTherapyFormatted(
          (ComplexTherapyDto)therapy,
          showTherapyInterval,
          showTherapyDuration,
          locale,
          showComment);
    }
    if (therapy instanceof OxygenTherapyDto)
    {
      return getOxygenTherapyFormatted(
          (OxygenTherapyDto)therapy, showTherapyInterval, showTherapyDuration, showComment, locale);
    }
    return "";
  }

  private String getShortSimpleTherapyFormatted(
      final SimpleTherapyDto therapy,
      final boolean showTherapyInterval,
      final boolean showTherapyDuration,
      final Locale locale,
      final boolean showComment)
  {
    final StringBuilder description = new StringBuilder();

    description.append(getMedicationSpannedDisplay(therapy.getMedication(), true, true));
    description.append(addLineBreak());

    final List<String> therapyElementsToDelimit = new ArrayList<>();
    if (StringUtils.isNotEmpty(therapy.getQuantityDisplay()))
    {
      final String formattedQuantityDisplay = getFormattedDecimalValue(therapy.getQuantityDisplay(), locale, true);
      therapyElementsToDelimit.add(
          createSpannedValue(
              formattedQuantityDisplay,
              "Quantity TextDataBold",
              true));
    }

    if (StringUtils.isNotEmpty(therapy.getFrequencyDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getFrequencyDisplay(), "Frequency TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysOfWeekDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getDaysOfWeekDisplay(), "DaysOfWeek TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysFrequencyDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getDaysFrequencyDisplay(), "FrequencyDisplay TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getApplicationPreconditionDisplay()))
    {
      therapyElementsToDelimit.add(
          createSpannedValue(
              therapy.getApplicationPreconditionDisplay(),
              "ApplicationPrecondition TextData",
              true));
    }

    if (StringUtils.isNotEmpty(therapy.getWhenNeededDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getWhenNeededDisplay(), "WhenNeeded TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getStartCriterionDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getStartCriterionDisplay(), "StartCriterion TextData", true));
    }

    if (!CollectionUtils.isEmpty(therapy.getRoutes()))
    {
      therapyElementsToDelimit.add(createSpannedValue(getRouteNamesString(therapy.getRoutes()), "Route TextData", true));
    }

    if (!therapyElementsToDelimit.isEmpty())
    {
      description.append(delimitValues(therapyElementsToDelimit));
    }

    if (showTherapyInterval)
    {
      description.append(getTherapyIntervalDisplay(therapy, locale));
    }

    if (therapy.getPrescriptionSupply() != null)
    {
      appendPrescriptionSupply(description, therapy.getPrescriptionSupply(), locale);
    }

    appendCommentAndClinicalIndication(description, showComment, therapy, locale);

    if (showTherapyDuration)
    {
      description.append(getTherapyDurationDisplay(therapy, locale));
    }

    return description.toString();
  }

  private String getShortComplexTherapyFormatted(
      final ComplexTherapyDto therapy,
      final boolean showTherapyInterval,
      final boolean showTherapyDuration,
      final Locale locale,
      final boolean showComment)
  {
    final boolean allIngredientsSolutions = areAllIngredientsSolutions(therapy);

    final StringBuilder description = new StringBuilder();
    for (final InfusionIngredientDto ingredient : therapy.getIngredientsList())
    {
      final List<String> ingredientsToDelimit = new ArrayList<>();
      ingredientsToDelimit.add(getMedicationSpannedDisplay(ingredient.getMedication(), false, false));

      if (StringUtils.isNotEmpty(ingredient.getQuantityDisplay()))
      {
        final String formattedQuantityDisplay = getFormattedDecimalValue(ingredient.getQuantityDisplay(), locale, true);
        ingredientsToDelimit.add(createSpannedValue(formattedQuantityDisplay, "Quantity TextDataBold", true));
      }

      description.append(delimitValues(ingredientsToDelimit));
      description.append(addLineBreak());
    }

    final List<String> elementsToDelimit = new ArrayList<>();

    if (StringUtils.isNotEmpty(therapy.getVolumeSumDisplay()))
    {
      final String formattedVolumeSumDisplay = getFormattedDecimalValue(therapy.getVolumeSumDisplay(), locale, false);
      elementsToDelimit.add(createSpannedValue(formattedVolumeSumDisplay, "VolumeSum TextData", true));
    }

    if (therapy.isVariable() && therapy.getSpeedFormulaDisplay() != null)
    {
      final String formattedSpeedFormulaDisplay = getFormattedDecimalValue(therapy.getSpeedFormulaDisplay(), locale, false);
      elementsToDelimit.add(createSpannedValue(formattedSpeedFormulaDisplay, "SpeedFormula TextData", true));
    }
    else
    {
      if (StringUtils.isNotEmpty(therapy.getSpeedDisplay()) || therapy.isAdjustToFluidBalance())
      {
        final String speed =
            therapy.isAdjustToFluidBalance()
            ? Dictionary.getEntry("adjust.to.fluid.balance.short", locale)
            : therapy.getSpeedDisplay();

        final String formattedDecimalSpeed = getFormattedDecimalValue(speed, locale, false);

        elementsToDelimit.add(createSpannedValue(formattedDecimalSpeed, "Speed TextData", true));
      }

      if (!allIngredientsSolutions && StringUtils.isNotEmpty(therapy.getSpeedFormulaDisplay()))
      {
        final String formattedSpeedFormulaDisplay = getFormattedDecimalValue(therapy.getSpeedFormulaDisplay(), locale, false);
        elementsToDelimit.add(createSpannedValue(formattedSpeedFormulaDisplay, "SpeedFormula TextData", true));
      }
    }

    if (therapy instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto constantTherapy = (ConstantComplexTherapyDto)therapy;
      if (StringUtils.isNotEmpty(constantTherapy.getDurationDisplay()))
      {
        elementsToDelimit.add(createSpannedValue(constantTherapy.getDurationDisplay(), "Duration TextData", true));
      }
    }

    if (therapy.isContinuousInfusion())
    {
      elementsToDelimit.add(
          createSpannedValue(
              Dictionary.getEntry("continuous.infusion", locale).toUpperCase(),
              "Infusion TextData",
              true));
    }

    if (StringUtils.isNotEmpty(therapy.getFrequencyDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getFrequencyDisplay(), "Frequency TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getApplicationPreconditionDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(
          therapy.getApplicationPreconditionDisplay(),
          "ApplicationPrecondition TextData",
          true));
    }

    if (StringUtils.isNotEmpty(therapy.getWhenNeededDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getWhenNeededDisplay(), "WhenNeeded TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getStartCriterionDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getStartCriterionDisplay(), "StartCriterion TextData", true));
    }

    if (!CollectionUtils.isEmpty(therapy.getRoutes()))
    {
      elementsToDelimit.add(createSpannedValue(getRouteNamesString(therapy.getRoutes()), "Route TextData", true));
    }

    description.append(delimitValues(elementsToDelimit));

    if (showTherapyInterval)
    {
      description.append(getTherapyIntervalDisplay(therapy, locale));
    }

    if (therapy.getPrescriptionSupply() != null)
    {
      appendPrescriptionSupply(description, therapy.getPrescriptionSupply(), locale);
    }

    appendCommentAndClinicalIndication(description, showComment, therapy, locale);

    if (showTherapyDuration)
    {
      description.append(getTherapyDurationDisplay(therapy, locale));
    }

    return description.toString();
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  private boolean areAllIngredientsSolutions(final ComplexTherapyDto therapy)
  {
    return therapy.getIngredientsList()
        .stream()
        .allMatch(i -> i.getMedication().getMedicationType() == MedicationTypeEnum.SOLUTION);
  }

  private String getSimpleTherapyFormatted(
      final SimpleTherapyDto therapy,
      final boolean showTherapyInterval,
      final boolean showTherapyDuration,
      final Locale locale)
  {
    final StringBuilder description = new StringBuilder();

    description.append(getMedicationSpannedDisplay(therapy.getMedication(), true, false));
    description.append(addLineBreak());

    final List<String> therapyElementsToDelimit = new ArrayList<>();
    if (StringUtils.isNotEmpty(therapy.getQuantityDisplay()))
    {
      final String formattedQuantityDisplay = getFormattedDecimalValue(therapy.getQuantityDisplay(), locale, true);
      therapyElementsToDelimit.add(
          createSpannedValue(
              Dictionary.getEntry("dose", locale).toUpperCase(),
              "DoseLabel TextLabel MedicationLabel",
              true) +
              createSpannedValue(
                  formattedQuantityDisplay,
                  "Quantity TextDataBold",
                  true));
    }

    if (StringUtils.isNotEmpty(therapy.getFrequencyDisplay()) && !isSimpleProtocolTherapy(therapy))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getFrequencyDisplay(), "Frequency TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysOfWeekDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getDaysOfWeekDisplay(), "DaysOfWeek TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysFrequencyDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getDaysFrequencyDisplay(), "FrequencyDisplay TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getApplicationPreconditionDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(
          therapy.getApplicationPreconditionDisplay(),
          "ApplicationPrecondition TextData",
          true));
    }

    if (StringUtils.isNotEmpty(therapy.getWhenNeededDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getWhenNeededDisplay(), "WhenNeeded TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getStartCriterionDisplay()))
    {
      therapyElementsToDelimit.add(createSpannedValue(therapy.getStartCriterionDisplay(), "StartCriterion TextData", true));
    }

    if (!CollectionUtils.isEmpty(therapy.getRoutes()))
    {
      therapyElementsToDelimit.add(createSpannedValue(getRouteNamesString(therapy.getRoutes()), "Route TextData", true));
    }

    if (!therapyElementsToDelimit.isEmpty())
    {
      description.append(delimitValues(therapyElementsToDelimit));
    }

    if (showTherapyInterval)
    {
      description.append(getTherapyIntervalDisplay(therapy, locale));
    }

    if (therapy.getPrescriptionSupply() != null)
    {
      appendPrescriptionSupply(description, therapy.getPrescriptionSupply(), locale);
    }

    appendCommentAndClinicalIndication(description, true, therapy, locale);

    if (showTherapyDuration)
    {
      description.append(getTherapyDurationDisplay(therapy, locale));
    }

    return description.toString();
  }

  private String getComplexTherapyFormatted(
      final ComplexTherapyDto therapy,
      final boolean showTherapyInterval,
      final boolean showTherapyDuration,
      final Locale locale)
  {
    final StringBuilder description = new StringBuilder();

    if (therapy.isBaselineInfusion())
    {
      description.append(
          createSpannedValue(therapy.getBaselineInfusionDisplay(), "BaseLineInfusion TextLabel MedicationLabel", true));
    }

    final boolean infusionWithoutRate = StringUtils.isEmpty(therapy.getSpeedDisplay());
    final boolean singleIngredient = therapy.getIngredientsList().size() == 1;
    final boolean allIngredientsSolutions = areAllIngredientsSolutions(therapy);
    final boolean simpleComplexTherapy = infusionWithoutRate && !therapy.isContinuousInfusion() && !isBolusTherapy(therapy);

    for (final InfusionIngredientDto ingredient : therapy.getIngredientsList())
    {
      description.append(getIngredientDescription(
          ingredient,
          !infusionWithoutRate || !singleIngredient,
          simpleComplexTherapy && ingredient.getMedication().getMedicationType() != MedicationTypeEnum.SOLUTION,
          locale));
    }

    if (infusionWithoutRate && singleIngredient)
    {
      description.append(
          createSpannedValue(
              Dictionary.getEntry("dose", locale).toUpperCase(),
              "DoseLabel TextLabel MedicationLabel",
              true) +
              createSpannedValue(
                  getFormattedDecimalValue(therapy.getIngredientsList().get(0).getQuantityDisplay(), locale, true),
                  "Quantity TextDataBold",
                  true));
    }

    if (StringUtils.isNotEmpty(therapy.getVolumeSumDisplay()))
    {
      final String formattedVolumeSumDisplay = getFormattedDecimalValue(therapy.getVolumeSumDisplay(), locale, false);
      description.append(
          createSpannedValue(
              Dictionary.getEntry("volume.total", locale).toUpperCase(),
              "VolumeSumLabel TextLabel MedicationLabel",
              true) +
              createSpannedValue(formattedVolumeSumDisplay, "VolumeSum TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getVolumeSumDisplay()) &&
        StringUtils.isNotEmpty(therapy.getAdditionalInstructionDisplay()))
    {
      description.append(" + ");
    }

    if (StringUtils.isNotEmpty(therapy.getAdditionalInstructionDisplay()))
    {
      description.append(
          createSpannedValue(
              therapy.getAdditionalInstructionDisplay(),
              "AdditionalInstruction TextData",
              true));
    }

    if (StringUtils.isNotEmpty(therapy.getVolumeSumDisplay()) || StringUtils.isNotEmpty(therapy.getAdditionalInstructionDisplay()))
    {
      description.append(addLineBreak());
    }

    final List<String> elementsToDelimit = new ArrayList<>();

    if (therapy.isVariable())
    {
      final String labelValue = createSpannedValue(
          Dictionary.getEntry(
              therapy.isContinuousInfusion() ? "rate" : "dose",
              locale).toUpperCase(),
          "SpeedFormulaLabel TextLabel MedicationLabel",
          true);

      final String dataValue = createSpannedValue(
          Dictionary.getEntry(
              therapy.isContinuousInfusion() ? "variable.infusion" : "variable.rate",
              locale),
          "SpeedFormula TextData",
          true);

      elementsToDelimit.add(labelValue + dataValue);
    }
    else
    {
      if (StringUtils.isNotEmpty(therapy.getSpeedDisplay()) || therapy.isAdjustToFluidBalance())
      {
        final String speed =
            therapy.isAdjustToFluidBalance()
            ? Dictionary.getEntry("adjust.to.fluid.balance.short", locale)
            : therapy.getSpeedDisplay();

        final String formattedDecimalSpeed = getFormattedDecimalValue(speed, locale, false);

        elementsToDelimit.add(
            createSpannedValue(
                Dictionary.getEntry("rate", locale).toUpperCase(),
                "SpeedLabel TextLabel MedicationLabel",
                true) +
                createSpannedValue(
                    formattedDecimalSpeed,
                    "Speed TextData",
                    true));
      }

      if (!allIngredientsSolutions && StringUtils.isNotEmpty(therapy.getSpeedFormulaDisplay()))
      {
        final String formattedSpeedFormulaDisplay = getFormattedDecimalValue(therapy.getSpeedFormulaDisplay(), locale, false);
        elementsToDelimit.add(
            createSpannedValue(
                Dictionary.getEntry("dose", locale).toUpperCase(),
                "SpeedFormulaLabel TextLabel MedicationLabel",
                true) +
                createSpannedValue(
                    formattedSpeedFormulaDisplay,
                    "SpeedFormula TextData",
                    true));
      }
    }

    if (therapy instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto constantTherapy = (ConstantComplexTherapyDto)therapy;
      if (StringUtils.isNotEmpty(constantTherapy.getDurationDisplay()))
      {
        elementsToDelimit.add(
            createSpannedValue(
                Dictionary.getEntry("duration", locale).toUpperCase(),
                "DurationLabel TextLabel MedicationLabel",
                true) +
                createSpannedValue(
                    constantTherapy.getDurationDisplay(),
                    "Duration TextData",
                    true));
      }
    }

    if (therapy.isContinuousInfusion())
    {
      elementsToDelimit.add(
          createSpannedValue(
              Dictionary.getEntry("continuous.infusion", locale).toUpperCase(),
              "Infusion TextData",
              true));
    }

    if (StringUtils.isNotEmpty(therapy.getFrequencyDisplay()))
    {
      if (infusionWithoutRate && singleIngredient)
      {
        description.append(addDelimiter());
      }
      else if (therapy.getVolumeSum() != null)
      {
        elementsToDelimit.add(createSpannedValue(
            Dictionary.getEntry("dosing.interval", locale).toUpperCase(),
            "FrequencyLabel TextLabel MedicationLabel",
            true));
      }
      elementsToDelimit.add(createSpannedValue(therapy.getFrequencyDisplay(), "Frequency TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysOfWeekDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getDaysOfWeekDisplay(), "DaysOfWeek TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getDaysFrequencyDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(
          therapy.getDaysFrequencyDisplay().toLowerCase(),
          "DaysFrequency TextData",
          true));
    }

    if (StringUtils.isNotEmpty(therapy.getApplicationPreconditionDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(
          therapy.getApplicationPreconditionDisplay(),
          "ApplicationPrecondition TextData",
          true));
    }

    if (StringUtils.isNotEmpty(therapy.getWhenNeededDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getWhenNeededDisplay(), "WhenNeeded TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getStartCriterionDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getStartCriterionDisplay(), "StartCriterion TextData", true));
    }

    if (!CollectionUtils.isEmpty(therapy.getRoutes()))
    {
      elementsToDelimit.add(createSpannedValue(getRouteNamesString(therapy.getRoutes()), "Route TextData", true));
    }

    description.append(delimitValues(elementsToDelimit));

    if (showTherapyInterval)
    {
      description.append(getTherapyIntervalDisplay(therapy, locale));
    }

    if (therapy.getPrescriptionSupply() != null)
    {
      appendPrescriptionSupply(description, therapy.getPrescriptionSupply(), locale);
    }

    appendCommentAndClinicalIndication(description, true, therapy, locale);

    if (showTherapyDuration)
    {
      description.append(getTherapyDurationDisplay(therapy, locale));
    }

    return description.toString();
  }

  private boolean isBolusTherapy(final TherapyDto therapy)
  {
    return therapy.getMedicationOrderFormType() == MedicationOrderFormType.COMPLEX
        && MedicationsEhrUtils.BOLUS.equals(((ConstantComplexTherapyDto)therapy).getRateString());
  }

  private String getOxygenTherapyFormatted(
      final OxygenTherapyDto therapy,
      final boolean showTherapyInterval,
      final boolean showTherapyDuration,
      final boolean showComment,
      final Locale locale)
  {
    //noinspection TooBroadScope
    final StringBuilder description = new StringBuilder();

    description.append(getMedicationSpannedDisplay(therapy.getMedication(), true, true));
    description.append(addLineBreak());

    final List<String> elementsToDelimit = new ArrayList<>();

    if (therapy.getMaxTargetSaturation() != null && therapy.getMinTargetSaturation() != null)
    {
      final String saturationValue = therapy.getSaturationDisplay();
      final String saturationLabel = Dictionary.getEntry("target.saturation", locale).toUpperCase();

      elementsToDelimit.add(createSpannedValue(saturationLabel, "SaturationLabel TextLabel MedicationLabel", true)
                                + createSpannedValue(saturationValue, "Saturation TextData", true));
    }

    final String speed = therapy.getSpeedDisplay();
    if (StringUtils.isNotBlank(speed))
    {
      final String formattedDecimalSpeed = getFormattedDecimalValue(speed, locale, false);
      elementsToDelimit.add(
          createSpannedValue(
              Dictionary.getEntry("rate", locale).toUpperCase(),
              "SpeedLabel TextLabel MedicationLabel", true) + createSpannedValue(formattedDecimalSpeed, "Speed TextData", true));
    }
    if (therapy.getStartingDeviceDisplay() != null)
    {
      elementsToDelimit.add(createSpannedValue(therapy.getStartingDeviceDisplay(), "StartingDevice TextData", false));
    }

    if (therapy.isHumidification())
    {
      elementsToDelimit.add(createSpannedValue(Dictionary.getEntry("humidification", locale), "Humidification TextData", false));
    }

    if (therapy.getFlowRateMode() == AdministrationDetailsCluster.OxygenDeliveryCluster.FlowRateMode.HIGH_FLOW)
    {
      elementsToDelimit.add(createSpannedValue(Dictionary.getEntry("high.flow.oxygen.therapy", locale), "HighFlowRate TextData", false));
    }

    if (StringUtils.isNotEmpty(therapy.getWhenNeededDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getWhenNeededDisplay(), "WhenNeeded TextData", true));
    }

    if (StringUtils.isNotEmpty(therapy.getStartCriterionDisplay()))
    {
      elementsToDelimit.add(createSpannedValue(therapy.getStartCriterionDisplay(), "StartCriterion TextData", true));
    }

    if (!CollectionUtils.isEmpty(therapy.getRoutes()))
    {
      description.append(getRouteNamesString(therapy.getRoutes()));
    }

    description.append(delimitValues(elementsToDelimit));

    if (showTherapyInterval)
    {
      description.append(getTherapyIntervalDisplay(therapy, locale));
    }

    appendCommentAndClinicalIndication(description, showComment, therapy, locale);

    if (showTherapyDuration)
    {
      description.append(getTherapyDurationDisplay(therapy, locale));
    }

    return description.toString();
  }

  private String getIngredientDescription(
      final InfusionIngredientDto ingredient,
      final boolean showQuantity,
      final boolean showQuantityLabel,
      final Locale locale)
  {
    final String medicationDisplay = getIngredientMedicationDisplay(ingredient);
    final Opt<String> spannedQuantityDisplay = getIngredientSpannedQuantityDisplay(ingredient, locale);

    final String description;
    if (showQuantityLabel && showQuantity && spannedQuantityDisplay.isPresent())
    {
      final String doseLabel = createSpannedValue(
          Dictionary.getEntry("dose", locale).toUpperCase(),
          "BaseLineInfusion TextLabel MedicationLabel",
          true);

      description = medicationDisplay + addLineBreak() + doseLabel + spannedQuantityDisplay.get();
    }
    else
    {
      final List<String> elementsToDelimit = new ArrayList<>();
      elementsToDelimit.add(medicationDisplay);

      if (showQuantity && spannedQuantityDisplay.isPresent())
      {
        elementsToDelimit.add(spannedQuantityDisplay.get());
      }

      description = delimitValues(elementsToDelimit);
    }

    return description + addLineBreak();
  }

  private String getIngredientMedicationDisplay(final InfusionIngredientDto ingredient)
  {
    //noinspection IfMayBeConditional
    if (ingredient.getMedication().getMedicationType() == MedicationTypeEnum.SOLUTION)
    {
      return getMedicationSpannedDisplay(ingredient.getMedication(), false, false);
    }
    else
    {
      return getMedicationSpannedDisplay(ingredient.getMedication(), true, false);
    }
  }

  private Opt<String> getIngredientSpannedQuantityDisplay(final InfusionIngredientDto ingredient, final Locale locale)
  {
    if (StringUtils.isNotEmpty(ingredient.getQuantityDisplay()))
    {
      final String formattedQuantityDisplay = getFormattedDecimalValue(ingredient.getQuantityDisplay(), locale, true);
      return Opt.of(createSpannedValue(formattedQuantityDisplay, "Quantity TextDataBold", true));
    }
    return Opt.none();
  }

  public String getFormattedDecimalValue(final String decimalValue, final Locale locale, final boolean bold)
  {
    if (decimalValue == null || locale == null)
    {
      return "";
    }
    final DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
    final Character decimalSeparator = dfs.getDecimalSeparator();
    final String splitDelimiter = decimalSeparator.equals(new Character('.')) ? "\\." : ",";

    String formattedValue = decimalValue;
    final Pattern p = Pattern.compile("\\d+" + splitDelimiter + "\\d+"); // or ("[0-9]+" + splitDelimiter + "[0-9]+") ?
    final Matcher m = p.matcher(formattedValue);
    while (m.find())
    {
      final String[] decimalNumbers = m.group().split(splitDelimiter);
      if (decimalNumbers.length == 2)
      {
        formattedValue = formattedValue.replaceAll(
            m.group(),
            decimalNumbers[0] + splitDelimiter +
                createSpannedValue(
                    decimalNumbers[1],
                    "TextDataSmallerDecimal" + (bold ? " bold" : ""),
                    false));
      }
    }
    return formattedValue;
  }

  private String getMedicationSpannedDisplay(
      final MedicationDto medication,
      final boolean showGeneric,
      final boolean preferShortName)
  {
    final StringBuilder medicationDisplay = new StringBuilder();

    final String name = preferShortName && medication.getShortName() != null
                        ? medication.getShortName()
                        : medication.getName();

    if (showGeneric && medication.getGenericName() != null)
    {
      medicationDisplay.append(
          createSpannedValue(
              medication.getGenericName(),
              "GenericName TextDataBold",
              true));
      medicationDisplay.append(
          createSpannedValue(
              "(" + name + ")",
              "MedicationName TextData",
              true));
    }
    else
    {
      medicationDisplay.append(
          createSpannedValue(
              name,
              "MedicationName TextData",
              true));
    }

    return medicationDisplay.toString();
  }

  private String getTherapyIntervalDisplay(final TherapyDto therapy, final Locale locale)
  {
    if (therapy.getStart() == null && therapy.getEnd() == null)
    {
      return "";
    }
    final StringBuilder therapyDisplay = new StringBuilder();
    therapyDisplay.append("<span class='TherapyInterval'>");
    therapyDisplay.append(addLineBreak());

    if (therapy.getStart() != null)
    {
      therapyDisplay.append(
          createSpannedValue(
              Dictionary.getEntry("from", locale),
              "FromLabel TextLabel MedicationLabel",
              true));
      therapyDisplay.append(
          createSpannedValue(
              DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(therapy.getStart()),
              "From TextData",
              true));
    }

    if (therapy.getEnd() != null)
    {
      therapyDisplay.append(
          createSpannedValue(
              Dictionary.getEntry("until.low.case", locale),
              "UntilLabel TextLabel MedicationLabel",
              true));
      therapyDisplay.append(
          createSpannedValue(
              DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(therapy.getEnd()),
              "Until TextData",
              true));
    }
    therapyDisplay.append("</span>");
    return therapyDisplay.toString();
  }

  String getTherapyDurationDisplay(final TherapyDto therapy, final Locale locale)
  {
    if (therapy.getStart() == null || therapy.getEnd() == null ||
        (therapy.getDosingFrequency() != null &&
            therapy.getDosingFrequency().getType() == DosingFrequencyTypeEnum.ONCE_THEN_EX))
    {
      return "";
    }
    final StringBuilder therapyDisplay = new StringBuilder();

    therapyDisplay.append("<span class='TherapyDuration'>");
    therapyDisplay.append(addLineBreak());
    final String whiteSpace = " ";
    final Duration therapyDuration = new Duration(therapy.getEnd().getMillis() - therapy.getStart().getMillis());
    final PeriodFormatter therapyDurationFormatter = new PeriodFormatterBuilder()
        .appendWeeks()
        .appendSuffix(whiteSpace + Dictionary.getEntry("week.lc", locale), whiteSpace + Dictionary.getEntry("weeks", locale))
        .appendSeparator(", ")
        .appendDays()
        .appendSuffix(whiteSpace + Dictionary.getEntry("day.lc", locale), whiteSpace + Dictionary.getEntry("days", locale))
        .appendSeparator(", ")
        .appendHours()
        .appendSuffix(whiteSpace + Dictionary.getEntry("hour.lc", locale), whiteSpace + Dictionary.getEntry("hours.accusative", locale))
        .appendSeparator(", ")
        .appendMinutes()
        .appendSuffix(whiteSpace + Dictionary.getEntry("minute.short.lc", locale))
        .appendSeparator(", ")
        .toFormatter();
    final String formattedTherapyDuration =
        therapyDurationFormatter.print(therapyDuration.toPeriod().normalizedStandard());
    therapyDisplay.append(
        createSpannedValue(
            Dictionary.getEntry("duration", locale),
            "DurationLabel TextLabel MedicationLabel",
            true));
    therapyDisplay.append(
        createSpannedValue(
            formattedTherapyDuration,
            "Duration TextData",
            true));
    therapyDisplay.append("</span>");

    return therapyDisplay.toString();
  }

  private String delimitValues(final List<String> valuesToDelimit)
  {
    final StringBuilder delimitedDisplay = new StringBuilder();

    for (final Iterator<String> iterator = valuesToDelimit.iterator(); iterator.hasNext(); )
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

  private String createSpannedValue(final String valueToBeSpanned, final String spanClassValue, final boolean addWhiteSpace)
  {
    if (StringUtils.isEmpty(valueToBeSpanned) || StringUtils.isEmpty(spanClassValue))
    {
      return "";
    }
    final String whiteSpace = addWhiteSpace ? " " : "";
    return "<span class='" + spanClassValue + "'>" + valueToBeSpanned + whiteSpace + "</span>";
  }

  private String addLineBreak()
  {
    return "<br>";
  }

  private String addDelimiter()
  {
    return createSpannedValue("<span> &ndash; </span>", "Delimiter TextData", true);
  }

  private void fillComplexOrderDisplayValues(final ComplexTherapyDto complexOrder, final Locale locale) throws ParseException
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
      final String ingredientQuantityDisplay = getIngredientQuantityDisplay(complexOrder, ingredient, locale);
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

  private void fillOxygenOrderDisplayValues(final OxygenTherapyDto therapy, final Locale locale) throws ParseException
  {
    if (therapy.getMedication() != null)
    {
      final String medicationDisplay = getMedicationDisplay(therapy.getMedication());
      therapy.getMedication().setDisplayName(medicationDisplay);
    }

    if (therapy.getFlowRate() != null)
    {
      therapy.setSpeedDisplay(getRateDisplayString(therapy.getFlowRate(), therapy.getFlowRateUnit(), locale));
    }

    final OxygenStartingDevice device = therapy.getStartingDevice();
    if (device != null)
    {
      final StringBuilder deviceBuilder = new StringBuilder();
      final String key =
          AdministrationDetailsCluster.OxygenDeliveryCluster.class.getSimpleName()
          + "."
          + AdministrationDetailsCluster.OxygenDeliveryCluster.Route.class.getSimpleName() + "." + device.getRoute().name();

      deviceBuilder.append(Dictionary.getEntry(key, locale));

      if (device.getRouteType() != null)
      {
        deviceBuilder.append("-").append(device.getRouteType());
      }

      therapy.setStartingDeviceDisplay(deviceBuilder.toString());
    }

    final String orderDescription = getOxygenOrderDescription(therapy);
    therapy.setTherapyDescription(orderDescription);
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
      return "heparin " + NumberFormatters.doubleFormatter2(locale).valueToString(0.5) + " IE/mL";
    }
    else if (additionalInstructionEnum == MedicationAdditionalInstructionEnum.HEPARIN_1)
    {
      return "heparin " + NumberFormatters.doubleFormatter2(locale).valueToString(1) + " IE/mL";
    }
    else
    {
      throw new IllegalArgumentException("Unknown additional instruction:" + additionalInstruction);
    }
  }

  private void fillSimpleOrderDisplayValues(final SimpleTherapyDto simpleOrder, final Locale locale) throws ParseException
  {
    simpleOrder.setQuantityDisplay(getSimpleOrderQuantityDisplay(simpleOrder, locale));

    if (simpleOrder.getMedication() != null)
    {
      simpleOrder.getMedication().setDisplayName(getMedicationDisplay(simpleOrder.getMedication()));
    }

    simpleOrder.setTherapyDescription(getSimpleOrderDescription(simpleOrder));

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
    if (simpleOrder.getQuantityDisplay() != null)
    {
      descriptionBuilder.append(simpleOrder.getQuantityDisplay());
      descriptionBuilder.append(" - ");
    }
    if (simpleOrder.getFrequencyDisplay() != null && !isSimpleProtocolTherapy(simpleOrder))
    {
      descriptionBuilder.append(simpleOrder.getFrequencyDisplay());
      descriptionBuilder.append(" - ");
    }
    if (simpleOrder.getApplicationPreconditionDisplay() != null)
    {
      descriptionBuilder.append(simpleOrder.getApplicationPreconditionDisplay());
      descriptionBuilder.append(" - ");
    }
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
    if (!CollectionUtils.isEmpty(simpleOrder.getRoutes()))
    {
      descriptionBuilder.append(getRouteNamesString(simpleOrder.getRoutes()));
    }
    return descriptionBuilder.toString();
  }

  private String getOxygenOrderDescription(final OxygenTherapyDto therapyDto)
  {
    final StringBuilder descriptionBuilder = new StringBuilder();
    if (therapyDto.getMedication() != null)
    {
      descriptionBuilder.append(therapyDto.getMedication().getDisplayName());
    }
    if (!CollectionUtils.isEmpty(therapyDto.getRoutes()))
    {
      descriptionBuilder.append(getRouteNamesString(therapyDto.getRoutes()));
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
    if (complexOrder.getSpeedDisplay() != null)
    {
      descriptionBuilder.append(complexOrder.getSpeedDisplay());
      descriptionBuilder.append(" - ");
    }
    if (complexOrder.getFrequencyDisplay() != null)
    {
      descriptionBuilder.append(complexOrder.getFrequencyDisplay());
      descriptionBuilder.append(" - ");
    }
    if (complexOrder.getApplicationPreconditionDisplay() != null)
    {
      descriptionBuilder.append(complexOrder.getApplicationPreconditionDisplay());
      descriptionBuilder.append(" - ");
    }
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
    if (!CollectionUtils.isEmpty(complexOrder.getRoutes()))
    {
      descriptionBuilder.append(getRouteNamesString(complexOrder.getRoutes()));
    }
    return descriptionBuilder.toString();
  }

  private String getRouteNamesString(final List<MedicationRouteDto> medicationRoutes)
  {
    return medicationRoutes.stream().map(NamedIdentityDto::getName).collect(Collectors.joining(", "));
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
      return Dictionary.getEntry("once.every", locale) + ' '
          + frequency.getValue() + ' ' +
          Dictionary.getEntry("hours.accusative", locale);
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.DAILY_COUNT)
    {
      Preconditions.checkNotNull(frequency.getValue());
      return frequency.getValue() + "X " + Dictionary.getEntry("per.day", locale);
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.EVENING)
    {
      return Dictionary.getEntry("in.evening", locale);
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.NOON)
    {
      return Dictionary.getEntry("at.noon", locale);
    }
    if (frequency.getType() == DosingFrequencyTypeEnum.MORNING)
    {
      return Dictionary.getEntry("in.morning", locale);
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
      return Dictionary.getEntry("when.needed", locale);
    }
    return null;
  }

  private String getStartCriterionDisplay(final String startCriterion, final Locale locale)
  {
    if (startCriterion != null)
    {
      final MedicationStartCriterionEnum startConditionEnum = MedicationStartCriterionEnum.valueOf(startCriterion);
      if (startConditionEnum == MedicationStartCriterionEnum.BY_DOCTOR_ORDERS)
      {
        return Dictionary.getEntry("by.doctor.orders", locale);
      }
      else
      {
        throw new IllegalArgumentException("No display defined for start criterion " + startCriterion);
      }
    }
    return null;
  }

  private String getApplicationPreconditionDisplay(final String applicationPrecondition, final Locale locale)
  {
    if (applicationPrecondition != null)
    {
      final MedicationAdditionalInstructionEnum preconditionEnum =
          MedicationAdditionalInstructionEnum.valueOf(applicationPrecondition);
      if (preconditionEnum == MedicationAdditionalInstructionEnum.BEFORE_MEAL)
      {
        return Dictionary.getEntry("before.meal", locale);
      }
      else if (preconditionEnum == MedicationAdditionalInstructionEnum.AFTER_MEAL)
      {
        return Dictionary.getEntry("after.meal", locale);
      }
      else
      {
        throw new IllegalArgumentException("No display defined for start criterion " + applicationPrecondition);
      }
    }
    return null;
  }

  private String getSimpleOrderQuantityDisplay(final SimpleTherapyDto order, final Locale locale) throws ParseException
  {
    if (order instanceof ConstantSimpleTherapyDto)
    {
      final ConstantSimpleTherapyDto constantOrder = (ConstantSimpleTherapyDto)order;
      return getSimpleOrderElementQuantityDisplay(constantOrder, constantOrder.getDoseElement(), locale);
    }
    if (order instanceof VariableSimpleTherapyDto)
    {
      final boolean isProtocol = isSimpleProtocolTherapy(order);
      return Dictionary.getEntry(isProtocol ? "protocol" : "variable.dose", locale);
    }

    throw new IllegalArgumentException("Therapy type not supported!");
  }

  private boolean isSimpleProtocolTherapy(final TherapyDto order)
  {
    return order instanceof VariableSimpleTherapyDto
        && ((VariableSimpleTherapyDto)order).getTimedDoseElements().get(0).getDate() != null;
  }

  private String getSimpleOrderElementQuantityDisplay(
      final SimpleTherapyDto simpleOrder,
      final SimpleDoseElementDto simpleDoseElement,
      final Locale locale)
      throws ParseException
  {
    if (simpleDoseElement != null)
    {
      if (simpleDoseElement.getQuantity() != null)
      {
        String quantityDisplay = getDecimalStringValue(simpleDoseElement.getQuantity(), locale);
        quantityDisplay += ' ' + getFormattedUnit(simpleOrder.getQuantityUnit());

        if (simpleDoseElement.getQuantityDenominator() != null && simpleOrder.getQuantityDenominatorUnit() != null)
        {
          quantityDisplay += "/"
              + getDecimalStringValue(simpleDoseElement.getQuantityDenominator(), locale)
              + " "
              + getFormattedUnit(simpleOrder.getQuantityDenominatorUnit());
        }

        return quantityDisplay;
      }
      if (simpleDoseElement.getDoseRange() != null)
      {
        return new StringBuilder()
            .append(getDecimalStringValue(simpleDoseElement.getDoseRange().getMinNumerator(), locale))
            .append("–")
            .append(getDecimalStringValue(simpleDoseElement.getDoseRange().getMaxNumerator(), locale))
            .append(" ")
            .append(getFormattedUnit(simpleOrder.getQuantityUnit()))
            .toString();
      }
      if (StringUtils.isNotBlank(simpleDoseElement.getDoseDescription()))
      {
        return simpleDoseElement.getDoseDescription();
      }
    }
    if (hasTitration(simpleOrder))
    {
      return Dictionary.getEntry("titrate", GrammaticalGender.UNDEFINED, locale);
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
    return getMedicationWithGenericDisplay(medication.getName(), medication.getGenericName());
  }

  public String getMedicationDisplay(final MedicationSimpleDto medication)
  {
    if (medication == null)
    {
      return null;
    }
    if (medication.getGenericName() == null)
    {
      return medication.getName();
    }
    return getMedicationWithGenericDisplay(medication.getName(), medication.getGenericName());
  }

  private String getMedicationWithGenericDisplay(final String medicationName, final String genericName)
  {
    return genericName + " (" + medicationName + ')';
  }

  private String getVolumeSumDisplay(final Double volumeSum, final String volumeSumUnit, final Locale locale)
      throws ParseException
  {
    if (volumeSum != null)
    {
      return getDecimalStringValue(volumeSum, locale) + ' ' + getFormattedUnit(volumeSumUnit);
    }
    return null;
  }

  private String getIngredientQuantityDisplay(
      final ComplexTherapyDto complexTherapy,
      final InfusionIngredientDto ingredient,
      final Locale locale) throws ParseException
  {
    final boolean hasQuantity = ingredient.getQuantity() != null || ingredient.getQuantityDenominator() != null;
    if (hasQuantity)
    {
      final StringBuilder quantityDisplay = new StringBuilder();
      if (ingredient.getQuantity() != null)
      {
        quantityDisplay
            .append(getDecimalStringValue(ingredient.getQuantity(), locale))
            .append(" ")
            .append(getFormattedUnit(ingredient.getQuantityUnit()));
      }
      if (ingredient.getQuantityDenominator() != null)
      {
        if (ingredient.getQuantity() != null)
        {
          quantityDisplay.append("/");
        }
        quantityDisplay
            .append(getDecimalStringValue(ingredient.getQuantityDenominator(), locale))
            .append(" ")
            .append(getFormattedUnit(ingredient.getQuantityDenominatorUnit()));
      }

      return quantityDisplay.toString();
    }

    if (!complexTherapy.isContinuousInfusion() && hasTitration(complexTherapy))
    {
      return Dictionary.getEntry("titrate", GrammaticalGender.UNDEFINED, locale);
    }

    return null;
  }

  private boolean hasTitration(final TherapyDto therapy)
  {
    return therapy instanceof ConstantTherapy && ((ConstantTherapy)therapy).getTitration() != null;
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

      if (order.isContinuousInfusion() && hasTitration(constantOrder))
      {
        return Dictionary.getEntry("titrate", GrammaticalGender.UNDEFINED, locale);
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
            getDecimalStringValue(timedDoseElement.getDoseElement().getRate(), locale));
        if (index < variableOrder.getTimedDoseElements().size() - 1)
        {
          speedDisplay.append('-');
        }
        index++;
      }
      speedDisplay.append(' ');
      speedDisplay.append(getFormattedUnit(variableOrder.getTimedDoseElements().get(0).getDoseElement().getRateUnit()));
      return speedDisplay.toString();
    }
    return null;
  }

  public String getRateDisplayString(final Double rate, final String rateUnit, final Locale locale) throws ParseException
  {
    String rateDisplay = getDecimalStringValue(rate, locale);
    rateDisplay += ' ' + getFormattedUnit(rateUnit);
    return rateDisplay;
  }

  private String getSpeedFormulaDisplay(final ComplexTherapyDto therapy, final Locale locale)
      throws ParseException
  {
    if (therapy instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto complexTherapy = (ConstantComplexTherapyDto)therapy;

      if (complexTherapy.getDoseElement() != null && complexTherapy.getDoseElement().getRateFormula() != null)
      {
        final String speedFormulaString = getDecimalStringValue(complexTherapy.getDoseElement().getRateFormula(), locale);

        return speedFormulaString + ' ' + getFormattedUnit(complexTherapy.getDoseElement().getRateFormulaUnit());
      }
    }
    else if (therapy instanceof VariableComplexTherapyDto)
    {
      final VariableComplexTherapyDto variableOrder = (VariableComplexTherapyDto)therapy;

      if (variableOrder.getTimedDoseElements().get(0).getDoseElement().getRateFormula() == null)
      {
        return null;
      }

      final StringBuilder speedFormulaDisplay = new StringBuilder();
      int index = 0;
      for (final TimedComplexDoseElementDto timedDoseElement : variableOrder.getTimedDoseElements())
      {
        speedFormulaDisplay.append(getDecimalStringValue(timedDoseElement.getDoseElement().getRateFormula(), locale));
        if (index < variableOrder.getTimedDoseElements().size() - 1)
        {
          speedFormulaDisplay.append('-');
        }
        index++;
      }
      speedFormulaDisplay.append(' ');
      speedFormulaDisplay.append(
          getFormattedUnit(variableOrder.getTimedDoseElements().get(0).getDoseElement().getRateFormulaUnit()));
      return speedFormulaDisplay.toString();
    }
    else
    {
      throw new IllegalArgumentException("therapy type nut supported");
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
    String speedDisplay = getDecimalStringValue(complexDoseElement.getRate(), locale);
    speedDisplay += ' ' + getFormattedUnit(complexDoseElement.getRateUnit());
    return speedDisplay;
  }

  private String getSpeedFormulaElementDisplay(final ComplexDoseElementDto complexDoseElement, final Locale locale)
      throws ParseException
  {
    if (complexDoseElement.getRateFormula() != null)
    {
      String speedDisplay = getDecimalStringValue(complexDoseElement.getRateFormula(), locale);
      speedDisplay += ' ' + getFormattedUnit(complexDoseElement.getRateFormulaUnit());
      return speedDisplay;
    }
    return null;
  }

  private String getBaselineInfusionDisplay(final boolean baselineInfusion, final Locale locale)
  {
    if (baselineInfusion)
    {
      return Dictionary.getEntry("baseline.infusion.short", locale);
    }
    return null;
  }

  private String hourMinuteToString(final HourMinuteDto hourMinuteDto)
  {
    if (hourMinuteDto != null)
    {
      return hourMinuteDto.prettyPrint();
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
      final LocalDateTime start = new LocalDateTime(1, 1, 1, startHourMinute.getHour(), startHourMinute.getMinute());

      String intervalString = hourMinuteToString(startHourMinute) + "–";
      if (durationInMinutes != null)
      {
        final LocalDateTime end = start.plusMinutes(durationInMinutes);
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
      displayStringBuilder.append(Dictionary.getEntry(day.toLowerCase(locale) + ".f3l", locale));
      displayStringBuilder.append(' ');
    }
    return displayStringBuilder.toString().trim();
  }

  private String getDaysFrequencyDisplay(final Integer daysFrequency, final Locale locale)
  {
    if (daysFrequency != null)
    {
      return Dictionary.getEntry("every", locale).toLowerCase(locale) + ' '
          + daysFrequency + ". "
          + Dictionary.getEntry("day.lc", locale);
    }
    return null;
  }

  private String getFormattedUnit(final String unit)
  {
    final Pattern mlPattern = Pattern.compile("ml");
    final Pattern lMinPattern = Pattern.compile("l/min");

    final String formattedUnit = mlPattern.matcher(unit).replaceAll("mL");
    return lMinPattern.matcher(formattedUnit).replaceAll("L/min");
  }

  private void appendPrescriptionSupply(
      final StringBuilder description,
      final PrescriptionSupplyDto prescriptionSupplyDto,
      final Locale locale)
  {
    description.append(addLineBreak());
    if (prescriptionSupplyDto.getQuantity() != null)
    {
      description.append(
          createSpannedValue(
              Dictionary.getEntry("number.of.packages", locale).toUpperCase(),
              "PrescriptionSupply TextLabel MedicationLabel",
              true));
      description.append(
          createSpannedValue(
              String.valueOf(prescriptionSupplyDto.getQuantity()),
              "PrescriptionSupply TextData",
              true));
    }
    else
    {
      description.append(
          createSpannedValue(
              Dictionary.getEntry("number.of.therapy.duration.days", locale).toUpperCase(),
              "PrescriptionSupply TextLabel MedicationLabel",
              true));

      description.append(
          createSpannedValue(
              String.valueOf(prescriptionSupplyDto.getDaysDuration()),
              "PrescriptionSupply TextData",
              true));
    }
  }

  private void appendCommentAndClinicalIndication(
      final StringBuilder description,
      final boolean showComment,
      final TherapyDto therapy,
      final Locale locale)
  {
    if (showComment && StringUtils.isNotEmpty(therapy.getComment()) && therapy.getClinicalIndication() != null)
    {
      description.append(addLineBreak());
      description.append(
          createSpannedValue(
              Dictionary.getEntry("commentary", locale).toUpperCase(),
              "CommentLabel TextLabel MedicationLabel",
              true));
      description.append(
          createSpannedValue(
              therapy.getComment(),
              "Comment TextData",
              false));
      description.append(", ");
      description.append(
          createSpannedValue(
              Dictionary.getEntry("indication", locale).toUpperCase(),
              "IndicationLabel TextLabel MedicationLabel",
              true));
      description.append(
          createSpannedValue(
              therapy.getClinicalIndication().getName(),
              "Indication TextData",
              true));
    }
    else if (showComment && StringUtils.isNotEmpty(therapy.getComment()))
    {
      description.append(addLineBreak());
      description.append(
          createSpannedValue(
              Dictionary.getEntry("commentary", locale).toUpperCase(),
              "CommentLabel TextLabel MedicationLabel",
              true));

      description.append(createSpannedValue(therapy.getComment(), "Comment TextData", true));
    }
    else if (therapy.getClinicalIndication() != null)
    {
      description.append(addLineBreak());
      description.append(
          createSpannedValue(
              Dictionary.getEntry("indication", locale).toUpperCase(),
              "IndicationLabel TextLabel MedicationLabel",
              true));
      description.append(
          createSpannedValue(
              therapy.getClinicalIndication().getName(),
              "Indication TextData",
              true));
    }
  }

  public String getDecimalStringValue(final Double value, final Locale locale) throws ParseException
  {
    final boolean isDecimalNumber = value != null && value < 1 && value != 0;
    if (isDecimalNumber)
    {
      //if decimal, print until three nonzero numbers are visible
      final String[] numberSplittedByDecimalPoint = BigDecimal.valueOf(value).toPlainString().split("\\.");
      final Pattern pattern = Pattern.compile("[1-9]");
      final Matcher matcher = pattern.matcher(numberSplittedByDecimalPoint[1]);
      if (matcher.find())
      {
        final int firstNonZeroNumberPosition = matcher.start();
        final String hashes = StringUtils.repeat("#", firstNonZeroNumberPosition + 3);
        return NumberFormatters.adjustableDoubleFormatter("0." + hashes, locale).valueToString(value);
      }
    }
    return NumberFormatters.doubleFormatter3(locale).valueToString(value);
  }
}
