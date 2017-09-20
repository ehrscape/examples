package com.marand.thinkmed.medications.rule.parameters;

import com.marand.thinkmed.medications.rule.MedicationParacetamolRuleType;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class ParacetamolRuleParameters extends RuleParameters
{
  private MedicationParacetamolRuleType medicationParacetamolRuleType;

  private Double patientWeight;
  private Long patientAgeInYears;

  public MedicationParacetamolRuleType getMedicationParacetamolRuleType()
  {
    return medicationParacetamolRuleType;
  }

  public void setMedicationParacetamolRuleType(final MedicationParacetamolRuleType medicationParacetamolRuleType)
  {
    this.medicationParacetamolRuleType = medicationParacetamolRuleType;
  }

  public Double getPatientWeight()
  {
    return patientWeight;
  }

  public void setPatientWeight(final Double patientWeight)
  {
    this.patientWeight = patientWeight;
  }

  public Long getPatientAgeInYears()
  {
    return patientAgeInYears;
  }

  public void setPatientAgeInYears(final Long patientAgeInYears)
  {
    this.patientAgeInYears = patientAgeInYears;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("medicationParacetamolRuleType", medicationParacetamolRuleType)
        .append("patientWeight", patientWeight)
        .append("patientAgeInYears", patientAgeInYears);
  }
}
