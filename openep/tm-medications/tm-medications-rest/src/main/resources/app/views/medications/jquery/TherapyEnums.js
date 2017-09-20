/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */

Class.define('app.views.medications.TherapyEnums', 'tm.jquery.Object', {
  /** members: configs */

  //ALWAYS ADD NEW ENUMS TO TherapyEnumsTest.java

  statics: {
    /** enums */
    templateTypeEnum:  //java class TherapyTemplateTypeEnum
    {
      USER: "USER",
      ORGANIZATIONAL: "ORGANIZATIONAL",
      PATIENT: "PATIENT"
    },
    medicationRouteTypeEnum:  //java class MedicationRouteTypeEnum
    {
      IV: "IV",
      IM: "IM",
      INHAL: "INHAL",
      ORAL: "ORAL"
    },
    dosingFrequencyTypeEnum:  //java class DosingFrequencyTypeEnum
    {
      BETWEEN_DOSES: "BETWEEN_DOSES", //time between doses in hours
      DAILY_COUNT: "DAILY_COUNT", //number of administrations per day
      MORNING: "MORNING", //once per day in the morning
      NOON: "NOON", //once per day at noon
      EVENING: "EVENING", //once per day in the evening
      ONCE_THEN_EX: "ONCE_THEN_EX" //only once
    },
    medicationOrderFormType: //java class MedicationOrderFormType
    {
      SIMPLE: "SIMPLE",
      COMPLEX: "COMPLEX",
      OXYGEN: "OXYGEN",
      DESCRIPTIVE: "DESCRIPTIVE"
    },
    therapyStatusEnum:    //java class TherapyStatusEnum
    {
      NORMAL: "NORMAL",
      ABORTED: "ABORTED",
      CANCELLED: "CANCELLED",
      SUSPENDED: "SUSPENDED",
      LATE: "LATE",
      VERY_LATE: "VERY_LATE",
      FUTURE: "FUTURE"
    },
    administrationTypeEnum: //java class AdministrationTypeEnum
    {
      START: "START",
      STOP: "STOP",
      ADJUST_INFUSION: "ADJUST_INFUSION",
      INFUSION_SET_CHANGE: "INFUSION_SET_CHANGE",
      BOLUS: "BOLUS"
    },
    administrationResultEnum: //java class AdministrationResultEnum
    {
      GIVEN: "GIVEN",
      DEFER: "DEFER",
      SELF_ADMINISTERED: "SELF_ADMINISTERED",
      NOT_GIVEN: "NOT_GIVEN"
    },
    selfAdministrationTypeEnum: // java class MedicationActionAction
    {
      LEVEL_1: "LEVEL_1",
      LEVEL_2: "LEVEL_2",
      LEVEL_3: "LEVEL_3"
    },
    infusionSetChangeEnum: //java class InfusionSetChangeEnum
    {
      INFUSION_SYSTEM_CHANGE: "INFUSION_SYSTEM_CHANGE",
      INFUSION_SYRINGE_CHANGE: "INFUSION_SYRINGE_CHANGE"
    },
    therapySortTypeEnum: //java class TherapySortTypeEnum
    {
      DESCRIPTION_ASC: "DESCRIPTION_ASC",
      DESCRIPTION_DESC: "DESCRIPTION_DESC",
      CREATED_TIME_ASC: "CREATED_TIME_ASC",
      CREATED_TIME_DESC: "CREATED_TIME_DESC"
    },
    medicationStartCriterionEnum: //java class MedicationStartCriterionEnum
    {
      BY_DOCTOR_ORDERS: "BY_DOCTOR_ORDERS"
    },
    therapyTag: //java class TherapyTagEnum
    {
      PRESCRIPTION: "PRESCRIPTION",
      SOURCE: "SOURCE",
      GROUP_UUID: "GROUP_UUID",
      REVIEWED_ON_DISCHARGE: "REVIEWED_ON_DISCHARGE"
    },
    warningSeverityEnum: //java class MedicationsWarningDto.Severity
    {
      HIGH: "HIGH",
      SIGNIFICANT: "SIGNIFICANT",
      LOW: "LOW"
    },
    medicationAdditionalInstructionEnum: //java class MedicationAdditionalInstructionEnum
    {
      HEPARIN_05: "HEPARIN_05",
      HEPARIN_1: "HEPARIN_1",
      ADJUST_TO_FLUID_BALANCE: "ADJUST_TO_FLUID_BALANCE",
      BEFORE_MEAL: "BEFORE_MEAL",
      AFTER_MEAL: "AFTER_MEAL"
    },
    prescriptionChangeTypeEnum: //java class PrescriptionChangeTypeEnum
    {
      NEW_ADMISSION_PRESCRIPTION: "NEW_ADMISSION_PRESCRIPTION",
      ADDITION_TO_EXISTING_PRESCRIPTION: "ADDITION_TO_EXISTING_PRESCRIPTION"
    },
    pharmacistReviewTaskStatusEnum: //java class PharmacistReviewTaskStatusEnum
    {
      PENDING: "PENDING",
      IN_PROGRESS: "IN_PROGRESS",
      DONE: "DONE"
    },
    pharmacistTherapyChangeType: // java class PharmacistTherapyChangeType
    {
      NONE: "NONE",
      EDIT: "EDIT",
      ABORT: "ABORT",
      SUSPEND: "SUSPEND"
    },
    reviewPharmacistReviewAction: // java class ReviewPharmacistReviewAction
    {
      ACCEPTED: "ACCEPTED",
      MODIFIED: "MODIFIED",
      DENIED: "DENIED",
      REISSUED: "REISSUED",
      COPIED: "COPIED",
      ABORTED: "ABORTED"
    },
    therapyProblemDescriptionEnum: // java class TherapyProblemDescriptionEnum
    {
      DRUG_RELATED_PROBLEM_CATEGORY: "DRUG_RELATED_PROBLEM_CATEGORY",
      DRUG_RELATED_PROBLEM_OUTCOME: "DRUG_RELATED_PROBLEM_OUTCOME",
      DRUG_RELATED_PROBLEM_IMPACT: "DRUG_RELATED_PROBLEM_IMPACT",
      PHARMACOKINETIC_ISSUE_CATEGORY: "PHARMACOKINETIC_ISSUE_CATEGORY",
      PHARMACOKINETIC_ISSUE_OUTCOME: "PHARMACOKINETIC_ISSUE_OUTCOME",
      PHARMACOKINETIC_ISSUE_IMPACT: "PHARMACOKINETIC_ISSUE_IMPACT",
      PATIENT_RELATED_PROBLEM_CATEGORY: "PATIENT_RELATED_PROBLEM_CATEGORY",
      PATIENT_RELATED_PROBLEM_OUTCOME: "PATIENT_RELATED_PROBLEM_OUTCOME",
      PATIENT_RELATED_PROBLEM_IMPACT: "PATIENT_RELATED_PROBLEM_IMPACT"
    },
    pharmacistReviewStatusEnum: // java class TherapyPharmacistReviewStatusEnum
    {
      DRAFT: "Draft",
      FINAL: "Final",
      REVIEWED: "Reviewed"
    },
    warningType:  // java class WarningType.java
    {
      UNMATCHED: "UNMATCHED",
      INTERACTION: "INTERACTION",
      ALLERGY: "ALLERGY",
      CONTRAINDICATION: "CONTRAINDICATION",
      DUPLICATE: "DUPLICATE",
      BNF: "BNF",
      PARACETAMOL: "PARACETAMOL",
      MENTAL_HEALTH: "MENTAL_HEALTH",
      CUSTOM: "CUSTOM"
    },
    additionalWarningType:
    {
      BNF: "BNF",
      PARACETAMOL: "PARACETAMOL"
    },
    taskTypeEnum:  // java class TaskTypeEnum
    {
      ADMINISTRATION_TASK: "ADMINISTRATION_TASK",
      INFUSION_BAG_CHANGE_TASK: "INFUSION_BAG_CHANGE_TASK",
      PHARMACIST_REVIEW: "PHARMACIST_REVIEW",
      PHARMACIST_REMINDER: "PHARMACIST_REMINDER",
      SUPPLY_REMINDER: "SUPPLY_REMINDER",
      SUPPLY_REVIEW: "SUPPLY_REVIEW",
      DISPENSE_MEDICATION: "DISPENSE_MEDICATION",
      DOCTOR_REVIEW: "DOCTOR_REVIEW",
      SWITCH_TO_ORAL: "SWITCH_TO_ORAL",
      CHECK_NEW_ALLERGIES: "CHECK_NEW_ALLERGIES",
      CHECK_MENTAL_HEALTH_MEDS: "CHECK_MENTAL_HEALTH_MEDS",
      PERFUSION_SYRINGE_START: "PERFUSION_SYRINGE_START",
      PERFUSION_SYRINGE_COMPLETE: "PERFUSION_SYRINGE_COMPLETE",
      PERFUSION_SYRINGE_DISPENSE: "PERFUSION_SYRINGE_DISPENSE"
    },
    therapyPharmacistReviewStatusEnum: // java class TherapyPharmacistReviewStatusEnum
    {
      REVIEWED: "REVIEWED",
      REVIEWED_REFERRED_BACK: "REVIEWED_REFERRED_BACK",
      NOT_REVIEWED: "NOT_REVIEWED"
    },
    medicationSupplyTypeEnum: // java class MedicationSupplyTypeEnum
    {
      WARD_STOCK: "WARD_STOCK",
      NON_WARD_STOCK: "NON_WARD_STOCK",
      PATIENTS_OWN: "PATIENTS_OWN",
      ONE_STOP_DISPENSING: "ONE_STOP_DISPENSING"
    },
    therapySourceGroupEnum: // java class TherapySourceGroupEnum
    {
      LAST_HOSPITALIZATION: "LAST_HOSPITALIZATION",
      MEDICATION_ON_ADMISSION: "MEDICATION_ON_ADMISSION",
      LAST_DISCHARGE_MEDICATIONS: "LAST_DISCHARGE_MEDICATIONS"
    },
    medicationOrderActionEnum: // java class MedicationOrderActionEnum
    {
      PRESCRIBE: "PRESCRIBE",
      EDIT: "EDIT",
      SUSPEND: "SUSPEND",
      SUSPEND_ADMISSION: "SUSPEND_ADMISSION",
      ABORT: "ABORT"
    },
    medicationOnAdmissionStatus: // java class MedicationOnAdmissionStatus
    {
      PENDING: "PENDING",
      PRESCRIBED: "PRESCRIBED",
      SUSPENDED: "SUSPENDED",
      ABORTED: "ABORTED"
    },
    medicationOnDischargeStatus: // java class MedicationOnDischargeStatus
    {
      PRESCRIBED: "PRESCRIBED",
      EDITED_AND_PRESCRIBED: "EDITED_AND_PRESCRIBED",
      NOT_PRESCRIBED: "NOT_PRESCRIBED"
    },
    reconciliationRowGroupEnum: // java class ReconciliationRowGroupEnum
    {
      NOT_CHANGED: "NOT_CHANGED",
      CHANGED: "CHANGED",
      ONLY_ON_ADMISSION: "ONLY_ON_ADMISSION",
      ONLY_ON_DISCHARGE: "ONLY_ON_DISCHARGE"
    },
    actionReasonTypeEnum: // java class ActionReasonType
    {
      SUSPEND: "SUSPEND",
      SUSPEND_ADMISSION: "SUSPEND_ADMISSION",
      EDIT: "EDIT",
      ABORT: "ABORT",
      ADMINISTRATION_DEFER: "ADMINISTRATION_DEFER",
      ADMINISTRATION_NOT_GIVEN: "ADMINISTRATION_NOT_GIVEN",
      NOT_RECORDED: "NOT_RECORDED"
    },
    bnfMaximumUnitType:  //java class BnfMaximumUnitType
    {
      DAY: "DAY",
      WEEK: "WEEK"
    },
    prescriptionDocumentType: //java class PrescriptionDocumentType
    {
      GREEN: "GREEN",
      WHITE: "WHITE"
    },
    illnessConditionType: //java class IllnessConditionType
    {
      ACUTE_CONDITION: "ACUTE_CONDITION",
      CHRONIC_CONDITION: "CHRONIC_CONDITION"
    },
    selfAdministeringActionEnum: //java class SelfAdministeringActionEnum
    {
      CHARTED_BY_NURSE: "CHARTED_BY_NURSE",
      AUTOMATICALLY_CHARTED: "AUTOMATICALLY_CHARTED",
      STOP_SELF_ADMINISTERING: "STOP_SELF_ADMINISTERING"
    },
    administrationStatusEnum: //java class AdministrationStatusEnum
    {
      PLANNED: "PLANNED",
      DUE: "DUE",
      LATE: "LATE",
      COMPLETED: "COMPLETED",
      COMPLETED_LATE: "COMPLETED_LATE",
      COMPLETED_EARLY: "COMPLETED_EARLY",
      FAILED: "FAILED"
    },
    mentalHealthGroupEnum:
    {
      NEW_MEDICATION: "NEW_MEDICATION",
      INPATIENT_ACTIVE: "INPATIENT_ACTIVE",
      INPATIENT_ABORTED: "INPATIENT_ABORTED",
      TEMPLATES: "TEMPLATES"
    },
    mentalHealthDocumentType: // MentalHealthDocumentType.java
    {
      T2: "T2",
      T3: "T3"
    },
    medicationFinderFilterEnum: // MedicationFinderFilterEnum.java
    {
      MENTAL_HEALTH: "MENTAL_HEALTH",
      OUTPATIENT_PRESCRIPTION: "OUTPATIENT_PRESCRIPTION",
      INPATIENT_PRESCRIPTION: "INPATIENT_PRESCRIPTION",
      FORMULARY: "FORMULARY"
    },
    therapyTemplateModeEnum: //TherapyTemplateModeEnum.java
    {
      INPATIENT: "INPATIENT",
      OUTPATIENT: "OUTPATIENT"
    },
    medicationRuleEnum: // MedicationRuleEnum.java
    {
      PARACETAMOL_MAX_DAILY_DOSE: "PARACETAMOL_MAX_DAILY_DOSE"
    },
    medicationParacetamolRuleType: // MedicationParacetamolRuleType.java
    {
      FOR_THERAPY: "FOR_THERAPY",
      FOR_THERAPIES: "FOR_THERAPIES",
      FOR_ADMINISTRATION: "FOR_ADMINISTRATION"
    },
    therapyIntervalPaneSelectionIds:
    {
      UNTIL_CANCELED: "UNTIL_CANCELED",
      HOURS: "HOURS",
      DAYS: "DAYS",
      WEEKS: "WEEKS",
      MONTHS: "MONTHS",
      DATE: "DATE"
    },
    therapyDoseTypeEnum: //TherapyDoseTypeEnum.java
    {
      RATE: "RATE",
      QUANTITY: "QUANTITY",
      VOLUME_SUM: "VOLUME_SUM",
      RATE_QUANTITY: "RATE_QUANTITY",
      RATE_VOLUME_SUM: "RATE_VOLUME_SUM"
    },
    medicationAuthorisationSloveniaClusterPayer: // MedicationAuthorisationSloveniaCluster.Payer
    {
      PERSON: "PERSON",
      UPB: "UPB",
      MO: "MO",
      OTHER: "OTHER"
    },
    medicationTypeEnum: //MedicationTypeEnum.java
    {
      //main
      MEDICATION: "MEDICATION",
      SUPPLEMENT: "SUPPLEMENT",
      SOLUTION: "SOLUTION",
      OXYGEN: "OXYGEN",

      //other
      ANTIBIOTIC: "ANTIBIOTIC"
    },
    therapyTitrationTypeEnum: // TitrationType.java
    {
      BLOOD_SUGAR : "BLOOD_SUGAR",
      INR: "INR"
    },
    therapyChangeReasonEnum: //TherapyChangeReasonEnum.java
    {
      TEMPORARY_LEAVE: "TEMPORARY_LEAVE"
    },
    oxygenDeliveryClusterRoute: //OxygenDeliveryCluster.Route
    {
      CPAP_MASK : "CPAP_MASK",
      CPAP_NASAL : "CPAP_NASAL",
      FULL_FACE_MASK : "FULL_FACE_MASK",
      NASAL_NIV_MASK : "NASAL_NIV_MASK",
      OXYGEN_MASK : "OXYGEN_MASK",
      NASAL_CATHETER : "NASAL_CATHETER",
      HIGH_FLOW_NASAL_CATHETER : "HIGH_FLOW_NASAL_CATHETER",
      VENTURI_MASK : "VENTURI_MASK",
      OHIO_MASK : "OHIO_MASK",
      INCUBATOR : "INCUBATOR",
      TENT : "TENT",
      T_TUBE : "T_TUBE",
      TRACHEAL_TUBE : "TRACHEAL_TUBE",
      TRACHEAL_CANNULA : "TRACHEAL_CANNULA",
      HIGH_FLOW_TRACHEAL_CANNULA : "HIGH_FLOW_TRACHEAL_CANNULA"
    },
    flowRateMode: //OxygenDeliveryCluster.FlowRateMode
    {
      LOW_FLOW: "LOW_FLOW",
      HIGH_FLOW: "HIGH_FLOW"
    },
    adjustAdministrationSubtype: //AdjustAdministrationSubtype.java
    {
      OXYGEN: "OXYGEN"
    },
    startAdministrationSubtype: //StartAdministrationSubtype.java
    {
      OXYGEN: "OXYGEN"
    },
    therapyChangeTypeEnum: //TherapyChangeType.java
    {
      MEDICATION: "MEDICATION",
      ROUTE: "ROUTE",
      VARIABLE_DOSE: "VARIABLE_DOSE",
      VARIABLE_DOSE_TO_DOSE: "VARIABLE_DOSE_TO_DOSE",
      DOSE_TO_VARIABLE_DOSE: "DOSE_TO_VARIABLE_DOSE",
      VARIABLE_RATE: "VARIABLE_RATE",
      VARIABLE_RATE_TO_RATE: "VARIABLE_RATE_TO_RATE",
      RATE_TO_VARIABLE_RATE: "RATE_TO_VARIABLE_RATE",
      DOSE: "DOSE",
      DOSE_INTERVAL: "DOSE_INTERVAL",
      DOSE_TIMES: "DOSE_TIMES",
      RATE: "RATE",
      INFUSION_DURATION: "INFUSION_DURATION",
      ADDITIONAL_CONDITIONS: "ADDITIONAL_CONDITIONS",
      WHEN_NEEDED: "WHEN_NEEDED",
      MAX_DOSES: "MAX_DOSES",
      DOCTOR_ORDERS: "DOCTOR_ORDERS",
      COMMENT: "COMMENT",
      INDICATION: "INDICATION",
      START: "START",
      END: "END",
      DEVICE: "DEVICE",
      SATURATION: "SATURATION"
    },
    additionalWarningsTypeEnum: //AdditionalWarningsType.java
    {
      MENTAL_HEALTH: "MENTAL_HEALTH",
      ALLERGIES: "ALLERGIES"
    },
    therapyDocumentType: //TherapyDocumentType.java
    {
      T2: "T2",
      T3: "T3",
      EER_PRESCRIPTION: "EER_PRESCRIPTION",
      EXTERNAL_EER_PRESCRIPTION: "EXTERNAL_EER_PRESCRIPTION"
    },
    barcodeSearchResult: //BarcodeSearchResult.java
    {
      TASK_FOUND: "TASK_FOUND",
      NO_MEDICATION: "NO_MEDICATION",
      NO_TASK: "NO_TASK",
      MULTIPLE_TASKS: "MULTIPLE_TASKS"
    }
  }
});