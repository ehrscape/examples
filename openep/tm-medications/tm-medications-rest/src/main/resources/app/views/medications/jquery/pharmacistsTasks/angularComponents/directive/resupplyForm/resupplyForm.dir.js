var ResupplyFormDir = function ()
{
  return {
    restrict: 'E',
    replace: true,
    scope: {
      dataItem: '=item',
      formPosition: '=formPosition',
      showResupplyForm: '=showResupplyForm',
      formMode: '=formMode'
    },
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/resupplyForm/resupplyForm.template.html',
    controller: ['$scope', '$rootScope', '$http', 'TasksOperationService', 'TaskTypeEnum', 'tm.angularjs.common.tmcBridge.ViewProxy', function ($scope, $rootScope, $http, TasksOperationService, TaskTypeEnum, viewProxy)
    {
      $scope.init = function ()
      {
        $scope.data = {
        };
        $scope.maskData = {};
      };
      $scope.init();

      $scope.$watch('data.days', function (newValue, oldValue)
      {
        if(!newValue) //prevent endless angular digest
        {
          return;
        }
        if ((newValue !== null) && (newValue.length > 0) && (isFinite(newValue) === true))
        {
          $scope.data.days = oldValue;
        }
      });

      $scope.$watch('showResupplyForm', function (newValue)
      {
        if (newValue === true)
        {
          $scope.data = {
            stockType: $scope.dataItem.supplyTypeEnum,
            days: parseInt($scope.dataItem.supplyInDays),
            comment: ''
          };
        }
      });

      $scope.submitResuppyAction = function ()
      {
        var selfTaskId = $scope.dataItem.id;
        if ($scope.dataItem.taskType === TaskTypeEnum.SUPPLY_REMINDER)
        {
          var operation = $scope.formMode === 2 ? 'editSupplyReminderTask' : 'confirmSupplyReminderTask';
          TasksOperationService.supplyTaskOperation(operation, $scope.dataItem.id,  $scope.dataItem.therapyDayDto.therapy.compositionUid, $scope.data.stockType, $scope.data.days, $scope.data.comment).
              then(function (response)
              {
                $scope.showResupplyForm = false;
                if(operation === 'editSupplyReminderTask')
                {
                  $scope.dataItem.supplyTypeEnum = $scope.data.stockType;
                  $scope.dataItem.supplyInDays = $scope.data.days;
                }
                else
                {
                  $scope.$emit('refreshData');
                }
                //$rootScope.$emit('removeSupplyTask', selfTaskId);
              },
              function (response)
              {
                viewProxy.displayRequestErrorNotice(response);
              });
        }
        else if ($scope.dataItem.taskType === TaskTypeEnum.SUPPLY_REVIEW)
        {
          TasksOperationService.confirmSupplyReviewTask($scope.dataItem.patientDisplayDto.id, $scope.dataItem.id, true, $scope.data.stockType, $scope.data.days, $scope.data.comment, $scope.dataItem.therapyDayDto.therapy.compositionUid).
              then(function (response)
              {
                //TODO: what to do here?
                $scope.showResupplyForm = false;
                $rootScope.$emit('removeSupplyTask', selfTaskId);
              },
              function (response)
              {
                viewProxy.displayRequestErrorNotice(response);
              });
        }
      };
      $scope.closeResupplyForm = function ()
      {
        $scope.showResupplyForm = false;
        //revertamo na prvotne vrednosti
        $scope.data = {
          stockType: $scope.dataItem.supplyTypeEnum,
          days: parseInt($scope.dataItem.supplyInDays),
          comment: ''
        };
      };
      $rootScope.$on("documentClicked", function (event, target, escKey)
      {
        if (escKey)
        {
          $scope.$apply(function ()
          {
            $scope.closeResupplyForm();
          });
        }
      });


    }]
  };
};
