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
import com.marand.maf.core.JsonSerializable;
import com.marand.thinkmed.medications.MedicationTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class MedicationDto extends DataObject implements JsonSerializable
{
  private Long id;
  private String name;
  private String shortName;
  private String genericName;
  private MedicationTypeEnum medicationType;

  private String displayName;

  public Long getId()
  {
    return id;
  }

  public void setId(final Long id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getShortName()
  {
    return shortName;
  }

  public void setShortName(final String shortName)
  {
    this.shortName = shortName;
  }

  public String getGenericName()
  {
    return genericName;
  }

  public void setGenericName(final String genericName)
  {
    this.genericName = genericName;
  }

  public MedicationTypeEnum getMedicationType()
  {
    return medicationType;
  }

  public void setMedicationType(final MedicationTypeEnum medicationType)
  {
    this.medicationType = medicationType;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setDisplayName(final String displayName)
  {
    this.displayName = displayName;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("id", id)
        .append("name", name)
        .append("shortName", shortName)
        .append("genericName", genericName)
        .append("medicationType", medicationType)
        .append("displayName", displayName);
  }
}
