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

package com.marand.thinkmed.medications.b2b;

import java.util.List;
import java.util.Locale;

import com.marand.maf.core.service.ThrowableConverter;
import com.marand.thinkmed.api.core.Dictionary;
import com.marand.thinkmed.api.core.data.Coded;
import com.marand.thinkmed.api.core.data.NamedIdentity;
import com.marand.thinkmed.api.medical.data.Care;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.api.user.CurrentUserNameProvider;
import com.marand.thinkmed.medications.dto.MedicationSearchDto;
import com.marand.thinkmed.medications.dto.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.dto.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.report.PatientDataForTherapyReportDto;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public interface MedicationsConnector extends CurrentUserNameProvider, KnownClinic.ValuesProvider, ThrowableConverter, Dictionary
{
  List<? extends NamedIdentity> getMedicalStaff();

  KnownClinic getClinic(String departmentCode);

  NamedIdentity getUsersName(long userId, DateTime when);

  PatientDataForMedicationsDto getPatientData(Long patientId, Long episodeId, DateTime when);

  PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      long patientId,
      Long encounterId,
      Care care,
      DateTime when,
      final Locale locale);

  DateTime getLastDischargeEncounterTime(long patientId);

  MedicationsCentralCaseDto getCentralCaseForMedicationsDto(Long patientId, Coded knownOrganizationalEntity);

  byte[] getPdfDocument(final String reference);

  List<MedicationSearchDto> loadMedicationsTree();
}
