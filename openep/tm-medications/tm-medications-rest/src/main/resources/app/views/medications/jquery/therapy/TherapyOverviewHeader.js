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

Class.define('tm.views.medications.TherapyOverviewHeader', 'tm.jquery.Container', {

  scrollable: 'visible',
  OTHER_CUSTOM_GROUP: null,
  ALL_CUSTOM_GROUPS: null,

  /** configs */
  mode: "GRID", //GRID, TIMELINE
  state: null,
  view: null,
  timelineFilterChangedFunction: null, //optional
  /** privates */
  customGroups: null,
  selectedRoutes: null,
  selectedCustomGroups: null,
  /** privates: components */
  orderButton: null,
  previousHospitalizationTherapiesButton: null,
  printButton: null,
  sortButton: null,
  /** privates: components - grid */
  weightButton: null,
  previousButton: null,
  nextButton: null,
  dayCountButtonGroup: null,
  /** privates: components - timeline */
  routesFilterContainer: null,
  routesFilterButton: null,
  customGroupFilterContainer: null,
  customGroupFilterButton: null,
  referenceWeightContainer: null,

  //sort
  therapySortTypeEnum: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.OTHER_CUSTOM_GROUP = this.view.getDictionary("other.undef");
    this.ALL_CUSTOM_GROUPS = this.view.getDictionary("all.groups.short");
    this.selectedRoutes = [];
    this.selectedCustomGroups = [];
    var appFactory = this.view.getAppFactory();
    this.setLayout(appFactory.createDefaultHFlexboxLayout("justify", "stretch"));
    this._buildGui();
  },

  /** private methods */
  _buildGui: function()
  {
    this._addWestContainer();
    if (this.mode == "GRID")
    {
      this._addCenterGridContainer();
      this._addEastGridContainer();
    }
    else if (this.mode == "TIMELINE")
    {
      this._addCenterTimelineContainer();
      this._addEastTimelineContainer();
    }
  },

  _addWestContainer: function()
  {
    var self = this;

    var container = new tm.jquery.Container({
      scrollable: 'visible',
      padding: 5,
      layout: tm.jquery.HFlexboxLayout.create("end", "center", 5)
    });

    var orderPopupMenu = new tm.jquery.PopupMenu();
    orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
      iconCls: "icon-add",
      text: self.view.getDictionary("medications.ordering"),
      handler: function()
      {
        self.view.openMedicationOrderingDialog();
      }
    }));
    orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
      iconCls: "icon-add",
      text: self.view.getDictionary('universal.form') + ' - ' + self.view.getDictionary('simple1').toLowerCase(),
      handler: function()
      {
        self.view.openUniversalSimpleTherapyDialog(null);
      }
    }));
    orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
      iconCls: "icon-add",
      text: self.view.getDictionary('universal.form') + ' - ' + self.view.getDictionary('expanded1').toLowerCase(),
      handler: function()
      {
        self.view.openUniversalComplexTherapyDialog(null);
      }
    }));
    orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
      iconCls: "icon-add-to-24",
      text: self.view.getDictionary('therapy.transfer'),
      handler: function()
      {
        self.view.orderPreviousHospitalizationTherapies();
      }
    }));
    this.orderButton = new tm.jquery.SplitButton({
      iconCls: "icon-add",
      popupMenu: orderPopupMenu,
      handler: function()
      {
        self.view.openMedicationOrderingDialog();
      }
    });
    container.add(this.orderButton);
    this.previousHospitalizationTherapiesButton = new tm.jquery.Button({
      iconCls: "icon-add-to-24",
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("therapy.transfer"), "bottom", this.view),
      handler: function()
      {
        self.view.orderPreviousHospitalizationTherapies();
      }
    });
    this.previousHospitalizationTherapiesButton.hide();
    container.add(this.previousHospitalizationTherapiesButton);

    this.weightButton = new tm.jquery.Button({
      iconCls: "icon-reference-weight",
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("reference.weight"), "bottom", this.view),
      handler: function()
      {
        self.view.openReferenceWeightDialog(null, null);
      }
    });
    container.add(this.weightButton);

    var printPopupMenu = new tm.jquery.PopupMenu();
    printPopupMenu.addMenuItem(new tm.jquery.MenuItem({
      iconCls: "icon-print",
      text: this.view.getDictionary("print.therapy.report"),
      handler: function()
      {
        self._printTodayTherapyReport();
      }
    }));
    printPopupMenu.addMenuItem(new tm.jquery.MenuItem({
      iconCls: "icon-print",
      text: this.view.getDictionary("empty.therapy.report"),
      handler: function()
      {
        self._printEmptyTherapyReport();
      }
    }));
    this.printButton = new tm.jquery.SplitButton({
      iconCls: "icon-print",
      popupMenu: printPopupMenu
    });
    this.printButton.setTooltip(tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("print.therapy.report"), null, this.view));
    this.printButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._printTodayTherapyReport();
    });
    container.add(this.printButton);

    this.add(container);
  },

  _printTodayTherapyReport: function()
  {
    var centralCaseData = this.view.getCentralCaseData(); //TherapyCentralCaseData
    ViewManager.getInstance().sendAction(
        this.view,
        ViewAction.create("printTodayTherapyReport")
            .addParam("action", "printTodayTherapyReport")
            .addParam("patientId", this.view.getPatientId())
            .addParam("knownOrganizationalEntity", centralCaseData && centralCaseData.knownOrganizationalEntityName ? centralCaseData.knownOrganizationalEntityName : null)
            .addParam("sessionId", centralCaseData && centralCaseData.sessionId ? centralCaseData.sessionId : null)
            .addParam("encounterId", centralCaseData && centralCaseData.encounterId ? centralCaseData.encounterId : null)
            .addParam("careProvisionType", centralCaseData && centralCaseData.careProvisionType ? centralCaseData.careProvisionType : null)
            .addParam("patientHeight", this.view.getPatientData().heightInCm)
    );
  },

  _printEmptyTherapyReport: function()
  {
    var centralCaseData = this.view.getCentralCaseData(); //TherapyCentralCaseData
    ViewManager.getInstance().sendAction(
        this.view,
        ViewAction.create("printEmptyTherapyReport")
            .addParam("action", "printEmptyTherapyReport")
            .addParam("patientId", this.view.getPatientId())
            .addParam("encounterId", centralCaseData && centralCaseData.encounterId ? centralCaseData.encounterId : null)
            .addParam("careProvisionType", centralCaseData && centralCaseData.careProvisionType ? centralCaseData.careProvisionType : null)
    );
  },

  _addCenterGridContainer: function()
  {
    var centerContainer = new tm.jquery.Container({
      layout: new tm.jquery.CenterLayout(),
      flex: 1,
      scrollable: 'visible'
    });

    var container = new tm.jquery.Container({
      padding: 5,
      width: 200,
      layout: tm.jquery.HFlexboxLayout.create("start", "center", 5)
    });

    this.previousButton = new tm.jquery.Button({cls: "left-icon", size: 'medium', type: 'filter'});
    this.nextButton = new tm.jquery.Button({cls: "right-icon", size: 'medium', type: 'filter'});
    container.add(this.previousButton);
    container.add(this.nextButton);

    centerContainer.add(container);
    this.add(centerContainer);
  },

  _addEastGridContainer: function()
  {
    var container = new tm.jquery.Container({
      padding: 5,
      layout: tm.jquery.HFlexboxLayout.create("end", "center", 5),
      scrollable: 'visible'
    });

    var numberOfDaysMode = this.view.getContext() ? this.view.getContext().numberOfDaysMode : null;
    var threeDaysButton = new tm.jquery.Button({
      text: "3",
      dayCount: 3,
      type: 'filter',
      mode: 'numberOfDaysMode3',
      pressed: numberOfDaysMode == null || numberOfDaysMode == 'numberOfDaysMode3'
    });
    var fiveDaysButton = new tm.jquery.Button({
      text: "5",
      dayCount: 5,
      type: 'filter',
      mode: 'numberOfDaysMode5',
      pressed: numberOfDaysMode == 'numberOfDaysMode5'
    });

    this.dayCountButtonGroup = new tm.jquery.ButtonGroup({
      orientation: "horizontal",
      type: "radio",
      buttons: [threeDaysButton, fiveDaysButton]
    });
    this.groupingButtonGroup = this._buildGroupingButtonGroup();

    this._buildSortSplitButton();

    container.add(this.dayCountButtonGroup);
    container.add(this.groupingButtonGroup);
    container.add(this.sortButton);
    this.add(container);
  },

  _buildGroupingButtonGroup: function()
  {
    var groupByMode = this.view.getContext() ? this.view.getContext().groupByMode : null;
    var noGroupButton = new tm.jquery.Button({
      cls: 'no-group-icon',
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("no.grouping"), "left", this.view),
      groupField: null,
      type: 'filter',
      mode: 'groupByModeNone',
      pressed: groupByMode == null || groupByMode == 'groupByModeNone'
    });
    var groupByAtcButton = new tm.jquery.Button({
      text: "ATC",
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("group.by.atc.classification"), "left", this.view),
      groupField: "atcGroup",
      type: 'filter',
      mode: 'groupByModeAtc',
      pressed: groupByMode == 'groupByModeAtc'
    });
    var groupByRouteButton = new tm.jquery.Button({
      text: "APL",
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("group.by.route"), "left", this.view),
      groupField: "route",
      type: 'filter',
      mode: 'groupByModeRoute',
      pressed: groupByMode == 'groupByModeRoute'
    });
    var groupByModeCustomGroupButton = new tm.jquery.Button({
      cls: 'custom-group-icon',
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("group.by.custom.groups"), "left", this.view),
      groupField: "customGroup",
      type: 'filter',
      mode: 'groupByModeCustomGroup',
      pressed: groupByMode == 'groupByModeCustomGroup'
    });

    return new tm.jquery.ButtonGroup({
      orientation: "horizontal",
      type: "radio",
      buttons: [noGroupButton, groupByAtcButton, groupByRouteButton, groupByModeCustomGroupButton]
    });
  },

  _buildSortSplitButton: function()
  {
    //creates this.sortButton as createRadioSplitButton
    var self = this;
    var view = this.view;
    var appFactory = view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;

    this.sortButton = appFactory.createRadioSplitButton({
      cls: "therapy-sort-button",
      showSelectedItemsText: false,
      popupMenuHorizontalAlignment: "right"
    });

    this.sortButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
      iconCls: "icon-sort-name-asc",
      text: "DESCRIPTION_ASC",
      checked: self.therapySortTypeEnum == enums.therapySortTypeEnum.DESCRIPTION_ASC,
      handler: null,
      data: enums.therapySortTypeEnum.DESCRIPTION_ASC
    }));
    this.sortButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
      iconCls: "icon-sort-name-desc",
      text: "DESCRIPTION_DESC",
      checked: self.therapySortTypeEnum == enums.therapySortTypeEnum.DESCRIPTION_DESC,
      handler: null,
      data: enums.therapySortTypeEnum.DESCRIPTION_DESC
    }));
    this.sortButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
      iconCls: "icon-sort-time-asc",
      text: "CREATED_TIME_ASC",
      checked: self.therapySortTypeEnum == enums.therapySortTypeEnum.CREATED_TIME_ASC,
      handler: null,
      data: enums.therapySortTypeEnum.CREATED_TIME_ASC
    }));
    this.sortButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
      iconCls: "icon-sort-time-desc",
      text: "CREATED_TIME_DESC",
      checked: self.therapySortTypeEnum == enums.therapySortTypeEnum.CREATED_TIME_DESC,
      handler: null,
      data: enums.therapySortTypeEnum.CREATED_TIME_DESC
    }));
  },

  _addCenterTimelineContainer: function()
  {
    this.referenceWeightContainer = new tm.jquery.Container({
      cls: 'TextLabel',
      flex: 1,
      layout: tm.jquery.HFlexboxLayout.create("start", "center"),
      padding: "13 0 0 0"
    });
    this.add(this.referenceWeightContainer);
    this._showReferenceWeight();
  },

  _showReferenceWeight: function()
  {
    var referenceWeightString = "";
    if (this.view.getReferenceWeight())
    {
      var referenceWeightValueString = tm.views.medications.MedicationUtils.doubleToString(this.view.getReferenceWeight(), 'n3') + ' kg';
      referenceWeightString = "<span style='text-transform: none; color: #63818D'>" + this.view.getDictionary('reference.weight') + ": " + referenceWeightValueString + "</span";
    }
    this.referenceWeightContainer.setHtml(referenceWeightString);
  },

  _addEastTimelineContainer: function()
  {
    this.routesFilterContainer = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch")
    });
    this.add(this.routesFilterContainer);

    this.customGroupFilterContainer = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch")
    });

    this.groupingButtonGroup = this._buildGroupingButtonGroup();
    this.groupingButtonGroup.setPadding(5);

    this._buildSortSplitButton();
    this.sortButton.setPadding(5);

    this.add(this.customGroupFilterContainer);
    this.add(this.groupingButtonGroup);
    this.add(this.sortButton);
  },

  _timelineFilterChanged: function(refreshTimelines)
  {
    this.selectedRoutes = this._getSelectedRoutes();
    this.selectedCustomGroups = this._getSelectedCustomGroups();
    if (this.timelineFilterChangedFunction)
    {
      this.timelineFilterChangedFunction(this.selectedRoutes, this.selectedCustomGroups, refreshTimelines);
    }
  },

  _getSelectedRoutes: function()
  {
    var selectedRoutes = [];
    if (this.routesFilterButton)
    {
      var selections = this.routesFilterButton.getSelections();
      for (var i = 0; i < selections.length; i++)
      {
        if (selections[i].data != null)
        {
          selectedRoutes.push(selections[i].data);
        }
      }
    }
    return selectedRoutes;
  },

  _getSelectedCustomGroups: function()
  {
    var selectedCustomGroups = [];
    if (this.customGroupFilterButton)
    {
      var selections = this.customGroupFilterButton.getSelections();
      for (var i = 0; i < selections.length; i++)
      {
        if (selections[i].text != this.ALL_CUSTOM_GROUPS)
        {
          selectedCustomGroups.push(selections[i].data);
        }
      }
    }
    return selectedCustomGroups;
  },

  _setupRoutesFilter: function(therapyTimelines)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    this.routesFilterContainer.removeAll();

    this.routesFilterButton = appFactory.createCheckboxSplitButton({
      margin: 5,
      showSelectedItemsIcon: false,
      showSelectedItemsText: true,
      popupMenuHorizontalAlignment: "right",
      clearSelectedCheckBoxItemText: this.view.getDictionary("all.routes.short"),
      clearSelectedItemsText: this.view.getDictionary("all.routes.short")
    });

    var routes = [];
    for (var i = 0; i < therapyTimelines.length; i++)
    {
      var routeName = therapyTimelines[i].therapy.route.name;
      if (!routes.contains(routeName))
      {
        routes.push(routeName);
        this.routesFilterButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
          text: routeName,
          checked: this.selectedRoutes.contains(routeName),
          data: routeName
        }));
      }
    }

    this.routesFilterButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._timelineFilterChanged(true);
    });

    this.routesFilterContainer.add(this.routesFilterButton);
  },

  _setupCustomGroupFilter: function(therapyTimelines)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    this.customGroupFilterContainer.removeAll();

    if (this.customGroups && this.customGroups.length > 0)
    {
      this.customGroupFilterButton = appFactory.createCheckboxSplitButton({
        margin: 5,
        showSelectedItemsIcon: false,
        showSelectedItemsText: true,
        popupMenuHorizontalAlignment: "right",
        clearSelectedCheckBoxItemText: this.ALL_CUSTOM_GROUPS,
        clearSelectedItemsText: this.ALL_CUSTOM_GROUPS
      });

      var therapiesCustomGroups = [];
      for (var i = 0; i < therapyTimelines.length; i++)
      {
        var therapyCustomGroup = therapyTimelines[i].customGroup;
        if (therapyCustomGroup && !therapiesCustomGroups.contains(therapyCustomGroup))
        {
          therapiesCustomGroups.push(therapyCustomGroup);
        }
      }

      for (var j = 0; j < this.customGroups.length; j++)
      {
        var customGroup = this.customGroups[j];
        this.customGroupFilterButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
          text: customGroup,
          checked: this.selectedCustomGroups.contains(customGroup),
          enabled: therapiesCustomGroups.contains(customGroup),
          data: customGroup
        }));
      }

      this.customGroupFilterButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
        text: this.OTHER_CUSTOM_GROUP,
        checked: this.selectedCustomGroups.contains(this.OTHER_CUSTOM_GROUP),
        data: null
      }));

      this.customGroupFilterButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        self._timelineFilterChanged(true);
      });

      this.customGroupFilterContainer.add(this.customGroupFilterButton);
    }
  },

  /** public methods */
  setupTimelineFilter: function(therapyTimelines)
  {
    if (this.mode == "TIMELINE")
    {
      this._setupRoutesFilter(therapyTimelines);
      this._setupCustomGroupFilter(therapyTimelines);
      this._timelineFilterChanged(false);
      this.repaint();
    }
  },

  setCustomGroups: function(customGroups)
  {
    this.customGroups = customGroups;
  },

  addPreviousButtonAction: function(action)
  {
    this.previousButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      action();
    });
  },
  addNextButtonAction: function(action)
  {
    this.nextButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      action();
    });
  },
  addDayCountButtonGroupAction: function(action)
  {
    this.dayCountButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      action(componentEvent.getEventData().newSelectedButton.dayCount);
    });
  },
  addGroupingButtonGroupAction: function(action)
  {
    this.groupingButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      action(componentEvent.getEventData().newSelectedButton.groupField);
    });
  },
  addSortButtonGroupAction: function(action)
  {
    var self = this;
    this.sortButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      var selections = componentEvent.getEventData().selections;
      self.therapySortTypeEnum = selections[0].getData();
      action(self.therapySortTypeEnum);
    });
  },
  getFilterContext: function()
  {
    return {
      groupByMode: this.groupingButtonGroup.getPressedButtons()[0].mode,
      numberOfDaysMode: this.dayCountButtonGroup.getPressedButtons()[0].mode
    }
  },
  setTherapySortType: function(therapySortTypeEnum)
  {
    for (var i = 0; i < this.sortButton.getCheckBoxMenuItems().length; i++)
    {
      var menuItem = this.sortButton.getCheckBoxMenuItems()[i];
      if (menuItem.data == therapySortTypeEnum)
      {
        menuItem.setChecked(true, true);
        return menuItem.data;
      }
    }
    return null;
  },
  setNumberOfDaysMode: function(mode)
  {
    for (var i = 0; i < this.dayCountButtonGroup.getButtons().length; i++)
    {
      var button = this.dayCountButtonGroup.getButtons()[i];
      if (button.mode == mode)
      {
        button.setPressed(true, true);
        return button.dayCount;
      }
    }
    return null;
  },
  setGroupMode: function(mode)
  {
    for (var i = 0; i < this.groupingButtonGroup.getButtons().length; i++)
    {
      var button = this.groupingButtonGroup.getButtons()[i];
      if (button.mode == mode)
      {
        button.setPressed(true, true);
        return button.groupField;
      }
    }
    return null;
  },
  showRecentHospitalizationButton: function(isRecentHospitalization)
  {
    if (isRecentHospitalization)
    {
      this.previousHospitalizationTherapiesButton.show()
    }
    else
    {
      this.previousHospitalizationTherapiesButton.hide()
    }
  },
  disableOrdering: function()
  {
    this.orderButton.hide();
    this.previousHospitalizationTherapiesButton.hide();
  },
  setMode: function(mode)
  {
    this.mode = mode;
    this.removeAll();
    this._buildGui();
  },
  refreshReferenceWeightDisplay: function()
  {
    if (this.mode == 'TIMELINE')
    {
      this._showReferenceWeight();
    }
  }
});