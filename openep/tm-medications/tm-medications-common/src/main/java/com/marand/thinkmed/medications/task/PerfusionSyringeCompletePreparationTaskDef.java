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

package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.medications.TaskTypeEnum;

/**
 * @author Klavdij Lapajne
 */
public class PerfusionSyringeCompletePreparationTaskDef extends PerfusionSyringeTaskDef
{
  private static final TaskTypeEnum taskTypeEnum = TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE;

  public static final PerfusionSyringeCompletePreparationTaskDef INSTANCE = new PerfusionSyringeCompletePreparationTaskDef();
  public static final String TASK_EXECUTION_ID = taskTypeEnum.getName();
  public static final String KEY_PREFIX = "PERFUSION_SYRINGE_COMPLETE_TASK_PATIENT_ID";

  @Override
  public String getTaskExecutionId()
  {
    return TASK_EXECUTION_ID;
  }

  public static TaskTypeEnum getTaskTypeEnum()
  {
    return taskTypeEnum;
  }
}
