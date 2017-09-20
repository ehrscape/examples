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

package com.marand.thinkmed.medications.business.util;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * @author Mitja Lapajne
 */
public final class TherapyUnitsConverter
{
  private static final Map<String, UnitConversionDo> unitsMap = buildUnitsMap();

  private TherapyUnitsConverter()
  {
  }

  private static Map<String, UnitConversionDo> buildUnitsMap()
  {
    final Map<String, UnitConversionDo> map = new HashMap<>();

    map.put("l", new UnitConversionDo(UnitType.LIQUID_UNIT, 1.0));
    map.put("litre", new UnitConversionDo(UnitType.LIQUID_UNIT, 1.0));
    map.put("dl", new UnitConversionDo(UnitType.LIQUID_UNIT, 0.1));
    map.put("cl", new UnitConversionDo(UnitType.LIQUID_UNIT, 0.01));
    map.put("ml", new UnitConversionDo(UnitType.LIQUID_UNIT, 0.001));
    map.put("mL", new UnitConversionDo(UnitType.LIQUID_UNIT, 0.001));
    map.put("µl", new UnitConversionDo(UnitType.LIQUID_UNIT, 0.000001));
    map.put("microlitre", new UnitConversionDo(UnitType.LIQUID_UNIT, 0.000001));

    map.put("g", new UnitConversionDo(UnitType.MASS_UNIT, 1.0));
    map.put("gram", new UnitConversionDo(UnitType.MASS_UNIT, 1.0));
    map.put("mg", new UnitConversionDo(UnitType.MASS_UNIT, 0.001));
    map.put("µg", new UnitConversionDo(UnitType.MASS_UNIT, 0.000001));
    map.put("microgram", new UnitConversionDo(UnitType.MASS_UNIT, 0.000001));
    map.put("ng", new UnitConversionDo(UnitType.MASS_UNIT, 0.000000001));
    map.put("nanogram", new UnitConversionDo(UnitType.MASS_UNIT, 0.000000001));

    map.put("d", new UnitConversionDo(UnitType.TIME_UNIT, 86400.0));
    map.put("h", new UnitConversionDo(UnitType.TIME_UNIT, 3600.0));
    map.put("min", new UnitConversionDo(UnitType.TIME_UNIT, 60.0));
    map.put("m", new UnitConversionDo(UnitType.TIME_UNIT, 60.0));
    map.put("s", new UnitConversionDo(UnitType.TIME_UNIT, 1.0));

    return map;
  }

  public static boolean isLiquidUnit(final String unit)
  {
    return unitsMap.get(unit) != null && unitsMap.get(unit).getType() == UnitType.LIQUID_UNIT;
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  public static boolean areUnitsCompatible(@Nonnull final String unit1, @Nonnull final String unit2)
  {
    Preconditions.checkNotNull(unit1, "unit1 must not be null");
    Preconditions.checkNotNull(unit2, "unit2 must not be null");

    if (unit1.equals(unit2))
    {
      return true;
    }

    return unitsMap.containsKey(unit1) && unitsMap.containsKey(unit2)
        && unitsMap.get(unit1).getType() == unitsMap.get(unit2).getType();
  }

  public static Double convertToUnit(
      @Nonnull final Double value,
      @Nonnull final String fromUnit,
      @Nonnull final String toUnit)
  {
    Preconditions.checkNotNull(value, "value must not be null");
    Preconditions.checkNotNull(fromUnit, "fromUnit must not be null");
    Preconditions.checkNotNull(toUnit, "toUnit must not be null");

    if (fromUnit.equals(toUnit))
    {
      return value;
    }

    if (areUnitsCompatible(fromUnit, toUnit))
    {
      final Double fromFactor = unitsMap.get(fromUnit).getFactor();
      final Double toFactor = unitsMap.get(toUnit).getFactor();

      return value * fromFactor / toFactor;
    }

    return null;
  }

  private static class UnitConversionDo
  {
    private final UnitType type;
    private final Double factor;

    private UnitConversionDo(final UnitType type, final Double factor)
    {
      this.type = type;
      this.factor = factor;
    }

    public UnitType getType()
    {
      return type;
    }

    public Double getFactor()
    {
      return factor;
    }
  }

  private enum UnitType
  {
    LIQUID_UNIT, MASS_UNIT, TIME_UNIT
  }
}
