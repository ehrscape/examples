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

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class MedicationSupplyTaskSimpleDto extends DataTransferObject
{
  private String taskId;
  private MedicationSupplyTypeEnum supplyTypeEnum;
  private TaskTypeEnum taskType;
  private Integer supplyInDays;

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(final String taskId)
  {
    this.taskId = taskId;
  }

  public MedicationSupplyTypeEnum getSupplyTypeEnum()
  {
    return supplyTypeEnum;
  }

  public void setSupplyTypeEnum(final MedicationSupplyTypeEnum supplyTypeEnum)
  {
    this.supplyTypeEnum = supplyTypeEnum;
  }

  public TaskTypeEnum getTaskType()
  {
    return taskType;
  }

  public void setTaskType(final TaskTypeEnum taskType)
  {
    this.taskType = taskType;
  }

  public Integer getSupplyInDays()
  {
    return supplyInDays;
  }

  public void setSupplyInDays(final Integer supplyInDays)
  {
    this.supplyInDays = supplyInDays;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("taskId", taskId)
        .append("supplyTypeEnum", supplyTypeEnum)
        .append("taskType", taskType)
        .append("supplyInDays", supplyInDays)
    ;
  }
}
