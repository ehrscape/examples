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

import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.prefs.AbstractPreference;
import com.marand.maf.core.prefs.MafPrefsStorageType;
import com.marand.maf.core.prefs.ObjectStringConverter;
import com.marand.thinkmed.medications.dto.AdministrationTaskLimitsDto;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Mitja Lapajne
 */
public class AdministrationTaskLimitsPreference extends AbstractPreference<AdministrationTaskLimitsDto>
{
  public AdministrationTaskLimitsPreference(
      final String key,
      final MafPrefsStorageType storageType,
      final boolean required,
      final AdministrationTaskLimitsDto defaultValue)
  {
    super(key, storageType, required, new AdministrationTaskLimitsConverter(), defaultValue);
  }

  private static class AdministrationTaskLimitsConverter implements ObjectStringConverter<AdministrationTaskLimitsDto>
  {
    @Override
    public AdministrationTaskLimitsDto convertFromString(final String stringValue)
    {
      return StringUtils.isBlank(stringValue) ? null : JsonUtil.fromJson(stringValue, AdministrationTaskLimitsDto.class);
    }

    @Override
    public String convertToString(final AdministrationTaskLimitsDto value)
    {
      return value == null ? "" : JsonUtil.toJson(value);
    }
  }
}