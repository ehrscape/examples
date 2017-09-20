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

import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster.AmbientOxygenCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster.DeviceCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster.HumidifierCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.TherapyCommentEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyUnitsConverter;
import com.marand.thinkmed.medications.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvQuantity;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("Duplicates")
public class OxygenMedicationToEhrConverter extends MedicationToEhrConverter<OxygenTherapyDto>
{
  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof OxygenTherapyDto;
  }

  @Override
  public void fillInstructionFromTherapy(final MedicationInstructionInstruction instruction, final OxygenTherapyDto therapy)
  {
    instruction.setNarrative(DataValueUtils.getText(therapy.getTherapyDescription()));
    instruction.getOrder().clear();
    fillDoseElements(therapy, instruction);

    fillBnfToEhr(instruction, therapy);
    fillSelfAdministrationDataToEhr(instruction, therapy);
  }

  private void fillDoseElements(final OxygenTherapyDto therapy, final MedicationInstructionInstruction instruction)
  {
    fillOrderActivityFromTherapyDto(
        MedicationsEhrUtils.createEmptyOrderActivityFor(instruction),
        therapy,
        therapy.getStart());
  }

  private void fillOrderActivityFromTherapyDto(
      final MedicationInstructionInstruction.OrderActivity orderActivity,
      final OxygenTherapyDto therapyDto,
      final DateTime administrationDate)
  {
    MedicationsEhrUtils.setMedicineDescription(
        orderActivity,
        therapyDto.getTherapyDescription(),
        therapyDto.getMedication().getName(),
        therapyDto.getMedication().getId());

    MedicationsEhrUtils.setMedicationTiming(
        orderActivity,
        therapyDto.getDosingFrequency(),
        therapyDto.getDosingDaysFrequency(),
        therapyDto.getDaysOfWeek(),
        therapyDto.getStart(),
        therapyDto.getEnd(),
        null,
        administrationDate,
        therapyDto.getWhenNeeded(),
        therapyDto.getStartCriterion(),
        therapyDto.getMaxDailyFrequency());

    orderActivity.getComment().add(DataValueUtils.getText(therapyDto.getComment()));
    for (final String warning : therapyDto.getCriticalWarnings())
    {
      final String warningCommentPrefix = TherapyCommentEnum.getFullString(TherapyCommentEnum.WARNING) + " ";
      orderActivity.getComment().add(DataValueUtils.getText(warningCommentPrefix + warning));
    }

    MedicationsEhrUtils.setClinicalIndication(orderActivity, therapyDto.getClinicalIndication());

    if (therapyDto.getPastDaysOfTherapy() != null)
    {
      orderActivity.setPastDaysOfTherapy(new DvCount());
      orderActivity.getPastDaysOfTherapy().setMagnitude(therapyDto.getPastDaysOfTherapy());
    }

    MedicationsEhrUtils.setApplicationPrecondition(orderActivity, therapyDto.getApplicationPrecondition());

    final AdministrationDetailsCluster administrationDetails = MedicationsEhrUtils.createAdministrationDetailsFor(
        orderActivity);

    MedicationsEhrUtils.setRoutes(administrationDetails, therapyDto.getRoutes());

    setOxygenDeliveryData(administrationDetails, therapyDto);

    MedicationsEhrUtils.setMedicationForm(MedicationsEhrUtils.createIngredientsAndFormFor(orderActivity), null);
  }

  private void setOxygenDeliveryData(final AdministrationDetailsCluster details, final OxygenTherapyDto therapy)
  {
    final OxygenDeliveryCluster oxygenDelivery = new OxygenDeliveryCluster();
    final AmbientOxygenCluster ambientOxygenCluster = new AmbientOxygenCluster();
    oxygenDelivery.setAmbientOxygen(ambientOxygenCluster);
    details.getOxygenDelivery().add(oxygenDelivery);

    setFlowRate(therapy, oxygenDelivery, ambientOxygenCluster);
    setSaturation(therapy, ambientOxygenCluster);
    setDevice(therapy, oxygenDelivery);
    setHumidification(therapy, oxygenDelivery);
  }

  private void setHumidification(
      final OxygenTherapyDto therapyDto,
      final OxygenDeliveryCluster oxygenDelivery)
  {
    final HumidifierCluster humidifierCluster = new HumidifierCluster();
    humidifierCluster.setHumidiferUsed(DataValueUtils.getBoolean(therapyDto.isHumidification()));
    oxygenDelivery.setHumidifier(humidifierCluster);
  }

  private void setDevice(final OxygenTherapyDto therapy, final OxygenDeliveryCluster oxygenDelivery)
  {
    if (therapy.getStartingDevice() != null)
    {
      oxygenDelivery.setRouteEnum(therapy.getStartingDevice().getRoute());
      if (therapy.getStartingDevice().getRouteType() != null)
      {
        final DeviceCluster deviceCluster = new DeviceCluster();
        deviceCluster.setDeviceName(
            DataValueUtils.getText(therapy.getStartingDevice().getRoute().getTerm().getDescription()));
        deviceCluster.setType(DataValueUtils.getText(String.valueOf(therapy.getStartingDevice().getRouteType())));
        oxygenDelivery.getDevice().add(deviceCluster);
      }
    }
  }

  private void setSaturation(final OxygenTherapyDto therapy, final AmbientOxygenCluster ambientOxygenCluster)
  {
    if (therapy.getMaxTargetSaturation() != null)
    {
      ambientOxygenCluster.setMaximumPercentO2(DataValueUtils.getPercentProportion(therapy.getMaxTargetSaturation().floatValue()));
    }
    if (therapy.getMinTargetSaturation() != null)
    {
      ambientOxygenCluster.setMinimumPercentO2(DataValueUtils.getPercentProportion(therapy.getMinTargetSaturation().floatValue()));
    }
  }

  private void setFlowRate(
      final OxygenTherapyDto therapy,
      final OxygenDeliveryCluster oxygenDelivery,
      final AmbientOxygenCluster ambientOxygen)
  {
    if (therapy.getFlowRateMode() != null)
    {
      oxygenDelivery.setFlowRateModeEnum(therapy.getFlowRateMode());
    }
    if (therapy.getFlowRate() != null)
    {
      ambientOxygen.setOxygenFlowRate(getOxygenFlowRate(therapy.getFlowRate(), therapy.getFlowRateUnit()));
    }
  }

  public static DvQuantity getOxygenFlowRate(final double flowRate, @Nonnull final String unit)
  {
    final String[] unitParts = Pattern.compile("/").split(unit);

    final String massUnit = unitParts[0].toLowerCase();
    final String timeUnits = unitParts[1].toLowerCase();

    final Double liters = TherapyUnitsConverter.convertToUnit(flowRate, massUnit, "l");
    final Double timeRatio = TherapyUnitsConverter.convertToUnit(1.0, "m", timeUnits);

    if (liters == null || timeRatio == null)
    {
      throw new IllegalArgumentException("Invalid units for oxygen flow rate: " + massUnit + ", " + timeUnits);
    }

    return DataValueUtils.getQuantity(liters * timeRatio, "l/m", 2);
  }
}
