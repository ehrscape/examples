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

package com.marand.thinkmed.api.demographics;

import com.marand.thinkmed.api.demographics.data.object.NameDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;

/**
 * @author Saxo
 */
public enum PersonNameForm
{
  NAME_SURNAME(
      (firstName, lastName, prefix, suffix) -> combineName(firstName, lastName, " ")),
  PREFIX_NAME_SURNAME_SUFFIX(
      new NameCalculator()
      {
        @Override
        public String calculateName(final String firstName, final String lastName, final String prefix, final String suffix)
        {
          final String name = NAME_SURNAME.calculateFullName(firstName, lastName, null, null);
          final String prefixName = combineName(prefix, name, " ");

          return combineName(prefixName, suffix, ", ");
        }
      }),
  SURNAME_NAME(
      (firstName, lastName, prefix, suffix) -> combineName(lastName, firstName, " ")),
  SURNAME_COMMA_NAME(
      (firstName, lastName, prefix, suffix) -> combineName(lastName, firstName, ", "));

  private final NameCalculator calculator;

  PersonNameForm(final NameCalculator calculator)
  {
    this.calculator = calculator;
  }

  public String calculateFullName(final NameDto nameDto)
  {
    return
        nameDto == null
        ? ""
        : calculateFullName(
            DemographicsUtils.calculateName(nameDto.getFirstName1(), nameDto.getFirstName2(), nameDto.getFirstNameHyphen()),
            DemographicsUtils.calculateName(nameDto.getLastName1(), nameDto.getLastName2(), nameDto.getLastNameHyphen()),
            nameDto.getPrefix(),
            nameDto.getSuffix());
  }

  public String calculateFullName(final String firstName, final String lastName, final String prefix, final String suffix)
  {
    return calculator.calculateName(firstName, lastName, prefix, suffix);
  }

  private interface NameCalculator
  {
    String calculateName(final String firstName, final String lastName, final String prefix, final String suffix);
  }

  private static String combineName(final String name1, final String name2, final String delimiter)
  {
    final StrBuilder builder = new StrBuilder();
    if (StringUtils.isNotBlank(name1))
    {
      builder.append(name1);
    }
    if (StringUtils.isNotBlank(name2))
    {
      if (!builder.isEmpty())
      {
        builder.append(delimiter);
      }
      builder.append(name2);
    }

    return builder.toString();
  }
}