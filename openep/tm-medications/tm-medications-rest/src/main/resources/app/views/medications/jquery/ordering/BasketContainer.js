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
  headerTitle: null,
  therapyAddedEvent: null,
  editTherapyFunction: null,
  therapiesRemovedEvent: null,
  saveTemplateFunction: null,
  /** privates */
  displayProvider: null,
  existsUniversalTherapy: false,
  /** privates: components */
  header: null,
  list: null,

  _refreshing: false,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.displayProvider = this.getConfigValue("displayProvider", new app.views.medications.TherapyDisplayProvider({
      view: this.view,
      showBnf: true
    }));

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function ()
  {
    var self = this;
    this.header = new app.views.medications.ordering.MedicationsTitleHeader({
      title: this.getHeaderTitle(),
      view: this.view,
      actionsMenuFunction: function ()
      {
        return self._createHeaderActionsMenu();
      }
    });
    this.list = new tm.jquery.List({
      cls: "basket-container-list",
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      autoLoad: false,
      dataSource: [],
      itemTpl: function (index, item)
      {
        return self._buildRow(item);
      },
      selectable: true
    });
  },

  _buildGui: function ()
  {
    this.add(this.header);
    this.add(this.list);
  },


  _buildRow: function (item)
  {
    var self = this;

    var therapyContainer = new app.views.medications.common.TherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.getView(),
      data: item,
      displayProvider: this.getDisplayProvider(),
      showIconTooltip: false
    });
    therapyContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_DOUBLE_CLICK, function (component)
    {
      self._editTherapy(component);
    });

    if (this._refreshing == false) therapyContainer.setCls(therapyContainer.getCls() + " animated slideInLeft");
    if (item.therapy.completed == false) therapyContainer.setCls(therapyContainer.getCls() + " incomplete-therapy");

    this.attachElementToolbar(therapyContainer);

    if (this._refreshing == false)
    therapyContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component){
      // don't trigger apply
      component.cls = component.getCls().replace("animated", "");
    });
    return therapyContainer;
  },

  _createHeaderActionsMenu: function ()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var menu = appFactory.createPopupMenu();
    if (this.getTherapies().length > 0)
    {
      var menuItemRemove = new tm.jquery.MenuItem({
            text: this.view.getDictionary('remove.all'),
            cls: "remove-all-menu-item",
            iconCls: 'icon-delete',
            handler: function ()
            {
              self._clearBasket();
            }
          }
      );
      menu.addMenuItem(menuItemRemove);
      if (!this.existsUniversalTherapy)
      {
        var menuItemSaveTemplate = new tm.jquery.MenuItem({
              text: this.view.getDictionary('save.template'),
              cls: "save-template-menu-item",
              iconCls: 'icon-save',
              handler: function()
              {
                var therapies = self.getTherapies();
                self.saveTemplateFunction(therapies);
              }
            }
        );
        menu.addMenuItem(menuItemSaveTemplate);
      }
      return menu;
    }
    else
    {
      return null;
    }
  },

  _removeTherapy: function (therapyContainer)
  {
    var elementData = therapyContainer.getData();

    var therapyCanBeRemoved = this._canTherapyBeRemovedDueToLinks(elementData.therapy);
    if (therapyCanBeRemoved)
    {
      this.list.removeRowData(elementData);

      if (elementData.therapy.linkName)
      {
        this._removeTherapyLink(elementData.therapy);
        this.list.rebuild();
      }

      this.therapiesRemovedEvent([elementData]);
    }
    var listData = this.list.getListData();
    this.existsUniversalTherapy = this._existsTherapyWithUniversalMedication(listData);
  },

  _canTherapyBeRemovedDueToLinks: function (therapy)
  {
    if (therapy.linkName)
    {
      var nextLinkName = tm.views.medications.MedicationUtils.getNextLinkName(therapy.linkName);
      var listData = this.list.getListData();
      for (var j = 0; j < listData.length; j++)
      {
        if (listData[j].therapy.linkName == nextLinkName)
        {
          var message = this.view.getDictionary('therapy.can.not.remove.if.linked');
          this.view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
          return false;
        }
      }
    }
    return true;
  },

  _clearBasket: function ()
  {
    var data = this.list.getListData().slice();
    this.list.clearListData();
    this.therapiesRemovedEvent(data, {clearBasket: true});
    this.existsUniversalTherapy = false;
  },

  _editTherapy: function (therapyContainer)
  {
    var elementData = therapyContainer.getData();
    var medication = this._getMedication(elementData.therapy);
    if (tm.jquery.Utils.isEmpty(medication))
    {
      var message = this.view.getDictionary('therapy.template.can.not.edit');
      this.view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
    }
    else
    {
      this.editTherapyFunction(therapyContainer);
    }
  },

  _getMedication: function (therapy)
  {
    if (therapy.isOrderTypeComplex())
    {
      return !tm.jquery.Utils.isEmpty(therapy.ingredientsList[0]) ? therapy.ingredientsList[0].medication : null;
    }
    else
    {
      return therapy.medication;
    }
  },

  _removeTherapyLink: function (therapy)
  {
    var linkName = therapy.linkName;
    var listData = this.list.getListData();

    //remove first if second
    var isSecondLinkedTherapy = linkName.length == 2 && linkName.charAt(1) == '2';
    if (isSecondLinkedTherapy) //clear linkName on first therapy
    {
      var previousLinkName = linkName.charAt(0) + '1';
      for (var j = 0; j < listData.length; j++)
      {
        if (listData[j].therapy.linkName == previousLinkName)
        {
          listData[j].therapy.linkName = null;
          break;
        }
      }
    }

    therapy.linkName = null;
  },

  _existsTherapyWithUniversalMedication: function(listData)
  {
    for (var i = 0; i < listData.length; i++)
    {
      if (listData[i].therapy.hasUniversalIngredient())
      {
        return true;
      }
    }
    return false;
  },

  /** public methods */

  /* for override, attach event handlers to therapyContainer's toolbars from outside */
  attachElementToolbar: function (elementContainer)
  {
    var self = this;
    var toolbar = new app.views.medications.ordering.TherapyContainerBasketToolbar({
      therapyContainer: elementContainer,
      editTherapyEventCallback: function (therapyContainer)
      {
        self._editTherapy(therapyContainer);
      },
      removeFromBasketEventCallback: function (therapyContainer)
      {
        self._removeTherapy(therapyContainer);
      }
    });
    elementContainer.setToolbar(toolbar);
  },

  addTherapy: function (data, options)
  {
    if (!tm.jquery.Utils.isEmpty(data) && data.hasOwnProperty("therapy"))
    {
      this.list.addRowData(data, 0);
      var listData = this.list.getListData();
      this.existsUniversalTherapy = this._existsTherapyWithUniversalMedication(listData);
      this.therapyAddedEvent(options);
    }
    else
    {
      this.getView().getLocalLogger().warn("An element with an invalid structure was added to the BasketContainer. Call ignored.");
    }
  },

  getTherapies: function ()
  {
    var therapies = [];
    var listData = this.list.getListData();
    for (var i = 0; i < listData.length; i++)
    {
      therapies.push(listData[i].therapy);
    }
    return therapies;
  },

  getBasketItems: function()
  {
    return this.list.getListData();
  },

  getHeaderTitle: function ()
  {
    return this.headerTitle;
  },

  getView: function ()
  {
    return this.view;
  },

  getDisplayProvider: function ()
  {
    return this.displayProvider;
  },

  removeTherapy: function (therapy)
  {
    var listData = this.list.getListData();
    for (var i = 0; i < listData.length; i++)
    {
      if (listData[i].therapy == therapy)
      {
        this.list.removeRowData(listData[i]);
        break;
      }
    }
  },

  refreshWithExistingData: function ()
  {
    this._refreshing = true; // ugly way of blocking animations due to a lot of refreshes, causing itemTpl to fire again
    this.list.rebuild();
    this._refreshing = false;
  }
});

