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

package com.marand.thinkmed.medications;

import java.util.EnumSet;

import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.CheckMentalHealthMedsTaskDef;
import com.marand.thinkmed.medications.task.CheckNewAllergiesTaskDef;
import com.marand.thinkmed.medications.task.DispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.DoctorReviewTaskDef;
import com.marand.thinkmed.medications.task.InfusionBagChangeTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeCompletePreparationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeDispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeStartPreparationTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReminderTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReviewTaskDef;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.SupplyReviewTaskDef;
import com.marand.thinkmed.medications.task.SwitchToOralTaskDef;

/**
 * User: Klavdij Lapajne
 */

public enum TaskTypeEnum
{
  ADMINISTRATION_TASK("AdministrationTask", AdministrationTaskDef.KEY_PREFIX),
  INFUSION_BAG_CHANGE_TASK("InfusionBagChangeTask", InfusionBagChangeTaskDef.KEY_PREFIX),
  PHARMACIST_REVIEW("PharmacistReviewTask", PharmacistReviewTaskDef.KEY_PREFIX),
  PHARMACIST_REMINDER("PharmacistReminderTask", PharmacistReminderTaskDef.KEY_PREFIX),
  SUPPLY_REMINDER("SupplyReminderTask", SupplyReminderTaskDef.KEY_PREFIX),
  SUPPLY_REVIEW("SupplyReviewTask", SupplyReviewTaskDef.KEY_PREFIX),
  DISPENSE_MEDICATION("DispenseMedicationTask", DispenseMedicationTaskDef.KEY_PREFIX),
  DOCTOR_REVIEW("DoctorReview", DoctorReviewTaskDef.KEY_PREFIX),
  SWITCH_TO_ORAL("SwitchToOral", SwitchToOralTaskDef.KEY_PREFIX),
  CHECK_NEW_ALLERGIES("CheckNewAllergies", CheckNewAllergiesTaskDef.KEY_PREFIX),
  CHECK_MENTAL_HEALTH_MEDS("CheckMentalHealthMeds", CheckMentalHealthMedsTaskDef.KEY_PREFIX),
  PERFUSION_SYRINGE_START("PerfusionSyringeStartPreparationTask", PerfusionSyringeStartPreparationTaskDef.KEY_PREFIX),
  PERFUSION_SYRINGE_COMPLETE("PerfusionSyringeCompletePreparationTask", PerfusionSyringeCompletePreparationTaskDef.KEY_PREFIX),
  PERFUSION_SYRINGE_DISPENSE("PerfusionSyringeDispenseMedicationTask", PerfusionSyringeDispenseMedicationTaskDef.KEY_PREFIX);

  private final String name;
  private final String keyPrefix;

  TaskTypeEnum(final String name, final String keyPrefix)
  {
    this.name = name;
    this.keyPrefix = keyPrefix;
  }

  public String getName()
  {
    return name;
  }

  public String buildKey(final String value)
  {
    if (value == null)
    {
      return null;
    }
    return keyPrefix + "_" + value;
  }

  public static TaskTypeEnum getByName(final String name)
  {
    for (final TaskTypeEnum taskTypeEnum : values())
    {
      if (name.equals(taskTypeEnum.name))
      {
        return taskTypeEnum;
      }
    }
    return null;
  }

  public static final EnumSet<TaskTypeEnum> SUPPLY_TASKS_SET = EnumSet.of(
      SUPPLY_REMINDER,
      DISPENSE_MEDICATION,
      SUPPLY_REVIEW);
  public static final EnumSet<TaskTypeEnum> PERFUSION_SYRINGE_TASKS_SET = EnumSet.of(
      PERFUSION_SYRINGE_START,
      PERFUSION_SYRINGE_COMPLETE,
      PERFUSION_SYRINGE_DISPENSE);
}
