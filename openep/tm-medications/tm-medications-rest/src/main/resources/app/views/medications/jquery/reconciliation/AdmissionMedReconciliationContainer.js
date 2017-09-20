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
Class.define('app.views.medications.reconciliation.AdmissionMedReconciliationContainer', 'app.views.medications.reconciliation.MedicineReconciliationContainer', {
  selectedCls: "item-selected",
  startEndTimeSupport: false,
  warningsEnabled: false,
  groupPanelsAttached: false,
  showSource: true,

  Constructor: function (config)
  {
    this.callSuper(config);
    this.getBasketTherapyDisplayProvider().setShowChangeReason(false);
    this.getBasketTherapyDisplayProvider().setShowChangeHistory(false);
    this.baselineInfusionIntervals = null; /* dont need to check if infusion intervals overlap */
  },

  /* Override values for column titles */
  getLeftColumnTitle: function ()
  {
    return this.getView().getDictionary("previous.prescription");
  },

  /* Override values for column titles */
  getRightColumnTitle: function ()
  {
    return this.getView().getDictionary("medication.on.admission");
  },

  attachTherapyGroupPanels: function (container)
  {
    var self = this;
    var view = this.getView();
    var getAdmissionGroupsUrl =
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPY_GROUPS_ON_ADMISSION;

    var centralCaseData = view.getCentralCaseData();
    var hospitalizationStart =
        (centralCaseData.outpatient === false && centralCaseData.centralCaseEffective && centralCaseData.centralCaseEffective.startMillis) ?
            new Date(centralCaseData.centralCaseEffective.startMillis) :
            CurrentTime.get();

    var params = {
      patientId: view.getPatientId(),
      patientHeight: view.getReferenceWeight(),
      hospitalizationStart: JSON.stringify(hospitalizationStart),
      language: view.getViewLanguage()
    };

    view.loadViewData(getAdmissionGroupsUrl, params, null, function (data)
    {
      if (!tm.jquery.Utils.isEmpty(data) && tm.jquery.Utils.isArray(data))
      {
        data.forEach(function (therapyGroupData)
        {
          var containerData = app.views.medications.reconciliation.dto.MedicationOnAdmissionGroup.fromJson(therapyGroupData);

          container.add(new app.views.medications.common.TherapyGroupPanel({
            groupTitle: containerData.getName(),
            groupId: containerData.getGroupEnum(),
            view: this.getView(),
            contentData: containerData.getGroupElements(),
            attachElementToolbar: function (container)
            {
              self._attachGroupPanelElementToolbar(container);
            }
          }));
        }, self);
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

  onTherapyRemovedFromBasket: function (elementData)
  {
    this.callSuper();
    var self = this;
    if (!tm.jquery.Utils.isEmpty(elementData))
    {
      var sourceGroupEnum = elementData.getSourceGroupEnum();
      var sourceId = elementData.getSourceId();

      if (sourceGroupEnum && sourceId)
      {
        this.getTherapySelectionContainer().getTherapyGroupPanelContentByGroupEnum(sourceGroupEnum,
            function (content)
            {
              for (var idx = 0; idx < content.length; idx++)
              {
                var therapyContainer = content[idx];
                if (therapyContainer.getData().getId() === sourceId)
                {
                  self.unmarkSourceTherapyContainer(therapyContainer);
                  break;
                }
              }
            });
      }
    }
  },

  /* Override, remove the start and end date complelty. */
  fixTherapyTiming: function (therapy)
  {
    therapy.start = null;
    therapy.end = null;
  },

  /* Override to wrap the therapy into a medicationonadmission object */
  addToBasket: function (therapyDto, changeReason, sourceContainer)
  {
    var self = this;
    var sourceData = null;
    // in case of add with edit, no source container is passed over, check if there is one
    if (!sourceContainer) {
      sourceContainer = this.getTherapySelectionContainer().getSourceTherapyContainer();
    }

    if (sourceContainer)
    {
      sourceData = sourceContainer.getData();
    }

    // if we're reading a basket item, reroute the source container
    if (sourceData instanceof app.views.medications.reconciliation.dto.MedicationOnAdmission)
    {
      var sourceGroupEnum = sourceData.getSourceGroupEnum();
      var sourceId = sourceData.getSourceId();
      if (!tm.jquery.Utils.isEmpty(sourceGroupEnum) && !tm.jquery.Utils.isEmpty(sourceId))
      {
        this.getTherapySelectionContainer().getTherapyGroupPanelContentByGroupEnum(sourceGroupEnum, function (content)
        {
          for (var idx = 0; idx < content.length; idx++)
          {
            var itemData = content[idx].getData();
            if (itemData instanceof app.views.medications.reconciliation.dto.SourceMedication
                && itemData.getId() === sourceId)
            {
              sourceContainer = content[idx];
              self.getTherapySelectionContainer().setSourceTherapyContainer(sourceContainer);
              sourceData = sourceContainer.getData();
            }
          }
        });
      }
    }

    var basketItem = new app.views.medications.reconciliation.dto.MedicationOnAdmission({
      therapy: therapyDto,
      status: app.views.medications.TherapyEnums.medicationOnAdmissionStatus.PENDING,
      changeReasonDto: changeReason ? changeReason : null
    });

    if (sourceContainer && sourceData instanceof app.views.medications.reconciliation.dto.SourceMedication)
    {
      basketItem.setSourceGroupEnum(sourceContainer.getGroupPanel().getGroupId());
      basketItem.setSourceId(sourceData.getId());
      this.markSourceTherapyContainer(sourceContainer);
    }

    this.getBasketContainer().addTherapy(basketItem);
  },

  unmarkSourceTherapyContainer: function (container)
  {
    var selectedCls = this.getSelectedCls();
    var oldCls;

    if (!tm.jquery.Utils.isEmpty(container))
    {
      oldCls = tm.jquery.Utils.isEmpty(container.getCls()) ? "" : container.getCls();
      if (oldCls.contains(selectedCls))
      {
        container.setCls(container.getCls().replace(" " + selectedCls, ""));
        container.getToolbar().setEnabled(true, true);
      }
    }
  },

  /* for override - add effects or hide when the therapy is added to the basket */
  markSourceTherapyContainer: function (container)
  {
    var selectedCls = this.getSelectedCls();
    var oldCls;

    if (!tm.jquery.Utils.isEmpty(container) && !container.getCls().contains(selectedCls))
    {
      oldCls = container.getCls();
      container.setCls(tm.jquery.Utils.isEmpty(oldCls) ? selectedCls : (oldCls + " " + selectedCls));
      container.applyCls(container.getCls());
      container.getToolbar().setEnabled(false, true);
    }
  },

  getSelectedCls: function ()
  {
    return this.selectedCls;
  },

  saveBasketContent: function (prescriber, callback)
  {
    var view = this.getView();
    var basketItems = this.getBasketContainer().getContent();

    if (basketItems.length === 0) callback();

    var saveMedicationOnAdmissionUrl =
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_MEDICATIONS_ON_ADMISSION;
    var centralCaseData = view.getCentralCaseData();
    var hospitalizationStart = CurrentTime.get();

    if (centralCaseData.outpatient === false && centralCaseData.centralCaseEffective
        && centralCaseData.centralCaseEffective.startMillis)
    {
      hospitalizationStart = new Date(centralCaseData.centralCaseEffective.startMillis);
    }

    var params = {
      patientId: view.getPatientId(),
      therapies: JSON.stringify(basketItems),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: view.getCareProviderId(),
      prescriber: JSON.stringify(prescriber),
      saveDateTime: null,
      hospitalizationStart: JSON.stringify(hospitalizationStart),
      language: view.getViewLanguage()
    };

    view.loadPostViewData(saveMedicationOnAdmissionUrl, params, null,
        function (data)
        {
          // update the therapy composition uids with real values
          if (!tm.jquery.Utils.isEmpty(data) && data.length > 0)
          {
            for (var idx = 0; idx < basketItems.length; idx++)
            {
              basketItems[idx].getTherapy().setCompositionUid(data[idx]);
            }
            callback();
          }
        });
  },

  markSourceTherapies: function (therapyGroupEnum, ids)
  {
    var self = this;
    var idList = tm.jquery.Utils.isArray(ids) ? ids : [ids];

    this.getTherapySelectionContainer().getTherapyGroupPanelContentByGroupEnum(therapyGroupEnum, function (content)
    {
      content.forEach(function (therapyContainer)
      {
        var data = therapyContainer.getData();
        if (data instanceof app.views.medications.reconciliation.dto.SourceMedication && data.getId())
        {
          if (idList.contains(data.getId()))
          {
            self.markSourceTherapyContainer(therapyContainer);
          }
        }
      });
    });
  },

  isGroupPanelsAttached: function ()
  {
    return this.groupPanelsAttached;
  }
});