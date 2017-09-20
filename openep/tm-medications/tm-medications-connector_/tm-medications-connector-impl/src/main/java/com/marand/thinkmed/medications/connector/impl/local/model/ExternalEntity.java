package com.marand.thinkmed.medications.connector.impl.local.model;

import com.marand.maf.core.data.entity.PermanentEntity;

/**
 * @author Bostjan Vester
 */
public interface ExternalEntity extends PermanentEntity
{
  String getExternalId();
  void setExternalId(String externalId);
}
