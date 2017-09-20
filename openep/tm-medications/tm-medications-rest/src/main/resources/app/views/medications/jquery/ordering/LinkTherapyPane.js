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

Class.define('app.views.medications.ordering.LinkTherapyPane', 'app.views.common.containers.AppDataEntryContainer', {

  /** configs */
  view: null,
  orderedTherapies: null,
  /** components **/
  list: null,
  /** privates*/
  resultCallback: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._setListData(self.orderedTherapies);
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.list = new tm.jquery.List({
      cls: 'link-candidates-list',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      autoLoad: false,
      dataSource: [],
      itemTpl: function(index, item)
      {
        return self._buildRow(index, item.therapy);
      },
      selectable: true
    });
  },
  _buildGui: function()
  {
    this.add(this.list);
  },

  _buildRow: function(index, therapy)
  {
    var container = new tm.jquery.Container({
      cls: 'link-candidates-row',
      padding: '5 5 5 10',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "start")
    });

    container.add(new tm.jquery.Container({
      margin: '2 0 0 0',
      height: 20,
      width: 20,
      cls: therapy.completed == false ? 'incomplete-therapy-icon' : ""
    }));

    var therapyContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      html: therapy.formattedTherapyDisplay,
      cls: 'TherapyDescription'
    });

    if (therapy.completed == false)
    {
      container.setCls('incomplete-therapy');
    }
    container.add(therapyContainer);
    return container;
  },

  _setListData: function(therapies)
  {
    for (var i = 0; i < therapies.length; i++)
    {
      var therapy = therapies[i];
      var otherTherapiesLinkedToTherapy =
          tm.views.medications.MedicationUtils.areOtherTherapiesLinkedToTherapy(therapy.linkName, therapies);
      if (!tm.jquery.Utils.isEmpty(therapy.end) && !otherTherapiesLinkedToTherapy)
      {
        var rowData = {therapy: therapies[i]};
        this.list.addRowData(rowData, i);
      }
    }
    if (this.list.getListData().length == 1)
    {
      this.list.setSelections([this.list.getListData()[0]]);
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    var selectedTherapy = this.list.getSelections().isEmpty() ? null : this.list.getSelections().get(0).therapy;

    if (tm.jquery.Utils.isEmpty(selectedTherapy))
    {
      var message = this.view.getDictionary('you.have.no.therapies.selected');
      this.view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
      this.resultCallback(new app.views.common.AppResultData({success: false}));
    }
    else
    {
      this.resultCallback(new app.views.common.AppResultData({success: true, selectedTherapy: selectedTherapy}));
    }
  }
});
