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
Class.define('app.views.medications.reconciliation.BasketContainer', 'app.views.medications.ordering.BasketContainer', {
  Constructor: function (config)
  {
    this.callSuper(config);
    this.getDisplayProvider().setShowBnf(true);
  },

  /* override, make sure we never remove any read-only elements! */
  _clearBasket: function ()
  {
    var self = this;
    var listData = this.list.getListData().slice();
    var removedData = [];

    this._loopBasketAndMarkForRemoval(listData, [], function (itemsToRemove)
    {
      for (var idx = itemsToRemove.length - 1; idx >= 0; idx--)
      {
        var data = itemsToRemove[idx];
        removedData.push(data);
        self.list.removeRowData(data);
      }
      self.therapiesRemovedEvent(removedData, {clearBasket: listData.length === 0});
    });
  },

  _loopBasketAndMarkForRemoval: function (listData, itemsToRemove, processedCallback)
  {
    var self = this;
    var currentElement = listData.pop();
    if (currentElement)
    {
      if (currentElement instanceof app.views.medications.reconciliation.dto.MedicationOnAdmission
          && currentElement.isReadOnly())
      {
        this._displayConfirmRemovalDialog(currentElement, function (confirmed)
        {
          if (confirmed === true)
          {
            itemsToRemove.push(currentElement);
          }
          self._loopBasketAndMarkForRemoval(listData, itemsToRemove, processedCallback);
        });
      }
      else
      {
        itemsToRemove.push(currentElement);
        self._loopBasketAndMarkForRemoval(listData, itemsToRemove, processedCallback);
      }
    }
    else
    {
      processedCallback(itemsToRemove);
    }
  },

  /* Override, confirm removal for read-only elements! */
  attachElementToolbar: function (elementContainer)
  {
    var self = this;
    var data = elementContainer.getData();

    var toolbar = new app.views.medications.ordering.TherapyContainerBasketToolbar({
      therapyContainer: elementContainer,
      editTherapyEventCallback: function (therapyContainer)
      {
        self._editTherapy(therapyContainer);
      },
      removeFromBasketEventCallback: function (therapyContainer)
      {
        if (data && data instanceof app.views.medications.reconciliation.dto.MedicationOnAdmission && data.isReadOnly())
        {
          self._displayConfirmRemovalDialog(data, function (confirmed)
          {
            if (confirmed === true) self._removeTherapy(therapyContainer);
          });
        }
        else
        {
          self._removeTherapy(therapyContainer);
        }
      }
    });
    elementContainer.setToolbar(toolbar);
  },

  _displayConfirmRemovalDialog: function (data, callback)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var confirmDialog = appFactory.createConfirmSystemDialog(
        view.getDictionary('medication.reconciliation.therapy.already.processed.warning').replace("{0}",
            data.getTherapy().getTherapyDescription()),
        callback
    );
    confirmDialog.setWidth(580);
    confirmDialog.setHeight(162);
    confirmDialog.show();
  },

  getContent: function ()
  {
    var listData = this.list.getListData();
    return listData.slice(0);
  },

  setContent: function (items)
  {
    this._refreshing = true; // ugly way of blocking animations due to a lot of refreshes, causing itemTpl to fire again
    this.list.setListData(items);
    this._refreshing = false;
  },

  /* override, base not calling it's own event */
  removeTherapy: function (therapy)
  {
    var listData = this.list.getListData();
    for (var i = 0; i < listData.length; i++)
    {
      var item = listData[i];
      if (listData[i].getTherapy() === therapy)
      {
        this.list.removeRowData(item);
        this.therapiesRemovedEvent([item], {clearBasket: false });
        break;
      }
    }
  },

  removeByIndex: function (index)
  {
    this.list.removeRowData(index);
  }
});