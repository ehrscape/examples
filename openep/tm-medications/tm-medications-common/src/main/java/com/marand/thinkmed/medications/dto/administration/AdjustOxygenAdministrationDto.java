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

import com.marand.thinkmed.medications.dto.OxygenStartingDevice;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class AdjustOxygenAdministrationDto extends AdjustInfusionAdministrationDto implements OxygenAdministration
{
  private OxygenStartingDevice plannedStartingDevice;
  private OxygenStartingDevice startingDevice;

  public AdjustOxygenAdministrationDto()
  {
    super(AdjustAdministrationSubtype.OXYGEN);
  }

  @Override
  public OxygenStartingDevice getPlannedStartingDevice()
  {
    return plannedStartingDevice;
  }

  @Override
  public void setPlannedStartingDevice(final OxygenStartingDevice plannedStartingDevice)
  {
    this.plannedStartingDevice = plannedStartingDevice;
  }

  @Override
  public OxygenStartingDevice getStartingDevice()
  {
    return startingDevice;
  }

  @Override
  public void setStartingDevice(final OxygenStartingDevice startingDevice)
  {
    this.startingDevice = startingDevice;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("plannedStartingDevice", plannedStartingDevice)
        .append("startingDevice", startingDevice)
    ;
  }
}
