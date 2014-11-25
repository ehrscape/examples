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

Class.define('app.views.medications.TherapyDetailsCardContainer', 'app.views.common.containers.AppBodyContentContainer', {
  scrollable: 'vertical',
  padding: '5 5 0 10',
  view: null,
  maxWidth: 750,
  maxHeight: 400,
  similarTherapiesInterval: null,

  /**privates**/
  mainContainer: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.HFlexboxLayout.create("start", "stretch", 0));
    this.mainContainer = new tm.jquery.Container({
      cls: "tooltip-order-card-info",
      scrollable: 'visible',
      layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5)
    });

    this.add(this.mainContainer);

    this.similarTherapiesContainer = new tm.jquery.Container({
      cls: "similar-therapies-container",
      layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5),
      scrollable: 'visible'});

    this.similarTherapiesContainer.add(tm.views.medications.MedicationUtils.crateLabel('PortletHeading2', this.view.getDictionary('past.therapies'), '0 0 0 5'));

    this.similarTherapiesList = new tm.jquery.List({
      flex: 1,
      autoLoad: false,
      dataSource: [],
      scrollable: 'visible',
      itemTpl: function(index, item)
      {
        return item;
      },
      selectable: false
    });

    this.similarTherapiesContainer.add(this.similarTherapiesList);
    this.add(this.similarTherapiesContainer);
    this.similarTherapiesContainer.hide();
  },

  updateData: function(data)
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var patientId = view.getPatientId();
    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MEDICATION_ORDER_CARD_INFO_DATA;
    var params = {
      patientId: patientId,
      patientHeight: this.view.getPatientData().heightInCm,
      compositionId: data.ehrCompositionId,
      ehrOrderName: data.ehrOrderName,
      similarTherapiesInterval: JSON.stringify(this.similarTherapiesInterval)
    };

    this.mainContainer.removeAll();

    view.showLoaderMask();
    view.loadViewData(url, params, null, function(therapyOrderCardInfo)
    {
      view.hideLoaderMask();
      self.mainContainer.add(self._createTherapyOrderCardContainer(therapyOrderCardInfo, data.therapyState));
      //setTimeout(function()
      //{
      //  var therapy = therapyOrderCardInfo.currentTherapy;
      //  var isComplex = therapy.medicationOrderFormType == enums.medicationOrderFormType.COMPLEX;
      //
      //  if (isComplex)
      //  {
      //    for (var i = 0; i < therapy.ingredientsList.length; i++)
      //    {
      //      var ingredient = therapy.ingredientsList[i];
      //      var complexMedication = ingredient.medication;
      //      self._createMedicationInfo(complexMedication.id, i);
      //    }
      //  }
      //  else
      //  {
      //    var simpleMedication = therapy.medication;
      //    self._createMedicationInfo(simpleMedication.id, 0);
      //  }
      //}, 100);
      // render grid //
      self._renderChangeHistoryGrid(therapyOrderCardInfo.changeHistoryList);
      self.repaint();

      var similarTherapies = therapyOrderCardInfo.similarTherapies;

      if (similarTherapies.length > 0)
      {
        self.similarTherapiesContainer.show();
        self.similarTherapiesList.clearListData();
        for (var i = 0; i < similarTherapies.length; i++)
        {
          self.similarTherapiesList.addRowData(similarTherapies[i]);
        }
      }
      else
      {
        self.similarTherapiesContainer.hide();
      }
    });
  },

  _createMedicationInfo: function(medicationId, index)
  {
    //todo: create medication info container and tooltip whe two level tooltips enabled
    //var medicationInfoContainerId = this._createMedicationInfoContainerId(medicationId, index);
    //
    //var medicationInfoContainerElement = $("#" + medicationInfoContainerId).get(0);
    //
    //if (tm.jquery.Utils.isEmpty(medicationInfoContainerElement) == false)
    //{
    //  var container = this._getMedicationInfoContainer(medicationId);
    //  container.setRenderToElement(medicationInfoContainerElement);
    //  container.doRender();
    //}
  },

  //_getMedicationInfoContainer: function(medicationId)
  //{
  //  var self = this;
  //  var appFactory = this.view.getAppFactory();
  //
  //  var medicationDetailsPane = new app.views.medications.therapy.MedicationDetailsCardPane({view: self.view});
  //  var detailsCardTooltip = appFactory.createDefaultPopoverTooltip(
  //      self.view.getDictionary("medication"),
  //      null,
  //      medicationDetailsPane
  //  );
  //  detailsCardTooltip.setTrigger("manual");
  //  detailsCardTooltip.setAppendTo(this.getParent().getScrollableElement());
  //  detailsCardTooltip.setPlacement("leftBottom");
  //
  //  var container = new tm.jquery.Container({
  //    cls: 'info-icon pointer-cursor',
  //    width: 25,
  //    height: 30,
  //    margin: '0 0 0 5',
  //    tooltip: detailsCardTooltip
  //  });
  //  container.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
  //  {
  //    if (detailsCardTooltip.isShowed())
  //    {
  //      detailsCardTooltip.hide();
  //    }
  //    else
  //    {
  //      self._readMedicationData(medicationId, function(medicationData)
  //      {
  //        medicationDetailsPane.setMedicationData(medicationData);
  //      });
  //      detailsCardTooltip.show();
  //    }
  //  });
  //
  //  return container;
  //},

  //_readMedicationData: function(medicationId, callback)
  //{
  //  var medicationDataUrl =
  //      this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MEDICATION_DATA;
  //  var params = {medicationId: medicationId};
  //  this.view.loadViewData(medicationDataUrl, params, null, function(data)
  //  {
  //    callback(data);
  //  });
  //},

  _createMedicationInfoContainerId: function(medicationId, index)
  {
    return this.view.getViewId() + "_medication_info_container_" + medicationId + "_" + index;
  },

  _createTherapyOrderCardContainer: function(therapyOrderCardInfo, therapyState)   // [TherapyCardInfoDto.java]
  {
    var view = this.getView();
    var currentTherapy = therapyOrderCardInfo.currentTherapy;
    var originalTherapy = therapyOrderCardInfo.originalTherapy;
    var changeHistoryList = therapyOrderCardInfo.changeHistoryList;

    var haveHistoryChanges = !(tm.jquery.Utils.isEmpty(changeHistoryList) || changeHistoryList.length == 0);

    var therapyOrderCardContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 0)
    });
    if (haveHistoryChanges)
    {
      // current order //
      var currentOrderContainer = new tm.jquery.Container({
        cls: "current-order content",
        layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5),
        html: this._createTherapyOrderHtmlTemplate(currentTherapy, haveHistoryChanges, true, therapyState)
      });
      therapyOrderCardContainer.add(currentOrderContainer);
      // / current order //
      therapyOrderCardContainer.add(new tm.jquery.Container({cls: "divider"}));
    }

    var originalOrderContainer = new tm.jquery.Container({
      cls: "original-order",
      layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5)
    });
    therapyOrderCardContainer.add(originalOrderContainer);

    // original order //
    if (haveHistoryChanges)
    {
      var originalOrderHeaderContainer = new tm.jquery.Container({
        cls: "header",
        layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5),
        html: '<p class="PortletHeading2">' + view.getDictionary("basic.order") + '</p>'
      });
      originalOrderContainer.add(originalOrderHeaderContainer);
    }

    var therapyStateForCurrentOrder = haveHistoryChanges ? null : therapyState;
    var originalOrderContentContainer = new tm.jquery.Container({
      cls: "content",
      layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5),
      html: this._createTherapyOrderHtmlTemplate(originalTherapy, haveHistoryChanges, false, therapyStateForCurrentOrder)
    });
    originalOrderContainer.add(originalOrderContentContainer);
    // /original order //

    return therapyOrderCardContainer;
  },

  _createTherapyOrderHtmlTemplate: function(therapy, haveHistoryChanges, isCurrent, therapyState)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var html = '';
    html += '<table border=0 cellpadding=0 cellspacing="0">';
    html += '<tr>';

    // left //
    html += '<td valign=top>';

    if ((haveHistoryChanges && isCurrent) || haveHistoryChanges == false)
    {
      html += '<div class="' + tm.views.medications.MedicationUtils.getTherapyIcon(therapy) + '"></div>';
    }

    html += '<div class="time">';
    html += '<p class="TextHeading1">';
    html += view.getDisplayableValue(therapy.start, "short.time");
    html += '</p>';
    html += '<p class="TextDataLight">';
    html += view.getDisplayableValue(therapy.start, "short.date");
    html += '</p>';
    html += '</div>';
    html += '</td>';
    // /left //

    // detail //
    html += '<td>';
    html += '<div class="detail">';

    var isComplex = therapy.medicationOrderFormType == enums.medicationOrderFormType.COMPLEX;
    if (isComplex)
    {
      return this._createComplexTherapyOrderHtmlTemplate(html, therapy, haveHistoryChanges, isCurrent, therapyState);
    }
    else
    {
      return this._createSimpleTherapyOrderHtmlTemplate(html, therapy, haveHistoryChanges, isCurrent, therapyState);
    }
  },

  _createSimpleTherapyOrderHtmlTemplate: function(html, therapy, haveHistoryChanges, isCurrent, therapyState)
  {
    var view = this.getView();

    html += "<table><tr><td valign='top'>";
    if (tm.jquery.Utils.isEmpty(therapy.medication.genericName))
    {
      html += '<p class="TextHeading2">' + therapy.medication.name + '</p>';
    }
    else
    {
      html += '<p class="TextHeading2">' + therapy.medication.genericName + '</p>';
      html += '<p class="TextData">';
      html += '(' + therapy.medication.name + ')';
      html += '</p>';
    }

    if (tm.jquery.Utils.isEmpty(therapy.timedDoseElements))
    {
      if (tm.jquery.Utils.isEmpty(therapy.quantityDisplay) == false)
      {
        html += '<span class="TextLabel">' + view.getDictionary("dose") + ' </span>';
        html += '<span class="TextData">' + therapy.quantityDisplay + ' </span>';
      }
    }
    else
    {
      if (therapy.timedDoseElements[0].date)
      {
        html += '<span class="TextLabel">' + 'Variable dose' + ' </span>';    //TODO Mitja
      }
      else
      {
        html += this._createTimedDoseElementsTemplateHtml(therapy.timedDoseElements, false);
      }
    }
    html += '<br>';

    // dose form name //
    if (therapy.doseForm)
    {
      html += this._addLabelDataPair(view.getDictionary("dose.form"), therapy.doseForm.name);
    }

    // dosing interval //
    html += this._createDosingIntervalTemplateHtml(therapy);

    // route //
    html += this._createRouteTemplateHtml(therapy);

    // comment //
    html += this._createCommentTemplateHtml(therapy);

    // indication //
    html += this._createIndicationTemplateHtml(therapy);

    // therapy start //
    html += this._createTherapyStartTemplateHtml(therapy);

    // therapy end //
    html += this._createTherapyEndTemplateHtml(therapy);
    html += '<br>';

    // status warning //
    html += this._createStatusWarningTemplateHtml(therapyState ? therapyState.therapyStatus : null);

    // consecutive day //
    html += this._createConsecutiveDayTemplateHtml(therapyState ? therapyState.consecutiveDay : null);

    // prescriber //
    html += this._createPrescriberTemplateHtml(therapy);
    if (therapy.composerName != therapy.prescriberName)
    {
      html += this._createComposerTemplateHtml(therapy);
    }

    // warnings //
    html += this._createWarningsTemplateHtml(therapy.criticalWarnings);

    html += '<br>';

    html += '</td><td valign="top">';
    html += '<div id="' + this._createMedicationInfoContainerId(therapy.medication.id, 0) + '"></div>';
    html += '</td></tr></table>';

    //table and div start outside function call
    html += '</div>';
    html += '</td>';
    html += '</tr>';
    html += '</table>';

    return html;
  },

  _createComplexTherapyOrderHtmlTemplate: function(html, therapy, haveHistoryChanges, isCurrent, therapyState)
  {
    var view = this.getView();
    html += '<table>';
    for (var i = 0; i < therapy.ingredientsList.length; i++)
    {
      var ingredient = therapy.ingredientsList[i];
      var medication = ingredient.medication;
      var isSolution = medication.medicationType == "SOLUTION";

      html += '<tr><td valign="top">';

      if (tm.jquery.Utils.isEmpty(medication.genericName) || isSolution)
      {
        html += '<p class="TextHeading2">' + medication.name + '</p>';
      }
      else
      {
        html += '<p class="TextHeading2">' + medication.genericName + '</p>';
        html += '<p class="TextData">';
        html += '(' + medication.name + ')';
        html += '</p>';
      }

      if (ingredient.quantity)
      {
        html += '<span class="TextLabel">' + view.getDictionary("dose") + ' </span>';
        var quantityDisplay = tm.views.medications.MedicationUtils.doubleToString(ingredient.quantity, 'n2') + ' ' + ingredient.quantityUnit;
        if (ingredient.volume)
        {
          quantityDisplay += '/' + tm.views.medications.MedicationUtils.doubleToString(ingredient.volume, 'n2') + ' ' + ingredient.volumeUnit
        }
        html += '<span class="TextData">' + quantityDisplay + ' </span>';
        html += '<br>';
      }
      else if (ingredient.volume)
      {
        html += '<span class="TextLabel">' + view.getDictionary("dose") + ' </span>';
        html += '<span class="TextData">' + tm.views.medications.MedicationUtils.doubleToString(ingredient.volume, 'n2') + ' ' + ingredient.volumeUnit + ' </span>';
        html += '<br>';
      }
      html += '</td><td valign="top">';
      html += '<div id="' + this._createMedicationInfoContainerId(medication.id, i) + '"></div>';
      html += '</td></tr>';
    }
    html += '</table>';

    // volume sum //
    var volumeSumDisplay = therapy.volumeSumDisplay;
    if (tm.jquery.Utils.isEmpty(volumeSumDisplay) == false)
    {
      html += '<br>';
      html += '<span class="TextLabel">' + view.getDictionary("volume.total") + ' </span>';
      html += '<span class="TextData">' + (tm.jquery.Utils.isEmpty(volumeSumDisplay) ? "" : volumeSumDisplay) + '</span>';
    }

    // heparin //
    if (therapy.additionalInstructionDisplay)
    {
      html += '<span class="TextData"> + ' + therapy.additionalInstructionDisplay + ' </span>';
      html += '<br>';
    }

    if (!volumeSumDisplay || !therapy.additionalInstructionDisplay)
    {
      html += '<br>';
    }

    // infusion rate //
    if (tm.jquery.Utils.isEmpty(therapy.timedDoseElements))
    {
      if (therapy.speedDisplay)
      {
        html += this._addLabelDataPair(view.getDictionary("rate"), therapy.speedDisplay);
      }
      else if (therapy.adjustToFluidBalance == true)
      {
        html += this._addLabelDataPair(view.getDictionary("rate"), view.getDictionary("adjust.to.fluid.balance.short"));
      }
      if (therapy.speedFormulaDisplay)
      {
        html += this._addLabelDataPair(view.getDictionary("dose"), therapy.speedFormulaDisplay);
      }
    }
    else
    {
      html += this._createTimedDoseElementsTemplateHtml(therapy.timedDoseElements, true);
      html += '<br>';
    }

    // duration //
    if (therapy.durationDisplay)
    {
      html += this._addLabelDataPair(view.getDictionary("duration"), therapy.durationDisplay);
    }

    // dosing interval //
    html += this._createDosingIntervalTemplateHtml(therapy);

    // continuous infusion
    if (therapy.continuousInfusion == true)
    {
      html += '<span class="TextLabel">' + view.getDictionary("continuous.infusion") + ' </span>';
      html += '<div class="TextDataBold checkBox_on-icon"></div>';
      html += '<br>';
    }

    // route //
    html += this._createRouteTemplateHtml(therapy);

    // comment //
    html += this._createCommentTemplateHtml(therapy);

    // indication //
    html += this._createIndicationTemplateHtml(therapy);

    // therapy start //
    html += this._createTherapyStartTemplateHtml(therapy);

    // therapy end //
    html += this._createTherapyEndTemplateHtml(therapy);
    html += '<br>';

    // status warning //
    html += this._createStatusWarningTemplateHtml(therapyState ? therapyState.therapyStatus : null);

    // consecutive day //
    html += this._createConsecutiveDayTemplateHtml(therapyState ? therapyState.consecutiveDay : null);

    html += this._createPrescriberTemplateHtml(therapy);
    html += this._createComposerTemplateHtml(therapy);
    html += '<br>';

    //table and div start outside function call
    html += '</div>';
    html += '</td>';

    html += '</tr>';
    html += '</table>';

    return html;
  },

  _createTimedDoseElementsTemplateHtml: function(timedDoseElements, isComplex)
  {
    var view = this.getView();

    var html = '';
    html += '<div class="TextLabel" style="display:inline-block;vertical-align:top;padding-top:1px;">';
    html += view.getDictionary("dose");
    html += '&nbsp;&nbsp;</div>';
    html += '<div style="display:inline-block;">';
    html += '<table border=0 cellpadding=0 cellspacing=0>';
    for (var n = 0; n < timedDoseElements.length; n++)
    {
      var timedDoseElement = timedDoseElements[n];
      html += '<tr>';
      html += '<td style="padding-right:15px;">';
      html += isComplex ? timedDoseElement.intervalDisplay : timedDoseElement.timeDisplay;
      html += '<td>';
      html += '<td>';
      html += isComplex ? timedDoseElement.speedDisplay : timedDoseElement.quantityDisplay;
      if (isComplex && timedDoseElement.speedFormulaDisplay)
      {
        html += '<span class="TextData"> (' + timedDoseElement.speedFormulaDisplay + ')</span>';
      }
      html += '<td>';
      html += '</tr>';
    }
    html += '</table>';
    html += '</div>';

    return html;
  },

  _createDosingIntervalTemplateHtml: function(therapy)
  {
    var view = this.getView();

    var html = '';
    var frequencyDisplay = therapy.frequencyDisplay;
    if (tm.jquery.Utils.isEmpty(frequencyDisplay) == false)
    {
      html += '<span class="TextLabel">' + view.getDictionary("dosing.interval") + ' </span>';
      html += '<span class="TextData">';
      html += frequencyDisplay;
      if (therapy.daysOfWeekDisplay)
      {
        html += ' &ndash; ' + therapy.daysOfWeekDisplay;
      }
      if (therapy.daysFrequencyDisplay)
      {
        html += ' &ndash; ' + therapy.daysFrequencyDisplay.toLowerCase();
      }
      if (therapy.whenNeededDisplay)
      {
        html += ' &ndash; ' + therapy.whenNeededDisplay;
      }
      if (therapy.startCriterionDisplay)
      {
        html += ' &ndash; ' + therapy.startCriterionDisplay;
      }
      html += '</span>';
      html += '<br>';
      if (tm.jquery.Utils.isEmpty(therapy.maxDailyFrequency) == false)
      {
        html += '<span class="TextLabel">' + view.getDictionary("dosing.max.24h") + ' </span>';
        html += '<span class="TextData">' + therapy.maxDailyFrequency + '</span>';
        html += '<br>';
      }
    }
    return html;
  },

  _createRouteTemplateHtml: function(order)
  {
    var view = this.getView();

    var html = '';
    var routeName = order.route.name;
    if (tm.jquery.Utils.isEmpty(routeName) == false)
    {
      html += this._addLabelDataPair(view.getDictionary("route"), routeName);
    }
    return html;
  },

  _createCommentTemplateHtml: function(order)
  {
    var view = this.getView();

    var html = '';
    var comment = order.comment;
    if (comment)
    {
      if (tm.jquery.Utils.isEmpty(comment) == false)
      {
        html += this._addLabelDataPair(view.getDictionary("commentary"), (tm.jquery.Utils.isEmpty(comment) ? "" : comment));
      }
    }
    return html;
  },

  _createIndicationTemplateHtml: function(therapy)
  {
    var view = this.getView();

    var html = '';
    if (therapy.clinicalIndication)
    {
      html += this._addLabelDataPair(view.getDictionary("indication"), therapy.clinicalIndication);
    }
    return html;
  },

  _createTherapyStartTemplateHtml: function(order)
  {
    var view = this.getView();

    var html = '';
    var startTime = order.start;
    html += '<span class="TextLabel">' + view.getDictionary("from") + ' </span>';
    html += '<span class="TextData">';
    html += (tm.jquery.Utils.isEmpty(startTime) ? "" : view.getDisplayableValue(startTime, "short.date.time"));
    html += '</span>';
    return html;
  },

  _createTherapyEndTemplateHtml: function(order)
  {
    var view = this.getView();

    var html = '';
    var endTime = order.end;
    if (endTime)
    {
      html += ' ';
      html += '<span class="TextLabel"> ' + view.getDictionary("until.low.case") + ' </span>';
      html += '<span class="TextData">';
      html += view.getDisplayableValue(endTime, "short.date.time");
      html += '</span>';
    }
    return html;
  },

  _createPrescriberTemplateHtml: function(therapy)
  {
    if (tm.jquery.Utils.isEmpty(therapy.prescriberName))
    {
      return '';
    }
    return this._addLabelDataPair(this.view.getDictionary("prescribed.by"), therapy.prescriberName);
  },

  _createComposerTemplateHtml: function(therapy)
  {
    if (tm.jquery.Utils.isEmpty(therapy.composerName))
    {
      return '';
    }
    var html = '';
    html += '<span class="TextLabel">' + this.view.getDictionary("composed.by") + ' </span>';
    html += '<span class="TextData">' + therapy.composerName + ' </span>';
    return html;
  },

  _createConsecutiveDayTemplateHtml: function(consecutiveDay)
  {
    var view = this.getView();

    var html = '';
    if (consecutiveDay != null)
    {
      html += '<span class="TextLabel">' + view.getDictionary("therapy.day") + ' </span>';
      html += '<span class="TextData">';
      html += consecutiveDay;
      html += '</span>';
      html += '<br>';
    }
    return html;
  },

  _createWarningsTemplateHtml: function(warnings)
  {
    var html = '';
    for (var i = 0; i < warnings.length; i++)
    {
      html += '<span class="icon_warning"/>' + " " + warnings[i];
      html += '<br>';
    }
    return html;
  },

  _createStatusWarningTemplateHtml: function(therapyStatus)
  {
    var html = '';
    if (therapyStatus)
    {
      if (therapyStatus == 'LATE' || therapyStatus == 'VERY_LATE')
      {
        if (status == "LATE")
        {
          html += '<span class="icon_late"/>';
        }
        else
        {
          html += '<span class="icon_very_late"/>';
        }
        html += ' ' + this.getView().getDictionary('therapy.not.reviewed.today');
        html += '<br>';
      }
    }
    return html;
  },

  _renderChangeHistoryGrid: function(changeHistoryList)
  {
    if (!(tm.jquery.Utils.isEmpty(changeHistoryList) || changeHistoryList.length == 0))
    {
      var view = this.getView();
      var historyContainer = this._createHistoryContainer(changeHistoryList);

      var changeHistoryContainer = new tm.jquery.Container({
        cls: "change-history",
        scrollable: 'visible',
        layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5)
      });
      changeHistoryContainer.add(new tm.jquery.Container({cls: "divider"}));
      changeHistoryContainer.add(new tm.jquery.Container({
        cls: "header",
        layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5),
        html: '<p class="PortletHeading2">' + view.getDictionary("history.of.changes") + '</p>'
      }));
      changeHistoryContainer.add(historyContainer);
      this.mainContainer.add(changeHistoryContainer);
    }
  },

  _createHistoryContainer: function(changeHistoryList)
  {
    var view = this.getView();
    var historyContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 5)
    });

    var html = '<table>';
    for (var i = 0; i < changeHistoryList.length; i++)
    {
      var historyElement = changeHistoryList[i];    // [TherapyChangeHistoryDto.java]
      html += '<tr><td valign="top" style="padding:0;">';
      //time
      html += '<div class="time">';
      html += '<p class="TextHeading1">';
      html += view.getDisplayableValue(historyElement.changeTime, "short.time");
      html += '</p>';
      html += '<p class="TextDataLight">';
      html += view.getDisplayableValue(historyElement.changeTime, "short.date");
      html += '</p>';
      html += '</div>';
      html += '</td><td valign="top"><div class="detail">';

      //content
      if (tm.jquery.Utils.isEmpty(historyElement.editor) == false)
      {
        html += this._addLabelDataPair(view.getDictionary("prescribed.by"), historyElement.editor);
      }

      if (tm.jquery.Utils.isEmpty(historyElement.changes) || historyElement.changes.length == 0)
      {
        html += '<p><span class="TextLabel">' + this.getView().getDictionary("no.changes") + '</span></p>';
      }
      else
      {
        for (var j = 0; j < historyElement.changes.length; j++)
        {
          var change = historyElement.changes[j];        // [TherapyChangeDto.java]
          var changeDataValue = '<strike>' + change.oldValue + '</strike> <b>' + change.newValue + '</b>';
          html += this._addLabelDataPair(this._getChangeLabelValue(change.type, view), changeDataValue);
        }
      }
      html += '';
      html += '';

      html += '</div></td><tr>';
    }

    html += '</table>';
    historyContainer.setHtml(html);
    return historyContainer;
  },

  _getChangeLabelValue: function(changeType, view)    // enum [TherapyChangeType.java]
  {
    if (changeType == "MEDICATION")
    {
      return view.getDictionary("medication");
    }
    else if (changeType == "DOSE")
    {
      return view.getDictionary("dose");
    }
    else if (changeType == "DOSE_INTERVAL")
    {
      return view.getDictionary("dosing.interval.short");
    }
    else if (changeType == "SPEED")
    {
      return view.getDictionary("rate");
    }
    else
    {
      return changeType;
    }
  },

  _addLabelDataPair: function(labelValue, dataValue)
  {
    var html = '<span class="TextLabel">' + labelValue + ' </span>';
    html += '<span class="TextData">' + dataValue + ' </span><br />';
    return html;
  },

  /**
   * Getters & Setters
   */
  getView: function()
  {
    return this.view;
  }
  //getParent: function()
  //{
  //  return this.parent;
  //}
});