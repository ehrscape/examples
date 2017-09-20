package com.marand.thinkmed.medications.connector.impl.local.model;

/**
 * @author Bostjan Vester
 */
public interface ExternalDiseaseType extends NamedExternalEntity
{
  String getCode();
  void setCode(String code);
}
