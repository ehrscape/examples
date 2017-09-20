package com.marand.thinkmed.medications.connector.impl.local.model;

import com.marand.thinkmed.medications.connector.impl.local.model.impl.ExternalEncounterType;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public interface ExternalEncounter extends ExternalEntity
{
  ExternalEncounterType getType();
  void setType(ExternalEncounterType type);
  DateTime getWhen();
  void setWhen(DateTime when);
  ExternalEpisode getEpisode();
  void setEpisode(ExternalEpisode episode);
}
