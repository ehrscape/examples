package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCareProvider;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalUser;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalUserCareProviderMember;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_user_cp_member")
public class ExternalUserCareProviderMemberImpl extends AbstractPermanentEntity implements ExternalUserCareProviderMember
{
  private ExternalUser user;
  private ExternalCareProvider careProvider;

  @Override
  @ManyToOne(targetEntity = ExternalUserImpl.class)
  public ExternalUser getUser()
  {
    return user;
  }

  @Override
  public void setUser(final ExternalUser user)
  {
    this.user = user;
  }

  @Override
  @ManyToOne(targetEntity = ExternalCareProviderImpl.class)
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
        .append("user", user)
        .append("careProvider", careProvider)
        ;
  }
}
