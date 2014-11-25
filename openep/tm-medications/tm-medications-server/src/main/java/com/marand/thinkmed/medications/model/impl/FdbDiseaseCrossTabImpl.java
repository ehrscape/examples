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

import com.marand.thinkmed.medications.model.FdbDiseaseCrossTab;
import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

/**
 * @author Bostjan Vester
 */
@Entity
public class FdbDiseaseCrossTabImpl extends AbstractPermanentEntity implements FdbDiseaseCrossTab
{
  private String icd10Code;
  private String icd9Code;
  private boolean equivalent;
  private boolean unique;

  @Override
  public boolean isEquivalent()
  {
    return equivalent;
  }

  @Override
  public void setEquivalent(final boolean equivalent)
  {
    this.equivalent = equivalent;
  }

  @Override
  @Index(name = "xeIcd10Code")
  public String getIcd10Code()
  {
    return icd10Code;
  }

  @Override
  public void setIcd10Code(final String icd10Code)
  {
    this.icd10Code = icd10Code;
  }

  @Override
  @Index(name = "xeIcd9Code")
  public String getIcd9Code()
  {
    return icd9Code;
  }

  @Override
  public void setIcd9Code(final String icd9Code)
  {
    this.icd9Code = icd9Code;
  }

  @Override
  @Column(name = "unique_")
  public boolean isUnique()
  {
    return unique;
  }

  @Override
  public void setUnique(final boolean unique)
  {
    this.unique = unique;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    
    tsb
        .append("icd10Code", icd10Code)
        .append("icd9Code", icd9Code)
        .append("equivalent", equivalent)
        .append("unique", unique)
        ;
  }
}
