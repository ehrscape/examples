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
Class.define('app.views.medications.ordering.SearchContainer', 'tm.jquery.Container', {
  scrollable: 'visible',
  view: null,
  searchField: null,
  universalOrderButton: null,
  medicationSelectedEvent: null,
  additionalFilter : null,

  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this.displayProvider = new app.views.medications.TherapyDisplayProvider({view: config.view});
    this._buildComponents();
    this._buildGui();

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'ordering-search-container',
      view: this.getView(),
      component: this,
      manualMode: true
    });

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self.getView().getAppFactory().createConditionTask(
          function()
          {
            self.searchField.focus();
            self._testRenderCoordinator.insertCoordinator();
          },
          function(task)
          {
            if(!self.isRendered())
            {
              task.abort()
            }
            return self.isRendered(true) && $(self.searchField.getDom()).isVisible();
          },
          1000, 50
      );
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    this.searchField = new app.views.medications.common.MedicationSearchField({
      cls: "medication-search-field",
      placeholder: view.getDictionary("search.medication") + "...",
      view: view,
      width: 650,
      dropdownAppendTo: view.getAppFactory().getDefaultRenderToElement(), /* due to the dialog use */
      dropdownHorizontalAlignment: "auto",
      dropdownVerticalAlignment: "auto",
      dropdownWidth: 642,
      dropdownMaxWidth: 642,
      dropdownHeight: "auto",
      dropdownMaxHeight: 400,
      additionalFilter: this.additionalFilter
    });

    this.searchField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
    {
      var medication = component.getSelectionMedication();
      if (medication)
      {
        self._readMedicationData(medication.getId());
      }
    });

    var popupMenu = appFactory.createPopupMenu();

    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      cls: "open-universal-ordering",
      text: view.getDictionary("universal.form"),
      handler: function()
      {
        self._openUniversalMedicationDataDialog()
      },
      iconCls: 'icon-add-universal'
    }));

    this.universalOrderButton = new tm.jquery.Image({
      cls: 'icon-show-universal-ordering',
      width: 46,
      height: 34,
      cursor: 'pointer'
    });
    this.universalOrderButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      if (component.isEnabled())
      {
        popupMenu.show(elementEvent);
      }
    });
  },

  _buildGui: function()
  {
    var searchFieldContainer = new tm.jquery.Container({
      scrollable: 'visible',
      cls: "search-container",
      layout: tm.jquery.HFlexboxLayout.create("center", "center")
    });
    searchFieldContainer.add(this.searchField);
    searchFieldContainer.add(this.universalOrderButton);

    this.add(searchFieldContainer);
  },

  /**
   * @param {string} medicationId
   * @private
   */
  _readMedicationData: function(medicationId)
  {
    var self = this;
    this.getView().getRestApi().loadMedicationData(medicationId).then(function onDataLoad(medicationData)
    {
      self.medicationSelectedEvent(medicationData);
    });
  },

  _openUniversalMedicationDataDialog: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var universalMedicationDataContainer = new app.views.medications.ordering.UniversalMedicationDataContainer({
      view: view
    });
    var universalMedicationDataDialog = appFactory.createDataEntryDialog(
        view.getDictionary("universal.form"),
        null,
        universalMedicationDataContainer,
        function(resultData)
        {
          if (resultData)
          {
            self.medicationSelectedEvent(resultData.value);
          }
        },
        468,
        240
    );
    if (view.getDoseForms().length == 0)
    {
      view.loadDoseForms();
    }
    universalMedicationDataDialog.show();
  },

  /**
   * @returns {tm.jquery.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.MedicationSearchField}
   */
  getSearchField: function()
  {
    return this.searchField;
  },

  abortUnlicensedMedicationSelection: function()
  {
    this.getSearchField().abortUnlicensedMedicationSelection();
  },

  /** public methods */
  clear: function()
  {
    this.searchField.setSelection(null);
    this.searchField.focus();
  }
});