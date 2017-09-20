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

import com.marand.maf.core.time.CurrentTime;
import com.marand.thinkmed.bpm.medications.process.PreparePerfusionSyringeProcess;
import com.marand.thinkmed.medications.task.PerfusionSyringeStartPreparationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeTaskDef;
import com.marand.thinkmed.medications.task.TaskListenerUtils;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * @author Klavdij Lapajne
 */
public class PerfusionSyringeStartPreparationTaskListener implements TaskListener
{
  @Override
  public void notify(final DelegateTask delegateTask)
  {
    if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE))
    {
      //delegateTask.setAssignee(); //todo which group gets assigned ?
      final Map<String, Object> variablesMap = new HashMap<>();
      delegateTask.getExecution().removeVariable(PreparePerfusionSyringeProcess.startedDateTimeMillis.name());
      TaskListenerUtils.setPerfusionSyringeTaskDefVariables(
          delegateTask,
          PerfusionSyringeStartPreparationTaskDef.TASK_EXECUTION_ID,
          PerfusionSyringeStartPreparationTaskDef.getTaskTypeEnum(),
          variablesMap);
      delegateTask.setVariablesLocal(variablesMap);
    }
    else if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_COMPLETE))
    {
      final DelegateExecution execution = delegateTask.getExecution();
      final Long completionTimeMillis = CurrentTime.get().getMillis();
      execution.setVariable(PreparePerfusionSyringeProcess.startedDateTimeMillis.name(), completionTimeMillis);
      delegateTask.setVariable(PerfusionSyringeTaskDef.PREPARATION_STARTED_TIME_MILLIS.getName(), completionTimeMillis);
    }
  }
}
