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

package com.marand.thinkmed.medicationsexternal.dto;

import com.marand.maf.core.data.object.DataObject;
import com.marand.maf.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class DoseRangeCheckDto extends DataObject implements JsonSerializable
{
  private Long ageFrom;
  private Long ageTo;
  private String route;
  private UnitValueDto<Double, String> doseLow;
  private UnitValueDto<Double, String> doseHigh;
  private UnitValueDto<Double, String> maxDailyDose;
  private String indicationDescription;

  public Long getAgeFrom()
  {
    return ageFrom;
  }

  public void setAgeFrom(final Long ageFrom)
  {
    this.ageFrom = ageFrom;
  }

  public Long getAgeTo()
  {
    return ageTo;
  }

  public void setAgeTo(final Long ageTo)
  {
    this.ageTo = ageTo;
  }

  public UnitValueDto<Double, String> getDoseHigh()
  {
    return doseHigh;
  }

  public void setDoseHigh(final UnitValueDto<Double, String> doseHigh)
  {
    this.doseHigh = doseHigh;
  }

  public UnitValueDto<Double, String> getDoseLow()
  {
    return doseLow;
  }

  public void setDoseLow(final UnitValueDto<Double, String> doseLow)
  {
    this.doseLow = doseLow;
  }

  public UnitValueDto<Double, String> getMaxDailyDose()
  {
    return maxDailyDose;
  }

  public void setMaxDailyDose(final UnitValueDto<Double, String> maxDailyDose)
  {
    this.maxDailyDose = maxDailyDose;
  }

  public String getRoute()
  {
    return route;
  }

  public void setRoute(final String route)
  {
    this.route = route;
  }

  public String getIndicationDescription()
  {
    return indicationDescription;
  }

  public void setIndicationDescription(final String indicationDescription)
  {
    this.indicationDescription = indicationDescription;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("ageFrom", ageFrom)
        .append("ageTo", ageTo)
        .append("route", route)
        .append("doseLow", doseLow)
        .append("doseHigh", doseHigh)
        .append("maxDailyDose", maxDailyDose)
        .append("indicationDescription", indicationDescription)
        ;
  }
}
