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
Class.define('tm.views.medications.pharmacists.dto.TherapyProblemDescription', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject){
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new tm.views.medications.pharmacists.dto.TherapyProblemDescription({
        categories: jsonObject.categories,
        outcome: jsonObject.outcome,
        impact: jsonObject.impact,
        recommendation: jsonObject.recommendation
      });
    }
  },
  categories: null, /* array of NamedIdentity */
  outcome: null, /* NamedIdentity */
  impact: null, /* NamedIdentity */
  recommendation: null, /* string */

  /* getters and setters */
  getCategories: function()
  {
    return this.categories;
  },
  setCategories: function(value)
  {
    this.categories = value;
  },
  getOutcome: function()
  {
    return this.outcome;
  },
  setOutcome: function(value)
  {
    this.outcome = value;
  },
  getImpact: function()
  {
    return this.impact;
  },
  setImpact: function(value)
  {
    this.impact = value;
  },
  getRecommendation: function()
  {
    return this.recommendation;
  },
  setRecommendation: function(value)
  {
    this.recommendation = value;
  }
});