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

import com.marand.maf.core.JsonSerializable;
import com.marand.maf.core.data.object.DataObject;
import com.marand.thinkmed.api.medical.data.Care;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */

public class MedicationsCentralCaseDto extends DataObject implements JsonSerializable
{
  private Care care;
  private Long centralCaseId;
  private Interval centralCaseEffective;
  private Long encounterId;
  private Long episodeId;
  private Long sessionId;
  private Long careProviderId;
  private Long organizationalEntityId;
  private String organizationalEntityName;
  private String departmentName;
  private boolean recentHospitalization;

  public Care getCare()
  {
    return care;
  }

  public void setCare(final Care care)
  {
    this.care = care;
  }

  public Long getCentralCaseId()
  {
    return centralCaseId;
  }

  public void setCentralCaseId(final Long centralCaseId)
  {
    this.centralCaseId = centralCaseId;
  }

  public Interval getCentralCaseEffective()
  {
    return centralCaseEffective;
  }

  public void setCentralCaseEffective(final Interval centralCaseEffective)
  {
    this.centralCaseEffective = centralCaseEffective;
  }

  public Long getEncounterId()
  {
    return encounterId;
  }

  public void setEncounterId(final Long encounterId)
  {
    this.encounterId = encounterId;
  }

  public Long getEpisodeId()
  {
    return episodeId;
  }

  public void setEpisodeId(final Long episodeId)
  {
    this.episodeId = episodeId;
  }

  public Long getSessionId()
  {
    return sessionId;
  }

  public void setSessionId(final Long sessionId)
  {
    this.sessionId = sessionId;
  }

  public Long getCareProviderId()
  {
    return careProviderId;
  }

  public void setCareProviderId(final Long careProviderId)
  {
    this.careProviderId = careProviderId;
  }

  public Long getOrganizationalEntityId()
  {
    return organizationalEntityId;
  }

  public void setOrganizationalEntityId(final Long organizationalEntityId)
  {
    this.organizationalEntityId = organizationalEntityId;
  }

  public String getOrganizationalEntityName()
  {
    return organizationalEntityName;
  }

  public void setOrganizationalEntityName(final String organizationalEntityName)
  {
    this.organizationalEntityName = organizationalEntityName;
  }

  public String getDepartmentName()
  {
    return departmentName;
  }

  public void setDepartmentName(final String departmentName)
  {
    this.departmentName = departmentName;
  }

  public boolean isRecentHospitalization()
  {
    return recentHospitalization;
  }

  public void setRecentHospitalization(final boolean recentHospitalization)
  {
    this.recentHospitalization = recentHospitalization;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("care", care)
        .append("centralCaseId", centralCaseId)
        .append("centralCaseEffective", centralCaseEffective)
        .append("encounterId", encounterId)
        .append("episodeId", episodeId)
        .append("sessionId", sessionId)
        .append("careProviderId", careProviderId)
        .append("organizationalEntityId", organizationalEntityId)
        .append("organizationalEntityName", organizationalEntityName)
        .append("departmentName", departmentName)
        .append("recentHospitalization", recentHospitalization);
  }
}
