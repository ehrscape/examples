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
  cls: 'therapy-overview-header',
  scrollable: 'visible',
  OTHER_CUSTOM_GROUP: null,
  ALL_CUSTOM_GROUPS: null,

  /** configs */
  state: null,
  view: null,
  timelineFilterChangedFunction: null, //optional
  timelineDateSelectorChangedFunction: null,
  medicationIdentifierScannedFunction: null,
  isDischarged: null,
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
  timelineDateField: null,
  _timelineDateFieldTimer: null,
  _medicationBarcodeField: null,

  //sort
  therapySortTypeEnum: null,

  //temp
  previousSelectedSort: null,
  previousSelectedShownTherapies: null,

  _orderButtonTimer: null,
  _outpatientButtonTimer: null,

  /**
   * Private
   */
  _viewType: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.OTHER_CUSTOM_GROUP = this.view.getDictionary("other.undef");
    this.ALL_CUSTOM_GROUPS = this.view.getDictionary("all.groups.short");
    this.selectedRoutes = [];
    this.selectedCustomGroups = [];
  },

  /** private methods */
  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("space-between", "center"));
    var viewType = this.getViewType();
    if (viewType === "GRID")
    {
      this._addWestContainer();
      this._addCenterGridContainer();
      this._addEastGridContainer();
    }
    else if (viewType === "TIMELINE")
    {
      this._addWestContainer(true);
      this._addTimelineDatePicker();
      this._addEastTimelineContainer();
    }
    else if (viewType === "PHARMACIST")
    {
      this.add(new tm.jquery.Container({
        cls: 'TextLabel',
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      }));
      this._addEastTimelineContainer();
    }
    else if (viewType === "SUMMARY")
    {
      this._addWestContainer();
    }
    else if (viewType === "DOCUMENTATION")
    {
      this._addWestContainer();
    }
  },

  _addWestContainer: function(expand)
  {
    var self = this;
    var view = this.getView();

    var container = new tm.jquery.Container({
      cls: 'west-container',
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(expand ? 1 : 0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 5)
    });

    if (view.getTherapyAuthority().isManageOutpatientPrescriptionsAllowed() &&
        this.getViewType() === 'DOCUMENTATION'  &&
        view.isSwingApplication())
    {
      var outpatientPrescribingButton = new tm.jquery.Button({
        cls: "btn-flat",
        iconCls: "icon-add-outpatient-prescription",
        tooltip: tm.views.medications.MedicationUtils.createTooltip(
            view.getDictionary("new.outpatient.prescription"), "bottom", view),
        handler: function()
        {
          outpatientPrescribingButton.setEnabled(false);
          view.openOutpatientOrderingDialog();
          clearTimeout(self._outpatientButtonTimer);
          self._outpatientButtonTimer = setTimeout(function()
          {
            outpatientPrescribingButton.setEnabled(true);
          }, 250);
        }
      });
      container.add(outpatientPrescribingButton);
    }

    if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
    {
      var orderPopupMenu = new tm.jquery.PopupMenu();
      orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: "order-therapy-menu-item",
        iconCls: "icon-add",
        text: view.getDictionary("medications.ordering"),
        handler: function()
        {
          self.orderButton.closePopupMenu();
          view.openMedicationOrderingDialog();
        }
      }));

      orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: "previous-host-therapies-menu-item",
        iconCls: "icon-add-to-24",
        text: view.getDictionary('therapy.transfer'),
        handler: function()
        {
          self.orderButton.closePopupMenu();
          view.orderPreviousHospitalizationTherapies();
        }
      }));
      this.orderButton = new tm.jquery.SplitButton({
        cls: "order-medications-button splitbutton-flat",
        iconCls: "icon-add",
        popupMenu: orderPopupMenu,
        handler: function()
        {
          self.orderButton.setEnabled(false);
          view.openMedicationOrderingDialog();
          clearTimeout(self._orderButtonTimer);
          self._orderButtonTimer = setTimeout(function()
          {
            self.orderButton.setEnabled(true);
          }, 250);
        }
      });

      if (view.getTherapyAuthority().isManageOutpatientPrescriptionsAllowed() && view.isSwingApplication())
      {
        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          iconCls: "icon-add-outpatient-prescription",
          text: view.getDictionary('outpatient.prescription'),
          handler: function()
          {
            self.orderButton.closePopupMenu();
            view.openOutpatientOrderingDialog();
          }
        }));
      }

      if (view.isMentalHealthReportEnabled() === true && view.getTherapyAuthority().isMedicationConsentT2T3Allowed())
      {
        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          iconCls: "icon-add",
          text: 'T2',
          handler: function()
          {
            self.orderButton.closePopupMenu();
            view.openT2T3OrderingDialog(app.views.medications.TherapyEnums.mentalHealthDocumentType.T2);
          }
        }));
        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          iconCls: "icon-add",
          text: 'T3',
          handler: function()
          {
            self.orderButton.closePopupMenu();
            view.openT2T3OrderingDialog(app.views.medications.TherapyEnums.mentalHealthDocumentType.T3);
          }
        }));
      }
      container.add(this.orderButton);
    }

    if (view.getTherapyAuthority().isTimelineViewEnabled() &&
        view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
    {
      this.previousHospitalizationTherapiesButton = new tm.jquery.Button({
        cls: "previous-hosp-therapies-button btn-flat",
        iconCls: "icon-add-to-24",
        hidden: true, /* visible based on patient hospitalization data, which is loaded later */
        tooltip: tm.views.medications.MedicationUtils.createTooltip(view.getDictionary("therapy.transfer"), "bottom", view),
        handler: function()
        {
          view.orderPreviousHospitalizationTherapies();
        }
      });
      container.add(this.previousHospitalizationTherapiesButton);
    }

    this.weightButton = new tm.jquery.Button({
      cls: "reference-weight-button btn-flat",
      iconCls: "icon-reference-weight",
      tooltip: tm.views.medications.MedicationUtils.createTooltip(view.getDictionary("reference.weight"), "bottom", view),
      handler: function()
      {
        view.openReferenceWeightDialog(null, null);
      }
    });
    container.add(this.weightButton);
    var printPopupMenu = new tm.jquery.PopupMenu();

    if (view.getTherapyAuthority().isTherapyReportEnabled())
    {
      printPopupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: "print-report-menu-item",
        iconCls: "icon-print",
        text: this.view.getDictionary("print.therapy.report"),
        handler: function()
        {
          self._printTodayTherapyReport();
        }
      }));
    }

    if (view.getTherapyAuthority().isSurgeryReportEnabled())
    {
      printPopupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: "print-surgery-report-menu-item",
        iconCls: "icon-print",
        text: view.getDictionary("therapy.surgery.report"),
        handler: function()
        {
          self._printSurgeryTherapyReport();
        }
      }));
    }

    if (printPopupMenu.hasMenuItems())
    {
      this.printButton = new tm.jquery.SplitButton({
        cls: "print-button splitbutton-flat",
        iconCls: "icon-print",
        popupMenu: printPopupMenu
      });

      this.printButton.setTooltip(tm.views.medications.MedicationUtils.createTooltip(
          view.getDictionary(view.getTherapyAuthority().isTherapyReportEnabled() ? "print.therapy.report" : "therapy.surgery.report"),
          null,
          view));
      this.printButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
      {
        view.getTherapyAuthority().isTherapyReportEnabled() ? self._printTodayTherapyReport() : self._printSurgeryTherapyReport();
      });

      container.add(this.printButton);
    }

    if (this.getViewType() === 'DOCUMENTATION' &&
        view.isSwingApplication() &&
        view.getTherapyAuthority().isManageOutpatientPrescriptionsAllowed())
    {
      var showExternalOutpatientPrescriptionsButton = new tm.jquery.Button({
        cls: "btn-flat",
        iconCls: "icon-show-external-outpatient-prescriptions",
        tooltip: tm.views.medications.MedicationUtils.createTooltip(
            view.getDictionary("get.outpatient.prescriptions.from.eer"),
            "bottom",
            view),
        handler: function(component)
        {
          self._displayExternalOutpatientPrescriptionRequestDataEntryTooltip(component);
        }
      });
      container.add(showExternalOutpatientPrescriptionsButton);
    }

    if (this.getViewType() === "TIMELINE" && view.getTherapyAuthority().isMedicationIdentifierScanningAllowed() &&
        view.getTherapyAuthority().isManageAdministrationsAllowed())
    {
      this._medicationBarcodeField = new tm.jquery.TextField({
        cls: 'medication-barcode-field',
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        placeholder: view.getDictionary("barcode"),
        width: 120
      });
      this._medicationBarcodeField.onKey(new tm.jquery.event.KeyStroke({key: "return"}), function(component)
      {
        if (self.medicationIdentifierScannedFunction && component.getValue())
        {
          view.getRestApi().getAdministrationTaskForBarcode(component.getValue()).then(
              function(barcodeTaskSearch)
              {
                self.medicationIdentifierScannedFunction(barcodeTaskSearch, component.getValue());
                component.setValue(null, true);
              });
        }
      });
      container.add(this._medicationBarcodeField)
    }
    this.add(container);
  },

  _printTodayTherapyReport: function()
  {
    var view = this.view;
    var documentURI = tm.jquery.URI.create(
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPY_PDF_REPORT);

    documentURI.addParam("patientId", view.getPatientId());
    documentURI.addParam("language", view.getViewLanguage());
    documentURI.addParam("ticket", view.getCasTicket());

    window.open(documentURI.toUrl());
  },

  _printEmptyTherapyReport: function()
  {
    var view = this.view;
    view.sendAction("printEmptyTherapyReport", {
      patientId: view.getPatientId()
    });
  },

  _printSurgeryTherapyReport: function()
  {
    var view = this.view;
    view.sendAction("printSurgeryTherapyReport", {
      "patientId": view.getPatientId(),
      "careProviderId": view.getCareProviderId(),
      "patientHeight": view.getPatientHeightInCm()
    });
  },

  _addCenterGridContainer: function()
  {
    var centerContainer = new tm.jquery.Container({
      layout: new tm.jquery.CenterLayout(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      scrollable: 'visible'
    });

    var container = new tm.jquery.Container({
      cls: 'center-grid-container',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("start", "center", 5)
    });

    this.previousButton = new tm.jquery.Button({cls: "left-icon btn-flat", size: 'medium', type: 'filter'});
    this.nextButton = new tm.jquery.Button({cls: "right-icon btn-flat", size: 'medium', type: 'filter'});
    container.add(this.previousButton);
    container.add(this.nextButton);

    centerContainer.add(container);
    this.add(centerContainer);
  },

  _addEastGridContainer: function()
  {
    var self = this;
    var container = new tm.jquery.Container({
      cls: 'east-grid-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 5),
      scrollable: 'visible'
    });

    var numberOfDaysMode = this.view.getContext() ? this.view.getContext().numberOfDaysMode : null;
    var threeDaysButton = new tm.jquery.Button({
      cls: "three-days-button btn-flat",
      text: "3",
      dayCount: 3,
      type: 'filter',
      mode: 'numberOfDaysMode3',
      pressed: numberOfDaysMode == null || numberOfDaysMode == 'numberOfDaysMode3'
    });
    var fiveDaysButton = new tm.jquery.Button({
      cls: "five-days-button btn-flat",
      text: "5",
      dayCount: 5,
      type: 'filter',
      mode: 'numberOfDaysMode5',
      pressed: numberOfDaysMode == 'numberOfDaysMode5'
    });

    this.dayCountButtonGroup = new tm.jquery.ButtonGroup({
      cls: "btn-group-flat",
      orientation: "horizontal",
      type: "radio",
      buttons: [threeDaysButton, fiveDaysButton]
    });
    this.groupingButtonGroup = this._buildGroupingButtonGroup();

    this._buildSortSplitButton();
    var groupsSelectBoxContainer = new tm.jquery.Container({
      style: "position:relative;max-width: 50px;max-height: 50px;",
      scrollable: "visible",
      layout: tm.jquery.VFlexboxLayout.create("center", "stretch", 0)
    });
    groupsSelectBoxContainer.add(new tm.jquery.Button({
      cls: 'btn-flat icon-sort',
      iconCls: "icon-sort-show-menu",
      alignSelf: "flex-end",
      handler: function()
      {
        self.sortButton.openDropdown();
      }
    }));
    this.sortButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      // don't call component.getDom(), it will trigger DOM creation after being destroyed by fast switching views
      // and fail inside the getSelections method of the component.
      $("#" + component.getId()).find(".bootstrap-select button").hide();
    });

    container.add(this.dayCountButtonGroup);
    container.add(this.groupingButtonGroup);
    groupsSelectBoxContainer.add(this.sortButton);
    container.add(groupsSelectBoxContainer);
    this.add(container);
  },

  _buildGroupingButtonGroup: function()
  {
    var groupByMode = this.view.getContext() ? this.view.getContext().groupByMode : null;
    var noGroupButton = new tm.jquery.Button({
      cls: 'no-group-icon no-grouping-button btn-flat',
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("no.grouping"), "left", this.view),
      groupField: null,
      type: 'filter',
      mode: 'groupByModeNone',
      pressed: groupByMode == null || groupByMode == 'groupByModeNone'
    });
    var groupByAtcButton = new tm.jquery.Button({
      cls: "atc-grouping-button btn-flat",
      text: "ATC",
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("group.by.atc.classification"), "left", this.view),
      groupField: "atcGroup",
      type: 'filter',
      mode: 'groupByModeAtc',
      pressed: groupByMode == 'groupByModeAtc'
    });
    var groupByRouteButton = new tm.jquery.Button({
      cls: "apl-grouping-button btn-flat",
      text: "APL",
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("group.by.route"), "left", this.view),
      groupField: "routes",
      type: 'filter',
      mode: 'groupByModeRoute',
      pressed: groupByMode == 'groupByModeRoute'
    });
    var groupByModeCustomGroupButton = new tm.jquery.Button({
      cls: 'custom-group-icon custom-grouping-button btn-flat',
      tooltip: tm.views.medications.MedicationUtils.createTooltip(this.view.getDictionary("group.by.custom.groups"), "left", this.view),
      groupField: "customGroup",
      type: 'filter',
      mode: 'groupByModeCustomGroup',
      pressed: groupByMode == 'groupByModeCustomGroup'
    });

    return new tm.jquery.ButtonGroup({
      cls: "btn-group-flat",
      orientation: "horizontal btn-group-flat",
      type: "radio",
      buttons: [noGroupButton, groupByAtcButton, groupByRouteButton, groupByModeCustomGroupButton]
    });
  },

  _setSortButtonOptions: function()
  {
    var self = this;
    var view = this.view;
    var enums = app.views.medications.TherapyEnums;
    var sortOptions = [];
    var shownTherapiesOptions = [];

    var optionGroups = [];

    sortOptions.push(tm.jquery.SelectBox.createOption(
        new tm.jquery.Object({
          value: enums.therapySortTypeEnum.DESCRIPTION_ASC,
          group: "sort"
        }),
        this.view.getDictionary("Sort.DESCRIPTION_ASC"),
        "sort-name-asc-menu-item",
        "icon-sort-name-asc",
        view.therapySortTypeEnum == enums.therapySortTypeEnum.DESCRIPTION_ASC,
        true
    ));
    sortOptions.push(tm.jquery.SelectBox.createOption(
        new tm.jquery.Object({
          value: enums.therapySortTypeEnum.DESCRIPTION_DESC,
          group: "sort"
        }),
        this.view.getDictionary("Sort.DESCRIPTION_DESC"),
        "sort-name-desc-menu-item",
        "icon-sort-name-desc",
        view.therapySortTypeEnum == enums.therapySortTypeEnum.DESCRIPTION_DESC,
        true
    ));
    sortOptions.push(tm.jquery.SelectBox.createOption(
        new tm.jquery.Object({
          value: enums.therapySortTypeEnum.CREATED_TIME_ASC,
          group: "sort"
        }),
        this.view.getDictionary("Sort.CREATED_TIME_ASC"),
        "sort-time-asc-menu-item",
        "icon-sort-time-asc",
        view.therapySortTypeEnum == enums.therapySortTypeEnum.CREATED_TIME_ASC,
        true
    ));
    sortOptions.push(tm.jquery.SelectBox.createOption(
        new tm.jquery.Object({
          value: enums.therapySortTypeEnum.CREATED_TIME_DESC,
          group: "sort"
        }),
        this.view.getDictionary("Sort.CREATED_TIME_DESC"),
        "sort-time-desc-menu-item",
        "icon-sort-time-desc",
        view.therapySortTypeEnum == enums.therapySortTypeEnum.CREATED_TIME_DESC,
        true
    ));

    for (var i = 0; i < sortOptions.length; i++)
    {
      if (sortOptions[i].selected)
      {
        this.therapySortTypeEnum = sortOptions[i].value.value;
        this.previousSelectedSort = sortOptions[i].value;
      }
    }

    optionGroups.push(tm.jquery.SelectBox.createOptionGroup(this.view.getDictionary("sort"), sortOptions));
    if (this._viewType == "TIMELINE" || this._viewType == "PHARMACIST")
    {
      if (tm.jquery.Utils.isEmpty(this.isDischarged) || !self.isDischarged)
      {
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "activeTherapies",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.active")),
            "sort-shown-therapies-active",
            null,
            true,
            true
        ));
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "threeDaysTherapies",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.three.days")),
            "sort-shown-therapies-threeDays",
            null,
            false,
            true
        ));
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "allTherapies",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.all")),
            "sort-shown-therapies-all",
            null,
            false,
            true
        ));
      }
      else
      {
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "discharge",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.discharge")),
            "sort-shown-therapies-discharge",
            null,
            true,
            true
        ));
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "hospital",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.hospitalization")),
            "sort-shown-therapies-hospital",
            null,
            false,
            true
        ));
      }

      shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
          new tm.jquery.Object({
            value: "customDateTherapies",
            group: "hide"
          }),
          this._buildOptionTextSpan(this.view.getDictionary("choose.date") + "..."),
          "sort-shown-therapies-active",
          null,
          false,
          true
      ));

      this.previousSelectedShownTherapies = shownTherapiesOptions[0].value;
      optionGroups.unshift(tm.jquery.SelectBox.createOptionGroup(this.view.getDictionary("show.therapies"), shownTherapiesOptions));
    }
    if (!tm.jquery.Utils.isEmpty(this.sortButton))
    {
      this.sortButton.setOptionGroups(optionGroups);
    }

    function isOptionSelected(item)
    {
      return item.isSelected();
    }
  },

  _buildOptionTextSpan: function(text)
  {
    return "<span style='margin-left: 36px;'>" + text + "</span>";
  },

  _buildSortSplitButton: function()
  {
    this.sortButton = new tm.jquery.SelectBox({
      hidden: true,
      style: "position:absolute;left:46px;",
      cls: "therapy-sort-button",
      dropdownAlignment: "right",
      multiple: true,
      allowSingleDeselect: true,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return value1 && value2 ? value1.group === value2.group && value1.value === value2.value : value1 === value2;
      }
    });
    this._setSortButtonOptions();

    this.sortButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      component.show();
    });
  },

  _addEastTimelineContainer: function()
  {
    var self = this;

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

    this._buildSortSplitButton();
    var groupsSelectBoxContainer = new tm.jquery.Container({
      cls: 'groups-select-box',
      style: "position:relative;max-width: 50px;max-height: 50px;",
      scrollable: "visible",
      layout: tm.jquery.VFlexboxLayout.create("center", "stretch", 0)
    });
    groupsSelectBoxContainer.add(new tm.jquery.Button({
      cls: 'btn-flat icon-sort',
      iconCls: "icon-sort-show-menu",
      alignSelf: "flex-end",
      handler: function()
      {
        setTimeout(function()
        {
          $(self.sortButton.getDom()).find(".dropdown-menu").css("top", -10);
          $(self.sortButton.getDom()).find(".bootstrap-select button").trigger("click");
        }, 0);
      }
    }));
    this.sortButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      // don't call component.getDom(), it will trigger DOM creation after being destroyed by fast switching views
      // and fail inside the getSelections method of the component.
      $("#" + component.getId()).find(".bootstrap-select button").hide();
    });

    this.add(this.customGroupFilterContainer);
    this.add(this.groupingButtonGroup);
    groupsSelectBoxContainer.add(this.sortButton);
    this.add(groupsSelectBoxContainer);
  },

  _addTimelineDatePicker: function()
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();

    this.timelineDateContainer = new tm.jquery.Container({
      scrollable: 'visible',
      hidden: true,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("center", "center", 0)
    });

    var previousDateButton = new tm.jquery.Button({
      enabled: false,
      cls: 'left-icon btn-flat',
      size: 'medium',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      handler: function()
      {
        var date = new Date(self.timelineDateField.getDate());
        date.setDate(date.getDate() - 1);
        self.timelineDateField.setDate(date);
      }
    });
    this.timelineDateContainer.add(previousDateButton);

    this.timelineDateField = new tm.jquery.DatePicker({
      cls: "timeline-date-field",
      showType: "focus",
      width: 95,
      date: CurrentTime.get(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this.timelineDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      appFactory.createConditionTask(
          function()
          {
            nextDateButton.setEnabled(true);
            previousDateButton.setEnabled(true);
          },
          function(task)
          {
            if (!self.isRendered())
            {
              task.abort();
              return;
            }
            return component.isRendered() && !tm.jquery.Utils.isEmpty(component.getPlugin());
          },
          50, 100
      );
    });
    this.timelineDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      clearTimeout(self._timelineDateFieldTimer);
      {
        self._timelineDateFieldTimer = setTimeout(function()
        {
          self.timelineDateSelectorChangedFunction(self.timelineDateField.getDate());
        }, 250);
      }
    });

    this.timelineDateContainer.add(this.timelineDateField);

    var nextDateButton = new tm.jquery.Button({
      enabled: false,
      cls: 'right-icon btn-flat',
      size: 'medium',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      handler: function()
      {
        var date = new Date(self.timelineDateField.getDate());
        date.setDate(date.getDate() + 1);
        self.timelineDateField.setDate(date);
      }
    });
    this.timelineDateContainer.add(nextDateButton);
    this.add(this.timelineDateContainer);
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

  _setupRoutesFilter: function(routes)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    this.routesFilterContainer.removeAll();

    this.routesFilterButton = appFactory.createCheckboxSplitButton({
      cls: "splitbutton-flat print",
      showSelectedItemsIcon: false,
      showSelectedItemsText: true,
      popupMenuHorizontalAlignment: "right",
      clearSelectedCheckBoxItemText: this.view.getDictionary("all.routes.short"),
      clearSelectedItemsText: this.view.getDictionary("all.routes.short")
    });

    routes = tm.jquery.Utils.isEmpty(routes) ? [] : routes;

    routes.forEach(function(routeName)
    {
      self.routesFilterButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
        text: routeName,
        checked: self.selectedRoutes.contains(routeName),
        data: routeName
      }));
    });

    this.routesFilterButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._timelineFilterChanged(true);
    });

    this.routesFilterContainer.add(this.routesFilterButton);
    this.routesFilterContainer.repaint();
  },

  _setupCustomGroupFilter: function(groups)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    this.customGroupFilterContainer.removeAll();

    if (this.customGroups && this.customGroups.length > 0)
    {
      this.customGroupFilterButton = appFactory.createCheckboxSplitButton({
        cls: "splitbutton-flat filter",
        showSelectedItemsIcon: false,
        showSelectedItemsText: true,
        popupMenuHorizontalAlignment: "right",
        clearSelectedCheckBoxItemText: this.ALL_CUSTOM_GROUPS,
        clearSelectedItemsText: this.ALL_CUSTOM_GROUPS
      });

      groups = tm.jquery.Utils.isEmpty(groups) ? [] : groups;

      for (var j = 0; j < this.customGroups.length; j++)
      {
        var customGroup = this.customGroups[j];
        this.customGroupFilterButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
          text: customGroup,
          checked: this.selectedCustomGroups.contains(customGroup),
          enabled: groups.contains(customGroup),
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
      this.customGroupFilterContainer.repaint();
    }
  },

  /**
   * Display the data entry tooltip to request the display of external outpatient prescriptions
   * for the given patient.
   * @param {tm.jquery.Component} button
   * @private
   */
  _displayExternalOutpatientPrescriptionRequestDataEntryTooltip: function(button)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var entryContainer = new app.views.medications.therapy.ShowExternalPrescriptionsDataEntryContainer({
      view: view
    });

    var popoverTooltip = appFactory.createDataEntryPopoverTooltip(
        view.getDictionary("select.date"),
        entryContainer,
        function(resultData)
        {
          if (resultData && (resultData).success)
          {
            var value = resultData.value;
            if (!tm.jquery.Utils.isEmpty(value))
            {
              var params = {
                patientId: view.getPatientId(),
                searchStart: JSON.stringify(value.startDate),
                searchEnd: JSON.stringify(value.endDate)
              };
              view.sendAction(tm.views.medications.TherapyView.VIEW_ACTION_GET_EXTERNAL_OUTPATIENT_PRESCRIPTIONS, params);
            }
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
    popoverTooltip.setHeight(entryContainer.getDefaultHeight());
    popoverTooltip.setHideOnDocumentClick(false); // issues with the date picker
    button.setTooltip(popoverTooltip);

    setTimeout(function()
    {
      popoverTooltip.show();
    }, 0);
  },

  /** public methods */
  setupTimelineFilter: function(routes, groups)
  {
    if (this._viewType == "TIMELINE" || this._viewType == "PHARMACIST")
    {
      this._setupRoutesFilter(routes);
      this._setupCustomGroupFilter(groups);
      this._timelineFilterChanged(false);
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
      if (selections.length == 1)
      {
        var selected = selections[0];
        if (self.sortButton.optionGroups.length == 1 && selected.group == "sort")
        {
          self.previousSelectedSort = selected;
          self.sortButton.setSelections([selected, self.previousSelectedShownTherapies], true);
        }
        else
        {
          self.sortButton.setSelections([self.previousSelectedSort, self.previousSelectedShownTherapies], true);
          self.sortButton.hidePluginDropdown();
        }
      }
      else if (selections.length > 1)
      {
        self.sortButton.hidePluginDropdown();
        var fireShownTherapiesEvent = false;
        var fireTherapySortEvent = false;
        var fireTimelineCustomDateEvent = false;
        var newSelectedSort;
        var newSelectedShownTherapies;
        for (var i = 0; i < selections.length; i++)
        {
          if (selections[i].group == "hide" && selections[i].value != self.previousSelectedShownTherapies.value)
          {
            newSelectedShownTherapies = selections[i];
            fireShownTherapiesEvent = true;

            if (self.timelineDateField)
            {
              if (newSelectedShownTherapies.value == "customDateTherapies")
              {
                fireTimelineCustomDateEvent = true;
                self.timelineDateField.setDate(CurrentTime.get(), true);
                self.timelineDateContainer.show();
              }
              else
              {
                self.timelineDateContainer.hide();
              }
            }
          }
          if (selections[i].group == "sort" && selections[i].value != self.therapySortTypeEnum)
          {
            newSelectedSort = selections[i];
            fireTherapySortEvent = true;
          }
        }
        if (fireTimelineCustomDateEvent)
        {
          self.previousSelectedShownTherapies = newSelectedShownTherapies;
          newSelectedSort = self.previousSelectedSort;
          self.timelineDateSelectorChangedFunction(self.timelineDateField.getDate());
        }
        else if (fireShownTherapiesEvent)
        {
          self.previousSelectedShownTherapies = newSelectedShownTherapies;
          newSelectedSort = self.previousSelectedSort;
          action(self.therapySortTypeEnum, self.previousSelectedShownTherapies);
        }
        else if (fireTherapySortEvent)
        {
          self.previousSelectedSort = newSelectedSort;
          newSelectedShownTherapies = self.previousSelectedShownTherapies;
          self.therapySortTypeEnum = newSelectedSort.value;
          action(self.therapySortTypeEnum, self.previousSelectedShownTherapies);
        }
        else
        {
          newSelectedShownTherapies = self.previousSelectedShownTherapies;
          newSelectedSort = self.previousSelectedSort;
        }
        self.sortButton.setSelections([newSelectedSort, newSelectedShownTherapies], true);
      }
      else
      {
        self.sortButton.setSelections([self.previousSelectedSort, self.previousSelectedShownTherapies], true);
        self.sortButton.hidePluginDropdown();
      }
    });
  },
  getDefaultTherapiesShownSelection: function()
  {
    return this.sortButton.optionGroups[0].getOptions().get(0).value.value;
  },

  setIsDischarged: function(isDischarged)
  {
    this.isDischarged = isDischarged;
  },
  getFilterContext: function()
  {
    var groupByMode = null;
    if (this.groupingButtonGroup && this.groupingButtonGroup.getSelection().length > 0)
    {
      groupByMode = this.groupingButtonGroup.getSelection()[0].mode
    }
    else if (this.view.getContext())
    {
      groupByMode = this.view.getContext().groupByMode;
    }

    return {
      groupByMode: groupByMode,
      numberOfDaysMode: this.dayCountButtonGroup ? this.dayCountButtonGroup.getSelection()[0].mode : null
    }
  },
  setTherapySortType: function(therapySortTypeEnum)
  {
    for (var j = 0; j < this.sortButton.optionGroups.length; j++)
    {
      for (var i = 0; i < this.sortButton.optionGroups[j].getOptions().length; i++)
      {
        var menuItem = this.sortButton.optionGroups[j].getOptions()[i];
        if (menuItem.value.value == therapySortTypeEnum)
        {
          this.sortButton.selections.add(menuItem.value);
          return menuItem.value.value;
        }
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
        this.dayCountButtonGroup.setSelection([button], true);
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
    if (this.previousHospitalizationTherapiesButton)
    {
      if (isRecentHospitalization)
      {
        this.previousHospitalizationTherapiesButton.show()
      }
      else
      {
        this.previousHospitalizationTherapiesButton.hide()
      }
    }
  },

  setViewType: function(viewType)
  {
    this._viewType = viewType;
    this.removeAll();
    this._buildGui();
  },

  getView: function()
  {
    return this.view;
  },

  getViewType: function()
  {
    return this._viewType;
  },

  resetViewType: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.sortButton))
    {
      this._setSortButtonOptions();
    }
    if (this.timelineDateField)
    {
      this.timelineDateField.setDate(CurrentTime.get(), true);
      this.timelineDateContainer.hide();
    }
  },

  requestBarcodeFieldFocus: function()
  {
    if (this._medicationBarcodeField)
    {
      this._medicationBarcodeField.focus();
    }
  },

  /**
   * @Override
   */
  destroy: function()
  {
    clearTimeout(this._timelineDateFieldTimer);
    this.callSuper();
  }
});