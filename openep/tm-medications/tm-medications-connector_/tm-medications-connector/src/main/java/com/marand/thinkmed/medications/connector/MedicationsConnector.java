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

package com.marand.thinkmed.medications.connector;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Bostjan Vester
 */
public interface MedicationsConnector
{
  PatientDataForMedicationsDto getPatientData(@Nonnull String patientId, @Nonnull DateTime when);

  PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      String patientId,
      boolean mainDiseaseTypeOnly,
      DateTime when,
      Locale locale);

  Interval getLastDischargedCentralCaseEffectiveInterval(String patientId);

  byte[] getPdfDocument(final String reference);

  List<NamedExternalDto> getCurrentUserCareProviders();

  Map<String, PatientDisplayWithLocationDto> getPatientDisplayWithLocationMap(
      Collection<String> careProviderIds,
      Collection<String> patientIds);

  boolean assertPasswordForUsername(String username, String password);

  List<QuantityWithTimeDto> getBloodSugarObservations(@Nonnull String patientId, @Nonnull Interval interval);
}
