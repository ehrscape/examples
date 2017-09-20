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
Class.define('app.views.medications.outpatient.OutpatientOrderingContainer', 'app.views.medications.ordering.MedicationsOrderingContainer', {

  defaultWidth: null,
  defaultHeight: null,
  templateMode: 'OUTPATIENT',

  _lastViewActionDeferred: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getDefaultWidth: function()
  {
    return tm.jquery.Utils.isEmpty(this.defaultWidth) ? $(window).width() - 50 : this.defaultWidth;
  },

  getDefaultHeight: function()
  {
    return tm.jquery.Utils.isEmpty(this.defaultHeight) ? $(window).height() - 10 : this.defaultHeight;
  },

  /* overriding to set content extensions for prescribing */
  buildOrderingContainer: function()
  {
    var view = this.view;
    var isPastMode = this.isPastMode;
    var self = this;

    var visibilityContext = {
      medicationsShowHeparinPane : false,
      infusionRateTypeSelectionVisible : false,
      variableButtonVisible : true,
      daysButtonVisibile : true,
      pastDaysOfTherapyVisible : false,
      indicationFieldVisible: false,
      dosingPatternPaneVisible: false,
      titratedDoseModeVisible: false
    };

    return new app.views.medications.ordering.OrderingContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "720px"),
      mainContainer: this,
      isPastMode: isPastMode,
      withStartEndTime: false,
      withSupply: true,
      visibilityContext: visibilityContext,
      templateMode: this.getTherapyTemplateMode(),
      additionalMedicationSearchFilter: app.views.medications.TherapyEnums.medicationFinderFilterEnum.OUTPATIENT_PRESCRIPTION,
      preventUnlicensedMedicationSelection: true,
      addTherapiesToBasketFunction: function(therapies)
      {
        var basketContainerElementsCount = self.basketContainer.getTherapies().length;
        for (var i = 0; i < therapies.length; i++)
        {
          var therapy = therapies[i];
          self._addToBasket(therapy, therapies.length, basketContainerElementsCount);
        }
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePane.setHeight(34);
        self.saveDateTimePane.setPadding('4 0 0 0');
        self.saveDateTimePane.show();
        self.saveDateTimePane.repaint();
      },
      confirmTherapyEvent: function(data)
      {
        if (data != 'VALIDATION_FAILED')
        {
          return self._addToBasket(data);
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
      },
      buildPrescriptionContentExtensions: function(prescriptionContainer)
      {
        return self._buildPrescriptionContentExtensions();
      }
    });
  },

  _buildPrescriptionContentExtensions: function()
  {
    var view = this.view;
    var extensions = [];
    extensions.push(new app.views.medications.outpatient.EERContentExtensionContainer({
      view: view
    }));
    return extensions;
  },

  /**
   * Override to add support for a second parameter, set based on the confirm button we press.
   * @param {Function} resultDataCallback The callback method.
   * @param {Boolean} saveOnly Optional parameter defining if the order will only be saved as oppose to saved and authorised.
   */
  processResultData: function(resultDataCallback, saveOnly)
  {
    var self = this;

    this._validateUserInput().then(
        function validationSuccessHandler(basketItems)
        {
          self.placeOrder(basketItems, saveOnly).then(successResultCallbackHandler, failureResultCallbackHandler);
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

  /**
   * Override, implementing order placement via a view action.
   * @param {Array} basketItems
   * @param {Boolean} saveOnly
   * @returns {tm.jquery.Promise}
   */
  placeOrder: function(basketItems, saveOnly)
  {
    var self = this;

    if (this._lastViewActionDeferred)
    {
      this._lastViewActionDeferred.reject();
    }

    this._lastViewActionDeferred = tm.jquery.Deferred.create();

    var prescriptions = [];
    for (var i = 0; i < basketItems.length; i++)
    {
      prescriptions.push({therapy: basketItems[i].therapy});
    }

    this.view.showLoaderMask();
    var view = this.view;
    view.sendAction("outpatientPrescription", {
      patientId: view.getPatientId(),
      prescriptionBundle: JSON.stringify(
          {
            prescriptionTherapies: prescriptions
          }),
      saveOnly: saveOnly === true // normalise the value to a boolean 
    });

    return this._lastViewActionDeferred.promise();
  },

  //@Overring
  getTherapyTemplateMode: function()
  {
    return app.views.medications.TherapyEnums.therapyTemplateModeEnum.OUTPATIENT;
  },

  /**
   * Called from the view when an view action callback is received. Will resolve or reject the last deferred that
   * was created when sending the action, if present.
   * @param {Object} actionCallback
   */
  onActionCallback: function(actionCallback)
  {
    if (this._lastViewActionDeferred)
    {
      var lastDeferred = this._lastViewActionDeferred;
      this._lastViewActionDeferred = null;

      this.view.hideLoaderMask();

      if (actionCallback.successful)
      {
        lastDeferred.resolve();
      }
      else
      {
        lastDeferred.reject();
      }
    }
  }
});