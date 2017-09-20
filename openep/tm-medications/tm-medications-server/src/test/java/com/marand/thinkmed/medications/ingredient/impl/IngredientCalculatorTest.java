package com.marand.thinkmed.medications.ingredient.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("ALL")
@RunWith(MockitoJUnitRunner.class)
public class IngredientCalculatorTest
{

  private IngredientCalculatorImpl ingredientCalculator = new IngredientCalculatorImpl();

  @Test
  public void testCalculateQuantitySimpleTherapyBetweenDosesJustParacetamol() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    //Mockito
    //    .when(medicationsBo.getMainMedicationId(Matchers.any(TherapyDto.class)))
    //    .thenReturn(1L);

    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setMedication(medicationDto);

    final SimpleDoseElementDto doseElementDto = new SimpleDoseElementDto();
    doseElementDto.setQuantity(10.0);
    constantSimpleTherapy.setDoseElement(doseElementDto);

    final DosingFrequencyDto dosingFrequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 12);
    constantSimpleTherapy.setDosingFrequency(dosingFrequency);

    constantSimpleTherapy.setDoseElement(doseElementDto);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(250.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");

    final MedicationIngredientDto paracetamolIngredient2 = new MedicationIngredientDto();
    paracetamolIngredient2.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient2.setStrengthNumerator(250.0);
    paracetamolIngredient2.setStrengthNumeratorUnit("mg");

    final List<MedicationIngredientDto> medicationIngredientDtos = new ArrayList<>();
    medicationIngredientDtos.add(paracetamolIngredient);
    medicationIngredientDtos.add(paracetamolIngredient2);

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setMedicationIngredients(medicationIngredientDtos);
    medicationDataDto.setBasicUnit("g");

    final Map<Long, MedicationDataDto> medicationDataDtoMap = new HashMap<>();
    medicationDataDtoMap.put(medicationId, medicationDataDto);

    final Double quantity = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(constantSimpleTherapy),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(quantity);
    assertEquals(20000, quantity.intValue()); // in mg
  }

  @Test
  public void testCalculateQuantitySimpleTherapyBetweenDosesDifferentIngredientsDifferentUnits() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setMedication(medicationDto);

    final SimpleDoseElementDto doseElementDto = new SimpleDoseElementDto();
    doseElementDto.setQuantity(10.0);
    constantSimpleTherapy.setDoseElement(doseElementDto);

    final DosingFrequencyDto dosingFrequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, 12);
    constantSimpleTherapy.setDosingFrequency(dosingFrequency);

    constantSimpleTherapy.setDoseElement(doseElementDto);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(250.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");

    final MedicationIngredientDto paracetamolIngredient2 = new MedicationIngredientDto();
    paracetamolIngredient2.setStrengthNumerator(0.25);
    paracetamolIngredient2.setStrengthNumeratorUnit("g");

    final List<MedicationIngredientDto> medicationIngredientDtos = new ArrayList<>();
    medicationIngredientDtos.add(paracetamolIngredient);
    medicationIngredientDtos.add(paracetamolIngredient2);

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setMedicationIngredients(medicationIngredientDtos);
    medicationDataDto.setBasicUnit("g");

    final Map<Long, MedicationDataDto> medicationDataDtoMap = new HashMap<>();
    medicationDataDtoMap.put(medicationId, medicationDataDto);

    final Double quantity = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(constantSimpleTherapy),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(quantity);
    assertEquals(10000, quantity.intValue()); // in mg
  }

  @Test
  public void testCalculateQuantitySimpleVariableTherapyOneIngredient() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final VariableSimpleTherapyDto variableSimpleTherapyDto = new VariableSimpleTherapyDto();
    variableSimpleTherapyDto.setMedication(medicationDto);

    final List<TimedSimpleDoseElementDto> doseElementDtos = new ArrayList<>();

    final TimedSimpleDoseElementDto timedSimpleDoseElementDto1 = new TimedSimpleDoseElementDto();

    final SimpleDoseElementDto simpleDoseElementDto1 = new SimpleDoseElementDto();
    simpleDoseElementDto1.setQuantity(150.0);
    simpleDoseElementDto1.setQuantityDenominator(2.5);

    timedSimpleDoseElementDto1.setDoseElement(simpleDoseElementDto1);
    final HourMinuteDto hourMinuteDto1 = new HourMinuteDto(0, 0);
    timedSimpleDoseElementDto1.setDoseTime(hourMinuteDto1);
    doseElementDtos.add(timedSimpleDoseElementDto1);

    final TimedSimpleDoseElementDto timedSimpleDoseElementDto2 = new TimedSimpleDoseElementDto();

    final SimpleDoseElementDto simpleDoseElementDto2 = new SimpleDoseElementDto();
    simpleDoseElementDto2.setQuantity(200.0);
    simpleDoseElementDto2.setQuantityDenominator(3.3333333333333335);

    timedSimpleDoseElementDto2.setDoseElement(simpleDoseElementDto2);
    final HourMinuteDto hourMinuteDto2 = new HourMinuteDto(8, 0);
    timedSimpleDoseElementDto2.setDoseTime(hourMinuteDto2);
    doseElementDtos.add(timedSimpleDoseElementDto2);

    final TimedSimpleDoseElementDto timedSimpleDoseElementDto3 = new TimedSimpleDoseElementDto();

    final SimpleDoseElementDto simpleDoseElementDto3 = new SimpleDoseElementDto();
    simpleDoseElementDto3.setQuantity(100.0);
    simpleDoseElementDto3.setQuantityDenominator(1.6666666666666667);

    timedSimpleDoseElementDto3.setDoseElement(simpleDoseElementDto3);
    final HourMinuteDto hourMinuteDto3 = new HourMinuteDto(16, 0);
    timedSimpleDoseElementDto3.setDoseTime(hourMinuteDto3);
    doseElementDtos.add(timedSimpleDoseElementDto3);

    variableSimpleTherapyDto.setTimedDoseElements(doseElementDtos);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(250.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");

    final MedicationIngredientDto otherIngredient = new MedicationIngredientDto();
    otherIngredient.setStrengthNumerator(250.0);
    otherIngredient.setStrengthNumeratorUnit("mg");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("mg");
    final List<MedicationIngredientDto> ingredients = new ArrayList<>();
    ingredients.add(paracetamolIngredient);
    ingredients.add(otherIngredient);
    medicationDataDto.setMedicationIngredients(ingredients);

    final Map<Long, MedicationDataDto> medicationDataDtoMap = new HashMap<>();
    medicationDataDtoMap.put(medicationId, medicationDataDto);

    final Double quantity = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(variableSimpleTherapyDto),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(quantity);
    assertEquals(225, quantity.intValue()); // in mg
  }

  @Test
  public void testCalculateQuantitySimpleVariableTherapyOneIngredientInGrams() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final VariableSimpleTherapyDto variableSimpleTherapyDto = new VariableSimpleTherapyDto();
    variableSimpleTherapyDto.setMedication(medicationDto);

    final List<TimedSimpleDoseElementDto> doseElementDtos = new ArrayList<>();

    final TimedSimpleDoseElementDto timedSimpleDoseElementDto1 = new TimedSimpleDoseElementDto();

    final SimpleDoseElementDto simpleDoseElementDto1 = new SimpleDoseElementDto();
    simpleDoseElementDto1.setQuantity(150.0);
    simpleDoseElementDto1.setQuantityDenominator(2.5);

    timedSimpleDoseElementDto1.setDoseElement(simpleDoseElementDto1);
    final HourMinuteDto hourMinuteDto1 = new HourMinuteDto(0, 0);
    timedSimpleDoseElementDto1.setDoseTime(hourMinuteDto1);
    doseElementDtos.add(timedSimpleDoseElementDto1);

    final TimedSimpleDoseElementDto timedSimpleDoseElementDto2 = new TimedSimpleDoseElementDto();

    final SimpleDoseElementDto simpleDoseElementDto2 = new SimpleDoseElementDto();
    simpleDoseElementDto2.setQuantity(200.0);
    simpleDoseElementDto2.setQuantityDenominator(3.3333333333333335);

    timedSimpleDoseElementDto2.setDoseElement(simpleDoseElementDto2);
    final HourMinuteDto hourMinuteDto2 = new HourMinuteDto(8, 0);
    timedSimpleDoseElementDto2.setDoseTime(hourMinuteDto2);
    doseElementDtos.add(timedSimpleDoseElementDto2);

    final TimedSimpleDoseElementDto timedSimpleDoseElementDto3 = new TimedSimpleDoseElementDto();

    final SimpleDoseElementDto simpleDoseElementDto3 = new SimpleDoseElementDto();
    simpleDoseElementDto3.setQuantity(100.0);
    simpleDoseElementDto3.setQuantityDenominator(1.6666666666666667);

    timedSimpleDoseElementDto3.setDoseElement(simpleDoseElementDto3);
    final HourMinuteDto hourMinuteDto3 = new HourMinuteDto(16, 0);
    timedSimpleDoseElementDto3.setDoseTime(hourMinuteDto3);
    doseElementDtos.add(timedSimpleDoseElementDto3);

    variableSimpleTherapyDto.setTimedDoseElements(doseElementDtos);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(250.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");

    final MedicationIngredientDto otherIngredient = new MedicationIngredientDto();
    otherIngredient.setStrengthNumerator(250.0);
    otherIngredient.setStrengthNumeratorUnit("mg");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("mg");
    final List<MedicationIngredientDto> ingredients = new ArrayList<>();
    ingredients.add(paracetamolIngredient);
    ingredients.add(otherIngredient);
    medicationDataDto.setMedicationIngredients(ingredients);

    final Map<Long, MedicationDataDto> medicationDataDtoMap = new HashMap<>();
    medicationDataDtoMap.put(medicationId, medicationDataDto);

    final Double quantity = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(variableSimpleTherapyDto),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "g");

    assertNotNull(quantity);
    assertTrue(quantity.equals(0.225)); // in g
  }

  @Test
  public void testCalculateQuantityConstantSimpleTherapyOneIngredient() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setMedication(medicationDto);

    final SimpleDoseElementDto doseElementDto = new SimpleDoseElementDto();
    doseElementDto.setQuantity(2.0);
    constantSimpleTherapy.setDoseElement(doseElementDto);

    final DosingFrequencyDto dosingFrequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 4);
    constantSimpleTherapy.setDosingFrequency(dosingFrequency);

    constantSimpleTherapy.setDoseElement(doseElementDto);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(2.0);
    paracetamolIngredient.setStrengthNumeratorUnit("g");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("g");
    medicationDataDto.setMedicationIngredients(Collections.singletonList(paracetamolIngredient));

    final Map<Long, MedicationDataDto> medicationDataDtoMap = new HashMap<>();
    medicationDataDtoMap.put(medicationId, medicationDataDto);

    final Double quantity = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(constantSimpleTherapy),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(quantity);
    assertEquals(8000, quantity.intValue()); // in mg
  }


  @Test
  public void testCalculateQuantityConstantComplexTherapyContinuousInfusionOneInfusionIngredientNotLiquidUnit() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(2.0);
    paracetamolIngredient.setStrengthNumeratorUnit("g");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("g");
    medicationDataDto.setMedicationIngredients(Collections.singletonList(paracetamolIngredient));


    final List<InfusionIngredientDto> infusionIngredientDtoList = new ArrayList<>();
    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setQuantityUnit("mg");
    infusionIngredientDto.setQuantityDenominatorUnit("ml");
    infusionIngredientDto.setMedication(medicationDto);
    infusionIngredientDtoList.add(infusionIngredientDto);

    final ConstantComplexTherapyDto constantComplexTherapyDto = new ConstantComplexTherapyDto();
    constantComplexTherapyDto.setIngredientsList(infusionIngredientDtoList);
    constantComplexTherapyDto.setContinuousInfusion(true);

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(120.0);
    complexDoseElementDto.setRateUnit("ml/h");

    constantComplexTherapyDto.setDoseElement(complexDoseElementDto);

    final Map<Long, MedicationDataDto> medicationDataDtoMap = new HashMap<>();
    medicationDataDtoMap.put(medicationId, medicationDataDto);

    final Double ingredientQuantityInTherapies = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(constantComplexTherapyDto),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(ingredientQuantityInTherapies);
    assertEquals(0,ingredientQuantityInTherapies.intValue());
  }

  @Test
  public void testCalculateQuantityConstantComplexTherapyContinuousInfusionOneInfusionIngredientLiquidUnit() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(2.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");
    paracetamolIngredient.setStrengthDenominator(1.0);
    paracetamolIngredient.setStrengthDenominatorUnit("ml");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("g");
    medicationDataDto.setMedicationIngredients(Collections.singletonList(paracetamolIngredient));


    final List<InfusionIngredientDto> infusionIngredientDtoList = new ArrayList<>();
    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setQuantityUnit("mg");
    infusionIngredientDto.setQuantityDenominatorUnit("ml");
    infusionIngredientDto.setMedication(medicationDto);
    infusionIngredientDtoList.add(infusionIngredientDto);

    final ConstantComplexTherapyDto constantComplexTherapyDto = new ConstantComplexTherapyDto();
    constantComplexTherapyDto.setIngredientsList(infusionIngredientDtoList);
    constantComplexTherapyDto.setContinuousInfusion(true);

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(120.0);
    complexDoseElementDto.setRateUnit("ml/h");

    constantComplexTherapyDto.setDoseElement(complexDoseElementDto);

    final Map<Long, MedicationDataDto> medicationDataDtoMap = new HashMap<>();
    medicationDataDtoMap.put(medicationId, medicationDataDto);

    final Double ingredientQuantityInTherapies = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(constantComplexTherapyDto),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(ingredientQuantityInTherapies);
    assertEquals(5760 ,ingredientQuantityInTherapies.intValue()); // 2 * 120 * 24
  }

  @Test
  public void testCalculateQuantityConstantComplexTherapyContinuousInfusionMoreInfusionIngredients() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

   final long medicationId2 = 2L;
    final MedicationDto medicationDto2 = buildMedicationDto(medicationId2);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(2.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");
    paracetamolIngredient.setStrengthDenominator(1.0);
    paracetamolIngredient.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto paracetamolIngredient2 = new MedicationIngredientDto();
    paracetamolIngredient2.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient2.setStrengthNumerator(4.0);
    paracetamolIngredient2.setStrengthNumeratorUnit("mg");
    paracetamolIngredient2.setStrengthDenominator(1.0);
    paracetamolIngredient2.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto otherIngredient = new MedicationIngredientDto();
    otherIngredient.setStrengthNumerator(2.0);
    otherIngredient.setStrengthNumeratorUnit("mg");
    otherIngredient.setStrengthDenominator(1.0);
    otherIngredient.setStrengthDenominatorUnit("ml");

    final ArrayList<MedicationIngredientDto> ingredientDtos = new ArrayList<>();
    ingredientDtos.add(paracetamolIngredient);
    ingredientDtos.add(otherIngredient);

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("g");
    medicationDataDto.setMedicationIngredients(ingredientDtos);

    final ArrayList<MedicationIngredientDto> ingredientDtos2 = new ArrayList<>();
    ingredientDtos2.add(paracetamolIngredient2);
    ingredientDtos2.add(otherIngredient);

    final MedicationDataDto medicationDataDto2 = new MedicationDataDto();
    medicationDataDto2.setMedication(medicationDto2);
    medicationDataDto2.setBasicUnit("g");
    medicationDataDto2.setMedicationIngredients(ingredientDtos2);

    final List<InfusionIngredientDto> infusionIngredientDtoList = new ArrayList<>();

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setQuantity(50.0);
    infusionIngredientDto.setQuantityUnit("mg");
    infusionIngredientDto.setQuantityDenominatorUnit("ml");
    infusionIngredientDto.setMedication(medicationDto);
    infusionIngredientDtoList.add(infusionIngredientDto);

    final InfusionIngredientDto infusionIngredientDto2 = new InfusionIngredientDto();
    infusionIngredientDto2.setQuantity(50.0);
    infusionIngredientDto2.setQuantityUnit("mg");
    infusionIngredientDto2.setQuantityDenominatorUnit("ml");
    infusionIngredientDto2.setMedication(medicationDto2);
    infusionIngredientDtoList.add(infusionIngredientDto2);

    final ConstantComplexTherapyDto constantComplexTherapyDto = new ConstantComplexTherapyDto();
    constantComplexTherapyDto.setIngredientsList(infusionIngredientDtoList);
    constantComplexTherapyDto.setContinuousInfusion(true);
    constantComplexTherapyDto.setVolumeSum(100.0);
    constantComplexTherapyDto.setVolumeSumUnit("ml");

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(100.0);
    complexDoseElementDto.setRateUnit("ml/h");

    constantComplexTherapyDto.setDoseElement(complexDoseElementDto);

    final Map<Long, MedicationDataDto> medicationDataDtoMap = new HashMap<>();
    medicationDataDtoMap.put(medicationId, medicationDataDto);
    medicationDataDtoMap.put(medicationId2, medicationDataDto2);

    final Double ingredientQuantityInTherapies = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(constantComplexTherapyDto),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * volumeSum = 100 ml
     * Rate = 100 ml/h
     * infusionIngredient1 = 1/2
     * infusionIngredient1 quantity = 50 mg
     * infusionIngrediet2 = 2/3
     * infusionIngrediet2  quantity = 50 mg
     *
     * result = (1/2 * 50 + 2/3 * 50) / 100 * 100 * 24 = 1400
     */

    assertNotNull(ingredientQuantityInTherapies);
    assertEquals(1400 ,ingredientQuantityInTherapies.intValue());
  }

  @Test
  public void testCalculateQuantityConstantComplexTherapyNormalInfusionMoreInfusionIngredients() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

   final long medicationId2 = 2L;
    final MedicationDto medicationDto2 = buildMedicationDto(medicationId2);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(2.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");
    paracetamolIngredient.setStrengthDenominator(1.0);
    paracetamolIngredient.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto paracetamolIngredient2 = new MedicationIngredientDto();
    paracetamolIngredient2.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient2.setStrengthNumerator(4.0);
    paracetamolIngredient2.setStrengthNumeratorUnit("mg");
    paracetamolIngredient2.setStrengthDenominator(1.0);
    paracetamolIngredient2.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto otherIngredient = new MedicationIngredientDto();
    otherIngredient.setStrengthNumerator(2.0);
    otherIngredient.setStrengthNumeratorUnit("mg");
    otherIngredient.setStrengthDenominator(1.0);
    otherIngredient.setStrengthDenominatorUnit("ml");

    final ArrayList<MedicationIngredientDto> ingredientDtos = new ArrayList<>();
    ingredientDtos.add(paracetamolIngredient);
    ingredientDtos.add(otherIngredient);

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("g");
    medicationDataDto.setMedicationIngredients(ingredientDtos);

    final ArrayList<MedicationIngredientDto> ingredientDtos2 = new ArrayList<>();
    ingredientDtos2.add(paracetamolIngredient2);
    ingredientDtos2.add(otherIngredient);

    final MedicationDataDto medicationDataDto2 = new MedicationDataDto();
    medicationDataDto2.setMedication(medicationDto2);
    medicationDataDto2.setBasicUnit("g");
    medicationDataDto2.setMedicationIngredients(ingredientDtos2);

    final List<InfusionIngredientDto> infusionIngredientDtoList = new ArrayList<>();

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setQuantity(50.0);
    infusionIngredientDto.setQuantityUnit("mg");
    infusionIngredientDto.setQuantityDenominatorUnit("ml");
    infusionIngredientDto.setMedication(medicationDto);
    infusionIngredientDtoList.add(infusionIngredientDto);

    final InfusionIngredientDto infusionIngredientDto2 = new InfusionIngredientDto();
    infusionIngredientDto2.setQuantity(50.0);
    infusionIngredientDto2.setQuantityUnit("mg");
    infusionIngredientDto2.setQuantityDenominatorUnit("ml");
    infusionIngredientDto2.setMedication(medicationDto2);
    infusionIngredientDtoList.add(infusionIngredientDto2);

    final ConstantComplexTherapyDto constantComplexTherapyDto = new ConstantComplexTherapyDto();
    constantComplexTherapyDto.setIngredientsList(infusionIngredientDtoList);

    final DosingFrequencyDto dosingFrequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 6);
    constantComplexTherapyDto.setDosingFrequency(dosingFrequency);

    final Map<Long, MedicationDataDto> medicationDataDtoMap = new HashMap<>();
    medicationDataDtoMap.put(medicationId, medicationDataDto);
    medicationDataDtoMap.put(medicationId2, medicationDataDto2);

    final Double ingredientQuantityInTherapies = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(constantComplexTherapyDto),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * infusionIngredient1 = 1/2
     * infusionIngredient1 quantity = 50 mg
     * infusionIngrediet2 = 2/3
     * infusionIngrediet2  quantity = 50 mg
     * times per day = 4
     *
     * result = (1/2 * 50 + 2/3 * 50) * 6 = 350
     */

    assertNotNull(ingredientQuantityInTherapies);
    assertEquals(350 ,ingredientQuantityInTherapies.intValue());
  }

  @Test
  public void testCalculateQuantityForQuantityAdministrationOneIngredient() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantSimpleTherapyDto constantSimpleTherapyDto = new ConstantSimpleTherapyDto();
    constantSimpleTherapyDto.setMedication(medicationDto);

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();

    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, constantSimpleTherapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();
    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    administrationTherapyDoseDto.setNumerator(1.0);
    administrationTherapyDoseDto.setNumeratorUnit("g");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 19, 16, 0, 0)); // 20 hours ago
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(1000, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForCurrentQuantityAdministrationOneIngredient() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantSimpleTherapyDto constantSimpleTherapyDto = new ConstantSimpleTherapyDto();
    constantSimpleTherapyDto.setMedication(medicationDto);

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    administrationTherapyDoseDto.setNumerator(1.0);
    administrationTherapyDoseDto.setNumeratorUnit("g");

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        administrationTherapyDoseDto,
        constantSimpleTherapyDto,
        ArrayListMultimap.create(),
        Collections.emptyMap(),
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(1000, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForCurrentQuantityAdministrationWithIngredientIdAndIngredientRule() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantSimpleTherapyDto constantSimpleTherapyDto = new ConstantSimpleTherapyDto();
    constantSimpleTherapyDto.setMedication(medicationDto);

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    administrationTherapyDoseDto.setNumerator(1.0);
    administrationTherapyDoseDto.setNumeratorUnit("g");

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        administrationTherapyDoseDto,
        constantSimpleTherapyDto,
        ArrayListMultimap.create(),
        Collections.emptyMap(),
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        1L,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(0, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForQuantityAdministrationMultipleIngredients() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantSimpleTherapyDto constantSimpleTherapyDto = new ConstantSimpleTherapyDto();
    constantSimpleTherapyDto.setMedication(medicationDto);

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();

    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, constantSimpleTherapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();
    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    administrationTherapyDoseDto.setNumerator(1.0);
    administrationTherapyDoseDto.setNumeratorUnit("g");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 19, 16, 0, 0)); // 20 hours ago
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(500, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForRateQuantityAdministrationWithOverlapInterval() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();

    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    ingredient.setMedication(medicationDto);
    therapyDto.getIngredientsList().add(ingredient);

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();

    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, therapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();
    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final DateTime startAdministrationTime = new DateTime(2016, 1, 20, 2, 0, 0);
    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(startAdministrationTime);
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    final StopAdministrationDto stopAdministration = new StopAdministrationDto();
    stopAdministration.setAdministrationId("Administration1");
    stopAdministration.setAdministrationResult(AdministrationResultEnum.GIVEN);
    stopAdministration.setAdministrationTime(startAdministrationTime.plusHours(100));
    stopAdministration.setTherapyId(therapyId);
    administrationDtoList.add(stopAdministration);

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setStrengthDenominator(1.0);
    medicationIngredientDto1.setStrengthDenominatorUnit("ml");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");
    medicationIngredientDto2.setStrengthDenominator(1.0);
    medicationIngredientDto2.setStrengthDenominatorUnit("ml");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * administrationInterval = 10h
     * ingredientQuantityIn1Ml = 10mg
     * rate = 10 ml/h
     * 10 * 10 * 10 = 1000
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(1000, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForRateQuantityAdministrationNoOverlapInterval() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();

    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    ingredient.setMedication(medicationDto);
    therapyDto.getIngredientsList().add(ingredient);

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();

    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, therapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();
    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setSecondaryNumerator(100.0);
    administrationTherapyDoseDto.setSecondaryNumeratorUnit("mg");
    administrationTherapyDoseDto.setSecondaryDenominator(1.0);
    administrationTherapyDoseDto.setSecondaryDenominatorUnit("l");
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 5, 2, 0, 0));
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * intervalsOverlapPercentage = 0.0
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(0, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForRateAdministrationBeforeSearchInterval() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();
    therapyDto.setContinuousInfusion(true);

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setMedication(medicationDto);

    therapyDto.setIngredientsList(Collections.singletonList(infusionIngredientDto));

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();
    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, therapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();
    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 5, 2, 0, 0));
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setStrengthDenominator(1.0);
    medicationIngredientDto1.setStrengthDenominatorUnit("ml");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");
    medicationIngredientDto2.setStrengthDenominator(1.0);
    medicationIngredientDto2.setStrengthDenominatorUnit("ml");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * ingredientQuantityPerHour = 10 ml/h
     * rate = 10 ml/h
     * duration = 24 h = 1440 min
     * result = ingredientQuantityPerHour * rate * durationInMinutes  / 60 = 2400
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(2400, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForRateAdministrationMidlleOfSearchInterval() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();
    therapyDto.setContinuousInfusion(true);

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setMedication(medicationDto);

    therapyDto.setIngredientsList(Collections.singletonList(infusionIngredientDto));

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();
    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, therapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();
    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 19, 23, 0, 0));
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setStrengthDenominator(1.0);
    medicationIngredientDto1.setStrengthDenominatorUnit("ml");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");
    medicationIngredientDto2.setStrengthDenominator(1.0);
    medicationIngredientDto2.setStrengthDenominatorUnit("ml");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * ingredientQuantityPerHour = 10 ml/h
     * rate = 10 ml/h
     * duration = 780 min
     * result = ingredientQuantityPerHour * rate * durationInMinutes  / 60 = 1300
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(1300, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForRateAdministrationAfterSearchInterval() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setMedication(medicationDto);

    therapyDto.setIngredientsList(Collections.singletonList(infusionIngredientDto));
    therapyDto.setContinuousInfusion(true);

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();
    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, therapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();
    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 22, 23, 0, 0));
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setStrengthDenominator(1.0);
    medicationIngredientDto1.setStrengthDenominatorUnit("ml");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");
    medicationIngredientDto2.setStrengthDenominator(1.0);
    medicationIngredientDto2.setStrengthDenominatorUnit("ml");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * ingredientQuantityPerHour = 10 ml/h
     * rate = 10 ml/h
     * duration = 0 min
     * result = ingredientQuantityPerHour * rate * durationInMinutes  / 60 = 0
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(0, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForRateAdministrationWithLaterStopAdministration() throws Exception
  {
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);
    final String therapyId = "1t";

    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setMedication(medicationDto);
    therapyDto.setContinuousInfusion(true);

    therapyDto.setIngredientsList(Collections.singletonList(infusionIngredientDto));

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();
    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, therapyDto);

    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 19, 23, 0, 0));
    administrationDto.setTherapyId(therapyId);
    administrationDtoMap.put(therapyId, administrationDto);

    final StopAdministrationDto administrationDto2 = new StopAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto2 = new TherapyDoseDto();
    administrationTherapyDoseDto2.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto2.setNumerator(10.0);
    administrationTherapyDoseDto2.setNumeratorUnit("ml/h");
    administrationDto2.setAdministrationId("Administration2");
    administrationDto2.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto2.setAdministrationTime(new DateTime(2016, 1, 20, 1, 0, 0));
    administrationDto2.setTherapyId(therapyId);
    administrationDtoMap.put(therapyId, administrationDto2);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setStrengthDenominator(1.0);
    medicationIngredientDto1.setStrengthDenominatorUnit("ml");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");
    medicationIngredientDto2.setStrengthDenominator(1.0);
    medicationIngredientDto2.setStrengthDenominatorUnit("ml");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * ingredientQuantityPerHour = 10 ml/h
     * rate = 10 ml/h
     * duration = 120 min
     * result = ingredientQuantityPerHour * rate * durationInMinutes  / 60 = 200
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(200, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForRateVolumeSumWithLaterStopAdministration() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final long medicationId2 = 2L;
    final MedicationDto medicationDto2 = buildMedicationDto(medicationId2);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(2.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");
    paracetamolIngredient.setStrengthDenominator(1.0);
    paracetamolIngredient.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto paracetamolIngredient2 = new MedicationIngredientDto();
    paracetamolIngredient2.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient2.setStrengthNumerator(10.0);
    paracetamolIngredient2.setStrengthNumeratorUnit("mg");
    paracetamolIngredient2.setStrengthDenominator(1.0);
    paracetamolIngredient2.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto otherIngredient = new MedicationIngredientDto();
    otherIngredient.setStrengthNumerator(10.0);
    otherIngredient.setStrengthNumeratorUnit("mg");
    otherIngredient.setStrengthDenominator(1.0);
    otherIngredient.setStrengthDenominatorUnit("ml");

    final ArrayList<MedicationIngredientDto> ingredientDtos = new ArrayList<>();
    ingredientDtos.add(paracetamolIngredient);
    ingredientDtos.add(otherIngredient);

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("g");
    medicationDataDto.setMedicationIngredients(ingredientDtos);

    final ArrayList<MedicationIngredientDto> ingredientDtos2 = new ArrayList<>();
    ingredientDtos2.add(paracetamolIngredient2);
    ingredientDtos2.add(otherIngredient);

    final MedicationDataDto medicationDataDto2 = new MedicationDataDto();
    medicationDataDto2.setMedication(medicationDto2);
    medicationDataDto2.setBasicUnit("g");
    medicationDataDto2.setMedicationIngredients(ingredientDtos2);

    final List<InfusionIngredientDto> infusionIngredientDtoList = new ArrayList<>();

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setQuantity(50.0);
    infusionIngredientDto.setQuantityUnit("mg");
    infusionIngredientDto.setQuantityDenominatorUnit("ml");
    infusionIngredientDto.setMedication(medicationDto);
    infusionIngredientDtoList.add(infusionIngredientDto);

    final InfusionIngredientDto infusionIngredientDto2 = new InfusionIngredientDto();
    infusionIngredientDto2.setQuantity(50.0);
    infusionIngredientDto2.setQuantityUnit("mg");
    infusionIngredientDto2.setQuantityDenominatorUnit("ml");
    infusionIngredientDto2.setMedication(medicationDto2);
    infusionIngredientDtoList.add(infusionIngredientDto2);

    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();
    therapyDto.setIngredientsList(infusionIngredientDtoList);
    therapyDto.setContinuousInfusion(true);
    therapyDto.setVolumeSum(100.0);
    therapyDto.setVolumeSumUnit("ml");

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(100.0);
    complexDoseElementDto.setRateUnit("ml/h");

    therapyDto.setDoseElement(complexDoseElementDto);

    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final String therapyId = "1t";

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();
    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, therapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();
    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 19, 23, 0, 0));
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setStrengthDenominator(1.0);
    medicationIngredientDto1.setStrengthDenominatorUnit("ml");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");
    medicationIngredientDto2.setStrengthDenominator(1.0);
    medicationIngredientDto2.setStrengthDenominatorUnit("ml");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);
    medicationDataMap.put(medicationDataDto2.getMedication().getId(), medicationDataDto2);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * ingredientQuantityInMg = 0.5 * 50 + 0.5 * 50 = 50 mg
     * rate = 10 ml/h
     * volumeSumInMl = 100 ml
     * minutesDuration = 780 = 13 h
     * result = ingredientQuantityInMg / volumeSumInMl * rate * minutesDuration / 60 = 65
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(65, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForRateVolumeSumWithLaterStopAdministration2() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final long medicationId2 = 2L;
    final MedicationDto medicationDto2 = buildMedicationDto(medicationId2);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(2.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");
    paracetamolIngredient.setStrengthDenominator(1.0);
    paracetamolIngredient.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto paracetamolIngredient2 = new MedicationIngredientDto();
    paracetamolIngredient2.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient2.setStrengthNumerator(10.0);
    paracetamolIngredient2.setStrengthNumeratorUnit("mg");
    paracetamolIngredient2.setStrengthDenominator(1.0);
    paracetamolIngredient2.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto otherIngredient = new MedicationIngredientDto();
    otherIngredient.setStrengthNumerator(10.0);
    otherIngredient.setStrengthNumeratorUnit("mg");
    otherIngredient.setStrengthDenominator(1.0);
    otherIngredient.setStrengthDenominatorUnit("ml");

    final ArrayList<MedicationIngredientDto> ingredientDtos = new ArrayList<>();
    ingredientDtos.add(paracetamolIngredient);
    ingredientDtos.add(otherIngredient);

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("g");
    medicationDataDto.setMedicationIngredients(ingredientDtos);

    final ArrayList<MedicationIngredientDto> ingredientDtos2 = new ArrayList<>();
    ingredientDtos2.add(paracetamolIngredient2);
    ingredientDtos2.add(otherIngredient);

    final MedicationDataDto medicationDataDto2 = new MedicationDataDto();
    medicationDataDto2.setMedication(medicationDto2);
    medicationDataDto2.setBasicUnit("g");
    medicationDataDto2.setMedicationIngredients(ingredientDtos2);

    final List<InfusionIngredientDto> infusionIngredientDtoList = new ArrayList<>();

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setQuantity(50.0);
    infusionIngredientDto.setQuantityUnit("mg");
    infusionIngredientDto.setMedication(medicationDto);
    infusionIngredientDtoList.add(infusionIngredientDto);

    final InfusionIngredientDto infusionIngredientDto2 = new InfusionIngredientDto();
    infusionIngredientDto2.setQuantity(50.0);
    infusionIngredientDto2.setQuantityUnit("mg");
    infusionIngredientDto2.setMedication(medicationDto2);
    infusionIngredientDtoList.add(infusionIngredientDto2);

    final VariableComplexTherapyDto therapyDto = new VariableComplexTherapyDto();
    therapyDto.setIngredientsList(infusionIngredientDtoList);
    therapyDto.setContinuousInfusion(false);
    therapyDto.setVolumeSum(100.0);
    therapyDto.setVolumeSumUnit("ml");

    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);
    final String therapyId = "1t";

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();
    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, therapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();

    final StartAdministrationDto administrationDto = new StartAdministrationDto(); // 10ml/h * 3h = 30ml
    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto.setNumerator(10.0);
    administrationTherapyDoseDto.setNumeratorUnit("ml/h");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 20, 2, 0, 0));
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    final AdjustInfusionAdministrationDto administrationDto2 = new AdjustInfusionAdministrationDto(); // 30ml/h * 3h = 90ml
    final TherapyDoseDto administrationTherapyDoseDto2 = new TherapyDoseDto();
    administrationTherapyDoseDto2.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto2.setNumerator(30.0);
    administrationTherapyDoseDto2.setNumeratorUnit("ml/h");
    administrationDto2.setAdministrationId("Administration2");
    administrationDto2.setAdministeredDose(administrationTherapyDoseDto2);
    administrationDto2.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto2.setAdministrationTime(new DateTime(2016, 1, 20, 5, 0, 0));
    administrationDto2.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto2);

    final AdjustInfusionAdministrationDto administrationDto3 = new AdjustInfusionAdministrationDto(); // 40ml/h * 4h = 160ml
    final TherapyDoseDto administrationTherapyDoseDto3 = new TherapyDoseDto();
    administrationTherapyDoseDto3.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto3.setNumerator(40.0);
    administrationTherapyDoseDto3.setNumeratorUnit("ml/h");
    administrationDto3.setAdministrationId("Administration3");
    administrationDto3.setAdministeredDose(administrationTherapyDoseDto3);
    administrationDto3.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto3.setAdministrationTime(new DateTime(2016, 1, 20, 8, 0, 0));
    administrationDto3.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto3);

    // 200ml / 1000ml = 0.28

    final StartAdministrationDto administrationDto4 = new StartAdministrationDto(); // 10ml/h * 2h = 20ml
    final TherapyDoseDto administrationTherapyDoseDto4 = new TherapyDoseDto();
    administrationTherapyDoseDto4.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    administrationTherapyDoseDto4.setNumerator(10.0);
    administrationTherapyDoseDto4.setNumeratorUnit("ml/h");
    administrationDto4.setAdministrationId("Administration4");
    administrationDto4.setAdministeredDose(administrationTherapyDoseDto4);
    administrationDto4.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto4.setAdministrationTime(new DateTime(2016, 1, 20, 10, 0, 0));
    administrationDto4.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto4);

    // 20ml / 1000ml  = 0.02

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setStrengthDenominator(1.0);
    medicationIngredientDto1.setStrengthDenominatorUnit("ml");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");
    medicationIngredientDto2.setStrengthDenominator(1.0);
    medicationIngredientDto2.setStrengthDenominatorUnit("ml");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);
    medicationDataMap.put(medicationDataDto2.getMedication().getId(), medicationDataDto2);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * 10ml/h * 3h = 30ml
     * 30ml/h * 3h = 90ml
     * 40ml/h * 4h = 160ml
     * TOTAL: 280ml
     *
     * 10ml/h * 2h = 20ml
     * TOTAL: 20ml
     *
     *
     * Paracetamol INGREDIENT ratio = 0.5
     *
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(150, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForVolumeSum() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final long medicationId2 = 2L;
    final MedicationDto medicationDto2 = buildMedicationDto(medicationId2);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(2.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");
    paracetamolIngredient.setStrengthDenominator(1.0);
    paracetamolIngredient.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto paracetamolIngredient2 = new MedicationIngredientDto();
    paracetamolIngredient2.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient2.setStrengthNumerator(10.0);
    paracetamolIngredient2.setStrengthNumeratorUnit("mg");
    paracetamolIngredient2.setStrengthDenominator(1.0);
    paracetamolIngredient2.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto otherIngredient = new MedicationIngredientDto();
    otherIngredient.setStrengthNumerator(10.0);
    otherIngredient.setStrengthNumeratorUnit("mg");
    otherIngredient.setStrengthDenominator(1.0);
    otherIngredient.setStrengthDenominatorUnit("ml");

    final ArrayList<MedicationIngredientDto> ingredientDtos = new ArrayList<>();
    ingredientDtos.add(paracetamolIngredient);
    ingredientDtos.add(otherIngredient);

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("g");
    medicationDataDto.setMedicationIngredients(ingredientDtos);

    final ArrayList<MedicationIngredientDto> ingredientDtos2 = new ArrayList<>();
    ingredientDtos2.add(paracetamolIngredient2);
    ingredientDtos2.add(otherIngredient);

    final MedicationDataDto medicationDataDto2 = new MedicationDataDto();
    medicationDataDto2.setMedication(medicationDto2);
    medicationDataDto2.setBasicUnit("g");
    medicationDataDto2.setMedicationIngredients(ingredientDtos2);

    final List<InfusionIngredientDto> infusionIngredientDtoList = new ArrayList<>();

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setQuantity(50.0);
    infusionIngredientDto.setQuantityUnit("mg");
    infusionIngredientDto.setQuantityDenominatorUnit("ml");
    infusionIngredientDto.setMedication(medicationDto);
    infusionIngredientDtoList.add(infusionIngredientDto);

    final InfusionIngredientDto infusionIngredientDto2 = new InfusionIngredientDto();
    infusionIngredientDto2.setQuantity(50.0);
    infusionIngredientDto2.setQuantityUnit("mg");
    infusionIngredientDto2.setQuantityDenominatorUnit("ml");
    infusionIngredientDto2.setMedication(medicationDto2);
    infusionIngredientDtoList.add(infusionIngredientDto2);

    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();
    therapyDto.setIngredientsList(infusionIngredientDtoList);
    therapyDto.setContinuousInfusion(true);

    therapyDto.setVolumeSum(100.0);
    therapyDto.setVolumeSumUnit("ml");

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(100.0);
    complexDoseElementDto.setRateUnit("ml/h");

    therapyDto.setDoseElement(complexDoseElementDto);

    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final String therapyId = "1t";

    final Multimap<String, AdministrationDto> administrationDtoMap = ArrayListMultimap.create();
    final Map<String, TherapyDto> therapyMap = new HashMap<>();
    therapyMap.put(therapyId, therapyDto);

    final List<AdministrationDto> administrationDtoList = new ArrayList<>();
    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.VOLUME_SUM);
    administrationTherapyDoseDto.setNumerator(1.0);
    administrationTherapyDoseDto.setNumeratorUnit("l");
    administrationDto.setAdministrationId("Administration1");
    administrationDto.setAdministeredDose(administrationTherapyDoseDto);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administrationDto.setAdministrationTime(new DateTime(2016, 1, 19, 23, 0, 0));
    administrationDto.setTherapyId(therapyId);
    administrationDtoList.add(administrationDto);

    administrationDtoMap.putAll(therapyId, administrationDtoList);

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setStrengthDenominator(1.0);
    medicationIngredientDto1.setStrengthDenominatorUnit("ml");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");
    medicationIngredientDto2.setStrengthDenominator(1.0);
    medicationIngredientDto2.setStrengthDenominatorUnit("ml");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);
    medicationDataMap.put(medicationDataDto2.getMedication().getId(), medicationDataDto2);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        null,
        null,
        administrationDtoMap,
        therapyMap,
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * ingredientQuantityInMg = 0.5 * 50 + 0.5 * 50 = 50 mg
     * therapyVolumeSum = 100 ml
     * administrationVolumeSum = 1 l = 1000 ml
     * result = administrationVolumeSum / therapyVolumeSum * ingredientQuantityInMg = 1000 / 100 * 50 = 500
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(500, ingredientQuantityInAdministrations.intValue());
  }

  @Test
  public void testCalculateQuantityForCurrentVolumeSum() throws Exception
  {
    final long medicationId = 1L;
    final MedicationDto medicationDto = buildMedicationDto(medicationId);

    final long medicationId2 = 2L;
    final MedicationDto medicationDto2 = buildMedicationDto(medicationId2);

    final MedicationIngredientDto paracetamolIngredient = new MedicationIngredientDto();
    paracetamolIngredient.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient.setStrengthNumerator(2.0);
    paracetamolIngredient.setStrengthNumeratorUnit("mg");
    paracetamolIngredient.setStrengthDenominator(1.0);
    paracetamolIngredient.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto paracetamolIngredient2 = new MedicationIngredientDto();
    paracetamolIngredient2.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    paracetamolIngredient2.setStrengthNumerator(10.0);
    paracetamolIngredient2.setStrengthNumeratorUnit("mg");
    paracetamolIngredient2.setStrengthDenominator(1.0);
    paracetamolIngredient2.setStrengthDenominatorUnit("ml");

    final MedicationIngredientDto otherIngredient = new MedicationIngredientDto();
    otherIngredient.setStrengthNumerator(10.0);
    otherIngredient.setStrengthNumeratorUnit("mg");
    otherIngredient.setStrengthDenominator(1.0);
    otherIngredient.setStrengthDenominatorUnit("ml");

    final ArrayList<MedicationIngredientDto> ingredientDtos = new ArrayList<>();
    ingredientDtos.add(paracetamolIngredient);
    ingredientDtos.add(otherIngredient);

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setMedication(medicationDto);
    medicationDataDto.setBasicUnit("g");
    medicationDataDto.setMedicationIngredients(ingredientDtos);

    final ArrayList<MedicationIngredientDto> ingredientDtos2 = new ArrayList<>();
    ingredientDtos2.add(paracetamolIngredient2);
    ingredientDtos2.add(otherIngredient);

    final MedicationDataDto medicationDataDto2 = new MedicationDataDto();
    medicationDataDto2.setMedication(medicationDto2);
    medicationDataDto2.setBasicUnit("g");
    medicationDataDto2.setMedicationIngredients(ingredientDtos2);

    final List<InfusionIngredientDto> infusionIngredientDtoList = new ArrayList<>();

    final InfusionIngredientDto infusionIngredientDto = new InfusionIngredientDto();
    infusionIngredientDto.setQuantity(50.0);
    infusionIngredientDto.setQuantityUnit("mg");
    infusionIngredientDto.setQuantityDenominatorUnit("ml");
    infusionIngredientDto.setMedication(medicationDto);
    infusionIngredientDtoList.add(infusionIngredientDto);

    final InfusionIngredientDto infusionIngredientDto2 = new InfusionIngredientDto();
    infusionIngredientDto2.setQuantity(50.0);
    infusionIngredientDto2.setQuantityUnit("mg");
    infusionIngredientDto2.setQuantityDenominatorUnit("ml");
    infusionIngredientDto2.setMedication(medicationDto2);
    infusionIngredientDtoList.add(infusionIngredientDto2);

    final ConstantComplexTherapyDto therapyDto = new ConstantComplexTherapyDto();
    therapyDto.setIngredientsList(infusionIngredientDtoList);
    therapyDto.setContinuousInfusion(true);

    therapyDto.setVolumeSum(100.0);
    therapyDto.setVolumeSumUnit("ml");

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(100.0);
    complexDoseElementDto.setRateUnit("ml/h");

    therapyDto.setDoseElement(complexDoseElementDto);

    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);

    final String therapyId = "1t";

    final TherapyDoseDto administrationTherapyDoseDto = new TherapyDoseDto();
    administrationTherapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.VOLUME_SUM);
    administrationTherapyDoseDto.setNumerator(1.0);
    administrationTherapyDoseDto.setNumeratorUnit("l");

    final Map<Long, MedicationDataDto> medicationDataMap = new HashMap<>();

    final MedicationDataDto medicationDataDto1 = new MedicationDataDto();
    medicationDataDto1.setMedication(medicationDto);

    final List<MedicationIngredientDto> medicationDataDto1Ingredients = new ArrayList<>();

    final MedicationIngredientDto medicationIngredientDto1 = new MedicationIngredientDto();
    medicationIngredientDto1.setStrengthNumerator(10.0);
    medicationIngredientDto1.setStrengthNumeratorUnit("mg");
    medicationIngredientDto1.setStrengthDenominator(1.0);
    medicationIngredientDto1.setStrengthDenominatorUnit("ml");
    medicationIngredientDto1.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto medicationIngredientDto2 = new MedicationIngredientDto();
    medicationIngredientDto2.setStrengthNumerator(10.0);
    medicationIngredientDto2.setStrengthNumeratorUnit("mg");
    medicationIngredientDto2.setStrengthDenominator(1.0);
    medicationIngredientDto2.setStrengthDenominatorUnit("ml");

    medicationDataDto1Ingredients.add(medicationIngredientDto1);
    medicationDataDto1Ingredients.add(medicationIngredientDto2);

    medicationDataDto1.setMedicationIngredients(medicationDataDto1Ingredients);
    medicationDataMap.put(medicationDataDto1.getMedication().getId(), medicationDataDto1);
    medicationDataMap.put(medicationDataDto2.getMedication().getId(), medicationDataDto2);

    final Double ingredientQuantityInAdministrations = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        administrationTherapyDoseDto,
        therapyDto,
        ArrayListMultimap.create(),
        Collections.emptyMap(),
        medicationDataMap,
        new Interval(actionTimeStamp.minusDays(1), actionTimeStamp),
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    /**
     * ingredientQuantityInMg = 0.5 * 50 + 0.5 * 50 = 50 mg
     * therapyVolumeSum = 100 ml
     * administrationVolumeSum = 1 l = 1000 ml
     * result = administrationVolumeSum / therapyVolumeSum * ingredientQuantityInMg = 1000 / 100 * 50 = 500
     */

    assertNotNull(ingredientQuantityInAdministrations);
    assertEquals(500, ingredientQuantityInAdministrations.intValue());
  }

  private MedicationDto buildMedicationDto(final long id)
  {
    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(id);
    return medicationDto;
  }
}