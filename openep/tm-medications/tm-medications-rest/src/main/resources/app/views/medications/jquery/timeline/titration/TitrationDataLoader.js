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
Class.define('app.views.medications.timeline.titration.TitrationDataLoader', 'tm.jquery.Object', {

  titrationType: null,
  therapyId: null,
  view: null,

  _cache: null,
  _currentData: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._cache = {};
  },

  /**
   * @param {app.views.medications.timeline.titration.TitrationDataLoader.Interval} interval
   * @returns {tm.jquery.Promise}
   * @private
   */
  _fetchData: function(interval)
  {
    var deferred = tm.jquery.Deferred.create();
    var view = this.getView();
    var self = this;

    var cachedData = this._getCachedData(interval);
    if (cachedData)
    {
      self._currentData = cachedData;
      deferred.resolve(cachedData);
    }
    else
    {
      var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_DATA_FOR_TITRATION;
      var params = {
        patientId: view.getPatientId(),
        therapyId: this.getTherapyId(),
        titrationType: this.getTitrationType(),
        searchStart: JSON.stringify(interval.getStart()),
        searchEnd: JSON.stringify(interval.getEnd())
      };

      view.showLoaderMask();
      view.loadViewData(url, params, null, function successHandler(result)
      {
        view.hideLoaderMask();

        if (params.patientId === view.getPatientId() && result)
        {
          var titrationData = app.views.medications.timeline.titration.dto.Titration.fromJson(result);
          titrationData.setStartInterval(interval.getStart());
          titrationData.setEndInterval(interval.getEnd());
          self._addDataToCache(interval, titrationData);
          self._currentData = titrationData;
          deferred.resolve(titrationData);
          return;
        }
        deferred.reject();
      });
    }

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.timeline.titration.TitrationDataLoader.Interval} interval
   * @returns {app.views.medications.timeline.titration.dto.Titration|null}
   * @private
   */
  _getCachedData: function(interval)
  {
    return this._cache.hasOwnProperty(interval.getCacheKey()) ? this._cache[interval.getCacheKey()] : null;
  },

  _addDataToCache: function(interval, titrationData)
  {
    if (!this._cache.hasOwnProperty(interval.getCacheKey()))
    {
      this._cache[interval.getCacheKey()] = titrationData;
    }
  },

  /**
   * @param {Date} endTime
   * @returns {app.views.medications.timeline.titration.TitrationDataLoader.Interval}
   * @private
   */
  _calculateIntervalFromEndTime: function(endTime)
  {
    var startTime = endTime ? new Date(endTime.getTime()) : CurrentTime.get();
    startTime.setHours(
        startTime.getHours() - tm.views.medications.MedicationTimingUtils.getTitrationIntervalHoursByType(this.getTitrationType()));

    return new app.views.medications.timeline.titration.TitrationDataLoader.Interval({
      start: startTime,
      end: endTime
    });
  },

  /**
   * @param {Date} startTime
   * @returns {app.views.medications.timeline.titration.TitrationDataLoader.Interval}
   * @private
   */
  _calculateIntervalFromStartTime: function(startTime)
  {
    var endTime = startTime ? new Date(startTime.getTime()) : CurrentTime.get();
    endTime.setHours(
        endTime.getHours() + tm.views.medications.MedicationTimingUtils.getTitrationIntervalHoursByType(this.getTitrationType()));

    return new app.views.medications.timeline.titration.TitrationDataLoader.Interval({
      start: startTime,
      end: endTime
    });
  },

  /**
   * @param {Date} initEndTime
   * @returns {tm.jquery.Promise}
   */
  init: function(initEndTime)
  {
    this._cache = {};
    this._currentData = null;

    return this._fetchData(this._calculateIntervalFromEndTime(initEndTime));
  },

  /**
   * @returns {tm.jquery.Promise}
   */
  getPrev: function()
  {
    return this._fetchData(this._calculateIntervalFromEndTime(this.getCurrentData().getStartInterval()));
  },

  /**
   * @returns {tm.jquery.Promise}
   */
  getNext: function()
  {
    return this._fetchData(this._calculateIntervalFromStartTime(this.getCurrentData().getEndInterval()))
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {String}
   */
  getTherapyId: function()
  {
    return this.therapyId;
  },

  /**
   * @returns {app.views.medications.TherapyEnums.therapyTitrationTypeEnum}
   */
  getTitrationType: function()
  {
    return this.titrationType;
  },

  /**
   * @returns {app.views.medications.timeline.titration.dto.Titration}
   */
  getCurrentData: function()
  {
    return this._currentData;
  }
});
Class.define('app.views.medications.timeline.titration.TitrationDataLoader.Interval', 'tm.jquery.Object', {

  start: null,
  end: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {Date}
   */
  getStart: function()
  {
    return this.start;
  },

  /**
   * @returns {Date}
   */
  getEnd: function()
  {
    return this.end;
  },

  /**
   * @returns {String}
   */
  getCacheKey: function()
  {
    return this.start.toISOString() + this.end.toISOString();
  }
});