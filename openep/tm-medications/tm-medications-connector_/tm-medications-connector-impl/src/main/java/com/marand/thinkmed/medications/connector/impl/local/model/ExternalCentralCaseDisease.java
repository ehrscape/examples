package com.marand.thinkmed.medications.connector.impl.local.model;

import com.marand.maf.core.data.entity.PermanentEntity;

/**
 * @author Bostjan Vester
 */
public interface ExternalCentralCaseDisease extends PermanentEntity
{
  ExternalDiseaseType getDisease();
  void setDisease(ExternalDiseaseType disease);
  ExternalCentralCase getCentralCase();
  void setCentralCase(ExternalCentralCase centralCase);
}
