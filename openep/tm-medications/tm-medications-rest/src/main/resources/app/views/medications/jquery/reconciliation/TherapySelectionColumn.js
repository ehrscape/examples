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
Class.define('app.views.medications.reconciliation.TherapySelectionColumn', 'app.views.medications.ordering.OrderingContainer', {
  titleText: null,
  view: null,

  suspendCancelSupport: null,

  therapyGroupsContainer: null,

  Constructor: function (config)
  {
    this.callSuper(config);

    this.suspendCancelSupport = this.getConfigValue("suspendCancelSupport", false);
  },

  /***
   * Override
   */
  buildTherapySelectionCardContent: function()
  {
    var self = this;
    var contentScrollContainer = new tm.jquery.Container({
      cls: "content-scroll-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      scrollable: 'vertical',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var therapyGroupsContainer =  new tm.jquery.Container({
      cls: "therapy-groups-list",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    this.attachTherapyGroupPanels(therapyGroupsContainer);
    this.therapyGroupsContainer = therapyGroupsContainer;

    contentScrollContainer.add(this.therapyGroupsContainer);
    contentScrollContainer.add(this.templatesContainer);

    this.getTherapySelectionCard().add(this.searchContainer);
    this.getTherapySelectionCard().add(contentScrollContainer);
  },

  /* Override to build therapy lists. */
  attachTherapyGroupPanels: function(container)
  {
    // do nothing
  },

  /**
   * Override
   */
  createHeadContainer: function()
  {
    var header = null;

    if (!tm.jquery.Utils.isEmpty(this.getTitleText()))
    {
      var patientDataContainer = new tm.jquery.Container({
        cls: 'TextLabel patinet-data-container',
        horizontalAlign: 'right',
        html: tm.views.medications.MedicationUtils.getPatientsReferenceWeightAndHeightHtml(this.view),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });

      header = new app.views.medications.ordering.MedicationsTitleHeader({
        view: this.getView(),
        title: this.getTitleText(),
        additionalDataContainer: patientDataContainer
      });
    }

    return header;
  },

  /**
   * Override, do nothing
   */
  refreshBasketFunction: function()
  {

  },

  /**
   * Override, do nothing
   */
  saveDateTimePaneEvent: function()
  {

  },

  /* override, do nothing for now */
  getPatientMedsForWarningsFunction: function()
  {

  },

  findTherapyGroupPanelByEnum: function(groupEnum){
    var container = this.getTherapyGroupsContainer();
    if (container && !tm.jquery.Utils.isEmpty(groupEnum)) {
      for (var idx = 0; idx < container.getComponents().length; idx++) {
        var panel = container.getComponents()[idx];
        if (panel && panel instanceof app.views.medications.common.TherapyGroupPanel && panel.getGroupId() === groupEnum)
        {
          return panel;
        }
      }
    }
    return null;
  },

  getTherapyGroupPanelContentByGroupEnum: function(groupEnum, processCallback){
    var panel = this.findTherapyGroupPanelByEnum(groupEnum);
    if (panel){
      var panelContent = panel.getContent();
      if (processCallback) {
        processCallback(panelContent.getComponents());
      }
      else
      {
        return panelContent.getComponents();
      }
    }
    return [];
  },

  showList: function(){
    this.clear();
  },

  getTitleText: function()
  {
    return this.titleText;
  },

  getView: function ()
  {
    return this.view;
  },

  setSourceTherapyContainer: function(container) {
    this._sourceTherapyContainer = container;
  },

  getSourceTherapyContainer: function(){
    return this._sourceTherapyContainer;
  },

  getTherapyGroupsContainer: function()
  {
    return this.therapyGroupsContainer;
  },

  /**
   * Override, set the source therapy container to null when selected via the search container.
   */
  _handleMedicationSelected: function (medicationData, clear)
  {
    this.setSourceTherapyContainer(null);
    this.callSuper(medicationData, clear);
  },

  editTherapy: function (therapyContainer, changeReasonRequired, callback)
  {
    if (therapyContainer.getData() instanceof app.views.medications.common.dto.TherapyTemplateElement)
    {
      this.setSourceTherapyContainer(null);
    }
    else
    {
      this.setSourceTherapyContainer(therapyContainer);
    }

    this.callSuper(therapyContainer, changeReasonRequired, callback);
  }
});