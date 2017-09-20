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
Class.define('app.views.medications.common.TherapyContainer', 'tm.jquery.Container', {
  cls: "therapy-container",
  data: null,
  displayProvider: null,
  view: null,
  toolbar: null,

  showIconTooltip: null,  // TODO: move this to the display provider
  scrollableElement: null,

  _baseCls: null,
  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.displayProvider = this.getConfigValue("displayProvider", new app.views.medications.TherapyDisplayProvider({
      view: this.view
    }));
    this.showIconTooltip = this.getConfigValue("showIconTooltip", true);

    this._baseCls = tm.jquery.Utils.isEmpty(this.getCls()) ? "" : this.getCls();

    this._buildGui();

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'therapy-container-coordinator',
      view: this.getView(),
      component: this
    });
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    var displayProvider = this.getDisplayProvider();
    var data = this.getData();
    var therapy = this.getTherapy();
    var toolbar = this.getToolbar();
    var therapyShortDescription = therapy.formattedTherapyDisplay;
    var therapyStatusDisplayClass = displayProvider.getStatusClass(data);
    var self = this;

    var showDetailsCard = this.showIconTooltip === true;

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));
    this.setCls(this._baseCls + " " + therapyStatusDisplayClass + " text-unselectable");

    var statusLine = new tm.jquery.Component({
      cls: "status-line",
      alignSelf: "stretch",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "5px")
    });

    var iconContainer = new tm.jquery.Container({
      alignSelf: "flex-start",
      cursor: showDetailsCard ? "pointer" : "default",
      margin: "2 4 2 3",
      width: 48,
      height: 48,
      html: displayProvider.getBigIconContainerHtml(data)
    });

    if (showDetailsCard)
    {
      iconContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        self._showTherapyDetailsContentPopup(component);
      });
    }

    if (displayProvider.getShowBnf() && !tm.jquery.Utils.isEmpty(data.therapy.bnfMaximumPercentage))
    {
      therapyShortDescription += tm.views.medications.MedicationUtils.createBnfPercentageInfoHtml(this.view, data.therapy.bnfMaximumPercentage);
    }

    if (displayProvider.getShowChangeHistory() && !tm.jquery.Utils.isEmpty(data.changes) && data.changes.length > 0)
    {
      therapyShortDescription += '<p class="TextData history-title">' + this.getView().getDictionary("history.of.changes") + '</p>';
      therapyShortDescription += this._createHistoryContent(data.changes, false);
    }
    else if (data.changeType == 'ABORT' || data.changeType == 'SUSPEND') // enum [PharmacistTherapyChangeType.java]
    {
      therapyShortDescription += '<p class="TextData history-title">' + this.getView().getDictionary("history.of.changes") + '</p>';
      therapyShortDescription += this._createHistoryContent(data.changeType, true);
    }

    if (displayProvider.getShowChangeReason() && !tm.jquery.Utils.isEmpty(data.changeReasonDto)
        && data.changeReasonDto instanceof app.views.medications.common.dto.TherapyChangeReason)
    {
      therapyShortDescription += this._createChangeReasonContent(data.changeReasonDto);
    }

    var contentContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      html: therapyShortDescription,
      cursor: showDetailsCard ? "pointer" : "default",
      cls: 'TherapyDescription',
      alignSelf: "flex-start"
    });

    if (showDetailsCard)
    {
      contentContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        self._showTherapyDetailsContentPopup(component);
      });
    }

    if (data.therapyEndsBeforeNextRounds)
    {
      this.setStyle((this.getStyle() != null ? this.getStyle() : '') + 'border-right: 2px solid grey;');
      //this.setStyle('border-right: 2px solid grey;');
    }

    this.add(statusLine);
    this.add(iconContainer);
    this.add(contentContainer);

    if (!tm.jquery.Utils.isEmpty(toolbar)) {
      // add an empty flex contianer to push the toolbar to the right
      this.add(new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      }));
      this.add(toolbar);

      if (toolbar.refresh instanceof Function) toolbar.refresh();
    }
  },

  _createChangeReasonContent: function(changeReasonDto)
  {
    var view = this.getView();

    var reasonName = changeReasonDto.getReason().name;
    var comment = changeReasonDto.getComment();

    var reasonText = tm.jquery.Utils.isEmpty(reasonName) ? "" : reasonName;

    if (!tm.jquery.Utils.isEmpty(comment))
    {
      reasonText += " - " + comment;
    }

    return '<div class="change-reason"><div class="TextLabel MedicationLabel">' + view.getDictionary("reason")
        + '</div><div class="TextData">' + reasonText + '</div></div>';
  },

  _createHistoryContent: function (changeData, changedAction)
  {
    var view = this.getView();
    var html = '';
    var change;
    if (changedAction)
    {
      change = this._createChangedActionValue(changeData, view);
      if (!tm.jquery.Utils.isEmpty(change))
      {
        html += this._addLabelDataPair(
            view.getDictionary("action"), this._combineChangeDataValue("", change));
      }
    }
    else
    {
      for (var i = 0; i < changeData.length; i++)
      {
        change = changeData[i]; // [TherapyChangeDto.java]
        html += this._addLabelDataPair(
            this._getChangeLabelValue(change.type, view), this._combineChangeDataValue(change.oldValue, change.newValue));
      }
    }
    return html;
  },

  _createChangedActionValue: function(changeType, view) // enum [PharmacistTherapyChangeType.java]
  {
    var result = '';
    if (changeType == "ABORT")
    {
      return view.getDictionary("stop.past");
    }
    else if (changeType == "SUSPEND")
    {
      return view.getDictionary("suspend.past");
    }
    else
    {
      return result;
    }
  },

  _combineChangeDataValue: function(oldValue, newValue)
  {
    return '<span class="old-data">' + oldValue + '</span> <span class="new-data">' + newValue + '</span>';
  },

  _addLabelDataPair: function(labelValue, dataValue)
  {
    var html = '<span class="TextLabel MedicationLabel">' + labelValue + ' </span>';
    html += '<span class="TextData">' + dataValue + ' </span><br />';
    return html;
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

  _isTherapyCancelledOrAborted: function (therapyData)
  {
    var enums = app.views.medications.TherapyEnums;
    var therapyStatus = therapyData.therapyStatus;
    return therapyStatus == enums.therapyStatusEnum.ABORTED || therapyStatus == enums.therapyStatusEnum.CANCELLED;
  },

  /**
   * @param {tm.jquery.Component} component
   * @param {app.views.medications.common.dto.MedicationData} [medicationData=undefined]
   * @private
   */
  _buildTherapyDetailsContentPopup: function(component, medicationData)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var data = this.getData();

    var therapyDetailsContent = new app.views.medications.common.TherapyDetailsContentContainer({
      view: view,
      displayProvider: this.getDisplayProvider(),
      data: data,
      therapy: this.getTherapy(),
      medicationData: medicationData
    });

    var therapyDetailsContentPopup = appFactory.createDefaultPopoverTooltip(
        view.getDictionary("medication"),
        null,
        therapyDetailsContent
    );
    therapyDetailsContent.setDialogZIndex(therapyDetailsContentPopup.zIndex);

    therapyDetailsContentPopup.addTestAttribute(this.getTherapy().getTherapyId());
    therapyDetailsContentPopup.setDefaultAutoPlacements(["rightBottom", "rightTop", "right"]);
    therapyDetailsContentPopup.setPlacement("auto");
    // don't touch default placements, because it triggers errors in positioning ...

    if (this.getScrollableElement())
    {
      therapyDetailsContentPopup.setAppendTo(this.getScrollableElement());
    }

    therapyDetailsContentPopup.setTrigger("manual");
    component.setTooltip(therapyDetailsContentPopup);

    setTimeout(function()
    {
      therapyDetailsContentPopup.show();
    }, 10);
  },

  /**
   * @param {tm.jquery.Component} component
   * @private
   */
  _showTherapyDetailsContentPopup: function(component)
  {
    var self = this;
    var therapy = this.getTherapy();
    var view = this.getView();

    tm.jquery.ComponentUtils.hideAllTooltips();
    if (therapy.hasNonUniversalIngredient())
    {
      view.getRestApi().loadMedicationDataForMultipleIds(therapy.getAllIngredientIds()).then(function(medicationData)
      {
        self._buildTherapyDetailsContentPopup(component, medicationData);
      });
    }
    else
    {
      self._buildTherapyDetailsContentPopup(component);
    }
  },

  /* getters, setters, public */
  getData: function ()
  {
    return tm.jquery.Utils.isEmpty(this.data) ? {} : this.data;
  },

  getTherapy: function()
  {
    return this.getData().therapy;
  },

  getTherapyId: function ()
  {
    var data = this.getData();

    return tm.jquery.Utils.isEmpty(data) ?
        null : (tm.jquery.Utils.isEmpty(data.therapy) ? null : data.therapy.compositionUid);
  },

  getView: function ()
  {
    return this.view;
  },

  getDisplayProvider: function ()
  {
    return this.displayProvider;
  },

  getScrollableElement: function()
  {
    return this.scrollableElement;
  },

  getToolbar: function ()
  {
    return this.toolbar;
  },

  setToolbar: function (toolbar)
  {
    // add an empty flex contianer to push the toolbar to the right
    if (tm.jquery.Utils.isEmpty(this.toolbar))
    {
      this.add(new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      }));
    }
    this.toolbar = toolbar;
    this.add(this.toolbar);
  },

  highlight: function ()
  {
    if (this.isRendered())
    {
      $(this.getDom()).effect("highlight", {color: "#FFFFCC"}, 1000);
    }
    else
    {
      var appFactory = this.getView().getAppFactory();
      var self = this;

      appFactory.createConditionTask(
          function ()
          {
            self.highlight();
          },
          function ()
          {
            return self.isRendered();
          },
          50, 10);
    }
  },

  refresh: function ()
  {
    this.removeAll();
    this._buildGui();
    this.repaint();
  }
});
