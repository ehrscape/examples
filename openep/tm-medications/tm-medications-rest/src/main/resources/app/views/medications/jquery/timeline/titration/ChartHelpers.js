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
Class.define('app.views.medications.timeline.titration.ChartHelpers', 'tm.jquery.Object', {
  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {Element} renderToElement
   * @param {Number} chartHeight
   * @param {Boolean} isChartPrimary
   * @returns {tm.jquery.Promise}
   */
  createChart: function(renderToElement, chartHeight, isChartPrimary)
  {
    var chartTheme = this.getTheme();
    var deferred = tm.jquery.Deferred.create();

    tm.jquery.ExternalsUtils.createHighchart({
          chart: {
            renderTo: renderToElement,
            backgroundColor: chartTheme.background,
            animation: false,
            spacingTop: 0,
            spacingLeft: 0,
            spacingRight: 0,
            spacingBottom: 0,
            height: chartHeight,
            width: 525,
            events: {
              load: function(event)
              {
                deferred.resolve(event.currentTarget);
              }
            }
          },
          title: {
            text: null
          },
          xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: {
              hour: '%H'
            },
            gridLineWidth: 1,
            tickInterval: 4 * 3600 * 1000,
            lineColor: chartTheme.xAxisGridLineColor,
            tickLength: 0,
            labels: {
              enabled: isChartPrimary,
              y: 12,
              style: {
                color: chartTheme.xAxisLabelsColor,
                fontSize: '10px'
              },
              formatter: this.getXAxisLabelFormatter()
            }
          },
          yAxis: {
            min: 0,
            offset: 24,
            tickAmount: 5,
            showLastLabel: true,
            type: 'linear',
            title: {
              text: null
            },
            gridLineWidth: 0,
            labels: {
              enabled: true,
              allowDecimals: false,
              align: 'left',
              x: 3,
              y: -2,
              style: {
                color: chartTheme.yAxisLabelsColor,
                fontSize: '10px'
              }
            }
          },
          legend: {
            enabled: false
          },
          tooltip: {
            enabled: true,
            shadow: true,
            backgroundColor: chartTheme.tooltipBackgroundColor,
            followPointer: false,
            shared: false,
            borderColor: chartTheme.tooltipBackgroundColor,
            borderRadius: 2,
            borderWidth: 0,
            formatter: this.getYAxisTooltipFormatter(),
            hideDelay: 20,
            snap: 0,
            style: {
              width: 450
            }
          },
          plotOptions: {
            series: {
              states: {
                hover: {
                  enabled: false
                }
              }
            }
          }
        }
    );

    return deferred.promise();
  },

  /**
   * @param {Date} intervalStart
   * @param {Date} intervalEnd
   * @param {Boolean} showLabels
   * @returns {Array}
   */
  getYAxisDayLines: function(intervalStart, intervalEnd, showLabels)
  {
    var fromDate = new Date(intervalStart.getTime());
    var toDate = new Date(intervalStart.getTime());
    toDate.setHours(0, 0, 0, 0);
    fromDate.setHours(0, 0, 0, 0);

    var timeSpan = [];

    do {
      toDate.setDate(fromDate.getDate() + 1);
      timeSpan.push(this.getYAxisDayLine(fromDate, toDate, showLabels));
      fromDate.setDate(fromDate.getDate() + 1);
    }
    while (toDate < intervalEnd);

    return timeSpan;
  },

  /**
   * @param {Date} fromDate
   * @param {Date} toDate
   * @param {Boolean} showLabels
   * @returns {{borderWidth: number, borderColor: string, from: (*|number), to: (*|number), id: string}}
   */
  getYAxisDayLine: function(fromDate, toDate, showLabels)
  {
    var fromUtc = tm.views.medications.MedicationTimingUtils.getUtcTime(fromDate);
    var toUtc = tm.views.medications.MedicationTimingUtils.getUtcTime(toDate);

    var lineConfig = {
      borderWidth: 2,
      borderColor: '#D3D3D3',
      from: fromUtc,
      to: toUtc,
      id: fromUtc.toString()
    };

    if (showLabels)
    {
      var dateLabel = tm.views.medications.MedicationTimingUtils.getFriendlyDateDisplayableValue(fromDate, this.getView());
      lineConfig.label = {
        text: dateLabel,
        zIndex: -10,
        verticalAlign: 'bottom',
        align: 'left',
        x: 2,
        y: -5,
        style: {
          color: '#848998',
          fontSize: '13px'
        }
      }
    }

    return lineConfig;
  },

  /**
   * @returns {{background: string, xAxisGridLineColor: string, yAxisGridLineColor: string, xAxisLabelsColor: string, xAxisStackLabelsColor: string, yAxisLabelsColor: string, yAxisStackLabelsColor: string, columnBorderColor: string, zeroLineColor: string, currentTimeLineColor: string, cumulativeLineColor: string, maxInputLineColor: string, maxInputLabelColor: string, maxOutputLineColor: string, maxOutputLabelColor: string, balanceBoxLabelColor: string, balanceBoxFillColor: string, balanceBoxStrokeColor: string}}
   */
  getTheme: function()
  {
    return {
      background: '#ffffff',
      xAxisGridLineColor: '#dcdcdc',
      yAxisGridLineColor: '#dcdcdc',
      xAxisLabelsColor: "#61646D",
      xAxisStackLabelsColor: "#000",
      yAxisLabelsColor: "#61646D",
      yAxisStackLabelsColor: '#576c80',
      columnBorderColor: "#fff",
      zeroLineColor: '#b1b1b1',
      currentTimeLineColor: "#be1e2d",
      cumulativeLineColor: '#61646D',
      maxInputLineColor: '#fcd9dd',
      maxInputLabelColor: '#be1e2d',
      maxOutputLineColor: '#fcd9dd',
      maxOutputLabelColor: '#be1e2d',
      balanceBoxLabelColor: '#61646D',
      balanceBoxFillColor: '#eeeeee',
      balanceBoxStrokeColor: '#888',
      tooltipBackgroundColor: '#F7F7F7'
    };
  },

  /**
   * @returns {Function}
   */
  getXAxisLabelFormatter: function()
  {
    return function()
    {
      var hour = Highcharts.dateFormat('%H', this.value);
      return hour == 0 ? '24' : +hour;
    }
  },

  /**
   * @returns {Function}
   */
  getYAxisDataLabelFormatter: function()
  {
    return function()
    {
      var format = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2};
      return tm.views.medications.MedicationUtils.safeFormatNumber(this.y, format);
    }
  },

  /**
   * @returns {Function}
   */
  getYAxisTooltipFormatter: function()
  {
    var self = this;
    return function()
    {
      var view = self.getView();
      var element = this.point.element;
      if (element)
      {
        var quantityLabel;
        var quantity = element.getQuantity();
        var quantityUnit = this.point.quantityUnit;

        if (element.isBolusAdministration())
        {
          quantityLabel = view.getDictionary("dose");
          quantity = element.getBolusQuantity();
          quantityUnit = element.getBolusUnit();
        }
        else if (this.point.hasMeasurementResults)
        {
          quantityLabel = view.getDictionary("measurement");
        }
        else if (this.point.hasAdministrations && !this.point.isContinuousInfusion)
        {
          quantityLabel = view.getDictionary("dose");
        }
        else if (this.point.hasAdministrations && this.point.isContinuousInfusion)
        {
          quantityLabel = view.getDictionary("rate");
        }
        var format = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2};
        var tooltipContent = '<span style="color: #848998; text-transform: uppercase;">' + quantityLabel +
            " " + '</span>' + '<span style="color: #646464">' +
            tm.views.medications.MedicationUtils.safeFormatNumber(quantity, format) + " " + quantityUnit + '</span>';

        if (element.hasComment())
        {
          tooltipContent += '<br>' + '<span style="color: #848998; text-transform: uppercase;">' +
              view.getDictionary("commentary") + " " + '</span>' + '<span style="color: #646464">' + element.getComment() +
              '</span>';
        }
        return tooltipContent;
      }
      return false;
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});