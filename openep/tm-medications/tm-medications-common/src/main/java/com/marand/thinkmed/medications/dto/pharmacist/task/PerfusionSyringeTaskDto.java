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

package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringeLabelDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class PerfusionSyringeTaskDto extends TherapyTaskSimpleDto
{
  private int numberOfSyringes;
  private String originalTherapyId;
  private NamedExternalDto orderedBy;
  private TherapyDayDto therapyDayDto;
  private PerfusionSyringeLabelDto perfusionSyringeLabelDto;
  private boolean printSystemLabel;

  public int getNumberOfSyringes()
  {
    return numberOfSyringes;
  }

  public void setNumberOfSyringes(final int numberOfSyringes)
  {
    this.numberOfSyringes = numberOfSyringes;
  }

  public TherapyDayDto getTherapyDayDto()
  {
    return therapyDayDto;
  }

  public void setTherapyDayDto(final TherapyDayDto therapyDayDto)
  {
    this.therapyDayDto = therapyDayDto;
  }

  public String getOriginalTherapyId()
  {
    return originalTherapyId;
  }

  public void setOriginalTherapyId(final String originalTherapyId)
  {
    this.originalTherapyId = originalTherapyId;
  }

  public PerfusionSyringeLabelDto getPerfusionSyringeLabelDto()
  {
    return perfusionSyringeLabelDto;
  }

  public void setPerfusionSyringeLabelDto(final PerfusionSyringeLabelDto perfusionSyringeLabelDto)
  {
    this.perfusionSyringeLabelDto = perfusionSyringeLabelDto;
  }

  public NamedExternalDto getOrderedBy()
  {
    return orderedBy;
  }

  public void setOrderedBy(final NamedExternalDto orderedBy)
  {
    this.orderedBy = orderedBy;
  }

  public boolean isPrintSystemLabel()
  {
    return printSystemLabel;
  }

  public void setPrintSystemLabel(final boolean printSystemLabel)
  {
    this.printSystemLabel = printSystemLabel;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("numberOfSyringes", numberOfSyringes)
        .append("originalTherapyId", originalTherapyId)
        .append("orderedBy", orderedBy)
        .append("therapyDayDto", therapyDayDto)
        .append("perfusionSyringeLabelDto", perfusionSyringeLabelDto)
        .append("printSystemLabel", printSystemLabel);
  }
}
