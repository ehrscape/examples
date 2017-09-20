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
Class.define('app.views.medications.reconciliation.DischargeMedsReconciliationContainer', 'app.views.medications.reconciliation.AdmissionMedReconciliationContainer', {
  groupPanelsAttached: false,
  warningsEnabled: true,
  abortedTherapiesPanel: null,
  showSource: false,

  Constructor: function (config)
  {
    // override before buildGUI sets the instance to parent components
    this.basketTherapyDisplayProvider = new app.views.medications.common.MedicationOnDischargeTherapyDisplayProvider({
      view: config.view
    });

    this.callSuper(config);

    this.getBasketTherapyDisplayProvider().setShowChangeReason(true);
    this.getBasketTherapyDisplayProvider().setShowChangeHistory(false);
  },

  attachTherapyGroupPanels: function (container)
  {
    var self = this;
    var view = this.getView();
    var getDischargeGroupsUrl =
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPY_GROUPS_ON_DISCHARGE;
    var centralCaseData = view.getCentralCaseData();
    var lastHospitalizationStart = CurrentTime.get();
    var hospitalizationActive = null;
    var displayProvider = new app.views.medications.TherapyDisplayProvider({
      view: view,
      showChangeReason: true,
      showChangeHistory: false,
      getBigIconContainerHtml: function(dto)
      {
        var view = this.getView();
        var appFactory = view.getAppFactory();
        var options = this.getBigIconContainerOptions(dto);
        if (dto.getTherapy().isLinkedToAdmission())
        {
          options.layers.push({hpos: "left", vpos: "top", cls: "icon_linked_to_admission"});
        }
        return appFactory.createLayersContainerHtml(options);
      }
    });

    if (centralCaseData.outpatient === false && centralCaseData.centralCaseEffective)
    {
      if (centralCaseData.centralCaseEffective.startMillis)
      {
        lastHospitalizationStart = new Date(centralCaseData.centralCaseEffective.startMillis);
      }
      if (centralCaseData.centralCaseEffective.endMillis)
      {
        var endDate = new Date(centralCaseData.centralCaseEffective.endMillis);
        var now = CurrentTime.get();
        hospitalizationActive = endDate <= now ? false : true;
      }
      else
      {
        hospitalizationActive = false;
      }
    }

    var params = {
      patientId: view.getPatientId(),
      patientHeight: view.getPatientHeightInCm(),
      lastHospitalizationStart: JSON.stringify(lastHospitalizationStart),
      hospitalizationActive: hospitalizationActive,
      language: view.getViewLanguage()
    };

    view.loadViewData(getDischargeGroupsUrl, params, null, function (data)
    {
      if (!tm.jquery.Utils.isEmpty(data) && tm.jquery.Utils.isArray(data))
      {
        data.forEach(function (therapyGroupData)
        {
          var containerData = app.views.medications.reconciliation.dto.MedicationOnDischargeGroup.fromJson(therapyGroupData);

          container.add(new app.views.medications.common.TherapyGroupPanel({
            groupTitle: containerData.getName(),
            groupId: containerData.getGroupEnum(),
            view: this.getView(),
            contentData: containerData.getGroupElements(),
            attachElementToolbar: function (container)
            {
              self._attachGroupPanelElementToolbar(container);
            },
            displayProvider: displayProvider
          }));
        }, self);

        var abortedTherapiesPanel = new app.views.medications.common.TherapyGroupPanel({
          groupTitle:  view.getDictionary("stop.past"),
          view: view,
          contentData: [],
          expanded: false,
          attachElementToolbar: function (container)
          {
            self._attachGroupPanelElementToolbar(container);
          },
          displayProvider: self.getBasketTherapyDisplayProvider()
        });
        container.add(abortedTherapiesPanel);

        self.abortedTherapiesPanel = abortedTherapiesPanel;
      }

      container.repaint();

      view.getAppFactory().createConditionTask(  //wait for the medications to load
          function ()
          {
            self.groupPanelsAttached = true;
          },
          function ()
          {
            return container.isRendered(true);
          },
          250, 100
      );
    });
  },

  /* Override to wrap the therapy into a medicationonadmission object */
  addToBasket: function (therapyDto, changeReason, sourceContainer)
  {
    var self = this;
    var statusEnums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;
    var actionEnum = statusEnums.PRESCRIBED;
    var sourceData = null;
    var abortedTherapiesPanel = this.getAbortedTherapiesPanel();
    // in case of add with edit, no source container is passed over, check if there is one
    if (!sourceContainer)
    {
      sourceContainer = this.getTherapySelectionContainer().getSourceTherapyContainer();
      // if the source container was not defined, but we found it, edit was conducted
      if (sourceContainer)
      {
        actionEnum = statusEnums.EDITED_AND_PRESCRIBED;
      }
    }

    if (sourceContainer) {
      sourceData = sourceContainer.getData();
    }

    // if we're reading a basket item, reroute the source container
    if (sourceData instanceof app.views.medications.reconciliation.dto.MedicationOnDischarge)
    {
      // since the aborted therapies panel also contains basket items, make sure to remove it there before we reroute
      this.getAbortedTherapiesPanel().removeByData(sourceData);

      var sourceGroupEnum = sourceData.getSourceGroupEnum();
      var sourceId = sourceData.getSourceId();

      this.getTherapySelectionContainer().getTherapyGroupPanelContentByGroupEnum(sourceGroupEnum, function (content)
      {
        for (var idx = 0; idx < content.length; idx++)
        {
          var itemData = content[idx].getData();
          if (itemData instanceof app.views.medications.reconciliation.dto.DischargeSourceMedication
              && itemData.getId() === sourceId)
          {
            sourceContainer = content[idx];
            self.getTherapySelectionContainer().setSourceTherapyContainer(sourceContainer);
            sourceData = sourceContainer.getData();
          }
        }
      });
    }

    var basketItem = new app.views.medications.reconciliation.dto.MedicationOnDischarge({
      therapy: therapyDto,
      status: actionEnum,
      changeReasonDto: changeReason ? changeReason : null
    });

    if (sourceContainer && sourceData instanceof app.views.medications.reconciliation.dto.DischargeSourceMedication)
    {
      basketItem.setSourceGroupEnum(sourceContainer.getGroupPanel().getGroupId());
      basketItem.setSourceId(sourceData.getId());
      this.markSourceTherapyContainer(sourceContainer);
    }

    this.getBasketContainer().addTherapy(basketItem);
  },

  _attachGroupPanelElementToolbar: function (elementContainer)
  {
    var self = this;
    var data = elementContainer.getData();
    var statusEnums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;

    var toolbar = new app.views.medications.reconciliation.TherapyContainerPanelToolbar({
      therapyContainer: elementContainer,
      withSuspend: false,
      withCancel: data.getTherapy().isLinkedToAdmission() && data.getStatus() !== statusEnums.NOT_PRESCRIBED,
      addToBasketEventCallback: function (therapyContainer)
      {
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        self.onAddTherapyToBasket(therapyContainer);
      },
      addToBasketWithEditEventCallback: function (therapyContainer)
      {
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        self.onEditTherapy(therapyContainer);
      },
      cancelEventCallback: function(therapyContainer){
        // double check
        if (therapyContainer.getData().getTherapy().isLinkedToAdmission()) {
          self.onAbortTherapy(therapyContainer);
        }
      }
    });
    elementContainer.setToolbar(toolbar);

  },

  _markSourceTherapies: function (therapyGroupEnum, ids)
  {
    var self = this;
    var idList = tm.jquery.Utils.isArray(ids) ? ids : [ids];

    this.getTherapySelectionContainer().getTherapyGroupPanelContentByGroupEnum(therapyGroupEnum, function (content)
    {
      content.forEach(function (therapyContainer)
      {
        var data = therapyContainer.getData();
        if (data instanceof app.views.medications.reconciliation.dto.DischargeSourceMedication && data.getId())
        {
          if (idList.contains(data.getId()))
          {
            self.markSourceTherapyContainer(therapyContainer);
          }
        }
      });
    });
  },

  _abortTherapy: function(therapyContainer, changeReasonDto)
  {
    changeReasonDto = tm.jquery.Utils.isEmpty(changeReasonDto) ? null : changeReasonDto;

    var actionEnums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;

    var abortOrder = new app.views.medications.reconciliation.dto.MedicationOnDischarge({
      therapy: therapyContainer.getData().getTherapy(),
      sourceId: therapyContainer.getData().getId(),
      sourceGroupEnum: therapyContainer.getGroupPanel().getGroupId(),
      status: actionEnums.NOT_PRESCRIBED,
      changeReasonDto: changeReasonDto
    });

    this.getAbortedTherapiesPanel().addElement(abortOrder);
    this.markSourceTherapyContainer(therapyContainer);
  },

  /* override */
  unmarkSourceTherapyContainer: function (container)
  {
    container.show();
  },

  /* override */
  markSourceTherapyContainer: function (container)
  {
    container.hide();
  },

  markSourceTherapies: function ()
  {
    var self = this;
    var basketItems = this.getBasketContainer().getContent().concat(this.getAbortedTherapiesPanel().getContentData());

    var basketMap = new tm.jquery.HashMap();

    basketItems.forEach(function (basketItem)
    {
      if (!tm.jquery.Utils.isEmpty(basketItem.getSourceGroupEnum()))
      {
        var groupEnum = basketItem.getSourceGroupEnum();
        if (basketMap.get(groupEnum) == null)
        {
          basketMap.put(groupEnum, []);
        }
        basketMap.get(groupEnum).push(basketItem.getSourceId());
      }
    });

    basketMap.keys().forEach(function (groupEnum)
    {
      self._markSourceTherapies(groupEnum, basketMap.get(groupEnum), false);
    });
  },

  onAddTherapyToBasket: function(therapyContainer)
  {
    var self = this;
    var therapy = therapyContainer.getData().therapy;

    therapyContainer.getToolbar().setEnabled(false, true); // prevent double clicks when the server lags

    this.fixTherapyTiming(therapy);

    if (therapy.isOrderTypeOxygen())
    {
      therapy.setMaxTargetSaturation(null);
      therapy.setMinTargetSaturation(null);
    }

    if (self._isPrescriptionValid(therapy))
    {
      self._getTherapyDto(therapy, function (therapyDto)
      {
        self.addToBasket(therapyDto, null, therapyContainer);
        therapyContainer.getToolbar().setEnabled(true, true);
      });
    }
    else
    {
      therapyContainer.getToolbar().setEnabled(true, true);
    }
  },

  onAbortTherapy: function(therapyContainer){
    var changeTypeEnums = app.views.medications.TherapyEnums.actionReasonTypeEnum;
    var self = this;
    var view = this.getView();

    var changeReasonEntryContainer = new app.views.medications.common.ChangeReasonDataEntryContainer({
      titleIcon: "warningYellow_status_48.png",
      titleText: view.getDictionary("therapy.needs.reason.for.stopping"),
      view: view,
      changeReasonTypeKey: changeTypeEnums.ABORT
    });

    var abortConfirmationDialog = this.getView().getAppFactory().createDataEntryDialog(
        view.getDictionary("warning"),
        null,
        changeReasonEntryContainer, function(resultData)
        {
          if (resultData != null && resultData.isSuccess())
          {
            self._abortTherapy(therapyContainer, resultData.value);
          }
        },
        changeReasonEntryContainer.defaultWidth,
        changeReasonEntryContainer.defaultHeight
    );
    abortConfirmationDialog.show();
  },

  getSourceTherapiesPanelTitle: function ()
  {
    return this.getView().getDictionary("inpatient.prescription");
  },

  /* Override values for column titles */
  getLeftColumnTitle: function ()
  {
    return null;
  },

  /* Override values for column titles */
  getRightColumnTitle: function ()
  {
    return this.getView().getDictionary("discharge.prescription");
  },

  getAbortedTherapiesPanel: function()
  {
    return this.abortedTherapiesPanel;
  }
});