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

package com.marand.thinkmed.medications.business.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.ispek.ehr.common.IspekEhrUtils;
import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.ispek.ehr.common.tdo.IspekComposition;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.openehr.visitor.IspekTdoDataSupport;
import com.marand.maf.core.openehr.visitor.TdoPopulatingVisitor;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MaximumDoseCluster.MedicationAmountCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster.DayOfWeek;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection;
import com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.InfusionAdministrationDetailsPurpose;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.medications.tdo.StructuredDoseCluster;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkehr.util.ConversionUtils;
import com.marand.thinkehr.web.FdoConstants;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.ParticipationTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.ConstantTherapy;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.IndicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSiteDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.dose.DoseRangeDto;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvDate;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.DvIdentifier;
import org.openehr.jaxb.rm.DvInterval;
import org.openehr.jaxb.rm.DvParsable;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.DvTime;
import org.openehr.jaxb.rm.InstructionDetails;
import org.openehr.jaxb.rm.IsmTransition;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.Locatable;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.openehr.jaxb.rm.Participation;
import org.openehr.jaxb.rm.PartyIdentified;
import org.openehr.jaxb.rm.PartyProxy;

import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster.IngredientQuantityCluster;
import static com.marand.openehr.medications.tdo.StructuredDoseCluster.RatioDenominatorCluster;
import static com.marand.openehr.medications.tdo.StructuredDoseCluster.RatioNumeratorCluster;

/**
 * @author Bostjan Vester
 */
public final class MedicationsEhrUtils
{
  public static final String BOLUS = "BOLUS";

  private MedicationsEhrUtils()
  {
  }

  public static MedicationOrderComposition createEmptyMedicationOrderComposition()
  {
    final MedicationOrderComposition composition = new MedicationOrderComposition();
    final MedicationDetailSection compositionDetail = new MedicationDetailSection();
    final List<MedicationInstructionInstruction> instructionsList = new ArrayList<>();
    compositionDetail.setMedicationInstruction(instructionsList);
    composition.setMedicationDetail(compositionDetail);

    return composition;
  }

  public static MedicationInstructionInstruction createEmptyMedicationInstruction()
  {
    return new MedicationInstructionInstruction();
  }

  public static boolean isSimpleTherapy(final OrderActivity order)
  {
    return order != null &&
        (order.getIngredientsAndForm() == null || order.getIngredientsAndForm().getIngredient().isEmpty());
  }

  public static boolean isSimpleTherapy(final MedicationActionAction medicationAction)
  {
    return medicationAction != null &&
        (medicationAction.getIngredientsAndForm() == null || medicationAction.getIngredientsAndForm().getIngredient().isEmpty());
  }

  public static boolean isSimpleInstruction(final MedicationInstructionInstruction instruction)
  {
    return instruction != null && isSimpleTherapy(instruction.getOrder().get(0));
  }

  public static boolean isOxygenInstruction(final MedicationInstructionInstruction instruction)
  {
    return !instruction.getOrder().isEmpty()
        && instruction.getOrder().get(0).getAdministrationDetails() != null
        && instruction.getOrder().get(0).getAdministrationDetails().getOxygenDelivery().size() == 1;
  }

  public static boolean isVariableInstruction(final MedicationInstructionInstruction instruction)
  {
    return instruction != null && instruction.getOrder().size() > 1;
  }

  public static boolean isVariableDaysInstruction(final MedicationInstructionInstruction instruction)
  {
    return instruction != null && instruction.getOrder().size() > 1 &&
        instruction.getOrder().get(0).getMedicationTiming() != null &&
        instruction.getOrder().get(0).getMedicationTiming().getTiming() != null &&
        !instruction.getOrder().get(0).getMedicationTiming().getTiming().getDate().isEmpty();
  }

  public static boolean isContinuousInfusion(final MedicationInstructionInstruction instruction)
  {
    return instruction != null &&
        instruction.getOrder().get(0).getAdministrationDetails() != null &&
        MedicationDeliveryMethodEnum.isContinuousInfusion(
            instruction.getOrder().get(0).getAdministrationDetails().getDeliveryMethod());
  }

  public static boolean isRecurringContinuousInfusion(final MedicationInstructionInstruction instruction)
  {
    return instruction != null && instruction.getOrder().get(0).getAdministrationDetails() != null &&
        MedicationDeliveryMethodEnum.RECURRING_CONTINUOUS_INFUSION
            .isEqualTo(instruction.getOrder().get(0).getAdministrationDetails().getDeliveryMethod());
  }

  public static boolean isWhenNeeded(final MedicationInstructionInstruction instruction)
  {
    return instruction != null && instruction.getOrder().get(0).getMedicationTiming() != null &&
        instruction.getOrder().get(0).getMedicationTiming().getPRN() != null &&
        instruction.getOrder().get(0).getMedicationTiming().getPRN().isValue();
  }

  public static DayOfWeek dayOfWeekToEhrEnum(final DateTime dateTime)
  {
    if (dateTime.getDayOfWeek() == DateTimeConstants.MONDAY)
    {
      return DayOfWeek.MONDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.TUESDAY)
    {
      return DayOfWeek.TUESDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.WEDNESDAY)
    {
      return DayOfWeek.WEDNESDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.THURSDAY)
    {
      return DayOfWeek.THURSDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.FRIDAY)
    {
      return DayOfWeek.FRIDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.SATURDAY)
    {
      return DayOfWeek.SATURDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.SUNDAY)
    {
      return DayOfWeek.SUNDAY;
    }
    throw new IllegalArgumentException("Day of week conversion error");
  }

  public static OrderActivity createEmptyOrderActivityFor(final MedicationInstructionInstruction instruction)
  {
    final OrderActivity orderActivity = new OrderActivity();
    final DvParsable timing = new DvParsable();
    timing.setValue(ISOPeriodFormat.standard().print(new Period(0, 0, 0, 1, 0, 0, 0, 0)));
    timing.setFormalism(IspekEhrUtils.TIMING_FORMALISM);
    orderActivity.setTiming(timing);
    orderActivity.setActionArchetypeId("[openEHR-EHR-ACTION.medication.v1]");
    instruction.getOrder().add(orderActivity);
    return orderActivity;
  }

  public static AdministrationDetailsCluster createAdministrationDetailsFor(final OrderActivity order)
  {
    final AdministrationDetailsCluster administrationDetails = new AdministrationDetailsCluster();
    order.setAdministrationDetails(administrationDetails);
    return administrationDetails;
  }

  public static IngredientsAndFormCluster createIngredientsAndFormFor(final OrderActivity orderActivity)
  {
    final IngredientsAndFormCluster ingredientsAndForm = new IngredientsAndFormCluster();
    orderActivity.setIngredientsAndForm(ingredientsAndForm);
    return ingredientsAndForm;
  }

  public static OrderActivity setMedicineDescription(
      final OrderActivity orderActivity,
      final String directions,
      final String medicineName,
      final Long medicineId)
  {
    if (medicineName != null)
    {
      if (medicineId != null)
      {
        orderActivity.setMedicine(DataValueUtils.getLocalCodedText(String.valueOf(medicineId), medicineName));
      }
      else
      {
        orderActivity.setMedicine(DataValueUtils.getText(medicineName));
      }
    }
    else
    {
      orderActivity.setMedicine(DataValueUtils.getText(directions));
    }
    orderActivity.setDirections(DataValueUtils.getText(directions));
    return orderActivity;
  }

  public static void setTitration(@Nonnull final OrderActivity orderActivity, @Nonnull final ConstantTherapy constantTherapy)
  {
    Preconditions.checkNotNull(orderActivity, "orderActivity is required");
    Preconditions.checkNotNull(constantTherapy, "constantTherapy is required");
    if (constantTherapy.getTitration() != null)
    {
      final String enumString = TitrationType.getFullString(constantTherapy.getTitration());
      orderActivity.getAdditionalInstruction().add(DataValueUtils.getLocalCodedText(enumString, enumString));
    }
  }

  public static TitrationType getTitration(@Nonnull final OrderActivity orderActivity)
  {
    Preconditions.checkNotNull(orderActivity, "orderActivity is required");
    return orderActivity.getAdditionalInstruction().stream()
        .filter(i -> i instanceof DvCodedText)
        .map(i -> TitrationType.getByFullString(((DvCodedText)i).getDefiningCode().getCodeString()))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  public static OrderActivity setMedicationTiming(
      final OrderActivity orderActivity,
      final DosingFrequencyDto dosingFrequency,
      final Integer dosingDaysFrequency,
      final List<String> daysOfWeek,
      final DateTime start,
      final DateTime stop,
      final List<HourMinuteDto> administrationTimes,
      final DateTime administrationDate,
      final Boolean whenNeeded,
      final String startCriterion,
      final Integer maxDailyFrequency)
  {
    final OrderActivity.MedicationTimingCluster medicationTiming = new OrderActivity.MedicationTimingCluster();
    orderActivity.setMedicationTiming(medicationTiming);
    final OrderActivity.MedicationTimingCluster.TimingCluster timing = new OrderActivity.MedicationTimingCluster.TimingCluster();
    medicationTiming.setTiming(timing);

    if (maxDailyFrequency != null)
    {
      final OrderActivity.MaximumDoseCluster maximumDoseCluster = new OrderActivity.MaximumDoseCluster();
      maximumDoseCluster.setAllowedPeriod(DataValueUtils.getDuration(0, 0, 1, 0, 0, 0));
      final MedicationAmountCluster medicationAmountCluster = new MedicationAmountCluster();
      medicationAmountCluster.setAmount(DataValueUtils.getQuantity(maxDailyFrequency, ""));
      maximumDoseCluster.setMedicationAmount(medicationAmountCluster);
      orderActivity.getMaximumDose().add(maximumDoseCluster);
    }
    if (dosingFrequency != null)
    {
      if (dosingFrequency.getType() == DosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        timing.setInterval(DataValueUtils.getDuration(0, 0, 0, dosingFrequency.getValue(), 0, 0));
      }
      else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.DAILY_COUNT)
      {
        final DvCount count = new DvCount();
        count.setMagnitude(dosingFrequency.getValue());
        timing.setDailyCount(count);
      }
      else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.MORNING)
      {
        medicationTiming.setTimingDescription(
            DataValueUtils.getText(DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.MORNING)));
      }
      else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.EVENING)
      {
        medicationTiming.setTimingDescription(
            DataValueUtils.getText(DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.EVENING)));
      }
      else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.NOON)
      {
        medicationTiming.setTimingDescription(
            DataValueUtils.getText(DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.NOON)));
      }
      else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.ONCE_THEN_EX)
      {
        final DvCount numberOfAdministrations = new DvCount();
        numberOfAdministrations.setMagnitude(1L);
        medicationTiming.setNumberOfAdministrations(numberOfAdministrations);
        medicationTiming.setTimingDescription(
            DataValueUtils.getText(DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.ONCE_THEN_EX)));
      }
    }

    if (dosingDaysFrequency != null)
    {
      timing.setInterval(DataValueUtils.getDuration(0, 0, dosingDaysFrequency, 0, 0, 0));
    }

    medicationTiming.setStartDate(DataValueUtils.getDateTime(start));
    if (stop != null)
    {
      medicationTiming.setStopDate(DataValueUtils.getDateTime(stop));
    }

    if (daysOfWeek != null && !daysOfWeek.isEmpty())
    {
      final List<DvCodedText> ehrDaysOfWeek = new ArrayList<>();
      for (final String dayOfWeek : daysOfWeek)
      {
        ehrDaysOfWeek.add(
            DataValueUtils.getCodedText(DayOfWeek.valueOf(dayOfWeek)));
      }
      timing.setDayOfWeek(ehrDaysOfWeek);
    }

    final List<DvTime> dvTimes = new ArrayList<>();
    if (administrationTimes != null)
    {
      for (final HourMinuteDto administrationTime : administrationTimes)
      {
        final DvTime dvTime = new DvTime();
        final String isoTime =
            ISODateTimeFormat.time().print(
                administrationTime.combine(new DateTime(2000, 1, 1, 0, 0)));
        dvTime.setValue(isoTime);
        dvTimes.add(dvTime);
      }
    }

    timing.setTime(dvTimes);

    if (administrationDate != null)
    {
      final DvDate dvDate = new DvDate();
      dvDate.setValue(ISODateTimeFormat.date().print(administrationDate));
      final List<DvDate> dvDates = new ArrayList<>();
      dvDates.add(dvDate);
      timing.setDate(dvDates);
    }

    if (whenNeeded != null)
    {
      medicationTiming.setPRN(DataValueUtils.getBoolean(whenNeeded));
    }
    if (startCriterion != null)
    {
      final MedicationStartCriterionEnum startConditionEnum = MedicationStartCriterionEnum.valueOf(startCriterion);
      if (startConditionEnum != null)
      {
        final String startConditionEnumString = MedicationStartCriterionEnum.getFullString(startConditionEnum);
        medicationTiming.getStartCriterion()
            .add(DataValueUtils.getLocalCodedText(startConditionEnumString, startConditionEnumString));
      }
      else
      {
        throw new IllegalArgumentException("Unknown start condition " + startCriterion);
      }
    }

    return orderActivity;
  }

  public static AdministrationDetailsCluster setRoutes(
      final AdministrationDetailsCluster administration,
      final List<MedicationRouteDto> routeDtos)
  {
    if (routeDtos != null)
    {
      final List<DvCodedText> routes = routeDtos
          .stream()
          .map(routeDto -> DataValueUtils.getLocalCodedText(String.valueOf(routeDto.getId()), routeDto.getName()))
          .collect(Collectors.toList());

      administration.setRoute(routes);
    }
    return administration;
  }

  public static StructuredDoseCluster buildStructuredDose(final Double amount, final String doseUnit)
  {
    return buildNormalStructuredDose(amount, doseUnit, null, null);
  }

  public static StructuredDoseCluster buildStructuredDose(
      final Double amount,
      final String doseUnit,
      final Double denominator,
      final String denominatorUnit,
      final String singleDose,
      final DoseRangeDto doseRange)
  {
    final StructuredDoseCluster structuredDose =
        doseRange == null
        ? buildNormalStructuredDose(amount, doseUnit, denominator, denominatorUnit)
        : buildDoseRangeStructuredDose(doseUnit, denominatorUnit, doseRange);

    if (StringUtils.isNotBlank(singleDose))
    {
      structuredDose.setDescription(DataValueUtils.getText(singleDose));
    }

    return structuredDose;
  }

  private static StructuredDoseCluster buildNormalStructuredDose(
      final Double amount,
      final String doseUnit,
      final Double denominator,
      final String denominatorUnit)
  {
    final StructuredDoseCluster structuredDose = new StructuredDoseCluster();
    if (amount != null)
    {
      if (denominator != null)
      {
        final RatioNumeratorCluster ratioNumerator = new RatioNumeratorCluster();
        ratioNumerator.setAmount(DataValueUtils.getQuantity(amount, ""));
        ratioNumerator.setDoseUnit(DataValueUtils.getLocalCodedText(doseUnit, doseUnit));
        structuredDose.setRatioNumerator(ratioNumerator);

        final RatioDenominatorCluster ratioDenominator = new RatioDenominatorCluster();
        ratioDenominator.setAmount(DataValueUtils.getQuantity(denominator, ""));
        if (denominatorUnit != null)
        {
          ratioDenominator.setDoseUnit(DataValueUtils.getLocalCodedText(denominatorUnit, denominatorUnit));
        }
        structuredDose.setRatioDenominator(ratioDenominator);
      }
      else
      {
        structuredDose.setQuantity(DataValueUtils.getQuantity(amount, ""));
        structuredDose.setDoseUnit(DataValueUtils.getLocalCodedText(doseUnit, doseUnit));
      }
    }
    return structuredDose;
  }

  private static StructuredDoseCluster buildDoseRangeStructuredDose(
      final String numeratorUnit,
      final String denominatorUnit,
      final DoseRangeDto doseRange)
  {
    final boolean hasNumerator = (doseRange.getMinNumerator() != null || doseRange.getMaxNumerator() != null) && numeratorUnit != null;
    final boolean hasDenominator = hasNumerator && (doseRange.getMinDenominator() != null || doseRange.getMaxDenominator() != null);

    final StructuredDoseCluster structuredDose = new StructuredDoseCluster();
    if (hasDenominator)
    {
      final RatioNumeratorCluster ratioNumerator = new RatioNumeratorCluster();
      ratioNumerator.setAmount(DataValueUtils.getInterval("", doseRange.getMinNumerator(), doseRange.getMaxNumerator()));
      ratioNumerator.setDoseUnit(DataValueUtils.getLocalCodedText(numeratorUnit, numeratorUnit));
      structuredDose.setRatioNumerator(ratioNumerator);

      final RatioDenominatorCluster ratioDenominator = new RatioDenominatorCluster();
      ratioDenominator.setAmount(DataValueUtils.getInterval("", doseRange.getMinDenominator(), doseRange.getMaxDenominator()));
      ratioDenominator.setDoseUnit(DataValueUtils.getLocalCodedText(denominatorUnit, denominatorUnit));
      structuredDose.setRatioDenominator(ratioDenominator);
    }
    else if (hasNumerator)
    {
      structuredDose.setQuantity(DataValueUtils.getInterval("", doseRange.getMinNumerator(), doseRange.getMaxNumerator()));
      structuredDose.setDoseUnit(DataValueUtils.getLocalCodedText(numeratorUnit, numeratorUnit));
    }
    return structuredDose;
  }

  public static DoseRangeDto buildDoseRangeFromStructuredDose(final StructuredDoseCluster structuredDose)
  {
    if (!isDoseRangeStructuredDose(structuredDose))
    {
      return null;
    }
    if (structuredDose.getQuantity() != null)
    {
      return buildQuantityDoseRange(structuredDose);
    }
    if (structuredDose.getRatioNumerator() != null)
    {
      return buildRatioQuantityDoseRange(structuredDose);
    }

    return null;
  }

  private static DoseRangeDto buildQuantityDoseRange(final StructuredDoseCluster structuredDose)
  {
    final DoseRangeDto doseRange = new DoseRangeDto();
    final Opt<DvInterval> interval = Opt.resolve(structuredDose::getQuantity).map(q -> ((DvInterval)q));

    interval.map(DvInterval::getLower).map(d -> ((DvQuantity)d).getMagnitude()).ifPresent(doseRange::setMinNumerator);
    interval.map(DvInterval::getUpper).map(d -> ((DvQuantity)d).getMagnitude()).ifPresent(doseRange::setMaxNumerator);

    return doseRange;
  }

  private static DoseRangeDto buildRatioQuantityDoseRange(final StructuredDoseCluster structuredDose)
  {
    final DoseRangeDto doseRange = new DoseRangeDto();
    final Opt<DataValue> numerator = Opt.resolve(structuredDose::getRatioNumerator).map(RatioNumeratorCluster::getAmount);
    final Opt<DataValue> denominator = Opt.resolve(structuredDose::getRatioDenominator).map(RatioDenominatorCluster::getAmount);

    numerator.map(n -> ((DvInterval)n).getLower())
        .map(d -> ((DvQuantity)d).getMagnitude())
        .ifPresent(doseRange::setMinNumerator);

    numerator.map(n -> ((DvInterval)n).getUpper())
        .map(d -> ((DvQuantity)d).getMagnitude())
        .ifPresent(doseRange::setMaxNumerator);

    denominator.map(n -> ((DvInterval)n).getLower())
        .map(d -> ((DvQuantity)d).getMagnitude())
        .ifPresent(doseRange::setMinDenominator);

    denominator.map(n -> ((DvInterval)n).getUpper())
        .map(d -> ((DvQuantity)d).getMagnitude())
        .ifPresent(doseRange::setMaxDenominator);

    return doseRange;
  }

  public static boolean isDoseRangeStructuredDose(final StructuredDoseCluster structuredDose)
  {
    return structuredDose.getQuantity() instanceof DvInterval
        || Opt.resolve(() -> structuredDose.getRatioNumerator().getAmount()).get() instanceof DvInterval;
  }

  public static void setClinicalIndication(final OrderActivity orderActivity, final IndicationDto indicationDto)
  {
    if (indicationDto != null)
    {
      final DvText indication =
          indicationDto.getId() != null ?
          DataValueUtils.getLocalCodedText(indicationDto.getId(), indicationDto.getName()) :
          DataValueUtils.getText(indicationDto.getName());
      orderActivity.getClinicalIndication().add(indication);
    }
  }

  public static TherapyDoseDto buildTherapyDoseDto(
      final StructuredDoseCluster structuredDose,
      final boolean multipleIngredients)
  {
    if (structuredDose != null)
    {
      if (structuredDose.getQuantity() != null)
      {
        final TherapyDoseDto therapyDose = new TherapyDoseDto();
        therapyDose.setNumerator(((DvQuantity)structuredDose.getQuantity()).getMagnitude());
        therapyDose.setNumeratorUnit(structuredDose.getDoseUnit().getDefiningCode().getCodeString());
        therapyDose.setTherapyDoseTypeEnum(
            multipleIngredients ? TherapyDoseTypeEnum.VOLUME_SUM : TherapyDoseTypeEnum.QUANTITY);
        return therapyDose;
      }
      else if (structuredDose.getRatioNumerator() != null)
      {
        final TherapyDoseDto therapyDose = new TherapyDoseDto();
        therapyDose.setNumerator(((DvQuantity)structuredDose.getRatioNumerator().getAmount()).getMagnitude());
        therapyDose.setNumeratorUnit(structuredDose.getRatioNumerator().getDoseUnit().getDefiningCode().getCodeString());
        therapyDose.setDenominator(((DvQuantity)structuredDose.getRatioDenominator().getAmount()).getMagnitude());
        therapyDose.setDenominatorUnit(structuredDose.getRatioDenominator().getDoseUnit().getDefiningCode().getCodeString());
        therapyDose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
        return therapyDose;
      }
    }
    return null;
  }

  public static IngredientsAndFormCluster setMedicationForm(
      final IngredientsAndFormCluster ingredientsAndFormCluster,
      final DoseFormDto doseFormDto)
  {
    if (doseFormDto != null)
    {
      ingredientsAndFormCluster.setForm(DataValueUtils.getLocalCodedText(doseFormDto.getCode(), doseFormDto.getName()));
    }
    return ingredientsAndFormCluster;
  }

  public static void setApplicationPrecondition(final OrderActivity orderActivity, final String applicationPrecondition)
  {
    if (applicationPrecondition != null)
    {
      final MedicationAdditionalInstructionEnum additionalInstructionEnum =
          MedicationAdditionalInstructionEnum.valueOf(applicationPrecondition);
      if (additionalInstructionEnum == null)
      {
        throw new IllegalArgumentException("Not a valid applicationPrecondition");
      }

      final String fullEnumString = MedicationAdditionalInstructionEnum.getFullString(additionalInstructionEnum);
      orderActivity.getAdditionalInstruction().add(DataValueUtils.getLocalCodedText(fullEnumString, fullEnumString));
    }
  }

  public static AdministrationDetailsCluster setSite(
      final AdministrationDetailsCluster administration, final MedicationSiteDto site)
  {
    if (site != null)
    {
      //todo - site can be any codedText
      //administration.setSite(DataValueUtils.getLocalCodedText(site.getCode(), site.getName()));
    }
    return administration;
  }

  public static AdministrationDetailsCluster setStringDoseAdministrationRate(
      final AdministrationDetailsCluster administration,
      final String administrationRate)
  {
    final InfusionAdministrationDetailsCluster infusionDetails = new InfusionAdministrationDetailsCluster();
    administration.getInfusionAdministrationDetails().add(infusionDetails);
    infusionDetails.setDoseAdministrationRate(DataValueUtils.getText(administrationRate));
    return administration;
  }

  public static AdministrationDetailsCluster addInfusionAdministrationDetails(
      final AdministrationDetailsCluster administration,
      final boolean baselineInfusion,
      final String administrationRateString,
      final Double rate,
      final String unit,
      final Double formula,
      final String formulaUnit)
  {
    final InfusionAdministrationDetailsCluster infusionDetails = getInfusionDetails(
        baselineInfusion, administrationRateString, rate, unit, formula, formulaUnit);
    administration.getInfusionAdministrationDetails().add(infusionDetails);
    return administration;
  }

  public static InfusionAdministrationDetailsCluster getInfusionDetails(
      final boolean baselineInfusion,
      final String administrationRateString,
      final Double rate,
      final String unit,
      final Double formula,
      final String formulaUnit)
  {
    final InfusionAdministrationDetailsCluster infusionDetails = new InfusionAdministrationDetailsCluster();
    if (baselineInfusion)
    {
      infusionDetails.setPurposeEnum(InfusionAdministrationDetailsPurpose.BASELINE_ELECTROLYTE_INFUSION);
    }

    if (administrationRateString != null)
    {
      infusionDetails.setDoseAdministrationRate(DataValueUtils.getText(administrationRateString));
    }
    else
    {
      if (rate != null)
      {
        infusionDetails.setDoseAdministrationRate(DataValueUtils.getQuantity(rate, unit));
      }
      if (formula != null)
      {
        infusionDetails.setDoseAdministrationFormula(DataValueUtils.getQuantity(formula, formulaUnit));
      }
    }
    return infusionDetails;
  }

  public static AdministrationDetailsCluster setDuration(
      final AdministrationDetailsCluster administration,
      final int durationInMinutes)
  {
    administration.setDoseDuration(DataValueUtils.getDuration(0, 0, 0, 0, durationInMinutes, 0));
    return administration;
  }

  public static IngredientCluster addIngredientTo(final IngredientsAndFormCluster ingredientsAndForm)
  {
    final IngredientCluster ingredient = new IngredientCluster();
    ingredientsAndForm.getIngredient().add(ingredient);
    return ingredient;
  }

  public static IngredientCluster setName(
      final IngredientCluster activeIngredient, final String name, final Long medicationId)
  {
    if (medicationId != null)
    {
      activeIngredient.setName(DataValueUtils.getLocalCodedText(String.valueOf(medicationId), name));
    }
    else
    {
      activeIngredient.setName(DataValueUtils.getText(name));
    }
    return activeIngredient;
  }

  public static IngredientCluster setIngredientForm(final IngredientCluster activeIngredient, final DoseFormDto form)
  {
    if (form != null)
    {
      activeIngredient.setForm(DataValueUtils.getLocalCodedText(form.getCode(), form.getName()));
    }
    return activeIngredient;
  }

  public static IngredientCluster setQuantity(
      final IngredientCluster ingredient,
      final Double quantity,
      final String quantityUnit,
      final Double quantityDenominator,
      final String quantityDenominatorUnit)
  {
    final IngredientQuantityCluster ingredientQuantity = new IngredientQuantityCluster();
    ingredient.setIngredientQuantity(ingredientQuantity);
    if (quantity != null && quantityDenominator != null)
    {
      final IngredientQuantityCluster.RatioNumeratorCluster ratioNumerator =
          new IngredientQuantityCluster.RatioNumeratorCluster();
      ingredientQuantity.setRatioNumerator(ratioNumerator);
      ratioNumerator.setAmount(DataValueUtils.getQuantity(quantity, ""));
      ratioNumerator.setDoseUnit(DataValueUtils.getLocalCodedText(quantityUnit, quantityUnit));

      final IngredientQuantityCluster.RatioDenominatorCluster ratioDenominator =
          new IngredientQuantityCluster.RatioDenominatorCluster();
      ingredientQuantity.setRatioDenominator(ratioDenominator);
      ratioDenominator.setAmount(DataValueUtils.getQuantity(quantityDenominator, ""));
      ratioDenominator.setDoseUnit(DataValueUtils.getLocalCodedText(quantityDenominatorUnit, quantityDenominatorUnit));
    }
    else if (quantity != null)
    {
      ingredientQuantity.setQuantity(DataValueUtils.getQuantity(quantity, ""));
      ingredientQuantity.setDoseUnit(DataValueUtils.getLocalCodedText(quantityUnit, quantityUnit));
    }
    return ingredient;
  }

  public static List<Link> getLinksOfType(
      @Nonnull final MedicationInstructionInstruction instruction,
      @Nonnull final EhrLinkType linkType)
  {
    Preconditions.checkNotNull(instruction, "instruction");
    Preconditions.checkNotNull(linkType, "linkType");

    return instruction.getLinks()
        .stream()
        .filter(instructionLink -> instructionLink.getType().getValue().equals(linkType.getName()))
        .collect(Collectors.toList());
  }

  public static boolean hasLinksOfType(
      @Nonnull final MedicationInstructionInstruction instruction,
      @Nonnull final EhrLinkType linkType)
  {
    Preconditions.checkNotNull(instruction, "instruction");
    Preconditions.checkNotNull(linkType, "linkType");

    return !getLinksOfType(instruction, linkType).isEmpty();
  }

  public static boolean removeLinksOfType(final MedicationInstructionInstruction instruction, final EhrLinkType linkType)
  {
    boolean hadLinks = false;
    if (instruction != null )
    {
      final List<Link> linksOfType = getLinksOfType(instruction, linkType);
      hadLinks = !linksOfType.isEmpty();
      instruction.getLinks().removeAll(linksOfType);
    }
    return hadLinks;
  }

  public static MedicationOrderComposition addInstructionTo(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);
    return composition;
  }

  public static void addMedicationActionTo(
      final MedicationOrderComposition composition,
      final MedicationActionEnum actionEnum,
      final NamedExternalDto user,
      final TherapyChangeReasonDto changeReasonDto,
      final DateTime when)
  {
    final MedicationActionAction action = buildMedicationAction(composition, actionEnum, when);
    if (changeReasonDto != null)
    {
      setTherapyChangeReasonToAction(changeReasonDto, action);
    }
    visitEhrBean(action, user, when);
    composition.getMedicationDetail().getMedicationAction().add(action);
  }

  public static void addMedicationActionTo(
      final MedicationOrderComposition composition,
      final MedicationActionEnum actionEnum,
      final NamedExternalDto user,
      final DateTime when)
  {
    addMedicationActionTo(composition, actionEnum, user, null, when);
  }

  public static void visitEhrBean(final Locatable ehrLocatable, final NamedExternalDto prescriber, final DateTime when)
  {
    final TdoPopulatingVisitor.DataContext dataContext = TdoPopulatingVisitor.getSloveneContext(when);
    if (prescriber != null)
    {
      setContextParticipation(dataContext, prescriber, ParticipationTypeEnum.PRESCRIBER);
    }
    new TdoPopulatingVisitor().visitBean(ehrLocatable, dataContext);
  }

  public static void addMedicationActionTo(
      final PharmacyReviewReportComposition composition,
      final MedicationActionEnum action,
      final DateTime when)
  {
    final MedicationActionAction completeAction = buildMedicationAction(composition, action, when);
    composition.getMiscellaneous().getMedicationAction().add(completeAction);
  }

  public static MedicationActionAction buildMedicationAction(
      final Composition composition,
      final MedicationActionEnum medicationAction,
      final DateTime when)
  {
    final MedicationActionAction action = new MedicationActionAction();
    action.setTime(DataValueUtils.getDateTime(when));

    final InstructionDetails instructionDetails = new InstructionDetails();
    final LocatableRef instructionId = createInstructionLocatableRef(composition);
    instructionDetails.setInstructionId(instructionId);
    instructionDetails.setActivityId("activities[at0001] ");
    action.setInstructionDetails(instructionDetails);

    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(medicationAction.getCareflowStep());
    ismTransition.setCurrentState(medicationAction.getCurrentState());
    action.setIsmTransition(ismTransition);

    return action;
  }

  public static LocatableRef createInstructionLocatableRef(final Composition composition)
  {
    final LocatableRef locatableRef = new LocatableRef();
    locatableRef.setType("INSTRUCTION");
    locatableRef.setNamespace("local");
    final ObjectVersionId objectVersionId = new ObjectVersionId();
    objectVersionId.setValue(
        composition.getUid() != null ?
        TherapyIdUtils.getCompositionUidWithoutVersion(composition.getUid().getValue()) :
        "/");
    locatableRef.setId(objectVersionId);

    return locatableRef;
  }

  public static boolean isRoundsTimeForSession(final DateTime when, final RoundsIntervalDto roundsInterval)
  {
    final int minuteOfDay = when.getMinuteOfDay();

    final int roundsStart = roundsInterval.getStartHour() * 60 + roundsInterval.getStartMinute();
    final int roundsEnd = roundsInterval.getEndHour() * 60 + roundsInterval.getEndMinute();

    return minuteOfDay > roundsStart && minuteOfDay < roundsEnd;
  }

  public static MedicationInstructionInstruction getMedicationInstructionByEhrName(
      final MedicationOrderComposition composition,
      final String ehrOrderName)
  {
    for (final MedicationInstructionInstruction instruction : composition.getMedicationDetail().getMedicationInstruction())
    {
      if (instruction.getName().getValue().equals(ehrOrderName))
      {
        return instruction;
      }
    }
    throw new IllegalArgumentException("No order with name: " + ehrOrderName + " exists in composition!");
  }

  public static TherapyDoseDto buildTherapyDoseDtoForRate(final InfusionAdministrationDetailsCluster infusionDetails)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    if (infusionDetails.getDoseAdministrationRate() instanceof DvQuantity)
    {
      final DvQuantity administrationRateQuantity = (DvQuantity)infusionDetails.getDoseAdministrationRate();
      therapyDoseDto.setNumerator(administrationRateQuantity.getMagnitude());
      therapyDoseDto.setNumeratorUnit(administrationRateQuantity.getUnits());
    }
    if (infusionDetails.getDoseAdministrationFormula() instanceof DvQuantity)
    {
      final DvQuantity administrationRateFormula = (DvQuantity)infusionDetails.getDoseAdministrationFormula();
      therapyDoseDto.setDenominator(administrationRateFormula.getMagnitude());
      therapyDoseDto.setDenominatorUnit(administrationRateFormula.getUnits());
    }
    if (therapyDoseDto.getNumerator() != null || therapyDoseDto.getDenominator() != null)
    {
      therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
      return therapyDoseDto;
    }
    return null;
  }

  public static Pair<MedicationOrderComposition, MedicationInstructionInstruction> findMedicationInstructionPairByTherapyId(
      final String searchedTherapyId,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionPairs)
  {
    if (searchedTherapyId != null)
    {
      for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : medicationInstructionPairs)
      {
        final String therapyId = TherapyIdUtils.createTherapyId(instructionPair.getFirst(), instructionPair.getSecond());
        if (searchedTherapyId.equals(therapyId))
        {
          return instructionPair;
        }
      }
    }
    return null;
  }

  public static OrderActivity getRepresentingOrderActivity(final MedicationInstructionInstruction instruction)
  {
    for (final OrderActivity orderActivity : instruction.getOrder())
    {
      if (orderActivity.getStructuredDose() != null)
      {
        return orderActivity;
      }
    }
    return instruction.getOrder().get(0);
  }

  public static void fillActionInstructionId(
      final LocatableRef actionInstructionId,
      final Composition composition,
      final MedicationInstructionInstruction instruction,
      final String compositionUid)
  {
    final RmPath rmPath = TdoPathable.pathOfItem(composition, instruction);
    actionInstructionId.setPath(rmPath.getCanonicalString());
    final ObjectVersionId objectVersionId = new ObjectVersionId();
    objectVersionId.setValue(compositionUid);
    actionInstructionId.setId(objectVersionId);
  }

  public static void addContext(
      final IspekComposition composition, @Nullable final NamedExternalDto careProvider, final DateTime when)
  {
    final CompositionEventContext compositionEventContext =
        IspekTdoDataSupport.getEventContext(CompositionEventContext.class, when);
    if (careProvider != null)
    {
      final CompositionEventContext.ContextDetailCluster contextDetailCluster =
          new CompositionEventContext.ContextDetailCluster();
      contextDetailCluster.setDepartmentalPeriodOfCareIdentifier(DataValueUtils.getLocalCodedText(
          careProvider.getId(),
          careProvider.getName()));
      compositionEventContext.getContextDetail().add(contextDetailCluster);
    }
    composition.setCompositionEventContext(compositionEventContext);
  }

  public static void addContext(
      final IspekComposition composition,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final DateTime when)
  {
    final CompositionEventContext compositionEventContext =
        IspekTdoDataSupport.getEventContext(CompositionEventContext.class, when);

    if (centralCaseId != null)
    {
      final List<CompositionEventContext.ContextDetailCluster> contextDetailClusterList = new ArrayList<>();
      final CompositionEventContext.ContextDetailCluster contextDetailCluster = new CompositionEventContext.ContextDetailCluster();
      contextDetailCluster.setPeriodOfCareIdentifier(DataValueUtils.getText(centralCaseId));

      if (careProviderId != null)
      {
        contextDetailCluster.setDepartmentalPeriodOfCareIdentifier(DataValueUtils.getText(careProviderId));
      }

      contextDetailClusterList.add(contextDetailCluster);
      compositionEventContext.setContextDetail(contextDetailClusterList);
    }
    else if (composition.getCompositionEventContext() != null)
    {
      compositionEventContext.setContextDetail(composition.getCompositionEventContext().getContextDetail());
    }
    composition.setCompositionEventContext(compositionEventContext);
  }

  public static void setContextParticipation(
      final TdoPopulatingVisitor.DataContext dataContext,
      final NamedExternalDto identity,
      final ParticipationTypeEnum participationTypeEnum)
  {
    if (identity != null)
    {
      final Participation participation = new Participation();
      final PartyIdentified performerProxy = new PartyIdentified();
      final DvIdentifier partyIdentifier = new DvIdentifier();
      partyIdentifier.setId(identity.getId());
      performerProxy.getIdentifiers().add(partyIdentifier);
      performerProxy.setName(identity.getName());
      participation.setPerformer(performerProxy);
      participation.setFunction(
          DataValueUtils.getText(
              participationTypeEnum == ParticipationTypeEnum.PRESCRIBER
              ? ParticipationTypeEnum.PRESCRIBER.getCode()
              : ParticipationTypeEnum.WITNESS.getCode()));
      participation.setMode(FdoConstants.PARTICIPATION_UNSPECIFIED_MODE);

      dataContext.withEntryParticipation(participation);
    }
  }

  public static void visitComposition(final Composition composition, final DateTime when)
  {
    final PartyIdentified composer = RequestContextHolder.getContext().getUserMetadata()
        .map(meta -> IspekTdoDataSupport.getPartyIdentified(meta.getFullName(), meta.getId()))
        .get();
    visitComposition(composition, composer, when);
  }

  public static void visitComposition(final Composition composition, final NamedExternalDto composer, final DateTime when)
  {
    visitComposition(composition, IspekTdoDataSupport.getPartyIdentified(composer.getName(), composer.getId()), when);
  }

  public static void visitComposition(final Composition composition, final PartyProxy composer, final DateTime when)
  {
    final TdoPopulatingVisitor.DataContext dataContext =
        TdoPopulatingVisitor.getSloveneContext(when).withCompositionComposer(composer);

    new TdoPopulatingVisitor().visitBean(composition, dataContext);
  }

  public static String getPreviousLinkName(final String linkName)
  {
    final String prefix = linkName.substring(0, 1);
    final Integer linkNumber = Integer.valueOf(linkName.substring(1, linkName.length()));
    return prefix + (linkNumber - 1);
  }

  public static String getTargetCompositionIdFromLink(final Link link)
  {
    final DvEhrUri target = link.getTarget();
    final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(target.getValue());
    return ehrUri.getCompositionId();
  }

  public static TherapyChangeReasonDto getTherapyChangeReasonDtoFromAction(final MedicationActionAction action)
  {
    //TODO nejc - read form different reason list in EHR
    CodedNameDto changeReason = null;
    for (final DvText reason : action.getReason())
    {
      if (reason instanceof DvCodedText)
      {
        final DvCodedText codedReason = (DvCodedText)reason;
        changeReason = new CodedNameDto(codedReason.getDefiningCode().getCodeString(), codedReason.getValue());
      }
    }
    if (changeReason != null)
    {
      final TherapyChangeReasonDto therapyChangeReasonDto = new TherapyChangeReasonDto();
      therapyChangeReasonDto.setComment(action.getComment() != null ? action.getComment().getValue() : null);

      therapyChangeReasonDto.setChangeReason(changeReason);
      return therapyChangeReasonDto;
    }
    return null;
  }

  public static void setTherapyChangeReasonToAction(
      final TherapyChangeReasonDto changeReasonDto,
      final MedicationActionAction action)
  {
    //TODO nejc - read form different reason list in EHR
    if (changeReasonDto != null)
    {
      final List<DvText> reasonList = new ArrayList<>();
      reasonList.add(
          DataValueUtils.getLocalCodedText(
              changeReasonDto.getChangeReason().getCode(),
              changeReasonDto.getChangeReason().getName()));

      action.setReason(reasonList);

      if (changeReasonDto.getComment() != null)
      {
        action.setComment(DataValueUtils.getText(changeReasonDto.getComment()));
      }
    }
  }

  public static MedicationActionAction getLatestAction(final List<MedicationActionAction> actionsList)
  {
    DateTime latestDate = null;
    MedicationActionAction latestAction = null;

    for (final MedicationActionAction action : actionsList)
    {
      final DateTime actionTime = DataValueUtils.getDateTime(action.getTime());
      if (latestDate == null || actionTime.isAfter(latestDate))
      {
        latestDate = actionTime;
        latestAction = action;
      }
    }
    return latestAction;
  }

  public static MedicationActionAction getLatestModifyAction(final List<MedicationActionAction> actionsList)
  {
    DateTime latestDate = null;
    MedicationActionAction latestAction = null;

    for (final MedicationActionAction action : actionsList)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.MODIFY_EXISTING || actionEnum == MedicationActionEnum.COMPLETE)
      {
        final DateTime actionTime = DataValueUtils.getDateTime(action.getTime());
        if (latestDate == null || actionTime.isAfter(latestDate))
        {
          latestDate = actionTime;
          latestAction = action;
        }
      }
    }
    return latestAction;
  }

  public static MedicationActionAction getLatestActionWithChangeReason(
      final List<MedicationActionAction> actionsList,
      final boolean onlyForAbortedOrSuspended)
  {
    DateTime latestDate = null;
    MedicationActionAction latestAction = null;

    for (final MedicationActionAction action : actionsList)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      final boolean isModifiedActionType = actionEnum == MedicationActionEnum.MODIFY_EXISTING || actionEnum == MedicationActionEnum.COMPLETE;
      if (actionEnum == MedicationActionEnum.ABORT || actionEnum == MedicationActionEnum.CANCEL)
      {
        return action;
      }
      else if (actionEnum == MedicationActionEnum.SUSPEND || actionEnum == MedicationActionEnum.REISSUE || (!onlyForAbortedOrSuspended && isModifiedActionType))
      {
        final DateTime actionTime = DataValueUtils.getDateTime(action.getTime());
        if (latestDate == null || actionTime.isAfter(latestDate))
        {
          latestDate = actionTime;
          latestAction = action;
        }
      }
    }
    return latestAction;
  }

  public static DateTime getLastModifiedTimestamp(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    final List<MedicationActionAction> actions = getInstructionActions(composition, instruction);
    return actions.stream()
        .filter(action -> MedicationActionEnum.getActionEnum(action) == MedicationActionEnum.MODIFY_EXISTING)
        .map(action -> DataValueUtils.getDateTime(action.getTime()))
        .max(Comparator.naturalOrder())
        .orElse(null);
  }

  public static HourMinuteDto getHourMinute(final DvTime time)
  {
    if (time == null)
    {
      return null;
    }
    final LocalTime localTime = ConversionUtils.toLocalTime(time);
    return new HourMinuteDto(localTime.getHourOfDay(), localTime.getMinuteOfHour());
  }

  public static Interval getInstructionInterval(final OrderActivity.MedicationTimingCluster medicationTiming)
  {
    final DateTime start = DataValueUtils.getDateTime(medicationTiming.getStartDate());
    final DateTime stop =
        medicationTiming.getStopDate() != null ?
        DataValueUtils.getDateTime(medicationTiming.getStopDate()) :
        null;

    return stop != null ? new Interval(start, stop) : Intervals.infiniteFrom(start);
  }

  public static List<MedicationActionAction> getInstructionActions(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    return getInstructionActions(composition, instruction, null);
  }

  public static List<MedicationActionAction> getInstructionActions(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      @Nullable final Interval interval)
  {
    final String instructionPath = TdoPathable.pathOfItem(composition, instruction).getCanonicalString();
    final List<MedicationActionAction> actionsList = new ArrayList<>();
    for (final MedicationActionAction action : composition.getMedicationDetail().getMedicationAction())
    {
      if (action.getInstructionDetails().getInstructionId().getPath().equals(instructionPath))
      {
        if (interval == null || DataValueUtils.getDateTime(action.getTime()).isBefore(interval.getEnd()))
        {
          actionsList.add(action);
        }
      }
    }

    Collections.sort(
        actionsList,
        (action1, action2) -> DataValueUtils.getDateTime(action1.getTime()).compareTo(DataValueUtils.getDateTime(action2.getTime()))
    );
    return actionsList;
  }

  public static String getOriginalTherapyId(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    final List<Link> originLinks = getLinksOfType(instruction, EhrLinkType.ORIGIN);
    return originLinks.isEmpty()
           ? TherapyIdUtils.createTherapyId(composition, instruction)
           : TherapyIdUtils.getTherapyIdFromLink(originLinks.get(0));
  }
}
