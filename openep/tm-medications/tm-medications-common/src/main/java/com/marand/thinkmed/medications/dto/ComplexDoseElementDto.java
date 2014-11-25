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

import com.marand.maf.core.data.object.DataObject;
import com.marand.maf.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class ComplexDoseElementDto extends DataObject implements JsonSerializable
{
  private Integer duration; //in minutes
  private Double rate;
  private String rateUnit;
  private Double rateFormula;
  private String rateFormulaUnit;

  public Integer getDuration()
  {
    return duration;
  }

  public void setDuration(final Integer duration)
  {
    this.duration = duration;
  }

  public Double getRate()
  {
    return rate;
  }

  public void setRate(final Double rate)
  {
    this.rate = rate;
  }

  public String getRateUnit()
  {
    return rateUnit;
  }

  public void setRateUnit(final String rateUnit)
  {
    this.rateUnit = rateUnit;
  }

  public Double getRateFormula()
  {
    return rateFormula;
  }

  public void setRateFormula(final Double rateFormula)
  {
    this.rateFormula = rateFormula;
  }

  public String getRateFormulaUnit()
  {
    return rateFormulaUnit;
  }

  public void setRateFormulaUnit(final String rateFormulaUnit)
  {
    this.rateFormulaUnit = rateFormulaUnit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("duration", duration)
        .append("rate", rate)
        .append("rateUnit", rateUnit)
        .append("rateFormula", rateFormula)
        .append("rateFormulaUnit", rateFormulaUnit)
    ;
  }
}
