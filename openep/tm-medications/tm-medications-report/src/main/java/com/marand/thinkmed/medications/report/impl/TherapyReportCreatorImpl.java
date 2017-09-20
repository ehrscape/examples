package com.marand.thinkmed.medications.report.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;

import com.marand.ispek.common.Dictionary;
import com.marand.ispek.print.common.ReportAction;
import com.marand.ispek.print.jasperreports.JasperReportId;
import com.marand.ispek.print.jasperreports.JasperReportPrintParameters;
import com.marand.ispek.print.jasperreports.JasperReportsUtils;
import com.marand.maf.core.time.CurrentTime;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayElementReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportUtils;
import com.marand.thinkmed.medications.dto.report.TherapyReportPdfDto;
import com.marand.thinkmed.medications.report.MedicationsReports;
import com.marand.thinkmed.medications.report.TherapyReportCreator;
import com.marand.thinkmed.medications.service.MedicationsService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * @author DusanM
 * @author Mitja Lapajne
 */
public class TherapyReportCreatorImpl implements TherapyReportCreator, InitializingBean
{
  private MedicationsService medicationsService;

  @Required
  public void setMedicationsService(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    TherapyDayReportUtils.init(Dictionary.getDelegate());
  }

  @Override
  public TherapyReportPdfDto createPdfReport(
      @Nonnull final String patientId,
      @Nonnull final String username,
      final TherapyDayReportDto reportData,
      @Nonnull final Locale locale)
  {
    final JasperReportId jasperReportId = MedicationsReports.THERAPY_DAY_SCOTLAND;
    final DateTime actionTimestamp = CurrentTime.get();

    if (reportData != null && reportData.getPatientData() != null)
    {
      if (!reportData.getComplexElements().isEmpty() || !reportData.getSimpleElements().isEmpty())
      {
        final JasperReportPrintParameters parameters = getJasperReportPrintParameters(
            jasperReportId,
            username,
            actionTimestamp.minusDays(3).withTimeAtStartOfDay().toDate(),
            reportData);

        final String pdfFilename = getPdfFilename(reportData);
        final byte[] pdfData = JasperReportsUtils.createPdfByteArray(parameters);
        return new TherapyReportPdfDto(pdfData, pdfFilename);
      }
    }
    return null;
  }

  private JasperReportPrintParameters getJasperReportPrintParameters(
      @Nonnull final JasperReportId jasperReportId,
      @Nonnull final String requestingUserName,
      @Nonnull final Date therapyApplicationStartDate,
      @Nonnull final TherapyDayReportDto reportData)
  {
    final JasperReportPrintParameters parameters =
        new JasperReportPrintParameters(
            jasperReportId,
            Collections.singleton(reportData),
            ReportAction.PDF,
            requestingUserName,
            false);

    parameters.addReportParameter("therapyApplicationStartDate", therapyApplicationStartDate);
    parameters.addReportParameter("showLegend", false);

    if (!reportData.isForEmptyReport())
    {
      parameters.addReportParameter("showSimpleGroups", shouldShowGroups(reportData.getSimpleElements()));
      parameters.addReportParameter("showComplexGroups", shouldShowGroups(reportData.getComplexElements()));
    }
    return parameters;
  }

  private String getPdfFilename(@Nonnull final TherapyDayReportDto reportData)
  {
    final PatientDataForTherapyReportDto patientData = reportData.getPatientData();
    final StringBuilder pdfName = new StringBuilder();

    return pdfName
        .append(patientData.getPatientIdentificatorType())
        .append(patientData.getPatientIdentificator())
        .append(".pdf")
        .toString();
  }

  private boolean shouldShowGroups(final List<TherapyDayElementReportDto> elements)
  {
    return elements != null && elements.stream().anyMatch(e -> StringUtils.isNotBlank(e.getCustomGroupName()));
  }
}
