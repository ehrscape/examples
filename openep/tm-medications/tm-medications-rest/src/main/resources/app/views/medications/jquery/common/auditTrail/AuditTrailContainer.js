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

Class.define('app.views.medications.common.auditTrail.AuditTrailContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "audit-trail-container",
  view: null,
  auditTrailData: null,
  _fixedDateContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var view = this.getView();
    var therapiesContainer = new tm.jquery.Container({
      cls: "therapies-container",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch")
    });

    var originalTherapyDescription = new app.views.medications.common.TherapyContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      data: {
        therapy: this.getAuditTrailData().getOriginalTherapy()
      },
      showIconTooltip: false
    });
    var originalTherapyContainer = new tm.views.medications.common.VerticallyTitledComponent({
      cls: "vertically-titled-component original-therapy-container",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      titleText: view.getDictionary("initial.prescription"),
      contentComponent: originalTherapyDescription
    });

    var currentTherapyDescription = new app.views.medications.common.TherapyContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      data: {
        therapy: this.getAuditTrailData().getCurrentTherapy()
      },
      showIconTooltip: false
    });

    var currentTherapyContainer = new tm.views.medications.common.VerticallyTitledComponent({
      cls: "vertically-titled-component current-therapy-container",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      titleText: view.getDictionary("current.prescription"),
      contentComponent: currentTherapyDescription
    });
    this._fixedDateContainer = this._createNewDayContainer(null);
    therapiesContainer.add(originalTherapyContainer);
    therapiesContainer.add(currentTherapyContainer);
    this.add(therapiesContainer);
    this.add(this._fixedDateContainer);
    this.add(this._createChangeEventsContainers());
  },

  /**
   * @return {tm.jquery.Container}
   * @private
   */
  _createChangeEventsContainers: function()
  {
    var self = this;
    var view = this.getView();
    var currentDay = null;
    var contentContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "vertical",
      cls: "audit-trail-content-container"
    });
    contentContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_SCROLL, function()
    {
      var $fixedDateContainer = $(self._fixedDateContainer.getDom());
      var fixedDateContainerBottom = $fixedDateContainer.offset().top + $fixedDateContainer.outerHeight();
      var actionContainers = $('.audit-trail-content-container .change-row-container');
      for (var i = 0; i < actionContainers.length; i++)
      {
        var $actionContainer = $(actionContainers.get(i));
        if ($actionContainer.offset().top + $actionContainer.outerHeight() >= fixedDateContainerBottom)
        {
          self._fixedDateContainer.setHtml(view.getDisplayableValue(
              new Date(self.auditTrailData.actionHistoryList[i].actionPerformedTime),
              "date.medium"));
          break;
        }
      }
    });
    this.getAuditTrailData().getActionHistoryList().forEach(function(action)
    {
      var actionPerformedTime = action.getActionPerformedTime();
      var actionPerformedDate = new Date(actionPerformedTime.getFullYear(),
          actionPerformedTime.getMonth(),
          actionPerformedTime.getDate());
      actionPerformedDate = new Date(actionPerformedDate.setHours(0, 0, 0, 0));
      var firstChangeInDay = false;
      if (currentDay === null)
      {
        self._fixedDateContainer.setHtml(view.getDisplayableValue(actionPerformedDate, "date.medium"));
        currentDay = actionPerformedDate;
        firstChangeInDay = true;
      }

      else if (actionPerformedDate.getTime() !== currentDay.getTime())
      {
        currentDay = actionPerformedDate;
        contentContainer.add(self._createNewDayContainer(actionPerformedTime));
        firstChangeInDay = true;
      }
      contentContainer.add(self._createActionRow(action, firstChangeInDay))
    });
    return contentContainer;
  },

  /**
   * @param {Date} date
   * @return {tm.jquery.Container}
   * @private
   */
  _createNewDayContainer: function(date)
  {
    return new tm.jquery.Container({
      cls: "date-container TextLabel title-label ellipsis",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      html: date ? this.getView().getDisplayableValue(date, "date.medium") : null
    });
  },

  /**
   * @param {Object} action
   * @param {boolean} borderless
   * @return {tm.jquery.Container}
   * @private
   */
  _createActionRow: function(action, borderless)
  {
    var view = this.getView();
    var actionRowContainer = new tm.jquery.Container({
      cls: "change-row-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var actionTimeContainer = new tm.jquery.Container({
      cls: "TextData time-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: view.getDisplayableValue(action.getActionPerformedTime(), "time.short")
    });

    actionRowContainer.add(actionTimeContainer);

    var actionRowContentContainer = this._createActionRowContentContainer(action);
    var titledContentContainerCls = borderless ? "borderless-change-content-container" : "change-content-container";

    var titledContentContainer = new tm.jquery.Container({
      cls: titledContentContainerCls,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var actionRowTitleContainer = new tm.jquery.Container({
      cls: "title-label",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
    });

    var actionHistoryTypeContainer = new tm.jquery.Container({
      cls: "TextDataBold",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: view.getDictionary("TherapyActionHistoryType." + action.getTherapyActionHistoryType())
    });
    actionRowTitleContainer.add(actionHistoryTypeContainer);

    if (action.getPerformer())
    {
      var actionPerformerContainer = new tm.jquery.Container({
        cls: "TextData",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        html: " &nbsp; &ndash; &nbsp;" + action.getPerformer()
      });
      actionRowTitleContainer.add(actionPerformerContainer);
    }


    titledContentContainer.add(actionRowTitleContainer);
    titledContentContainer.add(actionRowContentContainer);

    actionRowContainer.add(titledContentContainer);
    return actionRowContainer;
  },

  /**
   * @param {Object} action
   * @return {tm.jquery.Container}
   * @private
   */
  _createActionRowContentContainer: function(action)
  {
    var self = this;
    var view = this.getView();
    var actionChangesContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    action.getChanges().forEach(function(change)
    {
      var therapyChangeTypePrefix = "TherapyChangeType.";
      if (change.isChangeTypeDose())
      {
        if (tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(change.getOldValue()) ||
            tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(change.getNewValue()))
        {
          actionChangesContainer.add(self._createProtocolButtonContainer(
              view.getDictionary(therapyChangeTypePrefix + change.getType()),
              change.getOldValue(),
              change.getNewValue()));
        }
        else
        {
          actionChangesContainer.add(self._createVariableRateOrDoseContainer(
              view.getDictionary(therapyChangeTypePrefix + change.getType()),
              change.getOldValue(),
              change.getNewValue(),
              false));
        }
      }
      else if (change.isChangeTypeRate())
      {
        actionChangesContainer.add(self._createVariableRateOrDoseContainer(
            view.getDictionary(therapyChangeTypePrefix + change.getType()),
            change.getOldValue(),
            change.getNewValue(),
            true));
      }
      else
      {
        actionChangesContainer.add(self._createChangeRowForPair(
            view.getDictionary(therapyChangeTypePrefix + change.getType()),
            change.getOldValue(),
            change.getNewValue()));
      }
    });
    if (action.getActionTakesEffectTime())
    {
      actionChangesContainer.add(this._createChangeRowForSingle(
          view.getDictionary("change.takes.effect"),
          view.getDisplayableValue(action.getActionTakesEffectTime(), "datetime.medium")));
    }

    if (action.getChangeReason() && action.getChangeReason().getReason())
    {
      actionChangesContainer.add(
          this._createChangeRowForSingle(
              view.getDictionary("change.reason"),
              view.getDictionary(action.getChangeReason().getReason().name)));
    }
    return actionChangesContainer;
  },

  /**
   * @param {String} title
   * @return {tm.jquery.Container}
   * @private
   */
  _createActionTitleContainer: function(title)
  {
    return new tm.jquery.Container({
      cls: "TextLabel",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "160px"),
      html: title
    });
  },

  /**
   * @returns {tm.jquery.Container}
   * @private
   */
  _createArrowContainer: function()
  {
    return new tm.jquery.Container({
      cls: "arrow-icon",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      height: 20,
      width: 20
    });
  },

  /**
   * @param {String} title
   * @param {String} oldValue
   * @param {String} newValue
   * @return {tm.jquery.Container}
   * @private
   */
  _createChangeRowForPair: function(title, oldValue, newValue)
  {
    var view = this.getView();
    if (tm.jquery.Utils.isArray(oldValue))
    {
      oldValue = oldValue.join(", ")
    }
    if (tm.jquery.Utils.isArray(newValue))
    {
      newValue = newValue.join(", ")
    }
    var actionChangeRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      alignSelf: "stretch"
    });
    actionChangeRowContainer.add(this._createActionTitleContainer(title));

    var oldValueContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      cls: "value-container TextData crossed-out",
      html: oldValue && oldValue.length > 0 ? oldValue : view.getDictionary("empty")
    });
    actionChangeRowContainer.add(oldValueContainer);
    actionChangeRowContainer.add(this._createArrowContainer());

    var newValueContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      cls: "value-container TextData",
      html: newValue && newValue.length > 0 ? newValue : view.getDictionary("empty")
    });
    actionChangeRowContainer.add(newValueContainer);

    return actionChangeRowContainer;
  },

  /**
   * @param {String} title
   * @param {String} value
   * @return {tm.jquery.Container}
   * @private
   */
  _createChangeRowForSingle: function(title, value)
  {
    var actionChangeRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      alignSelf: "stretch"
    });
    actionChangeRowContainer.add(this._createActionTitleContainer(title));

    var valueContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      cls: "value-container TextData",
      html: value
    });
    actionChangeRowContainer.add(valueContainer);

    return actionChangeRowContainer;
  },

  /**
   * @param {String} title
   * @param {Array | String} oldDoseElements
   * @param {Array | String} newDoseElements
   * @param {boolean} complex
   * @returns {tm.jquery.Container}
   * @private
   */
  _createVariableRateOrDoseContainer: function(title, oldDoseElements, newDoseElements, complex)
  {
    var actionChangeRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      alignSelf: "stretch"
    });
    actionChangeRowContainer.add(this._createActionTitleContainer(title));
    var newDoseElementsContainer = this._createNewDoseElementsContainer(newDoseElements, complex);
    var oldDoseElementsContainer = this._createOldDoseElementsContainer(oldDoseElements, complex);
    actionChangeRowContainer.add(oldDoseElementsContainer);
    actionChangeRowContainer.add(this._createArrowContainer());
    actionChangeRowContainer.add(newDoseElementsContainer);

    return actionChangeRowContainer;
  },

  /**
   * @param {Array | String} oldDoseElements
   * @param {Boolean} complex
   * @returns {tm.jquery.Container}
   * @private
   */
  _createOldDoseElementsContainer: function(oldDoseElements, complex)
  {
    var oldDoseElementsContainer = new tm.jquery.Container({
      cls: "value-container TextData crossed-out",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start")
    });

    if (tm.jquery.Utils.isArray(oldDoseElements) && oldDoseElements.length > 0)
    {
      oldDoseElements.forEach(function(element)
      {
        var html;

        if (complex)
        {
          html = element.intervalDisplay + " &ensp; " + element.speedDisplay +
              (element.speedFormulaDisplay ? ' &ensp; ' + element.speedFormulaDisplay : "");
        }
        else
        {
          html = element.timeDisplay + " - " + element.quantityDisplay;
        }
        var elementContainer = new tm.jquery.Container({
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
          html: html,
          alignSelf: "stretch"
        });
        oldDoseElementsContainer.add(elementContainer);
      });
    }
    else
    {
      var oldDoseContainer = new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        html: oldDoseElements,
        alignSelf: "stretch"
      });
      oldDoseElementsContainer.add(oldDoseContainer);
    }
    return oldDoseElementsContainer;
  },

  /**
   * @param {Array | String} newDoseElements
   * @param {Boolean} complex
   * @returns {tm.jquery.Container}
   * @private
   */
  _createNewDoseElementsContainer: function(newDoseElements, complex)
  {
    var newDoseElementsContainer = new tm.jquery.Container({
      cls: "value-container TextData",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start")
    });
    if (tm.jquery.Utils.isArray(newDoseElements) && newDoseElements.length > 0)
    {
      newDoseElements.forEach(function(element)
      {
        var html;
        if (complex)
        {
          html = element.intervalDisplay + " &ensp; " + element.speedDisplay +
              (element.speedFormulaDisplay ? ' &ensp; ' + element.speedFormulaDisplay : "");
        }
        else
        {
          html = element.timeDisplay + " - " + element.quantityDisplay;
        }
        var elementContainer = new tm.jquery.Container({
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
          html: html,
          alignSelf: "stretch"
        });
        newDoseElementsContainer.add(elementContainer);
      });
    }
    else
    {
      var newDoseContainer = new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        html: newDoseElements,
        alignSelf: "stretch"
      });
      newDoseElementsContainer.add(newDoseContainer);
    }
    return newDoseElementsContainer;
  },

  /**
   * @param {String} title
   * @param {Array | String} oldDoseElements
   * @param {Array | String} newDoseElements
   * @returns {tm.jquery.Container}
   * @private
   */
  _createProtocolButtonContainer: function(title, oldDoseElements, newDoseElements)
  {
    var self = this;
    var actionChangeRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      alignSelf: "stretch"
    });
    actionChangeRowContainer.add(this._createActionTitleContainer(title));
    var protocolButton = new tm.jquery.Button({
      cls: 'protocol-btn',
      type: 'link',
      text: this.getView().getDictionary("protocol"),
      height: 20,
      handler: function()
      {
        self._createProtocolHistoryContainer(oldDoseElements, newDoseElements);
      }
    });

    if (!tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(oldDoseElements))
    {
      actionChangeRowContainer.add(this._createOldDoseElementsContainer(oldDoseElements, false));
      actionChangeRowContainer.add(this._createArrowContainer());
    }
    actionChangeRowContainer.add(protocolButton);
    if (!tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(newDoseElements))
    {
      actionChangeRowContainer.add(this._createArrowContainer());
      actionChangeRowContainer.add(this._createNewDoseElementsContainer(newDoseElements, false));
    }

    return actionChangeRowContainer;
  },

  /**
   * @param {Array} oldDoseElements
   * @param {Array} newDoseElements
   * @private
   */
  _createProtocolHistoryContainer: function(oldDoseElements, newDoseElements)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var protocolSummaryContainers = new app.views.common.containers.AppDataEntryContainer({
      cls: 'protocol-summary-containers',
      scrollable: 'both',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "start")
    });
    if (oldDoseElements && tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(oldDoseElements))
    {
      var initialTherapyProtocolSummary = new app.views.medications.common.ProtocolSummaryContainer({
        view: view,
        timedDoseElements: oldDoseElements,
        unit: this.getAuditTrailData().getOriginalTherapy().getQuantityUnit(),
        lineAcross: true
      });
      protocolSummaryContainers.add(initialTherapyProtocolSummary);
    }
    if (newDoseElements && tm.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(newDoseElements))
    {
      var currentTherapyProtocolSummary = new app.views.medications.common.ProtocolSummaryContainer({
        view: view,
        timedDoseElements: newDoseElements,
        unit: this.getAuditTrailData().getCurrentTherapy().getQuantityUnit()
      });
      protocolSummaryContainers.add(currentTherapyProtocolSummary);
    }

    var protocolSummaryDialog = appFactory.createDefaultDialog(
        view.getDictionary("variable.dose"),
        null,
        protocolSummaryContainers,
        null,
        950,
        850
    );
    protocolSummaryDialog.header.setCls("audit-trail-header");
    protocolSummaryDialog.setHideOnEscape(true);
    protocolSummaryDialog.setHideOnDocumentClick(true);
    protocolSummaryDialog.show();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.TherapyAuditTrail}
   * @private
   */
  getAuditTrailData: function()
  {
    return this.auditTrailData;
  },

  /**
   * @param {function} resultDataCallback
   */
  processResultData: function (resultDataCallback)
  {
    resultDataCallback(new app.views.common.AppResultData({success: true}));
  }
});