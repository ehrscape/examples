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
var AplicationTypeDir = function()
{
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/nurseTasks/angularComponents/directive/aplicationType/aplicationType.template.html',
    controllerAs: 'aplicationType',
    scope: {
      clickCallback: '=clickCallback',
      getContextFilters: '=selected'
    },
    controller: ['$scope', '$rootScope', 'ActiveInitData', 'SaveFilterToContext',
      'tm.angularjs.common.tmcBridge.ViewProxy', function($scope, $rootScope, activeInitData, saveFilterToContext, viewProxy)
      {
        $rootScope.$on('closeDialogs', function()
        {
          $scope.opened = false;
        });
        $rootScope.$on('setAplicationTypeFilterOptions', function(event, filterItems)
        {
          $scope.data.options = [];

          for (var i = 0; i < filterItems.length; i++)
          {
            var contextIndex = activeInitData.applicationTypes.indexOf(filterItems[i]);
            $scope.data.options.push({
              key: filterItems[i],
              selected: contextIndex > -1
            });
          }
          _updateFilterTitle();
        });

        $scope.getSelectedFilter = function()
        {
          var selected = [];
          for (var i = 0; i < $scope.data.options.length; i++)
          {
            if ($scope.data.options[i].selected)
            {
              selected.push($scope.data.options[i].key);
            }
          }
          return selected;
        };
        $scope.filterClick = function(option)
        {
          option.selected = !option.selected;
          var selectedItems = $scope.getSelectedFilter();
          $scope.clickCallback(selectedItems);

          _updateFilterTitle();
          activeInitData.applicationTypes = selectedItems;
          saveFilterToContext(activeInitData.careProviderIds, activeInitData.applicationTypes);
        };

        $scope.showFilter = function()
        {
          $scope.opened = true;
        };
        $scope.hideFilter = function()
        {
          $scope.opened = false;
        };

        var init = function()
        {
          $scope.data = {
            options: []
          };
          _updateFilterTitle();
        }();

        function _updateFilterTitle()
        {
          $scope.data.title = _generateFilterTitle();
        }

        /**
         * @returns {String}
         * @private
         */
        function _generateFilterTitle()
        {
          var selected = $scope.getSelectedFilter();
          if (selected.length > 0)
          {
            if (selected.length <= 3)
            {
              return viewProxy.getDictionary('route.short') + ": " + selected.join(', ');
            }
            return viewProxy.getDictionary('route.short') + ": " + viewProxy.getDictionary('filtered');
          }
          return viewProxy.getDictionary('all.routes.short');
        }
      }],
    replace: true,
    link: function(scope, element)
    {
      scope.element = element;
      scope.opened = false;
    }
  };
};
