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

package com.marand.thinkmed.medications.dto.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.core.Dictionary;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.SimpleMedicationOrderDoseDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Primoz Prislan
 */
public class TherapyDayReportUtils
{
  private static final String SPACER = " ";
  //private static final String SPACER = "&thinsp;";
  private static final String VALUES_DELIMITER_BEFORE_AFTER =
      SPACER + com.marand.maf.core.StringUtils.VALUES_DELIMITER_POINT + SPACER;
  private static final String LABEL_COLOR = "#666666";

  private static Dictionary dictionary = null;

  private TherapyDayReportUtils()
  {
  }

  public static void init(final Dictionary dictionary)
  {
    if (TherapyDayReportUtils.dictionary != null)
    {
      throw new IllegalArgumentException(TherapyDayReportUtils.class.getSimpleName() + " already initialized!");
    }
    TherapyDayReportUtils.dictionary = Preconditions.checkNotNull(dictionary);
  }

  public static String getPatientDataDisplay(
      @Nonnull final PatientDataForTherapyReportDto patientData, @Nonnull final Locale locale)
  {
    final Gender gender = patientData.getGender();
    return
        "<font size=\"3\">" +
        getValueHtml(
            patientData.getPatientName(),
            true,
            false,
            false,
            false,
            locale) +
        "</font>" +
        getValueHtml(
            dictionary.getEntry("birthdate", gender.getGrammaticalGender(), locale),
            patientData.getBirthDateAndAge(),
            true,
            false,
            false,
            true,
            locale) +
        VALUES_DELIMITER_BEFORE_AFTER +
        getValueHtml(
            dictionary.getEntry(gender.getShortEntryKey(), gender.getGrammaticalGender(), locale),
            true,
            false,
            false,
            false,
            locale) +
        getValueHtml(
            patientData.getPatientIdentificatorType(),
            patientData.getPatientIdentificator(),
            true,
            false,
            false,
            true,
            locale) +
        getValueHtml(
            "address",
            patientData.getAddressDisplay(),
            true,
            false,
            false,
            true,
            locale) +
        getValueHtml(
            "case",
            patientData.getCentralCaseIdNumber(),
            true,
            false,
            false,
            true,
            locale) +
        getValueHtml(
            "weight",
            patientData.getWeight(),
            true,
            false,
            false,
            true,
            locale);
  }

  public static String getPatientHospitalizationDataDisplay(
      @Nonnull final PatientDataForTherapyReportDto patientData, @Nonnull final Locale locale)
  {
    final StringBuilder strBuilder = new StringBuilder();

    strBuilder.append(
        getValueHtml(
            patientData.isInpatient() ? "admission.date" : "encounter",
            patientData.getAdmissionDate(),
            true,
            true,
            false,
            false,
            locale));
    if (patientData.isInpatient())
    {
      final String hospitalizationConsecutive =
          patientData.getHospitalizationConsecutiveDay() != null ?
          String.valueOf(patientData.getHospitalizationConsecutiveDay()) : null;
      strBuilder.append(
          getValueHtml(
              "hospitalization.day",
              hospitalizationConsecutive,
              true,
              false,
              false,
              true,
              locale));
    }

    return strBuilder.toString();
  }

  public static String getSimpleCombinedDisplay(
      @Nonnull final SimpleTherapyDto order,
      final String therapyStart,
      final String therapyEnd,
      @Nonnull final Locale locale)
  {
    final StringBuilder strBuilder = new StringBuilder();

    return
        strBuilder
            .append(getMedicationDisplay(order.getMedication()))
            .append(getValueHtml("dose", getQuantityDisplay(order), false, true, false, strBuilder.length() > 0, locale))
            .append(
                getValueHtml(
                    "dosing.interval",
                    combineFrequencyAndDaysOfWeek(order.getFrequencyDisplay(), order.getDaysOfWeekDisplay()),
                    false,
                    false,
                    false,
                    strBuilder.length() > 0,
                    locale))
            .append(getAdditionalDoseDisplay(order, locale))
            .append(
                getValueHtml(
                    "from",
                    getTherapyIntervalDisplay(therapyStart, therapyEnd, locale),
                    false,
                    true,
                    false,
                    strBuilder.length() > 0,
                    locale))
            .toString();
  }

  public static List<SimpleMedicationOrderDoseDto> getSimpleMedicationDoseElements(
      @Nonnull final SimpleTherapyDto order, @Nonnull final Locale locale)
  {
    final List<SimpleMedicationOrderDoseDto> elements = new ArrayList<>();

    if (order instanceof ConstantSimpleTherapyDto)
    {
      elements.add(new SimpleMedicationOrderDoseDto(order.getQuantityDisplay()));
    }
    else
    {
      for (final TimedSimpleDoseElementDto element : ((VariableSimpleTherapyDto)order).getTimedDoseElements())
      {
        elements.add(
            new SimpleMedicationOrderDoseDto(
                element.getTimeDisplay() + SPACER + '-' + SPACER + element.getQuantityDisplay()));
      }
    }

    return elements;
  }

  public static String getComplexCombinedDisplay(
      @Nonnull final ComplexTherapyDto order,
      final String therapyStart,
      final String therapyEnd,
      @Nonnull final Locale locale)
  {
    // for empty report
    if (order.getIngredientsList().isEmpty())
    {
      return "<BR><BR><BR><BR><BR><BR><BR><BR>";
    }

    final String volumeDisplay = getVolumeDisplay(order, locale);
    final String additionalInstructionDisplay = order.getAdditionalInstructionDisplay();

    final StringBuilder strBuilder = new StringBuilder();

    return
        strBuilder
            .append(
                getValueHtml(
                    volumeDisplay,
                    false,
                    false,
                    false,
                    strBuilder.length() > 0,
                    locale))
            .append(
                getValueHtml(
                    "rate",
                    getQuantityDisplay(order),
                    false,
                    false,
                    strBuilder.length() > 0
                        && StringUtils.isNotBlank(volumeDisplay)
                        && StringUtils.isBlank(additionalInstructionDisplay)
                        && order instanceof ConstantComplexTherapyDto,
                    strBuilder.length() > 0,
                    locale))
            //.append(strBuilder.length() > 0 && order instanceof CombinedComplexMedicationOrderDto ? "<br> " : "")
            .append(
                getValueHtml(
                    "dose",
                    order.isContinuousInfusion() ? order.getSpeedFormulaDisplay() : null,
                    false,
                    false,
                    strBuilder.length() > 0,
                    false,
                    locale))
            .append(
                order instanceof ConstantComplexTherapyDto
                ? getValueHtml(
                    "duration",
                    ((ConstantComplexTherapyDto)order).getDurationDisplay(),
                    false,
                    false,
                    strBuilder.length() > 0,
                    false,
                    locale)
                : "")
            .append(
                order.isContinuousInfusion()
                ? getValueHtml("continuous.infusion", null, false, true, false, strBuilder.length() > 0, locale)
                : "")
            .append(
                getValueHtml(
                    "dosing.interval",
                    combineFrequencyAndDaysOfWeek(order.getFrequencyDisplay(), order.getDaysOfWeekDisplay()),
                    false,
                    true,
                    false,
                    strBuilder.length() > 0,
                    locale))
            .append(getAdditionalDoseDisplay(order, locale))
            .append(
                getValueHtml(
                    "from",
                    getTherapyIntervalDisplay(therapyStart, therapyEnd, locale),
                    false,
                    true,
                    false,
                    strBuilder.length() > 0,
                    locale))
            .toString();
  }

  private static String getAdditionalDoseDisplay(final TherapyDto order, final Locale locale)
  {
    final StringBuilder strBuilder = new StringBuilder();

    final String display = getWhenNeededOrStartCriterionDisplay(order);
    if (StringUtils.isNotBlank(display))
    {
      strBuilder
          .append(VALUES_DELIMITER_BEFORE_AFTER)
          .append(getValueHtml(display, false, true, false, false, locale))
      ;
    }
    strBuilder
        .append(VALUES_DELIMITER_BEFORE_AFTER)
        .append(getValueHtml(order.getRoute().getName(), false, true, false, false, locale))
    ;

    return strBuilder.toString();
  }

  private static String getVolumeDisplay(final ComplexTherapyDto order, final Locale locale)
  {
    final StringBuilder strBuilder = new StringBuilder();

    strBuilder.append(
        getValueHtml("volume.total", order.getVolumeSumDisplay(), false, false, false, false, locale));

    final String additionalInstructionDisplay = order.getAdditionalInstructionDisplay();
    if (StringUtils.isNotBlank(additionalInstructionDisplay))
    {
      strBuilder.append(
          getValueHtml("+ " + additionalInstructionDisplay, false, strBuilder.length() > 0, false, false, locale));
    }

    return strBuilder.toString();
  }

  public static String getMedicationDisplay(@Nonnull final MedicationDto medication)
  {
    return
        medication.getGenericName() == null || medication.getMedicationType() == MedicationTypeEnum.SOLUTION
        ? medication.getName()
        : "<b>" + medication.getGenericName() + "</b>" + SPACER + '(' + medication.getName() + ')';
  }

  public static String getWhenNeededOrStartCriterionDisplay(@Nonnull final TherapyDto order)
  {
    final StringBuilder strBuilder = new StringBuilder();
    if (StringUtils.isNotBlank(order.getWhenNeededDisplay()))
    {
      strBuilder.append(order.getWhenNeededDisplay());
    }
    if (StringUtils.isNotBlank(order.getStartCriterionDisplay()))
    {
      strBuilder.append(order.getStartCriterionDisplay());
    }
    return strBuilder.toString();
  }

  private static String getTherapyIntervalDisplay(final String therapyStart, final String therapyEnd, final Locale locale)
  {
    return therapyStart + (therapyEnd == null ? "" : "&nbsp;" + getLabelHtml("to", locale) + "&nbsp;" + therapyEnd);
  }

  private static String getQuantityDisplay(final SimpleTherapyDto order)
  {
    if (order instanceof ConstantSimpleTherapyDto)
    {
      return order.getQuantityDisplay();
    }
    else
    {
      final StringBuilder strBuilder = new StringBuilder();
      for (final TimedSimpleDoseElementDto element : ((VariableSimpleTherapyDto)order).getTimedDoseElements())
      {
        strBuilder.append(combineTimeAndQuantity(element.getTimeDisplay(), element.getQuantityDisplay()));
      }
      return strBuilder.toString();
    }
  }

  private static String getQuantityDisplay(final ComplexTherapyDto order)
  {
    if (order.isAdjustToFluidBalance())
    {
      return dictionary.getEntry("adjust.to.fluid.balance.short", null, null);
    }
    if (order instanceof ConstantComplexTherapyDto)
    {
      return order.getSpeedDisplay();
    }
    else
    {
      final StringBuilder strBuilder = new StringBuilder();
      for (final TimedComplexDoseElementDto element : ((VariableComplexTherapyDto)order).getTimedDoseElements())
      {
        strBuilder.append(combineTimeAndQuantity(element.getIntervalDisplay(), element.getSpeedDisplay()));
      }
      return strBuilder.toString();
    }
  }

  private static String combineTimeAndQuantity(final String time, final String quantity)
  {
    return "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + time + "&nbsp;-&nbsp;" + quantity;
  }

  private static String combineFrequencyAndDaysOfWeek(final String frequency, final String daysOfWeek)
  {
    return
        StringUtils.defaultString(frequency)
            + (StringUtils.isBlank(daysOfWeek) ? "" : VALUES_DELIMITER_BEFORE_AFTER + daysOfWeek);
  }

  private static String getValueHtml(
      final String value,
      final boolean highlightValue,
      final boolean addEmptyValue,
      final boolean addSpacerBefore,
      final boolean addNewLineBefore,
      final Locale locale)
  {
    return getValueHtml(null, value, highlightValue, addEmptyValue, addSpacerBefore, addNewLineBefore, locale);
  }

  private static String getValueHtml(
      final String labelOrKey,
      final String value,
      final boolean highlightValue,
      final boolean addEmptyValue,
      final boolean addSpacerBefore,
      final boolean addNewLineBefore,
      final Locale locale)
  {
    if (!addEmptyValue && StringUtils.isBlank(value))
    {
      return "";
    }

    final String label = getLabelHtml(labelOrKey, locale);
    return
        (addSpacerBefore ? VALUES_DELIMITER_BEFORE_AFTER : "")
            + (!addSpacerBefore && addNewLineBefore ? "<br>" : "")
            + (StringUtils.isNotBlank(label) ? label + "&nbsp;" : "")
            + (highlightValue ? "<b>" : "")
            + StringUtils.defaultString(value)
            + (highlightValue ? "</b>" : "");
  }

  private static String getLabelHtml(final String labelOrKey, final Locale locale)
  {
    return
        StringUtils.isBlank(labelOrKey)
        ? ""
        : "<span style=\"color:" + LABEL_COLOR + "; font-size:5px;\">"
            + dictionary.getEntry(labelOrKey, null, locale).toUpperCase()
            + "</span>";
  }
}