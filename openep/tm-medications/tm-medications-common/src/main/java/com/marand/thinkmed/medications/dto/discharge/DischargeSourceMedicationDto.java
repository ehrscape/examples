package com.marand.thinkmed.medications.dto.discharge;

import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.reconsiliation.SourceMedicationDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class DischargeSourceMedicationDto extends SourceMedicationDto
{
  private boolean reviewed;
  private TherapyStatusEnum status;
  private TherapyChangeReasonDto changeReason;

  public boolean isReviewed()
  {
    return reviewed;
  }

  public void setReviewed(final boolean reviewed)
  {
    this.reviewed = reviewed;
  }

  public TherapyStatusEnum getStatus()
  {
    return status;
  }

  public void setStatus(final TherapyStatusEnum status)
  {
    this.status = status;
  }

  public TherapyChangeReasonDto getChangeReason()
  {
    return changeReason;
  }

  public void setChangeReason(final TherapyChangeReasonDto changeReason)
  {
    this.changeReason = changeReason;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("reviewed", reviewed);
    tsb.append("status", status);
    tsb.append("changeReason", changeReason);
  }
}
