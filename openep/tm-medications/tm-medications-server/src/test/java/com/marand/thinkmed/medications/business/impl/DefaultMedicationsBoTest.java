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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.maf.core.security.remoting.GlobalAuditContext;
import com.marand.maf.core.service.ConstantUserMetadataProvider;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.RequestContextImpl;
import com.marand.maf.core.time.Intervals;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster.TimingCluster.DayOfWeek;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.api.core.Dictionary;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.TherapyJsonDeserializer;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.business.mapper.MedicationHolderDtoMapper;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationToEhrConverter;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.hibernate.HibernateMedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.IndicationDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.InfusionRateCalculationDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSiteDto;
import com.marand.thinkmed.medications.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateElementDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.openehr.jaxb.rm.UidBasedId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster;
import static com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition.MedicationReferenceBodyWeightObservation.HistoryHistory.AnyEventEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultMedicationsBoTest
{
  @InjectMocks
  private DefaultMedicationsBo medicationsBo = new DefaultMedicationsBo();

  @Mock
  private Dictionary testDictionary;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private TaggingOpenEhrDao taggingOpenEhrDao;

  @Mock
  private TherapyDisplayProvider therapyDisplayProvider;

  @Mock
  private ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder;

  @Mock
  private ValueHolder<Map<Long, MedicationRouteDto>> medicationRoutesValueHolder;

  @Spy
  private MedicationHolderDtoMapper medicationHolderDtoMapper = new MedicationHolderDtoMapper();

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler;

  @Mock
  private AdministrationProvider administrationProvider;

  @Before
  public void setUp()
  {
    medicationHolderDtoMapper.setMarkNonFormularyMedication(true);
    //mock
    final Map<Long, MedicationHolderDto> medicationHolderMap = new HashMap<>();
    final MedicationHolderDto medicationDto1 = new MedicationHolderDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    medicationHolderMap.put(1L, medicationDto1);

    final MedicationHolderDto medicationDto2 = new MedicationHolderDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Primotren 20x500mg");
    medicationHolderMap.put(2L, medicationDto2);

    final MedicationsValueHolder medicationsValueHolder = mock(MedicationsValueHolder.class);
    when(medicationsValueHolder.getValue()).thenReturn(medicationHolderMap);
    medicationsBo.setMedicationsValueHolder(medicationsValueHolder);
  }

  @Test
  public void testTransformConstantSimpleTherapy()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setTherapyDescription("CODE1:NAME1 - 3.0mg 1TBL, 3x per day");
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);
    route.setName("ORAL");
    therapy.setRoutes(Collections.singletonList(route));
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
    assertEquals("1", orderActivity.getAdministrationDetails().getRoute().get(0).getDefiningCode().getCodeString());
    //quantity
    assertEquals(DataValueUtils.getQuantity(3.0, ""), orderActivity.getStructuredDose().getQuantity());
    assertEquals(DataValueUtils.getLocalCodedText("mg", "mg"), orderActivity.getStructuredDose().getDoseUnit());
    assertEquals("1/2", orderActivity.getStructuredDose().getDescription().getValue());
    final TitrationType titration =
        TitrationType.getByFullString(
            ((DvCodedText)orderActivity.getAdditionalInstruction().get(0)).getDefiningCode().getCodeString());
    assertEquals(TitrationType.BLOOD_SUGAR, titration);
    //form
    assertEquals("TABLET", orderActivity.getIngredientsAndForm().getForm().getValue());
  }

  @Test
  public void testTransformConstantComplexTherapy()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setTherapyDescription("Taxol 10.0mg, NOSILNA RAZTOPINA: Natrijev Klorid 90ml, 100ml/h, 2x na dan");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setId(1L);
    routeDto.setName("IV");
    therapy.setRoutes(Collections.singletonList(routeDto));
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
        "mL/h", ((DvQuantity)infusionAdministrationDetailsCluster.getDoseAdministrationRate()).getUnits());
    assertEquals(
        new Double(5.0),
        (Double)((DvQuantity)infusionAdministrationDetailsCluster.getDoseAdministrationFormula()).getMagnitude());
    assertEquals(
        "mg/kg/h", ((DvQuantity)infusionAdministrationDetailsCluster.getDoseAdministrationFormula()).getUnits());
    //quantity and strength
    assertEquals(DataValueUtils.getQuantity(100.0, ""), orderActivity.getStructuredDose().getQuantity());
    assertEquals(DataValueUtils.getLocalCodedText("mL", "mL"), orderActivity.getStructuredDose().getDoseUnit());
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
    assertEquals("mL", ratioDenominator1.getDoseUnit().getDefiningCode().getCodeString());
    assertEquals("mL", ratioDenominator1.getDoseUnit().getValue());
    //solution quantity and strength
    assertEquals("2", ((DvCodedText)ingredient.get(1).getName()).getDefiningCode().getCodeString());
    assertEquals("Natrijev klorid Braun 9 mg/mL razt.za inf. vreča 100 mL 1x", ingredient.get(1).getName().getValue());

    final TitrationType titration =
        TitrationType.getByFullString(
            ((DvCodedText)orderActivity.getAdditionalInstruction().get(0)).getDefiningCode().getCodeString());
    assertEquals(TitrationType.INR, titration);
  }

  @Test
  public void testTransformOxygenTherapy()
  {
    final OxygenTherapyDto therapy = new OxygenTherapyDto();
    therapy.setTherapyDescription("Oxygen description");
    final MedicationRouteDto routeDto = new MedicationRouteDto();
    routeDto.setId(1L);
    routeDto.setName("IV");
    therapy.setRoutes(Collections.singletonList(routeDto));
    therapy.setStart(new DateTime(2013, 2, 2, 9, 0));
    therapy.setEnd(new DateTime(2013, 2, 5, 9, 0));
    therapy.setComment("COMMENT1");

    final InfusionIngredientDto medication = new InfusionIngredientDto();
    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(1L);
    medicationDto.setName("Oxygen medication name");
    medication.setMedication(medicationDto);
    medication.setQuantity(2.0);
    medication.setQuantityUnit("mg");
    medication.setQuantityDenominator(10.0);
    medication.setQuantityDenominatorUnit("mL");
    therapy.setMedication(medicationDto);

    // OXYGEN SPECIFIC DATA
    therapy.setFlowRate(50.0);
    therapy.setFlowRateUnit("l/min");
    therapy.setFlowRateMode(AdministrationDetailsCluster.OxygenDeliveryCluster.FlowRateMode.LOW_FLOW);
    therapy.setMinTargetSaturation(0.8);
    therapy.setMaxTargetSaturation(0.9);
    final OxygenStartingDevice startingDevice = new OxygenStartingDevice(AdministrationDetailsCluster.OxygenDeliveryCluster.Route.CPAP_MASK);
    startingDevice.setRouteType("24");
    therapy.setStartingDevice(startingDevice);
    therapy.setHumidification(true);
    therapy.setSpeedDisplay("10ml/h");

    final MedicationToEhrConverter<?> converter = MedicationConverterSelector.getConverter(therapy);
    final MedicationInstructionInstruction instruction = converter.createInstructionFromTherapy(therapy);

    final OrderActivity orderActivity = instruction.getOrder().get(0);

    //narrative
    assertEquals("Oxygen description", instruction.getNarrative().getValue());

    //description
    assertEquals("Oxygen medication name", orderActivity.getMedicine().getValue());
    assertEquals("Oxygen description",orderActivity.getDirections().getValue());

    //timing
    assertNull(orderActivity.getMedicationTiming().getNumberOfAdministrations());
    assertEquals(DataValueUtils.getDateTime(new DateTime(2013, 2, 2, 9, 0)), orderActivity.getMedicationTiming().getStartDate());
    assertEquals(DataValueUtils.getDateTime(new DateTime(2013, 2, 5, 9, 0)), orderActivity.getMedicationTiming().getStopDate());

    //comment
    assertEquals("COMMENT1", orderActivity.getComment().get(0).getValue());

    //route
    assertEquals("IV", orderActivity.getAdministrationDetails().getRoute().get(0).getValue());
    assertEquals("1", orderActivity.getAdministrationDetails().getRoute().get(0).getDefiningCode().getCodeString());

    //Oxygen
    final AdministrationDetailsCluster.OxygenDeliveryCluster oxygenDelivery = orderActivity.getAdministrationDetails()
        .getOxygenDelivery().get(0);
    assertSame(AdministrationDetailsCluster.OxygenDeliveryCluster.Route.CPAP_MASK, oxygenDelivery.getRouteEnum());
    assertTrue(oxygenDelivery.getHumidifier().getHumidiferUsed().isValue());
    assertEquals((Double)50.0, (Double)oxygenDelivery.getAmbientOxygen().getOxygenFlowRate().getMagnitude());
    assertEquals("l/m", oxygenDelivery.getAmbientOxygen().getOxygenFlowRate().getUnits());

    final Double minimumPercentO2 = (double)oxygenDelivery.getAmbientOxygen().getMinimumPercentO2().getNumerator();
    final Double maximumPercentO2 = (double)oxygenDelivery.getAmbientOxygen().getMaximumPercentO2().getNumerator();
    assertEquals(0.8, minimumPercentO2, 2);
    assertEquals(0.9, maximumPercentO2, 2);

    assertEquals(
        AdministrationDetailsCluster.OxygenDeliveryCluster.FlowRateMode.LOW_FLOW,
        oxygenDelivery.getFlowRateModeEnum());
  }

  @Test
  public void testGetOriginalTherapyId()
  {
    final MedicationOrderComposition originComposition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", null, null);
    final MedicationInstructionInstruction originInstruction =
        MedicationsTestUtils.buildTestMedicationInstruction("Medication instruction");
    originComposition.getMedicationDetail().getMedicationInstruction().add(originInstruction);

    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid2::1", null, null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("Medication instruction");
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);

    instruction.getLinks().add(
        OpenEhrRefUtils.getLinkToTdoTarget("origin", EhrLinkType.ORIGIN.getName(), originComposition, originInstruction));

    final String originalTherapyId = medicationsBo.getOriginalTherapyId(composition);
    assertEquals("uid1|Medication instruction", originalTherapyId);
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
        OpenEhrRefUtils.getLinkToTdoTarget("update", EhrLinkType.UPDATE.getName(), composition1, instruction1);
    instruction2.getLinks().add(linkToInstruction1);

    assertTrue(
        medicationsBo.doesInstructionHaveLinkToCompareInstruction(
            instruction2, Pair.of(composition1, instruction1), EhrLinkType.UPDATE));
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
        OpenEhrRefUtils.getLinkToTdoTarget("update", EhrLinkType.UPDATE.getName(), composition1, instruction1);
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
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, 1L, "ORAL", "1", "TBL");
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
            "Daleron", 2L, 500.0, "mg", null, null, null, null, 1L, "ORAL", "1", "TBL");
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
    medicationData1.setAtcGroupCode("ATC 1");
    medicationsMap.put(1L, medicationData1);

    final MedicationDataForTherapyDto medicationData2 = new MedicationDataForTherapyDto();
    medicationData2.setGenericName("Paracetamol");
    medicationData2.setCustomGroupName("Group 1");
    medicationData2.setAtcGroupCode("ATC 1");
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
  public void testFilterMentalHealthDrugsList()
  {
    final List<MentalHealthTherapyDto> mentalHealthTherapyDtos = new ArrayList<>();

    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(1L, 1, TherapyStatusEnum.NORMAL));
    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(2L, 1, TherapyStatusEnum.NORMAL));
    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(1L, 1, TherapyStatusEnum.NORMAL));
    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(2L, 1, TherapyStatusEnum.NORMAL));

    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(2L, 2, TherapyStatusEnum.NORMAL));
    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(2L, 2, TherapyStatusEnum.ABORTED));

    final Set<MentalHealthTherapyDto> mentalHealthMedicationDtoSet = medicationsBo.filterMentalHealthTherapyList(
        mentalHealthTherapyDtos);
    Assert.assertEquals(4, mentalHealthMedicationDtoSet.size());
  }

  private MentalHealthTherapyDto createMentalHealthTherapyDto(
      final long routeId,
      final long medicationId,
      final TherapyStatusEnum therapyStatusEnum)
  {
    final MentalHealthTherapyDto mentalHealthTherapyDto = new MentalHealthTherapyDto();
    mentalHealthTherapyDto.setTherapyStatusEnum(therapyStatusEnum);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(routeId);
    route.setName("route name");

    final MentalHealthMedicationDto mentalHealthMedicationDto = new MentalHealthMedicationDto(
        medicationId,
        "medication name",
        "generic name",
        route);

    mentalHealthTherapyDto.setMentalHealthMedicationDto(mentalHealthMedicationDto);
    return mentalHealthTherapyDto;
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
            null, null, 500.0, "mL", null, null, null, null, 2L, "IV", null, null);
    instruction.getOrder().add(orderActivity);
    final MedicationTimingCluster medicationTiming =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2013, 11, 11, 0, 0, 0), null, null);
    orderActivity.setMedicationTiming(medicationTiming);
    final IngredientsAndFormCluster ingredientsAndForm = new IngredientsAndFormCluster();
    orderActivity.setIngredientsAndForm(ingredientsAndForm);
    final IngredientCluster ingredient1 =
        MedicationsTestUtils.buildTestActiveIngredient("Dopamin1", "1", null, null, 10.0, "mg", 500.0, "mL");
    ingredientsAndForm.getIngredient().add(ingredient1);

    //compare therapy
    final MedicationOrderComposition compareComposition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid2::1", new DateTime(2013, 10, 10, 0, 0, 0), null);
    final MedicationInstructionInstruction compareInstruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    compareComposition.getMedicationDetail().getMedicationInstruction().add(compareInstruction);
    final OrderActivity compareOrderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            null, null, 1000.0, "mL", null, null, null, null, 2L, "IV", null, null);
    compareInstruction.getOrder().add(compareOrderActivity);
    final MedicationTimingCluster compareMedicationTiming =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2013, 10, 10, 0, 0, 0), new DateTime(2013, 11, 11, 0, 0, 0), null);
    compareOrderActivity.setMedicationTiming(compareMedicationTiming);
    final IngredientsAndFormCluster compareIngredientsAndForm = new IngredientsAndFormCluster();
    compareOrderActivity.setIngredientsAndForm(compareIngredientsAndForm);
    final IngredientCluster compareIngredient1 =
        MedicationsTestUtils.buildTestActiveIngredient("Dopamin2", "1", null, null, 10.0, "mg", 1000.0, "mL");
    compareIngredientsAndForm.getIngredient().add(compareIngredient1);

    //medications data map
    final Map<Long, MedicationDataForTherapyDto> medicationsMap = new HashMap<>();

    final MedicationDataForTherapyDto medicationData1 = new MedicationDataForTherapyDto();
    medicationData1.setGenericName("Dopamin");
    medicationData1.setCustomGroupName("Group 1");
    medicationData1.setAtcGroupCode("ATC 1");
    medicationsMap.put(1L, medicationData1);

    final MedicationDataForTherapyDto medicationData2 = new MedicationDataForTherapyDto();
    medicationData2.setGenericName("Dopamin");
    medicationData2.setCustomGroupName("Group 1");
    medicationData2.setAtcGroupCode("ATC 1");
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
    final HibernateMedicationsDao medicationsDaoMock = mock(HibernateMedicationsDao.class);
    medicationsBo.setMedicationsDao(medicationsDaoMock);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setName("route1");
    route.setId(1L);
    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setName("route2");
    route2.setId(2L);
    final Map<Long, MedicationRouteDto> routes = new HashMap<>();
    routes.put(2L, route2);
    routes.put(1L, route);
    Mockito.when(medicationRoutesValueHolder.getValue()).thenReturn(routes);

    final Map<Long, MedicationDataForTherapyDto> medicationsDataMap = new HashMap<>();
    final MedicationDataForTherapyDto medicationData1 = new MedicationDataForTherapyDto();
    medicationData1.setGenericName("Paracetamol");
    medicationData1.setCustomGroupName("Group 1");
    medicationData1.setAtcGroupCode("ATC 1");
    medicationsDataMap.put(1L, medicationData1);

    //similar to admission
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", new DateTime(2013, 11, 11, 0, 0, 0), "aaa");
    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, 1L, "ORAL", "1", "TBL");
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
            "Lekadol", 1L, 1000.0, "mg", null, null, null, null, 1L, "ORAL", "1", "TBL");
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
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, 1L, "ORAL", "1", "TBL");
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
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, 1L, "ORAL", "1", "TBL");
    instruction4.getOrder().add(orderActivity4);
    final MedicationTimingCluster medicationTiming4 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            2L, null, null, null, new DateTime(2014, 6, 12, 10, 0, 0), new DateTime(2014, 6, 15, 10, 0, 0), null);
    orderActivity4.setMedicationTiming(medicationTiming4);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs4 = new ArrayList<>();
    final Link linkToInstruction11 =
        OpenEhrRefUtils.getLinkToTdoTarget("update", EhrLinkType.UPDATE.getName(), composition2, instruction2);
    instruction2.getLinks().add(linkToInstruction11);
    final Link linkToInstruction22 =
        OpenEhrRefUtils.getLinkToTdoTarget("update", EhrLinkType.UPDATE.getName(), composition4, instruction4);
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
    final Map<Long, MedicationHolderDto> medicationHolderMap = new HashMap<>();
    final MedicationHolderDto medicationDto1 = new MedicationHolderDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    medicationHolderMap.put(1L, medicationDto1);

    final MedicationHolderDto medicationDto2 = new MedicationHolderDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Primotren 20x500mg");
    medicationHolderMap.put(2L, medicationDto2);

    final MedicationsValueHolder medicationsValueHolder = mock(MedicationsValueHolder.class);
    when(medicationsValueHolder.getValue()).thenReturn(medicationHolderMap);
    medicationsBo.setMedicationsValueHolder(medicationsValueHolder);

    //simple therapy
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", null, null);
    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");
    final MedicationTimingCluster medicationTimingCluster =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, null, null);

    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, 1L, "ORAL", "1", "TBL");
    instruction1.getOrder().add(orderActivity1);
    orderActivity1.setMedicationTiming(medicationTimingCluster);
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);

    final List<MedicationOrderComposition> medicationInstructionsList1 = new ArrayList<>();
    medicationInstructionsList1.add(composition1);
    final List<MedicationForWarningsSearchDto> warningsSearchDtosList1 = medicationsBo.extractWarningsSearchDtos(
        medicationInstructionsList1);

    assertEquals((Double)500.0, warningsSearchDtosList1.get(0).getDoseAmount());
    assertEquals("mg", warningsSearchDtosList1.get(0).getDoseUnit());
    assertEquals("1", warningsSearchDtosList1.get(0).getRouteCode());

    //complex therapy
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid1::1", null, null);
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 1");

    final IngredientCluster ingredient =
        MedicationsTestUtils.buildTestActiveIngredient("Primotren", "0002", null, null, 10.0, "mg", 3.0, "mL");
    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(null, null, null, null, null, null, null, null, 1L, "ORAL", "1", "TBL");
    instruction2.getOrder().add(orderActivity2);
    final IngredientsAndFormCluster ingredientsAndForm = new IngredientsAndFormCluster();
    orderActivity2.setIngredientsAndForm(ingredientsAndForm);
    ingredientsAndForm.getIngredient().add(ingredient);
    orderActivity2.setMedicationTiming(medicationTimingCluster);
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);

    final List<MedicationOrderComposition> medicationInstructionsList2 = new ArrayList<>();
    medicationInstructionsList2.add(composition2);
    final List<MedicationForWarningsSearchDto> warningsSearchDtosList2 = medicationsBo.extractWarningsSearchDtos(
        medicationInstructionsList2);

    assertEquals((Double)10.0, warningsSearchDtosList2.get(0).getDoseAmount());
    assertEquals("mg", warningsSearchDtosList2.get(0).getDoseUnit());
    assertEquals("1", warningsSearchDtosList2.get(0).getRouteCode());
  }

  @Test
  public void testBuildWarningSearchDtoFromIngredient()
  {
    //problems with date conversion
    final DateTime start =
        DataValueUtils.getDateTime(DataValueUtils.getDateTime(new DateTime(2013, 3, 25, 12, 0, DateTimeZone.UTC)));

    final Map<Long, MedicationHolderDto> medicationHolderMap = new HashMap<>();
    final MedicationHolderDto medicationDto1 = new MedicationHolderDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    medicationHolderMap.put(1L, medicationDto1);

    final MedicationHolderDto medicationDto2 = new MedicationHolderDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Primotren 20x500mg");
    medicationHolderMap.put(2L, medicationDto2);

    final MedicationsValueHolder medicationsValueHolder = mock(MedicationsValueHolder.class);
    when(medicationsValueHolder.getValue()).thenReturn(medicationHolderMap);
    medicationsBo.setMedicationsValueHolder(medicationsValueHolder);

    final MedicationTimingCluster medicationTimingCluster =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, null, null);

    final IngredientCluster ingredient1 =
        MedicationsTestUtils.buildTestActiveIngredient("Lekadol", "0001", 500.0, "mg", null, null, null, null);
    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(null, null, null, null, null, null, null, null, 1L, "ORAL", "1", "TBL");
    orderActivity1.setMedicationTiming(medicationTimingCluster);
    final MedicationForWarningsSearchDto warningsSearchDto1 =
        medicationsBo.buildWarningSearchDtoFromIngredient(orderActivity1, ingredient1);
    assertEquals((Double)500.0, warningsSearchDto1.getDoseAmount());
    assertEquals("mg", warningsSearchDto1.getDoseUnit());
    assertEquals("1", warningsSearchDto1.getRouteCode());

    final IngredientCluster ingredient2 =
        MedicationsTestUtils.buildTestActiveIngredient("Primotren", "0002", null, null, 10.0, "mg", 3.0, "mL");
    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(null, null, null, null, null, null, null, null, 1L, "ORAL", "1", "TBL");
    orderActivity2.setMedicationTiming(medicationTimingCluster);
    final MedicationForWarningsSearchDto warningsSearchDto2 =
        medicationsBo.buildWarningSearchDtoFromIngredient(orderActivity2, ingredient2);
    assertEquals((Double)10.0, warningsSearchDto2.getDoseAmount());
    assertEquals("mg", warningsSearchDto2.getDoseUnit());
    assertEquals("1", warningsSearchDto2.getRouteCode());
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
    final HibernateMedicationsDao medicationsDaoMock = mock(HibernateMedicationsDao.class);
    medicationsBo.setMedicationsDao(medicationsDaoMock);

    final MedicationTimingCluster medicationTimingCluster =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, null, null);

    final OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, 1L, "ORAL", "1", "TBL");
    orderActivity1.setMedicationTiming(medicationTimingCluster);
    final MedicationForWarningsSearchDto warningsSearchDto1 =
        medicationsBo.buildWarningSearchDtoFromMedication(orderActivity1);
    assertEquals((Double)500.0, warningsSearchDto1.getDoseAmount());
    assertEquals("mg", warningsSearchDto1.getDoseUnit());
    assertEquals("1", warningsSearchDto1.getRouteCode());

    final OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Primotren", 1L, null, null, 10.0, "mg", 3.0, "mL", 1L, "ORAL", "1", "TBL");
    orderActivity2.setMedicationTiming(medicationTimingCluster);
    final MedicationForWarningsSearchDto warningsSearchDto2 =
        medicationsBo.buildWarningSearchDtoFromMedication(orderActivity2);
    assertEquals((Double)10.0, warningsSearchDto2.getDoseAmount());
    assertEquals("mg", warningsSearchDto2.getDoseUnit());
    assertEquals("1", warningsSearchDto2.getRouteCode());
  }

  @Test
  public void testBuildWarningSearchDto()
  {
    //problems with date conversion
    final DateTime start =
        DataValueUtils.getDateTime(DataValueUtils.getDateTime(new DateTime(2013, 3, 25, 12, 0, DateTimeZone.UTC)));

    final Map<Long, MedicationHolderDto> medicationHolderMap = new HashMap<>();
    final MedicationHolderDto medicationDto = new MedicationHolderDto();
    medicationDto.setId(1L);
    medicationDto.setName("Lekadol 20x500mg");
    medicationHolderMap.put(1L, medicationDto);

    final MedicationsValueHolder medicationsValueHolder = mock(MedicationsValueHolder.class);
    when(medicationsValueHolder.getValue()).thenReturn(medicationHolderMap);
    medicationsBo.setMedicationsValueHolder(medicationsValueHolder);

    //end not set
    final DateTime stop1 = null;
    final MedicationTimingCluster medicationTimingCluster1 =
        MedicationsTestUtils.buildMedicationTimingCluster(4L, null, null, null, start, stop1, null);
    final MedicationForWarningsSearchDto warningsSearchDto1 =
        medicationsBo.buildWarningSearchDto(medicationTimingCluster1, 1L, "ORAL", 500.0, "mg");
    assertEquals(4L, (long)warningsSearchDto1.getFrequency());
    assertEquals("ORAL", warningsSearchDto1.getRouteCode());
    assertEquals(Double.valueOf(500.0), warningsSearchDto1.getDoseAmount());
    assertEquals("mg", warningsSearchDto1.getDoseUnit());
    assertEquals("Lekadol 20x500mg", warningsSearchDto1.getName());
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
        medicationsBo.buildWarningSearchDto(medicationTimingCluster2, 1L, "ORAL", 500.0, "mg");
    assertEquals(4L, (long)warningsSearchDto2.getFrequency());
    assertEquals("ORAL", warningsSearchDto2.getRouteCode());
    assertEquals(Double.valueOf(500.0), warningsSearchDto2.getDoseAmount());
    assertEquals("mg", warningsSearchDto2.getDoseUnit());
    assertEquals("Lekadol 20x500mg", warningsSearchDto2.getName());
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
  public void testTherapyModifiedFromLastReviewWithUpdateLink()
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
        OpenEhrRefUtils.getLinkToTdoTarget("update", EhrLinkType.UPDATE.getName(), composition1, instruction1);
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
        medicationsBo.isTherapyModifiedFromLastReview(instruction2, actionsList, new DateTime(2013, 5, 13, 5, 0));
    assertTrue(modified1);
    final boolean modified2 =
        medicationsBo.isTherapyModifiedFromLastReview(instruction2, actionsList, new DateTime(2013, 5, 12, 5, 0));
    assertFalse(modified2);
  }

  @Test
  public void testTherapyModifiedFromLastReviewWithModifyAction()
  {
    final MedicationOrderComposition composition =
        MedicationsTestUtils.buildTestMedicationOrderComposition("uid2::1", null, null);
    final MedicationInstructionInstruction instruction =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstructionInstruction 2");
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);

    final List<MedicationActionAction> actionsList = new ArrayList<>();
    actionsList.add(MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.SCHEDULE, new DateTime(2013, 5, 10, 12, 0)));
    actionsList.add(MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.START, new DateTime(2013, 5, 10, 12, 0)));
    actionsList.add(MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 10, 12, 0)));
    actionsList.add(MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 11, 12, 0)));
    actionsList.add(MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2013, 5, 12, 12, 0)));
    actionsList.add(MedicationsTestUtils.buildMedicationAction(MedicationActionEnum.MODIFY_EXISTING, new DateTime(2013, 5, 12, 13, 0)));
    composition.getMedicationDetail().getMedicationAction().addAll(actionsList);

    final boolean modified = medicationsBo.isTherapyModifiedFromLastReview(instruction, actionsList, new DateTime(2013, 5, 12, 5, 0));
    assertTrue(modified);
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

    RequestContextHolder.setContext(
        new RequestContextImpl(
            1L,
            GlobalAuditContext.current(),
            new DateTime(),
            Opt.of(ConstantUserMetadataProvider.createMetadata("1", "First Last"))));

    final MedicationReferenceWeightComposition comp =
        medicationsBo.buildReferenceWeightComposition(10.0, testTimestamp);
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
    calculationDto.setQuantityDenominator(5.0);
    final Double formula = medicationsBo.calculateInfusionFormulaFromRate(10.0, calculationDto, 10.0, "mg/kg/h", 10.0);
    assertEquals(new Double(2.0), formula);
  }

  @Test
  public void testCalculateInfusionRateFromFormula1()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(10.0);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setQuantityDenominator(5.0);
    final Double rate = medicationsBo.calculateInfusionRateFromFormula(2.0, "mg/kg/h", calculationDto, 10.0, 70.0);
    assertEquals(new Double(10.0), rate);
  }

  @Test
  public void testCalculateInfusionFormulaFromRate2()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(0.5);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setQuantityDenominator(2.0);
    final Double formula = medicationsBo.calculateInfusionFormulaFromRate(21.0, calculationDto, 10.0, "microgram/kg/min", 5.0);
    final double formulaWithNormalPrecision = Math.round(formula * 1000.0) / 1000.0;
    assertEquals(new Double(17.5), new Double(formulaWithNormalPrecision));
  }

  @Test
  public void testCalculateInfusionRateFromFormula2()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(0.5);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setQuantityDenominator(2.0);
    final Double rate = medicationsBo.calculateInfusionRateFromFormula(17.5, "microgram/kg/min", calculationDto, 5.0, 70.0);
    final double rateWithNormalPrecision = Math.round(rate * 1000.0) / 1000.0;
    assertEquals(new Double(21.0), new Double(rateWithNormalPrecision));
  }

  @Test
  public void testCalculateInfusionFormulaFromRate3()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(50.0);
    calculationDto.setQuantityUnit("nanogram");
    calculationDto.setQuantityDenominator(1.0);
    final Double formula = medicationsBo.calculateInfusionFormulaFromRate(100.0, calculationDto, 10.0, "microgram/kg/d", 10.0);
    final double formulaWithNormalPrecision = Math.round(formula * 1000.0) / 1000.0;
    assertEquals(new Double(12.0), new Double(formulaWithNormalPrecision));
  }

  @Test
  public void testCalculateInfusionRateFromFormula3()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(50.0);
    calculationDto.setQuantityUnit("nanogram");
    calculationDto.setQuantityDenominator(1.0);
    final Double rate = medicationsBo.calculateInfusionRateFromFormula(12.0, "microgram/kg/d", calculationDto, 10.0, 70.0);
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
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionRateCalculationDto data = medicationsBo.getInfusionRateCalculationData(therapy);
    assertEquals(new Double(10.0), data.getQuantity());
    assertEquals("mg", data.getQuantityUnit());
    assertEquals(new Double(5.0), data.getQuantityDenominator());
  }

  @Test
  public void testGetInfusionRateCalculationData2()
  {
    //single infusion ingredient (MEDICATION), continuous infusion

    final Map<Long, MedicationHolderDto> medicationHolderMap = new HashMap<>();
    final MedicationHolderDto medicationDto1 = new MedicationHolderDto();
    medicationDto1.setId(1L);
    final MedicationIngredientDto definingIngredient = new MedicationIngredientDto();
    definingIngredient.setId(1L);
    definingIngredient.setIngredientName("Paracetamol");
    definingIngredient.setStrengthNumerator(10.0);
    definingIngredient.setStrengthNumeratorUnit("mg");
    definingIngredient.setStrengthDenominator(5.0);
    definingIngredient.setStrengthDenominatorUnit("mL");
    medicationDto1.setDefiningIngredient(definingIngredient);
    medicationHolderMap.put(1L, medicationDto1);

    final MedicationsValueHolder medicationsValueHolder = mock(MedicationsValueHolder.class);
    when(medicationsValueHolder.getValue()).thenReturn(medicationHolderMap);
    medicationsBo.setMedicationsValueHolder(medicationsValueHolder);

    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setStart(new DateTime(2013, 12, 10, 12, 0, 0));
    therapy.setContinuousInfusion(true);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);
    medication.setId(1L);

    final InfusionRateCalculationDto data = medicationsBo.getInfusionRateCalculationData(therapy);
    assertEquals(new Double(10.0), data.getQuantity());
    assertEquals("mg", data.getQuantityUnit());
    assertEquals(new Double(5.0), data.getQuantityDenominator());
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
    ingredient1.setQuantityDenominator(5.0);
    final MedicationDto medication1 = new MedicationDto();
    ingredient1.setMedication(medication1);
    medication1.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient2);
    ingredient2.setQuantityDenominator(50.0);
    final MedicationDto solution = new MedicationDto();
    ingredient2.setMedication(solution);
    solution.setMedicationType(MedicationTypeEnum.SOLUTION);

    final InfusionRateCalculationDto data = medicationsBo.getInfusionRateCalculationData(therapy);
    assertEquals(new Double(10.0), data.getQuantity());
    assertEquals("mg", data.getQuantityUnit());
    assertEquals(new Double(55.0), data.getQuantityDenominator());
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
    ingredient1.setQuantityDenominator(5.0);
    final MedicationDto medication1 = new MedicationDto();
    ingredient1.setMedication(medication1);
    medication1.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient2);
    ingredient2.setQuantityUnit("mg");
    ingredient2.setQuantity(2.0);
    ingredient2.setQuantityDenominator(1.0);
    final MedicationDto solution = new MedicationDto();
    ingredient2.setMedication(solution);
    solution.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionRateCalculationDto data = medicationsBo.getInfusionRateCalculationData(therapy);
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
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionFormulaFromRate(therapy, 10.0, 70.0);
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
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionRateFromFormula(therapy, 10.0, 70.0);
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
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionFormulaFromRate(therapy, 10.0, 70.0);
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
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionRateFromFormula(therapy, 10.0, 70.0);
    assertEquals(new Double(10.0), therapy.getTimedDoseElements().get(0).getDoseElement().getRate());
    assertEquals(new Double(20.0), therapy.getTimedDoseElements().get(1).getDoseElement().getRate());
  }

  @Test
  public void testGetTherapySuspendReasonForSuspendedTherapy()
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair =
        buildTestInstructionForSuspendReasonTest();

    addActionToSuspendReasonTestData(
        instructionPair.getFirst(),
        instructionPair.getSecond(),
        MedicationActionEnum.SUSPEND,
        new DateTime(2016, 6, 10, 12, 0));

    final TherapyChangeReasonDto therapySuspendReason =
        medicationsBo.getTherapySuspendReason(instructionPair.getFirst(), instructionPair.getSecond());
    assertNotNull(therapySuspendReason);
    assertNotNull(therapySuspendReason.getChangeReason());
    assertEquals(
        TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
        therapySuspendReason.getChangeReason().getCode());
    assertEquals(
        TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
        therapySuspendReason.getChangeReason().getName());
  }

  @Test
  public void testGetTherapySuspendReasonForReissuedTherapy()
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair =
        buildTestInstructionForSuspendReasonTest();

    final MedicationOrderComposition composition = instructionPair.getFirst();
    final MedicationInstructionInstruction instruction = instructionPair.getSecond();

    addActionToSuspendReasonTestData(
        composition,
        instruction,
        MedicationActionEnum.SUSPEND,
        new DateTime(2016, 6, 10, 12, 0));

    addActionToSuspendReasonTestData(
        composition,
        instruction,
        MedicationActionEnum.REISSUE,
        new DateTime(2016, 6, 10, 13, 0));

    final TherapyChangeReasonDto therapySuspendReason = medicationsBo.getTherapySuspendReason(composition, instruction);
    assertNull(therapySuspendReason);
  }

  @Test
  public void testGetTherapySuspendReasonNoAction()
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair =
        buildTestInstructionForSuspendReasonTest();

    final TherapyChangeReasonDto therapySuspendReason =
        medicationsBo.getTherapySuspendReason(instructionPair.getFirst(), instructionPair.getSecond());
    assertNull(therapySuspendReason);
  }

  private Pair<MedicationOrderComposition, MedicationInstructionInstruction> buildTestInstructionForSuspendReasonTest()
  {
    final MedicationInstructionInstruction instruction = new MedicationInstructionInstruction();
    instruction.setName(DataValueUtils.getText("Medication instruction"));

    final MedicationOrderComposition composition = new MedicationOrderComposition();
    final UidBasedId uidBasedId = new ObjectVersionId();
    uidBasedId.setValue("uid1");
    composition.setUid(uidBasedId);

    final MedicationDetailSection medicationDetail = new MedicationDetailSection();
    medicationDetail.getMedicationInstruction().add(instruction);
    composition.setMedicationDetail(medicationDetail);

    return Pair.of(composition, instruction);
  }

  private void addActionToSuspendReasonTestData(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final MedicationActionEnum medicationActionEnum,
      final DateTime actionTime)
  {
    final MedicationActionAction suspendAction =
        MedicationsEhrUtils.buildMedicationAction(composition, medicationActionEnum, actionTime);
    MedicationsEhrUtils.fillActionInstructionId(
        suspendAction.getInstructionDetails().getInstructionId(), composition, instruction, "uid1");
    suspendAction.getReason().add(DataValueUtils.getLocalCodedText(
        TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
        TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString()));
    composition.getMedicationDetail().getMedicationAction().add(suspendAction);
  }

  @Test
  public void pharmacyReviewJsonDeserializer()
  {
    final String json =
        "[\n" +
            "  {\n" +
            "    \"configs\": [],\n" +
            "    \"createTimestamp\": \"2015-04-14T14:58:14.346Z\",\n" +
            "    \"compositionId\": null,\n" +
            "    \"composer\": {\n" +
            "      \"id\": \"2323321\",\n" +
            "      \"name\": \"Today Guy\"\n" +
            "    },\n" +
            "    \"referBackToPescriber\": null,\n" +
            "    \"relatedTherapies\": [\n" +
            "      {\n" +
            "        \"therapy\": {\n" +
            "          \"doseElement\": {\n" +
            "            \"quantity\": 4,\n" +
            "            \"doseDescription\": null,\n" +
            "            \"quantityDenominator\": 0.4,\n" +
            "            \"serialVersionUID\": 0\n" +
            "          },\n" +
            "          \"medication\": {\n" +
            "            \"id\": 1717028,\n" +
            "            \"name\": \"ASPIRIN 10 MG SVEČKA ZA OTROKE\",\n" +
            "            \"shortName\": \"ASPIRIN 10 mg SVEČKA ZA OTROKE \",\n" +
            "            \"genericName\": \"acetilsalicilna kislina\",\n" +
            "            \"medicationType\": \"MEDICATION\",\n" +
            "            \"displayName\": \"acetilsalicilna kislina (ASPIRIN 10 MG SVEČKA ZA OTROKE)\",\n" +
            "            \"serialVersionUID\": 0\n" +
            "          },\n" +
            "          \"quantityUnit\": \"mg\",\n" +
            "          \"doseForm\": {\n" +
            "            \"doseFormType\": \"SUPPOSITORY\",\n" +
            "            \"medicationOrderFormType\": \"SIMPLE\",\n" +
            "            \"serialVersionUID\": 0,\n" +
            "            \"name\": \"Svečka\",\n" +
            "            \"code\": \"289\",\n" +
            "            \"description\": null,\n" +
            "            \"deleted\": false,\n" +
            "            \"auditInfo\": null,\n" +
            "            \"version\": 0,\n" +
            "            \"id\": 289\n" +
            "          },\n" +
            "          \"quantityDenominatorUnit\": \"svečka\",\n" +
            "          \"quantityDisplay\": \"4 mg\",\n" +
            "          \"compositionUid\": \"07e5280c-3309-48ce-a8b6-04ad41a53da5::prod.pediatrics.marand.si::3\",\n" +
            "          \"ehrOrderName\": \"Medication instruction\",\n" +
            "          \"medicationOrderFormType\": \"SIMPLE\",\n" +
            "          \"variable\": false,\n" +
            "          \"therapyDescription\": \"acetilsalicilna kislina (ASPIRIN 10 MG SVEČKA ZA OTROKE) - 4 mg - 3X na dan - rect\",\n" +
            "          \"route\": {\n" +
            "            \"type\": null,\n" +
            "            \"unlicensedRoute\": false,\n" +
            "            \"serialVersionUID\": 0,\n" +
            "            \"code\": \"35\",\n" +
            "            \"name\": \"rect\",\n" +
            "            \"id\": 0\n" +
            "          },\n" +
            "          \"dosingFrequency\": {\n" +
            "            \"type\": \"DAILY_COUNT\",\n" +
            "            \"value\": 3\n" +
            "          },\n" +
            "          \"dosingDaysFrequency\": null,\n" +
            "          \"daysOfWeek\": null,\n" +
            "          \"start\": {\n" +
            "            \"data\": \"2015-03-11T13:48:00.000+01:00\",\n" +
            "            \"default\": \"11.3.2015\",\n" +
            "            \"short.date\": \"11.3.2015\",\n" +
            "            \"short.time\": \"13:48\",\n" +
            "            \"short.date.time\": \"11.3.2015 13:48\"\n" +
            "          },\n" +
            "          \"end\": null,\n" +
            "          \"whenNeeded\": false,\n" +
            "          \"comment\": null,\n" +
            "          \"clinicalIndication\": null,\n" +
            "          \"prescriberName\": null,\n" +
            "          \"composerName\": \"Tadej Avčin\",\n" +
            "          \"startCriterion\": null,\n" +
            "          \"applicationPrecondition\": null,\n" +
            "          \"frequencyDisplay\": \"3X na dan\",\n" +
            "          \"daysFrequencyDisplay\": null,\n" +
            "          \"whenNeededDisplay\": null,\n" +
            "          \"startCriterionDisplay\": null,\n" +
            "          \"daysOfWeekDisplay\": null,\n" +
            "          \"applicationPreconditionDisplay\": null,\n" +
            "          \"formattedTherapyDisplay\": \"<span class='GenericName TextDataBold'>acetilsalicilna kislina </span><span class='MedicationName TextData'>(ASPIRIN 10 MG SVEČKA ZA OTROKE) </span><br><span class='DoseLabel TextLabel'>ODMEREK </span><span class='Quantity TextData'>4 mg </span><span class='Delimiter TextData'><span> &ndash; </span> </span><span class='Frequency TextData'>3X na dan </span><span class='Delimiter TextData'><span> &ndash; </span> </span><span class='Route TextData'>rect </span><span class='TherapyInterval'><br><span class='FromLabel TextLabel'>Od </span><span class='From TextData'>11.3.2015 13:48 </span></span>\",\n" +
            "          \"pastDaysOfTherapy\": null,\n" +
            "          \"linkName\": null,\n" +
            "          \"maxDailyFrequency\": null,\n" +
            "          \"createdTimestamp\": \"2015-03-11T13:48:41.174+01:00\",\n" +
            "          \"tags\": [],\n" +
            "          \"criticalWarnings\": [],\n" +
            "          \"serialVersionUID\": 0\n" +
            "        },\n" +
            "        \"therapyStatus\": \"NORMAL\",\n" +
            "        \"doctorReviewNeeded\": true,\n" +
            "        \"therapyEndsBeforeNextRounds\": false,\n" +
            "        \"modifiedFromLastReview\": false,\n" +
            "        \"modified\": false,\n" +
            "        \"active\": true,\n" +
            "        \"consecutiveDay\": 26,\n" +
            "        \"showConsecutiveDay\": false\n" +
            "      },\n" +
            "      {\n" +
            "        \"therapy\": {\n" +
            "          \"timedDoseElements\": [\n" +
            "            {\n" +
            "              \"doseElement\": {\n" +
            "                \"quantity\": 15,\n" +
            "                \"doseDescription\": null,\n" +
            "                \"quantityDenominator\": null,\n" +
            "                \"serialVersionUID\": 0\n" +
            "              },\n" +
            "              \"doseTime\": {\n" +
            "                \"hour\": 8,\n" +
            "                \"minute\": 0\n" +
            "              },\n" +
            "              \"date\": null,\n" +
            "              \"timeDisplay\": \"08:00\",\n" +
            "              \"quantityDisplay\": \"15 mg\",\n" +
            "              \"serialVersionUID\": 0\n" +
            "            },\n" +
            "            {\n" +
            "              \"doseElement\": {\n" +
            "                \"quantity\": 20,\n" +
            "                \"doseDescription\": null,\n" +
            "                \"quantityDenominator\": null,\n" +
            "                \"serialVersionUID\": 0\n" +
            "              },\n" +
            "              \"doseTime\": {\n" +
            "                \"hour\": 13,\n" +
            "                \"minute\": 0\n" +
            "              },\n" +
            "              \"date\": null,\n" +
            "              \"timeDisplay\": \"13:00\",\n" +
            "              \"quantityDisplay\": \"20 mg\",\n" +
            "              \"serialVersionUID\": 0\n" +
            "            },\n" +
            "            {\n" +
            "              \"doseElement\": {\n" +
            "                \"quantity\": 15,\n" +
            "                \"doseDescription\": null,\n" +
            "                \"quantityDenominator\": null,\n" +
            "                \"serialVersionUID\": 0\n" +
            "              },\n" +
            "              \"doseTime\": {\n" +
            "                \"hour\": 17,\n" +
            "                \"minute\": 0\n" +
            "              },\n" +
            "              \"date\": null,\n" +
            "              \"timeDisplay\": \"17:00\",\n" +
            "              \"quantityDisplay\": \"15 mg\",\n" +
            "              \"serialVersionUID\": 0\n" +
            "            },\n" +
            "            {\n" +
            "              \"doseElement\": {\n" +
            "                \"quantity\": 20,\n" +
            "                \"doseDescription\": null,\n" +
            "                \"quantityDenominator\": null,\n" +
            "                \"serialVersionUID\": 0\n" +
            "              },\n" +
            "              \"doseTime\": {\n" +
            "                \"hour\": 21,\n" +
            "                \"minute\": 0\n" +
            "              },\n" +
            "              \"date\": null,\n" +
            "              \"timeDisplay\": \"21:00\",\n" +
            "              \"quantityDisplay\": \"20 mg\",\n" +
            "              \"serialVersionUID\": 0\n" +
            "            }\n" +
            "          ],\n" +
            "          \"medication\": {\n" +
            "            \"id\": 1061700,\n" +
            "            \"name\": \"SINECOD 50 mg film.obl.tbl. \",\n" +
            "            \"shortName\": \"Sinecod tbl.\",\n" +
            "            \"genericName\": \"butamirat\",\n" +
            "            \"medicationType\": \"MEDICATION\",\n" +
            "            \"displayName\": \"butamirat (SINECOD 50 mg film.obl.tbl. )\",\n" +
            "            \"serialVersionUID\": 0\n" +
            "          },\n" +
            "          \"quantityUnit\": \"mg\",\n" +
            "          \"doseForm\": {\n" +
            "            \"doseFormType\": null,\n" +
            "            \"medicationOrderFormType\": \"SIMPLE\",\n" +
            "            \"serialVersionUID\": 0,\n" +
            "            \"name\": \"Tableta s podaljšanim sproščanjem\",\n" +
            "            \"code\": \"296\",\n" +
            "            \"description\": null,\n" +
            "            \"deleted\": false,\n" +
            "            \"auditInfo\": null,\n" +
            "            \"version\": 0,\n" +
            "            \"id\": 296\n" +
            "          },\n" +
            "          \"quantityDenominatorUnit\": null,\n" +
            "          \"quantityDisplay\": \"15-20-15-20 mg\",\n" +
            "          \"compositionUid\": \"8da7c530-03c8-44ec-b022-8539c43f0b71::prod.pediatrics.marand.si::3\",\n" +
            "          \"ehrOrderName\": \"Medication instruction\",\n" +
            "          \"medicationOrderFormType\": \"SIMPLE\",\n" +
            "          \"variable\": true,\n" +
            "          \"therapyDescription\": \"butamirat (SINECOD 50 mg film.obl.tbl. ) - 15-20-15-20 mg - 4X na dan - po\",\n" +
            "          \"route\": {\n" +
            "            \"type\": null,\n" +
            "            \"unlicensedRoute\": false,\n" +
            "            \"serialVersionUID\": 0,\n" +
            "            \"code\": \"34\",\n" +
            "            \"name\": \"po\",\n" +
            "            \"id\": 0\n" +
            "          },\n" +
            "          \"dosingFrequency\": {\n" +
            "            \"type\": \"DAILY_COUNT\",\n" +
            "            \"value\": 4\n" +
            "          },\n" +
            "          \"dosingDaysFrequency\": null,\n" +
            "          \"daysOfWeek\": null,\n" +
            "          \"start\": {\n" +
            "            \"data\": \"2015-03-11T14:17:00.000+01:00\",\n" +
            "            \"default\": \"11.3.2015\",\n" +
            "            \"short.date\": \"11.3.2015\",\n" +
            "            \"short.time\": \"14:17\",\n" +
            "            \"short.date.time\": \"11.3.2015 14:17\"\n" +
            "          },\n" +
            "          \"end\": null,\n" +
            "          \"whenNeeded\": false,\n" +
            "          \"comment\": null,\n" +
            "          \"clinicalIndication\": null,\n" +
            "          \"prescriberName\": null,\n" +
            "          \"composerName\": \"Tadej Avčin\",\n" +
            "          \"startCriterion\": null,\n" +
            "          \"applicationPrecondition\": null,\n" +
            "          \"frequencyDisplay\": \"4X na dan\",\n" +
            "          \"daysFrequencyDisplay\": null,\n" +
            "          \"whenNeededDisplay\": null,\n" +
            "          \"startCriterionDisplay\": null,\n" +
            "          \"daysOfWeekDisplay\": null,\n" +
            "          \"applicationPreconditionDisplay\": null,\n" +
            "          \"formattedTherapyDisplay\": \"<span class='GenericName TextDataBold'>butamirat </span><span class='MedicationName TextData'>(SINECOD 50 mg film.obl.tbl. ) </span><br><span class='DoseLabel TextLabel'>ODMEREK </span><span class='Quantity TextData'>15-20-15-20 mg </span><span class='Delimiter TextData'><span> &ndash; </span> </span><span class='Frequency TextData'>4X na dan </span><span class='Delimiter TextData'><span> &ndash; </span> </span><span class='Route TextData'>po </span><span class='TherapyInterval'><br><span class='FromLabel TextLabel'>Od </span><span class='From TextData'>11.3.2015 14:17 </span></span>\",\n" +
            "          \"pastDaysOfTherapy\": null,\n" +
            "          \"linkName\": null,\n" +
            "          \"maxDailyFrequency\": null,\n" +
            "          \"createdTimestamp\": \"2015-03-11T14:17:33.693+01:00\",\n" +
            "          \"tags\": [],\n" +
            "          \"criticalWarnings\": [],\n" +
            "          \"serialVersionUID\": 0\n" +
            "        },\n" +
            "        \"therapyStatus\": \"NORMAL\",\n" +
            "        \"doctorReviewNeeded\": true,\n" +
            "        \"therapyEndsBeforeNextRounds\": false,\n" +
            "        \"modifiedFromLastReview\": false,\n" +
            "        \"modified\": false,\n" +
            "        \"active\": true,\n" +
            "        \"consecutiveDay\": 26,\n" +
            "        \"showConsecutiveDay\": false\n" +
            "      }\n" +
            "    ],\n" +
            "    \"drugRelatedProblem\": {\n" +
            "      \"c\": {\n" +
            "        \"categories\": [\n" +
            "          {\n" +
            "            \"id\": 123,\n" +
            "            \"name\": \"Adherence issue\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"outcome\": {\n" +
            "          \"id\": 123,\n" +
            "          \"name\": \"Cost saving only\"\n" +
            "        },\n" +
            "        \"impact\": {\n" +
            "          \"id\": 123,\n" +
            "          \"name\": \"Potentially severe\"\n" +
            "        },\n" +
            "        \"recommendation\": \"Withold Warfarin for next 2 days as INR >6. Repeat INR and reassess dose based on tomorrow's result.\"\n" +
            "      },\n" +
            "      \"configs\": [],\n" +
            "      \"categories\": [\n" +
            "        {\n" +
            "          \"id\": 123,\n" +
            "          \"name\": \"Adherence issue\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"outcome\": {\n" +
            "        \"id\": 123,\n" +
            "        \"name\": \"Cost saving only\"\n" +
            "      },\n" +
            "      \"impact\": {\n" +
            "        \"id\": 123,\n" +
            "        \"name\": \"Potentially severe\"\n" +
            "      },\n" +
            "      \"recommendation\": \"Withold Warfarin for next 2 days as INR >6. Repeat INR and reassess dose based on tomorrow's result.\"\n" +
            "    },\n" +
            "    \"pharmacokineticIssue\": null,\n" +
            "    \"patientRelatedProblem\": null\n" +
            "  }\n" +
            "]\n";
    JsonUtil.fromJson(json, PharmacistReviewDto[].class, TherapyJsonDeserializer.INSTANCE.getTypeAdapters());
  }

  @Test
  public void testSortTherapyTemplates()
  {
    final TherapyTemplatesDto templates = new TherapyTemplatesDto();
    final TherapyTemplateDto template = new TherapyTemplateDto();
    final List<TherapyTemplateElementDto> templateElements = new ArrayList<>();
    template.setTemplateElements(templateElements);
    templates.getOrganizationTemplates().add(template);

    final TherapyTemplateElementDto element1 = new TherapyTemplateElementDto();
    final ConstantSimpleTherapyDto therapy1 = new ConstantSimpleTherapyDto();
    therapy1.setTherapyDescription("Lekadol 500mg 1x per day po");
    element1.setTherapy(therapy1);
    template.getTemplateElements().add(element1);

    final TherapyTemplateElementDto element2 = new TherapyTemplateElementDto();
    final ConstantSimpleTherapyDto therapy2 = new ConstantSimpleTherapyDto();
    therapy2.setTherapyDescription("Aspirin 500mg 2x per day po");
    element2.setTherapy(therapy2);
    template.getTemplateElements().add(element2);

    final TherapyTemplateElementDto element3 = new TherapyTemplateElementDto();
    final ConstantComplexTherapyDto therapy3 = new ConstantComplexTherapyDto();
    therapy3.setTherapyDescription("Dopamin 100ml 10ml/h ivk");
    element3.setTherapy(therapy3);
    template.getTemplateElements().add(element3);

    final TherapyTemplateElementDto element4 = new TherapyTemplateElementDto();
    final ConstantComplexTherapyDto therapy4 = new ConstantComplexTherapyDto();
    therapy4.setTherapyDescription("Glucose 500ml Baselie infusion ivk");
    therapy4.setBaselineInfusion(true);
    element4.setTherapy(therapy4);
    template.getTemplateElements().add(element4);

    medicationsBo.sortTherapyTemplates(templates);

    final List<TherapyTemplateElementDto> sortedElements =
        templates.getOrganizationTemplates().get(0).getTemplateElements();
    assertEquals(sortedElements.get(0), element4);
    assertEquals(sortedElements.get(1), element2);
    assertEquals(sortedElements.get(2), element3);
    assertEquals(sortedElements.get(3), element1);
  }
}
