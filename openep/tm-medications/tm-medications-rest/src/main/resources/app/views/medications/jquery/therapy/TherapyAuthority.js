/*
 * Copyright (c) 2010-2015 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.therapy.TherapyAuthority', 'tm.jquery.Object', {

  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._editAllowed = this.getView().getProperty("editAllowed");
    this._therapyReportEnabled = this.getView().getProperty("therapyReportEnabled");
    this._surgeryReportEnabled = this.getView().getProperty("surgeryReportEnabled");
    this._gridViewEnabled = this.getView().getProperty("gridViewEnabled");
    this._timelineViewEnabled = this.getView().getProperty("timelineViewEnabled");
    this._pharmacistReviewViewEnabled = this.getView().getProperty("pharmacistReviewViewEnabled");
    this._managePatientPharmacistReviewAllowed = this.getView().getProperty("managePatientPharmacistReviewAllowed");
    this._showPharmacistReviewStatus = this.getView().getProperty("showPharmacistReviewStatus");
    this._medicationSummaryViewEnabled = this.getView().getProperty("medicationSummaryViewEnabled");
    this._medicationConsentT2T3Allowed = this.getView().getProperty("medicationConsentT2T3Allowed");
    this._medicationDocumentViewEnabled = this.getView().getProperty("medicationDocumentViewEnabled");
    this._addMedicationToPreparationTasklistAllowed = this.getView().getProperty("addMedicationToPreparationTasklistAllowed");
    this._nonFormularyMedicationSearchAllowed = this.getView().getProperty("nonFormularyMedicationSearchAllowed");
    this._managePatientTemplatesAllowed = this.getView().getProperty("managePatientTemplatesAllowed");
    this._manageOrganizationalTemplatesAllowed = this.getView().getProperty("manageOrganizationalTemplatesAllowed");
    this._manageInpatientPrescriptionsAllowed = this.getView().getProperty("manageInpatientPrescriptionsAllowed");
    this._manageOutpatientPrescriptionsAllowed = this.getView().getProperty("manageOutpatientPrescriptionsAllowed");
    this._copyPrescriptionAllowed = this.getView().getProperty("copyPrescriptionAllowed");
    this._suspendPrescriptionAllowed = this.getView().getProperty("suspendPrescriptionAllowed");
    this._restartSuspendPrescriptionAllowed = this.getView().getProperty("restartSuspendPrescriptionAllowed");
    this._doctorReviewAllowed = this.getView().getProperty("doctorReviewAllowed");
    this._startSelfAdministrationAllowed = this.getView().getProperty("startSelfAdministrationAllowed");
    this._scheduleAdditionalAdministrationAllowed = this.getView().getProperty("scheduleAdditionalAdministrationAllowed");
    this._recordPrnAdministrationAllowed = this.getView().getProperty("recordPrnAdministrationAllowed");
    this._createResupplyRequestAllowed = this.getView().getProperty("createResupplyRequestAllowed");
    this._manageAdministrationsAllowed = this.getView().getProperty("manageAdministrationsAllowed");
    this._rescheduleAdministrationsAllowed = this.getView().getProperty("rescheduleAdministrationsAllowed");
    this._medicationIdentifierScanningAllowed = this.getView().getProperty("medicationIdentifierScanningAllowed");
  },


  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {boolean}
   */
  isEditAllowed: function()
  {
    return this._editAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isTherapyReportEnabled: function()
  {
    return this._therapyReportEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isSurgeryReportEnabled: function()
  {
    return this._surgeryReportEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isGridViewEnabled: function()
  {
    return this._gridViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isTimelineViewEnabled: function()
  {
    return this._timelineViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isPharmacistReviewViewEnabled: function()
  {
    return this._pharmacistReviewViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isManagePatientPharmacistReviewAllowed: function()
  {
    return this._managePatientPharmacistReviewAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isShowPharmacistReviewStatus: function()
  {
    return this._showPharmacistReviewStatus === true;
  },

  /**
   * @returns {boolean}
   */
  isMedicationSummaryViewEnabled: function()
  {
    return this._medicationSummaryViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isMedicationConsentT2T3Allowed: function()
  {
    return this._medicationConsentT2T3Allowed === true;
  },

  /**
   * @returns {boolean}
   */
  isMedicationDocumentViewEnabled: function()
  {
    return this._medicationDocumentViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isAddMedicationToPreparationTasklistAllowed: function()
  {
    return this._addMedicationToPreparationTasklistAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isNonFormularyMedicationSearchAllowed: function()
  {
    return this._nonFormularyMedicationSearchAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManagePatientTemplatesAllowed: function()
  {
    return this._managePatientTemplatesAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManageOrganizationalTemplatesAllowed: function()
  {
    return this._manageOrganizationalTemplatesAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManageInpatientPrescriptionsAllowed: function()
  {
    return this._manageInpatientPrescriptionsAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManageOutpatientPrescriptionsAllowed: function()
  {
    return this._manageOutpatientPrescriptionsAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isCopyPrescriptionAllowed: function()
  {
    return this._copyPrescriptionAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isSuspendPrescriptionAllowed: function()
  {
    return this._suspendPrescriptionAllowed === true;
  },

  /**
   * @returns {boolean}
   */

  isRestartSuspendPrescriptionAllowed: function()
  {
    return this._restartSuspendPrescriptionAllowed === true;
  },

  /**
   * @returns {boolean}
   */

  isDoctorReviewAllowed: function()
  {
    return this._doctorReviewAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isStartSelfAdministrationAllowed: function()
  {
    return this._startSelfAdministrationAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isScheduleAdditionalAdministrationAllowed: function()
  {
    return this._scheduleAdditionalAdministrationAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isRecordPrnAdministrationAllowed: function()
  {
    return this._recordPrnAdministrationAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isCreateResupplyRequestAllowed: function()
  {
    return this._createResupplyRequestAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManageAdministrationsAllowed: function()
  {
    return this._manageAdministrationsAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isRescheduleAdministrationsAllowed: function()
  {
    return this._rescheduleAdministrationsAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isMedicationIdentifierScanningAllowed: function()
  {
    return this._medicationIdentifierScanningAllowed === true;
  }

});