package com.marand.thinkmed.medications.connector.impl.local.model;

/**
 * @author Bostjan Vester
 */
public interface NamedExternalEntity extends ExternalEntity
{
  String getName();
  void setName(String name);
}
