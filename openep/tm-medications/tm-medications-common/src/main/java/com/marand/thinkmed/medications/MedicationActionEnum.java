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

import java.util.EnumSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Bostjan Vester
 */
public enum MedicationActionEnum
{
  //therapy options
  SCHEDULE(
      DataValueUtils.getCodedText("local", "at0016", "Set medication start date"),
      DataValueUtils.getCodedText("openehr", "529", "scheduled")),
  START(
      DataValueUtils.getCodedText("local", "at0004", "Commence medication"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  REVIEW(
      DataValueUtils.getCodedText("local", "at0005", "Review medication"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  SUSPEND(
      DataValueUtils.getCodedText("local", "at0009", "Suspend administration"),
      DataValueUtils.getCodedText("openehr", "530", "suspended")),
  REISSUE(
      DataValueUtils.getCodedText("local", "at0010", "Reissue medication"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  CANCEL(
      DataValueUtils.getCodedText("local", "at0012", "Cancel medication plan"),
      DataValueUtils.getCodedText("openehr", "528", "cancelled")),
  COMPLETE(
      DataValueUtils.getCodedText("local", "at0039", "Change dose or timing"),
      DataValueUtils.getCodedText("openehr", "531", "aborted")),
  ABORT(
      DataValueUtils.getCodedText("local", "at0015", "Cease administration"),
      DataValueUtils.getCodedText("openehr", "531", "aborted")),

  //administration options
  ADMINISTER(
      DataValueUtils.getCodedText("local", "at0006", "Administer medication"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  WITHHOLD(
      DataValueUtils.getCodedText("local", "at0018", "Withhold medication"),
      DataValueUtils.getCodedText("openehr", "245", "active"));

  public static final Set<MedicationActionEnum> THERAPY_FINISHED = EnumSet.of(ABORT, CANCEL, COMPLETE);

  private final DvCodedText careflowStep;
  private final DvCodedText currentState;

  MedicationActionEnum(final DvCodedText careflowStep, final DvCodedText currentState)
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

  public static MedicationActionEnum getAction(final DvCodedText careflowStep, final DvCodedText currentState)
  {
    for (final MedicationActionEnum medicationAction : values())
    {
      if (medicationAction.getCareflowStep().equals(careflowStep) && medicationAction.getCurrentState().equals(currentState))
      {
        return medicationAction;
      }
    }
    return null;
  }
}
