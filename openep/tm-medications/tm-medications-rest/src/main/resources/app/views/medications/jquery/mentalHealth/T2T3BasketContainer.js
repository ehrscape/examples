/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.mentalHealth.T2T3BasketContainer', 'tm.jquery.Container', {
  cls: "basket-container",

  view: null,
  displayProvider: null,
  editMedicationRouteFunction: null,
  fillMentalHealthDisplayValueFunction: null,

  bnfMaximumContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;
    var view = this.getView();

    this.bnfMaximumContainer = new app.views.medications.mentalHealth.T2T3BnfMaximumContainer({
      view: view
    });

    this.header = new app.views.medications.ordering.MedicationsTitleHeader({
      title: this.getHeaderTitle(),
      view: view,
      actionsMenuFunction: function()
      {
        return self._createHeaderActionsMenu();
      }
    });

    this.list = new tm.jquery.List({
      cls: "basket-container-list",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      autoLoad: false,
      dataSource: [],
      itemTpl: function(index, item)
      {
        return self._buildRow(item);
      },
      selectable: true
    });

    this.add(this.header);
    this.add(this.list);
    this.add(this.bnfMaximumContainer);
  },

  _buildRow: function(item)
  {
    var self = this;
    var therapyContainer = new app.views.medications.common.TherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.getView(),
      data: item,
      displayProvider: this.getDisplayProvider(),
      showIconTooltip: false
    });

    therapyContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_DOUBLE_CLICK, function(component)
    {
      if (this.getToolbar().isEditAllowed())
      {
        self._editMedicationRoute(component);
      }
    });

    therapyContainer.setCls(therapyContainer.getCls() + " animated slideInLeft");
    this._attachElementToolbar(therapyContainer);

    return therapyContainer;
  },

  _createHeaderActionsMenu: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var menu = appFactory.createPopupMenu();
    if (this.getListData().length > 0)
    {
      var menuItemRemove = new tm.jquery.MenuItem({
            text: view.getDictionary('remove.all'),
            cls: "remove-all-menu-item",
            iconCls: 'icon-delete',
            handler: function()
            {
              self._clearBasket();
            }
          }
      );
      menu.addMenuItem(menuItemRemove);

      return menu;
    }
    else
    {
      return null;
    }
  },

  _removeTherapy: function(therapyContainer)
  {
    var elementData = therapyContainer.getData();

    this.list.removeRowData(elementData);

    this.therapiesRemovedEvent([elementData]);
  },

  _clearBasket: function()
  {
    var self = this;
    var listData = this.list.getListData().slice();
    var removedData = listData.slice();

    self.list.reloadList();
    self.therapiesRemovedEvent(removedData);
  },

  _editMedicationRoute: function(therapyContainer)
  {
    var self = this;
    var view = this.getView();
    var elementData = therapyContainer.getData();

    view.showLoaderMask();

    var therapy = elementData.therapy; // MentalHealthTherapyDto.java
    var medicationId = therapy.mentalHealthMedicationDto.id;
    var medicationDataUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MEDICATION_ROUTES;

    var params = {medicationId: medicationId};
    view.loadViewData(medicationDataUrl, params, null, function(routes)
    {
      view.hideLoaderMask();
      self.editMedicationRouteFunction(therapy, routes, function(result)
      {
        if (!tm.jquery.Utils.isEmpty(result) && !tm.jquery.Utils.isEmpty(result.id))
        {
          therapy.mentalHealthMedicationDto.route = result;
        }

        self.fillMentalHealthDisplayValueFunction(therapy, true);
        therapyContainer.refresh();
      });
    });
  },

  _attachElementToolbar: function(elementContainer)
  {
    var self = this;
    var editAllowed = elementContainer.getData().group != app.views.medications.TherapyEnums.mentalHealthGroupEnum.TEMPLATES;

    var toolbar = new app.views.medications.ordering.TherapyContainerBasketToolbar({
      therapyContainer: elementContainer,
      editAllowed: editAllowed,
      editTitle: self.getView().getDictionary("change.route"),
      removeFromBasketEventCallback: function(therapyContainer)
      {
        self._removeTherapy(therapyContainer);
      },
      editTherapyEventCallback: function(therapyContainer)
      {
        self._editMedicationRoute(therapyContainer);
      }
    });
    elementContainer.setToolbar(toolbar);
  },

  getHeaderTitle: function()
  {
    return this.headerTitle;
  },

  /** public methods */
  addTherapy: function(data)
  {
    this.list.addRowData(data, 0);
  },

  getListData: function()
  {
    return this.list.getListData();
  },

  getView: function()
  {
    return this.view;
  },

  getDisplayProvider: function()
  {
    return this.displayProvider;
  },

  getContent: function()
  {
    var listData = this.list.getListData();
    return listData.slice(0);
  }
});
