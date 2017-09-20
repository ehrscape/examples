package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbWarningDto implements JsonSerializable
{
  private String FullAlertMessage;
  private FdbNameValue AlertSeverity;
  private FdbNameId PrimaryDrug;
  private FdbNameId SecondaryDrug;

  public String getFullAlertMessage()
  {
    return FullAlertMessage;
  }

  public void setFullAlertMessage(final String fullAlertMessage)
  {
    FullAlertMessage = fullAlertMessage;
  }

  public FdbNameValue getAlertSeverity()
  {
    return AlertSeverity;
  }

  public void setAlertSeverity(final FdbNameValue alertSeverity)
  {
    AlertSeverity = alertSeverity;
  }

  public FdbNameId getPrimaryDrug()
  {
    return PrimaryDrug;
  }

  public void setPrimaryDrug(final FdbNameId primaryDrug)
  {
    PrimaryDrug = primaryDrug;
  }

  public FdbNameId getSecondaryDrug()
  {
    return SecondaryDrug;
  }

  public void setSecondaryDrug(final FdbNameId secondaryDrug)
  {
    SecondaryDrug = secondaryDrug;
  }
}
