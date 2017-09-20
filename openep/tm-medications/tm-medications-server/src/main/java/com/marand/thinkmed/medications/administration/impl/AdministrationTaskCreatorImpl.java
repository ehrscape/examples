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

package com.marand.thinkmed.medications.administration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster.DayOfWeek;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.AdministrationTaskCreator;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantTherapy;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.VariableTherapy;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.OxygenTaskDef;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class AdministrationTaskCreatorImpl implements AdministrationTaskCreator
{
  private AdministrationTaskConverter administrationTaskConverter;
  private AdministrationUtils administrationUtils;

  private int taskCreationDays;

  @Required
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Required
  public void setAdministrationUtils(final AdministrationUtils administrationUtils)
  {
    this.administrationUtils = administrationUtils;
  }

  @Required
  public void setTaskCreationDays(final Integer taskCreationDays)
  {
    this.taskCreationDays = taskCreationDays == null ? 14 : taskCreationDays;
  }

  @Override
  public List<NewTaskRequestDto> createTaskRequests(
      final String patientId,
      @Nonnull final TherapyDto therapy,
      @Nonnull final AdministrationTaskCreateActionEnum action,
      @Nonnull final DateTime actionTimestamp,
      final DateTime lastTaskTimestamp)
  {
    Preconditions.checkNotNull(therapy, "therapy is required");
    Preconditions.checkNotNull(action, "action is required");
    Preconditions.checkNotNull(actionTimestamp, "actionTimestamp is required");

    final List<Interval> tasksCreationIntervals = calculateAdministrationTasksInterval(
        therapy,
        actionTimestamp,
        action,
        taskCreationDays,
        lastTaskTimestamp);

    if (tasksCreationIntervals.isEmpty())
    {
      return new ArrayList<>();
    }

    return createTasksInInterval(patientId, therapy, action, tasksCreationIntervals, actionTimestamp, lastTaskTimestamp);
  }

  private List<NewTaskRequestDto> createTasksInInterval(
      final String patientId,
      final TherapyDto therapy,
      final AdministrationTaskCreateActionEnum action,
      final List<Interval> tasksCreationIntervals,
      final DateTime actionTimestamp,
      final DateTime lastTaskTimestamp)
  {
    final boolean continuousInfusion = isContinuousInfusion(therapy);
    final boolean recurringContinuousInfusion = isRecurringContinuousInfusion(therapy);
    final boolean variable = therapy instanceof VariableTherapy;
    final boolean variableDays = isVariableDaysTherapy(therapy);

    final boolean isPresetTimeAction =
        EnumSet.of(
            AdministrationTaskCreateActionEnum.PRESET_TIME_ON_NEW_PRESCRIPTION,
            AdministrationTaskCreateActionEnum.PRESET_TIME_ON_MODIFY).contains(action);

    final boolean whenNeeded = therapy.getWhenNeeded() != null && therapy.getWhenNeeded();
    if (!isPresetTimeAction && !continuousInfusion && whenNeeded)
    {
      return new ArrayList<>();
    }

    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();
    for (final Interval interval : tasksCreationIntervals)
    {
      final List<NewTaskRequestDto> tasks;

      if (therapy instanceof OxygenTherapyDto)
      {
        tasks = createTasksForContinuousInfusion(patientId, therapy, interval, lastTaskTimestamp, action);
      }
      else if (recurringContinuousInfusion)
      {
        tasks = createTasksForRecurringContinuousInfusion(
            patientId,
            therapy,
            interval,
            action,
            lastTaskTimestamp,
            actionTimestamp);
      }
      else if (continuousInfusion && variable)
      {
        tasks = createTasksForVariableContinuousInfusion(
            patientId,
            (VariableComplexTherapyDto)therapy,
            interval,
            action,
            lastTaskTimestamp,
            actionTimestamp);
      }
      else if (continuousInfusion)
      {
        tasks = createTasksForContinuousInfusion(
            patientId,
            therapy,
            interval,
            lastTaskTimestamp,
            action);
      }
      else if (variableDays) //protocol
      {
        //noinspection ConstantConditions
        tasks = createTasksForVariableDaysTherapy(patientId, (VariableSimpleTherapyDto)therapy, interval, action);
      }
      else if (variable)
      {
        tasks = createTasksForVariableTherapy(patientId, therapy, interval, action);
      }
      else
      {
        tasks = createTasksForConstantTherapy(patientId, therapy, interval, action);
      }
      taskRequests.addAll(tasks);
    }
    return taskRequests;
  }

  private boolean isContinuousInfusion(final TherapyDto therapy)
  {
    return therapy instanceof ComplexTherapyDto && ((ComplexTherapyDto)therapy).isContinuousInfusion();
  }

  private boolean isRecurringContinuousInfusion(final TherapyDto therapy)
  {
    return therapy instanceof VariableComplexTherapyDto &&
        ((VariableComplexTherapyDto)therapy).isRecurringContinuousInfusion();
  }

  private boolean isVariableDaysTherapy(final TherapyDto therapy)
  {
    return therapy instanceof VariableSimpleTherapyDto &&
        ((VariableSimpleTherapyDto)therapy).getTimedDoseElements().get(0).getDate() != null;
  }

  private TherapyDoseDto getTherapyDoseForOxygenTherapy(final OxygenTherapyDto therapy)
  {
    if (therapy.getFlowRate() != null)
    {
      final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
      therapyDoseDto.setNumerator(therapy.getFlowRate());
      therapyDoseDto.setNumeratorUnit(therapy.getFlowRateUnit());

      therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);

      return therapyDoseDto;
    }
    return null;
  }

  private List<NewTaskRequestDto> createTasksForContinuousInfusion(
      final String patientId,
      final TherapyDto therapy,
      final Interval taskCreationInterval,
      final DateTime lastTaskTimestamp,
      final AdministrationTaskCreateActionEnum action)
  {
    final TherapyDoseDto dose;
    if (therapy instanceof ConstantComplexTherapyDto)
    {
      dose = getTherapyDoseForComplexTherapyWithRate(((ConstantComplexTherapyDto)therapy).getDoseElement());
    }
    else if (therapy instanceof OxygenTherapyDto)
    {
      dose = getTherapyDoseForOxygenTherapy((OxygenTherapyDto)therapy);
    }
    else
    {
      throw new IllegalArgumentException("therapy type not supported for this method");
    }

    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();

    //noinspection OverlyComplexBooleanExpression
    final boolean createStartTask = action == AdministrationTaskCreateActionEnum.PRESCRIBE ||
        action == AdministrationTaskCreateActionEnum.MODIFY_BEFORE_START ||
        action == AdministrationTaskCreateActionEnum.REISSUE ||
        action == AdministrationTaskCreateActionEnum.PREVIEW_TIMES_ON_NEW_PRESCRIPTION ||
        (action == AdministrationTaskCreateActionEnum.AUTO_CREATE && lastTaskTimestamp == null);

    if (createStartTask)
    {
      final NewTaskRequestDto startTaskRequest =
          createMedicationTaskRequest(
              patientId,
              therapy,
              AdministrationTypeEnum.START,
              taskCreationInterval.getStart(),
              dose);
      taskRequests.add(startTaskRequest);
    }
    else if (action == AdministrationTaskCreateActionEnum.MODIFY)
    {
      final NewTaskRequestDto startTaskRequest =
          createMedicationTaskRequest(
              patientId,
              therapy,
              AdministrationTypeEnum.ADJUST_INFUSION,
              taskCreationInterval.getStart(),
              dose);
      taskRequests.add(startTaskRequest);
    }

    final DateTime therapyEnd = therapy.getEnd();
    if (therapyEnd != null && !therapyEnd.isAfter(taskCreationInterval.getEnd()))
    {
      final NewTaskRequestDto endTaskRequest =
          createMedicationTaskRequest(patientId, therapy, AdministrationTypeEnum.STOP, therapyEnd, null);
      taskRequests.add(endTaskRequest);
    }

    return taskRequests;
  }

  private List<NewTaskRequestDto> createTasksForVariableContinuousInfusion(
      final String patientId,
      final VariableComplexTherapyDto therapy,
      final Interval taskCreationInterval,
      final AdministrationTaskCreateActionEnum action,
      final DateTime lastTaskTimestamp,
      final DateTime actionTimestamp)
  {
    final DateTime therapyStart = therapy.getStart();

    final Map<HourMinuteDto, TherapyDoseDto> timesWithDosesMap = getTimesDosesMapForVariableTherapies(therapy);

    final HourMinuteDto lastChangeHourMinute = Iterables.getLast(timesWithDosesMap.keySet());

    final boolean isTwoDayInfusion =
        therapyStart.isAfter(therapyStart.withTime(lastChangeHourMinute.getHour(), lastChangeHourMinute.getMinute(), 0, 0));
    final DateTime lastChangeTime =
        isTwoDayInfusion ?
        therapyStart.withTime(lastChangeHourMinute.getHour(), lastChangeHourMinute.getMinute(), 0, 0).plusDays(1) :
        therapyStart.withTime(lastChangeHourMinute.getHour(), lastChangeHourMinute.getMinute(), 0, 0);

    final HourMinuteDto firstChangeHourMinute = Iterables.getFirst(timesWithDosesMap.keySet(), null);

    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();

    //noinspection OverlyComplexBooleanExpression
    final boolean createStartTask = action == AdministrationTaskCreateActionEnum.PRESCRIBE ||
        action == AdministrationTaskCreateActionEnum.MODIFY_BEFORE_START ||
        action == AdministrationTaskCreateActionEnum.PREVIEW_TIMES_ON_NEW_PRESCRIPTION ||
        action == AdministrationTaskCreateActionEnum.MODIFY ||
        (action == AdministrationTaskCreateActionEnum.AUTO_CREATE && lastTaskTimestamp == null);

    if (createStartTask)
    {
      final TherapyDoseDto dose = timesWithDosesMap.get(firstChangeHourMinute);

      final NewTaskRequestDto startTaskRequest =
          createMedicationTaskRequest(
              patientId,
              therapy,
              AdministrationTypeEnum.START,
              therapyStart,
              dose);
      taskRequests.add(startTaskRequest);
    }
    else if (action == AdministrationTaskCreateActionEnum.REISSUE)
    {
      final TherapyDoseDto dose;
      final DateTime restartTime;
      if (actionTimestamp.isBefore(therapyStart))
      {
        dose = timesWithDosesMap.get(firstChangeHourMinute);
        restartTime = therapyStart;
      }
      else if (actionTimestamp.isAfter(lastChangeTime))
      {
        dose = timesWithDosesMap.get(lastChangeHourMinute);
        restartTime = actionTimestamp;
      }
      else
      {
        dose = getPreviousAdministrationTimeWithDose(actionTimestamp, timesWithDosesMap, true).getSecond();
        restartTime = actionTimestamp;
      }
      final NewTaskRequestDto startTaskRequest =
          createMedicationTaskRequest(
              patientId,
              therapy,
              AdministrationTypeEnum.START,
              restartTime,
              dose);
      taskRequests.add(startTaskRequest);
    }

    Pair<DateTime, TherapyDoseDto> nextTimeWithDose =
        getNextAdministrationTimeWithDose(action, therapy, therapyStart, timesWithDosesMap, false);

    while (!nextTimeWithDose.getFirst().isAfter(taskCreationInterval.getEnd())
        && !nextTimeWithDose.getFirst().isAfter(lastChangeTime))
    {
      if (nextTimeWithDose.getFirst().isAfter(taskCreationInterval.getStart()))
      {
        taskRequests.add(
            createMedicationTaskRequest(
                patientId,
                therapy,
                AdministrationTypeEnum.ADJUST_INFUSION,
                nextTimeWithDose.getFirst(),
                nextTimeWithDose.getSecond()));
      }
      nextTimeWithDose =
          getNextAdministrationTimeWithDose(action, therapy, nextTimeWithDose.getFirst(), timesWithDosesMap, false);
    }

    final DateTime therapyEnd = therapy.getEnd();
    if (therapyEnd != null && !therapyEnd.isAfter(taskCreationInterval.getEnd()))
    {
      final NewTaskRequestDto endTaskRequest =
          createMedicationTaskRequest(patientId, therapy, AdministrationTypeEnum.STOP, therapyEnd, null);
      taskRequests.add(endTaskRequest);
    }
    return taskRequests;
  }

  private List<NewTaskRequestDto> createTasksForRecurringContinuousInfusion(
      final String patientId,
      final TherapyDto therapy,
      final Interval taskCreationInterval,
      final AdministrationTaskCreateActionEnum action,
      final DateTime lastTaskTimestamp,
      final DateTime actionTimestamp)
  {
    final DateTime therapyStart = therapy.getStart();
    final DateTime therapyEnd = therapy.getEnd();

    final Map<HourMinuteDto, TherapyDoseDto> timesWithDosesMap =
        getTimesDosesMapForVariableTherapies((VariableTherapy)therapy);

    final Pair<DateTime, TherapyDoseDto> lastOrCurrentTimeDose =
        getPreviousAdministrationTimeWithDose(therapyStart, timesWithDosesMap, true);

    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();

    //noinspection OverlyComplexBooleanExpression
    final boolean createStartTask = action == AdministrationTaskCreateActionEnum.PRESCRIBE ||
        action == AdministrationTaskCreateActionEnum.PREVIEW_TIMES_ON_NEW_PRESCRIPTION ||
        action == AdministrationTaskCreateActionEnum.MODIFY_BEFORE_START ||
        (action == AdministrationTaskCreateActionEnum.AUTO_CREATE && lastTaskTimestamp == null);

    if (createStartTask)
    {
      final NewTaskRequestDto startTaskRequest =
          createMedicationTaskRequest(
              patientId,
              therapy,
              AdministrationTypeEnum.START,
              therapyStart,
              lastOrCurrentTimeDose.getSecond());
      taskRequests.add(startTaskRequest);
    }
    else if (action == AdministrationTaskCreateActionEnum.MODIFY)
    {
      final NewTaskRequestDto startTaskRequest =
          createMedicationTaskRequest(
              patientId,
              therapy,
              AdministrationTypeEnum.ADJUST_INFUSION,
              therapyStart,
              lastOrCurrentTimeDose.getSecond());
      taskRequests.add(startTaskRequest);
    }
    else if (action == AdministrationTaskCreateActionEnum.REISSUE)
    {
      final Pair<DateTime, TherapyDoseDto> reissueTimeDose =
          getPreviousAdministrationTimeWithDose(actionTimestamp, timesWithDosesMap, true);

      final NewTaskRequestDto startTaskRequest =
          createMedicationTaskRequest(
              patientId,
              therapy,
              AdministrationTypeEnum.START,
              actionTimestamp,
              reissueTimeDose.getSecond());
      taskRequests.add(startTaskRequest);
    }

    Pair<DateTime, TherapyDoseDto> nextTime =
        getNextAdministrationTimeWithDose(action, therapy, taskCreationInterval.getStart(), timesWithDosesMap, false);

    while (!nextTime.getFirst().isAfter(taskCreationInterval.getEnd()))
    {
      taskRequests.add(
          createMedicationTaskRequest(
              patientId,
              therapy,
              AdministrationTypeEnum.ADJUST_INFUSION,
              nextTime.getFirst(),
              nextTime.getSecond()));
      nextTime = getNextAdministrationTimeWithDose(action, therapy, nextTime.getFirst(), timesWithDosesMap, false);
    }

    if (therapyEnd != null && !therapyEnd.isAfter(taskCreationInterval.getEnd()))
    {
      final NewTaskRequestDto endTaskRequest =
          createMedicationTaskRequest(patientId, therapy, AdministrationTypeEnum.STOP, therapyEnd, null);
      taskRequests.add(endTaskRequest);
    }
    return taskRequests;
  }

  private List<NewTaskRequestDto> createTasksForConstantTherapy(
      final String patientId,
      final TherapyDto therapy,
      final Interval taskCreationInterval,
      final AdministrationTaskCreateActionEnum action)
  {
    final ConstantTherapy constantTherapy = (ConstantTherapy)therapy;
    final TherapyDoseDto dose = getDoseForConstantTherapy(constantTherapy);

    final Map<HourMinuteDto, TherapyDoseDto> timesWithDosesMap = constantTherapy.getDoseTimes()
        .stream()
        .distinct()
        .collect(Collectors.toMap(Function.identity(), d -> dose));

    if (therapy instanceof ConstantSimpleTherapyDto)
    {
      return createTasksFromAdministrationTimes(patientId, therapy, taskCreationInterval, timesWithDosesMap, action);
    }
    else if (therapy instanceof ConstantComplexTherapyDto)
    {
      if (TherapyDoseTypeEnum.WITH_RATE.contains(dose.getTherapyDoseTypeEnum()))
      {
        return createTasksForConstantComplexTherapyWithRate(
            patientId,
            (ConstantComplexTherapyDto)therapy,
            taskCreationInterval,
            timesWithDosesMap,
            action);
      }
      return createTasksFromAdministrationTimes(patientId, therapy, taskCreationInterval, timesWithDosesMap, action);
    }
    else
    {
      throw new IllegalArgumentException("Therapy type not supported");
    }
  }

  private List<NewTaskRequestDto> createTasksForConstantComplexTherapyWithRate(
      final String patientId,
      final ConstantComplexTherapyDto therapy,
      final Interval taskCreationInterval,
      final Map<HourMinuteDto, TherapyDoseDto> timesWithDosesMap,
      final AdministrationTaskCreateActionEnum action)
  {
    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();
    if (!timesWithDosesMap.isEmpty())
    {
      Pair<DateTime, TherapyDoseDto> next = getNextAdministrationTimeWithDose(
          action,
          therapy,
          taskCreationInterval.getStart(),
          timesWithDosesMap,
          action.isTaskCreationIntervalStartIncluded());

      DateTime nextTaskTime = next.getFirst();

      final int duration = therapy.getDoseElement().getDuration();
      while (!isAfterTherapyEnd(therapy, nextTaskTime))
      {
        if (nextTaskTime.isAfter(taskCreationInterval.getEnd()))
        {
          break;
        }

        final String groupUUId =
            action == AdministrationTaskCreateActionEnum.PREVIEW_TIMES_ON_NEW_PRESCRIPTION
            ? null
            : administrationUtils.generateGroupUUId(Opt.of(therapy.getStart()).orElseGet(DateTime::now));

        taskRequests.add(createMedicationTaskRequestWithGroupUUId(
            patientId,
            groupUUId,
            therapy,
            AdministrationTypeEnum.START,
            nextTaskTime,
            next.getSecond()));

        final DateTime plannedStopTaskTime = nextTaskTime.plusMinutes(duration);
        final DateTime actualStopTaskTime =
            isAfterTherapyEnd(therapy, plannedStopTaskTime)
            ? therapy.getEnd()
            : plannedStopTaskTime;

        taskRequests.add(createMedicationTaskRequestWithGroupUUId(
            patientId,
            groupUUId,
            therapy,
            AdministrationTypeEnum.STOP,
            actualStopTaskTime,
            null));

        next = getNextAdministrationTimeWithDose(action, therapy, nextTaskTime, timesWithDosesMap, false);
        nextTaskTime = next.getFirst();
      }
    }

    return taskRequests;
  }

  private List<NewTaskRequestDto> createTasksForVariableTherapy(
      final String patientId,
      final TherapyDto therapy,
      final Interval taskCreationInterval,
      final AdministrationTaskCreateActionEnum action)
  {
    final Map<HourMinuteDto, TherapyDoseDto> timesWithDosesMap = getTimesDosesMapForVariableTherapies((VariableTherapy)therapy);
    if (therapy instanceof VariableSimpleTherapyDto)
    {
      return createTasksFromAdministrationTimes(
          patientId,
          therapy,
          taskCreationInterval,
          timesWithDosesMap,
          action);
    }
    else if (therapy instanceof VariableComplexTherapyDto)
    {
      return createTasksForVariableComplexTherapy(
          patientId,
          (VariableComplexTherapyDto)therapy,
          taskCreationInterval,
          timesWithDosesMap,
          action);
    }
    else
    {
      throw new IllegalArgumentException("Therapy type not supported");
    }
  }

  private List<NewTaskRequestDto> createTasksFromAdministrationTimes(
      final String patientId,
      final TherapyDto therapy,
      final Interval taskCreationInterval,
      final Map<HourMinuteDto, TherapyDoseDto> timesWithDosesMap,
      final AdministrationTaskCreateActionEnum action)
  {
    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();

    if (!timesWithDosesMap.isEmpty())
    {
      Pair<DateTime, TherapyDoseDto> nextTime =
          getNextAdministrationTimeWithDose(
              action,
              therapy,
              taskCreationInterval.getStart(),
              timesWithDosesMap,
              action.isTaskCreationIntervalStartIncluded());

      while (!nextTime.getFirst().isAfter(taskCreationInterval.getEnd()))
      {
        taskRequests.add(
            createMedicationTaskRequest(
                patientId,
                therapy,
                AdministrationTypeEnum.START,
                nextTime.getFirst(),
                nextTime.getSecond()));
        nextTime = getNextAdministrationTimeWithDose(action, therapy, nextTime.getFirst(), timesWithDosesMap, false);
      }
    }

    return taskRequests;
  }

  private List<NewTaskRequestDto> createTasksForVariableComplexTherapy(
      final String patientId,
      final VariableComplexTherapyDto therapy,
      final Interval taskCreationInterval,
      final Map<HourMinuteDto, TherapyDoseDto> timesWithDosesMap,
      final AdministrationTaskCreateActionEnum action)
  {
    final List<TimedComplexDoseElementDto> timedDoseElements = therapy.getTimedDoseElements();
    final List<NewTaskRequestDto> taskRequests = new ArrayList<>();
    if (!timesWithDosesMap.isEmpty())
    {
      Pair<DateTime, TherapyDoseDto> next = getNextAdministrationTimeWithDose(
          action,
          therapy,
          taskCreationInterval.getStart(),
          timesWithDosesMap,
          action.isTaskCreationIntervalStartIncluded());

      DateTime nextTaskTime = next.getFirst();

      if (action == AdministrationTaskCreateActionEnum.REISSUE) // move on to first next START task in taskCreationInterval
      {
        while (!isFirstPrescribedComplexDoseElement(timedDoseElements, nextTaskTime))
        {
          next = getNextAdministrationTimeWithDose(
              AdministrationTaskCreateActionEnum.REISSUE,
              therapy,
              nextTaskTime,
              timesWithDosesMap,
              false);

          nextTaskTime = next.getFirst();
        }
      }

      final Map<HourMinuteDto, Integer> durations = therapy.getTimedDoseElements()
          .stream()
          .collect(Collectors.toMap(TimedComplexDoseElementDto::getDoseTime, d -> d.getDoseElement().getDuration()));

      final boolean preview = action == AdministrationTaskCreateActionEnum.PREVIEW_TIMES_ON_NEW_PRESCRIPTION;
      String groupUUId = null;
      while (!isAfterTherapyEnd(therapy, nextTaskTime))
      {
        final boolean isFirst = isFirstPrescribedComplexDoseElement(timedDoseElements, nextTaskTime);
        final boolean isLast = isLastPrescribedComplexDoseElement(timedDoseElements, nextTaskTime);
        if (nextTaskTime.isAfter(taskCreationInterval.getEnd()) && isFirst)
        {
          break;
        }

        if (isFirst && !preview)
        {
          groupUUId = administrationUtils.generateGroupUUId(Opt.of(therapy.getStart()).orElseGet(DateTime::now));
        }

        Preconditions.checkArgument(preview || groupUUId != null, "groupUUId must be set for start administration!");

        taskRequests.add(createMedicationTaskRequestWithGroupUUId(
            patientId,
            groupUUId,
            therapy,
            isFirst ? AdministrationTypeEnum.START : AdministrationTypeEnum.ADJUST_INFUSION,
            nextTaskTime,
            next.getSecond()));

        if (isLast)
        {
          final DateTime stopTaskTime = nextTaskTime.plusMinutes(getDoseDurationForDate(durations, nextTaskTime));
          final boolean stopTimeAfterTherapyEnd = isAfterTherapyEnd(therapy, stopTaskTime);
          taskRequests.add(createMedicationTaskRequestWithGroupUUId(
              patientId,
              groupUUId,
              therapy,
              AdministrationTypeEnum.STOP,
              stopTimeAfterTherapyEnd ? therapy.getEnd() : stopTaskTime,
              null));

          if (stopTimeAfterTherapyEnd)
          {
            break;
          }
        }

        next = getNextAdministrationTimeWithDose(action, therapy, next.getFirst(), timesWithDosesMap, false);
        nextTaskTime = next.getFirst();

        if (!isLast && isAfterTherapyEnd(therapy, nextTaskTime))
        {
          taskRequests.add(createMedicationTaskRequestWithGroupUUId(
              patientId,
              groupUUId,
              therapy,
              AdministrationTypeEnum.STOP,
              therapy.getEnd(),
              null));
        }
      }
    }

    return taskRequests;
  }

  private Integer getDoseDurationForDate(final Map<HourMinuteDto, Integer> durations, final DateTime date)
  {
    return Opt.of(
        durations.get(new HourMinuteDto(date.getHourOfDay(), date.getMinuteOfHour())))
        .orElseThrow(() -> new IllegalStateException("No matching date in durations map!"));
  }

  private boolean isAfterTherapyEnd(final TherapyDto therapy, final DateTime time)
  {
    return therapy.getEnd() != null && (time.isAfter(therapy.getEnd()) || time.isEqual(therapy.getEnd()));
  }

  private boolean isFirstPrescribedComplexDoseElement(final List<TimedComplexDoseElementDto> elements, final DateTime time)
  {
    final TimedComplexDoseElementDto firstElement = elements.get(0);
    return time.getHourOfDay() == firstElement.getDoseTime().getHour()
        && time.getMinuteOfHour() == firstElement.getDoseTime().getMinute();
  }

  private boolean isLastPrescribedComplexDoseElement(final List<TimedComplexDoseElementDto> elements, final DateTime time)
  {
    final TimedComplexDoseElementDto lastElement = elements.get(elements.size() -1);
    return time.getHourOfDay() == lastElement.getDoseTime().getHour()
        && time.getMinuteOfHour() == lastElement.getDoseTime().getMinute();
  }

  private List<NewTaskRequestDto> createTasksForVariableDaysTherapy(
      final String patientId,
      final VariableSimpleTherapyDto therapy,
      final Interval taskCreationInterval,
      final AdministrationTaskCreateActionEnum action)
  {
    final Map<DateTime, TherapyDoseDto> timesWithDosesMap = new HashMap<>();

    for (final TimedSimpleDoseElementDto timedDoseElement : therapy.getTimedDoseElements())
    {
      getTimedDoseForVariableDaysTherapy(therapy, taskCreationInterval, action, timedDoseElement, timedDoseElement.getDate())
          .forEach(e -> timesWithDosesMap.put(e.getFirst(), e.getSecond()));
    }

    final DateTime lastAdministrationDateTime = timesWithDosesMap.entrySet().stream()
        .map(Map.Entry::getKey)
        .max(Comparator.naturalOrder())
        .orElse(null);

    final boolean therapyContinuesAfterLastDefinedDose =
        lastAdministrationDateTime != null && lastAdministrationDateTime.isBefore(taskCreationInterval.getEnd());
    if (therapyContinuesAfterLastDefinedDose)
    {
      //repeat last day
      final DateTime lastDayStart = lastAdministrationDateTime.withTimeAtStartOfDay();
      final List<TimedSimpleDoseElementDto> lastDayDoses = therapy.getTimedDoseElements().stream()
          .filter(t -> t.getDate().withTimeAtStartOfDay().equals(lastDayStart))
          .collect(Collectors.toList());

      DateTime nextDay = lastDayStart.plusDays(1);
      while (taskCreationInterval.contains(nextDay))
      {
        for (final TimedSimpleDoseElementDto timedDoseElement : lastDayDoses)
        {
          getTimedDoseForVariableDaysTherapy(therapy, taskCreationInterval, action, timedDoseElement, nextDay)
              .forEach(e -> timesWithDosesMap.put(e.getFirst(), e.getSecond()));
        }
        nextDay = nextDay.plusDays(1);
      }
    }

    return timesWithDosesMap.keySet()
        .stream()
        .sorted(Comparator.naturalOrder())
        .filter(administrationTime -> !administrationTime.isAfter(taskCreationInterval.getEnd()))
        .map(administrationTime ->
                 createMedicationTaskRequest(
                     patientId,
                     therapy,
                     AdministrationTypeEnum.START,
                     administrationTime,
                     timesWithDosesMap.get(administrationTime)))
        .collect(Collectors.toList());
  }

  private Opt<Pair<DateTime, TherapyDoseDto>> getTimedDoseForVariableDaysTherapy(
      final VariableSimpleTherapyDto therapy,
      final Interval taskCreationInterval,
      final AdministrationTaskCreateActionEnum action,
      final TimedSimpleDoseElementDto timedDoseElement,
      final DateTime date)
  {
    final HourMinuteDto doseTime = timedDoseElement.getDoseTime();
    final DateTime administrationDateTime =
        date.withTimeAtStartOfDay().plusHours(doseTime.getHour()).plusMinutes(doseTime.getMinute());
    final TherapyDoseDto dose = getTherapyDoseForSimpleTherapy(timedDoseElement.getDoseElement(), therapy);

    final DateTime taskCreationIntervalEnd = taskCreationInterval.getEnd();
    final boolean inTaskCreationInterval;
    //noinspection IfMayBeConditional
    if (action.isTaskCreationIntervalStartIncluded())
    {
      inTaskCreationInterval = taskCreationInterval.contains(administrationDateTime) ||
          taskCreationIntervalEnd.equals(administrationDateTime);
    }
    else
    {
      inTaskCreationInterval = taskCreationInterval.getStart().isBefore(administrationDateTime) &&
          (taskCreationIntervalEnd.isAfter(administrationDateTime) || taskCreationIntervalEnd.equals(administrationDateTime));
    }

    if (dose != null && dose.getNumerator() != null && inTaskCreationInterval)
    {
      return Opt.of(Pair.of(administrationDateTime, dose));
    }
    return Opt.none();
  }

  private TherapyDoseDto getDoseForConstantTherapy(final ConstantTherapy therapy)
  {
    if (therapy instanceof ConstantSimpleTherapyDto)
    {
      final ConstantSimpleTherapyDto constantSimpleTherapy = (ConstantSimpleTherapyDto)therapy;
      return getTherapyDoseForSimpleTherapy(constantSimpleTherapy.getDoseElement(), constantSimpleTherapy);
    }
    else if (therapy instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto constantComplexTherapy = (ConstantComplexTherapyDto)therapy;
      return getTherapyDoseForComplexTherapy(constantComplexTherapy.getDoseElement(), constantComplexTherapy);
    }
    else
    {
      throw new IllegalArgumentException("Therapy type not supported");
    }
  }

  private Map<HourMinuteDto, TherapyDoseDto> getTimesDosesMapForVariableTherapies(final VariableTherapy therapy)
  {
    if (therapy instanceof VariableSimpleTherapyDto)
    {
      final VariableSimpleTherapyDto variableSimpleTherapy = (VariableSimpleTherapyDto)therapy;
      return variableSimpleTherapy.getTimedDoseElements().stream()
          .collect(Collectors.toMap(
              TimedSimpleDoseElementDto::getDoseTime,
              d -> getTherapyDoseForSimpleTherapy(d.getDoseElement(), variableSimpleTherapy),
              (v1, v2) -> v2,
              LinkedHashMap::new));
    }
    else if (therapy instanceof VariableComplexTherapyDto)
    {
      final VariableComplexTherapyDto variableComplexTherapy = (VariableComplexTherapyDto)therapy;
      return variableComplexTherapy.getTimedDoseElements().stream()
          .collect(Collectors.toMap(
              TimedComplexDoseElementDto::getDoseTime,
              d -> getTherapyDoseForComplexTherapy(d.getDoseElement(), variableComplexTherapy),
              (v1, v2) -> v2,
              LinkedHashMap::new));
    }
    else
    {
      throw new IllegalArgumentException("Therapy type not supported");
    }
  }

  private TherapyDoseDto getTherapyDoseForSimpleTherapy(
      final SimpleDoseElementDto doseElement,
      final SimpleTherapyDto therapy)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    if (doseElement != null)
    {
      if (doseElement.getQuantity() != null)
      {
        therapyDoseDto.setNumerator(doseElement.getQuantity());
        therapyDoseDto.setNumeratorUnit(therapy.getQuantityUnit());
      }
      if (doseElement.getQuantityDenominator() != null)
      {
        therapyDoseDto.setDenominator(doseElement.getQuantityDenominator());
        therapyDoseDto.setDenominatorUnit(therapy.getQuantityDenominatorUnit());
      }
    }
    therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    return therapyDoseDto;
  }

  private TherapyDoseDto getTherapyDoseForComplexTherapy(
      final ComplexDoseElementDto doseElement,
      final ComplexTherapyDto complexTherapy)
  {
    final List<InfusionIngredientDto> ingredients = complexTherapy.getIngredientsList();
    final boolean infusionHasSpeedDefined = doseElement != null && doseElement.getRate() != null;
    final boolean isContinuousInfusion = complexTherapy.isContinuousInfusion();

    if (isContinuousInfusion) // rate
    {
      return getTherapyDoseForComplexTherapyWithRate(doseElement);
    }
    if (infusionHasSpeedDefined) // rate_volume_sum / rate_quantity
    {
      return getTherapyDoseForNonContinuousInfusionWithRate(doseElement);
    }
    if (ingredients.size() > 1) // volume_sum
    {
      return getTherapyDoseForComplexTherapyWithVolumeSum(complexTherapy);
    }
    if (!ingredients.isEmpty()) // quantity
    {
      return getTherapyDoseForComplexTherapyWithQuantity(complexTherapy.getIngredientsList().get(0));
    }

    return null;
  }

  private TherapyDoseDto getTherapyDoseForComplexTherapyWithQuantity(final InfusionIngredientDto infusionIngredient)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);

    if (infusionIngredient.getQuantity() != null)
    {
      therapyDoseDto.setNumerator(infusionIngredient.getQuantity());
      therapyDoseDto.setNumeratorUnit(infusionIngredient.getQuantityUnit());
    }
    if (infusionIngredient.getQuantityDenominator() != null)
    {
      if (infusionIngredient.getQuantity() == null)
      {
        therapyDoseDto.setNumerator(infusionIngredient.getQuantityDenominator());
        therapyDoseDto.setNumeratorUnit(infusionIngredient.getQuantityDenominatorUnit());
      }
      else
      {
        therapyDoseDto.setDenominator(infusionIngredient.getQuantityDenominator());
        therapyDoseDto.setDenominatorUnit(infusionIngredient.getQuantityDenominatorUnit());
      }
    }
    return therapyDoseDto;
  }

  private TherapyDoseDto getTherapyDoseForComplexTherapyWithVolumeSum(final ComplexTherapyDto complexTherapy)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.VOLUME_SUM);
    if (complexTherapy.getVolumeSum() != null)
    {
      therapyDoseDto.setNumerator(complexTherapy.getVolumeSum());
      therapyDoseDto.setNumeratorUnit(complexTherapy.getVolumeSumUnit());
    }
    return therapyDoseDto;
  }

  private TherapyDoseDto getTherapyDoseForComplexTherapyWithRate(final ComplexDoseElementDto doseElement)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    if (doseElement != null)
    {
      if (doseElement.getRate() != null)
      {
        therapyDoseDto.setNumerator(doseElement.getRate());
        therapyDoseDto.setNumeratorUnit(doseElement.getRateUnit());
      }
      if (doseElement.getRateFormula() != null)
      {
        therapyDoseDto.setDenominator(doseElement.getRateFormula());
        therapyDoseDto.setDenominatorUnit(doseElement.getRateFormulaUnit());
      }
      therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    }
    return therapyDoseDto;
  }

  private TherapyDoseDto getTherapyDoseForNonContinuousInfusionWithRate(final ComplexDoseElementDto doseElement)
  {
    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    if (doseElement != null)
    {
      final TherapyDoseDto rateDose = getTherapyDoseForComplexTherapyWithRate(doseElement);

      therapyDoseDto.setNumerator(rateDose.getNumerator());
      therapyDoseDto.setNumeratorUnit(rateDose.getNumeratorUnit());
      therapyDoseDto.setDenominator(rateDose.getDenominator());
      therapyDoseDto.setDenominatorUnit(rateDose.getDenominatorUnit());
    }
    return therapyDoseDto;
  }

  List<Interval> calculateAdministrationTasksInterval(
      final TherapyDto therapy,
      final DateTime actionTimestamp,
      final AdministrationTaskCreateActionEnum action,
      final int daysFromAction,
      final DateTime lastTaskTimestamp)
  {
    final DateTime start;
    if (action == AdministrationTaskCreateActionEnum.AUTO_CREATE)
    {
      if (lastTaskTimestamp != null)
      {
        start = lastTaskTimestamp;
      }
      else if (actionTimestamp.isAfter(therapy.getStart()))
      {
        start = actionTimestamp;
      }
      else
      {
        start = therapy.getStart();
      }
    }
    else
    {
      start = action.isCreateTasksFromTherapyStart() && therapy.getStart() != null ? therapy.getStart() : actionTimestamp;
    }

    DateTime end;
    //noinspection IfMayBeConditional
    if (action == AdministrationTaskCreateActionEnum.PREVIEW_TIMES_ON_NEW_PRESCRIPTION)
    {
      end = start.plusDays(daysFromAction);
    }
    else
    {
      end = actionTimestamp.plusDays(daysFromAction + 1).withTimeAtStartOfDay();
    }

    final boolean isPresetTimeAction =
        EnumSet.of(
            AdministrationTaskCreateActionEnum.PRESET_TIME_ON_NEW_PRESCRIPTION,
            AdministrationTaskCreateActionEnum.PRESET_TIME_ON_MODIFY).contains(action);
    final DateTime therapyEndTime = therapy.getEnd();
    if (!isPresetTimeAction && therapyEndTime != null && therapyEndTime.isBefore(end))
    {
      end = therapyEndTime;
    }

    return removeInactiveTherapyDaysFromTasksInterval(therapy, start, end);
  }

  private List<Interval> removeInactiveTherapyDaysFromTasksInterval(
      final TherapyDto therapy,
      final DateTime start,
      final DateTime end)
  {
    final DateTime therapyStart = therapy.getStart();
    final Integer daysFrequency = therapy.getDosingDaysFrequency();

    final List<Interval> intervals = new ArrayList<>();

    DateTime intervalStart = new DateTime(start);
    DateTime intervalEnd = intervalStart.withTimeAtStartOfDay().plusDays(1);

    final DateTime firstDayStart = intervalStart.withTimeAtStartOfDay();

    boolean validDayOfWeek = isInValidDaysOfWeek(intervalStart, therapy.getDaysOfWeek());
    boolean validFrequency = isInValidDaysFrequency(intervalStart, therapyStart, daysFrequency);
    boolean previousDayWasValid = validDayOfWeek && validFrequency;
    if (!previousDayWasValid)
    {
      intervalStart = firstDayStart.plusDays(1);
    }
    boolean validInFutureDays = false;
    while (intervalEnd.isBefore(end) || intervalEnd.equals(end))
    {
      validDayOfWeek = isInValidDaysOfWeek(intervalEnd, therapy.getDaysOfWeek());
      validFrequency = isInValidDaysFrequency(intervalEnd, therapyStart, daysFrequency);
      if (validDayOfWeek && validFrequency)
      {
        previousDayWasValid = true;
      }
      else
      {
        if (previousDayWasValid)
        {
          intervals.add(new Interval(intervalStart, intervalEnd));
        }
        previousDayWasValid = false;
        intervalStart = intervalEnd.plusDays(1);
      }
      intervalEnd = intervalEnd.plusDays(1);
      validInFutureDays = true;
    }
    if (previousDayWasValid && validInFutureDays || intervalEnd.minusDays(1).isBefore(end))
    {
      if (!intervalStart.isAfter(end))
      {
        intervals.add(new Interval(intervalStart, end));
      }
    }
    return intervals;
  }

  private boolean isInValidDaysOfWeek(final DateTime when, final List<String> daysOfWeek)
  {
    if (daysOfWeek == null || daysOfWeek.size() == 7 || daysOfWeek.isEmpty())
    {
      return true;
    }
    final DayOfWeek dayOfWeekEnum = MedicationsEhrUtils.dayOfWeekToEhrEnum(when.withTimeAtStartOfDay());

    return daysOfWeek.stream()
        .map(DayOfWeek::valueOf)
        .anyMatch(Predicate.isEqual(dayOfWeekEnum));
  }

  private boolean isInValidDaysFrequency(final DateTime start, final DateTime when, final Integer daysFrequency)
  {
    return daysFrequency == null ||
        Days.daysBetween(start.withTimeAtStartOfDay(), when.withTimeAtStartOfDay()).getDays() % daysFrequency == 0;
  }

  private Pair<DateTime, TherapyDoseDto> getNextAdministrationTimeWithDose(
      final AdministrationTaskCreateActionEnum action,
      final TherapyDto therapy,
      final DateTime fromTime,
      final Map<HourMinuteDto, TherapyDoseDto> administrationTimesWithDoses,
      final boolean fromTimeIncluded)
  {
    final boolean variable = therapy instanceof VariableTherapy;
    final DosingFrequencyDto dosingFrequency = therapy.getDosingFrequency();
    if (!variable && dosingFrequency != null && dosingFrequency.getType() == DosingFrequencyTypeEnum.BETWEEN_DOSES)
    {
      Preconditions.checkArgument(administrationTimesWithDoses.size() == 1);
      final HourMinuteDto hourMinute = administrationTimesWithDoses.keySet().iterator().next();
      final int hoursBetweenDoses = dosingFrequency.getValue();
      return getNextAdministrationTimeWithDoseForFrequencyBetweenDoses(
          action,
          therapy.getStart(),
          fromTime,
          hourMinute,
          administrationTimesWithDoses.get(hourMinute),
          hoursBetweenDoses,
          fromTimeIncluded);
    }
    return getNextAdministrationTimeWithDoseFromPattern(fromTime, administrationTimesWithDoses, fromTimeIncluded);
  }

  private Pair<DateTime, TherapyDoseDto> getNextAdministrationTimeWithDoseFromPattern(
      final DateTime fromTime,
      final Map<HourMinuteDto, TherapyDoseDto> administrationTimesWithDoses,
      final boolean fromTimeIncluded)
  {
    final List<HourMinuteDto> times = administrationTimesWithDoses.keySet()
        .stream()
        .sorted(Comparator.naturalOrder())
        .collect(Collectors.toList());

    int index = 0;
    DateTime foundTime = times.get(index).combine(fromTime);
    TherapyDoseDto therapyDoseDto = administrationTimesWithDoses.get(times.get(0));

    while (foundTime.isBefore(fromTime) || (foundTime.equals(fromTime) && !fromTimeIncluded))
    {
      index++;
      if (index >= administrationTimesWithDoses.size())
      {
        index = 0;
        foundTime = foundTime.plusDays(1);
      }
      final HourMinuteDto hourMinute = times.get(index);
      foundTime = hourMinute.combine(foundTime);
      therapyDoseDto = administrationTimesWithDoses.get(hourMinute);
    }
    return Pair.of(foundTime, therapyDoseDto);
  }

  private Pair<DateTime, TherapyDoseDto> getNextAdministrationTimeWithDoseForFrequencyBetweenDoses(
      final AdministrationTaskCreateActionEnum action,
      final DateTime therapyStart,
      final DateTime fromTime,
      final HourMinuteDto administrationTime,
      final TherapyDoseDto dose,
      final int hoursBetweenDoses,
      final boolean fromTimeIncluded)
  {
    DateTime foundTime;
    if (action == AdministrationTaskCreateActionEnum.PRESET_TIME_ON_NEW_PRESCRIPTION)
    {
      foundTime = fromTime.withTimeAtStartOfDay()
          .plusHours(administrationTime.getHour()).plusMinutes(administrationTime.getMinute());
      if (hoursBetweenDoses >= 24)
      {
        while (foundTime.isBefore(fromTime) || (foundTime.equals(fromTime) && !fromTimeIncluded))
        {
          foundTime = foundTime.plusDays(1);
        }
        return Pair.of(foundTime, dose);
      }
    }
    else
    {
      foundTime = therapyStart;
    }
    while (foundTime.isBefore(fromTime) || (foundTime.equals(fromTime) && !fromTimeIncluded))
    {
      foundTime = foundTime.plusHours(hoursBetweenDoses);
    }
    return Pair.of(foundTime, dose);
  }

  private Pair<DateTime, TherapyDoseDto> getPreviousAdministrationTimeWithDose(
      final DateTime fromTime,
      final Map<HourMinuteDto, TherapyDoseDto> administrationTimesWithDoses,
      final boolean fromTimeIncluded)
  {
    int index = administrationTimesWithDoses.size() - 1;
    final List<HourMinuteDto> times = new ArrayList<>(administrationTimesWithDoses.keySet());
    Collections.sort(times);
    DateTime foundTime = times.get(index).combine(fromTime.plusDays(1));
    TherapyDoseDto therapyDoseDto = administrationTimesWithDoses.get(times.get(index));
    while (foundTime.isAfter(fromTime) || (foundTime.equals(fromTime) && !fromTimeIncluded))
    {
      index--;
      if (index < 0)
      {
        index = administrationTimesWithDoses.size() - 1;
        foundTime = foundTime.minusDays(1);
      }
      final HourMinuteDto hourMinute = times.get(index);
      foundTime = hourMinute.combine(foundTime);
      therapyDoseDto = administrationTimesWithDoses.get(hourMinute);
    }
    return Pair.of(foundTime, therapyDoseDto);
  }

  @Override
  public NewTaskRequestDto createMedicationTaskRequestWithGroupUUId(
      final String patientId,
      final String groupUUId,
      @Nonnull final TherapyDto therapy,
      @Nonnull final AdministrationTypeEnum administrationType,
      @Nonnull final DateTime timestamp,
      final TherapyDoseDto dose)
  {
    Preconditions.checkNotNull(therapy, "therapy");
    Preconditions.checkNotNull(administrationType, "administrationType");
    Preconditions.checkNotNull(timestamp, "timestamp");

    final NewTaskRequestDto request = createMedicationTaskRequest(patientId, therapy, administrationType, timestamp, dose);
    request.getVariables().add(Pair.of(AdministrationTaskDef.GROUP_UUID, groupUUId));
    return request;
  }

  @Override
  public NewTaskRequestDto createMedicationTaskRequest(
      final String patientId,
      @Nonnull final TherapyDto therapy,
      @Nonnull final AdministrationTypeEnum administrationType,
      @Nonnull final DateTime timestamp,
      final TherapyDoseDto dose)
  {
    Preconditions.checkNotNull(therapy, "therapy is required");
    Preconditions.checkNotNull(administrationType, "administrationType is required");
    Preconditions.checkNotNull(timestamp, "timestamp is required");

    final String therapyId =
        therapy.getCompositionUid() != null ?
        TherapyIdUtils.createTherapyId(therapy.getCompositionUid(), therapy.getEhrOrderName()) :
        null;

    final String medicine = getMedicineString(therapy);

    final List<Pair<TaskVariable, Object>> variablesList = createAdministrationVariablesForTaskRequest(
        patientId,
        therapyId,
        administrationType,
        therapy,
        dose);

    return new NewTaskRequestDto(
        AdministrationTaskDef.INSTANCE,
        AdministrationTaskDef.getTaskTypeEnum().buildKey(patientId),
        medicine,
        medicine,
        TherapyAssigneeEnum.NURSE.name(),
        timestamp,
        null,
        variablesList.toArray(new Pair[0]));
  }

  @Override
  public List<NewTaskRequestDto> createTaskRequestsForAdditionalAdministration(
      final String patientId,
      @Nonnull final TherapyDto therapy,
      @Nonnull final AdministrationTypeEnum type,
      @Nonnull final DateTime timestamp,
      final TherapyDoseDto dose)
  {
    Preconditions.checkNotNull(therapy, "therapy");
    Preconditions.checkNotNull(type, "type");
    Preconditions.checkNotNull(timestamp, "timestamp");

    //noinspection IfMayBeConditional
    if (therapy.isNormalInfusion() && type == AdministrationTypeEnum.START)
    {
      return createRequestsForAdditionalRateAdministration(patientId, therapy, timestamp, dose, null, true);
    }
    else
    {
      return Collections.singletonList(createMedicationTaskRequest(patientId, therapy, type, timestamp, dose));
    }
  }

  @Override
  public List<NewTaskRequestDto> createRequestsForAdditionalRateAdministration(
      final String patientId,
      @Nonnull final TherapyDto therapy,
      @Nonnull final DateTime timestamp,
      @Nonnull final TherapyDoseDto dose,
      final String groupUUId,
      final boolean createStart)
  {
    Preconditions.checkNotNull(therapy, "therapy");
    Preconditions.checkNotNull(timestamp, "timestamp");
    Preconditions.checkNotNull(dose, "dose");

    final int duration = administrationUtils.calculateDurationForRateQuantityDose(dose);
    dose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);

    final String uuId = Opt
        .of(groupUUId)
        .orElseGet(() -> administrationUtils.generateGroupUUId(DateTime.now()));

    final List<NewTaskRequestDto> requests = new ArrayList<>();
    if (createStart)
    {
      requests.add(createMedicationTaskRequestWithGroupUUId(
          patientId,
          uuId,
          therapy,
          AdministrationTypeEnum.START,
          timestamp,
          dose));
    }
    requests.add(createMedicationTaskRequestWithGroupUUId(
        patientId,
        uuId,
        therapy,
        AdministrationTypeEnum.STOP,
        timestamp.plusMinutes(duration),
        null));

    return requests;
  }

  @Override
  public NewTaskRequestDto createTherapyEndTaskRequest(
      final String patientId,
      @Nonnull final TherapyDto therapy,
      @Nonnull final DateTime timestamp)
  {
    final NewTaskRequestDto request = createMedicationTaskRequest(
        patientId,
        therapy,
        AdministrationTypeEnum.STOP,
        timestamp,
        null);

    request.getVariables().add(Pair.of(AdministrationTaskDef.THERAPY_END, true));
    return request;
  }

  private String getMedicineString(final TherapyDto therapy)
  {
    final String medicine;
    if (therapy instanceof SimpleTherapyDto)
    {
      medicine = ((SimpleTherapyDto)therapy).getMedication().getDisplayName();
    }
    else if (therapy instanceof ComplexTherapyDto || therapy instanceof OxygenTherapyDto)
    {
      medicine = therapy.getTherapyDescription();
    }
    else
    {
      throw new IllegalArgumentException("Therapy type not supported");
    }

    return medicine != null && medicine.length() > 255 ? medicine.substring(0, 250) + "..." : medicine;
  }

  private List<Pair<TaskVariable, Object>> createAdministrationVariablesForTaskRequest(
      final String patientId,
      final String therapyId,
      final AdministrationTypeEnum administrationTypeEnum,
      final TherapyDto therapyDto,
      final TherapyDoseDto dose)
  {
    final List<Pair<TaskVariable, Object>> variablesList = new ArrayList<>();

    variablesList.add(Pair.of(MedsTaskDef.PATIENT_ID, patientId));
    variablesList.add(Pair.of(AdministrationTaskDef.THERAPY_ID, therapyId));
    variablesList.add(Pair.of(AdministrationTaskDef.ADMINISTRATION_TYPE, administrationTypeEnum.name()));

    if (dose != null)
    {
      variablesList.addAll(getDoseTaskVariables(dose));
    }

    if (therapyDto instanceof OxygenTherapyDto)
    {
      variablesList.addAll(getOxygenTaskVariables((OxygenTherapyDto)therapyDto));
    }

    return variablesList;
  }

  private List<Pair<TaskVariable, Object>> getOxygenTaskVariables(final OxygenTherapyDto therapy)
  {
    final List<Pair<TaskVariable, Object>> variables = new ArrayList<>();

    variables.add(Pair.of(OxygenTaskDef.OXYGEN_ADMINISTRATION, true));

    final OxygenStartingDevice startingDevice = therapy.getStartingDevice();
    if (startingDevice != null)
    {
      variables.add(Pair.of(OxygenTaskDef.STARTING_DEVICE_ROUTE, startingDevice.getRoute().name()));
      if (startingDevice.getRouteType() != null)
      {
        variables.add(Pair.of(OxygenTaskDef.STARTING_DEVICE_TYPE, startingDevice.getRouteType()));
      }
    }

    return variables;
  }

  @Override
  public List<Pair<TaskVariable, Object>> getDoseTaskVariables(@Nonnull final TherapyDoseDto dose)
  {
    Preconditions.checkNotNull(dose, "dose is required");
    final String therapyDoseTypeEnum = dose.getTherapyDoseTypeEnum() != null ? dose.getTherapyDoseTypeEnum().name() : null;

    final List<Pair<TaskVariable, Object>> variablesList = new ArrayList<>();
    addVariableIfNotNull(variablesList, AdministrationTaskDef.DOSE_TYPE, therapyDoseTypeEnum);
    addVariableIfNotNull(variablesList, AdministrationTaskDef.DOSE_NUMERATOR, dose.getNumerator());
    addVariableIfNotNull(variablesList, AdministrationTaskDef.DOSE_NUMERATOR_UNIT, dose.getNumeratorUnit());
    addVariableIfNotNull(variablesList, AdministrationTaskDef.DOSE_DENOMINATOR, dose.getDenominator());
    addVariableIfNotNull(variablesList, AdministrationTaskDef.DOSE_DENOMINATOR_UNIT, dose.getDenominatorUnit());

    return variablesList;
  }

  private void addVariableIfNotNull(
      final List<Pair<TaskVariable, Object>> variablesList,
      final TaskVariable taskVariable,
      final Object value)
  {
    if (value != null)
    {
      variablesList.add(Pair.of(taskVariable, value));
    }
  }

  @Override
  public NewTaskRequestDto createTaskRequestFromTaskDto(@Nonnull final TaskDto taskDto)
  {
    Preconditions.checkNotNull(taskDto, "taskDto is required");

    final NewTaskRequestDto newTaskRequestDto = new NewTaskRequestDto(
        AdministrationTaskDef.INSTANCE,
        taskDto.getName(),
        taskDto.getDisplayName(),
        taskDto.getDescription(),
        taskDto.getAssignee(),
        taskDto.getDueTime(),
        taskDto.getAssociatedEntity());

    taskDto.getVariables()
        .keySet()
        .forEach(key -> newTaskRequestDto.addVariables(Pair.of(TaskVariable.named(key), taskDto.getVariables().get(key))));

    return newTaskRequestDto;
  }

  @Override
  public List<AdministrationDto> calculateTherapyAdministrationTimes(
      @Nonnull final TherapyDto therapy,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(therapy, "therapy is required");
    Preconditions.checkNotNull(when, "when is required");

    //calculates administration times for 24h from first administration
    final List<NewTaskRequestDto> tasks = calculateTherapyAdministrationTimes(
        therapy,
        AdministrationTaskCreateActionEnum.PREVIEW_TIMES_ON_NEW_PRESCRIPTION,
        when);

    final Optional<DateTime> firstTaskTime = tasks.stream()
        .map(NewTaskRequestDto::getDue)
        .min(Comparator.naturalOrder());

    return tasks
        .stream()
        .filter(t -> !firstTaskTime.isPresent() || Hours.hoursBetween(firstTaskTime.get(), t.getDue()).getHours() < 24)
        .map(t -> administrationTaskConverter.convertNewTaskRequestDtoToAdministrationDto(t, when))
        .collect(Collectors.toList());
  }

  @Override
  public DateTime calculateNextTherapyAdministrationTime(
      @Nonnull final TherapyDto therapy,
      final boolean presetTimeOnNewPrescription,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(therapy, "therapy is required");
    Preconditions.checkNotNull(when, "when is required");

    final AdministrationTaskCreateActionEnum action =
        presetTimeOnNewPrescription
        ? AdministrationTaskCreateActionEnum.PRESET_TIME_ON_NEW_PRESCRIPTION
        : AdministrationTaskCreateActionEnum.PRESET_TIME_ON_MODIFY;

    final List<NewTaskRequestDto> tasks = calculateTherapyAdministrationTimes(therapy, action, when);
    return tasks.isEmpty() ? null : tasks.get(0).getDue();
  }

  private List<NewTaskRequestDto> calculateTherapyAdministrationTimes(
      final TherapyDto therapy,
      final AdministrationTaskCreateActionEnum action,
      final DateTime when)
  {
    return createTaskRequests(null, therapy, action, when, null)
        .stream()
        .sorted((o1, o2) -> o1.getDue().compareTo(o2.getDue()))
        .collect(Collectors.toList());
  }
}