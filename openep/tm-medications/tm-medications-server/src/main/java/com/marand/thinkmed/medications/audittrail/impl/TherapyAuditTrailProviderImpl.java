package com.marand.thinkmed.medications.audittrail.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.audittrail.TherapyAuditTrailProvider;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryDto;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryType;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.PartyIdentified;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class TherapyAuditTrailProviderImpl implements TherapyAuditTrailProvider
{
  private TherapyChangeCalculator therapyChangeCalculator;
  private MedicationsOpenEhrDao  medicationsOpenEhrDao;
  private MedicationsBo medicationsBo;

  @Required
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

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

  @Override
  public TherapyAuditTrailDto getTherapyAuditTrail(
      @Nonnull final String patientId,
      @Nonnull final String compositionId,
      @Nonnull final String ehrOrderName,
      final Double patientHeight,
      @Nonnull final Locale locale,
      @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    StringUtils.checkNotBlank(compositionId, "compositionId");
    StringUtils.checkNotBlank(ehrOrderName, "ehrOrderName");
    Preconditions.checkNotNull(locale, "locale");
    Preconditions.checkNotNull(when, "when");

    final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, Intervals.infiniteTo(when));

    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> currentInstructionPairLastVersion =
        medicationsOpenEhrDao.getTherapyInstructionPair(patientId, compositionId, ehrOrderName);

    final MedicationOrderComposition currentCompositionFirstVersion = getFirstCompositionVersion(
        currentInstructionPairLastVersion.getFirst(),
        patientId);

    final TherapyDto currentTherapyLastVersion =
        medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
            currentInstructionPairLastVersion.getFirst(),
            currentInstructionPairLastVersion.getSecond(),
            referenceWeight,
            patientHeight,
            when,
            true,
            locale);

    final TherapyDto currentTherapyFirstVersion =
        medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
            currentCompositionFirstVersion,
            currentCompositionFirstVersion.getMedicationDetail().getMedicationInstruction().get(0),
            referenceWeight,
            patientHeight,
            when,
            true,
            locale);

    final TherapyAuditTrailDto auditTrail = new TherapyAuditTrailDto();
    auditTrail.setCurrentTherapy(currentTherapyLastVersion);

    final List<TherapyActionHistoryDto> actionHistoryList = auditTrail.getActionHistoryList();
    actionHistoryList.addAll(extractSimpleActions(currentInstructionPairLastVersion.getFirst()));
    actionHistoryList.addAll(extractModifyExistingCompActions(currentInstructionPairLastVersion.getFirst(), when, locale));

    TherapyDto therapyFirstVersion = currentTherapyFirstVersion;

    Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair = currentInstructionPairLastVersion;
    List<Link> updateLinks = MedicationsEhrUtils.getLinksOfType(instructionPair.getSecond(), EhrLinkType.UPDATE);

    while (!updateLinks.isEmpty())
    {
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> previousInstructionPairLastVersion =
          medicationsBo.getInstructionFromLink(patientId, instructionPair.getSecond(), EhrLinkType.UPDATE, true);

      final MedicationOrderComposition previousCompositionPreviousVersion = getPreviousCompositionVersion(
          previousInstructionPairLastVersion.getFirst(),
          patientId);

      final TherapyDto previousTherapyPreviousVersion =
          medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
              previousCompositionPreviousVersion,
              previousCompositionPreviousVersion.getMedicationDetail().getMedicationInstruction().get(0),
              null,
              null,
              when,
              true,
              locale);

      actionHistoryList.addAll(extractModifyExistingCompActions(previousCompositionPreviousVersion, when, locale));
      actionHistoryList.addAll(extractSimpleActions(previousCompositionPreviousVersion));

      // only latest version has complete action (modify and start new) but it has changed therapy.end !!
      // use previous version with old therapy.end to calculate changes
      actionHistoryList.add(extractModifyAction(
          previousInstructionPairLastVersion.getFirst(),
          previousTherapyPreviousVersion,
          therapyFirstVersion,
          false,
          locale));

      instructionPair = previousInstructionPairLastVersion;
      updateLinks = MedicationsEhrUtils.getLinksOfType(instructionPair.getSecond(), EhrLinkType.UPDATE);

      // use first version to calculate changes in the next iteration
      final MedicationOrderComposition previousCompositionFirstVersion = getFirstCompositionVersion(
          previousInstructionPairLastVersion.getFirst(),
          patientId);

      therapyFirstVersion = medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
          previousCompositionFirstVersion,
          previousCompositionFirstVersion.getMedicationDetail().getMedicationInstruction().get(0),
          null,
          null,
          when,
          true,
          locale);
    }

    auditTrail.setOriginalTherapy(therapyFirstVersion);

    actionHistoryList.addAll(getPharmacyReviewsToAuditTrail(patientId, therapyFirstVersion.getCreatedTimestamp()));
    actionHistoryList.add(getPrescribeActionToAuditTrail(instructionPair.getFirst()));

    actionHistoryList.removeAll(getRedundantDoctorReviewActions(actionHistoryList));
    actionHistoryList.sort(Comparator.comparing(TherapyActionHistoryDto::getActionPerformedTime));

    return auditTrail;
  }

  private MedicationOrderComposition getPreviousCompositionVersion(
      final MedicationOrderComposition composition,
      final String patientId)
  {
    final String compositionUid = composition.getUid().getValue();
    final String previousVersion = TherapyIdUtils.getCompositionUidForPreviousVersion(compositionUid);
    return medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, previousVersion);
  }

  private MedicationOrderComposition getFirstCompositionVersion(
      final MedicationOrderComposition composition,
      final String patientId)
  {
    final String firstVersion = TherapyIdUtils.getCompositionUidForFirstVersion(composition.getUid().getValue());
    return medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, firstVersion);
  }

  private TherapyActionHistoryDto extractModifyAction(
      final MedicationOrderComposition composition,
      final TherapyDto previousTherapy,
      final TherapyDto therapy,
      final boolean modifyExistingComp,
      final Locale locale)
  {
    final MedicationActionEnum actionEnum = modifyExistingComp ? MedicationActionEnum.MODIFY_EXISTING : MedicationActionEnum.COMPLETE;

    final MedicationActionAction latestModifyAction = composition.getMedicationDetail().getMedicationAction()
        .stream()
        .filter(action -> MedicationActionEnum.getActionEnum(action) == actionEnum)
        .max(Comparator.comparing(a -> DataValueUtils.getDateTime(a.getTime())))
        .orElse(null);

    final TherapyActionHistoryDto changeHistory = new TherapyActionHistoryDto();
    changeHistory.setChanges(therapyChangeCalculator.calculateTherapyChanges(
        previousTherapy,
        therapy,
        modifyExistingComp,
        locale));

    Opt.resolve(() -> ((PartyIdentified)latestModifyAction.getOtherParticipations().get(0).getPerformer()).getName())
        .ifPresent(changeHistory::setPerformer);

    if (!modifyExistingComp)
    {
      changeHistory.setActionTakesEffectTime(therapy.getStart());
    }

    changeHistory.setTherapyActionHistoryType(
        modifyExistingComp ? TherapyActionHistoryType.MODIFY_EXISTING : TherapyActionHistoryType.MODIFY);

    changeHistory.setActionPerformedTime(DataValueUtils.getDateTime(latestModifyAction.getTime()));
    changeHistory.setChangeReason(MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(latestModifyAction));

    return changeHistory;
  }

  private TherapyActionHistoryDto getPrescribeActionToAuditTrail(final MedicationOrderComposition composition)
  {
    final TherapyActionHistoryDto changeHistory = new TherapyActionHistoryDto();
    changeHistory.setPerformer(((PartyIdentified)composition.getComposer()).getName());
    changeHistory.setActionPerformedTime(
        DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()));
    changeHistory.setTherapyActionHistoryType(TherapyActionHistoryType.PRESCRIBE);
    return changeHistory;
  }

  private List<TherapyActionHistoryDto> extractModifyExistingCompActions(
      final MedicationOrderComposition currentComposition,
      final DateTime when,
      final Locale locale)
  {
    final List<TherapyActionHistoryDto> actions = new ArrayList<>();
    final boolean therapyModifiedBeforeStart = !getModifyExistingActions(currentComposition).isEmpty();
    if (therapyModifiedBeforeStart)
    {
      final List<MedicationOrderComposition> allVersions =
          medicationsOpenEhrDao.getAllMedicationOrderCompositionVersions(currentComposition.getUid().getValue());

      MedicationOrderComposition previousVersion = null;

      for (final MedicationOrderComposition compositionVersion : allVersions)
      {
        if (previousVersion != null)
        {
          final List<MedicationActionAction> previousModifyExistingActions = getModifyExistingActions(previousVersion);
          final List<MedicationActionAction> modifyExistingActions = getModifyExistingActions(compositionVersion);
          final boolean therapyModifiedInThisVersion = previousModifyExistingActions.size() != modifyExistingActions.size();
          if (therapyModifiedInThisVersion)
          {
            final TherapyDto previousTherapy =
                medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
                    previousVersion,
                    previousVersion.getMedicationDetail().getMedicationInstruction().get(0),
                    null,
                    null,
                    when,
                    true,
                    locale);

            final TherapyDto therapy =
                medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
                    compositionVersion,
                    compositionVersion.getMedicationDetail().getMedicationInstruction().get(0),
                    null,
                    null,
                    when,
                    true,
                    locale);

            actions.add(extractModifyAction(compositionVersion, previousTherapy, therapy, true, locale));
          }
        }
        previousVersion = compositionVersion;
      }
    }
    return actions;
  }

  private List<MedicationActionAction> getModifyExistingActions(final MedicationOrderComposition composition)
  {
    return composition.getMedicationDetail().getMedicationAction().stream()
        .filter(a -> MedicationActionEnum.getActionEnum(a) == MedicationActionEnum.MODIFY_EXISTING)
        .collect(Collectors.toList());
  }

  private List<TherapyActionHistoryDto> extractSimpleActions(final MedicationOrderComposition composition)
  {
    return composition.getMedicationDetail().getMedicationAction().stream()
        .map(this::buildActionHistory)
        .filter(Opt::isPresent)
        .map(Opt::get)
        .collect(Collectors.toList());
  }

  private Opt<TherapyActionHistoryType> mapToTherapyActionHistoryType(final MedicationActionEnum medicationActionEnum)
  {
    if (medicationActionEnum == MedicationActionEnum.REVIEW)
    {
      return Opt.of(TherapyActionHistoryType.DOCTOR_REVIEW);
    }
    if (medicationActionEnum == MedicationActionEnum.SUSPEND)
    {
      return Opt.of(TherapyActionHistoryType.SUSPEND);
    }
    if (medicationActionEnum == MedicationActionEnum.REISSUE)
    {
      return Opt.of(TherapyActionHistoryType.REISSUE);
    }
    if (medicationActionEnum == MedicationActionEnum.ABORT || medicationActionEnum == MedicationActionEnum.CANCEL)
    {
      return Opt.of(TherapyActionHistoryType.STOP);
    }
    return Opt.none();
  }

  private Opt<TherapyActionHistoryDto> buildActionHistory(final MedicationActionAction action)
  {
    final Opt<TherapyActionHistoryType> type = mapToTherapyActionHistoryType(MedicationActionEnum.getActionEnum(action));

    if (type.isPresent())
    {
      final TherapyActionHistoryDto actionHistory = new TherapyActionHistoryDto();

      actionHistory.setTherapyActionHistoryType(type.get());
      actionHistory.setActionPerformedTime(DataValueUtils.getDateTime(action.getTime()));
      if (!action.getOtherParticipations().isEmpty())
      {
        actionHistory.setPerformer(((PartyIdentified)action.getOtherParticipations().get(0).getPerformer()).getName());
      }
      actionHistory.setChangeReason(MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(action));
      return Opt.of(actionHistory);
    }
    return Opt.none();
  }

  private List<TherapyActionHistoryDto> getPharmacyReviewsToAuditTrail(final String patientId, final DateTime fromTime)
  {
    return medicationsOpenEhrDao.findPharmacistsReviewCompositions(patientId, fromTime)
        .stream()
        .filter(c -> DataValueUtils.getDateTime(c.getCompositionEventContext().getStartTime()).isAfter(fromTime))
        .map(this::getPharmacyReviewAction)
        .collect(Collectors.toList());
  }

  private TherapyActionHistoryDto getPharmacyReviewAction(final PharmacyReviewReportComposition composition)
  {
    final TherapyActionHistoryDto therapyActionHistory = new TherapyActionHistoryDto();
    therapyActionHistory.setTherapyActionHistoryType(TherapyActionHistoryType.PHARMACIST_REVIEW);
    therapyActionHistory.setActionPerformedTime(
        DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()));
    therapyActionHistory.setPerformer(((PartyIdentified)composition.getComposer()).getName());
    return therapyActionHistory;
  }

  private List<TherapyActionHistoryDto> getRedundantDoctorReviewActions(final Collection<TherapyActionHistoryDto> actions)
  {
    final Set<DateTime> otherActionTimes = actions.stream()
        .filter(a -> a.getTherapyActionHistoryType() != TherapyActionHistoryType.DOCTOR_REVIEW)
        .map(TherapyActionHistoryDto::getActionPerformedTime)
        .collect(Collectors.toSet());

    return actions.stream()
        .filter(a -> a.getTherapyActionHistoryType() == TherapyActionHistoryType.DOCTOR_REVIEW)
        .filter(a -> otherActionTimes.contains(a.getActionPerformedTime()))
        .collect(Collectors.toList());
  }
}
