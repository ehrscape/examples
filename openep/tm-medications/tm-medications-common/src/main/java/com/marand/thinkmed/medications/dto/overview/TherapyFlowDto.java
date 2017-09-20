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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyFlowDto extends DataTransferObject
{
  private List<TherapyFlowRowDto> therapyRows = new ArrayList<>();
  private Map<Integer, Double> referenceWeightsDayMap = new HashMap<>();

  public List<TherapyFlowRowDto> getTherapyRows()
  {
    return therapyRows;
  }

  public void setTherapyRows(final List<TherapyFlowRowDto> therapyRows)
  {
    this.therapyRows = therapyRows;
  }

  public Map<Integer, Double> getReferenceWeightsDayMap()
  {
    return referenceWeightsDayMap;
  }

  public void setReferenceWeightsDayMap(final Map<Integer, Double> referenceWeightsDayMap)
  {
    this.referenceWeightsDayMap = referenceWeightsDayMap;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapyRows", therapyRows)
        .append("referenceWeightsDayMap", referenceWeightsDayMap)
    ;
  }
}
