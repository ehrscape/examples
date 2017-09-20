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
Class.define('tm.views.medications.therapy.GridTherapyContainerToolbar', 'app.views.medications.common.TherapyContainerToolbar', {
  cls:'grid-therapy-toolbar',
  alignSelf: "stretch",
  style: "min-width: 130px; position: relative;", /* min width required to prevent validToComponent from wrapping */

  showRelatedPharmacistReviewsEventCallback: null,
  tasksChangedEventCallback: null,
  editTherapyEventCallback: null,
  confirmTherapyEventCallback: null,
  abortTherapyEventCallback: null,

  _editButton: null,
  _confirmButton: null,
  _abortButton: null,

  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /* for override */
  _buildGUI: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var therapyContainer = this.getTherapyContainer();
    var data = therapyContainer.getData();
    var therapy = data.therapy;
    var enums = app.views.medications.TherapyEnums;
    var pharmacistReviewReferBack =
        data.therapyPharmacistReviewStatus == enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK;
    var validTo = therapy.getEnd() ?
        view.getDisplayableValue(therapy.getEnd(), "short.date.time") :
        '<span style="font-size: 9px; color: #D3D3D3;">&diams;&diams;</span>';

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-end"));
    this._abortButton = null; // reset button instances in case of a refresh()
    this._confirmButton = null; // reset button instances in case of a refresh()
    this._editButton = null; // reset button instances in case of a refresh()

    var suspended = data.therapyStatus === enums.therapyStatusEnum.SUSPENDED;

    var showActionButtons = (data.doctorReviewNeeded || suspended) && !pharmacistReviewReferBack;
    if (showActionButtons)
    {
      var actionButtonContainer = new tm.jquery.Container({
        cls: "action-buttons",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 5),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });

      var editButton = new tm.jquery.Button({
        tooltip: tm.views.medications.MedicationUtils.createTooltip(view.getDictionary("edit"), "bottom", view),
        cls: "btn-flat icon-edit edit-therapy-button",
        enabled: view.getTherapyAuthority().isEditAllowed(),
        width: 32,
        handler: function()
        {
          if (self.editTherapyEventCallback)
          {
            self.editTherapyEventCallback(therapyContainer);
          }
        }
      });
      var abortButton = new tm.jquery.Button({
        tooltip: tm.views.medications.MedicationUtils.createTooltip(view.getDictionary("stop"), "bottom", view),
        cls: "btn-flat icon-delete abort-therapy-button",
        enabled: view.getTherapyAuthority().isEditAllowed(),
        width: 32,
        handler: function()
        {
          if (self.abortTherapyEventCallback)
          {
            self.abortTherapyEventCallback(therapyContainer);
          }
        }
      });

      var toolTipTextKey = suspended ? "reissue" : "confirm";
      var confirmButton = new tm.jquery.Button({
        tooltip: tm.views.medications.MedicationUtils.createTooltip(view.getDictionary(toolTipTextKey), "bottom", view),
        cls: "btn-flat review-icon review-therapy-button",
        enabled: view.getTherapyAuthority().isEditAllowed(),
        width: 32,
        handler: function()
        {
          if (self.confirmTherapyEventCallback)
          {
            self.confirmTherapyEventCallback(therapyContainer);
          }
        }
      });

      this._abortButton = abortButton;
      this._editButton = editButton;
      this._confirmButton = confirmButton;

      actionButtonContainer.add(editButton);
      actionButtonContainer.add(abortButton);

      if (view.doctorReviewEnabled === true || suspended)
      {
        actionButtonContainer.add(confirmButton);
      }

      this.add(actionButtonContainer);
    }
    else if (pharmacistReviewReferBack)
    {
      var prIconOptions = {
        background: {cls: "icon_pharmacist_review" },
        layers: [
          {hpos: "right", vpos: "bottom", cls: "status-icon icon_pharmacist_review_status"}
        ]
      };
      var pharmacistReviewIcon = new tm.jquery.Image({
        cls:'pharmacist-review-icon',
        cursor: "pointer",
        html: appFactory.createLayersContainerHtml(prIconOptions),
        tooltip: tm.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("pharmacists.review"))
      });
      pharmacistReviewIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component)
      {
        if (self.showRelatedPharmacistReviewsEventCallback)
        {
          self.showRelatedPharmacistReviewsEventCallback(therapyContainer);
        }
      });
      this.add(pharmacistReviewIcon);
    }

    var validToComponent = new tm.jquery.Component({
      cls: "TextData",
      style: "position: absolute; top: 2px; right: 5px;",
      html: therapy.isTaggedForPrescription() ? '<div class="icon_prescription"></div>' + validTo : validTo
    });
    this.add(validToComponent);

    this._appendTherapyTasksReminderContainer(data);
  },

  _appendTherapyTasksReminderContainer: function(data)
  {
    var self = this;
    var container = new tm.jquery.Container({
      cls:'append-task-reminder-container',
      layout: tm.jquery.VFlexboxLayout.create("flex-end", "flex-end"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")
    });
    var tasksContainer = new app.views.medications.TherapyTasksRemindersContainer({
      view: this.getView(),
      therapyData: data,
      tasks: data.tasks,
      offset: -20,
      tasksChangedEvent: function()
      {
        if (self.tasksChangedEventCallback)
        {
          self.tasksChangedEventCallback(self.getTherapyContainer());
        }
      }
    });
    container.add(tasksContainer);
    this.add(container);
  },

  _setEnabledAllActionButtons: function(enabled){
    if (this._editButton != null) this._editButton.setEnabled(enabled);
    if (this._confirmButton != null) this._confirmButton.setEnabled(enabled);
    if (this._abortButton != null) this._abortButton.setEnabled(enabled);
  },

  enableActionButtons: function()
  {
    this._setEnabledAllActionButtons(true);
  },

  disableActionButtons: function()
  {
    this._setEnabledAllActionButtons(false);
  },

  setShowRelatedPharmacistReviewsEventCallback: function(callback){
    this.showRelatedPharmacistReviewsEventCallback = callback;
  },
  setTasksChangedEventCallback: function(callback){
    this.tasksChangedEventCallback = callback;
  },
  setEditTherapyEventCallback: function(callback){
    this.editTherapyEventCallback = callback;
  },
  setConfirmTherapyEventCallback: function(callback){
    this.confirmTherapyEventCallback = callback;
  },
  setAbortTherapyEventCallback: function(callback){
    this.abortTherapyEventCallback = callback;
  }
});