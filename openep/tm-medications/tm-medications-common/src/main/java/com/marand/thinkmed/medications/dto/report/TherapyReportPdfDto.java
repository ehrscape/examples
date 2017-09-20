package com.marand.thinkmed.medications.dto.report;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TherapyReportPdfDto extends DataTransferObject
{
  private final byte[] data;
  private final String filename;

  public TherapyReportPdfDto(@Nonnull final byte[] data, @Nonnull final String filename)
  {
    this.data = Preconditions.checkNotNull(data, "data must not be null!");
    this.filename = Preconditions.checkNotNull(filename, "filename must not be null!");
  }

  public byte[] getData()
  {
    return data;
  }

  public String getFilename()
  {
    return filename;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("data.size", data.length).append("filename", filename);
  }
}
