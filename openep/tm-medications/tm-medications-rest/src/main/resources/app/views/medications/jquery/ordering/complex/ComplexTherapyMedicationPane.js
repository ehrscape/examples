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

Class.define('app.views.medications.ordering.ComplexTherapyMedicationPane', 'tm.jquery.Container', {
  scrollable: 'visible',
  /** configs */
  view: null,
  typeaheadAdvancedMode: false,
  medicationData: null, //optional  MedicationDataDto.java
  addSpacer: false,
  searchEnabled: false,
  addRemoveEnabled: true,
  addElementEvent: null,
  removeElementEvent: null,
  volumeChangedEvent: null,
  numeratorChangeEvent: null,
  medicationChangedEvent: null,
  focusLostEvent: null, //optional
  closeDialogFunction: null, //optional
  templates: null, //optional

  /** privates */
  settingValue: false,
  medicationEditableSameGenericOnly: false,
  /** privates: components */
  medicationInfo: null,
  addButton: null,
  removeButton: null,
  doseContainer: null,
  buttonsContainer: null,
  medicationField: null,
  medicationTypeLabel: null,
  dosePane: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(new tm.jquery.VFlexboxLayout({
      alignment: new tm.jquery.FlexboxLayoutAlignment({
        pack: 'start',
        align: 'stretch'
      })
    }));
    this._buildComponents();
    this._buildGui();
    if (this.medicationData)
    {
      this._setMedicationType(this.medicationData.medication.medicationType);
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var medicationDetailsPane = new app.views.medications.therapy.MedicationDetailsCardPane({view: self.view});
    var detailsCardTooltip = appFactory.createDefaultPopoverTooltip(
        self.view.getDictionary("medication"),
        null,
        medicationDetailsPane
    );
    detailsCardTooltip.onShow = function()
    {
      medicationDetailsPane.setMedicationData(self.medicationData);
    };
    this.medicationInfo = new tm.jquery.Container({cls: 'info-icon pointer-cursor', width: 20, height: 30, margin: '0 0 0 8', tooltip: detailsCardTooltip});

    this.addButton = new tm.jquery.Container({cls: 'add-icon', width: 30, height: 30});
    this.addButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self.addElementEvent(self);
    });
    this.removeButton = new tm.jquery.Container({cls: 'remove-icon', width: 30, height: 30});
    this.removeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self.removeElementEvent();
    });
    //this.medicationField = tm.views.medications.MedicationUtils.createMedicationsSearchField(
    //    this.view,
    //    636,
    //    this.typeaheadAdvancedMode ? 'advanced' : 'basic',
    //    this.medicationData ? this.medicationData.medication : null,
    //    this.searchEnabled
    //);
    this.medicationField = new app.views.medications.MedicationSearchField({
      view: this.view,
      width: 636,
      enabled: this.searchEnabled,
      preselectedMedication: this.medicationData ? this.medicationData.medication : null,
      medicationSelectedFunction: function(medicationId)
      {
        if (self.templates && self.templates.patientTemplates && self.templates.patientTemplates.length > 0)
        {
          var patientTemplates = self.templates.patientTemplates;
          var templatesContainMedication = tm.views.medications.MedicationUtils.assertTemplatesContainMedication(medicationId, patientTemplates);
          if (!templatesContainMedication)
          {
            var message = self.view.getDictionary('medication.not.in.patient.templates');
            self.view.getAppFactory().createConfirmSystemDialog(message, function(params)
            {
              if (params == true)
              {
                self._readMedicationData(medicationId);
              }
              else
              {
                self.medicationField.clear();
              }
            }, 320, 160).show();
          }
          else
          {
            self._readMedicationData(medicationId);
          }
        }
        self._readMedicationData(medicationId);
      }});

    //if (this.searchEnabled)
    //{
    //  this.medicationField.setSource(this.view.getMedications());
    //  this.medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    //  {
    //    if (!self.settingValue)
    //    {
    //      var selection = component.getSelection();
    //      if (selection)
    //      {
    //        self._readMedicationData(selection.id)
    //      }
    //      else
    //      {
    //        self.dosePane.hide();
    //        self.dosePane.clear();
    //      }
    //    }
    //  });
    //}
    this.medicationTypeLabel = new tm.jquery.Container({cls: 'TextData', width: 151, padding: '5 0 0 40'});
    this.dosePane = new app.views.medications.ordering.DosePane({
      margin: '0 0 0 5',
      view: this.view,
      pack: 'end',
      width: 447,
      denominatorAlwaysVolume: false, //version for england - check before merge to master
      volumeChangedEvent: function()
      {
        self.volumeChangedEvent();
      },
      numeratorChangeEvent: function()
      {
        self.numeratorChangeEvent();
      },
      focusLostEvent: function()
      {
        if (self.focusLostEvent)
        {
          self.focusLostEvent(self);
        }
      }
    });
    if (this.medicationData)
    {
      this.dosePane.setMedicationData(this.medicationData);
    }
    else
    {
      this.dosePane.hide();
      this.dosePane.clear();
    }
  },

  _buildGui: function()
  {
    var mainContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout(), height: 67, scrollable: 'visible', padding: '0 0 0 20'});
    if (this.addSpacer)
    {
      this.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));
    }

    var rowsContainer = new tm.jquery.Container({layout: new tm.jquery.VFlexboxLayout(), height: 67, scrollable: 'visible'});
    var searchContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout(), height: 30, scrollable: 'visible'});
    searchContainer.add(this.medicationField);
    searchContainer.add(this.medicationInfo);
    rowsContainer.add(searchContainer);
    rowsContainer.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));

    this.doseContainer = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({
      alignment: new tm.jquery.FlexboxLayoutAlignment({
        pack: 'end',
        align: 'start'
      })
    })});

    this.doseContainer.add(this.addButton);
    this.doseContainer.add(this.removeButton);
    this.doseContainer.add(this.medicationTypeLabel);
    this.doseContainer.add(this.dosePane);
    rowsContainer.add(this.doseContainer);
    mainContainer.add(rowsContainer);
    this.setAddRemoveButtonsVisible(this.addRemoveEnabled);

    this.add(mainContainer);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));
    this.add(new tm.jquery.Container({style: 'border-bottom: 1px solid #d6d6d6'}));
  },

  _setMedicationType: function(medicationType)
  {
    this.medicationTypeLabel.setHtml(this.view.getDictionary('MedicationTypeEnum.' + medicationType));
  },

  _readMedicationData: function(medicationId, callback)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var medicationDataUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MEDICATION_DATA;
    var params = {medicationId: medicationId};
    this.view.loadViewData(medicationDataUrl, params, null, function(medicationData)
    {
      if (medicationData)
      {
        self.medicationData = medicationData;
        self.dosePane.show();
        self.dosePane.setMedicationData(medicationData);
        self._setMedicationType(medicationData.medication.medicationType);
        self.requestFocusToDose();
        if (callback)
        {
          callback();
        }
        self.medicationChangedEvent(medicationData);
      }
      else
      {
        var message = self.view.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            self.view.getDictionary('abort.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 320, 150).show();
        if (self.closeDialogFunction)
        {
          self.closeDialogFunction();
        }
      }
    });
  },

  /** public methods */
  getVolume: function()
  {
    var dose = this.dosePane.getDose();
    if (dose.quantityDenominator)
    {
      return dose.quantityDenominator;
    }
    return 0;
  },

  setVolume: function(volume)
  {
    this.dosePane.setDoseDenominator(volume);
  },

  getMedicationData: function()
  {
    return this.medicationData;
  },

  setMedicationAndDose: function(medication, numerator, volume, medicationLoadedEvent)
  {
    var self = this;
    if (medication)
    {
      this._readMedicationData(medication.id, function()
      {
        setTimeout(function()
        {
          self.settingValue = true;
          self.medicationField.setMedication(medication);
          self.settingValue = false;
        }, 100);

        self.dosePane.setDoseNumerator(numerator);
        self.dosePane.setDoseDenominator(volume);
        if (medicationLoadedEvent)
        {
          medicationLoadedEvent();
        }
      });
    }
  },

  setDose: function(numerator, volume)
  {
    this.dosePane.setDoseNumerator(numerator);
    this.dosePane.setDoseDenominator(volume);
  },

  getInfusionIngredient: function()
  {
    var dose = this.dosePane.getDose();
    if (this.medicationData)
    {
      return {
        medication: this.medicationData.medication,
        quantity: dose.quantity,
        quantityUnit: tm.views.medications.MedicationUtils.getStrengthNumeratorUnit(this.medicationData),
        volume: dose.quantityDenominator,
        volumeUnit: 'ml',
        doseForm: this.medicationData.doseForm
      }
    }
    return null;
  },

  requestFocusToDose: function()
  {
    this.dosePane.requestFocusToDose();
  },

  focusToMedicationField: function()
  {
    this.medicationField.focus();
  },

  getMedicationPaneValidations: function()
  {
    var self = this;
    var formFields = [];
    if (!this.dosePane.isHidden())
    {
      formFields = formFields.concat(this.dosePane.getDosePaneValidations());
    }
    formFields.push(new tm.jquery.FormField({
      component: self.medicationField.getTextField(),
      required: true
    }));
    return formFields;
  },

  setDoseVisible: function(visible)
  {
    if (visible)
    {
      this.dosePane.show();
    }
    else
    {
      this.dosePane.hide();
      this.dosePane.clear();
    }
  },

  setPaneEditable: function(showAddRemoveButtons, medicationEditable, medicationEditableSameGenericOnly, doseEditable, repaint)
  {
    this.setAddRemoveButtonsVisible(showAddRemoveButtons);
    this.medicationField.setEnabled(medicationEditable);
    this.dosePane.setPaneEditable(doseEditable);
    if (repaint)
    {
      this.doseContainer.repaint();
    }
    if (this.medicationEditableSameGenericOnly != medicationEditableSameGenericOnly)
    {
      //this._loadSimilarMedications();
      //this.medicationEditableSameGenericOnly = medicationEditableSameGenericOnly
    }
  },

  setTemplates: function(templates)
  {
    this.templates = templates;
  },

  _loadSimilarMedications: function()
  {
    //Similar medications have same generic, route, atc and custom group
    var self = this;
    if (this.medicationData)
    {
      var medicationsUrl =
          this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_SIMILAR_MEDICATIONS;
      var params = {
        medicationId: this.medicationData.medication.id,
        routeCode: null
      };
      this.view.loadViewData(medicationsUrl, params, null, function(similarMedications)
      {
        self.medicationField.setSource(similarMedications);
      });
    }
  },

  setAddRemoveButtonsVisible: function(visible)
  {
    if (visible)
    {
      this.addButton.setHeight(30);
      this.removeButton.setHeight(30);
    }
    else
    {
      this.addButton.setHeight(0);
      this.removeButton.setHeight(0);
    }
  }
});

