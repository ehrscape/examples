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

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.JsonSerializable;
import com.marand.thinkmed.medications.dto.TherapyDayDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyTimelineRowDto extends TherapyDayDto implements JsonSerializable
{
  private String therapyId;
  private String atcGroupName;
  private String atcGroupCode;
  private String customGroup;
  private Integer customGroupSortOrder;
  private List<AdministrationDto> administrations = new ArrayList<>();

  public String getTherapyId()
  {
    return therapyId;
  }

  public void setTherapyId(final String therapyId)
  {
    this.therapyId = therapyId;
  }

  public String getAtcGroupName()
  {
    return atcGroupName;
  }

  public void setAtcGroupName(final String atcGroupName)
  {
    this.atcGroupName = atcGroupName;
  }

  public String getAtcGroupCode()
  {
    return atcGroupCode;
  }

  public void setAtcGroupCode(final String atcGroupCode)
  {
    this.atcGroupCode = atcGroupCode;
  }

  public String getCustomGroup()
  {
    return customGroup;
  }

  public void setCustomGroup(final String customGroup)
  {
    this.customGroup = customGroup;
  }

  public Integer getCustomGroupSortOrder()
  {
    return customGroupSortOrder;
  }

  public void setCustomGroupSortOrder(final Integer customGroupSortOrder)
  {
    this.customGroupSortOrder = customGroupSortOrder;
  }

  public List<AdministrationDto> getAdministrations()
  {
    return administrations;
  }

  public void setAdministrations(final List<AdministrationDto> administrations)
  {
    this.administrations = administrations;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("therapyId", therapyId)
        .append("atcGroupName", atcGroupName)
        .append("atcGroupCode", atcGroupCode)
        .append("customGroup", customGroup)
        .append("customGroupSortOrder", customGroupSortOrder)
        .append("administrations", administrations);
  }
}
