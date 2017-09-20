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
Class.define('app.views.medications.timeline.titration.MeasurementResultRowContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_NAVIGATION_CLICK: new tm.jquery.event.EventType({
      name: 'measurementResultNavigationClick', delegateName: null
    })
  },
  cls: "measurement-result-row-container",

  displayProvider: null,
  view: null,

  titrationData: null,
  titrationType: null,

  _highChart: null,
  _chartHelpers: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.registerEventTypes('app.views.medications.timeline.titration.MeasurementResultRowContainer', [
      {eventType: app.views.medications.timeline.titration.MeasurementResultRowContainer.EVENT_TYPE_NAVIGATION_CLICK}
    ]);

    this._chartHelpers = new app.views.medications.timeline.titration.ChartHelpers({
      view: this.getView()
    });
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;
    var titrationData = this.getTitrationData();

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    var iconContainer = new tm.jquery.Container({
      alignSelf: "flex-start",
      width: 48,
      height: 48,
      cls: "icon-container " + this._getTitrationTypeIcon(titrationData.getTitrationType()),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var therapyTitrationDescription = '';

    if (titrationData.getTitrationType())
    {
      therapyTitrationDescription += '<p class="TextDataBold">' +
          this.getView().getDictionary("TitrationType." + titrationData.getTitrationType()) + '</p>';
    }

    if (titrationData.getUnit())
    {
      therapyTitrationDescription += '<div class="circle">' + '</div>' + " " + '<span class="TextData">'
          + titrationData.getUnit() + '</span>';
    }

    // TODO uncomment once we know where the data will come from
    // if (titrationData.getNormalRangeMax() === null)
    // {
    //   therapyTitrationDescription += '<p class="warning-text">' +
    //       this.getView().getDictionary("reference.values.for.titration.not.set") + '</p>';
    // }

    var titrationDescription = new tm.jquery.Container({
      alignSelf: "flex-start",
      html: therapyTitrationDescription,
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto")
    });

    var measurementContainer = new tm.jquery.Container({
      cls: 'measurement-description-container',
      view: this.getView(),
      displayProvider: this.getDisplayProvider(),
      showIconTooltip: false,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "405px"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)
    });
    measurementContainer.add(iconContainer);
    measurementContainer.add(titrationDescription);

    var chartContainer = new tm.jquery.Container({
      cls: "measurement-results-chart",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    chartContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      self._chartHelpers.createChart(component.getDom(), 200, false).then(function(highChart)
      {
        self._applyChartData(highChart);
        self._createNavigationButtons(highChart);
        self._highChart = highChart;
      });
    });

    this.add(measurementContainer);
    this.add(chartContainer);
  },

  /**
   * @param {Chart} highChart
   * @private
   */
  _createNavigationButtons: function(highChart)
  {
    var imgSize = {width: 24, height: 24};
    var self = this;
    var yAxisPosition = (highChart.chartHeight - imgSize.height) / 2;
    var appFactory = this.getView().getAppFactory();
    var rightArrowImg = appFactory.createResourceModuleImageIconPath("/arrowRight_24.png");
    var leftArrowImg = appFactory.createResourceModuleImageIconPath("/arrowLeft_24.png");

    createArrowImage(leftArrowImg,
        'left',
        imgSize.width + 20,
        yAxisPosition,
        function clickHandler()
        {
          self._onNavigateLeftClick();
        });
    createArrowImage(rightArrowImg,
        'right',
        highChart.chartWidth - imgSize.width - 20,
        yAxisPosition,
        function clickHandler()
        {
          self._onNavigateRightClick();
        });

    function createArrowImage(img, className, x, y, clickFn)
    {
      var imageElement = highChart.renderer.image(img, x, y, imgSize.width, imgSize.height);
      if (clickFn)
      {
        imageElement.on('click', clickFn);
      }
      imageElement.element.classList.add("navigation");
      imageElement.element.classList.add(className);
      imageElement.attr({zIndex: 15});
      imageElement.add();
    }
  },

  _onNavigateLeftClick: function()
  {
    this._fireNavigationEvent(true);
  },

  _onNavigateRightClick: function()
  {
    this._fireNavigationEvent(false);
  },

  /**
   * @param {Boolean} moveBack
   * @private
   */
  _fireNavigationEvent: function(moveBack)
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.timeline.titration.MeasurementResultRowContainer.EVENT_TYPE_NAVIGATION_CLICK,
      eventData: {moveBack: moveBack, moveForward: !moveBack}
    }), null);
  },

  /**
   * @param {Object} chart
   * @private
   */
  _applyChartData: function(chart)
  {
    var titrationData = this.getTitrationData();
    var startInterval = titrationData.getStartInterval();
    var endInterval = titrationData.getEndInterval();
    var startIntervalUtc = tm.views.medications.MedicationTimingUtils.getUtcTime(startInterval);
    var endIntervalUtc = tm.views.medications.MedicationTimingUtils.getUtcTime(endInterval);

    var normalRangeMax = titrationData.getNormalRangeMax();
    var normalRangeMin = titrationData.getNormalRangeMin();
    var normalExtremeRangeDiffMax = (normalRangeMax / 100) * 20;
    var normalExtremeRangeDiffMin = (normalRangeMin / 100) * 20;
    var extremeRangeMax = normalRangeMax != null ? normalRangeMax + ((normalRangeMax / 100) * 20) : null;
    var extremeRangeMin = normalRangeMin != null ? normalRangeMin - ((normalRangeMin / 100) * 20) : 0;

    chart.xAxis[0].update({
      min: startIntervalUtc,
      max: endIntervalUtc
    });

    chart.yAxis[0].update({
      min: extremeRangeMin,
      max: extremeRangeMax
    });

    chart.xAxis[0].plotLinesAndBands.map(function(plotBand)
    {
      return plotBand.id;
    }).forEach(function(id)
    {
      chart.xAxis[0].removePlotBand(id);
    });

    this._chartHelpers.getYAxisDayLines(startInterval, endInterval, true).forEach(function(plotBand)
    {
      chart.xAxis[0].addPlotBand(plotBand);
    });

    if (normalRangeMax != null)
    {
      chart.yAxis[0].plotLinesAndBands.map(function(plotLine)
      {
        return plotLine.id;
      }).forEach(function(id)
      {
        chart.yAxis[0].removePlotLine(id);
      });

      this.getMeasurementNormalRangeLines().forEach(function(plotLine)
      {
        chart.yAxis[0].addPlotLine(plotLine);
      });
    }

    var measurementResults = this.getTitrationData().getResults();
    var maxQuantity = measurementResults.reduce(function(maxQuantity, measurementResult)
    {
      var resultQuantity = measurementResult.getQuantity();
      if (resultQuantity > maxQuantity)
      {
        maxQuantity = resultQuantity;
      }
      return maxQuantity;
    }, normalRangeMax);

    var minQuantity = measurementResults.reduce(function(minQuantity, measurementResult)
    {
      var resultQuantity = measurementResult.getQuantity();
      if (resultQuantity < minQuantity)
      {
        minQuantity = resultQuantity;
      }
      return minQuantity;
    }, normalRangeMin);

    if (measurementResults)
    {
      var charSeries = {
        stickyTracking: false,
        type: 'scatter',
        color: '#cc3300',
        marker: {
          radius: 5
        },
        dataLabels: {
          enabled: false
        }
      };
      charSeries.data = measurementResults.map(function(element)
      {
        var y = element.getQuantity();

        if (normalRangeMax != null && y > normalRangeMax)
        {
          y = normalRangeMax + (normalExtremeRangeDiffMax / (maxQuantity - normalRangeMax)) * (y - normalRangeMax);
        }
        else if (normalRangeMin != null && y < normalRangeMin)
        {
          y = normalRangeMin - (normalExtremeRangeDiffMin / (minQuantity + normalRangeMin)) * (y + normalRangeMin);
        }
        return {
          x: element.getUtcTime(),
          y: y,
          data: element.getQuantity(),
          element: element,
          hasMeasurementResults: measurementResults.length > 0,
          quantityUnit: titrationData.getUnit()
        };
      });

      if (chart.series.length > 0)
      {
        chart.series[0].setData(charSeries.data);
      }
      else
      {
        chart.addSeries(charSeries);
      }
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.timeline.titration.dto.Titration}
   */
  getTitrationData: function()
  {
    return this.titrationData;
  },

  /**
   * @returns {app.views.medications.TherapyDisplayProvider}
   */
  getDisplayProvider: function()
  {
    return this.displayProvider;
  },
  /**
   * @returns {String}
   */
  getTitrationType: function()
  {
    return this.titrationType;
  },

  /**
   * @returns {Array<{id: string|null, color: string, value: number, width: String, zIndex: number}>}
   */
  getMeasurementNormalRangeLines: function()
  {
    var normalRangeMax = this.getTitrationData().getNormalRangeMax();
    var normalRangeMin = this.getTitrationData().getNormalRangeMin();
    var secondPlotLineMax = normalRangeMax + (normalRangeMax / 100) * 2;
    var secondPlotLineMin = normalRangeMin - (normalRangeMin / 100) * 2;

    return [this.getMeasurementNormalRangeLine(normalRangeMax),
      this.getMeasurementNormalRangeLine(normalRangeMin),
      this.getMeasurementNormalRangeLine(secondPlotLineMax),
      this.getMeasurementNormalRangeLine(secondPlotLineMin)];
  },

  /**
   * @param {number} value
   * @returns {{id: string|null, color: string, value: number, width: String, zIndex: number}}
   */
  getMeasurementNormalRangeLine: function(value)
  {
    var normalRangeMax = this.getTitrationData().getNormalRangeMax();

    return {
      id: normalRangeMax != null ? normalRangeMax.toString() : null,
      color: '#cc3300',
      value: value,
      width: '1',
      zIndex: 2
    };
  },

  /**
   * @param {String} titrationType of type app.views.medications.TherapyEnums.therapyTitrationTypeEnum
   * @returns {String}
   * @private
   */
  _getTitrationTypeIcon: function(titrationType)
  {
    var enums = app.views.medications.TherapyEnums.therapyTitrationTypeEnum;
    if (titrationType === enums.BLOOD_SUGAR)
    {
      return "icon_blood_sugar";
    }
    else if (titrationType === enums.INR)
    {
      return "icon_inr";
    }

    return '';
  },

  /**
   * @param {app.views.medications.timeline.titration.dto.Titration} titrationData
   */
  setTitrationData: function(titrationData)
  {
    this.titrationData = titrationData;

    if (this._highChart)
    {
      this._applyChartData(this._highChart);
    }
  }
});
