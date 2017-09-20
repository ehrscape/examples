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
Class.define('app.views.medications.common.TherapyDetailsContentContainer', 'app.views.medications.common.BaseTherapyDetailsContentContainer', {
  scrollable: 'vertical',

  data: null, //TherapyDayDto.java
  dialogZIndex: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildContentContainer();
  },

  _buildContentContainer: function()
  {
    var therapy = this.getTherapy();

    if (therapy.isOrderTypeComplex())
    {
      this._addComplexTherapyOrderRows()
    }
    else if (therapy.isOrderTypeOxygen())
    {
      this._addOxygenTherapyOrderRows();
    }
    else
    {
      this._addSimpleTherapyOrderRows();
    }
    this._addCommonRows();

    this._addHistoryContentRow();

    this._addLegendContentRow();
  },

  _addCommonRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();

    //dosing interval
    this._addDosingIntervalRows();

    //route
    this._addRouteRow();

    //comment
    if (!tm.jquery.Utils.isEmpty(therapy.getComment()))
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("commentary"),
              therapy.getComment(),
              "comment"));
    }
    //indication
    if (therapy.getClinicalIndication())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("indication"),
              therapy.getClinicalIndication().name,
              "indication"));
    }
    //therapy start
    if (this.getData().originalTherapyStart)
    {
      var startTimeValue = view.getDisplayableValue(new Date(this.getData().originalTherapyStart), "short.date.time");
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("from"),
              startTimeValue,
              "start-time"));
    }
    //therapy end
    if (therapy.getEnd())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("until.low.case"),
              view.getDisplayableValue(therapy.getEnd(), "short.date.time"),
              "end-time"));
    }

    //consecutive day
    if (this.getConsecutiveDay())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("therapy.day"),
              this.getConsecutiveDay(),
              "consecutive-day"));
    }

    //prescriber
    if (therapy.getPrescriberName())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("prescribed.by"),
              therapy.getPrescriberName(),
              "prescriber-name"));
    }

    //composer
    if (therapy.getComposerName() !== therapy.getPrescriberName())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("composed.by"),
              therapy.getComposerName(),
              "composer-name"));
    }

    //timestamp
    var timeDataString = view.getDisplayableValue(therapy.getCreatedTimestamp(), "short.date.time");
    this._contentContainer.add(
        this._buildLabelDataRowContainer(
            view.getDictionary("when"),
            timeDataString,
            "when"));
  },

  _addSimpleTherapyOrderRows: function()
  {
    var self = this;
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = tm.views.medications.MedicationUtils;

    if (tm.jquery.Utils.isEmpty(therapy.getTimedDoseElements()))
    {
      if (!tm.jquery.Utils.isEmpty(therapy.getQuantityDisplay()))
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(
                view.getDictionary("dose"),
                utils.getFormattedDecimalNumber(therapy.getQuantityDisplay()),
                "dose"));
      }
    }
    else
    {
      if (tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(therapy.getTimedDoseElements()))
      {
        var protocolButtonHandler = function()
        {
          self._showVariableDoseProtocolContainer();
        };
        this._contentContainer.add(this._createLabelButtonRow(view.getDictionary("dose"),
            view.getDictionary("protocol"), protocolButtonHandler, "protocol", "show-variable-dose-protocol"));
      }
      else
      {
        this._addTimedDoseElementsRows();
      }
    }
    //dose form name
    if (therapy.getDoseForm())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("dose.form"),
              therapy.getDoseForm().name),
          "dose-form");
    }
  },

  _addComplexTherapyOrderRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = tm.views.medications.MedicationUtils;

    //heparin
    if (therapy.getAdditionalInstructionDisplay())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(" ", therapy.getAdditionalInstructionDisplay(), "instructions"));
    }

    //infusion rate
    if (tm.jquery.Utils.isEmpty(therapy.getTimedDoseElements()))
    {
      if (therapy.getSpeedDisplay())
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(
                view.getDictionary("rate"),
                utils.getFormattedDecimalNumber(therapy.getSpeedDisplay()),
                "rate"));
      }
      else if (therapy.isAdjustToFluidBalance())
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(view.getDictionary("rate"),
                view.getDictionary("adjust.to.fluid.balance.short"),
                "adjust-to-fluid"));
      }
      if (therapy.getSpeedFormulaDisplay())
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(view.getDictionary("dose"),
                utils.getFormattedDecimalNumber(therapy.getSpeedFormulaDisplay()),
                "speed"));
      }
    }
    else
    {
      this._addTimedDoseElementsRows();
    }
    //continuous infusion
    if (therapy.isContinuousInfusion())
    {
      var checkBoxIcon = this._buildCheckBoxIconHtml();
      this._contentContainer.add(
          this._buildLabelDataRowContainer(view.getDictionary("continuous.infusion"), checkBoxIcon, "continuous-infusion"));

      //recurring continuous infusion
      if (therapy.isRecurringContinuousInfusion())
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(view.getDictionary("repeat.every.24h"), checkBoxIcon, "repeat-24h"));
      }
    }
    //duration
    if (therapy.getDurationDisplay())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("duration"),
              therapy.getDurationDisplay(),
              "duration"));
    }
  },

  /**
   * @private
   */
  _addOxygenTherapyOrderRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = tm.views.medications.MedicationUtils;

    if (therapy.getMinTargetSaturation() && therapy.getMaxTargetSaturation())
    {
      var minSaturation = this._getRoundSaturationValue(therapy.getMinTargetSaturation());
      var maxSaturation = this._getRoundSaturationValue(therapy.getMaxTargetSaturation());
      var targetSaturationValue = minSaturation + '%' + ' - ' + maxSaturation + '%';

      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("target.saturation"),
              targetSaturationValue,
              "target-saturation"));
    }
    if (therapy.getSpeedDisplay())
    {
      var formattedRate = utils.getFormattedDecimalNumber(therapy.getSpeedDisplay());

      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("rate"),
              utils.getFormattedUnit(formattedRate),
              "rate"));
    }
    if (therapy.getStartingDevice())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("device"),
              therapy.getStartingDevice().getDisplayText(view),
              "device"));
    }

    var checkBoxIcon = this._buildCheckBoxIconHtml();
    if (therapy.getWhenNeeded())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(view.getDictionary("when.needed"), checkBoxIcon, "when-needed"));
    }
    if (therapy.isHumidification())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(view.getDictionary("humidification"), checkBoxIcon, "humidification"));
    }
    if (therapy.isHighFlowOxygen())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(view.getDictionary("high.flow.oxygen.therapy"), checkBoxIcon, "high-flow"));
    }
  },

  _addTimedDoseElementsRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = tm.views.medications.MedicationUtils;
    var isComplex = therapy.isOrderTypeComplex();
    var timedDoseElements = therapy.getTimedDoseElements();

    var timedDoseContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var labelColumn = new tm.jquery.Container({
      cls: "TextLabel row-label",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: view.getDictionary("dose")
    });
    timedDoseContainer.add(labelColumn);

    var timedDoseElementsContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    timedDoseElements.map(function(timedDoseElement)
    {
      var timeDoseElementRow = new tm.jquery.Container({
        cls: "TextData",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
      });

      var list = [];
      isComplex ? list.push(timedDoseElement.intervalDisplay) : list.push(timedDoseElement.timeDisplay);
      isComplex ? list.push(utils.getFormattedDecimalNumber(timedDoseElement.speedDisplay)) :
          list.push(utils.getFormattedDecimalNumber(timedDoseElement.quantityDisplay));
      if (isComplex && timedDoseElement.speedFormulaDisplay)
      {
        list.push(utils.getFormattedDecimalNumber(timedDoseElement.speedFormulaDisplay));
      }
      var timedDoseElementString = list.map(function(item)
      {
        return '<span class="timed-dose-element-column">' + item + '</span>'
      }).join("");
      timeDoseElementRow.setHtml(timedDoseElementString);

      timedDoseElementsContainer.add(timeDoseElementRow);
    });
    timedDoseContainer.add(timedDoseElementsContainer);

    this._contentContainer.add(timedDoseContainer);
  },

  _showVariableDoseProtocolContainer: function()
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var therapy = this.getTherapy();

    var protocolSummaryContainer = new app.views.medications.common.ProtocolSummaryContainer({
      view: view,
      timedDoseElements: therapy.getTimedDoseElements(),
      unit: therapy.quantityUnit
    });

    var protocolSummaryDialog = appFactory.createDefaultDialog(
        view.getDictionary("protocol"),
        null,
        protocolSummaryContainer,
        null,
        950,
        850
    );
    protocolSummaryDialog.setZIndex(this.dialogZIndex + 10);
    protocolSummaryDialog.setHideOnEscape(true);
    protocolSummaryDialog.setHideOnDocumentClick(true);
    protocolSummaryDialog.addTestAttribute("variable-dose-protocol-summary-dialog");
    protocolSummaryDialog.show();
  },

  _addDosingIntervalRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var frequencyDisplay = therapy.getFrequencyDisplay();

    if (!tm.jquery.Utils.isEmpty(frequencyDisplay))
    {
      var dosingIntervalLabel = view.getDictionary("dosing.interval");
      var dosingIntervals = [];
      dosingIntervals.push(frequencyDisplay);
      if (therapy.getDaysOfWeekDisplay())
      {
        dosingIntervals.push(therapy.getDaysOfWeekDisplay());
      }
      if (therapy.getDaysFrequencyDisplay())
      {
        dosingIntervals.push(therapy.getDaysFrequencyDisplay().toLowerCase());
      }
      if (therapy.getWhenNeeded())
      {
        dosingIntervals.push(view.getDictionary("when.needed"));
      }
      if (therapy.getStartCriterionDisplay())
      {
        dosingIntervals.push(therapy.getStartCriterionDisplay());
      }
      if (therapy.getApplicationPreconditionDisplay())
      {
        dosingIntervals.push(therapy.getApplicationPreconditionDisplay());
      }
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              dosingIntervalLabel,
              dosingIntervals.join(" - "),
              "dosing-interval"));

      if (!tm.jquery.Utils.isEmpty(therapy.getMaxDailyFrequency()))
      {
        var maxDosingIntervalLabel = view.getDictionary("dosing.max.24h");
        var dosingInterval = therapy.getMaxDailyFrequency();
        this._contentContainer.add(this._buildLabelDataRowContainer(maxDosingIntervalLabel, dosingInterval, "max-daily"));
      }
    }
    this._addDosingTimesRow();
  },

  _addDosingTimesRow: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var enums = app.views.medications.TherapyEnums;
    if (therapy.getDosingFrequency() && therapy.getDosingFrequency().type === enums.dosingFrequencyTypeEnum.ONCE_THEN_EX &&
        therapy.getDoseTimes() && therapy.getDoseTimes().length)
    {
      var doseTimes;
      if (therapy.getDosingFrequency().type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        var frequencyKey = tm.views.medications.MedicationTimingUtils.getFrequencyKey(therapy.getDosingFrequency());
        doseTimes = tm.views.medications.MedicationTimingUtils.getPatternForFrequencyBetweenHours(therapy.getDoseTimes()[0],
            frequencyKey);
      }
      else
      {
        doseTimes = therapy.getDoseTimes();
      }
      var labelValue = view.getDictionary("administration.time");
      var doseTime = this._getDosingTimes(doseTimes);
      this._contentContainer.add(this._buildLabelDataRowContainer(labelValue, doseTime, "dose-times"));
    }
  },

  _getDosingTimes: function(doseTimes)
  {
    return doseTimes.map(function(doseTime)
    {
      return tm.views.medications.MedicationTimingUtils.hourMinuteToString(doseTime.hour, doseTime.minute);
    }).join(" ");
  },

  _addRouteRow: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var routes = therapy.getRoutes();
    var routeNames = routes.map(function(route)
    {
      return route.name;
    }).join(", ");

    if (!tm.jquery.Utils.isEmpty(routeNames))
    {
      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("route"), routeNames, "route"));
    }
  },

  _buildTherapyWarningsContainer: function(therapyContainer)
  {
    var therapy = this.getTherapy();
    var warnings = therapy.getCriticalWarnings();
    if (warnings.length)
    {
      var warningDescription = '<div class="therapy-details-warning">';
      for (var i = 0; i < warnings.length; i++)
      {
        warningDescription += '<span class="icon_warning"/>' + " " + warnings[i] + '</span>';
        warningDescription += '<br>';
      }
      warningDescription += '</div>';

      var therapyWarningsContainer = new tm.jquery.Container({
        cls: "therapy-warnings-container",
        html: warningDescription,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });
      therapyContainer.add(therapyWarningsContainer);
    }
  },

  _createWarningIcon: function(cls)
  {
    return new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      cls: "row-icon " + cls,
      width: 16,
      height: 16
    });
  },

  _createLabelButtonRow: function(labelValue, buttonValue, buttonHandler, cls, testAttributeName)
  {
    var contentContainerRow = new tm.jquery.Container({
      cls: cls ? null : "link-row-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0)
    });

    var rowLabel = new tm.jquery.Container({
      cls: "TextLabel row-label",
      html: labelValue,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    contentContainerRow.add(rowLabel);

    var rowButton = new tm.jquery.Button({
      type: "link",
      cls: cls ? cls : null,
      text: buttonValue,
      handler: buttonHandler,
      testAttribute: testAttributeName
    });
    contentContainerRow.add(rowButton);

    return contentContainerRow;
  },

  _buildTherapyConflictIcons: function(medicationData)
  {
    var therapy = this.getTherapy();
    var warningsIconRow = new tm.jquery.Container({
      cls: "warnings-icon-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
    });
    if (!tm.jquery.Utils.isEmpty(therapy.getBnfMaximumPercentage()))
    {
      var bnfWarningsCls =
          tm.views.medications.warning.WarningsHelpers.getImageClsForBnfMaximumPercentage(therapy.getBnfMaximumPercentage());
      warningsIconRow.add(this._createWarningIcon(bnfWarningsCls));
    }
    if (medicationData.isControlledDrug())
    {
      warningsIconRow.add(this._createWarningIcon("controlled-drug-icon"));
    }
    if (this.getView().isFormularyFilterEnabled() && !medicationData.isFormulary())
    {
      warningsIconRow.add(this._createWarningIcon("non-formulary-icon"));
    }
    if (medicationData.isBlackTriangleMedication())
    {
      warningsIconRow.add(this._createWarningIcon("black-triangle-icon"));
    }
    if (medicationData.isUnlicensedMedication())
    {
      warningsIconRow.add(this._createWarningIcon("unlicensed-medication-icon"));
    }
    if (medicationData.isHighAlertMedication())
    {
      warningsIconRow.add(this._createWarningIcon("high-alert-icon"));
    }
    if (medicationData.isClinicalTrialMedication())
    {
      warningsIconRow.add(this._createWarningIcon("trial-medicine-icon"));
    }
    if (medicationData.isExpensiveDrug())
    {
      warningsIconRow.add(this._createWarningIcon("expensive-drug-icon"));
    }
    return warningsIconRow;
  },

  _addLegendContentRow: function()
  {
    var therapy = this.getTherapy();

    if (this._isMedicationConflicted() || !tm.jquery.Utils.isEmpty(therapy.getBnfMaximumPercentage()) ||
        this._hasTherapyTaskActionReminder() || this._hasTherapyDisplayedIconStatus())
    {
      var detailsLegendContainer = new app.views.medications.common.TherapyDetailsLegendContainer({
        therapy: therapy,
        view: this.getView(),
        data: this.getData(),
        medicationData: this.getMedicationData(),
        displayProvider: this.getDisplayProvider()
      });

      this._contentContainer.add(detailsLegendContainer);
    }
  },

  _createAuditTrailDialog: function()
  {
    var therapy = this.getTherapy();
    var appFactory = this.getView().getAppFactory();
    var self = this;
    this.getView().getRestApi().loadAuditTrailData(therapy).then(function(auditTrailData)
    {
      var auditTrailContainer = new app.views.medications.common.auditTrail.AuditTrailContainer({
        view: self.getView(),
        auditTrailData: auditTrailData
      });
      var auditTrailDialog = appFactory.createDataEntryDialog(
          self.getView().getDictionary("audit.trail"),
          null,
          auditTrailContainer,
          function(resultData)
          {
            //nothing changes
          },
          850,
          $(window).height() - 100);
      auditTrailDialog.setContainmentElement(self.getView().getDom());
      var footer = auditTrailDialog.getFooter();

      footer.getConfirmButton().setText(self.getView().getDictionary("close"));
      footer.getRightButtons().remove(footer.getCancelButton());
      auditTrailDialog.setHideOnDocumentClick(true);
      auditTrailDialog.show();
    });
  },

  /**
   * @param {number} saturationValue
   * @returns {number}
   * @private
   */
  _getRoundSaturationValue: function(saturationValue)
  {
    return Math.round(saturationValue * 100);
  },

  /**
   * @returns {boolean}
   * @private
   */
  _isMedicationConflicted: function()
  {
    var view = this.getView();
    var medicationDataList = this.getMedicationData();

    return medicationDataList.some(function(medicationData)
    {
      return medicationData.isControlledDrug() ||
          view.isFormularyFilterEnabled() && !medicationData.isFormulary() ||
          medicationData.isBlackTriangleMedication() || medicationData.isUnlicensedMedication() ||
          medicationData.isHighAlertMedication() || medicationData.isClinicalTrialMedication();
    });
  },

  _addHistoryContentRow: function()
  {
    var self = this;
    var view = this.getView();

    this._contentContainer.add(this._createLabelButtonRow(view.getDictionary("history"),
        view.getDictionary("audit.trail"), historyButtonHandler, "view-history-details"));

    function historyButtonHandler()
    {
      self._createAuditTrailDialog();
      tm.jquery.ComponentUtils.hideAllTooltips();
    }
  },
  /**
   *
   * @returns {boolean}
   * @private
   */
  _hasTherapyTaskActionReminder: function()
  {
    var view = this.getView();
    var data = this.getData();
    var enums = app.views.medications.TherapyEnums;

    return tm.jquery.Utils.isArray(data.tasks) && data.tasks.some(function(task)
    {
      return task.taskType === enums.taskTypeEnum.DOCTOR_REVIEW && view.getTherapyAuthority().isShowPharmacistReviewStatus()||
          task.taskType === enums.taskTypeEnum.SWITCH_TO_ORAL ||
          task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_START ||
          task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_COMPLETE ||
          task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_DISPENSE ||
          task.taskType === enums.taskTypeEnum.SUPPLY_REMINDER ||
          task.taskType === enums.taskTypeEnum.SUPPLY_REVIEW
    });
  },

  /**
   * @returns {boolean}
   * @private
   */
  _hasTherapyDisplayedIconStatus: function()
  {
    var therapy = this.getTherapy();
    var data = this.getData();
    var view = this.getView();
    var displayProvider = this.getDisplayProvider();
    return displayProvider.getStatusIcon(data) ||
        displayProvider.getPharmacistReviewIcon(this.getTherapyPharmacistReviewStatus()) &&
        view.getTherapyAuthority().isShowPharmacistReviewStatus() ||
        displayProvider.getSelfAdminStatusIcon(therapy.selfAdministeringActionEnum, this.getTherapyStatus()) ||
        (data.modifiedFromLastReview || data.completed === false || therapy.completed === false) ||
        (therapy !== null && therapy.getLinkName()) || data.showConsecutiveDay ||
        (!tm.jquery.Utils.isEmpty(therapy.criticalWarnings) && therapy.criticalWarnings.length > 0)
  },

  _buildCheckBoxIconHtml: function()
  {
    return "<span class='checkbox-on-icon'></span>";
  },

  /**
   * Getters & Setters
   */

  getData: function()
  {
    return this.data;
  },

  getTherapyPharmacistReviewStatus: function()
  {
    return this.getData().therapyPharmacistReviewStatus;
  },

  getTherapyStatus: function()
  {
    return this.getData().therapyStatus;
  },

  getConsecutiveDay: function()
  {
    return this.getData().consecutiveDay;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  setDialogZIndex: function(dialogZIndex)
  {
    this.dialogZIndex = dialogZIndex;
  }
});