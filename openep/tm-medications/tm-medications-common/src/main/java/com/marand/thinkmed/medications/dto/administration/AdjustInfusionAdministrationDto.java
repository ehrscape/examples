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

package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class AdjustInfusionAdministrationDto extends AdministrationDto
{
  private TherapyDoseDto administeredDose;
  private TherapyDoseDto plannedDose;
  private boolean differentFromOrder;

  public AdjustInfusionAdministrationDto()
  {
    super(AdministrationTypeEnum.ADJUST_INFUSION);
  }

  public TherapyDoseDto getAdministeredDose()
  {
    return administeredDose;
  }

  public void setAdministeredDose(final TherapyDoseDto administeredDose)
  {
    this.administeredDose = administeredDose;
  }

  public TherapyDoseDto getPlannedDose()
  {
    return plannedDose;
  }

  public void setPlannedDose(final TherapyDoseDto plannedDose)
  {
    this.plannedDose = plannedDose;
  }

  public boolean isDifferentFromOrder()
  {
    return differentFromOrder;
  }

  public void setDifferentFromOrder(final boolean differentFromOrder)
  {
    this.differentFromOrder = differentFromOrder;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("administeredDose", administeredDose)
        .append("plannedDose", plannedDose)
        .append("differentFromOrder", differentFromOrder);
  }
}
