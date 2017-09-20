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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.api.core.Dictionary;
import com.marand.thinkmed.api.core.GrammaticalGender;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.MedicationsTestDictionary;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
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
@SuppressWarnings("TooBroadScope")
public class TherapyDisplayProviderTest
{
  private Dictionary testDictionary = new MedicationsTestDictionary();

  private TherapyDisplayProvider therapyDisplayProvider = new TherapyDisplayProvider();

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
    therapy.setStart(new DateTime(2017, 1, 1, 0, 0));
    therapy.setEnd(new DateTime(2017, 2, 7, 11, 0));

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, true, locale, false);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals("3X " + testDictionary.getEntry("per.day", GrammaticalGender.UNDEFINED, locale),
        therapy.getFrequencyDisplay());
    assertEquals("10 mg/1 mL", therapy.getQuantityDisplay());
    assertEquals("When needed", therapy.getWhenNeededDisplay());
    assertEquals("every 2. day", therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();
    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='DoseLabel TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='Quantity TextDataBold'>10 mg/1 mL </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>3X per day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Mon Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyDisplay TextData'>every 2. day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='WhenNeeded TextData'>When needed </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>" +
            "<span class='TherapyDuration'><br><span class='DurationLabel TextLabel MedicationLabel'>Duration </span>" +
            "<span class='Duration TextData'>5 weeks, 2 days, 11 hours </span></span>",
        formattedDescription);
  }

  @Test
  public void testTitratedSimpleTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final DosingFrequencyDto frequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3);
    fillSimpleTherapyCommonValues(therapy, frequency);
    therapy.setTitration(TitrationType.BLOOD_SUGAR);

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, true, locale, false);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals(
        "3X " + testDictionary.getEntry("per.day", GrammaticalGender.UNDEFINED, locale), therapy.getFrequencyDisplay());
    assertEquals("Titrate", therapy.getQuantityDisplay());
    assertNull(therapy.getWhenNeededDisplay());
    assertNull(therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();
    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='DoseLabel TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='Quantity TextDataBold'>Titrate </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>3X per day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Mon Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
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
    assertEquals("3X " + testDictionary.getEntry("per.day", GrammaticalGender.UNDEFINED, locale),
                 therapy.getFrequencyDisplay());
    assertEquals("10 mg/1 mL", therapy.getQuantityDisplay());
    assertEquals("When needed", therapy.getWhenNeededDisplay());
    assertEquals("every 2. day", therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();
    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='Quantity TextDataBold'>10 mg/1 mL </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>3X per day </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Mon Fri </span>" +
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
    therapy.setStartCriterion(MedicationStartCriterionEnum.BY_DOCTOR_ORDERS.name());

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, locale);

    assertEquals("Paracetamol (Lekadol)", therapy.getMedication().getDisplayName());
    assertEquals(
        testDictionary.getEntry("once.every", GrammaticalGender.UNDEFINED, locale) + " 8 " + testDictionary.getEntry(
            "hours.accusative",
            GrammaticalGender.UNDEFINED,
            locale),
        therapy.getFrequencyDisplay());
    assertEquals("Variable dose", therapy.getQuantityDisplay());
    assertEquals("10 mg/1 mL", therapy.getTimedDoseElements().get(0).getQuantityDisplay());
    assertEquals("09:00", therapy.getTimedDoseElements().get(0).getTimeDisplay());
    assertEquals("15 mg/1.5 mL", therapy.getTimedDoseElements().get(1).getQuantityDisplay());
    assertEquals("12:00", therapy.getTimedDoseElements().get(1).getTimeDisplay());
    assertEquals("20 mg/2 mL", therapy.getTimedDoseElements().get(2).getQuantityDisplay());
    assertEquals("18:30", therapy.getTimedDoseElements().get(2).getTimeDisplay());

    assertEquals("By doctor orders", therapy.getStartCriterionDisplay());
    assertNull(therapy.getDaysFrequencyDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Paracetamol </span>" +
            "<span class='MedicationName TextData'>(Lekadol) </span><br>" +
            "<span class='DoseLabel TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='Quantity TextDataBold'>Variable dose </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Mon Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='StartCriterion TextData'>By doctor orders </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
        formattedDescription);
  }

  @Test
  public void testConstantComplexTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    therapy.setDoseElement(createComplexDoseElement(120, 50.0, "mL/h"));
    therapy.setWhenNeeded(false);
    therapy.setStart(new DateTime(2017, 1, 1, 0, 0));

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, true, locale, false);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg/1 mL", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 mL", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertEquals(
        testDictionary.getEntry("once.every", GrammaticalGender.UNDEFINED, locale) + " 8 " + testDictionary.getEntry(
            "hours.accusative",
            GrammaticalGender.UNDEFINED,
            locale),
        therapy.getFrequencyDisplay());
    assertEquals("50 mL/h", therapy.getSpeedDisplay());
    assertNull(therapy.getWhenNeededDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Ketoprofen </span>" +
            "<span class='MedicationName TextData'>(Ketonal) </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>100 mg/1 mL </span><br>" +
            "<span class='MedicationName TextData'>Glukoza 10% </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>200 mL </span><br>" +
            "<span class='VolumeSumLabel TextLabel MedicationLabel'>TOTAL VOLUME </span>" +
            "<span class='VolumeSum TextData'>201 mL </span><br>" +
            "<span class='SpeedLabel TextLabel MedicationLabel'>RATE </span>" +
            "<span class='Speed TextData'>50 mL/h </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DurationLabel TextLabel MedicationLabel'>DURATION </span>" +
            "<span class='Duration TextData'>2h </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyLabel TextLabel MedicationLabel'>DOSING INTERVAL </span><span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Mon Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
        formattedDescription);
  }

  @Test
  public void testConstantComplexNoRateTherapyDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    therapy.setDoseElement(createComplexDoseElement(null, null, "mL/h"));
    therapy.setWhenNeeded(false);

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, locale);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg/1 mL", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 mL", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertNull(therapy.getWhenNeededDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Ketoprofen </span><span class='MedicationName TextData'>(Ketonal) " +
            "</span><br><span class='BaseLineInfusion TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='Quantity TextDataBold'>100 mg/1 mL </span><br><span class='MedicationName TextData'>Glukoza 10% </span><span class='Delimiter TextData'>" +
            "<span> &ndash; </span> </span><span class='Quantity TextDataBold'>200 mL </span><br>" +
            "<span class='VolumeSumLabel TextLabel MedicationLabel'>TOTAL VOLUME </span>" +
            "<span class='VolumeSum TextData'>201 mL </span><br><span class='FrequencyLabel TextLabel MedicationLabel'>DOSING INTERVAL </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span><span class='Delimiter TextData'><span> &ndash; " +
            "</span> </span><span class='DaysOfWeek TextData'>Mon Fri </span><span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br><span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
        formattedDescription);
  }

  @Test
  public void testConstantComplexTherapyShortDisplay()
  {
    final Locale locale = new Locale("en");
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillComplexTherapyCommonValues(therapy);
    therapy.setDoseElement(createComplexDoseElement(120, 50.0, "mL/h"));
    therapy.setWhenNeeded(false);

    therapyDisplayProvider.fillDisplayValues(therapy, false, false, locale);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg/1 mL", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 mL", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertEquals(
        testDictionary.getEntry("once.every", GrammaticalGender.UNDEFINED, locale) + " 8 " + testDictionary.getEntry(
            "hours.accusative",
            GrammaticalGender.UNDEFINED,
            locale),
        therapy.getFrequencyDisplay());
    assertEquals("50 mL/h", therapy.getSpeedDisplay());
    assertNull(therapy.getWhenNeededDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='MedicationName TextData'>Ketonal </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>100 mg/1 mL </span><br>" +
            "<span class='MedicationName TextData'>Glukoza 10% </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>200 mL </span><br>" +
            "<span class='VolumeSum TextData'>201 mL </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Speed TextData'>50 mL/h </span>" +
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
    doseElement1.setDoseElement(createComplexDoseElement(15, 50.0, "mL/h"));
    doseElement1.setDoseTime(new HourMinuteDto(10, 0));
    timedDoseElements.add(doseElement1);
    final TimedComplexDoseElementDto doseElement2 = new TimedComplexDoseElementDto();
    doseElement2.setDoseElement(createComplexDoseElement(15, 100.0, "mL/h"));
    doseElement2.setDoseTime(new HourMinuteDto(10, 15));
    timedDoseElements.add(doseElement2);
    final TimedComplexDoseElementDto doseElement3 = new TimedComplexDoseElementDto();
    doseElement3.setDoseElement(createComplexDoseElement(null, 200.0, "mL/h"));
    doseElement3.setDoseTime(new HourMinuteDto(10, 30));
    timedDoseElements.add(doseElement3);

    therapy.setTimedDoseElements(timedDoseElements);

    therapyDisplayProvider.fillDisplayValues(therapy, true, false, locale);

    assertEquals("Ketoprofen (Ketonal)", therapy.getIngredientsList().get(0).getMedication().getDisplayName());
    assertEquals("100 mg/1 mL", therapy.getIngredientsList().get(0).getQuantityDisplay());
    assertEquals("Glukoza 10%", therapy.getIngredientsList().get(1).getMedication().getDisplayName());
    assertEquals("200 mL", therapy.getIngredientsList().get(1).getQuantityDisplay());
    assertEquals(
        testDictionary.getEntry("once.every", GrammaticalGender.UNDEFINED, locale) + " 8 " + testDictionary.getEntry(
            "hours.accusative",
            GrammaticalGender.UNDEFINED,
            locale),
        therapy.getFrequencyDisplay());
    assertEquals("50-100-200 mL/h", therapy.getSpeedDisplay());
    assertEquals("50 mL/h", therapy.getTimedDoseElements().get(0).getSpeedDisplay());
    assertEquals("10:00–10:15", therapy.getTimedDoseElements().get(0).getIntervalDisplay());
    assertEquals("100 mL/h", therapy.getTimedDoseElements().get(1).getSpeedDisplay());
    assertEquals("10:15–10:30", therapy.getTimedDoseElements().get(1).getIntervalDisplay());
    assertEquals("200 mL/h", therapy.getTimedDoseElements().get(2).getSpeedDisplay());
    assertEquals("10:30–...", therapy.getTimedDoseElements().get(2).getIntervalDisplay());

    final String formattedDescription = therapy.getFormattedTherapyDisplay();

    assertEquals(
        "<span class='GenericName TextDataBold'>Ketoprofen </span>" +
            "<span class='MedicationName TextData'>(Ketonal) </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>100 mg/1 mL </span><br>" +
            "<span class='MedicationName TextData'>Glukoza 10% </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Quantity TextDataBold'>200 mL </span><br>" +
            "<span class='VolumeSumLabel TextLabel MedicationLabel'>TOTAL VOLUME </span>" +
            "<span class='VolumeSum TextData'>201 mL </span><br>" +
            "<span class='SpeedFormulaLabel TextLabel MedicationLabel'>DOSE </span>" +
            "<span class='SpeedFormula TextData'>Variable rate </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='FrequencyLabel TextLabel MedicationLabel'>DOSING INTERVAL </span><span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Frequency TextData'>Every 8 hours </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='DaysOfWeek TextData'>Mon Fri </span>" +
            "<span class='Delimiter TextData'><span> &ndash; </span> </span>" +
            "<span class='Route TextData'>Oral </span><br>" +
            "<span class='CommentLabel TextLabel MedicationLabel'>COMMENT </span>" +
            "<span class='Comment TextData'>Comment </span>",
        formattedDescription);
  }
  @Test
  public void testGetFormattedDecimalValue()
  {
    final Locale locale = new Locale("en");
    final String value1 = "3.652";
    final String value2 = "3.652 ml";
    final String value3 = "3 ml";
    final String value4 = "150";
    final String value5 = "volume sum equals 150.654 ml";
    final String value6 = "volume sum equals 500 ml";
    final String value7 = "150.654ml";
    final String value8 = "300ml";

    final String formattedValue1 = therapyDisplayProvider.getFormattedDecimalValue(value1, locale, false);
    final String formattedValue2 = therapyDisplayProvider.getFormattedDecimalValue(value2, locale, false);
    final String formattedValue3 = therapyDisplayProvider.getFormattedDecimalValue(value3, locale, false);
    final String formattedValue4 = therapyDisplayProvider.getFormattedDecimalValue(value4, locale, false);
    final String formattedValue5 = therapyDisplayProvider.getFormattedDecimalValue(value5, locale, false);
    final String formattedValue6 = therapyDisplayProvider.getFormattedDecimalValue(value6, locale, false);
    final String formattedValue7 = therapyDisplayProvider.getFormattedDecimalValue(value7, locale, false);
    final String formattedValue8 = therapyDisplayProvider.getFormattedDecimalValue(value8, locale, false);

    assertEquals("3.<span class='TextDataSmallerDecimal'>652</span>", formattedValue1);
    assertEquals("3.<span class='TextDataSmallerDecimal'>652</span> ml", formattedValue2);
    assertEquals("3 ml", formattedValue3);
    assertEquals("150", formattedValue4);
    assertEquals("volume sum equals 150.<span class='TextDataSmallerDecimal'>654</span> ml", formattedValue5);
    assertEquals("volume sum equals 500 ml", formattedValue6);
    assertEquals("150.<span class='TextDataSmallerDecimal'>654</span>ml", formattedValue7);
    assertEquals("300ml", formattedValue8);
  }

  @Test
  public void testGetDecimalStringValueEN() throws ParseException
  {
    final Locale locale = new Locale("en");

    assertEquals("11.667", therapyDisplayProvider.getDecimalStringValue(11.66666, locale));
    assertEquals("1.667", therapyDisplayProvider.getDecimalStringValue(1.66666, locale));
    assertEquals("0.667", therapyDisplayProvider.getDecimalStringValue(0.66666, locale));
    assertEquals("0.0667", therapyDisplayProvider.getDecimalStringValue(0.06666, locale));
    assertEquals("0.00667", therapyDisplayProvider.getDecimalStringValue(0.006666, locale));
    assertEquals("0.000667", therapyDisplayProvider.getDecimalStringValue(0.0006666, locale));
    assertEquals("1", therapyDisplayProvider.getDecimalStringValue(1.0000006, locale));
    assertEquals("1.007", therapyDisplayProvider.getDecimalStringValue(1.006666, locale));
    assertEquals("0", therapyDisplayProvider.getDecimalStringValue(0.0, locale));
    assertEquals("", therapyDisplayProvider.getDecimalStringValue(null, locale));
  }

  @Test
  public void testGetDecimalStringValueSLO() throws ParseException
  {
    final Locale locale = new Locale("sl");

    assertEquals("11,667", therapyDisplayProvider.getDecimalStringValue(11.66666, locale));
    assertEquals("1,667", therapyDisplayProvider.getDecimalStringValue(1.66666, locale));
    assertEquals("0,667", therapyDisplayProvider.getDecimalStringValue(0.66666, locale));
    assertEquals("0,0667", therapyDisplayProvider.getDecimalStringValue(0.06666, locale));
    assertEquals("0,00667", therapyDisplayProvider.getDecimalStringValue(0.006666, locale));
    assertEquals("0,000667", therapyDisplayProvider.getDecimalStringValue(0.0006666, locale));
    assertEquals("1", therapyDisplayProvider.getDecimalStringValue(1.0000006, locale));
    assertEquals("1,007", therapyDisplayProvider.getDecimalStringValue(1.006666, locale));
    assertEquals("0", therapyDisplayProvider.getDecimalStringValue(0.0, locale));
    assertEquals("", therapyDisplayProvider.getDecimalStringValue(null, locale));
  }

  @Test
  public void testGetTherapyDurationDisplay()
  {
    final Locale locale = new Locale("sl");

    ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2016, 12, 10, 8, 0));
    therapy.setEnd(new DateTime(2016, 12, 15, 16, 0));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>5 dni, 8 ur </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 11, 9, 1));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>1 dan, 1 ura, 1 min </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 10, 10, 15));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>2 ur, 15 min </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 10, 8, 15));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>15 min </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 25, 6, 0));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>2 tednov, 22 ur </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
    therapy.setEnd(new DateTime(2016, 12, 11, 8, 0));
    assertEquals("<span class='TherapyDuration'><br>" +
                     "<span class='DurationLabel TextLabel MedicationLabel'>Trajanje </span>" +
                     "<span class='Duration TextData'>1 dan </span></span>",
                 therapyDisplayProvider.getTherapyDurationDisplay(therapy, locale));
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
    therapy.setRoutes(Collections.singletonList(routeDto));
    therapy.setStart(new DateTime(2013, 3, 3, 0, 0));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("mL");
  }

  private void fillComplexTherapyCommonValues(final ComplexTherapyDto therapy)
  {
    final List<InfusionIngredientDto> ingredientsList = new ArrayList<>();
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(createMedicationDto(1L, "Ketonal", "Ketoprofen", MedicationTypeEnum.MEDICATION));
    ingredient1.setQuantity(100.0);
    ingredient1.setQuantityUnit("mg");
    ingredient1.setQuantityDenominator(1.0);
    ingredient1.setQuantityDenominatorUnit("mL");
    ingredientsList.add(ingredient1);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(createMedicationDto(1L, "Glukoza 10%", "Glukoza", MedicationTypeEnum.SOLUTION));
    ingredient2.setQuantityDenominator(200.0);
    ingredient2.setQuantityDenominatorUnit("mL");
    ingredientsList.add(ingredient2);
    therapy.setIngredientsList(ingredientsList);

    therapy.setVolumeSum(201.0);
    therapy.setVolumeSumUnit("mL");
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 8));
    therapy.setDaysOfWeek(createDaysList("MONDAY", "FRIDAY"));
    therapy.setPrescriberName("Tadej Avčin");
    therapy.setComment("Comment");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setCode("1");
    routeDto.setName("Oral");
    therapy.setRoutes(Collections.singletonList(routeDto));
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
