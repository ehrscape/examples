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

package com.marand.thinkmed.medications.pharmacist.impl;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Pair;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskCreator;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import com.marand.thinkmed.medications.pharmacist.PharmacistReviewSaver;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistUtils;
import com.marand.thinkmed.medications.pharmacist.converter.PharmacistReviewFromEhrConverter;
import com.marand.thinkmed.medications.pharmacist.converter.PharmacistReviewToEhrConverter;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Link;
import org.openehr.jaxb.rm.LocatableRef;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class PharmacistReviewSaverImpl implements PharmacistReviewSaver
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private PharmacistTaskProvider pharmacistTaskProvider;
  private ProcessService processService;
  private AdministrationTaskCreator administrationTaskCreator;
  private PharmacistReviewToEhrConverter pharmacistReviewToEhrConverter;
  private PharmacistReviewFromEhrConverter pharmacistReviewFromEhrConverter;
  private MedicationsBo medicationsBo;
  private MedicationsService medicationsService;
  private MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider;
  private TherapyDisplayProvider therapyDisplayProvider;

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setPharmacistTaskProvider(final PharmacistTaskProvider pharmacistTaskProvider)
  {
    this.pharmacistTaskProvider = pharmacistTaskProvider;
  }

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Required
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Required
  public void setPharmacistReviewToEhrConverter(final PharmacistReviewToEhrConverter pharmacistReviewToEhrConverter)
  {
    this.pharmacistReviewToEhrConverter = pharmacistReviewToEhrConverter;
  }

  @Required
  public void setPharmacistReviewFromEhrConverter(final PharmacistReviewFromEhrConverter pharmacistReviewFromEhrConverter)
  {
    this.pharmacistReviewFromEhrConverter = pharmacistReviewFromEhrConverter;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setMedicationsService(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  @Required
  public void setMedicationDataProvider(final MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider)
  {
    this.medicationDataProvider = medicationDataProvider;
  }

  @Required
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Override
  public String savePharmacistReview(
      final String patientId,
      final PharmacistReviewDto pharmacistReview,
      final Boolean authorize,
      final Locale locale)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    final PharmacyReviewReportComposition composition =
        pharmacistReviewToEhrConverter.convert(patientId, pharmacistReview, when);
    final String compositionUid =
        medicationsOpenEhrDao.saveComposition(patientId, composition, pharmacistReview.getCompositionUid());

    linkActionsToInstructions(composition, compositionUid);

    if (authorize != null && authorize)
    {
      authorizePatientPharmacistReviews(
          patientId,
          Collections.singletonList(compositionUid),
          locale,
          when);
    }
    return compositionUid;
  }

  private void linkActionsToInstructions(final PharmacyReviewReportComposition composition, final String compositionUid)
  {
    final PharmacyReviewReportComposition.MiscellaneousSection miscellaneousSection = composition.getMiscellaneous();
    if (miscellaneousSection != null)
    {
      final MedicationInstructionInstruction instruction = miscellaneousSection.getMedicationInstruction().get(0);
      for (final MedicationActionAction action : miscellaneousSection.getMedicationAction())
      {
        final LocatableRef actionInstructionId = action.getInstructionDetails().getInstructionId();
        if (actionInstructionId.getPath() == null)
        {
          MedicationsEhrUtils.fillActionInstructionId(actionInstructionId, composition, instruction, compositionUid);
        }
      }
    }
  }

  @Override
  public void authorizePatientPharmacistReviews(
      final String patientId,
      final List<String> pharmacistReviewUids,
      final Locale locale,
      final DateTime when)
  {
    final List<String> taskIds =
        pharmacistTaskProvider.findTaskIds(
            null,
            TherapyAssigneeEnum.PHARMACIST.name(),
            Collections.singleton(String.valueOf(patientId)),
            Collections.singleton(TaskTypeEnum.PHARMACIST_REVIEW));
    for (final String taskId : taskIds)
    {
      processService.completeTasks(taskId);
    }

    for (final String pharmacistReviewUid : pharmacistReviewUids)
    {
      final PharmacyReviewReportComposition composition =
          medicationsOpenEhrDao.loadPharmacistsReviewComposition(patientId, pharmacistReviewUid);
      composition.setUid(OpenEhrRefUtils.getObjectVersionId(pharmacistReviewUid));
      composition.getCompositionEventContext().setStatus(DataValueUtils.getText(PharmacistReviewStatusEnum.Final.name()));
      medicationsOpenEhrDao.saveComposition(patientId, composition, composition.getUid().getValue());

      abortTherapyIfSuggestedByPharmacist(patientId, composition);
      suspendTherapyIfSuggestedByPharmacist(patientId, composition);

      final boolean referredBackToPrescriber =
          composition.getMiscellaneous() != null && composition.getMiscellaneous().getPrescriberReferral() != null;

      if (!referredBackToPrescriber)
      {
        modifyTherapyIfSuggestedByPharmacist(patientId, locale, when, composition, pharmacistReviewUid);
      }
    }
  }

  @Override
  public void reviewPharmacistReview(
      final String patientId,
      final String pharmacistReviewUid,
      final ReviewPharmacistReviewAction reviewAction,
      final List<String> deniedReviews,
      final DateTime when,
      final Locale locale)
  {
    final DateTime requestTimestamp = RequestContextHolder.getContext().getRequestTimestamp();

    final PharmacyReviewReportComposition composition =
        medicationsOpenEhrDao.loadPharmacistsReviewComposition(patientId, pharmacistReviewUid);
    pharmacistReviewToEhrConverter.setPrescriberReferralResponse(composition, reviewAction, when);
    medicationsOpenEhrDao.saveComposition(patientId, composition, composition.getUid().getValue());

    if (deniedReviews != null)
    {
      setPrescriberReferralResponseForPharmacistReviews(
          ReviewPharmacistReviewAction.DENIED, deniedReviews, patientId, when);
    }

    if (reviewAction == ReviewPharmacistReviewAction.ACCEPTED)
    {
      modifyTherapyIfSuggestedByPharmacist(
          patientId,
          locale,
          requestTimestamp,
          composition,
          pharmacistReviewUid);
    }
  }

  private void abortTherapyIfSuggestedByPharmacist(final String patientId, final PharmacyReviewReportComposition composition)
  {
    if (composition.getMiscellaneous() != null && !composition.getMiscellaneous().getMedicationInstruction().isEmpty())
    {
      final MedicationInstructionInstruction instruction =
          composition.getMiscellaneous().getMedicationInstruction().get(0);

      if (!composition.getMiscellaneous().getMedicationAction().isEmpty())
      {
        final PharmacistTherapyChangeType pharmacistTherapyChangeType =
            PharmacistUtils.getPharmacistTherapyChangeType(composition.getMiscellaneous().getMedicationAction().get(0));

        if (pharmacistTherapyChangeType != null && pharmacistTherapyChangeType == PharmacistTherapyChangeType.ABORT)
        {
          final Link instructionLink = MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.REVIEWED)
              .get(0);
          final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionToAbort =
              medicationsBo.getInstructionFromLink(patientId, instructionLink, true);
          final boolean therapyCancelledOrAborted =
              medicationsBo.isTherapyCancelledOrAborted(instructionToAbort.getFirst(), instructionToAbort.getSecond());
          if (!therapyCancelledOrAborted)
          {
            medicationsService.abortTherapy(
                patientId,
                instructionToAbort.getFirst().getUid().getValue(),
                instructionToAbort.getSecond().getName().getValue(),
                null);
          }
        }
      }
    }
  }

  private void setPrescriberReferralResponseForPharmacistReviews(
      final ReviewPharmacistReviewAction reviewAction,
      final List<String> reviewIds,
      final String patientId,
      final DateTime when)
  {
    for (final String reviewId : reviewIds)
    {
      final PharmacyReviewReportComposition composition =
          medicationsOpenEhrDao.loadPharmacistsReviewComposition(patientId, reviewId);
      pharmacistReviewToEhrConverter.setPrescriberReferralResponse(composition, reviewAction, when);
      medicationsOpenEhrDao.saveComposition(patientId, composition, composition.getUid().getValue());
    }
  }

  private void suspendTherapyIfSuggestedByPharmacist(final String patientId, final PharmacyReviewReportComposition composition)
  {
    if (composition.getMiscellaneous() != null && !composition.getMiscellaneous().getMedicationInstruction().isEmpty())
    {
      final MedicationInstructionInstruction instruction =
          composition.getMiscellaneous().getMedicationInstruction().get(0);

      if (!composition.getMiscellaneous().getMedicationAction().isEmpty())
      {
        final PharmacistTherapyChangeType pharmacistTherapyChangeType =
            PharmacistUtils.getPharmacistTherapyChangeType(composition.getMiscellaneous().getMedicationAction().get(0));

        if (pharmacistTherapyChangeType != null && pharmacistTherapyChangeType == PharmacistTherapyChangeType.SUSPEND)
        {
          final Link instructionLink = MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.REVIEWED)
              .get(0);
          final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionToSuspend =
              medicationsBo.getInstructionFromLink(patientId, instructionLink, true);

          final boolean therapyCancelledOrAborted = medicationsBo.isTherapyCancelledOrAborted(
              instructionToSuspend.getFirst(),
              instructionToSuspend.getSecond());
          final boolean therapySuspended = medicationsBo.isTherapySuspended(
              instructionToSuspend.getFirst(),
              instructionToSuspend.getSecond());

          if (!therapyCancelledOrAborted && !therapySuspended)
          {
            medicationsService.suspendTherapy(
                patientId,
                instructionToSuspend.getFirst().getUid().getValue(),
                instructionToSuspend.getSecond().getName().getValue(), null);
          }
        }
      }
    }
  }

  private void modifyTherapyIfSuggestedByPharmacist(
      final String patientId,
      final Locale locale,
      final DateTime when,
      final PharmacyReviewReportComposition composition,
      final String pharmacistReviewUid)
  {
    if (composition.getMiscellaneous() != null && !composition.getMiscellaneous().getMedicationInstruction().isEmpty())
    {
      final MedicationInstructionInstruction instruction =
          composition.getMiscellaneous().getMedicationInstruction().get(0);

      if (!composition.getMiscellaneous().getMedicationAction().isEmpty())
      {
        final PharmacistTherapyChangeType pharmacistTherapyChangeType =
            PharmacistUtils.getPharmacistTherapyChangeType(composition.getMiscellaneous().getMedicationAction().get(0));
        if (pharmacistTherapyChangeType != null && pharmacistTherapyChangeType == PharmacistTherapyChangeType.EDIT)
        {
          final Link instructionLink = MedicationsEhrUtils.getLinksOfType(instruction, EhrLinkType.REVIEWED)
              .get(0);
          final Pair<MedicationOrderComposition, MedicationInstructionInstruction> oldTherapy =
              medicationsBo.getInstructionFromLink(patientId, instructionLink, true);
          final boolean therapyCancelledOrAborted =
              medicationsBo.isTherapyCancelledOrAborted(oldTherapy.getFirst(), oldTherapy.getSecond());

          if (!therapyCancelledOrAborted)
          {
            final MedicationFromEhrConverter<?> converter = MedicationConverterSelector.getConverter(instruction);

            final TherapyDto therapyDto =
                converter.createTherapyFromInstruction(
                    instruction,
                    oldTherapy.getFirst().getUid().getValue(),
                    oldTherapy.getSecond().getName().getValue(),
                    DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
                    when,
                    medicationDataProvider);

            if (therapyDto.getStart().isBefore(when))
            {
              final DateTime nextAdministrationTime =
                  administrationTaskCreator.calculateNextTherapyAdministrationTime(
                      therapyDto,
                      false,
                      RequestContextHolder.getContext().getRequestTimestamp());
              therapyDto.setStart(nextAdministrationTime == null ? when : nextAdministrationTime);
            }

            therapyDisplayProvider.fillDisplayValues(therapyDto, true, false, locale);

            final NamedExternalDto composer = pharmacistReviewFromEhrConverter.extractComposer(composition);
            medicationsService.modifyTherapy(
                patientId,
                therapyDto,
                null,
                null,
                null,
                composer,
                null,
                when,
                pharmacistReviewUid,
                locale);
          }
        }
      }
    }
  }
}
