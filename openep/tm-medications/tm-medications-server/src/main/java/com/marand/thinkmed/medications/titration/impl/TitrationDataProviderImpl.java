package com.marand.thinkmed.medications.titration.impl;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StringUtils;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TitrationType;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.BolusQuantityWithTimeDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyForTitrationDto;
import com.marand.thinkmed.medications.dto.TitrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.overview.RateTherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.titration.TitrationDataProvider;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class TitrationDataProviderImpl implements TitrationDataProvider
{
  private MedicationsConnector medicationsConnector;
  private MedicationsDao medicationsDao;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private AdministrationProvider administrationProvider;
  private MedicationsBo medicationsBo;
  private OverviewContentProvider overviewContentProvider;

  @Required
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Required
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setOverviewContentProvider(final OverviewContentProvider overviewContentProvider)
  {
    this.overviewContentProvider = overviewContentProvider;
  }

  @Override
  public TitrationDto getDataForTitration(
      @Nonnull final String patientId,
      @Nonnull final String therapyId,
      @Nonnull final TitrationType titrationType,
      @Nonnull final DateTime searchStart,
      @Nonnull final DateTime searchEnd,
      @Nonnull final DateTime when,
      @Nonnull final Locale locale)
  {
    StringUtils.checkNotBlank(patientId, "patientId is required");
    StringUtils.checkNotBlank(patientId, "therapyId is required");
    Preconditions.checkNotNull(titrationType, "titrationType is required");
    Preconditions.checkNotNull(when, "start is required");
    Preconditions.checkNotNull(when, "when is required");
    Preconditions.checkNotNull(locale, "locale is required");

    final TitrationDto titrationDto = new TitrationDto();
    titrationDto.setTitrationType(titrationType);
    titrationDto.setName(Dictionary.getEntry(titrationType.name(), locale));
    titrationDto.setUnit(titrationType.getUnit());

    final Interval searchInterval = new Interval(searchStart, searchEnd);

    fillTherapiesAndMedicationData(patientId, therapyId, titrationType, titrationDto, searchInterval, when, locale);
    fillObservationResults(patientId, titrationDto, searchInterval);

    return titrationDto;
  }

  private void fillTherapiesAndMedicationData(
      final String patientId,
      final String administrationTherapyId,
      final TitrationType titrationType,
      final TitrationDto titrationDto,
      final Interval interval,
      final DateTime when,
      final Locale locale)
  {
    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs =
        medicationsOpenEhrDao.findMedicationInstructions(
            patientId,
            Intervals.infiniteFrom(interval.getStart()),
            null)
            .stream()
            .filter(p -> !isTherapyCanceled(p))
            .collect(Collectors.toList());

    final List<AdministrationDto> administrations = administrationProvider.getTherapiesAdministrations(
        patientId,
        instructionPairs,
        null);

    final List<TherapyRowDto> therapyRows = overviewContentProvider.buildTherapyRows(
        patientId,
        instructionPairs,
        administrations,
        Collections.emptyList(),
        TherapySortTypeEnum.DESCRIPTION_ASC,
        false,
        Collections.emptyList(),
        null,
        interval,
        null,
        locale,
        when);

    for (final TherapyRowDto therapyRow : therapyRows)
    {
      final Long mainMedicationId = therapyRow.getTherapy().getMainMedicationId();

      if (mainMedicationId != null)
      {
        final MedicationDataDto medicationData = medicationsDao.getMedicationData(mainMedicationId, null, when);

        if (medicationData.getTitration() == titrationType)
        {
          final TherapyForTitrationDto therapyForTitration = buildTherapyForTitration(therapyRow);
          titrationDto.getTherapies().add(therapyForTitration);
        }

        if (therapyRow.getTherapyId().equals(administrationTherapyId))
        {
          titrationDto.setMedicationData(medicationData);
        }
      }
    }
  }

  private boolean isTherapyCanceled(final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair)
  {
    return MedicationsEhrUtils.getInstructionActions(instructionPair.getFirst(), instructionPair.getSecond())
        .stream()
        .map(MedicationActionEnum::getActionEnum)
        .anyMatch(e -> e == MedicationActionEnum.CANCEL);
  }

  private TherapyForTitrationDto buildTherapyForTitration(final TherapyRowDto therapyRow)
  {
    final TherapyForTitrationDto therapyForTitration = new TherapyForTitrationDto();
    therapyForTitration.setTherapy(therapyRow.getTherapy());

    therapyRow.getAdministrations()
        .stream()
        .filter(administration -> AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administration.getAdministrationType()))
        .filter(administration -> (administration.getAdministrationResult() == AdministrationResultEnum.GIVEN ||
            administration.getAdministrationResult() == AdministrationResultEnum.SELF_ADMINISTERED))
        .forEach(administration -> addAdministrationToTherapyForTitration(administration, therapyForTitration));

    if (therapyRow instanceof RateTherapyRowDto)
    {
      therapyForTitration.setInfusionFormulaAtIntervalStart(((RateTherapyRowDto)therapyRow).getInfusionFormulaAtIntervalStart());
      if (therapyForTitration.getDoseUnit() == null)
      {
        therapyForTitration.setDoseUnit(((RateTherapyRowDto)therapyRow).getFormulaUnit());
      }
    }

    return therapyForTitration;
  }

  private void addAdministrationToTherapyForTitration(
      final AdministrationDto administration,
      final TherapyForTitrationDto therapyForTitration)
  {
    final QuantityWithTimeDto quantityWithTimeDto;
    String doseUnit = null;

    final DateTime administrationTime = administration.getAdministrationTime();
    final String comment = administration.getComment();

    if (administration instanceof DoseAdministration)
    {
      final TherapyDoseDto administeredDose = ((DoseAdministration)administration).getAdministeredDose();
      if (therapyForTitration.getTherapy().isWithRate())
      {
        if (administration.getAdministrationType() == AdministrationTypeEnum.BOLUS)
        {
          quantityWithTimeDto = new BolusQuantityWithTimeDto(
              administrationTime,
              null,
              comment,
              administeredDose.getNumerator(),
              administeredDose.getNumeratorUnit());
        }
        else
        {
          quantityWithTimeDto = new QuantityWithTimeDto(administrationTime, administeredDose.getDenominator(), comment);
          doseUnit = administeredDose.getDenominatorUnit();
        }
      }
      else
      {
        quantityWithTimeDto = new QuantityWithTimeDto(administrationTime, administeredDose.getNumerator(), comment);
        doseUnit = administeredDose.getNumeratorUnit();
      }
    }
    else
    {
      quantityWithTimeDto = new QuantityWithTimeDto(administrationTime, null, comment);
    }

    if (therapyForTitration.getDoseUnit() == null)
    {
      therapyForTitration.setDoseUnit(doseUnit);
    }

    therapyForTitration.getAdministrations().add(quantityWithTimeDto);
  }

  private void fillObservationResults(
      final String patientId,
      final TitrationDto titrationDto,
      final Interval interval)
  {
    final List<QuantityWithTimeDto> observationResults;
    if (titrationDto.getTitrationType() == TitrationType.BLOOD_SUGAR)
    {
      observationResults = medicationsConnector.getBloodSugarObservations(patientId, interval);
    }
    else
    {
      throw new IllegalArgumentException("Titration type " + titrationDto.getTitrationType().name() + " not supported!");
    }
    titrationDto.setResults(observationResults);
  }
}
