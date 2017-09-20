// Generated using Marand-EHR TDO Generator vUnknown
// Source: ISPEK - MED - Medication Order.opt
// Time: 2016-10-18T14:09:57.098+02:00
package com.marand.openehr.medications.tdo;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.marand.openehr.tdo.TdoAccess;
import com.marand.openehr.tdo.annotations.Archetype;
import com.marand.openehr.tdo.annotations.TdoNode;
import com.marand.openehr.tdo.validation.constraints.DecimalMin;
import com.marand.openehr.tdo.validation.constraints.ValidDouble;
import com.marand.openehr.tdo.validation.constraints.ValidDvQuantity;
import com.marand.openehr.tdo.validation.constraints.ValidString;
import org.openehr.jaxb.rm.Cluster;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Item;

@Archetype(name = "Structured dose", archetypeId = "openEHR-EHR-CLUSTER.medication_amount.v1")
public class StructuredDoseCluster extends Cluster
{
  @Deprecated
  @Override
  public List<Item> getItems()
  {
    TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
    return super.getItems();
  }

  private DataValue quantity;

  @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = ""), notNull = @NotNull))
  @TdoNode(name = "Quantity", path = "/items[at0001:ELEMENT]/value")
  public DataValue getQuantity()
  {
    return quantity;
  }

  public void setQuantity(DataValue quantity)
  {
    this.quantity = quantity;
  }

  private DvCodedText doseUnit;

  @TdoNode(name = "Dose unit", path = "/items[at0002:ELEMENT]/value")
  public DvCodedText getDoseUnit()
  {
    return doseUnit;
  }

  public void setDoseUnit(DvCodedText doseUnit)
  {
    this.doseUnit = doseUnit;
  }

  private DvText description;

  @TdoNode(name = "Description", path = "/items[at0003:ELEMENT]/value")
  public DvText getDescription()
  {
    return description;
  }

  public void setDescription(DvText description)
  {
    this.description = description;
  }

  private RatioNumeratorCluster ratioNumerator;

  @TdoNode(name = "Ratio numerator", path = "/items[at0008:CLUSTER]")
  public RatioNumeratorCluster getRatioNumerator()
  {
    return ratioNumerator;
  }

  public void setRatioNumerator(RatioNumeratorCluster ratioNumerator)
  {
    this.ratioNumerator = ratioNumerator;
  }

  public static class RatioNumeratorCluster extends Cluster
  {
    @Deprecated
    @Override
    public List<Item> getItems()
    {
      TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
      return super.getItems();
    }

    private DataValue amount;

    @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = ""), notNull = @NotNull))
    @TdoNode(name = "Amount", path = "/items[at0001:ELEMENT]/value")
    public DataValue getAmount()
    {
      return amount;
    }

    public void setAmount(DataValue amount)
    {
      this.amount = amount;
    }

    private DvCodedText doseUnit;

    @TdoNode(name = "Dose unit", path = "/items[at0002:ELEMENT]/value")
    public DvCodedText getDoseUnit()
    {
      return doseUnit;
    }

    public void setDoseUnit(DvCodedText doseUnit)
    {
      this.doseUnit = doseUnit;
    }
  }

  private RatioDenominatorCluster ratioDenominator;

  @TdoNode(name = "Ratio denominator", path = "/items[at0007:CLUSTER]")
  public RatioDenominatorCluster getRatioDenominator()
  {
    return ratioDenominator;
  }

  public void setRatioDenominator(RatioDenominatorCluster ratioDenominator)
  {
    this.ratioDenominator = ratioDenominator;
  }

  public static class RatioDenominatorCluster extends Cluster
  {
    @Deprecated
    @Override
    public List<Item> getItems()
    {
      TdoAccess.check("Property 'Items' is deprecated in: " + getClass().getName());
      return super.getItems();
    }

    private DataValue amount;

    @ValidDvQuantity(magnitude = @ValidDouble(min = @DecimalMin(value = 0.0, inclusive = true)), units = @ValidString(pattern = @Pattern(regexp = ""), notNull = @NotNull))
    @TdoNode(name = "Amount", path = "/items[at0001:ELEMENT]/value")
    public DataValue getAmount()
    {
      return amount;
    }

    public void setAmount(DataValue amount)
    {
      this.amount = amount;
    }

    private DvCodedText doseUnit;

    @TdoNode(name = "Dose unit", path = "/items[at0002:ELEMENT]/value")
    public DvCodedText getDoseUnit()
    {
      return doseUnit;
    }

    public void setDoseUnit(DvCodedText doseUnit)
    {
      this.doseUnit = doseUnit;
    }
  }
}