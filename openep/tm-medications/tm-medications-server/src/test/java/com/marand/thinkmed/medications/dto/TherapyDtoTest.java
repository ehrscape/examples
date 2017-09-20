package com.marand.thinkmed.medications.dto;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */
public class TherapyDtoTest
{
  @Test
  public void testGetMainMedicationIdFromSimpleTherapy()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    final MedicationDto medication = new MedicationDto();
    medication.setId(1L);

    therapy.setMedication(medication);

    assertEquals(medication.getId(), therapy.getMainMedicationId());
  }

  @Test
  public void testGetMainMedicationIdFromComplexTherapy()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();

    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(1L);
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(medication1);

    final MedicationDto medication2 = new MedicationDto();
    medication2.setId(2L);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(medication2);

    therapy.getIngredientsList().add(ingredient1);
    therapy.getIngredientsList().add(ingredient2);

    final Long mainMedicationId = therapy.getMainMedicationId();
    assertEquals(medication1.getId(), mainMedicationId);
  }

  @Test
  public void testGetMainMedicationIdFromSimpleTherapyNoMedication()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    assertNull(therapy.getMainMedicationId());
  }

  @Test
  public void testGetMainMedicationIdFromComplexTherapyNoIngredients()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();

    assertNull(therapy.getMainMedicationId());
  }

  @Test
  public void testGetMedicationsFromSimpleTherapy()
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    final MedicationDto medication = new MedicationDto();
    medication.setId(1L);

    therapy.setMedication(medication);

    final List<MedicationDto> medications = therapy.getMedications();
    assertEquals(1, medications.size());
    assertEquals(medication, medications.get(0));
  }

  @Test
  public void testGetMedicationsFromComplexTherapy()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();

    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(1L);
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(medication1);

    final MedicationDto medication2 = new MedicationDto();
    medication2.setId(2L);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(medication2);

    therapy.getIngredientsList().add(ingredient1);
    therapy.getIngredientsList().add(ingredient2);

    final List<MedicationDto> medications = therapy.getMedications();
    assertEquals(2, medications.size());
    assertTrue(medications.contains(medication1));
    assertTrue(medications.contains(medication2));
  }
}
