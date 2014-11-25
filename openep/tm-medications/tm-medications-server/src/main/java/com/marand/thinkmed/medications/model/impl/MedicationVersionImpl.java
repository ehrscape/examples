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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.marand.maf.core.hibernate.entity.AbstractTemporalEntity;
import com.marand.thinkmed.medications.model.AtcClassification;
import com.marand.thinkmed.medications.model.DoseForm;
import com.marand.thinkmed.medications.model.Medication;
import com.marand.thinkmed.medications.model.MedicationBasicUnit;
import com.marand.thinkmed.medications.model.MedicationGeneric;
import com.marand.thinkmed.medications.model.MedicationVersion;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

/**
 * @author Klavdij Lapajne
 */
@Entity
public class MedicationVersionImpl extends AbstractTemporalEntity implements MedicationVersion
{
  private String name;
  private String shortName;
  private String longName;
  private MedicationGeneric medicationGeneric;
  private DoseForm doseForm;
  private AtcClassification atcClassification;
  private MedicationBasicUnit basicUnit;
  private Medication medication;
  private Boolean active;

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public void setName(final String name)
  {
    this.name = name;
  }

  @Override
  public String getShortName()
  {
    return shortName;
  }

  @Override
  public void setShortName(final String shortName)
  {
    this.shortName = shortName;
  }

  @Override
  public String getLongName()
  {
    return longName;
  }

  @Override
  public void setLongName(final String longName)
  {
    this.longName = longName;
  }

  @Override
  @ManyToOne(targetEntity = MedicationGenericImpl.class, fetch = FetchType.LAZY)
  @Index(name = "xfMedicationGeneric")
  public MedicationGeneric getMedicationGeneric()
  {
    return medicationGeneric;
  }

  @Override
  public void setMedicationGeneric(final MedicationGeneric medicationGeneric)
  {
    this.medicationGeneric = medicationGeneric;
  }

  @Override
  @ManyToOne(targetEntity = DoseFormImpl.class, fetch = FetchType.LAZY)
  @Index(name = "xfMedicationDoseForm")
  public DoseForm getDoseForm()
  {
    return doseForm;
  }

  @Override
  @ManyToOne(targetEntity = MedicationVersionImpl.class, fetch = FetchType.LAZY)
  public void setDoseForm(final DoseForm doseForm)
  {
    this.doseForm = doseForm;
  }

  @Override
  @ManyToOne(targetEntity = AtcClassificationImpl.class, fetch = FetchType.LAZY)
  @Index(name = "xfMedicationAtcClass")
  public AtcClassification getAtcClassification()
  {
    return atcClassification;
  }

  @Override
  public void setAtcClassification(final AtcClassification atcClassification)
  {
    this.atcClassification = atcClassification;
  }

  @Override
  @ManyToOne(targetEntity = MedicationBasicUnitImpl.class, fetch = FetchType.LAZY)
  @Index(name = "xfMedicationBasicUnit")
  public MedicationBasicUnit getBasicUnit()
  {
    return basicUnit;
  }

  @Override
  public void setBasicUnit(final MedicationBasicUnit basicUnit)
  {
    this.basicUnit = basicUnit;
  }

  @Override
  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY)
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
  public Boolean getActive()
  {
    return active;
  }

  @Override
  @Column(nullable = false)
  public void setActive(final Boolean active)
  {
    this.active = active;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("shortName", shortName)
        .append("longName", longName)
        .append("name", name)
        .append("doseForm", doseForm)
        .append("atcClassification", atcClassification)
        .append("basicUnit", basicUnit)
        .append("medication", medication)
        .append("active", active)
    ;
  }
}
