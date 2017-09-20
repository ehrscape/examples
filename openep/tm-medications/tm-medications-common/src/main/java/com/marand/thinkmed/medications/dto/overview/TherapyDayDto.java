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

package com.marand.thinkmed.medications.dto.overview;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class TherapyDayDto extends DataTransferObject
{
  private TherapyDto therapy;

  private TherapyStatusEnum therapyStatus;
  private TherapyChangeReasonEnum therapyChangeReasonEnum;
  private boolean doctorReviewNeeded;
  private boolean therapyEndsBeforeNextRounds;
  private boolean modifiedFromLastReview;
  private boolean modified;
  private boolean active;
  private TherapyPharmacistReviewStatusEnum therapyPharmacistReviewStatus;
  private boolean activeAnyPartOfDay;
  private int consecutiveDay;
  private boolean showConsecutiveDay;
  private boolean basedOnPharmacyReview;
  private DateTime originalTherapyStart;
  private DateTime lastModifiedTimestamp;
  private List<TherapyTaskSimpleDto> tasks = new ArrayList<>();
  private DateTime reviewedUntil;

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

  public TherapyChangeReasonEnum getTherapyChangeReasonEnum()
  {
    return therapyChangeReasonEnum;
  }

  public void setTherapyChangeReasonEnum(final TherapyChangeReasonEnum therapyChangeReasonEnum)
  {
    this.therapyChangeReasonEnum = therapyChangeReasonEnum;
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

  public boolean isActiveAnyPartOfDay()
  {
    return activeAnyPartOfDay;
  }

  public void setActiveAnyPartOfDay(final boolean activeAnyPartOfDay)
  {
    this.activeAnyPartOfDay = activeAnyPartOfDay;
  }

  public boolean isDoctorReviewNeeded()
  {
    return doctorReviewNeeded;
  }

  public void setDoctorReviewNeeded(final boolean doctorReviewNeeded)
  {
    this.doctorReviewNeeded = doctorReviewNeeded;
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

  public TherapyPharmacistReviewStatusEnum getTherapyPharmacistReviewStatus()
  {
    return therapyPharmacistReviewStatus;
  }

  public void setTherapyPharmacistReviewStatus(final TherapyPharmacistReviewStatusEnum therapyPharmacistReviewStatus)
  {
    this.therapyPharmacistReviewStatus = therapyPharmacistReviewStatus;
  }

  public boolean isShowConsecutiveDay()
  {
    return showConsecutiveDay;
  }

  public void setShowConsecutiveDay(final boolean showConsecutiveDay)
  {
    this.showConsecutiveDay = showConsecutiveDay;
  }

  public boolean isBasedOnPharmacyReview()
  {
    return basedOnPharmacyReview;
  }

  public void setBasedOnPharmacyReview(final boolean basedOnPharmacyReview)
  {
    this.basedOnPharmacyReview = basedOnPharmacyReview;
  }

  public DateTime getOriginalTherapyStart()
  {
    return originalTherapyStart;
  }

  public void setOriginalTherapyStart(final DateTime originalTherapyStart)
  {
    this.originalTherapyStart = originalTherapyStart;
  }

  public DateTime getLastModifiedTimestamp()
  {
    return lastModifiedTimestamp;
  }

  public void setLastModifiedTimestamp(final DateTime lastModifiedTimestamp)
  {
    this.lastModifiedTimestamp = lastModifiedTimestamp;
  }

  public List<TherapyTaskSimpleDto> getTasks()
  {
    return tasks;
  }

  public void setTasks(final List<TherapyTaskSimpleDto> tasks)
  {
    this.tasks = tasks;
  }

  public DateTime getReviewedUntil()
  {
    return reviewedUntil;
  }

  public void setReviewedUntil(final DateTime reviewedUntil)
  {
    this.reviewedUntil = reviewedUntil;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("therapyStatus", therapyStatus)
        .append("therapyChangeReasonEnum", therapyChangeReasonEnum)
        .append("modifiedFromLastReview", modifiedFromLastReview)
        .append("modified", modified)
        .append("active", active)
        .append("activeAnyPartOfDay", activeAnyPartOfDay)
        .append("doctorReviewNeeded", doctorReviewNeeded)
        .append("therapyEndsBeforeNextRounds", therapyEndsBeforeNextRounds)
        .append("therapyPharmacistReviewStatus", therapyPharmacistReviewStatus)
        .append("consecutiveDay", consecutiveDay)
        .append("showConsecutiveDay", showConsecutiveDay)
        .append("basedOnPharmacyReview", basedOnPharmacyReview)
        .append("originalTherapyStart", originalTherapyStart)
        .append("lastModifiedTimestamp", lastModifiedTimestamp)
        .append("tasks", tasks)
        .append("reviewedUntil", reviewedUntil)
    ;
  }
}
