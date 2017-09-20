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

import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.ispek.ehr.common.tdo.IspekComposition;
import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction.OrderActivity;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import com.marand.thinkmed.medications.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeGroupDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportElementDto;
import com.marand.thinkmed.medicationsexternal.dto.MedicationForWarningsSearchDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.Link;

/**
 * @author Mitja Lapajne
 */
public interface MedicationsBo
{
  Map<Long, MedicationDataForTherapyDto> getMedicationDataForTherapies(
      List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionsList,
      @Nullable String careProviderId);

  boolean isTherapyModifiedFromLastReview(
      @Nonnull MedicationInstructionInstruction instruction,
      @Nonnull List<MedicationActionAction> actionsList,
      @Nonnull DateTime compositionCreatedTime);

  boolean isTherapySuspended(MedicationOrderComposition composition, MedicationInstructionInstruction instruction);

  TherapyChangeReasonDto getTherapySuspendReason(
      @Nonnull MedicationOrderComposition composition,
      @Nonnull MedicationInstructionInstruction instruction);

  boolean isTherapyCancelledOrAborted(
      MedicationOrderComposition composition, MedicationInstructionInstruction instruction);

  String getTherapyFormattedDisplay(String patientId, String therapyId, DateTime when, Locale locale);

  TherapyDto getTherapy(
      String patientId, String compositionId, String ehrOrderName, DateTime when, Locale locale);

  TherapyDto convertInstructionToTherapyDto(
      @Nonnull IspekComposition composition,
      @Nonnull MedicationInstructionInstruction instruction,
      DateTime currentTime);

  TherapyDto convertInstructionToTherapyDtoWithDisplayValues(
      @Nonnull IspekComposition composition,
      @Nonnull MedicationInstructionInstruction instruction,
      Double referenceWeight,
      Double patientHeight,
      DateTime currentTime,
      boolean isToday,
      Locale locale);

  void fillDisplayValues(@Nonnull TherapyDto therapy, Double referenceWeight, Double patientHeight, boolean isToday, Locale locale);

  double calculateBodySurfaceArea(final double heightInCm, final double weightInKg);

  List<MedicationForWarningsSearchDto> getTherapiesForWarningsSearch(String patientId, DateTime when);

  List<TherapyDto> getTherapies(
      final String patientId,
      @Nullable final String centralCaseId,
      final Double referenceWeight,
      @Nullable final Locale locale,
      final DateTime when);

  List<TherapyDto> getTherapies(
      String patientId,
      @Nullable Interval searchInterval,
      Double referenceWeight,
      Double patientHeight,
      @Nullable Locale locale,
      final DateTime when);

  List<MedicationOnDischargeGroupDto> getMedicationOnDischargeGroups(
      String patientId,
      DateTime lastHospitalizationStart,
      Interval searchInterval,
      Double referenceWeight,
      Double patientHeight,
      @Nullable Locale locale,
      DateTime when);

  List<MentalHealthTherapyDto> getMentalHealthTherapies(
      String patientId,
      Interval searchInterval,
      DateTime when,
      Locale locale);

  List<TherapyDto> getLinkTherapyCandidates(
      @Nonnull String patientId,
      @Nonnull Double referenceWeight,
      Double patientHeight,
      @Nonnull DateTime when,
      @Nonnull Locale locale);

  List<TherapySurgeryReportElementDto> getTherapySurgeryReportElements(
      @Nonnull String patientId,
      Double patientHeight,
      @Nonnull DateTime searchStart,
      @Nonnull RoundsIntervalDto roundsIntervalDto,
      @Nonnull Locale locale,
      @Nonnull DateTime when);

  List<MedicationForWarningsSearchDto> extractWarningsSearchDtos(@Nonnull List<MedicationOrderComposition> compositions);

  int getTherapyConsecutiveDay(DateTime therapyStart, DateTime therapyDay, DateTime currentTime, Integer pastDaysOfTherapy);

  String getOriginalTherapyId(String patientId, String compositionUid);

  String getOriginalTherapyId(@Nonnull MedicationOrderComposition composition);

  boolean isMentalHealthMedication(long medicationId);

  boolean isTherapyActive(List<String> daysOfWeek, Integer dosingDaysFrequency, Interval therapyInterval, DateTime when);

  boolean isMedicationTherapyCompleted(
      MedicationOrderComposition composition, MedicationInstructionInstruction instruction);

  MedicationActionAction getInstructionAction(
      MedicationOrderComposition composition,
      MedicationInstructionInstruction instruction,
      MedicationActionEnum searchActionEnum,
      @Nullable Interval searchInterval);

  DateTime getOriginalTherapyStart(@Nonnull String patientId, @Nonnull MedicationOrderComposition composition);

  Pair<MedicationOrderComposition, MedicationInstructionInstruction> getInstructionFromLink(
      @Nonnull String patientId,
      @Nonnull MedicationInstructionInstruction instruction,
      @Nonnull EhrLinkType linkType,
      boolean getLatestVersion);

  Pair<MedicationOrderComposition, MedicationInstructionInstruction> getInstructionFromLink(
      String patientId,
      Link link,
      boolean getLatestVersion);

  Long getMainMedicationId(OrderActivity orderActivity);

  MedicationReferenceWeightComposition buildReferenceWeightComposition(double weight, DateTime when);

  void fillInfusionFormulaFromRate(ComplexTherapyDto therapy, Double referenceWeight, Double patientHeight);

  void fillInfusionRateFromFormula(ComplexTherapyDto therapy, Double referenceWeight, Double patientHeight);

  boolean isTherapySuspended(final List<MedicationActionAction> actionsList);

  void sortTherapiesByMedicationTimingStart(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> therapies,
      final boolean descending);

  void deleteAdministration(String patientId, String administrationId, String comment);

  void sortTherapyTemplates(final TherapyTemplatesDto templates);

  int compareTherapiesForSort(final TherapyDto firstTherapy, final TherapyDto secondTherapy, final Collator collator);

  boolean areInstructionsLinkedByUpdate(
      Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair,
      Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair);

  boolean doesInstructionHaveLinkToCompareInstruction(
      MedicationInstructionInstruction instruction,
      Pair<MedicationOrderComposition, MedicationInstructionInstruction> compareInstructionPair,
      EhrLinkType linkType);

  List<Long> getMedicationIds(@Nonnull OrderActivity orderActivity);

  List<Long> getMedicationIds(@Nonnull MedicationActionAction medicationAction);

  DocumentationTherapiesDto findTherapyGroupsForDocumentation(
      String patientId,
      String centralCseId,
      Interval centralCaseEffective,
      List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs,
      boolean isOutpatient,
      DateTime when,
      Locale locale);

  DateTime findPreviousTaskForTherapy(
      String patientId,
      String compositionUid,
      String ehrOrderName,
      DateTime when);
}
