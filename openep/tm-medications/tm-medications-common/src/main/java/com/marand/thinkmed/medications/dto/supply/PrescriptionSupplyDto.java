package com.marand.thinkmed.medications.dto.supply;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class PrescriptionSupplyDto extends DataTransferObject implements JsonSerializable
{
  private Integer daysDuration;
  private Integer quantity;
  private String unit;
  private List<MedicationControlDrugSupplyDto> controlDrugSupplies = new ArrayList<>();

  public Integer getDaysDuration()
  {
    return daysDuration;
  }

  public void setDaysDuration(final Integer daysDuration)
  {
    this.daysDuration = daysDuration;
  }

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

  public List<MedicationControlDrugSupplyDto> getControlDrugSupplies()
  {
    return controlDrugSupplies;
  }

  public void setControlDrugSupplies(final List<MedicationControlDrugSupplyDto> controlDrugSupplies)
  {
    this.controlDrugSupplies = controlDrugSupplies;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("daysDuration", daysDuration)
        .append("quantity", quantity)
        .append("unit", unit)
        .append("controlDrugSupplies", controlDrugSupplies)
    ;
  }
}
