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

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class PatientDto extends DataTransferObject implements JsonSerializable
{
  private PatientDemographicsDto demographics;
  private EncounterDto encounter;
  private ObservationDto weight;
  private ObservationDto height;
  private AllergiesDto allergies;
  private List<DiseaseDto> diseases = new ArrayList<>();

  public PatientDemographicsDto getDemographics()
  {
    return demographics;
  }

  public void setDemographics(final PatientDemographicsDto demographics)
  {
    this.demographics = demographics;
  }

  public EncounterDto getEncounter()
  {
    return encounter;
  }

  public ObservationDto getWeight()
  {
    return weight;
  }

  public void setWeight(final ObservationDto weight)
  {
    this.weight = weight;
  }

  public ObservationDto getHeight()
  {
    return height;
  }

  public void setHeight(final ObservationDto height)
  {
    this.height = height;
  }

  public AllergiesDto getAllergies()
  {
    return allergies;
  }

  public void setAllergies(final AllergiesDto allergies)
  {
    this.allergies = allergies;
  }

  public List<DiseaseDto> getDiseases()
  {
    return diseases;
  }

  public void setDiseases(final List<DiseaseDto> diseases)
  {
    this.diseases = diseases;
  }

  public void setEncounter(final EncounterDto encounter)
  {
    this.encounter = encounter;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientDemographics", demographics)
        .append("encounter", encounter)
        .append("weight", weight)
        .append("height", height)
        .append("allergies", allergies)
        .append("diseases", diseases);
  }
}
