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

package com.marand.thinkmed.medicationsexternal.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */
public class MedicationWarningsDto extends DataTransferObject
{
  private int highSeverityWarningsCount;
  private int significantSeverityWarningsCount;
  private int lowSeverityWarningsCount;
  private int noSeverityWarningsCount;

  private List<MedicationsWarningDto> warnings = new ArrayList<>();

  public List<MedicationsWarningDto> getWarnings()
  {
    return warnings;
  }

  public void setWarnings(final List<MedicationsWarningDto> warnings)
  {
    this.warnings = warnings;
  }

  public int getHighSeverityWarningsCount()
  {
    return highSeverityWarningsCount;
  }

  public void setHighSeverityWarningsCount(final int highSeverityWarningsCount)
  {
    this.highSeverityWarningsCount = highSeverityWarningsCount;
  }

  public int getSignificantSeverityWarningsCount()
  {
    return significantSeverityWarningsCount;
  }

  public void setSignificantSeverityWarningsCount(final int significantSeverityWarningsCount)
  {
    this.significantSeverityWarningsCount = significantSeverityWarningsCount;
  }

  public int getLowSeverityWarningsCount()
  {
    return lowSeverityWarningsCount;
  }

  public void setLowSeverityWarningsCount(final int lowSeverityWarningsCount)
  {
    this.lowSeverityWarningsCount = lowSeverityWarningsCount;
  }

  public int getNoSeverityWarningsCount()
  {
    return noSeverityWarningsCount;
  }

  public void setNoSeverityWarningsCount(final int noSeverityWarningsCount)
  {
    this.noSeverityWarningsCount = noSeverityWarningsCount;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("warnings", warnings)
        .append("highSeverityWarningsCount", highSeverityWarningsCount)
        .append("significantSeverityWarningsCount", significantSeverityWarningsCount)
        .append("lowSeverityWarningsCount", lowSeverityWarningsCount)
        .append("noSeverityWarningsCount", noSeverityWarningsCount);
  }
}
