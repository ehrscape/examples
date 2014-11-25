package com.marand.thinkmed.fdb.dto;

import com.marand.maf.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbDrugSensitivityWarningDto implements JsonSerializable
{
  private String FullAlertMessage;
  private FdbNameId Allergen;
  private FdbNameId Drug;

  public String getFullAlertMessage()
  {
    return FullAlertMessage;
  }

  public void setFullAlertMessage(final String fullAlertMessage)
  {
    FullAlertMessage = fullAlertMessage;
  }

  public FdbNameId getAllergen()
  {
    return Allergen;
  }

  public void setAllergen(final FdbNameId allergen)
  {
    Allergen = allergen;
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
