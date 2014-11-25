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

Class.define('tm.views.medications.timeline.TherapyTimelineContainer', 'tm.jquery.Container', {
  cls: "therapy-timeline-container",

  /** config */
  view: null,
  patientId: null,
  groupField: null,
  therapyTimelineRowsLoadedFunction: null,
  /** privates */
  therapyTimelineRows: null,
  therapySortTypeEnum: null,
  axis: null,
  therapyTimelinesWithGroup: null,
  routesFilter: null,
  customGroupsFilter: null,
  filteredTherapyTimelineRows: null,
  timelineStart: null,
  timelineEnd: null,
  /** privates: components */
  axisContainer: null,
  timelinesContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    var now = new Date();
    this.timelineStart = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000); //-7 days
    this.timelineEnd = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000); //+7 days
    this.therapyTimelinesWithGroup = [];
    this.filteredTherapyTimelineRows = [];
    this.therapyTimelineRows = [];
    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.axisContainer = new tm.jquery.Container({
      cls: "axis-container",
      scrollable: 'visible',
      flex: 1
    });
    this.axisContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      self._paintAxis(component);
    });
    this.timelinesContainer = new tm.jquery.Container({
      cls: "timelines-container",
      scrollable: "vertical",
      flex: 1,
      layout: this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"))
    });
  },

  _buildGui: function()
  {
    var topContainer = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create('start', 'start')
    });
    this.add(topContainer);
    topContainer.add(new tm.jquery.Container({width: 401}));
    topContainer.add(this.axisContainer);
    this.add(this.timelinesContainer);
  },

  _addTherapyTimeline: function(timelineIndex, groupName, data)
  {
    var self = this;
    var therapyTimeline = new tm.views.medications.timeline.TherapyTimeline({
      timelineIndex: timelineIndex,
      view: this.view,
      timelineContainer: this,
      patientId: this.patientId,
      intervalStart: this.timelineStart,
      intervalEnd: this.timelineEnd,
      flex: 1,
      width: "100%",
      timelineReadyFunction: function()
      {
        self._fixAxisPaddingIfScrollbar();
      },
      timelineRangeChangedFunction: function(start, end)
      {
        self._timelineRangeChanged(start, end);
      },
      reloadTimelinesFunction: function()
      {
        self.reloadTimelines(false, self.therapySortTypeEnum);
      }
    });
    this.therapyTimelinesWithGroup.push({
      therapyTimeline: therapyTimeline,
      group: groupName
    });

    var panel = new tm.jquery.Panel({collapsed: false, showHeader: groupName != null, showFooter: false, width: '100%'});
    if (groupName != null)
    {
      var panelHeader = panel.getHeader();
      panelHeader.setCls('grouping-panel text-unselectable');
      panelHeader.setLayout(tm.jquery.HFlexboxLayout.create("start", "stretch"));
      var panelTitlePane = new tm.jquery.Container({
        padding: "3 0 0 0",
        layout: tm.jquery.HFlexboxLayout.create('start', 'center'),
        flex: 1,
        html: '<span class="TextData">' + groupName + '</span>'
      });
      panelHeader.add(panelTitlePane);
      panel.bindToggleEvent([panelTitlePane]);
    }

    panel.on(tm.jquery.ComponentEvent.EVENT_TYPE_BEFORE_EXPAND, function()
    {
      setTimeout(function()
      {
        therapyTimeline.redrawTimeline();
      }, 0);
    });

    var panelContent = panel.getContent();
    panelContent.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"));
    panelContent.setScrollable('visible');
    panelContent.add(therapyTimeline);
    this.timelinesContainer.add(panel);

    therapyTimeline.setTherapyTimelineData(data);

    return therapyTimeline;
  },

  _paintAxis: function(paintToComponent)
  {
    var self = this;
    this.axis = new links.Timeline(paintToComponent.getDom());
    var axisModel = new google.visualization.DataTable();
    axisModel.addColumn('datetime', 'start');
    axisModel.addColumn('datetime', 'end');
    axisModel.addColumn('string', 'content');

    var axisOptions = {
      locale: this.view.getViewLanguage(),
      groupsOnRight: false,
      groupsWidth: "402px",
      width: "100%",
      height: "52px",
      zoomable: true
    };

    tm.views.medications.TherapyTimelineUtils.overrideTimelineOnMouseWheel(this.axis);
    if (this.view.getOptimizeForPerformance())
    {
      tm.views.medications.TherapyTimelineUtils.overrideTimelineOnMouseMove(this.axis);
    }

    google.visualization.events.addListener(this.axis, 'rangechange', function()
    {
      var range = self.axis.getVisibleChartRange();
      self._timelineRangeChanged(range.start, range.end);
    });
    this.axis.draw(axisModel, axisOptions);
  },

  _timelineRangeChanged: function(start, end)
  {
    this.axis.setVisibleChartRange(start, end);
    for (var i = 0; i < this.therapyTimelinesWithGroup.length; i++)
    {
      this.therapyTimelinesWithGroup[i].therapyTimeline.setVisibleRange(start, end)
    }
  },

  _fixAxisPaddingIfScrollbar: function()
  {
    var self = this;
    if (tm.views.medications.MedicationUtils.isScrollVisible(this.timelinesContainer))
    {
      var scrollbarWidth = tm.views.medications.MedicationUtils.getScrollbarWidth();
      if (scrollbarWidth)
      {
        this.axisContainer.setPadding("0 " + scrollbarWidth + " 0 0");
      }
    }
    else
    {
      this.axisContainer.setPadding(0);
    }
    setTimeout(function()
    {
      self.axis.redraw();
    }, 20);
  },

  _presentTimelineData: function(rebuildTimelines)
  {
    this._filterTherapyTimelineRows();
    var groupedData = null;
    if (this.groupField == 'atcGroup')
    {
      groupedData = this._groupByAtc(this.filteredTherapyTimelineRows);
    }
    else if (this.groupField == 'route')
    {
      groupedData = this._groupByRoute(this.filteredTherapyTimelineRows);
    }
    else if (this.groupField == 'customGroup')
    {
      groupedData = this._groupByCustomGroup(this.filteredTherapyTimelineRows);
    }

    if (groupedData != null)
    {
      this._sortGroupsByKey(groupedData);
    }

    if (rebuildTimelines)
    {
      this.therapyTimelinesWithGroup.removeAll();
      this.timelinesContainer.removeAll();
      if (groupedData != null)
      {
        this._buildTimelinesFromGroupedData(groupedData);
      }
      else
      {
        this._addTherapyTimeline(0, null, this.filteredTherapyTimelineRows);
      }
      this.timelinesContainer.repaint();
    }
    else
    {
      if (groupedData != null)
      {
        if (groupedData.length != this.therapyTimelinesWithGroup.length)
        {
          this._presentTimelineData(true);
        }
        else
        {
          for (var i = 0; i < this.therapyTimelinesWithGroup.length; i++)
          {
            var key = this.therapyTimelinesWithGroup[i].group;
            var group = this._getGroupByKey(groupedData, key);
            this.therapyTimelinesWithGroup[i].therapyTimeline.setTherapyTimelineData(group.elements);
          }
        }
      }
      else
      {
        this.therapyTimelinesWithGroup[0].therapyTimeline.setTherapyTimelineData(this.filteredTherapyTimelineRows);
      }
    }
  },

  _sortGroupsByKey: function(groups)
  {
    groups.sort(
        function(group1, group2)
        {
          var key1 = group1.key.toLowerCase();
          var key2 = group2.key.toLowerCase();
          if (key1 < key2)
          {
            return -1;
          }
          if (key1 > key2)
          {
            return 1;
          }
          return 0;
        });
  },

  _groupByRoute: function(therapyTimelineRows)
  {
    var routeGroups = [];
    for (var i = 0; i < therapyTimelineRows.length; i++)
    {
      var route = therapyTimelineRows[i].therapy.route.name;
      var group = this._getGroupByKey(routeGroups, route);
      if (group == null)
      {
        group = {key: route, elements: []};
        routeGroups.push(group);
      }
      group.elements.push(therapyTimelineRows[i]);
    }
    return routeGroups;
  },

  _groupByAtc: function(therapyTimelineRows)
  {
    var atcGroups = [];
    for (var i = 0; i < therapyTimelineRows.length; i++)
    {
      var therapyTimelineRow = therapyTimelineRows[i];
      var atc = therapyTimelineRow.atcGroupName ?
          therapyTimelineRow.atcGroupName + ' (' + therapyTimelineRow.atcGroupCode + ')' :
          this.view.getDictionary("without.atc");
      var group = this._getGroupByKey(atcGroups, atc);
      if (group == null)
      {
        group = {key: atc, elements: []};
        atcGroups.push(group);
      }
      group.elements.push(therapyTimelineRows[i]);
    }

    return atcGroups;
  },

  _groupByCustomGroup: function(therapyTimelineRows)
  {
    var customGroups = [];
    for (var i = 0; i < therapyTimelineRows.length; i++)
    {
      var therapyTimelineRow = therapyTimelineRows[i];
      var customGroup = tm.views.medications.MedicationUtils.getTherapyCustomGroupDisplayName(therapyTimelineRow, this.view);
      var group = this._getGroupByKey(customGroups, customGroup);
      if (group == null)
      {
        group = {key: customGroup, elements: []};
        customGroups.push(group);
      }
      group.elements.push(therapyTimelineRows[i]);
    }
    return customGroups;
  },

  _getGroupByKey: function(groups, key)
  {
    for (var i = 0; i < groups.length; i++)
    {
      if (groups[i].key == key)
      {
        return groups[i]
      }
    }
    return null;
  },

  _buildTimelinesFromGroupedData: function(groups)
  {
    for (var i = 0; i < groups.length; i++)
    {
      this._addTherapyTimeline(i, groups[i].key, groups[i].elements);
    }
  },

  _filterTherapyTimelineRows: function()
  {
    this.filteredTherapyTimelineRows.removeAll();
    var routesFilterIsSet = this.routesFilter != null && this.routesFilter.length > 0;
    var customGroupsFilterIsSet = this.customGroupsFilter != null && this.customGroupsFilter.length > 0;
    if (routesFilterIsSet || customGroupsFilterIsSet)
    {
      for (var i = 0; i < this.therapyTimelineRows.length; i++)
      {
        var routeInFilter = !routesFilterIsSet || this.routesFilter.contains(this.therapyTimelineRows[i].therapy.route.name);
        var customGroupInFilter = !customGroupsFilterIsSet || this.customGroupsFilter.contains(this.therapyTimelineRows[i].customGroup);
        if (routeInFilter && customGroupInFilter)
        {
          this.filteredTherapyTimelineRows.push(this.therapyTimelineRows[i])
        }
      }
    }
    else
    {
      this.filteredTherapyTimelineRows = this.filteredTherapyTimelineRows.concat(this.therapyTimelineRows);
    }
  },

  /** public methods */
  reloadTimelines: function(rebuildTimelines, therapySortTypeEnum)
  {
    var self = this;
    this.therapySortTypeEnum = therapySortTypeEnum;
    var params = {
      patientId: this.patientId,
      centralCaseId: this.view.getCentralCaseData() ? this.view.getCentralCaseData().centralCaseId : null,
      knownOrganizationalEntityName: this.view.getKnownOrganizationalEntity(),
      timelineInterval: JSON.stringify({
        startMillis: this.timelineStart.getTime(),
        endMillis: this.timelineEnd.getTime()
      }),
      roundsInterval: JSON.stringify(this.view.getRoundsInterval()),
      therapySortTypeEnum: this.therapySortTypeEnum
    };

    this.view.showLoaderMask();
    var timelineDataUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPY_TIMELINE_DATA;
    this.view.loadViewData(timelineDataUrl, params, null,
        function(therapyTimelineData) // [TherapyTimelineRowDto.java]
        {
          self.therapyTimelineRows = therapyTimelineData;
          self.therapyTimelineRowsLoadedFunction(therapyTimelineData);
          self._presentTimelineData(rebuildTimelines);
          self.view.hideLoaderMask();
        },
        function()
        {
          self.view.hideLoaderMask();
        });
  },

  setPatientId: function(patientId, therapySortTypeEnum)
  {
    this.patientId = patientId;
    this.clear();
    this.reloadTimelines(true, therapySortTypeEnum);
  },

  clear: function()
  {
    this.timelinesContainer.removeAll();
    this.therapyTimelineRows.removeAll();
    this.therapyTimelinesWithGroup.removeAll();
    this.filteredTherapyTimelineRows.removeAll();
  },

  setGrouping: function(groupField)
  {
    this.groupField = groupField;
    this._presentTimelineData(true);
  },

  setTimelineFilter: function(routes, customGroups, refreshTimelines)
  {
    if (this.routesFilter != routes || this.customGroupsFilter != customGroups)
    {
      this.routesFilter = routes;
      this.customGroupsFilter = customGroups;
      if (refreshTimelines)
      {
        this._presentTimelineData(true);
      }
    }
  }
});

