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
Class.define('app.views.medications.reconciliation.SuspendAdmissionTherapyContainer', 'tm.jquery.Container', {
  statics: {
    asDialog: function(view, resultCallback)
    {
      var content = new app.views.medications.reconciliation.SuspendAdmissionTherapyContainer({
        view: view,
        processResultData: function(resultData){
          if (tm.jquery.Utils.isFunction(resultCallback))
          {
            resultCallback(resultData);
          }
          suspendDialog.hide();
        }
      });

      var suspendDialog = view.getAppFactory().createDefaultDialog(
          view.getDictionary("warning"),
          null,
          content,
          null,
          content.defaultWidth, content.defaultHeight
      );

      return suspendDialog;
    }
  },
  padding: 10,
  cls: 'suspend-admission-dialog',

  /* public members */
  defaultHeight: 155,
  defaultWidth: 460,
  view: null,

  titleIcon: null,
  titleText: null,

  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGUI();
  },

  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var view = this.getView();
    var self = this;

    var titleContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var icon = new tm.jquery.Image({
      style: "background-image: url("
        + view.getAppFactory().createViewModuleImageIconPath("/warningYellow_status_48.png") + ");",
      width: 48,
      height: 48
    });
    view.getLocalLogger().info(view.getAppFactory().createViewModuleImageIconPath("/warningYellow_status_48.png"));

    var description = new tm.jquery.Component({
      cls: "TextDataBold",
      html: view.getDictionary("therapy.needs.reason.for.suspending"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    titleContainer.add(icon);
    titleContainer.add(description);

    var buttonContainer = new tm.jquery.Container({
      cls: "button-container",
      layout: tm.jquery.HFlexboxLayout.create("center", "center", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    var btnSuspendUntilDischarge = new tm.jquery.Button({
      text: view.getDictionary("suspend.until.discharge"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      handler: function()
      {
        btnSuspendUntilDischarge.setEnabled(false);
        btnPrescribeAndSuspend.setEnabled(false);
        var resultData = new app.views.common.AppResultData({success: true, value: {
          prescribe: false
        }});
        self.processResultData(resultData);
      }
    });
    var btnPrescribeAndSuspend = new tm.jquery.Button({
      text: view.getDictionary("prescribe.and.suspend"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      handler: function()
      {
        btnSuspendUntilDischarge.setEnabled(false);
        btnPrescribeAndSuspend.setEnabled(false);
        var resultData = new app.views.common.AppResultData({success: true, value: {
          prescribe: true
        }});
        self.processResultData(resultData);
      }
    });
    buttonContainer.add(btnSuspendUntilDischarge);
    buttonContainer.add(btnPrescribeAndSuspend);

    this.add(titleContainer);
    this.add(buttonContainer);
  },

  getView: function ()
  {
    return this.view;
  },


  getTitleIcon: function ()
  {
    return this.titleIcon;
  },

  getTitleText: function ()
  {
    return this.titleText;
  },

  processResultData: function(resultData)
  {
  }
});