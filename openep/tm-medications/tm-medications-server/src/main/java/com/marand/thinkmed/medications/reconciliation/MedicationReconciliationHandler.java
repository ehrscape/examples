package com.marand.thinkmed.medications.reconciliation;

import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import org.joda.time.DateTime;

/**
 * @author nejck
 */
public interface MedicationReconciliationHandler
{
  List<ReconciliationRowDto> getReconciliationGroups(
      String patientId,
      DateTime fromDate,
      DateTime when,
      Locale locale);
}
