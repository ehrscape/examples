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

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.data.object.DataObject;
import com.marand.maf.core.data.object.HourMinuteDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class AdministrationTimingDto extends DataObject
{
  private List<AdministrationTimestampsDto> timestampsList = new ArrayList<AdministrationTimestampsDto>();

  public List<AdministrationTimestampsDto> getTimestampsList()
  {
    return timestampsList;
  }

  public void setTimestampsList(final List<AdministrationTimestampsDto> timestampsList)
  {
    this.timestampsList = timestampsList;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("timestampsList", timestampsList);
  }

  public static class AdministrationTimestampsDto extends DataObject
  {
    private String frequency;
    private List<HourMinuteDto> timesList = new ArrayList<HourMinuteDto>();

    public String getFrequency()
    {
      return frequency;
    }

    public void setFrequency(final String frequency)
    {
      this.frequency = frequency;
    }

    public List<HourMinuteDto> getTimesList()
    {
      return timesList;
    }

    public void setTimesList(final List<HourMinuteDto> timesList)
    {
      this.timesList = timesList;
    }

    @Override
    protected void appendToString(final ToStringBuilder tsb)
    {
      tsb.append("frequency", frequency)
          .append("timesList", timesList);
    }
  }
}
