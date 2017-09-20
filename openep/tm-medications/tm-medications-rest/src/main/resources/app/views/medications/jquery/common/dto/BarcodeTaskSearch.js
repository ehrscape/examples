/*
 * Copyright (c) 2010-2017 Marand d.o.o. (www.marand.com)
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
Class.define('app.views.medications.common.dto.BarcodeTaskSearch', 'tm.jquery.Object', {
  statics: {
    TASK_FOUND: "TASK_FOUND",
    NO_MEDICATION: "NO_MEDICATION",
    NO_TASK: "NO_TASK",
    MULTIPLE_TASKS: "MULTIPLE_TASKS"
  },
  barcodeSearchResult: null,
  medicationId: null,
  taskId: null,

  /**
   * @returns {String}
   */
  getBarcodeSearchResult: function()
  {
    return this.barcodeSearchResult;
  },

  /**
   * @param {String} barcodeSearchResult
   */
  setBarcodeSearchResult: function(barcodeSearchResult)
  {
    this.barcodeSearchResult = barcodeSearchResult;
  },

  /**
   * @returns {Number}
   */
  getMedicationId: function()
  {
    return this.medicationId;
  },

  /**
   * @param {Number} medicationId
   */
  setMedicationId: function(medicationId)
  {
    this.medicationId = medicationId;
  },

  /**
   * @returns {Number}
   */
  getTaskId: function()
  {
    return this.taskId;
  },

  /**
   * @param {Number} taskId
   */
  setTaskId: function(taskId)
  {
    this.taskId = taskId;
  },

  /**
   * @returns {boolean}
   */
  isFailed: function()
  {
    return this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.NO_MEDICATION ||
        this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.NO_TASK ||
        this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.MULTIPLE_TASKS
  },

  /**
   * @returns {boolean}
   */
  isTaskFound: function()
  {
    return this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.TASK_FOUND;
  },

  /**
   * @returns {String}
   */
  getFailedMessageKey: function()
  {
    if (this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.MULTIPLE_TASKS ||
        this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.NO_TASK)
    {
      return "scanned.medication.no.due.administration";
    }
    else if (this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.NO_MEDICATION)
    {
      return "scanned.code.not.in.database";
    }
  }
});