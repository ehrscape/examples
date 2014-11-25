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

Class.define('app.views.medications.ordering.MedicationsTitleHeader', 'tm.jquery.Container', {
  cls: "title-header",
  padding: '6 6 6 10',

  /** configs */
  disabled: false,
  title: null,
  view: null,
  height: 30,
  actionsMenuFunction: null, // optional
  additionalDataContainer: null, // optional

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    var self = this;
    var appFactory = this.view.getAppFactory();
    this.setLayout(appFactory.createDefaultHFlexboxLayout("start", "center"));
    var cls = this.disabled ? 'PortletHeading2 cancelled' : 'PortletHeading2';
    this.add(new tm.jquery.Container({cls: cls, html: this.title, flex: 1}), {region: 'center'});
    if (this.additionalDataContainer)
    {
      this.add(this.additionalDataContainer)
    }
    if (this.actionsMenuFunction)
    {
      var menuButton = new tm.jquery.Container({
        width: 16, height: 16,
        cls: 'menu-icon'
      });
      menuButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
      {
        var menu = self.actionsMenuFunction();
        if (menu)
        {
          menu.show(elementEvent);
        }
      });
      this.add(menuButton);
    }
  }
});