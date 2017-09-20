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
Class.define('tm.views.medications.pharmacists.ReviewView', 'tm.jquery.Container', {
  cls: "pharmacists-review",

  /** public members  */
  view: null,
  activeTherapyData: null,
  activeReportsData: null,
  lastTaskChangeTimestamp: null,
  therapyDataSorter: null,

  therapyDataLoadedCallback: null,

  /* config options */
  therapyListColumnWidth: null,
  routesFilter: null,
  customGroupsFilter: null,
  timelineStart: null,
  timelineEnd: null,
  selectedShownTherapies: null,
  hidePastTherapies: null,
  previousSelectedButtonId: null,

  /* private members */
  _therapyColumn: null,
  _reportsColumn: null,

  _activeDailyContainer: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.therapyListColumnWidth = this.getConfigValue("therapyListColumnWidth", 425);
    this.therapyDataSorter = this.getConfigValue("therapyDataSorter", new app.views.medications.TherapyDataSorter({
      view: this.view,
      getTherapyDtoFromDataObject: function (object)
      {
        return !tm.jquery.Utils.isEmpty(object) ? object : {};
      }
    }));

    this._buildGui();
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    var therapyColumn = new tm.views.medications.pharmacists.ColumnContainer({
      columnTitle: this.getView().getDictionary("therapies"),
      cls: "therapy-column",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, this.therapyListColumnWidth + "px")
    });

    var reportsColumn = new tm.views.medications.pharmacists.ColumnContainer({
      columnTitle: this.getView().getDictionary("pharmacists.reviews"),
      cls: "reports-column",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this.add(therapyColumn);
    this.add(reportsColumn);

    this._therapyColumn = therapyColumn;
    this._reportsColumn = reportsColumn;
  },

  _loadTherapyData: function (therapySortTypeEnum, callback)
  {
    this.getView().getLocalLogger().debug("Calling load therapy data.");
    var self = this;
    var findTherapyFlowDataUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_PHARMACIST_TIMELINE;

    this.getView().showLoaderMask();
    var patientId = this.getPatientId();

    this._setTimelineInterval();
    var params = {
      patientId: patientId,
      patientData: JSON.stringify(this.view.getPatientData()),
      timelineInterval: JSON.stringify({startMillis: this.timelineStart.getTime(), endMillis: this.timelineEnd.getTime()}),
      roundsInterval: JSON.stringify(this.getRoundsInterval()),
      therapySortTypeEnum: therapySortTypeEnum,
      hidePastTherapies: this.hidePastTherapies
    };

    this.getView().loadViewData(findTherapyFlowDataUrl, params, null, function (data)
        {
          self.getView().hideLoaderMask();
          if (patientId == self.getPatientId())
          {
            data.therapyRows.forEach(function(flowRowObject)
            {
              flowRowObject.therapy = app.views.medications.common.TherapyJsonConverter.convert(flowRowObject.therapy);
            });

            self.getView().getLocalLogger().info("Therapy data loaded.");

            self.setActiveTherapyData(data.therapyRows);
            if (!tm.jquery.Utils.isEmpty(self.therapyDataLoadedCallback)) self.therapyDataLoadedCallback(data.therapyRows);
            callback();
          }
          self.view.updateCurrentBnfMaximumSum();
        },
        function()
        {
          self.view.hideLoaderMask();
        });
  },

  _loadReportData: function (callback)
  {
    var yesterday = CurrentTime.get();
    yesterday.setDate(yesterday.getDate() - 1);
    yesterday.setHours(0, 0, 0, 0);

    var self = this;
    var loadReviewsUrl = this.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_PHARMACIST_REVIEWS;

    this.getView().showLoaderMask();

    var patientId = this.view.getPatientId();
    var params = {
      patientId: patientId,
      fromDate: JSON.stringify(yesterday),
      language: this.getView().getViewLanguage()
    };

    this.getView().loadViewData(loadReviewsUrl, params, null, function (data)
    {
      self.getView().hideLoaderMask();

      if (patientId == self.view.getPatientId())
      {
        var reviewData = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.pharmacistReviews) ?
            data.pharmacistReviews : [];
        var taskTimestamp = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.lastTaskChangeTimestamp) ?
            new Date(data.lastTaskChangeTimestamp) : null;

        var reviews = reviewData.map(function(item)
        {
          return tm.views.medications.pharmacists.dto.PharmacistMedicationReview.fromJson(item);
        });

        self.setActiveReportsData(reviews);
        self.setLastTaskChangeTimestamp(taskTimestamp);

        callback();
      }
    });
  },

  _fillTherapyListContainer: function ()
  {
    this.getView().getLocalLogger().debug("Calling _fillTherapyListContainer.");
    var listContainer = this.getTherapyListContainer();
    var data = this.getActiveTherapyData();
    var routesFilterIsSet = this.routesFilter != null && this.routesFilter.length > 0;
    var customGroupsFilterIsSet = this.customGroupsFilter != null && this.customGroupsFilter.length > 0;
    var therapyRows = tm.jquery.Utils.isEmpty(data) ? [] : data; // clone or create a new one

    if (routesFilterIsSet || customGroupsFilterIsSet)
    {
      var routeFilter = this.routesFilter;
      var customGroupFilter = this.customGroupsFilter;

      therapyRows = jQuery.grep(therapyRows, function(therapy){
        return (!routesFilterIsSet || routeFilter.contains(therapy.route))  &&
            (!customGroupsFilterIsSet || customGroupFilter.contains(therapy.customGroup));
      });
    }

    var groupedData = this.getTherapyDataSorter().group(this.getGroupField(), therapyRows);

    listContainer.removeAll();

    if (tm.jquery.Utils.isEmpty(groupedData))
    {
      this._addTherapyContainers(listContainer, therapyRows);
    }
    else
    {
      this._addGroupedTherapyContainers(groupedData);
    }

    if (therapyRows.isEmpty())
    {
      listContainer.add(this.view.getNoTherapiesField());
    }

    if (listContainer.isRendered()) listContainer.repaint();

    listContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_SCROLL, function()
    {
      tm.jquery.ComponentUtils.hideAllTooltips();
    });

    this.getView().getLocalLogger().debug("Finished executing _fillTherapyListContainer.");
  },

  _addTherapyContainers: function (container, therapyData)
  {
    var self = this;
    var view = this.getView();
    var listContainer = this.getTherapyListContainer();

    therapyData.forEach(function (rowData)
    {
      var therapyContainer = app.views.medications.pharmacists.TherapyContainer.forReviewView({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        view: view,
        scrollableElement: listContainer.getDom(),
        data: !tm.jquery.Utils.isEmpty(rowData) ? rowData : null
      });
      therapyContainer.getToolbar().setAddToIconClickCallback(function (container)
      {
        self.addTherapyToReview(container);
      });
      therapyContainer.getToolbar().setTasksChangedCallback(function()
      {
        self.refreshTherapies();
      });
      container.add(therapyContainer);
    });
  },

  _addGroupedTherapyContainers: function (groupedData)
  {
    var self = this;
    var data = this.getActiveTherapyData();
    var listContainer = this.getTherapyListContainer();

    if (!tm.jquery.Utils.isEmpty(groupedData))
    {
      groupedData.forEach(function (group)
      {
        var panel = new tm.jquery.Panel({
          collapsed: false,
          showHeader: !tm.jquery.Utils.isEmpty(group.key),
          showFooter: false,
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
        });

        if (!tm.jquery.Utils.isEmpty(group.key))
        {
          var panelHeader = panel.getHeader();
          panelHeader.setCls('grouping-panel text-unselectable');
          panelHeader.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));
          var panelTitlePane = new tm.jquery.Component({
            padding: "3 0 3 0",
            cursor: "pointer",
            flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
            html: '<span class="TextData">' + group.key + '</span>'
          });

          panelHeader.add(panelTitlePane);
          panel.bindToggleEvent([panelTitlePane]);
        }

        var panelContent = panel.getContent();
        panelContent.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
        panelContent.setScrollable('visible');

        self._addTherapyContainers(panelContent, group.elements);
        listContainer.add(panel);
      });
    }
  },

  /* Recursive method that goes over the therapies in the therapy list and calls the callback for each.
   Will break when the callback method returns true! */
  _processTherapies: function (callback, container)
  {
    container = tm.jquery.Utils.isEmpty(container) ? this.getTherapyListContainer() : container;

    var components = container.getComponents();

    for (var idx = 0; idx < components.length; idx++)
    {
      if (components[idx] instanceof tm.jquery.Panel)
      {
        this._processTherapies(callback, components[idx].getContent());
      }
      else if (components[idx] instanceof app.views.medications.pharmacists.TherapyContainer)
      {
        if (callback(components[idx]) === true) break;
      }
    }

  },

  _processTherapies: function (callback, container)
  {
    container = tm.jquery.Utils.isEmpty(container) ? this.getTherapyListContainer() : container;

    var components = container.getComponents();

    for (var idx = 0; idx < components.length; idx++)
    {
      if (components[idx] instanceof tm.jquery.Panel)
      {
        this._processTherapies(callback, components[idx].getContent());
      }
      else if (components[idx] instanceof app.views.medications.pharmacists.TherapyContainer)
      {
        if (callback(components[idx]) === true) break;
      }
    }

  },

  _fillDailyContainersListContainer: function ()
  {
    this.getView().getLocalLogger().debug("Calling _fillDailyContainersListContainer.");
    var self = this;
    var data = this.getActiveReportsData();
    var yesterday = CurrentTime.get();
    var today = CurrentTime.get();
    var appFactory = this.getView().getAppFactory();

    yesterday.setDate(yesterday.getDate() - 1);
    yesterday.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    var reportsForYesterday = [];
    var reportsForToday = [];
    var lastConfirmedReportDate = null;
    var lastTaskChangeDate = !tm.jquery.Utils.isEmpty(this.getLastTaskChangeTimestamp()) ?
        new Date(this.getLastTaskChangeTimestamp().getTime()) : null;

    // if the last task date is older than yesterday, neglect it since we only have 2 days worth of reports
    lastTaskChangeDate = !tm.jquery.Utils.isEmpty(lastTaskChangeDate)
        && lastTaskChangeDate < yesterday ? null : lastTaskChangeDate;

    // we need save the date and time of the last review that wasn't a draft and if that time is older
    // than the time of the last task update, we should make the daily container show "all therapies ok" placeholder
    // if there's no task at all, don't show it and if the last authorized review is newer than the task date presume
    // the review process for that task is done
    data.forEach(function (reviewDto)
    {
      var reviewDate = new Date(reviewDto.getCreateTimestamp().getTime());
      reviewDate.setHours(0, 0, 0, 0);

      if (!reviewDto.isDraft() && reviewDto.getCreateTimestamp() > lastConfirmedReportDate)
      {
        lastConfirmedReportDate = new Date(reviewDto.getCreateTimestamp().getTime());
      }

      if (reviewDate.getTime() === today.getTime())
      {
        reportsForToday.push(reviewDto);
      }
      else if (reviewDate.getTime() === yesterday.getTime())
      {
        reportsForYesterday.push(reviewDto);
      }
    });
    this.getView().getLocalLogger().debug("Finished sorting reviews.");
    this.getView().getLocalLogger().info("Last task time:", lastTaskChangeDate);

    var showPlaceHolder = this.getView().getTherapyAuthority().isManagePatientPharmacistReviewAllowed() &&
        (!tm.jquery.Utils.isEmpty(lastTaskChangeDate) &&
        (lastTaskChangeDate > lastConfirmedReportDate) ? true : false);

    var todayContainer = new tm.views.medications.pharmacists.DailyReviewsContainer({
      view: this.getView(),
      active: true,
      contentDate: today,
      content: reportsForToday,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      showPlaceHolder: showPlaceHolder,
      showSupply: this.getMedicationsSupplyPresent()
    });
    todayContainer.on(tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_THERAPY_SELECTION_CHANGE,
        function (component, componentEvent)
        {
          self.onTherapySelectionChange(componentEvent.getEventData().action, componentEvent.getEventData().therapyIds);
        });
    todayContainer.on(tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_CHANGE,
        function (component, componentEvent)
        {
          self.onDailyContainerContentChange();
        });
    todayContainer.on(tm.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_AUTHORIZED,
        function (component, componentEvent)
        {
          self.onDailyContainerContentAuthorized();
        });
    this.getReportsListContainer().add(todayContainer);

    if (this.getReportsListContainer().isRendered())
    {
      // we're preredering each container so the user gets to see something quicker
      this.getView().getLocalLogger().debug("Prerendering today container.");
      todayContainer.doRender();

      appFactory.createConditionTask(
          function ()
          {
            var $reportsListContainer = jQuery(self.getReportsListContainer().getDom());
            $reportsListContainer.prepend(todayContainer.getDom());
            self.getView().getLocalLogger().debug("Prerendering today container finished.");
          },
          function ()
          {
            return todayContainer.isRendered();
          },
          50, 50);
    }

    //if (reportsForYesterday.length > 0)
    //{
    var yesterdayContainer = new tm.views.medications.pharmacists.DailyReviewsContainer({
      view: this.getView(),
      active: false,
      contentDate: yesterday,
      content: reportsForYesterday,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      showSupply: this.getMedicationsSupplyPresent()
    });
    this.getReportsListContainer().add(yesterdayContainer);

    if (this.getReportsListContainer().isRendered())
    {
      // we're preredering each container so the user gets to see something quicker
      this.getView().getLocalLogger().debug("Prerendering yesterday container.");
      yesterdayContainer.doRender();

      appFactory.createConditionTask(
          function ()
          {
            var $reportsListContainer = jQuery(self.getReportsListContainer().getDom());
            $reportsListContainer.append(yesterdayContainer.getDom());
            self.getView().getLocalLogger().debug("Prerendering yesterday container finished.");
          },
          function ()
          {
            return yesterdayContainer.isRendered();
          },
          50, 50);
      var $reportsListContainer = jQuery(this.getReportsListContainer().getDom());
      $reportsListContainer.append(yesterdayContainer.getDom());
      this.getView().getLocalLogger().debug("Prerendering yesterday container finished.");
    }
    //}

    this._activeDailyContainer = todayContainer;
    this.getView().getLocalLogger().debug("Finished _fillDailyContainersListContainer.");
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
      }
      if (newSelected == "threeDaysTherapies")
      {
        this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 3, 0, 0);  //-3 days
        this.timelineEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 4, 0, 0);  //+3 days

        this.hidePastTherapies = false;
      }
      if (newSelected == "allTherapies")
      {
        if (!tm.jquery.Utils.isEmpty(this.view.getCentralCaseData()))  // MedicationsCentralCaseDto
        {
          if (!tm.jquery.Utils.isEmpty(this.view.getCentralCaseData().centralCaseEffective))
          {
            if (this.view.getCentralCaseData().care != "HOSPITAL")
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
      }
      if (newSelected == "discharge")
      {
        this.timelineStart = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis - 1000 * 60 * 60 * 24); //-24 hours  * 3
        this.timelineEnd = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis + 1000 * 60 * 60 * 24);  //+24 hours  * 3
        this.hidePastTherapies = true;
      }
      if (newSelected == "hospital")
      {
        var start = new Date(this.view.getCentralCaseData().centralCaseEffective.startMillis);
        var end = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis);
        this.timelineStart = new Date(start.getFullYear(), start.getMonth(), start.getDate() - 1, 0, 0);  // start of hospitalization  - 3, 0, 0
        this.timelineEnd = new Date(end.getFullYear(), end.getMonth(), end.getDate() + 2, 0, 0);  // end of the day of discharge  + 3, 0, 0
        this.hidePastTherapies = false;
      }
      this.previousSelectedButtonId = newSelected;
    }
  },

  ///
  /// public methods
  ///
  refreshData: function ()
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();

    this.clear();

    this.getView().getLocalLogger().debug("ReviewView refreshData() called.");

    this._loadTherapyData(this.getTherapySortTypeEnum(), function ()
    {
      self._fillTherapyListContainer();

      /* wait for the reports to load and mark the therapies*/
      appFactory.createConditionTask(
          function ()
          {
            self.markCheckedTherapies();
          },
          function ()
          {
            return !tm.jquery.Utils.isEmpty(self.getActiveDailyContainer());
          },
          50, 1000);
    });
    this._loadReportData(function ()
    {
      self._fillDailyContainersListContainer();
    });
  },

  ///
  /// getters/setters
  ///
  getView: function ()
  {
    return this.view;
  },

  getPatientId: function ()
  {
    return this.getView().patientId;
  },

  getCentralCaseId: function ()
  {
    var centralCaseData = this.getView().getCentralCaseData();
    return tm.jquery.Utils.isEmpty(centralCaseData) ? null : centralCaseData.centralCaseId;
  },

  getPatientHeightInCm: function ()
  {
    return this.getView().getPatientHeightInCm();
  },

  getRoundsInterval: function ()
  {
    return this.getView().getRoundsInterval();
  },

  getTherapySortTypeEnum: function ()
  {
    return this.getView().therapySortTypeEnum;
  },

  getGroupField: function ()
  {
    return this.getView().groupField;
  },

  getPharmacistReviewReferBackPreset: function()
  {
    return this.getView().getPharmacistReviewReferBackPreset();
  },

  getMedicationsSupplyPresent: function()
  {
    return this.getView().getMedicationsSupplyPresent();
  },

  getActiveTherapyData: function ()
  {
    return this.activeTherapyData;
  },

  setActiveTherapyData: function (data)
  {
    this.activeTherapyData = data;
  },

  getActiveReportsData: function ()
  {
    return this.activeReportsData;
  },

  setActiveReportsData: function (value)
  {
    this.activeReportsData = value;
  },
  setLastTaskChangeTimestamp: function (value)
  {
    this.lastTaskChangeTimestamp = value;
  },
  getLastTaskChangeTimestamp: function ()
  {
    return this.lastTaskChangeTimestamp;
  },
  getTherapyListContainer: function ()
  {
    return this._therapyColumn.getListContainer();
  },

  getReportsListContainer: function ()
  {
    return this._reportsColumn.getListContainer();
  },

  getActiveDailyContainer: function ()
  {
    return this._activeDailyContainer;
  },

  getTherapyDataSorter: function ()
  {
    return this.therapyDataSorter;
  },

  addTherapyToReview: function (therapyContainer)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    appFactory.createConditionTask(function()
        {
          var dailyContainer = self.getActiveDailyContainer();
          var activeReviewContainer = dailyContainer.getReviewContainerInEdit();
          var therapyData = jQuery.extend(true, {}, therapyContainer.getData());  // has to be deep copy or else references to child DTOs will be kept!

          if (tm.jquery.Utils.isEmpty(activeReviewContainer))
          {
            var now = CurrentTime.get();
            dailyContainer.addReview(new tm.views.medications.pharmacists.dto.PharmacistMedicationReview({
              createTimestamp: now,
              composer: view.getCurrentUserAsCareProfessional(),
              relatedTherapies: [ therapyData ],
              referBackToPrescriber: self.getPharmacistReviewReferBackPreset()
            }), true, true, true);
          }
          else
          {
            activeReviewContainer.addTherapy(therapyData);
          }

          therapyContainer.markActive(true);
          therapyContainer.markChecked(true);
        },
        function()
        {
          return !tm.jquery.Utils.isEmpty(self.getActiveDailyContainer()) && self.getActiveDailyContainer().isRendered();
        },
        50, 100);
  },

  /*
   * Marks therapies present in the current draft reports in the
   * */
  markCheckedTherapies: function ()
  {
    var draftTherapyIds = this.getActiveDailyContainer().getDraftReviewsTherapyIds();

    this._processTherapies(function (therapyContainer)
    {
      therapyContainer.markChecked(draftTherapyIds.contains(therapyContainer.getTherapyId()));
    });
  },

  markActiveTherapies: function()
  {
    var dailyContainer = this.getActiveDailyContainer();
    var activeReviewContainer = !tm.jquery.Utils.isEmpty(dailyContainer) ? dailyContainer.getReviewContainerInEdit() : null;

    if (!tm.jquery.Utils.isEmpty(activeReviewContainer))
    {
      var therapies = activeReviewContainer.getActiveTherapies();
      var therapyIds = therapies.map(function (relatedTherapy)
      {
        return tm.jquery.Utils.isEmpty(relatedTherapy.therapy) ? null : relatedTherapy.therapy.compositionUid;
      });
      this.onTherapySelectionChange('set', therapyIds);
    }
  },

  onDailyContainerContentChange: function ()
  {
    this.markCheckedTherapies();
  },

  onDailyContainerContentAuthorized: function()
  {
    this.refreshTherapies();
  },

  onTherapySelectionChange: function (action, therapyIds)
  {
    var contentContainers = this.getTherapyListContainer().getComponents();

    if (action === 'set')
    {
      this._processTherapies(function (therapyContainer)
      {
        for (var idx = 0; idx < therapyIds.length; idx++)
        {
          if (therapyContainer.getTherapyId() == therapyIds[idx])
          {
            therapyContainer.markActive(true);
            return;
          }
        }
        therapyContainer.markActive(false);
      });
    }
    else if (action === 'remove')
    {
      this._processTherapies(function (therapyContainer)
      {
        if (therapyContainer.getTherapyId() == therapyIds[0])
        {
          therapyContainer.markActive(false);
          return true;
        }
      });
      this.markCheckedTherapies(); // go trough all therapies again in case the therapy was already present on another review
    }
  },

  refreshTherapies: function ()
  {
    var self = this;
    var dailyContainer = this.getActiveDailyContainer();
    var activeReviewContainer = dailyContainer.getReviewContainerInEdit();

    this._loadTherapyData(this.getTherapySortTypeEnum(), function ()
    {
      self._fillTherapyListContainer();
      self.markActiveTherapies();
      self.markCheckedTherapies();
    });
  },
  clear: function ()
  {
    this.getTherapyListContainer().removeAll();
    this._therapyColumn.setColumnTitle(this.getView().getDictionary("therapies"));
    this.getReportsListContainer().removeAll();
    this._activeDailyContainer = null;
    this.previousSelectedButtonId = null;
    this.selectedShownTherapies = null;
  },

  setRoutesAndCustomGroupsFilter: function(routes, customgroups, applyFilter)
  {
    // comparing by reference since a new array for routes and groups is always created and always set by the toolbar
    if (this.routesFilter != routes || this.customGroupsFilter != customgroups)
    {
      this.routesFilter = routes;
      this.customGroupsFilter = customgroups;

      if (applyFilter == true)
      {
        this._fillTherapyListContainer();
        this.markActiveTherapies();
        this.markCheckedTherapies();
      }
    }
  },

  setShownTherapies: function(shownTherapies)
  {
    this.selectedShownTherapies = shownTherapies;
  }
});
