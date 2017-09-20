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

Class.define('app.views.medications.proxy.ProxyTherapyView', 'app.views.common.AppView', {
  cls: "v-proxy-therapy-view",
  flex: 1,
  html: '<iframe></iframe>',

  _activeCommandConditionTask: null,
  _activeUpdateData: null,

  _htmlProxy: null,
  
  statics: {
    PROPERTY_KEY_OPEN_EP_URL: "OPEN_EP_URL"
  },
  
  Constructor: function()
  {
    this.callSuper();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      component._loadTherapyView();
    });
  },
  /**
   * @Override
   */
  onViewInit: function()
  {
    this.callSuper();

    if (this.isDevelopmentMode())
    {
      this.sendCommand("update", {
        data: {
          patientId: 399195450,
          casTicket: this.getCasTicket()
        }
      });
    }
  },
  /**
   * @Override
   * @param command
   */
  onViewCommand: function(command)
  {
    tm.jquery.ComponentUtils.hideAllDropDownMenus();
    tm.jquery.ComponentUtils.hideAllTooltips();
    tm.jquery.ComponentUtils.hideAllDialogs();

    var self = this;
    var appFactory = this.getAppFactory();
    if (command.hasOwnProperty('update'))
    {
      this._activeUpdateData = command.update.data;
    }
    if(tm.jquery.Utils.isEmpty(this._activeCommandConditionTask) == false)
    {
      this._activeCommandConditionTask.abort();
    }
    this._activeCommandConditionTask = appFactory.createConditionTask(
        function()
        {
          self._onViewCommandImpl(command);
        },
        function()
        {
          return self.isRendered() && self._htmlProxy != null && self._htmlProxy.isInitialized();
        },
        100, 300
    );
  },
  _onViewCommandImpl: function(command)
  {
    var htmlProxy = this._htmlProxy;
    if (command.hasOwnProperty('update'))
    {
      htmlProxy.sendCommand(command);
    }
    else if (command.hasOwnProperty('refresh'))
    {
      htmlProxy.sendCommand(command);
    }
    else if (command.hasOwnProperty("clear"))
    {
      if (tm.jquery.Utils.isEmpty(this._activeUpdateData) == false)
      {
        htmlProxy.sendCommand(command);
      }
    }
  },
  _loadTherapyView: function()
  {
    var ProxyTherapyView = app.views.medications.proxy.ProxyTherapyView;
    var openEpUrl = this.getProperty(ProxyTherapyView.PROPERTY_KEY_OPEN_EP_URL);

    var viewConfig = {
      language: "en", theme: "fresh", debug: false,
      header: {
        url: openEpUrl, context: "rest", controller: "medications"
      },
      view: "therapyView"
    };

    var proxyFrameElement = this._getProxyFrameElement();
    proxyFrameElement.onload = this._createHtmlProxyFrameLoadHandler();
    proxyFrameElement.src =
        openEpUrl + "/rest/htmldocument/externalview" +
        "?config=" + encodeURIComponent(JSON.stringify(viewConfig));
  },
  _createHtmlProxyFrameLoadHandler: function()
  {
    var self = this;
    return function()
    {
      var proxyFrameElement = self._getProxyFrameElement();
      var htmlProxyFrameElementContentWindow = proxyFrameElement.contentWindow || proxyFrameElement.contentDocument;
      var proxyUrl = proxyFrameElement.src;

      // html proxy instance //
      var htmlProxy = tm.proxy.HtmlProxyManager.createMasterProxy(
          "_html_proxy_" + self.getId(),
          proxyUrl,
          htmlProxyFrameElementContentWindow,
          {security: null, context: null, data: null, autoHeight: false}
      );
      // html proxy action listener //
      htmlProxy.addActionListener(function(action)
      {
        //console.log("action", action);
      });
      // register white-list //
      htmlProxy.addWhiteListAddress(self.getProperty(app.views.medications.proxy.ProxyTherapyView.PROPERTY_KEY_OPEN_EP_URL));

      self._htmlProxy = htmlProxy;
    }
  },
  _getProxyFrameElement: function()
  {
    return $(this.getDom()).find("iframe")[0];
  }
});