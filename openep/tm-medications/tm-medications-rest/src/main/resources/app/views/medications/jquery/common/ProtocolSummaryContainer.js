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

Class.define('app.views.medications.common.ProtocolSummaryContainer', 'tm.jquery.Container', {
  cls: "protocol-summary-container",
  scrollable: "both",
  /** configs */
  view: null,
  timedDoseElements: null,
  unit: null,
  lineAcross: false,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(new tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"));
    this._buildGui();
  },

  /**
   * @private
   */
  _buildGui: function()
  {
    var self = this;
    this.timedDoseElements = this.getTimedDoseElements().sort(function(a, b)
    {
      var aDateTime = new Date(a.date);
      aDateTime.setHours(a.doseTime.hour);
      aDateTime.setMinutes(a.doseTime.minute);

      var bDateTime = new Date(b.date);
      bDateTime.setHours(b.doseTime.hour);
      bDateTime.setMinutes(b.doseTime.minute);
      return aDateTime < bDateTime ? -1 : 1;
    });
    
    var times = []; //in HourMinute
    for (var i = 0; i < this.getTimedDoseElements().length; i++)
    {
      if (tm.views.medications.MedicationUtils.getIndexOfHourMinute(this.getTimedDoseElements()[i].doseTime, times) == -1)
      {
        times.push(this.getTimedDoseElements()[i].doseTime);
      }
    }

    times = times.sort(function(a, b)
    {
      var aTime = CurrentTime.get();
      aTime.setHours(a.hour);
      aTime.setMinutes(a.minute);

      var bTime = CurrentTime.get();
      bTime.setHours(b.hour);
      bTime.setMinutes(b.minute);
      return aTime < bTime ? -1 : 1;
    });

    var timesContainer = new tm.jquery.Container({
      cls: "protocol-summary-metadata",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    times.forEach(function(time)
    {
      var fullTime = CurrentTime.get();
      fullTime.setHours(time.hour);
      fullTime.setMinutes(time.minute);
      timesContainer.add(
          self._buildCell(
              self.view.getDisplayableValue(fullTime, "time.short"),
              "TextData protocol-time-cell",
              "time-index-" + times.indexOf(time) + '-0'));
    });
    this.add(timesContainer);

    var currentDate = null;
    var currentDayContainer = null;
    var rowIndex, columnIndex = 0;
    
    for (var j = 0; j < this.getTimedDoseElements().length; j++)
    {
      var element = this.getTimedDoseElements()[j];
      var elementDate = new Date(element.date);
      if (currentDate === null ||
          currentDate.getTime() !== elementDate.getTime())
      {
        currentDate = elementDate;
        if (currentDayContainer)
        {
          this.add(currentDayContainer);
        }
        currentDayContainer = new tm.jquery.Container({
          layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
        });
        currentDayContainer.add(this._buildCell(
            tm.views.medications.MedicationTimingUtils.getDateWithoutYearDisplay(this.view, currentDate),
            "TextData protocol-date-cell"));
        rowIndex = 0; // since we're adding column by column, reset row index each new column
        columnIndex++;
      }
      currentDayContainer.add(
          this._buildCell(
              element.doseElement.quantity,
              "TextData protocol-summary-cell",
              "data-index-" + rowIndex + '-' + columnIndex));
      rowIndex++;
    }
    if (currentDayContainer)
    {
      this.add(currentDayContainer);
    }

    var dosesContainer = new tm.jquery.Container({
      cls: "protocol-summary-metadata",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    times.forEach(function()
    {
      dosesContainer.add(self._buildCell(self.getUnit(), "TextData protocol-unit-cell"));
    });
    this.add(dosesContainer);
  },

  /**
   * @param {String} value
   * @param {String} cls
   * @param {String} [testAttribute=undefined]
   * @returns {tm.jquery.Container}
   * @private
   */
  _buildCell: function(value, cls, testAttribute)
  {
    return new tm.jquery.Container({
      cls: (this.isLineAcross() ? "protocol-cell-crossed " : "protocol-cell ") + cls,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      width: "60px",
      height: "30px",
      html: value,
      testAttribute: testAttribute
    })
  },

  /**
   * @returns {Array}
   */
  getTimedDoseElements: function()
  {
    return this.timedDoseElements;
  },

  /**
   * @returns {boolean}
   */
  isLineAcross: function()
  {
    return this.lineAcross;
  },

  /**
   * @returns {String}
   */
  getUnit: function()
  {
    return this.unit;
  }
});