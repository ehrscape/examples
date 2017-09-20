package com.marand.thinkmed.medications.connector.impl.local.provider;

import java.sql.SQLException;
import java.util.List;

import com.marand.maf.core.hibernate.query.Hql;
import com.marand.maf.core.resultrow.ProcessingException;
import com.marand.maf.core.resultrow.ResultRowProcessor;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCareProvider;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalMedicalStaff;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalUser;
import com.marand.thinkmed.medications.connector.impl.provider.StaffDataProvider;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import static com.marand.maf.core.hibernate.query.Alias.effectiveEntities;
import static com.marand.maf.core.hibernate.query.Alias.permanentEntities;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalCareProvider;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalMedicalStaff;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalUser;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalUserCareProviderMember;

/**
 * @author Bostjan Vester
 */
public class LocalStaffDataProvider extends HibernateDaoSupport implements StaffDataProvider
{
  @Override
  public List<NamedExternalDto> getMedicalStaff()
  {
    return getHibernateTemplate().execute(session -> new Hql()
        .select(externalMedicalStaff).from(externalMedicalStaff).where(permanentEntities(externalMedicalStaff).notDeleted())
        .buildQuery(session, ExternalMedicalStaff.class)
        .list((resultRow, hasNext) -> {
          return new NamedExternalDto(resultRow.getExternalId(), resultRow.getName());
        }));
  }

  @Override
  public NamedExternalDto getUsersName(
      final String userId, final DateTime when)
  {
    return getHibernateTemplate().execute(session -> new Hql()
        .select(
            externalUser)
        .from(
            externalUser)
        .where(
            externalUser.get("externalId").eq(userId),
            effectiveEntities(externalUser).notDeletedAndEffectiveAt(when))
        .buildQuery(session, ExternalUser.class)
        .getSingleRowOrNull((resultRow, hasNext) -> new NamedExternalDto(resultRow.getExternalId(), resultRow.getName())));
  }

  @Override
  public List<NamedExternalDto> getUserCareProviders(final String userId)
  {
    return getHibernateTemplate().execute(session -> new Hql()
        .select(externalCareProvider)
        .from(
            externalUserCareProviderMember.innerJoin("user").as(externalUser),
            externalUserCareProviderMember.innerJoin("careProvider").as(externalCareProvider))
        .where(
            externalUser.get("externalId").eq(userId),
            permanentEntities(externalUserCareProviderMember).notDeleted())
        .buildQuery(session, ExternalCareProvider.class)
        .list((resultRow, hasNext) -> new NamedExternalDto(resultRow.getExternalId(), resultRow.getName())));
  }
}
