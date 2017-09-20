package com.marand.thinkmed.medications;

/**
 * @author Nejc Korasa
 */
public enum MedicationJobPerformerEnum
{
  AUTOMATIC_CHARTING_PERFORMER("automatic_charting_performer", "100");

  private final String code;
  private final String id;

  MedicationJobPerformerEnum(final String code, final String id)
  {
    this.code = code;
    this.id = id;
  }

  public String getCode()
  {
    return code;
  }

  public String getId()
  {
    return id;
  }
}
