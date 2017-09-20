var TaskPrintedDir = function ()
{
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/taskPrinted/taskPrinted.template.html',
    controller: ['$scope', 'SupplyRequestStatus', function ($scope, SupplyRequestStatus)
    {
      $scope.showPrinted = function ()
      {
        return $scope.dataItem.supplyRequestStatus === SupplyRequestStatus.VERIFIED &&
            $scope.dataItem.therapyDayDto &&
            $scope.dataItem.therapyDayDto.therapyPharmacistReviewStatus === true;
      };

      $scope.print = function ()
      {
        //TODO: call print
      };
    }],
    replace: true,
    link: function (scope, ele, attrs)
    {

    }
  };
};


