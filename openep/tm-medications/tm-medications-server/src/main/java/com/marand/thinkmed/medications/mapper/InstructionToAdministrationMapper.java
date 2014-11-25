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

package com.marand.thinkmed.medications.mapper;

import java.util.ArrayList;

import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;

import static com.marand.openehr.medications.tdo.MedicationOrderComposition.MedicationDetailSection.MedicationInstructionInstruction;

/**
 * @author Klavdij Lapajne
 */
public class InstructionToAdministrationMapper
{
  private InstructionToAdministrationMapper()
  {
  }

  public static void map(
      final MedicationInstructionInstruction instruction,
      final MedicationAdministrationComposition administrationComposition)
  {
    administrationComposition.setMedicationDetail(new MedicationAdministrationComposition.MedicationDetailSection());
    administrationComposition.getMedicationDetail().setMedicationAction(new ArrayList<MedicationActionAction>());

    for (final MedicationInstructionInstruction.OrderActivity orderActivity : instruction.getOrder())
    {
      final MedicationActionAction medicationAction = new MedicationActionAction();
      medicationAction.setMedicine(orderActivity.getMedicine());
      medicationAction.setIngredientsAndForm(orderActivity.getIngredientsAndForm());
      medicationAction.setStructuredDose(orderActivity.getStructuredDose());
      medicationAction.setAdministrationDetails(orderActivity.getAdministrationDetails());
      administrationComposition.getMedicationDetail().getMedicationAction().add(medicationAction);
    }
  }
}
