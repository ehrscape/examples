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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import com.marand.thinkmed.medications.model.TherapyTemplate;
import com.marand.thinkmed.medications.model.TherapyTemplateElement;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

/**
 * @author Mitja Lapajne
 */
@Entity
public class TherapyTemplateElementImpl extends AbstractPermanentEntity implements TherapyTemplateElement
{
  private TherapyTemplate therapyTemplate;
  private String therapy;
  private Boolean completed;

  @Override
  @ManyToOne(targetEntity = TherapyTemplateImpl.class, fetch = FetchType.LAZY, optional = false)
  @Index(name = "xfTherapyTempMemberTherapyTemp")
  public TherapyTemplate getTherapyTemplate()
  {
    return therapyTemplate;
  }

  @Override
  public void setTherapyTemplate(final TherapyTemplate therapyTemplate)
  {
    this.therapyTemplate = therapyTemplate;
  }

  @Override
  @Lob
  public String getTherapy()
  {
    return therapy;
  }

  @Override
  public void setTherapy(final String therapy)
  {
    this.therapy = therapy;
  }

  @Override
  @Column(nullable = false)
  public Boolean getCompleted()
  {
    return completed;
  }

  @Override
  public void setCompleted(final Boolean completed)
  {
    this.completed = completed;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("therapyTemplate", therapyTemplate)
        .append("therapy", therapy);
  }
}
