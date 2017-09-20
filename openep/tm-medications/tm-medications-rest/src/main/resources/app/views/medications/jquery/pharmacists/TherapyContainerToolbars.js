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
Class.define('app.views.medications.pharmacists.TherapyContainerReviewViewToolbar', 'app.views.medications.common.TherapyContainerToolbar', {
  /* event handler callbacks */
  addToIconClickCallback: null,
  tasksChangedCallback: null,
  alignSelf: "flex-start",

  /* override */
  _buildGUI: function()
  {
    var self = this;
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-end", "stretch", 0));

    
    if (this.getView().getTherapyAuthority().isManagePatientPharmacistReviewAllowed() &&
        !this.getTherapyContainer()._isTherapyCancelledOrAborted(this.getTherapyData()))
    {
      var addToIcon = new tm.jquery.Image({
        cls: "add-to-icon",
        width: 32,
        height: 32,
        alignSelf: "flex-end"
      });

      addToIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component)
      {
        if (!tm.jquery.Utils.isEmpty(self.addToIconClickCallback))
        {
          self.addToIconClickCallback(self.getTherapyContainer());
        }
      });

      this.add(addToIcon);
      var therapyData = this.getTherapyData();
      this._addTherapyTasks(therapyData);
    }
  },

  _addTherapyTasks: function(therapyData)
  {
    var self = this;
    var container = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-end", "flex-end"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      padding: 5
    });
    var tasksContainer = new app.views.medications.TherapyTasksRemindersContainer({
      view: this.getView(),
      therapyData: therapyData,
      tasks: therapyData.tasks,
      offset: 380,
      showPharmacyTasks: true,
      enablePharmacyTasksActions: true,
      tasksChangedEvent: function()
      {
        self.tasksChangedCallback();
      }
    });
    container.add(tasksContainer);
    this.add(container);
  },

  setAddToIconClickCallback: function(callback)
  {
    this.addToIconClickCallback = callback;
  },

  setTasksChangedCallback: function(callback)
  {
    this.tasksChangedCallback = callback;
  }
});
Class.define('app.views.medications.pharmacists.TherapyContainerEditReviewContentToolbar', 'app.views.medications.common.TherapyContainerToolbar', {
  /* event handler callbacks */
  editTherapyEventCallback: null,
  abortTherapyEventCallback: null,
  suspendTherapyEventCallback: null,
  removeTherapyEventCallback: null,
  revertAbortOrSuspendEventCallback: null,

  editTherapyButton: null,
  abortTherapyButton: null,
  suspendTherapyButton: null,

  /* override */
  _buildGUI: function()
  {
    var self = this;
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-end", "center", 0));


    if (this.getView().getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
    {
      var editTherapyButton = new tm.jquery.Button({
        margin: "0 0 0 5",
        text: this.getView().getDictionary('edit.therapy'),
        alignSelf: 'center',
        handler: function (component)
        {
          if (!tm.jquery.Utils.isEmpty(self.editTherapyEventCallback))
          {
            self.editTherapyEventCallback(self.getTherapyContainer());
          }
        }
      });
      var abortTherapyButton = new tm.jquery.Button({
        margin: "0 0 0 5",
        text: this.getView().getDictionary('stop.therapy'),
        alignSelf: 'center',
        handler: function (component)
        {
          if (!tm.jquery.Utils.isEmpty(self.abortTherapyEventCallback))
          {
            self.abortTherapyEventCallback(self.getTherapyContainer());
            self._handleTherapyButtonsVisibility(true);
          }
        }
      });
      this.add(editTherapyButton);
      this.add(abortTherapyButton);

      this.editTherapyButton = editTherapyButton;
      this.abortTherapyButton = abortTherapyButton;
    }
    if (this.getView().getTherapyAuthority().isSuspendPrescriptionAllowed())
    {
      var suspendTherapyButton = new tm.jquery.Button({
        margin: "0 0 0 5",
        text: this.getView().getDictionary('suspend'),
        alignSelf: 'center',
        handler: function (component)
        {
          if (!tm.jquery.Utils.isEmpty(self.suspendTherapyEventCallback))
          {
            self.suspendTherapyEventCallback(self.getTherapyContainer());
            self._handleTherapyButtonsVisibility(true);
          }
        }
      });
      this.add(suspendTherapyButton);

      this.suspendTherapyButton = suspendTherapyButton;
    }

    var popupMenuHotSpot = new tm.jquery.Image({
      cls: 'more-actions-icon',
      width: 32,
      height: 24,
      cursor: 'pointer',
      alignSelf: 'flex-start'
    });
    popupMenuHotSpot.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component, componentEvent, elementEvent)
    {
      var popupMenu = self._createPopupMenu();
      popupMenu.show(elementEvent);
    });
    this.add(popupMenuHotSpot);
  },

  _createPopupMenu: function (editMode)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var data = this.getTherapyData();
    var popupMenu = appFactory.createPopupMenu();

    if (data.changeType != app.views.medications.TherapyEnums.pharmacistTherapyChangeType.ABORT &&
        data.changeType != app.views.medications.TherapyEnums.pharmacistTherapyChangeType.SUSPEND)
    {
      if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
      {
        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: view.getDictionary('edit.therapy'),
          handler: function()
          {
            if (!tm.jquery.Utils.isEmpty(self.editTherapyEventCallback))
            {
              self.editTherapyEventCallback(self.getTherapyContainer());
            }
          },
          iconCls: 'icon-edit'
        }));

        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: view.getDictionary('stop.therapy'),
          handler: function()
          {
            if (!tm.jquery.Utils.isEmpty(self.abortTherapyEventCallback))
            {
              self.abortTherapyEventCallback(self.getTherapyContainer());
              self._handleTherapyButtonsVisibility(true);
            }
          },
          iconCls: 'icon-delete'
        }));
      }
      if (view.getTherapyAuthority().isSuspendPrescriptionAllowed())
      {
        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: view.getDictionary('suspend'),
          handler: function()
          {
            if (!tm.jquery.Utils.isEmpty(self.suspendTherapyEventCallback))
            {
              self.suspendTherapyEventCallback(self.getTherapyContainer());
              self._handleTherapyButtonsVisibility(true);
            }
          }
        }));
      }
    }
    else if ((data.changeType != app.views.medications.TherapyEnums.pharmacistTherapyChangeType.ABORT &&
        view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed()) ||
        (data.changeType != app.views.medications.TherapyEnums.pharmacistTherapyChangeType.SUSPEND &&
        view.getTherapyAuthority().isSuspendPrescriptionAllowed()))
    {
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        text: view.getDictionary('undo.change'),
        handler: function()
        {
          self._handleTherapyButtonsVisibility(false);
          if (!tm.jquery.Utils.isEmpty(self.revertAbortOrSuspendEventCallback))
          {
            self.revertAbortOrSuspendEventCallback(self.getTherapyContainer());
          }
        },
        iconCls: 'icon-revert'
      }));
    }

    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      text: view.getDictionary('deselect.therapy'),
      handler: function ()
      {
        if (!tm.jquery.Utils.isEmpty(self.removeTherapyEventCallback))
        {
          self.removeTherapyEventCallback(self.getTherapyContainer());
        }
      }
    }));

    return popupMenu;
  },

  _handleTherapyButtonsVisibility: function(hide)
  {
    if (this.editTherapyButton)
    {
      if (hide)
      {
        this.editTherapyButton.hide();
      }
      else
      {
        this.editTherapyButton.show();
      }

    }
    if (this.abortTherapyButton)
    {
      if (hide)
      {
        this.abortTherapyButton.hide();
      }
      else
      {
        this.abortTherapyButton.show();
      }

    }
    if (this.suspendTherapyButton)
    {
      if (hide)
      {
        this.suspendTherapyButton.hide();
      }
      else
      {
        this.suspendTherapyButton.show();
      }
    }
  },

  setRemoveTherapyEventCallback: function(callback)
  {
    this.removeTherapyEventCallback = callback;
  },

  setEditTherapyEventCallback: function(callback)
  {
    this.editTherapyEventCallback = callback;
  },

  setAbortTherapyEventCallback: function(callback)
  {
    this.abortTherapyEventCallback = callback;
  },

  setSuspendTherapyEventCallback: function (callback)
  {
    this.suspendTherapyEventCallback = callback;
  },

  setRevertAbortOrSuspendEventCallback: function(callback)
  {
    this.revertAbortOrSuspendEventCallback = callback;
  }
});
Class.define('app.views.medications.pharmacists.TherapyContainerFlowViewReviewPaneToolbar', 'app.views.medications.common.TherapyContainerToolbar', {
  acceptChangeEventCallback: null,
  denyChangeEventCallback: null,
  editTherapyEventCallback: null,
  reissueTherapyEventCallback: null,
  copyTherapyEventCallback: null,
  abortSuspendedTherapyEventCallback: null,

  acceptButton: null,
  denyButton: null,
  editButton: null,
  abortButton: null,
  resultDisplayComponent: null,
  changeType: null,

  /* override */
  _buildGUI: function()
  {
    var self = this;
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-end", "center", 0));

    var acceptChangeMenu = new tm.jquery.Button({
      tooltip: tm.views.medications.MedicationUtils.createTooltip(
          self.getView().getDictionary('accept'), "bottom", this.view),
      margin: "0 0 0 5",
      alignSelf: 'center',
      iconCls: 'icon-finish-24',
      handler: function ()
      {
        acceptChangeMenu.setEnabled(false);
        if (!tm.jquery.Utils.isEmpty(self.acceptChangeEventCallback))
        {
          self.acceptChangeEventCallback(self.getTherapyContainer());
        }
      }
    });

    var denyChangeButton = new tm.jquery.Button({
      margin: "0 0 0 5",
      text: this._isChangeTypeSuspendOrAbort() ? this.getView().getDictionary('reissue') : this.getView().getDictionary('deny'),
      alignSelf: 'center',
      handler: function (component)
      {
        if (self.changeType == "SUSPEND")
        {
          self.markReissued();
          if (!tm.jquery.Utils.isEmpty(self.reissueTherapyEventCallback))
          {
            self.reissueTherapyEventCallback(self.getTherapyContainer());
          }
        }
        else if (self.changeType == "ABORT")
        {
          if (!tm.jquery.Utils.isEmpty(self.copyTherapyEventCallback))
          {
            self.copyTherapyEventCallback(self.getTherapyContainer());
          }
        }
        else
        {
          self.markDenied();
          if (!tm.jquery.Utils.isEmpty(self.denyChangeEventCallback))
          {
            self.denyChangeEventCallback(self.getTherapyContainer());
          }
        }
      }
    });

    var editTherapyButton = new tm.jquery.Button({
      margin: "0 0 0 5",
      text: this.getView().getDictionary('edit'),
      alignSelf: 'center',
      handler: function (component)
      {
        if (!tm.jquery.Utils.isEmpty(self.editTherapyEventCallback))
        {
          self.editTherapyEventCallback(self.getTherapyContainer());
        }
      }
    });

    var abortSuspendedTherapyButton = new tm.jquery.Button({
      margin: "0 0 0 5",
      text: this.getView().getDictionary('stop.therapy.short'),
      alignSelf: 'center',
      handler: function (component)
      {
        if (!tm.jquery.Utils.isEmpty(self.abortSuspendedTherapyEventCallback))
        {
          self.abortSuspendedTherapyEventCallback(self.getTherapyContainer());
        }
      }
    });

    var resultDisplayComponent = new tm.jquery.Component({
      cls: "TextDataBold",
      html: null,
      hidden: true
    });

    this.add(acceptChangeMenu);
    this.add(denyChangeButton);
    this.add(denyChangeButton);
    if (this.changeType == "SUSPEND")
    {
      this.add(abortSuspendedTherapyButton);
    }
    if(this.changeType != "ABORT")
    {
      this.add(editTherapyButton);
    }
    this.add(resultDisplayComponent);

    this.resultDisplayComponent = resultDisplayComponent;
    this.acceptButton = acceptChangeMenu;
    this.denyButton = denyChangeButton;
    this.editButton = editTherapyButton;
    this.abortButton = abortSuspendedTherapyButton;
  },

  _mark: function (text)
  {
    this.acceptButton.hide();
    this.denyButton.hide();
    if (this.changeType != "ABORT")
    {
      this.editButton.hide();
    }
    if (this.changeType == "SUSPEND")
    {
      this.abortButton.hide();
    }
    this.resultDisplayComponent.setHtml(text);
    this.resultDisplayComponent.show();
  },

  markAccepted: function(){
    this._mark(this.getView().getDictionary("accepted"));
  },
  markDenied: function(){
    this._mark(this.getView().getDictionary("denied"));
  },
  markEdited: function() {
    this._mark(this.getView().getDictionary("modified"));
  },
  markReissued: function() {
    this._mark(this.getView().getDictionary("reissued"));
  },
  markCopied: function() {
    this._mark(this.getView().getDictionary("copied"));
  },
  markAborted: function() {
    this._mark(this.getView().getDictionary("stop.past"));
  },
  clearMark: function()
  {
    this.resultDisplayComponent.setHtml(null);
    this.resultDisplayComponent.hide();
    this.acceptButton.show();
    this.acceptButton.setEnabled(true);
    this.denyButton.show();
    this.denyButton.setEnabled(true);
    if (this.changeType != "ABORT")
    {
      this.editButton.show();
      this.editButton.setEnabled(true);
    }
    if (this.changeType == "SUSPEND")
    {
      this.abortButton.show();
      this.abortButton.setEnabled(true);
    }
  },

  _isChangeTypeSuspendOrAbort: function()
  {
    return !!(this.changeType == "SUSPEND" || this.changeType == "ABORT");
  },
  setAcceptChangeEventCallback: function(callback)
  {
    this.acceptChangeEventCallback = callback;
  },
  setDenyChangeEventCallback: function(callback)
  {
    this.denyChangeEventCallback = callback;
  },
  setEditTherapyEventCallback: function(callback)
  {
    this.editTherapyEventCallback = callback;
  },
  setReissueTherapyEventCallback: function(callback)
  {
    this.reissueTherapyEventCallback = callback;
  },
  setCopyTherapyEventCallback: function(callback)
  {
    this.copyTherapyEventCallback = callback;
  },
  setAbortSuspendedTherapyEventCallback: function(callback)
  {
    this.abortSuspendedTherapyEventCallback = callback;
  }

});
