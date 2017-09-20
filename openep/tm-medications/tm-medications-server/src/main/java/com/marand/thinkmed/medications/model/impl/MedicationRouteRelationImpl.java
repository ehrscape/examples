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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.marand.maf.core.hibernate.entity.AbstractEffectiveEntity;
import com.marand.thinkmed.medications.model.MedicationRoute;
import com.marand.thinkmed.medications.model.MedicationRouteRelation;
import org.apache.commons.lang3.builder.ToStringBuilder;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedicationRouteRelationPar", columnList = "parent_route_id"),
    @Index(name = "xfMedicationRouteRelationChil", columnList = "child_route_id")})
public class MedicationRouteRelationImpl extends AbstractEffectiveEntity implements MedicationRouteRelation
{
  private MedicationRoute parentRoute;
  private MedicationRoute childRoute;
  private boolean defaultRoute;

  @Override
  @ManyToOne(targetEntity = MedicationRouteImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationRoute getParentRoute()
  {
    return parentRoute;
  }

  @Override
  public void setParentRoute(final MedicationRoute parentRoute)
  {
    this.parentRoute = parentRoute;
  }

  @Override
  @ManyToOne(targetEntity = MedicationRouteImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationRoute getChildRoute()
  {
    return childRoute;
  }

  @Override
  public void setChildRoute(final MedicationRoute childRoute)
  {
    this.childRoute = childRoute;
  }

  @Override
  public boolean isDefaultRoute()
  {
    return defaultRoute;
  }

  @Override
  public void setDefaultRoute(final boolean defaultRoute)
  {
    this.defaultRoute = defaultRoute;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("parentRoute", parentRoute)
        .append("childRoute", childRoute)
        .append("defaultRoute", defaultRoute)
    ;
  }
}
