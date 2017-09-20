package com.marand.thinkmed.medications.connector.impl.local.model;

/**
 * @author Bostjan Vester
 */
public interface ExternalCareProvider extends NamedExternalEntity
{
  ExternalOrganization getOrganization();
  void setOrganization(ExternalOrganization organization);
}
