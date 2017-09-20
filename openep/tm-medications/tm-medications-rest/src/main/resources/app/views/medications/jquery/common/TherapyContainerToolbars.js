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
Class.define('app.views.medications.common.TherapyContainerToolbar', 'tm.jquery.Container', {
  cls: "toolbar-container",
  therapyContainer: null,

  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGUI();
  },

  /* for override */
  _buildGUI: function()
  {

  },

  getTherapyContainer: function()
  {
    return this.therapyContainer;
  },

  getTherapyData: function()
  {
    return this.getTherapyContainer().getData();
  },

  getView: function()
  {
    return this.getTherapyContainer().getView();
  },

  refresh: function ()
  {
    this.removeAll();
    this._buildGUI();

    if (this.isRendered()) this.repaint();

  }
});
Class.define('app.views.medications.common.TemplateTherapyContainerToolbar', 'app.views.medications.common.TherapyContainerToolbar', {

  addToBasketEventCallback: null,
  addToBasketWithEditEventCallback: null,
  removeFromTemplateEventCallback: null,

  addButtonIcon: null,
  addWithEditButtonIcon: null,
  changeActionAllowed: true,


  /* override */
  _buildGUI: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-end", "center", 0));

    var addButtonIcon = new tm.jquery.Image({
      cursor: "pointer",
      cls: "icon-extend-24 template-element-add-basket-menu-item",
      width: 32,
      height: 32,
      tooltip: tm.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("do.order"))
    });

    var addToBasketTask = appFactory.createDebouncedTask(
        "app.views.medications.reconciliation.TherapyGroupPanel.addToBasketEventCallback", function ()
    {
      self.addToBasketEventCallback(self.getTherapyContainer());
    }, 0, 1000);

    addButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component){
      if (!tm.jquery.Utils.isEmpty(self.addToBasketEventCallback) && component.isEnabled())
      {
        addToBasketTask.run();
      }
    });

    var addWithEditButtonIcon = new tm.jquery.Image({
      cursor: "pointer",
      cls: "icon-extend-edit-24 edit-template-element-add-to-basket-menu-item",
      width: 32,
      height: 32,
      tooltip: tm.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary('edit.and.add'))
    });

    addWithEditButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component){
      if (!tm.jquery.Utils.isEmpty(self.addToBasketWithEditEventCallback) && component.isEnabled())
      {
        self.addToBasketWithEditEventCallback(self.getTherapyContainer());
      }
    });

    this.add(addWithEditButtonIcon);
    this.add(addButtonIcon);

    this._addActionsMenu();

    this.addButtonIcon = addButtonIcon;
    this.addWithEditButtonIcon = addWithEditButtonIcon;
  },

  _addActionsMenu: function()
  {
    var self = this;
    var popupMenuHotSpot = new tm.jquery.Image({
      cls: 'menu-icon',
      width: 32,
      height: 24,
      cursor: 'pointer',
      alignSelf: 'flex-start'
    });
    popupMenuHotSpot.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component, componentEvent, elementEvent)
    {
      if (component.isEnabled())
      {
        var popupMenu = self._createMoreActionsPopupMenu();
        popupMenu.show(elementEvent);
      }
    });
    this.add(popupMenuHotSpot);
  },

  _createMoreActionsPopupMenu: function ()
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var therapyContainer = this.getTherapyContainer();

    var addToBasketEventCallback = this.addToBasketEventCallback;
    var addToBasketWithEditEventCallback = this.addToBasketWithEditEventCallback;
    var removeFromTemplateEventCallback = this.removeFromTemplateEventCallback;

    var popupMenu = appFactory.createPopupMenu();

    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      cls: "template-element-add-basket-menu-item",
      text: view.getDictionary("do.order"),
      handler: function ()
      {
        if (!tm.jquery.Utils.isEmpty(addToBasketEventCallback))
        {
          addToBasketEventCallback(therapyContainer);
        }
      },
      iconCls: 'icon-extend-24'
    }));
    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      cls: "edit-template-element-add-to-basket-menu-item",
      text: view.getDictionary('edit.and.add'),
      handler: function ()
      {
        if (!tm.jquery.Utils.isEmpty(addToBasketWithEditEventCallback))
        {
          addToBasketWithEditEventCallback(therapyContainer);
        }
      },
      iconCls: 'icon-extend-edit-24'
    }));
    if (this.isChangeActionAllowed())
    {
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: "remove-from-template-menu-item",
        text: view.getDictionary('remove.from.template'),
        handler: function()
        {
          if (!tm.jquery.Utils.isEmpty(removeFromTemplateEventCallback))
          {
            removeFromTemplateEventCallback(therapyContainer);
          }
        },
        iconCls: 'icon-delete'
      }));
    }
    return popupMenu;
  },

  setAddToBasketEventCallback: function(callback)
  {
    this.addToBasketEventCallback = callback;
  },
  setAddToBasketWithEditEventCallback: function(callback)
  {
    this.addToBasketWithEditEventCallback = callback;
  },

  /**
   *
   * @returns {boolean}
   */
  isChangeActionAllowed: function()
  {
    return this.changeActionAllowed;
  }
});
