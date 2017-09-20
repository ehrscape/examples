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

package com.marand.thinkmed.medicationsexternal.service;

import java.util.List;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medicationsexternal.dto.DoseRangeCheckDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.medicationsexternal.dto.WarningsProviderDto;

/**
 * @author Mitja Lapajne
 */
public interface MedicationsExternalService
{
  void preparePlugins();

  List<DoseRangeCheckDto> findDoseRangeChecks(String externalSystem, String externalId);

  List<MedicationsWarningDto> findMedicationWarnings(
      String externalSystem,
      long patientAgeInDays,
      Double patientWeightInKg,
      Double bsaInM2,
      boolean isFemale,
      final List<NamedExternalDto> diseaseTypeValues,
      List<NamedExternalDto> allergiesExternalValues,
      List<MedicationForWarningsSearchDto> medicationSummaries);

  List<WarningsProviderDto> getWarningProviders();

  String getMedicationOverviewProvider();

  String getDoseRangeChecksProvider();
}
