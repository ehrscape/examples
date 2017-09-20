package com.marand.thinkmed.medications.discharge;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeReconciliationDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface MedicationOnDischargeHandler
{
  List<String> saveMedicationsOnDischarge(
      String patientId,
      List<MedicationOnDischargeDto> therapiesList,
      List<String> compositionIdsToDelete,
      String centralCaseId,
      @Nullable String careProviderId,
      String userId,
      NamedExternalDto prescriber,
      DateTime when,
      Locale locale);

  List<MedicationOnDischargeDto> getMedicationsOnDischarge(
      String patientId,
      Interval searchInterval,
      DateTime when,
      Locale locale);

    List<MedicationOnDischargeReconciliationDto> getMedicationsOnDischargeForReconciliation(
      String patientId,
      DateTime hospitalizationStart,
      DateTime when,
      Locale locale);

  List<String> getMedicationsOnDischargeIds(
      String patientId,
      DateTime fromDate);

  void deleteMedicationsOnDischarge(
      String patientId,
      List<String> compositionIds);
}

