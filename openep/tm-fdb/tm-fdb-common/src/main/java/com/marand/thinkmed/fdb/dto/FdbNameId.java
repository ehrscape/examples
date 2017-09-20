package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbNameId implements JsonSerializable
{
  private final String Name;
  private final Long Id;

  public FdbNameId(final String Name, final Long Id)
  {
    this.Name = Name;
    this.Id = Id;
  }

  public String getName()
  {
    return Name;
  }

  public Long getId()
  {
    return Id;
  }
}
