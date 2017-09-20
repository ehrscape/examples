package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.connector.impl.local.model.ExternalEncounter;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalEpisode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_encounter")
public class ExternalEncounterImpl extends ExternalEntityImpl implements ExternalEncounter
{
  private ExternalEpisode episode;
  private ExternalEncounterType type;
  private DateTime when;

  @Override
  @Enumerated(EnumType.STRING)
  public ExternalEncounterType getType()
  {
    return type;
  }

  @Override
  public void setType(final ExternalEncounterType type)
  {
    this.type = type;
  }

  @Override
  @Type(type = "com.marand.maf.core.hibernate.type.DateTimeType")
  public DateTime getWhen()
  {
    return when;
  }

  @Override
  public void setWhen(final DateTime when)
  {
    this.when = when;
  }

  @Override
  @ManyToOne(targetEntity = ExternalEpisodeImpl.class, optional = false)
  public ExternalEpisode getEpisode()
  {
    return episode;
  }

  @Override
  public void setEpisode(final ExternalEpisode episode)
  {
    this.episode = episode;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("type", type)
        .append("when", when)
        .append("episode", episode)
        ;
  }
}
