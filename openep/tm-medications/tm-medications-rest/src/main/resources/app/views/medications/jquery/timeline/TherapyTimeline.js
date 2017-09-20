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

Class.define('tm.views.medications.timeline.TherapyTimeline', 'tm.jquery.Container', {
  cls: "therapy-timeline",
  scrollable: "visible",

  /** config */
  view: null,
  timelineIndex: null,
  patientId: null,
  timelineRangeChangedFunction: null,
  reloadTimelinesFunction: null,
  intervalStart: null,
  intervalEnd: null,
  start: null,
  end: null,
  scrollableElement: null,
  /** privates */
  options: null,
  displayProvider: null,
  therapyActions: null,
  therapyTimelineRows: null, //[TherapyTimelineRowDto]
  /** privates: components */
  timeline: null,
  timelineContainer: null,

  _itemSet: null,
  _groupSet: null,
  _administrationDialogBuilder: null,

  _ghostClick: null,
  _ghostClickTimer: null,

  _lastActionGuid: null,
  _preventDoubleClickOnTask: null,
  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.therapyTimelineRows = [];
    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"));
    this.therapyActions = new app.views.medications.TherapyActions({view: config.view});
    this.displayProvider = new app.views.medications.TherapyDisplayProvider({view: this.getView()});
    this.groupHeaderDisplayProvider =  new app.views.medications.TherapyDisplayProvider({
      view: this.getView(),
      showChangeHistory: false,
      showChangeReason: false
    });
    this._administrationDialogBuilder = new app.views.medications.timeline.administration.TherapyAdministrationDialogBuilder({
      view: this.getView()
    });

    this.options = {
      width: "100%",
      locale: this.getView().getViewLanguage(),
      locales: {},
      orientation: {axis: 'none', item: 'top'},
      margin: {
        axis: 0,
        item: 0
      }, /* default margin is 20px, which affects the position of items in the first group since we hide it */
      showMajorLabels: true,
      showMinorLabels: false,
      moveable: true,
      max: this.intervalEnd,
      min: this.intervalStart,
      zoomable: true,
      zoomMax: 500000000,
      zoomMin: 1000000,
      stack: false,
      start: this.start, //new Date(now.getTime() - 12 * 60 * 60 * 1000),      //24 hours
      end: this.end, //new Date(now.getTime() + 24 * 60 * 60 * 1000),       //12 hours
      type: 'point',
      selectable: false, /* prevent selectable styles being added to content since we don't need them */
      align: 'center',
      groupOrder: "orderIndex"
      //timeAxis: { scale: 'hour', step: 1 }
    };
    // create locale (text strings should be replaced with localized strings) for timeline
    this.options.locales[this.options.locale] = {
      current: this.getView().getDictionary("visjs.timeline.current"),
      time: this.getView().getDictionary("visjs.timeline.time")
    };
    var self = this;
    this.getView().on(tm.views.medications.TherapyView.EVENT_TYPE_MEDICATION_BARCODE_SCANNED,
        function(component, componentEvent)
        {
          var eventData = componentEvent.getEventData();
          self._medicationIdentifierScanned(eventData.barcodeTaskSearch, eventData.barcode);

        });
    this._buildGui();
  },

  /** private methods */
  _buildGui: function(elements, groups)
  {
    var self = this;
    this.timelineContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "visible"
    });
    this.add(this.timelineContainer);
    this.timelineContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      self._paintTimeline(component, elements, groups);
    });

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'therapy-timeline-coordinator',
      view: this.getView(),
      component: this.timelineContainer
    });
  },

  _paintTimeline: function(paintToComponent, elements, groups)
  {
    var self = this;
    var view = self.getView();

    this._itemSet = new vis.DataSet();
    this._itemSet.add(elements);
    this._groupSet = new vis.DataSet();
    this._groupSet.add(groups);

    if (this.timeline) {
      this.timeline.destroy();
      this.timeline = null;
    }

    this.timeline = new vis.Timeline(paintToComponent.getDom(), this._itemSet, this._groupSet, this.options);
    this.timeline.setCurrentTime(CurrentTime.get());

    var timelineWindow = this.timeline.getWindow();
    this.onTimelineRangeChange(timelineWindow.start, timelineWindow.end);

    tm.views.medications.TherapyTimelineUtils.overrideTimelineOnMouseWheel(this.timeline);

    this.timeline.on('click', function(event)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(view);
      if (event.item)
      {
        if (self._preventDoubleClickOnTask !== true)
        {
          self._preventDoubleClickOnTask = true;
          // has to be triggered in a timeout because otherwise the Vis.js library has some kind of
          // press event detection, which triggers on Windows 7 + jqBrowser for each click
          setTimeout(function()
          {
            self._handleLeftClickOnElement(self.getItemSet().get(event.item));
          }, 0);
        }
      }
      else if(event.event.target && tm.jquery.ClientUserAgent.isTablet())
      {
        /* delegating click event - tablet fix */
        var evt = event.event;
        $(evt.target).trigger({
          type: "click", target: evt.target, originalEvent: evt.srcEvent, pageX: evt.center.x, pageY: evt.center.y
        });
      }
    });
    this.timeline.on('contextmenu', function(event)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(view);
      if (event.item)
      {
        self._handleRightClickOnElement(self.getItemSet().get(event.item), event);
      }
    });
    this.timeline.on('press', function(event)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(view);
      tm.jquery.ComponentUtils.hideAllTooltips(view);

      var props = self.timeline.getEventProperties(event);

      if (props.item)
      {
        self._ghostClick = true;
        self._handleRightClickOnElement(self.getItemSet().get(props.item), event);

        clearTimeout(self._ghostClickTimer);
        self._ghostClickTimer = setTimeout(function()
        {
          self._ghostClick = false;
        }, 500);
      }
    });
    this.timeline.on('rangechange', function(event)
    {
      if (event.byUser)
      {
        self.onTimelineRangeChange(event.start, event.end);
      }
    });
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, event)
    {
      if (self._ghostClick)
      {
        event.stopPropagation();
      }
    });
    if (view.getOptimizeForPerformance())
    {
      tm.views.medications.TherapyTimelineUtils.overrideTimelinePanMove(this.timeline);
    }
  },

  onTimelineRangeChange: function(start, end)
  {
    this._addOrRemoveClusterFlags(start, end);
    this.timelineRangeChangedFunction(this, start, end);
  },

  _addOrRemoveClusterFlags: function(start, end)
  {
    var timelineRangeInHours = (end - start) / (60 * 60 * 1000);
    var timelineCls = this.getCls();
    timelineCls = timelineCls.replace(" hide-info-elements", "");

    if (timelineRangeInHours > 8)
    {
      this.setCls(timelineCls);
    }
    if (timelineRangeInHours <= 8)
    {
      this.setCls(timelineCls + " hide-info-elements");
    }
  },

  _buildTimeline: function(readOnly)
  {
    var timelineElements = [];
    var workdayInterval = tm.views.medications.MedicationTimingUtils.getWorkdayInterval(
        this.getView().getRoundsInterval(),
        CurrentTime.get()
    );

    var groups = this._buildGroups(this.therapyTimelineRows, readOnly);
    for (var i = 0; i < this.therapyTimelineRows.length; i++)  // [TherapyRowDto.java]
    {
      var therapyTimeline = this.therapyTimelineRows[i];
      var therapy = therapyTimeline.therapy;    // [TherapyDto.java]
      var groupId = therapy.getTherapyId();

      //paint workday interval
      if (workdayInterval)
      {
        timelineElements.push(
            this._buildTimelineElement(
                workdayInterval.start,
                workdayInterval.end,
                null,
                groupId,
                null,
                null,
                "work-hours",
                "background",
                false));
      }

      // add an empty task to hold row height when no other tasks exist
      timelineElements.push(
          this._buildTimelineElement(
              this.options.min,
              this.options.max,
              null,
              groupId,
              null,
              null,
              "empty-task",
              null,
              false));

      //add task elements to timeline
      this._addAdministrationTasksToTimeline(timelineElements, therapyTimeline);

      //draw duration lines for infusions
      if (therapy.isTherapyWithDurationAdministrations())
      {
        this._addInfusionDurationElements(timelineElements, therapyTimeline, groupId);
      }

      //draw warning lines for "when needed" therapies
      if (therapy.getWhenNeeded())
      {
        this._addWhenNeededElements(timelineElements, therapyTimeline, groupId);
      }
    }

    if (this.timeline)
    {
      var updatedIds = this.getGroupSet().update(groups); // update so it doesn't collapse the height!
      this.getItemSet().clear();
      this.getItemSet().add(timelineElements);

      var obsoleteIdChecker = {};
      updatedIds.forEach(function(id)
      {
        obsoleteIdChecker[id] = true;
      }, this);

      this.getGroupSet().remove(this.getGroupSet().getIds({
        filter: function(groupItem)
        {
          return obsoleteIdChecker[groupItem.id] !== true;
        }
      }));
      if (!tm.jquery.Utils.isEmpty(this.timeline.range))
      {
        this._addOrRemoveClusterFlags(new Date(this.timeline.range.start), new Date(this.timeline.range.end));
      }

      this._insertTestCoordinator(); // signal the end of data drawing
    }
    else
    {
      this.removeAll(true);
      this._buildGui(timelineElements, groups);
      this.repaint();
    }
  },

  /**
   * Inserts the test coordinator into the timeline, for test coordination, but does so with a yield due to the fact
   * that the Vis.JS library uses a timer to redraw the timeline once it detects the item change.
   * @private
   */
  _insertTestCoordinator: function()
  {
    var self = this;
    setTimeout(function yieldToRedrawTimer(){
      self._testRenderCoordinator.insertCoordinator();
    }, 0);
  },

  /**
   * @param {Array<Object>} timelineElements
   * @param {Object} therapyTimeline
   * @private
   */
  _addAdministrationTasksToTimeline: function(timelineElements, therapyTimeline)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var therapy = therapyTimeline.therapy;
    var groupId = therapy.getTherapyId();
    var putThisAdministrationInCluster = false;

    var administrationsInCluster = [];
    var firstAdministrationInCluster = null;
    var currentRate = null;
    for (var j = 0; j < therapyTimeline.administrations.length; j++)
    {
      var administration = therapyTimeline.administrations[j];  // [AdministrationDto.java]
      var administrationTimestamp = tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(administration);
      var isDeferredTask = this._isAdministrationTaskDeferred(therapy, administration);

      if (administrationTimestamp.getTime() >= this.options.min.getTime())
      {
        var nextAdministration = j < therapyTimeline.administrations.length - 1 ? therapyTimeline.administrations[j + 1] : null;
        var nextAdministrationTimestamp =
            nextAdministration ?
                tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(nextAdministration)
                : null;

        var nextAdministrationIsTooClose = false;
        if (nextAdministrationTimestamp)
        {
          if (nextAdministrationTimestamp.getTime() !== administrationTimestamp.getTime() &&
              ((nextAdministrationTimestamp - administrationTimestamp) / 60 / 1000) <= 30)
          {
            nextAdministrationIsTooClose = true;
          }
        }

        var isRateDecreased = null;

        if ((therapy.isContinuousInfusion() || therapy.isOrderTypeOxygen()) &&
            administration.administeredDose &&
            (administration.administrationType == enums.administrationTypeEnum.ADJUST_INFUSION ||
            administration.administrationType == enums.administrationTypeEnum.START))
        {
          var administrationRate = administration.administeredDose ? administration.administeredDose.numerator : null;
          if (this._isAdministrationAdministered(administration) &&
              administration.administrationType != enums.administrationTypeEnum.START)
          {
            if (!tm.jquery.Utils.isEmpty(currentRate) && !tm.jquery.Utils.isEmpty(administrationRate) &&
                (currentRate != administrationRate))
            {
              isRateDecreased = currentRate < administrationRate;
            }
            else if (tm.jquery.Utils.isEmpty(currentRate) && !tm.jquery.Utils.isEmpty(therapyTimeline.infusionRateAtIntervalStart) &&
                therapyTimeline.infusionRateAtIntervalStart != administrationRate)
            {
              isRateDecreased = therapyTimeline.infusionRateAtIntervalStart < administrationRate;
            }
          }
          currentRate = administrationRate;
        }

        var orderState = this._getOrderStateString(administration, therapy, isRateDecreased, isDeferredTask);
        var taskId = this._getTaskId(administration);
        var additionalWarnings = therapyTimeline.additionalWarnings;
        var administrationClass = this._getAdministrationClass(administration, therapy, additionalWarnings, isRateDecreased, isDeferredTask);

        var taskStateContainer = new tm.jquery.Container({
          cls: "task-state-container",
          html: '<div class="task ' + administrationClass + '" id="' + taskId + '">'
          + '<div class="orderState">' + orderState + '</div></div>',
          testAttribute: therapy.getTherapyId()
        });
        if (this.getView().isTestMode())
        {
          taskStateContainer.addAttribute('data-planned-time', administration.plannedTime);
          taskStateContainer.addAttribute('data-administration-time', administration.administrationTime);
          taskStateContainer.addAttribute('data-administration-type', administration.administrationType);
          taskStateContainer.addAttribute('data-is-additional', administration.additionalAdministration);
        }
        taskStateContainer.doRender();

        if (!tm.jquery.ClientUserAgent.isTablet())
        {
          var hoverTooltip = this._getTherapyAdministrationTooltip(therapy, administration, isDeferredTask);
          taskStateContainer.setTooltip(hoverTooltip);
        }

        if (putThisAdministrationInCluster === false && nextAdministrationIsTooClose === true)
        {
          firstAdministrationInCluster =
              this._buildTimelineAdministrationTaskElement(
                  administrationTimestamp,
                  taskStateContainer,
                  groupId,
                  therapy,
                  administration
              );
        }
        else if (putThisAdministrationInCluster === true)
        {
          administrationsInCluster.push(
              this._buildTimelineAdministrationTaskElement(
                  administrationTimestamp,
                  taskStateContainer,
                  groupId,
                  therapy,
                  administration
              )
          );
          if (nextAdministrationIsTooClose === false)
          {
            timelineElements.push(
                this._buildTimelineElement(
                    new Date(firstAdministrationInCluster.start),
                    null,
                    tm.jquery.Utils.formatMessage(this.getView().getDictionary('multiple.admins.too.close'),
                        administrationsInCluster.length + 1),
                    groupId,
                    null,
                    null,
                    "timeline-admin-info-label",
                    "box",
                    true
                )
            );
            timelineElements.push(firstAdministrationInCluster);
            administrationsInCluster.forEach(function(administration)
            {
              timelineElements.push(administration);
            });
            firstAdministrationInCluster = null;
            administrationsInCluster = [];
          }
        }
        else
        {
          timelineElements.push(
              this._buildTimelineAdministrationTaskElement(
                  administrationTimestamp,
                  taskStateContainer,
                  groupId,
                  therapy,
                  administration
              )
          );
        }
        putThisAdministrationInCluster = nextAdministrationIsTooClose;
      }
    }
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @returns {boolean}
   * @private
   */
  _isAdministrationTaskDeferred: function(therapy, administration)
  {
    var enums = app.views.medications.TherapyEnums;
    if (administration.administrationResult && administration.administrationResult === enums.administrationResultEnum.DEFER)
    {
      return this._existsAdministrationAfterDefer(therapy, administration);
    }
    return false;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Boolean} isDeferredTask
   * @returns {tm.views.medications.timeline.TherapyTimelineTooltip}
   * @private
   */
  _getTherapyAdministrationTooltip: function(therapy, administration, isDeferredTask)
  {
    var enums = app.views.medications.TherapyEnums;
    var view = this.getView();
    var tooltipString = "";
    var additionalTaskString = "";
    var administrationClassString = "";
    var whenNeededString = "";
    var orderStateString = "";
    var doctorsOrdersString = "";
    var continuousInfusionString = "";

    function getCommaIfNeeded()
    {
      return tooltipString !== '' ? ", " : "";
    }

    if (!administration.taskId && !therapy.isContinuousInfusion())
    {
      additionalTaskString += view.getDictionary("additional.administration");
    }
    if (isDeferredTask)
    {
      administrationClassString += view.getDictionary("administration.defer");
    }
    else if (!tm.jquery.Utils.isEmpty(administration.infusionSetChangeEnum))
    {
      administrationClassString += view.getDictionary("InfusionSetChangeEnum." + administration.infusionSetChangeEnum);
    }
    else if (!tm.jquery.Utils.isEmpty(administration.administrationStatus))
    {
      administrationClassString += view.getDictionary("AdministrationStatusEnum." + administration.administrationStatus);
    }

    if (therapy.getWhenNeeded())
    {
      whenNeededString += view.getDictionary("when.needed");
    }
    if (therapy.isContinuousInfusion())
    {
      continuousInfusionString += view.getDictionary("continuous.infusion");
    }
    if (therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.doctorConfirmation == null &&
        administration.taskId)
    {
      doctorsOrdersString += view.getDictionary("by.doctors.orders.not.reviewed");
    }
    else if (therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.doctorConfirmation === false &&
        administration.taskId)
    {
      doctorsOrdersString += view.getDictionary("by.doctors.orders.do.not.administer");
      administrationClassString = "";
    }
    if (administration && administration.administrationType === enums.administrationTypeEnum.START)
    {
      if (administration.differentFromOrder)
      {
        orderStateString += view.getDictionary("different.from.order");
      }
    }
    if (administration && administration.administrationType === enums.administrationTypeEnum.STOP)
    {
      orderStateString += view.getDictionary("stop.therapy");
    }
    if (administration && administration.administrationType === enums.administrationTypeEnum.ADJUST_INFUSION)
    {
      orderStateString += therapy.isOrderTypeOxygen() ?
          view.getDictionary("adjust.oxygen.rate") :
          view.getDictionary("adjust.infusion.rate");
    }

    tooltipString += additionalTaskString;
    if (doctorsOrdersString !== '')
    {
      tooltipString += getCommaIfNeeded() + doctorsOrdersString;
    }
    if (continuousInfusionString !== '')
    {
      tooltipString += getCommaIfNeeded() + continuousInfusionString;
    }
    if (whenNeededString !== '')
    {
      tooltipString += getCommaIfNeeded() + whenNeededString;
    }
    if (orderStateString !== '')
    {
      tooltipString += getCommaIfNeeded() + orderStateString;
    }
    if (administrationClassString !== '')
    {
      tooltipString += getCommaIfNeeded() + administrationClassString;
    }
    tooltipString = this._capitalizeFirstLetter(tooltipString);

    return new tm.views.medications.timeline.TherapyTimelineTooltip({
      title: tooltipString
    });
  },

  _capitalizeFirstLetter: function(stringToCapitalize)
  {
    if(!tm.jquery.Utils.isEmpty(stringToCapitalize))
    {
      return stringToCapitalize.charAt(0).toUpperCase() + stringToCapitalize.slice(1).toLocaleLowerCase();
    }
  },

  _addInfusionDurationElements: function(timelineElements, therapyTimeline, groupContentHtml)
  {
    var utils = tm.views.medications.MedicationUtils;
    var enums = app.views.medications.TherapyEnums;
    var therapy = therapyTimeline.therapy;  // [TherapyDto.java]
    var therapyStart = therapy.getStart();
    var therapyEnd = therapy.getEnd() ? therapy.getEnd() : null;

    var administrationIntervals = [];
    var hasStart = false;
    var therapyWithoutStartOrEndAdministration = true;
    var previousAdministrationWithZeroRate = false;

    for (var j = 0; j < therapyTimeline.administrations.length; j++)
    {
      var administration = therapyTimeline.administrations[j];  // [AdministrationDto.java]

      if (administration.administrationType !== enums.administrationTypeEnum.INFUSION_SET_CHANGE)
      {
        var administrationWithZeroRate = administration.administeredDose && administration.administeredDose.numerator == 0;

        var adjustInfusionTaskIsStart = !administrationWithZeroRate &&
            (previousAdministrationWithZeroRate || (j == 0 && therapyTimeline.infusionRateAtIntervalStart == 0)) &&
            administration.administrationType === enums.administrationTypeEnum.ADJUST_INFUSION;
        if ((administration.administrationType === enums.administrationTypeEnum.START || adjustInfusionTaskIsStart) &&
            administration.administrationResult !== enums.administrationResultEnum.NOT_GIVEN)
        {
          therapyWithoutStartOrEndAdministration = false;
          hasStart = true;
          var intervalStart = {};
          intervalStart.administrationStart =
              administration.administrationTime ?
                  new Date(administration.administrationTime)
                  : new Date(administration.plannedTime);
          intervalStart.administrationEnd =
              (!tm.jquery.Utils.isEmpty(therapyEnd) && therapyEnd < this.options.max) ? therapyEnd : this.options.max; //if end exists it will be overwritten later
          administrationIntervals.push(intervalStart);
        }

        if ((administration.administrationType === enums.administrationTypeEnum.STOP || administrationWithZeroRate) &&
            administration.administrationResult !== enums.administrationResultEnum.NOT_GIVEN)
        {
          therapyWithoutStartOrEndAdministration = false;
          var administrationEnd = administration.administrationTime ?
              new Date(administration.administrationTime) :
              new Date(administration.plannedTime);

          var intervalEnd = hasStart ? administrationIntervals[(administrationIntervals.length - 1)] : {};
          intervalEnd.administrationEnd = administrationEnd;

          if (hasStart === false && !previousAdministrationWithZeroRate && therapyTimeline.infusionRateAtIntervalStart !== 0)
          {
            intervalEnd.administrationStart =
                !tm.jquery.Utils.isEmpty(therapyStart) && therapyStart > this.options.min ? therapyStart : this.options.min;
            administrationIntervals.push(intervalEnd);
          }
          hasStart = false;
        }
        previousAdministrationWithZeroRate = administrationWithZeroRate;

        if (administration.administeredDose &&
            (administration.administrationType === enums.administrationTypeEnum.START ||
            (administration.administrationType === enums.administrationTypeEnum.ADJUST_INFUSION
            && !administration.startingDevice)))
        {
          var administrationTime = new Date(administration.administrationTime);
          if (this._isAdministrationAdministered(administration) &&
              administrationTime < new Date(CurrentTime.get().getTime() - 2 * 60 * 60 * 1000)) // if at least two hours old
          {
            var displayRate = tm.views.medications.MedicationUtils.buildAdministeredDoseDisplayString(administration, true);

            timelineElements.push(
                this._buildTimelineElement(
                    administrationTime,
                    null,
                    displayRate,
                    groupContentHtml,
                    null,
                    null,
                    "infusion-rate-label",
                    "box",
                    false
                )
            );
          }
        }
      }
    }

    if (therapyWithoutStartOrEndAdministration && therapyTimeline.infusionRateAtIntervalStart
        && therapyTimeline.infusionRateAtIntervalStart !== 0)
    {

      var administrationIntervalInfinite = {
        administrationStart: therapyStart > this.options.min && therapyTimeline.administrations.length === 0 ?
            therapyStart : this.options.min,
        administrationEnd: (therapyEnd && therapyEnd < this.options.max && therapyTimeline.administrations.length === 0) ?
            therapyEnd : this.options.max
      };
      administrationIntervals.push(administrationIntervalInfinite);
    }
    for (var k = 0; k < administrationIntervals.length; k++)
    {
      timelineElements.push(
          this._buildTimelineElement(
              administrationIntervals[k].administrationStart,
              administrationIntervals[k].administrationEnd,
              '<div></div>',
              groupContentHtml,
              null,
              null,
              "duration-line",
              "range",
              false));
    }
    if (!tm.jquery.Utils.isEmpty(therapyTimeline.currentInfusionRate))
    {
      var infusionRateUnit = therapy.isOrderTypeOxygen() ?
          therapy.getFlowRateUnit() :
          therapyTimeline.rateUnit;

      var infusionRateString = utils.getFormattedDecimalNumber(
              utils.doubleToString(therapyTimeline.currentInfusionRate, 'n2')) + " " +
          tm.views.medications.MedicationUtils.getFormattedUnit(infusionRateUnit);

        if (therapyTimeline.currentStartingDevice)
        {
          infusionRateString = [infusionRateString,
            therapyTimeline.currentStartingDevice.getDisplayText(this.getView())].join(', ');
        }

      timelineElements.push(
          this._buildTimelineElement(
              CurrentTime.get(),
              null,
              infusionRateString,
              groupContentHtml,
              null,
              null,
              "infusion-rate-label",
              "box",
              false
          )
      );
    }
  },

  _addWhenNeededElements: function(timelineElements, therapyTimeline, groupContentHtml)
  {
    var therapy = therapyTimeline.therapy;  // [TherapyDto.java]

    var lastAdministrationTime = null;
    for (var j = 0; j < therapyTimeline.administrations.length; j++)
    {
      var administration = therapyTimeline.administrations[j];  // [AdministrationDto.java]

      var administrationStart = administration.administrationTime ? new Date(administration.administrationTime) : new Date(administration.plannedTime);
      if (!lastAdministrationTime || administrationStart.getTime() > lastAdministrationTime.getTime())
      {
        lastAdministrationTime = administrationStart;
      }
    }

    if (lastAdministrationTime)
    {
      var nextAllowedAdministrationTime =
          tm.views.medications.MedicationTimingUtils.getNextAllowedAdministrationTimeForPRN(therapy.getDosingFrequency(), lastAdministrationTime);

      if (!tm.jquery.Utils.isEmpty(nextAllowedAdministrationTime))
      {
        timelineElements.push(
            this._buildTimelineElement(
                lastAdministrationTime,
                nextAllowedAdministrationTime,
                null,
                groupContentHtml,
                null,
                null,
                "when-needed-line",
                "range",
                false
            )
        );
      }
    }
  },

  /**
   * @param {Object} administration
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array} additionalWarnings
   * @param {Boolean} isRateDecreased
   * @param {Boolean} isDeferredTask
   * @returns {String}
   * @private
   */
  _getAdministrationClass: function(administration, therapy, additionalWarnings, isRateDecreased, isDeferredTask)
  {
    var enums = app.views.medications.TherapyEnums;
    var administrationClass = '';
    var administrationStatus = administration.administrationStatus;

    if (!administration.taskId)
    {
      if (administrationStatus === enums.administrationStatusEnum.FAILED)
      {
        administrationClass = administrationStatus;
      }
      else
      {
        administrationClass = "ADDITIONAL";
      }
      if (administration.administrationType == enums.administrationTypeEnum.BOLUS)
      {
        administrationClass += " BOLUS";
      }
      if (!tm.jquery.Utils.isEmpty(isRateDecreased))
      {
        administrationClass += " RATE_CHANGE";
      }
    }
    else
    {
      administrationClass = administrationStatus;
    }
    if (administration.infusionSetChangeEnum)
    {
      administrationClass += " " + administration.infusionSetChangeEnum;
      if (!tm.jquery.Utils.isEmpty(administration.taskId) || !tm.jquery.Utils.isEmpty(administration.infusionBag))
      {
        administrationClass += " INFUSION_BAG_CHANGE";
      }
    }
    if (isDeferredTask)
    {
      administrationClass = "DEFER";
    }
    if (therapy.isTitrationDoseType() && (!administration.plannedDose && !administration.administeredDose) &&
        administration.administrationType != enums.administrationTypeEnum.INFUSION_SET_CHANGE &&
        administration.administrationType != enums.administrationTypeEnum.STOP)
    {
      administrationClass += " TITRATION_DOSE_NOT_SET";
    }
    if (this._hasAdditionalWarnings(additionalWarnings))
    {
      administrationClass += " ADDITIONAL_WARNING";
    }
    else if (therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.doctorConfirmation === null &&
        administration.administrationStatus !== enums.administrationStatusEnum.COMPLETED &&
        administration.taskId)
    {
      administrationClass += " DOCTOR_NOT_REVIEWED";
    }
    else if (therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.doctorConfirmation === false)
    {
      administrationClass += " DOCTOR_DO_NOT_ADMINISTER";
    }
    return administrationClass;
  },

  /**
   * @param {Object} administration
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} isRateDecreased
   * @param {Boolean} isDeferredTask
   * @returns {String}
   * @private
   */
  _getOrderStateString: function(administration, therapy, isRateDecreased, isDeferredTask)
  {
    var enums = app.views.medications.TherapyEnums;

    if (therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.doctorConfirmation === false)
    {
      return '&#88;';
    }
    if (isDeferredTask)
    {
      return '&#68;';
    }
    if (administration.administrationType === enums.administrationTypeEnum.INFUSION_SET_CHANGE)
    {
      if (administration.infusionSetChangeEnum === enums.infusionSetChangeEnum.INFUSION_SYSTEM_CHANGE)
      {
        return "";
      }
      if (administration.infusionSetChangeEnum === enums.infusionSetChangeEnum.INFUSION_SYRINGE_CHANGE)
      {
        return "";
      }
      return "";
    }
    if (isRateDecreased === true)
    {
      return '&#10548;';
    }
    else if (isRateDecreased === false)
    {
      return '&#10549;';
    }
    if (administration && administration.administrationType === enums.administrationTypeEnum.START)
    {
      return administration.differentFromOrder ? '&#916;' : '';         //delta
    }
    if (administration && administration.administrationType === enums.administrationTypeEnum.STOP)
    {
      return 'X';
    }
    if (administration && administration.administrationType === enums.administrationTypeEnum.ADJUST_INFUSION)
    {
      if (administration.differentFromOrder)
      {
        return 'E\'';
      }
      return 'E';
    }
    return '';
  },

  _getTaskId: function(administration)
  {
    var administrationId = "";
    if (administration.administrationId) //syntax error if id contains ::
    {
      var indexOfVersion = administration.administrationId.indexOf("::");
      administrationId = administration.administrationId.substring(0, indexOfVersion);
    }

    return 'task-menu-' + administration.taskId + administrationId;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @returns {boolean}
   * @private
   */
  _existsAdministrationAfterDefer: function(therapy, administration)
  {
    var administrationTimestamp = tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(administration);
    var administrations = this._getTherapyAdministrationsByTherapy(therapy);

    var now = CurrentTime.get();
    var administrationTimestamps = administrations.map(function extractTimestamp(currentAdministration)
    {
      return tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(currentAdministration);
    });

    var exists = true;
    administrationTimestamps.forEach(function(timestamp)
    {
      if (administrationTimestamp.getTime() !== timestamp.getTime() // skip self
          && administrationTimestamp < timestamp && timestamp < now)
      {
        exists = false;
      }
    });
    return exists;
  },

  _createPopupMenu: function(selectedItem)
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var popupMenu = view.getAppFactory().createPopupMenu();
    var therapy = selectedItem.therapy;
    var administration = selectedItem.administration;
    var isAdministrationConfirmed = this._isAdministrationConfirmed(administration);
    var byDoctorsOrders = therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS;
    var doctorConfirmation = administration.doctorConfirmation;

    if (!isAdministrationConfirmed)
    {
      if (view.getTherapyAuthority().isRescheduleAdministrationsAllowed() &&
          administration.administrationType !== enums.administrationTypeEnum.INFUSION_SET_CHANGE)
      {
        var moveMenuItem = new tm.jquery.MenuItem({
          text: view.getDictionary("move"),
          iconCls: 'icon-add-to-24',
          handler: function()
          {
            self._createRescheduleTasksContainer(therapy, administration);
          }
        });
        popupMenu.addMenuItem(moveMenuItem);
      }
      if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
      {
        if (therapy.isTitrationDoseType() && administration.plannedDose)
        {
          popupMenu.addMenuItem(new tm.jquery.MenuItem({
            text: view.getDictionary("edit"),
            iconCls: 'icon-edit',
            handler: function()
            {
              self._showTitrationBasedAdministrationDialog(therapy, administration);
            }
          }));
        }
        else if (byDoctorsOrders && doctorConfirmation !== null)
        {
          var confirmAdministrationItem = new tm.jquery.CheckBoxMenuItem({
            text: view.getDictionary('confirm.administration'),
            checked: doctorConfirmation,
            handler: function()
            {
              self._setDoctorConfirmationResult(administration, true);
            }
          });
          popupMenu.addMenuItem(confirmAdministrationItem);
          var cancelAdministrationItem = new tm.jquery.CheckBoxMenuItem({
            text: view.getDictionary('cancel.administration'),
            checked: !doctorConfirmation,
            handler: function()
            {
              self._setDoctorConfirmationResult(administration, false);
            }
          });
          popupMenu.addMenuItem(cancelAdministrationItem);
        }

        var isDescriptiveDose = therapy.getDoseForm() &&
            therapy.getDoseForm().medicationOrderFormType === enums.medicationOrderFormType.DESCRIPTIVE;
        if (!therapy.isTitrationDoseType() && !isDescriptiveDose)
        {
          var commentMenuItem = new tm.jquery.MenuItem({
            text: view.getDictionary("doctors.comment"),
            iconCls: 'icon-doctors-comment',
            handler: function()
            {
              self._createDoctorsCommentContainer(therapy, administration);
            }
          });
          popupMenu.addMenuItem(commentMenuItem);
        }
      }
    }
    if (view.getTherapyAuthority().isManageAdministrationsAllowed())
    {
      if (isAdministrationConfirmed)
      {
        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: view.getDictionary("edit"),
          iconCls: 'icon-edit',
          handler: function()
          {
            self._createEditAdministrationContainer(therapy, administration);
          }
        }));
      }
      if ((isAdministrationConfirmed || (therapy && !therapy.isContinuousInfusion())) &&
          administration.administrationType !== enums.administrationTypeEnum.STOP)
      {
        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: isAdministrationConfirmed ? view.getDictionary("cancel") : view.getDictionary("delete"),
          iconCls: 'icon-delete',
          handler: function()
          {
            self._openDeleteAdministrationDialog(selectedItem);
          }
        }));
      }
    }


    return popupMenu;
  },

  _isAdministrationConfirmed: function(administration)
  {
    var enums = app.views.medications.TherapyEnums;
    return administration.administrationStatus === enums.administrationStatusEnum.COMPLETED ||
        administration.administrationStatus === enums.administrationStatusEnum.COMPLETED_LATE ||
        administration.administrationStatus === enums.administrationStatusEnum.COMPLETED_EARLY ||
        administration.administrationStatus === enums.administrationStatusEnum.FAILED;
  },

  _buildGroupHeaderTherapyContainer: function(rowDto, readOnly)   // [TherapyRowForContInfusionDto.java / TherapyRowDto.java]
  {
    var self = this;
    var view = this.getView();
    var actionEnums = app.views.medications.TherapyEnums.medicationOrderActionEnum;

    var therapyContainer = new app.views.medications.common.TherapyContainer({
      view: view,
      data: rowDto,
      width: 407,
      scrollableElement: this.getScrollableElement(),
      displayProvider: this.getGroupHeaderDisplayProvider()
    });
    therapyContainer.addTestAttribute(rowDto.therapy.getTherapyId());

    var toolBar = new tm.views.medications.timeline.TimelineTherapyContainerToolbar({
      therapyContainer: therapyContainer,
      readOnly: readOnly
    });
    toolBar.setShowPharmacistsReviewEventCallback(function(therapyContainer)
    {
      view.onShowRelatedPharmacistReviews(therapyContainer.getData(), function()
      {
        self.reloadTimelinesFunction();
      });
    });
    toolBar.setConfirmTherapyEventCallback(function(therapyContainer)
    {
      toolBar.setEnabled(false, true);
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var reissue = data.therapyStatus === app.views.medications.TherapyEnums.therapyStatusEnum.SUSPENDED;
      var actionGuid = tm.jquery.Utils.createGUID();
      self._lastActionGuid = actionGuid;

      var action = reissue ? self.therapyActions.reissueTherapy : self.therapyActions.reviewTherapy;
      action.call(self, therapy.getCompositionUid(), therapy.getEhrOrderName(),
          function()
          {
            self.reloadTimelinesFunction(true, function()
            {
              return self._lastActionGuid === actionGuid;
            });
          },
          function()
          {
            toolBar.setEnabled(true, true);
            self._redrawTimeline();
          });
    });
    toolBar.setNurseResupplyRequestEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      self._sendNurseResupplyRequest(view.getPatientId(), therapy);
    });
    toolBar.setEditTherapyEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      view.showEditTherapyDialog(therapy, false, data.modified);
    });
    toolBar.setAbortTherapyEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      if (self._canTherapyBeAborted(therapy))
      {
        if (therapy.isLinkedToAdmission())
        {
          var abortConfirmationEntryPane = new app.views.medications.common.ChangeReasonDataEntryContainer({
            titleIcon: "warningYellow_status_48.png",
            titleText: view.getDictionary("therapy.needs.reason.for.stopping"),
            view: view,
            changeReasonTypeKey: actionEnums.ABORT
          });
          var abortConfirmationDialog = view.getAppFactory().createDataEntryDialog(
              view.getDictionary("warning"),
              null,
              abortConfirmationEntryPane, function(resultData)
              {
                if (resultData && resultData.isSuccess())
                {
                  self._onAbortTherapy(therapy, resultData);
                }
              }, abortConfirmationEntryPane.defaultWidth, abortConfirmationEntryPane.defaultHeight
          );
          abortConfirmationDialog.show();
        }
        else
        {
          self._onAbortTherapy(therapy, null);
        }
      }
      else
      {
        var message = view.getDictionary('therapy.can.not.stop.if.linked');
        view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
      }
    });
    toolBar.setUntagTherapyForPrescriptionEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      view.untagTherapyForPrescription(
          view.getPatientId(),
          view.getCentralCaseData().centralCaseId,
          therapy.getCompositionUid(),
          therapy.getEhrOrderName());
    });
    toolBar.setTagTherapyForPrescriptionEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      view.tagTherapyForPrescription(
          view.getPatientId(),
          view.getCentralCaseData().centralCaseId,
          therapy.getCompositionUid(),
          therapy.getEhrOrderName());
    });
    toolBar.setSuspendTherapyEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;

      if (therapy.isLinkedToAdmission())
      {
        var actionTypeEnum = actionEnums.SUSPEND;
        var changeReasonMap = view.getTherapyChangeReasonTypeMap();
        self._onSuspendTherapy(therapy,
            tm.views.medications.MedicationUtils.getFirstOrNullTherapyChangeReason(changeReasonMap, actionTypeEnum));
      }
      else
      {
        self._onSuspendTherapy(therapy, null);
      }
    });
    toolBar.setCopyTherapyEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      view.showEditTherapyDialog(therapy, true, false);
    });
    toolBar.setShowMedicationInfoCallback(function(therapyContainer)
    {
      var therapy = therapyContainer.getData().therapy;
      self._showMedicationDetailsContainer(therapy);
    });
    toolBar.setAdministerScheduledTaskEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var therapyDoseTypeEnum = self._getTherapyDoseTypeEnum(data);
      var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.START;
      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType, true, false);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            data.administrations,
            null,
            true,
            view.getDictionary("administration.schedule.additional"),
            therapyDoseTypeEnum,
            administrationType);
      }
    });
    toolBar.setAdministerUnscheduledTaskEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var therapyDoseTypeEnum = self._getTherapyDoseTypeEnum(data);
      var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.START;

      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType, false, true);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            data.administrations,
            null,
            false,
            view.getDictionary("administration.apply.unplanned"),
            therapyDoseTypeEnum,
            administrationType);
      }
    });
    toolBar.setAdjustRateEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.ADJUST_INFUSION;

      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            data.administrations,
            null,
            false,
            therapy.isOrderTypeOxygen() ? view.getDictionary("adjust.oxygen.rate") :
                view.getDictionary("adjust.infusion.rate"),
            app.views.medications.TherapyEnums.therapyDoseTypeEnum.RATE,
            app.views.medications.TherapyEnums.administrationTypeEnum.ADJUST_INFUSION,
            false,
            false
        );
      }
    });
    toolBar.setStopFlowCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var isInfusionActive = therapyContainer.getData().currentInfusionRate !== 0;
      var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.ADJUST_INFUSION;
      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType, false, false, isInfusionActive);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            data.administrations,
            null,
            false,
            isInfusionActive ? view.getDictionary('pause.flow.rate') : view.getDictionary('resume.flow.rate'),
            app.views.medications.TherapyEnums.therapyDoseTypeEnum.RATE,
            administrationType,
            false,
            true
        );
      }
    });
    toolBar.setInfusionSetChangeEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      self._showTherapyAdministrationContainer(
          therapy,
          data.administrations,
          null,
          false,
          view.getDictionary("infusion.set.change"),
          null,
          app.views.medications.TherapyEnums.administrationTypeEnum.INFUSION_SET_CHANGE,
          false,
          false);
    });
    toolBar.setTasksChangedEventCallback(function()
    {
      self.reloadTimelinesFunction();
    });
    toolBar.setEditSelfAdministeringCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;

      self._onEditSelfAdministering(therapy);
    });
    toolBar.setPerfusionSyringeRequestEventCallback(function(menuHotSpot, therapyContainer)
    {
      var data = therapyContainer.getData();
      self._showOrderPerfusionSyringeToolTip(data.therapy, menuHotSpot);
    });
    toolBar.setChangeOxygenStartingDeviceCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      self._showOxygenStartingDeviceDialog(data.therapy, data.administrations, null);
    });

    toolBar.setAddBolusEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var therapyDoseTypeEnum = self._getTherapyDoseTypeEnum(data, true);
      var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.BOLUS;
      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            data.administrations,
            null,
            false,
            view.getDictionary('add.bolus.administration'),
            therapyDoseTypeEnum,
            administrationType,
            false,
            false);
      }
    });

    therapyContainer.setToolbar(toolBar);

    return therapyContainer;
  },

  _onAbortTherapy: function (therapy, changeReason)
  {
    var self = this;
    var view = this.getView();
    view.showLoaderMask();
    this.therapyActions.abortTherapy(therapy.getCompositionUid(), therapy.getEhrOrderName(), changeReason ? changeReason.value : null,
        function ()
        {
          self.view.hideLoaderMask();
          self.reloadTimelinesFunction();
        },
        function ()
        {
          self.view.hideLoaderMask();
          self._redrawTimeline();
        });
  },

  _onSuspendTherapy: function (therapy, changeReason)
  {
    var self = this;
    var view = this.getView();
    view.showLoaderMask();
    this.therapyActions.suspendTherapy(therapy.getCompositionUid(), therapy.getEhrOrderName(), changeReason ? changeReason.value : null,
        function ()
        {
          self.view.hideLoaderMask();
          self.reloadTimelinesFunction();
        },
        function ()
        {
          self.view.hideLoaderMask();
          self._redrawTimeline();
        });
  },
  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @private
   */
  _showMedicationDetailsContainer: function(therapy)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    view.getRestApi().loadMedicationDataForMultipleIds(therapy.getAllIngredientIds()).then(function(medicationData)
    {
      var medicationDetailsContainer = new app.views.medications.common.MedicationDetailsContainer({
        view: view,
        medicationData: medicationData
      });

      var medicationDetailsContainerDialog = appFactory.createDefaultDialog(
          view.getDictionary("drug.information"),
          null,
          medicationDetailsContainer,
          null,
          450,
          medicationData.length * 170 + 35
      );
      medicationDetailsContainerDialog.setHideOnDocumentClick(true);
      medicationDetailsContainerDialog.show();

    });
  },

  _onEditSelfAdministering: function(therapy)
  {
    var self = this;
    var view = this.getView();
    var isTherapySelfAdmin = !tm.jquery.Utils.isEmpty(therapy.selfAdministeringActionEnum);

    var dialog = view.getAppFactory().createDataEntryDialog(
        isTherapySelfAdmin ? view.getDictionary('edit.self.administration') : view.getDictionary('self.administration'),
        null,
        new app.views.medications.therapy.SelfAdministrationContainer({
          view: view,
          therapy: therapy,
          patientId: self.patientId
        }),
        function(resultData)
        {
          if (resultData) // self administering action enum
          {
            self.reloadTimelinesFunction();
          }
        },
        "auto", 130
    );
    dialog.header.setCls("therapy-admin-header");
    dialog.getFooter().setCls("therapy-admin-footer");
    dialog.getFooter().rightContainer.layout.gap = 0;
    dialog.show();
  },

  _sendNurseResupplyRequest: function(patientId, therapyDto)
  {
    var view = this.getView();
    view.showLoaderMask();
    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SEND_THERAPY_RESUPPLY_REQUEST;

    var self = this;
    var params = {
      patientId: patientId,
      therapy: JSON.stringify(therapyDto)
    };

    view.sendPostRequest(url, params, function ()
        {
          self.view.hideLoaderMask();
          self.reloadTimelinesFunction();
        },
        function ()
        {
          self.view.hideLoaderMask();
          self._redrawTimeline();
        },
        app.views.common.AppNotifierDisplayType.HTML);
  },

  /**
   *
   * @param {app.views.medications.common.dto.Therapy} therapyDto
   * @param {tm.jquery.Component} menuHotSpot
   * @private
   */
  _showOrderPerfusionSyringeToolTip: function(therapyDto, menuHotSpot)
  {
    var view = this.getView();
    var self = this;
    var appFactory = view.getAppFactory();

    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_FINISHED_PERFUSION_SYRINGE_REQUESTS_EXIST;
    var params = {
      patientId: view.getPatientId(),
      originalTherapyId: therapyDto.getTherapyId(),
      hours: 24
    };

    view.showLoaderMask();

    view.loadViewData(url, params, null, function(result)
    {
      view.hideLoaderMask();

      var warningText = result === true ? view.getDictionary("finished.perfusion.syringe.requests.24h.warning.text") : null;

      var entryContainer = new app.views.medications.common.PerfusionSyringeDataEntryContainer({
        view: view,
        warningText: warningText
      });

      var popoverTooltip = appFactory.createDataEntryPopoverTooltip(
          view.getDictionary("add.to.catheter"),
          entryContainer,
          function(resultData)
          {
            if (resultData && (resultData).success)
            {
              view.showLoaderMask();
              var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_ORDER_THERAPY_PERFUSION_SYRINGE;

              var params = {
                patientId: view.getPatientId(),
                compositionUid: therapyDto.compositionUid,
                ehrOrderName: therapyDto.ehrOrderName,
                numberOfSyringes: resultData.value.count,
                urgent: resultData.value.urgent,
                dueTime: JSON.stringify(resultData.value.orderDate),
                printSystemLabel: resultData.value.printSystemLabel
              };

              view.sendPostRequest(url, params,
                  function()
                  {
                    view.hideLoaderMask();
                    self.reloadTimelinesFunction();
                  },
                  function()
                  {
                    view.hideLoaderMask();
                  },
                  app.views.common.AppNotifierDisplayType.HTML);
            }
          }
      );

      entryContainer.onKey(
          new tm.jquery.event.KeyStroke({key: "esc", altKey: false, ctrlKey: false, shiftKey: false}),
          function()
          {
            popoverTooltip.cancel();
          }
      );

      popoverTooltip.setTrigger('manual');
      popoverTooltip.setWidth(entryContainer.getDefaultWidth());
      popoverTooltip.setHeight(tm.jquery.Utils.isEmpty(warningText) ?
          entryContainer.getDefaultHeight() : entryContainer.getDefaultHeight() + 40);
      menuHotSpot.setTooltip(popoverTooltip);

      setTimeout(function()
      {
        popoverTooltip.show();
      }, 0);
    });
  },

  _canTherapyBeAborted: function(therapy)
  {
    var enums = app.views.medications.TherapyEnums;
    if (therapy.getLinkName())
    {
      var nextTherapyLink = tm.views.medications.MedicationUtils.getNextLinkName(therapy.getLinkName());
      var linkedTherapyDay = this._getTherapyDayByLinkName(nextTherapyLink);
      if (linkedTherapyDay && linkedTherapyDay.therapyStatus)
      {
        if (linkedTherapyDay.therapyStatus !== enums.therapyStatusEnum.ABORTED &&
            linkedTherapyDay.therapyStatus !== enums.therapyStatusEnum.CANCELLED)
        {
          return false;
        }
      }
    }
    return true;
  },

  _getTherapyDayByLinkName: function(linkName)
  {
    for (var i = 0; i < this.therapyTimelineRows.length; i++)
    {
      var therapyDay = this.therapyTimelineRows[i];
      if (therapyDay && therapyDay.therapy && therapyDay.therapy.getLinkName() && therapyDay.therapy.getLinkName() === linkName)
      {
        return therapyDay;
      }
    }
    return null;
  },

  _getTherapyDoseTypeEnum: function(therapyTimeline, bolus)
  {
    var enums = app.views.medications.TherapyEnums;
    var therapy = therapyTimeline.therapy;

    var doseElement = !tm.jquery.Utils.isEmpty(therapy) ? therapy.getDoseElement() : null;
    var isDescriptiveDose = doseElement && doseElement.doseDescription && doseElement.quantity;

    if (bolus === true)
    {
      if (therapy.getIngredientsList() && therapy.getIngredientsList().length > 1)
      {
        return enums.therapyDoseTypeEnum.VOLUME_SUM;
      }
      else
      {
        return enums.therapyDoseTypeEnum.QUANTITY;
      }
    }

    if (!tm.jquery.Utils.isEmpty(isDescriptiveDose) && isDescriptiveDose)
    {
      return null;
    }

    if (!tm.jquery.Utils.isEmpty(therapy) && !tm.jquery.Utils.isEmpty(therapy.getDoseType()))
    {
      return therapy.getDoseType();
    }

    if (therapy.isOrderTypeComplex())
    {
      var firstAdministrationWithDose = null;
      var administrations = therapyTimeline.administrations;

      for (var i = 0; i < administrations.length; i++)
      {
        if (administrations[i].plannedDose || administrations[i].administeredDose)
        {
          firstAdministrationWithDose = administrations[i];
          break;
        }
      }
      if (!firstAdministrationWithDose)
      {
        return enums.therapyDoseTypeEnum.QUANTITY;
      }
      else if (firstAdministrationWithDose.plannedDose)
      {
        return firstAdministrationWithDose.plannedDose.therapyDoseTypeEnum;
      }
      else
      {
        return firstAdministrationWithDose.administeredDose.therapyDoseTypeEnum;
      }
    }
    else
    {
      return enums.therapyDoseTypeEnum.QUANTITY;
    }
  },

  _handleRightClickOnElement: function(selectedItem, elementEvent)
  {
    if (selectedItem && selectedItem.administration)
    {
      var $taskElement = $("#" + this._getTaskId(selectedItem.administration));
      var offset = $taskElement.offset();
      var popupMenu = this._createPopupMenu(selectedItem);

      if (popupMenu.hasMenuItems())
      {
        popupMenu.show(function ()
        {
          var position = tm.jquery.ComponentUtils.calculatePopupMenuPosition(popupMenu, elementEvent);
          position.x = offset.left + $taskElement.width() + 5;
          return position;
        });
      }
    }
  },

  _hasAdditionalWarnings: function(additionalWarnings)
  {
      return additionalWarnings.length > 0;
  },

  _handleLeftClickOnElement: function(selectedItem)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var administration = selectedItem.administration;
    var self = this;

    if (selectedItem && selectedItem.changeRangeOnClick)
    {
      var selectedItemHours = selectedItem.start.getHours();
      var timelineStart = new Date(selectedItem.start);
      timelineStart.setHours(selectedItemHours - 1);
      var timelineEnd = new Date(selectedItem.start);
      timelineEnd.setHours(selectedItemHours + 3);
      this.onTimelineRangeChange(timelineStart, timelineEnd);
    }
    else if (selectedItem && administration)
    {
      var therapy = selectedItem.therapy;
      var selectedTherapyId = therapy.getTherapyId();
      var selectedTimelineRow = self._getTimelineRowByTherapyId(selectedTherapyId);
      var administrationStatus = administration.administrationStatus;

      if (self._hasAdditionalWarnings(selectedTimelineRow.additionalWarnings))
      {
        var appFactory = view.getAppFactory();
        var message = view.getDictionary('additional.warning.doctor.review');
        var confirmSystemDialog = appFactory.createWarningSystemDialog(message, 391, 141);
        confirmSystemDialog.show();
      }
      else if (administrationStatus === enums.administrationStatusEnum.PLANNED ||
          administrationStatus === enums.administrationStatusEnum.DUE ||
          administrationStatus === enums.administrationStatusEnum.LATE)
      {
        var byDoctorsOrders = therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS;

        if (byDoctorsOrders && administration.taskId &&
            (administration.doctorConfirmation === null || administration.doctorConfirmation === false))
        {
          this._showConfirmationContainer(therapy, administration);
        }
        else if (therapy.isTitrationDoseType() && !administration.plannedDose &&
            (administration.administrationType === enums.administrationTypeEnum.START ||
            administration.administrationType === enums.administrationTypeEnum.ADJUST_INFUSION))
        {
          this._showTitrationBasedAdministrationDialog(therapy, administration, administration.administrationType);
        }
        else if (view.getTherapyAuthority().isManageAdministrationsAllowed())
        {
          if ((therapy.isContinuousInfusion() || therapy.isOrderTypeOxygen()) &&
              (!administration || administration.administrationType === enums.administrationTypeEnum.START))
          {
            this._showTherapyAdministrationContainer(
                therapy,
                this._getTherapyAdministrationsByTherapy(therapy),
                administration,
                false,
                view.getDictionary('administration'),
                app.views.medications.TherapyEnums.therapyDoseTypeEnum.RATE,
                enums.administrationTypeEnum.START);
          }
          else
          {
            this._showTherapyAdministrationContainer(
                therapy,
                this._getTherapyAdministrationsByTherapy(therapy),
                administration,
                false,
                view.getDictionary('administration'),
                null,
                administration && administration.administrationType == enums.administrationTypeEnum.INFUSION_SET_CHANGE ?
                    administration.administrationType :
                    null
            );
          }
        }
      }
      else if (!tm.jquery.Utils.isEmpty(administration.administrationResult) && selectedItem.taskContainer)
      {
        this._ensureTherapyVersionMatchesAdministration(therapy, administration).then(
            function(syncedTherapy)
            {
              self._showAdministrationDetailsContentPopup(selectedItem.taskContainer, syncedTherapy, administration);
            }
        );
      }
    }
    setTimeout(function()
    {
      self._preventDoubleClickOnTask = false;
    }, 250);
  },

  _openDeleteAdministrationDialog: function(selectedItem)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    if (selectedItem && selectedItem.administration)
    {
      var deleteAdministrationAllowed = view.getTherapyAuthority().isEditAllowed() ||
          selectedItem.therapy.getStartCriterion() !== app.views.medications.TherapyEnums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS ||
          tm.jquery.Utils.isEmpty(selectedItem.administration.taskId);

      if (deleteAdministrationAllowed)
      {
        var isAdministrationConfirmed = this._isAdministrationConfirmed(selectedItem.administration);

        var deleteCommentField = new tm.jquery.TextField({width: 370});
        var deleteContainer = new app.views.common.containers.AppDataEntryContainer({
          layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center', 5),
          cls:'delete-container',
          startProcessOnEnter: true,
          processResultData: function(resultCallback)
          {
            var validationForm = new tm.jquery.Form({
              onValidationSuccess: function()
              {
                if (isAdministrationConfirmed)
                {
                  self._deleteAdministration(resultCallback, deleteCommentField.getValue(), selectedItem.administration, selectedItem.therapy);
                }
                else
                {
                  self._deleteTask(resultCallback, deleteCommentField.getValue(), selectedItem.administration);
                }
              },
              onValidationError: function()
              {
                resultCallback(new app.views.common.AppResultData({success: false}));
              },
              requiredFieldValidatorErrorMessage: view.getDictionary("field.value.is.required")
            });
            validationForm.addFormField(new tm.jquery.FormField({component: deleteCommentField, required: true}));
            validationForm.submit();
          }
        });
        deleteContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', view.getDictionary("delete.reason")));
        deleteContainer.add(deleteCommentField);
        deleteContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
        {
          setTimeout(function()
          {
            deleteCommentField.focus();
          }, 300);
        });

        var administrationTimestamp =
            tm.views.medications.MedicationTimingUtils.getAdministrationTimestamp(selectedItem.administration);
        var administrationTimeDisplay = view.getDisplayableValue(administrationTimestamp, "short.time");
        var title = isAdministrationConfirmed ? view.getDictionary("delete.administration.at") + " " + administrationTimeDisplay :
        view.getDictionary("delete.scheduled.administration.at") + " " + administrationTimeDisplay;
        var therapyAdministrationDialog = appFactory.createDataEntryDialog(
            title,
            null,
            deleteContainer,
            function(resultData)
            {
              if (resultData)
              {
                self.reloadTimelinesFunction();
              }
              else
              {
                self._redrawTimeline();
              }
            }, 500, 130
        );
        therapyAdministrationDialog.setContainmentElement(view.getDom());
        therapyAdministrationDialog.show();
      }
      else
      {
        appFactory.createWarningSystemDialog(view.getDictionary("by.doctors.orders.not.reviewed.delete"), 350, 170).show();
      }
    }
  },

  _deleteTask: function(resultCallback, comment, administration)
  {
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_DELETE_ADMINISTRATION_TASK_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var params = {
      patientId: this.patientId,
      taskId: administration.taskId,
      therapyId: administration.therapyId
    };

    if (!tm.jquery.Utils.isEmpty(administration.groupUUId))
    {
      params.groupUUId = administration.groupUUId;
    }
    if (!tm.jquery.Utils.isEmpty(comment))
    {
      params.comment = comment;
    }

    var deleteUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DELETE_TASK;
    view.loadPostViewData(deleteUrl, params, null,
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: true}));
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: false}));
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  _deleteAdministration: function(resultCallback, comment, administration, therapy)
  {
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_DELETE_ADMINISTRATION_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var params = {
      patientId: this.patientId,
      administration: JSON.stringify(administration),
      therapyId: therapy.getTherapyId(),
      therapyDoseType: therapy.getDoseType(),
      comment: comment
    };

    var deleteUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DELETE_ADMINISTRATION;
    view.loadPostViewData(deleteUrl, params, null,
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: true}));
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: false}));
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @private
   */
  _createEditAdministrationContainer: function(therapy, administration)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    if (administration && administration.adjustAdministrationSubtype === enums.adjustAdministrationSubtype.OXYGEN
        && administration.startingDevice)
    {
      var administrations = this._getTimelineRowByTherapyId(therapy.getTherapyId()).administrations;
      this._showOxygenStartingDeviceDialog(therapy, administrations, administration);
    }
    else
    {
      var stopInfusionRateAdministration = !tm.jquery.Utils.isEmpty(administration.administeredDose) && administration.administeredDose.numerator == 0;
      this._showTherapyAdministrationContainer(
          therapy,
          this._getTherapyAdministrationsByTherapy(therapy),
          administration,
          false,
          view.getDictionary('edit'),
          null,
          administration.administrationType,
          true,
          stopInfusionRateAdministration
      );
    }
  },

  _redrawTimeline: function()
  {
    this.clearTestCoordinator();
    this.timeline.redraw();
    this._insertTestCoordinator();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param administration
   * @private
   */
  _createRescheduleTasksContainer: function(therapy, administration)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var view = this.getView();
    var rescheduleContainer = new tm.views.medications.timeline.RescheduleTasksContainer({
      view: view,
      startProcessOnEnter: true,
      administration: administration,
      administrations: this._getTherapyAdministrationsByTherapy(therapy),
      therapy: therapy
    });
    var rescheduleContainerDialog = view.getAppFactory().createDataEntryDialog(
        view.getDictionary('move.administration'),
        null,
        rescheduleContainer,
        function(resultData)
        {
          if (resultData)
          {
            self.reloadTimelinesFunction();
          }
          else
          {
            self._redrawTimeline();
          }
        },
        350,
        (therapy.getDosingFrequency() && therapy.getDosingFrequency().type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES) ? 
            270 : 220
    );
    rescheduleContainer.setEnableDialogConfirmationFunction(function(enabled)
    {
      rescheduleContainerDialog.getConfirmButton().setEnabled(enabled);
    });

    rescheduleContainerDialog.show();
  },

  _createDoctorsCommentContainer: function(therapy, administration)
  {
    var self = this;
    var view = this.getView();
    var doctorsCommentContainer = new tm.views.medications.timeline.DoctorsCommentDataEntryContainer({
      view: view,
      therapy: therapy,
      administration: administration
    });
    var doctorsCommentDialog = view.getAppFactory().createDataEntryDialog(
        view.getDictionary('doctors.comment'),
        null,
        doctorsCommentContainer,
        function(resultData)
        {
          if (resultData)
          {
            self.reloadTimelinesFunction();
          }
          else
          {
            self._redrawTimeline();
          }
        },
        450,
        300
    );
    doctorsCommentDialog.show();
  },

  _showConfirmationContainer: function(therapy, administration)
  {
    var view = this.getView();
    var therapyDescriptionContainer = new tm.jquery.Container({
      layout: new tm.jquery.HFlexboxLayout(),
      html: therapy.getFormattedTherapyDisplay(),
      cls: 'TherapyDescription container'
    });

    if (administration.plannedTime)
    {
      therapyDescriptionContainer.setHtml(therapyDescriptionContainer.getHtml() +
          tm.views.medications.MedicationTimingUtils.getFormattedAdministrationPlannedTime(view, administration));
    }
    var self = this;
    var height = view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() ? 200 : 220;
    if (!tm.jquery.Utils.isEmpty(therapy.getIngredientsList()) && therapy.getIngredientsList().length > 1)
    {
      height += 38;
    }
    if (!tm.jquery.Utils.isEmpty(therapy.getComment()) && therapy.getComment().length > 0)
    {
      height += 28;
    }
    var confirmationContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      scrollable: "both"
    });
    var confirmationContainerDialog = view.getAppFactory().createDataEntryDialog(
        view.getDictionary('therapy.administration'),
        null,
        confirmationContainer,
        function(resultData)
        {
          if (resultData)
          {
            self.reloadTimelinesFunction();
          }
          else
          {
            self._redrawTimeline();
          }
        },
        448,
        height
    );
    var administerButton = new tm.jquery.Button({
      cls: "footer-right-btn",
      text: view.getDictionary('confirm.administration'),
      handler: function()
      {
        self._setDoctorConfirmationResult(administration, true);
        confirmationContainerDialog.hide();
      }
    });
    var doNotAdministerButton = new tm.jquery.Button({
      type: "link",
      cls: "no-border-btn",
      text: view.getDictionary('cancel.administration'),
      handler: function()
      {
        self._setDoctorConfirmationResult(administration, false);
        confirmationContainerDialog.hide();
      }
    });

    var administrationNotPossibleWarning = administration.doctorConfirmation === false ?
        view.getDictionary('therapy.administration.withdrawn') :
        view.getDictionary('therapy.administration.must.be.confirmed');
    var administrationNotPossibleLabel = new tm.jquery.Container({
      cls: "TextData administration-not-possible-label",
      html: administrationNotPossibleWarning
    });
    confirmationContainer.add(therapyDescriptionContainer);
    confirmationContainerDialog.header.setCls("therapy-admin-header");
    confirmationContainerDialog.getFooter().setCls("therapy-admin-footer");

    if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() && administration.doctorConfirmation !== false)
    {
      confirmationContainerDialog.setRightButtons([administerButton]);
      confirmationContainerDialog.setLeftButtons([doNotAdministerButton]);
    }
    else
    {
      confirmationContainer.add(administrationNotPossibleLabel);
      var closeButton = confirmationContainerDialog.getRightButtons()[1];
      closeButton.setText(view.getDictionary('close'));
      closeButton.setCls("footer-right-btn");
      confirmationContainerDialog.setRightButtons([closeButton]);
    }
    confirmationContainerDialog.show();
  },

  _setDoctorConfirmationResult: function(administration, result)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_MOVE_ADMINISTRATION_HUB;
    viewHubNotifier.actionStarted(hubAction);
    var params = {
      taskId: administration.taskId,
      result: result
    };

    var doctorConfirmationUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SET_DOCTOR_CONFIRMATION_RESULT;
    view.loadPostViewData(doctorConfirmationUrl, params, null,
        function()
        {
          self.reloadTimelinesFunction();
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          self.reloadTimelinesFunction();
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @returns {tm.jquery.Deferred}
   * @private
   */
  _ensureTherapyVersionMatchesAdministration: function(therapy, administration)
  {
    var currentTherapyId = therapy.getTherapyId();
    var isOldTherapyAdministration = administration && therapy && currentTherapyId !== administration.therapyId;

    if (isOldTherapyAdministration) //therapy was modified, this administration is from old therapy
    {
      return this.getView().getRestApi().loadTherapy(administration.therapyId);
    }
    else
    {
      var deferred = tm.jquery.Deferred.create();
      deferred.resolve(therapy);
      return deferred.promise();
    }
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<Object>} administrations
   * @param {Object} administration
   * @param {boolean} createNewTask
   * @param {string} containerTitle
   * @param {app.views.medications.TherapyEnums.therapyDoseTypeEnum|string} therapyDoseTypeEnum
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|string} administrationType
   * @param {boolean} [editMode=false]
   * @param {boolean} [stopFlow=false]
   * @private
   */
  _showTherapyAdministrationContainer: function(therapy, administrations, administration, createNewTask, containerTitle,
                                                therapyDoseTypeEnum, administrationType, editMode, stopFlow,
                                                scannedMedicationId, barcode)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    this._ensureTherapyVersionMatchesAdministration(therapy, administration).then(
        function(syncedTherapy)
        {
          view.showLoaderMask();
          appFactory.createConditionTask(
              function()
              {
                view.hideLoaderMask();
                self._showTherapyAdministrationContainerImpl(
                    syncedTherapy,
                    administrations,
                    administration,
                    createNewTask,
                    containerTitle,
                    therapyDoseTypeEnum,
                    administrationType,
                    editMode,
                    stopFlow,
                    scannedMedicationId,
                    barcode
                );
              },
              function()
              {
                return !tm.jquery.Utils.isEmpty(view.getTherapyChangeReasonTypeMap());
              },
              70, 10);
        }
    );
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<Object>} administrations
   * @param {Object} administration
   * @param {boolean} createNewTask
   * @param {string} containerTitle
   * @param {app.views.medications.TherapyEnums.therapyDoseTypeEnum|string} therapyDoseTypeEnum
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|string} administrationType
   * @param {boolean} [editMode=false]
   * @param {boolean} [stopFlow=false]
   * @param {Number} scannedMedicationId
   * @param {String} barcode
   * @private
   */
  _showTherapyAdministrationContainerImpl: function(therapy, administrations, administration, createNewTask, containerTitle,
                                                    therapyDoseTypeEnum, administrationType, editMode, stopFlow,
                                                    scannedMedicationId, barcode)
  {
    var self = this;
    var timelineRowData = this._getTimelineRowByAdministration(administration);
    if (!timelineRowData)
    {
      timelineRowData = this._getTimelineRowByTherapyId(therapy.getTherapyId());
    }

    this._administrationDialogBuilder.showAdministrationDialog(
        timelineRowData,
        therapy,
        administrations,
        administration,
        createNewTask,
        containerTitle,
        therapyDoseTypeEnum,
        administrationType,
        editMode,
        stopFlow,
        scannedMedicationId,
        barcode).then(
        function onResolve(resultData)
        {
          if (resultData)
          {
            self.reloadTimelinesFunction();
          }
          else
          {
            self._redrawTimeline();
          }

          self.view.setActionCallbackListener(null);
        });
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|String|undefined} [administrationType=undefined]
   * @param {Boolean} [scheduleAdditional=false]
   * @param {Boolean} [applyUnplanned=false]
   * @param {Boolean} [stopFlow=false]
   * @private
   */
  _showTitrationBasedAdministrationDialog: function(therapy, administration, administrationType, scheduleAdditional,
  applyUnplanned, stopFlow)
  {
    var self = this;
    var timelineRowData = this._getTimelineRowByTherapyId(therapy.getTherapyId());
    var lastPositiveInfusionRate = null;
    var activeContinuousInfusion = false;

    if ((therapy.isContinuousInfusion() && timelineRowData))
    {
      lastPositiveInfusionRate = timelineRowData.lastPositiveInfusionRate;
      activeContinuousInfusion = timelineRowData.infusionActive;
    }

    var dialogBuilder = new app.views.medications.timeline.titration.TitrationDialogBuilder({
      view: this.getView()
    });

    dialogBuilder.showAdministrationDialog(
        therapy,
        administration,
        timelineRowData.administrations,
        administrationType,
        lastPositiveInfusionRate,
        activeContinuousInfusion,
        scheduleAdditional,
        applyUnplanned,
        stopFlow)
        .then(function onDialogCloseHandler(resultData)
        {
          if (resultData && resultData.success)
          {
            self.reloadTimelinesFunction();
          }
        });
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<Object>} administrations
   * @param {Object|null} administration
   * @private
   */
  _showOxygenStartingDeviceDialog: function(therapy, administrations, administration)
  {
    var view = this.getView();
    var self = this;
    var timelineRow = this._getTimelineRowByTherapyId(therapy.getTherapyId());

    var startingDevice = null;
    if (administration && administration.startingDevice)
    {
      startingDevice = new app.views.medications.common.dto.OxygenStartingDevice(administration.startingDevice)
    }
    else if (timelineRow && timelineRow.currentStartingDevice)
    {
      startingDevice = timelineRow.currentStartingDevice
    }
    
    var oxygenStartingDeviceEntryContainer =
        new app.views.medications.timeline.administration.OxygenStartingDeviceDataEntryContainer({
          view: view,
          therapy: therapy,
          currentStartingDevice: startingDevice,
          currentFlowRate: timelineRow.currentInfusionRate,
          administration: administration
        });

    var oxygenStartingDeviceDialog = view.getAppFactory().createDataEntryDialog(
        view.getDictionary("change.device"),
        null,
        oxygenStartingDeviceEntryContainer,
        function(resultData)
        {
          if (resultData)
          {
            self.reloadTimelinesFunction();
          }
          else
          {
            self._redrawTimeline();
          }
        },
        475,
        450
    );

    oxygenStartingDeviceDialog.show();
  },

  /**
   * @param {String} therapyId
   * @returns {Object|*}
   * @private
   */
  _getTimelineRowByTherapyId: function(therapyId)
  {
    for (var i = 0; i < this.therapyTimelineRows.length; i++)
    {
      if (this.therapyTimelineRows[i].therapy.getTherapyId() === therapyId)
      {
        return this.therapyTimelineRows[i];
      }
    }
    return null;
  },

  /**
   * @param {Object} administration
   * @returns {Object|null}
   * @private
   */
  _getTimelineRowByAdministration: function(administration)
  {
    for (var i = 0; i < this.therapyTimelineRows.length; i++)
    {
      if (tm.jquery.Utils.isArray(this.therapyTimelineRows[i].administrations) &&
          this.therapyTimelineRows[i].administrations.indexOf(administration) > -1)
      {
        return this.therapyTimelineRows[i];
      }
    }
    return null;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @returns {Array<Object>}
   * @private
   */
  _getTherapyAdministrationsByTherapy: function(therapy)
  {
    var therapyTimeline = this._getTimelineRowByTherapyId(therapy.getTherapyId());
    return therapyTimeline ? therapyTimeline.administrations : [];
  },

  /**
   * @param {Object} taskStateContainer
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @private
   */
  _showAdministrationDetailsContentPopup: function(taskStateContainer, therapy, administration)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var therapyAdministrationDetailsContent = new tm.views.medications.timeline.TherapyAdministrationDetailsContentContainer({
      view: view,
      displayProvider: this.displayProvider,
      therapy: therapy,
      administration: administration
    });
    var therapyAdministrationDetailsPopup = appFactory.createDefaultPopoverTooltip(
        view.getDictionary('administration'),
        null,
        therapyAdministrationDetailsContent
    );
    therapyAdministrationDetailsPopup.setPlacement("auto");
    therapyAdministrationDetailsPopup.setDefaultAutoPlacements(["bottom", "top", "center", "rightBottom", "rightTop"]);

    if (this.getScrollableElement())
    {
      therapyAdministrationDetailsPopup.setAppendTo(this.getScrollableElement());
    }

    therapyAdministrationDetailsPopup.onHide = function()
    {
      if (!tm.jquery.ClientUserAgent.isTablet() &&
          !(taskStateContainer.getTooltip() instanceof tm.views.medications.timeline.TherapyTimelineTooltip))
      {
        var isDeferredTask = self._isAdministrationTaskDeferred(therapy, administration);
        var hoverTooltip = self._getTherapyAdministrationTooltip(therapy, administration, isDeferredTask);

        setTimeout(function()
        {
          taskStateContainer.setTooltip(hoverTooltip);
        }, 10);
      }
    };
    therapyAdministrationDetailsPopup.setTrigger("manual");
    taskStateContainer.setTooltip(therapyAdministrationDetailsPopup);

    setTimeout(function()
    {
      therapyAdministrationDetailsPopup.show();
    }, 10);
  },

  /**
   * @param {Date} from
   * @param {Date} to
   * @param {String|Element} taskContent
   * @param {String} groupId
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {String} className
   * @param {String} type
   * @param {Boolean} changeRangeOnClick
   * @returns {{start: *, end: *, content: string, group: *, therapy: *, administration: *, className: *, type: null, changeRangeOnClick: boolean}}
   * @private
   */
  _buildTimelineElement: function(from, to, taskContent, groupId, therapy, administration, className, type, changeRangeOnClick)
  {
    return {
      start: from,
      end: to,
      content: !taskContent ? '' : taskContent,
      group: groupId,
      therapy: therapy,
      administration: administration,
      className: className,
      type: tm.jquery.Utils.isEmpty(type) ? null : type,
      changeRangeOnClick: changeRangeOnClick ? changeRangeOnClick : false
    };
  },

  _buildTimelineAdministrationTaskElement: function(administrationTime, taskContainer, groupId, therapy, administration)
  {
    return {
      start: administrationTime,
      content: taskContainer.getDom(),
      group: groupId,
      therapy: therapy,
      administration: administration,
      taskContainer: taskContainer
    };
  },

  _buildGroups: function(therapyTimelineRows, readOnly)
  {
    var groups = [];

    for (var idx = 0; idx < therapyTimelineRows.length; idx++)
    {
      var groupContentContainer = this._buildGroupHeaderTherapyContainer(therapyTimelineRows[idx], readOnly);
      var additionalWarnings = therapyTimelineRows[idx].additionalWarnings;

      groupContentContainer.doRender();

      groups.push({
        id: therapyTimelineRows[idx].therapy.getTherapyId(),
        orderIndex: idx,
        content: groupContentContainer.getDom(),
        className: this._hasAdditionalWarnings(additionalWarnings) ? "additional-warning" : ""
      });
    }

    return groups;
  },

  /**
   * @param {Object} administration
   * @returns {boolean}
   * @private
   */
  _isAdministrationAdministered: function(administration)
  {
    var enums = app.views.medications.TherapyEnums;
    return (administration.administrationResult === enums.administrationResultEnum.GIVEN ||
        administration.administrationResult === enums.administrationResultEnum.SELF_ADMINISTERED);
  },

  /**
   *
   * @param {app.views.medications.common.dto.BarcodeTaskSearch} barcodeTaskSearch
   * @param {String} barcode
   */
  _medicationIdentifierScanned: function(barcodeTaskSearch, barcode)
  {
    for (var i = 0; i < this.therapyTimelineRows.length; i++)
    {
      var timelineRow = this.therapyTimelineRows[i];
      if (timelineRow.administrations)
      {
        for (var j = 0; j < timelineRow.administrations.length; j++)
        {
          var administration = timelineRow.administrations[j];
          if (administration.taskId === barcodeTaskSearch.getTaskId())
          {
            this._showTherapyAdministrationContainer(timelineRow.therapy, timelineRow.administrations, administration, false,
                this.getView().getDictionary('administration'), null, administration.administrationType, false, false,
                barcodeTaskSearch.getMedicationId(), barcode);
          }
        }
      }
    }
  },

  /** public methods */
  setTherapyTimelineData: function(therapyTimelineRows, readOnly)
  {
    this.therapyTimelineRows = therapyTimelineRows;
    this._buildTimeline(readOnly);
  },

  setVisibleRange: function(start, end)
  {
    if (this.timeline)
    {
      var range = this.timeline.getWindow();
      if (range.start !== start || range.end !== end)
      {
        this.timeline.setWindow(start, end, {animation: false});
        /* make sure animation is off, otherwise slowdowns occur */
      }
    }
  },

  clear: function()
  {
    this.therapyTimelineRows.removeAll();
    this._buildTimeline();
  },

  clearTestCoordinator: function()
  {
    this._testRenderCoordinator.removeCoordinator();
  },

  redrawTimeline: function()
  {
    this._redrawTimeline();
  },

  getGroupSet: function()
  {
    return this._groupSet;
  },

  getItemSet: function()
  {
    return this._itemSet;
  },

  setMinMaxTimeline: function(min, max)
  {
    this.options.min = min;
    this.options.max = max;

    if (!tm.jquery.Utils.isEmpty(this.timeline))
    {
      this.timeline.setOptions({ min: min, max: max});
    }
  },

  getGroupHeaderDisplayProvider: function()
  {
    return this.groupHeaderDisplayProvider;
  },

  /**
   * @returns {Element|null}
   */
  getScrollableElement: function()
  {
    return this.scrollableElement;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
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
  }
});

