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

package com.marand.thinkmed.medications.change.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.IndicationDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedSimpleDoseElementDto;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mitja Lapajne
 */
public class TherapyChangeCalculatorTest
{
  private static final Locale LOCALE = new Locale("en_GB");

  private TherapyChangeCalculator therapyChangeCalculator = new TherapyChangeCalculatorImpl();

  private TherapyDisplayProvider therapyDisplayProvider = new TherapyDisplayProvider();

  @Test
  public void testCalculateTherapyChangesConstantSimple()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);
    therapy.setQuantityDisplay("500 mg");
    therapy.setFrequencyDisplay("3x");
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setName("Oral");
    therapy.setRoutes(Collections.singletonList(route));
    therapy.setApplicationPreconditionDisplay("Before meal");
    therapy.setStartCriterionDisplay("Doctor's orders");
    therapy.setWhenNeededDisplay("When needed");
    therapy.setMaxDailyFrequency(5);
    therapy.setComment("Old comment");
    therapy.setClinicalIndication(new IndicationDto(null, "Old indication"));

    final ConstantSimpleTherapyDto changedTherapy = new ConstantSimpleTherapyDto();
    final MedicationDto changedMedication = new MedicationDto();
    changedMedication.setDisplayName("Paracetamol 1000 mg");
    changedTherapy.setMedication(changedMedication);
    changedTherapy.setQuantityDisplay("1000 mg");
    changedTherapy.setFrequencyDisplay("2x");
    final List<MedicationRouteDto> changedMedicationRoutes = new ArrayList<>();
    final MedicationRouteDto firstRoute = new MedicationRouteDto();
    firstRoute.setName("IV");
    changedMedicationRoutes.add(firstRoute);
    final MedicationRouteDto secondRoute = new MedicationRouteDto();
    secondRoute.setName("IM");
    changedMedicationRoutes.add(secondRoute);
    changedTherapy.setRoutes(changedMedicationRoutes);
    changedTherapy.setApplicationPreconditionDisplay("After meal");
    changedTherapy.setComment("New comment");
    changedTherapy.setClinicalIndication(new IndicationDto(null, "New indication"));

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);
    assertEquals(10, changes.size());

    final TherapyChangeDto<?, ?> medicationChange = changes.get(0);
    assertEquals(TherapyChangeType.MEDICATION, medicationChange.getType());
    assertEquals("Paracetamol 500 mg", medicationChange.getOldValue());
    assertEquals("Paracetamol 1000 mg", medicationChange.getNewValue());

    final TherapyChangeDto<?, ?> doseChange = changes.get(1);
    assertEquals(TherapyChangeType.DOSE, doseChange.getType());
    assertEquals("500 mg", doseChange.getOldValue());
    assertEquals("1000 mg", doseChange.getNewValue());

    final TherapyChangeDto<?, ?> frequencyChange = changes.get(2);
    assertEquals(TherapyChangeType.DOSE_INTERVAL, frequencyChange.getType());
    assertEquals("3x", frequencyChange.getOldValue());
    assertEquals("2x", frequencyChange.getNewValue());

    final TherapyChangeDto<?, ?> commentChange = changes.get(3);
    assertEquals(TherapyChangeType.COMMENT, commentChange.getType());
    assertEquals("Old comment", commentChange.getOldValue());
    assertEquals("New comment", commentChange.getNewValue());

    final TherapyChangeDto<?, ?> indicationChange = changes.get(4);
    assertEquals(TherapyChangeType.INDICATION, indicationChange.getType());
    assertEquals("Old indication", indicationChange.getOldValue());
    assertEquals("New indication", indicationChange.getNewValue());

    final TherapyChangeDto<?, ?> routeChange = changes.get(5);
    final List<String> oldRouteValues = Collections.singletonList("Oral");
    final List<String> newRouteValues = Arrays.asList("IV", "IM");

    assertEquals(TherapyChangeType.ROUTE, routeChange.getType());
    assertEquals(oldRouteValues, routeChange.getOldValue());
    assertEquals(newRouteValues, routeChange.getNewValue());

    final TherapyChangeDto<?, ?> additionalConditionsChange = changes.get(6);
    assertEquals(TherapyChangeType.ADDITIONAL_CONDITIONS, additionalConditionsChange.getType());
    assertEquals("Before meal", additionalConditionsChange.getOldValue());
    assertEquals("After meal", additionalConditionsChange.getNewValue());

    final TherapyChangeDto<?, ?> whenNeededChange = changes.get(7);
    assertEquals(TherapyChangeType.WHEN_NEEDED, whenNeededChange.getType());
    assertEquals("When needed", whenNeededChange.getOldValue());
    assertNull(whenNeededChange.getNewValue());

    final TherapyChangeDto<?, ?> maxDosesChange = changes.get(8);
    assertEquals(TherapyChangeType.MAX_DOSES, maxDosesChange.getType());
    assertEquals("5", maxDosesChange.getOldValue());
    assertNull(maxDosesChange.getNewValue());

    final TherapyChangeDto<?, ?> doctorOrdersChange = changes.get(9);
    assertEquals(TherapyChangeType.DOCTOR_ORDERS, doctorOrdersChange.getType());
    assertEquals("Doctor's orders", doctorOrdersChange.getOldValue());
    assertNull(doctorOrdersChange.getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesOxygenSameMedicationDifferentOxygenValues()
  {
    final OxygenTherapyDto therapy = new OxygenTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);
    final String oldSpeed = "5ml/min";
    therapy.setSpeedDisplay(oldSpeed);
    therapy.setStartingDevice(new OxygenStartingDevice(AdministrationDetailsCluster.OxygenDeliveryCluster.Route.CPAP_MASK));
    therapy.setMinTargetSaturation(10.0);
    therapy.setMaxTargetSaturation(20.0);

    final OxygenTherapyDto changedTherapy = new OxygenTherapyDto();
    changedTherapy.setMedication(medication);
    final String newSpeed = "6ml/min";
    changedTherapy.setSpeedDisplay(newSpeed);
    changedTherapy.setStartingDevice(new OxygenStartingDevice(AdministrationDetailsCluster.OxygenDeliveryCluster.Route.FULL_FACE_MASK));
    changedTherapy.setMinTargetSaturation(20.0);
    changedTherapy.setMaxTargetSaturation(30.0);

    therapyDisplayProvider.fillDisplayValues(therapy, false, false, LOCALE);
    therapyDisplayProvider.fillDisplayValues(changedTherapy, false, false, LOCALE);
    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);

    assertEquals(3, changes.size());

    assertEquals(TherapyChangeType.DOSE, changes.get(0).getType());
    assertEquals(oldSpeed, changes.get(0).getOldValue());
    assertEquals(newSpeed, changes.get(0).getNewValue());

    assertEquals(TherapyChangeType.DEVICE, changes.get(1).getType());
    assertEquals(therapy.getStartingDeviceDisplay(), changes.get(1).getOldValue());
    assertEquals(changedTherapy.getStartingDeviceDisplay(), changes.get(1).getNewValue());

    assertEquals(TherapyChangeType.SATURATION, changes.get(2).getType());
    assertEquals(therapy.getSaturationDisplay(), changes.get(2).getOldValue());
    assertEquals(changedTherapy.getSaturationDisplay(), changes.get(2).getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesOxygenSameMedicationDifferentOxygenValuesWithRouteType()
  {
    final OxygenTherapyDto therapy = new OxygenTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);
    final String oldSpeed = "5ml/min";
    therapy.setSpeedDisplay(oldSpeed);
    therapy.setStartingDevice(new OxygenStartingDevice(AdministrationDetailsCluster.OxygenDeliveryCluster.Route.CPAP_MASK));
    therapy.getStartingDevice().setRouteType("1");
    therapy.setMinTargetSaturation(10.0);
    therapy.setMaxTargetSaturation(20.0);

    final OxygenTherapyDto changedTherapy = new OxygenTherapyDto();
    changedTherapy.setMedication(medication);
    final String newSpeed = "6ml/min";
    changedTherapy.setSpeedDisplay(newSpeed);
    changedTherapy.setStartingDevice(new OxygenStartingDevice(AdministrationDetailsCluster.OxygenDeliveryCluster.Route.CPAP_MASK));
    changedTherapy.getStartingDevice().setRouteType("2");
    changedTherapy.setMinTargetSaturation(20.0);
    changedTherapy.setMaxTargetSaturation(30.0);

    therapyDisplayProvider.fillDisplayValues(therapy, false, false, LOCALE);
    therapyDisplayProvider.fillDisplayValues(changedTherapy, false, false, LOCALE);
    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);

    assertEquals(3, changes.size());

    assertEquals(TherapyChangeType.DOSE, changes.get(0).getType());
    assertEquals(oldSpeed, changes.get(0).getOldValue());
    assertEquals(newSpeed, changes.get(0).getNewValue());

    assertEquals(TherapyChangeType.DEVICE, changes.get(1).getType());
    assertEquals(therapy.getStartingDeviceDisplay(), changes.get(1).getOldValue());
    assertEquals(changedTherapy.getStartingDeviceDisplay(), changes.get(1).getNewValue());

    assertEquals(TherapyChangeType.SATURATION, changes.get(2).getType());
    assertEquals(therapy.getSaturationDisplay(), changes.get(2).getOldValue());
    assertEquals(changedTherapy.getSaturationDisplay(), changes.get(2).getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesOxygenDifferentMedicationAndOxygenValues()
  {
    final OxygenTherapyDto therapy = new OxygenTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);

    final OxygenTherapyDto changedTherapy = new OxygenTherapyDto();
    final MedicationDto medication2 = new MedicationDto();
    medication2.setDisplayName("Paracetamol 1400 mg");
    changedTherapy.setMedication(medication2);

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);

    assertEquals(1, changes.size());

    final TherapyChangeDto<?, ?> medicationChange = changes.get(0);
    assertEquals(TherapyChangeType.MEDICATION, medicationChange.getType());
    assertEquals("Paracetamol 500 mg", medicationChange.getOldValue());
    assertEquals("Paracetamol 1400 mg", medicationChange.getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesOxygenNoDevice()
  {
    final OxygenTherapyDto therapy = new OxygenTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);
    final String oldSpeed = "5ml/min";
    therapy.setSpeedDisplay(oldSpeed);
    therapy.setMinTargetSaturation(10.0);
    therapy.setMaxTargetSaturation(20.0);

    final OxygenTherapyDto changedTherapy = new OxygenTherapyDto();
    changedTherapy.setMedication(medication);
    final String newSpeed = "6ml/min";
    changedTherapy.setSpeedDisplay(newSpeed);
    changedTherapy.setStartingDevice(new OxygenStartingDevice(AdministrationDetailsCluster.OxygenDeliveryCluster.Route.FULL_FACE_MASK));
    changedTherapy.setMinTargetSaturation(20.0);
    changedTherapy.setMaxTargetSaturation(30.0);

    therapyDisplayProvider.fillDisplayValues(therapy, false, false, new Locale("en_GB"));
    therapyDisplayProvider.fillDisplayValues(changedTherapy, false, false, new Locale("en_GB"));
    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);

    assertEquals(3, changes.size());

    assertEquals(TherapyChangeType.DOSE, changes.get(0).getType());
    assertEquals(oldSpeed, changes.get(0).getOldValue());
    assertEquals(newSpeed, changes.get(0).getNewValue());

    assertEquals(TherapyChangeType.DEVICE, changes.get(1).getType());
    assertNull(changes.get(1).getOldValue());
    assertEquals(changedTherapy.getStartingDeviceDisplay(), changes.get(1).getNewValue());

    assertEquals(TherapyChangeType.SATURATION, changes.get(2).getType());
    assertEquals(therapy.getSaturationDisplay(), changes.get(2).getOldValue());
    assertEquals(changedTherapy.getSaturationDisplay(), changes.get(2).getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesOxygenNoDeviceSameSaturation()
  {
    final OxygenTherapyDto therapy = new OxygenTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);
    final String oldSpeed = "5ml/min";
    therapy.setSpeedDisplay(oldSpeed);
    therapy.setMinTargetSaturation(10.0);
    therapy.setMaxTargetSaturation(20.0);

    final OxygenTherapyDto changedTherapy = new OxygenTherapyDto();
    changedTherapy.setMedication(medication);
    final String newSpeed = "6ml/min";
    changedTherapy.setSpeedDisplay(newSpeed);
    changedTherapy.setMinTargetSaturation(10.0);
    changedTherapy.setMaxTargetSaturation(20.0);

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);

    assertEquals(1, changes.size());

    assertEquals(TherapyChangeType.DOSE, changes.get(0).getType());
    assertEquals(oldSpeed, changes.get(0).getOldValue());
    assertEquals(newSpeed, changes.get(0).getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesConstantComplex()
  {
    //therapy
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setSpeedDisplay("30 ml/h");

    //changed therapy
    final ConstantComplexTherapyDto changedTherapy = new ConstantComplexTherapyDto();
    changedTherapy.setSpeedDisplay("50 ml/h");

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);
    assertEquals(1, changes.size());

    final TherapyChangeDto<?, ?> doseChange = changes.get(0);
    assertEquals(TherapyChangeType.RATE, doseChange.getType());
    assertEquals("30 ml/h", doseChange.getOldValue());
    assertEquals("50 ml/h", doseChange.getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesVariableDose()
  {
    //therapy
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);

    final TimedSimpleDoseElementDto timedDoseElement1 = new TimedSimpleDoseElementDto();
    timedDoseElement1.setDoseTime(new HourMinuteDto(8, 0));
    timedDoseElement1.setQuantityDisplay("500 mg");
    therapy.getTimedDoseElements().add(timedDoseElement1);

    final TimedSimpleDoseElementDto timedDoseElement2 = new TimedSimpleDoseElementDto();
    timedDoseElement2.setDoseTime(new HourMinuteDto(20, 0));
    timedDoseElement2.setQuantityDisplay("1000 mg");
    therapy.getTimedDoseElements().add(timedDoseElement2);

    //changed therapy
    final VariableSimpleTherapyDto changedTherapy = new VariableSimpleTherapyDto();
    changedTherapy.setMedication(medication);

    final TimedSimpleDoseElementDto changedTimedDoseElement1 = new TimedSimpleDoseElementDto();
    changedTimedDoseElement1.setDoseTime(new HourMinuteDto(9, 0));
    changedTimedDoseElement1.setQuantityDisplay("600 mg");
    changedTherapy.getTimedDoseElements().add(changedTimedDoseElement1);

    final TimedSimpleDoseElementDto changedTimedDoseElement2 = new TimedSimpleDoseElementDto();
    changedTimedDoseElement2.setDoseTime(new HourMinuteDto(21, 0));
    changedTimedDoseElement2.setQuantityDisplay("1200 mg");
    changedTherapy.getTimedDoseElements().add(changedTimedDoseElement2);

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);
    assertEquals(1, changes.size());

    final TherapyChangeDto<?, ?> doseChange = changes.get(0);
    assertEquals(TherapyChangeType.VARIABLE_DOSE, doseChange.getType());
    assertEquals(therapy.getTimedDoseElements(), doseChange.getOldValue());
    assertEquals(changedTherapy.getTimedDoseElements(), doseChange.getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesVariableDoseToDose()
  {
    //therapy
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);

    final TimedSimpleDoseElementDto timedDoseElement1 = new TimedSimpleDoseElementDto();
    timedDoseElement1.setDoseTime(new HourMinuteDto(8, 0));
    timedDoseElement1.setQuantityDisplay("500 mg");
    therapy.getTimedDoseElements().add(timedDoseElement1);

    final TimedSimpleDoseElementDto timedDoseElement2 = new TimedSimpleDoseElementDto();
    timedDoseElement2.setDoseTime(new HourMinuteDto(20, 0));
    timedDoseElement2.setQuantityDisplay("1000 mg");
    therapy.getTimedDoseElements().add(timedDoseElement2);

    //changed therapy
    final ConstantSimpleTherapyDto changedTherapy = new ConstantSimpleTherapyDto();
    changedTherapy.setMedication(medication);
    changedTherapy.setQuantityDisplay("600 mg");

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);
    assertEquals(1, changes.size());

    final TherapyChangeDto<?, ?> doseChange = changes.get(0);
    assertEquals(TherapyChangeType.VARIABLE_DOSE_TO_DOSE, doseChange.getType());
    assertEquals(therapy.getTimedDoseElements(), doseChange.getOldValue());
    assertEquals("600 mg", doseChange.getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesDoseToVariableDose()
  {
    //therapy
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);
    therapy.setQuantityDisplay("500 mg");

    //changed therapy
    final VariableSimpleTherapyDto changedTherapy = new VariableSimpleTherapyDto();
    changedTherapy.setMedication(medication);

    final TimedSimpleDoseElementDto changedTimedDoseElement1 = new TimedSimpleDoseElementDto();
    changedTimedDoseElement1.setDoseTime(new HourMinuteDto(9, 0));
    changedTimedDoseElement1.setQuantityDisplay("600 mg");
    changedTherapy.getTimedDoseElements().add(changedTimedDoseElement1);

    final TimedSimpleDoseElementDto changedTimedDoseElement2 = new TimedSimpleDoseElementDto();
    changedTimedDoseElement2.setDoseTime(new HourMinuteDto(21, 0));
    changedTimedDoseElement2.setQuantityDisplay("1200 mg");
    changedTherapy.getTimedDoseElements().add(changedTimedDoseElement2);

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);
    assertEquals(1, changes.size());

    final TherapyChangeDto<?, ?> doseChange = changes.get(0);
    assertEquals(TherapyChangeType.DOSE_TO_VARIABLE_DOSE, doseChange.getType());
    assertEquals("500 mg", doseChange.getOldValue());
    assertEquals(changedTherapy.getTimedDoseElements(), doseChange.getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesVariableRate()
  {
    //therapy
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();

    final TimedComplexDoseElementDto timedDoseElement1 = new TimedComplexDoseElementDto();
    timedDoseElement1.setDoseTime(new HourMinuteDto(8, 0));
    timedDoseElement1.setSpeedDisplay("50 ml/h");
    therapy.getTimedDoseElements().add(timedDoseElement1);

    final TimedComplexDoseElementDto timedDoseElement2 = new TimedComplexDoseElementDto();
    timedDoseElement2.setDoseTime(new HourMinuteDto(20, 0));
    timedDoseElement1.setSpeedDisplay("10 ml/h");
    therapy.getTimedDoseElements().add(timedDoseElement2);

    //changed therapy
    final VariableComplexTherapyDto changedTherapy = new VariableComplexTherapyDto();

    final TimedComplexDoseElementDto changedTimedDoseElement1 = new TimedComplexDoseElementDto();
    changedTimedDoseElement1.setDoseTime(new HourMinuteDto(9, 0));
    changedTimedDoseElement1.setSpeedDisplay("60 ml/h");
    changedTherapy.getTimedDoseElements().add(changedTimedDoseElement1);

    final TimedComplexDoseElementDto changedTimedDoseElement2 = new TimedComplexDoseElementDto();
    changedTimedDoseElement2.setDoseTime(new HourMinuteDto(21, 0));
    changedTimedDoseElement2.setSpeedDisplay("20 ml/h");
    changedTherapy.getTimedDoseElements().add(changedTimedDoseElement2);

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);
    assertEquals(1, changes.size());

    final TherapyChangeDto<?, ?> doseChange = changes.get(0);
    assertEquals(TherapyChangeType.VARIABLE_RATE, doseChange.getType());
    assertEquals(therapy.getTimedDoseElements(), doseChange.getOldValue());
    assertEquals(changedTherapy.getTimedDoseElements(), doseChange.getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesVariableRateToRate()
  {
    //therapy
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();

    final TimedComplexDoseElementDto timedDoseElement1 = new TimedComplexDoseElementDto();
    timedDoseElement1.setDoseTime(new HourMinuteDto(8, 0));
    timedDoseElement1.setSpeedDisplay("50 ml/h");
    therapy.getTimedDoseElements().add(timedDoseElement1);

    final TimedComplexDoseElementDto timedDoseElement2 = new TimedComplexDoseElementDto();
    timedDoseElement2.setDoseTime(new HourMinuteDto(20, 0));
    timedDoseElement1.setSpeedDisplay("10 ml/h");
    therapy.getTimedDoseElements().add(timedDoseElement2);

    //changed therapy
    final ConstantComplexTherapyDto changedTherapy = new ConstantComplexTherapyDto();
    changedTherapy.setSpeedDisplay("30 ml/h");

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);
    assertEquals(1, changes.size());

    final TherapyChangeDto<?, ?> doseChange = changes.get(0);
    assertEquals(TherapyChangeType.VARIABLE_RATE_TO_RATE, doseChange.getType());
    assertEquals(therapy.getTimedDoseElements(), doseChange.getOldValue());
    assertEquals("30 ml/h", doseChange.getNewValue());
  }

  @Test
  public void testCalculateTherapyChangesRateToVariableRate()
  {
    //therapy
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setSpeedDisplay("30 ml/h");

    //changed therapy
    final VariableComplexTherapyDto changedTherapy = new VariableComplexTherapyDto();

    final TimedComplexDoseElementDto changedTimedDoseElement1 = new TimedComplexDoseElementDto();
    changedTimedDoseElement1.setDoseTime(new HourMinuteDto(9, 0));
    changedTimedDoseElement1.setSpeedDisplay("60 ml/h");
    changedTherapy.getTimedDoseElements().add(changedTimedDoseElement1);

    final TimedComplexDoseElementDto changedTimedDoseElement2 = new TimedComplexDoseElementDto();
    changedTimedDoseElement2.setDoseTime(new HourMinuteDto(21, 0));
    changedTimedDoseElement2.setSpeedDisplay("20 ml/h");
    changedTherapy.getTimedDoseElements().add(changedTimedDoseElement2);

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, false, LOCALE);
    assertEquals(1, changes.size());

    final TherapyChangeDto<?, ?> doseChange = changes.get(0);
    assertEquals(TherapyChangeType.RATE_TO_VARIABLE_RATE, doseChange.getType());
    assertEquals("30 ml/h", doseChange.getOldValue());
    assertEquals(changedTherapy.getTimedDoseElements(), doseChange.getNewValue());
  }


  @Test
  public void testCalculateTherapyChangesIncludeTherapyInterval()
  {
    //therapy
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName("Paracetamol 500 mg");
    therapy.setMedication(medication);
    therapy.setStart(new DateTime(2016, 12, 6, 12, 0));

    //changed therapy
    final ConstantSimpleTherapyDto changedTherapy = new ConstantSimpleTherapyDto();
    changedTherapy.setMedication(medication);
    changedTherapy.setStart(new DateTime(2016, 12, 6, 13, 0));
    changedTherapy.setEnd(new DateTime(2016, 12, 7, 13, 0));

    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(therapy, changedTherapy, true, LOCALE);
    assertEquals(2, changes.size());

    final TherapyChangeDto<?, ?> doseChange = changes.get(0);
    assertEquals(TherapyChangeType.START, doseChange.getType());
    assertEquals("06-Dec-2016 12:00", doseChange.getOldValue());
    assertEquals("06-Dec-2016 13:00", doseChange.getNewValue());

    final TherapyChangeDto<?, ?> doseChange1 = changes.get(1);
    assertEquals(TherapyChangeType.END, doseChange1.getType());
    assertNull(doseChange1.getOldValue());
    assertEquals("07-Dec-2016 13:00", doseChange1.getNewValue());
  }
}
