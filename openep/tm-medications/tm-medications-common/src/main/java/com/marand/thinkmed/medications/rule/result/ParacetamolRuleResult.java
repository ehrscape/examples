package com.marand.thinkmed.medications.rule.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public class ParacetamolRuleResult implements RuleResult, Serializable
{
  private String rule;
  private boolean quantityOk;
  private boolean betweenDosesTimeOk;
  private Double adultRulePercentage;
  private Double underageRulePercentage;
  private List<NamedExternalDto> medications = new ArrayList<>();

  private DateTime lastTaskTimestamp;
  private boolean lastTaskAdministered;

  private String errorMessage;

  @Override
  public String getRule()
  {
    return rule;
  }

  @Override
  public void setRule(final String rule)
  {
    this.rule = rule;
  }

  public boolean isQuantityOk()
  {
    return quantityOk;
  }

  public void setQuantityOk(final boolean quantityOk)
  {
    this.quantityOk = quantityOk;
  }

  public boolean isBetweenDosesTimeOk()
  {
    return betweenDosesTimeOk;
  }

  public void setBetweenDosesTimeOk(final boolean betweenDosesTimeOk)
  {
    this.betweenDosesTimeOk = betweenDosesTimeOk;
  }

  public DateTime getLastTaskTimestamp()
  {
    return lastTaskTimestamp;
  }

  public void setLastTaskTimestamp(final DateTime lastTaskTimestamp)
  {
    this.lastTaskTimestamp = lastTaskTimestamp;
  }


  public List<NamedExternalDto> getMedications()
  {
    return medications;
  }

  public void setMedications(final List<NamedExternalDto> medications)
  {
    this.medications = medications;
  }

  public Double getAdultRulePercentage()
  {
    return adultRulePercentage;
  }

  public Double getUnderageRulePercentage()
  {
    return underageRulePercentage;
  }

  public void setUnderageRulePercentage(final Double underageRulePercentage)
  {
    this.underageRulePercentage = underageRulePercentage;
  }

  public boolean isLastTaskAdministered()
  {
    return lastTaskAdministered;
  }

  public void setLastTaskAdministered(final boolean lastTaskAdministered)
  {
    this.lastTaskAdministered = lastTaskAdministered;
  }

  @Override
  public String getErrorMessage()
  {
    return errorMessage;
  }

  @Override
  public void setErrorMessage(final String errorMessage)
  {
    this.errorMessage = errorMessage;
  }

  public void setAdultRulePercentage(final Double adultRulePercentage)
  {
    this.adultRulePercentage = adultRulePercentage;
  }

  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("rule", rule)
        .append("quantityOk", quantityOk)
        .append("betweenDosesTimeOk", betweenDosesTimeOk)
        .append("medications", medications)
        .append("adultRulePercentage", adultRulePercentage)
        .append("underageRulePercentage", underageRulePercentage)
        .append("lastTaskAdministered", lastTaskAdministered)
        .append("lastTaskTimestamp", lastTaskTimestamp)
        .append("errorMessage", errorMessage);
  }
}
