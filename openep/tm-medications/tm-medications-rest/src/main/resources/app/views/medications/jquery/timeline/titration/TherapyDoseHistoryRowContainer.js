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
Class.define('app.views.medications.timeline.titration.TherapyDoseHistoryRowContainer', 'tm.jquery.Container', {
  cls: "therapy-dose-history-row-container",

  displayProvider: null,
  view: null,
  titrationTherapy: null,
  startInterval: null,
  endInterval: null,
  isPrimary: false,
  enableAdministration: false,

  _chartHelpers: null,
  _therapyBolusLegendRow: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._chartHelpers = new app.views.medications.timeline.titration.ChartHelpers({
      view: this.getView()
    });
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;
    var titrationTherapy = this.getTitrationTherapy();
    var view = this.getView();
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    var wrapperMarkerLine = new tm.jquery.Container({
      cls: "wrapper-marker-line",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "5px")
    });

    var therapyDescription = new tm.jquery.Container({
      cls: "therapy-description-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "400px")
    });

    var therapyContainer = new app.views.medications.common.TherapyContainer({
      view: view,
      data: titrationTherapy,
      displayProvider: this.getDisplayProvider(),
      showIconTooltip: false
    });
    therapyDescription.add(therapyContainer);

    if (titrationTherapy.getUnit())
    {
      var utils = tm.views.medications.MedicationUtils;
      var therapyDoseLegendRow = new tm.jquery.Container({
        cls: "therapy-legend-row",
        html: '<div class="square">' + '</div>' + " " + '<span class="TextData">' +
        utils.getFormattedUnit(titrationTherapy.getUnit()) + '</span>'
      });
     therapyDescription.add(therapyDoseLegendRow);
    }

    this._therapyBolusLegendRow = new tm.jquery.Container({
      cls: "therapy-legend-row",
      hidden: !this._hasBolusQuantity(),
      html: '<div class="circle">' + '</div>' + " " + '<span class="TextData">' +
      view.getDictionary("bolus") + '</span>'
    });
    therapyDescription.add(this._therapyBolusLegendRow);

    var chartContainer = new tm.jquery.Container({
      cls: "dose-history-chart",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    chartContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      self._chartHelpers.createChart(component.getDom(), 150, self.getIsPrimary()).then(function(highChart)
      {
        self._applyChartData(highChart);
        self._highChart = highChart;
      });
    });

    this.add(wrapperMarkerLine);
    this.add(therapyDescription);
    this.add(chartContainer);
  },

  _handleBolusLegendRowVisibility: function()
  {
    this._hasBolusQuantity() ? this._therapyBolusLegendRow.show() : this._therapyBolusLegendRow.hide();
  },

  /**
   * @param {Object} chart
   * @private
   */
  _applyChartData: function(chart)
  {
    var titrationTherapy = this.getTitrationTherapy();

    var continuousInfusion = titrationTherapy.getTherapy().isContinuousInfusion();
    var isDoseTypeWithRate = titrationTherapy.getTherapy().isDoseTypeWithRate();
    var startInterval = this.getStartInterval();
    var endInterval = this.getEndInterval();

    var startIntervalUtc = tm.views.medications.MedicationTimingUtils.getUtcTime(startInterval);
    var endIntervalUtc = tm.views.medications.MedicationTimingUtils.getUtcTime(endInterval);

    chart.xAxis[0].update({
      max: endIntervalUtc,
      min: startIntervalUtc
    });

    chart.xAxis[0].plotLinesAndBands.map(function(plotLine)
    {
      return plotLine.id;
    }).forEach(function(id)
    {
      chart.xAxis[0].removePlotLine(id);
    });

    this._chartHelpers.getYAxisDayLines(startInterval, endInterval, false).forEach(function(plotLine)
    {
      chart.xAxis[0].addPlotLine(plotLine);
    });

    if (titrationTherapy)
    {
      var view = this.getView();
      var administrations = titrationTherapy.getAdministrations();
      var infusionFormulaAtIntervalStart = this.getTitrationTherapy().getInfusionFormulaAtIntervalStart();
      var currentTime = CurrentTime.get();
      var currentTimeUtc = tm.views.medications.MedicationTimingUtils.getUtcTime(currentTime);

      var charSeries = {
        stickyTracking: false,
        name: view.getDictionary("doses"),
        lineColor: '#339900',
        type: isDoseTypeWithRate ? 'line' : 'scatter',
        step: isDoseTypeWithRate ? 'left' : null,
        dataLabels: {
          enabled: false,
          zIndex: 7,
          allowOverlap: true,
          color: '#646464',
          style: {
            textShadow: false,
            fontSize: 14
          },
          formatter: this._chartHelpers.getYAxisDataLabelFormatter()
        }
      };

      var previousQuantity = infusionFormulaAtIntervalStart;
      administrations.forEach(function(element)
      {
        if (element.getQuantity() === null && element.isBolusAdministration())
        {
          element.setQuantity(previousQuantity);
        }
        else if (element.getQuantity() !== null)
        {
          previousQuantity = element.getQuantity();
        }
        else if (element.getQuantity() === null && continuousInfusion)
        {
          // presume stop task
          element.setQuantity(0);
        }
      });

      charSeries.data = administrations.map(function(element, index, items)
      {
        return {
          x: element.getUtcTime(),
          y: !continuousInfusion && isDoseTypeWithRate && element.getQuantity() === null && index !== 0 ?
              items[index - 1].getQuantity() : element.getQuantity(),
          color: element.isBolusAdministration() ? '#FFA413' : '#339900',
          marker: {
            enabled: true,
            radius: element.isBolusAdministration() ? 7 : 5,
            symbol: element.isBolusAdministration() ? 'circle' : 'square'
          },
          dataLabels: {
            enabled: false
          },
          element: element,
          hasAdministrations: administrations.length > 0,
          isContinuousInfusion: continuousInfusion,
          quantityUnit: titrationTherapy.getUnit()
        };
      });
      if (infusionFormulaAtIntervalStart)
      {
        charSeries.data.unshift({
              x: startIntervalUtc,
              y: infusionFormulaAtIntervalStart,
              marker: {
                enabled: false
              },
              dataLabels: {
                enabled: true
              }
            }
        );
      }
      if (continuousInfusion)
      {
        var therapyEndUtc = titrationTherapy.getTherapy().getEnd() ?
            tm.views.medications.MedicationTimingUtils.getUtcTime(titrationTherapy.getTherapy().getEnd()) :
            null;
        var timesArray = [endIntervalUtc, currentTimeUtc];
        if (therapyEndUtc)
        {
          timesArray.push(therapyEndUtc);
        }
        charSeries.data.push({
          x: minTime(timesArray),
          y: administrations.length ? administrations[administrations.length - 1].getQuantity() :
              infusionFormulaAtIntervalStart,
          marker: {
            enabled: false
          }
        });
      }

      if (chart.series.length > 0)
      {
        chart.series[0].setData(charSeries.data);
      }
      else
      {
        chart.addSeries(charSeries);
      }
    }
    function minTime(times)
    {
      return Math.min.apply(null, times);
    }
  },

  /**
   * @returns {boolean}
   * @private
   */
  _hasBolusQuantity: function()
  {
    var administrations = this.getTitrationTherapy().getAdministrations();

    return administrations.some(function(administration)
    {
      return !tm.jquery.Utils.isEmpty(administration.getBolusQuantity())
    });
  },

  /**
   * Getters & Setters
   */

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.timeline.titration.dto.TherapyForTitration}
   */
  getTitrationTherapy: function()
  {
    return this.titrationTherapy;
  },

  /**
   * @returns {app.views.medications.TherapyDisplayProvider}
   */
  getDisplayProvider: function()
  {
    return this.displayProvider;
  },

  /**
   * @returns {Date}
   */
  getStartInterval: function()
  {
    return this.startInterval;
  },

  /**
   * @returns {Date}
   */
  getEndInterval: function()
  {
    return this.endInterval;
  },

  /**
   * @returns {Boolean}
   */
  getIsPrimary: function()
  {
    return this.isPrimary;
  },

  /**
   * @param {app.views.medications.timeline.titration.dto.TherapyForTitration} titrationTherapy
   * @param {Date} startInterval
   * @param {Date} endInterval
   */
  updateData: function(titrationTherapy, startInterval, endInterval)
  {
    this.titrationTherapy = titrationTherapy;
    this.startInterval = startInterval;
    this.endInterval = endInterval;

    if (this._highChart)
    {
      this._applyChartData(this._highChart);
      this._handleBolusLegendRowVisibility();
    }
  }
});