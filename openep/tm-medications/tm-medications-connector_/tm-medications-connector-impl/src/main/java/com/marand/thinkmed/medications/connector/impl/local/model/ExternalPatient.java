package com.marand.thinkmed.medications.connector.impl.local.model;

import java.util.Set;

import com.marand.thinkmed.api.demographics.data.Gender;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public interface ExternalPatient extends ExternalEntity
{
  String getIdentNumber();
  void setIdentNumber(String identNumber);
  String getIdentNumberType();
  void setIdentNumberType(String type);
  String getName();
  void setName(String name);
  DateTime getBirthDate();
  void setBirthDate(DateTime birthDate);
  Double getWeightInKg();
  void setWeightInKg(Double weightInKg);
  Double getHeightInCm();
  void setHeightInCm(Double heightInCm);
  Gender getGender();
  void setGender(Gender gender);
  Set<ExternalPatientAllergy> getPatientAllergies();
  void setPatientAllergies(Set<ExternalPatientAllergy> allergies);
  String getPatientImagePath();
  void setPatientImagePath(String patientImagePath);
  String getAddress();
  void setAddress(String address);
}
