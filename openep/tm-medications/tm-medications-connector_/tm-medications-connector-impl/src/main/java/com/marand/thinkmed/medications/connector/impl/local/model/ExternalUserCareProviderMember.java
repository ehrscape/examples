package com.marand.thinkmed.medications.connector.impl.local.model;

import com.marand.maf.core.data.entity.PermanentEntity;

/**
 * @author Bostjan Vester
 */
public interface ExternalUserCareProviderMember extends PermanentEntity
{
  ExternalUser getUser();
  void setUser(ExternalUser user);
  ExternalCareProvider getCareProvider();
  void setCareProvider(ExternalCareProvider careProvider);
}
