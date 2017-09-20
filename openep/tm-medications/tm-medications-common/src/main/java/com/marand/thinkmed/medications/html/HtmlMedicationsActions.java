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

package com.marand.thinkmed.medications.html;

/**
 * @author Bostjan Vester
 */
public interface HtmlMedicationsActions
{
  String PRINT_SURGERY_THERAPY_REPORT = "printSurgeryTherapyReport";
  String SAVE_CONTEXT = "SAVE_CONTEXT";
  String OPEN_MEDICATION_DOCUMENT = "openMedicationDocument";
  String OPEN_PATIENT = "openPatient";
  String OUTPATIENT_PRESCRIPTION = "outpatientPrescription";
  String CANCEL_PRESCRIPTION = "cancelPrescription";
  String UPDATE_OUTPATIENT_PRESCRIPTION = "updateOutpatientPrescription";
  String GET_EXTERNAL_OUTPATIENT_PRESCRIPTIONS = "getExternalOutpatientPrescription";
  String PRINT_PERFUSION_SYRINGE_LABEL = "perfusionSyringeLabelPrint";
  String DELETE_OUTPATIENT_PRESCRIPTION = "deleteOutpatientPrescription";
  String AUTHORIZE_OUTPATIENT_PRESCRIPTION = "authorizeOutpatientPrescription";
  String AUTHENTICATE_ADMINISTRATION_WITNESS = "authenticateAdministrationWitness";
}
