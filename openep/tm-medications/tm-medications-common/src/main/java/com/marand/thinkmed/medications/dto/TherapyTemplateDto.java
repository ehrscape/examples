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

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.maf.core.data.object.VersionalIdentityDto;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyTemplateDto extends VersionalIdentityDto implements JsonSerializable
{
  private String name;
  private TherapyTemplateTypeEnum type;
  private String userId;
  private String careProviderId;
  private String patientId;
  private List<TherapyTemplateElementDto> templateElements = new ArrayList<>();

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public TherapyTemplateTypeEnum getType()
  {
    return type;
  }

  public void setType(final TherapyTemplateTypeEnum type)
  {
    this.type = type;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(final String userId)
  {
    this.userId = userId;
  }

  public String getCareProviderId()
  {
    return careProviderId;
  }

  public void setCareProviderId(final String careProviderId)
  {
    this.careProviderId = careProviderId;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public void setPatientId(final String patientId)
  {
    this.patientId = patientId;
  }

  public List<TherapyTemplateElementDto> getTemplateElements()
  {
    return templateElements;
  }

  public void setTemplateElements(final List<TherapyTemplateElementDto> templateElements)
  {
    this.templateElements = templateElements;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("name", name)
        .append("type", type)
        .append("userId", userId)
        .append("careProviderId", careProviderId)
        .append("patientId", patientId)
        .append("templateElements", templateElements);
  }
}
