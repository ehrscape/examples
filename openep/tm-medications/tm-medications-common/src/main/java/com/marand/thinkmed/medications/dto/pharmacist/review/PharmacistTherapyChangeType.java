package com.marand.thinkmed.medications.dto.pharmacist.review;

/**
 * @author Mitja Lapajne
 */
public enum PharmacistTherapyChangeType
{
  NONE, EDIT, ABORT, SUSPEND;

  public static String getFullString(final PharmacistTherapyChangeType pharmacistTherapyChangeType)
  {
    return PharmacistTherapyChangeType.class.getSimpleName() + '.' + pharmacistTherapyChangeType.name();
  }

  public static PharmacistTherapyChangeType getByFullString(final String fullString)
  {
    for (final PharmacistTherapyChangeType pharmacistTherapyChangeType : values())
    {
      if (getFullString(pharmacistTherapyChangeType).equals(fullString))
      {
        return pharmacistTherapyChangeType;
      }
    }
    return null;
  }
}
