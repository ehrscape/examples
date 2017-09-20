package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Nejc Korasa
 */
public abstract class TherapyTaskDef extends MedsTaskDef
{
  public static final TaskVariable ORIGINAL_THERAPY_ID = TaskVariable.named("originalTherapyId");
}
