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

Class.define('tm.views.medications.warning.SimpleWarningsContainer', 'tm.jquery.Container', {
  cls: "simple-warnings-container",
  /** configs */
  view: null,
  /** privates */
  /** privates: components */
  list: null,
  warningsMessageContainer: null,
  loadingWarningsImg: null,
  warningsMessageLabel: null,
  containsAdditionalWarnings: false,
  setWarningsConditionTask: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  _buildComponents: function()
  {
    var self = this;
    this.list = new tm.jquery.List({
      cls: "warnings-list",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      autoLoad: false,
      dataSource: [],
      scrollable: "visible",
      itemTpl: function(index, item)
      {
        return self._buildRow(item);
      },
      selectable: false
    });
    this.warningsMessageContainer = new tm.jquery.Container({
      cls: 'warnings-message-container',
      height: 30,
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start', 0)
    });
    this.warningsMessageLabel = new tm.jquery.Label();
    this.loadingWarningsImg = new tm.jquery.Container({
      cls: 'loader',
      hidden: true
    });
  },

  _buildGui: function()
  {
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('warnings')));
    this.add(this.list);
    this.warningsMessageLabel.style = 'color: #646464; text-transform: none;';
    this.warningsMessageContainer.add(this.loadingWarningsImg);
    this.warningsMessageContainer.add(this.warningsMessageLabel);
    this.add(this.warningsMessageContainer);
  },

  _buildRow: function(cellvalue)          // [ MedicationsWarningDto.java ]
  {
    var rowContainer = new tm.jquery.Container({
      cls: "warning-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 10)
    });

    var warningContainer = new tm.jquery.Container({
      cls: "TextData",
      //style: "width:100%;",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 0),
      html: tm.views.medications.warning.WarningsHelpers.getFormattedWarningDescription(this.view, cellvalue),
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var severityIconContainer = tm.views.medications.warning.WarningsHelpers.createTypeAndSeverityIconContainer(
        this.view,
        cellvalue.type,
        cellvalue.severity
    );
    var monographContainer = tm.views.medications.warning.WarningsHelpers.createMonographContainer(this.view, cellvalue);

    if (severityIconContainer)
    {
      rowContainer.add(severityIconContainer);
    }
    rowContainer.add(warningContainer);
    if (monographContainer)
    {
      rowContainer.add(monographContainer);
    }

    return rowContainer;
  },

  /**
   * Finds additional warnings from list
   * @returns {Array}
   * @private
   */
  _getAdditionalWarnings: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var additionalWarnings = [];
    this.list.getListData().forEach(function(warning)
    {
      if (enums.additionalWarningType.hasOwnProperty(warning.type))
      {
        additionalWarnings.push(warning);
      }
    });
    return additionalWarnings;
  },

  isContainsAdditionalWarnings: function()
  {
    return this.containsAdditionalWarnings === true;
  },

  _setContainsAdditionalWarnings: function(value)
  {
    this.containsAdditionalWarnings = value;
  },

  clear: function()
  {
    this.list.clearListData();
    this.list.setDataSource([]);
  },

  setWarnings: function(warnings)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    if(tm.jquery.Utils.isEmpty(this.setWarningsConditionTask) == false)
    {
      this.setWarningsConditionTask.abort();
    }

    this.setWarningsConditionTask = appFactory.createConditionTask(
        function()
        {
          self.setWarningsConditionTask = null;
          if (warnings.length == 0 && !self.isContainsAdditionalWarnings())
          {
            self.list.setListData(warnings);
            self.setWarningsMessage(false, self.view.getDictionary('no.warnings.found'));
          }
          else
          {
            self.warningsMessageContainer.hide();
            if (!self.isContainsAdditionalWarnings())
            {
              self.list.setListData(warnings);
            }
            else
            {
              var newList = self._getAdditionalWarnings().concat(warnings);
              self.list.setListData(newList);
            }
          }
        },
        function()
        {
          return self.isRendered() && self.list.isRendered();
        },
        function()
        {
          self.setWarningsConditionTask = null;
          self.setWarningsMessage(false, self.view.getDictionary('error.unexpected'));
        },
        250, 10
    );
  },

  addBnfWarning: function(percentage, medication)
  {
    if (!tm.jquery.Utils.isEmpty(percentage))
    {
      var medicationName = !tm.jquery.Utils.isEmpty(medication.getName()) ?
          medication.getName() : medication.getGenericName();

      var enums = app.views.medications.TherapyEnums;
      var description = '<strong>' + medicationName + '</strong>' + " value is " + '<strong>' + percentage + "% " +
          '</strong>' + "of antipsychotic " + '<strong>' + "BNF maximum" + '</strong>';

      var severity;
      if (percentage < 50)
      {
        severity = app.views.medications.TherapyEnums.warningSeverityEnum.LOW;
      }
      else if (percentage < 100)
      {
        severity = app.views.medications.TherapyEnums.warningSeverityEnum.SIGNIFICANT;
      }
      else
      {
        severity = app.views.medications.TherapyEnums.warningSeverityEnum.HIGH;
      }
      var warning =
      {
        description: description,
        severity: severity,
        type: enums.additionalWarningType.BNF,
        bnf: true,
        medications: [],
        medicationId: medication.getId()
      };
      this.removeBnfWarning(medication.getId());
      this.list.addRowData(warning, 0, true);
      this._setContainsAdditionalWarnings(true);
    }
  },

  /**
   * @param {Number} medicationId
   * @param {Boolean} *removeAllBnfWarnings
   */
  removeBnfWarning: function(medicationId, removeAllBnfWarnings)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var warnings = this.list.getListData();

    warnings.forEach(function(warning)
    {
      if (warning.medicationId === medicationId && warning.type === enums.additionalWarningType.BNF ||
          removeAllBnfWarnings && warning.type === enums.additionalWarningType.BNF)
      {
        self.list.removeRowData(warning, true);
      }
    });

    if (warnings.isEmpty())
    {
      this.clear();
      this._setContainsAdditionalWarnings(false);
    }
  },

  addParacetamolLimitExceededWarning: function(calculatedParacetamolRule) // MedicationIngredientRuleDto.java
  {
    if (!tm.jquery.Utils.isEmpty(calculatedParacetamolRule)
        && !tm.jquery.Utils.isEmpty(calculatedParacetamolRule.rule))
    {
      var percentage = calculatedParacetamolRule.underageRulePercentage >= calculatedParacetamolRule.adultRulePercentage
          ? calculatedParacetamolRule.underageRulePercentage
          : calculatedParacetamolRule.adultRulePercentage;

      if (!tm.jquery.Utils.isEmpty(percentage))
      {
        var enums = app.views.medications.TherapyEnums;
        var exceededPercentage = percentage - 100;
        var description =
            tm.jquery.Utils.formatMessage(
                this.view.getDictionary("paracetamol.max.daily.dose"),
                [calculatedParacetamolRule.rule, '<strong>' + exceededPercentage + "%" + '</strong>']);

        var warning =
        {
          description: description,
          severity: app.views.medications.TherapyEnums.warningSeverityEnum.HIGH,
          type: enums.additionalWarningType.PARACETAMOL,
          medications: []
        };

        this.removeAdditionalWarning(enums.additionalWarningType.PARACETAMOL);
        this.list.addRowData(warning, 0, true);
      }
    }
    this._setContainsAdditionalWarnings(true);
  },

  removeAdditionalWarning: function(additionalWarningType)
  {
    var self = this;
    var listData = this.list.getListData();

    for (var index = listData.length - 1; index >= 0; index--)
    {
      if (listData[index].type === additionalWarningType)
      {
        self.list.removeRowData(listData[index], true);
      }
    }

    if (listData.isEmpty()) this.clear();

    this._setContainsAdditionalWarnings(false);
  },

  setWarningsMessage: function(showLoadingIcon, text)
  {
    if (showLoadingIcon)
    {
      this.loadingWarningsImg.show();
    }
    else
    {
      this.loadingWarningsImg.hide();
    }
    this.warningsMessageLabel.setText(text);
    this.warningsMessageContainer.show();
  },

  /**
   * @Override
   */
  destroy: function()
  {
    if(tm.jquery.Utils.isEmpty(this.setWarningsConditionTask) == false)
    {
      this.setWarningsConditionTask.abort();
    }
    this.callSuper();
  }
});
