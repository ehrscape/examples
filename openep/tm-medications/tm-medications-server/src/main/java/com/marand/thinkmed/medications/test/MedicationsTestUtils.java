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

package com.marand.thinkmed.medications.test;

import java.util.Collections;

import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster.DayOfWeek;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.StructuredDoseCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.util.ConversionUtils;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvIdentifier;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.DvTime;
import org.openehr.jaxb.rm.InstructionDetails;
import org.openehr.jaxb.rm.IsmTransition;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.openehr.jaxb.rm.Participation;
import org.openehr.jaxb.rm.PartyIdentified;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster.IngredientQuantityCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection;

/**
 * @author Mitja Lapajne
 */
public class MedicationsTestUtils
{
  private MedicationsTestUtils()
  {
  }

  public static MedicationActionAction buildMedicationAction(
      final MedicationActionEnum medicationAction,
      final DateTime when)
  {
    final MedicationActionAction action = new MedicationActionAction();
    action.setTime(DataValueUtils.getDateTime(when));

    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(medicationAction.getCareflowStep());
    ismTransition.setCurrentState(medicationAction.getCurrentState());
    action.setIsmTransition(ismTransition);

    return action;
  }

  public static MedicationOrderComposition buildTestMedicationOrderComposition(
      final String uidValue, final DateTime composeTimestamp, final String composer)
  {
    final MedicationOrderComposition composition = new MedicationOrderComposition();
    final MedicationDetailSection medicationDetail = new MedicationDetailSection();
    composition.setMedicationDetail(medicationDetail);
    final ObjectVersionId uid = new ObjectVersionId();
    uid.setValue(uidValue);
    composition.setUid(uid);
    if (composeTimestamp != null)
    {
      final CompositionEventContext compositionEventContext = new CompositionEventContext();
      compositionEventContext.setStartTime(DataValueUtils.getDateTime(composeTimestamp));
      composition.setCompositionEventContext(compositionEventContext);
    }
    if (composer != null)
    {
      final PartyIdentified composerPartyIdentified = new PartyIdentified();
      composerPartyIdentified.setName(composer);
      composition.setComposer(composerPartyIdentified);
    }
    return composition;
  }

  public static MedicationInstructionInstruction buildTestMedicationInstruction(final String id)
  {
    return buildTestMedicationInstruction(id, null);
  }

  public static MedicationInstructionInstruction buildTestMedicationInstruction(final String id, final String prescriber)
  {
    final MedicationInstructionInstruction instruction = new MedicationInstructionInstruction();
    instruction.setName(DataValueUtils.getText(id));
    if (prescriber != null)
    {
      final Participation participation = new Participation();
      final PartyIdentified performerProxy = new PartyIdentified();
      final DvIdentifier partyIdentifier = new DvIdentifier();
      performerProxy.getIdentifiers().add(partyIdentifier);
      performerProxy.setName(prescriber);
      participation.setPerformer(performerProxy);
      participation.setFunction(DataValueUtils.getText("prescriber"));
      instruction.getOtherParticipations().add(participation);
    }
    return instruction;
  }

  public static IngredientCluster buildTestActiveIngredient(
      final String medicationName,
      final String medicationCode,
      final Double quantity,
      final String quantityUnit,
      final Double quantityNumerator,
      final String quantityNumeratorUnit,
      final Double quantityDenominator,
      final String quantityDenominatorUnit)
  {
    final IngredientCluster ingredient = new IngredientCluster();

    ingredient.setName(DataValueUtils.getLocalCodedText(medicationCode, medicationName));

    final IngredientQuantityCluster medicationQuantity = new IngredientQuantityCluster();
    ingredient.setIngredientQuantity(medicationQuantity);

    if (quantity != null)
    {
      medicationQuantity.setQuantity(DataValueUtils.getQuantity(quantity, ""));
      medicationQuantity.setDoseUnit(DataValueUtils.getLocalCodedText(quantityUnit, quantityUnit));
    }
    else if (quantityNumerator != null)
    {
      final IngredientQuantityCluster.RatioNumeratorCluster ratioNumerator =
          new IngredientQuantityCluster.RatioNumeratorCluster();
      ratioNumerator.setAmount(DataValueUtils.getQuantity(quantityNumerator, ""));
      ratioNumerator.setDoseUnit(DataValueUtils.getLocalCodedText(quantityNumeratorUnit, quantityNumeratorUnit));
      medicationQuantity.setRatioNumerator(ratioNumerator);

      final IngredientQuantityCluster.RatioDenominatorCluster ratioDenominator =
          new IngredientQuantityCluster.RatioDenominatorCluster();
      ratioDenominator.setAmount(DataValueUtils.getQuantity(quantityDenominator, ""));
      ratioDenominator.setDoseUnit(DataValueUtils.getLocalCodedText(quantityDenominatorUnit, quantityDenominatorUnit));
      medicationQuantity.setRatioDenominator(ratioDenominator);
    }

    return ingredient;
  }

  public static MedicationInstructionInstruction.OrderActivity buildTestOrderActivity(
      final String medicationName,
      final Long medicationId,
      final Double quantity,
      final String quantityUnit,
      final Double quantityNumerator,
      final String quantityNumeratorUnit,
      final Double quantityDenominator,
      final String quantityDenominatorUnit,
      final Long routeId,
      final String routeName,
      final String doseFormCode,
      final String doseFormName)
  {
    final MedicationInstructionInstruction.OrderActivity orderActivity = new MedicationInstructionInstruction.OrderActivity();
    if (medicationName != null)
    {
      orderActivity.setMedicine(DataValueUtils.getLocalCodedText(String.valueOf(medicationId), medicationName));
    }

    final AdministrationDetailsCluster medicationAdministration = new AdministrationDetailsCluster();
    medicationAdministration.setRoute(Collections.singletonList(DataValueUtils.getLocalCodedText(String.valueOf(routeId), routeName)));
    orderActivity.setAdministrationDetails(medicationAdministration);
    final StructuredDoseCluster doseAmount = new StructuredDoseCluster();
    orderActivity.setStructuredDose(doseAmount);

    if (quantity != null)
    {
      doseAmount.setQuantity(DataValueUtils.getQuantity(quantity, ""));
      doseAmount.setDoseUnit(DataValueUtils.getLocalCodedText(quantityUnit, quantityUnit));
    }
    else if (quantityNumerator != null)
    {
      final StructuredDoseCluster.RatioNumeratorCluster ratioNumerator =
          new StructuredDoseCluster.RatioNumeratorCluster();
      ratioNumerator.setAmount(DataValueUtils.getQuantity(quantityNumerator, ""));
      ratioNumerator.setDoseUnit(DataValueUtils.getLocalCodedText(quantityNumeratorUnit, quantityNumeratorUnit));
      doseAmount.setRatioNumerator(ratioNumerator);

      final StructuredDoseCluster.RatioDenominatorCluster ratioDenominator =
          new StructuredDoseCluster.RatioDenominatorCluster();
      ratioDenominator.setAmount(DataValueUtils.getQuantity(quantityDenominator, ""));
      ratioDenominator.setDoseUnit(DataValueUtils.getLocalCodedText(quantityDenominatorUnit, quantityDenominatorUnit));
      doseAmount.setRatioDenominator(ratioDenominator);
    }
    if (doseFormCode != null)
    {
      final IngredientsAndFormCluster ingredientsAndForm = new IngredientsAndFormCluster();
      ingredientsAndForm.setForm(DataValueUtils.getLocalCodedText(doseFormCode, doseFormName));
      orderActivity.setIngredientsAndForm(ingredientsAndForm);
    }
    return orderActivity;
  }

  public static MedicationTimingCluster buildMedicationTimingCluster(
      final Long dailyCount,
      final Integer doseInterval,
      final Integer numberOfAdministration,
      final DosingFrequencyTypeEnum timingDescription,
      final DateTime start,
      final DateTime stop,
      final Integer daysFrequency,
      final DayOfWeek... daysOfWeek)
  {
    return buildMedicationTimingCluster(
        dailyCount, doseInterval, numberOfAdministration, timingDescription, start, stop, daysFrequency, false, daysOfWeek);
  }

  public static MedicationTimingCluster buildMedicationTimingCluster(
      final Long dailyCount,
      final Integer doseInterval,
      final Integer numberOfAdministration,
      final DosingFrequencyTypeEnum timingDescription,
      final DateTime start,
      final DateTime stop,
      final Integer daysFrequency,
      final boolean variableDose,
      final DayOfWeek... daysOfWeek)
  {
    final MedicationTimingCluster medicationTiming = new MedicationTimingCluster();
    final TimingCluster timing = new TimingCluster();
    medicationTiming.setTiming(timing);
    if (dailyCount != null)
    {
      final DvCount count = new DvCount();
      count.setMagnitude(dailyCount);
      timing.setDailyCount(count);
      if (!variableDose)
      {
        if (dailyCount == 1)
        {
          timing.getTime().add(buildDvTime(start.getHourOfDay(), start.getMinuteOfHour()));
        }
        else if (dailyCount == 2)
        {
          timing.getTime().add(buildDvTime(8, 0));
          timing.getTime().add(buildDvTime(20, 0));
        }
        else if (dailyCount == 3)
        {
          timing.getTime().add(buildDvTime(8, 0));
          timing.getTime().add(buildDvTime(13, 0));
          timing.getTime().add(buildDvTime(20, 0));
        }
        else if (dailyCount == 4)
        {
          timing.getTime().add(buildDvTime(8, 0));
          timing.getTime().add(buildDvTime(13, 0));
          timing.getTime().add(buildDvTime(17, 0));
          timing.getTime().add(buildDvTime(21, 0));
        }
        else if (dailyCount == 5)
        {
          timing.getTime().add(buildDvTime(0, 0));
          timing.getTime().add(buildDvTime(8, 0));
          timing.getTime().add(buildDvTime(12, 0));
          timing.getTime().add(buildDvTime(16, 0));
          timing.getTime().add(buildDvTime(20, 0));
        }
        else
        {
          throw new IllegalArgumentException("Test data not set for daily count: " + dailyCount);
        }
      }
    }
    if (doseInterval != null)
    {
      timing.setInterval(DataValueUtils.getDuration(0, 0, 0, doseInterval, 0, 0));
      if (!variableDose)
      {
        final HourMinuteDto hourMinuteDto = new HourMinuteDto(start.getHourOfDay(), start.getMinuteOfHour());
        timing.getTime().add(buildDvTime(hourMinuteDto));
      }
    }
    if (numberOfAdministration != null)
    {
      final DvCount numberOfAdministrations = new DvCount();
      numberOfAdministrations.setMagnitude(1L);
      medicationTiming.setNumberOfAdministrations(numberOfAdministrations);
    }
    if (timingDescription != null)
    {
      medicationTiming.setTimingDescription(DataValueUtils.getText(DosingFrequencyTypeEnum.getFullString(timingDescription)));
     if (!variableDose)
     {
       if (timingDescription == DosingFrequencyTypeEnum.MORNING)
       {
         timing.getTime().add(buildDvTime(8, 0));
       }
       else if (timingDescription == DosingFrequencyTypeEnum.EVENING)
       {
         timing.getTime().add(buildDvTime(21, 0));
       }
       else if (timingDescription == DosingFrequencyTypeEnum.NOON)
       {
         timing.getTime().add(buildDvTime(12, 0));
       }
       else if (timingDescription == DosingFrequencyTypeEnum.ONCE_THEN_EX)
       {
         timing.getTime().add(buildDvTime(start.getHourOfDay(), start.getMinuteOfHour()));
       }
       else
       {
         throw new IllegalArgumentException("Test data not set for timingDescription: " + timingDescription);
       }
     }
    }
    if (daysOfWeek != null)
    {
      for (final DayOfWeek dayOfWeek : daysOfWeek)
      {
        timing.getDayOfWeek().add(DataValueUtils.getCodedText(DayOfWeek.valueOf(dayOfWeek.name())));
      }
    }
    if (daysFrequency != null)
    {
      timing.setInterval(DataValueUtils.getDuration(0, 0, daysFrequency, 0, 0, 0));
    }

    medicationTiming.setStartDate(DataValueUtils.getDateTime(start));
    medicationTiming.setStopDate(stop != null ? DataValueUtils.getDateTime(stop) : null);
    return medicationTiming;
  }

  public static InfusionAdministrationDetailsCluster buildInfusionAdministrationDetails(
      final double rate,
      final String rateUnit)
  {
    final InfusionAdministrationDetailsCluster infusionDetails = new InfusionAdministrationDetailsCluster();
    infusionDetails.setDoseAdministrationRate(DataValueUtils.getQuantity(rate, rateUnit));
    return infusionDetails;
  }

  public static RoundsIntervalDto getTestRoundsIntervalDto()
  {
    final RoundsIntervalDto roundsDto = new RoundsIntervalDto();
    roundsDto.setStartHour(7);
    roundsDto.setStartMinute(0);
    roundsDto.setEndHour(17);
    roundsDto.setEndMinute(0);
    return roundsDto;
  }

  public static MedicationAdministrationComposition buildMedicationAdministrationComposition(
      final String uidValue,
      final DateTime actionTime,
      final LocatableRef linkToInstruction,
      final String composer,
      final Double doseNumerator,
      final String doseNumeratorUnit,
      final String comment,
      final MedicationActionEnum medicationActionEnum,
      final AdministrationTypeEnum administrationTypeEnum)
  {
    final MedicationAdministrationComposition composition = new MedicationAdministrationComposition();
    final MedicationAdministrationComposition.MedicationDetailSection medicationDetail =
        new MedicationAdministrationComposition.MedicationDetailSection();
    composition.setMedicationDetail(medicationDetail);
    composition.getMedicationDetail().getMedicationAction().add(new MedicationActionAction());


    final ObjectVersionId uid = new ObjectVersionId();
    uid.setValue(uidValue);
    composition.setUid(uid);
    final MedicationActionAction medicationActionAction = composition.getMedicationDetail().getMedicationAction().get(0);
    final AdministrationDetailsCluster administrationDetails = new AdministrationDetailsCluster();
    medicationActionAction.setAdministrationDetails(administrationDetails);
    if (actionTime != null)
    {
      medicationActionAction.setTime(DataValueUtils.getDateTime(actionTime));
    }
    if (linkToInstruction != null)
    {
      final InstructionDetails instructionDetails = new InstructionDetails();
      instructionDetails.setActivityId("activities[at0001] ");
      instructionDetails.setInstructionId(linkToInstruction);
      medicationActionAction.setInstructionDetails(instructionDetails);
    }
    if (composer != null)
    {
      final PartyIdentified composerPartyIdentified = new PartyIdentified();
      composerPartyIdentified.setName(composer);
      composition.setComposer(composerPartyIdentified);
    }

    medicationActionAction.setStructuredDose(MedicationsEhrUtils.buildStructuredDose(doseNumerator, doseNumeratorUnit));
    medicationActionAction.setIsmTransition(new IsmTransition());
    medicationActionAction.getIsmTransition().setCareflowStep(medicationActionEnum.getCareflowStep());
    medicationActionAction.getIsmTransition().setCurrentState(medicationActionEnum.getCurrentState());
    if(medicationActionAction.getReason().isEmpty())
    {
      medicationActionAction.getReason().add(new DvText());
    }
    medicationActionAction.getReason().get(0).setValue(AdministrationTypeEnum.getFullString(administrationTypeEnum));
    medicationActionAction.setComment(DataValueUtils.getText(comment));
    medicationActionAction.setAdministrationDetails(new AdministrationDetailsCluster());

    return composition;
  }

  public static DvTime buildDvTime(final HourMinuteDto hourMinute)
  {
    return buildDvTime(hourMinute.getHour(), hourMinute.getMinute());
  }

  public static DvTime buildDvTime(final int hour, final int minute)
  {
    return ConversionUtils.toDvTime(new DateTime(2000, 1, 1, hour, minute).toLocalTime());
  }
}
