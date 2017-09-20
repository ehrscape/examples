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

Class.define('app.views.medications.ordering.CalculatedDosagePane', 'tm.jquery.Container', {
  cls: "calculated-dosage-pane",
  /** configs */
  view: null,
  /** privates */
  /** privates: components */

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(new tm.jquery.VFlexboxLayout({gap: 5}));
  },

  /**
   * @param {Number|{minNumerator: Number, maxNumerator: Number}} dosage
   * @param {Number} weightInKg
   * @param {Number} [dosageMultiplier=1]
   * @returns {String}
   * @private
   */
  _dosagePerWeight: function(dosage, weightInKg, dosageMultiplier)
  {
    var dm = tm.jquery.Utils.isNumeric(dosageMultiplier) ? dosageMultiplier : 1;
    
    if (dosage && (dosage.maxNumerator || dosage.minNumerator))
    {
      return tm.views.medications.MedicationUtils.doubleToString(dosage.minNumerator * dm / weightInKg, 'n2') +
          ' - ' + tm.views.medications.MedicationUtils.doubleToString(dosage.maxNumerator * dm / weightInKg, 'n2');
    }

    return tm.views.medications.MedicationUtils.doubleToString(dosage * dm / weightInKg, 'n2');
  },

  /**
   * @param {Number|{minNumerator: Number, maxNumerator: Number}} dosage
   * @param {Number} timesPerDay
   * @param {Number} weightInKg
   * @returns {String}
   * @private
   */
  _dosagePerWeightPerDay: function(dosage, timesPerDay, weightInKg)
  {
    if (dosage && (dosage.maxNumerator || dosage.minNumerator))
    {
      return tm.views.medications.MedicationUtils.doubleToString((dosage.minNumerator / weightInKg) * timesPerDay, 'n2') +
          ' - ' + tm.views.medications.MedicationUtils.doubleToString((dosage.maxNumerator / weightInKg) * timesPerDay, 'n2');
    }

    return tm.views.medications.MedicationUtils.doubleToString((dosage / weightInKg) * timesPerDay, 'n2');
  },

  /**
   * @param {Number|{minNumerator: Number, maxNumerator: Number}} dosage
   * @param {Number} bodySurfaceArea
   * @param {Number} [dosageMultiplier=1]
   * @returns {String}
   * @private
   */
  _dosagePerSurface: function(dosage, bodySurfaceArea, dosageMultiplier)
  {
    var dm = tm.jquery.Utils.isNumeric(dosageMultiplier) ? dosageMultiplier : 1;

    if (dosage && (dosage.maxNumerator || dosage.minNumerator))
    {
      return tm.views.medications.MedicationUtils.doubleToString(dosage.minNumerator * dm / bodySurfaceArea, 'n2') +
          ' - ' + tm.views.medications.MedicationUtils.doubleToString(dosage.maxNumerator * dm / bodySurfaceArea, 'n2');
    }

    return tm.views.medications.MedicationUtils.doubleToString(dosage * dm/ bodySurfaceArea, 'n2');
  },

  /**
   * @param {Number|{minNumerator: Number, maxNumerator: Number}} dosage
   * @param {Number} timesPerDay
   * @param {Number} bodySurfaceArea
   * @returns {String}
   * @private
   */
  _dosagePerSurfacePerDay: function(dosage, timesPerDay, bodySurfaceArea)
  {
    if (dosage && (dosage.maxNumerator || dosage.minNumerator))
    {
      return tm.views.medications.MedicationUtils.doubleToString((dosage.minNumerator / bodySurfaceArea) * timesPerDay, 'n2') +
          ' - ' +
          tm.views.medications.MedicationUtils.doubleToString((dosage.maxNumerator / bodySurfaceArea) * timesPerDay, 'n2');
    }

    return tm.views.medications.MedicationUtils.doubleToString((dosage / bodySurfaceArea) * timesPerDay, 'n2');
  },

  _getCalculationDisplayItem: function(text)
  {
    return {text: "<span class='TextData lowercase'>" + text + "</span>"};
  },

  /** public methods */
  calculate: function(dosage, dosageUnit, timesPerDay, weightInKg, heightInCm, perHour)
  {
    var appFactory = this.view.getAppFactory();
    var utils = tm.views.medications.MedicationUtils;

    var displayItemsList = [];
    if (dosage && dosageUnit && weightInKg)
    {
      var perUnit = perHour ? this.view.getDictionary("hour.accusative") : this.view.getDictionary("dose");

      var dosagePerWeight = this._dosagePerWeight(dosage, weightInKg);
      displayItemsList.push(
          this._getCalculationDisplayItem(utils.getFormattedDecimalNumber(dosagePerWeight) + ' ' +
              dosageUnit + '/kg/' + perUnit));

      var dosagePerWeightPerDay = null;
      if (perHour)
      {
        dosagePerWeightPerDay = this._dosagePerWeight(dosage, weightInKg, 24);
      }
      else if (timesPerDay)
      {
        dosagePerWeightPerDay = this._dosagePerWeightPerDay(dosage, timesPerDay, weightInKg);
      }
      if (dosagePerWeightPerDay)
      {
        displayItemsList.push(
            this._getCalculationDisplayItem(utils.getFormattedDecimalNumber(dosagePerWeightPerDay) + ' ' +
                dosageUnit + '/kg/' + this.view.getDictionary("day.lc")));
      }

      if (heightInCm)
      {
        var bodySurfaceArea = utils.calculateBodySurfaceArea(heightInCm, weightInKg);

        var dosagePerSurface = this._dosagePerSurface(dosage, bodySurfaceArea);
        displayItemsList.push(
            this._getCalculationDisplayItem(utils.getFormattedDecimalNumber(dosagePerSurface) + ' ' +
                dosageUnit + '/m2/' + perUnit));

        var dosagePerSurfacePerDay = null;
        if (perHour)
        {
          dosagePerSurfacePerDay = this._dosagePerSurface(dosage, bodySurfaceArea, 24);
        }
        else if (timesPerDay)
        {
          dosagePerSurfacePerDay = this._dosagePerSurfacePerDay(dosage, timesPerDay, bodySurfaceArea);
        }
        if (dosagePerSurfacePerDay)
        {
          displayItemsList.push(
              this._getCalculationDisplayItem(utils.getFormattedDecimalNumber(dosagePerSurfacePerDay) + ' ' +
                  dosageUnit + '/m2/' + this.view.getDictionary("day.lc")));
        }
      }
    }
    this.setHtml(appFactory.createInlineItemListHtml({block: true, items: displayItemsList}));
  },

  clear: function()
  {
    this.setHtml("");
  }
});
