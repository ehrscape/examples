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
Class.define('app.views.medications.timeline.administration.PlannedDoseTimeValidator', 'tm.jquery.Object', {

  _plannedTimes: null,
  administrations: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._mapPlannedTimesByAdministrations();
  },
  
  /**
   * @param {Array<Object> | null} administrations
   */
  setAdministrations: function(administrations)
  {
    this.administrations = administrations;
    this._mapPlannedTimesByAdministrations();
  },

  /**
   * @private
   */
  _mapPlannedTimesByAdministrations: function()
  {
    var administrations = this.getAdministrations();
    this._plannedTimes = tm.jquery.Utils.isArray(administrations) ?
        administrations.map(function extractTimes(administration)
        {
          return tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(administration);
        })
        : [];
  },

  /**
   * @param {Date | String} originalPlannedTime
   * @param {Date | String} desiredPlannedTime
   * @returns {boolean}
   */
  isChangeAcceptable: function(originalPlannedTime, desiredPlannedTime)
  {
    originalPlannedTime = tm.jquery.Utils.isDate(originalPlannedTime) ? originalPlannedTime : new Date(originalPlannedTime);
    desiredPlannedTime = tm.jquery.Utils.isDate(desiredPlannedTime) ? desiredPlannedTime : new Date(desiredPlannedTime);

    var pastAdministrations = [];
    var futureAdministrations = [];
    var selectedTimeAcceptable = false;

    this.getPlannedTimes().forEach(function(plannedTime)
    {
      if (plannedTime.getTime() !== originalPlannedTime.getTime())
      {
        if (plannedTime < originalPlannedTime)
        {
          pastAdministrations.push(plannedTime);
        }
        else
        {
          futureAdministrations.push(plannedTime);
        }
      }
    }, this);

    var hasPast = pastAdministrations.length > 0;
    var hasFuture = futureAdministrations.length > 0;

    if (hasPast && hasFuture)
    {
      return desiredPlannedTime > pastAdministrations[pastAdministrations.length - 1] &&
          desiredPlannedTime < futureAdministrations[0];
    }
    else if (hasPast && !hasFuture)
    {
      return desiredPlannedTime > pastAdministrations[pastAdministrations.length - 1];
    }
    else if (!hasPast && hasFuture)
    {
      return desiredPlannedTime < futureAdministrations[0];
    }

    return true;
  },
  
  /**
   * @returns {Array<Date>}
   */
  getPlannedTimes: function()
  {
    return this._plannedTimes;
  },

  /**
   * Assert if selected timestamp complies with prescribed dosing interval
   * @param {app.views.medications.TherapyEnums.dosingFrequencyTypeEnum} dosingFrequency
   * @param {Date} selectedTimestamp
   * @returns {boolean}
   */
  assertPrnTimeTooSoon: function(selectedTimestamp, dosingFrequency)
  {
    var lastAdministrationTime = null;
    for (var j = 0; j < this.getAdministrations().length; j++)
    {
      var administration = this.getAdministrations()[j];

      var administrationStart = tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(administration);
      if (!lastAdministrationTime ||
          (administrationStart.getTime() > lastAdministrationTime.getTime() &&
          administrationStart < selectedTimestamp))
      {
        lastAdministrationTime = administrationStart;
      }
    }
    if (lastAdministrationTime)
    {
      var nextAllowedAdministrationTime = tm.views.medications.MedicationTimingUtils.getNextAllowedAdministrationTimeForPRN(
          dosingFrequency,
          lastAdministrationTime);
    }

    return selectedTimestamp > lastAdministrationTime && selectedTimestamp < nextAllowedAdministrationTime;
  },

  /**
   * Assert if two rate change administrations are within 5 mins. of each other
   * @param {number} timestamp
   * @param {Object} administration
   * @returns {boolean}
   */
  assertRateChangesTooClose: function(timestamp, administration)
  {
    var enums = app.views.medications.TherapyEnums;
    var timingUtils = tm.views.medications.MedicationTimingUtils;
    var futureAndPastAdministrations =
        this._getFutureAndPastAdministrations(timestamp, administration);
    var lastPastInfusionChangeAdministration = null;
    var firstFutureInfusionChangeAdministration = null;
    if (futureAndPastAdministrations.pastAdministrations)
    {
      for (var i = futureAndPastAdministrations.pastAdministrations.length - 1; i >= 0; i--)
      {
        if (futureAndPastAdministrations.pastAdministrations[i].administrationType ===
            enums.administrationTypeEnum.ADJUST_INFUSION)
        {
          lastPastInfusionChangeAdministration = futureAndPastAdministrations.pastAdministrations[i];
          break;
        }
      }
    }
    if (futureAndPastAdministrations.futureAdministrations)
    {
      for (var j = 0; j < futureAndPastAdministrations.futureAdministrations.length; j++)
      {
        if (futureAndPastAdministrations.futureAdministrations[j].administrationType ===
            enums.administrationTypeEnum.ADJUST_INFUSION)
        {
          firstFutureInfusionChangeAdministration = futureAndPastAdministrations.futureAdministrations[j];
          break;
        }
      }
    }

    return ((lastPastInfusionChangeAdministration &&
    getAbsoluteDifferenceInMinutes(lastPastInfusionChangeAdministration) < 5) ||
    (firstFutureInfusionChangeAdministration &&
    getAbsoluteDifferenceInMinutes(firstFutureInfusionChangeAdministration) < 5));

    function getAbsoluteDifferenceInMinutes(administrationToCompare)
    {
      return Math.abs(timingUtils.getAdministrationTimestamp(administrationToCompare).getTime() -
              timestamp.getTime()) / (1000 * 60)
    }
  },

  /**
   * @param {number} selectedTimestamp
   * @param {Object} selectedAdministration
   * @returns {{pastAdministrations: Array, futureAdministrations: Array}}
   * @private
   */
  _getFutureAndPastAdministrations: function(selectedTimestamp, selectedAdministration)
  {
    var pastAdministrations = [];
    var futureAdministrations = [];
    this.getAdministrations().forEach(function(administration)
    {
      if (administration !== selectedAdministration)
      {
        var plannedTime = tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(administration);

        if (plannedTime < selectedTimestamp)
        {
          pastAdministrations.push(administration);
        }
        else
        {
          futureAdministrations.push(administration);
        }
      }
    });
    return {
      pastAdministrations: pastAdministrations,
      futureAdministrations: futureAdministrations
    };
  },

  /**
   * Assert if time difference is more than factor of prescribed dosing interval
   * @param {Date | String} selectedTime
   * @param {Date | String} plannedTime
   * @param {number} allowedDifferenceFactor must be 0 < n < 1
   * @param {number} dosingFrequency
   * @returns {boolean}
   */
  assertTimeDifferenceTooBig: function(selectedTime, plannedTime, allowedDifferenceFactor, dosingFrequency)
  {
    var allowedDiff = (dosingFrequency * 60) * allowedDifferenceFactor;
    return (Math.abs(selectedTime.getTime() - plannedTime.getTime())) / (1000 * 60) > allowedDiff
  },

  /**
   * @param {number} bagQuantity
   * @param {object} dose
   * @returns {boolean}
   */
  assertInsufficientQuantityForBolus: function(bagQuantity, dose)
  {
    if (dose.quantityDenominator)
    {
      if (bagQuantity < dose.quantityDenominator)
      {
        return true;
      }
    }
    else if (dose.quantity && tm.views.medications.MedicationUtils.isUnitVolumeUnit(dose.quantityUnit))
    {
      if (bagQuantity < dose.quantity)
      {
        return true;
      }
    }
    return false;
  },

  /**
   * @returns {Array}
   */
  getAdministrations: function()
  {
    return this.administrations;
  },

  /**
   * @param {Date} selectedTimestamp
   * @param {Object} administration
   * @returns {boolean}
   */
  isRateAdministrationJump: function(selectedTimestamp, administration)
  {
    var timingUtils = tm.views.medications.MedicationTimingUtils;
    var originalTimestamp = timingUtils.getAdministrationTimestamp(administration);
    var futureAndPastAdministrations =
        this._getFutureAndPastAdministrations(originalTimestamp, administration);
    var pastAdministrations = futureAndPastAdministrations.pastAdministrations;
    var futureAdministrations = futureAndPastAdministrations.futureAdministrations;
    
    if (pastAdministrations.length > 0)
    {
      var lastPastAdministration =  pastAdministrations[pastAdministrations.length - 1];
      var lastPastAdministrationTimestamp = timingUtils.getAdministrationTimestamp(lastPastAdministration);
      if (selectedTimestamp < lastPastAdministrationTimestamp)
      {
        return true;
      }
    }

    if (futureAdministrations.length > 1 &&
        selectedTimestamp > timingUtils.getAdministrationTimestamp(futureAdministrations[1]))
    {
      return true;
    }
    
    if (futureAdministrations.length > 0)
    {
      var firstFutureAdministration = futureAdministrations[0];
      var firstFutureAdministrationTimestamp = timingUtils.getAdministrationTimestamp(firstFutureAdministration);
      if (selectedTimestamp > firstFutureAdministrationTimestamp)
      {
        if (administration.groupUUId && administration.groupUUId !== firstFutureAdministration.groupUUId)
        {
          return true;
        }
      }
    }
    return false;
    
  }
});