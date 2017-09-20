package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class BnfMaximumDto extends DataTransferObject implements JsonSerializable
{
  private Integer quantity;
  private BnfMaximumUnitType quantityUnit;

  public Integer getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final Integer quantity)
  {
    this.quantity = quantity;
  }

  public BnfMaximumUnitType getQuantityUnit()
  {
    return quantityUnit;
  }

  public void setQuantityUnit(final BnfMaximumUnitType quantityUnit)
  {
    this.quantityUnit = quantityUnit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("quantity", quantity)
        .append("quantityUnit", quantityUnit);
  }
}