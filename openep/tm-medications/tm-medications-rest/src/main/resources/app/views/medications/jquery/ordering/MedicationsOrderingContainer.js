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

Class.define('app.views.medications.ordering.MedicationsOrderingContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "medications-ordering-container",
  padding: 0,

  /** configs */
  view: null,
  patientId: null,
  presetMedicationId: null, //optional
  presetTherapies: null,    //optional
  warningsEnabled: true,
  isPastMode: false,
  assertBaselineInfusion: true,

  /** privates */
  baselineInfusionIntervals: null,
  oldTherapiesForWarningsSearch: null,
  resultCallback: null,
  linkIndex: null,
  bnfPercentageSum: null,
  currentBnfMedications: null,
  medicationRuleUtils: null,

  /** privates: components */
  orderingContainer: null,
  basketContainer: null,
  warningsContainer: null,
  performerContainer: null,
  saveDateTimePane: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this.baselineInfusionIntervals = [];
    this.oldTherapiesForWarningsSearch = [];
    this.linkIndex = 0;
    this.bnfPercentageSum = self.view.getCurrentBnfMaximumSum();
    this.currentBnfMedications = [];
    this._loadPatientBaselineInfusionIntervals();
    if (this.warningsEnabled)
    {
      this._loadTherapiesForWarningsSearch();
    }

    this.medicationRuleUtils = this.getConfigValue("medicationRuleUtils",
        new tm.views.medications.MedicationRuleUtils({view: this.view}));

    this._buildComponents();
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      if (self.presetMedicationId)
      {
        setTimeout(function()
        {
          self.orderingContainer.presetMedication(self.presetMedicationId);
        }, 500);
      }
      if (self.presetTherapies)
      {
        self._fixTherapiesTimingAndAddToBasket(self.presetTherapies, true);
      }
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var view = this.view;
    var appFactory = view.getAppFactory();

    this.orderingContainer = this.buildOrderingContainer();
    this.basketContainer = this.buildBasketContainer();

    this.warningsContainer = new app.views.medications.ordering.WarningsContainer({
      view: view,
      medicationRuleUtils: this.medicationRuleUtils,
      height: this.warningsEnabled ? 300 : 30,
      getPatientMedsForWarningsFunction: function()
      {
        return self._getTherapiesForWarningsSearch(true);
      }
    });

    if (this.isPastMode === true)
    {
      var careProfessionals = view.getCareProfessionals();
      var currentUserAsCareProfessionalName = view.getCurrentUserAsCareProfessional() ? view.getCurrentUserAsCareProfessional().name : null;
      this._performerContainer =
          tm.views.medications.MedicationUtils.createPerformerContainer(view, careProfessionals, currentUserAsCareProfessionalName);
    }

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane();
    this.saveDateTimePane.hide();
  },

  _buildGui: function()
  {
    var mainContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    mainContainer.add(this.orderingContainer);
    var eastContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    eastContainer.add(this.basketContainer);
    eastContainer.add(this.warningsContainer);
    mainContainer.add(eastContainer);
    this.add(mainContainer);
    if (this._performerContainer != null)
    {
      this.add(this._performerContainer);
    }
    this.add(this.saveDateTimePane);
  },

  _editBasketTherapy: function(therapyContainer)
  {
    var self = this;
    var therapy = therapyContainer.getData().therapy;
    this._reduceCalculatedBnfPercentage([therapy]);

    this.basketContainer.removeTherapy(therapy); // has to be removed before warnings are calculated (otherwise it's included)
    this.orderingContainer.editTherapy(therapyContainer, false, function(){
      if (self.warningsEnabled)
      {
        self.warningsContainer.refreshWarnings();
      }
      if (therapy && therapy.baselineInfusion)
      {
        self._removeBaselineInfusion(therapy);
      }

      self.warningsContainer.refreshParacetamolLimitWarning([], self.basketContainer.getTherapies(), true);
    });
  },

  _addToBasket: function(therapy, addedTherapiesCount, intialBasketItemsCount, changeReason, linkedTherapy)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var params = {therapy: JSON.stringify(therapy)};
    var fillDisplayValuesUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FILL_DISPLAY_VALUES;

    var baselineInfusionAlreadyExists =
        therapy.baselineInfusion && this._baselineInfusionsIntersects(therapy.start, therapy.end);

    if (baselineInfusionAlreadyExists && this.assertBaselineInfusion)
    {
      appFactory.createWarningSystemDialog(self.view.getDictionary("patient.already.has.baseline.infusion"), 320, 122).show();
      return false;
    }
    else
    {
      this.view.loadPostViewData(fillDisplayValuesUrl, params, null, function(therapyWithDisplayValues)
      {
        //fix date after serialization
        therapyWithDisplayValues.start = therapyWithDisplayValues.start ? new Date(therapyWithDisplayValues.start) : null;
        therapyWithDisplayValues.end = therapyWithDisplayValues.end ? new Date(therapyWithDisplayValues.end) : null;
        therapyWithDisplayValues.completed = therapy.completed;

        var basketItemsCount = self.basketContainer.getTherapies().length;
        var addingLastTherapyFromList = !addedTherapiesCount || basketItemsCount - intialBasketItemsCount == addedTherapiesCount - 1;
        var therapyWithDisplayValuesDto = app.views.medications.common.TherapyJsonConverter.convert(therapyWithDisplayValues);        

        self._addToCalculatedBnfPercentage(therapy);
        if (addingLastTherapyFromList)
        {
          self.warningsContainer.refreshParacetamolLimitWarning([therapy], self.basketContainer.getTherapies(), true);
        }

        self.basketContainer.addTherapy({
              therapy: therapyWithDisplayValuesDto,
              linkedTherapy: linkedTherapy
            },
            {
              forceNoRefreshWarnings: !addingLastTherapyFromList
            });
      });
      if (therapy.baselineInfusion)
      {
        this.baselineInfusionIntervals.push({
          startMillis: therapy.start.getTime(),
          endMillis: therapy.end ? therapy.end.getTime() : null
        })
      }
      return true;
    }
  },

  _openSaveTemplateDialog: function(therapies, addSingleTherapy, invalidTherapy)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var content = jQuery.map(therapies, function(item){
      var clonedItem = jQuery.extend(true, {}, item);
      clonedItem.linkName = null;
      return clonedItem;
    });

    var dialog = appFactory.createDataEntryDialog(
        this.view.getDictionary('therapy.template'),
        null,
        new app.views.medications.ordering.SaveTemplatePane({
          view: self.view,
          templateMode: self.getTherapyTemplateMode(),
          startProcessOnEnter: true,
          padding: 8,
          addSingleTherapy: addSingleTherapy,
          invalidTherapy: invalidTherapy,
          therapies: content,
          templates: self.orderingContainer.getTemplates()}),
        function(resultData)
        {
          if (resultData)
          {
            self.orderingContainer.reloadTemplates();
          }
        },
        300,
        300
    );

    dialog.show();
  },

  /**
   *
   * @param {Array<app.views.medications.common.dto.Therapy>}therapies
   * @param {Boolean} clearEnd
   * @private
   */
  _fixTherapiesTimingAndAddToBasket: function(therapies, clearEnd)
  {
    var basketContainerElementsCount = this.basketContainer.getTherapies().length;
    for (var i = 0; i < therapies.length; i++)
    {
      var therapy = therapies[i];
      therapy.rescheduleTherapyTimings(clearEnd);
      this._addToBasket(therapy, therapies.length, basketContainerElementsCount);
    }
  },

  _baselineInfusionsIntersects: function(start, end)
  {
    for (var i = 0; i < this.baselineInfusionIntervals.length; i++)
    {
      if (start.getTime() < this.baselineInfusionIntervals[i].startMillis)
      {
        if (!end || end.getTime() > this.baselineInfusionIntervals[i].startMillis)
        {
          return true
        }
      }
      else
      {
        if (!this.baselineInfusionIntervals[i].endMillis || this.baselineInfusionIntervals[i].endMillis > start.getTime())
        {
          return true;
        }
      }
    }
    return false;
  },

  /**
   * Validates the user input and returns a promise.
   * @returns {tm.jquery.Promise}
   * @private
   */
  _validateUserInput: function()
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var appFactory = this.view.getAppFactory();
    var therapies = this.basketContainer.getTherapies();
    var basketItems = this.basketContainer.getBasketItems();
    var prescriber = this._getPrescriber();

    if (therapies.length == 0)
    {
      appFactory.createWarningSystemDialog(self.view.getDictionary("you.have.no.therapies.in.basket"), 360, 122).show();
      deferred.reject();
    }
    else if (!self.isPastMode && !prescriber)
    {
      appFactory.createWarningSystemDialog(self.view.getDictionary("prescriber.not.defined.warning"), 320, 122).show();
      deferred.reject();
    }
    else
    {
      var incompleteTherapiesExist = this._doesOrderContainIncompleteTherapies(therapies);
      if (incompleteTherapiesExist)
      {
        appFactory.createWarningSystemDialog(self.view.getDictionary("unfinished.therapies.in.basket.warning"), 360, 122).show();
        deferred.reject();
      }
      else
      {
        var unfinishedOrderExists = this.orderingContainer.unfinishedOrderExists();
        var warningsLoadingInProgress = this.warningsContainer.isDataLoading();

        if (unfinishedOrderExists || warningsLoadingInProgress)
        {
          var warningString = "";
          var height = 110;
          if (unfinishedOrderExists)
          {
            warningString += this.view.getDictionary('unfinished.therapy.that.will.not.be.saved.warning') + "\n";
            height += 32;
          }
          if (warningsLoadingInProgress)
          {
            warningString += this.view.getDictionary('interactions.not.loaded.warning');
            height += 48;
          }
          var confirmDialog = appFactory.createConfirmSystemDialog(warningString,
              function(confirmed)
              {
                if (confirmed == true && self._fillAndValidateCriticalWarnings(therapies))
                {
                  deferred.resolve(basketItems);
                }
                else
                {
                  deferred.reject();
                }
              }
          );
          confirmDialog.setWidth(380);
          confirmDialog.setHeight(height);
          confirmDialog.show();
        }
        else if (self._fillAndValidateCriticalWarnings(therapies))
        {
          deferred.resolve(basketItems);
        }
        else
        {
          deferred.reject();
        }
      }
    }

    return deferred.promise();
  },

  _fillAndValidateCriticalWarnings: function(therapies)
  {
    var areCriticalWarningsOverriden = this.warningsContainer.assertAllCriticalWarningsOverridden();
    if (!areCriticalWarningsOverriden)
    {
      var message = this.view.getDictionary('you.have.unchecked.warnings');
      this.view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
      return false;
    }
    this._fillTherapyWarningsBeforeSave(therapies);
    return true;
  },

  _doesOrderContainIncompleteTherapies: function(therapies)
  {
    for (var i = 0; i < therapies.length; i++)
    {
      if (therapies[i].completed == false)
      {
        return true;
      }
    }
    return false;
  },

  _getPrescriber: function()
  {
    return this._performerContainer != null ?
        this._performerContainer.getPerformer() : this.view.getCurrentUserAsCareProfessional();
  },

  /**
   * Places the order of the specified therapies.
   * @param {Array} basketItems with therapies to order.
   * @returns {tm.jquery.Promise}
   */
  placeOrder: function(basketItems)
  {
    var self = this;

    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SAVE_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var medicationOrders = basketItems.map(function(basketItem){
      var saveMedicationOrder = new app.views.medications.common.dto.SaveMedicationOrder({
        therapy: basketItem.therapy,
        actionEnum: app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE
      });
      if (basketItem.linkedTherapy)
      {
        saveMedicationOrder.linkCompositionUid = basketItem.linkedTherapy.getCompositionUid();
      }
      return saveMedicationOrder;
    });

    var saveDateTime = this.saveDateTimePane.isHidden() ? null : this.saveDateTimePane.getSaveDateTime();

    return this.view.getRestApi().saveMedicationsOrder(medicationOrders, this._getPrescriber(), saveDateTime,
        this.view.getPatientLastLinkNamePrefix(), true);
  },

  _fillTherapyWarningsBeforeSave: function(therapies)
  {
    var enums = app.views.medications.TherapyEnums;
    var warnings = this.warningsContainer.getOverriddenWarnings();

    for (var i = 0; i < warnings.length; i++)
    {
      var overrideReason = warnings[i].overrideReason;
      var warningDto = warnings[i].warning; //MedicationsWarningDto

      if (warningDto.medications.length != 0)
      {
        for (var j = 0; j < therapies.length; j++)
        {
          var therapy = therapies[j];
          var therapyContainsMedicationWithWarning = false;
          if (therapy.isOrderTypeSimple())
          {
            for (var l = 0; l < warningDto.medications.length; l++)
            {
              var simpleMedicationForWarningDto = warningDto.medications[l];
              if (therapy.medication.id == simpleMedicationForWarningDto.id)
              {
                therapyContainsMedicationWithWarning = true;
              }
            }
          }
          else if (therapy.isOrderTypeComplex())
          {
            for (var k = 0; k < therapy.ingredientsList.length; k++)
            {
              var infusionIngredient = therapy.ingredientsList[k];
              for (var n = 0; n < warningDto.medications.length; n++)
              {
                var complexMedicationForWarningDto = warningDto.medications[n];
                if (infusionIngredient.medication.id == complexMedicationForWarningDto.id)
                {
                  therapyContainsMedicationWithWarning = true;
                }
              }
            }
          }
          if (therapyContainsMedicationWithWarning)
          {
            therapy.criticalWarnings.push("Warning overridden. Reason: " + overrideReason + " Warning: " + warningDto.description);
          }
        }
      }
    }
  },

  _loadPatientBaselineInfusionIntervals: function()
  {
    var self = this;
    var params = {patientId: this.patientId};
    var baselineTherapiesUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_BASELINE_THERAPIES;
    this.view.loadViewData(baselineTherapiesUrl, params, null, function(data)
    {
      self.baselineInfusionIntervals = data;
    });
  },

  _loadTherapiesForWarningsSearch: function()
  {
    var self = this;
    var params = {patientId: this.patientId};
    var therapiesForWarningsUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPIES_FOR_WARNINGS;
    this.view.loadViewData(therapiesForWarningsUrl, params, null, function(data)
    {
      self.oldTherapiesForWarningsSearch = data;
    });
  },

  _getTherapiesForWarningsSearch: function(prospective)
  {
    var therapiesForWarnings = [];
    var therapies = this.basketContainer.getTherapies();
    for (var i = 0; i < therapies.length; i++)
    {
      therapiesForWarnings.push.apply(
          therapiesForWarnings,
          tm.views.medications.warning.WarningsHelpers.getMedicationsForWarningSearchForTherapy(therapies[i], prospective)
      );
    }
    $.merge(therapiesForWarnings, this.oldTherapiesForWarningsSearch);
    return therapiesForWarnings;
  },

  _removeBaselineInfusion: function(therapy)
  {
    var baselineInfusionIntervalsLength = this.baselineInfusionIntervals.length;
    for (var i = 0; i < baselineInfusionIntervalsLength; i++)
    {
      var baselineInterval = this.baselineInfusionIntervals[i];
      if (baselineInterval.startMillis == therapy.start.getTime() &&
          (therapy.end == null && baselineInterval.endMillis == null) || (baselineInterval.endMillis == therapy.end.getTime()))
      {
        this.baselineInfusionIntervals.splice(i, 1);
        break;
      }
    }
  },

  /**
   * Validates the input and places the order if successful.
   * @param {function} resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    var self = this;

    this._validateUserInput().then(
        function validationSuccessHandler(basketItems)
        {
          self.placeOrder(basketItems).then(successResultCallbackHandler, failureResultCallbackHandler);
        },
        failureResultCallbackHandler
    );

    function successResultCallbackHandler(){
      resultDataCallback(new app.views.common.AppResultData({success: true}));
    }

    function failureResultCallbackHandler()
    {
      resultDataCallback(new app.views.common.AppResultData({success: false}));
    }
  },

  _addToCalculatedBnfPercentage: function(therapy)
  {
    if (!tm.jquery.Utils.isEmpty(therapy.bnfMaximumPercentage))
    {
      this.currentBnfMedications.push(therapy.getMainMedication().getId());
      this.bnfPercentageSum += therapy.bnfMaximumPercentage;

      if (this.bnfPercentageSum > 100)
      {
        this.warningsContainer.addBnfWarning(this.bnfPercentageSum, this.currentBnfMedications);
      }
    }
  },

  _removeParacetamolLimitWarning: function(therapies)
  {
    var basketTherapies = this.basketContainer.getTherapies();
    if (therapies)
    {
      basketTherapies = basketTherapies.filter(function(item)
      {
        return therapies.indexOf(item) === -1;
      });
    }

    this.warningsContainer.refreshParacetamolLimitWarning(basketTherapies, null, false);
  },

  _reduceCalculatedBnfPercentage: function(therapies)
  {
    var medicationIds = [];
    var percentageSum = 0;
    var oldPercentage = this.bnfPercentageSum;

    therapies.forEach(function(item)
    {
      if (!tm.jquery.Utils.isEmpty(item.bnfMaximumPercentage))
      {
        medicationIds.push(item.getMainMedication().getId());
        percentageSum += item.bnfMaximumPercentage;
      }
    });

    this.bnfPercentageSum -= percentageSum;
    this.currentBnfMedications = this.currentBnfMedications.filter(
        function (item)
        {
          return medicationIds.indexOf(item) === -1;
        });

    if (oldPercentage > 100)
    {
      if (this.bnfPercentageSum < 100 || this.currentBnfMedications.isEmpty())
      {
        this.warningsContainer.removeAdditionalWarning(app.views.medications.TherapyEnums.additionalWarningType.BNF);
      }
      else if (!this.currentBnfMedications.isEmpty())
      {
        this.warningsContainer.addBnfWarning(this.bnfPercentageSum, this.currentBnfMedications);
      }
    }
  },

  /* overridable! check usage! */
  buildOrderingContainer: function()
  {
    var view = this.view;
    var isPastMode = this.isPastMode;
    var self = this;

    return new app.views.medications.ordering.OrderingContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "720px"),
      mainContainer: this,
      isPastMode: isPastMode,
      templateMode: this.getTherapyTemplateMode(),
      additionalMedicationSearchFilter: app.views.medications.TherapyEnums.medicationFinderFilterEnum.INPATIENT_PRESCRIPTION,
      medicationRuleUtils: self.medicationRuleUtils,
      addTherapiesToBasketFunction: function(therapies)
      {
        self._fixTherapiesTimingAndAddToBasket(therapies, false);
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePane.setHeight(34);
        self.saveDateTimePane.setPadding('4 0 0 0');
        self.saveDateTimePane.show();
        self.saveDateTimePane.repaint();
      },
      confirmTherapyEvent: function(data, changeReason, linkedTherapy)
      {
        if (data != 'VALIDATION_FAILED')
        {
          return self._addToBasket(data, null, null, changeReason, linkedTherapy);
        }
        return false;
      },
      saveTherapyToTemplateFunction: function(therapy, invalidTherapy)
      {
        self._openSaveTemplateDialog([therapy], true, invalidTherapy);
      },
      getBasketTherapiesFunction: function()
      {
        return self.getBasketContainer().getTherapies();
      },
      refreshBasketFunction: function()
      {
        self.getBasketContainer().refreshWithExistingData();
      },
      getPatientMedsForWarningsFunction: function()
      {
        return self._getTherapiesForWarningsSearch(false);
      }
    });
  },

  /* overridable! check usage! */
  buildBasketContainer: function()
  {
    var self = this;
    var view = this.view;
    var appFactory = view.getAppFactory();

    return new app.views.medications.ordering.BasketContainer({
      view: view,
      headerTitle: view.getDictionary("therapy.list"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      therapyAddedEvent: function(options)
      {
        if (self.isWarningsEnabled())
        {
          if (options && options.forceNoRefreshWarnings)
          {
            // do nothing
          }
          else
          {
            self.getWarningsContainer().refreshWarnings();
          }
        }
        self.getOrderingContainer().clear();
      },
      therapiesRemovedEvent: function(therapyContainers, options)
      {
        var therapies = [];
        therapyContainers.forEach(function(item)
        {
          therapies.push(item.therapy);
        });

        self._reduceCalculatedBnfPercentage(therapies);
        self._removeParacetamolLimitWarning(therapies);

        if (self.isWarningsEnabled())
        {
          if (options && options.clearBasket) {
            self.getWarningsContainer().clear();
          } else {
            self.getWarningsContainer().refreshWarnings();
          }
        }
        for (var i = 0; i < therapies.length; i++)
        {
          var therapy = therapies[i].therapy;
          if (therapy && therapy.baselineInfusion)
          {
            self._removeBaselineInfusion(therapy);
          }
        }
      },
      editTherapyFunction: function(therapyContainer)
      {
        if (self.getOrderingContainer().unfinishedOrderExists()) {
          var confirmDialog = appFactory.createConfirmSystemDialog(
              view.getDictionary('unfinished.therapy.that.will.not.be.saved.warning'),
              function (confirmed)
              {
                if (confirmed == true)
                {
                  self._editBasketTherapy(therapyContainer);
                }
              }
          );
          confirmDialog.setWidth(380);
          confirmDialog.setHeight(122);
          confirmDialog.show();
        }
        else
        {
          self._editBasketTherapy(therapyContainer);
        }
      },
      saveTemplateFunction: function(therapies)
      {
        self._openSaveTemplateDialog(therapies, false);
      }
    });
  },

  clear: function ()
  {
    this.orderingContainer.clear();
    this.basketContainer.clear();
    this.warningsContainer.clear();
  },

  getOrderingContainer: function()
  {
    return this.orderingContainer;
  },

  getBasketContainer: function()
  {
    return this.basketContainer;
  },

  getWarningsContainer: function()
  {
    return this.warningsContainer;
  },

  isWarningsEnabled: function()
  {
    return this.warningsEnabled === true;
  },

  getDialogResultCallback: function()
  {
    return this.resultCallback;
  },

  getTherapyTemplateMode: function()
  {
    return app.views.medications.TherapyEnums.therapyTemplateModeEnum.INPATIENT;
  }
});

