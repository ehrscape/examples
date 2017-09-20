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
Class.define('tm.views.medications.timeline.TimelineTherapyContainerToolbar', 'app.views.medications.common.TherapyContainerToolbar', {
  alignSelf: "flex-start",
  readOnly: false,
  showPharmacistsReviewEventCallback: null,
  confirmTherapyEventCallback: null,
  tasksChangedEventCallback: null,
  nurseResupplyRequestEventCallback: null,
  perfusionSyringeRequestEventCallback: null,
  editTherapyEventCallback: null,
  abortTherapyEventCallback: null,
  tagTherapyForPrescriptionEventCallback: null,
  untagTherapyForPrescriptionEventCallback: null,
  suspendTherapyEventCallback: null,
  copyTherapyEventCallback: null,
  showMedicationInfoCallback: null,
  administerScheduledTaskEventCallback: null,
  administerUnscheduledTaskEventCallback: null,
  adjustRateEventCallback: null,
  infusionSetChangeEventCallback: null,
  editSelfAdministeringCallback: null,
  addBolusEventCallback: null,
  stopFlowCallback: null,
  changeOxygenStartingDeviceCallback: null,

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

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-end"));

    var actionContainerTopRow = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "flex-start")
    });

    var moreActionsButton = new tm.jquery.Button({
      width: 24,
      height: 16,
      cls: 'btn-flat menu-icon',
      testAttribute: 'more-actions-menu-button',
      handler: function(component, componentEvent, elementEvent)
      {
        tm.jquery.ComponentUtils.hideAllDropDownMenus(view);
        var popupMenu = self._createMoreActionsPopupMenu(component, confirmButton);
        if (confirmButton)
        {
          confirmButton.setEnabled(false);
        }
        popupMenu.show(elementEvent);
      }
    });
   
    if (pharmacistReviewReferBack)
    {
      var prIconOptions = {
        background: {cls: "icon_pharmacist_review"},
        layers: [
          {hpos: "right", vpos: "bottom", cls: "status-icon icon_pharmacist_review_status"}]
      };
      var pharmacistReviewIcon = new tm.jquery.Image({
        cursor: "pointer",
        html: appFactory.createLayersContainerHtml(prIconOptions),
        tooltip: tm.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("pharmacists.review"))
      });

      pharmacistReviewIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component)
      {
        if (self.showPharmacistsReviewEventCallback)
        {
          self.showPharmacistsReviewEventCallback(therapyContainer);
        }
      });
      actionContainerTopRow.add(pharmacistReviewIcon);
    }
    else
    {
      var tagIconHtml = '';
      if (therapy.isTaggedForPrescription())
      {
        tagIconHtml = '<div style="display: inline-block; margin-right: 1px;" class="icon_prescription"></div>';
      }

      var tagIconContainer = new tm.jquery.Container({
        cls:"tag-icon-container",
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        width: 16,
        height: 16,
        html: tagIconHtml
      });
      actionContainerTopRow.add(tagIconContainer);
    }
    actionContainerTopRow.add(moreActionsButton);
    this.add(actionContainerTopRow);

    var reissue = data.therapyStatus == enums.therapyStatusEnum.SUSPENDED;
    var showConfirmButton = (reissue || data.doctorReviewNeeded) && !this.readOnly && !pharmacistReviewReferBack &&
        !therapyContainer._isTherapyCancelledOrAborted(data);
    if (showConfirmButton)
    {
      var toolTipTextKey = reissue ? "reissue" : "confirm";

      var enabled = reissue ?
          view.getTherapyAuthority().isDoctorReviewAllowed() :
          view.getTherapyAuthority().isRestartSuspendPrescriptionAllowed()
      var confirmButton = new tm.jquery.Button({
        cls: "btn-flat review-icon review-therapy-button",
        width: 32,
        enabled: enabled,
        alignSelf: "flex-end",
        handler: function()
        {
          if (self.confirmTherapyEventCallback)
          {
            self.confirmTherapyEventCallback(therapyContainer);
          }
        }
      });

      if (!tm.jquery.ClientUserAgent.isTablet())
      {
        var tooltip = new tm.views.medications.timeline.TherapyTimelineTooltip({
          title: view.getDictionary(toolTipTextKey)
        });
        confirmButton.setTooltip(tooltip);
      }

      if (view.isDoctorReviewRequired() === true || reissue === true)
      {
        this.add(confirmButton);
      }
    }
    this._appendTherapyTasksReminderContainer(data);
  },

  _appendTherapyTasksReminderContainer: function(data)
  {
    var self = this;
    var container = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-end", "flex-end"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      cls: "task-reminder-container"
    });
    var tasksContainer = new app.views.medications.TherapyTasksRemindersContainer({
      view: this.getView(),
      therapyData: data,
      tasks: data.tasks,
      offset: 380,
      showPharmacyTasks: true,
      enablePharmacyTasksActions: false,
      showPerfusionSyringeTasks: true,
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

  /**
   *
   * @param {tm.jquery.Component} menuHotSpot Popup menu hotspot
   * @param {tm.jquery.Component} confirmButton Confirm therapy button, will be disabled while popup is shown.
   * @returns {tm.jquery.PopupMenu}
   * @private
   */
  _createMoreActionsPopupMenu: function (menuHotSpot, confirmButton)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var therapyContainer = this.getTherapyContainer();
    var enums = app.views.medications.TherapyEnums;
    var data = therapyContainer.getData();
    var therapy = data.therapy;
    var pharmacistReviewReferBack =
        data.therapyPharmacistReviewStatus == enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK;

    var popupMenu = appFactory.createPopupMenu();
    popupMenu.addTestAttribute(therapy.getTherapyId());
    popupMenu.on(tm.jquery.ComponentEvent.EVENT_TYPE_HIDE, function()
    {
      if (confirmButton)
      {
        confirmButton.setEnabled(true);
      }
    });

    //therapy actions
    if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
    {
      var therapyCancelledOrAborted = therapyContainer._isTherapyCancelledOrAborted(data);

      if (!therapyCancelledOrAborted && this.readOnly !== true)
      {
        var editTherapyItem = new tm.jquery.MenuItem({
          text: view.getDictionary("edit.therapy"),
          iconCls: 'icon-edit',
          enabled: !pharmacistReviewReferBack,
          testAttribute: 'edit-therapy-menu-item',
          handler: function()
          {
            if (self.editTherapyEventCallback)
            {
              self.editTherapyEventCallback(therapyContainer);
            }
          }
        });
        popupMenu.addMenuItem(editTherapyItem);

        if (data.therapyStatus !== app.views.medications.TherapyEnums.therapyStatusEnum.SUSPENDED &&
            view.getTherapyAuthority().isSuspendPrescriptionAllowed())
        {
          var suspendTherapyItem = new tm.jquery.MenuItem({
            text: view.getDictionary("suspend.therapy"),
            iconCls: 'icon-suspend',
            testAttribute: 'suspend-therapy-menu-item',
            handler: function()
            {
              if (self.suspendTherapyEventCallback)
              {
                self.suspendTherapyEventCallback(therapyContainer);
              }
            }
          });
          popupMenu.addMenuItem(suspendTherapyItem);
        }

        if (data.doctorReviewNeeded &&
            (view.getTherapyAuthority().isDoctorReviewAllowed() ||
            view.getTherapyAuthority().isRestartSuspendPrescriptionAllowed()))
        {
          var reissue = data.therapyStatus == enums.therapyStatusEnum.SUSPENDED &&
              view.getTherapyAuthority().isRestartSuspendPrescriptionAllowed();
          var doctorConfirmation = view.isDoctorReviewRequired() === true &&
              view.getTherapyAuthority().isDoctorReviewAllowed();
          var toolTipTextKey =  reissue ? "reissue" : "confirm";

          var confirmTherapyItem = new tm.jquery.MenuItem({
            text: view.getDictionary(toolTipTextKey),
            iconCls: 'icon-confirm',
            testAttribute: reissue ? 'reissue-therapy-menu-item' : 'confirm-therapy-menu-item',
            handler: function()
            {
              if (self.confirmTherapyEventCallback)
              {
                self.confirmTherapyEventCallback(therapyContainer);
              }
            }
          });
          if (reissue || doctorConfirmation)
          {
            popupMenu.addMenuItem(confirmTherapyItem);
          }
        }

        var stopTherapyItem = new tm.jquery.MenuItem({
          text: view.getDictionary("stop.therapy"),
          iconCls: 'icon-delete',
          testAttribute: 'stop-therapy-menu-item',
          handler: function()
          {
            if (self.abortTherapyEventCallback)
            {
              self.abortTherapyEventCallback(therapyContainer);
            }
          }
        });
        popupMenu.addMenuItem(stopTherapyItem);

        var centralCaseExists = view.getCentralCaseData() && view.getCentralCaseData().centralCaseId;

        if (centralCaseExists)
        {
          if (therapy.isTaggedForPrescription())
          {
            var untagTherapyItem = new tm.jquery.MenuItem({
              text: view.getDictionary("untag.therapy.for.prescription"),
              iconCls: 'icon-add-prescription',
              testAttribute: 'untag-therapy-menu-item',
              handler: function()
              {
                if (self.untagTherapyForPrescriptionEventCallback)
                {
                  self.untagTherapyForPrescriptionEventCallback(therapyContainer);
                }
              }
            });
            popupMenu.addMenuItem(untagTherapyItem);
          }
          else
          {
            var tagTherapyItem = new tm.jquery.MenuItem({
              text: view.getDictionary("tag.therapy.for.prescription"),
              iconCls: 'icon-add-prescription',
              testAttribute: 'tag-therapy-menu-item',
              handler: function()
              {
                if (self.tagTherapyForPrescriptionEventCallback)
                {
                  self.tagTherapyForPrescriptionEventCallback(therapyContainer);
                }
              }
            });
            popupMenu.addMenuItem(tagTherapyItem);
          }
        }
      }
    }
    //self administrations action
    if (view.isAutoAdministrationChartingEnabled() &&
        view.getTherapyAuthority().isStartSelfAdministrationAllowed() &&
        !therapyCancelledOrAborted && !therapy.isTitrationDoseType())
    {
      var editSelfAdministering = new tm.jquery.MenuItem({
        text: view.getDictionary("self.administration"),
        iconCls: 'icon-self-administered',
        enabled: true,
        testAttribute: 'self-admission-therapy-menu-item',
        handler: function()
        {
          if (self.editSelfAdministeringCallback)
          {
            self.editSelfAdministeringCallback(therapyContainer);
          }
        }
      });
      popupMenu.addMenuItem(editSelfAdministering);
    }

    //administration/task actions
    if (therapy.isOrderTypeOxygen() && data.infusionActive || therapy.isContinuousInfusion())
    {
      var adjustRateMenuItem = new tm.jquery.MenuItem({
        text: therapy.isOrderTypeOxygen() ? view.getDictionary("adjust.oxygen.rate") :
            view.getDictionary("adjust.infusion.rate"),
        iconCls: 'icon-add',
        testAttribute: 'adjust-therapy-rate-menu-item',
        handler: function()
        {
          if (self.adjustRateEventCallback)
          {
            self.adjustRateEventCallback(therapyContainer);
          }
        }
      });
      popupMenu.addMenuItem(adjustRateMenuItem);

      var stopFlowMenuItem = new tm.jquery.MenuItem({
        text: data.currentInfusionRate === 0 ?
            view.getDictionary('resume.flow.rate') :
            view.getDictionary('pause.flow.rate'),
        iconCls: 'icon-add',
        testAttribute: data.currentInfusionRate === 0 ?
            'resume-therapy-flow-menu-item' :
            'pause-therapy-flow-menu-item',
        handler: function()
        {
          if (data.currentInfusionRate !== 0)
          {
            if (self.stopFlowCallback)
            {
              self.stopFlowCallback(therapyContainer)
            }
          }
          else
          {
            if (self.adjustRateEventCallback)
            {
              self.adjustRateEventCallback(therapyContainer);
            }
          }
        }
      });
      popupMenu.addMenuItem(stopFlowMenuItem);
    }

    if (therapy.isContinuousInfusion())
    {
      var addBolusAdministrationMenuItem = new tm.jquery.MenuItem({
        text: view.getDictionary('add.bolus.administration'),
        iconCls: 'icon-add',
        testAttribute: 'add-bolus-administration-menu-item',
        handler: function()
        {
          if (self.addBolusEventCallback)
          {
            self.addBolusEventCallback(therapyContainer)
          }

        }
      });
      popupMenu.addMenuItem(addBolusAdministrationMenuItem);
    }
    else
    {
      if (therapy.isOrderTypeOxygen())
      {
        if (data.infusionActive)
        {
          var changeOxygenStartingDeviceMenuItem = new tm.jquery.MenuItem({
            text: view.getDictionary("change.device"),
            testAttribute: 'change-oxygen-device-menu-item',
            handler: function()
            {
              if (self.changeOxygenStartingDeviceCallback)
              {
                self.changeOxygenStartingDeviceCallback(therapyContainer);
              }
            }
          });
          popupMenu.addMenuItem(changeOxygenStartingDeviceMenuItem);
        }
      }
      // Only permit the change device and rate options if the oxygen is not a prn prescription or
      // if it is, only when the first (start) administration was performed
      else
      {
        if (view.getTherapyAuthority().isScheduleAdditionalAdministrationAllowed())
        {
          var addTaskMenuItem = new tm.jquery.MenuItem({
            text: view.getDictionary("administration.schedule.additional"),
            iconCls: 'icon-add',
            testAttribute: 'schedule-additional-administration-menu-item',
            handler: function()
            {
              if (self.administerScheduledTaskEventCallback)
              {
                self.administerScheduledTaskEventCallback(therapyContainer);
              }
            }
          });
          popupMenu.addMenuItem(addTaskMenuItem);
        }

      }
      // only permit PRN applications for oxygen when it hasn't already started
      if (!therapy.isOrderTypeOxygen() ||
          (therapy.isOrderTypeOxygen() && therapy.getWhenNeeded() && !data.infusionActive) &&
          view.getTherapyAuthority().isRecordPrnAdministrationAllowed())
      {
        var addAdministrationMenuItem = new tm.jquery.MenuItem({
          text: view.getDictionary("administration.apply.unplanned"),
          iconCls: 'icon-add',
          testAttribute: 'apply-unplanned-administration-menu-item',
          handler: function()
          {
            if (self.administerUnscheduledTaskEventCallback)
            {
              self.administerUnscheduledTaskEventCallback(therapyContainer);
            }
          }
        });
        popupMenu.addMenuItem(addAdministrationMenuItem);
      }
    }

    if (therapy.isOrderTypeComplex())
    {
      var infusionSetChangeMenuItem = new tm.jquery.MenuItem({
        text: view.getDictionary("infusion.set.change"),
        testAttribute: 'change-infusion-set-menu-item',
        handler: function()
        {
          if (self.infusionSetChangeEventCallback)
          {
            self.infusionSetChangeEventCallback(therapyContainer);
          }
        }
      });
      popupMenu.addMenuItem(infusionSetChangeMenuItem);
    }

    // perfusion syringe
    if (!therapy.isOrderTypeOxygen())
    {
      var orderPerfusionSyringeRequest = new tm.jquery.MenuItem({
        iconCls: 'icon-catheter-gray-24',
        text: view.getDictionary("prepare.syringe"),
        testAttribute: 'order-perfusion-syringe-menu-item',
        handler: function()
        {
          if (self.perfusionSyringeRequestEventCallback)
          {
            self.perfusionSyringeRequestEventCallback(menuHotSpot, therapyContainer);
          }
        },
        hidden: !view.getTherapyAuthority().isAddMedicationToPreparationTasklistAllowed()
      });
      popupMenu.addMenuItem(orderPerfusionSyringeRequest);
    }

    //supply actions
    if (!therapyCancelledOrAborted && view.getMedicationsSupplyPresent()
        && view.getTherapyAuthority().isCreateResupplyRequestAllowed())
    {
      var nurseResupplyRequest = new tm.jquery.MenuItem({
        text: view.getDictionary("nurse.resupply.request.button"),
        iconCls: 'icon-nurse-supply',
        testAttribute: 'nurse-resupply-request-menu-item',
        handler: function()
        {
          if (self.nurseResupplyRequestEventCallback)
          {
            self.nurseResupplyRequestEventCallback(therapyContainer);
          }
        }
      });
      popupMenu.addMenuItem(nurseResupplyRequest);
    }

    //therapy actions
    if (view.getTherapyAuthority().isCopyPrescriptionAllowed())
    {
      var menuItemCopySimpleTherapy = new tm.jquery.MenuItem({
        text: view.getDictionary("copy"),
        iconCls: 'icon-copy',
        testAttribute: 'copy-therapy-menu-item',
        handler: function()
        {
          if (self.copyTherapyEventCallback)
          {
            self.copyTherapyEventCallback(therapyContainer);
          }
        }
      });
      popupMenu.addMenuItem(menuItemCopySimpleTherapy);
    }
    var existsMedicationWithId = therapy ? therapy.hasNonUniversalIngredient() : false;

    if (existsMedicationWithId)
    {
      var menuItemShowMedicationInfo = new tm.jquery.MenuItem({
        text: view.getDictionary("drug.information"),
        iconCls: 'icon-info',
        testAttribute: 'drug-information-menu-item',
        handler: function()
        {
          if (self.showMedicationInfoCallback)
          {
            self.showMedicationInfoCallback(therapyContainer);
          }
        }
      });
      popupMenu.addMenuItem(menuItemShowMedicationInfo);
    }
    return popupMenu;
  },

  /**
   * @param {Function} callback
   */
  setShowPharmacistsReviewEventCallback: function(callback)
  {
    this.showPharmacistsReviewEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setConfirmTherapyEventCallback: function(callback)
  {
    this.confirmTherapyEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setTasksChangedEventCallback: function(callback)
  {
    this.tasksChangedEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setNurseResupplyRequestEventCallback: function(callback)
  {
    this.nurseResupplyRequestEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setEditTherapyEventCallback: function(callback)
  {
    this.editTherapyEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setAbortTherapyEventCallback: function(callback)
  {
    this.abortTherapyEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setTagTherapyForPrescriptionEventCallback: function(callback)
  {
    this.tagTherapyForPrescriptionEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setUntagTherapyForPrescriptionEventCallback: function(callback)
  {
    this.untagTherapyForPrescriptionEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setSuspendTherapyEventCallback: function(callback)
  {
    this.suspendTherapyEventCallback = callback;
  }, /**
   * @param {Function} callback
   */

  setCopyTherapyEventCallback: function(callback)
  {
    this.copyTherapyEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setShowMedicationInfoCallback: function(callback)
  {
    this.showMedicationInfoCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setAdministerScheduledTaskEventCallback: function(callback)
  {
    this.administerScheduledTaskEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setAdministerUnscheduledTaskEventCallback: function(callback)
  {
    this.administerUnscheduledTaskEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setAdjustRateEventCallback: function(callback)
  {
    this.adjustRateEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setAddBolusEventCallback: function(callback)
  {
    this.addBolusEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setStopFlowCallback: function(callback)
  {
    this.stopFlowCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setInfusionSetChangeEventCallback: function(callback)
  {
    this.infusionSetChangeEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setEditSelfAdministeringCallback: function(callback)
  {
    this.editSelfAdministeringCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setPerfusionSyringeRequestEventCallback: function(callback)
  {
    this.perfusionSyringeRequestEventCallback = callback;
  },
  /**
   * @param {Function} callback
   */
  setChangeOxygenStartingDeviceCallback: function(callback)
  {
    this.changeOxygenStartingDeviceCallback = callback;
  }
});