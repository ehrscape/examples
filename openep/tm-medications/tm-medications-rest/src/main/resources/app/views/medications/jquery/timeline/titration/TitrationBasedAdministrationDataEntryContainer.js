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
Class.define('app.views.medications.timeline.titration.TitrationBasedAdministrationDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'titration-administration-container',
  scrollable: 'vertical',

  /* public members */
  view: null,
  therapy: null,
  administration: null,
  allAdministrations: null,
  enableDosing: true,
  scheduleAdditional: false,
  applyUnplanned: false,
  stopFlow: false,
  administrationType: null,
  lastPositiveInfusionRate: null,
  activeContinuousInfusion: false,

  startProcessOnEnter: false,
  defaultHeight: "auto",
  defaultWidth: 950,
  dataLoader: null,

  _therapyDisplayProvider: null,
  _resizeToActualSizeTask: null,
  _applicationRowContainer: null,
  _doseHistoryRowContainers: null,
  _measurementResultRow: null,

  Constructor: function(config)
  {
    this.callSuper(config);

    this._buildGUI();
    this._configureForm();
    var self = this;
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._reconfigureFooterButtons();
      self._startResizeTask();
    });
  },

  _buildGUI: function()
  {
    var taskTherapy = this.getTherapy();
    var therapyForAdministration = null;
    var titrationData = this.getDataLoader().getCurrentData();
    var measurementResults = titrationData.getResults();
    var isContinuousInfusion = taskTherapy.isContinuousInfusion();

    this._doseHistoryRowContainers = [];

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    this._therapyDisplayProvider = new app.views.medications.TherapyDisplayProvider({
      view: this.getView()
    });

    if (measurementResults)
    {
      this._addMeasurementResultRowContainerForTherapy(titrationData)
    }

    titrationData.getTherapies().forEach(function findAdministrationTherapy(titrationTherapy)
    {
      if (titrationTherapy.getTherapy().getTherapyId() !== taskTherapy.getTherapyId())
      {
        var doseHistoryRowContainer = this._addDoseHistoryRowContainerForTherapy(titrationTherapy, false);
        this._doseHistoryRowContainers.push(doseHistoryRowContainer);
      }
      else
      {
        therapyForAdministration = titrationTherapy;
      }
    }, this);

    if (therapyForAdministration)
    {
      var doseHistoryRowAdministrationContainer = this._addDoseHistoryRowContainerForTherapy(therapyForAdministration,
          true);
      this._doseHistoryRowContainers.push(doseHistoryRowAdministrationContainer);

      if (this.isDosingEnabled() && (!isContinuousInfusion || this.isBolusAdministration()))
      {
        var doseApplicationRowContainer = new app.views.medications.timeline.titration.DoseApplicationRowContainer({
          cls: "dose-application-row-container primary-row",
          view: this.getView(),
          therapyForTitration: therapyForAdministration,
          latestTherapyId: this.getTherapy().getTherapyId(),
          administration: this.getAdministration(),
          medicationData: titrationData.getMedicationData(),
          allAdministrations: this.getAllAdministrations(),
          administrationType: this.getAdministrationType(),
          scheduleAdditional: this.isScheduleAdditional(),
          applyUnplanned: this.isApplyUnplanned()
        });

        this.add(doseApplicationRowContainer);

        this._applicationRowContainer = doseApplicationRowContainer;
      }
      else if (this.isDosingEnabled() && isContinuousInfusion)
      {
        var rateApplicationRowContainer = new app.views.medications.timeline.titration.RateApplicationRowContainer({
          cls: "rate-application-row-container primary-row",
          view: this.getView(),
          therapyForTitration: therapyForAdministration,
          administration: this.getAdministration(),
          medicationData: titrationData.getMedicationData(),
          allAdministrations: this.getAllAdministrations(),
          administrationType: this.getAdministrationType(),
          lastPositiveInfusionRate: this.getLastPositiveInfusionRate(),
          activeContinuousInfusion: this.isActiveContinuousInfusion(),
          latestTherapyId: this.getTherapy().getTherapyId(),
          stopFlow: this.isStopFlow()
        });

        this.add(rateApplicationRowContainer);

        this._applicationRowContainer = rateApplicationRowContainer;
      }
    }
  },

  _abortResizeToActualSizeTask: function()
  {
    if (this._resizeToActualSizeTask)
    {
      this._resizeToActualSizeTask.abort();
      this._resizeToActualSizeTask = null;
    }
  },

  _reconfigureFooterButtons: function()
  {
    if (!this._applicationRowContainer)
    {
      var dialog = this._findParentDialog();
      var footer = dialog && dialog.getFooter();

      if (footer)
      {
        footer.getConfirmButton().setText(this.getView().getDictionary("close"));
        footer.getConfirmButton().setHandler(footer.getCancelButton().getHandler());
        footer.getRightButtons().remove(footer.getCancelButton());
      }
    }
  },

  _startResizeTask: function()
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();

    this._abortResizeToActualSizeTask();

    this._resizeToActualSizeTask = appFactory.createConditionTask(
        function()
        {
          self._applyDialogSize();
        },
        function(task)
        {
          if (!self.isRendered())
          {
            task.abort();
            return;
          }
          return self.isRendered(true);
        },
        50, 1000
    );
  },

  /**
   * @param {app.views.medications.timeline.titration.dto.QuantityWithTime} titrationData
   * @private
   */
  _addMeasurementResultRowContainerForTherapy: function(titrationData)
  {
    var view = this.getView();
    var self = this;
    var dataLoader = this.getDataLoader();

    var measurementResultRow = new app.views.medications.timeline.titration.MeasurementResultRowContainer({
      cls: "measurement-result-row-container",
      view: view,
      titrationData: titrationData,
      displayProvider: this.getDisplayProvider(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    measurementResultRow.on(
        app.views.medications.timeline.titration.MeasurementResultRowContainer.EVENT_TYPE_NAVIGATION_CLICK,
        function navEventListener(component, compEvent)
        {
          var eventData = compEvent.getEventData();
          if (eventData)
          {
            var promise = eventData.moveBack ? dataLoader.getPrev() : dataLoader.getNext();

            promise.then(function()
            {
              self.refreshData()
            });
          }
        });

    this.add(measurementResultRow);

    this._measurementResultRow = measurementResultRow;
  },

  /**
   * @param {app.views.medications.timeline.titration.dto.TherapyForTitration} titrationTherapy
   * @param {boolean} isPrimary
   * @private
   */
  _addDoseHistoryRowContainerForTherapy: function(titrationTherapy, isPrimary)
  {
    var view = this.getView();

    var therapyRow = new app.views.medications.timeline.titration.TherapyDoseHistoryRowContainer({
      cls: "therapy-dose-history-row-container" + (isPrimary ? " primary-row" : ""),
      view: view,
      titrationTherapy: titrationTherapy,
      isPrimary: isPrimary,
      displayProvider: this.getDisplayProvider(),
      startInterval: this.getDataLoader().getCurrentData().getStartInterval(),
      endInterval: this.getDataLoader().getCurrentData().getEndInterval(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this.add(therapyRow);
    return therapyRow;
  },

  _configureForm: function()
  {
    var form = new tm.jquery.Form({
      view: this.getView(),
      showTooltips: false,
      requiredFieldValidatorErrorMessage: this.getView().getDictionary("field.value.is.required")
    });

    if (this._applicationRowContainer)
    {
      this._applicationRowContainer.getFormFields().forEach(function addFieldsToForm(field)
      {
        form.addFormField(field);
      });
    }

    this._form = form;
  },

  /**
   * @returns {tm.jquery.Dialog|null}
   * @private
   */
  _findParentDialog: function()
  {
    var component = this;
    while (component && component.getParent())
    {
      if (component.getParent() instanceof tm.jquery.Dialog)
      {
        return component.getParent();
      }
      component = component.getParent();
    }

    return null;
  },

  _applyDialogSize: function()
  {
    var childrenHeight = 0;
    var $dom = $(this.getDom());
    var dialog = this._findParentDialog();

    if (!dialog)
    {
      this.getView().getLocalLogger().warn("Parent dialog not found, aborting resize");
      return;
    }

    $dom.children().each(function calculateChildrenHeight()
    {
      childrenHeight += $(this).outerHeight(true);
    });

    if (childrenHeight > 0)
    {
      var desiredDialogHeight = childrenHeight + $(dialog.getDom()).outerHeight();
      var maxHeight = $(window).height();

      if (desiredDialogHeight >= maxHeight)
      {
        desiredDialogHeight = maxHeight - 20;
      }

      dialog.setHeight(desiredDialogHeight);
      dialog.applyPosition();
      dialog.removeCls("invisible");
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {Object}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * @returns {Array<Object>}
   */
  getAllAdministrations: function()
  {
    return this.allAdministrations;
  },

  /**
   * @returns {null|app.views.medications.TherapyEnums.administrationTypeEnum}
   */
  getAdministrationType: function()
  {
    return this.administrationType;
  },

  /**
   * @returns {app.views.medications.TherapyDisplayProvider}
   */
  getDisplayProvider: function()
  {
    return this._therapyDisplayProvider;
  },

  /**
   * @returns {tm.jquery.Form}
   */
  getForm: function()
  {
    return this._form;
  },

  /**
   * @returns {number|string}
   */
  getDefaultHeight: function()
  {
    return this.defaultHeight;
  },

  /**
   * @returns {number|string}
   */
  getDefaultWidth: function()
  {
    return this.defaultWidth;
  },

  /**
   * @returns {number|null}
   */
  getLastPositiveInfusionRate: function()
  {
    return this.lastPositiveInfusionRate;
  },

  /**
   * @returns {app.views.medications.timeline.titration.TitrationDataLoader}
   */
  getDataLoader: function()
  {
    return this.dataLoader;
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;
    var form = this.getForm();

    // in case the therapy was not found in titration data
    if (!this._applicationRowContainer)
    {
      var dialog = this._findParentDialog();
      if (dialog)
      {
        dialog.hide();
      }
      return;
    }

    form.setOnValidationSuccess(function()
    {
      var isContinuousInfusion = self.getTherapy().isContinuousInfusion();
      var dose = self._applicationRowContainer.getTherapyDose();
      var plannedTime = self._applicationRowContainer.getAdministrationDateTime();
      var setUntilDate = !isContinuousInfusion ? self._applicationRowContainer.getSetDoseUntilDateTime() : null;
      var isMarkedGiven = self._applicationRowContainer.isMarkedGiven();
      var applicationComment = self._applicationRowContainer.getDoctorsComment();
      self._applicationRowContainer.applyAdministration(dose, plannedTime, isMarkedGiven, applicationComment, setUntilDate)
          .then(successResultCallbackHandler, failureResultCallbackHandler);
    });
    form.setOnValidationError(function()
    {
      failureResultCallbackHandler();
    });

    form.submit();

    function successResultCallbackHandler()
    {
      resultDataCallback(new app.views.common.AppResultData({success: true}));
    }

    function failureResultCallbackHandler()
    {
      resultDataCallback(new app.views.common.AppResultData({success: false}));
    }
  },

  refreshData: function()
  {
    var dataLoader = this.getDataLoader();

    this._measurementResultRow.setTitrationData(dataLoader.getCurrentData());

    var dataLoaderTherapies = dataLoader.getCurrentData().getTherapies();
    var dataLoaderCompositionUid = dataLoader.getCurrentData().getTherapies().map(function(therapy)
    {
      return therapy.getTherapy().getCompositionUid();
    });
    this._doseHistoryRowContainers.forEach(function(container)
    {
      var containerCompositionUid = container.getTitrationTherapy().getTherapy().getCompositionUid();

      var index = dataLoaderCompositionUid.indexOf(containerCompositionUid);
      if (index === -1)
      {
        var missingTherapy = container.getTitrationTherapy();
        container.updateData(missingTherapy,
            dataLoader.getCurrentData().getStartInterval(),
            dataLoader.getCurrentData().getEndInterval());
      }
      else
      {
        container.updateData(dataLoaderTherapies[index],
            dataLoader.getCurrentData().getStartInterval(),
            dataLoader.getCurrentData().getEndInterval());
      }

    });
  },

  /**
   * @returns {boolean}
   */
  isActiveContinuousInfusion: function()
  {
    return this.activeContinuousInfusion === true;
  },

  /**
   * Can dosing be set?
    * @returns {boolean}
   */
  isDosingEnabled: function()
  {
    return this.enableDosing;
  },

  /**
   * @returns {boolean}
   */
  isScheduleAdditional: function()
  {
    return this.scheduleAdditional === true;
  },

  /**
   * @returns {boolean}
   */
  isApplyUnplanned: function()
  {
    return this.applyUnplanned === true;
  },

  /**
   * @returns {boolean}
   */
  isBolusAdministration: function()
  {
    return this.getAdministrationType() === app.views.medications.TherapyEnums.administrationTypeEnum.BOLUS;
  },

  /**
   * @returns {boolean}
   */
  isStopFlow: function()
  {
    return this.stopFlow === true;
  },

  /**
   * Override
   */
  destroy: function()
  {
    this._abortResizeToActualSizeTask();
    this.callSuper();
  }
});