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

Class.define('tm.views.medications.MedicationRuleUtils', 'tm.jquery.Object', {
      /** members: configs */
      view: null,

      /** constructor */
      Constructor: function(config)
      {
        this.callSuper(config);
      },

      isMedicationRuleSet: function(medicationData, medicationIngredientRuleEnum) // MedicationRuleEnum.java
      {
        if (!tm.jquery.Utils.isEmpty(medicationData))
        {
          var medicationRuleExists = false;
          var medicationIngredients = medicationData.medicationIngredients;
          if (tm.jquery.Utils.isArray(medicationIngredients))
          {
            medicationIngredients.forEach(function(ingredient)
            {
              if (ingredient.ingredientRule === medicationIngredientRuleEnum)
              {
                medicationRuleExists = true;
              }
            });
          }
          return medicationRuleExists;
        }
        return null;
      },

      extractMedicationWithMedicationRule: function(medications, medicationIngredientRuleEnum) // MedicationRuleEnum.java
      {
        var self = this;
        var medicationWithRule = null;

        if (tm.jquery.Utils.isArray(medications))
        {
          medications.some(function(medication)
          {
            if (self.isMedicationRuleSet(medication, medicationIngredientRuleEnum))
            {
              medicationWithRule = medication;
              return true;
            }
            return false;
          });
        }

        return medicationWithRule;
      },

      getParacetamolRuleForTherapy: function(view, therapy, medicationDataList, patientData, referenceWeight)
      {
        var patientAgeInYears = this._calculatePatientAgeInYears(patientData);
        var enums = app.views.medications.TherapyEnums;

        var ruleParameters =
        {
          medicationParacetamolRuleType: enums.medicationParacetamolRuleType.FOR_THERAPY,
          therapyDto: therapy,
          patientHeight: patientData.heightInCm,
          patientWeight: referenceWeight,
          patientAgeInYears: patientAgeInYears,
          medicationDataDtoList: medicationDataList
        };

        return this.getMedicationRule(enums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE, ruleParameters, view);
      },

      getParacetamolRuleForTherapies: function(view, therapies, patientData, referenceWeight)
      {
        var patientAgeInYears = this._calculatePatientAgeInYears(patientData);
        var enums = app.views.medications.TherapyEnums;

        var searchStart = new Date();
        searchStart.setHours(searchStart.getHours() - 24);
        var searchEnd = new Date();

        var ruleParameters =
        {
          medicationParacetamolRuleType: enums.medicationParacetamolRuleType.FOR_THERAPIES,
          therapies: therapies,
          patientHeight: patientData.heightInCm,
          patientWeight: referenceWeight,
          patientAgeInYears: patientAgeInYears,
          searchInterval: {
            startMillis: searchStart.getTime(),
            endMillis: searchEnd.getTime()
          },
          patientId: view.getPatientId()
        };

        return this.getMedicationRule(enums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE, ruleParameters, view);
      },

      getMedicationIngredientWithIngredientRule: function(medicationData, medicationIngredientRuleEnum) // MedicationRuleEnum.java
      {
        var medicationIngredient = null;
        var medicationIngredients = medicationData.medicationIngredients;
        if (tm.jquery.Utils.isArray(medicationIngredients))
        {
          medicationIngredients.forEach(function(ingredient)
          {
            if (ingredient.ingredientRule === medicationIngredientRuleEnum)
            {
              medicationIngredient = ingredient;
            }
          });
        }
        return medicationIngredient;
      },

      getParacetamolRuleForAdministration: function(
          medicationData,  // MedicationDataDto.java
          therapy,
          administrationDate,
          administrationTime,
          patientId,
          view,
          therapyDose,  // TherapyDoseDto.java
          administrationId,
          taskId)
      {
        var enums = app.views.medications.TherapyEnums;
        var deferred = tm.jquery.Deferred.create();

        var medicationIngredientWithRule = null;
        if (!tm.jquery.Utils.isEmpty(medicationData))
        {
          medicationIngredientWithRule = this.getMedicationIngredientWithIngredientRule(
              medicationData,
              app.views.medications.TherapyEnums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
        }

        if (!tm.jquery.Utils.isEmpty(medicationIngredientWithRule) && !tm.jquery.Utils.isEmpty(administrationDate))
        {
          var selectedTimestamp = new Date(
              administrationDate.getFullYear(),
              administrationDate.getMonth(),
              administrationDate.getDate(),
              administrationTime.getHours(),
              administrationTime.getMinutes(),
              0, 0);

          var searchStart = new Date(selectedTimestamp);
          searchStart.setHours(searchStart.getHours() - 24);
          var searchEnd = new Date(selectedTimestamp);

          var patientAge = this._calculatePatientAgeInYears(view.getPatientData());

          var ruleParameters =
          {
            medicationParacetamolRuleType: enums.medicationParacetamolRuleType.FOR_ADMINISTRATION,
            therapyDoseDto: therapyDose,
            taskId: taskId,
            administrationId: administrationId,
            therapyDto: therapy,
            patientHeight: view.getPatientHeightInCm(),
            patientWeight: view.getReferenceWeight(),
            patientAgeInYears: patientAge,
            searchInterval: {
              startMillis: searchStart.getTime(),
              endMillis: searchEnd.getTime()
            },
            patientId: view.getPatientId()
          };

          this.getMedicationRule(enums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE, ruleParameters, view).then(
              function onSuccessFunction(medicationRuleResult)
              {
                deferred.resolve(medicationRuleResult);
              },
              function onSuccessFunction()
              {
                deferred.reject();
              }
          );
        }
        else
        {
          deferred.resolve();
        }
        return deferred.promise();
      },

      getMedicationRule: function(medicationRuleEnum, ruleParameters, view)
      {
        var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_APPLY_MEDICATION_RULE;

        ruleParameters.medicationRuleEnum = medicationRuleEnum;

        var params = {ruleParameters: JSON.stringify(ruleParameters)};

        var deferred = tm.jquery.Deferred.create();

        view.loadPostViewData(url, params, null,
            function(medicationRuleResult)
            {
              deferred.resolve(medicationRuleResult);
            },
            function()
            {
              deferred.reject();
            });

        return deferred.promise();
      },

      /** private methods **/
      _calculatePatientAgeInYears: function(patientData)
      {
        if (!tm.jquery.Utils.isEmpty(patientData) && !tm.jquery.Utils.isEmpty(patientData.birthDate))
        {
          return moment(CurrentTime.get()).diff(new Date(patientData.birthDate), 'years');
        }
        return null;
      }
    }
);
