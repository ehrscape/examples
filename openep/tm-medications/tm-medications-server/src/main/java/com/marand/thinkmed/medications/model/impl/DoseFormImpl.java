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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.marand.maf.core.hibernate.entity.AbstractEffectiveCatalogEntity;
import com.marand.thinkmed.medications.DoseFormType;
import com.marand.thinkmed.medications.MedicationOrderFormType;
import com.marand.thinkmed.medications.model.DoseForm;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
public class DoseFormImpl extends AbstractEffectiveCatalogEntity implements DoseForm
{
  private MedicationOrderFormType medicationOrderFormType;
  private DoseFormType doseFormType;
  private Double splitFactor;

  @Override
  @Enumerated(EnumType.STRING)
  public MedicationOrderFormType getMedicationOrderFormType()
  {
    return medicationOrderFormType;
  }

  @Override
  public void setMedicationOrderFormType(final MedicationOrderFormType medicationOrderFormType)
  {
    this.medicationOrderFormType = medicationOrderFormType;
  }

  @Override
  @Enumerated(EnumType.STRING)
  public DoseFormType getDoseFormType()
  {
    return doseFormType;
  }

  @Override
  public void setDoseFormType(final DoseFormType doseFormType)
  {
    this.doseFormType = doseFormType;
  }

  @Override
  public Double getSplitFactor()
  {
    return splitFactor;
  }

  @Override
  public void setSplitFactor(final Double splitFactor)
  {
    this.splitFactor = splitFactor;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("medicationOrderFormType", medicationOrderFormType)
        .append("doseFormType", doseFormType)
        .append("splitFactor", splitFactor);
  }
}
