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

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyUnitsConverter;
import com.marand.thinkmed.medications.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.dto.OxygenTherapyDto;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvProportion;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Nejc Korasa
 */
public class OxygenMedicationFromEhrConverter extends MedicationFromEhrConverter<OxygenTherapyDto>
{

  @Override
  public boolean isFor(final MedicationInstructionInstruction instruction)
  {
    return MedicationsEhrUtils.isOxygenInstruction(instruction);
  }

  @Override
  public OxygenTherapyDto createTherapyFromInstruction(
      final MedicationInstructionInstruction instruction,
      final String compositionId,
      final String ehrOrderName,
      final DateTime createdTimestamp,
      final DateTime when,
      final MedicationDataProvider medicationDataProvider)
  {
    final OxygenTherapyDto result = createEmptyTherapyDto();

    fillBnfFromEhr(instruction, result);

    final OrderActivity representingOrderActivity = MedicationsEhrUtils.getRepresentingOrderActivity(instruction);

    fillTherapyDto(result, representingOrderActivity, createdTimestamp, compositionId, ehrOrderName, medicationDataProvider);
    result.setLinkedToAdmission(
        !MedicationsEhrUtils.getLinksOfType(
            instruction,
            EhrLinkType.MEDICATION_ON_ADMISSION)
            .isEmpty());

    result.setMedication(buildMedicationFromOrderForSimpleTherapy(representingOrderActivity, medicationDataProvider));
    result.setCompositionUid(compositionId);
    result.setEhrOrderName(ehrOrderName);

    fillOxygenData(result, instruction.getOrder());

    result.setApplicationPrecondition(getApplicationPreconditionFromOrderForSimpleTherapy(representingOrderActivity));

    fillSelfAdministerDataFromEhr(instruction, result);

    result.setDoseType(TherapyDoseTypeEnum.RATE);
    return result;
  }

  private void fillOxygenData(final OxygenTherapyDto result, final List<OrderActivity> orderActivities)
  {
    if (orderActivities.size() != 1)
    {
      throw new IllegalArgumentException("Expected exactly one order activity, got " + orderActivities.size() + '!');
    }

    final AdministrationDetailsCluster administrationDetails = orderActivities.get(0).getAdministrationDetails();
    if (administrationDetails == null)
    {
      throw new IllegalArgumentException("Expected administration detail!");
    }
    if (administrationDetails.getOxygenDelivery().size() != 1)
    {
      throw new IllegalArgumentException(
          "Expected exactly one oxygen delivery, got " + administrationDetails.getOxygenDelivery().size() + '!');
    }

    final OxygenDeliveryCluster oxygenDeliveryCluster = administrationDetails.getOxygenDelivery().get(0);

    setSaturation(result, Opt.of(oxygenDeliveryCluster));
    setFlowRate(result, Opt.of(oxygenDeliveryCluster));
    setOxygenDevice(result, Opt.of(oxygenDeliveryCluster));

    Optional.ofNullable(oxygenDeliveryCluster)
        .map(OxygenDeliveryCluster::getHumidifier)
        .map(OxygenDeliveryCluster.HumidifierCluster::getHumidiferUsed)
        .map(DvBoolean::isValue)
        .ifPresent(result::setHumidification);
  }

  private void setFlowRate(final OxygenTherapyDto result, final Opt<OxygenDeliveryCluster> oxygenDelivery)
  {
    oxygenDelivery
        .map(OxygenDeliveryCluster::getFlowRateModeEnum)
        .ifPresent(result::setFlowRateMode);

    final Opt<Double> rate = oxygenDelivery
        .map(OxygenDeliveryCluster::getAmbientOxygen)
        .map(OxygenDeliveryCluster.AmbientOxygenCluster::getOxygenFlowRate)
        .map(DvQuantity::getMagnitude);

    final Opt<String> unit = oxygenDelivery
        .map(OxygenDeliveryCluster::getAmbientOxygen)
        .map(OxygenDeliveryCluster.AmbientOxygenCluster::getOxygenFlowRate)
        .map(DvQuantity::getUnits);

    if (rate.isPresent() && unit.isPresent())
    {
      final Pair<Double, String> oxygenFlowRateWithUnit = getOxygenFlowRateWithUnit(rate.get(), unit.get());
      result.setFlowRate(oxygenFlowRateWithUnit.getFirst());
      result.setFlowRateUnit(oxygenFlowRateWithUnit.getSecond());
    }
  }

  public static Pair<Double, String> getOxygenFlowRateWithUnit(final double rate, @Nonnull final String unit)
  {
    final String[] unitParts = Pattern.compile("/").split(unit);

    final String massUnit = unitParts[0].toLowerCase();
    final String timeUnits = unitParts[1].toLowerCase();

    final Double liters = TherapyUnitsConverter.convertToUnit(rate, massUnit, "l");
    final Double timeRatio = TherapyUnitsConverter.convertToUnit(1.0, "min", timeUnits);

    if (liters == null || timeRatio == null)
    {
      throw new IllegalArgumentException("Invalid units for oxygen flow rate: " + massUnit + ", " + timeUnits);
    }

    return Pair.of(liters * timeRatio, "l/min");
  }

  private void setOxygenDevice(final OxygenTherapyDto result, final Opt<OxygenDeliveryCluster> oxygenDelivery)
  {
    oxygenDelivery
        .map(OxygenDeliveryCluster::getRouteEnum)
        .ifPresent(route -> result.setStartingDevice(new OxygenStartingDevice(route)));

    if (oxygenDelivery.isPresent() && oxygenDelivery.get().getDevice().size() == 1)
    {
      Opt.resolve(() -> oxygenDelivery.get().getDevice().get(0).getType())
          .map(DvText::getValue)
          .map(String::valueOf)
          .ifPresent(i -> result.getStartingDevice().setRouteType(i));
    }
  }

  private void setSaturation(final OxygenTherapyDto result, final Opt<OxygenDeliveryCluster> oxygenDelivery)
  {
    oxygenDelivery
        .map(OxygenDeliveryCluster::getAmbientOxygen)
        .map(OxygenDeliveryCluster.AmbientOxygenCluster::getMinimumPercentO2)
        .map(DvProportion::getNumerator)
        .map(Float::doubleValue)
        .ifPresent(result::setMinTargetSaturation);

    oxygenDelivery
        .map(OxygenDeliveryCluster::getAmbientOxygen)
        .map(OxygenDeliveryCluster.AmbientOxygenCluster::getMaximumPercentO2)
        .map(DvProportion::getNumerator)
        .map(Float::doubleValue)
        .ifPresent(result::setMaxTargetSaturation);
  }

  @Override
  protected OxygenTherapyDto createEmptyTherapyDto()
  {
    return new OxygenTherapyDto();
  }
}
