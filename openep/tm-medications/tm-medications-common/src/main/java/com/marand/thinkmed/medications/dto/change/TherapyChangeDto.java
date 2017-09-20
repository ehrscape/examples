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

package com.marand.thinkmed.medications.dto.change;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Igor Horvat
 * @author Mitja Lapajne
 */

public abstract class TherapyChangeDto<O, N> extends DataTransferObject implements JsonSerializable
{
  private final TherapyChangeType type;
  private N newValue;
  private O oldValue;

  protected TherapyChangeDto(@Nonnull final TherapyChangeType type)
  {
    Preconditions.checkNotNull(type, "type is required");
    this.type = type;
  }

  public TherapyChangeType getType()
  {
    return type;
  }

  public N getNewValue()
  {
    return newValue;
  }

  public void setNewValue(final N newValue)
  {
    this.newValue = newValue;
  }

  public O getOldValue()
  {
    return oldValue;
  }

  public void setOldValue(final O oldValue)
  {
    this.oldValue = oldValue;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("type", type)
        .append("newValue", newValue)
        .append("oldValue", oldValue);
  }
}


