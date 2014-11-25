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
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TherapyDoseDto extends DataObject
{
  private TherapyDoseTypeEnum therapyDoseTypeEnum;
  private Double numerator;
  private String numeratorUnit;
  private Double denominator;
  private String denominatorUnit;

  public Double getNumerator()
  {
    return numerator;
  }

  public void setNumerator(final Double numerator)
  {
    this.numerator = numerator;
  }

  public String getNumeratorUnit()
  {
    return numeratorUnit;
  }

  public void setNumeratorUnit(final String numeratorUnit)
  {
    this.numeratorUnit = numeratorUnit;
  }

  public Double getDenominator()
  {
    return denominator;
  }

  public void setDenominator(final Double denominator)
  {
    this.denominator = denominator;
  }

  public String getDenominatorUnit()
  {
    return denominatorUnit;
  }

  public void setDenominatorUnit(final String denominatorUnit)
  {
    this.denominatorUnit = denominatorUnit;
  }

  public TherapyDoseTypeEnum getTherapyDoseTypeEnum()
  {
    return therapyDoseTypeEnum;
  }

  public void setTherapyDoseTypeEnum(final TherapyDoseTypeEnum therapyDoseTypeEnum)
  {
    this.therapyDoseTypeEnum = therapyDoseTypeEnum;
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj != null && obj instanceof TherapyDoseDto)
    {
      final TherapyDoseDto doseDto = (TherapyDoseDto)obj;
      final boolean compareDose =
          (doseDto.getTherapyDoseTypeEnum() == null && therapyDoseTypeEnum == null) ||
              (doseDto.getTherapyDoseTypeEnum() != null && doseDto.getTherapyDoseTypeEnum() == therapyDoseTypeEnum);
      final boolean compareNumerator =
          (doseDto.getNumerator() == null && numerator == null) ||
              (doseDto.getNumerator() != null &&  numerator != null &&
                  Math.abs(doseDto.getNumerator() - numerator) < 0.0001 &&
                  doseDto.getNumeratorUnit().equals(numeratorUnit)
              );
      final boolean compareDenominator =
          (doseDto.getDenominator() == null && denominator == null) ||
              (doseDto.getDenominator() != null && denominator != null &&
                  Math.abs(doseDto.getDenominator() - denominator) < 0.0001 &&
                  doseDto.getDenominatorUnit().equals(denominatorUnit)
              );
      //return compareDose && compareNumerator && compareDenominator;
      return compareNumerator && compareDenominator;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(therapyDoseTypeEnum);
    buffer.append(numerator);
    buffer.append(numeratorUnit);
    buffer.append(denominator);
    buffer.append(denominatorUnit);
    return buffer.toString().hashCode();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("numerator", numerator)
        .append("numeratorUnit", numeratorUnit)
        .append("denominator", denominator)
        .append("denominatorUnit", denominatorUnit)
        .append("therapyDoseTypeEnum", therapyDoseTypeEnum);
  }
}
