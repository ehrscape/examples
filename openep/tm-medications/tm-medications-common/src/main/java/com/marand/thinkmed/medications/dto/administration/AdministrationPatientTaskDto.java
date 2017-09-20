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

import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class AdministrationPatientTaskDto extends PatientTaskDto
{
  private DateTime plannedTime;
  private TherapyDoseDto plannedDose;
  private AdministrationStatusEnum administrationStatus;
  private TherapyDayDto therapyDayDto;
  private TherapyActionHistoryDto therapyChange;
  private String roomAndBed;

  public AdministrationPatientTaskDto()
  {
    setTaskType(TaskTypeEnum.ADMINISTRATION_TASK);
  }

  public DateTime getPlannedTime()
  {
    return plannedTime;
  }

  public void setPlannedTime(final DateTime plannedTime)
  {
    this.plannedTime = plannedTime;
  }

  public TherapyDoseDto getPlannedDose()
  {
    return plannedDose;
  }

  public void setPlannedDose(final TherapyDoseDto plannedDose)
  {
    this.plannedDose = plannedDose;
  }

  public AdministrationStatusEnum getAdministrationStatus()
  {
    return administrationStatus;
  }

  public void setAdministrationStatus(final AdministrationStatusEnum administrationStatus)
  {
    this.administrationStatus = administrationStatus;
  }

  public TherapyDayDto getTherapyDayDto()
  {
    return therapyDayDto;
  }

  public void setTherapyDayDto(final TherapyDayDto therapyDayDto)
  {
    this.therapyDayDto = therapyDayDto;
  }

  public String getRoomAndBed()
  {
    return roomAndBed;
  }

  public void setRoomAndBed(final String roomAndBed)
  {
    this.roomAndBed = roomAndBed;
  }

  public TherapyActionHistoryDto getTherapyChange()
  {
    return therapyChange;
  }

  public void setTherapyChange(final TherapyActionHistoryDto therapyChange)
  {
    this.therapyChange = therapyChange;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("plannedTime", plannedTime)
        .append("plannedDose", plannedDose)
        .append("administrationStatus", administrationStatus)
        .append("therapyDayDto", therapyDayDto)
        .append("roomAndBed", roomAndBed)
        .append("therapyChange", therapyChange)
    ;
  }
}
