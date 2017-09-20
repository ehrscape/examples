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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.marand.ispek.common.Dictionary;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.ispek.ehr.common.tdo.CompositionEventContext;
import com.marand.ispek.ehr.common.tdo.IspekComposition;
import com.marand.maf.core.EnumUtils;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.maf.core.openehr.visitor.IspekTdoDataSupport;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.ServiceMethod;
import com.marand.maf.core.service.auditing.Auditing;
import com.marand.maf.core.service.auditing.Level;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity.MedicationTimingCluster;
import com.marand.openehr.medications.tdo.MedicationOnDischargeComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.InfusionAdministrationDetailsPurpose;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.openehr.medications.tdo.StructuredDoseCluster;
import com.marand.openehr.medications.tdo.StructuredDoseCluster.RatioNumeratorCluster;
import com.marand.openehr.rm.TdoPathable;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkehr.tagging.dto.TagDto;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import com.marand.thinkmed.medications.ParticipationTypeEnum;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.TherapyTagEnum;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.data.TherapyDocumentationData;
import com.marand.thinkmed.medications.business.data.TherapyLinkType;
import com.marand.thinkmed.medications.business.data.TherapySimilarityType;
import com.marand.thinkmed.medications.business.mapper.MedicationHolderDtoMapper;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.business.util.TherapyUnitsConverter;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.InfusionRateCalculationDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionStatus;
import com.marand.thinkmed.medications.dto.discharge.DischargeSourceMedicationDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeGroupDto;
import com.marand.thinkmed.medications.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.reconsiliation.SourceMedicationDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportElementDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.DvInterval;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.PartyIdentified;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static com.marand.openehr.medications.tdo.AdministrationDetailsCluster.InfusionAdministrationDetailsCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster;
import static com.marand.openehr.medications.tdo.IngredientsAndFormCluster.IngredientCluster.IngredientQuantityCluster;
import static com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition.MedicationReferenceBodyWeightObservation;
import static com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition.MedicationReferenceBodyWeightObservation.HistoryHistory;

/**
 * @author Mitja Lapajne
 */
public class DefaultMedicationsBo implements MedicationsBo, MedicationFromEhrConverter.MedicationDataProvider
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsDao medicationsDao;
  private TaggingOpenEhrDao taggingOpenEhrDao;

  private TherapyDisplayProvider therapyDisplayProvider;
  private ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder;
  private ValueHolder<Map<Long, MedicationRouteDto>> medicationRoutesValueHolder;
  private MedicationHolderDtoMapper medicationHolderDtoMapper;
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler;
  private AdministrationProvider administrationProvider;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Autowired
  public void setTaggingOpenEhrDao(final TaggingOpenEhrDao taggingOpenEhrDao)
  {
    this.taggingOpenEhrDao = taggingOpenEhrDao;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setMedicationsValueHolder(final ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Autowired
  public void setMedicationRoutesValueHolder(final ValueHolder<Map<Long, MedicationRouteDto>> medicationRoutesValueHolder)
  {
    this.medicationRoutesValueHolder = medicationRoutesValueHolder;
  }

  @Autowired
  public void setMedicationHolderDtoMapper(final MedicationHolderDtoMapper medicationHolderDtoMapper)
  {
    this.medicationHolderDtoMapper = medicationHolderDtoMapper;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setMedicationOnAdmissionHandler(final MedicationOnAdmissionHandler medicationOnAdmissionHandler)
  {
    this.medicationOnAdmissionHandler = medicationOnAdmissionHandler;
  }

  @Autowired
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Override
  public Map<Long, MedicationDataForTherapyDto> getMedicationDataForTherapies(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList,
      @Nullable final String careProviderId)
  {
    final Map<Long, MedicationDataForTherapyDto> medicationsMap = new HashMap<>();
    final Set<Long> medicationIds = new HashSet<>();

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionsList)
    {
      final OrderActivity orderActivity = MedicationsEhrUtils.getRepresentingOrderActivity(instructionPair.getSecond());
      medicationIds.addAll(getMedicationIds(orderActivity));
    }
    for (final Long medicationId : medicationIds)
    {
      final MedicationHolderDto medicationHolderDto = medicationsValueHolder.getValue().get(medicationId);
      final MedicationDataForTherapyDto dto =
          medicationHolderDtoMapper.mapToMedicationDataForTherapyDto(medicationHolderDto, careProviderId);
      medicationsMap.put(medicationId, dto);
    }
    return medicationsMap;
  }

  @Override
  public boolean isTherapyModifiedFromLastReview(
      @Nonnull final MedicationInstructionInstruction instruction,
      @Nonnull final List<MedicationActionAction> actionsList,
      @Nonnull final DateTime compositionCreatedTime)
  {
    Preconditions.checkNotNull(instruction, "instruction");
    Preconditions.checkNotNull(actionsList, "actionsList");
    Preconditions.checkNotNull(compositionCreatedTime, "compositionCreatedTime");

    final Optional<DateTime> latestModifyActionTime = actionsList
        .stream()
        .filter(action -> MedicationActionEnum.getActionEnum(action) == MedicationActionEnum.MODIFY_EXISTING)
        .map(a -> DataValueUtils.getDateTime(a.getTime()))
        .max(Comparator.naturalOrder());

    final boolean hasUpdateLink = MedicationsEhrUtils.hasLinksOfType(instruction, EhrLinkType.UPDATE);
    if (!hasUpdateLink && !latestModifyActionTime.isPresent())
    {
      return false;
    }

    final DateTime latestModifyTime =
        latestModifyActionTime.isPresent() && latestModifyActionTime.get().isAfter(compositionCreatedTime)
        ? latestModifyActionTime.get()
        : compositionCreatedTime;

    for (final MedicationActionAction action : actionsList)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      final DateTime actionDateTime = DataValueUtils.getDateTime(action.getTime());
      if (MedicationActionEnum.THERAPY_REVIEW_ACTIONS.contains(actionEnum) && actionDateTime.isAfter(latestModifyTime))
      {
        return false;
      }
    }

    return true;
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
  public DateTime getOriginalTherapyStart(
      @Nonnull final String patientId,
      @Nonnull final MedicationOrderComposition composition)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    Preconditions.checkNotNull(composition, "composition");

    final String originalTherapyId = getOriginalTherapyId(composition);
    return medicationsOpenEhrDao.getTherapyInstructionStart(
        patientId,
        TherapyIdUtils.parseTherapyId(originalTherapyId).getFirst());
  }

  @Override
  public Pair<MedicationOrderComposition, MedicationInstructionInstruction> getInstructionFromLink(
      @Nonnull final String patientId,
      @Nonnull final MedicationInstructionInstruction instruction,
      @Nonnull final EhrLinkType linkType,
      final boolean getLatestVersion)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    Preconditions.checkNotNull(instruction, "instruction");
    Preconditions.checkNotNull(linkType, "linkType");

    final List<Link> updateLinks = MedicationsEhrUtils.getLinksOfType(instruction, linkType);
    return updateLinks.isEmpty() ? null : getInstructionFromLink(patientId, updateLinks.get(0), getLatestVersion);
  }

  @Override
  public Pair<MedicationOrderComposition, MedicationInstructionInstruction> getInstructionFromLink(
      final String patientId,
      final Link link,
      final boolean getLatestVersion)
  {
    final String targetCompositionId = MedicationsEhrUtils.getTargetCompositionIdFromLink(link);
    final String compositionId = getLatestVersion
                                 ? TherapyIdUtils.getCompositionUidWithoutVersion(targetCompositionId)
                                 : targetCompositionId;

    final MedicationOrderComposition composition = medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, compositionId);
    return Pair.of(composition, composition.getMedicationDetail().getMedicationInstruction().get(0));
  }

  @Override
  public boolean isTherapySuspended(final List<MedicationActionAction> actionsList)  //actions sorted by time ascending
  {
    boolean suspended = false;
    for (final MedicationActionAction action : actionsList)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
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

  @Override
  public void sortTherapyTemplates(final TherapyTemplatesDto templates)
  {
    sortTherapyTemplateElements(templates.getOrganizationTemplates());
    sortTherapyTemplateElements(templates.getUserTemplates());
    sortTherapyTemplateElements(templates.getPatientTemplates());
  }

  private void sortTherapyTemplateElements(final List<TherapyTemplateDto> templates)
  {
    final Collator collator = Collator.getInstance();
    for (final TherapyTemplateDto template : templates)
    {
      Collections.sort(
          template.getTemplateElements(), (o1, o2) -> compareTherapiesForSort(o1.getTherapy(), o2.getTherapy(), collator));
    }
  }

  @Override
  public int compareTherapiesForSort(final TherapyDto firstTherapy, final TherapyDto secondTherapy, final Collator collator)
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

  @Override
  public boolean areInstructionsLinkedByUpdate(
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair)
  {
    if (doesInstructionHaveLinkToCompareInstruction(
        instructionPair.getSecond(), compareInstructionPair, EhrLinkType.UPDATE))
    {
      return true;
    }
    return doesInstructionHaveLinkToCompareInstruction(
        compareInstructionPair.getSecond(), instructionPair, EhrLinkType.UPDATE);
  }

  @Override
  public boolean doesInstructionHaveLinkToCompareInstruction(
      final MedicationInstructionInstruction instruction,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair,
      final EhrLinkType linkType)
  {
    for (final Link link : instruction.getLinks())
    {
      if (link.getType().getValue().equals(linkType.getName()))
      {
        final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(link.getTarget().getValue());
        final MedicationOrderComposition compareComposition = compareInstructionPair.getFirst();
        final MedicationInstructionInstruction compareInstruction = compareInstructionPair.getSecond();
        if (TherapyIdUtils.getCompositionUidWithoutVersion(ehrUri.getCompositionId()).equals(
            TherapyIdUtils.getCompositionUidWithoutVersion(compareComposition.getUid().getValue())))
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
    final OrderActivity orderActivity = MedicationsEhrUtils.getRepresentingOrderActivity(instructionPair.getSecond());
    final OrderActivity compareOrderActivity =
        MedicationsEhrUtils.getRepresentingOrderActivity(compareInstructionPair.getSecond());

    final boolean simpleTherapy = MedicationsEhrUtils.isSimpleTherapy(orderActivity);
    final Interval therapyInterval = MedicationsEhrUtils.getInstructionInterval(orderActivity.getMedicationTiming());
    final Interval therapyOrderInterval = MedicationsEhrUtils.getInstructionInterval(compareOrderActivity.getMedicationTiming());
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
    final List<DvCodedText> routes = orderActivity.getAdministrationDetails().getRoute();
    final Long medicationId = getMainMedicationId(orderActivity);
    final String medicationName = orderActivity.getMedicine() != null ? orderActivity.getMedicine().getValue() : null;

    //compare therapy
    final List<DvCodedText> compareRoutes = compareOrderActivity.getAdministrationDetails().getRoute();
    final Long compareMedicationId = getMainMedicationId(compareOrderActivity);
    final String compareMedicationName =
        compareOrderActivity.getMedicine() != null ? compareOrderActivity.getMedicine().getValue() : null;

    final boolean similarMedication =
        isSimilarMedication(medicationId, medicationName, compareMedicationId, compareMedicationName, medicationsMap);
    final boolean sameRoute = CollectionUtils.containsAny(routes, compareRoutes);

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
        ((medication.getAtcGroupCode() == null && compareMedication.getAtcGroupCode() == null) ||
            medication.getAtcGroupCode() != null && compareMedication.getAtcGroupCode() != null &&
                medication.getAtcGroupCode().equals(compareMedication.getAtcGroupCode()));
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
    final boolean compareTherapyBaselineInfusion = isBaselineInfusion(compareOrderActivity);

    if (therapyBaselineInfusion && compareTherapyBaselineInfusion)
    {
      return true;
    }

    if (orderActivity.getIngredientsAndForm().getIngredient().size() == 1 &&
        compareOrderActivity.getIngredientsAndForm().getIngredient().size() == 1)
    {
      //therapy
      final List<DvCodedText> routes = orderActivity.getAdministrationDetails().getRoute();
      final Long medicationId = getMainMedicationId(orderActivity);
      final String medicationName = orderActivity.getIngredientsAndForm().getIngredient().get(0).getName().getValue();

      //compare therapy
      final List<DvCodedText> compareRoutes = compareOrderActivity.getAdministrationDetails().getRoute();
      final Long compareMedicationId = getMainMedicationId(compareOrderActivity);
      final String compareMedicationName =
          compareOrderActivity.getIngredientsAndForm().getIngredient().get(0).getName().getValue();

      final boolean sameRoute = CollectionUtils.containsAny(routes, compareRoutes);
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
      return infusionDetails.getPurposeEnum() == InfusionAdministrationDetailsPurpose.BASELINE_ELECTROLYTE_INFUSION;
    }
    return false;
  }

  @Override
  public List<Long> getMedicationIds(final OrderActivity orderActivity)
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

  @Override
  public List<Long> getMedicationIds(final MedicationActionAction medicationAction)
  {
    final List<Long> medicationIds = new ArrayList<>();
    final boolean simpleTherapy = MedicationsEhrUtils.isSimpleTherapy(medicationAction);
    if (simpleTherapy)
    {
      if (medicationAction.getMedicine() instanceof DvCodedText)
      {
        medicationIds.add(Long.parseLong(((DvCodedText)medicationAction.getMedicine()).getDefiningCode().getCodeString()));
      }
    }
    else
    {
      for (final IngredientCluster ingredient : medicationAction.getIngredientsAndForm().getIngredient())
      {
        if (ingredient.getName() instanceof DvCodedText)
        {
          medicationIds.add(Long.parseLong(((DvCodedText)ingredient.getName()).getDefiningCode().getCodeString()));
        }
      }
    }
    return medicationIds;
  }

  @Override
  public Long getMainMedicationId(final OrderActivity orderActivity)
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

  private boolean isOnlyOnceThenEx(final MedicationTimingCluster medicationTiming)
  {
    return
        medicationTiming.getTimingDescription() != null &&
            medicationTiming.getTimingDescription().getValue().equals(
                DosingFrequencyTypeEnum.getFullString(DosingFrequencyTypeEnum.ONCE_THEN_EX));
  }

  @Override
  public boolean isMentalHealthMedication(final long medicationId)
  {
    final Map<Long, MedicationHolderDto> value = medicationsValueHolder.getValue();
    final MedicationHolderDto medicationHolderDto = value.get(medicationId);

    return medicationHolderDto != null && medicationHolderDto.isMentalHealthDrug();
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
  public boolean isTherapySuspended(final MedicationOrderComposition composition, final MedicationInstructionInstruction instruction)
  {
    final List<MedicationActionAction> actions = MedicationsEhrUtils.getInstructionActions(composition, instruction);
    return isTherapySuspended(actions);
  }

  @Override
  public TherapyChangeReasonDto getTherapySuspendReason(
      @Nonnull final MedicationOrderComposition composition,
      @Nonnull final MedicationInstructionInstruction instruction)
  {
    Preconditions.checkNotNull(composition, "composition is required");
    Preconditions.checkNotNull(instruction, "instruction is required");

    final List<MedicationActionAction> actions = MedicationsEhrUtils.getInstructionActions(composition, instruction);
    TherapyChangeReasonDto suspendReason = null;
    for (final MedicationActionAction action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.SUSPEND)
      {
        suspendReason = MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(action);
      }
      else if (actionEnum == MedicationActionEnum.REISSUE)
      {
        suspendReason = null;
      }
    }
    return suspendReason;
  }

  @Override
  public boolean isTherapyCancelledOrAborted(
      final MedicationOrderComposition composition, final MedicationInstructionInstruction instruction)
  {
    final List<MedicationActionAction> actions = MedicationsEhrUtils.getInstructionActions(composition, instruction);
    return isTherapyCanceledOrAborted(actions);
  }

  @Override
  public boolean isMedicationTherapyCompleted(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    final List<MedicationActionAction> instructionActions = MedicationsEhrUtils.getInstructionActions(
        composition,
        instruction);
    for (final MedicationActionAction action : instructionActions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (MedicationActionEnum.THERAPY_FINISHED.contains(actionEnum))
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public MedicationActionAction getInstructionAction(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction,
      final MedicationActionEnum searchActionEnum,
      @Nullable final Interval searchInterval)
  {
    if (composition.getMedicationDetail().getMedicationInstruction().size() == 1)
    {
      final List<MedicationActionAction> actions = composition.getMedicationDetail().getMedicationAction();
      for (final MedicationActionAction action : actions)
      {
        final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
        if (actionEnum == searchActionEnum)
        {
          final DateTime actionDateTime = DataValueUtils.getDateTime(action.getTime());
          if (searchInterval == null || searchInterval.contains(actionDateTime))
          {
            return action;
          }
        }
      }
      return null;
    }
    else
    {
      final String instructionPath = TdoPathable.pathOfItem(composition, instruction).getCanonicalString();

      for (final MedicationActionAction action : composition.getMedicationDetail().getMedicationAction())
      {
        if (action.getInstructionDetails().getInstructionId().getPath().equals(instructionPath))
        {
          final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
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
  }

  @Override
  public String getTherapyFormattedDisplay(
      final String patientId, final String therapyId, final DateTime when, final Locale locale)
  {
    final TherapyDto therapy = getTherapy(patientId, therapyId, when, locale);
    return therapy.getFormattedTherapyDisplay();
  }

  @Override
  public TherapyDto getTherapy(
      final String patientId, final String compositionId, final String ehrOrderName, final DateTime when, final Locale locale)
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> medicationInstructionPair =
        medicationsOpenEhrDao.getTherapyInstructionPair(patientId, compositionId, ehrOrderName);
    return convertInstructionToTherapyDtoWithDisplayValues(
        medicationInstructionPair.getFirst(),
        medicationInstructionPair.getSecond(),
        null,
        null,
        when,
        true,
        locale);
  }

  private TherapyDto getTherapy(
      final String patientId, final String therapyId, final DateTime when, final Locale locale)
  {
    final Pair<String, String> compositionIdAndInstructionName = TherapyIdUtils.parseTherapyId(therapyId);
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> medicationInstructionPair =
        medicationsOpenEhrDao.getTherapyInstructionPair(
            patientId, compositionIdAndInstructionName.getFirst(), compositionIdAndInstructionName.getSecond());
    return convertInstructionToTherapyDtoWithDisplayValues(
        medicationInstructionPair.getFirst(),
        medicationInstructionPair.getSecond(),
        null,
        null,
        when,
        true,
        locale);
  }

  @Override
  public TherapyDto convertInstructionToTherapyDto(
      @Nonnull final IspekComposition composition,
      @Nonnull final MedicationInstructionInstruction instruction,
      final DateTime currentTime)
  {
    Preconditions.checkNotNull(composition, "composition must not be null");
    Preconditions.checkNotNull(instruction, "instruction must not be null");

    final MedicationFromEhrConverter<?> converter = MedicationConverterSelector.getConverter(instruction);
    final TherapyDto therapyDto = converter.createTherapyFromInstruction(
        instruction,
        ((Composition)composition).getUid().getValue(),
        instruction.getName().getValue(),
        DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
        currentTime,
        this);

    final PartyIdentified composer = (PartyIdentified)((Composition)composition).getComposer();
    therapyDto.setComposerName(composer.getName());
    final DvText prescriberCode = DataValueUtils.getText(ParticipationTypeEnum.PRESCRIBER.getCode());

    instruction.getOtherParticipations()
        .stream()
        .filter(participation -> participation.getFunction().equals(prescriberCode))
        .forEach(participation -> therapyDto.setPrescriberName(((PartyIdentified)participation.getPerformer()).getName()));

    return therapyDto;
  }

  @Override
  public TherapyDto convertInstructionToTherapyDtoWithDisplayValues(
      @Nonnull final IspekComposition composition,
      @Nonnull final MedicationInstructionInstruction instruction,
      final Double referenceWeight,
      final Double patientHeight,
      final DateTime currentTime,
      final boolean isToday,
      final Locale locale)
  {
    Preconditions.checkNotNull(composition, "composition");
    Preconditions.checkNotNull(instruction, "instruction");

    final TherapyDto therapyDto = convertInstructionToTherapyDto(composition, instruction, currentTime);

    if (therapyDto instanceof ComplexTherapyDto && referenceWeight != null)
    {
      fillInfusionFormulaFromRate((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight);
    }

    fillDisplayValues(therapyDto, referenceWeight, patientHeight, isToday, locale);
    return therapyDto;
  }

  @Override
  public void fillDisplayValues(
      @Nonnull final TherapyDto therapy,
      final Double referenceWeight,
      final Double patientHeight,
      final boolean isToday,
      final Locale locale)
  {
    Preconditions.checkNotNull(therapy, "therapy");

    if (locale != null)
    {
      therapyDisplayProvider.fillDisplayValues(therapy, isToday, isToday, locale);
    }
  }

  @Override
  public void fillInfusionFormulaFromRate(
      final ComplexTherapyDto therapy,
      final Double referenceWeight,
      final Double patientHeight)
  {
    final InfusionRateCalculationDto calculationData = getInfusionRateCalculationData(therapy);
    if (calculationData != null && calculationData.getQuantity() != null && calculationData.getQuantityDenominator() != null)
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

    final Double rateWithPatientUnit = getRateWithPatientUnits(rate, patientHeight, referenceWeight, formulaPatientUnit);

    final Double rateInMassUnit = rateWithPatientUnit * calculationDto.getQuantity() / calculationDto.getQuantityDenominator(); // mg/kg/h
    final Double rateInFormulaMassUnit =
        TherapyUnitsConverter.convertToUnit(rateInMassUnit, calculationDto.getQuantityUnit(), formulaMassUnit); // ug/kg/h
    if (rateInFormulaMassUnit != null)
    {
      final Double timeRatio = TherapyUnitsConverter.convertToUnit(1.0, "h", formulaTimeUnit);
      return rateInFormulaMassUnit / timeRatio;  // ug/kg/min
    }
    return null;
  }

  private Double getRateWithPatientUnits(
      final Double rate,
      final Double patientHeight,
      final Double referenceWeight,
      final String formulaPatientUnit)
  {
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
    return rateWithPatientUnit;
  }

  private Double calculateInfusionRateInMassUnit(
      final Double rate,
      final InfusionRateCalculationDto calculationDto,
      final Double patientHeight,
      final String formulaUnit,
      final Double referenceWeight)
  {
    final String[] formulaUnitParts = Pattern.compile("/").split(formulaUnit);
    String formulaPatientUnit = null;

    if (formulaUnitParts.length != 2)
    {
      formulaPatientUnit = formulaUnitParts[1];
    }

    final Double rateWithPatientUnit = getRateWithPatientUnits(rate, patientHeight, referenceWeight, formulaPatientUnit);

    return rateWithPatientUnit * calculationDto.getQuantity() / calculationDto.getQuantityDenominator();
  }

  @Override
  public void fillInfusionRateFromFormula(
      final ComplexTherapyDto therapy, final Double referenceWeight, final Double patientHeight)
  {
    final InfusionRateCalculationDto calculationData = getInfusionRateCalculationData(therapy);
    if (calculationData != null && calculationData.getQuantity() != null && calculationData.getQuantityDenominator() != null)
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

          if (rate != null)
          {
            doseElement.setRate(rate);
          }
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

            if (rate != null)
            {
              timedDoseElement.getDoseElement().setRate(rate);
            }
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
    return formulaInHours * calculationDto.getQuantityDenominator() / calculationDto.getQuantity(); // ml/h
  }

  @Override
  public double calculateBodySurfaceArea(final double heightInCm, final double weightInKg)
  {
    return Math.sqrt((heightInCm * weightInKg) / 3600.0);
  }

  InfusionRateCalculationDto getInfusionRateCalculationData(final ComplexTherapyDto therapy)
  {
    if (therapy.getIngredientsList().size() == 1)
    {
      final InfusionIngredientDto onlyInfusionIngredient = therapy.getIngredientsList().get(0);
      if (MedicationTypeEnum.MEDS_AND_SUPPS.contains(onlyInfusionIngredient.getMedication().getMedicationType()))
      {
        if (therapy.isContinuousInfusion() && onlyInfusionIngredient.getMedication().getId() != null)
        {
          final MedicationIngredientDto medicationDefiningIngredient =
              medicationsValueHolder.getValue().get(onlyInfusionIngredient.getMedication().getId()).getDefiningIngredient();
          if (medicationDefiningIngredient != null &&
              ("ml".equals(medicationDefiningIngredient.getStrengthDenominatorUnit()) ||
                  "mL".equals(medicationDefiningIngredient.getStrengthDenominatorUnit())))
          {
            final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
            calculationDto.setQuantity(medicationDefiningIngredient.getStrengthNumerator());
            calculationDto.setQuantityUnit(medicationDefiningIngredient.getStrengthNumeratorUnit());
            calculationDto.setQuantityDenominator(medicationDefiningIngredient.getStrengthDenominator());
            return calculationDto;
          }
        }
        else
        {
          final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
          calculationDto.setQuantity(onlyInfusionIngredient.getQuantity());
          calculationDto.setQuantityUnit(onlyInfusionIngredient.getQuantityUnit());
          calculationDto.setQuantityDenominator(onlyInfusionIngredient.getQuantityDenominator());
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
          calculationDto.setQuantityDenominator(therapy.getVolumeSum());
        }
      }
      return calculationDto;
    }
    return null;
  }

  @Override
  public List<MedicationForWarningsSearchDto> getTherapiesForWarningsSearch(final String patientId, final DateTime when)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, Intervals.infiniteFrom(when), null);

    return extractWarningsSearchDtos(medicationInstructionsList.stream().map(Pair::getFirst).collect(Collectors.toList()));
  }

  @Override
  public List<TherapyDto> getTherapies(
      final String patientId,
      final String centralCaseId,
      final Double referenceWeight,
      @Nullable final Locale locale,
      final DateTime when)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, null, centralCaseId);

    return convertInstructionsToTherapies(medicationInstructionsList, referenceWeight, null, locale, when);
  }

  @Override
  public List<TherapyDto> getTherapies(
      final String patientId,
      final Interval searchInterval,
      final Double referenceWeight,
      final Double patientHeight,
      @Nullable final Locale locale,
      final DateTime when)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, searchInterval, null);

    return convertInstructionsToTherapies(medicationInstructionsList, referenceWeight, patientHeight, locale, when);
  }

  @Override
  public List<MedicationOnDischargeGroupDto> getMedicationOnDischargeGroups(
      final String patientId,
      final DateTime lastHospitalizationStart,
      @Nullable final Interval searchInterval,
      final Double referenceWeight,
      final Double patientHeight, // is this @Nullable ?
      @Nullable final Locale locale,
      final DateTime when)
  {
    final List<MedicationOnDischargeGroupDto> dischargeGroupsDtoList = new ArrayList<>();

    final Map<String, Pair<TherapyStatusEnum, TherapyChangeReasonDto>> reasonsForCompositionsFromAdmission =
        medicationsOpenEhrDao.getLastChangeReasonsForCompositionsFromAdmission(patientId, false);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, searchInterval, null);

    final Map<String, String> linkedAdmissionCompositions = findLinkedMedicationsOnAdmissionFromInstructions(
        medicationInstructionsList);

    final List<TherapyDto> therapiesList = convertInstructionsToTherapies(
        medicationInstructionsList,
        referenceWeight,
        patientHeight,
        locale,
        when);

    // create inpatient therapies group
    final MedicationOnDischargeGroupDto inpatientTherapies = new MedicationOnDischargeGroupDto();
    inpatientTherapies.setGroupEnum(TherapySourceGroupEnum.INPATIENT_THERAPIES);
    inpatientTherapies.setGroupName(
        Dictionary.getEntry(EnumUtils.getIdentifier(TherapySourceGroupEnum.INPATIENT_THERAPIES), locale));

    // fill inpatient therapies group
    for (final TherapyDto therapyDto : therapiesList)
    {
      final String compositionId = therapyDto.getCompositionUid();
      final DischargeSourceMedicationDto sourceMedicationDto = new DischargeSourceMedicationDto();

      therapyDto.setCompositionUid(null);
      sourceMedicationDto.setTherapy(therapyDto);
      sourceMedicationDto.setSourceId(compositionId);
      sourceMedicationDto.setReviewed(
          taggingOpenEhrDao.getTags(compositionId).contains(new TagDto(TherapyTagEnum.REVIEWED_ON_DISCHARGE.getPrefix())));

      if (therapyDto.isLinkedToAdmission())
      {
        // set change reason for inpatient therapy
        for (final Map.Entry<String, String> entry : linkedAdmissionCompositions.entrySet())
        {
          if (entry.getValue().equals(TherapyIdUtils.getCompositionUidWithoutVersion(compositionId)))
          {
            final Pair<TherapyStatusEnum, TherapyChangeReasonDto> reasonDtoPair = reasonsForCompositionsFromAdmission
                .get(entry.getKey());

            if (reasonDtoPair != null)
            {
              sourceMedicationDto.setChangeReason(reasonDtoPair.getSecond());
              sourceMedicationDto.setStatus(reasonDtoPair.getFirst());
            }
          }
        }
      }
      inpatientTherapies.getGroupElements().add(sourceMedicationDto);
    }

    final List<MedicationOnAdmissionDto> existingMedicationsOnAdmission = medicationOnAdmissionHandler.getMedicationsOnAdmission(
        patientId,
        lastHospitalizationStart,
        when,
        locale);

    // create admission therapies group
    final MedicationOnDischargeGroupDto therapiesOnAdmission = new MedicationOnDischargeGroupDto();
    therapiesOnAdmission.setGroupEnum(TherapySourceGroupEnum.MEDICATION_ON_ADMISSION);
    therapiesOnAdmission.setGroupName(
        Dictionary.getEntry(
            EnumUtils.getIdentifier(TherapySourceGroupEnum.MEDICATION_ON_ADMISSION), locale));

    // fill admission therapies group
    for (final MedicationOnAdmissionDto admissionDto : existingMedicationsOnAdmission)
    {
      final boolean isSuspendedOrPending = admissionDto.getStatus() == MedicationOnAdmissionStatus.SUSPENDED ||
          admissionDto.getStatus() == MedicationOnAdmissionStatus.PENDING;

      final String compositionUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(
          admissionDto.getTherapy()
              .getCompositionUid());

      if (linkedAdmissionCompositions.get(compositionUidWithoutVersion) == null)
      {
        final DischargeSourceMedicationDto sourceMedicationDto = new DischargeSourceMedicationDto();
        final String compositionId = admissionDto.getTherapy().getCompositionUid();

        admissionDto.getTherapy().setCompositionUid(null);
        admissionDto.getTherapy().setLinkedToAdmission(true);

        //TODO Nejc check
        if (admissionDto.getStatus() == MedicationOnAdmissionStatus.SUSPENDED)
        {
          sourceMedicationDto.setStatus(TherapyStatusEnum.SUSPENDED);
        }
        sourceMedicationDto.setTherapy(admissionDto.getTherapy());
        sourceMedicationDto.setSourceId(compositionId);
        sourceMedicationDto.setReviewed(
            taggingOpenEhrDao.getTags(compositionId).contains(new TagDto(TherapyTagEnum.REVIEWED_ON_DISCHARGE.getPrefix())));

        final Pair<TherapyStatusEnum, TherapyChangeReasonDto> changeReasonDtoPair = reasonsForCompositionsFromAdmission
            .get(compositionUidWithoutVersion);

        if (changeReasonDtoPair != null)
        {
          sourceMedicationDto.setChangeReason(changeReasonDtoPair.getSecond());
          sourceMedicationDto.setStatus(changeReasonDtoPair.getFirst());
        }

        if (isSuspendedOrPending)
        {
          inpatientTherapies.getGroupElements().add(sourceMedicationDto);
        }
        else
        {
          therapiesOnAdmission.getGroupElements().add(sourceMedicationDto);
        }
      }
    }
    sortTherapiesByAdmissionLink(inpatientTherapies.getGroupElements());

    // add inpatient and admission therapies groups to list
    dischargeGroupsDtoList.add(inpatientTherapies);
    //TODO Nejc check
    //dischargeGroupsDtoList.add(therapiesOnAdmission);

    return dischargeGroupsDtoList;
  }

  @Override
  public List<MentalHealthTherapyDto> getMentalHealthTherapies(
      final String patientId,
      final Interval searchInterval,
      final DateTime when,
      final Locale locale)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, searchInterval, null);

    final List<MentalHealthTherapyDto> mentalHealthTherapyDtos = extractMentalHealthTherapiesList(medicationInstructionsList);
    final Set<MentalHealthTherapyDto> filteredMentalHealthTherapyList = filterMentalHealthTherapyList(mentalHealthTherapyDtos);

    return new ArrayList(filteredMentalHealthTherapyList);
  }

  @Override
  public List<TherapyDto> getLinkTherapyCandidates(
      @Nonnull final String patientId,
      @Nonnull final Double referenceWeight,
      final Double patientHeight,
      @Nonnull final DateTime when,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(referenceWeight, "referenceWeight must not be null");
    Preconditions.checkNotNull(when, "when must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> continuousInfusionInstructions =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, Intervals.infiniteFrom(when), null)
            .stream()
            .filter(this::isContinuousInfusion)
            .collect(Collectors.toList());

    final Set<String> followedCompositionIds = continuousInfusionInstructions
        .stream()
        .filter(pair -> !isTherapyCancelledOrAborted(pair.getFirst(), pair.getSecond()))
        .filter(pair -> hasFollowLink(pair.getSecond()))
        .map(this::getFollowLinkCompositionUid)
        .collect(Collectors.toSet());

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> followInstructionCandidates =
        continuousInfusionInstructions
            .stream()
            .filter(pair -> !isTherapyCancelledOrAborted(pair.getFirst(), pair.getSecond()))
            .filter(this::hasTherapyEnd)
            .filter(pair -> !followedCompositionIds.contains(
                TherapyIdUtils.getCompositionUidWithoutVersion(pair.getFirst().getUid().getValue())))
            .collect(Collectors.toList());

    return convertInstructionsToTherapies(followInstructionCandidates, referenceWeight, patientHeight, locale, when);
  }

  private boolean hasFollowLink(final MedicationInstructionInstruction instruction)
  {
    return !MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.FOLLOW).isEmpty();
  }

  private String getFollowLinkCompositionUid(final Pair<MedicationOrderComposition, MedicationInstructionInstruction> pair)
  {
    final List<Link> followLinks = MedicationsEhrUtils.getLinksOfType(pair.getSecond(), EhrLinkType.FOLLOW);

    if (followLinks.isEmpty())
    {
      return null;
    }
    else
    {
      final String targetCompositionId = MedicationsEhrUtils.getTargetCompositionIdFromLink(followLinks.get(0));
      return TherapyIdUtils.getCompositionUidWithoutVersion(targetCompositionId);
    }
  }

  private boolean hasTherapyEnd(final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instruction)
  {
    final OrderActivity orderActivity = instruction.getSecond().getOrder().get(0);
    final MedicationTimingCluster medicationTiming = orderActivity.getMedicationTiming();
    return medicationTiming.getStopDate() != null;
  }

  private boolean isContinuousInfusion(final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instruction)
  {
    final OrderActivity orderActivity = instruction.getSecond().getOrder().get(0);
    final AdministrationDetailsCluster administration = orderActivity.getAdministrationDetails();
    return MedicationDeliveryMethodEnum.isContinuousInfusion(administration.getDeliveryMethod());
  }

  Set<MentalHealthTherapyDto> filterMentalHealthTherapyList(final List<MentalHealthTherapyDto> mentalHealthTherapyDtos)
  {
    final Set<MentalHealthTherapyDto> removedDuplicates = new TreeSet<>((o1, o2) -> {
      final int statusCompare =  o1.getTherapyStatusEnum().compareTo(o2.getTherapyStatusEnum());
      if (statusCompare == 0)
      {
        final Long o1Id = o1.getMentalHealthMedicationDto().getId();
        final Long o2Id = o2.getMentalHealthMedicationDto().getId();

        final int idCompare = o1Id.compareTo(o2Id);

        final Long o1RouteId = o1.getMentalHealthMedicationDto().getRoute().getId();
        final Long o2RouteId = o2.getMentalHealthMedicationDto().getRoute().getId();

        return idCompare == 0 ? o1RouteId.compareTo(o2RouteId) : idCompare;
      }
      else
      {
        return statusCompare;
      }
    });

    removedDuplicates.addAll(mentalHealthTherapyDtos);
    return removedDuplicates;
  }

  private void sortTherapiesByAdmissionLink(
      final List<SourceMedicationDto> therapies)
  {
    final Collator collator = Collator.getInstance();
    Collections.sort(
        therapies, (o1, o2) -> {
          if (o1.getTherapy().isLinkedToAdmission() && !o2.getTherapy().isLinkedToAdmission())
          {
            return -1;
          }
          if (!o1.getTherapy().isLinkedToAdmission() && o2.getTherapy().isLinkedToAdmission())
          {
            return 1;
          }
          return compareTherapiesForSort(o1.getTherapy(), o2.getTherapy(), collator);
        });
  }

  private Map<String, String> findLinkedMedicationsOnAdmissionFromInstructions(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList)
  {
    final Map<String, String> linkedAdmissionIdWithInpatientId = new HashMap<>();
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : medicationInstructionsList)
    {
      final List<Link> admissionLinks = MedicationsEhrUtils.getLinksOfType(
          instructionPair.getSecond(),
          EhrLinkType.MEDICATION_ON_ADMISSION);

      if (!admissionLinks.isEmpty())
      {
        final DvEhrUri target = admissionLinks.get(0).getTarget();
        final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(target.getValue());
        linkedAdmissionIdWithInpatientId.put(
            TherapyIdUtils.getCompositionUidWithoutVersion(ehrUri.getCompositionId()),
            TherapyIdUtils.getCompositionUidWithoutVersion(instructionPair.getFirst().getUid().getValue()));
      }
    }
    return linkedAdmissionIdWithInpatientId;
  }

  private List<TherapyDto> convertInstructionsToTherapies(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList,
      final Double referenceWeight,
      final Double patientHeight,
      @Nullable final Locale locale,
      final DateTime when)
  {
    final List<TherapyDto> therapiesList = new ArrayList<>();
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : medicationInstructionsList)
    {
      final List<MedicationActionAction> actions =
          MedicationsEhrUtils.getInstructionActions(instructionPair.getFirst(), instructionPair.getSecond());
      final boolean therapyEndedWithModify = isTherapyCompletedWithModify(actions);
      if (!therapyEndedWithModify)
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

    return therapiesList;
  }

  List<MentalHealthTherapyDto> extractMentalHealthTherapiesList(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructionsList)
  {
    final List<MentalHealthTherapyDto> mentalHealthTherapyDtos = new ArrayList<>();
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> medicationInstruction : medicationInstructionsList)
    {
      final OrderActivity orderActivity = MedicationsEhrUtils.getRepresentingOrderActivity(medicationInstruction.getSecond());
      final List<Long> medicationIds = getMedicationIds(orderActivity);

      mentalHealthTherapyDtos.addAll(
          medicationIds.stream()
              .filter(this::isMentalHealthMedication)
              .map(medicationId -> buildMentalHealthTherapyDto(orderActivity, medicationId, medicationInstruction))
              .collect(Collectors.toList()));
    }
    return mentalHealthTherapyDtos;
  }

  private MentalHealthTherapyDto buildMentalHealthTherapyDto(
      final OrderActivity orderActivity,
      final Long medicationId,
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> medicationInstruction)
  {
    final MentalHealthTherapyDto mentalHealthTherapyDto = new MentalHealthTherapyDto();
    final MedicationDto medicationDto = getMedication(medicationId);

    TherapyStatusEnum therapyStatusEnum = TherapyStatusEnum.NORMAL;
    if (isTherapySuspended(MedicationsEhrUtils.getInstructionActions(medicationInstruction.getFirst(), medicationInstruction.getSecond())))
    {
      therapyStatusEnum = TherapyStatusEnum.SUSPENDED;
    }
    else if (isTherapyCancelledOrAborted(medicationInstruction.getFirst(), medicationInstruction.getSecond()))
    {
      therapyStatusEnum = TherapyStatusEnum.ABORTED;
    }

    final String routeId = orderActivity.getAdministrationDetails().getRoute().get(0).getDefiningCode().getCodeString();

    final MentalHealthMedicationDto mentalHealthMedicationDto = new MentalHealthMedicationDto(
        medicationId,
        medicationDto.getShortName(),
        medicationDto.getGenericName(),
        getMedicationRoute(Long.valueOf(routeId)));

    mentalHealthTherapyDto.setMentalHealthMedicationDto(mentalHealthMedicationDto);
    mentalHealthTherapyDto.setGenericName(medicationDto.getGenericName());
    mentalHealthTherapyDto.setTherapyStatusEnum(therapyStatusEnum);

    return mentalHealthTherapyDto;
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
      fillInfusionFormulaFromRate((ComplexTherapyDto)therapy, referenceWeight, patientHeight);
    }

    return therapy;
  }

  @Override
  public List<TherapySurgeryReportElementDto> getTherapySurgeryReportElements(
      @Nonnull final String patientId,
      final Double patientHeight,
      @Nonnull final DateTime searchStart,
      @Nonnull final RoundsIntervalDto roundsIntervalDto,
      @Nonnull final Locale locale,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(patientId, "patientId");
    Preconditions.checkNotNull(searchStart, "searchStart");
    Preconditions.checkNotNull(roundsIntervalDto, "roundsIntervalDto");
    Preconditions.checkNotNull(locale, "locale");
    Preconditions.checkNotNull(when, "when");

    final DateTime roundsStart = new DateTime(
        searchStart.getYear(),
        searchStart.getMonthOfYear(),
        searchStart.getDayOfMonth(),
        roundsIntervalDto.getStartHour(),
        roundsIntervalDto.getStartMinute());

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList =
        medicationsOpenEhrDao.findMedicationInstructions(
            patientId,
            Intervals.infiniteFrom(roundsStart),
            null);

    final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(
        patientId,
        Intervals.infiniteTo(searchStart));


    final List<TherapySurgeryReportElementDto> elements = new ArrayList<>();
    final Map<Long, MedicationDataForTherapyDto> medicationsMap = getMedicationDataForTherapies(instructionsList, null);

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionsList)
    {
      final MedicationOrderComposition composition = instructionPair.getFirst();
      final MedicationInstructionInstruction instruction = instructionPair.getSecond();

      final MedicationTimingCluster medicationTiming = instruction.getOrder().get(0).getMedicationTiming();
      final Interval instructionInterval = MedicationsEhrUtils.getInstructionInterval(medicationTiming);
      final boolean onlyOnce = isOnlyOnceThenEx(medicationTiming);
      if (instructionInterval.overlaps(Intervals.infiniteFrom(searchStart)) ||
          onlyOnce && instructionInterval.getStart().isAfter(searchStart.minusHours(1)))
      {
        final List<MedicationActionAction> actions = MedicationsEhrUtils.getInstructionActions(composition, instruction);
        if (!isTherapyCanceledAbortedOrSuspended(actions))
        {
          final TherapyDto therapy = getTherapyFromMedicationInstruction(
              composition,
              instruction,
              referenceWeight,
              patientHeight,
              when);

          therapyDisplayProvider.fillDisplayValues(therapy, false, true, false, locale, true);

          final boolean containsAntibiotics = getMedicationIds(MedicationsEhrUtils.getRepresentingOrderActivity(instruction))
              .stream()
              .anyMatch(id -> medicationsMap.containsKey(id) && medicationsMap.get(id).isAntibiotic());

          if (containsAntibiotics)
          {
            final int consecutiveDays = getTherapyConsecutiveDay(
                getOriginalTherapyStart(patientId, composition),
                when,
                when,
                therapy.getPastDaysOfTherapy());

            elements.add(new TherapySurgeryReportElementDto(therapy.getFormattedTherapyDisplay(), consecutiveDays));
          }
          else
          {
            elements.add(new TherapySurgeryReportElementDto(therapy.getFormattedTherapyDisplay()));
          }
        }
      }
    }
    return elements;
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
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.CANCEL || actionEnum == MedicationActionEnum.ABORT)
      {
        return true;
      }
    }
    return false;
  }

  private boolean isTherapyCompletedWithModify(final List<MedicationActionAction> actions)
  {
    for (final MedicationActionAction action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.COMPLETE)
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<MedicationForWarningsSearchDto> extractWarningsSearchDtos(
      @Nonnull final List<MedicationOrderComposition> compositions)
  {
    Preconditions.checkNotNull(compositions, "compositions must not be null");

    final List<MedicationForWarningsSearchDto> medicationSummariesList = new ArrayList<>();
    for (final MedicationOrderComposition composition : compositions)
    {
      final OrderActivity orderActivity = MedicationsEhrUtils.getRepresentingOrderActivity(
          composition.getMedicationDetail().getMedicationInstruction().get(0));

      if (orderActivity.getIngredientsAndForm() != null && !orderActivity.getIngredientsAndForm().getIngredient().isEmpty())
      {
        for (final IngredientCluster ingredient : orderActivity.getIngredientsAndForm().getIngredient())
        {
          final MedicationForWarningsSearchDto medicationDto =
              buildWarningSearchDtoFromIngredient(orderActivity, ingredient);
          if (medicationDto != null)
          {
            medicationSummariesList.add(medicationDto);
          }
        }
      }
      else
      {
        final MedicationForWarningsSearchDto medicationDto = buildWarningSearchDtoFromMedication(orderActivity);
        if (medicationDto != null)
        {
          medicationSummariesList.add(medicationDto);
        }
      }
    }
    return medicationSummariesList;
  }

  MedicationForWarningsSearchDto buildWarningSearchDtoFromIngredient(
      final OrderActivity orderActivity, final IngredientCluster ingredient)
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
      return buildWarningSearchDto(orderActivity.getMedicationTiming(), medicationId, route, doseAmount, doseUnit);
    }
    return null;
  }

  MedicationForWarningsSearchDto buildWarningSearchDtoFromMedication(final OrderActivity orderActivity)
  {
    if (orderActivity.getMedicine() instanceof DvCodedText) //if DvCodedText then medication exists in database
    {
      final String definingCode = ((DvCodedText)orderActivity.getMedicine()).getDefiningCode().getCodeString();
      final Long medicationId = Long.parseLong(definingCode);

      Double doseAmount = null;
      String doseUnit = null;

      final StructuredDoseCluster structuredDose = orderActivity.getStructuredDose();
      if (structuredDose != null)
      {
        final DataValue quantity = structuredDose.getQuantity();
        if (quantity != null)
        {
          doseUnit = structuredDose.getDoseUnit().getDefiningCode().getCodeString();
          doseAmount = quantity instanceof DvQuantity
                       ? ((DvQuantity)quantity).getMagnitude()
                       : ((DvQuantity)((DvInterval)quantity).getUpper()).getMagnitude();
        }
        else
        {
          final RatioNumeratorCluster numerator = structuredDose.getRatioNumerator();
          if (numerator != null)
          {
            doseUnit = numerator.getDoseUnit().getDefiningCode().getCodeString();
            doseAmount = numerator.getAmount() instanceof DvQuantity
                         ? ((DvQuantity)numerator.getAmount()).getMagnitude()
                         : ((DvQuantity)((DvInterval)numerator.getAmount()).getUpper()).getMagnitude();
          }
        }
      }

      final String route = !orderActivity.getAdministrationDetails().getRoute().isEmpty() ?
                           orderActivity.getAdministrationDetails().getRoute().get(0).getDefiningCode().getCodeString() :
                           null;

      return buildWarningSearchDto(orderActivity.getMedicationTiming(), medicationId, route, doseAmount, doseUnit);
    }
    return null;
  }

  MedicationForWarningsSearchDto buildWarningSearchDto(
      final MedicationTimingCluster medicationTimingCluster,
      final Long medicationId,
      final String route,
      final Double doseAmount,
      final String doseUnit)
  {
    final MedicationForWarningsSearchDto summary = new MedicationForWarningsSearchDto();
    final DateTime start = DataValueUtils.getDateTime(medicationTimingCluster.getStartDate());
    final DateTime stop = DataValueUtils.getDateTime(medicationTimingCluster.getStopDate());
    summary.setEffective(
        new Interval(
            start != null ? start : Intervals.INFINITE.getStart(),
            stop != null ? stop : Intervals.INFINITE.getEnd())
    );
    final MedicationDto medicationDto = getMedication(medicationId);
    if (medicationDto == null)
    {
      throw new IllegalArgumentException("Medication with id: " + medicationId + " not found!");
    }
    summary.setName(medicationDto.getName());
    summary.setShortName(medicationDto.getShortName());
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
      final int hours = DataValueUtils.getPeriod(medicationTiming.getTiming().getInterval().getValue()).getHours();
      if (hours != 0)
      {
        return 24 / hours;
      }
      return 1;
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
  public MedicationDto getMedication(final Long medicationId)
  {
    final Map<Long, MedicationHolderDto> value = medicationsValueHolder.getValue();
    return medicationHolderDtoMapper.mapToMedicationDto(value.get(medicationId));
  }

  @Override
  public DoseFormDto getDoseForm(final String doseFormCode, final DateTime when)
  {
    return medicationsDao.getDoseFormByCode(doseFormCode, when);
  }

  @Override
  public DoseFormDto getMedicationDoseForm(final long medicationId)
  {
    return medicationsValueHolder.getValue().get(medicationId).getDoseFormDto();
  }

  @Override
  public MedicationRouteDto getMedicationRoute(final long routeId)
  {
    return medicationRoutesValueHolder.getValue().get(routeId);
  }

  @Override
  public String getOriginalTherapyId(final String patientId, final String compositionUid)
  {
    return getOriginalTherapyId(medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, compositionUid));
  }

  @Override
  public String getOriginalTherapyId(@Nonnull final MedicationOrderComposition composition)
  {
    Preconditions.checkNotNull(composition, "composition");

    final MedicationInstructionInstruction instruction = composition.getMedicationDetail().getMedicationInstruction().get(0);
    final List<Link> originLinks = MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.ORIGIN);
    if (!originLinks.isEmpty())
    {
      final DvEhrUri target = originLinks.get(0).getTarget();
      final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(target.getValue());
      return TherapyIdUtils.createTherapyId(ehrUri.getCompositionId());
    }
    return TherapyIdUtils.createTherapyId(composition);
  }

  @Override
  public MedicationReferenceWeightComposition buildReferenceWeightComposition(
      final double weight,
      final DateTime when)
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

    final PartyIdentified composer = RequestContextHolder.getContext().getUserMetadata()
        .map(meta -> IspekTdoDataSupport.getPartyIdentified(meta.getFullName(), meta.getId()))
        .get();

    MedicationsEhrUtils.visitComposition(comp, composer, when);
    return comp;
  }

  @Override
  public void sortTherapiesByMedicationTimingStart(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies, final boolean descending)
  {
    Collections.sort(
        therapies, (therapy1, therapy2) -> {
          final DateTime firstCompositionStart =
              DataValueUtils.getDateTime(therapy1.getSecond().getOrder().get(0).getMedicationTiming().getStartDate());
          final DateTime secondCompositionStart =
              DataValueUtils.getDateTime(therapy2.getSecond().getOrder().get(0).getMedicationTiming().getStartDate());

          final DateTime firstContextStart =
              DataValueUtils.getDateTime(therapy1.getFirst().getCompositionEventContext().getStartTime());
          final DateTime secondContextStart =
              DataValueUtils.getDateTime(therapy2.getFirst().getCompositionEventContext().getStartTime());

          if (descending)
          {
            if (secondCompositionStart.equals(firstCompositionStart))
            {
              return secondContextStart.compareTo(firstContextStart);
            }
            return secondCompositionStart.compareTo(firstCompositionStart);
          }
          if (firstCompositionStart.equals(secondCompositionStart))
          {
            return firstContextStart.compareTo(secondContextStart);
          }
          return firstCompositionStart.compareTo(secondCompositionStart);
        }
    );
  }

  @Override
  public void deleteAdministration(final String patientId, final String compositionId, final String comment)
  {
    medicationsOpenEhrDao.deleteTherapyAdministration(patientId, compositionId, comment);
  }

  @Override
  public DocumentationTherapiesDto findTherapyGroupsForDocumentation(
      final String patientId,
      final String centralCaseId,
      final Interval centralCaseEffective,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs,
      final boolean isOutpatient,
      final DateTime when,
      final Locale locale)
  {
    sortTherapiesByMedicationTimingStart(instructionPairs, false);

    final Map<Long, MedicationDataForTherapyDto> medicationsDataMap = getMedicationDataForTherapies(instructionPairs, null);

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
      final String patientId,
      final String centralCaseId,
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
    final List<TherapyDto> taggedTherapiesForPrescription = new ArrayList<>();

    if (!isOutpatient)
    {
      //use taggedTherapiesForPrescription to display medication on discharge list for EMRAM
      final List<MedicationOnDischargeComposition> dischargeCompositions =
          medicationsOpenEhrDao.findMedicationOnDischargeCompositions(patientId, centralCaseEffective);

      for (final MedicationOnDischargeComposition dischargeComposition : dischargeCompositions)
      {
        final TherapyDto convertedTherapy = convertInstructionToTherapyDtoWithDisplayValues(
            dischargeComposition,
            dischargeComposition.getMedicationDetail().getMedicationInstruction().get(0),
            null,
            null,
            when,
            true,
            locale);

        taggedTherapiesForPrescription.add(convertedTherapy);
      }
    }

    final Set<TherapyDocumentationData> alreadyHandled = new HashSet<>();

    final boolean isOutpatientOrLastsOneDay = isOutpatient || Intervals.durationInDays(centralCaseEffective) <= 1;

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair : instructionPairs)
    {
      final DateTime therapyStart =
          DataValueUtils.getDateTime(instructionPair.getSecond().getOrder().get(0).getMedicationTiming().getStartDate());
      final DateTime therapyEnd =
          DataValueUtils.getDateTime(instructionPair.getSecond().getOrder().get(0).getMedicationTiming().getStopDate());

      final TherapyDto convertedTherapy = convertInstructionToTherapyDtoWithDisplayValues(
          instructionPair.getFirst(),
          instructionPair.getSecond(),
          null,
          null,
          when,
          true,
          locale);

      if (!areAllIngredientsSolutions(convertedTherapy))
      {
        final Interval therapyInterval =
            new Interval(therapyStart, therapyEnd != null ? therapyEnd : Intervals.INFINITE.getEnd());

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
        getTherapyDisplayValues(taggedTherapiesForPrescription, locale));
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
      therapyData.addInterval(
          TherapyIdUtils.createTherapyId(therapy.getCompositionUid(), therapy.getEhrOrderName()), therapyInterval);
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
      final Interval interval =
          linkedToTherapy.findIntervalForId(
              TherapyIdUtils.createTherapyId(
                  linkedToTherapy.getTherapy().getCompositionUid(), linkedToTherapy.getTherapy().getEhrOrderName()));

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
        final TherapyDocumentationData dischargeTherapy =
            createTherapyData(
                therapyToCompare, convertedTherapy, new Interval(interval.getStart(), therapyInterval.getEnd()));
        dischargeTherapies.add(dischargeTherapy);
      }
      else
      {
        final Interval newInterval = new Interval(interval.getStart(), therapyInterval.getEnd());
        linkedToTherapy.addInterval(
            TherapyIdUtils.createTherapyId(
                convertedTherapy.getCompositionUid(),
                convertedTherapy.getEhrOrderName()), newInterval);
        linkedToTherapy.removeInterval(
            TherapyIdUtils.createTherapyId(
                linkedToTherapy.getTherapy().getCompositionUid(),
                linkedToTherapy.getTherapy().getEhrOrderName()), interval);
        linkedToTherapy.setTherapy(convertedTherapy);
      }
      return true;
    }
    if (pair.getFirst() == TherapyLinkType.LINKED_TO_ADMISSION_THERAPY && alreadyHandled.contains(pair.getSecond()))
    {
      if (isDischargeTherapy(therapyInterval, centralCaseEnd, when))
      {
        final TherapyDocumentationData newDischargeTherapy =
            createTherapyData(therapyToCompare, convertedTherapy, therapyInterval);
        dischargeTherapies.add(newDischargeTherapy);
      }
      else
      {
        final TherapyDocumentationData linkedToTherapy = pair.getSecond();
        final Interval interval =
            linkedToTherapy.findIntervalForId(
                TherapyIdUtils.createTherapyId(
                    linkedToTherapy.getTherapy().getCompositionUid(),
                    linkedToTherapy.getTherapy().getEhrOrderName()));
        final Interval newInterval = new Interval(interval.getStart(), therapyInterval.getEnd());
        linkedToTherapy.addInterval(
            TherapyIdUtils.createTherapyId(
                convertedTherapy.getCompositionUid(),
                convertedTherapy.getEhrOrderName()), newInterval);
        linkedToTherapy.removeInterval(
            TherapyIdUtils.createTherapyId(
                linkedToTherapy.getTherapy().getCompositionUid(),
                linkedToTherapy.getTherapy().getEhrOrderName()), interval);
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
        similarTherapy.addInterval(
            TherapyIdUtils.createTherapyId(convertedTherapy.getCompositionUid(), convertedTherapy.getEhrOrderName()),
            therapyInterval);
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
        final TherapyDocumentationData dischargeTherapy =
            createTherapyData(therapyToCompare, convertedTherapy, therapyInterval);

        for (final Pair<String, Interval> pair1 : similarTherapy.getIntervals())
        {
          dischargeTherapy.addInterval(pair1.getFirst(), pair1.getSecond());
        }
        dischargeTherapies.add(dischargeTherapy);
      }
      else
      {
        similarTherapy.addInterval(
            TherapyIdUtils.createTherapyId(
                convertedTherapy.getCompositionUid(),
                convertedTherapy.getEhrOrderName()), therapyInterval);
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
      String formatted = therapy.getTherapy().getFormattedTherapyDisplay();
      for (final Pair<String, Interval> pair : therapy.getIntervals())
      {
        final Interval interval = pair.getSecond();
        final String endDate =
            Intervals.isEndInfinity(interval.getEnd()) ?
            "..." :
            DateTimeFormatters.shortDateTime(locale).print(interval.getEnd());
        formatted =
            formatted + " "
                + DateTimeFormatters.shortDateTime(locale).print(interval.getStart()) + " &ndash; "
                + endDate + "<br>";
      }
      strings.add(formatted);
    }
    return strings;
  }

  private List<String> getTherapyDisplayValues(final List<TherapyDto> therapyList, final Locale locale)
  {
    final List<String> therapyDisplayValues = new ArrayList<>();
    for (final TherapyDto therapy : therapyList)
    {
      therapyDisplayProvider.fillDisplayValues(therapy, true, false, locale);
      therapyDisplayValues.add(therapy.getFormattedTherapyDisplay());
    }
    return therapyDisplayValues;
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public DateTime findPreviousTaskForTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final DateTime when)
  {
    Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair = medicationsOpenEhrDao.getTherapyInstructionPair(
        patientId,
        compositionUid,
        ehrOrderName);

    while (instructionPair != null)
    {
      final List<AdministrationDto> administrations = administrationProvider.getTherapiesAdministrations(
          patientId,
          Collections.singletonList(instructionPair),
          null);

      final String therapyId = TherapyIdUtils.createTherapyId(instructionPair.getFirst(), instructionPair.getSecond());

      final DateTime lastTask = medicationsTasksProvider.findLastAdministrationTaskTimeForTherapy(
          patientId,
          therapyId,
          new Interval(when.minusDays(10), when),
          false)
          .orElse(null);

      final DateTime lastAdministration = administrations
          .stream()
          .filter(a -> a.getAdministrationResult() != AdministrationResultEnum.NOT_GIVEN)
          .map(AdministrationDto::getAdministrationTime)
          .max(Comparator.naturalOrder())
          .orElse(null);

      final DateTime lastTime = getMostRecent(lastTask, lastAdministration);
      if (lastTime != null)
      {
        return lastTime;
      }

      instructionPair = getInstructionFromLink(patientId, instructionPair.getSecond(), EhrLinkType.UPDATE, true);
    }
    return null;
  }

  private DateTime getMostRecent(final DateTime t1, final DateTime t2)
  {
    if (t1 != null && t2 != null && t2.isAfter(t1))
    {
      return t2;
    }
    return t1 != null ? t1 : t2;
  }
}
