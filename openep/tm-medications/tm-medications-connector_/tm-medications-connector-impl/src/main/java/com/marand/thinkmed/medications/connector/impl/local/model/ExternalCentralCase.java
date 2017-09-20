package com.marand.thinkmed.medications.connector.impl.local.model;

import java.util.Set;

import com.marand.maf.core.data.entity.EffectiveEntity;

/**
 * @author Bostjan Vester
 */
public interface ExternalCentralCase extends ExternalEntity, EffectiveEntity
{
  ExternalPatient getPatient();
  void setPatient(ExternalPatient patient);
  boolean isOutpatient();
  void setOutpatient(boolean outpatient);
  ExternalMedicalStaff getCuringCareProfessional();
  void setCuringCareProfessional(ExternalMedicalStaff careProfessional);
  ExternalMedicalStaff getSupervisoryCareProfessional();
  void setSupervisoryCareProfessional(ExternalMedicalStaff careProfessional);
  String getRoomAndBed();
  void setRoomAndBed(String roomAndBed);
  Set<ExternalCentralCaseDisease> getDiseases();
  void setDiseases(Set<ExternalCentralCaseDisease> diseases);
}
