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

package com.marand.thinkmed.medications.dto.administration;

import com.marand.maf.core.data.object.DataObject;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class TherapyTaskDto extends DataObject
{
  private String taskId;
  private String administrationId;
  private String therapyId;
  private DateTime plannedAdministrationTime;
  private AdministrationTypeEnum administrationTypeEnum;
  private TherapyDoseDto therapyDoseDto;
  private String triggersTherapyId;

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(final String taskId)
  {
    this.taskId = taskId;
  }

  public String getAdministrationId()
  {
    return administrationId;
  }

  public void setAdministrationId(final String administrationId)
  {
    this.administrationId = administrationId;
  }

  public String getTherapyId()
  {
    return therapyId;
  }

  public void setTherapyId(final String therapyId)
  {
    this.therapyId = therapyId;
  }

  public DateTime getPlannedAdministrationTime()
  {
    return plannedAdministrationTime;
  }

  public void setPlannedAdministrationTime(final DateTime plannedAdministrationTime)
  {
    this.plannedAdministrationTime = plannedAdministrationTime;
  }

  public AdministrationTypeEnum getAdministrationTypeEnum()
  {
    return administrationTypeEnum;
  }

  public void setAdministrationTypeEnum(final AdministrationTypeEnum administrationTypeEnum)
  {
    this.administrationTypeEnum = administrationTypeEnum;
  }

  public TherapyDoseDto getTherapyDoseDto()
  {
    return therapyDoseDto;
  }

  public void setTherapyDoseDto(final TherapyDoseDto therapyDoseDto)
  {
    this.therapyDoseDto = therapyDoseDto;
  }

  public String getTriggersTherapyId()
  {
    return triggersTherapyId;
  }

  public void setTriggersTherapyId(final String triggersTherapyId)
  {
    this.triggersTherapyId = triggersTherapyId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("taskId", taskId)
        .append("administrationId", administrationId)
        .append("therapyId", therapyId)
        .append("plannedAdministrationTime", plannedAdministrationTime)
        .append("administrationTypeEnum", administrationTypeEnum)
        .append("therapyDoseDto", therapyDoseDto)
        .append("triggersTherapyId", triggersTherapyId);
  }
}
