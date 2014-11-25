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

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.model.TherapyTemplate;
import com.marand.thinkmed.medications.model.TherapyTemplateElement;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

/**
 * @author Mitja Lapajne
 */
@Entity
public class TherapyTemplateImpl extends AbstractPermanentEntity implements TherapyTemplate
{
  private String name;
  private TherapyTemplateTypeEnum type;
  private Long userId;
  private Long departmentId;
  private Long patientId;
  private Set<TherapyTemplateElement> therapyTemplateElements = new HashSet<>();

  @Override
  @Column(nullable = false)
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
  @Enumerated(EnumType.STRING)
  public TherapyTemplateTypeEnum getType()
  {
    return type;
  }

  @Override
  public void setType(final TherapyTemplateTypeEnum type)
  {
    this.type = type;
  }

  @Override
  public Long getUserId()
  {
    return userId;
  }

  @Override
  public void setUserId(final Long userId)
  {
    this.userId = userId;
  }

  @Override
  @Index(name = "xfTherapyTemplateDepartment")
  public Long getDepartmentId()
  {
    return departmentId;
  }

  @Override
  public void setDepartmentId(final Long departmentId)
  {
    this.departmentId = departmentId;
  }

  @Override
  @Index(name = "xfTherapyTemplatePatient")
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
  @OneToMany(targetEntity = TherapyTemplateElementImpl.class, mappedBy = "therapyTemplate", fetch = FetchType.LAZY)
  public Set<TherapyTemplateElement> getTherapyTemplateElements()
  {
    return therapyTemplateElements;
  }

  @Override
  public void setTherapyTemplateElements(final Set<TherapyTemplateElement> therapyTemplateElements)
  {
    this.therapyTemplateElements = therapyTemplateElements;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("name", name);
    tsb.append("userId", userId);
    tsb.append("departmentId", departmentId);
    tsb.append("patientId", patientId);
    tsb.append("therapyTemplateElements", therapyTemplateElements);
  }
}
