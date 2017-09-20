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

package com.marand.thinkmed.medications.connector.data.object;

import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class PatientDataForMedicationsDto extends DataTransferObject implements JsonSerializable
{
  private final DateTime birthDate;
  private final Double weightInKg;   //last weight in hospitalization (if patient hospitalized) or last weight in 24h form now
  private final Double heightInCm;   //last height
  private final Gender gender;
  private final List<ExternalCatalogDto> diseases;
  private final List<NamedExternalDto> allergies;
  private final MedicationsCentralCaseDto centralCaseDto;

  public PatientDataForMedicationsDto(
      final DateTime birthDate,
      final Double weightInKg,
      final Double heightInCm,
      final Gender gender,
      final List<ExternalCatalogDto> diseases,
      final List<NamedExternalDto> allergies,
      final MedicationsCentralCaseDto centralCaseDto)
  {
    this.birthDate = birthDate;
    this.weightInKg = weightInKg;
    this.heightInCm = heightInCm;
    this.gender = gender;
    this.diseases = diseases;
    this.allergies = allergies;
    this.centralCaseDto = centralCaseDto;
  }

  public DateTime getBirthDate()
  {
    return birthDate;
  }

  public Double getWeightInKg()
  {
    return weightInKg;
  }

  public Double getHeightInCm()
  {
    return heightInCm;
  }

  public Gender getGender()
  {
    return gender;
  }

  public List<ExternalCatalogDto> getDiseases()
  {
    return diseases;
  }

  public List<NamedExternalDto> getAllergies()
  {
    return allergies;
  }

  public MedicationsCentralCaseDto getCentralCaseDto()
  {
    return centralCaseDto;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("birthDate", birthDate)
        .append("weightInKg", weightInKg)
        .append("heightInCm", heightInCm)
        .append("diseases", diseases)
        .append("allergies", allergies)
        .append("centralCaseDto", centralCaseDto);
  }
}
