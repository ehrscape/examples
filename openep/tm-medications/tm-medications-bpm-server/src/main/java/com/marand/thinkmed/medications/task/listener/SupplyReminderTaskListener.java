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

import com.marand.thinkmed.bpm.medications.process.PharmacySupplyProcess;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.TaskListenerUtils;
import com.marand.thinkmed.process.TaskConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */
public class SupplyReminderTaskListener implements TaskListener
{
  @Override
  public void notify(final DelegateTask delegateTask)
  {
    if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE))
    {
      final DelegateExecution execution = delegateTask.getExecution();
      final String patientId = (String)execution.getVariable(PharmacySupplyProcess.patientId.name());
      final String originalTherapyId = (String)execution.getVariable(PharmacySupplyProcess.originalTherapyId.name());
      delegateTask.setName(SupplyReminderTaskDef.getTaskTypeEnum().buildKey(patientId));
      delegateTask.setAssignee(TherapyAssigneeEnum.PHARMACIST.name());
      final DateTime createTime = new DateTime(delegateTask.getCreateTime());
      delegateTask.setDueDate(
          createTime.plusDays((Integer)execution.getVariable(PharmacySupplyProcess.supplyInDays.name())).toDate());

      final Map<String, Object> variablesMap = new HashMap<>();
      TaskListenerUtils.setTherapyTaskDefVariables(patientId, originalTherapyId, variablesMap);
      variablesMap.put(TaskConstants.TASK_EXECUTION_ID_VARIABLE_NAME, SupplyReminderTaskDef.TASK_EXECUTION_ID);
      variablesMap.put(
          SupplyReminderTaskDef.DAYS_SUPPLY.getName(),
          execution.getVariable(PharmacySupplyProcess.supplyInDays.name()));
      variablesMap.put(
          SupplyReminderTaskDef.IS_DISMISSED.getName(),
          false);
      variablesMap.put(
          SupplyReminderTaskDef.SUPPLY_TYPE.getName(),
          execution.getVariable(PharmacySupplyProcess.supplyType.name()));

      delegateTask.setVariablesLocal(variablesMap);
    }
  }
}
