package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCareProvider;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalOrganization;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_care_provider")
public class ExternalCareProviderImpl extends NamedExternalEntityImpl implements ExternalCareProvider
{
  private ExternalOrganization organization;

  @Override
  @ManyToOne(targetEntity = ExternalOrganizationImpl.class)
  public ExternalOrganization getOrganization()
  {
    return organization;
  }

  @Override
  public void setOrganization(final ExternalOrganization organization)
  {
    this.organization = organization;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("organization", organization)
        ;
  }
}
