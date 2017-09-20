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

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.WarningType;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class MedicationsWarningDto extends DataTransferObject
{
  private List<MedicationsWarningDto> detailedWarnings;
  private String description;
  private WarningSeverity severity;
  private WarningType type;
  private String longDescription;
  private String monographHtml;
  //private NamedIdentityDto primaryMedication;
  //private NamedIdentityDto secondaryMedication;

  private List<NamedExternalDto> medications = new ArrayList<>();

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

  public WarningSeverity getSeverity()
  {
    return severity;
  }

  public void setSeverity(final WarningSeverity severity)
  {
    this.severity = severity;
  }

  public WarningType getType()
  {
    return type;
  }

  public void setType(final WarningType type)
  {
    this.type = type;
  }

  public String getLongDescription()
  {
    return longDescription;
  }

  public void setLongDescription(final String longDescription)
  {
    this.longDescription = longDescription;
  }

  public List<NamedExternalDto> getMedications()
  {
    return medications;
  }

  public void setMedications(final List<NamedExternalDto> medications)
  {
    this.medications = medications;
  }

  public String getMonographHtml()
  {
    return monographHtml;
  }

  public void setMonographHtml(final String monographHtml)
  {
    this.monographHtml = monographHtml;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("description", description)
        .append("severity", severity)
        .append("type", type)
        .append("detailedWarnings", detailedWarnings)
        .append("longDescription", longDescription)
        .append("medications", medications)
    //.append("monographHtml", monographHtml)
    ;
  }

  public void addDetail(final MedicationsWarningDto warning)
  {
    if (detailedWarnings == null)
    {
      detailedWarnings = new ArrayList<>();
    }
    detailedWarnings.add(warning);
  }
}
