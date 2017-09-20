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

Class.define('tm.views.medications.warning.WarningsHelpers', 'tm.jquery.Object', {

      /** statics */
      statics: {

        getMedicationsForWarningSearchForTherapy: function(therapy, prospective)  // [MedicationForWarningsSearchDto.java]
        {
          var enums = app.views.medications.TherapyEnums;
          var therapiesForWarnings = [];
          if (therapy.isOrderTypeSimple())
          {
            therapiesForWarnings.push(
                {
                  id: therapy.medication.id,
                  name: therapy.medication.name,
                  shortName: therapy.medication.shortName,
                  prospective: prospective,
                  routeCode: tm.jquery.Utils.isArray(therapy.getRoutes()) && !tm.jquery.Utils.isEmpty(therapy.getRoutes()[0])
                      ? therapy.getRoutes()[0].id
                      : null,
                  doseUnit: therapy.quantityUnit,
                  doseAmount: therapy.variable ? 0 : therapy.doseElement.quantity
                });
          }
          else if (therapy.isOrderTypeComplex())
          {
            for (var j = 0; j < therapy.ingredientsList.length; j++)
            {
              var infusionIngredient = therapy.ingredientsList[j];
              therapiesForWarnings.push(
                  {
                    id: infusionIngredient.medication.id,
                    name: infusionIngredient.medication.name,
                    shortName: infusionIngredient.medication.shortName,
                    prospective: prospective,
                    routeCode: tm.jquery.Utils.isArray(therapy.getRoutes()) && !tm.jquery.Utils.isEmpty(therapy.getRoutes()[0])
                        ? therapy.getRoutes()[0].id
                        : null,
                    doseUnit: infusionIngredient.quantityUnit,
                    doseAmount: infusionIngredient.quantity
                  });
            }
          }
          return therapiesForWarnings;
        },

        loadMedicationWarnings: function(view, patientMedsForWarnings, severityFilterValues, successCallback, options)
        {
          var self = this;
          var warningsUrl =
              view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_WARNINGS;

          var patientData = view.getPatientData();
          if (patientData)
          {
            var referenceWeight = view.getReferenceWeight();
            var heightInCm = patientData.heightInCm ? patientData.heightInCm : 0;

            var bsaInM2 = tm.views.medications.MedicationUtils.calculateBodySurfaceArea(heightInCm, referenceWeight);
            var patientAgeInDays = moment(CurrentTime.get()).diff(new Date(patientData.birthDate), 'days');

            var params = {
              patientId: view.getPatientId(),
              patientAgeInDays: patientAgeInDays,
              patientWeightInKg: referenceWeight ? referenceWeight : 0,
              patientAllergies: JSON.stringify(patientData.allergies),
              bsaInM2: bsaInM2 ? bsaInM2 : 0,
              isFemale: patientData.gender == "FEMALE",
              patientDiseases: JSON.stringify(patientData.diseases),
              patientMedications: JSON.stringify(patientMedsForWarnings),
              severityFilterValues: JSON.stringify(severityFilterValues)
            };

            if (!view.runningRequests) {
              view.runningRequests = new Array();
            }
            if (view.runningRequests[options.taskName]) {
              view.runningRequests[options.taskName].abort();
              view.runningRequests[options.taskName] = null;
            }
            view.runningRequests[options.taskName] = view.loadPostViewData(warningsUrl, params, null, function(data)
                {
                  successCallback(data);
                },
                null,
                null,
                {
                  abortHandler: function(req, result) {

                  }
                });
          }
        },

        getFormattedWarningDescription: function(view, warning)
        {
          var enums = app.views.medications.TherapyEnums;
          var formattedWarning = warning.description;
          if (!tm.jquery.Utils.isEmpty(formattedWarning) &&
              formattedWarning.length > 170 && !tm.jquery.Utils.isEmpty(warning.monographHtml) &&
              warning.severity != enums.warningSeverityEnum.HIGH)
          {
            formattedWarning = formattedWarning.substring(0, 170);
            formattedWarning += " ...";
          }
          for (var i = 0; i < warning.medications.length; i++)
          {
            var medicationForWarningDto = warning.medications[i];
            formattedWarning = formattedWarning.replaceAll(medicationForWarningDto.name, '<strong>' + medicationForWarningDto.name + '</strong>', null, true);
          }

          if (warning.type == enums.warningType.UNMATCHED)
          {
            formattedWarning = view.getDictionary('medications.unmatched.for.warnings') + ' ' + formattedWarning;
          }
          return formattedWarning;
        },

        createMonographContainer: function(view, warning)
        {
          if (warning.monographHtml)
          {
            return new tm.jquery.Container({
              width: 30,
              cls: 'icon-monograph',
              flex: tm.jquery.HFlexboxLayout.create("flex-start", "flex-end"),
              tooltip: view.getAppFactory().createDefaultPopoverTooltip(
                  view.getDictionary('monograph'),
                  null,
                  new tm.jquery.Container({
                    cls: 'monograph',
                    scrollable: "both",
                    html: warning.monographHtml
                  }),
                  800, 600
              )
            });
          }
          return null;
        },

        createWarningsLegendTooltip: function (view)
        {
          var enums = app.views.medications.TherapyEnums;
          var legendContainer = new tm.jquery.Container({
            cls: 'warnings-legend',
            layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start")
          });

          legendContainer.add(
              tm.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary("warning.severity"), '0 0 0 0'));
          var highSeverityContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5)});
          highSeverityContainer.add(new tm.jquery.Container({cls: "severity icon-high"}));
          highSeverityContainer.add(
              tm.views.medications.MedicationUtils.crateLabel('TextData', tm.views.medications.warning.WarningsHelpers.getWarningSeverityString(view, enums.warningSeverityEnum.HIGH)));
          var significantSeverityContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5)});
          significantSeverityContainer.add(new tm.jquery.Container({cls: "severity icon-significant"}));
          significantSeverityContainer.add(
              tm.views.medications.MedicationUtils.crateLabel('TextData', tm.views.medications.warning.WarningsHelpers.getWarningSeverityString(view, enums.warningSeverityEnum.SIGNIFICANT)));
          var lowSeverityContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5)});
          lowSeverityContainer.add(new tm.jquery.Container({cls: "severity icon-low"}));
          lowSeverityContainer.add(
              tm.views.medications.MedicationUtils.crateLabel('TextData', tm.views.medications.warning.WarningsHelpers.getWarningSeverityString(view, enums.warningSeverityEnum.LOW)));
          legendContainer.add(highSeverityContainer);
          legendContainer.add(significantSeverityContainer);
          legendContainer.add(lowSeverityContainer);

          legendContainer.add(
              tm.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary("warning.types")));
          var allergyContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5)});
          allergyContainer.add(tm.views.medications.warning.WarningsHelpers.createTypeAndSeverityIconContainer(view, enums.warningType.ALLERGY, null));
          allergyContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', view.getDictionary("warning.type." + enums.warningType.ALLERGY)));
          var contraindicationContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5)});
          contraindicationContainer.add(tm.views.medications.warning.WarningsHelpers.createTypeAndSeverityIconContainer(view, enums.warningType.CONTRAINDICATION, null));
          contraindicationContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', view.getDictionary("warning.type." + enums.warningType.CONTRAINDICATION)));
          var interactionContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5)});
          interactionContainer.add(tm.views.medications.warning.WarningsHelpers.createTypeAndSeverityIconContainer(view, enums.warningType.INTERACTION, null));
          interactionContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', view.getDictionary("warning.type." + enums.warningType.INTERACTION)));
          var duplicateContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5)});
          duplicateContainer.add(tm.views.medications.warning.WarningsHelpers.createTypeAndSeverityIconContainer(view, enums.warningType.DUPLICATE, null));
          duplicateContainer.add(tm.views.medications.MedicationUtils.crateLabel('TextData', view.getDictionary("warning.type." + enums.warningType.DUPLICATE)));
          legendContainer.add(allergyContainer);
          legendContainer.add(contraindicationContainer);
          legendContainer.add(interactionContainer);
          legendContainer.add(duplicateContainer);

          return view.getAppFactory().createDefaultPopoverTooltip(
              view.getDictionary('warnings'),
              null,
              legendContainer,
              200, 255
          );
        },

        createTypeAndSeverityIconContainer: function(view, warningType, warningSeverity)
        {
          var enums = app.views.medications.TherapyEnums;
          // warningType and warningSeverity set as single icon, if warning type not present we don't set the icon, 'unmatched' warning has no icon
          if (!tm.jquery.Utils.isEmpty(warningType) && warningType != enums.warningType.UNMATCHED)
          {
            var warningSeverityIconClass = tm.jquery.Utils.isEmpty(warningSeverity) ? 'icon-without' : 'icon-' + warningSeverity.toLowerCase();
            return new tm.jquery.Container({
              width: 30,
              cls: 'severity ' + warningSeverityIconClass + ' ' + warningType.toLowerCase(),
              tooltip: tm.views.medications.warning.WarningsHelpers.getTypeAndSeverityTooltip(view, warningType, warningSeverity)
            });
          }
          return null;
        },

        getTypeAndSeverityTooltip: function(view, warningType, warningSeverity)
        {
          var severityString = tm.views.medications.warning.WarningsHelpers.getWarningSeverityString(view, warningSeverity);
          var typeString = view.getDictionary("warning.type." + warningType);
          var tooltipString = tm.jquery.Utils.isEmpty(warningSeverity) ? typeString : typeString + " - " + severityString;

          return tm.views.medications.MedicationUtils.createTooltip(tooltipString, "right", this.view);
        },
        getWarningSeverityString: function(view, warningSeverity)
        {
          return warningSeverity ? view.getDictionary("warning.severity." + warningSeverity) : '';
        },

        getSeverityWarningsCounterDisplayValue: function(warningsDto)
        {
          var highSeverityWarningsCount = warningsDto != null ? warningsDto.highSeverityWarningsCount : 0;
          var significantSeverityWarningsCount = warningsDto != null ? warningsDto.significantSeverityWarningsCount : 0;
          var lowSeverityWarningsCount = warningsDto != null ? warningsDto.lowSeverityWarningsCount : 0;
          var noSeverityWarningsCount = warningsDto != null ? warningsDto.noSeverityWarningsCount : 0;

          return "<span class='severity-high'>" + highSeverityWarningsCount + "</span>" +
              "/<span class='severity-significant'>" + significantSeverityWarningsCount + "</span>" +
              "/<span class='severity-low'>" + lowSeverityWarningsCount + "</span>" +
              "/<span class='severity-without'>" + noSeverityWarningsCount + "</span>"
              ;
        },

        screenSelectedMedicationForWarnings: function(view, selectedTherapy, patientMedsForWarnings, callback)
        {
          var enums = app.views.medications.TherapyEnums;
          var therapiesForWarnings = [];
          var selectedTherapiesForWarning =
              tm.views.medications.warning.WarningsHelpers.getMedicationsForWarningSearchForTherapy(selectedTherapy, true);
          therapiesForWarnings.push.apply(therapiesForWarnings, selectedTherapiesForWarning);
          therapiesForWarnings.push.apply(therapiesForWarnings, patientMedsForWarnings);

          var severityFilterValues = [];      //show all warnings
          severityFilterValues.push(enums.warningSeverityEnum.HIGH);
          severityFilterValues.push(enums.warningSeverityEnum.SIGNIFICANT);
          severityFilterValues.push(enums.warningSeverityEnum.LOW);

          tm.views.medications.warning.WarningsHelpers.loadMedicationWarnings(
              view,
              therapiesForWarnings,
              severityFilterValues,
              function(warning)
              {
                callback(selectedTherapy, warning.warnings);
              },
              { taskName: 'SELECTED_MEDICATION' }
          );
        },

        getImageClsForBnfMaximumPercentage: function(percentage)
        {
          if (tm.jquery.Utils.isEmpty(percentage))
          {
            return null;
          }
          if (percentage < 50)
          {
            return 'bnf-low-icon';
          }
          else if (percentage < 100)
          {
            return 'bnf-significant-icon';

          }
          else
          {
            return 'bnf-high-icon';
          }
        }
      }
    }
);
