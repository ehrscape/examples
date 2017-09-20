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
Class.define('app.views.medications.reconciliation.DischargeMedReconciliationEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'reconciliation-dialog',

  view: null,
  dialog: null,

  _contentContainer: null,

  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGUI();
    this._loadData();
  },

  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var contentContainer = new app.views.medications.reconciliation.DischargeMedsReconciliationContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.getView()
    });

    this.add(contentContainer);

    this._contentContainer = contentContainer;
  },

  _loadData: function ()
  {
    var self = this;
    var view = this.getView();
    var appFactory = this.getView().getAppFactory();

    this._loadBasketContents(function (items)
    {
      var abortedItems = [];
      for (var idx = items.length-1; idx >= 0; idx--)
      {
        if (items[idx].isAborted()) {
          abortedItems.push(items[idx]);
          items.splice(idx, 1);
        }
      }
      abortedItems.reverse();

      var dischargeContainer = self.getContentContainer();

      appFactory.createConditionTask(  //wait for the medications to load
          function ()
          {
            dischargeContainer.getBasketContainer().setContent(items);

            dischargeContainer.getAbortedTherapiesPanel().setContentData(abortedItems);
            dischargeContainer.getAbortedTherapiesPanel().refresh();
            dischargeContainer.markSourceTherapies();
          },
          function ()
          {
            return dischargeContainer.isGroupPanelsAttached() && dischargeContainer.isRendered(true);
          },
          250, 100
      );
    });

    if (self.getContentContainer().isWarningsEnabled())
    {
      this._loadTherapiesForWarningsSearch(function (items)
      {
        self.getContentContainer().setOldTherapiesForWarningsSearch(tm.jquery.Utils.isEmpty(items) ? [] : items);
      });
    }
  },

  _loadTherapiesForWarningsSearch: function(callback)
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

  _loadBasketContents: function (callback)
  {
    var view = this.getView();

    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_MEDICATIONS_ON_DISCHARGE;

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
            items = data.map(function (item)
            {
              return app.views.medications.reconciliation.dto.MedicationOnDischarge.fromJson(item);
            });
          }
          callback(items);
        }
    );
  },

  /* Override, don't need this test. */
  _baselineInfusionsIntersects: function (start, end)
  {
    return false;
  },

  _saveBasketContent: function (successCallback, failCallback)
  {
    var view = this.getView();
    var contentContainer = this.getContentContainer();
    var prescriber = this.view.getCurrentUserAsCareProfessional();
    var basketItems = contentContainer.getBasketContainer().getContent();
    // in case no custom groups load there's also no aborted panel
    var abortedItems = tm.jquery.Utils.isEmpty(contentContainer.getAbortedTherapiesPanel()) ?
        [] : contentContainer.getAbortedTherapiesPanel().getContentData();

    var saveMedicationOnDischargeUrl =
        view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_MEDICATIONS_ON_DISCHARGE;
    var centralCaseData = view.getCentralCaseData();
    var hospitalizationStart = CurrentTime.get();

    if (centralCaseData.outpatient === false && centralCaseData.centralCaseEffective
        && centralCaseData.centralCaseEffective.startMillis)
    {
      hospitalizationStart = new Date(centralCaseData.centralCaseEffective.startMillis);
    }

    var combinedOrder = basketItems.concat(abortedItems);

    if (contentContainer.isWarningsEnabled())
    {
      contentContainer.fillTherapyWarningsBeforeSave(combinedOrder);
    }

    var params = {
      patientId: view.getPatientId(),
      therapies: JSON.stringify(combinedOrder),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: view.getCareProviderId(),
      prescriber: JSON.stringify(prescriber),
      saveDateTime: null,
      hospitalizationStart: JSON.stringify(hospitalizationStart),
      language: view.getViewLanguage()
    };

    view.loadPostViewData(saveMedicationOnDischargeUrl, params, null,
        function (data)
        {
          successCallback();
        },
        function ()
        {
          failCallback();
        });
  },

  getView: function ()
  {
    return this.view;
  },

  getContentContainer: function ()
  {
    return this._contentContainer;
  },

  getDialog: function ()
  {
    return this.dialog;
  },

  setDialog: function (value)
  {
    this.dialog = value;
  },

  processResultData: function (resultDataCallback)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var self = this;
    var medicationOrders = this.getContentContainer().getBasketContainer().getContent();

    var resultData = new app.views.common.AppResultData({success: false, value: null});

    for (var idx = 0; idx < medicationOrders.length; idx++)
    {
      if (!medicationOrders[idx].getTherapy().isCompleted())
      {
        appFactory.createWarningSystemDialog(view.getDictionary("unfinished.therapies.in.basket.warning"), 360, 122).show();
        resultDataCallback(resultData);
        return;
      }
    }

    var areCriticalWarningsOverriden = this.getContentContainer().isWarningsEnabled() &&
        this.getContentContainer().getWarningsContainer().assertAllCriticalWarningsOverridden();

    if (!areCriticalWarningsOverriden)
    {
      var message = this.view.getDictionary('you.have.unchecked.warnings');
      view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
      resultDataCallback(resultData);
      return;
    }

    this.getContentContainer().assertAndWarnWarningsDataLoading().then(function()
        {
          self._saveBasketContent(function()
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
  }
});