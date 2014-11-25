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

import com.marand.maf.core.JsonSerializable;
import com.marand.maf.core.data.object.DataObject;
import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public abstract class AdministrationDto extends DataObject implements JsonSerializable
{
  private final AdministrationTypeEnum administrationType;
  private AdministrationStatusEnum administrationStatus;
  private DateTime administrationTime;
  private DateTime plannedTime;
  private boolean additionalAdministration;
  private String administrationId; //composition uid
  private String taskId; //activity task id
  private String therapyId;
  private String composerName;
  private String comment;
  private String triggersTherapyId;

  protected AdministrationDto(final AdministrationTypeEnum administrationType)
  {
    this.administrationType = administrationType;
  }

  public AdministrationTypeEnum getAdministrationType()
  {
    return administrationType;
  }

  public AdministrationStatusEnum getAdministrationStatus()
  {
    return administrationStatus;
  }

  public void setAdministrationStatus(final AdministrationStatusEnum administrationStatus)
  {
    this.administrationStatus = administrationStatus;
  }

  public DateTime getAdministrationTime()
  {
    return administrationTime;
  }

  public void setAdministrationTime(final DateTime administrationTime)
  {
    this.administrationTime = administrationTime;
  }

  public DateTime getPlannedTime()
  {
    return plannedTime;
  }

  public void setPlannedTime(final DateTime plannedTime)
  {
    this.plannedTime = plannedTime;
  }

  public boolean isAdditionalAdministration()
  {
    return additionalAdministration;
  }

  public void setAdditionalAdministration(final boolean additionalAdministration)
  {
    this.additionalAdministration = additionalAdministration;
  }

  public String getAdministrationId()
  {
    return administrationId;
  }

  public void setAdministrationId(final String administrationId)
  {
    this.administrationId = administrationId;
  }

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(final String taskId)
  {
    this.taskId = taskId;
  }

  public String getTherapyId()
  {
    return therapyId;
  }

  public void setTherapyId(final String therapyId)
  {
    this.therapyId = therapyId;
  }

  public String getComposerName()
  {
    return composerName;
  }

  public void setComposerName(final String composerName)
  {
    this.composerName = composerName;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
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
        .append("administrationType", administrationType)
        .append("administrationStatus", administrationStatus)
        .append("administrationTime", administrationTime)
        .append("plannedTime", plannedTime)
        .append("additionalAdministration", additionalAdministration)
        .append("administrationId", administrationId)
        .append("taskId", taskId)
        .append("therapyId", therapyId)
        .append("composerName", composerName)
        .append("comment", comment)
        .append("triggersTherapyId", triggersTherapyId)
    ;
  }
}
