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

package com.marand.thinkmed.medications.service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.marand.maf.core.Opt;
import com.marand.openehr.medications.tdo.MedicationAuthorisationSloveniaCluster.PrescriptionStatus;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.UserPersonDto;
import com.marand.thinkmed.medications.dto.AdministrationTaskLimitsDto;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.PrescriptionDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.TherapyViewPatientDto;
import com.marand.thinkmed.medications.dto.TitrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeGroupDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentType;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentsDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringePreparationDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewsDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import com.marand.thinkmed.medications.dto.pharmacist.review.SupplyDataForPharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyProblemDescriptionEnum;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskSimpleDto;
import com.marand.thinkmed.medications.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import com.marand.thinkmed.medications.dto.supply.MedicationSupplyCandidateDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsActionDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.dto.DoseRangeCheckDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationWarningsDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.security.access.annotation.Secured;

/**
 * @author Bostjan Vester
 */
@Secured("ROLE_User")
public interface MedicationsService
{
  Map<String, Object> getProperties();

  UserPersonDto getUserData();

  Long getDemoPatientId();

  List<TreeNodeData> findMedications(
      @Nonnull String searchString,
      String careProviderId,
      @Nonnull EnumSet<MedicationFinderFilterEnum> filters);

  MedicationDataDto getMedicationData(long medicationId, String careProviderId);

  Map<Long, MedicationDataDto> getMedicationDataMap(@Nonnull Set<Long> medicationIdsList);

  List<MedicationRouteDto> getMedicationRoutes(long medicationId);

  List<DoseRangeCheckDto> findDoseRangeChecks(long medicationId);

  MedicationWarningsDto findMedicationWarnings(
      String patientId,
      long patientAgeInDays,
      Double patientWeightInKg,
      Double bsaInM2,
      boolean isFemale,
      List<ExternalCatalogDto> diseases,
      List<NamedExternalDto> patientAllergiesList,
      List<MedicationForWarningsSearchDto> medicationSummaries,
      Set<WarningSeverity> severityFilterValues);

  void newAllergiesAddedForPatient(@Nonnull String patientId, @Nonnull Collection<NamedExternalDto> newAllergies);

  AdditionalWarningsDto getAdditionalWarnings(
      @Nonnull String patientId,
      @Nonnull List<AdditionalWarningsType> additionalWarningsTypes,
      @Nonnull PatientDataForMedicationsDto patientDataForMedications,
      @Nonnull Locale locale);

  void handleAdditionalWarningsAction(@Nonnull AdditionalWarningsActionDto additionalWarningsActionDto);

  void tagTherapyForPrescription(String patientId, String compositionId, String centralCaseId, String ehrOrderName);

  void untagTherapyForPrescription(String patientId, String compositionId, String centralCaseId, String ehrOrderName);

  void saveNewMedicationOrder(
      @Nonnull String patientId,
      @Nonnull List<SaveMedicationOrderDto> medicationOrders,
      String centralCaseId,
      DateTime hospitalizationStart,
      String careProviderId,
      NamedExternalDto prescriber,
      String lastLinkName,
      DateTime saveDateTime,
      @Nonnull Locale locale);

  List<String> saveMedicationsOnAdmission(
      String patientId,
      List<MedicationOnAdmissionDto> therapyList,
      String centralCaseId,
      @Nullable String careProviderId,
      NamedExternalDto prescriber,
      @Nullable DateTime saveDateTime,
      DateTime hospitalizationStart,
      Locale locale);

  List<MedicationOnAdmissionDto> getMedicationsOnAdmission(
      String patientId,
      DateTime hospitalizationStart,
      Locale locale);

  List<String> saveMedicationsOnDischarge(
      String patientId,
      List<MedicationOnDischargeDto> therapyList,
      String centralCaseId,
      @Nullable String careProviderId,
      NamedExternalDto prescriber,
      @Nullable DateTime saveDateTime,
      DateTime hospitalizationStart,
      Locale locale);

  List<MedicationOnDischargeDto> getMedicationsOnDischarge(
      String patientId,
      DateTime hospitalizationStart,
      DateTime saveDateTime,
      Locale locale);

  List<ReconciliationRowDto> getReconciliationSummaryGroups(
      String patientId,
      DateTime fromDate,
      DateTime when,
      Locale locale);

  TherapyFlowDto getTherapyFlow(
      String patientId,
      String centralCaseId,
      Double patientHeight,
      DateTime startDate,
      int dayCount,
      Integer todayIndex,
      RoundsIntervalDto roundsInterval,
      TherapySortTypeEnum therapySortTypeEnum,
      @Nullable String careProviderId,
      Locale locale);

  TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      String patientId,
      String compositionUid,
      String ehrOrderName,
      RoundsIntervalDto roundsInterval);

  void modifyTherapy(
      String patientId,
      TherapyDto therapy,
      TherapyChangeReasonDto changeReasonDto,
      String centralCaseId,
      @Nullable String careProviderId,
      NamedExternalDto prescriber,
      Boolean therapyAlreadyStarted,
      DateTime saveDateTime,
      String basedOnPharmacyReviewId,
      Locale locale);

  void abortTherapy(String patientId, String compositionUid, String ehrOrderName, TherapyChangeReasonDto changeReasonDto);

  void abortAllTherapiesForPatient(@Nonnull String patientId);

  void reviewTherapy(String patientId, String compositionUid, String ehrOrderName);

  void suspendTherapy(String patientId, String compositionUid, String ehrOrderName, TherapyChangeReasonDto changeReason);

  void reissueTherapy(String patientId, String compositionUid, String ehrOrderName);

  void createAdditionalAdministrationTask(
      String therapyCompositionUid,
      String ehrOrderName,
      String patientId,
      StartAdministrationDto administrationDto);

  void suspendAllTherapies(@Nonnull String patientId);

  void suspendAllTherapiesOnTemporaryLeave(@Nonnull String patientId);

  void reissueAllTherapiesOnReturnFromTemporaryLeave(@Nonnull String patientId);

  List<MedicationForWarningsSearchDto> getTherapiesForWarningsSearch(String patientId);

  PatientDataForMedicationsDto getPatientData(String patientId);

  TherapyViewPatientDto getTherapyViewPatientData(@Nonnull String patientId);

  byte[] getMedicationDocument(String reference);

  byte[] getTherapyReport(String patientId, Locale locale);

  List<Interval> getPatientBaselineInfusionIntervals(String patientId);

  String getTherapyReportDataJson(@Nonnull String patientId, @Nonnull Locale locale);

  TherapySurgeryReportDto getTherapySurgeryReportData(
      String patientId,
      Double patientHeight,
      RoundsIntervalDto roundsInterval,
      Locale locale,
      DateTime when);

  String getTherapyFormattedDisplay(String patientId, String therapyId, Locale locale);

  TherapyAuditTrailDto getTherapyAuditTrail(
      String patientId,
      String compositionId,
      String ehrOrderName,
      Double patientHeight,
      Locale locale);

  TherapyDto fillTherapyDisplayValues(TherapyDto therapy, Locale locale);

  TherapyDto getTherapyDto(String patientId, String therapyId, Locale locale);

  PharmacistReviewTherapyDto fillPharmacistReviewTherapyOnEdit(
      TherapyDto originalTherapy,
      TherapyDto changedTherapy,
      Locale locale);

  List<TreeNodeData> findSimilarMedications(long medicationId, @Nonnull List<Long> routeCodes);

  List<MedicationDto> findMedicationProducts(long medicationId, @Nonnull List<Long> routeCodes);

  List<MedicationSupplyCandidateDto> getMedicationSupplyCandidates(final long medicationId, final long routeId);

  List<MedicationRouteDto> getRoutes();

  List<DoseFormDto> getDoseForms();

  List<String> getMedicationBasicUnits();

  void savePatientReferenceWeight(String patientId, double weight);

  void saveConsecutiveDays(String patientId, String compositionUid, String ehrOrderName, Integer consecutiveDays);

  List<TherapyDto> getLastTherapiesForPreviousHospitalization(String patientId, Double patientHeight, Locale locale);

  List<MedicationOnAdmissionGroupDto> getTherapiesOnAdmissionGroups(
      String patientId,
      DateTime currentHospitalizationStart,
      Locale locale);

  List<MedicationOnDischargeGroupDto> getTherapiesOnDischargeGroups(
      String patientId,
      Double patientHeight,
      @Nullable DateTime saveDateTime,
      DateTime lastHospitalizationStart,
      boolean hospitalizationActive,
      Locale locale);

  List<MentalHealthTherapyDto> getCurrentHospitalizationMentalHealthTherapies(
      String patientId,
      DateTime lastHospitalizationStart,
      Locale locale);

  List<TherapyDto> getLinkTherapyCandidates(
      @Nullable String patientId,
      @Nullable Double referenceWeight,
      Double patientHeight,
      @Nullable Locale locale);

  TherapyTemplatesDto getTherapyTemplates(
      @Nonnull String patientId,
      @Nonnull TherapyTemplateModeEnum templateMode,
      String careProviderId,
      Double referenceWeight,
      Double patientHeight,
      @Nonnull Locale locale);

  long saveTherapyTemplate(TherapyTemplateDto therapyTemplate, TherapyTemplateModeEnum templateMode);

  void deleteTherapyTemplate(long templateId);

  TherapyTimelineDto getTherapyTimeline(
      @Nonnull String patientId,
      @Nonnull Interval interval,
      @Nonnull TherapySortTypeEnum sortTypeEnum,
      boolean hidePastTherapies,
      boolean hideFutureTherapies,
      PatientDataForMedicationsDto patientData,
      RoundsIntervalDto roundsInterval,
      Locale locale);

  TherapyTimelineDto getPharmacistTimeline(
      @Nonnull String patientId,
      @Nonnull Interval interval,
      @Nonnull TherapySortTypeEnum sortTypeEnum,
      boolean hidePastTherapies,
      PatientDataForMedicationsDto patientData,
      RoundsIntervalDto roundsInterval,
      Locale locale);

  DocumentationTherapiesDto getTherapyDataForDocumentation(
      String patientId,
      Interval centralCaseEffective,
      String centralCaseId,
      boolean isOutpatient,
      Locale locale);

  void confirmTherapyAdministration(
      String compositionUid,
      String instructionName,
      String patientId,
      AdministrationDto administrationDto,
      boolean editMode,
      String centralCaseId,
      @Nullable String careProviderId,
      boolean requestSupply,
      Locale locale);

  void setDoctorConfirmationResult(String taskId, Boolean result);

  void rescheduleTask(
      @Nonnull String patientId,
      @Nonnull String taskId,
      @Nonnull DateTime newTime,
      @Nonnull String therapyId);

  void rescheduleTasks(
      @Nonnull String patientId,
      @Nonnull String taskId,
      @Nonnull DateTime newTime,
      @Nonnull String therapyId);

  void setAdministrationTitratedDose(
      @Nonnull String patientId,
      @Nonnull String latestTherapyId,
      @Nonnull StartAdministrationDto administrationDto,
      boolean confirmAdministration,
      String centralCaseId,
      String careProviderId,
      DateTime until,
      @Nonnull Locale locale);

  void deleteTask(String patientId, String taskId, String groupUUId, String therapyId, String comment);

  void deletePatientTasksOfTypes(final String patientId, final Set<TaskTypeEnum> taskTypes);

  void deleteAdministration(
      String patientId,
      AdministrationDto administration,
      TherapyDoseTypeEnum therapyDoseType,
      String therapyId,
      String comment);

  List<NamedExternalDto> getCareProfessionals();

  boolean assertPasswordForUsername(String username, String password);

  AdministrationTaskLimitsDto getAdministrationTaskLimits(@Nullable String careProviderId);

  List<PatientTaskDto> getPharmacistReviewTasks(Opt<Collection<String>> careProviderIds, Opt<Collection<String>> patientIds);

  List<AdministrationPatientTaskDto> getAdministrationTasks(
      Opt<Collection<String>> careProviderIds,
      Opt<Collection<String>> patientIds,
      @Nonnull Locale locale);

  RuleResult applyMedicationRule(
      @Nonnull RuleParameters ruleParameters,
      @Nonnull Locale locale);

  List<MedicationSupplyTaskDto> findSupplyTasks(
      Opt<Collection<String>> careProviderIds,
      Opt<Collection<String>> patientIds,
      Set<TaskTypeEnum> taskTypes,
      boolean closedTasksOnly,
      boolean includeUnverifiedDispenseTasks,
      @Nonnull Locale locale);

  String savePharmacistReview(
      String patientId,
      PharmacistReviewDto pharmacistReview,
      final Boolean authorize,
      Locale locale);

  void reviewPharmacistReview(
      String patientId,
      String pharmacistReviewUid,
      ReviewPharmacistReviewAction reviewAction,
      TherapyDto modifiedTherapy,
      List<String> deniedReviews,
      String centralCaseId,
      @Nullable String careProviderId,
      NamedExternalDto prescriber,
      Locale locale);

  void handleNurseResupplyRequest(
      String patientId,
      String therapyCompositionUid,
      String ehrOrderName,
      Locale locale);

  Map<TherapyProblemDescriptionEnum, List<NamedExternalDto>> getProblemDescriptionNamedIdentities(Locale locale);

  PharmacistReviewsDto getPharmacistReviews(String patientId, DateTime fromDate, Locale locale);

  List<PharmacistReviewDto> getPharmacistReviewsForTherapy(String patientId, String compositionUid, Locale locale);

  void authorizePharmacistReviews(
      String patientId,
      List<String> pharmacistReviewUids,
      Locale locale);

  void deletePharmacistReview(String patientId, String pharmacistReviewUid);

  List<NamedExternalDto> getCurrentUserCareProviders();

  List<AdministrationDto> calculateTherapyAdministrationTimes(TherapyDto therapy);

  DateTime calculateNextTherapyAdministrationTime(TherapyDto therapy, boolean newPrescription);

  DateTime findPreviousTaskForTherapy(String patientId, String compositionUid, String ehrOrderName);

  void dismissSupplyTask(String patientId, List<String> taskIds);

  void deleteNurseSupplyTask(String patientId, String taskId, Locale locale);

  void confirmSupplyReminderTask(
      String taskId,
      String compositionUid,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment);

  void editSupplyReminderTask(
      String taskId,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment);

  void confirmSupplyReviewTask(
      String patientId,
      String taskId,
      String compositionUid,
      boolean createSupplyReminder,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment);

  void confirmPharmacistDispenseTask(
      String patientId,
      String taskId,
      String compositionUid,
      TherapyAssigneeEnum requesterRole,
      SupplyRequestStatus supplyRequestStatus);

  MedicationSupplyTaskSimpleDto getSupplySimpleTask(String taskId, Locale locale);

  Map<ActionReasonType, List<CodedNameDto>> getActionReasons(ActionReasonType type);

  List<MentalHealthTemplateDto> getMentalHealthTemplates();

  void saveMentalHealthReport(MentalHealthDocumentDto mentalHealthDocumentDto, NamedExternalDto careProvider);

  SupplyDataForPharmacistReviewDto getSupplyDataForPharmacistReview(
      @Nonnull String patientId,
      @Nonnull String therapyCompositionUid);

  Map<String, String> getMedicationExternalValues(
      String externalSystem, MedicationsExternalValueType valueType, Set<String> valuesSet);

  String getMedicationExternalId(String externalSystem, long medicationId);

  void orderPerfusionSyringePreparation(
      @Nonnull String patientId,
      @Nonnull String compositionUid,
      @Nonnull String ehrOrderName,
      int numberOfSyringes,
      boolean urgent,
      @Nonnull DateTime dueTime,
      boolean printSystemLabel);

  List<PerfusionSyringePatientTasksDto> findPerfusionSyringePreparationRequests(
      Opt<Collection<String>> careProviderIds,
      Opt<Collection<String>> patientIds,
      @Nonnull Set<TaskTypeEnum> taskTypes,
      @Nonnull Locale locale);

  List<PerfusionSyringePatientTasksDto> findFinishedPerfusionSyringePreparationRequests(
      Opt<Collection<String>> careProviderIds,
      Opt<Collection<String>> patientIds,
      @Nonnull Interval searchInterval,
      @Nonnull Locale locale);

  boolean finishedPerfusionSyringeRequestsExistInLastHours(
      @Nonnull String patientId,
      @Nonnull String originalTherapyId,
      int hours);

  Map<String, PerfusionSyringePreparationDto> startPerfusionSyringePreparations(
      String patientId,
      List<String> taskIds,
      Set<String> originalTherapyIds,
      boolean isUrgent,
      Locale locale);

  Map<String, String> confirmPerfusionSyringePreparations(String patientId, List<String> taskIds, boolean isUrgent);

  Map<String, String> dispensePerfusionSyringePreparations(String patientId, List<String> taskIds, boolean isUrgent);

  void deletePerfusionSyringeRequest(String taskId, Locale locale);

  Map<String, String> undoPerfusionSyringeRequestState(String patientId, String taskId, boolean isUrgent);

  void updateTherapySelfAdministeringStatus(
      String patientId,
      String compositionUid,
      SelfAdministeringActionEnum selfAdministeringActionEnum);

  void printPharmacistDispenseTask(
      String patientId,
      String taskId,
      String compositionUid,
      TherapyAssigneeEnum requesterRole,
      SupplyRequestStatus supplyRequestStatusEnum);

  Map<String, String> getTherapiesFormattedDescriptionsMap(String patientId, Set<String> therapyIds, Locale locale);

  Integer getPatientsCurrentBnfMaximumSum(String patientId);

  String saveOutpatientPrescription(String patientId, PrescriptionPackageDto prescriptionPackage);

  void deleteOutpatientPrescription(@Nonnull String patientId, @Nonnull String prescriptionUid);

  void updateOutpatientPrescriptionStatus(
      String patientId,
      String compositionUid,
      String prescriptionTherapyId,
      PrescriptionStatus status);

  PerfusionSyringeTaskSimpleDto getPerfusionSyringeTaskSimpleDto(String taskId, Locale locale);

  void editPerfusionSyringeTask(
      @Nonnull String taskId,
      @Nonnull Integer numberOfSyringes,
      boolean isUrgent,
      @Nonnull DateTime dueDate,
      boolean printSystemLabel);

  TherapyDocumentsDto getTherapyDocuments(String patientId, Integer numberOfResults, Integer resultsOffset, Locale locale);

  String getMedicationExternalId(@Nonnull String externalSystem, @Nonnull Long medicationId);

  Double getRemainingInfusionBagQuantity(@Nonnull DateTime when, @Nonnull String patientId, @Nonnull String therapyId);

  String updateOutpatientPrescription(
      String patientId,
      String prescriptionPackageId,
      String compositionUid,
      List<PrescriptionDto> prescriptionDtoList,
      DateTime when,
      Locale locale);

  TherapyDocumentDto getTherapyDocument(
      String patientId,
      String contentId,
      TherapyDocumentType documentType,
      Locale locale);

  TitrationDto getDataForTitration(
      @Nonnull String patientId,
      @Nonnull String therapyId,
      @Nonnull TitrationType titrationType,
      @Nonnull DateTime searchStart,
      @Nonnull DateTime searchEnd,
      @Nonnull Locale locale);

  List<TherapyRowDto> getTherapiesForCurrentCentralCase(@Nonnull String patientId, @Nonnull Locale localel);

  String getUnlicensedMedicationWarning(@Nonnull Locale locale);

  void setAdministrationDoctorsComment(@Nonnull String taskId, String doctorsComment);

  Long getMedicationIdForBarcode(@Nonnull String barcode);

  BarcodeTaskSearchDto getAdministrationTaskForBarcode(@Nonnull String patientId, @Nonnull String barcode);

  String getOriginalTherapyId(@Nonnull String patientId, @Nonnull String therapyId);
}
