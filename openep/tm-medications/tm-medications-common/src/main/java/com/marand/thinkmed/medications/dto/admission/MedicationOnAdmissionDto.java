package com.marand.thinkmed.medications.dto.admission;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
public class MedicationOnAdmissionDto extends DataTransferObject implements JsonSerializable
{
  private TherapyDto therapy;
  private MedicationOnAdmissionStatus status;
  private TherapySourceGroupEnum sourceGroupEnum;
  private TherapyChangeReasonDto changeReasonDto;
  private String sourceId;

  public String getSourceId()
  {
    return sourceId;
  }

  public void setSourceId(final String sourceId)
  {
    this.sourceId = sourceId;
  }

  public TherapySourceGroupEnum getSourceGroupEnum()
  {
    return sourceGroupEnum;
  }

  public void setSourceGroupEnum(final TherapySourceGroupEnum sourceGroupEnum)
  {
    this.sourceGroupEnum = sourceGroupEnum;
  }

  public MedicationOnAdmissionStatus getStatus()
  {
    return status;
  }

  public void setStatus(final MedicationOnAdmissionStatus status)
  {
    this.status = status;
  }

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  public TherapyChangeReasonDto getChangeReasonDto()
  {
    return changeReasonDto;
  }

  public void setChangeReasonDto(final TherapyChangeReasonDto changeReasonDto)
  {
    this.changeReasonDto = changeReasonDto;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("status", status)
        .append("sourceGroupEnum", sourceGroupEnum)
        .append("sourceId", sourceId)
        .append("changeReasonDto", changeReasonDto)
    ;
  }
}
