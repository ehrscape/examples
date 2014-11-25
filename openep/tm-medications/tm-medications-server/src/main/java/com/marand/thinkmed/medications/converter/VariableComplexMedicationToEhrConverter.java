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

package com.marand.thinkmed.medications.converter;

import java.util.Collection;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.AdministrationDetailsCluster;
import com.marand.thinkmed.medications.dto.ComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.VariableComplexTherapyDto;

/**
 * @author Bostjan Vester
 */
public class VariableComplexMedicationToEhrConverter extends ComplexMedicationToEhrConverter<VariableComplexTherapyDto>
{
  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof VariableComplexTherapyDto;
  }

  @Override
  protected Collection<Pair<ComplexDoseElementDto, HourMinuteDto>> getDoseElements(final VariableComplexTherapyDto therapy)
  {
    return Lists.transform(
        therapy.getTimedDoseElements(),
        new Function<TimedComplexDoseElementDto, Pair<ComplexDoseElementDto,HourMinuteDto>>()
        {
          @Nullable
          @Override
          public Pair<ComplexDoseElementDto, HourMinuteDto> apply(@Nullable final TimedComplexDoseElementDto from)
          {
            if (from == null)
            {
              return null;
            }
            return Pair.of(from.getDoseElement(), from.getDoseTime());
          }
        });
  }

  @Override
  protected boolean isSpecificSpeedHandled(
      final VariableComplexTherapyDto therapy,
      final AdministrationDetailsCluster administration)
  {
    return false;
  }
}
