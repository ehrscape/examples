package com.marand.thinkmed.medications;

import com.marand.maf.core.Pair;

/**
 * @author Nejc Korasa
 */
public enum TherapySourceGroupEnum
{
  // SOURCES for creating medication on admission compositions
  LAST_HOSPITALIZATION("LAST_HOSPITALIZATION_"),
  LAST_DISCHARGE_MEDICATIONS("LAST_DISCHARGE_MEDICATIONS_"),

  // SOURCES for creating medication on discharge compositions
  MEDICATION_ON_ADMISSION("MEDICATION_ON_ADMISSION_"),
  INPATIENT_THERAPIES("INPATIENT_THERAPIES_");

  private final String prefix;

  TherapySourceGroupEnum(final String prefix)
  {
    this.prefix = prefix;
  }

  public String getPrefix()
  {
    return prefix;
  }

  public static String createTherapyTagSourceId(final TherapySourceGroupEnum groupEnum, final String compositionId)
  {
    final StringBuilder builder = new StringBuilder();
    builder.append(groupEnum.getPrefix()).append(compositionId);
    return builder.toString();
  }

  public static Pair<TherapySourceGroupEnum, String> getSourceGroupEnumAndSourceIdFromTherapyTag(
      final String therapyTag)
  {
    TherapySourceGroupEnum groupEnum = null;
    if (therapyTag.startsWith(LAST_HOSPITALIZATION.getPrefix()))
    {
      groupEnum = LAST_HOSPITALIZATION;
    }
    else if (therapyTag.startsWith(MEDICATION_ON_ADMISSION.getPrefix()))
    {
      groupEnum = MEDICATION_ON_ADMISSION;
    }
    else if (therapyTag.startsWith(INPATIENT_THERAPIES.getPrefix()))
    {
      groupEnum = INPATIENT_THERAPIES;
    }
    else if (therapyTag.startsWith(LAST_DISCHARGE_MEDICATIONS.getPrefix()))
    {
      groupEnum = LAST_DISCHARGE_MEDICATIONS;
    }
    return groupEnum == null ? null : Pair.of(groupEnum, therapyTag.replaceFirst(groupEnum.getPrefix(), ""));
  }
}
