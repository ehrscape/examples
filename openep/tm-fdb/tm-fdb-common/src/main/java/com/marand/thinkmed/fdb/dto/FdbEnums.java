package com.marand.thinkmed.fdb.dto;

/**
 * @author Mitja Lapajne
 */
public enum FdbEnums
{
  MDDF_TERMINOLOGY("MDDF", 1L),
  SNOMED_TERMINOLOGY("SNoMedCT", 2L),

  DRUG_CONCEPT_TYPE("Drug", 1L),
  PRODUCT_CONCEPT_TYPE("Product", 2L),
  SUBSTANCE_CONCEPT_TYPE("Substance", 3L),

  GENDER_MALE("Male", 1L),
  GENDER_FEMALE("Female", 2L),

  MINIMUM_CONDITION_ALERT_SEVERITY_CONTRAINDICATION("Contraindication", 1L),
  MINIMUM_CONDITION_ALERT_SEVERITY_PRECAUTION("Precaution", 2L),

  MINIMUM_INTERACTION_ALERT_SEVERITY_LOW_RISK("LowRisk", 1L),
  MINIMUM_INTERACTION_ALERT_SEVERITY_MEDIUM_RISK("Moderate", 2L),
  MINIMUM_INTERACTION_ALERT_SEVERITY_SIGNIFICANT_RISK("SignificantRisk", 3L),
  MINIMUM_INTERACTION_ALERT_SEVERITY_HIGH_RISK("HighRisk", 4L);

  FdbEnums(final String name, final Long value)
  {
    this.name = name;
    this.value = value;
  }

  private final String name;
  private final Long value;

  public FdbNameValue getNameValue()
  {
    return new FdbNameValue(name, value);
  }
}
