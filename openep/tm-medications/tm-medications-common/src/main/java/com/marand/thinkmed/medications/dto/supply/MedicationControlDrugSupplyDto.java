package com.marand.thinkmed.medications.dto.supply;

import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class MedicationControlDrugSupplyDto extends NamedIdentityDto implements JsonSerializable
{
  private Integer quantity;
  private String unit;

  public Integer getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final Integer quantity)
  {
    this.quantity = quantity;
  }

  public String getUnit()
  {
    return unit;
  }

  public void setUnit(final String unit)
  {
    this.unit = unit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("quantity", quantity)
        .append("unit", unit)
    ;
  }
}
