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
Class.define('tm.views.medications.pharmacists.ReviewContainerHeader', 'tm.jquery.Container', {
  cls: 'header-container',

  reviewContainer: null,
  editMode: null,

  editReviewEventCallback: null,
  deleteReviewEventCallback: null,
  confirmReviewEventCallback: null,
  cancelEditEventCallback: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this.editMode = this.getConfigValue('editMode', false);
    this.showReminders = this.getConfigValue('showReminders', true);
    this._buildGui(false);
    this._setKeyboardShortcuts();

  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    var self = this;
    var view = this.getReviewContainer().getView();
    var appFactory = view.getAppFactory();
    var data = this.getReviewContainer().getReviewData();
    var statusCls = data.isDraft() ? 'status-icon inprogress' : 'status-icon done';
    var confirmCallback = this.confirmReviewEventCallback;

    var layerContainerOptions = {
      background: {
        cls: 'document-icon'
      },
      layers: []
    };
    layerContainerOptions.layers.push({
      hpos: 'right', vpos: 'bottom', cls: statusCls,
      title: data.isDraft() ? view.getDictionary('Status.IN_PROGRESS') : view.getDictionary('Status.DONE')
    });

    var statusIcon = new tm.jquery.Image({
      html: appFactory.createLayersContainerHtml(layerContainerOptions),
      width: 48,
      height: 48
    });

    var titleContainer = new tm.jquery.Container({
      cls: 'title-container',
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0),
      html: '<div class="PortletHeading1">' + view.getDictionary('pharmacists.review') + '</div>' +
          '<div class="TextData">' + view.getDisplayableValue(new Date(data.getCreateTimestamp()), 'short.date.time')
          + (tm.jquery.Utils.isEmpty(data.getComposer()) ? '' : ', '.concat(data.getComposer().name)) + '</div>',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      alignSelf: 'stretch'
    });

    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));
    this.add(statusIcon);
    this.add(titleContainer);

    if (!this.isEditMode() && this.getShowReminders() && !tm.jquery.Utils.isEmpty(data.getReminderDate()))
    {
      var now = CurrentTime.get();
      now.setHours(0, 0, 0, 0);
      var reminderDate = new Date(data.getReminderDate().getTime());
      reminderDate.setHours(0, 0, 0, 0);

      if (reminderDate > now)
      {
        var oneDay = 24 * 60 * 60 * 1000; // hours*minutes*seconds*milliseconds
        var diffDays = Math.abs((now.getTime() - reminderDate.getTime()) / (oneDay));

        var reminderText = new tm.jquery.Label({
          cls: 'TextData reminder-container',
          alignSelf: 'flex-start',
          html: view.getDictionary("reminder") + ": " + (diffDays <= 1 ?
              view.getDictionary("check.in.1.day") : view.getDictionary("check.in.x.days").replace("{0}", diffDays))
        });
        this.add(reminderText);
      }
    }

    if (view.getTherapyAuthority().isManagePatientPharmacistReviewAllowed() && data.isDraft())
    {
      if (this.isEditMode())
      {
        var confirmButton = new tm.jquery.Button({
          text: view.getDictionary('confirm.report'),
          handler: function (component)
          {
            if (!tm.jquery.Utils.isEmpty(confirmCallback))
            {
              confirmCallback();
            }
          },
          alignSelf: 'center',
          enabled: !tm.jquery.Utils.isEmpty(view.getCurrentUserAsCareProfessional())
        });
        this.add(confirmButton);
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
        var popupMenu = self._createHeaderPopupMenu();
        popupMenu.show(elementEvent);
      });
      this.add(popupMenuHotSpot);
    }
  },

  _setKeyboardShortcuts: function ()
  {
    var self = this;
    var view = this.getReviewContainer().getView();
    var confirmCallback = this.confirmReviewEventCallback;
    var cancelEditCallback = this.cancelEditEventCallback;

    this.onKey(new tm.jquery.event.KeyStroke({key: "esc", altKey: false, ctrlKey: false, shiftKey: false}),
        function ()
        {
          if (self.isEditMode() && !tm.jquery.Utils.isEmpty(cancelEditCallback))
          {
            cancelEditCallback();
          }
        });

    if (!tm.jquery.Utils.isEmpty(view.getCurrentUserAsCareProfessional()))
    {
      this.onKey(new tm.jquery.event.KeyStroke({key: "return", altKey: false, ctrlKey: true, shiftKey: false}),
          function (component, componentEvent, elementEvent)
          {
            if (self.isEditMode() && !tm.jquery.Utils.isEmpty(confirmCallback))
            {
              confirmCallback();
            }
          });
    }
  },

  _createHeaderPopupMenu: function ()
  {
    var view = this.getReviewContainer().getView();
    var appFactory = view.getAppFactory();
    var editCallback = this.editReviewEventCallback;
    var confirmCallback = this.confirmReviewEventCallback;
    var deleteCallback = this.deleteReviewEventCallback;
    var cancelEditCallback = this.cancelEditEventCallback;

    var popupMenu = appFactory.createPopupMenu();

    if (this.editMode === true)
    {
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        text: view.getDictionary('confirm.report'),
        handler: function ()
        {
          if (!tm.jquery.Utils.isEmpty(confirmCallback))
          {
            confirmCallback();
          }
        },
        iconCls: 'icon-finish-24'
      }));
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        text: view.getDictionary('cancel'),
        handler: function ()
        {
          if (!tm.jquery.Utils.isEmpty(cancelEditCallback))
          {
            cancelEditCallback();
          }
        },
        iconCls: 'icon-delete'
      }));
    }
    else
    {
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        text: view.getDictionary('edit.report'),
        handler: function ()
        {
          if (!tm.jquery.Utils.isEmpty(editCallback))
          {
            editCallback();
          }
        },
        iconCls: 'icon-edit'
      }));
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        text: view.getDictionary('delete.report'),
        handler: function ()
        {
          if (!tm.jquery.Utils.isEmpty(deleteCallback))
          {
            deleteCallback();
          }
        },
        iconCls: 'icon-delete'
      }));
    }

    return popupMenu;
  },

  setEditMode: function (value)
  {
    this.editMode = value;
  },

  isEditMode: function ()
  {
    return this.editMode == true;
  },

  getShowReminders: function ()
  {
    return this.showReminders;
  },

  refresh: function ()
  {
    if (this.isRendered())
    {
      this.removeAll();
      this._buildGui();
      this.repaint();
    }
  },

  /* public methods*/
  getReviewContainer: function ()
  {
    return this.reviewContainer;
  },

  setConfirmReviewEventCallback: function (callback)
  {
    this.confirmReviewEventCallback = callback;
  },

  setDeleteReviewEventCallback: function (callback)
  {
    this.deleteReviewEventCallback = callback;
  },

  setEditReviewEventCallback: function (callback)
  {
    this.editReviewEventCallback = callback;
  },

  setCancelEditEventCallback: function (callback)
  {
    this.cancelEditEventCallback = callback;
  }
});