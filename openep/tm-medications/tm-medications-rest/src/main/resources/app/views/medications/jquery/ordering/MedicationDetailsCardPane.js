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
  scrollable: 'both',
  /** configs */
  view: null,
  style: "min-width:150px;max-width:450px;max-height:500px; padding:10px;",
  /** privates: components */

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /** private methods */
  _createHtmlTemplate: function(medicationData)
  {
    var html = '';
    html += '<div class="medication-details-card">';
    html += '<table border=0 cellpadding=0 cellspacing="0" width=100%>';
    html += '<tr>';

    // left
    html += '<td valign=top>';
    html += '<div class="' + this._getMedicationIcon(medicationData.doseForm) + '"></div>';
    html += '</td>';
    //end left

    // right
    html += '<td>';
    html += '<div class="detail">';
    //medication
    if (medicationData.medication.genericName)
    {
      html += '<p class="TextHeading2">' + medicationData.medication.genericName + '</p>';
      html += '<p class="TextData">(' + medicationData.medication.name + ')</p>';
    }
    else
    {
      html += '<p class="TextHeading2">' + medicationData.medication.name + '</p>';
    }
    //dose form
    html += '<span class="TextLabel">' + this.view.getDictionary("dose.form") + ' </span>';
    html += '<span class="TextDataBold">' + medicationData.doseForm.name + ' </span>';
    html += '<br>';
    //medication ingredients
    if (medicationData.medicationIngredients.length > 0)
    {
      html += '<table border=0 cellpadding=0 cellspacing="0" width=100%>';
      html += '<tr>';
      html += '<td>';
      html += '<span class="TextLabel">' + this.view.getDictionary("strength") + ' </span>';
      html += '</td>';
      for (var i = 0; i < medicationData.medicationIngredients.length; i++)
      {
        var strengthString = tm.views.medications.MedicationUtils.getStrengthDisplayString(medicationData.medicationIngredients[i], true);
        if (i > 0)
        {
          html += '<tr>';
          html += '<td>';
          html += '</td>';
        }
        html += '<td class="strength-label">';
        html += '<span class="TextDataBold">' + strengthString + ' </span>';
        html += '</td>';
        html += '<td class="ingredient-label">';
        html += '<span class="TextDataBold">' + ' (' + medicationData.medicationIngredients[i].ingredient.name + ')' + ' </span>';
        html += '</td>';
        html += '</tr>';
      }
      if (medicationData.descriptiveIngredient)
      {
        html += '<tr>';
        html += '<td>';
        html += '<span class="TextLabel">' + this.view.getDictionary("together") + ' </span>';
        html += '</td>';
        html += '<td class="descriptive-ingredient-label">';
        html += '<span class="TextDataBold">' + tm.views.medications.MedicationUtils.getStrengthDisplayString(medicationData.descriptiveIngredient, true) + ' </span>';
        html += '</td>';
        html += '</tr>';
      }
      html += '<tr>';
      html += '<td colspan="2">';
      html += '<div id="' + this._createSmcpButtonsContainerId() + '">';
      html += '</div>';
      html += '</td>';
      html += '</tr>';

      html += '</table>';
    }
    // end right
    html += '</div>';
    html += '</td>';

    html += '</tr>';
    html += '</table>';
    html += '</div>';
    return html;
  },

  _getMedicationIcon: function(doseForm)
  {
    if (doseForm.doseFormType == 'TBL')
    {
      return "icon_pills";
    }
    return "icon_other_medication";
  },

  _getSmcpButtonsContainer: function(medicationData)
  {
    var self = this;
    var container = new tm.jquery.Container({
      layout: this.view.getAppFactory().createDefaultHFlexboxLayout("start", "start", 10)
    });

    for (var documentIndex = 0; documentIndex < medicationData.medicationDocuments.length; documentIndex++)
    {
      var medicationDocument = medicationData.medicationDocuments[documentIndex];
      if (medicationDocument.externalSystem && medicationDocument.documentReference)
      {
        var documentButton = self._createDocumentButton(medicationDocument);
        container.add(documentButton);
      }
    }

    return container;
  },

  _createDocumentButton: function(medicationDocument)
  {
    var self = this;
    var dictionaryName = this.view.getDictionary(medicationDocument.externalSystem);
    var hasDictionaryName = dictionaryName.split(" ")[0] && dictionaryName.split(" ")[0] != 'undefined';
    var name = hasDictionaryName ? dictionaryName : medicationDocument.externalSystem;
    var documentButton = new tm.jquery.Button({cls: 'button-align-left', text: name, type: 'link'});
    documentButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (self.view.isTablet()) //todo: isTablet in browser returns false - igor: add check if in browser
      {
        var viewSecurity = self.view.getViewData().getViewSecurity();
        var data = {};
        data.dau = tm.jquery.Utils.encode64(viewSecurity.username);
        data.dap = tm.jquery.Utils.encode64(viewSecurity.password);

        var documentUrl =
            self.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_MEDICATION_DOCUMENT
                + "?data=" + encodeURI(JSON.stringify(data)) + "&reference=" + medicationDocument.documentReference;
        window.open(documentUrl);
      }
      else
      {
        ViewManager.getInstance().sendAction(
            self.view,
            ViewAction.create("openMedicationDocument").addParam("reference", medicationDocument.documentReference));
      }
    });
    return documentButton;
  },

  _createSmcpButtonsContainerId: function()
  {
    return this.view.getViewId() + "_smcp_buttons_container";
  },

  /** public methods */
  setMedicationData: function(medicationData)
  {
    var self = this;
    if (medicationData)
    {

      this.setHtml(this._createHtmlTemplate(medicationData));
      setTimeout(function()
      {
        var smcpButtonsContainerId = self._createSmcpButtonsContainerId();

        var smcpButtonsContainerElement = $("#" + smcpButtonsContainerId).get(0);
        if (tm.jquery.Utils.isEmpty(smcpButtonsContainerElement) == false)
        {
          var container = self._getSmcpButtonsContainer(medicationData);
          container.setRenderToElement(smcpButtonsContainerElement);
          container.doRender();
        }
      }, 100);

    }
  }
});