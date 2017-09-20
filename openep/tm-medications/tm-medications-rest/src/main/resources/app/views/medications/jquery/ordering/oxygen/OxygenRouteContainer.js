/*
 * Copyright (c) 2010-2017 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.ordering.oxygen.OxygenRouteContainer', 'tm.jquery.Container', {
  cls: 'oxygen-route-container',
  scrollable: 'visible',
  view: null,

  startingDevice: null, /* preselection */
  allowDeviceDeselect: true,

  _deviceSelectBox: null,
  _venturiMaskButtonGroup: null,
  _venturiMaskUnitComponet: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
    if (this._getPresetStartingDevice())
    {
      this.setStartingDevice(this._getPresetStartingDevice());
    }
  },

  _buildGui: function()
  {
    var self = this;
    var routeEnums = app.views.medications.TherapyEnums.oxygenDeliveryClusterRoute;

    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));

    var deviceSelectBox = new tm.jquery.SelectBox({
      cls: 'device-selectbox',
      options: this._getDeviceSelectBoxOptions(),
      liveSearch: false,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      dropdownWidth: 'stretch',
      allowSingleDeselect: this.isAllowDeviceDeselect(),
      appendTo: function()
      {
        return self.getView().getAppFactory().getDefaultRenderToElement();
      }
    });
    deviceSelectBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function eventHandler(component, componentEvent)
    {
       self._handleRouteChange(componentEvent.eventData.selections);
    });

    var venturiMaskButtonGroup = new tm.jquery.ButtonGroup({
      orientation: 'horizontal',
      type: 'radio',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      buttons: this._getVenturiMaskButtons(),
      hidden: !(this._getPresetStartingDevice() && this._getPresetStartingDevice().isVenturiMask())
    });

    var venturiMaskUnitComponent = new tm.jquery.Component({
      cls: 'venturi-mask-unit',
      html: '%',
      hidden: !(this._getPresetStartingDevice() && this._getPresetStartingDevice().isVenturiMask())
    });

    this.add(deviceSelectBox);
    this.add(venturiMaskButtonGroup);
    this.add(venturiMaskUnitComponent);

    this._deviceSelectBox = deviceSelectBox;
    this._venturiMaskButtonGroup = venturiMaskButtonGroup;
    this._venturiMaskUnitComponet = venturiMaskUnitComponent;
  },

  /**
   * @returns {Array<tm.jquery.selectbox.Option>}
   * @private
   */
  _getDeviceSelectBoxOptions: function()
  {
    var preselection = this._getPresetStartingDevice() ? this._getPresetStartingDevice().getRoute() : null;
    var routeEnums = app.views.medications.TherapyEnums.oxygenDeliveryClusterRoute;
    var options = [];
    for (var route in routeEnums)
    {
      options.push(tm.jquery.SelectBox.createOption(
          route, 
          this.getView().getDictionary('OxygenDeliveryCluster.Route.' + route),
          null,
          null,
          preselection ? preselection === route : false
      ));
    }
    return options;
  },

  /**
   * @returns {Array<tm.jquery.Button>}
   * @private
   */
  _getVenturiMaskButtons: function()
  {
    var button24 = new tm.jquery.Button({
      text: '24',
      data: "24%"
    });

    var button28 = new tm.jquery.Button({
      text: '28',
      data: "28%"
    });

    var button35 = new tm.jquery.Button({
      text: '35',
      data: "35%"
    });

    var button40 = new tm.jquery.Button({
      text: '40',
      data: "40%"
    });

    var button60 = new tm.jquery.Button({
      text: '60',
      data: "60%"
    });

    return [ button24, button28, button35, button40, button60 ];
  },

  /**
   * @param {Number} value
   * @returns {tm.jquery.Button|null}
   * @private
   */
  _findVenturiMaskButtonByValue: function(value)
  {
    var buttons = this._venturiMaskButtonGroup.getButtons();
    for (var idx = 0; idx < buttons.length; idx++)
    {
      if (buttons[idx].data && buttons[idx].data === value)
      {
        return buttons[idx];
      }
    }
    return null;
  },

  /**
   * @param {Array<string>} selections
   * @private
   */
  _handleRouteChange: function(selections)
  {
    var routeEnums = app.views.medications.TherapyEnums.oxygenDeliveryClusterRoute;

    if (tm.jquery.Utils.isArray(selections) && selections.length > 0 && selections[0] === routeEnums.VENTURI_MASK)
    {
      this.isRendered() ? this._venturiMaskButtonGroup.show() : this._venturiMaskButtonGroup.setHidden(false);
      this.isRendered() ? this._venturiMaskUnitComponet.show() : this._venturiMaskUnitComponet.setHidden(false);
    }
    else
    {
      this.isRendered() ? this._venturiMaskButtonGroup.hide() : this._venturiMaskButtonGroup.setHidden(true);
      this.isRendered() ? this._venturiMaskUnitComponet.hide() : this._venturiMaskUnitComponet.setHidden(true);
      this._venturiMaskButtonGroup.setSelection([], true);
    }
  },

  /**
   * @returns {app.views.medications.common.dto.OxygenStartingDevice|null}
   * @private
   */
  _getPresetStartingDevice: function()
  {
    return this.startingDevice;
  },
  
  clear: function()
  {
    this._deviceSelectBox.setSelections([], true);
  },

  /**
   * @returns {app.views.medications.common.dto.OxygenStartingDevice|null}
   */
  getStartingDevice: function()
  {
    if (this._deviceSelectBox.getSelections().length > 0)
    {
      var venturiMaskSelection = this._venturiMaskButtonGroup.getSelection();
      return new app.views.medications.common.dto.OxygenStartingDevice({
        route: this._deviceSelectBox.getSelections()[0],
        routeType: venturiMaskSelection.length > 0 ? venturiMaskSelection[0].data : null
      });
    }
    return null;
  },
  
  getInputElement: function()
  {
    return this._deviceSelectBox.getDom();
  },

  /**
   * @param {app.views.medications.common.dto.OxygenStartingDevice|null} device
   */
  setStartingDevice: function(device)
  {
    if (device)
    {
      this._deviceSelectBox.setSelections([device.getRoute()], true);
      this._handleRouteChange([device.getRoute()]);

      if (device.getRouteType())
      {
        var maskButton = this._findVenturiMaskButtonByValue(device.getRouteType());
        this._venturiMaskButtonGroup.setSelection([maskButton], true);
      }
    }
    else
    {
      this._deviceSelectBox.setSelections([], true);
      this._handleRouteChange([]);
    }
  },

  /**
   * @returns {boolean}
   */
  isAllowDeviceDeselect: function()
  {
    return this.allowDeviceDeselect === true;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});