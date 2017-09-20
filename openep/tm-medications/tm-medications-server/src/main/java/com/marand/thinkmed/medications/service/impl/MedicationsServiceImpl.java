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

package com.marand.thinkmed.medications.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StackTraceUtils;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.eventbus.EventProducer;
import com.marand.maf.core.exception.SystemException;
import com.marand.maf.core.exception.UserWarning;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.maf.core.sequence.SequenceGenerator;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.ServiceMethod;
import com.marand.maf.core.service.UserMetadata;
import com.marand.maf.core.service.auditing.Auditing;
import com.marand.maf.core.service.auditing.Level;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationAuthorisationSloveniaCluster.PrescriptionStatus;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.rm.TdoPathable;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkehr.tagging.dto.TagDto;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.api.externals.data.object.ExternalIdentityDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.MedicationPreferences;
import com.marand.thinkmed.medications.MedicationPreferencesUtil;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.TherapyAuthorityEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyTagEnum;
import com.marand.thinkmed.medications.TherapyTaggingUtils;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.AdministrationTaskCreator;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.allergies.AllergiesHandler;
import com.marand.thinkmed.medications.audittrail.TherapyAuditTrailProvider;
import com.marand.thinkmed.medications.b2b.MedicationsConnectorUtils;
import com.marand.thinkmed.medications.barcode.BarcodeTaskFinder;
import com.marand.thinkmed.medications.batch.TherapyBatchActionHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.MedicationsFinder;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.UserPersonDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.CompositionNotFoundException;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.discharge.MedicationOnDischargeHandler;
import com.marand.thinkmed.medications.document.TherapyDocumentProvider;
import com.marand.thinkmed.medications.dto.AdministrationPatientTaskLimitsDto;
import com.marand.thinkmed.medications.dto.AdministrationTaskLimitsDto;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
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
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
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
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import com.marand.thinkmed.medications.dto.pharmacist.review.SupplyDataForPharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyProblemDescriptionEnum;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.SupplyReviewTaskSimpleDto;
import com.marand.thinkmed.medications.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyReportPdfDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import com.marand.thinkmed.medications.dto.supply.MedicationSupplyCandidateDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsActionDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.infusion.InfusionBagHandler;
import com.marand.thinkmed.medications.infusion.impl.InfusionBagTaskProviderImpl;
import com.marand.thinkmed.medications.mentalhealth.MentalHealthFormHandler;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistReviewProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistReviewSaver;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskCreator;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskHandler;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacySupplyProcessHandler;
import com.marand.thinkmed.medications.pharmacist.PreparePerfusionSyringeProcessHandler;
import com.marand.thinkmed.medications.prescription.PrescriptionHandler;
import com.marand.thinkmed.medications.reconciliation.MedicationReconciliationHandler;
import com.marand.thinkmed.medications.report.TherapyReportCreator;
import com.marand.thinkmed.medications.report.TherapyReportDataProvider;
import com.marand.thinkmed.medications.rule.MedicationRuleHandler;
import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import com.marand.thinkmed.medications.task.TasksRescheduler;
import com.marand.thinkmed.medications.therapy.TherapyCacheInvalidator;
import com.marand.thinkmed.medications.therapy.TherapyUpdater;
import com.marand.thinkmed.medications.titration.TitrationDataProvider;
import com.marand.thinkmed.medications.warnings.TherapyWarningsProvider;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsActionHandler;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsDelegator;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.dto.DoseRangeCheckDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationWarningsDto;
import com.marand.thinkmed.medicationsexternal.service.MedicationsExternalService;
import com.marand.thinkmed.patient.PatientDataProvider;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Bostjan Vester
 */
public class MedicationsServiceImpl implements MedicationsService, MedicationsServiceEvents
{
  private MedicationsBo medicationsBo;
  private TherapyUpdater therapyUpdater;
  private MedicationsDao medicationsDao;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsExternalService medicationsExternalService;
  private TherapyCacheInvalidator therapyCacheInvalidator;
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationsTasksHandler medicationsTasksHandler;
  private PharmacistTaskProvider pharmacistTaskProvider;
  private PharmacistTaskCreator pharmacistTaskCreator;
  private PharmacistTaskHandler pharmacistTaskHandler;
  private PharmacySupplyProcessHandler pharmacySupplyProcessHandler;
  private AdministrationTaskCreator administrationTaskCreator;
  private MedicationsFinder medicationsFinder;
  private TaggingOpenEhrDao taggingOpenEhrDao;
  private PharmacistReviewProvider pharmacistReviewProvider;
  private PharmacistReviewSaver pharmacistReviewSaver;
  private TherapyDisplayProvider therapyDisplayProvider;
  private TherapyWarningsProvider therapyWarningsProvider;
  private AdministrationHandler administrationHandler;
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler;
  private MedicationOnDischargeHandler medicationOnDischargeHandler;
  private MedicationReconciliationHandler medicationReconciliationHandler;
  private PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler;
  private AdministrationTaskConverter administrationTaskConverter;
  private OverviewContentProvider overviewContentProvider;
  private MentalHealthFormHandler mentalHealthFormHandler;
  private PrescriptionHandler prescriptionHandler;
  private TherapyDocumentProvider therapyDocumentProvider;
  private MedicationRuleHandler medicationIngredientRuleHandler;
  private TherapyReportDataProvider therapyReportDataProvider;
  private TherapyReportCreator therapyReportCreator;
  private InfusionBagHandler infusionBagHandler;
  private InfusionBagTaskProviderImpl infusionBagTaskProvider;
  private TitrationDataProvider titrationDataProvider;
  private TherapyBatchActionHandler therapyBatchActionHandler;
  private TherapyChangeCalculator therapyChangeCalculator;
  private TherapyAuditTrailProvider therapyAuditTrailProvider;
  private PatientDataProvider patientDataProvider;

  private AdministrationProvider administrationProvider;
  private MedicationsConnector medicationsConnector;
  private SequenceGenerator demoPatientIdGenerator;
  private AllergiesHandler allergiesHandler;
  private AdditionalWarningsDelegator additionalWarningsDelegator;
  private AdditionalWarningsActionHandler additionalWarningsActionHandler;
  private TasksRescheduler tasksRescheduler;
  private BarcodeTaskFinder barcodeTaskFinder;

  //properties
  private boolean medicationsSearchStartMustMatch;
  private Boolean medicationsPharmacistReviewReferBackPreset;
  private Boolean mentalHealthReportEnabled;
  private Boolean autoAdministrationCharting;
  private Boolean medicationsSupplyPresent;
  private Boolean medicationsWitnessRequiredUnderAge;
  private Boolean medicationsWitnessRequired;
  private Boolean medicationsShowHeparinPane;
  private boolean formularyFilterEnabled;
  private Boolean bnfEnabled;
  private Boolean infusionBagEnabled;
  private String prescriptionSystem;
  private boolean doctorReviewEnabled;
  private boolean doseRangeEnabled;

  private static final Logger LOG = LoggerFactory.getLogger(MedicationsServiceImpl.class);

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setTherapyUpdater(final TherapyUpdater therapyUpdater)
  {
    this.therapyUpdater = therapyUpdater;
  }

  @Required
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationsExternalService(final MedicationsExternalService medicationsExternalService)
  {
    this.medicationsExternalService = medicationsExternalService;
  }

  @Required
  public void setMentalHealthFormHandler(final MentalHealthFormHandler mentalHealthFormHandler)
  {
    this.mentalHealthFormHandler = mentalHealthFormHandler;
  }

  @Required
  public void setInfusionBagTaskProvider(final InfusionBagTaskProviderImpl infusionBagTaskProvider)
  {
    this.infusionBagTaskProvider = infusionBagTaskProvider;
  }

  @Required
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Required
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Required
  public void setTherapyCacheInvalidator(final TherapyCacheInvalidator therapyCacheInvalidator)
  {
    this.therapyCacheInvalidator = therapyCacheInvalidator;
  }

  @Required
  public void setPatientDataProvider(final PatientDataProvider patientDataProvider)
  {
    this.patientDataProvider = patientDataProvider;
  }

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Required
  public void setPharmacistTaskProvider(final PharmacistTaskProvider pharmacistTaskProvider)
  {
    this.pharmacistTaskProvider = pharmacistTaskProvider;
  }

  @Required
  public void setPharmacistTaskCreator(final PharmacistTaskCreator pharmacistTaskCreator)
  {
    this.pharmacistTaskCreator = pharmacistTaskCreator;
  }

  @Required
  public void setPharmacistTaskHandler(final PharmacistTaskHandler pharmacistTaskHandler)
  {
    this.pharmacistTaskHandler = pharmacistTaskHandler;
  }

  @Required
  public void setPharmacySupplyProcessHandler(final PharmacySupplyProcessHandler pharmacySupplyProcessHandler)
  {
    this.pharmacySupplyProcessHandler = pharmacySupplyProcessHandler;
  }

  @Required
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Required
  public void setTaggingOpenEhrDao(final TaggingOpenEhrDao taggingOpenEhrDao)
  {
    this.taggingOpenEhrDao = taggingOpenEhrDao;
  }

  @Required
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Required
  public void setInfusionBagHandler(final InfusionBagHandler infusionBagHandler)
  {
    this.infusionBagHandler = infusionBagHandler;
  }

  @Required
  public void setPharmacistReviewProvider(final PharmacistReviewProvider pharmacistReviewProvider)
  {
    this.pharmacistReviewProvider = pharmacistReviewProvider;
  }

  @Required
  public void setPharmacistReviewSaver(final PharmacistReviewSaver pharmacistReviewSaver)
  {
    this.pharmacistReviewSaver = pharmacistReviewSaver;
  }

  @Required
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Required
  public void setMedicationsFinder(final MedicationsFinder medicationsFinder)
  {
    this.medicationsFinder = medicationsFinder;
  }

  @Required
  public void setDemoPatientIdGenerator(final SequenceGenerator demoPatientIdGenerator)
  {
    this.demoPatientIdGenerator = demoPatientIdGenerator;
  }

  @Required
  public void setTherapyWarningsProvider(final TherapyWarningsProvider therapyWarningsProvider)
  {
    this.therapyWarningsProvider = therapyWarningsProvider;
  }

  @Required
  public void setAdministrationHandler(final AdministrationHandler administrationHandler)
  {
    this.administrationHandler = administrationHandler;
  }

  @Required
  public void setMedicationOnAdmissionHandler(final MedicationOnAdmissionHandler medicationOnAdmissionHandler)
  {
    this.medicationOnAdmissionHandler = medicationOnAdmissionHandler;
  }

  @Required
  public void setMedicationOnDischargeHandler(final MedicationOnDischargeHandler medicationOnDischargeHandler)
  {
    this.medicationOnDischargeHandler = medicationOnDischargeHandler;
  }

  @Required
  public void setMedicationIngredientRuleHandler(final MedicationRuleHandler medicationIngredientRuleHandler)
  {
    this.medicationIngredientRuleHandler = medicationIngredientRuleHandler;
  }

  @Required
  public void setMedicationReconciliationHandler(final MedicationReconciliationHandler medicationReconciliationHandler)
  {
    this.medicationReconciliationHandler = medicationReconciliationHandler;
  }

  @Required
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Required
  public void setOverviewContentProvider(final OverviewContentProvider overviewContentProvider)
  {
    this.overviewContentProvider = overviewContentProvider;
  }

  @Required
  public void setPreparePerfusionSyringeProcessHandler(final PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler)
  {
    this.preparePerfusionSyringeProcessHandler = preparePerfusionSyringeProcessHandler;
  }

  @Required
  public void setPrescriptionHandler(final PrescriptionHandler prescriptionHandler)
  {
    this.prescriptionHandler = prescriptionHandler;
  }

  @Required
  public void setTherapyDocumentProvider(final TherapyDocumentProvider therapyDocumentProvider)
  {
    this.therapyDocumentProvider = therapyDocumentProvider;
  }

  @Required
  public void setTherapyReportDataProvider(final TherapyReportDataProvider therapyReportDataProvider)
  {
    this.therapyReportDataProvider = therapyReportDataProvider;
  }

  @Required
  public void setTherapyReportCreator(final TherapyReportCreator therapyReportCreator)
  {
    this.therapyReportCreator = therapyReportCreator;
  }

  @Required
  public void setTitrationDataProvider(final TitrationDataProvider titrationDataProvider)
  {
    this.titrationDataProvider = titrationDataProvider;
  }

  @Required
  public void setTherapyBatchActionHandler(final TherapyBatchActionHandler therapyBatchActionHandler)
  {
    this.therapyBatchActionHandler = therapyBatchActionHandler;
  }

  @Required
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Required
  public void setTherapyAuditTrailProvider(final TherapyAuditTrailProvider therapyAuditTrailProvider)
  {
    this.therapyAuditTrailProvider = therapyAuditTrailProvider;
  }

  @Required
  public void setAllergiesHandler(final AllergiesHandler allergiesHandler)
  {
    this.allergiesHandler = allergiesHandler;
  }

  @Required
  public void setAdditionalWarningsDelegator(final AdditionalWarningsDelegator additionalWarningsDelegator)
  {
    this.additionalWarningsDelegator = additionalWarningsDelegator;
  }

  @Required
  public void setAdditionalWarningsActionHandler(final AdditionalWarningsActionHandler additionalWarningsActionHandler)
  {
    this.additionalWarningsActionHandler = additionalWarningsActionHandler;
  }

  @Required
  public void setTasksRescheduler(final TasksRescheduler tasksRescheduler)
  {
    this.tasksRescheduler = tasksRescheduler;
  }

  @Required
  public void setBarcodeTaskFinder(final BarcodeTaskFinder barcodeTaskFinder)
  {
    this.barcodeTaskFinder = barcodeTaskFinder;
  }

  public void setMedicationsSearchStartMustMatch(final String medicationsSearchStartMustMatch)
  {
    this.medicationsSearchStartMustMatch =
        medicationsSearchStartMustMatch != null ? Boolean.valueOf(medicationsSearchStartMustMatch) : false;
  }

  public void setMedicationsPharmacistReviewReferBackPreset(final Boolean medicationsPharmacistReviewReferBackPreset)
  {
    this.medicationsPharmacistReviewReferBackPreset =
        medicationsPharmacistReviewReferBackPreset != null ?
        medicationsPharmacistReviewReferBackPreset :
        true;
  }

  public void setMentalHealthReportEnabled(final Boolean mentalHealthReportEnabled)
  {
    this.mentalHealthReportEnabled =
        mentalHealthReportEnabled != null ?
        mentalHealthReportEnabled :
        false;
  }

  public void setAutoAdministrationCharting(final Boolean autoAdministrationCharting)
  {
    this.autoAdministrationCharting =
        autoAdministrationCharting != null ?
        autoAdministrationCharting :
        false;
  }

  public void setMedicationsSupplyPresent(final Boolean medicationsSupplyPresent)
  {
    this.medicationsSupplyPresent = medicationsSupplyPresent != null ? medicationsSupplyPresent : false;
  }

  public void setMedicationsWitnessRequiredUnderAge(final Boolean medicationsWitnessRequiredUnderAge)
  {
    this.medicationsWitnessRequiredUnderAge =
        medicationsWitnessRequiredUnderAge != null ?
        medicationsWitnessRequiredUnderAge :
        false;
  }

  public void setMedicationsWitnessRequired(final Boolean medicationsWitnessRequired)
  {
    this.medicationsWitnessRequired = medicationsWitnessRequired != null ? medicationsWitnessRequired : false;
  }

  public void setMedicationsShowHeparinPane(final Boolean medicationsShowHeparinPane)
  {
    this.medicationsShowHeparinPane = medicationsShowHeparinPane != null ? medicationsShowHeparinPane : true;
  }

  @Required
  public void setBnfEnabled(final Boolean bnfEnabled)
  {
    this.bnfEnabled = bnfEnabled != null ? bnfEnabled : false;
  }

  public void setInfusionBagEnabled(final Boolean infusionBagEnabled)
  {
    this.infusionBagEnabled = infusionBagEnabled != null ? infusionBagEnabled : false;
  }

  public void setPrescriptionSystem(final String prescriptionSystem)
  {
    this.prescriptionSystem = prescriptionSystem;
  }

  @Required
  public void setFormularyFilterEnabled(final boolean formularyFilterEnabled)
  {
    this.formularyFilterEnabled = formularyFilterEnabled;
  }

  @Required
  public void setDoctorReviewEnabled(final boolean doctorReviewEnabled)
  {
    this.doctorReviewEnabled = doctorReviewEnabled;
  }

  @Required
  public void setDoseRangeEnabled(final boolean doseRangeEnabled)
  {
    this.doseRangeEnabled = doseRangeEnabled;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public Map<String, Object> getProperties()
  {
    final Map<String, Object> properties = new HashMap<>();
    properties.put("pharmacistReviewReferBackPreset", medicationsPharmacistReviewReferBackPreset);
    properties.put("mentalHealthReportEnabled", mentalHealthReportEnabled);
    properties.put("autoAdministrationCharting", autoAdministrationCharting);
    properties.put("medicationsSupplyPresent", medicationsSupplyPresent);
    properties.put("medicationsWitnessRequiredUnderAge", medicationsWitnessRequiredUnderAge);
    properties.put("medicationsWitnessRequired", medicationsWitnessRequired);
    properties.put("medicationsShowHeparinPane", medicationsShowHeparinPane);
    properties.put("bnfEnabled", bnfEnabled);
    properties.put("infusionBagEnabled", infusionBagEnabled);
    properties.put("outpatientPrescriptionType", prescriptionSystem);
    properties.put("formularyFilterEnabled", formularyFilterEnabled);
    properties.put("doctorReviewEnabled", doctorReviewEnabled);
    properties.put("doseRangeEnabled", doseRangeEnabled);

    return properties;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public UserPersonDto getUserData()
  {
    final Set<String> authoritiesCodes = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

    final Map<String, Boolean> authoritiesMap = Arrays.stream(TherapyAuthorityEnum.values())
        .collect(
            Collectors.toMap(
                TherapyAuthorityEnum::getClientSideName,
                a -> authoritiesCodes.contains("ROLE_" + a.getCode())));

    return RequestContextHolder.getContext().getUserMetadata()
        .map(meta -> new UserPersonDto(meta.getId(), meta.getFullName(), authoritiesMap))
        .get();
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  @EhrSessioned
  public Long getDemoPatientId()
  {
    return demoPatientIdGenerator.generate();
  }

  @Override
  @Transactional()
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_RECORD))
  @EhrSessioned
  public List<TreeNodeData> findMedications(
      @Nonnull final String searchString,
      final String careProviderId,
      @Nonnull final EnumSet<MedicationFinderFilterEnum> filters)
  {
    Preconditions.checkNotNull(searchString, "searchString is required");
    Preconditions.checkNotNull(filters, "filters is required");
    return medicationsFinder.findMedications(searchString, medicationsSearchStartMustMatch, careProviderId, filters);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public MedicationDataDto getMedicationData(final long medicationId, final String careProviderId)
  {
    return medicationsDao.getMedicationData(
        medicationId,
        careProviderId,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  public Map<Long, MedicationDataDto> getMedicationDataMap(@Nonnull final Set<Long> medicationIdsList)
  {
    Preconditions.checkNotNull(medicationIdsList, "medicationIdsList is required");
    return medicationsDao.getMedicationDataMap(
        medicationIdsList,
        null,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  public List<MedicationRouteDto> getMedicationRoutes(final long medicationId)
  {
    return medicationsDao.getMedicationRoutes(medicationId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<DoseRangeCheckDto> findDoseRangeChecks(final long medicationId)
  {
    final String doseRangeChecksProvider = medicationsExternalService.getDoseRangeChecksProvider();
    if (doseRangeChecksProvider != null)
    {
      final String externalId =
          medicationsDao.getMedicationExternalId(
              doseRangeChecksProvider, medicationId, RequestContextHolder.getContext().getRequestTimestamp());
      if (externalId != null)
      {
        return medicationsExternalService.findDoseRangeChecks("FDB", externalId);
      }
    }
    return new ArrayList<>();
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public MedicationWarningsDto findMedicationWarnings(
      final String patientId,
      final long patientAgeInDays,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final boolean isFemale,
      final List<ExternalCatalogDto> patientDiseasesList,
      final List<NamedExternalDto> patientAllergiesList,
      final List<MedicationForWarningsSearchDto> medicationSummaries,
      final Set<WarningSeverity> severityFilterValues)
  {
    return therapyWarningsProvider.findMedicationWarnings(
        patientId,
        patientAgeInDays,
        patientWeightInKg,
        bsaInM2,
        isFemale,
        patientDiseasesList,
        patientAllergiesList,
        medicationSummaries,
        severityFilterValues,
        true,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void newAllergiesAddedForPatient(
      @Nonnull final String patientId,
      @Nonnull final Collection<NamedExternalDto> newAllergies)
  {
    StringUtils.checkNotBlank(patientId, "patientId must be defined");
    Preconditions.checkNotNull(newAllergies, "newAllergies must not be null");

    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    final PatientDataForMedicationsDto patientData = patientDataProvider.getPatientData(patientId, when);

    allergiesHandler.handleNewAllergies(patientId, patientData, newAllergies, when);
  }

  @Override
  public AdditionalWarningsDto getAdditionalWarnings(
      @Nonnull final String patientId,
      @Nonnull final List<AdditionalWarningsType> additionalWarningsTypes,
      @Nonnull final PatientDataForMedicationsDto patientDataForMedications,
      @Nonnull final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId must be defined");
    Preconditions.checkNotNull(additionalWarningsTypes, "additionalWarningsTypes must not be null");
    Preconditions.checkNotNull(patientDataForMedications, "patientDataForMedications must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");

    return additionalWarningsDelegator.getAdditionalWarnings(
        additionalWarningsTypes,
        patientId,
        patientDataForMedications,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale).orElse(null);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void handleAdditionalWarningsAction(@Nonnull final AdditionalWarningsActionDto additionalWarningsActionDto)
  {
    Preconditions.checkNotNull(additionalWarningsActionDto, "additionalWarningsActionDto must not be null");

    additionalWarningsActionHandler.handleAdditionalWarningsAction(additionalWarningsActionDto);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public void tagTherapyForPrescription(
      final String patientId,
      final String compositionId,
      final String centralCaseId,
      final String ehrOrderName)
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
        medicationsOpenEhrDao.getTherapyInstructionPair(patientId, compositionId, ehrOrderName);
    final RmPath rmPath = TdoPathable.pathOfItem(therapyInstructionPair.getFirst(), therapyInstructionPair.getSecond());

    taggingOpenEhrDao.tag(
        compositionId,
        new TagDto(
            TherapyTaggingUtils.generateTag(TherapyTagEnum.PRESCRIPTION, centralCaseId),
            rmPath.getCanonicalString()));
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public void untagTherapyForPrescription(
      final String patientId,
      final String compositionId,
      final String centralCaseId,
      final String ehrOrderName)
  {
    final Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair =
        medicationsOpenEhrDao.getTherapyInstructionPair(patientId, compositionId, ehrOrderName);
    final RmPath rmPath = TdoPathable.pathOfItem(therapyInstructionPair.getFirst(), therapyInstructionPair.getSecond());

    taggingOpenEhrDao.deleteTags(
        compositionId,
        new TagDto(
            TherapyTaggingUtils.generateTag(TherapyTagEnum.PRESCRIPTION, centralCaseId),
            rmPath.getCanonicalString()));
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  @EventProducer(PrescribeTherapy.class)
  public void saveNewMedicationOrder(
      @Nonnull final String patientId,
      @Nonnull final List<SaveMedicationOrderDto> medicationOrders,
      final String centralCaseId,
      final DateTime hospitalizationStart,
      final String careProviderId,
      final NamedExternalDto prescriber,
      final String lastLinkName,
      final DateTime saveDateTime,
      @Nonnull final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(medicationOrders, "medicationOrders is required");
    Preconditions.checkNotNull(locale, "locale is required");

    final DateTime when = saveDateTime != null ? saveDateTime : RequestContextHolder.getContext().getRequestTimestamp();

    therapyUpdater.saveTherapies(
        patientId,
        medicationOrders,
        centralCaseId,
        careProviderId,
        prescriber,
        when,
        locale);

    if (lastLinkName != null)
    {
      final Long patientIdLong = MedicationsConnectorUtils.getId(patientId);
      if (patientIdLong != null)
      {
        medicationsDao.savePatientLastLinkName(patientIdLong, lastLinkName);
      }
    }

    pharmacistTaskHandler.handleReviewTaskOnTherapiesChange(
        patientId,
        hospitalizationStart,
        when,
        prescriber != null ? prescriber.getName() : null,
        when,
        null,
        PharmacistReviewTaskStatusEnum.PENDING);

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<String> saveMedicationsOnAdmission(
      final String patientId,
      final List<MedicationOnAdmissionDto> medicationsOnAdmission,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      @Nullable final DateTime saveDateTime,
      final DateTime hospitalizationStart,
      final Locale locale)
  {
    final DateTime when = saveDateTime != null ? saveDateTime : RequestContextHolder.getContext().getRequestTimestamp();

    final List<String> existingCompositionIds = medicationOnAdmissionHandler.getMedicationsOnAdmissionCompositionIds(
        patientId,
        hospitalizationStart,
        locale);

    for (final MedicationOnAdmissionDto admissionDto : medicationsOnAdmission)
    {
      final String compositionUid = TherapyIdUtils.getCompositionUidWithoutVersion(
          admissionDto.getTherapy()
              .getCompositionUid());

      if (existingCompositionIds.contains(compositionUid))
      {
        existingCompositionIds.remove(compositionUid);
      }
    }

    final List<String> savedCompositionIds = medicationOnAdmissionHandler.saveMedicationsOnAdmission(
        patientId,
        medicationsOnAdmission,
        existingCompositionIds,
        centralCaseId,
        careProviderId,
        RequestContextHolder.getContext().getUserId(),
        prescriber,
        when,
        locale);

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);

    return savedCompositionIds;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public List<MedicationOnAdmissionDto> getMedicationsOnAdmission(
      final String patientId,
      final DateTime hospitalizationStart,
      final Locale locale)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    return medicationOnAdmissionHandler.getMedicationsOnAdmission(patientId, hospitalizationStart, when, locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<String> saveMedicationsOnDischarge(
      final String patientId,
      final List<MedicationOnDischargeDto> medicationsOnDischarge,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      @Nullable final DateTime saveDateTime,
      @Nullable final DateTime hospitalizationStart,
      final Locale locale)
  {
    final DateTime when = saveDateTime != null ? saveDateTime : RequestContextHolder.getContext().getRequestTimestamp();

    final List<String> existingCompositionIds = medicationOnDischargeHandler.getMedicationsOnDischargeIds(
        patientId,
        hospitalizationStart);

    for (final MedicationOnDischargeDto dischargeDto : medicationsOnDischarge)
    {
      final String compositionUid = TherapyIdUtils.getCompositionUidWithoutVersion(
          dischargeDto.getTherapy()
              .getCompositionUid());

      if (existingCompositionIds.contains(compositionUid))
      {
        existingCompositionIds.remove(compositionUid);
      }
    }

    final List<String> savedCompositionIds = medicationOnDischargeHandler.saveMedicationsOnDischarge(
        patientId,
        medicationsOnDischarge,
        existingCompositionIds,
        centralCaseId,
        careProviderId,
        RequestContextHolder.getContext().getUserId(),
        prescriber,
        when,
        locale);

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
    return savedCompositionIds;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<MedicationOnDischargeDto> getMedicationsOnDischarge(
      final String patientId,
      final DateTime hospitalizationDate,
      final DateTime saveDateTime,
      final Locale locale)
  {
    final DateTime when = saveDateTime != null ? saveDateTime : RequestContextHolder.getContext().getRequestTimestamp();
    return medicationOnDischargeHandler.getMedicationsOnDischarge(
        patientId,
        Intervals.infiniteFrom(hospitalizationDate),
        when,
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<ReconciliationRowDto> getReconciliationSummaryGroups(
      final String patientId,
      final DateTime fromDate,
      final DateTime saveDateTime,
      final Locale locale)
  {
    final DateTime when = saveDateTime != null ? saveDateTime : RequestContextHolder.getContext().getRequestTimestamp();
    return medicationReconciliationHandler.getReconciliationGroups(patientId, fromDate, when, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyFlowDto getTherapyFlow(
      final String patientId,
      final String centralCaseId,
      final Double patientHeight,
      final DateTime startDate,
      final int dayCount,
      final Integer todayIndex,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      @Nullable final String careProviderId,
      final Locale locale)
  {
    return overviewContentProvider.getTherapyFlow(
        patientId,
        centralCaseId,
        patientHeight,
        startDate,
        dayCount,
        todayIndex,
        roundsInterval,
        therapySortTypeEnum,
        careProviderId,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final RoundsIntervalDto roundsInterval)
  {
    final DateTime now = RequestContextHolder.getContext().getRequestTimestamp();
    return overviewContentProvider.reloadSingleTherapyAfterAction(
        patientId,
        compositionUid,
        ehrOrderName,
        roundsInterval,
        now);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  @EventProducer(ModifyTherapy.class)
  public void modifyTherapy(
      final String patientId,
      final TherapyDto therapy,
      final TherapyChangeReasonDto changeReasonDto,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final Boolean therapyAlreadyStarted,
      final DateTime saveDateTime,
      final String basedOnPharmacyReviewId,
      final Locale locale)
  {
    final MedicationOrderComposition composition =
        medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, therapy.getCompositionUid());
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, therapy.getEhrOrderName());

    if (medicationsBo.isMedicationTherapyCompleted(composition, instruction))
    {
      throw new IllegalArgumentException("Therapy is finished.");
    }

    final DateTime when = saveDateTime != null ? saveDateTime : RequestContextHolder.getContext().getRequestTimestamp();

    Boolean calculatedTherapyAlreadyStarted = therapyAlreadyStarted;
    if (therapyAlreadyStarted == null)
    {
      final DateTime therapyStart =
          DataValueUtils.getDateTime(instruction.getOrder().get(0).getMedicationTiming().getStartDate());

      calculatedTherapyAlreadyStarted = therapyStart.isBefore(when);
    }

    therapyUpdater.modifyTherapy(
        patientId,
        therapy,
        changeReasonDto,
        centralCaseId,
        careProviderId,
        prescriber,
        calculatedTherapyAlreadyStarted,
        basedOnPharmacyReviewId,
        when,
        locale);

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void abortTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final TherapyChangeReasonDto changeReasonDto)
  {
    final MedicationOrderComposition composition =
        medicationsOpenEhrDao.loadMedicationOrderComposition(patientId, compositionUid);
    final MedicationInstructionInstruction instruction =
        MedicationsEhrUtils.getMedicationInstructionByEhrName(composition, ehrOrderName);

    if (medicationsBo.isMedicationTherapyCompleted(composition, instruction))
    {
      throw new IllegalArgumentException("Therapy is finished.");
    }

    therapyUpdater.abortTherapy(
        patientId,
        compositionUid,
        ehrOrderName,
        changeReasonDto,
        RequestContextHolder.getContext().getRequestTimestamp());

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  @EventProducer(AbortTherapy.class)
  public void abortAllTherapiesForPatient(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    therapyBatchActionHandler.abortAllTherapies(patientId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void reviewTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName)
  {
    therapyUpdater.reviewTherapy(patientId, compositionUid);
    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void suspendTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final TherapyChangeReasonDto changeReason)
  {
    therapyUpdater.suspendTherapy(
        patientId,
        compositionUid,
        ehrOrderName,
        changeReason,
        RequestContextHolder.getContext().getRequestTimestamp());
    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void reissueTherapy(final String patientId, final String compositionUid, final String ehrOrderName)
  {
    therapyUpdater.reissueTherapy(
        patientId,
        compositionUid,
        ehrOrderName,
        RequestContextHolder.getContext().getRequestTimestamp());

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  @EventProducer(CreateAdministration.class)
  public void createAdditionalAdministrationTask(
      final String therapyCompositionUid,
      final String ehrOrderName,
      final String patientId,
      final StartAdministrationDto administrationDto)
  {
    final MedicationOrderComposition composition = medicationsOpenEhrDao.getTherapyInstructionPair(
        patientId,
        therapyCompositionUid,
        ehrOrderName).getFirst();

    therapyUpdater.createAdditionalAdministrationTask(
        composition,
        patientId,
        administrationDto.getPlannedTime(),
        AdministrationTypeEnum.START,
        administrationDto.getPlannedDose());

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  @EventProducer(SuspendTherapy.class)
  public void suspendAllTherapies(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    therapyBatchActionHandler.suspendAllTherapies(patientId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  @EventProducer(SuspendTherapy.class)
  public void suspendAllTherapiesOnTemporaryLeave(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    therapyBatchActionHandler.suspendAllTherapiesOnTemporaryLeave(
        patientId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void reissueAllTherapiesOnReturnFromTemporaryLeave(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    therapyBatchActionHandler.reissueAllTherapiesOnReturnFromTemporaryLeave(
        patientId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public List<MedicationForWarningsSearchDto> getTherapiesForWarningsSearch(final String patientId)
  {
    return medicationsBo.getTherapiesForWarningsSearch(patientId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public PatientDataForMedicationsDto getPatientData(final String patientId)
  {
    final PatientDataForMedicationsDto patientData =
        patientDataProvider.getPatientData(patientId, RequestContextHolder.getContext().getRequestTimestamp());
    return patientData;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyViewPatientDto getTherapyViewPatientData(@Nonnull final String patientId)
  {
    StringUtils.checkNotBlank(patientId, "patientId required");
    final DateTime requestTimestamp = RequestContextHolder.getContext().getRequestTimestamp();

    final TherapyViewPatientDto therapyViewPatientDto = new TherapyViewPatientDto();

    final PatientDataForMedicationsDto patientData = patientDataProvider.getPatientData(patientId, requestTimestamp);
    therapyViewPatientDto.setPatientData(patientData);

    final MedicationsCentralCaseDto centralCaseDto = patientData.getCentralCaseDto();

    final String careProviderId =
        centralCaseDto != null && centralCaseDto.getCareProvider() != null ? centralCaseDto.getCareProvider().getId() : null;

    final boolean inpatient = centralCaseDto != null && !centralCaseDto.isOutpatient();
    final Interval referenceWeightSearchInterval;
    if (inpatient)
    {
      referenceWeightSearchInterval = new Interval(centralCaseDto.getCentralCaseEffective().getStart(), requestTimestamp);
      final Interval recentHospitalizationInterval = new Interval(requestTimestamp.minusHours(12), requestTimestamp);
      therapyViewPatientDto.setRecentHospitalization(
          recentHospitalizationInterval.contains(centralCaseDto.getCentralCaseEffective().getStart()));
    }
    else
    {
      referenceWeightSearchInterval = new Interval(requestTimestamp.minusHours(24), requestTimestamp);
    }

    final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, referenceWeightSearchInterval);
    therapyViewPatientDto.setReferenceWeight(referenceWeight);

    if (careProviderId != null)
    {
      final List<String> customGroups = medicationsDao.getCustomGroupNames(careProviderId);
      therapyViewPatientDto.setCustomGroups(customGroups);
    }

    final AdministrationTimingDto administrationTiming = MedicationPreferencesUtil.getAdministrationTiming(careProviderId);
    therapyViewPatientDto.setAdministrationTiming(administrationTiming);

    final RoundsIntervalDto roundsInterval = MedicationPreferencesUtil.getRoundsInterval(careProviderId);
    therapyViewPatientDto.setRoundsInterval(roundsInterval);

    final Long patientIdLong = MedicationsConnectorUtils.getId(patientId);
    if (patientIdLong != null)
    {
      final String lastLinkName = medicationsDao.getPatientLastLinkName(patientIdLong);
      therapyViewPatientDto.setLastLinkName(lastLinkName);
    }

    return therapyViewPatientDto;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public byte[] getMedicationDocument(final String reference)
  {
    return medicationsConnector.getPdfDocument(reference);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public byte[] getTherapyReport(final String patientId, final Locale locale)
  {
    final TherapyDayReportDto therapyReportData =
        therapyReportDataProvider.getTherapyReportData(
            patientId, locale, RequestContextHolder.getContext().getRequestTimestamp());

    final TherapyReportPdfDto pdfReport =
        therapyReportCreator.createPdfReport(
            patientId, RequestContextHolder.getContext().getUsername(), therapyReportData, locale);

    return pdfReport != null ? pdfReport.getData() : null;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<Interval> getPatientBaselineInfusionIntervals(final String patientId)
  {
    return medicationsOpenEhrDao.getPatientBaselineInfusionIntervals(
        patientId, Intervals.infiniteFrom(RequestContextHolder.getContext().getRequestTimestamp()));
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  @EhrSessioned
  public String getTherapyReportDataJson(
      @Nonnull final String patientId,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");

    final TherapyDayReportDto reportData = therapyReportDataProvider.getTherapyReportData(
        patientId,
        locale,
        RequestContextHolder.getContext().getRequestTimestamp());

    return JsonUtil.toJson(reportData);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapySurgeryReportDto getTherapySurgeryReportData(
      final String patientId,
      final Double patientHeight,
      final RoundsIntervalDto roundsInterval,
      final Locale locale,
      final DateTime when)
  {
    return therapyReportDataProvider.getTherapySurgeryReportData(patientId, patientHeight, roundsInterval, locale, when);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public String getTherapyFormattedDisplay(final String patientId, final String therapyId, final Locale locale)
  {
    return medicationsBo.getTherapyFormattedDisplay(
        patientId, therapyId, RequestContextHolder.getContext().getRequestTimestamp(), locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyAuditTrailDto getTherapyAuditTrail(
      @Nonnull final String patientId,
      @Nonnull final String compositionId,
      @Nonnull final String ehrOrderName,
      final Double patientHeight,
      @Nonnull final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    StringUtils.checkNotBlank(compositionId, "compositionId is required");
    StringUtils.checkNotBlank(ehrOrderName, "ehrOrderName is required");
    Preconditions.checkNotNull(locale, "locale is required");

    return therapyAuditTrailProvider.getTherapyAuditTrail(
        patientId,
        compositionId,
        ehrOrderName,
        patientHeight,
        locale,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyDto fillTherapyDisplayValues(final TherapyDto therapy, final Locale locale)
  {
    therapyDisplayProvider.fillDisplayValues(therapy, true, true, locale);
    return therapy;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyDto getTherapyDto(final String patientId, final String therapyId, final Locale locale)
  {
    final Pair<String, String> therapyIdPair = TherapyIdUtils.parseTherapyId(therapyId);
    return medicationsBo.getTherapy(
        patientId,
        therapyIdPair.getFirst(),
        therapyIdPair.getSecond(),
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  @EhrSessioned
  public PharmacistReviewTherapyDto fillPharmacistReviewTherapyOnEdit(
      final TherapyDto originalTherapy,
      final TherapyDto changedTherapy,
      final Locale locale)
  {
    final PharmacistReviewTherapyDto pharmacistReviewTherapyDto = new PharmacistReviewTherapyDto();
    therapyDisplayProvider.fillDisplayValues(changedTherapy, true, true, locale);
    pharmacistReviewTherapyDto.setTherapy(changedTherapy);
    pharmacistReviewTherapyDto.setChangeType(PharmacistTherapyChangeType.EDIT);
    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(originalTherapy, changedTherapy, false, locale);
    pharmacistReviewTherapyDto.setChanges(changes);
    return pharmacistReviewTherapyDto;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<TreeNodeData> findSimilarMedications(final long medicationId, @Nonnull final List<Long> routeIds)
  {
    Preconditions.checkNotNull(routeIds, "routeIds must not be null");

    return medicationsFinder.findSimilarMedications(
        medicationId,
        routeIds,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<MedicationDto> findMedicationProducts(final long medicationId, @Nonnull final List<Long> routeIds)
  {
    Preconditions.checkNotNull(routeIds, "routeIds must not be null");

    return medicationsFinder.findMedicationProducts(
        medicationId,
        routeIds,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<MedicationSupplyCandidateDto> getMedicationSupplyCandidates(final long medicationId, final long routeId)
  {
    return medicationsDao.getMedicationSupplyCandidates(
        medicationId, routeId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public List<MedicationRouteDto> getRoutes()
  {
    return medicationsDao.getRoutes(RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public List<DoseFormDto> getDoseForms()
  {
    return medicationsDao.getDoseForms(RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_PAYLOAD))
  @EhrSessioned
  public List<String> getMedicationBasicUnits()
  {
    return medicationsDao.getMedicationBasicUnits();
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void savePatientReferenceWeight(final String patientId, final double weight)
  {
    final MedicationReferenceWeightComposition comp = medicationsBo.buildReferenceWeightComposition(
        weight,
        RequestContextHolder.getContext().getRequestTimestamp());
    medicationsOpenEhrDao.savePatientReferenceWeight(patientId, comp);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void saveConsecutiveDays(
      final String patientId, final String compositionUid, final String ehrOrderName, final Integer consecutiveDays)
  {
    therapyUpdater.saveConsecutiveDays(
        patientId,
        compositionUid,
        ehrOrderName,
        RequestContextHolder.getContext().getUserId(),
        consecutiveDays);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<TherapyDto> getLastTherapiesForPreviousHospitalization(
      final String patientId,
      final Double patientHeight,
      final Locale locale)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    final Interval lastDischargedCentralCaseInterval = medicationsConnector.getLastDischargedCentralCaseEffectiveInterval(
        patientId);
    if (lastDischargedCentralCaseInterval != null)
    {
      final Interval lastDayBeforeDischarge =
          new Interval(
              lastDischargedCentralCaseInterval.getEnd().minusHours(24),
              lastDischargedCentralCaseInterval.getEnd().plusMinutes(1));

      final Interval referenceWeightInterval = Intervals.infiniteTo(when);
      final Double referenceWeight =
          medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, referenceWeightInterval);

      //??
      //final Double height = patientDataForMedicationsProvider.getPatientLastHeight(patientId, new Interval(Intervals.INFINITE.getStart(), when));
      final List<TherapyDto> therapies = medicationsBo.getTherapies(
          patientId,
          lastDayBeforeDischarge,
          null,
          patientHeight,
          null,
          when);

      fillDisplayValuesAndInfusionRateForTherapies(patientHeight, locale, referenceWeight, therapies);

      return therapies;
    }
    return new ArrayList<>();
  }

  private void fillDisplayValuesAndInfusionRateForTherapies(
      final Double patientHeight,
      final Locale locale,
      final Double referenceWeight,
      final List<TherapyDto> therapies)
  {
    for (final TherapyDto therapyDto : therapies)
    {
      if (therapyDto instanceof ComplexTherapyDto && referenceWeight != null)
      {
        if (((ComplexTherapyDto)therapyDto).isContinuousInfusion())
        {
          medicationsBo.fillInfusionRateFromFormula((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight);
        }
        else
        {
          medicationsBo.fillInfusionFormulaFromRate((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight);
        }
      }
      therapyDisplayProvider.fillDisplayValues(therapyDto, true, true, locale);
    }
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<MedicationOnAdmissionGroupDto> getTherapiesOnAdmissionGroups(
      final String patientId,
      final DateTime currentHospitalizationStart,
      @Nullable final Locale locale)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(currentHospitalizationStart, "currentHospitalizationStart must not be null");

    return medicationOnAdmissionHandler.getTherapiesOnAdmissionGroups(
        patientId,
        currentHospitalizationStart,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<MedicationOnDischargeGroupDto> getTherapiesOnDischargeGroups(
      final String patientId,
      final Double patientHeight,
      @Nullable final DateTime saveDateTime,
      final DateTime lastHospitalizationStart,
      final boolean hospitalizationActive,
      @Nullable final Locale locale)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(lastHospitalizationStart, "lastHospitalizationStart must not be null");

    final DateTime when = saveDateTime == null ? RequestContextHolder.getContext().getRequestTimestamp() : saveDateTime;
    final Interval referenceWeightInterval = Intervals.infiniteTo(when);

    if (hospitalizationActive)
    {
      final Interval searchInterval = Intervals.infiniteFrom(when);
      final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, referenceWeightInterval);

      return medicationsBo.getMedicationOnDischargeGroups(
          patientId,
          lastHospitalizationStart,
          searchInterval,
          referenceWeight,
          patientHeight,
          locale,
          when);
    }
    else
    {
      final Interval lastDischargedCentralCaseInterval =
          medicationsConnector.getLastDischargedCentralCaseEffectiveInterval(patientId);

      final Interval lastHourBeforeDischarge =
          lastDischargedCentralCaseInterval == null ?
          null :
          new Interval(
              lastDischargedCentralCaseInterval.getEnd().minusHours(1),
              lastDischargedCentralCaseInterval.getEnd().plusMinutes(1));

      final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, referenceWeightInterval);

      return medicationsBo.getMedicationOnDischargeGroups(
          patientId,
          lastHospitalizationStart,
          lastHourBeforeDischarge,
          referenceWeight,
          patientHeight,
          locale,
          when);
    }
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<MentalHealthTherapyDto> getCurrentHospitalizationMentalHealthTherapies(
      final String patientId,
      final DateTime hospitalizationStart,
      @Nullable final Locale locale)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(hospitalizationStart, "hospitalizationStart must not be null");

    return medicationsBo.getMentalHealthTherapies(
        patientId,
        Intervals.infiniteFrom(hospitalizationStart),
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<TherapyDto> getLinkTherapyCandidates(
      @Nullable final String patientId,
      @Nullable final Double referenceWeight,
      final Double patientHeight,
      @Nullable final Locale locale)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(referenceWeight, "referenceWeight must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");

    return medicationsBo.getLinkTherapyCandidates(
        patientId,
        referenceWeight,
        patientHeight,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public TherapyTemplatesDto getTherapyTemplates(
      @Nonnull final String patientId,
      @Nonnull final TherapyTemplateModeEnum templateMode,
      final String careProviderId,
      final Double referenceWeight,
      final Double patientHeight,
      @Nonnull final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(templateMode, "templateMode is required");
    Preconditions.checkNotNull(locale, "locale is required");

    final TherapyTemplatesDto therapyTemplates = medicationsDao.getTherapyTemplates(
        patientId,
        String.valueOf(RequestContextHolder.getContext().getUserId()),
        templateMode,
        careProviderId,
        referenceWeight,
        patientHeight,
        locale);
    medicationsBo.sortTherapyTemplates(therapyTemplates);
    return therapyTemplates;
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public long saveTherapyTemplate(final TherapyTemplateDto therapyTemplate, final TherapyTemplateModeEnum templateMode)
  {
    return medicationsDao.saveTherapyTemplate(
        therapyTemplate,
        templateMode,
        String.valueOf(RequestContextHolder.getContext().getUserId()));
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public void deleteTherapyTemplate(final long templateId)
  {
    medicationsDao.deleteTherapyTemplate(templateId);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public TherapyTimelineDto getTherapyTimeline(
      @Nonnull final String patientId,
      @Nonnull final Interval interval,
      @Nonnull final TherapySortTypeEnum sortTypeEnum,
      final boolean hidePastTherapies,
      final boolean hideFutureTherapies,
      @Nonnull final PatientDataForMedicationsDto patientData,
      final RoundsIntervalDto roundsInterval,
      final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId must be defined!");
    Preconditions.checkNotNull(interval, "interval must not be null!");
    Preconditions.checkNotNull(sortTypeEnum, "sortTypeEnum must not be null!");
    Preconditions.checkNotNull(patientData, "patientData must not be null!");

    final Interval therapiesSearchInterval =
        hideFutureTherapies ? interval : Intervals.infiniteFrom(new DateTime(interval.getStartMillis()));

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, therapiesSearchInterval, null);

    final List<AdministrationDto> administrations = administrationProvider.getTherapiesAdministrations(
        patientId,
        instructionPairs,
        null);

    final List<String> therapyIds = instructionPairs
        .stream()
        .map(instructionPair -> TherapyIdUtils.createTherapyId(instructionPair.getFirst(), instructionPair.getSecond()))
        .collect(Collectors.toList());

    final List<AdministrationTaskDto> tasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        therapyIds,
        interval,
        true);

    tasks.addAll(infusionBagTaskProvider.findInfusionBagTasks(patientId, therapyIds, interval));

    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    return overviewContentProvider.getTherapyTimeline(
        patientId,
        administrations,
        tasks,
        instructionPairs,
        sortTypeEnum,
        hidePastTherapies,
        patientData,
        interval,
        roundsInterval,
        locale,
        when);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public TherapyTimelineDto getPharmacistTimeline(
      @Nonnull final String patientId,
      @Nonnull final Interval interval,
      @Nonnull final TherapySortTypeEnum sortTypeEnum,
      final boolean hidePastTherapies,
      @Nonnull final PatientDataForMedicationsDto patientData,
      final RoundsIntervalDto roundsInterval,
      final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId must be defined!");
    Preconditions.checkNotNull(interval, "interval must not be null!");
    Preconditions.checkNotNull(sortTypeEnum, "sortTypeEnum must not be null!");
    Preconditions.checkNotNull(patientData, "patientData must not be null!");

    final Interval intervalToInfinity = Intervals.infiniteFrom(new DateTime(interval.getStartMillis()));

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, intervalToInfinity, null);

    final List<String> therapyIds = instructionPairs
        .stream()
        .map(instructionPair -> TherapyIdUtils.createTherapyId(instructionPair.getFirst(), instructionPair.getSecond()))
        .collect(Collectors.toList());

    final List<AdministrationTaskDto> tasks =
        medicationsTasksProvider.findAdministrationTasks(patientId, therapyIds, null, null, null, false)
            .stream()
            .map(taskDto -> administrationTaskConverter.convertTaskToAdministrationTask(taskDto))
            .collect(Collectors.toList());

    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    return overviewContentProvider.getTherapyTimeline(
        patientId,
        Collections.emptyList(),
        tasks,
        instructionPairs,
        sortTypeEnum,
        hidePastTherapies,
        patientData,
        null,
        roundsInterval,
        locale,
        when);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public DocumentationTherapiesDto getTherapyDataForDocumentation(
      final String patientId,
      final Interval centralCaseEffective,
      final String centralCaseId,
      final boolean isOutpatient,
      final Locale locale)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, null, centralCaseId);

    return medicationsBo.findTherapyGroupsForDocumentation(
        patientId,
        centralCaseId,
        centralCaseEffective,
        instructionPairs,
        isOutpatient,
        when,
        locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  @EventProducer(ConfirmAdministration.class)
  public void confirmTherapyAdministration(
      final String therapyCompositionUid,
      final String ehrOrderName,
      final String patientId,
      final AdministrationDto administrationDto,
      final boolean editMode,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final boolean requestSupply,
      final Locale locale)
  {
    administrationHandler.confirmTherapyAdministration(
        therapyCompositionUid,
        ehrOrderName,
        patientId,
        RequestContextHolder.getContext().getUserId(),
        administrationDto,
        editMode,
        requestSupply,
        centralCaseId,
        careProviderId,
        locale,
        RequestContextHolder.getContext().getRequestTimestamp()
    );
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EventProducer(RescheduleTasks.class)
  public void rescheduleTasks(
      @Nonnull final String patientId,
      @Nonnull final String taskId,
      @Nonnull final DateTime newTime,
      @Nonnull final String therapyId)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null!");
    Preconditions.checkNotNull(taskId, "taskId must not be null!");
    Preconditions.checkNotNull(newTime, "newTime must not be null!");
    Preconditions.checkNotNull(therapyId, "therapyId must not be null!");

    tasksRescheduler.rescheduleTasks(patientId, taskId, newTime);
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EventProducer(RescheduleTask.class)
  public void rescheduleTask(
      @Nonnull final String patientId,
      @Nonnull final String taskId,
      @Nonnull final DateTime newTime,
      @Nonnull final String therapyId)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null!");
    Preconditions.checkNotNull(taskId, "taskId must not be null!");
    Preconditions.checkNotNull(newTime, "newTime must not be null!");
    Preconditions.checkNotNull(therapyId, "therapyId must not be null!");

    tasksRescheduler.rescheduleTask(patientId, taskId, newTime);
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void setAdministrationTitratedDose(
      @Nonnull final String patientId,
      @Nonnull final String latestTherapyId,
      @Nonnull final StartAdministrationDto administrationDto,
      final boolean confirmAdministration,
      final String centralCaseId,
      final String careProviderId,
      final DateTime until,
      @Nonnull final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    StringUtils.checkNotBlank(latestTherapyId, "latestTherapyId is required");
    Preconditions.checkNotNull(administrationDto, "administrationDto is required");
    Preconditions.checkNotNull(locale, "locale is required");

    medicationsTasksHandler.setAdministrationTitratedDose(
        patientId,
        latestTherapyId,
        administrationDto.getTaskId(),
        administrationDto.getPlannedDose(),
        administrationDto.getDoctorsComment(),
        administrationDto.getPlannedTime(),
        until);

    if (confirmAdministration)
    {
      final Pair<String, String> therapyIdPair = TherapyIdUtils.parseTherapyId(administrationDto.getTherapyId());
      administrationHandler.confirmTherapyAdministration(
          therapyIdPair.getFirst(),
          therapyIdPair.getSecond(),
          patientId,
          RequestContextHolder.getContext().getUserId(),
          administrationDto,
          false,
          false,
          centralCaseId,
          careProviderId,
          locale,
          RequestContextHolder.getContext().getRequestTimestamp()
      );
    }
    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void setDoctorConfirmationResult(final String taskId, final Boolean result)
  {
    medicationsTasksHandler.setDoctorConfirmationResult(taskId, result);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void deleteTask(
      final String patientId,
      final String taskId,
      final String groupUUId,
      final String therapyId,
      final String comment)
  {
    Preconditions.checkNotNull(patientId, "patientId");
    Preconditions.checkNotNull(taskId, "taskId");

    if (groupUUId != null)
    {
      Preconditions.checkNotNull(therapyId, "therapyId");
      medicationsTasksHandler.deleteAdministrationTasks(
          patientId,
          therapyId,
          groupUUId,
          Arrays.asList(AdministrationTypeEnum.values()));
    }
    else
    {
      medicationsTasksHandler.deleteTask(taskId, comment);
    }

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void deletePatientTasksOfTypes(
      final String patientId,
      final Set<TaskTypeEnum> taskTypes)
  {
    medicationsTasksHandler.deletePatientTasksOfType(
        patientId,
        taskTypes,
        RequestContextHolder.getContext().getUserId());

    therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
  }

  @Override
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  @Transactional
  @EventProducer(DeleteAdministration.class)
  public void deleteAdministration(
      final String patientId,
      final AdministrationDto administration,
      final TherapyDoseTypeEnum therapyDoseType,
      final String therapyId,
      final String comment)
  {
    administrationHandler.deleteAdministration(patientId, administration, therapyDoseType, therapyId, comment);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public List<NamedExternalDto> getCareProfessionals()
  {
    //only in use for DRP, implement when required
    return new ArrayList<>();
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_OUTPUT_RESULTS))
  @EhrSessioned
  public boolean assertPasswordForUsername(final String username, final String password)
  {
    return medicationsConnector.assertPasswordForUsername(username, password);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.WITHOUT_INPUT_OUTPUT_RECORD))
  public AdministrationTaskLimitsDto getAdministrationTaskLimits(@Nullable final String careProviderId)
  {
    return MedicationPreferencesUtil.getAdministrationTaskLimitsPreference(careProviderId);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public List<PatientTaskDto> getPharmacistReviewTasks(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds)
  {
    final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap =
        getPatientDisplayWithLocationDtoMap(careProviderIds, patientIds);

    final Set<TaskTypeEnum> taskTypes = EnumSet.of(
        TaskTypeEnum.PHARMACIST_REMINDER,
        TaskTypeEnum.PHARMACIST_REVIEW);

    return pharmacistTaskProvider.findPharmacistTasks(null, patientIdAndPatientWithLocationMap, taskTypes);
  }

  private Map<String, PatientDisplayWithLocationDto> getPatientDisplayWithLocationDtoMap(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds)
  {
    Preconditions.checkArgument(
        Boolean.logicalXor(careProviderIds.isPresent(), patientIds.isPresent()),
        "Exactly one, careProviderIds or patientIds must be present");

    if (careProviderIds.isPresent())
    {
      final Collection<String> searchCareProviders;
      if (careProviderIds.get().isEmpty())
      {
        searchCareProviders = getCurrentUserCareProviders().stream()
            .map(ExternalIdentityDto::getId)
            .collect(Collectors.toSet());
      }
      else
      {
        searchCareProviders = careProviderIds.get();
      }
      if (searchCareProviders.isEmpty())
      {
        return new HashMap<>();
      }
      return patientDataProvider.getPatientDisplayWithLocationMap(searchCareProviders, null);
    }
    if (patientIds.get().isEmpty())
    {
      return new HashMap<>();
    }
    return patientDataProvider.getPatientDisplayWithLocationMap(null, patientIds.get());
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<AdministrationPatientTaskDto> getAdministrationTasks(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(locale, "locale is required");

    final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap =
        getPatientDisplayWithLocationDtoMap(careProviderIds, patientIds);

    final AdministrationPatientTaskLimitsDto administrationLimits =
        MedicationPreferencesUtil.getAdministrationPatientTaskLimitsPreference();

    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();

    final Interval searchInterval = new Interval(
        when.minusMinutes(administrationLimits.getDueTaskOffset()),
        when.plusMinutes(administrationLimits.getFutureTaskOffset()));

    return medicationsTasksProvider.findAdministrationTasks(
        patientIdAndPatientWithLocationMap,
        searchInterval,
        administrationLimits.getMaxNumberOfTasks(),
        locale,
        when);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public RuleResult applyMedicationRule(
      @Nonnull final RuleParameters ruleParameters,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(ruleParameters, "ruleParameters must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");

    final DateTime requestTimestamp = RequestContextHolder.getContext().getRequestTimestamp();
    return medicationIngredientRuleHandler.applyMedicationRule(ruleParameters, requestTimestamp, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public List<MedicationSupplyTaskDto> findSupplyTasks(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds,
      final Set<TaskTypeEnum> taskTypes,
      final boolean closedTasksOnly,
      final boolean includeUnverifiedDispenseTasks,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(locale, "locale is required");

    final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap =
        getPatientDisplayWithLocationDtoMap(careProviderIds, patientIds);

    return pharmacistTaskProvider.findSupplyTasks(
        null,
        patientIdAndPatientWithLocationMap,
        taskTypes,
        closedTasksOnly,
        includeUnverifiedDispenseTasks,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EventProducer(SavePharmacistReview.class)
  public String savePharmacistReview(
      final String patientId,
      final PharmacistReviewDto pharmacistReview,
      final Boolean authorize,
      final Locale locale)
  {
    final String compositionUid =
        pharmacistReviewSaver.savePharmacistReview(patientId, pharmacistReview, authorize, locale);

    if (pharmacistReview.getReminderDate() != null)
    {
      pharmacistTaskCreator.createPharmacistReminderTask(
          patientId,
          compositionUid,
          pharmacistReview.getReminderDate(),
          pharmacistReview.getReminderNote(),
          locale);
    }
    if (!pharmacistReview.getRelatedTherapies().isEmpty()
        && pharmacistReview.getMedicationSupplyTypeEnum() != null
        && pharmacistReview.getDaysSupply() != null)
    {
      pharmacySupplyProcessHandler.handleSupplyRequest(
          patientId,
          TherapyAssigneeEnum.PHARMACIST,
          pharmacistReview.getRelatedTherapies().get(0).getTherapy().getCompositionUid(),
          pharmacistReview.getRelatedTherapies().get(0).getTherapy().getEhrOrderName(),
          pharmacistReview.getDaysSupply(),
          pharmacistReview.getMedicationSupplyTypeEnum());
    }

    return compositionUid;
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void handleNurseResupplyRequest(
      final String patientId,
      final String therapyCompositionUid,
      final String ehrOrderName,
      final Locale locale)
  {
    try
    {
      pharmacySupplyProcessHandler.handleSupplyRequest(
          patientId,
          TherapyAssigneeEnum.NURSE,
          therapyCompositionUid,
          ehrOrderName,
          null,
          null);
    }
    catch (final IllegalStateException ise)
    {
      throw new UserWarning(Dictionary.getEntry("nurse.resupply.request.already.exists.warning", locale));
    }
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EventProducer(ReviewPharmacistReview.class)
  public void reviewPharmacistReview(
      final String patientId,
      final String pharmacistReviewUid,
      final ReviewPharmacistReviewAction reviewAction,
      final TherapyDto modifiedTherapy,
      final List<String> deniedReviews,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final Locale locale)
  {
    final DateTime requestTimestamp = RequestContextHolder.getContext().getRequestTimestamp();
    pharmacistReviewSaver.reviewPharmacistReview(
        patientId,
        pharmacistReviewUid,
        reviewAction,
        deniedReviews,
        requestTimestamp,
        locale);

    if (reviewAction == ReviewPharmacistReviewAction.MODIFIED)
    {
      modifyTherapy(
          patientId,
          modifiedTherapy,
          null,
          centralCaseId,
          careProviderId,
          prescriber,
          null,
          requestTimestamp,
          null,
          locale);
    }
    else if (reviewAction == ReviewPharmacistReviewAction.ABORTED)
    {
      abortTherapy(
          patientId,
          TherapyIdUtils.getCompositionUidWithoutVersion(modifiedTherapy.getCompositionUid()),
          modifiedTherapy.getEhrOrderName(),
          null);
    }
    else if (reviewAction == ReviewPharmacistReviewAction.REISSUED)
    {
      reissueTherapy(
          patientId,
          TherapyIdUtils.getCompositionUidWithoutVersion(modifiedTherapy.getCompositionUid()),
          modifiedTherapy.getEhrOrderName());
    }
    else if (reviewAction == ReviewPharmacistReviewAction.COPIED)
    {
      final List<SaveMedicationOrderDto> medicationOrders = new ArrayList<>();
      final SaveMedicationOrderDto saveMedicationOrderDto = new SaveMedicationOrderDto();
      saveMedicationOrderDto.setTherapy(modifiedTherapy);
      medicationOrders.add(saveMedicationOrderDto);

      saveNewMedicationOrder(
          patientId,
          medicationOrders,
          centralCaseId,
          null,
          careProviderId,
          prescriber,
          null,
          requestTimestamp,
          locale);
    }
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public Map<TherapyProblemDescriptionEnum, List<NamedExternalDto>> getProblemDescriptionNamedIdentities(final Locale locale)
  {
    final Map<String, TherapyProblemDescriptionEnum> pathKeyMap = new HashMap<>();
    for (final TherapyProblemDescriptionEnum problemDescriptionEnum : TherapyProblemDescriptionEnum.values())
    {
      pathKeyMap.put(problemDescriptionEnum.getPath(), problemDescriptionEnum);
    }

    final Map<String, List<NamedExternalDto>> termsMap =
        medicationsOpenEhrDao.getTemplateTerms("OPENEP - Pharmacy Review Report.v0", pathKeyMap.keySet(), locale);

    final Map<TherapyProblemDescriptionEnum, List<NamedExternalDto>> problemDescriptionNamedIdentities = new HashMap<>();
    for (final String path : termsMap.keySet())
    {
      problemDescriptionNamedIdentities.put(pathKeyMap.get(path), termsMap.get(path));
    }

    return problemDescriptionNamedIdentities;
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public PharmacistReviewsDto getPharmacistReviews(
      final String patientId, final DateTime fromDate, final Locale locale)
  {
    return pharmacistReviewProvider.getPharmacistReviews(patientId, fromDate, locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public List<PharmacistReviewDto> getPharmacistReviewsForTherapy(
      final String patientId, final String compositionUid, final Locale locale)
  {
    return pharmacistReviewProvider.getPharmacistReviewsForTherapy(patientId, compositionUid, locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void authorizePharmacistReviews(
      final String patientId,
      final List<String> pharmacistReviewUids,
      final Locale locale)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    pharmacistReviewSaver.authorizePatientPharmacistReviews(patientId, pharmacistReviewUids, locale, when);
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void deletePharmacistReview(final String patientId, final String pharmacistReviewUid)
  {
    medicationsOpenEhrDao.deleteComposition(patientId, pharmacistReviewUid);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public List<NamedExternalDto> getCurrentUserCareProviders()
  {
    return medicationsConnector.getCurrentUserCareProviders();
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public List<AdministrationDto> calculateTherapyAdministrationTimes(final TherapyDto therapy)
  {
    return administrationTaskCreator.calculateTherapyAdministrationTimes(
        therapy,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public DateTime calculateNextTherapyAdministrationTime(final TherapyDto therapy, final boolean newPrescription)
  {
    return administrationTaskCreator.calculateNextTherapyAdministrationTime(
        therapy,
        newPrescription,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public DateTime findPreviousTaskForTherapy(final String patientId, final String compositionUid, final String ehrOrderName)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    return medicationsBo.findPreviousTaskForTherapy(patientId, compositionUid, ehrOrderName, when);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void dismissSupplyTask(final String patientId, final List<String> taskIds)
  {
    pharmacistTaskHandler.dismissSupplyTask(taskIds, RequestContextHolder.getContext().getUserId());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void deleteNurseSupplyTask(final String patientId, final String taskId, final Locale locale)
  {
    try
    {
      pharmacistTaskHandler.deleteNurseSupplyTask(patientId, taskId, RequestContextHolder.getContext().getUserId());
    }
    catch (final IllegalStateException ise)
    {
      throw new UserWarning(Dictionary.getEntry("nurse.resupply.cannot.delete.already.dispensed.warning", locale));
    }
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void confirmSupplyReminderTask(
      final String taskId,
      final String compositionUid,
      final MedicationSupplyTypeEnum supplyTypeEnum,
      final Integer supplyInDays,
      final String comment)
  {
    pharmacistTaskHandler.confirmSupplyReminderTask(
        taskId,
        compositionUid,
        supplyTypeEnum,
        supplyInDays,
        RequestContextHolder.getContext().getUserId(),
        comment);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void editSupplyReminderTask(
      final String taskId,
      final MedicationSupplyTypeEnum supplyTypeEnum,
      final Integer supplyInDays,
      final String comment)
  {
    pharmacistTaskHandler.editSupplyReminderTask(
        taskId,
        supplyTypeEnum,
        supplyInDays,
        comment,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void confirmSupplyReviewTask(
      final String patientId,
      final String taskId,
      final String compositionUid,
      final boolean createSupplyReminder,
      final MedicationSupplyTypeEnum supplyTypeEnum,
      final Integer supplyInDays,
      final String comment)
  {
    final SupplyReviewTaskSimpleDto reviewTask =
        (SupplyReviewTaskSimpleDto)pharmacistTaskProvider.getSupplySimpleTask(taskId);

    pharmacistTaskHandler.confirmSupplyReviewTask(
        patientId,
        taskId,
        compositionUid,
        reviewTask.isAlreadyDispensed(),
        createSupplyReminder,
        supplyTypeEnum,
        supplyInDays,
        comment,
        RequestContextHolder.getContext().getUserId(),
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void confirmPharmacistDispenseTask(
      final String patientId,
      final String taskId,
      final String compositionUid,
      final TherapyAssigneeEnum requesterRole,
      final SupplyRequestStatus supplyRequestStatus)
  {
    pharmacistTaskHandler.confirmPharmacistDispenseTask(
        patientId,
        taskId,
        compositionUid,
        requesterRole,
        supplyRequestStatus,
        RequestContextHolder.getContext().getUserId());
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public MedicationSupplyTaskSimpleDto getSupplySimpleTask(
      final String taskId,
      final Locale locale)
  {
    return pharmacistTaskProvider.getSupplySimpleTask(
        taskId,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public Map<ActionReasonType, List<CodedNameDto>> getActionReasons(final ActionReasonType type)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    return medicationsDao.getActionReasons(when, type);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public List<MentalHealthTemplateDto> getMentalHealthTemplates()
  {
    return medicationsDao.getMentalHealthTemplates();
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void saveMentalHealthReport(
      @Nonnull final MentalHealthDocumentDto mentalHealthDocumentDto,
      final NamedExternalDto careProvider)
  {
    mentalHealthFormHandler.saveNewMentalHealthForm(
        Preconditions.checkNotNull(mentalHealthDocumentDto, "mentalHealthDocumentDto must not be null!"),
        careProvider,
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public SupplyDataForPharmacistReviewDto getSupplyDataForPharmacistReview(
      @Nonnull final String patientId,
      @Nonnull final String therapyCompositionUid)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    StringUtils.checkNotBlank(therapyCompositionUid, "therapyCompositionUid");

    return pharmacistTaskProvider.getSupplyDataForPharmacistReview(patientId, therapyCompositionUid);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public Map<String, String> getMedicationExternalValues(
      final String externalSystem, final MedicationsExternalValueType valueType, final Set<String> valuesSet)
  {
    return medicationsDao.getMedicationExternalValues(externalSystem, valueType, valuesSet);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public String getMedicationExternalId(final String externalSystem, final long medicationId)
  {
    return medicationsDao.getMedicationExternalId(
        externalSystem,
        medicationId,

        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void orderPerfusionSyringePreparation(
      @Nonnull final String patientId,
      @Nonnull final String compositionUid,
      @Nonnull final String ehrOrderName,
      final int numberOfSyringes,
      final boolean urgent,
      @Nonnull final DateTime dueTime,
      final boolean printSystemLabel)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    StringUtils.checkNotBlank(compositionUid, "compositionUid");
    StringUtils.checkNotBlank(ehrOrderName, "ehrOrderName");
    Preconditions.checkNotNull(dueTime, "dueTime");

    preparePerfusionSyringeProcessHandler.handlePreparationRequest(
        patientId,
        compositionUid,
        ehrOrderName,
        numberOfSyringes,
        urgent,
        dueTime,
        RequestContextHolder.getContext().getUserMetadata().map(UserMetadata::getFullName).get(),
        printSystemLabel);
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public List<PerfusionSyringePatientTasksDto> findPerfusionSyringePreparationRequests(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds,
      @Nonnull final Set<TaskTypeEnum> taskTypes,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(taskTypes, "taskTypes is null");
    Preconditions.checkNotNull(locale, "locale is null");

    final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap =
        getPatientDisplayWithLocationDtoMap(careProviderIds, patientIds);

    return pharmacistTaskProvider.findPerfusionSyringeTasks(
        patientIdAndPatientWithLocationMap,
        null,
        taskTypes,
        false,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public List<PerfusionSyringePatientTasksDto> findFinishedPerfusionSyringePreparationRequests(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds,
      @Nonnull final Interval searchInterval,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(searchInterval, "searchInterval is null");
    Preconditions.checkNotNull(locale, "locale is null");

    final Map<String, PatientDisplayWithLocationDto> patientIdAndPatientWithLocationMap =
        getPatientDisplayWithLocationDtoMap(careProviderIds, patientIds);

    // we only show closed PERFUSION_SYRINGE_DISPENSE tasks
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE);

    return pharmacistTaskProvider.findPerfusionSyringeTasks(
        patientIdAndPatientWithLocationMap,
        searchInterval,
        taskTypes,
        true,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public boolean finishedPerfusionSyringeRequestsExistInLastHours(
      @Nonnull final String patientId,
      @Nonnull final String originalTherapyId,
      final int hours)
  {
    Preconditions.checkNotNull(patientId, "patientId is null");
    Preconditions.checkNotNull(originalTherapyId, "originalTherapyId is null");

    final DateTime requestTimestamp = RequestContextHolder.getContext().getRequestTimestamp();
    return pharmacistTaskProvider.therapyHasTasksClosedInInterval(
        patientId,
        originalTherapyId,
        Collections.singleton(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE),
        new Interval(requestTimestamp.minusHours(hours), requestTimestamp)
    );
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public Map<String, PerfusionSyringePreparationDto> startPerfusionSyringePreparations(
      final String patientId,
      final List<String> taskIds,
      final Set<String> originalTherapyIds,
      final boolean isUrgent,
      final Locale locale)
  {
    pharmacistTaskHandler.confirmPerfusionSyringeTasks(
        taskIds,
        RequestContextHolder.getContext().getUserId());
    return pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringePreparationDtoMap(
        patientId,
        isUrgent,
        originalTherapyIds,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public Map<String, String> dispensePerfusionSyringePreparations(
      final String patientId,
      final List<String> taskIds,
      final boolean isUrgent)
  {
    pharmacistTaskHandler.confirmPerfusionSyringeTasks(taskIds, RequestContextHolder.getContext().getUserId());
    return pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringeTaskIdMap(patientId, isUrgent);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public Map<String, String> confirmPerfusionSyringePreparations(
      final String patientId,
      final List<String> taskIds,
      final boolean isUrgent)
  {
    pharmacistTaskHandler.confirmPerfusionSyringeTasks(taskIds, RequestContextHolder.getContext().getUserId());
    return pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringeTaskIdMap(patientId, isUrgent);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void deletePerfusionSyringeRequest(final String taskId, final Locale locale)
  {
    try
    {
      pharmacistTaskHandler.deletePerfusionSyringeTask(taskId, RequestContextHolder.getContext().getUserId());
    }
    catch (SystemException se)
    {
      throw new UserWarning(Dictionary.getEntry("data.changed.please.reload", locale));
    }
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public Map<String, String> undoPerfusionSyringeRequestState(
      final String patientId,
      final String taskId,
      final boolean isUrgent)
  {
    pharmacistTaskHandler.undoPerfusionSyringeTask(taskId, RequestContextHolder.getContext().getUserId());
    return pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringeTaskIdMap(patientId, isUrgent);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void updateTherapySelfAdministeringStatus(
      final String patientId, final String compositionUid, final SelfAdministeringActionEnum selfAdministeringActionEnum)
  {
    final MedicationOrderComposition orderComposition = medicationsOpenEhrDao.loadMedicationOrderComposition(
        patientId,
        compositionUid);

    therapyUpdater.updateTherapySelfAdministeringStatus(
        patientId,
        orderComposition,
        selfAdministeringActionEnum,
        RequestContextHolder.getContext().getUserId(),
        RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void printPharmacistDispenseTask(
      final String patientId,
      final String taskId,
      final String compositionUid,
      final TherapyAssigneeEnum requesterRole,
      final SupplyRequestStatus supplyRequestStatusEnum)
  {
    pharmacistTaskHandler.setDispenseTaskPrintedTimestamp(taskId, RequestContextHolder.getContext().getRequestTimestamp());
    //todo supply implement printing
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public Map<String, String> getTherapiesFormattedDescriptionsMap(
      final String patientId,
      final Set<String> therapyIds,
      final Locale locale)
  {
    final Map<String, String> descriptionsMap = new HashMap<>();
    for (final String therapyId : therapyIds)
    {
      try
      {
        final String therapyFormattedDisplay = medicationsBo.getTherapyFormattedDisplay(
            patientId, therapyId, RequestContextHolder.getContext().getRequestTimestamp(), locale);
        descriptionsMap.put(therapyId, therapyFormattedDisplay);
      }
      catch (final CompositionNotFoundException e)
      {
        LOG.error(StackTraceUtils.getStackTraceString(e));
      }
    }
    return descriptionsMap;
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public Integer getPatientsCurrentBnfMaximumSum(
      final String patientId)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    return medicationsOpenEhrDao.getPatientsCurrentBnfMaximumSum(when, patientId);
  }

  @Override
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @Transactional
  @EhrSessioned
  public String saveOutpatientPrescription(
      final String patientId,
      final PrescriptionPackageDto prescriptionPackageDto)
  {
    return prescriptionHandler.savePrescription(
        patientId, prescriptionPackageDto, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void deleteOutpatientPrescription(@Nonnull final String patientId, @Nonnull final String prescriptionUid)
  {
    medicationsOpenEhrDao.deleteComposition(patientId, prescriptionUid);
  }

  @Override
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @Transactional
  @EhrSessioned
  public void updateOutpatientPrescriptionStatus(
      final String patientId,
      final String compositionUid,
      final String prescriptionTherapyId,
      final PrescriptionStatus status)
  {
    prescriptionHandler.updatePrescriptionStatus(
        patientId, compositionUid, prescriptionTherapyId, status, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @Transactional
  @EhrSessioned
  public String updateOutpatientPrescription(
      final String patientId,
      final String prescriptionPackageId,
      final String compositionUid,
      final List<PrescriptionDto> prescriptionDtoList,
      final DateTime when,
      final Locale locale)
  {
    return prescriptionHandler.updatePrescriptionPackage(patientId, compositionUid, prescriptionDtoList, when);
  }

  @Override
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @Transactional
  @EhrSessioned
  public TherapyDocumentDto getTherapyDocument(
      final String patientId,
      final String contentId,
      final TherapyDocumentType documentType,
      final Locale locale)
  {
    return therapyDocumentProvider.getTherapyDocument(
        patientId,
        contentId,
        documentType,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public PerfusionSyringeTaskSimpleDto getPerfusionSyringeTaskSimpleDto(final String taskId, final Locale locale)
  {
    return pharmacistTaskProvider.getPerfusionSyringeTaskSimpleDto(taskId, locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void editPerfusionSyringeTask(
      @Nonnull final String taskId,
      @Nonnull final Integer numberOfSyringes,
      final boolean isUrgent,
      @Nonnull final DateTime dueDate,
      final boolean printSystemLabel)
  {
    StringUtils.checkNotBlank(taskId, "taskId");
    Preconditions.checkNotNull(numberOfSyringes, "numberOfSyringes");
    Preconditions.checkNotNull(dueDate, "dueDate");

    pharmacistTaskHandler.editPerfusionSyringeTask(taskId, numberOfSyringes, isUrgent, dueDate, printSystemLabel);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  @EhrSessioned
  public TherapyDocumentsDto getTherapyDocuments(
      final String patientId,
      final Integer numberOfResults,
      final Integer resultsOffset,
      final Locale locale)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    return therapyDocumentProvider.getTherapyDocuments(patientId, numberOfResults, resultsOffset, when, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public String getMedicationExternalId(@Nonnull final String externalSystem, @Nonnull final Long medicationId)
  {
    StringUtils.checkNotBlank(externalSystem, "externalSystem must not be empty or null!");
    Preconditions.checkNotNull(medicationId, "medicationId must not be null!");
    return medicationsDao.getMedicationExternalId(
        externalSystem, medicationId, RequestContextHolder.getContext().getRequestTimestamp());
  }

  @Override
  @EhrSessioned
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public TitrationDto getDataForTitration(
      @Nonnull final String patientId,
      @Nonnull final String therapyId,
      @Nonnull final TitrationType titrationType,
      @Nonnull final DateTime searchStart,
      @Nonnull final DateTime searchEnd,
      @Nonnull final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    StringUtils.checkNotBlank(therapyId, "therapyId is required");
    Preconditions.checkNotNull(titrationType, "titrationType is required");
    Preconditions.checkNotNull(locale, "locale is required");
    return titrationDataProvider.getDataForTitration(
        patientId,
        therapyId,
        titrationType,
        searchStart,
        searchEnd,
        RequestContextHolder.getContext().getRequestTimestamp(),
        locale);
  }

  @Override
  @EhrSessioned
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  //for emram, refactor
  public List<TherapyRowDto> getTherapiesForCurrentCentralCase(@Nonnull final String patientId, @Nonnull final Locale locale)
  {
    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    final PatientDataForMedicationsDto patientData = medicationsConnector.getPatientData(patientId, when);

    final MedicationsCentralCaseDto centralCaseDto = patientData.getCentralCaseDto();
    if (centralCaseDto == null)
    {
      return Collections.emptyList();
    }

    final DateTime fromWhen =
        centralCaseDto.isOutpatient() ?
        when.withTimeAtStartOfDay() :
        centralCaseDto.getCentralCaseEffective().getStart();

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs =
        medicationsOpenEhrDao.findMedicationInstructions(
            patientId,
            Intervals.infiniteFrom(fromWhen),
            null);

    return overviewContentProvider.buildTherapyRows(
        patientId,
        instructionPairs,
        Collections.emptyList(),
        Collections.emptyList(),
        TherapySortTypeEnum.CREATED_TIME_DESC,
        false,
        Collections.emptyList(),
        null,
        Intervals.infiniteFrom(fromWhen),
        null,
        locale,
        when);
  }

  @Override
  @EhrSessioned
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public Double getRemainingInfusionBagQuantity(
      @Nonnull final DateTime when,
      @Nonnull final String patientId,
      @Nonnull final String therapyId)
  {
    Preconditions.checkNotNull(when, "when must not be null");
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(therapyId, "therapyId must not be null");

    return infusionBagHandler.getRemainingInfusionBagQuantity(patientId, therapyId, when);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED))
  public String getUnlicensedMedicationWarning(@Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(locale, "locale is required");
    return MedicationPreferences.getUnlicensedMedicationWarning(locale);
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public void setAdministrationDoctorsComment(@Nonnull final String taskId, final String doctorsComment)
  {
    Preconditions.checkNotNull(taskId, "taskId");

    medicationsTasksHandler.setAdministrationDoctorsComment(taskId, doctorsComment);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public Long getMedicationIdForBarcode(@Nonnull final String barcode)
  {
    StringUtils.checkNotBlank(barcode, "barcode");
    return medicationsDao.getMedicationIdForBarcode(barcode);
  }

  @Override
  @EhrSessioned
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public BarcodeTaskSearchDto getAdministrationTaskForBarcode(
      @Nonnull final String patientId,
      @Nonnull final String medicationBarcode)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    StringUtils.checkNotBlank(medicationBarcode, "barcode");

    final DateTime when = RequestContextHolder.getContext().getRequestTimestamp();
    return barcodeTaskFinder.getAdministrationTaskForBarcode(patientId, medicationBarcode, when);
  }

  @Override
  @Transactional(readOnly = true)
  @ServiceMethod(auditing = @Auditing(level = Level.FULL))
  public String getOriginalTherapyId(@Nonnull final String patientId, @Nonnull final String therapyId)
  {
    StringUtils.checkNotBlank(patientId, "patientId");
    StringUtils.checkNotBlank(therapyId, "therapyId");

    final MedicationOrderComposition composition = medicationsOpenEhrDao.loadMedicationOrderComposition(
        patientId,
        TherapyIdUtils.parseTherapyId(therapyId).getFirst());
    return MedicationsEhrUtils.getOriginalTherapyId(
        composition,
        composition.getMedicationDetail().getMedicationInstruction().get(0));
  }
}
