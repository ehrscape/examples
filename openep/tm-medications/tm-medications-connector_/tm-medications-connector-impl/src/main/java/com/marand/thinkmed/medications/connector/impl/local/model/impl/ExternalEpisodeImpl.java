package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCareProvider;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCentralCase;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalEpisode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.joda.time.Interval;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_episode")
public class ExternalEpisodeImpl extends ExternalEntityImpl implements ExternalEpisode
{
  private Interval effective;
  private ExternalCentralCase centralCase;
  private ExternalCareProvider careProvider;

  @Override
  @Type(type = "com.marand.maf.core.hibernate.type.IntervalType")
  @Columns(columns = {@Column(name = "effective_start", nullable = false), @Column(name = "effective_end", nullable = false)})
  public Interval getEffective()
  {
    return effective;
  }

  @Override
  public void setEffective(final Interval effective)
  {
    this.effective = effective;
  }

  @Override
  @ManyToOne(targetEntity = ExternalCentralCaseImpl.class, optional = false)
  public ExternalCentralCase getCentralCase()
  {
    return centralCase;
  }

  @Override
  public void setCentralCase(final ExternalCentralCase centralCase)
  {
    this.centralCase = centralCase;
  }

  @Override
  @ManyToOne(targetEntity = ExternalCareProviderImpl.class, optional = false)
  public ExternalCareProvider getCareProvider()
  {
    return careProvider;
  }

  @Override
  public void setCareProvider(final ExternalCareProvider careProvider)
  {
    this.careProvider = careProvider;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("centralCase", centralCase)
        .append("careProvider", careProvider)
        ;
  }
}
