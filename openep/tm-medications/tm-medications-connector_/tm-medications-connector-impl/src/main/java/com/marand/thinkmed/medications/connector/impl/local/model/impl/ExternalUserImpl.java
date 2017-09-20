package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.marand.thinkmed.medications.connector.impl.local.model.ExternalUser;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.joda.time.Interval;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_user")
public class ExternalUserImpl extends NamedExternalEntityImpl implements ExternalUser
{
  private Interval effective;

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
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("effective", effective)
        ;
  }
}
