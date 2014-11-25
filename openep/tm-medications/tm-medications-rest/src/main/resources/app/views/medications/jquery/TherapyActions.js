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
    var reviewTherapyUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_REVIEW_THERAPY;
    var uidWithoutVersion = this.view.getUidWithoutVersion(ehrCompositionId);
    var centralCaseData = this.view.getCentralCaseData(); //TherapyCentralCaseData
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName,
      sessionId: centralCaseData && centralCaseData.sessionId ? centralCaseData.sessionId : null,
      knownOrganizationalEntity: self.view.getKnownOrganizationalEntity()
    };
    this.view.loadPostViewData(reviewTherapyUrl, params, null,
        function()
        {
          successFunction();
        },
        function()
        {
          failedFunction();
        },
        true);
  },

  abortTherapy: function(ehrCompositionId, ehrOrderName, successFunction, failedFunction)
  {
    var self = this;
    var abortTherapyUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_ABORT_THERAPY;
    var uidWithoutVersion = this.view.getUidWithoutVersion(ehrCompositionId);
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName
    };
    this.view.loadPostViewData(abortTherapyUrl, params, null,
        function()
        {
          successFunction();
        },
        function()
        {
          failedFunction()
        },
        true);
  },

  suspendTherapy: function(ehrCompositionId, ehrOrderName, successFunction, failedFunction)
  {
    var self = this;
    var suspendTherapyUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SUSPEND_THERAPY;
    var uidWithoutVersion = this.view.getUidWithoutVersion(ehrCompositionId);
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName
    };
    this.view.loadPostViewData(suspendTherapyUrl, params, null,
        function()
        {
          successFunction();
        },
        function()
        {
          failedFunction();
        },
        true);
  },

  reissueTherapy: function(ehrCompositionId, ehrOrderName, successFunction, failedFunction)
  {
    var self = this;
    var reissueTherapyUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_REISSUE_THERAPY;
    var uidWithoutVersion = this.view.getUidWithoutVersion(ehrCompositionId);
    var centralCaseData = self.view.getCentralCaseData(); //TherapyCentralCaseData
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName,
      sessionId: centralCaseData && centralCaseData.sessionId ? centralCaseData.sessionId : null,
      knownOrganizationalEntity: self.view.getKnownOrganizationalEntity()
    };
    this.view.loadPostViewData(reissueTherapyUrl, params, null,
        function()
        {
          successFunction();
        },
        function()
        {
          failedFunction();
        },
        true);
  }

});
