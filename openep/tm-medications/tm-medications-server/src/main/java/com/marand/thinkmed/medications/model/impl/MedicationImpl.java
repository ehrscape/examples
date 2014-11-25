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
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.marand.maf.core.hibernate.entity.AbstractEffectiveEntity;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.model.Medication;
import com.marand.thinkmed.medications.model.MedicationCustomGroupMember;
import com.marand.thinkmed.medications.model.MedicationIngredientLink;
import com.marand.thinkmed.medications.model.MedicationRouteLink;
import com.marand.thinkmed.medications.model.MedicationType;
import com.marand.thinkmed.medications.model.MedicationVersion;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * @author Mitja Lapajne
 * @author Klavdij Lapajne
 */
@Entity
public class MedicationImpl extends AbstractEffectiveEntity implements Medication
{
  private Integer sortOrder;
  private String description;
  private Long vtmId; //Virtual Therapeutic Moiety
  private Long vmpId; //Virtual Medicinal Product
  private Long ampId; //Actual Medicinal Product
  private Long vmppId; //Virtual Medicinal Product Pack
  private Long amppId; //Actual Medicinal Product Pack
  private Long tfId; //Trade Family
  private MedicationLevelEnum medicationLevel;
  private boolean orderable;
  private Set<MedicationIngredientLink> ingredients = new HashSet<>();
  private Set<MedicationRouteLink> routes = new HashSet<>();
  private Set<MedicationType> types = new HashSet<>();
  private Set<MedicationCustomGroupMember> customGroupMembers = new HashSet<>();
  private SortedSet<MedicationVersion> versions = new TreeSet<>();

  @Override
  @Index(name = "xpMedicationSort")
  public Integer getSortOrder()
  {
    return sortOrder;
  }

  @Override
  public String getDescription()
  {
    return description;
  }

  @Override
  public void setDescription(final String description)
  {
    this.description = description;
  }

  @Override
  @Index(name = "xpMedicationVtm")
  public Long getVtmId()
  {
    return vtmId;
  }

  @Override
  public void setVtmId(final Long vtmId)
  {
    this.vtmId = vtmId;
  }

  @Override
  @Index(name = "xpMedicationVmp")
  public Long getVmpId()
  {
    return vmpId;
  }

  @Override
  public void setVmpId(final Long vmpId)
  {
    this.vmpId = vmpId;
  }

  @Override
  @Index(name = "xpMedicationAmp")
  public Long getAmpId()
  {
    return ampId;
  }

  @Override
  public void setAmpId(final Long ampId)
  {
    this.ampId = ampId;
  }

  @Override
  @Index(name = "xpMedicationVmpp")
  public Long getVmppId()
  {
    return vmppId;
  }

  @Override
  public void setVmppId(final Long vmppId)
  {
    this.vmppId = vmppId;
  }

  @Override
  @Index(name = "xpMedicationAmpp")
  public Long getAmppId()
  {
    return amppId;
  }

  @Override
  public void setAmppId(final Long amppId)
  {
    this.amppId = amppId;
  }

  @Override
  @Index(name = "xpMedicationTf")
  public Long getTfId()
  {
    return tfId;
  }

  @Override
  public void setTfId(final Long tfId)
  {
    this.tfId = tfId;
  }

  @Override
  @Enumerated(EnumType.STRING)
  @Index(name = "xpMedicationMedLevel")
  public MedicationLevelEnum getMedicationLevel()
  {
    return medicationLevel;
  }

  @Override
  public void setMedicationLevel(final MedicationLevelEnum medicationLevel)
  {
    this.medicationLevel = medicationLevel;
  }

  @Override
  @Index(name = "xpMedicationOrderable")
  public boolean isOrderable()
  {
    return orderable;
  }

  @Override
  public void setOrderable(final boolean orderable)
  {
    this.orderable = orderable;
  }

  @Override
  public void setSortOrder(final Integer sortOrder)
  {
    this.sortOrder = sortOrder;
  }

  @Override
  @OneToMany(targetEntity = MedicationIngredientLinkImpl.class, mappedBy = "medication", fetch = FetchType.LAZY)
  public Set<MedicationIngredientLink> getIngredients()
  {
    return ingredients;
  }

  @Override
  public void setIngredients(final Set<MedicationIngredientLink> ingredients)
  {
    this.ingredients = ingredients;
  }

  @Override
  @OneToMany(targetEntity = MedicationRouteLinkImpl.class, mappedBy = "medication", fetch = FetchType.LAZY)
  public Set<MedicationRouteLink> getRoutes()
  {
    return routes;
  }

  @Override
  public void setRoutes(final Set<MedicationRouteLink> routes)
  {
    this.routes = routes;
  }

  @Override
  @OneToMany(targetEntity = MedicationTypeImpl.class, mappedBy = "medication", fetch = FetchType.LAZY)
  public Set<MedicationType> getTypes()
  {
    return types;
  }

  @Override
  public void setTypes(final Set<MedicationType> types)
  {
    this.types = types;
  }

  @Override
  @OneToMany(targetEntity = MedicationCustomGroupMemberImpl.class, mappedBy = "medication", fetch = FetchType.LAZY)
  public Set<MedicationCustomGroupMember> getCustomGroupMembers()
  {
    return customGroupMembers;
  }

  @Override
  public void setCustomGroupMembers(final Set<MedicationCustomGroupMember> customGroupMembers)
  {
    this.customGroupMembers = customGroupMembers;
  }

  @Override
  @OneToMany(targetEntity = MedicationVersionImpl.class, mappedBy = "medication")
  @Sort(type = SortType.NATURAL)
  public SortedSet<MedicationVersion> getVersions()
  {
    return versions;
  }

  @Override
  public void setVersions(final SortedSet<MedicationVersion> versions)
  {
    this.versions = versions;
  }

  @Override
  public void addVersion(final MedicationVersion version)
  {
    SortedSet<MedicationVersion> medicationVersions = getVersions();
    if (medicationVersions == null)
    {
      medicationVersions = new TreeSet<MedicationVersion>();
      versions = medicationVersions;
    }

    version.setMedication(this);
    medicationVersions.add(version);
  }

  @Override
  public void removeVersion(final MedicationVersion version)
  {
    final Set<MedicationVersion> medicationVersions = getVersions();

    if (medicationVersions != null && medicationVersions.remove(version))
    {
      version.setDeleted(true);
    }
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("sortOrder", sortOrder)
        .append("description", description)
        .append("vtmId", vtmId)
        .append("vmpId", vmpId)
        .append("ampId", ampId)
        .append("vmppId", vmppId)
        .append("amppId", amppId)
        .append("tfId", ingredients)
        .append("medicationLevel", medicationLevel)
        .append("ingredients", ingredients)
        .append("routes", routes)
        .append("types", types)
        .append("customGroupMembers", customGroupMembers)
        .append("versions", versions)
    ;
  }
}
