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

package com.marand.thinkmed.medications;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Mitja Lapajne
 */
public enum AdministrationTypeEnum
{
  START, STOP, ADJUST_INFUSION, INFUSION_SET_CHANGE, BOLUS;

  public static final Set<AdministrationTypeEnum> MEDICATION_ADMINISTRATION = EnumSet.of(START, STOP, ADJUST_INFUSION, BOLUS);
  public static final Set<AdministrationTypeEnum> START_OR_ADJUST = EnumSet.of(START, ADJUST_INFUSION);
  public static final Set<AdministrationTypeEnum> NOT_STOP = EnumSet.of(START, ADJUST_INFUSION, INFUSION_SET_CHANGE, BOLUS);

  public static String getFullString(final AdministrationTypeEnum additionalInstructionEnum)
  {
    return AdministrationTypeEnum.class.getSimpleName() + '.' + additionalInstructionEnum.name();
  }

  public static AdministrationTypeEnum getByFullString(final String fullString)
  {
    for (final AdministrationTypeEnum additionalInstructionEnum : values())
    {
      if (getFullString(additionalInstructionEnum).equals(fullString))
      {
        return additionalInstructionEnum;
      }
    }
    return null;
  }
}
