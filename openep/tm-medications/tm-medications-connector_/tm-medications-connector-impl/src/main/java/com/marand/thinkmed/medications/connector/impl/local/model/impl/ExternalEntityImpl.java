package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
@MappedSuperclass
public abstract class ExternalEntityImpl extends AbstractPermanentEntity implements ExternalEntity
{
  private String externalId;

  @Override
  public String getExternalId()
  {
    return externalId;
  }

  @Override
  public void setExternalId(final String externalId)
  {
    this.externalId = externalId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("externalId", externalId)
        ;
  }
}
