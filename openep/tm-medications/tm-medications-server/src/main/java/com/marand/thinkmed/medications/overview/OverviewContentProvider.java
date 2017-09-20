package com.marand.thinkmed.medications.overview;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface OverviewContentProvider
{
  TherapyFlowDto getTherapyFlow(
      String patientId,
      String centralCaseId,
      Double patientHeight,
      DateTime startDate,
      int dayCount,
      Integer todayIndex,
      RoundsIntervalDto roundsInterval,
      TherapySortTypeEnum therapySortTypeEnum,
      @Nullable String careProviderId,
      DateTime currentTime,
      Locale locale);

  TherapyTimelineDto getTherapyTimeline(
      @Nonnull String patientId,
      @Nonnull List<AdministrationDto> administrations,
      @Nonnull List<AdministrationTaskDto> administrationTasks,
      @Nonnull List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      @Nonnull TherapySortTypeEnum therapySortTypeEnum,
      boolean hidePastTherapies,
      @Nonnull PatientDataForMedicationsDto patientData,
      Interval tasksInterval,
      RoundsIntervalDto roundsInterval,
      Locale locale,
      @Nonnull DateTime when);

  List<TherapyRowDto> buildTherapyRows(
      @Nonnull String patientId,
      @Nonnull List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      @Nonnull List<AdministrationDto> administrations,
      @Nonnull List<AdministrationTaskDto> administrationTasks,
      @Nonnull TherapySortTypeEnum therapySortTypeEnum,
      boolean hidePastTherapies,
      @Nonnull List<TherapyAdditionalWarningDto> additionalWarnings,
      PatientDataForMedicationsDto patientData,
      Interval interval,
      RoundsIntervalDto roundsInterval,
      Locale locale,
      @Nonnull DateTime when);

  Map<String, TherapyDayDto> getCompositionUidAndTherapyDayDtoMap(
      final Map<String, String> therapyCompositionUidAndPatientIdMap,
      final DateTime when,
      final Locale locale);

  Map<String, TherapyDayDto> getOriginalCompositionUidAndLatestTherapyDayDtoMap(
      Map<String, String> originalTherapyCompositionUidAndPatientIdMap,
      int searchIntervalInWeeks,
      DateTime when,
      Locale locale);

  TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      String patientId,
      String compositionUid,
      String ehrOrderName,
      RoundsIntervalDto roundsInterval,
      DateTime when);
}
