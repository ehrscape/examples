package com.marand.thinkmed.medications.connector.impl.local.model;

import com.marand.maf.core.data.entity.EffectiveEntity;

/**
 * @author Bostjan Vester
 */
public interface ExternalEpisode extends ExternalEntity, EffectiveEntity
{
  ExternalCentralCase getCentralCase();
  void setCentralCase(ExternalCentralCase centralCase);
  ExternalCareProvider getCareProvider();
  void setCareProvider(ExternalCareProvider careProvider);
}
