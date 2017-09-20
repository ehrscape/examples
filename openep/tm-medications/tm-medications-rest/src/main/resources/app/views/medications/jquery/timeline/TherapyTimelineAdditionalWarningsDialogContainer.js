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
Class.define('tm.views.medications.timeline.TherapyTimelineAdditionalWarningsDialogContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "therapy-timeline-container additional-warnings-container",
  layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 0),

  view: null,
  additionalWarnings: null,

  _form: null,
  _warningRowDataList: null,
  _therapyDisplayProvider: null,

  Constructor: function()
  {
    this.callSuper();

    var view = this.view;

    this._warningRowDataList = [];
    this._therapyDisplayProvider = new app.views.medications.TherapyDisplayProvider({
      view: this.view
    });

    this._form = new tm.jquery.Form({
      view: view,
      showTooltips: false,
      requiredFieldValidatorErrorMessage: view.getDictionary("field.value.is.required")
    });

    this._buildGUI();
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;
    var form = this._form;

    form.setOnValidationSuccess(function(form)
    {
      var abortTherapyIds = [];
      var overrideWarnings = [];
      var patientId = self.view.getPatientId();
      var completeTaskIds = self.getAdditionalWarnings().getTaskIds();

      self._warningRowDataList.forEach(function(warningRowData)
      {
        var therapy = warningRowData.getTherapy();
        var therapyId = therapy.getTherapyId();

        if (warningRowData.isAborted())
        {
          abortTherapyIds.add(therapyId);
        }
        else
        {
          var warnings = warningRowData.getWarnings();
          var overrideReasons = warningRowData.getReasons();
          var warningOverrideReasons = overrideReasons.map(function(overrideReason, index)
          {
            return self._createOverriddenWarningString(overrideReason, warnings[index].description);
          });
          overrideWarnings.add({therapyId: therapyId, warnings: warningOverrideReasons});
        }
      });

      var url;
      var params;
      var viewHubNotifier = this.view.getHubNotifier();
      var hubAction = tm.views.medications.TherapyView.THERAPY_HANDLE_ADDITIONAL_WARNINGS_ACTION;
      viewHubNotifier.actionStarted(hubAction);

      url = self.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_HANDLE_ADDITIONAL_WARNINGS_ACTION;
      params = {
        additionalWarningsActionDto: JSON.stringify({
          patientId: patientId,
          abortTherapyIds: abortTherapyIds,
          overrideWarnings: overrideWarnings,
          completeTaskIds: completeTaskIds
        })
      };

      self.view.loadPostViewData(url, params, null,
          function()
          {
            var resultData = new app.views.common.AppResultData({success: true});
            resultDataCallback(resultData);
            viewHubNotifier.actionEnded(hubAction);
          },
          function()
          {
            var resultData = new app.views.common.AppResultData({success: false});
            resultDataCallback(resultData);
            viewHubNotifier.actionFailed(hubAction);
          },
          true);
    });

    form.setOnValidationError(function(form, validationResults)
    {
      resultDataCallback(new app.views.common.AppResultData({success: false, value: null}));
    });
    form.submit();
  },

  _buildGUI: function()
  {
    var headerContainer = this._createHeaderContainer();
    var warningsContainer = this._createWarningsContainer();

    this.add(headerContainer);
    this.add(warningsContainer);
  },

  _createOverriddenWarningString: function(overrideReason, warningDescription)
  {
    var view = this.view;
    return view.getDictionary("warning.overridden") + ". " + view.getDictionary("reason")+ ": "  +
        overrideReason + " " + view.getDictionary("warning") + ": " + warningDescription;
  },

  _createHeaderContainer: function()
  {
    var view = this.view;

    var additionalWarningsHeader = new tm.jquery.Container({
      cls: "additional-warnings-header",
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 10)
    });

    var warningImage = new tm.jquery.Image({cls: "additional_warnings", width: 48, height: 48});

    var headerDescription = '';

    headerDescription += '<p class="TextDataBold">';
    headerDescription += view.getDictionary("therapy.risk.warning.dialog.title");
    headerDescription += '</p>';
    headerDescription += '<p></p>';
    headerDescription += '<p class="TextData">';
    headerDescription += view.getDictionary("therapy.risk.warning.dialog.description");
    headerDescription += '</p>';

    var descriptionContainer = new tm.jquery.Container({flex: 1, html: headerDescription});

    additionalWarningsHeader.add(warningImage);
    additionalWarningsHeader.add(descriptionContainer);

    return additionalWarningsHeader;
  },

  _createWarningsContainer: function()
  {
    var self = this;
    var additionalWarnings = self.getAdditionalWarnings();

    var container = new tm.jquery.Container({
      cls: "warnings-container",
      scrollable: "vertical",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 20)
    });

    additionalWarnings.getWarnings().filter(function(additionalWarning)
    {
      return additionalWarning.warnings.length;
    }).forEach(function(additionalWarning)
    {
      var therapy = additionalWarning.therapy;
      var warnings = additionalWarning.warnings;
      var warningRowData = new tm.views.medications.timeline.AdditionalWarningRowData();

      warningRowData.setTherapy(therapy);
      warnings.forEach(function(warning)
      {
        warningRowData.addWarning(warning.warning);
      });
      self._warningRowDataList.add(warningRowData);

      var warningContainer = self._createWarningContainer(warningRowData, function()
      {
        self._performAbortAction(warningRowData);
      });
      warningRowData.setContainer(warningContainer);
      container.add(warningContainer);
    });
    return container;
  },

  _createWarningContainer: function(warningRowData, abortCallback)
  {
    var view = this.view;
    var self = this;
    var appFactory = view.getAppFactory();

    var rowWarnings = warningRowData.getWarnings();
    var rowTherapy = warningRowData.getTherapy();

    var therapyIcon = new tm.jquery.Container({
      width: 48, height: 48, cls: "therapy-icon", html: appFactory.createLayersContainerHtml({
            background: {cls: this._therapyDisplayProvider.getTherapyIcon(rowTherapy)},
            layers: [
              {hpos: "right", vpos: "bottom", cls: "status-icon"}
            ]
          }
      )
    });

    var html = '';
    html += '<p class="TextData">';
    html += rowTherapy.formattedTherapyDisplay;
    html += '</p>';

    var detailTherapyContainer = new tm.jquery.Container({html: html});

    var detailWarningContainers = rowWarnings.map(function(warning)
    {
      var detailTherapyContainerWarningIcon = new tm.jquery.Image({cls: "high-alert-icon", width: 24, height: 24});
      var detailTherapyWarningInfoContainer = new tm.jquery.Container({
        flex: 1, html: warning.description
      });

      var detailWarningContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 10)});
      detailWarningContainer.add(detailTherapyContainerWarningIcon);
      detailWarningContainer.add(detailTherapyWarningInfoContainer);
      return detailWarningContainer;
    });

    var detailContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 10)
    });

    detailContainer.add(detailTherapyContainer);
    detailWarningContainers.forEach(function(detailWarningContainer)
    {
      var detailCommentContainer = new tm.jquery.Container({
        cls: "comment-container", layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 10)
      });
      var commentLabel = new tm.views.medications.MedicationUtils.crateLabel(
          "TextDataLight uppercase", view.getDictionary("reason") + ":", 0);
      var commentTextField = new tm.jquery.TextField({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        cls: "field-flat"
      });

      self._form.addFormField(new tm.jquery.FormField({
        component: commentTextField,
        required: true,
        validation: {
          type: "local"
        },
        componentValueImplementationFn: function()
        {
          return warningRowData.isAborted() ? true : commentTextField.getValue();
        }
      }));
      commentTextField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        if (warningRowData.isAborted() === false)
        {
          warningRowData.addReason(commentTextField.getValue().trim());
        }
      });

      detailCommentContainer.add(commentLabel);
      detailCommentContainer.add(commentTextField);

      detailContainer.add(detailWarningContainer);
      detailContainer.add(detailCommentContainer);
    });

    var deleteIcon = new tm.jquery.Image({
      cls: "icon-delete", cursor: "pointer", width: 24, height: 24
    });

    var container = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 10), html: html});

    container.add(therapyIcon);
    container.add(detailContainer);
    container.add(deleteIcon);

    deleteIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (warningRowData.isAborted() === false)
      {
        abortCallback();
      }
    });

    return container;
  },

  _performAbortAction: function(warningRowData)
  {
    var view = this.view;
    var appFactory = view.getAppFactory();

    var rowContainer = warningRowData.getContainer();
    var rowTherapy = warningRowData.getTherapy();

    var warningImage = new tm.jquery.Image({cls: "action_warning", width: 48, height: 48});

    var html = '';
    html += '<p class="TextDataBold">';
    html += view.getDictionary("therapy.stopping");
    html += '</p>';
    html += '<p class="TextData">';
    html += view.getDictionary("stop.therapy.confirm.msg");
    html += '</p>';

    var descriptionContainer = new tm.jquery.Container({flex: 1, html: html});

    var container = new tm.jquery.Container({
      cls: "abort-action-container",
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 10)
    });
    container.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      var message = $(component.getRenderToElement()).html();
      var confirmSystemDialog = appFactory.createConfirmSystemDialog(message, function(confirmed)
      {
        if (confirmed === true)
        {
          var $rowContainerDom = $(rowContainer.getDom());
          if (rowTherapy.getStart() > CurrentTime.get())
          {
            $rowContainerDom.find(".status-icon").addClass("icon_cancelled");
          }
          else
          {
            $rowContainerDom.find(".status-icon").addClass("icon_aborted");
          }

          $rowContainerDom.find(".tm-textfield").removeClass("form-field-validationError");
          rowContainer.setEnabled(false, true);
          rowContainer.addCls("disabled");

          tm.jquery.ComponentUtils.setElementOpacity($rowContainerDom, 0.5);

          warningRowData.setAborted(true);
        }
      });
      confirmSystemDialog.setWidth(320);
      confirmSystemDialog.setHeight(180);
      confirmSystemDialog.show();
    });
    container.add(warningImage);
    container.add(descriptionContainer);

    container.doRender();
  },

  getAdditionalWarnings: function()
  {
    return this.additionalWarnings;
  }

});

Class.define('tm.views.medications.timeline.AdditionalWarningRowData', 'tm.jquery.Object', {
  _therapy: null,
  _container: null,
  _aborted: false,
  _reasons: null,

  Constructor: function(config)
  {
    this._warnings = [];
    this._reasons = [];
    this.callSuper(config)
  },

  addWarning: function(warning)
  {
    this._warnings.push(warning);
  },
  getWarnings: function()
  {
    return this._warnings;
  },
  setTherapy: function(therapy)
  {
    this._therapy = therapy;
  },
  getTherapy: function()
  {
    return this._therapy;
  },
  setContainer: function(container)
  {
    this._container = container;
  },
  getContainer: function()
  {
    return this._container;
  },
  setAborted: function(aborted)
  {
    this._aborted = aborted;
  },
  isAborted: function()
  {
    return this._aborted === true;
  },
  addReason: function(reason)
  {
    this._reasons.push(reason);
  },
  getReasons: function()
  {
    return this._reasons;
  },

  toJson: function()
  {
    return this.convert(this.callSuper(), {
      warning: this._warnings,
      therapy: this._therapy,
      container: this._container,
      aborted: this._aborted,
      reason: this._reasons
    });
  }
});