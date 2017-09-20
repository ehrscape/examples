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
Class.define('app.views.medications.common.dto.MedicationData', 'tm.jquery.Object', {
  statics: {
    /**
     * Use this method for special conversion from json (dates etc...)
     * @param jsonObject
     * @returns {object}
     */
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;
      var config = jQuery.extend(true, {}, jsonObject);
      config.medication = !tm.jquery.Utils.isEmpty(jsonObject.medication) ? 
          new app.views.medications.common.dto.Medication(jsonObject.medication) : 
          null;
      return new app.views.medications.common.dto.MedicationData(config);
    }
  },
  medication: null, /* MedicationDto */
  defaultRoute: null, /* MedicationRouteDto */
  doseForm: null, /* DoseFormDto */
  doseSplitFactor: null, /* Double */
  basicUnit: null, /* String */
  antibiotic: null, /* boolean */
  reviewReminder: null, /* boolean */
  mentalHealthDrug: null, /* boolean */
  controlledDrug: null, /* boolean */
  blackTriangleMedication: null,  /* boolean */
  clinicalTrialMedication: null, /* boolean */
  highAlertMedication: null, /* boolean */
  unlicensedMedication: null, /* boolean */
  expensiveDrug: null, /* boolean */
  inpatientMedication: null, /* boolean */
  outpatientMedication: null, /* boolean */
  formulary: null, /* boolean */
  descriptiveIngredient: null, /* MedicationIngredientDto */
  medicationIngredients: null, /* array [MedicationIngredientDto] */
  routes: null, /* array [MedicationRouteDto] */
  medicationDocuments: null, /* array [MedicationDocumentDto] */
  indications: null, /* array [IndicationDto] */
  titration: null, /* string enum from TitrationType */
  roundingFactor: null, /* double */
  price: null, /* String */
  medicationPackaging: null, /* String */

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   * @param {object} medication
   */
  setMedication: function(medication)
  {
    this.medication = medication;
  },

  /**
   * @returns {object}
   */
  getMedication: function()
  {
    return this.medication;
  },

  /**
   * @param {object} defaultRoute
   */
  setDefaultRoute: function(defaultRoute)
  {
    this.defaultRoute = defaultRoute;
  },
  /**
   * @returns {{type: String, unlicensedRoute: Boolean, bnfMaximumDto: object, discretionary: Boolean}|null}
   */
  getDefaultRoute: function()
  {
    return this.defaultRoute;
  },
  /**
   * @param {object} doseForm
   */
  setDoseForm: function(doseForm)
  {
    this.doseForm = doseForm;
  },
  /**
   * @returns {object}
   */
  getDoseForm: function()
  {
    return this.doseForm;
  },
  /**
   * @param {float} doseSplitFactor
   */
  setDoseSplitFactor: function(doseSplitFactor)
  {
    this.doseSplitFactor = doseSplitFactor;
  },
  /**
   * @returns {float}
   */
  getDoseSplitFactor: function()
  {
    return this.doseSplitFactor;
  },
  /**
   * @param {string} basicUnit
   */
  setBasicUnit: function(basicUnit)
  {
    this.basicUnit = basicUnit;
  },
  /**
   * @returns {string}
   */
  getBasicUnit: function()
  {
    return this.basicUnit;
  },
  /**
   * @param {boolean} antibiotic
   */
  setAntibiotic: function(antibiotic)
  {
    this.antibiotic = antibiotic;
  },
  /**
   * @returns {boolean}
   */
  isAntibiotic: function()
  {
    return this.antibiotic;
  },
  /**
   * @param {boolean} reviewReminder
   */
  setReviewReminder: function(reviewReminder)
  {
    this.reviewReminder = reviewReminder;
  },
  /**
   * @returns {boolean}
   */
  getReviewReminder: function()
  {
    return this.reviewReminder;
  },
  /**
   * @param {boolean} mentalHealthDrug
   */
  setMentalHealthDrug: function(mentalHealthDrug)
  {
    this.mentalHealthDrug = mentalHealthDrug;
  },
  /**
   * @returns {Boolean}
   */
  isMentalHealthDrug: function()
  {
    return this.mentalHealthDrug === true;
  },
  /**
   * @param {boolean} controlledDrug
   */
  setControlledDrug: function(controlledDrug)
  {
    this.controlledDrug = controlledDrug;
  },
  /**
   * @param {Boolean} value
   */
  setBlackTriangleMedication: function(value)
  {
    this.blackTriangleMedication = value;
  },
  /**
   * @param {Boolean} value
   */
  setClinicalTrialMedication: function(value)
  {
    this.clinicalTrialMedication = value;
  },
  /**
   * @param {Boolean} value
   */
  setHighAlertMedication: function(value)
  {
    this.highAlertMedication = value;
  },
  /**
   * @param {Boolean} value
   */
  setUnlicensedMedication: function(value)
  {
    this.unlicensedMedication = value;
  },
  /**
   * @param {Boolean} value
   */
  setExpensiveDrug: function(value)
  {
    this.expensiveDrug = value;
  },
  /**
   * @param {Boolean} value
   */
  setInpatientMedication: function(value)
  {
    this.inpatientMedication = value;
  },
  /**
   * @param {Boolean} value
   */
  setOutpatientMedication: function(value)
  {
    this.outpatientMedication = value;
  },
  /**
   * @param {Boolean} value
   */
  setFormulary: function(value)
  {
    this.formulary = value;
  },
  /**
   * @returns {Boolean}
   */
  isControlledDrug: function()
  {
    return this.controlledDrug === true;
  },
  /**
   * @returns {Boolean}
   */
  isBlackTriangleMedication: function()
  {
    return this.blackTriangleMedication === true;
  },
  /**
   * @returns {Boolean}
   */
  isClinicalTrialMedication: function()
  {
    return this.clinicalTrialMedication === true;
  },
  /**
   * @returns {Boolean}
   */
  isHighAlertMedication: function()
  {
    return this.highAlertMedication === true;
  },
  /**
   * @returns {Boolean}
   */
  isUnlicensedMedication: function()
  {
    return this.unlicensedMedication === true;
  },
  /**
   * @returns {Boolean}
   */
  isExpensiveDrug: function()
  {
    return this.expensiveDrug === true;
  },
  /**
   * @returns {Boolean}
   */
  isInpatientMedication: function()
  {
    return this.inpatientMedication === true;
  },
  /**
   * @returns {Boolean}
   */
  isOutpatientMedication: function()
  {
    return this.outpatientMedication === true;
  },
  /**
   * @returns {Boolean}
   */
  isFormulary: function()
  {
    return this.formulary === true;
  },
  /**
   * @param {object} descriptiveIngredient
   */
  setDescriptiveIngredient: function(descriptiveIngredient)
  {
    this.descriptiveIngredient = descriptiveIngredient;
  },
  /**
   * @returns {object}
   */
  getDescriptiveIngredient: function()
  {
    return this.descriptiveIngredient;
  },
  /**
   * @param {Array<*>} medicationIngredients
   */
  setMedicationIngredients: function(medicationIngredients)
  {
    this.medicationIngredients = medicationIngredients;
  },
  /**
   * @returns {Array<Object>|null}
   */
  getMedicationIngredients: function()
  {
    return this.medicationIngredients;
  },
  /**
   * @param {array} routes
   */
  setRoutes: function(routes)
  {
    this.routes = routes;
  },
  /**
   * @returns {Array<{ type: String, unlicensedRoute: Boolean, bnfMaximumDto: object, discretionary: Boolean }>|null}
   */
  getRoutes: function()
  {
    return this.routes;
  },
  /**
   * @returns {Number}
   */
  getRoundingFactor: function()
  {
    return this.roundingFactor;
  },
  /**
   * @param {Number} value
   */
  setRoundingFactor: function(value)
  {
    this.roundingFactor = value;
  },
  /**
   * @param {array} medicationDocuments
   */
  setMedicationDocuments: function(medicationDocuments)
  {
    this.medicationDocuments = medicationDocuments;
  },
  /**
   * @returns {array}
   */
  getMedicationDocuments: function()
  {
    return this.medicationDocuments;
  },
  /**
   * @param {array} indications
   */
  setIndications: function(indications)
  {
    this.indications = indications;
  },
  /**
   * @returns {array}
   */
  getIndications: function()
  {
    return this.indications;
  },

  /**
   * @returns {string|null}
   */
  getTitration: function()
  {
    return this.titration;
  },

  /**
   * @returns {string|null}
   */
  getPrice: function()
  {
    return this.price;
  },

  /**
   * @param {string|null} value
   */
  setPrice: function(value)
  {
    this.price = value;
  },

  /**
   * @returns {string|null}
   */
  getMedicationPackaging: function()
  {
    return this.medicationPackaging;
  },

  /**
   * @param {string|null} value
   */
  setMedicationPackaging: function(value)
  {
    this.medicationPackaging = value;
  },

  /**
   * @returns {boolean}
   */
  isDoseFormDescriptive: function()
  {
    return this.getDoseForm() && this.getDoseForm().medicationOrderFormType === 'DESCRIPTIVE';
  },

  /**
   * @returns {{strengthNumeratorUnit: String, strengthDenominatorUnit: String}|null}
   */
  getDefiningIngredient: function ()
  {
    if (tm.jquery.Utils.isArray(this.getMedicationIngredients()) && this.getMedicationIngredients().length == 1)
    {
      return this.getMedicationIngredients()[0];
    }
    else
    {
      return this.getDescriptiveIngredient();
    }
  },

  /**
   * @returns {String|null}
   */
  getStrengthNumeratorUnit: function()
  {
    var definingIngredient = this.getDefiningIngredient();
    if (definingIngredient)
    {
      return definingIngredient.strengthNumeratorUnit;
    }
    else 
    {
      return this.getBasicUnit();
    }
  },

  /**
   * @returns {String|null}
   */
  getStrengthDenominatorUnit: function()
  {
    var definingIngredient = this.getDefiningIngredient();
    if (definingIngredient)
    {
      return definingIngredient.strengthDenominatorUnit;
    }
    return null;
  }
});
