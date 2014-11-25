/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.marand.thinkmed.medications.service;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

import com.marand.maf.core.jboss.remoting.Cacheable;
import com.marand.thinkmed.api.core.data.Catalog;
import com.marand.thinkmed.api.core.data.Named;
import com.marand.thinkmed.api.core.data.NamedIdentity;
import com.marand.thinkmed.api.medical.data.Care;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSearchDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.dto.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyCardInfoDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.TherapyViewDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTimelineRowDto;
import com.marand.thinkmed.medications.dto.report.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medicationsexternal.dto.DoseRangeCheckDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.security.access.annotation.Secured;

/**
 * @author Bostjan Vester
 */
@Secured("ROLE_User")
public interface MedicationsService
{
  @Cacheable(autoEvictMillis = 0L)
  List<MedicationSimpleDto> findMedications();

  List<MedicationSearchDto> findMedications(final String searchString);

  @Cacheable(autoEvictMillis = 0L)
  MedicationDataDto getMedicationData(long medicationId);

  @Cacheable(autoEvictMillis = 0L)
  List<DoseRangeCheckDto> findDoseRangeChecks(long medicationId);

  List<MedicationsWarningDto> findMedicationWarnings(
      long patientAgeInDays,
      Double patientWeightInKg,
      Integer gabInWeeks,
      Double bsaInM2,
      boolean isFemale,
      List<String> diseaseTypeCodes,
      List<String> patientAllergiesList,
      List<MedicationForWarningsSearchDto> medicationSummaries);

  @Cacheable(autoEvictMillis = 0L)
  String getMedicationOverviewExternalId(long medicationId);

  void tagTherapyForPrescription(Long patientId, String compositionId, Long centralCaseId, String ehrOrderName);

  void untagTherapyForPrescription(Long patientId, String compositionId, Long centralCaseId, String ehrOrderName);

  void saveNewMedicationOrder(
      long patientId,
      List<TherapyDto> therapyList,
      Long centralCaseId,
      Long careProviderId,
      Long sessionId,
      Named knownOrganizationalEntity,
      NamedIdentity prescriber,
      @Nullable DateTime saveDateTime,
      Locale locale);

  TherapyFlowDto getTherapyFlow(
      long patientId,
      long centralCaseId,
      Double patientHeight,
      DateTime startDate,
      int dayCount,
      Integer todayIndex,
      RoundsIntervalDto roundsInterval,
      TherapySortTypeEnum therapySortTypeEnum,
      KnownClinic department,
      Locale locale);

  TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      long patientId,
      String compositionUid,
      String ehrOrderName,
      RoundsIntervalDto roundsInterval);

  void modifyTherapy(
      long patientId,
      TherapyDto therapy,
      Long centralCaseId,
      Long careProviderId,
      Long sessionId,
      Named knownOrganizationalEntity,
      NamedIdentity prescriber,
      DateTime saveDateTime,
      Locale locale);

  void abortTherapy(long patientId, String compositionUid, String ehrOrderName);

  void abortAllTherapies(long patientId, DateTime when);

  void reviewTherapy(
      long patientId,
      String compositionUid,
      String ehrOrderName,
      Long sessionId,
      Named knownOrganizationalEntity);

  void suspendTherapy(long patientId, String compositionUid, String ehrOrderName);

  void reissueTherapy(
      long patientId,
      String compositionUid,
      String ehrOrderName,
      Long sessionId,
      Named knownOrganizationalEntity);

  void createAdditionalAdministrationTask(
      String therapyCompositionUid,
      String ehrOrderName,
      Long patientId,
      StartAdministrationDto administrationDto);

  void suspendAllTherapies(long patientId, DateTime when);

  List<MedicationForWarningsSearchDto> getTherapiesForWarningsSearch(long patientId);

  PatientDataForMedicationsDto getPatientData(Long patientId, Long episodeId);

  TherapyViewDto getTherapyViewDto(Long patientId, Catalog knownOrganizationalEntity);

  byte[] getMedicationDocument(String reference);

  List<Interval> getPatientBaselineInfusionIntervals(Long patientId);

  PatientDataForTherapyReportDto getTherapyDayPatientReportData(
      long patientId,
      Long encounterId,
      Care encounterCare,
      Locale locale,
      DateTime when);

  TherapyDayReportDto getTherapyDayReportData(
      long patientId,
      Double patientHeight,
      Long encounterId,
      Care encounterCare,
      RoundsIntervalDto roundsInterval,
      Locale locale,
      DateTime when);

  String getTherapyFormattedDisplay(long patientId, String therapyId, Locale locale);

  TherapyCardInfoDto getTherapyCardInfoData(
      long patientId,
      Double patientHeight,
      String compositionId,
      String ehrOrderName,
      Interval similarTherapiesInterval,
      Locale locale);

  TherapyDto fillTherapyDisplayValues(TherapyDto therapy, Locale locale);

  @Cacheable(autoEvictMillis = 0L)
  List<MedicationDto> findSimilarMedications(long medicationId, String routeCode);

  @Cacheable(autoEvictMillis = 0L)
  List<MedicationDto> getMedicationProducts(long medicationId, String routeCode);

  @Cacheable(autoEvictMillis = 0L)
  List<MedicationRouteDto> getRoutes();

  @Cacheable(autoEvictMillis = 0L)
  List<DoseFormDto> getDoseForms();

  @Cacheable(autoEvictMillis = 0L)
  List<String> getMedicationBasicUnits();

  void savePatientReferenceWeight(long patientId, double weight);

  void saveConsecutiveDays(Long patientId, String compositionUid, String ehrOrderName, Integer consecutiveDays);

  List<TherapyDto> getLastTherapiesForPreviousHospitalization(long patientId, Double patientHeight, Locale locale);

  TherapyTemplatesDto getTherapyTemplates(
      final Long userId,
      @Nullable final Long departmentId,
      @Nullable final Long patientId,
      @Nullable Double referenceWeight,
      @Nullable Double height,
      Locale locale);

  long saveTherapyTemplate(final TherapyTemplateDto therapyTemplate);

  void deleteTherapyTemplate(long templateId);

  List<TherapyTimelineRowDto> getTherapyTimelineData(
      long patientId,
      long centralCaseId,
      Interval interval,
      RoundsIntervalDto roundsInterval,
      TherapySortTypeEnum therapySortTypeEnum,
      KnownClinic department,
      Locale locale);

  DocumentationTherapiesDto getTherapyDataForDocumentation(
      Long patientId,
      Interval centralCaseEffective,
      Long centralCaseId,
      KnownClinic department,
      boolean isOutpatient,
      Locale locale);

  void confirmTherapyAdministration(
      String therapyCompositionUid,
      String ehrOrderName,
      Long patientId,
      AdministrationDto administrationDto,
      boolean editMode,
      boolean administrationSuccessful,
      Long centralCaseId,
      String departmentName,
      Long careProviderId,
      Long sessionId,
      Locale locale);

  void rescheduleTasks(
      String taskId,
      DateTime fromTime,
      long moveTimeInMillis,
      boolean rescheduleSingleTask,
      String therapyId);

  void deleteTask(String taskId, String comment);

  void deleteAdministration(long patientId, String administrationId, String taskId, String comment);

  List<? extends NamedIdentity> getCareProfessionals();
}
