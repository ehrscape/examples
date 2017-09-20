/*
 * Copyright (c) 2010-2015 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.common.TherapyDetailsLegendContainer.Filters', 'tm.jquery.Object', {
    isMedicationNonFormulary: function(medicationData)
    {
      return !medicationData.isFormulary();
    },

    isMedicationControlled: function(medicationData)
    {
      return medicationData.isControlledDrug();
    },

    isMedicationBlackTriangle: function(medicationData)
    {
      return medicationData.isBlackTriangleMedication();
    },

    isMedicationUnlicensed: function(medicationData)
    {
      return medicationData.isUnlicensedMedication();
    },

    isMedicationHighAlert: function(medicationData)
    {
      return medicationData.isHighAlertMedication();
    },

    isMedicationClinicalTrial: function(medicationData)
    {
      return medicationData.isClinicalTrialMedication();
    },
    isMedicationExpensive: function(medicationData)
    {
      return medicationData.isExpensiveDrug();
    },

    isTaskLate: function(task)
    {
      return CurrentTime.get().getTime() - new Date(task.dueTime).getTime() > 24 * 60 * 60 * 1000;
    },

    isDoctorReviewTaskActive: function(task)
    {
      var enums = app.views.medications.TherapyEnums;
      return task.taskType === enums.taskTypeEnum.DOCTOR_REVIEW;
    },

    isSwitchToOralTaskActive: function(task)
    {
      var enums = app.views.medications.TherapyEnums;
      return task.taskType === enums.taskTypeEnum.SWITCH_TO_ORAL;
    },

    isPerfusionSyringeStartTaskActive: function(task)
    {
      var enums = app.views.medications.TherapyEnums;
      return task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_START;
    },

    isPerfusionSyringeCompleteTaskActive: function(task)
    {
      var enums = app.views.medications.TherapyEnums;
      return task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_COMPLETE;
    },

    isPerfusionSyringeDispenseTaskActive: function(task)
    {
      var enums = app.views.medications.TherapyEnums;
      return task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_DISPENSE;
    },

    isSupplyReminderTaskActive: function(task)
    {
      var enums = app.views.medications.TherapyEnums;
      return task.taskType === enums.taskTypeEnum.SUPPLY_REMINDER;
    },

    isSupplyReviewTaskActive: function(task)
    {
      var enums = app.views.medications.TherapyEnums;
      return task.taskType === enums.taskTypeEnum.SUPPLY_REVIEW;
    }
});