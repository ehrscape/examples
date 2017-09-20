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

package com.marand.thinkmed.medications.connector.data.object;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class AllergiesDto extends DataTransferObject implements JsonSerializable
{
  private DateTime reviewDate;
  private AllergiesStatus status;
  private List<AllergyDto> allergies = new ArrayList<>();

  public DateTime getReviewDate()
  {
    return reviewDate;
  }

  public void setReviewDate(final DateTime reviewDate)
  {
    this.reviewDate = reviewDate;
  }

  public AllergiesStatus getStatus()
  {
    return status;
  }

  public void setStatus(final AllergiesStatus status)
  {
    this.status = status;
  }

  public List<AllergyDto> getAllergies()
  {
    return allergies;
  }

  public void setAllergies(final List<AllergyDto> allergies)
  {
    this.allergies = allergies;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("reviewDate", reviewDate)
        .append("status", status)
        .append("allergies", allergies);
  }
}
