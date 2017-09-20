package com.marand.thinkmed.medications.overview.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Pair;
import com.marand.maf.core.daterule.service.MafDateRuleService;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.maf.core.time.DayType;
import com.marand.maf.core.time.Intervals;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.IngredientsAndFormCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkehr.tagging.dto.TaggedObjectDto;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.TherapyTagEnum;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.administration.impl.AdministrationFromEhrConverterImpl;
import com.marand.thinkmed.medications.administration.impl.AdministrationHandlerImpl;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskConverterImpl;
import com.marand.thinkmed.medications.administration.impl.AdministrationUtilsImpl;
import com.marand.thinkmed.medications.business.impl.DefaultMedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.mapper.MedicationHolderDtoMapper;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.overview.ContinuousInfusionTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsDelegator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Instruction;
import org.openehr.jaxb.rm.IsmTransition;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.openehr.jaxb.rm.UidBasedId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author Nejc Korasa
 */
@SuppressWarnings("TooBroadScope")
@RunWith(MockitoJUnitRunner.class)
public class OverviewContentProviderTest
{

  @InjectMocks
  private OverviewContentProviderImpl overviewContentProvider = new OverviewContentProviderImpl();

  @InjectMocks
  private DefaultMedicationsBo medicationsBo = new DefaultMedicationsBo();

  @Spy
  private MedicationHolderDtoMapper medicationHolderDtoMapper = new MedicationHolderDtoMapper();

  @Mock
  private ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder;

  @Mock
  private ValueHolder<Map<Long, MedicationRouteDto>> medicationRoutesValueHolder;

  @Spy
  private AdministrationFromEhrConverterImpl administrationDtoConverter = new AdministrationFromEhrConverterImpl();

  @Mock
  private MafDateRuleService mafDateRuleService;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private TaggingOpenEhrDao taggingOpenEhrDao;

  @Spy
  private AdministrationUtils administrationUtils = new AdministrationUtilsImpl();

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Spy
  private AdministrationHandler administrationHandler = new AdministrationHandlerImpl();

  @Mock
  private AdditionalWarningsDelegator additionalWarningsDelegator;

  @Spy
  private TherapyDisplayProvider therapyDisplayProvider;

  @Spy
  private AdministrationTaskConverter administrationTaskConverter = new AdministrationTaskConverterImpl();

  @Before
  public void setUp()
  {
    administrationDtoConverter.setAdministrationUtils(administrationUtils);
    medicationHolderDtoMapper.setMarkNonFormularyMedication(true);
    overviewContentProvider.setDoctorReviewEnabled(true);
    overviewContentProvider.setMedicationsBo(medicationsBo);
    //mocks
    final Map<Long, MedicationHolderDto> medicationHolderMap = new HashMap<>();
    final MedicationHolderDto medicationDto1 = new MedicationHolderDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");
    medicationHolderMap.put(1L, medicationDto1);

    final MedicationHolderDto medicationDto2 = new MedicationHolderDto();
    medicationDto2.setId(2L);
    medicationDto2.setName("Primotren 20x500mg");
    medicationHolderMap.put(2L, medicationDto2);

    Mockito
        .when(medicationsValueHolder.getValue())
        .thenReturn(medicationHolderMap);

    Mockito
        .when(medicationRoutesValueHolder.getValue())
        .thenReturn(new HashMap<>());

    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2016, 7, 4, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2016, 7, 5, 0, 0), DayType.WORKING_DAY)).thenReturn(false);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2016, 7, 6, 0, 0), DayType.WORKING_DAY)).thenReturn(false);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2016, 7, 7, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
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
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstruction");
    //
    final MedicationInstructionInstruction.OrderActivity orderActivity =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol 20x500mg", 1L, 500.0, "mg", null, null, null, null, 1L, "po", null, null);
    instruction.getOrder().add(orderActivity);
    final MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster medicationTiming =
        MedicationsTestUtils.buildMedicationTimingCluster(
            4L, null, null, null, new DateTime(2014, 2, 21, 8, 10, 0), null, null);
    orderActivity.setMedicationTiming(medicationTiming);
    final IngredientsAndFormCluster ingredientsAndForm = new IngredientsAndFormCluster();
    orderActivity.setIngredientsAndForm(ingredientsAndForm);
    ingredientsAndForm.setForm(DataValueUtils.getLocalCodedText("tbl", "tbl"));
    composition.getMedicationDetail().getMedicationInstruction().add(instruction);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair = Pair.of(composition, instruction);
    therapies.add(therapyPair);
    final String therapyId = TherapyIdUtils.createTherapyId(therapyPair.getFirst(), therapyPair.getSecond());

    administrations.put(therapyId, new ArrayList<>());
    final LocatableRef instructionLocatableRef =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair.getFirst());
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

    final List<AdministrationTaskDto> tasks = new ArrayList<>();
    tasks.add(buildAdministrationTaskDto("t0", "adm0", therapyId, new DateTime(2014, 2, 21, 8, 0, 0), 500.0, "mg"));
    tasks.add(buildAdministrationTaskDto("t1", "adm1", therapyId, new DateTime(2014, 2, 21, 11, 0, 0), 500.0, "mg"));
    tasks.add(buildAdministrationTaskDto("t2", "adm2", therapyId, new DateTime(2014, 2, 21, 14, 0, 0), 500.0, "mg"));
    tasks.add(buildAdministrationTaskDto("t3", "adm3", therapyId, new DateTime(2014, 2, 21, 16, 0, 0), 500.0, "mg"));
    tasks.add(buildAdministrationTaskDto("t4", null, therapyId, new DateTime(2014, 2, 21, 18, 0, 0), 500.0, "mg")); //late
    tasks.add(buildAdministrationTaskDto("t5", null, therapyId, new DateTime(2014, 2, 21, 18, 30, 0), 500.0, "mg")); //due
    tasks.add(buildAdministrationTaskDto("t6", null, therapyId, new DateTime(2014, 2, 21, 19, 0, 0), 500.0, "mg")); //due
    tasks.add(buildAdministrationTaskDto("t7", null, therapyId, new DateTime(2014, 2, 21, 21, 0, 0), 500.0, "mg")); //planned

    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionStart("1", "uid1"))
        .thenReturn(new DateTime(2014, 2, 20, 12, 0, 0));

    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");

    Mockito
        .when(medicationRoutesValueHolder.getValue())
        .thenReturn(new HashMap<>());

    final TaggedObjectDto<Object> taggedObjectDto = new TaggedObjectDto<>();
    taggedObjectDto.setComposition(composition);
    taggedObjectDto.setObject(instruction);

    final Set<TaggedObjectDto<Object>> tags = new HashSet<>();
    tags.add(taggedObjectDto);

    Mockito
        .when(taggingOpenEhrDao.findObjectCompositionPairs(any(), any()))
        .thenReturn(tags);

    final MedicationsCentralCaseDto centralCase = new MedicationsCentralCaseDto();
    centralCase.setCentralCaseId("1");
    final PatientDataForMedicationsDto patientData = new PatientDataForMedicationsDto(
        null,
        null,
        null,
        Gender.FEMALE,
        null,
        null,
        centralCase);

    final List<TherapyRowDto> therapyTimelineRows =
        overviewContentProvider.buildTherapyRows(
            "1",
            therapies,
            administrationDtoConverter.convertToAdministrationDtos(administrations, therapies),
            tasks,
            TherapySortTypeEnum.DESCRIPTION_ASC,
            false,
            Collections.emptyList(),
            patientData,
            Intervals.infiniteFrom(new DateTime(2014, 2, 10, 0, 0, 0)),
            roundsIntervalDto,
            null,
            new DateTime(2014, 2, 21, 18, 40, 0));

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
    assertFalse(administration2.isDifferentFromOrder());

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
    assertEquals(
        500.0, ((ConstantSimpleTherapyDto)therapyTimelineRows.get(0).getTherapy()).getDoseElement().getQuantity(), 0);
    assertEquals("Lekadol 20x500mg", ((SimpleTherapyDto)therapyTimelineRows.get(0).getTherapy()).getMedication().getName());

    assertEquals(new DateTime(2014, 2, 20, 12, 0, 0), therapyTimelineRows.get(0).getOriginalTherapyStart());
    assertEquals(TherapyTagEnum.PRESCRIPTION, therapyTimelineRows.get(0).getTherapy().getTags().get(0));
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

    overviewContentProvider.fillPrescriptionTagsForTherapies(therapyDtos, taggedObjectDtos);
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

    overviewContentProvider.fillPrescriptionTagsForTherapies(therapyDtos, taggedObjectDtos);
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

    overviewContentProvider.fillPrescriptionTagsForTherapies(therapyDtos, taggedObjectDtos);
    assertEquals(0L, therapyDtos.get(0).getTags().size());
  }

  @Test
  public void testBuildTherapyTimeline2()
  {
    //mocks
    Mockito
        .when(medicationRoutesValueHolder.getValue())
        .thenReturn(new HashMap<>());

    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies = new ArrayList<>();
    final Map<String, List<MedicationAdministrationComposition>> administrations = new LinkedHashMap<>();
    final List<AdministrationTaskDto> tasks = new ArrayList<>();

    //original therapy
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid1::1", new DateTime(2014, 2, 20, 12, 0, 0), "1");
    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstruction");
    final MedicationInstructionInstruction.OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 500.0, "mg", null, null, null, null, 2L, "IV", null, null);
    instruction1.getOrder().add(orderActivity1);
    final MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster medicationTiming1 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            4L, null, null, null, new DateTime(2014, 2, 20, 12, 10, 0), null, null);
    orderActivity1.setMedicationTiming(medicationTiming1);
    final IngredientsAndFormCluster ingredientsAndForm1 = new IngredientsAndFormCluster();
    orderActivity1.setIngredientsAndForm(ingredientsAndForm1);
    ingredientsAndForm1.setForm(DataValueUtils.getLocalCodedText("1", "po"));
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair1 = Pair.of(
        composition1,
        instruction1);
    therapies.add(therapyPair1);

    final String therapyId1 = TherapyIdUtils.createTherapyId(therapyPair1.getFirst(), therapyPair1.getSecond());
    administrations.put(therapyId1, new ArrayList<>());
    final LocatableRef instructionLocatableRef1 =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair1.getFirst());
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
    tasks.add(buildAdministrationTaskDto("t0", "adm0", therapyId1, new DateTime(2014, 2, 21, 8, 0, 0), 500.0, "mg"));

    //modified therapy
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid2::1", new DateTime(2014, 2, 21, 12, 0, 0), "1");
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstruction");
    final MedicationInstructionInstruction.OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Lekadol", 1L, 1000.0, "mg", null, null, null, null, 2L, "IV", null, null);
    instruction2.getOrder().add(orderActivity2);
    final MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster medicationTiming2 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            4L, null, null, null, new DateTime(2014, 2, 21, 12, 10, 0), null, null);
    orderActivity2.setMedicationTiming(medicationTiming2);
    final IngredientsAndFormCluster ingredientsAndForm2 = new IngredientsAndFormCluster();
    orderActivity2.setIngredientsAndForm(ingredientsAndForm2);
    ingredientsAndForm2.setForm(DataValueUtils.getLocalCodedText("po", "po"));
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);
    instruction2.getLinks().add(
        OpenEhrRefUtils.getLinkToTdoTarget("update", EhrLinkType.UPDATE.getName(), composition1, instruction1));
    instruction2.getLinks().add(
        OpenEhrRefUtils.getLinkToTdoTarget("origin", EhrLinkType.ORIGIN.getName(), composition1, instruction1));
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair2 = Pair.of(
        composition2,
        instruction2);
    therapies.add(therapyPair2);

    final String therapyId2 = TherapyIdUtils.createTherapyId(therapyPair2.getFirst(), therapyPair2.getSecond());
    administrations.put(therapyId2, new ArrayList<>());
    final LocatableRef instructionLocatableRef2 =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair1.getFirst());
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
    tasks.add(buildAdministrationTaskDto("t1", "adm1", therapyId2, new DateTime(2014, 2, 21, 13, 0, 0), 1000.0, "mg"));

    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionStart("1", "uid1"))
        .thenReturn(new DateTime(2014, 2, 20, 12, 0, 0));

    final MedicationsCentralCaseDto centralCase = new MedicationsCentralCaseDto();
    centralCase.setCentralCaseId("1");
    final PatientDataForMedicationsDto patientData = new PatientDataForMedicationsDto(
        null,
        null,
        null,
        Gender.FEMALE,
        null,
        null,
        centralCase);

    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final List<TherapyRowDto> therapyTimelineRows =
        overviewContentProvider.buildTherapyRows(
            "1",
            therapies,
            administrationDtoConverter.convertToAdministrationDtos(administrations, therapies),
            tasks,
            TherapySortTypeEnum.DESCRIPTION_ASC,
            false,
            Collections.emptyList(),
            patientData,
            Intervals.INFINITE,
            roundsIntervalDto,
            null,
            new DateTime(2014, 2, 21, 18, 40, 0));

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
    assertEquals(new DateTime(2014, 2, 20, 12, 0, 0), therapyTimelineRows.get(0).getOriginalTherapyStart());
  }

  @Test
  public void testBuildTherapyTimeline3() //continuous infusion
  {
    //mocks
    final MedicationDto medicationDto1 = new MedicationDto();
    medicationDto1.setId(1L);
    medicationDto1.setName("Lekadol 20x500mg");

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

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies = new ArrayList<>();
    final Map<String, List<MedicationAdministrationComposition>> administrations = new LinkedHashMap<>();
    final List<AdministrationTaskDto> tasks = new ArrayList<>();

    //original therapy
    final MedicationOrderComposition composition1 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid1::1", new DateTime(2014, 2, 20, 12, 0, 0), "1");
    final MedicationInstructionInstruction instruction1 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstruction");
    final MedicationInstructionInstruction.OrderActivity orderActivity1 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Dopamin", 1L, null, null, null, null, null, null, 2L, "IV", null, null);
    instruction1.getOrder().add(orderActivity1);
    final MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster medicationTiming1 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 20, 12, 10, 0), null, null);
    orderActivity1.setMedicationTiming(medicationTiming1);
    final AdministrationDetailsCluster administrationDetails = new AdministrationDetailsCluster();
    orderActivity1.setAdministrationDetails(administrationDetails);
    administrationDetails.setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION))
    );
    final AdministrationDetailsCluster.InfusionAdministrationDetailsCluster infusionDetails = new AdministrationDetailsCluster.InfusionAdministrationDetailsCluster();
    administrationDetails.getInfusionAdministrationDetails().add(infusionDetails);
    infusionDetails.setDoseAdministrationRate(DataValueUtils.getQuantity(20.0, "mL/h"));
    administrationDetails.setRoute(Collections.singletonList(DataValueUtils.getLocalCodedText("IV", "IV")));

    final IngredientsAndFormCluster ingredientsAndForm1 = new IngredientsAndFormCluster();
    orderActivity1.setIngredientsAndForm(ingredientsAndForm1);
    ingredientsAndForm1.setForm(DataValueUtils.getLocalCodedText("fluid", "fluid"));
    composition1.getMedicationDetail().getMedicationInstruction().add(instruction1);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair1 = Pair.of(
        composition1,
        instruction1);
    therapies.add(therapyPair1);

    final String therapyId1 = TherapyIdUtils.createTherapyId(therapyPair1.getFirst(), therapyPair1.getSecond());
    administrations.put(therapyId1, new ArrayList<>());
    final LocatableRef instructionLocatableRef1 =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair1.getFirst());
    administrations.get(therapyId1).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm0",
            new DateTime(2014, 2, 20, 12, 0, 0),
            instructionLocatableRef1,
            "1",
            21.0,
            "mL/h",
            "comment123",
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    );

    //modified therapy
    final MedicationOrderComposition composition2 =
        MedicationsTestUtils.buildTestMedicationOrderComposition(
            "uid2::1", new DateTime(2014, 2, 21, 12, 0, 0), "1");
    final MedicationInstructionInstruction instruction2 =
        MedicationsTestUtils.buildTestMedicationInstruction("MedicationInstruction");
    final MedicationInstructionInstruction.OrderActivity orderActivity2 =
        MedicationsTestUtils.buildTestOrderActivity(
            "Dopamin", 1L, null, null, null, null, null, null, 2L, "IV", null, null);
    instruction2.getOrder().add(orderActivity2);
    final MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster medicationTiming2 =
        MedicationsTestUtils.buildMedicationTimingCluster(
            null, null, null, null, new DateTime(2014, 2, 20, 17, 30, 0), null, null);
    orderActivity2.setMedicationTiming(medicationTiming2);
    final AdministrationDetailsCluster administrationDetails2 = new AdministrationDetailsCluster();
    orderActivity2.setAdministrationDetails(administrationDetails2);
    administrationDetails2.setDeliveryMethod(
        DataValueUtils.getText(
            MedicationDeliveryMethodEnum.getFullString(MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION))
    );
    final AdministrationDetailsCluster.InfusionAdministrationDetailsCluster infusionDetails2 = new AdministrationDetailsCluster.InfusionAdministrationDetailsCluster();
    administrationDetails2.getInfusionAdministrationDetails().add(infusionDetails2);
    infusionDetails2.setDoseAdministrationRate(DataValueUtils.getQuantity(20.0, "mL/h"));
    administrationDetails2.setRoute(Collections.singletonList(DataValueUtils.getLocalCodedText("1", "IV")));

    final IngredientsAndFormCluster ingredientsAndForm2 = new IngredientsAndFormCluster();
    orderActivity2.setIngredientsAndForm(ingredientsAndForm2);
    ingredientsAndForm2.setForm(DataValueUtils.getLocalCodedText("fluid", "fluid"));
    composition2.getMedicationDetail().getMedicationInstruction().add(instruction2);
    instruction2.getLinks().add(
        OpenEhrRefUtils.getLinkToTdoTarget("update", EhrLinkType.UPDATE.getName(), composition1, instruction1));
    instruction2.getLinks().add(
        OpenEhrRefUtils.getLinkToTdoTarget("origin", EhrLinkType.ORIGIN.getName(), composition1, instruction1));

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyPair2 = Pair.of(
        composition2,
        instruction2);
    therapies.add(therapyPair2);

    final String therapyId2 = TherapyIdUtils.createTherapyId(therapyPair2.getFirst(), therapyPair2.getSecond());
    administrations.put(therapyId2, new ArrayList<>());
    final LocatableRef instructionLocatableRef2 =
        MedicationsEhrUtils.createInstructionLocatableRef(therapyPair2.getFirst());
    administrations.get(therapyId2).add(
        MedicationsTestUtils.buildMedicationAdministrationComposition(
            "adm1",
            new DateTime(2014, 2, 21, 8, 10, 0),
            instructionLocatableRef2,
            "1",
            25.0,
            "mL/h",
            null,
            MedicationActionEnum.ADMINISTER,
            AdministrationTypeEnum.START)
    );

    Mockito
        .when(medicationsOpenEhrDao.getTherapyInstructionStart("1", "uid1"))
        .thenReturn(new DateTime(2014, 2, 20, 12, 0, 0));

    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final MedicationsCentralCaseDto centralCase = new MedicationsCentralCaseDto();
    centralCase.setCentralCaseId("1");
    final PatientDataForMedicationsDto patientData = new PatientDataForMedicationsDto(
        null,
        null,
        null,
        Gender.FEMALE,
        null,
        null,
        centralCase);

    final List<TherapyRowDto> therapyTimelineRows =
        overviewContentProvider.buildTherapyRows(
            "1",
            therapies,
            administrationDtoConverter.convertToAdministrationDtos(administrations, therapies),
            tasks,
            TherapySortTypeEnum.DESCRIPTION_ASC,
            false,
            Collections.emptyList(),
            patientData,
            Intervals.INFINITE,
            roundsIntervalDto,
            new Locale("en"),
            new DateTime(2014, 2, 21, 18, 40, 0));

    assertEquals(1L, therapyTimelineRows.size());
    final TherapyRowDto timeline = therapyTimelineRows.get(0);
    assertEquals(2L, timeline.getAdministrations().size());

    final StartAdministrationDto administration0 = (StartAdministrationDto)timeline.getAdministrations().get(0);
    assertEquals("adm0", administration0.getAdministrationId());
    assertNull(administration0.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration0.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 20, 12, 0, 0), administration0.getAdministrationTime());
    assertEquals(21.0, administration0.getAdministeredDose().getNumerator(), 0);
    assertEquals("mL/h", administration0.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration0.isDifferentFromOrder());
    assertEquals("comment123", administration0.getComment());

    final StartAdministrationDto administration1 = (StartAdministrationDto)timeline.getAdministrations().get(1);
    assertEquals("adm1", administration1.getAdministrationId());
    assertNull(administration0.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration1.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 8, 10, 0), administration1.getAdministrationTime());
    assertEquals(25, administration1.getAdministeredDose().getNumerator(), 0);
    assertEquals("mL/h", administration1.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration1.isDifferentFromOrder());
    assertNull(administration1.getComment());

    assertEquals("uid2::1", timeline.getTherapy().getCompositionUid());
    assertTrue(timeline instanceof ContinuousInfusionTherapyRowDtoDto);
    assertEquals(25, ((ContinuousInfusionTherapyRowDtoDto)timeline).getCurrentInfusionRate(), 0);
    assertEquals(new DateTime(2014, 2, 20, 12, 0, 0), timeline.getOriginalTherapyStart());
  }

  @Test
  public void testRemoveOldCompletedTherapies()
  {
    final List<TherapyRowDto> rows = new ArrayList<>();
    final TherapyRowDto row = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setEnd(new DateTime(2013, 1, 1, 0, 0));
    row.setTherapy(therapy);
    rows.add(row);
    overviewContentProvider.removeOldCompletedTherapies(rows, new DateTime(2015, 6, 15, 12, 0, 0));
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testRemoveOldCompletedTherapiesCompletedOpenTasks()
  {
    final List<TherapyRowDto> rows = new ArrayList<>();
    final TherapyRowDto row = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    therapy.setEnd(new DateTime(2015, 6, 14, 12, 0, 0));

    final List<AdministrationDto> administrations = new ArrayList<>();

    final AdjustInfusionAdministrationDto admin1 = new AdjustInfusionAdministrationDto();
    admin1.setAdministrationStatus(AdministrationStatusEnum.PLANNED);
    admin1.setPlannedTime(new DateTime(2015, 6, 14, 8, 0, 0));
    administrations.add(admin1);

    final AdjustInfusionAdministrationDto admin2 = new AdjustInfusionAdministrationDto();
    admin2.setAdministrationStatus(AdministrationStatusEnum.DUE);
    admin2.setPlannedTime(new DateTime(2015, 6, 14, 9, 0, 0));
    administrations.add(admin2);

    final AdjustInfusionAdministrationDto admin3 = new AdjustInfusionAdministrationDto();
    admin3.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    admin3.setPlannedTime(new DateTime(2015, 6, 14, 10, 0, 0));
    administrations.add(admin3);

    final AdjustInfusionAdministrationDto admin4 = new AdjustInfusionAdministrationDto();
    admin4.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    admin4.setPlannedTime(new DateTime(2015, 6, 14, 12, 0, 0));
    administrations.add(admin4);

    row.setTherapy(therapy);
    row.setAdministrations(administrations);

    rows.add(row);

    overviewContentProvider.removeOldCompletedTherapies(rows, new DateTime(2015, 6, 15, 12, 0, 0));
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testRemoveOldCompletedTherapiesNotCompletedOpenTasks()
  {
    final List<TherapyRowDto> rows = new ArrayList<>();
    final TherapyRowDto row = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    therapy.setEnd(new DateTime(2015, 6, 17, 12, 0, 0));

    final List<AdministrationDto> administrations = new ArrayList<>();
    final AdjustInfusionAdministrationDto admin1 = new AdjustInfusionAdministrationDto();
    final AdjustInfusionAdministrationDto admin2 = new AdjustInfusionAdministrationDto();
    final AdjustInfusionAdministrationDto admin3 = new AdjustInfusionAdministrationDto();
    final AdjustInfusionAdministrationDto admin4 = new AdjustInfusionAdministrationDto();

    admin1.setAdministrationStatus(AdministrationStatusEnum.PLANNED);
    admin2.setAdministrationStatus(AdministrationStatusEnum.DUE);
    admin3.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    admin4.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);

    administrations.add(admin1);
    administrations.add(admin2);
    administrations.add(admin3);
    administrations.add(admin4);

    row.setTherapy(therapy);
    row.setAdministrations(administrations);

    rows.add(row);

    overviewContentProvider.removeOldCompletedTherapies(rows, new DateTime(2015, 6, 15, 12, 0, 0));
    assertTrue(!rows.isEmpty());
  }

  @Test
  public void testRemoveOldCompletedTherapiesCompletedOpenTasksInPast()
  {
    final List<TherapyRowDto> rows = new ArrayList<>();
    final TherapyRowDto row = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    therapy.setEnd(new DateTime(2015, 6, 14, 12, 0, 0));

    final List<AdministrationDto> administrations = new ArrayList<>();
    final AdjustInfusionAdministrationDto admin1 = new AdjustInfusionAdministrationDto();
    admin1.setAdministrationStatus(AdministrationStatusEnum.PLANNED);
    admin1.setPlannedTime(new DateTime(2015, 6, 7, 12, 0, 0));
    administrations.add(admin1);

    final AdjustInfusionAdministrationDto admin2 = new AdjustInfusionAdministrationDto();
    admin2.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    administrations.add(admin2);

    row.setTherapy(therapy);
    row.setAdministrations(administrations);

    rows.add(row);

    overviewContentProvider.removeOldCompletedTherapies(rows, new DateTime(2015, 6, 15, 12, 0, 0));
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testRemoveOldTherapies1exTherapy()
  {
    final List<TherapyRowDto> rows = new ArrayList<>();
    final TherapyRowDto row = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    therapy.setEnd(new DateTime(2015, 6, 7, 3, 0, 0));
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX));

    final List<AdministrationDto> administrations = new ArrayList<>();
    final StartAdministrationDto admin1 = new StartAdministrationDto();
    admin1.setAdministrationStatus(AdministrationStatusEnum.PLANNED);
    admin1.setPlannedTime(new DateTime(2015, 6, 7, 3, 0, 0));
    administrations.add(admin1);

    row.setTherapy(therapy);
    row.setAdministrations(administrations);

    rows.add(row);

    overviewContentProvider.removeOldCompletedTherapies(rows, new DateTime(2015, 6, 7, 12, 0, 0));
    assertTrue(rows.isEmpty());

    final List<TherapyRowDto> rows1exCompletedTask = new ArrayList<>();
    final TherapyRowDto row2 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy2 = new ConstantSimpleTherapyDto();

    therapy2.setEnd(new DateTime(2015, 6, 7, 3, 0, 0));
    therapy2.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX));

    final List<AdministrationDto> administrations2 = new ArrayList<>();
    final StartAdministrationDto admin2 = new StartAdministrationDto();
    admin2.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    admin2.setPlannedTime(new DateTime(2015, 6, 7, 3, 0, 0));
    admin2.setAdministrationTime(new DateTime(2015, 6, 7, 3, 0, 0));
    administrations2.add(admin2);

    row2.setTherapy(therapy2);
    row2.setAdministrations(administrations2);
    rows1exCompletedTask.add(row2);

    overviewContentProvider.removeOldCompletedTherapies(rows1exCompletedTask, new DateTime(2015, 6, 7, 12, 0, 0));
    assertEquals(1, rows1exCompletedTask.size());

    final List<TherapyRowDto> rows1exPlanned = new ArrayList<>();
    final TherapyRowDto row3 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy3 = new ConstantSimpleTherapyDto();

    therapy3.setEnd(new DateTime(2015, 6, 7, 13, 0, 0));
    therapy3.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX));

    final List<AdministrationDto> administrations3 = new ArrayList<>();
    final StartAdministrationDto admin3 = new StartAdministrationDto();
    admin3.setAdministrationStatus(AdministrationStatusEnum.PLANNED);
    admin3.setPlannedTime(new DateTime(2015, 6, 7, 13, 0, 0));
    administrations3.add(admin3);

    row3.setTherapy(therapy3);
    row3.setAdministrations(administrations3);
    rows1exPlanned.add(row3);

    overviewContentProvider.removeOldCompletedTherapies(rows1exPlanned, new DateTime(2015, 6, 7, 12, 0, 0));
    assertEquals(1, rows1exPlanned.size());

    final List<TherapyRowDto> rows1exCompletedLast8h = new ArrayList<>();
    final TherapyRowDto row4 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy4 = new ConstantSimpleTherapyDto();

    therapy4.setEnd(new DateTime(2015, 6, 7, 10, 0, 0));
    therapy4.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX));

    final List<AdministrationDto> administrations4 = new ArrayList<>();
    final StartAdministrationDto admin4 = new StartAdministrationDto();
    admin4.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    admin4.setPlannedTime(new DateTime(2015, 6, 7, 10, 0, 0));
    admin4.setAdministrationTime(new DateTime(2015, 6, 7, 10, 0, 0));
    administrations4.add(admin4);

    row4.setTherapy(therapy4);
    row4.setAdministrations(administrations4);
    rows1exCompletedLast8h.add(row4);

    overviewContentProvider.removeOldCompletedTherapies(rows1exCompletedLast8h, new DateTime(2015, 6, 7, 12, 0, 0));
    assertEquals(1, rows1exCompletedLast8h.size());

    final List<TherapyRowDto> rowsCompletedMoreThan24hAgo = new ArrayList<>();
    final TherapyRowDto row5 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy5 = new ConstantSimpleTherapyDto();

    therapy5.setEnd(new DateTime(2015, 6, 6, 11, 0, 0));
    therapy5.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX));

    final List<AdministrationDto> administrations5 = new ArrayList<>();
    final StartAdministrationDto admin5 = new StartAdministrationDto();
    admin5.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    admin5.setPlannedTime(new DateTime(2015, 6, 6, 11, 0, 0));
    admin5.setAdministrationTime(new DateTime(2015, 6, 6, 11, 0, 0));
    administrations5.add(admin5);

    row5.setTherapy(therapy5);
    row5.setAdministrations(administrations5);
    rowsCompletedMoreThan24hAgo.add(row5);

    overviewContentProvider.removeOldCompletedTherapies(rowsCompletedMoreThan24hAgo, new DateTime(2015, 6, 7, 12, 0, 0));
    assertTrue(rowsCompletedMoreThan24hAgo.isEmpty());
  }

  @Test
  public void testSortTherapies()
  {
    final List<TherapyRowDto> timelineRows = new ArrayList<>();

    final TherapyRowDto row = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setTherapyDescription("ZZAspirin");
    therapy.setLinkName("A1");
    therapy.setCreatedTimestamp(new DateTime(2015, 11, 5, 0, 0));
    row.setTherapy(therapy);
    timelineRows.add(row);

    final TherapyRowDto row2 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy2 = new ConstantComplexTherapyDto();
    therapy2.setTherapyDescription("Cspirin");
    therapy2.setLinkName("B1");
    therapy2.setCreatedTimestamp(new DateTime(2015, 11, 4, 0, 0));
    row2.setTherapy(therapy2);
    timelineRows.add(row2);

    final TherapyRowDto row3 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy3 = new ConstantComplexTherapyDto();
    therapy3.setTherapyDescription("Aspirin");
    therapy3.setLinkName("B2");
    therapy3.setCreatedTimestamp(new DateTime(2015, 11, 3, 0, 0));
    row3.setTherapy(therapy3);
    timelineRows.add(row3);

    final TherapyRowDto row4 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy4 = new ConstantComplexTherapyDto();
    therapy4.setTherapyDescription("AAspirin");
    therapy4.setCreatedTimestamp(new DateTime(2015, 11, 11, 0, 0));
    row4.setTherapy(therapy4);
    timelineRows.add(row4);

    final TherapyRowDto row5 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy5 = new ConstantComplexTherapyDto();
    therapy5.setTherapyDescription("Bspirin");
    therapy5.setCreatedTimestamp(new DateTime(2015, 11, 5, 0, 0));
    row5.setTherapy(therapy5);
    timelineRows.add(row5);

    final TherapyRowDto row6 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy6 = new ConstantComplexTherapyDto();
    therapy6.setTherapyDescription("Azlekadol");
    therapy6.setLinkName("C1");
    therapy6.setCreatedTimestamp(new DateTime(2015, 11, 1, 0, 0));
    row6.setTherapy(therapy6);
    timelineRows.add(row6);

    final TherapyRowDto row7 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy7 = new ConstantComplexTherapyDto();
    therapy7.setTherapyDescription("Bzlekadol");
    therapy7.setCreatedTimestamp(new DateTime(2015, 11, 6, 0, 0));
    row7.setTherapy(therapy7);
    timelineRows.add(row7);

    final TherapyRowDto row8 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy8 = new ConstantComplexTherapyDto();
    therapy8.setTherapyDescription("Czlekadol");
    therapy8.setLinkName("C2");
    therapy8.setCreatedTimestamp(new DateTime(2015, 11, 1, 0, 0));
    row8.setTherapy(therapy8);
    timelineRows.add(row8);

    final TherapyRowDto row10 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy10 = new ConstantComplexTherapyDto();
    therapy10.setTherapyDescription("BAlekadol");
    therapy10.setLinkName("C3");
    therapy10.setCreatedTimestamp(new DateTime(2015, 11, 1, 0, 0));
    row10.setTherapy(therapy10);
    timelineRows.add(row10);

    final TherapyRowDto row9 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy9 = new ConstantComplexTherapyDto();
    therapy9.setTherapyDescription("ZZZlekadol");
    therapy9.setLinkName("D2");
    therapy9.setCreatedTimestamp(new DateTime(2015, 10, 5, 0, 0));
    row9.setTherapy(therapy9);
    timelineRows.add(row9);

    final TherapySortTypeEnum therapySortTypeEnumTimeDesc = TherapySortTypeEnum.CREATED_TIME_DESC;
    overviewContentProvider.sortTherapyRowsAndAdministrations(timelineRows, therapySortTypeEnumTimeDesc);

    assertEquals("AAspirin", timelineRows.get(0).getTherapy().getTherapyDescription());
    assertEquals("Azlekadol", timelineRows.get(6).getTherapy().getTherapyDescription());
    assertEquals("C1", timelineRows.get(6).getTherapy().getLinkName());
    assertEquals("Czlekadol", timelineRows.get(7).getTherapy().getTherapyDescription());
    assertEquals("C2", timelineRows.get(7).getTherapy().getLinkName());
    assertEquals("BAlekadol", timelineRows.get(8).getTherapy().getTherapyDescription());
    assertEquals("C3", timelineRows.get(8).getTherapy().getLinkName());
    assertEquals("Bspirin", timelineRows.get(2).getTherapy().getTherapyDescription());
    assertEquals("Bzlekadol", timelineRows.get(1).getTherapy().getTherapyDescription());
    assertEquals("Cspirin", timelineRows.get(4).getTherapy().getTherapyDescription());
    assertEquals("B1", timelineRows.get(4).getTherapy().getLinkName());
    assertEquals("Aspirin", timelineRows.get(5).getTherapy().getTherapyDescription());
    assertEquals("B2", timelineRows.get(5).getTherapy().getLinkName());
    assertEquals("ZZAspirin", timelineRows.get(3).getTherapy().getTherapyDescription());
    assertEquals("A1", timelineRows.get(3).getTherapy().getLinkName());
    assertEquals("ZZZlekadol", timelineRows.get(9).getTherapy().getTherapyDescription());
    assertEquals("D2", timelineRows.get(9).getTherapy().getLinkName());

    final TherapySortTypeEnum therapySortTypeEnumTimeAsc = TherapySortTypeEnum.CREATED_TIME_ASC;
    overviewContentProvider.sortTherapyRowsAndAdministrations(timelineRows, therapySortTypeEnumTimeAsc);

    assertEquals("AAspirin", timelineRows.get(9).getTherapy().getTherapyDescription());
    assertEquals("Azlekadol", timelineRows.get(1).getTherapy().getTherapyDescription());
    assertEquals("C1", timelineRows.get(1).getTherapy().getLinkName());
    assertEquals("Czlekadol", timelineRows.get(2).getTherapy().getTherapyDescription());
    assertEquals("C2", timelineRows.get(2).getTherapy().getLinkName());
    assertEquals("BAlekadol", timelineRows.get(3).getTherapy().getTherapyDescription());
    assertEquals("C3", timelineRows.get(3).getTherapy().getLinkName());
    assertEquals("Bspirin", timelineRows.get(6).getTherapy().getTherapyDescription());
    assertEquals("Bzlekadol", timelineRows.get(8).getTherapy().getTherapyDescription());
    assertEquals("Cspirin", timelineRows.get(4).getTherapy().getTherapyDescription());
    assertEquals("B1", timelineRows.get(4).getTherapy().getLinkName());
    assertEquals("Aspirin", timelineRows.get(5).getTherapy().getTherapyDescription());
    assertEquals("B2", timelineRows.get(5).getTherapy().getLinkName());
    assertEquals("ZZAspirin", timelineRows.get(7).getTherapy().getTherapyDescription());
    assertEquals("A1", timelineRows.get(7).getTherapy().getLinkName());
    assertEquals("ZZZlekadol", timelineRows.get(0).getTherapy().getTherapyDescription());
    assertEquals("D2", timelineRows.get(0).getTherapy().getLinkName());

    final TherapySortTypeEnum therapySortTypeEnumDescAsc = TherapySortTypeEnum.DESCRIPTION_ASC;
    overviewContentProvider.sortTherapyRowsAndAdministrations(timelineRows, therapySortTypeEnumDescAsc);

    assertEquals("AAspirin", timelineRows.get(0).getTherapy().getTherapyDescription());
    assertEquals("Azlekadol", timelineRows.get(1).getTherapy().getTherapyDescription());
    assertEquals("C1", timelineRows.get(1).getTherapy().getLinkName());
    assertEquals("Czlekadol", timelineRows.get(2).getTherapy().getTherapyDescription());
    assertEquals("C2", timelineRows.get(2).getTherapy().getLinkName());
    assertEquals("BAlekadol", timelineRows.get(3).getTherapy().getTherapyDescription());
    assertEquals("C3", timelineRows.get(3).getTherapy().getLinkName());
    assertEquals("Bspirin", timelineRows.get(4).getTherapy().getTherapyDescription());
    assertEquals("Bzlekadol", timelineRows.get(5).getTherapy().getTherapyDescription());
    assertEquals("Cspirin", timelineRows.get(6).getTherapy().getTherapyDescription());
    assertEquals("B1", timelineRows.get(6).getTherapy().getLinkName());
    assertEquals("Aspirin", timelineRows.get(7).getTherapy().getTherapyDescription());
    assertEquals("B2", timelineRows.get(7).getTherapy().getLinkName());
    assertEquals("ZZAspirin", timelineRows.get(8).getTherapy().getTherapyDescription());
    assertEquals("A1", timelineRows.get(8).getTherapy().getLinkName());
    assertEquals("ZZZlekadol", timelineRows.get(9).getTherapy().getTherapyDescription());
    assertEquals("D2", timelineRows.get(9).getTherapy().getLinkName());

    final TherapySortTypeEnum therapySortTypeEnumDescDes = TherapySortTypeEnum.DESCRIPTION_DESC;
    overviewContentProvider.sortTherapyRowsAndAdministrations(timelineRows, therapySortTypeEnumDescDes);

    assertEquals("AAspirin", timelineRows.get(9).getTherapy().getTherapyDescription());
    assertEquals("Azlekadol", timelineRows.get(6).getTherapy().getTherapyDescription());
    assertEquals("C1", timelineRows.get(6).getTherapy().getLinkName());
    assertEquals("Czlekadol", timelineRows.get(7).getTherapy().getTherapyDescription());
    assertEquals("C2", timelineRows.get(7).getTherapy().getLinkName());
    assertEquals("BAlekadol", timelineRows.get(8).getTherapy().getTherapyDescription());
    assertEquals("C3", timelineRows.get(8).getTherapy().getLinkName());
    assertEquals("Bspirin", timelineRows.get(5).getTherapy().getTherapyDescription());
    assertEquals("Bzlekadol", timelineRows.get(4).getTherapy().getTherapyDescription());
    assertEquals("Cspirin", timelineRows.get(2).getTherapy().getTherapyDescription());
    assertEquals("B1", timelineRows.get(2).getTherapy().getLinkName());
    assertEquals("Aspirin", timelineRows.get(3).getTherapy().getTherapyDescription());
    assertEquals("B2", timelineRows.get(3).getTherapy().getLinkName());
    assertEquals("ZZAspirin", timelineRows.get(1).getTherapy().getTherapyDescription());
    assertEquals("A1", timelineRows.get(1).getTherapy().getLinkName());
    assertEquals("ZZZlekadol", timelineRows.get(0).getTherapy().getTherapyDescription());
    assertEquals("D2", timelineRows.get(0).getTherapy().getLinkName());
  }

  @Test
  public void testGetTherapyReviewedUntilReviewedInRounds()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.START, new DateTime(2016, 7, 1, 11, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 1, 11, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 2, 12, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 3, 13, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.MODIFY_EXISTING, new DateTime(2016, 7, 3, 22, 0)));

    final DateTime therapyReviewedUntil = overviewContentProvider.getTherapyReviewedUntil(actions, roundsIntervalDto);
    assertEquals(new DateTime(2016, 7, 4, 17, 0), therapyReviewedUntil);
  }

  @Test
  public void testGetTherapyReviewedUntilReviewedInRoundsNextTwoDaysWorkFree()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 4, 13, 0)));

    final DateTime therapyReviewedUntil = overviewContentProvider.getTherapyReviewedUntil(actions, roundsIntervalDto);
    assertEquals(new DateTime(2016, 7, 7, 17, 0), therapyReviewedUntil);
  }

  @Test
  public void testGetTherapyReviewedBeforeRounds()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 4, 5, 0)));

    final DateTime therapyReviewedUntil = overviewContentProvider.getTherapyReviewedUntil(actions, roundsIntervalDto);
    assertEquals(new DateTime(2016, 7, 4, 17, 0), therapyReviewedUntil);
  }

  @Test
  public void testGetTherapyReviewedBeforeRoundsNextTwoDaysWorkFree()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 5, 5, 0)));

    final DateTime therapyReviewedUntil = overviewContentProvider.getTherapyReviewedUntil(actions, roundsIntervalDto);
    assertEquals(new DateTime(2016, 7, 7, 17, 0), therapyReviewedUntil);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionCancelled()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 15, 10, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.CANCEL, new DateTime(2017, 1, 15, 12, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 13, 0),
        new DateTime(2017, 1, 16, 17, 0));
    assertEquals(TherapyStatusEnum.CANCELLED, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionAborted()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 15, 10, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.ABORT, new DateTime(2017, 1, 15, 12, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 13, 0),
        new DateTime(2017, 1, 16, 17, 0));
    assertEquals(TherapyStatusEnum.ABORTED, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionSuspended()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 15, 10, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.SUSPEND, new DateTime(2017, 1, 15, 12, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 13, 0),
        new DateTime(2017, 1, 16, 17, 0));
    assertEquals(TherapyStatusEnum.SUSPENDED, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionFuture()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 15, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 16, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 13, 0),
        new DateTime(2017, 1, 16, 17, 0));
    assertEquals(TherapyStatusEnum.FUTURE, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionVeryLateSameDay()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 14, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 18, 0),
        new DateTime(2017, 1, 15, 17, 0));
    assertEquals(TherapyStatusEnum.VERY_LATE, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionVeryLatePastDay()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 12, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 12, 0),
        new DateTime(2017, 1, 13, 17, 0));
    assertEquals(TherapyStatusEnum.VERY_LATE, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionLate()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 14, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 16, 30),
        new DateTime(2017, 1, 15, 17, 0));
    assertEquals(TherapyStatusEnum.LATE, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionNormal()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationActionAction> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 14, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 10, 30),
        new DateTime(2017, 1, 15, 17, 0));
    assertEquals(TherapyStatusEnum.NORMAL, status);
  }

  private MedicationActionAction buildMedicationAction(final MedicationActionEnum actionEnum, final DateTime actionTime)
  {
    final MedicationActionAction action = new MedicationActionAction();
    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(actionEnum.getCareflowStep());
    ismTransition.setCurrentState(actionEnum.getCurrentState());
    action.setIsmTransition(ismTransition);
    action.setTime(DataValueUtils.getDateTime(actionTime));
    return action;
  }

  private AdministrationTaskDto buildAdministrationTaskDto(
      final String taskId,
      final String administrationId,
      final String therapyId,
      final DateTime timestamp,
      final Double doseNumerator,
      final String doseNumeratorUnit)
  {
    final AdministrationTaskDto taskDto = new AdministrationTaskDto();
    taskDto.setTaskId(taskId);
    taskDto.setAdministrationId(administrationId);
    taskDto.setTherapyId(therapyId);
    taskDto.setPlannedAdministrationTime(timestamp);
    taskDto.setAdministrationTypeEnum(AdministrationTypeEnum.START);
    taskDto.setTherapyDoseDto(new TherapyDoseDto());
    taskDto.getTherapyDoseDto().setNumerator(doseNumerator);
    taskDto.getTherapyDoseDto().setNumeratorUnit(doseNumeratorUnit);
    taskDto.getTherapyDoseDto().setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    return taskDto;
  }
}
