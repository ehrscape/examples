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

Class.define('app.views.medications.ordering.OrderingContainer', 'tm.jquery.Container', {
  cls: "ordering-container",
  /** configs */
  view: null,
  confirmTherapyEvent: null,
  saveDateTimePaneEvent: null,
  removeFromBasketFunction: null,
  addTherapiesToBasketFunction: null,
  saveTherapyToTemplateFunction: null,
  linkTherapyFunction: null,
  isPastMode: false,
  presetDate: null, //optional
  /** privates: components */
  header: null,
  cardContainer: null,
  templatesContainer: null,
  simpleTherapyContainer: null,
  complexTherapyContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(new tm.jquery.BorderLayout());
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var patientDataString;
    if (this.view.getReferenceWeight())
    {
      var referenceWeightValueString = tm.views.medications.MedicationUtils.doubleToString(this.view.getReferenceWeight(), 'n3') + ' kg';
      var bodySurfaceAreaString;
      if (this.view.getPatientData().heightInCm)
      {
        var bodySurfaceArea = tm.views.medications.MedicationUtils.calculateBodySurfaceArea(this.view.getPatientData().heightInCm, this.view.getReferenceWeight());
        bodySurfaceAreaString = tm.views.medications.MedicationUtils.doubleToString(bodySurfaceArea, 'n3') + ' m2';
      }
      patientDataString = "<span style='text-transform: none; color: #63818D'>" + this.view.getDictionary('reference.weight') + ": " + referenceWeightValueString + "</span>";
      if (bodySurfaceAreaString)
      {
        patientDataString = patientDataString + " - <span style='text-transform: none; color: #63818D'>" + this.view.getDictionary('body.surface') + ": " + bodySurfaceAreaString + "</span>";
      }
    }
    var patientDataContainer = new tm.jquery.Container({
      cls: 'TextLabel',
      horizontalAlign: 'right',
      html: patientDataString,
      flex: 1});
    this.header = new app.views.medications.ordering.MedicationsTitleHeader({
      view: this.view,
      title: this.presetDate ? this.view.getDictionary('therapy.order') + "  " + this.view.getDisplayableValue(this.presetDate, "short.date") : this.view.getDictionary('therapy.order'),
      additionalDataContainer: patientDataContainer
    });
    this.cardContainer = new tm.jquery.CardContainer({width: 720});
    this.templatesContainer = new app.views.medications.ordering.TemplatesContainer({
      view: this.view,
      addTherapiesToBasketFunction: this.addTherapiesToBasketFunction,
      editTherapyFunction: function(therapy)
      {
        self._editTherapy(therapy, false);
      },
      medicationSelectedEvent: function(medicationData)
      {
        self._handleMedicationSelected(medicationData, true);
      }});
    this.simpleTherapyContainer = new app.views.medications.ordering.SimpleTherapyContainer({
      view: this.view,
      isPastMode: this.isPastMode,
      presetDate: this.presetDate,
      changeCardEvent: function(data)
      {
        if (data == 'TEMPLATES')
        {
          self.clear();
        }
        else
        {
          self.complexTherapyContainer.setMedicationData(data, true);
          self.cardContainer.setActiveItem(self.complexTherapyContainer);
        }
      },
      saveToTemplateFunction: function(therapy, invalidTherapy)
      {
        self.saveTherapyToTemplateFunction(therapy, invalidTherapy);
      },
      linkTherapyFunction: function(callback)
      {
        self.linkTherapyFunction(callback);
      },
      confirmTherapyEvent: function(therapy)
      {
        return self.confirmTherapyEvent(therapy);
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePaneEvent();
      }
    });
    this.complexTherapyContainer = new app.views.medications.ordering.ComplexTherapyContainer({
      view: this.view,
      startProcessOnEnter: true,
      isPastMode: this.isPastMode,
      presetDate: this.presetDate,
      getTemplatesFunction: function()
      {
       return self.getTemplates();
      },
      changeCardEvent: function(data)
      {
        if (data == 'TEMPLATES')
        {
          self.clear();
        }
        else
        {
          self.simpleTherapyContainer.setMedicationData(data, true);
          self.cardContainer.setActiveItem(self.simpleTherapyContainer);
        }
      },
      saveToTemplateFunction: function(therapy, invalidTherapy)
      {
        self.saveTherapyToTemplateFunction(therapy, invalidTherapy);
      },
      linkTherapyFunction: function(callback)
      {
        self.linkTherapyFunction(callback);
      },
      confirmTherapyEvent: function(therapy)
      {
        return self.confirmTherapyEvent(therapy);
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePaneEvent();
      }
    });
  },

  _buildGui: function()
  {
    this.cardContainer.add(this.templatesContainer);
    this.cardContainer.add(this.simpleTherapyContainer);
    this.cardContainer.add(this.complexTherapyContainer);
    this.add(this.header, {region: 'north', height: 30});
    this.add(this.cardContainer, {region: 'center'});
  },

  _editTherapy: function(therapy, removeFromBasket)
  {
    if (removeFromBasket)
    {
      this.removeFromBasketFunction(therapy);
    }
    if (therapy.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX)
    {
      this.complexTherapyContainer.setComplexTherapy(therapy, true);
      this.cardContainer.setActiveItem(this.complexTherapyContainer);
    }
    else
    {
      this.simpleTherapyContainer.setSimpleTherapy(therapy, true);
      this.cardContainer.setActiveItem(this.simpleTherapyContainer);
    }
  },

  _handleMedicationSelected: function(medicationData, clear)
  {
    if (medicationData.doseForm &&
        medicationData.doseForm.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX)
    {
      this.cardContainer.setActiveItem(this.complexTherapyContainer);
      this.complexTherapyContainer.setMedicationData(medicationData, true);
    }
    else
    {
      this.simpleTherapyContainer.setMedicationData(medicationData, clear);
      this.cardContainer.setActiveItem(this.simpleTherapyContainer);
    }
  },

  /** public methods */
  clear: function()
  {
    this.cardContainer.setActiveItem(this.templatesContainer);
    this.templatesContainer.clear();
  },

  unfinishedOrderExists: function()
  {
    return this.cardContainer.getActiveItem() != this.templatesContainer;
  },

  editTherapy: function(therapy)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var unfinishedOrderExists = this.unfinishedOrderExists();

    if (unfinishedOrderExists)
    {
      var confirmDialog = appFactory.createConfirmSystemDialog(this.view.getDictionary('unfinished.therapy.that.will.not.be.saved.warning'),
          function(confirmed)
          {
            if (confirmed == true)
            {
              self._editTherapy(therapy, true);
            }
          }
      );
      confirmDialog.setWidth(380);
      confirmDialog.setHeight(122);
      confirmDialog.show();
    }
    else
    {
      this._editTherapy(therapy, true);
    }
  },

  presetMedication: function(medicationId)
  {
    var self = this;
    var medicationDataUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_MEDICATION_DATA;
    var params = {medicationId: medicationId};
    this.view.loadViewData(medicationDataUrl, params, null, function(data)
    {
      self._handleMedicationSelected(data, false);
    });
  },

  reloadTemplates: function()
  {
    this.templatesContainer.reloadTemplates();
  },

  getTemplates: function()
  {
    return this.templatesContainer.getTemplates();
  }
});
