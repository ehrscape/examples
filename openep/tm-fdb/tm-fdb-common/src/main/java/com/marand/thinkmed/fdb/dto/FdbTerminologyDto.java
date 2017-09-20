package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbTerminologyDto implements JsonSerializable
{
  private String Id;
  private String Name;
  private FdbNameValue Terminology;

  public String getId()
  {
    return Id;
  }

  public void setId(final String id)
  {
    Id = id;
  }

  public String getName()
  {
    return Name;
  }

  public void setName(final String name)
  {
    Name = name;
  }

  public FdbNameValue getTerminology()
  {
    return Terminology;
  }

  public void setTerminology(final FdbNameValue terminology)
  {
    Terminology = terminology;
  }
}
