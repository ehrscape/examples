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

package com.marand.thinkmed.medications.automatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;
import com.marand.maf.core.server.util.ProxyUtils;
import com.marand.maf.core.service.ConstantUserMetadataProvider;
import com.marand.maf.core.service.RequestContextHolder;
import com.marand.maf.core.service.RequestContextImpl;
import com.marand.maf_test.core.security.remoting.TestingGlobalAuditContext;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.medications.charting.NormalInfusionAutomaticChartingDto;
import com.marand.thinkmed.medications.charting.SelfAdminAutomaticChartingDto;
import com.marand.thinkmed.medications.charting.TherapyAutomaticChartingDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.TherapyCacheInvalidator;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */
@RunWith(SpringProxiedJUnit4ClassRunner.class)
@ContextConfiguration({"/com/marand/thinkmed/medications/automatic/AdministrationAutoTaskConfirmerTest-context.xml"})
@DbUnitConfiguration(databaseConnection = {"dbUnitDatabaseConnection"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@Transactional
public class AdministrationAutoTaskConfirmerTest
{
  @Autowired
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Autowired
  private MedicationsTasksProvider medicationsTasksProvider;

  @Autowired
  private AdministrationAutoTaskConfirmerHandler administrationAutoTaskConfirmerHandler;

  @Autowired
  private TherapyCacheInvalidator therapyCacheInvalidator;

  @Autowired
  private AdministrationAutoTaskConfirmer administrationAutoTaskConfirmer;

  @After
  public void tearDown() throws Exception
  {
    RequestContextHolder.clearContext();
    Mockito.validateMockitoUsage();
  }

  @Test
  public void testAutoConfirmTasks()
  {
    RequestContextHolder.setContext(
        new RequestContextImpl(
            1L,
            TestingGlobalAuditContext.INSTANCE,
            new DateTime(2015, 11, 20, 22, 5),
            Opt.of(ConstantUserMetadataProvider.createMetadata("1", "username"))));

    final List<TherapyAutomaticChartingDto> therapyAutomaticChartingDtos = new ArrayList<>();
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c1",
        "i1",
        "p1",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c2",
        "i2",
        "p1",
        new DateTime(2015, 11, 20, 9, 55),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c3",
        "i3",
        "p2",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c4",
        "i4",
        "p2",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c5",
        "i5",
        "p3",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c6",
        "i6",
        "p4",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c7",
        "i7",
        "p5",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c8",
        "i8",
        "p6",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c9",
        "i9",
        "p7",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c10",
        "i10",
        "p8",
        new DateTime(2015, 12, 14, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));

    therapyAutomaticChartingDtos.add(new NormalInfusionAutomaticChartingDto("c11", "i11", "p8"));
    therapyAutomaticChartingDtos.add(new NormalInfusionAutomaticChartingDto("c12", "i12", "p8"));

    Mockito
        .when(
            medicationsOpenEhrDao.getAutoChartingTherapyDtos(ArgumentMatchers.any(DateTime.class)))
        .thenReturn(therapyAutomaticChartingDtos);

    final List<TaskDto> taskDtos = new ArrayList<>();

    // now -> 2015, 11, 20, 22, 5

    //composition 1 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 10, 5))); // Y
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 9, 55))); // n
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 9, 50))); // n
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 11, 5))); // Y
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 12, 5))); // Y

    //composition 2 2015, 11, 20, 9, 55
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 9, 55))); // Y
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 9, 50))); // n
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 8, 50))); // n
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 7, 5))); // n
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 12, 5))); // Y

    //composition 3 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c3", "i3", new DateTime(2015, 11, 20, 12, 0))); // Y

    //composition 5 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 12, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 12, 20, 7, 0))); // Y

    //composition 6 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 11, 20, 8, 0))); // n
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 11, 20, 8, 0))); // n
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 11, 20, 8, 0))); // n
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 12, 20, 8, 0))); // n
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 12, 20, 7, 0))); // n

    //composition 11 2015, 11, 20, 10, 0
    taskDtos.add(createLinkedStopTask("c11", "i11", new DateTime(2015, 12, 20, 12, 0))); // Y
    taskDtos.add(createLinkedStopTask("c11", "i11", new DateTime(2015, 12, 20, 7, 0))); // Y

    //composition 12 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c12", "i12", new DateTime(2015, 12, 20, 12, 0))); // n
    taskDtos.add(createTestingTaskDto("c12", "i12", new DateTime(2015, 12, 20, 7, 0))); // n

    Mockito
        .when(
            medicationsTasksProvider.findAdministrationTasks(
                ArgumentMatchers.anySetOf(String.class),
                ArgumentMatchers.any(DateTime.class),
                ArgumentMatchers.any(DateTime.class)))
        .thenReturn(taskDtos);

    final MedicationOrderComposition medicationOrderComposition = new MedicationOrderComposition();
    final MedicationInstructionInstruction instruction = new MedicationInstructionInstruction();

    Mockito
        .when(
            medicationsOpenEhrDao.getTherapyInstructionPair(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
        .thenReturn(Pair.of(medicationOrderComposition, instruction));

    administrationAutoTaskConfirmer.run();

    Mockito.verify(
        ProxyUtils.unwrapAdvisedProxy(administrationAutoTaskConfirmerHandler),
        Mockito.times(30)).autoConfirmAdministrationTask(
        ArgumentMatchers.any(AutomaticChartingType.class),
        ArgumentMatchers.anyString(),
        ArgumentMatchers.any(MedicationOrderComposition.class),
        ArgumentMatchers.any(TaskDto.class),
        ArgumentMatchers.any(DateTime.class));

    Mockito.verify(ProxyUtils.unwrapAdvisedProxy(therapyCacheInvalidator), Mockito.times(4)).invalidateTherapyTasksCache(ArgumentMatchers.anyString());
    Mockito.verify(ProxyUtils.unwrapAdvisedProxy(medicationsOpenEhrDao), Mockito.times(5))
        .getTherapyInstructionPair(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
  }

  private TaskDto createLinkedStopTask(final String compositionId, final String instructionName, final DateTime dueDate)
  {
    final TaskDto task = createTestingTaskDto(compositionId, instructionName, dueDate);
    task.getVariables()
        .put(AdministrationTaskDef.GROUP_UUID.getName(), "uuid");
    task.getVariables()
        .put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), AdministrationTypeEnum.STOP.name());
    return task;
  }

  private TaskDto createTestingTaskDto(final String compositionId, final String instructionName, final DateTime dueDate)
  {
    final TaskDto taskDto = new TaskDto();
    taskDto.setVariables(new HashMap<>());
    taskDto.getVariables()
        .put(AdministrationTaskDef.THERAPY_ID.getName(), TherapyIdUtils.createTherapyId(compositionId, instructionName));
    taskDto.setDueTime(dueDate);
    taskDto.getVariables()
        .put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), AdministrationTypeEnum.START.name());
    return taskDto;
  }
}
