package com.marand.thinkmed.fdb.dto;

/**
 * @author Mitja Lapajne
 */
public class FdbTerminologyWithConceptDto extends FdbTerminologyDto
{
  private FdbNameValue ConceptType;

  public FdbNameValue getConceptType()
  {
    return ConceptType;
  }

  public void setConceptType(final FdbNameValue conceptType)
  {
    ConceptType = conceptType;
  }
}
