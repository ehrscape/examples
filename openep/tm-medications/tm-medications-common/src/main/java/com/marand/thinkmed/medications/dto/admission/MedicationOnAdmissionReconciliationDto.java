package com.marand.thinkmed.medications.dto.admission;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
public class MedicationOnAdmissionReconciliationDto extends DataTransferObject implements JsonSerializable
{
  private TherapyDto therapy;
  private TherapyChangeReasonDto changeReasonDto;
  private TherapyStatusEnum statusEnum;

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

  public TherapyStatusEnum getStatusEnum()
  {
    return statusEnum;
  }

  public void setStatusEnum(final TherapyStatusEnum statusEnum)
  {
    this.statusEnum = statusEnum;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("changeReasonDto", changeReasonDto)
        .append("statusEnum", statusEnum)
    ;
  }
}
