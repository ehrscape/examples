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

import com.marand.maf.core.data.object.DataObject;
import com.marand.maf.core.formatter.Displayable;
import com.marand.maf.core.formatter.DisplayableFormatters;
import com.marand.maf.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Igor Horvat
 */

public class TherapyChangeHistoryDto extends DataObject implements JsonSerializable
{
  @Displayable({DisplayableFormatters.ShortDate.class, DisplayableFormatters.ShortTime.class})
  private DateTime changeTime;
  private String editor;
  private List<TherapyChangeDto> changes = new ArrayList<>();

  public DateTime getChangeTime()
  {
    return changeTime;
  }

  public void setChangeTime(final DateTime changeTime)
  {
    this.changeTime = changeTime;
  }

  public String getEditor()
  {
    return editor;
  }

  public void setEditor(final String editor)
  {
    this.editor = editor;
  }

  public List<TherapyChangeDto> getChanges()
  {
    return changes;
  }

  public void setChanges(final List<TherapyChangeDto> changes)
  {
    this.changes = changes;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("changeTime", changeTime)
        .append("editor", editor)
        .append("changes", changes);
  }
}