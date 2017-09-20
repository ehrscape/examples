package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Bostjan Vester
 */
public interface AdtDataProvider
{
  Interval getLastDischargedCentralCaseEffectiveInterval(String patientId);

  MedicationsCentralCaseDto getCentralCaseForMedicationsDto(String patientId);

  PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      String patientId,
      boolean mainDiseaseTypeOnly,
      DateTime when,
      Locale locale);

  List<ExternalCatalogDto> getPatientDiseases(String patientId);
}
