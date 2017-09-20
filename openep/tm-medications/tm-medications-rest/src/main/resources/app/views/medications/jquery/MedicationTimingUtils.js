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
        titrationIntervalHours: {
          BLOOD_SUGAR: 48,
          INR: 24
        },
        /** private methods */
        _getFrequencyTypeAdministrationTimes: function(administrationTiming, frequencyType)
        {
          if (administrationTiming)
          {
            if (frequencyType == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.DAILY_COUNT ||
                frequencyType == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.ONCE_THEN_EX)
            {
              return this._getFrequencyTypeKeyAdministrationTimes(administrationTiming, "X");
            }
            else if (frequencyType == app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
            {
              return this._getFrequencyTypeKeyAdministrationTimes(administrationTiming, "H");
            }
          }
          return [];
        },

        _getFrequencyTypeKeyAdministrationTimes: function(administrationTiming, typeString)
        {
          var administrationTimesOfDay = [];
          for (var i = 0; i < administrationTiming.timestampsList.length; i++)
          {
            var isFrequencyOfType = this._isFrequencyOfType(administrationTiming.timestampsList[i].frequency, typeString);
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

        getFrequencyTimingPattern: function(administrationTiming, frequencyKey, frequencyType)
        {
          var enums = app.views.medications.TherapyEnums;
          var administrationTimes = this.getFrequencyAdministrationTimesOfDay(administrationTiming, frequencyKey);
          if (administrationTimes.length <= 0)
          {
            var allTimes = this._getFrequencyTypeAdministrationTimes(administrationTiming, frequencyType);

            var isOnceFrequency =
                frequencyKey == "1X" ||
                frequencyKey == "24H" ||
                frequencyType == enums.dosingFrequencyTypeEnum.ONCE_THEN_EX;
            if (isOnceFrequency && allTimes.length > 0)
            {
              var now = CurrentTime.get();
              var currentTimeInMinutes = now.getHours() * 60 + now.getMinutes();
              var nextAdministration = null;
              for (var i = 0; i < allTimes.length; i++)
              {
                var administrationTimeInMinutes =
                    allTimes[i].hour * 60 + allTimes[i].minute;
                if (administrationTimeInMinutes >= currentTimeInMinutes)
                {
                  nextAdministration = allTimes[i];
                  break;
                }
              }

              if (nextAdministration == null)
              {
                nextAdministration = allTimes[0];
              }
              administrationTimes.push(nextAdministration);
            }
            else if (frequencyKey != null) //nonstandard timing
            {
              var firstAdministration =
              {
                hour: 9,
                minute: 0
              };
              administrationTimes.push(firstAdministration);

              var betweenDoses = 24;
              var dailyCount = 1;
              if (frequencyType == enums.dosingFrequencyTypeEnum.DAILY_COUNT)
              {
                dailyCount = frequencyKey.substring(0, frequencyKey.length - 1);
                betweenDoses = 24 / dailyCount;
              }
              else if (frequencyType == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
              {
                betweenDoses = this._getHoursBetweenDosesFromFrequencyKey(frequencyKey);
                dailyCount = 24 / betweenDoses;
              }

              for (var j = 1; j < dailyCount; j++)
              {
                var nextHour = Math.ceil(firstAdministration.hour + j * betweenDoses) % 24;
                administrationTimes.push({
                  hour: nextHour,
                  minute: 0
                });
              }
            }
          }
          return administrationTimes;
        },

        _getHoursBetweenDosesFromFrequencyKey: function(frequencyKey)
        {
          return Number(frequencyKey.substring(0, frequencyKey.length - 1));
        },

        getPatternForFrequencyBetweenHours: function(firstHourMinute, frequencyKey)
        {
          var hoursBetweenDoses = this._getHoursBetweenDosesFromFrequencyKey(frequencyKey);
          var pattern = [];

          var now = CurrentTime.get();
          var time = new Date(now.getFullYear(), now.getMonth(), now.getDate(), firstHourMinute.hour, firstHourMinute.minute, 0, 0);
          var endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), firstHourMinute.hour + 24, firstHourMinute.minute, 0, 0);
          while (time < endTime)
          {
            pattern.push(
                {
                  hour: time.getHours(),
                  minute: time.getMinutes()
                });
            time = new Date(time.getFullYear(), time.getMonth(), time.getDate(), time.getHours() + hoursBetweenDoses, time.getMinutes(), 0, 0);
          }
          return pattern;
        },

        getNextTimeFromPattern: function(pattern, daysOfWeek)
        {
          var day = CurrentTime.get();
          var now = CurrentTime.get();
          var nextTime = null;
          var index = -1;

          if (pattern && pattern.length > 0)
          {
            pattern.sort(function(first, second)
            {
              var firstInMinutes = first.hour * 60 + first.minute;
              var secondInMinutes = second.hour * 60 + second.minute;
              if (firstInMinutes < secondInMinutes)
              {
                return -1;
              }
              if (firstInMinutes > secondInMinutes)
              {
                return 1;
              }
              return 0;
            });
            
            while (nextTime == null || nextTime < now)
            {
              if (index < pattern.length - 1)
              {
                index++;
              }
              else
              {
                index = 0;
                day = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1, day.getHours(), day.getMinutes(), 0, 0);
              }
              nextTime = new Date(day.getFullYear(), day.getMonth(), day.getDate(), pattern[index].hour, pattern[index].minute, 0, 0);
              nextTime = this._adjustToDaysOfWeek(nextTime, daysOfWeek);
            }
            return {
              hourMinute: pattern[index],
              time: nextTime
            }
          }
          return null;
        },

        hourMinuteToString: function(hour, minute)
        {
          return hour + ":" + tm.views.medications.MedicationUtils.pad(minute, 2);
        },

        hourMinuteToDate: function(hourMinute)
        {
          if (hourMinute)
          {
            var now = CurrentTime.get();
            return new Date(now.getFullYear(), now.getMonth(), now.getDate(), hourMinute.hour, hourMinute.minute, 0, 0);
          }
          return null;
        },

        getDateWithoutYearDisplay: function(view, date)
        {
          var dateDisplay = view.getDisplayableValue(new Date(date), "short.date");
          dateDisplay = dateDisplay.substring(0, dateDisplay.length - 5);
          return dateDisplay;
        },

        getDayOfWeekDisplay: function(view, day, onlyFirstThreeLetters)
        {
          var dayOfWeekString = view.getDictionary("DayOfWeek." + day);
          return onlyFirstThreeLetters ? dayOfWeekString.substring(0, 3) : dayOfWeekString;
        },

        //calculates next allowed administration time for "PRN" (when needed) therapies
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
          var now = CurrentTime.get();
          var day = CurrentTime.get();
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

        getNextAdministrationTimestampForVarioWithRate: function(timedDoseElements)
        {
          if (!timedDoseElements)
          {
            return null;
          }
          var now = CurrentTime.get();
          var doseTime = timedDoseElements[0].doseTime;
          var administrationTimestamp =
              new Date(now.getFullYear(), now.getMonth(), now.getDate(), doseTime.hour, doseTime.minute, 0, 0);
          if (administrationTimestamp < now)
          {
            administrationTimestamp.setDate(administrationTimestamp.getDate() + 1);
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
            };
          }
          else
          {
            //yesterdays workday
            return {
              start: new Date(todaysRoundsStart.getTime() - 24 * 60 * 60 * 1000),
              end: new Date(todaysRoundsStart)
            };
          }
        },
        checkEditAllowed: function(therapy, view)
        {
          var therapyHasAlreadyStarted = new Date(therapy.start) < CurrentTime.get();
          var isOnlyOnce = therapy.dosingFrequency 
              && therapy.dosingFrequency.type === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.ONCE_THEN_EX;
          if (therapyHasAlreadyStarted && isOnlyOnce)
          {
            view.getAppFactory().createWarningSystemDialog(view.getDictionary('cannot.edit.past.once.only.therapy'), 520, 120).show();
            return false;
          }
          return true;
        },
        getTherapyNextAdministration: function(view, therapy, newPrescription, callback)
        {
          var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_CALCULATE_NEXT_THERAPY_ADMINISTRATION_TIME;

          var params = {
            therapy: JSON.stringify(therapy),
            newPrescription: newPrescription
          };

          view.sendPostRequest(url, params, function(nextTime)
          {
            callback(nextTime ? new Date(JSON.parse(nextTime)) : null);
          });
        },
        /**
         * Returns the interval length in hours for the specified titration type.
         * @param {String} type app.views.medications.TherapyEnums.therapyTitrationTypeEnum
         * @returns {Integer} Interval length in hours.
         */
        getTitrationIntervalHoursByType: function(type)
        {
          var intervalEnums = tm.views.medications.MedicationTimingUtils.titrationIntervalHours;

          if (intervalEnums.hasOwnProperty(type))
          {
            return intervalEnums[type];
          }
          return 24;
        },

        /**
         * @param {Date} date
         * @returns {number}
         */
        getUtcTime: function(date)
        {
          return Date.UTC(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes());
        },

        /**
         * @param {Date} date
         * @param {app.views.common.AppView} view
         * @returns {string}
         */
        getFriendlyDateDisplayableValue: function(date, view)
        {
          var today = new Date();
          var yesterday = new Date();
          var tomorrow = new Date();

          yesterday.setDate(yesterday.getDate() - 1);
          tomorrow.setDate(tomorrow.getDate() + 1);
          yesterday.setHours(0, 0, 0, 0);
          today.setHours(0, 0, 0, 0);
          tomorrow.setHours(0, 0, 0, 0);

          var friendlyDate = new Date(date);
          friendlyDate.setHours(0, 0, 0, 0);

          if (today.getTime() === friendlyDate.getTime())
          {
            return view.getDictionary("today");
          }
          else if (yesterday.getTime() === friendlyDate.getTime())
          {
            return view.getDictionary("yesterday");

          }
          else if (tomorrow.getTime() === friendlyDate.getTime())
          {
            return view.getDictionary("tomorrow");
          }
          else
          {
            if (view.getViewLanguage() === "en")
            {
              return Globalize.formatDate(friendlyDate, {skeleton: "MMMd"});
            }
            else
            {
              return Globalize.formatDate(friendlyDate, {skeleton: "Md"});
            }
          }
        },

        /**
         * Extracts the administration time from AdministrationDto (should move to jsClass implementation when ready).
         * @param {Object} administration
         * @returns {Date}
         */
        getAdministrationTimestamp: function(administration)
        {
          return new Date(administration.administrationTime ? administration.administrationTime : administration.plannedTime);
        },

        /**
         * @param {Array<{date: Date, doseTime: { hour: Date, minute: Date}}>} timedDoseElements
         * @returns {{start: *, end: *}}
         */
        getVariableDaysTherapyInterval: function(timedDoseElements)
        {
          var start = null;
          var end = null;
          for (var i = 0; i < timedDoseElements.length; i++)
          {
            var timedDoseElement = timedDoseElements[i];
            var date = new Date(timedDoseElement.date);
            var applicationTimestamp =
                new Date(
                    date.getFullYear(),
                    date.getMonth(),
                    date.getDate(),
                    timedDoseElement.doseTime.hour,
                    timedDoseElement.doseTime.minute);
            if (end == null || end < applicationTimestamp)
            {
              end = applicationTimestamp;
            }
            if (applicationTimestamp > CurrentTime.get() && (start == null || start > applicationTimestamp))
            {
              start = applicationTimestamp;
            }
          }
          return {
            start: start,
            end: end
          };
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {Object} administration
         * @returns {string}
         */
        getFormattedAdministrationPlannedTime: function(view, administration)
        {
          return '<br/><span class="PlannedAdministrationTime TextLabel MedicationLabel">' +
              view.getDictionary("planned.time") + '&nbsp;</span>' + '<span class="TextData">' +
              view.getDisplayableValue(new Date(administration.plannedTime), "short.date.time") + '</span>';
        }
      }
    }
);
