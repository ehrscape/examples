package com.marand.thinkmed.medications.warnings.additional.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Opt;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.mentalHealth.CheckMentalHealthMedsTaskDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthAllowedMedicationsDo;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import com.marand.thinkmed.medications.mentalhealth.MentalHealthFormProvider;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.warnings.MentalHealthWarningsHandler;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsProvider;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class MentalHealthAdditionalWarningsProvider implements AdditionalWarningsProvider
{
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationsBo medicationsBo;
  private MentalHealthFormProvider mentalHealthFormProvider;
  private MentalHealthWarningsHandler mentalHealthWarningsHandler;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ProcessService processService;

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setMentalHealthFormProvider(final MentalHealthFormProvider mentalHealthFormProvider)
  {
    this.mentalHealthFormProvider = mentalHealthFormProvider;
  }

  @Required
  public void setMentalHealthWarningsHandler(final MentalHealthWarningsHandler mentalHealthWarningsHandler)
  {
    this.mentalHealthWarningsHandler = mentalHealthWarningsHandler;
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

  @Override
  public Opt<AdditionalWarningsDto> getAdditionalWarnings(
      @Nonnull final String patientId,
      @Nonnull final PatientDataForMedicationsDto patientData,
      @Nonnull final DateTime when,
      @Nonnull final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId must be defined");
    Preconditions.checkNotNull(patientData, "patientData must not be null");
    Preconditions.checkNotNull(when, "when must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");

    final List<String> taskIds = medicationsTasksProvider.findNewCheckMentalHealthMedsTasks(patientId)
        .stream()
        .map(CheckMentalHealthMedsTaskDto::getTaskId)
        .collect(Collectors.toList());

    if (taskIds.isEmpty())
    {
      return Opt.none();
    }

    final Opt<MentalHealthDocumentDto> document = mentalHealthFormProvider.getLatestMentalHealthDocument(patientId);
    if (document.isAbsent())
    {
      taskIds.forEach(id -> processService.completeTasks(id));
      return Opt.none();
    }

    final MentalHealthAllowedMedicationsDo allowedMedications = mentalHealthWarningsHandler.getAllowedMedications(document.get());

    final List<ConflictTherapy> conflictTherapies =
        findTherapies(patientId, when, patientData, locale)
            .stream()
            .map(t -> new ConflictTherapy(t, getConflictMedications(t, allowedMedications)))
            .filter(c -> !CollectionUtils.isEmpty(c.getConflictMedications()))
            .collect(Collectors.toList());

    if (conflictTherapies.isEmpty())
    {
      taskIds.forEach(id -> processService.completeTasks(id));
      return Opt.none();
    }

    return Opt.of(buildAdditionalWarnings(conflictTherapies, taskIds));
  }

  List<NamedExternalDto> getConflictMedications(
      final TherapyDto therapy,
      final MentalHealthAllowedMedicationsDo allowedMedications)
  {
    final List<Long> routeIds = therapy.getRoutes().stream().map(MedicationRouteDto::getId).collect(Collectors.toList());
    return therapy.getMedications()
        .stream()
        .filter(m -> m.getId() != null)
        .filter(m -> medicationsBo.isMentalHealthMedication(m.getId()))
        .filter(mentalHealthWarningsHandler.isMedicationWithRoutesAllowed(routeIds, allowedMedications).negate())
        .map(m -> new NamedExternalDto(String.valueOf(m.getId()), m.getName()))
        .collect(Collectors.toList());
  }

  private AdditionalWarningsDto buildAdditionalWarnings(
      final Collection<ConflictTherapy> conflictTherapies,
      final Collection<String> taskIds)
  {
    final List<TherapyAdditionalWarningDto> therapyAdditionalWarnings = conflictTherapies
        .stream()
        .map(this::buildTherapyAdditionalWarning)
        .collect(Collectors.toList());

    final AdditionalWarningsDto result = new AdditionalWarningsDto();
    result.setWarnings(therapyAdditionalWarnings);
    result.setTaskIds(new HashSet<>(taskIds));
    return result;
  }

  private TherapyAdditionalWarningDto buildTherapyAdditionalWarning(final ConflictTherapy conflictTherapy)
  {
    final List<AdditionalWarningDto> additionalWarnings = conflictTherapy.getConflictMedications()
        .stream()
        .map(this::buildAdditionalWarning)
        .collect(Collectors.toList());

    return new TherapyAdditionalWarningDto(conflictTherapy.getTherapyDto(), additionalWarnings);
  }

  private AdditionalWarningDto buildAdditionalWarning(final NamedExternalDto medication)
  {
    final MedicationsWarningDto warningDto = mentalHealthWarningsHandler.buildMentalHealthMedicationsWarning(medication);

    return new AdditionalWarningDto(AdditionalWarningsType.MENTAL_HEALTH, warningDto);
  }

  List<TherapyDto> findTherapies(
      final String patientId,
      final DateTime when,
      final PatientDataForMedicationsDto patientData,
      final Locale locale)
  {
    return medicationsOpenEhrDao.findMedicationInstructions(patientId, Intervals.infiniteFrom(when), null)
        .stream()
        .map(p -> medicationsBo.convertInstructionToTherapyDtoWithDisplayValues(
            p.getFirst(),
            p.getSecond(),
            patientData.getWeightInKg(),
            patientData.getHeightInCm(),
            when,
            true,
            locale))
        .collect(Collectors.toList());
  }

  private static class ConflictTherapy
  {
    private final TherapyDto therapyDto;
    private final Collection<NamedExternalDto> conflictMedications;

    private ConflictTherapy(final TherapyDto therapyDto, final Collection<NamedExternalDto> conflictMedications)
    {
      this.therapyDto = therapyDto;
      this.conflictMedications = conflictMedications;
    }

    public TherapyDto getTherapyDto()
    {
      return therapyDto;
    }

    public Collection<NamedExternalDto> getConflictMedications()
    {
      return conflictMedications;
    }
  }
}
