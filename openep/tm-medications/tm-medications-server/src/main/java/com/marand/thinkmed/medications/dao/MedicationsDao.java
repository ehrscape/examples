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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.api.organization.data.KnownClinic;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.MedicationSearchDto;
import com.marand.thinkmed.medications.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.dto.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.TherapyTemplatesDto;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public interface MedicationsDao
{
  List<MedicationSimpleDto> findMedications(DateTime when);

  List<MedicationSearchDto> loadMedicationsTree(final DateTime when);

  MedicationDataDto getMedicationData(long medicationId, DateTime when);

  MedicationIngredientDto getMedicationDefiningIngredient(long medicationId, DateTime when);

  String getMedicationExternalId(String externalSystem, long medicationId, DateTime when);

  List<String> getIcd9Codes(String icd10Code);

  Map<Long, MedicationDataForTherapyDto> getMedicationDataForTherapies(
      final Set<Long> medicationIds,
      @Nullable final KnownClinic department,
      final DateTime when);

  List<MedicationDto> findSimilarMedications(long medicationId, String routeCode,  DateTime when);

  List<MedicationDto> getMedicationProducts(long medicationId, String routeCode,  DateTime when);

  MedicationDto getMedicationById(Long medicationId, DateTime when);

  DoseFormDto getDoseFormByCode(String doseFormDto, DateTime when);

  Map<String, String> getMedicationExternalValues(
      String externalSystem, MedicationsExternalValueType valueType, Set<String> valuesSet);

  List<MedicationRouteDto> getRoutes(final DateTime when);

  List<DoseFormDto> getDoseForms(DateTime when);

  List<String> getMedicationBasicUnits();

  Map<Long, Pair<String, Integer>> getCustomGroupNameSortOrderMap(
      final String knownOrganizationalEntityName,
      Set<Long> medicationsCodes);

  List<String> getCustomGroupNames(final String knownOrganizationalEntityName);

  TherapyTemplatesDto getTherapyTemplates(
      final Long userId,
      @Nullable final Long departmentId,
      @Nullable final Long patientId,
      @Nullable Double referenceWeight,
      @Nullable Double height,
      DateTime when,
      Locale locale);

  long saveTherapyTemplate(TherapyTemplateDto therapyTemplate);

  void deleteTherapyTemplate(long templateId);

  Map<Long, MedicationLevelEnum> getMedicationsLevels(Set<Long> longs);

  Map<Long, Long> getMedicationIdsFromExternalIds(String externalSystem, Set<String> medicationExternalIds, DateTime when);
}
