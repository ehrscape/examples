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

Class.define('app.views.medications.ordering.OrderingContainer', 'tm.jquery.Container', {
  cls: "ordering-container",
  /** configs */
  view: null,
  withStartEndTime: true,
  withSupply: false, /* denotes withStartEndTime */
  withOxygenSaturation: true,
  confirmTherapyEvent: null,
  saveDateTimePaneEvent: null,
  addTherapiesToBasketFunction: null,
  saveTherapyToTemplateFunction: null,
  getPatientMedsForWarningsFunction: null,
  isPastMode: false,
  presetDate: null, //optional
  additionalMedicationSearchFilter: null, //optional
  getBasketTherapiesFunction: null,
  refreshBasketFunction: null,
  warningsLoaded: null,
  lastTherapyForWarnings: null,
  showSource: null,
  templateMode: null,
  medicationRuleUtils: null,
  preventUnlicensedMedicationSelection: false,
  /** privates: components */
  header: null,
  cardContainer: null,

  visibilityContext: null,

  therapySelectionCard: null,
  searchContainer: null,
  templatesContainer: null,
  simpleTherapyContainer: null,
  complexTherapyContainer: null,
  _oxygenTherapyContainer: null,
  _therapyMedicationDataLoader: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    if (this.withSupply === true) {
      // not compatible due to PRN button
      this.withStartEndTime = false;
    }

    this.medicationRuleUtils = this.getConfigValue("medicationRuleUtils",
        new tm.views.medications.MedicationRuleUtils({view: this.view}));

    this._therapyMedicationDataLoader = new app.views.medications.common.TherapyMedicationDataLoader({
      view: this.getView()
    });

    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function ()
  {
    var self = this;

    this.header = this.createHeadContainer(this);

    if (tm.jquery.ClientUserAgent.isIPad()) /* ipad performance fix */
    {
      this.cardContainer = new tm.jquery.CardContainer({flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")});
    }
    else
    {
      this.cardContainer = new tm.jquery.SimpleCardContainer({
        prerendering: false,
        optimized: false,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });
    }
    this.therapySelectionCard = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch")
    });

    this.searchContainer = new app.views.medications.ordering.SearchContainer({
      view: this.view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      additionalFilter: this.additionalMedicationSearchFilter,
      medicationSelectedEvent: function (medicationData)
      {
        self._handleMedicationSelected(medicationData, true);
      }
    });
    this.templatesContainer = new app.views.medications.ordering.TemplatesContainer({
      cls: "templates-container",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.view,
      templateMode: this.templateMode,
      addTherapiesToBasketFunction: this.addTherapiesToBasketFunction,
      editTherapyFunction: function (therapyContainer)
      {
        self.editTherapy(therapyContainer, false);
      }
    });
    this.simpleTherapyContainer = new app.views.medications.ordering.SimpleTherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.view,
      withStartEndTime: this.withStartEndTime,
      withSupply: this.withSupply,
      isPastMode: this.isPastMode,
      showSource: this.showSource,
      visibilityContext: this.visibilityContext,
      medicationRuleUtils: self.medicationRuleUtils,
      changeCardEvent: function(data, warnings)
      {
        if (data == 'TEMPLATES')
        {
          self.clear();
        }
        else
        {
          self.complexTherapyContainer.setMedicationData(data, true, warnings, self.warningsLoaded);
          self.complexTherapyContainer.removeBnfWarning();
          self.cardContainer.setActiveItem(self.complexTherapyContainer);
        }
      },
      saveToTemplateFunction: function (therapy, invalidTherapy)
      {
        self.saveTherapyToTemplateFunction(therapy, invalidTherapy);
      },
      confirmTherapyEvent: function (therapy, changeReason)
      {
        return self.confirmTherapyEvent(therapy, changeReason);
      },
      saveDateTimePaneEvent: function ()
      {
        self.saveDateTimePaneEvent();
      }
    });
    this.simpleTherapyContainer.setContentExtensions(this.buildPrescriptionContentExtensions(this.simpleTherapyContainer));
    this.complexTherapyContainer = new app.views.medications.ordering.ComplexTherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.view,
      startProcessOnEnter: true,
      isPastMode: this.isPastMode,
      withStartEndTime: this.withStartEndTime,
      withSupply: this.withSupply,
      showSource: this.showSource,
      additionalMedicationSearchFilter: self.additionalMedicationSearchFilter,
      visibilityContext: this.visibilityContext,
      medicationRuleUtils: self.medicationRuleUtils,
      preventUnlicensedMedicationSelection: this.preventUnlicensedMedicationSelection,
      changeCardEvent: function(data, warnings, reloadWarnings)
      {
        if (data == 'TEMPLATES')
        {
          self.clear();
        }
        else
        {
          self.simpleTherapyContainer.setMedicationData(data, true, warnings, self.warningsLoaded);
          self.simpleTherapyContainer.removeBnfWarning();
          self.cardContainer.setActiveItem(self.simpleTherapyContainer);
          if (reloadWarnings)
          {
            self.lastTherapyForWarnings = self.simpleTherapyContainer.getSimpleTherapy();
            self._screenSelectedTherapyForWarnings(
                self.lastTherapyForWarnings,
                function(selectedTherapy, warnings)
                {
                  self._handleLoadedWarnings(selectedTherapy, warnings)
                }
            );
          }
        }
        this.clear();
      },
      saveToTemplateFunction: function (therapy, invalidTherapy)
      {
        self.saveTherapyToTemplateFunction(therapy, invalidTherapy);
      },
      confirmTherapyEvent: function (therapy, changeReason, linkedTherapy)
      {
        var result = self.confirmTherapyEvent(therapy, changeReason, linkedTherapy);
        if (result)
        {
          this.clear();
        }
        return result;
      },
      saveDateTimePaneEvent: function ()
      {
        self.saveDateTimePaneEvent();
      },
      getBasketTherapiesFunction: function ()
      {
        return self.getBasketTherapiesFunction();
      },
      refreshBasketFunction: function ()
      {
        self.refreshBasketFunction();
      },
      screenTherapyForWarningsFunction: function ()
      {
        self.lastTherapyForWarnings = self.complexTherapyContainer.getComplexTherapy();
        self._screenSelectedTherapyForWarnings(
            self.lastTherapyForWarnings,
            function(selectedTherapy, warnings)
            {
              self._handleLoadedWarnings(selectedTherapy, warnings)
            }
        );
      }
    });
    this.complexTherapyContainer.setContentExtensions(this.buildPrescriptionContentExtensions(this.complexTherapyContainer));
    this._oxygenTherapyContainer = new app.views.medications.ordering.OxygenTherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.view,
      withStartEndTime: this.withStartEndTime,
      withSaturation: this.isWithOxygenSaturation(),
      withSupply: this.withSupply,
      pastMode: this.isPastMode,
      showSource: this.showSource,
      confirmTherapyCallback: function (therapy, changeReason)
      {
        var result = self.confirmTherapyEvent(therapy, changeReason);
        if (result)
        {
          this.clear();
        }
        return result;
      },
      saveToTemplateCallback: function (therapy, invalidTherapy)
      {
        self.saveTherapyToTemplateFunction(therapy, invalidTherapy);
      }
    });

    this._oxygenTherapyContainer.on(
        app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_CANCEL_EDIT,
        function(component)
        {
          self.clear();
          component.clear();
        });
    this._oxygenTherapyContainer.on(
        app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE,
        function(component)
        {
          self.saveDateTimePaneEvent();
        });
  },

  _buildGui: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    this.buildTherapySelectionCardContent();

    this.cardContainer.add(this.therapySelectionCard);
    this.cardContainer.add(this.simpleTherapyContainer);
    this.cardContainer.add(this.complexTherapyContainer);
    this.cardContainer.add(this._oxygenTherapyContainer);

    if (!tm.jquery.Utils.isEmpty(this.header))
    {
      this.add(this.header);
    }
    this.add(this.cardContainer);
  },

  _handleMedicationSelected: function(medicationData, clear)
  {
    if (medicationData == null)
    {
      throw Error("selected medication data is null");
    }
    var self = this;
    if (this.preventUnlicensedMedicationSelection && medicationData.isUnlicensedMedication())
    {
      this.searchContainer.abortUnlicensedMedicationSelection();
    }
    else if (medicationData.getMedication().isOxygen())
    {
      this.cardContainer.setActiveItem(this._oxygenTherapyContainer);
      setTimeout(function yieldToPaint()
      {
        self._oxygenTherapyContainer.setMedicationData(medicationData);
        self._oxygenTherapyContainer.setChangeReasonRequired(false);
        self._oxygenTherapyContainer.setTherapyStart(
            tm.views.medications.MedicationTimingUtils.getTimestampRoundedUp(
                CurrentTime.get(),
                5)
        ); // preset by design
      }, 0);
    }
    else if (medicationData.doseForm &&
        medicationData.doseForm.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX)
    {
      self.complexTherapyContainer.setMedicationData(medicationData, true);
      this.cardContainer.setActiveItem(this.complexTherapyContainer);
      setTimeout(function yieldToPaint()
      {
        self.complexTherapyContainer.setChangeReasonRequired(false);
        self.complexTherapyContainer.setWarningsMessage(true, self.view.getDictionary('loading.warnings'));
        self.lastTherapyForWarnings = self.complexTherapyContainer.getComplexTherapy();
        self._screenSelectedTherapyForWarnings(
            self.lastTherapyForWarnings,
            function(selectedTherapy, warnings)
            {
              self._handleLoadedWarnings(selectedTherapy, warnings)
            }
        );
      }, 0);
    }
    else
    {
      this.cardContainer.setActiveItem(this.simpleTherapyContainer);
      // Use timeout so setting data to components is pushed to the end of call stack and happens after 
      // render event (which has it's own zero timeouts) is finished
      setTimeout(
          function()
          {
            self.simpleTherapyContainer.setMedicationData(medicationData, clear);
            self.simpleTherapyContainer.setChangeReasonRequired(false);
            self.simpleTherapyContainer.setWarningsMessage(true, self.view.getDictionary('loading.warnings'));
            self.lastTherapyForWarnings = self.simpleTherapyContainer.getSimpleTherapy();
            self._screenSelectedTherapyForWarnings(
                self.lastTherapyForWarnings,
                function(selectedTherapy, warnings)
                {
                  self._handleLoadedWarnings(selectedTherapy, warnings)
                }
            );
          }, 0
      );
    }
  },

  _handleLoadedWarnings: function(selectedTherapy, warnings)
  {
    var activeItem = this.isRendered() ? this.getCardContainerActiveItemContent() : null;

    if (selectedTherapy === this.lastTherapyForWarnings &&
      (activeItem instanceof app.views.medications.ordering.SimpleTherapyContainer)
        || activeItem instanceof app.views.medications.ordering.ComplexTherapyContainer)
    {
      this.getCardContainerActiveItemContent().setWarnings(warnings);
      this.warningsLoaded = true;
    }
  },

  _screenSelectedTherapyForWarnings: function (therapy, callback)
  {
    this.warningsLoaded = false;
    tm.views.medications.warning.WarningsHelpers.screenSelectedMedicationForWarnings(
        this.view,
        therapy,
        this.getPatientMedsForWarningsFunction(),
        callback
    );
  },

  buildPrescriptionContentExtensions: function(prescriptionContainer)
  {
    return [];
  },

  /** public methods */
  clear: function ()
  {
    this.cardContainer.setActiveItem(this.therapySelectionCard);
    this.searchContainer.clear();
  },

  unfinishedOrderExists: function ()
  {
    return this.cardContainer.getActiveItem() && this.getCardContainerActiveItemContent() != this.therapySelectionCard;
  },

  editTherapy: function(therapyContainer, changeReasonRequired, callback)
  {
    var view = this.getView();
    var therapy = therapyContainer.getData().therapy;
    var self = this;

    this._therapyMedicationDataLoader.load(therapy).then(function onDataLoad(medicationData)
    {
      if (self._therapyMedicationDataLoader.isMedicationNoLongerAvailable(therapy, medicationData))
      {
        // make sure it's still active
        var message = view.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            view.getDictionary('stop.therapy.order.alternative.medication');
        view.getAppFactory().createWarningSystemDialog(message, 500, 160).show();
        return;
      }
      if (therapy.isOrderTypeOxygen())
      {
        self.cardContainer.setActiveItem(self._oxygenTherapyContainer);
        setTimeout(function yieldToPaint()
        {
          self._oxygenTherapyContainer.setMedicationData(medicationData);
          self._oxygenTherapyContainer.setChangeReasonRequired(changeReasonRequired === true);
          self._oxygenTherapyContainer.setValues(therapy);
        }, 0);
      }
      else if (therapy.isOrderTypeComplex())
      {
        self.complexTherapyContainer.clear();
        self.complexTherapyContainer.setMedicationDataByTherapy(therapy, medicationData);
        self.cardContainer.setActiveItem(self.complexTherapyContainer);
        setTimeout(function yieldToPaint()
        {
          self.complexTherapyContainer.setComplexTherapy(therapy, therapy.getStart() != null, null);
          self.complexTherapyContainer.setChangeReasonRequired(changeReasonRequired === true);
          self.complexTherapyContainer.setWarningsMessage(true, self.getView().getDictionary('loading.warnings'));
          self.lastTherapyForWarnings = therapy;
          self._screenSelectedTherapyForWarnings(
              therapy,
              function(selectedTherapy, warnings)
              {
                self._handleLoadedWarnings(selectedTherapy, warnings)
              }
          );
        }, 0);
      }
      else
      {
        self.cardContainer.setActiveItem(self.simpleTherapyContainer);
        setTimeout(
            function()
            {
              self.simpleTherapyContainer.setSimpleTherapyData(therapy, medicationData, therapy.getStart() != null, false);
              self.simpleTherapyContainer.setChangeReasonRequired(changeReasonRequired === true);
              self.simpleTherapyContainer.setWarningsMessage(true, self.getView().getDictionary('loading.warnings'));
              self.lastTherapyForWarnings = therapy;
              self._screenSelectedTherapyForWarnings(
                  therapy,
                  function(selectedTherapy, warnings)
                  {
                    self._handleLoadedWarnings(selectedTherapy, warnings)
                  }
              );
            },
            0
        );
      }
    });

    if (callback) callback(therapyContainer);
  },

  presetMedication: function (medicationId)
  {
    var self = this;

    this.getView().getRestApi().loadMedicationData(medicationId).then(function onSuccessHandler(medicationData){
      if (self.isRendered())
      {
        self._handleMedicationSelected(medicationData, false);
      }
    });
  },

  reloadTemplates: function ()
  {
    this.templatesContainer.reloadTemplates();
  },

  getTemplates: function ()
  {
    return this.templatesContainer.getTemplates();
  },

  getTherapySelectionCard: function()
  {
    return this.therapySelectionCard;
  },

  /**
   * Override for different content - @see {app.views.medications.reconciliation.MedicineReconciliationContainer}.
   */
  createHeadContainer: function ()
  {
    var patientDataContainer = new tm.jquery.Container({
      cls: 'TextLabel patinet-data-container',
      horizontalAlign: 'right',
      html: tm.views.medications.MedicationUtils.getPatientsReferenceWeightAndHeightHtml(this.view),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    var presetDate = this.view.getPresetDate();

    var header = new app.views.medications.ordering.MedicationsTitleHeader({
      view: this.view,
      title: presetDate ? this.view.getDictionary('therapy.order') + "  " + this.view.getDisplayableValue(new Date(presetDate), "short.date.time") : this.view.getDictionary('therapy.order'),
      additionalDataContainer: patientDataContainer
    });

    return header;
  },

  /***
   * Override to add content to the therapy selection card. @see {app.views.medications.reconciliation.MedicineReconciliationContainer}.
   */
  buildTherapySelectionCardContent: function()
  {
    var contentScrollContainer = new tm.jquery.Container({
      cls: "content-scroll-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      scrollable: 'vertical',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    contentScrollContainer.add(this.templatesContainer);

    this.therapySelectionCard.add(this.searchContainer);
    this.therapySelectionCard.add(contentScrollContainer);
  },
  getCardContainerActiveItemContent: function ()
  {
    var cardContainer = this.cardContainer;
    return cardContainer instanceof tm.jquery.SimpleCardContainer ?
        cardContainer.getActiveItem().getContent() :
        cardContainer instanceof tm.jquery.CardContainer ? cardContainer.getActiveItem() : null
  },

  /**
   * @returns {boolean}
   */
  isWithOxygenSaturation: function()
  {
    return this.withOxygenSaturation === true;
  },
  
  getView: function()
  {
    return this.view;
  }
});
