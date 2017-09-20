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

Class.define('app.views.medications.TherapyTasksRemindersContainer', 'tm.jquery.Container', {
  cls: "therapy-tasks-reminders",
  therapyData: null, //TherapyDayDto.java
  tasks: null,
  tasksChangedEvent: null,
  offset: null,
  showPharmacyTasks: false,
  showPerfusionSyringeTasks: false,
  enablePharmacyTasksActions: false,
  /** configs */
  view: null,
  /** privates: components */

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(new tm.jquery.HFlexboxLayout({gap: 5}));
    this._presentTasks();
  },

  /** private methods */
  _presentTasks: function()
  {
    var self = this;

    if (this.tasks)
    {
      this.tasks.forEach(function(task)
      {
        self._addTaskButton(task, self.therapyData ? self.therapyData.therapy: null);
      });
    }
  },

  _addTaskButton: function(task, therapy) //TherapyTaskSimpleDto.java , TherapyDto.java
  {
    var enums = app.views.medications.TherapyEnums;

    if (task.taskType == enums.taskTypeEnum.DOCTOR_REVIEW)
    {
      this._showDoctorReminderButton(task);
    }
    else if (task.taskType == enums.taskTypeEnum.SWITCH_TO_ORAL)
    {
      this._showSwitchToOralButton(task);
    }
    else if (this.showPerfusionSyringeTasks && (task.taskType == enums.taskTypeEnum.PERFUSION_SYRINGE_START || task.taskType == enums.taskTypeEnum.PERFUSION_SYRINGE_COMPLETE || task.taskType == enums.taskTypeEnum.PERFUSION_SYRINGE_DISPENSE))
    {
      this._showPerfusionSyringeButton(task);
    }
    else if (this.showPharmacyTasks)
    {
      if (task.taskType == enums.taskTypeEnum.SUPPLY_REMINDER)
      {
        this._showSupplyReminderButton(task, therapy);
      }
      else if (task.taskType == enums.taskTypeEnum.SUPPLY_REVIEW)
      {
        this._showSupplyReviewButton(task, therapy);
      }
    }
  },

  _isTaskLate: function(task)
  {
    return CurrentTime.get().getTime() - new Date(task.dueTime).getTime() > 24 * 60 * 60 * 1000;
  },

  _showPerfusionSyringeButton: function(task)
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;

    var perfusionSyringeButtonIconCls = 'start';

    if (task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_COMPLETE)
    {
      perfusionSyringeButtonIconCls = 'complete';
    }
    else if (task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_DISPENSE)
    {
      perfusionSyringeButtonIconCls = 'dispense';
    }

    var perfusionSyringeButton = new tm.jquery.Button({
      tooltip: tm.views.medications.MedicationUtils.createTooltip(
          this.view.getDictionary("perfusion.syringe.preparation"), "left", this.view),
      cls: "btn-flat perfusion-syringe-icon " + perfusionSyringeButtonIconCls,
      width: 32,
      handler: function(component, componentEvent, elementEvent)
      {
        self._showPerfusionSyringeMenuPopup(task, component, elementEvent);
      },
      enabled: view.getTherapyAuthority().isAddMedicationToPreparationTasklistAllowed()
    });
    this.add(perfusionSyringeButton);
  },

  _showSwitchToOralButton: function(task)
  {
    var self = this;
    var isLate = this._isTaskLate(task);
    var switchToOralIconCls = isLate ? 'icon-switch-to-oral-late' : 'icon-switch-to-oral';
    var tooltipText = this.view.getDictionary("switch.IV.to.oral") + " " + this.view.getDisplayableValue(new Date(task.dueTime), "short.date");
    var switchToOralButton = new tm.jquery.Button({
      tooltip: tm.views.medications.MedicationUtils.createTooltip(tooltipText, "left", this.view),
      cls: "btn-flat switch-to-oral-button " + switchToOralIconCls,
      width: 32,
      handler: function(component, componentEvent, elementEvent)
      {
        var appFactory = self.view.getAppFactory();

        var popupMenu = appFactory.createPopupMenu();

        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: self.view.getDictionary("switch.IV.to.oral"),
          handler: function()
          {
            self.view.showEditTherapyDialog(self.therapyData.therapy, false, self.therapyData.modified);
          },
          iconCls: 'icon-switch-to-oral'
        }));
        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: self.view.getDictionary("dismiss"),
          handler: function()
          {
            self._deleteTask(task.id);
          },
          iconCls: 'icon-delete'
        }));
        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: self.view.getDictionary("dismiss.and.remind"),
          handler: function(component, componentEvent, elementEvent)
          {
            elementEvent.stopPropagation();
            self._showDaysContainer(component, task);
          },
          iconCls: 'icon-reprompt'
        }), true);
        popupMenu.show(elementEvent);
      }
    });
    this.add(switchToOralButton);
  },

  _showDoctorReminderButton: function(task)
  {
    var self = this;
    var isLate = this._isTaskLate(task);
    var doctorReminderIconCls = isLate ? 'icon-doctor-review-reminder-late' : 'icon-doctor-review-reminder';
    var tooltipText = this.view.getDictionary("reminder") + " " + this.view.getDisplayableValue(new Date(task.dueTime), "short.date");
    var doctorReminderButton = new tm.jquery.Button({
      tooltip: tm.views.medications.MedicationUtils.createTooltip(tooltipText, "left", this.view),
      cls: "btn-flat doctor-review-reminder-button " + doctorReminderIconCls,
      width: 32,
      handler: function(component, componentEvent, elementEvent)
      {
        var appFactory = self.view.getAppFactory();

        var popupMenu = appFactory.createPopupMenu();

        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: self.view.getDictionary('edit.therapy'),
          handler: function()
          {
            self.view.showEditTherapyDialog(self.therapyData.therapy, false, self.therapyData.modified);
          },
          iconCls: 'icon-edit'
        }));
        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: self.view.getDictionary("dismiss"),
          handler: function()
          {
            self._deleteTask(task.id);
          },
          iconCls: 'icon-delete'
        }));
        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: self.view.getDictionary("dismiss.and.remind"),
          handler: function(component, componentEvent, elementEvent)
          {
            elementEvent.stopPropagation();
            self._showDaysContainer(component, task);
          },
          iconCls: 'icon-reprompt'
        }), true);
        popupMenu.show(elementEvent);
      }
    });
    this.add(doctorReminderButton);
  },

  _showDaysContainer: function(menuItem, task)
  {
    var self = this;
    var $renderToElement = $("<placeholder>");
    var container = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      renderToElement: $renderToElement[0]
    });

    container.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary("review.in"), '5 0 0 39'));

    var rowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start")
    });
    container.add(rowContainer);

    var daysField = new tm.jquery.NumberField({
      cls: 'field-flat',
      width: 50,
      margin: "0 0 0 39",
      maxLength: 3,
      formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 0},
      placeholder: "Days",
      value: null,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    daysField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      elementEvent.stopPropagation();
      daysField.focus();
    });
    rowContainer.add(daysField);

    var confirmButton = new tm.jquery.Button({
      cls: 'confirm-button btn-flat',
      text: this.view.getDictionary("confirm")
    });
    confirmButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      elementEvent.stopPropagation();
      confirmButton.focus();
      var days = daysField.getValue();
      if (days)
      {
        self._rescheduleTask(task, days);
      }
    });
    rowContainer.add(confirmButton);

    $(menuItem.getDom()).append($renderToElement);
    container.doRender();
    daysField.focus();
  },

  _deleteTask: function(taskId)
  {
    var self = this;
    var view = this.view;
    var params = {
      patientId: this.view.getPatientId(),
      taskId: taskId
    };

    var deleteUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DELETE_TASK;
    view.showLoaderMask();
    this.view.loadPostViewData(deleteUrl, params, null,
        function()
        {
          view.hideLoaderMask();
          self.tasksChangedEvent();
        });
  },

  _deletePerfusionSyringeTask: function(taskId)
  {
    var self = this;
    var view = this.view;
    var params = {
      taskId: taskId
    };

    var deleteUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DISMISS_THERAPY_PERFUSION_SYRINGE;
    view.showLoaderMask();
    this.view.loadPostViewData(deleteUrl, params, null,
        function()
        {
          view.hideLoaderMask();
          self.tasksChangedEvent();
        });
  },

  _dismissNurseRequestedSupply: function(taskId)
  {
    var self = this;
    var view = this.view;
    var params = {
      patientId: this.view.getPatientId(),
      taskId: taskId
    };

    var deleteUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DISMISS_NURSE_SUPPLY_TASK;
    view.showLoaderMask();
    this.view.loadPostViewData(deleteUrl, params, null,
        function ()
        {
          view.hideLoaderMask();
          self.tasksChangedEvent();
        });
  },

  _dismissPharmacistSupply: function (taskId)
  {
    var self = this;
    var view = this.view;
    var params = {
      patientId: this.view.getPatientId(),
      taskId: taskId
    };

    var deleteUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DISMISS_PHARMACIST_SUPPLY_TASK;
    view.showLoaderMask();
    this.view.loadPostViewData(deleteUrl, params, null,
        function ()
        {
          view.hideLoaderMask();
          self.tasksChangedEvent();
        });
  },

  _rescheduleTask: function(task, days)
  {
    var self = this;
    var now = CurrentTime.get();

    this.getView().getRestApi().rescheduleTasks(
        task.id,
        JSON.stringify(new Date(now.getFullYear(), now.getMonth(), now.getDate() + days, 0, 0, 0, 0)),
        task.therapyId,
        true,
        false).then(
        function()
        {
          tm.jquery.ComponentUtils.hideAllDropDownMenus(self.getView());
          self.tasksChangedEvent();
        }
    );
  },

  _showSupplyReminderButton: function(task, therapy)
  {
    var self = this;
    var tooltipText = this.view.getDictionary("supply") + " " + this.view.getDisplayableValue(new Date(task.dueTime), "short.date");
    var supplyReminderButton = new tm.jquery.Button({
      tooltip: tm.views.medications.MedicationUtils.createTooltip(tooltipText, "left", this.view),
      cls: "btn-flat doctor-review-reminder-button icon-supply-reminder",
      width: 32,
      margin: "0 -16 0 0",
      handler: function(component, componentEvent, elementEvent)
      {
        if (self.enablePharmacyTasksActions)
        {
          self._showSupplyReminderMenuPopup(task, therapy, supplyReminderButton, elementEvent);
        }
      }
    });
    var dueTimeMoment = moment(new Date(task.dueTime));
    var nowMoment = moment(CurrentTime.get()).startOf('day');
    var daysUntilSupply = dueTimeMoment.diff(nowMoment, 'days');
    this.add(supplyReminderButton);
    this.add(new tm.jquery.Container({cls: 'icon_supply_days', html: daysUntilSupply, width: 16}));
  },

  _loadSupplyTask: function(task, callback)
  {
    var params = {taskId: task.id};
    var getSupplyTaskUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_PHARMACIST_SUPPLY_SIMPLE_TASK;
    this.view.loadViewData(getSupplyTaskUrl, params, null, function(data)
    {
      callback(data);
    });
  },

  _showNurseSupplyMenuPopup: function(task, therapy, supplyReviewButton, elementEvent)
  {
    var self = this;
    var appFactory = self.view.getAppFactory();

    var popupMenu = appFactory.createPopupMenu();

    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      text: this.view.getDictionary("confirm"),
      handler: function()
      {
        self._confirmSupplyReviewTask(false, task.id, therapy, null, null, null, null);
      },
      iconCls: 'icon-confirm'
    }), true);
    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      text: this.view.getDictionary('confirm.and.remind'),
      handler: function()
      {
        tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
        self._showConfirmOrEditSupplyPopup(task, therapy, supplyReviewButton, false);
      },
      iconCls: 'icon-supply-reminder'
    }));
    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      text: this.view.getDictionary("dismiss"),
      handler: function()
      {
        self._dismissNurseRequestedSupply(task.id);
      },
      iconCls: 'icon-delete'
    }));

    popupMenu.show(elementEvent);
  },

  _showSupplyReminderMenuPopup: function (task, therapy, supplyReminderButton, elementEvent)
  {
    var self = this;
    var popupMenu = self.view.getAppFactory().createPopupMenu();

    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      text: self.view.getDictionary('confirm'),
      handler: function ()
      {
        tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
        self._showConfirmOrEditSupplyPopup(task, therapy, supplyReminderButton, false);
      },
      iconCls: 'icon-confirm'
    }), true);
    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      text: self.view.getDictionary('edit'),
      handler: function ()
      {
        tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
        self._showConfirmOrEditSupplyPopup(task, therapy, supplyReminderButton, true);
      },
      iconCls: 'icon-supply-reminder'
    }));
    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      text: self.view.getDictionary('dismiss'),
      handler: function()
      {
        self._dismissPharmacistSupply(task.id)
      },
      iconCls: 'icon-delete'
    }));
    popupMenu.show(elementEvent);
  },

  _showConfirmOrEditSupplyPopup: function(task, therapy, button, isEditMode)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;

    var confirmSupplyRequestPane = new app.views.common.containers.AppDataEntryContainer({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      padding: 5,
      processResultData: function(resultCallback)
      {
        var selectedButton = supplyTypeButtonGroup.getActiveRadioButton();
        var supplyInDays = daysField.getValue();
        if (selectedButton && supplyInDays)
        {
          var comment = commentField.getValue();

          if (task.taskType == enums.taskTypeEnum.SUPPLY_REMINDER)
          {
            if (isEditMode == true)
            {
              self._editSupplyReminderTask(
                  task.id,
                  selectedButton.supplyType,
                  supplyInDays,
                  comment ? comment : null,
                  resultCallback);
            }
            else
            {
              self._confirmSupplyReminderTask(
                  task.id,
                  therapy,
                  selectedButton.supplyType,
                  supplyInDays,
                  comment ? comment : null,
                  resultCallback);
            }
          }
          else if (task.taskType == enums.taskTypeEnum.SUPPLY_REVIEW)
          {
            self._confirmSupplyReviewTask(
                true,
                task.id,
                therapy,
                selectedButton.supplyType,
                supplyInDays,
                comment ? comment : null,
                resultCallback);
          }
        }
        else
        {
          resultCallback(new app.views.common.AppResultData({success: false}));
        }
      }
    });

    var firstRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start")
    });

    var patientsOwnButton = new tm.jquery.RadioButton({
      labelText: this.view.getDictionary('patients.own'),
      supplyType: enums.medicationSupplyTypeEnum.PATIENTS_OWN,
      labelAlign: "right",
      width: 130
    });
    var oneStopButton = new tm.jquery.RadioButton({
      labelText: this.view.getDictionary('one.stop.supply'),
      supplyType: enums.medicationSupplyTypeEnum.ONE_STOP_DISPENSING,
      labelAlign: "right",
      width: 130
    });
    var nonStockButton = new tm.jquery.RadioButton({
      labelText: this.view.getDictionary('non.stock'),
      supplyType: enums.medicationSupplyTypeEnum.NON_WARD_STOCK,
      labelAlign: "right",
      width: 130
    });
    var supplyTypeButtonGroup = new tm.jquery.RadioButtonGroup();
    supplyTypeButtonGroup.add(patientsOwnButton);
    supplyTypeButtonGroup.add(oneStopButton);
    supplyTypeButtonGroup.add(nonStockButton);

    firstRowContainer.add(patientsOwnButton);
    firstRowContainer.add(oneStopButton);
    confirmSupplyRequestPane.add(firstRowContainer);

    var secondRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start")
    });
    secondRowContainer.add(nonStockButton);

    var daysField = new tm.jquery.NumberField({
      cls: 'field-flat',
      width: 70,
      margin: "0 5 0 5",
      maxLength: 3,
      formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 0},
      value: null,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    secondRowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', this.view.getDictionary("for"), '5 0 0 0'));
    secondRowContainer.add(daysField);
    secondRowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', this.view.getDictionary("days"), '5 0 0 0'));
    confirmSupplyRequestPane.add(secondRowContainer);

    var thirdRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start")
    });
    var commentField = new tm.jquery.TextField({cls: 'field-flat'});
    thirdRowContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', this.view.getDictionary("commentary"), '5 0 0 0'));
    thirdRowContainer.add(commentField);
    confirmSupplyRequestPane.add(thirdRowContainer);

    var tooltipTitle = this.view.getDictionary('review.nurse.supply.request');
    if (task.taskType == enums.taskTypeEnum.SUPPLY_REMINDER)
    {
     tooltipTitle = isEditMode == true ? "Edit supply request" : "Create supply request";
    }

    var tooltip = appFactory.createDataEntryPopoverTooltip(
        tooltipTitle,  //TODO TMC-7374  / TMC-7376
        confirmSupplyRequestPane,
        function() {}
    );

    tooltip.setWidth(400);
    tooltip.setHeight(185);
    tooltip.setTrigger("manual");
    button.setTooltip(tooltip);

    confirmSupplyRequestPane.onKey(
        new tm.jquery.event.KeyStroke({key: "esc", altKey: false, ctrlKey: false, shiftKey: false}),
        function()
        {
          tooltip.cancel();
        }
    );
    confirmSupplyRequestPane.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      component.focus();
    });

    if (task.taskType == enums.taskTypeEnum.SUPPLY_REMINDER)
    {
      this._loadSupplyTask(task, function(supplyTask)
      {
        if (supplyTask)
        {
          daysField.setValue(supplyTask.supplyInDays);
          supplyTypeButtonGroup.getRadioButtons().forEach(function(radioButton)
          {
            if (radioButton.supplyType == supplyTask.supplyTypeEnum)
            {
              supplyTypeButtonGroup.setActiveRadioButton(radioButton);
            }
          });
          setTimeout(function()
          {
            tooltip.show();
          }, 10);
        }
        else
        {
          self.view.getAppNotifier().error(
              self.view.getDictionary('task.not.found'),
              app.views.common.AppNotifierDisplayType.SWING_HTML,
              300, 160
          );
        }
      });
    }
    else if (task.taskType == enums.taskTypeEnum.SUPPLY_REVIEW)
    {
      this._loadSupplyTask(task, function(supplyTask)
      {
        if (supplyTask)
        {
          if (supplyTask.alreadyDispensed === true)
          {
            self.view.getAppNotifier().error(
                self.view.getDictionary('task.already.dispensed'),
                app.views.common.AppNotifierDisplayType.SWING_HTML,
                300, 160
            );
          }
          else
          {
            setTimeout(function()
            {
              tooltip.show();
            }, 10);
          }
        }
        else
        {
          self.view.getAppNotifier().error(
              self.view.getDictionary('task.not.found'),
              app.views.common.AppNotifierDisplayType.SWING_HTML,
              300, 160
          );
        }
      });
    }
    else
    {
      setTimeout(function()
      {
        tooltip.show();
      }, 10);
    }
  },

  _confirmSupplyReminderTask: function(taskId, therapy, supplyType, supplyInDays, comment, resultCallback)
  {
    var self = this;
    var params = {
      patientId: this.view.getPatientId(),
      taskId: taskId,
      compositionUid: therapy.compositionUid,
      supplyType: supplyType,
      supplyInDays: supplyInDays,
      comment: comment
    };

    var url = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_CONFIRM_SUPPLY_REMINDER_TASK;
    this.view.loadPostViewData(url, params, null,
        function()
        {
          tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
          self.tasksChangedEvent();
          resultCallback(new app.views.common.AppResultData({success: true}));
        },
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: false}));
        });
  },

  _editSupplyReminderTask: function(taskId, supplyType, supplyInDays, comment, resultCallback)
  {
    var self = this;
    var params = {
      patientId: this.view.getPatientId(),
      taskId: taskId,
      supplyType: supplyType,
      supplyInDays: supplyInDays,
      comment: comment
    };

    var url = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_EDIT_SUPPLY_REMINDER_TASK;
    this.view.loadPostViewData(url, params, null,
        function()
        {
          tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
          self.tasksChangedEvent();
          resultCallback(new app.views.common.AppResultData({success: true}));
        },
        function()
        {
          resultCallback(new app.views.common.AppResultData({success: false}));
        });
  },

  _confirmSupplyReviewTask: function(createSupplyReminder, taskId, therapy, supplyType, supplyInDays, comment, resultCallback)
  {
    var self = this;
    var params = {
      patientId: this.view.getPatientId(),
      taskId: taskId,
      compositionUid: therapy.compositionUid,
      createSupplyReminder: createSupplyReminder,
      supplyType: supplyType,
      supplyInDays: supplyInDays,
      comment: comment
    };

    var url = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_CONFIRM_SUPPLY_REVIEW_TASK;
    this.view.loadPostViewData(url, params, null,
        function()
        {
          tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
          self.tasksChangedEvent();
          if (resultCallback)
          {
            resultCallback(new app.views.common.AppResultData({success: true}));
          }
        },
        function()
        {
          if (resultCallback)
          {
            resultCallback(new app.views.common.AppResultData({success: false}));
          }
        });
  },

  _showSupplyReviewButton: function(task, therapy)
  {
    var self = this;
    var tooltipText = this.view.getDictionary("nurse.resupply.request");
    var supplyReviewButton = new tm.jquery.Button({
      tooltip: tm.views.medications.MedicationUtils.createTooltip(tooltipText, "left", this.view),
      cls: "btn-flat icon-nurse-supply",
      width: 32,
      handler: function(component, componentEvent, elementEvent)
      {
        if (self.enablePharmacyTasksActions)
        {
          self._showNurseSupplyMenuPopup(task, therapy, supplyReviewButton, elementEvent);
        }
      }
    });
    this.add(supplyReviewButton);
  },

  _showPerfusionSyringeMenuPopup: function(task, component, elementEvent)
  {
    var self = this;
    var appFactory = self.view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;
    var popupMenu = appFactory.createPopupMenu();

    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      text: this.view.getDictionary("edit"),
      enabled: task.taskType == enums.taskTypeEnum.PERFUSION_SYRINGE_START,
      handler: function()
      {
        self._showPerfusionSyringePopoverEditTooltip(component, task);
      },
      iconCls: 'icon-edit'
    }), true);
    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      text: this.view.getDictionary('dismiss'),
      enabled: task.taskType == enums.taskTypeEnum.PERFUSION_SYRINGE_START,
      handler: function()
      {
        tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
        self._deletePerfusionSyringeTask(task.id);
      },
      iconCls: 'icon-delete'
    }));

    popupMenu.show(elementEvent);
  },

  _showPerfusionSyringePopoverEditTooltip: function(menuHotSpot, task)
  {
    var self = this;
    var view = this.view;
    var appFactory = view.getAppFactory();

    var params = {
      taskId: task.id
    };

    var getTaskUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_PERFUSION_SYRINGE_TASK;
    view.showLoaderMask();
    view.loadViewData(getTaskUrl, params, null,
        function(data)
        {
          view.hideLoaderMask();

          // drop callback if the original task container is not rendered anymore
          if (!self.isRendered()) return;

          if (tm.jquery.Utils.isEmpty(data)) return;

          var entryContainer = new app.views.medications.common.PerfusionSyringeDataEntryContainer({
            view: view,
            task: data
          });

          var popoverTooltip = appFactory.createDataEntryPopoverTooltip(
              view.getDictionary("edit.perfusion.syringe.task"),
              entryContainer,
              function(resultData)
              {
                if (resultData && (resultData).success)
                {
                  var value = resultData.value;
                  if (!tm.jquery.Utils.isEmpty(value))
                  {
                    self._editPerfusionSyringeTask(
                        task.id,
                        value.count,
                        value.urgent,
                        value.orderDate,
                        value.printSystemLabel);
                  }
                }
              }
          );

          entryContainer.onKey(
              new tm.jquery.event.KeyStroke({key: "esc", altKey: false, ctrlKey: false, shiftKey: false}),
              function()
              {
                popoverTooltip.cancel();
              }
          );

          popoverTooltip.setTrigger('manual');
          popoverTooltip.setWidth(entryContainer.getDefaultWidth());
          popoverTooltip.setHeight(entryContainer.getDefaultHeight());
          menuHotSpot.setTooltip(popoverTooltip);

          setTimeout(function()
          {
            popoverTooltip.show();
          }, 0);
        });
  },

  _editPerfusionSyringeTask: function(taskId, numberOfSyringes, urgent, dueTime, printSystemLabel)
  {
    var self = this;
    var view = this.view;

    var params = {
      taskId: taskId,
      numberOfSyringes: numberOfSyringes,
      urgent: urgent,
      dueTime: JSON.stringify(dueTime),
      printSystemLabel: printSystemLabel
    };

    var editTaskUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_EDIT_PERFUSION_SYRINGE_TASK;
    view.showLoaderMask();
    view.loadPostViewData(editTaskUrl, params, null,
        function(data)
        {
          view.hideLoaderMask();
          self.tasksChangedEvent();
        });
  },

  getView: function()
  {
    return this.view;
  }
});

