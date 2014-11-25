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

package com.marand.thinkmed.medications.business.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mitja Lapajne
 */
public final class TherapyUnitsConverter
{
  private static final Map<String, Double> unitsMap = buildUnitsMap();

  private TherapyUnitsConverter()
  {
  }

  private static Map<String, Double> buildUnitsMap()
  {
    final Map<String, Double> map = new HashMap<>();
    map.put("l", 1.0);
    map.put("dl", 0.1);
    map.put("cl", 0.01);
    map.put("ml", 0.001);
    map.put("µl", 0.000001);
    map.put("g", 1.0);
    map.put("mg", 0.001);
    map.put("µg", 0.000001);
    map.put("ng", 0.000000001);
    map.put("d", 86400.0);
    map.put("h", 3600.0);
    map.put("min", 60.0);
    map.put("s", 1.0);
    map.put("i.e.", 1.0);
    map.put("mmol", 0.001);
    return map;
  }

  public static Double convertToUnit(final Double value, final String fromUnit, final String toUnit)
  {
    final Double fromFactor = unitsMap.get(fromUnit);
    final Double toFactor = unitsMap.get(toUnit);
    if (fromFactor == null || toFactor == null)
    {
      return null;
    }
    return value * fromFactor / toFactor;
  }
}
