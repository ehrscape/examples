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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.model.MedicationExternalCrossTab;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

/**
 * @author Mitja Lapajne
 */
@Entity
public class MedicationsExternalCrossTabImpl extends AbstractPermanentEntity implements MedicationExternalCrossTab
{
  private String externalSystem;
  private MedicationsExternalValueType valueType;
  private String value;
  private String externalValue;

  @Override
  @Column(nullable = false)
  @Index(name = "xpMedsExternalTransExtSys")
  public String getExternalSystem()
  {
    return externalSystem;
  }

  @Override
  public void setExternalSystem(final String externalSystem)
  {
    this.externalSystem = externalSystem;
  }

  @Override
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Index(name = "xpMedsExternalTransValueType")
  public MedicationsExternalValueType getValueType()
  {
    return valueType;
  }

  @Override
  public void setValueType(final MedicationsExternalValueType valueType)
  {
    this.valueType = valueType;
  }

  @Override
  @Column(nullable = false)
  @Index(name = "xpMedsExternalTransValue")
  public String getValue()
  {
    return value;
  }

  @Override
  public void setValue(final String value)
  {
    this.value = value;
  }

  @Override
  @Column(nullable = false)
  public String getExternalValue()
  {
    return externalValue;
  }

  @Override
  public void setExternalValue(final String externalValue)
  {
    this.externalValue = externalValue;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("externalSystem", externalSystem)
        .append("valueType", valueType)
        .append("value", value)
        .append("externalValue", externalValue)
    ;
  }
}
