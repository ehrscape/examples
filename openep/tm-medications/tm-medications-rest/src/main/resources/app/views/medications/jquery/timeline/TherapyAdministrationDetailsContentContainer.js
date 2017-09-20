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

Class.define('tm.views.medications.timeline.TherapyAdministrationDetailsContentContainer', 'app.views.medications.common.BaseTherapyDetailsContentContainer', {
  scrollable: 'both',

  administration: null,
  reasonMap: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.reasonMap = this.view.getTherapyChangeReasonTypeMap();
    this._buildContentContainer();
  },

  _buildContentContainer: function()
  {
    var enums = app.views.medications.TherapyEnums;

    this._addAdministrationResultRow();
    this._addAdministrationTimeRow();
    this._addInfusionSetVolumeRow();

    this._addInfusionSetChangeRow();
    this._addSubstituteMedicationRow();
    this._addWitnessRow();
    this._addCommentRow();
    this._addComposerRow();

    if ((this.getAdministration().administrationResult === enums.administrationResultEnum.GIVEN ||
        this.getAdministration().administrationResult === enums.administrationResultEnum.SELF_ADMINISTERED))
    {
      this._addAdministeredVolumeRow();
      this._addAdministeredQuantityRow();
      this._addAdministeredDoseRow();
      this._addStartingDeviceRow();
    }

    this._addPlannedTimeRow();
    this._addPlannedDoseRow();
    this._addPlannedStartingDeviceRow();
    this._addDoctorsCommentRow();
  },

  _addPlannedTimeRow: function()
  {
    if (this.getAdministration().plannedTime)
    {
      var view = this.getView();
      this._contentContainer.add(new tm.jquery.Container({cls: 'administration-card-border'}));
      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('administration.planned.time'),
          view.getDisplayableValue(new Date(this.getAdministration().plannedTime), "short.date.time")));
    }
  },

  _addPlannedDoseRow: function()
  {
    if (this.getAdministration().plannedDose)
    {
      var view = this.getView();
      var utils = tm.views.medications.MedicationUtils;

      var plannedDose = this.getAdministration().plannedDose;
      var isOxygen = this.getTherapy().isOrderTypeOxygen();

      var plannedNumeratorString =
          plannedDose.numerator ? utils.getFormattedDecimalNumber(utils.doubleToString(plannedDose.numerator, 'n2')) + ' ' +
          utils.getFormattedUnit(plannedDose.numeratorUnit) : '';
      var plannedDenominatorString =
          plannedDose.denominator ? utils.getFormattedDecimalNumber(utils.doubleToString(plannedDose.denominator, 'n2')) +
          ' ' + utils.getFormattedUnit(plannedDose.denominatorUnit) : '';
      var plannedDoseString =
          plannedDenominatorString ? (plannedNumeratorString + ' / ' + plannedDenominatorString) : plannedNumeratorString;

      this._contentContainer.add(this._buildLabelDataRowContainer(
          isOxygen ? view.getDictionary('planned.rate') : view.getDictionary('dose.planned'), plannedDoseString));
    }
  },

  _addInfusionSetChangeRow: function()
  {
    if (this.getAdministration().infusionSetChangeEnum)
    {
      var view = this.getView();

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("InfusionSetChangeEnum." +
          this.getAdministration().infusionSetChangeEnum), ""));
    }
  },

  _addInfusionSetVolumeRow: function()
  {
    if (this.getAdministration().infusionBag && this.getAdministration().infusionBag.quantity != null)
    {
      var infusionBagQuantity = this.getAdministration().infusionBag.quantity;
      var infusionBagUnit = this.getAdministration().infusionBag.unit;
      var infusionBagChange = infusionBagQuantity + " " + infusionBagUnit;

      var view = this.getView();
      this._contentContainer.add(
          this._buildLabelDataRowContainer(view.getDictionary("bag.syringe.volume"), infusionBagChange));
    }
  },

  _addAdministrationResultRow: function()
  {
    if (this.getAdministration().administrationResult)
    {
      var map;
      var reason;
      var reasonCode;
      var administrationResultString = "";
      var enums = app.views.medications.TherapyEnums;
      var view = this.getView();

      if (this.getAdministration().administrationResult === enums.administrationResultEnum.GIVEN)
      {
        administrationResultString += view.getDictionary("given");
      }
      else if (this.getAdministration().administrationResult === enums.administrationResultEnum.DEFER)
      {
        map = this.reasonMap.ADMINISTRATION_DEFER;
        reasonCode = this.getAdministration().notAdministeredReason.code;
        administrationResultString += view.getDictionary("defer");
        if (!tm.jquery.Utils.isEmpty(reasonCode))
        {
          reason = this._getReasonFromMap(map, reasonCode);
          administrationResultString += ", " + reason.name;
        }
      }
      else if (this.getAdministration().administrationResult === enums.administrationResultEnum.SELF_ADMINISTERED)
      {
        administrationResultString += view.getDictionary("self.administration");
        if (this.getAdministration().selfAdministrationType === enums.selfAdministrationTypeEnum.LEVEL_2)
        {
          administrationResultString += ", " + view.getDictionary("charted.by.nurse");
        }
        else if (this.getAdministration().selfAdministrationType === enums.selfAdministrationTypeEnum.LEVEL_3)
        {
          administrationResultString += ", " + view.getDictionary("automatically.charted");
        }
      }
      else if (this.getAdministration().administrationResult == enums.administrationResultEnum.NOT_GIVEN)
      {
        map = this.reasonMap.ADMINISTRATION_NOT_GIVEN;
        reasonCode = this.getAdministration().notAdministeredReason.code;
        administrationResultString += view.getDictionary("not.given");
        if (!tm.jquery.Utils.isEmpty(reasonCode))
        {
          reason = this._getReasonFromMap(map, reasonCode);
          if (!tm.jquery.Utils.isEmpty(reason))
          {
            administrationResultString += ", " + reason.name;
          }
        }
      }
      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("administration"),
          administrationResultString));
    }
  },

  _addSubstituteMedicationRow: function()
  {
    if (this.getAdministration().substituteMedication)
    {
      var view = this.getView();
      var medicationDisplay = this.getDisplayProvider().getMedicationNameDisplay(this.getAdministration().substituteMedication,
          true, false);

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("medication"),
          medicationDisplay));
    }
  },

  _addAdministrationTimeRow: function()
  {
    if (this.getAdministration().administrationTime)
    {
      var view = this.getView();

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("administration.time"),
          view.getDisplayableValue(new Date(this.getAdministration().administrationTime), "short.date.time")));
    }
  },

  _addAdministeredVolumeRow: function()
  {
    if (this.getAdministration().administeredDose &&
        this.getAdministration().administeredDose.therapyDoseTypeEnum === 'RATE_VOLUME_SUM')
    {
      var view = this.getView();
      var utils = tm.views.medications.MedicationUtils;
      var volumeData = utils.getFormattedDecimalNumber(
          utils.doubleToString(this.getAdministration().administeredDose.secondaryNumerator, 'n2'))
          + ' ' + utils.getFormattedUnit(this.getAdministration().administeredDose.secondaryNumeratorUnit);

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("volume.total"), volumeData));
    }
  },

  _addAdministeredQuantityRow: function()
  {
    if (this.getAdministration().administeredDose &&
        this.getAdministration().administeredDose.therapyDoseTypeEnum === 'RATE_QUANTITY')
    {
      var view = this.getView();
      var utils = tm.views.medications.MedicationUtils;
      var quantityData = utils.getFormattedDecimalNumber(
              utils.doubleToString(this.getAdministration().administeredDose.secondaryNumerator, 'n2')) + ' ' +
          utils.getFormattedUnit(this.getAdministration().administeredDose.secondaryNumeratorUnit) + ' / '
          + utils.getFormattedDecimalNumber(
              utils.doubleToString(this.getAdministration().administeredDose.secondaryDenominator, 'n2'))
          + ' ' + utils.getFormattedUnit(this.getAdministration().administeredDose.secondaryDenominatorUnit);

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("quantity"), quantityData));

    }
  },

  _addAdministeredDoseRow: function()
  {
    if (this.getAdministration().administeredDose)
    {
      var view = this.getView();
      var isOxygen = this.getTherapy().isOrderTypeOxygen();
      var doseString = tm.views.medications.MedicationUtils.buildAdministeredDoseDisplayString(
          this.getAdministration(), false);

      this._contentContainer.add(this._buildLabelDataRowContainer(isOxygen ? view.getDictionary('rate') :
          view.getDictionary('dose'), doseString));
    }
  },
  _addPlannedStartingDeviceRow: function()
  {
    if (this.getAdministration().plannedStartingDevice)
    {
      var view = this.getView();
      var route = this.getAdministration().plannedStartingDevice.route;
      var routeType = this.getAdministration().plannedStartingDevice.routeType;
      var plannedStartingDeviceString = view.getDictionary('OxygenDeliveryCluster.Route.' + route);

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('device'),
          routeType ? plannedStartingDeviceString + " " + routeType : plannedStartingDeviceString));
    }
  },

  _addStartingDeviceRow: function()
  {
    if (this.getAdministration().startingDevice)
    {
      var view = this.getView();
      var route = this.getAdministration().startingDevice.route;
      var routeType = this.getAdministration().startingDevice.routeType;
      var startingDeviceString = view.getDictionary('OxygenDeliveryCluster.Route.' + route);

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('device'),
          routeType ? startingDeviceString + " " + routeType : startingDeviceString));
    }
  },

  _addWitnessRow: function()
  {
    if (this.getAdministration().witness)
    {
      var view = this.getView();
      var witnessString = this.getAdministration().witness.name;

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('witness'), witnessString));
    }
  },

  _addCommentRow: function()
  {
    if (this.getAdministration().comment)
    {
      var view = this.getView();

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('commentary'),
          this.getAdministration().comment));
    }
  },

  _addComposerRow: function()
  {
    if (this.getAdministration().composerName)
    {
      var view = this.getView();

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('administered.by'),
          this.getAdministration().composerName));
    }
  },
  _getReasonFromMap: function(map, code)
  {
    if (tm.jquery.Utils.isEmpty(map)) return {};

    var reason = null;
    map.forEach(function(r)
    {
      if (r.code == code) reason = r
    });
    return reason;
  },

  _addDoctorsCommentRow: function()
  {
    if (this.getAdministration().doctorsComment)
    {
      var view = this.getView();

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('doctors.comment'),
          this.getAdministration().doctorsComment));
    }
  },
  /**
   * Getters & Setters
   */

  /**
   * @returns {Object|null}
   */
  getAdministration: function()
  {
    return this.administration;
  }

});