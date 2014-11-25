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

Class.define('tm.views.medications.timeline.TherapyAdministrationCard', 'app.views.common.containers.AppBodyContentContainer', {
  scrollable: 'both',
  /** configs */
  view: null,
  displayProvider: null,
  latestTherapy: null,
  therapyFormattedDisplay: null,
  administration: null,
  style: "min-width:150px; max-width:600px; max-height:500px; padding: 5px 10px 10px 0px;",
  /** privates: components */
  mainContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.HFlexboxLayout.create("start", "stretch"));
    this._present();
  },

  _present: function()
  {
    var medicationIconContainer = new tm.jquery.Container({
      html: '<div class="' + tm.views.medications.MedicationUtils.getTherapyIcon(this.latestTherapy) + '"></div>'
    });
    this.add(medicationIconContainer);

    this.mainContainer = new tm.jquery.Container({layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)});
    this.add(this.mainContainer);

    this._presentTherapy();
    this._presentPlannedTime();
    this._presentPlannedDose();
    this.mainContainer.add(new tm.jquery.Container({cls: 'administration-card-bottom-border'}));
    this._presentInfusionSetChange();
    this._presentAdministrationStatus();
    this._presentSubstituteMedication();
    this._presentAdministrationTime();
    this._presentAdministeredDose();
    this._presentComment();
    this._presentComposer();
  },

  _presentTherapy: function()
  {
    this.mainContainer.add(new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 5),
      html: this.therapyFormattedDisplay,
      cls: 'TherapyDescription',
      style: 'border-bottom: 1px solid #646464; padding-bottom: 4px; margin-bottom: 4px; max-width:550px;'
    }));
  },

  _presentPlannedTime: function()
  {
    if (this.administration.plannedTime)
    {
      var row = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 5)});
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('administration.planned.time'), '1 0 0 0'));
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextData', Globalize.format(new Date(this.administration.plannedTime), 'K'), 0));
      this.mainContainer.add(row);
    }
  },

  _presentPlannedDose: function()
  {
    if (this.administration.plannedDose)
    {
      var plannedDose = this.administration.plannedDose;
      var row = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 5)
      });
      var plannedNumeratorString =
          plannedDose.numerator ? tm.views.medications.MedicationUtils.doubleToString(plannedDose.numerator, 'n2') + ' ' + plannedDose.numeratorUnit : '';
      var plannedDenominatorString =
          plannedDose.denominator ? tm.views.medications.MedicationUtils.doubleToString(plannedDose.denominator, 'n2') + ' ' + plannedDose.denominatorUnit : '';
      var plannedDoseString =
          plannedDenominatorString ? (plannedNumeratorString + ' / ' + plannedDenominatorString) : plannedNumeratorString;
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dose.planned'), '1 0 0 0'));
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextData', plannedDoseString, 0));
      this.mainContainer.add(row);
    }
  },

  _presentInfusionSetChange: function()
  {
    if (this.administration.infusionSetChangeEnum)
    {
      var row = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 5)});
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary("InfusionSetChangeEnum." + this.administration.infusionSetChangeEnum), '0 0 5 0'));
      this.mainContainer.add(row);
    }
  },

  _presentAdministrationStatus: function()
  {
    if (this.administration.administrationStatus)
    {
      var administrationStatusString = this.administration.administrationStatus == "FAILED" ?
          this.view.getDictionary('unsuccessful') : this.view.getDictionary('successful');
      var row = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 5)});
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('administration'), '1 0 0 0'));
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextData', administrationStatusString, 0));
      this.mainContainer.add(row);
    }
  },

  _presentSubstituteMedication: function()
  {
    if (this.administration.substituteMedication)
    {
      var medicationDisplay = this.displayProvider.getMedicationNameDisplay(this.administration.substituteMedication, true, false);
      var row = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 5)});
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary("medication"), '1 0 0 0'));
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextData', medicationDisplay, 0));
      this.mainContainer.add(row);
    }
  },

  _presentAdministrationTime: function()
  {
    if (this.administration.administrationTime)
    {
      var row = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 5)});
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('administration.time'), '1 0 0 0'));
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextData', Globalize.format(new Date(this.administration.administrationTime), 'K'), 0));
      this.mainContainer.add(row);
    }
  },

  _presentAdministeredDose: function()
  {
    if (this.administration.administeredDose)
    {
      var row = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 5)});
      var doseString = tm.views.medications.MedicationUtils.buildAdministeredDoseDisplayString(this.administration, false);
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dose'), '1 0 0 0'));
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextData', doseString, 0));
      this.mainContainer.add(row);
    }
  },

  _presentComment: function()
  {
    if (this.administration.comment)
    {
      var row = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 5)});
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('commentary'), '1 0 0 0'));
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextData', this.administration.comment, 0));
      this.mainContainer.add(row);
    }
  },

  _presentComposer: function()
  {
    if (this.administration.composerName)
    {
      var row = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 5)});
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('administered.by'), '1 0 0 0'));
      row.add(tm.views.medications.MedicationUtils.crateLabel('TextData', this.administration.composerName, 0));
      this.mainContainer.add(row);
    }
  }
});
/** public methods */

