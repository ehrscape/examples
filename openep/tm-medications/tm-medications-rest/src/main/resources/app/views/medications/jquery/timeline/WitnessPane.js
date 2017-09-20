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
Class.define('tm.views.medications.timeline.WitnessPane', 'tm.jquery.Container', {
  cls: 'witness-pane',
  view: null,
  _authenticatedWitness: null,
  _waitingForWitnessAuthenticationImg: null,
  _witnessAuthenticationSuccessfulImg: null,
  _witnessAuthenticationFailedImg: null,
  _witnessAuthenticationRequestButton: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.HFlexboxLayout.create("center", "flex-end"));
    this.setFlex(new tm.jquery.flexbox.item.Flex.create(0, 0, "auto"));
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;
    this._waitingForWitnessAuthenticationImg = new tm.jquery.Container({
      cls: 'loader witness-loader',
      alignSelf: "center",
      hidden: true
    });

    this._witnessAuthenticationSuccessfulImg = new tm.jquery.Container({
      cls: "CheckLabel",
      width: 16,
      height: 16,
      alignSelf: "center",
      hidden: true
    });

    this._witnessAuthenticationFailedImg = new tm.jquery.Container({
      cls: "CrossLabel",
      width: 16,
      height: 16,
      alignSelf: "center",
      hidden: true
    });

    this._witnessAuthenticationRequestButton = new tm.jquery.Button({
      cls: 'btn-bubble',
      width: 200,
      text: "Witness authentication",
      alignSelf: "center",
      handler: function()
      {
        self._requestWitnessConfirmation();
      }
    });

    this.add(this._waitingForWitnessAuthenticationImg);
    this.add(this._witnessAuthenticationFailedImg);
    this.add(this._witnessAuthenticationSuccessfulImg);
    this.add(this._witnessAuthenticationRequestButton);
  },

  _requestWitnessConfirmation: function()
  {
    this._authenticatedWitness = null;
    this._waitingForWitnessAuthenticationImg.show();
    this._witnessAuthenticationFailedImg.hide();
    this._witnessAuthenticationRequestButton.setEnabled(false);
    this._witnessAuthenticationSuccessfulImg.hide();
    this.getView().sendAction(tm.views.medications.TherapyView.VIEW_ACTION_AUTHENTICATE_ADMINISTRATION_WITNESS, {});
  },

  /**
   *
   * @returns {Object|null}
   */
  getAuthenticatedWitness: function()
  {
    return this._authenticatedWitness;
  },

  /**
   *
   * @param {Object|null} actionData
   */
  setAuthenticatedWitness: function(actionData)
  {
    this._witnessAuthenticationRequestButton.setEnabled(true);
    if (actionData && actionData.witness)
    {
      this._authenticatedWitness = actionData.witness;
      this._witnessAuthenticationSuccessfulImg.show();
      this._witnessAuthenticationFailedImg.hide();
    }
    else
    {
      this._authenticatedWitness = null;
      this._witnessAuthenticationFailedImg.show();
      this._witnessAuthenticationSuccessfulImg.hide();
    }
    this._waitingForWitnessAuthenticationImg.hide();
  },

  /**
   *
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});
