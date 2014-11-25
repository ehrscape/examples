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
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TimedComplexDoseElementDto extends DataObject implements JsonSerializable
{
  private ComplexDoseElementDto doseElement;
  private HourMinuteDto doseTime;

  private String intervalDisplay;
  private String speedDisplay;
  private String speedFormulaDisplay;

  public ComplexDoseElementDto getDoseElement()
  {
    return doseElement;
  }

  public void setDoseElement(final ComplexDoseElementDto doseElement)
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

  public String getIntervalDisplay()
  {
    return intervalDisplay;
  }

  public void setIntervalDisplay(final String intervalDisplay)
  {
    this.intervalDisplay = intervalDisplay;
  }

  public String getSpeedDisplay()
  {
    return speedDisplay;
  }

  public void setSpeedDisplay(final String speedDisplay)
  {
    this.speedDisplay = speedDisplay;
  }

  public String getSpeedFormulaDisplay()
  {
    return speedFormulaDisplay;
  }

  public void setSpeedFormulaDisplay(final String speedFormulaDisplay)
  {
    this.speedFormulaDisplay = speedFormulaDisplay;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("doseElement", doseElement)
        .append("doseTime", doseTime)
        .append("intervalDisplay", intervalDisplay)
        .append("speedDisplay", speedDisplay)
        .append("speedFormulaDisplay", speedFormulaDisplay)
    ;
  }
}
