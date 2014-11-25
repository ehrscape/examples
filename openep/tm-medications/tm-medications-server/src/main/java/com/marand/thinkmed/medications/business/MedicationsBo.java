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

package com.marand.thinkmed.medications.business;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.openehr.util.OpenEhrLinkType;
import com.marand.thinkmed.api.core.data.NamedIdentity;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.dto.MedicationSearchDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyCardInfoDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyFlowRowDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTaskDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTimelineRowDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.Link;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Mitja Lapajne
 */
public interface MedicationsBo
{
  MedicationOrderComposition saveNewMedicationOrder(
      long patientId,
      List<TherapyDto> therapiesList,
      Long centralCaseId,
      Long careProviderId,
      long userId,
      NamedIdentity prescriber,
      RoundsIntervalDto roundsInterval,
      DateTime when,
      Locale locale);

  List<TherapyFlowRowDto> getTherapyFlow(
      long patientId,
      long centralCaseId,
      Double patientHeight,
      DateTime startDate,
      int dayCount,
      Integer todayIndex,
      RoundsIntervalDto roundsInterval,
      TherapySortTypeEnum therapySortTypeEnum,
      KnownClinic department,
      DateTime currentTime,
      Locale locale);

  TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      long patientId,
      String compositionUid,
      String ehrOrderName,
      RoundsIntervalDto roundsInterval,
      DateTime when);

  <M extends TherapyDto> Pair<MedicationOrderComposition, MedicationInstructionInstruction> modifyTherapy(
      long patientId,
      M therapy,
      Long centralCaseId,
      Long careProviderId,
      RoundsIntervalDto roundsInterval,
      long userId,
      NamedIdentity prescriber,
      DateTime when,
      boolean alwaysOverrideTherapy,
      Locale locale);

  void abortTherapy(
      long patientId,
      String compositionUid,
      String ehrOrderName,
      long userId,
      DateTime when);

  void abortTherapy(
      long patientId,
      MedicationOrderComposition composition,
      MedicationInstructionInstruction instruction,
      long userId,
      DateTime when);

  String reviewTherapy(long patientId, String compositionUid, String ehrOrderName, long userId, DateTime when);

  String suspendTherapy(long patientId, String compositionUid, String ehrOrderName, long userId, DateTime when);

  String suspendTherapy(
      long patientId,
      MedicationOrderComposition composition,
      MedicationInstructionInstruction instruction,
      long userId,
      DateTime when);

  boolean isTherapySuspended(
      MedicationOrderComposition composition, MedicationInstructionInstruction instruction);

  void reissueTherapy(
      long patientId,
      String compositionUid,
      String ehrOrderName,
      RoundsIntervalDto roundsInterval,
      long userId, DateTime when);

  String getTherapyFormattedDisplay(long patientId, String therapyId, DateTime when, Locale locale);

  TherapyDto convertInstructionToTherapyDto(
      MedicationOrderComposition composition,
      MedicationInstructionInstruction instruction,
      Double referenceWeight,
      Double patientHeight,
      DateTime currentTime,
      boolean isToday,
      Locale locale);

  List<MedicationForWarningsSearchDto> getTherapiesForWarningsSearch(long patientId, DateTime when);

  List<TherapyDto> getTherapies(
      final long patientId,
      @Nullable final Long centralCaseId,
      final Double referenceWeight,
      @Nullable final Locale locale,
      final DateTime when);

  List<TherapyDto> getTherapies(
      long patientId,
      @Nullable Interval searchInterval,
      Double referenceWeight,
      Double patientHeight,
      @Nullable Locale locale,
      final DateTime when);

  List<TherapyDto> getPatientTherapiesForReport(
      long patientId,
      Double patientHeight,
      DateTime searchStart,
      RoundsIntervalDto roundsIntervalDto,
      DateTime currentTime);

  List<HourMinuteDto> getPossibleAdministrations(AdministrationTimingDto administrationTiming, String frequency);

  int getTherapyConsecutiveDay(DateTime therapyStart, DateTime therapyDay, DateTime currentTime, Integer pastDaysOfTherapy);

  TherapyCardInfoDto getTherapyCardInfoData(
      long patientId,
      Double patientHeight,
      String compositionId,
      String ehrOrderName,
      Locale locale,
      Interval similarTherapiesInterval,
      DateTime when);

  Pair<MedicationOrderComposition, MedicationInstructionInstruction> getOriginalTherapy(
      Long patientId, String compositionUid, String ehrOrderName);

  boolean isTherapyActive(List<String> daysOfWeek, Integer dosingDaysFrequency, Interval therapyInterval, DateTime when);

  boolean isMedicationTherapyCompleted(
      MedicationOrderComposition composition, MedicationInstructionInstruction instruction);

  DateTime getTherapyStart(long patientId, String compositionUid, String ehrOrderName);

  Pair<MedicationOrderComposition, MedicationInstructionInstruction> getInstructionFromLink(long patientId, Link link);

  Long getMainMedicationId(final TherapyDto therapy);

  MedicationReferenceWeightComposition buildReferenceWeightComposition(double weight, DateTime when, long composerId);

  void saveConsecutiveDays(Long patientId, String compositionUid, String ehrOrderName, Long userId, Integer consecutiveDays);

  void fillInfusionFormulaFromRate(ComplexTherapyDto therapy, Double referenceWeight, Double patientHeight, DateTime when);

  void fillInfusionRateFromFormula(ComplexTherapyDto therapy, Double referenceWeight, Double patientHeight, DateTime when);

  List<TherapyTimelineRowDto> buildTherapyTimeline(
      long patientId,
      long centralCaseId,
      List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      Map<String, List<MedicationAdministrationComposition>> administrations,
      @Nullable List<TherapyTaskDto> tasks,
      Interval tasksInterval,
      RoundsIntervalDto roundsInterval,
      TherapySortTypeEnum therapySortTypeEnum,
      KnownClinic department,
      DateTime when,
      Locale locale);

  String confirmTherapyAdministration(
      String therapyCompositionUid,
      String ehrOrderName,
      Long patientId,
      Long userId,
      Long centralCaseId,
      Long careProviderId,
      AdministrationDto administrationDto,
      boolean administrationSuccessful,
      DateTime when,
      Locale locale);

  String confirmMedicationAdministration(
      Pair<MedicationOrderComposition, MedicationInstructionInstruction> therapyInstructionPair,
      Long patientId,
      Long userId,
      Long centralCaseId,
      Long careProviderId,
      AdministrationDto administrationDto,
      MedicationActionEnum medicationActionEnum,
      DateTime when);

  void deleteAdministration(long patientId, String administrationId, String comment);

  boolean areInstructionsLinkedByUpdate(
      Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair);

  boolean doesInstructionHaveLinkToCompareInstruction(
      MedicationInstructionInstruction instruction,
      Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair,
      OpenEhrLinkType linkType);

  DocumentationTherapiesDto findTherapyGroupsForDocumentation(
      Long patientId,
      Long centralCseId,
      Interval centralCaseEffective,
      List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs,
      final KnownClinic department,
      boolean isOutpatient,
      DateTime when,
      Locale locale);

  List<MedicationSearchDto> filterMedicationsTree(List<MedicationSearchDto> medications, String searchString);
}
