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

package com.marand.thinkmed.medications.dto.change;

import static com.marand.thinkmed.medications.dto.change.TherapyChangeType.TherapyChangeLevel.INSIGNIFICANT;
import static com.marand.thinkmed.medications.dto.change.TherapyChangeType.TherapyChangeLevel.SIGNIFICANT;

/**
 * @author Igor Horvat
 * @author Mitja Lapajne
 */

public enum TherapyChangeType
{
  MEDICATION(StringTherapyChangeDto.class, SIGNIFICANT),
  ROUTE(StringsTherapyChangeDto.class, SIGNIFICANT),
  VARIABLE_DOSE(VariableDoseTherapyChangeDto.class, SIGNIFICANT),
  VARIABLE_DOSE_TO_DOSE(VariableDoseToDoseTherapyChangeDto.class, SIGNIFICANT),
  DOSE_TO_VARIABLE_DOSE(DoseToVariableDoseTherapyChangeDto.class, SIGNIFICANT),
  VARIABLE_RATE(VariableRateTherapyChangeDto.class, SIGNIFICANT),
  VARIABLE_RATE_TO_RATE(VariableRateToRateTherapyChangeDto.class, SIGNIFICANT),
  RATE_TO_VARIABLE_RATE(RateToVariableRateTherapyChangeDto.class, SIGNIFICANT),
  DOSE(StringTherapyChangeDto.class, SIGNIFICANT),
  DOSE_INTERVAL(StringTherapyChangeDto.class, SIGNIFICANT),
  DOSE_TIMES(StringTherapyChangeDto.class, SIGNIFICANT),
  RATE(StringTherapyChangeDto.class, SIGNIFICANT),
  INFUSION_DURATION(StringTherapyChangeDto.class, SIGNIFICANT),
  ADDITIONAL_CONDITIONS(StringTherapyChangeDto.class, SIGNIFICANT),
  WHEN_NEEDED(StringTherapyChangeDto.class, SIGNIFICANT),
  MAX_DOSES(StringTherapyChangeDto.class, SIGNIFICANT),
  DOCTOR_ORDERS(StringTherapyChangeDto.class, SIGNIFICANT),
  COMMENT(StringTherapyChangeDto.class, INSIGNIFICANT),
  INDICATION(StringTherapyChangeDto.class, INSIGNIFICANT),
  START(StringTherapyChangeDto.class, INSIGNIFICANT),
  END(StringTherapyChangeDto.class, SIGNIFICANT),

  //Oxygen
  DEVICE(StringTherapyChangeDto.class, SIGNIFICANT),
  SATURATION(StringTherapyChangeDto.class, SIGNIFICANT);

  private final Class<? extends TherapyChangeDto<?, ?>> dtoClass;
  private final TherapyChangeLevel level;

  TherapyChangeType(final Class<? extends TherapyChangeDto<?, ?>> dtoClass, final TherapyChangeLevel level)
  {
    this.dtoClass = dtoClass;
    this.level = level;
  }

  public Class<? extends TherapyChangeDto<?, ?>> getDtoClass()
  {
    return dtoClass;
  }

  public TherapyChangeLevel getLevel()
  {
    return level;
  }

  public enum TherapyChangeLevel
  {
    SIGNIFICANT,
    INSIGNIFICANT
  }
}
