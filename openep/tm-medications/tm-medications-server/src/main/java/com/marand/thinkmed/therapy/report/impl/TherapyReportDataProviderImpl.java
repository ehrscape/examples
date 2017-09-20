package com.marand.thinkmed.therapy.report.impl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.MedicationPreferencesUtil;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.report.TherapyDayElementReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportElementDto;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.report.TherapyReportDataProvider;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class TherapyReportDataProviderImpl implements TherapyReportDataProvider
{
  private MedicationsDao medicationsDao;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsBo medicationsBo;
  private MedicationsConnector medicationsConnector;
  private OverviewContentProvider overviewContentProvider;
  private AdministrationProvider administrationProvider;

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
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Required
  public void setOverviewContentProvider(final OverviewContentProvider overviewContentProvider)
  {
    this.overviewContentProvider = overviewContentProvider;
  }

  @Required
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Override
  public TherapyDayReportDto getTherapyReportData(
      @Nonnull final String patientId,
      @Nonnull final Locale locale,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(patientId, "patientId must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");
    Preconditions.checkNotNull(when, "when must not be null");

    final TherapyDayReportDto reportDto = new TherapyDayReportDto(false);

    final PatientDataForTherapyReportDto therapyDayPatientReportData =
        getTherapyDayPatientReportData(patientId, true, locale, when);

    if (therapyDayPatientReportData == null)
    {
      return null;
    }

    reportDto.setPatientSortOrder(0);
    reportDto.setPatientData(therapyDayPatientReportData);

    final DateTime startOfFirstDay = when.minusDays(3).withTimeAtStartOfDay();
    final DateTime endOfLastDay = when.plusDays(4).withTimeAtStartOfDay();
    final Interval searchInterval = new Interval(startOfFirstDay, endOfLastDay);

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, searchInterval, null);

    final List<AdministrationDto> administrations = administrationProvider.getTherapiesAdministrations(
        patientId,
        instructionsList,
        null);

    final List<TherapyRowDto> therapyRowDtoList = overviewContentProvider.buildTherapyRows(
        patientId,
        instructionsList,
        administrations,
        Collections.emptyList(),
        TherapySortTypeEnum.CREATED_TIME_ASC,
        false,
        Collections.emptyList(),
        null,
        searchInterval,
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        locale,
        when);

    final List<TherapyDayElementReportDto> simpleElementsList = new ArrayList<>();
    final List<TherapyDayElementReportDto> complexElementsList = new ArrayList<>();

    final Map<String, Long> therapyIdWithMedicationIdMap = getTherapyIdWithMedicationIdsMap(therapyRowDtoList);

    final Map<Long, Pair<String, Integer>> customGroupNameSortOrderMap =
        medicationsDao.getCustomGroupNameSortOrderMap(
            therapyDayPatientReportData.getCareProviderId(),
            new HashSet<>(therapyIdWithMedicationIdMap.values()));

    for (final TherapyRowDto therapyRowDto : therapyRowDtoList)
    {
      final TherapyDto therapy = therapyRowDto.getTherapy();
      if (therapy != null)
      {
        final TherapyDayElementReportDto elementDto =
            createTherapyDayElementReportDto(
                therapy,
                therapyRowDto.getConsecutiveDay(),
                therapyRowDto.getTherapyStatus(),
                customGroupNameSortOrderMap,
                therapyIdWithMedicationIdMap.get(therapyRowDto.getTherapyId()),
                when,
                locale);

        final TherapyPharmacistReviewStatusEnum pharmacistsReviewState = therapyRowDto.getTherapyPharmacistReviewStatus();
        if (pharmacistsReviewState != null)
        {
          elementDto.setPharmacistsReviewState(pharmacistsReviewState);
        }

        elementDto.setAdministrations(therapyRowDto.getAdministrations());

        if (therapy instanceof SimpleTherapyDto)
        {
          simpleElementsList.add(elementDto);
        }
        else
        {
          complexElementsList.add(elementDto);
        }
      }
    }

    sortSimpleTherapyReportElements(simpleElementsList);
    sortComplexTherapyReportElements(complexElementsList);

    reportDto.setSimpleElements(simpleElementsList);
    reportDto.setComplexElements(complexElementsList);

    return reportDto;
  }

  private void sortComplexTherapyReportElements(final List<TherapyDayElementReportDto> complexElementsList)
  {
    final Collator collator = Collator.getInstance();
    Collections.sort(
        complexElementsList, (o1, o2) -> {
          final boolean firstOrderIsBaselineInfusion = ((ComplexTherapyDto)o1.getOrder()).isBaselineInfusion();
          final boolean secondOrderIsBaselineInfusion = ((ComplexTherapyDto)o2.getOrder()).isBaselineInfusion();

          //inactive therapies last
          final boolean firstTherapyActive = !isTherapyFinished(o1);
          final boolean secondTherapyActive = !isTherapyFinished(o2);
          if (firstTherapyActive && !secondTherapyActive)
          {
            return -1;
          }
          if (!firstTherapyActive && secondTherapyActive)
          {
            return 1;
          }
          //baseline infusions first
          if (firstOrderIsBaselineInfusion && !secondOrderIsBaselineInfusion)
          {
            return -1;
          }
          if (!firstOrderIsBaselineInfusion && secondOrderIsBaselineInfusion)
          {
            return 1;
          }
          return collator.compare(o1.getOrder().getTherapyDescription(), o2.getOrder().getTherapyDescription());
        });
  }

  private boolean isTherapyFinished(final TherapyDayElementReportDto reportDto)
  {
    return reportDto.getTherapyReportStatusEnum() == TherapyReportStatusEnum.FINISHED;
  }

  private void sortSimpleTherapyReportElements(final List<TherapyDayElementReportDto> simpleElementsList)
  {
    final Collator collator = Collator.getInstance();
    Collections.sort(
        simpleElementsList, (o1, o2) -> {
          //inactive therapies last
          final boolean firstTherapyActive = !isTherapyFinished(o1);
          final boolean secondTherapyActive = !isTherapyFinished(o2);
          if (firstTherapyActive && !secondTherapyActive)
          {
            return -1;
          }
          if (!firstTherapyActive && secondTherapyActive)
          {
            return 1;
          }
          return collator.compare(o1.getOrder().getTherapyDescription(), o2.getOrder().getTherapyDescription());
        });
  }

  @Override
  public TherapySurgeryReportDto getTherapySurgeryReportData(
      @Nonnull final String patientId,
      final Double patientHeight,
      @Nonnull final RoundsIntervalDto roundsInterval,
      @Nonnull final Locale locale,
      @Nonnull final DateTime when)
  {
    Preconditions.checkNotNull(patientId, "patientId");
    Preconditions.checkNotNull(roundsInterval, "roundsInterval");
    Preconditions.checkNotNull(locale, "locale");
    Preconditions.checkNotNull(when, "when");

    final PatientDataForTherapyReportDto patientReportData = getTherapyDayPatientReportData(
        patientId,
        false,
        locale,
        when);

    if (patientReportData == null)
    {
      return null;
    }

    final List<TherapySurgeryReportElementDto> elements = medicationsBo.getTherapySurgeryReportElements(
        patientId,
        patientHeight,
        when,
        roundsInterval,
        locale,
        when);

    return new TherapySurgeryReportDto(patientReportData, elements, DateTimeFormatters.shortDate(locale).print(when));
  }

  private PatientDataForTherapyReportDto getTherapyDayPatientReportData(
      final String patientId,
      final boolean mainDiseaseTypeOnly,
      final Locale locale,
      final DateTime when)
  {
    final PatientDataForTherapyReportDto patientData =
        medicationsConnector.getPatientDataForTherapyReport(patientId, mainDiseaseTypeOnly, when, locale);
    if (patientData != null)
    {
      final Double referenceWeight =
          medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, Intervals.infiniteTo(when));
      patientData.setWeight(referenceWeight != null ? (referenceWeight + " " + "kg") : null);
    }

    return patientData;
  }

  private Map<String, Long> getTherapyIdWithMedicationIdsMap(final List<TherapyRowDto> therapyRowDtoList)
  {
    final Map<String, Long> therapyIdWithMedicationIdMap = new HashMap<>();

    for (final TherapyRowDto therapyRowDto : therapyRowDtoList)
    {
      final Long mainMedicationId = therapyRowDto.getTherapy().getMainMedicationId();
      if (mainMedicationId != null)
      {
        therapyIdWithMedicationIdMap.put(therapyRowDto.getTherapyId(), mainMedicationId);
      }
    }

    return therapyIdWithMedicationIdMap;
  }

  private TherapyDayElementReportDto createTherapyDayElementReportDto(
      final TherapyDto therapy,
      final int therapyConsecutiveDay,
      final TherapyStatusEnum therapyStatus,
      final Map<Long, Pair<String, Integer>> customGroupNameSortOrderMap,
      final Long mainMedicationId,
      final DateTime when,
      final Locale locale)
  {
    final TherapyDayElementReportDto reportElement = new TherapyDayElementReportDto();
    reportElement.setTherapyConsecutiveDay(String.valueOf(therapyConsecutiveDay));
    reportElement.setOrder(therapy);
    reportElement.setTherapyStart(DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(therapy.getStart()));

    final DateTime therapyEnd = therapy.getEnd();
    if (therapyEnd != null)
    {
      reportElement.setTherapyEnd(DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(therapyEnd));
    }

    final boolean therapyExpired = therapyEnd != null && therapyEnd.isBefore(when);
    if (therapyExpired || therapyStatus == TherapyStatusEnum.CANCELLED || therapyStatus == TherapyStatusEnum.ABORTED)
    {
      reportElement.setTherapyReportStatusEnum(TherapyReportStatusEnum.FINISHED);
    }
    else if (therapyStatus == TherapyStatusEnum.SUSPENDED)
    {
      reportElement.setTherapyReportStatusEnum(TherapyReportStatusEnum.SUSPENDED);
    }
    else
    {
      reportElement.setTherapyReportStatusEnum(TherapyReportStatusEnum.ACTIVE);
    }

    final Long medicationId = mainMedicationId == null ? therapy.getMainMedicationId() : mainMedicationId;
    if (medicationId != null)
    {
      final Pair<String, Integer> customGroupNameSortOrderPair = customGroupNameSortOrderMap.get(medicationId);

      if (customGroupNameSortOrderPair != null)
      {
        reportElement.setCustomGroupName(customGroupNameSortOrderPair.getFirst());
        reportElement.setCustomGroupSortOrder(
            customGroupNameSortOrderPair.getSecond() != null ? customGroupNameSortOrderPair.getSecond() : Integer.MAX_VALUE);
      }
      else
      {
        reportElement.setCustomGroupSortOrder(Integer.MAX_VALUE);
      }
    }
    else
    {
      reportElement.setCustomGroupSortOrder(Integer.MAX_VALUE);
    }

    return reportElement;
  }
}
