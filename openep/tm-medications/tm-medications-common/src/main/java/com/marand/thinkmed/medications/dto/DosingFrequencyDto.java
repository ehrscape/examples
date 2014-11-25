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

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.marand.maf.core.data.object.DataObject;
import com.marand.thinkmed.medications.DosingFrequencyTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class DosingFrequencyDto extends DataObject
{
  private DosingFrequencyTypeEnum type;
  private Integer value;

  public DosingFrequencyDto(final DosingFrequencyTypeEnum type)
  {
    this(type, null);
  }

  public DosingFrequencyDto(final DosingFrequencyTypeEnum type, @Nullable final Integer value)
  {
    this.type = type;
    this.value = value;
  }

  public DosingFrequencyTypeEnum getType()
  {
    return type;
  }

  public void setType(final DosingFrequencyTypeEnum type)
  {
    this.type = type;
  }

  public Integer getValue()
  {
    return value;
  }

  public void setValue(final Integer value)
  {
    this.value = value;
  }

  public String getKey()
  {
    if (type == DosingFrequencyTypeEnum.BETWEEN_DOSES)
    {
      Preconditions.checkNotNull(value);
      return String.valueOf(value) + 'H';
    }
    if (type == DosingFrequencyTypeEnum.DAILY_COUNT)
    {
      Preconditions.checkNotNull(value);
      return String.valueOf(value) + 'X';
    }
    Preconditions.checkArgument(value == null);
    return type.name();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("type", type)
        .append("value", value);
  }
}
