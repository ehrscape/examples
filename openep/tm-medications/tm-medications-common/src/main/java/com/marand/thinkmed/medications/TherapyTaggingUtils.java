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

package com.marand.thinkmed.medications;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * User: MihaA
 */

public class TherapyTaggingUtils
{
  private TherapyTaggingUtils()
  {
  }

  public static String generateTag(final TherapyTagEnum therapyTagEnum, @Nonnull final String centralCaseId)
  {
    Preconditions.checkNotNull(centralCaseId, "centralCaseId");
    return therapyTagEnum.getPrefix() + centralCaseId;
  }

  public static String generateSourceTag(@Nonnull final String sourceId)
  {
    Preconditions.checkNotNull(sourceId, "sourceId");
    return TherapyTagEnum.SOURCE.getPrefix() + sourceId;
  }

  public static String getTherapySourceIdFromTag(final String therapyTag)
  {
    final String prefix = TherapyTagEnum.SOURCE.getPrefix();
    return therapyTag.startsWith(prefix) ? therapyTag.replaceFirst(prefix, "") : null;
  }

  public static String createGroupUUIdTag(@Nonnull final String groupUUId)
  {
    Preconditions.checkNotNull(groupUUId, "groupUUId");
    return TherapyTagEnum.GROUP_UUID.getPrefix() + groupUUId;
  }

  public static String getGroupUUIdFromTag(final String tag)
  {
    final String prefix = TherapyTagEnum.GROUP_UUID.getPrefix();
    return tag.startsWith(prefix) ? tag.replaceFirst(prefix, "") : null;
  }

}
