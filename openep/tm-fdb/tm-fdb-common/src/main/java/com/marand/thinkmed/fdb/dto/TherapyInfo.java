package com.marand.thinkmed.fdb.dto;

import java.util.ArrayList;
import java.util.List;

import firstdatabank.dif.DispensableDrug;

/**
 * @author Bostjan Vester
 */
public class TherapyInfo
{
  private List<DrugDosingInfo> dosingInfos = new ArrayList<DrugDosingInfo>();
  private long ageInDays;
  private Double weightInKg;
  private Integer gabInWeeks;
  private Double bsaInM2;
  private List<String> icd9Codes;

  public long getAgeInDays()
  {
    return ageInDays;
  }

  public void setAgeInDays(final long ageInDays)
  {
    this.ageInDays = ageInDays;
  }

  public List<DrugDosingInfo> getDosingInfos()
  {
    return dosingInfos;
  }

  public void setDosingInfos(final List<DrugDosingInfo> dosingInfos)
  {
    this.dosingInfos = dosingInfos;
  }

  public Double getWeightInKg()
  {
    return weightInKg;
  }

  public void setWeightInKg(final Double weightInKg)
  {
    this.weightInKg = weightInKg;
  }

  public Integer getGabInWeeks()
  {
    return gabInWeeks;
  }

  public void setGabInWeeks(final Integer gabInWeeks)
  {
    this.gabInWeeks = gabInWeeks;
  }

  public Double getBsaInM2()
  {
    return bsaInM2;
  }

  public void setBsaInM2(final Double bsaInM2)
  {
    this.bsaInM2 = bsaInM2;
  }

  public void addDosingInfo(final DrugDosingInfo dosingInfo)
  {
    dosingInfos.add(dosingInfo);
  }

  public DispensableDrug getDispensableDrug(final int dispensableDrugIndex)
  {
    return dosingInfos.get(dispensableDrugIndex).getDispensableDrug();
  }

  public void setIcd9Codes(final List<String> icd9Codes)
  {
    this.icd9Codes = icd9Codes;
  }

  public List<String> getIcd9Codes()
  {
    return icd9Codes;
  }
}
