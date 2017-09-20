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
var NurseListGridCtrl = function($scope, $rootScope, $timeout, $q, $http, AdministrationTasksService,
                                 ActiveInitData, ActiveUpdateData, SaveFilterToContext, GetTranslation, viewProxy,
                                 NurseTaskService)
{
  /*************************************************************************************************************************/

  $scope.showNurseTaskGrid = function()
  {
    $scope.clearDataImpl();
    $scope.refreshNurseTaskGridGrid();
  };

  $scope.refreshNurseTaskGridGrid = function()
  {
    viewProxy.showLoaderMask();
    delete $scope.maskData.nurseTaskList['data']; // clears the no data message in the grid
    $rootScope.$emit('setAplicationTypeFilterOptions', []);

    AdministrationTasksService.getAdministrationTasks(
        $scope.isCareProviderFilterEnabled() ? ActiveInitData.careProviderIds : null,
        $scope.isCareProviderFilterEnabled() ? null : ActiveUpdateData.patientIds)
        .then(
            function(data)
            {
              var filterItemsObj = {},
                  filterItems = [];
              data.forEach(function(task)
              {
                if (angular.isObject(task) && angular.isObject(task.therapyDayDto) &&
                    angular.isObject(task.therapyDayDto.therapy) && angular.isArray(task.therapyDayDto.therapy.routes))
                {
                  task.therapyDayDto.therapy.routes.forEach(function markAllRoutes(route){
                    filterItemsObj[route.name.toUpperCase()] = 0;
                  });
                }
              });

              for (var key in filterItemsObj)
              {
                if (filterItemsObj.hasOwnProperty(key))
                {
                  filterItems.push(key);
                }
              }

          $rootScope.$emit('setAplicationTypeFilterOptions', filterItems);
          $scope.maskData.nurseTaskList.data = data;
          $scope.applyAplicationTypeFilter(ActiveInitData.applicationTypes);
        },
        function(data)
        {
          viewProxy.displayRequestErrorNotice(data);
        })
        .finally(function hideLoaderMask()
        {
          viewProxy.hideLoaderMask();
        });
  };

  /**
   * @param {Object} updateData
   */
  $scope.updateDataImpl = function(updateData)
  {
    if (!$scope.isCareProviderFilterEnabled())
    {
      ActiveUpdateData.patientIds = angular.isArray(updateData.patientIds) ? updateData.patientIds : [];
    }
    $scope.showNurseTaskGrid();
  };

  $scope.refreshDataImpl = function()
  {
    $scope.refreshNurseTaskGridGrid();
  };

  $scope.clearDataImpl = function()
  {
    $scope.maskData.nurseTaskList.origData = [];
    $scope.maskData.nurseTaskList.data = [];
  };

  $scope.isCareProviderFilterEnabled = function()
  {
    return ActiveInitData.careProviderFilterEnabled === true;
  };

  $scope.$on('refreshData', function()
  {
    $scope.refreshDataImpl();
  });

  $scope.$on('clearData', function()
  {
    $rootScope.$emit('clearData');
    $scope.clearDataImpl();
  });

  $scope.$on('updateData', function(event, updateData)
  {
    $scope.updateDataImpl(updateData);
  });

  $scope.init = function()
  {
    $scope.maskData = {
      nurseTaskList: {
        filter: [],
        origData: []
      }
    };

    $scope.applyAplicationTypeFilter = function(filterValue)
    {
      $scope.maskData.nurseTaskList.filter = filterValue;

      if ($scope.maskData.nurseTaskList.origData.length === 0)
      {
        $scope.maskData.nurseTaskList.origData = angular.copy($scope.maskData.nurseTaskList.data);
      }

      if ($scope.maskData.nurseTaskList.filter.length === 0)
      {
        $scope.maskData.nurseTaskList.data = $scope.maskData.nurseTaskList.origData;
      }
      else
      {
        var temp = [];
        $scope.maskData.nurseTaskList.origData.forEach(function(task)
        {
          if (task.therapyDayDto && task.therapyDayDto.therapy && angular.isArray(task.therapyDayDto.therapy.routes))
          {
            task.therapyDayDto.therapy.routes.forEach(function checkRoute(route){
              if ($scope.maskData.nurseTaskList.filter.contains(route.name.toUpperCase()))
              {
                temp.push(task);
              }
            });
          }
        });
        $scope.maskData.nurseTaskList.data = temp;
      }

    };

    //directives callbacks
    $scope.confirm = function(selectedIds, zeroSelectedCareproviders)
    {
      //ce ni izbran noben potem dobimo nazaj VSE idje
      //vendar jih ne shranimo v context - saj bi v tem primeru pri naslednjem odprtju imel vse izbrane
      if (zeroSelectedCareproviders === true)
      {
        SaveFilterToContext([], ActiveInitData.applicationTypes);
      }
      else
      {
        SaveFilterToContext(selectedIds, ActiveInitData.applicationTypes);
      }
      ActiveInitData.careProviderIds = selectedIds;
      $scope.clearDataImpl();
      $scope.refreshDataImpl();
    };

    $scope.cancel = function()
    {
    };

    $scope.careproviderSelectorDataProvider = NurseTaskService.careproviderSelectorDataProvider;

    $scope.getContextCareproviders = function()
    {
      return ActiveInitData.careProviderIds;
    };

  };
  $scope.init();

};
NurseListGridCtrl.$inject = ['$scope', '$rootScope', '$timeout', '$q', '$http', 'AdministrationTasksService',
  'ActiveInitData', 'ActiveUpdateData', 'SaveFilterToContext', 'GetTranslation', 'tm.angularjs.common.tmcBridge.ViewProxy',
  'NurseTaskService'];