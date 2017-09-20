package com.marand.thinkmed.medications.connector.impl.local.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.marand.maf.core.hibernate.query.Hql;
import com.marand.maf.core.resultrow.ProcessingException;
import com.marand.maf.core.resultrow.ResultRowProcessor;
import com.marand.maf.core.resultrow.TupleProcessor;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalAllergy;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalPatient;
import com.marand.thinkmed.medications.connector.impl.provider.AdtDataProvider;
import com.marand.thinkmed.medications.connector.impl.provider.PatientDataProvider;
import org.joda.time.DateTime;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.beans.factory.annotation.Required;

import static com.marand.maf.core.hibernate.query.Alias.effectiveEntities;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalAllergy;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalCareProvider;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalCentralCase;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalEpisode;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalOrganization;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalPatient;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalPatientAllergy;

/**
 * @author Bostjan Vester
 */
public class LocalPatientDataProvider extends HibernateDaoSupport implements PatientDataProvider
{
  private AdtDataProvider adtDataProvider;

  @Required
  public void setAdtDataProvider(final AdtDataProvider adtDataProvider)
  {
    this.adtDataProvider = adtDataProvider;
  }

  @Override
  public PatientDataForMedicationsDto getPatientData(final String patientId, final DateTime when)
  {
    return getHibernateTemplate().execute(session -> {
      final ExternalPatient patient = new Hql()
          .select(
              externalPatient)
          .from(
              externalPatient)
          .where(
              externalPatient.get("externalId").eq(patientId))
          .buildQuery(session, ExternalPatient.class)
          .getSingleRowOrNull();
      if (patient != null)
      {
        final List<ExternalCatalogDto> diseaseTypeCodes = adtDataProvider.getPatientDiseases(patientId);
        final List<NamedExternalDto> allergies = getPatientAllergies(patientId, when);

        final MedicationsCentralCaseDto centralCaseDto = adtDataProvider.getCentralCaseForMedicationsDto(patientId);
        return new PatientDataForMedicationsDto(
            patient.getBirthDate(),
            patient.getWeightInKg(),
            patient.getHeightInCm(),
            patient.getGender(),
            diseaseTypeCodes,
            allergies,
            centralCaseDto);
      }
      return null;
    });
  }

  @Override
  public List<NamedExternalDto> getPatientAllergies(final String patientId, final DateTime when)
  {
    return getHibernateTemplate().execute(
        session -> new Hql()
            .select(
                externalAllergy)
            .from(
                externalPatientAllergy.innerJoin("patient").as(externalPatient),
                externalPatientAllergy.innerJoin("allergy").as(externalAllergy))
            .where(
                externalPatient.get("externalId").eq(patientId),
                effectiveEntities(externalPatientAllergy).notDeletedAndEffectiveAt(when))
            .buildQuery(session, ExternalAllergy.class)
            .list(
                (resultRow, hasNext) -> new NamedExternalDto(resultRow.getExternalId(), resultRow.getName())));
  }

  @Override
  public Map<String, PatientDisplayDto> getPatientDisplayData(
      final Set<String> patientIds)
  {
    return getHibernateTemplate().execute(session -> {
      final Map<String, PatientDisplayDto> result = new HashMap<>();
      new Hql()
          .select(
              externalPatient.get("externalId"),
              externalPatient.get("name"),
              externalPatient.get("birthDate"),
              externalPatient.get("gender"),
              externalPatient.get("patientImagePath"))
          .from(externalPatient)
          .where(
              externalPatient.get("externalId").in(patientIds))
          .buildQuery(session, Object[].class)
          .list(new TupleProcessor<PatientDisplayDto>()
          {
            @Override
            protected PatientDisplayDto process(final boolean hasNextTuple) throws ProcessingException
            {
              final String externalId = nextString();
              result.put(
                  externalId,
                  new PatientDisplayDto(externalId, nextString(), next(DateTime.class), next(Gender.class), nextString()));
              return null;
            }
          });
      return result;
    });
  }

  @Override
  public Set<NamedExternalDto> getPatientsIdsAndCareProviderNames(final Set<String> careProviderIds, final DateTime when)
  {
    return getHibernateTemplate().execute((HibernateCallback<Set<NamedExternalDto>>)session -> new HashSet<>(
        new Hql()
            .select(
                externalPatient.get("externalId"),
                externalOrganization.get("name"),
                externalCareProvider.get("name"))
            .from(
                externalEpisode.innerJoin("careProvider").as(externalCareProvider),
                externalCareProvider.innerJoin("organization").as(externalOrganization),
                externalEpisode.innerJoin("centralCase").as(externalCentralCase),
                externalCentralCase.innerJoin("patient").as(externalPatient))
            .where(
                externalCareProvider.get("externalId").in(careProviderIds),
                effectiveEntities(externalEpisode).notDeletedAndEffectiveAt(when))
            .buildQuery(session, Object[].class)
            .list(new TupleProcessor<NamedExternalDto>()
            {
              @Override
              protected NamedExternalDto process(final boolean hasNextTuple) throws ProcessingException
              {
                return new NamedExternalDto(nextString(), nextString() + " - " + nextString());
              }
            })
    ));
  }
}
