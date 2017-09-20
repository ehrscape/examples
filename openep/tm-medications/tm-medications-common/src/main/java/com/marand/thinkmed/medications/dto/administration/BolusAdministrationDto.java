/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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
 * @author Nejc Korasa
 */

public class BolusAdministrationDto extends AdministrationDto implements DoseAdministration
{
  private TherapyDoseDto administeredDose;

  public BolusAdministrationDto()
  {
    super(AdministrationTypeEnum.BOLUS);
  }

  @Override
  public TherapyDoseDto getAdministeredDose()
  {
    return administeredDose;
  }

  @Override
  public void setAdministeredDose(final TherapyDoseDto administeredDose)
  {
    this.administeredDose = administeredDose;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("administeredDose", administeredDose);
  }
}
