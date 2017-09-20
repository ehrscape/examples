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
Class.define('app.views.medications.outpatient.EERContentExtensionContainer', 'app.views.medications.ordering.PrescriptionContentExtensionContainer', {
  cls: "eer-prescription-extension",

  whitePrescriptionStyleName: "white-prescription",
  scrollable: "visible",

  _exceedMaxDoseCheckBox: null,
  _doNotSwitchCheckBox: null,
  _whitePrescriptionCheckBox: null,
  _pharmacistInstructionsField: null,
  _repetitionsField: null,
  _acuteCheckBox: null,
  _chronicCheckBox: null,
  _magistralPreparationCheckBox: null,
  _paymentOptionSelectBox: null,
  _paymentOptionsComponent: null,
  _urgentCheckBox: null,

  _numberOfMedicationsInTherapy: 1,
  _medicationData: null,
  _cbzCodeExists: true,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var view = this.getView();
    var self = this;

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch"));

    this.add(new tm.jquery.Container({
      cls: "status-line",
      alignSelf: "stretch",
      scrollable: "visible",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "5px")
    }));

    var contentLayout = new tm.jquery.Container({
      cls: "content",
      scrollable: "visible",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch")
    });

    var firstColumn = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "visible"
    });

    var magistralPreparationComponent = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('type.of.prescription'),
      scrollable: "visible",
      contentComponent: new tm.jquery.CheckBox({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        labelText: view.getDictionary("magistral.preparation"),
        checked: false,
        labelAlign: "right",
        scrollable: "visible"
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    firstColumn.add(magistralPreparationComponent);

    var typeOfCoverageComponent = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('type.of.coverage'),
      scrollable: "visible",
      cls: "vertically-titled-component type-of-coverage",
      contentComponent: new tm.jquery.CheckBox({
        labelText: view.getDictionary('white.prescription'),
        scrollable: "visible",
        checked: false,
        labelAlign: "right"
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    typeOfCoverageComponent.getContentComponent().on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self.setWhitePrescription(component.checked === true);
    });

    firstColumn.add(typeOfCoverageComponent);

    var prescriptionRow = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: "visible"
    });

    prescriptionRow.add(firstColumn);

    var secondColumn = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("center", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "visible"
    });

    var repetitionsComponent = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('prescription.repetitions'),
      cls: "vertically-titled-component prescription-repetitions",
      contentComponent: new tm.jquery.TextField({
        alignSelf: "flex-start",
        width: 80,
        scrollable: "visible",
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var paymentOptions = this.getPaymentOptions().map(function(item)
    {
      return tm.jquery.SelectBox.createOption(item, null);
    });
    var paymentOptionsComponent = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('prescription.payment'),
      cls: "vertically-titled-component payment-options",
      hidden: true,
      scrollable: "visible",
      contentComponent: new tm.jquery.SelectBox({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        cls: "left-margin",
        liveSearch: false,
        options: paymentOptions,
        selections: paymentOptions.length > 0 ? [paymentOptions[0]] : [],
        width: 80,
        dropdownWidth: "auto",
        allowSingleDeselect: false,
        defaultValueCompareToFunction: this._defaultSelectBoxValueCompareToFunction,
        defaultTextProvider: this._defaultSelectBoxTextProvider
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    secondColumn.add(repetitionsComponent);
    secondColumn.add(paymentOptionsComponent);
    prescriptionRow.add(secondColumn);

    var thirdColumn = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-end", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: 'visible'
    });

    var urgentCheckBoxComponent = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('additional.information'),
      scrollable: "visible",
      contentComponent: new tm.jquery.CheckBox({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        labelText: view.getDictionary("urgent"),
        checked: false,
        labelAlign: "right",
        scrollable: "visible"
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var exceedMaxDoseCheckBox = new tm.jquery.CheckBox({
      labelText: view.getDictionary('exceed.maximum.dose'),
      checked: false,
      labelAlign: "right"
    });
    var switchCheckBox = new tm.jquery.CheckBox({
      labelText: view.getDictionary('do.not.switch'),
      checked: false,
      labelAlign: "right"
    });
    var acuteChronicContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      cls: 'acute-chronic-container'
    });
    var acuteCheckBox = new tm.jquery.CheckBox({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      labelText: view.getDictionary("acute"),
      checked: false,
      labelAlign: "right"
    });
    var chronicCheckBox = new tm.jquery.CheckBox({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      labelText: view.getDictionary("chronic"),
      checked: false,
      labelAlign: "right"
    });
    acuteCheckBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (component.isChecked())
      {
        chronicCheckBox.setChecked(false, true);
      }
    });
    chronicCheckBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (component.isChecked())
      {
        acuteCheckBox.setChecked(false, true);
      }
    });

    acuteChronicContainer.add(acuteCheckBox);
    acuteChronicContainer.add(chronicCheckBox);

    thirdColumn.add(urgentCheckBoxComponent);
    thirdColumn.add(exceedMaxDoseCheckBox);
    thirdColumn.add(switchCheckBox);
    thirdColumn.add(acuteChronicContainer);
    prescriptionRow.add(thirdColumn);

    contentLayout.add(prescriptionRow);
    var instructionComponent = new tm.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('instructions.for.pharmacist'),
      scrollable: "visible",
      contentComponent: new tm.jquery.TextField({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%")
      })
    });
    contentLayout.add(instructionComponent);

    this.add(contentLayout);

    this._magistralPreparationCheckBox = magistralPreparationComponent.getContentComponent();
    this._whitePrescriptionCheckBox = typeOfCoverageComponent.getContentComponent();
    this._repetitionsField = repetitionsComponent.getContentComponent();
    this._paymentOptionSelectBox = paymentOptionsComponent.getContentComponent();
    this._paymentOptionsComponent = paymentOptionsComponent;
    this._urgentCheckBox = urgentCheckBoxComponent.getContentComponent();
    this._exceedMaxDoseCheckBox = exceedMaxDoseCheckBox;
    this._doNotSwitchCheckBox = switchCheckBox;
    this._pharmacistInstructionsField = instructionComponent.getContentComponent();
    this._acuteCheckBox = acuteCheckBox;
    this._chronicCheckBox = chronicCheckBox;
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

  getPaymentOptions: function()
  {
    var view = this.getView();
    var payerEnum = app.views.medications.TherapyEnums.medicationAuthorisationSloveniaClusterPayer;
    var paymentOptions = [];

    paymentOptions.push({
      id: payerEnum.PERSON,
      title: view.getDictionary("MedicationAuthorisationSloveniaCluster.Payer." + payerEnum.PERSON)
    });
    paymentOptions.push({
      id: payerEnum.MO,
      title: view.getDictionary("MedicationAuthorisationSloveniaCluster.Payer." + payerEnum.MO)
    });
    paymentOptions.push({
      id: payerEnum.UPB,
      title: view.getDictionary("MedicationAuthorisationSloveniaCluster.Payer." + payerEnum.UPB)
    });
    paymentOptions.push({
      id: payerEnum.OTHER,
      title: view.getDictionary("MedicationAuthorisationSloveniaCluster.Payer." + payerEnum.OTHER)
    });
    return paymentOptions;
  },

  /* override, add form validators */
  getFormValidations: function()
  {
    var _isPositiveInteger = function(n)
    {
      return n >>> 0 === parseFloat(n);
    };

    var validators = [];
    validators.push(new tm.jquery.FormField({
      component: this._repetitionsField,
      required: true,
      componentValueImplementationFn: function(component)
      {
        var value = component.getValue();

        if (value)
        {
          return tm.jquery.Utils.isNumeric(value) && _isPositiveInteger(value) ? true : null;
        }
        else
        {
          return true;
        }
      }
    }));
    return validators;
  },

  setWhitePrescription: function(isWhite)
  {
    var originalCls = tm.jquery.Utils.isEmpty(this.getCls()) ? "" : this.getCls();
    var whiteStyle = this.whitePrescriptionStyleName;

    if (isWhite === true)
    {
      if (!originalCls.contains(whiteStyle))
      {
        this.setCls(originalCls.concat(" " + whiteStyle));
        this._paymentOptionsComponent.show();
        this._paymentOptionsComponent.focus();
      }
    }
    else
    {
      if (originalCls.contains(whiteStyle))
      {
        this.setCls(originalCls.replace(" " + whiteStyle, ""));
        this._paymentOptionsComponent.hide();
      }
    }
  },

  /* override, add data to therapy */
  buildTherapy: function(therapy)
  {
    var instructionsToPharmacist =
        this._pharmacistInstructionsField.getValue() ? this._pharmacistInstructionsField.getValue() : null;
    var repetitionsFieldValue = this._repetitionsField.getValue();
    var repetitions =
        repetitionsFieldValue && tm.jquery.Utils.isNumeric(repetitionsFieldValue) ? repetitionsFieldValue : null;

    var payer = null;
    var prescriptionDocumentType = null;
    var enums = app.views.medications.TherapyEnums;

    if (this._whitePrescriptionCheckBox.isChecked())
    {
      prescriptionDocumentType = enums.prescriptionDocumentType.WHITE;
      payer = this._paymentOptionSelectBox.getSelections()[0].id;
    }
    else
    {
      prescriptionDocumentType = enums.prescriptionDocumentType.GREEN;
    }
    var illnessConditionType = null;
    if (this._acuteCheckBox.isChecked())
    {
      illnessConditionType = enums.illnessConditionType.ACUTE_CONDITION;
    }
    else if (this._chronicCheckBox.isChecked())
    {
      illnessConditionType = enums.illnessConditionType.CHRONIC_CONDITION;
    }

    therapy.prescriptionLocalDetails = {
      prescriptionDocumentType: prescriptionDocumentType,
      illnessConditionType: illnessConditionType,
      prescriptionRepetition: repetitions,
      payer: payer,
      instructionsToPharmacist: instructionsToPharmacist,
      prescriptionSystem: 'EER',
      maxDoseExceeded: this._exceedMaxDoseCheckBox.isChecked(),
      doNotSwitch: this._doNotSwitchCheckBox.isChecked(),
      magistralPreparation: this._magistralPreparationCheckBox.isChecked(),
      urgent: this._urgentCheckBox.isChecked()
    }
  },

  setComponentsFromTherapy: function(therapy)
  {
    if (therapy && therapy.hasOwnProperty("prescriptionLocalDetails"))
    {
      var enums = app.views.medications.TherapyEnums;

      if (therapy.prescriptionLocalDetails.prescriptionDocumentType === enums.prescriptionDocumentType.WHITE)
      {
        this._whitePrescriptionCheckBox.setChecked(true);
        this.setWhitePrescription(true);
        this._paymentOptionSelectBox.setSelections([{id: therapy.prescriptionLocalDetails.payer}]);

      }
      else
      {
        this._whitePrescriptionCheckBox.setChecked(false);
        this.setWhitePrescription(false);
      }

      this._pharmacistInstructionsField.setValue(therapy.prescriptionLocalDetails.instructionsToPharmacist);
      this._repetitionsField.setValue(therapy.prescriptionLocalDetails.prescriptionRepetition);
      this._magistralPreparationCheckBox.setChecked(therapy.prescriptionLocalDetails.magistralPreparation);
      this._exceedMaxDoseCheckBox.setChecked(therapy.prescriptionLocalDetails.maxDoseExceeded);
      this._doNotSwitchCheckBox.setChecked(therapy.prescriptionLocalDetails.doNotSwitch);
      this._urgentCheckBox.setChecked(therapy.prescriptionLocalDetails.urgent);

      if (therapy.prescriptionLocalDetails.illnessConditionType === enums.illnessConditionType.ACUTE_CONDITION)
      {
        this._acuteCheckBox.setChecked(true);
        this._chronicCheckBox.setChecked(false);
      }
      else if (therapy.prescriptionLocalDetails.illnessConditionType === enums.illnessConditionType.CHRONIC_CONDITION)
      {
        this._acuteCheckBox.setChecked(false);
        this._chronicCheckBox.setChecked(true);
      }
      else
      {
        this._acuteCheckBox.setChecked(false);
        this._chronicCheckBox.setChecked(false);
      }

      if (therapy.ingredientsList)
      {
        this._numberOfMedicationsInTherapy = therapy.ingredientsList.length;
      }
      else
      {
        this._numberOfMedicationsInTherapy = 1;
      }
      this._handleMagistralPreparation();
    }
  },

  _handleMagistralPreparation: function()
  {
    if (this._numberOfMedicationsInTherapy > 1 || this._cbzCodeExists === false)
    {
      this._magistralPreparationCheckBox.setChecked(true);
      this._magistralPreparationCheckBox.setEnabled(false);
    }
    else
    {
      if (this._medicationData.antibiotic)
      {
        this._magistralPreparationCheckBox.setChecked(false);
        this._magistralPreparationCheckBox.setEnabled(false);
      }
      else
      {
        this._magistralPreparationCheckBox.setEnabled(true);
      }
    }
  },

  _loadMedicationCbzCode: function()
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_MEDICATION_EXTERNAL_ID;
    var params = {
      externalSystem: "CBZ",
      medicationId: self._medicationData.medication.id
    };

    view.loadViewData(url, params, null, function(data)
    {
      setTimeout(function yieldToPossiblePaint()
      {
        if (params.medicationId == self._medicationData.medication.id)
        {

          self._cbzCodeExists = !tm.jquery.Utils.isEmpty(data);
          self._handleMagistralPreparation();
        }
      }, 0);
    });
  },

  /* override */
  therapyMedicationsCountChangedFunction: function(numberOfMedications)
  {
    this._numberOfMedicationsInTherapy = numberOfMedications;
    this._handleMagistralPreparation();
  },

  /* override */
  setMedicationData: function(medicationData)
  {
    this._medicationData = medicationData;
    if (!medicationData.getMedication().isMedicationUniversal())
    {
      this._loadMedicationCbzCode();
    }
    else
    {
      this._cbzCodeExists = false;
      this._handleMagistralPreparation();
    }
  },

  /* clear content implementation */
  clear: function()
  {
    var paymentOptions = this._paymentOptionSelectBox.getOptions();

    this._paymentOptionSelectBox.setSelections(paymentOptions.length > 0 ? [paymentOptions[0].value] : null);
    this._magistralPreparationCheckBox.setChecked(false);
    this._whitePrescriptionCheckBox.setChecked(false);
    this.setWhitePrescription(false);
    this._exceedMaxDoseCheckBox.setChecked(false);
    this._doNotSwitchCheckBox.setChecked(false);
    this._urgentCheckBox.setChecked(false);
    this._pharmacistInstructionsField.setValue(null);
    this._repetitionsField.setValue(null);
    this._acuteCheckBox.setChecked(false);
    this._chronicCheckBox.setChecked(false);
    this._numberOfMedicationsInTherapy = 1;
  }
});