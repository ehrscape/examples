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
var TasksOperationService = function ($q, $http, RequestTransformer, ActiveInitData,
                                      PrintPerfusionSyringeTaskViewAction, PerfusionSyringeLabelTypeEnum)
{

  this.supplyTaskOperation = function (operation, taskId, compositionUid, supplyType, supplyInDays, comment)
  {
    var deferred = $q.defer();

    var data = {
      taskId: taskId,
      compositionUid: compositionUid,
      supplyType: supplyType,
      supplyInDays: supplyInDays,
      comment: comment
    };

    $http({
      method: "post",
      url: 'medications/'+operation,
      transformRequest: RequestTransformer.getRequestAsFormPostTransformer(),
      data: data
    }).success(
        function (response)
        {
          deferred.resolve(response);
        }
    ).error(
        function (response)
        {
          deferred.reject(response);
        }
    );
    return deferred.promise;
  };

  this.dismissSupplyTask = function (serverMethod, patientId, taskId)
  {
    var deferred = $q.defer();
    $http({
      skipLanguageParam : true,
      method: "post",
      url: 'medications/'+serverMethod,
      transformRequest: RequestTransformer.getRequestAsFormPostTransformer(),
      data: {
        patientId: patientId,
        taskId: taskId,
        language : ActiveInitData.language
      }
    }).success(function (response)
        {
          deferred.resolve(response);
        }
    ).error(function (response)
        {
          deferred.reject(response);
        }
    );
    return deferred.promise;
  };

  this.confirmSupplyReviewTask = function (patientId, taskId, createSupplyReminder, supplyType, supplyInDays, comment, compositionUid)
  {
    var data = {
      patientId: patientId,
      taskId: taskId,
      createSupplyReminder: createSupplyReminder,
      supplyType: supplyType,
      supplyInDays: supplyInDays,
      comment: comment,
      compositionUid : compositionUid
    };
    var deferred = $q.defer();
    $http({
      method: "post",
      url: 'medications/confirmSupplyReviewTask',
      transformRequest: RequestTransformer.getRequestAsFormPostTransformer(),
      data: data
    }).success(function (response)
        {
          deferred.resolve(response);
        }
    ).error(function (response)
        {
          deferred.reject(response);
        }
    );
    return deferred.promise;
  };


  this.confirmPharmacistDispenseTask = function (patientId, taskId, compositionUid, requesterRole, supplyRequestStatus)
  {
    var data = {
      patientId: patientId,
      taskId: taskId,
      compositionUid: compositionUid,
      requesterRole: requesterRole,
      supplyRequestStatus: supplyRequestStatus
    };
    var deferred = $q.defer();
    $http({
      method: "post",
      url: 'medications/confirmPharmacistDispenseTask',
      transformRequest: RequestTransformer.getRequestAsFormPostTransformer(),
      data: data
    }).success(function (response)
        {
          deferred.resolve(response);
        }
    ).error(function (response)
        {
          deferred.reject(response);
        }
    );
    return deferred.promise;
  };

  this.startPerfusionSyringePreparations = function (patientId, taskIds, originalTherapyIds, isUrgent) {
    var data = {
      patientId: patientId,
      taskIds: JSON.stringify(taskIds),
      originalTherapyIds: JSON.stringify(originalTherapyIds),
      isUrgent: isUrgent,
      language: ActiveInitData.language
    };
    var deferred = $q.defer();
    $http({
      skipLanguageParam : true,
      method: "post",
      url: 'medications/startPerfusionSyringePreparations',
      transformRequest: RequestTransformer.getRequestAsFormPostTransformer(),
      data: data
    }).success(function (response)
        {
          deferred.resolve(response);
        }
    ).error(function (response)
        {
          deferred.reject(response);
        }
    );
    return deferred.promise;
  };

  this.confirmPerfusionSyringePreparations = function (patientId, taskIds, originalTherapyIds, isUrgent) {
    var data = {
      patientId: patientId,
      taskId: JSON.stringify(taskIds),
      isUrgent: isUrgent
    };
    var deferred = $q.defer();
    $http({
      method: "post",
      url: 'medications/confirmPerfusionSyringePreparations',
      transformRequest: RequestTransformer.getRequestAsFormPostTransformer(),
      data: data
    }).success(function (response)
        {
          deferred.resolve(response);
        }
    ).error(function (response)
        {
          deferred.reject(response);
        }
    );
    return deferred.promise;
  };

  this.undoPerfusionSyringeRequestState = function(patientId, taskIds, originalTherapyIds, isUrgent)
  {
    // the service method only accepts 1 task ID, but we're allowing an array
    // so that we can unify the service calls in syringesList.ctrl.js.
    var data = {
      patientId: patientId,
      taskId: taskIds[0],
      isUrgent: isUrgent
    };
    var deferred = $q.defer();
    $http({
      method: "post",
      url: 'medications/undoPerfusionSyringeRequestState',
      transformRequest: RequestTransformer.getRequestAsFormPostTransformer(),
      data: data
    }).success(function(response)
        {
          deferred.resolve(response);
        }
    ).error(function(response)
        {
          deferred.reject(response);
        }
    );
    return deferred.promise;
  };

  this.printPerfusionSyringeTask = function (taskLabels)
  {
    PrintPerfusionSyringeTaskViewAction(taskLabels);
  };

  this.deletePerfusionSyringeTask = function(taskId)
  {
    var data = {
      taskId: taskId,
      language: ActiveInitData.language
    };
    var deferred = $q.defer();
    $http({
      skipLanguageParam: true,
      method: "post",
      url: 'medications/deletePerfusionSyringeRequest',
      transformRequest: RequestTransformer.getRequestAsFormPostTransformer(),
      data: data
    }).success(function(response)
        {
          deferred.resolve(response);
        }
    ).error(function(response)
        {
          deferred.reject(response);
        }
    );
    return deferred.promise;
  };
};
TasksOperationService.$inject = ['$q', '$http', 'tm.angularjs.common.rest.RequestTransformerService', 'ActiveInitData',
'PrintPerfusionSyringeTaskViewAction', 'PerfusionSyringeLabelTypeEnum'];
