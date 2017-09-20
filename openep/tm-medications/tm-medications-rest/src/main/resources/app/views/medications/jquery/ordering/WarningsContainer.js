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

Class.define('app.views.medications.ordering.WarningsContainer', 'tm.jquery.Container', {
  cls: "warnings-container",

  /** configs */
  view: null,
  getPatientMedsForWarningsFunction: null,
  medicationRuleUtils: null,
  /** privates: components */
  header: null,
  warningsCounterContainer: null,
  severityFilterButton: null,
  list: null,
  overriddenWarnings: null,

  severityLowButton: null,
  severitySignificantButton: null,
  severityHighButton: null,

  _loadingWarningsContainer: null,
  _loadingWarnings: false,
  _loadingMedicationRule: false,
  _destroying: false,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.medicationRuleUtils = this.getConfigValue("medicationRuleUtils",
        new tm.views.medications.MedicationRuleUtils({view: this.view}));
    this.overriddenWarnings = [];
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var appFactory = this.view.getAppFactory();

    this.warningsCounterContainer = new tm.jquery.Container({
      cls: "warnings-counter",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      tooltip: tm.views.medications.warning.WarningsHelpers.createWarningsLegendTooltip(this.view),
      html: tm.views.medications.warning.WarningsHelpers.getSeverityWarningsCounterDisplayValue(null)
    });

    this.severityFilterButton = appFactory.createMultiSelectBoxSplitButton({
      cls: "severity-filter-button",
      showSelectedItemsText: false,
      popupMenuHorizontalAlignment: "right",
      alignSelf: "center"
    });

    this.severityHighButton = new tm.jquery.CheckBoxMenuItem({
      cls: "severity-filter-menu-item",
      iconCls: "severity icon-high",
      text: tm.views.medications.warning.WarningsHelpers.getWarningSeverityString(this.view, enums.warningSeverityEnum.HIGH),
      checked: true,
      handler: null,
      data: enums.warningSeverityEnum.HIGH
    });

    this.severitySignificantButton = new tm.jquery.CheckBoxMenuItem({
      cls: "severity-filter-menu-item",
      iconCls: "severity icon-significant",
      text: tm.views.medications.warning.WarningsHelpers.getWarningSeverityString(this.view, enums.warningSeverityEnum.SIGNIFICANT),
      checked: false,
      handler: null,
      data: enums.warningSeverityEnum.SIGNIFICANT
    });

    this.severityLowButton = new tm.jquery.CheckBoxMenuItem({
      cls: "severity-filter-menu-item",
      iconCls: "severity icon-low",
      text: tm.views.medications.warning.WarningsHelpers.getWarningSeverityString(this.view, enums.warningSeverityEnum.LOW),
      checked: false,
      handler: null,
      data: enums.warningSeverityEnum.LOW
    });

    this.severityFilterButton.addCheckBoxMenuItem(this.severityHighButton);
    this.severityFilterButton.addCheckBoxMenuItem(this.severitySignificantButton);
    this.severityFilterButton.addCheckBoxMenuItem(this.severityLowButton);

    this.severityFilterButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent, elementEvent)
    {
      self._handleSeverityFilterValueChanged(componentEvent.eventData.changes.menuItems[0]);
      self.refreshWarnings();
    });
    var warningsHeaderContentContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 10),
      scrollable: "visible"
    });

    warningsHeaderContentContainer.add(this.warningsCounterContainer);
    warningsHeaderContentContainer.add(this.severityFilterButton);

    this.header = new app.views.medications.ordering.MedicationsTitleHeader({
      title: this.view.getDictionary('warnings'),
      view: this.view,
      scrollable: "visible",
      additionalDataContainer: warningsHeaderContentContainer
    });

    this.list = new tm.jquery.List({
      cls: "warnings-list",
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      autoLoad: false,
      dataSource: [],
      itemTpl: function(index, item)
      {
        return new app.views.medications.ordering.WarningsContainer.WarningRow({
          view: self.view,
          cellvalue: item,
          findOverriddenWarningFunction: function(cellvalue)
          {
            return self._findOverriddenWarning(cellvalue)
          },
          handleOverrideReasonEnteredFunction: function(commentField, cellvalue)
          {
            return self._handleOverrideReasonEntered(commentField, cellvalue);
          }
        });
      },
      selectable: false
    });

    this._loadingWarningsContainer = new tm.jquery.Container({
      cls: 'loading-warnings-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      hidden: true
    });

    var loadingWarningsImg = new tm.jquery.Container({
      cls: 'loader'
    });

    var loadingWarningsText = new tm.jquery.Container({
      cls: 'TextData',
      html: this.view.getDictionary('loading.warnings')
    });

    this._loadingWarningsContainer.add(loadingWarningsImg);
    this._loadingWarningsContainer.add(loadingWarningsText);
  },

  _buildGui: function()
  {
    this.add(this.header);
    this.add(this._loadingWarningsContainer);
    this.add(this.list);
  },

  _handleOverrideReasonEntered: function(commentField, cellvalue)
  {
    var self = this;
    var overriddenWarning = self._findOverriddenWarning(cellvalue);
    if (overriddenWarning)
    {
      if(commentField.getValue() == "")
      {
        commentField.setValue(overriddenWarning.overrideReason);
      }
      else
      {
        overriddenWarning.overrideReason = commentField.getValue();
      }
    }
    else
    {
      self.overriddenWarnings.push({warning: cellvalue, overrideReason: commentField.getValue()});
    }
  },

  _findOverriddenWarning: function(cellvalue)
  {
    for (var i = 0; i < this.overriddenWarnings.length; i++)
    {
      var overriddenWarning = this.overriddenWarnings[i];
      if (cellvalue.description == overriddenWarning.warning.description)
      {
        return overriddenWarning;
      }
    }
    return null;
  },

  _handleSeverityFilterValueChanged: function(changedMenuItem)
  {
    var enums = app.views.medications.TherapyEnums;

    if (changedMenuItem.data == enums.warningSeverityEnum.HIGH)
    {
      this.severityFilterButton.setSelections([this.severityHighButton], true);
    }
    else if (changedMenuItem.data == enums.warningSeverityEnum.SIGNIFICANT)
    {
      this.severityFilterButton.setSelections([this.severityHighButton, this.severitySignificantButton], true);
    }
    else if (changedMenuItem.data == enums.warningSeverityEnum.LOW)
    {
      this.severityFilterButton.setSelections([this.severityHighButton, this.severitySignificantButton, this.severityLowButton], true);
    }
  },

  _getSeverityFilterValues: function()
  {
    var selections = [];
    for (var i = 0; i < this.severityFilterButton.getSelections().length; i++)
    {
      var selection = this.severityFilterButton.getSelections().get(i);
      selections.push(selection.data);
    }
    return selections;
  },

  _handleLoadingContainerVisibility: function()
  {
    if (!this.isDataLoading())
    {
      this.isRendered() ? this._loadingWarningsContainer.hide() : this._loadingWarningsContainer.setHidden(true);
    }
    else
    {
      this.isRendered() ? this._loadingWarningsContainer.show() : this._loadingWarningsContainer.setHidden(false);
    }
  },

  /** public methods */
  refreshWarnings: function()
  {
    var self = this;
    this._loadingWarnings = true;
    this._handleLoadingContainerVisibility();
    tm.views.medications.warning.WarningsHelpers.loadMedicationWarnings(
        this.view,
        this.getPatientMedsForWarningsFunction(),
        this._getSeverityFilterValues(),
        function(warningsDto)
        {
          if (!self._destroying)
          {
            var enums = app.views.medications.TherapyEnums;
            self._refreshOverriddenWarnings(warningsDto.warnings);

            self.warningsCounterContainer.setHtml(
                tm.views.medications.warning.WarningsHelpers.getSeverityWarningsCounterDisplayValue(warningsDto));

            var newList = warningsDto.warnings.slice();
            var bnfWarnings = self.getAdditionalWarnings(enums.additionalWarningType.BNF);
            if (!bnfWarnings.isEmpty())
            {
              newList.unshift(bnfWarnings[0]);
            }

            var paracetamolWarnings = self.getAdditionalWarnings(enums.additionalWarningType.PARACETAMOL);
            if (!paracetamolWarnings.isEmpty())
            {
              newList = paracetamolWarnings.concat(newList);
            }

            self._loadingWarnings = false;
            self._handleLoadingContainerVisibility();
            self.list.setListData(newList);
          }
        },
        {taskName: 'REFRESH_MEDICATIONS' }
    );
  },

  refreshParacetamolLimitWarning: function(therapies, basketTherapies, includeBasketTherapies)
  {
    var self = this;
    this.removeParacetamolWarning();
    this._loadingMedicationRule = true;

    var allTherapies = therapies;

    if (therapies && basketTherapies && includeBasketTherapies)
    {
      allTherapies = allTherapies.concat(basketTherapies);
    }

    self.medicationRuleUtils.getParacetamolRuleForTherapies(
        this.view,
        allTherapies,
        this.view.getPatientData(),
        this.view.getReferenceWeight()).then(
        function validationSuccessHandler(medicationRuleResult)
        {
          if (!self._destroying)
          {
            self._loadingMedicationRule = false;
            self._handleLoadingContainerVisibility();
            self._refreshParacetamolDailyLimitWarning(medicationRuleResult);
          }
        });
  },

  addBnfWarning: function (percentage, medicationIds)
  {
    var description = '<strong> Cumulative </strong>' + " dose is " + '<strong>' + percentage + "% " + '</strong>'
        + "of antipsychotic" + '<strong>' + " BNF maximum" + '</strong>';

    var medications = [];
    medicationIds.forEach(function (item)
    {
      medications.push({id: item});
    });

    var warning =
    {
      description: description,
      severity: app.views.medications.TherapyEnums.warningSeverityEnum.HIGH,
      type: "BNF",
      bnf: true,
      medications: medications
    };

    this.removeAdditionalWarning(app.views.medications.TherapyEnums.additionalWarningType.BNF);
    var newList = this.list.getListData().slice();
    newList.unshift(warning);
    this.list.setListData(newList);
  },

  _refreshParacetamolDailyLimitWarning: function(ingredientRule) // MedicationIngredientRule.java
  {
    this.removeParacetamolWarning();
    if (!tm.jquery.Utils.isEmpty(ingredientRule) && ingredientRule.quantityOk === false && tm.jquery.Utils.isEmpty(ingredientRule.errorMessage))
    {
      var enums = app.views.medications.TherapyEnums;

      var medicationIds = [];
      var description = "";

      if (!tm.jquery.Utils.isEmpty(ingredientRule.medications))
      {
        ingredientRule.medications.forEach(function(medication)
        {
          medicationIds.push({id: medication.id});
          description += medication.name + " - ";
        });
      }

      var percentage = ingredientRule.underageRulePercentage >= ingredientRule.adultRulePercentage
          ? ingredientRule.underageRulePercentage
          : ingredientRule.adultRulePercentage;

      description += tm.jquery.Utils.formatMessage(
          this.view.getDictionary("paracetamol.max.daily.limit.percentage"),
          [percentage]);

      var warning =
      {
        description: description,
        severity: app.views.medications.TherapyEnums.warningSeverityEnum.HIGH,
        type: enums.additionalWarningType.PARACETAMOL,
        medications: medicationIds
      };

      var newList = this.list.getListData().slice();
      newList.unshift(warning);
      this.list.setListData(newList);
    }
  },

  removeParacetamolWarning: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var newDataList = [];

    this.list.getListData().forEach(function(warning)
    {
      if (warning.type != enums.additionalWarningType.PARACETAMOL)
      {
        newDataList.push(warning);
      }
    });
    this.list.setListData(newDataList);
  },

  getAdditionalWarnings: function (additionalWarningTypeEnum)
  {
    var additionalWarnings = [];
    if (!tm.jquery.Utils.isEmpty(additionalWarningTypeEnum))
    {
      this.list.getListData().forEach(function(warning)
      {
        if (warning.type === additionalWarningTypeEnum)
        {
          additionalWarnings.push(warning);
        }
      });
    }
    return additionalWarnings;
  },

  removeAdditionalWarning: function (additionalWarningType)
  {
    var newDataList = [];
    this.list.getListData().forEach(function (warning)
    {
      if (warning.type != additionalWarningType)
      {
        newDataList.push(warning);
      }
    });
    this.list.setListData(newDataList);
  },

  //removes old overridden warnings if no longer needed
  _refreshOverriddenWarnings: function(warnings)
  {
    var newOverriddenWarnings = [];
    for (var i = 0; i < this.overriddenWarnings.length; i++)
    {
      var overriddenWarning = this.overriddenWarnings[i];
      for (var j = 0; j < warnings.length; j++)
      {
        var warning = warnings[j];
        if (warning.description == overriddenWarning.warning.description)
        {
          newOverriddenWarnings.push(overriddenWarning);
        }
      }
    }
    this.overriddenWarnings = [];
    this.overriddenWarnings.push.apply(this.overriddenWarnings, newOverriddenWarnings);
  },

  clear: function()
  {
    this.list.clearListData();
    this._refreshOverriddenWarnings([]);
    this.warningsCounterContainer.setHtml(
        tm.views.medications.warning.WarningsHelpers.getSeverityWarningsCounterDisplayValue(
            {"highSeverityWarningsCount":0,"significantSeverityWarningsCount":0,"lowSeverityWarningsCount":0,"noSeverityWarningsCount":0,"warnings":[]}
        ));
  },

  getOverriddenWarnings: function()
  {
    return this.overriddenWarnings;
  },

  assertAllCriticalWarningsOverridden: function()
  {
    var enums = app.views.medications.TherapyEnums;

    var allCriticalWarningsOverridden = true;
    for (var i = 0; i < this.list.getDataSource().length; i++)
    {
      var singleWarningsRow = this.list.getItemTemplateByRowIndex(i);
      if (singleWarningsRow.getWarningSeverity()  == enums.warningSeverityEnum.HIGH)
      {
        if (tm.jquery.Utils.isEmpty(singleWarningsRow.getCommentFieldValue()) ||
            singleWarningsRow.getCommentFieldValue().trim() == "")
        {
          var singleWarningsRowCls = singleWarningsRow.getCls();
          singleWarningsRow.setCls(singleWarningsRowCls + " form-field-validationError");
          allCriticalWarningsOverridden = false;
        }
      }
    }
    return allCriticalWarningsOverridden;
  },

  isDataLoading: function()
  {
    return this._loadingWarnings || this._loadingMedicationRule;
  },

  destroy: function()
  {
    this._destroying = true;
    this.callSuper();
  }
});

Class.define('app.views.medications.ordering.WarningsContainer.WarningRow', 'tm.jquery.Container', {
  cls: "warnings-container-warning-row",
  /** configs */
  view: null,
  cellvalue: null,
  findOverriddenWarningFunction: null,
  handleOverrideReasonEnteredFunction: null,

  /** privates: components */
  commentField: null,

  Constructor: function(config)
  {

    this.callSuper(config);
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0));
    this._buildGui();
  },

  _buildGui: function()             // [ MedicationsWarningDto.java ]
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;

    var warningRowContainer = new tm.jquery.Container({
      cls: 'warning-row-container',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var warningContainer = new tm.jquery.Container({
      cls: "TextData",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      html: tm.views.medications.warning.WarningsHelpers.getFormattedWarningDescription(this.view, this.cellvalue),
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var leftIconsContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start")
    });
    var rightIconsContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "flex-end"),
      margin: '0 0 0 10'
    });
    this.add(leftIconsContainer);

    var severityIconContainer = tm.views.medications.warning.WarningsHelpers.createTypeAndSeverityIconContainer(
        this.view,
        this.cellvalue.type,
        this.cellvalue.severity
    );
    var monographContainer = tm.views.medications.warning.WarningsHelpers.createMonographContainer(this.view, this.cellvalue);
    if (severityIconContainer)
    {
      warningContainer.setPadding('0 0 0 5');
      leftIconsContainer.add(severityIconContainer);
    }
    warningRowContainer.add(warningContainer);
    this.add(warningRowContainer);

    if (this.cellvalue.severity == enums.warningSeverityEnum.HIGH)
    {
      var commentContainer = new tm.jquery.Container({
        cls: "comment-container",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
        margin: "0 0 0 5",
        width: '100%'
      });
      var commentLabel = new tm.views.medications.MedicationUtils.crateLabel(
          "comment-label TextDataBold",
          this.view.getDictionary("override.reason")
      );
      this.commentField = new tm.jquery.TextField({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        cls: "field-flat"
      });
      this.commentField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
      {
        if(self.commentField.getValue() != "")
        {
          var singleWarningsRowCls = self.getCls();
          self.setCls(singleWarningsRowCls.replace(" form-field-validationError", ""));
        }
        self.handleOverrideReasonEnteredFunction(self.commentField, self.cellvalue);
      });

      commentContainer.add(commentLabel);
      commentContainer.add(this.commentField);
      warningRowContainer.add(commentContainer);

      //repaint rows from old overridden warnings
      var oldOverriddenWarning = this.findOverriddenWarningFunction(this.cellvalue);
      if (oldOverriddenWarning)
      {
        this.commentField.setValue(oldOverriddenWarning.overrideReason);
      }
    }

    if (monographContainer)
    {
      rightIconsContainer.add(monographContainer);
    }
    this.add(rightIconsContainer);
  },

  getCommentFieldValue: function()
  {
    return this.commentField.getValue();
  },
  getWarningSeverity: function()
  {
    return this.cellvalue.severity;
  }

});

