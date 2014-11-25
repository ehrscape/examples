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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.medications.DoseFormType;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSearchDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

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
        "/ac-hibernate-audit.xml",
        "/ac-default-user-details.xml",
        "/ac-catalog.xml",
        "com/marand/thinkmed/medications/ac-hibernate-packages.xml",
        "com/marand/thinkmed/medications/dao/hibernate/HibernateMedicationsDaoTest-context.xml"
    }
)
@DataSet
public class HibernateMedicationsDaoTest
{
  @SpringBeanByName
  private HibernateMedicationsDao medicationsDao;

  @BeforeClass
  public static void setUp()
  {
    KnownClinic.Utils.setValuesProvider(new MedicationsTestUtils.TestClinicProvider());
  }

  @Test
  public void testFindMedications()
  {
    final List<MedicationSimpleDto> resultList = medicationsDao.findMedications(new DateTime(2013, 1, 1, 10, 15));
    assertEquals(2L, (long)resultList.size());

    assertEquals(1L, resultList.get(0).getId());
    assertEquals("Lekadol", resultList.get(0).getName());
    assertEquals("Paracetamol", resultList.get(0).getGenericName());
    assertTrue(resultList.get(0).isActive());

    assertEquals(2L, resultList.get(1).getId());
    assertEquals("Nalgesin 20 mg", resultList.get(1).getName());
    assertEquals("Nalgus + Sinus", resultList.get(1).getGenericName());
    assertFalse(resultList.get(1).isActive());
  }

  @Test
  public void testGetMedicationData()
  {
    final MedicationDataDto medicationData1 = medicationsDao.getMedicationData(1L, new DateTime(2013, 1, 1, 10, 15));
    assertEquals("Oral", medicationData1.getRoutes().iterator().next().getName());
    assertEquals("Oral", medicationData1.getDefaultRoute().getName());
    Assert.assertNull(medicationData1.getDescriptiveIngredient());
    Assert.assertFalse(medicationData1.isAntibiotic());

    final MedicationIngredientDto ingredient1 = medicationData1.getMedicationIngredients().iterator().next();
    assertEquals("Paracetamol", ingredient1.getIngredient().getName());
    assertEquals(new Double(2.0), ingredient1.getStrengthNumerator());
    assertEquals("mg", ingredient1.getStrengthNumeratorUnit());
    Assert.assertNull(ingredient1.getStrengthDenominator());
    Assert.assertNull(ingredient1.getStrengthDenominatorUnit());

    final MedicationDataDto medicationData2 = medicationsDao.getMedicationData(2L, new DateTime(2013, 1, 1, 10, 15));
    final Iterator<MedicationRouteDto> routeIterator = medicationData2.getRoutes().iterator();
    assertEquals("IV", routeIterator.next().getName());
    assertEquals("SC", routeIterator.next().getName());
    assertEquals("IV", medicationData2.getDefaultRoute().getName());
    Assert.assertTrue(medicationData2.isAntibiotic());

    final MedicationIngredientDto descriptiveIngredient = medicationData2.getDescriptiveIngredient();
    assertEquals("Nalgesin", descriptiveIngredient.getIngredient().getName());
    assertEquals(new Double(20.0), descriptiveIngredient.getStrengthNumerator());
    assertEquals("mg", descriptiveIngredient.getStrengthNumeratorUnit());
    Assert.assertNull(descriptiveIngredient.getStrengthDenominator());
    Assert.assertNull(descriptiveIngredient.getStrengthDenominatorUnit());

    final Iterator<MedicationIngredientDto> ingredientIterator = medicationData2.getMedicationIngredients().iterator();

    final MedicationIngredientDto ingredient2 = ingredientIterator.next();
    assertEquals("Nalgus", ingredient2.getIngredient().getName());
    assertEquals(new Double(2.0), ingredient2.getStrengthNumerator());
    assertEquals("mg", ingredient2.getStrengthNumeratorUnit());
    Assert.assertNull(ingredient2.getStrengthDenominator());
    Assert.assertNull(ingredient2.getStrengthDenominatorUnit());

    final MedicationIngredientDto ingredient3 = ingredientIterator.next();
    assertEquals("Sinus", ingredient3.getIngredient().getName());
    assertEquals(new Double(10.0), ingredient3.getStrengthNumerator());
    assertEquals("mg", ingredient3.getStrengthNumeratorUnit());
    assertEquals(new Double(3.0), ingredient3.getStrengthDenominator());
    assertEquals("ml", ingredient3.getStrengthDenominatorUnit());
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
  @DataSet("HibernateMedicationsDaoTest.testFindSimilarMedications.xml")
  public void testFindSimilarMedications1()
  {
    final List<MedicationDto> medications =
        medicationsDao.findSimilarMedications(1L, "11",new DateTime(2013, 1, 1, 10, 15));

    assertEquals(6L, (long)medications.size());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testFindSimilarMedications.xml")
  public void testFindSimilarMedications2()
  {
    final List<MedicationDto> medications =
        medicationsDao.findSimilarMedications(8L, "11", new DateTime(2013, 1, 1, 10, 15));

    assertEquals(6L, (long)medications.size());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testFindSimilarMedications.xml")
  public void testFindSimilarMedications4()
  {
    final List<MedicationDto> medications =
        medicationsDao.findSimilarMedications(4L, "11", new DateTime(2013, 1, 1, 10, 15));

    assertEquals(6L, (long)medications.size());
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
        medicationsDao.getMedicationExternalValues("FDB", MedicationsExternalValueType.UNIT, new HashSet<String>());
    assertTrue(unitsMap.isEmpty());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testGetDoseForms.xml")
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
    Assert.assertNull(doseForms.get(2).getDoseFormType());
    assertEquals(MedicationOrderFormType.SIMPLE, doseForms.get(2).getMedicationOrderFormType());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testGetRoutes.xml")
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
  @DataSet("HibernateMedicationsDaoTest.testGetMedicationBasicUnits.xml")
  public void testGetMedicationBasicUnits()
  {
    final List<String> units = medicationsDao.getMedicationBasicUnits();
    assertEquals(2L, (long)units.size());

    assertEquals("kg", units.get(0));
    assertEquals("ml", units.get(1));
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testGetDoseFormByCode.xml")
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
  @DataSet("HibernateMedicationsDaoTest.testGetDoseFormByCode.xml")
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
  @DataSet("HibernateMedicationsDaoTest.testGetMedicationDataForTherapyFlow.xml")
  public void testGetMedicationDataForTherapyFlowByCodes()
  {
    final Set<Long> medicationIds = new HashSet<>();
    medicationIds.add(1L);
    medicationIds.add(2L);

    final Map<Long, MedicationDataForTherapyDto> resultsMap =
        medicationsDao.getMedicationDataForTherapies(
            medicationIds,
            MedicationsTestUtils.TestingKnownClinicEnum.PEK,
            new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(2L, (long)resultsMap.size());

    final MedicationDataForTherapyDto result1 = resultsMap.get(1L);
    assertEquals("C1", result1.getAtcCode());
    assertEquals("ATC 1", result1.getAtcName());
    assertEquals("Paracetamol PEK", result1.getCustomGroupName());
    assertEquals("Paracetamol", result1.getGenericName());
    assertTrue(result1.isAntibiotic());

    final MedicationDataForTherapyDto result3 = resultsMap.get(2L);
    assertEquals("C1", result3.getAtcCode());
    assertEquals("ATC 1", result3.getAtcName());
    assertNull(result3.getCustomGroupName());
    assertNull(result3.getGenericName());
    assertFalse(result3.isAntibiotic());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testCustomGroups.xml")
  public void testGetCustomGroupNameSortOrderMap()
  {
    final List<String> groups = medicationsDao.getCustomGroupNames("KOOKIT");

    assertEquals(2L, (long)groups.size());
    assertEquals("Paracetamol KOOKIT", groups.get(0));
    assertEquals("Acet. kislina KOOKIT", groups.get(1));
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testCustomGroups.xml")
  public void testGetCustomGroupNames()
  {
    final Set<Long> medicationCodes = new HashSet<>();
    medicationCodes.add(1L);
    medicationCodes.add(2L);
    medicationCodes.add(3L);
    medicationCodes.add(4L);

    final Map<Long, Pair<String, Integer>> resultMapPek =
        medicationsDao.getCustomGroupNameSortOrderMap("PEK", medicationCodes);

    assertEquals(1L, (long)resultMapPek.size());
    assertEquals("Paracetamol PEK", resultMapPek.get(1L).getFirst());
    assertEquals(Integer.valueOf(2), resultMapPek.get(1L).getSecond());

    final Map<Long, Pair<String, Integer>> resultMapKookit =
        medicationsDao.getCustomGroupNameSortOrderMap("KOOKIT", medicationCodes);
    assertEquals(4L, (long)resultMapKookit.size());
    assertEquals("Paracetamol KOOKIT", resultMapKookit.get(1L).getFirst());
    assertEquals(Integer.valueOf(1), resultMapKookit.get(1L).getSecond());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetTherapyTemplates1()
  {
    final TherapyTemplatesDto templatesDto =
        medicationsDao.getTherapyTemplates(2L, 1L, null, null, null, new DateTime(2013, 11, 1, 0, 0, 0), new Locale("en"));
    assertEquals(2L, (long)templatesDto.getUserTemplates().size());

    final TherapyTemplateDto firstUserTemplate = templatesDto.getUserTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.USER, firstUserTemplate.getType());
    assertEquals("AAA", firstUserTemplate.getName());
    assertEquals(2L, (long)firstUserTemplate.getUserId());
    assertNull(firstUserTemplate.getDepartmentId());
    assertEquals(2L, (long)firstUserTemplate.getTemplateElements().size());
    assertTrue(firstUserTemplate.getTemplateElements().get(0).isCompleted());
    assertFalse(firstUserTemplate.getTemplateElements().get(1).isCompleted());

    final TherapyTemplateDto secondUserTemplate = templatesDto.getUserTemplates().get(1);
    assertEquals(TherapyTemplateTypeEnum.USER, secondUserTemplate.getType());
    assertEquals("BBB", secondUserTemplate.getName());
    assertEquals(2L, (long)secondUserTemplate.getUserId());
    assertNull(secondUserTemplate.getDepartmentId());
    assertEquals(1L, (long)secondUserTemplate.getTemplateElements().size());
    assertTrue(secondUserTemplate.getTemplateElements().get(0).isCompleted());

    assertEquals(1L, (long)templatesDto.getOrganizationTemplates().size());
    final TherapyTemplateDto organizationTemplate = templatesDto.getOrganizationTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.ORGANIZATIONAL, organizationTemplate.getType());
    assertEquals("CCC", organizationTemplate.getName());
    assertNull(organizationTemplate.getUserId());
    assertEquals(1L, (long)organizationTemplate.getDepartmentId());
    assertEquals(1L, (long)organizationTemplate.getTemplateElements().size());
    assertTrue(organizationTemplate.getTemplateElements().get(0).isCompleted());

    assertEquals(0L, (long)templatesDto.getPatientTemplates().size());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testGetTherapyTemplates.xml")
  public void testGetTherapyTemplates2()
  {
    final TherapyTemplatesDto templatesDto =
        medicationsDao.getTherapyTemplates(2L, null, 3L, null, null, new DateTime(2013, 11, 1, 0, 0, 0), new Locale("en"));
    assertEquals(2L, (long)templatesDto.getUserTemplates().size());
    assertEquals(0L, (long)templatesDto.getOrganizationTemplates().size());
    assertEquals(1L, (long)templatesDto.getPatientTemplates().size());

    final TherapyTemplateDto patientTemplate = templatesDto.getPatientTemplates().get(0);
    assertEquals(TherapyTemplateTypeEnum.PATIENT, patientTemplate.getType());
    assertEquals("DDD", patientTemplate.getName());
    assertNull(patientTemplate.getUserId());
    assertEquals(3L, (long)patientTemplate.getPatientId());
    assertEquals(1L, (long)patientTemplate.getTemplateElements().size());
    assertTrue(patientTemplate.getTemplateElements().get(0).isCompleted());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testMedicationsTree.xml")
  public void testLoadMedicationsTree1()
  {
    final List<MedicationSearchDto> tree = medicationsDao.loadMedicationsTree(new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(1, tree.size());
    assertEquals(1L, (long)tree.get(0).getKey());
    assertNull(tree.get(0).getParentId());
    assertEquals("Paracetamol", tree.get(0).getTitle());
    assertTrue(tree.get(0).isUnselectable());
    final List<MedicationSearchDto> children1 = tree.get(0).getSublevelMedications();
    assertEquals(2, children1.size());

    assertEquals(12L, (long)children1.get(0).getKey());
    assertEquals(1L, (long)children1.get(0).getParentId());
    assertEquals("Paracetamol 250mg supp", children1.get(0).getTitle());
    assertFalse(children1.get(0).isUnselectable());
    final List<MedicationSearchDto> children12 = children1.get(0).getSublevelMedications();
    assertEquals(1, children12.size());

    assertEquals(121L, (long)children12.get(0).getKey());
    assertEquals(12L, (long)children12.get(0).getParentId());
    assertFalse(children12.get(0).isUnselectable());
    assertEquals("Lekadol 250mg supp", children12.get(0).getTitle());

    assertEquals(11L, (long)children1.get(1).getKey());
    assertEquals(1L, (long)children1.get(1).getParentId());
    assertEquals("Paracetamol 500mg tablet", children1.get(1).getTitle());
    assertFalse(children1.get(1).isUnselectable());
    final List<MedicationSearchDto> children11 = children1.get(1).getSublevelMedications();
    assertEquals(2, children11.size());

    assertEquals(111L, (long)children11.get(0).getKey());
    assertEquals(11L, (long)children11.get(0).getParentId());
    assertFalse(children11.get(0).isUnselectable());
    assertEquals("Lekadol 500mg tablet", children11.get(0).getTitle());

    assertEquals(112L, (long)children11.get(1).getKey());
    assertEquals(11L, (long)children11.get(1).getParentId());
    assertTrue(children11.get(1).isUnselectable());
    assertEquals("Daleron 500mg tablet", children11.get(1).getTitle());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testMedicationsTree.xml")
  public void testGetMedicationProducts1()
  {
    final List<MedicationDto> medicationProducts =
        medicationsDao.getMedicationProducts(1L, "1", new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(2, medicationProducts.size());
    assertEquals(112L, (long)medicationProducts.get(0).getId());
    assertEquals(111L, (long)medicationProducts.get(1).getId());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testMedicationsTree.xml")
  public void testGetMedicationProducts2()
  {
    final List<MedicationDto> medicationProducts =
        medicationsDao.getMedicationProducts(1L, "2", new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(1, medicationProducts.size());
    assertEquals(121L, (long)medicationProducts.get(0).getId());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testMedicationsTree.xml")
  public void testGetMedicationProducts3()
  {
    final List<MedicationDto> medicationProducts =
        medicationsDao.getMedicationProducts(11L, "1", new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(2, medicationProducts.size());
    assertEquals(112L, (long)medicationProducts.get(0).getId());
    assertEquals(111L, (long)medicationProducts.get(1).getId());
  }

  @Test
  @DataSet("HibernateMedicationsDaoTest.testMedicationsTree.xml")
  public void testGetMedicationProducts4()
  {
    final List<MedicationDto> medicationProducts =
        medicationsDao.getMedicationProducts(12L, "2", new DateTime(2013, 11, 1, 0, 0, 0));
    assertEquals(1, medicationProducts.size());
    assertEquals(121L, (long)medicationProducts.get(0).getId());
  }
}
