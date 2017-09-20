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
Class.define('app.views.medications.common.TemplateTherapyGroupPanel', 'app.views.medications.common.TherapyGroupPanel', {
  cls: "therapy-group-panel template-therapy-group-panel",
  deleteTemplateEventCallback: null,
  addToBasketEventCallback: null,
  dynamicContent: true,
  changeActionAllowed: true,

  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /* extending due to extra padding of the arrow */
  _addEvents: function()
  {
    this.callSuper();
    var self = this;
    this._header.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      if (self._isToggleIconEventHandlerRegistered == false)
      {
        var headerComponentDom = component.getDom();

        var originalEventSrcElement = elementEvent.originalEvent.srcElement ?
            elementEvent.originalEvent.srcElement : elementEvent.originalEvent.target;

        var isToggleIconAreaEvent = elementEvent.originalEvent.pageX - $(originalEventSrcElement).offset().left <= 35;
        if (originalEventSrcElement == headerComponentDom && isToggleIconAreaEvent)
        {
          elementEvent.stopPropagation();
          // only collapse/expand icon click event //
          self.toggle(null);
        }
      }
    });

  },

  _initHeaderContainer: function ()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var header = this.getHeader();
    header.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));
    /* overriding the only way possible due to tm.jquery.Panel impl. */

    var headerValueContainer = new tm.jquery.Container({
      cursor: "pointer",
      cls: "TextDataBold text-unselectable ellipsis",
      html: this.getGroupTitle(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var addTemplateToBasket = new tm.jquery.Image({
      cursor: "pointer",
      cls: "icon-extend-24 add-template-to-basket-menu-item",
      style: "background-size: 16px 16px;", // missing 16px icon, workaround for now
      width: 32,
      height: 24,
      tooltip: tm.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("do.order")),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var addToBasketTask = appFactory.createDebouncedTask(
        "app.views.medications.common.TherapyGroupPanel.addToBasketEventCallback", function ()
        {
          self.addToBasketEventCallback(self.getContentData());
        }, 0, 1000);

    addTemplateToBasket.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component){
      if (!tm.jquery.Utils.isEmpty(self.addToBasketEventCallback) && component.isEnabled())
      {
        addToBasketTask.run();
      }
    });
    header.add(headerValueContainer);
    header.add(addTemplateToBasket);

    if (this.isChangeActionAllowed())
    {
      var deleteTemplateIcon = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-remove-small delete-template-menu-item",
        width: 32,
        height: 24,
        tooltip: tm.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary('delete.template')),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
      });
      deleteTemplateIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        if (!tm.jquery.Utils.isEmpty(self.deleteTemplateEventCallback))
        {
          self.deleteTemplateEventCallback();
        }
      });
      header.add(deleteTemplateIcon);
    }

    this.bindToggleEvent([headerValueContainer]);
  },

  setDeleteTemplateEventCallback: function(callback)
  {
    this.deleteTemplateEventCallback = callback;
  },

  setAddToBasketEventCallback: function(callback)
  {
    this.addToBasketEventCallback = callback;
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