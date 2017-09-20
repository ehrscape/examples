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

package com.marand.thinkmed.medications.converter.therapy;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.thinkmed.medications.dto.TherapyDto;

/**
 * @author Bostjan Vester
 */
public final class MedicationConverterSelector
{
  // To EHR converters
  private static final ConstantSimpleMedicationToEhrConverter CONSTANT_SIMPLE_TO = new ConstantSimpleMedicationToEhrConverter();
  private static final VariableSimpleMedicationToEhrConverter VARIABLE_SIMPLE_TO = new VariableSimpleMedicationToEhrConverter();
  private static final ConstantComplexMedicationToEhrConverter CONSTANT_COMPLEX_TO = new ConstantComplexMedicationToEhrConverter();
  private static final VariableComplexMedicationToEhrConverter VARIABLE_COMPLEX_TO = new VariableComplexMedicationToEhrConverter();
  private static final OxygenMedicationToEhrConverter OXYGEN_TO = new OxygenMedicationToEhrConverter();

  // From EHR converters
  private static final ConstantSimpleMedicationFromEhrConverter CONSTANT_SIMPLE_FROM = new ConstantSimpleMedicationFromEhrConverter();
  private static final VariableSimpleMedicationFromEhrConverter VARIABLE_SIMPLE_FROM = new VariableSimpleMedicationFromEhrConverter();
  private static final ConstantComplexMedicationFromEhrConverter CONSTANT_COMPLEX_FROM = new ConstantComplexMedicationFromEhrConverter();
  private static final VariableComplexMedicationFromEhrConverter VARIABLE_COMPLEX_FROM = new VariableComplexMedicationFromEhrConverter();
  private static final OxygenMedicationFromEhrConverter OXYGEN_FROM = new OxygenMedicationFromEhrConverter();

  private static final List<? extends MedicationToEhrConverter<?>> toEhrConverters = Arrays.asList(
      CONSTANT_SIMPLE_TO,
      VARIABLE_SIMPLE_TO,
      CONSTANT_COMPLEX_TO,
      VARIABLE_COMPLEX_TO,
      OXYGEN_TO
  );

  private static final List<? extends MedicationFromEhrConverter<?>> fromEhrConverters = Arrays.asList(
      CONSTANT_SIMPLE_FROM,
      VARIABLE_SIMPLE_FROM,
      CONSTANT_COMPLEX_FROM,
      VARIABLE_COMPLEX_FROM,
      OXYGEN_FROM
  );

  private MedicationConverterSelector()
  {
  }

  public static MedicationToEhrConverter<?> getConverter(final TherapyDto therapy)
  {
    Preconditions.checkNotNull(therapy);
    for (final MedicationToEhrConverter<?> converter : toEhrConverters)
    {
      if (converter.isFor(therapy))
      {
        return converter;
      }
    }
    throw new IllegalArgumentException("No medication converter to EHR found for ["
                                           + therapy.getClass().getSimpleName() + "]!");
  }

  public static MedicationFromEhrConverter<?> getConverter(final MedicationInstructionInstruction instruction)
  {
    Preconditions.checkNotNull(instruction);
    for (final MedicationFromEhrConverter<?> converter : fromEhrConverters)
    {
      if (converter.isFor(instruction))
      {
        return converter;
      }
    }
    throw new IllegalArgumentException("No medication converter from EHR found for instruction ["
                                           + DvUtils.getString(instruction.getNarrative()) + "]!");
  }
}
