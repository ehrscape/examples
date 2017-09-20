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

package com.marand.thinkmed.bpm.medications.process;

import com.marand.ispek.bpm.definition.ExecutionVariable;
import com.marand.ispek.bpm.definition.ProcessMessage;
import com.marand.ispek.bpm.definition.ProcessName;
import com.marand.ispek.bpm.definition.ProcessVariable;

/**
 * @author Klavdij Lapajne
 */
@ProcessName("PreparePerfusionSyringeProcess")
public enum PreparePerfusionSyringeProcess
{
  @ProcessVariable
  patientId,
  @ProcessVariable
  originalTherapyId,

  @ProcessMessage
  cancelTherapyMessage,
  @ProcessMessage
  cancelOrderMessage,
  @ProcessMessage
  medicationAdministrationMessage,

  @ExecutionVariable
  numberOfSyringes,
  @ExecutionVariable
  isUrgent,
  @ExecutionVariable
  startedDateTimeMillis,
  @ExecutionVariable
  dueDateTimeMillis,
  @ExecutionVariable
  cancelPreparation,
  @ExecutionVariable
  orderCanceled,
  @ExecutionVariable
  therapyCanceled,
  @ExecutionVariable
  undoState,
  @ExecutionVariable
  orderer,
  @ExecutionVariable
  ordererFullName,
  @ExecutionVariable
  printSystemLabel
}
