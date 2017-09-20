var MoreDropDownMenuDir = function ()
{
  return {
    restrict: 'E',
    scope: {},
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/moreDropDownMenu/moreDropDownMenu.template.html',
    controller: ['$scope', '$rootScope', 'TasksOperationService', 'PharmacistsTaskService', 'TaskTypeEnum', 'ActiveInitData', 'tm.angularjs.common.tmcBridge.ViewProxy', function ($scope, $rootScope, TasksOperationService, PharmacistsTaskService, TaskTypeEnum, viewProxy)
    {

      $scope.init = function ()
      {
        $scope.dataItem = $scope.$parent.dataItem;
        $scope.maskData = {
          showSupplyForm: false
        };
      };
      $scope.init();

      $scope.positionDropDownMenu = function ($event)
      {
        $rootScope.$emit('closeDialogs');
        var popupWidth = $scope.dataItem.taskType  === 'SUPPLY_REVIEW' ? 170 : 120;
        $scope.maskData.dropDownPosition = PharmacistsTaskService.getElementTopLeft($event, popupWidth, 0);
        $scope.maskData.dropDownPosition.width = popupWidth+'px';
      };

      $scope.positionFormDialog = function ($event)
      {
        $rootScope.$emit('closeDialogs');
        var left = $scope.dataItem.taskType  === 'SUPPLY_REVIEW' ? 140 : 180;
        $scope.maskData.formMode = $scope.dataItem.taskType  === 'SUPPLY_REVIEW' ? 1 : 2;
        var tempResupplyFormPosition = PharmacistsTaskService.getElementTopLeft($event, left, 0);
        $scope.maskData.formPosition = PharmacistsTaskService.calculateTopResupplyFormTopPosition($event.target, tempResupplyFormPosition);
      };

      $scope.closeTherapyActionDialog = function ()
      {
        $scope.maskData.showSupplyForm = false;
      };


      $scope.confirmEditTask = function ()
      {
      };

      $scope.dismissTask = function ()
      {
        var selfTaskId = $scope.dataItem.id;

        var dismissFunction = null;
        if ($scope.dataItem.taskType === TaskTypeEnum.SUPPLY_REMINDER)
        {
          dismissFunction = 'dismissPharmacistSupplyTask';
        }
        else if ($scope.dataItem.taskType === TaskTypeEnum.SUPPLY_REVIEW)
        {
          dismissFunction = 'dismissNurseSupplyTask';
        }

        if (dismissFunction)
        {
          TasksOperationService.dismissSupplyTask(dismissFunction, $scope.dataItem.patientDisplayDto.id, $scope.dataItem.id).
              then(function ()
              {
                $scope.closeTherapyActionDialog();
                $rootScope.$emit('removeSupplyTask', selfTaskId);
              },
              function (response)
              {
                $scope.closeTherapyActionDialog();
                viewProxy.displayRequestErrorNotice(response);
              });
        }
      };

      $rootScope.$on('closeDialogs', function ()
      {
        $scope.maskData.showSupplyForm = false;
        $scope.maskData.showDropDownContextMenu = false;
      });
      $rootScope.$on('documentClicked', function (event, target, escKey)
      {
        if((escKey) || (!target.target.isSameNode($scope.element[0].children[0])))
        {
          $scope.maskData.showDropDownContextMenu = false;
          $scope.$apply();
        }
      });

    }],
    replace: true,
    link: function (scope, element, attrs)
    {
      scope.element = element;
    }
  };
};


