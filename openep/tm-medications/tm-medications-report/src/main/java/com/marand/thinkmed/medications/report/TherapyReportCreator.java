package com.marand.thinkmed.medications.report;

import java.util.Locale;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyReportPdfDto;

/**
 * @author DusanM
 * @author Mitja Lapajne
 */
public interface TherapyReportCreator
{
  TherapyReportPdfDto createPdfReport(
      @Nonnull String patientId,
      @Nonnull String userUsername,
      TherapyDayReportDto reportDataJson,
      @Nonnull Locale locale);
}
