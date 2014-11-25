package com.marand.thinkmed.fdb.dto;

import firstdatabank.dif.DispensableDrug;

/**
 * @author Bostjan Vester
 */
public class DrugDosingInfo
{
  private DispensableDrug dispensableDrug;
  private double singleDoseAmount;
  private String doseUnit;
  private String frequency;
  private String route;
  private boolean onlyOnce;
  private boolean prospective;
  private long duration;

  public DispensableDrug getDispensableDrug()
  {
    return dispensableDrug;
  }

  public void setDispensableDrug(final DispensableDrug dispensableDrug)
  {
    this.dispensableDrug = dispensableDrug;
  }

  public double getSingleDoseAmount()
  {
    return singleDoseAmount;
  }

  public void setSingleDoseAmount(final double singleDoseAmount)
  {
    this.singleDoseAmount = singleDoseAmount;
  }

  public String getDoseUnit()
  {
    return doseUnit;
  }

  public void setDoseUnit(final String doseUnit)
  {
    this.doseUnit = doseUnit;
  }

  public String getFrequency()
  {
    return frequency;
  }

  public void setFrequency(final String frequency)
  {
    this.frequency = frequency;
  }

  public String getRoute()
  {
    return route;
  }

  public void setRoute(final String route)
  {
    this.route = route;
  }

  public long getDuration()
  {
    return duration;
  }

  public void setDuration(final long duration)
  {
    this.duration = duration;
  }

  public boolean isOnlyOnce()
  {
    return onlyOnce;
  }

  public void setOnlyOnce(final boolean onlyOnce)
  {
    this.onlyOnce = onlyOnce;
  }

  public boolean isProspective()
  {
    return prospective;
  }

  public void setProspective(final boolean prospective)
  {
    this.prospective = prospective;
  }

  public enum Rate
  {
    DAY
  }
}
