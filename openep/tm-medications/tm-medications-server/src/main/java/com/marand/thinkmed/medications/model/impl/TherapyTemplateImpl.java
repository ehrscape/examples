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
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.model.TherapyTemplate;
import com.marand.thinkmed.medications.model.TherapyTemplateElement;
import org.apache.commons.lang3.builder.ToStringBuilder;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xpTherapyTemplateCareProvider", columnList = "care_provider_id"),
    @Index(name = "xfTherapyTemplatePatient", columnList = "patient_id")})
public class TherapyTemplateImpl extends AbstractPermanentEntity implements TherapyTemplate
{
  private String name;
  private TherapyTemplateTypeEnum type;
  private TherapyTemplateModeEnum templateMode;
  private String userId;
  private String careProviderId;
  private String patientId;
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
  @Enumerated(EnumType.STRING)
  public TherapyTemplateModeEnum getTemplateMode()
  {
    return templateMode;
  }

  @Override
  public void setTemplateMode(final TherapyTemplateModeEnum templateMode)
  {
    this.templateMode = templateMode;
  }

  @Override
  public String getUserId()
  {
    return userId;
  }

  @Override
  public void setUserId(final String userId)
  {
    this.userId = userId;
  }

  @Override
  public String getCareProviderId()
  {
    return careProviderId;
  }

  @Override
  public void setCareProviderId(final String careProviderId)
  {
    this.careProviderId = careProviderId;
  }

  @Override
  public String getPatientId()
  {
    return patientId;
  }

  @Override
  public void setPatientId(final String patientId)
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
    tsb.append("type", type);
    tsb.append("templateMode", templateMode);
    tsb.append("userId", userId);
    tsb.append("careProviderId", careProviderId);
    tsb.append("patientId", patientId);
    tsb.append("therapyTemplateElements", therapyTemplateElements);
  }
}
