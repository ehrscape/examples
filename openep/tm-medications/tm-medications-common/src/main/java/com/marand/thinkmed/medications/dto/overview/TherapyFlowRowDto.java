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

package com.marand.thinkmed.medications.dto.overview;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */

public class TherapyFlowRowDto extends DataTransferObject implements JsonSerializable
{
  private String atcGroupName;
  private String atcGroupCode;
  private List<String> routes = new ArrayList<>();
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

  public List<String> getRoutes()
  {
    return routes;
  }

  public void setRoutes(final List<String> routes)
  {
    this.routes = routes;
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
        .append("routes", routes)
        .append("customGroup", customGroup)
        .append("therapyFlowDayMap", therapyFlowDayMap)
        .append("customGroupSortOrder", customGroupSortOrder)
    ;
  }
}
