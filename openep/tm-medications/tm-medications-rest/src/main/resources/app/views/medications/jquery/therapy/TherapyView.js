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

Class.define('tm.views.medications.TherapyView', 'app.views.common.AppView', {
  cls: "v-therapy-view",

  mode: "FLOW", //FLOW, ORDERING, EDIT, ORDERING_PAST, EDIT_PAST (TherapyPaneModeEnum.java)

  /** properties */
  pharmacistReviewReferBackPreset: null,
  mentalHealthReportEnabled: null,
  medicationsSupplyPresent: null,
  autoAdministrationCharting: null,
  medicationsWitnessRequiredUnderAge: null,
  medicationsWitnessRequired: null,
  bnfEnabled: null,
  infusionBagEnabled: null,
  medicationsShowHeparinPane: null,
  outpatientPrescriptionType: null,
  doctorReviewEnabled: null,
  doseRangeEnabled: false,
  formularyFilterEnabled: null,

  /** update data */
  dayCount: null,
  groupField: null,
  patientData: null,  //PatientDataForMedicationsDto.java
  patientLastLinkName: null,
  patientId: null,
  roundsInterval: null,
  administrationTiming: null,
  referenceWeight: null,
  userPerson: null,
  context: null,
  editAllowed: null,
  surgeryReportEnabled: null,
  customGroups: null,
  therapySortTypeEnum: null,
  nonFormularyMedicationSearchAllowed: null,

  presetMedicationId: null,
  presetDate: null,
  therapyToEdit: null,

  /** privates */
  therapyGrid: null,
  header: null,
  actionsHeader: null,
  timelineContainer: null,
  therapyEnums: null,
  medications: null,
  careProfessionals: null,
  doseForms: null,
  routes: null,
  units: null,
  problemDescriptionNamedIdentitiesMap: null,
  therapyChangeReasonTypeMap: null,
  displayTypeButtonGroup: null,
  flowGridTypeButton: null,
  timelineTypeButton: null,
  pharmacistTypeButton: null,
  reconciliationSummaryTypeButton: null,
  pharmacistReviewContainer: null,
  summaryContainer: null,
  documentationContainer: null,
  patientDataContainer: null,
  actionCallbackListener: null,
  bnfMaximumSum: null,

  _presentDataConditionalTask: null,
  _therapyAuthority: null,
  _testRenderCoordinator: null,

  /** statics */
  statics: {
    VIEW_ACTION_CANCEL_PRESCRIPTION: 'cancelPrescription',
    VIEW_ACTION_OUTPATIENT_PRESCRIPTION: 'outpatientPrescription',
    VIEW_ACTION_DELETE_OUTPATIENT_PRESCRIPTION: 'deleteOutpatientPrescription',
    VIEW_ACTION_UPDATE_OUTPATIENT_PRESCRIPTION: 'updateOutpatientPrescription',
    VIEW_ACTION_GET_EXTERNAL_OUTPATIENT_PRESCRIPTIONS: 'getExternalOutpatientPrescription',
    VIEW_ACTION_AUTHORIZE_OUTPATIENT_PRESCRIPTION: 'authorizeOutpatientPrescription',
    VIEW_ACTION_AUTHENTICATE_ADMINISTRATION_WITNESS: 'authenticateAdministrationWitness',

    SERVLET_PATH_LOAD_THERAPY_VIEW_PATIENT_DATA: '/getTherapyViewPatientData',
    SERVLET_PATH_GET_TIME_OFFSET: '/getTimeOffset',
    SERVLET_PATH_LOAD_THERAPY_FLOW_DATA: '/therapyflowdata',
    SERVLET_PATH_RELOAD_SINGLE_THERAPY_AFTER_ACTION: '/reloadSingleTherapyAfterAction',
    SERVLET_PATH_GET_REMAINING_INFUSION_BAG_QUANTITY: '/getRemainingInfusionBagQuantity',
    SERVLET_PATH_PATIENT_DATA: '/getPatientData',
    SERVLET_PATH_MEDICATION_ORDER_CARD_INFO_DATA: '/patientmedicationordercardinfodata',
    SERVLET_PATH_GET_BASELINE_THERAPIES: '/getPatientBaselineInfusionIntervals',
    SERVLET_PATH_MEDICATION_ROUTES: '/getMedicationRoutes',
    SERVLET_PATH_FILL_DISPLAY_VALUES: '/fillTherapyDisplayValues',
    SERVLET_PATH_GET_THERAPY_FORMATTED_DISPLAY: '/getTherapyFormattedDisplay',
    SERVLET_PATH_SAVE_MEDICATIONS_ON_ADMISSION: '/saveMedicationsOnAdmission',
    SERVLET_PATH_SAVE_MEDICATIONS_ON_DISCHARGE: '/saveMedicationsOnDischarge',
    SERVLET_PATH_SAVE_REFERENCE_WEIGHT: '/saveReferenceWeight',
    SERVLET_PATH_FIND_SUPPLY_CANDIDATES: '/getMedicationSupplyCandidates',
    SERVLET_PATH_REVIEW_THERAPY: "/reviewTherapy",
    SERVLET_PATH_SUSPEND_THERAPY: "/suspendTherapy",
    SERVLET_PATH_REISSUE_THERAPY: "/reissueTherapy",
    SERVLET_PATH_SUSPEND_ALL_THERAPIES: "/suspendAllTherapies",
    SERVLET_PATH_ABORT_THERAPY: "/abortTherapy",
    SERVLET_PATH_GET_ROUTES: "/getRoutes",
    SERVLET_PATH_GET_DOSE_FORMS: "/getDoseForms",
    SERVLET_PATH_GET_UNITS: "/getMedicationBasicUnits",
    SERVLET_PATH_FIND_WARNINGS: "/findMedicationWarnings",
    SERVLET_PATH_GET_THERAPIES_FOR_WARNINGS: "/getTherapiesForWarnings",
    SERVLET_PATH_SAVE_TEMPLATE: "/saveTherapyTemplate",
    SERVLET_PATH_DELETE_TEMPLATE: "/deleteTherapyTemplate",
    SERVLET_PATH_GET_MEDICATION_DOCUMENT: "/getMedicationDocument",
    SERVLET_PATH_GET_THERAPY_PDF_REPORT: "/getTherapyPdfReport",
    SERVLET_PATH_OPEN_MEDICATION_DOCUMENT: "/openMedicationDocument",
    SERVLET_PATH_SAVE_CONSECUTIVE_DAYS: "/saveConsecutiveDays",
    SERVLET_PATH_GET_THERAPIES_FOR_PREVIOUS_HOSPITALIZATION: "/getLastTherapiesForPreviousHospitalization",
    SERVLET_PATH_GET_CURRENT_HOSPITALIZATION_MENTAL_HEALTH_DRUGS: "/getCurrentHospitalizationMentalHealthTherapies",
    SERVLET_PATH_SAVE_MENTAL_HEALTH_DOCUMENT: "/saveMentalHealthDocument",
    SERVLET_PATH_GET_THERAPIES_ON_ADMISSION_GROUPS: "/getTherapiesOnAdmissionGroups",
    SERVLET_PATH_GET_THERAPY_TIMELINE: '/getTherapyTimeline',
    SERVLET_PATH_GET_PHARMACIST_TIMELINE: '/getPharmacistTimeline',
    SERVLET_PATH_RESCHEDULE_TASKS: '/rescheduleTasks',
    SERVLET_PATH_RESCHEDULE_TASK: '/rescheduleTask',
    SERVLET_PATH_DELETE_TASK: '/deleteTask',
    SERVLET_PATH_DELETE_ADMINISTRATION: '/deleteAdministration',
    SERVLET_PATH_GET_CARE_PROFESSIONALS: '/getCareProfessionals',
    SERVLET_PATH_TAG_THERAPY_FOR_PRESCRIPTION: '/tagTherapyForPrescription',
    SERVLET_PATH_UNTAG_THERAPY_FOR_PRESCRIPTION: '/untagTherapyForPrescription',
    SERVLET_PATH_SAVE_PHARMACIST_REVIEW: '/savePharmacistReview',
    SERVLET_PATH_GET_PROBLEM_DESC_NAMED_IDENTITIES: "/getProblemDescriptionNamedIdentities",
    SERVLET_PATH_AUTHORIZE_PHARMACIST_REVIEWS: '/authorizePharmacistReviews',
    SERVLET_PATH_GET_PHARMACIST_REVIEWS: '/getPharmacistReviews',
    SERVLET_PATH_REVIEW_PHARMACIST_REVIEW: '/reviewPharmacistReview',
    SERVLET_PATH_GET_THERAPY_PHARMACIST_REVIEWS: '/getPharmacistReviewsForTherapy',
    SERVLET_PATH_DELETE_PHARMACIST_REVIEW: '/deletePharmacistReview',
    SERVLET_PATH_GET_THERAPY_ADMINISTRATION_TIMES: '/calculateTherapyAdministrationTimes',
    SERVLET_PATH_CALCULATE_NEXT_THERAPY_ADMINISTRATION_TIME: '/calculateNextTherapyAdministrationTime',
    SERVLET_PATH_CALCULATE_PARACETAMOL_ADMINISTRATION_RULE: '/calculateParacetamolAdministrationRule',
    SERVLET_PATH_APPLY_MEDICATION_RULE: '/applyMedicationRule',
    SERVLET_PATH_CALCULATE_INGREDIENT_RULE_FOR_THERAPIES: '/calculateIngredientRuleForTherapies',
    SERVLET_PATH_FIND_PREVIOUS_TASK_FOR_THERAPY: '/findPreviousTaskForTherapy',
    SERVLET_PATH_LOAD_FILL_PHARMACISTS_THERAPY_EDIT: '/fillPharmacistReviewTherapyOnEdit',
    SERVLET_PATH_GET_THERAPY_GROUPS_ON_ADMISSION: '/getTherapiesOnAdmissionGroups',
    SERVLET_PATH_GET_MEDICATIONS_ON_ADMISSION: '/getMedicationsOnAdmission',
    SERVLET_PATH_GET_THERAPY_GROUPS_ON_DISCHARGE: '/getTherapiesOnDischargeGroups',
    SERVLET_PATH_ASSERT_PASSWORD_FOR_USERNAME: '/assertPasswordForUsername',
    SERVLET_PATH_GET_MEDICATIONS_ON_DISCHARGE: '/getMedicationsOnDischarge',
    SERVLET_PATH_GET_MENTAL_HEALTH_TEMPLATES: '/getMentalHealthTemplates',
    SERVLET_PATH_SEND_THERAPY_RESUPPLY_REQUEST: '/sendNurseResupplyRequest',
    SERVLET_PATH_ORDER_THERAPY_PERFUSION_SYRINGE: '/orderPerfusionSyringePreparation',
    SERVLET_PATH_DISMISS_THERAPY_PERFUSION_SYRINGE: '/deletePerfusionSyringeRequest',
    SERVLET_PATH_CONFIRM_SUPPLY_REMINDER_TASK: '/confirmSupplyReminderTask',
    SERVLET_PATH_EDIT_SUPPLY_REMINDER_TASK: '/editSupplyReminderTask',
    SERVLET_PATH_CONFIRM_SUPPLY_REVIEW_TASK: '/confirmSupplyReviewTask',
    SERVLET_PATH_DISMISS_NURSE_SUPPLY_TASK: '/dismissNurseSupplyTask',
    SERVLET_PATH_DISMISS_PHARMACIST_SUPPLY_TASK: '/dismissPharmacistSupplyTask',
    SERVLET_PATH_GET_RECONCILIATION_GROUPS: '/getReconciliationGroups',
    SERVLET_PATH_GET_PHARMACIST_SUPPLY_SIMPLE_TASK: '/getPharmacistSupplySimpleTask',
    SERVLET_PATH_GET_SUPPLY_DATA_FOR_PHARMACIST_REVIEW: '/getSupplyDataForPharmacistReview',
    SERVLET_PATH_SET_DOCTOR_CONFIRMATION_RESULT: '/setDoctorConfirmationResult',
    SERVLET_PATH_UPDATE_SELF_ADMINISTERING_STATUS: '/updateTherapySelfAdministeringStatus',
    SERVLET_PATH_GET_PATIENTS_CURRENT_BNF_MAXIMUM_SUM: '/getPatientsCurrentBnfMaximumSum',
    SERVLET_PATH_GET_PERFUSION_SYRINGE_TASK: '/getPerfusionSyringeTaskSimpleDto',
    SERVLET_PATH_EDIT_PERFUSION_SYRINGE_TASK: '/editPerfusionSyringeTask',
    SERVLET_PATH_GET_MEDICATION_EXTERNAL_ID: '/getMedicationExternalId',
    SERVLET_PATH_GET_LINK_THERAPY_CANDIDATES: '/getLinkTherapyCandidates',
    SERVLET_PATH_GET_FINISHED_PERFUSION_SYRINGE_REQUESTS_EXIST: '/finishedPerfusionSyringeRequestsExistInLastHours',
    SERVLET_PATH_GET_DATA_FOR_TITRATION: '/getDataForTitration',
    SERVLET_PATH_HANDLE_ADDITIONAL_WARNINGS_ACTION: '/handleAdditionalWarningsAction',

    EVENT_TYPE_MEDICATION_BARCODE_SCANNED: new tm.jquery.event.EventType({
      name: 'medicationBarcodeScanned', delegateName: null
    })
  },

  /** constructor */
  Constructor: function(config)
  {
    config = tm.jquery.Utils.applyIf({
      layout: new tm.jquery.VFlexboxLayout.create("flex-start", "stretch")
    }, config);
    this.callSuper(config);

    this.registerEventTypes('tm.views.medications.TherapyView', [
      {eventType: tm.views.medications.TherapyView.EVENT_TYPE_MEDICATION_BARCODE_SCANNED}
    ]);

    this._restApi = new app.views.medications.RestApi({ view: this });
    this.pharmacistReviewReferBackPreset = this.getProperty("pharmacistReviewReferBackPreset");
    this.mentalHealthReportEnabled = this.getProperty("mentalHealthReportEnabled");
    this.autoAdministrationCharting = this.getProperty("autoAdministrationCharting");
    this.medicationsSupplyPresent = this.getProperty("medicationsSupplyPresent");
    this.medicationsWitnessRequiredUnderAge = this.getProperty("medicationsWitnessRequiredUnderAge");
    this.medicationsWitnessRequired = this.getProperty("medicationsWitnessRequired");
    this.bnfEnabled = this.getProperty("bnfEnabled");
    this.infusionBagEnabled = this.getProperty("infusionBagEnabled");
    this.medicationsShowHeparinPane = this.getProperty("medicationsShowHeparinPane");
    this.outpatientPrescriptionType = this.getProperty("outpatientPrescriptionType");
    this.doctorReviewEnabled = this.getProperty("doctorReviewEnabled");
    this.doseRangeEnabled = this.getProperty("doseRangeEnabled");
    this.formularyFilterEnabled = this.getProperty("formularyFilterEnabled");

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'therapy-view-coordinator',
      view: this,
      component: this
    });

    CurrentTime.setOffset(this.getProperty("timeOffset"));

    tm.views.medications.TherapyView.THERAPY_PREPARE_VIEW_HUB = this.createUserAction("therapyPrepareViewHub");
    tm.views.medications.TherapyView.THERAPY_FLOW_LOAD_HUB = this.createUserAction("therapyFlowLoadHub");
    tm.views.medications.TherapyView.THERAPY_FLOW_NAVIGATE_HUB = this.createUserAction("therapyFlowNavigateHub");
    tm.views.medications.TherapyView.THERAPY_SAVE_REFERENCE_WEIGHT_HUB = this.createUserAction("therapySaveReferenceWeightHub");
    tm.views.medications.TherapyView.THERAPY_SAVE_HUB = this.createUserAction("therapySaveHub");
    tm.views.medications.TherapyView.THERAPY_TIMELINE_LOAD_HUB = this.createUserAction("therapyTimelineLoadHub");
    tm.views.medications.TherapyView.THERAPY_ABORT_HUB = this.createUserAction("therapyAbortHub");
    tm.views.medications.TherapyView.THERAPY_REVIEW_HUB = this.createUserAction("therapyReviewHub");
    tm.views.medications.TherapyView.THERAPY_SUSPEND_HUB = this.createUserAction("therapySuspendHub");
    tm.views.medications.TherapyView.THERAPY_SUSPEND_ALL_HUB = this.createUserAction("therapySuspendAllHub");
    tm.views.medications.TherapyView.THERAPY_REISSUE_HUB = this.createUserAction("therapyReissueHub");
    tm.views.medications.TherapyView.THERAPY_CONFIRM_ADMINISTRATION_HUB = this.createUserAction("therapyConfirmAdministrationHub");
    tm.views.medications.TherapyView.THERAPY_MOVE_ADMINISTRATION_HUB = this.createUserAction("therapyMoveAdministrationHub");
    tm.views.medications.TherapyView.THERAPY_DELETE_ADMINISTRATION_HUB = this.createUserAction("therapyDeleteAdministrationHub");
    tm.views.medications.TherapyView.THERAPY_DELETE_ADMINISTRATION_TASK_HUB = this.createUserAction("therapyDeleteAdministrationTaskHub");
    tm.views.medications.TherapyView.THERAPY_GET_LINK_THERAPY_CANDIDATES_HUB = this.createUserAction("therapyGetLinkTherapyCandidatesHub");
    tm.views.medications.TherapyView.THERAPY_HANDLE_ADDITIONAL_WARNINGS_ACTION = this.createUserAction("therapyHandleAdditionalWarningsActionHub");

    this.careProfessionals = [];
    this.doseForms = [];
    this.routes = [];
    this.units = [];
    var viewInitData = this.getViewInitData();

    if (!tm.jquery.Utils.isEmpty(viewInitData) && !tm.jquery.Utils.isEmpty(viewInitData.paneMode))
    {
      this.mode = viewInitData.paneMode;
    }

    this.userPerson = {
      id: this.getProperty("userPersonId"),
      name: this.getProperty("userPersonName")
    };

    this._therapyAuthority = new app.views.medications.therapy.TherapyAuthority({ view: this });

    this.optimizeForPerformance = this.isSwingApplication();
    this.editAllowed = this.getProperty("editAllowed");
    this.therapyReportEnabled = this.getProperty("therapyReportEnabled");
    this.surgeryReportEnabled = this.getProperty("surgeryReportEnabled");
    this.gridViewEnabled = this.getProperty("gridViewEnabled");
    this.timelineViewEnabled = this.getProperty("timelineViewEnabled");
    this.pharmacistReviewAllowed = this.getProperty("pharmacistReviewAllowed");
    this.showPharmacistReviewStatus = this.getProperty("showPharmacistReviewStatus");
    this.medicationReconciliationEnabled = this.getProperty("medicationReconciliationEnabled");
    this.medicationConsentT2T3Allowed = this.getProperty("medicationConsentT2T3Allowed");
    this.medicationDocumentViewEnabled = this.getProperty("medicationDocumentViewEnabled");
    this.addMedicationToPreparationTasklistAllowed = this.getProperty("addMedicationToPreparationTasklistAllowed");
    this.nonFormularyMedicationSearchAllowed = this.getProperty("nonFormularyMedicationSearchAllowed");

    this.context = viewInitData && viewInitData.contextData ? JSON.parse(viewInitData.contextData) : null;

    this._loadCareProfessionals();
    if (this.mode == 'FLOW')
    {
      this._loadRoutes();
      this._buildFlowGui();
    }
   
    this.getLocalLogger().info("viewInitData.patientId:", viewInitData ? viewInitData.patientId : "");
    this.getLocalLogger().info("this.getProperty(patientId):", this.getProperty("patientId"));

    var patientId = viewInitData && viewInitData.patientId ? viewInitData.patientId : this.getProperty("patientId", true);
    if (patientId)
    {
      var updateDataCommand = {
        "update": {"data": {"patientId": patientId}}
      };
      this.onViewCommand(updateDataCommand);
    }
  },

  onViewCommand: function(command)
  {
    if (command.hasOwnProperty('update'))
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus();
      tm.jquery.ComponentUtils.hideAllTooltips();
      tm.jquery.ComponentUtils.hideAllDialogs();
      this.updateDataCommand = command;
      this.updateData(command);
    }
    else if (command.hasOwnProperty('clear'))
    {
      this._abortPresentDataTask();

      tm.jquery.ComponentUtils.hideAllDropDownMenus();
      tm.jquery.ComponentUtils.hideAllTooltips();
      tm.jquery.ComponentUtils.hideAllDialogs();
      if (this.therapyGrid)
      {
        this.therapyGrid.clear();
      }
      else if (this.timelineContainer)
      {
        this.timelineContainer.clear();
      }
      else if (this.pharmacistReviewContainer)
      {
        this.pharmacistReviewContainer.clear();
      }
      else if (this.summaryContainer)
      {
        this.summaryContainer.clearData();
      }
      else if (this.documentationContainer)
      {
        this.documentationContainer.clearData();
      }
      this.clearPatientDataContainer();
    }
    else if (command.hasOwnProperty('refresh'))
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus();
      tm.jquery.ComponentUtils.hideAllTooltips();
      tm.jquery.ComponentUtils.hideAllDialogs();
      this.updateData(this.updateDataCommand);
    }
    else if (command.hasOwnProperty("actionCallback"))
    {
      this.onViewActionCallback(command.actionCallback);
    }
  },

  onViewActionCallback: function(actionCallback)
  {
    if (!tm.jquery.Utils.isEmpty(this.actionCallbackListener))
    {
      this.actionCallbackListener.onActionCallback(actionCallback);
    }
    else if (!tm.jquery.Utils.isEmpty(this.documentationContainer)
        && this.documentationContainer.getPrescribedViewActions().contains(actionCallback.action))
    {
      this.documentationContainer.onViewActionCallback(actionCallback);
    }
  },

  updateData: function(config)
  {
    tm.jquery.ComponentUtils.hideAllDialogs();
    tm.jquery.ComponentUtils.hideAllDropDownMenus(this);
    tm.jquery.ComponentUtils.hideAllTooltips(this);

    this.patientId = config.update.data.patientId; //data - TherapyHtmlPortletValue.java

    //ORDERING PAST, EDIT PAST (DRP)
    this.patientData = config.update.data.patientData;
    this.presetMedicationId = config.update.data.presetMedicationId || null;
    this.therapyToEdit = config.update.data.therapyToEdit ?
        app.views.medications.common.TherapyJsonConverter.convert(config.update.data.therapyToEdit) :
        config.update.data.therapyToEdit;
    this.presetDate = config.update.data.presetDate ? new Date(config.update.data.presetDate) : null;

    this._refreshTimeOffsetFromServer();

    if (this.mode == 'FLOW')
    {
      if (tm.jquery.Utils.isEmpty(this.getPatientData()))
      {
        this._showToolbarGlassLayer();
      }

      var viewHubNotifier = this.getHubNotifier();
      var hubAction = tm.views.medications.TherapyView.THERAPY_PREPARE_VIEW_HUB;
      viewHubNotifier.actionStarted(hubAction);

      tm.jquery.ComponentUtils.hideAllDialogs();

      if (config.update.data.viewType)
      {
        this._setViewType(config.update.data.viewType);
        this._saveContext();
      }

      // Since we throw away the whole DOM for the UI, we need to make sure the UI is repainted
      // before we start reapplying values.
      // But we need to prevent content container's repaint if the documentation container is already rendered,
      // because I can't find a clean way to signal angular to destroy itself before jQuery removes
      // everything from the DOM. The issue seems to be timer related, since emitting close all dialogs
      // before letting repaint happens solved nothing. Open the external prescriptions dialog
      // and trigger refresh to test the issue.
      if (!(this.documentationContainer && this.documentationContainer.isRendered()))
      {
        this.content.repaint();
      }
      this.header.repaint();

      this._loadTherapyViewPatientData();
    }
    else if (['ORDERING', 'EDIT', 'ORDERING_PAST', 'EDIT_PAST'].indexOf(this.mode) > -1)
    {
      if (this.mode == 'ORDERING' || this.mode == 'ORDERING_PAST')
      {
        this._buildOrderingGui(this.mode == 'ORDERING_PAST', this.mode != 'ORDERING_PAST');
      }
      else
      {
        this._buildEditGui(this.mode == 'EDIT_PAST');
      }
    }
  },

  _presentData: function()
  {
    this.actionsHeader.showRecentHospitalizationButton(
        this.recentHospitalization && this.getTherapyAuthority().isManageInpatientPrescriptionsAllowed());
    this.actionsHeader.setCustomGroups(this.customGroups);
    var discharged = this._isDischarged();
    this.actionsHeader.setIsDischarged(discharged);
    this.actionsHeader.resetViewType();
    this.refreshPatientDataContainer();

    var viewType = this.actionsHeader.getViewType();

    if (viewType === "GRID")
    {
      var searchDate = this._getDefaultSearchDate(this.dayCount);
      this.therapyGrid.paintGrid(this.dayCount, searchDate, this.groupField, this.therapySortTypeEnum);
      this._addGridEvents();
    }
    else if (viewType === "TIMELINE")
    {
      this.timelineContainer.setPatientId(this.patientId, discharged, this.therapySortTypeEnum);
    }
    else if (viewType === "PHARMACIST")
    {
      this.pharmacistReviewContainer.refreshData();
    }
    else if (viewType === "SUMMARY")
    {
      this.summaryContainer.refreshData();
    }
    else if (viewType === "DOCUMENTATION")
    {
      this.documentationContainer.refreshData();
    }
  },

  _isDischarged: function()
  {
    var centralCaseData = this.getCentralCaseData();
    if (!tm.jquery.Utils.isEmpty(centralCaseData))
    {
      if (centralCaseData.outpatient === false)
      {
        if (centralCaseData.centralCaseEffective.endMillis)
        {
          var centralCaseEnd = new Date(centralCaseData.centralCaseEffective.endMillis);
          if (centralCaseEnd < CurrentTime.get())
          {
            return true;
          }
        }
      }
    }
    else
    {
      return false;
    }
  },

  clearPatientDataContainer: function()
  {
    if (this.patientDataContainer.hasComponents())
    {
      this.patientDataContainer.removeAll();
    }
  },

  refreshPatientDataContainer: function()
  {
    var self = this;
    var utils = tm.views.medications.MedicationUtils;
    this.clearPatientDataContainer();

    var patientReferenceWeight = new tm.jquery.Container({html: "", flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")}); //layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-end")
    var patientBodyArea = new tm.jquery.Container({html: "", flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")});
    var patientHeight = new tm.jquery.Container({html: "", flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")});
    var patientAllergies = new tm.jquery.Container({html: "", flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")});

    if (!tm.jquery.Utils.isEmpty(this.referenceWeight))
    {
      patientReferenceWeight.setHtml("<span class= TextLabel>" + this.getDictionary('reference.weight') + ": " + "</span>" +
          "<span class = TextData>" + utils.getFormattedDecimalNumber(utils.doubleToString(this.referenceWeight)) + "</span>" +
          "<span class = TextUnit>" + " kg" + "</span>");
      this.patientDataContainer.add(patientReferenceWeight);
    }
    if (!tm.jquery.Utils.isEmpty(this.patientData))
    {
      if (this.patientData.heightInCm && this.referenceWeight)
      {
        var patientBodySurfaceArea = utils.calculateBodySurfaceArea(this.patientData.heightInCm, this.referenceWeight);
        patientBodyArea.setHtml("<span class= TextLabel>" + this.getDictionary('body.surface') + ": " + "</span>" +
            "<span class = TextData>" + utils.getFormattedDecimalNumber(utils.doubleToString(patientBodySurfaceArea, 'n3')) + "</span>" +
            "<span class = TextUnit>" + " m2" + "</span>");
        this.patientDataContainer.add(patientBodyArea);
      }
      if (!tm.jquery.Utils.isEmpty(this.patientData.heightInCm))
      {
        patientHeight.setHtml("<span class= TextLabel>" + this.getDictionary('height') + ": " + "</span>" +
            "<span class = TextData>" + utils.getFormattedDecimalNumber(utils.doubleToString(this.patientData.heightInCm)) + "</span>" +
            "<span class = TextUnit>" + " cm" + "</span>");
        this.patientDataContainer.add(patientHeight);
      }
      if (!tm.jquery.Utils.isEmpty(this.patientData.allergies) && this.patientData.allergies.length > 0)
      {
        var allergiesString = "<span class = TextData>";
        this.patientData.allergies.forEach(function(allergie)
        {
          allergiesString += allergie.name;
          if (self.patientData.allergies.indexOf(allergie) != self.patientData.allergies.length - 1)
          {
            allergiesString += ", ";
          }
        });
        allergiesString += "</span>";
        patientAllergies.setHtml("<span class= TextLabel>" + this.getDictionary('known.allergies') + ": " + "</span>" + allergiesString);
        this.patientDataContainer.add(patientAllergies);
      }
      if (this.getTimelineContainer())
      {
        if (this.getTimelineContainer().getAdditionalWarnings() && this.getTimelineContainer().getAdditionalWarnings().hasAdditionalWarnings())
        {
          var patientAdditionalWarningsIcon = new tm.jquery.Container({
            cls: "high-alert-icon",
            flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
          });
          var patientAdditionalWarnings = new tm.jquery.Container({
            flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
          });
          patientAdditionalWarnings.setHtml("<span class= TextLabel>" + this.getDictionary('warning') + ": " + "</span>"
              + "<span class= TextData>"
              + this.getDictionary('therapy.risk.warning') + "</span>");
          this.patientDataContainer.add(patientAdditionalWarningsIcon);
          this.patientDataContainer.add(patientAdditionalWarnings);
        }
      }
    }

    var bnfMaximumSum = this.bnfMaximumSum;
    if (!tm.jquery.Utils.isEmpty(bnfMaximumSum) && bnfMaximumSum > 0)
    {
      var tooltip = this.getAppFactory().createDefaultPopoverTooltip(null, null, new tm.jquery.Label({
        cls: 'bnf-tooltip',
        text: bnfMaximumSum + "% of antipsychotic BNF maximum",
        alignSelf: "center"
      }));
      tooltip.setPlacement("bottom");

      var bnfMaximumSumContainer = new tm.jquery.Container({
        html: tm.views.medications.MedicationUtils.createBnfPercentageInfoHtml(self, bnfMaximumSum, true),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        cursor: 'pointer'
      });

      bnfMaximumSumContainer.setTooltip(tooltip);
      this.patientDataContainer.add(bnfMaximumSumContainer);
    }

    this.patientDataContainer.repaint();
  },

  /** private methods */
  _loadTherapyViewPatientData: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var patientId = this.getPatientId();
    var params = {
      patientId: patientId
    };
    var url = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_LOAD_THERAPY_VIEW_PATIENT_DATA;
    self.loadViewData(url, params, null, function(data)
    {
      if (patientId == self.getPatientId())
      {
        self.patientData = data.patientData;   //PatientDataForMedicationsDto.java
        self.roundsInterval = data.roundsInterval;
        self.administrationTiming = data.administrationTiming;
        self.customGroups = data.customGroups;
        self.referenceWeight = data.referenceWeight;
        self.patientLastLinkName = data.lastLinkName;
        self.recentHospitalization = data.recentHospitalization;
        self._hideToolbarGlassLayer();
        self._abortPresentDataTask();
        self._presentDataConditionalTask = appFactory.createConditionTask(
            function()
            {
              self._presentDataConditionalTask = null;
              self._presentData();
            },
            function()
            {
              return self.isRendered(true);
            },
            50, 300
        );

        var viewHubNotifier = self.getHubNotifier();
        var hubAction = tm.views.medications.TherapyView.THERAPY_PREPARE_VIEW_HUB;
        viewHubNotifier.actionEnded(hubAction);
      }
    });
  },

  _refreshTimeOffsetFromServer: function()
  {
    var url = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_TIME_OFFSET;
    this.loadViewData(url, null, null, function(timeOffset)
    {
      CurrentTime.setOffset(timeOffset);
    });
  },

  _loadCareProfessionals: function()
  {
    var self = this;
    var careProfessionalsUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_CARE_PROFESSIONALS;
    this.loadViewData(careProfessionalsUrl, null, null, function(data)
    {
      self.careProfessionals.length = 0;
      $.merge(self.careProfessionals, data);
    });
  },

  _loadRoutes: function()
  {
    var self = this;
    var getRoutesUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_ROUTES;
    this.loadViewData(getRoutesUrl, null, null, function(data)
    {
      self.routes.length = 0;
      $.merge(self.routes, data);
    });
  },

  _loadUnits: function(callback)
  {
    var self = this;
    var getUnitsUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_UNITS;
    this.loadViewData(getUnitsUrl, null, null, function(data)
    {
      self.units.length = 0;
      $.merge(self.units, data);
      callback();
    });
  },

  _loadProblemDescriptionNamedIdentitiesMap: function()
  {
    var self = this;

    var getDataUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_PROBLEM_DESC_NAMED_IDENTITIES;

    var params = {
      language: this.getViewLanguage()
    };

    this.loadViewData(getDataUrl, params, null, function(data)
    {
      if (!tm.jquery.Utils.isEmpty(data))
      {
        self.problemDescriptionNamedIdentitiesMap = data;
      }
      else
      {
        self.problemDescriptionNamedIdentitiesMap = {};
      }
    });
  },

  loadTherapyChangeReasonTypeMap: function(callback)
  {
    var self = this;

    this.getRestApi().loadTherapyChangeReasonTypeMap(true).then(
        function onSuccess(data)
        {
          self.therapyChangeReasonTypeMap = data;
          if (callback) callback(data);
        }
    );
  },

  getMedicationSupplyCandidates: function(medicationId, routeId, callback)
  {
    var url = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_SUPPLY_CANDIDATES;

    var params = {
      medicationId: medicationId,
      routeId: routeId
    };

    this.loadViewData(url, params, null, function(data)
    {
      if (!tm.jquery.Utils.isEmpty(data))
      {
        callback(data);
      }
      else
      {
        callback([]);
      }
    });
  },

  _buildFlowGui: function()
  {
    var self = this;
    this.content = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    this.header = new tm.jquery.Container({
      cls: 'header',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      scrollable: 'visible'
    });
    var headerContainer = new app.views.common.Toolbar({
      cls: 'app-views-toolbar portlet-header',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
    });
    headerContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      if (tm.jquery.Utils.isEmpty(self.getPatientData()))
      {
        self._showToolbarGlassLayer();
      }
    });

    this.patientDataContainer = new tm.jquery.Container({
      cls: 'patient-data-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 10),
      height: 20
    });

    var viewButtons = [];

    if (this.getTherapyAuthority().isGridViewEnabled())
    {
      this.flowGridTypeButton = new tm.jquery.Button({
        cls: 'flow-grid-icon btn-flat',
        type: 'GRID',
        tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("therapies"), "bottom")
      });
      this.flowGridTypeButton.addTestAttribute('view-selection-grid-button');
      viewButtons.add(this.flowGridTypeButton);
    }
    if (this.getTherapyAuthority().isTimelineViewEnabled())
    {
      this.timelineTypeButton = new tm.jquery.Button({
        cls: 'timeline-icon btn-flat',
        type: 'TIMELINE',
        tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("administrations"), "bottom")
      });
      this.timelineTypeButton.addTestAttribute('view-selection-timeline-button');
      viewButtons.add(this.timelineTypeButton);
    }
    if (this.getTherapyAuthority().isPharmacistReviewViewEnabled())
    {
      this.pharmacistTypeButton = new tm.jquery.Button({
        cls: 'pharmacist-review btn-flat',
        type: 'PHARMACIST',
        tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("pharmacists.review"), "bottom")
      });
      this.pharmacistTypeButton.addTestAttribute('view-selection-pharmacist-button');
      viewButtons.add(this.pharmacistTypeButton);
    }
    if (this.getTherapyAuthority().isMedicationSummaryViewEnabled())
    {
      this.reconciliationSummaryTypeButton = new tm.jquery.Button({
        cls: 'reconciliation-summary-icon btn-flat',
        type: 'SUMMARY',
        tooltip: this.getAppFactory().createDefaultHintTooltip(
            this.getDictionary("medication.reconciliation.summary"),
            "bottom")
      });
      this.reconciliationSummaryTypeButton.addTestAttribute('view-selection-reconciliation-button');
      viewButtons.add(this.reconciliationSummaryTypeButton);
    }
    if (this.getTherapyAuthority().isMedicationDocumentViewEnabled())
    {
      this.therapyDocumentationTypeButton = new tm.jquery.Button({
        cls: 'therapy-documentation-icon btn-flat',
        type: 'DOCUMENTATION',
        tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("documentation"), "bottom")
      });
      this.therapyDocumentationTypeButton.addTestAttribute('view-selection-documentation-button');
      viewButtons.add(this.therapyDocumentationTypeButton);
    }

    this.displayTypeButtonGroup = new tm.jquery.ButtonGroup({
      cls: "btn-group-flat header",
      width: 246,
      orientation: "horizontal",
      type: "radio",
      buttons: viewButtons
    });

    this.displayTypeButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      component.setEnabled(false); // prevent the user from switching too fast while we set up the new view
      tm.jquery.ComponentUtils.hideAllDropDownMenus();
      tm.jquery.ComponentUtils.hideAllTooltips();
      tm.jquery.ComponentUtils.hideAllDialogs();
      var eventData = componentEvent.getEventData();
      var newSelectedButton = eventData.newSelectedButton;
      if (newSelectedButton.type == 'GRID')
      {
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setViewType("GRID");
        self._buildFlowGridGui();
        var searchDate = self._getDefaultSearchDate(self.dayCount);
        self.therapyGrid.paintGrid(self.dayCount, searchDate, self.groupField, self.therapySortTypeEnum);
        self._addGridEvents();
        self.content.repaint();
        self.header.repaint();
      }
      else if (newSelectedButton.type == 'TIMELINE')
      {
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setViewType("TIMELINE");
        self._buildTimelineGui();
        self.timelineContainer.reloadTimelines(true, self.therapySortTypeEnum);
        self.content.repaint();
        self.header.repaint();
      }
      else if (newSelectedButton.type == 'PHARMACIST')
      {
        /* switch to the Pharmacist's review mode */
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setViewType("PHARMACIST");
        self._buildPharmacistReviewGui();
        self.content.repaint();
        self.header.repaint();
        self.getAppFactory().createConditionTask(
            function()
            {
              self.pharmacistReviewContainer.refreshData()
            },
            function(task)
            {
              if (self.pharmacistReviewContainer == null)
              {
                // abort the taks, the pharmacistReviewContainer was removed - which happens when you switch views
                task.abort();
                return;
              }
              return self.isRendered() && self.pharmacistReviewContainer.isRendered();
            },
            50, 10);
      }
      else if (newSelectedButton.type == 'SUMMARY')
      {
        /* switch to the Medication Reconciliation Summary mode */
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setViewType("SUMMARY");
        self._buildReconciliationSummaryGui();
        self.content.repaint();
        self.header.repaint();
        self.getAppFactory().createConditionTask(
            function()
            {
              self.summaryContainer.refreshData()
            },
            function()
            {
              return self.isRendered() && self.summaryContainer && self.summaryContainer.isRendered();
            },
            70, 10);
      }
      else if (newSelectedButton.type == 'DOCUMENTATION')
      {
        /* switch to the Documentation view mode */
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setViewType("DOCUMENTATION");
        self._buildTherapyDocumentationGui();
        self.content.repaint();
        self.header.repaint();
        self.documentationContainer.refreshData();
      }
      self._saveContext();
      component.setEnabled(true);
    });

    headerContainer.add(this.displayTypeButtonGroup);
    headerContainer.add(this.header);

    this.add(headerContainer);
    this.add(this.patientDataContainer);
    this.add(this.content);

    this.actionsHeader = new tm.views.medications.TherapyOverviewHeader({
      view: this,
      therapySortTypeEnum: this.therapySortTypeEnum,
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      timelineFilterChangedFunction: function(routes, customGroups, refreshTimelines)
      {
        if (!tm.jquery.Utils.isEmpty(self.timelineContainer))
        {
          self.timelineContainer.setTimelineFilter(routes, customGroups, refreshTimelines);
        }
        else if (!tm.jquery.Utils.isEmpty(self.pharmacistReviewContainer))
        {
          self.pharmacistReviewContainer.setRoutesAndCustomGroupsFilter(routes, customGroups, refreshTimelines);
        }
      },
      timelineDateSelectorChangedFunction: function(newDate)
      {
        self.timelineContainer.setShownTherapies("customDateTherapies");
        self.timelineContainer.setTimelineDate(newDate);
      },
      medicationIdentifierScannedFunction: function(barcodeTaskSearch, barcode)
      {
        self._handleBarcodeScanned(barcodeTaskSearch, barcode);
      }
    });
    this.header.add(this.actionsHeader);
    this.actionsHeader.showRecentHospitalizationButton(
        this.recentHospitalization && this.getTherapyAuthority().isManageInpatientPrescriptionsAllowed());

    this._setViewType(this.context ? this.context.viewType : null);
  },

  _setViewType: function(viewType) //GRID, TIMELINE, PHARMACIST
  {
    if (viewType === 'GRID' && this.getTherapyAuthority().isGridViewEnabled())
    {
      this.actionsHeader.setViewType(viewType);
      this.displayTypeButtonGroup.setSelection([this.flowGridTypeButton], true);
      this.flowGridTypeButton.focus();
      this._buildFlowGridGui();
    }
    else if (viewType === 'PHARMACIST' && this.getTherapyAuthority().isPharmacistReviewViewEnabled())
    {
      this.actionsHeader.setViewType(viewType);
      this.displayTypeButtonGroup.setSelection([this.pharmacistTypeButton], true);
      this.pharmacistTypeButton.focus();
      this._buildPharmacistReviewGui();
    }
    else if (viewType === 'SUMMARY' && this.getTherapyAuthority().isMedicationSummaryViewEnabled())
    {
      this.actionsHeader.setViewType(viewType);
      this.displayTypeButtonGroup.setSelection([this.reconciliationSummaryTypeButton], true);
      this.reconciliationSummaryTypeButton.focus();
      this._buildReconciliationSummaryGui();
    }
    else if (viewType === 'DOCUMENTATION' && this.getTherapyAuthority().isMedicationDocumentViewEnabled())
    {
      this.actionsHeader.setViewType(viewType);
      this.displayTypeButtonGroup.setSelection([this.therapyDocumentationTypeButton], true);
      this.therapyDocumentationTypeButton.focus();
      this._buildTherapyDocumentationGui();
    }
    else if (viewType === 'TIMELINE' && this.getTherapyAuthority().isTimelineViewEnabled())
    {
      // default to TIMELINE
      this.actionsHeader.setViewType('TIMELINE');
      this.displayTypeButtonGroup.setSelection([this.timelineTypeButton], true);
      this.timelineTypeButton.focus();
      this._buildTimelineGui()
    }
    else
    {
      var possibleDefault = this._getFirstEnabledViewType();
      if (possibleDefault)
      {
        this._setViewType(possibleDefault);
      }
    }
  },

  /**
   * Finds the first possible view type (if any), based on available view types and the predefined view type priority.
   * @returns {String|null}
   * @private
   */
  _getFirstEnabledViewType: function()
  {
    var viewTypesByPriority = ['TIMELINE', 'GRID', 'DOCUMENTATION', 'PHARMACIST', 'SUMMARY'];
    var buttons = this.displayTypeButtonGroup.getButtons();

    for (var idx = 0; idx < viewTypesByPriority.length; idx++)
    {
      var currentViewType = viewTypesByPriority[idx];
      var viewTypeButton = buttons.find(function isButtonVisibleAndViewTypeMatches(button)
      {
        return !button.isHidden() && button.type === currentViewType;
      });

      if (viewTypeButton)
      {
        return viewTypeButton.type;
      }
    }

    return null;
  },

  _buildFlowGridGui: function()
  {
    this.timelineContainer = null;
    this.pharmacistReviewContainer = null;
    this.summaryContainer = null;
    this.documentationContainer = null;
    this.content.removeAll(true);
    this.therapyGrid = new tm.views.medications.TherapyFlowGrid({
      view: this,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    this.content.add(this.therapyGrid);
    this._addGridHeaderEvents();

    if (this.context)
    {
      this.dayCount = this.actionsHeader.setNumberOfDaysMode(this.context.numberOfDaysMode);
      if (!this.dayCount)
      {
        this.dayCount = 3;
      }
    }
    else
    {
      this.dayCount = 3;
    }

    this._setGroupModeEnum();
    this._setTherapySortTypeEnum();
  },

  _buildTimelineGui: function()
  {
    var self = this;
    if (tm.jquery.Utils.isEmpty(this.getTherapyChangeReasonTypeMap()))
    {
      this.loadTherapyChangeReasonTypeMap();
    }
    this.therapyGrid = null;
    this.pharmacistReviewContainer = null;
    this.summaryContainer = null;
    this.documentationContainer = null;
    this.content.removeAll(true);

    this.actionsHeader.addGroupingButtonGroupAction(function(groupField)
    {
      self.groupField = groupField;
      self.timelineContainer.setGrouping(groupField);
      self._saveContext();
    });

    this.actionsHeader.addSortButtonGroupAction(function(therapySortTypeEnum, showTherapies)
    {
      if (therapySortTypeEnum == self.therapySortTypeEnum)
      {
        self.timelineContainer.setShownTherapies(showTherapies.value);
        self.timelineContainer.reloadTimelines(false, self.therapySortTypeEnum);
        self._saveContext();
      }
      else
      {
        self.therapySortTypeEnum = therapySortTypeEnum;
        self.timelineContainer.reloadTimelines(false, therapySortTypeEnum);
        self._saveContext();
      }
    });

    this._setGroupModeEnum();
    this._setTherapySortTypeEnum();

    this.timelineContainer = new tm.views.medications.timeline.TherapyTimelineContainer({
      view: this,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      patientId: this.patientId,
      groupField: this.groupField,
      therapyTimelineRowsLoadedFunction: function(therapyTimelineRows)
      {
        therapyTimelineRows = tm.jquery.Utils.isEmpty(therapyTimelineRows) ? [] : therapyTimelineRows;

        var groups = [];
        var routes = [];

        therapyTimelineRows.forEach(function(row)
        {
          if (!tm.jquery.Utils.isEmpty(row.customGroup) && !groups.contains(row.customGroup)) groups.push(row.customGroup);

          if (!tm.jquery.Utils.isEmpty(row.therapy) && !tm.jquery.Utils.isEmpty(row.therapy.getRoutes()))
          {
            row.therapy.routes.forEach(function(route)
            {
              if (!routes.contains(route.name))
              {
                routes.push(route.name);
              }
            });
          }

          if (row.currentStartingDevice)
          {
            row.currentStartingDevice = new app.views.medications.common.dto.OxygenStartingDevice(row.currentStartingDevice);
          }
        });

        self.actionsHeader.setupTimelineFilter(routes, groups);
        self.actionsHeader.requestBarcodeFieldFocus();
      }
    });
    this.content.add(this.timelineContainer);
  },
  _buildPharmacistReviewGui: function()
  {
    var self = this;

    this.therapyGrid = null;
    this.timelineContainer = null;
    this.summaryContainer = null;
    this.documentationContainer = null;
    this.content.removeAll(true);

    this._setGroupModeEnum();
    this._setTherapySortTypeEnum();

    if (tm.jquery.Utils.isEmpty(this.getProblemDescriptionNamedIdentitiesMap()))
    {
      this._loadProblemDescriptionNamedIdentitiesMap();
    }

    this.pharmacistReviewContainer = new tm.views.medications.pharmacists.ReviewView({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this,
      therapyDataLoadedCallback: function(data)
      {
        data = tm.jquery.Utils.isEmpty(data) ? {} : data;
        data.therapyRows = tm.jquery.Utils.isEmpty(data.therapyRows) ? [] : data.therapyRows;

        var groups = [];
        var routes = [];

        data.therapyRows.forEach(function(row)
        {
          if (!tm.jquery.Utils.isEmpty(row.customGroup) && !groups.contains(row.customGroup)) groups.push(row.customGroup);
          if (!tm.jquery.Utils.isEmpty(row.route) && !routes.contains(row.route)) routes.push(row.route);
        });

        self.actionsHeader.setupTimelineFilter(routes, groups);
      }
    });

    this.actionsHeader.addGroupingButtonGroupAction(function(groupField)
    {
      self.groupField = groupField;
      self.pharmacistReviewContainer.refreshTherapies();
      self._saveContext();
    });

    this.actionsHeader.addSortButtonGroupAction(function(therapySortTypeEnum, showTherapies)
    {
      if (therapySortTypeEnum == self.therapySortTypeEnum)
      {
        self.pharmacistReviewContainer.setShownTherapies(showTherapies.value);
        self.pharmacistReviewContainer.refreshTherapies();
        self._saveContext();
      }
      else
      {
        self.therapySortTypeEnum = therapySortTypeEnum;
        self.pharmacistReviewContainer.refreshTherapies();
        self._saveContext();
      }
    });

    this.content.add(this.pharmacistReviewContainer);
  },

  _buildReconciliationSummaryGui: function()
  {
    var self = this;

    if (tm.jquery.Utils.isEmpty(this.getTherapyChangeReasonTypeMap()))
    {
      this.loadTherapyChangeReasonTypeMap();
    }

    this.therapyGrid = null;
    this.timelineContainer = null;
    this.pharmacistReviewContainer = null;
    this.documentationContainer = null;
    this.content.removeAll(true);

    this.summaryContainer = new app.views.medications.reconciliation.SummaryView({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this
    });

    this.content.add(this.summaryContainer);
  },

  _buildTherapyDocumentationGui: function()
  {
    if (tm.jquery.Utils.isEmpty(this.documentationContainer))
    {
      var self = this;

      this.therapyGrid = null;
      this.timelineContainer = null;
      this.pharmacistReviewContainer = null;
      this.summaryContainer = null;
      this.content.removeAll(true);

      this.documentationContainer = new app.views.medications.documentation.TherapyDocumentationView({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        view: this
      });

      this.content.add(this.documentationContainer);
    }
  },

  _setTherapySortTypeEnum: function()
  {
    if (this.context)
    {
      this.therapySortTypeEnum = this.actionsHeader.setTherapySortType(this.context.therapySortTypeEnum);
      if (tm.jquery.Utils.isEmpty(this.therapySortTypeEnum))
      {
        this.therapySortTypeEnum = this.actionsHeader.setTherapySortType(app.views.medications.TherapyEnums.therapySortTypeEnum.DESCRIPTION_ASC);
      }
    }
    else
    {
      this.therapySortTypeEnum = this.actionsHeader.setTherapySortType(app.views.medications.TherapyEnums.therapySortTypeEnum.DESCRIPTION_ASC);
    }
  },

  _setGroupModeEnum: function()
  {
    if (this.context)
    {
      this.groupField = this.actionsHeader.setGroupMode(this.context.groupByMode);
    }
  },

  _buildOrderingGui: function(allowPastOrdering, assertBaselineInfusion)
  {
    var orderingContainer = new app.views.medications.ordering.MedicationsOrderingContainer({
      view: this,
      patientId: this.patientId,
      presetMedicationId: this.presetMedicationId ? this.presetMedicationId : null,
      isPastMode: allowPastOrdering,
      assertBaselineInfusion: assertBaselineInfusion,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    this.add(orderingContainer, {region: 'center'});
    var footer = this._buildDialogFooter(orderingContainer);
    this.add(footer, {region: 'south', height: 40});

    this.repaint();
  },

  _buildEditGui: function(isPastEditMode)
  {
    //this.therapyToEdit must be set
    var therapyToEdit = this.getTherapyToEdit();
    var self = this;
    var appFactory = this.getAppFactory();
    var ingredientsMedicationLoader = new app.views.medications.common.TherapyMedicationDataLoader({view: this});
    var editContainer;

    ingredientsMedicationLoader.load(therapyToEdit).then(function showDialog(medicationData)
    {
      if (ingredientsMedicationLoader.isMedicationNoLongerAvailable(therapyToEdit, medicationData))
      {
        var message = self.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            self.getDictionary('stop.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 500, 160).show();
      }
      else if (therapyToEdit.isOrderTypeComplex())
      {
        editContainer = new app.views.medications.ordering.ComplexTherapyEditContainer({
          view: self,
          startProcessOnEnter: true,
          therapy: therapyToEdit,
          isPastMode: isPastEditMode,
          medicationData: medicationData,
          flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
        });
        addAndRepaint.call(self, editContainer);
      }
      else
      {
        editContainer = new app.views.medications.ordering.SimpleTherapyEditContainer({
          view: self,
          startProcessOnEnter: true,
          therapy: therapyToEdit,
          isPastMode: isPastEditMode,
          medicationData: medicationData
        });
        addAndRepaint.call(this, editContainer);
      }
    });

    function addAndRepaint(editContainer)
    {
      this.add(editContainer, {region: 'center'});
      var footer = this._buildDialogFooter(editContainer);
      this.add(footer, {region: 'south', height: 40});
      this.repaint();
    }
  },

  _buildDialogFooter: function(dialogContainer)
  {
    var self = this;
    var footer = new tm.jquery.Container({
      height: 50,
      style: "background-color: #f7f7f7; border-top: 1px solid #ebebeb; padding-right: 20px;",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 10)
    });

    footer.saveButton = new tm.jquery.Button({
      text: this.getDictionary("confirm"),
      tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("save.therapy"), "left"),
      handler: function()
      {
        footer.saveButton.setEnabled(false);
        dialogContainer.processResultData(function(data)
        {
          if (data.success)
          {
            self.sendAction("closeTherapyDialog", {reason: "save"});
          }
          else
          {
            footer.saveButton.setEnabled(true);
          }
        });
      }
    });
    footer.add(footer.saveButton);

    footer.cancelLink = new tm.jquery.Button({
      text: this.getDictionary("cancel"), type: "link",
      handler: function()
      {
        self.sendAction("closeTherapyDialog", {reason: "cancel"});
      }
    });
    footer.add(footer.cancelLink);
    return footer;
  },

  _addGridHeaderEvents: function()
  {
    var self = this;
    this.actionsHeader.addPreviousButtonAction(function()
    {
      tm.jquery.ComponentUtils.hideAllDialogs();
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self);
      tm.jquery.ComponentUtils.hideAllTooltips(self);
      self.therapyGrid.changeSearchDate(false);
    });
    this.actionsHeader.addNextButtonAction(function()
    {
      tm.jquery.ComponentUtils.hideAllDialogs();
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self);
      tm.jquery.ComponentUtils.hideAllTooltips(self);
      self.therapyGrid.changeSearchDate(true);
    });
    this.actionsHeader.addDayCountButtonGroupAction(function(dayCount)
    {
      self.dayCount = dayCount;
      var searchDate = self._getDefaultSearchDate(dayCount);
      self.therapyGrid.repaintGrid(dayCount, searchDate, self.therapySortTypeEnum);
      self._addGridEvents();
      self._saveContext();
    });
    this.actionsHeader.addGroupingButtonGroupAction(function(groupField)
    {
      self.groupField = groupField;
      self.therapyGrid.setGrouping(groupField);
      self._saveContext();
    });

    this.actionsHeader.addSortButtonGroupAction(function(therapySortTypeEnum)
    {
      self.therapySortTypeEnum = therapySortTypeEnum;
      var searchDate = self._getDefaultSearchDate(self.dayCount);
      self.therapyGrid.repaintGrid(self.dayCount, searchDate, therapySortTypeEnum);
      self._addGridEvents();
      self._saveContext();
    });
  },

  _addGridEvents: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    this.therapyGrid.grid.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      appFactory.createConditionTask(
          function()
          {
            self.therapyGrid.reloadGridData();
          },
          function(task)
          {
            if (self.therapyGrid == null || !component.isRendered())
            {
              // abort the task if the grid was destroyed (happens when fast switching back and forth)
              task.abort();
              return;
            }
            return component.getPlugin() != null && self.getPatientData() != null;
          },
          20, 1000
      );
    });
  },

  _saveContext: function()
  {
    if (!this.context)
    {
      this.context = {};
    }
    var displayTypeSelection = this.displayTypeButtonGroup.getSelection().length > 0 ?
        this.displayTypeButtonGroup.getSelection()[0].type :
        null;

    this.context.viewType = displayTypeSelection;
    var headerContext = this.actionsHeader.getFilterContext();
    this.context.groupByMode = headerContext.groupByMode;
    this.context.therapySortTypeEnum = this.therapySortTypeEnum;
    if (this.context.viewType == "GRID")
    {
      this.context.numberOfDaysMode = headerContext.numberOfDaysMode;
    }

    this.sendAction("SAVE_CONTEXT", {contextData: JSON.stringify(this.context)});
  },

  _getDefaultSearchDate: function(dayCount)
  {
    var searchDate = CurrentTime.get();
    searchDate.setDate(searchDate.getDate() - dayCount + 1);
    return searchDate;
  },

  refreshTherapies: function(repaintTimeline)
  {
    if (this.therapyGrid)
    {
      this.therapyGrid.reloadGridData();
    }
    else if (this.timelineContainer)
    {
      this.timelineContainer.reloadTimelines(repaintTimeline, this.therapySortTypeEnum);
    }
    else if (this.pharmacistReviewContainer)
    {
      this.pharmacistReviewContainer.refreshTherapies();
    }
    this._saveContext();
  },

  /** public methods */
  tagTherapyForPrescription: function(patientId, centralCaseId, compositionId, ehrOrderName)
  {
    var self = this;
    this.showLoaderMask();
    var tagTherapiesUrl = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_TAG_THERAPY_FOR_PRESCRIPTION;

    var params = {
      patientId: patientId,
      centralCaseId: centralCaseId,
      compositionId: compositionId,
      ehrOrderName: ehrOrderName
    };
    this.loadPostViewData(tagTherapiesUrl, params, null, function()
    {
      self.hideLoaderMask();
      self.refreshTherapies(false);
    });
  },

  untagTherapyForPrescription: function(patientId, centralCaseId, compositionId, ehrOrderName)
  {
    var self = this;
    this.showLoaderMask();
    var untagTherapiesUrl = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_UNTAG_THERAPY_FOR_PRESCRIPTION;

    var params = {
      patientId: patientId,
      centralCaseId: centralCaseId,
      compositionId: compositionId,
      ehrOrderName: ehrOrderName
    };
    this.loadPostViewData(untagTherapiesUrl, params, null, function()
    {
      self.hideLoaderMask();
      self.refreshTherapies(false);
    });
  },

  showEditConsecutiveDaysDialog: function(pastDaysOfTherapy, ehrCompositionId, ehrOrderName)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        self.getDictionary("consecutive.days.edit"),
        null,
        new app.views.medications.ordering.EditPastDaysOfTherapyContainer({
          view: self,
          startProcessOnEnter: true,
          pastDaysOfTherapy: pastDaysOfTherapy,
          ehrCompositionId: ehrCompositionId,
          ehrOrderName: ehrOrderName
        }),
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(false);
          }
        },
        210,
        130
    );
    dialog.setHideOnEscape(false);
    dialog.show();
  },

  openReferenceWeightDialog: function(weight, callback)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        this.getDictionary("reference.weight"),
        null,
        new app.views.medications.therapy.ReferenceWeightPane({
          cls: 'therapy-reference-weight',
          view: self,
          startProcessOnEnter: true,
          weight: weight
        }),
        function(resultData)
        {
          if (resultData)
          {
            self.referenceWeight = resultData.value;
            self.refreshTherapies(false);
            self.refreshPatientDataContainer();
            if (callback)
            {
              callback();
            }
          }
        },
        180,
        130
    );
    dialog.addTestAttribute('reference-weight-dialog');
    dialog.setHideOnEscape(false);
    dialog.show();
  },

  openMedicationOrderingDialog: function(presetTherapies)
  {
    var self = this;
    if (this.referenceWeight)
    {
      this._openMedicationOrderingDialog(presetTherapies);
    }
    else
    {
      var patientWeight = this.patientData ? this.patientData.weightInKg : null;
      this.openReferenceWeightDialog(patientWeight, function()
      {
        self._openMedicationOrderingDialog(presetTherapies);
      });
    }
  },

  openOutpatientOrderingDialog: function()
  {
    var self = this;
    if (this.referenceWeight)
    {
      this._openOutpatientOrderingDialog();
    }
    else
    {
      var patientWeight = this.patientData ? this.patientData.weightInKg : null;
      this.openReferenceWeightDialog(patientWeight, function()
      {
        self._openOutpatientOrderingDialog();
      });
    }
  },

  openT2T3OrderingDialog: function(mentalHealthReportType)
  {
    var centralCaseData = this.getCentralCaseData();

    if (tm.jquery.Utils.isEmpty(centralCaseData) || tm.jquery.Utils.isEmpty(centralCaseData.outpatient))
    {
      var errorSystemDialog = this.getAppFactory().createErrorSystemDialog(this.getDictionary("patient.not.hospitalised") + ".", 400, 122);
      errorSystemDialog.show();
      return;
    }

    this._openT2T3OrderingDialog(mentalHealthReportType);
  },

  _openMedicationOrderingDialog: function(presetTherapies)
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var orderingDialog = appFactory.createDataEntryDialog(
        self.getDictionary("medications.ordering"),
        null,
        new app.views.medications.ordering.MedicationsOrderingContainer({
          view: self,
          patientId: self.patientId,
          presetTherapies: presetTherapies
        }),
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(true);
          }
        },
        this._getOrderingDialogWidth(),
        this._getOrderingDialogHeight()
    );
    orderingDialog.setContainmentElement(this.getDom());
    orderingDialog.addTestAttribute('medication-ordering-dialog');
    orderingDialog.setFitSize(true);
    orderingDialog.setHideOnEscape(false);
    var confirmButton = orderingDialog.getBody().footer.confirmButton;
    confirmButton.setText(this.getDictionary("do.order"));
    orderingDialog.show();
  },

  _openOutpatientOrderingDialog: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var content = new app.views.medications.outpatient.OutpatientOrderingContainer({
      view: self,
      patientId: self.patientId
    });

    this.actionCallbackListener = content;

    var dialog = appFactory.createDataEntryDialog(
        self.getDictionary("outpatient.prescription"),
        null,
        content,
        dialogResultCallback,
        content.getDefaultWidth(),
        content.getDefaultHeight()
    );
    dialog.setContainmentElement(this.getDom());
    dialog.setHideOnEscape(false);

    tm.views.medications.MedicationUtils.attachOutpatientOrderingDialogFooterButtons(this, dialog, dialogResultCallback);

    dialog.show();

    function dialogResultCallback(resultData)
    {
      if (resultData && resultData.success && !tm.jquery.Utils.isEmpty(self.documentationContainer))
      {
        self.documentationContainer.refreshData();
      }
      self.actionCallbackListener = null;
    }
  },

  _openT2T3OrderingDialog: function(mentalHealthReportType)
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var content = new app.views.medications.mentalHealth.T2T3OrderingContainer({
      view: self,
      patientId: self.patientId,
      reportType: mentalHealthReportType
    });

    this.actionCallbackListener = content;
    var dialog = appFactory.createDataEntryDialog(
        mentalHealthReportType == app.views.medications.TherapyEnums.mentalHealthDocumentType.T2 ? 'T2' : 'T3',
        null,
        content,
        function()
        {
          self.actionCallbackListener = null;
        },
        content.getDefaultWidth(),
        content.getDefaultHeight()
    );
    dialog.setContainmentElement(this.getDom());
    dialog.setHideOnEscape(false);

    var confirmButton = dialog.getBody().getFooter().getConfirmButton();
    confirmButton.setText(this.getDictionary("save"));

    dialog.show();
  },

  _getOrderingDialogWidth: function()
  {
    return $(window).width() - 50;
  },

  _getOrderingDialogHeight: function()
  {
    return $(window).height() - 10;
  },

  /**
   * @param {app.views.medications.common.dto.BarcodeTaskSearch} barcodeTaskSearch
   * @param {String} barcode
   * @private
   */
  _handleBarcodeScanned: function(barcodeTaskSearch, barcode)
  {
    if (barcodeTaskSearch.isTaskFound())
    {
      this.fireEvent(new tm.jquery.ComponentEvent({
        eventType: tm.views.medications.TherapyView.EVENT_TYPE_MEDICATION_BARCODE_SCANNED,
        eventData: {
          barcodeTaskSearch: barcodeTaskSearch,
          barcode: barcode
        }
      }), null);
    }
    else
    {
      this.getAppFactory().createWarningSystemDialog(
          this.getDictionary(barcodeTaskSearch.getFailedMessageKey()), 500, 160).show();
    }
  },

  orderPreviousHospitalizationTherapies: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var patientId = this.patientId;
    var previousTherapiesUrl = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPIES_FOR_PREVIOUS_HOSPITALIZATION;
    var params = {
      patientId: patientId,
      patientHeight: this.getPatientHeightInCm()
    };
    this.loadViewData(previousTherapiesUrl, params, null, function(previousTherapies)
    {
      if (patientId == self.patientId)
      {
        if (previousTherapies && previousTherapies.length != 0)
        {
          for (var i = 0; i < previousTherapies.length; i++)
          {
            previousTherapies[i] = app.views.medications.common.TherapyJsonConverter.convert(previousTherapies[i]);
          }
          self.openMedicationOrderingDialog(previousTherapies);
        }
        else
        {
          appFactory.createWarningSystemDialog(self.getDictionary("no.previous.hospitalization.therapies"), 320, 122).show();
        }
      }
    });
  },

  showEditTherapyDialog: function(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction)
  {
    var enums = app.views.medications.TherapyEnums;

    var editAllowed = true;
    if (!copyTherapy)
    {
      editAllowed = tm.views.medications.MedicationTimingUtils.checkEditAllowed(therapy, this);
    }
    if (editAllowed)
    {
      if (therapy.isOrderTypeOxygen())
      {
        this._showEditOxygenTherapyDialog(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction);
      }
      else if (!therapy.isOrderTypeComplex())
      {
        this._showEditSimpleTherapyDialog(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction);
      }
      else
      {
        this._showEditComplexTherapyDialog(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction);
      }
    }
  },

  _getEditTherapyDialogTitle: function(therapyAlreadyStarted, copyTherapy)
  {
    if (copyTherapy)
    {
      return this.getDictionary("copy.therapy");
    }
    if (therapyAlreadyStarted)
    {
      return this.getDictionary("edit.therapy");
    }
    return this.getDictionary("change.prescription");
  },

  _showEditSimpleTherapyDialog: function(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var therapyAlreadyStarted = this._therapyAlreadyStarted(therapy);
    var ingredientsMedicationLoader = new app.views.medications.common.TherapyMedicationDataLoader({ view: this });

    ingredientsMedicationLoader.load(therapy).then(function showDialog(medicationData)
    {
      if (ingredientsMedicationLoader.isMedicationNoLongerAvailable(therapy, medicationData))
      {
        var message = self.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            self.getDictionary('stop.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 500, 160).show();
      }
      else
      {
        var editContainer = new app.views.medications.ordering.SimpleTherapyEditContainer({
          view: self,
          startProcessOnEnter: true,
          therapy: therapy,
          copyTherapy: copyTherapy,
          therapyAlreadyStarted: therapyAlreadyStarted,
          therapyModifiedInThePast: therapyModifiedInThePast,
          medicationData: medicationData,
          saveTherapyFunction: saveTherapyFunction ? function(result, prescriber)
              {
                saveTherapyFunction(result, editContainer, prescriber)
              } : null
        });
        var title = self._getEditTherapyDialogTitle(therapyAlreadyStarted, copyTherapy);
        var dialog = appFactory.createDataEntryDialog(
            title,
            null,
            editContainer,
            function(resultData)
            {
              if (resultData)
              {
                self.refreshTherapies(false);
              }
            },
            725,
            $(window).height() - 10 < 655 ? $(window).height() - 10 : 655
        );
        dialog.addTestAttribute('simple-therapy-edit-dialog');
        dialog.setContainmentElement(self.getDom());
        dialog.setHideOnEscape(false);
        dialog.show();
      }
    });
  },

  _showEditComplexTherapyDialog: function(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction)
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var therapyAlreadyStarted = this._therapyAlreadyStarted(therapy);
    var ingredientsMedicationLoader = new app.views.medications.common.TherapyMedicationDataLoader({ view: this });

    ingredientsMedicationLoader.load(therapy).then(function showDialog(medicationData)
    {
      if (ingredientsMedicationLoader.isMedicationNoLongerAvailable(therapy, medicationData))
      {
        var message = self.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            self.getDictionary('stop.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 500, 160).show();
      }
      else
      {
        var editContainer = new app.views.medications.ordering.ComplexTherapyEditContainer({
          view: self,
          startProcessOnEnter: true,
          therapy: therapy,
          copyTherapy: copyTherapy,
          medicationData: medicationData,
          therapyAlreadyStarted: therapyAlreadyStarted,
          therapyModifiedInThePast: therapyModifiedInThePast,
          saveTherapyFunction: saveTherapyFunction ?
              function(result, prescriber)
              {
                saveTherapyFunction(result, editContainer, prescriber)
              } : null
        });
        var title = self._getEditTherapyDialogTitle(therapyAlreadyStarted, copyTherapy);
        var dialog = appFactory.createDataEntryDialog(
            title,
            null,
            editContainer,
            function(resultData)
            {
              if (resultData)
              {
                self.refreshTherapies(false);
              }
            },
            715,
            $(window).height() - 10 < 700 ? $(window).height() - 10 : 700
        );
        dialog.addTestAttribute('complex-therapy-edit-dialog');
        dialog.setContainmentElement(self.getDom());
        dialog.setHideOnEscape(false);
        dialog.show();
      }
    });
  },

  /**
   * @param {app.views.medication.common.dto.OxygenTherapy|app.views.medication.common.dto.Therapy} therapy
   * @param {Boolean} copyTherapy
   * @param {Boolean} therapyModifiedInThePast
   * @param {Function|null} saveTherapyFunction
   * @private
   */
  _showEditOxygenTherapyDialog: function(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var therapyAlreadyStarted = this._therapyAlreadyStarted(therapy);
    var dataEntryContainer = new app.views.medications.ordering.oxygen.OxygenTherapyEditDataEntryContainer({
      view: self,
      startProcessOnEnter: true,
      therapy: therapy,
      copyMode: copyTherapy,
      therapyAlreadyStarted: therapyAlreadyStarted,
      saveTherapyFunction: saveTherapyFunction ? function(result, prescriber)
      {
        saveTherapyFunction(result, dataEntryContainer, prescriber)
      } : null
    });

    var title = this._getEditTherapyDialogTitle(therapyAlreadyStarted, copyTherapy);
    var dialog = appFactory.createDataEntryDialog(
        title,
        null,
        dataEntryContainer,
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(false);
          }
        },
        715,
        $(window).height() - 10 < 680 ? $(window).height() - 10 : 680
    );
    dialog.setContainmentElement(this.getDom());
    dialog.setHideOnEscape(false);
    dialog.show();
  },

  _therapyAlreadyStarted: function(therapy)
  {
    return CurrentTime.get() > new Date(therapy.start);
  },

  _abortPresentDataTask: function()
  {
    if (this._presentDataConditionalTask)
    {
      this._presentDataConditionalTask.abort();
      this._presentDataConditionalTask = null;
    }
  },

  /**
   * Will preventing the user from interacting with the toolbar content. It's used to prevent changing the view
   * or trying to open the ordering dialog until the patient data is loaded, since it's vital for other ajax
   * queries to the server.
   *
   * Using the jQuery plugin directly since the {@link app.views.common.AppView#showLoaderMask} imposes a msg.
   * @private
   */
  _showToolbarGlassLayer: function()
  {
    $(this.getDom()).find(".app-views-toolbar.portlet-header").loadmask();
  },

  /**
   * Hides the toolbar's glass layer, if present, effectively enabling interaction with the main toolbar.
   * Should be executed once the core patient data is loaded.
   * @private
   */
  _hideToolbarGlassLayer: function()
  {
    $(this.getDom()).find(".app-views-toolbar.portlet-header").unloadmask();
  },

  loadDoseForms: function()
  {
    var self = this;
    var getDoseFormsUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_DOSE_FORMS;
    this.loadViewData(getDoseFormsUrl, null, null, function(data)
    {
      self.doseForms.length = 0;
      $.merge(self.doseForms, data);
    });
  },

  getNoTherapiesField: function(hidden)
  {
    var self = this;
    var noTherapiesContainer = new tm.jquery.Container({
      cls: 'patient-data-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 10),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      style: 'text-align: center',
      hidden: hidden
    });
    noTherapiesContainer.addTestAttribute('no-therapies-container');
    var infoAlert = new tm.jquery.Alert({
      type: "info",
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      style: "background-color:white; border-color:white; font-size: 18px; color: #dcdcdc",
      content: self.getDictionary("no.therapies.found"),
      closeButton: false
    });
    infoAlert.setAlignSelf("center");
    noTherapiesContainer.add(infoAlert);
    this.noTherapiesField = noTherapiesContainer;
    return noTherapiesContainer;
  },

  updateCurrentBnfMaximumSum: function()
  {
    var patientId = this.getPatientId();
    if (this.getBnfEnabled() && patientId)
    {
      var self = this;
      var url = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_PATIENTS_CURRENT_BNF_MAXIMUM_SUM;
      var params = {patientId: patientId};

      this.loadViewData(url, params, null, function(data)
      {
        if (!tm.jquery.Utils.isEmpty(data))
        {
          self.bnfMaximumSum = data;
          self.refreshPatientDataContainer();
        }
      });
    }
  },

  getCurrentBnfMaximumSum: function()
  {
    return this.bnfMaximumSum;
  },

  onShowRelatedPharmacistReviews: function(therapyData, refreshCallback)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var loadReviewsUrl = self.getViewModuleUrl() +
        tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPY_PHARMACIST_REVIEWS;

    self.showLoaderMask();

    var params = {
      patientId: self.patientId,
      therapyCompositionUid: therapyData.therapy.compositionUid,
      language: self.getViewLanguage()
    };

    self.loadViewData(loadReviewsUrl, params, null, function(data)
    {
      data = tm.jquery.Utils.isEmpty(data) ? [] : data;

      var reviews = data.map(function(item)
      {
        return tm.views.medications.pharmacists.dto.PharmacistMedicationReview.fromJson(item);
      });

      var contentContainer = new app.views.medications.therapy.PharmacistReviewPane({
        view: self,
        reviews: reviews,
        refreshCallbackFunction: refreshCallback
      });

      var closeFooterButtonsContainer = appFactory.createCloseFooterButtonsContainer();

      var dialogContentAndFooterButtonsContainer
          = appFactory.createContentAndFooterButtonsContainer(contentContainer, closeFooterButtonsContainer);

      var dialog = appFactory.createDefaultDialog(
          self.getDictionary("pharmacists.reviews"),
          null,
          dialogContentAndFooterButtonsContainer,
          null,
          jQuery(window).width() - 10 < 700 ? jQuery(window).width() - 10 : 700,
          jQuery(window).height() - 10 < 650 ? jQuery(window).height() - 10 : 650
      );
      contentContainer.setDialog(dialog);

      var closeButton = closeFooterButtonsContainer.getCloseButton();
      closeButton.setHandler(function()
      {
        dialog.hide();
      });
      dialog.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
      {
        elementEvent.stopPropagation();
      });

      dialog.setModal(false);
      dialog.setHideOnEscape(true);
      self.hideLoaderMask();
      dialog.show();
    });
  },

  getPatientId: function()
  {
    return this.patientId;
  },
  getTimelineContainer: function()
  {
    return this.timelineContainer;
  },
  getPatientData: function()
  {
    return this.patientData;   //PatientDataForMedicationsDto.java
  },
  getPatientHeightInCm: function()
  {
    return this.patientData ? this.patientData.heightInCm : null;
  },
  getCareProfessionals: function()
  {
    return this.careProfessionals;
  },
  getDoseForms: function()
  {
    return this.doseForms;
  },
  getRoutes: function()
  {
    return this.routes;
  },
  provideUnits: function(returnUnitsFn)
  {
    var self = this;
    if (this.units.length == 0)
    {
      this._loadUnits(function()
      {
        returnUnitsFn(self.units);
      });
    }
    else
    {
      returnUnitsFn(this.units);
    }
  },
  getAdministrationTiming: function()
  {
    return this.administrationTiming;
  },
  getRoundsInterval: function()
  {
    return this.roundsInterval;
  },
  getCentralCaseData: function()
  {
    return !tm.jquery.Utils.isEmpty(this.patientData) ? this.patientData.centralCaseDto : null;
  },
  getHospitalizationStart: function()
  {
    var centralCaseData = this.getCentralCaseData();
    if (!tm.jquery.Utils.isEmpty(centralCaseData) &&
        centralCaseData.outpatient === false &&
        centralCaseData.centralCaseEffective &&
        centralCaseData.centralCaseEffective.startMillis)
    {
      return centralCaseData.centralCaseEffective.startMillis;
    }
    return null;
  },
  getCurrentUserAsCareProfessional: function()
  {
    return this.userPerson;
  },
  getCareProvider: function()
  {
    var centralCaseData = this.getCentralCaseData();
    return !tm.jquery.Utils.isEmpty(centralCaseData) ? centralCaseData.careProvider : null;
  },
  getCareProviderId: function()
  {
    var careProvider = this.getCareProvider();
    return careProvider != null ? careProvider.id : null;
  },
  getCareProviderName: function()
  {
    var careProvider = this.getCareProvider();
    return careProvider != null ? careProvider.name : null;
  },

  getReferenceWeight: function()
  {
    return this.referenceWeight;
  },
  getContext: function()
  {
    return this.context;
  },
  setContext: function(context)
  {
    this.context = context;
  },
  getOptimizeForPerformance: function()
  {
    return this.optimizeForPerformance;
  },
  isMentalHealthReportEnabled: function()
  {
    return this.mentalHealthReportEnabled === true;
  },
  isAutoAdministrationChartingEnabled: function()
  {
    return this.autoAdministrationCharting === true;
  },
  getPatientNextLinkName: function()
  {
    if (this.patientLastLinkName != null)
    {
      if (this.patientLastLinkName == 'Z')
      {
        return 'A1';
      }
      return String.fromCharCode(this.patientLastLinkName.charCodeAt(0) + 1) + 1;
    }
    return 'A1';
  },
  getPatientLastLinkNamePrefix: function()
  {
    return this.patientLastLinkName;
  },
  setPatientLastLinkNamePrefix: function(patientLastLinkName)
  {
    this.patientLastLinkName = patientLastLinkName;
  },
  getViewMode: function()
  {
    return this.mode;
  },
  getPresetDate: function()
  {
    return this.presetDate;
  },
  setPresetDate: function(presetDate)
  {
    this.presetDate = presetDate;
  },
  getProblemDescriptionNamedIdentitiesMap: function()
  {
    return this.problemDescriptionNamedIdentitiesMap;
  },
  getPharmacistReviewReferBackPreset: function()
  {
    return this.pharmacistReviewReferBackPreset;
  },
  getMedicationsSupplyPresent: function()
  {
    return this.medicationsSupplyPresent;
  },
  getTherapyChangeReasonTypeMap: function()
  {
    return this.therapyChangeReasonTypeMap;
  },
  getMedicationsWitnessRequiredUnderAge: function()
  {
    return this.medicationsWitnessRequiredUnderAge;
  },
  getMedicationsWitnessRequired: function()
  {
    return this.medicationsWitnessRequired;
  },
  getMedicationsShowHeparinPane: function()
  {
    return this.medicationsShowHeparinPane;
  },
  getBnfEnabled: function()
  {
    return this.bnfEnabled;
  },

  /**
   * @returns {boolean}
   */
  isInfusionBagEnabled: function()
  {
    return this.infusionBagEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isFormularyFilterEnabled: function()
  {
    return this.formularyFilterEnabled === true;
  },
  /**
   * @returns {boolean}
   */
  isNonFormularyMedicationSearchAllowed: function()
  {
    return this.nonFormularyMedicationSearchAllowed === true;
  },
  getOutpatientPrescriptionType: function()
  {
    return this.outpatientPrescriptionType;
  },

  setActionCallbackListener: function(content)
  {
    this.actionCallbackListener = content;
  },

  /**
   * @returns {Boolean}
   */
  isDoctorReviewRequired: function()
  {
    return this.doctorReviewEnabled;
  },

  /**
   * @returns {Boolean}
   */
  isDoseRangeEnabled: function()
  {
    return this.doseRangeEnabled === true;
  },

  /**
   * @returns {app.views.medications.RestApi}
   */
  getRestApi: function()
  {
    return this._restApi;
  },

  /**
   *
   * @returns {tm.views.medications.TherapyAuthority}
   */
  getTherapyAuthority: function()
  {
    return this._therapyAuthority;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapyToEdit: function()
  {
    return this.therapyToEdit;
  },

  /**
   * @Override
   */
  destroy: function()
  {
    this._abortPresentDataTask();
    this.callSuper();
  },

  onViewInit: function()
  {
    this.callSuper();

    if (this.isDevelopmentMode() && !this.patientId)
    {
      // update data command //
      var updateDataCommand = {
        //Steluca Zubčić
        // 397965328
        //"update":{"data":{"patientId":398227601, viewType: "GRID"}}};
        //"update":{"data":{"patientId":397965335, viewType: "GRID"}}};
        //"update":{"data":{"patientId":397965335, "viewType": "PHARMACIST"}}};
        //"update":{"data":{"patientId":408510900, "viewType": "DOCUMENTATION"}}};
        "update": {"data": {"patientId": 71390167, "viewType": "TIMELINE"}}
        // "update": {"data": {"patientId": 399195450, "viewType": "TIMELINE"}}
      };
    
      var self = this;
      setTimeout(function()
      {
        self.onViewCommand(updateDataCommand);
      }, 100);
    }
  }
});