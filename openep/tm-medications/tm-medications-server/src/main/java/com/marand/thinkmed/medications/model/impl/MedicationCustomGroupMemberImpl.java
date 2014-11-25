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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import com.marand.thinkmed.medications.model.Medication;
import com.marand.thinkmed.medications.model.MedicationCustomGroup;
import com.marand.thinkmed.medications.model.MedicationCustomGroupMember;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

/**
 * @author Mitja Lapajne
 */
@Entity
public class MedicationCustomGroupMemberImpl extends AbstractPermanentEntity implements MedicationCustomGroupMember
{
  private Medication medication;
  private MedicationCustomGroup medicationCustomGroup;

  @Override
  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY, optional = false)
  @Index(name = "xfMedCustomGroupMed")
  public Medication getMedication()
  {
    return medication;
  }

  @Override
  public void setMedication(final Medication medication)
  {
    this.medication = medication;
  }

  @Override
  @ManyToOne(targetEntity = MedicationCustomGroupImpl.class, fetch = FetchType.LAZY, optional = false)
  @Index(name = "xfMedCustomGroupGroup")
  public MedicationCustomGroup getMedicationCustomGroup()
  {
    return medicationCustomGroup;
  }

  @Override
  public void setMedicationCustomGroup(final MedicationCustomGroup medicationCustomGroup)
  {
    this.medicationCustomGroup = medicationCustomGroup;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("medication", medication)
        .append("medicationCustomGroup", medicationCustomGroup);
  }
}
