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

package com.marand.thinkmed.medications.activity;

import com.marand.maf.core.activity.KnownActivityType;
import com.marand.maf.core.activity.model.ActivityType;

/**
 * @author Bostjan Vester
 */
public interface MedicationsActivityConstants
{
  enum ActivityTypeEnum implements KnownActivityType
  {
    MODIFY_THERAPY("modify.therapy");

    private final String descriptionId;

    ActivityTypeEnum(final String descriptionId)
    {
      this.descriptionId = descriptionId;
    }

    @Override
    public Class<ActivityType> getCatalogType()
    {
      return ActivityType.class;
    }

    @Override
    public String getCode()
    {
      return name();
    }

    @Override
    public String getDescriptionId()
    {
      return descriptionId;
    }
  }

  interface ActivityStreamDescription
  {
    String MODIFY_THERAPY = "TherapyUseCase.MODIFY_THERAPY";
  }
}
