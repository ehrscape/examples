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

package com.marand.thinkmed.medications.dto;

import java.util.List;
import java.util.Set;

import com.marand.maf.core.JsonSerializable;
import com.marand.maf.core.data.object.DataObject;
import com.marand.thinkmed.api.demographics.data.Gender;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class PatientDataForMedicationsDto extends DataObject implements JsonSerializable
{
  private final Long ageInDays;
  private final Double weightInKg;   //last weight in hospitalization (if patient hospitalized) or last weight in 24h form now
  private final Double heightInCm;   //last height
  private final Integer gabInWeeks;
  private final Gender gender;
  private final Set<String> diseaseTypeCodes;
  private List<Long> allergyIds;

  public PatientDataForMedicationsDto(
      final Long ageInDays,
      final Double weightInKg,
      final Double heightInCm,
      final Integer gabInWeeks,
      final Gender gender,
      final Set<String> diseaseTypeCodes,
      final List<Long> allergyIds)
  {
    this.ageInDays = ageInDays;
    this.weightInKg = weightInKg;
    this.heightInCm = heightInCm;
    this.gabInWeeks = gabInWeeks;
    this.gender = gender;
    this.diseaseTypeCodes = diseaseTypeCodes;
    this.allergyIds = allergyIds;
  }

  public Long getAgeInDays()
  {
    return ageInDays;
  }

  public Double getWeightInKg()
  {
    return weightInKg;
  }

  public Double getHeightInCm()
  {
    return heightInCm;
  }

  public Integer getGabInWeeks()
  {
    return gabInWeeks;
  }

  public Gender getGender()
  {
    return gender;
  }

  public Set<String> getDiseaseTypeCodes()
  {
    return diseaseTypeCodes;
  }

  public List<Long> getAllergyIds()
  {
    return allergyIds;
  }

  public void setAllergyIds(final List<Long> allergyIds)
  {
    this.allergyIds = allergyIds;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("ageInDays", ageInDays)
        .append("weightInKg", weightInKg)
        .append("heightInCm", heightInCm)
        .append("gabInWeeks", gabInWeeks)
        .append("diseaseTypeCodes", diseaseTypeCodes)
        .append("allergyIds", allergyIds);
  }
}
