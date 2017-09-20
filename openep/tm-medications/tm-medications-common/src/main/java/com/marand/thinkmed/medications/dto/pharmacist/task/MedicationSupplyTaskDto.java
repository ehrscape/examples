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

import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */

public class MedicationSupplyTaskDto extends PatientTaskDto
{
  private MedicationSupplyTypeEnum supplyTypeEnum;
  private Integer supplyInDays;
  private TherapyDayDto therapyDayDto;
  private DateTime createdDateTime;
  private DateTime closedDateTime;

  public MedicationSupplyTypeEnum getSupplyTypeEnum()
  {
    return supplyTypeEnum;
  }

  public void setSupplyTypeEnum(final MedicationSupplyTypeEnum supplyTypeEnum)
  {
    this.supplyTypeEnum = supplyTypeEnum;
  }

  public Integer getSupplyInDays()
  {
    return supplyInDays;
  }

  public void setSupplyInDays(final Integer supplyInDays)
  {
    this.supplyInDays = supplyInDays;
  }

  public TherapyDayDto getTherapyDayDto()
  {
    return therapyDayDto;
  }

  public void setTherapyDayDto(final TherapyDayDto therapyDayDto)
  {
    this.therapyDayDto = therapyDayDto;
  }

  public DateTime getCreatedDateTime()
  {
    return createdDateTime;
  }

  public void setCreatedDateTime(final DateTime createdDateTime)
  {
    this.createdDateTime = createdDateTime;
  }

  public DateTime getClosedDateTime()
  {
    return closedDateTime;
  }

  public void setClosedDateTime(final DateTime closedDateTime)
  {
    this.closedDateTime = closedDateTime;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("supplyTypeEnum", supplyTypeEnum)
        .append("supplyInDays", supplyInDays)
        .append("createdDateTime", createdDateTime)
        .append("closedDateTime", closedDateTime)
        .append("therapyDayDto", therapyDayDto);
  }
}
