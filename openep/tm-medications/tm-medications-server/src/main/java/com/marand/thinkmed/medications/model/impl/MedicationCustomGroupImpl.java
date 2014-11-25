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

import com.marand.maf.core.hibernate.entity.AbstractCatalogEntity;
import com.marand.thinkmed.medications.model.MedicationCustomGroup;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

/**
 * @author Mitja Lapajne
 */
@Entity
public class MedicationCustomGroupImpl extends AbstractCatalogEntity implements MedicationCustomGroup
{
  private String organizationalEntityName;
  private Integer sortOrder;

  @Override
  @Column(nullable = false)
  @Index(name = "xpMedCustomGroupOrg")
  public String getOrganizationalEntityName()
  {
    return organizationalEntityName;
  }

  @Override
  public void setOrganizationalEntityName(final String organizationalEntityName)
  {
    this.organizationalEntityName = organizationalEntityName;
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
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("organizationalEntityName", organizationalEntityName);
  }
}
