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

import com.google.common.base.Preconditions;
import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Mitja Lapajne
 */
public enum ClinicalInterventionEnum
{
  COMPLETED(
      DataValueUtils.getCodedText("local", "at0043", "Procedure has been completed."),
      DataValueUtils.getCodedText("openehr", "532", "completed"));

  private final DvCodedText careflowStep;
  private final DvCodedText currentState;

  ClinicalInterventionEnum(final DvCodedText careflowStep, final DvCodedText currentState)
  {
    this.careflowStep = Preconditions.checkNotNull(careflowStep);
    this.currentState = Preconditions.checkNotNull(currentState);
  }

  public DvCodedText getCareflowStep()
  {
    return careflowStep;
  }

  public DvCodedText getCurrentState()
  {
    return currentState;
  }

  public static ClinicalInterventionEnum getAction(final DvCodedText careflowStep, final DvCodedText currentState)
  {
    for (final ClinicalInterventionEnum medicationAction : values())
    {
      if (medicationAction.getCareflowStep().equals(careflowStep) && medicationAction.getCurrentState().equals(currentState))
      {
        return medicationAction;
      }
    }
    return null;
  }
}
