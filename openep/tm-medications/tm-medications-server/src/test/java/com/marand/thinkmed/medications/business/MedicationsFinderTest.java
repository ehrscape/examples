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

package com.marand.thinkmed.medications.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.mapper.MedicationHolderDtoMapper;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.anyLong;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class MedicationsFinderTest
{
  @InjectMocks
  private MedicationsFinderImpl medicationsFinder = new MedicationsFinderImpl();

  @Mock
  private ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder;

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private TherapyDisplayProvider therapyDisplayProvider;

  @Spy
  private MedicationHolderDtoMapper medicationHolderDtoMapper = new MedicationHolderDtoMapper();

  @Before
  public void setUpValueHolderMock()
  {
    medicationHolderDtoMapper.setMarkNonFormularyMedication(true);
    Mockito
        .when(medicationsValueHolder.getValue())
        .thenReturn(getMedicationsMap());
  }

  @Test
  public void testFilterMedicationsTreeSingleGeneric()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "para", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(2, filteredMedications.get(0).getChildren().size());
    assertEquals(2, filteredMedications.get(0).getChildren().get(0).getChildren().size());
    assertEquals(2, filteredMedications.get(0).getChildren().get(1).getChildren().size());

    assertFalse(filteredMedications.get(0).isExpanded());
    assertFalse(filteredMedications.get(0).getChildren().get(0).isExpanded());
    assertFalse(filteredMedications.get(0).getChildren().get(1).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeGenericDose()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "para 500", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(2, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpanded());
    assertFalse(filteredMedications.get(0).getChildren().get(0).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeBrandDose1()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "leka 500", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpanded());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeBrandDose2()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "dale 500", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpanded());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeBrand()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "dale", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(2, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(1).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpanded());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpanded());
    assertTrue(filteredMedications.get(0).getChildren().get(1).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeDoesntStartWithSearchWord1()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "aleron", true);

    assertEquals(0, filteredMedications.size());
  }

  @Test
  public void testFilterMedicationsTreeDoesntStartWithSearchWord2()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "aleron", false);

    assertEquals(1, filteredMedications.size());
  }

  @Test
  public void testFilterMedicationsTreeEmptySearch()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "", true);

    assertEquals(1, filteredMedications.size());
  }

  @Test
  public void testFindMedicationsNoFilter()
  {
    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications("para", true, null, EnumSet.noneOf(MedicationFinderFilterEnum.class));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(2, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(2, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Lekadol 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());

    final TreeNodeData amp12 = vmp1.getChildren().get(1);
    assertEquals("Daleron 500 mg oral", ((MedicationSimpleDto)amp12.getData()).getName());

    final TreeNodeData vmp2 = vtm.getChildren().get(1);
    assertEquals("Paracetamol 250 mg oral", ((MedicationSimpleDto)vmp2.getData()).getName());
    assertEquals(2, vmp2.getChildren().size());

    final TreeNodeData amp21 = vmp2.getChildren().get(0);
    assertEquals("Lekadol 250 mg oral", ((MedicationSimpleDto)amp21.getData()).getName());

    final TreeNodeData amp22 = vmp2.getChildren().get(1);
    assertEquals("Daleron 250 mg oral", ((MedicationSimpleDto)amp22.getData()).getName());
  }

  @Test
  public void testFindMedicationsInpatientFilter()
  {
    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications("para", true, null, EnumSet.of(MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(2, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Lekadol 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());

    final TreeNodeData vmp2 = vtm.getChildren().get(1);
    assertEquals("Paracetamol 250 mg oral", ((MedicationSimpleDto)vmp2.getData()).getName());
    assertEquals(1, vmp2.getChildren().size());

    final TreeNodeData amp21 = vmp2.getChildren().get(0);
    assertEquals("Lekadol 250 mg oral", ((MedicationSimpleDto)amp21.getData()).getName());
  }

  @Test
  public void testFindMedicationsOutpatientFilter()
  {
    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications("para", true, null, EnumSet.of(MedicationFinderFilterEnum.OUTPATIENT_PRESCRIPTION));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(2, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Daleron 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());

    final TreeNodeData vmp2 = vtm.getChildren().get(1);
    assertEquals("Paracetamol 250 mg oral", ((MedicationSimpleDto)vmp2.getData()).getName());
    assertEquals(1, vmp2.getChildren().size());

    final TreeNodeData amp21 = vmp2.getChildren().get(0);
    assertEquals("Daleron 250 mg oral", ((MedicationSimpleDto)amp21.getData()).getName());
  }

  @Test
  public void testFindMedicationsInpatientFormularyFilter()
  {
    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            null,
            EnumSet.of(MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION, MedicationFinderFilterEnum.FORMULARY));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(1, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Lekadol 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());
  }

  @Test
  public void testFindMedicationsInpatientFormularyCareProviderFilter()
  {
    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            "cp1",
            EnumSet.of(MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION, MedicationFinderFilterEnum.FORMULARY));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(1, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Lekadol 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());
  }

  @Test
  public void testFindMedicationsInpatientFormularyCareProviderFilter2()
  {
    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            "cp2",
            EnumSet.of(MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION, MedicationFinderFilterEnum.FORMULARY));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(1, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(0, vmp1.getChildren().size());
  }

  @Test
  public void testFindMedicationsInpatientMentalHealthFilter()
  {
    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            null,
            EnumSet.of(MedicationFinderFilterEnum.MENTAL_HEALTH));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(1, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Daleron 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());
  }

  @Test
  public void testFindSimilarMedications()
  {
    Mockito
        .when(medicationsDao.findSimilarMedicationsIds(anyLong(), anyListOf(Long.class), any()))
        .thenReturn(Sets.newHashSet(11L, 12L, 111L, 112L, 121L, 122L));

    final List<TreeNodeData> searchResult =
        medicationsFinder.findSimilarMedications(11L, Collections.singletonList(10L), new DateTime());

    assertEquals(2L, searchResult.size());
    final TreeNodeData vmp1 = searchResult.get(0);
    final TreeNodeData vmp2 = searchResult.get(1);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals("Paracetamol 250 mg oral", ((MedicationSimpleDto)vmp2.getData()).getName());

    assertEquals(2L, vmp1.getChildren().size());
    assertEquals("Daleron 500 mg oral", ((MedicationSimpleDto)vmp1.getChildren().get(0).getData()).getName());
    assertEquals("Lekadol 500 mg oral", ((MedicationSimpleDto)vmp1.getChildren().get(1).getData()).getName());

    assertEquals(2L, vmp2.getChildren().size());
    assertEquals("Lekadol 250 mg oral", ((MedicationSimpleDto)vmp2.getChildren().get(0).getData()).getName());
    assertEquals("Daleron 250 mg oral", ((MedicationSimpleDto)vmp2.getChildren().get(1).getData()).getName());
  }

  @Test
  public void testFindSimilarMedicationsNoSimilar()
  {
    Mockito
        .when(medicationsDao.findSimilarMedicationsIds(anyLong(), anyListOf(Long.class), any()))
        .thenReturn(Collections.emptySet());

    final List<TreeNodeData> searchResult =
        medicationsFinder.findSimilarMedications(11L, Collections.singletonList(10L), new DateTime());

    assertTrue(searchResult.isEmpty());
  }

  @Test
  public void testFindSimilarMedicationsSimilarNotInValueHolder()
  {
    Mockito
        .when(medicationsDao.findSimilarMedicationsIds(anyLong(), anyListOf(Long.class), any()))
        .thenReturn(Collections.singleton(555L));

    final List<TreeNodeData> searchResult =
        medicationsFinder.findSimilarMedications(11L, Collections.singletonList(10L), new DateTime());

    assertTrue(searchResult.isEmpty());
  }

  @Test
  public void testFindMedicationProductsProductBasedMedication()
  {
    Mockito
        .when(medicationsDao.isProductBasedMedication(anyLong()))
        .thenReturn(true);

    Mockito
        .when(medicationsDao.findSimilarMedicationsIds(anyLong(), anyListOf(Long.class), any()))
        .thenReturn(Sets.newHashSet(111L, 112L));

    final List<MedicationDto> searchResult =
        medicationsFinder.findMedicationProducts(111L, Collections.singletonList(10L), new DateTime());

    assertEquals(2L, searchResult.size());
    assertEquals("Daleron 500 mg oral", searchResult.get(0).getName());
    assertEquals("Lekadol 500 mg oral", searchResult.get(1).getName());
  }

  @Test
  public void testFindMedicationProductsNonProductBasedMedication()
  {
    final MedicationDto medication1 = new MedicationDto();
    medication1.setName("Medication1");
    final MedicationDto medication2 = new MedicationDto();
    medication2.setName("Medication2");

    Mockito
        .when(medicationsDao.isProductBasedMedication(anyLong()))
        .thenReturn(false);

    Mockito
        .when(medicationsDao.getMedicationChildProducts(anyLong(), anyListOf(Long.class), any()))
        .thenReturn(Lists.newArrayList(medication1, medication2));

    final List<MedicationDto> searchResult =
        medicationsFinder.findMedicationProducts(111L, Collections.singletonList(10L), new DateTime());

    assertEquals(2L, searchResult.size());
    assertEquals("Medication1", searchResult.get(0).getName());
    assertEquals("Medication2", searchResult.get(1).getName());
  }

  private Map<Long, MedicationHolderDto> getMedicationsMap()
  {
    final Map<Long, MedicationHolderDto> medicationsMap = new LinkedHashMap<>();
    medicationsMap.put(
        1L,
        getMedicationHolderDto(
            1L, 9L, null, null, "Paracetamol", MedicationLevelEnum.VTM, true, true, true, true));
    medicationsMap.put(
        11L,
        getMedicationHolderDto(
            11L, 9L, 91L, null, "Paracetamol 500 mg oral", MedicationLevelEnum.VMP, true, true, true, true));
    final MedicationHolderDto lekadol500 = getMedicationHolderDto(
        111L, 9L, 91L, 911L, "Lekadol 500 mg oral", MedicationLevelEnum.AMP, true, false, true, false);
    lekadol500.getFormularyCareProviders().add("cp1");
    medicationsMap.put(111L, lekadol500);
    medicationsMap.put(
        112L,
        getMedicationHolderDto(
            112L, 9L, 91L, 912L, "Daleron 500 mg oral", MedicationLevelEnum.AMP, false, true, true, true));
    medicationsMap.put(
        12L,
        getMedicationHolderDto(
            12L, 9L, 92L, null, "Paracetamol 250 mg oral", MedicationLevelEnum.VMP, true, true, false, false));
    medicationsMap.put(
        121L,
        getMedicationHolderDto(
            121L, 9L, 92L, 921L, "Lekadol 250 mg oral", MedicationLevelEnum.AMP, true, false, false, false));
    medicationsMap.put(
        122L,
        getMedicationHolderDto(
            122L, 9L, 92L, 922L, "Daleron 250 mg oral", MedicationLevelEnum.AMP, false, true, false, false));

    return medicationsMap;
  }

  private MedicationHolderDto getMedicationHolderDto(
      final long id,
      final Long vtmId,
      final Long vmpId,
      final Long ampId,
      final String paracetamol,
      final MedicationLevelEnum vtm,
      final boolean inpatientMedication,
      final boolean outpatientMedication,
      final boolean formulary,
      final boolean mentalHealth)
  {
    final MedicationHolderDto holder1 = new MedicationHolderDto();
    holder1.setId(id);
    holder1.setVtmId(vtmId);
    holder1.setVmpId(vmpId);
    holder1.setAmpId(ampId);
    holder1.setName(paracetamol);
    holder1.setMedicationLevel(vtm);
    holder1.setInpatientMedication(inpatientMedication);
    holder1.setOutpatientMedication(outpatientMedication);
    holder1.setFormulary(formulary);
    holder1.setMentalHealthDrug(mentalHealth);
    return holder1;
  }

  private List<TreeNodeData> getMedicationTree()
  {
    final List<TreeNodeData> medications = new ArrayList<>();

    final TreeNodeData dto1 = new TreeNodeData();
    dto1.setTitle("Paracetamol");
    dto1.setKey("1");
    dto1.setData(new MedicationSimpleDto());
    medications.add(dto1);

    final TreeNodeData dto11 = new TreeNodeData();
    dto11.setTitle("Paracetamol 500 mg oral");
    dto11.setKey("11");
    dto11.setData(new MedicationSimpleDto());
    dto1.getChildren().add(dto11);

    final TreeNodeData dto111 = new TreeNodeData();
    dto111.setTitle("Lekadol 500 mg oral");
    dto111.setKey("111");
    dto111.setData(new MedicationSimpleDto());
    dto11.getChildren().add(dto111);

    final TreeNodeData dto112 = new TreeNodeData();
    dto112.setTitle("Daleron 500 mg oral");
    dto112.setKey("112");
    dto112.setData(new MedicationSimpleDto());
    dto11.getChildren().add(dto112);

    final TreeNodeData dto12 = new TreeNodeData();
    dto12.setTitle("Paracetamol 250 mg oral");
    dto12.setKey("12");
    dto12.setData(new MedicationSimpleDto());
    dto1.getChildren().add(dto12);

    final TreeNodeData dto121 = new TreeNodeData();
    dto121.setTitle("Lekadol 250 mg oral");
    dto121.setKey("121");
    dto121.setData(new MedicationSimpleDto());
    dto12.getChildren().add(dto121);

    final TreeNodeData dto122 = new TreeNodeData();
    dto122.setTitle("Daleron 250 mg oral");
    dto122.setKey("122");
    dto122.setData(new MedicationSimpleDto());
    dto12.getChildren().add(dto122);

    return medications;
  }
}
