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

package com.marand.thinkmed.medications.dto.report;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyDayElementReportDto extends DataTransferObject
{
  private TherapyDto order;
  private String therapyConsecutiveDay;
  private String therapyStart;
  private String therapyEnd;
  private TherapyReportStatusEnum therapyReportStatusEnum;
  private String customGroupName;
  private int customGroupSortOrder;
  private TherapyPharmacistReviewStatusEnum pharmacistsReviewState;
  private List<AdministrationDto> administrations = new ArrayList<>();

  public TherapyDto getOrder()
  {
    return order;
  }

  public void setOrder(final TherapyDto order)
  {
    this.order = order;
  }

  public String getTherapyConsecutiveDay()
  {
    return therapyConsecutiveDay;
  }

  public void setTherapyConsecutiveDay(final String therapyConsecutiveDay)
  {
    this.therapyConsecutiveDay = therapyConsecutiveDay;
  }

  public String getTherapyStart()
  {
    return therapyStart;
  }

  public void setTherapyStart(final String therapyStart)
  {
    this.therapyStart = therapyStart;
  }

  public String getTherapyEnd()
  {
    return therapyEnd;
  }

  public void setTherapyEnd(final String therapyEnd)
  {
    this.therapyEnd = therapyEnd;
  }

  public TherapyReportStatusEnum getTherapyReportStatusEnum()
  {
    return therapyReportStatusEnum;
  }

  public void setTherapyReportStatusEnum(final TherapyReportStatusEnum therapyReportStatusEnum)
  {
    this.therapyReportStatusEnum = therapyReportStatusEnum;
  }

  public String getCustomGroupName()
  {
    return customGroupName;
  }

  public void setCustomGroupName(final String customGroupName)
  {
    this.customGroupName = customGroupName;
  }

  public int getCustomGroupSortOrder()
  {
    return customGroupSortOrder;
  }

  public void setCustomGroupSortOrder(final int customGroupSortOrder)
  {
    this.customGroupSortOrder = customGroupSortOrder;
  }

  public List<AdministrationDto> getAdministrations()
  {
    return administrations;
  }

  public void setAdministrations(final List<AdministrationDto> administrations)
  {
    this.administrations = administrations;
  }

  public TherapyPharmacistReviewStatusEnum getPharmacistsReviewState()
  {
    return pharmacistsReviewState;
  }

  public void setPharmacistsReviewState(final TherapyPharmacistReviewStatusEnum pharmacistsReviewState)
  {
    this.pharmacistsReviewState = pharmacistsReviewState;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("order", order)
        .append("therapyConsecutiveDay", therapyConsecutiveDay)
        .append("therapyStart", therapyStart)
        .append("therapyEnd", therapyEnd)
        .append("therapyReportStatusEnum", therapyReportStatusEnum)
        .append("customGroupName", customGroupName)
        .append("customGroupSortOrder", customGroupSortOrder)
        .append("pharmacistsReviewState", pharmacistsReviewState)
        .append("administrations", administrations)
    ;
  }
}
