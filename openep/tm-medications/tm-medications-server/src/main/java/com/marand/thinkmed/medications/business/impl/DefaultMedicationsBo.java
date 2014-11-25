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

import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.marand.ispek.ehr.common.server.util.EhrUtils;
import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.ispek.ehr.common.tdo.IspekComposition;
import com.marand.maf.core.EnumUtils;
import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.openehr.dao.EhrTaggingDao;
import com.marand.maf.core.openehr.util.InstructionTranslator;
import com.marand.maf.core.openehr.visitor.IspekTdoDataSupport;
import com.marand.maf.core.openehr.visitor.TdoPopulatingVisitor;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrLinkType;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkehr.tagging.dto.TagFilteringDto;
import com.marand.thinkehr.tagging.dto.TaggedObjectDto;
import com.marand.thinkehr.web.FdoConstants;
import com.marand.thinkmed.api.core.data.NamedIdentity;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.ClinicalInterventionEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.EhrTerminologyEnum;
import com.marand.thinkmed.medications.InfusionSetChangeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.TherapyTag;
import com.marand.thinkmed.medications.TherapyTaggingUtils;
import com.marand.thinkmed.medications.b2b.MedicationsConnector;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.TherapyDocumentationData;
import com.marand.thinkmed.medications.business.TherapyLinkType;
import com.marand.thinkmed.medications.business.TherapySimilarityType;
import com.marand.thinkmed.medications.converter.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.converter.MedicationToEhrConverter;
import com.marand.thinkmed.medications.dao.EhrMedicationsDao;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import com.marand.thinkmed.medications.dto.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.InfusionRateCalculationDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationSearchDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyCardInfoDto;
import com.marand.thinkmed.medications.dto.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.TherapyChangeHistoryDto;
import com.marand.thinkmed.medications.dto.TherapyChangeType;
import com.marand.thinkmed.medications.dto.TherapyDayDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyFlowRowDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTaskDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTimelineRowDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTimelineRowForContInfusionDto;
import com.marand.thinkmed.medications.mapper.InstructionToAdministrationMapper;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.Action;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.DvIdentifier;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Instruction;
import org.openehr.jaxb.rm.InstructionDetails;
import org.openehr.jaxb.rm.IsmTransition;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.LocatableRef;
import org.openehr.jaxb.rm.ObjectVersionId;
import org.openehr.jaxb.rm.Participation;
import org.openehr.jaxb.rm.PartyIdentified;
import org.openehr.jaxb.rm.PartyProxy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster.IngredientQuantityCluster;
import static com.marand.openehr.medications.tdo.MedicationAdministrationComposition.MedicationDetailSection;
import static com.marand.openehr.medications.tdo.MedicationAdministrationComposition.MedicationDetailSection.ClinicalInterventionAction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity;
import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster;
import static com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition.MedicationReferenceBodyWeightObservation;
import static com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition.MedicationReferenceBodyWeightObservation.HistoryHistory;

/**
 * @author Mitja Lapajne
 */
public class DefaultMedicationsBo
    implements MedicationsBo, InitializingBean, MedicationFromEhrConverter.MedicationDataProvider
{
  private EhrMedicationsDao ehrMedicationsDao;
  private MedicationsDao medicationsDao;
  private EhrTaggingDao ehrTaggingDao;

  private TherapyDisplayProvider therapyDisplayProvider;
  private MedicationsConnector medicationsConnector;

  public void setEhrMedicationsDao(final EhrMedicationsDao ehrMedicationsDao)
  {
    this.ehrMedicationsDao = ehrMedicationsDao;
  }

  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  public void setEhrTaggingDao(final EhrTaggingDao ehrTaggingDao)
  {
    this.ehrTaggingDao = ehrTaggingDao;
  }

  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    Assert.notNull(ehrMedicationsDao, "ehrMedicationsDao is required");
    Assert.notNull(therapyDisplayProvider, "therapyDisplayProvider is required");
    Assert.notNull(medicationsConnector, "medicationsConnector is required");
  }

  @Override
  public MedicationOrderComposition saveNewMedicationOrder(
      final long patientId,
      final List<TherapyDto> therapiesList,
      final Long centralCaseId,
      final Long careProviderId,
      final long userId,
      final NamedIdentity prescriber,
      final RoundsIntervalDto roundsInterval,
      final DateTime when,
      final Locale locale)
  {
    final MedicationOrderComposition composition =
        buildMedicationOrderComposition(
            therapiesList,
            centralCaseId,
            careProviderId,
            userId,
            prescriber,
            roundsInterval,
            when,
            locale);
    return ehrMedicationsDao.saveNewMedicationOrderComposition(patientId, composition);
  }

  private MedicationOrderComposition buildMedicationOrderComposition(
      final List<TherapyDto> therapiesList,
      final Long centralCaseId,
      final Long careProviderId,
      final long userId,
      final NamedIdentity prescriber,
      final RoundsIntervalDto roundsInterval,
      final DateTime when,
      final Locale locale)
  {
    final MedicationOrderComposition composition = MedicationsEhrUtils.createEmptyMedicationOrderComposition();
    final Map<String, MedicationInstructionInstruction> fromLinks = new HashMap<>();
    final Map<String, MedicationInstructionInstruction> toLinks = new HashMap<>();

    for (final TherapyDto therapy : therapiesList)
    {
      if (therapy.getTherapyDescription() == null)
      {
        therapyDisplayProvider.fillDisplayValues(therapy, true, true, locale);
      }
      final MedicationToEhrConverter<?> therapyConverter = MedicationConverterSelector.getConverter(therapy);
      final MedicationInstructionInstruction instruction = therapyConverter.createInstructionFromTherapy(therapy);

      MedicationsEhrUtils.addInstructionTo(composition, instruction);

      if (therapy.getLinkToTherapy() != null)
      {
        toLinks.put(therapy.getLinkToTherapy(), instruction);
      }
      if (therapy.getLinkFromTherapy() != null)
      {
        fromLinks.put(therapy.getLinkFromTherapy(), instruction);
      }

      //createTherapyLinks(therapy, instruction, composition, therapiesList);
      MedicationsEhrUtils.addMedicationActionTo(composition, instruction, MedicationActionEnum.SCHEDULE, when);

      final DateTime therapyStart = therapy.getStart().isAfter(when) ? therapy.getStart() : when;
      MedicationsEhrUtils.addMedicationActionTo(composition, instruction, MedicationActionEnum.START, therapyStart);

      final boolean roundsTimeForSession = MedicationsEhrUtils.isRoundsTimeForSession(when, roundsInterval);
      if (roundsTimeForSession)
      {
        MedicationsEhrUtils.addMedicationActionTo(composition, instruction, MedicationActionEnum.REVIEW, when);
      }
    }

    createTherapyLinks(fromLinks, toLinks, composition);
    final NamedIdentity usersName = medicationsConnector.getUsersName(userId, when);
    addContext(composition, usersName, prescriber, centralCaseId, careProviderId, when);
    return composition;
  }

  private void createTherapyLinks(
      final Map<String, MedicationInstructionInstruction> fromLinks,
      final Map<String, MedicationInstructionInstruction> toLinks,
      final MedicationOrderComposition composition)
  {
    for (final String fromLink : fromLinks.keySet())
    {
      final MedicationInstructionInstruction instruction = fromLinks.get(fromLink);
      final MedicationInstructionInstruction linkInstruction = toLinks.get(fromLink);

      //final RmPath rmPath = TdoPathable.pathOfItem(composition, linkInstruction);
      final Link link = new Link();
      link.setMeaning(DataValueUtils.getText("startOnEndOf"));                   //todo fix name
      link.setType(DataValueUtils.getText(OpenEhrLinkType.ISSUE.getName()));   //todo fix type
      final DvEhrUri value = new DvEhrUri();
      value.setValue(String.valueOf(composition.getMedicationDetail().getMedicationInstruction().indexOf(linkInstruction)));
      link.setTarget(value);
      instruction.getLinks().add(link);
    }
  }

  //private void createTherapyLinks(
  //    final TherapyDto therapy,
  //    final MedicationInstructionInstruction therapyInstruction,
  //    final MedicationOrderComposition therapyComposition,
  //    final List<TherapyDto> therapiesList)
  //{
  //  if (therapy.getLinkFromTherapy() != null)
  //  {
  //    for (final TherapyDto previousTherapy : therapiesList)
  //    {
  //      if (previousTherapy.getLinkToTherapy() != null &&
  //          previousTherapy.getLinkToTherapy().equals(therapy.getLinkFromTherapy()))
  //      {
  //        final RmPath rmPath = TdoPathable.pathOfItem(therapyComposition, therapyInstruction);
  //        final Link link = new Link();
  //        link.setMeaning(DataValueUtils.getText("startOnEndOf"));                   //todo fix name
  //        link.setType(DataValueUtils.getText(OpenEhrLinkType.ISSUE.getName()));   //todo fix type
  //        link.setTarget(DataValueUtils.getEhrUri("1", rmPath));
  //        therapyInstruction.getLinks().add(link);
  //      }
  //    }
  //  }
  //}

  private void updateExistingContext(final MedicationOrderComposition composition, final NamedIdentity composer)
  {
    updateExistingContext(composition, composer, null);
  }

  private void updateExistingContext(
      final MedicationOrderComposition composition,
      final NamedIdentity composer,
      final NamedIdentity prescriber)
  {
    addContext(
        composition,
        composer,
        prescriber,
        null,
        null,
        DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()));
  }

  private void addContext(
      final IspekComposition medicationOrder,
      final NamedIdentity composer,
      final NamedIdentity prescriber,
      final Long centralCaseId,
      final Long careProviderId,
      final DateTime when)
  {
    final CompositionEventContext compositionEventContext =
        IspekTdoDataSupport.getEventContext(CompositionEventContext.class, when);

    if (centralCaseId != null)
    {
      final List<CompositionEventContext.ContextDetailCluster> contextDetailClusterList = new ArrayList<>();
      final CompositionEventContext.ContextDetailCluster contextDetailCluster = new CompositionEventContext.ContextDetailCluster();
      contextDetailCluster.setPeriodOfCareIdentifier(DataValueUtils.getText(Long.toString(centralCaseId)));

      if (careProviderId != null)
      {
        contextDetailCluster.setDepartmentalPeriodOfCareIdentifier(DataValueUtils.getText(Long.toString(careProviderId)));
      }

      contextDetailClusterList.add(contextDetailCluster);
      compositionEventContext.setContextDetail(contextDetailClusterList);
    }
    else if (medicationOrder.getCompositionEventContext() != null)
    {
      compositionEventContext.setContextDetail(medicationOrder.getCompositionEventContext().getContextDetail());
    }
    medicationOrder.setCompositionEventContext(compositionEventContext);

    final TdoPopulatingVisitor.DataContext dataContext =
        TdoPopulatingVisitor.getSloveneContext(when)
            .withCompositionDynamic(true)
            .withReplaceParticipation(true)
            .withCompositionComposer(
                IspekTdoDataSupport.getPartyIdentified(composer.name(), Long.toString(composer.id())));
    if (prescriber != null)
    {
      final Participation participation = new Participation();
      final PartyIdentified performerProxy = new PartyIdentified();
      final DvIdentifier partyIdentifier = new DvIdentifier();
      partyIdentifier.setId(Long.toString(prescriber.id()));
      performerProxy.getIdentifiers().add(partyIdentifier);
      performerProxy.setName(prescriber.name());
      participation.setPerformer(performerProxy);
      participation.setFunction(DataValueUtils.getText("prescriber"));
      participation.setMode(FdoConstants.PARTICIPATION_UNSPECIFIED_MODE);

      dataContext.withEntryParticipation(participation);
    }

    new TdoPopulatingVisitor().visitBean(medicationOrder, dataContext);
  }

  @Override
  public List<TherapyFlowRowDto> getTherapyFlow(
      final long patientId,
      final long centralCaseId,
      final Double patientHeight,
      final DateTime startDate,
      final int dayCount,
      final Integer todayIndex,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      final KnownClinic department,
      final DateTime currentTime,
      final Locale locale)
  {
    final DateTime startDateAtMidnight = startDate.withTimeAtStartOfDay();
    DateTime searchEnd = startDateAtMidnight.plusDays(dayCount);

    //show therapies that start in the future
    if ((todayIndex != null && todayIndex == dayCount - 1) || startDate.plusDays(dayCount - 1).isAfter(currentTime))
    {
      searchEnd = Intervals.INFINITE.getEnd();
    }

    final Interval searchInterval = new Interval(startDateAtMidnight, searchEnd);
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList =
        ehrMedicationsDao.findMedicationInstructions(patientId, searchInterval, null);

    final Map<Integer, Double> referenceWeightsByDay = new HashMap<>();
    for (int i = 0; i < dayCount; i++)
    {
      final Double referenceWeight = ehrMedicationsDao.getPatientLastReferenceWeight(
          patientId, Intervals.infiniteTo(startDateAtMidnight.plusDays(i).plusDays(1)));
      if (referenceWeight != null)
      {
        referenceWeightsByDay.put(i, referenceWeight);
      }
    }
    return buildTherapyFlow(
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
        department,
        currentTime,
        locale);
  }

  private List<TherapyFlowRowDto> buildTherapyFlow(
      final long patientId,
      final long centralCaseId,
      final Double patientHeight,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList,
      final DateTime startDate,
      final int dayCount,
      final Integer todayIndex,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      final Map<Integer, Double> referenceWeightsByDay,
      final KnownClinic department,
      final DateTime currentTime,
      final Locale locale)
  {
    if (instructionsList.isEmpty())
    {
      return new ArrayList<>();
    }
    sortTherapiesByMedicationTimingStart(instructionsList, false);

    final Map<TherapyFlowRowDto, List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>>> therapyRowsMap = new LinkedHashMap<>();

    final Map<Long, MedicationDataForTherapyDto> medicationsMap =
        getMedicationDataForTherapies(instructionsList, department, currentTime);

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionsList)
    {
      final MedicationOrderComposition composition = instructionPair.getFirst();
      final MedicationInstructionInstruction instruction = instructionPair.getSecond();
      final OrderActivity orderActivity = instruction.getOrder().get(0);

      TherapyFlowRowDto therapyRowDto = findSimilarTherapy(instructionPair, therapyRowsMap, medicationsMap);
      if (therapyRowDto == null)
      {
        therapyRowDto = new TherapyFlowRowDto();

        final Long mainMedicationId = getMainMedicationId(orderActivity);
        final MedicationDataForTherapyDto mainMedicationData = medicationsMap.get(mainMedicationId);
        if (mainMedicationData != null)
        {
          therapyRowDto.setCustomGroup(mainMedicationData.getCustomGroupName());
          therapyRowDto.setCustomGroupSortOrder(mainMedicationData.getCustomGroupSortOrder());
          therapyRowDto.setAtcGroupCode(mainMedicationData.getAtcCode());
          therapyRowDto.setAtcGroupName(mainMedicationData.getAtcName());
        }
      }

      if (!therapyRowsMap.containsKey(therapyRowDto))
      {
        therapyRowsMap.put(
            therapyRowDto, new ArrayList<Pair<MedicationOrderComposition, MedicationInstructionInstruction>>());
      }

      therapyRowsMap.get(therapyRowDto).add(instructionPair);

      if (therapyRowDto.getRoute() == null)
      {
        therapyRowDto.setRoute(orderActivity.getAdministrationDetails().getRoute().get(0).getValue());
      }

      final Interval instructionInterval = getInstructionInterval(orderActivity.getMedicationTiming());

      for (int i = 0; i < dayCount; i++)
      {
        Interval therapyDay = Intervals.wholeDay(startDate.plusDays(i));
        //if today or future, show therapies that start in the future
        final boolean isToday = todayIndex != null && todayIndex == i;
        if (isToday || startDate.plusDays(i).isAfter(currentTime))
        {
          therapyDay = Intervals.infiniteFrom(therapyDay.getStart());
        }

        if (instructionInterval.overlap(therapyDay) != null)
        {
          final Double referenceWeight = referenceWeightsByDay.get(i);
          final TherapyDto therapy =
              convertInstructionToTherapyDto(
                  composition,
                  instruction,
                  referenceWeight,
                  patientHeight,
                  currentTime,
                  isToday,
                  locale);
          final TherapyDayDto dayDto = new TherapyDayDto();
          dayDto.setTherapy(therapy);

          fillTherapyDayState(
              dayDto,
              patientId,
              therapy,
              instruction,
              composition,
              roundsInterval,
              medicationsMap,
              therapyDay,
              currentTime);
          therapyRowDto.getTherapyFlowDayMap().put(i, dayDto);
        }
      }
    }

    final List<TherapyFlowRowDto> therapiesList = new ArrayList<>(therapyRowsMap.keySet());
    sortTherapyFlowRows(therapiesList, therapySortTypeEnum);
    if (todayIndex != null)
    {
      fillTherapyFlowLinksDisplay(therapiesList, todayIndex);
    }

    if (centralCaseId > 0)
    {
      final TagFilteringDto filter = new TagFilteringDto();
      filter.setCompositionVersion(TagFilteringDto.CompositionVersion.LAST_VERSION_OF_ANY_TAGGED);

      final Set<TaggedObjectDto<Instruction>> taggedTherapies =
          ehrTaggingDao.findObjectCompositionPairs(
              filter, TherapyTaggingUtils.generateTag(TherapyTag.PRESCRIPTION, centralCaseId));

      fillPrescriptionTagsForTherapyFlow(therapiesList, taggedTherapies);
    }

    return therapiesList;
  }

  private void fillPrescriptionTagsForTherapyFlow(
      final List<TherapyFlowRowDto> therapiesList,
      final Set<TaggedObjectDto<Instruction>> therapiesWithPrescriptionTag)
  {
    final List<TherapyDto> therapyDtos = new ArrayList<>();
    for (final TherapyFlowRowDto flowRowDto : therapiesList)
    {
      for (final Integer key : flowRowDto.getTherapyFlowDayMap().keySet())
      {
        therapyDtos.add(flowRowDto.getTherapyFlowDayMap().get(key).getTherapy());
      }
    }
    fillPrescriptionTagsForTherapies(therapyDtos, therapiesWithPrescriptionTag);
  }

  //fills linkToTherapy for all linked therapies (when linksFromTherapy exists)
  private void fillTherapyFlowLinksDisplay(final List<TherapyFlowRowDto> therapiesList, final Integer todayIndex)
  {
    String link = "A";
    for (final TherapyFlowRowDto therapyFlowRowDto : therapiesList)
    {
      final TherapyDayDto therapyDayDto = therapyFlowRowDto.getTherapyFlowDayMap().get(todayIndex);

      if (therapyDayDto != null && therapyDayDto.getTherapy().getLinkFromTherapy() != null)
      {
        final boolean incrementLink = setTherapyFlowLinkDisplay(therapyDayDto.getTherapy(), therapiesList, todayIndex, link);
        if (incrementLink)
        {
          final int charValue = link.charAt(0);
          link = String.valueOf((char)(charValue + 1));
        }
      }
    }
  }

  private boolean setTherapyFlowLinkDisplay(
      final TherapyDto therapyWithLink,
      final List<TherapyFlowRowDto> therapiesList,
      final Integer todayIndex,
      final String link)
  {
    boolean linkUsed = false;

    for (final TherapyFlowRowDto therapyFlowRowDto : therapiesList)
    {
      final TherapyDayDto therapyDayDto = therapyFlowRowDto.getTherapyFlowDayMap().get(todayIndex);
      if (therapyDayDto != null)
      {
        final TherapyDto therapyDto = therapyDayDto.getTherapy();
        final boolean therapyUsedNewLink = setTherapyLinkDisplayValue(therapyWithLink, therapyDto, link);
        linkUsed = therapyUsedNewLink ? true : linkUsed;
      }
    }
    return linkUsed;
  }

  private boolean setTherapyLinkDisplayValue(final TherapyDto therapyWithLink, final TherapyDto therapyDto, final String link)
  {
    final String therapyFromLink = therapyWithLink.getLinkFromTherapy();
    final String therapyId =
        InstructionTranslator.createId(therapyDto.getEhrOrderName(), therapyDto.getCompositionUid());
    if (therapyId.equals(therapyFromLink))
    {
      if (therapyDto.getLinkFromTherapy() != null && therapyDto.getLinkFromTherapy().length() < 4)
      {
        therapyDto.setLinkToTherapy(therapyDto.getLinkFromTherapy());
        final String therapyLinkName = therapyDto.getLinkFromTherapy().substring(0, 1) +
            (Integer.parseInt(therapyDto.getLinkFromTherapy().substring(1)) + 1);
        therapyWithLink.setLinkFromTherapy(therapyLinkName);
      }
      else
      {
        therapyDto.setLinkToTherapy(link + '1');
        therapyWithLink.setLinkFromTherapy(link + '2');
        return true;
      }
    }
    return false;
  }

  private Map<Long, MedicationDataForTherapyDto> getMedicationDataForTherapies(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList,
      final KnownClinic department,
      final DateTime currentTime)
  {
    final Set<Long> medicationIds = new HashSet<>();

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionsList)
    {
      final OrderActivity orderActivity = instructionPair.getSecond().getOrder().get(0);
      medicationIds.addAll(getMedicationIds(orderActivity));
    }
    if (!medicationIds.isEmpty())
    {
      return medicationsDao.getMedicationDataForTherapies(medicationIds, department, currentTime);
    }
    return new HashMap<>();
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

  private void sortTherapyFlowRows(
      final List<TherapyFlowRowDto> therapyRows,
      final TherapySortTypeEnum therapySortTypeEnum)
  {
    Collections.sort(
        therapyRows, new Comparator<TherapyFlowRowDto>()
        {
          @Override
          public int compare(
              final TherapyFlowRowDto o1, final TherapyFlowRowDto o2)
          {
            final TherapyDto firstTherapy = o1.getTherapyFlowDayMap().values().iterator().next().getTherapy();
            final TherapyDto secondTherapy = o2.getTherapyFlowDayMap().values().iterator().next().getTherapy();

            return sortTherapies(firstTherapy, secondTherapy, therapySortTypeEnum);
          }
        }
    );
  }

  private void sortTherapyTimelineRows(
      final List<TherapyTimelineRowDto> timelineRows,
      final TherapySortTypeEnum therapySortTypeEnum)
  {
    Collections.sort(
        timelineRows, new Comparator<TherapyTimelineRowDto>()
        {
          @Override
          public int compare(final TherapyTimelineRowDto o1, final TherapyTimelineRowDto o2)
          {
            return sortTherapies(o1.getTherapy(), o2.getTherapy(), therapySortTypeEnum);
          }
        }
    );
  }

  private int sortTherapies(
      final TherapyDto firstTherapy,
      final TherapyDto secondTherapy,
      final TherapySortTypeEnum therapySortTypeEnum)
  {
    if (TherapySortTypeEnum.CREATED_TIME.contains(therapySortTypeEnum))
    {
      return therapySortTypeEnum == TherapySortTypeEnum.CREATED_TIME_DESC
             ? secondTherapy.getCreatedTimestamp().compareTo(firstTherapy.getCreatedTimestamp())
             : firstTherapy.getCreatedTimestamp().compareTo(secondTherapy.getCreatedTimestamp());
    }
    else if (TherapySortTypeEnum.DESCRIPTION.contains(therapySortTypeEnum))
    {
      final Collator collator = Collator.getInstance();
      return therapySortTypeEnum == TherapySortTypeEnum.DESCRIPTION_DESC
             ? compareTherapiesForSort(secondTherapy, firstTherapy, collator)
             : compareTherapiesForSort(firstTherapy, secondTherapy, collator);
    }
    else
    {
      throw new IllegalArgumentException("Unsopported therapy sort type!");
    }
  }

  private void removeOldTherapiesFromTimeline(final List<TherapyTimelineRowDto> timelines, final DateTime showTherapiesFrom)
  {
    final List<TherapyTimelineRowDto> activeTherapyTimelines = new ArrayList<>();
    for (final TherapyTimelineRowDto timeline : timelines)
    {
      final DateTime therapyEnd = timeline.getTherapy().getEnd();
      if (therapyEnd == null || Intervals.infiniteFrom(showTherapiesFrom).contains(therapyEnd))
      {
        activeTherapyTimelines.add(timeline);
      }
    }
    timelines.clear();
    timelines.addAll(activeTherapyTimelines);
  }

  private int compareTherapiesForSort(final TherapyDto firstTherapy, final TherapyDto secondTherapy, final Collator collator)
  {
    final boolean firstTherapyIsBaselineInfusion =
        firstTherapy instanceof ComplexTherapyDto && ((ComplexTherapyDto)firstTherapy).isBaselineInfusion();
    final boolean secondTherapyIsBaselineInfusion =
        secondTherapy instanceof ComplexTherapyDto && ((ComplexTherapyDto)secondTherapy).isBaselineInfusion();

    if (firstTherapyIsBaselineInfusion && !secondTherapyIsBaselineInfusion)
    {
      return -1;
    }
    if (!firstTherapyIsBaselineInfusion && secondTherapyIsBaselineInfusion)
    {
      return 1;
    }
    return collator.compare(firstTherapy.getTherapyDescription(), secondTherapy.getTherapyDescription());
  }

  boolean wasTherapyModifiedFromLastReview(
      final MedicationInstructionInstruction instruction,
      final List<MedicationActionAction> actionsList,
      final DateTime linksCreateTimestamp)
  {
    final List<Link> instructionLinks = MedicationsEhrUtils.getLinksOfType(instruction, OpenEhrLinkType.UPDATE);
    if (instructionLinks.isEmpty())
    {
      return false;
    }
    for (final MedicationActionAction action : actionsList)
    {
      final MedicationActionEnum actionEnum =
          MedicationActionEnum.getAction(
              action.getIsmTransition().getCareflowStep(),
              action.getIsmTransition().getCurrentState());
      final DateTime actionDateTime = DataValueUtils.getDateTime(action.getTime());
      if (actionEnum == MedicationActionEnum.REVIEW && !actionDateTime.isBefore(linksCreateTimestamp))
      {
        return false;
      }
    }
    return true;
  }

  @Override
  public TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final RoundsIntervalDto roundsInterval,
      final DateTime when)
  {
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);

    final TherapyReloadAfterActionDto reloadDto = new TherapyReloadAfterActionDto();
    reloadDto.setEhrCompositionId(composition.getUid().getValue());
    reloadDto.setEhrOrderName(instruction.getName().getValue());
    final DateTime therapyStart = getTherapyStart(patientId, instruction);
    final DateTime therapyEnd =
        DataValueUtils.getDateTime(instruction.getOrder().get(0).getMedicationTiming().getStopDate());

    reloadDto.setTherapyStart(therapyStart);
    reloadDto.setTherapyEnd(therapyEnd);
    final List<MedicationActionAction> actionsList = getInstructionActions(composition, instruction);
    final TherapyStatusEnum therapyStatus =
        getTherapyStatusFromMedicationAction(actionsList, therapyStart, roundsInterval, Intervals.wholeDay(when), when);
    reloadDto.setTherapyStatus(therapyStatus);
    final boolean therapyActionsAllowed = areTherapyActionsAllowed(therapyStatus, roundsInterval, when);
    reloadDto.setTherapyActionsAllowed(therapyActionsAllowed);
    final boolean therapyEndsBeforeNextRounds =
        doesTherapyEndBeforeNextRounds(therapyEnd, roundsInterval, when);
    reloadDto.setTherapyEndsBeforeNextRounds(therapyEndsBeforeNextRounds);

    return reloadDto;
  }

  @Override
  public int getTherapyConsecutiveDay(
      final DateTime therapyStart,
      final DateTime therapyDay,
      final DateTime currentTime,
      final Integer pastDaysOfTherapy)
  {
    final int pastDays = pastDaysOfTherapy != null ? pastDaysOfTherapy : 0;

    //today
    if (DateUtils.isSameDay(therapyDay.toDate(), currentTime.toDate()))
    {
      return Days.daysBetween(therapyStart, currentTime).getDays() + pastDays;
    }
    return Days.daysBetween(therapyStart.withTimeAtStartOfDay(), therapyDay.withTimeAtStartOfDay()).getDays() + pastDays;
  }

  @Override
  public DateTime getTherapyStart(final long patientId, final String compositionUid, final String ehrOrderName)
  {
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);
    return getTherapyStart(patientId, instruction);
  }

  private DateTime getTherapyStart(final long patientId, final MedicationInstructionInstruction instruction)
  {
    final MedicationInstructionInstruction firstInstruction = getFirstInstruction(patientId, instruction);
    return DataValueUtils.getDateTime(firstInstruction.getOrder().get(0).getMedicationTiming().getStartDate());
  }

  MedicationInstructionInstruction getFirstInstruction(
      final long patientId,
      final MedicationInstructionInstruction instruction)
  {
    MedicationInstructionInstruction firstInstruction = instruction;

    List<Link> updateLinks = MedicationsEhrUtils.getLinksOfType(firstInstruction, OpenEhrLinkType.UPDATE);
    while (!updateLinks.isEmpty())
    {
      firstInstruction = getPreviousInstruction(patientId, firstInstruction).getSecond();
      updateLinks = MedicationsEhrUtils.getLinksOfType(firstInstruction, OpenEhrLinkType.UPDATE);
    }
    return firstInstruction;
  }

  private Pair<MedicationOrderComposition, MedicationInstructionInstruction> getPreviousInstruction(
      final long patientId,
      final MedicationInstructionInstruction instruction)
  {
    final List<Link> updateLinks = MedicationsEhrUtils.getLinksOfType(instruction, OpenEhrLinkType.UPDATE);
    return updateLinks.isEmpty() ? null : getInstructionFromLink(patientId, updateLinks.get(0));
  }

  @Override
  public Pair<MedicationOrderComposition, MedicationInstructionInstruction> getInstructionFromLink(
      final long patientId,
      final Link link)
  {
    final DvEhrUri target = link.getTarget();
    final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(target.getValue());
    final String compositionId = InstructionTranslator.getCompositionUidWithoutVersion(ehrUri.getCompositionId());
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionId);
    final List<Object> instructions = TdoPathable.itemsAtPath(ehrUri.getArchetypePath(), composition);
    return Pair.of(composition, (MedicationInstructionInstruction)instructions.get(0));
  }

  private boolean areTherapyActionsAllowed(
      final TherapyStatusEnum therapyStatus,
      final RoundsIntervalDto roundsInterval,
      final DateTime currentTime)
  {
    final boolean roundsTime = MedicationsEhrUtils.isRoundsTimeForSession(currentTime, roundsInterval);
    final boolean isLate = therapyStatus == TherapyStatusEnum.LATE || therapyStatus == TherapyStatusEnum.VERY_LATE;
    return isLate || (roundsTime && therapyStatus == TherapyStatusEnum.NORMAL) || therapyStatus == TherapyStatusEnum.SUSPENDED;
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

  private TherapyStatusEnum getTherapyStatusFromMedicationAction(
      final List<MedicationActionAction> actionsList,  //sorted by time asc
      final DateTime therapyStart,
      final RoundsIntervalDto roundsInterval,
      final Interval therapyDayInterval,
      final DateTime currentTime)
  {
    final DateTime startOfTodaysRounds =
        currentTime.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getStartHour())
            .plusMinutes(roundsInterval.getStartMinute());
    final DateTime startOfNextDaysRounds = startOfTodaysRounds.plusDays(1);

    final Set<MedicationActionEnum> actionEnumSet = new HashSet<>();
    for (final MedicationActionAction action : actionsList)
    {
      final MedicationActionEnum actionEnum =
          MedicationActionEnum.getAction(
              action.getIsmTransition().getCareflowStep(), action.getIsmTransition().getCurrentState());
      actionEnumSet.add(actionEnum);
    }

    if (actionEnumSet.contains(MedicationActionEnum.CANCEL))
    {
      return TherapyStatusEnum.CANCELLED;
    }
    if (actionEnumSet.contains(MedicationActionEnum.ABORT))
    {
      return TherapyStatusEnum.ABORTED;
    }
    final boolean suspended = isTherapySuspended(actionsList);
    if (suspended)
    {
      return TherapyStatusEnum.SUSPENDED;
    }
    if (therapyStart.isAfter(startOfNextDaysRounds))
    {
      return TherapyStatusEnum.FUTURE;
    }
    final boolean reviewed = isTherapyReviewed(actionsList, therapyDayInterval);
    if (reviewed)
    {
      return TherapyStatusEnum.REVIEWED;
    }

    final int minuteOfDay = currentTime.getMinuteOfDay();
    final int minuteOfRoundsEnd = roundsInterval.getEndHour() * 60 + roundsInterval.getEndMinute();
    final boolean therapyStartBeforeTodaysRounds = therapyStart.isBefore(startOfTodaysRounds);
    if (therapyStartBeforeTodaysRounds && minuteOfDay > minuteOfRoundsEnd - 60 && minuteOfDay < minuteOfRoundsEnd)
    {
      return TherapyStatusEnum.LATE;
    }
    if (therapyStartBeforeTodaysRounds && minuteOfDay > minuteOfRoundsEnd)
    {
      return TherapyStatusEnum.VERY_LATE;
    }
    return TherapyStatusEnum.NORMAL;
  }

  private boolean isTherapySuspended(final List<MedicationActionAction> actionsList)  //actions sorted by time ascending
  {
    boolean suspended = false;
    for (final MedicationActionAction action : actionsList)
    {
      final MedicationActionEnum actionEnum =
          MedicationActionEnum.getAction(
              action.getIsmTransition().getCareflowStep(), action.getIsmTransition().getCurrentState());
      if (actionEnum == MedicationActionEnum.SUSPEND)
      {
        suspended = true;
      }
      else if (actionEnum == MedicationActionEnum.REISSUE)
      {
        suspended = false;
      }
    }
    return suspended;
  }

  private boolean isTherapyReviewed(
      final List<MedicationActionAction> actionsList,   //actions sorted by time ascending
      final Interval searchInterval)
  {
    for (final MedicationActionAction action : actionsList)
    {
      final MedicationActionEnum actionEnum =
          MedicationActionEnum.getAction(
              action.getIsmTransition().getCareflowStep(), action.getIsmTransition().getCurrentState());
      final DateTime actionDateTime = DataValueUtils.getDateTime(action.getTime());
      if (actionEnum == MedicationActionEnum.REVIEW && searchInterval.contains(actionDateTime))
      {
        return true;
      }
    }
    return false;
  }

  private TherapyFlowRowDto findSimilarTherapy(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapy,
      final Map<TherapyFlowRowDto, List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>>> therapyRowsMap,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap)
  {
    TherapyFlowRowDto similarTherapy = null;
    for (final TherapyFlowRowDto therapyFlowRow : therapyRowsMap.keySet())
    {
      for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareTherapy :
          therapyRowsMap.get(therapyFlowRow))
      {
        final boolean therapiesLinkedByEdit = areInstructionsLinkedByUpdate(therapy, compareTherapy);
        if (therapiesLinkedByEdit)
        {
          return therapyFlowRow;
        }
      }

      for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareTherapy :
          therapyRowsMap.get(therapyFlowRow))
      {
        final boolean similar = areTherapiesSimilar(therapy, compareTherapy, medicationsMap, false);
        if (similar)
        {
          similarTherapy = therapyFlowRow;
        }
      }
    }
    return similarTherapy;
  }

  @Override
  public boolean areInstructionsLinkedByUpdate(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair)
  {
    if (doesInstructionHaveLinkToCompareInstruction(
        instructionPair.getSecond(), compareInstructionPair, OpenEhrLinkType.UPDATE))
    {
      return true;
    }
    if (doesInstructionHaveLinkToCompareInstruction(
        compareInstructionPair.getSecond(), instructionPair, OpenEhrLinkType.UPDATE))
    {
      return true;
    }
    return false;
  }

  @Override
  public boolean doesInstructionHaveLinkToCompareInstruction(
      final MedicationInstructionInstruction instruction,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair,
      final OpenEhrLinkType linkType)
  {
    for (final Link link : instruction.getLinks())
    {
      if (link.getType().getValue().equals(linkType.getName()))
      {
        final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(link.getTarget().getValue());
        final MedicationOrderComposition compareComposition = compareInstructionPair.getFirst();
        final MedicationInstructionInstruction compareInstruction = compareInstructionPair.getSecond();
        if (InstructionTranslator.getCompositionUidWithoutVersion(ehrUri.getCompositionId()).equals(
            InstructionTranslator.getCompositionUidWithoutVersion(compareComposition.getUid().getValue())))
        {
          final List<Object> linkedInstructions = TdoPathable.itemsAtPath(ehrUri.getArchetypePath(), compareComposition);
          if (!linkedInstructions.isEmpty() && linkedInstructions.get(0).equals(compareInstruction))
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  boolean areTherapiesSimilar(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap,
      final boolean canOverlap)
  {
    final OrderActivity orderActivity = instructionPair.getSecond().getOrder().get(0);
    final OrderActivity compareOrderActivity = compareInstructionPair.getSecond().getOrder().get(0);

    final boolean simpleTherapy = MedicationsEhrUtils.isSimpleTherapy(orderActivity);
    final Interval therapyInterval = getInstructionInterval(orderActivity.getMedicationTiming());
    final Interval therapyOrderInterval = getInstructionInterval(compareOrderActivity.getMedicationTiming());
    if (canOverlap || !therapyInterval.overlaps(therapyOrderInterval))
    {
      final boolean compareTherapyIsSimple = MedicationsEhrUtils.isSimpleTherapy(compareOrderActivity);
      if (simpleTherapy && compareTherapyIsSimple)
      {
        final boolean similarSimpleTherapy = isSimilarSimpleTherapy(orderActivity, compareOrderActivity, medicationsMap);
        if (similarSimpleTherapy)
        {
          return true;
        }
      }
      if (!simpleTherapy && !compareTherapyIsSimple)
      {
        final boolean similarComplexTherapy = isSimilarComplexTherapy(orderActivity, compareOrderActivity, medicationsMap);
        if (similarComplexTherapy)
        {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isSimilarSimpleTherapy(
      final OrderActivity orderActivity,
      final OrderActivity compareOrderActivity,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap)
  {
    //therapy
    final String route = orderActivity.getAdministrationDetails().getRoute().get(0).getValue();
    final Long medicationId = getMainMedicationId(orderActivity);
    final String medicationName = orderActivity.getMedicine() != null ? orderActivity.getMedicine().getValue() : null;

    //compare therapy
    final String compareRoute = compareOrderActivity.getAdministrationDetails().getRoute().get(0).getValue();
    final Long compareMedicationId = getMainMedicationId(compareOrderActivity);
    final String compareMedicationName = compareOrderActivity.getMedicine() != null ? compareOrderActivity.getMedicine().getValue() : null;

    final boolean similarMedication =
        isSimilarMedication(medicationId, medicationName, compareMedicationId, compareMedicationName, medicationsMap);
    final boolean sameRoute = route.equals(compareRoute);

    return similarMedication && sameRoute;
  }

  private boolean isSimilarMedication(
      final Long medicationId,
      final String medicationName,
      final Long compareMedicationId,
      final String compareMedicationName,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap)
  {
    final MedicationDataForTherapyDto medicationData = medicationId != null ? medicationsMap.get(medicationId) : null;
    final MedicationDataForTherapyDto compareMedicationData =
        compareMedicationId != null ? medicationsMap.get(compareMedicationId) : null;

    final boolean sameGeneric = isSameGeneric(medicationData, compareMedicationData);
    final boolean sameName = medicationName != null && medicationName.equals(compareMedicationName);

    final boolean sameAtc = isSameAtc(medicationData, compareMedicationData);
    final boolean sameCustomGroup = isSameOrNoCustomGroup(medicationData, compareMedicationData);

    return (sameGeneric || sameName) && sameAtc && sameCustomGroup;
  }

  private boolean isSameGeneric(
      final MedicationDataForTherapyDto medication, final MedicationDataForTherapyDto compareMedication)
  {
    return medication != null && compareMedication != null &&
        medication.getGenericName() != null && compareMedication.getGenericName() != null &&
        medication.getGenericName().equals(compareMedication.getGenericName());
  }

  private boolean isSameAtc(
      final MedicationDataForTherapyDto medication, final MedicationDataForTherapyDto compareMedication)
  {
    return medication != null && compareMedication != null &&
        ((medication.getAtcCode() == null && compareMedication.getAtcCode() == null) ||
            medication.getAtcCode() != null && compareMedication.getAtcCode() != null &&
                medication.getAtcCode().equals(compareMedication.getAtcCode()));
  }

  private boolean isSameOrNoCustomGroup(
      final MedicationDataForTherapyDto medication, final MedicationDataForTherapyDto compareMedication)
  {
    return medication != null && compareMedication != null &&
        ((medication.getCustomGroupName() == null && compareMedication.getCustomGroupName() == null) ||
            (medication.getCustomGroupName() != null && compareMedication.getCustomGroupName() != null &&
                medication.getCustomGroupName().equals(compareMedication.getCustomGroupName()))
        );
  }

  private boolean isSimilarComplexTherapy(
      final OrderActivity orderActivity,
      final OrderActivity compareOrderActivity,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap)
  {
    final boolean therapyBaselineInfusion = isBaselineInfusion(orderActivity);
    final boolean compareTherapyBaselineInfusion = isBaselineInfusion(orderActivity);

    if (therapyBaselineInfusion && compareTherapyBaselineInfusion)
    {
      return true;
    }

    if (orderActivity.getIngredientsAndForm().getIngredient().size() == 1 &&
        compareOrderActivity.getIngredientsAndForm().getIngredient().size() == 1)
    {
      //therapy
      final String route = orderActivity.getAdministrationDetails().getRoute().get(0).getValue();
      final Long medicationId = getMainMedicationId(orderActivity);
      final String medicationName = orderActivity.getIngredientsAndForm().getIngredient().get(0).getName().getValue();

      //compare therapy
      final String compareRoute = compareOrderActivity.getAdministrationDetails().getRoute().get(0).getValue();
      final Long compareMedicationId = getMainMedicationId(compareOrderActivity);
      final String compareMedicationName = compareOrderActivity.getIngredientsAndForm().getIngredient().get(0).getName().getValue();

      final boolean sameRoute = route.equals(compareRoute);
      final boolean similarMedication =
          isSimilarMedication(medicationId, medicationName, compareMedicationId, compareMedicationName, medicationsMap);

      return sameRoute && similarMedication;
    }
    return false;
  }

  private boolean isBaselineInfusion(final OrderActivity orderActivity)
  {
    final AdministrationDetailsCluster administration = orderActivity.getAdministrationDetails();

    if (!administration.getInfusionAdministrationDetails().isEmpty())
    {
      final InfusionAdministrationDetailsCluster infusionDetails = administration.getInfusionAdministrationDetails().get(0);
      return infusionDetails.getPurposeEnum() ==
          MedicationOrderComposition.MedicationDetailSection.InfusionAdministrationDetailsPurpose.BASELINE_ELECTROLYTE_INFUSION;
    }
    return false;
  }

  private List<Long> getMedicationIds(final OrderActivity orderActivity)
  {
    final List<Long> medicationIds = new ArrayList<>();
    final boolean simpleTherapy = MedicationsEhrUtils.isSimpleTherapy(orderActivity);
    if (simpleTherapy)
    {
      if (orderActivity.getMedicine() instanceof DvCodedText)
      {
        final String definingCode = ((DvCodedText)orderActivity.getMedicine()).getDefiningCode().getCodeString();
        final Long medicationId = Long.parseLong(definingCode);
        medicationIds.add(medicationId);
      }
    }
    else
    {
      for (final IngredientCluster ingredient : orderActivity.getIngredientsAndForm().getIngredient())
      {
        if (ingredient.getName() instanceof DvCodedText)
        {
          final String definingCode = ((DvCodedText)ingredient.getName()).getDefiningCode().getCodeString();
          final Long medicationId = Long.parseLong(definingCode);
          medicationIds.add(medicationId);
        }
      }
    }
    return medicationIds;
  }

  private Long getMainMedicationId(final OrderActivity orderActivity)
  {
    if (orderActivity.getIngredientsAndForm() != null && !orderActivity.getIngredientsAndForm().getIngredient().isEmpty())
    {
      final IngredientCluster ingredient = orderActivity.getIngredientsAndForm().getIngredient().get(0);
      if (ingredient.getName() instanceof DvCodedText)
      {
        final String definingCode = ((DvCodedText)ingredient.getName()).getDefiningCode().getCodeString();
        return Long.parseLong(definingCode);
      }
      return null;
    }
    if (orderActivity.getMedicine() instanceof DvCodedText) //if DvCodedText then medication exists in database
    {
      final String definingCode = ((DvCodedText)orderActivity.getMedicine()).getDefiningCode().getCodeString();
      return Long.parseLong(definingCode);
    }
    return null;
  }

  @Override
  public Long getMainMedicationId(final TherapyDto therapy)
  {
    if (therapy instanceof SimpleTherapyDto)
    {
      return ((SimpleTherapyDto)therapy).getMedication().getId();
    }

    if (therapy instanceof ComplexTherapyDto)
    {
      return ((ComplexTherapyDto)therapy).getIngredientsList().get(0).getMedication().getId();
    }

    return null;
  }

  @Override
  public void saveConsecutiveDays(
      final Long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final Long userId,
      final Integer consecutiveDays)
  {
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);
    final NamedIdentity usersName = medicationsConnector.getUsersName(userId, DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()));

    updateConsecutiveDays(instruction, consecutiveDays);
    updateExistingContext(composition, usersName);
    ehrMedicationsDao.modifyMedicationOrderComposition(patientId, composition);
  }

  private void updateConsecutiveDays(final MedicationInstructionInstruction instruction, final Integer consecutiveDays)
  {
    for (final OrderActivity order : instruction.getOrder())
    {
      if (consecutiveDays != null)
      {
        order.setPastDaysOfTherapy(new DvCount());
        order.getPastDaysOfTherapy().setMagnitude(consecutiveDays);
      }
      else
      {
        order.setPastDaysOfTherapy(null);
      }
    }
  }

  private Interval getInstructionInterval(final MedicationTimingCluster medicationTiming)
  {
    final DateTime start = DataValueUtils.getDateTime(medicationTiming.getStartDate());
    final DateTime stop =
        medicationTiming.getStopDate() != null ?
        DataValueUtils.getDateTime(medicationTiming.getStopDate()) :
        null;

    return stop != null ? new Interval(start, stop) : Intervals.infiniteFrom(start);
  }

  private boolean isOnlyOnceThenEx(final MedicationTimingCluster medicationTiming)
  {
    return
        medicationTiming.getTimingDescription() != null &&
            medicationTiming.getTimingDescription().getValue().equals(
                DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.ONCE_THEN_EX));
  }

  @Override
  public boolean isTherapyActive(
      final List<String> daysOfWeek,
      final Integer dosingDaysFrequency,
      final Interval therapyInterval,
      final DateTime when)
  {
    if (therapyInterval.overlap(Intervals.wholeDay(when)) == null)
    {
      return false;
    }
    if (daysOfWeek != null)
    {
      boolean activeDay = false;
      final String searchDay = MedicationsEhrUtils.dayOfWeekToEhrEnum(when).name();
      for (final String day : daysOfWeek)
      {
        if (day.equals(searchDay))
        {
          activeDay = true;
        }
      }
      if (!activeDay)
      {
        return false;
      }
    }
    if (dosingDaysFrequency != null)
    {
      final int daysFromStart =
          Days.daysBetween(therapyInterval.getStart().withTimeAtStartOfDay(), when.withTimeAtStartOfDay()).getDays();
      if (daysFromStart % dosingDaysFrequency != 0)
      {
        return false;
      }
    }
    return true;
  }

  @Override
  public <M extends TherapyDto> Pair<MedicationOrderComposition, MedicationInstructionInstruction> modifyTherapy(
      final long patientId,
      final M therapy,
      final Long centralCaseId,
      final Long careProviderId,
      final RoundsIntervalDto roundsInterval,
      final long userId,
      final NamedIdentity prescriber,
      final DateTime when,
      final boolean alwaysOverrideTherapy,
      final Locale locale)
  {
    final DateTime updateTime = new DateTime(when.withSecondOfMinute(0).withMillisOfSecond(0));
    therapyDisplayProvider.fillDisplayValues(therapy, true, true, locale);
    final MedicationOrderComposition oldComposition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, therapy.getCompositionUid());
    final MedicationInstructionInstruction oldInstruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(oldComposition, therapy.getEhrOrderName());

    final MedicationToEhrConverter<M> converter =
        (MedicationToEhrConverter<M>)MedicationConverterSelector.getConverter(therapy);
    final NamedIdentity usersName = medicationsConnector.getUsersName(userId, when);

    //if therapy not started yet or alwaysOverrideTherapy is true
    if (alwaysOverrideTherapy ||
        DataValueUtils.getDateTime(
            oldInstruction.getOrder().get(0).getMedicationTiming().getStartDate()).isAfter(updateTime))
    {
      //fix start
      converter.fillInstructionFromTherapy(oldInstruction, therapy);
      final MedicationActionAction startAction =
          getInstructionAction(oldComposition, oldInstruction, MedicationActionEnum.START, null);
      startAction.setTime(DataValueUtils.getDateTime(updateTime));

      final boolean therapySuspended = isTherapySuspended(oldComposition, oldInstruction);
      if (therapySuspended)
      {
        MedicationsEhrUtils.addMedicationActionTo(oldComposition, oldInstruction, MedicationActionEnum.REISSUE, when);
      }
      final MedicationActionAction reviewAction =
          getInstructionAction(oldComposition, oldInstruction, MedicationActionEnum.REVIEW, Intervals.wholeDay(updateTime));
      if (reviewAction == null && roundsInterval != null)
      {
        reviewTherapyIfInRoundsInterval(patientId, oldComposition, oldInstruction, roundsInterval, updateTime);
      }
      updateExistingContext(oldComposition, usersName, prescriber);
      ehrMedicationsDao.modifyMedicationOrderComposition(patientId, oldComposition);
      return Pair.of(oldComposition, oldInstruction);
    }
    else
    {
      //create new instruction
      final MedicationOrderComposition newComposition = MedicationsEhrUtils.createEmptyMedicationOrderComposition();
      final MedicationInstructionInstruction newInstruction = converter.createInstructionFromTherapy(therapy);
      newComposition.getMedicationDetail().getMedicationInstruction().add(newInstruction);

      //link new instruction to old instruction
      final Link linkToExisting =
          OpenEhrRefUtils.getLinkToTdoTarget("original", OpenEhrLinkType.UPDATE, oldComposition, oldInstruction);
      newInstruction.getLinks().add(linkToExisting);

      //add SCHEDULE and START actions
      final DateTime therapyStart = therapy.getStart().isAfter(updateTime) ? therapy.getStart() : updateTime;
      MedicationsEhrUtils.addMedicationActionTo(newComposition, newInstruction, MedicationActionEnum.SCHEDULE, updateTime);
      MedicationsEhrUtils.addMedicationActionTo(newComposition, newInstruction, MedicationActionEnum.START, therapyStart);

      if (roundsInterval != null)
      {
        reviewTherapyIfInRoundsInterval(patientId, newComposition, newInstruction, roundsInterval, updateTime);
      }
      addContext(newComposition, usersName, prescriber, centralCaseId, careProviderId, when);
      final String newCompositionUid =
          ehrMedicationsDao.saveNewMedicationOrderComposition(patientId, newComposition).getUid().getValue();
      newComposition.setUid((OpenEhrRefUtils.getObjectVersionId(newCompositionUid)));
      newInstruction.setName(DataValueUtils.getText("Medication instruction"));
      //old composition
      final DateTime oldCompositionStopDate = updateTime.isBefore(therapy.getStart()) ? updateTime : therapy.getStart();
      for (final OrderActivity orderActivity : oldInstruction.getOrder())
      {
        orderActivity.getMedicationTiming().setStopDate(DataValueUtils.getDateTime(oldCompositionStopDate));
      }
      MedicationsEhrUtils.addMedicationActionTo(oldComposition, oldInstruction, MedicationActionEnum.COMPLETE, updateTime);
      updateExistingContext(oldComposition, usersName, prescriber);
      ehrMedicationsDao.modifyMedicationOrderComposition(patientId, oldComposition);
      return Pair.of(newComposition, newInstruction);
    }
  }

  @Override
  public boolean isTherapySuspended(
      final MedicationOrderComposition composition, final MedicationInstructionInstruction instruction)
  {
    final List<MedicationActionAction> actions = getInstructionActions(composition, instruction);
    return isTherapySuspended(actions);
  }

  private boolean reviewTherapyIfInRoundsInterval(
      final long patientId,
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final RoundsIntervalDto roundsInterval,
      final DateTime when)
  {
    final DateTime therapyStart = getTherapyStart(patientId, instruction);
    final DateTime startOfTodaysRounds =
        when.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getStartHour())
            .plusMinutes(roundsInterval.getStartMinute());
    final DateTime endOfTodaysRounds =
        when.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getEndHour())
            .plusMinutes(roundsInterval.getEndMinute());
    final Interval todaysRoundsInterval = new Interval(startOfTodaysRounds, endOfTodaysRounds);

    final DateTime endOfLastRounds =
        when.isBefore(endOfTodaysRounds) ? endOfTodaysRounds.minusDays(1) : endOfTodaysRounds;
    if (todaysRoundsInterval.contains(when) || therapyStart.isBefore(endOfLastRounds))
    {
      MedicationsEhrUtils.addMedicationActionTo(composition, instruction, MedicationActionEnum.REVIEW, when);
      return true;
    }
    return false;
  }

  @Override
  public boolean isMedicationTherapyCompleted(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    final List<MedicationActionAction> instructionActions = getInstructionActions(composition, instruction);
    for (final MedicationActionAction action : instructionActions)
    {
      final MedicationActionEnum actionEnum =
          MedicationActionEnum.getAction(
              action.getIsmTransition().getCareflowStep(),
              action.getIsmTransition().getCurrentState());

      if (MedicationActionEnum.THERAPY_FINISHED.contains(actionEnum))
      {
        return true;
      }
    }
    return false;
  }

  private List<MedicationActionAction> getInstructionActions(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    final String instructionPath = TdoPathable.pathOfItem(composition, instruction).getCanonicalString();
    final List<MedicationActionAction> actionsList = new ArrayList<>();
    for (final MedicationActionAction action : composition.getMedicationDetail().getMedicationAction())
    {
      if (action.getInstructionDetails().getInstructionId().getPath().equals(instructionPath))
      {
        actionsList.add(action);
      }
    }

    Collections.sort(
        actionsList, new Comparator<MedicationActionAction>()
        {
          @Override
          public int compare(
              final MedicationActionAction action1,
              final MedicationActionAction action2)
          {
            return DataValueUtils.getDateTime(action1.getTime()).compareTo(DataValueUtils.getDateTime(action2.getTime()));
          }
        }
    );
    return actionsList;
  }

  private MedicationActionAction getInstructionAction(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final MedicationActionEnum searchActionEnum,
      @Nullable final Interval searchInterval)
  {
    final String instructionPath = TdoPathable.pathOfItem(composition, instruction).getCanonicalString();

    for (final MedicationActionAction action : composition.getMedicationDetail().getMedicationAction())
    {
      if (action.getInstructionDetails().getInstructionId().getPath().equals(instructionPath))
      {
        final MedicationActionEnum actionEnum =
            MedicationActionEnum.getAction(
                action.getIsmTransition().getCareflowStep(),
                action.getIsmTransition().getCurrentState());
        if (searchInterval != null)
        {
          final DateTime actionDateTime = DataValueUtils.getDateTime(action.getTime());
          if (searchInterval.contains(actionDateTime))
          {
            if (actionEnum == searchActionEnum)
            {
              return action;
            }
          }
        }
        else
        {
          if (actionEnum == searchActionEnum)
          {
            return action;
          }
        }
      }
    }
    return null;
  }

  @Override
  public void abortTherapy(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final long userId,
      final DateTime when)
  {
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);

    abortTherapy(patientId, composition, instruction, userId, when);
  }

  @Override
  public void abortTherapy(
      final long patientId,
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final long userId,
      final DateTime when)
  {
    final DateTime medicationTimingStart =
        DataValueUtils.getDateTime(instruction.getOrder().get(0).getMedicationTiming().getStartDate());
    final DateTime medicationTimingEnd =
        DataValueUtils.getDateTime(instruction.getOrder().get(0).getMedicationTiming().getStopDate());
    final boolean continuousInfusion = MedicationsEhrUtils.isContinuousInfusion(instruction);

    //if medication not started yet
    if (medicationTimingStart.isAfter(when))
    {
      for (final OrderActivity orderActivity : instruction.getOrder())
      {
        orderActivity.getMedicationTiming().setStartDate(DataValueUtils.getDateTime(when));
        orderActivity.getMedicationTiming().setStopDate(DataValueUtils.getDateTime(when));
      }
      MedicationsEhrUtils.addMedicationActionTo(composition, instruction, MedicationActionEnum.CANCEL, when);
    }
    else
    {
      if (medicationTimingEnd == null || medicationTimingEnd.isAfter(when))
      {
        for (final OrderActivity orderActivity : instruction.getOrder())
        {
          orderActivity.getMedicationTiming().setStopDate(DataValueUtils.getDateTime(when));
        }
      }
      MedicationsEhrUtils.addMedicationActionTo(composition, instruction, MedicationActionEnum.ABORT, when);
    }

    final NamedIdentity usersName = medicationsConnector.getUsersName(userId, when);
    updateExistingContext(composition, usersName);
    ehrMedicationsDao.modifyMedicationOrderComposition(patientId, composition);
  }

  @Override
  public String reviewTherapy(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final long userId,
      final DateTime when)
  {
    return addMedicationActionAndSaveComposition(
        patientId,
        compositionUid,
        ehrOrderName,
        MedicationActionEnum.REVIEW,
        userId,
        when);
  }

  @Override
  public String suspendTherapy(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final long userId,
      final DateTime when)
  {
    return addMedicationActionAndSaveComposition(
        patientId,
        compositionUid,
        ehrOrderName,
        MedicationActionEnum.SUSPEND,
        userId,
        when);
  }

  @Override
  public void reissueTherapy(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final RoundsIntervalDto roundsInterval,
      final long userId,
      final DateTime when)
  {
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);

    MedicationsEhrUtils.addMedicationActionTo(composition, instruction, MedicationActionEnum.REISSUE, when);

    final MedicationActionAction reviewAction =
        getInstructionAction(composition, instruction, MedicationActionEnum.REVIEW, Intervals.wholeDay(when));
    if (reviewAction == null)
    {
      reviewTherapyIfInRoundsInterval(patientId, composition, instruction, roundsInterval, when);
    }
    final NamedIdentity usersName = medicationsConnector.getUsersName(userId, when);
    updateExistingContext(composition, usersName);
    ehrMedicationsDao.modifyMedicationOrderComposition(patientId, composition);
  }

  @Override
  public String suspendTherapy(
      final long patientId,
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final long userId,
      final DateTime when)
  {
    MedicationsEhrUtils.addMedicationActionTo(composition, instruction, MedicationActionEnum.SUSPEND, when);

    final NamedIdentity usersName = medicationsConnector.getUsersName(userId, when);
    updateExistingContext(composition, usersName);
    return ehrMedicationsDao.modifyMedicationOrderComposition(patientId, composition);
  }

  private String addMedicationActionAndSaveComposition(
      final long patientId,
      final String compositionUid,
      final String ehrOrderName,
      final MedicationActionEnum actionEnum,
      final long userId,
      final DateTime when)
  {
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);

    MedicationsEhrUtils.addMedicationActionTo(composition, instruction, actionEnum, when);

    final NamedIdentity usersName = medicationsConnector.getUsersName(userId, when);
    updateExistingContext(composition, usersName);
    return ehrMedicationsDao.modifyMedicationOrderComposition(patientId, composition);
  }

  @Override
  public String getTherapyFormattedDisplay(
      final long patientId, final String therapyId, final DateTime when, final Locale locale)
  {
    final TherapyDto therapy = getTherapy(patientId, therapyId, when, locale);
    return therapy.getFormattedTherapyDisplay();
  }

  private TherapyDto getTherapy(
      final long patientId, final String therapyId, final DateTime when, final Locale locale)
  {
    final Pair<String, String> compositionIdAndInstructionName =
        InstructionTranslator.getCompositionIdAndInstructionName(therapyId);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> medicationInstructionPair =
        loadMedicationInstructionPair(
            patientId, compositionIdAndInstructionName.getFirst(), compositionIdAndInstructionName.getSecond());
    return convertInstructionToTherapyDto(
        medicationInstructionPair.getFirst(),
        medicationInstructionPair.getSecond(),
        null,
        null,
        when,
        true,
        locale);
  }

  private Pair<MedicationOrderComposition, MedicationInstructionInstruction> loadMedicationInstructionPair(
      final long patientId,
      final String compositionId,
      final String ehrOrderName)
  {
    final MedicationOrderComposition composition =
        ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionId);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);
    return Pair.of(composition, instruction);
  }

  @Override
  public TherapyDto convertInstructionToTherapyDto(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final Double referenceWeight,
      final Double patientHeight,
      final DateTime currentTime,
      final boolean isToday,
      final Locale locale)
  {
    final MedicationFromEhrConverter<?> converter = MedicationConverterSelector.getConverter(instruction);

    final TherapyDto therapyDto = converter.createTherapyFromInstruction(
        instruction,
        composition.getUid().getValue(),
        instruction.getName().getValue(),
        DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
        currentTime,
        this);

    final List<Link> issueLinks = MedicationsEhrUtils.getLinksOfType(instruction, OpenEhrLinkType.ISSUE);
    if (!issueLinks.isEmpty())
    {
      final Link link = issueLinks.get(0);
      final DvEhrUri target = link.getTarget();
      final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(target.getValue());
      //final String compositionId = InstructionTranslator.getCompositionUidWithoutVersion(ehrUri.getCompositionId());
      //final MedicationOrderComposition composition =
      //    ehrMedicationsDao.loadMedicationOrderComposition(patientId, compositionId);
      final List<Object> linkedInstructions = TdoPathable.itemsAtPath(ehrUri.getArchetypePath(), composition);

      final String therapyId = InstructionTranslator.createId(
          ((MedicationInstructionInstruction)linkedInstructions.get(0)).getName().getValue(),
          composition.getUid().getValue());
      therapyDto.setLinkFromTherapy(therapyId);
    }

    final PartyIdentified composer = (PartyIdentified)composition.getComposer();
    therapyDto.setComposerName(composer.getName());
    if (!instruction.getOtherParticipations().isEmpty())
    {
      final PartyProxy performer = instruction.getOtherParticipations().get(0).getPerformer();
      therapyDto.setPrescriberName(((PartyIdentified)performer).getName());
    }

    if (therapyDto instanceof ComplexTherapyDto && referenceWeight != null)
    {
      fillInfusionFormulaFromRate((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight, currentTime);
    }

    if (locale != null)
    {
      therapyDisplayProvider.fillDisplayValues(therapyDto, isToday, true, locale);
    }
    return therapyDto;
  }

  @Override
  public void fillInfusionFormulaFromRate(final ComplexTherapyDto therapy, final Double referenceWeight, final Double patientHeight, final DateTime when)
  {
    final InfusionRateCalculationDto calculationData = getInfusionRateCalculationData(therapy, when);
    if (calculationData != null)
    {
      if (therapy instanceof ConstantComplexTherapyDto)
      {
        final ConstantComplexTherapyDto constantTherapy = (ConstantComplexTherapyDto)therapy;
        final ComplexDoseElementDto doseElement = constantTherapy.getDoseElement();
        if (doseElement != null && doseElement.getRate() != null && doseElement.getRateFormulaUnit() != null)
        {
          final Double formula = calculateInfusionFormulaFromRate(
              doseElement.getRate(),
              calculationData,
              patientHeight,
              doseElement.getRateFormulaUnit(),
              referenceWeight);
          doseElement.setRateFormula(formula);
        }
      }
      else
      {
        final VariableComplexTherapyDto variableTherapy = (VariableComplexTherapyDto)therapy;
        for (final TimedComplexDoseElementDto timedDoseElement : variableTherapy.getTimedDoseElements())
        {
          if (timedDoseElement.getDoseElement() != null &&
              timedDoseElement.getDoseElement().getRate() != null &&
              timedDoseElement.getDoseElement().getRateFormulaUnit() != null)
          {
            final Double formula = calculateInfusionFormulaFromRate(
                timedDoseElement.getDoseElement().getRate(),
                calculationData,
                patientHeight,
                timedDoseElement.getDoseElement().getRateFormulaUnit(),
                referenceWeight);
            timedDoseElement.getDoseElement().setRateFormula(formula);
          }
        }
      }
    }
  }

  Double calculateInfusionFormulaFromRate(
      final Double rate,
      final InfusionRateCalculationDto calculationDto,
      final Double patientHeight,
      final String formulaUnit,
      final Double referenceWeight)
  {
    final String[] formulaUnitParts = Pattern.compile("/").split(formulaUnit);     // (ug/kg/min)
    final String formulaMassUnit;
    String formulaPatientUnit = null;
    final String formulaTimeUnit;

    if (formulaUnitParts.length == 2)
    {
      formulaMassUnit = formulaUnitParts[0];
      formulaTimeUnit = formulaUnitParts[1];
    }
    else
    {
      formulaMassUnit = formulaUnitParts[0];
      formulaPatientUnit = formulaUnitParts[1];
      formulaTimeUnit = formulaUnitParts[2];
    }

    final Double rateWithPatientUnit;
    if (formulaPatientUnit != null && formulaPatientUnit.equals("kg"))
    {
      rateWithPatientUnit = rate / referenceWeight;  // ml/kg/h
    }
    else if (formulaPatientUnit != null && formulaPatientUnit.equals("m2") && patientHeight != null)
    {
      final Double bodySurface = calculateBodySurfaceArea(patientHeight, referenceWeight);
      rateWithPatientUnit = rate / bodySurface;
    }
    else
    {
      rateWithPatientUnit = rate;
    }

    final Double rateInMassUnit = rateWithPatientUnit * calculationDto.getQuantity() / calculationDto.getVolume(); // mg/kg/h
    final Double rateInFormulaMassUnit =
        TherapyUnitsConverter.convertToUnit(rateInMassUnit, calculationDto.getQuantityUnit(), formulaMassUnit); // ug/kg/h
    if (rateInFormulaMassUnit != null)
    {
      final Double timeRatio = TherapyUnitsConverter.convertToUnit(1.0, "h", formulaTimeUnit);
      return rateInFormulaMassUnit / timeRatio;  // ug/kg/min
    }
    return null;
  }

  @Override
  public void fillInfusionRateFromFormula(
      final ComplexTherapyDto therapy, final Double referenceWeight, final Double patientHeight, final DateTime when)
  {
    final InfusionRateCalculationDto calculationData = getInfusionRateCalculationData(therapy, when);
    if (calculationData != null)
    {
      if (therapy instanceof ConstantComplexTherapyDto)
      {
        final ConstantComplexTherapyDto constantTherapy = (ConstantComplexTherapyDto)therapy;
        final ComplexDoseElementDto doseElement = constantTherapy.getDoseElement();
        if (doseElement != null && doseElement.getRateFormula() != null && doseElement.getRateFormulaUnit() != null)
        {
          final Double rate = calculateInfusionRateFromFormula(
              doseElement.getRateFormula(),
              doseElement.getRateFormulaUnit(),
              calculationData,
              referenceWeight,
              patientHeight);
          doseElement.setRate(rate);
        }
      }
      else
      {
        final VariableComplexTherapyDto variableTherapy = (VariableComplexTherapyDto)therapy;
        for (final TimedComplexDoseElementDto timedDoseElement : variableTherapy.getTimedDoseElements())
        {
          if (timedDoseElement.getDoseElement() != null &&
              timedDoseElement.getDoseElement().getRateFormula() != null &&
              timedDoseElement.getDoseElement().getRateFormulaUnit() != null)
          {
            final Double rate = calculateInfusionRateFromFormula(
                timedDoseElement.getDoseElement().getRateFormula(),
                timedDoseElement.getDoseElement().getRateFormulaUnit(),
                calculationData,
                referenceWeight,
                patientHeight);
            timedDoseElement.getDoseElement().setRate(rate);
          }
        }
      }
    }
  }

  Double calculateInfusionRateFromFormula(
      final Double formula,
      final String formulaUnit,
      final InfusionRateCalculationDto calculationDto,
      final Double referenceWeight,
      final Double patientHeight)
  {
    final String[] formulaUnitParts = Pattern.compile("/").split(formulaUnit);     // (ug/kg/min)
    final String formulaMassUnit;
    String formulaPatientUnit = null;
    final String formulaTimeUnit;

    if (formulaUnitParts.length == 2)
    {
      formulaMassUnit = formulaUnitParts[0];
      formulaTimeUnit = formulaUnitParts[1];
    }
    else
    {
      formulaMassUnit = formulaUnitParts[0];
      formulaPatientUnit = formulaUnitParts[1];
      formulaTimeUnit = formulaUnitParts[2];
    }

    final Double formulaWithoutPatientUnit;
    if (formulaPatientUnit != null && formulaPatientUnit.equals("kg"))
    {
      formulaWithoutPatientUnit = formula * referenceWeight;  // ug/min
    }
    else if (formulaPatientUnit != null && formulaPatientUnit.equals("m2") && patientHeight != null)
    {
      final Double bodySurfaceArea = calculateBodySurfaceArea(patientHeight, referenceWeight);
      formulaWithoutPatientUnit = formula * bodySurfaceArea;
    }
    else
    {
      formulaWithoutPatientUnit = formula;
    }

    final Double formulaInRateMassUnit =    // mg/min
        TherapyUnitsConverter.convertToUnit(formulaWithoutPatientUnit, formulaMassUnit, calculationDto.getQuantityUnit());
    if (formulaInRateMassUnit == null)
    {
      return null;
    }
    final Double timeRatio = TherapyUnitsConverter.convertToUnit(1.0, formulaTimeUnit, "h");
    final Double formulaInHours = formulaInRateMassUnit / timeRatio;  // mg/min
    return formulaInHours * calculationDto.getVolume() / calculationDto.getQuantity(); // ml/h
  }

  private Double calculateBodySurfaceArea(final Double heightInCm, final Double weightInKg)
  {
    return Math.sqrt((heightInCm * weightInKg) / 3600.0);
  }

  InfusionRateCalculationDto getInfusionRateCalculationData(final ComplexTherapyDto therapy, final DateTime when)
  {
    if (therapy.getIngredientsList().size() == 1)
    {
      final InfusionIngredientDto onlyInfusionIngredient = therapy.getIngredientsList().get(0);
      if (MedicationTypeEnum.MEDS_AND_SUPPS.contains(onlyInfusionIngredient.getMedication().getMedicationType()))
      {
        if (therapy.isContinuousInfusion() && onlyInfusionIngredient.getMedication().getId() != null)
        {
          final MedicationIngredientDto medicationDefiningIngredient =
              medicationsDao.getMedicationDefiningIngredient(onlyInfusionIngredient.getMedication().getId(), when);
          if (medicationDefiningIngredient != null && "ml".equals(medicationDefiningIngredient.getStrengthDenominatorUnit()))
          {
            final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
            calculationDto.setQuantity(medicationDefiningIngredient.getStrengthNumerator());
            calculationDto.setQuantityUnit(medicationDefiningIngredient.getStrengthNumeratorUnit());
            calculationDto.setVolume(medicationDefiningIngredient.getStrengthDenominator());
            return calculationDto;
          }
        }
        else
        {
          final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
          calculationDto.setQuantity(onlyInfusionIngredient.getQuantity());
          calculationDto.setQuantityUnit(onlyInfusionIngredient.getQuantityUnit());
          calculationDto.setVolume(onlyInfusionIngredient.getVolume());
          return calculationDto;
        }
      }
    }
    else
    {
      InfusionRateCalculationDto calculationDto = null;
      for (final InfusionIngredientDto ingredient : therapy.getIngredientsList())
      {
        final MedicationTypeEnum medicationType = ingredient.getMedication().getMedicationType();
        if (MedicationTypeEnum.MEDS_AND_SUPPS.contains(medicationType))
        {
          if (calculationDto != null)    //more than one medication on supplement
          {
            return null;
          }
          calculationDto = new InfusionRateCalculationDto();
          calculationDto.setQuantity(ingredient.getQuantity());
          calculationDto.setQuantityUnit(ingredient.getQuantityUnit());
          calculationDto.setVolume(therapy.getVolumeSum());
        }
      }
      return calculationDto;
    }
    return null;
  }

  @Override
  public List<MedicationForWarningsSearchDto> getTherapiesForWarningsSearch(final long patientId, final DateTime when)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList =
        ehrMedicationsDao.findMedicationInstructions(patientId, Intervals.infiniteFrom(when), null);
    return extractWarningsSearchDtos(medicationInstructionsList, when);
  }

  @Override
  public List<TherapyDto> getTherapies(
      final long patientId,
      final Long centralCaseId,
      final Double referenceWeight,
      @Nullable final Locale locale,
      final DateTime when)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList =
        ehrMedicationsDao.findMedicationInstructions(patientId, null, centralCaseId);
    final List<TherapyDto> therapiesList = new ArrayList<>();
    fillTherapiesList(therapiesList, medicationInstructionsList, referenceWeight, null, locale, when);
    return therapiesList;
  }

  @Override
  public List<TherapyDto> getTherapies(
      final long patientId,
      final Interval searchInterval,
      final Double referenceWeight,
      final Double patientHeight,
      @Nullable final Locale locale,
      final DateTime when)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList =
        ehrMedicationsDao.findMedicationInstructions(patientId, searchInterval, null);
    final List<TherapyDto> therapiesList = new ArrayList<>();
    fillTherapiesList(therapiesList, medicationInstructionsList, referenceWeight, patientHeight, locale, when);
    return therapiesList;
  }

  private void fillTherapiesList(
      final List<TherapyDto> therapiesList,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList,
      final Double referenceWeight,
      final Double patientHeight,
      @Nullable final Locale locale,
      final DateTime when)
  {
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : medicationInstructionsList)
    {
      final MedicationOrderComposition composition = instructionPair.getFirst();
      final MedicationInstructionInstruction instruction = instructionPair.getSecond();
      final TherapyDto therapyDto =
          getTherapyFromMedicationInstruction(composition, instruction, referenceWeight, patientHeight, when);
      if (locale != null)
      {
        therapyDisplayProvider.fillDisplayValues(therapyDto, true, true, locale);
      }
      therapiesList.add(therapyDto);
    }
  }

  private TherapyDto getTherapyFromMedicationInstruction(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final Double referenceWeight,
      final Double patientHeight,
      final DateTime when)
  {
    final MedicationFromEhrConverter<?> converter = MedicationConverterSelector.getConverter(instruction);
    final TherapyDto therapy = converter.createTherapyFromInstruction(
        instruction,
        composition.getUid().getValue(),
        instruction.getName().getValue(),
        DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
        when,
        this);

    if (therapy instanceof ComplexTherapyDto && referenceWeight != null)
    {
      fillInfusionFormulaFromRate((ComplexTherapyDto)therapy, referenceWeight, patientHeight, when);
    }

    return therapy;
  }

  @Override
  public List<TherapyDto> getPatientTherapiesForReport(
      final long patientId,
      final Double patientHeight,
      final DateTime searchStart,
      final RoundsIntervalDto roundsIntervalDto,
      final DateTime currentTime)
  {
    final DateTime roundsStart = new DateTime(
        searchStart.getYear(),
        searchStart.getMonthOfYear(),
        searchStart.getDayOfMonth(),
        roundsIntervalDto.getStartHour(),
        roundsIntervalDto.getStartMinute());
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList =
        ehrMedicationsDao.findMedicationInstructions(patientId, Intervals.infiniteFrom(roundsStart), null);

    final Double referenceWeight =
        ehrMedicationsDao.getPatientLastReferenceWeight(patientId, Intervals.infiniteTo(searchStart));

    final List<TherapyDto> therapiesList = new ArrayList<>();
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionsList)
    {
      final MedicationOrderComposition composition = instructionPair.getFirst();
      final MedicationInstructionInstruction instruction = instructionPair.getSecond();

      final MedicationTimingCluster medicationTiming = instruction.getOrder().get(0).getMedicationTiming();
      final Interval instructionInterval = getInstructionInterval(medicationTiming);
      final boolean onlyOnce = isOnlyOnceThenEx(medicationTiming);
      if (instructionInterval.overlaps(Intervals.infiniteFrom(searchStart)) || onlyOnce)
      {
        final List<MedicationActionAction> actions = getInstructionActions(composition, instruction);
        if (!isTherapyCanceledAbortedOrSuspended(actions))
        {
          final TherapyDto therapyDto =
              getTherapyFromMedicationInstruction(composition, instruction, referenceWeight, patientHeight, currentTime);
          therapiesList.add(therapyDto);
        }
      }
    }
    return therapiesList;
  }

  private boolean isTherapyCanceledAbortedOrSuspended(final List<MedicationActionAction> actions)
  {
    if (isTherapySuspended(actions))
    {
      return true;
    }
    return isTherapyCanceledOrAborted(actions);
  }

  private boolean isTherapyCanceledOrAborted(final List<MedicationActionAction> actions)
  {
    for (final MedicationActionAction action : actions)
    {
      final MedicationActionEnum actionEnum =
          MedicationActionEnum.getAction(
              action.getIsmTransition().getCareflowStep(),
              action.getIsmTransition().getCurrentState());
      if (actionEnum == MedicationActionEnum.CANCEL || actionEnum == MedicationActionEnum.ABORT)
      {
        return true;
      }
    }
    return false;
  }

  List<MedicationForWarningsSearchDto> extractWarningsSearchDtos(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList,
      final DateTime when)
  {
    final List<MedicationForWarningsSearchDto> medicationSummariesList = new ArrayList<MedicationForWarningsSearchDto>();
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> medicationInstruction : medicationInstructionsList)
    {
      final OrderActivity orderActivity = medicationInstruction.getSecond().getOrder().get(0);

      if (orderActivity.getIngredientsAndForm() != null && !orderActivity.getIngredientsAndForm().getIngredient().isEmpty())
      {
        for (final IngredientCluster ingredient : orderActivity.getIngredientsAndForm().getIngredient())
        {
          final MedicationForWarningsSearchDto medicationDto =
              buildWarningSearchDtoFromIngredient(orderActivity, ingredient, when);
          if (medicationDto != null)
          {
            medicationSummariesList.add(medicationDto);
          }
        }
      }
      else
      {
        final MedicationForWarningsSearchDto medicationDto = buildWarningSearchDtoFromMedication(orderActivity, when);
        if (medicationDto != null)
        {
          medicationSummariesList.add(medicationDto);
        }
      }
    }
    return medicationSummariesList;
  }

  MedicationForWarningsSearchDto buildWarningSearchDtoFromIngredient(
      final OrderActivity orderActivity,
      final IngredientCluster ingredient,
      final DateTime when)
  {
    if (ingredient.getName() instanceof DvCodedText) //if DvCodedText then medication exists in database
    {
      final String definingCode = ((DvCodedText)ingredient.getName()).getDefiningCode().getCodeString();
      final Long medicationId = Long.parseLong(definingCode);

      final IngredientQuantityCluster medicationQuantity = ingredient.getIngredientQuantity();
      Double doseAmount = null;
      String doseUnit = null;
      if (medicationQuantity != null)
      {
        if (medicationQuantity.getQuantity() != null)
        {
          doseAmount = medicationQuantity.getQuantity().getMagnitude();
          doseUnit = medicationQuantity.getDoseUnit().getDefiningCode().getCodeString();
        }
        else if (medicationQuantity.getRatioNumerator() != null)
        {
          doseAmount = medicationQuantity.getRatioNumerator().getAmount().getMagnitude();
          doseUnit = medicationQuantity.getRatioNumerator().getDoseUnit().getDefiningCode().getCodeString();
        }
      }

      final String route = orderActivity.getAdministrationDetails().getRoute().get(0).getDefiningCode().getCodeString();
      return buildWarningSearchDto(orderActivity.getMedicationTiming(), medicationId, route, doseAmount, doseUnit, when);
    }
    return null;
  }

  MedicationForWarningsSearchDto buildWarningSearchDtoFromMedication(final OrderActivity orderActivity, final DateTime when)
  {
    if (orderActivity.getMedicine() instanceof DvCodedText) //if DvCodedText then medication exists in database
    {
      final String definingCode = ((DvCodedText)orderActivity.getMedicine()).getDefiningCode().getCodeString();
      final Long medicationId = Long.parseLong(definingCode);

      Double doseAmount = null;
      String doseUnit = null;
      if (orderActivity.getStructuredDose().getQuantity() != null)
      {
        doseAmount = orderActivity.getStructuredDose().getQuantity().getMagnitude();
        doseUnit = orderActivity.getStructuredDose().getDoseUnit().getDefiningCode().getCodeString();
      }
      else if (orderActivity.getStructuredDose().getRatioNumerator() != null)
      {
        doseAmount = orderActivity.getStructuredDose().getRatioNumerator().getAmount().getMagnitude();
        doseUnit = orderActivity.getStructuredDose().getRatioNumerator().getDoseUnit().getDefiningCode().getCodeString();
      }
      final String route = orderActivity.getAdministrationDetails().getRoute().get(0).getDefiningCode().getCodeString();
      return buildWarningSearchDto(orderActivity.getMedicationTiming(), medicationId, route, doseAmount, doseUnit, when);
    }
    return null;
  }

  MedicationForWarningsSearchDto buildWarningSearchDto(
      final MedicationTimingCluster medicationTimingCluster,
      final Long medicationId,
      final String route,
      final Double doseAmount,
      final String doseUnit,
      final DateTime currentTime)
  {
    final MedicationForWarningsSearchDto summary = new MedicationForWarningsSearchDto();
    final DateTime start = DataValueUtils.getDateTime(medicationTimingCluster.getStartDate());
    final DateTime stop = DataValueUtils.getDateTime(medicationTimingCluster.getStopDate());
    summary.setEffective(
        new Interval(
            start != null ? start : Intervals.INFINITE.getStart(),
            stop != null ? stop : Intervals.INFINITE.getEnd())
    );
    final MedicationDto medicationDto = medicationsDao.getMedicationById(medicationId, currentTime);
    if (medicationDto == null)
    {
      throw new IllegalArgumentException("Medication with id: " + medicationId + " not found!");
    }
    summary.setDescription(medicationDto.getName());
    summary.setId(medicationDto.getId());

    final int dailyFrequency = getMedicationDailyFrequency(medicationTimingCluster);
    summary.setFrequency(dailyFrequency);
    summary.setFrequencyUnit("/d");

    final boolean onlyOnce = isOnlyOnceThenEx(medicationTimingCluster);
    summary.setOnlyOnce(onlyOnce);
    summary.setRouteCode(route);
    summary.setDoseAmount(doseAmount);
    summary.setDoseUnit(doseUnit);
    summary.setProspective(false);
    return summary;
  }

  int getMedicationDailyFrequency(final MedicationTimingCluster medicationTiming)
  {
    if (medicationTiming.getTiming() != null && medicationTiming.getTiming().getDailyCount() != null)
    {
      return Long.valueOf(medicationTiming.getTiming().getDailyCount().getMagnitude()).intValue();
    }
    if (medicationTiming.getTiming() != null && medicationTiming.getTiming().getInterval() != null)
    {
      return 24 / DataValueUtils.getPeriod(medicationTiming.getTiming().getInterval().getValue()).getHours();
    }
    if (medicationTiming.getNumberOfAdministrations() != null)
    {
      return Long.valueOf(medicationTiming.getNumberOfAdministrations().getMagnitude()).intValue();
    }
    if (medicationTiming.getTimingDescription() != null &&
        medicationTiming.getTimingDescription().getValue().equals(
            DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.MORNING)))
    {
      return 1;
    }
    if (medicationTiming.getTimingDescription() != null &&
        medicationTiming.getTimingDescription().getValue().equals(
            DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.NOON)))
    {
      return 1;
    }
    if (medicationTiming.getTimingDescription() != null &&
        medicationTiming.getTimingDescription().getValue().equals(
            DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.EVENING)))
    {
      return 1;
    }
    else
    {
      return 1;
    }
  }

  @Override
  public List<HourMinuteDto> getPossibleAdministrations(
      final AdministrationTimingDto administrationTiming, final String frequency)
  {
    Preconditions.checkNotNull(administrationTiming);
    Preconditions.checkNotNull(frequency);

    for (final AdministrationTimingDto.AdministrationTimestampsDto administrationTimestamps : administrationTiming.getTimestampsList())
    {
      if (frequency.equals(administrationTimestamps.getFrequency()))
      {
        return administrationTimestamps.getTimesList();
      }
    }
    return null;
  }

  @Override
  public MedicationDto getMedication(final Long medicationId, final DateTime when)
  {
    return medicationsDao.getMedicationById(medicationId, when);
  }

  @Override
  public DoseFormDto getDoseForm(final String code, final DateTime when)
  {
    return medicationsDao.getDoseFormByCode(code, when);
  }

  @Override
  public TherapyCardInfoDto getTherapyCardInfoData(
      final long patientId,
      final Double patientHeight,
      final String compositionId,
      final String ehrOrderName,
      final Locale locale,
      final Interval similarTherapiesInterval,
      final DateTime when)
  {
    final TherapyCardInfoDto cardInfoDto = new TherapyCardInfoDto();

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> currentInstructionPair =
        loadMedicationInstructionPair(patientId, compositionId, ehrOrderName);

    final Double referenceWeight = ehrMedicationsDao.getPatientLastReferenceWeight(patientId, Intervals.infiniteTo(when));

    final TherapyDto currentTherapy =
        convertInstructionToTherapyDto(
            currentInstructionPair.getFirst(),
            currentInstructionPair.getSecond(),
            referenceWeight,
            patientHeight,
            when,
            true,
            locale);

    cardInfoDto.setCurrentTherapy(currentTherapy);

    MedicationInstructionInstruction instruction = currentInstructionPair.getSecond();
    TherapyDto therapy = currentTherapy;

    List<Link> updateLinks = MedicationsEhrUtils.getLinksOfType(instruction, OpenEhrLinkType.UPDATE);
    while (!updateLinks.isEmpty())
    {
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> previousInstructionPair =
          getPreviousInstruction(patientId, instruction);

      final TherapyDto previousTherapy =
          convertInstructionToTherapyDto(
              previousInstructionPair.getFirst(),
              previousInstructionPair.getSecond(),
              referenceWeight,
              patientHeight,
              when,
              true,
              locale);

      final TherapyChangeHistoryDto changeHistory = calculateTherapyChange(previousTherapy, therapy);
      cardInfoDto.getChangeHistoryList().add(changeHistory);

      instruction = previousInstructionPair.getSecond();
      updateLinks = MedicationsEhrUtils.getLinksOfType(instruction, OpenEhrLinkType.UPDATE);
      therapy = previousTherapy;
    }
    cardInfoDto.setOriginalTherapy(therapy);

    if (similarTherapiesInterval != null)
    {
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> otherTherapies =
          ehrMedicationsDao.findMedicationInstructions(patientId, similarTherapiesInterval, null);

      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap =
          getMedicationDataForTherapies(otherTherapies, null, when);

      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> similarTherapies = new ArrayList<>();

      for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> otherTherapy : otherTherapies)
      {
        final boolean sameUid = EhrUtils.extractIdBeforeVersion(otherTherapy.getFirst())
            .equals(EhrUtils.extractIdBeforeVersion(currentInstructionPair.getFirst()));
        final boolean sameInstructionName =
            otherTherapy.getSecond().getName().getValue().equals(currentInstructionPair.getSecond().getName().getValue());
        final boolean sameTherapy = sameUid && sameInstructionName;
        final boolean linkedByUpdate = areInstructionsLinkedByUpdate(currentInstructionPair, otherTherapy);
        if (!sameTherapy && !linkedByUpdate)
        {
          final boolean therapyIsSimilar =
              areTherapiesSimilar(otherTherapy, currentInstructionPair, medicationsDataMap, false);
          if (therapyIsSimilar)
          {
            final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyLinedByUpdate =
                getTherapyLinedByUpdate(otherTherapy, similarTherapies);
            if (therapyLinedByUpdate != null)
            {
              similarTherapies.remove(therapyLinedByUpdate);
            }
            similarTherapies.add(otherTherapy);
          }
        }
      }
      for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> similarTherapy : similarTherapies)
      {
        final TherapyDto similarTherapyDto =
            convertInstructionToTherapyDto(
                similarTherapy.getFirst(),
                similarTherapy.getSecond(),
                referenceWeight,
                patientHeight,
                when,
                true,
                locale);

        cardInfoDto.getSimilarTherapies().add(similarTherapyDto.getFormattedTherapyDisplay());
      }
    }

    return cardInfoDto;
  }

  private Pair<MedicationOrderComposition, MedicationInstructionInstruction> getTherapyLinedByUpdate(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapy,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapiesToCompare)
  {
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareTherapy : therapiesToCompare)
    {
      final boolean linkedByUpdate = areInstructionsLinkedByUpdate(therapy, compareTherapy);
      if (linkedByUpdate)
      {
        return compareTherapy;
      }
    }
    return null;
  }

  @Override
  public Pair<MedicationOrderComposition, MedicationInstructionInstruction> getOriginalTherapy(
      final Long patientId, final String compositionUid, final String ehrOrderName)
  {
    Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair =
        ehrMedicationsDao.getTherapyInstructionPair(patientId, compositionUid, ehrOrderName);


    List<Link> updateLinks = MedicationsEhrUtils.getLinksOfType(instructionPair.getSecond(), OpenEhrLinkType.UPDATE);
    while (!updateLinks.isEmpty())
    //while (!instructionPair.getSecond().getLinks().isEmpty())
    {
      instructionPair = getPreviousInstruction(patientId, instructionPair.getSecond());
      updateLinks = MedicationsEhrUtils.getLinksOfType(instructionPair.getSecond(), OpenEhrLinkType.UPDATE);
    }
    return instructionPair;
  }

  TherapyChangeHistoryDto calculateTherapyChange(
      final TherapyDto therapy,
      final TherapyDto changedTherapy)
  {
    final TherapyChangeHistoryDto changeHistoryDto = new TherapyChangeHistoryDto();

    changeHistoryDto.setEditor(changedTherapy.getPrescriberName());
    changeHistoryDto.setChangeTime(changedTherapy.getStart());

    if (therapy instanceof SimpleTherapyDto)
    {
      if (changedTherapy instanceof SimpleTherapyDto)
      {
        final SimpleTherapyDto simpleTherapy = (SimpleTherapyDto)therapy;
        final SimpleTherapyDto changedSimpleTherapy = (SimpleTherapyDto)changedTherapy;
        if (!simpleTherapy.getMedication().getDisplayName().equals(changedSimpleTherapy.getMedication().getDisplayName()))
        {
          final TherapyChangeDto change = new TherapyChangeDto();
          change.setOldValue(simpleTherapy.getMedication().getDisplayName());
          change.setNewValue(changedSimpleTherapy.getMedication().getDisplayName());
          change.setType(TherapyChangeType.MEDICATION);
          changeHistoryDto.getChanges().add(change);
        }
        if (!simpleTherapy.getQuantityDisplay().equals(changedSimpleTherapy.getQuantityDisplay()))
        {
          final TherapyChangeDto change = new TherapyChangeDto();
          change.setOldValue(simpleTherapy.getQuantityDisplay());
          change.setNewValue(changedSimpleTherapy.getQuantityDisplay());
          change.setType(TherapyChangeType.DOSE);
          changeHistoryDto.getChanges().add(change);
        }
      }
      else
      {
        throw new IllegalArgumentException("Therapy not saved correctly");
      }
    }

    if (therapy instanceof ComplexTherapyDto)
    {
      if (changedTherapy instanceof ComplexTherapyDto)
      {
        final ComplexTherapyDto complexTherapy = (ComplexTherapyDto)therapy;
        final ComplexTherapyDto changedComplexTherapy = (ComplexTherapyDto)changedTherapy;
        if (complexTherapy.getSpeedDisplay() != null &&
            !complexTherapy.getSpeedDisplay().equals(changedComplexTherapy.getSpeedDisplay()))
        {
          final TherapyChangeDto change = new TherapyChangeDto();
          change.setOldValue(complexTherapy.getSpeedDisplay());
          change.setNewValue(changedComplexTherapy.getSpeedDisplay());
          change.setType(TherapyChangeType.SPEED);
          changeHistoryDto.getChanges().add(change);
        }
      }
      else
      {
        throw new IllegalArgumentException("Therapy not saved correctly");
      }
    }

    final boolean dosingFrequencyChange =
        therapy.getFrequencyDisplay() != null && changedTherapy.getFrequencyDisplay() != null &&
            !therapy.getFrequencyDisplay().equals(changedTherapy.getFrequencyDisplay());
    final boolean dosingDaysFrequencyChange =
        (therapy.getDaysFrequencyDisplay() != null && changedTherapy.getDaysFrequencyDisplay() == null) ||
            (therapy.getDaysFrequencyDisplay() == null && changedTherapy.getDaysFrequencyDisplay() != null) ||
            (therapy.getDaysFrequencyDisplay() != null && changedTherapy.getDaysFrequencyDisplay() != null &&
                !therapy.getDaysFrequencyDisplay().equals(changedTherapy.getFrequencyDisplay()));
    final boolean daysOfWeekFrequencyChange =
        (therapy.getDaysOfWeekDisplay() != null && changedTherapy.getDaysOfWeekDisplay() == null) ||
            (therapy.getDaysOfWeekDisplay() == null && changedTherapy.getDaysOfWeekDisplay() != null) ||
            (therapy.getDaysOfWeekDisplay() != null && changedTherapy.getDaysOfWeekDisplay() != null &&
                !therapy.getDaysOfWeekDisplay().equals(changedTherapy.getDaysOfWeekDisplay()));

    if (dosingFrequencyChange || dosingDaysFrequencyChange || daysOfWeekFrequencyChange)
    {
      final TherapyChangeDto change = new TherapyChangeDto();
      String oldValue = therapy.getFrequencyDisplay();
      if (therapy.getDaysFrequencyDisplay() != null)
      {
        oldValue += " - " + therapy.getDaysFrequencyDisplay();
      }
      if (therapy.getDaysOfWeekDisplay() != null)
      {
        oldValue += " - " + therapy.getDaysOfWeekDisplay();
      }
      change.setOldValue(oldValue);

      String newValue = changedTherapy.getFrequencyDisplay();
      if (changedTherapy.getDaysFrequencyDisplay() != null)
      {
        newValue += " - " + changedTherapy.getDaysFrequencyDisplay();
      }
      if (changedTherapy.getDaysOfWeekDisplay() != null)
      {
        newValue += " - " + changedTherapy.getDaysOfWeekDisplay();
      }
      change.setNewValue(newValue);
      change.setType(TherapyChangeType.DOSE_INTERVAL);
      changeHistoryDto.getChanges().add(change);
    }

    return changeHistoryDto;
  }

  @Override
  public MedicationReferenceWeightComposition buildReferenceWeightComposition(
      final double weight, final DateTime when, final long composerId)
  {
    final MedicationReferenceWeightComposition comp = new MedicationReferenceWeightComposition();
    final MedicationReferenceBodyWeightObservation medicationReferenceBodyWeight = new MedicationReferenceBodyWeightObservation();
    comp.setMedicationReferenceBodyWeight(medicationReferenceBodyWeight);
    final HistoryHistory history = new HistoryHistory();
    medicationReferenceBodyWeight.setHistoryHistory(history);
    final HistoryHistory.AnyEventEvent event = new HistoryHistory.AnyEventEvent();
    history.getAnyEvent().add(event);
    event.setTime(DataValueUtils.getDateTime(when));
    event.setWeight(DataValueUtils.getQuantity(weight, "kg"));

    final CompositionEventContext context = IspekTdoDataSupport.getEventContext(CompositionEventContext.class, when);
    comp.setCompositionEventContext(context);
    final NamedIdentity usersName = medicationsConnector.getUsersName(composerId, when);
    new TdoPopulatingVisitor().visitBean(
        comp,
        TdoPopulatingVisitor.getSloveneContext(when)
            .withCompositionDynamic(true)
            .withCompositionComposer(IspekTdoDataSupport.getPartyIdentified(usersName.name(), Long.toString(composerId)))
    );

    return comp;
  }

  @Override
  public List<TherapyTimelineRowDto> buildTherapyTimeline(
      final long patientId,
      final long centralCaseId,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      final Map<String, List<MedicationAdministrationComposition>> administrations,
      @Nullable final List<TherapyTaskDto> tasks,
      final Interval tasksInterval,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      final KnownClinic department,
      final DateTime when,
      final Locale locale)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> sortedInstructions = new ArrayList<>();
    sortedInstructions.addAll(medicationInstructions);
    sortTherapiesByMedicationTimingStart(sortedInstructions, true);

    final Map<String, TherapyTimelineRowDto> therapyTimelinesMap = new HashMap<>();
    final Map<String, String> similarTherapiesMap = new HashMap<>(); //therapy, latest similar therapy

    fillTherapyTimelinesMaps(
        patientId,
        sortedInstructions,
        therapyTimelinesMap,
        similarTherapiesMap,
        department,
        roundsInterval,
        when,
        locale);

    addAdministrationsToTimelines(
        administrations,
        therapyTimelinesMap,
        similarTherapiesMap,
        medicationInstructions,
        tasksInterval,
        when,
        locale);

    if (tasks != null)
    {
      addTasksToTimeline(tasks, therapyTimelinesMap, similarTherapiesMap, when);
    }
    final List<TherapyTimelineRowDto> therapyTimelines = new ArrayList<>(therapyTimelinesMap.values());
    sortTherapyTimelineRows(therapyTimelines, therapySortTypeEnum);

    //show ony therapies that are active today
    removeOldTherapiesFromTimeline(therapyTimelines, when.withTimeAtStartOfDay());

    if (centralCaseId > 0)
    {
      final TagFilteringDto filter = new TagFilteringDto();
      filter.setCompositionVersion(TagFilteringDto.CompositionVersion.LAST_VERSION_OF_ANY_TAGGED);

      final Set<TaggedObjectDto<Instruction>> taggedTherapies =
          ehrTaggingDao.findObjectCompositionPairs(
              filter,
              TherapyTaggingUtils.generateTag(TherapyTag.PRESCRIPTION, centralCaseId));

      fillPrescriptionTagsForTherapyTimeline(therapyTimelines, taggedTherapies);
    }

    fillTherapyTimelineLinksDisplay(therapyTimelines);
    return therapyTimelines;
  }

  //fills linkToTherapy for all linked therapies (when linksFromTherapy exists)
  private void fillTherapyTimelineLinksDisplay(final List<TherapyTimelineRowDto> therapyTimelines)
  {
    String link = "A";
    for (final TherapyTimelineRowDto therapyTimeline : therapyTimelines)
    {
      final TherapyDto therapy = therapyTimeline.getTherapy();
      if (therapy != null && therapy.getLinkFromTherapy() != null)
      {
        final boolean incrementLink = setTherapyTimelineLinkDisplay(
            therapy,
            therapyTimelines,
            link);
        if (incrementLink)
        {
          final int charValue = link.charAt(0);
          link = String.valueOf((char)(charValue + 1));
        }
      }
    }
  }

  private boolean setTherapyTimelineLinkDisplay(
      final TherapyDto therapyWithLink,
      final List<TherapyTimelineRowDto> therapyTimelines,
      final String link)
  {
    boolean linkUsed = false;

    for (TherapyTimelineRowDto therapyTimeline : therapyTimelines)
    {
      final TherapyDto therapyDto = therapyTimeline.getTherapy();
      final boolean therapyUsedNewLink = setTherapyLinkDisplayValue(therapyWithLink, therapyDto, link);
      linkUsed = therapyUsedNewLink ? true : linkUsed;
    }
    return linkUsed;
  }

  private void fillPrescriptionTagsForTherapyTimeline(
      final List<TherapyTimelineRowDto> therapyTimelines,
      final Set<TaggedObjectDto<Instruction>> taggedTherapies)
  {
    final List<TherapyDto> therapyDtos = new ArrayList<>();
    for (final TherapyTimelineRowDto timelineRowDto : therapyTimelines)
    {
      therapyDtos.add(timelineRowDto.getTherapy());
    }
    fillPrescriptionTagsForTherapies(therapyDtos, taggedTherapies);
  }

  void fillPrescriptionTagsForTherapies(
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
          therapy.addTag(TherapyTag.PRESCRIPTION);
        }
      }
    }
  }


  private void addTasksToTimeline(
      final List<TherapyTaskDto> tasks,
      final Map<String, TherapyTimelineRowDto> therapyTimelineRowsMap,
      final Map<String, String> similarTherapiesMap,
      final DateTime when)
  {
    for (final TherapyTaskDto task : tasks)
    {
      final String therapyId = task.getTherapyId();
      final String latestTherapyId =
          similarTherapiesMap.containsKey(therapyId) ? similarTherapiesMap.get(therapyId) : therapyId;
      final TherapyTimelineRowDto timelineRow = therapyTimelineRowsMap.get(latestTherapyId);
      if (timelineRow != null)
      {
        boolean isAdministrationFound = false;
        for (final AdministrationDto administration : timelineRow.getAdministrations())
        {
          final String administrationUidWithoutVersion =
              InstructionTranslator.getCompositionUidWithoutVersion(administration.getAdministrationId());
          final String taskAdministrationUidWithoutVersion =
              InstructionTranslator.getCompositionUidWithoutVersion(task.getAdministrationId());
          if (administrationUidWithoutVersion != null &&
              administrationUidWithoutVersion.equals(taskAdministrationUidWithoutVersion))
          {
            isAdministrationFound = true;
            administration.setTaskId(task.getTaskId());
            administration.setPlannedTime(task.getPlannedAdministrationTime());
            administration.setTriggersTherapyId(task.getTriggersTherapyId());

            if (administration.getAdministrationStatus() == AdministrationStatusEnum.COMPLETED
                || administration.getAdministrationStatus() == AdministrationStatusEnum.FAILED)
            {
              if (administration instanceof StartAdministrationDto)
              {
                final StartAdministrationDto startAdministration = (StartAdministrationDto)administration;
                final TherapyDoseDto administrationDose = startAdministration.getAdministeredDose();
                final TherapyDoseDto taskDose = task.getTherapyDoseDto();
                startAdministration.setPlannedDose(taskDose);
                if (administrationDose == null || taskDose == null)
                {
                  startAdministration.setDifferentFromOrder(false);
                }
                else
                {
                  startAdministration.setDifferentFromOrder(
                      //!administrationDose.equals(taskDose) || startAdministration.getSubstituteMedication() != null); //TODO ENGLISH
                      !administrationDose.equals(taskDose));
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

              if (administration.getAdministrationStatus() == AdministrationStatusEnum.COMPLETED)
              {
                if (isAdministrationLate(task.getPlannedAdministrationTime(), administration.getAdministrationTime()))
                {
                  administration.setAdministrationStatus(AdministrationStatusEnum.COMPLETED_LATE);
                }
                else if (isAdministrationEarly(task.getPlannedAdministrationTime(), administration.getAdministrationTime()))
                {
                  administration.setAdministrationStatus(AdministrationStatusEnum.COMPLETED_EARLY);
                }
              }
            }
            break;
          }
        }
        if (!isAdministrationFound)
        {
          final AdministrationDto administration = buildAdministrationFromTask(task, when);
          timelineRow.getAdministrations().add(administration);
        }
      }
    }
  }

  private void addAdministrationsToTimelines(
      final Map<String, List<MedicationAdministrationComposition>> administrations,
      final Map<String, TherapyTimelineRowDto> therapyTimelineRowsMap,
      final Map<String, String> similarTherapiesMap,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      final Interval tasksInterval,
      final DateTime when,
      final Locale locale)
  {
    for (final String therapyId : administrations.keySet())
    {
      final String latestTherapyId =
          similarTherapiesMap.containsKey(therapyId) ? similarTherapiesMap.get(therapyId) : therapyId;
      final TherapyTimelineRowDto timelineRow = therapyTimelineRowsMap.get(latestTherapyId);
      final List<MedicationAdministrationComposition> administrationsList = administrations.get(therapyId);
      for (final MedicationAdministrationComposition administrationComp : administrationsList)
      {
        final AdministrationDto administration;
        final Action action;
        if (!administrationComp.getMedicationDetail().getMedicationAction().isEmpty())
        {
          action = administrationComp.getMedicationDetail().getMedicationAction().get(0);
          final Pair<MedicationOrderComposition, MedicationInstructionInstruction> medicationInstructionPair =
              MedicationsEhrUtils.findMedicationInstructionPairByTherapyId(therapyId, medicationInstructions);
          administration =
              buildAdministrationFromMedicationAction((MedicationActionAction)action, medicationInstructionPair, when);
        }
        else if (!administrationComp.getMedicationDetail().getClinicalIntervention().isEmpty())
        {
          action = administrationComp.getMedicationDetail().getClinicalIntervention().get(0);
          administration =
              buildAdministrationFromClinicalIntervention((ClinicalInterventionAction)action);
        }
        else
        {
          throw new IllegalArgumentException(
              "MedicationAdministrationComposition must have MedicationActions or ClinicalInterventions");
        }

        administration.setAdministrationTime(DataValueUtils.getDateTime(action.getTime()));
        final String composerName =
            administrationComp.getComposer() instanceof PartyIdentified ?
            ((PartyIdentified)administrationComp.getComposer()).getName() : "";
        administration.setComposerName(composerName);
        administration.setAdministrationId(administrationComp.getUid().getValue());
        administration.setTherapyId(therapyId);

        timelineRow.getAdministrations().add(administration);
      }
    }

    fillContinuousInfusionCurrentRate(therapyTimelineRowsMap, locale);
    filterAdministrationsByTime(therapyTimelineRowsMap, tasksInterval);
  }

  private AdministrationDto buildAdministrationFromMedicationAction(
      final MedicationActionAction action,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      final DateTime when)
  {
    final MedicationActionEnum actionEnum =
        MedicationActionEnum.getAction(
            action.getIsmTransition().getCareflowStep(),
            action.getIsmTransition().getCurrentState());
    Preconditions.checkArgument(
        actionEnum == MedicationActionEnum.ADMINISTER || actionEnum == MedicationActionEnum.WITHHOLD);
    final AdministrationDto administration;
    final AdministrationTypeEnum reasonAdministrationTypeEnum =
        action.getReason().isEmpty() ? null :
        AdministrationTypeEnum.getByFullString(action.getReason().get(0).getValue());
    final AdministrationDetailsCluster administrationDetails = action.getAdministrationDetails();
    if (reasonAdministrationTypeEnum != null && reasonAdministrationTypeEnum == AdministrationTypeEnum.START)
    {
      administration = new StartAdministrationDto();
      if (administrationDetails != null && !administrationDetails.getInfusionAdministrationDetails().isEmpty() &&
          administrationDetails.getInfusionAdministrationDetails()
              .get(0).getDoseAdministrationRate() instanceof DvQuantity) //TherapyDoseTypeEnum RATE
      {
        final InfusionAdministrationDetailsCluster infusionAdministrationDetails =
            administrationDetails.getInfusionAdministrationDetails().get(0);
        final TherapyDoseDto administeredDose =
            MedicationsEhrUtils.buildTherapyDoseDtoForRate(infusionAdministrationDetails);
        ((StartAdministrationDto)administration).setAdministeredDose(administeredDose);
      }
      else if (action.getStructuredDose() != null) //TherapyDoseTypeEnum QUANTITY, VOLUME_SUM
      {
        ((StartAdministrationDto)administration).setAdministeredDose(
            MedicationsEhrUtils.buildTherapyDoseDto(action.getStructuredDose()));
        final MedicationInstructionInstruction instruction = instructionPair.getSecond();
        if (MedicationsEhrUtils.isSimpleInstruction(instruction))
        {
          final DvText therapyMedicine = instruction.getOrder().get(0).getMedicine();
          if (therapyMedicine != null && therapyMedicine instanceof DvCodedText)
          {
            if (action.getMedicine() != null && action.getMedicine() instanceof DvCodedText)
            {
              final Long therapyMedicationId =
                  Long.parseLong(((DvCodedText)therapyMedicine).getDefiningCode().getCodeString());
              final Long administrationMedicationId =
                  Long.parseLong(((DvCodedText)action.getMedicine()).getDefiningCode().getCodeString());
              if (!therapyMedicationId.equals(administrationMedicationId))
              {
                final MedicationDto substituteMedication =
                    medicationsDao.getMedicationById(administrationMedicationId, when);
                ((StartAdministrationDto)administration).setSubstituteMedication(substituteMedication);
              }
            }
          }
        }
      }
    }
    else if (reasonAdministrationTypeEnum != null && reasonAdministrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION)   //TherapyDoseTypeEnum RATE
    {
      administration = new AdjustInfusionAdministrationDto();
      final InfusionAdministrationDetailsCluster infusionAdministrationDetails =
          administrationDetails.getInfusionAdministrationDetails().get(0);
      ((AdjustInfusionAdministrationDto)administration).setAdministeredDose(
          MedicationsEhrUtils.buildTherapyDoseDtoForRate(infusionAdministrationDetails));
    }
    else if (reasonAdministrationTypeEnum != null && reasonAdministrationTypeEnum == AdministrationTypeEnum.STOP)
    {
      administration = new StopAdministrationDto();
    }
    else
    {
      throw new IllegalArgumentException("Administration reason not supported");
    }
    final AdministrationStatusEnum administrationStatus =
        actionEnum == MedicationActionEnum.ADMINISTER
        ? AdministrationStatusEnum.COMPLETED
        : AdministrationStatusEnum.FAILED;
    administration.setAdministrationStatus(administrationStatus);
    if (action.getComment() != null)
    {
      administration.setComment(action.getComment().getValue());
    }
    return administration;
  }

  private InfusionSetChangeDto buildAdministrationFromClinicalIntervention(final ClinicalInterventionAction action)
  {
    final InfusionSetChangeDto administration = new InfusionSetChangeDto();
    administration.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    administration.setInfusionSetChangeEnum(
        InfusionSetChangeEnum.getEnumByCode(action.getIntervention().getDefiningCode().getCodeString()));
    if (action.getComments() != null)
    {
      administration.setComment(action.getComments().getValue());
    }
    return administration;
  }

  private void filterAdministrationsByTime(
      final Map<String, TherapyTimelineRowDto> therapyTimelineRowsMap, final Interval tasksInterval)
  {
    for (final String timelineKey : therapyTimelineRowsMap.keySet())
    {
      final TherapyTimelineRowDto timelineRow = therapyTimelineRowsMap.get(timelineKey);
      final List<AdministrationDto> filteredAdministrations = new ArrayList<>();
      for (final AdministrationDto administration : timelineRow.getAdministrations())
      {
        if (administration.getAdministrationTime() != null &&
            tasksInterval.contains(administration.getAdministrationTime()))
        {
          filteredAdministrations.add(administration);
        }
      }
      timelineRow.setAdministrations(filteredAdministrations);
    }
  }

  private void fillContinuousInfusionCurrentRate(
      final Map<String, TherapyTimelineRowDto> therapyTimelineRowsMap, final Locale locale)
  {
    for (final String timelineKey : therapyTimelineRowsMap.keySet())
    {
      final TherapyTimelineRowDto timelineRow = therapyTimelineRowsMap.get(timelineKey);
      if (timelineRow instanceof TherapyTimelineRowForContInfusionDto)
      {
        final List<AdministrationDto> administrations = timelineRow.getAdministrations();

        AdministrationDto lastAdministration = null;

        for (final AdministrationDto administration : administrations)
        {
          if (AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administration.getAdministrationType()))
          {
            if (lastAdministration == null ||
                administration.getAdministrationTime().isAfter(lastAdministration.getAdministrationTime()))
            {
              lastAdministration = administration;
            }
          }
        }

        if (lastAdministration != null && lastAdministration.getAdministrationType() != AdministrationTypeEnum.STOP)
        {
          TherapyDoseDto dose = null;
          if (lastAdministration instanceof StartAdministrationDto)
          {
            dose = ((StartAdministrationDto)lastAdministration).getAdministeredDose();
          }
          else if (lastAdministration instanceof AdjustInfusionAdministrationDto)
          {
            dose = ((AdjustInfusionAdministrationDto)lastAdministration).getAdministeredDose();
          }
          if (dose != null && dose.getNumerator() != null)
          {
            try
            {
              final String rateDisplayString =
                  therapyDisplayProvider.getRateDisplayString(dose.getNumerator(), dose.getNumeratorUnit(), locale);
              ((TherapyTimelineRowForContInfusionDto)timelineRow).setInfusionRateDisplay(rateDisplayString);
            }
            catch (final ParseException e)
            {
              throw new IllegalArgumentException("Infusion rate invalid format" + e);
            }
          }
        }
      }
    }
  }

  private void fillTherapyTimelinesMaps(
      final long patientId,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies,
      final Map<String, TherapyTimelineRowDto> therapyTimelineRowsMap,
      final Map<String, String> similarTherapiesMap,
      final KnownClinic department,
      final RoundsIntervalDto roundsInterval,
      final DateTime when,
      final Locale locale)
  {
    final Map<String, Pair<MedicationOrderComposition, MedicationInstructionInstruction>> processedTherapiesMap = new HashMap<>();
    final Map<Long, MedicationDataForTherapyDto> medicationsDataMap =
        getMedicationDataForTherapies(therapies, department, when);
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : therapies)
    {
      final MedicationInstructionInstruction instruction = instructionPair.getSecond();
      final MedicationOrderComposition composition = instructionPair.getFirst();
      final String therapyId = InstructionTranslator.translate(instruction, composition);

      final String latestSimilarTherapyId =
          findSimilarTherapyForTimeline(processedTherapiesMap, medicationsDataMap, instructionPair);
      if (latestSimilarTherapyId != null)
      {
        similarTherapiesMap.put(therapyId, latestSimilarTherapyId);
        processedTherapiesMap.put(latestSimilarTherapyId, instructionPair);
      }
      else
      {
        final TherapyTimelineRowDto timelineRow =
            buildTherapyTimelineDto(
                roundsInterval,
                when,
                locale,
                medicationsDataMap,
                instructionPair,
                instruction,
                composition,
                therapyId,
                patientId);
        therapyTimelineRowsMap.put(therapyId, timelineRow);

        processedTherapiesMap.put(therapyId, instructionPair);
      }
    }
  }

  private TherapyTimelineRowDto buildTherapyTimelineDto(
      final RoundsIntervalDto roundsInterval,
      final DateTime when,
      final Locale locale,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      final MedicationInstructionInstruction instruction,
      final MedicationOrderComposition composition,
      final String therapyId,
      final long patientId)
  {
    final boolean continuousInfusion = MedicationsEhrUtils.isContinuousInfusion(instructionPair.getSecond());
    final TherapyDto therapy = convertInstructionToTherapyDto(
        instructionPair.getFirst(),
        instructionPair.getSecond(),
        null,
        null,
        when,
        true,
        locale);
    final TherapyTimelineRowDto timelineRow =
        continuousInfusion ? new TherapyTimelineRowForContInfusionDto() : new TherapyTimelineRowDto();
    final Long mainMedicationId = getMainMedicationId(instructionPair.getSecond().getOrder().get(0));
    final MedicationDataForTherapyDto mainMedicationData = medicationsDataMap.get(mainMedicationId);
    if (mainMedicationData != null)
    {
      timelineRow.setCustomGroup(mainMedicationData.getCustomGroupName());
      timelineRow.setCustomGroupSortOrder(mainMedicationData.getCustomGroupSortOrder());
      timelineRow.setAtcGroupCode(mainMedicationData.getAtcCode());
      timelineRow.setAtcGroupName(mainMedicationData.getAtcName());
    }
    timelineRow.setTherapy(therapy);
    timelineRow.setTherapyId(therapyId);
    fillTherapyDayState(
        timelineRow,
        patientId,
        therapy,
        instruction,
        composition,
        roundsInterval,
        medicationsDataMap,
        Intervals.wholeDay(when),
        when);
    return timelineRow;
  }

  private String findSimilarTherapyForTimeline(
      final Map<String, Pair<MedicationOrderComposition, MedicationInstructionInstruction>> processedTherapiesMap,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair)
  {
    String similarTherapyId = null;
    for (final String therapyId : processedTherapiesMap.keySet())
    {
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair =
          processedTherapiesMap.get(therapyId);

      final boolean laterLinkedTherapyFound = areInstructionsLinkedByUpdate(instructionPair, compareInstructionPair);
      if (laterLinkedTherapyFound)
      {
        similarTherapyId = therapyId;
        break;
      }
    }
    if (similarTherapyId == null)
    {
      for (final String therapyId : processedTherapiesMap.keySet())
      {
        final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair =
            processedTherapiesMap.get(therapyId);

        final boolean similarTherapyFound =
            areTherapiesSimilar(instructionPair, compareInstructionPair, medicationsDataMap, false);
        if (similarTherapyFound)
        {
          similarTherapyId = therapyId;
        }
      }
    }
    return similarTherapyId;
  }

  private void fillTherapyDayState(
      final TherapyDayDto therapyDayDto,
      final long patientId,
      final TherapyDto therapy,
      final MedicationInstructionInstruction instruction,
      final MedicationOrderComposition composition,
      final RoundsIntervalDto roundsInterval,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final Interval therapyDay,
      final DateTime currentTime)
  {
    final List<MedicationActionAction> actions = getInstructionActions(composition, instruction);
    final OrderActivity orderActivity = instruction.getOrder().get(0);
    final DateTime therapyStart = getTherapyStart(patientId, instruction);
    DateTime therapyEnd = DataValueUtils.getDateTime(orderActivity.getMedicationTiming().getStopDate());
    therapyEnd = therapyEnd != null ? therapyEnd : Intervals.INFINITE.getEnd();

    final TherapyStatusEnum therapyStatus =
        getTherapyStatusFromMedicationAction(actions, therapyStart, roundsInterval, therapyDay, currentTime);
    final boolean modifiedFromLastReview = wasTherapyModifiedFromLastReview(
        instruction,
        actions,
        DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()));

    final List<Long> medicationIds = getMedicationIds(orderActivity);
    final boolean containsAntibiotic = containsAntibiotic(medicationIds, medicationsDataMap);
    final Integer pastDaysOfTherapy =
        orderActivity.getPastDaysOfTherapy() != null ? (int)orderActivity.getPastDaysOfTherapy().getMagnitude() : null;
    final int consecutiveDay =
        getTherapyConsecutiveDay(therapyStart, therapyDay.getStart(), currentTime, pastDaysOfTherapy);
    final boolean therapyActionsAllowed = areTherapyActionsAllowed(therapyStatus, roundsInterval, currentTime);
    final boolean therapyEndsBeforeNextRounds = doesTherapyEndBeforeNextRounds(therapyEnd, roundsInterval, currentTime);
    final boolean therapyIsActive =
        isTherapyActive(
            therapy.getDaysOfWeek(),
            therapy.getDosingDaysFrequency(),
            new Interval(therapyStart, therapyEnd),
            therapyDay.getStart());

    therapyDayDto.setActive(therapyIsActive);
    therapyDayDto.setModified(!instruction.getLinks().isEmpty());
    therapyDayDto.setTherapyEndsBeforeNextRounds(therapyEndsBeforeNextRounds);
    therapyDayDto.setTherapyStatus(therapyStatus);
    therapyDayDto.setShowConsecutiveDay(containsAntibiotic && therapyDay.getStart().isBefore(currentTime));
    therapyDayDto.setModifiedFromLastReview(modifiedFromLastReview);
    therapyDayDto.setConsecutiveDay(consecutiveDay);
    therapyDayDto.setTherapyActionsAllowed(therapyActionsAllowed);
  }

  private void sortTherapiesByMedicationTimingStart(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies, final boolean descending)
  {
    Collections.sort(
        therapies, new Comparator<Pair<MedicationOrderComposition, MedicationInstructionInstruction>>()
        {
          @Override
          public int compare(
              final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapy1,
              final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapy2)
          {
            final DateTime firstCompositionStart =
                DataValueUtils.getDateTime(therapy1.getSecond().getOrder().get(0).getMedicationTiming().getStartDate());
            final DateTime secondCompositionStart =
                DataValueUtils.getDateTime(therapy2.getSecond().getOrder().get(0).getMedicationTiming().getStartDate());
            if (descending)
            {
              return secondCompositionStart.compareTo(firstCompositionStart);
            }
            return firstCompositionStart.compareTo(secondCompositionStart);
          }
        }
    );
  }

  AdministrationDto buildAdministrationFromTask(final TherapyTaskDto task, final DateTime when)
  {
    final AdministrationDto administration;
    if (task.getAdministrationTypeEnum() == AdministrationTypeEnum.START)
    {
      administration = new StartAdministrationDto();
      ((StartAdministrationDto)administration).setPlannedDose(task.getTherapyDoseDto());
    }
    else if (task.getAdministrationTypeEnum() == AdministrationTypeEnum.STOP)
    {
      administration = new StopAdministrationDto();
    }
    else if (task.getAdministrationTypeEnum() == AdministrationTypeEnum.ADJUST_INFUSION)
    {
      administration = new AdjustInfusionAdministrationDto();
      ((AdjustInfusionAdministrationDto)administration).setPlannedDose(task.getTherapyDoseDto());
    }
    else
    {
      throw new IllegalArgumentException("Administration type not supported!");
    }

    administration.setTaskId(task.getTaskId());
    administration.setAdministrationId(task.getAdministrationId());
    administration.setPlannedTime(task.getPlannedAdministrationTime());
    administration.setTriggersTherapyId(task.getTriggersTherapyId());

    if (isAdministrationDue(task.getPlannedAdministrationTime(), when))
    {
      administration.setAdministrationStatus(AdministrationStatusEnum.DUE);
    }
    else if (isAdministrationLate(task.getPlannedAdministrationTime(), when))
    {
      administration.setAdministrationStatus(AdministrationStatusEnum.LATE);
    }
    else
    {
      administration.setAdministrationStatus(AdministrationStatusEnum.PLANNED);
    }
    return administration;
  }

  private boolean isAdministrationLate(final DateTime planned, final DateTime compare)
  {
    return getOffsetInMinutes(planned, compare) > 30;
  }

  private boolean isAdministrationEarly(final DateTime planned, final DateTime compare)
  {
    return getOffsetInMinutes(planned, compare) < -30;
  }

  private boolean isAdministrationDue(final DateTime planned, final DateTime compare)
  {
    final long offset = getOffsetInMinutes(planned, compare);
    return offset < 30 && offset > -30;
  }

  private long getOffsetInMinutes(final DateTime planned, final DateTime compare)
  {
    return new Duration(planned, compare).getStandardMinutes();
  }

  @Override
  public String confirmTherapyAdministration(
      final String therapyCompositionUid,
      final String ehrOrderName,
      final Long patientId,
      final Long userId,
      final Long centralCaseId,
      final Long careProviderId,
      final AdministrationDto administrationDto,
      final boolean administrationSuccessful,
      final DateTime when,
      final Locale locale)
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
        ehrMedicationsDao.getTherapyInstructionPair(patientId, therapyCompositionUid, ehrOrderName);

    if (AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administrationDto.getAdministrationType()))
    {
      final MedicationActionEnum medicationActionEnum =
          administrationSuccessful ? MedicationActionEnum.ADMINISTER : MedicationActionEnum.WITHHOLD;
      return confirmMedicationAdministration(
          therapyInstructionPair,
          patientId,
          userId,
          centralCaseId,
          careProviderId,
          administrationDto,
          medicationActionEnum,
          when);
    }
    else if (administrationDto.getAdministrationType() == AdministrationTypeEnum.INFUSION_SET_CHANGE)
    {
      return confirmInfusionSetChange(
          therapyInstructionPair, patientId, userId, (InfusionSetChangeDto)administrationDto, when, locale);
    }
    else
    {
      throw new IllegalArgumentException(
          "Administration type: " + administrationDto.getAdministrationType().name() + " not supported;");
    }
  }

  @Override
  public String confirmMedicationAdministration(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair,
      final Long patientId,
      final Long userId,
      final Long centralCaseId,
      final Long careProviderId,
      final AdministrationDto administrationDto,
      final MedicationActionEnum medicationActionEnum,
      final DateTime when)
  {
    Preconditions.checkArgument(
        AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administrationDto.getAdministrationType()));
    final MedicationInstructionInstruction instruction = therapyInstructionPair.getSecond();
    final MedicationAdministrationComposition administrationComposition = new MedicationAdministrationComposition();
    InstructionToAdministrationMapper.map(instruction, administrationComposition);
    updateAdministrationComposition(administrationComposition, administrationDto, medicationActionEnum);
    linkActionsToInstructions(therapyInstructionPair, administrationComposition.getMedicationDetail().getMedicationAction());

    final NamedIdentity usersName = medicationsConnector.getUsersName(userId, when);
    addContext(administrationComposition, usersName, null, null, null, when);
    return ehrMedicationsDao.saveMedicationAdministrationComposition(
        patientId, administrationComposition, administrationDto.getAdministrationId());
  }

  private String confirmInfusionSetChange(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair,
      final Long patientId,
      final Long userId,
      final InfusionSetChangeDto administrationDto,
      final DateTime when,
      final Locale locale)
  {
    final MedicationAdministrationComposition administrationComposition = new MedicationAdministrationComposition();
    administrationComposition.setMedicationDetail(new MedicationDetailSection());
    final ClinicalInterventionAction clinicalInterventionAction = new ClinicalInterventionAction();
    administrationComposition.getMedicationDetail().getClinicalIntervention().add(clinicalInterventionAction);

    final String translatedIntervention =
        medicationsConnector.getEntry(EnumUtils.getIdentifier(administrationDto.getInfusionSetChangeEnum()), null, locale);

    clinicalInterventionAction.setIntervention(
        DataValueUtils.getCodedText(
            EhrTerminologyEnum.PK_NANDA.getEhrName(),
            administrationDto.getInfusionSetChangeEnum().getCode(),
            translatedIntervention)
    );
    clinicalInterventionAction.setInterventionUnsuccessful(DataValueUtils.getBoolean(false));
    clinicalInterventionAction.setTime(DataValueUtils.getDateTime(administrationDto.getAdministrationTime()));
    clinicalInterventionAction.setComments(DataValueUtils.getText(administrationDto.getComment()));

    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(ClinicalInterventionEnum.COMPLETED.getCareflowStep());
    ismTransition.setCurrentState(ClinicalInterventionEnum.COMPLETED.getCurrentState());
    clinicalInterventionAction.setIsmTransition(ismTransition);

    linkActionsToInstructions(
        therapyInstructionPair, administrationComposition.getMedicationDetail().getClinicalIntervention());

    final NamedIdentity usersName = medicationsConnector.getUsersName(userId, when);
    addContext(administrationComposition, usersName, null, null, null, when);
    return ehrMedicationsDao.saveMedicationAdministrationComposition(
        patientId, administrationComposition, administrationDto.getAdministrationId());
  }

  private void updateAdministrationComposition(
      final MedicationAdministrationComposition administrationComposition,
      final AdministrationDto administrationDto,
      final MedicationActionEnum medicationActionEnum)
  {
    final MedicationActionAction medicationAction =
        administrationComposition.getMedicationDetail().getMedicationAction().get(0);
    medicationAction.setTime(DataValueUtils.getDateTime(administrationDto.getAdministrationTime()));
    medicationAction.getReason().add(
        DataValueUtils.getText(AdministrationTypeEnum.getFullString(administrationDto.getAdministrationType())));

    // for simple therapies user can select substitute medication
    if (administrationDto instanceof StartAdministrationDto)
    {
      final MedicationDto substituteMedication = ((StartAdministrationDto)administrationDto).getSubstituteMedication();
      if (substituteMedication != null)
      {
        if (medicationAction.getIngredientsAndForm() != null &&
            !medicationAction.getIngredientsAndForm().getIngredient().isEmpty())
        {
          throw new IllegalArgumentException("Substitute medication can only be set for simple therapies");
        }
        medicationAction.setMedicine(
            DataValueUtils.getLocalCodedText(String.valueOf(substituteMedication.getId()), substituteMedication.getName()));
        medicationAction.setBrandSubstituted(DataValueUtils.getBoolean(true));
      }
    }

    //is start or adjust set therapyDose data
    if (administrationDto instanceof StartAdministrationDto || administrationDto instanceof AdjustInfusionAdministrationDto)
    {
      final TherapyDoseDto therapyDose = administrationDto instanceof StartAdministrationDto
                                         ? ((StartAdministrationDto)administrationDto).getAdministeredDose()
                                         : ((AdjustInfusionAdministrationDto)administrationDto).getAdministeredDose();
      if (therapyDose != null && therapyDose.getNumerator() != null)
      {
        if (therapyDose.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.QUANTITY)
        {
          medicationAction.setStructuredDose(
              MedicationsEhrUtils.buildStructuredDose(
                  therapyDose.getNumerator(),
                  therapyDose.getNumeratorUnit(),
                  therapyDose.getDenominator(),
                  therapyDose.getDenominatorUnit(),
                  null)
          );
        }
        else if (therapyDose.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.RATE)
        {
          final InfusionAdministrationDetailsCluster infusionDetails = MedicationsEhrUtils.getInfusionDetails(
              false,
              null,
              therapyDose.getNumerator(),
              therapyDose.getNumeratorUnit(),
              therapyDose.getDenominator(),
              therapyDose.getDenominatorUnit());
          if (!medicationAction.getAdministrationDetails().getInfusionAdministrationDetails().isEmpty())
          {
            infusionDetails.setPurposeEnum(
                medicationAction.getAdministrationDetails().getInfusionAdministrationDetails().get(0).getPurposeEnum());
          }
          medicationAction.getAdministrationDetails().getInfusionAdministrationDetails().clear();
          medicationAction.getAdministrationDetails().getInfusionAdministrationDetails().add(infusionDetails);
        }
        else if (therapyDose.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.VOLUME_SUM)
        {
          medicationAction.setStructuredDose(
              MedicationsEhrUtils.buildStructuredDose(
                  therapyDose.getNumerator(),
                  therapyDose.getNumeratorUnit(),
                  null, null, null)
          );
        }
      }
    }
    medicationAction.setComment(DataValueUtils.getText(administrationDto.getComment()));

    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(medicationActionEnum.getCareflowStep());
    ismTransition.setCurrentState(medicationActionEnum.getCurrentState());
    medicationAction.setIsmTransition(ismTransition);
  }

  private void linkActionsToInstructions(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair,
      final List<? extends Action> actions)
  {
    for (final Action action : actions)
    {
      final MedicationInstructionInstruction therapyInstruction = therapyInstructionPair.getSecond();
      final MedicationOrderComposition therapyComposition = therapyInstructionPair.getFirst();
      action.setInstructionDetails(new InstructionDetails());
      action.getInstructionDetails().setActivityId("activities[at0001] ");

      final LocatableRef actionInstructionId =
          MedicationsEhrUtils.createInstructionLocatableRef(therapyComposition, therapyInstruction);
      final RmPath rmPath = TdoPathable.pathOfItem(therapyComposition, therapyInstruction);
      actionInstructionId.setPath(rmPath.getCanonicalString());
      final ObjectVersionId objectVersionId = new ObjectVersionId();
      final String compositionUid =
          InstructionTranslator.getCompositionUidWithoutVersion(therapyComposition.getUid().getValue());
      objectVersionId.setValue(compositionUid);
      actionInstructionId.setId(objectVersionId);
      action.getInstructionDetails().setInstructionId(actionInstructionId);
    }
  }

  @Override
  public void deleteAdministration(final long patientId, final String compositionId, final String comment)
  {
    ehrMedicationsDao.deleteTherapyAdministration(patientId, compositionId, comment);
  }

  @Override
  public DocumentationTherapiesDto findTherapyGroupsForDocumentation(
      final Long patientId,
      final Long centralCaseId,
      final Interval centralCaseEffective,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs,
      final KnownClinic department,
      final boolean isOutpatient,
      final DateTime when,
      final Locale locale)
  {
    sortTherapiesByMedicationTimingStart(instructionPairs, false);

    final Map<Long, MedicationDataForTherapyDto> medicationsDataMap =
        getMedicationDataForTherapies(instructionPairs, department, when);

    return getTherapiesForDocumentation(
        patientId,
        centralCaseId,
        centralCaseEffective,
        instructionPairs,
        medicationsDataMap,
        isOutpatient,
        when,
        locale
    );
  }

  DocumentationTherapiesDto getTherapiesForDocumentation(
      final Long patientId,
      final Long centralCaseId,
      final Interval centralCaseEffective,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final boolean isOutpatient,
      final DateTime when,
      final Locale locale)
  {
    final List<TherapyDocumentationData> therapies = new ArrayList<>();
    final List<TherapyDocumentationData> dischargeTherapies = new ArrayList<>();
    final List<TherapyDocumentationData> admissionTherapies = new ArrayList<>();
    final List<TherapyDocumentationData> taggedTherapiesForPrescription = new ArrayList<>();

    if (centralCaseId != null && centralCaseId > 0)
    {
      final TagFilteringDto filter = new TagFilteringDto();
      filter.setCompositionVersion(TagFilteringDto.CompositionVersion.LAST_VERSION_OF_ANY_TAGGED);

      final Set<TaggedObjectDto<Instruction>> compositionPairs =
          ehrTaggingDao.findObjectCompositionPairs(
              filter,
              TherapyTaggingUtils.generateTag(TherapyTag.PRESCRIPTION, centralCaseId));

      for (final TaggedObjectDto<Instruction> taggedObjectDto : compositionPairs)
      {
        final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair =
            ehrMedicationsDao.getTherapyInstructionPair(
                patientId,
                taggedObjectDto.getComposition().getUid().getValue(),
                taggedObjectDto.getObject().getName().getValue());

        final TherapyDto convertedTherapy = convertInstructionToTherapyDto(
            instructionPair.getFirst(),
            instructionPair.getSecond(),
            null,
            null,
            when,
            true,
            locale);

        taggedTherapiesForPrescription.add(createTherapyData(instructionPair, convertedTherapy, null));
      }
    }

    final Set<TherapyDocumentationData> alreadyHandled = new HashSet<>();

    final boolean isOutpatientOrLastsOneDay = isOutpatient || Intervals.durationInDays(centralCaseEffective) <= 1;

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionPairs)
    {
      final DateTime therapyStart = DataValueUtils.getDateTime(instructionPair.getSecond().getOrder().get(0).getMedicationTiming().getStartDate());
      final DateTime therapyEnd = DataValueUtils.getDateTime(instructionPair.getSecond().getOrder().get(0).getMedicationTiming().getStopDate());

      final TherapyDto convertedTherapy = convertInstructionToTherapyDto(
          instructionPair.getFirst(),
          instructionPair.getSecond(),
          null,
          null,
          when,
          true,
          locale);

      if (!areAllIngredientsSolutions(convertedTherapy))
      {
        final Interval therapyInterval = new Interval(therapyStart, therapyEnd != null ? therapyEnd : Intervals.INFINITE.getEnd());

        final boolean handled =
            handleSimilarAndLinkedTherapies(
                admissionTherapies,
                dischargeTherapies,
                therapies,
                alreadyHandled,
                instructionPair,
                convertedTherapy,
                medicationsDataMap,
                therapyInterval,
                centralCaseEffective.getEnd(),
                when);

        final TherapyDocumentationData therapy = createTherapyData(instructionPair, convertedTherapy, therapyInterval);
        alreadyHandled.add(therapy);

        if (!handled)
        {
          if (isOutpatientOrLastsOneDay)
          {
            if (therapyInterval.overlaps(Intervals.wholeDay(centralCaseEffective.getStart())))
            {
              therapies.add(therapy);
            }
          }
          else
          {
            boolean isAdmission = false;
            if (therapyInterval.overlaps(Intervals.wholeDay(centralCaseEffective.getStart())))
            {
              admissionTherapies.add(therapy);
              isAdmission = true;
            }

            boolean isDischarge = false;
            if (isDischargeTherapy(therapyInterval, centralCaseEffective.getEnd(), when))
            {
              dischargeTherapies.add(therapy);
              isDischarge = true;
            }

            if (!isAdmission && !isDischarge)
            {
              if (therapyInterval.overlaps(centralCaseEffective))
              {
                therapies.add(therapy);
              }
            }
          }
        }
      }
    }

    return new DocumentationTherapiesDto(
        getTherapyDisplayValuesForDocumentation(therapies, locale),
        getTherapyDisplayValuesForDocumentation(dischargeTherapies, locale),
        getTherapyDisplayValuesForDocumentation(admissionTherapies, locale),
        getTherapyDisplayValuesForDocumentation(taggedTherapiesForPrescription, locale));
  }

  private TherapyDocumentationData createTherapyData(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      final TherapyDto therapy,
      @Nullable final Interval therapyInterval)
  {
    final TherapyDocumentationData therapyData = new TherapyDocumentationData();
    therapyData.setInstructionPair(instructionPair);
    therapyData.setTherapy(therapy);

    if (therapyInterval != null)
    {
      therapyData.addInterval(InstructionTranslator.createId(therapy.getEhrOrderName(), therapy.getCompositionUid()), therapyInterval);
    }
    return therapyData;
  }

  private boolean isDischargeTherapy(final Interval therapyInterval, final DateTime centralCaseEnd, final DateTime when)
  {
    return Intervals.isEndInfinity(therapyInterval.getEnd())
        && therapyInterval.overlaps(Intervals.wholeDay(when))
        || !Intervals.isEndInfinity(therapyInterval.getEnd())
        && therapyInterval.overlaps(Intervals.wholeDay(centralCaseEnd));
  }

  private boolean handleSimilarAndLinkedTherapies(
      final List<TherapyDocumentationData> admissionTherapies,
      final List<TherapyDocumentationData> dischargeTherapies,
      final List<TherapyDocumentationData> therapies,
      final Set<TherapyDocumentationData> alreadyHandled,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyToCompare,
      final TherapyDto convertedTherapy,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final Interval therapyInterval,
      final DateTime centralCaseEnd,
      final DateTime when)
  {
    final Pair<TherapyLinkType, TherapyDocumentationData> pair = getLinkedToTherapyPair(
        admissionTherapies,
        therapies,
        therapyToCompare);

    if (pair.getFirst() == TherapyLinkType.REGULAR_LINK)
    {
      final TherapyDocumentationData linkedToTherapy = pair.getSecond();
      final Interval interval = linkedToTherapy.findIntervalForId(InstructionTranslator.createId(
                                                                      linkedToTherapy.getTherapy()
                                                                          .getEhrOrderName(),
                                                                      linkedToTherapy.getTherapy()
                                                                          .getCompositionUid()));

      if (isDischargeTherapy(therapyInterval, centralCaseEnd, when))
      {
        if (therapies.contains(linkedToTherapy))
        {
          therapies.remove(linkedToTherapy);
        }
        if (admissionTherapies.contains(linkedToTherapy))
        {
          admissionTherapies.remove(linkedToTherapy);
        }
        final TherapyDocumentationData dischargeTherapy = createTherapyData(therapyToCompare, convertedTherapy, new Interval(interval.getStart(), therapyInterval.getEnd()));
        dischargeTherapies.add(dischargeTherapy);
      }
      else
      {
        final Interval newInterval = new Interval(interval.getStart(), therapyInterval.getEnd());
        linkedToTherapy.addInterval(InstructionTranslator.createId(
                                        convertedTherapy.getEhrOrderName(),
                                        convertedTherapy.getCompositionUid()), newInterval);
        linkedToTherapy.removeInterval(InstructionTranslator.createId(
                                           linkedToTherapy.getTherapy().getEhrOrderName(),
                                           linkedToTherapy.getTherapy().getCompositionUid()), interval);
        linkedToTherapy.setTherapy(convertedTherapy);
      }
      return true;
    }
    if (pair.getFirst() == TherapyLinkType.LINKED_TO_ADMISSION_THERAPY && alreadyHandled.contains(pair.getSecond()))
    {
      if (isDischargeTherapy(therapyInterval, centralCaseEnd, when))
      {
        final TherapyDocumentationData newDischargeTherapy = createTherapyData(therapyToCompare, convertedTherapy, therapyInterval);
        dischargeTherapies.add(newDischargeTherapy);
      }
      else
      {
        final TherapyDocumentationData linkedToTherapy = pair.getSecond();
        final Interval interval =
            linkedToTherapy.findIntervalForId(
                InstructionTranslator.createId(linkedToTherapy.getTherapy().getEhrOrderName(), linkedToTherapy.getTherapy().getCompositionUid()));
        final Interval newInterval = new Interval(interval.getStart(), therapyInterval.getEnd());
        linkedToTherapy.addInterval(InstructionTranslator.createId(
                                        convertedTherapy.getEhrOrderName(),
                                        convertedTherapy.getCompositionUid()), newInterval);
        linkedToTherapy.removeInterval(InstructionTranslator.createId(
                                           linkedToTherapy.getTherapy().getEhrOrderName(),
                                           linkedToTherapy.getTherapy().getCompositionUid()), interval);
      }
      return true;
    }

    final Pair<TherapySimilarityType, TherapyDocumentationData> similarityTypePair =
        getSimilarTherapyPair(admissionTherapies, therapies, therapyToCompare, medicationsDataMap);

    if (similarityTypePair.getFirst() == TherapySimilarityType.SIMILAR_TO_ADMISSION_THERAPY)
    {
      final TherapyDocumentationData similarTherapy = similarityTypePair.getSecond();

      if (isDischargeTherapy(therapyInterval, centralCaseEnd, when))
      {
        final TherapyDocumentationData newTherapy = createTherapyData(therapyToCompare, convertedTherapy, therapyInterval);
        dischargeTherapies.add(newTherapy);
      }
      else
      {
        similarTherapy.addInterval(InstructionTranslator.createId(
                                       convertedTherapy.getEhrOrderName(),
                                       convertedTherapy.getCompositionUid()), therapyInterval);
      }
      return true;
    }
    if (similarityTypePair.getFirst() == TherapySimilarityType.SIMILAR)
    {
      final TherapyDocumentationData similarTherapy = similarityTypePair.getSecond();

      if (isDischargeTherapy(therapyInterval, centralCaseEnd, when))
      {
        if (therapies.contains(similarTherapy))
        {
          therapies.remove(similarTherapy);
        }
        if (admissionTherapies.contains(similarTherapy))
        {
          admissionTherapies.remove(similarTherapy);
        }
        final TherapyDocumentationData dischargeTherapy = createTherapyData(therapyToCompare, convertedTherapy, therapyInterval);

        for (final Pair<String, Interval> pair1 : similarTherapy.getIntervals())
        {
          dischargeTherapy.addInterval(pair1.getFirst(), pair1.getSecond());
        }
        dischargeTherapies.add(dischargeTherapy);
      }
      else
      {
        similarTherapy.addInterval(InstructionTranslator.createId(
                                       convertedTherapy.getEhrOrderName(),
                                       convertedTherapy.getCompositionUid()), therapyInterval);
        similarTherapy.setTherapy(convertedTherapy);
      }
      return true;
    }

    return false;
  }

  private Pair<TherapyLinkType, TherapyDocumentationData> getLinkedToTherapyPair(
      final List<TherapyDocumentationData> admissionTherapies,
      final List<TherapyDocumentationData> therapies,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyToCompare)
  {
    for (final TherapyDocumentationData data : admissionTherapies)
    {
      if (areInstructionsLinkedByUpdate(therapyToCompare, data.getInstructionPair()))
      {
        return Pair.of(TherapyLinkType.LINKED_TO_ADMISSION_THERAPY, data);
      }
    }

    for (final TherapyDocumentationData data : therapies)
    {
      if (areInstructionsLinkedByUpdate(therapyToCompare, data.getInstructionPair()))
      {
        return Pair.of(TherapyLinkType.REGULAR_LINK, data);
      }
    }

    return Pair.of(TherapyLinkType.NONE, null);
  }

  private Pair<TherapySimilarityType, TherapyDocumentationData> getSimilarTherapyPair(
      final List<TherapyDocumentationData> admissionTherapies,
      final List<TherapyDocumentationData> therapies,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapy,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap)
  {
    for (final TherapyDocumentationData data : admissionTherapies)
    {
      final boolean similar = areTherapiesSimilar(therapy, data.getInstructionPair(), medicationsDataMap, true);
      if (similar)
      {
        return Pair.of(TherapySimilarityType.SIMILAR_TO_ADMISSION_THERAPY, data);
      }
    }
    for (final TherapyDocumentationData data : therapies)
    {
      final boolean similar = areTherapiesSimilar(therapy, data.getInstructionPair(), medicationsDataMap, true);
      if (similar)
      {
        return Pair.of(TherapySimilarityType.SIMILAR, data);
      }
    }
    return Pair.of(TherapySimilarityType.NONE, null);
  }

  private boolean areAllIngredientsSolutions(final TherapyDto therapy)
  {
    if (therapy instanceof ComplexTherapyDto)
    {
      final ComplexTherapyDto complexTherapy = (ComplexTherapyDto)therapy;
      for (final InfusionIngredientDto ingredient : complexTherapy.getIngredientsList())
      {
        if (ingredient.getMedication().getMedicationType() != MedicationTypeEnum.SOLUTION)
        {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private List<String> getTherapyDisplayValuesForDocumentation(
      final List<TherapyDocumentationData> therapyList,
      final Locale locale)
  {
    final List<String> strings = new ArrayList<>();

    for (final TherapyDocumentationData therapy : therapyList)
    {
      therapyDisplayProvider.fillDisplayValues(therapy.getTherapy(), true, false, locale);
      String formatted =  therapy.getTherapy().getFormattedTherapyDisplay();
      for (final Pair<String, Interval> pair : therapy.getIntervals())
      {
        final Interval interval = pair.getSecond();
        final String endDate = Intervals.isEndInfinity(interval.getEnd()) ? "..." : DateTimeFormatters.shortDateTime().print(interval.getEnd());
        formatted =
            formatted + " "
                + DateTimeFormatters.shortDateTime().print(interval.getStart()) + " &ndash; "
                + endDate + "<br>";
      }
      strings.add(formatted);
    }
    return strings;
  }

  @Override
  public List<MedicationSearchDto> filterMedicationsTree(
      final List<MedicationSearchDto> medications, final String searchString)
  {
    if (searchString == null)
    {
      return medications;
    }
    final String[] searchSubstrings = searchString.split(" ");
    return filterMedicationsTree(medications, searchSubstrings);
  }

  private List<MedicationSearchDto> filterMedicationsTree(
      final List<MedicationSearchDto> medications, final String[] searchSubstrings)
  {
    final List<MedicationSearchDto> filteredMedications = new ArrayList<>();

    for (final MedicationSearchDto medication : medications)
    {
      final String medicationSearchName =
          medication.getMedication().getGenericName() != null ?
          medication.getMedication().getGenericName() + " " + medication.getTitle() :
          medication.getTitle();

      medication.setExpand(false);
      boolean match = true;

      if (searchSubstrings.length > 0)
      {
        final String firstSearchString = searchSubstrings[0];
        final boolean genericStartsWithFirstSearchString =
            medication.getMedication().getGenericName() != null &&
                StringUtils.startsWithIgnoreCase(medication.getMedication().getGenericName(), firstSearchString);
        final boolean medicationStartsWithFirstSearchString =
            StringUtils.startsWithIgnoreCase(medication.getTitle(), firstSearchString);
        if (!genericStartsWithFirstSearchString && !medicationStartsWithFirstSearchString)
        {
          match = false;
        }
      }
      if (match)
      {
        for (int i = 1; i < searchSubstrings.length; i++)
        {
          if (!StringUtils.containsIgnoreCase(medicationSearchName, searchSubstrings[i]))
          {
            match = false;
            break;
          }
        }
      }
      if (match)
      {
        filteredMedications.add(medication);
        fillMedicationTreeChildren(Collections.singletonList(medication));
      }
      else
      {
        if (!medication.getSublevelMedications().isEmpty())
        {
          final List<MedicationSearchDto> filteredChildren =
              filterMedicationsTree(medication.getSublevelMedications(), searchSubstrings);
          if (!filteredChildren.isEmpty())
          {
            medication.setChildren(filteredChildren);
            filteredMedications.add(medication);
            medication.setExpand(true);
          }
        }
      }
    }
    return filteredMedications;
  }

  private void fillMedicationTreeChildren(final List<MedicationSearchDto> medications)
  {
    for (final MedicationSearchDto medication : medications)
    {
      medication.setChildren(medication.getSublevelMedications());
      fillMedicationTreeChildren(medication.getChildren());
    }
  }
}
