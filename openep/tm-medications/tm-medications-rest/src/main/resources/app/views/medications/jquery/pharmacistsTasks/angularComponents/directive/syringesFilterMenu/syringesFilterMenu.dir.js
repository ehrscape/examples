var SyringesFilterMenuDir = function()
{
  var refreshDebounceTimer;
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/syringesFilterMenu/syringesFilterMenu.template.html',
    scope: true,
    controller: ['$scope', 'ActiveInitData', 'SavePerfusionSyringeFilterStateToContext', '$timeout',
      function($scope, ActiveInitData, SavePerfusionSyringeFilterStateToContext, $timeout)
    {
      function debouncedRefreshPerfusionSyringesGrid()
      {
        $timeout.cancel(refreshDebounceTimer);
        refreshDebounceTimer = $timeout(function()
        {
          $scope.refreshPerfusionSyringesGrid();
        }, 500);
      };

      $scope.toggleShowInProgressSyringeTasks = function()
      {
        ActiveInitData.perfusionSyringeFilter.showInProgress = !ActiveInitData.perfusionSyringeFilter.showInProgress;
        ActiveInitData.perfusionSyringeFilter.showDispensed = false;
        SavePerfusionSyringeFilterStateToContext(ActiveInitData.perfusionSyringeFilter);
        debouncedRefreshPerfusionSyringesGrid();
      };

      $scope.toggleShowCompletedSyringeTasks = function()
      {
        ActiveInitData.perfusionSyringeFilter.showCompleted = !ActiveInitData.perfusionSyringeFilter.showCompleted;
        ActiveInitData.perfusionSyringeFilter.showDispensed = false;
        SavePerfusionSyringeFilterStateToContext(ActiveInitData.perfusionSyringeFilter);
        debouncedRefreshPerfusionSyringesGrid();
      };

      $scope.toggleShowDispensedSyringeTasks = function()
      {
        // reset the date to today when toggling
        if (ActiveInitData.perfusionSyringeFilter.showDispensed === true)
        {
          ActiveInitData.perfusionSyringeFilter.closedTasksDate = new Date();
          $scope.datePickerOpen = false;
          ActiveInitData.perfusionSyringeFilter.showInProgress = true; // reset the filter back to default task types
        }
        else
        {
          // radio button logic, when dispensed task types are selected all other types are disabled
          ActiveInitData.perfusionSyringeFilter.showInProgress = false;
          ActiveInitData.perfusionSyringeFilter.showCompleted = false;
        }
        ActiveInitData.perfusionSyringeFilter.showDispensed = !ActiveInitData.perfusionSyringeFilter.showDispensed;
        SavePerfusionSyringeFilterStateToContext(ActiveInitData.perfusionSyringeFilter);
        debouncedRefreshPerfusionSyringesGrid();
      };

      $scope.getPerfusionSyringeFilterState = function()
      {
        return ActiveInitData.perfusionSyringeFilter;
      };

      $scope.onDateChange = function()
      {
        debouncedRefreshPerfusionSyringesGrid();
        SavePerfusionSyringeFilterStateToContext(ActiveInitData.perfusionSyringeFilter);
      };
    }],
    replace: true,
    link: function(scope, element)
    {
    }
  };
};
