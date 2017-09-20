package com.marand.thinkmed.medications.administration.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.ispek.common.Dictionary;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.exception.UserWarning;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.UserMetadata;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkehr.tagging.dto.TagDto;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.MedicationJobPerformerEnum;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TherapyTagEnum;
import com.marand.thinkmed.medications.TherapyTaggingUtils;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationSaver;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.AdministrationTaskCreator;
import com.marand.thinkmed.medications.administration.AdministrationToEhrConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.OxygenAdministration;
import com.marand.thinkmed.medications.dto.administration.PlannedDoseAdministration;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.overview.OxygenTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.RateTherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskHandler;
import com.marand.thinkmed.medications.pharmacist.PharmacySupplyProcessHandler;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.TasksRescheduler;
import com.marand.thinkmed.medications.therapy.TherapyCacheInvalidator;
import com.marand.thinkmed.medications.therapy.TherapyUpdater;
import com.marand.thinkmed.process.TaskCompletedType;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class AdministrationHandlerImpl implements AdministrationHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsBo medicationsBo;
  private PharmacySupplyProcessHandler pharmacySupplyProcessHandler;
  private PharmacistTaskHandler pharmacistTaskHandler;
  private TherapyUpdater therapyUpdater;
  private TherapyCacheInvalidator therapyCacheInvalidator;
  private MedicationsTasksHandler medicationsTasksHandler;
  private MedicationsTasksProvider medicationsTasksProvider;
  private ProcessService processService;
  private AdministrationToEhrConverter administrationToEhrConverter;
  private TasksRescheduler tasksRescheduler;
  private AdministrationTaskCreator administrationTaskCreator;
  private AdministrationUtils administrationUtils;
  private AdministrationSaver administrationSaver;
  private AdministrationTaskConverter administrationTaskConverter;
  private TaggingOpenEhrDao taggingOpenEhrDao;

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setTherapyUpdater(final TherapyUpdater therapyUpdater)
  {
    this.therapyUpdater = therapyUpdater;
  }

  @Required
  public void setPharmacySupplyProcessHandler(final PharmacySupplyProcessHandler pharmacySupplyProcessHandler)
  {
    this.pharmacySupplyProcessHandler = pharmacySupplyProcessHandler;
  }

  @Required
  public void setPharmacistTaskHandler(final PharmacistTaskHandler pharmacistTaskHandler)
  {
    this.pharmacistTaskHandler = pharmacistTaskHandler;
  }

  @Required
  public void setTherapyCacheInvalidator(final TherapyCacheInvalidator therapyCacheInvalidator)
  {
    this.therapyCacheInvalidator = therapyCacheInvalidator;
  }

  @Required
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Required
  public void setAdministrationToEhrConverter(final AdministrationToEhrConverter administrationToEhrConverter)
  {
    this.administrationToEhrConverter = administrationToEhrConverter;
  }

  @Required
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Required
  public void setTasksRescheduler(final TasksRescheduler tasksRescheduler)
  {
    this.tasksRescheduler = tasksRescheduler;
  }

  @Required
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Required
  public void setAdministrationUtils(final AdministrationUtils administrationUtils)
  {
    this.administrationUtils = administrationUtils;
  }

  @Required
  public void setAdministrationSaver(final AdministrationSaver administrationSaver)
  {
    this.administrationSaver = administrationSaver;
  }

  @Required
  public void setTaggingOpenEhrDao(final TaggingOpenEhrDao taggingOpenEhrDao)
  {
    this.taggingOpenEhrDao = taggingOpenEhrDao;
  }

  @Override
  public void addAdministrationsToTimelines(
      @Nonnull final List<AdministrationDto> administrations,
      @Nonnull final Map<String, TherapyRowDto> therapyTimelineRowsMap,
      @Nonnull final Map<String, String> modifiedTherapiesMap,
      @Nonnull final Interval tasksInterval)
  {
    Preconditions.checkNotNull(administrations, "administrations must not be null");
    Preconditions.checkNotNull(therapyTimelineRowsMap, "therapyTimelineRowsMap must not be null");
    Preconditions.checkNotNull(modifiedTherapiesMap, "modifiedTherapiesMap must not be null");
    Preconditions.checkNotNull(tasksInterval, "tasksInterval must not be null");

    for (final AdministrationDto administrationDto : administrations)
    {
      final String therapyId = administrationDto.getTherapyId();
      final String latestTherapyId = Opt.of(modifiedTherapiesMap.get(therapyId)).orElse(therapyId);

      therapyTimelineRowsMap.get(latestTherapyId).getAdministrations().add(administrationDto);
    }

    therapyTimelineRowsMap.values()
        .forEach(r -> fillAdditionalAdministrationRowData(r, new DateTime(tasksInterval.getStart())));

    filterAdministrationsByTime(therapyTimelineRowsMap, tasksInterval);
  }

  @Override
  public void deleteAdministration(
      @Nonnull final String patientId,
      @Nonnull final AdministrationDto administration,
      @Nonnull final TherapyDoseTypeEnum therapyDoseType,
      @Nonnull final String therapyId,
      final String comment)
  {
    Preconditions.checkNotNull(patientId, "patientId");
    Preconditions.checkNotNull(administration, "administration");
    Preconditions.checkNotNull(therapyDoseType, "therapyDoseType");
    Preconditions.checkNotNull(therapyId, "therapyId");

    final boolean rateQuantityOrVolumeSum = therapyDoseType == TherapyDoseTypeEnum.RATE_QUANTITY ||
        therapyDoseType == TherapyDoseTypeEnum.RATE_VOLUME_SUM;

    if (administration.getTaskId() == null && rateQuantityOrVolumeSum)
    {
      handleDeleteGroupedPrnAdministration(patientId, administration, therapyId, comment);
    }
    else if (administration.getAdministrationType() == AdministrationTypeEnum.START && administration.getGroupUUId() != null)
    {
      handleDeleteGroupedNonPrnStartAdministration(patientId, administration, therapyId, comment);
    }
    else
    {
      medicationsBo.deleteAdministration(patientId, administration.getAdministrationId(), comment);
      if (administration.getTaskId() != null)
      {
        medicationsTasksHandler.undoCompleteTask(administration.getTaskId());
      }
    }

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  private void handleDeleteGroupedNonPrnStartAdministration(
      final String patientId,
      final AdministrationDto administration,
      final String therapyId,
      final String comment)
  {
    medicationsTasksHandler.undoCompleteTask(administration.getTaskId());
    medicationsBo.deleteAdministration(patientId, administration.getAdministrationId(), comment);

    final List<TaskDto> completedGroupTasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(therapyId),
        administration.getAdministrationTime(),
        null,
        administration.getGroupUUId(),
        true)
        .stream()
        .filter(TaskDto::getCompleted).collect(Collectors.toList());

    for (final TaskDto task : completedGroupTasks)
    {
      final String uid = (String)task.getVariables().get(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName());
      medicationsTasksHandler.undoCompleteTask(task.getId());
      medicationsBo.deleteAdministration(patientId, uid, comment);
    }
  }

  private void handleDeleteGroupedPrnAdministration(
      final String patientId,
      final AdministrationDto administration,
      final String therapyId,
      final String comment)
  {
    medicationsBo.deleteAdministration(patientId, administration.getAdministrationId(), comment);

    final String groupUUId = getGroupUUIdFromAdministrationComposition(administration.getAdministrationId());
    final List<TaskDto> completedGroupTasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(therapyId),
        administration.getAdministrationTime(),
        null,
        groupUUId,
        true)
        .stream()
        .filter(TaskDto::getCompleted).collect(Collectors.toList());

    for (final TaskDto task : completedGroupTasks)
    {
      final String uid = (String)task.getVariables().get(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName());
      medicationsBo.deleteAdministration(patientId, uid, comment);
      processService.setTaskDeleteReason(task.getId(), TaskCompletedType.DELETED.getBpmName());
    }

    medicationsTasksHandler.deleteAdministrationTasks(
        patientId,
        therapyId,
        groupUUId,
        Arrays.asList(AdministrationTypeEnum.values()));
  }

  private void fillAdditionalAdministrationRowData(final TherapyRowDto row, final DateTime before)
  {
    if (row instanceof RateTherapyRowDto)
    {
      fillInfusionRateData((RateTherapyRowDto)row, row.getAdministrations(), before);
    }
    if (row instanceof OxygenTherapyRowDtoDto)
    {
      fillOxygenData((OxygenTherapyRowDtoDto)row, before);
    }
  }

  private void fillInfusionRateData(
      final RateTherapyRowDto rateRow,
      final Collection<AdministrationDto> administrations,
      final DateTime before)
  {
    AdministrationDto lastAdministration = null;
    AdministrationDto lastBeforeAdministration = null;
    Double lastPositiveRate = null;

    for (final AdministrationDto administration : administrations)
    {
      if (AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administration.getAdministrationType()) &&
          administration.getAdministrationType() != AdministrationTypeEnum.BOLUS)
      {
        if (lastAdministration == null || (administration.getAdministrationTime() != null
                && administration.getAdministrationTime().isAfter(lastAdministration.getAdministrationTime())))
        {
          if (AdministrationResultEnum.ADMINISTERED.contains(administration.getAdministrationResult()))
          {
            lastAdministration = administration;
            if (administration.getAdministrationTime().isBefore(before))
            {
              lastBeforeAdministration = administration;
            }

            final Opt<Double> administrationInfusionRate = getAdministrationInfusionRate(administration)
                    .map(TherapyDoseDto::getNumerator);

            if (administrationInfusionRate.isPresent() && administrationInfusionRate.get() != 0)
            {
              lastPositiveRate = administrationInfusionRate.get();
            }
          }
        }
      }
    }

    if (lastAdministration != null && lastAdministration.getAdministrationType() != AdministrationTypeEnum.STOP)
    {
      final Opt<TherapyDoseDto> lastAdministrationRate = getAdministrationInfusionRate(lastAdministration);
      rateRow.setCurrentInfusionRate(lastAdministrationRate.map(TherapyDoseDto::getNumerator).orElse(null));
      rateRow.setRateUnit(lastAdministrationRate.map(TherapyDoseDto::getNumeratorUnit).orElse(null));
    }
    if (lastBeforeAdministration != null && lastBeforeAdministration.getAdministrationType() != AdministrationTypeEnum.STOP)
    {
      final Opt<TherapyDoseDto> administrationInfusionRate = getAdministrationInfusionRate(lastBeforeAdministration);
      rateRow.setInfusionRateAtIntervalStart(administrationInfusionRate.map(TherapyDoseDto::getNumerator).orElse(null));
      rateRow.setInfusionFormulaAtIntervalStart(administrationInfusionRate.map(TherapyDoseDto::getDenominator).orElse(null));
      rateRow.setFormulaUnit(administrationInfusionRate.map(TherapyDoseDto::getDenominatorUnit).orElse(null));
    }

    rateRow.setLastPositiveInfusionRate(lastPositiveRate);
    rateRow.setInfusionActive(
        lastAdministration != null
            && AdministrationTypeEnum.START_OR_ADJUST.contains(lastAdministration.getAdministrationType()));
  }

  private Opt<TherapyDoseDto> getAdministrationInfusionRate(final AdministrationDto administration)
  {
    if (administration instanceof StartAdministrationDto)
    {
      return Opt.of(((StartAdministrationDto)administration).getAdministeredDose());
    }
    if (administration instanceof AdjustInfusionAdministrationDto)
    {
      return Opt.of(((AdjustInfusionAdministrationDto)administration).getAdministeredDose());
    }
    return Opt.none();
  }

  private void fillOxygenData(final OxygenTherapyRowDtoDto oxygenRow, final DateTime before)
  {
    oxygenRow.setStartingDeviceAtIntervalStart(getLastOxygenStartingDevice(oxygenRow.getAdministrations(), before).get());
    oxygenRow.setCurrentStartingDevice(getLastOxygenStartingDevice(oxygenRow.getAdministrations(), null).get());
  }

  private Opt<OxygenStartingDevice> getLastOxygenStartingDevice(
      final Collection<AdministrationDto> administrations,
      final DateTime before)
  {
    final Predicate<AdministrationDto> isAdministered = a ->
        AdministrationResultEnum.ADMINISTERED.contains(a.getAdministrationResult());

    final List<AdministrationDto> beforeAdministrations = administrations
        .stream()
        .filter(a -> before == null || a.getAdministrationTime().isBefore(before))
        .collect(Collectors.toList());

    final Optional<AdministrationDto> lastDeviceChange = beforeAdministrations
        .stream()
        .filter(isAdministered)
        .filter(a -> a instanceof OxygenAdministration)
        .filter(a -> ((OxygenAdministration)a).getStartingDevice() != null)
        .max(Comparator.comparing(AdministrationDto::getAdministrationTime));

    if (lastDeviceChange.isPresent())
    {
      final Optional<DateTime> lastStop = beforeAdministrations
          .stream()
          .filter(isAdministered)
          .filter(a -> a instanceof StopAdministrationDto)
          .map(AdministrationDto::getAdministrationTime)
          .max(Comparator.naturalOrder());

      if (!lastStop.isPresent() || lastDeviceChange.get().getAdministrationTime().isBefore(lastStop.get()))
      {
        return Opt.of(((OxygenAdministration)lastDeviceChange.get()).getStartingDevice());
      }
    }

    return Opt.none();
  }

  private void filterAdministrationsByTime(final Map<String, TherapyRowDto> therapyTimelineRowsMap, final Interval interval)
  {
    for (final String timelineKey : therapyTimelineRowsMap.keySet())
    {
      final TherapyRowDto timelineRow = therapyTimelineRowsMap.get(timelineKey);
      final List<AdministrationDto> filteredAdministrations = timelineRow.getAdministrations()
          .stream()
          .filter(administration -> administration.getAdministrationTime() != null &&
              interval.contains(administration.getAdministrationTime()))
          .collect(Collectors.toList());
      timelineRow.setAdministrations(filteredAdministrations);
    }
  }

  @Override
  public void confirmTherapyAdministration(
      @Nonnull final String therapyCompositionUid,
      @Nonnull final String ehrOrderName,
      @Nonnull final String patientId,
      @Nonnull final String userId,
      @Nonnull final AdministrationDto administration,
      final boolean edit,
      final boolean requestSupply,
      final String centralCaseId,
      final String careProviderId,
      @Nonnull final Locale locale,
      @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(therapyCompositionUid, "therapyCompositionUid");
    StringUtils.checkNotBlank(ehrOrderName, "ehrOrderName");
    StringUtils.checkNotBlank(patientId, "patientId");
    Preconditions.checkNotNull(userId, "userId");
    Preconditions.checkNotNull(administration, "administration");
    Preconditions.checkNotNull(when, "when");
    Preconditions.checkNotNull(locale, "locale");

    final MedicationOrderComposition medicationOrder = medicationsOpenEhrDao.getTherapyInstructionPair(
        patientId,
        therapyCompositionUid,
        ehrOrderName).getFirst();

    final TherapyDto therapy = medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
        medicationOrder,
        medicationOrder.getMedicationDetail().getMedicationInstruction().get(0),
        null,
        null,
        when,
        false,
        locale);

    final boolean additionalAdministration = administration.isAdditionalAdministration();
    final boolean normalInfusion = therapy.isNormalInfusion();
    final boolean startAdditionalInfusionWithRate = additionalAdministration
        && normalInfusion
        && administration.getAdministrationType() == AdministrationTypeEnum.START;

    if (startAdditionalInfusionWithRate)
    {
      administration.setGroupUUId(administrationUtils.generateGroupUUId(when));
      Opt.resolve(((StartAdministrationDto)administration)::getAdministeredDose)
          .ifPresent(dose -> dose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE));
    }
    //noinspection OverlyComplexBooleanExpression
    if (administration.getAdministrationId() != null
        && administration.getTaskId() == null
        && normalInfusion
        && administration.getAdministrationType() == AdministrationTypeEnum.START)
    {
      administration.setGroupUUId(getGroupUUIdFromAdministrationComposition(administration.getAdministrationId()));
    }

    final String administrationUid = handleConfirmAdministration(
        patientId,
        userId,
        administration,
        edit,
        centralCaseId,
        careProviderId,
        locale,
        when,
        medicationOrder);

    if (startAdditionalInfusionWithRate)
    {
      final TherapyDoseDto dose = ((StartAdministrationDto)administration).getAdministeredDose();
      final List<NewTaskRequestDto> requests = administrationTaskCreator.createRequestsForAdditionalRateAdministration(
          patientId,
          therapy,
          administration.getAdministrationTime(),
          dose,
          administration.getGroupUUId(),
          false);

      processService.createTasks(requests.toArray(new NewTaskRequestDto[requests.size()]));
    }

    handleTasksOnConfirm(patientId, userId, administration, medicationOrder, therapy, requestSupply, when, locale);

    if (administration.getAdministrationType() == AdministrationTypeEnum.STOP)
    {
      startPossibleLinkedTherapy(patientId, therapyCompositionUid, ehrOrderName, administration.getAdministrationTime());
    }

    if (!additionalAdministration && !edit)
    {
      medicationsTasksHandler.associateTaskWithAdministration(administration.getTaskId(), administrationUid);
      processService.completeTasks(administration.getTaskId());
    }

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  public String getGroupUUIdFromAdministrationComposition(@Nonnull final String compUId)
  {
    Preconditions.checkNotNull(compUId, "compUId");
    final String groupUUIdPrefix = TherapyTagEnum.GROUP_UUID.getPrefix();
    return taggingOpenEhrDao.getTags(compUId)
        .stream()
        .map(TagDto::getTag)
        .filter(tag -> tag.startsWith(groupUUIdPrefix))
        .map(TherapyTaggingUtils::getGroupUUIdFromTag)
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);
  }

  private String handleConfirmAdministration(
      final String patientId,
      final String userId,
      final AdministrationDto administration,
      final boolean edit,
      final String centralCaseId,
      final String careProviderId,
      final Locale locale,
      final DateTime when,
      final MedicationOrderComposition medicationOrder)
  {
    if (AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administration.getAdministrationType()))
    {
      return confirmTherapyAdministration(
          medicationOrder,
          patientId,
          userId,
          administration,
          mapAdministrationReasonToActionEnum(administration.getAdministrationResult()),
          edit,
          centralCaseId,
          careProviderId,
          when);
    }
    if (administration.getAdministrationType() == AdministrationTypeEnum.INFUSION_SET_CHANGE)
    {
      return confirmInfusionSetChange(
          medicationOrder,
          patientId,
          (InfusionSetChangeDto)administration,
          when,
          centralCaseId,
          careProviderId,
          locale);
    }

    throw new IllegalArgumentException("Administration type: " + administration.getAdministrationType().name() + " not supported");
  }

  private MedicationActionEnum mapAdministrationReasonToActionEnum(final AdministrationResultEnum result)
  {
    if (result == AdministrationResultEnum.NOT_GIVEN)
    {
      return MedicationActionEnum.WITHHOLD;
    }
    else if (result == AdministrationResultEnum.DEFER)
    {
      return MedicationActionEnum.DEFER;
    }
    else
    {
      return MedicationActionEnum.ADMINISTER;
    }
  }

  private void startPossibleLinkedTherapy(
      final String patientId,
      final String therapyCompositionUid,
      final String ehrOrderName,
      final DateTime startTime)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> linkedTherapies =
        medicationsOpenEhrDao.getLinkedTherapies(
            patientId,
            therapyCompositionUid,
            ehrOrderName,
            EhrLinkType.FOLLOW);

    Preconditions.checkArgument(linkedTherapies.size() < 2, "not more than 1 follow therapy should exist");

    if (!linkedTherapies.isEmpty())
    {
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> linkedTherapy = linkedTherapies.get(0);

      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
          medicationsOpenEhrDao.getTherapyInstructionPair(patientId, therapyCompositionUid, ehrOrderName);

      if (!medicationsBo.isTherapySuspended(therapyInstructionPair.getFirst(), therapyInstructionPair.getSecond()))
      {
        final boolean linkedTherapyStarted = therapyUpdater.startLinkedTherapy(patientId, linkedTherapy, startTime);
        if (linkedTherapyStarted)
        {
          therapyUpdater.createTherapyTasks(
              patientId,
              linkedTherapy.getFirst(),
              AdministrationTaskCreateActionEnum.PRESCRIBE,
              null,
              startTime);
        }
      }
    }
  }

  private void handleTasksOnConfirm(
      final String patientId,
      final String userId,
      final AdministrationDto administration,
      final MedicationOrderComposition order,
      final TherapyDto therapy,
      final boolean requestSupply,
      final DateTime when,
      final Locale locale)
  {
    final String originalTherapyId = medicationsBo.getOriginalTherapyId(order);
    handleRequestSupply(patientId, originalTherapyId, requestSupply, locale);
    handleSyringeTasks(patientId, userId, originalTherapyId);
    handleGroupTasksOnAdministrationConfirm(patientId, userId, administration, order, therapy, when);
  }

  private void handleRequestSupply(
      final String patientId,
      final String originalTherapyId,
      final boolean requestSupply,
      final Locale locale)
  {
    if (requestSupply)
    {
      try
      {
        pharmacySupplyProcessHandler.handleSupplyRequest(patientId, TherapyAssigneeEnum.NURSE, originalTherapyId, null, null);
      }
      catch (final IllegalStateException ise)
      {
        //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
        throw new UserWarning(Dictionary.getEntry("nurse.resupply.request.already.exists.warning", locale));
      }
    }
  }

  private void handleSyringeTasks(final String patientId, final String userId, final String originalTherapyId)
  {
    pharmacistTaskHandler.confirmPerfusionSyringeTasksForTherapy(
        patientId,
        userId,
        TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE,
        originalTherapyId);
  }

  private void handleGroupTasksOnAdministrationConfirm(
      final String patientId,
      final String userId,
      final AdministrationDto administration,
      final MedicationOrderComposition order,
      final TherapyDto therapy,
      final DateTime when)
  {
    if (therapy.isNormalInfusion())
    {
      final AdministrationTypeEnum type = administration.getAdministrationType();
      final DateTime administrationTime = administration.getAdministrationTime();
      final boolean given = administration.getAdministrationResult() == AdministrationResultEnum.GIVEN;
      final boolean notGiven = administration.getAdministrationResult() == AdministrationResultEnum.NOT_GIVEN;
      final String groupUUId = administration.getGroupUUId();

      if (type == AdministrationTypeEnum.START)
      {
        if (given)
        {
          if (administration.isAdministeredAtDifferentTime() || administration.getAdministrationId() != null)
          {
            final DateTime previousTime = Opt.of(administration.getAdministrationId())
                .map(id -> medicationsOpenEhrDao.getAdministrationTime(patientId, id).orElse(null))
                .orElseGet(administration::getPlannedTime);

            final long diff = administrationTime.getMillis() - previousTime.getMillis();
            tasksRescheduler.rescheduleGroup(patientId, administration.getTherapyId(), groupUUId, diff);
          }

          if (administrationTime.isBefore(when))
          {
            final String therapyId = TherapyIdUtils.createTherapyId(therapy.getCompositionUid(), therapy.getEhrOrderName());
            confirmPossibleGroupStopTask(patientId, therapyId, groupUUId, userId, administrationTime, order, when);
          }
        }
        else if (notGiven)
        {
          medicationsTasksHandler.deleteAdministrationTasks(
              patientId,
              administration.getTherapyId(),
              groupUUId,
              Stream.of(AdministrationTypeEnum.STOP, AdministrationTypeEnum.ADJUST_INFUSION).collect(Collectors.toList()));
        }
      }
      else if (type == AdministrationTypeEnum.ADJUST_INFUSION && given && administrationTime.isBefore(when))
      {
        final String therapyId = TherapyIdUtils.createTherapyId(therapy.getCompositionUid(), therapy.getEhrOrderName());
        confirmPossibleGroupStopTask(patientId, therapyId, groupUUId, userId, administrationTime, order, when);
      }
    }
  }

  private void confirmPossibleGroupStopTask(
      final String patientId,
      final String therapyId,
      final String groupUUId,
      final String userId,
      final DateTime tasksDueAfter,
      final MedicationOrderComposition order,
      final DateTime when)
  {
    final Optional<AdministrationTaskDto> stopTask = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(therapyId),
        tasksDueAfter,
        when,
        groupUUId,
        false)
        .stream()
        .filter(this::isStopTask)
        .filter(Objects::nonNull)
        .findAny()
        .map(administrationTaskConverter::convertTaskToAdministrationTask);

    if (stopTask.isPresent())
    {
      final AdministrationDto administration = administrationTaskConverter.buildAdministrationFromTask(stopTask.get(), when);
      administration.setAdministrationTime(stopTask.get().getPlannedAdministrationTime());

      final String administrationUid = confirmTherapyAdministration(
          order,
          patientId,
          userId,
          administration,
          MedicationActionEnum.ADMINISTER,
          false,
          null,
          null,
          when);

      medicationsTasksHandler.associateTaskWithAdministration(administration.getTaskId(), administrationUid);
      processService.completeTasks(administration.getTaskId());
    }
  }

  private boolean isStopTask(final TaskDto task)
  {
    final String administrationTypeVar = (String)task.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName());
    final AdministrationTypeEnum type = AdministrationTypeEnum.valueOf(administrationTypeVar);
    return type == AdministrationTypeEnum.STOP;
  }

  @Override
  public String autoConfirmSelfAdministration(
      @Nonnull final AutomaticChartingType type,
      @Nonnull final MedicationOrderComposition composition,
      @Nonnull final String patientId,
      @Nonnull final AdministrationDto administration,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(type, "type");
    Preconditions.checkNotNull(administrationToEhrConverter, "composition");
    StringUtils.checkNotBlank(patientId, "patient");
    Preconditions.checkNotNull(administration, "administration");
    Preconditions.checkNotNull(when, "when");
    Preconditions.checkArgument(AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administration.getAdministrationType()));

    if (type == AutomaticChartingType.SELF_ADMINISTER)
    {
      administration.setAdministrationResult(AdministrationResultEnum.SELF_ADMINISTERED);
      administration.setSelfAdministrationType(MedicationActionAction.SelfAdministrationType.LEVEL_3);
    }

    administration.setAdministrationTime(administration.getPlannedTime());

    if (administration instanceof PlannedDoseAdministration && administration instanceof DoseAdministration)
    {
      final PlannedDoseAdministration plannedDoseAdministration = (PlannedDoseAdministration)administration;
      final DoseAdministration doseAdministration = (DoseAdministration)administration;

      doseAdministration.setAdministeredDose(plannedDoseAdministration.getPlannedDose());
    }

    final MedicationAdministrationComposition administrationComposition =
        administrationToEhrConverter.buildMedicationAdministrationComposition(
            composition,
            administration,
            MedicationActionEnum.ADMINISTER,
            MedicationJobPerformerEnum.AUTOMATIC_CHARTING_PERFORMER.getCode(),
            MedicationJobPerformerEnum.AUTOMATIC_CHARTING_PERFORMER.getId(),
            null,
            null,
            when);

    return administrationSaver.save(patientId, administrationComposition, administration);
  }

  @Override
  public String confirmTherapyAdministration(
      @Nonnull final MedicationOrderComposition composition,
      @Nonnull final String patientId,
      @Nonnull final String userId,
      @Nonnull final AdministrationDto administrationDto,
      @Nonnull final MedicationActionEnum medicationActionEnum,
      final boolean edit,
      final String centralCaseId,
      final String careProviderId,
      @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    StringUtils.checkNotBlank(userId, "userId");
    Preconditions.checkNotNull(composition, "composition");
    Preconditions.checkNotNull(administrationDto, "administrationDto");
    Preconditions.checkNotNull(medicationActionEnum, "medicationActionEnum");
    Preconditions.checkNotNull(when, "when");

    Preconditions.checkArgument(
        AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administrationDto.getAdministrationType()));

    final MedicationAdministrationComposition administrationComposition =
        administrationToEhrConverter.buildMedicationAdministrationComposition(
            composition,
            administrationDto,
            medicationActionEnum,
            RequestContextHolder.getContext().getUserMetadata().map(UserMetadata::getFullName).get(),
            RequestContextHolder.getContext().getUserId(),
            centralCaseId,
            careProviderId,
            when);

    if (!edit && administrationDto.getTaskId() != null)
    {
      handleChangeTherapySelfAdministeringType(composition, administrationDto, userId, patientId, when);
    }

    return administrationSaver.save(patientId, administrationComposition, administrationDto);
  }

  private void handleChangeTherapySelfAdministeringType(
      final MedicationOrderComposition composition,
      final AdministrationDto administrationDto,
      final String userId,
      final String patientId,
      final DateTime when)
  {
    //TODO nejc change location
    final MedicationInstructionInstruction instruction = composition.getMedicationDetail().getMedicationInstruction().get(0);
    final AdministrationResultEnum administrationResult = administrationDto.getAdministrationResult();

    final SelfAdministeringActionEnum therapySelfAdministeringActionEnum =
        instruction.getOrder().get(0).getParsableDoseDescription() != null
        ? SelfAdministeringActionEnum.valueOf(instruction.getOrder().get(0).getParsableDoseDescription().getValue())
        : null;

    if (administrationResult == AdministrationResultEnum.SELF_ADMINISTERED)
    {
      final MedicationActionAction.SelfAdministrationType selfAdministrationType =
          administrationDto.getSelfAdministrationType();

      if (selfAdministrationType == MedicationActionAction.SelfAdministrationType.LEVEL_3
          && therapySelfAdministeringActionEnum != SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED)
      {
        therapyUpdater.updateTherapySelfAdministeringStatus(
            patientId,
            composition,
            SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED,
            userId,
            when);
      }
      else if (selfAdministrationType == MedicationActionAction.SelfAdministrationType.LEVEL_2
          && therapySelfAdministeringActionEnum != SelfAdministeringActionEnum.CHARTED_BY_NURSE)
      {
        therapyUpdater.updateTherapySelfAdministeringStatus(
            patientId,
            composition,
            SelfAdministeringActionEnum.CHARTED_BY_NURSE,
            userId,
            when);
      }
    }
    else if (administrationResult == AdministrationResultEnum.GIVEN && therapySelfAdministeringActionEnum != null)
    {
      therapyUpdater.updateTherapySelfAdministeringStatus(
          patientId,
          composition,
          SelfAdministeringActionEnum.STOP_SELF_ADMINISTERING,
          userId,
          when);
    }
  }

  private String confirmInfusionSetChange(
      final MedicationOrderComposition composition,
      final String patientId,
      final InfusionSetChangeDto administration,
      final DateTime when,
      final String centralCaseId,
      final String careProviderId,
      final Locale locale)
  {
    final MedicationAdministrationComposition administrationComposition =
        administrationToEhrConverter.buildSetChangeAdministrationComposition(
            composition,
            administration,
            RequestContextHolder.getContext().getUserMetadata().map(UserMetadata::getFullName).get(),
            RequestContextHolder.getContext().getUserId(),
            centralCaseId,
            careProviderId,
            locale,
            when);

    return administrationSaver.save(
        patientId,
        administrationComposition,
        administration
    );
  }
}
