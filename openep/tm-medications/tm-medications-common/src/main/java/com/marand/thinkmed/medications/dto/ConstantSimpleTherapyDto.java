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

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.dto.dose.SimpleDoseElementDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class ConstantSimpleTherapyDto extends SimpleTherapyDto implements ConstantTherapy
{
  private SimpleDoseElementDto doseElement;
  private TitrationType titration;
  private List<HourMinuteDto> doseTimes = new ArrayList<>();

  public ConstantSimpleTherapyDto()
  {
    super(false);
  }

  public SimpleDoseElementDto getDoseElement()
  {
    return doseElement;
  }

  public void setDoseElement(final SimpleDoseElementDto doseElement)
  {
    this.doseElement = doseElement;
  }

  @Override
  public TitrationType getTitration()
  {
    return titration;
  }

  @Override
  public void setTitration(final TitrationType titration)
  {
    this.titration = titration;
  }

  @Override
  public List<HourMinuteDto> getDoseTimes()
  {
    return doseTimes;
  }

  @Override
  public void setDoseTimes(final List<HourMinuteDto> doseTimes)
  {
    this.doseTimes = doseTimes;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("doseElement", doseElement)
        .append("titration", titration)
        .append("doseTimes", doseTimes);
  }
}
