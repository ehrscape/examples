package com.marand.thinkmed.medications.dto.discharge;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
public class MedicationOnDischargeReconciliationDto extends DataTransferObject implements JsonSerializable
{
  private TherapyDto therapy;
  private String linkedAdmissionCompositionId;
  private String linkedOrderCompositionId;
  private TherapyChangeReasonDto changeReasonDto;
  private MedicationOnDischargeStatus status;

  public String getLinkedAdmissionCompositionId()
  {
    return linkedAdmissionCompositionId;
  }

  public void setLinkedAdmissionCompositionId(final String linkedAdmissionCompositionId)
  {
    this.linkedAdmissionCompositionId = linkedAdmissionCompositionId;
  }

  public String getLinkedOrderCompositionId()
  {
    return linkedOrderCompositionId;
  }

  public void setLinkedOrderCompositionId(final String linkedOrderCompositionId)
  {
    this.linkedOrderCompositionId = linkedOrderCompositionId;
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

  public MedicationOnDischargeStatus getStatus()
  {
    return status;
  }

  public void setStatus(final MedicationOnDischargeStatus status)
  {
    this.status = status;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("linkedAdmissionCompositionId", linkedAdmissionCompositionId)
        .append("linkedOrderCompositionId", linkedOrderCompositionId)
        .append("changeReasonDto", changeReasonDto)
        .append("status", status)
    ;
  }
}
