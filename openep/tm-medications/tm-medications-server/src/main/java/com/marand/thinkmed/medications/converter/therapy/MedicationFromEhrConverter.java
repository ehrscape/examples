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

package com.marand.thinkmed.medications.converter.therapy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.primitives.Ints;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.TherapyCommentEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.IndicationDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;

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
      final String ehrOrderName,
      final MedicationDataProvider medicationDataProvider)
  {
    therapy.setCompositionUid(compositionId);
    therapy.setEhrOrderName(ehrOrderName);
    therapy.setCreatedTimestamp(createdTimestamp);

    final AdministrationDetailsCluster administrationDetailsCluster = orderActivity.getAdministrationDetails();
    if (administrationDetailsCluster != null && administrationDetailsCluster.getRoute() != null)
    {
      therapy.setRoutes
          (administrationDetailsCluster.getRoute()
               .stream()
               .map(dv -> medicationDataProvider.getMedicationRoute(Long.valueOf(dv.getDefiningCode().getCodeString())))
               .collect(Collectors.toList()));
    }

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
      final DvText indication = orderActivity.getClinicalIndication().iterator().next();
      if (indication != null)
      {
        if (indication instanceof DvCodedText)
        {
          final DvCodedText codedIndication = (DvCodedText)indication;
          therapy.setClinicalIndication(
              new IndicationDto(codedIndication.getDefiningCode().getCodeString(), codedIndication.getValue()));
        }
        else
        {
          therapy.setClinicalIndication(new IndicationDto(null, indication.getValue()));
        }
      }
      else
      {
        therapy.setClinicalIndication(null);
      }
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
          therapy.setStartCriterion(startCriterionEnum.name());
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

  protected final List<HourMinuteDto> getAdministrationTimes(final OrderActivity order)
  {
    final TimingCluster timing = order.getMedicationTiming().getTiming();
    final List<HourMinuteDto> administrationTimes = new ArrayList<>();
    if (timing != null)
    {
      administrationTimes.addAll(
          timing.getTime()
              .stream()
              .map(MedicationsEhrUtils::getHourMinute)
              .collect(Collectors.toList()));
    }
    return administrationTimes;
  }

  protected final void fillSelfAdministerDataFromEhr(
      final MedicationInstructionInstruction instruction,
      final TherapyDto therapy)
  {
    //TODO Nejc save somewhere else in new template
    if (!instruction.getOrder().isEmpty() && instruction.getOrder().get(0).getParsableDoseDescription() != null)
    {
      final String dateTimeJson = instruction.getOrder().get(0).getParsableDoseDescription().getFormalism();
      final SelfAdministeringActionEnum selfAdministeringActionEnum = SelfAdministeringActionEnum.valueOf(
          instruction.getOrder()
              .get(0)
              .getParsableDoseDescription()
              .getValue());

      therapy.setSelfAdministeringActionEnum(selfAdministeringActionEnum);
      therapy.setSelfAdministeringLastChange(JsonUtil.fromJson(dateTimeJson, DateTime.class));
    }
  }

  public void fillBnfFromEhr(final MedicationInstructionInstruction instruction, final TherapyDto result)
  {
    if (instruction.getConcessionBenefit() != null)
    {
      //TODO Nejc save somewhere else in new template
      result.setBnfMaximumPercentage(Integer.valueOf(DvUtils.getString(instruction.getConcessionBenefit())));
    }
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

  protected MedicationDto buildMedicationFromOrderForSimpleTherapy(
      final OrderActivity representingOrderActivity,
      final MedicationDataProvider medicationDataProvider)
  {
    if (representingOrderActivity.getMedicine() instanceof DvCodedText)   //if DvCodedText then medication exists in database
    {
      final String definingCode = ((DvCodedText)representingOrderActivity.getMedicine()).getDefiningCode().getCodeString();
      final Long medicationId = Long.parseLong(definingCode);
      return medicationDataProvider.getMedication(medicationId);
    }
    else
    {
      final MedicationDto medication = new MedicationDto();
      medication.setName(representingOrderActivity.getMedicine().getValue());
      return medication;
    }
  }

  protected String getApplicationPreconditionFromOrderForSimpleTherapy(final OrderActivity representingOrderActivity)
  {
    for (final DvText additionalInstruction : representingOrderActivity.getAdditionalInstruction())
    {
      if (additionalInstruction instanceof DvCodedText)
      {
        final String additionalInstructionEnumString =
            ((DvCodedText)additionalInstruction).getDefiningCode().getCodeString();
        final MedicationAdditionalInstructionEnum additionalInstructionEnum =
            MedicationAdditionalInstructionEnum.getByFullString(additionalInstructionEnumString);
        if (additionalInstructionEnum != null)
        {
          if (MedicationAdditionalInstructionEnum.APPLICATION_PRECONDITION.contains(additionalInstructionEnum))
          {
           return additionalInstructionEnum.name();
          }
        }
      }
      else
      {
        throw new IllegalArgumentException(
            "Additional instruction " + additionalInstruction.getValue() + " is not DvCodedText");
      }
    }
    return null;
  }

  public interface MedicationDataProvider
  {
    MedicationDto getMedication(Long medicationId);

    DoseFormDto getDoseForm(String code, DateTime when);

    DoseFormDto getMedicationDoseForm(long medicationId);

    MedicationRouteDto getMedicationRoute(long routeId);
  }
}
