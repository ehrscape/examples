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

package com.marand.thinkmed.medications.dto;

import java.util.List;

import com.marand.maf.core.data.object.DataObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyViewDto extends DataObject
{
  private MedicationsCentralCaseDto medicationsCentralCase;
  private RoundsIntervalDto roundsInterval;
  private AdministrationTimingDto administrationTiming;
  private List<String> customGroups;
  private Double referenceWeight;

  public MedicationsCentralCaseDto getMedicationsCentralCase()
  {
    return medicationsCentralCase;
  }

  public void setMedicationsCentralCase(final MedicationsCentralCaseDto medicationsCentralCase)
  {
    this.medicationsCentralCase = medicationsCentralCase;
  }

  public RoundsIntervalDto getRoundsInterval()
  {
    return roundsInterval;
  }

  public void setRoundsInterval(final RoundsIntervalDto roundsInterval)
  {
    this.roundsInterval = roundsInterval;
  }

  public AdministrationTimingDto getAdministrationTiming()
  {
    return administrationTiming;
  }

  public void setAdministrationTiming(final AdministrationTimingDto administrationTiming)
  {
    this.administrationTiming = administrationTiming;
  }

  public List<String> getCustomGroups()
  {
    return customGroups;
  }

  public void setCustomGroups(final List<String> customGroups)
  {
    this.customGroups = customGroups;
  }

  public Double getReferenceWeight()
  {
    return referenceWeight;
  }

  public void setReferenceWeight(final Double referenceWeight)
  {
    this.referenceWeight = referenceWeight;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("medicationsCentralCase", medicationsCentralCase)
        .append("roundsInterval", roundsInterval)
        .append("administrationTiming", administrationTiming)
        .append("customGroups", customGroups)
        .append("referenceWeight", referenceWeight)
    ;
  }
}
