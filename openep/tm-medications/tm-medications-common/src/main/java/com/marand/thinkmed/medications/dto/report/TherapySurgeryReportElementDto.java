package com.marand.thinkmed.medications.dto.report;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class TherapySurgeryReportElementDto extends DataTransferObject
{
  private final String htmlDescription;
  private Integer consecutiveDays;

  public TherapySurgeryReportElementDto(final String htmlDescription)
  {
    this.htmlDescription = htmlDescription;
  }

  public TherapySurgeryReportElementDto(final String htmlDescription, final Integer consecutiveDays)
  {
    this.htmlDescription = htmlDescription;
    this.consecutiveDays = consecutiveDays;
  }

  public String getHtmlDescription()
  {
    return htmlDescription;
  }

  public Integer getConsecutiveDays()
  {
    return consecutiveDays;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("htmlDescription", htmlDescription).append("consecutiveDays", consecutiveDays);
  }
}