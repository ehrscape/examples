package com.marand.thinkmed.medications.dto.dose;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class DoseRangeDto extends DataTransferObject implements JsonSerializable
{
  private Double minNumerator;
  private Double maxNumerator;

  private Double minDenominator;
  private Double maxDenominator;

  public Double getMinNumerator()
  {
    return minNumerator;
  }

  public void setMinNumerator(final Double minNumerator)
  {
    this.minNumerator = minNumerator;
  }

  public Double getMaxNumerator()
  {
    return maxNumerator;
  }

  public void setMaxNumerator(final Double maxNumerator)
  {
    this.maxNumerator = maxNumerator;
  }

  public Double getMinDenominator()
  {
    return minDenominator;
  }

  public void setMinDenominator(final Double minDenominator)
  {
    this.minDenominator = minDenominator;
  }

  public Double getMaxDenominator()
  {
    return maxDenominator;
  }

  public void setMaxDenominator(final Double maxDenominator)
  {
    this.maxDenominator = maxDenominator;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("minNumerator", minNumerator)
        .append("maxNumerator", maxNumerator)
        .append("minDenominator", minDenominator)
        .append("maxDenominator", maxDenominator)
    ;
  }
}
