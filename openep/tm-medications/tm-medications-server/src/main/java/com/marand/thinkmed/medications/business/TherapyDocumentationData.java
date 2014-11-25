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

package com.marand.thinkmed.medications.business;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.joda.time.Interval;

/**
 * User: MihaA
 */

public class TherapyDocumentationData
{
  private Pair<MedicationOrderComposition, MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction> instructionPair;
  private final List<Pair<String, Interval>> intervals = new ArrayList<>();
  private TherapyDto therapy;

  public Pair<MedicationOrderComposition, MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction> getInstructionPair()
  {
    return instructionPair;
  }

  public void setInstructionPair(final Pair<MedicationOrderComposition, MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction> instructionPair)
  {
    this.instructionPair = instructionPair;
  }

  public List<Pair<String, Interval>> getIntervals()
  {
    return intervals;
  }

  public void addInterval(final String therapyId, final Interval interval)
  {
    intervals.add(Pair.of(therapyId, interval));
  }

  public void removeInterval(final String therapyId, final Interval interval)
  {
    intervals.remove(Pair.of(therapyId, interval));
  }

  public Interval findIntervalForId(final String id)
  {
    for (final Pair<String, Interval> pair : intervals)
    {
      if (pair.getFirst().equals(id))
      {
        return pair.getSecond();
      }
    }
    return null;
  }

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }
}
