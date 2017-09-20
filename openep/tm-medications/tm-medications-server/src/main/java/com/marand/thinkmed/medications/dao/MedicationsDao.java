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

package com.marand.thinkmed.medications.dao;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.dto.CodedNameDto;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateMemberDto;
import com.marand.thinkmed.medications.dto.supply.MedicationSupplyCandidateDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public interface MedicationsDao
{
  Map<Long, MedicationHolderDto> loadMedicationsMap(DateTime when);

  MedicationDataDto getMedicationData(long medicationId, String careProviderId, @Nonnull DateTime when);

  Map<Long, MedicationDataDto> getMedicationDataMap(
      @Nonnull Set<Long> medicationIds,
      String careProvideIds,
      @Nonnull DateTime when);

  Set<Long> getMedicationIdsWithIngredientRule(MedicationRuleEnum medicationRuleEnum, DateTime when);

  List<Long> getMedicationIdsWithIngredientId(long ingredientId, DateTime when);

  List<MedicationRouteDto> getMedicationRoutes(long medicationId, DateTime when);

  String getMedicationExternalId(String externalSystem, long medicationId, DateTime when);

  Set<Long> findSimilarMedicationsIds(long medicationId, @Nonnull Collection<Long> routeIds, @Nonnull DateTime when);

  boolean isProductBasedMedication(long medicationId);

  List<MedicationSupplyCandidateDto> getMedicationSupplyCandidates(
      final long medicationId,
      final long routeId,
      final DateTime when);

  List<MedicationDto> getMedicationChildProducts(
      long medicationId,
      @Nonnull Collection<Long> routeIds,
      @Nonnull DateTime when);

  MedicationDto getMedicationById(Long medicationId, DateTime when);

  Map<Long, MedicationDto> getMedicationsMap(Set<Long> medicationIds, DateTime when);

  DoseFormDto getDoseFormByCode(String doseFormDto, DateTime when);

  Map<String, String> getMedicationExternalValues(
      String externalSystem, MedicationsExternalValueType valueType, Set<String> valuesSet);

  List<MedicationRouteDto> getRoutes(DateTime when);

  List<DoseFormDto> getDoseForms(DateTime when);

  List<String> getMedicationBasicUnits();

  Map<Long, Pair<String, Integer>> getCustomGroupNameSortOrderMap(String careProviderId, Set<Long> medicationsCodes);

  List<String> getCustomGroupNames(String careProviderId);

  TherapyTemplatesDto getTherapyTemplates(
      @Nonnull String patientId,
      @Nonnull String userId,
      @Nonnull TherapyTemplateModeEnum templateMode,
      String careProviderId,
      Double referenceWeight,
      Double patientHeight,
      Locale locale);

  long saveTherapyTemplate(TherapyTemplateDto therapyTemplate, TherapyTemplateModeEnum templateMode, final String userId);

  void deleteTherapyTemplate(long templateId);

  Map<Long, MedicationLevelEnum> getMedicationsLevels(Set<Long> longs);

  String getPatientLastLinkName(long patientId);

  void savePatientLastLinkName(long patientId, String lastLinkName);

  Map<ActionReasonType, List<CodedNameDto>> getActionReasons(@Nonnull DateTime when, ActionReasonType type);

  List<MentalHealthTemplateDto> getMentalHealthTemplates();

  List<MentalHealthTemplateMemberDto> getMentalHealthTemplateMembers(@Nonnull Collection<Long> mentalHealthTemplateIds);

  Map<Long, MedicationRouteDto> loadRoutesMap(DateTime when);

  Collection<MedicationsWarningDto> getCustomWarningsForMedication(@Nonnull Set<Long> medicationIds, @Nonnull DateTime when);

  Long getMedicationIdForBarcode(@Nonnull String barcode);
}
