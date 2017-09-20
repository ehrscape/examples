package com.marand.thinkmed.medications.dto.discharge;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
public class MedicationOnDischargeDto extends DataTransferObject implements JsonSerializable
{
  private TherapyDto therapy;
  private MedicationOnDischargeStatus status;
  private TherapyChangeReasonDto changeReasonDto;
  private TherapySourceGroupEnum sourceGroupEnum;
  private String sourceId;

  public String getSourceId()
  {
    return sourceId;
  }

  public void setSourceId(final String sourceId)
  {
    this.sourceId = sourceId;
  }

  public MedicationOnDischargeStatus getStatus()
  {
    return status;
  }

  public void setStatus(final MedicationOnDischargeStatus status)
  {
    this.status = status;
  }

  public TherapyChangeReasonDto getChangeReasonDto()
  {
    return changeReasonDto;
  }

  public void setChangeReasonDto(final TherapyChangeReasonDto changeReasonDto)
  {
    this.changeReasonDto = changeReasonDto;
  }

  public TherapySourceGroupEnum getSourceGroupEnum()
  {
    return sourceGroupEnum;
  }

  public void setSourceGroupEnum(final TherapySourceGroupEnum sourceGroupEnum)
  {
    this.sourceGroupEnum = sourceGroupEnum;
  }

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("status", status)
        .append("changeReasonDto", changeReasonDto)
        .append("sourceGroupEnum", sourceGroupEnum)
        .append("sourceId", sourceId);
  }
}
