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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;

import com.marand.maf.core.hibernate.entity.AbstractEffectiveCatalogEntity;
import com.marand.thinkmed.medications.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.model.MedicationRoute;
import com.marand.thinkmed.medications.model.MedicationRouteRelation;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
public class MedicationRouteImpl extends AbstractEffectiveCatalogEntity implements MedicationRoute
{
  private String shortName;
  private MedicationRouteTypeEnum type;
  private Integer sortOrder;
  private Set<MedicationRouteRelation> parentRelations = new HashSet<>();
  private Set<MedicationRouteRelation> childRelations = new HashSet<>();

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
  @Enumerated(EnumType.STRING)
  public MedicationRouteTypeEnum getType()
  {
    return type;
  }

  @Override
  public void setType(final MedicationRouteTypeEnum type)
  {
    this.type = type;
  }

  @Override
  public Integer getSortOrder()
  {
    return sortOrder;
  }

  @Override
  public void setSortOrder(final Integer sortOrder)
  {
    this.sortOrder = sortOrder;
  }

  @Override
  @OneToMany(targetEntity = MedicationRouteRelationImpl.class, mappedBy = "childRoute")
  public Set<MedicationRouteRelation> getParentRelations()
  {
    return parentRelations;
  }

  @Override
  public void setParentRelations(final Set<MedicationRouteRelation> parentRelations)
  {
    this.parentRelations = parentRelations;
  }

  @Override
  @OneToMany(targetEntity = MedicationRouteRelationImpl.class, mappedBy = "parentRoute")
  public Set<MedicationRouteRelation> getChildRelations()
  {
    return childRelations;
  }

  @Override
  public void setChildRelations(final Set<MedicationRouteRelation> childRelations)
  {
    this.childRelations = childRelations;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("shortName", shortName)
        .append("type", type)
        .append("sortOrder", sortOrder)
        .append("parentRelations", parentRelations)
        .append("childRelations", childRelations);
  }
}
