package com.marand.thinkmed.medications.dto.mentalHealth;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class MentalHealthTherapyDto extends DataTransferObject implements JsonSerializable
{
  private MentalHealthMedicationDto mentalHealthMedicationDto;
  private String genericName;
  private TherapyStatusEnum therapyStatusEnum;

  public MentalHealthMedicationDto getMentalHealthMedicationDto()
  {
    return mentalHealthMedicationDto;
  }

  public void setMentalHealthMedicationDto(final MentalHealthMedicationDto mentalHealthMedicationDto)
  {
    this.mentalHealthMedicationDto = mentalHealthMedicationDto;
  }

  public String getGenericName()
  {
    return genericName;
  }

  public void setGenericName(final String genericName)
  {
    this.genericName = genericName;
  }

  public TherapyStatusEnum getTherapyStatusEnum()
  {
    return therapyStatusEnum;
  }

  public void setTherapyStatusEnum(final TherapyStatusEnum therapyStatusEnum)
  {
    this.therapyStatusEnum = therapyStatusEnum;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("mentalHealthMedicationDto", mentalHealthMedicationDto)
        .append("genericName", genericName)
        .append("therapyStatusEnum", therapyStatusEnum)
    ;
  }
}
