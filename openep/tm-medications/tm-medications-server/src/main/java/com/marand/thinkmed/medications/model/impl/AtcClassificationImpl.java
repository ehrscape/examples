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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.marand.maf.core.hibernate.entity.AbstractEffectiveCatalogEntity;
import com.marand.thinkmed.medications.model.AtcClassification;
import org.apache.commons.lang3.builder.ToStringBuilder;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xfAtcClassificationParent", columnList = "parent_id"),
    @Index(name = "xfAtcClassificationTopParent", columnList = "top_parent_id")})
public class AtcClassificationImpl extends AbstractEffectiveCatalogEntity implements AtcClassification
{
  private AtcClassification parent;
  private AtcClassification topParent;
  private int depth;
  private boolean leaf;
  private Set<AtcClassification> children = new HashSet<>();

  @Override
  @ManyToOne(targetEntity = AtcClassificationImpl.class, fetch = FetchType.LAZY)
  public AtcClassification getParent()
  {
    return parent;
  }

  @Override
  public void setParent(final AtcClassification parent)
  {
    this.parent = parent;
  }

  @Override
  @ManyToOne(targetEntity = AtcClassificationImpl.class, fetch = FetchType.LAZY)
  public AtcClassification getTopParent()
  {
    return topParent;
  }

  @Override
  public void setTopParent(final AtcClassification topParent)
  {
    this.topParent = topParent;
  }

  @Override
  public int getDepth()
  {
    return depth;
  }

  @Override
  public void setDepth(final int depth)
  {
    this.depth = depth;
  }

  @Override
  public boolean isLeaf()
  {
    return leaf;
  }

  @Override
  public void setLeaf(final boolean leaf)
  {
    this.leaf = leaf;
  }

  @OneToMany(targetEntity = AtcClassificationImpl.class, mappedBy = "parent", fetch = FetchType.LAZY)
  public Set<AtcClassification> getChildren()
  {
    return children;
  }

  public void setChildren(final Set<AtcClassification> children)
  {
    this.children = children;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("parent", parent)
        .append("topParent", topParent)
        .append("depth", depth)
        .append("leaf", leaf)
        .append("children", children)
    ;
  }
}
