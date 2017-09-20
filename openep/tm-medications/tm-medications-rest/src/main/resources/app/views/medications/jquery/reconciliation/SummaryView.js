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
Class.define('app.views.medications.reconciliation.SummaryView', 'tm.jquery.Container', {
  cls: "rec-summary-view",

  /** public members  */
  view: null,

  _headerRowContainer: null,
  _noTherapiesField: null,
  _rowsContainer: null,

  _scrollbarDetectionTimer: null,
  _scrollbarDetectionTimerInterval: 250,
  _prevScrollbarStatus: null,

  _summaryColumnTherapyDisplayProvider: null,
  _defaultTherapyDisplayProvider: null,

  _preventAdmissionBtnDoubleClick: null,
  _preventDischargeBtnDoubleClick: null,

  _admissionDoubleClickTimer: null,
  _dischargeDoubleClickTimer: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this._summaryColumnTherapyDisplayProvider = new app.views.medications.TherapyDisplayProvider({
      view: this.getView()
    });
    this._defaultTherapyDisplayProvider = new app.views.medications.TherapyDisplayProvider({
      view: this.getView(),
      showChangeHistory: false,
      showChangeReason: false
    });

    this._buildGui();
    this._buildColumnHeaders();
    //this._buildRows();
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));
  },

  _buildColumnHeaders: function ()
  {
    var view = this.getView();
    var self = this;

    var headerRow = new app.views.medications.reconciliation.SummaryRowContainer({
      cls: "header-row column-title"
    });

    var col1Title = this._buildTitleColumnContent(view.getDictionary("medication.on.admission"), "center");
    var openAdmissionDialogIcon = new tm.jquery.Image({
      cls: "admission-meds-reconciliation-icon",
      cursor: "pointer",
      width: 32,
      height: 24,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    openAdmissionDialogIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (self._preventAdmissionBtnDoubleClick !== true)
      {
        self._preventAdmissionBtnDoubleClick = true;
        self._ensureReferenceWeightExists(function()
        {
          self._openAdmissionMedicationReconciliationDialog();
        });
      }
    });
    col1Title.add(openAdmissionDialogIcon);

    var col2Title = this._buildTitleColumnContent(view.getDictionary("discharge.prescription"), "center");
    var openDischargeDialogIcon = new tm.jquery.Image({
      cls: "discharge-meds-reconciliation-icon",
      cursor: "pointer",
      width: 32,
      height: 24,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    openDischargeDialogIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (self._preventDischargeBtnDoubleClick !== true)
      {
        self._preventDischargeBtnDoubleClick = true;
        self._ensureReferenceWeightExists(function()
        {
          self._openDischargeMedicationReconciliationDialog();
        });
      }
    });
    col2Title.add(openDischargeDialogIcon);

    var col3Title = this._buildTitleColumnContent(view.getDictionary("discharge.summary"), "center");

    headerRow.getColumn(0).add(col1Title);
    headerRow.getColumn(1).add(col2Title);
    headerRow.getColumn(2).add(col3Title);

    var contentContainer = new tm.jquery.Container({
      scrollable: "vertical",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });
    contentContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function (component)
    {
      self._onCheckScrollbarTimerTick(component);
    });
    contentContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_DESTROY, function ()
    {
      clearTimeout(self._scrollbarDetectionTimer);
    });

    this._noTherapiesField = this.view.getNoTherapiesField();
    this._rowsContainer = contentContainer;
    this._headerRowContainer = headerRow;

    this.add(headerRow);
    this.add(this._noTherapiesField);
    this.add(contentContainer);

    this._noTherapiesField.hide();
  },

  _onCheckScrollbarTimerTick: function (component)
  {
    clearTimeout(this._scrollbarDetectionTimer);

    var self = this;
    var scrollbars = tm.views.medications.MedicationUtils.isScrollVisible(component);

    if (self._prevScrollbarStatus != scrollbars)
    {
      self._fixHeaderPaddingIfScrollbar();
      self._prevScrollbarStatus = scrollbars;
    }

    this._scrollbarDetectionTimer = setTimeout(function ()
    {
      self._onCheckScrollbarTimerTick(component);
    }, this._scrollbarDetectionTimerInterval);
  },

  _fixHeaderPaddingIfScrollbar: function ()
  {
    var rowsContainer = this.getRowsContainer();
    var headerRowContainer = this.getHeaderRowContainer();
    if (!rowsContainer || !headerRowContainer) return;

    var self = this;

    if (tm.views.medications.MedicationUtils.isScrollVisible(rowsContainer))
    {
      var scrollbarWidth = tm.views.medications.MedicationUtils.getScrollbarWidth();
      if (scrollbarWidth)
      {
        headerRowContainer.setPadding("0 " + scrollbarWidth + " 0 0");
      }
    }
    else
    {
      headerRowContainer.setPadding(0);
    }
  },

  _ensureReferenceWeightExists: function(callback)
  {
    var view = this.getView();
    if (!tm.jquery.Utils.isEmpty(view.getReferenceWeight()))
    {
      callback();
    }
    else
    {
      var patientData = view.getPatientData();
      var patientWeight = !tm.jquery.Utils.isEmpty(patientData) ? patientData.weightInKg : null;
      view.openReferenceWeightDialog(patientWeight, function()
      {
        callback();
      });
    }
  },

  _openAdmissionMedicationReconciliationDialog: function ()
  {
    var view = this.getView();
    var self = this;
    var centralCaseData = view.getCentralCaseData();

    if (tm.jquery.Utils.isEmpty(centralCaseData) || tm.jquery.Utils.isEmpty(centralCaseData.outpatient))
    {
      var errorSystemDialog = view.getAppFactory().createErrorSystemDialog(view.getDictionary("patient.not.hospitalised") + ".", 400, 122);
      errorSystemDialog.show();
      return;
    }

    var content = new app.views.medications.reconciliation.AdmissionInpatientMedReconciliationEntryContainer({
      view: view
    });
    var buttonText = {
      confirmText: view.getDictionary("confirm"),
      cancelText: view.getDictionary("cancel"),
      nextText: view.getDictionary("confirm"),
      backText: view.getDictionary("admission")
    };
    var dialog = tm.views.medications.MedicationUtils.createWizardDialog(view,
        view.getDictionary("medication.on.admission"), content,
        function(resultData) {
          if (resultData != null && resultData.isSuccess())
          {
            self.refreshData();
          }
        },
        $(window).width() - 50, $(window).height() - 150, buttonText);
    content.setDialog(dialog);
    dialog.setContainmentElement(view.getDom());
    dialog.setFitSize(true);
    dialog.setHideOnEscape(false);
    dialog.show();

    clearTimeout(this._admissionDoubleClickTimer);
    this._admissionDoubleClickTimer = setTimeout(function()
    {
      self._preventAdmissionBtnDoubleClick = false;
    }, 250);
  },

  _openDischargeMedicationReconciliationDialog: function ()
  {
    var view = this.getView();
    var self = this;
    var centralCaseData = view.getCentralCaseData();

    if (tm.jquery.Utils.isEmpty(centralCaseData) || tm.jquery.Utils.isEmpty(centralCaseData.outpatient))
    {
      var errorSystemDialog = view.getAppFactory().createErrorSystemDialog(view.getDictionary("patient.not.hospitalised") + ".", 400, 122);
      errorSystemDialog.show();
      return;
    }

    var content = new app.views.medications.reconciliation.DischargeMedReconciliationEntryContainer({
      view: view
    });
    var dischargeMedsDialog = view.getAppFactory().createDataEntryDialog(
        view.getDictionary("medication.on.discharge"),
        null,
        content, function (resultData)
        {
          if (resultData != null && resultData.isSuccess())
          {
            self.refreshData();
          }
        },
        $(window).width() - 50,
        $(window).height() - 10
    );
    dischargeMedsDialog.setContainmentElement(view.getDom());
    dischargeMedsDialog.setFitSize(true);
    dischargeMedsDialog.setHideOnEscape(false);
    dischargeMedsDialog.getFooter().setCls("reconciliation-dialog-buttons");
    content.setDialog(dischargeMedsDialog);

    dischargeMedsDialog.show();

    clearTimeout(this._dischargeDoubleClickTimer);
    this._dischargeDoubleClickTimer = setTimeout(function()
    {
      self._preventDischargeBtnDoubleClick = false;
    }, 250);
  },

  _buildTitleColumnContent: function (titleText, textAlign)
  {
    var columnContent = new tm.jquery.Container({
      cls: "column-title",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    columnContent.add(new tm.jquery.Component({
      cls: "PortletHeading1" + (tm.jquery.Utils.isEmpty(textAlign) ? "" : (" align-" + textAlign)),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      html: titleText
    }));
    return columnContent;
  },

  _buildTitleColumnEmptyContent: function(withBorder) {
    return new tm.jquery.Container({
      cls: "empty-title" + (withBorder === true ? " right-border" : ""),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
  },

  _buildRows: function (data)
  {
    var rowsContainer = this.getRowsContainer();
    var groupMap = new tm.jquery.HashMap();
    var groupEnums = app.views.medications.TherapyEnums.reconciliationRowGroupEnum;
    var view = this.getView();
    var defaultDisplayProvider = this.getDefaultTherapyDisplayProvider();
    var summaryColumnDisplayProvider = this.getSummaryColumnTherapyDisplayProvider();

    groupMap.put(groupEnums.NOT_CHANGED, []);
    groupMap.put(groupEnums.CHANGED, []);
    groupMap.put(groupEnums.ONLY_ON_ADMISSION, []);
    groupMap.put(groupEnums.ONLY_ON_DISCHARGE, []);

    data.forEach(function (row)
    {
      groupMap.get(row.groupEnum).push(row);
    }, this);

    rowsContainer.removeAll();

    groupMap.keys().forEach(function (key)
    {
      var rows = groupMap.get(key);
      if (rows.length > 0)
      {
        var groupTitleRow = this._buildGroupTitleRow(key);
        rowsContainer.add(groupTitleRow);

        rows.forEach(function (row)
        {
          var summaryRow = new app.views.medications.reconciliation.SummaryRowContainer();

          if (!tm.jquery.Utils.isEmpty(row.therapyOnAdmission))
          {
            summaryRow.getColumn(0).add(new app.views.medications.common.TherapyContainer({
              view: view,
              displayProvider: defaultDisplayProvider,
              data: new app.views.medications.reconciliation.dto.SummaryRowTherapyData({
                therapy: app.views.medications.common.TherapyJsonConverter.convert(row.therapyOnAdmission)
              })
            }));
          }

          if (!tm.jquery.Utils.isEmpty(row.therapyOnDischarge))
          {
            summaryRow.getColumn(1).add(new app.views.medications.common.TherapyContainer({
              view: view,
              displayProvider: defaultDisplayProvider,
              data: new app.views.medications.reconciliation.dto.SummaryRowTherapyData({
                therapy: app.views.medications.common.TherapyJsonConverter.convert(row.therapyOnDischarge)
              })
            }));
          }

          var summaryTherapy = !tm.jquery.Utils.isEmpty(row.therapyOnDischarge) ?
              row.therapyOnDischarge : row.therapyOnAdmission;

          var data = new app.views.medications.reconciliation.dto.SummaryRowTherapyData({
            therapy: app.views.medications.common.TherapyJsonConverter.convert(summaryTherapy),
            changeReasonDto: app.views.medications.common.dto.TherapyChangeReason.fromJson(row.changeReasonDto),
            changes: row.changes,
            therapyStatus: row.statusEnum
          });
          summaryRow.getColumn(2).add(new app.views.medications.common.TherapyContainer({
            view: view,
            data: data,
            displayProvider: summaryColumnDisplayProvider
          }));
          rowsContainer.add(summaryRow);
        }, this);
      }
    }, this);

    if (rowsContainer.isRendered()) rowsContainer.repaint();
  },

  _buildGroupTitleRow: function (groupEnum)
  {
    var groupEnums = app.views.medications.TherapyEnums.reconciliationRowGroupEnum;
    var titleRow = new app.views.medications.reconciliation.SummaryRowContainer({
      cls: "header-row"
    });
    var view = this.getView();

    switch (groupEnum)
    {
      case groupEnums.CHANGED:
        titleRow.getColumn(0).add(this._buildTitleColumnEmptyContent(false));
        titleRow.getColumn(1).add(this._buildTitleColumnEmptyContent(true));
        titleRow.getColumn(2).add(this._buildTitleColumnContent(view.getDictionary("changed.therapies.short"), "left"));
        break;
      case groupEnums.NOT_CHANGED:
        titleRow.getColumn(0).add(this._buildTitleColumnContent(view.getDictionary("existing.therapies.short"), "left"));
        titleRow.getColumn(1).add(this._buildTitleColumnContent("", "left"));
        titleRow.getColumn(2).add(this._buildTitleColumnContent(view.getDictionary("unchanged.therapies.short"), "left"));
        break;
      case groupEnums.ONLY_ON_ADMISSION:
        titleRow.getColumn(0).add(this._buildTitleColumnEmptyContent(false));
        titleRow.getColumn(1).add(this._buildTitleColumnEmptyContent(true));
        titleRow.getColumn(2).add(this._buildTitleColumnContent(view.getDictionary("stop.past"), "left"));
        break;
      case groupEnums.ONLY_ON_DISCHARGE:
        titleRow.getColumn(0).add(this._buildTitleColumnEmptyContent(false));
        titleRow.getColumn(1).add(this._buildTitleColumnEmptyContent(true));
        titleRow.getColumn(2).add(this._buildTitleColumnContent(view.getDictionary("new.therapies.short"), "left"));
        break;
      default:
        break;
    }
    ;

    return titleRow;
  },

  _loadSummaryRows: function (successCallback)
  {
    var view = this.getView();
    var self = this;
    var getReconciliationGroupsUrl = this.getView().getViewModuleUrl() +
        tm.views.medications.TherapyView.SERVLET_PATH_GET_RECONCILIATION_GROUPS;

    view.showLoaderMask();

    var patientId = view.getPatientId();
    var centralCaseData = view.getCentralCaseData();
    var hospitalizationStart = CurrentTime.get();

    if (centralCaseData && centralCaseData.outpatient === false && centralCaseData.centralCaseEffective
        && centralCaseData.centralCaseEffective.startMillis)
    {
      hospitalizationStart = new Date(centralCaseData.centralCaseEffective.startMillis);
    }

    var params = {
      patientId: patientId,
      hospitalizationStart: JSON.stringify(hospitalizationStart),
      saveDateTime: null,
      language: view.getViewLanguage()
    };

    this.getView().loadViewData(getReconciliationGroupsUrl, params, null, function (data)
    {
      view.hideLoaderMask();
      successCallback(data);
    });
  },

  refreshData: function ()
  {
    var self = this;
    this._loadSummaryRows(function(data)
    {
      console.log(data);
      if (data.isEmpty())
      {
        self._noTherapiesField.show();
        self._rowsContainer.hide();
      }
      else
      {
        self._noTherapiesField.hide();
        self._rowsContainer.show();
      }
      self._buildRows(data);
    })
  },

  clearData: function ()
  {
    if (this.getRowsContainer() != null)
    {
      this.getRowsContainer().removeAll();
    }
  },

  getView: function ()
  {
    return this.view;
  },

  getRowsContainer: function ()
  {
    return this._rowsContainer;
  },

  getHeaderRowContainer: function ()
  {
    return this._headerRowContainer;
  },

  getSummaryColumnTherapyDisplayProvider: function ()
  {
    return this._summaryColumnTherapyDisplayProvider;
  },
  getDefaultTherapyDisplayProvider: function ()
  {
    return this._defaultTherapyDisplayProvider;
  },
  /**
   * @Override
   */
  destroy: function()
  {
    clearTimeout(this._admissionDoubleClickTimer);
    clearTimeout(this._dischargeDoubleClickTimer);
    this.callSuper();
  }

});