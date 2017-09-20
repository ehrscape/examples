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
Class.define('app.views.medications.reconciliation.MedicineReconciliationContainer', 'tm.jquery.Container', {
  cls: "reconciliation-container",
  view: null,

  startEndTimeSupport: true,
  oxygenSaturationSupported: false,

  leftColumnTitle: null,
  rightColumnTitle: null,
  warningsEnabled: true,
  baselineInfusionIntervals: null,
  basketTherapyDisplayProvider: null,
  oldTherapiesForWarningsSearch: null,
  showSource: false,
  medicationRuleUtils: null,

  bnfPercentageSum: 0,
  currentBnfMedications: null,

  _basketContainer: null,
  _therapySelectionContainer: null,
  _warningsContainer: null,

  Constructor: function (config)
  {
    this.callSuper(config);
    this.baselineInfusionIntervals = [];
    this.oldTherapiesForWarningsSearch = [];
    this.currentBnfMedications = [];

    this.basketTherapyDisplayProvider = this.getConfigValue("basketTherapyDisplayProvider", new app.views.medications.TherapyDisplayProvider({
      view: this.view
    }));

    this.medicationRuleUtils = this.getConfigValue(
        "medicationRuleUtils",
        new tm.views.medications.MedicationRuleUtils({view: this.view}));

    this._buildGUI();
  },

  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch"));

    this.add(this.buildLeftColumn(this));
    this.add(this.buildRightColumn(this));
  },

  buildLeftColumn: function ()
  {
    var self = this;
    var therapySelectionColumn = new app.views.medications.reconciliation.TherapySelectionColumn({
      titleText: this.getLeftColumnTitle(),
      view: this.getView(),
      withStartEndTime: this.isStartEndTimeSupported(),
      withOxygenSaturation: this.isOxygenSaturationSupported(),
      showSource: this.showSource,
      templateMode: self.getTherapyTemplateMode(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "720px"),
      addTherapiesToBasketFunction: function(therapies)
      {
        therapies.forEach(function(therapy)
        {
          therapy.rescheduleTherapyTimings(false);
          if (self._isPrescriptionValid(therapy))
          {
            self._getTherapyDto(therapy, function(therapyDto)
            {
              if (therapy.baselineInfusion && !tm.jquery.Utils.isEmpty(self.baselineInfusionIntervals))
              {
                self.baselineInfusionIntervals.push({
                  startMillis: therapy.start.getTime(),
                  endMillis: therapy.end ? therapy.end.getTime() : null
                })
              }

              // clear the source container, we have no idea where it came from, could be a whole template..
              self.getTherapySelectionContainer().setSourceTherapyContainer(null);
              self.addToBasket(therapyDto);
            });
          }
        }, this);
      },
      confirmTherapyEvent: function (data, changeReason)
      {
        if (data != 'VALIDATION_FAILED')
        {
          var validPrescription = self._isPrescriptionValid(data);
          if (validPrescription)
          {
            self._getTherapyDto(data, function (therapyDto)
            {
              if (data.baselineInfusion && !tm.jquery.Utils.isEmpty(self.baselineInfusionIntervals))
              {
                self.baselineInfusionIntervals.push({
                  startMillis: data.start.getTime(),
                  endMillis: data.end ? data.end.getTime() : null
                })
              }
              self.addToBasket(therapyDto, changeReason);
              self.onTherapySelectionColumnConfirmEdit(therapyDto);
            });
          }
          return validPrescription;
        }
        return false;
      },
      removeFromBasketFunction: function (therapy)
      {
        self._removeTherapyFromBasket(therapy);
      },
      saveTherapyToTemplateFunction: function (therapy, invalidTherapy)
      {
        self._openSaveTemplateDialog([therapy], true, invalidTherapy);
      },
      getBasketTherapiesFunction: function ()
      {
        return self.getBasketContents();
      },
      attachTherapyGroupPanels: function (container)
      {
        self.attachTherapyGroupPanels(container);
      },
      getPatientMedsForWarningsFunction: function()
      {
        return self._getTherapiesForWarningsSearch(false);
      }
    });

    this.setTherapySelectionContainer(therapySelectionColumn);

    return therapySelectionColumn;
  },

  buildRightColumn: function ()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var container = new tm.jquery.Container({
      cls: "right-column",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto") // ordering container preset
    });

    var basketContainer = new app.views.medications.reconciliation.BasketContainer({
      view: this.getView(),
      headerTitle: this.getRightColumnTitle(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      displayProvider: this.getBasketTherapyDisplayProvider(),
      therapyAddedEvent: function ()
      {
        self.onTherapyAddedToBasket();
      },
      therapiesRemovedEvent: function (removedElementsData)
      {
        var therapies = [];
        for (var i = 0; i < removedElementsData.length; i++)
        {
          var elementData = removedElementsData[i];
          var therapy = elementData.therapy;
          if (therapy && therapy.baselineInfusion)
          {
            self._removeBaselineInfusion(therapy);
          }
          self.onTherapyRemovedFromBasket(elementData);
          therapies.push(therapies);
        }
        if (self.isWarningsEnabled())
        {
          self._removeParacetamolLimitWarning(therapies);
        }
      },
      editTherapyFunction: function (therapyContainer)
      {
        var therapy = therapyContainer.getData().therapy;

        if (self.getTherapySelectionContainer().unfinishedOrderExists())
        {
          var confirmDialog = appFactory.createConfirmSystemDialog(
              view.getDictionary('unfinished.therapy.that.will.not.be.saved.warning'),
              function(confirmed)
              {
                if (confirmed == true)
                {
                  triggerEditTherapy();
                }
              }
          );
          confirmDialog.setWidth(380);
          confirmDialog.setHeight(122);
          confirmDialog.show();
        }
        else
        {
          triggerEditTherapy();
        }

        function triggerEditTherapy()
        {
          self.getBasketContainer().removeTherapy(therapy);
          self.onEditTherapy(therapyContainer);
          if (self.isWarningsEnabled())
          {
            self.getWarningsContainer().refreshWarnings();
          }
        }
      },
      saveTemplateFunction: function (therapies)
      {
        self._openSaveTemplateDialog(therapies, false);
      }
    });

    container.add(basketContainer);
    this.setBasketContainer(basketContainer);


    if (this.isWarningsEnabled())
    {
      var warningsContainer = new app.views.medications.ordering.WarningsContainer({
        view: view,
        //height: this.warningsEnabled ? 300 : 30,
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "300px"),
        getPatientMedsForWarningsFunction: function()
        {
          return self._getTherapiesForWarningsSearch(true);
        }
      });
      container.add(warningsContainer);
      this.setWarningsContainer(warningsContainer);

    }

    return container;
  },

  /* Override and extend content building. */
  attachTherapyGroupPanels: function (container)
  {

  },

  _validateCriticalWarnings: function()
  {
    var areCriticalWarningsOverriden = this.getWarningsContainer().assertAllCriticalWarningsOverridden();
    if (!areCriticalWarningsOverriden)
    {
      var message = this.view.getDictionary('you.have.unchecked.warnings');
      this.view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
      return false;
    }
    return true;
  },

  _attachGroupPanelElementToolbar: function (elementContainer)
  {
    var self = this;
    var toolbar = new app.views.medications.reconciliation.TherapyContainerPanelToolbar({
      therapyContainer: elementContainer,
      withSuspend: false,
      withCancel: false,
      addToBasketEventCallback: function (therapyContainer)
      {
        therapyContainer.getToolbar().setEnabled(false, true); // prevent double clicks when the server lags
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        var therapy = therapyContainer.getData().therapy;
        therapy.rescheduleTherapyTimings(false);
        if (self._isPrescriptionValid(therapy))
        {
          self._getTherapyDto(therapy, function (therapyDto)
          {
            if (therapy.baselineInfusion && !tm.jquery.Utils.isEmpty(self.baselineInfusionIntervals))
            {
              self.baselineInfusionIntervals.push({
                startMillis: therapy.start.getTime(),
                endMillis: therapy.end ? therapy.end.getTime() : null
              })
            }
            self.addToBasket(therapyDto, null, therapyContainer);
            therapyContainer.getToolbar().setEnabled(true, true);
          });
        }
        else
        {
          therapyContainer.getToolbar().setEnabled(true, true);
        }
        ;
      },
      addToBasketWithEditEventCallback: function (therapyContainer)
      {
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        self.onEditTherapy(therapyContainer);
      }
    });
    elementContainer.setToolbar(toolbar);
  },

  _removeBaselineInfusion: function(therapy)
  {
    if (tm.jquery.Utils.isEmpty(this.baselineInfusionIntervals)) return;

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

  _updateCalculatedBnfPercentage: function(therapy)
  {
    var calculatedBnf = 0;
    var basketTherapies = this.getBasketContainer().getTherapies();
    var warningTherapies = basketTherapies.filter(function(therapy){
          if (!tm.jquery.Utils.isEmpty(therapy.bnfMaximumPercentage)) {
            calculatedBnf += therapy.bnfMaximumPercentage;
            return true;
          }
    }, this);

    if (calculatedBnf > 100)
    {
      this.getWarningsContainer().addBnfWarning(calculatedBnf, warningTherapies);
    }
    else
    {
      this.getWarningsContainer().removeAdditionalWarning(app.views.medications.TherapyEnums.additionalWarningType.BNF);
    }
  },

  
  _removeParacetamolLimitWarning: function(therapies)
  {
    if (this.isWarningsEnabled())
    {
      var basketTherapies = this.getBasketContainer().getTherapies();
      if (therapies)
      {
        basketTherapies = basketTherapies.filter(function(item)
        {
          return therapies.indexOf(item) === -1;
        });
      }
      this.getWarningsContainer().refreshParacetamolLimitWarning(basketTherapies, null, false);
    }
  },

  fillTherapyWarningsBeforeSave: function(orders, warnings)
  {
    if (!this.isWarningsEnabled()) return;

    warnings = this.getWarningsContainer().getOverriddenWarnings();
    var enums = app.views.medications.TherapyEnums;

    for (var i = 0; i < warnings.length; i++)
    {
      var overrideReason = warnings[i].overrideReason;
      var warningDto = warnings[i].warning; //MedicationsWarningDto

      if (warningDto.medications.length != 0)
      {
        for (var j = 0; j < orders.length; j++)
        {
          var therapy = orders[j].therapy;
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

  /* Override for custom objects in the basket */
  addToBasket: function (therapy, changeReason, sourceContainer)
  {
    if (sourceContainer != null){
      this.markSourceTherapyContainer(sourceContainer);
    }
    this.getBasketContainer().addTherapy({therapy: therapy, changeReasonDto: changeReason });
  },

  _baselineInfusionsIntersects: function (start, end)
  {
    if (tm.jquery.Utils.isEmpty(this.baselineInfusionIntervals)) return false;

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

  _isPrescriptionValid: function (therapy)
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();
    var enums = app.views.medications.TherapyEnums;

    var baselineInfusionAlreadyExists = !tm.jquery.Utils.isEmpty(this.baselineInfusionIntervals) &&
        therapy.baselineInfusion && this._baselineInfusionsIntersects(therapy.start, therapy.end);

    if (baselineInfusionAlreadyExists && this.assertBaselineInfusion)
    {
      appFactory.createWarningSystemDialog(self.getView().getDictionary("patient.already.has.baseline.infusion"), 320, 122).show();
      return false;
    }
    return true;
  },

  _getTherapyDto: function (therapy, callback)
  {
    var params = {therapy: JSON.stringify(therapy)};
    var fillDisplayValuesUrl =
        this.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FILL_DISPLAY_VALUES;

    this.getView().loadPostViewData(fillDisplayValuesUrl, params, null, function (displayValuesJson)
    {
      var therapyDto = app.views.medications.common.TherapyJsonConverter.convert(displayValuesJson);
      if (!tm.jquery.Utils.isEmpty(therapy.completed))
      {
        therapyDto.setCompleted(therapy.completed);
      }
      callback(therapyDto);
    });
  },

  _getTherapiesForWarningsSearch: function(prospective)
  {
    var therapiesForWarnings = [];
    var therapies = this.getBasketContainer().getTherapies();
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

  _removeTherapyFromBasket: function (therapy)
  {
    this.getBasketContainer().removeTherapy(therapy);
  },

  _openSaveTemplateDialog: function (therapies, addSingleTherapy, invalidTherapy)
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        this.getView().getDictionary('therapy.template'),
        null,
        new app.views.medications.ordering.SaveTemplatePane({
          view: self.getView(),
          templateMode: self.getTherapyTemplateMode(),
          startProcessOnEnter: true,
          padding: 8,
          addSingleTherapy: addSingleTherapy,
          invalidTherapy: invalidTherapy,
          therapies: therapies,
          templates: self.getTherapySelectionContainer().getTemplates()
        }),
        function (resultData)
        {
          if (resultData)
          {
            self.getTherapySelectionContainer().reloadTemplates();
          }
        },
        300,
        300
    );

    dialog.show();
  },

  /***
   * Triggered by the basket component once a therapy was successfully added.
   */
  onTherapyAddedToBasket: function ()
  {
    this.getTherapySelectionContainer().showList();

    if (this.isWarningsEnabled())
    {
      this._updateCalculatedBnfPercentage();
      this.getWarningsContainer().refreshParacetamolLimitWarning([], this.getBasketContainer().getTherapies(), true);
      this.getWarningsContainer().refreshWarnings();
    }
  },

  onTherapyRemovedFromBasket: function (elementData)
  {
    if (this.isWarningsEnabled())
    {
      this._updateCalculatedBnfPercentage();
      this.getWarningsContainer().refreshWarnings();
    }
  },

  onEditTherapy: function (therapyContainer, callback)
  {
    var data = therapyContainer.getData();
    var therapy = data.getTherapy();

    this.getTherapySelectionContainer().editTherapy(therapyContainer, therapy.isLinkedToAdmission(), callback);
    if (this.isWarningsEnabled())
    {
      this.getWarningsContainer().refreshParacetamolLimitWarning([], this.getBasketContainer().getTherapies(), true);
    }
  },

  onTherapySelectionColumnConfirmEdit: function (therapy)
  {
    var sourceContainer = this.getTherapySelectionContainer().getSourceTherapyContainer();

    if (sourceContainer != null)
    {
      this.markSourceTherapyContainer(sourceContainer);
    }
  },

  /* for override - remove effects or unhide the therapy once removed from the basket container */
  unmarkSourceTherapyContainer: function (container)
  {

  },

  /* for override - add effects or hide when the therapy is added to the basket */
  markSourceTherapyContainer: function (container)
  {

  },

  /* Getters, setters */
  getBasketContents: function ()
  {
    return this.getBasketContainer().getTherapies();
  },

  getView: function ()
  {
    return this.view;
  },

  getLeftColumnTitle: function ()
  {
    return this.leftColumnTitle;
  },

  getRightColumnTitle: function ()
  {
    return this.rightColumnTitle;
  },

  setBasketContainer: function (container)
  {
    this._basketContainer = container;
  },
  getBasketContainer: function ()
  {
    return this._basketContainer;
  },
  setWarningsContainer: function(container)
  {
    this._warningsContainer = container;
  },
  getWarningsContainer: function()
  {
    return this._warningsContainer;
  },
  isWarningsEnabled: function()
  {
    return this.warningsEnabled === true;
  },
  setTherapySelectionContainer: function (value)
  {
    this._therapySelectionContainer = value;
  },
  getTherapySelectionContainer: function ()
  {
    return this._therapySelectionContainer;
  },
  getBasketTherapyDisplayProvider: function()
  {
    return this.basketTherapyDisplayProvider;
  },
  /**
   * @returns {boolean}
   */
  isStartEndTimeSupported: function ()
  {
    return this.startEndTimeSupport === true;
  },
  setOldTherapiesForWarningsSearch: function(therapies)
  {
    this.oldTherapiesForWarningsSearch = therapies;
  },
  /**
   * @returns {boolean}
   */
  isOxygenSaturationSupported: function()
  {
    return this.oxygenSaturationSupported === true;
  },

  getTherapyTemplateMode: function()
  {
    return app.views.medications.TherapyEnums.therapyTemplateModeEnum.INPATIENT;
  },

  assertAndWarnWarningsDataLoading: function()
  {
    var deferred = tm.jquery.Deferred.create();
    if (this.getWarningsContainer().isDataLoading())
    {
      var confirmDialog = this.getView().getAppFactory().createConfirmSystemDialog(this.view.getDictionary('interactions.not.loaded.warning'),
          function(confirmed)
          {
            if (confirmed === true)
            {
              deferred.resolve();
            }
            else
            {
              deferred.reject();
            }
          }
      );
      confirmDialog.setWidth(380);
      confirmDialog.setHeight(122);
      confirmDialog.show();
    }
    else
    {
      deferred.resolve();
    }
    return deferred;
  }
});