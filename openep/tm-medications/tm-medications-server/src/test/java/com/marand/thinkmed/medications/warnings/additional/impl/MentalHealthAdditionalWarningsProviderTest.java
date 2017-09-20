package com.marand.thinkmed.medications.warnings.additional.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthAllowedMedicationsDo;
import com.marand.thinkmed.medications.mentalhealth.MentalHealthFormProvider;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.process.service.ProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("TooBroadScope")
@RunWith(MockitoJUnitRunner.class)
public class MentalHealthAdditionalWarningsProviderTest
{
  @InjectMocks
  private MentalHealthAdditionalWarningsProvider mentalHealthAdditionalWarningsProvider = new MentalHealthAdditionalWarningsProvider();

  @InjectMocks
  private MentalHealthWarningsHandlerImpl mentalHealthWarningsHandler= new MentalHealthWarningsHandlerImpl();

  @Mock
  private MedicationsBo medicationsBo;

  @Mock
  private MentalHealthFormProvider mentalHealthFormProvider;

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private ProcessService processService;

  @Before
  public void mock()
  {
    mentalHealthAdditionalWarningsProvider.setMentalHealthWarningsHandler(mentalHealthWarningsHandler);
    Mockito.when(medicationsBo.isMentalHealthMedication(ArgumentMatchers.anyLong())).thenReturn(true);
  }

  @Test
  public void testGetConflictMedicationsFromSimple()
  {
    final long medication1Id = 2L;
    final String medication1Name = "medication2";

    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    final MedicationDto medication = new MedicationDto();
    therapy.setMedication(medication);
    medication.setId(medication1Id);
    medication.setName(medication1Name);

    final List<MedicationRouteDto> routes = new ArrayList<>();
    therapy.setRoutes(routes);

    final MedicationRouteDto route1 = new MedicationRouteDto();
    route1.setId(1L);
    route1.setName("route1");
    routes.add(route1);

    final SetMultimap<Long, Long> medicationsWithRoutes = HashMultimap.create();
    medicationsWithRoutes.put(1L, 3L);

    final List<NamedExternalDto> conflictMedicationIds = mentalHealthAdditionalWarningsProvider.getConflictMedications(
        therapy,
        new MentalHealthAllowedMedicationsDo(medicationsWithRoutes, Collections.emptySet()));

    Assert.assertTrue(conflictMedicationIds.contains(new NamedExternalDto(String.valueOf(medication1Id), medication1Name)));
  }

  @Test
  public void testGetConflictMedicationsFromSimple2()
  {
    final long medication1Id = 1L;
    final String medication1Name = "medication1";

    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    final MedicationDto medication = new MedicationDto();
    therapy.setMedication(medication);
    medication.setId(medication1Id);
    medication.setName(medication1Name);

    final List<MedicationRouteDto> routes = new ArrayList<>();
    therapy.setRoutes(routes);

    final MedicationRouteDto route1 = new MedicationRouteDto();
    route1.setId(1L);
    route1.setName("route1");
    routes.add(route1);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);
    route2.setName("route2");
    routes.add(route2);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);
    route3.setName("route3");
    routes.add(route3);

    final SetMultimap<Long, Long> medicationsWithRoutes = HashMultimap.create();
    medicationsWithRoutes.put(1L, 3L);

    final List<NamedExternalDto> conflictMedicationIds = mentalHealthAdditionalWarningsProvider.getConflictMedications(
        therapy,
        new MentalHealthAllowedMedicationsDo(medicationsWithRoutes, Collections.emptySet()));

    Assert.assertTrue(conflictMedicationIds.isEmpty());
  }

  @Test
  public void testGetConflictMedicationsFromComplexTherapy()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();

    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(1L);
    medication1.setName("medication1");
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(medication1);
    therapy.getIngredientsList().add(ingredient1);

    final MedicationDto medication2 = new MedicationDto();
    medication2.setId(2L);
    medication2.setName("medication2");
    therapy.getMedications().add(medication2);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(medication2);
    therapy.getIngredientsList().add(ingredient2);

    final MedicationDto medication3 = new MedicationDto();
    medication3.setId(3L);
    medication3.setName("medication3");
    therapy.getMedications().add(medication3);
    final InfusionIngredientDto ingredient3 = new InfusionIngredientDto();
    ingredient3.setMedication(medication3);
    therapy.getIngredientsList().add(ingredient3);

    final MedicationDto medication4 = new MedicationDto();
    medication4.setId(4L);
    medication4.setName("medication4");
    therapy.getMedications().add(medication4);
    final InfusionIngredientDto ingredient4 = new InfusionIngredientDto();
    ingredient4.setMedication(medication4);
    therapy.getIngredientsList().add(ingredient4);

    final List<MedicationRouteDto> routes = new ArrayList<>();
    therapy.setRoutes(routes);

    final MedicationRouteDto route1 = new MedicationRouteDto();
    route1.setId(1L);
    route1.setName("route1");
    routes.add(route1);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);
    route2.setName("route2");
    routes.add(route2);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);
    route3.setName("route3");
    routes.add(route3);

    final Set<Long> medicationIds = new HashSet<>();
    medicationIds.add(1L);
    medicationIds.add(2L);
    medicationIds.add(4L);

    final List<NamedExternalDto> conflictMedicationIds = mentalHealthAdditionalWarningsProvider.getConflictMedications(
        therapy,
        new MentalHealthAllowedMedicationsDo(HashMultimap.create(), medicationIds));

    Assert.assertEquals(1, conflictMedicationIds.size());
    Assert.assertTrue(conflictMedicationIds.contains(new NamedExternalDto("3", "medication3")));
  }

  @Test
  public void testGetConflictMedicationsFromComplexTherapyAllOk()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();

    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(1L);
    medication1.setName("medication1");
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(medication1);
    therapy.getIngredientsList().add(ingredient1);

    final MedicationDto medication2 = new MedicationDto();
    medication2.setId(2L);
    medication2.setName("medication2");
    therapy.getMedications().add(medication2);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(medication2);
    therapy.getIngredientsList().add(ingredient2);

    final MedicationDto medication3 = new MedicationDto();
    medication3.setId(3L);
    medication3.setName("medication3");
    therapy.getMedications().add(medication3);
    final InfusionIngredientDto ingredient3 = new InfusionIngredientDto();
    ingredient3.setMedication(medication3);
    therapy.getIngredientsList().add(ingredient3);

    final MedicationDto medication4 = new MedicationDto();
    medication4.setId(4L);
    medication4.setName("medication4");
    therapy.getMedications().add(medication4);
    final InfusionIngredientDto ingredient4 = new InfusionIngredientDto();
    ingredient4.setMedication(medication4);
    therapy.getIngredientsList().add(ingredient4);

    final List<MedicationRouteDto> routes = new ArrayList<>();
    therapy.setRoutes(routes);

    final MedicationRouteDto route1 = new MedicationRouteDto();
    route1.setId(1L);
    route1.setName("route1");
    routes.add(route1);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);
    route2.setName("route2");
    routes.add(route2);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);
    route3.setName("route3");
    routes.add(route3);

    final Set<Long> medicationIds = new HashSet<>();
    medicationIds.add(1L);
    medicationIds.add(2L);
    medicationIds.add(3L);
    medicationIds.add(4L);

    final List<NamedExternalDto> conflictMedicationIds = mentalHealthAdditionalWarningsProvider.getConflictMedications(
        therapy,
        new MentalHealthAllowedMedicationsDo(HashMultimap.create(), medicationIds));

    Assert.assertTrue(conflictMedicationIds.isEmpty());
  }

  @Test
  public void testGetConflictMedicationsFromComplexTherapy1()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();

    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(1L);
    medication1.setName("medication1");
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(medication1);
    therapy.getIngredientsList().add(ingredient1);

    final MedicationDto medication2 = new MedicationDto();
    medication2.setId(2L);
    medication2.setName("medication2");
    therapy.getMedications().add(medication2);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(medication2);
    therapy.getIngredientsList().add(ingredient2);

    final MedicationDto medication3 = new MedicationDto();
    medication3.setId(3L);
    medication3.setName("medication3");
    therapy.getMedications().add(medication3);
    final InfusionIngredientDto ingredient3 = new InfusionIngredientDto();
    ingredient3.setMedication(medication3);
    therapy.getIngredientsList().add(ingredient3);

    final MedicationDto medication4 = new MedicationDto();
    medication4.setId(4L);
    medication4.setName("medication4");
    therapy.getMedications().add(medication4);
    final InfusionIngredientDto ingredient4 = new InfusionIngredientDto();
    ingredient4.setMedication(medication4);
    therapy.getIngredientsList().add(ingredient4);

    final List<MedicationRouteDto> routes = new ArrayList<>();
    therapy.setRoutes(routes);

    final MedicationRouteDto route1 = new MedicationRouteDto();
    route1.setId(1L);
    route1.setName("route1");
    routes.add(route1);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);
    route2.setName("route2");
    routes.add(route2);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);
    route3.setName("route3");
    routes.add(route3);

    final Set<Long> medicationIds = new HashSet<>();
    medicationIds.add(1L);

    final SetMultimap<Long, Long> medicationsWithRoutes = HashMultimap.create();
    medicationsWithRoutes.put(3L, 1L);
    medicationsWithRoutes.put(4L, 4L);

    final List<NamedExternalDto> conflictMedicationIds = mentalHealthAdditionalWarningsProvider.getConflictMedications(
        therapy,
        new MentalHealthAllowedMedicationsDo(medicationsWithRoutes, medicationIds));

    Assert.assertEquals(2, conflictMedicationIds.size());
    Assert.assertTrue(conflictMedicationIds.contains(new NamedExternalDto("2", "medication1")));
    Assert.assertTrue(conflictMedicationIds.contains(new NamedExternalDto("4", "medication4")));
  }

  @Test
  public void testGetConflictMedicationsFromComplexTherapy2()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();

    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(1L);
    medication1.setName("medication1");
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(medication1);
    therapy.getIngredientsList().add(ingredient1);

    final MedicationDto medication2 = new MedicationDto();
    medication2.setId(2L);
    medication2.setName("medication2");
    therapy.getMedications().add(medication2);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(medication2);
    therapy.getIngredientsList().add(ingredient2);

    final List<MedicationRouteDto> routes = new ArrayList<>();
    therapy.setRoutes(routes);

    final MedicationRouteDto route1 = new MedicationRouteDto();
    route1.setId(1L);
    route1.setName("route1");
    routes.add(route1);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);
    route2.setName("route2");
    routes.add(route2);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);
    route3.setName("route3");
    routes.add(route3);

    final SetMultimap<Long, Long> medicationsWithRoutes = HashMultimap.create();
    medicationsWithRoutes.put(1L, 4L);
    medicationsWithRoutes.put(1L, 5L);
    medicationsWithRoutes.put(1L, 6L);

    medicationsWithRoutes.put(2L, 4L);

    medicationsWithRoutes.put(3L, 1L);
    medicationsWithRoutes.put(3L, 2L);

    medicationsWithRoutes.put(4L, 3L);
    medicationsWithRoutes.put(4L, 4L);

    final List<NamedExternalDto> conflictMedicationIds = mentalHealthAdditionalWarningsProvider.getConflictMedications(
        therapy,
        new MentalHealthAllowedMedicationsDo(medicationsWithRoutes, Collections.emptySet()));

    Assert.assertEquals(2, conflictMedicationIds.size());
    Assert.assertTrue(conflictMedicationIds.contains(new NamedExternalDto("1", "medication1")));
    Assert.assertTrue(conflictMedicationIds.contains(new NamedExternalDto("2", "medication2")));
  }

  @Test
  public void testGetConflictMedicationsFromComplexTherapy3()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();

    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(1L);
    medication1.setName("medication1");
    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    ingredient1.setMedication(medication1);
    therapy.getIngredientsList().add(ingredient1);

    final MedicationDto medication2 = new MedicationDto();
    medication2.setId(2L);
    medication2.setName("medication2");
    therapy.getMedications().add(medication2);
    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    ingredient2.setMedication(medication2);
    therapy.getIngredientsList().add(ingredient2);

    final MedicationDto medication3 = new MedicationDto();
    medication3.setId(3L);
    medication3.setName("medication3");
    therapy.getMedications().add(medication3);
    final InfusionIngredientDto ingredient3 = new InfusionIngredientDto();
    ingredient3.setMedication(medication3);
    therapy.getIngredientsList().add(ingredient3);

    final MedicationDto medication4 = new MedicationDto();
    medication4.setId(4L);
    medication4.setName("medication4");
    therapy.getMedications().add(medication4);
    final InfusionIngredientDto ingredient4 = new InfusionIngredientDto();
    ingredient4.setMedication(medication4);
    therapy.getIngredientsList().add(ingredient4);

    final List<MedicationRouteDto> routes = new ArrayList<>();
    therapy.setRoutes(routes);

    final MedicationRouteDto route1 = new MedicationRouteDto();
    route1.setId(1L);
    route1.setName("route1");
    routes.add(route1);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);
    route2.setName("route2");
    routes.add(route2);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);
    route3.setName("route3");
    routes.add(route3);

    final SetMultimap<Long, Long> medicationsWithRoutes = HashMultimap.create();
    medicationsWithRoutes.put(1L, 4L);
    medicationsWithRoutes.put(1L, 5L);
    medicationsWithRoutes.put(1L, 6L);

    medicationsWithRoutes.put(2L, 4L);

    medicationsWithRoutes.put(3L, 1L);
    medicationsWithRoutes.put(3L, 2L);

    medicationsWithRoutes.put(4L, 3L);
    medicationsWithRoutes.put(4L, 4L);

    final List<NamedExternalDto> conflictMedicationIds = mentalHealthAdditionalWarningsProvider.getConflictMedications(
        therapy,
        new MentalHealthAllowedMedicationsDo(medicationsWithRoutes, Collections.emptySet()));

    Assert.assertEquals(2, conflictMedicationIds.size());
    Assert.assertTrue(conflictMedicationIds.contains(new NamedExternalDto("1", "medication1")));
    Assert.assertTrue(conflictMedicationIds.contains(new NamedExternalDto("2", "medication2")));
  }
}
