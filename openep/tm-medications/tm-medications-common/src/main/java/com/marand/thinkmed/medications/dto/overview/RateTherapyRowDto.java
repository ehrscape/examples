package com.marand.thinkmed.medications.dto.overview;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class RateTherapyRowDto extends TherapyRowDto
{
  private Double currentInfusionRate;
  private Double infusionRateAtIntervalStart;
  private Double infusionFormulaAtIntervalStart;
  private Double lastPositiveInfusionRate;
  private boolean infusionActive;

  private String rateUnit;
  private String formulaUnit;

  public Double getCurrentInfusionRate()
  {
    return currentInfusionRate;
  }

  public void setCurrentInfusionRate(final Double currentInfusionRate)
  {
    this.currentInfusionRate = currentInfusionRate;
  }

  public Double getInfusionRateAtIntervalStart()
  {
    return infusionRateAtIntervalStart;
  }

  public void setInfusionRateAtIntervalStart(final Double infusionRateAtIntervalStart)
  {
    this.infusionRateAtIntervalStart = infusionRateAtIntervalStart;
  }

  public Double getLastPositiveInfusionRate()
  {
    return lastPositiveInfusionRate;
  }

  public void setLastPositiveInfusionRate(final Double lastPositiveInfusionRate)
  {
    this.lastPositiveInfusionRate = lastPositiveInfusionRate;
  }

  public boolean isInfusionActive()
  {
    return infusionActive;
  }

  public void setInfusionActive(final boolean infusionActive)
  {
    this.infusionActive = infusionActive;
  }

  public String getRateUnit()
  {
    return rateUnit;
  }

  public void setRateUnit(final String rateUnit)
  {
    this.rateUnit = rateUnit;
  }

  public void setInfusionFormulaAtIntervalStart(final Double infusionFormulaAtIntervalStart)
  {
    this.infusionFormulaAtIntervalStart = infusionFormulaAtIntervalStart;
  }

  public void setFormulaUnit(final String formulaUnit)
  {
    this.formulaUnit = formulaUnit;
  }

  public Double getInfusionFormulaAtIntervalStart()
  {
    return infusionFormulaAtIntervalStart;
  }

  public String getFormulaUnit()
  {
    return formulaUnit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("currentInfusionRate", currentInfusionRate)
        .append("infusionRateAtIntervalStart", infusionRateAtIntervalStart)
        .append("infusionFormulaAtIntervalStart", infusionFormulaAtIntervalStart)
        .append("lastPositiveInfusionRate", lastPositiveInfusionRate)
        .append("infusionActive", infusionActive)
        .append("rateUnit", rateUnit)
        .append("formulaUnit", formulaUnit)
    ;
  }
}
