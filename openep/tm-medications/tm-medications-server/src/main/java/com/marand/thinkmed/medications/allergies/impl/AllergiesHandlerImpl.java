package com.marand.thinkmed.medications.allergies.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.allergies.AllergiesHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.warnings.TherapyWarningsProvider;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.WarningType;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationWarningsDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class AllergiesHandlerImpl implements AllergiesHandler
{
  private MedicationsBo medicationsBo;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private TherapyWarningsProvider therapyWarningsProvider;
  private MedicationsTasksHandler medicationsTasksHandler;

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setTherapyWarningsProvider(final TherapyWarningsProvider therapyWarningsProvider)
  {
    this.therapyWarningsProvider = therapyWarningsProvider;
  }

  @Required
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Override
  public void handleNewAllergies(
      @Nonnull final String patientId,
      @Nonnull final PatientDataForMedicationsDto patientData,
      @Nonnull final Collection<NamedExternalDto> newAllergies,
      @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId must be defined");
    Preconditions.checkNotNull(newAllergies, "newAllergies must not be null");
    Preconditions.checkNotNull(patientData, "patientData must not be null");
    Preconditions.checkNotNull(when, "when must not be null");

    if (!getAllergyWarnings(patientId, patientData, newAllergies, when).isEmpty())
    {
      medicationsTasksHandler.createCheckNewAllergiesTask(patientId, newAllergies, when);
    }
  }

  @Override
  public List<MedicationsWarningDto> getAllergyWarnings(
      @Nonnull final String patientId,
      @Nonnull final PatientDataForMedicationsDto patientData,
      @Nonnull final Collection<NamedExternalDto> allergies,
      @Nonnull final DateTime when)
  {
    StringUtils.checkNotBlank(patientId, "patientId must be defined");
    Preconditions.checkNotNull(allergies, "allergies must not be null");
    Preconditions.checkNotNull(patientData, "patientData must not be null");
    Preconditions.checkNotNull(when, "when must not be null");

    if (allergies.isEmpty())
    {
      return new ArrayList<>();
    }

    final List<MedicationOrderComposition> compositions = findMedicationOrderCompositions(patientId, when);
    final List<MedicationForWarningsSearchDto> summaries = extractWarningsSearchDtos(compositions);

    final Double patientWeight = Opt
        .resolve(patientData::getWeightInKg)
        .or(() -> Opt.of(medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, Intervals.infiniteTo(when))))
        .orElse(null);

    final Double bsaInM2 = Opt
        .resolve(() -> medicationsBo.calculateBodySurfaceArea(patientData.getHeightInCm(), patientData.getWeightInKg()))
        .orElse(null);

    final MedicationWarningsDto medicationWarnings = therapyWarningsProvider.findMedicationWarnings(
        patientId,
        new Interval(patientData.getBirthDate(), when).toDuration().getStandardDays(),
        patientWeight,
        bsaInM2,
        patientData.getGender() == Gender.FEMALE,
        patientData.getDiseases(),
        new ArrayList<>(allergies),
        summaries,
        Collections.singleton(WarningSeverity.HIGH),
        false,
        RequestContextHolder.getContext().getRequestTimestamp());

    return medicationWarnings.getWarnings()
        .stream()
        .filter(m -> m.getType() == WarningType.ALLERGY)
        .collect(Collectors.toList());
  }

  private List<MedicationOrderComposition> findMedicationOrderCompositions(final String patientId, final DateTime when)
  {
    return medicationsOpenEhrDao.findMedicationInstructions(patientId, Intervals.infiniteFrom(when), null)
        .stream()
        .map(Pair::getFirst)
        .collect(Collectors.toList());
  }

  private List<MedicationForWarningsSearchDto> extractWarningsSearchDtos(final List<MedicationOrderComposition> compositions)
  {
    final List<MedicationForWarningsSearchDto> therapies = medicationsBo.extractWarningsSearchDtos(compositions);
    therapies.forEach(t -> t.setProspective(true));
    return therapies;
  }
}
