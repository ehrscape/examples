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

import com.marand.maf.core.JsonSerializable;
import com.marand.maf.core.data.object.DataObject;
import com.marand.maf.core.data.object.HourMinuteDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class TimedSimpleDoseElementDto extends DataObject implements JsonSerializable
{
  private SimpleDoseElementDto doseElement;
  private HourMinuteDto doseTime;
  private DateTime date; //null means every day

  private String timeDisplay;
  private String quantityDisplay;

  public SimpleDoseElementDto getDoseElement()
  {
    return doseElement;
  }

  public void setDoseElement(final SimpleDoseElementDto doseElement)
  {
    this.doseElement = doseElement;
  }

  public HourMinuteDto getDoseTime()
  {
    return doseTime;
  }

  public void setDoseTime(final HourMinuteDto doseTime)
  {
    this.doseTime = doseTime;
  }

  public String getTimeDisplay()
  {
    return timeDisplay;
  }

  public void setTimeDisplay(final String timeDisplay)
  {
    this.timeDisplay = timeDisplay;
  }

  public String getQuantityDisplay()
  {
    return quantityDisplay;
  }

  public void setQuantityDisplay(final String quantityDisplay)
  {
    this.quantityDisplay = quantityDisplay;
  }

  public DateTime getDate()
  {
    return date;
  }

  public void setDate(final DateTime date)
  {
    this.date = date;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("doseElement", doseElement)
        .append("doseTime", doseTime)
        .append("timeDisplay", timeDisplay)
        .append("quantityDisplay", quantityDisplay)
        .append("date", date)
    ;
  }
}
