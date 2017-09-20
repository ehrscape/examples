package com.marand.thinkmed.medications;

/**
 * @author nejck
 */
public enum ParticipationTypeEnum
{
  PRESCRIBER("prescriber"),
  WITNESS("witness");

  private final String code;

  ParticipationTypeEnum(final String code)
  {
    this.code = code;
  }

  public String getCode()
  {
    return code;
  }

}
