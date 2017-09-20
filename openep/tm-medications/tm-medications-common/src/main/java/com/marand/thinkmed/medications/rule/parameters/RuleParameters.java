package com.marand.thinkmed.medications.rule.parameters;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public abstract class RuleParameters extends DataTransferObject implements JsonSerializable
{
  private MedicationRuleEnum medicationRuleEnum;

  public MedicationRuleEnum getMedicationRuleEnum()
  {
    return medicationRuleEnum;
  }

  public void setMedicationRuleEnum(final MedicationRuleEnum medicationRuleEnum)
  {
    this.medicationRuleEnum = medicationRuleEnum;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("medicationRuleEnum", medicationRuleEnum);
  }
}
