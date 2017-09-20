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

Class.define('app.views.medications.therapy.MedicationDetailsCardPane', 'app.views.common.containers.AppBodyContentContainer', {
  cls: 'medication-details-card',
  scrollable: 'both',
  /** configs */
  view: null,
  medicationData: null,
  selectedRoute: null,
  /** privates: components */

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (this.medicationData)
    {
      this._buildGui(this.medicationData, this.selectedRoute);
    }
  },

  /** private methods */
  _buildGui: function(medicationData, selectedRoutes)
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0));

    var iconContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      width: 48,
      height: 48,
      cls: this._getMedicationIcon(medicationData.doseForm)
    });

    var contentContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      cls: 'detail'
    });

    var medicationDetailInfo = '';
    //medication
    if (medicationData.medication.genericName)
    {
      medicationDetailInfo += '<p class="TextDataBold">' + medicationData.medication.genericName + '</p>';
      medicationDetailInfo += '<p class="TextData">(' + medicationData.medication.name + ')</p>';
    }
    else
    {
      medicationDetailInfo += '<p class="TextDataBold">' + medicationData.medication.name + '</p>';
    }
    if (medicationData.doseForm)
    {
      //dose form
      medicationDetailInfo += '<span class="TextLabel">' + this.view.getDictionary("dose.form") + ' </span>';
      medicationDetailInfo += '<span class="TextDataBold">' + medicationData.doseForm.name + ' </span>';
      medicationDetailInfo += '<br>';
    }
    if (!tm.jquery.Utils.isEmpty(medicationData.getMedicationPackaging()))
    {
      medicationDetailInfo += '<span class="TextLabel">' + this.view.getDictionary("medication.packaging") + ' </span>';
      medicationDetailInfo += '<span class="TextDataBold">' + medicationData.getMedicationPackaging() + '</span>';
      medicationDetailInfo += '<br>';
    }

    if (!tm.jquery.Utils.isEmpty(medicationData.getPrice()))
    {
      medicationDetailInfo += '<span class="TextLabel">' + this.view.getDictionary("price") + ' </span>';
      medicationDetailInfo += '<span class="TextDataBold">' + medicationData.getPrice() + '</span>';
      medicationDetailInfo += '<br>';
    }
    var medicationDataCom = new tm.jquery.Component({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: medicationDetailInfo
    });

    contentContainer.add(medicationDataCom);

    //medication ingredients
    if (medicationData.getMedicationIngredients().length > 0)
    {
      var medicationIngredientsStrength = "";
      for (var i = 0; i < medicationData.getMedicationIngredients().length; i++)
      {
        var strengthString =
            tm.views.medications.MedicationUtils.getStrengthDisplayString(medicationData.getMedicationIngredients()[i], true);
        if (strengthString)
        {
          medicationIngredientsStrength += '<span class="TextDataBold">' + strengthString + ' </span>';
          medicationIngredientsStrength += '<span class="TextDataBold">' +
              ' (' + medicationData.getMedicationIngredients()[i].ingredientName + ')' +
              ' </span>';
        }
      }
      if (medicationData.getDescriptiveIngredient())
      {
        medicationIngredientsStrength += '<span class="TextLabel">' + this.view.getDictionary("together") + ' </span>';
        medicationIngredientsStrength += '<span class="TextDataBold">' +
            tm.views.medications.MedicationUtils.getStrengthDisplayString(medicationData.getDescriptiveIngredient(), true) +
            ' </span>';
      }
      if (medicationIngredientsStrength)
      {
        var medicationIngredient = '<span class="TextLabel">' + this.view.getDictionary("strength") + ' </span>';
        medicationIngredient += medicationIngredientsStrength;
        var medicationIngredientCom = new tm.jquery.Component({
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
          html: medicationIngredient
        });

        contentContainer.add(medicationIngredientCom);
      }

      var smcpButtons = tm.views.medications.MedicationUtils.getSmcpButtonsContainer(medicationData, this.getView());
      contentContainer.add(smcpButtons);
    }
    if (tm.jquery.Utils.isArray(selectedRoutes) && selectedRoutes.length === 1
        && !tm.jquery.Utils.isEmpty(selectedRoutes[0].bnfMaximumDto))
    {
      var bnfConfiguredMax = '<span class="TextLabel">' + this.getView().getDictionary("configured.max") + ' ' + ' </span>';
      bnfConfiguredMax += '<span class="TextDataBold">' + selectedRoutes[0].bnfMaximumDto.quantity + ' ' +
          'mg' + '/' + selectedRoutes[0].bnfMaximumDto.quantityUnit.toLowerCase() + ' </span>';
      bnfConfiguredMax += '<br>';

      var medicationInfoBnfMax = new tm.jquery.Component({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        html: bnfConfiguredMax
      });

      contentContainer.add(medicationInfoBnfMax);
    }
    this.add(iconContainer);
    this.add(contentContainer);
  },

  _getMedicationIcon: function(doseForm)
  {
    if (doseForm && doseForm.doseFormType == 'TBL')
    {
      return "icon_pills";
    }
    return "icon_other_medication";
  },

  /** public methods */
  setMedicationData: function(medicationData, selectedRoutes)
  {
    if (medicationData)
    {
      this.removeAll();
      this._buildGui(medicationData, tm.jquery.Utils.isEmpty(selectedRoutes) ? null : selectedRoutes);
    }
  },

  /**
   * Getters & Setters
   */
  getView: function()
  {
    return this.view;
  }
});