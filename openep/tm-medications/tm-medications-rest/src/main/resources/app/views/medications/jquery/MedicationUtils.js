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

Class.define('tm.views.medications.MedicationUtils', 'tm.jquery.Object', {

  /** statics */
  statics: {
    unitsMap: {
      "l": 1,
      "dl": 0.1,
      "cl": 0.01,
      "ml": 0.001,
      "µl": 0.000001,
      "g": 1,
      "mg": 0.001,
      "µg": 0.000001,
      "ng": 0.000000001,
      "day": 86400,
      "h": 3600,
      "min": 60,
      "s": 1,
      "i.e.": 1,
      "mmol": 0.001
    },

    getStrengthDisplayString: function(strength, withoutIngredient)
    {
      var strengthString = Globalize.format(strength.strengthNumerator) + strength.strengthNumeratorUnit;
      if (strength.strengthDenominator)
      {
        strengthString += '/' + Globalize.format(strength.strengthDenominator) + strength.strengthDenominatorUnit;
      }
      if (!withoutIngredient && strength.ingredient)
      {
        strengthString += ' (' + strength.ingredient.name +  ')';
      }
      return strengthString;
    },

    getMedicationDisplayString: function(medication)
    {
      if (medication)
      {
        if (medication.genericName)
        {
          return medication.genericName + ' (' + medication.name + ')';
        }
        return medication.name;
      }
      return null;
    },

    isTherapyTaggedForPrescription: function (therapyTags)
    {
      var enums = app.views.medications.TherapyEnums;

      for (var i = 0; i < therapyTags.length; i++)
      {
        var tag = therapyTags[i];
        if (tag == enums.therapyTag.PRESCRIPTION)
        {
          return true;
        }
      }
      return false;
    },

    getTherapyIcon: function(therapy)
    {
      if (therapy.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX)
      {
        if (therapy.baselineInfusion)
        {
          return "icon_baseline_infusion";
        }
        if (therapy.continuousInfusion)
        {
          return "icon_continuous_infusion";
        }
        if (therapy.speedDisplay)
        {
          return "icon_infusion";
        }
        return "icon_injection"
      }
      if (therapy.doseForm && therapy.doseForm.doseFormType == 'TBL')
      {
        return "icon_pills";
      }
      return "icon_other_medication";
    },

    //Mosteller formula
    calculateBodySurfaceArea: function (heightInCm, weightInKg)
    {
      if (heightInCm && weightInKg)
      {
        return Math.sqrt((heightInCm * weightInKg) / 3600.0);
      }
      return null;
    },
    createTooltip: function (text, placement, view)
    {
      if(tm.jquery.Utils.isEmpty(view))
      {
        return new tm.jquery.Tooltip({
          type: "tooltip",
          placement: tm.jquery.Utils.isEmpty(placement) ? "bottom" : placement,
          trigger: "hover",
          title: text,
          html: true,
          animation: false,
          delay: {
            show: 1000,
            hide: 1000
          }
        });
      }
      else if(view instanceof app.views.common.AppView)
      {
        return view.getAppFactory().createDefaultHintTooltip(
            text, tm.jquery.Utils.isEmpty(placement) ? "bottom" : placement, "hover");
      }
    },

    convertToUnit: function (value, fromUnit, toUnit)
    {
      var fromFactor = this.unitsMap[fromUnit];
      var toFactor = this.unitsMap[toUnit];
      if (tm.jquery.Utils.isEmpty(fromFactor) || tm.jquery.Utils.isEmpty(toFactor))
      {
        return null;
      }
      return value * fromFactor / toFactor;
    },

    pad: function (number, length)
    {
      var str = '' + number;
      while (str.length < length)
      {
        str = '0' + str;
      }
      return str;
    },

    doubleToString: function(value, format)
    {
      if (value == null)
      {
        return null;
      }
      var formatted = Globalize.format(value, format);
      var groupDelimiter = Globalize.culture().numberFormat[","]; // string that separates number groups, as in 1,000,000
      formatted = formatted.indexOf(groupDelimiter) < 0 ? formatted : formatted.split(groupDelimiter).join("");
      formatted = this.removeTrailingZeros(formatted);
      return formatted;
    },

    removeTrailingZeros: function(value)
    {
      var decimalDelimiter = Globalize.culture().numberFormat["."]; 	// string that separates a number from the fractional portion, as in 1.99
      return value.indexOf(decimalDelimiter) < 0 ? value : value.replaceAll("0*$", "").replaceAll("\\" + decimalDelimiter + "$", "");
    },

    filterMedicationsByTypes: function (therapyMedications, medicationTypes)
    {
      var medicationsOfType = [];
      for (var i = 0; i < therapyMedications.length; i++)
      {
        if (therapyMedications[i].medication)
        {
          var type = therapyMedications[i].medication.medicationType;
          if ($.inArray(type, medicationTypes) > -1)
          {
            medicationsOfType.push(therapyMedications[i])
          }
        }
      }
      return medicationsOfType;
    },

    getDefiningIngredient: function (medicationData)
    {
      if (medicationData)
      {
        if (medicationData.medicationIngredients.length == 1)
        {
          return medicationData.medicationIngredients[0];
        }
        else if (medicationData.descriptiveIngredient)
        {
          return medicationData.descriptiveIngredient;
        }
      }
      return null;
    },

    buildAdministeredDoseDisplayString: function(administration, numeratorOnly)
    {
      if (administration.administeredDose)
      {
        //var dose = administration.administeredDose;
        //return dose.numerator ? tm.views.medications.MedicationUtils.doubleToString(dose.numerator, 'n2') + ' ' + dose.numeratorUnit : '';
        var dose = administration.administeredDose;
        var numeratorString = dose.numerator ? this.doubleToString(dose.numerator, 'n2') + ' ' + dose.numeratorUnit : '';
        if (numeratorOnly)
        {
          return numeratorString;
        }
        var denominatorString = dose.denominator ? this.doubleToString(dose.denominator, 'n2') + ' ' + dose.denominatorUnit : '';
        return denominatorString ? (numeratorString + ' / ' + denominatorString) : numeratorString;
      }
      return null;
    },

    getStrengthNumeratorUnit: function(medicationData)
    {
      var definingIngredient = tm.views.medications.MedicationUtils.getDefiningIngredient(medicationData);
      if (definingIngredient)
      {
        return definingIngredient.strengthNumeratorUnit;
      }
      else
      {
        return medicationData.basicUnit;
      }
    },

    getStrengthDenominatorUnit: function(medicationData)
    {
      var definingIngredient = tm.views.medications.MedicationUtils.getDefiningIngredient(medicationData);
      if (definingIngredient)
      {
        return definingIngredient.strengthDenominatorUnit;
      }
      return null;
    },

    crateLabel: function(cls, text, padding, block)
    {
      return new tm.jquery.Label({cls: cls, text: text, style: block !== false ? 'display:block;' : '', padding: padding || padding == 0 ? padding : "5 0 0 0"});
    },

    createMedicationsSearchField: function(view, width, mode, selection, enabled)
    {
      return new tm.jquery.TypeaheadField({
        placeholder: view.getDictionary("search.medication") + "...",
        displayProvider: tm.views.medications.MedicationUtils.getMedicationDisplayString,
        minLength: 3,
        mode: mode,
        width: width,
        selection: selection,
        editable: enabled,
        items: 10000,
        matcher: function(item)
        {
          var cleanItem = item.replace('<strong>', '');
          cleanItem = cleanItem.replace('</strong>', '');
          var itemStrings = cleanItem.split(' ');
          var queryStrings = this.query.split(' ');
          for (var i = 0; i < queryStrings.length; i++)
          {
            var isAMatch = false;
            for (var j = 0; j < itemStrings.length; j++)
            {
              if (itemStrings[j].indexOf('%') == itemStrings[j].length - 1)
              {
                if (itemStrings[j].toLowerCase() == queryStrings[i].toLowerCase())
                {
                  isAMatch = true;
                  break;
                }
              }
              else if (itemStrings[j].toLowerCase().indexOf(queryStrings[i].toLowerCase()) != -1)
              {
                isAMatch = true;
                break;
              }
            }
            if (!isAMatch)
            {
              return false
            }
          }
          return true;
        },
        highlighter: function(item, selectedItemData, query)
        {
          var queryStrings = this.query.split(' ');
          var highlightedItem = item;
          var replaceString = '';
          for (var i = 0; i < queryStrings.length; i++)
          {
            if (queryStrings[i])
            {
              replaceString += queryStrings[i].replace(/[\-\[\]{}()*+?.,\\\^$|#\s]/g, '\\$&');
              if (i < queryStrings.length - 1)
              {
                replaceString += '|';
              }
            }
          }

          if (query != "")
          {
            highlightedItem = item.replace(new RegExp('(' + query + ')', 'ig'),
                function($1, match)
                {
                  return '<strong>' + match + '</strong>'
                });
          }
          else
          {
            highlightedItem = item;
          }

          if (selectedItemData.active === false)
          {
            return "<span class='inactive'>" + highlightedItem + "</span>";
          }
          return highlightedItem;
        }
      });
    },

    createMenuButton: function(contextMenu)
    {
      var menuButton = new tm.jquery.Container({
        width: 16, height: 16,
        cls: 'menu-icon'
      });
      menuButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
      {
        contextMenu.show(elementEvent);
      });
      return menuButton;
    },

    createPerformerContainer: function(view, careProfessionals, presetCareProfessionalName)
    {
      var presetCareProfessional = this._getCareProfessionalAsPotentialPerformer(careProfessionals, presetCareProfessionalName);
      return new app.views.common.PerformerContainer({
        view: view,
        padding: "0 5 0 5",
        height: 35,
        dateEditable: false,
        withDetails: false,
        performDate: new Date(),
        compositionEditor: presetCareProfessional,
        compositionDate: new Date(),
        careProfessionals: careProfessionals,
        performer: presetCareProfessional,
        performerTitle: view.getDictionary("requested.by")
      });
    },

    createNumberField: function(formatting, width, margin)
    {
      return new tm.jquery.NumberField({
        formatting: formatting,
        width: width,
        margin: margin,
        //@Override
        getDisplayValue: function(value)
        {
          var displayValue = value == null ? "" :
              Globalize.format(value, formatting).replaceAll("\\" + Globalize.culture().numberFormat[","] + "", "");
          return tm.views.medications.MedicationUtils.removeTrailingZeros(displayValue);
        }
      });
    },

    _getCareProfessionalAsPotentialPerformer: function(careProfessionals, careProfessionalName)
    {
      if (careProfessionalName)
      {
        for (var index = 0; index < careProfessionals.length; index++)
        {
          if (careProfessionals[index].name === careProfessionalName)
          {
            return careProfessionals[index];
          }
        }
      }
      return null;
    },

    getScrollbarWidth: function()
    {
      var outer = document.createElement("div");
      outer.style.visibility = "hidden";
      outer.style.width = "100px";
      outer.style.msOverflowStyle = "scrollbar";

      document.body.appendChild(outer);

      var widthNoScroll = outer.offsetWidth;
      outer.style.overflow = "scroll";

      var inner = document.createElement("div");
      inner.style.width = "100%";
      outer.appendChild(inner);

      var widthWithScroll = inner.offsetWidth;
      outer.parentNode.removeChild(outer);

      return widthNoScroll - widthWithScroll;
    },

    isScrollVisible: function(component)
    {
      if (component.getDom())
      {
        var scrollHeight = $(component.getDom())[0].scrollHeight;
        var height = $(component.getDom()).height();
        return scrollHeight > height + 1;
      }
      return false;
    },

    getTherapyCustomGroupDisplayName: function(data, view)
    {
      var sortOrder = data.customGroupSortOrder ? data.customGroupSortOrder : 999999;
      sortOrder = this.padDigits(sortOrder, 6);
      var customGroupName = data.customGroup ? data.customGroup : view.getDictionary("other.undef");
      return "<span style='display:none;'>" + sortOrder + "</span>" + customGroupName;
    },

    padDigits: function(number, digits)
    {
      return Array(Math.max(digits - String(number).length + 1, 0)).join(0) + number;
    },

    createTherapyDetailsCardTooltip: function(dayTherapy, scrollableElement, view, similarTherapiesInterval)       // [TherapyDayDto.java]
    {
      var self = this;
      var appFactory = view.getAppFactory();

      var ehrOrderName = dayTherapy.therapy.ehrOrderName;
      var ehrCompositionId = dayTherapy.therapy.compositionUid;
      var consecutiveDay = dayTherapy.consecutiveDay;
      var therapyStatus = dayTherapy.therapyStatus;

      var detailsCardContainer = new app.views.medications.TherapyDetailsCardContainer({
        view: view,
        similarTherapiesInterval: similarTherapiesInterval
      });

      var tooltip = appFactory.createDefaultPopoverTooltip(
          view.getDictionary("medication"),
          null,
          detailsCardContainer
      );

      detailsCardContainer.updateData({
        ehrCompositionId: ehrCompositionId,
        ehrOrderName: ehrOrderName,
        therapyState: {
          consecutiveDay: consecutiveDay,
          therapyStatus: therapyStatus
        }});

      tooltip.setPlacement("auto");
      tooltip.setDefaultAutoPlacements(["rightBottom", "rightTop", "right", "left", "center"]);

      if(scrollableElement)                                          //todo for timeline
      {
        tooltip.setAppendTo(scrollableElement);
      }

      return tooltip;
    },

    getTherapyMedications: function(therapy)
    {
      var medications = [];
      if (therapy.medicationOrderFormType == app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX)
      {
        for (var i = 0; i < therapy.ingredientsList.length; i++)
        {
          var ingredient = therapy.ingredientsList[i];
          medications.push(ingredient.medication);
        }
      }
      else
      {
        medications.push(therapy.medication);
      }
      return medications;
    },

    assertTemplatesContainMedication: function(medicationId, templates)
    {
      var medicationIds = [];
      for (var i = 0; i < templates.length; i++)
      {
        var template = templates[i];
        for (var j = 0; j < template.templateElements.length; j++)
        {
          var therapy = template.templateElements[j].therapy;
          var therapyMedications = tm.views.medications.MedicationUtils.getTherapyMedications(therapy);
          for (var k = 0; k < therapyMedications.length; k++)
          {
            medicationIds.push(therapyMedications[k].id);
          }
        }
      }
      var isTemplatesContainMedication = false;
      for (var l = 0; l < medicationIds.length; l++)
      {
        if (medicationIds[l] == medicationId)
        {
          isTemplatesContainMedication = true;
        }
      }
      return isTemplatesContainMedication;
    }
  }}
);
