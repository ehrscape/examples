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

package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;

import com.marand.maf.core.hibernate.entity.AbstractEntity;
import com.marand.thinkmed.medications.model.TherapyChanged;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
@Entity
public class TherapyChangedImpl extends AbstractEntity implements TherapyChanged
{
  private String patientId;
  private DateTime changeTime;

  @Override
  public String getPatientId()
  {
    return patientId;
  }

  @Override
  public void setPatientId(final String patientId)
  {
    this.patientId = patientId;
  }

  @Override
  @Type(type = "com.marand.maf.core.hibernate.type.DateTimeType")
  public DateTime getChangeTime()
  {
    return changeTime;
  }

  @Override
  public void setChangeTime(final DateTime changeTime)
  {
    this.changeTime = changeTime;
  }


  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("patientId", patientId);
    tsb.append("changeTime", changeTime);
  }
}
