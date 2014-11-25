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

package com.marand.thinkmed.medications.process.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.maf.core.PartialList;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.TherapyTaskDto;
import com.marand.thinkmed.medications.process.task.MedicationTaskDef;
import com.marand.thinkmed.medications.process.utils.TherapyTaskUtils;
import com.marand.thinkmed.medications.provider.TherapyTasksProvider;
import com.marand.thinkmed.process.TaskCompletedType;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Mitja Lapajne
 */
public class TherapyTaskProviderImpl implements TherapyTasksProvider, InitializingBean
{
  private ProcessService processService;

  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    Assert.notNull(processService, "processService is required");
  }

  @Override
  public List<TherapyTaskDto> findTherapyTasks(final Long patientId, final String therapyId, final Interval searchInterval)
  {
    final List<Pair<TaskVariable, Object>> taskVariables = new ArrayList<>();
    if (patientId != null)
    {
      taskVariables.add(Pair.<TaskVariable, Object>of(MedicationTaskDef.PATIENT_ID, patientId));
    }
    if (therapyId != null)
    {
      taskVariables.add(Pair.<TaskVariable, Object>of(MedicationTaskDef.THERAPY_ID, therapyId));
    }
    final List<TaskDto> allTasks = processService.findTasks(
        null,
        null,
        null,
        true,
        searchInterval != null ? searchInterval.getStart() : null,
        searchInterval != null ? searchInterval.getEnd() : null,
        taskVariables.toArray(new Pair[taskVariables.size()]));

    final List<TherapyTaskDto> therapyTasks = new ArrayList<>();
    for (final TaskDto task : allTasks)
    {
      if (task.getCompletedType() != TaskCompletedType.DELETED)
      {
        final TherapyTaskDto therapyTaskDto = new TherapyTaskDto();
        therapyTaskDto.setTaskId(task.getId());
        therapyTaskDto.setTherapyId((String)task.getVariables().get(MedicationTaskDef.THERAPY_ID.getName()));
        therapyTaskDto.setPlannedAdministrationTime(task.getDueTime());
        therapyTaskDto.setAdministrationTypeEnum(
            AdministrationTypeEnum.valueOf(
                (String)task.getVariables().get(MedicationTaskDef.ADMINISTRATION_TYPE.getName())));
        therapyTaskDto.setAdministrationId(
            (String)task.getVariables().get(MedicationTaskDef.THERAPY_ADMINISTRATION_ID.getName()));
        therapyTaskDto.setTriggersTherapyId(
            (String)task.getVariables().get(MedicationTaskDef.TRIGGERS_THERAPY_ID.getName()));
        if (therapyTaskDto.getAdministrationTypeEnum() != AdministrationTypeEnum.STOP)
        {
          final TherapyDoseDto therapyDoseDto = TherapyTaskUtils.buildTherapyDoseDtoFromTask(task);
          therapyTaskDto.setTherapyDoseDto(therapyDoseDto);
        }
        therapyTasks.add(therapyTaskDto);
      }
    }
    return therapyTasks;
  }

  @Override
  public void associateTaskWithAdministration(final String taskId, final String administrationCompositionUid)
  {
    processService.setVariable(taskId, MedicationTaskDef.THERAPY_ADMINISTRATION_ID.getName(), administrationCompositionUid);
  }

  @Override
  public void completeTasks(final String... taskIds)
  {
    processService.completeTasks(taskIds);
  }

  @Override
  public void rescheduleTasks(
      final String taskId,
      final DateTime fromTime,
      final long moveTimeInMillis,
      final boolean rescheduleSingleTask,
      final String therapyId)
  {
    if (!rescheduleSingleTask)
    {
      final PartialList<TaskDto> therapyTasks = processService.findTasks(
          null,
          MedicationTaskDef.INSTANCE.getGroupName(),
          MedicationTaskDef.INSTANCE.getTaskExecutionId(),
          false,
          fromTime,
          null,
          Pair.of(MedicationTaskDef.THERAPY_ID, therapyId));

      for (final TaskDto therapyTask : therapyTasks)
      {
        final long newTimeInMillis = therapyTask.getDueTime().getMillis() + moveTimeInMillis;
        processService.setDueDate(therapyTask.getId(), new DateTime(newTimeInMillis));
      }
    }
    processService.setDueDate(taskId, new DateTime(fromTime.getMillis() + moveTimeInMillis));
  }

  @Override
  public void deleteTask(final String taskId, final String comment)
  {
    processService.setVariable(taskId, MedicationTaskDef.DELETE_COMMENT.getName(), comment);
    processService.deleteTasks(Collections.singletonList(taskId));
  }
}
