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
Class.define('app.views.medications.common.dto.Therapy', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.start = tm.jquery.Utils.isEmpty(jsonObject.start) ? null : new Date(jsonObject.start);
      config.end = tm.jquery.Utils.isEmpty(jsonObject.end) ? null : new Date(jsonObject.end);
      config.createdTimestamp = tm.jquery.Utils.isEmpty(jsonObject.createdTimestamp) ? null :
          new Date(jsonObject.createdTimestamp);
      config.medication = !tm.jquery.Utils.isEmpty(jsonObject.medication) ?
          new app.views.medications.common.dto.Medication(jsonObject.medication) :
          null;
      if (!tm.jquery.Utils.isEmpty(jsonObject.ingredientsList))
      {
        jsonObject.ingredientsList.forEach(function(ingredient)
        {
          ingredient.medication = new app.views.medications.common.dto.Medication(ingredient.medication);
        });
        config.ingredientsList = jsonObject.ingredientsList;
      }

      return new app.views.medications.common.dto.Therapy(config);
    }
  },

  compositionUid: null, /* string */
  ehrOrderName: null, /* string */
  medicationOrderFormType: null, /* enum MedicationOrderFormType */
  variable: false, 
  therapyDescription: null, /* string */
  routes: null, /* array - route dto */
  dosingFrequency: null, /* dosingfrequency dto */
  dosingDaysFrequency: null, /* integer */
  daysOfWeek: null, /* array */
  start: null, /* date */
  end: null, /* date */
  whenNeeded: null, /* boolean */
  comment: null, /* string */
  clinicalIndication: null, /* NamedIdentityDto */
  prescriberName: null, /* string */
  composerName: null, /* string */
  startCriterion: null, /* string */
  applicationPrecondition: null,  /* string */
  reviewReminderDays: null, /* integer */

  speedDisplay: null, /* sting */
  frequencyDisplay: null, /* string */
  daysFrequencyDisplay: null, /* string */
  whenNeededDisplay: null, /* string */
  startCriterionDisplay: null, /* string */
  daysOfWeekDisplay: null, /* string */
  applicationPreconditionDisplay: null,/* string */

  formattedTherapyDisplay: null, /* string */
  pastDaysOfTherapy: null, /* integer */

  linkName: null, /* string */

  maxDailyFrequency: null,/* integer */
  bnfMaximumPercentage: null, /* integer */
  
  createdTimestamp: null, /* Date */
  tags: null, /* array */
  criticalWarnings: null, /* array */
  linkedToAdmission: null, /* boolean */

  completed: null, /* boolean, used for the ordering container */
  medication: null, /* Medication.js */
  titration: null, /* TitrationType */
  continuousInfusion: null, /* Boolean */
  recurringContinuousInfusion: null, /* Boolean */
  doseType: null, /* TherapyDoseTypeEnum */
  ingredientsList: null, /* InfusionIngredientDto */
  doseElement: null, /* ComplexDoseElementDto */
  timedDoseElements: null, /* Array */
  doseTimes: null, /* array */

  quantityUnit: null, /* string */
  doseForm: null, /* string */
  volumeSum: null, /* number */
  volumeSumUnit: null, /* string */
  volumeSumDisplay: null, /* string */
  quantityDisplay: null, /* string */
  prescriptionSupply: null,  /* PrescriptionSupplyDto object */
  adjustToFluidBalance: null, /* Boolean */
  speedFormulaDisplay: null, /* String */
  additionalInstructionDisplay: null, /* String */
  durationDisplay: null, /* String */
  selfAdministeringActionEnum: null, /* String */

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.daysOfWeek = this.getConfigValue("daysOfWeek", []);
    this.tags = this.getConfigValue("tags", []);
    this.criticalWarnings = this.getConfigValue("criticalWarnings", []);
    this.completed = this.getConfigValue("completed", true);
  },

  getCompositionUid: function(){
    return this.compositionUid;
  },
  getEhrOrderName: function(){
    return this.ehrOrderName;
  },
  getMedicationOrderFormType: function()
  {
    return this.medicationOrderFormType;
  },
  /**
   * @returns {boolean}
   */
  isOrderTypeOxygen: function()
  {
    return this.medicationOrderFormType === app.views.medications.TherapyEnums.medicationOrderFormType.OXYGEN;
  },
  /**
   * @returns {boolean}
   */
  isOrderTypeComplex: function()
  {
    return this.medicationOrderFormType === app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX;
  },
  /**
   * @returns {boolean}
   */
  isOrderTypeSimple: function()
  {
    return this.medicationOrderFormType === app.views.medications.TherapyEnums.medicationOrderFormType.SIMPLE;
  },
  /**
   * @returns {boolean}
   */
  isOrderTypeDescriptive: function()
  {
    return this.medicationOrderFormType === app.views.medications.TherapyEnums.medicationOrderFormType.DESCRIPTIVE;
  },
  /**
   * @returns {boolean}
   */
  isVariable: function()
  {
    return this.variable === true;
  },
  /**
   * @returns {boolean}
   */
  isRecurringContinuousInfusion: function()
  {
    return this.recurringContinuousInfusion === true;
  },
  /**
   * @returns {Boolean}
   */
  isAdjustToFluidBalance: function()
  {
    return this.adjustToFluidBalance === true;
  },
  getTherapyDescription: function(){
    return this.therapyDescription;
  },
  getRoutes: function(){
    return this.routes;
  },
  getDosingFrequency: function(){
    return this.dosingFrequency;
  },
  getDosingDaysFrequency: function(){
    return this.dosingDaysFrequency;
  },
  getDaysOfWeek: function(){
    return this.daysOfWeek;
  },
  getStart: function(){
    return this.start;
  },
  getEnd: function(){
    return this.end;
  },
  getWhenNeeded: function(){
    return this.whenNeeded;
  },
  getComment: function(){
    return this.comment;
  },
  getClinicalIndication: function(){
    return this.clinicalIndication;
  },
  getPrescriberName: function() {
   return this.prescriberName;
  },
  getComposerName: function(){
    return this.composerName;
  },
  getStartCriterion: function(){
    return this.startCriterion;
  },
  getApplicationPrecondition: function(){
    return this.applicationPrecondition;
  },
  getReviewReminderDays: function(){
    return this.reviewReminderDays;
  },
  /**
   * @returns {Number|null}
   */
  getVolumeSum: function()
  {
    return this.volumeSum;
  },
  /**
   * @returns {String|null}
   */
  getVolumeSumUnit: function()
  {
    return this.volumeSumUnit;
  },
  /**
   * @returns {String|null}
   */
  getVolumeSumDisplay: function()
  {
    return this.volumeSumDisplay;
  },
  /**
   * @returns {String|null}
   */
  getQuantityDisplay: function()
  {
    return this.quantityDisplay;
  },
  /**
   * @returns {String|null}
   */
  getDoseForm: function()
  {
    return this.doseForm;
  },
  /**
   * @returns {String|null}
   */
  getAdditionalInstructionDisplay: function()
  {
    return this.additionalInstructionDisplay;
  },
  /**
   * @returns {String|null}
   */
  getSpeedFormulaDisplay: function()
  {
    return this.speedFormulaDisplay;
  },
  /**
   * @returns {String|null}
   */
  getSpeedDisplay: function()
  {
    return this.speedDisplay;
  },
  /**
   * @returns {String|null}
   */
  getDurationDisplay: function()
  {
    return this.durationDisplay;
  },
  getFrequencyDisplay: function(){
    return this.frequencyDisplay;
  },
  getDaysFrequencyDisplay: function(){
    return this.daysFrequencyDisplay;
  },
  getWhenNeededDisplay: function(){
    return this.whenNeededDisplay;
  },
  getStartCriterionDisplay: function(){
    return this.startCriterionDisplay;
  },
  getDaysOfWeekDisplay: function(){
    return this.daysOfWeekDisplay;
  },
  getApplicationPreconditionDisplay: function(){
    return this.applicationPreconditionDisplay;
  },

  getFormattedTherapyDisplay: function(){
    return this.formattedTherapyDisplay;
  },
  getPastDaysOfTherapy: function(){
    return this.pastDaysOfTherapy;
  },

  getLinkName: function(){
    return this.linkName;
  },

  getMaxDailyFrequency: function(){
    return this.maxDailyFrequency;
  },

  getCreatedTimestamp: function(){
    return this.createdTimestamp;
  },

  /**
   * @returns {Array<*>}
   */
  getTags: function(){
    return this.tags ? this.tags : [];
  },

  getCriticalWarnings: function() {
    return this.criticalWarnings;
  },

  getSelfAdministeringActionEnum: function()
  {
    return this.selfAdministeringActionEnum;
  },

  isLinkedToAdmission: function()
  {
    return this.linkedToAdmission === true;
  },


  isCompleted: function()
  {
    return this.completed;
  },

  setCompositionUid: function(value){
    this.compositionUid = value;
  },
  setEhrOrderName: function(value){
    this.ehrOrderName = value;
  },
  setMedicationOrderFormType: function(value){
    this.medicationOrderFormType = value;
  },
  setVariable: function(value){
    this.variable = value;
  },
  setTherapyDescription: function(value){
    this.therapyDescription = value;
  },
  setRoutes: function(value){
    this.routes = value;
  },
  setDosingFrequency: function(value){
    this.dosingDaysFrequency = value;
  },
  setDosingDaysFrequency: function(value){
    this.dosingDaysFrequency = value;
  },
  setDaysOfWeek: function(value){
    this.daysOfWeek = value;
  },
  setStart: function(value){
    this.start = value;
  },
  setEnd: function(value){
    this.end = value;
  },
  setWhenNeeded: function(value){
    this.whenNeeded = value;
  },
  setComment: function(value){
    this.comment = value;
  },
  setClinicalIndication: function(value){
    this.clinicalIndication = value;
  },
  setPrescriberName: function(value){
    this.prescriberName = value;
  },
  setComposerName: function(value){
    this.composerName = value;
  },
  setStartCriterion: function(value){
    this.startCriterion = value;
  },
  setApplicationPrecondition: function(value){
    this.applicationPrecondition = value;
  },
  setReviewReminderDays: function(value){
    this.reviewReminderDays = value;
  },
  /**
   * @param {Number|null} value
   */
  setVolumeSum: function(value)
  {
    this.volumeSum = value;
  },
  /**
   * @param {String|null} value
   */
  setVolumeSumUnit: function(value)
  {
    this.volumeSumUnit = value;
  },
  /**
   * @param {String} value
   */
  setSpeedDisplay: function(value)
  {
    this.speedDisplay = value;
  },
  setFrequencyDisplay: function(value){
    this.frequencyDisplay = value;
  },
  setDaysFrequencyDisplay: function(value){
    this.daysFrequencyDisplay = value;
  },
  setWhenNeededDisplay: function(value){
    this.whenNeededDisplay = value;
  },
  setStartCriterionDisplay: function(value){
    this.startCriterionDisplay = value;
  },
  setDaysOfWeekDisplay: function(value){
    this.daysOfWeekDisplay = value;
  },
  setApplicationPreconditionDisplay: function(value){
    this.applicationPreconditionDisplay = value;
  },

  setFormattedTherapyDisplay: function(value){
    this.formattedTherapyDisplay = value;
  },
  setPastDaysOfTherapy: function(value){
    this.pastDaysOfTherapy = value;
  },

  setLinkName: function(value){
    this.linkName = value;
  },

  setMaxDailyFrequency: function(value){
    this.maxDailyFrequency = value;
  },

  setCreatedTimestamp: function(value){
    this.createdTimestamp = value;
  },
  setTags: function(value){
    this.tags = value;
  },
  setCriticalWarnings: function(value) {
    this.criticalWarnings = value;
  },
  setLinkedToAdmission: function(value)
  {
    this.linkedToAdmission = value;
  },

  setCompleted: function(value){
    this.completed = value;
  },

  getTherapyId: function()
  {
    return tm.views.medications.MedicationUtils.getUidWithoutVersion(this.compositionUid) + '|' + this.ehrOrderName;
  },

  /**
   * 
   * @param {object} medication
   */
  setMedication: function(medication)
  {
    this.medication = medication;
  },

  /**
   * 
   * @returns {app.views.medications.common.dto.Medication|null}
   */
  getMedication: function()
  {
    return this.medication;
  },

  /**
   * @returns {boolean}
   */
  isTitrationDoseType: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getTitration());
  },

  /**
   * @param {string} type
   */
  setTitration: function(type)
  {
    if (type && !app.views.medications.TherapyEnums.therapyTitrationTypeEnum.hasOwnProperty(type))
    {
      console.warn("Unknown titration type enum set to Therapy.js.");
    }
    this.titration = type;
  },

  /**
   * @returns {string|null}
   */
  getTitration: function()
  {
    return this.titration;
  },

  /**
   * @returns {boolean}
   */
  isContinuousInfusion: function()
  {
    return this.continuousInfusion === true;
  },

  /**
   * @returns {boolean}
   */
  isBaselineInfusion: function()
  {
    return this.baselineInfusion === true;
  },

  /**
   * @param {boolean} value
   */
  setContinuousInfusion: function(value)
  {
    this.continuousInfusion = value;
  },

  /**
   * @param {boolean} value
   */
  setBaselineInfusion: function(value)
  {
    this.baselineInfusion = value;
  },

  /**
   * @returns {TherapyDoseTypeEnum}
   */
  getDoseType: function()
  {
    return this.doseType;
  },

  /**
   * @param {TherapyDoseTypeEnum} value
   */
  setDoseType: function(value)
  {
    this.doseType = value;
  },

  /**
   * @returns {array}
   */
  getIngredientsList: function()
  {
    return this.ingredientsList;
  },

  /**
   * @param {array} ingredientsList
   */
  setIngredientsList: function(ingredientsList)
  {
    this.ingredientsList = ingredientsList;
  },

  /**
   * @returns {Object|null}
   */
  getDoseElement: function()
  {
    return this.doseElement;
  },

  /**
   * @param {Object|null} doseElement
   */
  setDoseElement: function(doseElement)
  {
    this.doseElement = doseElement;
  },
  /**
   * @returns {array|null}
   */
  getTimedDoseElements: function()
  {
    return this.timedDoseElements;
  },
  /**
   * @param {Array<*>|null} timedDoseElements
   */
  setTimedDoseElements: function(timedDoseElements)
  {
    this.timedDoseElements = timedDoseElements;
  },

  /**
   * @returns {boolean}
   */
  hasUniversalIngredient: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.getMedication()))
    {
      if (this.getMedication().isMedicationUniversal())
      {
        return true;
      }
    }
    else if (!tm.jquery.Utils.isEmpty(this.getIngredientsList()))
    {
      for (var i = 0; i < this.getIngredientsList().length; i++)
      {
        if (this.getIngredientsList()[i].medication.isMedicationUniversal())
        {
          return true;
        }
      }
    }
    return false;
  },

  /**
   * @returns {boolean}
   */
  hasNonUniversalIngredient: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.getMedication()))
    {
      if (!this.getMedication().isMedicationUniversal())
      {
        return true;
      }
    }
    else if (!tm.jquery.Utils.isEmpty(this.getIngredientsList()))
    {
      for (var i = 0; i < this.getIngredientsList().length; i++)
      {
        if (!this.getIngredientsList()[i].medication.isMedicationUniversal())
        {
          return true;
        }
      }
    }
    return false;
  },

  /**
   * @returns {String}
   */
  getQuantityUnit: function()
  {
    return this.quantityUnit;
  },

  /**
   * @param {String} quantityUnit
   */
  setQuantityUnit: function(quantityUnit)
  {
    this.quantityUnit = quantityUnit;
  },

  /**
   * @returns {Array|null}
   */
  getDoseTimes: function()
  {
    return this.doseTimes;
  },
  /**
   * @param {Array|null} value
   */
  setDoseTimes: function(value)
  {
    this.doseTimes = value;
  },

  /**
   * @returns {Object}
   */
  getPrescriptionSupply: function()
  {
    return this.prescriptionSupply;
  },
  /**
   * @param {Object} value
   */
  setPrescriptionSupply: function(value)
  {
    this.prescriptionSupply = value;
  },

  /**
   * @returns {Number|null}
   */
  getBnfMaximumPercentage: function()
  {
    return this.bnfMaximumPercentage;
  },
  /**
   * @param {Number|null} value
   */
  setBnfMaximumPercentage: function(value)
  {
    this.bnfMaximumPercentage = value;
  },

  /**
   * @returns {boolean}
   */
  isTaggedForPrescription: function()
  {
    return this.getTags().some(isPrescriptionTag);

    function isPrescriptionTag(tag)
    {
      return tag === app.views.medications.TherapyEnums.therapyTag.PRESCRIPTION;
    }
  },

  /**
   * @returns {boolean}
   */
  isNormalVariableInfusion: function()
  {
    return this.isOrderTypeComplex() &&
        tm.jquery.Utils.isArray(this.getTimedDoseElements()) &&
        this.getTimedDoseElements().length > 0 && !this.isContinuousInfusion();
  },

  /**
   * @returns {app.views.medications.common.dto.Medication|null}
   */
  getMainMedication: function()
  {
    if (this.isOrderTypeComplex())
    {
      return this.getIngredientsList()[0].medication;
    }

    return this.getMedication();
  },

  /**
   * @returns {Array<String>}
   */
  getAllIngredientIds: function()
  {
    var idsArray = [];
    if (this.getIngredientsList())
    {
      this.getIngredientsList().forEach(function(ingredient)
      {
        if (!ingredient.medication.isMedicationUniversal())
        {
          idsArray.push(ingredient.medication.getId());
        }
      });
    }
    else
    {
      if (this.getMedication() && !this.getMedication().isMedicationUniversal())
      {
        idsArray.push(this.getMedication().getId());
      }
    }
    return idsArray;
  },

  /**
   * @returns {boolean}
   */
  isTherapyWithDurationAdministrations: function()
  {
    return this.isOrderTypeComplex() && this.isDoseTypeWithRate();
  },

  /**
   * @returns {boolean}
   */
  isDoseTypeWithRate: function()
  {
    var enums = app.views.medications.TherapyEnums;

    return this.getDoseType() === enums.therapyDoseTypeEnum.RATE || 
        this.getDoseType() === enums.therapyDoseTypeEnum.RATE_QUANTITY || 
        this.getDoseType() === enums.therapyDoseTypeEnum.RATE_VOLUME_SUM;
  },

  /**
   * Fixes therapy start to corresponding date, fixes therapy end accordingly unless specified otherwise
   * @param {Boolean} clearEnd
   */
  rescheduleTherapyTimings: function(clearEnd)
  {
    var timingUtils = tm.views.medications.MedicationTimingUtils;
    var enums = app.views.medications.TherapyEnums;

    var nextAdministrationTimestamp;
    if (this.isVariable())
    {
      if (this.isDoseTypeWithRate())
      {
        nextAdministrationTimestamp =
            timingUtils.getNextAdministrationTimestampForVarioWithRate(this.getTimedDoseElements());
      }
      else
      {
        nextAdministrationTimestamp =
            timingUtils.getNextAdministrationTimestampForVario(this.getTimedDoseElements())
      }
    }
    else
    {
      var pattern = null;
      if (!tm.jquery.Utils.isEmpty(this.getDosingFrequency()) &&
          this.getDosingFrequency().type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        var frequencyKey = timingUtils.getFrequencyKey(this.getDosingFrequency());
        pattern = timingUtils.getPatternForFrequencyBetweenHours(this.getDoseTimes()[0], frequencyKey);
      }
      else
      {
        pattern = this.getDoseTimes();
      }
      var nextTime = timingUtils.getNextTimeFromPattern(pattern, this.getDaysOfWeek());
      nextAdministrationTimestamp = nextTime ? nextTime.time : timingUtils.getTimestampRoundedUp(CurrentTime.get(), 5);
    }
    var initialTherapyDuration = this.getTherapyDurationInMilliseconds();

    this.setStart(nextAdministrationTimestamp ? nextAdministrationTimestamp : CurrentTime.get());

    if (!tm.jquery.Utils.isEmpty(this.getDosingFrequency()) &&
        enums.dosingFrequencyTypeEnum.ONCE_THEN_EX === this.getDosingFrequency().type)
    {
      if (!tm.jquery.Utils.isEmpty(this.getDoseElement()) &&
          !tm.jquery.Utils.isEmpty(this.getDoseElement().duration))
      {
        this.setEnd(moment(this.getStart()).add(this.getDoseElement().duration, 'minutes').toDate());
      }
      else
      {
        this.setEnd(new Date(this.getStart().getTime()));
      }
    }
    else if (clearEnd)
    {
      this.setEnd(null);
    }
    else
    {
      if (initialTherapyDuration)
      {
        this.setEnd(new Date(this.getStart().getTime() + initialTherapyDuration));
      }
      else
      {
        this.setEnd(null);
      }
    }

    if (tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(this.getTimedDoseElements()))
    {
      this.rescheduleVariableDaysDoseTherapyTiming();
    }
  },

  /**
   * Fixes variable dose timing
   */
  rescheduleVariableDaysDoseTherapyTiming: function()
  {
    var firstDate = null;
    for (var i = 0; i < this.getTimedDoseElements().length; i++)
    {
      var date = new Date(this.getTimedDoseElements()[i].date);
      if (firstDate === null || date < firstDate)
      {
        firstDate = date;
      }
    }
    var today = CurrentTime.get();
    today.setHours(0, 0, 0, 0);
    var offset = today.getTime() - firstDate.getTime();

    for (var j = 0; j < this.getTimedDoseElements().length; j++)
    {
      this.getTimedDoseElements()[j].date = new Date(new Date(this.getTimedDoseElements()[j].date).getTime() + offset);
    }

    var therapyInterval =
        tm.views.medications.MedicationTimingUtils.getVariableDaysTherapyInterval(this.getTimedDoseElements());
    this.setStart(therapyInterval.start);
    this.setEnd(therapyInterval.end);
  },

  /**
   * Returns therapy duration in milliseconds
   * @returns {Long}
   */
  getTherapyDurationInMilliseconds: function()
  {
    return this.getStart() && this.getEnd() ? this.getEnd() - this.getStart() : null;
  },


  /**
   * Returns first infusion ingredient medication id for complex, or therapy medication id for other therapies
   * @returns {Number}
   */
  getMedicationId: function ()
  {
    if (this.isOrderTypeComplex())
    {
      return this.getIngredientsList()[0].medication.getId();
    }
    else
    {
      return this.getMedication().getId();
    }
  }
});