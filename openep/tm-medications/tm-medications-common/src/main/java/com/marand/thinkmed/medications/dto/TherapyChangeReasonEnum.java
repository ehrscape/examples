package com.marand.thinkmed.medications.dto;

/**
 * @author Mitja Lapajne
 */
public enum TherapyChangeReasonEnum
{
  TEMPORARY_LEAVE;

  public String toFullString()
  {
    return TherapyChangeReasonEnum.class.getSimpleName() + '.' + name();
  }

  public static TherapyChangeReasonEnum fromFullString(final String fullString)
  {
    for (final TherapyChangeReasonEnum therapyChangeReasonEnum : values())
    {
      if (therapyChangeReasonEnum.toFullString().equals(fullString))
      {
        return therapyChangeReasonEnum;
      }
    }
    return null;
  }
}
