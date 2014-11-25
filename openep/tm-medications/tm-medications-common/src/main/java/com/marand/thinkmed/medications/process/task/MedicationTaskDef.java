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

package com.marand.thinkmed.medications.process.task;

import com.marand.thinkmed.process.definition.TaskDef;
import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Bostjan Vester
 */
public class MedicationTaskDef extends TaskDef
{
  public static final MedicationTaskDef INSTANCE = new MedicationTaskDef();
  private static final String TASK_EXECUTION_ID = "TherapyTask";
  private static final String GROUP_NAME = "Medications";
  public static final TaskVariable PATIENT_ID = TaskVariable.named("patientId");
  public static final TaskVariable THERAPY_ID = TaskVariable.named("therapyId");
  public static final TaskVariable THERAPY_ADMINISTRATION_ID = TaskVariable.named("therapyAdministrationId");
  public static final TaskVariable DELETE_COMMENT = TaskVariable.named("deleteReason");
  public static final TaskVariable ADMINISTRATION_TYPE = TaskVariable.named("administrationType");
  public static final TaskVariable DOSE_TYPE = TaskVariable.named("doseType");
  public static final TaskVariable DOSE_NUMERATOR = TaskVariable.named("doseNumerator");
  public static final TaskVariable DOSE_NUMERATOR_UNIT = TaskVariable.named("doseNumeratorUnit");
  public static final TaskVariable DOSE_DENOMINATOR = TaskVariable.named("doseDenominator");
  public static final TaskVariable DOSE_DENOMINATOR_UNIT = TaskVariable.named("doseDenominatorUnit");
  public static final TaskVariable TRIGGERS_THERAPY_ID = TaskVariable.named("triggersTherapyId");

  @Override
  public final String getGroupName()
  {
    return GROUP_NAME;
  }

  @Override
  public String getTaskExecutionId()
  {
    return TASK_EXECUTION_ID;
  }
}
