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
Class.define('tm.views.medications.pharmacists.ConfirmAllTherapiesPlaceholderContainer', 'tm.jquery.Container', {
  cls: 'placeholder-container',

  view: null,

  confirmAllTherapiesCallback: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("center", "center", 0));

    var self = this;
    var view = this.getView();

    var confirmAllTherapiesButton = new tm.jquery.Button({
      text: view.getDictionary("pharmacists.review.confirm.all"),
      type: "link",
      enabled: !tm.jquery.Utils.isEmpty(this.getView().getCurrentUserAsCareProfessional())
    });

    confirmAllTherapiesButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (!tm.jquery.Utils.isEmpty(self.confirmAllTherapiesCallback))
      {
        self.confirmAllTherapiesCallback();
      }
    });

    this.add(confirmAllTherapiesButton);

    this.add(new tm.jquery.Component({
      cls: "TextDataLight",
      html: view.getDictionary("or")
    }));
    this.add(new tm.jquery.Component({
      cls: "TextDataLight",
      html: view.getDictionary("select.therapy.from.list")
    }));
  },

  getReviewData: function()
  {
    return { createTimestamp: CurrentTime.get() };
  },

  getView: function()
  {
    return this.view;
  }
});