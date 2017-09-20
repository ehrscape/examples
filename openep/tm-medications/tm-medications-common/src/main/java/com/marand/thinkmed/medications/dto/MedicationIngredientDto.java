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

import com.marand.maf.core.data.IdentityDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */
public class MedicationIngredientDto extends IdentityDto
{
  private long ingredientId;
  private String ingredientName;
  private Double strengthNumerator;
  private String strengthNumeratorUnit;
  private Double strengthDenominator;
  private String strengthDenominatorUnit;
  private boolean descriptive;
  private MedicationRuleEnum ingredientRule;

  public String getIngredientName()
  {
    return ingredientName;
  }

  public void setIngredientName(final String ingredientName)
  {
    this.ingredientName = ingredientName;
  }

  public Double getStrengthNumerator()
  {
    return strengthNumerator;
  }

  public void setStrengthNumerator(final Double strengthNumerator)
  {
    this.strengthNumerator = strengthNumerator;
  }

  public String getStrengthNumeratorUnit()
  {
    return strengthNumeratorUnit;
  }

  public void setStrengthNumeratorUnit(final String strengthNumeratorUnit)
  {
    this.strengthNumeratorUnit = strengthNumeratorUnit;
  }

  public Double getStrengthDenominator()
  {
    return strengthDenominator;
  }

  public void setStrengthDenominator(final Double strengthDenominator)
  {
    this.strengthDenominator = strengthDenominator;
  }

  public String getStrengthDenominatorUnit()
  {
    return strengthDenominatorUnit;
  }

  public void setStrengthDenominatorUnit(final String strengthDenominatorUnit)
  {
    this.strengthDenominatorUnit = strengthDenominatorUnit;
  }

  public boolean isDescriptive()
  {
    return descriptive;
  }

  public void setDescriptive(final boolean descriptive)
  {
    this.descriptive = descriptive;
  }

  public MedicationRuleEnum getIngredientRule()
  {
    return ingredientRule;
  }

  public void setIngredientRule(final MedicationRuleEnum ingredientRule)
  {
    this.ingredientRule = ingredientRule;
  }

  public long getIngredientId()
  {
    return ingredientId;
  }

  public void setIngredientId(final long ingredientId)
  {
    this.ingredientId = ingredientId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("ingredientId", ingredientId)
        .append("ingredientName", ingredientName)
        .append("strengthNumerator", strengthNumerator)
        .append("strengthNumeratorUnit", strengthNumeratorUnit)
        .append("strengthDenominator", strengthDenominator)
        .append("strengthDenominatorUnit", strengthDenominatorUnit)
        .append("descriptive", descriptive)
        .append("ingredientRule", ingredientRule)
    ;
  }
}
