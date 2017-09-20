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

Class.define('tm.views.medications.TherapyFlowGrid', 'tm.jquery.Container', {

  /** configs */
  view: null,
  /** privates */
  state: null,
  dayCount: null,
  therapySortTypeEnum: null,
  todayIndex: null,
  searchDate: null,
  grouping: null,
  isRoundsTime: true,
  gridCellTherapyDisplayProvider: null,
  therapyFlowData: null,
  previousDayTherapyFlowData: null,
  nextDayTherapyFlowData: null,
  previousDayDataLoaded: false,
  nextDayDataLoaded: false,
  navigationLocked: false,
  actionsQueue: null,
  therapyAction: null,
  /** privates: components */
  grid: null,
  noTherapiesField: null,

  _isGridReadyConditionalTask: null,

  /** constructor */
  Constructor: function(config)
  {
    config = tm.jquery.Utils.applyIf({
      cls: "therapy-flow-grid-container",
      //layout: new tm.jquery.BorderLayout()
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch")
    }, config);

    this.callSuper(config);

    this.actionsQueue = [];
    this.therapyActions = new app.views.medications.TherapyActions({view: this.getView()});
    this.gridCellTherapyDisplayProvider = new app.views.medications.TherapyDisplayProvider({view: this.getView()});
  },

  /** private methods */
  _paintGrid: function(searchDate)
  {
    var self = this;

    this.grid = new tm.jquery.Grid({
      cls: 'therapy-grid',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%"),
      selectable: false,
      highlighting: false,

      data: new tm.jquery.GridData({
        source: []
      }),
      columns: new tm.jquery.GridColumns({
        names: self._getColumnNames(searchDate),
        model: self._createModelColumn()
      }),
      rows: new tm.jquery.GridRows({
        rowCount: 100,
        rowNumbers: false,
        multiSelect: false
      }),
      grouping: self.grouping,
      options: {
        autowidth: true,
        shrinkToFit: true /* makes the column width property act as a proportion instead of fixed pixel size */
      }
    });

    this.noTherapiesField = this.getView().getNoTherapiesField();
    this.add(this.noTherapiesField);
    this.noTherapiesField.hide();
    this.add(this.grid, {region: 'center'});
  },

  _setGridData: function(gridData, adjustedParams)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    if (this._isGridReadyConditionalTask)
    {
      this._abortIsGridReadyConditionalTask();
    }

    this._isGridReadyConditionalTask = appFactory.createConditionTask(
        function()
        {
          hideLoaderMaskClearTask();
          var scrollPosition = $(self.grid.getScrollableElement()).scrollTop();
          self.grid.setGridData(gridData);
          self._fixGridColumns(adjustedParams);
          $(self.grid.getScrollableElement()).scrollTop(scrollPosition);
        },
        function(task)
        {
          if (!self.isRendered())
          {
            task.abort();
            hideLoaderMaskClearTask();
          }
          return self.grid && self.grid.isRendered() && !tm.jquery.Utils.isEmpty(self.grid.getPlugin())
        },
        function()
        {
          hideLoaderMaskClearTask();
        },
        100, 100);

    function hideLoaderMaskClearTask()
    {
      self._isGridReadyConditionalTask = null;
      view.hideLoaderMask();
    }
  },

  _executeTaskWhenCondition: function(condition, task)
  {
    var self = this;
    if (condition())
    {
      task();
    }
    else
    {
      setTimeout(
          function()
          {
            self._executeTaskWhenCondition(condition, task)
          }, 50);
    }
  },

  _fixGridColumns: function(searchParams)
  {
    var lastColumnName = 'column' + this.dayCount;
    if (searchParams.todayIndex != null)
    {
      this.grid.getPlugin().jqGrid('hideCol', lastColumnName);
    }
    else
    {
      this.grid.getPlugin().jqGrid('showCol', lastColumnName);
    }

    var date = new Date(searchParams.searchDate);
    for (var i = 0; i < searchParams.dayCount; i++)
    {
      /* see http://stackoverflow.com/questions/12171640/jqgrid-changing-the-width-of-a-column-dynamically/12172228#12172228 */
      this.grid.getPlugin().setColProp('column' + i, {
        width: searchParams.todayIndex == i ? 2 : 1,
        widthOrg: searchParams.todayIndex == i ? 2 : 1
      });

      var headerLabel = this._getHeaderString(date, i == searchParams.todayIndex, i);
      this.grid.setHeaderLabel('column' + i, headerLabel);
      date.setDate(date.getDate() + 1);
    }

    var gridWidth = jQuery(this.grid.getDom()).width();
    /* we need the actual pixels */
    this.grid.getPlugin().jqGrid('setGridWidth', gridWidth);
    /* makes the grid resize the columns */
  },

  _loadData: function(searchParams, callback)
  {
    var self = this;
    var view = this.getView();
    var findTherapyFlowDataUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_LOAD_THERAPY_FLOW_DATA;

    view.showLoaderMask();
    var patientId = view.getPatientId();
    var params = {
      patientId: patientId,
      centralCaseId: view.getCentralCaseData() && view.getCentralCaseData().centralCaseId ?
          view.getCentralCaseData().centralCaseId : null,
      patientHeight: view.getPatientHeightInCm(),
      startDate: searchParams.searchDate.getTime(),
      dayCount: searchParams.dayCount,
      todayIndex: searchParams.todayIndex != null ? searchParams.todayIndex : -1,
      roundsInterval: JSON.stringify(view.getRoundsInterval()),
      therapySortTypeEnum: this.therapySortTypeEnum,
      careProviderId: view.getCareProviderId()
    };
    view.loadViewData(findTherapyFlowDataUrl, params, null, function(therapyFlowData)
    {
      if (patientId == view.getPatientId())
      {
        self.therapyFlowData = therapyFlowData;
        var gridData = self._buildGridData(therapyFlowData.therapyRows);
        callback(gridData);
      }
      view.hideLoaderMask();
    });
  },

  _buildGridData: function(therapies)
  {
    var self = this;
    var view = this.getView();
    var gridData = [];
    for (var i = 0; i < therapies.length; i++)
    {
      var rowData = therapies[i];
      var row = {
        id: i,
        atcGroup: rowData.atcGroupName ? rowData.atcGroupName + ' (' + rowData.atcGroupCode + ')' : view.getDictionary("without.atc"),
        routes: self._createRowValueForRowDataRoutes(rowData.routes),
        customGroup: tm.views.medications.MedicationUtils.getTherapyCustomGroupDisplayName(rowData, view)
      };

      for (var j = 0; j < self.dayCount + 1; j++)
      {
        var cellData = rowData.therapyFlowDayMap[j];
        if (cellData && cellData.therapy)
        {
          cellData.therapy = app.views.medications.common.TherapyJsonConverter.convert(cellData.therapy);
        }
        row['column' + j] = cellData ?
        {
          id: j,
          dayTherapy: cellData
        } : null;
      }
      gridData.push(row);
    }
    return gridData;
  },

  _loadPreviousDayData: function()
  {
    var self = this;
    this.previousDayDataLoaded = false;
    var view = this.getView();

    var previousDate = new Date(this.searchDate);
    previousDate.setDate(previousDate.getDate() - 1);
    var adjustedParams = self._getAdjustedSearchParams(previousDate, false);
    var patientId = view.getPatientId();

    var params = {
      patientId: patientId,
      centralCaseId: view.getCentralCaseData() && view.getCentralCaseData().centralCaseId ?
          view.getCentralCaseData().centralCaseId : null,
      patientHeight: view.getPatientHeightInCm(),
      startDate: adjustedParams.searchDate.getTime(),
      dayCount: adjustedParams.dayCount,
      todayIndex: adjustedParams.todayIndex != null ? adjustedParams.todayIndex : -1,
      roundsInterval: JSON.stringify(view.getRoundsInterval()),
      therapySortTypeEnum: this.therapySortTypeEnum,
      careProviderId: view.getCareProviderId()
    };

    var findTherapyFlowDataUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_LOAD_THERAPY_FLOW_DATA;
    view.loadViewData(findTherapyFlowDataUrl, params, null, function(therapyFlowData)
    {
      if (patientId == view.getPatientId())
      {
        self.previousDayTherapyFlowData = therapyFlowData;
        self.previousDayDataLoaded = true;
      }
    });
  },

  _loadNextDayData: function()
  {
    var self = this;
    this.nextDayDataLoaded = false;
    var view = this.getView();

    var nextDate = new Date(this.searchDate);
    nextDate.setDate(nextDate.getDate() + 1);
    var adjustedParams = self._getAdjustedSearchParams(nextDate, true);
    var patientId = view.getPatientId();

    var params = {
      patientId: patientId,
      centralCaseId: view.getCentralCaseData() && view.getCentralCaseData().centralCaseId ?
          view.getCentralCaseData().centralCaseId : null,
      patientHeight: view.getPatientHeightInCm(),
      startDate: adjustedParams.searchDate.getTime(),
      dayCount: adjustedParams.dayCount,
      todayIndex: adjustedParams.todayIndex != null ? adjustedParams.todayIndex : -1,
      roundsInterval: JSON.stringify(view.getRoundsInterval()),
      therapySortTypeEnum: this.therapySortTypeEnum,
      careProviderId: view.getCareProviderId()
    };

    var findTherapyFlowDataUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_LOAD_THERAPY_FLOW_DATA;
    view.loadViewData(findTherapyFlowDataUrl, params, null, function(therapyFlowData)
    {
      if (patientId == view.getPatientId())
      {
        self.nextDayTherapyFlowData = therapyFlowData;
        self.nextDayDataLoaded = true;
      }
    });
  },

  _getAdjustedSearchParams: function(searchDate, forward)
  {
    var todayIndex = this._getTodayIndex(this.dayCount, searchDate);

    var adjustedDayCount = this.dayCount;
    var adjustedSearchDate = searchDate;

    if (todayIndex == null)
    {
      if (forward != null && this._getTodayIndex(this.dayCount + 1, searchDate) != null)
      {
        adjustedDayCount = this.dayCount + 1;
        if (forward)
        {
          adjustedSearchDate.setDate(searchDate.getDate() + 1);
        }
        else
        {
          adjustedSearchDate.setDate(searchDate.getDate() - 1);
        }
      }
      else
      {
        adjustedDayCount = this.dayCount + 1;
      }
    }

    var adjustedTodayIndex = this._getTodayIndex(adjustedDayCount, adjustedSearchDate);

    return {
      dayCount: adjustedDayCount,
      searchDate: adjustedSearchDate,
      todayIndex: adjustedTodayIndex
    }
  },

  _getColumnNames: function(searchDate)
  {
    var self = this;
    var columns = [];
    columns.push("id");
    columns.push("atcGroup");
    columns.push("routes");
    columns.push("customGroup");
    var date = new Date(searchDate);
    for (var j = 0; j < this.dayCount + 1; j++)
    {
      columns.push(this._getHeaderString(date, j == self.todayIndex, j));
      date.setDate(date.getDate() + 1);
    }
    return columns;
  },

  _getHeaderString: function(date, today, index)
  {
    var view = this.getView();
    var dayString = view.getDisplayableValue(new Date(date), "short.date");
    var referenceWeightString = "";
    if (this.therapyFlowData)
    {
      var referenceWeight = this.therapyFlowData.referenceWeightsDayMap[index];
      if (referenceWeight)
      {
        referenceWeightString = tm.views.medications.MedicationUtils.doubleToString(referenceWeight, 'n3') + 'kg';
      }
    }

    var headerString = "<div class='TextData'; style='float:left; width:33%; height: 18px; text-align:left; padding-left:5px;'>" + referenceWeightString + "</div>";
    if (today)
    {
      return headerString +
          "<div class='TextData'; style='float:left; width:33%; text-align:center; overflow: visible;'> <b>" + view.getDictionary("today") + " " + dayString + "</b></div>";
    }

    return headerString +
        "<div class='TextData'; style='float:left; width:33%; text-align:center; overflow: visible;'> " + dayString + "</div>";
  },

  _getTodayIndex: function(dayCount, searchDate)
  {
    var date = new Date(searchDate);
    var today = CurrentTime.get();
    for (var j = 0; j < dayCount; j++)
    {
      if (today.getDate() == date.getDate() &&
          today.getMonth() == date.getMonth() &&
          today.getFullYear() == today.getFullYear())
      {
        return j;
      }
      date.setDate(date.getDate() + 1);
    }
    return null;
  },

  _createModelColumn: function()
  {
    var self = this;
    var columns = [];
    columns.push({
      name: 'id',
      index: 'id',
      width: 0,
      hidden: true
    });
    columns.push({
      name: 'atcGroup',
      index: 'atcGroup',
      width: 0,
      hidden: true,
      sorttype: function(cellValue, obj)
      {
        return obj.atcGroup + "." + tm.views.medications.MedicationUtils.pad(obj.id, 4);
      }
    });
    columns.push({
      name: 'routes',
      index: 'routes',
      width: 0,
      hidden: true,
      sorttype: function(cellValue, obj)
      {
        return obj.routes + "." + tm.views.medications.MedicationUtils.pad(obj.id, 4);
      }
    });
    columns.push({
      name: 'customGroup',
      index: 'customGroup',
      width: 0,
      hidden: true,
      sorttype: function(cellValue, obj)
      {
        return obj.customGroup + "." + tm.views.medications.MedicationUtils.pad(obj.id, 4);
      }
    });

    for (var i = 0; i < this.dayCount + 1; i++)
    {
      columns.push({
            name: 'column' + i,
            index: i,
            width: this.todayIndex == i ? 2 : 1,
            sortable: false,
            title: false,
            hidden: self.todayIndex > 0 && i == self.dayCount,
            resizable: false,
            formatter: new tm.jquery.GridCellFormatter({
              content: function(cellvalue, options)
              {
                if (cellvalue)
                {
                  if (options.colModel.index == self.todayIndex)
                  {
                    return self._createTodayCellTherapyContainer(options.rowId, self.todayIndex, cellvalue.dayTherapy);
                  }
                  else
                  {
                    return self._createCellHtmlTemplate(options.rowId, options.colModel.index, cellvalue.dayTherapy)
                  }
                }
                return self._createEmptyCellHtmlTemplate();
              }
            }),
            cellattr: function(rowId, value, rowObject, colModel, arrData)
            {
              var cellData = rowObject[colModel.name];
              var enums = app.views.medications.TherapyEnums;
              var dayTherapy = cellData ? cellData.dayTherapy : null;
              if (dayTherapy)
              {
                var isPastDay = self._isPastDay(colModel.index);
                var isActiveTodayOrFutureTherapy = !isPastDay && dayTherapy.active && dayTherapy.therapyStatus != enums.therapyStatusEnum.ABORTED && dayTherapy.therapyStatus != enums.therapyStatusEnum.CANCELLED && dayTherapy.therapyStatus != enums.therapyStatusEnum.SUSPENDED;
                var isActivePastTherapy = isPastDay && dayTherapy.activeAnyPartOfDay;
                if (isActiveTodayOrFutureTherapy || isActivePastTherapy)
                {
                  return 'style="vertical-align:top; background:#fff';
                }
              }
              return 'style="vertical-align:top;background:#E2DFDF"';
            }
          }
      );
    }
    return columns;
  },

  _isPastDay: function(index)
  {
    var todayIndex = this._getTodayIndex(this.dayCount, this.searchDate);
    if (todayIndex)
    {
      return index < todayIndex;
    }
    return this.searchDate < CurrentTime.get();
  },

  _createCellHtmlTemplate: function(rowId, columnIndex, dayTherapy)
  {
    var mainContainer = new tm.jquery.Container({
      cls: 'cell-item',
      padding: dayTherapy.showConsecutiveDay ? "5 5 5 3" : 5,
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start')
    });

    if (dayTherapy.showConsecutiveDay)
    {
      mainContainer.add(new tm.jquery.Container({
        cls: "icon_day_number",
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px"), /* width has to be set otherwise IE10 will use auto style set on tm.jquery.Component! */
        layout: tm.jquery.VFlexboxLayout.create('flex-end', 'flex-end'),
        html: dayTherapy.consecutiveDay
      }));
    }

    mainContainer.add(new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: 'visible',
      html: dayTherapy.therapy.formattedTherapyDisplay,
      cls: 'TherapyDescription ShortDescription'
    }));

    var contextMenu = this._createTherapyContextMenu(dayTherapy, rowId, false, false);
    if (contextMenu && contextMenu.hasMenuItems())
    {
      mainContainer.setContextMenu(contextMenu);
    }

    return mainContainer;
  },

  /**
   * @param {Number} rowId
   * @param {Number} columnIndex
   * @param {Object} dayTherapy
   * @returns {app.views.medications.common.TherapyContainer}
   * @private
   */
  _createTodayCellTherapyContainer: function(rowId, columnIndex, dayTherapy)
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var pharmacistReviewReferBack =
        dayTherapy.therapyPharmacistReviewStatus == enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK;

    var therapyContainer = new app.views.medications.common.TherapyContainer({
      view: view,
      data: dayTherapy,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollableElement: this.grid.getScrollableElement(),
      displayProvider: this.getGridCellTherapyDisplayProvider()
    });

    var toolBar = new tm.views.medications.therapy.GridTherapyContainerToolbar({
      therapyContainer: therapyContainer
    });
    toolBar.setShowRelatedPharmacistReviewsEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      view.onShowRelatedPharmacistReviews(data, function()
      {
        self.reloadGridData();
      });
    });
    toolBar.setEditTherapyEventCallback(function(therapyContainer)
    {
      tm.jquery.ComponentUtils.hideAllTooltips();
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      view.showEditTherapyDialog(therapy, false, data.modified);
    });
    toolBar.setTasksChangedEventCallback(function()
    {
      self.reloadGridData();
    });
    toolBar.setAbortTherapyEventCallback(function()
    {
      tm.jquery.ComponentUtils.hideAllTooltips();
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      this.disableActionButtons();
      self._addActionToQueue(therapy.getCompositionUid(), therapy.getEhrOrderName(), rowId, 'ABORT');
    });
    toolBar.setConfirmTherapyEventCallback(function(callback)
    {
      tm.jquery.ComponentUtils.hideAllTooltips();
      var data = therapyContainer.getData();
      var therapy = data.therapy;

      this.disableActionButtons();
      var action = data.therapyStatus == 'SUSPENDED' ? 'REISSUE' : 'CONFIRM';
      self._addActionToQueue(therapy.getCompositionUid(), therapy.getEhrOrderName(), rowId, action);
    });

    therapyContainer.setToolbar(toolBar);

    var contextMenu = this._createTherapyContextMenu(dayTherapy, rowId, true, pharmacistReviewReferBack);
    if (contextMenu && contextMenu.hasMenuItems())
    {
      therapyContainer.setContextMenu(contextMenu);
    }

    return therapyContainer;
  },

  _createEmptyCellHtmlTemplate: function()
  {
    return new tm.jquery.Container({
      cls: 'cell-item',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      style: "position:relative; white-space: normal; background:#E2DFDF;"
    });
  },

  _createActionButtonsContainerId: function(rowId)
  {
    return this.getView().getViewId() + "_action_buttons_" + rowId;
  },

  //_createTherapyDetailsCardTooltip: function(dayTherapy)       // [TherapyDayDto.java]
  //{
  //  var self = this;
  //  var appFactory = this.view.getAppFactory();
  //
  //  var ehrOrderName = dayTherapy.therapy.ehrOrderName;
  //  var ehrCompositionId = dayTherapy.therapy.compositionUid;
  //  var consecutiveDay = dayTherapy.consecutiveDay;
  //  var therapyStatus = dayTherapy.therapyStatus;
  //
  //  var detailsCardContainer = new app.views.medications.TherapyDetailsCardContainer({view: this.view});
  //
  //  var scrollableElement = this.grid.getScrollableElement();
  //
  //  var tooltip = appFactory.createDefaultPopoverTooltip(
  //      self.view.getDictionary("medication"),
  //      null,
  //      detailsCardContainer
  //  );
  //  tooltip.onShow = function()
  //  {
  //    detailsCardContainer.updateData({
  //      ehrCompositionId: ehrCompositionId,
  //      ehrOrderName: ehrOrderName,
  //      therapyState: {
  //        consecutiveDay: consecutiveDay,
  //        therapyStatus: therapyStatus
  //      }});
  //  };
  //  tooltip.setPlacement("auto");
  //  tooltip.setDefaultAutoPlacements(["rightBottom", "rightTop", "right"]);
  //  tooltip.setAppendTo(scrollableElement);
  //
  //  return tooltip;
  //},

  _createTherapyContextMenu: function(dayTherapy, rowId, today, disableEditTherapy)     // [TherapyDayDto.java]
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;

    var therapy = dayTherapy.therapy;
    var therapyModifiedInThePast = dayTherapy.modified;
    var ehrOrderName = therapy.getEhrOrderName();
    var ehrCompositionId = therapy.getCompositionUid();
    var therapyStatus = dayTherapy.therapyStatus;
    var orderFormType = therapy.getMedicationOrderFormType();

    if (therapy == null || 
        (!view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() && 
        !view.getTherapyAuthority().isCopyPrescriptionAllowed()))
    {
      return null;
    }

    var contextMenu = appFactory.createContextMenu();
    if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
    {
      if (today && therapyStatus != enums.therapyStatusEnum.ABORTED && therapyStatus != enums.therapyStatusEnum.CANCELLED)
      {
        var menuItemEdit = new tm.jquery.MenuItem({
          text: view.getDictionary("edit"), /*iconCls: 'icon-edit',*/
          enabled: !disableEditTherapy,
          handler: function()
          {
            view.showEditTherapyDialog(therapy, false, therapyModifiedInThePast);
          }
        });
        contextMenu.addMenuItem(menuItemEdit);
      }

      var centralCaseExists = view.getCentralCaseData() && view.getCentralCaseData().centralCaseId;

      if (centralCaseExists)
      {
        var therapyStart = therapy.getStart();
        var consecutiveDay = dayTherapy.consecutiveDay;
        var columnDate = new Date(therapyStart.getTime() + consecutiveDay * 24 * 60 * 60 * 1000).setHours(0, 0, 0, 0);
        var effectiveEnd = view.getCentralCaseData().centralCaseEffective.end
            ? new Date(view.getCentralCaseData().centralCaseEffective.end).setHours(0, 0, 0, 0) : null;

        if (today || effectiveEnd && columnDate.getTime() === effectiveEnd.getTime())
        {
          if (therapy.isTaggedForPrescription())
          {
            var menuItemTag = new tm.jquery.MenuItem({
              text: view.getDictionary("untag.therapy.for.prescription"),
              handler: function()
              {
                view.untagTherapyForPrescription(
                    view.getPatientId(),
                    view.getCentralCaseData().centralCaseId,
                    ehrCompositionId,
                    ehrOrderName);
              }
            });
            contextMenu.addMenuItem(menuItemTag);
          }
          else
          {
            var menuItemUntag = new tm.jquery.MenuItem({
              text: view.getDictionary("tag.therapy.for.prescription"),
              handler: function()
              {
                view.tagTherapyForPrescription(
                    view.getPatientId(),
                    view.getCentralCaseData().centralCaseId,
                    ehrCompositionId,
                    ehrOrderName);
              }
            });
            contextMenu.addMenuItem(menuItemUntag);
          }
        }
      }

      if (today && therapyStatus != enums.therapyStatusEnum.ABORTED && therapyStatus != enums.therapyStatusEnum.CANCELLED)
      {
        var menuItemDelete = new tm.jquery.MenuItem({
          text: view.getDictionary("stop.therapy"), /* iconCls: 'icon-delete', */
          handler: function()
          {
            self._addActionToQueue(ehrCompositionId, ehrOrderName, rowId, 'ABORT');
          }
        });
        contextMenu.addMenuItem(menuItemDelete);
        if (therapyStatus != 'SUSPENDED')
        {
          var menuItemSuspend = new tm.jquery.MenuItem({
            text: view.getDictionary("suspend"), /*iconCls: 'icon-suspend', */
            handler: function()
            {
              self._addActionToQueue(ehrCompositionId, ehrOrderName, rowId, 'SUSPEND');
            }
          });
          contextMenu.addMenuItem(menuItemSuspend);
        }
  
        if (dayTherapy.showConsecutiveDay && !dayTherapy.modified)
        {
          var menuItemConsecutive = new tm.jquery.MenuItem({
            text: view.getDictionary("consecutive.days.edit"), /*iconCls: 'icon-edit-consecutive-days',*/
            handler: function()
            {
              view.showEditConsecutiveDaysDialog(therapy.getPastDaysOfTherapy(), ehrCompositionId, ehrOrderName);
            }
          });
          contextMenu.addMenuItem(menuItemConsecutive);
        }
      }
    }

    if (view.getTherapyAuthority().isCopyPrescriptionAllowed())
    {
      var menuItemCopySimpleTherapy = new tm.jquery.MenuItem({
        text: view.getDictionary("copy"), /*iconCls: 'icon-copy', */
        handler: function()
        {
          view.showEditTherapyDialog(therapy, true, false);
        }
      });
      contextMenu.addMenuItem(menuItemCopySimpleTherapy);
    }

    if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())

    {
      if (today && therapyStatus != enums.therapyStatusEnum.ABORTED && therapyStatus != enums.therapyStatusEnum.CANCELLED)
      {
        var menuItemSuspendAll = new tm.jquery.MenuItem({
          text: view.getDictionary("suspend.all"), /* iconCls: 'icon-suspend-all', */
          handler: function()
          {
            self._suspendAllTherapies();
          }
        });
        contextMenu.addMenuItem(menuItemSuspendAll);
      }

    }
    return contextMenu;
  },

  _canTherapyBeAborted: function(therapy)
  {
    var enums = app.views.medications.TherapyEnums;
    if (therapy.linkName)
    {
      var nextTherapyLink = tm.views.medications.MedicationUtils.getNextLinkName(therapy.linkName);
      var linkedTherapyDay = this._getTherapyDayByLinkName(nextTherapyLink);
      if (linkedTherapyDay && linkedTherapyDay.therapyStatus)
      {
        if (linkedTherapyDay.therapyStatus != enums.therapyStatusEnum.ABORTED &&
            linkedTherapyDay.therapyStatus != enums.therapyStatusEnum.CANCELLED)
        {
          return false;
        }
      }
    }
    return true;
  },

  _getTherapyDayByLinkName: function(linkName)
  {
    for (var i = 0; i < this.therapyFlowData.therapyRows.length; i++)
    {
      var therapyRow = this.therapyFlowData.therapyRows[i];
      var therapyDay = therapyRow.therapyFlowDayMap[this.todayIndex];
      if (therapyDay && therapyDay.therapy && therapyDay.therapy.linkName && therapyDay.therapy.linkName == linkName)
      {
        return therapyDay;
      }
    }
    return null;
  },

  _addActionToQueue: function(ehrCompositionId, ehrOrderName, rowIndex, action)
  {
    this.actionsQueue.push({
      ehrCompositionId: ehrCompositionId,
      ehrOrderName: ehrOrderName,
      rowIndex: rowIndex,
      action: action
    });
    this._executeTasks(true);
  },

  _onAbortTherapy: function(nextTask, self, changeReason)
  {
    this.therapyActions.abortTherapy(nextTask.ehrCompositionId, nextTask.ehrOrderName, changeReason != null ? changeReason.value : null,
        function()
        {
          self._reloadSingleTherapyChanges(nextTask.ehrCompositionId, nextTask.ehrOrderName, nextTask.rowIndex, function()
          {
            self.actionsQueue.shift();
            self._executeTasks(false);
          });
        },
        function()
        {
          self.actionsQueue.shift();
          self._executeTasks(false);
        });
  },

  _onSuspendTherapy: function(nextTask, self, changeReason)
  {
    this.therapyActions.suspendTherapy(nextTask.ehrCompositionId, nextTask.ehrOrderName, changeReason != null ? changeReason.value : null,
        function()
        {
          self._reloadSingleTherapyChanges(nextTask.ehrCompositionId, nextTask.ehrOrderName, nextTask.rowIndex, function()
          {
            self.actionsQueue.shift();
            self._executeTasks(false);
          });
        },
        function()
        {
          self.actionsQueue.shift();
          self._executeTasks(false);
        });
  },

  _executeTasks: function(newTask)
  {
    var actionEnums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var self = this;
    var view = this.getView();
    if (this.actionsQueue.length == 1 || (!newTask && this.actionsQueue.length > 0))
    {
      var nextTask = self.actionsQueue[0];
      if (nextTask.action == 'CONFIRM')
      {
        this.therapyActions.reviewTherapy(nextTask.ehrCompositionId, nextTask.ehrOrderName,
            function()
            {
              self._reloadSingleTherapyChanges(nextTask.ehrCompositionId, nextTask.ehrOrderName, nextTask.rowIndex, function()
              {
                self.actionsQueue.shift();
                self._executeTasks(false);
              });
            },
            function()
            {
              self.actionsQueue.shift();
              self._executeTasks(false);
            });
      }
      else if (nextTask.action == 'ABORT')
      {
        var rowData = this.therapyFlowData.therapyRows[nextTask.rowIndex];
        var therapyDay = rowData.therapyFlowDayMap[this.todayIndex];
        if (this._canTherapyBeAborted(therapyDay.therapy, therapyDay))
        {
          if (therapyDay.therapy.linkedToAdmission == true)
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
                  if (resultData != null && resultData.isSuccess())
                  {
                    self._onAbortTherapy(nextTask, self, resultData.value);
                  }
                  else
                  {
                    self.actionsQueue.shift();
                    self._executeTasks(false);
                  }
                }, abortConfirmationEntryPane.defaultWidth, abortConfirmationEntryPane.defaultHeight
            );
            abortConfirmationDialog.show();
          }
          else
          {
            this._onAbortTherapy(nextTask, self, null);
          }
        }
        else
        {
          self.actionsQueue.shift();
          self._executeTasks(false);
          var message = view.getDictionary('therapy.can.not.stop.if.linked');
          view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
        }
      }
      else if (nextTask.action == 'REISSUE')
      {
        this.therapyActions.reissueTherapy(nextTask.ehrCompositionId, nextTask.ehrOrderName,
            function()
            {
              self._reloadSingleTherapyChanges(nextTask.ehrCompositionId, nextTask.ehrOrderName, nextTask.rowIndex, function()
              {
                self.actionsQueue.shift();
                self._executeTasks(false);
              });
            },
            function()
            {
              self.actionsQueue.shift();
              self._executeTasks(false);
            });
      }
      else if (nextTask.action == 'SUSPEND')
      {
        var therapy = this.therapyFlowData.therapyRows[nextTask.rowIndex].therapyFlowDayMap[this.todayIndex].therapy;
        if (therapy.linkedToAdmission == true)
        {
          var actionTypeEnum = actionEnums.SUSPEND;
          var changeReasonMap = view.getTherapyChangeReasonTypeMap();
          this._onSuspendTherapy(nextTask, self,
              tm.views.medications.MedicationUtils.getFirstOrNullTherapyChangeReason(changeReasonMap, actionTypeEnum));
        }
        else
        {
          this._onSuspendTherapy(nextTask, self, null);
        }
      }
    }
  },

  _reloadSingleTherapyChanges: function(ehrCompositionId, ehrOrderName, rowIndex, callback)
  {
    var self = this;
    var view = this.getView();
    var reloadUrl =
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_RELOAD_SINGLE_THERAPY_AFTER_ACTION;
    var uidWithoutVersion = tm.views.medications.MedicationUtils.getUidWithoutVersion(ehrCompositionId);
    var params = {
      patientId: view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName,
      roundsInterval: JSON.stringify(view.getRoundsInterval())
    };
    view.loadViewData(reloadUrl, params, null,
        function(reloadAfterActionDto) //TherapyReloadAfterActionDto.java
        {
          self._updateAllTherapiesCompositionUid(
              ehrCompositionId,
              reloadAfterActionDto.ehrCompositionId,
              reloadAfterActionDto.ehrOrderName,
              reloadAfterActionDto.therapyStatus,
              reloadAfterActionDto.doctorReviewNeeded,
              reloadAfterActionDto.therapyEndsBeforeNextRounds,
              reloadAfterActionDto.therapyStart,
              reloadAfterActionDto.therapyEnd
          );
          self._refreshTherapiesAfterAction(rowIndex);
          callback();
        },
        function()
        {
          callback();
        });
  },

  _refreshTherapiesAfterAction: function(rowIndex)
  {
    var scrollPosition = tm.jquery.ComponentUtils.getScrollPosition(this.grid.getScrollableElement());
    var gridData = this._buildGridData(this.therapyFlowData.therapyRows);
    this.grid.setRowData(rowIndex, gridData[rowIndex]);

    if (!tm.jquery.Utils.isEmpty(scrollPosition) && scrollPosition.scrollTop)
    {
      this.grid.scrollTo(scrollPosition.scrollLeft, scrollPosition.scrollTop, 0);
    }
  },

  _suspendAllTherapies: function()
  {
    var view = this.getView();

    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SUSPEND_ALL_HUB;
    viewHubNotifier.actionStarted(hubAction);

    view.showLoaderMask();
    var suspendAllTherapiesUrl =
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SUSPEND_ALL_THERAPIES;
    var params = {patientId: view.getPatientId()};
    view.loadPostViewData(suspendAllTherapiesUrl, params, null, function()
    {
      view.refreshTherapies(false);
      view.hideLoaderMask();
      viewHubNotifier.actionEnded(hubAction);
    });
  },

  _updateAllTherapiesCompositionUid: function(oldCompositionUid, newCompositionUid, ehrOrderName, status, doctorReviewNeeded, therapyEndsBeforeNextRounds, therapyStart, therapyEnd)
  {
    this._updateTherapiesCompositionUid(
        this.therapyFlowData.therapyRows,
        oldCompositionUid,
        newCompositionUid,
        ehrOrderName,
        status,
        doctorReviewNeeded,
        therapyEndsBeforeNextRounds,
        therapyStart,
        therapyEnd,
        this.todayIndex,
        null
    );
    var previousDay = new Date(this.searchDate);
    previousDay.setDate(previousDay.getDate() - 1);
    var previousDaySearchParameters = this._getAdjustedSearchParams(previousDay, false);
    this._updateTherapiesCompositionUid(
        this.previousDayTherapyFlowData.therapyRows,
        oldCompositionUid,
        newCompositionUid,
        ehrOrderName,
        status,
        doctorReviewNeeded,
        therapyEndsBeforeNextRounds,
        therapyStart,
        therapyEnd,
        previousDaySearchParameters.todayIndex,
        false
    );
    var nextDay = new Date(this.searchDate);
    nextDay.setDate(nextDay.getDate() + 1);
    var nextDaySearchParameters = this._getAdjustedSearchParams(nextDay, true);
    this._updateTherapiesCompositionUid(
        this.nextDayTherapyFlowData.therapyRows,
        oldCompositionUid,
        newCompositionUid,
        ehrOrderName,
        status,
        doctorReviewNeeded,
        therapyEndsBeforeNextRounds,
        therapyStart,
        therapyEnd,
        nextDaySearchParameters.todayIndex,
        true
    );
  },

  /**
   * Safely abort the conditional task that blocks setting data to the grid if it's not ready.
   * @private
   */
  _abortIsGridReadyConditionalTask: function()
  {
    if (this._isGridReadyConditionalTask)
    {
      this._isGridReadyConditionalTask.abort();
      this._isGridReadyConditionalTask = null;
    }
  },

  _updateTherapiesCompositionUid: function(therapies, oldCompositionUid, newCompositionUid, ehrOrderName, status, doctorReviewNeeded, therapyEndsBeforeNextRounds, therapyStart, therapyEnd, todayIndex, forward)
  {
    var enums = app.views.medications.TherapyEnums;
    for (var i = 0; i < therapies.length; i++)
    {
      var therapyRow = therapies[i];
      for (var day in therapyRow.therapyFlowDayMap)
      {
        var dayTherapy = therapyRow.therapyFlowDayMap[day];                                   // [TherapyDayDto.java]
        var therapy = dayTherapy.therapy;
        var oldUidWithoutVersion = tm.views.medications.MedicationUtils.getUidWithoutVersion(oldCompositionUid);
        var newUidWithoutVersion = tm.views.medications.MedicationUtils.getUidWithoutVersion(therapy.compositionUid);
        if (oldUidWithoutVersion == newUidWithoutVersion)
        {
          therapy.compositionUid = newCompositionUid;
          if (therapy.ehrOrderName == ehrOrderName)
          {
            dayTherapy.therapyStatus = status;
            dayTherapy.doctorReviewNeeded = doctorReviewNeeded;
            dayTherapy.therapyEndsBeforeNextRounds = therapyEndsBeforeNextRounds;
            if (status == enums.therapyStatusEnum.ABORTED || status == enums.therapyStatusEnum.CANCELLED)
            {
              if (forward == null)
              {
                if (day > todayIndex)
                {
                  delete therapyRow.therapyFlowDayMap[day];
                }
              }
              else if (forward == true)
              {
                if (todayIndex == null || day > todayIndex)
                {
                  delete therapyRow.therapyFlowDayMap[day];
                }
              }
              else if (forward == false)
              {
                if (todayIndex != null && day > todayIndex)
                {
                  delete therapyRow.therapyFlowDayMap[day];
                }
              }
            }
            if (therapyStart)
            {
              therapy.start = therapyStart;
            }
            if (therapyEnd)
            {
              therapy.end = therapyEnd;
            }
          }
        }
      }
    }
  },

  /**
   * @param {Array<String>} routes
   * @returns {String}
   * @private
   */
  _createRowValueForRowDataRoutes: function(routes)
  {
    if (tm.jquery.Utils.isArray(routes))
    {
      if (routes.length > 0)
      {
        return routes.length === 1 ? routes[0] : this.getView().getDictionary("multiple.routes");
      }
      else
      {
        return "";
      }
    }

    return routes ? routes : "";
  },

  getGridCellTherapyDisplayProvider: function()
  {
    return this.gridCellTherapyDisplayProvider;
  },

  /**
   * Getters & Setters
   */

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /** public methods */
  reloadGridData: function()
  {
    var self = this;

    var viewHubNotifier = this.getView().getHubNotifier();
    var therapyFlowLoadHubAction = tm.views.medications.TherapyView.THERAPY_FLOW_LOAD_HUB;
    viewHubNotifier.actionStarted(therapyFlowLoadHubAction);

    var adjustedParams = this._getAdjustedSearchParams(this.searchDate, null);
    this._loadData(adjustedParams, function(gridData)
    {
      if (gridData.isEmpty())
      {
        self.noTherapiesField.show();
        self.grid.hide();
      }
      else
      {
        self.noTherapiesField.hide();
        self.grid.show();
        self.getView().updateCurrentBnfMaximumSum();
      }
      self._setGridData(gridData, adjustedParams);
      viewHubNotifier.actionEnded(therapyFlowLoadHubAction);
    });
    setTimeout(function()
    {
      self._loadPreviousDayData();
      self._loadNextDayData();
    }, 0);
  },

  changeSearchDate: function(forward)
  {
    var viewHubNotifier = this.getView().getHubNotifier();
    var therapyFlowNavigateHubAction = tm.views.medications.TherapyView.THERAPY_FLOW_NAVIGATE_HUB;
    viewHubNotifier.actionStarted(therapyFlowNavigateHubAction);

    var self = this;

    if (this.navigationLocked == true)
    {
      return;
    }
    this.navigationLocked = true;
    this._executeTaskWhenCondition(
        function()
        {
          if (forward)
          {
            return self.nextDayDataLoaded == true;
          }
          else
          {
            return self.previousDayDataLoaded == true;
          }
        },
        function()
        {
          var gridData;
          var adjustedParams;
          if (forward)
          {
            self.searchDate.setDate(self.searchDate.getDate() + 1);
            self.therapyFlowData = self.nextDayTherapyFlowData;
            gridData = self._buildGridData(self.therapyFlowData.therapyRows);
            adjustedParams = self._getAdjustedSearchParams(self.searchDate, true);
          }
          else
          {
            self.searchDate.setDate(self.searchDate.getDate() - 1);
            self.therapyFlowData = self.previousDayTherapyFlowData;
            gridData = self._buildGridData(self.therapyFlowData.therapyRows);
            adjustedParams = self._getAdjustedSearchParams(self.searchDate, false);
          }
          self.searchDate = adjustedParams.searchDate;
          self.todayIndex = adjustedParams.todayIndex;
          self._setGridData(gridData, adjustedParams);
          setTimeout(function()
          {
            self._loadPreviousDayData();
            self._loadNextDayData();
          }, 0);
          setTimeout(function()
          {
            self.navigationLocked = false;
          }, 0);
          viewHubNotifier.actionEnded(therapyFlowNavigateHubAction);
        });
  },

  repaintGrid: function(dayCount, searchDate, therapySortTypeEnum)
  {
    var self = this;

    this.removeAll(true);
    this.dayCount = dayCount;
    this.therapySortTypeEnum = therapySortTypeEnum;
    var adjustedParams = self._getAdjustedSearchParams(searchDate, null);
    this.searchDate = adjustedParams.searchDate;
    this.todayIndex = adjustedParams.todayIndex;

    this._paintGrid(this.searchDate);
    this.repaint();
    setTimeout(function()
    {
      self._loadPreviousDayData();
      self._loadNextDayData();
    }, 0);
  },

  paintGrid: function(dayCount, searchDate, groupField, therapySortTypeEnum)
  {
    var self = this;
    this.createGrouping(groupField);
    this.dayCount = dayCount;
    this.therapySortTypeEnum = therapySortTypeEnum;
    var adjustedParams = self._getAdjustedSearchParams(searchDate, null);
    this.searchDate = adjustedParams.searchDate;
    this.todayIndex = adjustedParams.todayIndex;
    this.removeAll(true);
    this._paintGrid(this.searchDate);
    this.repaint();
  },

  setGrouping: function(groupField)
  {
    this.createGrouping(groupField);
    if (this.grouping)
    {
      this.grid.setGrouping(this.grouping);
    }
    else
    {
      this.grid.removeGrouping();
    }
    var adjustedParams = this._getAdjustedSearchParams(this.searchDate, null);
  },

  createGrouping: function(groupField)
  {
    if (groupField)
    {
      this.grouping = new tm.jquery.GridGrouping({
        groupAlignment: 'right',
        groupField: [groupField],
        groupOrder: ['asc'],
        groupColumnShow: [false],
        groupSummary: [false]
      });
    }
    else
    {
      this.grouping = null;
    }
  },

  clear: function()
  {
    this.removeAll();
  },

  /* @Override */
  destroy: function()
  {
    this._abortIsGridReadyConditionalTask();
    this.callSuper();
  }
});