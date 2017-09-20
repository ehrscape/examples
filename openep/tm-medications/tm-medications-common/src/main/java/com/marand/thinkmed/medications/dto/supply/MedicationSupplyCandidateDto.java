package com.marand.thinkmed.medications.dto.supply;

import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class MedicationSupplyCandidateDto extends NamedIdentityDto implements JsonSerializable
{
  private String basicUnit;
  private Double strengthNumerator;
  private String strengthNumeratorUnit;

  public String getBasicUnit()
  {
    return basicUnit;
  }

  public void setBasicUnit(final String basicUnit)
  {
    this.basicUnit = basicUnit;
  }

  public Double getStrengthNumerator()
  {
    return strengthNumerator;
  }

  public void setStrengthNumerator(final Double strengthNumerator)
  {
    this.strengthNumerator = strengthNumerator;
  }

  public String getStrengthNumeratorUnit()
  {
    return strengthNumeratorUnit;
  }

  public void setStrengthNumeratorUnit(final String strengthNumeratorUnit)
  {
    this.strengthNumeratorUnit = strengthNumeratorUnit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("basicUnit", basicUnit)
        .append("strengthNumerator", strengthNumerator)
        .append("strengthNumeratorUnit", strengthNumeratorUnit)
    ;
  }
}
