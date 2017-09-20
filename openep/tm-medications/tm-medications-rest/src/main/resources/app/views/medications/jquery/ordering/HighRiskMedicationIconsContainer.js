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

Class.define('app.views.medications.ordering.HighRiskMedicationIconsContainer', 'tm.jquery.Container', {
  cls: 'high-alert-medication-icons-container',

  /** configs */
  view: null,

  /** privates: components */
  _blackTriangleIconContainer: null,
  _trialMedicationIconContainer: null,
  _controlledDrugIconContainer: null,
  _highAlertIconContainer: null,
  _nonFormularyIconContainer: null,
  _unlicensedMedicationIconContainer: null,
  _expensiveDrugIconContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /** private methods */
  _buildGui: function()
  {
    var utils = tm.views.medications.MedicationUtils;
    var view = this.getView();

    this.setLayout(tm.jquery.HFlexboxLayout.create("center", "center", 0));

    this._blackTriangleIconContainer = this._createIconContainer("high-risk-icon black-triangle-icon",
        view.getDictionary("black.triangle.medication"));

    this._trialMedicationIconContainer = this._createIconContainer("high-risk-icon trial-medicine-icon",
        view.getDictionary("clinical.trial.medication"));

    this._controlledDrugIconContainer = this._createIconContainer("high-risk-icon controlled-drug-icon",
        view.getDictionary("controlled.drug"));

    this._highAlertIconContainer = this._createIconContainer("high-risk-icon high-alert-icon",
        view.getDictionary("high.alert.medication"));

    this._unlicensedMedicationIconContainer = this._createIconContainer("high-risk-icon unlicensed-medication-icon",
        view.getDictionary("unlicensed.medication"));

    this._nonFormularyIconContainer = this._createIconContainer("high-risk-icon non-formulary-icon",
        view.getDictionary("non.formulary.medication"));

    this._expensiveDrugIconContainer = this._createIconContainer("high-risk-icon expensive-drug-icon",
        view.getDictionary("expensive.drug"));

    this.add(this._blackTriangleIconContainer);
    this.add(this._trialMedicationIconContainer);
    this.add(this._controlledDrugIconContainer);
    this.add(this._highAlertIconContainer);
    this.add(this._unlicensedMedicationIconContainer);
    this.add(this._nonFormularyIconContainer);
    this.add(this._expensiveDrugIconContainer);
  },

  /**
   * @param {string} cls
   * @param {string} tooltipString
   * @returns {tm.jquery.Container}
   * @private
   */
  _createIconContainer: function(cls, tooltipString)
  {
    var utils = tm.views.medications.MedicationUtils;
    var view = this.getView();

    return new tm.jquery.Container({
      cls: cls,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("center", "center", 0),
      tooltip: utils.createTooltip(tooltipString, "bottom", view),
      hidden: true
    });
  },


  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  presentHighAlertIcons: function(medicationData)
  {
    if (!medicationData) return;
    
    if (medicationData.isControlledDrug())
    {
      this._controlledDrugIconContainer.show();
    }
    if (medicationData.isClinicalTrialMedication())
    {
      this._trialMedicationIconContainer.show();
    }
    if (medicationData.isUnlicensedMedication())
    {
      this._unlicensedMedicationIconContainer.show();
    }
    if (medicationData.isHighAlertMedication())
    {
      this._highAlertIconContainer.show();
    }
    if (medicationData.isBlackTriangleMedication())
    {
      this._blackTriangleIconContainer.show();
    }
    if (this.getView().isFormularyFilterEnabled() && !medicationData.isFormulary())
    {
      this._nonFormularyIconContainer.show();
    }
    if (medicationData.isExpensiveDrug())
    {
      this._expensiveDrugIconContainer.show();
    }
  },

  clear: function()
  {
    this._controlledDrugIconContainer.hide();
    this._trialMedicationIconContainer.hide();
    this._unlicensedMedicationIconContainer.hide();
    this._highAlertIconContainer.hide();
    this._blackTriangleIconContainer.hide();
    this._nonFormularyIconContainer.hide();
    this._expensiveDrugIconContainer.hide();
  },

  /**
   * @returns {tm.jquery.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});