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
var TherapyActionDir = function ()
{
  return {
    restrict: 'E',
    scope: {},
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/therapyAction/therapyAction.template.html',
    controller: ['$scope', '$rootScope', '$http', 'TasksOperationService', 'PharmacistsTaskService', 'tm.angularjs.common.tmcBridge.ViewProxy', 'TaskType', function ($scope, $rootScope, $http, TasksOperationService, PharmacistsTaskService, viewProxy, TaskType)
    {
      $scope.dataItem = $scope.$parent.dataItem;
      $scope.init = function ()
      {
        $scope.maskData = {
          showResupplyForm: false
        };
      };
      $scope.init();

      $scope.confirmTask = function() {
        $scope.maskData.disabled = true;
        if($scope.dataItem.taskType !== TaskType.DISPENSE_MEDICATION)
        {
          TasksOperationService.confirmSupplyReviewTask($scope.dataItem.patientDisplayDto.id, $scope.dataItem.id, false, null, null, null, $scope.dataItem.therapyDayDto.therapy.compositionUid)
              .then(function(response)
              {
                $scope.maskData.disabled = false;
                $scope.$emit('refreshData');
              },
              function(response)
              {
                $scope.maskData.disabled = false;
                viewProxy.displayRequestErrorNotice(response);
              });
        }
        else
        {
          TasksOperationService.confirmPharmacistDispenseTask($scope.dataItem.patientDisplayDto.id, $scope.dataItem.id, $scope.dataItem.therapyDayDto.therapy.compositionUid, $scope.dataItem.requesterRole, $scope.dataItem.supplyRequestStatus)
              .then(function(response)
              {
                $scope.maskData.disabled = false;
                $scope.$emit('refreshData');
              },
              function(response)
              {
                $scope.maskData.disabled = false;
                viewProxy.displayRequestErrorNotice(response);
              });
        }
      };
      $rootScope.$on('closeDialogs', function ()
      {
        $scope.maskData.showResupplyForm = false;
      });

      $scope.positionFormDialog = function ($event)
      {
        $rootScope.$emit('closeDialogs');
        var tempResupplyFormPosition = PharmacistsTaskService.getElementTopLeft($event, 300, -20);
        $scope.maskData.formPosition = PharmacistsTaskService.calculateTopResupplyFormTopPosition($event.target, tempResupplyFormPosition);
      };

      $scope.closeTherapyActionDialog = function ()
      {
        $scope.maskData.showResupplyForm = false;
      };

    }],
    replace: true,
    link: function (scope, ele, attrs)
    {
    }
  };
};
