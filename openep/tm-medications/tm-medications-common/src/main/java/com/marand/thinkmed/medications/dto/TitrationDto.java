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

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class TitrationDto extends DataTransferObject implements JsonSerializable
{
  private TitrationType titrationType;
  private String name;
  private String unit;
  private Double normalRangeMin;
  private Double normalRangeMax;
  private MedicationDataDto medicationData;
  private List<QuantityWithTimeDto> results = new ArrayList<>();
  private List<TherapyForTitrationDto> therapies = new ArrayList<>();

  public TitrationType getTitrationType()
  {
    return titrationType;
  }

  public void setTitrationType(final TitrationType titrationType)
  {
    this.titrationType = titrationType;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getUnit()
  {
    return unit;
  }

  public void setUnit(final String unit)
  {
    this.unit = unit;
  }

  public Double getNormalRangeMin()
  {
    return normalRangeMin;
  }

  public void setNormalRangeMin(final Double normalRangeMin)
  {
    this.normalRangeMin = normalRangeMin;
  }

  public Double getNormalRangeMax()
  {
    return normalRangeMax;
  }

  public void setNormalRangeMax(final Double normalRangeMax)
  {
    this.normalRangeMax = normalRangeMax;
  }

  public MedicationDataDto getMedicationData()
  {
    return medicationData;
  }

  public void setMedicationData(final MedicationDataDto medicationData)
  {
    this.medicationData = medicationData;
  }

  public List<QuantityWithTimeDto> getResults()
  {
    return results;
  }

  public void setResults(final List<QuantityWithTimeDto> results)
  {
    this.results = results;
  }

  public List<TherapyForTitrationDto> getTherapies()
  {
    return therapies;
  }

  public void setTherapies(final List<TherapyForTitrationDto> therapies)
  {
    this.therapies = therapies;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("titrationType", titrationType)
        .append("name", name)
        .append("unit", unit)
        .append("normalRangeMin", normalRangeMin)
        .append("normalRangeMax", normalRangeMax)
        .append("medicationData", medicationData)
        .append("results", results)
        .append("therapies", therapies);
  }
}
