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

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */
public class WarningsProviderDto extends DataTransferObject
{
  private String externalSystem;
  private boolean requiresDiseaseCodesTranslation;

  public String getExternalSystem()
  {
    return externalSystem;
  }

  public void setExternalSystem(final String externalSystem)
  {
    this.externalSystem = externalSystem;
  }

  public boolean isRequiresDiseaseCodesTranslation()
  {
    return requiresDiseaseCodesTranslation;
  }

  public void setRequiresDiseaseCodesTranslation(final boolean requiresDiseaseCodesTranslation)
  {
    this.requiresDiseaseCodesTranslation = requiresDiseaseCodesTranslation;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("externalSystem", externalSystem)
        .append("requiresDiseaseCodesTranslation", requiresDiseaseCodesTranslation);
  }
}
