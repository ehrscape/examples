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

Class.define('tm.views.medications.TherapyView', 'app.views.common.AppView', {
  cls: "v-therapy-view",
  scrollable: 'visible',

  mode: null, //FLOW, ORDERING, EDIT, ORDERING_PAST, EDIT_PAST (TherapyPaneModeEnum.java)

  /** update data */
  dayCount: null,
  groupField: null,
  patientData: null,  //PatientDataForMedicationsDto.java
  patientId: null,
  centralCaseData: null,
  roundsInterval: null,
  knownOrganizationalEntityName: null,
  administrationTiming: null,
  referenceWeight: null,
  organizationalEntityId: null,
  organizationalEntityName: null,
  userId: null,
  currentUserAsCareProfessional: null,
  context: null,
  editAllowed: null,
  orgTemplatesEditAllowed: null,
  customGroups: null,
  therapySortTypeEnum: null,

  presetMedicationId: null,
  presetDate: null,
  therapyToEdit: null,

  /** privates */
  therapyGrid: null,
  header: null,
  actionsHeader: null,
  timelineContainer: null,
  therapyEnums: null,
  medications: null,
  careProfessionals: null,
  careProfessionalsLoaded: false,
  doseForms: null,
  routes: null,
  units: null,
  displayTypeButtonGroup: null,
  flowGridTypeButton: null,
  timelineTypeButton: null,
  orderingDialog: null,

  /** statics */
  statics: {
    SERVLET_PATH_LOAD_THERAPY_VIEW_DATA: '/getTherapyViewData',
    SERVLET_PATH_LOAD_THERAPY_FLOW_DATA: '/therapyflowdata',
    SERVLET_PATH_RELOAD_SINGLE_THERAPY_AFTER_ACTION: '/reloadSingleTherapyAfterAction',
    SERVLET_PATH_FIND_MEDICATIONS: '/findmedications',
    SERVLET_PATH_PATIENT_DATA: '/patientdata',
    SERVLET_PATH_MODIFY_THERAPY: '/modifyTherapy',
    SERVLET_PATH_MEDICATION_ORDER_CARD_INFO_DATA: '/patientmedicationordercardinfodata',
    SERVLET_PATH_GET_BASELINE_THERAPIES: '/getPatientBaselineInfusionIntervals',
    SERVLET_PATH_MEDICATION_DATA: '/medicationdata',
    SERVLET_PATH_FILL_DISPLAY_VALUES: '/fillTherapyDisplayValues',
    SERVLET_PATH_GET_THERAPY_FORMATTED_DISPLAY: '/getTherapyFormattedDisplay',
    SERVLET_PATH_SAVE_MEDICATIONS_ORDER: '/saveMedicationsOrder',
    SERVLET_PATH_SAVE_REFERENCE_WEIGHT: '/saveReferenceWeight',
    SERVLET_PATH_FIND_SIMILAR_MEDICATIONS: '/findSimilarMedications',
    SERVLET_PATH_FIND_MEDICATION_PRODUCTS: '/getMedicationProducts',
    SERVLET_PATH_REVIEW_THERAPY: "/reviewTherapy",
    SERVLET_PATH_SUSPEND_THERAPY: "/suspendTherapy",
    SERVLET_PATH_REISSUE_THERAPY: "/reissueTherapy",
    SERVLET_PATH_SUSPEND_ALL_THERAPIES: "/suspendAllTherapies",
    SERVLET_PATH_ABORT_THERAPY: "/abortTherapy",
    SERVLET_PATH_GET_ROUTES: "/getRoutes",
    SERVLET_PATH_GET_DOSE_FORMS: "/getDoseForms",
    SERVLET_PATH_GET_UNITS: "/getMedicationBasicUnits",
    SERVLET_PATH_FIND_WARNINGS: "/findMedicationWarnings",
    SERVLET_PATH_GET_THERAPIES_FOR_WARNINGS: "/getTherapiesForWarnings",
    SERVLET_PATH_GET_TEMPLATES: "/getTherapyTemplates",
    SERVLET_PATH_SAVE_TEMPLATE: "/saveTherapyTemplate",
    SERVLET_PATH_DELETE_TEMPLATE: "/deleteTherapyTemplate",
    SERVLET_PATH_GET_MEDICATION_DOCUMENT: "/getMedicationDocument",
    SERVLET_PATH_OPEN_MEDICATION_DOCUMENT: "/openMedicationDocument",
    SERVLET_PATH_SAVE_CONSECUTIVE_DAYS: "/saveConsecutiveDays",
    SERVLET_PATH_GET_THERAPIES_FOR_PREVIOUS_HOSPITALIZATION: "/getLastTherapiesForPreviousHospitalization",
    SERVLET_PATH_GET_THERAPY_TIMELINE_DATA: '/getTherapyTimelineData',
    SERVLET_PATH_CONFIRM_THERAPY_ADMINISTRATION: '/confirmTherapyAdministration',
    SERVLET_PATH_CREATE_ADMINISTRATION_TASK: '/createAdministrationTask',
    SERVLET_PATH_RESCHEDULE_TASKS: '/rescheduleTasks',
    SERVLET_PATH_DELETE_TASK: '/deleteTask',
    SERVLET_PATH_DELETE_ADMINISTRATION: '/deleteAdministration',
    SERVLET_PATH_GET_CARE_PROFESSIONALS: '/getCareProfessionals',
    SERVLET_PATH_TAG_THERAPY_FOR_PRESCRIPTION: '/tagTherapyForPrescription',
    SERVLET_PATH_UNTAG_THERAPY_FOR_PRESCRIPTION: '/untagTherapyForPrescription'
  },

  /** constructor */
  Constructor: function(config)
  {
    config = tm.jquery.Utils.applyIf({

      layout: new tm.jquery.BorderLayout()
    }, config);
    this.callSuper(config);

    tm.views.medications.TherapyView.THERAPY_FLOW_LOAD_HUB = this.createUserAction("therapyFlowLoadHub");
    tm.views.medications.TherapyView.THERAPY_FLOW_NAVIGATE_HUB = this.createUserAction("therapyFlowNavigateHub");

    var self = this;

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_WINDOW_RESIZE, function()
    {
      if (self.orderingDialog)
      {
        self.orderingDialog.setWidth(this._getOrderingDialogWidth());
        self.orderingDialog.setHeight(this._getOrderingDialogHeight());
      }
    });

    this.medications = [];
    this.careProfessionals = [];
    this.doseForms = [];
    this.routes = [];
    this.units = [];
    var viewInitData = this.getViewInitData();
    this.mode = viewInitData.paneMode;
    this.userId = viewInitData.userId;
    this.currentUserAsCareProfessional = viewInitData.currentUserAsCareProfessional;
    this.editAllowed = viewInitData.editAllowed;
    this.optimizeForPerformance = viewInitData.optimizeForPerformance;
    this.orgTemplatesEditAllowed = viewInitData.orgTemplatesEditAllowed;
    this.context = viewInitData.contextData ? JSON.parse(viewInitData.contextData) : null;

    this.careProfessionalsLoaded = false;
    this._loadCareProfessionals();
    if (this.mode == 'FLOW')
    {
      this._loadRoutes();
      this._buildFlowGui();
    }
    else if (['ORDERING', 'EDIT', 'ORDERING_PAST', 'EDIT_PAST'].indexOf(this.mode) > -1)
    {
      this._loadMedications();
    }

    if (viewInitData.patientId)
    {
      var updateDataCommand = {
        "update":{"data":{"patientId":viewInitData.patientId,"departmentName":"PEK"}}};
      this.onViewCommand(updateDataCommand);
    }
  },

  onViewCommand: function(command)
  {
    if (command.hasOwnProperty('update'))
    {
      this.updateDataCommand = command;
      this.updateData(command);
    }
    if (command.hasOwnProperty('clear'))
    {
      if (this.therapyGrid)
      {
        this.therapyGrid.clear();
      }
      else if (this.timelineContainer)
      {
        this.timelineContainer.clear();
      }
    }
    if (command.hasOwnProperty('refresh'))
    {
      this.updateData(this.updateDataCommand);
    }
  },

  updateData: function(config)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    tm.jquery.ComponentUtils.hideAllDialogs();
    tm.jquery.ComponentUtils.hideAllDropDownMenus(this);
    tm.jquery.ComponentUtils.hideAllTooltips(this);

    this.patientId = config.update.data.patientId;
    this.knownOrganizationalEntityName = config.update.data.departmentName;

    //ORDERING PAST, EDIT PAST (DRP)
    this.centralCaseData = config.update.data.centralCaseData;
    //centralCaseData:
    //    Long centralCaseId
    //    Long episodeId
    //    Long sessionId
    //    Long careProviderId
    //    boolean recentHospitalization
    this.presetMedicationId = config.update.data.presetMedicationId || null;
    this.therapyToEdit = config.update.data.therapyToEdit;
    this.presetDate = config.update.data.presetDate ? new Date(config.update.data.presetDate) : null;

    if (this.mode == 'FLOW')
    {
      this._loadTherapyViewData(function()
      {
        self._loadPatientData();
        self._presentData();
      });
    }
    else if (['ORDERING', 'EDIT', 'ORDERING_PAST', 'EDIT_PAST'].indexOf(this.mode) > -1)
    {
      appFactory.createConditionTask(  //wait for the medications to load
          function()
          {
            if (self.mode == 'ORDERING' || self.mode == 'ORDERING_PAST')
            {
              self._buildOrderingGui(self.mode == 'ORDERING_PAST', self.mode != 'ORDERING_PAST');
            }
            else
            {
              self._buildEditGui(self.mode == 'EDIT_PAST');
            }
          },
          function()
          {
            return self.medications.length > 0 && self.careProfessionalsLoaded;
          },
          50, 1000
      );
    }
  },

  _presentData: function()
  {
    this.actionsHeader.showRecentHospitalizationButton(this.centralCaseData && this.centralCaseData.recentHospitalization && this.editAllowed);
    this.actionsHeader.setCustomGroups(this.customGroups);
    this.actionsHeader.refreshReferenceWeightDisplay();
    if (this.therapyGrid)
    {
      var searchDate = this._getDefaultSearchDate(this.dayCount);
      this.therapyGrid.paintGrid(this.dayCount, searchDate, this.groupField, this.therapySortTypeEnum);
      this._addGridEvents();
    }
    else
    {
      this.timelineContainer.setPatientId(this.patientId, this.therapySortTypeEnum);
    }
  },

  /** private methods */

  _loadTherapyViewData: function(callback)
  {
    var self = this;
    var params = {
      patientId: self.patientId,
      knownOrganizationalEntityName: self.knownOrganizationalEntityName
    };
    var url = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_LOAD_THERAPY_VIEW_DATA;
    self.loadViewData(url, params, null, function(data)
    {
      self.roundsInterval = data.roundsInterval;
      self.administrationTiming = data.administrationTiming;
      self.referenceWeight = data.referenceWeight;
      self.customGroups = data.customGroups;
      self.centralCaseData = data.medicationsCentralCase;
      if (data.medicationsCentralCase)
      {
        self.knownOrganizationalEntityName = data.medicationsCentralCase.departmentName;
        self.organizationalEntityId = data.medicationsCentralCase.organizationalEntityId;
        self.organizationalEntityName = data.medicationsCentralCase.organizationalEntityName;
      }
      callback();
    });
  },

  _loadPatientData: function()
  {
    var self = this;
    var params = {
      patientId: self.patientId,
      episodeId: self.centralCaseData && self.centralCaseData.episodeId ? self.centralCaseData.episodeId : null
    };
    var patientDataUrl = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_PATIENT_DATA;
    self.loadViewData(patientDataUrl, params, null, function(data)
    {
      self.patientData = data;   //PatientDataForMedicationsDto.java
    });
  },

  _loadMedications: function()
  {
    var self = this;
    var medicationsUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_MEDICATIONS;
    this.loadViewData(medicationsUrl, null, null, function(data)
    {
      self.medications.length = 0;
      $.merge(self.medications, data);
    });
  },

  _loadCareProfessionals: function()
  {
    var self = this;
    var careProfessionalsUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_CARE_PROFESSIONALS;
    this.loadViewData(careProfessionalsUrl, null, null, function(data)
    {
      self.careProfessionals.length = 0;
      self.careProfessionalsLoaded = true;
      $.merge(self.careProfessionals, data);
    });
  },

  _loadDoseForms: function()
  {
    var self = this;
    var getDoseFormsUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_DOSE_FORMS;
    this.loadViewData(getDoseFormsUrl, null, null, function(data)
    {
      self.doseForms.length = 0;
      $.merge(self.doseForms, data);
    });
  },

  _loadRoutes: function()
  {
    var self = this;
    var getRoutesUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_ROUTES;
    this.loadViewData(getRoutesUrl, null, null, function(data)
    {
      self.routes.length = 0;
      $.merge(self.routes, data);
    });
  },

  _loadUnits: function(callback)
  {
    var self = this;
    var getUnitsUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_UNITS;
    this.loadViewData(getUnitsUrl, null, null, function(data)
    {
      self.units.length = 0;
      $.merge(self.units, data);
      callback();
    });
  },

  _buildFlowGui: function()
  {
    var self = this;
    this.content = new tm.jquery.Container({layout: this.getAppFactory().createDefaultVFlexboxLayout("start", "stretch")});
    this.header = new tm.jquery.Container({cls: 'header', flex: 1, layout: this.getAppFactory().createDefaultHFlexboxLayout("start", "stretch"), scrollable: 'visible'});
    var headerContainer = new app.views.common.Toolbar({
      cls: 'app-views-toolbar portlet-header',
      layout: this.getAppFactory().createDefaultHFlexboxLayout("start", "center")
    });
    this.flowGridTypeButton = new tm.jquery.Button({
      cls: 'flow-grid-icon',
      type: 'GRID',
      pressed: true,
      tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("therapies"), "bottom")
    });
    this.timelineTypeButton = new tm.jquery.Button({
      cls: 'timeline-icon',
      type: 'TIMELINE',
      tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("administrations"), "bottom")
    });

    this.displayTypeButtonGroup = new tm.jquery.ButtonGroup({
      width: 200,
      margin: '0 0 0 5',
      orientation: "horizontal",
      type: "radio",
      buttons: [this.flowGridTypeButton, this.timelineTypeButton]
    });
    this.displayTypeButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      var eventData = componentEvent.getEventData();
      var newSelectedButton = eventData.newSelectedButton;
      if (newSelectedButton.type == 'GRID')
      {
        self.actionsHeader.setMode("GRID");
        self._buildFlowGridGui();
        var searchDate = self._getDefaultSearchDate(self.dayCount);
        self.therapyGrid.paintGrid(self.dayCount, searchDate, self.groupField, self.therapySortTypeEnum);
        self._addGridEvents();
        self.content.repaint();
        self.header.repaint();
      }
      else if (newSelectedButton.type == 'TIMELINE')
      {
        self.actionsHeader.setMode("TIMELINE");
        self._buildTimelineGui();
        self.timelineContainer.reloadTimelines(true, self.therapySortTypeEnum);
        self.content.repaint();
        self.header.repaint();
      }
      self._saveContext();
    });

    headerContainer.add(this.displayTypeButtonGroup);
    headerContainer.add(this.header);
    this.add(headerContainer, {region: 'north', height: 40});
    this.add(this.content, {region: 'center'});
    this.actionsHeader = new tm.views.medications.TherapyOverviewHeader({
      view: this,
      mode: "GRID",
      flex: 1,
      timelineFilterChangedFunction: function(routes, customGroups, refreshTimelines)
      {
        self.timelineContainer.setTimelineFilter(routes, customGroups, refreshTimelines);
      }
    });
    this.header.add(this.actionsHeader);
    if (this.editAllowed == false)
    {
      this.actionsHeader.disableOrdering();
    }
    this.actionsHeader.showRecentHospitalizationButton(this.centralCaseData && this.centralCaseData.recentHospitalization && this.editAllowed);

    if (this.context && this.context.viewType == "TIMELINE")
    {
      this.displayTypeButtonGroup.setSelection([this.timelineTypeButton]);
      this.actionsHeader.setMode("TIMELINE");
      this._buildTimelineGui();
    }
    else
    {
      this.displayTypeButtonGroup.setSelection([this.flowGridTypeButton]);
      this.actionsHeader.setMode("GRID");
      this._buildFlowGridGui();
    }
  },

  _buildFlowGridGui: function()
  {
    this.timelineContainer = null;
    this.content.removeAll(true);
    this.therapyGrid = new tm.views.medications.TherapyFlowGrid({view: this, flex: 1});
    this.content.add(this.therapyGrid);
    this._addGridHeaderEvents();

    if (this.context)
    {
      this.dayCount = this.actionsHeader.setNumberOfDaysMode(this.context.numberOfDaysMode);
      if (!this.dayCount)
      {
        this.dayCount = 3;
      }
      this.groupField = this.actionsHeader.setGroupMode(this.context.groupByMode);
    }
    else
    {
      this.dayCount = 3;
    }
    this._setTherapySortTypeEnum();
  },

  _buildTimelineGui: function()
  {
    var self = this;

    this.therapyGrid = null;
    this.content.removeAll(true);
    this.timelineContainer = new tm.views.medications.timeline.TherapyTimelineContainer({
      view: this,
      flex: 1,
      patientId: this.patientId,
      groupField: this.groupField,
      therapyTimelineRowsLoadedFunction: function(therapyTimelineRows)
      {
        self.actionsHeader.setupTimelineFilter(therapyTimelineRows);
      }
    });
    this.content.add(this.timelineContainer);

    this.actionsHeader.addGroupingButtonGroupAction(function(groupField)
    {
      self.groupField = groupField;
      self.timelineContainer.setGrouping(groupField);
      self._saveContext();
    });

    this.actionsHeader.addSortButtonGroupAction(function(therapySortTypeEnum)
    {
      self.therapySortTypeEnum = therapySortTypeEnum;
      self.timelineContainer.reloadTimelines(false, therapySortTypeEnum);
      self._saveContext();
    });

    this._setTherapySortTypeEnum();
  },

  _setTherapySortTypeEnum: function()
  {
    if (this.context)
    {
      this.therapySortTypeEnum = this.actionsHeader.setTherapySortType(this.context.therapySortTypeEnum);
      if (tm.jquery.Utils.isEmpty(this.therapySortTypeEnum))
      {
        this.therapySortTypeEnum = this.actionsHeader.setTherapySortType(app.views.medications.TherapyEnums.therapySortTypeEnum.DESCRIPTION_ASC);
      }
    }
    else
    {
      this.therapySortTypeEnum = this.actionsHeader.setTherapySortType(app.views.medications.TherapyEnums.therapySortTypeEnum.DESCRIPTION_ASC);
    }
  },

  _buildOrderingGui: function(allowPastOrdering, assertBaselineInfusion)
  {
    var orderingContainer = new app.views.medications.ordering.MedicationsOrderingContainer({
      view: this,
      patientId: this.patientId,
      presetMedicationId: this.presetMedicationId ? this.presetMedicationId : null,
      presetDate: this.presetDate ? this.presetDate : null,
      isPastMode: allowPastOrdering,
      assertBaselineInfusion: assertBaselineInfusion,
      flex: 1
    });
    this.add(orderingContainer, {region: 'center'});
    var footer = this._buildDialogFooter(orderingContainer);
    this.add(footer, {region: 'south', height: 40});

    this.repaint();
  },

  _buildEditGui: function(isPastEditMode)
  {
    //this.therapyToEdit must be set
    var self = this;
    var editContainer;
    if (this.therapyToEdit.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX)
    {
      editContainer = new app.views.medications.ordering.ComplexTherapyEditContainer({
        view: self,
        startProcessOnEnter: true,
        therapy: this.therapyToEdit,
        isPastMode: isPastEditMode
      });
    }
    else
    {
      editContainer = new app.views.medications.ordering.SimpleTherapyEditContainer({
        view: self,
        startProcessOnEnter: true,
        therapy: this.therapyToEdit,
        isPastMode: isPastEditMode
      });
    }
    this.add(editContainer, {region: 'center'});
    var footer = this._buildDialogFooter(editContainer);
    this.add(footer, {region: 'south', height: 40});

    this.repaint();
  },

  _buildDialogFooter: function(dialogContainer)
  {
    var self = this;
    var footer = new tm.jquery.Container({
      height: 50,
      style: "background-color: #f7f7f7; border-top: 1px solid #ebebeb; padding-right: 20px;",
      layout: this.getAppFactory().createDefaultHFlexboxLayout("end", "center", 10)
    });

    footer.saveButton = new tm.jquery.Button({text: this.getDictionary("confirm"),
      tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("save.therapy"), "left"),
      handler: function()
      {
        footer.saveButton.setEnabled(false);
        dialogContainer.processResultData(function(data)
        {
          if (data.success)
          {
            ViewManager.getInstance().sendAction(
                self,
                ViewAction.create("closeTherapyDialog").addParam("reason", "save"));
          }
          else
          {
            footer.saveButton.setEnabled(true);
          }
        });
      }});
    footer.add(footer.saveButton);

    footer.cancelLink = new tm.jquery.Button({text: this.getDictionary("cancel"), type: "link",
      handler: function()
      {
        ViewManager.getInstance().sendAction(self, ViewAction.create("closeTherapyDialog").addParam("reason", "cancel"));
      }
    });
    footer.add(footer.cancelLink);
    return footer;
  },

  _addGridHeaderEvents: function()
  {
    var self = this;
    this.actionsHeader.addPreviousButtonAction(function()
    {
      tm.jquery.ComponentUtils.hideAllDialogs();
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self);
      tm.jquery.ComponentUtils.hideAllTooltips(self);
      self.therapyGrid.changeSearchDate(false);
    });
    this.actionsHeader.addNextButtonAction(function()
    {
      tm.jquery.ComponentUtils.hideAllDialogs();
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self);
      tm.jquery.ComponentUtils.hideAllTooltips(self);
      self.therapyGrid.changeSearchDate(true);
    });
    this.actionsHeader.addDayCountButtonGroupAction(function(dayCount)
    {
      self.dayCount = dayCount;
      var searchDate = self._getDefaultSearchDate(dayCount);
      self.therapyGrid.repaintGrid(dayCount, searchDate, self.therapySortTypeEnum);
      self._addGridEvents();
      self._saveContext();
    });
    this.actionsHeader.addGroupingButtonGroupAction(function(groupField)
    {
      self.groupField = groupField;
      self.therapyGrid.setGrouping(groupField);
      self._saveContext();
    });

    this.actionsHeader.addSortButtonGroupAction(function(therapySortTypeEnum)
    {
      self.therapySortTypeEnum = therapySortTypeEnum;
      var searchDate = self._getDefaultSearchDate(self.dayCount);
      self.therapyGrid.repaintGrid(self.dayCount, searchDate, therapySortTypeEnum);
      self._addGridEvents();
      self._saveContext();
    });
  },

  _addGridEvents: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    this.therapyGrid.grid.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      appFactory.createConditionTask(
          function()
          {
            self.therapyGrid.reloadGridData();
          },
          function()
          {
            return self.therapyGrid.grid.getPlugin() != null && self.getPatientData() != null;
          },
          50, 500
      );
    });
  },

  _saveContext: function()
  {
    if (!this.context)
    {
      this.context = {};
    }
    this.context.viewType = this.displayTypeButtonGroup.getSelection()[0].type;
    var headerContext = this.actionsHeader.getFilterContext();
    this.context.groupByMode = headerContext.groupByMode;
    this.context.therapySortTypeEnum = this.therapySortTypeEnum;
    if (this.context.viewType == "GRID")
    {
      this.context.numberOfDaysMode = headerContext.numberOfDaysMode;
    }

    this.sendAction("SAVE_CONTEXT", {contextData: JSON.stringify(this.context)});
  },

  _getDefaultSearchDate: function(dayCount)
  {
    var searchDate = new Date();
    searchDate.setDate(searchDate.getDate() - dayCount + 1);
    return searchDate;
  },

  refreshTherapies: function(repaintTimeline)
  {
    if (this.therapyGrid)
    {
      this.therapyGrid.reloadGridData();
    }
    else if (this.timelineContainer)
    {
      this.timelineContainer.reloadTimelines(repaintTimeline, this.therapySortTypeEnum);
    }
    this._saveContext();
  },

  /** public methods */
  showEditSimpleTherapyDialog: function(therapy, copyTherapy)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        copyTherapy ? self.getDictionary("copy.therapy") : self.getDictionary("edit.therapy"),
        null,
        new app.views.medications.ordering.SimpleTherapyEditContainer({view: self, startProcessOnEnter: true, therapy: therapy, copyTherapy: copyTherapy}),
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(false);
          }
        },
        725,
            $(window).height() - 10 < 550 ? $(window).height() - 10 : 550
    );
    dialog.setHideOnEscape(false);
    dialog.show();
  },

  tagTherapyForPrescription: function(patientId, centralCaseId, compositionId, ehrOrderName)
  {
    var self = this;
    this.showLoaderMask(self.view, null, 5000);
    var tagTherapiesUrl = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_TAG_THERAPY_FOR_PRESCRIPTION;

    var params = {patientId: patientId, centralCaseId: centralCaseId, compositionId: compositionId, ehrOrderName: ehrOrderName};
    this.loadPostViewData(tagTherapiesUrl, params, null, function()
    {
      self.refreshTherapies(false);
    });
  },

  untagTherapyForPrescription: function(patientId, centralCaseId, compositionId, ehrOrderName)
  {
    var self = this;
    this.showLoaderMask(self.view, null, 5000);
    var untagTherapiesUrl = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_UNTAG_THERAPY_FOR_PRESCRIPTION;

    var params = {patientId: patientId, centralCaseId: centralCaseId, compositionId: compositionId, ehrOrderName: ehrOrderName};
    this.loadPostViewData(untagTherapiesUrl, params, null, function()
    {
      self.refreshTherapies(false);
    });
  },

  showEditConsecutiveDaysDialog: function(pastDaysOfTherapy, ehrCompositionId, ehrOrderName)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        self.getDictionary("consecutive.days.edit"),
        null,
        new app.views.medications.ordering.EditPastDaysOfTherapyContainer({
          view: self,
          startProcessOnEnter: true,
          pastDaysOfTherapy: pastDaysOfTherapy,
          ehrCompositionId: ehrCompositionId,
          ehrOrderName: ehrOrderName
        }),
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(false);
          }
        },
        210,
        130
    );
    dialog.setHideOnEscape(false);
    dialog.show();
  },

  showEditComplexTherapyDialog: function(therapy, copyTherapy)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        copyTherapy ? self.getDictionary("copy.therapy") : self.getDictionary("edit.therapy"),
        null,
        new app.views.medications.ordering.ComplexTherapyEditContainer({
          view: self,
          startProcessOnEnter: true,
          therapy: therapy,
          copyTherapy: copyTherapy,
          hideDialogFunction: function()
          {
            dialog.hide();
          }
        }),
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(false);
          }
        },
        715,
            $(window).height() - 10 < 650 ? $(window).height() - 10 : 650
    );
    if (self.medications.length == 0)
    {
      self._loadMedications();
    }
    dialog.setHideOnEscape(false);
    dialog.show();
  },

  openReferenceWeightDialog: function(weight, callback)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        this.getDictionary("reference.weight"),
        null,
        new app.views.medications.therapy.ReferenceWeightPane({view: self, startProcessOnEnter: true, weight: weight, padding: 8}),
        function(resultData)
        {
          if (resultData)
          {
            self.referenceWeight = resultData.value;
            self.refreshTherapies(false);
            self.actionsHeader.refreshReferenceWeightDisplay();
            if (callback)
            {
              callback();
            }
          }
        },
        180,
        130
    );

    dialog.setHideOnEscape(false);
    dialog.show();
  },

  openMedicationOrderingDialog: function(presetTherapies)
  {
    var self = this;
    if (this.referenceWeight)
    {
      this._openMedicationOrderingDialog(presetTherapies);
    }
    else
    {
      var patientWeight = this.patientData ? this.patientData.weightInKg : null;
      this.openReferenceWeightDialog(patientWeight, function()
      {
        self._openMedicationOrderingDialog(presetTherapies);
      });
    }
  },

  _openMedicationOrderingDialog: function(presetTherapies)
  {
    var self = this;
    var appFactory = this.getAppFactory();
    this.orderingDialog = appFactory.createDataEntryDialog(
        self.getDictionary("medications.ordering"),
        null,
        new app.views.medications.ordering.MedicationsOrderingContainer({
          view: self,
          patientId: self.patientId,
          presetTherapies: presetTherapies
        }),
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(true);
          }
        },
        this._getOrderingDialogWidth(),
        this._getOrderingDialogHeight()
    );

    if (self.medications.length == 0)
    {
      self._loadMedications();
    }
    this.orderingDialog.setHideOnEscape(false);
    var confirmButton = this.orderingDialog.getBody().footer.confirmButton;
    confirmButton.setText(this.getDictionary("do.order"));
    this.orderingDialog.show();
  },

  _getOrderingDialogWidth: function()
  {
    return $(window).width() - 50;
  },

  _getOrderingDialogHeight: function()
  {
    return $(window).height() - 10;
  },

  openUniversalSimpleTherapyDialog: function(therapyToEdit)
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var orderingDialog = appFactory.createDataEntryDialog(
            this.getDictionary('universal.form') + ' - ' + this.getDictionary('simple1'),
        null,
        new app.views.medications.ordering.UniversalSimpleTherapyContainer({
          view: self,
          therapyToEdit: therapyToEdit,
          patientId: self.patientId
        }),
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(true);
          }
        },
        725,
            $(window).height() - 10 < 550 ? $(window).height() - 10 : 550
    );

    if (self.doseForms.length == 0)
    {
      self._loadDoseForms();
    }

    orderingDialog.setHideOnEscape(false);
    var confirmButton = orderingDialog.getBody().footer.confirmButton;
    confirmButton.setText(this.getDictionary("do.order"));
    orderingDialog.show();
  },

  openUniversalComplexTherapyDialog: function(therapyToEdit)
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var orderingDialog = appFactory.createDataEntryDialog(
            this.getDictionary('universal.form') + ' - ' + this.getDictionary('expanded1'),
        null,
        new app.views.medications.ordering.UniversalComplexTherapyContainer({
          view: self,
          therapyToEdit: therapyToEdit,
          patientId: self.patientId
        }),
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(true);
          }
        },
        725,
            $(window).height() - 10 < 620 ? $(window).height() - 10 : 620
    );

    if (self.doseForms.length == 0)
    {
      self._loadDoseForms();
    }

    orderingDialog.setHideOnEscape(false);
    var confirmButton = orderingDialog.getBody().footer.confirmButton;
    confirmButton.setText(this.getDictionary("do.order"));
    orderingDialog.show();
  },

  orderPreviousHospitalizationTherapies: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var previousTherapiesUrl = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPIES_FOR_PREVIOUS_HOSPITALIZATION;
    var params = {
      patientId: self.patientId,
      patientHeight: self.getPatientData().heightInCm
    };
    this.loadViewData(previousTherapiesUrl, params, null, function(previousTherapies)
    {
      if (previousTherapies && previousTherapies.length != 0)
      {
        self.openMedicationOrderingDialog(previousTherapies);
      }
      else
      {
        appFactory.createWarningSystemDialog(self.getDictionary("no.previous.hospitalization.therapies"), 320, 122).show();
      }
    });
  },

  getPatientId: function()
  {
    return this.patientId;
  },
  getPatientData: function()
  {
    return this.patientData;   //PatientDataForMedicationsDto.java
  },
  getMedications: function()
  {
    return this.medications;
  },
  getCareProfessionals: function()
  {
    return this.careProfessionals;
  },
  getDoseForms: function()
  {
    return this.doseForms;
  },
  getRoutes: function()
  {
    return this.routes;
  },
  provideUnits: function(returnUnitsFn)
  {
    var self = this;
    if (this.units.length == 0)
    {
      this._loadUnits(function()
      {
        returnUnitsFn(self.units);
      });
    }
    else
    {
      returnUnitsFn(this.units);
    }
  },
  getAdministrationTiming: function()
  {
    return this.administrationTiming;
  },
  getRoundsInterval: function()
  {
    return this.roundsInterval;
  },
  getCentralCaseData: function()
  {
    return this.centralCaseData;
  },
  getKnownOrganizationalEntity: function()
  {
    return this.knownOrganizationalEntityName;
  },
  getUserId: function()
  {
    return this.userId;
  },
  getCurrentUserAsCareProfessional: function()
  {
    return this.currentUserAsCareProfessional;
  },
  getOrganizationalEntityId: function()
  {
    return this.organizationalEntityId;
  },
  getOrganizationalEntityName: function()
  {
    return this.organizationalEntityName;
  },
  getReferenceWeight: function()
  {
    return this.referenceWeight;
  },
  getContext: function()
  {
    return this.context;
  },
  setContext: function(context)
  {
    this.context = context;
  },
  isEditAllowed: function()
  {
    return this.editAllowed;
  },
  isOrgTemplatesEditAllowed: function()
  {
    return this.orgTemplatesEditAllowed;
  },
  getOptimizeForPerformance: function()
  {
    return this.optimizeForPerformance;
  },

  onViewInit: function()
  {
    this.callSuper();

    if (this.isDevelopmentMode() && !this.patientId)
    {
      // update data command //
      var updateDataCommand = {
        //"update":{"data":{"patientId":59860944,"departmentName":"PEK"}}};
        "update":{"data":{"patientId":120897727,"departmentName":"PEK"}}};
        //"update": {"data": {"patientId": 63937299, "departmentName": "KOOKIT",
          //"therapyToEdit": {"doseElement":{"quantity":85,"doseDescription":null,"quantityDenominator":null,"serialVersionUID":0},"administrationTimesOfDay":[{"hour":8,"minute":0},{"hour":20,"minute":0}],"medication":{"shortName":"Aspirin","genericName":"acetilsalicilna kislina","medicationType":"MEDICATION","displayName":"acetilsalicilna kislina (Aspirin direkt 500 mg žvečlj. tbl.)","serialVersionUID":0,"code":"48836","name":"Aspirin direkt 500 mg žvečlj. tbl.","id":1048836},"quantityUnit":"mg","doseForm":{"doseFormType":"TBL","medicationOrderFormType":"SIMPLE","serialVersionUID":0,"name":"Žvečljiva tableta","code":"337","description":null,"deleted":false,"auditInfo":{"creatorId":1,"createTimestamp":"2013-07-15T15:25:00.000+02:00","editorId":null,"editTimestamp":null},"version":0,"id":337},"quantityDenominatorUnit":null,"quantityDisplay":"85 mg","compositionUid":null,"ehrOrderName":null,"medicationOrderFormType":"SIMPLE","variable":false,"therapyDescription":"acetilsalicilna kislina (Aspirin direkt 500 mg žvečlj. tbl.) - 85 mg - 2X na dan - Po naročilu zdravnika - po","route":{"type":null,"serialVersionUID":0,"code":"34","name":"po","id":34},"dosingFrequency":{"type":"DAILY_COUNT","value":2},"dosingDaysFrequency":null,"daysOfWeek":[],"start":"2013-12-31T07:00:00.000Z","end":null,"whenNeeded":false,"startCriterion":"BY_DOCTOR_ORDERS","comment":"a","prescriberName":null,"frequencyDisplay":"2X na dan","daysFrequencyDisplay":null,"whenNeededDisplay":null,"startCriterionDisplay":"Po naročilu zdravnika","daysOfWeekDisplay":null,"pastDaysOfTherapy":null,"serialVersionUID":0}}
          //"therapyToEdit": {"doseElement": {"quantity": 50.0, "doseDescription": null, "quantityDenominator": 6.25, "serialVersionUID": 0}, "medication": {"id": 1006678, "name": "FORTECORTIN 8 mg tbl", "shortName": "FORTECORTIN", "genericName": "deksametazon", "medicationType": "MEDICATION", "displayName": "deksametazon (FORTECORTIN 8 mg tbl)", "serialVersionUID": 0}, "quantityUnit": "mg", "doseForm": {"doseFormType": "TBL", "medicationOrderFormType": "SIMPLE", "serialVersionUID": 0, "name": "Tableta", "code": "294", "description": null, "deleted": false, "auditInfo": {"creatorId": 1, "createTimestamp": "2013-07-15T15:25:00.000+02:00", "editorId": null, "editTimestamp": null}, "version": 0, "id": 294}, "quantityDenominatorUnit": "tableta", "quantityDisplay": "50 mg/6,25 tableta", "compositionUid": "8f9b3efe-f588-4339-ae7b-0a66fbbfa0e3::prod.pediatrics.marand.si::1", "ehrOrderName": "Medication instruction", "medicationOrderFormType": "SIMPLE", "variable": false, "therapyDescription": "deksametazon (FORTECORTIN 8 mg tbl) - 50 mg/6,25 tableta - 2X na dan - po", "route": {"type": null, "serialVersionUID": 0, "code": "34", "name": "po", "id": 0}, "dosingFrequency": {"type": "DAILY_COUNT", "value": 2}, "dosingDaysFrequency": null, "daysOfWeek": null, "start": {"data": "2014-04-17T20:00:00.000+02:00", "default": "17.4.2014", "short.date": "17.4.2014", "short.time": "20:00", "short.date.time": "17.4.2014 20:00"}, "end": null, "whenNeeded": false, "startCriterion": null, "comment": null, "clinicalIndication": null, "prescriberName": "David Neubauer", "composerName": "Tadej Avčin", "frequencyDisplay": "2X na dan", "daysFrequencyDisplay": null, "whenNeededDisplay": null, "startCriterionDisplay": null, "daysOfWeekDisplay": null, "pastDaysOfTherapy": null, "serialVersionUID": 0}}
          //"presetDate": "2014-02-03T15:45:00.000Z",
          //"centralCaseData": {
          //  "centralCaseId": 100926392,
          //  "episodeId": 100928955,
          //  "careProviderId": 244135,
          //  "recentHospitalization": true}
        //}
      //};
      this.onViewCommand(updateDataCommand);
    }
  },

  getUidWithoutVersion: function(uid)
  {
    return uid.substring(0, uid.indexOf("::"));
  }
});