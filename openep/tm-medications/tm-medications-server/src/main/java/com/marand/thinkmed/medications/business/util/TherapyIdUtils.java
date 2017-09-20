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

package com.marand.thinkmed.medications.business.util;

import java.util.UUID;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.util.OpenEhrRefUtils;
import org.openehr.jaxb.rm.Link;

/**
 * @author Mitja Lapajne
 */
public final class TherapyIdUtils
{
  //therapyId is compositionUid without version | instructionName
  private TherapyIdUtils()
  {
  }

  public static String createTherapyId(
      final MedicationOrderComposition composition,
      final MedicationInstructionInstruction instruction)
  {
    return createTherapyId(composition.getUid().getValue(), instruction.getName().getValue());
  }

  public static String createTherapyId(final MedicationOrderComposition composition)
  {
    return createTherapyId(
        composition.getUid().getValue(),
        composition.getMedicationDetail().getMedicationInstruction().get(0).getName().getValue());
  }

  public static String createTherapyId(final String compositionUid, final String instructionName)
  {
    Preconditions.checkNotNull(compositionUid);
    Preconditions.checkNotNull(instructionName);
    return getCompositionUidWithoutVersion(compositionUid) + '|' + instructionName;
  }

  public static String createTherapyId(@Nonnull final String compositionUid)
  {
    Preconditions.checkNotNull(compositionUid);
    return createTherapyId(compositionUid, "Medication instruction");
  }

  public static Pair<String, String> parseTherapyId(final String therapyId)
  {
    final int delimiterIndex = therapyId.indexOf('|');
    final String compositionUid = therapyId.substring(0, delimiterIndex);
    final String ehrOrderName = therapyId.substring(delimiterIndex + 1, therapyId.length());
    return Pair.of(compositionUid, ehrOrderName);
  }

  public static String getCompositionUidWithoutVersion(final String uid)
  {
    return hasVersion(uid)
           ? uid.substring(0, uid.indexOf("::"))
           : uid;
  }

  public static String getCompositionUidForPreviousVersion(final String uidWithVersion)
  {
    if (hasVersionNumber(uidWithVersion))
    {
      final Long version = getCompositionVersion(uidWithVersion);
      //noinspection ConstantConditions
      return version == 1 ? uidWithVersion : buildCompositionUid(uidWithVersion, version - 1);
    }
    throw new IllegalArgumentException("composition uid " + uidWithVersion + " has no version number!");
  }

  public static String getCompositionUidForFirstVersion(final String uidWithVersion)
  {
    if (hasVersionNumber(uidWithVersion))
    {
      return buildCompositionUid(uidWithVersion, 1L);
    }
    throw new IllegalArgumentException("composition uid " + uidWithVersion + " has no version number!");
  }

  public static Long getCompositionVersion(final String uid)
  {
    return hasVersion(uid)
           ? Long.valueOf(uid.substring(uid.lastIndexOf("::") + 2, uid.length()))
           : null;
  }

  private static boolean hasVersion(final String uid)
  {
    return uid != null && uid.contains("::");
  }

  private static boolean hasVersionNumber(final String uid)
  {
    if (uid != null)
    {
      final int first = uid.indexOf("::");
      final int last = uid.lastIndexOf("::");
      return first > -1 && first != last;
    }
    return false;
  }

  public static String buildCompositionUid(final String uid, final Long version)
  {
    return uid.substring(0, uid.lastIndexOf("::") + 2) + version;
  }

  public static String getTherapyIdFromLink(final Link link)
  {
    final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(link.getTarget().getValue());
    final String compositionId = getCompositionUidWithoutVersion(ehrUri.getCompositionId());
    return createTherapyId(compositionId, "Medication instruction");
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static boolean isValidTherapyId(final String therapyId)
  {
    if (therapyId == null || !therapyId.contains("|"))
    {
      return false;
    }
    try
    {
      final String uid = therapyId.substring(0, therapyId.indexOf('|'));
      UUID.fromString(uid);
    }
    catch (final IllegalArgumentException exception)
    {
      return false;
    }
    return true;
  }
}
