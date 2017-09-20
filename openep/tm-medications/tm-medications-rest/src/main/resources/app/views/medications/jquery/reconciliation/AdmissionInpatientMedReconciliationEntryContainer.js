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
Class.define('app.views.medications.reconciliation.AdmissionInpatientMedReconciliationEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  statics: {
    VIEW_MODE_ADMISSION: 0,
    VIEW_MODE_INPATIENT: 1
  },
  padding: 0,
  cls: 'reconciliation-dialog',
  view: null,

  activeViewMode: null,
  dialog: null,

  _cardContainer: null,

  Constructor: function (config)
  {
    this.callSuper(config);

    if (this.activeViewMode == null)
    {
      this.activeViewMode = app.views.medications.reconciliation.AdmissionInpatientMedReconciliationEntryContainer.VIEW_MODE_ADMISSION;
    }

    this._buildGUI();

    this._loadData();
  },

  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var cardContainer = new tm.jquery.CardContainer({
      activeIndex: this.activeViewMode,
      animation: "slide-horizontal-new",
      animationDuration: 700,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this._buildSlides(cardContainer);

    this.add(cardContainer);

    this._cardContainer = cardContainer;
  },

  _buildSlides: function (slideContainer)
  {
    var container1 = new app.views.medications.reconciliation.AdmissionMedReconciliationContainer({
      view: this.getView()
    });

    var container2 = new app.views.medications.reconciliation.InpatientMedReconciliationContainer({
      view: this.getView()
    });

    slideContainer.add(container1);
    slideContainer.add(container2);
  },

  _loadData: function(){
    var self = this;
    var view = this.getView();
    var appFactory = this.getView().getAppFactory();
    var staticEnums = app.views.medications.reconciliation.AdmissionInpatientMedReconciliationEntryContainer;

    this.loadBasketContents(function(items){

        var admissionContainer = self.getCardContainer().getComponents()[staticEnums.VIEW_MODE_ADMISSION];

        appFactory.createConditionTask(  //wait for the medications to load
            function()
            {
              admissionContainer.getBasketContainer().setContent(items);
              view.getLocalLogger().info("Admission group panels attached. Processing.");
              self._markSourceTherapies(admissionContainer, items);
            },
            function(task)
            {
              return admissionContainer.isGroupPanelsAttached() && admissionContainer.isRendered(true);
            },
            250, 100
        );

    });

    this.loadTherapiesForWarningsSearch(function(items)
    {
      var admissionContainer = self.getCardContainer().getComponents()[staticEnums.VIEW_MODE_ADMISSION];
      var inpatientContainer = self.getCardContainer().getComponents()[staticEnums.VIEW_MODE_INPATIENT];
      if (admissionContainer.isWarningsEnabled())
      {
        admissionContainer.setOldTherapiesForWarningsSearch(tm.jquery.Utils.isEmpty(items) ? [] : items);
      }
      if (inpatientContainer.isWarningsEnabled())
      {
        inpatientContainer.setOldTherapiesForWarningsSearch(tm.jquery.Utils.isEmpty(items) ? [] : items);
      }
    });
  },

  _markSourceTherapies: function(admissionContainer, basketItems)
  {
    var self = this;
    var markMap = new tm.jquery.HashMap();
    basketItems.forEach(function(basketItem){
       if (!tm.jquery.Utils.isEmpty(basketItem.getSourceGroupEnum()))
       {
         var groupEnum = basketItem.getSourceGroupEnum();
         if (markMap.get(groupEnum) == null) {
           markMap.put(groupEnum, []);
         }
         markMap.get(groupEnum).push(basketItem.getSourceId());
       }
    });

    markMap.keys().forEach(function(groupEnum){
      admissionContainer.markSourceTherapies(groupEnum, markMap.get(groupEnum));
    });
  },

  _savingFailed: function()
  {
    var resultData = new app.views.common.AppResultData({success: false});
    this.resultCallback(resultData);
  },

  /**
   * @returns {tm.jquery.Promise}
   * @private
   */
  _placeOrder: function()
  {
    var self = this;
    var view = this.getView();
    var staticEnums = app.views.medications.reconciliation.AdmissionInpatientMedReconciliationEntryContainer;
    var prescriber = view.getCurrentUserAsCareProfessional();
    var inpatientContainer = self.getCardContainer().getComponents()[staticEnums.VIEW_MODE_INPATIENT];
    var medicationOrders = inpatientContainer.getBasketContainer().getContent();
    var suspendedOrders = [];
    var lastLinkName = view.getPatientLastLinkNamePrefix();
    
    inpatientContainer.getSuspendedTherapiesPanel().getContentData().forEach(function(data){
      if (data instanceof app.views.medications.common.dto.SaveMedicationOrder && !data.isReadOnly())
      {
        suspendedOrders.push(data);
      }
    });

    var combinedOrder = jQuery.merge(medicationOrders, suspendedOrders);

    if (inpatientContainer.isWarningsEnabled())
    {
      inpatientContainer.fillTherapyWarningsBeforeSave(combinedOrder);
    }

    return view.getRestApi().saveMedicationsOrder(combinedOrder, prescriber, null, lastLinkName, false);
  },


  loadTherapiesForWarningsSearch: function(callback)
  {
    var view = this.getView();

    var params = {
      patientId: view.getPatientId()
    };

    var therapiesForWarningsUrl =
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPIES_FOR_WARNINGS;

    view.loadViewData(therapiesForWarningsUrl, params, null, function(data)
    {
      callback(data);
    });
  },

  loadBasketContents: function(callback)
  {
    var view = this.getView();

    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_MEDICATIONS_ON_ADMISSION;

    var centralCaseData = view.getCentralCaseData();
    var hospitalizationStart = CurrentTime.get();

    if (centralCaseData && centralCaseData.outpatient === false && centralCaseData.centralCaseEffective
        && centralCaseData.centralCaseEffective.startMillis)
    {
      hospitalizationStart = new Date(centralCaseData.centralCaseEffective.startMillis);
    }

    var params = {
      patientId: view.getPatientId(),
      hospitalizationStart: JSON.stringify(hospitalizationStart),
      language: view.getViewLanguage()
    };


    view.loadViewData(url, params, null,
        function (data)
        {
          var items = [];
          if (!tm.jquery.Utils.isEmpty(data) && data.length > 0)
          {
            items = data.map(function(item){
              var admissionItem = app.views.medications.reconciliation.dto.MedicationOnAdmission.fromJson(item);
              admissionItem.setReadyOnly(!admissionItem.isPending());
              return admissionItem;
            });
          }
          callback(items);
        }
    );
  },

  getView: function ()
  {
    return this.view;
  },

  getCardContainer: function ()
  {
    return this._cardContainer;
  },

  onButtonNextPressed: function (footer)
  {
    var view = this.getView();
    var activeIndex = this.getCardContainer().getActiveIndex();
    var nextIndex = activeIndex + 1;
    var staticEnums = app.views.medications.reconciliation.AdmissionInpatientMedReconciliationEntryContainer;
    var existingPanelItems, removedTherapies, newTherapies, inpatientBasketItems;
    var admissionBasketItems = [];
    var cardContainer = this.getCardContainer();
    var orderActionEnum = app.views.medications.TherapyEnums.medicationOrderActionEnum;

    var keyOmitFunction = function(key,value) {
      return key === "start" ? undefined : value; // omit the start time since adding a therapy to the basket sets the time...
    };

    if (nextIndex <= staticEnums.VIEW_MODE_INPATIENT)
    {
      var activeItem = cardContainer.getActiveItem();

      if (activeItem instanceof app.views.medications.reconciliation.AdmissionMedReconciliationContainer)
      {
        admissionBasketItems = activeItem.getBasketContainer().getContent();
        footer.getNextButton().setEnabled(false);
        view.showLoaderMask();

        activeItem.saveBasketContent(this.view.getCurrentUserAsCareProfessional(), function(){
          footer.getNextButton().setEnabled(true);
          view.hideLoaderMask();
          cardContainer.setActiveIndex(nextIndex);

          if (nextIndex > 0)
          {
            footer.getBackButton().show();
          }

          if (nextIndex === staticEnums.VIEW_MODE_INPATIENT){
            footer.getNextButton().hide();
            footer.getConfirmButton().show();
          }

          activeItem = cardContainer.getActiveItem();
          if (activeItem instanceof app.views.medications.reconciliation.InpatientMedReconciliationContainer)
          {
            inpatientBasketItems = activeItem.getBasketContainer().getContent();
            existingPanelItems = activeItem.getSourceTherapiesPanel().getContentData();
            // first remove any therapies in the admission panel that aren't present in the admission basket
            removedTherapies = jQuery.grep(existingPanelItems, function (itemData)
            {
              for (var idx = 0; idx < admissionBasketItems.length; idx++)
              {
                if (JSON.stringify(itemData.therapy, keyOmitFunction)
                    === JSON.stringify(admissionBasketItems[idx].therapy, keyOmitFunction))
                {
                  return false;
                }
              }
              return true;
            });
            newTherapies = jQuery.grep(admissionBasketItems, function (itemData)
            {
              for (var idx = 0; idx < existingPanelItems.length; idx++)
              {
                if (JSON.stringify(itemData.therapy, keyOmitFunction)
                    === JSON.stringify(existingPanelItems[idx].therapy, keyOmitFunction))
                {
                  return false;
                }
              }
              return true;
            });

            // figure out if if there are any suspended/canceled therapies that were removed from the basket...
            // if so, remove them
            removedTherapies.forEach(function (itemData)
            {
              var suspendedPanelData = activeItem.getSuspendedTherapiesPanel().getContentData();
              for (var idx = suspendedPanelData.length-1; idx > 0; idx-- )
              {
                var suspendedOrder = suspendedPanelData[idx];
                if (suspendedOrder.getTherapy() === itemData.getTherapy())
                {
                  activeItem.getSuspendedTherapiesPanel().removeByData(suspendedOrder);
                }
              }
              var admissionPanelElement = activeItem.getSourceTherapiesPanel().getElementContainerByData(itemData);
              activeItem.getSourceTherapiesPanel().remove(admissionPanelElement);
            });
            newTherapies.reverse().forEach(function (itemData)
            {
              //var newItemData = jQuery.extend({}, itemData, true);

              var newTherapyContainer = activeItem.getSourceTherapiesPanel().addElement(itemData);

              if (itemData.isReadOnly())
              {
                newTherapyContainer.hide();
              }

              if (itemData.isSuspended() || itemData.isAborted())
              {
                var suspendOrder = new app.views.medications.common.dto.SaveMedicationOrder({
                  therapy: newTherapyContainer.getData().getTherapy(),
                  sourceId: newTherapyContainer.getData().getTherapy().getCompositionUid(),
                  actionEnum: itemData.isSuspended() ? orderActionEnum.SUSPEND : orderActionEnum.ABORT,
                  changeReasonDto: newTherapyContainer.getData().getChangeReasonDto(),
                  readOnly: true
                });
                activeItem.getSuspendedTherapiesPanel().addElement(suspendOrder);
              }
            });
          }
        });
      }
    }

    this.getDialog().setTitle(this.getView().getDictionary(
      nextIndex === staticEnums.VIEW_MODE_ADMISSION ?
          "medication.on.admission" : "medications.ordering"));
  },

  onButtonBackPressed: function (footer)
  {
    var staticEnums = app.views.medications.reconciliation.AdmissionInpatientMedReconciliationEntryContainer;
    var prevIndex = this.getCardContainer().getActiveIndex() - 1;

    if (prevIndex >= 0)
    {
      this.getCardContainer().setActiveIndex(prevIndex);

      if (prevIndex == 0)
      {
        footer.getBackButton().hide();
        footer.getConfirmButton().hide();
        footer.getNextButton().show();
      }

      this.getDialog().setTitle(this.getView().getDictionary(
          prevIndex === staticEnums.VIEW_MODE_ADMISSION ?
              "medication.on.admission" : "medications.ordering"));
    }
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var staticEnums = app.views.medications.reconciliation.AdmissionInpatientMedReconciliationEntryContainer;
    var inpatientContainer = this.getCardContainer().getComponents()[staticEnums.VIEW_MODE_INPATIENT];
    var medicationOrders = inpatientContainer.getBasketContainer().getContent();

    var resultData = new app.views.common.AppResultData({success: false, value: null});

    for (var idx = 0; idx < medicationOrders.length; idx++) {
      if (!medicationOrders[idx].getTherapy().isCompleted())
      {
        appFactory.createWarningSystemDialog(view.getDictionary("unfinished.therapies.in.basket.warning"), 360, 122).show();
        resultDataCallback(resultData);
        return;
      }
    }

    var areCriticalWarningsOverriden = inpatientContainer.isWarningsEnabled() &&
        inpatientContainer.getWarningsContainer().assertAllCriticalWarningsOverridden();

    if (!areCriticalWarningsOverriden)
    {
      var message = this.view.getDictionary('you.have.unchecked.warnings');
      view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
      resultDataCallback(resultData);
      return;
    }

    inpatientContainer.assertAndWarnWarningsDataLoading().then(
        function()
        {
          self._placeOrder().then(function onSuccess()
              {
                resultDataCallback(new app.views.common.AppResultData({success: true, value: null}));
              },
              failureResultCallbackHandler);
        },
        failureResultCallbackHandler);
    
    function failureResultCallbackHandler()
    {
      resultDataCallback(resultData);
    }
  },

  setDialog: function(value) {
    this.dialog = value;
  },

  getDialog: function() {
    return this.dialog;
  }
});