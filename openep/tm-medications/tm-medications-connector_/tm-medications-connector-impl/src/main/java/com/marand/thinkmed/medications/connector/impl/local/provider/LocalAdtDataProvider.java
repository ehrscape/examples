package com.marand.thinkmed.medications.connector.impl.local.provider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.marand.maf.core.hibernate.query.Alias;
import com.marand.maf.core.hibernate.query.Hql;
import com.marand.maf.core.resultrow.ProcessingException;
import com.marand.maf.core.resultrow.TupleProcessor;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalDiseaseType;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalMedicalStaff;
import com.marand.thinkmed.medications.connector.impl.local.model.impl.ExternalEncounterType;
import com.marand.thinkmed.medications.connector.impl.provider.AdtDataProvider;
import com.marand.thinkmed.medications.connector.impl.provider.PatientDataProvider;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.beans.factory.annotation.Required;

import static com.marand.maf.core.hibernate.query.Alias.permanentEntities;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalCareProvider;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalCentralCase;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalCentralCaseDisease;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalDiseaseType;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalEncounter;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalEpisode;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalOrganization;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalPatient;

/**
 * @author Bostjan Vester
 */
public class LocalAdtDataProvider extends HibernateDaoSupport implements AdtDataProvider
{
  private PatientDataProvider patientDataProvider;

  @Required
  public void setPatientDataProvider(final PatientDataProvider patientDataProvider)
  {
    this.patientDataProvider = patientDataProvider;
  }

  @Override
  public Interval getLastDischargedCentralCaseEffectiveInterval(final String patientId)
  {
    return getHibernateTemplate().execute(session -> new Hql()
        .select(externalCentralCase.get("effective"))
        .from(
            externalEncounter.innerJoin("episode").as(externalEpisode),
            externalEpisode.innerJoin("centralCase").as(externalCentralCase),
            externalCentralCase.innerJoin("patient").as(externalPatient))
        .where(
            externalPatient.get("externalId").eq(patientId),
            externalEncounter.get("type").eq(ExternalEncounterType.DISCHARGE),
            permanentEntities(externalEncounter).notDeleted())
        .orderBy(externalEncounter.get("when").desc())
        .buildQuery(session, Interval.class)
        .getSingleRowOrNull());
  }

  @Override
  public MedicationsCentralCaseDto getCentralCaseForMedicationsDto(final String patientId)
  {
    return getHibernateTemplate().execute(
        session -> {
          final String latestEpisodeId = getPatientLatestEpisodeId(patientId);
          if (latestEpisodeId == null)
          {
            return null;
          }
          return new Hql()
              .select(
                  externalCentralCase.get("outpatient"),
                  externalCentralCase.get("externalId"),
                  externalCentralCase.get("effective"),
                  externalCareProvider.get("externalId"),
                  externalEpisode.get("externalId"),
                  externalCareProvider.get("name"))
              .from(
                  externalEpisode.innerJoin("centralCase").as(externalCentralCase),
                  externalCentralCase.innerJoin("patient").as(externalPatient),
                  externalEpisode.innerJoin("careProvider").as(externalCareProvider))
              .where(
                  externalPatient.get("externalId").eq(patientId),
                  externalEpisode.get("externalId").eq(latestEpisodeId),
                  permanentEntities(externalEpisode, externalCentralCase).notDeleted())
              .orderBy(externalCentralCase.get("effective.start").desc())
              .buildQuery(session, Object[].class)
              .getSingleRowOrNull(
                  new TupleProcessor<MedicationsCentralCaseDto>()
                  {
                    @Override
                    protected MedicationsCentralCaseDto process(final boolean hasNextTuple) throws ProcessingException
                    {
                      final boolean outpatient = next();
                      final String centralCaseId = next();
                      final Interval centralCaseEffective = next();
                      final String episodeId = next();
                      final String careProviderId = next();
                      final String careProviderName = next();

                      final MedicationsCentralCaseDto result = new MedicationsCentralCaseDto();
                      result.setOutpatient(outpatient);
                      result.setCentralCaseId(centralCaseId);
                      result.setCentralCaseEffective(centralCaseEffective);
                      result.setEpisodeId(episodeId);
                      result.setCareProvider(new NamedExternalDto(careProviderId, careProviderName));

                      return result;
                    }
                  });
        });
  }

  @Override
  public PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      final String patientId,
      final boolean mainDiseaseTypeOnly,
      final DateTime when,
      final Locale locale)
  {
    return getHibernateTemplate().execute(
        session -> {
          final Alias.Permanent<ExternalMedicalStaff> curingCareProfessional =
              Alias.forPermanentEntity(ExternalMedicalStaff.class);
          final Alias.Permanent<ExternalMedicalStaff> supervisoryCareProfessional =
              Alias.forPermanentEntity(ExternalMedicalStaff.class);

          final String latestEpisodeId = getPatientLatestEpisodeId(patientId);

          if (latestEpisodeId == null)
          {
            return null;
          }

          final List<String> allergies = new ArrayList<>(
              Lists.transform(
                  patientDataProvider.getPatientAllergies(patientId, when),
                  new Function<NamedExternalDto, String>()
                  {
                    @Nullable
                    @Override
                    public String apply(@Nullable final NamedExternalDto input)
                    {
                      return input != null ? input.getName() : null;
                    }
                  }));
          final List<ExternalCatalogDto> diseases = getCentralCaseDiseases(latestEpisodeId);

          return new Hql()
              .select(
                  externalPatient.get("name"),
                  externalPatient.get("birthDate"),
                  externalPatient.get("gender"),
                  externalPatient.get("identNumberType"),
                  externalPatient.get("identNumber"),
                  externalCentralCase.get("externalId"),
                  externalOrganization.get("name"),
                  externalCentralCase.get("roomAndBed"),
                  externalCentralCase.get("effective.start"),
                  externalPatient.get("weightInKg"),
                  externalPatient.get("address"),
                  curingCareProfessional.get("name"),
                  supervisoryCareProfessional.get("name"),
                  externalCareProvider.get("externalId"))
              .from(
                  externalEpisode.innerJoin("centralCase").as(externalCentralCase),
                  externalCentralCase.innerJoin("patient").as(externalPatient),
                  externalEpisode.innerJoin("careProvider").as(externalCareProvider),
                  externalCareProvider.innerJoin("organization").as(externalOrganization),
                  externalCentralCase.innerJoin("curingCareProfessional").as(curingCareProfessional),
                  externalCentralCase.innerJoin("supervisoryCareProfessional").as(supervisoryCareProfessional))
              .where(
                  externalPatient.get("externalId").eq(patientId),
                  externalEpisode.get("externalId").eq(latestEpisodeId))
              .orderBy(externalCentralCase.get("effective.start").desc())
              .buildQuery(session, Object[].class)
              .getSingleRowOrNull(
                  new TupleProcessor<PatientDataForTherapyReportDto>()
                  {
                    @Override
                    protected PatientDataForTherapyReportDto process(final boolean hasNextTuple) throws ProcessingException
                    {
                      return new PatientDataForTherapyReportDto(
                          true,
                          "",
                          nextString(),
                          SimpleDateFormat.getDateInstance().format(next(DateTime.class).toDate()),
                          next(Gender.class),
                          nextString(),
                          nextString(),
                          nextString(),
                          nextString(),
                          nextString(),
                          SimpleDateFormat.getDateInstance().format(next(DateTime.class).toDate()),
                          1,
                          diseases,
                          String.valueOf(next(Double.class)),
                          nextString(),
                          nextString(),
                          nextString(),
                          null,
                          1,
                          allergies,
                          nextString());
                    }
                  });
        });
  }

  @Override
  public List<ExternalCatalogDto> getPatientDiseases(final String patientId)
  {
    final String patientLatestCentralCaseId = getPatientLatestEpisodeId(patientId);
    return getCentralCaseDiseases(patientLatestCentralCaseId);
  }

  private List<ExternalCatalogDto> getCentralCaseDiseases(final String episodeId)
  {
    return getHibernateTemplate().execute(
        session -> new Hql()
            .select(
                externalDiseaseType)
            .from(
                externalEpisode.innerJoin("centralCase").as(externalCentralCase),
                externalCentralCase.innerJoin("diseases").as(externalCentralCaseDisease),
                externalCentralCaseDisease.innerJoin("disease").as(externalDiseaseType))
            .where(
                externalEpisode.get("externalId").eq(episodeId),
                permanentEntities(externalCentralCaseDisease).notDeleted())
            .buildQuery(session, ExternalDiseaseType.class)
            .list(
                (resultRow, hasNext) -> {
                  return new ExternalCatalogDto(resultRow.getExternalId(), resultRow.getName(), resultRow.getCode());
                }));
  }

  private String getPatientLatestEpisodeId(final String patientId)
  {
    return getHibernateTemplate().execute(
        session -> {
          final List<String> latestEpisodeIdList = new Hql()
              .select(
                  externalEpisode.get("externalId"))
              .from(
                  externalEpisode.innerJoin("centralCase").as(externalCentralCase),
                  externalCentralCase.innerJoin("patient").as(externalPatient))
              .where(
                  externalPatient.get("externalId").eq(patientId),
                  permanentEntities(externalCentralCase).notDeleted())
              .orderBy(externalCentralCase.get("effective.start"))
              .buildQuery(session, String.class)
              .list(1);

          return latestEpisodeIdList.isEmpty() ? null : latestEpisodeIdList.get(0);
        });
  }
}
