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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.api.core.Dictionary;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.dto.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringBeanByName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mitja Lapajne
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@Transactional(TransactionMode.ROLLBACK)

public class TherapyDisplayProviderTest
{
  @SpringBeanByName
  private Dictionary mainDictionary;

  @SpringBeanByName
  private TherapyDisplayProvider therapyDisplayProvider;

  @Test
  public void testConstantSimpleTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final DosingFrequencyDto frequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3);
    fillSimpleTherapyCommonValues(therapy, frequency);
    therapy.setDosingDaysFrequency(2);
    therapy.setDoseElement(createSimpleDoseElement(10.0, 1.0, "1 syrup"));
    therapy.setWhenNeeded(true);

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, locale);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals("3X " + mainDictionary.getEntry("per.day", null, locale),
        therapy.getFrequencyDisplay());
    assertEquals("10 mg/1 ml", therapy.getQuantityDisplay());
    assertEquals("When needed", therapy.getWhenNeededDisplay());
    assertEquals("every 2. day", therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();
    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='DoseLabel TextLabel'>DOSE </span>" +
            "<span class='Quantity TextData'>10 mg/1 ml </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>3X per day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>MO FR </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyDisplay TextData'>every 2. day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='WhenNeeded TextData'>When needed </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span><br>",
        formattedDescription);
  }

  @Test
  public void testConstantSimpleTherapyShortDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final DosingFrequencyDto frequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3);
    fillSimpleTherapyCommonValues(therapy, frequency);
    therapy.setDosingDaysFrequency(2);
    therapy.setDoseElement(createSimpleDoseElement(10.0, 1.0, "1 syrup"));
    therapy.setWhenNeeded(true);

    therapyDisplayProvider.fillDisplayValues(therapy, false, false, locale);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals("3X " + mainDictionary.getEntry("per.day", null, locale),
                 therapy.getFrequencyDisplay());
    assertEquals("10 mg/1 ml", therapy.getQuantityDisplay());
    assertEquals("When needed", therapy.getWhenNeededDisplay());
    assertEquals("every 2. day", therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();
    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='Quantity TextData'>10 mg/1 ml </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>3X per day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>MO FR </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyDisplay TextData'>every 2. day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='WhenNeeded TextData'>When needed </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span>",
        formattedDescription);
  }

  @Test
  public void testVariableSimpleTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    final DosingFrequencyDto frequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 8);
    fillSimpleTherapyCommonValues(therapy, frequency);
    final List<TimedSimpleDoseElementDto> timedDoseElements = new ArrayList<>();
    final TimedSimpleDoseElementDto doseElement1 = new TimedSimpleDoseElementDto();
    doseElement1.setDoseElement(createSimpleDoseElement(10.0, 1.0, "1 syrup"));
    doseElement1.setDoseTime(new HourMinuteDto(9, 0));
    timedDoseElements.add(doseElement1);
    final TimedSimpleDoseElementDto doseElement2 = new TimedSimpleDoseElementDto();
    doseElement2.setDoseElement(createSimpleDoseElement(15.0, 1.5, "1.5 syrup"));
    doseElement2.setDoseTime(new HourMinuteDto(12, 0));
    timedDoseElements.add(doseElement2);
    final TimedSimpleDoseElementDto doseElement3 = new TimedSimpleDoseElementDto();
    doseElement3.setDoseElement(createSimpleDoseElement(20.0, 2.0, "2 syrup"));
    doseElement3.setDoseTime(new HourMinuteDto(18, 30));
    timedDoseElements.add(doseElement3);
    therapy.setTimedDoseElements(timedDoseElements);
    therapy.getStartCriterions().add(MedicationStartCriterionEnum.BY_DOCTOR_ORDERS.name());

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, locale);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals(
        mainDictionary.getEntry("once.every", null, locale) + " 8 " + mainDictionary.getEntry("hours.accusative", null, locale),
        therapy.getFrequencyDisplay());
    assertEquals("10/1-15/1.5-20/2 mg/ml", therapy.getQuantityDisplay());
    assertEquals("10 mg/1 ml", therapy.getTimedDoseElements().get(0).getQuantityDisplay());
    assertEquals("09:00", therapy.getTimedDoseElements().get(0).getTimeDisplay());
    assertEquals("15 mg/1.5 ml", therapy.getTimedDoseElements().get(1).getQuantityDisplay());
    assertEquals("12:00", therapy.getTimedDoseElements().get(1).getTimeDisplay());
    assertEquals("20 mg/2 ml", therapy.getTimedDoseElements().get(2).getQuantityDisplay());
    assertEquals("18:30", therapy.getTimedDoseElements().get(2).getTimeDisplay());

    assertEquals("By doctor orders", therapy.getStartCriterionDisplay());
    assertNull(therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='DoseLabel TextLabel'>DOSE </span>" +
            "<span class='Quantity TextData'>10/1-15/1.5-20/2 mg/ml </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>MO FR </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='StartCriterion TextData'>By doctor orders </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span><br>",
        formattedDescription);
  }

  @Test
  public void testConstantComplexTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    therapy.setDoseElement(createComplexDoseElement(120, 50.0, "ml/h"));
    therapy.setWhenNeeded(false);

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, locale);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg/1 ml", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 ml", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertEquals(
        mainDictionary.getEntry("once.every", null, locale) + " 8 " + mainDictionary.getEntry("hours.accusative", null, locale),
        therapy.getFrequencyDisplay());
    assertEquals("50 ml/h", therapy.getSpeedDisplay());
    assertNull(therapy.getWhenNeededDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Ketoprofen </span>" +
            "<span class='MedicationName TextData'>(Ketonal) </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextData'>100 mg/1 ml </span><br>" +
            "<span class='MedicationName TextData'>Glukoza 10% </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextData'>200 ml </span><br>" +
            "<span class='VolumeSumLabel TextLabel'>TOTAL VOLUME </span>" +
            "<span class='VolumeSum TextData'>201 ml </span><br>" +
            "<span class='SpeedLabel TextLabel'>RATE </span>" +
            "<span class='Speed TextData'>50 ml/h </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DurationLabel TextLabel'>DURATION </span>" +
            "<span class='Duration TextData'>2h </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyLabel TextLabel'>DOSING INTERVAL </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>MO FR </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span><br>",
        formattedDescription);
  }

  @Test
  public void testConstantComplexTherapyShortDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    therapy.setDoseElement(createComplexDoseElement(120, 50.0, "ml/h"));
    therapy.setWhenNeeded(false);

    therapyDisplayProvider.fillDisplayValues(therapy, false, false, locale);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg/1 ml", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 ml", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertEquals(
        mainDictionary.getEntry("once.every", null, locale) + " 8 " + mainDictionary.getEntry("hours.accusative", null, locale),
        therapy.getFrequencyDisplay());
    assertEquals("50 ml/h", therapy.getSpeedDisplay());
    assertNull(therapy.getWhenNeededDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='MedicationName TextData'>Ketonal </span><br>" +
        "<span class='MedicationName TextData'>Glukoza 10% </span><br>" +
        "<span class='VolumeSum TextData'>201 ml </span>" +
        "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
        "<span class='Speed TextData'>50 ml/h </span>" +
        "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
        "<span class='Duration TextData'>2h </span>" +
        "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
        "<span class='Frequency TextData'>Every 8 hours </span>" +
        "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
        "<span class='Route TextData'>Oral </span>",
        formattedDescription);
  }

  @Test
  public void testVariableComplexTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    final List<TimedComplexDoseElementDto> timedDoseElements = new ArrayList<>();
    final TimedComplexDoseElementDto doseElement1 = new TimedComplexDoseElementDto();
    doseElement1.setDoseElement(createComplexDoseElement(15, 50.0, "ml/h"));
    doseElement1.setDoseTime(new HourMinuteDto(10, 0));
    timedDoseElements.add(doseElement1);
    final TimedComplexDoseElementDto doseElement2 = new TimedComplexDoseElementDto();
    doseElement2.setDoseElement(createComplexDoseElement(15, 100.0, "ml/h"));
    doseElement2.setDoseTime(new HourMinuteDto(10, 15));
    timedDoseElements.add(doseElement2);
    final TimedComplexDoseElementDto doseElement3 = new TimedComplexDoseElementDto();
    doseElement3.setDoseElement(createComplexDoseElement(null, 200.0, "ml/h"));
    doseElement3.setDoseTime(new HourMinuteDto(10, 30));
    timedDoseElements.add(doseElement3);

    therapy.setTimedDoseElements(timedDoseElements);

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, locale);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg/1 ml", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 ml", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertEquals(
        mainDictionary.getEntry("once.every", null, locale) + " 8 " + mainDictionary.getEntry("hours.accusative", null, locale),
        therapy.getFrequencyDisplay());
    assertEquals("50-100-200 ml/h", therapy.getSpeedDisplay());
    assertEquals("50 ml/h", therapy.getTimedDoseElements().get(0).getSpeedDisplay());
    assertEquals("10:00 - 10:15", therapy.getTimedDoseElements().get(0).getIntervalDisplay());
    assertEquals("100 ml/h", therapy.getTimedDoseElements().get(1).getSpeedDisplay());
    assertEquals("10:15 - 10:30", therapy.getTimedDoseElements().get(1).getIntervalDisplay());
    assertEquals("200 ml/h", therapy.getTimedDoseElements().get(2).getSpeedDisplay());
    assertEquals("10:30 - ...", therapy.getTimedDoseElements().get(2).getIntervalDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Ketoprofen </span>" +
            "<span class='MedicationName TextData'>(Ketonal) </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextData'>100 mg/1 ml </span><br>" +
            "<span class='MedicationName TextData'>Glukoza 10% </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextData'>200 ml </span><br>" +
            "<span class='VolumeSumLabel TextLabel'>TOTAL VOLUME </span>" +
            "<span class='VolumeSum TextData'>201 ml </span><br>" +
            "<span class='SpeedLabel TextLabel'>RATE </span>" +
            "<span class='Speed TextData'>50-100-200 ml/h </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyLabel TextLabel'>DOSING INTERVAL </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>MO FR </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span><br>",
        formattedDescription);
  }

  private void fillSimpleTherapyCommonValues(final SimpleTherapyDto therapy, final DosingFrequencyDto frequency)
  {
    therapy.setMedication(createMedicationDto(1L, "Lekadol", "Paracetamol", MedicationTypeEnum.MEDICATION));
    therapy.setDosingFrequency(frequency);
    therapy.setDaysOfWeek(createDaysList("MONDAY", "FRIDAY"));
    therapy.setPrescriberName("Tadej Avčin");
    therapy.setComment("Comment");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setCode("1");
    routeDto.setName("Oral");
    therapy.setRoute(routeDto);
    therapy.setStart(new DateTime(2013, 3, 3, 0, 0));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
  }

  private void fillComplexTherapyCommonValues(final ComplexTherapyDto therapy)
  {
    final List<InfusionIngredientDto> ingredientsList = new ArrayList<>();
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(createMedicationDto(1L, "Ketonal", "Ketoprofen", MedicationTypeEnum.MEDICATION));
    ingredient1.setQuantity(100.0);
    ingredient1.setQuantityUnit("mg");
    ingredient1.setVolume(1.0);
    ingredient1.setVolumeUnit("ml");
    ingredientsList.add(ingredient1);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(createMedicationDto(1L, "Glukoza 10%", "Glukoza", MedicationTypeEnum.SOLUTION));
    ingredient2.setVolume(200.0);
    ingredient2.setVolumeUnit("ml");
    ingredientsList.add(ingredient2);
    therapy.setIngredientsList(ingredientsList);

    therapy.setVolumeSum(201.0);
    therapy.setVolumeSumUnit("ml");
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 8));
    therapy.setDaysOfWeek(createDaysList("MONDAY", "FRIDAY"));
    therapy.setPrescriberName("Tadej Avčin");
    therapy.setComment("Comment");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setCode("1");
    routeDto.setName("Oral");
    therapy.setRoute(routeDto);
    therapy.setStart(new DateTime(2013, 3, 3, 0, 0));
  }

  private MedicationDto createMedicationDto(
      final long id,
      final String name,
      final String genericName,
      final MedicationTypeEnum type)
  {
    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(id);
    medicationDto.setName(name);
    medicationDto.setGenericName(genericName);
    medicationDto.setMedicationType(type);
    return medicationDto;
  }

  private List<String> createDaysList(final String... days)
  {
    return Arrays.asList(days);
  }

  private SimpleDoseElementDto createSimpleDoseElement(
      final Double quantity,
      final Double quantityDenominator,
      final String singleDose)
  {
    final SimpleDoseElementDto doseElementDto = new SimpleDoseElementDto();
    doseElementDto.setQuantity(quantity);
    doseElementDto.setQuantityDenominator(quantityDenominator);
    doseElementDto.setDoseDescription(singleDose);
    return doseElementDto;
  }

  private ComplexDoseElementDto createComplexDoseElement(
      final Integer duration,
      final Double speed,
      final String speedUnit)
  {
    final ComplexDoseElementDto doseElementDto = new ComplexDoseElementDto();
    doseElementDto.setDuration(duration);
    doseElementDto.setRate(speed);
    doseElementDto.setRateUnit(speedUnit);
    return doseElementDto;
  }
}
