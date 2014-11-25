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

package com.marand.thinkmed.medications.converter;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Ints;
import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.TherapyCommentEnum;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster;

/**
 * @author Bostjan Vester
 */
public abstract class MedicationFromEhrConverter<M extends TherapyDto>
{
  public abstract boolean isFor(final MedicationInstructionInstruction instruction);

  public abstract M createTherapyFromInstruction(
      MedicationInstructionInstruction instruction,
      String compositionId,
      String ehrOrderName,
      DateTime createdTimestamp,
      DateTime when,
      MedicationDataProvider medicationDataProvider);

  protected abstract M createEmptyTherapyDto();

  protected final void fillTherapyDto(
      final TherapyDto therapy,
      final OrderActivity orderActivity,
      final DateTime createdTimestamp,
      final String compositionId,
      final String ehrOrderName)
  {
    therapy.setCompositionUid(compositionId);
    therapy.setEhrOrderName(ehrOrderName);
    therapy.setCreatedTimestamp(createdTimestamp);

    final MedicationRouteDto routeDto = new MedicationRouteDto();
    final AdministrationDetailsCluster administrationDetailsCluster = orderActivity.getAdministrationDetails();
    routeDto.setCode(administrationDetailsCluster.getRoute().get(0).getDefiningCode().getCodeString());
    routeDto.setName(administrationDetailsCluster.getRoute().get(0).getValue());
    therapy.setRoute(routeDto);
    therapy.setPastDaysOfTherapy(
        orderActivity.getPastDaysOfTherapy() == null ? null : (int)orderActivity.getPastDaysOfTherapy().getMagnitude());
    if (orderActivity.getDirections() != null)
    {
      therapy.setTherapyDescription(orderActivity.getDirections().getValue());
    }
    final MedicationTimingCluster medicationTiming = orderActivity.getMedicationTiming();
    final TimingCluster timing = medicationTiming.getTiming();
    final DosingFrequencyDto dosingFrequency = getDosingFrequency(medicationTiming);
    therapy.setDosingFrequency(dosingFrequency);

    //Days frequency (every X-th day)
    if (timing != null &&
        (therapy.getDosingFrequency() == null ||
            therapy.getDosingFrequency().getType() != DosingFrequencyTypeEnum.BETWEEN_DOSES) &&
        timing.getInterval() != null)
    {
      final int daysFrequency = DataValueUtils.getPeriod(timing.getInterval()).getDays();
      if (daysFrequency == 0)
      {
        throw new IllegalArgumentException("Composition not saved correctly - days frequency 0");
      }
      therapy.setDosingDaysFrequency(daysFrequency);
    }
    therapy.setStart(DataValueUtils.getDateTime(medicationTiming.getStartDate()));
    if (medicationTiming.getStopDate() != null)
    {
      therapy.setEnd(DataValueUtils.getDateTime(medicationTiming.getStopDate()));
    }
    if (timing != null && !timing.getDayOfWeek().isEmpty())
    {
      final List<DvCodedText> ehrDaysOfWeek = timing.getDayOfWeek();
      final List<String> daysOfWeek = new ArrayList<>();
      for (final DvCodedText ehrDayOfWeek : ehrDaysOfWeek)
      {
        final TimingCluster.DayOfWeek dayOfWeekEnum =
            DataValueUtils.getTerminologyEnum(TimingCluster.DayOfWeek.class, ehrDayOfWeek);
        daysOfWeek.add(dayOfWeekEnum.name());
      }
      therapy.setDaysOfWeek(daysOfWeek);
    }
    if (!orderActivity.getComment().isEmpty())
    {
      for (final DvText commentDvText : orderActivity.getComment())
      {
        String comment = commentDvText.getValue();
        if (comment.startsWith(TherapyCommentEnum.getFullString(TherapyCommentEnum.WARNING)))
        {
          comment = comment.replaceAll(TherapyCommentEnum.getFullString(TherapyCommentEnum.WARNING) + " ", "");
          therapy.getCriticalWarnings().add(comment);
        }
        else
        {
          therapy.setComment(comment);
        }
      }
    }
    if (!orderActivity.getClinicalIndication().isEmpty())
    {
      therapy.setClinicalIndication(orderActivity.getClinicalIndication().iterator().next().getValue());
    }

    if (medicationTiming.getPRN() != null)
    {
      therapy.setWhenNeeded(medicationTiming.getPRN().isValue());
    }
    if (!medicationTiming.getStartCriterion().isEmpty())
    {
      if (medicationTiming.getStartCriterion().get(0) instanceof DvCodedText)
      {
        final String startConditionEnumString =
            ((DvCodedText)medicationTiming.getStartCriterion().get(0)).getDefiningCode().getCodeString();
        final MedicationStartCriterionEnum startCriterionEnum = MedicationStartCriterionEnum.getByFullString(
            startConditionEnumString);
        if (startCriterionEnum != null)
        {
          therapy.getStartCriterions().add(startCriterionEnum.name());
        }
        else
        {
          throw new IllegalArgumentException("Unknown start condition " + startConditionEnumString);
        }
      }
    }
    if (!orderActivity.getMaximumDose().isEmpty() && orderActivity.getMaximumDose().get(0).getMedicationAmount() != null)
    {
      final double magnitude =
          ((DvQuantity)orderActivity.getMaximumDose().get(0).getMedicationAmount().getAmount()).getMagnitude();
      therapy.setMaxDailyFrequency(((int)magnitude));
    }
  }

  protected final HourMinuteDto getAdministrationTime(final OrderActivity order)
  {
    final TimingCluster timing = order.getMedicationTiming().getTiming();
    if (timing != null && !timing.getTime().isEmpty())
    {
      final Pair<Integer, Integer> parsedTime = DvUtils.getHourMinute(timing.getTime().get(0));
      return new HourMinuteDto(parsedTime.getFirst(), parsedTime.getSecond());
    }
    return null;
  }

  protected final DateTime getAdministrationDate(final OrderActivity order)
  {
    final TimingCluster timing = order.getMedicationTiming().getTiming();
    if (timing != null && !timing.getDate().isEmpty())
    {
      return ISODateTimeFormat.date().parseDateTime(timing.getDate().get(0).getValue());
    }
    return null;
  }

  public static DosingFrequencyDto getDosingFrequency(final MedicationTimingCluster medicationTiming)
  {
    final TimingCluster timing = medicationTiming.getTiming();
    if (timing != null && timing.getInterval() != null && DataValueUtils.getPeriod(timing.getInterval()).getHours() > 0)
    {
      final int hoursBetweenDoses = DataValueUtils.getPeriod(timing.getInterval()).getHours();
      return new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, hoursBetweenDoses);
    }
    if (timing != null && timing.getDailyCount() != null)
    {
      final Integer dailyCount = Ints.checkedCast(timing.getDailyCount().getMagnitude());
      return new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, dailyCount);
    }
    if (medicationTiming.getTimingDescription() != null &&
        medicationTiming.getTimingDescription().getValue().equals(
            DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.ONCE_THEN_EX)))
    {
      return new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX);
    }
    if (medicationTiming.getTimingDescription() != null &&
        medicationTiming.getTimingDescription().getValue().equals(
            DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.MORNING)))
    {
      return new DosingFrequencyDto(DosingFrequencyTypeEnum.MORNING);
    }
    if (medicationTiming.getTimingDescription() != null &&
        medicationTiming.getTimingDescription().getValue().equals(
            DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.NOON)))
    {
      return new DosingFrequencyDto(DosingFrequencyTypeEnum.NOON);
    }
    if (medicationTiming.getTimingDescription() != null &&
        medicationTiming.getTimingDescription().getValue().equals(
            DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.EVENING)))
    {
      return new DosingFrequencyDto(DosingFrequencyTypeEnum.EVENING);
    }
    return null;
  }

  public interface MedicationDataProvider
  {
    MedicationDto getMedication(Long medicationId, DateTime when);

    DoseFormDto getDoseForm(String code, DateTime when);
  }
}
