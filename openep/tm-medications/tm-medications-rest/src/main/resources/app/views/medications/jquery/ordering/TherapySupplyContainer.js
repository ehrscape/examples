/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.ordering.TherapySupplyContainer', 'tm.jquery.Container', {
  cls: "therapy-supply-container",
  scrollable: 'visible',
  view: null,

  medicationId: null,
  routeId: null,
  supplyMedications: null,

  calculateButtonClickHandler: null,

  _whenNeededButton: null,
  _durationField: null,
  _durationUnitSelectBox: null,

  _quantityField: null,
  _quantityUnitSelectBox: null,
  _quantityCalcButton: null,

  _supplyUnitsButton: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;
    var view = this.getView();

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0));

    var durationField = new tm.jquery.TextField({
      width: 70,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var durationUnitOptions = this.getDurationUnits().map(function(item)
    {
      return tm.jquery.SelectBox.createOption(item, null);
    });
    var durationUnitSelectBox = new tm.jquery.SelectBox({
      cls: "left-margin",
      liveSearch: false,
      options: durationUnitOptions,
      selections: durationUnitOptions.length > 0 ? [durationUnitOptions[0]] : [],
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      dropdownWidth: "stretch",
      allowSingleDeselect: false,
      defaultValueCompareToFunction: this._defaultSelectBoxValueCompareToFunction,
      defaultTextProvider: this._defaultSelectBoxTextProvider
    });
    var durationWrapper = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("medication.supply.duration"),
      scrollable: 'visible',
      contentComponent: new tm.jquery.Container({
        scrollable: 'visible',
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')

      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
    durationWrapper.getContentComponent().add(durationField);
    durationWrapper.getContentComponent().add(durationUnitSelectBox);

    var quantityField = new tm.jquery.TextField({
      width: 70,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    quantityField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_LOST, function(component){
      if (self.calculateButtonClickHandler)
      {
        self.calculateButtonClickHandler();
      }
    });

    var quantityUnitOptions = this.getQuantityUnits().map(function(item)
    {
      return tm.jquery.SelectBox.createOption(item, null);
    });
    var quantityUnitSelectBox = new tm.jquery.SelectBox({
      cls: "left-margin",
      liveSearch: false,
      options: quantityUnitOptions,
      selections: quantityUnitOptions.length > 0 ? [quantityUnitOptions[0]] : [],
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      dropdownWidth: "stretch",
      allowSingleDeselect: false,
      defaultValueCompareToFunction: this._defaultSelectBoxValueCompareToFunction,
      defaultTextProvider: this._defaultSelectBoxTextProvider
    });
    var quantityCalcButton = new tm.jquery.Button({
      cls: "left-margin",
      text: "Select supply", //view.getDictionary("select.supply"),
      handler: function()
      {
        self._onOpenControlDrugsSupplyContainer(this, self.medicationId, self.routeId, null, null, function(data)
          //TODO nejc add correct number of doses and dose quantity
        {
          self.setSupplyUnits(data)
        });
      }
    });
    var supplyWrapper = new tm.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      titleText: view.getDictionary("medication.supply.quantity"),
      contentComponent: new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
        scrollable: 'visible'
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
    var supplyUnitsButton = new tm.jquery.Button({
      cls: "supply-units-list",
      type: "link",
      style: "text-align: left;",
      hidden: true,
      handler: function()
      {
        if (self.calculateButtonClickHandler)
        {
          self.calculateButtonClickHandler();
        }
      }
    });
    supplyWrapper.getContentComponent().add(quantityField);
    supplyWrapper.getContentComponent().add(quantityUnitSelectBox);
    //supplyWrapper.getContentComponent().add(quantityCalcButton);
    //supplyWrapper.getContentComponent().add(supplyUnitsButton);

    var flexEndWrapper = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });
    var whenNeededButton = new tm.jquery.ToggleButton({
      cls: "when-needed-button",
      text: this.view.getDictionary("when.needed.short")
    });
    whenNeededButton.setTooltip(tm.views.medications.MedicationUtils.createTooltip(view.getDictionary("when.needed"), "left", view));
    flexEndWrapper.add(whenNeededButton);

    this.add(durationWrapper);
    this.add(supplyWrapper);
    this.add(flexEndWrapper);

    this._whenNeededButton = whenNeededButton;
    this._durationField = durationField;
    this._durationUnitSelectBox = durationUnitSelectBox;
    this._quantityField = quantityField;
    this._quantityCalcButton = quantityCalcButton;
    this._quantityUnitSelectBox = quantityUnitSelectBox;
    this._supplyUnitsButton = supplyUnitsButton;
  },

  _defaultSelectBoxValueCompareToFunction: function(value1, value2)
  {
    return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
        === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
  },

  _defaultSelectBoxTextProvider: function(selectBox, index, option)
  {
    return option.getValue().title;
  },

  _onOpenControlDrugsSupplyContainer: function(button, medicationId, routeId, doseQuantity, numberOfDoses, callback)
  {
    var self = this;
    this.getView().getMedicationSupplyCandidates(medicationId, routeId, function(data)
    {
      var controlDrugsSupplyContainer = new app.views.medications.ordering.ControlDrugsSupplyContainer({
        view: self,
        data: data,
        doseQuantity: 140, // doseQuantity TODO nejc apply real data
        numberOfDoses: 20, // numberOfDoses
        padding: "5",
        scrollable: 'vertical'
      });

      var dialog = self.getView().getAppFactory().createDataEntryDialog(
          "Calculate Supply",
          null,
          controlDrugsSupplyContainer,
          function(resultData)
          {
            if (resultData)
            {
              callback(resultData.value);
            }
            else
            {
            }
          },
          685, 450
      );

      dialog.header.setCls("therapy-admin-header");
      dialog.setLeft(43);
      dialog.getFooter().setCls("therapy-admin-footer");
      dialog.getFooter().rightContainer.layout.gap = 0;
      dialog.setLeftButtons([controlDrugsSupplyContainer.resetButton, controlDrugsSupplyContainer.calculatedLabel]);
      dialog.show();
    });
  },

  getView: function()
  {
    return this.view;
  },

  getDurationUnits: function()
  {
    var view = this.getView();
    var durationUnits = [];

    durationUnits.push({
      id: "days",
      title: view.getDictionary("days")
    });
    durationUnits.push({
      id: "weeks",
      title: view.getDictionary("weeks")
    });
    durationUnits.push({
      id: "months",
      title: view.getDictionary("month.plural.lc")
    });
    return durationUnits;
  },

  getQuantityUnits: function()
  {
    var view = this.getView();
    var supplyUnits = [];

    supplyUnits.push({
      id: "packages",
      title: view.getDictionary("packages")
    });

    return supplyUnits;
  },

  getWhenNeeded: function()
  {
    return this._whenNeededButton.isPressed();
  },

  setWhenNeeded: function(whenNeeded)
  {
    return this._whenNeededButton.setPressed(whenNeeded);
  },

  getFormValidations: function()
  {
    var self = this;
    var formFields = [];

    var _isPositiveInteger = function(n)
    {
      return n >>> 0 === parseFloat(n);
    };

    var _getPositiveIntegerFromString = function(string)
    {
      if (string && tm.jquery.Utils.isNumeric(string) && _isPositiveInteger(string))
      {
        return parseInt(string);
      }
      return null;
    };

    var _durationQuantityFieldValidation = function()
    {
      var durationVal = self._durationField.getValue() ? self._durationField.getValue() : null;
      if (durationVal && !_getPositiveIntegerFromString(durationVal))
      {
          return null;
      }

      var quantityVal = self._quantityField.getValue() ? self._quantityField.getValue() : null;
      if (quantityVal && !_getPositiveIntegerFromString(quantityVal))
      {
        return null;
      }

      return durationVal || quantityVal ? true : null;
    };

    formFields.push(new tm.jquery.FormField({
      component: this._durationField,
      required: true,
      componentValueImplementationFn: function()
      {
        return _durationQuantityFieldValidation();
      }
    }));
    formFields.push(new tm.jquery.FormField({
      component: this._quantityField,
      required: true,
      componentValueImplementationFn: function()
      {
        return _durationQuantityFieldValidation();
      }
    }));
    return formFields;
  },

  clear: function()
  {
    var durationOptions = this._durationUnitSelectBox.getOptions();
    var quantityOptions = this._quantityUnitSelectBox.getOptions();
    this._whenNeededButton.setPressed(false);
    this._durationField.setValue(null);
    this._durationUnitSelectBox.setSelections(durationOptions.length > 0 ? [durationOptions[0].value] : null);
    this._quantityField.setValue(null);
    this._supplyUnitsButton.setText(null);
    this._quantityUnitSelectBox.setSelections(quantityOptions.length > 0 ? [quantityOptions[0].value] : null);

    this._supplyUnitsButton.hide();
    this._quantityField.show();
    this._quantityUnitSelectBox.show();
    this._quantityCalcButton.show();
  },

  setSupplyUnits: function(data)
  {
    this._quantityField.hide();
    this._quantityUnitSelectBox.hide();
    this._quantityCalcButton.hide();

    var unitListHtml = '';
    this.setSupplyMedications(data);
    data.forEach(function(item)
    {
      unitListHtml += '<p>' + item.name + '-' + item.quantity + ' ' + item.basicUnit + '</p>';
    });

    this._supplyUnitsButton.setText(unitListHtml);
    this._supplyUnitsButton.show();
  },

  getSupply: function()
  {
    var durationUnitSelectionId = this._durationUnitSelectBox.getSelections()[0].id;
    var durationValue = this._durationField.getValue() ? this._durationField.getValue() : null;
    if (!tm.jquery.Utils.isNumeric(durationValue))
    {
      durationValue = null;
    }
    if (durationValue)
    {
      if (durationUnitSelectionId === "months")
      {
        durationValue = durationValue * 30;
      } 
      else if (durationUnitSelectionId === "weeks")
      {
        durationValue = durationValue * 7;
      }
    }
    var quantityFieldValue = this._quantityField.getValue();
    return {
      daysDuration: durationValue,
      quantity: quantityFieldValue && tm.jquery.Utils.isNumeric(quantityFieldValue) ? this._quantityField.getValue() : null,
      unit: this._quantityUnitSelectBox.getSelections().length > 0 ? this._quantityUnitSelectBox.getSelections()[0].value : null
    };
  },

  setSupply: function(supply)
  {
    this._durationField.setValue(supply.daysDuration);
    this._quantityField.setValue(supply.quantity);
    if (supply.unit)
    {
      this._quantityUnitSelectBox.setSelections([supply.unit]);
    }
  },

  setRouteId: function(id)
  {
    this.routeId = id;
  },

  setSupplyMedications: function(value)
  {
    this.supplyMedications = value;
  },

  setMedicationId: function(value)
  {
    this.medicationId = value;
  }
});