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

package com.marand.thinkmed.medications.dto.prescription;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentContent;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class PrescriptionPackageDto extends DataTransferObject implements JsonSerializable, TherapyDocumentContent
{
  private String prescriptionPackageId;
  private String compositionUid;
  private List<PrescriptionTherapyDto> prescriptionTherapies = new ArrayList<>();
  private NamedExternalDto composer;
  private DateTime lastUpdateTimestamp;

  @Override
  public String getContentId()
  {
    return compositionUid;
  }

  public String getPrescriptionPackageId()
  {
    return prescriptionPackageId;
  }

  public void setPrescriptionPackageId(final String prescriptionPackageId)
  {
    this.prescriptionPackageId = prescriptionPackageId;
  }

  public String getCompositionUid()
  {
    return compositionUid;
  }

  public void setCompositionUid(final String compositionUid)
  {
    this.compositionUid = compositionUid;
  }

  public List<PrescriptionTherapyDto> getPrescriptionTherapies()
  {
    return prescriptionTherapies;
  }

  public void setPrescriptionTherapies(final List<PrescriptionTherapyDto> prescriptionTherapies)
  {
    this.prescriptionTherapies = prescriptionTherapies;
  }

  public NamedExternalDto getComposer()
  {
    return composer;
  }

  public void setComposer(final NamedExternalDto composer)
  {
    this.composer = composer;
  }

  public DateTime getLastUpdateTimestamp()
  {
    return lastUpdateTimestamp;
  }

  public void setLastUpdateTimestamp(final DateTime lastUpdateTimestamp)
  {
    this.lastUpdateTimestamp = lastUpdateTimestamp;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("prescriptionPackageId", prescriptionPackageId)
        .append("compositionUid", compositionUid)
        .append("prescriptionTherapies", prescriptionTherapies)
        .append("composer", composer)
        .append("lastUpdateTimestamp", lastUpdateTimestamp)
    ;
  }
}
