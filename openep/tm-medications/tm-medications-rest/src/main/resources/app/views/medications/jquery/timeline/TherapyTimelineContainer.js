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
  isDischarged: null,
  therapyTimelineRows: null,
  additionalWarnings: null,
  additionalWarningsTypes: null,
  therapySortTypeEnum: null,
  axis: null,
  therapyTimelinesWithGroup: null,
  routesFilter: null,
  customGroupsFilter: null,
  filteredTherapyTimelineRows: null,
  timelineStart: null,
  timelineEnd: null,
  hidePastTherapies: null,
  hideFutureTherapies: null,
  readOnly: null,
  previousSelectedButtonId: null,
  selectedShownTherapies: null,
  /** privates: components */
  axisContainer: null,
  timelinesContainer: null,
  pastTherapiesFilterContainer: null,
  pastTherapiesFilterButtonGroup: null,
  activeTherapiesButton: null,
  threeDaysTherapiesButton: null,
  allTherapiesButton: null,
  atDischargeButton: null,
  duringHospButton: null,
  therapyDataSorter: null,

  _scrollbarDetectionTimer: null,
  _prevScrollbarStatus: false,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.therapyDataSorter = this.getConfigValue("therapyDataSorter", new app.views.medications.TherapyDataSorter({
      view: this.view
    }));
    this.isDischarged = false;
    this.therapyTimelinesWithGroup = [];
    this.filteredTherapyTimelineRows = [];
    this.therapyTimelineRows = [];
    this.additionalWarningsTypes = [];
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    //this._buildPastTherapiesFilter();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function ()
  {
    var self = this;
    this.axisContainer = new tm.jquery.Container({
      cls: "axis-container",
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    this.axisContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function (component)
    {
      self._paintAxis(component);
    });
    this.timelinesContainer = new tm.jquery.Container({
      cls: "timelines-container",
      scrollable: "vertical",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"))
    });
    this.pastTherapiesFilterContainer = new tm.jquery.Container({
      cls: "past-therapies-filter-container",
      width: 407,
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      alignSelf: "center"
    });

    this.timelinesContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function (component)
    {
      self._onCheckScrollbarTimerTick(component);
    });
    this.timelinesContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_DESTROY, function(component){
      clearTimeout(self._scrollbarDetectionTimer);
    });
  },

  _onCheckScrollbarTimerTick: function (component)
  {
    clearTimeout(this._scrollbarDetectionTimer);

    var self = this;
    var scrollbars = tm.views.medications.MedicationUtils.isScrollVisible(component);

    if (self._prevScrollbarStatus != scrollbars)
    {
      self._fixAxisPaddingIfScrollbar();
      self._prevScrollbarStatus = scrollbars;
    }

    this._scrollbarDetectionTimer = setTimeout(function ()
    {
      self._onCheckScrollbarTimerTick(component);
    }, 250);
  },

  _buildGui: function ()
  {
    var topContainer = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'start')
    });
    this.add(topContainer);
    topContainer.add(this.pastTherapiesFilterContainer);
    topContainer.add(this.axisContainer);

    if(tm.jquery.ClientUserAgent.isTablet())
    {
      this.tabletTimelinesContainer = this._createTabletScrollbarContainer();
      this.add(this.tabletTimelinesContainer);
    }
    else
    {
      this.add(this.timelinesContainer);
    }
  },
  _createTabletScrollbarContainer: function ()
  {
    var timelinesContainer = this.timelinesContainer;

    var container = new tm.jquery.Container({
      cls: "tablet-slider-scrollbar",
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'stretch'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var leftSliderScrollbarOuterContainer = this._createSliderScrollbarOuterContainer("left");
    var rightSliderScrollbarOuterContainer = this._createSliderScrollbarOuterContainer("right");
    leftSliderScrollbarOuterContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_SCROLL, function (component, componentEvent, elementEvent)
    {
      var scrollTop = $(component.getDom()).scrollTop();
      $(timelinesContainer.getDom()).scrollTop(scrollTop);
      $(rightSliderScrollbarOuterContainer.getDom()).scrollTop(scrollTop);
    });
    rightSliderScrollbarOuterContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_SCROLL, function (component, componentEvent, elementEvent)
    {
      var scrollTop = $(component.getDom()).scrollTop();
      $(timelinesContainer.getDom()).scrollTop(scrollTop);
      $(leftSliderScrollbarOuterContainer.getDom()).scrollTop(scrollTop);
    });

    container.add(leftSliderScrollbarOuterContainer);
    container.add(timelinesContainer);
    container.add(rightSliderScrollbarOuterContainer);

    container.setData({
      left: {
        sliderScrollbarOuterContainer: leftSliderScrollbarOuterContainer,
        sliderScrollbarContainer: leftSliderScrollbarOuterContainer.getData().sliderScrollbarContainer
      },
      right: {
        sliderScrollbarOuterContainer: rightSliderScrollbarOuterContainer,
        sliderScrollbarContainer: rightSliderScrollbarOuterContainer.getData().sliderScrollbarContainer
      }
    });

    return container;
  },
  _createSliderScrollbarOuterContainer: function(direction)
  {
    var sliderScrollbarOuterContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch'), scrollable: "vertical",
      style: "position:absolute;z-index:1000;" + (direction == "left" ? "left:0;" : "right:0;")
    });

    var sliderScrollbarContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    sliderScrollbarOuterContainer.add(sliderScrollbarContainer);
    sliderScrollbarOuterContainer.setData({sliderScrollbarContainer: sliderScrollbarContainer});

    return sliderScrollbarOuterContainer;
  },
  _repaintTabletScrollbar: function()
  {
    var timelinesContainerDom = this.timelinesContainer.getDom();
    var $timelinesContainerDom = $(timelinesContainerDom);
    var tabletTimelinesContainer = this.tabletTimelinesContainer;
    var tabletTimelinesContainerData = tabletTimelinesContainer.getData();

    if(tm.jquery.Utils.isEmpty(tabletTimelinesContainerData.left) == false) update("left", tabletTimelinesContainerData.left);
    if(tm.jquery.Utils.isEmpty(tabletTimelinesContainerData.right) == false) update("right", tabletTimelinesContainerData.right);

    function update(direction, data)
    {
      var sliderScrollbarOuterContainer = data.sliderScrollbarOuterContainer;
      var sliderScrollbarContainer = data.sliderScrollbarContainer;

      var $sliderScrollbarOuterContainerDom = $(sliderScrollbarOuterContainer.getDom());
      var $sliderScrollbarContainerDom = $(sliderScrollbarContainer.getDom());

      if ($timelinesContainerDom.hasVerticalScrollbar())
      {
        //$sliderScrollbarOuterContainerDom.width($sliderScrollbarOuterContainerDom.getScrollbarWidth());
        $sliderScrollbarOuterContainerDom.width(direction == "left" ? 15 : 30);
        $sliderScrollbarOuterContainerDom.height($timelinesContainerDom.height());
        $sliderScrollbarContainerDom.height(timelinesContainerDom.scrollHeight);
        $sliderScrollbarOuterContainerDom.show();
      }
      else
      {
        $sliderScrollbarOuterContainerDom.hide();
      }
    }
  },
  _setTimelineInterval: function()
  {
    var newSelected = this.selectedShownTherapies;
    if(tm.jquery.Utils.isEmpty(newSelected))
    {
      newSelected = this.view.actionsHeader.getDefaultTherapiesShownSelection();
    }
    var now = CurrentTime.get();
    if (this.previousSelectedButtonId != newSelected)
    {
      if (newSelected == "activeTherapies")
      {
        this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7, 0, 0); //-7 days
        this.timelineEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 8, 0, 0); //+7 days
        this.hidePastTherapies = true;
        this.hideFutureTherapies = false;
        this.readOnly = false;
        if (!tm.jquery.Utils.isEmpty(this.axis))
        {
          this._resetTimelineOptions();
        }
      }
      else if (newSelected == "threeDaysTherapies")
      {
        this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 3, 0, 0);  //-3 days
        this.timelineEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 4, 0, 0);  //+3 days
        this.hidePastTherapies = false;
        this.hideFutureTherapies = false;
        this.readOnly = false;
        this._resetTimelineOptions();
      }
      else if (newSelected == "allTherapies")
      {
        if (!tm.jquery.Utils.isEmpty(this.view.getCentralCaseData()))  // MedicationsCentralCaseDto
        {
          if (!tm.jquery.Utils.isEmpty(this.view.getCentralCaseData().centralCaseEffective))
          {
            if (this.view.getCentralCaseData().outpatient)
            {
              this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7, 0, 0);  //-7 days
            }
            else
            {
              var timelineStart = new Date(this.view.getCentralCaseData().centralCaseEffective.startMillis);
              timelineStart.setDate(timelineStart.getDate() - 1);
              this.timelineStart = new Date(timelineStart);  //start of hospitalization
            }
          }
          else
          {
            this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7, 0, 0); //-7 days
          }
        }
        else
        {
          this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7, 0, 0);  //-7 days
        }
        this.timelineEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 8, 0, 0);  //+7 days
        this.hidePastTherapies = false;
        this.hideFutureTherapies = false;
        this.readOnly = false;
        this._resetTimelineOptions();
      }
      else if (newSelected == "customDateTherapies")
      {
        this.hidePastTherapies = false;
        this.hideFutureTherapies = true;
        this.readOnly = true;
      }
      else if (newSelected == "discharge")
      {
        this.timelineStart = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis - 1000 * 60 * 60 * 24); //-24 hours  * 3
        this.timelineEnd = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis + 1000 * 60 * 60 * 24);  //+24 hours  * 3
        this.hidePastTherapies = true;
        this.hideFutureTherapies = false;
        this.readOnly = false;
        if (!tm.jquery.Utils.isEmpty(this.axis))
        {
          this._resetTimelineOptions();
        }
      }
      else if (newSelected == "hospital")
      {
        var start = new Date(this.view.getCentralCaseData().centralCaseEffective.startMillis);
        var end = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis);
        this.timelineStart = new Date(start.getFullYear(), start.getMonth(), start.getDate() - 1, 0, 0);  // start of hospitalization  - 3, 0, 0
        this.timelineEnd = new Date(end.getFullYear(), end.getMonth(), end.getDate() + 2, 0, 0);  // end of the day of discharge  + 3, 0, 0
        this.hidePastTherapies = false;
        this.hideFutureTherapies = false;
        this.readOnly = false;
        this._resetTimelineOptions();
      }
      this.previousSelectedButtonId = newSelected;
    }
  },
  _getWindowInterval: function()
  {
    var focusTime;

    if (this.isDischarged)
    {
      focusTime = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis);
    }
    else
    {
      focusTime = CurrentTime.get();
    }
    var windowStart = new Date(focusTime.getTime() - 12 * 60 * 60 * 1000);      //12 hours
    var windowEnd = new Date(focusTime.getTime() + 24 * 60 * 60 * 1000);       //24 hours

    return {
      start: windowStart,
      end: windowEnd
    };
  },

  _addTherapyTimeline: function (timelineIndex, groupName, data)
  {
    var windowInterval = this._getWindowInterval();

    var self = this;
    var therapyTimeline = new tm.views.medications.timeline.TherapyTimeline({
      timelineIndex: timelineIndex,
      view: this.view,
      scrollableElement: this.getScrollableElement(),
      patientId: this.patientId,
      intervalStart: this.timelineStart,
      intervalEnd: this.timelineEnd,
      start: windowInterval.start,
      end: windowInterval.end,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      width: "100%",
      timelineRangeChangedFunction: function (sender, start, end)
      {
        self._timelineRangeChanged(sender, start, end);
        if(tm.jquery.ClientUserAgent.isTablet())
        {
          setTimeout(function ()
          {
            self._repaintTabletScrollbar();
          }, 0);
        }
      },
      reloadTimelinesFunction: function (preventLoaderMask, isDataValid)
      {
        self.reloadTimelines(false, self.therapySortTypeEnum, preventLoaderMask, isDataValid);
      }
    });
    this.therapyTimelinesWithGroup.push({
      therapyTimeline: therapyTimeline,
      group: groupName
    });

    var panel = new tm.jquery.Panel({
      collapsed: false,
      showHeader: groupName != null,
      showFooter: false,
      width: '100%',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    if (groupName != null)
    {
      var panelHeader = panel.getHeader();
      panelHeader.setCls('grouping-panel text-unselectable');
      panelHeader.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch"));
      var panelTitlePane = new tm.jquery.Container({
        cls:'panel-title-pane',
        layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center'),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        html: '<span class="TextData">' + groupName + '</span>'
      });
      panelHeader.add(panelTitlePane);
      panel.bindToggleEvent([panelTitlePane]);
    }

    panel.on(tm.jquery.ComponentEvent.EVENT_TYPE_BEFORE_EXPAND, function ()
    {
      setTimeout(function ()
      {
        therapyTimeline.redrawTimeline();
      }, 0);
    });

    var panelContent = panel.getContent();
    panelContent.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    panelContent.setScrollable('visible');
    panelContent.add(therapyTimeline);
    this.timelinesContainer.add(panel);

    therapyTimeline.setTherapyTimelineData(data, this.readOnly);

    return therapyTimeline;
  },

  _paintAxis: function (paintToComponent)
  {
    var self = this;
    var now = CurrentTime.get();
    var windowInterval = this._getWindowInterval();
    var axisModel = new vis.DataSet();

    var axisOptions = {
      width: "100%",
      height: "52px",
      locale: this.view.getViewLanguage(),
      locales: {},
      showMajorLabels: true,
      showMinorLabels: true,
      zoomable: true,
      moveable: true,
      selectable: false, /* prevent selectable styles being added to content since we don't need them */
      stack: false,
      type: 'point',
      min: !tm.jquery.Utils.isEmpty(this.timelineStart) ? this.timelineStart : new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7, 0, 0),
      max: !tm.jquery.Utils.isEmpty(this.timelineEnd) ? this.timelineEnd : new Date(now.getFullYear(), now.getMonth(), now.getDate() + 8, 0, 0),
      start: windowInterval.start,
      end: windowInterval.end,
      orientation: {axis: 'top'} /* this way the time lines will connect with the lower timeline's lines */
    };
    axisOptions.locales[axisOptions.locale] = {
      current: this.view.getDictionary("visjs.timeline.current"),
      time: this.view.getDictionary("visjs.timeline.time")
    };

    if (this.axis) {
      this.axis.destroy();
      this.axis = null;
    }

    this.axis = new vis.Timeline(paintToComponent.getDom(), axisModel, axisOptions);

    tm.views.medications.TherapyTimelineUtils.overrideTimelineOnMouseWheel(this.axis);

    this.axis.on('rangechange', function (event)
    {
      if (event.byUser == true) self._timelineRangeChanged(this, event.start, event.end);
    });
    if (this.view.getOptimizeForPerformance())
    {
      tm.views.medications.TherapyTimelineUtils.overrideTimelinePanMove(this.axis);
    }
    this.axis.setCurrentTime(CurrentTime.get());
  },

  _resetTimelineOptions: function(preventTimelinesMinMaxReset)
  {
    if (preventTimelinesMinMaxReset !== true)
    {
      this._resetTimelinesMinMax()
    }

    var windowInterval = this._getWindowInterval();
    this._timelineRangeChanged(null, windowInterval.start, windowInterval.end);
  },

  _resetTimelinesMinMax: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.axis))
    {
      var options = {
        min: this.timelineStart,
        max: this.timelineEnd
      };
      this.axis.setOptions(options);
    }
    for (var i = 0; i < this.therapyTimelinesWithGroup.length; i++)
    {
      this.therapyTimelinesWithGroup[i].therapyTimeline.setMinMaxTimeline(this.timelineStart, this.timelineEnd);
    }
  },

  _timelineRangeChanged: function (sender, start, end)
  {
    if (sender != this && !tm.jquery.Utils.isEmpty(this.axis)){
      this.axis.setWindow(start, end, {animation: false});
    }

    for (var i = 0; i < this.therapyTimelinesWithGroup.length; i++)
    {
      if (sender != this.therapyTimelinesWithGroup[i])
      {
        this.therapyTimelinesWithGroup[i].therapyTimeline.setVisibleRange(start, end);
      }
    }
  },

  _fixAxisPaddingIfScrollbar: function ()
  {
    if (!this.axisContainer || !this.axis) return;

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
    setTimeout(function ()
    {
      if (self.axis) self.axis.redraw();
    }, 20);
  },

  _presentTimelineData: function (rebuildTimelines)
  {
    var self = this;
    this._filterTherapyTimelineRows();

    rebuildTimelines = this.therapyTimelinesWithGroup.length == 0 ? true : rebuildTimelines;

    var groupedData = this.getTherapyDataSorter().group(this.groupField, this.filteredTherapyTimelineRows);

    if (rebuildTimelines)
    {
      this.therapyTimelinesWithGroup.removeAll();
      this.timelinesContainer.removeAll();
      this.timelinesContainer.add(self.view.getNoTherapiesField(this.filteredTherapyTimelineRows.length > 0));
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
            var group = this.getTherapyDataSorter().getGroupByKey(groupedData, key);
            this.therapyTimelinesWithGroup[i].therapyTimeline.setTherapyTimelineData(group.elements, this.readOnly);
          }
        }
      }
      else
      {
        this.therapyTimelinesWithGroup[0].therapyTimeline.setTherapyTimelineData(
            this.filteredTherapyTimelineRows, this.readOnly);
      }

      if (this.filteredTherapyTimelineRows.length > 0)
      {
        this.view.noTherapiesField.hide();
      }
      else
      {
        this.view.noTherapiesField.show();
      }
    }
  },

  _buildTimelinesFromGroupedData: function (groups)
  {
    var length = groups.length;
    if (length > 0)
    {
      for (var i = 0; i < groups.length; i++)
      {
        this._addTherapyTimeline(i, groups[i].key, groups[i].elements);
      }
    }
  },

  _filterTherapyTimelineRows: function ()
  {
    this.filteredTherapyTimelineRows.removeAll();
    var routesFilter = this.routesFilter;
    var routesFilterIsSet =  tm.jquery.Utils.isArray(routesFilter) && routesFilter.length > 0;
    var customGroupsFilterIsSet = this.customGroupsFilter != null && this.customGroupsFilter.length > 0;
    if (routesFilterIsSet || customGroupsFilterIsSet)
    {
      for (var i = 0; i < this.therapyTimelineRows.length; i++)
      {
        var therapyRoutes = this.therapyTimelineRows[i].therapy.getRoutes().map(function(route)
        {
          return route.name;
        });

        var routeInFilter = !routesFilterIsSet || therapyRoutes.some(function(r)
        {
          return routesFilter.indexOf(r) >= 0;
        });


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

  _showAdditionalWarningsDialog: function (additionalWarnings)
  {
    var view = this.view;
    var appFactory = view.getAppFactory();

    if (additionalWarnings.hasTaskIds() && additionalWarnings.hasAdditionalWarnings())
    {
      var dataEntryContainer = new tm.views.medications.timeline.TherapyTimelineAdditionalWarningsDialogContainer({
        view: view,
        additionalWarnings: additionalWarnings
      });

      var dataEntryDialog = appFactory.createDataEntryDialog(
          view.getDictionary("warning"),
          null,
          dataEntryContainer,
          function(resultData)
          {
            if (resultData && resultData.isSuccess())
            {
              view.refreshTherapies(true);
            }
          }, 600, 520
      );
      dataEntryDialog.setContainmentElement(view.getDom());
      dataEntryDialog.setResizable(false);
      dataEntryDialog.setHideOnDocumentClick(false);
      dataEntryDialog.setClosable(false);
      dataEntryDialog.setHideOnEscape(false);
      dataEntryDialog.setRightButtons([dataEntryDialog.getConfirmButton()]);

      dataEntryDialog.show();
    }
  },

  /**
   * Reloads the timeline data.
   * @param {boolean} rebuildTimelines Remove all timelines and add new ones.
   * @param {app.views.medications.TherapyEnums.therapySortTypeEnum} therapySortTypeEnum Therapy sort type.
   * @param {boolean} preventLoaderMask Set to true to prevent the loader mask from being used.
   * @param {function} isDataValid Optional function that needs to return true, otherwise the data will be discarded.
   * @returns {tm.jquery.Promise}
   */
  reloadTimelines: function (rebuildTimelines, therapySortTypeEnum, preventLoaderMask, isDataValid)
  {
    // return true if no extra validation is requested
    isDataValid = isDataValid ? isDataValid : function() { return true; };

    // trigger test coordinator removal
    this.therapyTimelinesWithGroup.forEach(
        function clearCoordinator(group)
        {
          group.therapyTimeline.clearTestCoordinator();
        });

    var self = this;
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_TIMELINE_LOAD_HUB;
    viewHubNotifier.actionStarted(hubAction);
    var deferred = tm.jquery.Deferred.create();
    this.therapySortTypeEnum = therapySortTypeEnum ? therapySortTypeEnum : app.views.medications.TherapyEnums.therapySortTypeEnum.DESCRIPTION_ASC;
    this._setTimelineInterval();
    var params = {
      patientId: this.patientId,
      patientData: JSON.stringify(this.view.getPatientData()),
      timelineInterval: JSON.stringify({
        startMillis: this.timelineStart.getTime(),
        endMillis: this.timelineEnd.getTime()
      }),
      roundsInterval: JSON.stringify(this.view.getRoundsInterval()),
      therapySortTypeEnum: this.therapySortTypeEnum,
      hidePastTherapies: this.hidePastTherapies,
      hideFutureTherapies: this.hideFutureTherapies
    };

    var patientId = this.patientId;

    if (!preventLoaderMask)
    {
      this.view.showLoaderMask();
    }

    var timelineDataUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPY_TIMELINE;
    this.view.loadViewData(timelineDataUrl, params, null,
        function(therapyTimelineData) // [TherapyTimelineDto.java]
        {
          if (patientId == self.patientId && isDataValid(therapyTimelineData))
          {
            therapyTimelineData.therapyRows.forEach(function(timelineRow)
            {
              timelineRow.therapy = app.views.medications.common.TherapyJsonConverter.convert(timelineRow.therapy);
              if (tm.jquery.Utils.isArray(timelineRow.administrations))
              {
                timelineRow.administrations.sort(function(a, b)
                {
                  var administrationTimestampA = tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(a);
                  var administrationTimestampB = tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(b);

                  if (administrationTimestampA < administrationTimestampB) return -1;
                  if (administrationTimestampA > administrationTimestampB) return 1;
                  return 0;
                });
              }
            });
            self.therapyTimelineRows = therapyTimelineData.therapyRows;
            self.additionalWarnings = 
                app.views.medications.common.dto.AdditionalWarnings.fromJson(therapyTimelineData.additionalWarnings);
            self.additionalWarningsTypes = therapyTimelineData.additionalWarningsTypes;
            self.therapyTimelineRowsLoadedFunction(therapyTimelineData.therapyRows);
            self._presentTimelineData(rebuildTimelines);
          }
          if (self.view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed()
              && self.getAdditionalWarnings()
              && self.getAdditionalWarnings().hasAdditionalWarnings())
          {
            self._showAdditionalWarningsDialog(self.getAdditionalWarnings());
          }
          if (!preventLoaderMask)
          {
            self.view.hideLoaderMask();
          }
          self.view.refreshPatientDataContainer();
          self.view.updateCurrentBnfMaximumSum();
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self.view.hideLoaderMask();
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        });
    return deferred.promise();
  },

  getTherapyDataSorter: function ()
  {
    return this.therapyDataSorter;
  },
  getAdditionalWarnings: function()
  {
    return this.additionalWarnings;
  },

  setPatientId: function (patientId, isDischarged, therapySortTypeEnum)
  {
    this.patientId = patientId;
    this.clear();
    this.isDischarged = isDischarged;
    this.reloadTimelines(true, therapySortTypeEnum);
    this._resetTimelineOptions();
  },

  clear: function ()
  {
    this.isDischarged = false; // set to false or repainting the axis might fail due to missing data for _getWindowInterval
    this.timelinesContainer.removeAll();
    this.therapyTimelineRows.removeAll();
    this.therapyTimelinesWithGroup.removeAll();
    this.filteredTherapyTimelineRows.removeAll();
    this.previousSelectedButtonId = null;
    this.selectedShownTherapies = null;
  },

  /**
   * @returns {Element|null}
   */
  getScrollableElement: function()
  {
    return this.timelinesContainer.getDom();
  },


  setGrouping: function (groupField)
  {
    this.groupField = groupField;
    this._presentTimelineData(true);
  },

  setShownTherapies: function(shownTherapies)
  {
    this.selectedShownTherapies = shownTherapies;
  },

  setTimelineFilter: function (routes, customGroups, refreshTimelines)
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
  },

  setTimelineDate: function(newDate)
  {
    var self = this;
    this.timelineStart = new Date(newDate.getFullYear(), newDate.getMonth(), newDate.getDate(), 0, 0);
    this.timelineEnd = new Date(newDate.getFullYear(), newDate.getMonth(), newDate.getDate() + 1, 0, 0);
    this._resetTimelinesMinMax();
    this.reloadTimelines(false, this.therapySortTypeEnum, false).done(
        function successResultCallbackHandler()
        {
          self._resetTimelineOptions(true)
        }
    );
  },

  /**
   * @Override
   */
  destroy: function ()
  {
    if (this.axis) {
      this.axis.destroy();
      this.axis = null;
    }
    this.callSuper();
  }
});

