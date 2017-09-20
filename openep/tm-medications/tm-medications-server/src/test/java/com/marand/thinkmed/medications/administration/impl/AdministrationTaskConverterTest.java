package com.marand.thinkmed.medications.administration.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.marand.openehr.medications.tdo.AdministrationDetailsCluster.OxygenDeliveryCluster;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.AdministrationStatusEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.OxygenTaskDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.OxygenTaskDef;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("TooBroadScope")
@RunWith(MockitoJUnitRunner.class)
public class AdministrationTaskConverterTest
{
  @InjectMocks
  private AdministrationTaskConverterImpl administrationTaskConverter = new AdministrationTaskConverterImpl();

  @Mock
  private OverviewContentProvider overviewContentProvider;

  @Test
  public void testBuildAdministrationFromTask()
  {
    final AdministrationTaskDto taskDto = new AdministrationTaskDto();
    taskDto.setTaskId("task1");
    taskDto.setAdministrationId(null);
    taskDto.setTherapyId("therapy1");
    taskDto.setPlannedAdministrationTime(new DateTime(2014, 5, 7, 12, 0, 0));
    taskDto.setAdministrationTypeEnum(AdministrationTypeEnum.START);
    taskDto.setTherapyDoseDto(new TherapyDoseDto());
    taskDto.getTherapyDoseDto().setNumerator(50.0);
    taskDto.getTherapyDoseDto().setNumeratorUnit("mg");
    taskDto.getTherapyDoseDto().setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);

    final AdministrationDto administration =
        administrationTaskConverter.buildAdministrationFromTask(taskDto, new DateTime(2014, 5, 7, 12, 0, 0));
    assertTrue(administration instanceof StartAdministrationDto);
    final StartAdministrationDto startAdministration = (StartAdministrationDto)administration;
    assertEquals(50, startAdministration.getPlannedDose().getNumerator(), 0);
    assertEquals("mg", startAdministration.getPlannedDose().getNumeratorUnit());
    assertNull(startAdministration.getPlannedDose().getDenominator());
    assertNull(startAdministration.getPlannedDose().getDenominatorUnit());
    assertEquals(TherapyDoseTypeEnum.QUANTITY, startAdministration.getPlannedDose().getTherapyDoseTypeEnum());
    assertEquals(new DateTime(2014, 5, 7, 12, 0, 0), startAdministration.getPlannedTime());
    assertEquals("task1", startAdministration.getTaskId());
    assertEquals(AdministrationTypeEnum.START, startAdministration.getAdministrationType());
  }

  @Test
  public void testBuildBolusAdministrationFromTask()
  {
    final AdministrationTaskDto taskDto = new AdministrationTaskDto();
    taskDto.setTaskId("task1");
    taskDto.setAdministrationId(null);
    taskDto.setTherapyId("therapy1");
    taskDto.setPlannedAdministrationTime(new DateTime(2014, 5, 7, 12, 0, 0));
    taskDto.setAdministrationTypeEnum(AdministrationTypeEnum.BOLUS);
    taskDto.setTherapyDoseDto(new TherapyDoseDto());
    taskDto.getTherapyDoseDto().setNumerator(50.0);
    taskDto.getTherapyDoseDto().setNumeratorUnit("mg");
    taskDto.getTherapyDoseDto().setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);

    final AdministrationDto administration =
        administrationTaskConverter.buildAdministrationFromTask(taskDto, new DateTime(2014, 5, 7, 12, 0, 0));
    assertTrue(administration instanceof BolusAdministrationDto);
    final BolusAdministrationDto startAdministration = (BolusAdministrationDto)administration;
    assertEquals(new DateTime(2014, 5, 7, 12, 0, 0), startAdministration.getPlannedTime());
    assertEquals("task1", startAdministration.getTaskId());
    assertEquals(AdministrationTypeEnum.BOLUS, startAdministration.getAdministrationType());
  }

  @Test
  public void testConvertTasksToAdministrationPatientTasks()
  {
    final Map<String, TherapyDayDto> therapiesMap = new HashMap<>();
    Mockito
        .when(
            overviewContentProvider.getCompositionUidAndTherapyDayDtoMap(
                ArgumentMatchers.anyMapOf(String.class, String.class),
                ArgumentMatchers.any(DateTime.class),
                ArgumentMatchers.any(Locale.class))
        ).thenReturn(therapiesMap);

    final List<TaskDto> tasksList = new ArrayList<>();

    final TaskDto task1 = new TaskDto();
    task1.setId("task1");
    task1.setDueTime(new DateTime(2016, 1, 6, 10, 0));
    task1.setVariables(new HashMap<>());
    task1.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), "patient1");
    task1.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e4|Medication order");
    task1.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR.getName(), 500.0);
    task1.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName(), "mg");
    task1.getVariables().put(AdministrationTaskDef.DOSE_TYPE.getName(), TherapyDoseTypeEnum.QUANTITY.name());
    tasksList.add(task1);

    final TaskDto task2 = new TaskDto();
    task2.setId("task2");
    task2.setDueTime(new DateTime(2016, 1, 6, 12, 0));

    task2.setVariables(new HashMap<>());
    task2.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), "patient1");
    task2.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e5|Medication order");
    task2.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR.getName(), 100.0);
    task2.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName(), "ml/h");
    task2.getVariables().put(AdministrationTaskDef.DOSE_TYPE.getName(), TherapyDoseTypeEnum.RATE.name());
    tasksList.add(task2);

    final TaskDto task3 = new TaskDto();
    task3.setId("task3");
    task3.setDueTime(new DateTime(2016, 1, 6, 20, 0));
    task3.setVariables(new HashMap<>());
    task3.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), "patient2");
    task3.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e6|Medication order");
    task3.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR.getName(), 120.0);
    task3.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName(), "mg");
    task3.getVariables().put(AdministrationTaskDef.DOSE_DENOMINATOR.getName(), 5.0);
    task3.getVariables().put(AdministrationTaskDef.DOSE_DENOMINATOR_UNIT.getName(), "ml");
    task3.getVariables().put(AdministrationTaskDef.DOSE_TYPE.getName(), TherapyDoseTypeEnum.QUANTITY.name());
    tasksList.add(task3);

    final Map<String, PatientDisplayWithLocationDto> patientWithLocationMap = new HashMap<>();

    patientWithLocationMap.put(
        "patient1",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient1", "patient1", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp1", "R1B1"));
    patientWithLocationMap.put(
        "patient2",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient2", "patient2", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp2", "R2B2"));

    final List<AdministrationPatientTaskDto> patientTasks =                                       //
        administrationTaskConverter.convertTasksToAdministrationPatientTasks(
            tasksList, patientWithLocationMap, new Locale("en"), new DateTime(2016, 1, 6, 11, 50));

    assertEquals(3, patientTasks.size());

    assertEquals(AdministrationStatusEnum.LATE, patientTasks.get(0).getAdministrationStatus());
    assertEquals("task1", patientTasks.get(0).getId());
    assertEquals(TaskTypeEnum.ADMINISTRATION_TASK, patientTasks.get(0).getTaskType());
    assertEquals(new DateTime(2016, 1, 6, 10, 0), patientTasks.get(0).getPlannedTime());
    assertEquals(500.0, patientTasks.get(0).getPlannedDose().getNumerator(), 0);
    assertEquals("mg", patientTasks.get(0).getPlannedDose().getNumeratorUnit());
    assertEquals(TherapyDoseTypeEnum.QUANTITY, patientTasks.get(0).getPlannedDose().getTherapyDoseTypeEnum());
    assertEquals("patient1", patientTasks.get(0).getPatientDisplayDto().getId());
    assertEquals("cp1", patientTasks.get(0).getCareProviderName());
    assertEquals("R1B1", patientTasks.get(0).getRoomAndBed());

    assertEquals(AdministrationStatusEnum.DUE, patientTasks.get(1).getAdministrationStatus());
    assertEquals("task2", patientTasks.get(1).getId());
    assertEquals(TaskTypeEnum.ADMINISTRATION_TASK, patientTasks.get(1).getTaskType());
    assertEquals(new DateTime(2016, 1, 6, 12, 0), patientTasks.get(1).getPlannedTime());
    assertEquals(100.0, patientTasks.get(1).getPlannedDose().getNumerator(), 0);
    assertEquals("ml/h", patientTasks.get(1).getPlannedDose().getNumeratorUnit());
    assertEquals(TherapyDoseTypeEnum.RATE, patientTasks.get(1).getPlannedDose().getTherapyDoseTypeEnum());
    assertEquals("patient1", patientTasks.get(1).getPatientDisplayDto().getId());
    assertEquals("cp1", patientTasks.get(1).getCareProviderName());
    assertEquals("R1B1", patientTasks.get(1).getRoomAndBed());

    assertEquals(AdministrationStatusEnum.PLANNED, patientTasks.get(2).getAdministrationStatus());
    assertEquals("task3", patientTasks.get(2).getId());
    assertEquals(TaskTypeEnum.ADMINISTRATION_TASK, patientTasks.get(2).getTaskType());
    assertEquals(new DateTime(2016, 1, 6, 20, 0), patientTasks.get(2).getPlannedTime());
    assertEquals(120.0, patientTasks.get(2).getPlannedDose().getNumerator(), 0);
    assertEquals("mg", patientTasks.get(2).getPlannedDose().getNumeratorUnit());
    assertEquals(5.0, patientTasks.get(2).getPlannedDose().getDenominator(), 0);
    assertEquals("ml", patientTasks.get(2).getPlannedDose().getDenominatorUnit());
    assertEquals(TherapyDoseTypeEnum.QUANTITY, patientTasks.get(2).getPlannedDose().getTherapyDoseTypeEnum());
    assertEquals("patient2", patientTasks.get(2).getPatientDisplayDto().getId());
    assertEquals("cp2", patientTasks.get(2).getCareProviderName());
    assertEquals("R2B2", patientTasks.get(2).getRoomAndBed());
  }

  @Test
  public void testConvertOxygenTaskWithStartingDeviceType()
  {
    final String taskId = "task";
    final DateTime dueTime = new DateTime(2016, 1, 6, 20, 0);
    final String patientId = "patient2";
    final String therapyId = "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e6|Medication order";
    final AdministrationTypeEnum administrationType = AdministrationTypeEnum.BOLUS;
    final boolean isOxygen = true;

    final OxygenDeliveryCluster.Route startingDevice = OxygenDeliveryCluster.Route.HIGH_FLOW_TRACHEAL_CANNULA;
    final String startingDeviceType = "10";

    final TaskDto task = new TaskDto();
    task.setId(taskId);
    task.setDueTime(dueTime);
    task.setVariables(new HashMap<>());
    task.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), patientId);
    task.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);
    task.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR.getName(), 120.0);
    task.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName(), "mg");
    task.getVariables().put(AdministrationTaskDef.DOSE_DENOMINATOR.getName(), 5.0);
    task.getVariables().put(AdministrationTaskDef.DOSE_DENOMINATOR_UNIT.getName(), "ml");
    task.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), administrationType.name());
    task.getVariables().put(AdministrationTaskDef.DOSE_TYPE.getName(), TherapyDoseTypeEnum.QUANTITY.name());
    task.getVariables().put(AdministrationTaskDef.DOSE_TYPE.getName(), TherapyDoseTypeEnum.QUANTITY.name());

    //Oxygen specific variables
    task.getVariables().put(OxygenTaskDef.OXYGEN_ADMINISTRATION.getName(), isOxygen);
    task.getVariables().put(OxygenTaskDef.STARTING_DEVICE_ROUTE.getName(), startingDevice.name());
    task.getVariables().put(OxygenTaskDef.STARTING_DEVICE_TYPE.getName(), startingDeviceType);

    final AdministrationTaskDto administrationTaskDto = administrationTaskConverter.convertTaskToAdministrationTask(task);

    assertEquals(((OxygenTaskDto)administrationTaskDto).getStartingDevice().getRoute(), startingDevice);
    assertEquals(startingDeviceType, ((OxygenTaskDto)administrationTaskDto).getStartingDevice().getRouteType());
    assertEquals(taskId, administrationTaskDto.getTaskId());
    assertEquals(therapyId, administrationTaskDto.getTherapyId());
    assertEquals(dueTime, administrationTaskDto.getPlannedAdministrationTime());
    assertEquals(administrationType, administrationTaskDto.getAdministrationTypeEnum());
  }

  @Test
  public void testConvertOxygenTaskWithoutStartingDeviceType()
  {
    final String taskId = "task";
    final DateTime dueTime = new DateTime(2016, 1, 6, 20, 0);
    final String patientId = "patient2";
    final String therapyId = "e9b4cb80-9697-4a83-b7a1-7237d4ecb2e6|Medication order";
    final AdministrationTypeEnum administrationType = AdministrationTypeEnum.BOLUS;
    final boolean isOxygen = true;

    final OxygenDeliveryCluster.Route startingDevice = OxygenDeliveryCluster.Route.HIGH_FLOW_TRACHEAL_CANNULA;

    final TaskDto task = new TaskDto();
    task.setId(taskId);
    task.setDueTime(dueTime);
    task.setVariables(new HashMap<>());
    task.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), patientId);
    task.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);
    task.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR.getName(), 120.0);
    task.getVariables().put(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName(), "mg");
    task.getVariables().put(AdministrationTaskDef.DOSE_DENOMINATOR.getName(), 5.0);
    task.getVariables().put(AdministrationTaskDef.DOSE_DENOMINATOR_UNIT.getName(), "ml");
    task.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), administrationType.name());
    task.getVariables().put(AdministrationTaskDef.DOSE_TYPE.getName(), TherapyDoseTypeEnum.QUANTITY.name());
    task.getVariables().put(AdministrationTaskDef.DOSE_TYPE.getName(), TherapyDoseTypeEnum.QUANTITY.name());

    //Oxygen specific variables
    task.getVariables().put(OxygenTaskDef.OXYGEN_ADMINISTRATION.getName(), isOxygen);
    task.getVariables().put(OxygenTaskDef.STARTING_DEVICE_ROUTE.getName(), startingDevice.name());

    final AdministrationTaskDto administrationTaskDto = administrationTaskConverter.convertTaskToAdministrationTask(task);

    assertEquals(((OxygenTaskDto)administrationTaskDto).getStartingDevice().getRoute(), startingDevice);
    assertNull(((OxygenTaskDto)administrationTaskDto).getStartingDevice().getRouteType());
    assertEquals(taskId, administrationTaskDto.getTaskId());
    assertEquals(therapyId, administrationTaskDto.getTherapyId());
    assertEquals(dueTime, administrationTaskDto.getPlannedAdministrationTime());
    assertEquals(administrationType, administrationTaskDto.getAdministrationTypeEnum());
  }

  @Test
  public void testGetAdministrationStatusByTimeLate()
  {
    final AdministrationStatusEnum status =
        AdministrationStatusEnum.getFromTime(
            new DateTime(2016, 1, 6, 11, 0), null, new DateTime(2016, 1, 6, 12, 0));
    assertEquals(AdministrationStatusEnum.LATE, status);
  }

  @Test
  public void testGetAdministrationStatusByTimeDue()
  {
    final AdministrationStatusEnum status =
        AdministrationStatusEnum.getFromTime(
            new DateTime(2016, 1, 6, 11, 40), null, new DateTime(2016, 1, 6, 12, 0));
    assertEquals(AdministrationStatusEnum.DUE, status);
  }

  @Test
  public void testGetAdministrationStatusByTimeDue2()
  {
    final AdministrationStatusEnum status =
        AdministrationStatusEnum.getFromTime(
            new DateTime(2016, 1, 6, 12, 15), null, new DateTime(2016, 1, 6, 12, 0));
    assertEquals(AdministrationStatusEnum.DUE, status);
  }

  @Test
  public void testGetAdministrationStatusByTimePlanned()
  {
    final AdministrationStatusEnum status =
        AdministrationStatusEnum.getFromTime(
            new DateTime(2016, 1, 6, 12, 45), null, new DateTime(2016, 1, 6, 12, 0));
    assertEquals(AdministrationStatusEnum.PLANNED, status);
  }

  @Test
  public void testGetAdministrationStatusByTimeCompleted()
  {
    final AdministrationStatusEnum status =
        AdministrationStatusEnum.getFromTime(
            new DateTime(2016, 1, 6, 12, 15), new DateTime(2016, 1, 6, 12, 0), null);
    assertEquals(AdministrationStatusEnum.COMPLETED, status);
  }

  @Test
  public void testGetAdministrationStatusByTimeCompletedLate()
  {
    final AdministrationStatusEnum status =
        AdministrationStatusEnum.getFromTime(
            new DateTime(2016, 1, 6, 11, 15), new DateTime(2016, 1, 6, 12, 0), null);
    assertEquals(AdministrationStatusEnum.COMPLETED_LATE, status);
  }

  @Test
  public void testGetAdministrationStatusByTimeCompletedEarly()
  {
    final AdministrationStatusEnum status =
        AdministrationStatusEnum.getFromTime(
            new DateTime(2016, 1, 6, 13, 15), new DateTime(2016, 1, 6, 12, 0), null);
    assertEquals(AdministrationStatusEnum.COMPLETED_EARLY, status);
  }
}
