package com.marand.thinkmed.medications.task;

import java.util.Map;

import com.marand.thinkmed.bpm.medications.process.PharmacySupplyProcess;
import com.marand.thinkmed.bpm.medications.process.PreparePerfusionSyringeProcess;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.process.TaskConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */
public class TaskListenerUtils
{
  private TaskListenerUtils()
  {
  }

  public static void setMedicationsTaskDefVariables(
      final String patientId,
      final Map<String, Object> variablesMap)
  {
    variablesMap.put(TaskConstants.TASK_GROUP_VARIABLE_NAME, MedsTaskDef.GROUP_NAME);
    variablesMap.put(MedsTaskDef.PATIENT_ID.getName(), patientId);
  }

  public static void setTherapyTaskDefVariables(
      final String patientId,
      final String originalTherapyId,
      final Map<String, Object> variablesMap)
  {
    setMedicationsTaskDefVariables(patientId, variablesMap);
    variablesMap.put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), originalTherapyId);
  }

  public static void setSupplyTaskDefVariables(final DelegateTask delegateTask, final Map<String, Object> variablesMap)
  {
    final DelegateExecution execution = delegateTask.getExecution();
    final String patientId = (String)execution.getVariable(PharmacySupplyProcess.patientId.name());
    setMedicationsTaskDefVariables(patientId, variablesMap);
    //todo  before use
    //public static final TaskVariable ORIGINAL_THERAPY_ID = TaskVariable.named("originalTherapyId");
    //public static final TaskVariable SUPPLY_TYPE = TaskVariable.named("supplyType");
    //public static final TaskVariable DAYS_SUPPLY = TaskVariable.named("supplyInDays");
    //public static final TaskVariable SUPPLY_REQUEST_COMMENT = TaskVariable.named("supplyRequestComment");
  }

  public static void setPerfusionSyringeTaskDefVariables(
      final DelegateTask delegateTask,
      final String taskExecutionId,
      final TaskTypeEnum taskTypeEnum,
      final Map<String, Object> variablesMap)
  {
    final DelegateExecution execution = delegateTask.getExecution();
    final String patientId = (String)execution.getVariable(PreparePerfusionSyringeProcess.patientId.name());
    setTherapyTaskDefVariables(
        patientId,
        (String)execution.getVariable(PreparePerfusionSyringeProcess.originalTherapyId.name()),
        variablesMap);

    delegateTask.setName(taskTypeEnum.buildKey(String.valueOf(patientId)));
    execution.setVariable(PreparePerfusionSyringeProcess.undoState.name(), false);

    variablesMap.put(TaskConstants.TASK_EXECUTION_ID_VARIABLE_NAME, taskExecutionId);
    variablesMap.put(TaskConstants.TASK_GROUP_VARIABLE_NAME, PerfusionSyringeTaskDef.GROUP_NAME);
    variablesMap.put(
        PerfusionSyringeTaskDef.IS_URGENT.getName(),
        execution.getVariable(PreparePerfusionSyringeProcess.isUrgent.name()));
    variablesMap.put(
        PerfusionSyringeTaskDef.NUMBER_OF_SYRINGES.getName(),
        execution.getVariable(PreparePerfusionSyringeProcess.numberOfSyringes.name()));
    variablesMap.put(
        PerfusionSyringeTaskDef.PREPARATION_STARTED_TIME_MILLIS.getName(),
        execution.getVariable(PreparePerfusionSyringeProcess.startedDateTimeMillis.name()));
    variablesMap.put(
        PerfusionSyringeTaskDef.ORDERER.getName(),
        execution.getVariable(PreparePerfusionSyringeProcess.orderer.name()));
    variablesMap.put(
        PerfusionSyringeTaskDef.ORDERER_FULL_NAME.getName(),
        execution.getVariable(PreparePerfusionSyringeProcess.ordererFullName.name()));
    variablesMap.put(
        PerfusionSyringeTaskDef.PRINT_SYSTEM_LABEL.getName(),
        execution.getVariable(PreparePerfusionSyringeProcess.printSystemLabel.name()));

    final Long dueDateMillis =
        (Long)execution.getVariable(PreparePerfusionSyringeProcess.dueDateTimeMillis.name());
    delegateTask.setDueDate(new DateTime(dueDateMillis).toDate());
  }
}
