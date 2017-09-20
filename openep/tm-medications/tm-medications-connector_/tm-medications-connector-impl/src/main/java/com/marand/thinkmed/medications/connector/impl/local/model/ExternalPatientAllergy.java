package com.marand.thinkmed.medications.connector.impl.local.model;

import com.marand.maf.core.data.entity.EffectiveEntity;

/**
 * @author Bostjan Vester
 */
public interface ExternalPatientAllergy extends EffectiveEntity
{
  ExternalPatient getPatient();
  void setPatient(ExternalPatient patient);
  ExternalAllergy getAllergy();
  void setAllergy(ExternalAllergy allergy);
}
