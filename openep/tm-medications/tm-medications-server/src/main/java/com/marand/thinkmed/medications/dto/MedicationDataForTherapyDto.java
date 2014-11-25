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

import com.marand.maf.core.data.object.DataObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class MedicationDataForTherapyDto extends DataObject
{
  private String genericName;
  private String atcCode;
  private String atcName;
  private String customGroupName;
  private Integer customGroupSortOrder;
  private boolean isAntibiotic;

  public String getGenericName()
  {
    return genericName;
  }

  public void setGenericName(final String genericName)
  {
    this.genericName = genericName;
  }

  public String getAtcCode()
  {
    return atcCode;
  }

  public void setAtcCode(final String atcCode)
  {
    this.atcCode = atcCode;
  }

  public String getAtcName()
  {
    return atcName;
  }

  public void setAtcName(final String atcName)
  {
    this.atcName = atcName;
  }

  public String getCustomGroupName()
  {
    return customGroupName;
  }

  public void setCustomGroupName(final String customGroupName)
  {
    this.customGroupName = customGroupName;
  }

  public boolean isAntibiotic()
  {
    return isAntibiotic;
  }

  public void setAntibiotic(final boolean antibiotic)
  {
    isAntibiotic = antibiotic;
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
        .append("genericName", genericName)
        .append("atcCode", atcCode)
        .append("atcName", atcName)
        .append("customGroupName", customGroupName)
        .append("customGroupSortOrder", customGroupSortOrder)
        .append("isAntibiotic", isAntibiotic)
    ;
  }
}
