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

import javax.persistence.Column;
import javax.persistence.Entity;

import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import com.marand.thinkmed.medications.model.PatientTherapyLastLinkName;
import org.apache.commons.lang3.builder.ToStringBuilder;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = @Index(name = "xfTherapyLastLinkPatient", columnList = "patient_id"))
public class PatientTherapyLastLinkNameImpl extends AbstractPermanentEntity implements PatientTherapyLastLinkName
{
  private Long patientId;
  private String lastLinkName;

  @Override
  public Long getPatientId()
  {
    return patientId;
  }

  @Override
  public void setPatientId(final Long patientId)
  {
    this.patientId = patientId;
  }

  @Override
  @Column(nullable = false)
  public String getLastLinkName()
  {
    return lastLinkName;
  }

  @Override
  public void setLastLinkName(final String lastLinkName)
  {
    this.lastLinkName = lastLinkName;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("patientId", patientId);
    tsb.append("lastLinkName", lastLinkName);
  }
}
