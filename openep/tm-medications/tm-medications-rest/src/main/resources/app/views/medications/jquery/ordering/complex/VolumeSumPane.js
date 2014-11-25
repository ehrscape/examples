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

Class.define('app.views.medications.ordering.VolumeSumPane', 'tm.jquery.Container', {

  /** configs */
  view: null,
  adjustVolumesEvent: null,
  /** privates */
  /** privates: components */
  adjustButton: null,
  volumeField: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(new tm.jquery.HFlexboxLayout({
      gap: 5,
      alignment: new tm.jquery.FlexboxLayoutAlignment({
        pack: 'end',
        align: 'center'
      })})
    );
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.adjustButton = new tm.jquery.Button({
      cls: 'update-icon',
      handler: function()
      {
        self.adjustVolumesEvent();
      }
    });
    this.volumeField = tm.views.medications.MedicationUtils.createNumberField('n2', 68, '0');
    this.volumeField.setEnabled(false);
  },

  _buildGui: function()
  {
    this.add(this.adjustButton);
    this.add(this.volumeField);
    this.add(tm.views.medications.MedicationUtils.crateLabel('TextData', 'ml'));
  },

  /** public methods */

  getVolumeSum: function()
  {
    return this.volumeField.getValue();
  },

  setVolumeSum: function(volume)
  {
    this.volumeField.setValue(volume);
  },

  clear: function()
  {
    this.volumeField.setValue(null);
  }
});
