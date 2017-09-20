package com.marand.thinkmed.medications.dto.pharmacist.review;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class SupplyDataForPharmacistReviewDto extends DataTransferObject
{
  private boolean processExists;
  private String taskId;
  private Integer daysSupply;
  private boolean isDismissed;
  private MedicationSupplyTypeEnum supplyTypeEnum;

  public boolean isProcessExists()
  {
    return processExists;
  }

  public void setProcessExists(final boolean processExists)
  {
    this.processExists = processExists;
  }

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(final String taskId)
  {
    this.taskId = taskId;
  }

  public Integer getDaysSupply()
  {
    return daysSupply;
  }

  public void setDaysSupply(final Integer daysSupply)
  {
    this.daysSupply = daysSupply;
  }

  public MedicationSupplyTypeEnum getSupplyTypeEnum()
  {
    return supplyTypeEnum;
  }

  public void setSupplyTypeEnum(final MedicationSupplyTypeEnum supplyTypeEnum)
  {
    this.supplyTypeEnum = supplyTypeEnum;
  }

  public boolean isDismissed()
  {
    return isDismissed;
  }

  public void setDismissed(final boolean dismissed)
  {
    isDismissed = dismissed;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("processExists", processExists)
        .append("taskId", taskId)
        .append("daysSupply", daysSupply)
        .append("supplyTypeEnum", supplyTypeEnum)
        .append("isDismissed", isDismissed)
    ;
  }
}