package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
public class SaveMedicationOrderDto extends DataTransferObject implements JsonSerializable
{
  private TherapyDto therapy;
  private MedicationOrderActionEnum actionEnum;
  private String sourceId;
  private String linkCompositionUid;
  private TherapyChangeReasonDto changeReasonDto;

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  public MedicationOrderActionEnum getActionEnum()
  {
    return actionEnum;
  }

  public void setActionEnum(final MedicationOrderActionEnum actionEnum)
  {
    this.actionEnum = actionEnum;
  }

  public String getSourceId()
  {
    return sourceId;
  }

  public void setSourceId(final String sourceId)
  {
    this.sourceId = sourceId;
  }

  public String getLinkCompositionUid()
  {
    return linkCompositionUid;
  }

  public void setLinkCompositionUid(final String linkCompositionUid)
  {
    this.linkCompositionUid = linkCompositionUid;
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
        .append("actionEnum", actionEnum)
        .append("sourceId", sourceId)
        .append("linkCompositionUid", linkCompositionUid)
        .append("changeReasonDto", changeReasonDto)
    ;
  }
}
