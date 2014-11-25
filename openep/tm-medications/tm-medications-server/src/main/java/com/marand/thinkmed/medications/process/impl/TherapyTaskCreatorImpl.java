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

package com.marand.thinkmed.medications.process.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.daterule.service.MafDateRuleService;
import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.maf.core.openehr.util.InstructionTranslator;
import com.marand.maf.core.time.DayType;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.StructuredDoseCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrLinkType;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.MedicationsEhrUtils;
import com.marand.thinkmed.medications.converter.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.process.TherapyTaskCreator;
import com.marand.thinkmed.medications.process.task.MedicationTaskDef;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.joda.time.format.ISODateTimeFormat;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDate;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster.IngredientQuantityCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster.DayOfWeek;

/**
 * @author Mitja Lapajne
 */
public class TherapyTaskCreatorImpl implements TherapyTaskCreator, InitializingBean
{
  private MafDateRuleService mafDateRuleService;
  private MedicationsBo medicationsBo;

  public void setMafDateRuleService(final MafDateRuleService mafDateRuleService)
  {
    this.mafDateRuleService = mafDateRuleService;
  }

  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    Assert.notNull(mafDateRuleService, "mafDateRuleService is required");
    Assert.notNull(medicationsBo, "medicationsBo is required");
  }

  @Override
  public List<NewTaskRequestDto> createTasks(
      final Long patientId,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair,
      final AdministrationTimingDto administrationTiming,
      final RoundsIntervalDto roundsInterval,
      final DateTime actionTimestamp,
      final boolean therapyStart,
      final DateTime lastTaskTimestamp)
  {
    final MedicationOrderComposition composition = therapyInstructionPair.getFirst();
    final MedicationInstructionInstruction instruction = therapyInstructionPair.getSecond();

    if (!MedicationsEhrUtils.isContinuousInfusion(therapyInstructionPair.getSecond()) &&
        MedicationsEhrUtils.isWhenNeeded(therapyInstructionPair.getSecond()))
    {
      return new ArrayList<>();
    }

    final List<Interval> tasksCreationIntervals =
        calculateAdministrationTasksInterval(roundsInterval, instruction, actionTimestamp, therapyStart);

    return createTasks(
        patientId,
        composition,
        instruction,
        administrationTiming,
        roundsInterval,
        tasksCreationIntervals,
        therapyStart,
        lastTaskTimestamp);
  }

  private List<NewTaskRequestDto> createTasks(
      final Long patientId,
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final AdministrationTimingDto administrationTiming,
      final RoundsIntervalDto roundsInterval,
      final List<Interval> tasksCreationIntervals,
      final boolean therapyStart,
      final DateTime lastTaskTimestamp)
  {
    final boolean simple = MedicationsEhrUtils.isSimpleInstruction(instruction);
    final boolean variable = MedicationsEhrUtils.isVariableInstruction(instruction);
    final boolean variableDays = MedicationsEhrUtils.isVariableDaysInstruction(instruction);
    String linkedTherapyId = null; //last task triggers task creation for linked therapy

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair =
        Pair.of(composition, instruction);
    for (final MedicationInstructionInstruction inst : composition.getMedicationDetail().getMedicationInstruction())
    {
      if (medicationsBo.doesInstructionHaveLinkToCompareInstruction(inst, instructionPair, OpenEhrLinkType.ISSUE))
      {
        linkedTherapyId = InstructionTranslator.translate(inst, composition);
        break;
      }
    }

    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();
    for (final Interval interval : tasksCreationIntervals)
    {
      if (variableDays)
      {
        final Map<DateTime, TherapyDoseDto> administrationDateTimesMap = new HashMap<>();
        for (final OrderActivity orderActivity : instruction.getOrder())
        {
          final TherapyDoseDto dose = getTherapyDoseDto(orderActivity, simple);
          if (dose != null)
          {
            final DvTime dvTime = orderActivity.getMedicationTiming().getTiming().getTime().get(0);
            final DvDate dvDate = orderActivity.getMedicationTiming().getTiming().getDate().get(0);
            final Pair<Integer, Integer> parsedTime = DvUtils.getHourMinute(dvTime);
            final DateTime parsedDate = ISODateTimeFormat.date().parseDateTime(dvDate.getValue());

            administrationDateTimesMap.put(
                new DateTime(
                    parsedDate.getYear(),
                    parsedDate.getMonthOfYear(),
                    parsedDate.getDayOfMonth(),
                    parsedTime.getFirst(),
                    parsedTime.getSecond()),
                dose);
          }
        }
        final List<NewTaskRequestDto> tasks = createTasksForVariableDaysTherapy(
            patientId,
            instruction,
            composition.getUid().getValue(),
            interval,
            administrationDateTimesMap,
            therapyStart);
        taskRequests.addAll(tasks);
      }
      else if (variable)
      {
        final Map<HourMinuteDto, TherapyDoseDto> administrationTimesMap = new HashMap<>();
        for (final OrderActivity orderActivity : instruction.getOrder())
        {
          final TherapyDoseDto dose = getTherapyDoseDto(orderActivity, simple);
          final Pair<Integer, Integer> parsedTime =
              DvUtils.getHourMinute(orderActivity.getMedicationTiming().getTiming().getTime().get(0));
          administrationTimesMap.put(new HourMinuteDto(parsedTime.getFirst(), parsedTime.getSecond()), dose);
        }
        final List<NewTaskRequestDto> tasks = createTasksForVariableTherapy(
            patientId,
            instruction,
            composition.getUid().getValue(),
            interval,
            administrationTimesMap,
            therapyStart);
        taskRequests.addAll(tasks);
      }
      else
      {
        final TherapyDoseDto dose = getTherapyDoseDto(instruction.getOrder().get(0), simple);
        final List<NewTaskRequestDto> tasks = createTasksForConstantTherapy(
            patientId,
            instruction,
            composition.getUid().getValue(),
            interval,
            dose,
            administrationTiming,
            roundsInterval,
            therapyStart,
            lastTaskTimestamp,
            linkedTherapyId);
        taskRequests.addAll(tasks);
      }
    }
    return taskRequests;
  }

  private List<NewTaskRequestDto> createTasksForConstantTherapy(
      final long patientId,
      final MedicationInstructionInstruction instruction,
      final String compositionUid,
      final Interval taskCreationInterval,
      final TherapyDoseDto dose,
      final AdministrationTimingDto administrationTiming,
      final RoundsIntervalDto roundsIntervalDto,
      final boolean therapyStart,
      final DateTime lastTaskTimestamp,
      final String linkedTherapyId)
  {
    final DateTime therapyEnd = DataValueUtils.getDateTime(instruction.getOrder().get(0).getMedicationTiming().getStopDate());
    final boolean therapyEndsWithLastTask = taskCreationInterval.getEnd().equals(therapyEnd);
    final DosingFrequencyDto dosingFrequency =
        MedicationFromEhrConverter.getDosingFrequency(instruction.getOrder().get(0).getMedicationTiming());

    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();

    final DateTime calculationBaseTimestamp = therapyStart ? taskCreationInterval.getStart() : lastTaskTimestamp;
    if (therapyStart)
    {
      final NewTaskRequestDto startTaskRequest =
          createMedicationTaskRequest(
              instruction,
              compositionUid,
              patientId,
              AdministrationTypeEnum.START,
              taskCreationInterval.getStart(),
              dose);
      taskRequests.add(startTaskRequest);
    }
    if (dosingFrequency != null && dosingFrequency.getType() != DosingFrequencyTypeEnum.ONCE_THEN_EX)
    {
      List<HourMinuteDto> defaultAdministrationTimes =
          getPossibleAdministrations(administrationTiming, dosingFrequency.getKey());
      if (defaultAdministrationTimes == null && dosingFrequency.getType() == DosingFrequencyTypeEnum.DAILY_COUNT)
      {
        defaultAdministrationTimes =
            calculatePossibleAdministrationsForDailyCount(
                dosingFrequency.getValue(), roundsIntervalDto, calculationBaseTimestamp);
      }
      final DateTime nextTime =
          getNextAdministrationTime(calculationBaseTimestamp, defaultAdministrationTimes, dosingFrequency);

      DateTime time = calculationBaseTimestamp;

      if (therapyStart && dosingFrequency.getType() != DosingFrequencyTypeEnum.BETWEEN_DOSES &&
          Minutes.minutesBetween(time, nextTime).getMinutes() < 30)
      {
        time = getNextAdministrationTime(nextTime, defaultAdministrationTimes, dosingFrequency);
      }
      else
      {
        time = nextTime;
      }

      while (!time.isAfter(taskCreationInterval.getEnd()))
      {
        if (time.isAfter(taskCreationInterval.getStart()))
        {
          taskRequests.add(
              createMedicationTaskRequest(
                  instruction, compositionUid, patientId, AdministrationTypeEnum.START, time, dose));
        }
        time = getNextAdministrationTime(time, defaultAdministrationTimes, dosingFrequency);
      }
    }
    else if (MedicationsEhrUtils.isContinuousInfusion(instruction))
    {
      if (therapyEnd != null && !therapyEnd.isAfter(taskCreationInterval.getEnd()))
      {
        final NewTaskRequestDto endTaskRequest =
            createMedicationTaskRequest(
                instruction, compositionUid, patientId, AdministrationTypeEnum.STOP, therapyEnd, dose);
        taskRequests.add(endTaskRequest);
      }
    }

    if (therapyEndsWithLastTask)
    {
      final NewTaskRequestDto lastTask = taskRequests.get(taskRequests.size() - 1);
      lastTask.getVariables().add(Pair.of(MedicationTaskDef.TRIGGERS_THERAPY_ID, linkedTherapyId));
    }
    return taskRequests;
  }

  private List<NewTaskRequestDto> createTasksForVariableTherapy(
      final long patientId,
      final MedicationInstructionInstruction instruction,
      final String compositionUid,
      final Interval taskCreationInterval,
      final Map<HourMinuteDto, TherapyDoseDto> administrationTimesMap,
      final boolean therapyStart)
  {
    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();

    Pair<DateTime, TherapyDoseDto> timeWithDose =
        getNextAdministrationTimeWithDose(
            therapyStart ? taskCreationInterval.getStart().minusMinutes(30) : taskCreationInterval.getStart(),
            administrationTimesMap);
    if (therapyStart)
    {
      final NewTaskRequestDto startTaskRequest =
          createMedicationTaskRequest(
              instruction,
              compositionUid,
              patientId,
              AdministrationTypeEnum.START,
              taskCreationInterval.getStart(),
              timeWithDose.getSecond());
      taskRequests.add(startTaskRequest);
      if (Minutes.minutesBetween(taskCreationInterval.getStart(), timeWithDose.getFirst()).getMinutes() < 30)
      {
        timeWithDose = getNextAdministrationTimeWithDose(taskCreationInterval.getStart(), administrationTimesMap);
      }
    }
    final boolean isContinuousInfusion = MedicationsEhrUtils.isContinuousInfusion(instruction);
    if (therapyStart || !isContinuousInfusion) //don't create tasks on review if continuous infusion
    {
      while (!timeWithDose.getFirst().isAfter(taskCreationInterval.getEnd()))
      {
        if (timeWithDose.getFirst().isAfter(taskCreationInterval.getStart()))
        {
          final NewTaskRequestDto medicationTaskRequest =
              createMedicationTaskRequest(
                  instruction,
                  compositionUid,
                  patientId,
                  isContinuousInfusion ? AdministrationTypeEnum.ADJUST_INFUSION : AdministrationTypeEnum.START,
                  timeWithDose.getFirst(),
                  timeWithDose.getSecond());
          taskRequests.add(medicationTaskRequest);
        }
        timeWithDose = getNextAdministrationTimeWithDose(timeWithDose.getFirst(), administrationTimesMap);

        if (MedicationsEhrUtils.isContinuousInfusion(instruction) &&
            !timeWithDose.getFirst().withTimeAtStartOfDay().equals(taskCreationInterval.getStart().withTimeAtStartOfDay()))
        {
          break;
        }
      }
    }

    if (MedicationsEhrUtils.isContinuousInfusion(instruction))
    {
      final DateTime therapyEnd =
          DataValueUtils.getDateTime(instruction.getOrder().get(0).getMedicationTiming().getStopDate());
      if (therapyEnd != null && !therapyEnd.isAfter(taskCreationInterval.getEnd()))
      {
        final NewTaskRequestDto endTaskRequest =
            createMedicationTaskRequest(
                instruction, compositionUid, patientId, AdministrationTypeEnum.STOP, therapyEnd, timeWithDose.getSecond());
        taskRequests.add(endTaskRequest);
      }
    }
    return taskRequests;
  }

  private List<NewTaskRequestDto> createTasksForVariableDaysTherapy( //TODO Mitja
      final long patientId,
      final MedicationInstructionInstruction instruction,
      final String compositionUid,
      final Interval taskCreationInterval,
      final Map<DateTime, TherapyDoseDto> administrationDateTimesMap,
      final boolean therapyStart)
  {
    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();

    for (final DateTime administrationTime : administrationDateTimesMap.keySet())
    {
      final NewTaskRequestDto medicationTaskRequest =
          createMedicationTaskRequest(
              instruction,
              compositionUid,
              patientId,
              AdministrationTypeEnum.START,
              administrationTime,
              administrationDateTimesMap.get(administrationTime));
      taskRequests.add(medicationTaskRequest);
    }
    return taskRequests;
  }

  @Override
  public TherapyDoseDto getTherapyDoseDto(final OrderActivity orderActivity, final boolean simple)
  {
    final StructuredDoseCluster structuredDose = orderActivity.getStructuredDose();
    if (simple)
    {
      return getTherapyDoseForSimple(structuredDose);
    }

    //complex therapy
    final AdministrationDetailsCluster administrationDetails = orderActivity.getAdministrationDetails();

    final List<IngredientsAndFormCluster.IngredientCluster> ingredients = orderActivity.getIngredientsAndForm()
        .getIngredient();

    final AdministrationDetailsCluster.InfusionAdministrationDetailsCluster infusionDetails =
        administrationDetails.getInfusionAdministrationDetails().isEmpty() ? null :
        administrationDetails.getInfusionAdministrationDetails().get(0);
    final boolean infusionHasSpeedDefined =
        infusionDetails != null && (infusionDetails.getDoseAdministrationRate() instanceof DvQuantity ||
            infusionDetails.getDoseAdministrationFormula() instanceof DvQuantity);
    final boolean isContinuousInfusion =
        MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION.isEqualTo(administrationDetails.getDeliveryMethod());

    if (isContinuousInfusion || infusionHasSpeedDefined)
    {
      return setTherapyDoseRateForComplex(infusionDetails);
    }
    if (ingredients.size() > 1)
    {
      return getTherapyDoseVolumeSumForComplex(structuredDose);
    }
    if (!ingredients.isEmpty())
    {
      return getTherapyDoseQuantityForComplex(ingredients.get(0).getIngredientQuantity());
    }

    return new TherapyDoseDto();
  }

  private TherapyDoseDto getTherapyDoseForSimple(final StructuredDoseCluster structuredDose)
  {
    if (structuredDose == null)
    {
      return null;
    }
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    if (structuredDose.getQuantity() != null)
    {
      therapyDoseDto.setNumerator(structuredDose.getQuantity().getMagnitude());
      therapyDoseDto.setNumeratorUnit(structuredDose.getDoseUnit().getDefiningCode().getCodeString());
    }
    else if (structuredDose.getRatioNumerator() != null)
    {
      therapyDoseDto.setNumerator(structuredDose.getRatioNumerator().getAmount().getMagnitude());
      therapyDoseDto.setNumeratorUnit(structuredDose.getRatioNumerator().getDoseUnit().getDefiningCode().getCodeString());
      therapyDoseDto.setDenominator(structuredDose.getRatioDenominator().getAmount().getMagnitude());
      therapyDoseDto.setDenominatorUnit(
          structuredDose.getRatioDenominator().getDoseUnit().getDefiningCode().getCodeString());
    }
    therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    return therapyDoseDto;
  }

  private TherapyDoseDto getTherapyDoseVolumeSumForComplex(final StructuredDoseCluster structuredDose)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.VOLUME_SUM);
    therapyDoseDto.setNumerator(structuredDose.getQuantity().getMagnitude());
    therapyDoseDto.setNumeratorUnit(DvUtils.getString(structuredDose.getDoseUnit()));
    return therapyDoseDto;
  }

  private TherapyDoseDto getTherapyDoseQuantityForComplex(final IngredientQuantityCluster ingredientQuantity)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    if (ingredientQuantity.getQuantity() != null)
    {
      therapyDoseDto.setNumerator(ingredientQuantity.getQuantity().getMagnitude());
      therapyDoseDto.setNumeratorUnit(ingredientQuantity.getDoseUnit().getDefiningCode().getCodeString());
    }
    else
    {
      if (ingredientQuantity.getRatioNumerator() != null)
      {
        therapyDoseDto.setNumerator(ingredientQuantity.getRatioNumerator().getAmount().getMagnitude());
        therapyDoseDto.setNumeratorUnit(
            ingredientQuantity.getRatioNumerator().getDoseUnit().getDefiningCode().getCodeString());
      }
      if (ingredientQuantity.getRatioDenominator() != null)
      {
        therapyDoseDto.setDenominator(ingredientQuantity.getRatioDenominator().getAmount().getMagnitude());
        therapyDoseDto.setDenominatorUnit(
            ingredientQuantity.getRatioDenominator().getDoseUnit().getDefiningCode().getCodeString());
      }
    }
    return therapyDoseDto;
  }

  private TherapyDoseDto setTherapyDoseRateForComplex(final AdministrationDetailsCluster.InfusionAdministrationDetailsCluster infusionDetails)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);

    if (infusionDetails.getDoseAdministrationRate() instanceof DvQuantity)    //using numerator for rate
    {
      final DvQuantity administrationRateQuantity = (DvQuantity)infusionDetails.getDoseAdministrationRate();
      therapyDoseDto.setNumerator(administrationRateQuantity.getMagnitude());
      therapyDoseDto.setNumeratorUnit(administrationRateQuantity.getUnits());
    }
    if (infusionDetails.getDoseAdministrationFormula() instanceof DvQuantity)   //using denominator for formula
    {
      final DvQuantity administrationRateFormula = (DvQuantity)infusionDetails.getDoseAdministrationFormula();
      therapyDoseDto.setDenominator(administrationRateFormula.getMagnitude());
      therapyDoseDto.setDenominatorUnit(administrationRateFormula.getUnits());
    }
    return therapyDoseDto;
  }

  List<Interval> calculateAdministrationTasksInterval(
      final RoundsIntervalDto roundsInterval,
      final MedicationInstructionInstruction instruction,
      final DateTime actionTimestamp,
      final boolean therapyStart)
  {
    final MedicationTimingCluster medicationTiming = instruction.getOrder().get(0).getMedicationTiming();
    final TimingCluster timing = medicationTiming.getTiming();
    final DateTime therapyStartTime = DataValueUtils.getDateTime(medicationTiming.getStartDate());
    final DateTime therapyEndTime = DataValueUtils.getDateTime(medicationTiming.getStopDate());

    final DateTime end = getAdministrationTasksIntervalEnd(
        !therapyStart || actionTimestamp.isBefore(therapyStartTime) ? actionTimestamp : therapyStartTime,
        therapyEndTime,
        roundsInterval);

    return removeInactiveTherapyDaysFromTasksInterval(
        therapyStart ? therapyStartTime : actionTimestamp, end, roundsInterval, timing);
  }

  private DateTime getAdministrationTasksIntervalEnd(
      final DateTime start,
      final DateTime end,
      final RoundsIntervalDto roundsInterval)
  {
    final DateTime startOfTodaysRounds =
        start.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getStartHour())
            .plusMinutes(roundsInterval.getStartMinute());

    DateTime endTimestamp =
        start.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getEndHour())
            .plusMinutes(roundsInterval.getEndMinute());

    if (start.isAfter(startOfTodaysRounds) || start.equals(startOfTodaysRounds))
    {
      endTimestamp = endTimestamp.plusDays(1);
    }

    boolean workingDay = mafDateRuleService.isDateOfType(endTimestamp.withTimeAtStartOfDay(), DayType.WORKING_DAY);

    while (!workingDay)
    {
      endTimestamp = endTimestamp.plusDays(1);
      workingDay = mafDateRuleService.isDateOfType(endTimestamp.withTimeAtStartOfDay(), DayType.WORKING_DAY);
    }
    if (end != null && endTimestamp.isAfter(end))
    {
      endTimestamp = end;
    }
    return endTimestamp;
  }

  private List<Interval> removeInactiveTherapyDaysFromTasksInterval(
      final DateTime start,
      final DateTime end,
      final RoundsIntervalDto roundsInterval,
      final TimingCluster timing)
  {
    final List<Interval> intervals = new ArrayList<>();
    DateTime tasksStart = new DateTime(start);

    final DateTime startOfTodaysRounds =
        start.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getStartHour())
            .plusMinutes(roundsInterval.getStartMinute());

    DateTime tasksEnd =
        start.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getEndHour())
            .plusMinutes(roundsInterval.getEndMinute());

    if (start.isAfter(startOfTodaysRounds) && tasksEnd.isBefore(end))
    {
      if (tasksEnd.plusDays(1).isAfter(end))
      {
        tasksEnd = end;
      }
      else
      {
        tasksEnd = tasksEnd.plusDays(1);
      }
    }
    int daysFrequency = 1;
    if (timing != null && timing.getInterval() != null)
    {
      final int days = DataValueUtils.getPeriod(timing.getInterval()).getDays();
      if (days > 0)
      {
        daysFrequency = days;
      }
    }

    boolean previousDayWasValid = isInValidDaysOfWeek(tasksStart, timing);
    if (!previousDayWasValid)
    {
      tasksStart = startOfTodaysRounds.plusDays(1);
    }
    int dayIndex = 1;
    while (tasksEnd.isBefore(end) || tasksEnd.equals(end))
    {
      final boolean validDayOfWeek = isInValidDaysOfWeek(tasksEnd, timing);
      final boolean validFrequency = dayIndex % daysFrequency == 0;
      if (validDayOfWeek && validFrequency)
      {
        previousDayWasValid = true;
      }
      else
      {
        final DateTime startOfRounds =
            tasksEnd.withTimeAtStartOfDay()
                .plusHours(roundsInterval.getStartHour())
                .plusMinutes(roundsInterval.getStartMinute());
        if (previousDayWasValid)
        {
          intervals.add(new Interval(tasksStart, startOfRounds));
        }
        previousDayWasValid = false;
        tasksStart = startOfRounds.plusDays(1);
      }
      tasksEnd = tasksEnd.plusDays(1);
      dayIndex++;
    }
    if (previousDayWasValid && dayIndex > 1 || tasksEnd.minusDays(1).isBefore(end))
    {
      if (!tasksStart.isAfter(end))
      {
        intervals.add(new Interval(tasksStart, end));
      }
    }
    return intervals;
  }

  private boolean isInValidDaysOfWeek(final DateTime dateTime, final TimingCluster timing)
  {
    if (timing == null || timing.getDayOfWeek().size() == 7 || timing.getDayOfWeek().isEmpty())
    {
      return true;
    }
    final DayOfWeek dayOfWeekEnum = MedicationsEhrUtils.dayOfWeekToEhrEnum(dateTime.withTimeAtStartOfDay());
    for (final DvCodedText validDay : timing.getDayOfWeek())
    {
      final DayOfWeek validDayEnum =
          DataValueUtils.getTerminologyEnum(DayOfWeek.class, validDay);
      if (dayOfWeekEnum == validDayEnum)
      {
        return true;
      }
    }
    return false;
  }

  private DateTime getNextAdministrationTime(
      final DateTime fromTime, final List<HourMinuteDto> possibleAdministrations, final DosingFrequencyDto dosingFrequency)
  {
    if (dosingFrequency.getType() == DosingFrequencyTypeEnum.ONCE_THEN_EX)
    {
      return fromTime;
    }
    if (dosingFrequency.getType() == DosingFrequencyTypeEnum.BETWEEN_DOSES)
    {
      final Integer betweenDoses = dosingFrequency.getValue();
      return fromTime.plusHours(betweenDoses);
    }
    if (!possibleAdministrations.isEmpty())
    {
      return getNextAdministrationTime(fromTime, possibleAdministrations);
    }
    throw new IllegalArgumentException(
        "Cannot calculate next administration timestamp for dosingFrequency: " + dosingFrequency +
            " and possibleAdministrations size: " + possibleAdministrations.size());
  }

  private DateTime getNextAdministrationTime(
      final DateTime fromTime, final List<HourMinuteDto> possibleAdministrations)
  {
    int index = 0;
    DateTime foundTime = combine(fromTime, possibleAdministrations.get(index));
    while (!foundTime.isAfter(fromTime))
    {
      index++;
      if (index >= possibleAdministrations.size())
      {
        index = 0;
        foundTime = foundTime.plusDays(1);
      }
      foundTime = combine(foundTime, possibleAdministrations.get(index));
    }
    return foundTime;
  }

  private Pair<DateTime, TherapyDoseDto> getNextAdministrationTimeWithDose(
      final DateTime fromTime, final Map<HourMinuteDto, TherapyDoseDto> administrationTimesWithDoses)
  {
    int index = 0;
    final List<HourMinuteDto> times = new ArrayList<>(administrationTimesWithDoses.keySet());
    Collections.sort(times);
    DateTime foundTime = combine(fromTime, times.get(index));
    TherapyDoseDto therapyDoseDto = administrationTimesWithDoses.get(times.get(0));
    while (!foundTime.isAfter(fromTime))
    {
      index++;
      if (index >= administrationTimesWithDoses.size())
      {
        index = 0;
        foundTime = foundTime.plusDays(1);
      }
      final HourMinuteDto hourMinute = times.get(index);
      foundTime = combine(foundTime, hourMinute);
      therapyDoseDto = administrationTimesWithDoses.get(hourMinute);
    }
    return Pair.of(foundTime, therapyDoseDto);
  }

  private List<HourMinuteDto> getPossibleAdministrations(
      final AdministrationTimingDto administrationTiming, final String frequency)
  {
    Preconditions.checkNotNull(administrationTiming);
    Preconditions.checkNotNull(frequency);

    for (final AdministrationTimingDto.AdministrationTimestampsDto administrationTimestamps : administrationTiming.getTimestampsList())
    {
      if (frequency.equals(administrationTimestamps.getFrequency()))
      {
        return administrationTimestamps.getTimesList();
      }
    }
    return null;
  }

  private DateTime combine(final DateTime fromTime, final HourMinuteDto hourMinuteDto)
  {
    return new DateTime(
        fromTime.getYear(),
        fromTime.getMonthOfYear(),
        fromTime.getDayOfMonth(),
        hourMinuteDto.getHour(),
        hourMinuteDto.getMinute());
  }

  private List<HourMinuteDto> calculatePossibleAdministrationsForDailyCount(
      final int dailyCount, final RoundsIntervalDto roundsInterval, final DateTime start)
  {
    final List<HourMinuteDto> administrationTimes = new ArrayList<>();
    if (dailyCount == 1)
    {
      administrationTimes.add(new HourMinuteDto(start.getHourOfDay(), start.getMinuteOfHour()));
    }
    else
    {
      double startMinutes = roundsInterval.getStartHour() * 60 + roundsInterval.getStartMinute();
      final double endMinutes = 24 * 60; //midnight

      final double minutesBetweenAdministrations = (endMinutes - startMinutes) / (dailyCount - 1);
      while (startMinutes <= endMinutes)
      {
        if (endMinutes - startMinutes < 0.1) //midnight (startMinutes equals endMinutes with precision 0.1)
        {
          administrationTimes.add(0, new HourMinuteDto(0, 0));
        }
        else
        {
          final long roundsInHours = Math.round(startMinutes / 60);
          administrationTimes.add(new HourMinuteDto((int)roundsInHours, 0));
        }

        startMinutes += minutesBetweenAdministrations;
      }
    }

    return administrationTimes;
  }

  @Override
  public NewTaskRequestDto createMedicationTaskRequest(
      final MedicationInstructionInstruction instruction,
      final String compositionUid,
      final long patientId,
      final AdministrationTypeEnum administrationTypeEnum,
      final DateTime timestamp,
      final TherapyDoseDto dose)
  {
    final String therapyId = InstructionTranslator.translate(instruction, compositionUid);

    return new NewTaskRequestDto(
        MedicationTaskDef.INSTANCE,
        instruction.getOrder().get(0).getMedicine().getValue(),
        instruction.getOrder().get(0).getMedicine().getValue(),
        instruction.getOrder().get(0).getMedicine().getValue(),
        "sessionId", //TODO THERAPY_TASK
        timestamp,
        null,
        Pair.of(MedicationTaskDef.PATIENT_ID, patientId),
        Pair.of(MedicationTaskDef.THERAPY_ID, therapyId),
        Pair.of(MedicationTaskDef.ADMINISTRATION_TYPE, administrationTypeEnum.name()),
        Pair.of(MedicationTaskDef.DOSE_TYPE, dose != null ? dose.getTherapyDoseTypeEnum().name() : null),
        Pair.of(MedicationTaskDef.DOSE_NUMERATOR, dose != null ? dose.getNumerator() : null),
        Pair.of(MedicationTaskDef.DOSE_NUMERATOR_UNIT, dose != null ? dose.getNumeratorUnit() : null),
        Pair.of(MedicationTaskDef.DOSE_DENOMINATOR, dose != null ? dose.getDenominator() : null),
        Pair.of(MedicationTaskDef.DOSE_DENOMINATOR_UNIT, dose != null ? dose.getDenominatorUnit() : null));
  }

  @Override
  public NewTaskRequestDto createTaskRequestFromTaskDto(final TaskDto taskDto)
  {
    final NewTaskRequestDto newTaskRequestDto = new NewTaskRequestDto(
        MedicationTaskDef.INSTANCE,
        taskDto.getName(),
        taskDto.getDisplayName(),
        taskDto.getDescription(),
        taskDto.getAssignee(),
        taskDto.getDueTime(),
        taskDto.getAssociatedEntity());
    for (final String key : taskDto.getVariables().keySet())
    {
      newTaskRequestDto.addVariables(Pair.of(TaskVariable.named(key), taskDto.getVariables().get(key)));
    }
    return newTaskRequestDto;
  }
}
