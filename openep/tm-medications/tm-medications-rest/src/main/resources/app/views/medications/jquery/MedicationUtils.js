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
          "litre": 1,
          "dl": 0.1,
          "cl": 0.01,
          "ml": 0.001,
          "mL": 0.001,
          "µl": 0.000001,
          "g": 1,
          "gram": 1,
          "mg": 0.001,
          "µg": 0.000001,
          "microgram": 0.000001,
          "ng": 0.000000001,
          "nanogram": 0.000000001,
          "day": 86400,
          "h": 3600,
          "min": 60,
          "s": 1,
          "i.e.": 1,
          "mmol": 0.001
        },

        getStrengthDisplayString: function(strength, withoutIngredient)
        {
          var strengthString = "";
          if (strength.strengthNumerator)
          {
            strengthString += Globalize.formatNumber(strength.strengthNumerator) + strength.strengthNumeratorUnit;
          }
          if (strength.strengthDenominator)
          {
            strengthString += '/' + Globalize.formatNumber(strength.strengthDenominator) + strength.strengthDenominatorUnit;
          }
          if (!withoutIngredient && strength.ingredientName)
          {
            strengthString += ' (' + strength.ingredientName + ')';
          }
          return strengthString;
        },

        //Mosteller formula
        calculateBodySurfaceArea: function(heightInCm, weightInKg)
        {
          if (heightInCm && weightInKg)
          {
            return Math.sqrt((heightInCm * weightInKg) / 3600.0);
          }
          return null;
        },
        createTooltip: function(text, placement, view)
        {
          if (tm.jquery.Utils.isEmpty(view))
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
          else if (view instanceof app.views.common.AppView)
          {
            return view.getAppFactory().createDefaultHintTooltip(
                text, tm.jquery.Utils.isEmpty(placement) ? "bottom" : placement, "hover");
          }
        },
        createHintTooltip: function(view, message, placement)
        {
          return view.getAppFactory().createDefaultHintTooltip(message, placement);
        },

        convertToUnit: function(value, fromUnit, toUnit)
        {
          if (fromUnit && fromUnit == toUnit)
          {
            return value;
          }
          var fromFactor = this.unitsMap[fromUnit];
          var toFactor = this.unitsMap[toUnit];
          if (tm.jquery.Utils.isEmpty(fromFactor) || tm.jquery.Utils.isEmpty(toFactor))
          {
            return null;
          }
          return value * fromFactor / toFactor;
        },

        pad: function(number, length)
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
          var maximumFractionDigits = 3;
          // var delimiter = Globalize.cldr.main("numbers/symbols-numberSystem-latn/decimal");
          if (value < 1)
          {
            var decimals = null;
            var valueString = value.toString();
            var delimitedNumber = valueString.split(".");
            if (delimitedNumber.length > 1)
            {
              decimals = delimitedNumber[1]
            }
            if (decimals)
            {
              var regexp = new RegExp('[1-9]');
              var indexOfFirstNonZero = regexp.exec(decimals).index;
              maximumFractionDigits = indexOfFirstNonZero + 3;
            }
          }
          format = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: maximumFractionDigits};
          

          if (value == null)
          {
            return null;
          }

          return Globalize.formatNumber(value, format);
        },

        filterMedicationsByTypes: function(therapyMedications, medicationTypes)
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

        buildAdministeredDoseDisplayString: function(administration, numeratorOnly)
        {
          if (administration.administeredDose)
          {
            //var dose = administration.administeredDose;
            //return dose.numerator ? tm.views.medications.MedicationUtils.doubleToString(dose.numerator, 'n2') + ' ' + dose.numeratorUnit : '';
            var dose = administration.administeredDose;
            var numeratorString = !tm.jquery.Utils.isEmpty(dose.numerator) ? this.getFormattedDecimalNumber(this.doubleToString(dose.numerator, 'n2')) + ' ' +
            this.getFormattedUnit(dose.numeratorUnit) : '';
            if (numeratorOnly)
            {
              return numeratorString;
            }
            var denominatorString = dose.denominator ? this.getFormattedDecimalNumber(this.doubleToString(dose.denominator, 'n2')) + ' ' +
            this.getFormattedUnit(dose.denominatorUnit) : '';
            return denominatorString ? (numeratorString + ' / ' + denominatorString) : numeratorString;
          }
          return null;
        },

        crateLabel: function(cls, text, padding, block)
        {
          return new tm.jquery.Label({
            cls: cls,
            text: text,
            style: block !== false ? 'display:block;' : '',
            padding: padding || padding == 0 ? padding : "5 0 0 0"
          });
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {app.views.medications.common.dto.Medication} selection
         * @param {boolean} enabled
         * @param {string|null} [cls=null]
         * @returns {tm.jquery.TypeaheadField}
         */
        createMedicationTypeaheadField: function(view, selection, enabled, cls)
        {
          return new tm.jquery.TypeaheadField({
            cls: cls,
            flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
            scrollable: 'visible',
            placeholder: view.getDictionary("search.medication") + "...",
            displayProvider: function(medication) {
              return medication ? medication.getDisplayName() : null;
            },
            minLength: 3,
            mode: 'advanced',
            selection: selection,
            enabled: enabled,
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

        createPerformerContainer: function(view, careProfessionals, presetCareProfessionalName)
        {
          var presetCareProfessional = this._getCareProfessionalAsPotentialPerformer(careProfessionals, presetCareProfessionalName);
          return new app.views.common.PerformerContainer({
            view: view,
            height: 35,
            dateEditable: false,
            withDetails: false,
            performDate: CurrentTime.get(),
            compositionEditor: presetCareProfessional,
            compositionDate: CurrentTime.get(),
            careProfessionals: careProfessionals,
            performer: presetCareProfessional,
            performerTitle: view.getDictionary("requested.by")
          });
        },

        createNumberField: function(formatting, width, cls)
        {
          if (formatting == "n0")
          {
            formatting = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 0};
          }
          else if (formatting == "n2")
          {
            formatting = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2};
          }
          else if (formatting == "n3")
          {
            formatting = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 3};
          }

          return new tm.jquery.NumberField({
            cls: cls,
            formatting: formatting,
            width: width,
            // @Override
            getDisplayValue: function(value)
            {
              return tm.views.medications.MedicationUtils.safeFormatNumber(value, this.formatting);
            }
          });
        },

        /**
         * Formats the number via Globalize and the given format, unless the value is less than 1. In that case
         * it's rounded to the first non zero fraction and additional 3 digits, to make sure the precision isn't lost.
         * @param {Number|null} value
         * @param {String} formatting
         * @returns {String}
         */
        safeFormatNumber: function(value, formatting)
        {
          if (tm.jquery.Utils.isEmpty(value))
          {
            return "";
          }
          if (value < 1)
          {
            var splitNumber = value.toString().split(".");
            if (splitNumber.length == 2)
            {
              var decimals = splitNumber[1];
              var regexp = new RegExp('[1-9]');
              var indexOfFirstNonZero = regexp.exec(decimals).index;
              var maximumFractionDigits = indexOfFirstNonZero + 3;
              var format = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: maximumFractionDigits};
              return Globalize.formatNumber(value, format);
            }
          }
          return Globalize.formatNumber(value, formatting)
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
          if (component && component.getDom())
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

        /**
         * Zero pads the given number to the specified amount of digits.
         * Function taken from http://stackoverflow.com/questions/10073699/pad-a-number-with-leading-zeros-in-javascript
         * @param {String|Number} number
         * @param {Number} digits
         * @returns {String}
         */
        padDigits: function(number, digits)
        {
          return new Array(Math.max(digits - String(number).length + 1, 0)).join(0) + number;
        },

        getIndexOfHourMinute: function(hourMinute, list)
        {
          for (var i = 0; i < list.length; i++)
          {
            if (list[i].hour == hourMinute.hour && list[i].minute == hourMinute.minute)
            {
              return i;
            }
          }
          return -1
        },

        isTherapyWithVariableDaysDose: function(timedDoseElements)
        {
          return timedDoseElements && timedDoseElements.length > 0 && timedDoseElements[0].date
        },

        getUidWithoutVersion: function(uid)
        {
          return uid.substring(0, uid.indexOf("::"));
        },
        getNextLinkName: function(linkName) //A2 -> A3
        {
          var prefix = linkName.substring(0, 1);
          var number = linkName.substring(1, linkName.length);
          var nextNumber = Number(number) + 1;
          return prefix + nextNumber;
        },
        /**
         * @param {String} therapyLinkName
         * @param {Array<app.views.medications.common.dto.Therapy|OxygenTherapy>} therapies
         * @returns {boolean}
         */
        areOtherTherapiesLinkedToTherapy: function(therapyLinkName, therapies)
        {
          if (!therapyLinkName)
          {
            return false;
          }
          var nextLinkName = this.getNextLinkName(therapyLinkName);
          for (var i = 0; i < therapies.length; i++)
          {
            if (therapies[i].getLinkName() == nextLinkName)
            {
              return true;
            }
          }
          return false;
        },
        createWizardDialog: function(view, title, content, resultCallback, width, height, buttonText)
        {
          var appFactory = view.getAppFactory();

          if (tm.jquery.Utils.isEmpty(buttonText))
          {
            buttonText = {
              confirmText: null,
              cancelText: null,
              nextText: null,
              backText: null
            }
          }

          var buttonContainer = new app.views.medications.reconciliation.MedicineReconciliationDialogButtons({
            confirmText: tm.jquery.Utils.isEmpty(buttonText.confirmText) ? view.getDictionary("confirm") : buttonText.confirmText,
            cancelText: tm.jquery.Utils.isEmpty(buttonText.cancelText) ? view.getDictionary("cancel") : buttonText.cancelText,
            nextText: tm.jquery.Utils.isEmpty(buttonText.nextText) ? view.getDictionary("dialog.next") : buttonText.nextText,
            backText: tm.jquery.Utils.isEmpty(buttonText.backText) ? view.getDictionary("dialog.back") : buttonText.backText
          });

          var contentWithFooter = appFactory.createContentAndFooterButtonsContainer(content, buttonContainer);

          var dialog = appFactory.createDefaultDialog(title, null, contentWithFooter, null, width, height);

          // confirm button //
          var confirmButton = buttonContainer.getConfirmButton();
          content._confirmButton = confirmButton;
          var confirmButtonHandler = function()
          {
            if (tm.jquery.Utils.isFunction(content.processResultData))
            {
              confirmButton.setEnabled(false);
              content.processResultData(function(resultData)
              {
                if (resultData instanceof app.views.common.AppResultData)
                {
                  if (resultData.isSuccess())
                  {
                    resultCallback(resultData);
                    dialog.hide();
                  }
                  else
                  {
                    setTimeout(function()
                    {
                      confirmButton.setEnabled(true);
                    }, 500);
                  }
                }
              });
            }
            else
            {
              //self.view.localLogger.warn(
              //    "The content container must implement abstract method 'processResultData' in app.views.common.containers.AppContentContainer");
            }
          };
          confirmButton.setHandler(confirmButtonHandler);

          // cancel button //
          var cancelButton = buttonContainer.getCancelButton();
          var cancelButtonHandler = function()
          {
            dialog.hide();
            //resultCallback(null);
          };
          cancelButton.setHandler(cancelButtonHandler);

          // next button //
          var nextButton = buttonContainer.getNextButton();
          // back button //
          var backButton = buttonContainer.getBackButton();
          // handlers

          nextButton.setHandler(function()
          {
            if (!tm.jquery.Utils.isEmpty(content.onButtonNextPressed))  content.onButtonNextPressed(buttonContainer);
          });

          backButton.setHandler(function()
          {
            if (!tm.jquery.Utils.isEmpty(content.onButtonBackPressed))  content.onButtonBackPressed(buttonContainer);
          });

          dialog.confirm = confirmButtonHandler;
          dialog.cancel = cancelButtonHandler;

          return dialog;
        },

        getPatientsReferenceWeightAndHeightHtml: function(view)
        {
          var html = "";
          var referenceWeight = view.getReferenceWeight();
          var height = view.getPatientHeightInCm();

          if (referenceWeight)
          {
            var referenceWeightValueString = tm.views.medications.MedicationUtils.doubleToString(referenceWeight, 'n3') + ' kg';
            var bodySurfaceAreaString;

            if (height)
            {
              var bodySurfaceArea = tm.views.medications.MedicationUtils.calculateBodySurfaceArea(height, referenceWeight);
              bodySurfaceAreaString = tm.views.medications.MedicationUtils.doubleToString(bodySurfaceArea, 'n3') + ' m2';
            }

            html = "<span style='text-transform: none; color: #63818D'>" + view.getDictionary('reference.weight') + ": " + referenceWeightValueString + "</span>";

            if (bodySurfaceAreaString)
            {
              html += " - <span style='text-transform: none; color: #63818D'>" + view.getDictionary('body.surface') + ": " + bodySurfaceAreaString + "</span>";
            }
          }

          return html;
        },

        getFirstOrNullTherapyChangeReason: function(map, actionEnum)
        {
          if (map.hasOwnProperty(actionEnum) && map[actionEnum].length > 0)
          {
            return new app.views.medications.common.dto.TherapyChangeReason({
              changeReason: map[actionEnum][0]
            });
          }
          return null;
        },

        calculateVariablePerDay: function(timedDoseElements)
        {
          if (timedDoseElements.isEmpty())
          {
            return null;
          }

          var dayDose = 0;
          timedDoseElements.forEach(function(item)
          {
            dayDose += item.doseElement.quantity;
          });
          return dayDose;
        },

        getFormattedDecimalNumber: function(value)
        {
          if (tm.jquery.Utils.isEmpty(value))
          {
            return value;
          }
          var delimiter = Globalize.cldr.main("numbers/symbols-numberSystem-latn/decimal");
          var restyledNumber = value.toString(); // numbers don't have regexp methods
          var splitRegex = new RegExp('[0-9]+(\\' + delimiter + '[0-9]+)', 'g');
          var floatNumbers = restyledNumber.match(splitRegex);
          if (!tm.jquery.Utils.isEmpty(floatNumbers))
          {
            for (var i = 0; i < floatNumbers.length; i++)
            {
              var splitNumber = floatNumbers[i].split(delimiter);
              if (splitNumber.length > 1)
              {
                restyledNumber = restyledNumber.replace(floatNumbers[i], splitNumber[0] + delimiter + '<span class="TextDataSmallerDecimal">' + splitNumber[1] + '</span>');
              }
            }
          }
          return restyledNumber;
        },

        createBnfPercentageInfoHtml: function(view, bnfPercentage, isForPatientDataContainer)
        {
          isForPatientDataContainer = tm.jquery.Utils.isEmpty(isForPatientDataContainer) ? false : isForPatientDataContainer;
          var bnfContainer = new tm.jquery.Container({
            layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
          });

          var bnfWarningImage = new tm.jquery.Image({
            cls: tm.views.medications.warning.WarningsHelpers.getImageClsForBnfMaximumPercentage(bnfPercentage),
            margin: "0 5 0 0",
            width: 16,
            height: 16,
            flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px")
          });

          var html = isForPatientDataContainer
              ? "<span class= TextLabel>" + view.getDictionary("cumulative.bnf.max") + ": " + "</span>" +
          "<span class = TextData>" + bnfPercentage + "</span>" + "<span class = TextUnit>" + "%" + "</span>"
              : '<span class="From TextData">' + bnfPercentage + '% of antipsychotic ' + '<strong>' +
          view.getDictionary("BNF.maximum") + '</strong></span>';

          if (!isForPatientDataContainer || isForPatientDataContainer && bnfPercentage >= 100)
          {
            bnfContainer.add(bnfWarningImage);
          }

          bnfContainer.add(new tm.jquery.Label({
            html: html,
            flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
          }));

          bnfContainer.doRender();
          return bnfContainer.getRenderToElement().innerHTML;
        },

        getFormattedUnit: function(unit)
        {
          return unit ? unit.replaceAll("ml", "mL").replaceAll('l/min', 'L/min') : unit;
        },

        unitForDosageCalculation: function(unit)
        {
          var acceptableUnits = ["g", "mg", "microgram", "µg", "nanogram", "i.e.", "mmol", "unit"];
          return acceptableUnits.indexOf(unit) > -1;
        },

        getDosageCalculationUnitOptions: function(view, doseUnit, patientHeight)
        {
          var options = [];
          doseUnit = tm.jquery.Utils.isEmpty(doseUnit) ? "" : doseUnit;
          options.push(
              {id: 0, displayUnit: doseUnit + '/kg/' + view.getDictionary("dose.short"), doseUnit: doseUnit, patientUnit: 'kg'});
          if (!tm.jquery.Utils.isEmpty(patientHeight))
          {
            options.push(
                {id: 1, displayUnit: doseUnit + '/m2/' + view.getDictionary("dose.short"), doseUnit: doseUnit, patientUnit: 'm2'});
          }
          return options;
        },

        isUnitVolumeUnit: function(unit)
        {
          var volumeUnits = ["l", "litre", "dl", "cl", "ml", "mL", "µl"];
          return volumeUnits.indexOf(unit) > -1;
        },


        /**
         * Use for universal therapies only!
         * @param {app.views.medications.common.dto.Therapy} therapy
         * @returns {app.views.medications.common.dto.MedicationData}
         */
        getMedicationDataFromSimpleTherapy: function(therapy)
        {
          var medication = {
            id: null,
            name: therapy.medication.name,
            medicationType: app.views.medications.TherapyEnums.medicationTypeEnum.MEDICATION
          };
          var medicationIngredients = [
            {
              strengthNumeratorUnit: therapy.quantityUnit,
              strengthDenominatorUnit: therapy.quantityDenominatorUnit
            }
          ];
          return app.views.medications.common.dto.MedicationData.fromJson({
            doseForm: therapy.doseForm,
            medication: medication,
            medicationIngredients: medicationIngredients
          });
        },

        /**
         * Use for universal therapies only!
         * @param {app.views.medications.common.dto.Therapy} therapy
         * @param {Number} infusionIndex
         * @returns {app.views.medications.common.dto.MedicationData}
         */
        getMedicationDataFromComplexTherapy: function(therapy, infusionIndex)
        {
          var index = !tm.jquery.Utils.isEmpty(infusionIndex) ? infusionIndex : 0;
          var medication = {
            id: null,
            name: therapy.ingredientsList[index].medication.name,
            medicationType: therapy.ingredientsList[index].medication.medicationType
          };
          var medicationIngredients = [
            {
              strengthNumeratorUnit: therapy.ingredientsList[index].quantityUnit,
              strengthDenominatorUnit: "ml"
            }
          ];
          return app.views.medications.common.dto.MedicationData.fromJson({
            doseForm: therapy.ingredientsList[index].doseForm,
            medication: medication,
            medicationIngredients: medicationIngredients
          });
        },

        /**
         * Attaches an extra save order button to the dialog, changes the confirm button text and configures the button handlers
         * to correctly call the extended version of the dialog content's processResultData as required.
         * {@see app.views.medications.outpatient.OutpatientOrderingContainer}
         *
         * @param {app.views.common.AppView} view A view, for support method acccess.
         * @param {tm.jquery.Dialog} dialog The dialog
         * @param {Function} dialogResultCallback The dialog result function to be called by confirm buttons.
         */
        attachOutpatientOrderingDialogFooterButtons: function(view, dialog, dialogResultCallback)
        {
          var dialogFooter = dialog.getBody().getFooter();
          var confirmButton = dialogFooter.getConfirmButton();

          var saveButton = new tm.jquery.Button({
            text: view.getDictionary("save"),
            handler: createConfirmButtonHandler(true)
          });

          confirmButton.setText(view.getDictionary("do.order"));
          confirmButton.setHandler(createConfirmButtonHandler(false));
          dialogFooter.rightButtons.unshift(saveButton);

          /** support functions **/

          function createConfirmButtonHandler(saveOnly)
          {
            return function()
            {
              if (tm.jquery.Utils.isFunction(dialog.getContent().processResultData))
              {
                enableAllRightButtons(false);

                dialog.getContent().processResultData(function(resultData)
                    {
                      if (resultData instanceof app.views.common.AppResultData)
                      {
                        if (resultData.isSuccess())
                        {
                          dialogResultCallback(resultData);
                          dialog.hide();
                        }
                        else
                        {
                          setTimeout(function()
                          {
                            enableAllRightButtons(true);
                          }, 500);
                        }
                      }
                    },
                    saveOnly);
              }
            };
          }

          function enableAllRightButtons(enable)
          {
            dialogFooter.rightButtons.forEach(function(button)
            {
              button.setEnabled(enable);
            });
          }
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {String} message
         * @param {number |null} height
         * @param {number | null} width
         * @returns {tm.jquery.Deferred}
         */
        openConfirmationWithWarningDialog: function(view, message, width, height)
        {
          var appFactory = view.getAppFactory();
          var deferred = new tm.jquery.Deferred;
          var warningDialog = appFactory.createConfirmSystemDialog(
              message,
              resultCallback,
              width ? width : 300,
              height ? height : 160);

          warningDialog.show();

          function resultCallback(confirm)
          {
            deferred.resolve(confirm);
          }

          return deferred.promise();
        },

        getSmcpButtonsContainer: function(medicationData, view)
        {
          var self = this;
          var container = new tm.jquery.Container({
            layout: tm.jquery.HFlexboxLayout.create("flex-start", "start", 0),
            flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
          });

          for (var documentIndex = 0; documentIndex < medicationData.medicationDocuments.length; documentIndex++)
          {
            var medicationDocument = medicationData.medicationDocuments[documentIndex];
            if (medicationDocument.externalSystem && medicationDocument.documentReference)
            {
              var documentButton = self.createDocumentButton(medicationDocument, view);
              container.add(documentButton);
            }
          }
          return container;
        },

        createDocumentButton: function(medicationDocument, view)
        {
          var dictionaryName = view.getDictionary(medicationDocument.externalSystem);
          var hasDictionaryName = dictionaryName.split(" ")[0] && dictionaryName.split(" ")[0] != 'undefined';
          var name = hasDictionaryName ? dictionaryName : medicationDocument.externalSystem;
          var documentButton = new tm.jquery.Button({cls: 'button-align-left smpc-cls', text: name, type: 'link'});
          documentButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
          {
            var documentReference = medicationDocument.documentReference;
            //var documentReference = "9b633e02-1810-4775-a67f-c0f8d5f737bb";
            if (view.isSwingApplication())
            {
              view.sendAction("openMedicationDocument", {reference: documentReference});
            }
            else
            {
              var documentURI = tm.jquery.URI.create(
                  view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_MEDICATION_DOCUMENT);
              documentURI.addParam("reference", documentReference);
              documentURI.addParam("ticket", view.getCasTicket());
              window.open(documentURI.toUrl());
            }
          });
          return documentButton;
        },

        /**
         * Based on the ES6 polyfill.
         * {@link https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Array/find}
         * @param {Array<Object>} inArray
         * @param {Function} predicate
         * @returns {*}
         */
        findInArray: function(inArray, predicate)
        {
          if (!inArray || !predicate)
          {
            return undefined;
          }

          if (typeof predicate !== 'function')
          {
            throw new TypeError('predicate must be a function');
          }

          // If thisArg was supplied, let T be thisArg; else let T be undefined.
          var thisArg = arguments[2];
          var k = 0;

          while (k < inArray.length)
          {
            var kValue = inArray[k];
            if (predicate.call(thisArg, kValue, k, inArray))
            {
              return kValue;
            }
            k++;
          }

          return undefined;
        },

        /**
         * TODO Mitja TMC-8095 load values frome ehr
         * @param {app.views.common.AppView} view
         * @return {Array<tm.jquery.selectbox.Option>}
         */
        createTherapySourceSelectBoxOptions: function(view) {
          return [
            tm.jquery.SelectBox.createOption(
                view.getDictionary("therapy.source.patient"),
                view.getDictionary("therapy.source.patient")),
            tm.jquery.SelectBox.createOption(
                view.getDictionary("therapy.source.relative.carer"),
                view.getDictionary("therapy.source.relative.carer")),
            tm.jquery.SelectBox.createOption(
                view.getDictionary("therapy.source.patients.own.medicines"),
                view.getDictionary("therapy.source.patients.own.medicines")),
            tm.jquery.SelectBox.createOption(
                view.getDictionary("therapy.source.over.the.counter.medicines"),
                view.getDictionary("therapy.source.over.the.counter.medicines")),
            tm.jquery.SelectBox.createOption(
                view.getDictionary("therapy.source.referral.letter"),
                view.getDictionary("therapy.source.referral.letter")),
            tm.jquery.SelectBox.createOption(
                view.getDictionary("therapy.source.gp.record.paper.copy.or.repeat.prescription.form"),
                view.getDictionary("therapy.source.gp.record.paper.copy.or.repeat.prescription.form")),
            tm.jquery.SelectBox.createOption(
                view.getDictionary("therapy.source.gp.record.electronic"),
                view.getDictionary("therapy.source.gp.record.electronic"))
          ];
        }
      }
    }
);
