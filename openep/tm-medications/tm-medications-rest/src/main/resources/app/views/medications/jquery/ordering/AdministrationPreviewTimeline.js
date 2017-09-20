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

Class.define('app.views.medications.ordering.AdministrationPreviewTimeline', 'tm.jquery.Container', {
  cls: "admission-preview-timeline",

  data: null,
  therapy: null,

  options: null,
  view: null,

  intervalStart: null,
  intervalEnd: null,

  /** privates: components */
  timeline: null,
  timelineContainer: null,
  headerComponents: null,

  _visData: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var tomorrow = CurrentTime.get();
    tomorrow.setDate(tomorrow.getDate() + 1);

    this.data = this.getConfigValue('data', []);
    this.intervalStart = this.getConfigValue('intervalStart', CurrentTime.get());
    this.intervalEnd = this.getConfigValue('intervalEnd', tomorrow);
    this._visData = new vis.DataSet();

    this.options = {
      "width": "100%",
      locale: this.getView().getViewLanguage(),
      locales: {},
      showMajorLabels: true,
      showMinorLabels: true,
      moveable: false,
      zoomable: false,
      selectable: false, /* prevent selectable styles being added to content since we don't need them */
      stack: false,
      timeAxis: { scale: 'hour', step: 2 },
      orientation: {axis: 'top'},
      type: 'point'
    };
    // create locale (text strings should be replaced with localized strings) for timeline
    this.options.locales[this.options.locale] = {
      current: this.getView().getDictionary("visjs.timeline.current"),
      time: this.getView().getDictionary("visjs.timeline.time")
    };

    this._buildGui();
  },

  /** private methods */
  _buildGui: function ()
  {
    var self = this;

    this.timelineContainer = new tm.jquery.Container({
      //cls: "therapy-timeline",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "visible"
    });
    this.add(this.timelineContainer);

    this.timelineContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function (component)
    {
      self._paintTimeline(component);
      self._buildTimeline();
    });
  },

  _buildTimelineElements: function()
  {
    var timelineElements = [];
    var idx = 0;
    var workdayInterval = tm.views.medications.MedicationTimingUtils.getWorkdayInterval(this.view.getRoundsInterval(), CurrentTime.get());

    //paint workday interval
    if (workdayInterval != null)
    {
      timelineElements.push(
          this._buildTimelineElement(0, workdayInterval.start, workdayInterval.end, "", "work-hours", "background"));
    }

    for (idx = 0; idx < this.getData().length; idx++)
    {
      var administration = this.getData()[idx];  // [AdministrationDto.java]
      var administrationTimestamp = new Date(administration.administrationTime ? administration.administrationTime : administration.plannedTime);

      var orderState = this._getOrderStateString(administration);

      var taskStateDisplay = '<div class="task ' + this._getAdministrationClass(administration)
          + ' id="preview_task_id' + idx + '">' + '<div class="orderState">' + orderState + '</div><div class="timeLabel TextData">'
          + this.getView().getDisplayableValue(administrationTimestamp, "short.time") + '</div></div>';

      timelineElements.push(
          this._buildTimelineElement(timelineElements.length, administrationTimestamp, null, taskStateDisplay, null));
    }

    //draw duration lines for infusions
    if (this.getTherapy() && this.getData().length > 0 &&
        (this.getTherapy().isTherapyWithDurationAdministrations()))
    {
      this._addInfusionDurationElements(timelineElements, idx);
      this._removeBrokenDurationElements(timelineElements);
    }

    if (this.getData().length == 0)
    {
      // add an empty task to hold row height when no other tasks exist
      timelineElements.push(
          this._buildTimelineElement(timelineElements.length,
              this.intervalStart,
              this.intervalEnd,
              '<div class="task" id="preview_task_id_0"><div></div><div class="timeLabel TextData">empty</div></div>',
              'empty-task'));
    }

    // redraw is smoother if we update what's left and remove the obsolete
    var obsoleteIds = this._visData.getIds({ filter: function(item){
      return item.id >= timelineElements.length;
    }});

    this._visData.update(timelineElements);
    this._visData.remove(obsoleteIds);
  },

  _buildTimeline: function ()
  {
    if (this.timeline)
    {
      var options = jQuery.extend(true, {}, this.options);
      options.min = this.intervalStart;
      options.max = this.intervalEnd;
      options.start = options.min;
      options.end =  options.max;
      this.timeline.setOptions(options);
      this._buildTimelineElements();
    }
    else
    {
      this._buildGui();
    }
  },

  /**
   * Checks all range elements which are generated based on start and stop tasks and removes those
   * who's start property is not set as they will produce exceptions inside the Vis.js library.
   * The cause for such generated items is when the duration of the infusion is such that
   * the individual infusion durations overlap - currently we don't know how to match the appropriate
   * start and end tasks from the data we receive.
   * @param {Array<Object>} timelineElements
   * @private
   */
  _removeBrokenDurationElements: function(timelineElements)
  {
    if (!timelineElements) return;

    var idx = timelineElements.length;
    while (idx--)
    {
      if (timelineElements[idx].type === 'range' && !timelineElements[idx].start)
      {
        timelineElements.remove(timelineElements[idx]);
      }
    }
  },

  _buildTimelineElement: function (id, from, to, content, className, type)
  {
    return {
      id: id,
      start: from ? from : to,
      end: to,
      content: content,
      group: 1,
      className: className,
      type: tm.jquery.Utils.isEmpty(type) ? 'point' : type
    };

  },

  _getAdministrationClass: function (administration)
  {
    var administrationClass = tm.jquery.Utils.isEmpty(administration) ? '' : administration.administrationStatus;

    if (!tm.jquery.Utils.isEmpty(administration) && !tm.jquery.Utils.isEmpty(administration.infusionSetChangeEnum))
    {
      administrationClass += " " + administration.infusionSetChangeEnum;
    }

    return administrationClass;
  },

  _getOrderStateString: function (administration)
  {
    var enums = app.views.medications.TherapyEnums;
    if (administration && administration.administrationType == enums.administrationTypeEnum.INFUSION_SET_CHANGE)
    {
      if (administration.infusionSetChangeEnum == enums.infusionSetChangeEnum.INFUSION_SYSTEM_CHANGE)
      {
        return "";
      }
      if (administration.infusionSetChangeEnum == enums.infusionSetChangeEnum.INFUSION_SYRINGE_CHANGE)
      {
        return "";
      }
      return "";
    }
    if (administration && administration.administrationType == enums.administrationTypeEnum.START)
    {
      return administration.differentFromOrder ? '&#916;' : '';         //delta
    }
    if (administration && administration.administrationType == enums.administrationTypeEnum.STOP)
    {
      return 'X';
    }
    if (administration && administration.administrationType == enums.administrationTypeEnum.ADJUST_INFUSION)
    {
      if (administration.differentFromOrder)
      {
        return 'E\'';
      }
      return 'E';
    }
    return '';
  },

  _addInfusionDurationElements: function(timelineElements)
  {
    if (tm.jquery.Utils.isEmpty(this.getTherapy())) return;

    var enums = app.views.medications.TherapyEnums;
    var administrationIntervals = [];
    var hasStart = false;
    var therapyWithoutStartOrEndAdministration = true;

    for (var j = 0; j < this.getData().length; j++)
    {
      var administration = this.getData()[j];  // [AdministrationDto.java]

      if (administration.administrationType != enums.administrationTypeEnum.INFUSION_SET_CHANGE)
      {
        if (administration.administrationType == enums.administrationTypeEnum.START)
        {
          therapyWithoutStartOrEndAdministration = false;
          hasStart = true;
          var intervalStart = {};
          intervalStart.administrationStart = administration.administrationTime
              ? new Date(administration.administrationTime)
              : new Date(administration.plannedTime);
          intervalStart.administrationEnd = this.options.max; //if end exists it will be overwritten later
          administrationIntervals.push(intervalStart);
        }
        if (administration.administrationType == enums.administrationTypeEnum.STOP)
        {
          therapyWithoutStartOrEndAdministration = false;
          var administrationEnd = administration.administrationTime ?
              new Date(administration.administrationTime) :
              new Date(administration.plannedTime);
          var intervalEnd = hasStart ? administrationIntervals[(administrationIntervals.length - 1)] : {};
          intervalEnd.administrationEnd = administrationEnd;

          if (hasStart === false)
          {
            intervalEnd.administrationStart = this.options.min;
            administrationIntervals.push(intervalEnd);
          }
          hasStart = false;
        }
      }
    }

    var therapyStart = this.getTherapy().getStart();

    if (therapyWithoutStartOrEndAdministration)
    {
      var therapyEnd = this.getTherapy().getEnd();
      var administrationIntervalInfinite = {
        administrationStart: therapyStart > this.options.min && timelineElements.length == 0 ?
            therapyStart :
            this.options.min,
        administrationEnd: (therapyEnd && therapyEnd < this.options.max && timelineElements.length == 0) ?
            therapyEnd :
            this.options.max
      };
      if (administrationIntervalInfinite.administrationStart)
      {
        administrationIntervals.push(administrationIntervalInfinite);
      }
    }
    for (var k = 0; k < administrationIntervals.length; k++)
    {
      timelineElements.push(
          this._buildTimelineElement(timelineElements.length,
              administrationIntervals[k].administrationStart,
              tm.jquery.Utils.isEmpty(administrationIntervals[k].administrationEnd) ?
                  this.intervalEnd : administrationIntervals[k].administrationEnd,
              '',
              "duration-line",
              "range"));
    }
  },

  _redrawTimeline: function ()
  {
    this.timeline.redraw();
  },

  _paintTimeline: function (paintToComponent)
  {
    var options = jQuery.extend(true, {}, this.options);
    options.min = this.intervalStart;
    options.max = this.intervalEnd;
    options.start = options.min;
    options.end =  options.max;

    if (this.timeline)
    {
      this.timeline.destroy();
      this.timeline = null;
    }
    this.timeline = new vis.Timeline(paintToComponent.getDom(),
        this._visData, this.getDefaultGroups(), options);
  },

  getView: function()
  {
    return this.view;
  },

  getData: function()
  {
    return tm.jquery.Utils.isEmpty(this.data) ? [] : this.data;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  getDefaultGroups: function()
  {
    return [{
      id: 1,
      content: ''
    }];
  },

  /** public methods */
  setData: function (therapy, data)
  {
    this.data = tm.jquery.Utils.isEmpty(data) ? [] : data;
    this.therapy = therapy;
    this._buildTimeline();
  },

  setStartTime: function(date)
  {
    this.intervalStart = new Date(date);
    this.intervalEnd = new Date(date);

    this.intervalStart.setHours(date.getHours() - 4);
    this.intervalEnd.setDate(date.getDate() + 1);
  },

  clear: function ()
  {
    this.data.removeAll();
    this.therapy = null;
    this._buildTimeline();
  },

  redrawTimeline: function ()
  {
    this._redrawTimeline();
  },

  /**
   * @Override
   */
  destroy: function ()
  {
    if (this.timeline) {
      this.timeline.destroy();
      this.timeline = null;
    }
    this.callSuper();
  },

  _refreshAdministrationPreviewImpl: function (start, therapy)
  {
    var self = this;
    if (tm.jquery.Utils.isEmpty(start))
    {
      self.clear();
      return;
    }

    var url = self.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPY_ADMINISTRATION_TIMES;
    var params = {therapy: JSON.stringify(therapy)};

    this.view.sendPostRequest(url, params, function (data)
    {
      var times = tm.jquery.Utils.isEmpty(data) ? [] : JSON.parse(data);
      self.setStartTime(start);
      self.setData(therapy, times);
    });
  }
});
