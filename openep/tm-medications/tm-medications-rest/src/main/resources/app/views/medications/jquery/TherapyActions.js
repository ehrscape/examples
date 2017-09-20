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

Class.define('app.views.medications.TherapyActions', 'tm.jquery.Object', {
  /** members: configs */
  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  reviewTherapy: function(ehrCompositionId, ehrOrderName, successFunction, failedFunction)
  {
    var self = this;

    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_REVIEW_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var reviewTherapyUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_REVIEW_THERAPY;
    var uidWithoutVersion = tm.views.medications.MedicationUtils.getUidWithoutVersion(ehrCompositionId);
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName
    };
    this.view.loadPostViewData(reviewTherapyUrl, params, null,
        function()
        {
          successFunction();
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          failedFunction();
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  abortTherapy: function(ehrCompositionId, ehrOrderName, changeReason, successFunction, failedFunction)
  {
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_ABORT_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var abortTherapyUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_ABORT_THERAPY;
    var uidWithoutVersion = tm.views.medications.MedicationUtils.getUidWithoutVersion(ehrCompositionId);
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName,
      changeReason: changeReason != null ? JSON.stringify(changeReason) : null
    };
    this.view.loadPostViewData(abortTherapyUrl, params, null,
        function()
        {
          successFunction();
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          failedFunction();
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  suspendTherapy: function(ehrCompositionId, ehrOrderName, changeReason, successFunction, failedFunction)
  {
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SUSPEND_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var suspendTherapyUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SUSPEND_THERAPY;
    var uidWithoutVersion = tm.views.medications.MedicationUtils.getUidWithoutVersion(ehrCompositionId);
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName,
      changeReason: changeReason != null ? JSON.stringify(changeReason) : null
    };
    this.view.loadPostViewData(suspendTherapyUrl, params, null,
        function()
        {
          successFunction();
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          failedFunction();
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  reissueTherapy: function(ehrCompositionId, ehrOrderName, successFunction, failedFunction)
  {
    var self = this;

    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_REISSUE_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var reissueTherapyUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_REISSUE_THERAPY;
    var uidWithoutVersion = tm.views.medications.MedicationUtils.getUidWithoutVersion(ehrCompositionId);
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName
    };
    this.view.loadPostViewData(reissueTherapyUrl, params, null,
        function()
        {
          successFunction();
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          failedFunction();
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  }

});
