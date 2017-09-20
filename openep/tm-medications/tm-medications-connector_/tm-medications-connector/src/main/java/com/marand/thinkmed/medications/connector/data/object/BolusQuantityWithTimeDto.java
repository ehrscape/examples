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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class BolusQuantityWithTimeDto extends QuantityWithTimeDto
{
  private final Double bolusQuantity;
  private final String bolusUnit;

  public BolusQuantityWithTimeDto(
      final DateTime time,
      final Double quantity,
      final String comment,
      final Double bolusQuantity,
      final String bolusUnit)
  {
    super(time, quantity, comment);
    this.bolusQuantity = bolusQuantity;
    this.bolusUnit = bolusUnit;
  }

  public Double getBolusQuantity()
  {
    return bolusQuantity;
  }

  public String getBolusUnit()
  {
    return bolusUnit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("bolusQuantity", bolusQuantity)
        .append("bolusUnit", bolusUnit)
    ;
  }
}
