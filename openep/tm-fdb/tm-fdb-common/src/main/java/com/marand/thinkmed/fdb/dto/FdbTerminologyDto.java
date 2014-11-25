package com.marand.thinkmed.fdb.dto;

import com.marand.maf.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbTerminologyDto implements JsonSerializable
{
  private String Id;
  private String Name;
  private FdbNameValue Terminology;
  private FdbNameValue ConceptType;

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

  public FdbNameValue getConceptType()
  {
    return ConceptType;
  }

  public void setConceptType(final FdbNameValue conceptType)
  {
    ConceptType = conceptType;
  }
}
