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
Class.define('app.views.medications.mentalHealth.TherapyGroupPanel', 'app.views.medications.common.TherapyGroupPanel', {

  addAllMedicationsToBasketFunction: null, // callback function for add all elements to basket
  addMedicationToBasketFunction: null,
  getBasketTherapiesFunction: null,

  addAllEnabled: null, // enabled adding all elements to basket
  selectedCls: null, // element container css when added to basket
  addAllImage: null, // add all elements to basket image

  /* Override */
  _initHeaderContainer: function()
  {
    var self = this;
    var header = this.getHeader();

    header.setCls("TextDataBold");
    header.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));

    var headerValueContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      cursor: "pointer",
      cls: "TextDataBold",
      html: this.getGroupTitle()
    });

    if (this.addAllEnabled === true)
    {
      this.addAllImage = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-extend-24",
        margin: "0px 5px 0px 0px",
        handler: function()
        {
          self._onAddAllMedicationsToBasket();
        }
      });
    }

    header.add(headerValueContainer);
    if (this.addAllEnabled === true)
    {
      header.add(this.addAllImage);
    }
    this.bindToggleEvent([headerValueContainer]);
  },

  _onAddAllMedicationsToBasket: function()
  {
    this.addAllMedicationsToBasketFunction(this);
    this.addAllImage.hide();
  },

  /* Override */
  buildElementContainer: function(elementData)
  {
    var therapyContainer = new app.views.medications.common.TherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.getView(),
      data: elementData,
      displayProvider: this.getDisplayProvider(),
      showIconTooltip: false,
      /* this is ugly but for some reason contentContainer's getParent() returns a different object ... */
      groupPanel: this,
      getGroupPanel: function()
      {
        return this.groupPanel;
      }
    });

    this.attachElementToolbar(therapyContainer);

    var currentBasketTherapies = this.getBasketTherapiesFunction();
    if (!tm.jquery.Utils.isEmpty(currentBasketTherapies) && currentBasketTherapies.indexOf(elementData) >= 0) //element is added to basket - different container css
    {
      var selectedCls = this.selectedCls;
      var oldCls = therapyContainer.getCls();
      therapyContainer.setCls(tm.jquery.Utils.isEmpty(oldCls) ? selectedCls : (oldCls + " " + selectedCls));
      therapyContainer.applyCls(therapyContainer.getCls());
      therapyContainer.getToolbar().hide();
    }

    return therapyContainer;
  },

  attachElementToolbar: function(elementContainer)
  {
    var self = this;
    var toolbar = new app.views.medications.mentalHealth.TherapyContainerToolbars({
      therapyContainer: elementContainer,
      addToBasketEventCallback: function(elementContainer)
      {
        self.addMedicationToBasketFunction(elementContainer);
      }
    });
    elementContainer.setToolbar(toolbar);
    elementContainer.getToolbar().show();
  },

  setElementAddedToBasket: function(elementData, addedToBasket)
  {
    if (!tm.jquery.Utils.isEmpty(elementData))
    {
      var index = this.getContentData().indexOf(elementData);
      var contentData = this.getContentData();
      if (index > -1 && index < contentData.length)
      {
        if (addedToBasket == false && this.addAllEnabled === true)
        {
          this.addAllImage.show();
        }
      }
    }
  },

  /* Override */
  setContentData: function(content, expandAfter)
  {
    this.contentData = tm.jquery.Utils.isEmpty(content) ? [] : content;

    if (expandAfter === true)
    {
      this._processContentData(true);
      this.getContent().repaint();
    }
    else
    {
      this.collapsed = true;
    }
  }
});