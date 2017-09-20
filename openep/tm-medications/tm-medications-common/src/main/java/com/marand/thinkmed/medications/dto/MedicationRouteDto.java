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

import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.medications.MedicationRouteTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class MedicationRouteDto extends NamedIdentityDto implements JsonSerializable
{
  private MedicationRouteTypeEnum type;
  private boolean unlicensedRoute;
  private BnfMaximumDto bnfMaximumDto;
  private boolean discretionary;

  public MedicationRouteTypeEnum getType()
  {
    return type;
  }

  public void setType(final MedicationRouteTypeEnum type)
  {
    this.type = type;
  }

  public boolean isUnlicensedRoute()
  {
    return unlicensedRoute;
  }

  public void setUnlicensedRoute(final boolean unlicensedRoute)
  {
    this.unlicensedRoute = unlicensedRoute;
  }

  public BnfMaximumDto getBnfMaximumDto()
  {
    return bnfMaximumDto;
  }

  public void setBnfMaximumDto(final BnfMaximumDto bnfMaximumDto)
  {
    this.bnfMaximumDto = bnfMaximumDto;
  }

  public boolean isDiscretionary()
  {
    return discretionary;
  }

  public void setDiscretionary(final boolean discretionary)
  {
    this.discretionary = discretionary;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("type", type)
        .append("unlicensedRoute", unlicensedRoute)
        .append("bnfMaximumDto", bnfMaximumDto)
        .append("discretionary", discretionary)
    ;
  }
}
