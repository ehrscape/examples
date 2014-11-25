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
import java.util.Map;
import javax.annotation.Nullable;

import com.marand.maf.core.Pair;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.medications.tdo.MedicationReferenceWeightComposition;
import org.joda.time.Interval;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Bostjan Vester
 */
public interface EhrMedicationsDao
{
  List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> findMedicationInstructions(
      long patientId, @Nullable Interval searchInterval, @Nullable Long centralCaseId);

  MedicationOrderComposition saveNewMedicationOrderComposition(long patientId, MedicationOrderComposition order);

  String modifyMedicationOrderComposition(long patientId, MedicationOrderComposition order);

  Map<String, List<MedicationAdministrationComposition>> getTherapiesAdministrations(
      Long patientId, List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructionPairs);

  MedicationOrderComposition loadMedicationOrderComposition(long patientId, String compositionUid);

  List<Interval> getPatientBaselineInfusionIntervals(Long patientId, Interval interval);

  Double getPatientLastReferenceWeight(Long patientId, Interval searchInterval);

  void savePatientReferenceWeight(long patientId, MedicationReferenceWeightComposition comp);

  Pair<MedicationOrderComposition, MedicationInstructionInstruction> getTherapyInstructionPair(
      long patientId, String compositionUid, String ehrOrderName);

  String saveMedicationAdministrationComposition(
      long patientId, MedicationAdministrationComposition composition, String uid);

  void deleteTherapy(long patientId, String compositionUid);

  void deleteTherapyAdministration(long patientId, String administrationCompositionUid, String comment);
}
