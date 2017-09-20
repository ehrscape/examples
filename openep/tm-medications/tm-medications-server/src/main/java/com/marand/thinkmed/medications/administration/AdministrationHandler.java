package com.marand.thinkmed.medications.administration;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface AdministrationHandler
{
  void confirmTherapyAdministration(
      @Nonnull String therapyCompositionUid,
      @Nonnull String ehrOrderName,
      @Nonnull String patientId,
      @Nonnull String userId,
      @Nonnull AdministrationDto administrationDto,
      boolean edit,
      boolean requestSupply,
      String centralCaseId,
      String careProviderId,
      @Nonnull Locale locale,
      @Nonnull DateTime when);

  String getGroupUUIdFromAdministrationComposition(@Nonnull String compUId);

  String autoConfirmSelfAdministration(
      @Nonnull AutomaticChartingType type,
      @Nonnull MedicationOrderComposition composition,
      @Nonnull String patientId,
      @Nonnull AdministrationDto administrationDto,
      @Nonnull DateTime when);

  String confirmTherapyAdministration(
      @Nonnull MedicationOrderComposition composition,
      @Nonnull String patientId,
      @Nonnull String userId,
      @Nonnull AdministrationDto administrationDto,
      @Nonnull MedicationActionEnum medicationActionEnum,
      boolean edit,
      String centralCaseId,
      String careProviderId,
      @Nonnull DateTime when);

  void addAdministrationsToTimelines(
      @Nonnull List<AdministrationDto> administrations,
      @Nonnull Map<String, TherapyRowDto> therapyTimelineRowsMap,
      @Nonnull Map<String, String> modifiedTherapiesMap,
      @Nonnull Interval tasksInterval);

  void deleteAdministration(
      @Nonnull String patientId,
      @Nonnull AdministrationDto administration,
      @Nonnull TherapyDoseTypeEnum therapyDoseType,
      @Nonnull String therapyId,
      String comment);
}
