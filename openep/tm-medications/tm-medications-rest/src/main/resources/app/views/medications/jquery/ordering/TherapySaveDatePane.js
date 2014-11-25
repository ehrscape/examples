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

Class.define('app.views.medications.ordering.TherapySaveDatePane', 'tm.jquery.Container', {

  /** configs */
  saveDateTime: null,
  /** privates */
  /** privates: components */
  saveDateField: null,
  saveTimeField: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.HFlexboxLayout.create("start", "start", 5));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var saveDateTime = this.saveDateTime ? this.saveDateTime : new Date();
    this.saveDateField = new tm.jquery.DatePicker({date: saveDateTime});
    this.saveTimeField = new tm.jquery.TimePicker({time: saveDateTime});
  },

  _buildGui: function()
  {
    this.add(this.saveDateField);
    this.add(this.saveTimeField);
  },

  /** public methods */
  getSaveDateTime: function()
  {
    var saveDate = this.saveDateField.getDate();
    var saveTime = this.saveTimeField.getTime();
    return new Date(
        saveDate.getFullYear(),
        saveDate.getMonth(),
        saveDate.getDate(),
        saveTime.getHours(),
        saveTime.getMinutes(),
        0,
        0);
  }
});

