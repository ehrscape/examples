package com.marand.thinkmed.api.externals.data.object;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class ExternalCatalogDto extends NamedExternalDto
{
  private final String code;

  public ExternalCatalogDto(final String id, final String name, final String code)
  {
    super(id, name);
    this.code = Preconditions.checkNotNull(code);
  }

  public String getCode()
  {
    return code;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("code", code);
  }
}
