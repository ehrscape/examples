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
Class.define('tm.views.medications.pharmacists.dto.PharmacistMedicationReview', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);

      config.relatedTherapies = tm.jquery.Utils.isArray(config.relatedTherapies) ? config.relatedTherapies : [];
      config.relatedTherapies.forEach(function convertTherapy(relatedTherapy)
      {
        if (relatedTherapy.therapy)
        {
          relatedTherapy.therapy = app.views.medications.common.TherapyJsonConverter.convert(relatedTherapy.therapy);
        }
      });

      config.createTimestamp = tm.jquery.Utils.isEmpty(config.createTimestamp) ? null : new Date(config.createTimestamp);
      config.reminderDate = tm.jquery.Utils.isEmpty(config.reminderDate) ? null : new Date(config.reminderDate);
      config.drugRelatedProblem = tm.jquery.Utils.isEmpty(config.drugRelatedProblem) ? null :
          tm.views.medications.pharmacists.dto.TherapyProblemDescription.fromJson(config.drugRelatedProblem);
      config.pharmacokineticIssue = tm.jquery.Utils.isEmpty(config.pharmacokineticIssue) ? null :
          tm.views.medications.pharmacists.dto.TherapyProblemDescription.fromJson(config.pharmacokineticIssue);
      config.patientRelatedProblem = tm.jquery.Utils.isEmpty(config.patientRelatedProblem) ? null :
          tm.views.medications.pharmacists.dto.TherapyProblemDescription.fromJson(config.patientRelatedProblem);

      return new tm.views.medications.pharmacists.dto.PharmacistMedicationReview(config);
    }
  },
  compositionUid: null, /* string */
  composer: null, /* NamedIdentity */
  createTimestamp: null, /* Date */
  relatedTherapies: null, /* array */
  noProblem: null, /* boolean */
  overallRecommendation: null, /* string */
  referBackToPrescriber: null, /* boolean */
  pharmacistReviewStatus: null, /* string enum */
  reminderDate: null,
  reminderNote: null, /* string */
  medicationSupplyTypeEnum: null, /* string enum */
  daysSupply: null, /* Integer */
  mostRecentReview: null, /* boolean */

  drugRelatedProblem: null, /* tm.views.medications.pharmacists.dto.TherapyProblemDescription */
  pharmacokineticIssue: null, /* tm.views.medications.pharmacists.dto.TherapyProblemDescription */
  patientRelatedProblem: null, /* tm.views.medications.pharmacists.dto.TherapyProblemDescription */

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.relatedTherapies = this.getConfigValue("relatedTherapies", []);
    this.pharmacistReviewStatus = this.getConfigValue("pharmacistReviewStatus",
        app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.DRAFT);
  },

  /* getters and setters */
  getCompositionUid: function ()
  {
    return this.compositionUid;
  },
  setCompositionUid: function (value)
  {
    this.compositionUid = value;
  },
  getComposer: function ()
  {
    return this.composer;
  },
  setComposer: function (value)
  {
    this.composer = value;
  },
  getCreateTimestamp: function ()
  {
    return this.createTimestamp;
  },
  setCreateTimestamp: function (value)
  {
    this.createTimestamp = value;
  },
  getRelatedTherapies: function ()
  {
    return this.relatedTherapies;
  },
  getNoProblem: function ()
  {
    return this.noProblem;
  },
  setNoProblem: function (value)
  {
    this.noProblem = value;
  },
  getOverallRecommendation: function ()
  {
    return this.overallRecommendation;
  },
  setOverallRecommendation: function (value)
  {
    this.overallRecommendation = value;
  },
  getReferBackToPrescriber: function ()
  {
    return this.referBackToPrescriber;
  },
  setReferBackToPrescriber: function (value)
  {
    this.referBackToPrescriber = value;
  },
  getDrugRelatedProblem: function ()
  {
    return this.drugRelatedProblem;
  },
  setDrugRelatedProblem: function (value)
  {
    this.drugRelatedProblem = value;
  },
  getPharmacokineticIssue: function ()
  {
    return this.pharmacokineticIssue;
  },
  setPharmacokineticIssue: function (value)
  {
    this.pharmacokineticIssue = value;
  },
  getPatientRelatedProblem: function ()
  {
    return this.patientRelatedProblem;
  },
  setPatientRelatedProblem: function (value)
  {
    this.patientRelatedProblem = value;
  },
  isDraft: function()
  {
    return this.pharmacistReviewStatus == app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.DRAFT;
  },
  markReviewed: function()
  {
    this.setPharmacistReviewStatus(app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.REVIEWED);
  },
  isReviewed: function()
  {
    return this.pharmacistReviewStatus == app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.REVIEWED;
  },
  markAuthorized: function ()
  {
    this.setPharmacistReviewStatus(app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.FINAL);
  },
  getPharmacistReviewStatus: function ()
  {
    return this.pharmacistReviewStatus;
  },
  setPharmacistReviewStatus: function (value)
  {
    this.pharmacistReviewStatus = value;
  },
  getReminderDate: function()
  {
    return this.reminderDate;
  },
  setReminderDate: function(value)
  {
    this.reminderDate = value;
  },
  getReminderNote: function()
  {
    return this.reminderNote;
  },
  setReminderNote: function(value)
  {
    this.reminderNote = value;
  },
  getMedicationSupplyType: function()
  {
      return this.medicationSupplyTypeEnum;
  },
  setMedicationSupplyType: function(value)
  {
    this.medicationSupplyTypeEnum = value;
  },
  getDaysSupply: function()
  {
    return this.daysSupply;
  },
  setDaysSupply: function(value)
  {
    this.daysSupply = value;
  },
  isMostRecentReview: function()
  {
    return this.mostRecentReview;
  },
  setMostRecentReview: function(value)
  {
    this.mostRecentReview = value;
  }
});