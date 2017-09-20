package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
public class CodedNameDto extends DataTransferObject implements JsonSerializable
{
  private String code;
  private String name;

  public CodedNameDto(final String code, final String name)
  {
    this.code = code;
    this.name = name;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(final String code)
  {
    this.code = code;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append(code, "code")
        .append(name, "name")
    ;
  }
}