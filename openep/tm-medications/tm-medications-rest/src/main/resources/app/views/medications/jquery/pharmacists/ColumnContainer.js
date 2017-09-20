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
Class.define('tm.views.medications.pharmacists.ColumnContainer', 'tm.jquery.Container', {
  /* public members */
  listContainer: null,
  columnTitle: null,

  /* private members */
  _titleComponent: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var header = new tm.jquery.Container({
      cls: "column-title",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0)
    });
    var titleText = new tm.jquery.Component({
      cls: "PortletHeading1",
      html: this.getColumnTitle(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    header.add(titleText);

    var listContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      cls: "list-container",
      scrollable: "vertical",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this.add(header);
    this.add(listContainer);

    this.listContainer = listContainer;
    this._titleComponent = titleText;
  },

  getListContainer: function ()
  {
    return this.listContainer;
  },

  getColumnTitle: function ()
  {
    return this.columnTitle;
  },

  setColumnTitle: function (value)
  {
    this.columnTitle = value;

    if (!tm.jquery.Utils.isEmpty(this._titleComponent))
    {
      this._titleComponent.setHtml(value);
    }
  }
});