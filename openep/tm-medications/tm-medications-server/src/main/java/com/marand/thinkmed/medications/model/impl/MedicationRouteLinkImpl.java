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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.maf.core.hibernate.entity.AbstractEffectiveEntity;
import com.marand.thinkmed.medications.dto.BnfMaximumUnitType;
import com.marand.thinkmed.medications.model.Medication;
import com.marand.thinkmed.medications.model.MedicationRoute;
import com.marand.thinkmed.medications.model.MedicationRouteLink;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.ColumnDefault;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedicationRouteLinkRoute", columnList = "route_id"),
    @Index(name = "xfMedicationRouteLinkMed", columnList = "medication_id")})
public class MedicationRouteLinkImpl extends AbstractEffectiveEntity implements MedicationRouteLink
{
  private MedicationRoute route;
  private Medication medication;
  private boolean defaultRoute;
  private boolean discretionary;
  private Boolean unlicensed;
  private Integer bnfMaximum;
  private BnfMaximumUnitType bnfMaximumUnitType;

  @Override
  @ManyToOne(targetEntity = MedicationRouteImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationRoute getRoute()
  {
    return route;
  }

  @Override
  public void setRoute(final MedicationRoute route)
  {
    this.route = route;
  }

  @Override
  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY, optional = false)
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
  @ColumnDefault("0")
  public boolean isDiscretionary()
  {
    return discretionary;
  }

  @Override
  public void setDiscretionary(final boolean discretionary)
  {
    this.discretionary = discretionary;
  }

  @Override
  public Boolean getUnlicensed()
  {
    return unlicensed;
  }

  @Override
  public void setUnlicensed(final Boolean unlicensed)
  {
    this.unlicensed = unlicensed;
  }

  @Override
  public Integer getBnfMaximum()
  {
    return bnfMaximum;
  }

  @Override
  public void setBnfMaximum(final Integer bnfMaximum)
  {
    this.bnfMaximum = bnfMaximum;
  }

  @Override
  @Enumerated(EnumType.STRING)
  public BnfMaximumUnitType getBnfMaximumUnitType()
  {
    return bnfMaximumUnitType;
  }

  @Override
  public void setBnfMaximumUnitType(final BnfMaximumUnitType bnfMaximumUnitType)
  {
    this.bnfMaximumUnitType = bnfMaximumUnitType;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("administrationRoute", route)
        .append("medication", medication)
        .append("defaultRoute", defaultRoute)
        .append("discretionary", discretionary)
        .append("unlicensed", unlicensed)
        .append("bnfMaximum", bnfMaximum)
        .append("bnfMaximumUnitType", bnfMaximumUnitType)
    ;
  }
}
