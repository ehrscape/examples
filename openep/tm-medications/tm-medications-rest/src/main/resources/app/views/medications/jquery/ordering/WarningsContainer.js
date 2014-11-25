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
  /** privates: components */
  header: null,
  list: null,
  overriddenWarnings: null,
  //{
  //  warning: null, //MedicationsWarningDto
  //  overrideReason: null, //String
  //}

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    var appFactory = this.view.getAppFactory();
    this.overriddenWarnings = [];
    this.setLayout(appFactory.createDefaultVFlexboxLayout("start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.header = new app.views.medications.ordering.MedicationsTitleHeader({
      title: this.view.getDictionary('warnings'),
      view: this.view});

    this.list = new tm.jquery.List({
      flex: 1,
      autoLoad: false,
      dataSource: [],
      itemTpl: function(index, item)
      {
        return self._buildRow(item);
      },
      selectable: false
    });
  },

  _buildGui: function()
  {
    this.add(this.header);
    this.add(this.list);
  },

  _buildRow: function(cellvalue)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var rowContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 0})});

    var dataContainer = new tm.jquery.Container({
      html: this._createSeverityIconContainer(cellvalue) + '<div>' + this._getFormattedWarningDescription(cellvalue) + '</div>',
      flex: 1
    });

    rowContainer.add(dataContainer);
    if (cellvalue.severity == enums.warningSeverityEnum.HIGH)
    {
      var checkBox = new tm.jquery.CheckBox({labelText: "", labelAlign: "right", enabled: true, nowrap: true});

      //repaint checboxes from old overridden warnings
      var oldOverriddenWarning = self._findOverriddenWarning(cellvalue);
      if (oldOverriddenWarning)
      {
        checkBox.setChecked(true);
      }

      //override warning or edit override reason
      checkBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        checkBox.setChecked(true);
        var overriddenWarning = self._findOverriddenWarning(cellvalue);
        var warningOverrideReasonDialog = self.view.getAppFactory().createDataEntryDialog(
            self.view.getDictionary('reason'),
            null,
            new app.views.medications.ordering.WarningOverrideReasonPane({
              view: self.view,
              overrideReason: overriddenWarning != null ? overriddenWarning.overrideReason : null
            }),
            function(resultData)
            {
              if (resultData)
              {
                //var overriddenWarning = self._findOverriddenWarning(cellvalue);
                if (overriddenWarning)
                {
                  overriddenWarning.overrideReason = resultData.overrideReason;
                }
                else
                {
                  self.overriddenWarnings.push({warning: cellvalue, overrideReason: resultData.overrideReason});
                }
              }
            },
            300, 190
        );
        warningOverrideReasonDialog.show();
      });
      rowContainer.add(checkBox);
    }

    return rowContainer;
  },

  _getFormattedWarningDescription: function(warning)
  {
    var formattedWarning = warning.description;
    if (warning.primaryMedication)
    {
      formattedWarning = formattedWarning.replace(warning.primaryMedication.name, '<strong>' + warning.primaryMedication.name + '</strong>');
    }
    if (warning.secondaryMedication)
    {
      formattedWarning = formattedWarning.replace(warning.secondaryMedication.name, '<strong>' + warning.secondaryMedication.name + '</strong>');
    }
    return formattedWarning;
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

  _buildWarningsDisplay: function(warnings)
  {
    var self = this;
    var warningsDisplay = [];

    $.each(warnings, function(index0, value0)
    {
      var html = self._createSeverityIconContainer(value0);
      html += value0.description;
      warningsDisplay[index0] = ({id: index0, order: value0, html: html});
    });

    return warningsDisplay;
  },

  _createSeverityIconContainer: function(warning)
  {
    var enums = app.views.medications.TherapyEnums;
    var severityHtml = "";
    if (warning.severity == enums.warningSeverityEnum.HIGH)
    {
      severityHtml += "<div class='severity high-icon'><span>" + enums.warningSeverityEnum.HIGH + "</span></div>";
    }
    else if (warning.severity == enums.warningSeverityEnum.SIGNIFICANT)
    {
      severityHtml += "<div class='severity significant-icon'><span>" + enums.warningSeverityEnum.SIGNIFICANT + "</span></div>";
    }
    return severityHtml;
  },

  _createDetailIconId: function(detailId)
  {
    return this.view.getViewId() + "_detail_icon_" + detailId;
  },

  /** public methods */
  refreshWarnings: function()
  {
    var self = this;
    var warningsUrl =
        self.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_WARNINGS;

    var patientData = this.view.getPatientData();
    if (typeof patientData == typeof (void 0)) {
      patientData = {};
    }
    var referenceWeight = this.view.getReferenceWeight();
    var heightInCm = patientData.heightInCm ? patientData.heightInCm : 0;

    var bsaInM2 = tm.views.medications.MedicationUtils.calculateBodySurfaceArea(heightInCm, referenceWeight);

    var params = {
      patientAgeInDays: patientData.ageInDays,
      patientWeightInKg: referenceWeight ? referenceWeight : 0,
      patientAllergies: JSON.stringify(patientData.allergyIds),
      gabInWeeks: patientData.gabInWeeks ? patientData.gabInWeeks : 0,
      bsaInM2: bsaInM2 ? bsaInM2 : 0,
      isFemale: patientData.gender == "FEMALE",
      diseaseTypeCodes: JSON.stringify(patientData.diseaseTypeCodes),
      patientMedications: JSON.stringify(self.getPatientMedsForWarningsFunction())};

    this.view.loadPostViewData(warningsUrl, params, null, function(warnings)
    {
      self._refreshOverriddenWarnings(warnings);
      self.list.setListData(warnings);
    });
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
    this.list.setDataSource([]);
  },

  getOverriddenWarnings: function()
  {
    return this.overriddenWarnings;
  }
});

