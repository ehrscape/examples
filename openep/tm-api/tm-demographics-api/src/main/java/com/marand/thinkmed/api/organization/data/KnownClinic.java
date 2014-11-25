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

package com.marand.thinkmed.api.organization.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.core.data.Catalog;
import com.marand.thinkmed.api.organization.NoKnownClinicProvider;

/**
 * @author Bostjan Vester
 */
public interface KnownClinic extends Serializable, Catalog
{
  interface ValuesProvider
  {
    KnownClinic[] allValues();
  }

  class Utils
  {
    private static ValuesProvider valuesProvider = new NoKnownClinicProvider();

    private Utils()
    {
    }

    public static KnownClinic fromName(final String name)
    {
      if (name == null)
      {
        return null;
      }
      for (final KnownClinic enumValue : valuesProvider.allValues())
      {
        if (name.equals(enumValue.name()))
        {
          return enumValue;
        }
      }
      return null;
    }

    public static KnownClinic fromCode(final String code)
    {
      if (code == null)
      {
        return null;
      }
      for (final KnownClinic enumValue : valuesProvider.allValues())
      {
        if (code.equals(enumValue.code()))
        {
          return enumValue;
        }
      }
      return null;
    }

    public static KnownClinic[] allValues()
    {
      return valuesProvider.allValues();
    }

    public static void setValuesProvider(final ValuesProvider valuesProvider)
    {
      Utils.valuesProvider = Preconditions.checkNotNull(valuesProvider);
    }

    public static Iterable<String> names(final List<KnownClinic> values)
    {
      final List<String> result = new ArrayList<String>();

      for (final KnownClinic value : values)
      {
        result.add(value.name());
      }

      return result;
    }
  }
}
