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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustAdministrationSubtype;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdjustOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationSubtype;
import com.marand.thinkmed.medications.dto.administration.StartOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;

public class TherapyJsonDeserializer
{
  private final List<JsonUtil.TypeAdapterPair> typeAdapters = new ArrayList<>();
  public static final TherapyJsonDeserializer INSTANCE = new TherapyJsonDeserializer();

  private TherapyJsonDeserializer()
  {
    typeAdapters.add(new JsonUtil.TypeAdapterPair(TherapyDto.class, new TherapyDtoDeserializer()));
    typeAdapters.add(new JsonUtil.TypeAdapterPair(AdministrationDto.class, new AdministrationDeserializer()));
  }

  public List<JsonUtil.TypeAdapterPair> getTypeAdapters()
  {
    return typeAdapters;
  }

  private static class TherapyDtoDeserializer implements JsonDeserializer<TherapyDto>
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
      final boolean variable = context.deserialize(json.getAsJsonObject().get("variable"), Boolean.class);

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

      if (medicationOrderFormType.equals(MedicationOrderFormType.OXYGEN.name()))
      {
        return context.deserialize(json, OxygenTherapyDto.class);
      }

      return null;
    }
  }

  private static class AdministrationDeserializer implements JsonDeserializer<AdministrationDto>
  {
    @Override
    public AdministrationDto deserialize(
        final JsonElement json,
        final Type typeOfT,
        final JsonDeserializationContext context) throws JsonParseException
    {
      final AdministrationTypeEnum administrationTypeEnum = context.deserialize(
          json.getAsJsonObject().get("administrationType"),
          AdministrationTypeEnum.class);

      if (administrationTypeEnum == AdministrationTypeEnum.START)
      {
        return context.deserialize(
            json,
            isOxygenStartAdministration(json, context)
            ? StartOxygenAdministrationDto.class
            : StartAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.BOLUS)
      {
        return context.deserialize(json, BolusAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION)
      {
        return context.deserialize(
            json,
            isOxygenAdjustAdministration(json, context)
            ? AdjustOxygenAdministrationDto.class
            : AdjustInfusionAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.STOP)
      {
        return context.deserialize(json, StopAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.INFUSION_SET_CHANGE)
      {
        return context.deserialize(json, InfusionSetChangeDto.class);
      }
      return null;
    }

    private boolean isOxygenAdjustAdministration(final JsonElement json, final JsonDeserializationContext context)
    {
      final JsonElement jsonElement = json.getAsJsonObject().get("adjustAdministrationSubtype");
      return Opt
          .resolve(() -> context.deserialize(jsonElement, AdjustAdministrationSubtype.class))
          .map(o -> o == AdjustAdministrationSubtype.OXYGEN)
          .orElseGet(() -> false);
    }

    private boolean isOxygenStartAdministration(final JsonElement json, final JsonDeserializationContext context)
    {
      final JsonElement jsonElement = json.getAsJsonObject().get("startAdministrationSubtype");
      return Opt
          .resolve(() -> context.deserialize(jsonElement, StartAdministrationSubtype.class))
          .map(o -> o == StartAdministrationSubtype.OXYGEN)
          .orElseGet(() -> false);
    }
  }

  public void addTypeAdapter(@Nonnull final JsonUtil.TypeAdapterPair typeAdapterPair)
  {
    Preconditions.checkNotNull(typeAdapterPair, "typeAdapterPair is required");
    typeAdapters.add(typeAdapterPair);
  }
}
