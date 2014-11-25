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
public class SimpleDoseElementDto extends DataObject implements JsonSerializable
{
  private Double quantity;
  private String doseDescription;
  private Double quantityDenominator;

  public Double getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final Double quantity)
  {
    this.quantity = quantity;
  }

  public Double getQuantityDenominator()
  {
    return quantityDenominator;
  }

  public void setQuantityDenominator(final Double quantityDenominator)
  {
    this.quantityDenominator = quantityDenominator;
  }

  public String getDoseDescription()
  {
    return doseDescription;
  }

  public void setDoseDescription(final String doseDescription)
  {
    this.doseDescription = doseDescription;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("quantity", quantity)
        .append("doseDescription", doseDescription)
        .append("quantityDenominator", quantityDenominator)
    ;
  }
}
