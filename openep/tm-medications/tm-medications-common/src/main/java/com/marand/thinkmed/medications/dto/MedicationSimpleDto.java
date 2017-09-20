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

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class MedicationSimpleDto extends DataTransferObject implements JsonSerializable
{
  private long id;
  private String name;
  private String genericName;
  private boolean active;
  private boolean outpatientMedication;
  private boolean inpatientMedication;

  public long getId()
  {
    return id;
  }

  public void setId(final long id)
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

  public String getGenericName()
  {
    return genericName;
  }

  public void setGenericName(final String genericName)
  {
    this.genericName = genericName;
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(final boolean active)
  {
    this.active = active;
  }

  public boolean isOutpatientMedication()
  {
    return outpatientMedication;
  }

  public void setOutpatientMedication(final boolean outpatientMedication)
  {
    this.outpatientMedication = outpatientMedication;
  }

  public boolean isInpatientMedication()
  {
    return inpatientMedication;
  }

  public void setInpatientMedication(final boolean inpatientMedication)
  {
    this.inpatientMedication = inpatientMedication;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("id", id)
        .append("name", name)
        .append("genericName", genericName)
        .append("active", active)
        .append("inpatientMedication", inpatientMedication)
        .append("outpatientMedication", outpatientMedication)
    ;
  }
}
