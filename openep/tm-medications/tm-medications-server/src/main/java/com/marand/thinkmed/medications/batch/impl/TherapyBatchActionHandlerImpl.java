package com.marand.thinkmed.medications.batch.impl;

import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.service.ServiceMethod;
import com.marand.maf.core.service.auditing.Auditing;
import com.marand.maf.core.service.auditing.Level;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.medications.TherapyBatchActionEnum;
import com.marand.thinkmed.medications.batch.TherapyBatchActionHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.therapy.TherapyCacheInvalidator;
import com.marand.thinkmed.medications.therapy.TherapyUpdater;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mitja Lapajne
 */
public class TherapyBatchActionHandlerImpl implements TherapyBatchActionHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsBo medicationsBo;
  private TherapyCacheInvalidator therapyCacheInvalidator;
  private TherapyUpdater therapyUpdater;

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
  public void setTherapyCacheInvalidator(final TherapyCacheInvalidator therapyCacheInvalidator)
  {
    this.therapyCacheInvalidator = therapyCacheInvalidator;
  }

  @Required
  public void setTherapyUpdater(final TherapyUpdater therapyUpdater)
  {
    this.therapyUpdater = therapyUpdater;
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void abortAllTherapies(@Nonnull final String patientId, @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(when, "when is required");

    updateAllTherapiesWithAction(patientId, when, TherapyBatchActionEnum.ABORT_ALL);
    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void suspendAllTherapies(@Nonnull final String patientId, @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(when, "when is required");

    updateAllTherapiesWithAction(patientId, when, TherapyBatchActionEnum.SUSPEND_ALL);
    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void suspendAllTherapiesOnTemporaryLeave(@Nonnull final String patientId, @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(when, "when is required");

    updateAllTherapiesWithAction(patientId, when, TherapyBatchActionEnum.SUSPEND_ALL_ON_TEMPORARY_LEAVE);
    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void reissueAllTherapiesOnReturnFromTemporaryLeave(@Nonnull final String patientId, @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(when, "when is required");

    updateAllTherapiesWithAction(patientId, when, TherapyBatchActionEnum.REISSUE_ALL_ON_RETURN_FROM_TEMPORARY_LEAVE);
    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  private void updateAllTherapiesWithAction(
      final String patientId,
      final DateTime when,
      final TherapyBatchActionEnum batchAction)
  {
    final Interval searchInterval = Intervals.infiniteFrom(when);
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, searchInterval, null);

    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> pair : instructionsList)
    {
      if (!medicationsBo.isMedicationTherapyCompleted(pair.getFirst(), pair.getSecond()))
      {
        final MedicationOrderComposition composition = pair.getFirst();
        final String compositionUid = composition.getUid().getValue();
        final String instructionName = pair.getSecond().getName().getValue();
        final MedicationInstructionInstruction instruction =
            MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, instructionName);
        final boolean therapySuspended = medicationsBo.isTherapySuspended(composition, instruction);

        if (batchAction == TherapyBatchActionEnum.ABORT_ALL)
        {
          therapyUpdater.abortTherapy(patientId, compositionUid, instructionName, null, when);
        }
        else if (batchAction == TherapyBatchActionEnum.SUSPEND_ALL)
        {
          if (!therapySuspended)
          {
            therapyUpdater.suspendTherapy(patientId, compositionUid, instructionName, null, when);
          }
        }
        else if (batchAction == TherapyBatchActionEnum.SUSPEND_ALL_ON_TEMPORARY_LEAVE)
        {
          if (!therapySuspended)
          {
            final TherapyChangeReasonDto therapyChangeReasonDto = new TherapyChangeReasonDto();
            final CodedNameDto changeReasonDto =
                new CodedNameDto(
                    TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
                    TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString());
            therapyChangeReasonDto.setChangeReason(changeReasonDto);
            therapyUpdater.suspendTherapy(patientId, compositionUid, instructionName, therapyChangeReasonDto, when);
          }
        }
        else if (batchAction == TherapyBatchActionEnum.REISSUE_ALL_ON_RETURN_FROM_TEMPORARY_LEAVE)
        {
          if (therapySuspended)
          {
            final boolean therapySuspendedBecauseOfTemporaryLeave =
                isTherapySuspendedBecauseOfTemporaryLeave(composition, instruction);
            if (therapySuspendedBecauseOfTemporaryLeave)
            {
              therapyUpdater.reissueTherapy(patientId, compositionUid, instructionName, when);
            }
          }
        }
        else
        {
          throw new IllegalArgumentException("Action not supported");
        }
      }
    }
  }

  private boolean isTherapySuspendedBecauseOfTemporaryLeave(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    final TherapyChangeReasonDto therapySuspendReason = medicationsBo.getTherapySuspendReason(composition, instruction);
    if (therapySuspendReason != null)
    {
      final TherapyChangeReasonEnum therapyChangeReasonEnum =
          TherapyChangeReasonEnum.fromFullString(therapySuspendReason.getChangeReason().getCode());
      if (therapyChangeReasonEnum == TherapyChangeReasonEnum.TEMPORARY_LEAVE)
      {
        return true;
      }
    }
    return false;
  }
}
