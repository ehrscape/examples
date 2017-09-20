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

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Klavdij Lapajne
 */
public abstract class PerfusionSyringeTaskDef extends TherapyTaskDef
{
  public static final TaskVariable IS_URGENT = TaskVariable.named("isUrgent");
  public static final TaskVariable NUMBER_OF_SYRINGES = TaskVariable.named("numberOfSyringes");
  public static final TaskVariable PREPARATION_STARTED_TIME_MILLIS = TaskVariable.named("preparationStartedTimeMillis");

  public static final TaskVariable ORDERER = TaskVariable.named("orderer");
  public static final TaskVariable ORDERER_FULL_NAME = TaskVariable.named("ordererFullName");
  public static final TaskVariable PRINT_SYSTEM_LABEL = TaskVariable.named("printSystemLabel");
}
