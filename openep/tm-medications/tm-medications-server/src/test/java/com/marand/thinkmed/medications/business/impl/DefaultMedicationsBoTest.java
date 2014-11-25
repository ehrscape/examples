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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.marand.maf.core.Pair;
import com.marand.maf.core.openehr.dao.EhrTaggingDao;
import com.marand.maf.core.openehr.dao.openehr.OpenEhrTaggingDao;
import com.marand.maf.core.openehr.util.InstructionTranslator;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrLinkType;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkehr.tagging.dto.TagFilteringDto;
import com.marand.thinkehr.tagging.dto.TaggedObjectDto;
import com.marand.thinkmed.api.core.data.NamedIdentity;
import com.marand.thinkmed.api.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyTag;
import com.marand.thinkmed.medications.TherapyTaggingUtils;
import com.marand.thinkmed.medications.b2b.MedicationsConnector;
import com.marand.thinkmed.medications.converter.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.MedicationToEhrConverter;
import com.marand.thinkmed.medications.dao.EhrMedicationsDao;
import com.marand.thinkmed.medications.dao.hibernate.HibernateMedicationsDao;
import com.marand.thinkmed.medications.dto.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.InfusionRateCalculationDto;
import com.marand.thinkmed.medications.dto.IngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSearchDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.dto.MedicationSiteDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.TherapyCardInfoDto;
import com.marand.thinkmed.medications.dto.TherapyChangeHistoryDto;
import com.marand.thinkmed.medications.dto.TherapyChangeType;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTaskDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTimelineRowDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTimelineRowForContInfusionDto;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Instruction;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.openehr.jaxb.rm.UidBasedId;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.mock.core.MockObject;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster.DayOfWeek;
import static com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition.MedicationReferenceBodyWeightObservation.HistoryHistory.AnyEventEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Mitja Lapajne
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@Transactional(TransactionMode.ROLLBACK)
@SpringApplicationContext(
    {
        "/com/marand/maf_test/unitils/tc-unitils.xml",
        "com/marand/thinkmed/medications/ac-hibernate-packages.xml",
        "com/marand/thinkmed/medications/business/impl/DefaultMedicationsBoTest-context.xml"
    }
)
public class DefaultMedicationsBoTest
{
  @SpringBeanByName
  private DefaultMedicationsBo medicationsBo;

  @Test
  public void testTransformConstantSimpleTherapy()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setTherapyDescription("CODE1:NAME1 - 3.0mg 1TBL, 3x per day");
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setCode("O");
    route.setName("ORAL");
    therapy.setRoute(route);
    final DoseFormDto doseForm = new DoseFormDto();
    doseForm.setCode("T");
    doseForm.setName("TABLET");
    therapy.setDoseForm(doseForm);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 4));
    therapy.setStart(new DateTime(2013, 2, 2, 0, 0));
    therapy.setEnd(Intervals.INFINITE.getEnd());
    therapy.setComment("COMMENT1");
    therapy.setClinicalIndication("INDICATION1");
    therapy.setPastDaysOfTherapy(5);

    final List<String> daysOfWeek = new ArrayList<>();
    daysOfWeek.add("SATURDAY");
    daysOfWeek.add("SUNDAY");
    therapy.setDaysOfWeek(daysOfWeek);

    final MedicationDto medication = new MedicationDto();
    therapy.setMedication(medication);
    medication.setId(1L);
    medication.setName("Lekadol");
    final IngredientDto ingredient = new IngredientDto();
    ingredient.setId(1L);
    ingredient.setName("paracetamol");
    ingredient.setCode("PA");
    final SimpleDoseElementDto doseElement = new SimpleDoseElementDto();
    doseElement.setQuantity(3.0);
    doseElement.setDoseDescription("1/2");
    therapy.setDoseElement(doseElement);
    therapy.setQuantityUnit("mg");

    final MedicationToEhrConverter<?> converter = MedicationConverterSelector.getConverter(therapy);
    final MedicationInstructionInstruction instruction = converter.createInstructionFromTherapy(therapy);

    final OrderActivity orderActivity = instruction.getOrder().get(0);
    //narrative
    assertEquals("CODE1:NAME1 - 3.0mg 1TBL, 3x per day", instruction.getNarrative().getValue());
    //description
    assertEquals("1", ((DvCodedText)orderActivity.getMedicine()).getDefiningCode().getCodeString());
    assertEquals("Lekadol", orderActivity.getMedicine().getValue());
    assertEquals("CODE1:NAME1 - 3.0mg 1TBL, 3x per day", orderActivity.getDirections().getValue());
    //timing
    assertNull(orderActivity.getMedicationTiming().getNumberOfAdministrations());
    assertEquals(
        DataValueUtils.getDuration(0, 0, 0, 4, 0, 0), orderActivity.getMedicationTiming().getTiming().getInterval());
    assertEquals(
        DataValueUtils.getDateTime(new DateTime(2013, 2, 2, 0, 0)), orderActivity.getMedicationTiming().getStartDate());
    assertEquals(
        DataValueUtils.getDateTime(Intervals.INFINITE.getEnd()), orderActivity.getMedicationTiming().getStopDate());
    final DayOfWeek dayOfWeek1 = DataValueUtils.getTerminologyEnum(
        DayOfWeek.class, orderActivity.getMedicationTiming().getTiming().getDayOfWeek().get(0));
    assertEquals(DayOfWeek.SATURDAY, dayOfWeek1);
    final DayOfWeek dayOfWeek2 = DataValueUtils.getTerminologyEnum(
        DayOfWeek.class, orderActivity.getMedicationTiming().getTiming().getDayOfWeek().get(1));
    assertEquals(DayOfWeek.SUNDAY, dayOfWeek2);
    assertEquals(5, orderActivity.getPastDaysOfTherapy().getMagnitude());
    //comment
    assertEquals("COMMENT1", orderActivity.getComment().get(0).getValue());
    //clinical indication
    assertEquals("INDICATION1", orderActivity.getClinicalIndication().get(0).getValue());
    //route
    assertEquals("ORAL", orderActivity.getAdministrationDetails().getRoute().get(0).getValue());
    assertEquals("O", orderActivity.getAdministrationDetails().getRoute().get(0).getDefiningCode().getCodeString());
    //quantity
    assertEquals(DataValueUtils.getQuantity(3.0, ""), orderActivity.getStructuredDose().getQuantity());
    assertEquals(DataValueUtils.getLocalCodedText("mg", "mg"), orderActivity.getStructuredDose().getDoseUnit());
    assertEquals("1/2", orderActivity.getStructuredDose().getDescription().getValue());
    //form
    assertEquals("TABLET", orderActivity.getIngredientsAndForm().getForm().getValue());
  }

  @Test
  public void testTransformConstantComplexTherapy()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setTherapyDescription("Taxol 10.0mg, NOSILNA RAZTOPINA: Natrijev Klorid 90ml, 100ml/h, 2x na dan");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setCode("1");
    routeDto.setName("IV");
    therapy.setRoute(routeDto);
    therapy.setContinuousInfusion(true);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3));
    therapy.setDosingDaysFrequency(2);
    therapy.setStart(new DateTime(2013, 2, 2, 9, 0));
    therapy.setEnd(new DateTime(2013, 2, 5, 9, 0));
    therapy.setComment("COMMENT1");
    therapy.setClinicalIndication("INDICATION1");
    therapy.setPastDaysOfTherapy(5);

    final MedicationSiteDto siteDto = new MedicationSiteDto();
    siteDto.setCode("1");
    siteDto.setName("CVK");
    therapy.setSite(siteDto);

    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setDuration(1);
    doseElement.setRate(100.0);
    doseElement.setRateUnit("ml/h");
    doseElement.setRateFormula(5.0);
    doseElement.setRateFormulaUnit("mg/kg/h");
    therapy.setDoseElement(doseElement);

    therapy.setVolumeSum(100.0);
    therapy.setVolumeSumUnit("ml");
    final List<InfusionIngredientDto> ingredientsList = new ArrayList<InfusionIngredientDto>();
    therapy.setIngredientsList(ingredientsList);
    final InfusionIngredientDto medication = new InfusionIngredientDto();
    ingredientsList.add(medication);
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Taxol");
    medication.setMedication(medicationDto1);
    medication.setQuantity(2.0);
    medication.setQuantityUnit("mg");
    medication.setVolume(10.0);
    medication.setVolumeUnit("ml");

    final DoseFormDto doseFormDto = new DoseFormDto();
    doseFormDto.setCode("1");
    doseFormDto.setName("IV");
    medication.setDoseForm(doseFormDto);

    final InfusionIngredientDto solution = new InfusionIngredientDto();
    ingredientsList.add(solution);
    final MedicationDto medicationDto2 = new MedicationDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Natrijev klorid Braun 9 mg/ml razt.za inf. vreča 100 ml 1x");
    solution.setMedication(medicationDto2);
    solution.setQuantity(90.0);
    solution.setQuantityUnit("ml");

    final MedicationToEhrConverter<?> converter = MedicationConverterSelector.getConverter(therapy);
    final MedicationInstructionInstruction instruction = converter.createInstructionFromTherapy(therapy);

    final OrderActivity orderActivity = instruction.getOrder().get(0);
    //narrative
    assertEquals(
        "Taxol 10.0mg, NOSILNA RAZTOPINA: Natrijev Klorid 90ml, 100ml/h, 2x na dan", instruction.getNarrative().getValue());
    //description
    assertEquals(
        "Taxol 10.0mg, NOSILNA RAZTOPINA: Natrijev Klorid 90ml, 100ml/h, 2x na dan",
        orderActivity.getMedicine().getValue());
    assertEquals(
        "Taxol 10.0mg, NOSILNA RAZTOPINA: Natrijev Klorid 90ml, 100ml/h, 2x na dan",
        orderActivity.getDirections().getValue());
    //timing
    assertNull(orderActivity.getMedicationTiming().getNumberOfAdministrations());
    assertEquals(3L, orderActivity.getMedicationTiming().getTiming().getDailyCount().getMagnitude());
    assertEquals(
        DataValueUtils.getDuration(0, 0, 2, 0, 0, 0), orderActivity.getMedicationTiming().getTiming().getInterval());
    assertEquals(
        DataValueUtils.getDateTime(new DateTime(2013, 2, 2, 9, 0)), orderActivity.getMedicationTiming().getStartDate());
    assertEquals(
        DataValueUtils.getDateTime(new DateTime(2013, 2, 5, 9, 0)), orderActivity.getMedicationTiming().getStopDate());
    assertEquals(5, orderActivity.getPastDaysOfTherapy().getMagnitude());
    //comment
    assertEquals("COMMENT1", orderActivity.getComment().get(0).getValue());
    //clinical indication
    assertEquals("INDICATION1", orderActivity.getClinicalIndication().get(0).getValue());
    //route
    assertEquals("IV", orderActivity.getAdministrationDetails().getRoute().get(0).getValue());
    assertEquals("1", orderActivity.getAdministrationDetails().getRoute().get(0).getDefiningCode().getCodeString());
    //continuous infusion
    assertEquals(
        MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION),
        orderActivity.getAdministrationDetails().getDeliveryMethod().getValue());
    //infusion details
    final InfusionAdministrationDetailsCluster infusionAdministrationDetailsCluster =
        orderActivity.getAdministrationDetails().getInfusionAdministrationDetails().get(0);
    assertEquals(
        new Double(100.0),
        (Double)((DvQuantity)infusionAdministrationDetailsCluster.getDoseAdministrationRate()).getMagnitude());
    assertEquals(
        "ml/h", ((DvQuantity)infusionAdministrationDetailsCluster.getDoseAdministrationRate()).getUnits());
    assertEquals(
        new Double(5.0),
        (Double)((DvQuantity)infusionAdministrationDetailsCluster.getDoseAdministrationFormula()).getMagnitude());
    assertEquals(
        "mg/kg/h", ((DvQuantity)infusionAdministrationDetailsCluster.getDoseAdministrationFormula()).getUnits());
    //quantity and strength
    assertEquals(DataValueUtils.getQuantity(100.0, ""), orderActivity.getStructuredDose().getQuantity());
    assertEquals(DataValueUtils.getLocalCodedText("ml", "ml"), orderActivity.getStructuredDose().getDoseUnit());
    assertNull(orderActivity.getStructuredDose().getDescription());
    final List<IngredientsAndFormCluster.IngredientCluster> ingredient = orderActivity.getIngredientsAndForm()
        .getIngredient();
    //medication quantity and strength
    assertEquals("1", ((DvCodedText)ingredient.get(0).getName()).getDefiningCode().getCodeString());
    assertEquals("Taxol", ingredient.get(0).getName().getValue());
    final IngredientCluster.IngredientQuantityCluster.RatioNumeratorCluster ratioNumerator1 =
        ingredient.get(0).getIngredientQuantity().getRatioNumerator();
    assertEquals((Double)2.0, (Double)ratioNumerator1.getAmount().getMagnitude());
    assertEquals("mg", ratioNumerator1.getDoseUnit().getDefiningCode().getCodeString());
    assertEquals("mg", ratioNumerator1.getDoseUnit().getValue());
    final IngredientCluster.IngredientQuantityCluster.RatioDenominatorCluster ratioDenominator1 =
        ingredient.get(0).getIngredientQuantity().getRatioDenominator();
    assertEquals((Double)10.0, (Double)ratioDenominator1.getAmount().getMagnitude());
    assertEquals("ml", ratioDenominator1.getDoseUnit().getDefiningCode().getCodeString());
    assertEquals("ml", ratioDenominator1.getDoseUnit().getValue());
    //solution quantity and strength
    assertEquals("2", ((DvCodedText)ingredient.get(1).getName()).getDefiningCode().getCodeString());
    assertEquals("Natrijev klorid Braun 9 mg/ml razt.za inf. vreča 100 ml 1x", ingredient.get(1).getName().getValue());
  }

  @Test
  public void testGetFirstInstruction()
  {
    // 1 --> 2 --> 3
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", null, null);
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid2::1", null, null);
    final MedicationOrderComposition composition3 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid3::1", null, null);

    final MedicationInstructionInstruction instruction1 = MedicationsTestUtils.
        buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 2");
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);
    final MedicationInstructionInstruction instruction3 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 3");
    composition3.getMedicationDetail().getMedicationInstruction().add(instruction3);

    final Link linkToInstruction1 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition1, instruction1);
    instruction2.getLinks().add(linkToInstruction1);
    final Link linkToInstruction2 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition2, instruction2);
    instruction3.getLinks().add(linkToInstruction2);

    //mock
    final MockObject<EhrMedicationsDao> ehrDaoMock = new MockObject<>(EhrMedicationsDao.class, this);
    ehrDaoMock.returns(composition1).loadMedicationOrderComposition(1L, "uid1");
    ehrDaoMock.returns(composition2).loadMedicationOrderComposition(1L, "uid2");
    ehrDaoMock.returns(composition3).loadMedicationOrderComposition(1L, "uid3");
    ehrDaoMock.returns(composition1).loadMedicationOrderComposition(1L, "uid1::1");
    ehrDaoMock.returns(composition2).loadMedicationOrderComposition(1L, "uid2::1");
    ehrDaoMock.returns(composition3).loadMedicationOrderComposition(1L, "uid3::1");
    medicationsBo.setEhrMedicationsDao(ehrDaoMock.getMock());

    final MedicationInstructionInstruction firstInstruction = medicationsBo.getFirstInstruction(1L, instruction3);
    assertEquals(firstInstruction, instruction1);
  }

  @Test
  public void testDoesInstructionHaveUpdateLinkToCompareInstruction()
  {
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", null, null);
    final MedicationOrderComposition composition2 = MedicationsTestUtils.
        buildTestMedicationOrderComposition("uid2::1", null, null);

    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 2");
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);

    final Link linkToInstruction1 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition1, instruction1);
    instruction2.getLinks().add(linkToInstruction1);

    assertTrue(
        medicationsBo.doesInstructionHaveLinkToCompareInstruction(
            instruction2, Pair.of(composition1, instruction1), OpenEhrLinkType.UPDATE));
  }

  @Test
  public void testAreInstructionsLinkedByUpdate()
  {
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", null, null);
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid2::1", null, null);

    final MedicationInstructionInstruction instruction1 = MedicationsTestUtils.
        buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 2");
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);

    final Link linkToInstruction1 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition1, instruction1);
    instruction2.getLinks().add(linkToInstruction1);

    assertTrue(
        medicationsBo.areInstructionsLinkedByUpdate(
            Pair.of(composition2, instruction2),
            Pair.of(composition1, instruction1))
    );
    assertTrue(
        medicationsBo.areInstructionsLinkedByUpdate(
            Pair.of(composition1, instruction1),
            Pair.of(composition2, instruction2))
    );
  }

  @Test
  public void testAreInstructionsSimilarSimple()
  {
    // therapy
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2013, 11, 11, 0, 0, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction.getOrder().add(orderActivity);
    final MedicationTimingCluster medicationTiming =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2013, 11, 11, 0, 0, 0), null, null);
    orderActivity.setMedicationTiming(medicationTiming);

    //compare therapy
    final MedicationOrderComposition compareComposition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid2::1", new DateTime(2013, 10, 10, 0, 0, 0), null);
    final MedicationInstructionInstruction compareInstruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    compareComposition.getMedicationDetail().getMedicationInstruction().add(compareInstruction);
    final OrderActivity compareOrderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    compareInstruction.getOrder().add(compareOrderActivity);
    final MedicationTimingCluster compareMedicationTiming =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2013, 10, 10, 0, 0, 0), new DateTime(2013, 11, 11, 0, 0, 0), null);
    compareOrderActivity.setMedicationTiming(compareMedicationTiming);

    //medications data map
    final Map<Long, MedicationDataForTherapyDto> medicationsMap = new HashMap<>();

    final MedicationDataForTherapyDto medicationData1 = new MedicationDataForTherapyDto();
    medicationData1.setGenericName("Paracetamol");
    medicationData1.setCustomGroupName("Group 1");
    medicationData1.setAtcCode("ATC 1");
    medicationsMap.put(1L, medicationData1);

    final MedicationDataForTherapyDto medicationData2 = new MedicationDataForTherapyDto();
    medicationData2.setGenericName("Paracetamol");
    medicationData2.setCustomGroupName("Group 1");
    medicationData2.setAtcCode("ATC 1");
    medicationsMap.put(2L, medicationData2);

    assertTrue(
        medicationsBo.areTherapiesSimilar(
            Pair.of(composition, instruction),
            Pair.of(compareComposition, compareInstruction),
            medicationsMap,
            false)
    );
  }

  @Test
  public void testAreInstructionsSimilarComplex()
  {
    // therapy
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2013, 11, 11, 0, 0, 0), null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            null, null, 500.0, "ml", null, null, null, null, "IV", "IV", null, null);
    instruction.getOrder().add(orderActivity);
    final MedicationTimingCluster medicationTiming =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2013, 11, 11, 0, 0, 0), null, null);
    orderActivity.setMedicationTiming(medicationTiming);
    final IngredientsAndFormCluster ingredientsAndForm = new IngredientsAndFormCluster();
    orderActivity.setIngredientsAndForm(ingredientsAndForm);
    final IngredientCluster ingredient1 =
        MedicationsTestUtils.buildTestActiveIngredient("Dopamin1", "1", null, null, 10.0, "mg", 500.0, "ml");
    ingredientsAndForm.getIngredient().add(ingredient1);

    //compare therapy
    final MedicationOrderComposition compareComposition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid2::1", new DateTime(2013, 10, 10, 0, 0, 0), null);
    final MedicationInstructionInstruction compareInstruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    compareComposition.getMedicationDetail().getMedicationInstruction().add(compareInstruction);
    final OrderActivity compareOrderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            null, null, 1000.0, "ml", null, null, null, null, "IV", "IV", null, null);
    compareInstruction.getOrder().add(compareOrderActivity);
    final MedicationTimingCluster compareMedicationTiming =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2013, 10, 10, 0, 0, 0), new DateTime(2013, 11, 11, 0, 0, 0), null);
    compareOrderActivity.setMedicationTiming(compareMedicationTiming);
    final IngredientsAndFormCluster compareIngredientsAndForm = new IngredientsAndFormCluster();
    compareOrderActivity.setIngredientsAndForm(compareIngredientsAndForm);
    final IngredientCluster compareIngredient1 =
        MedicationsTestUtils.buildTestActiveIngredient("Dopamin2", "1", null, null, 10.0, "mg", 1000.0, "ml");
    compareIngredientsAndForm.getIngredient().add(compareIngredient1);

    //medications data map
    final Map<Long, MedicationDataForTherapyDto> medicationsMap = new HashMap<>();

    final MedicationDataForTherapyDto medicationData1 = new MedicationDataForTherapyDto();
    medicationData1.setGenericName("Dopamin");
    medicationData1.setCustomGroupName("Group 1");
    medicationData1.setAtcCode("ATC 1");
    medicationsMap.put(1L, medicationData1);

    final MedicationDataForTherapyDto medicationData2 = new MedicationDataForTherapyDto();
    medicationData2.setGenericName("Dopamin");
    medicationData2.setCustomGroupName("Group 1");
    medicationData2.setAtcCode("ATC 1");
    medicationsMap.put(2L, medicationData2);

    assertTrue(
        medicationsBo.areTherapiesSimilar(
            Pair.of(composition, instruction),
            Pair.of(compareComposition, compareInstruction),
            medicationsMap,
            false)
    );
  }

  @Test
  public void testGetTherapiesForDocumentation()
  {
    final DateTime when = new DateTime(2014, 6, 16, 12, 0, 0);

    //mock
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    final MockObject<HibernateMedicationsDao> medicationsDaoMock = new MockObject<>(HibernateMedicationsDao.class, this);
    medicationsDaoMock.returns(medicationDto1).getMedicationById(1L, when);
    medicationsBo.setMedicationsDao(medicationsDaoMock.getMock());

    final Map<Long, MedicationDataForTherapyDto> medicationsDataMap = new HashMap<>();
    final MedicationDataForTherapyDto medicationData1 = new MedicationDataForTherapyDto();
    medicationData1.setGenericName("Paracetamol");
    medicationData1.setCustomGroupName("Group 1");
    medicationData1.setAtcCode("ATC 1");
    medicationsDataMap.put(1L, medicationData1);

    //similar to admission
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2013, 11, 11, 0, 0, 0), "aaa");
    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction1.getOrder().add(orderActivity1);
    final MedicationTimingCluster medicationTiming1 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2014, 6, 9, 13, 0, 0), new DateTime(2014, 6, 11, 13, 0, 0), null);
    orderActivity1.setMedicationTiming(medicationTiming1);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs1 = new ArrayList<>();
    instructionPairs1.add(Pair.of(composition1, instruction1));
    final DocumentationTherapiesDto groups1 =
        medicationsBo.getTherapiesForDocumentation(
            null,
            null,
            Intervals.infiniteFrom(new DateTime(2014, 6, 9, 12, 0, 0)),
            instructionPairs1,
            medicationsDataMap,
            false,
            when,
            new Locale("en"));

    assertEquals(1L, groups1.getAdmissionTherapies().size());

    //similar to discharge
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2013, 11, 11, 0, 0, 0), "aaa");
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);
    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 1000.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction2.getOrder().add(orderActivity2);
    final MedicationTimingCluster medicationTiming2 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2014, 6, 11, 13, 0, 0), new DateTime(2014, 6, 12, 13, 0, 0), null);
    orderActivity2.setMedicationTiming(medicationTiming2);

    instructionPairs1.add(Pair.of(composition2, instruction2));

    final DocumentationTherapiesDto groups2 =
        medicationsBo.getTherapiesForDocumentation(
            null,
            null,
            Intervals.infiniteFrom(new DateTime(2014, 6, 9, 12, 0, 0)),
            instructionPairs1,
            medicationsDataMap,
            false,
            when,
            new Locale("en"));
    assertEquals(1L, groups2.getAdmissionTherapies().size());

    //linked to discharge
    final MedicationOrderComposition composition3 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2013, 11, 11, 0, 0, 0), "aaa");
    final MedicationInstructionInstruction instruction3 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition3.getMedicationDetail().getMedicationInstruction().add(instruction3);
    final OrderActivity orderActivity3 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction3.getOrder().add(orderActivity3);
    final MedicationTimingCluster medicationTiming3 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2014, 6, 12, 10, 0, 0), null, null);
    orderActivity3.setMedicationTiming(medicationTiming3);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs2 = new ArrayList<>();
    instructionPairs2.add(Pair.of(composition2, instruction2));
    instructionPairs2.add(Pair.of(composition3, instruction3));

    final DocumentationTherapiesDto groups3 =
        medicationsBo.getTherapiesForDocumentation(
            null,
            null,
            Intervals.infiniteFrom(new DateTime(2014, 6, 9, 12, 0, 0)),
            instructionPairs2,
            medicationsDataMap,
            false,
            when,
            new Locale("en"));

    assertEquals(1L, groups3.getDischargeTherapies().size());
    assertEquals(0L, groups3.getAdmissionTherapies().size());

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs3 = new ArrayList<>();
    final Link linkToInstruction1 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition2, instruction2);
    instruction2.getLinks().add(linkToInstruction1);
    final Link linkToInstruction2 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition3, instruction3);
    instruction3.getLinks().add(linkToInstruction2);

    instructionPairs3.add(Pair.of(composition2, instruction2));
    instructionPairs3.add(Pair.of(composition3, instruction3));

    final DocumentationTherapiesDto groups4 =
        medicationsBo.getTherapiesForDocumentation(
            null,
            null,
            new Interval(new DateTime(2014, 6, 9, 12, 0, 0), new DateTime(2014, 6, 18, 12, 0, 0)),
            instructionPairs2,
            medicationsDataMap,
            false,
            when,
            new Locale("en"));

    assertEquals(1L, groups4.getDischargeTherapies().size());
    assertEquals(0L, groups4.getTherapies().size());


    //linked regular
    final MedicationOrderComposition composition4 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2013, 11, 11, 0, 0, 0), "aaa");
    final MedicationInstructionInstruction instruction4 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition4.getMedicationDetail().getMedicationInstruction().add(instruction4);
    final OrderActivity orderActivity4 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction4.getOrder().add(orderActivity4);
    final MedicationTimingCluster medicationTiming4 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2014, 6, 12, 10, 0, 0), new DateTime(2014, 6, 15, 10, 0, 0), null);
    orderActivity4.setMedicationTiming(medicationTiming4);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs4 = new ArrayList<>();
    final Link linkToInstruction11 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition2, instruction2);
    instruction2.getLinks().add(linkToInstruction11);
    final Link linkToInstruction22 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition4, instruction4);
    instruction3.getLinks().add(linkToInstruction22);

    instructionPairs4.add(Pair.of(composition2, instruction2));
    instructionPairs4.add(Pair.of(composition4, instruction4));

    final DocumentationTherapiesDto groups5 =
        medicationsBo.getTherapiesForDocumentation(
            null,
            null,
            new Interval(new DateTime(2014, 6, 9, 12, 0, 0), new DateTime(2014, 6, 18, 12, 0, 0)),
            instructionPairs4,
            medicationsDataMap,
            false,
            when,
            new Locale("en"));

    assertEquals(1L, groups5.getTherapies().size());
  }

  @Test
  public void testExtractWarningsSearchDtos()
  {
    //problems with date conversion
    final DateTime start =
        DataValueUtils.getDateTime(DataValueUtils.getDateTime(new DateTime(2013, 3, 25, 12, 0, DateTimeZone.UTC)));

    //mock
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    final MedicationDto medicationDto2 = new MedicationDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Primotren 20x500mg");
    final MockObject<HibernateMedicationsDao> medicationsDaoMock =
        new MockObject<>(HibernateMedicationsDao.class, this);
    medicationsDaoMock.returns(medicationDto1).getMedicationById(1L, start);
    medicationsDaoMock.returns(medicationDto2).getMedicationById(2L, start);
    medicationsBo.setMedicationsDao(medicationsDaoMock.getMock());

    //simple therapy
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", null, null);
    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    final MedicationTimingCluster medicationTimingCluster =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, null, null);

    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction1.getOrder().add(orderActivity1);
    orderActivity1.setMedicationTiming(medicationTimingCluster);
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList1 =
        new ArrayList<>();
    medicationInstructionsList1.add(Pair.of(composition1, instruction1));
    final List<MedicationForWarningsSearchDto> warningsSearchDtosList1 =
        medicationsBo.extractWarningsSearchDtos(medicationInstructionsList1, start);
    assertEquals((Double)500.0, warningsSearchDtosList1.get(0).getDoseAmount());
    assertEquals("mg", warningsSearchDtosList1.get(0).getDoseUnit());
    assertEquals("O", warningsSearchDtosList1.get(0).getRouteCode());

    //complex therapy
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", null, null);
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");

    final IngredientCluster ingredient =
        MedicationsTestUtils.buildTestActiveIngredient("Primotren", "0002", null, null, 10.0, "mg", 3.0, "ml");
    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(null, null, null, null, null, null, null, null, "O", "ORAL", "1", "TBL");
    instruction2.getOrder().add(orderActivity2);
    final IngredientsAndFormCluster ingredientsAndForm = new IngredientsAndFormCluster();
    orderActivity2.setIngredientsAndForm(ingredientsAndForm);
    ingredientsAndForm.getIngredient().add(ingredient);
    orderActivity2.setMedicationTiming(medicationTimingCluster);
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList2 =
        new ArrayList<>();
    medicationInstructionsList2.add(Pair.of(composition2, instruction2));
    final List<MedicationForWarningsSearchDto> warningsSearchDtosList2 =
        medicationsBo.extractWarningsSearchDtos(medicationInstructionsList2, start);
    assertEquals((Double)10.0, warningsSearchDtosList2.get(0).getDoseAmount());
    assertEquals("mg", warningsSearchDtosList2.get(0).getDoseUnit());
    assertEquals("O", warningsSearchDtosList2.get(0).getRouteCode());
  }

  @Test
  public void testBuildWarningSearchDtoFromIngredient()
  {
    //problems with date conversion
    final DateTime start =
        DataValueUtils.getDateTime(DataValueUtils.getDateTime(new DateTime(2013, 3, 25, 12, 0, DateTimeZone.UTC)));

    //mock
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    final MedicationDto medicationDto2 = new MedicationDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Primotren 20x500mg");
    final MockObject<HibernateMedicationsDao> medicationsDaoMock =
        new MockObject<HibernateMedicationsDao>(HibernateMedicationsDao.class, this);
    medicationsDaoMock.returns(medicationDto1).getMedicationById(1L, start);
    medicationsDaoMock.returns(medicationDto2).getMedicationById(2L, start);
    medicationsBo.setMedicationsDao(medicationsDaoMock.getMock());

    final MedicationTimingCluster medicationTimingCluster =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, null, null);

    final IngredientCluster ingredient1 =
        MedicationsTestUtils.buildTestActiveIngredient("Lekadol", "0001", 500.0, "mg", null, null, null, null);
    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(null, null, null, null, null, null, null, null, "O", "ORAL", "1", "TBL");
    orderActivity1.setMedicationTiming(medicationTimingCluster);
    final MedicationForWarningsSearchDto warningsSearchDto1 =
        medicationsBo.buildWarningSearchDtoFromIngredient(orderActivity1, ingredient1, start);
    assertEquals((Double)500.0, warningsSearchDto1.getDoseAmount());
    assertEquals("mg", warningsSearchDto1.getDoseUnit());
    assertEquals("O", warningsSearchDto1.getRouteCode());

    final IngredientCluster ingredient2 =
        MedicationsTestUtils.buildTestActiveIngredient("Primotren", "0002", null, null, 10.0, "mg", 3.0, "ml");
    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(null, null, null, null, null, null, null, null, "O", "ORAL", "1", "TBL");
    orderActivity2.setMedicationTiming(medicationTimingCluster);
    final MedicationForWarningsSearchDto warningsSearchDto2 =
        medicationsBo.buildWarningSearchDtoFromIngredient(orderActivity2, ingredient2, start);
    assertEquals((Double)10.0, warningsSearchDto2.getDoseAmount());
    assertEquals("mg", warningsSearchDto2.getDoseUnit());
    assertEquals("O", warningsSearchDto2.getRouteCode());
  }

  @Test
  public void testBuildWarningSearchDtoFromMedication()
  {
    //problems with date conversion
    final DateTime start =
        DataValueUtils.getDateTime(DataValueUtils.getDateTime(new DateTime(2013, 3, 25, 12, 0, DateTimeZone.UTC)));

    //mock
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    final MedicationDto medicationDto2 = new MedicationDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Primotren 20x500mg");
    final MockObject<HibernateMedicationsDao> medicationsDaoMock =
        new MockObject<HibernateMedicationsDao>(HibernateMedicationsDao.class, this);
    medicationsDaoMock.returns(medicationDto1).getMedicationById(1L, start);
    medicationsDaoMock.returns(medicationDto2).getMedicationById(2L, start);
    medicationsBo.setMedicationsDao(medicationsDaoMock.getMock());

    final MedicationTimingCluster medicationTimingCluster =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, null, null);

    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    orderActivity1.setMedicationTiming(medicationTimingCluster);
    final MedicationForWarningsSearchDto warningsSearchDto1 =
        medicationsBo.buildWarningSearchDtoFromMedication(orderActivity1, start);
    assertEquals((Double)500.0, warningsSearchDto1.getDoseAmount());
    assertEquals("mg", warningsSearchDto1.getDoseUnit());
    assertEquals("O", warningsSearchDto1.getRouteCode());

    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Primotren", 1L, null, null, 10.0, "mg", 3.0, "ml", "O", "ORAL", "1", "TBL");
    orderActivity2.setMedicationTiming(medicationTimingCluster);
    final MedicationForWarningsSearchDto warningsSearchDto2 =
        medicationsBo.buildWarningSearchDtoFromMedication(orderActivity2, start);
    assertEquals((Double)10.0, warningsSearchDto2.getDoseAmount());
    assertEquals("mg", warningsSearchDto2.getDoseUnit());
    assertEquals("O", warningsSearchDto2.getRouteCode());
  }

  @Test
  public void testBuildWarningSearchDto()
  {
    //problems with date conversion
    final DateTime start =
        DataValueUtils.getDateTime(DataValueUtils.getDateTime(new DateTime(2013, 3, 25, 12, 0, DateTimeZone.UTC)));

    //mock
    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(1L);
    medicationDto.setName("Lekadol 20x500mg");
    final MockObject<HibernateMedicationsDao> medicationsDaoMock =
        new MockObject<HibernateMedicationsDao>(HibernateMedicationsDao.class, this);
    medicationsDaoMock.returns(medicationDto).getMedicationById(1L, start);
    medicationsBo.setMedicationsDao(medicationsDaoMock.getMock());

    //end not set
    final DateTime stop1 = null;
    final MedicationTimingCluster medicationTimingCluster1 =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, stop1, null);
    final MedicationForWarningsSearchDto warningsSearchDto1 =
        medicationsBo.buildWarningSearchDto(medicationTimingCluster1, 1L, "ORAL", 500.0, "mg", start);
    assertEquals(4L, (long)warningsSearchDto1.getFrequency());
    assertEquals("ORAL", warningsSearchDto1.getRouteCode());
    assertEquals(Double.valueOf(500.0), warningsSearchDto1.getDoseAmount());
    assertEquals("mg", warningsSearchDto1.getDoseUnit());
    assertEquals("Lekadol 20x500mg", warningsSearchDto1.getDescription());
    assertEquals(1L, warningsSearchDto1.getId());
    assertEquals(
        new DateTime(2013, 3, 25, 12, 0, DateTimeZone.UTC).getMillis(),
        warningsSearchDto1.getEffective().getStart().getMillis());
    assertEquals(Intervals.INFINITE.getEnd().getMillis(), warningsSearchDto1.getEffective().getEnd().getMillis());

    //end set
    final DateTime stop2 = new DateTime(2013, 3, 30, 12, 0);
    final MedicationTimingCluster medicationTimingCluster2 =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, stop2, null);
    final MedicationForWarningsSearchDto warningsSearchDto2 =
        medicationsBo.buildWarningSearchDto(medicationTimingCluster2, 1L, "ORAL", 500.0, "mg", start);
    assertEquals(4L, (long)warningsSearchDto2.getFrequency());
    assertEquals("ORAL", warningsSearchDto2.getRouteCode());
    assertEquals(Double.valueOf(500.0), warningsSearchDto2.getDoseAmount());
    assertEquals("mg", warningsSearchDto2.getDoseUnit());
    assertEquals("Lekadol 20x500mg", warningsSearchDto2.getDescription());
    assertEquals(1L, warningsSearchDto2.getId());
    assertEquals(start, warningsSearchDto2.getEffective().getStart());
    assertEquals(stop2.getMillis(), warningsSearchDto2.getEffective().getEnd().getMillis());
  }

  @Test
  public void testGetMedicationDailyFrequency()
  {
    final DateTime start = new DateTime(2013, 3, 25, 12, 0);
    final MedicationTimingCluster medicationTiming1 =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, null, null);
    final int medicationDailyFrequency1 = medicationsBo.getMedicationDailyFrequency(medicationTiming1);
    assertEquals(4L, (long)medicationDailyFrequency1);

    final MedicationTimingCluster medicationTiming2 =
        MedicationsTestUtils.buildMedicationTimingCluster(null, 8, null, null, start, null, null);
    final int medicationDailyFrequency2 = medicationsBo.getMedicationDailyFrequency(medicationTiming2);
    assertEquals(3L, (long)medicationDailyFrequency2);

    final MedicationTimingCluster medicationTiming3 =
        MedicationsTestUtils.buildMedicationTimingCluster(null, null, 1, null, start, null, null);
    final int medicationDailyFrequency3 = medicationsBo.getMedicationDailyFrequency(medicationTiming3);
    assertEquals(1L, (long)medicationDailyFrequency3);

    final MedicationTimingCluster medicationTiming4 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, DosingFrequencyTypeEnum.MORNING, start, null, null);
    final int medicationDailyFrequency4 = medicationsBo.getMedicationDailyFrequency(medicationTiming4);
    assertEquals(1L, (long)medicationDailyFrequency4);

    final MedicationTimingCluster medicationTiming5 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, DosingFrequencyTypeEnum.EVENING, start, null, null);
    final int medicationDailyFrequency5 = medicationsBo.getMedicationDailyFrequency(medicationTiming5);
    assertEquals(1L, (long)medicationDailyFrequency5);

    final MedicationTimingCluster medicationTiming6 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, DosingFrequencyTypeEnum.NOON, start, null, null);
    final int medicationDailyFrequency6 = medicationsBo.getMedicationDailyFrequency(medicationTiming6);
    assertEquals(1L, (long)medicationDailyFrequency6);
  }

  @Test
  public void testGetMedicationOrderCardInfoData()
  {
    // 1 --> 2 --> 3
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid1::1", new DateTime(2013, 5, 10, 12, 0), "Composer 1");
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid2::1", new DateTime(2013, 5, 11, 12, 0), "Composer 2");
    final MedicationOrderComposition composition3 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid3::1", new DateTime(2013, 5, 12, 12, 0), "Composer 3");

    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1", "Prescriber 1");
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    orderActivity1.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            3L, null, null, null, new DateTime(2013, 5, 10, 12, 0), null, null)
    );
    instruction1.getOrder().add(orderActivity1);
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 2", "Prescriber 2");
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);
    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 1000.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    orderActivity2.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2013, 5, 11, 12, 0), null, null)
    );
    instruction2.getOrder().add(orderActivity2);
    final MedicationInstructionInstruction instruction3 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 3", "Prescriber 3");
    final OrderActivity orderActivity3 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Daleron", 2L, 1500.0, "mg", null, null, null, null, "O", "ORAL", "1", "TBL");
    orderActivity3.setMedicationTiming(
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, 8, null, null, new DateTime(2013, 5, 12, 12, 0), null, null)
    );
    instruction3.getOrder().add(orderActivity3);
    composition3.getMedicationDetail().getMedicationInstruction().add(instruction3);

    final Link linkToInstruction1 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition1, instruction1);
    instruction2.getLinks().add(linkToInstruction1);
    final Link linkToInstruction2 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition2, instruction2);
    instruction3.getLinks().add(linkToInstruction2);

    //mock
    final MockObject<EhrMedicationsDao> ehrDaoMock = new MockObject<>(EhrMedicationsDao.class, this);
    ehrDaoMock.returns(composition1).loadMedicationOrderComposition(1L, "uid1");
    ehrDaoMock.returns(composition2).loadMedicationOrderComposition(1L, "uid2");
    ehrDaoMock.returns(composition3).loadMedicationOrderComposition(1L, "uid3");
    ehrDaoMock.returns(composition1).loadMedicationOrderComposition(1L, "uid1::1");
    ehrDaoMock.returns(composition2).loadMedicationOrderComposition(1L, "uid2::1");
    ehrDaoMock.returns(composition3).loadMedicationOrderComposition(1L, "uid3::1");
    medicationsBo.setEhrMedicationsDao(ehrDaoMock.getMock());

    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol");
    medicationDto1.setGenericName("Paracetamol");
    final MedicationDto medicationDto2 = new MedicationDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Daleron");
    medicationDto2.setGenericName("Paracetamol");

    final MockObject<HibernateMedicationsDao> medicationsDaoMock =
        new MockObject<>(HibernateMedicationsDao.class, this);
    medicationsDaoMock.returns(medicationDto1).getMedicationById(1L, new DateTime(2013, 5, 12, 12, 0));
    medicationsDaoMock.returns(medicationDto1).getMedicationById(1L, new DateTime(2013, 5, 12, 12, 0));
    medicationsDaoMock.returns(medicationDto2).getMedicationById(2L, new DateTime(2013, 5, 12, 12, 0));
    medicationsBo.setMedicationsDao(medicationsDaoMock.getMock());

    final TherapyCardInfoDto cardInfoData =
        medicationsBo.getTherapyCardInfoData(
            1L, null, "uid3::1", "MedicationInstructionInstruction 3", new Locale("en"),
            Intervals.wholeDay(new DateTime(2013, 5, 12, 12, 0)), new DateTime(2013, 5, 12, 12, 0));
    final List<TherapyChangeHistoryDto> changeHistoryList = cardInfoData.getChangeHistoryList();
    assertEquals("Prescriber 3", changeHistoryList.get(0).getEditor());
    assertEquals(new DateTime(2013, 5, 12, 12, 0), changeHistoryList.get(0).getChangeTime());
    assertEquals(TherapyChangeType.MEDICATION, changeHistoryList.get(0).getChanges().get(0).getType());
    assertEquals("Paracetamol (Lekadol)", changeHistoryList.get(0).getChanges().get(0).getOldValue());
    assertEquals("Paracetamol (Daleron)", changeHistoryList.get(0).getChanges().get(0).getNewValue());
    assertEquals(TherapyChangeType.DOSE, changeHistoryList.get(0).getChanges().get(1).getType());
    assertEquals("1000 mg", changeHistoryList.get(0).getChanges().get(1).getOldValue());
    assertEquals("1500 mg", changeHistoryList.get(0).getChanges().get(1).getNewValue());
    assertEquals(TherapyChangeType.DOSE_INTERVAL, changeHistoryList.get(0).getChanges().get(2).getType());
    assertEquals("2X per day", changeHistoryList.get(0).getChanges().get(2).getOldValue());
    assertEquals("Every 8 hours", changeHistoryList.get(0).getChanges().get(2).getNewValue());

    assertEquals("Prescriber 2", changeHistoryList.get(1).getEditor());
    assertEquals(new DateTime(2013, 5, 11, 12, 0), changeHistoryList.get(1).getChangeTime());
    assertEquals(TherapyChangeType.DOSE, changeHistoryList.get(1).getChanges().get(0).getType());
    assertEquals("500 mg", changeHistoryList.get(1).getChanges().get(0).getOldValue());
    assertEquals("1000 mg", changeHistoryList.get(1).getChanges().get(0).getNewValue());
    assertEquals(TherapyChangeType.DOSE_INTERVAL, changeHistoryList.get(1).getChanges().get(1).getType());
    assertEquals("3X per day", changeHistoryList.get(1).getChanges().get(1).getOldValue());
    assertEquals("2X per day", changeHistoryList.get(1).getChanges().get(1).getNewValue());
  }

  @Test
  public void testCalculateSimpleTherapyChange()
  {
    final ConstantSimpleTherapyDto oldTherapy = new ConstantSimpleTherapyDto();
    final MedicationDto medication1 = new MedicationDto();
    medication1.setDisplayName("Paracetamol (Lekadol)");
    oldTherapy.setMedication(medication1);
    oldTherapy.setFrequencyDisplay("every 8 hours");
    oldTherapy.setQuantityDisplay("120 mg / 5 ml");
    oldTherapy.setPrescriberName("Prescriber 1");
    oldTherapy.setStart(new DateTime(2013, 5, 10, 12, 0));

    final ConstantSimpleTherapyDto newTherapy = new ConstantSimpleTherapyDto();
    final MedicationDto medication2 = new MedicationDto();
    medication2.setDisplayName("Paracetamol (Daleron)");
    newTherapy.setMedication(medication2);
    newTherapy.setFrequencyDisplay("every 6 hours");
    newTherapy.setQuantityDisplay("240 mg / 10 ml");
    newTherapy.setPrescriberName("Prescriber 2");
    newTherapy.setStart(new DateTime(2013, 5, 11, 12, 0));

    final TherapyChangeHistoryDto changeHistoryDto =
        medicationsBo.calculateTherapyChange(oldTherapy, newTherapy);
    assertEquals("Prescriber 2", changeHistoryDto.getEditor());
    assertEquals(new DateTime(2013, 5, 11, 12, 0), changeHistoryDto.getChangeTime());
    assertEquals(TherapyChangeType.MEDICATION, changeHistoryDto.getChanges().get(0).getType());
    assertEquals("Paracetamol (Lekadol)", changeHistoryDto.getChanges().get(0).getOldValue());
    assertEquals("Paracetamol (Daleron)", changeHistoryDto.getChanges().get(0).getNewValue());
    assertEquals(TherapyChangeType.DOSE, changeHistoryDto.getChanges().get(1).getType());
    assertEquals("120 mg / 5 ml", changeHistoryDto.getChanges().get(1).getOldValue());
    assertEquals("240 mg / 10 ml", changeHistoryDto.getChanges().get(1).getNewValue());
    assertEquals(TherapyChangeType.DOSE_INTERVAL, changeHistoryDto.getChanges().get(2).getType());
    assertEquals("every 8 hours", changeHistoryDto.getChanges().get(2).getOldValue());
    assertEquals("every 6 hours", changeHistoryDto.getChanges().get(2).getNewValue());
  }

  @Test
  public void testCalculateComplexTherapyChange()
  {
    final ConstantComplexTherapyDto oldTherapy = new ConstantComplexTherapyDto();
    oldTherapy.setSpeedDisplay("100 ml/h");
    oldTherapy.setFrequencyDisplay("every 8 hours");
    oldTherapy.setPrescriberName("Prescriber 1");
    oldTherapy.setStart(new DateTime(2013, 5, 10, 12, 0));

    final ConstantComplexTherapyDto newTherapy = new ConstantComplexTherapyDto();
    newTherapy.setSpeedDisplay("200 ml/h");
    newTherapy.setFrequencyDisplay("every 6 hours");
    newTherapy.setPrescriberName("Prescriber 2");
    newTherapy.setStart(new DateTime(2013, 5, 11, 12, 0));

    final TherapyChangeHistoryDto changeHistoryDto =
        medicationsBo.calculateTherapyChange(oldTherapy, newTherapy);
    assertEquals("Prescriber 2", changeHistoryDto.getEditor());
    assertEquals(new DateTime(2013, 5, 11, 12, 0), changeHistoryDto.getChangeTime());
    assertEquals(TherapyChangeType.SPEED, changeHistoryDto.getChanges().get(0).getType());
    assertEquals("100 ml/h", changeHistoryDto.getChanges().get(0).getOldValue());
    assertEquals("200 ml/h", changeHistoryDto.getChanges().get(0).getNewValue());
    assertEquals(TherapyChangeType.DOSE_INTERVAL, changeHistoryDto.getChanges().get(1).getType());
    assertEquals("every 8 hours", changeHistoryDto.getChanges().get(1).getOldValue());
    assertEquals("every 6 hours", changeHistoryDto.getChanges().get(1).getNewValue());
  }

  @Test
  public void testWasTherapyModifiedFromLastReview()
  {
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", null, null);
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid2::1", null, null);

    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 2");
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);

    final Link linkToInstruction1 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition1, instruction1);
    instruction2.getLinks().add(linkToInstruction1);

    final List<MedicationActionAction> actionsList = new ArrayList<>();
    actionsList.add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.SCHEDULE, new DateTime(2013, 5, 10, 12, 0)));
    actionsList.add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2013, 5, 10, 12, 0)));
    actionsList.add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 10, 12, 0)));
    actionsList.add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 11, 12, 0)));
    actionsList.add(
        MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 12, 12, 0)));
    final boolean modified1 =
        medicationsBo.wasTherapyModifiedFromLastReview(instruction2, actionsList, new DateTime(2013, 5, 13, 5, 0));
    assertTrue(modified1);
    final boolean modified2 =
        medicationsBo.wasTherapyModifiedFromLastReview(instruction2, actionsList, new DateTime(2013, 5, 12, 5, 0));
    assertFalse(modified2);
  }

  @Test
  public void testIsTherapyActive()
  {
    //1.10.2013 is TUESDAY
    final List<String> daysOfWeek = new ArrayList<>();
    daysOfWeek.add("TUESDAY");
    daysOfWeek.add("WEDNESDAY");

    //test start day on active day of week
    assertTrue(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 1, 11, 0, 0))
    );
    //test some day on active day of week
    assertTrue(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 2, 11, 0, 0))
    );
    //test some day on inactive day of week
    assertFalse(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 3, 11, 0, 0))
    );
    //test therapy finished
    assertFalse(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            new Interval(new DateTime(2013, 5, 1, 10, 0, 0), new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 2, 11, 0, 0))
    );
    //test therapy not started yet
    assertFalse(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            Intervals.infiniteFrom(new DateTime(2013, 10, 2, 10, 0, 0)),
            new DateTime(2013, 10, 1, 11, 0, 0))
    );
    //test every second day on start day
    assertTrue(
        medicationsBo.isTherapyActive(
            null,
            2,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 1, 11, 0, 0))
    );
    //test every second day on inactive day
    assertFalse(
        medicationsBo.isTherapyActive(
            null,
            2,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 2, 11, 0, 0))
    );
    //test every second day on active day
    assertTrue(
        medicationsBo.isTherapyActive(
            null,
            2,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 3, 11, 0, 0))
    );
    //test every fifth day on active day
    assertTrue(
        medicationsBo.isTherapyActive(
            null,
            5,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 6, 11, 0, 0))
    );
    //test every fifth day on inactive day
    assertFalse(
        medicationsBo.isTherapyActive(
            null,
            5,
            Intervals.infiniteFrom(new DateTime(2013, 1, 10, 10, 0, 0)),
            new DateTime(2013, 1, 5, 11, 0, 0))
    );
  }

  @Test
  public void testBuildReferenceWeightComposition()
  {
    final DateTime testTimestamp = new DateTime(2013, 11, 28, 16, 0, 0);

    //mock
    final NamedIdentity namedIdentity = new NamedIdentityDto(1L, "First Last");

    final MockObject<MedicationsConnector> medicationsConnector = new MockObject<>(MedicationsConnector.class, this);
    medicationsConnector.returns(namedIdentity).getUsersName(1L, testTimestamp);
    medicationsBo.setMedicationsConnector(medicationsConnector.getMock());

    final MedicationReferenceWeightComposition comp =
        medicationsBo.buildReferenceWeightComposition(10.0, testTimestamp, 1L);
    final AnyEventEvent event = comp.getMedicationReferenceBodyWeight().getHistoryHistory().getAnyEvent().get(0);
    assertEquals(new Double(10.0), new Double(event.getWeight().getMagnitude()));
    assertEquals(testTimestamp, DataValueUtils.getDateTime(event.getTime()));
  }

  @Test
  public void testCalculateInfusionFormulaFromRate1()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(10.0);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setVolume(5.0);
    final Double formula = medicationsBo.calculateInfusionFormulaFromRate(10.0, calculationDto, 10d, "mg/kg/h", 10.0);
    assertEquals(new Double(2.0), formula);
  }

  @Test
  public void testCalculateInfusionRateFromFormula1()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(10.0);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setVolume(5.0);
    final Double rate = medicationsBo.calculateInfusionRateFromFormula(2.0,  "mg/kg/h", calculationDto, 10.0, 70.0);
    assertEquals(new Double(10.0), rate);
  }

  @Test
  public void testCalculateInfusionFormulaFromRate2()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(0.5);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setVolume(2.0);
    final Double formula = medicationsBo.calculateInfusionFormulaFromRate(21.0, calculationDto, 10d, "µg/kg/min", 5.0);
    final double formulaWithNormalPrecision = Math.round(formula * 1000.0) / 1000.0;
    assertEquals(new Double(17.5), new Double(formulaWithNormalPrecision));
  }

  @Test
  public void testCalculateInfusionRateFromFormula2()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(0.5);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setVolume(2.0);
    final Double rate = medicationsBo.calculateInfusionRateFromFormula(17.5, "µg/kg/min", calculationDto, 5.0, 70.0);
    final double rateWithNormalPrecision = Math.round(rate * 1000.0) / 1000.0;
    assertEquals(new Double(21.0), new Double(rateWithNormalPrecision));
  }

  @Test
  public void testCalculateInfusionFormulaFromRate3()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(50.0);
    calculationDto.setQuantityUnit("ng");
    calculationDto.setVolume(1.0);
    final Double formula = medicationsBo.calculateInfusionFormulaFromRate(100.0, calculationDto, 10d, "µg/kg/d", 10.0);
    final double formulaWithNormalPrecision = Math.round(formula * 1000.0) / 1000.0;
    assertEquals(new Double(12.0), new Double(formulaWithNormalPrecision));
  }

  @Test
  public void testCalculateInfusionRateFromFormula3()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(50.0);
    calculationDto.setQuantityUnit("ng");
    calculationDto.setVolume(1.0);
    final Double rate = medicationsBo.calculateInfusionRateFromFormula(12.0, "µg/kg/d", calculationDto, 10.0, 70.0);
    final double rateWithNormalPrecision = Math.round(rate * 1000.0) / 1000.0;
    assertEquals(new Double(100.0), new Double(rateWithNormalPrecision));
  }

  @Test
  public void testGetInfusionRateCalculationData1()
  {
    //single infusion ingredient (MEDICATION), normal infusion
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setContinuousInfusion(false);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setVolume(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionRateCalculationDto data =
        medicationsBo.getInfusionRateCalculationData(therapy, new DateTime(2013, 12, 10, 12, 0, 0));
    assertEquals(new Double(10.0), data.getQuantity());
    assertEquals("mg", data.getQuantityUnit());
    assertEquals(new Double(5.0), data.getVolume());
  }

  @Test
  public void testGetInfusionRateCalculationData2()
  {
    //single infusion ingredient (MEDICATION), continuous infusion

    //mock
    final MedicationIngredientDto medicationIngredient = new MedicationIngredientDto();
    medicationIngredient.setStrengthNumerator(10.0);
    medicationIngredient.setStrengthNumeratorUnit("mg");
    medicationIngredient.setStrengthDenominator(5.0);
    medicationIngredient.setStrengthDenominatorUnit("ml");
    final MockObject<HibernateMedicationsDao> medicationsDao = new MockObject<>(HibernateMedicationsDao.class, this);
    medicationsDao.returns(medicationIngredient).getMedicationDefiningIngredient(1L, new DateTime(2013, 12, 10, 12, 0, 0));
    medicationsBo.setMedicationsDao(medicationsDao.getMock());

    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setStart(new DateTime(2013, 12, 10, 12, 0, 0));
    therapy.setContinuousInfusion(true);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);
    medication.setId(1L);

    final InfusionRateCalculationDto data =
        medicationsBo.getInfusionRateCalculationData(therapy, new DateTime(2013, 12, 10, 12, 0, 0));
    assertEquals(new Double(10.0), data.getQuantity());
    assertEquals("mg", data.getQuantityUnit());
    assertEquals(new Double(5.0), data.getVolume());
  }

  @Test
  public void testGetInfusionRateCalculationData3()
  {
    //multiple infusion ingredients (only one MEDICATION)
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setVolumeSum(55.0);

    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient1);
    ingredient1.setQuantityUnit("mg");
    ingredient1.setQuantity(10.0);
    ingredient1.setVolume(5.0);
    final MedicationDto medication1 = new MedicationDto();
    ingredient1.setMedication(medication1);
    medication1.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient2);
    ingredient2.setVolume(50.0);
    final MedicationDto solution = new MedicationDto();
    ingredient2.setMedication(solution);
    solution.setMedicationType(MedicationTypeEnum.SOLUTION);

    final InfusionRateCalculationDto data =
        medicationsBo.getInfusionRateCalculationData(therapy, new DateTime(2013, 12, 10, 12, 0, 0));
    assertEquals(new Double(10.0), data.getQuantity());
    assertEquals("mg", data.getQuantityUnit());
    assertEquals(new Double(55.0), data.getVolume());
  }

  @Test
  public void testGetInfusionRateCalculationData4()
  {
    //multiple infusion ingredients (multiple MEDICATION-s)
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setVolumeSum(55.0);

    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient1);
    ingredient1.setQuantityUnit("mg");
    ingredient1.setQuantity(10.0);
    ingredient1.setVolume(5.0);
    final MedicationDto medication1 = new MedicationDto();
    ingredient1.setMedication(medication1);
    medication1.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient2);
    ingredient2.setQuantityUnit("mg");
    ingredient2.setQuantity(2.0);
    ingredient2.setVolume(1.0);
    final MedicationDto solution = new MedicationDto();
    ingredient2.setMedication(solution);
    solution.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionRateCalculationDto data =
        medicationsBo.getInfusionRateCalculationData(therapy, new DateTime(2013, 12, 10, 12, 0, 0));
    assertNull(data);
  }

  @Test
  public void testFillInfusionFormulaFromRate1()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setContinuousInfusion(false);
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    therapy.setDoseElement(doseElement);
    doseElement.setRateFormulaUnit("mg/kg/h");
    doseElement.setRate(10.0);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setVolume(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionFormulaFromRate(therapy, 10.0, 70.0, new DateTime(2013, 12, 10, 12, 0, 0));
    assertEquals(new Double(2.0), therapy.getDoseElement().getRateFormula());
  }

  @Test
  public void testFillInfusionRateFromFormula1()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setContinuousInfusion(false);
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    therapy.setDoseElement(doseElement);
    doseElement.setRateFormulaUnit("mg/kg/h");
    doseElement.setRateFormula(2.0);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setVolume(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionRateFromFormula(therapy, 10.0, 70.0, new DateTime(2013, 12, 10, 12, 0, 0));
    assertEquals(new Double(10.0), therapy.getDoseElement().getRate());
  }

  @Test
  public void testFillInfusionFormulaFromRate2()
  {
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    therapy.setContinuousInfusion(false);

    final TimedComplexDoseElementDto timedComplexDoseElement1 = new TimedComplexDoseElementDto();
    therapy.getTimedDoseElements().add(timedComplexDoseElement1);
    final ComplexDoseElementDto doseElement1 = new ComplexDoseElementDto();
    timedComplexDoseElement1.setDoseElement(doseElement1);
    doseElement1.setRateFormulaUnit("mg/kg/h");
    doseElement1.setRate(10.0);

    final TimedComplexDoseElementDto timedComplexDoseElement2 = new TimedComplexDoseElementDto();
    therapy.getTimedDoseElements().add(timedComplexDoseElement2);
    final ComplexDoseElementDto doseElement2 = new ComplexDoseElementDto();
    timedComplexDoseElement2.setDoseElement(doseElement2);
    doseElement2.setRateFormulaUnit("mg/kg/h");
    doseElement2.setRate(20.0);

    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setVolume(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionFormulaFromRate(therapy, 10.0, 70.0, new DateTime(2013, 12, 10, 12, 0, 0));
    assertEquals(new Double(2.0), therapy.getTimedDoseElements().get(0).getDoseElement().getRateFormula());
    assertEquals(new Double(4.0), therapy.getTimedDoseElements().get(1).getDoseElement().getRateFormula());
  }

  @Test
  public void testFillInfusionRateFromFormula2()
  {
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    therapy.setContinuousInfusion(false);

    final TimedComplexDoseElementDto timedComplexDoseElement1 = new TimedComplexDoseElementDto();
    therapy.getTimedDoseElements().add(timedComplexDoseElement1);
    final ComplexDoseElementDto doseElement1 = new ComplexDoseElementDto();
    timedComplexDoseElement1.setDoseElement(doseElement1);
    doseElement1.setRateFormulaUnit("mg/kg/h");
    doseElement1.setRateFormula(2.0);

    final TimedComplexDoseElementDto timedComplexDoseElement2 = new TimedComplexDoseElementDto();
    therapy.getTimedDoseElements().add(timedComplexDoseElement2);
    final ComplexDoseElementDto doseElement2 = new ComplexDoseElementDto();
    timedComplexDoseElement2.setDoseElement(doseElement2);
    doseElement2.setRateFormulaUnit("mg/kg/h");
    doseElement2.setRateFormula(4.0);

    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setVolume(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionRateFromFormula(therapy, 10.0, 70.0, new DateTime(2013, 12, 10, 12, 0, 0));
    assertEquals(new Double(10.0), therapy.getTimedDoseElements().get(0).getDoseElement().getRate());
    assertEquals(new Double(20.0), therapy.getTimedDoseElements().get(1).getDoseElement().getRate());
  }

  @Test
  public void testBuildTherapyTimeline1()
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies = new ArrayList<>();
    final Map<String, List<MedicationAdministrationComposition>> administrations = new HashMap<>();

    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid1::1", new DateTime(2014, 2, 20, 12, 0, 0), "1");
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    final OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, "po", "po", null, null);
    instruction.getOrder().add(orderActivity);
    final MedicationTimingCluster medicationTiming =
        MedicationsTestUtils.buildMedicationTimingCluster(
            4L, null, null, null, new DateTime(2014, 2, 21, 8, 10, 0), null, null);
    orderActivity.setMedicationTiming(medicationTiming);
    final IngredientsAndFormCluster ingredientsAndForm = new IngredientsAndFormCluster();
    orderActivity.setIngredientsAndForm(ingredientsAndForm);
    ingredientsAndForm.setForm(DataValueUtils.getLocalCodedText("tbl", "tbl"));
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair = Pair.of(composition, instruction);
    therapies.add(therapyPair);
    final String therapyId = InstructionTranslator.translate(therapyPair.getSecond(), therapyPair.getFirst());

    administrations.put(therapyId, new ArrayList<MedicationAdministrationComposition>());
    final LocatableRef instructionLocatableRef =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair.getFirst(), therapyPair.getSecond());
    administrations.get(therapyId).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm0",
            new DateTime(2014, 2, 21, 8, 10, 0),
            instructionLocatableRef,
            "1",
            500.0,
            "mg",
            "comment123",
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    ); //administered on time
    administrations.get(therapyId).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm1",
            new DateTime(2014, 2, 21, 10, 40, 0),
            instructionLocatableRef,
            "1",
            500.0,
            "mg",
            null,
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    );  //administered on time
    administrations.get(therapyId).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm2",
            new DateTime(2014, 2, 21, 14, 50, 0),
            instructionLocatableRef,
            "1",
            500.0,
            "ug",
            null,
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    ); //administered late
    administrations.get(therapyId).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm3",
            new DateTime(2014, 2, 21, 15, 15, 0),
            instructionLocatableRef,
            "1",
            450.0,
            "mg",
            null,
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    ); //administered early
    administrations.get(therapyId).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm4",
            new DateTime(2014, 2, 21, 16, 30, 0),
            instructionLocatableRef,
            "1",
            600.0,
            "mg",
            null,
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    ); //unplanned
    administrations.get(therapyId).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm4",
            new DateTime(2013, 1, 1, 10, 30, 0),
            instructionLocatableRef,
            "1",
            600.0,
            "mg",
            null,
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    ); //past - should be filtered out

    final List<TherapyTaskDto> tasks = new ArrayList<>();
    tasks.add(buildTherapyTaskDto("t0", "adm0", therapyId, new DateTime(2014, 2, 21, 8, 0, 0), 500.0, "mg"));
    tasks.add(buildTherapyTaskDto("t1", "adm1", therapyId, new DateTime(2014, 2, 21, 11, 0, 0), 500.0, "mg"));
    tasks.add(buildTherapyTaskDto("t2", "adm2", therapyId, new DateTime(2014, 2, 21, 14, 0, 0), 500.0, "mg"));
    tasks.add(buildTherapyTaskDto("t3", "adm3", therapyId, new DateTime(2014, 2, 21, 16, 0, 0), 500.0, "mg"));
    tasks.add(buildTherapyTaskDto("t4", null, therapyId, new DateTime(2014, 2, 21, 18, 0, 0), 500.0, "mg")); //late
    tasks.add(buildTherapyTaskDto("t5", null, therapyId, new DateTime(2014, 2, 21, 18, 30, 0), 500.0, "mg")); //due
    tasks.add(buildTherapyTaskDto("t6", null, therapyId, new DateTime(2014, 2, 21, 19, 0, 0), 500.0, "mg")); //due
    tasks.add(buildTherapyTaskDto("t7", null, therapyId, new DateTime(2014, 2, 21, 21, 0, 0), 500.0, "mg")); //planned

    final KnownClinic department = KnownClinic.Utils.fromName("KOOKIT");
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    //mock medicationsBo
    final MockObject<EhrMedicationsDao> ehrDaoMock = new MockObject<>(EhrMedicationsDao.class, this);
    ehrDaoMock.returns(composition).loadMedicationOrderComposition(1L, "uid1");
    medicationsBo.setEhrMedicationsDao(ehrDaoMock.getMock());

    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    final MockObject<HibernateMedicationsDao> medicationsDaoMock = new MockObject<>(HibernateMedicationsDao.class, this);
    medicationsDaoMock.returns(medicationDto1).getMedicationById(1L, new DateTime(2014, 2, 21, 18, 40, 0));
    medicationsBo.setMedicationsDao(medicationsDaoMock.getMock());

    //mock taggingDao
    final MockObject<OpenEhrTaggingDao> ehrTaggingDaoMock = new MockObject<>(OpenEhrTaggingDao.class, this);
    final TagFilteringDto filteringDto = new TagFilteringDto();
    filteringDto.setCompositionVersion(TagFilteringDto.CompositionVersion.LAST_VERSION_OF_ANY_TAGGED);

    final TaggedObjectDto<Instruction> taggedObjectDto = new TaggedObjectDto<>();
    taggedObjectDto.setComposition(composition);
    taggedObjectDto.setObject(instruction);

    ehrTaggingDaoMock.returns(Collections.singleton(taggedObjectDto)).findObjectCompositionPairs(
        filteringDto, TherapyTaggingUtils.generateTag(TherapyTag.PRESCRIPTION, 1L));
    medicationsBo.setEhrTaggingDao(ehrTaggingDaoMock.getMock());


    final List<TherapyTimelineRowDto> therapyTimelineRows =
        medicationsBo.buildTherapyTimeline(
            1L,
            1L,
            therapies,
            administrations,
            tasks,
            Intervals.infiniteFrom(new DateTime(2014, 2, 10, 0, 0, 0)),
            roundsIntervalDto,
            TherapySortTypeEnum.DESCRIPTION_ASC,
            department,
            new DateTime(2014, 2, 21, 18, 40, 0),
            null);

    assertEquals(1L, therapyTimelineRows.size());
    assertEquals(9L, therapyTimelineRows.get(0).getAdministrations().size());

    final StartAdministrationDto administration0 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(0);
    assertEquals("adm0", administration0.getAdministrationId());
    assertEquals("t0", administration0.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration0.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 8, 10, 0), administration0.getAdministrationTime());
    assertEquals(500.0, administration0.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration0.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration0.isDifferentFromOrder());
    assertEquals("comment123", administration0.getComment());

    final StartAdministrationDto administration1 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(1);
    assertEquals("adm1", administration1.getAdministrationId());
    assertEquals("t1", administration1.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration1.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 10, 40, 0), administration1.getAdministrationTime());
    assertEquals(500.0, administration1.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration1.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration1.isDifferentFromOrder());
    assertNull(administration1.getComment());

    final StartAdministrationDto administration2 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(2);
    assertEquals("adm2", administration2.getAdministrationId());
    assertEquals("t2", administration2.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED_LATE, administration2.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 14, 50, 0), administration2.getAdministrationTime());
    assertEquals(500.0, administration2.getAdministeredDose().getNumerator(), 0);
    assertEquals("ug", administration2.getAdministeredDose().getNumeratorUnit());
    assertTrue(administration2.isDifferentFromOrder());

    final StartAdministrationDto administration3 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(3);
    assertEquals("adm3", administration3.getAdministrationId());
    assertEquals("t3", administration3.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED_EARLY, administration3.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 15, 15, 0), administration3.getAdministrationTime());
    assertEquals(450.0, administration3.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration3.getAdministeredDose().getNumeratorUnit());
    assertTrue(administration3.isDifferentFromOrder());

    final StartAdministrationDto administration4 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(4);
    assertEquals("adm4", administration4.getAdministrationId());
    assertNull(administration4.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration4.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 16, 30, 0), administration4.getAdministrationTime());
    assertEquals(600.0, administration4.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration4.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration4.isDifferentFromOrder());

    final StartAdministrationDto administration5 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(5);
    assertNull(administration5.getAdministrationId());
    assertEquals("t4", administration5.getTaskId());
    assertEquals(AdministrationStatusEnum.LATE, administration5.getAdministrationStatus());
    assertNull(administration5.getAdministrationTime());
    assertEquals(new DateTime(2014, 2, 21, 18, 0, 0), administration5.getPlannedTime());
    assertEquals(500.0, administration5.getPlannedDose().getNumerator(), 0);
    assertEquals("mg", administration5.getPlannedDose().getNumeratorUnit());

    final StartAdministrationDto administration6 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(6);
    assertNull(administration6.getAdministrationId());
    assertEquals("t5", administration6.getTaskId());
    assertEquals(AdministrationStatusEnum.DUE, administration6.getAdministrationStatus());
    assertNull(administration6.getAdministrationTime());
    assertEquals(new DateTime(2014, 2, 21, 18, 30, 0), administration6.getPlannedTime());
    assertEquals(500.0, administration6.getPlannedDose().getNumerator(), 0);
    assertEquals("mg", administration6.getPlannedDose().getNumeratorUnit());

    final StartAdministrationDto administration7 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(7);
    assertNull(administration7.getAdministrationId());
    assertEquals("t6", administration7.getTaskId());
    assertEquals(AdministrationStatusEnum.DUE, administration7.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 19, 0, 0), administration7.getPlannedTime());

    final StartAdministrationDto administration8 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(8);
    assertNull(administration8.getAdministrationId());
    assertEquals("t7", administration8.getTaskId());
    assertEquals(AdministrationStatusEnum.PLANNED, administration8.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 21, 0, 0), administration8.getPlannedTime());

    assertEquals(1L, therapyTimelineRows.get(0).getTherapy().getTags().size());
    assertEquals(TherapyTag.PRESCRIPTION, therapyTimelineRows.get(0).getTherapy().getTags().get(0));
  }

  @Test
  public void testFillingTagsForPrescription1()
  {
    //tag exists
    final List<TherapyDto> therapyDtos = new ArrayList<>();

    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setCompositionUid("12345");
    therapy.setEhrOrderName("ehrOrder");
    therapyDtos.add(therapy);

    final Set<TaggedObjectDto<Instruction>> taggedObjectDtos = new HashSet<>();

    final TaggedObjectDto<Instruction> taggedObjectDto = new TaggedObjectDto<>();
    final Composition composition = new Composition();
    final UidBasedId uidBasedId = new ObjectVersionId();
    uidBasedId.setValue("12345");
    composition.setUid(uidBasedId);
    taggedObjectDto.setComposition(composition);
    final Instruction instruction = new Instruction();
    final DvText dvText = new DvText();
    dvText.setValue("ehrOrder");
    instruction.setName(dvText);
    taggedObjectDto.setObject(instruction);
    taggedObjectDtos.add(taggedObjectDto);

    medicationsBo.fillPrescriptionTagsForTherapies(therapyDtos, taggedObjectDtos);
    assertEquals(1L, therapyDtos.get(0).getTags().size());
  }

  @Test
  public void testFillingTagsForPrescription2()
  {
    //another tag
    final List<TherapyDto> therapyDtos = new ArrayList<>();

    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setCompositionUid("12345");
    therapy.setEhrOrderName("ehrOrder");
    therapyDtos.add(therapy);

    final Set<TaggedObjectDto<Instruction>> taggedObjectDtos = new HashSet<>();

    final TaggedObjectDto<Instruction> taggedObjectDto = new TaggedObjectDto<>();
    final Composition composition = new Composition();
    final UidBasedId uidBasedId = new ObjectVersionId();
    uidBasedId.setValue("12345");
    composition.setUid(uidBasedId);
    taggedObjectDto.setComposition(composition);
    final Instruction instruction = new Instruction();
    final DvText dvText = new DvText();
    dvText.setValue("ehrOrder2");
    instruction.setName(dvText);
    taggedObjectDto.setObject(instruction);
    taggedObjectDtos.add(taggedObjectDto);

    medicationsBo.fillPrescriptionTagsForTherapies(therapyDtos, taggedObjectDtos);
    assertEquals(0L, therapyDtos.get(0).getTags().size());
  }

  @Test
  public void testFillingTagsForPrescription3()
  {
    //no tag
    final List<TherapyDto> therapyDtos = new ArrayList<>();

    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setCompositionUid("12345");
    therapy.setEhrOrderName("ehrOrder");
    therapyDtos.add(therapy);

    final Set<TaggedObjectDto<Instruction>> taggedObjectDtos = new HashSet<>();

    medicationsBo.fillPrescriptionTagsForTherapies(therapyDtos, taggedObjectDtos);
    assertEquals(0L, therapyDtos.get(0).getTags().size());
  }

  @Test
  public void testBuildTherapyTimeline2()
  {
    //mock
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    final MockObject<HibernateMedicationsDao> medicationsDaoMock = new MockObject<>(HibernateMedicationsDao.class, this);
    medicationsDaoMock.returns(medicationDto1).getMedicationById(1L, new DateTime(2014, 2, 21, 18, 40, 0));
    medicationsBo.setMedicationsDao(medicationsDaoMock.getMock());

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies = new ArrayList<>();
    final Map<String, List<MedicationAdministrationComposition>> administrations = new HashMap<>();
    final List<TherapyTaskDto> tasks = new ArrayList<>();

    //original therapy
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid1::1", new DateTime(2014, 2, 20, 12, 0, 0), "1");
    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, "IV", "IV", null, null);
    instruction1.getOrder().add(orderActivity1);
    final MedicationTimingCluster medicationTiming1 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            4L, null, null, null, new DateTime(2014, 2, 20, 12, 10, 0), null, null);
    orderActivity1.setMedicationTiming(medicationTiming1);
    final IngredientsAndFormCluster ingredientsAndForm1 = new IngredientsAndFormCluster();
    orderActivity1.setIngredientsAndForm(ingredientsAndForm1);
    ingredientsAndForm1.setForm(DataValueUtils.getLocalCodedText("po", "po"));
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair1 = Pair.of(
        composition1,
        instruction1);
    therapies.add(therapyPair1);

    final String therapyId1 = InstructionTranslator.translate(therapyPair1.getSecond(), therapyPair1.getFirst());
    administrations.put(therapyId1, new ArrayList<MedicationAdministrationComposition>());
    final LocatableRef instructionLocatableRef1 =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair1.getFirst(), therapyPair1.getSecond());
    administrations.get(therapyId1).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm0",
            new DateTime(2014, 2, 21, 8, 10, 0),
            instructionLocatableRef1,
            "1",
            500.0,
            "mg",
            "comment123",
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    );
    tasks.add(buildTherapyTaskDto("t0", "adm0", therapyId1, new DateTime(2014, 2, 21, 8, 0, 0), 500.0, "mg"));

    //modified therapy
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid2::1", new DateTime(2014, 2, 21, 12, 0, 0), "1");
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 1000.0, "mg", null, null, null, null, "IV", "IV", null, null);
    instruction2.getOrder().add(orderActivity2);
    final MedicationTimingCluster medicationTiming2 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            4L, null, null, null, new DateTime(2014, 2, 21, 12, 10, 0), null, null);
    orderActivity2.setMedicationTiming(medicationTiming2);
    final IngredientsAndFormCluster ingredientsAndForm2 = new IngredientsAndFormCluster();
    orderActivity2.setIngredientsAndForm(ingredientsAndForm2);
    ingredientsAndForm2.setForm(DataValueUtils.getLocalCodedText("po", "po"));
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);
    final Link linkToInstruction1 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition1, instruction1);
    instruction2.getLinks().add(linkToInstruction1);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair2 = Pair.of(
        composition2,
        instruction2);
    therapies.add(therapyPair2);

    final String therapyId2 = InstructionTranslator.translate(therapyPair2.getSecond(), therapyPair2.getFirst());
    administrations.put(therapyId2, new ArrayList<MedicationAdministrationComposition>());
    final LocatableRef instructionLocatableRef2 =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair1.getFirst(), therapyPair1.getSecond());
    administrations.get(therapyId2).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm1",
            new DateTime(2014, 2, 21, 12, 40, 0),
            instructionLocatableRef2,
            "1",
            1000.0,
            "mg",
            null,
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    );
    tasks.add(buildTherapyTaskDto("t1", "adm1", therapyId2, new DateTime(2014, 2, 21, 13, 0, 0), 1000.0, "mg"));

    //mock
    final MockObject<EhrMedicationsDao> ehrDaoMock = new MockObject<>(EhrMedicationsDao.class, this);
    ehrDaoMock.returns(composition1).loadMedicationOrderComposition(1L, "uid1");
    ehrDaoMock.returns(composition2).loadMedicationOrderComposition(1L, "uid2");
    medicationsBo.setEhrMedicationsDao(ehrDaoMock.getMock());

    final KnownClinic department = KnownClinic.Utils.fromName("KOOKIT");
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final List<TherapyTimelineRowDto> therapyTimelineRows =
        medicationsBo.buildTherapyTimeline(
            1L,
            1L,
            therapies, administrations, tasks, Intervals.INFINITE,
            roundsIntervalDto,
            TherapySortTypeEnum.DESCRIPTION_ASC,
            department,
            new DateTime(2014, 2, 21, 18, 40, 0), null);

    assertEquals(1L, therapyTimelineRows.size());
    assertEquals(2L, therapyTimelineRows.get(0).getAdministrations().size());

    final StartAdministrationDto administration0 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(0);
    assertEquals("adm0", administration0.getAdministrationId());
    assertEquals("t0", administration0.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration0.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 8, 10, 0), administration0.getAdministrationTime());
    assertEquals(500.0, administration0.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration0.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration0.isDifferentFromOrder());
    assertEquals("comment123", administration0.getComment());

    final StartAdministrationDto administration1 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(1);
    assertEquals("adm1", administration1.getAdministrationId());
    assertEquals("t1", administration1.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration1.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 12, 40, 0), administration1.getAdministrationTime());
    assertEquals(1000.0, administration1.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration1.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration1.isDifferentFromOrder());
    assertNull(administration1.getComment());

    assertEquals("uid2::1", therapyTimelineRows.get(0).getTherapy().getCompositionUid());
  }

  @Test
  public void testBuildTherapyTimeline3() //continuous infusion
  {
    //mock
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    final MockObject<HibernateMedicationsDao> medicationsDaoMock = new MockObject<>(HibernateMedicationsDao.class, this);
    medicationsDaoMock.returns(medicationDto1).getMedicationById(1L, new DateTime(2014, 2, 21, 18, 40, 0));
    medicationsBo.setMedicationsDao(medicationsDaoMock.getMock());

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies = new ArrayList<>();
    final Map<String, List<MedicationAdministrationComposition>> administrations = new HashMap<>();
    final List<TherapyTaskDto> tasks = new ArrayList<>();

    //original therapy
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid1::1", new DateTime(2014, 2, 20, 12, 0, 0), "1");
    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Dopamin", 1L, null, null, null, null, null, null, "IV", "IV", null, null);
    instruction1.getOrder().add(orderActivity1);
    final MedicationTimingCluster medicationTiming1 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 20, 12, 10, 0), null, null);
    orderActivity1.setMedicationTiming(medicationTiming1);
    final AdministrationDetailsCluster administrationDetails = new AdministrationDetailsCluster();
    orderActivity1.setAdministrationDetails(administrationDetails);
    administrationDetails.setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION))
    );
    final InfusionAdministrationDetailsCluster infusionDetails = new InfusionAdministrationDetailsCluster();
    administrationDetails.getInfusionAdministrationDetails().add(infusionDetails);
    infusionDetails.setDoseAdministrationRate(DataValueUtils.getQuantity(20.0, "ml/h"));
    administrationDetails.setRoute(Collections.singletonList(DataValueUtils.getLocalCodedText("IV", "IV")));

    final IngredientsAndFormCluster ingredientsAndForm1 = new IngredientsAndFormCluster();
    orderActivity1.setIngredientsAndForm(ingredientsAndForm1);
    ingredientsAndForm1.setForm(DataValueUtils.getLocalCodedText("fluid", "fluid"));
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair1 = Pair.of(
        composition1,
        instruction1);
    therapies.add(therapyPair1);

    final String therapyId1 = InstructionTranslator.translate(therapyPair1.getSecond(), therapyPair1.getFirst());
    administrations.put(therapyId1, new ArrayList<MedicationAdministrationComposition>());
    final LocatableRef instructionLocatableRef1 =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair1.getFirst(), therapyPair1.getSecond());
    administrations.get(therapyId1).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm0",
            new DateTime(2014, 2, 20, 12, 0, 0),
            instructionLocatableRef1,
            "1",
            21.0,
            "ml/h",
            "comment123",
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    );

    //modified therapy
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid2::1", new DateTime(2014, 2, 21, 12, 0, 0), "1");
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Dopamin", 1L, null, null, null, null, null, null, "IV", "IV", null, null);
    instruction2.getOrder().add(orderActivity2);
    final MedicationTimingCluster medicationTiming2 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 20, 17, 30, 0), null, null);
    orderActivity2.setMedicationTiming(medicationTiming2);
    final AdministrationDetailsCluster administrationDetails2 = new AdministrationDetailsCluster();
    orderActivity2.setAdministrationDetails(administrationDetails2);
    administrationDetails2.setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION))
    );
    final InfusionAdministrationDetailsCluster infusionDetails2 = new InfusionAdministrationDetailsCluster();
    administrationDetails2.getInfusionAdministrationDetails().add(infusionDetails2);
    infusionDetails2.setDoseAdministrationRate(DataValueUtils.getQuantity(20.0, "ml/h"));
    administrationDetails2.setRoute(Collections.singletonList(DataValueUtils.getLocalCodedText("IV", "IV")));

    final IngredientsAndFormCluster ingredientsAndForm2 = new IngredientsAndFormCluster();
    orderActivity2.setIngredientsAndForm(ingredientsAndForm2);
    ingredientsAndForm2.setForm(DataValueUtils.getLocalCodedText("fluid", "fluid"));
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);
    final Link linkToInstruction1 =
        OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, composition1, instruction1);
    instruction2.getLinks().add(linkToInstruction1);

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair2 = Pair.of(
        composition2,
        instruction2);
    therapies.add(therapyPair2);

    final String therapyId2 = InstructionTranslator.translate(therapyPair2.getSecond(), therapyPair2.getFirst());
    administrations.put(therapyId2, new ArrayList<MedicationAdministrationComposition>());
    final LocatableRef instructionLocatableRef2 =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair2.getFirst(), therapyPair2.getSecond());
    administrations.get(therapyId2).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm1",
            new DateTime(2014, 2, 21, 8, 10, 0),
            instructionLocatableRef2,
            "1",
            25.0,
            "ml/h",
            null,
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    );

    final KnownClinic department = KnownClinic.Utils.fromName("KOOKIT");
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    //mock
    final MockObject<EhrMedicationsDao> ehrDaoMock = new MockObject<>(EhrMedicationsDao.class, this);
    ehrDaoMock.returns(composition1).loadMedicationOrderComposition(1L, "uid1");
    ehrDaoMock.returns(composition2).loadMedicationOrderComposition(1L, "uid2");
    medicationsBo.setEhrMedicationsDao(ehrDaoMock.getMock());

    final List<TherapyTimelineRowDto> therapyTimelineRows =
        medicationsBo.buildTherapyTimeline(
            1L,
            1L,
            therapies, administrations, tasks, Intervals.INFINITE,
            roundsIntervalDto,
            TherapySortTypeEnum.DESCRIPTION_ASC,
            department,
            new DateTime(2014, 2, 21, 18, 40, 0), new Locale("en"));

    assertEquals(1L, therapyTimelineRows.size());
    final TherapyTimelineRowDto timeline = therapyTimelineRows.get(0);
    assertEquals(2L, timeline.getAdministrations().size());

    final StartAdministrationDto administration0 = (StartAdministrationDto)timeline.getAdministrations().get(0);
    assertEquals("adm0", administration0.getAdministrationId());
    assertNull(administration0.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration0.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 20, 12, 0, 0), administration0.getAdministrationTime());
    assertEquals(21.0, administration0.getAdministeredDose().getNumerator(), 0);
    assertEquals("ml/h", administration0.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration0.isDifferentFromOrder());
    assertEquals("comment123", administration0.getComment());

    final StartAdministrationDto administration1 = (StartAdministrationDto)timeline.getAdministrations().get(1);
    assertEquals("adm1", administration1.getAdministrationId());
    assertNull(administration0.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration1.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 8, 10, 0), administration1.getAdministrationTime());
    assertEquals(25, administration1.getAdministeredDose().getNumerator(), 0);
    assertEquals("ml/h", administration1.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration1.isDifferentFromOrder());
    assertNull(administration1.getComment());

    assertEquals("uid2::1", timeline.getTherapy().getCompositionUid());
    assertTrue(timeline instanceof TherapyTimelineRowForContInfusionDto);
    assertEquals("25 ml/h", ((TherapyTimelineRowForContInfusionDto)timeline).getInfusionRateDisplay());
  }

  @Test
  public void testBuildAdministrationFromTask()
  {
    final TherapyTaskDto therapyTask =
        buildTherapyTaskDto("task1", null, "therapy1", new DateTime(2014, 5, 7, 12, 0, 0), 50.0, "mg");
    final AdministrationDto administration =
        medicationsBo.buildAdministrationFromTask(therapyTask, new DateTime(2014, 5, 7, 12, 0, 0));
    assertTrue(administration instanceof StartAdministrationDto);
    final StartAdministrationDto startAdministration = (StartAdministrationDto)administration;
    assertEquals(50, startAdministration.getPlannedDose().getNumerator(), 0);
    assertEquals("mg", startAdministration.getPlannedDose().getNumeratorUnit());
    assertNull(startAdministration.getPlannedDose().getDenominator());
    assertNull(startAdministration.getPlannedDose().getDenominatorUnit());
    assertEquals(TherapyDoseTypeEnum.QUANTITY, startAdministration.getPlannedDose().getTherapyDoseTypeEnum());
    assertEquals(new DateTime(2014, 5, 7, 12, 0, 0), startAdministration.getPlannedTime());
    assertEquals("task1", startAdministration.getTaskId());
    assertEquals(AdministrationTypeEnum.START, startAdministration.getAdministrationType());
  }

  @Test
  public void testFilterMedicationsTreeSingleGeneric()
  {
    final List<MedicationSearchDto> medications = buildMedicationTree();
    final List<MedicationSearchDto> filteredMedications = medicationsBo.filterMedicationsTree(medications, "para");

    assertEquals(1, filteredMedications.size());
    assertEquals(2, filteredMedications.get(0).getChildren().size());
    assertEquals(2, filteredMedications.get(0).getChildren().get(0).getChildren().size());
    assertEquals(2, filteredMedications.get(0).getChildren().get(1).getChildren().size());

    assertFalse(filteredMedications.get(0).isExpand());
    assertFalse(filteredMedications.get(0).getChildren().get(0).isExpand());
    assertFalse(filteredMedications.get(0).getChildren().get(1).isExpand());
  }

  @Test
  public void testFilterMedicationsTreeGenericDose()
  {
    final List<MedicationSearchDto> medications = buildMedicationTree();
    final List<MedicationSearchDto> filteredMedications = medicationsBo.filterMedicationsTree(medications, "para 500");

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(2, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpand());
    assertFalse(filteredMedications.get(0).getChildren().get(0).isExpand());
  }

  @Test
  public void testFilterMedicationsTreeBrandDose1()
  {
    final List<MedicationSearchDto> medications = buildMedicationTree();
    final List<MedicationSearchDto> filteredMedications = medicationsBo.filterMedicationsTree(medications, "leka 500");

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpand());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpand());
  }

  @Test
  public void testFilterMedicationsTreeBrandDose2()
  {
    final List<MedicationSearchDto> medications = buildMedicationTree();
    final List<MedicationSearchDto> filteredMedications = medicationsBo.filterMedicationsTree(medications, "dale 500");

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpand());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpand());
  }

  @Test
  public void testFilterMedicationsTreeBrand()
  {
    final List<MedicationSearchDto> medications = buildMedicationTree();
    final List<MedicationSearchDto> filteredMedications = medicationsBo.filterMedicationsTree(medications, "dale");

    assertEquals(1, filteredMedications.size());
    assertEquals(2, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(1).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpand());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpand());
    assertTrue(filteredMedications.get(0).getChildren().get(1).isExpand());
  }

  @Test
  public void testFilterMedicationsTreeDoesntStartWithSearchWord()
  {
    final List<MedicationSearchDto> medications = buildMedicationTree();
    final List<MedicationSearchDto> filteredMedications = medicationsBo.filterMedicationsTree(medications, "aleron");

    assertEquals(0, filteredMedications.size());
  }

  @Test
  public void testFilterMedicationsTreeEmptySearch()
  {
    final List<MedicationSearchDto> medications = buildMedicationTree();
    final List<MedicationSearchDto> filteredMedications = medicationsBo.filterMedicationsTree(medications, "");

    assertEquals(1, filteredMedications.size());
  }

  private List<MedicationSearchDto> buildMedicationTree()
  {
    final List<MedicationSearchDto> medications = new ArrayList<>();

    final MedicationSearchDto dto1 = new MedicationSearchDto();
    dto1.setTitle("Paracetamol");
    dto1.setKey(1L);
    dto1.setMedication(new MedicationSimpleDto());
    medications.add(dto1);

    final MedicationSearchDto dto11 = new MedicationSearchDto();
    dto11.setTitle("Paracetamol 500 mg oral");
    dto11.setKey(11L);
    dto11.setMedication(new MedicationSimpleDto());
    dto1.getSublevelMedications().add(dto11);

    final MedicationSearchDto dto111 = new MedicationSearchDto();
    dto111.setTitle("Lekadol 500 mg oral");
    dto111.setKey(111L);
    dto111.setMedication(new MedicationSimpleDto());
    dto11.getSublevelMedications().add(dto111);

    final MedicationSearchDto dto112 = new MedicationSearchDto();
    dto112.setTitle("Daleron 500 mg oral");
    dto112.setKey(112L);
    dto112.setMedication(new MedicationSimpleDto());
    dto11.getSublevelMedications().add(dto112);

    final MedicationSearchDto dto12 = new MedicationSearchDto();
    dto12.setTitle("Paracetamol 250 mg oral");
    dto12.setKey(12L);
    dto12.setMedication(new MedicationSimpleDto());
    dto1.getSublevelMedications().add(dto12);

    final MedicationSearchDto dto121 = new MedicationSearchDto();
    dto121.setTitle("Lekadol 250 mg oral");
    dto121.setKey(121L);
    dto121.setMedication(new MedicationSimpleDto());
    dto12.getSublevelMedications().add(dto121);

    final MedicationSearchDto dto122 = new MedicationSearchDto();
    dto122.setTitle("Daleron 250 mg oral");
    dto122.setKey(122L);
    dto122.setMedication(new MedicationSimpleDto());
    dto12.getSublevelMedications().add(dto122);

    return medications;
  }

  private TherapyTaskDto buildTherapyTaskDto(
      final String taskId,
      final String administrationId,
      final String therapyId,
      final DateTime timestamp,
      final Double doseNumerator,
      final String doseNumeratorUnit)
  {
    final TherapyTaskDto therapyTaskDto = new TherapyTaskDto();
    therapyTaskDto.setTaskId(taskId);
    therapyTaskDto.setAdministrationId(administrationId);
    therapyTaskDto.setTherapyId(therapyId);
    therapyTaskDto.setPlannedAdministrationTime(timestamp);
    therapyTaskDto.setAdministrationTypeEnum(AdministrationTypeEnum.START);
    therapyTaskDto.setTherapyDoseDto(new TherapyDoseDto());
    therapyTaskDto.getTherapyDoseDto().setNumerator(doseNumerator);
    therapyTaskDto.getTherapyDoseDto().setNumeratorUnit(doseNumeratorUnit);
    therapyTaskDto.getTherapyDoseDto().setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    return therapyTaskDto;
  }
}
