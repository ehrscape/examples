/*
 * Copyright (c) 2010-2016 Marand d.o.o. (www.marand.com)
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
var SyringesListController = function($scope, TasksOperationService, TaskTypeEnum, viewProxy, systemDialogService)
{
  var _taskStepResolver = {};
  var self = this;

  /*
   *  Public functions
   */
  function isRequestDefined(request)
  {
    return (angular.isDefined(request) && angular.isArray(request.tasksList) && request.tasksList.length > 0);
  }

  function isRequestAtSpecificState(request, taskTypeEnum)
  {
    if (isRequestDefined(request) && angular.isDefined(taskTypeEnum))
    {
      return getRequestEarliestTaskType(request) === taskTypeEnum;
    }
    return false;
  }

  function startPerfusionSyringePreparations(request, tasks)
  {
    _progressTasks(request, tasks, TasksOperationService.startPerfusionSyringePreparations,
        TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE, true);
  }

  function confirmPerfusionSyringePreparations(request, tasks)
  {
    _progressTasks(request, tasks, TasksOperationService.confirmPerfusionSyringePreparations,
        TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE);
  }

  function undoPerfusionSyringeStartPreparations(request, tasks)
  {
    _progressTasks(request, tasks, TasksOperationService.undoPerfusionSyringeRequestState,
        TaskTypeEnum.PERFUSION_SYRINGE_START);
  }

  function undoPerfusionSyringePreparationsCompletion(request, tasks)
  {
    _progressTasks(request, tasks, TasksOperationService.undoPerfusionSyringeRequestState,
        TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE);
  }

  function isTaskOfTypeStart(task)
  {
    if (angular.isDefined(task))
    {
      return task.taskType === TaskTypeEnum.PERFUSION_SYRINGE_START;
    }
    return false;
  }

  function isTaskOfTypeComplete(task)
  {
    if (angular.isDefined(task))
    {
      return task.taskType === TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE;
    }
    return false;
  }

  function isTaskOfTypeDispense(task)
  {
    if (angular.isDefined(task))
    {
      return task.taskType === TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE;
    }
    return false;
  }

  function isRequestAtStartState(request)
  {
    return isRequestAtSpecificState(request, TaskTypeEnum.PERFUSION_SYRINGE_START);
  }

  function isRequestAtConfirmState(request)
  {
    return isRequestAtSpecificState(request, TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE);
  }

  function isRequestAtDispenseState(request)
  {
    return isRequestAtSpecificState(request, TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE);
  }

  function getRequestEarliestTaskType(request)
  {
    if (isRequestDefined(request))
    {
      var eldestType = TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE;

      request.tasksList.forEach(function(task)
      {
        if (_taskStepResolver[task.taskType].ordinal < _taskStepResolver[eldestType].ordinal)
        {
          eldestType = task.taskType;
        }
      });

      return eldestType;
    }
    return TaskTypeEnum.PERFUSION_SYRINGE_START;
  }

  function advanceRequestTasks(request)
  {
    if (isRequestDefined(request))
    {
      var countMap = {};
      for (var taskType in _taskStepResolver)
      {
        countMap[taskType] = 0;
      }

      request.tasksList.forEach(function(task)
      {
        countMap[task.taskType]++;
      });

      var progressTaskType = countMap[TaskTypeEnum.PERFUSION_SYRINGE_START] > 0 ? TaskTypeEnum.PERFUSION_SYRINGE_START :
          ( countMap[TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE] > 0 ? TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE : null );

      if (progressTaskType != null)
      {
        var tasks = request.tasksList.filter(function(task)
        {
          return task.taskType === progressTaskType;
        });

        if (tasks.length > 0)
        {
          _taskStepResolver[progressTaskType].nextStep(request, tasks);
        }
      }
    }
  }

  function printAllLabels(tasks)
  {
    if (angular.isDefined(tasks))
    {
      tasks = angular.isArray(tasks) ? tasks : [tasks];
      var labels = [];
      tasks.forEach(function(task)
      {
        var perfusionSyringeTherapyLabelsPrintDto = {
          label: task.perfusionSyringeLabelDto,
          numberOfMedicationLabels: task.numberOfSyringes,
          printSystemLabel: task.printSystemLabel
        };
        labels.push(perfusionSyringeTherapyLabelsPrintDto);
      });

      if (labels.length > 0)
      {
        TasksOperationService.printPerfusionSyringeTask(labels);
      }
    }
  }

  function printMedicationLabel(task)
  {
    var perfusionSyringeTherapyLabelsPrintDto = {
      label: task.perfusionSyringeLabelDto,
      numberOfMedicationLabels: task.numberOfSyringes,
      printSystemLabel: false
    };
    TasksOperationService.printPerfusionSyringeTask([perfusionSyringeTherapyLabelsPrintDto]);

  }

  function printSystemLabel(task)
  {
    var perfusionSyringeTherapyLabelsPrintDto = {
      label: task.perfusionSyringeLabelDto,
      numberOfMedicationLabels: 0,
      printSystemLabel: true
    };
    TasksOperationService.printPerfusionSyringeTask([perfusionSyringeTherapyLabelsPrintDto]);
  }

  function deletePerfusionSyringeTask(request, task)
  {
    if (angular.isDefined(request) && angular.isDefined(task))
    {
      TasksOperationService.deletePerfusionSyringeTask(task.id)
          .then(function()
              {
                var listData = $scope.maskData.perfusionSyringes.data;
                var tasksList = request.tasksList;

                tasksList.splice(tasksList.indexOf(task), 1);
                if (tasksList.lenght === 0)
                {
                  listData.splice(listData.indexOf(request), 1);
                }
              },
              function(response)
              {
                viewProxy.displayRequestErrorNotice(response);
              });
    }
  }

  /**
   *
   * @param {Object} request
   * @param {Object | Array} tasks
   * @param {Function} serviceMethod
   * @param {String} successEnum
   * @param {Boolean} printLabels
   * @private
   */
  function _progressTasks(request, tasks, serviceMethod, successEnum, printLabels)
  {
    viewProxy.showLoaderMask();

    if (!angular.isArray(tasks))
    {
      tasks = [tasks];
    }

    var patientId = request.patientDisplayDto ? request.patientDisplayDto.id : null;
    var taskIds = tasks.map(function(task)
    {
      return task.id;
    });
    var originalTherapyIds = tasks.map(function(task)
    {
      return task.originalTherapyId;
    });

    serviceMethod(patientId, taskIds, originalTherapyIds, request.urgent)
        .then(function(response)
            {
              // need to update the task Id, since it's a new task once progressed
              tasks.forEach(function(task)
              {
                var key = task.originalTherapyId;
                if (angular.isDefined(response))
                {
                  if (response.hasOwnProperty(key))
                  {
                    var newTaskData = response[key];
                    task.taskType = successEnum;

                    // in case of start perfusion syringe action, we get an object with
                    // the new key and label DTO, otherwise we just get a string with the new key
                    if (angular.isObject(newTaskData))
                    {
                      task.id = newTaskData.completePreparationTaskId;
                      task.perfusionSyringeLabelDto = newTaskData.perfusionSyringeLabelDto;
                      // since we got the label back, we should
                    }
                    else
                    {
                      task.id = newTaskData;
                    }
                  }
                  else
                  {
                    task.isDirty = true;
                  }
                }
              });
              if (printLabels === true)
              {
                printAllLabels(tasks);
              }

              var dirtyTasks = tasks.filter(function isDirtyTask(task)
              {
                return task.isDirty;
              });

              if (dirtyTasks.length > 0)
              {
                var dialog = systemDialogService.openWarningSystemDialog(viewProxy.getDictionary("warning"),
                    viewProxy.getDictionary("perfusion.syringe.preparation.therapy.aborted.warning"), 400, 140, true);

                dialog.result.then(function()
                {
                  _purgeRequestsDirtyTasksFromDataCache(request, dirtyTasks);
                });
              }
            },
            function(response)
            {
              viewProxy.displayRequestErrorNotice(response);
            })
        .finally(function hideLoaderMask()
        {
          viewProxy.hideLoaderMask()
        });
  }

  /**
   * Removes the requested tasks from the specified request. If no tasks remain in the request,
   * it's also removed from the data cache.
   * @param {Object} request
   * @param {Array} dirtyTasks
   * @private
   */
  function _purgeRequestsDirtyTasksFromDataCache(request, dirtyTasks)
  {
    var taskList = request.tasksList;

    dirtyTasks.forEach(function(dirtyTask)
    {
      var taskIndex = taskList.indexOf(dirtyTask);
      if (taskIndex > -1)
      {
        taskList.splice(taskIndex, 1);
      }
    });

    if (taskList.length === 0)
    {
      var requestIndex = $scope.maskData.perfusionSyringes.data.indexOf(request);
      if (requestIndex > -1)
      {
        $scope.maskData.perfusionSyringes.data.splice(requestIndex, 1);
      }
    }
  }

  function initExposedMethods()
  {
    $scope.startPerfusionSyringePreparations = startPerfusionSyringePreparations;
    $scope.confirmPerfusionSyringePreparations = confirmPerfusionSyringePreparations;
    $scope.isTaskOfTypeStart = isTaskOfTypeStart;
    $scope.isTaskOfTypeComplete = isTaskOfTypeComplete;
    $scope.isTaskOfTypeDispense = isTaskOfTypeDispense;
    $scope.isRequestAtStartState = isRequestAtStartState;
    $scope.isRequestAtConfirmState = isRequestAtConfirmState;
    $scope.isRequestAtDispenseState = isRequestAtDispenseState;
    $scope.getRequestEarliestTaskType = getRequestEarliestTaskType;
    $scope.advanceRequestTasks = advanceRequestTasks;
    $scope.printAllLabels = printAllLabels;
    $scope.printMedicationLabel = printMedicationLabel;
    $scope.printSystemLabel = printSystemLabel;
    $scope.deletePerfusionSyringeTask = deletePerfusionSyringeTask;
    $scope.undoPerfusionSyringeStartPreparations = undoPerfusionSyringeStartPreparations;
    $scope.undoPerfusionSyringePreparationsCompletion = undoPerfusionSyringePreparationsCompletion;
  }

  function initController()
  {
    _taskStepResolver[TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE] = {
      nextStep: angular.noop,
      ordinal: 3
    };
    _taskStepResolver[TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE] = {
      nextStep: confirmPerfusionSyringePreparations,
      ordinal: 2
    };
    _taskStepResolver[TaskTypeEnum.PERFUSION_SYRINGE_START] = {
      nextStep: startPerfusionSyringePreparations,
      ordinal: 1
    };

    initExposedMethods();
  }

  initController();
};
SyringesListController.$inject = ['$scope', 'TasksOperationService', 'TaskTypeEnum',
  'tm.angularjs.common.tmcBridge.ViewProxy', 'tm.angularjs.gui.components.dialog.SystemDialogService'];