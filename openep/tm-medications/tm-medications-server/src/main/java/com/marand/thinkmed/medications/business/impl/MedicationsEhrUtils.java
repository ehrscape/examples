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
import java.util.Collections;
import java.util.List;

import com.marand.ispek.ehr.common.IspekEhrUtils;
import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.openehr.util.InstructionTranslator;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.StructuredDoseCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrLinkType;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSiteDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvDate;
import org.openehr.jaxb.rm.DvParsable;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvTime;
import org.openehr.jaxb.rm.InstructionDetails;
import org.openehr.jaxb.rm.IsmTransition;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster.IngredientQuantityCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.InfusionAdministrationDetailsPurpose;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MaximumDoseCluster.MedicationAmountCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster.DayOfWeek;
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
    final MedicationOrderComposition.MedicationDetailSection compositionDetail = new MedicationOrderComposition.MedicationDetailSection();
    final List<MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction> instructionsList = new ArrayList<>();
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

  public static boolean isSimpleInstruction(final MedicationInstructionInstruction instruction)
  {
    return instruction != null && isSimpleTherapy(instruction.getOrder().get(0));
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
    return instruction != null && instruction.getOrder().get(0).getAdministrationDetails() != null &&
        MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION
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

  public static OrderActivity setMedicationTiming(
      final OrderActivity orderActivity,
      final DosingFrequencyDto dosingFrequency,
      final Integer dosingDaysFrequency,
      final List<String> daysOfWeek,
      final DateTime start,
      final DateTime stop,
      final HourMinuteDto administrationTime,
      final DateTime administrationDate,
      final Boolean whenNeeded,
      final List<String> startCriterions,
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
      if (dosingFrequency != null && dosingFrequency.getType() == DosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        throw new IllegalArgumentException("Not a valid dosingFrequency");
      }
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
    if (administrationTime != null)
    {
      final DvTime dvTime = new DvTime();
      final String isoTime =
          ISODateTimeFormat.time().print(
              new DateTime(2000, 1, 1, administrationTime.getHour(), administrationTime.getMinute()));
      dvTime.setValue(isoTime);
      dvTimes.add(dvTime);
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
    for (final String startCriterion : startCriterions)
    {
      if (startCriterion != null)
      {
        final MedicationStartCriterionEnum startConditionEnum = MedicationStartCriterionEnum.valueOf(startCriterion);
        if (startCriterion != null)
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
    }

    return orderActivity;
  }

  public static AdministrationDetailsCluster setRoute(
      final AdministrationDetailsCluster administration, final MedicationRouteDto routeDto)
  {
    if (routeDto != null)
    {
      administration.setRoute(
          Collections.singletonList(DataValueUtils.getLocalCodedText(routeDto.getCode(), routeDto.getName())));
    }
    return administration;
  }

  public static StructuredDoseCluster buildStructuredDose(
      final Double amount,
      final String doseUnit,
      final Double denominator,
      final String denominatorUnit,
      final String singleDose)
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
        ratioDenominator.setDoseUnit(DataValueUtils.getLocalCodedText(denominatorUnit, denominatorUnit));
        structuredDose.setRatioDenominator(ratioDenominator);
      }
      else
      {
        structuredDose.setQuantity(DataValueUtils.getQuantity(amount, ""));
        structuredDose.setDoseUnit(DataValueUtils.getLocalCodedText(doseUnit, doseUnit));
      }
    }

    if (singleDose != null)
    {
      structuredDose.setDescription(DataValueUtils.getText(singleDose));
    }
    return structuredDose;
  }

  public static TherapyDoseDto buildTherapyDoseDto(final StructuredDoseCluster structuredDose)
  {
    if (structuredDose != null)
    {
      if (structuredDose.getQuantity() != null)
      {
        final TherapyDoseDto therapyDose = new TherapyDoseDto();
        therapyDose.setNumerator(structuredDose.getQuantity().getMagnitude());
        therapyDose.setNumeratorUnit(structuredDose.getDoseUnit().getDefiningCode().getCodeString());
        therapyDose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.VOLUME_SUM);
        return therapyDose;
      }
      else if (structuredDose.getRatioNumerator() != null)
      {
        final TherapyDoseDto therapyDose = new TherapyDoseDto();
        therapyDose.setNumerator(structuredDose.getRatioNumerator().getAmount().getMagnitude());
        therapyDose.setNumeratorUnit(structuredDose.getRatioNumerator().getDoseUnit().getDefiningCode().getCodeString());
        therapyDose.setDenominator(structuredDose.getRatioDenominator().getAmount().getMagnitude());
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

  public static IngredientCluster setQuantityWithVolume(
      final IngredientCluster ingredient,
      final Double quantity,
      final String quantityUnit,
      final Double volume,
      final String volumeUnit)
  {
    final IngredientQuantityCluster ingredientQuantity = new IngredientQuantityCluster();
    ingredient.setIngredientQuantity(ingredientQuantity);
    if (quantity != null && volume != null)
    {
      final IngredientQuantityCluster.RatioNumeratorCluster ratioNumerator =
          new IngredientQuantityCluster.RatioNumeratorCluster();
      ingredientQuantity.setRatioNumerator(ratioNumerator);
      ratioNumerator.setAmount(DataValueUtils.getQuantity(quantity, ""));
      ratioNumerator.setDoseUnit(DataValueUtils.getLocalCodedText(quantityUnit, quantityUnit));

      final IngredientQuantityCluster.RatioDenominatorCluster ratioDenominator =
          new IngredientQuantityCluster.RatioDenominatorCluster();
      ingredientQuantity.setRatioDenominator(ratioDenominator);
      ratioDenominator.setAmount(DataValueUtils.getQuantity(volume, ""));
      ratioDenominator.setDoseUnit(DataValueUtils.getLocalCodedText(volumeUnit, volumeUnit));
    }
    else if (quantity != null)
    {
      ingredientQuantity.setQuantity(DataValueUtils.getQuantity(quantity, ""));
      ingredientQuantity.setDoseUnit(DataValueUtils.getLocalCodedText(quantityUnit, quantityUnit));
    }
    else if (volume != null)
    {
      ingredientQuantity.setQuantity(DataValueUtils.getQuantity(volume, ""));
      ingredientQuantity.setDoseUnit(DataValueUtils.getLocalCodedText(volumeUnit, volumeUnit));
    }
    return ingredient;
  }

  public static List<Link> getLinksOfType(final MedicationInstructionInstruction instruction, final OpenEhrLinkType linkType)
  {
    final List<Link> links = new ArrayList<>();
    for (final Link instructionLink : instruction.getLinks())
    {
      if (instructionLink.getType().getValue().equals(linkType.getName()))
      {
        links.add(instructionLink);
      }
    }
    return links;
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
      final MedicationInstructionInstruction instruction,
      final MedicationActionEnum action,
      final DateTime when)
  {
    final MedicationActionAction completeAction = buildMedicationAction(composition, instruction, action, when);
    composition.getMedicationDetail().getMedicationAction().add(completeAction);
  }

  private static MedicationActionAction buildMedicationAction(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final MedicationActionEnum medicationAction,
      final DateTime when)
  {
    final MedicationActionAction action = new MedicationActionAction();
    action.setTime(DataValueUtils.getDateTime(when));

    final InstructionDetails instructionDetails = new InstructionDetails();
    final LocatableRef instructionId = createInstructionLocatableRef(composition, instruction);
    instructionDetails.setInstructionId(instructionId);
    instructionDetails.setActivityId("activities[at0001] ");
    action.setInstructionDetails(instructionDetails);

    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(medicationAction.getCareflowStep());
    ismTransition.setCurrentState(medicationAction.getCurrentState());
    action.setIsmTransition(ismTransition);

    return action;
  }

  public static LocatableRef createInstructionLocatableRef(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    final LocatableRef locatableRef = new LocatableRef();
    locatableRef.setType("INSTRUCTION");
    locatableRef.setNamespace("local");
    locatableRef.setPath(String.valueOf(composition.getMedicationDetail().getMedicationInstruction().indexOf(instruction)));

    final ObjectVersionId objectVersionId = new ObjectVersionId();

    objectVersionId.setValue(
        composition.getUid() != null ?
        InstructionTranslator.getCompositionUidWithoutVersion(composition.getUid().getValue()) :
        "");
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

  public static TherapyDoseDto buildTherapyDoseDtoForRate(
      final InfusionAdministrationDetailsCluster infusionDetails)
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
        final String therapyId = InstructionTranslator.translate(instructionPair.getSecond(), instructionPair.getFirst());
        if (searchedTherapyId.equals(therapyId))
        {
          return instructionPair;
        }
      }
    }
    return null;
  }
}
