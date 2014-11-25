package com.marand.thinkmed.fdb.dto;

import com.marand.maf.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbPatientChecksWarningDto implements JsonSerializable
{
  private String FullAlertMessage;
  private FdbNameId Drug;

  public String getFullAlertMessage()
  {
    return FullAlertMessage;
  }

  public void setFullAlertMessage(final String fullAlertMessage)
  {
    FullAlertMessage = fullAlertMessage;
  }

  public FdbNameId getDrug()
  {
    return Drug;
  }

  public void setDrug(final FdbNameId drug)
  {
    Drug = drug;
  }
}
