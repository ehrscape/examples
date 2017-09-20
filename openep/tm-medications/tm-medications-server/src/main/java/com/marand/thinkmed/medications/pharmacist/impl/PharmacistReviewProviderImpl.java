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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.PharmacyReviewReportComposition;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewsDto;
import com.marand.thinkmed.medications.pharmacist.PharmacistReviewProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.pharmacist.converter.PharmacistReviewFromEhrConverter;
import com.marand.thinkmed.medications.task.PharmacistReminderTaskDef;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class PharmacistReviewProviderImpl implements PharmacistReviewProvider
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private PharmacistTaskProvider pharmacistTaskProvider;
  private PharmacistReviewFromEhrConverter pharmacistReviewFromEhrConverter;
  private MedicationsBo medicationsBo;

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
  public void setPharmacistReviewFromEhrConverter(final PharmacistReviewFromEhrConverter pharmacistReviewFromEhrConverter)
  {
    this.pharmacistReviewFromEhrConverter = pharmacistReviewFromEhrConverter;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Override
  public PharmacistReviewsDto getPharmacistReviews(
      final String patientId, final DateTime fromDate, final Locale locale)
  {
    final PharmacistReviewsDto pharmacistReviewsDto = new PharmacistReviewsDto();
    final List<PharmacyReviewReportComposition> pharmacistsReviews =
        medicationsOpenEhrDao.findPharmacistsReviewCompositions(patientId, fromDate);
    final List<TaskDto> pharmacistReviewAndSupplyTasks =
        pharmacistTaskProvider.findPharmacistReminderAndSupplyTasks(patientId, null);

    final List<PharmacistReviewDto> pharmacistReviewsList = new ArrayList<>();
    for (final PharmacyReviewReportComposition pharmacistsReview : pharmacistsReviews)
    {
      final PharmacistReviewDto pharmacistReviewDto =
          pharmacistReviewFromEhrConverter.convert(
              patientId, pharmacistsReview, RequestContextHolder.getContext().getRequestTimestamp(), locale);
      fillPharmacistReminderAndSupplyTasksData(patientId, pharmacistReviewDto, pharmacistReviewAndSupplyTasks);
      pharmacistReviewsList.add(pharmacistReviewDto);
    }
    pharmacistReviewsDto.setPharmacistReviews(pharmacistReviewsList);

    final DateTime lastEditTimestamp =
        pharmacistTaskProvider.getLastEditTimestampForPharmacistReview(String.valueOf(patientId));
    pharmacistReviewsDto.setLastTaskChangeTimestamp(lastEditTimestamp);
    return pharmacistReviewsDto;
  }

  @Override
  public List<PharmacistReviewDto> getPharmacistReviewsForTherapy(
      final String patientId, final String therapyCompositionUid, final Locale locale)
  {
    final List<PharmacyReviewReportComposition> pharmacistsReviews =
        medicationsOpenEhrDao.findPharmacistsReviewCompositions(
            patientId, RequestContextHolder.getContext().getRequestTimestamp().minusDays(30));
    final List<TaskDto> pharmacistReviewAndSupplyTasks =
        pharmacistTaskProvider.findPharmacistReminderAndSupplyTasks(patientId, null);

    final List<PharmacistReviewDto> pharmacistReviewDtos = new ArrayList<>();
    for (final PharmacyReviewReportComposition pharmacistsReview : pharmacistsReviews)
    {
      if (pharmacistsReview.getMiscellaneous() != null)
      {
        final PharmacistReviewStatusEnum pharmacistReviewStatus =
            PharmacistReviewStatusEnum.valueOf(pharmacistsReview.getCompositionEventContext().getStatus().getValue());

        final boolean reviewedByPharmacist = pharmacistsReview.getMiscellaneous().getPrescriberReferralResponse() != null;
        if (pharmacistReviewStatus == PharmacistReviewStatusEnum.Final && !reviewedByPharmacist)
        {
          for (final MedicationInstructionInstruction reviewedInstruction : pharmacistsReview.getMiscellaneous()
              .getMedicationInstruction())
          {
            if (!reviewedInstruction.getLinks().isEmpty())
            {
              final List<Link> linksOfType = MedicationsEhrUtils.getLinksOfType(reviewedInstruction, EhrLinkType.REVIEWED);
              if (!linksOfType.isEmpty() && TherapyIdUtils.getCompositionUidWithoutVersion(therapyCompositionUid).equals(
                  TherapyIdUtils.getCompositionUidWithoutVersion(
                      OpenEhrRefUtils.parseEhrUri(linksOfType.get(0).getTarget().getValue()).getCompositionId())))
              {
                final PharmacistReviewDto pharmacistReviewDto =
                    pharmacistReviewFromEhrConverter.convert(
                        patientId, pharmacistsReview, RequestContextHolder.getContext().getRequestTimestamp(), locale);
                fillPharmacistReminderAndSupplyTasksData(patientId, pharmacistReviewDto, pharmacistReviewAndSupplyTasks);
                if (pharmacistReviewDto.isReferBackToPrescriber())
                {
                  pharmacistReviewDtos.add(pharmacistReviewDto);
                }
              }
            }
          }
        }
      }
    }
    if (!pharmacistReviewDtos.isEmpty())
    {
      pharmacistReviewDtos.get(0).setMostRecentReview(true);
    }
    return pharmacistReviewDtos;
  }

  private void fillPharmacistReminderAndSupplyTasksData(
      final String patientId,
      final PharmacistReviewDto pharmacistReviewDto,
      final List<TaskDto> tasks)
  {
    if (!pharmacistReviewDto.getRelatedTherapies().isEmpty())
    {
      final TherapyDto therapyDto = pharmacistReviewDto.getRelatedTherapies().get(0).getTherapy();

      final String originalTherapyId = medicationsBo.getOriginalTherapyId(patientId, therapyDto.getCompositionUid());

      for (final TaskDto pharmacistTask : tasks)
      {
        if (pharmacistTask.getTaskExecutionStrategyId().equals(TaskTypeEnum.PHARMACIST_REMINDER.getName()))
        {
          final String compositionUid =
              (String)pharmacistTask.getVariables().get(PharmacistReminderTaskDef.PHARMACIST_REVIEW_ID.getName());
          if (compositionUid != null &&
              TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid).equals(
                  TherapyIdUtils.getCompositionUidWithoutVersion(pharmacistReviewDto.getCompositionUid())))
          {
            pharmacistReviewDto.setReminderDate(pharmacistTask.getDueTime());
            pharmacistReviewDto.setReminderNote(
                (String)pharmacistTask.getVariables().get(PharmacistReminderTaskDef.COMMENT.getName()));
          }
        }
        else if (pharmacistTask.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REMINDER.getName()))
        {
          final String compositionUid =
              (String)pharmacistTask.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
          if (compositionUid != null && compositionUid.equals(originalTherapyId))
          {
            final String supplyType = (String)pharmacistTask.getVariables().get(
                SupplyReminderTaskDef.SUPPLY_TYPE.getName());
            pharmacistReviewDto.setMedicationSupplyTypeEnum(
                supplyType != null ? MedicationSupplyTypeEnum.valueOf(supplyType) : null);
            pharmacistReviewDto.setDaysSupply(
                (Integer)pharmacistTask.getVariables().get(SupplyReminderTaskDef.DAYS_SUPPLY.getName()));
          }
        }
      }
    }
  }
}
