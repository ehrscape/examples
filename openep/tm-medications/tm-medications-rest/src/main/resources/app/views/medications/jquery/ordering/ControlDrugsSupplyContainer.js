/**
 * Created by Nejc Korasa on 16.10.2015.
 */
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

Class.define('app.views.medications.ordering.ControlDrugsSupplyContainer', 'app.views.common.containers.AppDataEntryContainer', {

  /** configs */
  view: null,
  data: null,

  doseQuantity: null,
  numberOfDoses: null,

  supplyQuantity: null,

  calculatedLabel: null,
  resetButton: null,

  /** privates */
  validationForm: null,
  medicationRows: null,

  /** constructor */
  Constructor: function(config)
  {
    this.medicationRows = [];

    this.callSuper(config);
    this._buildGui();
    this._calculateSupplyQuantity();
  },

  _buildGui: function()
  {
    var self = this;
    this.calculatedLabel = new tm.jquery.Label({
      width: 50,
      style: "align-self: center;",
      cls: "TextData",
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      padding: "5"
    });
    self._setCalculatedLabelText(0);

    this.resetButton = new tm.jquery.Button({
      cls: "btn-reset",
      type: "link",
      text: "Revert",
      handler: function()
      {
        self._resetAll();
      }
    });

    this.data.forEach(function(item) // SupplyMedicationDto.java
    {
      var medicationRow = new app.views.medications.ordering.ControlDrugsSupplyRowContainer({
        view: self.view,
        id: item.id,
        basicUnit: item.basicUnit,
        name: item.name,
        strengthNumerator: item.strengthNumerator,
        numberOfDoses: self.numberOfDoses,
        supplyData: item,
        onItemSelectedCallback: function(container)
        {
          self._onItemChanged(container, true)
        },
        onItemDeselectedCallback: function(container)
        {
          self._onItemChanged(container, false);
        }
      });
      self.medicationRows.push(medicationRow);
      self.add(medicationRow);
    });

    self._disableNonValidElements();
  },

  _onItemChanged: function(container, selected)
  {
    if (selected)
    {
      var remainingOneDoseQuantity = this._getRemainingOneDoseQuantity();
      var oneDoseRowSupplyQuantity = Math.floor(remainingOneDoseQuantity / container.getStrengthNumerator()); // to fill remaining quantity
      var rowSupplyQuantity = container.getSupplyQuantity();

      if (tm.jquery.Utils.isEmpty(rowSupplyQuantity) || rowSupplyQuantity > oneDoseRowSupplyQuantity)
      {
        var numberOfDoses = this._getNumberOfDoses();
        container.setSupplyQuantity(oneDoseRowSupplyQuantity * numberOfDoses);
        container.displayNumberOfUnits(oneDoseRowSupplyQuantity * numberOfDoses);
      }
    }
    else
    {
      container.setSupplyQuantity(null);
      container.displayNumberOfUnits(null);
    }

    var calculatedQuantity = this._getOneDoseQuantitySum();
    this._setCalculatedLabelText(calculatedQuantity);
    this._disableNonValidElements(selected ? container.getBasicUnit() : null);
  },

  _getOneDoseQuantitySum: function()
  {
    var self = this;
    var calculatedQuantity = 0;
    this.medicationRows.forEach(function(row)
    {
      calculatedQuantity += parseInt(row.getStrengthNumerator() * (row.getSupplyQuantity() / self._getNumberOfDoses()));
    });
    return calculatedQuantity;
  },

  _disableNonValidElements: function(unit)
  {
    var self = this;
    var selectedUnit = !tm.jquery.Utils.isEmpty(unit) ? unit : self._getSelectedUnit();
    var remainingOneDoseQuantity = self._getRemainingOneDoseQuantity();
    this.medicationRows.forEach(function(row)
    {
      if (row.getStrengthNumerator() > remainingOneDoseQuantity && !row.isChecked())
      {
        row.setEnabled(false);
      }
      else if (!tm.jquery.Utils.isEmpty(selectedUnit) && row.getBasicUnit() != selectedUnit)
      {
        row.setEnabled(false);
      }
      else
      {
        row.setEnabled(true);
      }
    });
  },

  _getSelectedUnit: function()
  {
    var unit = null;
    this.medicationRows.forEach(function(row)
    {
      if (row.isChecked() === true)
      {
        unit = row.getBasicUnit();
      }
    });

    return unit;
  },

  _resetAll: function()
  {
    this.medicationRows.forEach(function(row)
    {
      row.resetData();
    });

    this._disableNonValidElements();
    this._setCalculatedLabelText(0);
  },

  _calculateSupplyQuantity: function()
  {
    this.supplyQuantity = this.numberOfDoses * this.doseQuantity;
  },

  _setCalculatedLabelText: function(currentDose)
  {
    this.calculatedLabel.setHtml(
        '<span style="text-decoration: underline;">' + currentDose + "</span>" + "<span> mg of " + this.doseQuantity + "mg </span>")
  },

  _getRemainingOneDoseQuantity: function()
  {
    return this.doseQuantity - this._getOneDoseQuantitySum();
  },

  _getNumberOfDoses: function()
  {
    return this.numberOfDoses;
  },

  _getResult: function()
  {
    var resultData = [];
    this.medicationRows.forEach(function(row)
    {
      if (row.isChecked())
      {
        var rowResult =
        {
          id: row.getId(),
          name: row.getName(),
          quantity: row.getSupplyQuantity(),
          unit: row.getBasicUnit()
        };
        resultData.push(rowResult);
      }
    });

    return resultData;
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    var result = this._getResult();
    var success = !(result.isEmpty() || parseInt(this._getRemainingOneDoseQuantity()) != 0);
    var resultData = new app.views.common.AppResultData({success: success, value: result});

    resultDataCallback(resultData);
  }
});