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
Class.define('app.views.medications.reconciliation.TherapyContainerPanelToolbar', 'app.views.medications.common.TemplateTherapyContainerToolbar', {
  cancelEventCallback: null,
  suspendEventCallback: null,

  withSuspend: true,
  withCancel: true,

  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /* override */
  _buildGUI: function ()
  {
    var self = this;
    var view = this.getView();
    if (this.withCancel === true)
    {
      var cancelButtonIcon = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-delete group-element-cancel-therapy-menu-item",
        width: 32,
        height: 32,
        tooltip: tm.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("cancel"))
      });
      this.add(cancelButtonIcon);

      cancelButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component)
      {
        if (!tm.jquery.Utils.isEmpty(self.cancelEventCallback) && component.isEnabled())
        {
          self.cancelEventCallback(self.getTherapyContainer());
        }
      });
    }
    if (this.withSuspend === true)
    {
      var suspendButtonIcon = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-suspend group-element-cancel-therapy-menu-item",
        width: 32,
        height: 32,
        tooltip: tm.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("suspend"))
      });

      this.add(suspendButtonIcon);
      suspendButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component)
      {
        if (!tm.jquery.Utils.isEmpty(self.suspendEventCallback) && component.isEnabled())
        {
          self.suspendEventCallback(self.getTherapyContainer());
        }
      });
    }

    this.callSuper();
  },

  /* Override to disable the more actions menu. */
  _addActionsMenu: function ()
  {
    // do nothing, override so there's no popup menu!
  },

  setCancelEventCallback: function (callback)
  {
    this.cancelEventCallback = callback;
  },
  setSuspendEventCallback: function (callback)
  {
    this.suspendEventCallback = callback;
  }
});
