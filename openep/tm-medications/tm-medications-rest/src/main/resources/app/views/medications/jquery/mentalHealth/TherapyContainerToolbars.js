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
Class.define('app.views.medications.mentalHealth.TherapyContainerToolbars', 'app.views.medications.common.TemplateTherapyContainerToolbar', {
  addToBasketEventCallback: null,

  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /* override */
  _buildGUI: function ()
  {
    var self = this;
    var view = this.getView();
    var addButtonIcon = new tm.jquery.Image({
      cursor: "pointer",
      cls: "icon-extend-24 template-element-add-basket-menu-item",
      width: 32,
      height: 32,
      tooltip: tm.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("do.order"))
    });
    this.add(addButtonIcon);

    addButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
    {
      self.addToBasketEventCallback(self.getTherapyContainer());
    });
  },

  /* Override to disable the more actions menu. */
  _addActionsMenu: function ()
  {
  },

  setAddToBasketEventCallback: function (callback)
  {
    this.addToBasketEventCallback = callback;
  }
});

