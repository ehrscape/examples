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

package com.marand.thinkmed.api.demographics;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Bostjan Vester
 */
public final class DemographicsUtils
{
  private DemographicsUtils()
  {
  }

  public static String calculateName(final String name1, final String name2, final String nameHyphen)
  {
    final StringBuffer sBuf = new StringBuffer();

    if (StringUtils.isNotBlank(name1))
    {
      sBuf.append(name1);
    }
    if (StringUtils.isNotBlank(name2))
    {
      if (sBuf.length() != 0)
      {
        sBuf.append(' ');
      }
      if (StringUtils.isNotBlank(nameHyphen))
      {
        sBuf.append(nameHyphen);
        sBuf.append(' ');
      }
      sBuf.append(name2);
    }

    return sBuf.toString();
  }
}
