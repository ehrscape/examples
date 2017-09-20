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

Class.define('app.views.medications.common.MedicationDetailsContainer', 'tm.jquery.Container', {
  cls: "medication-details-container",
  scrollable: "vertical",
  /** config */
  view: this.view,
  medicationData: null,
  selectedRoute: null, /* optional: set if route selection possible */

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * @private
   */
  _buildGui: function()
  {
    var self = this;
    this.medicationData.forEach(function(medicationData)
    {
      if (!medicationData.getMedication().isMedicationUniversal())
      {
        var infoContainer = new app.views.medications.therapy.MedicationDetailsCardPane({
          view: self.view,
          medicationData: medicationData,
          selectedRoute: self.selectedRoute ? self.selectedRoute : medicationData.defaultRoute
        });
        self.add(infoContainer);
      }
    });
  }
});