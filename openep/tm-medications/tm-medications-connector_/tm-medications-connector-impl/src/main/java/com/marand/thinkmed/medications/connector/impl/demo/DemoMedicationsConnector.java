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

package com.marand.thinkmed.medications.connector.impl.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */
public class DemoMedicationsConnector implements MedicationsConnector
{
  @Override
  public byte[] getPdfDocument(final String reference)
  {
    return new byte[0];
  }

  @Override
  public PatientDataForMedicationsDto getPatientData(@Nonnull final String patientId, @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    Preconditions.checkNotNull(when, "when is required");

    final List<NamedExternalDto> allergies = new ArrayList<>();
    allergies.add(new NamedExternalDto("91936005", "Penicillin"));

    final MedicationsCentralCaseDto centralCaseDto = new MedicationsCentralCaseDto();
    centralCaseDto.setOutpatient(false);
    centralCaseDto.setCentralCaseId("1");
    centralCaseDto.setEpisodeId("1");
    final NamedExternalDto careProvider = new NamedExternalDto("1", "KOOKIT");
    centralCaseDto.setCareProvider(careProvider);
    centralCaseDto.setCentralCaseEffective(Intervals.infiniteFrom(new DateTime(2014, 11, 20, 12, 0)));

    return new PatientDataForMedicationsDto(
        new DateTime(1984, 5, 3, 0, 0), 52.0, 165.0, Gender.FEMALE, new ArrayList<>(), allergies, centralCaseDto);
  }

  @Override
  public PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      final String patientId,
      final boolean mainDiseaseTypeOnly,
      final DateTime when,
      final Locale locale)
  {
    return null;
  }

  @Override
  public Interval getLastDischargedCentralCaseEffectiveInterval(final String patientId)
  {
    return new Interval(new DateTime(2014, 10, 10, 12, 0), new DateTime(2014, 11, 15, 12, 0));
  }

  @Override
  public List<NamedExternalDto> getCurrentUserCareProviders()
  {
    return Collections.emptyList();
  }

  @Override
  public Map<String, PatientDisplayWithLocationDto> getPatientDisplayWithLocationMap(
      final Collection<String> careProviderIds,
      final Collection<String> patientIds)
  {
    return new HashMap<>();
  }

  @Override
  public boolean assertPasswordForUsername(final String username, final String password)
  {
    return password.length() == 8;
  }

  @Override
  public List<QuantityWithTimeDto> getBloodSugarObservations(
      @Nonnull final String patientId, @Nonnull final Interval interval)
  {
    return Collections.emptyList();
  }
}
