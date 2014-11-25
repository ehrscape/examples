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

package com.marand.thinkmed.api.core.data.object;

import com.marand.thinkmed.api.core.data.Identifiable;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class IdentityDto extends Dto implements Identifiable
{
  private long id;

  public IdentityDto()
  {
  }

  public IdentityDto(final long id)
  {
    this.id = id;
  }

  @Override
  public long id()
  {
    return id;
  }

  public void setId(final long id)
  {
    this.id = id;
  }

  @Override
  protected void appendToString(ToStringBuilder tsb)
  {
    tsb.append("id", id);
  }

  @Override
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (!(o instanceof IdentityDto))
    {
      return false;
    }

    final IdentityDto that = (IdentityDto)o;

    return id == that.id;
  }

  @Override
  public int hashCode()
  {
    if (id > 0)
    {
      final HashCodeBuilder builder = new HashCodeBuilder();
      builder.append(id);

      return builder.toHashCode();
    }
    else
    {
      return super.hashCode();
    }
  }
}
