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
Class.define('app.views.medications.timeline.administration.AdministrationWarningsProvider', 'tm.jquery.Object', {
  view: null,
  plannedDoseTimeValidator: null,
  administration: null,
  administrations: null,
  administrationType: null,
  therapy: null,
  infusionActive: true,
  therapyReviewedUntil: null,

  _nextAdministrationDisplayValue: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._assertInfusionActive();
  },

  /**
   * Fixes infusionActive property for variable infusions with rate and start/stop tasks
   * @private
   */
  _assertInfusionActive: function()
  {
    var enums = app.views.medications.TherapyEnums;
    if (!this.getAdministration() ||
        (this.getAdministration().administrationType !== enums.administrationTypeEnum.START &&
        this.getAdministration().administrationType !== enums.administrationTypeEnum.STOP))
    {
      if (!this.getTherapy().isContinuousInfusion())
      {
        this.infusionActive = this._isStartTaskConfirmed();
      }
    }
    else
    {
      this.infusionActive = true;
    }
  },

  /**
   * @param {Array} administrations
   * @param {Date} when
   * @returns {Array}
   * @private
   */
  _getTaskFor24HoursFromTime: function(administrations, when)
  {
    var enums = app.views.medications.TherapyEnums;
    if (administrations == null || administrations.length == 0)
    {
      return 0;
    }

    var whenMillis = when.getTime();
    var twentyFourHoursInMillis = 86400000;

    var tasksInInterval = [];
    for (var i = 0; i < administrations.length; i++)
    {
      var administration = administrations[i];
      if (administration.administrationStatus !== enums.administrationStatusEnum.FAILED)
      {
        var administrationTimeMillis =
            tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(administration).getTime();
        if ((whenMillis >= administrationTimeMillis) && ((whenMillis - administrationTimeMillis) < twentyFourHoursInMillis))
        {
          tasksInInterval.push(administration);
        }
      }
      if (whenMillis < administrationTimeMillis)
      {
        break;
      }
    }
    return tasksInInterval;
  },

  /**
   * @param {Array} administrations
   * @param {Date} when
   * @returns {Array}
   * @private
   */
  _getAdministrationsTimestampsFor24HourInterval: function(administrations, when)
  {
    var timestamps = [];
    var timePlus24Hours = when.getTime() + 86400000;
    for (var i = 0; i < administrations.length; i++)
    {
      var administration = administrations[i];
      var administrationTime = tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(administration);
      if (when.getTime() < administrationTime.getTime() && timePlus24Hours > administrationTime)
      {
        timestamps.push(administrationTime);
      }
      if (timePlus24Hours < administrationTime)
      {
        break;
      }
    }
    return timestamps;
  },

  /**
   * @param {Array} tasksInInterval
   * @param {Date} selectedTimestamp
   * @private
   */
  _assertNextAllowedAdministrationTime: function(tasksInInterval, selectedTimestamp)
  {
    var view = this.getView();
    var timingUtils = tm.views.medications.MedicationTimingUtils;
    //gets first task for interval where maxDailyFrequency isn't reached
    tasksInInterval.sort(function(administrationA, administrationB)
    {
      var administrationTimeAMillis = timingUtils.getAdministrationTimestamp(administrationA).getTime();
      var administrationTimeBMillis = timingUtils.getAdministrationTimestamp(administrationB).getTime();
      return administrationTimeAMillis - administrationTimeBMillis;
    });

    while (tasksInInterval.length > this.getTherapy().getMaxDailyFrequency())
    {
      tasksInInterval.shift();
      tasksInInterval = this._getTaskFor24HoursFromTime(tasksInInterval, selectedTimestamp);
    }
    var nextAdministration = timingUtils.getAdministrationTimestamp(tasksInInterval[0]);
    nextAdministration.setDate(nextAdministration.getDate() + 1);

    var tasksInNextInterval = this._getTaskFor24HoursFromTime(this.getAdministrations(), nextAdministration);
    //assertNumberOfAllowedAdministrations for the calculated nextAdministration time - repeat until you find appropriate value
    if (tasksInNextInterval.length >= this.getTherapy().getMaxDailyFrequency())
    {
      this._assertNextAllowedAdministrationTime(tasksInNextInterval, nextAdministration);
    }
    else
    {
      this._nextAdministrationDisplayValue = view.getDisplayableValue(nextAdministration, "short.date.time");
    }
  },

  /**
   * @returns {boolean}
   * @private
   */
  _isStartTaskConfirmed: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var firstPreviousStartAdministration = null;
    if (this.getAdministration() && this.getAdministration().administrationType === enums.administrationTypeEnum.START)
    {
      return true;
    }
    else
    {
      for (var i = this.getAdministrations().indexOf(this.getAdministration()) - 1; i >= 0; i--)
      {
        var administration = this.getAdministrations()[i];
        if (administration.administrationType === enums.administrationTypeEnum.START)
        {
          firstPreviousStartAdministration = administration;
          break;
        }
      }
      if (firstPreviousStartAdministration)
      {
        return (firstPreviousStartAdministration.administrationStatus === enums.administrationStatusEnum.COMPLETED ||
        firstPreviousStartAdministration.administrationStatus === enums.administrationStatusEnum.COMPLETED_EARLY ||
        firstPreviousStartAdministration.administrationStatus === enums.administrationStatusEnum.COMPLETED_LATE)
      }
      else
      {
        return true;
      }
    }
  },

  /**
   * @param {Date} selectedTimestamp
   * @returns {boolean}
   */
  isAdministrationJump: function(selectedTimestamp)
  {
    var enums = app.views.medications.TherapyEnums;

    if ((this.getTherapy().isContinuousInfusion() || this.getTherapy().isOrderTypeOxygen()) &&
        !this.getAdministration() &&
        this.getAdministrationType() !== enums.administrationTypeEnum.INFUSION_SET_CHANGE)
    {
      return !this.getPlannedDoseTimeValidator().isChangeAcceptable(
          CurrentTime.get(), selectedTimestamp);
    }

    if (this.getAdministration())
    {
      // Jump check for infusion with rate, since we must allow for start task to jump its complementary end task
      if (this.getTherapy().isDoseTypeWithRate() && !this.getTherapy().isContinuousInfusion())
      {
        return this.getPlannedDoseTimeValidator().isRateAdministrationJump(selectedTimestamp, this.getAdministration());
      }


      var administrationTime = tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(this.getAdministration());

      if (administrationTime)
      {
        return !this.getPlannedDoseTimeValidator().isChangeAcceptable(
            administrationTime, selectedTimestamp);
      }
    }

    return false;
  },

  /**
   * @param {Date} selectedTimestamp
   * @returns {boolean}
   */
  isAdministrationTooFarInFuture: function(selectedTimestamp)
  {
    var limitTimestamp = CurrentTime.get();
    limitTimestamp.setMinutes(limitTimestamp.getMinutes() + 5);
    return limitTimestamp < selectedTimestamp;
  },

  /**
   * @param {Date} selectedTimestamp
   * @returns {boolean}
   */
  isNumberOfAllowedAdministrationsReached: function(selectedTimestamp)
  {
    if (!tm.jquery.Utils.isEmpty(this.getTherapy().getMaxDailyFrequency()) && !tm.jquery.Utils.isEmpty(this.getAdministrations()))
    {
      if (selectedTimestamp)
      {
        var administrationTimestamps =
            this._getAdministrationsTimestampsFor24HourInterval(this.getAdministrations(), selectedTimestamp);
        administrationTimestamps.unshift(selectedTimestamp);

        for (var i = 0; i < administrationTimestamps.length; i++)
        {
          var administrationTimestamp = administrationTimestamps[i];
          var tasksInInterval = this._getTaskFor24HoursFromTime(this.getAdministrations(), administrationTimestamp);
          if (tasksInInterval.length >= this.getTherapy().getMaxDailyFrequency())
          {
            this._assertNextAllowedAdministrationTime(tasksInInterval, administrationTimestamp);
            return true;
          }
        }
      }
    }
    return false;
  },

  /**
   * @param {Date} selectedTimestamp
   * @returns {boolean}
   */
  isTherapyNotReviewed: function(selectedTimestamp)
  {
    return !(this.getTherapyReviewedUntil() >= selectedTimestamp);
  },

  /**
   * @param {Date} selectedTimestamp
   * @param {boolean} withJumpWarning
   * @param {boolean} withFutureWarning
   * @param {boolean} withMaxAdministrationsReachedWarning
   * @param {boolean} withTherapyNotReviewedWarning
   * @returns {app.views.medications.timeline.administration.AdministrationWarnings}
   */
  getRestrictiveAdministrationWarnings: function(selectedTimestamp,
                                                 withJumpWarning,
                                                 withFutureWarning,
                                                 withMaxAdministrationsReachedWarning,
                                                 withTherapyNotReviewedWarning)
  {
    var enums = app.views.medications.TherapyEnums;

    var warnings = new app.views.medications.timeline.administration.AdministrationWarnings();

    if (withJumpWarning)
    {
      if (this.isAdministrationJump(selectedTimestamp))
      {
        warnings.setJumpWarning(this.getView().getDictionary('therapy.administration.jump.check'));
      }
    }

    if (withFutureWarning)
    {
      if (this.isAdministrationTooFarInFuture(selectedTimestamp))
      {
        warnings.setAdministrationInFutureWarning(
            this.getView().getDictionary('therapy.administration.is.in.future.warning'));
      }
    }

    if (withMaxAdministrationsReachedWarning)
    {
      if (this.isNumberOfAllowedAdministrationsReached(selectedTimestamp))
      {
        warnings.setMaxAdministrationsWarning(this.getView().getDictionary('dosing.max.24h.warning') + " " +
            "<span><b>" + this._nextAdministrationDisplayValue + "</b></span>");
      }
    }

    if (this.getTherapy().isOrderTypeComplex())
    {
      if (this.getInfusionActive() === false)
      {
        warnings.setInfusionInactiveWarning((this.getTherapy().isContinuousInfusion() ?
            this.getView().getDictionary("infusion.not.active.warning") :
            this.getView().getDictionary("first.administration.not.confirmed.warning")));
      }
    }
    if (withTherapyNotReviewedWarning && this.getView().isDoctorReviewRequired())
    {
      if (this.isTherapyNotReviewed(selectedTimestamp) &&
          (!this.getAdministration() ||
          this.getAdministration().administrationType !== enums.administrationTypeEnum.STOP))
      {
        warnings.setTherapyNotReviewedWarning(this.getView().getDictionary('therapy.administration.not.reviewed.warning'));
      }
    }
    return warnings;
  },

  /**
   * @param {object} medicationIngredientDto
   * @returns {null | string}
   */
  getMedicationIngredientRuleHtml: function(medicationIngredientDto)
  {
    var view = this.getView();
    if (!tm.jquery.Utils.isEmpty(medicationIngredientDto))
    {
      var paracetamolWarning = "";
      if (medicationIngredientDto.quantityOk === false && tm.jquery.Utils.isEmpty(medicationIngredientDto.errorMessage))
      {
        var percentage = medicationIngredientDto.underageRulePercentage >= medicationIngredientDto.adultRulePercentage
            ? medicationIngredientDto.underageRulePercentage
            : medicationIngredientDto.adultRulePercentage;

        paracetamolWarning += view.getDictionary("paracetamol.daily.dose.24") + " " + percentage + "%.";
      }

      if (medicationIngredientDto.betweenDosesTimeOk ===
          false && !tm.jquery.Utils.isEmpty(medicationIngredientDto.lastTaskTimestamp))
      {
        var lastAdministration = new Date(medicationIngredientDto.lastTaskTimestamp);
        if (!tm.jquery.Utils.isEmpty(paracetamolWarning))
        {
          paracetamolWarning += "<br> <br>";
        }

        var dictionaryEntry = medicationIngredientDto.lastTaskAdministered === true
            ? view.getDictionary("medication.with.ingredient.already.given")
            : view.getDictionary("medication.with.ingredient.already.scheduled");

        paracetamolWarning += tm.jquery.Utils.formatMessage(
            dictionaryEntry,
            ["Paracetamol", "4", view.getDisplayableValue(lastAdministration, "short.time")]);
      }

      return paracetamolWarning;
    }
    return null;
  },

  /**
   * @returns {object}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns Array{object}
   */
  getAdministrations: function()
  {
    return this.administrations;
  },

  /**
   * @returns {object | null}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * @returns {string | null} app.views.medications.TherapyEnums.administrationTypeEnum
   */
  getAdministrationType: function()
  {
    return this.administrationType;
  },

  /**
   * @returns {object}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.timeline.administration.PlannedDoseTimeValidator|null}
   */
  getPlannedDoseTimeValidator: function()
  {
    return this.plannedDoseTimeValidator;
  },

  /**
   * @returns {boolean}
   */
  getInfusionActive: function()
  {
    return this.infusionActive;
  },

  /**
   * @returns {Date}
   */
  getTherapyReviewedUntil: function()
  {
    return this.therapyReviewedUntil;
  }

});

Class.define('app.views.medications.timeline.administration.AdministrationWarnings', 'tm.jquery.Object', {
  jumpWarning: null,
  administrationInFutureWarning: null,
  maxAdministrationsWarning: null,
  infusionInactiveWarning: null,
  therapyNotReviewedWarning: null,
  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {String | null}
   */
  getJumpWarning: function()
  {
    return this.jumpWarning;
  },

  /**
   * @param {String} jumpWarning
   */
  setJumpWarning: function(jumpWarning)
  {
    this.jumpWarning = jumpWarning;
  },

  /**
   * @returns {String | null}
   */
  getAdministrationInFutureWarning: function()
  {
    return this.administrationInFutureWarning;
  },

  /**
   * @param {String} administrationInFutureWarning
   */
  setAdministrationInFutureWarning: function(administrationInFutureWarning)
  {
    this.administrationInFutureWarning = administrationInFutureWarning;
  },

  /**
   * @returns {String | null}
   */
  getMaxAdministrationsWarning: function()
  {
    return this.maxAdministrationsWarning;
  },

  /**
   * @param {String} maxAdministrationsWarning
   */
  setMaxAdministrationsWarning: function(maxAdministrationsWarning)
  {
    this.maxAdministrationsWarning = maxAdministrationsWarning;
  },

  /**
   * @returns {String | null}
   */
  getInfusionInactiveWarning: function()
  {
    return this.infusionInactiveWarning;
  },

  /**
   * @param {String} infusionInactiveWarning
   */
  setInfusionInactiveWarning: function(infusionInactiveWarning)
  {
    this.infusionInactiveWarning = infusionInactiveWarning;
  },

  /**
   * @returns {String | null}
   */
  getTherapyNotReviewedWarning: function()
  {
    return this.therapyNotReviewedWarning;
  },

  /**
   * @param {String} therapyNotReviewedWarning
   */
  setTherapyNotReviewedWarning: function(therapyNotReviewedWarning)
  {
    this.therapyNotReviewedWarning = therapyNotReviewedWarning;
  },

  /**
   * @returns {boolean}
   */
  hasRestrictiveWarnings: function()
  {
    return Boolean(this.getAdministrationInFutureWarning() ||
            this.getInfusionInactiveWarning() ||
            this.getJumpWarning() ||
            this.getMaxAdministrationsWarning() ||
            this.getTherapyNotReviewedWarning())
  }

});