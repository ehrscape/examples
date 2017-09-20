/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.mentalHealth.T2T3TherapySelectionColumn', 'tm.jquery.Container', {
  cls: "ordering-container",
  selectedCls: "item-selected", // container css when it's added to basket

  /** configs */
  view: null,
  displayProvider: null,

  addTherapiesToBasketFunction: null,
  getBasketTherapiesFunction: null,
  fillMentalHealthDisplayValueFunction: null, // fill html display value for therapy
  fillMentalHealthTemplateDisplayValueFunction: null, // fill html display value for mental health template
  onMedicationSelected: null,

  /** privates: components */
  header: null,

  searchContainer: null,
  mentalHealthTemplatesContainer: null,
  activeMentalHealthDrugsContainer: null,
  abortedMentalHealthDrugsContainer: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /** private methods */
  _buildGui: function ()
  {
    var self = this;
    var view = self.getView();

    this.header = new app.views.medications.ordering.MedicationsTitleHeader({
    view: view,
    title: view.getDictionary('therapy.order')
    });

    this.searchContainer = new app.views.medications.ordering.SearchContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      additionalFilter: app.views.medications.TherapyEnums.medicationFinderFilterEnum.MENTAL_HEALTH,
      medicationSelectedEvent: function (medicationData)
      {
        self._handleMedicationSelected(medicationData);
      }
    });

    this.mentalHealthTemplatesContainer =  new app.views.medications.mentalHealth.TherapyGroupPanel({
      groupTitle: view.getDictionary("mental.health.groups"),
      groupId: app.views.medications.TherapyEnums.mentalHealthGroupEnum.TEMPLATES,
      view: view,
      contentData: [],
      dynamicContent: true,
      selectedCls: self.selectedCls,
      addAllEnabled: false,
      displayProvider: self.displayProvider,
      addMedicationToBasketFunction: function(therapyContainer)
      {
        self._onAddTherapyToBasket(therapyContainer.getData(), therapyContainer);
      },
      getBasketTherapiesFunction: function()
      {
        return self.getBasketTherapies();
      }
    });

    this.activeMentalHealthDrugsContainer = new app.views.medications.mentalHealth.TherapyGroupPanel({
      groupTitle: view.getDictionary("active.inpatient.medications"),
      groupId: app.views.medications.TherapyEnums.mentalHealthGroupEnum.INPATIENT_ACTIVE,
      view: view,
      contentData: [],
      dynamicContent: true,
      selectedCls: self.selectedCls,
      addAllEnabled: true,
      displayProvider: self.displayProvider,
      addMedicationToBasketFunction: function(therapyContainer)
      {
        self._onAddTherapyToBasket(therapyContainer.getData(), therapyContainer);
      },
      addAllMedicationsToBasketFunction: function(groupContainer)
      {
        self._onAddWholeGroupToBasket(groupContainer);
      },
      getBasketTherapiesFunction: function()
      {
        return self.getBasketTherapies();
      }
    });

    this.abortedMentalHealthDrugsContainer = new app.views.medications.mentalHealth.TherapyGroupPanel({
      groupTitle: view.getDictionary("stopped.and.suspended.medications"),
      groupId: app.views.medications.TherapyEnums.mentalHealthGroupEnum.INPATIENT_ABORTED,
      view: view,
      contentData: [],
      dynamicContent: true,
      selectedCls: self.selectedCls,
      addAllEnabled: true,
      displayProvider: self.displayProvider,
      addMedicationToBasketFunction: function(therapyContainer)
      {
        self._onAddTherapyToBasket(therapyContainer.getData(), therapyContainer);
      },
      addAllMedicationsToBasketFunction: function(groupContainer)
      {
        self._onAddWholeGroupToBasket(groupContainer);
      },
      getBasketTherapiesFunction: function()
      {
        return self.getBasketTherapies();
      }
    });

    this._setCurrentMentalHealthDrugsGroups();
    this._setMentalHealthTemplatesGroup();

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    var contentScrollContainer = new tm.jquery.Container({
      cls: "content-scroll-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      scrollable: 'vertical',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    contentScrollContainer.add(this.activeMentalHealthDrugsContainer);
    contentScrollContainer.add(this.abortedMentalHealthDrugsContainer);
    contentScrollContainer.add(this.mentalHealthTemplatesContainer);

    this.add(this.header);
    this.add(this.searchContainer);
    this.add(contentScrollContainer);
  },

  _handleMedicationSelected: function(medicationData)
  {
    this.onMedicationSelected(medicationData);
    this.searchContainer.clear();
  },

  _onAddTherapyToBasket: function(elementData, therapyContainer)
  {
    this._addToBasket(elementData);
    this._updateGroupPaneDataOnAddedToBasket(elementData);

    if (!tm.jquery.Utils.isEmpty(therapyContainer))
    {
      var selectedCls = this.selectedCls;
      var oldCls = therapyContainer.getCls();
      therapyContainer.setCls(tm.jquery.Utils.isEmpty(oldCls) ? selectedCls : (oldCls + " " + selectedCls));
      therapyContainer.applyCls(therapyContainer.getCls());
      therapyContainer.getToolbar().hide();
    }
  },

  _onAddWholeGroupToBasket: function(groupContainer)
  {
    var self = this;
    var isGroupContainerCollapsed = groupContainer.isCollapsed();
    var contentData = groupContainer.getContentData();
    var currentBasketTherapies = self.getBasketTherapies();

    contentData.forEach(function(data)
    {
      if (tm.jquery.Utils.isEmpty(currentBasketTherapies) || !currentBasketTherapies.contains(data))
      {
        var therapyContainer = isGroupContainerCollapsed ? null : groupContainer.getElementContainerByData(data);
        self._onAddTherapyToBasket(data, therapyContainer);
      }
    });
  },

  _setMentalHealthTemplatesGroup: function()
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var view = this.getView();
    view.showLoaderMask();

    var getDataUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_MENTAL_HEALTH_TEMPLATES;

    var params = {
      language: view.getViewLanguage()
    };

    view.loadViewData(getDataUrl, params, null, function(data)
    {
      if (!tm.jquery.Utils.isEmpty(data))
      {
        var mentalHealthTemplatesContentData = [];

        var id = 0;
        data.forEach(function(template) // java MentalHealthTemplateDto
        {
          self.fillMentalHealthTemplateDisplayValueFunction(template);

          var data = {
            group: enums.mentalHealthGroupEnum.TEMPLATES,
            id: id,
            therapy: template
          };
          id ++;

          mentalHealthTemplatesContentData.push(data);
        });
      }
      self.mentalHealthTemplatesContainer.setContentData(mentalHealthTemplatesContentData, true);
      view.hideLoaderMask();
    });
  },

  _updateGroupPaneDataOnAddedToBasket: function(elementData)
  {
    var groupContainer = this._getCorrectGroupContainerByGroupEnum(elementData.group);
    groupContainer.setElementAddedToBasket(elementData, true);
  },

  _addToBasket: function(data)
  {
    this.addTherapiesToBasketFunction([data]);
  },

  _setCurrentMentalHealthDrugsGroups: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var self = this;
    var view = this.getView();
    var patientId = view.getPatientId();
    var centralCaseData = view.getCentralCaseData();
    var lastHospitalizationStart = CurrentTime.get();

    view.showLoaderMask();
    if (centralCaseData.outpatient === false && centralCaseData.centralCaseEffective)
    {
      if (centralCaseData.centralCaseEffective.startMillis)
      {
        lastHospitalizationStart = new Date(centralCaseData.centralCaseEffective.startMillis);
      }
    }

    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_CURRENT_HOSPITALIZATION_MENTAL_HEALTH_DRUGS;
    var params = {
      patientId: patientId,
      hospitalizationStart: JSON.stringify(lastHospitalizationStart),
      language: view.getViewLanguage()
    };

    var activeMentalHealthDrugs = [];
    var notActiveMentalHealthDrugs = [];

    view.loadViewData(url, params, null, function(mentalHealthDrugs) // java MentalHealthDrugDto
    {
      if (mentalHealthDrugs && mentalHealthDrugs.length != 0)
      {
        var id = 1;
        mentalHealthDrugs.forEach(function(therapy)
        {
          var therapyActive = therapy.therapyStatusEnum == enums.therapyStatusEnum.NORMAL;
          self.fillMentalHealthDisplayValueFunction(therapy, true); // boolean display Route

          var data = {
            group: therapyActive ? enums.mentalHealthGroupEnum.INPATIENT_ACTIVE : enums.mentalHealthGroupEnum.INPATIENT_ABORTED,
            id: id,
            therapy: therapy,
            therapyStatus: therapy.therapyStatusEnum
          };
          id ++;

          if (therapyActive)
          {
            activeMentalHealthDrugs.push(data);
          }
          else
          {
            notActiveMentalHealthDrugs.push(data);
          }
        });
      }
      self.activeMentalHealthDrugsContainer.setContentData(activeMentalHealthDrugs, true);
      self.abortedMentalHealthDrugsContainer.setContentData(notActiveMentalHealthDrugs, false);
      view.hideLoaderMask();
    });
  },

  _getCorrectGroupContainerByGroupEnum: function(groupEnum)
  {
    var enums =  app.views.medications.TherapyEnums;
    if (groupEnum == enums.mentalHealthGroupEnum.INPATIENT_ABORTED)
    {
      return this.abortedMentalHealthDrugsContainer;
    }
    else if (groupEnum == enums.mentalHealthGroupEnum.INPATIENT_ACTIVE)
    {
      return this.activeMentalHealthDrugsContainer;
    }
    else
    {
      return this.mentalHealthTemplatesContainer;
    }
  },

  handleBasketTherapiesRemoved: function(dataRemoved)
  {
    var self = this;
    dataRemoved.forEach(function(elementData)
    {
      var groupContainer = self._getCorrectGroupContainerByGroupEnum(elementData.group);
      groupContainer.setElementAddedToBasket(elementData, false);

      if (!tm.jquery.Utils.isEmpty(elementData))
      {
        var therapyContainer = groupContainer.getElementContainerByData(elementData);

        if (!tm.jquery.Utils.isEmpty(therapyContainer))
        {
          var oldCls = tm.jquery.Utils.isEmpty(therapyContainer.getCls()) ? "" : therapyContainer.getCls();
          if (oldCls.contains(self.selectedCls))
          {
            therapyContainer.setCls(therapyContainer.getCls().replace(" " + self.selectedCls, ""));
            therapyContainer.getToolbar().show();
          }
        }
      }
    });
  },

  getBasketTherapies: function()
  {
    return this.getBasketTherapiesFunction();
  },

  clear: function ()
  {
    this.searchContainer.clear();
  },

  getView: function()
  {
    return this.view;
  }
});
