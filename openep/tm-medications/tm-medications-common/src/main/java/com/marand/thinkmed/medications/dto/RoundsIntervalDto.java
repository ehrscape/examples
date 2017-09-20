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

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class RoundsIntervalDto extends DataTransferObject
{
  private int startHour;
  private int startMinute;
  private int endHour;
  private int endMinute;

  public int getStartHour()
  {
    return startHour;
  }

  public void setStartHour(final int startHour)
  {
    this.startHour = startHour;
  }

  public int getStartMinute()
  {
    return startMinute;
  }

  public void setStartMinute(final int startMinute)
  {
    this.startMinute = startMinute;
  }

  public int getEndHour()
  {
    return endHour;
  }

  public void setEndHour(final int endHour)
  {
    this.endHour = endHour;
  }

  public int getEndMinute()
  {
    return endMinute;
  }

  public void setEndMinute(final int endMinute)
  {
    this.endMinute = endMinute;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("startHour", startHour)
        .append("startMinute", startMinute)
        .append("endHour", endHour)
        .append("endMinute", endMinute);
  }
}
