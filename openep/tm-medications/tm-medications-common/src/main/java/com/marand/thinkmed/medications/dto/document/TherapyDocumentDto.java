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

package com.marand.thinkmed.medications.dto.document;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class TherapyDocumentDto extends DataTransferObject
{
  private TherapyDocumentType documentType;
  private DateTime createTimestamp; //prescriptionDate
  private NamedExternalDto creator;
  private NamedExternalDto careProvider;
  private TherapyDocumentContent content;

  public TherapyDocumentType getDocumentType()
  {
    return documentType;
  }

  public void setDocumentType(final TherapyDocumentType documentType)
  {
    this.documentType = documentType;
  }

  public DateTime getCreateTimestamp()
  {
    return createTimestamp;
  }

  public void setCreateTimestamp(final DateTime createTimestamp)
  {
    this.createTimestamp = createTimestamp;
  }

  public NamedExternalDto getCreator()
  {
    return creator;
  }

  public void setCreator(final NamedExternalDto creator)
  {
    this.creator = creator;
  }

  public NamedExternalDto getCareProvider()
  {
    return careProvider;
  }

  public void setCareProvider(final NamedExternalDto careProvider)
  {
    this.careProvider = careProvider;
  }

  public TherapyDocumentContent getContent()
  {
    return content;
  }

  public void setContent(final TherapyDocumentContent content)
  {
    this.content = content;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("documentType", documentType)
        .append("createTimestamp", createTimestamp)
        .append("creator", creator)
        .append("careProvider", careProvider)
        .append("content", content)
    ;
  }
}
