package com.marand.thinkmed.medications.automatic;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.ServiceMethod;
import com.marand.maf.core.service.auditing.Auditing;
import com.marand.maf.core.service.auditing.Level;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.AutomaticAdministrationTaskCreatorDto;
import com.marand.thinkmed.medications.therapy.TherapyCacheInvalidator;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */
public class AdministrationAutoTaskCreator implements Runnable
{
  private static final Logger LOG = LoggerFactory.getLogger(AdministrationAutoTaskCreator.class);

  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private AdministrationAutoTaskCreatorHandler administrationAutoTaskCreatorHandler;
  private TherapyCacheInvalidator therapyCacheInvalidator;
  private MedicationsBo medicationsBo;
  private String username;
  private String password;

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setAdministrationAutoTaskCreatorHandler(final AdministrationAutoTaskCreatorHandler administrationAutoTaskCreatorHandler)
  {
    this.administrationAutoTaskCreatorHandler = administrationAutoTaskCreatorHandler;
  }

  @Required
  public void setTherapyCacheInvalidator(final TherapyCacheInvalidator therapyCacheInvalidator)
  {
    this.therapyCacheInvalidator = therapyCacheInvalidator;
  }

  @Required
  public void setUsername(final String username)
  {
    this.username = username;
  }

  @Required
  public void setPassword(final String password)
  {
    this.password = password;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Override
  @Transactional
  @ServiceMethod(auditing = @Auditing(level = Level.DISABLED, useImplementationName = true))
  public void run()
  {
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, password));
    createAdministrationTasks();
  }

  private void createAdministrationTasks()
  {
    final DateTime now = RequestContextHolder.getContext().getRequestTimestamp();
    final long startMillis = new DateTime().getMillis();

    final Map<MedicationOrderComposition, String> nonSuspendedInstructionsMap =
        medicationsOpenEhrDao.getActiveInstructionPairsWithPatientIds(now)
            .entrySet()
            .stream()
            .filter(entry -> !medicationsBo.isTherapySuspended(entry.getKey().getFirst(), entry.getKey().getSecond()))
            .collect(Collectors.toMap(e -> e.getKey().getFirst(), Map.Entry::getValue));

    final List<AutomaticAdministrationTaskCreatorDto> autoAdministrationTaskCreatorDtos =
        administrationAutoTaskCreatorHandler.getAutoAdministrationTaskCreatorDtos(now, nonSuspendedInstructionsMap);

    final Set<String> processedPatientIds = new HashSet<>();
    int count = 0;
    for (final AutomaticAdministrationTaskCreatorDto dto : autoAdministrationTaskCreatorDtos)
    {
      try
      {
        administrationAutoTaskCreatorHandler.createAdministrationTasksOnAutoCreate(dto, now);
        processedPatientIds.add(dto.getPatientId());
        count++;
      }
      catch (final Throwable t)
      {
        final String compUid = Opt.resolve(() -> dto.getTherapyDto().getCompositionUid()).get();
        LOG.error("Failed creating tasks - patientId: " + dto.getPatientId() + " compositionUid:" + compUid + "\n" + ExceptionUtils.getFullStackTrace(t));
      }
    }

    for (final String patientId : processedPatientIds)
    {
      try
      {
        therapyCacheInvalidator.invalidateTherapyTasksCache(patientId);
      }
      catch (final Throwable t)
      {
        LOG.error("Failed invalidation patient cache - patientId : " + patientId);
      }
    }

    final long endMillis = new DateTime().getMillis();
    LOG.debug("Successfully processed " + count + " therapies - TOOK: " + (endMillis - startMillis) + " ms");
  }
}
