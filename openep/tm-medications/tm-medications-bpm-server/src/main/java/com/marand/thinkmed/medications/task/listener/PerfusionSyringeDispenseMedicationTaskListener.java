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

package com.marand.thinkmed.medications.task.listener;

import java.util.HashMap;
import java.util.Map;

import com.marand.thinkmed.medications.task.PerfusionSyringeDispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.TaskListenerUtils;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * @author Klavdij Lapajne
 */
public class PerfusionSyringeDispenseMedicationTaskListener implements TaskListener
{
  @Override
  public void notify(final DelegateTask delegateTask)
  {
    if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE))
    {
      //delegateTask.setAssignee(); //todo which group gets assigned ?
      final Map<String, Object> variablesMap = new HashMap<>();
      TaskListenerUtils.setPerfusionSyringeTaskDefVariables(
          delegateTask,
          PerfusionSyringeDispenseMedicationTaskDef.TASK_EXECUTION_ID,
          PerfusionSyringeDispenseMedicationTaskDef.getTaskTypeEnum(),
          variablesMap);
      delegateTask.setVariablesLocal(variablesMap);
    }
  }
}
