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

package com.marand.thinkmed.medications.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.marand.maf.core.Pair;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.DoseFormType;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.BnfMaximumUnitType;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringProxiedJUnit4ClassRunner.class)
@ContextConfiguration({"/com/marand/thinkmed/medications/dao/hibernate/HibernateMedicationsDaoTest-context.xml"})
@DbUnitConfiguration(databaseConnection = {"dbUnitDatabaseConnection"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@DatabaseSetup("HibernateMedicationsDaoTest.xml")
@Transactional
public class HibernateMedicationsDaoTest
{
  @Autowired
  private MedicationsDao medicationsDao;

  @Test
  public void testGetMedicationDataPill()
  {
    final MedicationDataDto medicationData = medicationsDao.getMedicationData(1L, "CP1", new DateTime(2013, 1, 1, 10, 15));

    final MedicationRouteDto route = medicationData.getRoutes().get(0);
    assertNotNull(route.getBnfMaximumDto());
    assertEquals(500, (int)route.getBnfMaximumDto().getQuantity());
    assertEquals(BnfMaximumUnitType.DAY, route.getBnfMaximumDto().getQuantityUnit());
    assertEquals("Oral", route.getName());
    assertEquals(route, medicationData.getDefaultRoute());

    assertNull(medicationData.getDescriptiveIngredient());
    assertFalse(medicationData.isAntibiotic());
    assertFalse(medicationData.getControlledDrug());
    assertEquals(TitrationType.BLOOD_SUGAR, medicationData.getTitration());
    assertNull(medicationData.getMedicationPackaging());
    assertEquals(0.5, medicationData.getRoundingFactor(), 0.001);

    final MedicationIngredientDto ingredient = medicationData.getMedicationIngredients().iterator().next();
    assertEquals("Paracetamol", ingredient.getIngredientName());
    assertEquals(new Double(2.0), ingredient.getStrengthNumerator());
    assertEquals("mg", ingredient.getStrengthNumeratorUnit());
    assertNull(ingredient.getStrengthDenominator());
    assertNull(ingredient.getStrengthDenominatorUnit());
    assertTrue(medicationData.isBlackTriangleMedication());
    assertFalse(medicationData.isClinicalTrialMedication());
    assertFalse(medicationData.isInpatientMedication());
    assertTrue(medicationData.isOutpatientMedication());
    assertTrue(medicationData.isFormulary());
  }

  @Test
  public void testGetMedicationDataInfusion()
  {
    final MedicationDataDto medicationData = medicationsDao.getMedicationData(2L, "CP1", new DateTime(2013, 1, 1, 10, 15));
    final MedicationRouteDto route1 = medicationData.getRoutes().get(0);
    assertNull(route1.getBnfMaximumDto());
    assertEquals("IV", route1.getName());
    assertEquals(route1, medicationData.getDefaultRoute());

    final MedicationRouteDto route2 = medicationData.getRoutes().get(1);
    assertEquals("SC", route2.getName());
    assertNotNull(route2.getBnfMaximumDto());
    assertEquals(200, (int)route2.getBnfMaximumDto().getQuantity());
    assertEquals(BnfMaximumUnitType.WEEK, route2.getBnfMaximumDto().getQuantityUnit());
    assertTrue(medicationData.isAntibiotic());
    assertTrue(medicationData.getControlledDrug());
    assertNull(medicationData.getTitration());
    assertEquals("Packaging 20", medicationData.getMedicationPackaging());
    assertNull(medicationData.getRoundingFactor());
    assertFalse(medicationData.isBlackTriangleMedication());
    assertTrue(medicationData.isClinicalTrialMedication());
    assertFalse(medicationData.isInpatientMedication());
    assertTrue(medicationData.isOutpatientMedication());
    assertTrue(medicationData.isFormulary());

    final MedicationIngredientDto descriptiveIngredient = medicationData.getDescriptiveIngredient();
    assertEquals("Nalgesin", descriptiveIngredient.getIngredientName());
    assertEquals(new Double(20.0), descriptiveIngredient.getStrengthNumerator());
    assertEquals("mg", descriptiveIngredient.getStrengthNumeratorUnit());
    assertNull(descriptiveIngredient.getStrengthDenominator());
    assertNull(descriptiveIngredient.getStrengthDenominatorUnit());

    final Iterator<MedicationIngredientDto> ingredientIterator = medicationData.getMedicationIngredients().iterator();

    final MedicationIngredientDto ingredient1 = ingredientIterator.next();
    assertEquals("Nalgus", ingredient1.getIngredientName());
    assertEquals(new Double(2.0), ingredient1.getStrengthNumerator());
    assertEquals("mg", ingredient1.getStrengthNumeratorUnit());
    assertNull(ingredient1.getStrengthDenominator());
    assertNull(ingredient1.getStrengthDenominatorUnit());

    final MedicationIngredientDto ingredient2 = ingredientIterator.next();
    assertEquals("Sinus", ingredient2.getIngredientName());
    assertEquals(new Double(10.0), ingredient2.getStrengthNumerator());
    assertEquals("mg", ingredient2.getStrengthNumeratorUnit());
    assertEquals(new Double(3.0), ingredient2.getStrengthDenominator());
    assertEquals("mL", ingredient2.getStrengthDenominatorUnit());
  }

  @Test
  public void testGetMedicationExternalId()
  {
    final String fdbExternalId = medicationsDao.getMedicationExternalId("FDB", 1L, new DateTime(2013, 1, 1, 10, 15));
    assertEquals("111", fdbExternalId);

    final String winpisExternalId = medicationsDao.getMedicationExternalId("WINPIS", 1L, new DateTime(2013, 1, 1, 10, 15));
    assertEquals("222", winpisExternalId);
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testFindSimilarMedications.xml")
  public void testFindSimilarMedications()
  {
    final List<Long> routeIds = Collections.singletonList(11L);

    final Set<Long> medicationIds = medicationsDao.findSimilarMedicationsIds(
        1L,
        routeIds,
        new DateTime(2013, 1, 1, 10, 15));

    assertEquals(4L, (long)medicationIds.size());
    assertTrue(medicationIds.contains(1L));
    assertTrue(medicationIds.contains(2L));
    assertTrue(medicationIds.contains(4L));
    assertTrue(medicationIds.contains(7L));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testFindSimilarMedications.xml")
  public void testFindSimilarMedicationsMultipleRoutes()
  {
    final List<Long> routeIds = new ArrayList<>();
    routeIds.add(11L);
    routeIds.add(12L);
    routeIds.add(2L);

    final Set<Long> medicationIds = medicationsDao.findSimilarMedicationsIds(
        4L,
        routeIds,
        new DateTime(2013, 1, 1, 10, 15));

    assertEquals(5L, (long)medicationIds.size());
    assertTrue(medicationIds.contains(1L));
    assertTrue(medicationIds.contains(2L));
    assertTrue(medicationIds.contains(4L));
    assertTrue(medicationIds.contains(5L));
    assertTrue(medicationIds.contains(7L));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testFindSimilarMedications.xml")
  public void testFindSimilarMedicationsMultipleRoutesNoSimilar()
  {
    final List<Long> routeIds = new ArrayList<>();
    routeIds.add(12L);
    routeIds.add(13L);

    final Set<Long> medicationIds = medicationsDao.findSimilarMedicationsIds(
        3L,
        routeIds,
        new DateTime(2013, 1, 1, 10, 15));

    assertEquals(1L, medicationIds.size());
    assertTrue(medicationIds.contains(3L));
  }

  @Test
  public void testGetMedicationExternalValuesForRoutes()
  {
    final Set<String> routesSet = new HashSet<>();
    routesSet.add("R001");
    routesSet.add("R002");

    final Map<String, String> routesMap =
        medicationsDao.getMedicationExternalValues("FDB", MedicationsExternalValueType.ROUTE, routesSet);
    assertEquals("ExternalRoute1", routesMap.get("R001"));
    assertEquals("ExternalRoute2", routesMap.get("R002"));
  }

  @Test
  public void testGetMedicationExternalValuesForUnits()
  {
    final Set<String> unitsSet = new HashSet<>();
    unitsSet.add("tableta");
    unitsSet.add("viala");

    final Map<String, String> unitsMap =
        medicationsDao.getMedicationExternalValues("FDB", MedicationsExternalValueType.UNIT, unitsSet);
    assertEquals("tbl", unitsMap.get("tableta"));
    assertEquals("vial", unitsMap.get("viala"));
  }

  @Test
  public void testGetMedicationExternalValuesForUnitsWithEmptySet()
  {
    final Map<String, String> unitsMap =
        medicationsDao.getMedicationExternalValues("FDB", MedicationsExternalValueType.UNIT, new HashSet<>());
    assertTrue(unitsMap.isEmpty());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetDoseForms.xml")
  public void testGetDoseForms()
  {
    final List<DoseFormDto> doseForms = medicationsDao.getDoseForms(new DateTime(2013, 10, 28, 12, 0, 0));
    assertEquals(3L, (long)doseForms.size());

    assertEquals("1", doseForms.get(0).getCode());
    assertEquals("Pill", doseForms.get(0).getName());
    assertEquals(DoseFormType.TBL, doseForms.get(0).getDoseFormType());
    assertEquals(MedicationOrderFormType.SIMPLE, doseForms.get(0).getMedicationOrderFormType());

    assertEquals("2", doseForms.get(1).getCode());
    assertEquals("Suppository", doseForms.get(1).getName());
    assertEquals(DoseFormType.SUPPOSITORY, doseForms.get(1).getDoseFormType());
    assertEquals(MedicationOrderFormType.SIMPLE, doseForms.get(1).getMedicationOrderFormType());

    assertEquals("3", doseForms.get(2).getCode());
    assertEquals("Syrup", doseForms.get(2).getName());
    assertNull(doseForms.get(2).getDoseFormType());
    assertEquals(MedicationOrderFormType.SIMPLE, doseForms.get(2).getMedicationOrderFormType());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetRoutes.xml")
  public void testGetRoutes()
  {
    final List<MedicationRouteDto> routes = medicationsDao.getRoutes(new DateTime(2013, 10, 28, 12, 0, 0));
    assertEquals(3L, (long)routes.size());

    assertEquals("1", routes.get(0).getCode());
    assertEquals("po", routes.get(0).getName());
    assertNull(routes.get(0).getType());

    assertEquals("4", routes.get(1).getCode());
    assertEquals("ivk", routes.get(1).getName());
    assertEquals(MedicationRouteTypeEnum.IV, routes.get(1).getType());

    assertEquals("5", routes.get(2).getCode());
    assertEquals("im", routes.get(2).getName());
    assertEquals(MedicationRouteTypeEnum.IV, routes.get(2).getType());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationBasicUnits.xml")
  public void testGetMedicationBasicUnits()
  {
    final List<String> units = medicationsDao.getMedicationBasicUnits();
    assertEquals(2L, (long)units.size());

    assertEquals("kg", units.get(0));
    assertEquals("mL", units.get(1));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetDoseFormByCode.xml")
  public void testGetDoseFormByCode1()
  {
    final DoseFormDto doseForm = medicationsDao.getDoseFormByCode("1", new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(1L, doseForm.getId());
    assertEquals("1", doseForm.getCode());
    assertEquals("Tablet", doseForm.getName());
    assertEquals(DoseFormType.TBL, doseForm.getDoseFormType());
    assertEquals(MedicationOrderFormType.SIMPLE, doseForm.getMedicationOrderFormType());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetDoseFormByCode.xml")
  public void testGetDoseFormByCode2()
  {
    final DoseFormDto doseForm = medicationsDao.getDoseFormByCode("2", new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(2L, doseForm.getId());
    assertEquals("2", doseForm.getCode());
    assertEquals("Fluid", doseForm.getName());
    assertNull(doseForm.getDoseFormType());
    assertEquals(MedicationOrderFormType.COMPLEX, doseForm.getMedicationOrderFormType());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testCustomGroups.xml")
  public void testGetCustomGroupNameSortOrderMap()
  {
    final List<String> groups = medicationsDao.getCustomGroupNames("2");

    assertEquals(2L, (long)groups.size());
    assertEquals("Acet. kislina KOOKIT EIT", groups.get(0));
    assertEquals("Paracetamol KOOKIT EIT", groups.get(1));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testCustomGroups.xml")
  public void testGetCustomGroupNames()
  {
    final Set<Long> medicationCodes = new HashSet<>();
    medicationCodes.add(1L);
    medicationCodes.add(2L);
    medicationCodes.add(3L);
    medicationCodes.add(4L);

    final Map<Long, Pair<String, Integer>> resultMapPek =
        medicationsDao.getCustomGroupNameSortOrderMap("1", medicationCodes);

    assertEquals(1L, (long)resultMapPek.size());
    assertEquals("Paracetamol Kardio Hosp", resultMapPek.get(1L).getFirst());
    assertEquals(Integer.valueOf(2), resultMapPek.get(1L).getSecond());

    final Map<Long, Pair<String, Integer>> resultMapKookit =
        medicationsDao.getCustomGroupNameSortOrderMap("2", medicationCodes);
    assertEquals(4L, (long)resultMapKookit.size());
    assertEquals("Paracetamol KOOKIT EIT", resultMapKookit.get(1L).getFirst());
    assertEquals(Integer.valueOf(1), resultMapKookit.get(1L).getSecond());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetTherapyTemplates1()
  {
    final TherapyTemplatesDto templatesDto =
        medicationsDao.getTherapyTemplates("555", "2", TherapyTemplateModeEnum.INPATIENT, "1", null, null, new Locale("en"));
    assertEquals(2L, (long)templatesDto.getUserTemplates().size());

    final TherapyTemplateDto firstUserTemplate = templatesDto.getUserTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.USER, firstUserTemplate.getType());
    assertEquals("AAA", firstUserTemplate.getName());
    assertEquals("2", firstUserTemplate.getUserId());
    assertNull(firstUserTemplate.getCareProviderId());
    assertEquals(2L, (long)firstUserTemplate.getTemplateElements().size());
    assertTrue(firstUserTemplate.getTemplateElements().get(0).isCompleted());
    assertFalse(firstUserTemplate.getTemplateElements().get(1).isCompleted());

    final TherapyTemplateDto secondUserTemplate = templatesDto.getUserTemplates().get(1);
    assertEquals(TherapyTemplateTypeEnum.USER, secondUserTemplate.getType());
    assertEquals("BBB", secondUserTemplate.getName());
    assertEquals("2", secondUserTemplate.getUserId());
    assertNull(secondUserTemplate.getCareProviderId());
    assertEquals(1L, (long)secondUserTemplate.getTemplateElements().size());
    assertTrue(secondUserTemplate.getTemplateElements().get(0).isCompleted());

    assertEquals(1L, (long)templatesDto.getOrganizationTemplates().size());
    final TherapyTemplateDto organizationTemplate = templatesDto.getOrganizationTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.ORGANIZATIONAL, organizationTemplate.getType());
    assertEquals("CCC", organizationTemplate.getName());
    assertNull(organizationTemplate.getUserId());
    assertEquals("1", organizationTemplate.getCareProviderId());
    assertEquals(1L, (long)organizationTemplate.getTemplateElements().size());
    assertTrue(organizationTemplate.getTemplateElements().get(0).isCompleted());

    assertEquals(0L, (long)templatesDto.getPatientTemplates().size());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetTherapyTemplates2()
  {
    final TherapyTemplatesDto templatesDto =
        medicationsDao.getTherapyTemplates("3", "2", TherapyTemplateModeEnum.INPATIENT, null, null, null, new Locale("en"));
    assertEquals(2L, (long)templatesDto.getUserTemplates().size());
    assertEquals(0L, (long)templatesDto.getOrganizationTemplates().size());
    assertEquals(1L, (long)templatesDto.getPatientTemplates().size());

    final TherapyTemplateDto patientTemplate = templatesDto.getPatientTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.PATIENT, patientTemplate.getType());
    assertEquals("DDD", patientTemplate.getName());
    assertNull(patientTemplate.getUserId());
    assertEquals("3", patientTemplate.getPatientId());
    assertEquals(1L, (long)patientTemplate.getTemplateElements().size());
    assertTrue(patientTemplate.getTemplateElements().get(0).isCompleted());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testMedicationsTree.xml")
  public void testGetMedicationProducts1()
  {
    final List<Long> routeIds = Collections.singletonList(1L);

    final List<MedicationDto> medicationProducts =
        medicationsDao.getMedicationChildProducts(1L, routeIds, new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(2, medicationProducts.size());
    assertEquals(112L, (long)medicationProducts.get(0).getId());
    assertEquals(111L, (long)medicationProducts.get(1).getId());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testMedicationsTree.xml")
  public void testGetMedicationProducts2()
  {
    final List<Long> routeIds = Collections.singletonList(2L);

    final List<MedicationDto> medicationProducts =
        medicationsDao.getMedicationChildProducts(1L, routeIds, new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(1, medicationProducts.size());
    assertEquals(121L, (long)medicationProducts.get(0).getId());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testMedicationsTree.xml")
  public void testGetMedicationProducts3()
  {
    final List<Long> routeIds = Collections.singletonList(1L);

    final List<MedicationDto> medicationProducts =
        medicationsDao.getMedicationChildProducts(11L, routeIds, new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(2, medicationProducts.size());
    assertEquals(112L, (long)medicationProducts.get(0).getId());
    assertEquals(111L, (long)medicationProducts.get(1).getId());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testMedicationsTree.xml")
  public void testGetMedicationProducts4()
  {
    final List<Long> routeIds = Collections.singletonList(2L);

    final List<MedicationDto> medicationProducts =
        medicationsDao.getMedicationChildProducts(12L, routeIds, new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(1, medicationProducts.size());
    assertEquals(121L, (long)medicationProducts.get(0).getId());
  }

  @Test
  public void getMedicationIdsWithIngredientRule()
  {
    final Set<Long> medicationIdsWithIngredientRule = medicationsDao.getMedicationIdsWithIngredientRule(
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        new DateTime(2013, 11, 1, 0, 0, 0));

    assertTrue(medicationIdsWithIngredientRule.contains(1L));
  }

  @Test
  public void getMedicationIdsWithIngredientId()
  {
    final List<Long> medicationIdsWithIngredientId = medicationsDao.getMedicationIdsWithIngredientId(
        2L,
        new DateTime(2013, 11, 1, 0, 0, 0));

    assertTrue(medicationIdsWithIngredientId.contains(2L));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testLoadMedicationsMap.xml")
  public void testGetMedicationDataMap()
  {
    final Map<Long, MedicationHolderDto> medicationsMap =
        medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0));

    assertEquals(1, medicationsMap.size());
    final MedicationHolderDto dto = medicationsMap.get(1L);
    assertEquals(1L, (long)dto.getId());
    assertEquals("Lekadol", dto.getName());
    assertEquals("Lek", dto.getShortName());
    assertEquals("Paracetamol", dto.getGenericName());
    assertEquals(MedicationLevelEnum.VTM, dto.getMedicationLevel());
    assertEquals(1L, (long)dto.getVtmId());
    assertNull(dto.getVmpId());
    assertNull(dto.getAmpId());
    assertEquals(1L, dto.getDoseFormDto().getId());
    assertEquals("Pill", dto.getDoseFormDto().getName());
    assertEquals("C1", dto.getAtcGroupCode());
    assertEquals("ATC 1", dto.getAtcGroupName());
    assertEquals(1L, dto.getDefiningIngredient().getId());
    assertEquals("Paracetamol", dto.getDefiningIngredient().getIngredientName());
    assertEquals(Double.valueOf(2.0), dto.getDefiningIngredient().getStrengthNumerator());
    assertEquals("mg", dto.getDefiningIngredient().getStrengthNumeratorUnit());
    assertEquals(MedicationTypeEnum.MEDICATION, dto.getMedicationType());
    assertEquals(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE, dto.getMedicationRules().iterator().next());
    assertTrue(dto.getFormularyCareProviders().contains("CP1"));
    assertTrue(dto.getFormularyCareProviders().contains("CP2"));
    assertTrue(dto.isActive());
    assertFalse(dto.isAntibiotic());
    assertTrue(dto.isFormulary());
    assertTrue(dto.isInpatientMedication());
    assertTrue(dto.isOutpatientMedication());
    assertFalse(dto.isSuggestSwitchToOral());
    assertFalse(dto.isMentalHealthDrug());
    assertTrue(dto.isOrderable());
    assertFalse(dto.isReviewReminder());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testLoadMedicationWarnings.xml")
  public void testGetMedicationWarningsEmptyMedicationList()
  {
    final Collection<MedicationsWarningDto> customWarningsForMedication = medicationsDao.getCustomWarningsForMedication(
        Collections.emptySet(),
        DateTime.now());

    assertTrue(customWarningsForMedication.isEmpty());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testLoadMedicationWarnings.xml")
  public void testGetMedicationWarningsEmptyResult()
  {
    final Collection<MedicationsWarningDto> customWarningsForMedication = medicationsDao.getCustomWarningsForMedication(
        Collections.singleton(3L),
        DateTime.now());

    assertTrue(customWarningsForMedication.isEmpty());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testLoadMedicationWarnings.xml")
  public void testGetMedicationWarnings()
  {
    final Set<Long> medicationIds = new HashSet<>();
    medicationIds.add(1L);
    medicationIds.add(2L);

    final Collection<MedicationsWarningDto> customWarningsForMedication = medicationsDao.getCustomWarningsForMedication(
        medicationIds,
        DateTime.now());

    assertTrue(customWarningsForMedication.stream().anyMatch(
        w ->
        {
          final List<NamedExternalDto> medications = w.getMedications();
          return medications.stream().anyMatch(m -> "1".equals(m.getId())) && w.getSeverity() == WarningSeverity.SIGNIFICANT;
        }));

    assertTrue(customWarningsForMedication.stream().anyMatch(
        w ->
        {
          final List<NamedExternalDto> medications = w.getMedications();
          return medications.stream().anyMatch(m -> "2".equals(m.getId())) && w.getSeverity() == WarningSeverity.LOW;
        }));
  }

  @Test
  public void getMedicationIdsWithIngredientId2()
  {
    final List<Long> medicationIdsWithIngredientId = medicationsDao.getMedicationIdsWithIngredientId(
        10L,
        new DateTime(2013, 11, 1, 0, 0, 0));

    assertTrue(medicationIdsWithIngredientId.isEmpty());
  }

  @Test
  public void testGetMedicationIdForBarcodeExist()
  {
    final Long medicationId = medicationsDao.getMedicationIdForBarcode("111");
    assertEquals(Long.valueOf(1L), medicationId);
  }


  @Test
  public void testGetMedicationIdForBarcodeNotExist()
  {
    final Long medicationId = medicationsDao.getMedicationIdForBarcode("222");
    assertNull(medicationId);
  }
}
