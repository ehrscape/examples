
var OpenCloseFilterDir = function ()
{
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/openCloseFilter/openCloseFilter.template.html',
    controllerAs: 'careproviderSelector',
    scope : {
      clickCallback : '=clickCallback',
      getContextFilters :'=selected'
    },

    controller: ['$scope', '$rootScope', 'tm.angularjs.common.tmcBridge.ViewProxy', function ($scope, $rootScope, viewProxy)
    {
      $rootScope.$on('closeDialogs', function ()
      {
        $scope.opened = false;
      });
      $rootScope.$on('documentClicked', function (event, target, escKey)
      {
        if ((target.target.parentNode === null) || (target.target.parentNode.isSameNode($scope.element[0])))
        {
          return ;
        }
        else if ((escKey) || (!target.target.isSameNode($scope.element[0].children[0])))
        {
          $scope.opened = false;
          $scope.$apply();
        }
      });

      var init = function ()
      {
        $scope.openedFilter = viewProxy.getDictionary('tasks.opened');
        $scope.closedFilter = viewProxy.getDictionary('tasks.closed');
        $scope.filter = viewProxy.getDictionary('filter');
      }();

      $scope.selectedFilter = null;
      if($scope.getContextFilters)
      {
        $scope.selectedFilter = $scope.getContextFilters()
      }

      if($scope.selectedFilter === null)
      {
        $scope.selectedFilter = 'open';
      }

      $scope.filterOpened = function ()
      {
        $scope.selectedFilter = 'open';
        $scope.clickCallback($scope.selectedFilter);
      };
      $scope.filterClosed = function ()
      {
        $scope.selectedFilter = 'close';
        $scope.clickCallback($scope.selectedFilter);
      };

      $rootScope.$on('clearData', function ()
      {
        $scope.selectedFilter = $scope.getContextFilters();
      });

      $scope.showFilter = function ()
      {
        $scope.opened = true;
      };
      $scope.hideFilter = function ()
      {
        $scope.opened = false;
      };
    }],
    replace: true,
    link: function (scope, element)
    {
      scope.element = element;
      scope.opened = false;
    }
  };
};
