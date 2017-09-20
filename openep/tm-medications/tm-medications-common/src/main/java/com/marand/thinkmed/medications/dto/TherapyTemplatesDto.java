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
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyTemplatesDto extends DataTransferObject implements JsonSerializable
{
  private List<TherapyTemplateDto> userTemplates = new ArrayList<>();
  private List<TherapyTemplateDto> organizationTemplates = new ArrayList<>();
  private List<TherapyTemplateDto> patientTemplates = new ArrayList<>();

  public List<TherapyTemplateDto> getUserTemplates()
  {
    return userTemplates;
  }

  public void setUserTemplates(final List<TherapyTemplateDto> userTemplates)
  {
    this.userTemplates = userTemplates;
  }

  public List<TherapyTemplateDto> getOrganizationTemplates()
  {
    return organizationTemplates;
  }

  public void setOrganizationTemplates(final List<TherapyTemplateDto> organizationTemplates)
  {
    this.organizationTemplates = organizationTemplates;
  }

  public List<TherapyTemplateDto> getPatientTemplates()
  {
    return patientTemplates;
  }

  public void setPatientTemplates(final List<TherapyTemplateDto> patientTemplates)
  {
    this.patientTemplates = patientTemplates;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("userTemplates", userTemplates)
        .append("organizationTemplates", organizationTemplates)
        .append("patientTemplates", patientTemplates)
    ;
  }
}
