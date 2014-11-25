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

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;

public class TherapyJsonAdapter extends JsonUtil.TypeAdapterPair
{
  public static final TherapyJsonAdapter INSTANCE = new TherapyJsonAdapter();

  private TherapyJsonAdapter()
  {
    super(TherapyDto.class, new TherapyJsonDeserializer());
  }

  private static class TherapyJsonDeserializer implements JsonDeserializer<TherapyDto>
  {
    @Override
    public TherapyDto deserialize(
        final JsonElement json,
        final Type typeOfT,
        final JsonDeserializationContext context)
        throws JsonParseException
    {
      final String medicationOrderFormType =
          context.deserialize(json.getAsJsonObject().get("medicationOrderFormType"), String.class);
      final boolean variable =
          context.deserialize(json.getAsJsonObject().get("variable"), Boolean.class);

      if (MedicationOrderFormType.SIMPLE_ORDERS.contains(MedicationOrderFormType.valueOf(medicationOrderFormType)))
      {
        if (variable)
        {
          return context.deserialize(json, VariableSimpleTherapyDto.class);
        }
        return context.deserialize(json, ConstantSimpleTherapyDto.class);
      }

      if (medicationOrderFormType.equals(MedicationOrderFormType.COMPLEX.name()))
      {
        if (variable)
        {
          return context.deserialize(json, VariableComplexTherapyDto.class);
        }
        return context.deserialize(json, ConstantComplexTherapyDto.class);
      }
      return null;
    }
  }
}
