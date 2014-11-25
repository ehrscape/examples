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

package com.marand.thinkmed.medications.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.JsonSerializable;
import com.marand.maf.core.data.object.DataObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class MedicationSearchDto extends DataObject implements JsonSerializable
{
  private String title;
  private Long key;
  private boolean isFolder;
  private boolean expand;
  private boolean unselectable;
  private Long parentId;
  private MedicationSimpleDto medication;
  private List<MedicationSearchDto> sublevelMedications = new ArrayList<>(); //stays the same
  private List<MedicationSearchDto> children = new ArrayList<>(); //changed by filter

  public String getTitle()
  {
    return title;
  }

  public void setTitle(final String title)
  {
    this.title = title;
  }

  public Long getKey()
  {
    return key;
  }

  public void setKey(final Long key)
  {
    this.key = key;
  }

  public boolean isFolder()
  {
    return isFolder;
  }

  public void setFolder(final boolean isFolder)
  {
    this.isFolder = isFolder;
  }

  public boolean isExpand()
  {
    return expand;
  }

  public void setExpand(final boolean expand)
  {
    this.expand = expand;
  }

  public boolean isUnselectable()
  {
    return unselectable;
  }

  public void setUnselectable(final boolean unselectable)
  {
    this.unselectable = unselectable;
  }

  public Long getParentId()
  {
    return parentId;
  }

  public void setParentId(final Long parentId)
  {
    this.parentId = parentId;
  }

  public MedicationSimpleDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationSimpleDto medication)
  {
    this.medication = medication;
  }

  public List<MedicationSearchDto> getSublevelMedications()
  {
    return sublevelMedications;
  }

  public void setSublevelMedications(final List<MedicationSearchDto> sublevelMedications)
  {
    this.sublevelMedications = sublevelMedications;
  }

  public List<MedicationSearchDto> getChildren()
  {
    return children;
  }

  public void setChildren(final List<MedicationSearchDto> children)
  {
    this.children = children;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("title", title)
        .append("key", key)
        .append("isFolder", isFolder)
        .append("expand", expand)
        .append("unselectable", unselectable)
        .append("parentId", parentId)
        .append("medication", medication)
        .append("sublevelMedications", sublevelMedications)
        .append("children", children)
    ;
  }
}