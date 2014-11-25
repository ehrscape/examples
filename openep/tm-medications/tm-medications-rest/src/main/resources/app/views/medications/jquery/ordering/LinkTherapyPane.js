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
  linkIndex: null,

  /** components **/
  list: null,

  /** privates*/
  resultCallback: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);

    var appFactory = this.view.getAppFactory();
    this.setLayout(appFactory.createDefaultVFlexboxLayout("start", "stretch"));
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
      flex: 1,
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
    var self = this;
    var container = new tm.jquery.Container({
      padding: '5 5 5 10',
      layout: new tm.jquery.HFlexboxLayout({
        alignment: new tm.jquery.FlexboxLayoutAlignment({
          pack: 'start',
          align: 'start'
        })})
    });

    container.add(new tm.jquery.Container({
      margin: '2 0 0 0',
      height: 20,
      width: 20,
      cls: therapy.completed == false ? 'incomplete-therapy-icon' : ""
    }));

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
    return container;
  },

  _setListData: function(therapies)
  {
    for (var i = 0; i < therapies.length; i++)
    {
      var therapy = therapies[i];
      if (!tm.jquery.Utils.isEmpty(therapy.end) && tm.jquery.Utils.isEmpty(therapy.linkToTherapy))
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

    if (selectedTherapy != null)
    {
      selectedTherapy.linkToTherapy = this.linkIndex;
    }
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
