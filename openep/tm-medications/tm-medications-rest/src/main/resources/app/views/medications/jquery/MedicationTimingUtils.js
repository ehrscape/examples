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

Class.define('tm.views.medications.MedicationTimingUtils', 'tm.jquery.Object', {

  /** statics */
  statics: {
    daysOfWeek: {
      "SUNDAY": 0,
      "MONDAY": 1,
      "TUESDAY": 2,
      "WEDNESDAY": 3,
      "THURSDAY": 4,
      "FRIDAY": 5,
      "SATURDAY": 6
    },
    /** private methods */
    _getFrequencyModeAdministrationTimes: function(administrationTiming, frequencyModeKey)
    {
      if (administrationTiming)
      {
        if (frequencyModeKey == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.DAILY_COUNT ||
            frequencyModeKey == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.ONCE_THEN_EX)
        {
          return this._getFrequencyModeKeyAdministrationTimes(administrationTiming, "X");
        }
        else if (frequencyModeKey == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
        {
          return this._getFrequencyModeKeyAdministrationTimes(administrationTiming, "H");
        }
      }
      return [];
    },

    _getFrequencyModeKeyAdministrationTimes: function(administrationTiming, modeString)
    {
      var administrationTimesOfDay = [];
      for (var i = 0; i < administrationTiming.timestampsList.length; i++)
      {
        var isFrequencyOfType = this._isFrequencyOfType(administrationTiming.timestampsList[i].frequency, modeString);
        if (isFrequencyOfType)
        {
          var administrationTimesForFrequency = administrationTiming.timestampsList[i].timesList;
          this._pushAdministrationTimesToList(administrationTimesForFrequency, administrationTimesOfDay);
        }
      }
      this._sortAdministrationTimesOfDay(administrationTimesOfDay);
      return administrationTimesOfDay;
    },

    _isFrequencyOfType: function(frequency, type)
    {
      var frequencyType = frequency.slice(-1);
      return type ? type == frequencyType : true;
    },

    _pushAdministrationTimesToList: function(timesList, list)
    {
      for (var j = 0; j < timesList.length; j++)
      {
        list.push({
          hour: timesList[j].hour,
          minute: timesList[j].minute
        });
      }
    },

    _sortAdministrationTimesOfDay: function(list)
    {
      list.sort(function(a, b)
      {
        var minutesA = a.hour * 60 + a.minute;
        var minutesB = b.hour * 60 + b.minute;
        return minutesA - minutesB;
      });
    },

    _adjustToDaysOfWeek: function(date, daysOfWeek)
    {
      var daysToFirstSuitableDayOfWeek = this._getDaysToFirstSuitableDayOfWeek(date, daysOfWeek);
      date.setDate(date.getDate() + daysToFirstSuitableDayOfWeek);
      return date;
    },

    _getDaysToFirstSuitableDayOfWeek: function(startDate, daysOfWeek)
    {
      if (daysOfWeek == null || daysOfWeek.length == 0 || daysOfWeek.length == 7)
      {
        return 0;
      }
      var dayCounter = 0;
      var dayOfWeekIndex = startDate.getDay();
      for (var i = startDate.getDay(); i < startDate.getDay() + 7; i++)
      {
        for (var j = 0; j < daysOfWeek.length; j++)
        {
          var suitableDayOfWeekIndex = tm.views.medications.MedicationTimingUtils._getDayOfWeekIndex(daysOfWeek[j]);
          if (dayOfWeekIndex == suitableDayOfWeekIndex)
          {
            return dayCounter;
          }
        }
        dayCounter++;
        dayOfWeekIndex = (dayOfWeekIndex + 1) % 7;
      }
      //should never happen
      return 0;
    },

    _getDayOfWeekIndex: function(day)
    {
      return this.daysOfWeek[day];
    },

    /** public methods */
    getFrequencyAdministrationTimesOfDay: function(administrationTiming, frequency)
    {
      if (administrationTiming)
      {
        for (var i = 0; i < administrationTiming.timestampsList.length; i++)
        {
          if (administrationTiming.timestampsList[i].frequency == frequency)
          {
            var administrationTimesOfDay = administrationTiming.timestampsList[i].timesList;
            var administrationHourMinutes = [];
            for (var j = 0; j < administrationTimesOfDay.length; j++)
            {
              administrationHourMinutes.push({
                hour: administrationTimesOfDay[j].hour,
                minute: administrationTimesOfDay[j].minute
              });
            }
            return administrationHourMinutes;
          }
        }
      }
      return [];
    },

    hourMinuteToString: function(hour, minute)
    {
      return tm.views.medications.MedicationUtils.pad(hour, 2) + ":" +
          tm.views.medications.MedicationUtils.pad(minute, 2);
    },

    getDayOfWeekDisplay: function(view, day, onlyFirstThreeLetters)
    {
      var dayOfWeekString = view.getDictionary("DayOfWeek." + day);
      return onlyFirstThreeLetters ? dayOfWeekString.substring(0, 3) : dayOfWeekString;
    },

    getNextAdministrationTimestamp: function(frequencyKey, frequencyModeKey, daysOfWeek, administrationTiming)
    {
      if (frequencyKey || frequencyModeKey == "WITHOUT_FREQUENCY" || frequencyModeKey == "DAYS_ONLY")
      {
        var startDate = new Date();
        var currentTimeInMinutes = startDate.getHours() * 60 + startDate.getMinutes();

        if (frequencyModeKey == "WITHOUT_FREQUENCY")
        {
          startDate.setMinutes(startDate.getMinutes() + 5);
          return this.getTimestampRoundedUp(startDate, 5);
        }
        else if (frequencyModeKey == "DAYS_ONLY")
        {
          return this._adjustToDaysOfWeek(startDate, daysOfWeek);
        }

        var administrationTimes = this.getFrequencyAdministrationTimesOfDay(administrationTiming, frequencyKey);
        if (administrationTimes.length <= 0)
        {
          administrationTimes = this._getFrequencyModeAdministrationTimes(administrationTiming, frequencyModeKey);
        }

        if (administrationTimes.length > 0)
        {
          for (var j = 0; j < administrationTimes.length; j++)
          {
            var administrationTimeInMinutes =
                administrationTimes[j].hour * 60 + administrationTimes[j].minute;
            if (currentTimeInMinutes < administrationTimeInMinutes)
            {
              startDate.setHours(administrationTimes[j].hour);
              startDate.setMinutes(administrationTimes[j].minute);
              return this._adjustToDaysOfWeek(startDate, daysOfWeek);
            }
          }
          //first administration in next day
          startDate.setHours(administrationTimes[0].hour);
          startDate.setMinutes(administrationTimes[0].minute);
          startDate.setDate(startDate.getDate() + 1);
          return this._adjustToDaysOfWeek(startDate, daysOfWeek);
        }
      }
      return null;
    },
    //calculates text allowed administration time for "PRN" (when needed) therapies
    getNextAllowedAdministrationTimeForPRN: function(dosingFrequency, timestamp)
    {
      var enums = app.views.medications.TherapyEnums;
      if (dosingFrequency)
      {
        if (dosingFrequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
        {
          return new Date(timestamp.getTime() + dosingFrequency.value * 60 * 60 * 1000)
        }
        if (dosingFrequency.type == enums.dosingFrequencyTypeEnum.DAILY_COUNT)
        {
          var hoursUntilNextDose = 24 / dosingFrequency.value;
          return new Date(timestamp.getTime() + hoursUntilNextDose * 60 * 60 * 1000)
        }
      }
      return null;
    },

    getNextAdministrationTimestampForVario: function(timedDoseElements)
    {
      if (!timedDoseElements)
      {
        return null;
      }
      var now = new Date();
      var day = new Date();
      var index = 0;
      var doseTime = timedDoseElements[index].doseTime;
      var administrationTimestamp = new Date(day.getFullYear(), day.getMonth(), day.getDate(), doseTime.hour, doseTime.minute, 0, 0);
      while (administrationTimestamp < now)
      {
        if (index < timedDoseElements.length - 1)
        {
          index++;
        }
        else
        {
          index = 0;
          day = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1, day.getHours(), day.getMinutes(), 0, 0);
        }
        doseTime = timedDoseElements[index].doseTime;
        administrationTimestamp = new Date(day.getFullYear(), day.getMonth(), day.getDate(), doseTime.hour, doseTime.minute, 0, 0);
      }
      return administrationTimestamp;
    },

    getTimestampRoundedUp: function(timestamp, roundToMinutes)
    {
      var date = new Date(timestamp);
      while (date.getMinutes() % roundToMinutes != 0)
      {
        date.setMinutes(date.getMinutes() + 1);
      }
      return date;
    },
    getFrequencyKey: function(dosingFrequency)
    {
      var enums = app.views.medications.TherapyEnums;
      if (dosingFrequency)
      {
        if (dosingFrequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
        {
          return dosingFrequency.value + 'H';
        }
        if (dosingFrequency.type == enums.dosingFrequencyTypeEnum.DAILY_COUNT)
        {
          return dosingFrequency.value + 'X';
        }
        return dosingFrequency.type;
      }
      return null;
    },
    getWorkdayInterval: function(roundsInterval, when)
    {
      if (!roundsInterval)
      {
        return null;
      }
      var todaysRoundsStart = new Date(when.getFullYear(), when.getMonth(), when.getDate(), roundsInterval.startHour, roundsInterval.startMinute, 0, 0);

      if (when > todaysRoundsStart)
      {
        //todays workday
        return {
          start: todaysRoundsStart,
          end: new Date(todaysRoundsStart.getTime() + 24 * 60 * 60 * 1000)
        }
      }
      else
      {
        //yesterdays workday
        return {
          start: new Date(todaysRoundsStart.getTime() - 24 * 60 * 60 * 1000),
          end: new Date(todaysRoundsStart)
        }
      }
    }
  }}
);
