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

package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class AllergyDto extends DataTransferObject implements JsonSerializable
{
  private final NamedExternalDto allergen;
  private final String reaction;
  private final String comment;

  public AllergyDto(final NamedExternalDto allergen, final String reaction, final String comment)
  {
    this.allergen = allergen;
    this.reaction = reaction;
    this.comment = comment;
  }

  public NamedExternalDto getAllergen()
  {
    return allergen;
  }

  public String getReaction()
  {
    return reaction;
  }

  public String getComment()
  {
    return comment;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("allergen", allergen)
        .append("reaction", reaction)
        .append("comment", comment);
  }
}
