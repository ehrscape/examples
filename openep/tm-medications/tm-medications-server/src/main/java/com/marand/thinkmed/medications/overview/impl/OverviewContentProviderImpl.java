package com.marand.thinkmed.medications.overview.impl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.CollectionUtils;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.daterule.service.MafDateRuleService;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.maf.core.time.DayType;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.tagging.dto.TagFilteringDto;
import com.marand.thinkehr.tagging.dto.TaggedObjectDto;
import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.TherapyTagEnum;
import com.marand.thinkmed.medications.TherapyTaggingUtils;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.OxygenAdministration;
import com.marand.thinkmed.medications.dto.administration.OxygenTaskDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.overview.ContinuousInfusionTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.OxygenTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningSimpleDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistUtils;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsDelegator;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Instruction;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static com.marand.maf.core.StringUtils.checkNotBlank;

/**
 * @author Nejc Korasa
 */
public class OverviewContentProviderImpl implements OverviewContentProvider
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private TaggingOpenEhrDao taggingOpenEhrDao;
  private AdministrationTaskConverter administrationTaskConverter;
  private MedicationsTasksProvider medicationsTasksProvider;
  private AdministrationHandler administrationHandler;
  private MedicationsBo medicationsBo;
  private AdministrationUtils administrationUtils;
  private MafDateRuleService mafDateRuleService;
  private AdditionalWarningsDelegator additionalWarningsDelegator;

  private boolean doctorReviewEnabled;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setTaggingOpenEhrDao(final TaggingOpenEhrDao taggingOpenEhrDao)
  {
    this.taggingOpenEhrDao = taggingOpenEhrDao;
  }

  @Autowired
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setAdministrationHandler(final AdministrationHandler administrationHandler)
  {
    this.administrationHandler = administrationHandler;
  }

  @Autowired
  public void setAdministrationUtils(final AdministrationUtils administrationUtils)
  {
    this.administrationUtils = administrationUtils;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setMafDateRuleService(final MafDateRuleService mafDateRuleService)
  {
    this.mafDateRuleService = mafDateRuleService;
  }

  @Autowired
  public void setAdditionalWarningsDelegator(final AdditionalWarningsDelegator additionalWarningsDelegator)
  {
    this.additionalWarningsDelegator = additionalWarningsDelegator;
  }

  @Value("${doctor.review.enabled}")
  public void setDoctorReviewEnabled(final boolean doctorReviewEnabled)
  {
    this.doctorReviewEnabled = doctorReviewEnabled;
  }

  @Override
  public TherapyFlowDto getTherapyFlow(
      final String patientId,
      final String centralCaseId,
      final Double patientHeight,
      final DateTime startDate,
      final int dayCount,
      final Integer todayIndex,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      @Nullable final String careProviderId,
      final DateTime currentTime,
      final Locale locale)
  {
    final TherapyFlowDto therapyFlow = new TherapyFlowDto();

    final DateTime startDateAtMidnight = startDate.withTimeAtStartOfDay();
    DateTime searchEnd = startDateAtMidnight.plusDays(dayCount);

    //show therapies that start in the future
    if ((todayIndex != null && todayIndex == dayCount - 1) || startDate.plusDays(dayCount - 1).isAfter(currentTime))
    {
      searchEnd = Intervals.INFINITE.getEnd();
    }

    final Interval searchInterval = new Interval(startDateAtMidnight, searchEnd);
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, searchInterval, null);

    final Map<Integer, Double> referenceWeightsByDay = new HashMap<>();
    for (int i = 0; i < dayCount; i++)
    {
      final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(
          patientId, Intervals.infiniteTo(startDateAtMidnight.plusDays(i).plusDays(1)));
      if (referenceWeight != null)
      {
        referenceWeightsByDay.put(i, referenceWeight);
      }
    }
    therapyFlow.setReferenceWeightsDayMap(referenceWeightsByDay);
    final List<TherapyFlowRowDto> therapyRows = buildTherapyFlow(
        patientId,
        centralCaseId,
        patientHeight,
        instructionsList,
        startDateAtMidnight,
        dayCount,
        todayIndex,
        roundsInterval,
        therapySortTypeEnum,
        referenceWeightsByDay,
        careProviderId,
        currentTime,
        locale);

    therapyFlow.setTherapyRows(therapyRows);
    return therapyFlow;
  }

  private List<TherapyFlowRowDto> buildTherapyFlow(
      final String patientId,
      final String centralCaseId,
      final Double patientHeight,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList,
      final DateTime startDate,
      final int dayCount,
      final Integer todayIndex,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      final Map<Integer, Double> referenceWeightsByDay,
      @Nullable final String careProviderId,
      final DateTime currentTime,
      final Locale locale)
  {
    if (instructionsList.isEmpty())
    {
      return new ArrayList<>();
    }
    medicationsBo.sortTherapiesByMedicationTimingStart(instructionsList, false);

    final Map<Pair<MedicationOrderComposition, MedicationInstructionInstruction>, String> linksMap =
        buildLinksMap(instructionsList);

    final Map<String, String> therapyIdOriginalTherapyIdMap = new HashMap<>(); //therapy id, original therapy id

    final Map<TherapyFlowRowDto, List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>>> therapyRowsMap = new LinkedHashMap<>();

    final Map<Long, MedicationDataForTherapyDto> medicationsMap =
        medicationsBo.getMedicationDataForTherapies(instructionsList, careProviderId);

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionsList)
    {
      final MedicationOrderComposition composition = instructionPair.getFirst();
      final MedicationInstructionInstruction instruction = instructionPair.getSecond();
      final MedicationInstructionInstruction.OrderActivity orderActivity = MedicationsEhrUtils.getRepresentingOrderActivity(
          instruction);

      final String originalTherapyId = MedicationsEhrUtils.getOriginalTherapyId(composition, instruction);
      final String therapyId = TherapyIdUtils.createTherapyId(composition, instruction);
      therapyIdOriginalTherapyIdMap.put(therapyId, originalTherapyId);

      TherapyFlowRowDto therapyRowDto = findModifiedTherapyForFlow(instructionPair, therapyRowsMap);
      if (therapyRowDto == null)
      {
        therapyRowDto = new TherapyFlowRowDto();

        final Long mainMedicationId = medicationsBo.getMainMedicationId(orderActivity);
        final MedicationDataForTherapyDto mainMedicationData = medicationsMap.get(mainMedicationId);
        if (mainMedicationData != null)
        {
          therapyRowDto.setCustomGroup(mainMedicationData.getCustomGroupName());
          therapyRowDto.setCustomGroupSortOrder(mainMedicationData.getCustomGroupSortOrder());
          therapyRowDto.setAtcGroupCode(mainMedicationData.getAtcGroupCode());
          therapyRowDto.setAtcGroupName(mainMedicationData.getAtcGroupName());
        }
      }

      if (!therapyRowsMap.containsKey(therapyRowDto))
      {
        therapyRowsMap.put(therapyRowDto, new ArrayList<>());
      }

      therapyRowsMap.get(therapyRowDto).add(instructionPair);

      if (CollectionUtils.isEmpty(therapyRowDto.getRoutes()))
      {
        therapyRowDto.setRoutes(
            orderActivity.getAdministrationDetails().getRoute()
                .stream()
                .map(DvText::getValue)
                .collect(Collectors.toList()));
      }

      final Interval instructionInterval = MedicationsEhrUtils.getInstructionInterval(orderActivity.getMedicationTiming());

      for (int i = 0; i < dayCount; i++)
      {
        Interval therapyDay = Intervals.wholeDay(startDate.plusDays(i));
        //if today or future, show therapies that start in the future
        final boolean isToday = todayIndex != null && todayIndex == i;
        if (isToday || startDate.plusDays(i).isAfter(currentTime))
        {
          therapyDay = Intervals.infiniteFrom(therapyDay.getStart());
        }

        if (instructionInterval.overlap(therapyDay) != null || instructionInterval.getStart().equals(therapyDay.getStart()))
        {
          final Double referenceWeight = referenceWeightsByDay.get(i);
          final TherapyDto therapy =
              medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
                  composition,
                  instruction,
                  referenceWeight,
                  patientHeight,
                  currentTime,
                  isToday,
                  locale);

          final TherapyDayDto dayDto = new TherapyDayDto();
          fillTherapyDayState(
              dayDto,
              patientId,
              therapy,
              instruction,
              composition,
              roundsInterval,
              null,
              medicationsMap,
              therapyDay,
              currentTime);

          if (isToday)
          {
            therapy.setLinkName(linksMap.get(instructionPair));
          }
          therapyRowDto.getTherapyFlowDayMap().put(i, dayDto);
        }
      }
    }

    final List<TherapyFlowRowDto> therapiesList = new ArrayList<>(therapyRowsMap.keySet());
    sortTherapyFlowRows(therapiesList, therapySortTypeEnum);

    if (StringUtils.isNotBlank(centralCaseId))
    {
      final TagFilteringDto filter = new TagFilteringDto();
      filter.setCompositionVersion(TagFilteringDto.CompositionVersion.LAST_VERSION_OF_ANY_TAGGED);

      final Set<TaggedObjectDto<Instruction>> taggedTherapies =
          taggingOpenEhrDao.findObjectCompositionPairs(
              filter, TherapyTaggingUtils.generateTag(TherapyTagEnum.PRESCRIPTION, centralCaseId));

      fillPrescriptionTagsForTherapyFlow(therapiesList, taggedTherapies);
    }

    if (todayIndex != null && todayIndex > -1)
    {
      final List<TherapyDayDto> todayTherapies = new ArrayList<>();
      for (final TherapyFlowRowDto therapyFlowRowDto : therapiesList)
      {
        if (therapyFlowRowDto.getTherapyFlowDayMap().containsKey(todayIndex))
        {
          final TherapyDayDto therapyDayDto = therapyFlowRowDto.getTherapyFlowDayMap().get(todayIndex);
          todayTherapies.add(therapyDayDto);
        }
      }

      fillPharmacyReviewDate(patientId, todayTherapies);
      fillReminderTaskData(patientId, todayTherapies, therapyIdOriginalTherapyIdMap, currentTime);
    }

    return therapiesList;
  }

  private void fillPharmacyReviewDate(final String patientId, final List<? extends TherapyDayDto> therapiesList)
  {
    if (!therapiesList.isEmpty())
    {
      DateTime firstTherapyCreateTimestamp = null;
      for (final TherapyDayDto therapy : therapiesList)
      {
        if (firstTherapyCreateTimestamp == null ||
            therapy.getTherapy().getCreatedTimestamp().isBefore(firstTherapyCreateTimestamp))
        {
          firstTherapyCreateTimestamp = therapy.getTherapy().getCreatedTimestamp();
        }
      }

      final List<PharmacyReviewReportComposition> pharmacistsReviewCompositions =
          medicationsOpenEhrDao.findPharmacistsReviewCompositions(patientId, firstTherapyCreateTimestamp);

      final Pair<DateTime, Set<String>> lastReviewTimestampAndReferredBackTherapiesCompositionUIds =
          PharmacistUtils.getLastReviewTimestampAndReferredBackTherapiesCompositionUIds(pharmacistsReviewCompositions);

      final DateTime lastReviewTimestamp = lastReviewTimestampAndReferredBackTherapiesCompositionUIds.getFirst();
      final Set<String> referredBackTherapiesCompositionUIds = lastReviewTimestampAndReferredBackTherapiesCompositionUIds.getSecond();

      for (final TherapyDayDto therapyDayDto : therapiesList)
      {
        if (therapyDayDto != null)
        {
          if (referredBackTherapiesCompositionUIds.contains(
              TherapyIdUtils.getCompositionUidWithoutVersion(therapyDayDto.getTherapy().getCompositionUid())))
          {
            therapyDayDto.setTherapyPharmacistReviewStatus(TherapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK);
          }
          else if (therapyDayDto.isBasedOnPharmacyReview())
          {
            therapyDayDto.setTherapyPharmacistReviewStatus(TherapyPharmacistReviewStatusEnum.REVIEWED);
          }
          else if (lastReviewTimestamp == null || therapyDayDto.getLastModifiedTimestamp().isAfter(lastReviewTimestamp))
          {
            therapyDayDto.setTherapyPharmacistReviewStatus(TherapyPharmacistReviewStatusEnum.NOT_REVIEWED);
          }
          else
          {
            therapyDayDto.setTherapyPharmacistReviewStatus(TherapyPharmacistReviewStatusEnum.REVIEWED);
          }
        }
      }
    }
  }

  private void fillReminderTaskData(
      final String patientId,
      final List<? extends TherapyDayDto> therapiesList,
      final Map<String, String> therapyIdOriginalTherapyIdMap,
      final DateTime when) //therapy id, original therapy id
  {
    final Map<String, List<TherapyTaskSimpleDto>> tasksMap =
        medicationsTasksProvider.findSimpleTasksForTherapies(patientId, therapyIdOriginalTherapyIdMap.values(), when);

    for (final TherapyDayDto therapyDayDto : therapiesList)
    {
      final String therapyId =
          TherapyIdUtils.createTherapyId(
              therapyDayDto.getTherapy().getCompositionUid(), therapyDayDto.getTherapy().getEhrOrderName());
      final String originalTherapyId =
          therapyIdOriginalTherapyIdMap.containsKey(therapyId) ? therapyIdOriginalTherapyIdMap.get(therapyId) : therapyId;
      final List<TherapyTaskSimpleDto> tasksList = tasksMap.get(originalTherapyId);
      if (tasksList != null)
      {
        therapyDayDto.getTasks().addAll(tasksList);
      }
    }
  }

  private Map<Pair<MedicationOrderComposition, MedicationInstructionInstruction>, String> buildLinksMap(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList)
  {
    final Map<Pair<MedicationOrderComposition, MedicationInstructionInstruction>, String> linksMap = new HashMap<>();

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionsList)
    {
      final List<Link> followLinks = MedicationsEhrUtils.getLinksOfType(instructionPair.getSecond(), EhrLinkType.FOLLOW);
      if (!followLinks.isEmpty())
      {
        final Link link = followLinks.get(0);
        final String linkName = DvUtils.getString(link.getMeaning());
        linksMap.put(instructionPair, linkName);

        if (linkName.length() == 2 && linkName.endsWith("2"))
        {
          for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair : instructionsList)
          {
            final boolean linkExists = medicationsBo.doesInstructionHaveLinkToCompareInstruction(
                instructionPair.getSecond(), compareInstructionPair, EhrLinkType.FOLLOW);
            if (linkExists)
            {
              final String previousLinkName = MedicationsEhrUtils.getPreviousLinkName(linkName);
              linksMap.put(compareInstructionPair, previousLinkName);
              break;
            }
          }
        }
      }
    }
    return linksMap;
  }

  private void fillPrescriptionTagsForTherapyFlow(
      final List<TherapyFlowRowDto> therapiesList,
      final Set<TaggedObjectDto<Instruction>> therapiesWithPrescriptionTag)
  {
    final List<TherapyDto> therapyDtos = new ArrayList<>();
    for (final TherapyFlowRowDto flowRowDto : therapiesList)
    {
      therapyDtos.addAll(
          flowRowDto.getTherapyFlowDayMap().keySet()
              .stream()
              .map(key -> flowRowDto.getTherapyFlowDayMap().get(key).getTherapy())
              .collect(Collectors.toList()));
    }
    fillPrescriptionTagsForTherapies(therapyDtos, therapiesWithPrescriptionTag);
  }

  private void sortTherapyFlowRows(
      final List<TherapyFlowRowDto> therapyRows,
      final TherapySortTypeEnum therapySortTypeEnum)
  {
    final Map<String, TherapyDayDto> firstTherapyWithLinkPrefixMap = new HashMap<>();
    for (final TherapyFlowRowDto therapyFlowRow : therapyRows)
    {
      final TherapyDayDto therapyRow = therapyFlowRow.getTherapyFlowDayMap().values().iterator().next();
      if (therapyRow.getTherapy().getLinkName() != null)
      {
        final String linkPrefix = therapyRow.getTherapy().getLinkName().substring(0, 1);
        if (firstTherapyWithLinkPrefixMap.get(linkPrefix) != null)
        {
          final int compare = sortByLinkName(
              therapyRow, firstTherapyWithLinkPrefixMap.get(linkPrefix));
          if (compare < 0)
          {
            firstTherapyWithLinkPrefixMap.put(linkPrefix, therapyFlowRow.getTherapyFlowDayMap().values().iterator().next());
          }
        }
        else
        {
          firstTherapyWithLinkPrefixMap.put(linkPrefix, therapyFlowRow.getTherapyFlowDayMap().values().iterator().next());
        }
      }
    }
    Collections.sort(
        therapyRows, (firstTherapyFlowRow, secondTherapyFlowRow) -> {
          final TherapyDayDto firstTherapyDay = firstTherapyFlowRow.getTherapyFlowDayMap().values().iterator().next();
          final TherapyDayDto secondTherapyDay = secondTherapyFlowRow.getTherapyFlowDayMap().values().iterator().next();
          return compareTherapies(firstTherapyDay, secondTherapyDay, therapySortTypeEnum, firstTherapyWithLinkPrefixMap);
        }
    );
  }

  public void sortTherapyRowsAndAdministrations(
      final List<TherapyRowDto> timelineRows,
      final TherapySortTypeEnum therapySortTypeEnum)
  {
    final Map<String, TherapyDayDto> firstTherapyWithLinkPrefixMap = new HashMap<>();
    for (final TherapyRowDto timelineRow : timelineRows)
    {
      timelineRow.getAdministrations().sort(Comparator.comparing(administrationUtils::getAdministrationTime));

      if (timelineRow.getTherapy().getLinkName() != null)
      {
        final String linkPrefix = timelineRow.getTherapy().getLinkName().substring(0, 1);
        if (firstTherapyWithLinkPrefixMap.get(linkPrefix) != null)
        {
          final int compare = sortByLinkName(timelineRow, firstTherapyWithLinkPrefixMap.get(linkPrefix));
          if (compare < 0)
          {
            firstTherapyWithLinkPrefixMap.put(linkPrefix, timelineRow);
          }
        }
        else
        {
          firstTherapyWithLinkPrefixMap.put(linkPrefix, timelineRow);
        }
      }
    }

    Collections.sort(
        timelineRows, (firstTherapyTimelineRow, secondTherapyTimelineRow) -> compareTherapies(
            firstTherapyTimelineRow,
            secondTherapyTimelineRow,
            therapySortTypeEnum,
            firstTherapyWithLinkPrefixMap)
    );
  }

  public int compareTherapies(
      final TherapyDayDto firstTherapyDayRow,
      final TherapyDayDto secondTherapyDayRow,
      final TherapySortTypeEnum therapySortTypeEnum,
      final Map<String, TherapyDayDto> firstTherapyWithLinkPrefixMap)
  {
    TherapyDayDto firstTherapyDay = firstTherapyDayRow;
    TherapyDayDto secondTherapyDay = secondTherapyDayRow;
    final String firstTherapyLinkPrefix = firstTherapyDay.getTherapy().getLinkName() != null ?
                                          firstTherapyDay.getTherapy().getLinkName().substring(0, 1) : null;
    final String secondTherapyLinkPrefix = secondTherapyDay.getTherapy().getLinkName() != null ?
                                           secondTherapyDay.getTherapy().getLinkName().substring(0, 1) : null;
    if (firstTherapyLinkPrefix != null && secondTherapyLinkPrefix != null && firstTherapyLinkPrefix.equals(
        secondTherapyLinkPrefix))
    {
      return sortByLinkName(firstTherapyDay, secondTherapyDay);
    }
    if (firstTherapyLinkPrefix != null && firstTherapyWithLinkPrefixMap.keySet().contains(firstTherapyLinkPrefix))
    {
      firstTherapyDay = firstTherapyWithLinkPrefixMap.get(firstTherapyLinkPrefix);
    }
    if (secondTherapyLinkPrefix != null && firstTherapyWithLinkPrefixMap.keySet().contains(secondTherapyLinkPrefix))
    {
      secondTherapyDay = firstTherapyWithLinkPrefixMap.get(secondTherapyLinkPrefix);
    }

    return sortTherapies(firstTherapyDay, secondTherapyDay, therapySortTypeEnum);
  }

  private int sortTherapies(
      final TherapyDayDto firstDayTherapy,
      final TherapyDayDto secondDayTherapy,
      final TherapySortTypeEnum therapySortTypeEnum)
  {
    final TherapyDto firstTherapy = firstDayTherapy.getTherapy();
    final TherapyDto secondTherapy = secondDayTherapy.getTherapy();
    final Collator collator = Collator.getInstance();

    if (therapySortTypeEnum == TherapySortTypeEnum.CREATED_TIME_ASC)
    {
      final int compareResult = firstTherapy.getCreatedTimestamp().compareTo(secondTherapy.getCreatedTimestamp());
      return compareResult == 0 ? collator.compare(
          firstTherapy.getTherapyDescription(),
          secondTherapy.getTherapyDescription()) : compareResult;
    }
    if (therapySortTypeEnum == TherapySortTypeEnum.CREATED_TIME_DESC)
    {
      final int compareResult = secondTherapy.getCreatedTimestamp().compareTo(firstTherapy.getCreatedTimestamp());
      return compareResult == 0 ? collator.compare(
          firstTherapy.getTherapyDescription(),
          secondTherapy.getTherapyDescription()) : compareResult;
    }
    if (therapySortTypeEnum == TherapySortTypeEnum.DESCRIPTION_ASC)
    {
      return medicationsBo.compareTherapiesForSort(firstTherapy, secondTherapy, collator);
    }
    if (therapySortTypeEnum == TherapySortTypeEnum.DESCRIPTION_DESC)
    {
      return medicationsBo.compareTherapiesForSort(secondTherapy, firstTherapy, collator);
    }
    else
    {
      throw new IllegalArgumentException("Unsupported therapy sort type!");
    }
  }

  private int sortByLinkName(
      final TherapyDayDto firstDayTherapy,
      final TherapyDayDto secondDayTherapy)
  {
    final TherapyDto firstTherapy = firstDayTherapy.getTherapy();
    final TherapyDto secondTherapy = secondDayTherapy.getTherapy();
    final Collator collator = Collator.getInstance();
    final String firstTherapyLinkName = firstDayTherapy.getTherapy().getLinkName();
    final String secondTherapyLinkName = secondDayTherapy.getTherapy().getLinkName();
    if (firstTherapyLinkName != null && secondTherapyLinkName != null)
    {
      final int sortByLink = firstTherapyLinkName.compareTo(secondTherapyLinkName);
      if (sortByLink == 0)
      {
        return medicationsBo.compareTherapiesForSort(firstTherapy, secondTherapy, collator);
      }
      return sortByLink;
    }
    else if (firstTherapyLinkName == null && secondTherapyLinkName == null)
    {
      return medicationsBo.compareTherapiesForSort(firstTherapy, secondTherapy, collator);
    }
    else if (firstTherapyLinkName != null)
    {
      return -1;
    }
    else
    {
      return 1;
    }
  }

  public void removeOldCompletedTherapies(final List<TherapyRowDto> timelineRows, final DateTime now)
  {
    final List<TherapyRowDto> activeTherapyTimelines = new ArrayList<>();
    for (final TherapyRowDto therapyRowDto : timelineRows)
    {
      boolean isTherapy1ex = false;
      if (therapyRowDto.getTherapy().getDosingFrequency() != null)
      {
        isTherapy1ex = therapyRowDto.getTherapy().getDosingFrequency().getType() == DosingFrequencyTypeEnum.ONCE_THEN_EX;
      }
      boolean allRecentTasksCompleted = true;
      boolean taskFrom1exCompletedInLast24h = false;
      for (final AdministrationDto administration : therapyRowDto.getAdministrations())
      {
        final AdministrationStatusEnum status = administration.getAdministrationStatus();
        final boolean taskNotCompleted =
            status == AdministrationStatusEnum.PLANNED ||
                status == AdministrationStatusEnum.DUE ||
                status == AdministrationStatusEnum.LATE;
        final boolean recentTask =
            administration.getPlannedTime() != null &&
                Intervals.infiniteFrom(now.minusHours(8)).contains(administration.getPlannedTime());
        if (recentTask && taskNotCompleted)
        {
          allRecentTasksCompleted = false;
        }
        if (isTherapy1ex && !taskNotCompleted) //if task is completed and therapy is 1ex
        {
          taskFrom1exCompletedInLast24h = Intervals.infiniteFrom(now.minusHours(24))
              .contains(administration.getAdministrationTime());
        }
      }
      final DateTime therapyEnd = therapyRowDto.getTherapy().getEnd();
      final boolean oldTherapy = therapyEnd != null && !Intervals.infiniteFrom(now.minusHours(8)).contains(therapyEnd);
      if (isTherapy1ex && taskFrom1exCompletedInLast24h)
      {
        activeTherapyTimelines.add(therapyRowDto);
      }
      else if (!oldTherapy)
      {
        final boolean therapyActiveOrJustEnded =
            therapyEnd == null || Intervals.infiniteFrom(now.minusMinutes(5)).contains(therapyEnd);
        if (!allRecentTasksCompleted || therapyActiveOrJustEnded)
        {
          activeTherapyTimelines.add(therapyRowDto);
        }
      }

    }
    timelineRows.clear();
    timelineRows.addAll(activeTherapyTimelines);
  }

  private TherapyFlowRowDto findModifiedTherapyForFlow(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapy,
      final Map<TherapyFlowRowDto, List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>>> therapyRowsMap)
  {
    for (final TherapyFlowRowDto therapyFlowRow : therapyRowsMap.keySet())
    {
      for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareTherapy :
          therapyRowsMap.get(therapyFlowRow))
      {
        final boolean therapiesLinkedByEdit = medicationsBo.areInstructionsLinkedByUpdate(therapy, compareTherapy);
        if (therapiesLinkedByEdit)
        {
          return therapyFlowRow;
        }
      }
    }
    return null;
  }

  @Override
  public TherapyTimelineDto getTherapyTimeline(
      @Nonnull final String patientId,
      @Nonnull final List<AdministrationDto> administrations,
      @Nonnull final List<AdministrationTaskDto> administrationTasks,
      @Nonnull final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      @Nonnull final TherapySortTypeEnum therapySortTypeEnum,
      final boolean hidePastTherapies,
      @Nonnull final PatientDataForMedicationsDto patientData,
      final Interval tasksInterval,
      final RoundsIntervalDto roundsInterval,
      final Locale locale,
      @Nonnull final DateTime when)
  {
    checkNotBlank(patientId, "patientId must be defined!");
    Preconditions.checkNotNull(administrations, "administrations must not be null!");
    Preconditions.checkNotNull(administrationTasks, "administrationTasks must not be null!");
    Preconditions.checkNotNull(medicationInstructions, "medicationInstructions must not be null!");
    Preconditions.checkNotNull(therapySortTypeEnum, "therapySortTypeEnum must not be null!");
    Preconditions.checkNotNull(patientData, "patientData must not be null!");
    Preconditions.checkNotNull(when, "when must not be null!");

    final Opt<AdditionalWarningsDto> additionalWarnings = additionalWarningsDelegator.getAdditionalWarnings(
        Arrays.asList(AdditionalWarningsType.values()),
        patientId,
        patientData,
        when,
        locale);

    final List<TherapyAdditionalWarningDto> therapyAdditionalWarnings = additionalWarnings
        .map(a -> a.getWarnings().stream().collect(Collectors.toList()))
        .orElse(Collections.emptyList());

    final EnumSet<AdditionalWarningsType> additionalWarningTypes = EnumSet.noneOf(AdditionalWarningsType.class);
    therapyAdditionalWarnings
        .stream()
        .flatMap(therapyAdditionalWarning -> therapyAdditionalWarning.getWarnings().stream())
        .map(AdditionalWarningDto::getAdditionalWarningsType)
        .forEach(additionalWarningTypes::add);

    final List<TherapyRowDto> rows = buildTherapyRows(
        patientId,
        medicationInstructions,
        administrations,
        administrationTasks,
        therapySortTypeEnum,
        hidePastTherapies,
        therapyAdditionalWarnings,
        patientData,
        tasksInterval,
        roundsInterval,
        locale,
        when);

    return new TherapyTimelineDto(rows, additionalWarnings.get(), additionalWarningTypes);
  }

  @Override
  public List<TherapyRowDto> buildTherapyRows(
      @Nonnull final String patientId,
      @Nonnull final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      @Nonnull final List<AdministrationDto> administrations,
      @Nonnull final List<AdministrationTaskDto> administrationTasks,
      @Nonnull final TherapySortTypeEnum therapySortTypeEnum,
      final boolean hidePastTherapies,
      @Nonnull final List<TherapyAdditionalWarningDto> therapyAdditionalWarnings,
      final PatientDataForMedicationsDto patientData,
      final Interval interval,
      final RoundsIntervalDto roundsInterval,
      final Locale locale,
      @Nonnull final DateTime when)
  {
    checkNotBlank(patientId, "patientId");
    Preconditions.checkNotNull(medicationInstructions, "medicationInstructions");
    Preconditions.checkNotNull(administrations, "administrations");
    Preconditions.checkNotNull(administrationTasks, "administrationTasks");
    Preconditions.checkNotNull(therapySortTypeEnum, "therapySortTypeEnum");
    Preconditions.checkNotNull(therapyAdditionalWarnings, "therapyAdditionalWarnings");
    Preconditions.checkNotNull(when, "when");

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> sortedInstructions = new ArrayList<>();
    sortedInstructions.addAll(medicationInstructions);
    medicationsBo.sortTherapiesByMedicationTimingStart(sortedInstructions, true);

    final Map<String, TherapyRowDto> therapyRowsMap = new HashMap<>();
    final Map<String, String> modifiedTherapiesMap = new HashMap<>(); //therapy id, latest modified therapy id
    final Map<String, String> therapyIdOriginalTherapyIdMap = new HashMap<>(); //therapy id, original therapy id

    fillTherapyRowsMaps(
        patientId,
        patientData,
        sortedInstructions,
        therapyRowsMap,
        modifiedTherapiesMap,
        therapyIdOriginalTherapyIdMap,
        roundsInterval,
        interval,
        when,
        locale);

    if (!administrations.isEmpty())
    {
      administrations.sort(Comparator.comparing(administrationUtils::getAdministrationTime));
      administrationHandler.addAdministrationsToTimelines(administrations, therapyRowsMap, modifiedTherapiesMap, interval);
    }

    if (!administrationTasks.isEmpty())
    {
      addAdministrationTasksToTimeline(administrationTasks, therapyRowsMap, modifiedTherapiesMap, when);
    }

    final List<TherapyRowDto> therapyRows = new ArrayList<>(therapyRowsMap.values());
    if (hidePastTherapies)
    {
      removeOldCompletedTherapies(therapyRows, when);
    }
    sortTherapyRowsAndAdministrations(therapyRows, therapySortTypeEnum);

    final String centralCaseId = Opt.resolve(() -> patientData.getCentralCaseDto().getCentralCaseId()).orElse(null);
    if (StringUtils.isNotBlank(centralCaseId))
    {
      final TagFilteringDto filter = new TagFilteringDto();
      filter.setCompositionVersion(TagFilteringDto.CompositionVersion.LAST_VERSION_OF_ANY_TAGGED);

      final Set<TaggedObjectDto<Instruction>> taggedTherapies =
          taggingOpenEhrDao.findObjectCompositionPairs(
              filter,
              TherapyTaggingUtils.generateTag(TherapyTagEnum.PRESCRIPTION, centralCaseId));

      fillPrescriptionTagsForTherapyRows(therapyRows, taggedTherapies);
    }

    fillPharmacyReviewDate(patientId, therapyRows);
    fillReminderTaskData(patientId, therapyRows, therapyIdOriginalTherapyIdMap, when);

    therapyRows.forEach(r -> r.setAdditionalWarnings(getAdditionalWarningsForTherapy(
        r.getTherapyId(),
        therapyAdditionalWarnings)));

    return therapyRows;
  }

  List<AdditionalWarningSimpleDto> getAdditionalWarningsForTherapy(
      final String therapyId,
      final Collection<TherapyAdditionalWarningDto> therapyAdditionalWarnings)
  {
    return therapyAdditionalWarnings
        .stream()
        .filter(t -> t.getTherapy().getTherapyId().equals(therapyId))
        .flatMap(t -> t.getWarnings().stream())
        .map(a -> new AdditionalWarningSimpleDto(a.getWarning().getDescription(), a.getAdditionalWarningsType()))
        .collect(Collectors.toList());
  }

  private void fillPrescriptionTagsForTherapyRows(
      final List<TherapyRowDto> therapyRows,
      final Set<TaggedObjectDto<Instruction>> taggedTherapies)
  {
    final List<TherapyDto> therapyDtoList = new ArrayList<>();
    for (final TherapyRowDto timelineRowDto : therapyRows)
    {
      therapyDtoList.add(timelineRowDto.getTherapy());
    }
    fillPrescriptionTagsForTherapies(therapyDtoList, taggedTherapies);
  }

  public void fillPrescriptionTagsForTherapies(
      final List<TherapyDto> therapies,
      final Set<TaggedObjectDto<Instruction>> taggedTherapies)
  {
    for (final TherapyDto therapy : therapies)
    {
      final String compositionId = therapy.getCompositionUid();
      for (final TaggedObjectDto<Instruction> taggedObjectDto : taggedTherapies)
      {
        if (taggedObjectDto.getComposition().getUid().getValue().equals(compositionId)
            && taggedObjectDto.getObject().getName().getValue().equals(therapy.getEhrOrderName()))
        {
          therapy.addTag(TherapyTagEnum.PRESCRIPTION);
        }
      }
    }
  }

  private void addAdministrationTasksToTimeline(
      final List<AdministrationTaskDto> tasks,
      final Map<String, TherapyRowDto> therapyTimelineRowsMap,
      final Map<String, String> modifiedTherapiesMap,
      final DateTime when)
  {
    for (final AdministrationTaskDto task : tasks)
    {
      final String therapyId = task.getTherapyId();
      final String latestTherapyId =
          modifiedTherapiesMap.containsKey(therapyId) ? modifiedTherapiesMap.get(therapyId) : therapyId;
      final TherapyRowDto timelineRow = therapyTimelineRowsMap.get(latestTherapyId);
      if (timelineRow != null)
      {
        boolean isAdministrationFound = false;
        for (final AdministrationDto administration : timelineRow.getAdministrations())
        {
          final String administrationUidWithoutVersion =
              TherapyIdUtils.getCompositionUidWithoutVersion(administration.getAdministrationId());
          final String taskAdministrationUidWithoutVersion =
              TherapyIdUtils.getCompositionUidWithoutVersion(task.getAdministrationId());
          if (administrationUidWithoutVersion != null &&
              administrationUidWithoutVersion.equals(taskAdministrationUidWithoutVersion))
          {
            isAdministrationFound = true;
            administration.setTaskId(task.getTaskId());
            administration.setGroupUUId(task.getGroupUUId());
            administration.setTherapyId(task.getTherapyId());
            administration.setPlannedTime(task.getPlannedAdministrationTime());
            administration.setDoctorConfirmation(task.getDoctorConfirmation());
            administration.setDoctorsComment(task.getDoctorsComment());
            if (administration.getAdministrationStatus() == AdministrationStatusEnum.COMPLETED
                || administration.getAdministrationStatus() == AdministrationStatusEnum.FAILED)
            {
              if (administration instanceof StartAdministrationDto)
              {
                final StartAdministrationDto startAdministration = (StartAdministrationDto)administration;
                final TherapyDoseDto administrationDose = startAdministration.getAdministeredDose();
                final TherapyDoseDto taskDose = task.getTherapyDoseDto();
                startAdministration.setPlannedDose(taskDose);
                if (administrationDose == null || taskDose == null || administration.getAdministrationStatus() == AdministrationStatusEnum.FAILED)
                {
                  startAdministration.setDifferentFromOrder(false);
                }
                else
                {
                  final boolean numeratorsUnequal =
                      Math.abs(administrationDose.getNumerator() - taskDose.getNumerator()) > 0.000000000001;
                  boolean secondaryNumeratorsUnequal = false;
                  if (!timelineRow.getTherapy().isVariable() && administrationDose.getSecondaryNumerator() != null &&
                      taskDose.getSecondaryNumerator() != null)
                  {
                    secondaryNumeratorsUnequal = !administrationDose.getSecondaryNumerator()
                        .equals(taskDose.getSecondaryNumerator());
                  }

                  startAdministration.setDifferentFromOrder(
                      //!administrationDose.equals(taskDose) || startAdministration.getSubstituteMedication() != null); //TODO ENGLISH
                      numeratorsUnequal || secondaryNumeratorsUnequal);
                }
              }
              else if (administration instanceof AdjustInfusionAdministrationDto)
              {
                final AdjustInfusionAdministrationDto adjustAdministrationDto = (AdjustInfusionAdministrationDto)administration;
                final TherapyDoseDto administrationDose = adjustAdministrationDto.getAdministeredDose();
                final TherapyDoseDto taskDose = task.getTherapyDoseDto();
                adjustAdministrationDto.setPlannedDose(taskDose);
                if (administrationDose == null && taskDose == null)
                {
                  adjustAdministrationDto.setDifferentFromOrder(false);
                }
                else
                {
                  adjustAdministrationDto.setDifferentFromOrder(
                      administrationDose == null || !administrationDose.equals(taskDose));
                }
              }
              if (administration instanceof OxygenAdministration)
              {
                ((OxygenAdministration)administration).setPlannedStartingDevice(((OxygenTaskDto)task).getStartingDevice());
              }

              if (administration.getAdministrationStatus() == AdministrationStatusEnum.COMPLETED)
              {
                final AdministrationStatusEnum status =
                    AdministrationStatusEnum.getFromTime(
                        task.getPlannedAdministrationTime(),
                        administration.getAdministrationTime(),
                        when);
                administration.setAdministrationStatus(status);
              }
            }
            break;
          }
        }
        if (!isAdministrationFound)
        {
          final AdministrationDto administration = administrationTaskConverter.buildAdministrationFromTask(task, when);
          administration.setTherapyId(task.getTherapyId());
          timelineRow.getAdministrations().add(administration);
        }
      }
    }
  }

  private void fillTherapyRowsMaps(
      final String patientId,
      final PatientDataForMedicationsDto patientData,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies,
      final Map<String, TherapyRowDto> therapyTimelineRowsMap,
      final Map<String, String> modifiedTherapiesMap,
      final Map<String, String> therapyIdOriginalTherapyIdMap,
      final RoundsIntervalDto roundsInterval,
      @Nullable final Interval interval,
      final DateTime when,
      final Locale locale)
  {
    final Map<Pair<MedicationOrderComposition, MedicationInstructionInstruction>, String> linksMap =
        buildLinksMap(therapies);

    final Map<String, Pair<MedicationOrderComposition, MedicationInstructionInstruction>> processedTherapiesMap = new HashMap<>();
    final Map<Long, MedicationDataForTherapyDto> medicationsDataMap = medicationsBo.getMedicationDataForTherapies(
        therapies,
        Opt.resolve(() -> patientData.getCentralCaseDto().getCareProvider().getId()).orElse(null));

    final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, Intervals.infiniteTo(when));

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : therapies)
    {
      final MedicationInstructionInstruction instruction = instructionPair.getSecond();
      final MedicationOrderComposition composition = instructionPair.getFirst();

      final String originalTherapyId = MedicationsEhrUtils.getOriginalTherapyId(composition, instruction);
      final String therapyId = TherapyIdUtils.createTherapyId(composition, instruction);
      therapyIdOriginalTherapyIdMap.put(therapyId, originalTherapyId);

      final String latestModifiedTherapyId = findModifiedTherapyForTimeline(processedTherapiesMap, instructionPair);
      if (latestModifiedTherapyId != null)
      {
        modifiedTherapiesMap.put(therapyId, latestModifiedTherapyId);
        processedTherapiesMap.put(latestModifiedTherapyId, instructionPair);
      }
      else
      {
        final TherapyRowDto timelineRow =
            buildTherapyRowDto(
                patientData,
                referenceWeight,
                roundsInterval,
                interval,
                when,
                locale,
                medicationsDataMap,
                instruction,
                composition,
                therapyId,
                patientId);

        therapyTimelineRowsMap.put(therapyId, timelineRow);

        timelineRow.getTherapy().setLinkName(linksMap.get(instructionPair));

        processedTherapiesMap.put(therapyId, instructionPair);
      }
    }
  }

  private TherapyRowDto buildTherapyRowDto(
      final PatientDataForMedicationsDto patientData,
      final Double referenceWeight,
      final RoundsIntervalDto roundsInterval,
      @Nullable final Interval interval,
      final DateTime when,
      final Locale locale,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final MedicationInstructionInstruction instruction,
      final MedicationOrderComposition composition,
      final String therapyId,
      final String patientId)
  {
    //noinspection Convert2MethodRef
    final Double patientHeight = Opt.resolve(() -> patientData.getHeightInCm()).orElse(null);

    final TherapyDto therapy = medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
        composition,
        instruction,
        referenceWeight,
        patientHeight,
        when,
        true,
        locale);

    final TherapyRowDto timelineRow = createEmptyTherapyRow(instruction);
    timelineRow.setTherapyId(therapyId);

    final Long mainMedicationId = medicationsBo.getMainMedicationId(instruction.getOrder().get(0));
    final MedicationDataForTherapyDto mainMedicationData = medicationsDataMap.get(mainMedicationId);
    if (mainMedicationData != null)
    {
      timelineRow.setCustomGroup(mainMedicationData.getCustomGroupName());
      timelineRow.setCustomGroupSortOrder(mainMedicationData.getCustomGroupSortOrder());
      timelineRow.setAtcGroupCode(mainMedicationData.getAtcGroupCode());
      timelineRow.setAtcGroupName(mainMedicationData.getAtcGroupName());
    }

    fillTherapyDayState(
        timelineRow,
        patientId,
        therapy,
        instruction,
        composition,
        roundsInterval,
        interval,
        medicationsDataMap,
        Intervals.wholeDay(when),
        when);

    return timelineRow;
  }

  private TherapyRowDto createEmptyTherapyRow(final MedicationInstructionInstruction instruction)
  {
    if (MedicationsEhrUtils.isContinuousInfusion(instruction))
    {
      return new ContinuousInfusionTherapyRowDtoDto();
    }
    if (MedicationsEhrUtils.isOxygenInstruction(instruction))
    {
      return new OxygenTherapyRowDtoDto();
    }
    return new TherapyRowDto();
  }

  private String findModifiedTherapyForTimeline(
      final Map<String, Pair<MedicationOrderComposition, MedicationInstructionInstruction>> processedTherapiesMap,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair)
  {
    for (final String therapyId : processedTherapiesMap.keySet())
    {
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair =
          processedTherapiesMap.get(therapyId);

      final boolean laterLinkedTherapyFound = medicationsBo.areInstructionsLinkedByUpdate(
          instructionPair,
          compareInstructionPair);
      if (laterLinkedTherapyFound)
      {
        return therapyId;
      }
    }
    return null;
  }

  @Override
  public Map<String, TherapyDayDto> getCompositionUidAndTherapyDayDtoMap(
      final Map<String, String> therapyCompositionUidAndPatientIdMap,
      final DateTime when,
      final Locale locale)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapyInstructionPairs =
        medicationsOpenEhrDao.getTherapyInstructionPairs(therapyCompositionUidAndPatientIdMap.keySet());

    final Map<String, MedicationOrderComposition> compositionUidAndCompositionMap = new HashMap<>();
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair : therapyInstructionPairs)
    {
      final MedicationOrderComposition medicationOrderComposition = therapyInstructionPair.getFirst();
      compositionUidAndCompositionMap.put(
          TherapyIdUtils.getCompositionUidWithoutVersion(medicationOrderComposition.getUid().getValue()),
          medicationOrderComposition);
    }

    return getCompositionUidAndTherapyDayDtoMap(
        therapyCompositionUidAndPatientIdMap,
        therapyInstructionPairs,
        compositionUidAndCompositionMap,
        when,
        locale);
  }

  @Override
  public Map<String, TherapyDayDto> getOriginalCompositionUidAndLatestTherapyDayDtoMap(
      final Map<String, String> originalTherapyCompositionUidAndPatientIdMap,
      final int searchIntervalInWeeks,
      final DateTime when,
      final Locale locale)
  {
    final Set<String> originalCompositionUids = originalTherapyCompositionUidAndPatientIdMap.keySet(); //getCompositionUidsFromTherapyIds(originalTherapyIdAndPatientIdMap.keySet());
    final Set<String> patientIds = Sets.newHashSet(originalTherapyCompositionUidAndPatientIdMap.values());

    final Map<String, MedicationOrderComposition> originalCompositionUidAndLatestCompositionMap =
        medicationsOpenEhrDao.getLatestCompositionsForOriginalCompositionUids(
            originalCompositionUids,
            patientIds,
            searchIntervalInWeeks,
            when);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapyInstructionPairs = new ArrayList<>();
    for (final Map.Entry<String, MedicationOrderComposition> entrySet : originalCompositionUidAndLatestCompositionMap.entrySet())
    {
      therapyInstructionPairs.add(
          Pair.of(
              entrySet.getValue(),
              entrySet.getValue().getMedicationDetail().getMedicationInstruction().get(0)));
    }
    return getCompositionUidAndTherapyDayDtoMap(
        originalTherapyCompositionUidAndPatientIdMap,
        therapyInstructionPairs,
        originalCompositionUidAndLatestCompositionMap,
        when,
        locale);
  }

  private Map<String, TherapyDayDto> getCompositionUidAndTherapyDayDtoMap(
      final Map<String, String> therapyIdAndPatientIdMap,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapyInstructionPairs,
      final Map<String, MedicationOrderComposition> compositionUidAndCompositionMap,
      final DateTime when,
      final Locale locale)
  {
    final Map<String, TherapyDayDto> originalCompositionUdAndTherapyDayDtoMap = new HashMap<>();
    final Map<Long, MedicationDataForTherapyDto> medicationDataForTherapies = medicationsBo.getMedicationDataForTherapies(
        therapyInstructionPairs,
        null);

    final Map<String, List<TherapyDayDto>> patientIdAndTherapyDayDtosMap = new HashMap<>();
    for (final Map.Entry<String, MedicationOrderComposition> entrySet : compositionUidAndCompositionMap.entrySet())
    {
      final TherapyDayDto therapyDayDto = new TherapyDayDto();
      final MedicationOrderComposition orderComposition = entrySet.getValue();
      final MedicationInstructionInstruction instruction = orderComposition.getMedicationDetail()
          .getMedicationInstruction()
          .get(0);

      final TherapyDto therapy =
          medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
              orderComposition,
              instruction,
              null,  //todo supply - get from client
              null,
              when,
              true,
              locale);

      final String patientId = therapyIdAndPatientIdMap.get(entrySet.getKey());

      fillTherapyDayState(
          therapyDayDto,
          patientId,
          therapy,
          instruction,
          orderComposition,
          null,        //todo supply - load it
          null,
          medicationDataForTherapies,
          Intervals.wholeDay(when),
          when);

      originalCompositionUdAndTherapyDayDtoMap.put(entrySet.getKey(), therapyDayDto);

      if (patientIdAndTherapyDayDtosMap.containsKey(patientId))
      {
        patientIdAndTherapyDayDtosMap.get(patientId).add(therapyDayDto);
      }
      else
      {
        final List<TherapyDayDto> therapyDayDtos = new ArrayList<>();
        therapyDayDtos.add(therapyDayDto);
        patientIdAndTherapyDayDtosMap.put(patientId, therapyDayDtos);
      }
    }

    for (final String patientId : patientIdAndTherapyDayDtosMap.keySet())
    {
      fillPharmacyReviewDate(patientId, patientIdAndTherapyDayDtosMap.get(patientId));
    }

    return originalCompositionUdAndTherapyDayDtoMap;
  }

  private void fillTherapyDayState(
      final TherapyDayDto therapyDayDto,
      final String patientId,
      final TherapyDto therapy,
      final MedicationInstructionInstruction instruction,
      final MedicationOrderComposition composition,
      final RoundsIntervalDto roundsInterval,
      @Nullable final Interval interval,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final Interval therapyDay,
      final DateTime currentTime)
  {
    final List<MedicationActionAction> actions =
        MedicationsEhrUtils.getInstructionActions(composition, instruction, interval);
    final MedicationInstructionInstruction.OrderActivity orderActivity = MedicationsEhrUtils.getRepresentingOrderActivity(
        instruction);

    therapyDayDto.setTherapy(therapy);
    final DateTime lastModifiedTimestamp = MedicationsEhrUtils.getLastModifiedTimestamp(composition, instruction);

    if (lastModifiedTimestamp != null)
    {
      therapyDayDto.setLastModifiedTimestamp(lastModifiedTimestamp);
    }
    else
    {
      therapyDayDto.setLastModifiedTimestamp(therapy.getCreatedTimestamp());
    }

    therapyDayDto.setBasedOnPharmacyReview(!MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.BASED_ON).isEmpty());

    final DateTime originalTherapyStart = medicationsBo.getOriginalTherapyStart(patientId, composition);
    therapyDayDto.setOriginalTherapyStart(originalTherapyStart);
    DateTime therapyEnd = DataValueUtils.getDateTime(orderActivity.getMedicationTiming().getStopDate());
    therapyEnd = therapyEnd != null ? therapyEnd : Intervals.INFINITE.getEnd();

    final DateTime therapyReviewedUntil = getTherapyReviewedUntil(actions, roundsInterval);
    therapyDayDto.setReviewedUntil(therapyReviewedUntil);
    final boolean doctorReviewNeeded = isDoctorReviewNeeded(therapyReviewedUntil, roundsInterval, currentTime);
    therapyDayDto.setDoctorReviewNeeded(doctorReviewNeeded);

    final TherapyStatusEnum therapyStatus = getTherapyStatusFromMedicationAction(
        actions,
        originalTherapyStart,
        roundsInterval,
        currentTime,
        therapyReviewedUntil);

    final boolean modifiedFromLastReview = medicationsBo.isTherapyModifiedFromLastReview(
        instruction,
        actions,
        DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()));

    final List<Long> medicationIds = medicationsBo.getMedicationIds(orderActivity);
    final boolean containsAntibiotic = containsAntibiotic(medicationIds, medicationsDataMap);
    final Integer pastDaysOfTherapy =
        orderActivity.getPastDaysOfTherapy() != null ? (int)orderActivity.getPastDaysOfTherapy().getMagnitude() : null;
    final int consecutiveDay =
        medicationsBo.getTherapyConsecutiveDay(originalTherapyStart, therapyDay.getStart(), currentTime, pastDaysOfTherapy);
    final boolean therapyEndsBeforeNextRounds =
        roundsInterval != null && doesTherapyEndBeforeNextRounds(therapyEnd, roundsInterval, currentTime);
    final boolean therapyIsActive =
        medicationsBo.isTherapyActive(
            therapy.getDaysOfWeek(),
            therapy.getDosingDaysFrequency(),
            new Interval(originalTherapyStart, therapyEnd),
            therapyDay.getStart());

    if (therapyStatus == TherapyStatusEnum.SUSPENDED)
    {
      final TherapyChangeReasonDto therapySuspendReason = medicationsBo.getTherapySuspendReason(composition, instruction);
      if (therapySuspendReason != null)
      {
        therapyDayDto.setTherapyChangeReasonEnum(
            TherapyChangeReasonEnum.fromFullString(therapySuspendReason.getChangeReason().getCode()));
      }
    }

    therapyDayDto.setActive(therapyIsActive);
    therapyDayDto.setModified(!instruction.getLinks().isEmpty());
    therapyDayDto.setTherapyEndsBeforeNextRounds(therapyEndsBeforeNextRounds);
    therapyDayDto.setTherapyStatus(therapyStatus);
    therapyDayDto.setShowConsecutiveDay(containsAntibiotic && therapyDay.getStart().isBefore(currentTime));
    therapyDayDto.setModifiedFromLastReview(modifiedFromLastReview);
    therapyDayDto.setConsecutiveDay(consecutiveDay);

    final boolean therapySuspended = wasTherapySuspendedWholeDay(actions, therapyDay);
    therapyDayDto.setActiveAnyPartOfDay(therapyIsActive && !therapySuspended);
  }

  DateTime getTherapyReviewedUntil(
      final List<MedicationActionAction> therapyActions,
      final RoundsIntervalDto roundsInterval)
  {
    if (roundsInterval == null)
    {
      return null;
    }

    final DateTime lastReviewTimestamp = therapyActions
        .stream()
        .filter(a -> MedicationActionEnum.THERAPY_REVIEW_ACTIONS.contains(MedicationActionEnum.getActionEnum(a)))
        .map(a -> DataValueUtils.getDateTime(a.getTime()))
        .max(Comparator.naturalOrder())
        .orElse(null);

    if (lastReviewTimestamp == null)
    {
      return null;
    }

    final DateTime startOfReviewDayRounds =
        lastReviewTimestamp.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getStartHour())
            .plusMinutes(roundsInterval.getStartMinute());

    final DateTime endOfReviewDayRounds =
        lastReviewTimestamp.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getEndHour())
            .plusMinutes(roundsInterval.getEndMinute());

    final boolean lastReviewBeforeRounds = lastReviewTimestamp.isBefore(startOfReviewDayRounds);
    DateTime reviewedUntil = lastReviewBeforeRounds ? endOfReviewDayRounds : endOfReviewDayRounds.plusDays(1);

    while (!mafDateRuleService.isDateOfType(reviewedUntil.withTimeAtStartOfDay(), DayType.WORKING_DAY))
    {
      reviewedUntil = reviewedUntil.plusDays(1);
    }
    return reviewedUntil;
  }

  private boolean wasTherapySuspendedWholeDay(
      final List<MedicationActionAction> actionsList,  //actions sorted by time ascending
      final Interval dayInterval)
  {
    boolean suspended = false;
    for (final MedicationActionAction action : actionsList)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.SUSPEND &&
          DataValueUtils.getDateTime(action.getTime()).isBefore(dayInterval.getStart()))
      {
        suspended = true;
      }
      else if (actionEnum == MedicationActionEnum.REISSUE &&
          DataValueUtils.getDateTime(action.getTime()).isBefore(dayInterval.getEnd()))
      {
        suspended = false;
      }
    }
    return suspended;
  }

  private boolean containsAntibiotic(
      final List<Long> medicationIds,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap)
  {
    for (final Long medicationId : medicationIds)
    {
      final MedicationDataForTherapyDto mainMedicationData = medicationsMap.get(medicationId);
      if (mainMedicationData != null && mainMedicationData.isAntibiotic())
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final RoundsIntervalDto roundsInterval,
      final DateTime when)
  {
    final MedicationOrderComposition composition =
        medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);

    final TherapyReloadAfterActionDto reloadDto = new TherapyReloadAfterActionDto();
    reloadDto.setEhrCompositionId(composition.getUid().getValue());
    reloadDto.setEhrOrderName(instruction.getName().getValue());
    final DateTime originalTherapyStart = medicationsBo.getOriginalTherapyStart(patientId, composition);
    final DateTime therapyEnd =
        DataValueUtils.getDateTime(instruction.getOrder().get(0).getMedicationTiming().getStopDate());

    reloadDto.setTherapyStart(originalTherapyStart);
    reloadDto.setTherapyEnd(therapyEnd);

    final List<MedicationActionAction> actions = MedicationsEhrUtils.getInstructionActions(composition, instruction);

    final DateTime therapyReviewedUntil = getTherapyReviewedUntil(actions, roundsInterval);
    final boolean doctorReviewNeeded = isDoctorReviewNeeded(therapyReviewedUntil, roundsInterval, when);
    reloadDto.setDoctorReviewNeeded(doctorReviewNeeded);

    reloadDto.setTherapyStatus(getTherapyStatusFromMedicationAction(
        actions,
        originalTherapyStart,
        roundsInterval,
        when,
        therapyReviewedUntil));

    final boolean therapyEndsBeforeNextRounds = doesTherapyEndBeforeNextRounds(therapyEnd, roundsInterval, when);
    reloadDto.setTherapyEndsBeforeNextRounds(therapyEndsBeforeNextRounds);
    return reloadDto;
  }

  private boolean doesTherapyEndBeforeNextRounds(
      final DateTime therapyEnd, final RoundsIntervalDto roundsInterval, final DateTime currentTime)
  {
    if (therapyEnd == null)
    {
      return false;
    }
    final DateTime startOfNextDaysRounds =
        currentTime.withTimeAtStartOfDay()
            .plusDays(1)
            .plusHours(roundsInterval.getStartHour())
            .plusMinutes(roundsInterval.getStartMinute());

    return therapyEnd.isBefore(startOfNextDaysRounds);
  }

  TherapyStatusEnum getTherapyStatusFromMedicationAction(
      final List<MedicationActionAction> actionsList,  //sorted by time asc
      final DateTime therapyStart,
      final RoundsIntervalDto roundsInterval,
      final DateTime when,
      final DateTime therapyReviewedUntil)
  {
    final Set<MedicationActionEnum> actionEnumSet =
        actionsList.stream()
            .map(MedicationActionEnum::getActionEnum)
            .collect(Collectors.toSet());

    if (actionEnumSet.contains(MedicationActionEnum.CANCEL))
    {
      return TherapyStatusEnum.CANCELLED;
    }
    if (actionEnumSet.contains(MedicationActionEnum.ABORT))
    {
      return TherapyStatusEnum.ABORTED;
    }
    if (medicationsBo.isTherapySuspended(actionsList))
    {
      return TherapyStatusEnum.SUSPENDED;
    }

    final DateTime startOfNextDaysRounds =
        Opt.of(roundsInterval)
            .map(r -> when.withTimeAtStartOfDay()
                .plusDays(1)
                .plusHours(r.getStartHour()).plusMinutes(r.getStartMinute()))
            .orElse(null);

    if (startOfNextDaysRounds != null && therapyStart.isAfter(startOfNextDaysRounds))
    {
      return TherapyStatusEnum.FUTURE;
    }

    if (doctorReviewEnabled && therapyReviewedUntil != null)
    {
      if (therapyReviewedUntil.isBefore(when))
      {
        return TherapyStatusEnum.VERY_LATE;
      }
      if (therapyReviewedUntil.minusHours(1).isBefore(when))
      {
        return TherapyStatusEnum.LATE;
      }
    }

    return TherapyStatusEnum.NORMAL;
  }

  private boolean isDoctorReviewNeeded(
      final DateTime reviewedUntil,
      final RoundsIntervalDto roundsInterval,
      final DateTime currentTime)
  {
    if (reviewedUntil != null)
    {
      final DateTime startOfReviewDayRounds =
          reviewedUntil.withTimeAtStartOfDay()
              .plusHours(roundsInterval.getStartHour())
              .plusMinutes(roundsInterval.getStartMinute());

      return currentTime.isAfter(startOfReviewDayRounds);
    }
    return true;
  }
}
