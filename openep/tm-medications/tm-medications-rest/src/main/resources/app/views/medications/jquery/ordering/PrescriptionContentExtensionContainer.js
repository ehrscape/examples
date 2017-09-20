/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.ordering.PrescriptionContentExtensionContainer', 'tm.jquery.Container', {
  view: null,
  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /* append field content to the therapy DTO being constructed by the main edit container */
  buildTherapy: function (therapy)
  {

  },

  /**
   * Override if required - will be executed when the primary medication changes. Keep in mind that
   * a render event can happen on the parent right after this call, so if you are loading any data
   * via a yielded Ajax, make sure you yield via a setTimeout or create a conditional task before
   * trying to change any UI values in the DOM. Otherwise bad things can happen (values won't be visible in the DOM).
   * @param medicationData
   */
  setMedicationData: function(medicationData) {

  },

  /* implement and return any required field validations as an array */
  getFormValidations: function()
  {
    return [];
  },

  getView: function()
  {
    return this.view;
  },

  /* implement the response to the change of number of medications that build the therapy */
  therapyMedicationsCountChangedFunction: function(numberOfMedications)
  {
  },

  /* clear content implementation */
  clear: function()
  {

  }
});