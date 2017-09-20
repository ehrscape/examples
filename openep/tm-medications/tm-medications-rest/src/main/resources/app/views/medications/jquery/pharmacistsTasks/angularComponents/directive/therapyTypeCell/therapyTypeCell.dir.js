var TherapyTypeCellDir = function()
{
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/therapyTypeCell/therapyTypeCell.template.html',
    controller: ['$scope', 'TaskTypeEnum', 'tm.angularjs.common.tmcBridge.ViewProxy',
      function($scope, TaskTypeEnum, viewProxy)
      {
        $scope.dataItemTransponiert = '';

        if ($scope.dataItem === TaskTypeEnum.PHARMACIST_REVIEW)
        {
          $scope.dataItemTransponiert = viewProxy.getDictionary('therapy.review');
        }
        else if ($scope.dataItem === TaskTypeEnum.PHARMACIST_REMINDER)
        {
          $scope.dataItemTransponiert = viewProxy.getDictionary('therapy.review.reminder');
        }
        else
        {
          $scope.dataItemTransponiert = viewProxy.getDictionary('unknown');
        }

      }],
    replace: true,
    link: function(scope, ele, attrs)
    {
    }
  };
};
