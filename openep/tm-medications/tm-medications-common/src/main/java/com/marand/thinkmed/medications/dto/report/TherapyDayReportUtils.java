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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.formatter.NumberFormatters;
import com.marand.thinkmed.api.core.Dictionary;
import com.marand.thinkmed.api.core.GrammaticalGender;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.SimpleMedicationOrderDoseDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

/**
 * @author Primoz Prislan
 */
@SuppressWarnings("unused")
public class TherapyDayReportUtils
{
  private static final String SPACER = " ";

  private static final String VALUES_DELIMITER_BEFORE_AFTER =
      SPACER + "-" + SPACER;

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

  public static String getLegend(final int column, @Nonnull final Locale locale)
  {
    switch (column)
    {
      case 1:
        return
            createLegend("Strt", "Start", false)
                + "<BR>" + createLegend("Stp", "Stop", false);
      case 2:
        return
            createLegend("G", "Given", false)
                + "<BR>" + createLegend("L", "Late", false);
      case 3:
        return
            createLegend("D", "Defer", false)
                + "<BR>" + createLegend("pna", "patient not avaliable", true)
                + "<BR>" + createLegend("mna", "medication not avaliable", true)
                + "<BR>" + createLegend("cr", "clinical reason", true);
      case 4:
        return
            createLegend("SA", "Self-Administer", false)
                + "<BR>" + createLegend("cn", "charted by nurse", true)
                + "<BR>" + createLegend("ac", "automatically charted", true);
      case 5:
        return
            createLegend("NG", "Not given", false)
                + "<BR>" + createLegend("pr", "patient refuse", true)
                + "<BR>" + createLegend("nm", "nill by mouth", true)
                + "<BR>" + createLegend("mu", "medicine unavaliable", true);
      case 6:
        return
            ""
                + "<BR>" + createLegend("pu", "patient unavaliable", true)
                + "<BR>" + createLegend("cr", "clinical reason", true)
                + "<BR>" + createLegend("mfr", "medicine free interval", true);
      default:
        return "";
    }
  }

  private static String createLegend(final String key, final String value, final boolean child)
  {
    return (child ? "&nbsp;&nbsp;&nbsp;" : "") + "<B>" + key + "</B>&thinsp;&ndash;&thinsp;" + value;
  }

  public static String getPatientDataDisplay(
      @Nonnull final PatientDataForTherapyReportDto patientData,
      @Nonnull final Locale locale)
  {
    try
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
    catch (Exception e)
    {
      return exceptionToString(e);
    }
  }

  public static String getPatientHospitalizationDataDisplay(
      @Nonnull final PatientDataForTherapyReportDto patientData, @Nonnull final Locale locale)
  {
    try
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
    catch (Exception e)
    {
      return exceptionToString(e);
    }
  }

  public static String getSimpleCombinedDisplay(
      @Nonnull final SimpleTherapyDto order,
      final String therapyStart,
      final String therapyEnd,
      final TherapyReportStatusEnum therapyReportStatus,
      @Nonnull final Locale locale)
  {
    try
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
              .append(getTherapyStatusDisplay(therapyReportStatus, locale))
              .toString();
    }
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  public static List<SimpleMedicationOrderDoseDto> getSimpleMedicationDoseElements(
      @Nonnull final SimpleTherapyDto order,
      @Nonnull final Locale locale)
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
        elements.add(new SimpleMedicationOrderDoseDto(element.getTimeDisplay() + SPACER + '-' + SPACER + element.getQuantityDisplay()));
      }
    }

    return elements;
  }

  // column = 0 based
  public static String getTherapyApplicationColumnLabel(
      @Nonnull final Date startDate,
      final int column)
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.roll(Calendar.DAY_OF_MONTH, column);

    return calendar.get(Calendar.DAY_OF_MONTH) + ".";
  }

  // column = 0 based
  public static String getTherapyApplicationColumnValue(
      List<AdministrationDto> therapyAdministrations,
      @Nonnull final TherapyDto order,
      @Nonnull final Date startDate,
      final int column,
      @Nonnull Locale locale)
  {
    final DateTime day = new DateTime(startDate).plusDays(column).withTimeAtStartOfDay();
    final StringBuilder administrationString = new StringBuilder();

    if (therapyAdministrations != null)
    {
      Collections.sort(therapyAdministrations, Comparator.comparing(AdministrationDto::getAdministrationTime));
      for (final AdministrationDto administration : therapyAdministrations)
      {
        final DateTime administrationTime = administration.getAdministrationTime();
        if (administrationTime != null && administrationTime.withTimeAtStartOfDay().isEqual(day))
        {
          final StringBuilder dose = new StringBuilder();

          if (administration.getAdministrationResult() == AdministrationResultEnum.DEFER)
          {
            dose.append(StringUtils.capitalize(getDictionaryEntry("administration.defer", locale)));

            if (administration.getNotAdministeredReason() != null
                && administration.getNotAdministeredReason().getName() != null)
            {
              dose.append(" - ").append(administration.getNotAdministeredReason().getName());
            }
          }
          else if (administration.getAdministrationResult() == AdministrationResultEnum.NOT_GIVEN)
          {
            dose.append(StringUtils.capitalize(getDictionaryEntry("administration.not.given", locale)));

            if (administration.getNotAdministeredReason() != null
                && administration.getNotAdministeredReason().getName() != null)
            {
              dose.append(" - ").append(administration.getNotAdministeredReason().getName());
            }
          }
          else
          {
            if (administration instanceof StartAdministrationDto)
            {
              final TherapyDoseDto administeredDose = ((StartAdministrationDto)administration).getAdministeredDose();
              if (administeredDose != null &&
                  administeredDose.getNumerator() != null &&
                  administeredDose.getNumeratorUnit() != null)
              {
                try
                {
                  dose.append(NumberFormatters.doubleFormatter2(locale)
                                  .valueToString(administeredDose.getNumerator()) + " " + administeredDose.getNumeratorUnit());
                }
                catch (final ParseException e)
                {
                  e.printStackTrace();
                }
              }
            }
            else if (administration instanceof AdjustInfusionAdministrationDto)
            {
              final TherapyDoseDto administeredDose = ((AdjustInfusionAdministrationDto)administration).getAdministeredDose();
              if (administeredDose.getNumerator() != null && administeredDose.getNumeratorUnit() != null)
              {
                try
                {
                  dose.append(
                      NumberFormatters.doubleFormatter2(locale).valueToString(administeredDose.getNumerator())
                          + " " + administeredDose.getNumeratorUnit());
                }
                catch (ParseException e)
                {
                  e.printStackTrace();
                }
              }
            }
            else if (administration instanceof InfusionSetChangeDto)
            {
              //noinspection SwitchStatement
              switch (((InfusionSetChangeDto)administration).getInfusionSetChangeEnum())
              {
                case INFUSION_SYRINGE_CHANGE:
                  dose.append(getDictionaryEntry("infusion.syringe.change", locale));
                  break;

                case INFUSION_SYSTEM_CHANGE:
                  dose.append(getDictionaryEntry("infusion.system.change", locale));
                  break;
              }
            }
            else if (administration instanceof StopAdministrationDto)
            {
              dose.append(getDictionaryEntry("infusion.stopped", locale));
            }
          }

          if (administration.getComment() != null)
          {
            dose.append(" - ").append(administration.getComment());
          }

          administrationString.append(String.format(
              "<b>%02d:%02d<br></b>%s<br><br>",
              administrationTime.getHourOfDay(),
              administrationTime.getMinuteOfHour(),
              dose));
        }
      }
    }

    return administrationString.toString();
  }

  // line = 0 based
  public static String getTherapyApplicationComment(
      @Nonnull final TherapyDto order,
      final TherapyPharmacistReviewStatusEnum pharmacistsReviewState,
      final int line,
      @Nonnull final Locale locale)
  {
    if (line == 0)
    {
      final StringBuilder comment = new StringBuilder();

      if (order.getComment() != null)
      {
        String commentString = StringUtils.capitalize(getDictionaryEntry("comment", locale));
        comment.append(commentString).append(": <B>").append(order.getComment()).append("</B>     ");
      }
      if (order.getClinicalIndication() != null)
      {
        String indicationString = StringUtils.capitalize(getDictionaryEntry("indication", locale));
        comment.append(indicationString).append(": <B>").append(order.getClinicalIndication().getName()).append("</B>     ");
      }
      if (order.getClinicalIndication() != null)
      {
        String pharmacistReview = null;
        if (pharmacistsReviewState == TherapyPharmacistReviewStatusEnum.REVIEWED)
        {
          pharmacistReview = StringUtils.capitalize(getDictionaryEntry("verified", locale));
        }
        else if (pharmacistsReviewState == TherapyPharmacistReviewStatusEnum.NOT_REVIEWED)
        {
          pharmacistReview = StringUtils.capitalize(getDictionaryEntry("unverified", locale));
        }
        else if (pharmacistsReviewState == TherapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK)
        {
          final String referredBackString = getDictionaryEntry("referred.back", locale);
          pharmacistReview = StringUtils.capitalize(getDictionaryEntry("verified", locale)) + " - " + referredBackString;
        }

        if (pharmacistReview != null)
        {
          final String pharmacyVerificationString = StringUtils.capitalize(getDictionaryEntry(
              "pharmacy.verification",
              locale));

          comment.append(pharmacyVerificationString).append(": <B>").append(pharmacistReview).append("</B>");
        }
      }
      return comment.toString();
    }
    else if (line == 1)
    {
      final StringBuilder warning = new StringBuilder();
      if (order.getCriticalWarnings() != null)
      {
        for (final String criticalWarning : order.getCriticalWarnings())
        {
          final String warningOverriddenString = StringUtils.capitalize(getDictionaryEntry("warning.overridden", locale));
          warning.append(warningOverriddenString).append(": <B>").append(criticalWarning).append("</B>");
          if (order.getCriticalWarnings().size() > 1)
          {
            warning.append("<br>");
          }
        }
      }
      return warning.toString();
    }
    else
    {
      return exceptionToString(new UnsupportedOperationException());
    }
  }

  public static String getComplexCombinedDisplay(
      @Nonnull final ComplexTherapyDto order,
      final String therapyStart,
      final String therapyEnd,
      final TherapyReportStatusEnum therapyReportStatus,
      @Nonnull final Locale locale)
  {
    try
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
                      getQuantityDisplay(order, locale),
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
                  order instanceof VariableComplexTherapyDto
                  ? ""
                  : getValueHtml(
                      "dose",
                      order.getSpeedFormulaDisplay(),
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
              .append(getRecurringContinuousInfusionDisplay(order, locale))
              .append(
                  getValueHtml(
                      order.isContinuousInfusion() ? "continuous.infusion" : "dosing.interval",
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
              .append(getTherapyStatusDisplay(therapyReportStatus, locale))
              .toString();
    }
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  private static String getRecurringContinuousInfusionDisplay(final ComplexTherapyDto therapyDto, final Locale locale)
  {
    if (therapyDto instanceof VariableComplexTherapyDto &&
        ((VariableComplexTherapyDto)therapyDto).isRecurringContinuousInfusion())
    {
      return "<BR>" + getLabelHtml("repeat.every.24h", locale);
    }
    return "";
  }

  private static String getAdditionalDoseDisplay(final TherapyDto order, final Locale locale)
  {
    final StringBuilder strBuilder = new StringBuilder();

    final String display = getWhenNeededOrStartCriterionDisplay(order);
    if (StringUtils.isNotBlank(display))
    {
      strBuilder
          .append(VALUES_DELIMITER_BEFORE_AFTER)
          .append(getValueHtml(display, false, true, false, false, locale));
    }
    if (StringUtils.isNotBlank(order.getApplicationPreconditionDisplay()))
    {
      strBuilder
          .append(VALUES_DELIMITER_BEFORE_AFTER)
          .append(getValueHtml(order.getApplicationPreconditionDisplay(), false, true, false, false, locale));
    }

    if (!order.getRoutes().isEmpty())
    {
      strBuilder.append(VALUES_DELIMITER_BEFORE_AFTER);
      strBuilder.append(
          order.getRoutes()
              .stream()
              .map(route -> getValueHtml(route.getName(), false, true, false, false, locale))
              .collect(Collectors.joining(", ")));
    }

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
          getValueHtml(" + " + additionalInstructionDisplay, false, strBuilder.length() > 0, false, false, locale));
    }

    return strBuilder.toString();
  }

  public static String getMedicationDisplay(@Nonnull final MedicationDto medication)
  {
    try
    {
      return
          medication.getGenericName() == null || medication.getMedicationType() == MedicationTypeEnum.SOLUTION
          ? medication.getName()
          : "<b>" + medication.getGenericName() + "</b>" + SPACER + '(' + medication.getName() + ')';
    }
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  public static String getWhenNeededOrStartCriterionDisplay(@Nonnull final TherapyDto order)
  {
    try
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
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  private static String getTherapyIntervalDisplay(final String therapyStart, final String therapyEnd, final Locale locale)
  {
    return therapyStart + (therapyEnd == null ? "" : "&nbsp;" + getLabelHtml("to", locale) + "&nbsp;" + therapyEnd);
  }

  private static String getTherapyStatusDisplay(final TherapyReportStatusEnum therapyReportStatus, final Locale locale)
  {
    if (therapyReportStatus == TherapyReportStatusEnum.FINISHED)
    {
      return "<BR>" + getLabelHtml("status", locale) + " " + dictionary.getEntry(
          "stopped.therapy",
          GrammaticalGender.UNDEFINED,
          locale);
    }
    else if (therapyReportStatus == TherapyReportStatusEnum.SUSPENDED)
    {
      return "<BR>" + getLabelHtml("status", locale) + " " + dictionary.getEntry(
          "suspended.therapy",
          GrammaticalGender.UNDEFINED,
          locale);
    }
    return "";
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
      final List<TimedSimpleDoseElementDto> timedDoseElements = ((VariableSimpleTherapyDto)order).getTimedDoseElements()
          .stream()
          .filter(element -> element.getQuantityDisplay() != null)
          .collect(Collectors.toList());

      final boolean simpleVariable = timedDoseElements.get(0).getDate() == null;

      if (simpleVariable)
      {
        timedDoseElements
            .forEach(element -> strBuilder.append(combineTimeAndQuantity(
                null,
                element.getTimeDisplay(),
                element.getQuantityDisplay())));
      }
      else
      {
        Collections.sort(timedDoseElements, Comparator.comparing(TimedSimpleDoseElementDto::getDate));

        DateTime currentDate = null;
        for (final TimedSimpleDoseElementDto timedDoseElement : timedDoseElements)
        {
          if (timedDoseElement.getDate().equals(currentDate))
          {
            strBuilder.append(combineTimeAndQuantity(
                null,
                timedDoseElement.getTimeDisplay(),
                timedDoseElement.getQuantityDisplay()));
          }
          else
          {
            strBuilder.append(combineTimeAndQuantity(
                timedDoseElement.getDate(),
                timedDoseElement.getTimeDisplay(),
                timedDoseElement.getQuantityDisplay()));

            currentDate = timedDoseElement.getDate();
          }
        }
      }

      return strBuilder.toString();
    }
  }

  private static String getQuantityDisplay(final ComplexTherapyDto order, final Locale locale)
  {
    if (order.isAdjustToFluidBalance())
    {
      return dictionary.getEntry("adjust.to.fluid.balance.short", GrammaticalGender.UNDEFINED, locale);
    }
    if (order instanceof ConstantComplexTherapyDto)
    {
      return order.getSpeedDisplay();
    }
    else
    {
      final StringBuilder strBuilder = new StringBuilder();
      if (order instanceof VariableComplexTherapyDto)
      {
        for (final TimedComplexDoseElementDto element : ((VariableComplexTherapyDto)order).getTimedDoseElements())
        {
          final String speedFormulaDisplay = element.getSpeedFormulaDisplay() != null
                                             ? " (" + element.getSpeedFormulaDisplay() + ")"
                                             : "";

          strBuilder.append(
              combineTimeAndQuantity(null, element.getIntervalDisplay(), element.getSpeedDisplay() + speedFormulaDisplay));
        }
        return strBuilder.toString();
      }
      else
      {
        return "";
      }
    }
  }

  private static String combineTimeAndQuantity(final DateTime date, final String time, final String quantity)
  {
    final StringBuilder sb = new StringBuilder();

    if (date != null)
    {
      sb.append("<br>" + date.getDayOfMonth() + "." + date.getMonthOfYear());
    }

    sb.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;" + time + "&nbsp;-&nbsp;" + quantity);

    return sb.toString();
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
    if (StringUtils.isBlank(labelOrKey))
    {
      return "";
    }
    String entry;
    try
    {
      entry = dictionary.getEntry(labelOrKey, GrammaticalGender.UNDEFINED, locale);
    }
    catch (final MissingResourceException ex)
    {
      entry = labelOrKey;
    }
    return "<span style=\"color:" + LABEL_COLOR + "; font-size:5px;\">"
               + entry != null ? entry.toUpperCase() : labelOrKey.toUpperCase()
               + "</span>";
  }

  public static String getDictionaryEntry(final String key, final Locale locale)
  {
    try
    {
      return dictionary.getEntry(key, GrammaticalGender.UNDEFINED, locale);
    }
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  private static String exceptionToString(final Exception e)
  {
    return "<span style='color:red; font-size:10px;'>" + e.toString() + "</span>";
  }

  public static String getPatientAllergies(final PatientDataForTherapyReportDto patientData)
  {
    final String presenceOfAllergies = null;
    return (presenceOfAllergies == null ? "" : "<B>" + presenceOfAllergies + "</B><BR>") + patientData.getAllergies()
        .toString().replace("[", "").replace(", ", "<BR>").replace("]", "");
  }
}