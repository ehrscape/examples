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

package com.marand.thinkmed.medications.task.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.AdministrationTaskCreator;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskHandler;
import com.marand.thinkmed.medications.pharmacist.PreparePerfusionSyringeProcessHandler;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;

/**
 * @author Mitja Lapajne
 */
@RunWith(MockitoJUnitRunner.class)
public class MedicationsTasksHandlerTest
{
  @InjectMocks
  private MedicationsTasksHandler medicationsTasksHandler = new MedicationsTasksHandlerImpl();

  @Mock
  private AdministrationTaskCreator administrationTaskCreator;

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private ProcessService processService;

  @Test
  public void testSetAdministrationTitratedDose()
  {
    final List<Pair<TaskVariable, Object>> variables = new ArrayList<>();
    variables.add(Pair.of(AdministrationTaskDef.DOSE_TYPE, TherapyDoseTypeEnum.QUANTITY));
    variables.add(Pair.of(AdministrationTaskDef.DOSE_NUMERATOR, 500.0));
    variables.add(Pair.of(AdministrationTaskDef.DOSE_NUMERATOR_UNIT, "mg"));
    Mockito
        .when(administrationTaskCreator.getDoseTaskVariables(any(TherapyDoseDto.class)))
        .thenReturn(variables);

    final List<TaskDto> tasks = new ArrayList<>();
    final TaskDto task2 = new TaskDto();
    task2.setId("task2");
    tasks.add(task2);
    final TaskDto task3 = new TaskDto();
    task3.setId("task3");
    tasks.add(task3);
    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            anyString(), anyListOf(String.class), any(DateTime.class), any(DateTime.class), isNull(), anyBoolean()))
        .thenReturn(tasks);

    medicationsTasksHandler.setAdministrationTitratedDose(
        "patient1",
        "therapy1",
        "task1",
        new TherapyDoseDto(),
        null,
        new DateTime(),
        new DateTime());

    final Map<String, Object> variablesMap = new HashMap<>();
    variablesMap.put(AdministrationTaskDef.DOSE_TYPE.getName(), TherapyDoseTypeEnum.QUANTITY);
    variablesMap.put(AdministrationTaskDef.DOSE_NUMERATOR.getName(), 500.0);
    variablesMap.put(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName(), "mg");
    Mockito.verify(processService, times(1)).setVariables("task1", variablesMap);
    Mockito.verify(processService, times(1)).setVariables("task2", variablesMap);
    Mockito.verify(processService, times(1)).setVariables("task3", variablesMap);
  }
}
