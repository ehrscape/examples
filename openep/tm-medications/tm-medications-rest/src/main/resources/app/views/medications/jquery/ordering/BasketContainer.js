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

Class.define('app.views.medications.ordering.BasketContainer', 'tm.jquery.Container', {
  cls: "basket-container",

  /** configs */
  view: null,
  therapyAddedEvent: null,
  editTherapyFunction: null,
  therapiesRemovedEvent: null,
  saveTemplateFunction: null,
  /** privates */
  displayProvider: null,
  /** privates: components */
  header: null,
  list: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.displayProvider = new app.views.medications.TherapyDisplayProvider({view: config.view});
    var appFactory = this.view.getAppFactory();
    this.setLayout(appFactory.createDefaultVFlexboxLayout("start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.header = new app.views.medications.ordering.MedicationsTitleHeader({
      title: this.view.getDictionary("therapy.list"),
      view: this.view,
      actionsMenuFunction: function()
      {
        return self._createHeaderActionsMenu();
      }
    });
    this.list = new tm.jquery.List({
      flex: 1,
      autoLoad: false,
      dataSource: [],
      itemTpl: function(index, item)
      {
        return self._buildRow(index, item.therapy);
      },
      selectable: false
    });
  },

  _buildGui: function()
  {
    this.add(this.header);
    this.add(this.list);
  },

  _buildRow: function(index, therapy)
  {
    var self = this;
    var container = new tm.jquery.Container({
      padding: '5 5 5 10',
      layout: new tm.jquery.HFlexboxLayout({
        alignment: new tm.jquery.FlexboxLayoutAlignment({
          pack: 'start',
          align: 'start'
        })})
    });
    container.on(tm.jquery.ComponentEvent.EVENT_TYPE_DOUBLE_CLICK, function()
    {
      self._editTherapy(index);
    });

    var iconsContainer = new tm.jquery.Container({
      layout: tm.jquery.new.VFlexboxLayout.create("flex-start", "stretch", 0),
      width: 20
    });
    container.add(iconsContainer);

    if (therapy.completed == false)
    {
      iconsContainer.add(new tm.jquery.Container({
        margin: '2 0 0 0',
        height: 20,
        width: 20,
        cls: 'incomplete-therapy-icon'
      }));
    }
    var therapyHasLinks =
        (!tm.jquery.Utils.isEmpty(therapy.linkToTherapy) || !tm.jquery.Utils.isEmpty(therapy.linkFromTherapy));
    if (therapyHasLinks === true)
    {
      iconsContainer.add(new tm.jquery.Container({
        margin: '2 0 0 2',
        html: 'L',
        cls: 'basket-container-link'
      }));
    }

    var therapyContainer = new tm.jquery.Container({
      flex: 1,
      html: therapy.formattedTherapyDisplay,
      cls: 'TherapyDescription'
    });

    if (therapy.completed == false)
    {
      container.setCls('incomplete-therapy');
    }
    container.add(therapyContainer);

    var actionsContainer = new tm.jquery.Container({
      width: 16, height: 16,
      cls: 'menu-icon'
    });
    container.add(actionsContainer);

    var popupMenu = this._createRowActionsMenu(index);
    actionsContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
      popupMenu.show(elementEvent);
    });
    container.on(tm.jquery.ComponentEvent.EVENT_TYPE_RIGHT_MOUSE_DOWN, function(component, componentEvent, elementEvent)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self.view);
      popupMenu.show(elementEvent);
    });
    popupMenu.on(tm.jquery.ComponentEvent.EVENT_TYPE_SHOW, function()
    {
      container.originalCls = container.getCls();
      container.setCls('selected-therapy');
    });
    popupMenu.on(tm.jquery.ComponentEvent.EVENT_TYPE_HIDE, function()
    {
      container.setCls(container.originalCls);
    });

    return container;
  },

  _createRowActionsMenu: function(index)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var menu = appFactory.createPopupMenu();
    var menuItemEdit = new tm.jquery.MenuItem({
          text: this.view.getDictionary("edit"),
          iconCls: 'icon-edit',
          handler: function()
          {
            self._editTherapy(index);
          }}
    );
    menu.addMenuItem(menuItemEdit);
    var menuItemRemove = new tm.jquery.MenuItem({
          text: this.view.getDictionary("remove"),
          iconCls: 'icon-delete',
          handler: function()
          {
            self._removeRow(index);
          }}
    );
    menu.addMenuItem(menuItemRemove);
    return menu;
  },

  _createHeaderActionsMenu: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var menu = appFactory.createPopupMenu();
    if (this.getTherapies().length > 0)
    {
      var menuItemRemove = new tm.jquery.MenuItem({
            text: this.view.getDictionary('remove.all'),
            iconCls: 'icon-delete',
            handler: function()
            {
              self._clearBasket();
            }}
      );
      menu.addMenuItem(menuItemRemove);

      var menuItemSaveTemplate = new tm.jquery.MenuItem({
            text: this.view.getDictionary('save.template'),
            iconCls: 'icon-save',
            handler: function()
            {
              var therapies = self.getTherapies();
              self.saveTemplateFunction(therapies);
            }}
      );
      menu.addMenuItem(menuItemSaveTemplate);
      return menu;
    }
    else
    {
      return null;
    }
  },

  _removeRow: function(index)
  {
    var therapy = this.list.getListData()[index];
    this.list.removeRowData(this.list.getListData()[index]);
    this.therapiesRemovedEvent([therapy]);
  },

  _clearBasket: function()
  {
    this.therapiesRemovedEvent(this.list.getListData());
    this.list.clearListData();
  },

  _editTherapy: function(index)
  {
    var therapy = this._getTherapyAtIndex(index);
    var medication = this._getMedication(therapy);
    if (tm.jquery.Utils.isEmpty(medication) || tm.jquery.Utils.isEmpty(medication.id))
    {
      var message = this.view.getDictionary('therapy.template.can.not.edit');
      this.view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
    }
    else
    {
      this.editTherapyFunction(therapy);
    }
  },

  _getMedication: function(therapy)
  {
    if (therapy.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX)
    {
      return !tm.jquery.Utils.isEmpty(therapy.ingredientsList[0]) ? therapy.ingredientsList[0].medication : null;
    }
    else
    {
      return therapy.medication;
    }
  },

  _getTherapyAtIndex: function(index)
  {
    var rowData = this.list.getListData()[index];
    return rowData ? rowData.therapy : null;
  },

  /** public methods */
  addTherapy: function(therapy)
  {
    var rowData = {therapy: therapy};
    this.list.addRowData(rowData, 0);
    this.therapyAddedEvent();
  },

  addTherapies: function(therapies)
  {
    for (var i = 0; i < therapies.length; i++)
    {
      var rowData = {therapy: therapies[i]};
      this.list.addRowData(rowData, i);
    }
    this.therapyAddedEvent();
  },

  getTherapies: function()
  {
    var therapies = [];
    var listData = this.list.getListData();
    for (var i = 0; i < listData.length; i++)
    {
      therapies.push(listData[i].therapy);
    }
    return therapies;
  },

  removeTherapy: function(therapy)
  {
    var listData = this.list.getListData();
    for (var i = 0; i < listData.length; i++)
    {
      if (listData[i].therapy == therapy)
      {
        this.list.removeRowData(listData[i]);
      }
    }
  }
});

