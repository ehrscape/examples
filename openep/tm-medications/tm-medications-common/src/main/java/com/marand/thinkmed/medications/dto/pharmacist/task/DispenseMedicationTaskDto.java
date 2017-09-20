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

import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */

public class DispenseMedicationTaskDto extends MedicationSupplyTaskDto
{
  private SupplyRequestStatus supplyRequestStatus;
  private TherapyAssigneeEnum requesterRole;
  private DateTime lastPrintedTimestamp;

  public DispenseMedicationTaskDto()
  {
    setTaskType(TaskTypeEnum.DISPENSE_MEDICATION);
  }

  public TherapyAssigneeEnum getRequesterRole()
  {
    return requesterRole;
  }

  public void setRequesterRole(final TherapyAssigneeEnum requesterRole)
  {
    this.requesterRole = requesterRole;
  }

  public SupplyRequestStatus getSupplyRequestStatus()
  {
    return supplyRequestStatus;
  }

  public void setSupplyRequestStatus(final SupplyRequestStatus supplyRequestStatus)
  {
    this.supplyRequestStatus = supplyRequestStatus;
  }

  public DateTime getLastPrintedTimestamp()
  {
    return lastPrintedTimestamp;
  }

  public void setLastPrintedTimestamp(final DateTime lastPrintedTimestamp)
  {
    this.lastPrintedTimestamp = lastPrintedTimestamp;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("supplyRequestStatus", supplyRequestStatus)
        .append("requesterRole", requesterRole)
        .append("lastPrintedTimestamp", lastPrintedTimestamp);
  }
}
