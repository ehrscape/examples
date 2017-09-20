/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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

package com.marand.thinkmed.medications.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.IndicationDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayElementReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportUtils;

/**
 * @author Nejc Korasa
 */

/**
 * Used for testing only
 */
@SuppressWarnings({"unused", "UtilityClassWithoutPrivateConstructor"})
public class SampleMedicationsJRDataSourceFactory
{
  static
  {
    TherapyDayReportUtils.init(null);
  }

  public static Date getTherapyApplicationStartDate()
  {
    //noinspection deprecation
    return new Date(2016, Calendar.FEBRUARY, 27);
  }

  public static Collection<TherapyDayReportDto> createTherapyDayRecordsX()
  {
    final List<TherapyDayReportDto> therapyDayReportDtos = new ArrayList<>();
    final TherapyDayReportDto e = new TherapyDayReportDto(false);
    e.setPatientData(
        createPatientData(
            true,
            "13.34.8888 12:34",
            "Priimek in Ime pacienta 1",
            "Koprska ulica 100, 1234 Ljubljana",
            "99.99.9999 / 33l2m",
            Gender.MALE,
            "123456789",
            "123123123",
            "Oddelek z zelo dolgim imenom in veliko besedami",
            "SOBA999",
            "99.99.9999",
            999,
            "M999/17",
            "Diagnoza ali bolezen",
            new ArrayList<String>(),
            "20506",
            "presenceOfAllergies"));
    therapyDayReportDtos.add(e);
    return therapyDayReportDtos;
  }

  public static Collection<TherapyDayReportDto> createTherapyDayRecords()
  {
    final List<TherapyDayReportDto> records = new ArrayList<>();

    final TherapyDayReportDto record1 = new TherapyDayReportDto(false);
    record1.setPatientSortOrder(1);
    record1.setPatientData(
        createPatientData(
            true,
            "13.34.8888 12:34",
            "Priimek in Ime pacienta 1",
            "Koprska ulica 100, 1234 Ljubljana",
            "99.99.9999 / 33l2m",
            Gender.FEMALE,
            "123456789",
            "123123123",
            "Oddelek z zelo dolgim imenom in veliko besedami",
            "SOBA999",
            "99.99.9999",
            999,
            "M999/17",
            "Diagnoza ali bolezen",
            createAllergies("Alergija 1", "Alergija na penicilin in njegove derivate"),
            "20506",
            "presenceOfAllergies"));
    record1.setComplexElements(createTherapyDayElementComplexRecords(1));
    record1.setSimpleElements(createTherapyDayElementSimpleRecords(1));
    records.add(record1);

    final TherapyDayReportDto record2 = new TherapyDayReportDto(false);
    record2.setPatientSortOrder(2);
    record2.setPatientData(
        createPatientData(
            false,
            "13.34.8888 12:34",
            "Priimek in Ime pacienta 2",
            "Koprska ulica 100, 1234 Ljubljana",
            "99.99.9999 / 33l2m",
            Gender.MALE,
            "123456789",
            "123123123",
            "Oddelek z zelo dolgim imenom in veliko besedami",
            "SOBA999",
            "12.34.5678",
            999,
            "M999",
            "Diagnoza ali bolezen",
            createAllergies("Alergija 1", "Alergija na penicilin in njegove derivate"),
            "20506",
            "presenceOfAllergies"));
    record2.setComplexElements(createTherapyDayElementComplexRecords(2));
    record2.setSimpleElements(createTherapyDayElementSimpleRecords(2));
    records.add(record2);

    return records;
  }

  public static List<TherapyDayElementReportDto> createTherapyDayElementComplexRecords()
  {
    return createTherapyDayElementComplexRecords(1);
  }

  private static List<TherapyDayElementReportDto> createTherapyDayElementComplexRecords(final int seq)
  {
    final List<TherapyDayElementReportDto> records = new ArrayList<>();

    records.add(createConstantComplexElement(seq, createIngredients(seq * 10 + 1, true, true), null, Integer.MAX_VALUE));
    records.add(
        createConstantComplexElement(
            seq + 1, createIngredients((seq + 1) * 10 + 1, true, true), null, Integer.MAX_VALUE));

    final List<Pair<String, String>> intervalSpeedList2 = new ArrayList<>();
    intervalSpeedList2.add(Pair.of("10:00 - 11:00", "1011 ml/h"));
    intervalSpeedList2.add(Pair.of("11:00 - 12:00", "1112 ml/h"));
    intervalSpeedList2.add(Pair.of("12:00 - 13:00", "1213 ml/h"));
    intervalSpeedList2.add(Pair.of("13:00 - 14:00", "1314 ml/h"));
    intervalSpeedList2.add(Pair.of("14:00 - 15:00", "1415 ml/h"));
    intervalSpeedList2.add(Pair.of("15:00 - 16:00", "1516 ml/h"));
    intervalSpeedList2.add(Pair.of("16:00 - ...", "16.. ml/h"));
    records.add(
        createVariableComplexElement(
            seq + 2, createIngredients((seq + 2) * 10 + 2, true, true), intervalSpeedList2, "Group 1", 1));

    records.add(
        createConstantComplexElement(seq + 3, createIngredients((seq + 3) * 10 + 3, false, true), "Group 1", 1));

    final List<Pair<String, String>> intervalSpeedList1 = new ArrayList<>();
    intervalSpeedList1.add(Pair.of("10:00 - 10:15", "10 ml/h"));
    intervalSpeedList1.add(Pair.of("10:15 - 10:30", "20 ml/h"));
    intervalSpeedList1.add(Pair.of("10:30 - 10:45", "30 ml/h"));
    intervalSpeedList1.add(Pair.of("10:45 - 11:00", "40 ml/h"));
    intervalSpeedList1.add(Pair.of("11:00 -", "50 ml/h"));
    records.add(
        createVariableComplexElement(
            seq + 4, createIngredients((seq + 4) * 10 + 4, true, false), intervalSpeedList1, "Group 2", 2));

    return records;
  }

  private static List<TherapyDayElementReportDto> createTherapyDayElementSimpleRecords(final int seq)
  {
    final List<TherapyDayElementReportDto> records = new ArrayList<>();

    records.add(createConstantSimpleElement(seq * 10 + 1, "Group 1", 1));
    records.add(createConstantSimpleElement(seq * 10 + 2, "Group 2", 2));
    records.add(createConstantSimpleElement(seq * 10 + 3, "Group 3", 3));

    final List<Pair<String, String>> timedDoseList1 = new ArrayList<>();
    timedDoseList1.add(Pair.of("09:00", "100 mg"));
    timedDoseList1.add(Pair.of("12:00", "200 mg"));
    timedDoseList1.add(Pair.of("18:30", "300 mg"));
    records.add(createVariableSimpleElement(seq * 10 + 4, timedDoseList1, null, Integer.MAX_VALUE));

    records.add(createConstantSimpleElement(seq * 10 + 5, null, Integer.MAX_VALUE));
    records.add(createConstantSimpleElement(seq * 10 + 6, null, Integer.MAX_VALUE));
    records.add(createConstantSimpleElement(seq * 10 + 7, "Group 1", 1));

    final List<Pair<String, String>> timedDoseList2 = new ArrayList<>();
    timedDoseList2.add(Pair.of("00:00", "0 mg"));
    timedDoseList2.add(Pair.of("03:00", "10 mg"));
    timedDoseList2.add(Pair.of("06:00", "25 mg"));
    timedDoseList2.add(Pair.of("09:00", "50 mg"));
    timedDoseList2.add(Pair.of("12:00", "150 mg"));
    timedDoseList2.add(Pair.of("15:00", "300 mg"));
    timedDoseList2.add(Pair.of("18:00", "500 mg"));
    timedDoseList2.add(Pair.of("21:00", "750 mg"));
    records.add(createVariableSimpleElement(seq * 10 + 8, timedDoseList2, "Group 2", 2));

    records.add(createConstantSimpleElement(seq * 10 + 9, "Group 3", 3));
    records.add(createConstantSimpleElement(seq * 10 + 10, "Group 1", 1));

    return records;
  }

  public static Collection<InfusionIngredientDto> createTherapyDayElementMedicationRecords()
  {
    return createIngredients(0, true, true);
  }

  private static List<InfusionIngredientDto> createIngredients(
      final int seq, final boolean addMedications, final boolean addSolution)
  {
    final List<InfusionIngredientDto> ingredientsList = new ArrayList<>();
    if (addMedications)
    {
      ingredientsList.add(createIngredient(seq * 10 + 1, true));
      if (addSolution)
      {
        ingredientsList.add(createIngredient(seq * 10 + 2, true));
      }
    }
    if (addSolution)
    {
      ingredientsList.add(createIngredient(seq * 10 + 3, false));
    }

    return ingredientsList;
  }

  public static Collection<PatientDataForTherapyReportDto> createTherapyDayPatientRecords()
  {
    final List<PatientDataForTherapyReportDto> records = new ArrayList<>();

    records.add(createPatientData());

    return records;
  }

  public static PatientDataForTherapyReportDto createPatientData()
  {
    return
        createPatientData(
            true,
            "13.34.8888 12:34",
            "Priimek in Ime pacienta",
            "Ulica z zelo dolgim imenom in veliko stevilko 123456789, 123456789 Ljubljana Ljubljana Ljubljana Ljubljana",
            "12.34.1234 / 12l",
            Gender.FEMALE,
            "9999999999999",
            "9999999999999",
            "Oddelek z zelo dolgim imenom in veliko besedami",
            "SOBA999",
            "99.99.9999",
            999,
            "M999",
            "Diagnoza ali bolezen",
            createAllergies("Alergija 1", "Alergija na penicilin in njegove derivate"),
            "20506",
            "presenceOfAllergies");
  }

  private static PatientDataForTherapyReportDto createPatientData(
      final boolean inpatient,
      final String prescriptionDateTime,
      final String patientName,
      final String addressDisplay,
      final String birthDate,
      final Gender gender,
      final String patientIdentificator,
      final String centralCaseIdNumber,
      final String organizationalEntity,
      final String roomAndBed,
      final String admissionDate,
      final int hospitalizationConsecutiveDay,
      final String diseaseTypeCode,
      final String diseaseTypeName,
      final List<String> allergies,
      final String careProviderId,
      final String presenceOfAllergies)
  {
    return
        new PatientDataForTherapyReportDto(
            inpatient,
            prescriptionDateTime,
            patientName,
            birthDate,
            gender,
            "EMSO",
            patientIdentificator,
            centralCaseIdNumber,
            organizationalEntity,
            roomAndBed,
            admissionDate,
            hospitalizationConsecutiveDay,
            createDiseases(),
            "1.500g",
            addressDisplay,
            "cureingCareProfessional",
            "supervisoryCareProfessional",
            "chargeNurse",
            123,
            allergies,
            careProviderId);
  }

  private static List<ExternalCatalogDto> createDiseases()
  {
    final List<ExternalCatalogDto> diseases = new ArrayList<>();
    diseases.add(new ExternalCatalogDto("id1", "disease name 1", "C1"));
    diseases.add(new ExternalCatalogDto("id2", "disease name 2", "C2"));
    return diseases;
  }

  private static PatientDataForTherapyReportDto createPatientDataSurgery(
      final boolean inpatient,
      final String prescriptionDateTime,
      final String patientName,
      final String addressDisplay,
      final String birthDate,
      final Gender gender,
      final String cureingCareProfessional,
      final String patientIdentificator,
      final String centralCaseIdNumber,
      final String organizationalEntity,
      final String roomAndBed,
      final String admissionDate,
      final int hospitalizationConsecutiveDay,
      final List<String> diseaseTypeCode,
      final List<String> diseaseTypeName,
      final List<String> allergies,
      final String careProviderId, final String presenceOfAllergies)
  {
    return
        new PatientDataForTherapyReportDto(
            inpatient,
            prescriptionDateTime,
            patientName,
            birthDate,
            gender,
            "EMSO",
            patientIdentificator,
            centralCaseIdNumber,
            organizationalEntity,
            roomAndBed,
            admissionDate,
            hospitalizationConsecutiveDay,
            createDiseases(),
            "1.500g",
            addressDisplay,
            cureingCareProfessional,
            null,
            null,
            null,
            allergies,
            careProviderId);
  }

  private static List<String> createAllergies(final String... allergies)
  {
    return Arrays.asList(allergies);
  }

  private static List<String> createDiseaseName(final String... allergies)
  {
    return Arrays.asList(allergies);
  }

  private static List<String> createDiseaseCode(final String... allergies)
  {
    return Arrays.asList(allergies);
  }

  private static TherapyDayElementReportDto createConstantSimpleElement(
      final int seq, final String customGroupName, final int customGroupOrder)
  {
    final ConstantSimpleTherapyDto order = createSimpleTherapyDto(seq);

    final TherapyDayElementReportDto simpleElement = new TherapyDayElementReportDto();
    fillTherapyDayElementCommonData(seq, simpleElement, order, customGroupName, customGroupOrder);

    return simpleElement;
  }

  private static TherapyDayElementReportDto createVariableSimpleElement(
      final int seq,
      final List<Pair<String, String>> timedDoseList,
      final String customGroupName,
      final int customGroupOrder)
  {
    final VariableSimpleTherapyDto order = new VariableSimpleTherapyDto();
    fillSimpleOrderCommonData(seq, order);
    for (final Pair<String, String> timedDose : timedDoseList)
    {
      order.getTimedDoseElements().add(createTimedSimpleDoseElementDto(timedDose.getFirst(), timedDose.getSecond()));
    }

    final TherapyDayElementReportDto simpleElement = new TherapyDayElementReportDto();
    fillTherapyDayElementCommonData(seq, simpleElement, order, customGroupName, customGroupOrder);

    return simpleElement;
  }

  private static TherapyDayElementReportDto createConstantComplexElement(
      final int seq,
      final List<InfusionIngredientDto> ingredientsList,
      final String customGroupName,
      final int customGroupOrder)
  {
    final ConstantComplexTherapyDto order = new ConstantComplexTherapyDto();
    order.setIngredientsList(ingredientsList);
    order.setSpeedDisplay(getSequencedDisplay(seq, "Rate"));
    if ((seq & 1) == 0)
    {
      order.setAdditionalInstructionDisplay(getSequencedDisplay(seq, "AdditionalInstruction"));
      order.setBaselineInfusionDisplay("OM");
    }

    final TherapyDayElementReportDto complexElement = new TherapyDayElementReportDto();
    fillTherapyDayElementCommonData(seq, complexElement, order, customGroupName, customGroupOrder);

    return complexElement;
  }

  public static SimpleTherapyDto createSimpleTherapyDto()
  {
    return createSimpleTherapyDto(1);
  }

  private static ConstantSimpleTherapyDto createSimpleTherapyDto(final int seq)
  {
    final ConstantSimpleTherapyDto order = new ConstantSimpleTherapyDto();
    fillSimpleOrderCommonData(seq, order);
    order.setQuantityDisplay(getSequencedDisplay(seq, "SingleDose"));

    return order;
  }

  private static TherapyDayElementReportDto createVariableComplexElement(
      final int seq,
      final List<InfusionIngredientDto> ingredientsList,
      final List<Pair<String, String>> intervalSpeedList,
      final String customGroupName,
      final int customGroupOrder)
  {
    final VariableComplexTherapyDto order = new VariableComplexTherapyDto();
    fillComplexOrderCommonData(seq, order);
    order.setIngredientsList(ingredientsList);
    for (final Pair<String, String> intervalSpeed : intervalSpeedList)
    {
      order.getTimedDoseElements().add(
          createTimedComplexDoseElementDto(intervalSpeed.getFirst(), intervalSpeed.getSecond()));
    }

    final TherapyDayElementReportDto complexElement = new TherapyDayElementReportDto();
    fillTherapyDayElementCommonData(seq, complexElement, order, customGroupName, customGroupOrder);

    return complexElement;
  }

  private static void fillSimpleOrderCommonData(final int seq, final SimpleTherapyDto order)
  {
    fillOrderCommonData(seq, order);
    order.setMedication(createMedication(seq, true));
  }

  private static void fillComplexOrderCommonData(final int seq, final ComplexTherapyDto order)
  {
    fillOrderCommonData(seq, order);
    order.setVolumeSumDisplay(getSequencedDisplay(seq, "VolumeSum"));
  }

  private static void fillOrderCommonData(final int seq, final TherapyDto order)
  {
    order.setFrequencyDisplay(getSequencedDisplay(seq, "F"));
    if ((seq & 1) == 0)
    {
      order.setDaysOfWeekDisplay(getSequencedDisplay(seq, "PO TO SR"));
      order.setWhenNeededDisplay(getSequencedDisplay(seq, "WhenNeeded"));
    }
    else
    {
      order.setStartCriterionDisplay(getSequencedDisplay(seq, "StartCriterion"));
    }
    order.getRoutes().add(createMedicationRouteDto(seq));
    order.setComment(getSequencedDisplay(seq, "Comment Comment Comment Comment Comment Comment Comment"));
    order.setClinicalIndication(createClinicalIndication(seq));
  }

  private static <M extends TherapyDto> void fillTherapyDayElementCommonData(
      final int seq,
      final TherapyDayElementReportDto element,
      final M order,
      final String customGroupName,
      final int customGroupOrder)
  {
    final String seqStr = String.valueOf(seq);
    element.setTherapyConsecutiveDay(getSequencedDisplay(seq, "7"));
    element.setTherapyStart(seqStr + '.' + seqStr + '.' + seqStr + ' ' + seqStr + ':' + seqStr);
    if ((seq & 1) == 0)
    {
      element.setTherapyEnd("*** " + element.getTherapyStart());
    }
    element.setTherapyReportStatusEnum((seq & 2) == 0 ? TherapyReportStatusEnum.ACTIVE : TherapyReportStatusEnum.FINISHED);
    element.setOrder(order);
    element.setCustomGroupName(customGroupName);
    element.setCustomGroupSortOrder(customGroupOrder);
  }

  private static MedicationRouteDto createMedicationRouteDto(final int seq)
  {
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setName(getSequencedDisplay(seq, "Route"));

    return routeDto;
  }

  private static IndicationDto createClinicalIndication(final int seq)
  {
    return new IndicationDto("id" + seq, "name" + seq);
  }

  private static TimedComplexDoseElementDto createTimedComplexDoseElementDto(final String interval, final String speed)
  {
    final TimedComplexDoseElementDto doseElementDto = new TimedComplexDoseElementDto();
    doseElementDto.setIntervalDisplay(interval);
    doseElementDto.setSpeedDisplay(speed);
    return doseElementDto;
  }

  private static TimedSimpleDoseElementDto createTimedSimpleDoseElementDto(final String time, final String quantity)
  {
    final TimedSimpleDoseElementDto doseElementDto = new TimedSimpleDoseElementDto();
    doseElementDto.setTimeDisplay(time);
    doseElementDto.setQuantityDisplay(quantity);
    return doseElementDto;
  }

  private static InfusionIngredientDto createIngredient(final int seq, final boolean forMedication)
  {
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    ingredient.setMedication(createMedication(seq, forMedication));
    ingredient.setQuantityDisplay(getSequencedDisplay(seq, "QD"));

    return ingredient;
  }

  private static MedicationDto createMedication(final int seq, final boolean forMedication)
  {
    final MedicationDto medication = new MedicationDto();
    medication.setName(getSequencedDisplay(seq, forMedication ? "Medication name" : "Solution name"));
    if ((seq & 2) == 0)
    {
      medication.setGenericName(getSequencedDisplay(seq, forMedication ? "Generic medication" : "Generic solution"));
    }
    medication.setMedicationType(forMedication ? MedicationTypeEnum.MEDICATION : MedicationTypeEnum.SOLUTION);

    return medication;
  }

  private static String getSequencedDisplay(final int seq, final String s)
  {
    return seq + "*" + s;
  }
}