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

import com.marand.maf.core.hibernate.entity.AbstractEffectiveEntity;
import com.marand.thinkmed.medications.model.Medication;
import com.marand.thinkmed.medications.model.MedicationIngredient;
import com.marand.thinkmed.medications.model.MedicationIngredientLink;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

/**
 * @author Mitja Lapajne
 */
@Entity
public class MedicationIngredientLinkImpl extends AbstractEffectiveEntity implements MedicationIngredientLink
{
  private MedicationIngredient ingredient;
  private Medication medication;
  private Double strengthNumerator;
  private String strengthNumeratorUnit;
  private Double strengthDenominator;
  private String strengthDenominatorUnit;
  private boolean descriptive;

  @Override
  @ManyToOne(targetEntity = MedicationIngredientImpl.class, fetch = FetchType.LAZY, optional = false)
  @Index(name = "xfMedicationIngredientLinkIng")
  public MedicationIngredient getIngredient()
  {
    return ingredient;
  }

  @Override
  public void setIngredient(final MedicationIngredient ingredient)
  {
    this.ingredient = ingredient;
  }

  @Override
  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY, optional = false)
  @Index(name = "xfMedicationIngredientLinkMed")
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
  @Column(nullable = false)
  public Double getStrengthNumerator()
  {
    return strengthNumerator;
  }

  @Override
  public void setStrengthNumerator(final Double strengthNumerator)
  {
    this.strengthNumerator = strengthNumerator;
  }

  @Override
  @Column(nullable = false)
  public String getStrengthNumeratorUnit()
  {
    return strengthNumeratorUnit;
  }

  @Override
  public void setStrengthNumeratorUnit(final String strengthNumeratorUnit)
  {
    this.strengthNumeratorUnit = strengthNumeratorUnit;
  }

  @Override
  public Double getStrengthDenominator()
  {
    return strengthDenominator;
  }

  @Override
  public void setStrengthDenominator(final Double strengthDenominator)
  {
    this.strengthDenominator = strengthDenominator;
  }

  @Override
  public String getStrengthDenominatorUnit()
  {
    return strengthDenominatorUnit;
  }

  @Override
  public void setStrengthDenominatorUnit(final String strengthDenominatorUnit)
  {
    this.strengthDenominatorUnit = strengthDenominatorUnit;
  }

  @Override
  public boolean isDescriptive()
  {
    return descriptive;
  }

  @Override
  public void setDescriptive(final boolean descriptive)
  {
    this.descriptive = descriptive;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("ingredient", ingredient)
        .append("medication", medication)
        .append("strengthNumerator", strengthNumerator)
        .append("strengthNumeratorUnit", strengthNumeratorUnit)
        .append("strengthDenominator", strengthDenominator)
        .append("strengthDenominatorUnit", strengthDenominatorUnit)
        .append("descriptive", descriptive)
    ;
  }
}
