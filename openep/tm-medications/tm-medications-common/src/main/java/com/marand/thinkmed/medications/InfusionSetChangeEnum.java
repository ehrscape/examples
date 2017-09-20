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

package com.marand.thinkmed.medications;

/**
 * @author Mitja Lapajne
 */
public enum InfusionSetChangeEnum
{
  INFUSION_SYSTEM_CHANGE("M.1"),
  INFUSION_SYRINGE_CHANGE("M.2");

  private final String code;

  InfusionSetChangeEnum(final String code)
  {
    this.code = code;
  }

  public String getCode()
  {
    return code;
  }

  public static InfusionSetChangeEnum getEnumByCode(final String code)
  {
    for (final InfusionSetChangeEnum infusionSetChangeEnum : values())
    {
      if (infusionSetChangeEnum.getCode().equals(code))
      {
        return infusionSetChangeEnum;
      }
    }
    return null;
  }
}