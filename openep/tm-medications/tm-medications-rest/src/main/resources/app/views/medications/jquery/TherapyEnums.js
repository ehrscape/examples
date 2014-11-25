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

Class.define('app.views.medications.TherapyEnums', 'tm.jquery.Object', {
  /** members: configs */

  //ALWAYS ADD NEW ENUMS TO TherapyEnumsTest.java

  statics: {
    /** enums */
    templateTypeEnum:  //java class TherapyTemplateTypeEnum
    {
      USER: "USER",
      ORGANIZATIONAL: "ORGANIZATIONAL",
      PATIENT: "PATIENT"
    },
    medicationRouteTypeEnum:  //java class MedicationRouteTypeEnum
    {
      IV: "IV"
    },
    dosingFrequencyTypeEnum:  //java class DosingFrequencyTypeEnum
    {
      BETWEEN_DOSES: "BETWEEN_DOSES", //time between doses in hours
      DAILY_COUNT: "DAILY_COUNT", //number of administrations per day
      MORNING: "MORNING", //once per day in the morning
      NOON: "NOON", //once per day at noon
      EVENING: "EVENING", //once per day in the evening
      ONCE_THEN_EX: "ONCE_THEN_EX" //only once
    },
    medicationOrderFormType: //java class MedicationOrderFormType
    {
      SIMPLE: "SIMPLE",
      COMPLEX: "COMPLEX",
      DESCRIPTIVE: "DESCRIPTIVE"
    },
    therapyStatusEnum:    //java class TherapyStatusEnum
    {
      NORMAL: "NORMAL",
      REVIEWED: "REVIEWED",
      ABORTED: "ABORTED",
      CANCELLED: "CANCELLED",
      SUSPENDED: "SUSPENDED",
      LATE: "LATE",
      VERY_LATE: "VERY_LATE",
      FUTURE: "FUTURE"
    },
    administrationTypeEnum: //java class AdministrationTypeEnum
    {
      START: "START",
      STOP: "STOP",
      ADJUST_INFUSION: "ADJUST_INFUSION",
      INFUSION_SET_CHANGE: "INFUSION_SET_CHANGE"
    },
    infusionSetChangeEnum: //java class InfusionSetChangeEnum
    {
      INFUSION_SYSTEM_CHANGE: "INFUSION_SYSTEM_CHANGE",
      INFUSION_SYRINGE_CHANGE: "INFUSION_SYRINGE_CHANGE"
    },
    therapySortTypeEnum: //java class TherapySortTypeEnum
    {
      DESCRIPTION_ASC: "DESCRIPTION_ASC",
      DESCRIPTION_DESC: "DESCRIPTION_DESC",
      CREATED_TIME_ASC: "CREATED_TIME_ASC",
      CREATED_TIME_DESC: "CREATED_TIME_DESC"
    },
    medicationStartCriterionEnum: //java class MedicationStartCriterionEnum
    {
      BY_DOCTOR_ORDERS: "BY_DOCTOR_ORDERS",
      BEFORE_MEAL: "BEFORE_MEAL",
      AFTER_MEAL: "AFTER_MEAL"
    },
    therapyTag: //java class TherapyTag
    {
      PRESCRIPTION: "PRESCRIPTION"
    },
    warningSeverityEnum: //java MedicationsWarningDto.Severity
    {
      LOW: "LOW",
      MEDIUM: "MEDIUM",
      SIGNIFICANT: "SIGNIFICANT",
      HIGH: "HIGH"
    }
  }
});