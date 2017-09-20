package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.MappedSuperclass;

import com.marand.thinkmed.medications.connector.impl.local.model.NamedExternalEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
@MappedSuperclass
public abstract class NamedExternalEntityImpl extends ExternalEntityImpl implements NamedExternalEntity
{
  private String name;

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public void setName(final String name)
  {
    this.name = name;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("name", name)
        ;
  }
}
