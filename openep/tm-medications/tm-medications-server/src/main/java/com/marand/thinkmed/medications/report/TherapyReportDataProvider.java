package com.marand.thinkmed.medications.report;

import java.util.Locale;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TherapyReportDataProvider
{
  TherapyDayReportDto getTherapyReportData(
      @Nonnull String patientId,
      @Nonnull Locale locale,
      @Nonnull DateTime when);

  TherapySurgeryReportDto getTherapySurgeryReportData(
      @Nonnull String patientId,
      Double patientHeight,
      RoundsIntervalDto roundsInterval,
      @Nonnull Locale locale,
      @Nonnull DateTime when);
}
