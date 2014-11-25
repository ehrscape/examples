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

import com.marand.maf.core.data.object.DataObject;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyDayDto extends DataObject
{
  private TherapyDto therapy;

  private TherapyStatusEnum therapyStatus;
  private boolean therapyActionsAllowed;
  private boolean therapyEndsBeforeNextRounds;
  private boolean modifiedFromLastReview;
  private boolean modified;
  private boolean active;
  private int consecutiveDay;
  private boolean showConsecutiveDay;

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  public TherapyStatusEnum getTherapyStatus()
  {
    return therapyStatus;
  }

  public void setTherapyStatus(final TherapyStatusEnum therapyStatus)
  {
    this.therapyStatus = therapyStatus;
  }

  public boolean isModifiedFromLastReview()
  {
    return modifiedFromLastReview;
  }

  public void setModifiedFromLastReview(final boolean modifiedFromLastReview)
  {
    this.modifiedFromLastReview = modifiedFromLastReview;
  }

  public boolean isModified()
  {
    return modified;
  }

  public void setModified(final boolean modified)
  {
    this.modified = modified;
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(final boolean active)
  {
    this.active = active;
  }

  public boolean isTherapyActionsAllowed()
  {
    return therapyActionsAllowed;
  }

  public void setTherapyActionsAllowed(final boolean therapyActionsAllowed)
  {
    this.therapyActionsAllowed = therapyActionsAllowed;
  }

  public boolean isTherapyEndsBeforeNextRounds()
  {
    return therapyEndsBeforeNextRounds;
  }

  public void setTherapyEndsBeforeNextRounds(final boolean therapyEndsBeforeNextRounds)
  {
    this.therapyEndsBeforeNextRounds = therapyEndsBeforeNextRounds;
  }

  public int getConsecutiveDay()
  {
    return consecutiveDay;
  }

  public void setConsecutiveDay(final int consecutiveDay)
  {
    this.consecutiveDay = consecutiveDay;
  }

  public boolean isShowConsecutiveDay()
  {
    return showConsecutiveDay;
  }

  public void setShowConsecutiveDay(final boolean showConsecutiveDay)
  {
    this.showConsecutiveDay = showConsecutiveDay;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("therapyStatus", therapyStatus)
        .append("modifiedFromLastReview", modifiedFromLastReview)
        .append("modified", modified)
        .append("active", active)
        .append("therapyActionsAllowed", therapyActionsAllowed)
        .append("therapyEndsBeforeNextRounds", therapyEndsBeforeNextRounds)
        .append("consecutiveDay", consecutiveDay)
        .append("showConsecutiveDay", showConsecutiveDay)
    ;
  }
}
