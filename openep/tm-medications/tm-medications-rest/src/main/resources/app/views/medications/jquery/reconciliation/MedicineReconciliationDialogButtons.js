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
Class.define('app.views.medications.reconciliation.MedicineReconciliationDialogButtons', 'app.views.common.containers.AppConfirmCancelFooterButtonsContainer', {
  cls: "reconciliation-dialog-buttons",
  confirmButton: null,
  cancelButton: null,
  nextButton: null,
  backButton: null,

  confirmText: null,
  cancelText: null,
  nextText: null,
  backText: null,

  Constructor: function(config)
  {
    this.callSuper(config);

    this.confirmButton = this._createButton(this.confirmText, true, true);
    this.cancelButton = this._createButton(this.cancelText, true, false, "link");
    this.nextButton = this._createButton(this.nextText, true, false);
    this.backButton = this._createButton(this.backText, true, true);

    this.setRightButtons([this.backButton, this.nextButton, this.confirmButton, this.cancelButton]);
    this.rightContainer.setLayout(tm.jquery.HFlexboxLayout.create("flex-end", "center", 0));
  },

  _createButton: function(text, enabled, hidden, type) {
    return new tm.jquery.Button({
      margin: "0 5 0 5",
      alignSelf: "center",
      type: tm.jquery.Utils.isEmpty(type) ? "default" : type,
      text: tm.jquery.Utils.isEmpty(text) ? "" : text,
      enabled: enabled === false ? false : true,
      hidden: hidden === true ? true : false,
    });
  },

  getNextButton: function() {
    return this.nextButton;
  },

  getBackButton: function() {
    return this.backButton;
  }
});