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

import com.marand.maf.core.data.object.IdentityDo;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.Interval;

/**
 * @author Bostjan Vester
 */
public class MedicationForWarningsSearchDto extends IdentityDo
{
  private String externalId;
  private String description;
  private Double doseAmount;
  private String doseUnit;
  private String routeCode;
  private int frequency;
  private String frequencyUnit;
  private Interval effective;
  private boolean onlyOnce;
  private boolean prospective;
  private boolean product;

  public String getExternalId()
  {
    return externalId;
  }

  public void setExternalId(final String externalId)
  {
    this.externalId = externalId;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(final String description)
  {
    this.description = description;
  }

  public Double getDoseAmount()
  {
    return doseAmount;
  }

  public void setDoseAmount(final Double doseAmount)
  {
    this.doseAmount = doseAmount;
  }

  public String getDoseUnit()
  {
    return doseUnit;
  }

  public void setDoseUnit(final String doseUnit)
  {
    this.doseUnit = doseUnit;
  }

  public String getRouteCode()
  {
    return routeCode;
  }

  public void setRouteCode(final String routeCode)
  {
    this.routeCode = routeCode;
  }

  public int getFrequency()
  {
    return frequency;
  }

  public void setFrequency(final int frequency)
  {
    this.frequency = frequency;
  }

  public String getFrequencyUnit()
  {
    return frequencyUnit;
  }

  public void setFrequencyUnit(final String frequencyUnit)
  {
    this.frequencyUnit = frequencyUnit;
  }

  public Interval getEffective()
  {
    return effective;
  }

  public void setEffective(final Interval effective)
  {
    this.effective = effective;
  }

  public boolean isOnlyOnce()
  {
    return onlyOnce;
  }

  public void setOnlyOnce(final boolean onlyOnce)
  {
    this.onlyOnce = onlyOnce;
  }

  public boolean isProspective()
  {
    return prospective;
  }

  public void setProspective(final boolean prospective)
  {
    this.prospective = prospective;
  }

  public boolean isProduct()
  {
    return product;
  }

  public void setProduct(final boolean product)
  {
    this.product = product;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("externalId", externalId)
        .append("description", description)
        .append("doseAmount", doseAmount)
        .append("routeCode", routeCode)
        .append("frequency", frequency)
        .append("frequencyUnit", frequencyUnit)
        .append("effective", effective)
        .append("onlyOnce", onlyOnce)
        .append("prospective", prospective)
        .append("product", product)
        ;
  }
}
