package com.marand.thinkmed.medications.warnings.additional.impl;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsActionDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsActionHandler;
import com.marand.thinkmed.process.service.ProcessService;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class AdditionalWarningsActionHandlerImpl implements AdditionalWarningsActionHandler
{
  private MedicationsService medicationsService;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ProcessService processService;

  @Required
  public void setMedicationsService(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  public MedicationsService getMedicationsService()
  {
    return medicationsService;
  }

  public ProcessService getProcessService()
  {
    return processService;
  }

  @Override
  public void handleAdditionalWarningsAction(@Nonnull final AdditionalWarningsActionDto additionalWarningsActionDto)
  {
    Preconditions.checkNotNull(additionalWarningsActionDto, "additionalWarningsActionDto must not be null");
    StringUtils.checkNotBlank(additionalWarningsActionDto.getPatientId(), "patientId must not be null");
    Preconditions.checkNotNull(additionalWarningsActionDto.getAbortTherapyIds(), "abortTherapyIds must not be null");
    Preconditions.checkNotNull(additionalWarningsActionDto.getOverrideWarnings(), "overrideWarnings must not be null");
    Preconditions.checkNotNull(additionalWarningsActionDto.getCompleteTaskIds(), "completeTaskIds must not be null");

    for (final String id : additionalWarningsActionDto.getAbortTherapyIds())
    {
      final Pair<String, String> therapyIdPair = TherapyIdUtils.parseTherapyId(id);
      medicationsService.abortTherapy(
          additionalWarningsActionDto.getPatientId(),
          therapyIdPair.getFirst(),
          therapyIdPair.getSecond(),
          null);
    }

    additionalWarningsActionDto.getOverrideWarnings()
        .forEach(w -> medicationsOpenEhrDao.appendWarningsToTherapy(
            additionalWarningsActionDto.getPatientId(),
            w.getTherapyId(),
            w.getWarnings()));

    additionalWarningsActionDto.getCompleteTaskIds().forEach(id -> processService.completeTasks(id));
  }
}
