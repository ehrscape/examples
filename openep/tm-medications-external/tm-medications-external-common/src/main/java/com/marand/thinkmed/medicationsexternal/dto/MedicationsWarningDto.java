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

package com.marand.thinkmed.medicationsexternal.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.data.object.DataObject;
import com.marand.maf.core.data.object.NamedIdentityDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class MedicationsWarningDto extends DataObject
{
  private List<MedicationsWarningDto> detailedWarnings;
  private String description;
  private Severity severity;
  private String longDescription;
  private NamedIdentityDto primaryMedication;
  private NamedIdentityDto secondaryMedication;

  public String getDescription()
  {
    return description;
  }

  public void setDescription(final String description)
  {
    this.description = description;
  }

  public List<MedicationsWarningDto> getDetailedWarnings()
  {
    return detailedWarnings;
  }

  public void setDetailedWarnings(final List<MedicationsWarningDto> detailedWarnings)
  {
    this.detailedWarnings = detailedWarnings;
  }

  public Severity getSeverity()
  {
    return severity;
  }

  public void setSeverity(final Severity severity)
  {
    this.severity = severity;
  }

  public String getLongDescription()
  {
    return longDescription;
  }

  public void setLongDescription(final String longDescription)
  {
    this.longDescription = longDescription;
  }

  public NamedIdentityDto getPrimaryMedication()
  {
    return primaryMedication;
  }

  public void setPrimaryMedication(final NamedIdentityDto primaryMedication)
  {
    this.primaryMedication = primaryMedication;
  }

  public NamedIdentityDto getSecondaryMedication()
  {
    return secondaryMedication;
  }

  public void setSecondaryMedication(final NamedIdentityDto secondaryMedication)
  {
    this.secondaryMedication = secondaryMedication;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("description", description)
        .append("severity", severity)
        .append("detailedWarnings", detailedWarnings)
        .append("longDescription", longDescription)
        .append("primaryMedication", primaryMedication)
        .append("secondaryMedication", secondaryMedication)
        ;
  }

  public void addDetail(final MedicationsWarningDto warning)
  {
    if (detailedWarnings == null)
    {
      detailedWarnings = new ArrayList<MedicationsWarningDto>();
    }
    detailedWarnings.add(warning);
  }

  public enum Severity
  {
    LOW, MEDIUM, SIGNIFICANT, HIGH
  }
}
