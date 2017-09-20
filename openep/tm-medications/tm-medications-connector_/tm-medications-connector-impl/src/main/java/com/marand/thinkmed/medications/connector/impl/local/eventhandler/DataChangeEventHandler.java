package com.marand.thinkmed.medications.connector.impl.local.eventhandler;

import java.io.Serializable;
import java.sql.SQLException;

import com.marand.maf.core.hibernate.query.Alias;
import com.marand.maf.core.hibernate.query.Hql;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCareProvider;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCentralCase;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalEncounter;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalEntity;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalEpisode;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalMedicalStaff;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalPatient;
import com.marand.thinkmed.medications.connector.impl.local.model.impl.ExternalCentralCaseImpl;
import com.marand.thinkmed.medications.connector.impl.local.model.impl.ExternalEncounterImpl;
import com.marand.thinkmed.medications.connector.impl.local.model.impl.ExternalEncounterType;
import com.marand.thinkmed.medications.connector.impl.local.model.impl.ExternalEpisodeImpl;
import com.marand.thinkmed.medications.connector.impl.local.model.impl.ExternalPatientImpl;
import com.marand.thinkmed.medications.event.AdmitPatientEvent;
import com.marand.thinkmed.medications.event.DischargePatientEvent;
import com.marand.thinkmed.medications.event.EventHandler;
import com.marand.thinkmed.medications.event.PatientDetails;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalCareProvider;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalMedicalStaff;
import static com.marand.thinkmed.medications.connector.impl.local.provider.ExternalAliases.externalPatient;

/**
 * @author Bostjan Vester
 */
public class DataChangeEventHandler extends HibernateDaoSupport implements EventHandler
{
  @Override
  public void handle(final AdmitPatientEvent event)
  {
    if (event == null)
    {
      return;
    }
    final String patientId = event.getPatientId();
    ExternalPatient patient = findPatient(patientId);
    if (patient == null)
    {
      final PatientDetails patientDetails = event.getPatientDetails();
      if (patientDetails == null)
      {
        throw new NullPointerException("Patient [" + patientId + "] does not exist; details not provided for a new patient creation!");
      }
      patient = new ExternalPatientImpl();
      patient.setExternalId(patientId);
      patient.setName(patientDetails.getName());
      patient.setAddress(patientDetails.getAddress());
      patient.setGender(patientDetails.getGender());

      getHibernateTemplate().save(patient);
    }

    final ExternalCentralCase centralCase = new ExternalCentralCaseImpl();
    centralCase.setEffective(Intervals.infiniteFrom(event.getWhen()));
    centralCase.setPatient(patient);
    centralCase.setOutpatient(event.getCentralCaseDetails().isOutpatient());
    centralCase.setCuringCareProfessional(findMedicalStaff(event.getCentralCaseDetails().getCuringCareProfessionalId()));
    centralCase.setSupervisoryCareProfessional(findMedicalStaff(event.getCentralCaseDetails().getSupervisoryCareProfessionalId()));
    centralCase.setRoomAndBed(event.getCentralCaseDetails().getRoomAndBed());
    final Serializable centralCaseId = getHibernateTemplate().save(centralCase);
    centralCase.setExternalId(String.valueOf(centralCaseId));

    final ExternalEpisode episode = new ExternalEpisodeImpl();
    episode.setCentralCase(centralCase);
    final ExternalCareProvider careProvider = findExternalEntity(
        event.getCareProviderId(),
        externalCareProvider,
        ExternalCareProvider.class);
    if (careProvider == null)
    {
      throw new IllegalArgumentException("Care provider [" + event.getCareProviderId() + "] does not exist!");
    }
    episode.setCareProvider(careProvider);
    episode.setEffective(Intervals.infiniteFrom(event.getWhen()));
    final Serializable episodeId = getHibernateTemplate().save(episode);
    episode.setExternalId(String.valueOf(episodeId));

    final ExternalEncounter encounter = new ExternalEncounterImpl();
    encounter.setType(ExternalEncounterType.ADMISSION);
    encounter.setWhen(event.getWhen());
    encounter.setEpisode(episode);
    final Serializable encounterId = getHibernateTemplate().save(encounter);
    encounter.setExternalId(String.valueOf(encounterId));
  }

  private ExternalPatient findPatient(final String patientId)
  {
    return findExternalEntity(patientId, externalPatient, ExternalPatient.class);
  }

  private ExternalMedicalStaff findMedicalStaff(final String externalId)
  {
    return findExternalEntity(externalId, externalMedicalStaff, ExternalMedicalStaff.class);
  }

  private <E extends ExternalEntity> E findExternalEntity(final String externalId, final Alias.Permanent<E> entity, final Class<E> entityType)
  {
    if (StringUtils.isBlank(externalId))
    {
      return null;
    }
    return getHibernateTemplate().execute(session -> new Hql()
        .select(entity)
        .from(entity)
        .where(entity.get("externalId").eq(externalId))
        .buildQuery(session, entityType)
        .getSingleRowOrNull());
  }

  @Override
  public void handle(final DischargePatientEvent event)
  {

  }
}
