package com.marand.thinkmed.fdb.dto;

import com.marand.maf.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbNameValue implements JsonSerializable
{
  private final String Name;
  private final Long Value;

  public FdbNameValue(final String Name, final Long Value)
  {
    this.Name = Name;
    this.Value = Value;
  }

  public String getName()
  {
    return Name;
  }

  public Long getValue()
  {
    return Value;
  }
}
