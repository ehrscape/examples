package com.marand.thinkmed.medications.admission;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionReconciliationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionStatus;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface MedicationOnAdmissionHandler
{
  List<String> saveMedicationsOnAdmission(
      String patientId,
      List<MedicationOnAdmissionDto> medicationsOnAdmission,
      @Nullable List<String> compositionIdsToDelete,
      String centralCaseId,
      @Nullable String careProviderId,
      String userId,
      NamedExternalDto prescriber,
      DateTime when,
      Locale locale);

  String modifyAdmissionComposition(String patientId, Locale locale, TherapyDto therapy);

  void deleteMedicationsOnAdmission(String patientId, List<String> compositionIds);

  List<MedicationOnAdmissionDto> getMedicationsOnAdmission(
      String patientId,
      DateTime fromDate,
      DateTime when,
      Locale locale);

  List<MedicationOnAdmissionReconciliationDto> getMedicationsOnAdmissionForReconciliation(
      String patientId,
      DateTime fromDate,
      DateTime when,
      Locale locale);

  List<String> getMedicationsOnAdmissionCompositionIds(String patientId, DateTime fromDate, Locale locale);

  void updateMedicationOnAdmissionAction(
      String patientId,
      String compositionId,
      MedicationOrderActionEnum actionEnum,
      TherapyChangeReasonDto changeReasonDto,
      DateTime when,
      Locale locale);

  MedicationOnAdmissionStatus getMedicationOnAdmissionStatusFromMedicationAction(final MedicationActionAction action);

  List<MedicationOnAdmissionGroupDto> getTherapiesOnAdmissionGroups(
      String patientId,
      DateTime currentHospitalizationStart,
      DateTime actionTime,
      Locale locale);
}
