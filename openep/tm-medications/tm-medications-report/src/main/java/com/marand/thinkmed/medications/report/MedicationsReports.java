package com.marand.thinkmed.medications.report;

import com.marand.ispek.print.common.PrintUtils;
import com.marand.ispek.print.jasperreports.JasperReportId;

/**
 * @author Primoz Prislan
 */
public interface MedicationsReports
{
  JasperReportId THERAPY_DAY_SCOTLAND = new JasperReportId(
      "THERAPY_DAY_SCOTLAND",
      PrintUtils.REPORTS_PATH + "TherapyDayScotland",
      null,
      "report.TherapyDay.description");
}
