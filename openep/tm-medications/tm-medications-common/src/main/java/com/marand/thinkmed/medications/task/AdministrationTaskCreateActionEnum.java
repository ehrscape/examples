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

package com.marand.thinkmed.medications.task;

/**
 * @author Mitja Lapajne
 */
public enum AdministrationTaskCreateActionEnum
{
  PRESET_TIME_ON_NEW_PRESCRIPTION(true, true),
  PRESET_TIME_ON_MODIFY(false, true),
  PREVIEW_TIMES_ON_NEW_PRESCRIPTION(true, true),
  PRESCRIBE(true, true),
  MODIFY(true, true),
  MODIFY_BEFORE_START(true, true),
  AUTO_CREATE(false, false),
  SUSPEND(false, true),
  REISSUE(false, true),
  ABORT(false, true);

  private final boolean createTasksFromTherapyStart;
  private final boolean taskCreationIntervalStartIncluded;

  AdministrationTaskCreateActionEnum(
      final boolean createTasksFromTherapyStart,
      final boolean taskCreationIntervalStartIncluded)
  {
    this.createTasksFromTherapyStart = createTasksFromTherapyStart;
    this.taskCreationIntervalStartIncluded = taskCreationIntervalStartIncluded;
  }

  public boolean isCreateTasksFromTherapyStart()
  {
    return createTasksFromTherapyStart;
  }

  public boolean isTaskCreationIntervalStartIncluded()
  {
    return taskCreationIntervalStartIncluded;
  }
}
