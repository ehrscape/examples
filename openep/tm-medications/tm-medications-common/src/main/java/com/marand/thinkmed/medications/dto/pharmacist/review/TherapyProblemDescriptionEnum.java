package com.marand.thinkmed.medications.dto.pharmacist.review;

/**
 * @author Mitja Lapajne
 */
public enum TherapyProblemDescriptionEnum
{
  DRUG_RELATED_PROBLEM_CATEGORY("pharmacy_review_report/pharmacist_medication_review/medication_item_assessment/drug-related_problem/category"),
  DRUG_RELATED_PROBLEM_OUTCOME("pharmacy_review_report/pharmacist_medication_review/medication_item_assessment/drug-related_problem/outcome"),
  DRUG_RELATED_PROBLEM_IMPACT("pharmacy_review_report/pharmacist_medication_review/medication_item_assessment/drug-related_problem/impact"),
  PHARMACOKINETIC_ISSUE_CATEGORY("pharmacy_review_report/pharmacist_medication_review/medication_item_assessment/pharmacokinetic_issue/category"),
  PHARMACOKINETIC_ISSUE_OUTCOME("pharmacy_review_report/pharmacist_medication_review/medication_item_assessment/pharmacokinetic_issue/outcome"),
  PHARMACOKINETIC_ISSUE_IMPACT("pharmacy_review_report/pharmacist_medication_review/medication_item_assessment/pharmacokinetic_issue/impact"),
  PATIENT_RELATED_PROBLEM_CATEGORY("pharmacy_review_report/pharmacist_medication_review/medication_item_assessment/patient_related_problem/category"),
  PATIENT_RELATED_PROBLEM_OUTCOME("pharmacy_review_report/pharmacist_medication_review/medication_item_assessment/patient_related_problem/outcome"),
  PATIENT_RELATED_PROBLEM_IMPACT("pharmacy_review_report/pharmacist_medication_review/medication_item_assessment/patient_related_problem/impact")
  ;
  private final String path;

  TherapyProblemDescriptionEnum(final String path)
  {
    this.path = path;
  }
  public String getPath()
  {
    return path;
  }
}
