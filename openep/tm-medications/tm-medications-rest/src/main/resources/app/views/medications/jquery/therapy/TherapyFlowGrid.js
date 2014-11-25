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
  therapyDisplayProvider: null,
  therapyFlowData: null,
  previousDayTherapyFlowData: null,
  nextDayTherapyFlowData: null,
  previousDayDataLoaded: false,
  nextDayDataLoaded: false,
  navigationLocked: false,
  actionsQueue: [],
  therapyAction: null,
  /** privates: components */
  grid: null,

  /** constructor */
  Constructor: function(config)
  {
    config = tm.jquery.Utils.applyIf({
      cls: "therapy-flow-grid-container",
      layout: new tm.jquery.BorderLayout()
    }, config);

    this.callSuper(config);
    this.therapyActions = new app.views.medications.TherapyActions({view: config.view});
    this.therapyDisplayProvider = new app.views.medications.TherapyDisplayProvider({view: config.view});
  },

  /** private methods */
  _paintGrid: function(searchDate)
  {
    var self = this;

    this.grid = new tm.jquery.Grid({
      cls: 'therapy-grid',
      height: 300,
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
      grouping: self.grouping
    });

    this.grid.on(tm.jquery.ComponentEvent.EVENT_TYPE_GRID_COMPLETE, function(component)
    {
      if (this.getPlugin() != null)
      {
        self._afterUpdateDataEventHandler(component.getGridData())
      }
    });

    this.add(this.grid, {region: 'center'});
  },

  _afterUpdateDataEventHandler: function(data)
  {
    for (var i = 0; i < data.length; i++)
    {
      this._renderActionButtonsContainer(i, data[i]);
    }
  },

  _renderActionButtonsContainer: function(index, rowDataItem)
  {
    var self = this;
    var rowId = rowDataItem.id;

    if (this.view.isEditAllowed() == true)
    {
      var actionButtonsRenderToElement = $("#" + this._createActionButtonsContainerId(rowId))[0];
      if (!tm.jquery.Utils.isEmpty(actionButtonsRenderToElement))
      {
        $(actionButtonsRenderToElement).html("");
        var container = new tm.views.medications.TherapyFlowTodayButtons(
            {
              renderToElement: actionButtonsRenderToElement,
              view: self.view,
              rowId: rowId,
              dayTherapy: rowDataItem['column' + self.todayIndex] ? rowDataItem['column' + self.todayIndex].dayTherapy : null,
              actionFunction: function(compositionUid, orderName, rowId, action)
              {
                self._addActionToQueue(compositionUid, orderName, rowId, action); //actions: CONFIRM, ABORT, REISSUE
              }
            }
        );
        container.doRender();
      }
    }
  },

  _setGridData: function(gridData, adjustedParams)
  {
    var scrollPosition = $(this.grid.getScrollableElement()).scrollTop();
    this._fixGridColumns(adjustedParams, true);
    this.grid.setGridData(gridData);
    this._fixGridColumns(adjustedParams, false);
    $(this.grid.getScrollableElement()).scrollTop(scrollPosition);
    this.view.hideLoaderMask();
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

  _fixGridColumns: function(searchParams, reloadGrid)
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

    if (reloadGrid)
    {
      this.grid.getPlugin().trigger('reloadGrid');
    }

    var date = new Date(searchParams.searchDate);
    for (var i = 0; i < searchParams.dayCount; i++)
    {
      var headerId = '.therapy-flow-grid-container .therapy-grid #' + this.grid.id + '_grid_column' + i;
      var columnId = '.therapy-flow-grid-container .therapy-grid tr.jqgfirstrow td:nth-child(' + (5 + i) + ')';
      if (i == searchParams.todayIndex)
      {
        $(headerId).width(['50%']);
        $(columnId).width(['50%']);
      }
      else
      {
        $(headerId).width(['25%']);
        $(columnId).width(['25%']);
      }
      var headerLabel = this._getHeaderString(date, i == searchParams.todayIndex, i);
      this.grid.setHeaderLabel('column' + i, headerLabel);
      date.setDate(date.getDate() + 1);
    }
  },

  _loadData: function(searchParams, callback)
  {
    var self = this;
    var findTherapyFlowDataUrl = self.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_LOAD_THERAPY_FLOW_DATA;

    this.view.showLoaderMask();
    var params = {
      patientId: self.view.getPatientId(),
      centralCaseId: self.view.getCentralCaseData() && self.view.getCentralCaseData().centralCaseId ?
          self.view.getCentralCaseData().centralCaseId : null,
      patientHeight: this.view.getPatientData().heightInCm,
      startDate: searchParams.searchDate.getTime(),
      dayCount: searchParams.dayCount,
      todayIndex: searchParams.todayIndex != null ? searchParams.todayIndex : -1,
      roundsInterval: JSON.stringify(self.view.getRoundsInterval()),
      therapySortTypeEnum: this.therapySortTypeEnum,
      knownOrganizationalEntity: self.view.getKnownOrganizationalEntity()
    };
    self.view.loadViewData(findTherapyFlowDataUrl, params, null, function(therapyFlowData)
    {
      self.therapyFlowData = therapyFlowData;
      var gridData = self._buildGridData(therapyFlowData.therapyRows);
      callback(gridData);
      self.view.hideLoaderMask(self.view, null, 5000);
    });
  },

  _buildGridData: function(therapies)
  {
    var self = this;
    var gridData = [];
    for (var i = 0; i < therapies.length; i++)
    {
      var rowData = therapies[i];
      var row = {
        id: i,
        atcGroup: rowData.atcGroupName ? rowData.atcGroupName + ' (' + rowData.atcGroupCode + ')' : self.view.getDictionary("without.atc"),
        route: rowData.route,
        customGroup: tm.views.medications.MedicationUtils.getTherapyCustomGroupDisplayName(rowData, this.view)
      };

      for (var j = 0; j < self.dayCount + 1; j++)
      {
        var cellData = rowData.therapyFlowDayMap[j];
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

    var previousDate = new Date(this.searchDate);
    previousDate.setDate(previousDate.getDate() - 1);
    var adjustedParams = self._getAdjustedSearchParams(previousDate, false);

    var params = {
      patientId: self.view.getPatientId(),
      centralCaseId: self.view.getCentralCaseData() && self.view.getCentralCaseData().centralCaseId ?
          self.view.getCentralCaseData().centralCaseId : null,
      patientHeight: this.view.getPatientData().heightInCm,
      startDate: adjustedParams.searchDate.getTime(),
      dayCount: adjustedParams.dayCount,
      todayIndex: adjustedParams.todayIndex != null ? adjustedParams.todayIndex : -1,
      roundsInterval: JSON.stringify(self.view.getRoundsInterval()),
      therapySortTypeEnum: this.therapySortTypeEnum,
      knownOrganizationalEntity: self.view.getKnownOrganizationalEntity()
    };

    var findTherapyFlowDataUrl = self.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_LOAD_THERAPY_FLOW_DATA;
    self.view.loadViewData(findTherapyFlowDataUrl, params, null, function(therapyFlowData)
    {
      self.previousDayTherapyFlowData = therapyFlowData;
      self.previousDayDataLoaded = true
    });
  },

  _loadNextDayData: function()
  {
    var self = this;
    this.nextDayDataLoaded = false;

    var nextDate = new Date(this.searchDate);
    nextDate.setDate(nextDate.getDate() + 1);
    var adjustedParams = self._getAdjustedSearchParams(nextDate, true);

    var params = {
      patientId: self.view.getPatientId(),
      centralCaseId: self.view.getCentralCaseData() && self.view.getCentralCaseData().centralCaseId ?
          self.view.getCentralCaseData().centralCaseId : null,
      patientHeight: this.view.getPatientData().heightInCm,
      startDate: adjustedParams.searchDate.getTime(),
      dayCount: adjustedParams.dayCount,
      todayIndex: adjustedParams.todayIndex != null ? adjustedParams.todayIndex : -1,
      roundsInterval: JSON.stringify(self.view.getRoundsInterval()),
      therapySortTypeEnum: this.therapySortTypeEnum,
      knownOrganizationalEntity: self.view.getKnownOrganizationalEntity()
    };

    var findTherapyFlowDataUrl = self.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_LOAD_THERAPY_FLOW_DATA;
    self.view.loadViewData(findTherapyFlowDataUrl, params, null, function(therapyFlowData)
    {
      self.nextDayTherapyFlowData = therapyFlowData;
      self.nextDayDataLoaded = true
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
    columns.push("route");
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
    var dayString =  Globalize.format(date, "L");
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
          "<div class='TextData'; style='float:left; width:33%; text-align:center; overflow: visible;'> <b>" + this.view.getDictionary("today") + " " + dayString + "</b></div>";
    }

    return headerString +
        "<div class='TextData'; style='float:left; width:33%; text-align:center; overflow: visible;'> " + dayString + "</div>";
  },

  _getTodayIndex: function(dayCount, searchDate)
  {
    var date = new Date(searchDate);
    var today = new Date();
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
      hidden: true});
    columns.push({
      name: 'atcGroup',
      index: 'atcGroup',
      width: 0,
      hidden: true});
    columns.push({
      name: 'route',
      index: 'route',
      width: 0,
      hidden: true});
    columns.push({
      name: 'customGroup',
      index: 'customGroup',
      width: 0,
      hidden: true});

    for (var i = 0; i < this.dayCount + 1; i++)
    {
      var columnWidth = i == self.todayIndex ? '50%' : '25%';
      columns.push({
            name: 'column' + i,
            index: i,
            width: columnWidth,
            sortable: false,
            title: false,
            hidden: self.todayIndex > 0 && i == self.dayCount,
            formatter: new tm.jquery.GridCellFormatter({
              content: function(cellvalue, options)
              {
                if (cellvalue)
                {
                  if (options.colModel.index == self.todayIndex)
                  {
                    return self._createTodayCellHtmlTemplate(options.rowId, self.todayIndex, cellvalue.dayTherapy);
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
              if (dayTherapy && dayTherapy.active && dayTherapy.therapyStatus != enums.therapyStatusEnum.ABORTED && dayTherapy.therapyStatus != enums.therapyStatusEnum.CANCELLED && dayTherapy.therapyStatus != enums.therapyStatusEnum.SUSPENDED)
              {
                return 'style="vertical-align:top; background:#fff';
              }
              else
              {
                return 'style="vertical-align:top;background:#E2DFDF"';
              }
            }
          }
      );
    }
    return columns;
  },

  _createCellHtmlTemplate: function(rowId, columnIndex, dayTherapy)
  {
    var mainContainer = new tm.jquery.Container({
      cls: 'cell-item',
      padding: dayTherapy.showConsecutiveDay ? "5 5 5 3" : 5,
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create('start', 'start'),
      style: 'height:100%;'
    });

    if (dayTherapy.showConsecutiveDay)
    {
      mainContainer.add(new tm.jquery.Container({
        cls: "icon_day_number",
        layout: tm.jquery.VFlexboxLayout.create('end', 'end'),
        html: dayTherapy.consecutiveDay
      }));
    }

    mainContainer.add(new tm.jquery.Container({
      flex: 1,
      scrollable: 'visible',
      html: dayTherapy.therapy.formattedTherapyDisplay,
      cls: 'TherapyDescription ShortDescription'
    }));

    var therapyTaggedForPrescription = tm.views.medications.MedicationUtils.isTherapyTaggedForPrescription(dayTherapy.therapy.tags);
    var contextMenu = this._createTherapyContextMenu(dayTherapy, rowId, false, therapyTaggedForPrescription);
    if (contextMenu)
    {
      mainContainer.setContextMenu(contextMenu);
    }

    return mainContainer;
  },

  _createTodayCellHtmlTemplate: function(rowId, columnIndex, dayTherapy)      // [TherapyDayDto.java]
  {
    var appFactory = this.view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;
    var statusIcon = this.therapyDisplayProvider.getStatusIcon(dayTherapy.therapyStatus);
    var validTo = dayTherapy.therapy.end ? Globalize.format(new Date(dayTherapy.therapy.end.data), "K") : '...';
    //var validTo = dayTherapy.therapy.end ? new Date(dayTherapy.therapy.end.data).format("dd.mm.yyyy HH:MM") : '...';

    var style = 'height:100%; position:relative;';
    if (dayTherapy.active && dayTherapy.therapyStatus != enums.therapyStatusEnum.ABORTED && dayTherapy.therapyStatus != enums.therapyStatusEnum.CANCELLED && dayTherapy.therapyStatus != enums.therapyStatusEnum.SUSPENDED)
    {
      style += 'background:#fff;';
    }
    else
    {
      style += 'background:#E2DFDF;';
    }

    var id = 'id' + rowId + columnIndex;
    var container = new tm.jquery.Container({cls: 'today-cell-item', id: "", layout: appFactory.createDefaultHFlexboxLayout('start', 'stretch'), style: style});

    var isTherapyTaggedForPrescription = tm.views.medications.MedicationUtils.isTherapyTaggedForPrescription(dayTherapy.therapy.tags);
    var contextMenu = this._createTherapyContextMenu(dayTherapy, rowId, true, isTherapyTaggedForPrescription);
    if (contextMenu)
    {
      container.setContextMenu(contextMenu);
    }

    var options = {
      background: {cls: tm.views.medications.MedicationUtils.getTherapyIcon(dayTherapy.therapy)},
      layers: []
    };

    //todo something different to mark linked therapies
    if (dayTherapy.therapy != null && (dayTherapy.therapy.linkFromTherapy || dayTherapy.therapy.linkToTherapy))
    {
      var link = dayTherapy.therapy.linkFromTherapy ? dayTherapy.therapy.linkFromTherapy : dayTherapy.therapy.linkToTherapy;
      if (link.length <= 2)
      {
        options.layers.push({hpos: "left", vpos: "bottom", cls: "icon_link", html: link});
      }
    }
    if (dayTherapy.modifiedFromLastReview)
    {
      options.layers.push({hpos: "left", vpos: "top", cls: "icon_changed"});
    }
    if (dayTherapy.showConsecutiveDay)
    {
      options.layers.push({hpos: "right", vpos: "top", cls: "icon_day_number", html: dayTherapy.consecutiveDay});
    }
    if (dayTherapy.therapy.criticalWarnings.length > 0)
    {
      options.layers.push({hpos: "center", vpos: "center", cls: "icon_warning"});
    }
    options.layers.push({hpos: "right", vpos: "bottom", cls: statusIcon});

    var iconContainer = new tm.jquery.Container({
      cursor: "pointer",
      margin: 5,
      width: 48,
      height: 48,
      html: appFactory.createLayersContainerHtml(options)
    });
    
    var self = this;
    iconContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
    {
      var now = new Date();
      var today = {
        startMillis: new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0).getTime(),
        endMillis: new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0, 0).getTime()
      };
      var tooltip = tm.views.medications.MedicationUtils.createTherapyDetailsCardTooltip(
          dayTherapy, self.grid.getScrollableElement(), self.view, today);
      tooltip.setTrigger("manual");
      iconContainer.setTooltip(tooltip);
      setTimeout(function()
      {
        tooltip.show();
      }, 200);
    });
    container.add(iconContainer);

    var html = '';

    html += '<div style="float:right; padding-left: 10px">';
    html += '<div style="text-align:right; float: right;">' + validTo + '</div>';
    if (isTherapyTaggedForPrescription)
    {
      html += '<div class="icon_prescription"></div>';
    }

    if (dayTherapy.therapyActionsAllowed)
    {
      html += '<div class="action-buttons" style="clear:both;" id="' + this._createActionButtonsContainerId(rowId) + '"></div>';
    }

    html += '</div>';
    html += '<div class="TherapyDescription">' + dayTherapy.therapy.formattedTherapyDisplay + '</div>';

    var contentContainer = new tm.jquery.Container({
      flex: 1,
      padding: 5,
      html: html
    });
    container.add(contentContainer);

    if (dayTherapy.therapyEndsBeforeNextRounds)
    {
      contentContainer.setStyle('border-right: 5px solid grey;');
    }

    return container;
  },

  _createEmptyCellHtmlTemplate: function()
  {
    var appFactory = this.view.getAppFactory();
    return new tm.jquery.Container({
      cls: 'cell-item',
      layout: appFactory.createDefaultHFlexboxLayout('start', 'stretch'),
      style: "position:relative; white-space: normal; background:#E2DFDF; height:100%"
    });
  },

  _createActionButtonsContainerId: function(rowId)
  {
    return this.view.getViewId() + "_action_buttons_" + rowId;
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

  _createTherapyContextMenu: function(dayTherapy, rowId, today, therapyTaggedForPrescription)     // [TherapyDayDto.java]
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;

    var therapy = dayTherapy.therapy;
    var ehrOrderName = dayTherapy.therapy.ehrOrderName;
    var ehrCompositionId = dayTherapy.therapy.compositionUid;
    var therapyStatus = dayTherapy.therapyStatus;
    var orderFormType = dayTherapy.therapy.medicationOrderFormType;

    if (therapy == null || !this.view.isEditAllowed())
    {
      return null
    }

    var contextMenu = appFactory.createContextMenu();

    if (today && therapyStatus != enums.therapyStatusEnum.ABORTED && therapyStatus != enums.therapyStatusEnum.CANCELLED)
    {
      if (orderFormType != enums.medicationOrderFormType.COMPLEX)
      {
        var menuItemEdit = new tm.jquery.MenuItem({text: self.view.getDictionary("edit"), /*iconCls: 'icon-edit',*/
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
        contextMenu.addMenuItem(menuItemEdit);
      }
      else if (orderFormType == enums.medicationOrderFormType.COMPLEX)
      {
        var menuItemAdjustSpeed = new tm.jquery.MenuItem({text: self.view.getDictionary("edit"), /* iconCls: 'icon-edit',*/
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

        contextMenu.addMenuItem(menuItemAdjustSpeed);
      }
    }

    if (today && therapyStatus != enums.therapyStatusEnum.ABORTED && therapyStatus != enums.therapyStatusEnum.CANCELLED)
    {
      var menuItemDelete = new tm.jquery.MenuItem({text: self.view.getDictionary("abort.therapy"),/* iconCls: 'icon-delete', */
        handler: function()
        {
          self._addActionToQueue(ehrCompositionId, ehrOrderName, rowId, 'ABORT');
        }});
      contextMenu.addMenuItem(menuItemDelete);

      if (therapyStatus != 'SUSPENDED')
      {
        var menuItemSuspend = new tm.jquery.MenuItem({text: self.view.getDictionary("suspend"), /*iconCls: 'icon-suspend', */
          handler: function()
          {
            self._addActionToQueue(ehrCompositionId, ehrOrderName, rowId, 'SUSPEND');
          }});
        contextMenu.addMenuItem(menuItemSuspend);
      }

      var centralCaseExists = self.view.getCentralCaseData() && self.view.getCentralCaseData().centralCaseId;

      if (centralCaseExists)
      {
        if (therapyTaggedForPrescription)
        {
          var menuItemTag = new tm.jquery.MenuItem({text: self.view.getDictionary("untag.therapy.for.prescription"),
            handler: function()
            {
              self.view.untagTherapyForPrescription(self.view.getPatientId(), self.view.getCentralCaseData().centralCaseId, ehrCompositionId, ehrOrderName);
            }});
          contextMenu.addMenuItem(menuItemTag);
        }
        else
        {
          var menuItemUntag = new tm.jquery.MenuItem({text: self.view.getDictionary("tag.therapy.for.prescription"),
            handler: function()
            {
              self.view.tagTherapyForPrescription(self.view.getPatientId(), self.view.getCentralCaseData().centralCaseId, ehrCompositionId, ehrOrderName);
            }});
          contextMenu.addMenuItem(menuItemUntag);
        }
      }

      if (dayTherapy.showConsecutiveDay && !dayTherapy.modified)
      {
        var menuItemConsecutive = new tm.jquery.MenuItem({text: self.view.getDictionary("consecutive.days.edit"), /*iconCls: 'icon-edit-consecutive-days',*/
          handler: function()
          {
            self.view.showEditConsecutiveDaysDialog(dayTherapy.therapy.pastDaysOfTherapy, ehrCompositionId, ehrOrderName);
          }});
        contextMenu.addMenuItem(menuItemConsecutive);
      }
    }

    if (orderFormType != enums.medicationOrderFormType.COMPLEX && therapy.medication.id != null)
    {
      var menuItemCopySimpleTherapy = new tm.jquery.MenuItem({text: self.view.getDictionary("copy"), /*iconCls: 'icon-copy', */
        handler: function()
        {
          self.view.showEditSimpleTherapyDialog(therapy, true);
        }});
      contextMenu.addMenuItem(menuItemCopySimpleTherapy);
    }
    else if (orderFormType == enums.medicationOrderFormType.COMPLEX && therapy.ingredientsList[0].medication.id != null)
    {
      var menuItemCopyComplexTherapy = new tm.jquery.MenuItem({text: self.view.getDictionary("copy"), /*iconCls: 'icon-copy', */
        handler: function()
        {
          self.view.showEditComplexTherapyDialog(therapy, true);
        }});
      contextMenu.addMenuItem(menuItemCopyComplexTherapy);
    }

    if (today && therapyStatus != enums.therapyStatusEnum.ABORTED && therapyStatus != enums.therapyStatusEnum.CANCELLED)
    {
      var menuItemSuspendAll = new tm.jquery.MenuItem({text: self.view.getDictionary("suspend.all"),/* iconCls: 'icon-suspend-all', */
        handler: function()
        {
          self._suspendAllTherapies();
        }});
      contextMenu.addMenuItem(menuItemSuspendAll);
    }

    return contextMenu;
  },

  _addActionToQueue: function(ehrCompositionId, ehrOrderName, rowIndex, action)
  {
    this.actionsQueue.push({ehrCompositionId: ehrCompositionId, ehrOrderName: ehrOrderName, rowIndex: rowIndex, action: action});
    this._executeTasks(true);
  },

  _executeTasks: function(newTask)
  {
    var self = this;
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
        this.therapyActions.abortTherapy(nextTask.ehrCompositionId, nextTask.ehrOrderName,
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
        this.therapyActions.suspendTherapy(nextTask.ehrCompositionId, nextTask.ehrOrderName,
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
    }
  },

  _reloadSingleTherapyChanges: function(ehrCompositionId, ehrOrderName, rowIndex, callback)
  {
    var self = this;
    var reloadUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_RELOAD_SINGLE_THERAPY_AFTER_ACTION;
    var uidWithoutVersion = this.view.getUidWithoutVersion(ehrCompositionId);
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: ehrOrderName,
      roundsInterval: JSON.stringify(self.view.getRoundsInterval())
    };
    this.view.loadViewData(reloadUrl, params, null,
        function(reloadAfterActionDto) //TherapyReloadAfterActionDto.java
        {
          self._updateAllTherapiesCompositionUid(
              ehrCompositionId,
              reloadAfterActionDto.ehrCompositionId,
              reloadAfterActionDto.ehrOrderName,
              reloadAfterActionDto.therapyStatus,
              reloadAfterActionDto.therapyActionsAllowed,
              reloadAfterActionDto.therapyEndsBeforeNextRounds,
              reloadAfterActionDto.therapyStart,
              reloadAfterActionDto.therapyEnd
          );
          self._refreshTherapiesAfterAction(rowIndex);
          callback();
        },
        function()
        {
          callback()
        });
  },

  _refreshTherapiesAfterAction: function(rowIndex)
  {
    var gridData = this._buildGridData(this.therapyFlowData.therapyRows);
    this.grid.setRowData(rowIndex, gridData[rowIndex]);
    this._renderActionButtonsContainer(rowIndex, gridData[rowIndex])
  },

  _suspendAllTherapies: function()
  {
    var self = this;
    this.view.showLoaderMask(self.view, null, 5000);
    var suspendAllTherapiesUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SUSPEND_ALL_THERAPIES;
    var params = {patientId: this.view.getPatientId()};
    this.view.loadPostViewData(suspendAllTherapiesUrl, params, null, function()
    {
      self.view.refreshTherapies(false);
    });
  },

  _updateAllTherapiesCompositionUid: function(
      oldCompositionUid,
      newCompositionUid,
      ehrOrderName,
      status,
      therapyActionsAllowed,
      therapyEndsBeforeNextRounds,
      therapyStart,
      therapyEnd)
  {
    this._updateTherapiesCompositionUid(
        this.therapyFlowData.therapyRows,
        oldCompositionUid,
        newCompositionUid,
        ehrOrderName,
        status,
        therapyActionsAllowed,
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
        therapyActionsAllowed,
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
        therapyActionsAllowed,
        therapyEndsBeforeNextRounds,
        therapyStart,
        therapyEnd,
        nextDaySearchParameters.todayIndex,
        true
    );
  },

  _updateTherapiesCompositionUid: function(
      therapies,
      oldCompositionUid,
      newCompositionUid,
      ehrOrderName,
      status,
      therapyActionsAllowed,
      therapyEndsBeforeNextRounds,
      therapyStart,
      therapyEnd,
      todayIndex,
      forward)
  {
    var enums = app.views.medications.TherapyEnums;
    for (var i = 0; i < therapies.length; i++)
    {
      var therapyRow = therapies[i];
      for (var day in therapyRow.therapyFlowDayMap)
      {
        var dayTherapy = therapyRow.therapyFlowDayMap[day];                                   // [TherapyDayDto.java]
        var therapy = dayTherapy.therapy;
        var oldUidWithoutVersion = this.view.getUidWithoutVersion(oldCompositionUid);
        var newUidWithoutVersion = this.view.getUidWithoutVersion(therapy.compositionUid);
        if (oldUidWithoutVersion == newUidWithoutVersion)
        {
          therapy.compositionUid = newCompositionUid;
          if (therapy.ehrOrderName == ehrOrderName)
          {
            dayTherapy.therapyStatus = status;
            dayTherapy.therapyActionsAllowed = therapyActionsAllowed;
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

  /** public methods */
  reloadGridData: function()
  {
    var self = this;

    var viewHubNotifier = this.view.getHubNotifier();
    var therapyFlowLoadHubAction = tm.views.medications.TherapyView.THERAPY_FLOW_LOAD_HUB;
    viewHubNotifier.actionStarted(therapyFlowLoadHubAction);

    var adjustedParams = this._getAdjustedSearchParams(this.searchDate, null);
    this._loadData(adjustedParams, function(gridData)
    {
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
    var viewHubNotifier = this.view.getHubNotifier();
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
    this._fixGridColumns(adjustedParams, true);
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
  }
});