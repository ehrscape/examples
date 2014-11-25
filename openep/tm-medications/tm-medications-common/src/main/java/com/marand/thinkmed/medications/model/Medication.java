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

package com.marand.thinkmed.medications.model;

import java.util.Set;
import java.util.SortedSet;

import com.marand.maf.core.data.entity.EffectiveEntity;
import com.marand.thinkmed.medications.MedicationLevelEnum;

/**
 * @author Mitja Lapajne
 * @author Klavdij Lapajne
 */
public interface Medication extends EffectiveEntity
{
  String getDescription();

  void setDescription(final String description);

  Long getVtmId();

  void setVtmId(final Long vtmId);

  Long getVmpId();

  void setVmpId(final Long vmpId);

  Long getAmpId();

  void setAmpId(final Long ampId);

  Long getVmppId();

  void setVmppId(final Long vmppId);

  Long getAmppId();

  void setAmppId(final Long amppId);

  Long getTfId();

  void setTfId(final Long tfId);

  MedicationLevelEnum getMedicationLevel();

  void setMedicationLevel(final MedicationLevelEnum medicationLevel);

  boolean isOrderable();

  void setOrderable(final boolean orderable);

  Integer getSortOrder();

  void setSortOrder(Integer sortOrder);

  Set<MedicationIngredientLink> getIngredients();

  void setIngredients(Set<MedicationIngredientLink> ingredients);

  Set<MedicationRouteLink> getRoutes();

  void setRoutes(Set<MedicationRouteLink> routes);

  Set<MedicationType> getTypes();

  void setTypes(Set<MedicationType> types);

  Set<MedicationCustomGroupMember> getCustomGroupMembers();

  void setCustomGroupMembers(Set<MedicationCustomGroupMember> customGroupMembers);

  SortedSet<MedicationVersion> getVersions();

  void setVersions(SortedSet<MedicationVersion> versions);

  void removeVersion(final MedicationVersion version);

  void addVersion(final MedicationVersion version);
}
