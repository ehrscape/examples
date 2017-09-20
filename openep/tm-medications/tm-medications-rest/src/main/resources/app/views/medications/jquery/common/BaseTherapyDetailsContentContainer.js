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

Class.define('app.views.medications.common.BaseTherapyDetailsContentContainer', 'app.views.common.containers.AppBodyContentContainer', {
  cls: "therapy-details-content",
  view: null,

  medicationData: null,
  displayProvider: null,
  therapy: null,

  /**privates**/
  _contentContainer: null,
  _testRenderCoordinator: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    if (!tm.jquery.Utils.isArray(this.medicationData))
    {
      this.medicationData = [];
    }
    this._buildGui();

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'therapy-details-content-coordinator',
      view: this.getView(),
      component: this
    });
  },

  _buildGui: function()
  {

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    var mainContainer = new tm.jquery.Container({
      cls: "main-details-container",
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var headerContainer = new tm.jquery.Container({
      cls: "header-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var therapyIconContainer = new tm.jquery.Container({
      cls: this.getDisplayProvider().getTherapyIcon(this.getTherapy()) + " " + "icon-container",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    headerContainer.add(therapyIconContainer);

    var therapyContainer = new tm.jquery.Container({
      cls: "therapy-details-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    therapyContainer.add(this._buildTherapyDescriptionContainer());
    this._buildTherapyWarningsContainer(therapyContainer);
    headerContainer.add(therapyContainer);

    mainContainer.add(headerContainer);

    this._contentContainer = new tm.jquery.Container({
      cls: "content-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    mainContainer.add(this._contentContainer);
    this.add(mainContainer);
  },

  //Override in subclass - optional
  _buildTherapyWarningsContainer: function()
  {

  },

  _buildTherapyDescriptionContainer: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = tm.views.medications.MedicationUtils;
    var medicationDataList = this.getMedicationData();

    var therapyDescriptionContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    if (therapy.isOrderTypeComplex())
    {
      var ingredientList = therapy.getIngredientsList();
      for (var i = 0; i < ingredientList.length; i++)
      {
        var ingredient = ingredientList[i];
        var medication = ingredient.medication;
        var medicationData = this._getMedicationDataById(medication.getId());

        therapyDescriptionContainer.add(this._buildTherapyDescriptionRow(
            medication.getFormattedDisplayName(), this._buildQuantityFromIngredient(ingredient), "medication-ingredient"));
        if (medicationData)
        {
          therapyDescriptionContainer.add(utils.getSmcpButtonsContainer(medicationData, view));
          therapyDescriptionContainer.add(this._buildTherapyConflictIcons(medicationData));
        }
      }
      var volumeSumDisplay = therapy.getVolumeSumDisplay();

      if (!tm.jquery.Utils.isEmpty(volumeSumDisplay))
      {
        var volumeSumLabel = "<span class='TextLabel'>" + view.getDictionary("volume.total") + "</span>";
        var volumeSum = utils.getFormattedDecimalNumber(volumeSumDisplay);
        therapyDescriptionContainer.add(this._buildTherapyDescriptionRow(volumeSumLabel, volumeSum, "volume-sum"));
      }
    }
    else
    {
      therapyDescriptionContainer.add(this._buildTherapyDescriptionRow(
          therapy.getMedication().getFormattedDisplayName(),
          undefined,
          "medication-ingredient")
      );

      var medicationData = medicationDataList[0];
      if (medicationData)
      {
        therapyDescriptionContainer.add(utils.getSmcpButtonsContainer(medicationData, view));
        therapyDescriptionContainer.add(this._buildTherapyConflictIcons(medicationData));
      }
    }

    return therapyDescriptionContainer;
  },

  /**
   *
   * @param ingredient
   * @returns {String|null}
   * @private
   */
  _buildQuantityFromIngredient: function(ingredient)
  {
    var utils = tm.views.medications.MedicationUtils;

    if (ingredient.quantity)
    {
      var ingredientQuantity = utils.getFormattedDecimalNumber(utils.doubleToString(ingredient.quantity, 'n2')) + ' ' +
          utils.getFormattedUnit(ingredient.quantityUnit);
      if (ingredient.quantityDenominator)
      {
        ingredientQuantity += " / " + utils.getFormattedDecimalNumber(utils.doubleToString(ingredient.quantityDenominator, 'n2')) + ' ' +
            utils.getFormattedUnit(ingredient.quantityDenominatorUnit);
      }
      return ingredientQuantity;
    }
    return null;
  },

  /**
   * @param {String} therapyDescription
   * @param {String|undefined} [therapyDetails=undefined]
   * @param {String|undefined} [testAttributeName=undefined]
   * @private
   */
  _buildTherapyDescriptionRow: function(therapyDescription, therapyDetails, testAttributeName)
  {
    var therapyDescriptionRow = new tm.jquery.Container({
      cls: "therapy-description-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var therapyDescriptionColumn = new tm.jquery.Container({
      cls: "therapy-description-column",
      html: therapyDescription,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    therapyDescriptionRow.add(therapyDescriptionColumn);

    if (therapyDetails)
    {
      var therapyDetailsColumn = new tm.jquery.Container({
        cls: "TextData",
        html: therapyDetails,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });
      therapyDescriptionRow.add(therapyDetailsColumn);
    }

    if (testAttributeName)
    {
      therapyDescriptionRow.addTestAttribute(testAttributeName);
    }
    return therapyDescriptionRow;
  },

  /**
   * @param {String} labelValue
   * @param {String|null} descriptionValue
   * @param {String|null} [testAttributeName=null]
   * @private
   */
  _buildLabelDataRowContainer: function(labelValue, descriptionValue, testAttributeName)
  {
    var contentContainerRow = new tm.jquery.Container({
      cls: "content-container-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
    });

    if (testAttributeName)
    {
      contentContainerRow.addTestAttribute(testAttributeName);
    }

    var rowLabel = new tm.jquery.Container({
      cls: "TextLabel row-label",
      html: labelValue,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    contentContainerRow.add(rowLabel);

    var rowDescription = new tm.jquery.Container({
      cls: "TextData",
      html: descriptionValue,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    contentContainerRow.add(rowDescription);

    return contentContainerRow;
  },

  /**
   * @param {String} medicationId
   * @returns {Object|*}
   * @private
   */
  _getMedicationDataById: function(medicationId)
  {
    var medicationDataList = this.getMedicationData();
    for (var i = 0; i < medicationDataList.length; i++)
    {
      if (medicationDataList[i].getMedication().getId() === medicationId)
      {
        return medicationDataList[i];
      }
    }
    return null;
  },

  /**
   * Getters & Setters
   */

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @returns {app.views.medications.TherapyDisplayProvider}
   */
  getDisplayProvider: function()
  {
    return this.displayProvider;
  }
});