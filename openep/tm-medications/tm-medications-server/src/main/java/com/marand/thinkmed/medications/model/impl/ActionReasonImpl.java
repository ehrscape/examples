package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.marand.maf.core.hibernate.entity.AbstractEffectiveCatalogEntity;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.model.ActionReason;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
@Entity
public class ActionReasonImpl extends AbstractEffectiveCatalogEntity implements ActionReason
{
  private ActionReasonType reasonType;

  @Override
  @Enumerated(EnumType.STRING)
  public ActionReasonType getReasonType()
  {
    return reasonType;
  }

  @Override
  public void setReasonType(final ActionReasonType reasonType)
  {
    this.reasonType = reasonType;
  }

  @Override
  protected void appendToString(ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("reasonType", reasonType);
  }
}
