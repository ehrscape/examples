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
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition.MedicationDetailSection.ClinicalInterventionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.EhrTerminologyEnum;
import com.marand.thinkmed.medications.InfusionSetChangeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.administration.AdministrationFromEhrConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.converter.therapy.MedicationToEhrConverter;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.IndicationDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSiteDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionBagAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Nejc Korasa
 */
@RunWith(MockitoJUnitRunner.class)
public class AdministrationFromEhrConverterTest
{
  @Mock
  private MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider;

  @Spy
  private AdministrationUtils administrationUtils = new AdministrationUtilsImpl();

  @InjectMocks
  private AdministrationFromEhrConverterImpl administrationFromEhrConverter = new AdministrationFromEhrConverterImpl();

  @Test
  public void testBuildAdministration1()
  {
    final String composer = "composer";
    final double doseNumerator = 10.0;
    final String doseNumeratorUnit = "ml";
    final String comment = "comment";
    final DateTime administrationTime = new DateTime(2014, 2, 4, 12, 0);

    final MedicationActionEnum medicationActionEnum = MedicationActionEnum.ADMINISTER;
    final AdministrationTypeEnum administrationTypeEnum = AdministrationTypeEnum.START;
    final String uidValue = "u1";

    final MedicationAdministrationComposition administrationComposition = MedicationsTestUtils.buildMedicationAdministrationComposition(
        uidValue,
        administrationTime,
        null,
        composer,
        doseNumerator,
        doseNumeratorUnit,
        comment,
        medicationActionEnum,
        administrationTypeEnum);

    final MedicationActionAction.SelfAdministrationType selfAdministrationType = MedicationActionAction.SelfAdministrationType.LEVEL_1;
    administrationComposition.getMedicationDetail().getMedicationAction().get(0).setSelfAdministrationTypeEnum(
        selfAdministrationType);

    final ClinicalInterventionAction clinicalIntervention = new ClinicalInterventionAction();

    final ClinicalInterventionAction.InfusionBagAmountCluster bagChangeDose = new ClinicalInterventionAction.InfusionBagAmountCluster();
    final Double infusionBagQuantity = 15.0;
    final DateTime infusionBagTime = new DateTime(2014, 2, 4, 12, 0);

    bagChangeDose.setQuantity(DataValueUtils.getQuantity(infusionBagQuantity, ""));
    final String infusionBagUnit = "ml";
    bagChangeDose.setDoseUnit(DataValueUtils.getLocalCodedText(infusionBagUnit, infusionBagUnit));

    clinicalIntervention.setInfusionBagAmount(bagChangeDose);
    clinicalIntervention.setTime(DataValueUtils.getDateTime(infusionBagTime));

    administrationComposition.getMedicationDetail().getClinicalIntervention().add(clinicalIntervention);

    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setTherapyDescription("CODE1:NAME1 - 3.0mg 1TBL, 3x per day");
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);
    route.setName("ORAL");
    therapy.getRoutes().add(route);
    final DoseFormDto doseForm = new DoseFormDto();
    doseForm.setCode("T");
    doseForm.setName("TABLET");
    therapy.setDoseForm(doseForm);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 4));
    therapy.setStart(new DateTime(2013, 2, 2, 0, 0));
    therapy.setEnd(Intervals.INFINITE.getEnd());
    therapy.setComment("COMMENT1");
    therapy.setClinicalIndication(new IndicationDto("1", "INDICATION1"));
    therapy.setPastDaysOfTherapy(5);

    final List<String> daysOfWeek = new ArrayList<>();
    daysOfWeek.add("SATURDAY");
    daysOfWeek.add("SUNDAY");
    therapy.setDaysOfWeek(daysOfWeek);

    final MedicationDto medication = new MedicationDto();
    therapy.setMedication(medication);
    medication.setId(1L);
    medication.setName("Lekadol");

    final SimpleDoseElementDto doseElement = new SimpleDoseElementDto();
    doseElement.setQuantity(3.0);
    doseElement.setDoseDescription("1/2");
    therapy.setDoseElement(doseElement);
    therapy.setQuantityUnit("mg");
    therapy.setTitration(TitrationType.BLOOD_SUGAR);

    final MedicationToEhrConverter<?> therapyConverter = MedicationConverterSelector.getConverter(therapy);
    final MedicationInstructionInstruction instruction = therapyConverter.createInstructionFromTherapy(therapy);

    final String therapyId = "t1";
    final AdministrationDto administrationDto = administrationFromEhrConverter.convertToAdministrationDto(
        administrationComposition,
        Pair.of(null, instruction),
        therapyId);

    assertEquals(administrationTime, administrationDto.getAdministrationTime());
    assertEquals(administrationTime, administrationDto.getAdministrationTime());
    assertEquals(administrationDto.getAdministrationType(), administrationTypeEnum);
    assertEquals(AdministrationStatusEnum.COMPLETED, administrationDto.getAdministrationStatus());
    assertEquals(AdministrationResultEnum.SELF_ADMINISTERED, administrationDto.getAdministrationResult());
    assertEquals(administrationDto.getSelfAdministrationType(), selfAdministrationType);
    assertEquals(uidValue, administrationDto.getAdministrationId());
    assertEquals(therapyId, administrationDto.getTherapyId());
    assertEquals(composer, administrationDto.getComposerName());
    assertEquals(comment, administrationDto.getComment());

    assertEquals(
        TherapyDoseTypeEnum.QUANTITY,
        ((StartAdministrationDto)administrationDto).getAdministeredDose().getTherapyDoseTypeEnum());
    assertEquals(
        (int)doseNumerator,
        ((StartAdministrationDto)administrationDto).getAdministeredDose().getNumerator().intValue());
    assertEquals(doseNumeratorUnit, ((StartAdministrationDto)administrationDto).getAdministeredDose().getNumeratorUnit());

    assertEquals(
        ((InfusionBagAdministration)administrationDto).getInfusionBag().getQuantity().intValue(),
        infusionBagQuantity.intValue());
    assertEquals(infusionBagUnit, ((InfusionBagAdministration)administrationDto).getInfusionBag().getUnit());
  }

  @Test
  public void testBuildAdministration2()
  {
    final String composer = "composer";
    final double doseNumerator = 10.0;
    final String doseNumeratorUnit = "ml";
    final String comment = "comment";
    final DateTime administrationTime = new DateTime(2014, 2, 4, 12, 0);

    final MedicationActionEnum medicationActionEnum = MedicationActionEnum.WITHHOLD;
    final AdministrationTypeEnum administrationTypeEnum = AdministrationTypeEnum.START;
    final String uidValue = "u1";

    final MedicationAdministrationComposition administrationComposition = MedicationsTestUtils.buildMedicationAdministrationComposition(
        uidValue,
        administrationTime,
        null,
        composer,
        doseNumerator,
        doseNumeratorUnit,
        comment,
        medicationActionEnum,
        administrationTypeEnum);

    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setTherapyDescription("Taxol 10.0mg, NOSILNA RAZTOPINA: Natrijev Klorid 90ml, 100ml/h, 2x na dan");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setId(1L);
    routeDto.setName("IV");
    therapy.getRoutes().add(routeDto);
    therapy.setContinuousInfusion(true);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3));
    therapy.setDosingDaysFrequency(2);
    therapy.setStart(new DateTime(2013, 2, 2, 9, 0));
    therapy.setEnd(new DateTime(2013, 2, 5, 9, 0));
    therapy.setComment("COMMENT1");
    therapy.setClinicalIndication(new IndicationDto("1", "INDICATION1"));
    therapy.setPastDaysOfTherapy(5);
    therapy.setTitration(TitrationType.INR);

    final MedicationSiteDto siteDto = new MedicationSiteDto();
    siteDto.setCode("1");
    siteDto.setName("CVK");
    therapy.setSite(siteDto);

    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setDuration(1);
    doseElement.setRate(100.0);
    doseElement.setRateUnit("mL/h");
    doseElement.setRateFormula(5.0);
    doseElement.setRateFormulaUnit("mg/kg/h");
    therapy.setDoseElement(doseElement);

    therapy.setVolumeSum(100.0);
    therapy.setVolumeSumUnit("mL");
    final List<InfusionIngredientDto> ingredientsList = new ArrayList<>();
    therapy.setIngredientsList(ingredientsList);
    final InfusionIngredientDto medication = new InfusionIngredientDto();
    ingredientsList.add(medication);
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Taxol");
    medication.setMedication(medicationDto1);
    medication.setQuantity(2.0);
    medication.setQuantityUnit("mg");
    medication.setQuantityDenominator(10.0);
    medication.setQuantityDenominatorUnit("mL");

    final DoseFormDto doseFormDto = new DoseFormDto();
    doseFormDto.setCode("1");
    doseFormDto.setName("IV");
    medication.setDoseForm(doseFormDto);

    final InfusionIngredientDto solution = new InfusionIngredientDto();
    ingredientsList.add(solution);
    final MedicationDto medicationDto2 = new MedicationDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Natrijev klorid Braun 9 mg/mL razt.za inf. vreča 100 mL 1x");
    solution.setMedication(medicationDto2);
    solution.setQuantity(90.0);
    solution.setQuantityUnit("mL");

    final MedicationToEhrConverter<?> converter = MedicationConverterSelector.getConverter(therapy);
    final MedicationInstructionInstruction instruction = converter.createInstructionFromTherapy(therapy);

    final String therapyId = "t1";
    final AdministrationDto administrationDto = administrationFromEhrConverter.convertToAdministrationDto(
        administrationComposition,
        Pair.of(null, instruction),
        therapyId);

    assertEquals(administrationTime, administrationDto.getAdministrationTime());
    assertEquals(administrationTime, administrationDto.getAdministrationTime());
    assertEquals(administrationDto.getAdministrationType(), administrationTypeEnum);
    assertEquals(AdministrationStatusEnum.FAILED, administrationDto.getAdministrationStatus());
    assertEquals(AdministrationResultEnum.NOT_GIVEN, administrationDto.getAdministrationResult());
    assertEquals(uidValue, administrationDto.getAdministrationId());
    assertEquals(therapyId, administrationDto.getTherapyId());
    assertEquals(composer, administrationDto.getComposerName());
    assertEquals(comment, administrationDto.getComment());

    assertEquals(
        TherapyDoseTypeEnum.QUANTITY,
        ((StartAdministrationDto)administrationDto).getAdministeredDose().getTherapyDoseTypeEnum());
    assertEquals(
        (int)doseNumerator,
        ((StartAdministrationDto)administrationDto).getAdministeredDose().getNumerator().intValue());
    assertEquals(doseNumeratorUnit, ((StartAdministrationDto)administrationDto).getAdministeredDose().getNumeratorUnit());
    assertNull(((InfusionBagAdministration)administrationDto).getInfusionBag());
    assertNull(administrationDto.getSelfAdministrationType());

  }
  @Test
  public void testBuildAdministration3()
  {
    final String composer = "composer";
    final double doseNumerator = 10.0;
    final String doseNumeratorUnit = "ml";
    final String comment = "comment";
    final DateTime administrationTime = new DateTime(2014, 2, 4, 12, 0);

    final MedicationActionEnum medicationActionEnum = MedicationActionEnum.WITHHOLD;
    final AdministrationTypeEnum administrationTypeEnum = AdministrationTypeEnum.INFUSION_SET_CHANGE;
    final String uidValue = "u1";

    final MedicationAdministrationComposition administrationComposition = MedicationsTestUtils.buildMedicationAdministrationComposition(
        uidValue,
        administrationTime,
        null,
        composer,
        doseNumerator,
        doseNumeratorUnit,
        comment,
        medicationActionEnum,
        administrationTypeEnum);

    administrationComposition.getMedicationDetail().setMedicationAction(Collections.emptyList());
    administrationComposition.setMedicationDetail(new MedicationAdministrationComposition.MedicationDetailSection());
    final ClinicalInterventionAction clinicalInterventionAction =
        new ClinicalInterventionAction();
    administrationComposition.getMedicationDetail().getClinicalIntervention().add(clinicalInterventionAction);

    final InfusionSetChangeEnum infusionSetChangeEnum = InfusionSetChangeEnum.INFUSION_SYRINGE_CHANGE;
    final String intervention = "intervention";
    clinicalInterventionAction.setIntervention(
        DataValueUtils.getCodedText(EhrTerminologyEnum.PK_NANDA.getEhrName(), infusionSetChangeEnum.getCode(), intervention)
    );


    clinicalInterventionAction.setInterventionUnsuccessful(DataValueUtils.getBoolean(false));
    clinicalInterventionAction.setTime(DataValueUtils.getDateTime(administrationTime));
    clinicalInterventionAction.setComments(DataValueUtils.getText(comment));

    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setTherapyDescription("Taxol 10.0mg, NOSILNA RAZTOPINA: Natrijev Klorid 90ml, 100ml/h, 2x na dan");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setId(1L);
    routeDto.setName("IV");
    therapy.getRoutes().add(routeDto);
    therapy.setContinuousInfusion(true);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3));
    therapy.setDosingDaysFrequency(2);
    therapy.setStart(new DateTime(2013, 2, 2, 9, 0));
    therapy.setEnd(new DateTime(2013, 2, 5, 9, 0));
    therapy.setComment("COMMENT1");
    therapy.setClinicalIndication(new IndicationDto("1", "INDICATION1"));
    therapy.setPastDaysOfTherapy(5);
    therapy.setTitration(TitrationType.INR);

    final MedicationSiteDto siteDto = new MedicationSiteDto();
    siteDto.setCode("1");
    siteDto.setName("CVK");
    therapy.setSite(siteDto);

    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setDuration(1);
    doseElement.setRate(100.0);
    doseElement.setRateUnit("mL/h");
    doseElement.setRateFormula(5.0);
    doseElement.setRateFormulaUnit("mg/kg/h");
    therapy.setDoseElement(doseElement);

    therapy.setVolumeSum(100.0);
    therapy.setVolumeSumUnit("mL");
    final List<InfusionIngredientDto> ingredientsList = new ArrayList<>();
    therapy.setIngredientsList(ingredientsList);
    final InfusionIngredientDto medication = new InfusionIngredientDto();
    ingredientsList.add(medication);
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Taxol");
    medication.setMedication(medicationDto1);
    medication.setQuantity(2.0);
    medication.setQuantityUnit("mg");
    medication.setQuantityDenominator(10.0);
    medication.setQuantityDenominatorUnit("mL");

    final DoseFormDto doseFormDto = new DoseFormDto();
    doseFormDto.setCode("1");
    doseFormDto.setName("IV");
    medication.setDoseForm(doseFormDto);

    final InfusionIngredientDto solution = new InfusionIngredientDto();
    ingredientsList.add(solution);
    final MedicationDto medicationDto2 = new MedicationDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Natrijev klorid Braun 9 mg/mL razt.za inf. vreča 100 mL 1x");
    solution.setMedication(medicationDto2);
    solution.setQuantity(90.0);
    solution.setQuantityUnit("mL");

    final MedicationToEhrConverter<?> converter = MedicationConverterSelector.getConverter(therapy);
    final MedicationInstructionInstruction instruction = converter.createInstructionFromTherapy(therapy);

    final String therapyId = "t1";
    final AdministrationDto administrationDto = administrationFromEhrConverter.convertToAdministrationDto(
        administrationComposition,
        Pair.of(null, instruction),
        therapyId);

    assertEquals(administrationTime, administrationDto.getAdministrationTime());
    assertEquals(administrationTime, administrationDto.getAdministrationTime());
    assertEquals(administrationDto.getAdministrationType(), administrationTypeEnum);
    assertEquals(AdministrationStatusEnum.COMPLETED, administrationDto.getAdministrationStatus());
    assertEquals(uidValue, administrationDto.getAdministrationId());
    assertEquals(therapyId, administrationDto.getTherapyId());
    assertEquals(composer, administrationDto.getComposerName());
    assertEquals(comment, administrationDto.getComment());

    assertEquals(((InfusionSetChangeDto)administrationDto).getInfusionSetChangeEnum(), infusionSetChangeEnum);
    assertNull(((InfusionBagAdministration)administrationDto).getInfusionBag());
    assertNull(administrationDto.getSelfAdministrationType());
  }
}
