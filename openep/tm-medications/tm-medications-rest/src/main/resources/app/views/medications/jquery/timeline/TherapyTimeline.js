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
  timelineReadyFunction: null,
  reloadTimelinesFunction: null,
  intervalStart: null,
  intervalEnd: null,
  /** privates */
  options: null,
  displayProvider: null,
  therapyActions: null,
  therapyTimelineRows: null, //[TherapyTimelineRowDto]
  touchInProgress: null,
  longTouchPerformed: null,
  /** privates: components */
  timeline: null,
  timelineContainer: null,
  headerComponents: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.therapyTimelineRows = [];
    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"));
    this.therapyActions = new app.views.medications.TherapyActions({view: config.view});
    this.displayProvider = new app.views.medications.TherapyDisplayProvider({view: this.view});

    var now = new Date();
    this.options = {
      "width": "100%",
      locale: this.view.getViewLanguage(),
      groupsOnRight: false,
      groupsWidth: "402px",
      axisOnTop: true,
      eventMarginAxis: 0,
      showMajorLabels: true,
      showMinorLabels: false,
      moveable: true,
      max: this.intervalEnd,
      min: this.intervalStart,
      zoomable: true,
      zoomMax: 500000000,
      zoomMin: 1000000,
      stackEvents: false,
      animate: false,
      animateZoom: false,
      start: new Date(now.getTime() - 12 * 60 * 60 * 1000),      //24 hours
      end: new Date(now.getTime() + 24 * 60 * 60 * 1000),       //12 hours
      groupsOrder: false
    };
  },

  /** private methods */
  _buildGui: function(timelineData)
  {
    var self = this;
    this.removeAll(true);
    this.timelineContainer = new tm.jquery.Container({
      flex: 1,
      scrollable: "visible"
    });
    this.add(this.timelineContainer);
    this.timelineContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      self._paintTimeline(component, timelineData);
    });
    this.repaint();
  },

  _paintTimeline: function(paintToComponent, timelineData)
  {
    var self = this;
    this.timeline = new links.Timeline(paintToComponent.getDom());

    tm.views.medications.TherapyTimelineUtils.overrideTimelineOnMouseWheel(this.timeline);
    tm.views.medications.TherapyTimelineUtils.overrideTimelineReflowItems(this.timeline);
    if (this.view.getOptimizeForPerformance())
    {
      tm.views.medications.TherapyTimelineUtils.overrideTimelineOnMouseMove(this.timeline);
    }

    //timeline events
    links.Timeline.addEventListener(self.timeline.dom.content, "mouseup", function(event)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
      var target = links.Timeline.getTarget(event);
      self.timeline.selectItem(self.timeline.getItemIndex(target));
      if (event.button == 0)
      {
        self._handleLeftClickOnElement();
      }
      if (event.button == 2)
      {
        self._handleRightClickOnElement();
      }
    });

    this.touchInProgress = false;
    this.longTouchPerformed = false;
    links.Timeline.addEventListener(self.timeline.dom.content, "touchstart", function(event)
    {
      var target = links.Timeline.getTarget(event);
      self.timeline.selectItem(self.timeline.getItemIndex(target));
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
      tm.jquery.ComponentUtils.hideAllTooltips(self.view);
      self.touchInProgress = true;
      self.longTouchPerformed = false;
      setTimeout(function()
      {
        if (self.touchInProgress)
        {
          self.longTouchPerformed = true;
          self._handleRightClickOnElement();
        }
      }, 500);
    });

    links.Timeline.addEventListener(self.timeline.dom.content, "touchend", function(event)
    {
      self.touchInProgress = false;

      if (!self.longTouchPerformed)
      {
        var target = links.Timeline.getTarget(event);
        self.timeline.selectItem(self.timeline.getItemIndex(target));
        self._handleLeftClickOnElement();
      }
      self.longTouchPerformed = false;

    });
    links.events.addListener(self.timeline, 'ready', function()
    {
      self.timeline.draw(timelineData, self.options);
      self._repaintHeaders();
      $(".timeline-frame").parent().find(".ui-corner-all").removeClass("ui-corner-all");
      self.timelineReadyFunction();
      self.onTimelineRangeChange();
    });
    links.events.addListener(self.timeline, 'changed', function()
    {
      self._repaintHeaders();
    });
    links.events.addListener(self.timeline, 'rangechange', function()
    {
      self.onTimelineRangeChange();
    });
    links.events.addListener(self.timeline, 'rangechanged', function()
    {
      self._repaintHeaders();
    });
    links.events.addListener(self.timeline, 'timechanged', function()
    {
      self._repaintHeaders();
    });
  },

  onTimelineRangeChange: function()
  {
    var range = this.timeline.getVisibleChartRange();
    this.timelineRangeChangedFunction(range.start, range.end);
  },

  _repaintHeaders: function()
  {
    var self = this;
    setTimeout(
        function()
        {
          if (self.therapyTimelineRows) // [TherapyTimelineRowDto.java]
          {
            for (var i = 0; i < self.therapyTimelineRows.length; i++)
            {
              self._renderGroupContent(self.therapyTimelineRows[i], i);
            }
          }
        }, 50);
  },

  _buildTimeline: function()
  {
    this.headerComponents = [];

    var timelineElements = [];

    var workdayInterval = tm.views.medications.MedicationTimingUtils.getWorkdayInterval(this.view.getRoundsInterval(), new Date());

    for (var i = 0; i < this.therapyTimelineRows.length; i++)  // [TherapyTimelineRowDto.java]
    {
      var therapyTimeline = this.therapyTimelineRows[i];
      var therapy = therapyTimeline.therapy;    // [TherapyDto.java]
      var therapyId = therapyTimeline.therapyId;
      var groupContentContainer = this._getGroupContainer(therapyTimeline);
      groupContentContainer.doRender();
      var groupContentHtml = this._getGroupContentHtml(i, groupContentContainer);

      //paint workday interval
      if (workdayInterval != null)
      {
        timelineElements.push(
            this._buildTimelineElement(workdayInterval.start, workdayInterval.end, null, groupContentHtml, null, null, null, "workday-interval"));
      }

      // add an empty task to hold row height when no other tasks exist
      timelineElements.push(
          this._buildTimelineElement(this.options.min, this.options.max, null, groupContentHtml, null, null, null, "empty-task"));

      for (var j = 0; j < therapyTimeline.administrations.length; j++)
      {
        var administration = therapyTimeline.administrations[j];  // [AdministrationDto.java]
        var administrationTimestamp = administration.administrationTime ? administration.administrationTime : administration.plannedTime;

        var orderState = this._getOrderStateString(administration);
        var taskId = this._getTaskId(administration);
        var taskStateDisplay = '<div class="task ' + this._getAdministrationClass(administration) + '" id="' + taskId + '">' + orderState + '</div>';

        if (new Date(administrationTimestamp).getTime() > this.options.min.getTime())
        {
          timelineElements.push(
              this._buildTimelineElement(new Date(administrationTimestamp), null, taskStateDisplay, groupContentHtml, therapy, therapyId, administration, null));
        }
      }

      //draw duration lines for infusions
      if (therapy.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX)
      {
        this._addInfusionDurationElements(timelineElements, therapyTimeline, groupContentHtml);
      }

      //draw warning lines for "when needed" therapies
      if (therapy.whenNeeded)
      {
        this._addWhenNeededElements(timelineElements, therapyTimeline, groupContentHtml);
      }
    }
    if (this.timeline)
    {
      var visibleChartRange = this.timeline.getVisibleChartRange();
      this.options.start = visibleChartRange.start;
      this.options.end = visibleChartRange.end;
      this.timeline.draw(timelineElements, this.options);
      this._repaintHeaders();
    }
    else
    {
      this._buildGui(timelineElements);
    }
  },

  _addInfusionDurationElements: function(timelineElements, therapyTimeline, groupContentHtml)
  {
    var enums = app.views.medications.TherapyEnums;
    var therapy = therapyTimeline.therapy;  // [TherapyDto.java]

    var administrationIntervals = [];
    var hasStart = false;
    var therapyWithoutStartOrEndAdministration = true;

    therapyTimeline.administrations.sort(
        function(a, b)
        {
          var timeMillisA = new Date(a.plannedTime).getTime();
          var timeMillisB = new Date(b.plannedTime).getTime();
          return timeMillisA > timeMillisB;
        });
    for (var j = 0; j < therapyTimeline.administrations.length; j++)
    {
      var administration = therapyTimeline.administrations[j];  // [AdministrationDto.java]

      if (administration.administrationType != enums.administrationTypeEnum.INFUSION_SET_CHANGE)
      {
        if (therapy.continuousInfusion)
        {
          if (administration.administrationType == enums.administrationTypeEnum.START)
          {
            therapyWithoutStartOrEndAdministration = false;
            hasStart = true;
            var administrationIntervalS = {};
            administrationIntervalS.administrationStart = administration.administrationTime
                ? new Date(administration.administrationTime)
                : new Date(administration.plannedTime);
            administrationIntervalS.administrationEnd = this.options.max; //if end exists it will be overwritten later
            administrationIntervals.push(administrationIntervalS);
          }
          if (administration.administrationType == enums.administrationTypeEnum.STOP)
          {
            therapyWithoutStartOrEndAdministration = false;
            var administrationEnd = administration.administrationTime ? new Date(administration.administrationTime) : new Date(administration.plannedTime);
            var administrationIntervalE = hasStart ? administrationIntervals[(administrationIntervals.length - 1)] : {};
            administrationIntervalE.administrationEnd = administrationEnd;

            if (hasStart === false)
            {
              administrationIntervalE.administrationStart = this.options.min;
              administrationIntervals.push(administrationIntervalE);
            }
            hasStart = false;
          }
          if (administration.administeredDose &&
              (administration.administrationType == enums.administrationTypeEnum.START ||
              administration.administrationType == enums.administrationTypeEnum.ADJUST_INFUSION))
          {
            var administrationTime = new Date(administration.administrationTime);
            if (administrationTime < new Date(new Date().getTime() - 2 * 60 * 60 * 1000)) // if at least two hours old
            {
              var displayRate = tm.views.medications.MedicationUtils.buildAdministeredDoseDisplayString(administration, true);

              timelineElements.push(
                  this._buildTimelineElement(administrationTime, null, displayRate, groupContentHtml, null, null, null, "infusion-rate-label"));
            }
          }
        }
        else
        {
          if (therapy.doseElement && therapy.doseElement.duration)
          {
            var administrationTimestamp = administration.administrationTime ? administration.administrationTime : administration.plannedTime;
            var startAdministration = new Date(administrationTimestamp);
            var stopAdministration = new Date(startAdministration.getTime() + therapy.doseElement.duration * 60 * 1000); //therapy.doseElement.duration in minutes
            timelineElements.push(
                this._buildTimelineElement(startAdministration, stopAdministration, null, groupContentHtml, null, null, null, "duration-line"));
          }
        }
      }
    }
    if (therapy.continuousInfusion)
    {
      if (therapyWithoutStartOrEndAdministration)
      {
        var therapyStart = new Date(therapy.start.data);
        var administrationIntervalInfinite = {
          administrationStart: therapyStart > this.options.min && therapyTimeline.administrations.length == 0 ? therapyStart : this.options.min,
          administrationEnd: this.options.max
        };
        administrationIntervals.push(administrationIntervalInfinite);
      }
      for (var k = 0; k < administrationIntervals.length; k++)
      {
        timelineElements.push(
            this._buildTimelineElement(
                administrationIntervals[k].administrationStart,
                administrationIntervals[k].administrationEnd,
                null,
                groupContentHtml,
                null, null, null,
                "duration-line"));
      }
      if (therapyTimeline.infusionRateDisplay)
      {
        timelineElements.push(
            this._buildTimelineElement(new Date(), null, therapyTimeline.infusionRateDisplay, groupContentHtml, null, null, null, "infusion-rate-label"));
      }
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
      if (lastAdministrationTime == null || administrationStart.getTime() > lastAdministrationTime.getTime())
      {
        lastAdministrationTime = administrationStart;
      }
    }

    if (lastAdministrationTime)
    {
      var nextAllowedAdministrationTime =
          tm.views.medications.MedicationTimingUtils.getNextAllowedAdministrationTimeForPRN(therapy.dosingFrequency, lastAdministrationTime);
      timelineElements.push(
          this._buildTimelineElement(lastAdministrationTime, nextAllowedAdministrationTime, null, groupContentHtml, null, null, null, "when-needed-line"));
    }
  },

  _getAdministrationClass: function(administration)
  {
    var administrationClass = '';
    if (administration.taskId == null)
    {
      administrationClass = "ADDITIONAL";
    }
    else
    {
      administrationClass = administration.administrationStatus
    }
    if (administration.infusionSetChangeEnum)
    {
      administrationClass += " " + administration.infusionSetChangeEnum;
    }
    return administrationClass;
  },

  _getOrderStateString: function(administration)
  {
    var enums = app.views.medications.TherapyEnums;
    if (administration.administrationType == enums.administrationTypeEnum.INFUSION_SET_CHANGE)
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

  _createPopupMenu: function(selectedItem)
  {
    var self = this;
    var popupMenu = this.view.getAppFactory().createPopupMenu();
    var isAdministrationConfirmed = this._isAdministrationConfirmed(selectedItem.administration);

    if (!isAdministrationConfirmed)
    {
      var moveMenuItem = new tm.jquery.MenuItem({
        text: this.view.getDictionary("move"),
        iconCls: 'icon-add-to-24',
        handler: function()
        {
          self._createRescheduleTasksContainer(selectedItem.therapyId, selectedItem.therapy, selectedItem.administration);
        }
      });
      popupMenu.addMenuItem(moveMenuItem);
    }
    else
    {
      var editMenuItem = new tm.jquery.MenuItem({
        text: this.view.getDictionary("edit"),
        iconCls: 'icon-edit',
        handler: function()
        {
          self._createEditAdministrationContainer(selectedItem.therapyId, selectedItem.therapy, selectedItem.administration);
        }
      });
      popupMenu.addMenuItem(editMenuItem);
    }
    if (isAdministrationConfirmed || (selectedItem.therapy && !selectedItem.therapy.continuousInfusion))
    {
      var deleteMenuItem = new tm.jquery.MenuItem({
        text: isAdministrationConfirmed ? this.view.getDictionary("cancel") : this.view.getDictionary("delete"),
        iconCls: 'icon-delete',
        handler: function()
        {
          self._openDeleteAdministrationDialog();
        }
      });
      popupMenu.addMenuItem(deleteMenuItem);
    }

    return popupMenu;
  },

  _isAdministrationConfirmed: function(administration)
  {
    return administration.administrationStatus == 'COMPLETED' ||
        administration.administrationStatus == 'COMPLETED_LATE' ||
        administration.administrationStatus == 'COMPLETED_EARLY' ||
        administration.administrationStatus == "FAILED";
  },

  //gorup content as html - without actions
  _getGroupContentHtml: function(index, groupContentContainer)
  {
    this.headerComponents.add(groupContentContainer);
    var html = '';
    html += '<div id="' + this._getGroupId(index) + '" class="timeline-index">';
    html += groupContentContainer.getRenderToElement().innerHTML;
    html += '</div>';
    return html;
  },

  _getGroupId: function(index)
  {
    return 'timeline-index-' + index + "_" + this.timelineIndex;
  },

  //group content as containers - with actions
  _renderGroupContent: function(therapyTimeline, index)
  {
    var element = $("#" + this._getGroupId(index))[0];
    if (!tm.jquery.Utils.isEmpty(element))
    {
      $(element).html("");
      var groupContainer = this._getGroupContainer(therapyTimeline);
      groupContainer.setRenderToElement(element);
      groupContainer.doRender();
    }
  },

  _isTherapyCancelledOrAborted: function(therapyTimeline)
  {
    var enums = app.views.medications.TherapyEnums;
    var therapyStatus = therapyTimeline.therapyStatus;
    return therapyStatus == enums.therapyStatusEnum.ABORTED || therapyStatus == enums.therapyStatusEnum.CANCELLED;
  },

  _getGroupContainer: function(therapyTimeline)      // [TherapyTimelineRowDto.java]
  {
    var therapy = therapyTimeline.therapy;
    var enums = app.views.medications.TherapyEnums;
    var appFactory = this.view.getAppFactory();
    var therapyShortDescription = therapy.formattedTherapyDisplay;

    var options = {
      background: {cls: tm.views.medications.MedicationUtils.getTherapyIcon(therapy)},
      layers: []
    };
    var statusIcon = this.displayProvider.getStatusIcon(therapyTimeline.therapyStatus);

    if (therapy != null && (therapy.linkFromTherapy || therapy.linkToTherapy))
    {
      var link = therapy.linkFromTherapy ? therapy.linkFromTherapy : therapy.linkToTherapy;
      if (link.length <= 2)
      {
        options.layers.push({hpos: "left", vpos: "bottom", cls: "icon_link", html: link});
      }
    }
    if (therapyTimeline.modifiedFromLastReview)
    {
      options.layers.push({hpos: "left", vpos: "top", cls: "icon_changed"});
    }
    if (therapyTimeline.showConsecutiveDay)
    {
      options.layers.push({hpos: "right", vpos: "top", cls: "icon_day_number", html: therapyTimeline.consecutiveDay});
    }
    if (therapy.criticalWarnings.length > 0)
    {
      options.layers.push({hpos: "center", vpos: "center", cls: "icon_warning"});
    }

    options.layers.push({hpos: "right", vpos: "bottom", cls: statusIcon});
    var iconContainer = new tm.jquery.Container({
      cursor: "pointer",
      margin: "3 4 3 8",
      width: 48,
      height: 48,
      html: appFactory.createLayersContainerHtml(options)
    });

    var self = this;
    iconContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      var tooltip = tm.views.medications.MedicationUtils.createTherapyDetailsCardTooltip(
          therapyTimeline,
          null,
          self.view,
          {
            startMillis: self.intervalStart.getTime(),
            endMillis: self.intervalEnd.getTime()
          }
      );
      tooltip.setTrigger("manual");
      iconContainer.setTooltip(tooltip);
      setTimeout(function()
      {
        tooltip.show();
      }, 200);
    });

    var therapyStatusDisplayClass = (this._isTherapyCancelledOrAborted(therapyTimeline) || therapyTimeline.therapyStatus == enums.therapyStatusEnum.SUSPENDED) ? 'aborted' : 'normal';
    var groupContainer = new tm.jquery.Container({
      cls: therapyStatusDisplayClass + " text-unselectable",
      layout: new tm.jquery.HFlexboxLayout({gap: 0})
    });

    var contentContainer = new tm.jquery.Container({
      width: 310,
      html: therapyShortDescription,
      cls: 'TherapyDescription'
    });
    var actionsContainer = new tm.jquery.Container({
      width: 16, height: 16,
      cls: 'menu-icon pointer-cursor'
    });

    if (therapyTimeline.therapyEndsBeforeNextRounds)
    {
      groupContainer.setStyle('border-right: 2px solid grey;');
    }
    var isTherapyTaggedForPrescription = tm.views.medications.MedicationUtils.isTherapyTaggedForPrescription(therapyTimeline.therapy.tags);
    this._setContainerActions(actionsContainer, therapyTimeline, isTherapyTaggedForPrescription);

    var html = '';
    if (isTherapyTaggedForPrescription)
    {
      html = '<div style="display: inline-block; margin-right: 1px;" class="icon_prescription"></div>';
    }

    var tagIconContainer = new tm.jquery.Container({
      margin: "1 1 3 1",
      width: 16,
      height: 16,
      html: html
    });

    groupContainer.add(iconContainer);
    groupContainer.add(contentContainer);
    groupContainer.add(tagIconContainer);
    groupContainer.add(actionsContainer);
    return groupContainer;
  },

  //_setIconActions: function(iconContainer, therapyTimeline)
  //{
  //  var self = this;
  //  iconContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
  //  {
  //    tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
  //
  //  });
  //},

  _setContainerActions: function(actionsContainer, therapyTimeline, isTherapyTaggedForPrescription)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var therapy = therapyTimeline.therapy;
    var popupMenu = this.view.getAppFactory().createPopupMenu();

    //therapy actions
    if (therapy != null && !this._isTherapyCancelledOrAborted(therapyTimeline) && this.view.isEditAllowed())
    {
      if (therapy.medicationOrderFormType != "COMPLEX")
      {
        var editTherapyItem = new tm.jquery.MenuItem({
          text: self.view.getDictionary("edit"),
          /*iconCls: 'icon-edit', */
          handler: function()
          {
            if (therapy.medication.id != null)
            {
              self.view.showEditSimpleTherapyDialog(therapy, false);
            }
            else
            {
              self.view.openUniversalSimpleTherapyDialog(therapy);
            }
          }});
        popupMenu.addMenuItem(editTherapyItem);
      }
      else if (therapy.medicationOrderFormType == "COMPLEX")
      {
        var adjustSpeedItem = new tm.jquery.MenuItem({
          text: self.view.getDictionary("edit"),
          /*iconCls: 'icon-edit',*/
          handler: function()
          {
            if (therapy.ingredientsList[0].medication.id != null)
            {
              self.view.showEditComplexTherapyDialog(therapy, false);
            }
            else
            {
              self.view.openUniversalComplexTherapyDialog(therapy);
            }
          }});

        popupMenu.addMenuItem(adjustSpeedItem);
      }
      var stopTherapyItem = new tm.jquery.MenuItem({
        text: this.view.getDictionary("abort.therapy"),
        /* iconCls: 'icon-delete',  */
        handler: function()
        {
          self.view.showLoaderMask();
          self.therapyActions.abortTherapy(therapy.compositionUid, therapy.ehrOrderName,
              function()
              {
                self.view.hideLoaderMask();
                self.reloadTimelinesFunction();
              },
              function()
              {
                self.view.hideLoaderMask();
                self._redrawTimeline();
              });
        }
      });
      popupMenu.addMenuItem(stopTherapyItem);

      var centralCaseExists = self.view.getCentralCaseData() && self.view.getCentralCaseData().centralCaseId;

      if (centralCaseExists)
      {
        if (isTherapyTaggedForPrescription)
        {
          var untagTherapyItem = new tm.jquery.MenuItem({
            text: this.view.getDictionary("untag.therapy.for.prescription"),
            /* iconCls: 'icon-delete',  */
            handler: function ()
            {
              self.view.untagTherapyForPrescription(
                  self.view.getPatientId(),
                  self.view.getCentralCaseData().centralCaseId,
                  therapy.compositionUid,
                  therapy.ehrOrderName);
            }
          });
          popupMenu.addMenuItem(untagTherapyItem);
        }
        else
        {
          var tagTherapyItem = new tm.jquery.MenuItem({
            text: this.view.getDictionary("tag.therapy.for.prescription"),
            /* iconCls: 'icon-delete',  */
            handler: function()
            {
              self.view.tagTherapyForPrescription(
                  self.view.getPatientId(),
                  self.view.getCentralCaseData().centralCaseId,
                  therapy.compositionUid,
                  therapy.ehrOrderName);
            }
          });
          popupMenu.addMenuItem(tagTherapyItem);
        }
      }


      if (therapyTimeline.therapyActionsAllowed)
      {
        if (therapyTimeline.therapyStatus == enums.therapyStatusEnum.SUSPENDED)
        {
          var reissueTherapyItem = new tm.jquery.MenuItem({
            text: this.view.getDictionary("reissue"),
            /* iconCls: 'icon-confirm', */
            handler: function()
            {
              self.view.showLoaderMask();
              self.therapyActions.reissueTherapy(therapy.compositionUid, therapy.ehrOrderName,
                  function()
                  {
                    self.view.hideLoaderMask();
                    self.reloadTimelinesFunction();
                  },
                  function()
                  {
                    self.view.hideLoaderMask();
                    self._redrawTimeline();
                  });
            }
          });
          popupMenu.addMenuItem(reissueTherapyItem);
        }
        else
        {
          var confirmTherapyItem = new tm.jquery.MenuItem({
            text: this.view.getDictionary("confirm"),
            /*iconCls: 'icon-confirm',  */
            handler: function()
            {
              self.view.showLoaderMask();
              self.therapyActions.reviewTherapy(therapy.compositionUid, therapy.ehrOrderName,
                  function()
                  {
                    self.view.hideLoaderMask();
                    self.reloadTimelinesFunction();
                  },
                  function()
                  {
                    self.view.hideLoaderMask();
                    self._redrawTimeline();
                  });
            }
          });
          popupMenu.addMenuItem(confirmTherapyItem);
        }
      }

      if (therapyTimeline.therapyStatus != app.views.medications.TherapyEnums.therapyStatusEnum.SUSPENDED)
      {
        var suspendTherapyItem = new tm.jquery.MenuItem({
          text: this.view.getDictionary("suspend"),
          /*  iconCls: 'icon-suspend',  */
          handler: function()
          {
            self.view.showLoaderMask();
            self.therapyActions.suspendTherapy(therapy.compositionUid, therapy.ehrOrderName,
                function()
                {
                  self.view.hideLoaderMask();
                  self.reloadTimelinesFunction();
                },
                function()
                {
                  self.view.hideLoaderMask();
                  self._redrawTimeline();
                });
          }
        });
        popupMenu.addMenuItem(suspendTherapyItem);
      }
    }
    if (therapy.medicationOrderFormType != enums.medicationOrderFormType.COMPLEX && therapy.medication.id != null)
    {
      var menuItemCopySimpleTherapy = new tm.jquery.MenuItem({text: self.view.getDictionary("copy"), /*iconCls: 'icon-copy', */
        handler: function()
        {
          self.view.showEditSimpleTherapyDialog(therapy, true);
        }});
      popupMenu.addMenuItem(menuItemCopySimpleTherapy);
    }
    else if (therapy.medicationOrderFormType == enums.medicationOrderFormType.COMPLEX && therapy.ingredientsList[0].medication.id != null)
    {
      var menuItemCopyComplexTherapy = new tm.jquery.MenuItem({text: self.view.getDictionary("copy"), /*iconCls: 'icon-copy', */
        handler: function()
        {
          self.view.showEditComplexTherapyDialog(therapy, true);
        }});
      popupMenu.addMenuItem(menuItemCopyComplexTherapy);
    }

    //administration/task actions
    if (!therapy.continuousInfusion)
    {
      var addTaskMenuItem = new tm.jquery.MenuItem({
        text: this.view.getDictionary("administration.schedule.additional"),
        iconCls: 'icon-add',
        handler: function()
        {
          var therapyDoseTypeEnum = self._getTherapyDoseTypeEnum(therapyTimeline);
          self._showTherapyAdministrationContainer(
              therapy,
              therapyTimeline.administrations,
              null,
              true,
              self.view.getDictionary("administration.schedule.additional"),
              therapyDoseTypeEnum,
              enums.administrationTypeEnum.START);
        }
      });
      var addAdministrationMenuItem = new tm.jquery.MenuItem({
        text: this.view.getDictionary("administration.apply.unplanned"),
        iconCls: 'icon-add',
        handler: function()
        {
          var therapyDoseTypeEnum = self._getTherapyDoseTypeEnum(therapyTimeline);
          self._showTherapyAdministrationContainer(
              therapy,
              therapyTimeline.administrations,
              null,
              false,
              self.view.getDictionary("administration.apply.unplanned"),
              therapyDoseTypeEnum,
              enums.administrationTypeEnum.START);
        }
      });

      popupMenu.addMenuItem(addTaskMenuItem);
      popupMenu.addMenuItem(addAdministrationMenuItem);
    }
    else
    {
      var adjustInfusionMenuItem = new tm.jquery.MenuItem({
        text: this.view.getDictionary("adjust.infusion.rate"),
        iconCls: 'icon-add',
        handler: function()
        {
          self._showTherapyAdministrationContainer(
              therapy,
              therapyTimeline.administrations,
              null,
              false,
              self.view.getDictionary("adjust.infusion.rate"),
              "RATE", enums.administrationTypeEnum.ADJUST_INFUSION);
        }
      });

      popupMenu.addMenuItem(adjustInfusionMenuItem);
    }

    if (therapy.medicationOrderFormType == enums.medicationOrderFormType.COMPLEX)
    {
      var infusionSetChangeMenuItem = new tm.jquery.MenuItem({
        text: this.view.getDictionary("infusion.set.change"),
        handler: function()
        {
          self._showTherapyAdministrationContainer(
              therapy,
              therapyTimeline.administrations,
              null,
              false,
              self.view.getDictionary("infusion.set.change"),
              null,
              enums.administrationTypeEnum.INFUSION_SET_CHANGE);
        }
      });
      popupMenu.addMenuItem(infusionSetChangeMenuItem);
    }

    actionsContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
      popupMenu.show(elementEvent);
    });
  },

  _getTherapyDoseTypeEnum: function(therapyTimeline)
  {
    if (therapyTimeline.therapy.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX
        && therapyTimeline.administrations.length > 0)
    {
      var firstAdministration = therapyTimeline.administrations[0];
      if (firstAdministration.plannedDose)
      {
        return firstAdministration.plannedDose.therapyDoseTypeEnum;
      }
      else
      {
        return firstAdministration.administeredDose.therapyDoseTypeEnum;
      }
    }
    else
    {
      return "QUANTITY";
    }
  },

  _handleRightClickOnElement: function()
  {
    var selectedItem = this._getSelectedItem();
    if (selectedItem && selectedItem.administration)
    {
      var $taskElement = $("#" + this._getTaskId(selectedItem.administration));
      var $offset = $taskElement.offset();
      var popupMenu = this._createPopupMenu(selectedItem);
      popupMenu.show($offset.left + $taskElement.width() + 5, $offset.top);
    }
  },

  _handleLeftClickOnElement: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var selectedItem = this._getSelectedItem();
    if (selectedItem && selectedItem.administration)
    {
      var administrationStatus = selectedItem.administration.administrationStatus;

      //task not finished
      if (administrationStatus == 'PLANNED' || administrationStatus == 'DUE' || administrationStatus == 'LATE')
      {
        if (selectedItem.therapy.continuousInfusion &&
            (selectedItem.administration == null || selectedItem.administration.administrationType == enums.administrationTypeEnum.START))
        {
          this._showTherapyAdministrationContainer(
              selectedItem.therapy,
              null,
              selectedItem.administration,
              false,
              this.view.getDictionary('administration'),
              "RATE", enums.administrationTypeEnum.START);
        }
        else
        {
          this._showTherapyAdministrationContainer(
              selectedItem.therapy,
              null,
              selectedItem.administration,
              false,
              this.view.getDictionary('administration'));
        }
      }
      else
      {
        this._showAdministrationDetailsCard(selectedItem.therapy, selectedItem.therapyId, selectedItem.administration);
      }
    }
  },

  _openDeleteAdministrationDialog: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var selectedItem = this._getSelectedItem();
    if (selectedItem && selectedItem.administration)
    {
      var isAdministrationConfirmed = this._isAdministrationConfirmed(selectedItem.administration);

      var deleteCommentField = new tm.jquery.TextField({width: 370});
      var deleteContainer = new app.views.common.containers.AppDataEntryContainer({
        layout: tm.jquery.HFlexboxLayout.create('start', 'center', 5),
        startProcessOnEnter: true,
        margin: 7,
        processResultData: function(resultCallback)
        {
          var validationForm = new tm.jquery.Form({
            onValidationSuccess: function()
            {
              if (isAdministrationConfirmed)
              {
                self._deleteAdministration(resultCallback, deleteCommentField.getValue(), selectedItem.administration);
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
            requiredFieldValidatorErrorMessage: self.view.getDictionary("field.value.is.required")
          });
          if (!isAdministrationConfirmed)
          {
            validationForm.addFormField(new tm.jquery.FormField({component: deleteCommentField, required: true}));
          }
          validationForm.submit();
        }
      });
      deleteContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', self.view.getDictionary("delete.reason")));
      deleteContainer.add(deleteCommentField);
      deleteContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
      {
        setTimeout(function()
        {
          deleteCommentField.focus();
        }, 300);
      });

      var administrationTimestamp = selectedItem.administration.administrationTime ? selectedItem.administration.administrationTime : selectedItem.administration.plannedTime;
      var administrationTimeDisplay = this.view.getDisplayableValue(new Date(administrationTimestamp), "short.time");
      var title = isAdministrationConfirmed ? this.view.getDictionary("delete.administration.at") + " " + administrationTimeDisplay :
          this.view.getDictionary("delete.scheduled.administration.at") + " " + administrationTimeDisplay;
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
      therapyAdministrationDialog.show();
    }
  },

  _deleteTask: function(resultCallback, comment, administration)
  {
    var params = {
      taskId: administration.taskId,
      comment: comment
    };

    var deleteUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DELETE_TASK;
    this.view.loadPostViewData(deleteUrl, params, null,
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: true}));
        },
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: false}));
        },
        true);
  },

  _deleteAdministration: function(resultCallback, comment, administration)
  {
    var params = {
      patientId: this.patientId,
      administrationId: administration.administrationId,
      taskId: administration.taskId,
      comment: comment
    };

    var deleteUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DELETE_ADMINISTRATION;
    this.view.loadPostViewData(deleteUrl, params, null,
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: true}));
        },
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: false}));
        },
        true);
  },

  _createEditAdministrationContainer: function(therapyId, therapy, administration)
  {
    this._showTherapyAdministrationContainer(
        therapy,
        null,
        administration,
        false,
        this.view.getDictionary('edit'),
        null,
        administration.administrationType,
        true);
  },

  _redrawTimeline: function()
  {
    this.timeline.redraw();
    this._repaintHeaders();
  },

  _createRescheduleTasksContainer: function(therapyId, therapy, administration)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var rescheduleContainer = new tm.views.medications.timeline.RescheduleTasksContainer({
      view: this.view,
      startProcessOnEnter: true,
      administration: administration,
      therapyId: therapyId,
      therapy: therapy
    });
    var rescheduleContainerDialog = this.view.getAppFactory().createDataEntryDialog(
        this.view.getDictionary('move.administration'),
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
        300,
        (therapy.dosingFrequency && therapy.dosingFrequency.type == enums.dosingFrequencyTypeEnum.BETWEEN_DOSES) ? 200 : 150
    );

    rescheduleContainerDialog.show();
  },

  _showTherapyAdministrationContainer: function(therapy, administrations, administration, createNewTask, containerTitle, therapyDoseTypeEnum, administrationType, editMode)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var showAdministrationContainer = true;
    if (!editMode && !createNewTask && administration &&
        administration.plannedTime && new Date(administration.plannedTime) > new Date())
    {
      var futureAdministrationIsNextInLine = this._isFutureAdministrationNextInLine(therapy, administration);
      if (!futureAdministrationIsNextInLine)
      {
        var message = this.view.getDictionary('therapy.administration.not.next.in.line.warning');
        this.view.getAppFactory().createWarningSystemDialog(message, 380, 140).show();
        showAdministrationContainer = false;
      }
    }

    if (showAdministrationContainer)
    {
      var therapyAdministrationContainer = new tm.views.medications.timeline.TherapyAdministrationContainer({
        view: this.view,
        startProcessOnEnter: true,
        scrollable: 'vertical',
        displayProvider: this.displayProvider,
        therapy: therapy,
        administrations: administrations,
        administration: administration,
        patientId: this.patientId,
        createNewTask: createNewTask,
        therapyDoseTypeEnum: therapyDoseTypeEnum,
        administrationType: administrationType,
        editMode: editMode
      });
      var therapyAdministrationDialog = appFactory.createDataEntryDialog(
          containerTitle,
          null,
          therapyAdministrationContainer,
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
          470,
          415
      );
      therapyAdministrationDialog.show();
    }
  },

  _isFutureAdministrationNextInLine: function(therapy, newAdministration)
  {
    var plannedNewAdministrationTime = new Date(newAdministration.plannedTime);
    for (var i = 0; i < this.therapyTimelineRows.length; i++)
    {
      var therapyTimeline = this.therapyTimelineRows[i];  // [TherapyTimelineRowDto.java]
      if (therapyTimeline.therapy == therapy)
      {
        for (var j = 0; j < therapyTimeline.administrations.length; j++)
        {
          var administration = therapyTimeline.administrations[j];  // [AdministrationDto.java]
          if (!administration.administrationTime)
          {
            var plannedAdministrationTime = new Date(administration.plannedTime);
            if (plannedAdministrationTime < plannedNewAdministrationTime)
            {
              if (administration != newAdministration && plannedAdministrationTime > new Date())
              {
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  },

  _showAdministrationDetailsCard: function(latestTherapy, latestTherapyId, administration)
  {
    var self = this;
    if (administration.therapyId == latestTherapyId)
    {
      this._openAdministrationDetailsCardDialog(latestTherapy, latestTherapy.formattedTherapyDisplay, administration);
    }
    else
    {
      var url = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPY_FORMATTED_DISPLAY;
      var params = {
        patientId: this.view.getPatientId(),
        therapyId: administration.therapyId
      };

      this.view.loadViewData(url, params, null, function(formattedTherapyDisplay)
      {
        self._openAdministrationDetailsCardDialog(latestTherapy, formattedTherapyDisplay, administration);
      });
    }
  },

  _openAdministrationDetailsCardDialog: function(latestTherapy, therapyFormattedDisplay, administration)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var therapyAdministrationCard = new tm.views.medications.timeline.TherapyAdministrationCard({
      view: this.view,
      displayProvider: this.displayProvider,
      latestTherapy: latestTherapy,
      therapyFormattedDisplay: therapyFormattedDisplay,
      administration: administration
    });
    var therapyAdministrationCardDialog = appFactory.createDefaultPopoverTooltip(
        this.view.getDictionary('administration'),
        null,
        therapyAdministrationCard
    );
    therapyAdministrationCardDialog.setPlacement("center");
    therapyAdministrationCardDialog.setTrigger("manual");
    this.setTooltip(therapyAdministrationCardDialog);

    setTimeout(function()
    {
      therapyAdministrationCardDialog.show();
      self._repaintHeaders();
    }, 10);
    this.timeline.redraw();
  },

  _getSelectedItem: function()
  {
    var selection = this.timeline.getSelection();
    if (selection.length)
    {
      if (selection[0].row != undefined)
      {
        var selectedItemIndex = selection[0].row;
        return this.timeline.getItem(selectedItemIndex);
      }
    }
    return null;
  },

  //JSON
  _buildTimelineElement: function(from, to, taskContent, groupContent, therapy, therapyId, administration, className)
  {
    return {
      start: from,
      end: to,
      content: !taskContent ? '' : taskContent,
      group: groupContent,
      therapy: therapy,
      administration: administration,
      therapyId: therapyId,
      className: className
    };
  },

  /** public methods */
  setTherapyTimelineData: function(therapyTimelineRows)
  {
    this.therapyTimelineRows = therapyTimelineRows;
    this._buildTimeline();
  },

  setVisibleRange: function(start, end)
  {
    if (this.timeline)
    {
      var range = this.timeline.getVisibleChartRange();
      if (range.start != start || range.end != end)
      {
        this.timeline.setVisibleChartRange(start, end);
      }
    }
  },

  clear: function()
  {
    this.therapyTimelineRows.removeAll();
    this._buildTimeline();
  },

  redrawTimeline: function()
  {
    this._redrawTimeline();
  }
});

