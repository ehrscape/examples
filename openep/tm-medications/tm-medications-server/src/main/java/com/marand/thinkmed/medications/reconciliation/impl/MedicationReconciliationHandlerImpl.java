package com.marand.thinkmed.medications.reconciliation.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.discharge.MedicationOnDischargeHandler;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionReconciliationDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeReconciliationDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeStatus;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowGroupEnum;
import com.marand.thinkmed.medications.reconciliation.MedicationReconciliationHandler;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class MedicationReconciliationHandlerImpl implements MedicationReconciliationHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler;
  private MedicationOnDischargeHandler medicationOnDischargeHandler;
  private TherapyChangeCalculator therapyChangeCalculator;

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationOnDischargeHandler(final MedicationOnDischargeHandler medicationOnDischargeHandler)
  {
    this.medicationOnDischargeHandler = medicationOnDischargeHandler;
  }

  @Required
  public void setMedicationOnAdmissionHandler(final MedicationOnAdmissionHandler medicationOnAdmissionHandler)
  {
    this.medicationOnAdmissionHandler = medicationOnAdmissionHandler;
  }

  @Required
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Override
  public List<ReconciliationRowDto> getReconciliationGroups(
      final String patientId,
      final DateTime fromDate,
      final DateTime when,
      final Locale locale)
  {
    final List<ReconciliationRowDto> resultRows = new ArrayList<>();

    final List<MedicationOnAdmissionReconciliationDto> onAdmissionMedications =
        medicationOnAdmissionHandler.getMedicationsOnAdmissionForReconciliation(
            patientId,
            fromDate,
            when,
            locale);

    final List<MedicationOnDischargeReconciliationDto> onDischargeMedications =
        medicationOnDischargeHandler.getMedicationsOnDischargeForReconciliation(
            patientId,
            fromDate,
            when,
            locale);

    final Map<String, MedicationOnAdmissionReconciliationDto> onAdmissionTherapiesMap = new LinkedHashMap<>();
    for (final MedicationOnAdmissionReconciliationDto admissionDto : onAdmissionMedications)
    {
      onAdmissionTherapiesMap.put(
          TherapyIdUtils.getCompositionUidWithoutVersion(admissionDto.getTherapy().getCompositionUid()),
          admissionDto);
    }

    final Map<String, Pair<DateTime, TherapyChangeReasonDto>> reasonsForEditedAdmissionCompositions = medicationsOpenEhrDao
        .getLastEditChangeReasonsForCompositionsFromAdmission(patientId);

    for (final MedicationOnDischargeReconciliationDto onDischargeReconciliationDto : onDischargeMedications)
    {
      final String admissionCompositionUid =
          onDischargeReconciliationDto.getLinkedAdmissionCompositionId() != null
          ? TherapyIdUtils.getCompositionUidWithoutVersion(onDischargeReconciliationDto.getLinkedAdmissionCompositionId())
          : null;

      if (admissionCompositionUid != null && onAdmissionTherapiesMap.containsKey(admissionCompositionUid))
      {
        final ReconciliationRowDto rowDto = new ReconciliationRowDto();

        final MedicationOnAdmissionReconciliationDto onAdmissionReconciliationDto = onAdmissionTherapiesMap.get(
            admissionCompositionUid);
        rowDto.setTherapyOnAdmission(onAdmissionReconciliationDto.getTherapy());

        if (onDischargeReconciliationDto.getStatus() == MedicationOnDischargeStatus.NOT_PRESCRIBED)
        {
          rowDto.setChangeReasonDto(onDischargeReconciliationDto.getChangeReasonDto());
          rowDto.setGroupEnum(ReconciliationRowGroupEnum.ONLY_ON_ADMISSION);
          rowDto.setStatusEnum(TherapyStatusEnum.ABORTED);
        }
        else
        {
          rowDto.setTherapyOnDischarge(onDischargeReconciliationDto.getTherapy());

          final Pair<DateTime, TherapyChangeReasonDto> matchedChangeReasonDtoPair =
              reasonsForEditedAdmissionCompositions.get(admissionCompositionUid);

          setTherapyChangesAndReasonAndRowGroupEnum(
              onAdmissionReconciliationDto,
              onDischargeReconciliationDto,
              matchedChangeReasonDtoPair != null ? matchedChangeReasonDtoPair.getSecond() : null,
              rowDto,
              locale);
        }

        resultRows.add(rowDto);
        onAdmissionTherapiesMap.remove(admissionCompositionUid);
      }
      else
      {
        final ReconciliationRowDto rowDto = new ReconciliationRowDto();
        rowDto.setTherapyOnDischarge(onDischargeReconciliationDto.getTherapy());
        rowDto.setGroupEnum(ReconciliationRowGroupEnum.ONLY_ON_DISCHARGE);
        resultRows.add(rowDto);
      }
    }

    if (!onAdmissionTherapiesMap.isEmpty())
    {
      final Map<String, Pair<TherapyStatusEnum, TherapyChangeReasonDto>> reasonsForAbortedAdmissionCompositions =
          medicationsOpenEhrDao.getLastChangeReasonsForCompositionsFromAdmission(patientId, true);

      for (final Map.Entry<String, MedicationOnAdmissionReconciliationDto> mapEntry : onAdmissionTherapiesMap.entrySet())
      {
        final ReconciliationRowDto rowDto = new ReconciliationRowDto();
        final MedicationOnAdmissionReconciliationDto admissionReconciliationDto = mapEntry.getValue();

        rowDto.setTherapyOnAdmission(admissionReconciliationDto.getTherapy());
        rowDto.setGroupEnum(ReconciliationRowGroupEnum.ONLY_ON_ADMISSION);

        final TherapyStatusEnum admissionReconciliationStatusEnum = admissionReconciliationDto.getStatusEnum();
        final TherapyChangeReasonDto changeReasonOnAdmission = admissionReconciliationDto.getChangeReasonDto();
        final boolean isAdmissionAbortedOrCanceled = admissionReconciliationStatusEnum == TherapyStatusEnum.CANCELLED ||
            admissionReconciliationStatusEnum == TherapyStatusEnum.ABORTED;

        if (changeReasonOnAdmission != null && isAdmissionAbortedOrCanceled)
        {
          rowDto.setChangeReasonDto(changeReasonOnAdmission);
          rowDto.setStatusEnum(admissionReconciliationStatusEnum);
        }
        else
        {
          final Pair<TherapyStatusEnum, TherapyChangeReasonDto> admissionReasonMapEntry =
              reasonsForAbortedAdmissionCompositions.get(mapEntry.getKey());

          if (admissionReasonMapEntry != null)
          {
            rowDto.setChangeReasonDto(admissionReasonMapEntry.getSecond());
            rowDto.setStatusEnum(admissionReasonMapEntry.getFirst());
          }
          else
          {
            rowDto.setChangeReasonDto(changeReasonOnAdmission);
            rowDto.setStatusEnum(admissionReconciliationStatusEnum);
          }
        }
        if (rowDto.getStatusEnum() == null)
        {
          rowDto.setStatusEnum(TherapyStatusEnum.ABORTED);
        }
        resultRows.add(rowDto);
      }
    }
    return resultRows;
  }

  private void setTherapyChangesAndReasonAndRowGroupEnum(
      final MedicationOnAdmissionReconciliationDto onAdmissionReconciliationDto,
      final MedicationOnDischargeReconciliationDto onDischargeReconciliationDto,
      final TherapyChangeReasonDto inpatientChangeReason,
      final ReconciliationRowDto rowDto,
      final Locale locale)
  {
    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(
            onAdmissionReconciliationDto.getTherapy(),
            onDischargeReconciliationDto.getTherapy(),
            false,
            locale);

    rowDto.setChanges(changes);

    if (changes.isEmpty())
    {
      rowDto.setGroupEnum(ReconciliationRowGroupEnum.NOT_CHANGED);
    }
    else
    {
      rowDto.setGroupEnum(ReconciliationRowGroupEnum.CHANGED);

      if (onDischargeReconciliationDto.getChangeReasonDto() != null)
      {
        rowDto.setChangeReasonDto(onDischargeReconciliationDto.getChangeReasonDto());
      }
      else if (inpatientChangeReason != null)
      {
        rowDto.setChangeReasonDto(inpatientChangeReason);
      }
      else
      {
        rowDto.setChangeReasonDto(onAdmissionReconciliationDto.getChangeReasonDto());
      }
    }
  }
}
