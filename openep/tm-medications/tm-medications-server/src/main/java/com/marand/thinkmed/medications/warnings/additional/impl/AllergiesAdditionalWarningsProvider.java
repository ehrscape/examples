package com.marand.thinkmed.medications.warnings.additional.impl;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Opt;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.allergies.AllergiesHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.allergies.CheckNewAllergiesTaskDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsProvider;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class AllergiesAdditionalWarningsProvider implements AdditionalWarningsProvider
{
  private MedicationsTasksProvider medicationsTasksProvider;
  private AllergiesHandler allergiesHandler;
  private MedicationsBo medicationsBo;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ProcessService processService;

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

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Required
  public void setAllergiesHandler(final AllergiesHandler allergiesHandler)
  {
    this.allergiesHandler = allergiesHandler;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
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

    final List<CheckNewAllergiesTaskDto> newAllergiesTasks = medicationsTasksProvider.findNewAllergiesTasks(patientId);
    if (newAllergiesTasks.isEmpty())
    {
      return Opt.none();
    }

    final Set<NamedExternalDto> allergies = newAllergiesTasks
        .stream()
        .flatMap(t -> t.getAllergies().stream())
        .collect(Collectors.toSet());

    final List<MedicationsWarningDto> allergiesWarnings = allergiesHandler.getAllergyWarnings(
        patientId,
        patientData,
        allergies,
        when);

    if (allergiesWarnings.isEmpty())
    {
      newAllergiesTasks
          .stream()
          .map(CheckNewAllergiesTaskDto::getTaskId)
          .forEach(id -> processService.completeTasks(id));
    }

    return Opt.of(buildTherapyAdditionalWarnings(
        newAllergiesTasks,
        allergiesWarnings,
        findTherapies(patientId, patientData, when, locale)));
  }

  private AdditionalWarningsDto buildTherapyAdditionalWarnings(
      final Collection<CheckNewAllergiesTaskDto> allergyTasks,
      final Collection<MedicationsWarningDto> allergyWarnings,
      final List<TherapyDto> therapies)
  {
    final List<TherapyAdditionalWarningDto> therapyAdditionalWarnings = therapies
        .stream()
        .map(t -> buildTherapyAdditionalWarning(t, allergyWarnings))
        .collect(Collectors.toList());

    final Set<String> taskIds = allergyTasks
        .stream()
        .map(CheckNewAllergiesTaskDto::getTaskId)
        .collect(Collectors.toSet());

    final AdditionalWarningsDto additionalWarnings = new AdditionalWarningsDto();
    additionalWarnings.setWarnings(therapyAdditionalWarnings);
    additionalWarnings.setTaskIds(taskIds);
    return additionalWarnings;
  }

  private TherapyAdditionalWarningDto buildTherapyAdditionalWarning(
      final TherapyDto therapy,
      final Collection<MedicationsWarningDto> warnings)
  {
    final List<Long> therapyMedications = therapy.getMedications()
        .stream()
        .map(MedicationDto::getId)
        .collect(Collectors.toList());

    final Predicate<MedicationsWarningDto> isWarningForTherapy = warning ->
        warning.getMedications()
            .stream()
            .filter(m -> therapyMedications.contains(Long.valueOf(m.getId())))
            .anyMatch(Objects::nonNull);

    final List<AdditionalWarningDto> additionalWarningsForTherapy = warnings
        .stream()
        .filter(isWarningForTherapy)
        .map(w -> new AdditionalWarningDto(AdditionalWarningsType.ALLERGIES, w))
        .collect(Collectors.toList());

    return new TherapyAdditionalWarningDto(therapy, additionalWarningsForTherapy);
  }

  List<TherapyDto> findTherapies(
      final String patientId,
      final PatientDataForMedicationsDto patientData,
      final DateTime when,
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
}
