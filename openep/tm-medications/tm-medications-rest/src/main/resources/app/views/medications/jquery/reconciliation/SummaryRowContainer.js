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
Class.define('app.views.medications.reconciliation.SummaryRowContainer', 'tm.jquery.Container', {
  cls: "summary-row",
  scrollable: "visible",

  _columns: null,

  columnCount: 3,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._columns = [];
    this._buildGui();
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));
    this.setFlex(tm.jquery.flexbox.item.Flex.create(0, 0, "auto"));
    var colWidth = Math.round((100 / this.getColumnCount()) * 100) / 100;
    var totalWidth = 100;

    for (var idx = 0; idx < this.getColumnCount(); idx++)
    {
      var column = new tm.jquery.Container({
        cls: "summary-col",
        scrollable: "visible",
        layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, (totalWidth < colWidth ? totalWidth : colWidth) + "%")
      });
      this._columns.push(column);
      this.add(column);
      totalWidth = totalWidth - colWidth;
    }
  },

  getColumnCount: function()
  {
    return this.columnCount;
  },

  getColumn: function(number){
    return (number > this._columns.length-1 || number < 0) ? null : this._columns[number];
  }
});