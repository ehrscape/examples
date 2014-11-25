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

import java.util.LinkedHashMap;
import java.util.Map;

import com.marand.maf.core.data.object.DataObject;
import com.marand.maf.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */

public class TherapyFlowRowDto extends DataObject implements JsonSerializable
{
  private String atcGroupName;
  private String atcGroupCode;
  private String route;
  private String customGroup;
  private Integer customGroupSortOrder;
  private Map<Integer, TherapyDayDto> therapyFlowDayMap = new LinkedHashMap<>();

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

  public String getRoute()
  {
    return route;
  }

  public void setRoute(final String route)
  {
    this.route = route;
  }

  public String getCustomGroup()
  {
    return customGroup;
  }

  public void setCustomGroup(final String customGroup)
  {
    this.customGroup = customGroup;
  }

  public Map<Integer, TherapyDayDto> getTherapyFlowDayMap()
  {
    return therapyFlowDayMap;
  }

  public void setTherapyFlowDayMap(final Map<Integer, TherapyDayDto> therapyFlowDayMap)
  {
    this.therapyFlowDayMap = therapyFlowDayMap;
  }

  public Integer getCustomGroupSortOrder()
  {
    return customGroupSortOrder;
  }

  public void setCustomGroupSortOrder(final Integer customGroupSortOrder)
  {
    this.customGroupSortOrder = customGroupSortOrder;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("atcGroupName", atcGroupName)
        .append("atcGroupCode", atcGroupCode)
        .append("route", route)
        .append("customGroup", customGroup)
        .append("therapyFlowDayMap", therapyFlowDayMap)
        .append("customGroupSortOrder", customGroupSortOrder)
    ;
  }
}
