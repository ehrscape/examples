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

Class.define('tm.views.medications.TherapyFlowTodayButtons', 'tm.jquery.Container', {

  /** members: configs */
  view: null,
  dayTherapy: null,                     // [TherapyDayDto.java]
  actionFunction: null,
  rowId: null,
  /** members: interns */
  editButton: null,
  cancelButton: null,
  confirmButton: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    var appFactory = this.view.getAppFactory();
    this.setLayout(appFactory.createDefaultHFlexboxLayout('stretch', 'stretch', 5));
    this._buildComponents();
    this._buildGui();
  },

  _buildComponents: function()
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    this.editButton = new tm.jquery.Button(
        {
          cls: "edit-icon",
          width: 42
        });
    if (this.dayTherapy)
    {
      if (this.dayTherapy.therapy.medicationOrderFormType != enums.medicationOrderFormType.COMPLEX)
      {
        if (this.dayTherapy.therapy.medication.id != null)
        {
          this.editButton.setHandler(function()
          {
            self.view.showEditSimpleTherapyDialog(self.dayTherapy.therapy);
          });
        }
        else
        {
          this.editButton.setEnabled(false);
        }
      }
      else if (this.dayTherapy.therapy.medicationOrderFormType == enums.medicationOrderFormType.COMPLEX)
      {
        if (this.dayTherapy.therapy.ingredientsList[0].medication.id != null)
        {
          this.editButton.setHandler(function()
          {
            self.view.showEditComplexTherapyDialog(self.dayTherapy.therapy, false);
          });
        }
        else
        {
          this.editButton.setEnabled(false);
        }
      }
      else
      {
        this.editButton.setEnabled(false);
      }
    }
    this.cancelButton = new tm.jquery.Button(
        {
          cls: "delete-icon",
          width: 42,
          handler: function()
          {
            self._disableAllButtons();
            self.actionFunction(self.dayTherapy.therapy.compositionUid, self.dayTherapy.therapy.ehrOrderName, self.rowId, 'ABORT');
          }
        });

    this.confirmButton = new tm.jquery.Button(
        {
          cls: "confirm-icon",
          width: 42,
          handler: function()
          {
            var compositionUid = self.dayTherapy.therapy.compositionUid;
            var ehrOrderName = self.dayTherapy.therapy.ehrOrderName;

            self._disableAllButtons();
            var action = self.dayTherapy.therapyStatus == 'SUSPENDED' ? 'REISSUE' : 'CONFIRM';
            self.actionFunction(compositionUid, ehrOrderName, self.rowId, action);
          }
        });
  },

  _buildGui: function()
  {
    if (this.dayTherapy)
    {
      this.add(this.editButton);
      this.add(this.cancelButton);
      this.add(this.confirmButton);
    }
  },

  _disableAllButtons: function()
  {
    if (this.dayTherapy)
    {
      this.editButton.setEnabled(false);
      this.cancelButton.setEnabled(false);
      this.confirmButton.setEnabled(false);
    }
  }
});