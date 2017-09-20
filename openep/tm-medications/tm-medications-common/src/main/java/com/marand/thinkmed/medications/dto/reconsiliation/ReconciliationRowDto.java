package com.marand.thinkmed.medications.dto.reconsiliation;

import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
public class ReconciliationRowDto extends DataTransferObject implements JsonSerializable
{
  private ReconciliationRowGroupEnum groupEnum;

  private TherapyDto therapyOnAdmission;
  private TherapyDto therapyOnDischarge;

  private TherapyChangeReasonDto changeReasonDto;
  private TherapyStatusEnum statusEnum;

  private List<TherapyChangeDto<?, ?>> changes;

  public ReconciliationRowGroupEnum getGroupEnum()
  {
    return groupEnum;
  }

  public void setGroupEnum(final ReconciliationRowGroupEnum groupEnum)
  {
    this.groupEnum = groupEnum;
  }

  public TherapyDto getTherapyOnAdmission()
  {
    return therapyOnAdmission;
  }

  public void setTherapyOnAdmission(final TherapyDto therapyOnAdmission)
  {
    this.therapyOnAdmission = therapyOnAdmission;
  }

  public TherapyDto getTherapyOnDischarge()
  {
    return therapyOnDischarge;
  }

  public void setTherapyOnDischarge(final TherapyDto therapyOnDischarge)
  {
    this.therapyOnDischarge = therapyOnDischarge;
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

  public List<TherapyChangeDto<?, ?>> getChanges()
  {
    return changes;
  }

  public void setChanges(final List<TherapyChangeDto<?, ?>> changes)
  {
    this.changes = changes;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("groupEnum", groupEnum)
        .append("therapyOnAdmission", therapyOnAdmission)
        .append("therapyOnDischarge", therapyOnDischarge)
        .append("changeReasonDto", changeReasonDto)
        .append("statusEnum", statusEnum)
        .append("changes", changes)
    ;
  }
}
