package com.marand.thinkmed.medications.model;

import com.marand.maf.core.data.entity.EffectiveCatalogEntity;
import com.marand.thinkmed.medications.ActionReasonType;

/**
 * @author nejck
 */
public interface ActionReason extends EffectiveCatalogEntity
{
  ActionReasonType getReasonType();

  void setReasonType(final ActionReasonType reasonType);
}
